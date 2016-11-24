package de.appsist.service.mid;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import de.appsist.commons.event.MachineStateChangedEvent;
import de.appsist.commons.event.UserOfflineEvent;
import de.appsist.commons.event.UserOnlineEvent;
import de.appsist.commons.misc.ServiceStatus;
import de.appsist.commons.misc.StatusSignalConfiguration;
import de.appsist.commons.misc.StatusSignalSender;
import de.appsist.commons.util.EventUtil;
import de.appsist.service.iid.server.connector.IIDConnector;
import de.appsist.service.iid.server.model.Action;
import de.appsist.service.iid.server.model.ContentBody;
import de.appsist.service.iid.server.model.Level;
import de.appsist.service.iid.server.model.SendMessageAction;
import de.appsist.service.iid.server.model.ServiceItem;
import de.appsist.service.iid.server.model.SiteInformationItem;
import de.appsist.service.iid.server.model.SiteInformationItem.State;
import de.appsist.service.iid.server.model.SiteInformationItemBuilder;
import de.appsist.service.iid.server.model.SiteOverview;
import de.appsist.service.iid.server.model.SiteOverview.Station;
import de.appsist.service.iid.server.model.SiteOverviewBuilder;
import de.appsist.service.iid.server.model.StationInfo;
import de.appsist.service.iid.server.model.StationInfo.Panel;
import de.appsist.service.iid.server.model.StationInfoBuilder;
import de.appsist.service.mid.Driver.AppsistDataHandler;
import de.appsist.service.mid.Driver.AppsistHeartBeatHandler;
import de.appsist.service.mid.Driver.AppsistSchemaHandler;
import de.appsist.service.mid.Driver.AppsistServer;
import de.appsist.service.mid.cache.MIDCache;
import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.mid.cache.MachineCache;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.mid.mvd.WebsiteCreator;
import de.appsist.service.mid.rules.MIDConfig;
import de.appsist.service.mid.rules.MachineConfiguration;
import de.appsist.service.mid.rules.Rule;
import de.appsist.service.mid.rules.datatypes.ASValue;
import de.appsist.service.mid.serialize.MIDSerializer;
import de.appsist.service.mid.sparql.SparqlStorage;
import de.appsist.service.middrv.entity.MachineSchema;
import de.appsist.service.middrv.entity.MachineValueSpecification;
import de.appsist.service.middrv.entity.VisualizationLevel;
import de.appsist.service.middrv.rest.Constants;

public class MachineInformationManager
        extends Verticle{

    // threshold variables
    final double filllevelLow = 4;
    final double filllevelEmpty = 1;

    // verticle configuration variables
    private JsonObject config; 

    // this basePath is used in BasePathRouteMatcher
    private String basePath ="";

    //care about heartbeat singal...?
    private boolean useHeartbeat=false;
    // logger for this class
    private Logger log = LoggerFactory.getLogger(MachineInformationManager.class);

    // storage for machine states
    private Map<String, Set<String>> machinestates = null;
    
    //the global MID cache for all machines
    private MIDCache dataCache=new MIDCache();
  
    //the global schema cache
    private MIDSchemaCache schemaCache=new MIDSchemaCache();
    
    //global config, containing the rules
    private MIDConfig conf;
    
    //ugly static ref :-/
    
    public static MachineInformationManager dieses;
    
    private IIDConnector conn;
    
    private ArrayList<UserSession> sessions=new ArrayList<UserSession>();
    private AsyncResultHandler<Void> printHandler;
    
    //server and handlers for machine driver 
    AppsistServer driverServer;
    AppsistDataHandler dataHandler;
    AppsistSchemaHandler schemaHandler;
	private AppsistHeartBeatHandler heartBeatHandler;
	
	
	private SparqlStorage storage;
    

	public MIDCache getDataCache() {
		return dataCache;
	}

	public void setDataCache(MIDCache dataCache) {
		this.dataCache = dataCache;
	}


	public MIDSchemaCache getSchemaCache() {
		return schemaCache;
	}

	public void setSchemaCache(MIDSchemaCache schemaCache) {
		this.schemaCache = schemaCache;
	}


	//MDComm comm=new MDComm();
	
	ArrayList<String> onlineSessions=new ArrayList<String>();

	WebsiteCreator websiteCreator;
	private boolean useOntology;


    @Override
	public void start() {
    	
    	ServiceStatus status=ServiceStatus.OK;

    	
    	
		
    	printHandler=new AsyncResultHandler<Void>() {

			@Override
			public void handle(AsyncResult<Void> arg0) {
				if (arg0.failed()){
					log.error("Error when sending event. Cause:" + arg0.cause());
						arg0.cause().printStackTrace();
				}
				
			}
    		
		};
    	conn=new IIDConnector(this.vertx.eventBus(), IIDConnector.DEFAULT_ADDRESS);
    	dieses=this;
        if (container.config() != null && container.config().size() >0) {
            config = container.config();
            container.logger().debug("MID started with config \n" + config.encodePrettily());
        } else {
             container.logger().warn("Warning: No configuration applied for MID! Using default settings.");
             status=ServiceStatus.WARN;
            config = getDefaultMIDConfiguration();
        }
        
        useHeartbeat=config.getBoolean("useHeartbeat", false);
        log.debug("MID uses heartbeat:" + useHeartbeat);
        
    	if (!Globals.loadGlobals(config.getString("configPath"))){
    		//error when loading globals
    		status=ServiceStatus.ERROR;
    	}
    	loadConfig();
        websiteCreator=new WebsiteCreator(dataCache, schemaCache, vertx, config.getObject("webserver"), this,config.getString("configPath"));
        //config=websiteCreator.getConfig(); //dont overwrite with default config
        
        
        //this.basePath = config.getObject("webserver").getString("basePath");
        // setup variables
        machinestates = new HashMap<String, Set<String>>();
        // setupHandlers();
        //setupHttpRequestHandlers();
        setupEventHandlers();
        
        log.debug("*******************");
        log.debug("  Vertx-Kommunikation des Maschineninformationsdienst auf Port "
                + config.getObject("webserver").getNumber("port") + " gestartet ");
       
        Number nport = config.getNumber("RESTport");
        
        int port = Constants.HTTP_DEFAULT_PORT;
        
        if (nport!=null){
        	port=nport.intValue();
        }
        
        
        //comm.connect();
        //log.info("Socket-Comm gestartet");
        dataHandler=new AppsistDataHandler(dataCache, schemaCache);
        schemaHandler=new AppsistSchemaHandler(schemaCache);
		heartBeatHandler = useHeartbeat?new AppsistHeartBeatHandler(dataCache, schemaCache):null;
        driverServer=new AppsistServer();
        sendStartOKMessage(status);
        this.driverServer.start(container.logger(), this.vertx, dataHandler, schemaHandler, heartBeatHandler, port , "/services/mid");
        

        //get machine states and 

        this.useOntology=config.getBoolean("useOntology", true);
        
        if (true){
        	log.info("Loading ontology data: calling function and waiting for callback");
        	this.storage=new SparqlStorage(vertx.eventBus());
        	long timerID = vertx.setTimer(1000, new Handler<Long>() {
        	    public void handle(Long timerID) {
        	    	storage.startup();
        	    }
        	});
        	
        	
        }
    }

    public SparqlStorage getStorage() {
		return storage;
	}

	public void setStorage(SparqlStorage storage) {
		this.storage = storage;
	}

	private void sendStartOKMessage(ServiceStatus status){
    	JsonObject statusSignalObject = config.getObject("statusSignal");
    	StatusSignalConfiguration statusSignalConfig;
    	if (statusSignalObject != null) {
    	  statusSignalConfig = new StatusSignalConfiguration(statusSignalObject);
    	} else {
    	  statusSignalConfig = new StatusSignalConfiguration();
    	}

    	StatusSignalSender statusSignalSender =
    	  new StatusSignalSender("mid", vertx, statusSignalConfig);
    	statusSignalSender.setStatus(status);
    	statusSignalSender.start();
    }
    private static JsonObject getDefaultMIDConfiguration() {
        JsonObject defaultConfig = new JsonObject();
        JsonObject webserverConfig = new JsonObject();

			
        webserverConfig.putNumber("port", 8085);
        webserverConfig.putString("statics", "static");
        webserverConfig.putString("basePath", "/services/mid");
        defaultConfig.putObject("webserver", webserverConfig);
        defaultConfig.putObject("services", new JsonObject());
        defaultConfig.putString("configPath", "");
		defaultConfig.putNumber("RESTport", 8095);
		defaultConfig.putBoolean("useHeartbeat",true);
        return defaultConfig;
    }
	

   
	
	
    
	private void loadConfig() {
		MIDSerializer serializer=new MIDSerializer();
		try{
			conf=MIDSerializer.deserializeConfig(config.getString("configPath") + "rules.mid");
			MachineIdentifier id= new MachineIdentifier("Maschine 1","Station 1");
			MachineConfiguration mc=this.conf.get(id);
			//System.out.println(mc);
			log.info("Succesfully loaded rules.mid containing " + conf.getNumRules() + " rules");
		}
		catch (FileNotFoundException nf){
			log.warn("Warning: No MID config file (rules.mid) found at " + System.getProperty("user.dir") + " with configpath " + config.getString("configPath") + ". Creating empty one.");
			
		}
		if (conf==null){
			conf=new MIDConfig();
			log.warn("Warning: MID config file (rules.mid) is empty (null). Creating new one");
		}
	}

    private void setupEventHandlers() {
    	 Handler<Message<JsonObject>> userOnlineHandler = new Handler<Message<JsonObject>>()
 		        {
 		            @Override
					public void handle(Message<JsonObject> jsonMessage)
 		            {
 		                UserOnlineEvent msce =  (UserOnlineEvent) EventUtil
 		                        .parseEvent(jsonMessage.body().toMap());
 		                processUserOnlineEvent(msce);
 		            }
 		        };
 		        vertx.eventBus().registerHandler("appsist:event:userOnline",
 		                userOnlineHandler);
 		        
	       Handler<Message<JsonObject>> userOfflineHandler = new Handler<Message<JsonObject>>()
	 		        {
	 		            @Override
						public void handle(Message<JsonObject> jsonMessage)
	 		            {
	 		                UserOfflineEvent msce =  (UserOfflineEvent) EventUtil
	 		                        .parseEvent(jsonMessage.body().toMap());
	 		                processUserOfflineEvent(msce);
	 		            }
	 		        };
	 		        vertx.eventBus().registerHandler("appsist:event:userOffline",
	 		                userOfflineHandler);
	 		        
//	 		       Handler<Message<JsonObject>> setDataHandler = new Handler<Message<JsonObject>>()
//	 		        {
//	 		            @Override
//						public void handle(Message<JsonObject> jsonMessage)
//	 		            {
//	 		               /* SetMachineDataEvent msce =  EventUtil
//	 		                        .parseEvent(jsonMessage.body().toMap(), SetMachineDataEvent.class);*/
//	 		                
//	 		                String siteId=jsonMessage.body().getString("siteId");
//	 		                String machineId=jsonMessage.body().getString("machineId");
//	 		                String valType=jsonMessage.body().getString("type");
//	 		                String name=jsonMessage.body().getString("name");
//	 		                String value=jsonMessage.body().getString("value");
//	 		                ASValue val=ASValue.createFromString(valType, value);
//	 		                MachineIdentifier id=new MachineIdentifier(machineId, siteId);
//	 		                processSetMachineDataEvent(val,name, id);
//	 		            }
//
//
//	 		        };
//	 		        vertx.eventBus().registerHandler("appsist:event:setMachineData",
//	 		                userOfflineHandler);
	 		        
 		       Handler<Message<JsonObject>> actionHandler = new Handler<Message<JsonObject>>()
 		 		        {
 		 		            @Override
							public void handle(Message<JsonObject> jsonMessage)
 		 		            {
 		 		               String sessionID=jsonMessage.body().getString("sessionId");
 		 		               String action=jsonMessage.body().getString("action");
 		 		               String siteString=jsonMessage.body().getString("site");
 		 		             String stationString=jsonMessage.body().getString("station");
 		 		               
 		 		               if (action.equals("showStationInformation")){
 		 		            	   sendStationInfos(siteString, stationString, sessionID);
 		 		               }
 		 		               if (action.equals("showSiteOverview")){
 		 		            	   sendSiteOverview(siteString, sessionID);
 		 		               }
 		 		               if (action.equals("showSiteDetails")){
 		 		            	 	sendStationInfos(siteString, stationString, sessionID);
		 		               }
 		 		            }
 		 		        };
 		 		        vertx.eventBus().registerHandler("appsist:service:mvd",
 actionHandler);
 		        
		
	}

    /**
     * @deprecated -> use msd for synthetic value input
     * @param val
     * @param name
     * @param id
     */
    protected void processSetMachineDataEvent(ASValue val, String name, MachineIdentifier id) {
		//schemaCache.put(key, value)
		
	}

	protected void processUserOfflineEvent(UserOfflineEvent msce) {
		String userId=msce.getUserId();
		String sessionID=msce.getSessionId();
    	
		sessions.remove(new UserSession(userId, sessionID));
		
	}

    
    /**
     * detailansicht einer anlage
     */
    
	private void sendStationInfos(String site, String station , String sessionId) {
		
		StationInfoBuilder builder=new StationInfoBuilder();
		builder.setSite(site);
		builder.setStation(station);
				
		MachineIdentifier machineId = new MachineIdentifier(station, site);
		MachineCache mcache=this.dataCache.get(machineId);
		MachineSchema schema=schemaCache.get(machineId);
		
		ArrayList<Panel> panels=websiteCreator.createStationPanelsOnlyFrame(mcache,schema);
		
		for (Panel panel:panels){
			builder.addPanel(panel);
		}
		

		conn.displayStationInfo(sessionId, "MVD",builder.build(), printHandler);
		
	}
    

	/**
     * übersichtsseite aller Maschinen der Anlage mit KPIs
     */

	private void sendSiteOverview(String site, String sessionId) {
		SiteOverviewBuilder builder=new SiteOverviewBuilder();
	
		
		
		builder.setSite(site);
		
		if (Globals.cebitHack){
			builder.setSite("Demonstrator");
		}
		
		for (String stat:schemaCache.getMachines(site)){
			MachineIdentifier machineId = new MachineIdentifier(stat, site);
			MachineCache mcache=this.dataCache.get(machineId);
			MachineSchema schema=schemaCache.get(machineId);
			
			
			Level level=Globals.getMostSevereLevel(mcache.getStates());
			String machineName = mcache.getMachineID().toString();
			String siteString=mcache.getMachineID().getStationID();
			String stationString=mcache.getMachineID().getMachineID();
			
			//UUID resolving --------
			
			String siteUUID=schema.getSiteUUID();
			String machineUUID=schema.getMachine().getMachineUUID();
			
			//if UUID is provided, try to get labels from ontology
			
			if (machineUUID!=null && !machineUUID.equals("")){
				String mstring=MachineInformationManager.dieses.getStorage().resolveUUID(machineUUID);
				if (mstring!=null && !mstring.equals("")) {
					log.debug("sendSiteOverview: succesfully resolved " + stationString + ", machineUUID " + machineUUID + " --> " + mstring);
					stationString=mstring;
					builder.setSite(stationString);
				}
				else{
					log.debug("sendSiteOverview: Could not resolve UUID " + machineUUID + " : no mapping. map size: " + MachineInformationManager.dieses.getStorage().getSize());
				}
			}
			else{
				log.debug("sendSiteOverview: No UUID provided for machine " + stationString);
			}
			
			if (siteUUID!=null && !siteUUID.equals("")){
				String sstring=MachineInformationManager.dieses.getStorage().resolveUUID(siteUUID);
				if (sstring!=null && !sstring.equals("")) {
					siteString=sstring;
					machineName=new MachineIdentifier(siteString, stationString).toString();
				}
			}
			

			
			//--------------------
			
			

			
			Action action=new SendMessageAction("appsist:service:mvd", new JsonObject().putString("action", "showSiteDetails").putString("site", mcache.getMachineID().getStationID()).putString("station", mcache.getMachineID().getMachineID()));
			
			if (Globals.cebitHack){
				machineName="MachineIdentifier [stationID=1, machineID=Station Montage]";
			}
			Station station=new Station(machineName, level, new ContentBody.Frame(config.getObject("webserver").getString("basePath") + "/sites/"+ machineId.getStationID() +"/stations/" + machineId.getMachineID()), action,null);
			
					//websiteCreator.createSitePanel(mcache,schema);
			builder.addStation(station);
			

		}
		
		SiteOverview build = builder.build();
		//System.out.println("Panel as json:\n" + build.asJson());
		conn.displaySiteOverview(sessionId, "MVD", build, printHandler);
	}
		
		
	/**
     * use sendStationInfos instead
     */

	@Deprecated 
	private void sendSiteDetails(String site, String station, String sessionId) {
		StationInfoBuilder builder=new StationInfoBuilder();
		builder.setSite(site);
		builder.setStation(station);
		

		MachineIdentifier machineId = new MachineIdentifier(station, site);
		MachineCache mcache=this.dataCache.get(machineId);
		MachineSchema schema=schemaCache.get(machineId);
		Level level=Globals.getMostSevereLevel(mcache.getStates());
		String machineName = mcache.getMachineID().toString();
	
		//Station station=new Station(machineName, level, new ContentBody.Frame(config.getObject("webserver").getString("basePath") + "/sites/"+ machineId.getStationID() +"/stations/" + machineId.getMachineID()), null);
		
				//websiteCreator.createSitePanel(mcache,schema);
		//builder.addStation(station);

		int i=0;
		
		//TODO: WARNING!!! Wir nehmen an, dass die Enumeration der Variablen hier bei uns gleich mit der beim erstellen der website im Websitecreator ist. Diese sind jedoch unabhängig!
		for (Entry<String,ASValue> entry:mcache.entrySet())	{
			MachineValueSpecification spec=schema.getSpecification(entry.getKey());
			if (spec.getVisualizationLevel()!=VisualizationLevel.NEVER){
				///panels/:panelId
				Panel pan=new Panel(machineName, Level.INFO, new ContentBody.Frame(config.getObject("webserver").getString("basePath") + "/sites/"+ machineId.getStationID() +"/stations/" + machineId.getMachineID()+ "/panels/" + i++), null);
				builder.addPanel(pan);
			}
		}

		
		
		StationInfo build = builder.build();
		//System.out.println("Panel as json:\n" + build.asJson());
		conn.displayStationInfo(sessionId, "MVD", build, printHandler);
	}

	

	public void updateSiteInformation(){
		
		for (UserSession session:sessions){
			conn.purgeServiceItems(session.getSessionId(), "MVD", printHandler);
			sendServiceItems(session.getSessionId());
		}
    	//sessionid, serviceid->purgeitems
    	
    	//--> für alle online sessionids
    }
	protected void processUserOnlineEvent(UserOnlineEvent msce) {
		String userId=msce.getUserId();
		String sessionID=msce.getSessionId();
		
		sessions.add(new UserSession(userId, sessionID));
		
		sendServiceItems(sessionID);
		
		
		
		
	}
	
	public void sendServiceItems(String sessionId){	
		
		List<ServiceItem> liste=new ArrayList<ServiceItem>();
		log.info("MID: Sending service started " );
		
		if (!Globals.cebitHack){
			log.debug("sendServiceItems: Sending " + dataCache.getMachineIDs() + " items");
			for (MachineIdentifier mID:dataCache.getMachineIDs()){
				MachineCache cache=dataCache.get(mID);
				MachineSchema schema=schemaCache.get(mID);
				
				Level errorLevel=Globals.getMostSevereLevel(cache.getStates());
				
				String siteString=mID.getStationID();
				SiteInformationItemBuilder builder=new SiteInformationItemBuilder();
				//SiteOverviewBuilder builder=new SiteOverviewBuilder();
				
				String stationString=cache.getMachineID().getMachineID();
				
				String siteUUID=schema.getSiteUUID();
				String machineUUID=schema.getMachine().getMachineUUID();
				
				//if UUID is provided, try to get labels from ontology
				
				if (machineUUID!=null && !machineUUID.equals("")){
					String mstring=MachineInformationManager.dieses.getStorage().resolveUUID(machineUUID);
					if (mstring!=null && !mstring.equals("")) {
						log.debug("ServiceItems: succesfully resolved " + stationString + ", machineUUID " + machineUUID + " --> " + mstring);
						siteString=mstring;
						
					}
					else{
						log.debug("ServiceItems: Could not resolve UUID " + machineUUID + " : no mapping. map size: " + MachineInformationManager.dieses.getStorage().getSize());
					}
				}
				else{
					log.debug("ServiceItems: No UUID provided for machine " + stationString);
				}
				
				if (siteUUID!=null && !siteUUID.equals("")){
					String sstring=MachineInformationManager.dieses.getStorage().resolveUUID(siteUUID);
					if (sstring!=null && !sstring.equals("")) stationString=sstring;
				}
				
				
				
				builder.setId(mID.getId()+"");
				builder.setPriority(errorLevel.ordinal()+1);
				builder.setService("MVD"); //sender
				builder.setSite(siteString); //anlage 1
				builder.setState(levelToState(errorLevel));
				builder.setStation(stationString);
				builder.setMessage("");
				Action action=new SendMessageAction("appsist:service:mvd", new JsonObject().putString("action", "showSiteOverview").putString("site", mID.getStationID()));
				
				/*System.out.println("Adding overview page:\n" + "id = " + mID.getId() + "\nprio=" + errorLevel.ordinal() + "\nsite=" + siteString + 
						"\nstate=" + levelToState(errorLevel) + "\nstation=" + mID.getMachineID() + "\nactionsite=" + siteString);*/
				builder.setAction(action);
				SiteInformationItem infoItem=builder.build();
				liste.add(infoItem);
			}
		}
		else{
			for (MachineIdentifier mID:dataCache.getMachineIDs()){
				MachineCache cache=dataCache.get(mID);
				Level errorLevel=Globals.getMostSevereLevel(cache.getStates());

				String siteString=mID.getStationID();
				log.debug("MID: Sending service item for " + siteString);
				SiteInformationItemBuilder builder=new SiteInformationItemBuilder();
				
				builder.setId(mID.getId()+"");
				builder.setPriority(errorLevel.ordinal()+1);
				builder.setService("MVD"); //sender
				builder.setSite("Demonstrator"); //anlage 1
				builder.setState(levelToState(errorLevel));
				builder.setStation("Station Montage");
				builder.setMessage("");
				Action action=new SendMessageAction("appsist:service:mvd", new JsonObject().putString("action", "showSiteOverview").putString("site", siteString));
				
				builder.setAction(action);
				SiteInformationItem infoItem=builder.build();
				liste.add(infoItem);
			}
		}
		

		conn.addServiceItems(sessionId, liste, new AsyncResultHandler<Void>() {
			
			@Override
			public void handle(AsyncResult<Void> arg0) {
				if (arg0.failed()){
					log.error("addserviceItems failed. Cause:" + arg0.cause());
					arg0.cause().printStackTrace();
				}
				
			}
		});
	}

	private State levelToState(Level errorLevel) {
		switch(errorLevel){
		case ERROR:
			return State.ERROR;
		case WARNING:
			return State.WARNING;
		case INFO:
			return State.RUNNING;
		default:
			return State.RUNNING;
		}
	}

	public MachineInformationManager()
    {

    }


    // retrieve saved machine states for a station
    private Set<String> getMachineState(String stationId)
    {
        if (this.machinestates.containsKey(stationId)) {
            return this.machinestates.get(stationId);
        }
        return new HashSet<String>();
    }

    // add a new machinestate for station
    private void setMachineState(String stationId, String machineState)
    {
        Set<String> statesSet = null;
        if (this.machinestates.containsKey(stationId)) {
            statesSet = this.machinestates.get(stationId);
        }
        else {
            statesSet = new HashSet<String>();
        }
        statesSet.add(machineState);
        this.machinestates.put(stationId, statesSet);
    }

    // publish new machinestate to eventbus (someone might be interested)
    private void publishMachineState(MachineStateChangedEvent msce)
    {
        vertx.eventBus().publish("appsist:event:machinestateChangedEvent",
                new JsonObject(msce.asMap()));
    }
    
 
    /**called by this to notify Massnahmendienst about event
     * 
     * @param machineID
     * @param securityLevel
     * @param machineState
     */
	public void notifyActionService(MachineIdentifier machineID, String securityLevel, ArrayList<String> machineStates) {
		
		//TODO: check rules and find MSCE to throw, if any
		 MachineStateChangedEvent msce = new MachineStateChangedEvent(
                 "msce0001", "sessionId", machineID.getMachineID(), machineID.getStationID(), securityLevel,
                 machineStates, "");
		 publishMachineState(msce);
		
	}

	/**
	 * Called by requesthandler when new machine data arrives
	 * 
	 * @param machineCache
	 */
	
	public void notifyUpdate(MachineCache machineCache) {
		
		MachineConfiguration mc=conf.get(machineCache.getMachineID());
		
		boolean notifyMD=false;
		if (mc!=null){
			for (Rule rule:mc.getRules()){
				if (rule.applies(machineCache)){
					//apply consequences
					
					if (machineCache.addState(rule.getAction())){
						//state was not set before -> notify MD
						log.debug("State added ->" + rule.getAction());
						notifyMD=true;
					}
					
				}
				else{
					if (machineCache.removeState(rule.getAction())){
						log.debug("State removed ->" + rule.getAction());
						//state was set before -> notify MD
						notifyMD=true;
					}
				}
			}
		
		}
		
		if (notifyMD){
			log.debug("Pushing " + machineCache.getStates().size() + " states to MD");
			notifyActionService(machineCache.getMachineID(), "info", machineCache.getStates());
		}
		
		//TODO: nur benutzen, wenn MD benachrichtigt wird, oder neuer cache erzeugt wurde
		
		if (dataCache.hasNewCache()){
			updateSiteInformation();
		}
	}

	public void debug() {
		
		String site="Anlage1";
		String station="Maschine20";
		StationInfoBuilder builder=new StationInfoBuilder();
		builder.setSite(site);
		builder.setStation(station);
				
		MachineIdentifier machineId = new MachineIdentifier(station, site);
		MachineCache mcache=this.dataCache.get(machineId);
		MachineSchema schema=schemaCache.get(machineId);
		
		ArrayList<Panel> panels=websiteCreator.createStationPanelsOnlyFrame(mcache,schema);
		
		for (Panel panel:panels){
			builder.addPanel(panel);
		}
		
		StationInfo info=builder.build();
		System.out.println("Here is the debug info: @@@");
		System.out.println(info.asJson());

		
	}

	public String getStatesForSimon(String site, String machine) {
		MachineIdentifier machineId = new MachineIdentifier(machine, site);
		MachineCache mcache=this.dataCache.get(machineId);

		
		
		JSONObject obj=new JSONObject();
		
		if (mcache==null) return obj.toString(); //machine not available -> send empty map

		MachineConfiguration mc=conf.get(mcache.getMachineID());
		
		HashMap<String, Boolean> statesList=new HashMap<String, Boolean>();


		if (mc!=null){
			for (Rule rule:mc.getRules()){
				statesList.put(rule.getAction(), rule.applies(mcache));
				obj.put(rule.getAction(), rule.applies(mcache));
			}
		}
		
		return obj.toString();
	}

	/*
	public boolean checkIfStateChanged(MachineCache machineCache,
			String consequenceState, String consequenceTag) {
		//if (!(machineCache.getState().getCode()==getStateNumber(consequenceState))){
		
		if(machineCache.addState(consequenceState)){
			//state wasnt set before -> notify massnahmendienst
			
			System.out.println("State added ->" + consequenceState + " - Tag:" + consequenceTag);
			
			
			//site information updaten
			updateSiteInformation();
			machineCache.setState(consequenceState);
			machineCache.setStateTag(consequenceTag);
			
			//only one rule is fired atm
			return true;
		}
		return false;
	}*/

	


}

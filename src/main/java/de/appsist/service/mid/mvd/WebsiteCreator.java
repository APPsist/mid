package de.appsist.service.mid.mvd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import de.appsist.commons.lang.LangUtil;
import de.appsist.commons.lang.StringBundle;
import de.appsist.service.iid.server.model.Action;
import de.appsist.service.iid.server.model.ContentBody;
import de.appsist.service.iid.server.model.ContentBody.HTML;
import de.appsist.service.iid.server.model.Level;
import de.appsist.service.iid.server.model.SendMessageAction;
import de.appsist.service.iid.server.model.SiteOverview.Station;
import de.appsist.service.iid.server.model.StationInfo.Panel;
import de.appsist.service.mid.BasePathRouteMatcher;
import de.appsist.service.mid.Globals;
import de.appsist.service.mid.MachineInformationManager;
import de.appsist.service.mid.cache.MIDCache;
import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.mid.cache.MachineCache;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.mid.rules.datatypes.ASBoolean;
import de.appsist.service.mid.rules.datatypes.ASDouble;
import de.appsist.service.mid.rules.datatypes.ASValue;
import de.appsist.service.middrv.entity.Machine;
import de.appsist.service.middrv.entity.MachineSchema;
import de.appsist.service.middrv.entity.MachineValueSpecification;
import de.appsist.service.middrv.entity.VisualizationLevel;



public class WebsiteCreator {
	private HashMap<String, Template> templates;
	
	MIDCache cache;
	MIDSchemaCache schemaCache;
	Vertx vertx;
	JsonObject config;
	private BasePathRouteMatcher routeMatcher;
	private MachineInformationManager mid;

	private static final Logger logger = LoggerFactory.getLogger(WebsiteCreator.class);

	private String cssPath="src/main/resources/css/";
	
	StringBundle bundle;

	public WebsiteCreator(MIDCache cache, MIDSchemaCache schema, Vertx vertx,
			JsonObject config, MachineInformationManager machineInformationManager, String cssPath) {
		super();
		this.cache = cache;
		this.schemaCache = schema;
		this.vertx = vertx;
		this.config = config;
		mid=machineInformationManager;
		setupWebserver();
		
		bundle=LangUtil.getInstance().getBundle();
		
		if (cssPath!=null && !cssPath.equals("")){
			this.cssPath=cssPath + "css/";
		}

	}

	private ArrayList<Panel> createStationPanels(MachineCache mcache,
			MachineSchema schema) {
    	
    	ArrayList<Panel> ret=new ArrayList<Panel>();
    	
    	if (mcache==null) return ret;
    	for (Entry<String, ASValue> entry:mcache.entrySet()){

			MachineValueSpecification spec=schema.getSpecification(entry.getKey());
			String unit=spec.getUnit().toString();
			Level level=Globals.getMostSevereLevel(mcache.getStates());
			
			if (spec.getVisualizationLevel()!=VisualizationLevel.NEVER){
				switch(spec.getVisualizationType()){
				case PRECENT_BAR:
					ret.add(createBarPanel(entry, level));
					break;
				case ON_OFF_LIGHT:
					ret.add(createOnoffPanel(entry,level));
					break;
				case TEXT_FIELD:
					ret.add(createTextPanel(entry, unit,level));
					break;
				}
				
			}
			


		}
		return ret;
    }
	
	
	/**
	 * same as before, but does not create html code, but a reference to the debug site
	 * @param mcache
	 * @param schema
	 * @return
	 */
	public ArrayList<Panel> createStationPanelsOnlyFrame(MachineCache mcache,
			MachineSchema schema) {
    	
    	ArrayList<Panel> ret=new ArrayList<Panel>();
    	
    	//return empty panel list when machine not available
    	if (mcache==null || schema==null) return ret;
    	
    	MachineIdentifier mid=mcache.getMachineID();
    	
    	int i=0; //panel counter
    	
    	for (Entry<String, ASValue> entry:mcache.entrySet()){

			MachineValueSpecification spec=schema.getSpecification(entry.getKey());
			String unit=spec.getUnit().toString();
			Level level=Globals.getMostSevereLevel(mcache.getStates());
			
			if (spec.getVisualizationLevel()!=VisualizationLevel.NEVER){
				//sites/:siteId/stations/:stationId/panels/:panelId
				ContentBody body=new ContentBody.Frame(config.getString("basePath") + "/sites/" + mid.getStationID() + "/stations/" + mid.getMachineID() + "/panels/"+ i);
				Panel pan=new Panel(entry.getKey(), level, body, null);
				i++;
				ret.add(pan);
				
				//System.out.println("Panel as json:\n" + pan.asJson());
			}
			


		}
		return ret;
    }
	
	
	public JsonObject getConfig() {
		return config;
	}

	public void setConfig(JsonObject config) {
		this.config = config;
	}

	private JsonObject createTextObject(String name, ASValue value, String unit) {
		JsonObject object=new JsonObject();
		
		object.putString("name", name).putBoolean("useBalken", false).putBoolean("useonoff", false).putString("value",value.toString()).putString("unit", unit);
		
		return object;
	}
	private void setupWebserver() {
    	//config=getDefaultWebserverConfiguration();
    	// Preload templates to improve performance.
    			TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
    			Handlebars handlebars = new Handlebars(loader);
    			templates = new HashMap<>();
    			try {
    				templates.put("station", handlebars.compile("station"));
    				templates.put("panel", handlebars.compile("panel"));
    			} catch (IOException e) {
    				//container.logger().warn("Failed to load template.", e);
    			}
    			
    			initializeHTTPRouting();
    			vertx.createHttpServer()
    				.requestHandler(routeMatcher)
    				.listen(config
    				.getInteger("port"));
	}

	private Panel createTextPanel(Entry<String, ASValue> entry, String unit, Level level) {
		JsonObject object=createTextObject(entry.getKey(), entry.getValue(), unit);
		try {
			object.putString("csspath", config.getString("basePath") + "/ressources/fira-sans.css");
			String html= templates.get("panel").apply(object.toMap());
			
			ContentBody body=new ContentBody.HTML(html);
			Panel ret=new Panel(entry.getKey(), level, body, null);
			return ret;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Panel createOnoffPanel(Entry<String, ASValue> entry, Level level) {
		JsonObject object=createOnoffObject(entry.getKey(), entry.getValue());
		try {
			String html= templates.get("panel").apply(object.toMap());
			
			ContentBody body=new ContentBody.HTML(html);
			Panel ret=new Panel(entry.getKey(), level, body, null);
			return ret;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private JsonObject createOnoffObject(String name, ASValue value) {
		JsonObject object=new JsonObject();
		
		String onoffstr;
		String onofflabel="";
		if (!(value instanceof ASBoolean)){
			logger.error("MVD ERROR: Variable "+ name +": Visualisierung OnOff nur für Werte vom Typ Boolean erlaubt, ist aber " + value.getTypeString());
			return null;
		}
		
		boolean val=((ASBoolean)value).getBoolean();
		if (val){
			onoffstr="on";
			onofflabel=bundle.getString("mid.ui.values.ja","ja");;
		}
		else{
			onoffstr="off";
			onofflabel=bundle.getString("mid.ui.values.nein","nein");
		}
		
		object.putString("name",name).putBoolean("useBalken", false).putBoolean("useonoff", true).putString("onoff", onoffstr).putString("onofflabel", onofflabel);
		return object;
	}
	private Panel createBarPanel(Entry<String, ASValue> entry, Level level) {
		
		JsonObject object=createBarObject(entry.getKey(),entry.getValue());
		try {
			String html= templates.get("panel").apply(object.toMap());
			
			ContentBody body=new ContentBody.HTML(html);
			Panel ret=new Panel(entry.getKey(), level, body, null);
			return ret;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private JsonObject createBarObject(String name, ASValue value){
		JsonObject object=new JsonObject();
		
		if (!(value instanceof ASDouble)){
			logger.error("MVD ERROR: Variable "+ name +": Visualisierung Balken nur für Werte vom Typ Double erlaubt ([0...1]), ist aber " + value.getTypeString());
			return null;
		}
		
		double val=((ASDouble)value).getValue();
		
		String percentval=((int)(val *100))+"";
		String antipercentval=((int)((1-val) *100))+"";
		String barcolor =  val <=0.1 ? "#E02222":"#81A649";
		String textcolor =  val <=0.1 ? "#F60D0D":"#000000";
		
		object.putString("name", name).putString("barcolor",barcolor).putString("textcolor",textcolor).putBoolean("useBalken", true).putBoolean("useonoff", false).putString("percent", percentval).putString("antipercent", antipercentval);
		
		return object;
	}
	
	
	 
    private void initializeHTTPRouting() {
		routeMatcher = new BasePathRouteMatcher(config.getString("basePath"));
		
		logger.info("MID: URLS:\nOverview:" + config.getString("basePath")  + "/sites/:siteId/stations/:stationId\n"
				+"Details: localhost:<port>//" + config.getString("basePath")  + "/sites/:siteId/stations/:stationId/panels/:panelId\n"
				+"Set state: localhost:<port>//" + config.getString("basePath")  + "/setState/:siteId/:stationId/:state\n"
				+"Res: localhost:<port>//" + config.getString("basePath")  + "/ressources/:ressource\n"
				+"get State: localhost:<port>//" + config.getString("basePath")  + "/states/:siteId/:stationIdn"
				);
		
		
		
		//debug-schnittstellen
		
		//overview
		routeMatcher.get("/sites/:siteId/stations/:stationId", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				String siteId = request.params().get("siteId");
				String stationId = request.params().get("stationId");
				String html;
				try {
					html = getHtmlForStation(siteId, stationId);
					request.response().end(html);
				} catch (IOException e) {
					request.response().setStatusCode(404).end("Unknown entry.");
				}
			}
			
			//details
		}).get("/sites/:siteId/stations/:stationId/panels/:panelId", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				String siteId = request.params().get("siteId");
				String stationId = request.params().get("stationId");
				String panelId = request.params().get("panelId");
				String html;
				try {
					html = getHtmlForPanel(siteId, stationId, panelId);
					request.response().end(html);
				} catch (IOException e) {
					request.response().setStatusCode(404).end("Unknown entry.");
				}
			}
			
			//statusabfrage-schnittstelle
		}).get("/states/:siteId/:stationId", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				String siteId = request.params().get("siteId");
				String stationId = request.params().get("stationId");

				String html;
				try{
					html = getStateHtml(siteId, stationId);
					request.response().end(html);
				} catch (IOException e) {
					request.response().setStatusCode(404).end(e.getMessage());
				}
			}
		}).
		
		
		//status setzen-schnittstelle
		get("/setState/:siteId/:stationId/:state", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				String siteId = request.params().get("siteId");
				String stationId = request.params().get("stationId");
				String state = request.params().get("state");
				
				String html;
				try{
					html = setStateHtml(siteId, stationId,state, true);
					request.response().end(html);
				} catch (IOException e) {
					request.response().setStatusCode(404).end(e.getMessage());
				}
			}
		}).
		
		
		//ressourcen
		get("/ressources/:ressource", new Handler<HttpServerRequest>() {
					
				@Override
				public void handle(HttpServerRequest request) {
					String ressource = request.params().get("ressource");
					String filename = cssPath + ressource;
					
					
					File file=new File(filename);
					if (file.exists()){
						logger.debug("MID Webserver: Ressource sent:" + filename);
						request.response().sendFile(filename);
					}
					else{
						logger.error("MID Webserver: Ressource not found at '" + System.getProperty("user.dir") + "':" + filename );
						request.response().end("Ressource file not found:" + filename);
					}
					//request.response().end("Requested ressource:" + ressource);
				}
			}).
		
		
		get("/debug", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				logger.info("MID Debug URL called. This should not appear in productive use!");
				
				MachineInformationManager.dieses.debug();
				
				request.response().end("Debug Method called" );
			}
		}).
		
		
		get("/statesjson/:siteID/:machineID", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				String site = request.params().get("siteID");		
				String machine = request.params().get("machineID");	
				
				request.response().end(	MachineInformationManager.dieses.getStatesForSimon(site, machine));
			}
		}).
		
		get("/:url", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				logger.error("MID Webserver: invalid url called:" + request.params().get("url"));
				request.response().end("Invalid URL called:" + request.params().get("url"));
				
				//request.response().end("Requested ressource:" + ressource);
			}
		})
		
		
		;
	}
	
	
	protected String setStateHtml(String siteId, String stationId, String state, boolean createIfEmpty) throws IOException {
		MachineIdentifier machineId = new MachineIdentifier(stationId,siteId);
		MachineCache mcache=this.cache.get(machineId);
		
		if (mcache==null) {
			if (createIfEmpty){
				mcache=new MachineCache(machineId);
				this.cache.put(machineId, mcache);
			}
			else{
				throw new IOException( "machine not found: site='" + siteId + "' station/machineId='" + stationId+ "'\n\nAvailable machines:\n" + this.cache.getAvailableMachines());
			}	
		}

		boolean success=mcache.addState(state);
		
		if (success){
			return "State succesfully changed";
		}
		else{
			return "state unchanged. Machine already had the state";
		}
	}

	protected String getStateHtml(String siteId, String stationId) throws IOException{
		MachineIdentifier machineId = new MachineIdentifier(stationId,siteId);
		MachineCache mcache=this.cache.get(machineId);
		
		if (mcache==null) throw new IOException( "machine not found: site='" + siteId + "' station/machineId='" + stationId+ "'\n\nAvailable machines:\n" + this.cache.getAvailableMachines());
		String states="";
		for (String state:mcache.getStates()){
			states +=state + "\n";
		}
	
		return states;
		
	}

	/**
	 * only for debug issues, displays station infos on the webpage
	 * @param siteId
	 * @param stationId
	 * @param panelId
	 * @return
	 * @throws IOException
	 */
	private String getHtmlForPanel(String siteId, String stationId, String panelId) throws IOException {
		JsonObject data = new JsonObject();
		
		MachineIdentifier machineId = new MachineIdentifier(stationId,siteId);
		MachineCache mcache=this.cache.get(machineId);
		MachineSchema schema=schemaCache.get(machineId);
		
		ArrayList<Panel> panels=createStationPanels(mcache, schema);
		
		int panelIndex=Integer.parseInt(panelId);
		
		if (panels==null || panels.size()<=panelIndex) return "machine not found: site='" + siteId + "' station/machineId='" + stationId+ "'\n\nAvailable machines:\n" + this.cache.getAvailableMachines();
		Panel panel = panels.get(panelIndex);
		JsonObject asJson = panel.asJson();
		JsonObject array = asJson.getObject("content");
		String htmlbody = array.getString("body");
		ContentBody.HTML html=(HTML) panel.getContentBody();
		return htmlbody;
	}

	
	/*
	 * ########################################  site overview stuff
	 * 
	 */
	public Station createSitePanel(MachineCache mcache, MachineSchema schema) {

    	
    	if (mcache==null) return new Station("Unknown machine", Level.INFO, new ContentBody.HTML("machineID not found"), null,null);
    	
    	JsonArray machinesArray=new JsonArray();
    	
    	for (Entry<String, ASValue> entry:mcache.entrySet()){

			MachineValueSpecification spec=schema.getSpecification(entry.getKey());
			
			if (spec==null){
				logger.error("MID ERROR:" + entry.getKey() + " has no corresponding machineValueSpecification");
				continue;
			}
			String unit=spec.getUnit().toString();
			
			if (spec.getVisualizationLevel()==VisualizationLevel.OVERVIEW){
				switch(spec.getVisualizationType()){
				case PRECENT_BAR:
					machinesArray.add(createBarObject(entry.getKey(),entry.getValue()));
					break;
				case ON_OFF_LIGHT:
					machinesArray.add(createOnoffObject(entry.getKey(),entry.getValue()));
					break;
				case TEXT_FIELD:
					machinesArray.add(createTextObject(entry.getKey(),entry.getValue(),unit));
					break;
				}
				
			}
			


		}
    	
    	JsonObject ret=new JsonObject();
    	ret.putArray("values", machinesArray);
    	ret.putString("csspath", config.getString("basePath") + "/ressources/fira-sans.css");
    	String html;
		try {
			html = templates.get("station").apply(ret.toMap());
			ContentBody body=new ContentBody.HTML(html);
			Level level=Globals.getMostSevereLevel(mcache.getStates());
	
			String siteString=mcache.getMachineID().getStationID();
			String stationString=mcache.getMachineID().getMachineID();
			
			String siteUUID=schema.getSiteUUID();
			String machineUUID=schema.getMachine().getMachineUUID();
			
			//if UUID is provided, try to get labels from ontology
			
			if (machineUUID!=null && !machineUUID.equals("")){
				String mstring=MachineInformationManager.dieses.getStorage().resolveUUID(machineUUID);
				if (mstring!=null && !mstring.equals("")) {
					logger.debug("succesfully resolved " + stationString + ", machineUUID " + machineUUID + " --> " + mstring);
					stationString=mstring;
					
				}
				else{
					logger.debug("Could not resolve UUID " + machineUUID + " : no mapping. map size: " + MachineInformationManager.dieses.getStorage().getSize());
				}
			}
			else{
				logger.debug("No UUID provided for machine " + stationString);
			}
			
			if (siteUUID!=null && !siteUUID.equals("")){
				String sstring=MachineInformationManager.dieses.getStorage().resolveUUID(siteUUID);
				if (sstring!=null && !sstring.equals("")) siteString=sstring;
			}
			

			
			Action action=new SendMessageAction("appsist:service:mvd", new JsonObject().putString("action", "showSiteDetails").putString("site", siteString).putString("station", stationString));
			
			return new Station(stationString, level, body, action,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return new Station(mcache.getMachineID().toString(), Level.INFO, new ContentBody.HTML("exception when generating html code"), null,null);
	}
	private String getHtmlForStation(String siteId, String stationId) throws IOException {
		
		MachineIdentifier machineId = new MachineIdentifier( stationId,siteId);
		MachineCache mcache=this.cache.get(machineId);
		MachineSchema schema=schemaCache.get(machineId);
		
		Station station=createSitePanel(mcache, schema);
		
		if (mcache==null){
			station= new Station("", Level.INFO, new ContentBody.HTML("machineID not found: site='" + siteId + "' station/machineId='" + stationId+ "'\n\nAvailable machines:\n" + this.cache.getAvailableMachines()), null,null);
		}
		return ((ContentBody.HTML)station.getContentBody()).getHtmlBody();
		
	}
	
}

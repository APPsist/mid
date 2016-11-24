package de.appsist.service.mid.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;


public class SparqlVerbinder {
	private static final String PREFIXSTRING = "BASE <http://www.appsist.de/ontology/> PREFIX app: <http://www.appsist.de/ontology/> ";
	 private static final String SPARQLREQUESTS = "appsist:requests:semwiki";
	 
	// logger for this class
	    private final Logger log = LoggerFactory.getLogger(SparqlVerbinder.class);
	    
	    private HashMap<String,String> uuidMap=null;
	    
	    
	    private ArrayList<SparqlCallback> listener=new ArrayList<SparqlCallback>();
	    

	 public void addListener(SparqlCallback callback){
		 listener.add(callback);
	 }
	 public void getMachineStates(EventBus eb){
		 Handler<Message<String>> stationsInWorkplaceGroupsHandler = new Handler<Message<String>>() {

				@Override
				public void handle(Message<String> stringMessage) {
					
					HashMap<String,Integer> availableStates=new HashMap<String,Integer>();
					
					
					JsonObject result = new JsonObject(stringMessage.body());
					
					JsonArray messageArray = result.getObject("results").getArray("bindings");
					Iterator<Object> messageArrayIterator = messageArray.iterator();
					while (messageArrayIterator.hasNext()) {
						Object currentArrayEntry = messageArrayIterator.next();
						if (currentArrayEntry instanceof JsonObject) {
							
							String label=((JsonObject) currentArrayEntry).getObject("label").getString("value");
							int prioritaet=Integer.parseInt(((JsonObject) currentArrayEntry).getObject("prioritaet").getString("value"));
							availableStates.put(label, prioritaet);
							
							
						} else {
							log.info("Expected JsonObject. Found " + currentArrayEntry.getClass());
						}
					}
					setAvailableStates(availableStates);
				}};

		        String sparqlQuery = PREFIXSTRING + "SELECT DISTINCT ?zustand ?prioritaet ?label WHERE { ?zustand rdfs:subClassOf* app:Maschinenzustand . ?zustand app:hatPrioritaet ?prioritaet  . ?zustand rdfs:label ?label FILTER(LANG(?label)=\"de\")}";
		        sendSparQLQuery(sparqlQuery, eb, stationsInWorkplaceGroupsHandler);
	 }
	 
	protected void setAvailableStates(HashMap<String, Integer> availableStates) {
		for (SparqlCallback callback : listener){
			callback.stateMapReceived(availableStates);
		}

	}

	
	public void getUUIDMap(EventBus eb){

		//use cached map if available
        if (uuidMap!=null) setUUIDMap(this.uuidMap);
		
		
		 Handler<Message<String>> stationsInWorkplaceGroupsHandler = new Handler<Message<String>>() {

				@Override
				public void handle(Message<String> stringMessage) {
					JsonObject result = new JsonObject(stringMessage.body());
					
					HashMap<String,String> mappe = new HashMap<String,String>();
					JsonArray messageArray = result.getObject("results").getArray("bindings");
					Iterator<Object> messageArrayIterator = messageArray.iterator();
					while (messageArrayIterator.hasNext()) {
						Object currentArrayEntry = messageArrayIterator.next();
						if (currentArrayEntry instanceof JsonObject) {
							
							JsonObject anlage = ((JsonObject) currentArrayEntry).getObject("anlage");
							JsonObject anlagenlabel = ((JsonObject) currentArrayEntry).getObject("anlagenLabel");
							
							String auuid=null;

							String alabel=null;
							
							if (anlage==null || anlagenlabel==null){
								log.info("could not extract anlage / anlagenlabel for " + ((JsonObject) currentArrayEntry));
							}
							else{
								auuid=anlage.getString("value");
								alabel=anlagenlabel.getString("value");
							}
							
							
							JsonObject station = ((JsonObject) currentArrayEntry).getObject("station");

							JsonObject stationlabel = ((JsonObject) currentArrayEntry).getObject("stationLabel");
							
							
							String suuid=null;
							String slabel=null;
							
							
							if (station==null || stationlabel==null){
								log.info("could not extract station / stationlabel for " + ((JsonObject) currentArrayEntry));
							}
							else{
								suuid=station.getString("value");
								slabel=stationlabel.getString("value");
							}
							
							
							if (auuid!=null && !auuid.equals("") && alabel!=null && !alabel.equals("")){
								mappe.put(auuid,alabel);
							}
							if (suuid!=null && !suuid.equals("") && slabel!=null && !slabel.equals("")){
								mappe.put(suuid,slabel);
							}

						} else {
							log.info("Expected JsonObject. Found " + currentArrayEntry.getClass());
						}
					}
					
					setUUIDMap(mappe);
					
				}};
				String sparqlQuery = PREFIXSTRING;
				sparqlQuery += " SELECT DISTINCT ?anlage ?anlagenLabel ?station ?stationLabel WHERE { { ?anlage a app:Anlage . ?station app:isPartOf ?anlage . ?anlage rdfs:label ?anlagenLabel . ?station rdfs:label ?stationLabel FILTER(LANG(?anlagenLabel)='de' && LANG(?stationLabel)='de')  } UNION { ?station a app:Station . ?station rdfs:label ?stationLabel FILTER(LANG(?stationLabel)='de' && NOT EXISTS{?station app:isPartOf ?_})} UNION  {  ?anlage a app:Anlage . ?anlage rdfs:label ?anlagenLabel . FILTER(LANG(?anlagenLabel)='de' && NOT EXISTS{?_ app:isPartOf ?anlage})}}"; 
						

								log.info("uuid map query: "+ sparqlQuery );
				sendSparQLQuery(sparqlQuery, eb, stationsInWorkplaceGroupsHandler);
	 }

protected void setUUIDMap(HashMap<String, String> mappe) {
		for (SparqlCallback callback : listener){
			callback.uuidMapreceived(mappe);
		}
		
	}
	// sends a Query to the semantic database
    // @param sparQLQuery SparQLQuery in question
    // @param eb Vertx eventbus object
    // @param stringHandler A Vertx String message Handler to deal with results
    private static void sendSparQLQuery(String sparQLQuery, EventBus eb,
            Handler<Message<String>> stringHandler)
    {
        // TODO: add logger("[MeasureService] - BasicSparQLQueries - sending Query:" +
        // sparQLQuery);
        JsonObject sQuery = new JsonObject().putString("query", sparQLQuery);
        eb.send(SPARQLREQUESTS, new JsonObject().putObject("sparql", sQuery),
                stringHandler);
    }
}

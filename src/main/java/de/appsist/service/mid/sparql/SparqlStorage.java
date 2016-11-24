package de.appsist.service.mid.sparql;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.appsist.service.iid.server.model.Level;
import de.appsist.service.mid.Globals;
import de.appsist.service.mid.MachineInformationManager;

public class SparqlStorage implements SparqlCallback {

	HashMap<String, String> uuidmap=null;
	Set<String> stateslist=null;
	EventBus eb;
	
    // logger for this class
    private Logger log = LoggerFactory.getLogger(MachineInformationManager.class);
	
	@Override
	public void uuidMapreceived(HashMap<String, String> map) {
		this.uuidmap=map;
		log.info("Sparql: Got the following uuidMap:");
		for (Entry<String,String> entry: map.entrySet()){
			log.debug(entry.getKey() + " -> " + entry.getValue());
		}
		
	}

	public int getSize(){
		return uuidmap.size();
	}
	public SparqlStorage(EventBus eb) {
		super();
		this.eb = eb;
	}

	@Override
	public void stateMapReceived(HashMap<String,Integer> mappe) {
		this.stateslist=mappe.keySet();

		String log="SparqlStorage: Found the following Machine States:";
		
		Globals.purgeStates();
		for (Entry<String,Integer> entry: mappe.entrySet()){
			Level level=parseLevel(entry.getValue());
			Globals.addState(entry.getKey(), level);
			log+=entry.getKey() + " -> " + level + "\n";
		}
		
		this.log.debug(log);
		
	}
	
	
	private Level parseLevel(Integer level) {
		if (level <= 3) return Level.INFO;
		if (level <= 7) return Level.WARNING;
		
		return Level.ERROR;
	}

	public void startup(){
		SparqlVerbinder conn=new SparqlVerbinder();
		conn.addListener(this);
		conn.getUUIDMap(eb);
		conn.getMachineStates(eb);
	}
	public String resolveUUID(String uuid){
		if (uuidmap==null){
			log.debug("Cannot resolve uuid " + uuid + " : uuid map not filled yet");
		}
		return uuidmap.get(uuid);
	}
}

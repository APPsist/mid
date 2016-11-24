package de.appsist.service.mid.sparql;

import java.util.HashMap;
import java.util.Set;

public interface SparqlCallback {
	
	public void uuidMapreceived(HashMap<String,String> map);
	public void stateMapReceived(HashMap<String, Integer> availableStates);

}

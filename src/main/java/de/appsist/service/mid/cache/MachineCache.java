package de.appsist.service.mid.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.appsist.service.mid.Globals;
import de.appsist.service.mid.MachineInformationManager;
import de.appsist.service.mid.rules.datatypes.ASValue;

public class MachineCache {

	TreeMap<String,ASValue> cache=new TreeMap<String,ASValue>();
	private static final Logger logger = LoggerFactory.getLogger(MachineCache.class);
	
	//Status state=new Status(-1, "uninitialized");
	
	/*String state=Globals.runningState;
	String stateTag="";*/
	
	ArrayList<String> states=new ArrayList<String>();



	private boolean newData=false;
	
	private MachineIdentifier machineID;
	
	public MachineCache(MachineIdentifier machineID) {
		super();
		this.machineID = machineID;
	}

	public Object get(String key) {
		return cache.get(key);
	}

	public MachineIdentifier getMachineID() {
		return machineID;
	}

	public void setMachineID(MachineIdentifier machineID) {
		this.machineID = machineID;
	}

	public void updateValue(String name, ASValue data){
		ASValue current=cache.get(name);
		if (current !=null && !current.getClass().equals(data.getClass())){
				logger.error("Type clash: Put value is " + data.getTypeString() + ", but cache is:" + current.getTypeString());
				return;
			}

		setData(name, data, current);
		
		
	}
	
	

	public boolean hasNewData() {
		if (newData){
			newData=false;
			return true;
		}
		return false;
	}

	private void setData(String name, ASValue data, ASValue current) {
		if (current ==null || !current.equals(data)){
			newData=true;
			cache.put(name, data);
			notifyUpdate();
		}
	}
	
	


	

	public TreeMap<String, ASValue> getCache() {
		return cache;
	}

	public void setCache(TreeMap<String, ASValue> cache) {
		this.cache = cache;
	}

	private void notifyUpdate() {
		MachineInformationManager.dieses.notifyUpdate(this);

		
	}

	public Set<Entry<String, ASValue>> entrySet() {
		return cache.entrySet();
	}

	public Set<String> keySet() {
		return cache.keySet();
	}

	public ArrayList<String> getStates() {
		
		if (states.size()==0){
			ArrayList<String> fehlerfrei=new ArrayList<String>();
			fehlerfrei.add(Globals.runningState);
			return fehlerfrei;
		}
		return states;
	}
	
	public boolean isFehlerfrei(){
		return states.size()==0;
	}

	public boolean addState(String state) {
		if (states.contains(state)) return false;
		
		states.add(state);
		return true;
	}
	
	public boolean removeState(String state) {
		if (!states.contains(state)) return false;
		
		states.remove(state);
		return true;
	}

	
}

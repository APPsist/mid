package de.appsist.service.mid.cache;

import java.util.HashMap;
import java.util.Set;

import de.appsist.service.mid.rules.datatypes.ASValue;

public class MIDCache {

	private HashMap<MachineIdentifier, MachineCache> caches=new HashMap<MachineIdentifier, MachineCache>();
	
	private boolean newCache=false;
	
	public void newData(MachineIdentifier id, String name, ASValue data){
		MachineCache cache=caches.get(id);
		if (cache==null){
			cache=new MachineCache(id);
			caches.put(id,cache);
			
			//new cache created -> set flag
			newCache=true;
		}
		cache.updateValue(name, data);
	}


	public MachineCache remove(MachineIdentifier key) {
		newCache=true;
		return caches.remove(key);
	}


	public boolean hasNewCache(){
		if (newCache){
			newCache=false;
			return true;
		}
		return false;
	}
	public MachineCache get(Object key) {
		return caches.get(key);
	}


	public MachineCache put(MachineIdentifier arg0, MachineCache arg1) {
		return caches.put(arg0, arg1);
	}

	public Set<MachineIdentifier> getMachineIDs(){
		return caches.keySet();
	}
	public String getAvailableMachines(){
		String ret="";
		for (MachineIdentifier id: caches.keySet()){
			ret +=id.toString()+"\n";
		}
		return ret;
	}
	/*public void setStatus(MachineIdentifier id, Status status) {
		MachineCache cache=caches.get(id);
		if (cache==null){
			cache=new MachineCache(id);
			caches.put(id,cache);
		}
		cache.setStatus(status);
		
	}*/
	
}

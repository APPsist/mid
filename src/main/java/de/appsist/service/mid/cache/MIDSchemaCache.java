package de.appsist.service.mid.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import de.appsist.service.middrv.entity.Machine;
import de.appsist.service.middrv.entity.MachineSchema;

public class MIDSchemaCache {
	private HashMap<MachineIdentifier,MachineSchema> schemas=new HashMap<MachineIdentifier,MachineSchema>();
	private HashMap<Machine,MachineIdentifier> convertMap=new HashMap<Machine,MachineIdentifier>();

	public MachineSchema get(Object key) {
		return schemas.get(key);
	}

	public MachineSchema put(MachineIdentifier key, MachineSchema value) {
		return schemas.put(key, value);
	}

	public void remove(Machine key) {
		schemas.remove(machineToMachineID(key));
		convertMap.remove(key);
	}
	
	public void removeSchema(MachineIdentifier key) {
		schemas.remove(key);
	}
	
	public MachineIdentifier machineToMachineID(Machine machine){
		return convertMap.get(machine);
	}
	
	public Machine machineIDToMachine(MachineIdentifier mid){
		
		for (Entry<Machine,MachineIdentifier> entry:convertMap.entrySet()){
			if (entry.getValue().equals(mid)){
				return entry.getKey();
			}
		}
		return null;
	}

	public MachineIdentifier putConvertEntry(Machine key, MachineIdentifier value) {
		return convertMap.put(key, value);
	}
	
	public ArrayList<String> getMachines(String Station){
		ArrayList<String> ret=new ArrayList<String>();
		for (MachineIdentifier id: schemas.keySet()){
			if (id.getStationID().equals(Station)){
				ret.add(id.getMachineID());
			}
		}
		Collections.sort(ret);
		
		return ret;
	}

	public Set<Entry<MachineIdentifier, MachineSchema>> getSchemas() {
		return schemas.entrySet();
	}
}

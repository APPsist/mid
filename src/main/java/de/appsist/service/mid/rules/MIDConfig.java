package de.appsist.service.mid.rules;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.cache.MachineIdentifier;

@XmlRootElement(name = "MIDConfig")
public class MIDConfig {
	
	@XmlElement(name = "instanceVersion")
	private String instanceVersion="1.0";
	@XmlElement(name = "protocolVersion")
	private String protocolVersion="1.0";
	//
	HashMap<MachineIdentifier,MachineConfiguration> rules=new HashMap<MachineIdentifier,MachineConfiguration>();
	
	public int getNumRules(){
		int ret=0;
		for (MachineConfiguration conf:rules.values()){
			ret+=conf.getRules().size();
		}
		
		return ret;
	}

	public MachineConfiguration get(Object key) {
		return rules.get(key);
	}

	public MachineConfiguration put(MachineIdentifier arg0,
			MachineConfiguration arg1) {
		return rules.put(arg0, arg1);
	}
	
	@XmlElement(name = "rulesMap")
	public HashMap<MachineIdentifier, MachineConfiguration> getRules() {
		return rules;
	}

	public void setRules(HashMap<MachineIdentifier, MachineConfiguration> rules) {
		this.rules = rules;
	}
}

package de.appsist.service.mid.rules;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.cache.MachineIdentifier;

@XmlRootElement(name = "MachineConfiguration")
public class MachineConfiguration {

	private MachineIdentifier machineId;
	//@XmlElement(name = "rules")
	private ArrayList<Rule> rules=new ArrayList<Rule>();
	@XmlElement(name = "MachineIdentifier")
	public MachineIdentifier getMachineId() {
		return machineId;
	}
	public void setMachineId(MachineIdentifier machineId) {
		this.machineId = machineId;
	}
	public ArrayList<Rule> getRules() {
		return rules;
	}
	public void setRules(ArrayList<Rule> rules) {
		this.rules = rules;
	}
	public boolean add(Rule e) {
		return rules.add(e);
	}
	public Rule remove(int index) {
		return rules.remove(index);
	}
	public boolean remove(Object arg0) {
		return rules.remove(arg0);
	}
	
	/**
	 * move condition up by one place, e.g. switch with predecessor
	 * @param index
	 */
	public void ruleUp(int index){
		if (index<=0) return;
		
		Rule pred=rules.get(index-1);
		Rule actual=rules.get(index);
		rules.set(index-1, actual);
		rules.set(index, pred);
	}
	
	/**
	 * move condition down by one place, e.g. switch with postdecessor
	 * @param index
	 */
	public void ruleDown(int index){
		if (index>=rules.size()-1||index==-1) return;
		
		Rule post=rules.get(index+1);
		Rule actual=rules.get(index);
		rules.set(index+1, actual);
		rules.set(index, post);
	}
	
}

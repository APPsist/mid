package de.appsist.service.mid.rules;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.cache.MachineCache;

@XmlRootElement(namespace = "de.appsist.service.mid.rules")
public class Rule {
	
	@XmlElement(name = "condition")
	private ArrayList<Condition> conditions=new ArrayList<Condition>();
	private ConditionMode mode=ConditionMode.ALL;
	
	private String action;
	private String tag;
	private String name;
	
	public String getAction() {
		return action;
	}

	@XmlElement(name = "action")
	public void setAction(String action) {
		this.action = action;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Rule(ArrayList<Condition> conditions, ConditionMode mode) {
		super();
		this.conditions = conditions;
		this.mode = mode;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Condition remove(int index) {
		return conditions.remove(index);
	}

	public Rule(){
		
	}
	public ArrayList<Condition> getConditions() {
		return conditions;
	}

	public ConditionMode getMode() {
		return mode;
	}

	public void setMode(ConditionMode mode) {
		this.mode = mode;
	}

	public boolean add(Condition e) {
		return conditions.add(e);
	}

	public boolean addAll(Collection<? extends Condition> c) {
		return conditions.addAll(c);
	}

	public boolean remove(Object arg0) {
		return conditions.remove(arg0);
	}
	
	/**
	 * check if rules are valid, e.g. no null variable, data types are valid etc.
	 * @return
	 */
	public String checkSanity(){
		for (int i=0; i<conditions.size();i++){
			String checkSanity = conditions.get(i).checkSanity();
			if (!checkSanity.equals("")){
				return "Condition #" + (i+1) + ": " + checkSanity;
			}
		}
		if (action==null || action.equals("")){
			return "Aktion ist leer (NULL)";
		}
		if (action==null || action.equals("")){
			return "Name ist leer (NULL)";
		}
		return "";
	}
	
	/**
	 * move condition up by one place, e.g. switch with predecessor
	 * @param index
	 */
	public void conditionUp(int index){
		if (index<=0) return;
		
		Condition pred=conditions.get(index-1);
		Condition actual=conditions.get(index);
		conditions.set(index-1, actual);
		conditions.set(index, pred);
	}
	
	/**
	 * move condition down by one place, e.g. switch with postdecessor
	 * @param index
	 */
	public void conditionDown(int index){
		if (index>=conditions.size()-1) return;
		
		Condition post=conditions.get(index+1);
		Condition actual=conditions.get(index);
		conditions.set(index+1, actual);
		conditions.set(index, post);
	}
	@Override
	public Rule clone(){
		Rule ret=new Rule();
		ret.mode=mode;
		if (action !=null) ret.action=new String(action);
		if (tag !=null) ret.tag=new String(tag);
		if (name !=null) ret.name=new String(name);
		for (Condition cond:conditions){
			ret.add(cond.clone());
		}
		
		return ret;
	}

	public boolean applies(MachineCache machineCache) {
		switch(this.mode){
		case ALL:
		
			for (Condition condition:conditions){
				if (!condition.applies(machineCache)) return false;
			}
			return true;
			
		case ONE:
			
			for (Condition condition:conditions){
				if (condition.applies(machineCache)) return true;
			}
			return false;
			
		case NONE:
			
			for (Condition condition:conditions){
				if (condition.applies(machineCache)) return false;
			}
			return true;
		default:
			
			//this cannot happen
			return false;
		
		}
	}
	
	
	
}

package de.appsist.service.mid.rules;

import javax.xml.bind.annotation.XmlRootElement;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.appsist.service.mid.cache.MachineCache;
import de.appsist.service.mid.rules.datatypes.ASNull;
import de.appsist.service.mid.rules.datatypes.ASValue;


@XmlRootElement(name = "Condition")
public class Condition {
	private Variable variable;
	private Comparator vergleich;
	private Variable value;
	private static final Logger logger = LoggerFactory.getLogger(Condition.class);
	public Condition(Variable variable, Comparator vergleich, Variable value) {
		super();
		this.variable = variable;
		this.vergleich = vergleich;
		this.value = value;
	}
	
	public Condition(){
		
	}
	public Variable getVariable() {
		return variable;
	}
	public Comparator getVergleich() {
		return vergleich;
	}
	public Variable getValue() {
		return value;
	}
	public void setVariable(Variable variable) {
		this.variable = variable;
	}
	public void setVergleich(Comparator vergleich) {
		this.vergleich = vergleich;
	}
	public void setValue(Variable value) {
		this.value = value;
	}
	
	public String checkSanity(){
		if (variable.getValue() instanceof ASNull) return "Variable ist leer (NULL)";
		if (value.getValue() instanceof ASNull) return "Der Wert zur Variable '"+ variable.getCaption() +"' ist leer (NULL) oder inkompatibel";
		
		if (variable.getValue().getClass() != value.getValue().getClass()) return "Typ nicht kompatibel bei Variable '" + variable.getCaption() + "'.\nVariable ist: " + variable.getValue().getClass() + "\nWert ist:" + value.getValue().getClass();
		return "";
	}
	
	@Override
	public Condition clone(){
		Condition ret=new Condition(variable.clone(), vergleich, value.clone());
		return ret;
	}

	public boolean applies(MachineCache machineCache) {
		ASValue val=(ASValue) machineCache.get(variable.caption);
		if (val==null){
			logger.info("Info: Condition.applies: Searched variable " + variable.caption + " is not yet available in cache. Cannot apply rule now. Wait until variable was pushed the first time by the machine.");
			return false;
		}
		if (val.compareTo(value.getValue(), vergleich)){
			return true;
		}
		return false;
	}
}

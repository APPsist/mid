package de.appsist.service.mid.rules;

import de.appsist.service.mid.rules.datatypes.ASValue;

public class Variable {
	String caption;
	
	/*@XmlElements({
        @XmlElement(name="value",type=ASDouble.class),
        @XmlElement(name="value",type=ASNull.class),
    })*/
	
	//@XmlElement(name="value")
	ASValue value;
	public Variable(String caption, ASValue value) {
		super();
		this.caption = caption;
		this.value = value;
	}
	
	public Variable(){
		
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public void setValue(ASValue value) {
		this.value = value;
	}
	public ASValue getValue() {
		return value;
	}

	@Override
	public Variable clone(){
		Variable ret=new Variable();
		ret.setCaption(caption);
		ret.setValue(value.clone());
		
		return ret;
	}

	
}

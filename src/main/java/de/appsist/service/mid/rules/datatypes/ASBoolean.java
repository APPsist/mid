package de.appsist.service.mid.rules.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.rules.Comparator;

@XmlRootElement(name = "ASBoolean")
public class ASBoolean extends ASValue {

	@XmlElement(name="value")
	boolean value=false;
	
	@Override
	public String toString() {
		
		return value + "";
	}

	public boolean getBoolean(){
		return value;
	}
	@Override
	public boolean fromString(String s) {
		try{
			value=Boolean.parseBoolean(s);
			return true;
		}
		catch(NumberFormatException nf){
			return false;
		}
	}

	public ASBoolean(){
		
	}
	public ASBoolean(String value) {
		super();
		fromString(value);
	}
	
	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return "ASBoolean";
	}

	@Override
	public boolean compareTo(ASValue other, Comparator comp) {
		
		if (!(other instanceof ASBoolean)) return false;
		
		ASBoolean val2=(ASBoolean) other;
		
		switch(comp){
		case EQ:
			return (val2.value==value);
		case NEQ:
			return !(val2.value==value);
		default:
			return false;
		}
		
	}

	@Override
	public boolean compatibleWith(ASValue other) {

		if (!(other instanceof ASBoolean)){
			return false;
		}
		return true;
	}

	@Override
	public ASValue createSameType(String in) {

		return new ASBoolean(in+"");
	}

	@Override
	public ASValue clone() {

		return new ASBoolean(value+"");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASBoolean other = (ASBoolean) obj;
		if (value != other.value)
			return false;
		return true;
	}



}

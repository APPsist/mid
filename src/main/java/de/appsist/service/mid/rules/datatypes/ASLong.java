package de.appsist.service.mid.rules.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.rules.Comparator;

@XmlRootElement(name = "ASLong")
public class ASLong extends ASValue {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		ASLong other = (ASLong) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@XmlElement(name="value")
	long value=0;
	@Override
	public boolean fromString(String s) {
		// TODO Auto-generated method stub
		try{
			value=Long.parseLong(s);
			return true;
		}
		catch(NumberFormatException nf){
			return false;
		}
	}

	public ASLong(){
		
	}
	public ASLong(String value) {
		super();
		fromString(value);
	}

	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return "ASLong";
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return value+"";
	}

	@Override
	public boolean compareTo(ASValue other,Comparator comp) {
		if (other instanceof ASLong){
			long otherVal=((ASLong)other).getValue();
			return compareLongs(otherVal,comp);
		}
		return false;
	}

	private boolean compareLongs(long otherVal, Comparator comp) {
		switch(comp){
		case EQ:
			return value == otherVal;
		case NEQ:
			return value != otherVal;
		case LE:
			return value < otherVal;
		case LEQ:
			return value <= otherVal;
		case GEQ:
			return value >= otherVal;
		case GR:
			return value > otherVal;
		default:
			throw new IllegalStateException("This cannot happen");
		}
	}

	@Override
	public boolean compatibleWith(ASValue other) {
		if (other instanceof ASLong){
			return true;
		}
		return false;
	}

	@Override
	public ASValue createSameType(String in) {
		ASLong ret=new ASLong();
		
		if (!ret.fromString(in)){
			return new ASNull();
			
		}
		return ret;
	}

	@Override
	public ASValue clone() {
		ASLong ret=new ASLong();
		ret.value=value;
		return ret;
	}
	

}

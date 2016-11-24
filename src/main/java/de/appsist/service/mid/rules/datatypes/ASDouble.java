package de.appsist.service.mid.rules.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.rules.Comparator;

@XmlRootElement(name = "ASDouble")
public class ASDouble extends ASValue {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ASDouble other = (ASDouble) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}

	@XmlElement(name="value")
	double value=0;
	@Override
	public boolean fromString(String s) {
		// TODO Auto-generated method stub
		try{
			value=Double.parseDouble(s);
			return true;
		}
		catch(NumberFormatException nf){
			return false;
		}
	}

	public ASDouble(){
		
	}
	public ASDouble(String value) {
		super();
		fromString(value);
	}

	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return "ASDouble";
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return value+"";
	}

	@Override
	public boolean compareTo(ASValue other,Comparator comp) {
		if (other instanceof ASDouble){
			double otherVal=((ASDouble)other).getValue();
			return compareDoubles(otherVal,comp);
		}
		return false;
	}

	private boolean compareDoubles(double otherVal, Comparator comp) {
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
		if (other instanceof ASDouble){
			return true;
		}
		return false;
	}

	@Override
	public ASValue createSameType(String in) {
		ASDouble ret=new ASDouble();
		
		if (!ret.fromString(in)){
			return new ASNull();
			
		}
		return ret;
	}

	@Override
	public ASValue clone() {
		ASDouble ret=new ASDouble();
		ret.value=value;
		return ret;
	}
	

}

package de.appsist.service.mid.rules.datatypes;

import javax.xml.bind.annotation.XmlElement;

import de.appsist.service.mid.rules.Comparator;

public class ASString extends ASValue {

	@XmlElement(name="value")
	String value;
	
	@Override
	public String toString() {
	
		return value;
	}

	@Override
	public boolean fromString(String s) {

		value=s;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ASString other = (ASString) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String getTypeString() {

		return "ASString";
	}

	@Override
	public boolean compareTo(ASValue other, Comparator comp) {
		if (!other.getClass().equals(this.getClass())){
			return false;
		}
		ASString otherString=(ASString) other;
		return (otherString.value.equals(value));
	}

	@Override
	public boolean compatibleWith(ASValue other) {

		return other.getClass().equals(this.getClass());
	}

	@Override
	public ASValue createSameType(String in) {

		return new ASString(in);
	}

	public ASString(){
		value="";
	}
	public ASString(String value) {
		super();
		fromString(value);
	}
	@Override
	public ASValue clone() {

		return new ASString(value);
	}

}

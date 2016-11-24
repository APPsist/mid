package de.appsist.service.mid.rules.datatypes;

import javax.xml.bind.annotation.XmlRootElement;

import de.appsist.service.mid.rules.Comparator;

@XmlRootElement(name = "ASNull")
public class ASNull extends ASValue {

	@Override
	public boolean fromString(String s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean compareTo(ASValue other, Comparator comp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean compatibleWith(ASValue other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ASValue createSameType(String in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ASValue clone() {
		// TODO Auto-generated method stub
		return new ASNull();
	}

	@Override
	public boolean equals(Object obj) {

		return true;
	}

}

package de.appsist.service.mid.rules.datatypes;

import javax.xml.bind.annotation.XmlTransient;

import de.appsist.service.mid.rules.Comparator;

@XmlTransient
public abstract class ASValue {

	@Override
	public abstract String toString();
	public abstract boolean fromString(String s);
	public abstract String getTypeString();
	
	public abstract boolean compareTo(ASValue other, Comparator comp);
	public abstract boolean compatibleWith(ASValue other);
	
	
	public abstract ASValue createSameType(String in);
	
	@Override
	public abstract ASValue clone();
	@Override
	public abstract boolean equals(Object obj);

	public static ASValue createFromString(String type, String data){
		ASValue ret=null;
		if (type.equals("ASDouble")){
			ret=new ASDouble();
		}
		if (type.equals("ASBoolean")){
			ret=new ASBoolean();
		}
		if (type.equals("ASLong")){
			ret=new ASLong();
		}
		if (type.equals("ASString")){
			ret=new ASString();
		}
		
		if (ret==null) return null;
		
		ret.fromString(data);
		
		return ret;
	}
}

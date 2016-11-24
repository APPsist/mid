package de.appsist.service.mid.rules;

public class Util {
	public static String comparatorToString(Comparator in){
		switch(in){
		case EQ:
			return "==";
		case NEQ:
			return "!=";
		case LE:
			return "<";
		case LEQ:
			return "<=";
		case GEQ:
			return ">=";
		case GR:
			return ">";
		default:
			throw new IllegalStateException("This cannot happen");
		}
	}
	public static String[] getAllCompString(){
		Comparator[] all=Comparator.values();
		
		String[] ret=new String[all.length];
		
		for (int i=0;i<all.length;i++){
			ret[i]=comparatorToString(all[i]);
		}
		
		return ret;
	}
	
	public static Comparator stringToComparator(String in){
		if (in.equals("==")){
			return Comparator.EQ;
		}
		if (in.equals("!=")){
			return Comparator.NEQ;
		}
		if (in.equals(">=")){
			return Comparator.GEQ;
		}
		if (in.equals("<=")){
			return Comparator.LEQ;
		}
		if (in.equals("<")){
			return Comparator.LE;
		}
		if (in.equals(">")){
			return Comparator.GR;
		}
		throw new IllegalArgumentException("This cannot happen");
	}
}

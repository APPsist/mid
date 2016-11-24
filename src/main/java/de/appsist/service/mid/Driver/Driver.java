package de.appsist.service.mid.Driver;

import de.appsist.service.mid.rules.Variable;
import de.appsist.service.mid.rules.datatypes.ASDouble;

public class Driver {
	public static Variable getType(String variable){
		return new Variable(variable, new ASDouble("1.0"));
		//TODO: implement
	}
	
	
}

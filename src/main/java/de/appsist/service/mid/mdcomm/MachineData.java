/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.appsist.service.mid.mdcomm;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Denise
 */
public class MachineData {
    
    private HashMap<String,Double> data=new HashMap<String,Double>();
    // machine data als KV-pair: Bezeichner(String) -> Wert (Double) 
    
    
    
    public MachineData(){

    }
    
    /**
     * adds/changes a value in the data set
     */
    public void setValue(String name, double value){
    	//note: "put" overrides a given KV-pair with the same key 
    	data.put(name, value);
    }

    /**
     * "Purges" the data with the given name
     * @param name
     */
    public void resetValue(String name){
    	data.remove(name);
    }
    @Override
    public String toString() {
    	String valString="";
    	
    	boolean first=true;
    	
    	for (Entry<String,Double> entry:data.entrySet()){
    		
    		
    		//make sure the first entry is not preceeded by colon
    		if (!first){
    			valString+=" , ";
    		}
    		else{
    			first=false;
    		}
    		
    		//add kv string
    		valString+=entry.getKey() + " = " + entry.getValue();
    	}
        return "MachineData{" + valString +  '}';
    }   
    
}

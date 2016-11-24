package de.appsist.service.mid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.appsist.service.iid.server.model.Level;

public class Globals {

	public final static boolean cebitHack=false;
	public static String runningState="Funktionsfaehiger Zustand";
	/*public static String[] states;//=new String[]{"Stillstand","Warnung","Werkstoff wenig", "Loctite wenig","Fett wenig","gemessener Fehler", "Fehlendes Bauteil",
										"Warnungsfrei","Externe Störung", "Werkstoff leer", "Loctite leer", "Fett leer", "Anlagenbedingte Störung", "fehlendes Werkzeug"
						
										};
	
	public static String[] infostates;//=new String[]{"Stillstand","Warnungsfrei"};
	public static String[] warningStates=new String[]{"Warnung","Werkstoff wenig", "Loctite wenig","Fett wenig"};
	public static String[] errorStates=new String[]{"gemessener Fehler", "Fehlendes Bauteil",
			"Warnungsfrei","Externe Störung", "Werkstoff leer", "Loctite leer", "Fett leer", "Anlagenbedingte Störung", "fehlendes Werkzeug"};*/
	
	private static HashMap<String, Level> states=new HashMap<String,Level>();
	private static ArrayList<String> sortedStates=new ArrayList<String>();
	private static final Logger logger = LoggerFactory.getLogger(Globals.class);
	
	public static Level getMostSevereLevel(ArrayList<String> states){
		if (states.size()==0) return Level.INFO;

		boolean warningseen=false;
		
		for (String s:states){
			if (getStatusLevel(s)==Level.ERROR) return Level.ERROR; //gibt mindestens einen fehler: zustand ist fehler
			if (getStatusLevel(s)==Level.WARNING) warningseen=true;
		}
		
		if (warningseen) return Level.WARNING;
		
		return Level.INFO;
		
	}
	public static Level getStatusLevel(String status){
		if (states.containsKey(status)){
			return states.get(status);
		}
		else{
			logger.warn("status not found in globals state list:" + status);
			return Level.INFO;
		}
	}
	
	public static Set<String> getAllStates(){
		return states.keySet();
	}
	
	public static ArrayList<String> getSortedStates(){
		return sortedStates;
	}
	
	public static boolean isInArray (String var, String[] arr){
		for (String s : arr){
			if (s.equals(var)) return true;
		}
		
		return false;
	}
	
	public static void purgeStates(){
		states.clear();
		sortedStates.clear();
	}
	
	public static void addState(String state, Level level){
		states.put(state, level);
		createSortedStates();
	}
	
	public static boolean loadGlobals(String path){
		BufferedReader br = null;
		Logger log = LoggerFactory.getLogger(MachineInformationManager.class);
		
		try {

			String sCurrentLine;

			File f=new File(path + "globals.txt");
			
			if (!f.exists()){
				log.error("globals.txt not found at " + System.getProperty("user.dir") + " with path " + path);
			}
			
			br = new BufferedReader(new FileReader(path + "globals.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] split=sCurrentLine.split("@");
				if (split.length!=2){
					logger.warn("Ignored invalid globals config line: " + sCurrentLine  );
				}
				else{
					Level stateLevel=parseStatestring(split[1]);
					states.put(split[0],stateLevel);
				}
			}
			
			createSortedStates();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (br != null)br.close();
				return true;
			} catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
		}

	}

	private static void createSortedStates() {
		ArrayList<String> errors=getStatesForLevel(Level.ERROR);
		ArrayList<String> warnings=getStatesForLevel(Level.WARNING);
		ArrayList<String> infos=getStatesForLevel(Level.INFO);
		
		//sort the lists
		Collections.sort(errors);
		Collections.sort(warnings);
		Collections.sort(infos);
		
		//concatenate lists in the order infos->warnings->errors
		infos.add("----------------");
		infos.addAll(warnings);
		infos.add("----------------");
		infos.addAll(errors);
		
		sortedStates=infos;
		
	}
	
	private static ArrayList<String> getStatesForLevel(Level level){
		ArrayList<String> ret=new ArrayList<String>();
		
		for (Entry<String,Level> entry:states.entrySet()){
			if (entry.getValue()==level) ret.add(entry.getKey());
		}
		
		return ret;
	}

	private static Level parseStatestring(String string) {
		if (string.equals("error")) return Level.ERROR;
		if (string.equals("warning")) return Level.WARNING;
		if (string.equals("info")) return Level.INFO;
		
		logger.warn("Warning: loadGlobals: level parsing failed. using level.INFO as default");
		return Level.INFO;
	}
	
}

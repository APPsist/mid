package de.appsist.service.mid.serialize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.appsist.service.mid.rules.MIDConfig;
import de.appsist.service.mid.rules.MachineConfiguration;
import de.appsist.service.mid.rules.Rule;
import de.appsist.service.mid.rules.datatypes.ASBoolean;
import de.appsist.service.mid.rules.datatypes.ASDouble;
import de.appsist.service.mid.rules.datatypes.ASLong;
import de.appsist.service.mid.rules.datatypes.ASNull;
import de.appsist.service.mid.rules.datatypes.ASString;
import de.appsist.service.mid.rules.datatypes.ASValue;


public class MIDSerializer {
public static boolean serializeRule (Rule regel, String fileName){
    try {
    	JAXBContext context = JAXBContext.newInstance(Rule.class, ASValue.class, ASDouble.class, ASNull.class, ASLong.class, ASBoolean.class, ASString.class);

	    Marshaller m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			m.marshal(regel, new File(fileName));
			
			return true;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
}


public static Rule deserializeRule(String fileName){
    try {	
	JAXBContext context = JAXBContext.newInstance(Rule.class, ASValue.class, ASDouble.class, ASNull.class, ASLong.class, ASBoolean.class, ASString.class);
	
	// create new file input stream
    FileInputStream fis = new FileInputStream(fileName);
    
    // unmarshaller obj to convert xml data to java content tree

		Unmarshaller u = context.createUnmarshaller();
		Rule regel=(Rule) u.unmarshal(fis);
		return regel;
		
		
	} catch (JAXBException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    return null;
}

public static boolean serializeConfig (MIDConfig config, String fileName){
    try {
    	JAXBContext context = JAXBContext.newInstance(Rule.class, ASValue.class, ASDouble.class, ASNull.class, MIDConfig.class,MachineConfiguration.class, ASLong.class, ASBoolean.class, ASString.class);

	    Marshaller m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			m.marshal(config, new File(fileName));
			
			return true;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
}

public static MIDConfig deserializeConfig(String fileName) throws FileNotFoundException{
    try {	
	JAXBContext context = JAXBContext.newInstance(Rule.class, ASValue.class, ASDouble.class, ASNull.class, MIDConfig.class,MachineConfiguration.class, ASLong.class, ASBoolean.class, ASString.class);
	
	// create new file input stream
    FileInputStream fis = new FileInputStream(fileName);
    
    // unmarshaller obj to convert xml data to java content tree

		Unmarshaller u = context.createUnmarshaller();
		MIDConfig conf=(MIDConfig) u.unmarshal(fis);
		
		return conf;
		
		
	} catch (JAXBException e) {
		int answer=JOptionPane.showConfirmDialog(null,"MID-Konfiguration ist fehlerhaft!\n\nMöchten Sie die Konfiguration zurücksetzen (Programm wird sonst beendet)?");

		if (answer==JOptionPane.CANCEL_OPTION||answer==JOptionPane.NO_OPTION){
			System.exit(1);
		}
		return new MIDConfig();
	
	}
    

}

}

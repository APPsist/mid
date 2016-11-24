package de.appsist.service.mid.Driver;


import de.appsist.service.middrv.rest.*;

import de.appsist.service.middrv.entity.Machine;
import de.appsist.service.middrv.entity.MachineData;
import de.appsist.service.middrv.entity.MachineSchema;
import de.appsist.service.middrv.entity.MachineValueSpecification;
import de.appsist.service.middrv.entity.MachineValueType;
import de.appsist.service.middrv.entity.Unit;
import de.appsist.service.middrv.entity.VisualizationLevel;
import de.appsist.service.middrv.entity.VisualizationType;
import de.appsist.service.middrv.rest.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientMockup {

	private static void sendHeartBeat(RestClient client, String basePath, String machineId) {
		Map<String, String> params = new HashMap<>();
		params.put(Constants.GET_PARAM_MACHINE_ID, machineId);
		client.get(basePath + Constants.RES_CLIENT_HEART_BEAT, ContentType.JSON, params);
	}

    private static ExecutorService startHeartBeatThread(final RestClient client, String basePath, String machineId, long heartBeatInterval) {
        final String finalBasePath = new String(basePath);
        final String finalMachineId = new String(machineId);
        if (finalBasePath.contains(" ") || finalMachineId.contains(" "))
            throw new IllegalArgumentException("Cannot make a GET request containing a space!\n"
                    + "BasePath: \"" + finalBasePath + "\", Machine Id: \"" + machineId + "\"");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendHeartBeat(client, finalBasePath, finalMachineId);
            }
        };
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(runnable, 0, heartBeatInterval, TimeUnit.MILLISECONDS);
        return executorService;
    }

	public static void main(String[] args) throws InterruptedException {
		
		String basePath ="/services/mid";
		String host ="localhost";
		int port=8095;
		
		int site=1;
		String machineId = "Maschine20";
        long heartBeatInterval = 15000; // set this value to the value specified in telko

		for (String arg:args){
			if (arg.startsWith("-p")){
				//set port
				try{
					port=Integer.parseInt(arg.substring(2));
				}
				catch(NumberFormatException nf){
					System.err.println("MID: Port is no number");
				}
				System.out.println("MID Rest Port=" + port);
			}
			
			if (arg.startsWith("-b")){
				//set port
				basePath=arg.substring(2);
				System.out.println("MID basePath=" + basePath);
			}
			
			if (arg.startsWith("-h")){
				//set port
				host=arg.substring(2);
				System.out.println("MID host=" + host);
			}
			
			if (arg.startsWith("-?")){
				//show quick tip
				
				System.out.println("Dieses wunderschöne Tool simuliert an den MID angeschlossene Maschinen!\n\nSyntax: java -jar clientMockup.jar <options>\n\n\nAlle optionen sind hierbei optional:\n\n	-p<Port> : verwende Port <Port> zur Kommunikation mit dem MID-Server. Standard: 8095\n	-h<Host> : Verbinde mit MID Server auf <Host>. Standard:localhost\n	-s<Site> : Simuliere Maschine an Anlage <Site>. Standard: 1\n	"
						+ "-m<Machine> : Simuliere Maschine mit Nr. <Machine>. Standard:20" );
				return;
			}
			
			if (arg.startsWith("-s")){
				//set site
				try{
					site=Integer.parseInt(arg.substring(2));
				}
				catch(NumberFormatException nf){
					System.err.println("MID: Site is no number");
				}
				System.out.println("MID Site=" + site);
			}

			if (arg.startsWith("-m")){
				machineId = arg.substring(2);
				System.out.println("MID machine=" + machineId);
			}
		}

		final RestClient client = new RestClient("localhost", port, basePath);

		// Create background thread which checks if a machine did not report back within heartBeatinterval
        ExecutorService executorService = startHeartBeatThread(client, basePath, machineId, heartBeatInterval);


		Machine fmachine = new Machine("Anlage1", machineId, "RV-2FB Robot Arm Controller");

		MachineSchema fschema = new MachineSchema(fmachine, "Anlage1", "Anlage1");
		fschema.addField(new MachineValueSpecification("Loctite", MachineValueType.DOUBLE, Unit.NONE, VisualizationType.PRECENT_BAR, VisualizationLevel.OVERVIEW));
		fschema.addField(new MachineValueSpecification("Fett", MachineValueType.DOUBLE, Unit.NONE, VisualizationType.PRECENT_BAR, VisualizationLevel.OVERVIEW));
		
		
		MachineData fdata=new MachineData(fmachine);
		fdata.put("Loctite", 1.0);
		fdata.put("Fett", 1.0);

		
		MachineData fdatae1=new MachineData(fmachine);
		fdatae1.put("Loctite", 0.01);
		fdatae1.put("Fett", 1.0);

		MachineData fdatae2=new MachineData(fmachine);
		fdata.put("Loctite", 1.0);
		fdata.put("Fett", 0.02);
		
		
		DataMessage dataMessage1 = new DataMessage();
		DataMessage dataMessage2 = new DataMessage();
		DataMessage dataMessage3 = new DataMessage();
		try{
			dataMessage1.addMachineData(fdata, fschema);
			dataMessage2.addMachineData(fdatae1, fschema);
			dataMessage3.addMachineData(fdatae2, fschema);
		} catch(DataSchemaMismatchException e){
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		
		
		
		
		
		
		
		
		/*
		// Prepare data for test messages
		Machine station1 = new Machine(site, machine, "Machine");
		MachineData station1Data1 = new MachineData(station1);
		station1Data1.put("temperature", 3.13159);
		station1Data1.put("switch", true);
		station1Data1.put("cycles", 42);
		
		MachineData station1Data2 = new MachineData(station1);
		
		station1Data2.put("temperature", 2.5);
		station1Data2.put("switch", false);
		station1Data2.put("cycles", 82);
		
		MachineData station1Data3 = new MachineData(station1);
		
		station1Data3.put("temperature", 1.5);
		station1Data3.put("switch", false);
		station1Data3.put("cycles", 102);
		station1Data3.setStatus(13, "Boese13");
		
		MachineSchema station1Schema = new MachineSchema(station1, machine, site);
		station1Schema.addField("temperature", MachineValueType.DOUBLE, Unit.CELSIUS, VisualizationType.TEXT_FIELD, VisualizationLevel.OVERVIEW);
		station1Schema.addField("switch", MachineValueType.BOOL, Unit.NONE, VisualizationType.ON_OFF_LIGHT, VisualizationLevel.DETAIL);
		station1Schema.addField("cycles", MachineValueType.LONG, Unit.NONE);
		station1Schema.addField("comment", MachineValueType.STRING, Unit.NONE);
		station1Schema.addField("unused value", MachineValueType.BOOL, Unit.NONE);
		/*
		Machine station2 = new Machine(1, 20, "First component of Machine");
		MachineSchema station2Schema = new MachineSchema(station2, 2, 1);
		station2Schema.addField("pressure", MachineValueType.DOUBLE, Unit.MILLI_BAR, VisualizationType.TEXT_FIELD, VisualizationLevel.OVERVIEW);
		MachineData station2Data = new MachineData(station2);
		station2Data.put("pressure", 1013.11);
		
		Machine station3 = new Machine(1, 20, "Second component of Machine");
		MachineSchema station3Schema = new MachineSchema(station3, 3, 1);
		station3Schema.addField("voltage", MachineValueType.DOUBLE, Unit.VOLT, VisualizationType.TEXT_FIELD, VisualizationLevel.OVERVIEW);
		MachineData station3Data = new MachineData(station3);
		station3Data.put("voltage", 23.91);*/
		
		/*
		SchemaMessage schemaMessage = new SchemaMessage();
		schemaMessage.addSchema(station1Schema);
		//schemaMessage.addSchema(station2Schema);
		//schemaMessage.addSchema(station3Schema);
		*/
		/*DataMessage dataMessage1 = new DataMessage();
		DataMessage dataMessage2 = new DataMessage();
		DataMessage dataMessage3 = new DataMessage();
		try{
			dataMessage1.addMachineData(station1Data1, station1Schema);
			//dataMessage1.addMachineData(station2Data, station2Schema);
			//dataMessage1.addMachineData(station3Data, station3Schema);
			dataMessage2.addMachineData(station1Data2, station1Schema);
			dataMessage3.addMachineData(station1Data3, station1Schema);
			//dataMessage2.addMachineData(station2Data, station2Schema);
			//dataMessage2.addMachineData(station3Data, station3Schema);
		} catch(DataSchemaMismatchException e){
			e.printStackTrace();
			System.exit(-1);
		}
		*/

		// Optionally: Sending Schema. (If Server does not know Schema yet, Schema will automatically be sent)
		
		//System.out.println(schemaMessage.toJson());

		/*System.out.println("Sending schema in JSON: " + schemaMessage);
		client.send(ContentType.JSON, schemaMessage);
		System.out.println("Sending schema in XML: " + schemaMessage);
		client.send(ContentType.XML, schemaMessage);
		System.out.println("Sending schema in JSON: " + schemaMessage);
		client.send(ContentType.JSON, schemaMessage);*/
		
		// Send test messages
		
		//Thread.sleep(500);
		
		/*station1Data1.put("comment", "json content");
		station1Data2.put("comment", "json content");
		station1Data3.put("comment", "json content");*/
		System.out.println("Sending data in JSON " + dataMessage1);
		client.send(ContentType.JSON, dataMessage1);
		System.out.println("Sending data in JSON " + dataMessage2);
		client.send(ContentType.JSON, dataMessage2);
		System.out.println("Sending data in JSON " + dataMessage3);
		client.send(ContentType.JSON, dataMessage3);

		
		client.send(ContentType.JSON, dataMessage1);
		//Thread.sleep(500);
		
		/*station1Data1.put("comment", "xml content");
		station1Data3.put("comment", "xml content");
		System.out.println("Sending data in XML " + dataMessage1);
		client.send(ContentType.XML, dataMessage1);
		System.out.println("Sending data in XML " + dataMessage3);
		client.send(ContentType.XML, dataMessage3);
		*/

        // Await ExecutorService such that background thread does not die
        executorService.awaitTermination(100000, TimeUnit.DAYS);
		/*Thread.sleep(10000);

		System.out.println("Client quit");
		System.exit(0);*/
	}

}

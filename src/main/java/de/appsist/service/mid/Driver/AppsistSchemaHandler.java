package de.appsist.service.mid.Driver;


import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.middrv.entity.MachineSchema;
import de.appsist.service.middrv.entity.MachineValueType;
import de.appsist.service.middrv.entity.Serializer;
import de.appsist.service.middrv.entity.Status;
import de.appsist.service.middrv.entity.Unit;
import de.appsist.service.middrv.entity.VisualizationLevel;
import de.appsist.service.middrv.entity.VisualizationType;
import de.appsist.service.middrv.rest.ContentType;
import de.appsist.service.middrv.rest.SchemaMessage;
import de.appsist.service.middrv.rest.server.SchemaHandler;

public class AppsistSchemaHandler implements SchemaHandler{

	private MIDSchemaCache schemaCache;
	private static final Logger logger = LoggerFactory.getLogger(AppsistSchemaHandler.class);
	
	@Override
	public void handleNewSchema(SchemaMessage msg, HttpServerResponse response,
			ContentType responseContentType) {
		
		//System.out.println("Got schmea message, entries: " + schemas.size());
		for (MachineSchema schema : msg.getSchemas()){
			
			//artificial entries for errorCode & description
			schema.addField("errorCode", MachineValueType.LONG, Unit.NONE,VisualizationType.TEXT_FIELD, VisualizationLevel.DETAIL);
			schema.addField("errorDescription", MachineValueType.STRING, Unit.NONE,VisualizationType.TEXT_FIELD, VisualizationLevel.DETAIL);
			
			
			String stationId=schema.getSiteID();//"Anlage" + schema.getSiteID();
			String machineId= schema.getMachine().getMachineID(); //"Maschine"+ entry.getKey().getMachineID()+"";

			
			MachineIdentifier mid=new MachineIdentifier(machineId, stationId);
			
			schemaCache.removeSchema(mid);
			
			logger.debug("Received machine schema: " + stationId + ", " + machineId);
			schemaCache.put(mid,schema);
			schemaCache.putConvertEntry(schema.getMachine(), mid);
		}
		response.setStatusCode(201);
		try {
			response.putHeader("Content-Type", responseContentType.toString());
			response.end(Serializer.serializeToBuffer(new Status(0, "Data message parsed"), responseContentType));
		} catch (Exception e) {
			e.printStackTrace();
			response.putHeader("Content-Type", ContentType.TEXT_PLAIN.toString());
			response.end("Failed to generate response: " + e.getMessage());
		}
		
	}

	public AppsistSchemaHandler(MIDSchemaCache schemaCache) {
		super();
		this.schemaCache = schemaCache;
	}

	

}

package de.appsist.service.mid.Driver;

import java.util.Map.Entry;

import de.appsist.service.middrv.entity.*;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.appsist.service.mid.cache.MIDCache;
import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.mid.rules.datatypes.ASBoolean;
import de.appsist.service.mid.rules.datatypes.ASDouble;
import de.appsist.service.mid.rules.datatypes.ASLong;
import de.appsist.service.mid.rules.datatypes.ASNull;
import de.appsist.service.mid.rules.datatypes.ASString;
import de.appsist.service.mid.rules.datatypes.ASValue;
import de.appsist.service.middrv.rest.ContentType;
import de.appsist.service.middrv.rest.DataMessage;
import de.appsist.service.middrv.rest.server.DataMessageHandler;

public class AppsistDataHandler implements DataMessageHandler {

	private MIDCache dataCache;
	private MIDSchemaCache schemaCache;
	private static final Logger logger = LoggerFactory.getLogger(AppsistDataHandler.class);
	
	@Override
	public void handleDataMessage(DataMessage msg, HttpServerResponse response,
			ContentType responseContentType) {
		logger.debug("Got data message entries: " + msg.getContent().size());
		for (MachineData data:msg.getContent()){
            logger.debug("Data: " + data);
            MachineIdentifier id=schemaCache.machineToMachineID(data.getMachine());
			//DEPRECATED: UNCOMMENT THIS IF DRIVER SHOULD AFFECT MACHINE STATE
			//dataCache.setStatus(id, data.getStatus());
			
			if (id==null){
				logger.error("CRITICAL IMPOSSIBLE ERROR: Cannot resolve machineID for machine. Data package ommitted");
				return;
			}
			for (Entry<String, MachineValue> entry:data.entrySet()){
				
				ASValue val=machineValueToASValue(entry.getValue());
				dataCache.newData(id, entry.getKey(), val);
			}
			
			dataCache.newData(id, "errorCode", new ASLong(data.getStatus().getCode()+""));

			logger.debug("Received machine data: station " + id.getStationID() + ", machine " + id.getMachineID());
			dataCache.newData(id, "errorDescription", new ASString(data.getStatus().getDescription()));

			
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


	public AppsistDataHandler(MIDCache dataCache, MIDSchemaCache schemaCache) {
		super();
		this.dataCache = dataCache;
		this.schemaCache = schemaCache;
	}



	private ASValue machineValueToASValue(MachineValue val){
		MachineValueImpl impl=(MachineValueImpl) val;
		//uuhh this is nasty
		
		switch (impl.getType()){
		case DOUBLE:
			Double dval=(Double) impl.getValue();
			return new ASDouble(dval+"");
		case BOOL:
			Boolean bval=(Boolean) impl.getValue();
			return new ASBoolean(bval+"");
		case STRING:
			String sval=(String) impl.getValue();
			return new ASString(sval);
		case LONG:
			Long lval=(Long) impl.getValue();
			return new ASLong(lval+"");
		default:
			return new ASNull();
		}
	}

}

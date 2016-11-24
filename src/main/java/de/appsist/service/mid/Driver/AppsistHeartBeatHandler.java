package de.appsist.service.mid.Driver;


import de.appsist.service.mid.MachineInformationManager;
import de.appsist.service.mid.cache.MIDCache;
import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.middrv.entity.Machine;
import de.appsist.service.middrv.rest.server.HeartBeatHandler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class AppsistHeartBeatHandler implements HeartBeatHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppsistHeartBeatHandler.class);

	private MIDCache dataCache;
	private MIDSchemaCache schemaCache;

	public AppsistHeartBeatHandler(MIDCache dataCache, MIDSchemaCache schemaCache) {
		super();
		this.dataCache = dataCache;
		this.schemaCache = schemaCache;
	}

	@Override
    public void handleLostMachine(Machine machine) {
        logger.info("Lost connection to machine: " + machine.getMachineID());
        dataCache.remove(schemaCache.machineToMachineID(machine));
        schemaCache.remove(machine);
        MachineInformationManager.dieses.updateSiteInformation();
    }
}

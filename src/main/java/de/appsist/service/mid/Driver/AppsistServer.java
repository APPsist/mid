package de.appsist.service.mid.Driver;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.logging.Logger;

import de.appsist.service.middrv.rest.server.*;

public class AppsistServer {
	
	public void start(Logger logger, Vertx vertx, DataMessageHandler datahandler, SchemaHandler schemaHandler, HeartBeatHandler heartBeatHandler, int port, String basePath){
		
		if (vertx==null){
			vertx=VertxFactory.newVertx();
		}
		//RestServer server = new RestServer(VertxFactory.newVertx(), "localhost", exampleHandler, exampleHandler);
		//RestServer server = new RestServer(VertxFactory.newVertx(), getHostename(), "localhost", exampleHandler, exampleHandler);
		long heartBeatInterval = 30000;
		RestServer server = new RestServer(vertx, datahandler, schemaHandler, heartBeatHandler, basePath, heartBeatInterval);
		
		server.listenHttp(port); //todo: put port here
		/*JDialog dialogQuit = (new JOptionPane("REST-Server läuft. Mit OK Beenden")).createDialog("APPsist REST");
		dialogQuit.setAlwaysOnTop(true);
		dialogQuit.setModal(true);
		dialogQuit.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialogQuit.setVisible(true);
		
		System.out.println("Server quit");
		System.exit(0);*/
	}
}

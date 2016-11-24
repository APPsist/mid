package de.appsist.service.mid.mdcomm;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

/**
 *
 * @author Denise
 */
@Path("message")
public class MessageResource {

    @PUT
    @Path("MachineDataMessage/")
    @Consumes(MediaType.APPLICATION_JSON)
    public String postDataMessage(String message) {
        System.out.println("Machinendatennachricht angekommen: " + message);
        Gson gson = new Gson();
        try {
            MachineDataMessage machineDataMessage = gson.fromJson(message, MachineDataMessage.class);
            //MachineInformationManager.dieses.getCache().newData(new MachineIdentifier("01", "20"), "temperature", 33.0);
            return "Empfangene Maschinendaten: " + machineDataMessage.toString();
        }
        catch (Exception e) {
            System.out.println("Maschinendatennachricht fehlerhaft!");
            return "Maschinendatennachricht fehlerhaft!";
        }
    }
    
    @PUT
    @Path("ErrorMessage/")
    @Consumes(MediaType.APPLICATION_JSON)
    public String postErrorMessage(String message) {
        System.out.println("Fehlernachricht angekommen: " + message);
        Gson gson = new Gson();
        try {
            ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
            return "Empfangene Fehlernachricht: " + errorMessage.toString();
        }
        catch (Exception e) {
            System.out.println("Fehlernachricht fehlerhaft!");
            return "Fehlernachricht fehlerhaft!";
        }
    }
}

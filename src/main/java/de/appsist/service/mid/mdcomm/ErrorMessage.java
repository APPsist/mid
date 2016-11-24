package de.appsist.service.mid.mdcomm;

/**
 *
 * @author Denise
 */
public class ErrorMessage {
    
    private MachineData machineData;
    private long time;
    private int errorID;

    
    public ErrorMessage() {
    }

    public ErrorMessage(MachineData machineData, long time, int errorID) {
        this.machineData = machineData;
        this.time = time;
        this.errorID = errorID;
    }
    
    public MachineData getMachineData() {
        return machineData;
    }

    public void setMachineData(MachineData machineData) {
        this.machineData = machineData;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getErrorID() {
        return errorID;
    }

    public void setErrorID(int errorID) {
        this.errorID = errorID;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" + "machineData=" + machineData + ", time=" + time + ", errorID=" + errorID + '}';
    }
    
}

package de.appsist.service.mid.mdcomm;

/**
 *
 * @author Denise
 */
public class MachineDataMessage {
    
    private MachineData machineData;
    private long time;

    
    public MachineDataMessage() {
    }
  
    public MachineDataMessage(MachineData machineData, long time) {
        this.machineData = machineData;
        this.time = time;
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

    @Override
    public String toString() {
        return "MachineDataMessage{" + "machineData=" + machineData + ", time=" + time + '}';
    }
    
}

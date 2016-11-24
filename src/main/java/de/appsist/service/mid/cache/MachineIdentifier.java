package de.appsist.service.mid.cache;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MachineIdentifier")
public class MachineIdentifier {

	private static int counter=0;
	
	private int id=counter++;
	
	private String stationID;
	private String machineID;
	
	public int getId() {
		return id;
	}

	public MachineIdentifier(String machineID, String stationID) {
		super();
		this.machineID = machineID;
		this.stationID = stationID;
	}
	
	public MachineIdentifier(){
		
	}
	public String getMachineID() {
		return machineID;
	}
	@Override
	public String toString() {
		return "MachineIdentifier [stationID=" + stationID + ", machineID=" + machineID + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((machineID == null) ? 0 : machineID.hashCode());
		result = prime * result + ((stationID == null) ? 0 : stationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MachineIdentifier other = (MachineIdentifier) obj;
		if (machineID == null) {
			if (other.machineID != null)
				return false;
		} else if (!machineID.equals(other.machineID))
			return false;
		if (stationID == null) {
			if (other.stationID != null)
				return false;
		} else if (!stationID.equals(other.stationID))
			return false;
		return true;
	}

	public void setMachineID(String machineID) {
		this.machineID = machineID;
	}
	public String getStationID() {
		return stationID;
	}
	public void setStationID(String stationID) {
		this.stationID = stationID;
	}
	
}

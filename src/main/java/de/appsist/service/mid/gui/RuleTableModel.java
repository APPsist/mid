package de.appsist.service.mid.gui;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.mid.rules.Comparator;
import de.appsist.service.mid.rules.Condition;
import de.appsist.service.mid.rules.Rule;
import de.appsist.service.mid.rules.Util;
import de.appsist.service.mid.rules.Variable;
import de.appsist.service.mid.rules.datatypes.ASBoolean;
import de.appsist.service.mid.rules.datatypes.ASDouble;
import de.appsist.service.mid.rules.datatypes.ASLong;
import de.appsist.service.mid.rules.datatypes.ASNull;
import de.appsist.service.mid.rules.datatypes.ASString;
import de.appsist.service.mid.rules.datatypes.ASValue;
import de.appsist.service.middrv.entity.MachineValueType;

public class RuleTableModel extends AbstractTableModel {

	private Rule regel;
	
	private MIDSchemaCache schemaCache;
	
	private MachineIdentifier machineId;
	


	

	public RuleTableModel(Rule regel) {
		super();
		this.regel = regel;
	}

	public MIDSchemaCache getSchemaCache() {
		return schemaCache;
	}

	public void setSchemaCache(MIDSchemaCache schemaCache) {
		this.schemaCache = schemaCache;
	}

	@Override
	public int getColumnCount() {
		return 3;
		
	}

	public MachineIdentifier getMachineId() {
		return machineId;
	}

	public void setMachineId(MachineIdentifier machineId) {
		this.machineId = machineId;
	}

	public Rule getRegel() {
		return regel;
	}

	public void setRegel(Rule regel) {
		this.regel = regel;
	}

	@Override
	public int getRowCount() {
		
		return regel.getConditions().size();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		Condition cond=regel.getConditions().get(arg0);
		if (arg1==0){
			//get variable name
			return cond.getVariable().getCaption();
		}
		if (arg1==1){
			//get comnparator
			return Util.comparatorToString(cond.getVergleich());
		}
		if (arg1==2){
			//get variable name
			return cond.getValue().getValue().toString();
		}
		return null;
	}
	
    @Override
	public boolean isCellEditable(int row, int col) {

       return true;
    }

    public void addEmptyCondition(){
    	Variable var=new Variable("",new ASNull());
    	Variable wert=new Variable("",new ASNull());
    	
    	Condition cond=new Condition(var,Comparator.EQ,wert);
    	regel.add(cond);
    	fireTableDataChanged();
    }
    public void removeCondition(int index){
    	    	
    	regel.remove(index);
    	
    	fireTableDataChanged();
    }
    
    @Override
	public void setValueAt(Object value, int row, int col) {
        Condition cond=regel.getConditions().get(row);
        
        if (col==0){
        	//set variable name
        	Variable neu=getVariableType((String) value);
        	cond.setVariable(neu);
        }
        
        if (col==1){
        	//set comparison
        	cond.setVergleich(Util.stringToComparator((String) value));
        }
       
        if (col==2){
        	ASValue newVal=cond.getVariable().getValue().createSameType((String) value);
        	
        	if (newVal instanceof ASNull){
        		//TODO: cannot parse -> msgbox and set back
        		JOptionPane.showMessageDialog(null, "Ungültige Eingabe. Versuchen Sie es erneut.");
        		return;
        	}
        	//set comparison
        	cond.getValue().setValue(newVal);
        }
        
        fireTableCellUpdated(row, col);
    }

	private Variable getVariableType(String value) {
		// TODO Auto-generated method stub
		MachineValueType mtype= schemaCache.get(machineId).getType(value);
		Variable ret=new Variable(value, machineValueToASValue(mtype));
		return ret;
	}
	
	private ASValue machineValueToASValue(MachineValueType val){

		
		switch (val){
		case DOUBLE:
			return new ASDouble("0");
		case BOOL:
			return new ASBoolean("false");
		case STRING:
			return new ASString("");
		case LONG:
			return new ASLong("0");
		default:
			return new ASNull();
		}
	}

}

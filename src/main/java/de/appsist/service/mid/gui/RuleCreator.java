package de.appsist.service.mid.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.appsist.service.mid.Globals;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.mid.rules.ConditionMode;
import de.appsist.service.mid.rules.Rule;
import de.appsist.service.mid.rules.Util;
import de.appsist.service.middrv.entity.MachineSchema;

public class RuleCreator extends JFrame{

	private static final boolean showPicture = false;
	
	private String[] vergleiche= Util.getAllCompString();
	
	private MachineIdentifier machineID;
	public MachineIdentifier getMachineID() {
		return machineID;
	}

	public void setMachineID(MachineIdentifier machineID) {
		this.machineID = machineID;
	}

	private Rule regel=new Rule();
	private RuleTableModel tableModel=new RuleTableModel(regel);

	private JTable table;

	private JComboBox<String> cmbEvents;

	private JTextField txtTag;

	private JTextField txtName;
	
	private MIDGui parent;

	private JRadioButton radOne;

	private JRadioButton radNone;

	private JRadioButton radAll;
	
	public Rule getRegel() {
		return regel;
	}

	public void setRegel(Rule regel) {
		
		Rule klon=regel.clone();
		

		//always keep tblModel syncronized
		tableModel.setRegel(klon);
		
		switch(klon.getMode()){
			case ALL:
				radAll.setSelected(true);
				break;
			case NONE:
				radNone.setSelected(true);
				break;
			case ONE:
				radOne.setSelected(true);
				break;
		}
		
		cmbEvents.setSelectedItem(klon.getAction());
		txtTag.setText(klon.getTag());
		txtName.setText(klon.getName());
		this.regel = klon;
	}

	public static void main (String[] args){
		RuleCreator gui=new RuleCreator();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.showUp();
		gui.setVisible(true);
	}

	public void showUp() {
		buildGUI();
		
	}

	public RuleCreator(){
		buildGUI();
	}
	private void buildGUI() {
		
		setTitle("Rule details");
		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		this.getContentPane().add(mainPanel);
		this.setPreferredSize(new Dimension(800, 600));
		
		
		//##############   top box -> rule name
		
		JPanel namePanel=new JPanel();
		//namePanel.setLayout(new BorderLayout());
		namePanel.setLayout(new GridLayout());
		
		TitledBorder nametitle = BorderFactory.createTitledBorder("Name");
		namePanel.setBorder(nametitle);
		
		txtName = new JTextField("New rule");
		
		namePanel.add(txtName);
		
		mainPanel.add(namePanel);
		
		
		
		//############## conditional selector
		
		JPanel panCond=new JPanel();
		panCond.setLayout(new GridLayout());
		mainPanel.add(panCond);
		TitledBorder condTitle=new TitledBorder("Zu erfüllende Bedingungen");
		panCond.setBorder(condTitle);
		ButtonGroup condGroup=new ButtonGroup();
		
		radAll = new JRadioButton("Alle Bedingungen");
		radAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				condTypeAll();
				
			}
		});
		condGroup.add(radAll);
		panCond.add(radAll);
		radOne = new JRadioButton("Mindestens eine Bedingung");
		radOne.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				condTypeOne();
				
			}
		});
		panCond.add(radOne);
		condGroup.add(radOne);
		radNone = new JRadioButton("Keine der Bedingungen");
		radNone.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				condTypeNone();
				
			}
		});
		panCond.add(radNone);
		condGroup.add(radNone);
		
		
		
		
		//###################### Bedingungsfeld
		
		JPanel panConditions=new JPanel();
		

		
		panConditions.setLayout(new BorderLayout());
		TitledBorder condEdit=new TitledBorder("Bedingungen");
		panConditions.setBorder(condEdit);
		
		JTable tblConditions=createSpecialTable();
		panConditions.add(tblConditions,BorderLayout.CENTER);
		
		//up/down buttons
		
		
		JPanel panUpDown=new JPanel();

		panUpDown.setLayout(new GridLayout(2,1));
		//panUpDown.setLayout(new BoxLayout(panUpDown, BoxLayout.Y_AXIS));
		JButton cmdUp=new JButton();
		
		try {
		    Image img = ImageIO.read(new File("res/arrow-up.png"));
		    cmdUp.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		  }
		
		cmdUp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdUpClicked();
				
			}
		});
		
		JButton cmdDown=new JButton();
		try {
		    Image img = ImageIO.read(new File("res/arrow-down.png"));
		    cmdDown.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		  }
		cmdDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdDownClicked();
				
			}
		});
		
		panUpDown.add(cmdUp);
		panUpDown.add(cmdDown);
		
		//add/remove buttons
		
		JPanel panAddRemove=new JPanel();
		panAddRemove.setLayout(new GridLayout());
		
		JButton cmdAddCondition=new JButton("+ Bedingung hinzufügen");
		cmdAddCondition.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addRuleClicked();
				
			}
		});
		
		JButton cmdRemoveCondition=new JButton("- Bedingung entfernen");
		cmdRemoveCondition.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeRuleClicked();
				
			}
		});
		
		panAddRemove.add(cmdAddCondition);
		panAddRemove.add(cmdRemoveCondition);
		panConditions.add(panAddRemove,BorderLayout.SOUTH);
		//panConditions.add(panUpDown,BorderLayout.EAST);
		
		mainPanel.add(panConditions);

		
		
		
		//############### Aktionsfeld
		
		JPanel panActions=new JPanel();
		BoxLayout layActions=new BoxLayout(panActions, BoxLayout.PAGE_AXIS);
		panActions.setLayout(layActions);
		TitledBorder borActions=new TitledBorder("Aktion");	
		panActions.setBorder(borActions);
		
		mainPanel.add(panActions);
		
		String[] eventStrings=getEventStrings();

		JPanel panActionbox=new JPanel();
		TitledBorder borAction=new TitledBorder("Event");
		panActionbox.setBorder(borAction);
		
		JPanel panTagbox=new JPanel();
		TitledBorder borTag=new TitledBorder("Tag");
		panTagbox.setBorder(borTag);
		panTagbox.setLayout(new GridLayout());
		
		cmbEvents = new JComboBox<String>(eventStrings);
		cmbEvents.setSelectedIndex(0);
		cmbEvents.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				actionChanged();
				
			}
		});
		
		panActionbox.add(cmbEvents);
		
		panActions.add(cmbEvents);
		//panActions.add(panTagbox);
		
		txtTag = new JTextField();
		panTagbox.add(txtTag);
		
		//############# Bottom button group
		
		JPanel panBottom=new JPanel();
		mainPanel.add(panBottom);
		
		JButton cmdOK=new JButton("OK");
		cmdOK.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdOKClicked();
				
			}
		});
		JButton cmdCancel=new JButton("Cancel");
		
		cmdCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdCancelClicked();
			}
		});
		
		panBottom.add(cmdOK);
		panBottom.add(cmdCancel);
		
		
		
		pack();
		//setVisible(true);
		
	}

	protected void cmdDownClicked() {
		regel.conditionUp(table.getSelectedRow());
		tableModel.fireTableDataChanged();
		
	}

	protected void cmdUpClicked() {
		if (table.getSelectedRow()<=1) return;

		regel.conditionUp(table.getSelectedRow());
		tableModel.fireTableDataChanged();
	}

	protected void actionChanged() {
		Object selectedItem = cmbEvents.getSelectedItem();
		regel.setAction((String) selectedItem);
		
	}

	protected void condTypeNone() {
		regel.setMode(ConditionMode.NONE);
		
	}

	protected void condTypeOne() {
		regel.setMode(ConditionMode.ONE);
		
	}

	protected void condTypeAll() {
		regel.setMode(ConditionMode.ALL);
		
	}


	private JTable createSpecialTable() {
		
		/*String[] columnNames = {"Variable", "Vergleich", "Wert"};
	    Object[][] data = {
	      {"Druck", ">", "2"}, {"Druck", "<", "0.5"}
	    };
	    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
	      @Override public Class<?> getColumnClass(int column) {
	        return getValueAt(0, column).getClass();
	      }
	    };*/
		
		
		// create debug rule set
/*		Variable druck=new Variable("Druck", new ASDouble());
		Variable val1=new Variable("",new ASDouble("0.5"));
		Variable val2=new Variable("",new ASDouble("22.0"));
		
		
		Condition cond1=new Condition(druck, Comparator.LE,val1);
		Condition cond2=new Condition(druck, Comparator.GR,val2);
		
		
		Rule regelTest=new Rule();
		regelTest.add(cond1);
		regelTest.add(cond2);
		
		MachineConfiguration conf=new MachineConfiguration();
		MachineIdentifier machineId = new MachineIdentifier("1", "20");
		conf.setMachineId(machineId);
		conf.add(regelTest);
		MIDConfig midconf=new MIDConfig();
		midconf.put(machineId, conf);
		
		MIDSerializer.serializeConfig(midconf, "test.mcf");
		
		setRegel(regelTest);*/
		
		
		
	    table = new JTable(tableModel);
	    updateVariablesCombobox();
	    
	    TableColumn colVergleich = table.getColumnModel().getColumn(1);
	    colVergleich.setCellRenderer(new ComboBoxCellRenderer(vergleiche));
	    colVergleich.setCellEditor(new ComboBoxCellEditor(vergleiche));
	    

	    return table;
	}

	private void updateVariablesCombobox() {
		TableColumn colVariable = table.getColumnModel().getColumn(0);
	    colVariable.setCellRenderer(new ComboBoxCellRenderer(getVariables()));
	    colVariable.setCellEditor(new ComboBoxCellEditor(getVariables()));
	}

	@Override
	public void setVisible(boolean b) {
		// TODO Auto-generated method stub
		updateVariablesCombobox();
		super.setVisible(b);
	}

	private String[] getVariables() {
		if (getParent()==null||getParent().getSchemaCache()==null){
			//when form is first rendered
			return new String[]{};
		}
		
		MachineSchema schema=getParent().getSchemaCache().get(machineID);
		
		
		
		if (schema==null){
			System.err.println("Warning: no Machine schema available! Rules editor will not work.\n\nAvailable schemas:\n");
			
			for (Entry<MachineIdentifier,MachineSchema> entry: getParent().getSchemaCache().getSchemas()){
				System.out.println(entry.getKey() + "\n");
			}
			System.out.println("\n\n");
			return new String[]{};
		}
		Set<String> schemaKeys = schema.getSchemaKeys();
		String[] ret=new String[schemaKeys.size()];
		
		int i=0;
		for (String var:schemaKeys){
			ret[i]=var;
			i++;
		}
		return ret;
	}

	protected void cmdCancelClicked() {

		
		//do nothing, discard the new rule config
		setVisible(false);
		
	}

	@Override
	public MIDGui getParent() {
		return parent;
	}

	public void setParent(MIDGui parent) {
		this.parent = parent;
		tableModel.setSchemaCache(parent.getSchemaCache());
		tableModel.setMachineId(machineID);
	}

	protected void cmdOKClicked() {
		String sane=regel.checkSanity();
		if (!sane.equals("")){
			JOptionPane.showMessageDialog(null, "Die Regel kann nicht abgespeichert werden, da sie nicht gültig ist:\n\n" + sane);
			return;
		}
		
		if (regel.getAction().equals("----------------")){
			//es wurde der trennbalken ausgewählt.. dummer benutzer..
			
			JOptionPane.showMessageDialog(null, "Die Regel kann nicht abgespeichert werden, da die ausgewählte Aktion ungültig ist");
			return;
			
		}
		regel.setTag(txtTag.getText());
		regel.setName(txtName.getText());
		
		//MIDSerializer.serializeRule(regel, "rules.mid");
		
		parent.applyRuleChanges(regel);
		setVisible(false);
		
	}

	private String[] getEventStrings() {
		String[] ret=new String[Globals.getSortedStates().size()];
		
		int i=0;
		for (String str:Globals.getSortedStates()){
			ret[i++]=str;
		}
		
		return ret;
	}

	protected void removeRuleClicked() {
		tableModel.removeCondition(table.getSelectedRow());
		
	}

	protected void addRuleClicked() {
		tableModel.addEmptyCondition();
		
	}

	protected void idChanged() {
		// TODO Auto-generated method stub
		
	}
	
	
	// special table classes stuff
	
	
	class ComboBoxCellRenderer extends ComboBoxPanel implements
			TableCellRenderer {
		public ComboBoxCellRenderer(String[] entries) {
			super(entries);
			setName("Table.cellRenderer");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground(isSelected ? table.getSelectionBackground() : table
					.getBackground());
			if (value != null) {
				comboBox.setSelectedItem(value);
			}
			return this;
		}
	}
	
	class ComboBoxPanel extends JPanel {
		  protected JComboBox<String> comboBox;
		  public ComboBoxPanel(String[] entries) {
		    super();
		    
		    this.setLayout(new BorderLayout());
		    comboBox = new JComboBox<String>(entries) {
			    @Override public Dimension getPreferredSize() {
			      Dimension d = super.getPreferredSize();
			      return new Dimension(40, d.height);
			    }
			  };
			  
		    setOpaque(true);
		    comboBox.setEditable(true);
		    add(comboBox,BorderLayout.CENTER);
		  }
		}

	class ComboBoxCellEditor extends ComboBoxPanel implements TableCellEditor {
		public ComboBoxCellEditor(String[] entries) {
			super(entries);
			comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					fireEditingStopped();
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.setBackground(table.getSelectionBackground());
			comboBox.setSelectedItem(value);
			return this;
		}

		// Copid from DefaultCellEditor.EditorDelegate
		@Override
		public Object getCellEditorValue() {
			return comboBox.getSelectedItem();
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				MouseEvent e = (MouseEvent) anEvent;
				return e.getID() != MouseEvent.MOUSE_DRAGGED;
			}
			return true;
		}

		@Override
		public boolean stopCellEditing() {
			if (comboBox.isEditable()) {
				comboBox.actionPerformed(new ActionEvent(this, 0, ""));
			}
			fireEditingStopped();
			return true;
		}

		// Copid from AbstractCellEditor
		// protected EventListenerList listenerList = new EventListenerList();
		transient protected ChangeEvent changeEvent = null;

		@Override
		public boolean isCellEditable(EventObject e) {
			return true;
		}

		@Override
		public void cancelCellEditing() {
			fireEditingCanceled();
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
			listenerList.add(CellEditorListener.class, l);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			listenerList.remove(CellEditorListener.class, l);
		}

		public CellEditorListener[] getCellEditorListeners() {
			return listenerList.getListeners(CellEditorListener.class);
		}

		protected void fireEditingStopped() {
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == CellEditorListener.class) {
					// Lazily create the event:
					if (changeEvent == null)
						changeEvent = new ChangeEvent(this);
					((CellEditorListener) listeners[i + 1])
							.editingStopped(changeEvent);
				}
			}
		}

		protected void fireEditingCanceled() {
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == CellEditorListener.class) {
					// Lazily create the event:
					if (changeEvent == null)
						changeEvent = new ChangeEvent(this);
					((CellEditorListener) listeners[i + 1])
							.editingCanceled(changeEvent);
				}
			}
		}
	}
	
	
	
	
	
	
	
}

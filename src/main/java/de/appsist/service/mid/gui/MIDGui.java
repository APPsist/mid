package de.appsist.service.mid.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import de.appsist.service.mid.Driver.AppsistHeartBeatHandler;
import de.appsist.service.mid.Globals;
import de.appsist.service.mid.Driver.AppsistSchemaHandler;
import de.appsist.service.mid.Driver.AppsistServer;
import de.appsist.service.mid.cache.MIDSchemaCache;
import de.appsist.service.mid.cache.MachineIdentifier;
import de.appsist.service.mid.rules.MIDConfig;
import de.appsist.service.mid.rules.MachineConfiguration;
import de.appsist.service.mid.rules.Rule;
import de.appsist.service.mid.serialize.MIDSerializer;
import example.SysOutLoggerDelegate;
import org.vertx.java.core.logging.Logger;

public class MIDGui extends JFrame{

	private static final boolean showPicture = false;
	private RuleCreator creatorWindow;
	private MIDConfig config;
	private JTable tblRules;
	private MachineIdentifier currentMachine;
	private JComboBox<String> cmbStation;
	private JComboBox<String> cmbMachine;
	private MachineConfiguration currentRules=new MachineConfiguration();
	private int lastSelectedRuleIndex=-1;
	DefaultTableModel tableModel;
	
	//Driver comm
	
	private MIDSchemaCache schemaCache=new MIDSchemaCache();
	AppsistSchemaHandler handler=new AppsistSchemaHandler(schemaCache);
	//AppsistHeartBeatHandler heartBeatHandler = new AppsistHeartBeatHandler(null,schemaCache);
	AppsistServer server=new AppsistServer();

	
	boolean changesMade=false;
	
	public static void main (String[] args){

		MIDGui gui=new MIDGui();
		
		
		gui.setVisible(true);
	}



	public MIDSchemaCache getSchemaCache() {
		return schemaCache;
	}



	public MIDGui(){
		

		server.start(new Logger(new SysOutLoggerDelegate()), null, null,handler, null, 8095,"/services/mid");
		
		

		
		setTitle("AppSIST Rule Editor");
		MIDSerializer serializer=new MIDSerializer();
		try{
			config=MIDSerializer.deserializeConfig("rules.mid");
			MachineIdentifier id= new MachineIdentifier("Maschine 1","Station 1");
			MachineConfiguration mc=this.config.get(id);
			System.out.println(mc);
		}
		catch (FileNotFoundException nf){
			System.err.println("Warning: No MID config file (rules.mid) found. Creating empty one.");
			
		}
		if (config==null){
			config=new MIDConfig();
		}
		
		Globals.loadGlobals("");
		creatorWindow=new RuleCreator();
		buildGUI();
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				onApplicationClose();
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	protected void onApplicationClose() {
		//anwendung schlißet->speicher
		
		if (!changesMade) return;
		
		int dialogResult = JOptionPane.showConfirmDialog (null, "Möchten Sie die Änderungen speichern?","Anwendung schließt",JOptionPane.YES_NO_OPTION);
		
		if (dialogResult==JOptionPane.YES_OPTION){
			MIDSerializer.serializeConfig(config, "rules.mid");
		}
		
	}



	private void buildGUI() {
		
		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new BorderLayout(30, 30));
		this.getContentPane().add(mainPanel);
		this.setPreferredSize(new Dimension(800, 600));
		
		
		//##############   TOP selectors
		
		JPanel topPanel=new JPanel();
		
		GroupLayout topLayout=new GroupLayout(topPanel);
		topPanel.setLayout(topLayout);
		mainPanel.add(topPanel,BorderLayout.NORTH);
		
		String[] stationStrings = { "Bitte wählen..","Anlage1", "Anlage2", "Anlage3", "TAL01", "DNC_DNCB_DSBC_Automation" };
		
		cmbStation = new JComboBox<String>(stationStrings);
		cmbStation.setSelectedIndex(0);
		cmbStation.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				idChanged();
				
			}
		});
		
		String[] machineStrings = { "Bitte wählen..", "Maschine1", "Maschine2", "Maschine20", "Maschine30", "MVM700", "Station20" };
		
		cmbMachine = new JComboBox<String>(machineStrings);
		cmbMachine.setSelectedIndex(0);
		cmbMachine.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				idChanged();
				
			}
		});
		
		
		BufferedImage picAppsist = null;
		try {
			picAppsist = ImageIO.read(new File("res/appsist.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JLabel picLabel=new JLabel("Appsist Regeleditor");
		
		if (picAppsist!=null&&showPicture){
			 picLabel= new JLabel(new ImageIcon(picAppsist));
			
		}
		
		JLabel lblmachineCaption=new JLabel("Loctite System X");
		
		topLayout.setVerticalGroup(topLayout.createSequentialGroup().
				
				addGroup(topLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
						addComponent(cmbStation).addComponent(cmbMachine)).	
				addGroup(topLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
						addComponent(lblmachineCaption).addComponent(picLabel)));

		topLayout.setHorizontalGroup(topLayout.createSequentialGroup().
				
				addGroup(topLayout.createParallelGroup(GroupLayout.Alignment.LEADING).
							addComponent(cmbStation).addComponent(lblmachineCaption)).
				addGroup(topLayout.createParallelGroup(GroupLayout.Alignment.CENTER).
						addComponent(cmbMachine).addComponent(picLabel))		
						);
		
		//#################  Center stuff --> rules table
		
		JPanel panCenter=new JPanel();
		panCenter.setLayout(new BorderLayout());
		mainPanel.add(panCenter,BorderLayout.CENTER);
		
		String[][] rules=new String[][]{{ } };
		String[] captions=new String[]{"Regeln"};
		
		tableModel=new NullTableModel(rules,captions);
		tblRules = new JTable(tableModel);
		
		tblRules.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openExistingRule();
				}
				
			}
		});
		panCenter.add(tblRules, BorderLayout.CENTER);
		
		
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
				
				panCenter.add(panUpDown,BorderLayout.EAST);
		
		
		//########### bottom stuff -> edit buttons
		
		JPanel panBottom=new JPanel();
		panBottom.setLayout(new BorderLayout());
		mainPanel.add(panBottom,BorderLayout.SOUTH);
		
		JButton cmdAddrule=new JButton("+ Neue Regel");
		cmdAddrule.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				addRuleClicked();
			}
		});
		
		panBottom.add(cmdAddrule,BorderLayout.WEST);
		
		JButton cmdRemoverule=new JButton("- Regel löschen");
		cmdRemoverule.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				removeRuleClicked();
			}
		});
		
		panBottom.add(cmdRemoverule,BorderLayout.EAST);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pack();
		setVisible(true);
		
	}

	protected void cmdDownClicked() {
		int selected=this.tblRules.getSelectedRow();
		
		MachineConfiguration currentConfig=config.get(currentMachine);
		if (currentConfig==null) return;
		currentConfig.ruleDown(selected);
		
		loadRulesList();
		this.changesMade=true;
	}



	protected void cmdUpClicked() {
		int selected=this.tblRules.getSelectedRow();
		
		MachineConfiguration currentConfig=config.get(currentMachine);
		if (currentConfig==null) return;
		currentConfig.ruleUp(selected);
		
		loadRulesList();
		this.changesMade=true;
		
	}



	protected void openExistingRule() {
		lastSelectedRuleIndex = tblRules.getSelectedRow();
		if (lastSelectedRuleIndex<0 || lastSelectedRuleIndex>=currentRules.getRules().size()) return;
		
		Rule selected=currentRules.getRules().get(lastSelectedRuleIndex);
		this.creatorWindow.setRegel(selected.clone());
		showCreator();
	}



	protected void removeRuleClicked() {
		int selected=this.tblRules.getSelectedRow();
		MachineConfiguration currentConfig=config.get(currentMachine);
		
		currentConfig.remove(selected);
		loadRulesList();

		
	}

	protected void addRuleClicked() {
		if (currentMachine==null){
			JOptionPane.showMessageDialog(null, "Bitte wählen Sie erst Station und Maschine aus");
			return;
		}
		lastSelectedRuleIndex=-1;
		creatorWindow.setRegel(new Rule());
		showCreator();
		
		
	}

	private void showCreator() {
		creatorWindow.setMachineID(currentMachine);
		creatorWindow.setParent(this);
		this.creatorWindow.setVisible(true);
		
	}



	protected void idChanged() {
		if (cmbMachine.getSelectedIndex()==0||cmbStation.getSelectedIndex()==0) return;
		currentMachine=new MachineIdentifier((String)cmbMachine.getSelectedItem(), (String) cmbStation.getSelectedItem());
		System.out.println(currentMachine);
		loadRulesList();
		
	}



	private void loadRulesList() {
		currentRules = config.get(currentMachine);
				
		//clear rows
		for (int i=tableModel.getRowCount()-1;i>=0;i--){
			tableModel.removeRow(0);
		}
		
		if (currentRules==null) return;
		for (Rule rule:currentRules.getRules()){
			tableModel.addRow(new String[]{rule.getName()});
		}
		
		tableModel.fireTableDataChanged();
	}



	public void applyRuleChanges(Rule regel) {
		changesMade=true;
		if (currentRules==null) {
			//no rules for this machine yet
			MachineConfiguration config=new MachineConfiguration();
			config.add(regel);
			config.setMachineId(currentMachine);
			this.config.put(currentMachine,config);
			currentRules=config;
			
		}
		else{
			if (lastSelectedRuleIndex!=-1){
				currentRules.remove(lastSelectedRuleIndex);
			}	

			currentRules.add(regel);

		}
		
		loadRulesList();
	}
}

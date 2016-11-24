package de.appsist.service.mid.gui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * a model where no cell is editable
 * @author cyre
 *
 */
public class NullTableModel extends DefaultTableModel {

	@Override
	public boolean isCellEditable(int row, int column) {
		// TODO Auto-generated method stub
		return false;
	}

	public NullTableModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NullTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
		// TODO Auto-generated constructor stub
	}

	public NullTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
		// TODO Auto-generated constructor stub
	}

	public NullTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
		// TODO Auto-generated constructor stub
	}

	public NullTableModel(Vector columnNames, int rowCount) {
		super(columnNames, rowCount);
		// TODO Auto-generated constructor stub
	}

	public NullTableModel(Vector data, Vector columnNames) {
		super(data, columnNames);
		// TODO Auto-generated constructor stub
	}

	

}

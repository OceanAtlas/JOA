package javaoceanatlas.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.*;
import java.awt.Toolkit;
import java.io.LineNumberReader;
import java.io.StringReader;
import javaoceanatlas.ui.widgets.MergeEditorCell;
import javaoceanatlas.utility.JOAFormulas;
import javax.swing.table.DefaultTableModel;

/**
 * <pre>
 * Title:        MergeEditorTableModel
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      NOAA/PMEL/EPIC
 * </pre>
 * 
 * @author oz
 * @version $Revision: 1.1 $, $Date: 2005/10/18 23:42:19 $
 */

@SuppressWarnings("serial")
public class MergeEditorTableModel extends DefaultTableModel {
	private Vector<String> colNames = new Vector<String>();
	private Vector<Vector<MergeEditorCell>> cells = new Vector<Vector<MergeEditorCell>>();
	private int rowCount = 0;
	private int colCount = 0;
	private boolean mTableValuesChanged = false;
	private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	private LineNumberReader in = null;
	private Vector<Vector<String>> values = new Vector<Vector<String>>();

	public MergeEditorTableModel(String[] cn, Vector<String>[] vals, int[] dispPrec) {
		try {
			for (int i = 0; i < cn.length; i++) {
				colNames.add(cn[i]);
				cells.addElement(new Vector<MergeEditorCell>());
				values.addElement(new Vector<String>());
			}
			colCount = colNames.size();

			rowCount = 0;
			for (Vector<String> v : vals) {
				for (int i = 0; i < v.size(); i++) {
					String displayVal = JOAFormulas.formatDouble(v.elementAt(i), dispPrec[i], false);
					cells.elementAt(i).addElement(new MergeEditorCell(displayVal));
					values.elementAt(i).addElement(new String(displayVal));
				}
				rowCount++;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error in line = " + rowCount);
		}
	}

	public MergeEditorTableModel(String[] cn, String contents, int[] dispPrec) {
		try {
			for (int i = 0; i < cn.length; i++) {
				colNames.add(cn[i]);
				cells.addElement(new Vector<MergeEditorCell>());
				values.addElement(new Vector<String>());
			}

			in = new LineNumberReader(new StringReader(contents), 10000);
			// assign the column names
			colCount = colNames.size();

			// fill the table
			rowCount = 0;
			while (true) {
				String inLine;

				// get nth line
				inLine = in.readLine();

				if (inLine == null) {
					break;
				}

				if (inLine.indexOf("header") >= 0 || inLine.indexOf("units") >= 0) {
					continue;
				}

				// break by : if necessary
				try {
					String delimitedVals = null;
					if (inLine.indexOf(":") >= 0) {
						String[] tokens = inLine.split("[:]");
						delimitedVals = tokens[1];
					}
					else {
						delimitedVals = inLine;
					}

					String[] tokens2 = delimitedVals.split("[|]");

					for (int t = 0; t < tokens2.length; t++) {
						try {
							String displayVal = JOAFormulas.formatDouble(tokens2[t], dispPrec[t], false);
							cells.elementAt(t).addElement(new MergeEditorCell(displayVal));
							values.elementAt(t).addElement(new String(displayVal));
						}
						catch (Exception ex) {
							cells.elementAt(t).addElement(new MergeEditorCell(tokens2[t]));
							values.elementAt(t).addElement(new String(tokens2[t]));
						}
					}
				}
				catch (Exception ex) {
		      JFrame f = new JFrame("Parse Error");
		      Toolkit.getDefaultToolkit().beep();
		      JOptionPane.showMessageDialog(f, "JOA encountered an error parsing the column headers." + "\n" +
		      		" (" + ex.getCause() + " " + ex.getMessage() + ")" + "\n" +
		      		"Error in line = " + rowCount + " " + inLine);
				}
				rowCount++;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setColumnHeader(int idx, String text) {
		colNames.setElementAt(text, idx);
	}

	public Object getValAt(int rowIndex) {
		return null;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return colCount;
	}

	public String getColumnName(int columnIndex) {
		return colNames.elementAt(columnIndex);
	}

	@SuppressWarnings("unchecked")
	public Class getColumnClass(int columnIndex) {
		return colNames.elementAt(columnIndex).getClass();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return cells.elementAt(columnIndex).elementAt(rowIndex);
	}

	public boolean isTableChanged() {
		return mTableValuesChanged;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		fireTableModelChange(rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE);
	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	private void fireTableModelChange(int firstRow, int lastRow, int column, int type) {
		TableModelEvent tme = new TableModelEvent(this, firstRow, lastRow, column, type);
		Enumeration<TableModelListener> iter = listeners.elements();
		while (iter.hasMoreElements()) {
			(iter.nextElement()).tableChanged(tme);
		}
	}

	public void deleteColumn(int c) {
		// take the data for this column out of the data model
		cells.remove(c);

		/**
		 * @todo remove column header and update display
		 */
		colCount--;

	}

	/*
	 * public void addColumn(int afterColumn, String colHeader, String fillValue,
	 * TableColumnModel tcm) { Vector<MergeEditorCell> newCol = new Vector<MergeEditorCell>();
	 * 
	 * tcm.addColumn(new TableColumn(afterColumn, 75, new
	 * MergeEditorCellRenderer(true), new MergeEditorCellEditor(new
	 * JTextField()))); // intialize cell values colNames.add(afterColumn,
	 * colHeader); colCount++;
	 * 
	 * for (int i=0; i<rowCount; i++) { newCol.add(new
	 * MergeEditorCell(fillValue)); } // insert into the model
	 * cells.add(afterColumn, newCol); }
	 */
}

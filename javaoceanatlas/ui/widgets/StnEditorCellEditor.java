/* * $Id: StnEditorCellEditor.java,v 1.1 2005/10/18 23:42:19 oz Exp $ * */package javaoceanatlas.ui.widgets;import javax.swing.JTable;import javax.swing.DefaultCellEditor;import java.awt.Toolkit;import java.awt.Component;import javax.swing.JTextField;import javax.swing.JOptionPane;import javax.swing.JComponent;import javax.swing.JFrame;@SuppressWarnings("serial")public class StnEditorCellEditor extends DefaultCellEditor {  private StnEditorCell mCell;  private JComponent mComponent = new JTextField();  public StnEditorCellEditor(JTextField fld) {    super(fld);  }  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {    mCell = (StnEditorCell)value;    ((JTextField)mComponent).setText(mCell.toString());    return mComponent;  }  public Object getCellEditorValue() {    String result = ((JTextField)mComponent).getText();    try {      float newVal = Float.valueOf(result).floatValue();      mCell.setNewValue(newVal);    }    catch (NumberFormatException ex) {      Toolkit.getDefaultToolkit().beep();      JOptionPane.showMessageDialog(new JFrame(), "Invalid Number");    }    return result;  }}
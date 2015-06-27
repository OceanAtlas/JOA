
package javaoceanatlas.ui.widgets;

import javax.swing.JTable;
import javax.swing.DefaultCellEditor;
import java.awt.Toolkit;
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MergeEditorCellEditor extends DefaultCellEditor {
  private MergeEditorCell mCell;
  private JComponent mComponent = new JTextField();

  public MergeEditorCellEditor(JTextField fld) {
    super(fld);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    mCell = (MergeEditorCell)value;
    mCell.getValue();
    ((JTextField)mComponent).setText(mCell.toString());
    return mComponent;
  }

  public Object getCellEditorValue() {
    String result = ((JTextField)mComponent).getText();

    try {
      mCell.setNewValue(result);
    }
    catch (NumberFormatException ex) {
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(new JFrame(), "Invalid Number");
    }
    return result;
  }
}

package javaoceanatlas.ui.widgets;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

@SuppressWarnings("serial")
public class MergeEditorCellRenderer extends JLabel implements TableCellRenderer {
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
  Font mCellFont = new Font("sansserif", Font.PLAIN, 9);

	public MergeEditorCellRenderer(boolean isBordered) {
		super("", JLabel.RIGHT);
		this.isBordered = isBordered;
	}

	public Component getTableCellRendererComponent(JTable table, Object cell, boolean isSelected, boolean hasFocus,
	    int row, int column) {

		if (cell instanceof MergeEditorCell) {
			MergeEditorCell newCell = (MergeEditorCell) cell;

			this.setOpaque(true);
			// set the JLabel text
			this.setText(newCell.getValue());
	    this.setFont(mCellFont);

			if (isSelected) {
				setBackground(Color.lightGray);
			}
			else {
				if (row % 2 == 0) {
					setBackground(new Color(235, 235, 255));
				}
				else {
					setBackground(Color.white);
				}
			}

			// setToolTipText("QC Value: " + getQCString(std, qc));
		}
		return this;
	}
}

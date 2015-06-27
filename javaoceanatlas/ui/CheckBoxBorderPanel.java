/*
 * $Id: CheckBoxBorderPanel.java,v 1.2 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.widgets.*;

/**
 * A JPanel which has a checkbox embedded in its border. The contents of the
 * panel are automatically insensitized when the checkbox is turned off.
 */
@SuppressWarnings("serial")
public class CheckBoxBorderPanel extends JPanel implements ItemListener {
	public CheckBoxBorderPanel(String title) {
		this(title, null, null, true);
	}
	
	public CheckBoxBorderPanel(String title, boolean hookupListener) {
		this(title, null, null, hookupListener);
	}

	public CheckBoxBorderPanel(String title, Border border) {
		this(title, border, null, true);
	}

	public CheckBoxBorderPanel(String title, LayoutManager layout) {
		this(title, null, layout, true);
	}

	public CheckBoxBorderPanel(String title, Border border, LayoutManager layout, boolean hookupListener) {
		if (layout == null)
			layout = new FlowLayout();
		if (border == null)
			border = BorderFactory.createEtchedBorder();

		checkbox = new JOAJCheckBox(title);
    checkbox.setOpaque(true);
		setLayout(layout);
		setBorder(border);
		add(checkbox, 0);
		if (hookupListener) {
			checkbox.addItemListener(this);
		}
	}

	public void doLayout() {
		setEnabled();
		checkbox.setVisible(false);
		super.doLayout();
		checkbox.setVisible(true);
		Dimension size = checkbox.getPreferredSize();
		checkbox.setSize(size);
		checkbox.setLocation(10, 0);
	}

	public void itemStateChanged(ItemEvent e) {
		setEnabled();
	}

	private void setEnabled() {
		boolean set = checkbox.isSelected();
		Component[] children = getComponents();
		for (int i = 0; i < children.length; i++)
			if (children[i] != checkbox)
				children[i].setEnabled(set);
	}

	public void setBorder(Border border) {
		super.setBorder(border == null ? null : new CBBorder(border));
	}

	public JOAJCheckBox getCheckBox() {
		return checkbox;
	}

	private JOAJCheckBox checkbox;

	private class CBBorder implements Border {
		CBBorder(Border child) {
			this.child = child;
		}

		public Insets getBorderInsets(Component c) {
			Insets result = (Insets) child.getBorderInsets(c).clone();
			result.top = checkbox.getPreferredSize().height;
			return result;
		}

		public boolean isBorderOpaque() {
			return false;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			int cHeight = checkbox.getPreferredSize().height / 2;
			y += cHeight;
			height -= cHeight;
			child.paintBorder(c, g, x, y, width, height);
		}

		private Border child;
	}

	public static void main(String[] argv) {
		JFrame frame = new JFrame();
		CheckBoxBorderPanel p = new CheckBoxBorderPanel("test");
		p.add(new JButton("Test"));
		frame.setContentPane(p);
		frame.setSize(200, 200);
		frame.setVisible(true);
	}
}

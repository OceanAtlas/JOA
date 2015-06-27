/*
 * $Id: RadioButtonBorderPanel.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A JPanel which has a checkbox embedded in its border. The contents of the panel
 * are automatically insensitized when the checkbox is turned off.
 */
@SuppressWarnings("serial")
public class RadioButtonBorderPanel extends JPanel implements ItemListener {
  public RadioButtonBorderPanel(String title) {
    this(title, null, null);
  }

  public RadioButtonBorderPanel(String title, Border border) {
    this(title, border, null);
  }

  public RadioButtonBorderPanel(String title, LayoutManager layout) {
    this(title, null, layout);
  }

  public RadioButtonBorderPanel(String title, Border border, LayoutManager layout) {
    if (layout == null) {
      layout = new FlowLayout();
    }
    if (border == null) {
      border = BorderFactory.createEtchedBorder();
    }

    rb = new JOAJRadioButton(title);
    rb.setOpaque(true);
    setLayout(layout);
    setBorder(border);
    add(rb, 0);
    rb.addItemListener(this);
  }

  public void doLayout() {
    setEnabled();
    rb.setVisible(false);
    super.doLayout();
    rb.setVisible(true);
    Dimension size = rb.getPreferredSize();
    rb.setSize(size);
    rb.setLocation(10, 0);
  }

  public void itemStateChanged(ItemEvent e) {
    setEnabled();
  }

  private void setEnabled() {
    boolean set = rb.isSelected();
    Component[] children = getComponents();
    for (int i = 0; i < children.length; i++) {
      if (children[i] != rb) {
        children[i].setEnabled(set);
      }
    }
  }

  public void setBorder(Border border) {
    super.setBorder(border == null ? null : new CBBorder(border));
  }

  public JRadioButton getRadioButton() {
    return rb;
  }

  private JRadioButton rb;

  private class CBBorder implements Border {
    CBBorder(Border child) {
      this.child = child;
    }

    public Insets getBorderInsets(Component c) {
      Insets result = (Insets)child.getBorderInsets(c).clone();
      result.top = rb.getPreferredSize().height;
      return result;
    }

    public boolean isBorderOpaque() {
      return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      int cHeight = rb.getPreferredSize().height / 2;
      y += cHeight;
      height -= cHeight;
      child.paintBorder(c, g, x, y, width, height);
    }

    private Border child;
  }

  public static void main(String[] argv) {
    JFrame frame = new JFrame();
    RadioButtonBorderPanel p = new RadioButtonBorderPanel("test");
    p.add(new JButton("Test"));
    frame.setContentPane(p);
    frame.setSize(200, 200);
    frame.setVisible(true);
  }
}

/*
 * $Id: SectionToolWidthTextField.java,v 1.7 2005/09/07 18:38:39 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.beans.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import gov.noaa.pmel.text.*;
import gov.noaa.pmel.swing.*;

 /**
 * @author  oz 
 * @version 1.0 01/09/01
 */
public class SectionToolWidthTextField extends JTextField implements ActionListener, PropertyChangeListener,
			 FocusListener {

	int lastX, lastY;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	String toolTip = "Section tool width in KM";
	private boolean TRACE = false;
	double width;
	double prevWidth;
	Dimension sz = new Dimension(68,18);

   //
	public SectionToolWidthTextField() {
		super();
		this.setPreferredSize(sz);
		this.setMaximumSize(sz);
		this.setSize(sz);
		boolean windows = false;
		Font tff = BeanFonts.textFieldFont;
		if (-1 != System.getProperty("os.name").indexOf("Windows")) windows = true;
		if (windows) tff = new Font("SansSerif", Font.PLAIN, 12);
		
		this.setFont(tff);
		this.setSectionWidth(Constants.SECTION_WIDTH);
		this.setToolTipText(toolTip);
		this.addKeyListener(new KeyListener());
		this.addFocusListener(this);
		//this.setBackground(new java.awt.Color(200,200,200));
	}

   //
	public SectionToolWidthTextField(ViewManager vm, Object parentObject, String toolTip) {
		this();
		this.toolTip = toolTip;
		this.addActionListener(this);
		this.toolTip = toolTip;
		this.setToolTipText(toolTip);
	}
	
	private class KeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent ev) {
			int key = ev.getKeyCode();
			
			if (key == KeyEvent.VK_ENTER) {
				try {
					String s = getText();
					double f = Float.valueOf(s).floatValue();
					prevWidth = width;
					width = f;
					fireWidthChange();
					ev.consume();
					select(0, 43);
				}
				catch (NumberFormatException ex) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
			else {
				super.keyPressed(ev);
			}
		}
	}

   //
   public void setSectionWidth(double width) {
      this.setText(String.valueOf(width));
      this.width = width;
      this.prevWidth = width;
   }

   public double getSectionWidth() {
      return this.width;
   }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this) {
			String st = String.valueOf(prevWidth);
			if (this.getText().equals(st)) {
				// user didn't enter any new text.
			}
			else {
				try {
					String s = this.getText();
					// user entered new text by hand
					double f = Float.valueOf(s).floatValue();
					this.prevWidth = this.width;
					this.width = f;
					fireWidthChange();
				}
				catch (NumberFormatException ex) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}
	}

	// Fires both a property change (for anyone wanting to wire up to this object) 
	public void fireWidthChange() {
		pcs.firePropertyChange("sectionwidth", (new Float(this.prevWidth)), (new Float(this.width)));
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("sectionwidth")) {
			this.setText((String) e.getNewValue());
			try {
				float f = Float.valueOf((String)e.getNewValue()).floatValue();
				this.prevWidth = this.width;
				this.width = f;
				fireWidthChange();
			}
			catch (NumberFormatException ex) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
   
   public void focusGained(FocusEvent fe) {
		this.selectAll();
	}
	
	public void focusLost(FocusEvent fe) {
		String st = String.valueOf(prevWidth);
		if (this.getText().equals(st)) {
			// user didn't enter any new text.
		}
		else {
			String s = this.getText();
			try {
				float f = Float.valueOf(s).floatValue();
				this.prevWidth = this.width;
				this.width = f;
				fireWidthChange();
			}
			catch (NumberFormatException ex) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

   //--------------------------------------------------------------------------
   // 
   public void addPropertyChangeListener(PropertyChangeListener l) {
      if (pcs == null) {
         super.addPropertyChangeListener(l);
      }
      else {
         pcs.addPropertyChangeListener(l);
      }
   }

   //--------------------------------------------------------------------------
   // 
   public void removePropertyChangeListener(PropertyChangeListener l) {
      pcs.removePropertyChangeListener(l);
   }
}

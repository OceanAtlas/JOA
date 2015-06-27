/*
 * $Id: DepthTextField.java,v 1.8 2005/02/15 18:31:08 oz Exp $
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
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class DepthTextField extends JTextField implements ActionListener, 
		PropertyChangeListener, ChangeableInfoListener, FocusListener {
	JMenuItem miDate;
	int lastX, lastY;
	JDepthDialog depthDialog;
	Dimension sz = new Dimension(68,18);
	double depth, prevDepth;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	String changeable;   // string describing changeable this field is tracking
	String toolTip = "Depth";
	private boolean TRACE = false;
	DecimalFormat depthDM = new DecimalFormat();
	private Object parentObject;
	CIMFacade rootObject;
	String formatString = "###.###;###.###";
	int mWhichAxis;
	ViewManager mViewManager;

   public DepthTextField() {
      super();

      createFormatter();
	  this.addMouseListener(new MyMouseHandler());
      this.addKeyListener(new KeyListener());
      this.addFocusListener(this);
      this.setPreferredSize(sz);
      this.setMaximumSize(sz);
      this.setSize(sz);
      this.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	  //this.setBackground(new java.awt.Color(200,200,200));
      //this.setForeground(new java.awt.Color(75,75,75));
      //
      // Set font according to platform; use defaults in class BeanFonts unless
      //  running on Windows.
      //
      boolean windows = false;
      Font tff = BeanFonts.textFieldFont;
      if (-1 != System.getProperty("os.name").indexOf("Windows")) windows = true;
      if (windows) tff = new Font("SansSerif", Font.PLAIN, 12);

      this.setFont(tff);
      this.setDepth(0.0f);
      this.setToolTipText(toolTip);
   }

	public DepthTextField(ViewManager vm, Object parentObject, String changeable, String toolTip, int whichAxis) {
		this();
		mViewManager = vm;
		this.setParent(parentObject);
		this.changeable = changeable;
		this.toolTip = toolTip;
		createFormatter();
		this.addActionListener(this);
		rootObject = (CIMFacade)parentObject;
		if (rootObject != null) 
			rootObject.addChangeableInfoListener(this);
		this.toolTip = toolTip;
		this.setToolTipText(toolTip);
		mWhichAxis = whichAxis;
	    //this.setBackground(new java.awt.Color(200,200,200));
	}
	
	private class KeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent ev) {
			int key = ev.getKeyCode();
			
			if (key == KeyEvent.VK_ENTER) {
				selectAll();
				String st = depthDM.format(prevDepth);
				if (getText().equals(st)) {
					// user didn't enter any new text.
				}
				else {
					String s = getText();
					// user entered new text by hand
					double f;
					try {
						f = depthDM.parse(s).floatValue();
					} 
					catch (Exception ex) {
						f = 0;
						String errmsg = new String("Cannot parse depth value: " + getText());
						JOptionPane.showMessageDialog(null, errmsg,"ERROR", JOptionPane.ERROR_MESSAGE);
					}
					prevDepth = depth;
					depth = f;
					fireDepthChange();
				}
				ev.consume();
				select(0, 43);
			}
			else {
				super.keyPressed(ev);
			}
		}
	}

   public String getChangeable() {
      return changeable;
   }

   public void setDepth(double depth) {
      this.setText(depthDM.format(depth));
      this.depth = depth;
      this.prevDepth = depth;
   }

   public void setValue(double depth) {
      this.depth = depth;
      this.prevDepth = depth;
   }

   public double getDepth() {
      return this.depth;
   }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this) {
			String st = depthDM.format(prevDepth);
			if (this.getText().equals(st)) {
				// user didn't enter any new text.
			}
			else {
				String s = this.getText();
				// user entered new text by hand
				double f;
				try {
					f = depthDM.parse(s).floatValue();
				} catch (Exception ex) {
					f = 0;
					String errmsg = new String("Cannot parse depth value: " + this.getText());
					JOptionPane.showMessageDialog(null, errmsg,"ERROR", JOptionPane.ERROR_MESSAGE);
				}
				this.prevDepth = this.depth;
				this.depth = f;
				fireDepthChange();
			}
		}
	}

   // Fires both a property change (for anyone wanting to wire up to this object) 
   // and a conceptual change via the changeable info for objects not wanting
   // a hard wire connect to this object.
   //
	public void fireDepthChange() {
		String axis;
		if (mWhichAxis == Constants.X_AXIS)
			axis = "X";
		else
			axis = "Y";
		if (changeable.indexOf("Start") >= 0)
			pcs.firePropertyChange("zminsel" + axis, (new Double(this.prevDepth)), (new Double(this.depth)));
		else
			pcs.firePropertyChange("zmaxsel"  +axis, (new Double(this.prevDepth)), (new Double(this.depth)));
		
		if (rootObject != null) 
			rootObject.pushChangeableInfo(changeable, (new Double(this.prevDepth)), (new Double(this.depth)), true);
		try {
			prevDepth = depthDM.parse(this.getText()).floatValue();
		} 
		catch (Exception ex) {
			prevDepth = 0;
			String errmsg = new String("Cannot parse depth value: " + this.getText());
			JOptionPane.showMessageDialog(null, errmsg,"ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

   public void createFormatter() {
      depthDM = new DecimalFormat(formatString);
   }

   public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("FormattedDateTime")) {
         this.setText((String) e.getNewValue());

	 this.prevDepth = this.depth;
         try {
	    this.depth = depthDM.parse((String) e.getNewValue()).floatValue();
         } catch (Exception ex) {
            String errmsg = new String("Cannot parse depth value: " + 
		(String) e.getNewValue());
            JOptionPane.showMessageDialog(null, errmsg,"ERROR",
      		JOptionPane.ERROR_MESSAGE);
         }
         //this.getParent().invalidate();
         //this.getParent().validate();
         fireDepthChange();
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

   //--------------------------------------------------------------------------
   // 
   public void setParent(Object parentObject) {
      this.parentObject = parentObject;
   }

   //-----------------------------------------------------------------------
   //
   public Object getAncestor() {
      if (parentObject != null) {
         return ((Lineage)parentObject).getAncestor();
      }
      return this;
   }

	public void popChangeableInfo(ChangeableInfo ci) {
		if (TRACE)
			System.out.println("DepthTextField: popChangeableInfo entered");
	
		if (ci.getId().equals(changeable)) {
			double f = ((Double) ci.getNewValue()).floatValue();
			this.prevDepth = depth;
			this.depth = f;
			this.setText(depthDM.format(this.depth)); 
		}
	}

	private void handlePopupTrigger(java.awt.event.MouseEvent event) {
		mViewManager.setFocusView((CutPanelView)parentObject);
		depthDialog = new JDepthDialog((float)this.getDepth(), formatString);
		double f = depthDialog.getDepth();
		this.prevDepth = this.depth;
		this.depth = f;
		String ss = depthDM.format(this.depth);
		this.setText(depthDM.format(this.depth));
		fireDepthChange();
	}
			
	public class MyMouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent me) {
			if (me.getClickCount() == 2) {
				// show configuration dialog
				handlePopupTrigger(me);
			}
		}
			
		public void mouseReleased(MouseEvent me) {
			super.mouseReleased(me);
			if (me.isPopupTrigger()) {
				handlePopupTrigger(me);
			}
		}
			
		public void mousePressed(MouseEvent me) {
			super.mousePressed(me);
			if (me.isPopupTrigger()) {
				handlePopupTrigger(me);
			}
		}
	}
	
	public void focusGained(FocusEvent fe) {
		this.selectAll();
		mViewManager.setFocusView((CutPanelView)parentObject);
	}
	
	public void focusLost(FocusEvent fe) {
		String st = depthDM.format(prevDepth);
		if (this.getText().equals(st)) {
			// user didn't enter any new text.
		}
		else {
			String s = this.getText();
			// user entered new text by hand
			double f;
			try {
				f = depthDM.parse(s).floatValue();
			} catch (Exception ex) {
				f = 0;
				String errmsg = new String("Cannot parse depth value: " + this.getText());
				JOptionPane.showMessageDialog(null, errmsg,"ERROR", JOptionPane.ERROR_MESSAGE);
			}
			this.prevDepth = this.depth;
			this.depth = f;
			fireDepthChange();
		}
	}
	
	public int getAxis() {
		return mWhichAxis;
	}

   static public void main(String args[]) {
      JFrame fr = new JFrame();
      fr.setSize(918,570);
      fr.setVisible(true);

      DepthTextField depthTextField = new DepthTextField();

      javax.swing.Box bx = javax.swing.Box.createVerticalBox();
      bx.add(Box.createVerticalGlue());
      bx.add(new JPanel());
      bx.add(depthTextField);
      bx.add(new JPanel());
      bx.add(Box.createVerticalGlue());
     // bx.setBackground(Color.red);

      fr.getContentPane().add(bx);
      depthTextField.validate(); depthTextField.invalidate();
      fr.invalidate();
      fr.validate();
      depthTextField.setVisible(true);
   }
}

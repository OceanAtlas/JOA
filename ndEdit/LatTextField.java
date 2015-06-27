/*
 * $Id: LatTextField.java,v 1.8 2005/02/15 18:31:09 oz Exp $
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
import javax.swing.event.*;
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
public class LatTextField extends JTextField implements ActionListener, 
	PropertyChangeListener, ChangeableInfoListener, FocusListener {

   JMenuItem miDate;
   int lastX, lastY;
   JLatSpinDialog spinner;
   JLatDialog latDialog;
   Dimension sz = new Dimension(68,18);
   double lat, prevLat;
   private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
   String changeable;   // string describing changeable this field is tracking
   String toolTip = "Latitude";
   private boolean TRACE = true;
   LatitudeFormat latDM;
   int geoDisplayFormat = Constants.DEC_MINUTES_GEO_DISPLAY;
   private Object parentObject;
   CIMFacade rootObject;
   String formatString = "###.###N;###.###S";
	int mWhichAxis;
	ViewManager mViewManager;

   //
	public LatTextField() {
		super();
		
		createFormatter(geoDisplayFormat);
		//this.addActionListener(this);
		this.addMouseListener(new MyMouseHandler());
    	this.addKeyListener(new KeyListener());
    	this.addFocusListener(this);
		this.setPreferredSize(sz);
		this.setMaximumSize(sz);
		this.setSize(sz);
		this.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		//this.setForeground(new java.awt.Color(75,75,75));
	    //this.setBackground(new java.awt.Color(200,200,200));
		//
		// Set font according to platform; use defaults in class BeanFonts unless
		//  running on Windows.
		//
		boolean windows = false;
		Font tff = BeanFonts.textFieldFont;
		if (-1 != System.getProperty("os.name").indexOf("Windows")) windows = true;
		if (windows) tff = new Font("SansSerif", Font.PLAIN, 12);
		
		this.setFont(tff);
		this.setLat(0.0f);
		this.setToolTipText(toolTip);
	}
	
	private class KeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent ev) {
			int key = ev.getKeyCode();
			
			if (key == KeyEvent.VK_ENTER) {
				selectAll();
				String st = latDM.format((float)prevLat);
				if (getText().equals(st)) {
					// user didn't enter any new text.
				}
				else {
					String s = getText();
					// user entered new text by hand
					double f = latDM.parse(s);
					prevLat = lat;
					lat = f;
					fireLatChange();
				}
				ev.consume();
				select(0, 43);
			}
			else {
				super.keyPressed(ev);
			}
		}
	}

	public LatTextField(ViewManager vm, Object parentObject, String changeable, String toolTip, int geoDisplayFormat, int whichAxis) {
		this();
		mViewManager = vm;
		this.setParent(parentObject);
		this.changeable = changeable;
		this.toolTip = toolTip;
		this.geoDisplayFormat = geoDisplayFormat;
		mWhichAxis = whichAxis;
		createFormatter(geoDisplayFormat);
		this.addActionListener(this);
		rootObject = (CIMFacade)parentObject;
		if (rootObject != null) 
			rootObject.addChangeableInfoListener(this);
		this.toolTip = toolTip;
		this.setToolTipText(toolTip);
	    //this.setBackground(new java.awt.Color(200,200,200));
	}

   //
   public String getChangeable() {
      return changeable;
   }

   //
   public void setLat(double lat) {
   	if (lat != prevLat) {
      this.setText(latDM.format((float)lat));
      this.lat = lat;
      this.prevLat = lat;
     }
   }
   
   public void setValue(double lat) {
   	if (lat != prevLat) {
      this.lat = lat;
      this.prevLat = lat;
     }
   }
   //
   public double getLat() {
      return this.lat;
   }


   //
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this) {
			String st = latDM.format((float)prevLat);
			if (this.getText().equals(st)) {
				// user didn't enter any new text.
			}
			else {
				String s = this.getText();
				// user entered new text by hand
				double f = latDM.parse(s);
				this.prevLat = this.lat;
				this.lat = f;
				fireLatChange();
			}
		}
	}
   //
   // Fires both a property change (for anyone wanting to wire up to this object) 
   // and a conceptual change via the changeable info for objects not wanting
   // a hard wire connect to this object.
   //
	public void fireLatChange() {
		String axis;
		if (mWhichAxis == Constants.X_AXIS)
			axis = "X";
		else
			axis = "Y";
		if (changeable.indexOf("Start") >= 0)
			pcs.firePropertyChange("latminsel" + axis, (new Double(this.prevLat)), (new Double(this.lat)));
		else
			pcs.firePropertyChange("latmaxsel" + axis, (new Double(this.prevLat)), (new Double(this.lat)));
		
		if (rootObject != null) 
			rootObject.pushChangeableInfo(changeable, (new Double(this.prevLat)), (new Double(this.lat)), true);
		prevLat = latDM.parse(this.getText());
	}

   public void createFormatter(int geoDisplayFormat) {
      if (geoDisplayFormat == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
		latDM = new LatitudeFormat();
      }
      else {
		latDM = new LatitudeFormat(formatString);
      }
   }

   //
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("FormattedDateTime")) {
			this.setText((String) e.getNewValue());
			
			this.prevLat = this.lat;
			this.lat = latDM.parse((String) e.getNewValue());
			//this.getParent().invalidate();
			//this.getParent().validate();
			fireLatChange();
		}
		else if (e.getPropertyName().equals("DateTime")) {
			getParent();
		}
		else if (e.getPropertyName().equals("GeoDisplayFormat")) {
			createFormatter(((Integer) e.getNewValue()).intValue());
			this.setText(latDM.format((float)this.lat)); 
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

   // ---------------------------------------------------------------------
   //
	public void popChangeableInfo(ChangeableInfo ci) {
		//if (TRACE) System.out.println("LatTextField: popChangeableInfo entered");
		if (ci.getId().equals(changeable)) {
			double f = ((Double) ci.getNewValue()).floatValue();
			this.prevLat = lat;
			this.lat = f;
			this.setText(latDM.format((float)this.lat)); 
		}
	}
   
   public void setGeoDisplayFormat(int geoDisplayFormat) {
      this.geoDisplayFormat = geoDisplayFormat;
      createFormatter(geoDisplayFormat);
      this.setText(latDM.format((float)this.lat)); 
   }

	private void handlePopupTrigger(java.awt.event.MouseEvent event) {
		mViewManager.setFocusView((CutPanelView)parentObject);
		if (geoDisplayFormat == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			lastX = this.getX() + this.getSize().width + 30;
			lastY = this.getY();
			int x = event.getComponent().getLocationOnScreen().x + event.getComponent().getSize().width;
			int y = event.getComponent().getLocationOnScreen().y + event.getComponent().getSize().height;
			//dateTimeGetter.setLocation(lastX, lastY);
			spinner = new JLatSpinDialog((float)this.getLat());
			double f = spinner.getLatitude();
			this.prevLat = this.lat;
			this.lat = f;
			String ss = latDM.format((float)this.lat);
			this.setText(latDM.format((float)this.lat));
			fireLatChange();
		}
		else {
			latDialog = new JLatDialog((float)this.getLat(), formatString);
			double f = latDialog.getLatitude();
			this.prevLat = this.lat;
			this.lat = f;
			String ss = latDM.format((float)this.lat);
			this.setText(latDM.format((float)this.lat));
			fireLatChange();
		}
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
		String st = latDM.format((float)prevLat);
		if (this.getText().equals(st)) {
			// user didn't enter any new text.
		}
		else {
			String s = this.getText();
			// user entered new text by hand
			double f = latDM.parse(s);
			this.prevLat = this.lat;
			this.lat = f;
			fireLatChange();
		}
	}
	
	public int getAxis() {
		return mWhichAxis;
	}

   // ---------------------------------------------------------------------
   //
   static public void main(String args[]) {
      JFrame fr = new JFrame();
      fr.setSize(918,570);
      fr.setVisible(true);

      LatTextField latTextField = new LatTextField();

      javax.swing.Box bx = javax.swing.Box.createVerticalBox();
      bx.add(Box.createVerticalGlue());
      bx.add(new JPanel());
      bx.add(latTextField);
      bx.add(new JPanel());
      bx.add(Box.createVerticalGlue());
      //bx.setBackground(Color.red);

      fr.getContentPane().add(bx);
      latTextField.validate(); latTextField.invalidate();
      fr.invalidate();
      fr.validate();
      latTextField.setVisible(true);
   }
}

/*
 * $Id: LonTextField.java,v 1.8 2005/02/15 18:31:09 oz Exp $
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
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class LonTextField extends JTextField implements ActionListener, 
	PropertyChangeListener, Lineage, ChangeableInfoListener, FocusListener {

	protected JMenuItem miDate;
	protected int lastX, lastY;
	protected JLonSpinDialog spinner;
	protected JLonDialog lonDialog;
	protected Dimension sz = new Dimension(68,18);
	protected double lon, prevLon;
	protected double corrlon, corrprevLon;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected String changeable;   // string describing changeable this field is tracking
	protected String toolTip = "Longitude";
	private boolean TRACE = false;
	protected DecimalFormat dfLon = new DecimalFormat();
	protected LongitudeFormat lonDM;
	protected int geoDisplayFormat = Constants.DEC_MINUTES_GEO_DISPLAY;
	private Object parentObject;
	protected CIMFacade rootObject;
	protected String formatString = "###.###E;###.###W";
	protected int mWhichAxis;
	protected ViewManager mViewManager;

   //
   public LonTextField() {
      super();

      createFormatter(geoDisplayFormat);
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
      this.setLon(0.0f);
      this.setToolTipText(toolTip);
   }

   //
	public LonTextField(ViewManager vm, Object parentObject, String changeable, String toolTip, int geoDisplayFormat, int whichAxis) {
		this();
		mViewManager = vm;
		//this.setParent(parentObject);
		this.parentObject = parentObject;
		this.changeable = changeable;
		this.toolTip = toolTip;
		this.geoDisplayFormat = geoDisplayFormat;
		mWhichAxis = whichAxis;
		createFormatter(geoDisplayFormat);
		this.addActionListener(this);
		rootObject = (CIMFacade)getAncestor();
		if (rootObject != null) 
			rootObject.addChangeableInfoListener(this);
		this.toolTip = toolTip;
		this.setToolTipText(toolTip);
	    //this.setBackground(new java.awt.Color(200,200,200));
	}
	
	private class KeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent ev) {
			int key = ev.getKeyCode();
			
			if (key == KeyEvent.VK_ENTER) {
				selectAll();
				String st = lonDM.format((float)correctLongitude(prevLon));
				if (getText().equals(st)) {
					// user didn't enter any new text.
				}
				else {
					String s = getText();
					// user entered new text by hand
					double f;
					if (s.indexOf('E') > 0 && geoDisplayFormat != Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
						f = Float.valueOf(s.substring(0, s.length()-1)).floatValue();
					}
					else {
						f = lonDM.parse(s);
					}
					f = unCorrectLongitude(f);
					prevLon = lon;
					lon = f;
					fireLonChange();
				}
				ev.consume();
				select(0, 43);
			}
			else {
				super.keyPressed(ev);
			}
		}
	}

   //
   public String getChangeable() {
      return changeable;
   }

   //
   public void setLon(double lon) {
      this.setText(lonDM.format((float)correctLongitude(lon)));
      this.lon = lon;
      this.prevLon = lon;
   }
   
   public void setValue(double lon) {
      this.lon = lon;
      this.prevLon = lon;
   }

   public double getLon() {
      return this.lon;
   }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this) {
			String st = lonDM.format((float)correctLongitude(prevLon));
			if (this.getText().equals(st)) {
				// user didn't enter any new text.
			}
			else {
				String s = this.getText();
				// user entered new text by hand
				double f;
				if (s.indexOf('E') > 0 && geoDisplayFormat != Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
					f = Float.valueOf(s.substring(0, s.length()-1)).floatValue();
				}
				else {
					f = lonDM.parse(s);
				}
				f = unCorrectLongitude(f);
				this.prevLon = this.lon;
				this.lon = f;
				fireLonChange();
			}
		}
	}
   //
   // Fires both a property change (for anyone wanting to wire up to this object) 
   // and a conceptual change via the changeable info for objects not wanting
   // a hard wire connect to this object.
   //
	public void fireLonChange() {
		String axis;
		if (mWhichAxis == Constants.X_AXIS)
			axis = "X";
		else
			axis = "Y";
		if (changeable.indexOf("Start") >= 0)
			pcs.firePropertyChange("lonminsel" + axis, (new Double(this.prevLon)), (new Double(this.lon)));
		else
			pcs.firePropertyChange("lonmaxsel" + axis, (new Double(this.prevLon)), (new Double(this.lon)));
		
		if (rootObject != null) 
			rootObject.pushChangeableInfo(changeable, (new Double(this.prevLon)), (new Double(this.lon)), true);
		prevLon = unCorrectLongitude(lonDM.parse(this.getText()));
	}


	public void createFormatter(int geoDisplayFormat) {
		if (geoDisplayFormat == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			lonDM = new LongitudeFormat();
		}
		else {
			lonDM = new LongitudeFormat(formatString);
		}
	}

   public void propertyChange(PropertyChangeEvent e) {
   		//System.out.println("got PCE = " + e.getPropertyName());
      if (e.getPropertyName().equals("FormattedDateTime")) {
         this.setText((String) e.getNewValue());

	 this.prevLon = this.lon;
	 this.lon = lonDM.parse((String) e.getNewValue());
         //this.getParent().invalidate();
        // this.getParent().validate();
         fireLonChange();
      }
      else if (e.getPropertyName().equals("DateTime")) {
         getParent();
      }
      else if (e.getPropertyName().equals("GeoDisplayFormat")) {
         createFormatter(((Integer) e.getNewValue()).intValue());
      	 this.setText(lonDM.format((float)correctLongitude(this.lon))); 
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
      if (TRACE) 
      	System.out.println("LonTextField: popChangeableInfo entered");

		if (ci.getId().equals(changeable)) {
			double f = ((Double) ci.getNewValue()).floatValue();
			this.prevLon = lon;
			this.lon = f;
			this.setText(lonDM.format((float)correctLongitude(this.lon))); 
		}
	}
   //
   public void setGeoDisplayFormat(int geoDisplayFormat) {
      this.geoDisplayFormat = geoDisplayFormat;
      createFormatter(geoDisplayFormat);
      this.setText(lonDM.format((float)correctLongitude(this.lon))); 
   }
   //
	private void handlePopupTrigger(java.awt.event.MouseEvent event) {
		mViewManager.setFocusView((CutPanelView)parentObject);
		if (geoDisplayFormat == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			//System.out.println("Trigger");
			lastX = this.getX() + this.getSize().width + 30;
			lastY = this.getY();
			int x = event.getComponent().getLocationOnScreen().x + event.getComponent().getSize().width;
			int y = event.getComponent().getLocationOnScreen().y + event.getComponent().getSize().height;
			//dateTimeGetter.setLocation(lastX, lastY);
			spinner = new JLonSpinDialog((float)correctLongitude(this.getLon()));
			double f = unCorrectLongitude(spinner.getLongitude());
			this.prevLon = this.lon;
			this.lon = f;
			this.setText(lonDM.format((float)correctLongitude(this.lon)));
			fireLonChange();
		}
		else {
			lonDialog = new JLonDialog((float)this.getLon(), formatString);
			double f = lonDialog.getLongitude();
			this.prevLon = this.lon;
			this.lon = f;
			this.setText(lonDM.format((float)correctLongitude(this.lon)));
			fireLonChange();
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
		String st = lonDM.format((float)correctLongitude(prevLon));
		if (this.getText().equals(st)) {
			// user didn't enter any new text.
		}
		else {
			String s = this.getText();
			// user entered new text by hand
			double f;
			if (s.indexOf('E') > 0 && geoDisplayFormat != Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
				f = Float.valueOf(s.substring(0, s.length()-1)).floatValue();
			}
			else {
				f = lonDM.parse(s);
			}
			f = unCorrectLongitude(f);
			this.prevLon = this.lon;
			this.lon = f;
			fireLonChange();
		}
	}
	
	public int getAxis() {
		return mWhichAxis;
	}
	
	private double correctLongitude(double lon) {
		if (lon > 180)
			return lon - Constants.LONGITUDE_CONV_FACTOR;
		return lon;
	}
	
	private double unCorrectLongitude(double lon) {
		if (lon < 0)
			return lon + Constants.LONGITUDE_CONV_FACTOR;
		return lon;
	}

   // ---------------------------------------------------------------------
   //
   static public void main(String args[]) {
      JFrame fr = new JFrame();
      fr.setSize(918,570);
      fr.setVisible(true);

      LonTextField lonTextField = new LonTextField();

      javax.swing.Box bx = javax.swing.Box.createVerticalBox();
      bx.add(Box.createVerticalGlue());
      bx.add(new JPanel());
      bx.add(lonTextField);
      bx.add(new JPanel());
      bx.add(Box.createVerticalGlue());
      //bx.setBackground(Color.red);

      fr.getContentPane().add(bx);
      lonTextField.validate(); lonTextField.invalidate();
      fr.invalidate();
      fr.validate();
      lonTextField.setVisible(true);
   }
}
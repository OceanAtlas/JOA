/*
 * $Id: DateTextField.java,v 1.9 2005/02/15 18:31:08 oz Exp $
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
import java.util.Date;
import java.util.TimeZone;
import gov.noaa.pmel.swing.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class DateTextField extends JTextField implements ActionListener, PropertyChangeListener,
	ChangeableInfoListener, FocusListener {

   JDateTimeGetter dateTimeGetter;
   int lastX, lastY;
   Dimension sz = new Dimension(68,18);
   JDateDialog dateDialog;
   Date date, prevDate;
   SimpleDateFormat sdfGMT = new SimpleDateFormat("dd MMM yyyy");
   private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
   String changeable;   // string describing changeable this field is tracking
   String toolTip = "Date";
   int timeDisplayFormat = Constants.DATE_TIME_DISPLAY;
   private boolean TRACE = false;
   private Object parentObject;
   CIMFacade rootObject;
	int mWhichAxis;
	ViewManager mViewManager;

   //
   public DateTextField() {
      super();

      createFormatter(timeDisplayFormat);
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
      this.setDate(new Date());
      this.prevDate = date;
      this.setToolTipText(toolTip);

      // use GMT
      sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
   }

   //
	public DateTextField(ViewManager vm, Object parentObject, String changeable, String toolTip, int timeDisplayFormat, int whichAxis) {
		this();
		mViewManager = vm;
		this.setParent(parentObject);
		this.timeDisplayFormat = timeDisplayFormat;
		createFormatter(timeDisplayFormat);
		mWhichAxis = whichAxis;
		this.changeable = changeable;
		this.toolTip = toolTip;
		rootObject = (CIMFacade)parentObject;
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
				if (getText().equals(sdfGMT.format(prevDate))) {
					// user didn't enter any new text.
				}
				else { // user entered new text by hand
					String s = getText();
					Date d = sdfGMT.parse(s, new ParsePosition(0));
					prevDate = date;
					date = d;
					fireDateChange();
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
   void createJDateTimeGetter() {
      //if (Debug.DEBUG) System.out.println("createJDateTimeGetter with date: " + 
//	sdfGMT.format(this.getDate()));
      dateTimeGetter = new JDateTimeGetter(this.getDate());
      dateTimeGetter.setOutputDateFormatter(sdfGMT);
      dateTimeGetter.setTimeZone(TimeZone.getTimeZone("GMT"));
      dateTimeGetter.setHideTime(true);
      dateTimeGetter.addPropertyChangeListener(this);
   }

   //
   public void createFormatter(int timeDisplayFormat) {
      if (timeDisplayFormat == Constants.DATE_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
      }
      else if (timeDisplayFormat == Constants.YEAR_DAY_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("yyyy-DDD");
      }
      else if (timeDisplayFormat == Constants.MONTH_TIME_DISPLAY) {
        sdfGMT = new SimpleDateFormat("MMMMMMMMMMMMMMM");
      }
      else if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY) {
        sdfGMT = new SeasonFormatter();
      }
      sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

   //
	public void setDate(Date indate) {
		if (indate != null) {
			if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
				this.setText(new String(sdfGMT.format(indate, null, null)));
			else
				this.setText(sdfGMT.format(indate));
		}
		this.date = indate;
	}
   
   public void setValue(Date indate) {
      this.date = indate;
   }
   
   //
   public Date getDate() {
      return this.date;
   }

   //
	private void handlePopupTrigger(java.awt.event.MouseEvent event) {
		mViewManager.setFocusView((CutPanelView)parentObject);
		if (timeDisplayFormat == Constants.DATE_TIME_DISPLAY) {
			//System.out.println("Trigger");
			lastX = this.getX() + this.getSize().width + 30;
			lastY = this.getY();
			if (dateTimeGetter == null)  {
				createJDateTimeGetter();
			}
			int x = event.getComponent().getLocationOnScreen().x + event.getComponent().getSize().width;
			int y = event.getComponent().getLocationOnScreen().y + event.getComponent().getSize().height;
			dateTimeGetter.setDate(this.getDate());
			dateTimeGetter.showInJFrame(x, y);
			lastX = this.getX() + this.getSize().width + 30;
			lastY = this.getY();
		}
		else {
			dateDialog = new JDateDialog(this.getDate(), sdfGMT);
			Date dt = dateDialog.getDate();
			this.prevDate = date;
			this.setDate(dt);
			fireDateChange();
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

   //
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this) {
			if (this.getText().equals(sdfGMT.format(prevDate))) {
				// user didn't enter any new text.
			}
			else { // user entered new text by hand
				String s = this.getText();
				//System.out.println("New Text: " + s);
				Date d = sdfGMT.parse(s, new ParsePosition(0));
				this.prevDate = this.date;
				this.date = d;
				fireDateChange();
			}
		}
	}
   //
   // Fires both a property change (for anyone wanting to wire up to this object) 
   // and a conceptual change via the changeable info for objects not wanting
   // a hard wire connect to this object.
   //
	public void fireDateChange() {
		String axis;
		if (mWhichAxis == Constants.X_AXIS)
			axis = "X";
		else
			axis = "Y";
		if (changeable.indexOf("Start") >= 0)
			pcs.firePropertyChange("timminsel" + axis, (new Double(this.prevDate.getTime())), (new Double(this.date.getTime())));
		else
			pcs.firePropertyChange("timmaxsel" + axis, (new Double(this.prevDate.getTime())), (new Double(this.date.getTime())));
		rootObject.pushChangeableInfo(changeable, (new Double(this.prevDate.getTime())), (new Double(this.date.getTime())), true);
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd MMM yyyy z");
		sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		prevDate = sdfGMT.parse(this.getText(), new ParsePosition(0));
	}
	
	public void fireDateChangeable() {
		rootObject.pushChangeableInfo(changeable, (new Double(this.prevDate.getTime())), (new Double(this.date.getTime())), true);
	}

   //
   public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("FormattedDateTime")) {
         this.setText((String) e.getNewValue());
	 	this.prevDate = this.date;
	 	this.date = sdfGMT.parse((String) e.getNewValue(), new ParsePosition(0));
        // this.getParent().invalidate();
       //  this.getParent().validate();
         fireDateChange();
      }
      else if (e.getPropertyName().equals("DateTime")) {
         getParent();
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
			System.out.println("DateTextField: popChangeableInfo entered");
		
		if (ci.getId().equals(changeable)) {
			double tm = ((Double) ci.getNewValue()).doubleValue();
			this.prevDate = date;
			this.date = new Date((long)tm);
			if (this.date != null) {
				if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
					this.setText(new String(sdfGMT.format(this.date, null, null)));
				else
					this.setText(sdfGMT.format(this.date));
			}
			if (dateTimeGetter != null) {
				dateTimeGetter.setDate(this.getDate());
		}
		//System.out.println("setting DateTextField text to: " + sdfGMT.format(new Date(tm)));
		}
	}

	public void setTimeDisplayFormat(int timeDisplayFormat) {
		this.timeDisplayFormat = timeDisplayFormat;
		createFormatter(timeDisplayFormat);
		
		if (this.date != null) {
			if (timeDisplayFormat == Constants.SEASON_TIME_DISPLAY)
				this.setText(new String(sdfGMT.format(this.date, null, null)));
			else
				this.setText(sdfGMT.format(this.date));
		}
	}

   public int getTimeDisplayFormat() {
      return timeDisplayFormat;
   }
	
	public void focusGained(FocusEvent fe) {
		this.selectAll();
		mViewManager.setFocusView((CutPanelView)parentObject);
	}
	
	public void focusLost(FocusEvent fe) {
		if (this.getText().equals(sdfGMT.format(prevDate))) {
			// user didn't enter any new text.
		}
		else { // user entered new text by hand
			String s = this.getText();
			Date d = sdfGMT.parse(s, new ParsePosition(0));
			this.prevDate = this.date;
			this.date = d;
			fireDateChange();
		}
	}
	
	public int getAxis() {
		return mWhichAxis;
	}
			

   static public void main(String args[]) {
      JFrame fr = new JFrame();
      fr.setSize(918,570);
      fr.setVisible(true);

      DateTextField dateTextField = new DateTextField();
      dateTextField.setTimeDisplayFormat(Constants.YEAR_DAY_TIME_DISPLAY);

      javax.swing.Box bx = javax.swing.Box.createVerticalBox();
      bx.add(Box.createVerticalGlue());
      bx.add(new JPanel());
      bx.add(dateTextField);
      bx.add(new JPanel());
      bx.add(Box.createVerticalGlue());
      //bx.setBackground(Color.red);

      fr.getContentPane().add(bx);
      dateTextField.validate(); dateTextField.invalidate();
      fr.invalidate();
      fr.validate();
      dateTextField.setVisible(true);
   }
}
// Popup stuff removed from code:
   //JMenuItem miDate;
   //JPopupMenu popupMenu;
/*
  // void createPopupMenu() {
  //    popupMenu = new JPopupMenu();
  //    miDate = new JMenuItem("Date Getter");
  //    miDate.addActionListener(this);
  //    popupMenu.add(miDate);
  // }
*/
//From mouseReleased:
	/*
	//popupMenu.show(event.getComponent(), event.getX(), event.getY());
	//if (popupMenu == null) createPopupMenu();
        //popupMenu.show(event.getComponent(), event.getX(), event.getY());
	*/
//From actionPerformed:
	/*
      //if (e.getSource() == miDate) {
	// if (dateTimeGetter == null)  {
	//   createJDateTimeGetter();
	// }
	// dateTimeGetter.setDate(this.getDate());
        // dateTimeGetter.setLocation(lastX, lastY);
        // dateTimeGetter.setVisible(true);
     // }
	*/

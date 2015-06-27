/*
 * $Id: CutPanel.java,v 1.13 2005/06/17 17:24:16 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */
/*
      A basic implementation of the JFrame class.
*/
package ndEdit;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.beans.*;
import gov.noaa.pmel.swing.*;
import java.io.*;
import java.util.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class CutPanel extends JStretchPanel implements ActionListener, Lineage, MouseListener {
	private boolean TRACE = false;
	private int geoDisplayFormat = Constants.DEC_MINUTES_GEO_DISPLAY;
	private int timeDisplayFormat = Constants.DATE_TIME_DISPLAY;
	private double lonReference = 0.0f;
	private int useCenterWidthOrMinMax = 0;
	protected JPanel toolPanel = new JPanel();
	//protected SmallIconButton closeButn = new SmallIconButton();
	protected JToggleButton closeButn = new JToggleButton();
	protected JPanel labelPanel = new JPanel();
	protected JLabel topLabel = new JLabel();
	protected JLabel bottomLabel = new JLabel();
	protected JPanel textFieldsPanel = new JPanel();
	protected JTextField topLeftText = new JTextField();
	protected JTextField topRightText = new JTextField();
	protected JTextField bottomLeftText = new JTextField();
	protected JTextField bottomRightText = new JTextField();
	protected DateTextField startDate;
	protected DateTextField stopDate;
	protected LatTextField startLat;
	protected LatTextField stopLat;
	protected LonTextField startLon;
	protected LonTextField stopLon;
	protected DepthTextField startDepth;
	protected DepthTextField stopDepth;
	protected JPanel overlayPanel;
	protected CutPanel obj;
	protected JPanel plotPanel = new JPanel();
	//protected SoftBevelBorder loweredSoftBevel = new SoftBevelBorder(BevelBorder.LOWERED, new Color(255, 255, 255), new Color(142, 142, 142));
	protected ViewManager mViewManager;
	protected Object mOverlaySpec;
	
	public CutPanel() {
		super();	

		obj = this;
		
		// Set fonts according to platform; use defaults in class BeanFonts unless running on Windows.
		Font tff = BeanFonts.textFieldFont;
		Font lblf = BeanFonts.labelFont;
		
		boolean windows = false;
		if (-1 != System.getProperty("os.name").indexOf("Windows")) 
			windows = true;
		if (windows) {
			tff = new Font("SansSerif", Font.PLAIN, 10);
			lblf = new Font("Dialog",Font.BOLD, 11);
		}
		
		// text field panel
		textFieldsPanel.setLayout(new GridLayout(2,2,0,0));
		textFieldsPanel.setBounds(123,8,128,29);
		topLeftText.setText("Nov 02 1999");
		topLeftText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		textFieldsPanel.add(topLeftText);
		topLeftText.setFont(tff);
		topLeftText.setBounds(0,0,64,14);
		topRightText.setText("Nov 02 1999");
		topRightText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		textFieldsPanel.add(topRightText);
		topRightText.setFont(tff);
		topRightText.setBounds(64,0,64,14);
		bottomLeftText.setText("0");
		bottomLeftText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		textFieldsPanel.add(bottomLeftText);
		bottomLeftText.setFont(tff);
		bottomLeftText.setBounds(0,14,64,14);
		bottomRightText.setText("3000");
		bottomRightText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		textFieldsPanel.add(bottomRightText);
		bottomRightText.setFont(tff);
		bottomRightText.setBounds(64,14,64,14);
		overlayPanel = getOverlaySetupPanel();
		if (overlayPanel != null) {
			overlayPanel.setBounds(267,3,106,36);
		}
		else {
			overlayPanel = new JPanel();
		}
		//overlayPanel.setBackground(new Color(200, 200, 200));
		
		/*ov1.setBorderPainted(false);
		ov1.setToolTipText("Overlay #1");
		ov1.setFocusPainted(false);
		overlayPanel.add(ov1);
		ov1.setBackground(new java.awt.Color(226,226,226));
		ov1.setBounds(0,0,35,36);
		ov2.setBorderPainted(false);
		ov2.setToolTipText("Overlay #2");
		ov2.setFocusPainted(false);
		overlayPanel.add(ov2);
		ov2.setBackground(new java.awt.Color(226,226,226));
		ov2.setBounds(35,0,35,36);
		ov3.setBorderPainted(false);
		ov3.setToolTipText("Overlay #3");
		ov3.setFocusPainted(false);
		overlayPanel.add(ov3);
		ov3.setBackground(new java.awt.Color(226,226,226));
		ov3.setBounds(70,0,35,36);
		//plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		//plotPanel.setBackground(new java.awt.Color(200,200,200));
		//plotPanel.setForeground(java.awt.Color.white);
		//plotPanel.setBounds(37,213,386,249);
		
		ov1.addMouseListener(this);
		ov2.addMouseListener(this);
		ov3.addMouseListener(this);
		ov2PopupMenu.addMouseListener(this);
		ov3PopupMenu.addMouseListener(this);*/
		//SymComponent aSymComponent = new SymComponent();
		//toolPanel.addComponentListener(aSymComponent);
		//this.addMouseListener(this);
		
		// tool Panel (bottom of each window)
		LossyConstraints lcf = new LossyConstraints();
		lcf.setType(lcf.IS_FILLER);
		LossyConstraints lcs = new LossyConstraints();
		lcs.setType(lcs.IS_STATIC);
		toolPanel.setLayout(new LossyLayout(false));
		//toolPanel.setBounds(36,481,396,42);  // was 386
		//toolPanel.setPreferredSize(new Dimension(396, 42));  // was 386
		//toolPanel.setMinimumSize(new Dimension(396, 42));  // was 386
		toolPanel.setVisible(true);

		JPanel fill1 = new JPanel();
		//fill1.setMinimumSize(new Dimension(22, 38));
		//fill1.setPreferredSize(new Dimension(22, 38));
		
		lcf.setFillerPercentage(4);
		toolPanel.add(fill1, lcf);
		
		if (!Constants.ISMAC) {
			closeButn.setMinimumSize(new Dimension(34,33));
			closeButn.setPreferredSize(new Dimension(34,33));
		}
		closeButn.setToolTipText("Close this panel");
		closeButn.addActionListener(this);
     	closeButn.setSelected(false);
     	//closeButn.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
		
		lcs.setTossPriority(lcs.SECOND);
		toolPanel.add(closeButn, lcs);
		
		JPanel fill2 = new JPanel();
		//fill2.setMinimumSize(new Dimension(40, 38));
		//fill2.setPreferredSize(new Dimension(40, 38));
		
		lcf.setFillerPercentage(4);
		lcf.setTossPriority(lcf.SECOND);
		toolPanel.add(fill2, lcf);
		
		//labelPanel.setMinimumSize(new Dimension(49,33));   // was 39
		//labelPanel.setPreferredSize(new Dimension(49,33));   // was 39
		
		lcs.setTossPriority(lcs.FIRST);
		toolPanel.add(labelPanel, lcs);

		JPanel fill3 = new JPanel();
		//fill3.setMinimumSize(new Dimension(25, 38));
		//fill3.setPreferredSize(new Dimension(25, 38));
		
		lcf.setFillerPercentage(1);
		lcf.setTossPriority(lcf.FIRST);
		toolPanel.add(fill3, lcf);
		
		//lcs.setTossPriority(lcs.NEVER_TOSS);
		lcs.setTossPriority(lcs.THIRD);
		toolPanel.add(textFieldsPanel, lcs);
		
		JPanel fill4 = new JPanel();
		//fill4.setMinimumSize(new Dimension(25, 38));
		//fill4.setPreferredSize(new Dimension(25, 38));
		
		lcf.setTossPriority(lcf.NEVER_TOSS);
		lcf.setFillerPercentage(3);
		toolPanel.add(fill4, lcf);
		
		//overlayPanel.setPreferredSize(new Dimension(106, 36));
		//overlayPanel.setMinimumSize(new Dimension(106, 36));
		
		lcs.setTossPriority(lcs.NEVER_TOSS);
		toolPanel.add(overlayPanel, lcs);
		
		JPanel fill5 = new JPanel();
		//fill5.setMinimumSize(new Dimension(25, 38));
		//fill5.setPreferredSize(new Dimension(25, 38));
		
		lcf.setFillerPercentage(2);
		toolPanel.add(fill5, lcf);
		
		// toolPanel will listen for drag (resizes) from parent
		InnerStretchActionListener dal = new InnerStretchActionListener();
		this.addStretchActionListener(dal);
		
		this.setMinimumSize(new Dimension(200,200));
		this.setPreferredSize(new Dimension(200,200));
		this.setMaximumSize(new Dimension(800,800));
		this.setLayout(new BorderLayout(0, 0));
		
		this.add(BorderLayout.CENTER, new NPixelBorder(plotPanel, 5, 5, 5, 5));
		this.add(BorderLayout.SOUTH, new NPixelBorder(toolPanel, 5, 5, 5, 5));
		
		toolPanel.invalidate();
		toolPanel.validate();
		this.invalidate();
		this.validate();
	} 
	
	public void rubberbandEnded(Rubberband rb, boolean shiftDown) {
	}
	
	public void processSectionSpline(boolean isShiftDown) {
	}
	
	public void processPolygonSpline(boolean isShiftDown) {
	}
    
    public void handleDragEnded(DragHandle dh) {
    }
    
    public void handleAxisClick(String theAxis, Point thePt) {
    
    }
    
    public void handleAxisRangeClick(String theAxis) {
    
    }
    
    public int ComputeSectionPixelWidth(Point p) {return 1;}

	public void setImageIcon(URL imgIcon, String topLabelStr, String bottomLabelStr) {
		if (TRACE) 
			System.out.println("setImageIcon entered");
		
		if (imgIcon != null)
			closeButn.setIcon(new ImageIcon(imgIcon));
	}

   public CutPanel(String sTitle) {
      this();
   }

   public CutPanel(Object parentObject) {
      this();
      this.parentObject = parentObject;
   }

   public void setVisible(boolean b)
   {
      if (b)
         setLocation(50, 50);
      super.setVisible(b);
   }

   public void rebuildTextFieldsPanel(Component c1, Component c2, Component c3, Component c4) {
      textFieldsPanel.add(c1);
      textFieldsPanel.add(c2);
      textFieldsPanel.add(c3);
      textFieldsPanel.add(c4);
      textFieldsPanel.validate();
   }

   public void setTextFieldTypes(int topFieldType,
            int t_lowerConstraint,
            int t_upperConstraint,
            int bottomFieldType,
            int b_lowerConstraint,
            int b_upperConstraint) {

      Component c0 = topLeftText;
      Component c1 = topRightText;
      Component c2 = bottomLeftText;
      Component c3 = bottomRightText;
      textFieldsPanel.removeAll();

      if (topFieldType == Constants.TIME_FIELD) {
         startDate = new DateTextField(mViewManager, this,"SelTimeStart","Start Time", timeDisplayFormat, Constants.Y_AXIS);
         stopDate = new DateTextField(mViewManager, this, "SelTimeStop","Stop Time",timeDisplayFormat, Constants.Y_AXIS);
         c0 = startDate;
         c1 = stopDate;
      }
      if (bottomFieldType == Constants.TIME_FIELD) {
         startDate = new DateTextField(mViewManager, this, "SelTimeStart", "Start Time", timeDisplayFormat, Constants.X_AXIS);
         stopDate = new DateTextField(mViewManager, this, "SelTimeStop", "Stop Time", timeDisplayFormat, Constants.X_AXIS);
         c2 = startDate;
         c3 = stopDate;
      }
      if (topFieldType == Constants.LAT_FIELD) { 
         startLat = new LatTextField(mViewManager, this,"SelLatitudeStart", "South Latitude", geoDisplayFormat, Constants.Y_AXIS);
         stopLat = new LatTextField(mViewManager, this,"SelLatitudeStop" ,"North Latitude", geoDisplayFormat, Constants.Y_AXIS);
         c0 = startLat;
         c1 = stopLat;
      }
      if (bottomFieldType == Constants.LAT_FIELD) {
         startLat = new LatTextField(mViewManager, this,"SelLatitudeStart","South Latitude", geoDisplayFormat, Constants.X_AXIS);
         stopLat = new LatTextField(mViewManager, this,"SelLatitudeStop" ,"North Latitude", geoDisplayFormat, Constants.X_AXIS);
         c2 = startLat;
         c3 = stopLat;
      }
      if (topFieldType == Constants.LON_FIELD) { 
         startLon = new LonTextField(mViewManager, this, "SelLongitudeStart","Left Longitude", geoDisplayFormat, Constants.Y_AXIS);
         stopLon = new LonTextField(mViewManager, this,"SelLongitudeStop", "Right Longitude", geoDisplayFormat, Constants.Y_AXIS);
         c0 = startLon;
         c1 = stopLon;
      }
      if (bottomFieldType == Constants.LON_FIELD) {
         startLon = new LonTextField(mViewManager, this,"SelLongitudeStart","Left Longitude", geoDisplayFormat, Constants.X_AXIS);
         stopLon = new LonTextField(mViewManager, this,"SelLongitudeStop", "Right Longitude", geoDisplayFormat, Constants.X_AXIS);
         c2 = startLon;
         c3 = stopLon;
      }
      if (topFieldType == Constants.DEPTH_FIELD) { 
         startDepth = new DepthTextField(mViewManager, this,"SelDepthStart","Minimum Depth", Constants.Y_AXIS);
         stopDepth = new DepthTextField(mViewManager, this, "SelDepthStop","Maximum Depth", Constants.Y_AXIS);
         c0 = startDepth;
         c1 = stopDepth;
      }
      if (bottomFieldType == Constants.DEPTH_FIELD) {
         startDepth = new DepthTextField(mViewManager, this,"SelDepthStart","Minimum Depth", Constants.X_AXIS);
         stopDepth = new DepthTextField(mViewManager, this, "SelDepthStop","Maximum Depth", Constants.X_AXIS);
         c2 = startDepth;
         c3 = stopDepth;
      }

      rebuildTextFieldsPanel(c0, c1, c2, c3);
//      c0.addPropertyChangeListener(mViewManager);
//      c1.addPropertyChangeListener(mViewManager);
//      c2.addPropertyChangeListener(mViewManager);
//      c3.addPropertyChangeListener(mViewManager);
    }


   public void addNotify() {
      // Record the size of the window prior to calling parents addNotify.
      Dimension size = getSize();

      super.addNotify();

      if (frameSizeAdjusted)
         return;
      frameSizeAdjusted = true;

      // Adjust size of frame according to the insets and menu bar
      Insets insets = getInsets();
      javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
      int menuBarHeight = 0;
      if (menuBar != null)
         menuBarHeight = menuBar.getPreferredSize().height;
      setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
   }

	// Used by addNotify
	boolean frameSizeAdjusted = false;  
	
	public void handleNewFilteredDataEvent(){
    	System.out.println("IN handleNewFilteredDataEvent");
    }

	public void mouseReleased(MouseEvent event) { 
		super.mouseReleased(event);
	}

      public void mouseClicked(MouseEvent event) {
         Object object = event.getSource();
      }

	public void mousePressed(MouseEvent event) { 
		super.mousePressed(event);
		((ViewManager)parentObject).setFocusView((CutPanelView)this);
	}

	public void mouseEntered(MouseEvent event) {
		super.mouseEntered(event);
		//obj.setIsFocusPanel(true);
	}
	
	public void mouseExited(MouseEvent event) {
		super.mouseExited(event);
		//obj.setIsFocusPanel(false);
	}

   void ov2PopupMenu_mouseClicked(MouseEvent event)
   {
      // to do: code goes here.
          
   }

   void ov3PopupMenu_mouseClicked(MouseEvent event)
   {
      // to do: code goes here.
          
   }

   class SymComponent extends ComponentAdapter
   {
      public void componentResized(ComponentEvent event)
      {
         Object object = event.getSource();
         if (object == toolPanel)
            toolPanel_componentResized(event);
      }
   }

   void toolPanel_componentResized(ComponentEvent event)
   {
//      toolPanel.setVisible(false);
          
   }

   //
   // inner class InnerStretchActionListener
   //
   class InnerStretchActionListener implements StretchActionListener {
      public void stretchStarted(ChangeEvent evt) {
              int n = toolPanel.getComponentCount();
              for (int i=0; i<n; i++) {
                 Component c = toolPanel.getComponent(i);
                 c.setVisible(false);
         }
      }
      public void stretchStopped(ChangeEvent evt) {
              int n = toolPanel.getComponentCount();
              for (int i=0; i<n; i++) {
                 Component c = toolPanel.getComponent(i);
                 c.setVisible(true);
         }
      }
   }

   public void actionPerformed(ActionEvent event) {
     if (event.getSource() == closeButn) {

     	closeButn.setSelected(false);
     }
   }

   public int getUseCenterWidthOrMinMax() {
      return useCenterWidthOrMinMax;
   }
    
   public void setUseCenterWidthOrMinmax(int useCenterWidthOrMinMax) {
      this.useCenterWidthOrMinMax = useCenterWidthOrMinMax;
   }

   public double getLonReference() {
      return lonReference;
   }
    
   public void setLonReference(double lonReference) {
      this.lonReference = lonReference;
   }


   public int getGeoDisplayFormat() {
      return geoDisplayFormat;
   }
    
	public void setGeoDisplayFormat(int geoDisplayFormat) {
		this.geoDisplayFormat = geoDisplayFormat;
		if (startLon != null) 
			startLon.setGeoDisplayFormat(geoDisplayFormat);
		if (stopLon != null) 
			stopLon.setGeoDisplayFormat(geoDisplayFormat);
		if (startLat != null) 
			startLat.setGeoDisplayFormat(geoDisplayFormat);
		if (stopLat != null) 
			stopLat.setGeoDisplayFormat(geoDisplayFormat);
	}

	public void setTimeDisplayFormat(int timeDisplayFormat) {
		if (TRACE) 
			System.out.println("setTimeDisplayFormat entered");
		this.timeDisplayFormat = timeDisplayFormat;
		if (startDate != null)
			startDate.setTimeDisplayFormat(timeDisplayFormat);
		if (stopDate != null) 
			stopDate.setTimeDisplayFormat(timeDisplayFormat);
	}

   public int getTimeDisplayFormat() {
      return timeDisplayFormat;
   }


	public Object getAncestor() {
		if (parentObject != null) {
			return ((Lineage)parentObject).getAncestor();
		}
		return null;
	}
	
	public String toString() {
		return super.toString();
	}

	public void insertUpdate(DocumentEvent e) {
	}
	public void removeUpdate(DocumentEvent e) {
	}
	public void changedUpdate(DocumentEvent e) {
	}

   static public void main(String args[]) {
      JFrame fr = new JFrame();
      fr.setSize(918,570);
      fr.setVisible(true);
      fr.getContentPane().setLayout(null);

      CutPanel cp = new CutPanel();
      cp.setBounds(10,100,350,350);
       cp.setVisible(true);
/*
      cp.setTextFieldTypes(Constants.TIME_FIELD, 0, 0,
            Constants.LAT_FIELD, 0, 0);
*/
      fr.getContentPane().add(cp);
      fr.invalidate();
          fr.validate();
   }
	
	public OverlaySetupPanel getOverlaySetupPanel() {
		return null;
	}
	
	public Object getOverlaySpec() {
		return mOverlaySpec;
	}
	
	public void resetOverlaySpec() {
	System.out.println("CutPanel: resetOverlaySpec");
		mOverlaySpec = null;
	}
	
	public void setOverlaySpec(Object obj) {
		mOverlaySpec = obj;
	}
}
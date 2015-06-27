/*
 * $Id: CutPanelContainer.java,v 1.5 2005/03/23 23:52:21 oz Exp $
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

  import javax.swing.*;
  import java.awt.*;
  import java.util.*;
  import java.awt.event.*;
  import gov.noaa.pmel.swing.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
/**
* 
* The cutPanelContainer is a JScrollPane that scrolls over a JPanel (with a 
*  JPanel in its viewport).  The JPanel has a capacity to hold up to 6 cut panels.
*
* The JPanel is sized to hold either 3x2 or 2x3 cut panels, depending on 
*  the JScrollPane's width / height ratio.
*
* The JPanel is resized under two conditions:
*   - when the cut panels are resized (that is, its children are resized).
*   - when the JScrollPane is resized (that is, its parent is resized).
*/
public class CutPanelContainer extends JScrollPane implements StretchActionListener, ComponentListener, MouseListener, ActionListener {
	private JPanelScrollable jpanel = new JPanelScrollable();
	int gridLongSide = 3;   
	int gridShortSide = 2;
	int hgap = 1;
	int vgap = 1;
	int wMultiplier;	// horizontal (width) multiplier for sizing jpanel
	int hMultiplier;  // vertical (height) multiplier for sizing jpanel
	int cutPanelMinSize;
	int cutPanelMaxSize;
	boolean TRACE = false; 
	boolean asNeeded = true;
	Dimension activeCutPanelSize;
	JMenuItem miOptions;
	JMenuItem miRefit;
	JMenuItem miProps;
	JPopupMenu popupMenu;
	UserSettingsManager userSettingsMgr;
	UserSettingsGUI userSettingsGUI;
	Vector containedViews = new Vector();
	ViewManager mViewManager;
	Vector layerMenuItems = new Vector();

  // --------------------------------------------
  // constructor
  //
  /**
  * Sets visibility, adds jpanel to viewport, sets layout, adds listeners, and
  *  sets and calibrates sizes of cut panels and the viewport's jpanel.
  */
	public CutPanelContainer(ViewManager vm) {
		super();
		mViewManager = vm;
		if (TRACE) 
			System.out.println("CutPanelContainer constructor entered");
		this.setVisible(true);
		this.getViewport().add(jpanel);
		
		jpanel.setLayout(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
		addComponentListener(this);
		jpanel.addMouseListener(this);
		CutPanel tmp = new CutPanel();
		cutPanelMinSize = tmp.getMinimumSize().width;
		cutPanelMaxSize = tmp.getMaximumSize().width;
		activeCutPanelSize = tmp.getMinimumSize();
		adjustJPanelMultipliers();
		calibrateJPanelSize();
		validateAll();
	}

		/**
		* 
		*/
		void createPopupMenu() {
			NdEdit nd = (NdEdit)mViewManager.getParent();
			PointerCollectionGroup pcg = nd.getPointerCollection();
			if (pcg == null || pcg.size() <= 0)
				return;
			if (TRACE) 
				System.out.println(" createPopupMenu entered");
			popupMenu = new JPopupMenu();
			miOptions = new JMenuItem("Options...");
			miOptions.setFont(BeanFonts.miFont);
			miOptions.addActionListener(this);
			miRefit = new JMenuItem("Resize Cut Panels To Fit");
			miRefit.addActionListener(this);
			miRefit.setFont(BeanFonts.miFont);
			popupMenu.add(miOptions);
			popupMenu.add(miRefit);
			
			// add popupm items for which is the selected layer
			PointerCollection selectedPC = null;
			int selectedPCIndx = -99;
			layerMenuItems = null;
			layerMenuItems = new Vector();
			for (int i=0; i<pcg.size(); i++) {
				PointerCollection pc = (PointerCollection)pcg.elementAt(i);
				if (pcg.isSelectedLayer(pc)) {
					selectedPC = pc;
					selectedPCIndx = i;
				}
				JCheckBoxMenuItem pcmi = new JCheckBoxMenuItem(pc.getPCTitle());
				layerMenuItems.add(pcmi);
				pcmi.addActionListener(this);
				pcmi.setFont(BeanFonts.miFont);
				popupMenu.add(pcmi);
			}
			
			pcg.clearSelectedLayer();
			if (selectedPC != null)
				pcg.setSelectedLayer(selectedPC);
			else {
				
				selectedPCIndx = 0;
				selectedPC = ((PointerCollection)pcg.elementAt(0));
				pcg.setSelectedLayer(selectedPC);
			}
			
			JCheckBoxMenuItem selItem = (JCheckBoxMenuItem)layerMenuItems.elementAt(selectedPCIndx);
			selItem.setSelected(true);
		}
		
		void resetPopupMenu() {
			popupMenu = null;
			createPopupMenu();
		}

		/**
		* Adds a generic panel, adjusting both its size and preferred size to 
		* match the sizes of other panels already in container. 
		*
		* @param cutPanelView
		*/
		public void addCutPanel(Object inView){
			if (TRACE) 
				System.out.println(" addCutPanel (cutPanelView) entered");
			
			if (inView instanceof CutPanelView) {
				((CutPanelView)inView).addStretchActionListener(this);
				((CutPanelView)inView).addMouseListener(this);
				((CutPanelView)inView).setSize(activeCutPanelSize);
				((CutPanelView)inView).setPreferredSize(activeCutPanelSize);
				jpanel.add((Component)inView);
			}
			else if (inView instanceof InfoPanelView) {
				//((InfoPanelView)inView).setSize(activeCutPanelSize);
				//((InfoPanelView)inView).setPreferredSize(activeCutPanelSize);
				jpanel.add((Component)inView);
			}
			SelectionRegion.selOffset = 0;
			validateAll();
		}
		
		/**
		* Adds a cut panel, adjusting both its size and preferred size to 
		* match the sizes of other cut panels already in container. 
		*
		* @param cutPanelView
		*/
		public void addCutPanel(CutPanelView cutPanelView){
			if (TRACE) 
				System.out.println(" addCutPanel (cutPanelView) entered");
			
			cutPanelView.addStretchActionListener(this);
			cutPanelView.addMouseListener(this);
			cutPanelView.setSize(activeCutPanelSize);
			cutPanelView.setPreferredSize(activeCutPanelSize);
			jpanel.add(cutPanelView);
			SelectionRegion.selOffset = 0;
			validateAll();
		}

		/**
		* 
		* @param cutPanelView
		*/
		public void removeCutPanel(Component inView){
			if (TRACE) 
				System.out.println(" removeCutPanel entered");
			
			jpanel.remove(inView);
			if (inView instanceof CutPanelView) {
				((CutPanelView)inView).removeStretchActionListener(this);
				((CutPanelView)inView).removeMouseListener(this);
			}
			SelectionRegion.selOffset = 0;
			validateAll();
		}

		/**
		* 
		* @param cutPanelView
		*/
		public void removeCutPanel(CutPanelView cutPanelView){
			if (TRACE) 
				System.out.println(" removeCutPanel entered");
			
			jpanel.remove(cutPanelView);
			cutPanelView.removeStretchActionListener(this);
			cutPanelView.removeMouseListener(this);
			SelectionRegion.selOffset = 0;
			validateAll();
		}


		/**
		* 
		* Calibrates and sets the jpanel's size.
		*
		*  To determine its size, it considers two items:
		*
		*    - the size of its children (the CutPanels)
		*    - the width / height ratio of its parent JScrollPane 
		*
		*/
		public void calibrateJPanelSize(){
			if (TRACE)
				System.out.println(" calibrateJPanelSize entered");
			
			int cutPanelW = activeCutPanelSize.width;
			int cutPanelH = activeCutPanelSize.height;
			
			// Note: the ".95" is extra buffer space for stretching, but not enough
			// extra space to fit in another cut panel 
			//
			int w = ((cutPanelW + hgap + hgap) *  wMultiplier) + (int)(cutPanelW * .95);
			int h = ((cutPanelH + vgap + vgap) *  hMultiplier) + (int)(cutPanelH * .95);
			Dimension d = new Dimension(w, h);
			jpanel.setSize(d);
			jpanel.setPreferredSize(d);
		}

		/**
		*  
		*/
		public Dimension getPreferredSize() {
			return jpanel.getPreferredSize();
		}

		/**
		* Adjusts mulitipliers that are used when calibrating the JPanel's size.
		*/
		private void adjustJPanelMultipliers() {
			// Adjust: whichever dimension is greater, give the longer side; 
			//   give the smaller dim the shorter side.
			if (TRACE) 
				System.out.println(" adjustJPanelMultipliers entered");
			if ((this.getWidth() >= this.getHeight())) {
				wMultiplier = gridLongSide;
				hMultiplier = gridShortSide;
			}
			else {
				wMultiplier = gridShortSide;
				hMultiplier = gridLongSide;
			}
		}

  // --------------------------------------------
  //
		/**
		* 
		* Given a new size, resets sizes of all visible cut panels, adjusts
		* JPanel size, and adjusts scrollbars.
		*
		* @param dim
		*/
		public void cutPanelResize(Dimension dim){
			if (TRACE) 
				System.out.println(" cutPanelResize (Dimension) entered to " + dim);
			
			activeCutPanelSize = dim;
			resetAllCutPanelSizes();
			calibrateJPanelSize();
			validateAll();
			adjustScrollbars();
		}

  // --------------------------------------------
  //
  /**
  * Given the newly-stretched cut panel, resets sizes of other visible cut panels, 
  * adjusts JPanel size, and adjusts scrollbars.
  *
  * @param stretchedPanel 
  */
  public void cutPanelResize(JStretchPanel stretchedPanel){
     if (TRACE) 
     	System.out.println(" cutPanelResize (JStretchPanel) entered");

     cutPanelResize(stretchedPanel.getSize());
  }

  // --------------------------------------------
  //
	/**
	*
	* For all cut panels in the container, resizes to activeCutPanelSize.
	*
	*/
	public void resetAllCutPanelSizes(){
		if (TRACE) 
			System.out.println(" resetAllCutPanelSizes entered");
	 	int n = jpanel.getComponentCount();
		for (int i=0; i<n; i++) {
	    	Component c = jpanel.getComponent(i);
			if (c instanceof JStretchPanel) {
	   			JStretchPanel cutPanelView = (JStretchPanel) c;
	   			cutPanelView.setSize(activeCutPanelSize);
	   			cutPanelView.setPreferredSize(activeCutPanelSize);
	   			cutPanelView.invalidate();
	   			cutPanelView.validate();
			}
			else if (c instanceof InfoPanelView) {
			}
		}
	}


  /**
  * Fits the cut panels exactly into the container, precluding the need for 
  * scrollbars.  Sets the scrollbar policies to NEVER, after first scrolling back
  * to the home scrolling position.
  */
  public void fitCutPanelsToContainer() {

     if (TRACE)
     	System.out.println(" fitCutPanelsToContainer entered");
     int w = this.getSize().width;
     int h = this.getSize().height;
     int max = Math.max(w, h);
     Component cutPanelView = jpanel.getComponent(0);
     if (w > h) {
        int wid = (w/3) - 3;   // - 9;
        int hgt = (h/2) - 3;   // - 9;
		if (wid > cutPanelView.getMinimumSize().width) {
	           cutPanelResize(new Dimension (wid, hgt));
		}
		else {
		   JOptionPane.showMessageDialog(this, 
			new String("Refit failed: Cut Panel Minimum Size is " +
			cutPanelView.getMinimumSize().width + " ; Try enlarging your container."), "Warning", JOptionPane.WARNING_MESSAGE);
		}
     }
     else {
        int hgt = (h/3) - 3;   // - 9;
        int wid = (w/2) - 3;   // - 9;
		if (hgt > cutPanelView.getMinimumSize().height) {
		       cutPanelResize(new Dimension (wid, hgt));
		}
		else {
		   JOptionPane.showMessageDialog(this, 
			new String("Refit failed: Cut Panel Minimum Size is " + cutPanelView.getMinimumSize().width + " ; Try enlarging your container."), "Warning", JOptionPane.WARNING_MESSAGE);
		}
     }
     validateAll();
  }

  // -------------------------------------------
  //
  /**
  * Adjusts the scrollbar policy by looping through each cut panel in the
  * container to determine outermost boundary of cut panels, then setting 
  * scroll policy to "never" if all cut panels are fully visible, or to 
  * "as needed" if all cut panels aren't fully visible.
  */
  private void adjustScrollbars() {

     if (TRACE) 
     	System.out.println(" adjustScrollbars entered");
     int w = this.getSize().width;
     int h = this.getSize().height;
     int n = jpanel.getComponentCount();
     int leftmost = 0;
     int bottommost = 0;

     // find panel edge that's most extreme
     for (int i=0; i<n; i++) {
		Component c = jpanel.getComponent(i);
		if (c instanceof JStretchPanel) {
			leftmost =  Math.max(leftmost, c.getLocation().x);
			bottommost =  Math.max(bottommost, c.getLocation().y) ;
		}
     }

     int endx = leftmost + activeCutPanelSize.width; 
     int endy = bottommost + activeCutPanelSize.height;

     if (endx < w && endy < h) {
        if ((this.getHorizontalScrollBar().getValue() == 0) && (this.getVerticalScrollBar().getValue() == 0)) {
           setScrollBarsNever();
	   		if (asNeeded) 
	   			validateAll();
	   		asNeeded = false;
        }
     }
     else {
        setScrollBarsAsNeeded();
		if (!asNeeded) 
			validateAll();
		asNeeded = true;
     }
  }

  // -------------------------------------------
  //
  /**
  * Validates both Jpanel and the JScrollpane, plus repaints the JScrollpane
  * to force scrollbars to be reflect current state of the model.
  */
  public void validateAll() {
     if (TRACE) 
     	System.out.println(" validateAll entered");
     jpanel.invalidate();
     jpanel.validate();
     this.repaint();
     this.invalidate();
     this.validate();
  }

  // -------------------------------------------
  //
  /**
  * 
  */
  public Dimension getCutPanelSize() {
     return activeCutPanelSize;
  }

  // -------------------------------------------
  //
  /**
  * Gets the JPanel serving as the viewport for the JScrollPane.
  */
  public JPanel getJPanel() {
     return jpanel;
  }

  // -------------------------------------------
  //
  /**
  * Extracts current settings into a user settings object.
  */
  public UserSettings extractCurrentUserSettings() {
     // Get all other info from active state of display
     //
     UserSettings us = new UserSettings();
     us.setVisibleViewIds(getVisibleViews());
     us.setCutPanelSize(getCutPanelSize());

     CutPanelView cutPanelView = (CutPanelView) jpanel.getComponent(0);
     if (cutPanelView != null) {
        us.setLonReference(cutPanelView.getLonReference());
        us.setGeoDisplayFormat(cutPanelView.getGeoDisplayFormat());
  		us.setIndependentHandle(cutPanelView.isIndependentHandles());
        us.setTimeDisplayFormat(cutPanelView.getTimeDisplayFormat());
        us.setDisplayAxes(cutPanelView.getDisplayAxes());
     }

     return us;
  }
 
  // -------------------------------------------
  //
  /**
  * 
  */
  public int[] getVisibleViews() {

     int[] visViews = new int[jpanel.getComponentCount()];

     for (int i = 0; i < visViews.length; i++) {
        Component c = jpanel.getComponent(i);
        if (c instanceof CutPanelView) 
        	visViews[i] = ((CutPanelView)c).getViewEnum();
        else {
        	;
        }
     }
     return visViews;
  }
  
  /**
  * 
  */
  public void setUserSettingsManager(UserSettingsManager userSettingsMgr) {
     this.userSettingsMgr = userSettingsMgr;
  }

  // -------------------------------------------
  //
  /**
  * Convenience method to set both vertical and horizontal policies.
  */
  public void setScrollBarsAsNeeded() {
     if (TRACE) 
     	System.out.println(" setScrollBarsAsNeeded entered");
     this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
     this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  // -------------------------------------------
  //
  /**
  * Convenience method to set both vertical and horizontal policies.
  */
  public void setScrollBarsNever() {
     if (TRACE) 
     	System.out.println(" setScrollBarsNever entered");
     this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
     this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }

  // -------------------------------------------
  //
  /**
  * Sets up for stretching by turning scrollbar policy to "as needed". 
  *
  * @param evt
  */
  public void stretchStarted(javax.swing.event.ChangeEvent evt) {
     if (TRACE) 
     	System.out.println(" stretchStarted entered");
     setScrollBarsAsNeeded();
  }

  // -------------------------------------------
  //
  /**
  * Responds to stretching stopped by resizing other cut panels.
  *
  * @param evt
  */
  public void stretchStopped(javax.swing.event.ChangeEvent evt) {
     if (TRACE) 
     	System.out.println(" stretchStopped entered");
     cutPanelResize((JStretchPanel)evt.getSource());
  }


  // -------------------------------------------
  //
  /**
  * 
  */
  /*static JFrame tstFrame;
  static public void main(String[] args) {
      tstFrame = new JFrame();
      tstFrame.setSize(600, 600);
      tstFrame.setVisible(true);
      //CutPanelContainer cpc = new CutPanelContainer();
      for (int i = 0; i < 6; i++) {
         CutPanel cutPanelView = new CutPanel();
         cpc.getJPanel().add(cutPanelView);
         //cpc.addCutPanel(cutPanelView);
      }
      tstFrame.getContentPane().add(cpc);
      tstFrame.invalidate();
      tstFrame.validate();
  }*/


  // ---- Action Listener methods -----------------------------
  //
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miOptions) { 
			userSettingsGUI = new UserSettingsGUI(true, true, true, false);
			if (userSettingsGUI.getOkay()) {
				userSettingsGUI.updateSettings();
				userSettingsMgr.apply();
			}
		}
		else if (e.getSource() == miRefit) {
			fitCutPanelsToContainer();
		}
		else  {
			int selectedPC = 0;
			for (int i=0; i<layerMenuItems.size(); i++) {
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem)layerMenuItems.elementAt(i);
				cb.setSelected(false);
				if (cb == e.getSource()) {
					selectedPC = i;
				}
			}
			
			NdEdit nd = (NdEdit)mViewManager.getParent();
			PointerCollectionGroup pcg = nd.getPointerCollection();
			pcg.clearSelectedLayer();
			
			JCheckBoxMenuItem selItem = (JCheckBoxMenuItem)layerMenuItems.elementAt(selectedPC);
			selItem.setSelected(true);
			PointerCollection pc = (PointerCollection)pcg.elementAt(selectedPC);
			pcg.setSelectedLayer(pc);
		}
	}
  // --------------------------------------------------------


  // ---- Component Listener methods -----------------------------
  //
  public void componentHidden(ComponentEvent e) {}
  public void componentMoved(ComponentEvent e) {}
  public void componentResized(ComponentEvent e) {
     if (TRACE) System.out.println(" componentResized entered");
     setScrollBarsAsNeeded();
     //
     // if aspect changes, then validate, otherwise don't
     //
     int oldwMultiplier = wMultiplier;
     adjustJPanelMultipliers();
     if (oldwMultiplier != wMultiplier) {
          calibrateJPanelSize();
	  validateAll();  // adds ugly "flickering" when stretching.
     }
     adjustScrollbars();
  }
  public void componentShown(ComponentEvent e) {}
  // --------------------------------------------------------


  // ---- MouseListener methods -----------------------------
  //
  public void mouseReleased(MouseEvent event) { 
     //
     // Windows isTrigger is mouseReleased
     //
     if (event.isPopupTrigger() && !isInAxisAreaOfCutPanel(event)) {
		if (popupMenu == null) createPopupMenu();
		if (popupMenu != null && event.getComponent() != null)
			popupMenu.show(event.getComponent(), event.getX(), event.getY());
		}
  }
  public void mouseClicked(MouseEvent event) { }
  public void mousePressed(MouseEvent event) { 
     //
     // Unix isTrigger is mousePressed
     //
     if (event.isPopupTrigger() && !isInAxisAreaOfCutPanel(event)) {
		if (popupMenu == null) createPopupMenu();
		if (popupMenu != null && event.getComponent() != null)
			popupMenu.show(event.getComponent(), event.getX(), event.getY());
		}
  }
  public void mouseEntered(MouseEvent event) { }
  public void mouseExited(MouseEvent event) { }
  // --------------------------------------------------------



  class JPanelScrollable extends JPanel implements Scrollable {

     // ---- Scrollable interface methods -----------------------------
     //

     public Dimension getPreferredScrollableViewportSize() {
	return this.getPreferredSize();
     }

     public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {

        if (orientation == SwingConstants.VERTICAL) {
	   		return activeCutPanelSize.height / 2;
        }
        else {
	   		return activeCutPanelSize.width / 2;
        }
     }

     public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
	   		return activeCutPanelSize.height / 2;
        }
        else {
	   		return activeCutPanelSize.width / 2;
        }
     }

     public boolean getScrollableTracksViewportWidth() {
        return false;
     }

     public boolean getScrollableTracksViewportHeight() {
        return false;
     }
  }
	
	protected boolean isInAxisAreaOfCutPanel(MouseEvent event) {
		String theAxis = null;
		CutPanelView cp = mViewManager.getFocusView();
		if ((theAxis = cp.isInAxesRangeArea(event.getPoint())) != null) {
			cp.handleAxisRangeClick(theAxis);
			return true;
		}
		return false;
	}
}

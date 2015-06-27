/*
 * $Id: ViewManager.java,v 1.21 2005/06/17 17:24:17 oz Exp $
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
import java.util.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import ndEdit.*;
import gov.noaa.pmel.swing.*;
import java.awt.event.*;

 /**
 *
 * Manages and coordinates the actions of the view toolbar buttons
 * with the cut panels in the cut panel container.
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 *
 * @note View Manager manages the views.  It consults with the user 
  * preferences to determine which views should be available at 
  * startup.  It maintains button states for the view toolbar (if 
  * there is one), and it possibly provides intel to other objects 
  * that need to know which views are active.
  * @stereotype Mediator
*/
public class ViewManager implements Lineage, PropertyChangeListener, ChangeableInfoListener, AdjustmentListener {
	public static final int CENTER_WIDTH = 1;
	public static final int MIN_MAX = 2;
	boolean TRACE = false; 
	boolean DEBUG = false; 
	private Vector visibleViews = new Vector(Constants.MAX_CUT_PANEL_TYPES);
	private CutPanelView[] cutPanelViews = new CutPanelView[Constants.MAX_CUT_PANEL_TYPES];
	private CutPanelView focusedView;
	private CutPanelContainer cutPanelContainer = new CutPanelContainer(this);
	
	// Note: if adding any new view types, be sure to make changes in constructor also.
	private LatLonView latLonView;
	private LatTimeView latTimeView;
	private LatDepthView latDepthView;
	private LonTimeView lonTimeView; 
	private LonDepthView lonDepthView;
	private DepthTimeView depthTimeView;
	private SelectedInfoView selectedView;
	private Object parentObject;
	private PointerCollectionGroup mCurrPC = null;
	private Double offsetVal = new Double(0.0);
	private CutPanelView mCurrView;
    private UserSettingsManager	userSettingsMgr;
    private boolean mInBatch = false;

	// constructor
	//
	/**
	* Creates an object for each view, sets up data structures for
	* internal class use, and sets the sizes of the cut panels to their
	* preferred size.
	*
	* @param parentObject handle to parent object mainly used for tracing back up 
	*         the ancestral tree
	*/
	public ViewManager(Object parentObject) {
		this.parentObject = parentObject;
		latLonView = new LatLonView(this);
		latTimeView = new LatTimeView(this);
		latDepthView = new LatDepthView(this);
		lonTimeView = new LonTimeView(this); 
		lonDepthView = new LonDepthView(this);
		depthTimeView = new DepthTimeView(this);

		//selectedView = new SelectedInfoView(this);
		
		cutPanelViews[Constants.LAT_LON] = latLonView;
		cutPanelViews[Constants.LAT_TIME] = latTimeView;
		cutPanelViews[Constants.LAT_DEPTH] = latDepthView;
		cutPanelViews[Constants.LON_TIME] = lonTimeView;
		cutPanelViews[Constants.LON_DEPTH] = lonDepthView;
		cutPanelViews[Constants.DEPTH_TIME] = depthTimeView;
		//cutPanelViews[Constants.SELECTED_VIEW] = selectedView;

		// this needs to be more robust
		this.setFocusView(latLonView);
		//
		// Set actual sizes of cutPanels to their preferredsize.
		// Also, set ViewManager reference to "this"
		//
		for (int i = 0; i < cutPanelViews.length; i++) {
			cutPanelViews[i].setSize(cutPanelViews[i].getPreferredSize());
			if (cutPanelViews[i] instanceof CutPanelView)
				((CutPanelView)cutPanelViews[i]).setViewManager(this);
		}

		if (Debug.DEBUG) {
		   System.out.println(" latlonView size: " + latLonView.getSize());
		   System.out.println(" latlonView preferred size: " + latLonView.getPreferredSize());
		}

		// register with the ChangeableInfoMgr so we hear changes from the CutPanel text fields
		if (parentObject != null) 
			((NdEdit)parentObject).addChangeableInfoListener(this);
			
		if (Constants.ISMAC) {
			// try to register with the scrollbars to get adjustment events
			JScrollBar hsb = cutPanelContainer.getHorizontalScrollBar();
			JScrollBar vsb = cutPanelContainer.getVerticalScrollBar();
			if (hsb != null && vsb != null) {
			    hsb.addAdjustmentListener(this);
			    vsb.addAdjustmentListener(this);
			}
		}
	}
  
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		if (!ae.getValueIsAdjusting()) {
			if (latLonView != null && latLonView.isVisible())
				latLonView.repaint();
			if (latTimeView != null && latTimeView.isVisible())
				latTimeView.repaint();
			if (latDepthView != null && latDepthView.isVisible())
				latDepthView.repaint();
			if (lonTimeView != null && lonTimeView.isVisible())
				lonTimeView.repaint();
			if (lonDepthView != null && lonDepthView.isVisible())
				lonDepthView.repaint();
			if (depthTimeView != null && depthTimeView.isVisible())
				depthTimeView.repaint();
		}
	}

    public void setPointerCollection(PointerCollectionGroup pcg) {
    	mCurrPC = pcg;
    	latLonView.setPointerCollection(mCurrPC, true);
    	latTimeView.setPointerCollection(mCurrPC, true);
    	latDepthView.setPointerCollection(mCurrPC, true);
    	lonTimeView.setPointerCollection(mCurrPC, true); 
    	lonDepthView.setPointerCollection(mCurrPC, true);
    	depthTimeView.setPointerCollection(mCurrPC, true);
    }
    
	/**
	* Programmatically shows the view if not already visible by calling doClick 
	* on the corresponding toolbar button; this should ONLY be called for 
	* initialization or when resetting preferences - in most cases the views 
	* will be shown in response to user button actions.
	*
	* @param viewEnums static int value representing a view, as defined 
	*  in Constants.java; for example, Constants.LAT_LON.
	*/
	public void showView(int viewEnum) {
		if (TRACE) 
			System.out.println("showview entered  (calls doClick)");
		if (!((CutPanelView)cutPanelViews[viewEnum]).getToolbarButton().isSelected()) {
			((CutPanelView)cutPanelViews[viewEnum]).getToolbarButton().doClick();
		}
		cutPanelViews[viewEnum].setSize(cutPanelViews[viewEnum].getSize().width+1, cutPanelViews[viewEnum].getSize().height);
		cutPanelViews[viewEnum].setSize(cutPanelViews[viewEnum].getSize().width-1, cutPanelViews[viewEnum].getSize().height);
		setFocusView((CutPanelView)cutPanelViews[viewEnum]);
	}
	
	public void toggleView(int viewEnum) {
		if (TRACE) 
			System.out.println("toggleView entered  (calls doClick)");
		((CutPanelView)cutPanelViews[viewEnum]).getToolbarButton().doClick();
		//cutPanelViews[viewEnum].setFieldsFromHandles();
		cutPanelViews[viewEnum].setSize(cutPanelViews[viewEnum].getSize().width+1, cutPanelViews[viewEnum].getSize().height);
		cutPanelViews[viewEnum].setSize(cutPanelViews[viewEnum].getSize().width-1, cutPanelViews[viewEnum].getSize().height);
	}

	/**
	* Programmatically shows a list of views if not already visible by calling doClick 
	* on the corresponding toolbar button; this should ONLY be called for 
	* initialization or when resetting preferences - in most cases the views 
	* will be shown in response to user button actions.
	*
	* @param viewEnums array of values representing views, as defined 
	*  in Constants.java; for example, Constants.LAT_LON.
	*/
	public void showPreferredVisibleViews(int[] viewEnums){
		if (TRACE) 
			System.out.println(" showPreferredVisibleViews entered)");
		for (int i = 0; i < viewEnums.length; i++) {
			showView(viewEnums[i]);
		}
	}

	/**
	* 
	* Programmatically shows all views, if not already visible, by calling doClick 
	* on the corresponding toolbar button.
	*
	*/
	public void showAllViews(){
		if (TRACE) 
			System.out.println(" showAllViews entered ");
		for (int j = 0; j < Constants.MAX_CUT_PANEL_TYPES; j++) {
			if (!((CutPanelView)cutPanelViews[j]).getToolbarButton().isSelected()) {
				((CutPanelView)cutPanelViews[j]).getToolbarButton().doClick();
			}
		}
		setFocusView((CutPanelView)cutPanelViews[0]);
	}
  
	public void invalidateAllViews() {
		for (int j = 0; j < Constants.MAX_CUT_PANEL_TYPES; j++) {
			cutPanelViews[j].invalidate();
			cutPanelViews[j].validate();
    		cutPanelViews[j].setSize(cutPanelViews[j].getSize().width+1, cutPanelViews[j].getSize().height);
    		cutPanelViews[j].setSize(cutPanelViews[j].getSize().width-1, cutPanelViews[j].getSize().height);
			
			/*if (!cutPanelViews[j].isFirstDone()) {
		//System.out.println("replotting " + cutPanelViews[j]);
				cutPanelViews[j].paintPanelData(cutPanelViews[j].getGraphics(), 
					cutPanelViews[j].getSize().width - 10 - cutPanelViews[j].getLegendInset(), 
					cutPanelViews[j].getSize().height-75, 5, 5, 5, 5);
			}*/

		}
	}
	
	public void resetAllViews() {
		for (int j = 0; j < Constants.MAX_CUT_PANEL_TYPES; j++) {
			((CutPanelView)cutPanelViews[j]).reset(true);
    		((CutPanelView)cutPanelViews[j]).setPointerCollection(mCurrPC, true);
		}
	}
	
	public void resetOverlays() {
		for (int j = 0; j < Constants.MAX_CUT_PANEL_TYPES; j++) {
			((CutPanelView)cutPanelViews[j]).resetOverlay();
		}
	}

	/**
	* 
	* Given a cutPanelView object, adds the object to the visible field of
	*  cut panels, while NOT interacting with the corresponding toolbar button.
	*
	* @param cutPanelView 
	*/
	public void showView(Component cutPanelView) {
		if (TRACE) 
			System.out.println("showView entered (calls addCutPanel)");
		visibleViews.addElement((Object)cutPanelView);
		cutPanelContainer.addCutPanel(cutPanelView);
		//cutPanelView.setPointerCollection(mCurrPC, true);
		//cutPanelView.setSize(cutPanelView.getSize().width+1, cutPanelView.getSize().height);
		//cutPanelView.setSize(cutPanelView.getSize().width-1, cutPanelView.getSize().height);
		if (cutPanelView instanceof CutPanelView)
			setFocusView((CutPanelView)cutPanelView);
	}

	/**
	* 
	* Given a cutPanelView object, hides (removes) the object from the visible field of
	*  cut panels, while NOT interacting with the corresponding toolbar button.
	*
	* @param cutPanelView 
	*/
	public void hideView(CutPanelView cutPanelView) {
		if (TRACE) 
			System.out.println(" hideView entered (calls removeCutPanel)");
		cutPanelView.reset(false);
		visibleViews.removeElement((Object)cutPanelView);
		cutPanelContainer.removeCutPanel(cutPanelView);
	}

	/**
	* 
	* Programmatically hides all views, if not already hidden, by calling doClick 
	* on the corresponding toolbar button.
	*/
	public void hideAllViews() {
		if (TRACE) 
			System.out.println(" hideAllViews entered ");
		for (int j = 0; j < Constants.MAX_CUT_PANEL_TYPES; j++) {
			if (((CutPanelView)cutPanelViews[j]).getToolbarButton().isSelected()) {
				((CutPanelView)cutPanelViews[j]).getToolbarButton().doClick();
			}
		}
	}

  /**
  *  Return the cutpanel container
  */
  public CutPanelContainer getCutPanelContainer(){
     return cutPanelContainer;
  }
  
  /**
  * 
  */
  public PointerCollectionGroup getPointerCollection(){
     return mCurrPC;
  }

  /**
  * 
  */
  public JToggleButton[] getToolbarButtons() {
     JToggleButton[] togbutns = new JToggleButton[Constants.MAX_CUT_PANEL_TYPES];
     for (int i = 0; i < Constants.MAX_CUT_PANEL_TYPES; i++) {
		togbutns[i] =((CutPanelView)cutPanelViews[i]).getToolbarButton();
     }
     return togbutns;
  }

  /**
  * 
  */
  public int getNumVisibleViews(){
     return visibleViews.size();
  }

  /**
  * 
  */
  public int[] getVisibleViews() {
     int[] visViews = new int[visibleViews.size()];
     for (int i = 0; i < visibleViews.size(); i++) {
       visViews[i] = ((CutPanelView) visibleViews.elementAt(i)).getViewEnum(); 
     }
     return visViews;
  }

  /**
  * 
  */
  public Vector getVisibleViewsVector() {
     return visibleViews;
  }


  /**
  * 
  */
  public JToggleButton[] dumpToolbarButtons() {
     JToggleButton[] togbutns = new JToggleButton[Constants.MAX_CUT_PANEL_TYPES];
     for (int i = 0; i < Constants.MAX_CUT_PANEL_TYPES; i++) {
	if (DEBUG) System.out.println(" Button: \n" + ((CutPanelView)cutPanelViews[i]).getToolbarButton());
        ((CutPanelView)cutPanelViews[i]).getToolbarButton().repaint();
     }
     return togbutns;
  }

  /**
  * 
  */
  public Dimension getCutPanelSize() {
     return cutPanelContainer.getCutPanelSize();
  }

  /**
  * 
  */
  /*
  public Dimension setCutPanelSize() {
     return cutPanelContainer.setCutPanelSize();
  }
  */

  /**
  * Sets userSettings object and passes it down to other aggregated objects
  * that need it.
  */
  public void setUserSettingsManager(UserSettingsManager userSettingsMgr) {
     this.userSettingsMgr = userSettingsMgr;
     cutPanelContainer.setUserSettingsManager(userSettingsMgr);
  }

  /**
  * 
  */
  public Object getAncestor() {
     if (parentObject != null) {
		return ((Lineage)parentObject).getAncestor();
     }
     return this;
  }
  
  public Object getParent() {
	return parentObject;
  }

  /**
  * 
  */
  public String toString() {
     return("LatLon: " + latLonView 
		+ "\nLatTime: " + latTimeView   
		+ "\nLatDepth: " + latDepthView  
		+ "\nLonTime: " + lonTimeView   
		+ "\nLonDepth: " + lonDepthView  
		+ "\nDepthTime: " + depthTimeView  + "\n");
  }

  /**
  * 
  */
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("StartBatch")) {
			mInBatch = true;
			return;
		}
		else if (e.getPropertyName().equals("EndBatch")) {
			mInBatch = false;
			invalidateAllViews();
			return;
		}
			
		if (e.getPropertyName().equals("GeoDisplayFormat")) {
			for (int i = 0; i < cutPanelViews.length; i++) {
				((CutPanelView)cutPanelViews[i]).setGeoDisplayFormat(((Integer)e.getNewValue()).intValue());
				if (!mInBatch)
					invalidateAllViews();
			}
			
			// set the geo display format for the location toolbar
		}
		else if (e.getPropertyName().equals("TimeDisplayFormat")) {
			for (int i = 0; i < cutPanelViews.length; i++) {
				((CutPanelView)cutPanelViews[i]).setTimeDisplayFormat(((Integer)e.getNewValue()).intValue());
				if (!mInBatch)
					invalidateAllViews();
			}
			
			// set the time display format for the location toolbar
		}
		else if (e.getPropertyName().equals("CutPanelSize")) {
			//System.out.println(" resizing cut panels to: " + (Dimension) e.getNewValue());
			cutPanelContainer.cutPanelResize(((java.awt.Dimension) e.getNewValue()));
		}
		else if (e.getPropertyName().equals("CutPanelMinSize")) {
			int ii = ((Integer)e.getNewValue()).intValue();
			Dimension d = new Dimension(ii, ii);
			for (int i = 0; i < cutPanelViews.length; i++) {
				((CutPanelView)cutPanelViews[i]).setMinimumSize(d);
			}
		}
		else if (e.getPropertyName().equals("CutPanelMaxSize")) {
			int ii = ((Integer)e.getNewValue()).intValue();
			Dimension d = new Dimension(ii, ii);
			for (int i = 0; i < cutPanelViews.length; i++) {
				((CutPanelView)cutPanelViews[i]).setMaximumSize(d);
			}
		}
		else if (e.getPropertyName().equals("VisibleViews")) {
			hideAllViews();
			/*
			int[] ii = (int[]) e.getNewValue();
			
			if (DEBUG) System.out.println(" Visible Views: ");
			for (int i = 0; i < ii.length; i++) {
			if (DEBUG) System.out.println(" - " + ii[i]);
			}
			//
			// Turn off any already views showing but not included in list.
			//
			int[] newList = (int[]) e.getNewValue());
			int[] oldList = getVisibleViews();
			for (int i = 0; i < newList.length; i++) {
			
			}
			*/
			showPreferredVisibleViews((int[]) e.getNewValue());
		}
		else if (e.getPropertyName().equals("displayPanelAxes")) {
			int ii = ((Integer)e.getNewValue()).intValue();
			boolean val = ii == 1 ? true : false;
			for (int i = 0; i < cutPanelViews.length; i++) {
				((CutPanelView)cutPanelViews[i]).setAxesDisplay(val);
			}
			if (!mInBatch)
				invalidateAllViews();
		}
		else if (e.getPropertyName().equals("independentHandles")) {
			int ii = ((Integer)e.getNewValue()).intValue();
			boolean val = ii == 1 ? true : false;
			for (int i = 0; i < cutPanelViews.length; i++) {
				((CutPanelView)cutPanelViews[i]).setIndependentHandles(val);
			}
		}
		else {   	
			// Got a PCE from a draghandle
			String prop = e.getPropertyName();
			if (prop.indexOf("sel") >= 0) {
				// handle done adjusting or got something from a textfield
				// get the new values
				double minVal = 0.0f;
				double maxVal = 0.0f;
				try {
					DragHandle dh = (DragHandle)e.getSource();
					if (prop.indexOf("min") >= 0) {
						// get the max value of the new range too
						minVal = ((Double)e.getNewValue()).floatValue();
						maxVal = (double)dh.getFarNeighborValue();
					}
					else if (prop.indexOf("max") >= 0) {
						// get the min value of the new range too
						maxVal = ((Double)e.getNewValue()).floatValue();
						minVal = (double)dh.getFarNeighborValue();
					}
					if (prop.indexOf("ctr") >= 0) {
						// get the max value of the new range too
						if (prop.indexOf("z") >= 0) {
							maxVal = (double)dh.getMinNeighborValue();
							minVal = (double)dh.getMaxNeighborValue();
						}
						else  {
							minVal = (double)dh.getMinNeighborValue();
							maxVal = (double)dh.getMaxNeighborValue();
						}
					}
					
					// update the other handles and textfields in other views
					if (prop.indexOf("lat") >= 0) {
						// notify views that have a lat axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
							cutPanelViews[Constants.LAT_LON].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
							cutPanelViews[Constants.LAT_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
							cutPanelViews[Constants.LAT_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);
					}
					else if (prop.indexOf("lon") >= 0) {
						// notify views that have a lon axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
							cutPanelViews[Constants.LAT_LON].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
							cutPanelViews[Constants.LON_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
							cutPanelViews[Constants.LON_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);

					}
					else if (prop.indexOf("z") >= 0) {
						// notify views that have a depth axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
							cutPanelViews[Constants.LAT_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
							cutPanelViews[Constants.LON_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
							cutPanelViews[Constants.DEPTH_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					}
					else if (prop.indexOf("tim") >= 0) {
						// notify views that have a time axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
							cutPanelViews[Constants.LAT_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
							cutPanelViews[Constants.LON_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
							cutPanelViews[Constants.DEPTH_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					}
				}
				catch (ClassCastException ex) {
					// event came from a text field
					double newVal = 0.0f;
					try {
						newVal = (double)((Double)e.getNewValue()).doubleValue();
					}
					catch (ClassCastException ex2) {
						long lval = (long)(((Long)e.getNewValue()).longValue());
						newVal = (double)lval;
					}
					if (prop.indexOf("lat") >= 0) {
						if (prop.indexOf("min") >= 0) {
							minVal = newVal;
							if (prop.indexOf("X") >= 0)
								maxVal = (double)focusedView.getSelMaxXVal();
							else
								maxVal = (double)focusedView.getSelMaxYVal();
						}
						else {
							maxVal = newVal;
							if (prop.indexOf("X") >= 0)
								minVal = (double)focusedView.getSelMinXVal();
							else
								minVal = (double)focusedView.getSelMinYVal();
						}
					}
					if (prop.indexOf("lon") >= 0) {
						//boolean crossed180 = mCurrPC.crosses180();
						if (prop.indexOf("min") >= 0) {
							minVal = newVal;
							if (prop.indexOf("X") >= 0)
								maxVal = (double)focusedView.getSelMaxXVal();
							else
								maxVal = (double)focusedView.getSelMaxYVal();
						}
						else {
							maxVal = newVal;
							if (prop.indexOf("X") >= 0)
								minVal = (double)focusedView.getSelMinXVal();
							else
								minVal = (double)focusedView.getSelMinYVal();
						}
						/*if (prop.indexOf("min") >= 0) {
							if (crossed180) {
								minVal = newVal;
								maxVal = mCurrPC.getMaxWLon() + 360;
							}
							else {
								minVal = newVal;
								maxVal = lons[1];
							}
						}
						else {
							if (crossed180) {
								minVal = mCurrPC.getMinELon();
								maxVal = newVal;
							}
							else {
								minVal = lons[0];
								maxVal = newVal;
							}
						}*/
					}
					if (prop.indexOf("z") >= 0) {
						if (prop.indexOf("min") >= 0) {
							minVal = newVal;
							if (prop.indexOf("X") >= 0)
								maxVal = (double)focusedView.getSelMinXVal();
							else
								maxVal = (double)focusedView.getSelMinYVal();
						}
						else {
							maxVal = newVal;
							if (prop.indexOf("X") >= 0)
								minVal = (double)focusedView.getSelMaxXVal();
							else
								minVal = (double)focusedView.getSelMaxYVal();
						}
					}
					if (prop.indexOf("tim") >= 0) {
						if (prop.indexOf("min") >= 0) {
							minVal = newVal;
							if (prop.indexOf("X") >= 0)
								maxVal = (double)focusedView.getSelMaxXVal();
							else
								maxVal = (double)focusedView.getSelMaxYVal();
						}
						else {
							maxVal = newVal;
							if (prop.indexOf("X") >= 0)
								minVal = (double)focusedView.getSelMinXVal();
							else
								minVal = (double)focusedView.getSelMinYVal();
						}
					}
					
					// update the other handles and textfields in other views
					if (prop.indexOf("lat") >= 0) {
						// notify views that have a lat axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
							cutPanelViews[Constants.LAT_LON].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
							cutPanelViews[Constants.LAT_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
							cutPanelViews[Constants.LAT_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);
					}
					else if (prop.indexOf("lon") >= 0) {
						// notify views that have a lon axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
							cutPanelViews[Constants.LAT_LON].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
							cutPanelViews[Constants.LON_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
							cutPanelViews[Constants.LON_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);

					}
					else if (prop.indexOf("z") >= 0) {
						// notify views that have a depth axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
							cutPanelViews[Constants.LAT_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
							cutPanelViews[Constants.LON_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
							cutPanelViews[Constants.DEPTH_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					}
					else if (prop.indexOf("tim") >= 0) {
						// notify views that have a time axis
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
							cutPanelViews[Constants.LAT_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
							cutPanelViews[Constants.LON_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);

						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
							cutPanelViews[Constants.DEPTH_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					}
				}
				
				// set the filters for other views
				if (prop.indexOf("lat") >= 0) {
					double[] latRange = {minVal, maxVal};
					// notify views that don't have a lat axis to change their filter constraints
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(latRange, 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(latRange, 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(latRange, 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					/*if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(latRange, 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(latRange, 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(latRange, 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}*/
				}
				else if (prop.indexOf("lon") >= 0) {
					double[] lonRange = {minVal, maxVal};
					// notify views that don't have a lon axis to change their filter constraints
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							lonRange, 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							lonRange, 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							lonRange, 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					/*if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							lonRange, 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							lonRange, 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							lonRange, 
							currConstraints.getMinMaxDepth(), 
							currConstraints.getMinMaxTime(), null, null));
					}*/
				}
				else if (prop.indexOf("z") >= 0) {
					double[] zRange = {minVal, maxVal};
					// notify views that hdon't ave a depth axis to change their filter constraints
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							zRange, 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							zRange, 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							zRange, 
							currConstraints.getMinMaxTime(), null, null));
					}
					/*if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							zRange, 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							zRange, 
							currConstraints.getMinMaxTime(), null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							zRange, 
							currConstraints.getMinMaxTime(), null, null));
					}*/
				}
				else if (prop.indexOf("t") >= 0) {
					double[] tRange = {minVal, maxVal};
					// notify views that don't have a time axis to change their filter constraints
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							tRange, null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							tRange, null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							tRange, null, null));
					}
					/*if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							tRange, null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							tRange, null, null));
					}
					if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
						FilterConstraints currConstraints = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
						cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
							new FilterConstraints(currConstraints.getMinMaxLat(), 
							currConstraints.getMinMaxLon(), 
							currConstraints.getMinMaxDepth(), 
							tRange, null, null));
					}*/
				}
				
				// set the text fields
				if (prop.indexOf("lat") >= 0) {
					// notify views that have a lat axis
					if (prop.indexOf("ctr") >= 0) {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
							cutPanelViews[Constants.LAT_LON].setFieldValues("latmin", new Double(minVal));
							cutPanelViews[Constants.LAT_LON].setFieldValues("latmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
							cutPanelViews[Constants.LAT_TIME].setFieldValues("latmin", new Double(minVal));
							cutPanelViews[Constants.LAT_TIME].setFieldValues("latmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
							cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmin", new Double(minVal));
							cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmax", new Double(maxVal));
						}
					}
					else {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
							cutPanelViews[Constants.LAT_LON].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
							cutPanelViews[Constants.LAT_TIME].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
							cutPanelViews[Constants.LAT_DEPTH].setFieldValues(prop, e.getNewValue());
					}
				}
				else if (prop.indexOf("lon") >= 0) {
					// notify views that have a lon axis
					if (prop.indexOf("ctr") >= 0) {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
							cutPanelViews[Constants.LAT_LON].setFieldValues("lonmin", new Double(minVal));
							cutPanelViews[Constants.LAT_LON].setFieldValues("lonmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
							cutPanelViews[Constants.LON_TIME].setFieldValues("lonmin", new Double(minVal));
							cutPanelViews[Constants.LON_TIME].setFieldValues("lonmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
							cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmin", new Double(minVal));
							cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmax", new Double(maxVal));
						}
					}
					else {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
							cutPanelViews[Constants.LAT_LON].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
							cutPanelViews[Constants.LON_TIME].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
							cutPanelViews[Constants.LON_DEPTH].setFieldValues(prop, e.getNewValue());
					}
				}
				else if (prop.indexOf("z") >= 0) {
					// notify views that have a depth axis
					if (prop.indexOf("ctr") >= 0) {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
							cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmin", new Double(minVal));
							cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
							cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmin", new Double(minVal));
							cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
							cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmin", new Double(minVal));
							cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmax", new Double(maxVal));
						}
					}
					else {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
							cutPanelViews[Constants.LAT_DEPTH].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
							cutPanelViews[Constants.LON_DEPTH].setFieldValues(prop, e.getNewValue());
							
						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
							cutPanelViews[Constants.DEPTH_TIME].setFieldValues(prop, e.getNewValue());
					}
				}
				else if (prop.indexOf("tim") >= 0) {
					// notify views that have a time axis
					if (prop.indexOf("ctr") >= 0) {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
							cutPanelViews[Constants.LAT_TIME].setFieldValues("timmin", new Double(minVal));
							cutPanelViews[Constants.LAT_TIME].setFieldValues("timmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
							cutPanelViews[Constants.LON_TIME].setFieldValues("timmin", new Double(minVal));
							cutPanelViews[Constants.LON_TIME].setFieldValues("timmax", new Double(maxVal));
						}
						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
							cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmin", new Double(minVal));
							cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmax", new Double(maxVal));
						}
					}
					else {
						if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
							cutPanelViews[Constants.LAT_TIME].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
							cutPanelViews[Constants.LON_TIME].setFieldValues(prop, e.getNewValue());

						if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
							cutPanelViews[Constants.DEPTH_TIME].setFieldValues(prop, e.getNewValue());
					}
				}
				if (!mInBatch)
					invalidateAllViews();
			}
			else {
				// handle adjusting
				if (prop.indexOf("lat") >= 0) {
					// notify views that have a lat axis
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
						cutPanelViews[Constants.LAT_LON].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
						cutPanelViews[Constants.LAT_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
						cutPanelViews[Constants.LAT_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);
				}
				else if (prop.indexOf("lon") >= 0) {
					// notify views that have a lon axis
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
						cutPanelViews[Constants.LAT_LON].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
						cutPanelViews[Constants.LON_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
						cutPanelViews[Constants.LON_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);
				}
				else if (prop.indexOf("z") >= 0) {
					// notify views that have a depth axis
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
						cutPanelViews[Constants.LAT_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
						cutPanelViews[Constants.LON_DEPTH].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
						cutPanelViews[Constants.DEPTH_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
				}
				else if (prop.indexOf("t") >= 0) {
					// notify views that have a time axis
					if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
						cutPanelViews[Constants.LAT_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
						cutPanelViews[Constants.LON_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
					if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
						cutPanelViews[Constants.DEPTH_TIME].setHandle(e.getSource(), prop, e.getNewValue(), true);
				}
			}
		}
	}
	
	public void popChangeableInfo(ChangeableInfo ci) {
		double newVal = 0.0f;
		long lval = 0;
		try {
			newVal = ((Float)(ci.getNewValue())).floatValue();
		}
		catch (Exception ex) {
			//lval = ((Long)(ci.getNewValue())).longValue();
			//newVal = (double)lval;
		}
		if (ci.getId().equals("StartBatch")) {
			mInBatch = true;
			return;
		}
		else if (ci.getId().equals("EndBatch")) {
			mInBatch = false;
			invalidateAllViews();
			return;
		}

		// Zooming Changeables
		if (ci.getId().equals("ZoomLatitudeDomain")) {
			double[] oldLats = (double[])ci.getOldValue();
			double[] newLats = (double[])ci.getNewValue();
			
			// reset the axis range
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setMinYVal(newLats[0]);
				cutPanelViews[Constants.LAT_LON].setMaxYVal(newLats[1]);
				cutPanelViews[Constants.LAT_LON].scaleYAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setMinYVal(newLats[0]);
				cutPanelViews[Constants.LAT_TIME].setMaxYVal(newLats[1]);
				cutPanelViews[Constants.LAT_TIME].scaleYAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setMinXVal(newLats[0]);
				cutPanelViews[Constants.LAT_DEPTH].setMaxXVal(newLats[1]);
				cutPanelViews[Constants.LAT_DEPTH].scaleXAxis();
			}
			
			// recompute the positions of the handles
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandleVals("lat", newLats[0], newLats[1]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandleVals("lat", newLats[0], newLats[1]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandleVals("lat", newLats[0], newLats[1]);
			} 
		}
		else if (ci.getId().equals("ZoomLongitudeDomain")) {
			double[] oldLons = (double[])ci.getOldValue();
			double[] newLons = (double[])ci.getNewValue();
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setMinXVal(newLons[0]);
				cutPanelViews[Constants.LAT_LON].setMaxXVal(newLons[1]);
				cutPanelViews[Constants.LAT_LON].scaleXAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setMinYVal(newLons[0]);
				cutPanelViews[Constants.LON_TIME].setMaxYVal(newLons[1]);
				cutPanelViews[Constants.LON_TIME].scaleYAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setMinXVal(newLons[0]);
				cutPanelViews[Constants.LON_DEPTH].setMaxXVal(newLons[1]);
				cutPanelViews[Constants.LON_DEPTH].scaleXAxis();
			}
			
			// recompute the positions of the handles
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandleVals("lon", newLons[0], newLons[1]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandleVals("lon", newLons[0], newLons[1]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandleVals("lon", newLons[0], newLons[1]);
			} 
		}
		else if (ci.getId().equals("ZoomDepthDomain")) {
			double[] oldzees = (double[])ci.getOldValue();
			double[] newzees = (double[])ci.getNewValue();
			
			double minZ = newzees[0] <= newzees[1] ? newzees[0] : newzees[1];
			double maxZ = newzees[0] >= newzees[1] ? newzees[0] : newzees[1];
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setMinYVal(newzees[1]);
				cutPanelViews[Constants.LAT_DEPTH].setMaxYVal(newzees[0]);
				cutPanelViews[Constants.LAT_DEPTH].scaleYAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setMinYVal(newzees[1]);
				cutPanelViews[Constants.LON_DEPTH].setMaxYVal(newzees[0]);
				cutPanelViews[Constants.LON_DEPTH].scaleYAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMinYVal(newzees[1]);
				cutPanelViews[Constants.DEPTH_TIME].setMaxYVal(newzees[0]);
				cutPanelViews[Constants.DEPTH_TIME].scaleYAxis();
			}
			
			// recompute the positions of the handles
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandleVals("depth", newzees[1], newzees[0]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandleVals("depth", newzees[1], newzees[0]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandleVals("depth", newzees[1], newzees[0]);
			} 
		}
		else if (ci.getId().equals("ZoomTimeDomain")) {
			double[] oldtees = (double[])ci.getOldValue();
			double[] newtees = (double[])ci.getNewValue();
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setMinXVal(newtees[0]);
				cutPanelViews[Constants.LAT_TIME].setMaxXVal(newtees[1]);
				cutPanelViews[Constants.LAT_TIME].scaleXAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setMinXVal(newtees[0]);
				cutPanelViews[Constants.LON_TIME].setMaxXVal(newtees[1]);
				cutPanelViews[Constants.LON_TIME].scaleXAxis();
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMinXVal(newtees[0]);
				cutPanelViews[Constants.DEPTH_TIME].setMaxXVal(newtees[1]);
				cutPanelViews[Constants.DEPTH_TIME].scaleXAxis();
			}
			
			// recompute the positions of the handles
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandleVals("time", newtees[0], newtees[1]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandleVals("time", newtees[0], newtees[1]);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandleVals("time", newtees[0], newtees[1]);
			}
		}
		/*// rescale the axes and tell the handles to rescale
		if (ci.getId().equals("ZoomLatitudeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setMinYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setMinYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setMinXVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomLatitudeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setMaxYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setMaxYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setMaxXVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomLongitudeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setMinXVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setMinYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setMinXVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomLongitudeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setMaxXVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setMaxYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setMaxXVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomDepthStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setMinYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setMinYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMinYVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomDepthStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setMaxYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setMaxYVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMaxYVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomTimeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setMinXVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setMinXVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMinXVal(newVal);
			}
		}
		else if (ci.getId().equals("ZoomTimeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setMaxXVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setMaxXVal(newVal);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMaxXVal(newVal);
			}
		}
		// compute new handle locations
		if (ci.getId().equals("ZoomLatitudeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandleVal("latmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandleVal("latmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandleVal("latmin", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomLatitudeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandleVal("latmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandleVal("latmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandleVal("latmax", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomLongitudeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandleVal("lonmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandleVal("lonmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandleVal("lonmin", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomLongitudeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandleVal("lonmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandleVal("lonmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandleVal("lonmax", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomDepthStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandleVal("zmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandleVal("zmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setMinYVal(newVal);
				cutPanelViews[Constants.DEPTH_TIME].setHandleVal("zmin", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomDepthStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandleVal("zmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandleVal("zmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandleVal("zmax", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomTimeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandleVal("timmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandleVal("timmin", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandleVal("timmin", ci.getNewValue());
			}
		}
		else if (ci.getId().equals("ZoomTimeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandleVal("timmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandleVal("timmax", ci.getNewValue());
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandleVal("timmax", ci.getNewValue());
			}
		}*/
				
		// reset the handles to the new values
		if (ci.getId().equals("ZoomLatitudeStartHandle")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "latmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "latmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "latmin", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("ZoomLatitudeStopHandle")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "latmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "latmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "latmax", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("ZoomLongitudeStartHandle")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "lonmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandle(null, "lonmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "lonmin", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("ZoomLongitudeStopHandle")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "lonmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandle(null, "lonmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "lonmax", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("ZoomDepthStartHandle")) {
			double dval = (double)(((Double)ci.getNewValue()).floatValue());
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "zmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "zmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "zmin", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("ZoomDepthStopHandle")) {
			double dval = (double)(((Double)ci.getNewValue()).floatValue());
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "zmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "zmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "zmax", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("ZoomTimeStartHandle")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "timmin", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
				cutPanelViews[Constants.LON_TIME].setHandle(null, "timmin", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "timmin", ci.getNewValue(), true);
		}
		else if (ci.getId().equals("ZoomTimeStopHandle")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "timmax", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
				cutPanelViews[Constants.LON_TIME].setHandle(null, "timmax", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "timmax", ci.getNewValue(), true);
		}
		
		// now reset the handles from text field selections
		if (ci.getId().equals("SelLatitudeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "latmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "latmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "latmin", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("SelLatitudeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "latmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "latmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "latmax", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("SelLongitudeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "lonmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandle(null, "lonmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "lonmin", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("SelLongitudeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setHandle(null, "lonmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setHandle(null, "lonmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "lonmax", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("SelDepthStart")) {
			double dval = (double)(((Double)ci.getNewValue()).floatValue());
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "zmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "zmin", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "zmin", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("SelDepthStop")) {
			double dval = (double)(((Double)ci.getNewValue()).floatValue());
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setHandle(null, "zmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setHandle(null, "zmax", ci.getNewValue(), true);
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "zmax", ci.getNewValue(), true);
			}
		}
		else if (ci.getId().equals("SelTimeStart")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "timmin", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
				cutPanelViews[Constants.LON_TIME].setHandle(null, "timmin", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "timmin", ci.getNewValue(), true);
		}
		else if (ci.getId().equals("SelTimeStop")) {
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
				cutPanelViews[Constants.LAT_TIME].setHandle(null, "timmax", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
				cutPanelViews[Constants.LON_TIME].setHandle(null, "timmax", ci.getNewValue(), true);
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
				cutPanelViews[Constants.DEPTH_TIME].setHandle(null, "timmax", ci.getNewValue(), true);
		}
		if (!mInBatch) {
			this.resetSelectionRgns();
			invalidateAllViews();
		}
	}
	
	public void resetSelectionRgns() {
		for (int i=0; i<Constants.MAX_CUT_PANEL_TYPES; i++) {
			if (cutPanelViews[i] == null)
				continue;
			if (visibleViews.indexOf(cutPanelViews[i]) >= 0) {
				SelectionRegion sr = cutPanelViews[i].getSelectionRegion();
				if (sr != null)
					sr.resetRgnBounds();
			}
		}
	}
	
	public void resetZoom() {
		// change the filters for each of the views to be the same as the pointer collection
		// called by undo all
		cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(mCurrPC.getMinMaxLat(), 
							mCurrPC.getMinMaxLon(), mCurrPC.getMinMaxDepth(), mCurrPC.getMinMaxTime(), null, null));
		cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(mCurrPC.getMinMaxLat(), 
							mCurrPC.getMinMaxLon(), mCurrPC.getMinMaxDepth(), mCurrPC.getMinMaxTime(), null, null));
		cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(mCurrPC.getMinMaxLat(), 
							mCurrPC.getMinMaxLon(), mCurrPC.getMinMaxDepth(), mCurrPC.getMinMaxTime(), null, null));
		cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(mCurrPC.getMinMaxLat(), 
							mCurrPC.getMinMaxLon(), mCurrPC.getMinMaxDepth(), mCurrPC.getMinMaxTime(), null, null));
		cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(mCurrPC.getMinMaxLat(), 
							mCurrPC.getMinMaxLon(), mCurrPC.getMinMaxDepth(), mCurrPC.getMinMaxTime(), null, null));
		cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(mCurrPC.getMinMaxLat(), 
							mCurrPC.getMinMaxLon(), mCurrPC.getMinMaxDepth(), mCurrPC.getMinMaxTime(), null, null));

	}
	
	/*public void zoomAllViews() {
		// zoom each view to the range specified by their handles 
		// no filtering occurs here
		// since views are linked, we only need to get the range for each axis once.
		boolean gotLatRange = false;
		boolean gotLonRange = false;
		boolean gotTimeRange = false;
		boolean gotDepthRange = false;
		boolean latRangeChanged = false;
		boolean lonRangeChanged = false;
		boolean timeRangeChanged = false;
		boolean depthRangeChanged = false;
		
		double oldMaxZ = 0.0f;
		double oldMinZ = 0.0f;
		double oldMaxLat = 0.0f;
		double oldMinLat = 0.0f;
		double oldMaxLon = 0.0f;
		double oldMinLon = 0.0f;
		double oldMaxT = 0.0f;
		double oldMinT = 0.0f;
		double newMaxZ = 0.0f;
		double newMinZ = 0.0f;
		double newMaxLat = 0.0f;
		double newMinLat = 0.0f;
		double newMaxLon = 0.0f;
		double newMinLon = 0.0f;
		double newMaxT = 0.0f;
		double newMinT = 0.0f;
			
		double[] oldlats = new double[2];
		double[] newlats = new double[2]; 
		double[] oldlons = new double[2];
		double[] newlons = new double[2]; 
		double[] oldtimes = new double[2];
		double[] newtimes = new double[2]; 
		double[] oldzees = new double[2];
		double[] newzees = new double[2]; 
		
		newlats[0] = (double)cutPanelViews[Constants.LAT_LON].getMinYVal();
		newlats[1] = (double)cutPanelViews[Constants.LAT_LON].getMaxYVal(); 
		newlons[0] = (double)cutPanelViews[Constants.LAT_LON].getMinXVal();
		newlons[1] = (double)cutPanelViews[Constants.LAT_LON].getMinXVal();
		newtimes[0] = (double)cutPanelViews[Constants.LAT_TIME].getSelMinXVal();
		newtimes[1] = (double)cutPanelViews[Constants.LAT_TIME].getSelMaxXVal();
		newzees[0] = (double)cutPanelViews[Constants.LAT_DEPTH].getSelMinYVal();
		newzees[1] = (double)cutPanelViews[Constants.LAT_DEPTH].getSelMaxYVal();
			
		
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
			// get the current range of the axis
			oldMinLat = cutPanelViews[Constants.LAT_LON].getMinYVal();
			oldMaxLat = cutPanelViews[Constants.LAT_LON].getMaxYVal();
			oldMinLon = cutPanelViews[Constants.LAT_LON].getMinXVal();
			oldMaxLon = cutPanelViews[Constants.LAT_LON].getMaxXVal();
			
			// get the range of the handles
			newMinLat = (double)cutPanelViews[Constants.LAT_LON].getSelMinYVal();
			newMaxLat = (double)cutPanelViews[Constants.LAT_LON].getSelMaxYVal();
			newMinLon = (double)cutPanelViews[Constants.LAT_LON].getSelMinXVal();
			newMaxLon = (double)cutPanelViews[Constants.LAT_LON].getSelMaxXVal();
			
			oldlats[0] = oldMinLat;
			oldlats[1] = oldMaxLat;
			newlats[0] = newMinLat;
			newlats[1] = newMaxLat; 
			oldlons[0] = oldMinLon; 
			oldlons[1] = oldMaxLon;
			newlons[0] = newMinLon;
			newlons[1] = newMaxLon; 
			
			if (oldMinLat != newMinLat || oldMaxLat != newMaxLat)
				latRangeChanged = true;

			if (oldMinLon != newMinLon || oldMaxLon != newMaxLon)
				lonRangeChanged = true;
			gotLatRange = true;
			gotLonRange = true;
		}
		
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
			if (!gotLatRange) {
				// get the current range of the axis
				oldMinLat = cutPanelViews[Constants.LAT_TIME].getMinYVal();
				oldMaxLat = cutPanelViews[Constants.LAT_TIME].getMaxYVal();
				oldlats[0] = oldMinLat;
				oldlats[1] = oldMaxLat;
				
				// get the range of the handles
				newMinLat = (double)cutPanelViews[Constants.LAT_TIME].getSelMinYVal();
				newMaxLat = (double)cutPanelViews[Constants.LAT_TIME].getSelMaxYVal();
				newlats[0] = newMinLat;
				newlats[1] = newMaxLat; 
				
				if (oldMinLat != newMinLat || oldMaxLat != newMaxLat)
					latRangeChanged = true;
				gotLatRange = true;
			}
			if (!gotTimeRange) {
				// get the current range of the axis
				oldMinT = cutPanelViews[Constants.LAT_TIME].getMinXVal();
				oldMaxT = cutPanelViews[Constants.LAT_TIME].getMaxXVal();
				oldtimes[0] = oldMinT;
				oldtimes[1] = oldMaxT;
				
				// get the range of the handles
				newMinT = (double)cutPanelViews[Constants.LAT_TIME].getSelMinXVal();
				newMaxT = (double)cutPanelViews[Constants.LAT_TIME].getSelMaxXVal();
				newtimes[0] = newMinT;
				newtimes[1] = newMaxT; 
				
				if (oldMinT != newMinT || oldMaxT != newMaxT)
					timeRangeChanged = true;
				gotTimeRange = true;
			}
		}
		
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
			if (!gotLatRange) {
				// get the current range of the axis
				oldMinLat = cutPanelViews[Constants.LAT_DEPTH].getMinXVal();
				oldMaxLat = cutPanelViews[Constants.LAT_DEPTH].getMaxXVal();
				oldlats[0] = oldMinLat;
				oldlats[1] = oldMaxLat;

				// get the range of the handles
				newMinLat = (double)cutPanelViews[Constants.LAT_DEPTH].getSelMinXVal();
				newMaxLat = (double)cutPanelViews[Constants.LAT_DEPTH].getSelMaxXVal();
				newlats[0] = newMinLat;
				newlats[1] = newMaxLat; 
				
				if (oldMinLat != newMinLat || oldMaxLat != newMaxLat)
					latRangeChanged = true;
				gotLatRange = true;
			}
			if (!gotDepthRange) {
				// get the current range of the axis
				oldMinZ = cutPanelViews[Constants.LAT_DEPTH].getMinYVal();
				oldMaxZ = cutPanelViews[Constants.LAT_DEPTH].getMaxYVal();
				oldzees[0] = oldMinZ;
				oldzees[1] = oldMaxZ;
				
				// get the range of the handles
				newMinZ = (double)cutPanelViews[Constants.LAT_DEPTH].getSelMinYVal();
				newMaxZ = (double)cutPanelViews[Constants.LAT_DEPTH].getSelMaxYVal();
				newzees[0] = newMinZ;
				newzees[1] = newMaxZ; 
				
				if (oldMinZ != newMinZ || oldMaxZ != newMaxZ)
					depthRangeChanged = true;
				gotDepthRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
			if (!gotLonRange) {
				// get the current range of the axis
				oldMinLon = cutPanelViews[Constants.LON_DEPTH].getMinXVal();
				oldMaxLon = cutPanelViews[Constants.LON_DEPTH].getMaxXVal();
				oldlons[0] = oldMinLon; 
				oldlons[1] = oldMaxLon;
				
				// get the range of the handles
				newMinLon = (double)cutPanelViews[Constants.LON_DEPTH].getSelMinXVal();
				newMaxLon = (double)cutPanelViews[Constants.LON_DEPTH].getSelMaxXVal();
				newlons[0] = newMinLon;
				newlons[1] = newMaxLon; 
				
				if (oldMinLon != newMinLon || oldMaxLon != newMaxLon)
					lonRangeChanged = true;
				gotLonRange = true;
			}
			if (!gotDepthRange) {
				// get the current range of the axis
				oldMinZ = cutPanelViews[Constants.LON_DEPTH].getMinYVal();
				oldMaxZ = cutPanelViews[Constants.LON_DEPTH].getMaxYVal();
				oldzees[0] = oldMinZ;
				oldzees[1] = oldMaxZ;
				
				// get the range of the handles
				newMinZ = (double)cutPanelViews[Constants.LON_DEPTH].getSelMinYVal();
				newMaxZ = (double)cutPanelViews[Constants.LON_DEPTH].getSelMaxYVal();
				newzees[0] = newMinZ;
				newzees[1] = newMaxZ; 
				
				if (oldMinZ != newMinZ || oldMaxZ != newMaxZ)
					depthRangeChanged = true;
				gotDepthRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
			if (!gotTimeRange) {
				// get the current range of the axis
				oldMinT = cutPanelViews[Constants.DEPTH_TIME].getMinXVal();
				oldMaxT = cutPanelViews[Constants.DEPTH_TIME].getMaxXVal();
				oldtimes[0] = oldMinT;
				oldtimes[1] = oldMaxT;
				
				// get the range of the handles
				newMinT = (double)cutPanelViews[Constants.DEPTH_TIME].getSelMinXVal();
				newMaxT = (double)cutPanelViews[Constants.DEPTH_TIME].getSelMaxXVal();
				newtimes[0] = newMinT;
				newtimes[1] = newMaxT; 
				
				if (oldMinT != newMinT || oldMaxT != newMaxT)
					timeRangeChanged = true;
				gotTimeRange = true;
			}
			if (!gotDepthRange) {
				// get the current range of the axis
				oldMinZ = cutPanelViews[Constants.DEPTH_TIME].getMinYVal();
				oldMaxZ = cutPanelViews[Constants.DEPTH_TIME].getMaxYVal();
				oldzees[0] = oldMinZ;
				oldzees[1] = oldMaxZ;
				
				// get the range of the handles
				newMinZ = (double)cutPanelViews[Constants.DEPTH_TIME].getSelMinYVal();
				newMaxZ = (double)cutPanelViews[Constants.DEPTH_TIME].getSelMaxYVal();
				newzees[0] = newMinZ;
				newzees[1] = newMaxZ; 
				
				if (oldMinZ != newMinZ || oldMaxZ != newMaxZ)
					depthRangeChanged = true;
				gotDepthRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
			if (!gotTimeRange) {
				// get the current range of the axis
				oldMinT = cutPanelViews[Constants.LON_TIME].getMinXVal();
				oldMaxT = cutPanelViews[Constants.LON_TIME].getMaxXVal();
				oldtimes[0] = oldMinT;
				oldtimes[1] = oldMaxT;
				
				// get the range of the handles
				newMinT = (double)cutPanelViews[Constants.LON_TIME].getSelMinXVal();
				newMaxT = (double)cutPanelViews[Constants.LON_TIME].getSelMaxXVal();
				newtimes[0] = newMinT;
				newtimes[1] = newMaxT; 
				
				if (oldMinT != newMinT || oldMaxT != newMaxT)
					timeRangeChanged = true;
				gotTimeRange = true;
			}
			if (!gotLonRange) {
				// get the current range of the axis
				oldMinLon = cutPanelViews[Constants.LON_TIME].getMinYVal();
				oldMaxLon = cutPanelViews[Constants.LON_TIME].getMaxYVal();
				oldlons[0] = oldMinLon; 
				oldlons[1] = oldMaxLon;
				
				// get the range of the handles
				newMinLon = (double)cutPanelViews[Constants.LON_TIME].getSelMinYVal();
				newMaxLon = (double)cutPanelViews[Constants.LON_TIME].getSelMaxYVal();
				newlons[0] = newMinLon;
				newlons[1] = newMaxLon; 
				
				if (oldMinLon != newMinLon || oldMaxLon != newMaxLon)
					lonRangeChanged = true;
				gotLonRange = true;
			}
		}
		
		cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
							newlons, newzees, newtimes, null, null));
		cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
							newlons, newzees, newtimes, null, null));
		cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
							newlons, newzees, newtimes, null, null));
		cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
								newlons, newzees, newtimes, null, null));
		cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
							newlons, newzees, newtimes, null, null));
		cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
							newlons, newzees, newtimes, null, null));
		cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(newlats, 
							newlons, newzees, newtimes, null, null));
							
		// post changeables to do the zooming 
		if (latRangeChanged || lonRangeChanged || timeRangeChanged || depthRangeChanged)
			((NdEdit)this.getParent()).pushChangeableInfo("StartBatch", null, null, false);
		if (latRangeChanged) {   
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldlats, newlats, false);
	    }
	    
	    if (lonRangeChanged) {   
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomLongitudeDomain", oldlons, newlons, false);
	    }
	    
	    if (timeRangeChanged) {   
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomTimeDomain", oldtimes, newtimes, false);
		}
	    
	    if (depthRangeChanged) { 
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomDepthDomain", oldzees, newzees, false);
    	}
		if (latRangeChanged || lonRangeChanged || timeRangeChanged || depthRangeChanged)
			((NdEdit)this.getParent()).pushChangeableInfo("StartBatch", null, null, false);
    	//selectAll();

    	this.invalidateAllViews();
	}*/
	
	public void zoomAllViews() {
		// zoom each view to the range specified by their handles 
		// no filtering occurs here
		// since views are linked, we only need to get the range for each axis once.
		boolean gotLatRange = false;
		boolean gotLonRange = false;
		boolean gotTimeRange = false;
		boolean gotDepthRange = false;
		boolean latRangeChanged = false;
		boolean lonRangeChanged = false;
		boolean timeRangeChanged = false;
		boolean depthRangeChanged = false;
		
		double oldMaxZ = 0.0;
		double oldMinZ = 0.0;
		double oldMaxLat = 0.0;
		double oldMinLat = 0.0;
		double oldMaxLon = 0.0;
		double oldMinLon = 0.0;
		double oldMaxT = 0.0;
		double oldMinT = 0.0;
		double newMaxZ = 0.0;
		double newMinZ = 0.0;
		double newMaxLat = 0.0;
		double newMinLat = 0.0;
		double newMaxLon = 0.0;
		double newMinLon = 0.0;
		double newMaxT = 0.0;
		double newMinT = 0.0;
		
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
			// get the current range of the axis
			oldMinLat = cutPanelViews[Constants.LAT_LON].getMinYVal();
			oldMaxLat = cutPanelViews[Constants.LAT_LON].getMaxYVal();
			oldMinLon = cutPanelViews[Constants.LAT_LON].getMinXVal();
			oldMaxLon = cutPanelViews[Constants.LAT_LON].getMaxXVal();
			
			// get the range of the handles
			newMinLat = cutPanelViews[Constants.LAT_LON].getSelMinYVal();
			newMaxLat = cutPanelViews[Constants.LAT_LON].getSelMaxYVal();
			newMinLon = cutPanelViews[Constants.LAT_LON].getSelMinXVal();
			newMaxLon = cutPanelViews[Constants.LAT_LON].getSelMaxXVal();
			
			if (oldMinLat != newMinLat)
				latRangeChanged = true;

			if (oldMinLon != newMinLon)
				lonRangeChanged = true;
			gotLatRange = true;
			gotLonRange = true;
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
			if (!gotLatRange) {
				// get the current range of the axis
				oldMinLat = cutPanelViews[Constants.LAT_TIME].getMinYVal();
				oldMaxLat = cutPanelViews[Constants.LAT_TIME].getMaxYVal();
				
				// get the range of the handles
				newMinLat = cutPanelViews[Constants.LAT_TIME].getSelMinYVal();
				newMaxLat = cutPanelViews[Constants.LAT_TIME].getSelMaxYVal();
				
				if (oldMinLat != newMinLat || oldMaxLat != newMaxLat)
					latRangeChanged = true;
				gotLatRange = true;
			}
			if (!gotTimeRange) {
				// get the current range of the axis
				oldMinT = cutPanelViews[Constants.LAT_TIME].getMinXVal();
				oldMaxT = cutPanelViews[Constants.LAT_TIME].getMaxXVal();
				
				// get the range of the handles
				newMinT = cutPanelViews[Constants.LAT_TIME].getSelMinXVal();
				newMaxT = cutPanelViews[Constants.LAT_TIME].getSelMaxXVal();
				
				if (oldMinT != newMinT || oldMaxT != newMaxT)
					timeRangeChanged = true;
				gotTimeRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
			if (!gotLatRange) {
				// get the current range of the axis
				oldMinLat = cutPanelViews[Constants.LAT_DEPTH].getMinXVal();
				oldMaxLat = cutPanelViews[Constants.LAT_DEPTH].getMaxXVal();
				
				// get the range of the handles
				newMinLat = cutPanelViews[Constants.LAT_DEPTH].getSelMinXVal();
				newMaxLat = cutPanelViews[Constants.LAT_DEPTH].getSelMaxXVal();
				
				if (oldMinLat != newMinLat || oldMaxLat != newMaxLat)
					latRangeChanged = true;
				gotLatRange = true;
			}
			if (!gotDepthRange) {
				// get the current range of the axis
				oldMinZ = cutPanelViews[Constants.LAT_DEPTH].getMinYVal();
				oldMaxZ = cutPanelViews[Constants.LAT_DEPTH].getMaxYVal();
				
				// get the range of the handles
				newMinZ = cutPanelViews[Constants.LAT_DEPTH].getSelMinYVal();
				newMaxZ = cutPanelViews[Constants.LAT_DEPTH].getSelMaxYVal();
				
				if (oldMinZ != newMinZ || oldMaxZ != newMaxZ)
					depthRangeChanged = true;
				gotDepthRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
			if (!gotLonRange) {
				// get the current range of the axis
				oldMinLon = cutPanelViews[Constants.LON_DEPTH].getMinXVal();
				oldMaxLon = cutPanelViews[Constants.LON_DEPTH].getMaxXVal();
				
				// get the range of the handles
				newMinLon = cutPanelViews[Constants.LON_DEPTH].getSelMinXVal();
				newMaxLon = cutPanelViews[Constants.LON_DEPTH].getSelMaxXVal();
				
				if (oldMinLon != newMinLon || oldMaxLon != newMaxLon)
					lonRangeChanged = true;
				gotLonRange = true;
			}
			if (!gotDepthRange) {
				// get the current range of the axis
				oldMinZ = cutPanelViews[Constants.LON_DEPTH].getMinYVal();
				oldMaxZ = cutPanelViews[Constants.LON_DEPTH].getMaxYVal();
				
				// get the range of the handles
				newMinZ = cutPanelViews[Constants.LON_DEPTH].getSelMinYVal();
				newMaxZ = cutPanelViews[Constants.LON_DEPTH].getSelMaxYVal();
				
				if (oldMinZ != newMinZ || oldMaxZ != newMaxZ)
					depthRangeChanged = true;
				gotDepthRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
			if (!gotTimeRange) {
				// get the current range of the axis
				oldMinT = cutPanelViews[Constants.DEPTH_TIME].getMinXVal();
				oldMaxT = cutPanelViews[Constants.DEPTH_TIME].getMaxXVal();
				
				// get the range of the handles
				newMinT = cutPanelViews[Constants.DEPTH_TIME].getSelMinXVal();
				newMaxT = cutPanelViews[Constants.DEPTH_TIME].getSelMaxXVal();
				
				if (oldMinT != newMinT || oldMaxT != newMaxT)
					timeRangeChanged = true;
				gotTimeRange = true;
			}
			if (!gotDepthRange) {
				// get the current range of the axis
				oldMinZ = cutPanelViews[Constants.DEPTH_TIME].getMinYVal();
				oldMaxZ = cutPanelViews[Constants.DEPTH_TIME].getMaxYVal();
				
				// get the range of the handles
				newMinZ = cutPanelViews[Constants.DEPTH_TIME].getSelMinYVal();
				newMaxZ = cutPanelViews[Constants.DEPTH_TIME].getSelMaxYVal();
				
				if (oldMinZ != newMinZ || oldMaxZ != newMaxZ)
					depthRangeChanged = true;
				gotDepthRange = true;
			}
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
			if (!gotTimeRange) {
				// get the current range of the axis
				oldMinT = cutPanelViews[Constants.LON_TIME].getMinXVal();
				oldMaxT = cutPanelViews[Constants.LON_TIME].getMaxXVal();
				
				// get the range of the handles
				newMinT = cutPanelViews[Constants.LON_TIME].getSelMinXVal();
				newMaxT = cutPanelViews[Constants.LON_TIME].getSelMaxXVal();
				
				if (oldMinT != newMinT || oldMaxT != newMaxT)
					timeRangeChanged = true;
				gotTimeRange = true;
			}
			if (!gotLonRange) {
				// get the current range of the axis
				oldMinLon = cutPanelViews[Constants.LON_TIME].getMinYVal();
				oldMaxLon = cutPanelViews[Constants.LON_TIME].getMaxYVal();
				
				// get the range of the handles
				newMinLon = cutPanelViews[Constants.LON_TIME].getSelMinYVal();
				newMaxLon = cutPanelViews[Constants.LON_TIME].getSelMaxYVal();
				
				if (oldMinLon != newMinLon || oldMaxLon != newMaxLon)
					lonRangeChanged = true;
				gotLonRange = true;
			}
		}
		
		// post changeables to do the zooming 
		if (latRangeChanged) {
			double [] oldlats = {oldMinLat, oldMaxLat};
			double [] newlats = {newMinLat, newMaxLat};    
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldlats, newlats, false);
	    }
	    
	    if (lonRangeChanged) {
			double [] oldlons = {oldMinLon, oldMaxLon};
			double [] newlons = {newMinLon, newMaxLon};    
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomLongitudeDomain", oldlons, newlons, false);
	    }
	    
	    if (timeRangeChanged) {
			double [] oldtimes = {oldMinT, oldMaxT};
			double [] newtimes = {newMinT, newMaxT};    
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomTimeDomain", oldtimes, newtimes, false);
		}
	    
	    if (depthRangeChanged) {
			double [] oldzees = {oldMinZ, oldMaxZ};
			double [] newzees = {newMinZ, newMaxZ};  
			((NdEdit)this.getParent()).pushChangeableInfo("ZoomDepthDomain", oldzees, newzees, false);
    	}
    	//selectAll();
    	this.invalidateAllViews();
	}
	public void selectAll() {
		// changes the handle range to axes ranges for the focused view
		double newMinY = focusedView.getMinYVal();
		double newMaxY = focusedView.getMaxYVal();
		double newMinX = focusedView.getMinXVal();
		double newMaxX = focusedView.getMaxXVal();
		String prop = null;
		String propXMin = null;
		String propXMax = null;
		String propYMin = null;
		String propYMax = null;
		String propYCtr = null;
		String propXCtr = null;
		
		double newCtrY = newMinY + ((newMaxY - newMinY)/2.0f);
		double newCtrX = newMinX + ((newMaxX - newMinX)/2.0f);

		if (focusedView instanceof LatLonView) {
			propXMin = "lonmin";
			propXMax = "lonmax";
			propYMin = "latmin";
			propYMax = "latmax";
			propYCtr = "latctr";
			propXCtr = "lonctr";
			
			
			double[] latRange = {newMinY, newMaxY};
			double[] lonRange = {newMinX, newMaxX};
			
			FilterConstraints fc = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				lonRange, 
				fc.getMinMaxDepth(), 
				fc.getMinMaxTime(), null, null));
				
			
			fc = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				lonRange, 
				fc.getMinMaxDepth(), 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				lonRange, 
				fc.getMinMaxDepth(), 
				fc.getMinMaxTime(), null, null));
				
			fc = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				lonRange, 
				fc.getMinMaxDepth(), 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				lonRange, 
				fc.getMinMaxDepth(), 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				lonRange, 
				fc.getMinMaxDepth(), 
				fc.getMinMaxTime(), null, null));

			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setFieldValues("latmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_LON].setFieldValues("latmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setFieldValues("latmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("latmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmax", new Double(newMaxY));
			}
		
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setFieldValues("lonmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_LON].setFieldValues("lonmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setFieldValues("lonmin", new Double(newMinX));
				cutPanelViews[Constants.LON_TIME].setFieldValues("lonmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmin", new Double(newMinX));
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmax", new Double(newMaxX));
			}
		}
		else if (focusedView instanceof LatTimeView) {
			propXMin = "timmin";
			propXMax = "timmax";
			propYMin = "latmin";
			propYMax = "latmax";
			propYCtr = "latctr";
			propXCtr = "timctr";
			
			double[] latRange = {newMinY, newMaxY};
			double[] tRange = {newMinX, newMaxX};
			
			FilterConstraints fc = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				fc.getMinMaxDepth(), 
				tRange, null, null));
			
			fc = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				fc.getMinMaxDepth(), 
				tRange, null, null));
							
			fc = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				fc.getMinMaxDepth(), 
				tRange, null, null));

			fc = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				fc.getMinMaxDepth(), 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				fc.getMinMaxDepth(), 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				fc.getMinMaxDepth(), 
				tRange, null, null));
			
				cutPanelViews[Constants.LAT_LON].setFieldValues("latmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_LON].setFieldValues("latmax", new Double(newMaxY));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("latmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("latmax", new Double(newMaxY));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmax", new Double(newMaxY));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("timmax", new Double(newMaxX));
				cutPanelViews[Constants.LON_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.LON_TIME].setFieldValues("timmax", new Double(newMaxX));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmax", new Double(newMaxX));
		}
		else if (focusedView instanceof LatDepthView) {
			propXMin = "latmin";
			propXMax = "latmax";
			propYMin = "zmin";
			propYMax = "zmax";
			propXCtr = "latctr";
			propYCtr = "zctr";
			
			double[] latRange = {newMinX, newMaxX};
			double[] zRange = {newMinY, newMaxY};
			
			FilterConstraints fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				zRange, 
				fc.getMinMaxTime(), null, null));
			
			fc = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(latRange, 
				fc.getMinMaxLon(), 
				zRange, 
				fc.getMinMaxTime(), null, null));

			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setFieldValues("latmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_LON].setFieldValues("latmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setFieldValues("latmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("latmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmax", new Double(newMaxX));
			}
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmax", new Double(newMaxY));
			}
		}
		else if (focusedView instanceof LonTimeView) {
			propXMin = "timmin";
			propXMax = "timmax";
			propYMin = "lonmin";
			propYMax = "lonmax";
			propXCtr = "timctr";
			propYCtr = "lonctr";

			double[] lonRange = {newMinY, newMaxY};
			double[] tRange = {newMinX, newMaxX};

			FilterConstraints fc = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				fc.getMinMaxDepth(), 
				tRange, null, null));
			
			fc = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				fc.getMinMaxDepth(), 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				fc.getMinMaxDepth(), 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				fc.getMinMaxDepth(), 
				tRange, null, null));

			fc = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				fc.getMinMaxDepth(), 
				tRange, null, null));

			fc = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				fc.getMinMaxDepth(), 
				tRange, null, null));

		
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setFieldValues("lonmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_LON].setFieldValues("lonmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setFieldValues("lonmin", new Double(newMinY));
				cutPanelViews[Constants.LON_TIME].setFieldValues("lonmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmin", new Double(newMinY));
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmax", new Double(newMaxY));
			}
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("timmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.LON_TIME].setFieldValues("timmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmax", new Double(newMaxX));
			}
		}
		else if (focusedView instanceof LonDepthView) {
			propXMin = "lonmin";
			propXMax = "lonmax";
			propYMin = "zmin";
			propYMax = "zmax";
			propXCtr = "lonctr";
			propYCtr = "zctr";
			
			double[] lonRange = {newMinX, newMaxX};
			double[] zRange = {newMinY, newMaxY};
			
			FilterConstraints fc = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				zRange, 
				fc.getMinMaxTime(), null, null));
			
			fc = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				zRange, 
				fc.getMinMaxTime(), null, null));
		
			fc = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				zRange, 
				fc.getMinMaxTime(), null, null));

			fc = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				lonRange, 
				zRange, 
				fc.getMinMaxTime(), null, null));
		
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
				cutPanelViews[Constants.LAT_LON].setFieldValues("lonmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_LON].setFieldValues("lonmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setFieldValues("lonmin", new Double(newMinY));
				cutPanelViews[Constants.LON_TIME].setFieldValues("lonmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmin", new Double(newMinY));
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmax", new Double(newMaxY));
			}
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmax", new Double(newMaxY));
			}
		}
		else if (focusedView instanceof DepthTimeView) {
			propXMin = "timmin";
			propXMax = "timmax";
			propYMin = "zmin";
			propYMax = "zmax";
			propXCtr = "timctr";
			propYCtr = "zctr";
			
			double[] tRange = {newMinX, newMaxX};
			double[] zRange = {newMinY, newMaxY};
			
			FilterConstraints fc = cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.DEPTH_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				fc.getMinMaxLon(), 
				zRange, 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_LON].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				fc.getMinMaxLon(), 
				zRange, 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				fc.getMinMaxLon(), 
				zRange, 
				tRange, null, null));

			fc = cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_DEPTH].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				fc.getMinMaxLon(), 
				zRange, 
				tRange, null, null));
				
			fc = cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LON_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				fc.getMinMaxLon(), 
				zRange, 
				tRange, null, null));

			fc = cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().getFilterConstraints();
			cutPanelViews[Constants.LAT_TIME].getFilterConstraintsManager().resetFilterConstraints(
				new FilterConstraints(fc.getMinMaxLat(), 
				fc.getMinMaxLon(), 
				zRange, 
				tRange, null, null));
			
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
				cutPanelViews[Constants.LAT_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.LAT_TIME].setFieldValues("timmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
				cutPanelViews[Constants.LON_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.LON_TIME].setFieldValues("timmax", new Double(newMaxX));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmin", new Double(newMinX));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmax", new Double(newMaxX));
			}
			
			if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmax", new Double(newMaxY));
			}
			if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmin", new Double(newMinY));
				cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmax", new Double(newMaxY));
			}
		}
			
		//set the handles		
	    if (cutPanelViews[Constants.LAT_LON] != null) {
	    	cutPanelViews[Constants.LAT_LON].setHandle(null, propYCtr, new Double(newCtrY), true);
	    	cutPanelViews[Constants.LAT_LON].setHandle(null, propXCtr, new Double(newCtrX), true);
	    	cutPanelViews[Constants.LAT_LON].setHandle(null, propYMin, new Double(newMinY), true);
	    	cutPanelViews[Constants.LAT_LON].setHandle(null, propYMax, new Double(newMaxY), true);
	    	cutPanelViews[Constants.LAT_LON].setHandle(null, propXMin, new Double(newMinX), true);
	    	cutPanelViews[Constants.LAT_LON].setHandle(null, propXMax, new Double(newMaxX), true);
	    }
	    
	   /* if (cutPanelViews[Constants.LAT_TIME] != null) {
	    	cutPanelViews[Constants.LAT_TIME].setHandle(null, propYCtr, new Double(newCtrY));
	    	cutPanelViews[Constants.LAT_TIME].setHandle(null, propXCtr, new Double(newCtrX));
	    	
	    	cutPanelViews[Constants.LAT_TIME].setHandle(null, propYMin, new Double(newMinY));
	    	cutPanelViews[Constants.LAT_TIME].setHandle(null, propYMax, new Double(newMaxY));
	    	cutPanelViews[Constants.LAT_TIME].setHandle(null, propXMin, new Double(newMinX));
	    	cutPanelViews[Constants.LAT_TIME].setHandle(null, propXMax, new Double(newMaxX));
	    }
	    
	    if (cutPanelViews[Constants.LAT_DEPTH] != null) {
	    	cutPanelViews[Constants.LAT_DEPTH].setHandle(null, propYCtr, new Double(newCtrY));
	    	cutPanelViews[Constants.LAT_DEPTH].setHandle(null, propXCtr, new Double(newCtrX));
	    	
	    	cutPanelViews[Constants.LAT_DEPTH].setHandle(null, propXMin, new Double(newMinX));
	    	cutPanelViews[Constants.LAT_DEPTH].setHandle(null, propXMax, new Double(newMaxX));
	    	cutPanelViews[Constants.LAT_DEPTH].setHandle(null, propYMin, new Double(newMinY));
	    	cutPanelViews[Constants.LAT_DEPTH].setHandle(null, propYMax, new Double(newMaxY));
	    }
	    
	    if (cutPanelViews[Constants.LON_TIME] != null) {
	    	cutPanelViews[Constants.LON_TIME].setHandle(null, propYCtr, new Double(newCtrY));
	    	cutPanelViews[Constants.LON_TIME].setHandle(null, propXCtr, new Double(newCtrX));
	    	
	    	cutPanelViews[Constants.LON_TIME].setHandle(null, propYMin, new Double(newMinY));
	    	cutPanelViews[Constants.LON_TIME].setHandle(null, propYMax, new Double(newMaxY));
	    	cutPanelViews[Constants.LON_TIME].setHandle(null, propXMin, new Double(newMinX));
	    	cutPanelViews[Constants.LON_TIME].setHandle(null, propXMax, new Double(newMaxX));
	    }
	    
	    if (cutPanelViews[Constants.LON_DEPTH] != null) {
	    	cutPanelViews[Constants.LON_DEPTH].setHandle(null, propYCtr, new Double(newCtrY));
	    	cutPanelViews[Constants.LON_DEPTH].setHandle(null, propXCtr, new Double(newCtrX));
	    	
	    	cutPanelViews[Constants.LON_DEPTH].setHandle(null, propYMin, new Double(newMinY));
	    	cutPanelViews[Constants.LON_DEPTH].setHandle(null, propYMax, new Double(newMaxY));
	    	cutPanelViews[Constants.LON_DEPTH].setHandle(null, propXMin, new Double(newMinX));
	    	cutPanelViews[Constants.LON_DEPTH].setHandle(null, propXMax, new Double(newMaxX));
	    }
	    
	    if (cutPanelViews[Constants.DEPTH_TIME] != null) {
	    	cutPanelViews[Constants.DEPTH_TIME].setHandle(null, propYCtr, new Double(newCtrY));
	    	cutPanelViews[Constants.DEPTH_TIME].setHandle(null, propXCtr, new Double(newCtrX));
	    	
	    	cutPanelViews[Constants.DEPTH_TIME].setHandle(null, propYMin, new Double(newMinY));
	    	cutPanelViews[Constants.DEPTH_TIME].setHandle(null, propYMax, new Double(newMaxY));
	    	cutPanelViews[Constants.DEPTH_TIME].setHandle(null, propXMin, new Double(newMinX));
	    	cutPanelViews[Constants.DEPTH_TIME].setHandle(null, propXMax, new Double(newMaxX));
	    }*/
		
		// set the text fields
	/*	if (prop.indexOf("lat") >= 0) {
			// notify views that have a lat axis
			if (prop.indexOf("ctr") >= 0) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
					cutPanelViews[Constants.LAT_LON].setFieldValues("latmin", new Double(minVal));
					cutPanelViews[Constants.LAT_LON].setFieldValues("latmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
					cutPanelViews[Constants.LAT_TIME].setFieldValues("latmin", new Double(minVal));
					cutPanelViews[Constants.LAT_TIME].setFieldValues("latmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
					cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmin", new Double(minVal));
					cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmax", new Double(maxVal));
				}
			}
			else {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
					cutPanelViews[Constants.LAT_LON].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
					cutPanelViews[Constants.LAT_TIME].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
					cutPanelViews[Constants.LAT_DEPTH].setFieldValues(prop, e.getNewValue());
				}
			}
		}
		else if (prop.indexOf("lon") >= 0) {
			// notify views that have a lon axis
			if (prop.indexOf("ctr") >= 0) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
					cutPanelViews[Constants.LAT_LON].setFieldValues("lonmin", new Double(minVal));
					cutPanelViews[Constants.LAT_LON].setFieldValues("lonmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
					cutPanelViews[Constants.LON_TIME].setFieldValues("lonmin", new Double(minVal));
					cutPanelViews[Constants.LON_TIME].setFieldValues("lonmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
					cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmin", new Double(minVal));
					cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmax", new Double(maxVal));
				}
			}
			else {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
					cutPanelViews[Constants.LAT_LON].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
					cutPanelViews[Constants.LON_TIME].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
					cutPanelViews[Constants.LON_DEPTH].setFieldValues(prop, e.getNewValue());
				}
			}
		}
		else if (prop.indexOf("z") >= 0) {
			// notify views that have a depth axis
			if (prop.indexOf("ctr") >= 0) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
					cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmin", new Double(minVal));
					cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
					cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmin", new Double(minVal));
					cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
					cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmin", new Double(minVal));
					cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmax", new Double(maxVal));
				}
			}
			else {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
					cutPanelViews[Constants.LAT_DEPTH].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
					cutPanelViews[Constants.LON_DEPTH].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
					cutPanelViews[Constants.DEPTH_TIME].setFieldValues(prop, e.getNewValue());
				}
			}
		}
		else if (prop.indexOf("tim") >= 0) {
			// notify views that have a time axis
			if (prop.indexOf("ctr") >= 0) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
					cutPanelViews[Constants.LAT_TIME].setFieldValues("timmin", new Double(minVal));
					cutPanelViews[Constants.LAT_TIME].setFieldValues("timmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
					cutPanelViews[Constants.LON_TIME].setFieldValues("timmin", new Double(minVal));
					cutPanelViews[Constants.LON_TIME].setFieldValues("timmax", new Double(maxVal));
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
					cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmin", new Double(minVal));
					cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmax", new Double(maxVal));
				}
			}
			else {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
					cutPanelViews[Constants.LAT_TIME].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
					cutPanelViews[Constants.LON_TIME].setFieldValues(prop, e.getNewValue());
				}
				if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
					cutPanelViews[Constants.DEPTH_TIME].setFieldValues(prop, e.getNewValue());
				}
			}
		}*/
		this.invalidateAllViews();
	}
	
	public void resetFields() {
		// set the text fields
		PointerCollectionGroup pointerCollection = ((NdEdit)parentObject).getPointerCollection();
		double range = Math.abs(pointerCollection.getMaxLat() - pointerCollection.getMinLat());
		double[] oldlats = {pointerCollection.getMinLat(), pointerCollection.getMaxLat()};
		double[] newlats = {NdEditFormulas.getNiceLowerValue(pointerCollection.getMinLat(), range), NdEditFormulas.getNiceUpperValue(pointerCollection.getMaxLat(), range)};    
    
		range = Math.abs(pointerCollection.getMaxLon() - pointerCollection.getMinLon());
		double minLon = NdEditFormulas.getNiceLowerValue(pointerCollection.getMinLon(), range);
		double maxLon = NdEditFormulas.getNiceUpperValue(pointerCollection.getMaxLon(), range);
		double[] oldlons = {pointerCollection.getMinLon(), pointerCollection.getMaxLon()};
		double[] newlons = {minLon, maxLon};    
    
		range = Math.abs(pointerCollection.getMaxTime() - pointerCollection.getMinTime());
		double[] oldtimes = {pointerCollection.getMinTime(), pointerCollection.getMaxTime()};
		double[] newtimes = {NdEditFormulas.getNiceLowerValue(pointerCollection.getMinTime(), range), NdEditFormulas.getNiceUpperValue(pointerCollection.getMaxTime(), range)};    
    
		double[] newzees = {pointerCollection.getMinDepth() * 1.05, pointerCollection.getMaxDepth() * 0.95};  
		
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
			cutPanelViews[Constants.LAT_LON].setFieldValues("latmin", new Double(newlats[0]));
			cutPanelViews[Constants.LAT_LON].setFieldValues("latmax", new Double(newlats[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
			cutPanelViews[Constants.LAT_TIME].setFieldValues("latmin", new Double(newlats[0]));
			cutPanelViews[Constants.LAT_TIME].setFieldValues("latmax", new Double(newlats[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
			cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmin", new Double(newlats[0]));
			cutPanelViews[Constants.LAT_DEPTH].setFieldValues("latmax", new Double(newlats[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0) {
			cutPanelViews[Constants.LAT_LON].setFieldValues("lonmin", new Double(newlons[0]));
			cutPanelViews[Constants.LAT_LON].setFieldValues("lonmax", new Double(newlons[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
			cutPanelViews[Constants.LON_TIME].setFieldValues("lonmin", new Double(newlons[0]));
			cutPanelViews[Constants.LON_TIME].setFieldValues("lonmax", new Double(newlons[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
			cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmin", new Double(newlons[0]));
			cutPanelViews[Constants.LON_DEPTH].setFieldValues("lonmax", new Double(newlons[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0) {
			cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmin", new Double(newzees[0]));
			cutPanelViews[Constants.LAT_DEPTH].setFieldValues("zmax", new Double(newzees[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0) {
			cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmin", new Double(newzees[0]));
			cutPanelViews[Constants.LON_DEPTH].setFieldValues("zmax", new Double(newzees[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
			cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmin", new Double(newzees[0]));
			cutPanelViews[Constants.DEPTH_TIME].setFieldValues("zmax", new Double(newzees[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0) {
			cutPanelViews[Constants.LAT_TIME].setFieldValues("timmin", new Double(newtimes[0]));
			cutPanelViews[Constants.LAT_TIME].setFieldValues("timmax", new Double(newtimes[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0) {
			cutPanelViews[Constants.LON_TIME].setFieldValues("timmin", new Double(newtimes[0]));
			cutPanelViews[Constants.LON_TIME].setFieldValues("timmax", new Double(newtimes[1]));
		}
		if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0) {
			cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmin", new Double(newtimes[0]));
			cutPanelViews[Constants.DEPTH_TIME].setFieldValues("timmax", new Double(newtimes[1]));
		}
	}
	
	public double getFocusedViewXMin() {
		return focusedView.getSelMinXVal();
	}
	public double getFocusedViewXMax() {
		return focusedView.getSelMaxXVal();
	}
	public double getFocusedViewYMin() {
		return focusedView.getSelMinYVal();
	}
	public double getFocusedViewYMax() {
		return focusedView.getSelMaxYVal();
	}
	
	public String getFocusedViewXAxis() {
		String propXAxis = null;
		if (focusedView instanceof LatLonView) {
			propXAxis = "lon";
		}
		else if (focusedView instanceof LatTimeView){
			propXAxis = "tim";
		}
		else if (focusedView instanceof LatDepthView){
			propXAxis = "lat";
		}
		else if (focusedView instanceof LonTimeView){
			propXAxis = "tim";
		}
		else if (focusedView instanceof LonDepthView){
			propXAxis = "lon";
		}
		else if (focusedView instanceof DepthTimeView){
			propXAxis = "tim";
		}
		return propXAxis;
	}
	
	public String getFocusedViewYAxis() {
		String propYAxis = null;
		if (focusedView instanceof LatLonView) {
			propYAxis = "lat";
		}
		else if (focusedView instanceof LatTimeView){
			propYAxis = "lat";
		}
		else if (focusedView instanceof LatDepthView){
			propYAxis = "z";
		}
		else if (focusedView instanceof LonTimeView){
			propYAxis = "lon";
		}
		else if (focusedView instanceof LonDepthView){
			propYAxis = "z";
		}
		else if (focusedView instanceof DepthTimeView){
			propYAxis = "z";
		}
		return propYAxis;
	}
	
	public void setFocusView(CutPanelView viewToFocus) {
		focusedView = viewToFocus;
		for (int i = 0; i < cutPanelViews.length; i++) {
       		((CutPanelView)cutPanelViews[i]).setHandleFocus(false);
    	}

		viewToFocus.setHandleFocus(true);
	}
	
	public CutPanelView getFocusView() {
		return focusedView;
	}
	
	public void setIgnoreViewFilters(boolean ignore) {
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
			cutPanelViews[Constants.LAT_LON].setIgnoreFilter(ignore);
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
			cutPanelViews[Constants.LAT_TIME].setIgnoreFilter(ignore);
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
			cutPanelViews[Constants.LAT_DEPTH].setIgnoreFilter(ignore);
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
			cutPanelViews[Constants.LON_DEPTH].setIgnoreFilter(ignore);
		if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
			cutPanelViews[Constants.DEPTH_TIME].setIgnoreFilter(ignore);
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
			cutPanelViews[Constants.LON_TIME].setIgnoreFilter(ignore);
	}
	
	public void setToolMode(int mode) {
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0)
			cutPanelViews[Constants.LAT_LON].setToolMode(mode);
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0)
			cutPanelViews[Constants.LAT_TIME].setToolMode(mode);
		if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0)
			cutPanelViews[Constants.LAT_DEPTH].setToolMode(mode);
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0)
			cutPanelViews[Constants.LON_DEPTH].setToolMode(mode);
		if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0)
			cutPanelViews[Constants.DEPTH_TIME].setToolMode(mode);
		if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0)
			cutPanelViews[Constants.LON_TIME].setToolMode(mode);
	}
	
	public Float getAxisMin(int axis, int ignoreView) {
		try {
			if (axis == Constants.LON_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getMinYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.LAT_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getMinXVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.DEPTH_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getMinYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.TIME_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getMinXVal());
				}
				else 
					return null;
			}
		} catch (Exception ex) {}
		return null;
	}
	
	public Float getAxisMax(int axis, int ignoreView) {
		try {
			if (axis == Constants.LON_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getMaxYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.LAT_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getMaxXVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.DEPTH_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getMaxYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.TIME_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getMaxXVal());
				}
				else 
					return null;
			}
		} catch (Exception ex) {}
		return null;
	}
	
	public Float getAxisSelMin(int axis, int ignoreView) {
		try {
			if (axis == Constants.LON_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getSelMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getSelMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getSelMinYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.LAT_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getSelMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getSelMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getSelMinXVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.DEPTH_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getSelMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getSelMinYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getSelMinYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.TIME_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getSelMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getSelMinXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getSelMinXVal());
				}
				else 
					return null;
			}
		} catch (Exception ex) {}
		return null;
	}
	
	public Float getAxisSelMax(int axis, int ignoreView) {
		try {
			if (axis == Constants.LON_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getSelMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getSelMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getSelMaxYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.LAT_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_LON]) >= 0 && ignoreView != Constants.LAT_LON) {
					return new Float(cutPanelViews[Constants.LAT_LON].getSelMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getSelMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getSelMaxXVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.DEPTH_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LON_DEPTH]) >= 0 && ignoreView != Constants.LON_DEPTH) {
					return new Float(cutPanelViews[Constants.LON_DEPTH].getSelMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getSelMaxYVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LAT_DEPTH]) >= 0 && ignoreView != Constants.LAT_DEPTH) {
					return new Float(cutPanelViews[Constants.LAT_DEPTH].getSelMaxYVal());
				}
				else 
					return null;
			}
			else if (axis == Constants.TIME_AXIS) {
				if (visibleViews.indexOf(cutPanelViews[Constants.LAT_TIME]) >= 0 && ignoreView != Constants.LAT_TIME) {
					return new Float(cutPanelViews[Constants.LAT_TIME].getSelMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.LON_TIME]) >= 0 && ignoreView != Constants.LON_TIME) {
					return new Float(cutPanelViews[Constants.LON_TIME].getSelMaxXVal());
				}
				else if (visibleViews.indexOf(cutPanelViews[Constants.DEPTH_TIME]) >= 0 && ignoreView != Constants.DEPTH_TIME) {
					return new Float(cutPanelViews[Constants.DEPTH_TIME].getSelMaxXVal());
				}
				else 
					return null;
			}
		} catch (Exception ex) {}
		return null;
	}
	
	public void dumpVizViews(String msg) {
		for (int i=0; i<visibleViews.size(); i++)
			System.out.println(msg + " " + visibleViews.elementAt(i));
	}
	
	public CutPanelView getCutPanelView(int i) {
		return cutPanelViews[i];
	}
}

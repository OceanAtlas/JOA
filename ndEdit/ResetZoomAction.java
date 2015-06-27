/*
 * $Id: ResetZoomAction.java,v 1.9 2005/08/22 21:25:16 oz Exp $
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
import javax.swing.border.*;
import ndEdit.*;

 public class ResetZoomAction extends NdEditAction {
    public ResetZoomAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, vm, parent);
    }
    
    public ResetZoomAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, ks, vm, parent);
    }

	public void actionPerformed(ActionEvent e) {
		doAction();
	}

	public void doAction() {
		mParent.pushChangeableInfo("StartBatch", null, null, false);
		PointerCollectionGroup pointerCollection = mParent.getPointerCollection();
		// resets the zoom to the current ranges of the pointer collection
		
		//boolean crossed180 = pointerCollection.crosses180();
		double minLon;
		double maxLon;
		//if (crossed180) {
			// crossed 180
		//	minLon = pointerCollection.getMinELon();
		//	maxLon = pointerCollection.getMaxWLon() + 360;
	//	}
		//else {
			double range = Math.abs(pointerCollection.getMaxLon() - pointerCollection.getMinLon());
			minLon = NdEditFormulas.getNiceLowerValue(pointerCollection.getMinLon(), range);
			maxLon = NdEditFormulas.getNiceUpperValue(pointerCollection.getMaxLon(), range);
		//} */
		
		range = Math.abs(pointerCollection.getMaxLat() - pointerCollection.getMinLat());
		double[] oldlats = {pointerCollection.getMinLat(), pointerCollection.getMaxLat()};
		double[] newlats = {NdEditFormulas.getNiceLowerValue(pointerCollection.getMinLat(), range), NdEditFormulas.getNiceUpperValue(pointerCollection.getMaxLat(), range)};    
		mParent.pushChangeableInfo("ZoomLatitudeDomain", oldlats, newlats, false);
    
		double[] oldlons = {pointerCollection.getMinLon(), pointerCollection.getMaxLon()};
		double[] newlons = {minLon, maxLon};    
		mParent.pushChangeableInfo("ZoomLongitudeDomain", oldlons, newlons, false);
    
		range = Math.abs(pointerCollection.getMaxTime() - pointerCollection.getMinTime());
		double[] oldtimes = {pointerCollection.getMinTime(), pointerCollection.getMaxTime()};
		double[] newtimes = {NdEditFormulas.getNiceLowerValue(pointerCollection.getMinTime(), range), NdEditFormulas.getNiceUpperValue(pointerCollection.getMaxTime(), range)};    
		mParent.pushChangeableInfo("ZoomTimeDomain", oldtimes, newtimes, false);
    
		range = Math.abs(pointerCollection.getMaxDepth() - pointerCollection.getMinDepth());
		double[] oldzees = {pointerCollection.getMaxDepth(), pointerCollection.getMinDepth()};
		double[] newzees = {NdEditFormulas.getNiceLowerValue(pointerCollection.getMaxDepth(), range), NdEditFormulas.getNiceUpperValue(pointerCollection.getMinDepth(), range)};  
		mParent.pushChangeableInfo("ZoomDepthDomain", oldzees, newzees, false);
		mParent.pushChangeableInfo("EndBatch", null, null, false);
		//pointerCollection.resetSizes();
		//mViewManager.resetZoom();
		//mParent.getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(pointerCollection.getMinMaxLat(), 
		//					pointerCollection.getMinMaxLon(), pointerCollection.getMinMaxDepth(), pointerCollection.getMinMaxTime(), null, null));
		
		mViewManager.resetSelectionRgns();
		mViewManager.invalidateAllViews();
	
	}
	
	public boolean isEnabled() {
		PointerCollectionGroup pc = getPointerCollection();
		return pc.size() > 0;
	}
}

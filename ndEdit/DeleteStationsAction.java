/*
 * $Id: DeleteStationsAction.java,v 1.8 2005/08/22 21:25:15 oz Exp $
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

 public class DeleteStationsAction extends NdEditAction {
    // This is our sample action. It must have an actionPerformed() method,
    // which is called when the action should be invoked.
    public DeleteStationsAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
        super(menu, text,icon, vm, parent);
    }
    
    public DeleteStationsAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, ks, vm, parent);
    }
    
    public void doAction() {
		PointerCollectionGroup pointerCollection = mParent.getPointerCollection();
		// want to get new filter constraints from the focused view
		double[] lats = pointerCollection.getMinMaxLat();
		double[] lons = pointerCollection.getMinMaxLon();
		double[] zees = pointerCollection.getMinMaxDepth();
		double[] tees = pointerCollection.getMinMaxTime();
		double minLat = lats[0]; 
		double oldMinLat = minLat;
		double maxLat = lats[1];  
		double oldMaxLat = maxLat;
		double minLon = lons[0]; 
		double oldMinLon = minLon;
		double maxLon = lons[1];  
		double oldMaxLon = maxLon;
		double minZ = zees[0];   
		double oldMinZ = minZ;
		double maxZ = zees[1];    
		double oldMaxZ = maxZ;  
		double minT = tees[0];     
		double oldMinT = minT;
		double maxT = tees[1];      
		double oldMaxT = maxT;
		if (mViewManager.getFocusedViewXAxis().indexOf("lat") >= 0) {
			minLat = mViewManager.getFocusedViewXMin();
			maxLat = mViewManager.getFocusedViewXMax();
		}
		else if (mViewManager.getFocusedViewYAxis().indexOf("lat") >= 0) {
			minLat = mViewManager.getFocusedViewYMin();
			maxLat = mViewManager.getFocusedViewYMax();
		} 
		
		if (mViewManager.getFocusedViewXAxis().indexOf("lon") >= 0) {
			minLon = mViewManager.getFocusedViewXMin();
			maxLon = mViewManager.getFocusedViewXMax();
		}
		else if (mViewManager.getFocusedViewYAxis().indexOf("lon") >= 0) {
			minLon = mViewManager.getFocusedViewYMin();
			maxLon = mViewManager.getFocusedViewYMax();
		} 
		
		if (mViewManager.getFocusedViewXAxis().indexOf("tim") >= 0) {
			minT = mViewManager.getFocusedViewXMin();
			maxT = mViewManager.getFocusedViewXMax();
		}
		else if (mViewManager.getFocusedViewYAxis().indexOf("tim") >= 0) {
			minT = mViewManager.getFocusedViewYMin();
			maxT = mViewManager.getFocusedViewYMax();
		}
		
		if (mViewManager.getFocusedViewXAxis().indexOf("z") >= 0) {
			minZ = mViewManager.getFocusedViewXMin();
			maxZ = mViewManager.getFocusedViewXMax();
		}
		else if (mViewManager.getFocusedViewYAxis().indexOf("z") >= 0) {
			minZ = mViewManager.getFocusedViewYMin();
			maxZ = mViewManager.getFocusedViewYMax();
		}
		pointerCollection.resetSizes();
		lats[0] = minLat; 
		lats[1] = maxLat; 
		lons[0] = minLon; 
		lons[1] = maxLon;  
		zees[0] = minZ; 
		zees[1] = maxZ;   
		tees[0] = (long)minT; 
		tees[1] = (long)maxT; 
		mParent.getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(lats, lons, zees, tees, null, null));
		
		//increment the deletion flag for pointers that don't match the filter 
		byte[] currFilterResults = mParent.getFilteredDataManager().getResults();
		boolean first = true;
		int delOrd = 0;
		for (int i=0; i<pointerCollection.getSize(); i++) {
			if (currFilterResults[i] == 4) {
				// mark this as deleted
				if (first) {
					delOrd = pointerCollection.getCurrDeletionIndex();
					pointerCollection.setCurrDeletionIndex(++delOrd);
					first = false;
				}
				pointerCollection.delete(i, delOrd);
			}
		}
		pointerCollection.resetSizes();
		lats[0] = oldMinLat; 
		lats[1] = oldMaxLat; 
		lons[0] = oldMinLon; 
		lons[1] = oldMaxLon;  
		zees[0] = oldMinZ; 
		zees[1] = oldMaxZ;   
		tees[0] = (long)oldMinT; 
		tees[1] = (long)oldMaxT; 
		mParent.getFilterConstraintsManager().resetFilterConstraints(new FilterConstraints(lats, lons, zees, tees, null, null));
		mViewManager.invalidateAllViews();
	}

	public void actionPerformed(ActionEvent e) {
		doAction();
	}
	
	public boolean isEnabled() {
		PointerCollectionGroup pc = getPointerCollection();	
		return pc.size() > 0;
	}
}

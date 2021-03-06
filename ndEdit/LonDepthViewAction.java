/*
 * $Id: LonDepthViewAction.java,v 1.2 2005/02/15 18:31:09 oz Exp $
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

 public class LonDepthViewAction extends NdEditAction {
    public LonDepthViewAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, true, vm, parent);
    }
    
    public LonDepthViewAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
        // a checkbox action by default
        super(menu, text, icon, ks, true, vm, parent);
    }

	public void actionPerformed(ActionEvent e) {
		doAction();
	}

	public void doAction() {
		mViewManager.toggleView(Constants.LON_DEPTH);
	}
	
	public boolean getState() {
		int[] vizViews = mViewManager.getVisibleViews();
		for (int i=0; i<vizViews.length; i++) {
			if (vizViews[i] == Constants.LON_DEPTH)
				return true; 
		}
		return false;
	}
}

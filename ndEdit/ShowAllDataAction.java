/*
 * $Id: ShowAllDataAction.java,v 1.3 2005/02/15 18:31:10 oz Exp $
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

public class ShowAllDataAction extends NdEditAction implements ToggleAction {
    public ShowAllDataAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, vm, parent);
    }
        
    public ShowAllDataAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, ks, vm, parent);
    }

	public void actionPerformed(ActionEvent e) {
		doAction();
	}
	
	public void doAction() {
		// wait for a period of time
		ToggleTimer timer = new ToggleTimer(this, 1000, 100);
		timer.startTimer();
	}
	
	public void toggleOn() {
		// turn off the filtering at the view level
		mViewManager.setIgnoreViewFilters(true);
		mViewManager.invalidateAllViews();
	
	}
	
	public void toggleOff() {
		// turn filtering back on 
		mViewManager.setIgnoreViewFilters(false);
		mViewManager.invalidateAllViews();
	}
	
	public boolean isEnabled() {
		PointerCollectionGroup pc = getPointerCollection();
		return (pc.size() > 0);
	}
}

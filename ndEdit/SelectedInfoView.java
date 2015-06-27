/*
 * $Id: SelectedInfoView.java,v 1.2 2005/02/15 18:31:10 oz Exp $
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

import javax.swing.JPanel;
import java.awt.Graphics;

/**
*
* @author  oz 
* @version 1.0 08/18/04
* 
* The InfoPanelView is currently just a marker interface for panels
* that will display textual information
*/

public class SelectedInfoView extends JPanel implements InfoPanelView {
	private ViewManager mViewManager;
    private ViewToggleButn tb;
    private PointerCollectionGroup mCurrPC;
	
	public SelectedInfoView(Object parentObject) {
		mViewManager = (ViewManager)parentObject;
		tb = new ViewToggleButn(LatLonConstants.standardGif, LatLonConstants.toolTipText);
	
	}
	
	public void paintComponent(Graphics gin) {
		gin.drawString("Hello World", 100, 100);
	}

	/**
	* Resets the current pointer collection, notifying the filters, and 
	* optionally resetting the current set of filtering constraints (extents). 
	*
	* @param pc new pointer collection to be displayed and presented for filtering
	* @param resetFilteringConstraints boolean flag, if true, then set all 
	*        filtering constaints (extents) to match the extremes of the data in 
	*        the pointer collection
	* 
	*/
	public void setPointerCollection(PointerCollectionGroup pcg, boolean resetFilteringConstraints) {
		if (pcg == null)
			return;
		mCurrPC = pcg;
	}
}

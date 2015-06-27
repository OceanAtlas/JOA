/*
 * $Id: AboutAction.java,v 1.2 2005/02/15 18:30:23 oz Exp $
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

 public class AboutAction extends NdEditAction {
    public AboutAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, vm, parent);
    }
    
    public AboutAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, ks, vm, parent);
    }

	public void actionPerformed(ActionEvent e) {
		doAction();
	}

	public void doAction() {
		AboutDialog ff = new AboutDialog(new JFrame(), true);
		//ff.init();
		ff.pack();
	
		// show dialog at center of screen
		Rectangle dBounds = ff.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		ff.setLocation(x, y);
		ff.show(); 
	}
}

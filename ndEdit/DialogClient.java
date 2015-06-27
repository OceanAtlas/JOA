/*
 * $Id: DialogClient.java,v 1.2 2005/02/15 18:31:08 oz Exp $
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
import javax.swing.*;

/**
 * DialogClients are notified when the Dialog with which they 
 * are associated is dismissed.  A reference to the dismissed 
 * Dialog is passed as a parameter of dialogDismissed() in case 
 * a DialogClient is a client of more than one Dialog.<p>
 */
 
public interface DialogClient {
	// OK Button
    //abstract public void dialogDismissed(Frame d);
    abstract public void dialogDismissed(JDialog d);
    
    // Cancel button
    //abstract public void dialogCancelled(Frame d);
    abstract public void dialogCancelled(JDialog d);
    
    // something other than the OK button 
    //abstract public void dialogDismissedTwo(Frame d);
    abstract public void dialogDismissedTwo(JDialog d);
    
    // Apply button, OK w/o dismissing the dialog
    //abstract public void dialogApply(Frame d);
    abstract public void dialogApply(JDialog d);
    
    // Apply button, OK w/o dismissing the dialog
    //abstract public void dialogApplyTwo(Object d);
    abstract public void dialogApplyTwo(Object d);
}

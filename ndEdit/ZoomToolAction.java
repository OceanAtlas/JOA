/*
 * $Id: ZoomToolAction.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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

 public class ZoomToolAction extends AbstractAction {
       protected transient ChangeEvent changeEvent = null;
       protected EventListenerList listenerList = new EventListenerList();
       protected ViewManager mViewManager;
       protected NdEdit mParent;

        // This is our sample action. It must have an actionPerformed() method,
        // which is called when the action should be invoked.
        public ZoomToolAction(String text, Icon icon, ViewManager vm, NdEdit parent) {
            super(text,icon);
            mViewManager = vm;
            mParent = parent;
        }

	public void actionPerformed(ActionEvent e) {
		mViewManager.invalidateAllViews();
	}

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireChange() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }          
        }
    }   
}

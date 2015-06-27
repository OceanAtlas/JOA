/*
 * $Id: NdEditAction.java,v 1.3 2005/02/15 18:31:09 oz Exp $
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

public abstract class NdEditAction extends AbstractAction {
	protected transient ChangeEvent changeEvent = null;
	protected EventListenerList listenerList = new EventListenerList();
	protected ViewManager mViewManager;
	protected NdEdit mParent;
	protected KeyStroke mAccelerator;
	protected boolean mIsCheckBox = false;
	protected String mMenuName;
	protected String mText;
	
	public NdEditAction(String menu, String text, Icon icon) {
	    super(text, icon);
	    mText = new String(text);
	    mMenuName = new String(menu);
	    mViewManager = null;
	    mParent = null;
	    mAccelerator = null;
	}
	
	public NdEditAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
	    super(text, icon);
	    mText = new String(text);
	    mMenuName = new String(menu);
	    mViewManager = vm;
	    mParent = parent;
	    mAccelerator = null;
	}
	
	public NdEditAction(String menu, String text, Icon icon, boolean isCheck, ViewManager vm, NdEdit parent) {
	    super(text, icon);
	    mText = new String(text);
	    mMenuName = new String(menu);
	    mViewManager = vm;
	    mParent = parent;
	    mAccelerator = null;
	    mIsCheckBox = isCheck;
	}
	
	public NdEditAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
	    super(text, icon);
	    mText = new String(text);
	    mText = new String(text);
	    mMenuName = new String(menu);
	    mViewManager = vm;
	    mParent = parent;
	    mAccelerator = ks;
	}
	
	public NdEditAction(String menu, String text, Icon icon, KeyStroke ks, boolean isCheck, ViewManager vm, NdEdit parent) {
	    super(text, icon);
	    mText = new String(text);
	    mMenuName = new String(menu);
	    mViewManager = vm;
	    mParent = parent;
	    mAccelerator = ks;
	    mIsCheckBox = isCheck;
	}
	
	public boolean isCheckBox() {
		return mIsCheckBox;
	}
	
	public KeyStroke getAccelerator() {
		return mAccelerator;
	}
	
	public void setAccelerator(KeyStroke ks) {
		mAccelerator = ks;
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
    
    public String getText() {
	    return mText;
    } 
    
    public String getMenu() {
	    return mMenuName;
    } 
	
	public boolean getState() {
		return true;
	}
	
	public PointerCollectionGroup getPointerCollection() {
		return mParent.getPointerCollection();
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public NdEdit getParent() {
		return mParent;
	}
	
	public abstract void doAction();
}

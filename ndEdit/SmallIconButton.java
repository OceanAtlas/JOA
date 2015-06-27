/*
 * $Id: SmallIconButton.java,v 1.4 2005/02/15 18:31:10 oz Exp $
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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class SmallIconButton extends JToggleButton {
	protected int mWidth = 32, mHeight = 32;
	private Dimension mSize;
	public SmallIconButton() {
		if (Constants.ISMAC) {
			mWidth = 35;
			mHeight = 35;
		}
		mSize = new Dimension(mWidth, mHeight);
	}
	
    public SmallIconButton(Icon icon) {
    	super(icon);
		if (Constants.ISMAC) {
		 	mWidth = 35;
		 	mHeight = 35;
		}
		mSize = new Dimension(mWidth, mHeight);
	}
	
	public SmallIconButton(String gif, String tooltip) {
		ImageIcon icon = new ImageIcon();
			try {
	      icon = new ImageIcon(Class.forName("ndEdit.NdEdit").getResource(gif));
      }
      catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }

		this.setToolTipText(tooltip);
		this.setIcon(icon);

		if (Constants.ISMAC) {
			mWidth = 35;
			mHeight = 35;
		}
		mSize = new Dimension(mWidth, mHeight);
	}
    
	public Dimension getPreferredSize() {
		return mSize;
	} 
    
	public Dimension getMinimumSize() {
		return mSize;
	}  
    
	public Dimension getMaximumSize() {
		return mSize;
	} 
	
	public boolean isFocusTraversable() {
		return false;
	}
	
	public void setBtnSize(int width, int height) {
	 	mWidth = width;
	 	mHeight = height;
	}
	
	public Insets getInsets() {
		return new Insets(100, 100, 100, 100);
	}
}
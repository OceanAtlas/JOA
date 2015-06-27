/*
 * $Id: SmallIconButton.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import java.awt.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class SmallIconButton extends JToggleButton {
	protected int mWidth = 25, mHeight = 25;
    public SmallIconButton(Icon icon) {
    	super(icon);
		if (JOAConstants.ISMAC) {
		 	mWidth = 32;
		 	mHeight = 32;
		}
	}
    
	public Dimension getPreferredSize() {
		return new Dimension(mWidth, mHeight);
	} 
    
	public Dimension getMinimumSize() {
		return new Dimension(mWidth, mHeight);
	} 
	
	public boolean isFocusTraversable() {
		return false;
	}
	
	public void setBtnSize(int width, int height) {
	 	mWidth = width;
	 	mHeight = height;
	}
}
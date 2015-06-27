/*
 * $Id: LargeIconButton.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import java.awt.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class LargeIconButton extends JToggleButton {
    public LargeIconButton(Icon icon) {
    	super(icon);
    }
    
	public Dimension getPreferredSize() {
		if (JOAConstants.ISMAC)
			return new Dimension(38, 38);
		else
			return new Dimension(32, 32);
	} 
	
	public Dimension getMinimumSize() {
		if (JOAConstants.ISMAC)
			return new Dimension(38, 38);
		else
			return new Dimension(32, 32);
	}
	
	public boolean isFocusTraversable() {
		return false;
	}
}

/*
 * $Id: SimpleIconButton.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class SimpleIconButton extends JOAJButton {
    public SimpleIconButton(Icon icon) {
    	super(icon);
	}
    
	public Dimension getPreferredSize() {
		return new Dimension(20, 20);
	} 
	
	public boolean isFocusTraversable() {
		return false;
	}
}
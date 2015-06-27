/*
 * $Id: JOAJToggleButton.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJToggleButton extends JToggleButton {
	
	public JOAJToggleButton(Icon icon, boolean selected) {
		super(icon, selected);
	}
	
	public boolean isFocusTraversable() {
		return false;
	}
}
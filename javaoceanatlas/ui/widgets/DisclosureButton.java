/*
 * $Id: DisclosureButton.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class DisclosureButton extends JToggleButton {
	public DisclosureButton(String name, Icon image) {
		super(name, image);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}

	public boolean isFocusTraversable() {
		return true;
	} 	 	

	public void focusGained(FocusEvent fe) {
	}
}

/*
 * $Id: JOAJButton.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJButton extends JButton {
	public JOAJButton() {
		super();
	}
	
	public JOAJButton(String s) {
		super(s);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.BOLD, 11));
		//}
	}
	public JOAJButton(Icon icon) {
		super(icon);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.BOLD, 11));
		//}
	}
}
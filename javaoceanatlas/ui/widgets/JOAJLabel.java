/*
 * $Id: JOAJLabel.java,v 1.3 2005/06/21 17:25:52 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJLabel extends JLabel {
	public JOAJLabel() {
		super();
	}
	
	public JOAJLabel(String s, boolean isMac) {
		super(s);
		if (isMac) {
			setFont(new Font("Helvetica", Font.PLAIN, 11));
		}
	}
	
	public JOAJLabel(String s) {
		super(s);
		//if (JOAConstants.ISMAC) {
		////	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
	
	public JOAJLabel(String s, int orient) {
		super(s, orient);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
	
	public JOAJLabel(Icon icon) {
		super(icon);
		////if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
}
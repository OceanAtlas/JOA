/*
 * $Id: JOAJSlider.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJSlider extends JSlider {
	public JOAJSlider(int orient, int min, int max, int inc) {
		super(orient, min, max, inc);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
	
	public JOAJSlider(int min, int max) {
		super(min, max);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
	
	public JOAJSlider() {
		super();
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
}
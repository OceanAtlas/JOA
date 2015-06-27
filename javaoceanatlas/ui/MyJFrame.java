/*
 * $Id: MyJFrame.java,v 1.2 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MyJFrame extends JFrame {
	
	public MyJFrame(String filename) {
		super(filename);
	}
	
	public boolean isFocusTraversable() {
		return true;
	}
}
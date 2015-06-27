/*
 * $Id: JOAJToolBar.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.Insets;
import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJToolBar extends JToolBar {
	
	public JOAJToolBar() {
		super();
    this.setMargin(new Insets(0, 0, 0, 0));
	}
	
	public boolean isFocusTraversable() {
		return false;
	}
}
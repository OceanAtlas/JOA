/*
 * $Id: JOAJScrollPane.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJScrollPane extends JScrollPane {
	public JOAJScrollPane(Component c) {
		super(c);
	}
	
	public boolean isFocusTraversable() {
		return false;
	}
}
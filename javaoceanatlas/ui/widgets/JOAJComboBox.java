/*
 * $Id: JOAJComboBox.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.util.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJComboBox extends JComboBox {
	public JOAJComboBox(Object[] o) {
		super(o);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
	
	@SuppressWarnings("unchecked")
  public JOAJComboBox(Vector v) {
		super(v);
	//	if (JOAConstants.ISMAC) {
	//		setFont(new Font("Helvetica", Font.PLAIN, 11));
	//	}
	}
	
	public JOAJComboBox() {
		super();
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
	}
	
	public void setSelectedIndex(String inStr) {
		// set the 
		int numItems = this.getItemCount();
		for (int i=0; i<numItems; i++) {
			String itemStr = (String)this.getItemAt(i);
			if (itemStr.equals(inStr)) {
				this.setSelectedIndex(i);
				return;
			}
		}
	}
}
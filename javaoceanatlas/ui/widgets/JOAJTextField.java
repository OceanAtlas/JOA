/*
 * $Id: JOAJTextField.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;
import java.awt.event.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class JOAJTextField extends JTextField {
	private boolean mAcceptFocus = true;
		
	public JOAJTextField() {
		super();
	}
		
	public JOAJTextField(String s) {
		super(s);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
		
		/*this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (JOAConstants.ISMAC)
					mIgnoreFocusGained = true;
			}
		});*/
		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent me) {
				if (JOAConstants.ISMAC)
					selectAll();
			}
		});
	    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	    this.getKeymap().removeKeyStrokeBinding(enter);
	}
	
	public JOAJTextField(int width) {
		super(JOAConstants.ISMAC || JOAConstants.ISSUNOS ? width + 2: width);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
		
		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent me) {
				if (JOAConstants.ISMAC)
					selectAll();
			}
		});
	    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	    this.getKeymap().removeKeyStrokeBinding(enter);
	}
	
	public JOAJTextField(String s, int width) {
		super(JOAConstants.ISMAC || JOAConstants.ISSUNOS ? width + 2: width);
		//if (JOAConstants.ISMAC) {
		//	setFont(new Font("Helvetica", Font.PLAIN, 11));
		//}
		
		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent me) {
				if (JOAConstants.ISMAC)
					selectAll();
			}
		});
	    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	    this.getKeymap().removeKeyStrokeBinding(enter);
	}
	
	public void setFocusTraversable(boolean flag) {
		mAcceptFocus = flag;
	}
	
	public boolean isFocusTraversable() {
		return mAcceptFocus;
	}
}
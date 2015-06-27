/*
 * $Id: IntTextFld.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.event.*;
import java.awt.*;

@SuppressWarnings("serial")
public class IntTextFld extends JOAJTextField {

 	public IntTextFld(int cols) {
 		super(cols);
 		this.addKeyListener(new KeyListener());
 	}

 	public IntTextFld(String text) {
 		super(text);
 		this.addKeyListener(new KeyListener());
 	}

 	public IntTextFld(String text, int cols) {
 		super(text, cols);
 		this.addKeyListener(new KeyListener());
 	}

	private class KeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent ev) {
			int key = ev.getKeyCode();
			// don't allow anything but integers in fld

			if (key != KeyEvent.VK_TAB && key != KeyEvent.VK_SHIFT && key != KeyEvent.VK_ALT && key != KeyEvent.VK_CONTROL
				 && key != KeyEvent.VK_META&& key != KeyEvent.VK_CAPS_LOCK && key != KeyEvent.VK_ENTER) {
				if (!(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_UP || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_DOWN ||
					key == KeyEvent.VK_BACK_SPACE)) {
					if (key != KeyEvent.VK_0 &&
						key != KeyEvent.VK_1 &&
						key != KeyEvent.VK_2 &&
						key != KeyEvent.VK_3 &&
						key != KeyEvent.VK_4 &&
						key != KeyEvent.VK_5 &&
						key != KeyEvent.VK_6 &&
						key != KeyEvent.VK_7 &&
						key != KeyEvent.VK_8 &&
						key != KeyEvent.VK_9 &&
						key != KeyEvent.VK_NUMPAD0 &&
						key != KeyEvent.VK_NUMPAD1 &&
						key != KeyEvent.VK_NUMPAD2 &&
						key != KeyEvent.VK_NUMPAD3 &&
						key != KeyEvent.VK_NUMPAD4 &&
						key != KeyEvent.VK_NUMPAD5 &&
						key != KeyEvent.VK_NUMPAD6 &&
						key != KeyEvent.VK_NUMPAD7 &&
						key != KeyEvent.VK_NUMPAD8 &&
						key != KeyEvent.VK_NUMPAD9) {
		                Toolkit.getDefaultToolkit().beep();
						ev.consume();
						ev.consume();
					}
				}
			}
		}
	}
}

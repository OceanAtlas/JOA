/*
 * $Id: JOAFocusableSwatch.java,v 1.3 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.events.*;

@SuppressWarnings("serial")
public class JOAFocusableSwatch extends FocusableSwatch implements ColorSelChangedListener {
	public JOAFocusableSwatch(Color inColor, SwatchGroup swatchGroup) {
		super(inColor, swatchGroup);
	}	
	
	public JOAFocusableSwatch(Color inColor, int width, int height, SwatchGroup swatchGroup) {
		super(inColor, width, height, swatchGroup);
	}
	
	public void colorChanged(ColorSelChangedEvent evt) {
		if (!mHasFocus)
			return;
		
		Color newColor = evt.getColor();
		if (newColor != null) {
			mColor = newColor;
			mColorIndex = evt.getColorIndex();
		}
			
		invalidate();
		Graphics g = mThis.getGraphics();
		if (g != null) {
			paintComponent(g);
			g.dispose();
		}
	}
}
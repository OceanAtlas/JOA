/*
 * $Id: ColorSelChangedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import javaoceanatlas.ui.*;

@SuppressWarnings("serial")
public class ColorSelChangedEvent extends AWTEvent {
	Color mColor = null;
	int mColorIndex = 0;
	
	public ColorSelChangedEvent(ColorPalettePanel cpp) {
		super(cpp, COLOR_SEL_CHANGED_EVENT);
	}
	
	public static final int COLOR_SEL_CHANGED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 8888;
		
	public void setColor(Color newColor, int colorIndex) {
		mColor = newColor;
		mColorIndex = colorIndex;
	}
	
	public Color getColor() {
		return mColor;
	}
	
	public int getColorIndex() {
		return mColorIndex;
	}
}
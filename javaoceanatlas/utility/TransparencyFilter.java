/*
 * $Id: TransparencyFilter.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.image.*;

public class TransparencyFilter extends RGBImageFilter {
	int mBGColor;
	DirectColorModel cm = null;
		
	public TransparencyFilter(int bgColor) {
		canFilterIndexColorModel = true;
		
		cm = (DirectColorModel)ColorModel.getRGBdefault();
		this.mBGColor = bgColor;
	}
	
	public int filterRGB(int x, int y, int rgb) {
		int alpha = 255;
		int red = cm.getRed(rgb);
		int green = cm.getGreen(rgb);
		int blue = cm.getBlue(rgb);
		if (rgb == mBGColor)
			alpha = 0;		
		
		return alpha << 24  | red <<16  | green << 8  | blue;
	}
}
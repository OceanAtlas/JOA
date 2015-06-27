/*
 * $Id: DissolveFilter.java,v 1.2 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.image.*;

public class DissolveFilter extends RGBImageFilter {
	
	
	public DissolveFilter() {
		canFilterIndexColorModel = true;
	}
	
	public int filterRGB(int x, int y, int rgb) {
		DirectColorModel cm = (DirectColorModel)ColorModel.getRGBdefault();
		int alpha = cm.getAlpha(rgb);
		int red = cm.getRed(rgb);
		int green = cm.getGreen(rgb);
		int blue = cm.getBlue(rgb);
		if (rgb == -1)
			alpha = 0;
		else
			alpha = 255;
		
		
		return alpha << 24  | red <<16  | green << 8  | blue;
	}
}
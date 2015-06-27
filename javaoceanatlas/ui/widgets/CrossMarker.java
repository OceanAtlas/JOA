/*
 * $Id: CrossMarker.java,v 1.3 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;

public class CrossMarker extends ObsMarker {
	int mTop, mLeft, mBottom, mRight;
	
	public CrossMarker(int x, int y, int top, int left, int bottom, int right, int size) {
	    super(x, y, size);
		mTop = top;
		mLeft = left;
		mBottom = bottom;
		mRight = right;
	}
	
	public void drawMarker(Graphics g, boolean flag){
		if (g == null)
			return;
		drawCross(g, mCurrX, mCurrY);
	}
	
	private void drawCross(Graphics g, int x, int y) {
		g.drawLine(x, mTop, x, mBottom);
		g.drawLine(mLeft, y, mRight, y);
	}
}
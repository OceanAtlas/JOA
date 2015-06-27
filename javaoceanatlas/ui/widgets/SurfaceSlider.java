/*
 * $Id: SurfaceSlider.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;

@SuppressWarnings("serial")
public class SurfaceSlider extends JOAJSlider {
	public SurfaceSlider(int orient, int min, int max, int value) {
		super(orient, min, max, value);
	}
	
	public SurfaceSlider(int min, int max) {
		super(min, max);
	}
	
	public SurfaceSlider() {
		super();
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(100, 30);
	}
}
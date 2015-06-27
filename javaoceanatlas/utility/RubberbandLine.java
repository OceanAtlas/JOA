/*
 * $Id: RubberbandLine.java,v 1.4 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.awt.geom.*;
import javaoceanatlas.ui.*;

/** 
 * A Rubberband that does lines.
 *
 * @version 1.0, 12/27/95
 * @author  David Geary modified by oz
 * @see     Rubberband
 * @see     gjt.test.RubberbandTest
 */
public class RubberbandLine extends Rubberband {
	GeneralPath aLine = new GeneralPath();
	
	public RubberbandLine() {
	}
    public RubberbandLine(Component component) {
        super(component);
    }
    
    public void drawLast() {
        drawLine(anchorPt.x, anchorPt.y, lastPt.x,   lastPt.y);
    }
    
    public void drawNext() {
        drawLine(anchorPt.x, anchorPt.y, stretchedPt.x, stretchedPt.y);
    }
    
    public void drawLine(int x, int y, int x2, int y2) {
    	aLine.reset();
    	aLine.moveTo((float)x, (float)y);
    	aLine.lineTo((float)x2, (float)y2);
		((RubberbandPanel)component).setRubberbandDisplayObject(aLine, false);
    }
    
    public int getUpperLeftX() {
		Rectangle newBounds = lastBounds();
		return newBounds.x;
    }
    
	public int getUpperLeftY() {
		Rectangle newBounds = lastBounds();
		return newBounds.y;
    }
    
	public int getLowerRightX() {
		Rectangle newBounds = lastBounds();
		return newBounds.x + newBounds.width;
    }
    
	public int getLowerRightY() {
		Rectangle newBounds = lastBounds();
		return newBounds.y + newBounds.height;
    }
    
	public int getLowerLeftX() {
		Rectangle newBounds = lastBounds();
		return newBounds.x;
    }
    
	public int getLowerLeftY() {
		Rectangle newBounds = lastBounds();
		return newBounds.y + newBounds.height;
    }
    
	public int getUpperRightX() {
		Rectangle newBounds = lastBounds();
		return newBounds.x + newBounds.width;
    }
    
	public int getUpperRightY() {
		Rectangle newBounds = lastBounds();
		return newBounds.y;
    }	 
       
    public void resetRect() {
    }
		    
    public boolean isPoint() {
    	return false;
    }
}

/*
 * $Id: RubberbandVLine.java,v 1.4 2005/06/17 18:10:59 oz Exp $
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
 * @author  David Geary
 * @see     Rubberband
 * @see     gjt.test.RubberbandTest
 */
public class RubberbandVLine extends Rubberband {
	GeneralPath aPath = new GeneralPath();
	
	public RubberbandVLine() {
	}
    public RubberbandVLine(Component component) {
        super(component);
    }
    public void drawLast() {
    	drawVLine(anchorPt, lastPt);
    }
    public void drawNext() {
    	drawVLine(anchorPt, stretchedPt);
    }
    
    public void drawVLine(Point startPt, Point thePt) {
		aPath.reset();
		if (thePt.y >= startPt.y) {
			aPath.moveTo(startPt.x-4, startPt.y-1);
			aPath.lineTo(startPt.x+4, startPt.y-1);
			aPath.moveTo(startPt.x, startPt.y);
			aPath.lineTo(thePt.x, thePt.y);
			aPath.moveTo(thePt.x-4, thePt.y+1);
			aPath.lineTo(thePt.x+4, thePt.y+1);
		}
		else {
			aPath.moveTo(startPt.x-4, startPt.y+1);
			aPath.lineTo(startPt.x+4, startPt.y+1);
			aPath.moveTo(startPt.x, startPt.y);
			aPath.lineTo(thePt.x, thePt.y);
			aPath.moveTo(thePt.x-4, thePt.y-1);
			aPath.lineTo(thePt.x+4, thePt.y-1);
		}
		((RubberbandPanel)component).setRubberbandDisplayObject(aPath, false);
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

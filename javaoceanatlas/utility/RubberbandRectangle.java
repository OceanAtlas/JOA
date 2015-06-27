/*
 * $Id: RubberbandRectangle.java,v 1.7 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.Component;
import java.awt.Rectangle;
import javaoceanatlas.ui.RubberbandPanel;
import java.awt.geom.*;

/** 
 * A Rubberband that does rectangles.
 *
 * @version 1.00, 12/27/95
 * @author  David Geary
 * @see     Rubberband
 * @see     gjt.test.RubberbandTest
 */
public class RubberbandRectangle extends Rubberband {
	GeneralPath mSelectionRect = new GeneralPath();
	public RubberbandRectangle() {
	}
    public RubberbandRectangle(Component component) {
        super(component);
    }
    public void drawLast() {
        // get selection object
        Rectangle r = lastBounds();
		mSelectionRect.reset();
		mSelectionRect.moveTo((float)r.getX(), (float)r.getY());
		mSelectionRect.lineTo((float)(r.getX() + r.getWidth()), (float)r.getY());
		mSelectionRect.lineTo((float)(r.getX() + r.getWidth()), (float)(r.getY() + r.getHeight()));
		mSelectionRect.lineTo((float)r.getX(), (float)(r.getY() + r.getHeight()));
		mSelectionRect.lineTo((float)r.getX(), (float)r.getY());
        //((RubberbandPanel)component).setRubberbandDisplayObject(mSelectionRect, false);
    }
    public void drawNext() {
        Rectangle r = getBounds();
		mSelectionRect.reset();
		mSelectionRect.moveTo((float)r.getX(), (float)r.getY());
		mSelectionRect.lineTo((float)(r.getX() + r.getWidth()), (float)r.getY());
		mSelectionRect.lineTo((float)(r.getX() + r.getWidth()), (float)(r.getY() + r.getHeight()));
		mSelectionRect.lineTo((float)r.getX(), (float)(r.getY() + r.getHeight()));
		mSelectionRect.lineTo((float)r.getX(), (float)r.getY());
        ((RubberbandPanel)component).setRubberbandDisplayObject(mSelectionRect, false);
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
    	if (Math.abs(endPt.x - anchorPt.x) > 5 || Math.abs(endPt.y - anchorPt.y) > 5)
    		return false;
    	else
    		return true;
    }
}

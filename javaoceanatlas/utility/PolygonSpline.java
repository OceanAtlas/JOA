/*
 * $Id: PolygonSpline.java,v 1.4 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javaoceanatlas.ui.*;


public class PolygonSpline extends Rubberband {
	boolean mIstarted = false;
	int mStartWidth;
	MapPlotPanel mMap = null;
	GeneralPath aLine = new GeneralPath();
	
    public void drawLast() {
    	double dx = anchorPt.x - lastPt.x;
    	double dy = anchorPt.y - lastPt.y;
    	
    	if (dx == 0 && dy == 0)
    		return;
        drawLine(anchorPt.x, anchorPt.y, 
		                  lastPt.x,   lastPt.y);
    }
    
    public void drawNext() {
    	double dx = anchorPt.x - stretchedPt.x;
    	double dy = anchorPt.y - stretchedPt.y;
    	
    	if (dx == 0 && dy == 0)
    		return;
        drawLine(anchorPt.x, anchorPt.y, 
		                  stretchedPt.x,   stretchedPt.y);
    }
    
    public void drawPtToPt(Graphics graphics, Point p1, Point p2) {
    	double dx = p1.x - p2.x;
    	double dy = p1.y - p2.y;
    	
    	if (dx == 0 && dy == 0)
    		return;
        drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    
    public void drawLine(int x, int y, int x2, int y2) {
    	aLine.reset();
    	aLine.moveTo((float)x, (float)y);
    	aLine.lineTo((float)x2, (float)y2);
		((RubberbandPanel)component).setRubberbandDisplayObject(aLine, false);
    }

	public PolygonSpline() {
	}
	
	public PolygonSpline(Component c) {
		setComponent(c);
	}
			
	public void setComponent(Component c) { 
		component = c; 
		mMap = (MapPlotPanel)c;
		
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if(isActive()) {
					// start the spline	
					anchor(me.getPoint());
					
					if (!mIstarted) {
						// store anchor point
						mMap.getSplinePoints().removeAllElements();
						mMap.addSplinePoint(new Point(anchorPt));
						mIstarted = true;
					}
					else {
						if (me.getClickCount() == 2 && mIstarted) {
							// store point
							mMap.addSplinePoint(new Point(anchorPt));
							mIstarted = false;
							me.consume();
							mMap.processPolygonSpline(me.isShiftDown());
						}
						else {
							mMap.addSplinePoint(new Point(anchorPt));
						}
						end(me.getPoint());
					}
				}
			}

		});
		component.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent event) {
				if(isActive() && mIstarted)
					stretch(event.getPoint());
			}
		});
	}
	
    
    public boolean splineActive() {
    	return mIstarted;
    }
	    
    public int getUpperLeftX() {
		Rectangle newBounds = lastBounds();
		return newBounds.x;
    }
    
	public int getLowerRightX() {
		Rectangle newBounds = lastBounds();
		return newBounds.x + newBounds.width;
    }
    
	public int getUpperLeftY() {
		Rectangle newBounds = lastBounds();
		return newBounds.y;
    }
    
	public int getLowerRightY() {
		Rectangle newBounds = lastBounds();
		return newBounds.y + newBounds.height;
    }
	
	public int getUpperRightX() {return -99;}
	
	public int getUpperRightY() {return -99;}
	
	public int getLowerLeftX() {return -99;}
	
	public int getLowerLeftY() {return -99;}
	
	public void resetRect() {}
	    
    public boolean isPoint() {
    	return false;
    }
}

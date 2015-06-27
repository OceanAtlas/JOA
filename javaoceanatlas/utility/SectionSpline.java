/*
 * $Id: SectionSpline.java,v 1.4 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.*;
import java.awt.geom.*;

public class SectionSpline extends Rubberband {
	boolean mIstarted = false;
	int mStartWidth;
	MapPlotPanel mMap = null;
	GeneralPath aShape = new GeneralPath();

	public SectionSpline() {
	}
	
	public SectionSpline(Component c) {
		setComponent(c);
	}
	
	public void drawSpline(int x, int y, int x2, int y2) {
    	float dx = x - x2;
    	float dy = y - y2;
    	int width = ComputeSectionPixelWidth(x2, y2);
    	
    	if (dx == 0 && dy == 0)
    		return;
		float dist = (float)Math.pow(dx*dx + dy*dy, 0.5);
    		
		float p1x = x + (float)mStartWidth * (-dy/dist);
		float p1y = y + (float)mStartWidth * (dx/dist);
		
		float p2x = x - (float)mStartWidth * (-dy/dist);
		float p2y = y - (float)mStartWidth * (dx/dist);
		
		float p3x = x2 + (float)width * (-dy/dist);
		float p3y = y2 + (float)width * (dx/dist);
		
		float p4x = x2 - (float)width * (-dy/dist);
		float p4y = y2 - (float)width * (dx/dist);
    	aShape.reset();
        aShape.moveTo(p1x, p1y);
        aShape.lineTo(p2x, p2y);
        aShape.lineTo(p4x, p4y);
        aShape.lineTo(p3x, p3y);
        aShape.lineTo(p1x, p1y);
        aShape.moveTo((float)x, (float)y); 
		aShape.lineTo((float)x2,(float)y2);
		((RubberbandPanel)component).setRubberbandDisplayObject(aShape, false);
	}
	
    public void drawLast() {
    	drawSpline(anchorPt.x, anchorPt.y, lastPt.x, lastPt.y);
    }
    
    public void drawNext() {
    	drawSpline(anchorPt.x, anchorPt.y, stretchedPt.x, stretchedPt.y);
    }
    
    public void drawPtToPt(Point p1, Point p2) {
    	drawSpline(p1.x, p1.y, p2.x, p2.y);
    	/*double dx = p1.x - p2.x;
    	double dy = p1.y - p2.y;
    	int width1 = ComputeSectionPixelWidth(p1);
    	int width = ComputeSectionPixelWidth(p2);
    	
    	if (dx == 0 && dy == 0)
    		return;
		double dist = Math.pow(dx*dx + dy*dy, (double)0.5);
    		
		double p1x = p1.x + (double)width1 * (-dy/dist);
		double p1y = p1.y + (double)width1 * (dx/dist);
		
		double p2x = p1.x - (double)width1 * (-dy/dist);
		double p2y = p1.y - (double)width1 * (dx/dist);
		
		double p3x = p2.x + (double)width * (-dy/dist);
		double p3y = p2.y + (double)width * (dx/dist);
		
		double p4x = p2.x - (double)width * (-dy/dist);
		double p4y = p2.y - (double)width * (dx/dist);
    	
        graphics.drawLine((int)p1x, (int)p1y, 
                          (int)p2x, (int)p2y);
        graphics.drawLine((int)p2x, (int)p2y, 
                          (int)p4x, (int)p4y);
        graphics.drawLine((int)p4x, (int)p4y, 
                          (int)p3x, (int)p3y);
        graphics.drawLine((int)p3x, (int)p3y, 
                          (int)p1x, (int)p1y);
        graphics.drawLine(p1.x, p1.y, p2.x, p2.y);*/
    }
			
	public void setComponent(Component c) { 
		component = c; 
		
		mMap = (MapPlotPanel)c;
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if(isActive()) {
					// start the spline	
					anchor(me.getPoint());
					mStartWidth = ComputeSectionPixelWidth(anchorPt);
					
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
							mMap.processSectionSpline();
						}
						else {
							mMap.addSplinePoint(new Point(anchorPt));
							((RubberbandPanel)component).setRubberbandDisplayObject(null, false);
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
	
    public void anchor(Point p) {
    	int left = ((RubberbandPanel)component).getMinX();
    	int right = ((RubberbandPanel)component).getMaxX();
    	int top = ((RubberbandPanel)component).getMinY();
    	int bottom = ((RubberbandPanel)component).getMaxY();
    	if (p.x < left)
        	anchorPt.x = left + 1;
    	else if (p.x > right)
        	anchorPt.x = right - 1;
    	else 
        	anchorPt.x = p.x;
        	
    	if (p.y < top)
        	anchorPt.y = top + 1;
    	else if (p.y > bottom)
        	anchorPt.y = bottom - 1;
    	else 
        	anchorPt.y = p.y;
        	
        firstStretch = true;
        stretchedPt.x = lastPt.x = anchorPt.x;
        stretchedPt.y = lastPt.y = anchorPt.y;
    }
    
    public void stretch(Point p) {
    	int left = ((RubberbandPanel)component).getMinX();
    	int right = ((RubberbandPanel)component).getMaxX();
    	int top = ((RubberbandPanel)component).getMinY();
    	int bottom = ((RubberbandPanel)component).getMaxY();
        lastPt.x      = stretchedPt.x;
        lastPt.y      = stretchedPt.y;
        
        if (p.x < left)
        	stretchedPt.x = left + 1;
    	else if (p.x > right)
        	stretchedPt.x = right - 1;
    	else 
        	stretchedPt.x = p.x;
        	
    	if (p.y < top)
        	stretchedPt.y = top + 1;
    	else if (p.y > bottom)
        	stretchedPt.y = bottom - 1;
    	else 
        	stretchedPt.y = p.y;
        	        
       drawNext();
    }
    public void end(Point p) {
    	int left = ((RubberbandPanel)component).getMinX();
    	int right = ((RubberbandPanel)component).getMaxX();
    	int top = ((RubberbandPanel)component).getMinY();
    	int bottom = ((RubberbandPanel)component).getMaxY();
        
        if (p.x < left)
        	lastPt.x = endPt.x = left + 1;
    	else if (p.x > right)
        	lastPt.x = endPt.x = right - 1;
    	else 
        	lastPt.x = endPt.x = p.x;
        	
    	if (p.y < top)
        	lastPt.y = endPt.y = top + 1;
    	else if (p.y > bottom)
        	lastPt.y = endPt.y = bottom - 1;
    	else 
        	lastPt.y = endPt.y = p.y;
    }
    
    public boolean splineActive() {
    	return mIstarted;
    }
	
	public int ComputeSectionPixelWidth(Point p) {
		UVCoordinate uv = mMap.invTransformLL(p.x ,p.y);
		double stLon = uv.u;
		double stLat = uv.v;
	
		// keeping lon constant, iteratively find a matching great circle distance
		double tstLat = stLat;
		double d;
		while (true) {
			d = JOAFormulas.GreatCircle(stLat, stLon, tstLat, stLon);
			d /= 0.62;
			if (d >= JOAConstants.SECTION_WIDTH) {
				break;
			}
			tstLat += 0.1;
		}
		uv = mMap.transformLL(tstLat, stLon);
		uv = mMap.mapScaler(uv.u, uv.v);
		int pd = ((int)uv.v - p.y)/2;
		pd = pd < 0 ? -pd : pd;
		return pd == 0 ? 1 : pd;
	}
	
	public int ComputeSectionPixelWidth(int x, int y) {
		UVCoordinate uv = mMap.invTransformLL(x ,y);
		double stLon = uv.u;
		double stLat = uv.v;
	
		// keeping lon constant, iteratively find a matching great circle distance
		double tstLat = stLat;
		double d;
		while (true) {
			d = JOAFormulas.GreatCircle(stLat, stLon, tstLat, stLon);
			d /= 0.62;
			if (d >= JOAConstants.SECTION_WIDTH) {
				break;
			}
			tstLat += 0.1;
		}
		uv = mMap.transformLL(tstLat, stLon);
		uv = mMap.mapScaler(uv.u, uv.v);
		int pd = ((int)uv.v - y)/2;
		pd = pd < 0 ? -pd : pd;
		return pd == 0 ? 1 : pd;
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
	
	public int getUpperRightX() {return JOAConstants.MISSINGVALUE;}
	
	public int getUpperRightY() {return JOAConstants.MISSINGVALUE;}
	
	public int getLowerLeftX() {return JOAConstants.MISSINGVALUE;}
	
	public int getLowerLeftY() {return JOAConstants.MISSINGVALUE;}
	
	public void resetRect() {}
	    
    public boolean isPoint() {
    	return false;
    }
}

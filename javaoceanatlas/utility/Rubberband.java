/*
 * $Id: Rubberband.java,v 1.4 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.*;

/** 
 * An abstract base class for rubberbands.<p>
 *
 * Rubberbands do their rubberbanding inside of a Component, 
 * which must be specified at construction time.<p>
 * 
 * Subclasses are responsible for implementing 
 * <em>void drawLast(Graphics g)</em> and 
 * <em>void drawNext(Graphics g)</em>.  
 *
 * drawLast() draws the appropriate geometric shape at the last 
 * rubberband location, while drawNext() draws the appropriate 
 * geometric shape at the next rubberband location.  All of the 
 * underlying support for rubberbanding is taken care of here, 
 * including handling XOR mode setting; extensions of Rubberband
 * need not concern themselves with anything but drawing the 
 * last and next geometric shapes.  Extensions may get information
 * about where to draw their shape from getAnchor(), getStretched(),
 * and getLast(), and getEnd(), all of which return a Point.<p>
 *
 * @version 1.0, Dec 27 1995
 * @version 1.1, Nov 8  1996
 *
 *    Changed names of instance variables with same names as 
 *    methods to work around bug in Microsoft compiler.
 *
 * @author  David Geary
 * @see     RubberbandLine
 * @see     RubberbandRectangle
 * @see     RubberbandEllipse
 * @see     gjt.test.RubberbandTest
 */
abstract public class Rubberband {
    protected Point anchorPt    = new Point(0,0); 
    protected Point stretchedPt = new Point(0,0);
    protected Point lastPt      = new Point(0,0); 
    protected Point endPt       = new Point(0,0);
    protected Color mForeColor = Color.white;//Color.gray;
    protected Color mXORColor = Color.black;

    protected Component component;
    public boolean   firstStretch = true;
	protected boolean   active = false;
	protected boolean constrainVertical = false;

    abstract public void drawLast();
    abstract public void drawNext();

	public Rubberband() {
	}
	public Rubberband(Component c) {
		setComponent(c);
	}
	public void setActive(boolean b) {
		active = b;
	}
	public void setConstrainVertical(boolean b) {
		constrainVertical = b;
	}
	public void setComponent(Component c) { 
		component = c; 

		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {	
				if(isActive()) {
					anchor(event.getPoint());
				}
			}
			public void mouseClicked(MouseEvent event) {
				if(isActive())
					end(event.getPoint());
			}
			public void mouseReleased(MouseEvent event) {
				if(isActive())
					end(event.getPoint());
			}
		});
		component.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent event) {
				if(isActive())
					stretch(event.getPoint());
			}
		});
	}
	public boolean isActive    () { return active;      }
    public Point   getAnchor   () { return anchorPt;    }
    public Point   getStretched() { return stretchedPt; }
    public Point   getLast     () { return lastPt;      }
    public Point   getEnd      () { return endPt;       }

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
        	        
        if (constrainVertical)
        	stretchedPt.x = anchorPt.x;
        	
        //set the selection object in the component
        if(firstStretch == true) 
        	firstStretch = false;
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
        	        
        if (constrainVertical)
        	lastPt.x = endPt.x = anchorPt.x;
    }
    
    public Rectangle getBounds() {
      return new Rectangle(stretchedPt.x < anchorPt.x ? 
                           stretchedPt.x : anchorPt.x,
                           stretchedPt.y < anchorPt.y ? 
                           stretchedPt.y : anchorPt.y,
                           Math.abs(stretchedPt.x - anchorPt.x),
                           Math.abs(stretchedPt.y - anchorPt.y));
    }

    public Rectangle lastBounds() {
      return new Rectangle(
                  lastPt.x < anchorPt.x ? lastPt.x : anchorPt.x,
                  lastPt.y < anchorPt.y ? lastPt.y : anchorPt.y,
                  Math.abs(lastPt.x - anchorPt.x),
                  Math.abs(lastPt.y - anchorPt.y));
    }
    
    public void setForeColor(Color fc) {
    	mForeColor = fc;
    }
    
    public void setXORColor(Color c) {
    	mXORColor = c;
    }
    
    public boolean splineActive() {
    	return false;
    }
    
    abstract public int getUpperLeftX();
	abstract public int getUpperLeftY();
	abstract public int getLowerRightX();
	abstract public int getLowerRightY();
	abstract public int getLowerLeftX();
	abstract public int getLowerLeftY();
	abstract public int getUpperRightX();
	abstract public int getUpperRightY();
	abstract public void resetRect();
	abstract public boolean isPoint();
}

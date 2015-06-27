/*
 * $Id: LatTimeView.java,v 1.18 2005/03/23 23:52:21 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.*;
import java.util.*;
import gov.noaa.pmel.text.*;
import gov.noaa.pmel.swing.*;
import java.util.Date;
import java.util.TimeZone;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */

public class LatTimeView extends CutPanelView {
    private ViewToggleButn tb;
    private boolean TRACE = false;
	
    // --------------------------------------------------------------------------
    // 
    // constructor
    //
	public LatTimeView(Object parentObject) {
		super(LatTimeConstants.intValue, LatTimeConstants.viewName, parentObject);
		mViewManager = (ViewManager)parentObject;
		
		if (TRACE) 
			System.out.println("LatTimeView constructor entered");
		
		tb = new ViewToggleButn(LatTimeConstants.standardGif, LatTimeConstants.toolTipText);
		setToolbarButton(tb);
		try {
	    setImageIcon(Class.forName("ndEdit.NdEdit").getResource(LatTimeConstants.standardGif), "Lat: ","Time: ");
    }
    catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    	System.out.println("LatTimeView:ctor");
    }
		this.setTextFieldTypes(Constants.LAT_FIELD, 0,0, Constants.TIME_FIELD, 0,0);
		this.setMsg(LatTimeConstants.toolTipText);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		rbRect = new RubberbandRectangle(this);
		rbRect.setActive(true);
		this.invalidate();
		this.validate();
	}
			
	/*public void rubberbandEnded(Rubberband rb, boolean shiftDown) {
		// get the limits and zoom the plot
		Rectangle rbRect = rb.getBounds();
		if (rbRect.width == 0 || rbRect.height == 0)
			return;
		int x = rbRect.x - mLeft - 5;
		int y = rbRect.y - mTop - 5;
		int x2 = x + rbRect.width;
		int y2 = y + rbRect.height;
	
		double lat = (y/winYScale) + winYOrigin;
		double lat2 = (y2/winYScale) + winYOrigin;
		double tim = (x/winXScale) + winXOrigin;
		double tim2 = (x2/winXScale) + winXOrigin;
		
		double [] oldtimes = {getMinXVal(), getMaxXVal()};
		double [] newtimes = {tim, tim2};    
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomTimeDomain", oldtimes, newtimes, false);
		
		double [] oldlats = {getMinYVal(), getMaxYVal()};
		double [] newlats = {lat2, lat};    
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldlats, newlats, false);
		
		//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomLatitudeStart", new Double(getMinYVal()), new Double(lat2), false);
    	//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomLatitudeStop", new Double(getMaxYVal()), new Double(lat), false);
		//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomTimeStart", new Double(getMinXVal()), new Double(tim), false);
    	//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomTimeStop", new Double(getMaxXVal()), new Double(tim2), false);
    	mViewManager.invalidateAllViews();
	}*/
	
	public void zoomDomain(double[] oldYs, double[] newYs, double[] oldXs, double[] newXs) {
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("StartBatch", null, null, false);
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldYs, newYs, false);
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomTimeDomain", oldXs, newXs, false);
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("EndBatch", null, null, false);
	}
	
	public boolean isXAxisScaler() {
		return mCurrPC.getTimeArr2() == null;
	}
	
	public boolean isYAxisScaler() {
		return mCurrPC.getLatArr2() == null;
	}
	
	public double[] getXArray1() {
		return mCurrPC.getTimeArr1();
	}
	
	public double[] getXArray2() {
		return mCurrPC.getTimeArr2();
	}
	
	public double[] getYArray1() {
		return mCurrPC.getLatArr1();
	}
	
	public double[] getYArray2() {
		return mCurrPC.getLatArr2();
	}
	
	public double correctX(double x) {
		return x;
	}
	
	public double correctY(double y) {
		return y;
	}
	
	public double getXZoomIncrement() {
		return (mCurrPC.getMaxTime() - mCurrPC.getMinTime())/10.0; 
	}
	
	public double getYZoomIncrement() {
		return (mCurrPC.getMaxLat() - mCurrPC.getMinLat())/10.0; 
	}
	
	
	public void handleAxisClick(String theAxis, Point thePt) {
		// convert the point to a value on the axis
		double axisVal;
		int x = thePt.x - mLeft - 5;
		int y = thePt.y - mTop - 5;
		if (theAxis.equals("X"))
			axisVal = (x/winXScale) + winXOrigin;
		else {
			axisVal = (y/winYScale) + winYOrigin;
		}

		// get the closest draghandle
		double diff1 = 1.0e32f;
		double diff2 = 1.0e32f;
		double diff3 = 1.0e32f;
		boolean leftOOR = mXAxisLeft.isOutOfRange();
		boolean rightOOR = mXAxisRight.isOutOfRange();
		boolean topOOR = mYAxisUpper.isOutOfRange();
		boolean bottOOR = mYAxisLower.isOutOfRange();
		boolean xCtrOOR = mXAxisCtr.isOutOfRange();
		boolean yCtrOOR = mYAxisCtr.isOutOfRange();
		if (theAxis.equals("X")) {
			if (leftOOR && rightOOR && xCtrOOR) {
				// whole selection is out of range--translate the selection to this new center
				double leftVal = (double)mXAxisLeft.getValue();
				double rightVal = (double)mXAxisRight.getValue();
				double ctrVal = (double)mXAxisCtr.getValue();
				diff1 = Math.abs(ctrVal - leftVal);
				diff2 = Math.abs(rightVal - ctrVal);
				setHandle(null, "timctr", new Double(axisVal), true);
				setHandle(null, "timmin", new Double(axisVal - diff1), true);
				setHandle(null, "timmax", new Double(axisVal + diff2), true);
				mXAxisRight.broadcast(true);
				mXAxisLeft.broadcast(true);
				mXAxisCtr.broadcast(true);
			}
			else {
				// current values of the selection
				double leftVal = (double)mXAxisLeft.getValue();
				double rightVal = (double)mXAxisRight.getValue();
				double ctrVal = (double)mXAxisCtr.getValue();
				
				if (leftOOR)
					leftVal = getMinXVal();
				if (rightOOR)
					rightVal = getMaxXVal();
				if (xCtrOOR)
					ctrVal = leftVal + ((rightVal - leftVal)/2.0f);
				
				if (!xCtrOOR) {
					diff1 = Math.abs(leftVal - axisVal);
					diff2 = Math.abs(ctrVal - axisVal);
					diff3 = Math.abs(rightVal - axisVal);
					if (diff1 < diff2 && diff1 < diff3) {
						setHandle(null, "timmin", new Double(axisVal), true);
						mXAxisLeft.broadcast(true);
					}
					else if (diff2 < diff1 && diff2 < diff3) {
						setHandle(null, "timctr", new Double(axisVal), true);
						mXAxisCtr.broadcast(true);
					}
					else if (diff3 < diff1 && diff3 < diff2) {
						setHandle(null, "timmax", new Double(axisVal), true);
						mXAxisRight.broadcast(true);
					}
				}
				else {
					if (rightOOR) {
						// move the center handle to mouseclick, left handle stays stationary
						setHandle(null, "timctr", new Double(axisVal), true);
						setHandle(null, "timmax", new Double(axisVal + (axisVal - leftVal)), true);
						mXAxisRight.broadcast(true);
					}
					else if (leftOOR) {
						// move the center handle to mouseclick, right handle stays stationary
						setHandle(null, "timctr", new Double(axisVal), true);
						setHandle(null, "timmin", new Double(axisVal - (rightVal - axisVal)), true);
						mXAxisLeft.broadcast(true);
					}
				}
			}
		}
		else {
			if (topOOR && bottOOR && yCtrOOR) {
				// whole selection is out of range--translate the selection to this new center
				double topVal = (double)mYAxisUpper.getValue();
				double bottVal = (double)mYAxisLower.getValue();
				double ctrVal = (double)mYAxisCtr.getValue();
				diff1 = Math.abs(ctrVal - topVal);
				diff2 = Math.abs(bottVal - ctrVal);
				setHandle(null, "latctr", new Double(axisVal), true);
				setHandle(null, "latmin", new Double(axisVal - diff1), true);
				setHandle(null, "latmax", new Double(axisVal + diff2), true);
				mYAxisUpper.broadcast(true);
				mYAxisLower.broadcast(true);
				mYAxisCtr.broadcast(true);
			}
			else {
				double upperVal = (double)mYAxisUpper.getValue();
				double lowerVal = (double)mYAxisLower.getValue();
				double ctrVal = (double)mYAxisCtr.getValue();
				
				if (topOOR)
					upperVal = getMaxYVal();
				if (bottOOR)
					lowerVal = getMinYVal();
				if (yCtrOOR)
					ctrVal = lowerVal + ((upperVal - lowerVal)/2.0f);
					
				if (!yCtrOOR) {	
					diff1 = Math.abs(upperVal - axisVal);
					diff2 = Math.abs(ctrVal - axisVal);
					diff3 = Math.abs(lowerVal - axisVal);
					if (diff1 < diff2 && diff1 < diff3) {
						setHandle(null, "latmax", new Double(axisVal), true);
						mYAxisUpper.broadcast(true);
					}
					else if (diff2 < diff1 && diff2 < diff3) {
						setHandle(null, "latctr", new Double(axisVal), true);
						mYAxisCtr.broadcast(true);
					}
					else if (diff3 < diff1 && diff3 < diff2) {
						setHandle(null, "latmin", new Double(axisVal), true);
						mYAxisLower.broadcast(true);
					}
				}
				else {
					if (topOOR) {
						// move the center handle to mouseclick, bottom handle stays stationary
						setHandle(null, "latctr", new Double(axisVal), true);
						setHandle(null, "latmax", new Double(axisVal + (axisVal - lowerVal)), true);
						mYAxisUpper.broadcast(true);
					}
					else if (bottOOR) {
						// move the center handle to mouseclick, top handle stays stationary
						setHandle(null, "latctr", new Double(axisVal), true);
						setHandle(null, "latmin", new Double(axisVal - (upperVal - axisVal)), true);
						mYAxisLower.broadcast(true);
					}
				}
			}
		}
	
	}
		
    public void configureToolBelt() {
    }
    
    public void paintPanelData(Graphics gin, int width, int height, int top, int left, int bott, int right) {
		if (gin == null)
			return;
		Graphics2D g = (Graphics2D)gin;
		double minLat;
		double maxLat;
		double minT;
		double maxT;
		double selMinLat = 0.0f;
		double selMaxLat = 0.0f;
		double selMinT = 0.0f;
		double selMaxT = 0.0f;
		mTop = top;
		mLeft = left;
		mRight = right;
		mBottom = bott;
		mWidth = width;
		mHeight = height;
		
		// initialize the axes ranges
		if (mCurrPC == null) 
			return;
			
		firstPlotDone = true; 
		
		if (!axesInitialized) {
			// get a range from the view manager if these ranges have been set by another view
			Float latMin = vm.getAxisMin(Constants.LAT_AXIS, Constants.LAT_TIME);
			Float latMax = vm.getAxisMax(Constants.LAT_AXIS, Constants.LAT_TIME);
			Float tMin = vm.getAxisMin(Constants.TIME_AXIS, Constants.LAT_TIME);
			Float tMax = vm.getAxisMax(Constants.TIME_AXIS, Constants.LAT_TIME);
			Float selLatMin = vm.getAxisSelMin(Constants.LAT_AXIS, Constants.LAT_TIME);
			Float selLatMax = vm.getAxisSelMax(Constants.LAT_AXIS, Constants.LAT_TIME);
			Float selTMin = vm.getAxisSelMin(Constants.TIME_AXIS, Constants.LAT_TIME);
			Float selTMax = vm.getAxisSelMax(Constants.TIME_AXIS, Constants.LAT_TIME);
			
			if ((latMin != null && latMax != null && (latMin.floatValue() != 0.0 && latMax.floatValue() != 0.0)) &&
			    (selLatMin != null && selLatMax != null && (selLatMin.floatValue() != 0.0 && selLatMax.floatValue() != 0.0))) {
				setMinYVal(latMin.floatValue());
				setMaxYVal(latMax.floatValue());
				selMinLat = selLatMin.floatValue();
				selMaxLat = selLatMax.floatValue();
			}
			else {
				if (mCurrPC.getMaxLat() != mCurrPC.getMinLat()) {
					// compute a nice range
					double range = Math.abs(mCurrPC.getMaxLat() - mCurrPC.getMinLat());
					setMinYVal(NdEditFormulas.getNiceLowerValue(mCurrPC.getMinLat(), range, -90.0));
					setMaxYVal(NdEditFormulas.getNiceUpperValue(mCurrPC.getMaxLat(), range, 90.0));
					selMinLat = getMinYVal();
					selMaxLat = getMaxYVal();
				}
				else {
					// point observation
					setMinYVal(mCurrPC.getMinLat() - 1.0);
					setMaxYVal(mCurrPC.getMaxLat() + 1.0);
					selMinLat = getMinYVal();
					selMaxLat = getMaxYVal();
				}
			}
			
			if ((tMin != null && tMax != null && (tMin.floatValue() != 0.0 && tMax.floatValue() != 0.0)) &&
			    (selTMin != null && selTMax != null && (selTMin.floatValue() != 0.0 && selTMax.floatValue() != 0.0))) {
				setMinXVal(tMin.floatValue());
				setMaxXVal(tMax.floatValue());
				selMinT = selTMin.floatValue();
				selMaxT = selTMax.floatValue();
			}
			else {
				if (mCurrPC.getMaxTime() != mCurrPC.getMinTime()) {
					// compute a nice range
					double range = Math.abs(mCurrPC.getMaxTime() - mCurrPC.getMinTime());
					setMinXVal(NdEditFormulas.getNiceLowerValue(mCurrPC.getMinTime(), range));
					setMaxXVal(NdEditFormulas.getNiceUpperValue(mCurrPC.getMaxTime(), range));
					selMinT = getMinXVal();
					selMaxT = getMaxXVal();
				}
				else {
					// point observation
					setMinXVal(mCurrPC.getMinTime() - 86400000.0);
					setMaxXVal(mCurrPC.getMaxTime() + 86400000.0);
					selMinT = getMinXVal();
					selMaxT = getMaxXVal();
				}
			}
			axesInitialized = true;
		}
		
		// scale the axes
		minLat = getMinYVal();
		maxLat = getMaxYVal();
		minT = getMinXVal();
		maxT = getMaxXVal();
				
		if (plotAxes) {
			mTop += 10;
			mLeft += 25;
			mRight += 20;
			mBottom += 40;		
		}
		
		// x axis
		scaleXAxis();
		int scaleWidth = width;
		scaleWidth -= (mLeft + mRight);
		
		// y axis
		scaleYAxis();
		int scaleHeight = height;
		scaleHeight -= (mBottom + mTop);
		
		// set the clip
		setClip(g);
		
		g.setColor(new Color(200, 200, 200));
		g.fillRect(this.getMinX()-10, this.getMinY()-10, this.getMaxX()-this.getMinX()+10,
                                   this.getMaxY()-this.getMinY()+30);
				
		//draw the points
		BasicStroke bs2 = new BasicStroke(2);
		BasicStroke bs1 = new BasicStroke(1);
		byte[] currFilterResults = fdm.getResults();
		int numVPoints = mCurrPC.getSize();
		int maxx = this.getMaxX();
		int maxy = this.getMaxY();
		//boolean[][] t = new boolean[maxx][maxy];
		
		for (int i=0; i<numVPoints; i++) {
			if (!mIgnoreFilter && (mCurrPC.isDeleted(i) || currFilterResults[i] != 4))
				continue;
				
			boolean selected = mCurrPC.isSelected(i);
			double lat = mCurrPC.getLat1(i);
			double time = mCurrPC.getT1(i);
			double x = (time - winXOrigin) * winXScale;
			double y = (lat - winYOrigin) * winYScale;
			
			/*if (!selected) {
				int ix = (int)x;
				int iy = (int)y;
				try {
					if (ix < 0 || ix >= maxx || 
						iy < 0 || iy >= maxy || t[ix][iy])
						continue;
					
					t[ix][iy] = true;
				}
				catch (Exception ex) {}
			}*/
			
			boolean xIsScaler = mCurrPC.isTimeScaler(i);
			boolean yIsScaler = mCurrPC.isLatScaler(i);
				
			//System.out.print("yIsScaler = " + yIsScaler);
			////System.out.print("xIsScaler = " + xIsScaler);
			//System.out.println("time = " + time);
			
			// adjust for axes labels
			x += mLeft;
			y += mTop;
			
			double x2 = Float.NaN;
			double y2 = Float.NaN;
			double lat2 = Float.NaN;
			double time2 = Float.NaN;
							
			if (!yIsScaler) {
				lat2 = mCurrPC.getLat2(i);
				y2 = (lat2 - winYOrigin) * winYScale;
				y2 += mTop;
			}
				
			if (!xIsScaler) {
				time2 = mCurrPC.getT2(i);
			//System.out.println(" time2 = " + time2);
				x2 = (time2 - winXOrigin) * winXScale;
				x2 += mLeft;
			}
			if (selected) {
				g.setColor(Color.blue);
				((Graphics2D)g).setStroke(bs2);
			}
			else {
				g.setColor(mCurrPC.getColor(i));
				((Graphics2D)g).setStroke(bs1);
			}
				
			int swidth = 4;
			if (yIsScaler && xIsScaler) {
				// point observation
				g.drawLine((int)(x-swidth/2), (int)y, (int)(x+swidth/2), (int)y);
				g.drawLine((int)x, (int)(y-swidth/2), (int)x, (int)(y+swidth/2));
			}
			if (yIsScaler && !xIsScaler) {
				// time range
				g.drawLine((int)x, (int)(y-swidth/2), (int)x, (int)(y+swidth/2));
				g.drawLine((int)x, (int)y, (int)x2, (int)y);
				g.drawLine((int)x2, (int)(y-swidth/2), (int)x2, (int)(y+swidth/2));
				
			}
			else if (!yIsScaler && xIsScaler) {
				// lat range
				g.drawLine((int)(x-swidth/2), (int)y, (int)(x+swidth/2), (int)y);
				g.drawLine((int)x, (int)y, (int)x, (int)y2);
				g.drawLine((int)(x-swidth/2), (int)y2, (int)(x+swidth/2), (int)y2);
			}
			else if (!yIsScaler && !xIsScaler) {
				g.setColor(mCurrPC.getColor(i));
				((Graphics2D)g).setStroke(bs1);
				g.drawLine((int)x, (int)y, (int)x2, (int)y2);
				g.drawLine((int)(x2-1), (int)y2, (int)(x2+1), (int)y2);
				g.drawLine((int)x2, (int)(y2-1), (int)x2, (int)(y2+1));

				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D)g).setStroke(bs2);
				}
				g.drawLine((int)(x-1), (int)y, (int)(x+1), (int)y);
				g.drawLine((int)x, (int)(y-1), (int)x, (int)(y+1));
			}
		}

		((Graphics2D)g).setStroke(bs1);
		g.setClip(0, 0, 1000, 1000);
   		SelectionRegion.selOffset = 0;
		
   		if (mXAxisLeft == null) {
   			int initH = mLeft + 5;
   			int initV = mTop + 5;
   			double initVal = selMinT;
			mXAxisLeft = new LeftDragHandle(this, initH, initV, DragHandle.LEFTHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.HORIENTATION, Constants.TIME_AXIS, mLeft+5, mLeft+scaleWidth+5, minT, maxT, initVal);
			startDate.setValue(new Date((long)initVal));
   		}
   		else {
   			// set the current location of the handle
   			mXAxisLeft.setAxisRange(mLeft+5, mLeft+scaleWidth+5);
   		}
   		
   		if (mXAxisCtr == null) {
   			int initH = mLeft + 5 + (int)(0.50 * scaleWidth);
   			int initV = mTop + 5;
			double xDiff = (selMaxT - selMinT);
   			double initVal = selMinT + (0.50f * xDiff);
			mXAxisCtr = new CenterHDragHandle(this, initH, initV, DragHandle.CENTERHHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.HORIENTATION, Constants.TIME_AXIS, mLeft+5, mLeft+scaleWidth+5, minT, maxT, initVal);
   		}
   		else {
   			// set the current location of the handle
   			mXAxisCtr.setAxisRange(mLeft+5, mLeft+scaleWidth+5);
   		}
   		
   		if (mXAxisRight == null) {
   			int initH = mLeft + 5 + scaleWidth;
   			int initV = mTop + 5;
   			double initVal = selMaxT;
			mXAxisRight = new RightDragHandle(this, initH, initV, DragHandle.RIGHTHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.HORIENTATION, Constants.TIME_AXIS, mLeft+5, mLeft+scaleWidth+5, minT, maxT, initVal);
			stopDate.setValue(new Date((long)initVal));
   		}
   		else{
   			mXAxisRight.setAxisRange(mLeft, mLeft+scaleWidth+5);
   		}
   		
   		if (mYAxisUpper == null) {
   			int initV = mTop + 5;
   			int initH = mLeft + scaleWidth + 5;
   			double initVal = selMaxLat;
			mYAxisUpper = new UpperDragHandle(this, initH, initV, DragHandle.UPPERHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.VORIENTATION, Constants.LAT_AXIS, mTop+5, mTop+scaleHeight+5, minLat, maxLat, initVal);
			stopLat.setValue(initVal);
   		}
   		else {
   			// set the current location of the handle
   			mYAxisUpper.setMaxH(mLeft + scaleWidth + 5);
   			mYAxisUpper.setAxisRange(mTop+5, mTop+scaleHeight+5);
   		}
   		
   		if (mYAxisCtr == null) {
   			int initV = mTop + 5 + (int)(0.50 * scaleHeight);
   			int initH = mLeft + scaleWidth + 5;
			double yDiff = (selMaxLat - selMinLat);
   			double initVal = selMaxLat - (0.50f * yDiff);
			mYAxisCtr = new CenterVDragHandle(this, initH, initV, DragHandle.CENTERVHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.VORIENTATION, Constants.LAT_AXIS, mTop+5, mTop+scaleHeight+5, minLat, maxLat, initVal);
   		}
   		else {
   			// set the current location of the handle
   			mYAxisCtr.setMaxH(mLeft + scaleWidth + 5);
   			mYAxisCtr.setAxisRange(mTop+5, mTop+scaleHeight+5);
   		}
   		
   		if (mYAxisLower == null) {
   			int initV = mTop + 5 + scaleHeight;
   			int initH = mLeft + scaleWidth + 5;
   			double initVal = selMinLat;
			mYAxisLower = new LowerDragHandle(this, initH, initV, DragHandle.LOWERHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.VORIENTATION, Constants.LAT_AXIS, mTop+5, mTop+scaleHeight+5, minLat, maxLat, initVal);
			startLat.setValue(initVal);
   		}
   		else {
   			// set the current location of the handle
   			mYAxisLower.setMaxH(mLeft + scaleWidth + 5);
   			mYAxisLower.setAxisRange(mTop+5, mTop+scaleHeight+5);
   		}
   		
   		// create a selection rectangle object
   		if (mHilitedRegion == null) {
   			mHilitedRegion = new SelectionRegion(mXAxisLeft, mXAxisCtr, mXAxisRight, mYAxisUpper, mYAxisCtr, mYAxisLower);
   			   		
   			// install the neigboring handles
   			mXAxisLeft.setFarNeighbor(mXAxisRight);
   			mXAxisLeft.setCtrNeighbor(mXAxisCtr);
   			
   			// install the neigboring handles
   			mXAxisRight.setFarNeighbor(mXAxisLeft);
   			mXAxisRight.setCtrNeighbor(mXAxisCtr);
   			
   			// install the neigboring handles
   			mXAxisCtr.setLeftOrUpperNeighbor(mXAxisLeft);
   			mXAxisCtr.setRightOrLowerNeighbor(mXAxisRight);
   			
   			// install the neigboring handles
   			mYAxisUpper.setFarNeighbor(mYAxisLower);
   			mYAxisUpper.setCtrNeighbor(mYAxisCtr);
   			
   			// install the neigboring handles
   			mYAxisLower.setFarNeighbor(mYAxisUpper);
   			mYAxisLower.setCtrNeighbor(mYAxisCtr);
   			
   			// install the neigboring handles
   			mYAxisCtr.setLeftOrUpperNeighbor(mYAxisUpper);
   			mYAxisCtr.setRightOrLowerNeighbor(mYAxisLower);
   			
   			// connect up the listeners
   			mXAxisLeft.addPropertyChangeListener(mViewManager);
   			mXAxisRight.addPropertyChangeListener(mViewManager);
   			mXAxisCtr.addPropertyChangeListener(mViewManager);
   			mYAxisUpper.addPropertyChangeListener(mViewManager);
   			mYAxisLower.addPropertyChangeListener(mViewManager);
   			mYAxisCtr.addPropertyChangeListener(mViewManager);
			stopLat.fireLatChange();
			startLat.fireLatChange();
			startDate.fireDateChangeable();
			stopDate.fireDateChangeable();
			setHandle(this, "latmin", new Double(selMinLat), true);
			setHandle(this, "latmax", new Double(selMaxLat), true);
			setHandle(this, "timmin", new Double(selMinT), true);
			setHandle(this, "timmax", new Double(selMaxT), true);
   		}
   		else {
   			// set the current selection region
   			SelectionRegion.selOffset = 0;
   			mHilitedRegion.resetRgnBounds();
   		}

		// draw the axes
		if (plotAxes) {
			drawXAxis(g, minT, maxT, false, "Time", height, width, mBottom, mTop, mLeft, mRight);
			drawYAxis(g, minLat, maxLat, false, "Latitude", height, width, mBottom, mTop, mLeft, mRight);
		}		
	}
	public void drawYAxis(Graphics2D g, double winYPlotMin, double winYPlotMax, 
					  boolean mReverseY, String axisLabel, int mHeightCurrWindow, 
					  int mWidthCurrWindow, int PVPBOTTOM, int PVPTOP, int PVPLEFT, int PVPRIGHT) {;
		g.setColor(Color.black);
		int bottom = (int)mHeightCurrWindow - 1 * PVPBOTTOM;
		int top = PVPTOP;
		int left = PVPLEFT;
		int right = (int)mWidthCurrWindow - PVPRIGHT;
		
		// draw the Y axis 
		g.drawLine(left, top, left, bottom);
		
		// draw the tics
		g.drawLine(left-2, bottom, left+2, bottom);
		g.drawLine(left-2, top, left+2, top);
		
		// complete the box 
		g.drawLine(right, top, right, bottom);
					
		// set the  Y precision 
		int numPlaces = NdEditFormulas.GetDisplayPrecision(yInc);
		
	    // draw the lower label
		String minValLbl = null;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LatitudeFormat latDM = new LatitudeFormat();
			minValLbl = latDM.format((float)winYPlotMin);
		}
		else
			minValLbl = NdEditFormulas.formatLat(winYPlotMin, numPlaces);
	    
	    // draw the upper label
		String maxValLbl = null;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LatitudeFormat latDM = new LatitudeFormat();
			maxValLbl = latDM.format((float)winYPlotMax);
		}
		else
			maxValLbl = NdEditFormulas.formatLat(winYPlotMax, numPlaces);

		Font font = new Font(Constants.DEFAULT_AXIS_VALUE_FONT, Constants.DEFAULT_AXIS_VALUE_STYLE, Constants.DEFAULT_AXIS_VALUE_SIZE);
		g.setFont(font);
	    FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int height = this.getSize().height;

		// plot minLabel
		int width = fm.stringWidth(minValLbl);  
		NdEditFormulas.drawStyledString(
					minValLbl, 
					25, 
					bottom, 
					0.0,
					0.0,
					g, 
					90, 
					Constants.DEFAULT_AXIS_VALUE_FONT, 
					Constants.DEFAULT_AXIS_VALUE_SIZE, 
					Constants.DEFAULT_AXIS_VALUE_STYLE, 
					Constants.DEFAULT_AXIS_VALUE_COLOR);
					
		width = fm.stringWidth(maxValLbl);  
		NdEditFormulas.drawStyledString(
					maxValLbl, 
					25, 
					PVPTOP + width, 
					0.0,
					0.0,
					g, 
					90, 
					Constants.DEFAULT_AXIS_VALUE_FONT, 
					Constants.DEFAULT_AXIS_VALUE_SIZE, 
					Constants.DEFAULT_AXIS_VALUE_STYLE, 
					Constants.DEFAULT_AXIS_VALUE_COLOR);
					
		// add variable label
		font = new Font(Constants.DEFAULT_AXIS_LABEL_FONT, Constants.DEFAULT_AXIS_LABEL_STYLE, Constants.DEFAULT_AXIS_LABEL_SIZE);
		g.setFont(font);
	    fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		width = fm.stringWidth(axisLabel); 
		NdEditFormulas.drawStyledString(
					axisLabel, 
					20, 
					height/2 - width/2, 
					0.0,
					0.0,
					g, 
					90, 
					Constants.DEFAULT_AXIS_LABEL_FONT, 
					Constants.DEFAULT_AXIS_LABEL_SIZE, 
					Constants.DEFAULT_AXIS_LABEL_STYLE, 
					Constants.DEFAULT_AXIS_LABEL_COLOR);
	}
	
	public void drawXAxis(Graphics2D g, double winXPlotMin, double winXPlotMax, 
					  boolean mReverseY, String axisLabel, int mHeightCurrWindow, 
					  int mWidthCurrWindow, int PVPBOTTOM, int PVPTOP, int PVPLEFT, int PVPRIGHT) {
		g.setColor(Color.black);
		int bottom = (int)mHeightCurrWindow - 1 * PVPBOTTOM;
		int top = PVPTOP;
		int left = PVPLEFT;
		int right = (int)mWidthCurrWindow - PVPRIGHT;
		
		// draw the X axis 
		g.drawLine(left, bottom, right, bottom);
		
		// draw the tics
		g.drawLine(left, bottom+2, left, bottom-2);
		g.drawLine(right, bottom+2, right, bottom-2);
		
		// complete the box ;
		g.drawLine(left, top, right, top);
					
		// set the X precision 
		int numPlaces = NdEditFormulas.GetDisplayPrecision(xInc);
		
	    // draw the left label
		String minValLbl = null;
		if (this.getTimeDisplayFormat() == Constants.DATE_TIME_DISPLAY)
			minValLbl = NdEditFormulas.formatLongDate((long)winXPlotMin);
		else
			minValLbl = NdEditFormulas.formatShortDate((long)winXPlotMin);
	    
	    // draw the upper label
		String maxValLbl = null;
		if (this.getTimeDisplayFormat() == Constants.DATE_TIME_DISPLAY)
			maxValLbl = NdEditFormulas.formatLongDate((long)winXPlotMax);
		else
			maxValLbl = NdEditFormulas.formatShortDate((long)winXPlotMax);

		Font font = new Font(Constants.DEFAULT_AXIS_VALUE_FONT, Constants.DEFAULT_AXIS_VALUE_STYLE, Constants.DEFAULT_AXIS_VALUE_SIZE);
		g.setFont(font);
	    FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int height = this.getSize().height;
		int width = fm.stringWidth(minValLbl); 
		 
		NdEditFormulas.drawStyledString(
					minValLbl, 
					mLeft, 
					bottom + 12, 
					0.0,
					0.0,
					g, 
					0, 
					Constants.DEFAULT_AXIS_VALUE_FONT, 
					Constants.DEFAULT_AXIS_VALUE_SIZE, 
					Constants.DEFAULT_AXIS_VALUE_STYLE, 
					Constants.DEFAULT_AXIS_VALUE_COLOR);
					
		width = fm.stringWidth(maxValLbl);  
		NdEditFormulas.drawStyledString(
					maxValLbl, 
					mWidthCurrWindow -  width - 25, 
					bottom + 12, 
					0.0,
					0.0,
					g, 
					0, 
					Constants.DEFAULT_AXIS_VALUE_FONT, 
					Constants.DEFAULT_AXIS_VALUE_SIZE, 
					Constants.DEFAULT_AXIS_VALUE_STYLE, 
					Constants.DEFAULT_AXIS_VALUE_COLOR);
		
		// add variable label
		font = new Font(Constants.DEFAULT_AXIS_LABEL_FONT, Constants.DEFAULT_AXIS_LABEL_STYLE, Constants.DEFAULT_AXIS_LABEL_SIZE);
		g.setFont(font);
	    fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		width = fm.stringWidth(axisLabel); 
		int hh = mWidthCurrWindow/2 - (width/2);// + PVPRIGHT;
		int vv = mHeightCurrWindow - 30;
		NdEditFormulas.drawStyledString(
					axisLabel, 
					hh, 
					vv, 
					0.0,
					0.0,
					g, 
					0, 
					Constants.DEFAULT_AXIS_LABEL_FONT, 
					Constants.DEFAULT_AXIS_LABEL_SIZE, 
					Constants.DEFAULT_AXIS_LABEL_STYLE, 
					Constants.DEFAULT_AXIS_LABEL_COLOR);
	}	

	public void setHandle(Object src, String prop, Object val, boolean sn) {		
		if (mXAxisLeft == null)
			return;
			
		double dval = 0.0f;
		long lval = 0;
		try {
			dval = (double)(((Double)val).floatValue());
			lval = (long)dval;
		}
		catch (ClassCastException ex) {
			lval = (long)(((Long)val).longValue());
			dval = (double)lval;
		}
		
		Graphics g = this.getGraphics();
		if (prop.indexOf("timmin") >= 0 && mXAxisLeft != src) {
			mXAxisLeft.resetStyle();
   			//mXAxisLeft.setOutOfRange(false);
			mXAxisLeft.setDontBroadcast(false);
			mXAxisLeft.setValue(dval);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
			int del = mXAxisCtr.getCurrLocation().x - mXAxisLeft.getCurrLocation().x;
			mXAxisLeft.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("timctr") >= 0 && mXAxisCtr != src) {
			mXAxisCtr.setDontBroadcast(true);
			mXAxisCtr.setValue(dval);
			mXAxisCtr.setXLocation(mXAxisCtr.getNewLocation());
			mXAxisCtr.setNeighbors();
		}
		else if (prop.indexOf("timmax") >= 0 && mXAxisRight != src) {
			mXAxisRight.resetStyle();
   			//mXAxisRight.setOutOfRange(false);
			mXAxisRight.setDontBroadcast(false);
			mXAxisRight.setValue(dval);
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
			int del = mXAxisRight.getCurrLocation().x - mXAxisCtr.getCurrLocation().x;
			mXAxisRight.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.equals("latmin") && mYAxisLower != src) {
			mYAxisLower.resetStyle();
   			//mYAxisLower.setOutOfRange(false);
			mYAxisLower.setDontBroadcast(false);
			mYAxisLower.setValue(dval);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
			int del = mYAxisLower.getCurrLocation().y - mYAxisCtr.getCurrLocation().y;
			mYAxisLower.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.equals("latctr") && mYAxisCtr != src) {
			mYAxisCtr.setDontBroadcast(true);
			mYAxisCtr.setValue(dval);			
			mYAxisCtr.setYLocation(mYAxisCtr.getNewLocation());
			mYAxisCtr.setNeighbors();
		}
		else if (prop.equals("latmax") && mYAxisUpper != src) {
			mYAxisUpper.resetStyle();
   			//mYAxisUpper.setOutOfRange(false);
			mYAxisUpper.setDontBroadcast(false);
			mYAxisUpper.setValue(dval);
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
			int del = mYAxisCtr.getCurrLocation().y - mYAxisUpper.getCurrLocation().y;
			mYAxisUpper.setNeighbors(del, this.isIndependentHandles());
		}
		if (mHilitedRegion != null) {
			mHilitedRegion.resetRgnBounds();
			this.repaint();
		}	
	}
		
	public void setHandleVal(String prop, Object val) {
		if (mXAxisLeft == null)
			return;
		double dval = 0.0f;
		long lval = 0;
		try {
			dval = (double)(((Double)val).floatValue());
		}
		catch (ClassCastException ex) {
			lval = ((Long)val).longValue();
			dval = (double)lval;
		}
		
		lval = (long)dval;
		
		if (prop.indexOf("timmin") >= 0) {
			mXAxisLeft.setAxisMinVal(lval);
			mXAxisCtr.setAxisMinVal(lval);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
		}
		else if (prop.indexOf("timmax") >= 0) {
			mXAxisRight.setAxisMaxVal(lval);
			mXAxisCtr.setAxisMaxVal(lval);
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
		}
		else if (prop.indexOf("latmin") >= 0) {
			mYAxisLower.setAxisMinVal(dval);
			mYAxisCtr.setAxisMinVal(dval);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
		}
		else if (prop.indexOf("latmax") >= 0) {
			mYAxisUpper.setAxisMaxVal(dval);
			mYAxisCtr.setAxisMaxVal(dval);
			mYAxisCtr.setYLocation(mYAxisCtr.getNewLocation());
		}
		if (mHilitedRegion != null) {
			mHilitedRegion.resetRgnBounds();
			this.repaint();
		}
	}
	
	public void setHandleVals(String prop, double min, double max) {
		if (mXAxisLeft == null)
			return;
		if (prop.indexOf("lat") >= 0) {
			// lat on y axis
			mYAxisLower.setAxisMinVal(min);
			mYAxisLower.setAxisMaxVal(max);
			mYAxisCtr.setAxisMinVal(min);
			mYAxisCtr.setAxisMaxVal(max);
			mYAxisLower.setAxisMinVal(min);
			mYAxisUpper.setAxisMaxVal(max);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
			mYAxisCtr.setYLocation(mYAxisUpper.getCurrLocation().y + (mYAxisLower.getCurrLocation().y - mYAxisUpper.getCurrLocation().y)/2);
		}
		else if (prop.indexOf("time") >= 0) {
			//time on x axis
			mXAxisLeft.setAxisMinVal(min);
			mXAxisLeft.setAxisMaxVal(max);
			mXAxisCtr.setAxisMinVal(min);
			mXAxisCtr.setAxisMaxVal(max);
			mXAxisRight.setAxisMinVal(min);
			mXAxisRight.setAxisMaxVal(max);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
			mXAxisCtr.setXLocation(mXAxisLeft.getCurrLocation().x + (mXAxisRight.getCurrLocation().x - mXAxisLeft.getCurrLocation().x)/2);
		}
		if (mHilitedRegion != null) {
			mHilitedRegion.resetRgnBounds();
			this.repaint();
		}
	}

	public void setFieldValues(String prop, Object val) {
		double dval = 0.0f;
		long lval = 0;
		try {
			dval = (double)(((Double)val).floatValue());
			lval = (long)dval;
		}
		catch (ClassCastException ex) {
			lval = (long)(((Long)val).longValue());
			dval = (double)lval;
		}
		
		if (prop.indexOf("latmin") >= 0) {
			startLat.setLat(dval);
		}
		else if (prop.indexOf("latmax") >= 0) {
			stopLat.setLat(dval);
		}
		else if (prop.indexOf("timmin") >= 0) {
			startDate.setDate(new Date(lval));
		}
		else if (prop.indexOf("timmax") >= 0) {
			stopDate.setDate(new Date(lval));
		}
	}

	public String toString() {
		return "LatTime View";	
	}
	
	public int ComputeSectionPixelWidth(Point p) {
		int x = p.x - mLeft - 5;
		int y = p.y - mTop - 5;
		double xReal = 0.0f;  //assume lon at prime merdian
		double yReal = (y/winYScale) + winYOrigin;  //lat 
	
		// keeping lon constant, iteratively find a matching great circle distance
		double tstReal = yReal;
		double d;
		while (true) {
			d = (double)NdEditFormulas.GreatCircle(yReal, xReal, tstReal, xReal);
			d /= 0.62;
			if (d >= Constants.SECTION_WIDTH) {
				break;
			}
			tstReal += 0.1;
		}
		
		double u = (xReal - winXOrigin) * winXScale;
		double v = (tstReal - winYOrigin) * winYScale;
					
		int pd = ((int)v - y)/2;
		pd = pd < 0 ? -pd : pd;
		return pd == 0 ? 1 : pd; 
	}
	
	public void setFieldsFromHandles() {
		//startLon.setLon(mYAxisLower.getValue());
		//stopLon.setLon(mYAxisUpper.getValue());
		//startDate.setDate(new Date((long)mXAxisLeft.getValue()));
		//stopDate.setDate(new Date((long)mXAxisRight.getValue()));
	}
	
	public void handleAxisRangeClick(String theAxis) {
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("StartBatch", null, null, false);
		if (theAxis.equals("Y")) {
			double[] oldYs = {getMinYVal(), getMaxYVal()};
			if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
				JLatSpinDialog latDialog = new JLatSpinDialog((float)oldYs[0], (float)oldYs[1]);
				double[] newYs = {latDialog.getLatitude(), latDialog.getLatitude2()};
				((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldYs, newYs, false);
			}
			else {
				JLatDialog latDialog = new JLatDialog((float)oldYs[0], (float)oldYs[1], "###.###N;###.###S");
				double[] newYs = {latDialog.getLatitude(), latDialog.getLatitude2()};
				((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldYs, newYs, false);
			}
		}
		else {
			SimpleDateFormat sdfGMT = null;
			if (this.getTimeDisplayFormat() == Constants.DATE_TIME_DISPLAY)
				sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
			else
				sdfGMT = new SimpleDateFormat("yyyy-DDD");
			sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
			double[] oldXs = {getMinXVal(), getMaxXVal()};
			if (this.getTimeDisplayFormat() == Constants.DATE_TIME_DISPLAY) {
      			JDateTimeGetter dateTimeGetter = new JDateTimeGetter(false);
			    dateTimeGetter.setOutputDateFormatter(sdfGMT);
			    dateTimeGetter.setTimeZone(TimeZone.getTimeZone("GMT"));
			    dateTimeGetter.setDate(new Date((long)oldXs[0]));
      			JDateTimeGetter dateTimeGetter2 = new JDateTimeGetter(false);
			    dateTimeGetter2.setOutputDateFormatter(sdfGMT);
			    dateTimeGetter2.setTimeZone(TimeZone.getTimeZone("GMT"));
			    dateTimeGetter2.setDate(new Date((long)oldXs[1]));
				ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.swing.PMELSwingResources");
		
				Object[] options = {"OK","Cancel"};
				
				JPanel jp1 = new JPanel();
		    	jp1.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kMinimum"));
				tb.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
				tb.setTitleColor(java.awt.Color.black);
		    	jp1.add(dateTimeGetter);
		    	jp1.setBorder(tb);
		    	
				JPanel jp2 = new JPanel();
		    	jp2.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    			tb = BorderFactory.createTitledBorder(b.getString("kMaximum"));
				tb.setTitleFont(new java.awt.Font("Dialog",java.awt.Font.PLAIN,12));
				tb.setTitleColor(java.awt.Color.black);
		    	jp2.add(dateTimeGetter2);
		    	jp2.setBorder(tb);
		    	
				JPanel jp = new JPanel();
		    	jp.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		    	jp.add(jp1);
		    	jp.add(jp2);
		
		        JOptionPane pane = new JOptionPane(jp, 
								JOptionPane.PLAIN_MESSAGE,
								JOptionPane.DEFAULT_OPTION, 
								null,
								options,
								options[0]);
				JDialog dialog = pane.createDialog(null, "Date Axis Range");
		        dialog.show();
		        Object selectedValue = pane.getValue();
		        if (options[0].equals(selectedValue)) {
					double[] newXs = {(double)(dateTimeGetter.getDate().getTime()), (double)(dateTimeGetter2.getDate().getTime())};
					((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomTimeDomain", oldXs, newXs, false);
		        }
			}
			else {
				JDateDialog dateDialog = new JDateDialog(new Date((long)oldXs[0]), new Date((long)oldXs[1]), sdfGMT);
				Date min = dateDialog.getDate();
				Date max = dateDialog.getDate2();
				double[] newXs = {(double)min.getTime(), (double)max.getTime()};
				((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomTimeDomain", oldXs, newXs, false);
			}
		}
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("EndBatch", null, null, false);
	}
	
	public void displayCursorValues(Point thePt) {
		if (mCurrPC == null || !axesInitialized)
			return;

		// convert the point to values
		double yAxisVal, xAxisVal;
		int x = thePt.x - mLeft - 5;
		int y = thePt.y - mTop - 5;
		xAxisVal = (x/winXScale) + winXOrigin;
		yAxisVal = (y/winYScale) + winYOrigin;
		((NdEdit)vm.getParent()).setLocation(Float.NaN, yAxisVal, Float.NaN, xAxisVal);
	}
		public void doZoomIn(Rectangle rbRect) {
		int x = rbRect.x - mLeft - 5;
		int y = rbRect.y - mTop - 5;
		
		// zoom in
		double xInc = getXZoomIncrement();
		double yInc = getYZoomIncrement();
		
		// get coordinates of new view center
		// correct the x value if necessary
		double xx = correctX((double)x);
			
		// correct the Y coordinate if necessary
		double yy = correctY((double)y);
		
		double newXCtr = (xx/winXScale) + winXOrigin;
		double newYCtr = (yy/winYScale) + winYOrigin;
		
		// compute the deltas for current range
		double xDelta = Math.abs(getMaxXVal() - getMinXVal())/2.0f;
		double yDelta = Math.abs(getMaxYVal() - getMinYVal())/2.0f;
		
		// compute the aspect ratio of the few
		double aspect = xDelta/yDelta;
		//if (aspect > 1.0)
		//	xInc *= aspect;
		//else
		//	yInc *= aspect;

		double newXMin = newXCtr < 0 ? newXCtr - xDelta + xInc : newXCtr - xDelta + xInc;
		double newXMax = newXCtr < 0 ? newXCtr + xDelta - xInc : newXCtr + xDelta - xInc;
		double newYMin = newYCtr < 0 ? newYCtr - yDelta + yInc : newYCtr - yDelta + yInc;
		double newYMax = newYCtr < 0 ? newYCtr + yDelta - yInc : newYCtr + yDelta - yInc;				
		
		double[] oldYs = {getMinYVal(), getMaxYVal()};
		double[] newYs = {newYMin, newYMax};  
		double[] oldXs = {getMinXVal(), getMaxXVal()}; 
		double[] newXs = {newXMin, newXMax};  
		this.zoomDomain(oldYs, newYs, oldXs, newXs);
	 }
}

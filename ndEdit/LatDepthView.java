/*
 * $Id: LatDepthView.java,v 1.19 2005/03/23 23:52:21 oz Exp $
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
import gov.noaa.pmel.text.*;
import gov.noaa.pmel.swing.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */

public class LatDepthView extends CutPanelView {
    private boolean TRACE = false;
    private ViewToggleButn tb;

    // --------------------------------------------------------------------------
    // 
    // constructor
    //
    public LatDepthView(Object parentObject) {
        super(LatDepthConstants.intValue, LatDepthConstants.viewName, parentObject);
		mViewManager = (ViewManager)parentObject;

	  	if (TRACE) 
	  		System.out.println("LatDepthView constructor entered");
	
		tb = new ViewToggleButn(LatDepthConstants.standardGif, LatDepthConstants.toolTipText);
		setToolbarButton(tb);
		try {
	    setImageIcon(Class.forName("ndEdit.NdEdit").getResource(LatDepthConstants.standardGif), "Lat: ","Depth: ");
    }
    catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    	System.out.println("LatDepthView:ctor");
    }
		this.setTextFieldTypes(Constants.DEPTH_FIELD, 0,0, Constants.LAT_FIELD, 0,0);
		this.setMsg(LatDepthConstants.toolTipText);
		rbRect = new RubberbandRectangle(this);
		rbRect.setActive(true);
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
		
		// compute a local scale and origin
		int scaleHeight = mHeight;
		scaleHeight -= (mBottom + mTop);
		double lwinYScale = scaleHeight/(this.getMaxYVal() - this.getMinYVal());
		double lwinYOrigin = this.getMinYVal();
	
		double lat = (x/winXScale) + winXOrigin;
		double lat2 = (x2/winXScale) + winXOrigin;
		double oldMinZ = getMinYVal();
		double oldMaxZ = getMaxYVal();
		double z = (y/lwinYScale) + lwinYOrigin;
		double z2 = (y2/lwinYScale) + lwinYOrigin;
		double oldMinLat = getMinXVal();
		double oldMaxLat = getMaxXVal();
		
		double [] oldzees = {getMaxYVal(), getMinYVal()};
		double [] newzees = {z2, z};  
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomDepthDomain", oldzees, newzees, false);
		
		double [] oldlats = {oldMinLat, oldMaxLat};
		double [] newlats = {lat, lat2};    
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldlats, newlats, false);
		
		//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomLatitudeStart", new Double(oldMinLat), new Double(lat), false);
    	//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomLatitudeStop", new Double(oldMaxLat), new Double(lat2), false);
		//Orig:((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomDepthStop", new Double(oldMaxZ), new Double(z), false);
    	//Orig:((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomDepthStart", new Double(oldMinZ), new Double(z2), false);
		//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomDepthStop", new Double(oldMaxZ), new Double(z2), false);
    	//((NdEdit)(mViewManager.getParent())).pushChangeableInfo("ZoomDepthStart", new Double(oldMinZ), new Double(z), false);
    	mViewManager.invalidateAllViews();
	}*/
	
	public void zoomDomain(double[] oldYs, double[] newYs, double[] oldXs, double[] newXs) {
		// swap the y values
		double temp = oldYs[0];
		oldYs[0] = oldYs[1];
		oldYs[1] = temp;
		temp = newYs[0];
		newYs[0] = newYs[1];
		newYs[1] = temp;
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("StartBatch", null, null, false);
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomDepthDomain", oldYs, newYs, false);
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldXs, newXs, false);
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("EndBatch", null, null, false);
	}
	
	public boolean isXAxisScaler() {
		return mCurrPC.getLatArr2() == null;
	}
	
	public boolean isYAxisScaler() {
		return mCurrPC.getDepthArr2() == null || mCurrPC.getDepthArr2().length == 0;
	}
	
	public double[] getXArray1() {
		return mCurrPC.getLatArr1();
	}
	
	public double[] getXArray2() {
		return mCurrPC.getLatArr2();
	}
	
	public double[] getYArray1() {
		return mCurrPC.getDepthArr1();
	}
	
	public double[] getYArray2() {
		return mCurrPC.getDepthArr2();
	}
	
	public double correctX(double x) {
		return x;
	}
	
	public double correctY(double y) {
		double yy = mHeight - y;
		yy -= mBottom;
		yy -= mTop;
		return yy;
	}
	
	public double getXZoomIncrement() {
		return (mCurrPC.getMaxLat() - mCurrPC.getMinLat())/10.0; 
	}
	
	public double getYZoomIncrement() {
		return (mCurrPC.getMaxDepth() - mCurrPC.getMinDepth())/10.0; 
	}
	
	public void handleAxisClick(String theAxis, Point thePt) {
		// convert the point to a value on the axis
		double axisVal;
		int x = thePt.x - mLeft - 5;
		int y = getMaxY() - thePt.y;
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
				setHandle(null, "latctr", new Double(axisVal), true);
				setHandle(null, "latmin", new Double(axisVal - diff1), true);
				setHandle(null, "latmax", new Double(axisVal + diff2), true);
				mXAxisRight.broadcast(true);
				mXAxisLeft.broadcast(true);
				mXAxisCtr.broadcast(true);
			}
			else {
				// current values of the selection
				double leftVal = (double)mXAxisLeft.getValue();
				double rightVal = (double)mXAxisRight.getValue();
				double ctrVal =(double) mXAxisCtr.getValue();
				
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
						setHandle(null, "latmin", new Double(axisVal), true);
						mXAxisLeft.broadcast(true);
					}
					else if (diff2 < diff1 && diff2 < diff3) {
						setHandle(null, "latctr", new Double(axisVal), true);
						mXAxisCtr.broadcast(true);
					}
					else if (diff3 < diff1 && diff3 < diff2) {
						setHandle(null, "latmax", new Double(axisVal), true);
						mXAxisRight.broadcast(true);
					}
				}
				else {
					if (rightOOR) {
						// move the center handle to mouseclick, left handle stays stationary
						setHandle(null, "latctr", new Double(axisVal), true);
						setHandle(null, "latmax", new Double(axisVal + (axisVal - leftVal)), true);
						mXAxisRight.broadcast(true);
					}
					else if (leftOOR) {
						// move the center handle to mouseclick, right handle stays stationary
						setHandle(null, "latctr", new Double(axisVal), true);
						setHandle(null, "latmin", new Double(axisVal - (rightVal - axisVal)), true);
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
				setHandle(null, "zctr", new Double(axisVal), true);
				setHandle(null, "zmin", new Double(axisVal - diff1), true);
				setHandle(null, "zmax", new Double(axisVal + diff2), true);
				mYAxisUpper.broadcast(true);
				mYAxisLower.broadcast(true);
				mYAxisCtr.broadcast(true);
			}
			else {
				double upperVal = (double)mYAxisUpper.getValue();
				double lowerVal = (double)mYAxisLower.getValue();
				double ctrVal = (double)mYAxisCtr.getValue();
				
				if (topOOR)
					upperVal = getMinYVal();
				if (bottOOR)
					lowerVal = getMaxYVal();
				if (yCtrOOR)
					ctrVal = lowerVal + ((upperVal - lowerVal)/2.0f);
					
				if (!yCtrOOR) {	
					diff1 = Math.abs(upperVal - axisVal);
					diff2 = Math.abs(ctrVal - axisVal);
					diff3 = Math.abs(lowerVal - axisVal);
					if (diff1 < diff2 && diff1 < diff3) {
						setHandle(null, "zmin", new Double(axisVal), true);
						mYAxisUpper.broadcast(true);
					}
					else if (diff2 < diff1 && diff2 < diff3) {
						setHandle(null, "zctr", new Double(axisVal), true);
						mYAxisCtr.broadcast(true);
					}
					else if (diff3 < diff1 && diff3 < diff2) {
						setHandle(null, "zmax", new Double(axisVal), true);
						mYAxisLower.broadcast(true);
					}
				}
				else {
					if (topOOR) {
						// move the center handle to mouseclick, bottom handle stays stationary
						setHandle(null, "zctr", new Double(axisVal), true);
						setHandle(null, "zmin", new Double(axisVal + (axisVal - lowerVal)), true);
						mYAxisUpper.broadcast(true);
					}
					else if (bottOOR) {
						// move the center handle to mouseclick, top handle stays stationary
						setHandle(null, "zctr", new Double(axisVal), true);
						setHandle(null, "zmax", new Double(axisVal - (upperVal - axisVal)), true);
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
		mTop = top;
		mLeft = left;
		mRight = right;
		mBottom = bott;
		mWidth = width;
		mHeight = height;
		double minLat;
		double maxLat;
		double minZ;
		double maxZ;
		double selMinZ = 0.0f;
		double selMaxZ = 0.0f;
		double selMinLat = 0.0f;
		double selMaxLat = 0.0f;
		
		// initialize the axes ranges
		if (mCurrPC == null) 
			return;
			
		firstPlotDone = true;
		
		if (!axesInitialized) {
			// get a range from the view manager if these ranges have been set by another view
			Float latMin = vm.getAxisMin(Constants.LAT_AXIS, Constants.LAT_DEPTH);
			Float latMax = vm.getAxisMax(Constants.LAT_AXIS, Constants.LAT_DEPTH);
			Float zMin = vm.getAxisMin(Constants.DEPTH_AXIS, Constants.LAT_DEPTH);
			Float zMax = vm.getAxisMax(Constants.DEPTH_AXIS, Constants.LAT_DEPTH);
			Float selLatMin = vm.getAxisSelMin(Constants.LAT_AXIS, Constants.LAT_DEPTH);
			Float selLatMax = vm.getAxisSelMax(Constants.LAT_AXIS, Constants.LAT_DEPTH);
			Float selZMin = vm.getAxisSelMin(Constants.DEPTH_AXIS, Constants.LAT_DEPTH);
			Float selZMax = vm.getAxisSelMax(Constants.DEPTH_AXIS, Constants.LAT_DEPTH);
			
			if ((latMin != null && latMax != null && (latMin.floatValue() != 0.0 && latMax.floatValue() != 0.0)) &&
			    (selLatMin != null && selLatMax != null && (selLatMin.floatValue() != 0.0 && selLatMax.floatValue() != 0.0))) {
				setMinXVal(latMin.floatValue());
				setMaxXVal(latMax.floatValue());
				selMinLat = selLatMin.floatValue();
				selMaxLat = selLatMax.floatValue();
			}
			else {
				if (mCurrPC.getMaxLat() != mCurrPC.getMinLat()) {
					// compute a nice range
					double range = Math.abs(mCurrPC.getMaxLat() - mCurrPC.getMinLat());
					setMinXVal(NdEditFormulas.getNiceLowerValue(mCurrPC.getMinLat(), range, -90.0));
					setMaxXVal(NdEditFormulas.getNiceUpperValue(mCurrPC.getMaxLat(), range, 90.0));
					selMinLat = getMinXVal();
					selMaxLat = getMaxXVal();
				}
				else {
					// point observations
					setMinXVal(mCurrPC.getMinLat() - 1.0);
					setMaxXVal(mCurrPC.getMaxLat() + 1.0);
					selMinLat = getMinXVal();
					selMaxLat = getMaxXVal();
				}
			}
			
			if ((zMin != null && zMax != null && (zMin.floatValue() != 0.0 && zMax.floatValue() != 0.0)) &&
			    (selZMin != null && selZMax != null && (selZMin.floatValue() != 0.0 && selZMax.floatValue() != 0.0))) {
				setMinYVal(zMin.floatValue());
				setMaxYVal(zMax.floatValue());
				selMaxZ = selZMin.floatValue();
				selMinZ = selZMax.floatValue();
			}
			else {
				if (mCurrPC.getMaxDepth() != mCurrPC.getMinDepth()) {
					// compute a nice range
					double range = Math.abs(mCurrPC.getMaxDepth() - mCurrPC.getMinDepth());
					setMinYVal(NdEditFormulas.getNiceLowerValue(mCurrPC.getMinDepth(), range));
					setMaxYVal(NdEditFormulas.getNiceUpperValue(mCurrPC.getMaxDepth(), range));
					selMinZ = getMinYVal();
					selMaxZ = getMaxYVal();
				}
				else {
					// point observations
					setMinYVal(mCurrPC.getMinDepth() - 1.0);
					setMaxYVal(mCurrPC.getMaxDepth() + 1.0);
					selMinZ = getMinYVal();
					selMaxZ = getMaxYVal();
				}
			}
			axesInitialized = true;
		}
		
		// scale the axes
		minZ = getMinYVal();
		maxZ = getMaxYVal();
		minLat = getMinXVal();
		maxLat = getMaxXVal();
		
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
				
			double lat = mCurrPC.getLat1(i);
			double z = mCurrPC.getZ1(i);
			double x = (lat - winXOrigin) * winXScale;
			double y = (z - winYOrigin) * winYScale;
			//int ix = (int)x;
			//int iy = (int)y;
			
			/*try {
				if ((ix < 0 || ix >= maxx || 
					iy < 0 || iy >= maxy || t[ix][iy]) && !mCurrPC.isSelected(i))
					continue;
				
				t[ix][iy] = true;
			}
			catch (Exception ex) {}*/
				
			boolean xIsScaler = mCurrPC.isLatScaler(i);
			boolean yIsScaler = mCurrPC.isDepthScaler(i);		
			
			// adjust for axes labels
			y = height - y;
			x += mLeft;
			y -= mBottom;
			
			double x2 = Float.NaN;
			double y2 = Float.NaN;
			double lat2 = Float.NaN;
			double z2 = Float.NaN;
							
			if (!yIsScaler) {
				z2 = mCurrPC.getZ2(i);
				y2 = (z2 - winYOrigin) * winYScale;
				y2 = height - y2;
				y2 -= mBottom;
			}
				
			if (!xIsScaler) {
				lat2 = mCurrPC.getLat2(i);
				x2 = (lat2 - winXOrigin) * winXScale;
				x2 += mLeft;
			}
			int swidth = 4;
			if (xIsScaler && yIsScaler) {
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D)g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D)g).setStroke(bs1);
				}
				// point observation
				g.drawLine((int)(x-swidth/2), (int)y, (int)(x+swidth/2), (int)y);
				g.drawLine((int)x, (int)(y-swidth/2), (int)x, (int)(y+swidth/2));
			}
			if (!xIsScaler && yIsScaler) {
				// lat range
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D)g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D)g).setStroke(bs1);
				}
				g.drawLine((int)x, (int)(y-swidth/2), (int)x, (int)(y+swidth/2));
				g.drawLine((int)x, (int)y, (int)x2, (int)y);
				g.drawLine((int)x2, (int)(y-swidth/2), (int)x2, (int)(y+swidth/2));
			}
			else if (xIsScaler && !yIsScaler) {
				// depth range
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D)g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D)g).setStroke(bs1);
				}
				g.drawLine((int)(x-swidth/2), (int)y, (int)(x+swidth/2), (int)y);
				g.drawLine((int)x, (int)y, (int)x, (int)y2);
				g.drawLine((int)(x-swidth/2), (int)y2, (int)(x+swidth/2), (int)y2);
			}
			else if (!xIsScaler && !yIsScaler) {
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D)g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D)g).setStroke(bs1);
				}
				// range in both axes--draw a rectangle
				
				// symbol at top
				g.drawLine((int)(x2-swidth/2), (int)y, (int)(x2+swidth/2), (int)y);
				g.drawLine((int)x2, (int)(y-swidth/2), (int)x2, (int)(y+swidth/2));
				
				// symbol at bottom
				g.drawLine((int)(x2-swidth/2), (int)y2, (int)(x2+swidth/2), (int)y2);
				g.drawLine((int)x2, (int)(y2-swidth/2), (int)x2, (int)(y2+swidth/2));
				
				g.drawLine((int)x2, (int)y, (int)x2, (int)y2);
			}
		}

		((Graphics2D)g).setStroke(bs1);
		g.setClip(0, 0, 1000, 1000);
   		SelectionRegion.selOffset = 0;
		
		//construct the draghandles
   		if (mXAxisLeft == null) {
   			int initH = mLeft + 5;
   			int initV = mTop + 5;
   			double initVal = selMinLat;
			mXAxisLeft = new LeftDragHandle(this, initH, initV, DragHandle.LEFTHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.HORIENTATION, Constants.LAT_AXIS, mLeft+5, mLeft+scaleWidth+5, minLat, maxLat, initVal);
			startLat.setValue(initVal);
   		}
   		else {
   			// set the current location of the handle
   			mXAxisLeft.setAxisRange(mLeft+5, mLeft+scaleWidth+5);
   		}
   		
   		if (mXAxisCtr == null) {
   			int initH = mLeft + 5 + (int)(0.50 * scaleWidth);
   			int initV = mTop + 5;
			double yDiff = (selMaxLat - selMinLat);
   			double initVal = selMinLat + (0.50f * yDiff);
			mXAxisCtr = new CenterHDragHandle(this, initH, initV, DragHandle.CENTERHHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.HORIENTATION, Constants.LAT_AXIS, mTop+5, mTop+scaleHeight+5, minLat, maxLat, initVal);
   		}
   		else {
   			// set the current location of the handle
   			mXAxisCtr.setAxisRange(mLeft+5, mLeft+scaleWidth+5);
   		}
   		
   		if (mXAxisRight == null) {
   			int initH = mLeft + 5 + scaleWidth;
   			int initV = mTop + 5;
   			double initVal = selMaxLat;
			mXAxisRight = new RightDragHandle(this, initH, initV, DragHandle.RIGHTHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.HORIENTATION, Constants.LAT_AXIS, mLeft+5, mLeft+scaleWidth+5, minLat, maxLat, initVal);
			stopLat.setValue(initVal);
   		}
   		else{
   			mXAxisRight.setAxisRange(mLeft+5, mLeft+scaleWidth+5);
   		}
   		
   		if (mYAxisUpper == null) {
   			int initV = mTop + 5 + (int)(0.0 * scaleHeight);
   			int initH = mLeft + scaleWidth + 5;
   			double initVal = selMinZ;
			mYAxisUpper = new UpperDragHandle(this, initH, initV, DragHandle.UPPERHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.VORIENTATION, Constants.DEPTH_AXIS, mTop+5, mTop+scaleHeight+5, maxZ, minZ, initVal);
			startDepth.setValue(selMinZ);
   		}
   		else {
   			// set the current location of the handle
   			mYAxisUpper.setMaxH(mLeft + scaleWidth + 5);
   			mYAxisUpper.setAxisRange(mTop+5, mTop+scaleHeight+5);
   		}
   		
   		if (mYAxisCtr == null) {
   			int initV = mTop + 5 + (int)(0.50 * scaleHeight);
   			int initH = mLeft + scaleWidth + 5;
			double yDiff = (selMaxZ - selMinZ);
   			double initVal = selMaxZ - (0.50f * yDiff);
			mYAxisCtr = new CenterVDragHandle(this, initH, initV, DragHandle.CENTERVHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.VORIENTATION, Constants.DEPTH_AXIS, mTop+5, mTop+scaleHeight+5, maxZ, minZ, initVal);
   		}
   		else {
   			// set the current location of the handle
   			mYAxisCtr.setMaxH(mLeft + scaleWidth + 5);
   			mYAxisCtr.setAxisRange(mTop+5, mTop+scaleHeight+5);
   		}
   		
   		if (mYAxisLower == null) {
   			int initV = mTop + 5 + (int)(1.0 * scaleHeight);
   			int initH = mLeft + scaleWidth + 5;
   			double initVal = selMaxZ;
			mYAxisLower = new LowerDragHandle(this, initH, initV, DragHandle.LOWERHANDLE, DragHandle.RECTSTYLE, 
					DragHandle.VORIENTATION, Constants.DEPTH_AXIS, mTop+5, mTop+scaleHeight+5, maxZ, minZ, initVal);
			stopDepth.setValue(selMaxZ);
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
			stopDepth.fireDepthChange();
			startDepth.fireDepthChange();
			setHandle(this, "latmin", new Double(selMinLat), true);
			//setHandle(this, "latmax", new Double(selMaxLat), true);
			
			// hack-o-rama because the above doesn't work for some reason
			mXAxisRight.resetStyle();
   			//mXAxisRight.setOutOfRange(false);
			mXAxisRight.setDontBroadcast(true);
			mXAxisRight.setValue(selMaxLat);
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
			mXAxisRight.setNeighbors(this.isIndependentHandles());
			this.repaint();
			mXAxisRight.setDontBroadcast(false);
			
			setHandle(this, "zmin", new Double(selMinZ), true);
			setHandle(this, "zmax", new Double(selMaxZ), true);

   		}
   		else {
   			// set the current selection region
   			SelectionRegion.selOffset = 0;
   			mHilitedRegion.resetRgnBounds();
   		}

		// draw the axes
		if (plotAxes) {
			drawXAxis(g, minLat, maxLat, false, "Latitude", height, width, mBottom, mTop, mLeft, mRight);
			drawYAxis(g, minZ, maxZ, true, "Depth (M)", height, width, mBottom, mTop, mLeft, mRight);
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
		//Orig:String maxValLbl = NdEditFormulas.formatDouble(String.valueOf(winYPlotMax), numPlaces, false);
		String maxValLbl = NdEditFormulas.formatDouble(String.valueOf(winYPlotMax), 1, false);
	    
	    // draw the upper label
		//Orig:String minValLbl = NdEditFormulas.formatDouble(String.valueOf(winYPlotMin), numPlaces, false);
		String minValLbl = NdEditFormulas.formatDouble(String.valueOf(winYPlotMin), 1, false);
			
		Font font = new Font(Constants.DEFAULT_AXIS_VALUE_FONT, Constants.DEFAULT_AXIS_VALUE_STYLE, Constants.DEFAULT_AXIS_VALUE_SIZE);
		g.setFont(font);
	    FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int height = this.getSize().height;

		// plot minLabel
		int width = fm.stringWidth(maxValLbl);  
		NdEditFormulas.drawStyledString(
					maxValLbl, 
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
					
		width = fm.stringWidth(minValLbl);  
		NdEditFormulas.drawStyledString(
					minValLbl, 
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
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LatitudeFormat latDM = new LatitudeFormat();
			minValLbl = latDM.format((float)winXPlotMin);
		}
		else
			minValLbl = NdEditFormulas.formatLat(winXPlotMin, numPlaces);
	    
	    // draw the upper label
		String maxValLbl = null;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LatitudeFormat latDM = new LatitudeFormat();
			maxValLbl = latDM.format((float)winXPlotMax);
		}
		else
			maxValLbl = NdEditFormulas.formatLat(winXPlotMax, numPlaces);

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
		double dval = 0.0f;
		long lval = 0;
		try {
			dval = (double)(((Double)val).doubleValue());
		}
		catch (ClassCastException ex) {
			lval = (long)(((Long)val).longValue());
			dval = (double)lval;
		}
		
		if (mXAxisLeft == null)
			return;
		
		Graphics g = this.getGraphics();
		if (prop.indexOf("latmin") >= 0 && mXAxisLeft != src) {
			mXAxisLeft.resetStyle();
   			//mXAxisLeft.setOutOfRange(false);
			mXAxisLeft.setDontBroadcast(false);
			mXAxisLeft.setValue(dval);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
			int del = mXAxisCtr.getCurrLocation().x - mXAxisLeft.getCurrLocation().x;
			mXAxisLeft.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("latctr") >= 0 && mXAxisCtr != src) {
			mXAxisCtr.setDontBroadcast(true);
			mXAxisCtr.setValue(dval);
			mXAxisCtr.setXLocation(mXAxisCtr.getNewLocation());
			mXAxisCtr.setNeighbors();
		}
		else if (mXAxisRight != null && prop.indexOf("latmax") >= 0 && mXAxisRight != src) {
			mXAxisRight.resetStyle();
   			//mXAxisRight.setOutOfRange(false);
			mXAxisRight.setDontBroadcast(false);
			mXAxisRight.setValue(dval);
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
			int del = mXAxisRight.getCurrLocation().x - mXAxisCtr.getCurrLocation().x;
			mXAxisRight.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("zmin") >= 0 && mYAxisUpper != src) {
			mYAxisUpper.resetStyle();
   			//mYAxisUpper.setOutOfRange(false);
			mYAxisUpper.setDontBroadcast(false);
			mYAxisUpper.setValue(dval);
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
			int del = mYAxisCtr.getCurrLocation().y - mYAxisUpper.getCurrLocation().y;
			mYAxisUpper.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("zctr") >= 0 && mYAxisCtr != src) {
			mYAxisCtr.setDontBroadcast(true);
			mYAxisCtr.setValue(dval);
			mYAxisCtr.setYLocation(mYAxisCtr.getNewLocation());
			mYAxisCtr.setNeighbors();
		}
		else if (prop.indexOf("zmax") >= 0 && mYAxisLower != src) {
			mYAxisLower.resetStyle();
   			//mYAxisLower.setOutOfRange(false);
			mYAxisLower.setDontBroadcast(false);
			mYAxisLower.setValue(dval);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
			int del = mYAxisLower.getCurrLocation().y - mYAxisCtr.getCurrLocation().y;
			mYAxisLower.setNeighbors(del, this.isIndependentHandles());
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
				
		if (prop.indexOf("latmin") >= 0) {
			mXAxisLeft.setAxisMinVal(dval);
			mXAxisCtr.setAxisMinVal(dval);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
		}
		else if (prop.indexOf("latmax") >= 0) {
			mXAxisRight.setAxisMaxVal(dval);
			mXAxisCtr.setAxisMaxVal(dval);
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
		}
		else if (prop.indexOf("zmax") >= 0) {
			mYAxisLower.setAxisMinVal(dval);
			mYAxisCtr.setAxisMinVal(dval);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
		}
		else if (prop.indexOf("zmin") >= 0) {
			mYAxisUpper.setAxisMaxVal(dval);
			mYAxisCtr.setAxisMinVal(dval);
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
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
		
		// set the text field values
		if (prop.indexOf("zmax") >= 0) {
			stopDepth.setDepth(dval);
		}
		else if (prop.indexOf("zmin") >= 0) {
			startDepth.setDepth(dval);
		}
		else if (prop.indexOf("latmin") >= 0) {
			startLat.setLat(dval);
		}
		else if (prop.indexOf("latmax") >= 0) {
			stopLat.setLat(dval);
		}
		if (mHilitedRegion != null) {
			mHilitedRegion.resetRgnBounds();
			this.repaint();
		}
	}
	
	public String toString() {
		return "LatDepth View";	
	}
	
	public void setHandleVals(String prop, double min, double max) {
		if (mXAxisLeft == null)
			return;
		Graphics g = this.getGraphics();
				
		if (prop.indexOf("lat") >= 0) {
			// lat on the x axis
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
		else if (prop.indexOf("depth") >= 0) {
			//depth on the y axis
//System.out.println("curr value of lower = " + mYAxisLower.getValue());
//System.out.println("curr value of upper = " + mYAxisUpper.getValue());
			mYAxisLower.setAxisMinVal(max);
			mYAxisLower.setAxisMaxVal(min);
			mYAxisCtr.setAxisMinVal(max);
			mYAxisCtr.setAxisMaxVal(min);
			mYAxisUpper.setAxisMinVal(max);
			mYAxisUpper.setAxisMaxVal(min);
//System.out.println("new value of lower = " + mYAxisLower.getValue());
//System.out.println("new value of upper = " + mYAxisUpper.getValue());
//System.out.println("curr loc of lower = " + mYAxisLower.getCurrLocation().y);
//System.out.println("curr loc of upper = " + mYAxisUpper.getCurrLocation().y);
//System.out.println("new loc of lower = " + mYAxisLower.getNewLocation());
//System.out.println("new loc of upper = " + mYAxisUpper.getNewLocation());
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
			mYAxisCtr.setYLocation(mYAxisUpper.getCurrLocation().y + (mYAxisLower.getCurrLocation().y - mYAxisUpper.getCurrLocation().y)/2);
		}
	}
	
	public int ComputeSectionPixelWidth(Point p) {
		int x = p.x - mLeft - 5;
		int y = p.y - mTop - 5;
		double xReal = (x/winXScale) + winXOrigin; // latitude
		double yReal = 0.0f; //assume the prime meridian
	
		// keeping lon constant, iteratively find a matching great circle distance
		double tstReal = xReal;
		double d;
		while (true) {
			d = NdEditFormulas.GreatCircle(xReal, yReal, tstReal, yReal);
			d /= 0.62;
			if (d >= Constants.SECTION_WIDTH) {
				break;
			}
			tstReal += 0.1;
		}
		
		double u = (tstReal - winXOrigin) * winXScale;
		double v = (yReal - winYOrigin) * winYScale;
					
		int pd = ((int)u - x)/2;
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
		if (theAxis.equals("X")) {
			double[] oldXs = {getMinXVal(), getMaxXVal()};
			if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
				JLatSpinDialog latDialog = new JLatSpinDialog((float)oldXs[0], (float)oldXs[1]);
				double[] newXs = {latDialog.getLatitude(), latDialog.getLatitude2()};
				((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldXs, newXs, false);
			}
			else {
				JLatDialog latDialog = new JLatDialog((float)oldXs[0], (float)oldXs[1], "###.###;###.###");
				double[] newXs = {latDialog.getLatitude(), latDialog.getLatitude2()};
				((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldXs, newXs, false);
			}
		}
		else {
			double[] oldYs = {getMinYVal(), getMaxYVal()};
			JDepthDialog zDialog = new JDepthDialog((float)oldYs[0], (float)oldYs[1], "###.###;###.###");
			double[] newYs = {zDialog.getDepth2(), zDialog.getDepth()};
			((NdEdit)mViewManager.getParent()).pushChangeableInfo("ZoomDepthDomain", oldYs, newYs, false);
		}
		((NdEdit)mViewManager.getParent()).pushChangeableInfo("EndBatch", null, null, false);
	}
	
	public void displayCursorValues(Point thePt) {
		if (mCurrPC == null || !axesInitialized)
			return;

		// convert the point to values
		double yAxisVal, xAxisVal;
		int x = thePt.x - mLeft - 5;
		int y = getMaxY() - thePt.y;
		xAxisVal = (x/winXScale) + winXOrigin;
		yAxisVal = (y/winYScale) + winYOrigin;
		((NdEdit)vm.getParent()).setLocation(Float.NaN, xAxisVal, yAxisVal, Float.NaN);
	
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
		
		if (newYMax < newYMin) {
			double temp = newYMin;
			newYMin = newYMax;
			newYMax = temp;
		}				
		
		double[] oldYs = {getMinYVal(), getMaxYVal()};
		double[] newYs = {newYMin, newYMax};  
		double[] oldXs = {getMinXVal(), getMaxXVal()}; 
		double[] newXs = {newXMin, newXMax};  
		this.zoomDomain(oldYs, newYs, oldXs, newXs);
	 }
}

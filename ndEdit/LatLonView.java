/*
 * $Id: LatLonView.java,v 1.41 2005/08/22 21:25:15 oz Exp $
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

import java.awt.event.*;
import java.awt.*;
import gov.noaa.pmel.text.*;
import gov.noaa.pmel.swing.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.geom.*;

/**
 * 
 * 
 * @author Chris Windsor
 * @version 1.0 01/13/00
 */

public class LatLonView extends CutPanelView {
	private ViewToggleButn tb;
	private boolean TRACE = false;
	private boolean crossed180 = false;
	double selMinLat = 0.0f;
	double selMaxLat = 0.0f;
	double selMinLon = 0.0f;
	double selMaxLon = 0.0f;
	double[] coastlats = new double[130000];
	double[] coastlons = new double[130000];
	double[] templats = new double[130000];
	double[] templons = new double[130000];
	double[] latVal = null;
	double[] lonVal = null;
	private boolean mPlotBathy = false;
	protected Hashtable mIsobathCache = new Hashtable();
	private String custCoastPath = null;
	private Color mCoastColor = Color.black;

	// constructor
	public LatLonView(Object parentObject) {
		super(LatLonConstants.intValue, LatLonConstants.viewName, parentObject);
		mViewManager = (ViewManager) parentObject; 		

		if (TRACE)
			System.out.println("LatLonView constructor entered");

		tb = new ViewToggleButn(LatLonConstants.standardGif, LatLonConstants.toolTipText);
		this.setToolbarButton(tb);
		try {
			this.setImageIcon(Class.forName("ndEdit.NdEdit").getResource(LatLonConstants.standardGif), "Lat: ", "Lon: ");
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("LatLonView:ctor");
		}
		this.setTextFieldTypes(Constants.LAT_FIELD, 0, 0, Constants.LON_FIELD, 0, 0);
		this.setMsg(LatLonConstants.toolTipText);
		rbRect = new SectionSpline(this);
		rbRect.setActive(true);
		mOverlaySpec = new BathyOverlaySpec();
	}

	public void zoomDomain(double[] oldYs, double[] newYs, double[] oldXs, double[] newXs) {
		((NdEdit) mViewManager.getParent()).pushChangeableInfo("StartBatch", null, null, false);
		((NdEdit) mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldYs, newYs, false);
		((NdEdit) mViewManager.getParent()).pushChangeableInfo("ZoomLongitudeDomain", oldXs, newXs, false);
		((NdEdit) mViewManager.getParent()).pushChangeableInfo("EndBatch", null, null, false);
	}

	public boolean isXAxisScaler() {
		return mCurrPC.getLonArr2() == null;
	}

	public boolean isYAxisScaler() {
		return mCurrPC.getLatArr2() == null;
	}

	public double[] getXArray1() {
		return mCurrPC.getLonArr1();
	}

	public double[] getXArray2() {
		return mCurrPC.getLonArr2();
	}

	public double[] getYArray1() {
		return mCurrPC.getLatArr1();
	}

	public double[] getYArray2() {
		return mCurrPC.getLatArr2();
	}

	public double correctX(double lon) {
		if (crossed180 && lon < 0) {
			lon += 360;
		}
		return lon;
	}

	public double correctY(double y) {
		return y;
	}

	public double getXZoomIncrement() {
		return Math.abs((this.getMaxXVal() - this.getMinXVal()) / 10.0);
	}

	public double getYZoomIncrement() {
		return Math.abs((this.getMaxYVal() - this.getMinYVal()) / 10.0);
	}

	public void handleAxisClick(String theAxis, Point thePt) {
		// convert the point to a new value on the axis
		double axisVal;
		int x = thePt.x - mLeft - 5;
		int y = thePt.y - mTop - 5;
		if (theAxis.equals("X"))
			axisVal = (x / winXScale) + winXOrigin;
		else {
			axisVal = (y / winYScale) + winYOrigin;
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
				// whole selection is out of range--translate the selection to this new
				// center
				double leftVal = (double) mXAxisLeft.getValue();
				double rightVal = (double) mXAxisRight.getValue();
				double ctrVal = (double) mXAxisCtr.getValue();
				diff1 = Math.abs(ctrVal - leftVal);
				diff2 = Math.abs(rightVal - ctrVal);
				setHandle(null, "lonctr", new Double(axisVal), true);
				setHandle(null, "lonmin", new Double(axisVal - diff1), true);
				setHandle(null, "lonmax", new Double(axisVal + diff2), true);
				mXAxisRight.broadcast(true);
				mXAxisLeft.broadcast(true);
				mXAxisCtr.broadcast(true);
			}
			else {
				// current values of the selection
				double leftVal = (double) mXAxisLeft.getValue();
				double rightVal = (double) mXAxisRight.getValue();
				double ctrVal = (double) mXAxisCtr.getValue();

				if (leftOOR)
					leftVal = getMinXVal();
				if (rightOOR)
					rightVal = getMaxXVal();
				if (xCtrOOR)
					ctrVal = leftVal + ((rightVal - leftVal) / 2.0f);

				if (!xCtrOOR) {
					diff1 = Math.abs(leftVal - axisVal);
					diff2 = Math.abs(ctrVal - axisVal);
					diff3 = Math.abs(rightVal - axisVal);
					if (diff1 < diff2 && diff1 < diff3) {
						setHandle(null, "lonmin", new Double(axisVal), true);
						mXAxisLeft.broadcast(true);
					}
					else if (diff2 < diff1 && diff2 < diff3) {
						setHandle(null, "lonctr", new Double(axisVal), true);
						mXAxisCtr.broadcast(true);
					}
					else if (diff3 < diff1 && diff3 < diff2) {
						setHandle(null, "lonmax", new Double(axisVal), true);
						mXAxisRight.broadcast(true);
					}
				}
				else {
					if (rightOOR) {
						// move the center handle to mouseclick, left handle stays
						// stationary
						setHandle(null, "lonctr", new Double(axisVal), true);
						setHandle(null, "lonmax", new Double(axisVal + (axisVal - leftVal)), true);
						mXAxisRight.broadcast(true);
					}
					else if (leftOOR) {
						// move the center handle to mouseclick, right handle stays
						// stationary
						setHandle(null, "lonctr", new Double(axisVal), true);
						setHandle(null, "lonmin", new Double(axisVal - (rightVal - axisVal)), true);
						mXAxisLeft.broadcast(true);
					}
				}
			}
		}
		else {
			if (topOOR && bottOOR && yCtrOOR) {
				// whole selection is out of range--translate the selection to this new
				// center
				double topVal = (double) mYAxisUpper.getValue();
				double bottVal = (double) mYAxisLower.getValue();
				double ctrVal = (double) mYAxisCtr.getValue();
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
				double upperVal = (double) mYAxisUpper.getValue();
				double lowerVal = (double) mYAxisLower.getValue();
				double ctrVal = (double) mYAxisCtr.getValue();

				if (topOOR)
					upperVal = getMaxYVal();
				if (bottOOR)
					lowerVal = getMinYVal();
				if (yCtrOOR)
					ctrVal = lowerVal + ((upperVal - lowerVal) / 2.0f);

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
						// move the center handle to mouseclick, bottom handle stays
						// stationary
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

	public boolean LLKeep(double lat, double lon) {
		double mLatMin = this.getMinYVal();
		double mLatMax = this.getMaxYVal();
		double mLonLft = this.getMinXVal();
		double mLonRt = this.getMaxXVal();
		if ((lat < mLatMin) || (lat > mLatMax))
			return false;
		if (lon > 180)
			lon = lon - 360;
		else if (lon < -180)
			lon = lon + 360;
		if (mLonLft < mLonRt) {
			if ((lon < mLonLft) || (lon > mLonRt))
				return false;
		}
		else {
			if ((lon >= mLonLft) || (lon <= mLonRt))
				;
			else
				return false;
		}
		return true;
	}

	public void paintPanelData(Graphics gin, int width, int height, int top, int left, int bott, int right) {
		if (gin == null)
			return;
		Graphics2D g = (Graphics2D) gin;
		double minLat;
		double maxLat;
		double minLon;
		double maxLon;

		mTop = top;
		mLeft = left;
		mRight = right;
		mBottom = bott;
		mWidth = width;
		mHeight = height;

		if (mCurrPC == null)
			return;

		if (TRACE)
			System.out.println("Plotting LatLon for firstTime = " + firstPlotDone);
		firstPlotDone = true;

		// initialize the axes ranges
		if (!axesInitialized) {
			// get a range from the view manager if these ranges have been set by
			// another view
			Float latMin = vm.getAxisMin(Constants.LAT_AXIS, Constants.LAT_LON);
			Float latMax = vm.getAxisMax(Constants.LAT_AXIS, Constants.LAT_LON);
			Float lonMin = vm.getAxisMin(Constants.LON_AXIS, Constants.LAT_LON);
			Float lonMax = vm.getAxisMax(Constants.LON_AXIS, Constants.LAT_LON);
			Float selLatMin = vm.getAxisSelMin(Constants.LAT_AXIS, Constants.LAT_LON);
			Float selLatMax = vm.getAxisSelMax(Constants.LAT_AXIS, Constants.LAT_LON);
			Float selLonMin = vm.getAxisSelMin(Constants.LON_AXIS, Constants.LAT_LON);
			Float selLonMax = vm.getAxisSelMax(Constants.LON_AXIS, Constants.LAT_LON);

			if ((latMin != null && latMax != null && (latMin.floatValue() != 0.0 && latMax.floatValue() != 0.0))
			    && (selLatMin != null && selLatMax != null && (selLatMin.floatValue() != 0.0 && selLatMax.floatValue() != 0.0))) {
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

			if ((lonMin != null && lonMax != null && (lonMin.floatValue() != 0.0 && lonMax.floatValue() != 0.0))
			    && (selLonMin != null && selLonMax != null && (selLonMin.floatValue() != 0.0 && selLonMax.floatValue() != 0.0))) {
				setMinXVal(lonMin.floatValue());
				setMaxXVal(lonMax.floatValue());
				selMinLon = selLonMin.floatValue();
				selMaxLon = selLonMax.floatValue();
			}
			else {
				if (mCurrPC.getMaxLon() != mCurrPC.getMinLon()) {
					// compute a nice range
					double range = Math.abs(mCurrPC.getMaxLon() - mCurrPC.getMinLon());
					setMinXVal(NdEditFormulas.getNiceLowerValue(mCurrPC.getMinLon(), range, -180.0));
					setMaxXVal(NdEditFormulas.getNiceUpperValue(mCurrPC.getMaxLon(), range, 180.0));
					selMinLon = getMinXVal();
					selMaxLon = getMaxXVal();
				}
				else {
					// point observation
					setMinXVal(mCurrPC.getMinLon() - 1.0);
					setMaxXVal(mCurrPC.getMaxLon() + 1.0);
					selMinLon = getMinXVal();
					selMaxLon = getMaxXVal();
				}
			}
			axesInitialized = true;
		}

		// scale the axes
		minLat = getMinYVal();
		maxLat = getMaxYVal();
		minLon = getMinXVal();
		maxLon = getMaxXVal();

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
		g.fillRect(this.getMinX() - 10, this.getMinY() - 10, this.getMaxX() - this.getMinX() + 10, this.getMaxY()
		    - this.getMinY() + 30);

		try {
			// convertFOCIFile();
			plotMap(g);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		if (mPlotBathy) {
			drawIsobaths(g);
		}

		// draw the points
		int swidth = 4;
		swidth /= 2;

		// long stTime = System.currentTimeMillis();
		BasicStroke bs2 = new BasicStroke(2);
		BasicStroke bs1 = new BasicStroke(1);
		byte[] currFilterResults = fdm.getResults();

		int numVPoints = mCurrPC.getSize();
		int maxx = this.getMaxX();
		int maxy = this.getMaxY();
		boolean[][] t = new boolean[maxx][maxy];

		for (int i = 0; i < numVPoints; i++) {
			boolean deleted = mCurrPC.isDeleted(i);
			boolean filtered = currFilterResults[i] != 4;

			// System.out.println(i + " f = " + currFilterResults[i]);
			if (!mIgnoreFilter && (deleted || filtered))
				continue;

			double lat = mCurrPC.getLat1(i);
			double lon = mCurrPC.getLon1(i);

			// might be able to further optimize by doing a LLKeep function here

			double x = (lon - winXOrigin) * winXScale;
			double y = (lat - winYOrigin) * winYScale;
			/*
			 * int ix = (int)x; int iy = (int)y;
			 * 
			 * try { if ((ix < 0 || ix >= maxx || iy < 0 || iy >= maxy || t[ix][iy]) &&
			 * !mCurrPC.isSelected(i)) continue;
			 * 
			 * t[ix][iy] = true; } catch (Exception ex) {}
			 */

			boolean xIsScaler = mCurrPC.isLonScaler(i);
			boolean yIsScaler = mCurrPC.isLatScaler(i);

			// adjust for axes labels
			x += mLeft;
			y += mTop;

			double x2 = Float.NaN;
			double y2 = Float.NaN;
			double lat2 = Float.NaN;
			double lon2 = Float.NaN;

			if (!yIsScaler) {
				lat2 = mCurrPC.getLat2(i);
				y2 = (lat2 - winYOrigin) * winYScale;
				y2 += mTop;
			}

			if (!xIsScaler) {
				lon2 = mCurrPC.getLon2(i);
				// lon2 = (double)((lon2 + 360) % 360.0);
				x2 = (lon2 - winXOrigin) * winXScale;
				x2 += mLeft;
			}

			if (yIsScaler && xIsScaler) {
				// point observation
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D) g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D) g).setStroke(bs1);
				}
				g.drawLine((int) (x - swidth), (int) y, (int) (x + swidth), (int) y);
				g.drawLine((int) x, (int) (y - swidth), (int) x, (int) (y + swidth));
			}
			if (!xIsScaler && yIsScaler) {
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D) g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D) g).setStroke(bs1);
				}
				// lon range
				g.drawLine((int) x, (int) (y - swidth), (int) x, (int) (y + swidth));
				g.drawLine((int) x, (int) y, (int) x2, (int) y);
				g.drawLine((int) x2, (int) (y - swidth), (int) x2, (int) (y + swidth));
			}
			else if (xIsScaler && !yIsScaler) {
				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D) g).setStroke(bs2);
				}
				else {
					g.setColor(mCurrPC.getColor(i));
					((Graphics2D) g).setStroke(bs1);
				}
				// lat range
				g.drawLine((int) (x - swidth), (int) y, (int) (x + swidth), (int) y);
				g.drawLine((int) x, (int) y, (int) x, (int) y2);
				g.drawLine((int) (x - swidth), (int) y2, (int) (x + swidth), (int) y2);
			}
			else if (!xIsScaler && !yIsScaler) {
				// range in both axes--draw a rectangle
				g.setColor(mCurrPC.getColor(i));
				((Graphics2D) g).setStroke(bs1);

				if (Math.abs(x2 - x) < 100) {
					// line always drawn in unselected color
					g.drawLine((int) x, (int) y, (int) x2, (int) y2);
				}

				g.drawLine((int) (x2 - 1), (int) y2, (int) (x2 + 1), (int) y2);
				g.drawLine((int) x2, (int) (y2 - 1), (int) x2, (int) (y2 + 1));

				if (mCurrPC.isSelected(i)) {
					g.setColor(Color.blue);
					((Graphics2D) g).setStroke(bs2);
				}
				g.drawLine((int) (x - 1), (int) y, (int) (x + 1), (int) y);
				g.drawLine((int) x, (int) (y - 1), (int) x, (int) (y + 1));
			}
		}

		// long enTime = System.currentTimeMillis();
		// System.out.println("elapsed time is " + (enTime - stTime));

		g.setStroke(bs1);
		g.setClip(0, 0, 10000, 10000);
		SelectionRegion.selOffset = 0;

		// construct the draghandles
		if (mXAxisLeft == null) {
			int initH = mLeft + 5;
			int initV = mTop + 5;
			double initVal = selMinLon;
			mXAxisLeft = new LeftDragHandle(this, initH, initV, DragHandle.LEFTHANDLE, DragHandle.RECTSTYLE,
			    DragHandle.HORIENTATION, Constants.LON_AXIS, mLeft + 5, mLeft + scaleWidth + 5, minLon, maxLon, initVal);
			startLon.setValue(initVal);
		}
		else {
			// set range of the axis: only need to do this for one handle on the axis
			mXAxisLeft.setAxisRange(mLeft + 5, mLeft + scaleWidth + 5);
		}

		if (mXAxisCtr == null) {
			int initH = mLeft + 5 + (int) (0.50 * scaleWidth);
			int initV = mTop + 5;
			double xDiff = (selMaxLon - selMinLon);
			double initVal = selMinLon + (0.50f * xDiff);
			mXAxisCtr = new CenterHDragHandle(this, initH, initV, DragHandle.CENTERHHANDLE, DragHandle.RECTSTYLE,
			    DragHandle.HORIENTATION, Constants.LON_AXIS, mLeft + 5, mLeft + scaleWidth + 5, minLon, maxLon, initVal);
		}
		else {
			// set range of the axis: only need to do this for one handle on the axis
			mXAxisCtr.setAxisRange(mLeft + 5, mLeft + scaleWidth + 5);
		}

		if (mXAxisRight == null) {
			int initH = mLeft + 5 + scaleWidth;
			int initV = mTop + 5;
			double initVal = selMaxLon;
			mXAxisRight = new RightDragHandle(this, initH, initV, DragHandle.RIGHTHANDLE, DragHandle.RECTSTYLE,
			    DragHandle.HORIENTATION, Constants.LON_AXIS, mLeft + 5, mLeft + scaleWidth + 5, minLon, maxLon, initVal);
			stopLon.setValue(initVal);
		}
		else {
			// set range of the axis: only need to do this for one handle on the axis
			mXAxisRight.setAxisRange(mLeft + 5, mLeft + scaleWidth + 5);
		}

		if (mYAxisUpper == null) {
			int initV = mTop + 5;
			int initH = mLeft + scaleWidth + 5;
			double initVal = selMaxLat;
			mYAxisUpper = new UpperDragHandle(this, initH, initV, DragHandle.UPPERHANDLE, DragHandle.RECTSTYLE,
			    DragHandle.VORIENTATION, Constants.LAT_AXIS, mTop + 5, mTop + scaleHeight + 5, minLat, maxLat, initVal);
			stopLat.setValue(initVal);
		}
		else {
			// set the range of the axis: only need to do this for one handle on the
			// axis
			mYAxisUpper.setMaxH(mLeft + scaleWidth + 5);
			mYAxisUpper.setAxisRange(mTop + 5, mTop + scaleHeight + 5);
		}

		if (mYAxisCtr == null) {
			int initV = mTop + 5 + (int) (0.50 * scaleHeight);
			int initH = mLeft + scaleWidth + 5;
			double yDiff = (selMaxLat - selMinLat);
			double initVal = selMaxLat - (0.50f * yDiff);
			mYAxisCtr = new CenterVDragHandle(this, initH, initV, DragHandle.CENTERVHANDLE, DragHandle.RECTSTYLE,
			    DragHandle.VORIENTATION, Constants.LAT_AXIS, mTop + 5, mTop + scaleHeight + 5, minLat, maxLat, initVal);
		}
		else {
			// set the range of the axis: only need to do this for one handle on the
			// axis
			mYAxisCtr.setMaxH(mLeft + scaleWidth + 5);
			mYAxisCtr.setAxisRange(mTop + 5, mTop + scaleHeight + 5);
		}

		if (mYAxisLower == null) {
			int initV = mTop + 5 + scaleHeight;
			int initH = mLeft + scaleWidth + 5;
			double initVal = selMinLat;
			mYAxisLower = new LowerDragHandle(this, initH, initV, DragHandle.LOWERHANDLE, DragHandle.RECTSTYLE,
			    DragHandle.VORIENTATION, Constants.LAT_AXIS, mTop + 5, mTop + scaleHeight + 5, minLat, maxLat, initVal);
			startLat.setValue(initVal);
		}
		else {
			// set the range of the axis: only need to do this for one handle on the
			// axis
			mYAxisLower.setMaxH(mLeft + scaleWidth + 5);
			mYAxisLower.setAxisRange(mTop + 5, mTop + scaleHeight + 5);
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
			stopLon.fireLonChange();
			startLon.fireLonChange();
			stopLat.fireLatChange();
			startLat.fireLatChange();
			setHandle(this, "latmin", new Double(selMinLat), true);
			setHandle(this, "latmax", new Double(selMaxLat), true);
			setHandle(this, "lonmin", new Double(selMinLon), true);
			setHandle(this, "lonmax", new Double(selMaxLon), true);
		}
		else {
			// set the current selection region
			SelectionRegion.selOffset = 0;
			mHilitedRegion.resetRgnBounds();
		}

		// draw the axes
		if (plotAxes) {
			drawYAxis(g, minLat, maxLat, false, "Latitude", height, width, mBottom, mTop, mLeft, mRight);
			drawXAxis(g, minLon, maxLon, false, "Longitude", height, width, mBottom, mTop, mLeft, mRight);
		}

		if (mPlotBathy) {
			plotLegend(g);
		}
		// prog.endProgressPresenter();
	}

	public void plotLegend(Graphics g) {
		BathyOverlaySpec spec = (BathyOverlaySpec) mOverlaySpec;

		int numColors = spec.getNumIsobaths();

		Dimension d = this.getSize();
		int right = d.width;
		int mTop = 35;
		int bottom = d.height - 15;
		int pixelsPerBand = (bottom - mTop - 10) / numColors;
		if (pixelsPerBand > 17)
			pixelsPerBand = 17;
		int bandTop = 0;
		int bandBottom = 0;

		// title
		g.drawString("Depth (m)", right - 67, mTop - 22);

		g.setFont(new Font("Courier", Font.PLAIN, 11));
		int c = 0;
		for (int i = 0; i < numColors; i++) {
			// swatch
			bandTop = (int) (mTop + (c - 1) * pixelsPerBand);
			bandBottom = (int) (bandTop + pixelsPerBand);
			g.setColor(spec.mIsobathColors[i]);
			g.fillRect(right - 70, bandTop, 20, bandBottom - bandTop);

			// label
			g.setColor(Color.black);
			double myVal = spec.mIsobathValues[i];
			String sTemp = NdEditFormulas.formatDouble(String.valueOf(myVal), 0, true);
			g.drawString(sTemp, right - 70 + 2, bandBottom);
			c++;
		}

	}

	public void drawYAxis(Graphics2D g, double winYPlotMin, double winYPlotMax, boolean mReverseY, String axisLabel,
	    int mHeightCurrWindow, int mWidthCurrWindow, int PVPBOTTOM, int PVPTOP, int PVPLEFT, int PVPRIGHT) {
		;
		g.setColor(Color.black);
		int bottom = (int) mHeightCurrWindow - 1 * PVPBOTTOM;
		int top = PVPTOP;
		int left = PVPLEFT;
		int right = (int) mWidthCurrWindow - PVPRIGHT;

		// draw the Y axis
		g.drawLine(left, top, left, bottom);

		// draw the tics
		g.drawLine(left - 2, bottom, left + 2, bottom);
		g.drawLine(left - 2, top, left + 2, top);

		// complete the box
		g.drawLine(right, top, right, bottom);

		// set the Y precision
		int numPlaces = NdEditFormulas.GetDisplayPrecision(yInc);

		// draw the lower label
		String minValLbl = null;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LatitudeFormat latDM = new LatitudeFormat();
			minValLbl = latDM.format((float) winYPlotMin);
		}
		else
			minValLbl = NdEditFormulas.formatLat(winYPlotMin, numPlaces);

		// draw the upper label
		String maxValLbl = null;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LatitudeFormat latDM = new LatitudeFormat();
			maxValLbl = latDM.format((float) winYPlotMax);
		}
		else
			maxValLbl = NdEditFormulas.formatLat(winYPlotMax, numPlaces);

		Font font = new Font(Constants.DEFAULT_AXIS_VALUE_FONT, Constants.DEFAULT_AXIS_VALUE_STYLE,
		    Constants.DEFAULT_AXIS_VALUE_SIZE);
		g.setFont(font);
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int height = this.getSize().height;

		// plot minLabel
		int width = fm.stringWidth(minValLbl);
		NdEditFormulas.drawStyledString(minValLbl, 25, bottom, 0.0, 0.0, g, 90, Constants.DEFAULT_AXIS_VALUE_FONT,
		    Constants.DEFAULT_AXIS_VALUE_SIZE, Constants.DEFAULT_AXIS_VALUE_STYLE, Constants.DEFAULT_AXIS_VALUE_COLOR);

		width = fm.stringWidth(maxValLbl);
		NdEditFormulas.drawStyledString(maxValLbl, 25, PVPTOP + width, 0.0, 0.0, g, 90, Constants.DEFAULT_AXIS_VALUE_FONT,
		    Constants.DEFAULT_AXIS_VALUE_SIZE, Constants.DEFAULT_AXIS_VALUE_STYLE, Constants.DEFAULT_AXIS_VALUE_COLOR);

		// add variable label
		font = new Font(Constants.DEFAULT_AXIS_LABEL_FONT, Constants.DEFAULT_AXIS_LABEL_STYLE,
		    Constants.DEFAULT_AXIS_LABEL_SIZE);
		g.setFont(font);
		fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		width = fm.stringWidth(axisLabel);
		NdEditFormulas.drawStyledString(axisLabel, 20, height / 2 - width / 2, 0.0, 0.0, g, 90,
		    Constants.DEFAULT_AXIS_LABEL_FONT, Constants.DEFAULT_AXIS_LABEL_SIZE, Constants.DEFAULT_AXIS_LABEL_STYLE,
		    Constants.DEFAULT_AXIS_LABEL_COLOR);
	}

	public void drawXAxis(Graphics2D g, double winXPlotMin, double winXPlotMax, boolean mReverseY, String axisLabel,
	    int mHeightCurrWindow, int mWidthCurrWindow, int PVPBOTTOM, int PVPTOP, int PVPLEFT, int PVPRIGHT) {
		g.setColor(Color.black);
		int bottom = (int) mHeightCurrWindow - 1 * PVPBOTTOM;
		int top = PVPTOP;
		int left = PVPLEFT;
		int right = (int) mWidthCurrWindow - PVPRIGHT;

		// draw the X axis
		g.drawLine(left, bottom, right, bottom);

		// draw the tics
		g.drawLine(left, bottom + 2, left, bottom - 2);
		g.drawLine(right, bottom + 2, right, bottom - 2);

		// complete the box ;
		g.drawLine(left, top, right, top);

		// set the X precision
		int numPlaces = NdEditFormulas.GetDisplayPrecision(xInc);

		// label the x axis
		g.setColor(Color.black);

		// draw the left label
		String minValLbl = null;
		double correctedLon = winXPlotMin;
		if (winXPlotMin > 180)
			correctedLon = winXPlotMin - 360.0f;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LongitudeFormat lonDM = new LongitudeFormat();
			minValLbl = lonDM.format((float) correctedLon);
		}
		else
			minValLbl = NdEditFormulas.formatLon(correctedLon, numPlaces);

		// draw the right label
		correctedLon = winXPlotMax;
		if (winXPlotMax > 180)
			correctedLon = winXPlotMax - 360.0f;
		String maxValLbl = null;
		if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			LongitudeFormat lonDM = new LongitudeFormat();
			maxValLbl = lonDM.format((float) correctedLon);
		}
		else
			maxValLbl = NdEditFormulas.formatLon(correctedLon, numPlaces);

		Font font = new Font(Constants.DEFAULT_AXIS_VALUE_FONT, Constants.DEFAULT_AXIS_VALUE_STYLE,
		    Constants.DEFAULT_AXIS_VALUE_SIZE);
		g.setFont(font);
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int height = this.getSize().height;
		int width = fm.stringWidth(minValLbl);

		NdEditFormulas.drawStyledString(minValLbl, mLeft, bottom + 12, 0.0, 0.0, g, 0, Constants.DEFAULT_AXIS_VALUE_FONT,
		    Constants.DEFAULT_AXIS_VALUE_SIZE, Constants.DEFAULT_AXIS_VALUE_STYLE, Constants.DEFAULT_AXIS_VALUE_COLOR);

		width = fm.stringWidth(maxValLbl);
		NdEditFormulas.drawStyledString(maxValLbl, mWidthCurrWindow - width - 25, bottom + 12, 0.0, 0.0, g, 0,
		    Constants.DEFAULT_AXIS_VALUE_FONT, Constants.DEFAULT_AXIS_VALUE_SIZE, Constants.DEFAULT_AXIS_VALUE_STYLE,
		    Constants.DEFAULT_AXIS_VALUE_COLOR);

		// add variable label
		font = new Font(Constants.DEFAULT_AXIS_LABEL_FONT, Constants.DEFAULT_AXIS_LABEL_STYLE,
		    Constants.DEFAULT_AXIS_LABEL_SIZE);
		g.setFont(font);
		fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		width = fm.stringWidth(axisLabel);
		int hh = mWidthCurrWindow / 2 - (width / 2);// + PVPRIGHT;
		int vv = mHeightCurrWindow - 30;
		NdEditFormulas.drawStyledString(axisLabel, hh, vv, 0.0, 0.0, g, 0, Constants.DEFAULT_AXIS_LABEL_FONT,
		    Constants.DEFAULT_AXIS_LABEL_SIZE, Constants.DEFAULT_AXIS_LABEL_STYLE, Constants.DEFAULT_AXIS_LABEL_COLOR);
	}

	public void setHandle(Object src, String prop, Object val, boolean setNeighbors) {
		if (mXAxisLeft == null)
			return;
		double dval = 0.0f;
		long lval = 0;
		try {
			dval = (double) (((Double) val).floatValue());
		}
		catch (ClassCastException ex) {
			lval = ((Long) val).longValue();
			dval = (double) lval;
		}
		Graphics g = this.getGraphics();

		if (prop.indexOf("lonmin") >= 0 && mXAxisLeft != src) {
			mXAxisLeft.resetStyle();
			// mXAxisLeft.setOutOfRange(false);
			mXAxisLeft.setDontBroadcast(false);
			mXAxisLeft.setValue(dval);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
			int del = mXAxisCtr.getCurrLocation().x - mXAxisLeft.getCurrLocation().x;
			mXAxisLeft.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("lonctr") >= 0 && mXAxisCtr != src) {
			mXAxisCtr.setDontBroadcast(true);
			mXAxisCtr.setValue(dval);
			mXAxisCtr.setXLocation(mXAxisCtr.getNewLocation());
			mXAxisCtr.setNeighbors();
		}
		else if (prop.indexOf("lonmax") >= 0 && mXAxisRight != src) {
			mXAxisRight.resetStyle();
			// mXAxisRight.setOutOfRange(false);
			mXAxisRight.setDontBroadcast(false);
			mXAxisRight.setValue(dval);
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
			int del = mXAxisRight.getCurrLocation().x - mXAxisCtr.getCurrLocation().x;
			mXAxisRight.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("latmin") >= 0 && mYAxisLower != src) {
			mYAxisLower.resetStyle();
			// mYAxisLower.setOutOfRange(false);
			mYAxisLower.setDontBroadcast(false);
			mYAxisLower.setValue(dval);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
			int del = mYAxisLower.getCurrLocation().y - mYAxisCtr.getCurrLocation().y;
			mYAxisLower.setNeighbors(del, this.isIndependentHandles());
		}
		else if (prop.indexOf("latctr") >= 0 && mYAxisCtr != src) {
			mYAxisCtr.setDontBroadcast(true);
			mYAxisCtr.setValue(dval);
			mYAxisCtr.setYLocation(mYAxisCtr.getNewLocation());
			mYAxisCtr.setNeighbors();
		}
		else if (prop.indexOf("latmax") >= 0 && mYAxisUpper != src) {
			mYAxisUpper.resetStyle();
			// mYAxisUpper.setOutOfRange(false);
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
		Graphics g = this.getGraphics();
		double dval = 0.0f;
		long lval = 0;
		try {
			dval = (double) (((Double) val).floatValue());
		}
		catch (ClassCastException ex) {
			lval = ((Long) val).longValue();
			dval = (double) lval;
		}

		if (prop.indexOf("lonmin") >= 0) {
			mXAxisLeft.setAxisMinVal(dval);
			mXAxisCtr.setAxisMinVal(dval);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
		}
		else if (prop.indexOf("lonmax") >= 0) {
			mXAxisRight.setAxisMaxVal(dval);
			mXAxisCtr.setAxisMaxVal(dval);
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
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
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
			// lat on the y axis
			mYAxisLower.setDontBroadcast(true);
			mYAxisLower.setDontBroadcast(true);
			mYAxisLower.setDontBroadcast(true);
			mYAxisLower.setAxisMinVal(min);
			mYAxisLower.setAxisMaxVal(max);
			mYAxisCtr.setAxisMinVal(min);
			mYAxisCtr.setAxisMaxVal(max);
			mYAxisUpper.setAxisMinVal(min);
			mYAxisUpper.setAxisMaxVal(max);
			mYAxisLower.setYLocation(mYAxisLower.getNewLocation());
			mYAxisUpper.setYLocation(mYAxisUpper.getNewLocation());
			mYAxisCtr.setYLocation(mYAxisUpper.getCurrLocation().y
			    + (mYAxisLower.getCurrLocation().y - mYAxisUpper.getCurrLocation().y) / 2);
			mYAxisLower.setDontBroadcast(false);
			mYAxisLower.setDontBroadcast(false);
			mYAxisLower.setDontBroadcast(false);
		}
		else if (prop.indexOf("lon") >= 0) {
			// lon on the x axis
			mXAxisLeft.setAxisMinVal(min);
			mXAxisLeft.setAxisMaxVal(max);
			mXAxisCtr.setAxisMinVal(min);
			mXAxisCtr.setAxisMaxVal(max);
			mXAxisRight.setAxisMinVal(min);
			mXAxisRight.setAxisMaxVal(max);
			mXAxisLeft.setXLocation(mXAxisLeft.getNewLocation());
			mXAxisRight.setXLocation(mXAxisRight.getNewLocation());
			mXAxisCtr.setXLocation(mXAxisLeft.getCurrLocation().x
			    + (mXAxisRight.getCurrLocation().x - mXAxisLeft.getCurrLocation().x) / 2);
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
			dval = (double) (((Double) val).floatValue());
			lval = (long) dval;
		}
		catch (ClassCastException ex) {
			lval = (long) (((Long) val).longValue());
			dval = (double) lval;
		}

		// set the text field values
		if (prop.indexOf("lonmin") >= 0) {
			startLon.setLon(dval);
		}
		else if (prop.indexOf("lonmax") >= 0) {
			stopLon.setLon(dval);
		}
		else if (prop.indexOf("latmin") >= 0) {
			startLat.setLat(dval);
		}
		else if (prop.indexOf("latmax") >= 0) {
			stopLat.setLat(dval);
		}
	}

	public String toString() {
		return "LatLon View";
	}

	public int ComputeSectionPixelWidth(Point p) {
		int x = p.x - mLeft - 5;
		int y = p.y - mTop - 5;
		double xReal = (x / winXScale) + winXOrigin;
		double yReal = (y / winYScale) + winYOrigin;

		// keeping lon constant, iteratively find a matching great circle distance
		double tstReal = yReal;
		double d;
		while (true) {
			d = NdEditFormulas.GreatCircle(yReal, xReal, tstReal, xReal);
			d /= 0.62;
			if (d >= Constants.SECTION_WIDTH) {
				break;
			}
			tstReal += 0.1;
		}

		double u = (xReal - winXOrigin) * winXScale;
		double v = (tstReal - winYOrigin) * winYScale;

		int pd = ((int) v - y) / 2;
		pd = pd < 0 ? -pd : pd;
		return pd == 0 ? 1 : pd;
	}

	public void setFieldsFromHandles() {
		// startLon.setLon(mYAxisLower.getValue());
		// stopLon.setLon(mYAxisUpper.getValue());
		// startDate.setDate(new Date((long)mXAxisLeft.getValue()));
		// stopDate.setDate(new Date((long)mXAxisRight.getValue()));
	}

	public void handleAxisRangeClick(String theAxis) {
		((NdEdit) mViewManager.getParent()).pushChangeableInfo("StartBatch", null, null, false);
		if (theAxis.equals("Y")) {
			double[] oldYs = { getMinYVal(), getMaxYVal() };
			if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
				JLatSpinDialog latDialog = new JLatSpinDialog((float) oldYs[0], (float) oldYs[1]);
				double[] newYs = { latDialog.getLatitude(), latDialog.getLatitude2() };
				((NdEdit) mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldYs, newYs, false);
			}
			else {
				JLatDialog latDialog = new JLatDialog((float) oldYs[0], (float) oldYs[1], "###.###N;###.###S");
				double[] newYs = { latDialog.getLatitude(), latDialog.getLatitude2() };
				((NdEdit) mViewManager.getParent()).pushChangeableInfo("ZoomLatitudeDomain", oldYs, newYs, false);
			}
		}
		else {
			double[] oldXs = { getMinXVal(), getMaxXVal() };
			if (this.getGeoDisplayFormat() == Constants.DEG_MINUTES_SEC_GEO_DISPLAY) {
				JLonSpinDialog lonDialog = new JLonSpinDialog((float) correctLongitude(oldXs[0]),
				    (float) correctLongitude(oldXs[1]));
				double[] newXs = { unCorrectLongitude(lonDialog.getLongitude()), unCorrectLongitude(lonDialog.getLongitude2()) };
				((NdEdit) mViewManager.getParent()).pushChangeableInfo("ZoomLongitudeDomain", oldXs, newXs, false);
			}
			else {
				JLonDialog lonDialog = new JLonDialog((float) oldXs[0], (float) oldXs[1], "###.###E;###.###W");
				double[] newXs = { (float) lonDialog.getLongitude(), (float) lonDialog.getLongitude2() };
				((NdEdit) mViewManager.getParent()).pushChangeableInfo("ZoomLongitudeDomain", oldXs, newXs, false);
			}
		}
		((NdEdit) mViewManager.getParent()).pushChangeableInfo("EndBatch", null, null, false);
	}

	public void displayCursorValues(Point thePt) {
		if (mCurrPC == null || !axesInitialized)
			return;

		// convert the point to values
		double yAxisVal, xAxisVal;
		int x = thePt.x - mLeft - 5;
		int y = thePt.y - mTop - 5;
		xAxisVal = (x / winXScale) + winXOrigin;
		yAxisVal = (y / winYScale) + winYOrigin;
		((NdEdit) vm.getParent()).setLocation(xAxisVal, yAxisVal, Float.NaN, Float.NaN);

	}

	public void convertFOCIFile() {
		String[] inFiles = { "pjs_con50_bering.txt", "eps_bathy_foci.100", "eps_bathy_foci.200", "eps_bathy_foci.1000",
		    "eps_coastline_bcgs.dat" };
		String[] outFiles = { "c50_foci", "c100_foci", "c200_foci", "c1000_foci", "npac_hi_rez.bin" };

		double[] tempLats = new double[10000];
		double[] tempLons = new double[10000];
		double[] minLon = { 360.0f, 360.0f, 360.0f, 360.0f, 360.0f };
		double[] minLat = { 90.0f, 90.0f, 90.0f, 90.0f, 90.0f };
		double[] maxLon = { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
		double[] maxLat = { -90.0f, -90.0f, -90.0f, -90.0f, -90.0f };

		for (int i = 0; i < 1; i++) {
			try {
				// open the input file
				String directory = NdEditFormulas.getSupportPath() + "bathymetry" + File.separator + inFiles[i];
				File inFile = new File(directory);
				LineNumberReader in = new LineNumberReader(new FileReader(inFile), 10000);

				// open the output file
				directory = NdEditFormulas.getSupportPath() + "bathymetry" + File.separator + outFiles[i];
				File outFile = new File(directory);
				FileOutputStream fos = new FileOutputStream(outFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
				DataOutputStream out = new DataOutputStream(bos);
				int len = 0;
				int segCnt = 0;
				int cnt = 0;
				while (true) {
					String inLine = in.readLine();
					if (inLine == null)
						break;

					if ((len = inLine.length()) == 0)
						continue;

					StringTokenizer st = new StringTokenizer(inLine);

					// item #1 is longitude, item #2 is latitude
					double lonVal = Float.valueOf(st.nextToken()).floatValue();
					double latVal = Float.valueOf(st.nextToken()).floatValue();

					minLon[i] = (lonVal < minLon[i] && lonVal < 1.000000E+34) ? lonVal : minLon[i];
					minLat[i] = (latVal < minLat[i] && latVal < 1.000000E+34) ? latVal : minLat[i];
					maxLon[i] = (lonVal > maxLon[i] && lonVal < 1.000000E+34) ? lonVal : maxLon[i];
					maxLat[i] = (latVal > maxLat[i] && latVal < 1.000000E+34) ? latVal : maxLat[i];

					// correct longitudes
					if (lonVal > 180 && lonVal < 1.000000E+34)
						lonVal -= 360;

					if (lonVal > 1.000000E+34 || latVal > 1.000000E+34) {
						// write num entries in segment
						out.writeShort(segCnt);

						// write a segement's lats
						for (int ii = 0; ii < segCnt; ii++) {
							out.writeDouble(tempLats[ii]);
						}
						// write a segement's lons
						for (int ii = 0; ii < segCnt; ii++) {
							out.writeDouble(-tempLons[ii]);
						}
						cnt++;
						segCnt = 0;
					}
					else {
						// store lon, lat pair in temp array
						tempLats[segCnt] = latVal;
						tempLons[segCnt] = lonVal;
						segCnt++;
					}
				}

				// write last segment
				// write num entries in segment
				out.writeShort(segCnt);

				// write a segement's lats
				for (int ii = 0; ii < segCnt; ii++) {
					out.writeDouble(tempLats[ii]);
				}
				// write a segement's lons
				for (int ii = 0; ii < segCnt; ii++) {
					out.writeDouble(-tempLons[ii]);
				}
				out.writeShort(-1);
				out.flush();
				out.close();
			}
			catch (Exception ex) {
				System.out.println("threw" + i);
			}
			// System.out.println(inFiles[i] + "lat range=" + minLat[i] + ":" +
			// maxLat[i]);
			// System.out.println(inFiles[i] + "lon range=" + minLon[i] + ":" +
			// maxLon[i]);
		}
	}

	public void plotMap(Graphics ig) throws IOException {
		Graphics2D g = (Graphics2D) ig;
		if (latVal == null) {
			// get the coastline
			int cpt = 0;
			int cpt_save = 0;
			int cut_count = 0;
			BufferedInputStream bis1 = null;
			BufferedInputStream bis2 = null;
			BufferedInputStream bis3 = null;
			BufferedInputStream bis = null;

			String coastDir = null;

			// built in coast
			try {
				if (coastDir == null) {
					String directory = NdEditFormulas.getCoastlinePath();
					// try to get the coastline from the support directory instead of the
					// jar
					File coastFile = null;
					coastFile = new File(directory, "finerezcoast.bin");
					FileInputStream in = new FileInputStream(coastFile);
					bis2 = new BufferedInputStream(in, 1000000);
				}
				else {
					File coastFile = null;
					coastFile = new File(coastDir, "finerezcoast.bin");
					FileInputStream in = new FileInputStream(coastFile);
					bis2 = new BufferedInputStream(in, 1000000);
				}
			}
			catch (Exception ex) {
				return;
			}

			/*
			 * try { InputStream cin = null; Class coastClass =
			 * Class.forName("client.wizard.WizardPanel"); cin =
			 * coastClass.getResourceAsStream("coast/finerezcoast.bin"); bis1 = new
			 * BufferedInputStream(cin, 1000000); } catch (Exception ex) {}
			 */

			if (custCoastPath != null) {
				// get a custom coastline
				try {
					File coastFile = null;
					coastFile = new File(custCoastPath);
					FileInputStream in = new FileInputStream(coastFile);
					bis1 = new BufferedInputStream(in, 1000000);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (bis1 == null) {
				if (bis2 == null) {
					return;
				}
				else
					bis = bis2;
			}
			else
				bis = bis1;

			double testLeftLon = this.getMinXVal();
			double testRightLon = this.getMaxXVal();
			double testMinLat = this.getMinYVal();
			double testMaxLat = this.getMaxYVal();

			boolean lonCor = false;
			if (testLeftLon > -180 && testRightLon < 180)
				// 0 degree centered do nothing
				lonCor = false;
			else if (testLeftLon > 0 && testRightLon > 180)
				// 180 centered--convert the basin ranges
				lonCor = true;
			else if (testLeftLon < 0 && testRightLon > 180)
				// 180 centered--convert the basin ranges
				lonCor = true;

			try {
				DataInputStream inData = new DataInputStream(bis);
				while (true) {
					// get the number of entries
					int numEntries = inData.readShort();
					if (numEntries == -1)
						break;

					// get the lats and lons for this segment
					cpt_save = cpt;
					for (int i = 0; i < numEntries; i++) {
						coastlats[cpt] = (double) inData.readDouble();
						cpt++;
					}

					cpt = cpt_save;
					for (int i = 0; i < numEntries; i++) {
						// map -180 to 180 to 0 to 360
						double templon = (double) inData.readDouble();
						if (lonCor)
							coastlons[cpt] = (double) ((templon + 360) % 360.0);
						else
							coastlons[cpt] = templon;

						if (cpt > 0 && Math.abs(coastlons[cpt] - coastlons[cpt - 1]) > 50.0) {
							cut_count++;
						}
						cpt++;
					}

					coastlats[cpt] = Float.NaN;
					coastlons[cpt] = Float.NaN;
					cpt++;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			int length = cpt;
			latVal = new double[cpt + cut_count];
			lonVal = new double[cpt + cut_count];
			cpt = 0;
			for (int i = 0; i < length; i++) {
				if (i > 0 && Math.abs(coastlons[i] - coastlons[i - 1]) > 50.0) {
					latVal[cpt] = Float.NaN;
					lonVal[cpt] = Float.NaN;
					cpt++;
				}
				latVal[cpt] = coastlats[i];
				lonVal[cpt] = coastlons[i];
				cpt++;
			}
		}

		float u, u2, startU = 0.0f;
		float oldU = (float) (((lonVal[0]) - winXOrigin) * winXScale);
		float v, v2, startV = 0.0f;
		float oldV = (float) ((latVal[0] - winYOrigin) * winYScale);
		oldU += mLeft;
		oldV += mTop;
		int c = 1;
		int arrayLen = latVal.length;

		g.setColor(((BathyOverlaySpec) mOverlaySpec).getCoastColor());
		int w = ((BathyOverlaySpec) mOverlaySpec).getCoastWeight();

		BasicStroke lw = new BasicStroke(w);
		g.setStroke(lw);
		GeneralPath mCoastSegment = new GeneralPath();
		mCoastSegment.moveTo(oldU, oldV);

		while (c < arrayLen - 1) {
			if (!Double.isNaN(lonVal[c])) {
				u = (float) (((lonVal[c]) - winXOrigin) * winXScale);
				v = (float) ((latVal[c] - winYOrigin) * winYScale);
				u += mLeft;
				v += mTop;
				if (Math.abs(oldU - u) < 100) {
					// add to path
					mCoastSegment.lineTo(u, v);
					// g.drawLine((int)oldU, (int)oldV, (int)u, (int)v);
				}
				oldU = u;
				oldV = v;
				c++;
			}
			else {
				// draw the old path
				try {
					g.draw(mCoastSegment);
				}
				catch (Exception ex) {
				}

				c++;
				// start of new path
				oldU = (float) (((lonVal[c]) - winXOrigin) * winXScale);
				oldV = (float) ((latVal[c] - winYOrigin) * winYScale);
				oldU += mLeft;
				oldV += mTop;
				mCoastSegment.reset();
				mCoastSegment.moveTo(oldU, oldV);
				c++;
			}
		}
		g.setColor(Color.black);
	}

	public String getSupportDirAsUserDir(String subDir) throws Exception {
		try {
			String directory = NdEditFormulas.getSupportPath() + subDir + File.separator;
			File testFile = new File(directory);
			if (testFile == null || !testFile.isDirectory()) { throw new Exception(); }
			return directory;
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public String getSupportDirAsJOASupport(String subDir) throws Exception {
		try {
			String directory = NdEditFormulas.getSupportPath() + subDir + File.separator;
			File testFile = new File(directory);
			if (testFile == null || !testFile.isDirectory()) { throw new Exception(); }
			return directory;
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public void drawIsobaths(Graphics g) {
		// check to see if we can get one of the bathy support files
		String bathyDir = "";
		File testFile = null;
		if (Constants.SUPPORT_DIR != null) {
			try {
				String directory = Constants.SUPPORT_DIR + File.separator + "bathymetry" + File.separator + "c1000.antarc";
				testFile = new File(directory);
				bathyDir = Constants.SUPPORT_DIR + File.separator + "bathymetry" + File.separator;
			}
			catch (Exception e) {
				try {
					bathyDir = getSupportDirAsUserDir("bathymetry");
				}
				catch (Exception ex) {
					try {
						bathyDir = getSupportDirAsJOASupport("JOA_Support");
					}
					catch (Exception exx) {
						return;
					}
				}
			}
		}
		else {
			try {
				bathyDir = getSupportDirAsUserDir("bathymetry");
				testFile = new File(bathyDir + File.separator + "c1000.antarc");
			}
			catch (Exception ex) {
				try {
					bathyDir = getSupportDirAsJOASupport("JOA_Support");
					testFile = new File(bathyDir + File.separator + "c1000.antarc");
				}
				catch (Exception exx) {
					return;
				}
			}
		}

		if (testFile == null)
			return;

		// plot the isobaths
		double testLeftLon = this.getMinXVal();
		double testRightLon = this.getMaxXVal();
		double testMinLat = this.getMinYVal();
		double testMaxLat = this.getMaxYVal();

		boolean lonCor = false;
		if (testLeftLon > -180 && testRightLon < 180)
			// 0 degree centered do nothing
			lonCor = false;
		else if (testLeftLon > 0 && testRightLon > 180)
			// 180 centered--convert the basin ranges
			lonCor = true;
		else if (testLeftLon < 0 && testRightLon > 180)
			// 180 centered--convert the basin ranges
			lonCor = true;

		// found out the number of files that are going to be read
		boolean crossesDateLine = false;
		BathyOverlaySpec spec = (BathyOverlaySpec) mOverlaySpec;

		for (int i = 0; i < spec.getNumIsobaths(); i++) {
			// built in or custom?
			if (spec.isCustom(i)) {
				try {
					File mFile = new File(spec.getPath(i));
					System.out.println(i + " " + spec.getColor(i).getRGB());
					PlotIsobath(g, mFile.getParent() + File.separator, mFile.getName(), spec.getColor(i), crossesDateLine);
				}
				catch (IOException ex) {
					ex.printStackTrace();
					;
				}
			}
			else {
				// get a value for the specified isobath
				double isoVal = spec.getValue(i);
				int index = (int) (isoVal / 500.0) - 1;

				// built in isobath
				for (int b = 0; b < 8; b++) {
					// does this isobath intersect the map area?
					if (Constants.ISOINBASIN[b][index]) {
						double leftLon = Constants.BASINLIMITS[b * 12 + index][2];
						double rightLon = Constants.BASINLIMITS[b * 12 + index][3];
						double minLat = Constants.BASINLIMITS[b * 12 + index][0];
						double maxLat = Constants.BASINLIMITS[b * 12 + index][1];

						if (lonCor) {
							// correct the basin longitudes
							leftLon = (double) ((leftLon + 360) % 360.0);
							rightLon = (double) ((rightLon + 360) % 360.0);
						}

						// do the map region and the basin intersect?
						if (((leftLon <= testRightLon && leftLon >= testLeftLon) || (rightLon <= testRightLon && rightLon >= testLeftLon))
						    || ((minLat <= testMaxLat && minLat >= testMinLat) || (maxLat <= testMaxLat && maxLat >= testMinLat))) {
							String filename = new String(Constants.cArray[index] + Constants.bArray[b]);
							try {
								PlotIsobath(g, bathyDir, filename, spec.getColor(index), crossesDateLine);
							}
							catch (IOException ex) {
								ex.printStackTrace();
							}
						}
						else {
							// try the other way arround
							if (((testLeftLon <= rightLon && testLeftLon >= leftLon) || (testRightLon <= rightLon && testRightLon >= leftLon))
							    || ((testMaxLat <= maxLat && testMinLat >= minLat) || (testMaxLat <= maxLat && testMinLat >= minLat))) {
								String filename = new String(Constants.cArray[index] + Constants.bArray[b]);
								try {
									PlotIsobath(g, bathyDir, filename, spec.getColor(index), crossesDateLine);
								}
								catch (IOException ex) {
									ex.printStackTrace();
									;
								}
							}
						}
					}
				}
			}
		}
	}

	public void PlotIsobath(Graphics ig, String dir, String filename, Color isoColor, boolean crossesDateLine)
	    throws IOException {
		Graphics2D g = (Graphics2D) ig;
		File bathyFile = null;
		FileInputStream in1 = null;
		FileInputStream in2 = null;
		FileInputStream in = null;
		double[] bLatVal;
		double[] bLonVal;
		int cpt = 0;
		int cpt_save = 0;
		int cut_count = 0;

		// read and plot
		// try {
		// check to see if it's in the cache
		CachedIsobath cachedIsobath = (CachedIsobath) mIsobathCache.get(filename);
		if (cachedIsobath == null) {
			try {
				// String directory = NdEditFormulas.getSupportPath() + "bathymetry" +
				// File.separator + filename;
				bathyFile = new File(dir + filename);
				in1 = new FileInputStream(bathyFile);
			}
			catch (Exception ex) {
			}

			// try {
			// try to get it from the JOA support directory
			// String directory = NdEditFormulas.getSupportPath() + "JOA_Support" +
			// File.separator + filename;
			// bathyFile = new File(directory);
			// in2 = new FileInputStream(bathyFile);
			// }
			// catch (Exception ex) {}

			if (in1 == null) {
				if (in2 == null)
					return;
				else
					in = in2;
			}
			else
				in = in1;

			// got the file, now parse it and plot it
			BufferedInputStream bis = new BufferedInputStream(in, 1000000);
			DataInputStream inData = new DataInputStream(bis);

			double testLeftLon = this.getMinXVal();
			double testRightLon = this.getMaxXVal();
			double testMinLat = this.getMinYVal();
			double testMaxLat = this.getMaxYVal();

			boolean lonCor = false;
			if (testLeftLon > -180 && testRightLon < 180)
				// 0 degree centered do nothing
				lonCor = false;
			else if (testLeftLon > 0 && testRightLon > 180)
				// 180 centered--convert the basin ranges
				lonCor = true;
			else if (testLeftLon < 0 && testRightLon > 180)
				// 180 centered--convert the basin ranges
				lonCor = true;

			// store values in array
			while (true) {
				// get the number of entries
				int numEntries = inData.readShort();
				if (numEntries == -1)
					break;

				// get the lats and lons for this segment
				cpt_save = cpt;
				for (int i = 0; i < numEntries; i++) {
					templats[cpt] = (double) inData.readDouble();
					cpt++;
				}

				cpt = cpt_save;
				for (int i = 0; i < numEntries; i++) {
					// map -180 to 180 to 0 to 360
					double templon = (double) inData.readDouble();
					if (lonCor)
						templons[cpt] = (double) ((templon + 360) % 360.0);
					else
						templons[cpt] = templon;

					if (cpt > 0 && Math.abs(templons[cpt] - templons[cpt - 1]) > 50.0) {
						cut_count++;
					}
					cpt++;
				}

				templats[cpt] = Float.NaN;
				templons[cpt] = Float.NaN;
				cpt++;
			}

			int length = cpt;
			bLatVal = new double[cpt + cut_count];
			bLonVal = new double[cpt + cut_count];
			cpt = 0;
			for (int i = 0; i < length; i++) {
				if (i > 0 && Math.abs(templons[i] - templons[i - 1]) > 50.0) {
					bLatVal[cpt] = Float.NaN;
					bLonVal[cpt] = Float.NaN;
					cpt++;
				}
				bLatVal[cpt] = templats[i];
				bLonVal[cpt] = templons[i];
				cpt++;
			}

			// create the cached object and add to hash table
			CachedIsobath cb = new CachedIsobath(bLatVal, bLonVal);
			mIsobathCache.put(filename, cb);
		}
		else {
			bLatVal = cachedIsobath.getLats();
			bLonVal = cachedIsobath.getLons();
		}

		BasicStroke lw = new BasicStroke(1);
		g.setStroke(lw);
		g.setColor(isoColor);
		double u, u2, startU = 0.0f;
		double oldU = ((bLonVal[0]) - winXOrigin) * winXScale;
		double v, v2, startV = 0.0f;
		double oldV = (bLatVal[0] - winYOrigin) * winYScale;
		oldU += mLeft;
		oldV += mTop;
		int c = 1;
		int arrayLen = bLatVal.length;
		while (c < arrayLen - 1) {
			if (!Double.isNaN(bLonVal[c])) {
				u = ((bLonVal[c]) - winXOrigin) * winXScale;
				v = (bLatVal[c] - winYOrigin) * winYScale;
				u += mLeft;
				v += mTop;
				if (Math.abs(oldU - u) < 100)
					g.drawLine((int) oldU, (int) oldV, (int) u, (int) v);
				oldU = u;
				oldV = v;
				c++;
			}
			else {
				c++;
				oldU = ((bLonVal[c]) - winXOrigin) * winXScale;
				oldV = (bLatVal[c] - winYOrigin) * winYScale;
				oldU += mLeft;
				oldV += mTop;
				c++;
			}
		}
		// }
		// catch (IOException ex) {
		// ex.printStackTrace();
		// throw ex;
		// }
	}

	public OverlaySetupPanel getOverlaySetupPanel() {
		return new latLonOverlaySetup();
	}

	public class latLonOverlaySetup extends OverlaySetupPanel implements ActionListener, DialogClient {
		public latLonOverlaySetup() {
			init();
		}

		public void init() {
			ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
			this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));// ColumnLayout(Orientation.LEFT,
																														// Orientation.CENTER,
																														// 0));
			/*
			 * JButton mCoast = new JButton(b.getString("kCoastlineDot"));
			 * mCoast.setMargin(new Insets(0, 0, 0, 0)); mCoast.setFont(new
			 * java.awt.Font("SansSerif", Font.PLAIN, 10));
			 * mCoast.setActionCommand("coast"); mCoast.addActionListener(this);
			 * this.add(mCoast);
			 */
			JButton bathy = new JButton(b.getString("kMapSettings"));
			bathy.setToolTipText("Click to configure coastline and bathymetry overlay");
			bathy.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 10));
			bathy.setActionCommand("bathy");
			bathy.addActionListener(this);
			this.add(bathy, "Center");
			this.add(new NPixelBorder(bathy, 8, 0, 0, 0));
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kOverlay"));
			tb.setTitleFont(new Font("SansSerif", Font.PLAIN, 10));
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("bathy")) {
				// Open the bathy dialog
				ConfigBathyOverlay bathyDialog = new ConfigBathyOverlay(((NdEdit) mViewManager.getParent()).getFrame(),
				    (BathyOverlaySpec) mOverlaySpec, custCoastPath, this);
				bathyDialog.setSize(460, 450);
				// bathyDialog.pack();
				bathyDialog.setVisible(true);
			}

		}

		// OK Button
		public void dialogDismissed(JDialog d) {
			selMinLat = (double) getSelMinYVal();
			selMaxLat = (double) getSelMaxYVal();
			selMinLon = (double) getSelMinXVal();
			selMaxLon = (double) getSelMaxXVal();
			mOverlaySpec = ((ConfigBathyOverlay) d).getBathySpec();

			boolean changePlot = false;
			if (((ConfigBathyOverlay) d).isCoastWeightChanged() || ((ConfigBathyOverlay) d).isCoastColorChanged())
				changePlot = true;

			if (((ConfigBathyOverlay) d).isBathyNeeded()) {
				mPlotBathy = true;
				// redraw screen
				changePlot = true;

			}
			else {
				if (mPlotBathy) {
					// redraw screen
					mPlotBathy = false;
					((BathyOverlaySpec) mOverlaySpec).setNumIsobaths(0);
					changePlot = true;
				}
			}

			if (((ConfigBathyOverlay) d).isCustomCoastNeeded()) {
				String tcustCoastPath = ((ConfigBathyOverlay) d).getCustomCoastPath();
				if (custCoastPath == null || (custCoastPath != null && !custCoastPath.equals(tcustCoastPath))) {
					custCoastPath = new String(tcustCoastPath);
					latVal = null;
					// redraw screen
					changePlot = true;
				}
			}
			else {
				if (custCoastPath != null) {
					custCoastPath = null;
					latVal = null;
					// redraw screen
					changePlot = true;
				}
			}

			if (changePlot) {
				reset(true);
				mViewManager.invalidateAllViews();
			}
		}

		// Cancel button
		public void dialogCancelled(JDialog d) {
		}

		// something other than the OK button
		public void dialogDismissedTwo(JDialog d) {
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApply(JDialog d) {
		}

		// Apply button, OK w/o dismissing the dialog
		public void dialogApplyTwo(Object d) {
		}
	}

	public int getLegendInset() {
		if (mPlotBathy)
			return 60;
		else
			return 0;
	}

	public void doZoomIn(Rectangle rbRect) {
		int x = rbRect.x - mLeft - 5;
		int y = rbRect.y - mTop - 5;

		// zoom in
		double xInc = getXZoomIncrement();
		double yInc = getYZoomIncrement();

		// get coordinates of new view center
		// correct the x value if necessary
		double xx = correctX((double) x);

		// correct the Y coordinate if necessary
		double yy = correctY((double) y);

		double newXCtr = (xx / winXScale) + winXOrigin;
		double newYCtr = (yy / winYScale) + winYOrigin;

		// compute the deltas for current range
		double xDelta = Math.abs(getMaxXVal() - getMinXVal()) / 2.0f;
		double yDelta = Math.abs(getMaxYVal() - getMinYVal()) / 2.0f;

		// compute the aspect ratio of the few
		double aspect = xDelta / yDelta;
		// if (aspect > 1.0)
		// xInc *= aspect;
		// else
		// yInc *= aspect;

		double newXMin = newXCtr < 0 ? newXCtr - xDelta + xInc : newXCtr - xDelta + xInc;
		double newXMax = newXCtr < 0 ? newXCtr + xDelta - xInc : newXCtr - xDelta - xInc;
		double newYMin = newYCtr < 0 ? newYCtr - yDelta + yInc : newYCtr - yDelta + yInc;
		double newYMax = newYCtr < 0 ? newYCtr + yDelta - yInc : newYCtr + yDelta - yInc;

		double[] oldYs = { getMinYVal(), getMaxYVal() };
		double[] newYs = { newYMin, newYMax };
		double[] oldXs = { getMinXVal(), getMaxXVal() };
		double[] newXs = { newXMin, newXMax };
		this.zoomDomain(oldYs, newYs, oldXs, newXs);
	}
}
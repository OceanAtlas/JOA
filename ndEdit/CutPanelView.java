/*
 * $Id: CutPanelView.java,v 1.24 2005/08/22 21:25:15 oz Exp $
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

import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.net.*;
import gov.noaa.pmel.swing.*;
import ndEdit.filters.*;
import ndEdit.filters.semaphore.*;
import java.util.*;

/**
 * 
 * 
 * @author Chris Windsor
 * @version 1.0 01/13/00
 * 
 * @note A view-controller with scoping (handlebars).
 */
public abstract class CutPanelView extends CutPanel implements BaseView, ActionListener, CIMFacade {

	private boolean isVisible;
	private JToggleButton toolbarButton;
	public String viewName; // string name for the view.
	private String toolTipText;
	private boolean TRACE = false;
	protected boolean plotAxes = true;
	protected int mTop;
	protected int mLeft;
	protected int mRight;
	protected int mBottom;
	protected double winXScale;
	protected double winXOrigin;
	protected double winYScale;
	protected double winYOrigin;
	protected double xInc;
	protected double yInc;
	protected int mWidth;
	protected int mHeight;
	// See the Constants class for the list of possible enumerations
	private int viewEnum;
	protected ViewManager vm;
	protected double mWinYPlotMin;
	protected double mWinYPlotMax;
	protected double mWinXPlotMin;
	protected double mWinXPlotMax;
	private ChangeableInfoMgr cim;
	protected PointerCollectionGroup mCurrPC = null;
	private FilterConstraintsManager filterConstraintsMgr;
	protected FilteredDataManager fdm;
	protected boolean mIgnoreFilter = false;

	private static final String arrowcursorgif = "gifs/arrowCurs.gif";
	private static final String crosscursorgif = "gifs/cross.gif";
	private static final String sectioncursorgif = "gifs/sectionCurs.gif";
	private static final String zoomoutcursorgif = "gifs/zoomOutCurs.gif";
	private static final String zoomincursorgif = "gifs/zoomInCurs.gif";
	private static final String crosscursorgifX = "gifs/crossX.gif";
	private static final String sectioncursorgifX = "gifs/sectionCursX.gif";
	private static final String zoomoutcursorgifX = "gifs/zoomOutCursX.gif";
	private static final String zoomincursorgifX = "gifs/zoomInCursX.gif";
	protected Cursor arrow;
	protected Cursor cross;
	protected Cursor section;
	protected Cursor zoomIn;
	protected Cursor zoomOut;
	protected int toolMode = Constants.ZOOM_MODE;
	protected boolean axesInitialized = false;
	protected boolean firstPlotDone = false;

	// constructor
	//
	public CutPanelView(int viewEnum, String viewName, Object parentObject) {
		super(parentObject);
		this.viewEnum = viewEnum;
		this.viewName = viewName;
		cim = new ChangeableInfoMgr();
		filterConstraintsMgr = new FilterConstraintsManager(this);
		SemaphoreFilter sfilter = new SemaphoreFilter(this);
		fdm = sfilter;

		// define the custom cursors
		Toolkit tk = this.getToolkit();

		// arrow
		// Image cursImage =
		// Toolkit.getDefaultToolkit().createImage(getClass().getResource(arrowcursorgif));
		// Point hotSpot = new Point(13, 10);
		// arrow = tk.createCustomCursor(cursImage, hotSpot, "ARROW_CURSOR");
		
		
		try {
			if (!Constants.ISMAC) {
				// open cross
				Image cursImage = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(crosscursorgif));
				
				Point hotSpot = new Point(15, 15);
				cross = tk.createCustomCursor(cursImage, hotSpot, "CROSS_CURSOR");

				// section
				Image cursImage2 = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(sectioncursorgif));
				hotSpot = new Point(15, 15);
				section = tk.createCustomCursor(cursImage2, hotSpot, "SECTION_CURSOR");

				// zoom in
				Image cursImage3 = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(zoomincursorgif));
				hotSpot = new Point(14, 14);
				zoomIn = tk.createCustomCursor(cursImage3, hotSpot, "ZOOMIN_CURSOR");

				// zoom out
				Image cursImage4 = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(zoomoutcursorgif));
				hotSpot = new Point(14, 14);
				zoomOut = tk.createCustomCursor(cursImage4, hotSpot, "ZOOMOUT_CURSOR");
			}
			else {
				// open cross
				Image cursImage = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(crosscursorgifX));
				Point hotSpot = new Point(8, 8);
				cross = tk.createCustomCursor(cursImage, hotSpot, "CROSS_CURSOR");

				// section
				Image cursImage2 = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(sectioncursorgifX));
				hotSpot = new Point(4, 8);
				section = tk.createCustomCursor(cursImage2, hotSpot, "SECTION_CURSOR");

				// zoom in
				Image cursImage3 = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(zoomincursorgifX));
				hotSpot = new Point(6, 6);
				zoomIn = tk.createCustomCursor(cursImage3, hotSpot, "ZOOMIN_CURSOR");

				// zoom out
				Image cursImage4 = Toolkit.getDefaultToolkit().createImage(
				    Class.forName("ndEdit.NdEdit").getResource(zoomoutcursorgifX));
				hotSpot = new Point(6, 6);
				zoomOut = tk.createCustomCursor(cursImage4, hotSpot, "ZOOMOUT_CURSOR");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
    	System.out.println("CutPanelView:ctor");
		}
	}

	public boolean isFirstDone() {
		return firstPlotDone;
	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		if (TRACE)
			System.out.println("CutPanelView actionPerformed: view: " + viewName);

		if (event.getSource() == closeButn) {
			JToggleButton jtb = (JToggleButton) event.getSource();
			jtb.setSelected(false);
			// System.out.println("Close!");
			vm.hideView(this);
			toolbarButton.setSelected(false);
		}
		else if (toolbarButton.isSelected()) {
			vm.showView(this);
		}
		else
			vm.hideView(this);
	}

	public void setAxesDisplay(boolean val) {
		plotAxes = val;
	}

	public Rectangle getLegalDragArea() {
		return new Rectangle(this.getMinX(), this.getMinY(), this.getMaxX() - this.getMinX() + 1, this.getMaxY()
		    - this.getMinY() + 1);
	}

	public Rectangle getXAxisArea() {
		return new Rectangle(this.getMinX(), this.getMinY() - 20, this.getMaxX() - this.getMinX(), 20);
	}

	public Rectangle getYAxisArea() {
		return new Rectangle(this.getMaxX(), this.getMinY(), 20, this.getMaxY() - this.getMinY());
	}

	public Rectangle getXAxisRangeArea() {
		return new Rectangle(this.getMinX(), this.getMaxY(), this.getMaxX() - this.getMinX(), this.getMaxY() + 20);
	}

	public Rectangle getYAxisRangeArea() {
		return new Rectangle(this.getMinX() - 20, this.getMinY(), this.getMinX(), this.getMaxY() - this.getMinY());
	}

	public boolean getDisplayAxes() {
		return plotAxes;
	}

	public boolean isInDragArea(Point p) {
		if (getLegalDragArea().contains(p)) {
			return true;
		}
		else {
			return false;
		}
	}

	public String isInAxesArea(Point p) {
		if (getXAxisArea().contains(p))
			return "X";
		else if (getYAxisArea().contains(p))
			return "Y";
		return null;
	}

	public String isInAxesRangeArea(Point p) {
		if (getXAxisRangeArea().contains(p)) {
			return "X";
		}
		else if (getYAxisRangeArea().contains(p)) { return "Y"; }
		return null;
	}

	// Cursors: arrow, cross, section, zoomIn, zoomOut;
	public void setCPCursor(Point p, boolean shiftDown) {
		if (isInDragArea(p)) {
			if (toolMode == Constants.ZOOM_MODE) {
				if (shiftDown)
					setCursor(zoomOut);
				else
					setCursor(zoomIn);
			}
			else if (toolMode == Constants.SELECT_MODE) {
				setCursor(cross);
			}
			else if (toolMode == Constants.SECTION_MODE) {
				setCursor(section);
			}
			else {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}

			// display the cursor loc in real values
			displayCursorValues(p);
		}
		else if (isInDragArea(p) && Constants.ISMAC) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		else
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void displayCursorValues(Point p) {
		// overridden by the individual views
	}

	public void setViewManager(ViewManager vm) {
		this.vm = vm;
	}

	public ViewManager getViewManager() {
		return vm;
	}

	public void setToolbarButton(JToggleButton tb) {
		this.toolbarButton = tb;
		tb.addActionListener(this);
	}

	public JToggleButton getToolbarButton() {
		return toolbarButton;
	}

	// --------------------------------------------------------------------------
	// 
	public int getViewEnum() {
		return viewEnum;
	}

	public abstract void configureToolBelt();

	public abstract void setHandle(Object src, String prop, Object val, boolean setN);

	public abstract void setHandleVal(String prop, Object val);

	public abstract void setHandleVals(String prop, double min, double max);

	public abstract void setFieldValues(String prop, Object val);

	public String getName() {
		return viewName;
	}

	public int getMinX() {
		if (plotAxes)
			return mLeft + 5;
		else
			return 5;
	}

	public int getMinY() {
		if (plotAxes)
			return mTop + 5;
		else
			return 5;
	}

	public int getMaxX() {
		if (plotAxes)
			return this.getSize().width - mRight - 5 - getLegendInset();
		else
			return this.getSize().width - 5;
	}

	public int getMaxY() {
		if (plotAxes)
			return this.getSize().height - mBottom - 45;
		else
			return this.getSize().height - 35;
	}

	public double getMinXVal() {
		return mWinXPlotMin;
	}

	public double getMinYVal() {
		return mWinYPlotMin;
	}

	public double getMaxXVal() {
		return mWinXPlotMax;
	}

	public double getMaxYVal() {
		return mWinYPlotMax;
	}

	public void setMinXVal(double val) {
		mWinXPlotMin = val;
	}

	public void setMinYVal(double val) {
		mWinYPlotMin = val;
	}

	public void setMaxXVal(double val) {
		mWinXPlotMax = val;
	}

	public void setMaxYVal(double val) {
		mWinYPlotMax = val;
	}

	public void scaleXAxis() {
		// x axis
		int scaleWidth = mWidth;
		scaleWidth -= (mLeft + mRight);
		winXScale = scaleWidth / (this.getMaxXVal() - this.getMinXVal());
		winXOrigin = this.getMinXVal();
	}

	public void scaleYAxis() {
		// x axis
		int scaleHeight = mHeight;
		scaleHeight -= (mBottom + mTop);
		winYScale = scaleHeight / (this.getMinYVal() - this.getMaxYVal());
		winYOrigin = this.getMaxYVal();
	}

	public void setClip(Graphics g) {
		if (g == null)
			return;
		int scaleHeight = mHeight;
		scaleHeight -= (mBottom + mTop);
		int scaleWidth = mWidth;
		scaleWidth -= (mLeft + mRight);
		g.setClip(mLeft, mTop, scaleWidth, scaleHeight);
	}

	public void handleDragEnded(DragHandle dh) {
		// notfiy the listeners that handle has ended a drag
		dh.resetStyle();
		dh.setOutOfRange(false);
		dh.broadcast(true);
	}

	public PointerCollectionGroup getPointerCollection() {
		return mCurrPC;
	}

	public FilterConstraintsManager getFilterConstraintsManager() {
		return filterConstraintsMgr;
	}

	/**
	 * Resets the current pointer collection, notifying the filters, and
	 * optionally resetting the current set of filtering constraints (extents).
	 * 
	 * @param pc
	 *          new pointer collection to be displayed and presented for filtering
	 * @param resetFilteringConstraints
	 *          boolean flag, if true, then set all filtering constaints (extents)
	 *          to match the extremes of the data in the pointer collection
	 * 
	 */
	public void setPointerCollection(PointerCollectionGroup pcg, boolean resetFilteringConstraints) {
		if (pcg == null)
			return;
		mCurrPC = pcg;
		fdm.newPointerCollection(mCurrPC);
		if (resetFilteringConstraints) {
			filterConstraintsMgr
			    .resetFilterConstraints(new FilterConstraints(NdEditFormulas.GetPrettyRange(mCurrPC.getMinMaxLat(), -90.0, 90.0),
			        NdEditFormulas.GetPrettyRange(mCurrPC.getMinMaxLon(), -180.0, 180.0), NdEditFormulas.GetPrettyRange(mCurrPC
			            .getMinMaxDepth()), NdEditFormulas.GetPrettyRange(mCurrPC.getMinMaxTime()), null, null));
		}
	}

	public void pushChangeableInfo(ChangeableInfo ci) {
		cim.pushChangeableInfo(ci);
	}

	/**
	 * Pushes ChangeableInfo through to the ChangeableInfoManager for
	 * dissemination to all interested objects; this method provides a bridge to
	 * the ChangeableInfoManager for lower level objects scattered throughout the
	 * bean that lack access to the ChangeableInfoManager object.
	 * 
	 * @param id
	 *          string identifier describing information that's changing. This
	 *          identifier will be used as a tag by other objects interested in
	 *          this type of change. For instance, "LongitudeStart" says the start
	 *          longitude has changed.
	 * @param oldValue
	 *          object containing old value, if available
	 * @param newValue
	 *          object containing new value
	 * @param undoable
	 *          boolean flag indicating whether this change should be kept by the
	 *          back/forward manager for "undoing"
	 */
	public void pushChangeableInfo(String id, Object oldValue, Object newValue, boolean undoable) {
		cim.pushChangeableInfo(new ChangeableInfo(id, oldValue, newValue, undoable));
	}

	/**
	 * Adds a changeableInfoListener object to the ChangeableInfoManager's
	 * listener list; this method provides a bridge to the ChangeableInfoManager
	 * for lower level objects scattered throughout the bean that lack access to
	 * the ChangeableInfoManager object.
	 * 
	 * @param obj
	 *          object interested in listening for ChangeableInfo
	 */
	public void addChangeableInfoListener(ChangeableInfoListener obj) {
		cim.addChangeableInfoListener(obj);
	}

	public void setIgnoreFilter(boolean ignore) {
		mIgnoreFilter = ignore;
	}

	public void setToolMode(int mode) {
		toolMode = mode;

		// set the appropriate rubberband shape
		if (toolMode == Constants.ZOOM_MODE || toolMode == Constants.SELECT_MODE)
			setSelectionAsRectangle();
		else if (toolMode == Constants.SECTION_MODE)
			setSelectionAsLine();
		else if (toolMode == Constants.POLYGON_MODE)
			setSelectionAsPolygon();
	}

	// override me
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
		double xDelta = Math.abs(getMaxXVal() - getMinXVal()) / 2.0;
		double yDelta = Math.abs(getMaxYVal() - getMinYVal()) / 2.0;

		// compute the aspect ratio of the few
		double aspect = xDelta / yDelta;
		// if (aspect > 1.0)
		// xInc *= aspect;
		// else
		// yInc *= aspect;

		double newXMin = newXCtr < 0 ? newXCtr - xDelta + xInc : newXCtr - xDelta + xInc;
		double newXMax = newXCtr < 0 ? newXCtr + xDelta - xInc : newXCtr + xDelta - xInc;
		double newYMin = newYCtr < 0 ? newYCtr - yDelta + yInc : newYCtr - yDelta + yInc;
		double newYMax = newYCtr < 0 ? newYCtr + yDelta - yInc : newYCtr + yDelta - yInc;

		double[] oldYs = { getMinYVal(), getMaxYVal() };
		double[] newYs = { newYMin, newYMax };
		double[] oldXs = { getMinXVal(), getMaxXVal() };
		double[] newXs = { newXMin, newXMax };
		this.zoomDomain(oldYs, newYs, oldXs, newXs);
	}

	public void rubberbandEnded(Rubberband rb, boolean shiftDown) {
		// get the limits and zoom the plot
		if (mCurrPC == null)
			return;
		Rectangle rbRect = rb.getBounds();
		if (rbRect.width == 0 || rbRect.height == 0) {
			// click selection
			if (toolMode == Constants.SELECT_MODE) {
				// find any matches at the mouseclick location
				int x = rbRect.x - mLeft - 5;
				int y = rbRect.y - mTop - 5;

				// construct a search region;
				int[] xpoints = { x - 1, x + 1, x + 1, x - 1, x - 1 };
				int[] ypoints = { y - 1, y - 1, y + 1, y + 1, y - 1 };
				Polygon sr = new Polygon(xpoints, ypoints, 5);

				if (!shiftDown)
					mCurrPC.unselectAll();

				mCurrPC.setBatchModeOn();

				byte[] currFilterResults = fdm.getResults();
				double[] yArray1 = getYArray1();
				double[] xArray1 = getXArray1();
				double[] yArray2 = getYArray2();
				double[] xArray2 = getXArray2();

				for (int i = 0; i < mCurrPC.getSize(); i++) {
					if (!mIgnoreFilter && (mCurrPC.isDeleted(i) || currFilterResults[i] != 4) || !mCurrPC.isSelectedLayer(i))
						continue;

					// search for matches
					double yi = yArray1[i];
					double xi = xArray1[i];
					double xi2 = Float.NaN;
					double yi2 = Float.NaN;
					double x2 = Float.NaN;
					double y2 = Float.NaN;

					// correct the X value if necessary
					xi = correctX(xi);

					double x1 = (xi - winXOrigin) * winXScale;
					double y1 = (yi - winYOrigin) * winYScale;

					// correct the Y coordinate if necessary
					y1 = correctY(y1);

					if (!isYAxisScaler()) {
						yi2 = yArray2[i];
						y2 = (yi2 - winYOrigin) * winYScale;
						y2 = correctY(y2);
					}
					else
						y2 = y1;

					if (!isXAxisScaler()) {
						xi2 = xArray2[i];
						xi2 = correctX(xi2);
						x2 = (xi2 - winXOrigin) * winXScale;
					}
					else
						x2 = x1;

					if (sr.contains(new Point((int) x1, (int) y1)) || sr.contains(new Point((int) x2, (int) y2))) {
						mCurrPC.select(i);
					}
				}
				mViewManager.invalidateAllViews();
				mCurrPC.setBatchModeOff();
			}
			else if (toolMode == Constants.ZOOM_MODE) {
				if (!shiftDown) {
					doZoomIn(rbRect);
				}
				else {
					// zoom out
					double xInc = getXZoomIncrement();
					double yInc = getYZoomIncrement();
					double[] oldYs = { getMaxYVal(), getMinYVal() };
					double[] newYs = { getMinYVal() < 0 ? getMinYVal() - yInc : getMinYVal() + yInc,
					    getMaxYVal() < 0 ? getMaxYVal() - yInc : getMaxYVal() + yInc };
					double[] oldXs = { getMinXVal(), getMaxXVal() };
					double[] newXs = { getMinXVal() < 0 ? getMinXVal() - xInc : getMinXVal() + xInc,
					    getMaxXVal() < 0 ? getMaxXVal() - xInc : getMaxXVal() + xInc };
					this.zoomDomain(oldYs, newYs, oldXs, newXs);
				}
				mViewManager.invalidateAllViews();
			}
		}
		else {
			// region selected
			int x = rbRect.x - mLeft - 5;
			int y = rbRect.y - mTop - 5;
			int x2 = x + rbRect.width;
			int y2 = y + rbRect.height;
			Point p1 = new Point();
			Point p2 = new Point();

			if (toolMode == Constants.SELECT_MODE) {
				if (!shiftDown)
					mCurrPC.unselectAll();

				// construct a search region;
				int[] xpoints = { x, x2, x2, x, x };
				int[] ypoints = { y, y, y2, y2, y };
				Polygon sr = new Polygon(xpoints, ypoints, 5);

				// search for matches
				byte[] currFilterResults = fdm.getResults();
				double[] yArray1 = getYArray1();
				double[] xArray1 = getXArray1();
				double[] yArray2 = getYArray2();
				double[] xArray2 = getXArray2();

				mCurrPC.setBatchModeOn();
				for (int i = 0; i < mCurrPC.getSize(); i++) {
					if (!mIgnoreFilter && (mCurrPC.isDeleted(i) || currFilterResults[i] != 4 || !mCurrPC.isSelectedLayer(i)))
						continue;

					double yi = yArray1[i];
					double xi = xArray1[i];
					double xi2 = Float.NaN;
					double yi2 = Float.NaN;
					double xx2 = Float.NaN;
					double yy2 = Float.NaN;

					// correct the X value if necessary
					xi = correctX(xi);

					double xx1 = (xi - winXOrigin) * winXScale;
					double yy1 = (yi - winYOrigin) * winYScale;

					// correct the Y coordinate if necessary
					yy1 = correctY(yy1);

					if (!isYAxisScaler()) {
						yi2 = yArray2[i];
						yy2 = (yi2 - winYOrigin) * winYScale;
						yy2 = correctY(yy2);
					}
					else
						yy2 = yy1;

					if (!isXAxisScaler()) {
						xi2 = xArray2[i];
						xi2 = correctX(xi2);
						xx2 = (xi2 - winXOrigin) * winXScale;
					}
					else
						xx2 = xx1;

					p1.setLocation((int) xx1, (int) yy1);
					p2.setLocation((int) xx2, (int) yy2);

					if (sr.contains(p1) || sr.contains(p2)) {
						mCurrPC.select(i);
					}
				}
				mViewManager.invalidateAllViews();
				mCurrPC.setBatchModeOff();
			}
			else if (toolMode == Constants.ZOOM_MODE) {
				// correct the x value if necessary
				double xx = correctX((double) x);
				double xx2 = correctX((double) x2);

				// correct the Y coordinate if necessary
				double yy = correctY((double) y);
				double yy2 = correctY((double) y2);

				double newX = (xx / winXScale) + winXOrigin;
				double newX2 = (xx2 / winXScale) + winXOrigin;
				double newY = (yy / winYScale) + winYOrigin;
				double newY2 = (yy2 / winYScale) + winYOrigin;

				double[] oldYs = { getMaxYVal(), getMinYVal() };
				if (this.toString().indexOf("Depth") >= 0) {
					double temp = newY2;
					newY2 = newY;
					newY = temp;
				}
				double[] newYs = { newY2, newY };

				double[] oldXs = { getMinXVal(), getMaxXVal() };
				double[] newXs = { newX, newX2 };
				this.zoomDomain(oldYs, newYs, oldXs, newXs);
				mViewManager.invalidateAllViews();
			}
		}
	}

	public void processSectionSpline(boolean shiftDown) {
		mCurrPC.setBatchModeOn();

		if (!shiftDown)
			mCurrPC.unselectAll();

		// loop on spline pts
		for (int s = 0; s < mSplinePoints.size() - 2; s++) {
			Point p1 = (Point) mSplinePoints.elementAt(s);
			Point p2 = (Point) mSplinePoints.elementAt(s + 1);

			int x1 = p1.x - mLeft - 5;
			int y1 = p1.y - mTop - 5;
			int x2 = p2.x - mLeft - 5;
			int y2 = p2.y - mTop - 5;

			// construct the search polygon
			double dx = x1 - x2;
			double dy = y1 - y2;
			int width1 = ComputeSectionPixelWidth(p1);
			int width2 = ComputeSectionPixelWidth(p2);

			if (dx == 0 && dy == 0)
				return;
			double dist = (double) Math.pow(dx * dx + dy * dy, 0.5f);

			double p1x = x1 + (double) width1 * (-dy / dist);
			double p1y = y1 + (double) width1 * (dx / dist);

			double p2x = x1 - (double) width1 * (-dy / dist);
			double p2y = y1 - (double) width1 * (dx / dist);

			double p3x = x2 + (double) width2 * (-dy / dist);
			double p3y = y2 + (double) width2 * (dx / dist);

			double p4x = x2 - (double) width2 * (-dy / dist);
			double p4y = y2 - (double) width2 * (dx / dist);

			Polygon srcArea = new Polygon();
			srcArea.addPoint((int) p2x, (int) p2y);
			srcArea.addPoint((int) p1x, (int) p1y);
			srcArea.addPoint((int) p3x, (int) p3y);
			srcArea.addPoint((int) p4x, (int) p4y);

			// search for matches
			byte[] currFilterResults = fdm.getResults();
			double[] yArray1 = getYArray1();
			double[] xArray1 = getXArray1();
			double[] yArray2 = getYArray2();
			double[] xArray2 = getXArray2();
			mCurrPC.setBatchModeOn();
			for (int i = 0; i < mCurrPC.getSize(); i++) {
				if (!mIgnoreFilter && (mCurrPC.isDeleted(i) || currFilterResults[i] != 4 || !mCurrPC.isSelectedLayer(i)))
					continue;
				double yi = yArray1[i];
				double xi = xArray1[i];
				double xi2 = Float.NaN;
				double yi2 = Float.NaN;
				double xx2 = Float.NaN;
				double yy2 = Float.NaN;

				// correct the X value if necessary
				xi = correctX(xi);

				double xx1 = (xi - winXOrigin) * winXScale;
				double yy1 = (yi - winYOrigin) * winYScale;

				// correct the Y coordinate if necessary
				yy1 = correctY(yy1);

				if (!isYAxisScaler()) {
					yi2 = yArray2[i];
					yy2 = (yi2 - winYOrigin) * winYScale;
					;
					yy2 = correctY(yy2);
				}
				else
					yy2 = yy1;

				if (!isXAxisScaler()) {
					xi2 = xArray2[i];
					xi2 = correctX(xi2);
					xx2 = (xi2 - winXOrigin) * winXScale;
				}
				else
					xx2 = xx1;

				if (srcArea.contains(new Point((int) xx1, (int) yy1)) || srcArea.contains(new Point((int) xx2, (int) yy2))) {
					mCurrPC.select(i);
				}
			}
		} // for splines

		mSplinePoints.removeAllElements();
		mViewManager.invalidateAllViews();
		mCurrPC.setBatchModeOff();
	}

	public void processPolygonSpline(boolean shiftDown) {
		mCurrPC.setBatchModeOn();
		if (!shiftDown)
			mCurrPC.unselectAll();

		// loop on spline pts to construct a Polygon
		Polygon srcArea = new Polygon();
		for (int s = 0; s < mSplinePoints.size(); s++) {
			Point p = (Point) mSplinePoints.elementAt(s);
			int x = p.x - mLeft - 5;
			int y = p.y - mTop - 5;

			srcArea.addPoint(x, y);
		}

		mSplinePoints.removeAllElements();

		// search for matches
		byte[] currFilterResults = fdm.getResults();
		double[] yArray1 = getYArray1();
		double[] xArray1 = getXArray1();
		double[] yArray2 = getYArray2();
		double[] xArray2 = getXArray2();
		Point p1 = new Point();
		Point p2 = new Point();

		for (int i = 0; i < mCurrPC.getSize(); i++) {
			if (!mIgnoreFilter && (mCurrPC.isDeleted(i) || currFilterResults[i] != 4 || !mCurrPC.isSelectedLayer(i)))
				continue;
			double yi = yArray1[i];
			double xi = xArray1[i];
			double xi2 = Float.NaN;
			double yi2 = Float.NaN;
			double xx2 = Float.NaN;
			double yy2 = Float.NaN;

			// correct the X value if necessary
			xi = correctX(xi);

			double xx1 = (xi - winXOrigin) * winXScale;
			double yy1 = (yi - winYOrigin) * winYScale;

			// correct the Y coordinate if necessary
			yy1 = correctY(yy1);

			if (!isYAxisScaler()) {
				yi2 = yArray2[i];
				yy2 = (yi2 - winYOrigin) * winYScale;
				;
				yy2 = correctY(yy2);
			}
			else
				yy2 = yy1;

			if (!isXAxisScaler()) {
				xi2 = xArray2[i];
				xi2 = correctX(xi2);
				xx2 = (xi2 - winXOrigin) * winXScale;
			}
			else
				xx2 = xx1;

			p1.setLocation((int) xx1, (int) yy1);
			p2.setLocation((int) xx2, (int) yy2);

			if (srcArea.contains(p1) || srcArea.contains(p2)) {
				mCurrPC.select(i);
			}
		}
		mViewManager.invalidateAllViews();
		mCurrPC.setBatchModeOff();
	}

	public int ComputeSectionPixelWidth(Point p) {
		return 1;
	}

	// these are overriden by the actual view classes
	public abstract void zoomDomain(double[] oldYs, double[] newYs, double[] oldXs, double[] newXs);

	public abstract double[] getXArray1();

	public abstract double[] getXArray2();

	public abstract double[] getYArray1();

	public abstract double[] getYArray2();

	public abstract boolean isXAxisScaler();

	public abstract boolean isYAxisScaler();

	public abstract double correctX(double lon);

	public abstract double correctY(double y);

	public abstract double getXZoomIncrement();

	public abstract double getYZoomIncrement();

	protected double correctLongitude(double lon) {
		if (lon > 180)
			return lon - Constants.LONGITUDE_CONV_FACTOR;
		return lon;
	}

	protected double unCorrectLongitude(double lon) {
		if (lon < 0)
			return lon + Constants.LONGITUDE_CONV_FACTOR;
		return lon;
	}

	public void reset(boolean initAxes) {
		mXAxisLeft = null;
		mXAxisCtr = null;
		mXAxisRight = null;
		mYAxisUpper = null;
		mYAxisCtr = null;
		mYAxisLower = null;
		mHilitedRegion = null;
		axesInitialized = false;// initAxes;
	}

	public void resetOverlay() {
		System.out.println("CutPanelView: resetOverlaySpec");
		mOverlaySpec = null;
	}

	public int getLegendInset() {
		return 0;
	}

	public void setHandleFocus(boolean focusState) {
		// set the focus for all the handles in this view
		if (mXAxisLeft == null)
			return;
		mXAxisLeft.setFocused(focusState);
		mXAxisCtr.setFocused(focusState);
		mXAxisRight.setFocused(focusState);
		mYAxisUpper.setFocused(focusState);
		mYAxisCtr.setFocused(focusState);
		mYAxisLower.setFocused(focusState);
	}
}

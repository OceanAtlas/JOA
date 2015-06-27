/*
 * $Id: MapPlotPanel.java,v 1.46 2005/09/23 14:51:24 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import gov.noaa.pmel.eps2.*;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.Range2D;
import gov.noaa.pmel.util.SoTRange;
import ucar.multiarray.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.print.*;
import gov.noaa.pmel.sgt.contour.*;
import gov.noaa.pmel.sgt.CartesianGraph;
import gov.noaa.pmel.sgt.ColorMap;
import gov.noaa.pmel.sgt.ContourLevelNotFoundException;
import gov.noaa.pmel.sgt.ContourLevels;
import gov.noaa.pmel.sgt.ContourLineAttribute;
import gov.noaa.pmel.sgt.GridAttribute;
import gov.noaa.pmel.sgt.GridCartesianRenderer;
import gov.noaa.pmel.sgt.IndexedColor;
import gov.noaa.pmel.sgt.IndexedColorMap;
import gov.noaa.pmel.sgt.Layer;
import gov.noaa.pmel.sgt.LinearTransform;

@SuppressWarnings("serial")
public class MapPlotPanel extends RubberbandPanel implements ObsChangedListener, DialogClient, ActionListener,
    MetadataChangedListener, LevelChangedListener, Printable {
	protected static int SELECTION_MODE = 1;
	protected static int ZOOM_MODE = 2;
	protected static int SECTION_MODE = 3;
	protected static int POLYGON_MODE = 4;
	protected static int CENTERING_MODE = 5;
	protected static double SMALL = 1.0e-4;
	protected Image mOffScreen = null;
	protected Image mCompositeOffScreen = null;
	protected Image mStnOffScreen = null;
	protected Rubberband rbRect;
	protected DialogClient mDialogClient = null;
	protected FileViewer mFileViewer = null;
	protected MapSpecification mMapSpec = null;
	protected int mLegendHeight;
	protected ObsMarker mObsMarker = null;
	protected JOAMapContainer mContents;
	protected JOAWindow mFrame = null;
	protected MapLegend mLegend = null;
	protected MapColorBarPanel mColorBarLegendPanel = null;
	protected int mCurrIsoSurfaceLevel;
	protected int mCurrContourIsoSurfaceLevel;
	protected boolean mIgnore;
	protected int mSelectionMode = MapPlotPanel.ZOOM_MODE;
	protected Vector<SectionStation> mFoundStns = new Vector<SectionStation>();
	protected SectionEditor mCurrSectionEditor = null;
	private JPopupMenu mPopupMenu = null;
	protected RubberbandPanel mThis = null;
	protected boolean mCompositeMap = false;
	public double r_major = 1.0;
	public Dbase mETOPODB = null;
	public NewColorBar mETOPOColorBar = null;
	public String[] oldEtopoFile = null;
	private float[][] savedLats = null;
	private float[][][] savedVals = null;
	private float[][] savedLons = null;
	// double[] coastlats = new double[130000];
	// double[] coastlons = new double[130000];
	double[] templats = new double[130000];
	double[] templons = new double[130000];
	double[] latVal = null;
	double[] lonVal = null;
	private boolean firstTime = true;
	private boolean busy = false;
	private ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	boolean mWindowIsLocked = false;
	protected GeneralPath mSelectionRect = new GeneralPath();
	private GeneralPath mSplinePath = new GeneralPath();
	BasicStroke lw1 = new BasicStroke(1);
	BasicStroke lw2 = new BasicStroke(2);
	Rectangle oldRect = new Rectangle();
	Vector<Point> mSplinePoints = new Vector<Point>();
	private boolean ptsHaveBeenCached = false;
	private int mHOffset = 0;
	private int mVOffset = 0;
	private double mHScale = 1.0;
	private double mVScale = 1.0;
	int ref = 0;
	Contour mCurrContours;
	Shape mCurrClipShape = null;
	private Projection mEckert4Projection = null;

	public MapPlotPanel(FileViewer fv, MapSpecification mapspec, ObsMarker obsMarker, JOAMapContainer contents,
	    JOAWindow frame, MapLegend legend, MapColorBarPanel cbPanel, int hoffset, int voffset, double hscale, double vscale) {
		// super(false);
		mFileViewer = fv;
		mMapSpec = mapspec;
		mObsMarker = obsMarker;
		mContents = contents;
		mFrame = frame;
		mLegend = legend;
		this.setBackground(mMapSpec.getBGColor());
		if (mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION
		    || mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION) {
			rbRect = new RubberbandRectangle(this);
		}
		else if (mMapSpec.getProjection() > JOAConstants.MILLERPROJECTION
		    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			rbRect = new StereographicRectangle(this);
		}
		else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
			rbRect = new StereographicRectangle(this); // todo
		}
		else if (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
			rbRect = new RubberbandRectangle(this); // todo
		}
		else {
			rbRect = new PolarCircle(this);
		}
		rbRect.setActive(true);
		mDialogClient = this;
		mColorBarLegendPanel = cbPanel;
		this.requestFocus();
		mHOffset = hoffset;
		mVOffset = voffset;
		mHScale = hscale;
		mVScale = vscale;
	}

	public void setRubberbandDisplayObject(Object obj, boolean concat) {
		// this routine expects a Rectangle Object
		mSelectionRect = (GeneralPath)obj;
		// oldRect.add(mSelectionRect.getBounds());
		// oldRect.setBounds(oldRect.x - 4, oldRect.y - 4, oldRect.width + 8,
		// oldRect.height + 8);
		// paintImmediately(oldRect);
		// oldRect = mSelectionRect.getBounds();
		repaint();
	}

	public Vector<Point> getSplinePoints() {
		return mSplinePoints;
	}

	public void addSplinePoint(Point pt) {
		mSplinePoints.addElement(pt);
		setRubberbandDisplayObject(null, false);
	}

	public void drawIsobathLegend(Graphics gin, int width, int leftMargin) {
		Graphics2D g = (Graphics2D)gin;
		float v = 20;
		float h = (float)leftMargin;
		int numCols;

		int numColors = mMapSpec.getNumIsobaths();
		numCols = width / 55;

		GeneralPath aLine;
		BasicStroke lw1 = new BasicStroke(2);

		// compute how many squiggles will fit on legend
		// int numCols = mWidth/55 - 1;

		// set the font size
		g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
		    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
		g.setClip(0, 0, 1000, 1000);

		Color[] tempColors = new Color[numColors];
		double[] tempValues = new double[numColors];
		for (int i = 0; i < numColors; i++) {
			tempColors[i] = mMapSpec.getIsobathColors()[numColors - 1 - i];
			tempValues[i] = -mMapSpec.getIsobathValues()[numColors - 1 - i];
		}

		for (int i = 0; i < numColors; i++) {
			aLine = new GeneralPath();
			double myVal = tempValues[i];
			String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
			aLine.moveTo(h - 4, v + 2);
			aLine.lineTo(h + 2, v - 10);
			aLine.lineTo(h + 7, v - 2);
			aLine.lineTo(h + 13, v - 15);

			try {
				// aLine.closePath();
				g.setStroke(lw1);
				g.setColor(tempColors[i]);
				g.draw(aLine);
			}
			catch (Exception ex) {
			}

			g.setColor(Color.black);
			g.drawString(sTemp, h + 11, v);

			if ((i + 1) % numCols == 0) {
				h = (float)leftMargin;
				v += 20;
			}
			else {
				h += 55;
			}
		}
	}

	public void drawTextLegend(Graphics2D g, double value, int leftMargin) {
		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
			try {
				String valStr = null;
				if (mMapSpec.isStnColorByIsoMinIsoSurfaceValue()) {
					valStr = rb.getString("kAtMin");
				}
				else if (mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()) {
					valStr = rb.getString("kAtMax");
				}
				else {
					valStr = " = " + JOAFormulas.formatDouble(String.valueOf(value), 3, false);
				}

				if (mMapSpec.isStnColorByIsoIsReferenced()) {
					valStr += "(Ref. Level="
					    + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getStnColorByIsoReferenceLevel()), 3, false) + ")";
				}

				String filterStr = null;
				if (mFileViewer.mObsFilterActive) {
					filterStr = rb.getString("kSubjectTo");
				}
				else {
					filterStr = "";
				}

				String sisoLabel = new String(mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel()
				    + rb.getString("kOnwspaces") + mMapSpec.getStnColorByIsoSurface().getParam() + valStr + " ("
				    + mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoSurfVarCode()].getUnits() + ")" + filterStr);
				g.drawString(sisoLabel, leftMargin, 0);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
			Section sech = (Section)of.mSections.currElement();
			String sisoLabel = rb.getString("kStnColorsEqual") + sech.mStnProperties[mMapSpec.getStnColorByStnValVarCode()];
			g.drawString(sisoLabel, leftMargin, 0);
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
			String sisoLabel = rb.getString("kStnColorsEqual") + mMapSpec.getStnColorColorBar().getTitle();
			g.drawString(sisoLabel, leftMargin, 0);
		}
	}

	public int print(Graphics gin, PageFormat pageFormat, int pageIndex) {
		Graphics2D g = (Graphics2D)gin;
		int topSpacer = 10;
		int rightSpacer = 10;
		int bottomSpacer = 10;
		double mWidth;
		double mHeight;

		// get the real width of the plot
		UVCoordinate mm = getTransformedExtremes();
		mWidth = mm.u;
		mHeight = mm.v;

		if (mMapSpec.isPlotGratLabels()) {
			mWidth -= 2 * mHOffset;
			mHeight += topSpacer;
		}
		else {
			// remove the map borders from the plot
			mWidth -= 2 * mHOffset;
			mHeight -= 2 * mVOffset;
			mHeight += topSpacer;
		}

		// get the width of the color bar legend if present
		int cbwidth = 0;
		if (mColorBarLegendPanel != null) {
			cbwidth = mColorBarLegendPanel.getColorBarWidth(g);
		}

		int legendHeight = 0;
		if (mLegend != null && mLegend.getEtopoLegend() != null && mLegend.getEtopoLegend().getColorBar() != null) {
			legendHeight += 50;
			if (mMapSpec.getNumIsobaths() > 0) {
				legendHeight += 50;
				legendHeight += bottomSpacer;
			}
		}

		if (mMapSpec.getStnColorMode() != MapSpecification.COLOR_STNS_BY_JOADEFAULT) {
			legendHeight += bottomSpacer;
			legendHeight += JOAConstants.DEFAULT_PLOT_TITLE_SIZE;
		}

		if (pageIndex == 0) {
			// compute the offset for the legend
			Dimension od = this.getSize();
			double xOffset;
			double yOffset;

			// compute scales
			double xScale = 1.0;
			double yScale = 1.0;

			double rWidth = mWidth + rightSpacer + cbwidth;
			if (rWidth > pageFormat.getImageableWidth()) {
				xScale = pageFormat.getImageableWidth() / ((double)rWidth);
			}

			double rHeight = mHeight + legendHeight + JOAConstants.DEFAULT_PLOT_TITLE_SIZE;
			if (rHeight > pageFormat.getImageableHeight()) {
				yScale = pageFormat.getImageableHeight() / ((double)rHeight);
			}

			xScale = Math.min(xScale, yScale);
			yScale = xScale;

			xOffset = (pageFormat.getImageableWidth() - (rWidth * xScale)) / 2.0;
			yOffset = (pageFormat.getImageableHeight() - (rHeight * yScale)) / 2.0;

			// center the plot on the page
			g.translate(pageFormat.getImageableX() + xOffset, pageFormat.getImageableY() + yOffset);

			g.scale(xScale, yScale);

			// add the title
			String sTemp = mFrame.getTitle();
			Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
			map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_PLOT_TITLE_FONT);
			map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_PLOT_TITLE_SIZE));
			if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.BOLD
			    || JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
				map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			}
			if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.ITALIC
			    || JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
				map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
			}
			map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

			// layout the title
			Map<TextAttribute, Serializable> map2 = (Map<TextAttribute, Serializable>)map;
			TextLayout tl = new TextLayout(sTemp, map2, g.getFontRenderContext());
			Rectangle2D strbounds = tl.getBounds();
			double strWidth = strbounds.getWidth();
			double hh = 60 + mWidth / 2 - strWidth / 2;
			double vv = 40 / 2 + strbounds.getHeight() / 2;

			JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, g, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
			    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
			    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

			mCompositeMap = true;
			this.paintComponent(g);
			mCompositeMap = false;

			// draw the legends
			if (mColorBarLegendPanel != null) {
				g.translate(mWidth, 0);
				mColorBarLegendPanel.drawAllColorBars(g, (int)mHeight, (int)mWidth, 0, JOAConstants.VERTICAL_ORIENTATION);
				g.translate(-mWidth, 0);
			}

			if (mLegend != null && mLegend.getEtopoLegend() != null && mLegend.getEtopoLegend().getColorBar() != null) {
				g.translate(0, od.height);
				drawColorBar(g, mLegend.getEtopoLegend().getColorBar(), 50, (int)mWidth, 20, JOAConstants.HORIZONTAL_ORIENTATION);
				g.translate(0, -od.height);
				g.translate(0, od.height + 50);
				if (mMapSpec.getNumIsobaths() > 0) {
					drawIsobathLegend(g, (int)mWidth, 5);
				}
			}

			if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
				g.translate(0, JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
				drawTextLegend(g, mMapSpec.getStnColorByIsoIsoSurfaceValue(), 20);
			}
			return PAGE_EXISTS;
		}
		else {
			return NO_SUCH_PAGE;
		}
	}

	public void setLocked(boolean b) {
		mWindowIsLocked = b;
	}

	public JOAMapContainer getContents() {
		return mContents;
	}

	public void createPopup(Point point) {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		mPopupMenu = new JPopupMenu();

		JMenuItem openContextualMenu = new JMenuItem(b.getString("kProperties"));
		openContextualMenu.setActionCommand("opencontextual");
		openContextualMenu.addActionListener(this);
		mPopupMenu.add(openContextualMenu);

		JMenuItem saveKML = new JMenuItem(b.getString("kExportKML"));
		saveKML.setActionCommand("savekml");
		saveKML.addActionListener(this);
		mPopupMenu.add(saveKML);

		mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		mPopupMenu.show(this, point.x, point.y);
	}

	public void showConfigDialog() {
		// else show configuration dialog
		ConfigMapPlot cp = new ConfigMapPlot(mFrame, mFileViewer, mDialogClient, mMapSpec);
		cp.pack();
		cp.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("opencontextual")) {
			showConfigDialog();
		}
		else if (cmd.equals("savekml")) {
			((JOAMapPlotWindow)mContents).saveAsKML();
		}
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean bsy) {
		busy = bsy;
	}

	protected void init() {
		mThis = this;
		mFileViewer.addObsChangedListener(this);
		mFileViewer.addLevelChangedListener(this);
		mFileViewer.addMetadataChangedListener(this);
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent me) {
				if (mWindowIsLocked) { return; }
				double x = me.getX();
				double y = me.getY();
				UVCoordinate uv = invTransformLL(x, y);
				double lon = uv.u;
				double lat = uv.v;
				mContents.displayLocation(lat, lon);
			}

			public void mouseMoved(MouseEvent me) {
				double x = me.getX();
				double y = me.getY();
				UVCoordinate uv = invTransformLL(x, y);
				double lon = uv.u;
				double lat = uv.v;
				mContents.displayLocation(lat, lon);
			}
		});

		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (mWindowIsLocked) { return; }
				if (me.getClickCount() == 2 && (mSelectionMode == MapPlotPanel.ZOOM_MODE /*
																																									 * ||
																																									 * mSelectionMode
																																									 * ==
																																									 * MapPlotPanel
																																									 * .
																																									 * SELECTION_MODE
																																									 */)) {
					showConfigDialog();
				}
				else if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
				else {
					if (me.isPopupTrigger()) {
						createPopup(me.getPoint());
					}
					else {
						if (!rbRect.isPoint()) { return; }
						requestFocus();
						// if selectiontool mode start or allow vertex in section line
						if (mSelectionMode == MapPlotPanel.CENTERING_MODE) {
							centerMap(me.getX(), me.getY());
						}
						else if (mSelectionMode == MapPlotPanel.ZOOM_MODE) { // &&
							// !rbRect.splineActive())
							// {
							// find a new observation
							findByXY(me.getX(), me.getY());
							rbRect.resetRect();
						}
						else if (mSelectionMode == MapPlotPanel.SELECTION_MODE) {
							processSingleSelection(me.getPoint());
						}
					}
				}
			}

			public void mouseReleased(MouseEvent me) {
				if (mWindowIsLocked) { return; }
				super.mouseReleased(me);
				oldRect.setBounds(0, 0, 0, 0);
				setRubberbandDisplayObject(null, false);
				if (rbRect.isPoint()) { return; }
				if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
				else if (rbRect != null && rbRect.isActive() && !rbRect.firstStretch
				    && mSelectionMode == MapPlotPanel.ZOOM_MODE && me.getID() == MouseEvent.MOUSE_RELEASED) {
					zoomPlot(rbRect, me.isAltDown(), me.isShiftDown());
					rbRect.end(rbRect.getStretched());
					rbRect.end(rbRect.getStretched());
					rbRect.resetRect();
				}
				else if (mSelectionMode == MapPlotPanel.SELECTION_MODE) {
					boolean found = false;
					if ((mMapSpec.getProjection() >= JOAConstants.MERCATORPROJECTION && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION)
					    || (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) || (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION)) {
						found = processAreaSelection(rbRect);
					}
					else {
						found = processCircularSelection(rbRect);
					}
					rbRect.resetRect();
					rbRect.end(rbRect.getStretched());
					if (!found) {
						rbRect.end(rbRect.getStretched());
					}
				}
			}

			public void mousePressed(MouseEvent me) {
				if (mWindowIsLocked) { return; }
				super.mousePressed(me);
				if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
			}
		});
	}

	public void setSelectionAsRectangle() {
		rbRect.resetRect();
		rbRect.setActive(false);
		rbRect = null;
		rbRect = new RubberbandRectangle(this);
		rbRect.setActive(true);
		rbRect.resetRect();
	}

	public void setSelectionAsLine() {
		rbRect.resetRect();
		rbRect.setActive(false);
		rbRect = null;
		rbRect = new SectionSpline(this);
		rbRect.setActive(true);
		rbRect.resetRect();
	}

	public void setSelectionAsPolygon() {
		rbRect.resetRect();
		rbRect.setActive(false);
		rbRect = null;
		rbRect = new PolygonSpline(this);
		rbRect.setActive(true);
		rbRect.resetRect();
	}

	public void setSelectionAsPSRectangle() {
		rbRect.setActive(false);
		rbRect.resetRect();
		rbRect = null;
		rbRect = new StereographicRectangle(this);
		rbRect.setActive(true);
		rbRect.resetRect();
	}

	public void setSelectionAsPolarCircle() {
		rbRect.setActive(false);
		rbRect.resetRect();
		rbRect = null;
		rbRect = new PolarCircle(this);
		rbRect.setActive(true);
		rbRect.resetRect();
	}

	public void setSelectionAsCentering() {
		rbRect.resetRect();
		rbRect.setActive(false);
	}

	public void setSelectionMode() {
		mSelectionMode = MapPlotPanel.SELECTION_MODE;
	}

	public void setPolygonMode() {
		mSelectionMode = MapPlotPanel.POLYGON_MODE;
	}

	public void setSectionMode() {
		mSelectionMode = MapPlotPanel.SECTION_MODE;
	}

	public void setZoomMode() {
		mSelectionMode = MapPlotPanel.ZOOM_MODE;
	}

	public void setCenteringMode() {
		mSelectionMode = MapPlotPanel.CENTERING_MODE;
	}

	// public Dimension getPreferredSize() {
	// return new Dimension(mWidth-10, mHeight-mLegendHeight-25);
	// }

	public int getMinX() {
		return 1;
	}

	public int getMinY() {
		return 1;
	}

	public int getMaxX() {
		return this.getSize().width - 1;
	}

	public int getMaxY() {
		return this.getSize().height - 1;
	}

	public void rubberbandEnded(Rubberband rb) {
	}

	public void zoomPlot(Rubberband rbRect, boolean mode, boolean mode2) {
		double minLat = 0, minLon = 0, maxLat = 0, maxLon = 0;

		// convert corners of rectangle to new plot range
		int x1 = rbRect.getUpperLeftX();
		int x2 = rbRect.getLowerRightX();
		int y1 = rbRect.getUpperLeftY();
		int y2 = rbRect.getLowerRightY();
		rbRect.resetRect();

		// by definition a zoomed plot can not be a globe and is a custom map
		mMapSpec.setGlobe(false);
		mMapSpec.setCustomMap(true);

		if (x1 == JOAConstants.IOFFMAPN || x2 == JOAConstants.IOFFMAPN || y1 == JOAConstants.IOFFMAPN
		    || y2 == JOAConstants.IOFFMAPN) {
			rbRect.resetRect();
			return;
		}

		if (mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			// convert to lat/lon
			UVCoordinate uv = invTransformLL(x2, y2);
			maxLon = uv.u;
			minLat = uv.v;
			uv = invTransformLL(x1, y1);
			minLon = uv.u;
			maxLat = uv.v;

			if (Math.abs(maxLon - minLon) > 10) {
				maxLon = Math.round(maxLon);
				minLon = Math.round(minLon);
			}
			if (Math.abs(maxLat - minLat) > 10) {
				minLat = Math.round(minLat);
				maxLat = Math.round(maxLat);
			}

			if (Math.abs(x2 - x1) < 10 || Math.abs(y2 - y1) < 10) {
				rbRect.resetRect();
				return;
			}
		}
		else {
			// polar view
			UVCoordinate uv = invTransformLL(x1, y2);
			if (mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
				maxLat = 90;
				minLat = uv.v;
				maxLon = mMapSpec.getLonRt();
				minLon = mMapSpec.getLonLft();
			}
			else if (mMapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION) {
				minLat = -90;
				maxLat = uv.v;
				maxLon = mMapSpec.getLonRt();
				minLon = mMapSpec.getLonLft();
			}
			if (Math.abs(x2 - x1) < 5) {
				rbRect.resetRect();
				return;
			}
		}

		if (mode) {
			// zoom using current window: Only the map domain changes
			mMapSpec.setLatMax(maxLat);
			mMapSpec.setLatMin(minLat);
			mMapSpec.setLonRt(maxLon);
			mMapSpec.setLonLft(minLon);
			mMapSpec.setMapName(JOAFormulas.getCustomMapName(mMapSpec));
			mMapSpec.setGlobe(false);
			mMapSpec.setCustomMap(true);
			if (mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
				mMapSpec.setCenLat(minLat + (maxLat - minLat) / 2.0);
				double diff = 0;
				if (mMapSpec.getLonLft() > 0 && mMapSpec.getLonRt() < 0) {
					diff = Math.abs(((180 + mMapSpec.getLonRt()) + (180 - mMapSpec.getLonLft())) / 2);
				}
				else {
					diff = Math.abs((mMapSpec.getLonRt() - mMapSpec.getLonLft()) / 2);
				}
				mMapSpec
				    .setCenLon(diff + mMapSpec.getLonLft() > 180 ? mMapSpec.getLonRt() - diff : mMapSpec.getLonLft() + diff);
			}

			// invalidate and replot
			mMapSpec.setUMax(0.0);
			mMapSpec.setUMin(0.0);
			mMapSpec.setVMax(0.0);
			mMapSpec.setVMin(0.0);
			invalidate();
			rbRect.resetRect();
			mFrame.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
			mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height);
		}
		else {
			// zoom to new window but first copy specification
			MapSpecification ms = new MapSpecification();
			ms.setDrawLegend(mMapSpec.isDrawLegend());
			ms.setAutoGraticule(mMapSpec.isAutoGraticule());
			ms.setConnectStnsAcrossSections(mMapSpec.isConnectStnsAcrossSections());
			ms.setLatMax(maxLat);
			ms.setLatMin(minLat);
			ms.setLonRt(maxLon);
			ms.setLonLft(minLon);
			ms.setProjection(mMapSpec.getProjection());
			ms.setGlobe(false);
			ms.setCustomMap(true);
			ms.setMapName(JOAFormulas.getCustomMapName(ms));
			if (ms.getProjection() <= JOAConstants.STEREOPROJECTION) {
				ms.setCenLat(minLat + (maxLat - minLat) / 2.0);
				double diff = 0;
				if (ms.getLonLft() > 0 && ms.getLonRt() < 0) {
					diff = Math.abs(((180 + ms.getLonRt()) + (180 - ms.getLonLft())) / 2);
				}
				else {
					diff = Math.abs((ms.getLonRt() - ms.getLonLft()) / 2);
				}
				ms.setCenLon(diff + ms.getLonLft() > 180 ? ms.getLonRt() - diff : ms.getLonLft() + diff);
			}
			else {
				ms.setCenLat(mMapSpec.getCenLat());
				ms.setCenLon(mMapSpec.getCenLon());
			}

			ms.setConnectStns(mMapSpec.isConnectStns());
			ms.setSymbolSize(mMapSpec.getSymbolSize());
			ms.setPlotStnSymbols(mMapSpec.isPlotStnSymbols());
			ms.setSymbol(mMapSpec.getSymbol());
			ms.setLineWidth(mMapSpec.getLineWidth());
			ms.setDrawGraticule(mMapSpec.isDrawGraticule());
			ms.setLatGratSpacing(mMapSpec.getLatGratSpacing());
			ms.setLonGratSpacing(mMapSpec.getLonGratSpacing());
			ms.setBGColor(new Color(mMapSpec.getBGColor().getRGB()));
			ms.setCoastColor(new Color(mMapSpec.getCoastColor().getRGB()));
			ms.setGratColor(new Color(mMapSpec.getGratColor().getRGB()));
//			ms.setSectionColor(new Color(mMapSpec.getSectionColor().getRGB()));
			ms.setCurrBasin(mMapSpec.getCurrBasin());
			ms.setRetainProjAspect(mMapSpec.isRetainProjAspect());
			ms.setCoastLineRez(mMapSpec.getCoastLineRez());
			ms.setPlotSectionLabels(mMapSpec.isPlotSectionLabels());
			ms.setPlotGratLabels(mMapSpec.isPlotGratLabels());
			ms.setPlotStnLabels(mMapSpec.isPlotStnLabels());
			ms.setLabelColor(mMapSpec.getLabelColor());
			ms.setStnLabelOffset(mMapSpec.getStnLabelOffset());
			ms.setStnLabelAngle(mMapSpec.getStnLabelAngle());
			ms.setContourLabelPrec(mMapSpec.getContourLabelPrec());
			ms.setUMax(0.0);
			ms.setUMin(0.0);
			ms.setVMax(0.0);
			ms.setVMin(0.0);
			ms.setPlotStnSymbols(mMapSpec.isPlotStnSymbols());

			// stn coloring
			ms.setStnColorMode(mMapSpec.getStnColorMode());

			if (ms.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
				// isosurface
				ms.setStnColorByIsoVarCode(mMapSpec.getStnColorByIsoVarCode());
				ms.setStnColorByIsoSurfVarCode(mMapSpec.getStnColorByIsoSurfVarCode());
				if (mMapSpec.getStnColorByIsoSurface() != null) {
					ms.setStnColorByIsoSurface(new NewInterpolationSurface(mMapSpec.getStnColorByIsoSurface()));
				}
				ms.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoIsoSurfaceValue());
				ms.setStnColorByIsoMinIsoSurfaceValue(mMapSpec.isStnColorByIsoMinIsoSurfaceValue());
				ms.setStnColorByIsoMaxIsoSurfaceValue(mMapSpec.isStnColorByIsoMaxIsoSurfaceValue());
				ms.setStnColorColorBar(new NewColorBar(mMapSpec.getStnColorColorBar()));
				ms.setStnColorByIsoBottomUpSearch(mMapSpec.isStnColorByIsoBottomUpSearch());
				ms.setStnColorByIsoLocalInterpolation(mMapSpec.isStnColorByIsoLocalInterpolation());
				ms.setStnColorByIsoMaxInterpDistance(mMapSpec.getStnColorByIsoMaxInterpDistance());
				ms.setStnColorByIsoReferenceLevel(mMapSpec.getStnColorByIsoReferenceLevel());
				ms.setStnColorByIsoAutoscaledColorCB(mMapSpec.isStnColorByIsoAutoscaledColorCB());
				ms.setStnColorByIsoIsReferenced(mMapSpec.isStnColorByIsoIsReferenced());

				ms.setStnColorByIsoIsResidualInterp(mMapSpec.isStnColorByIsoIsResidualInterp());
				if (ms.isStnColorByIsoIsResidualInterp()) {
					ms.setStnColorByIsoMeanCastStnList(null);
					ms.setStnColorByIsoMeanCastStnList(new boolean[mMapSpec.getStnColorByIsoMeanCastStnList().length]);
					for (int i = 0; i < mMapSpec.getStnColorByIsoMeanCastStnList().length; i++) {
						ms.setStnColorByIsoMeanCastStn(i, mMapSpec.isStnColorByIsoMeanCastStn(i));
					}
				}
			}
			else if (ms.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
				// Stn Var
				ms.setStnColorByStnValVarCode(mMapSpec.getStnColorByStnValVarCode());
				ms.setStnColorColorBar(new NewColorBar(mMapSpec.getStnColorColorBar()));
				ms.setStnColorByStnValAutoscaledColorBar(mMapSpec.isStnColorByStnValAutoscaledColorBar());
			}
			else if (ms.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
				// Metadata
				try {
					// This colorbar has to know about the max and min values from the
					// fileviewer
					ms.getStnColorColorBar().setMetadata(mFileViewer.getMinLat(), mFileViewer.getMaxLat(),
					    mFileViewer.getMinLon(), mFileViewer.getMaxLon(), mFileViewer.getMinDate(), mFileViewer.getMaxDate());
					// add this as a listener
					mFileViewer.addMetadataChangedListener(ms.getStnColorColorBar());
				}
				catch (Exception ex) {
					ms.setStnColorColorBar(null);
				}
			}

			// overlay contour fields;
			ms.setContourOverlayMode(mMapSpec.getContourOverlayMode());

			if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
				ms.setIsoContourVarCode(mMapSpec.getIsoContourVarCode());
				ms.setIsoContourSurfVarCode(mMapSpec.getIsoContourSurfVarCode());
				ms.setIsoContourStyle(mMapSpec.getIsoContourStyle());
				if (mMapSpec.getIsoContourSurface() != null) {
					ms.setIsoContourSurface(new NewInterpolationSurface(mMapSpec.getIsoContourSurface()));
				}
				ms.setContourIsoSurfaceValue(mMapSpec.getContourIsoSurfaceValue());
				ms.setIsoContourMinSurfaceValue(mMapSpec.isIsoContourMinSurfaceValue());
				ms.setIsoContourMaxSurfaceValue(mMapSpec.isIsoContourMaxSurfaceValue());
				ms.setOverlayContoursColorBar(new NewColorBar(mMapSpec.getOverlayContoursColorBar()));
				ms.setIsoContourColor(mMapSpec.getIsoContourColor());
				ms.setIsoContourBottomUpSearch(mMapSpec.isIsoContourBottomUpSearch());
				ms.setIsoContourLocalInterpolation(mMapSpec.isIsoContourLocalInterpolation());
				ms.setIsoContourMaxInterpDistance(mMapSpec.getIsoContourMaxInterpDistance());
				ms.setIsoContourAutoscaledColorCB(mMapSpec.isIsoContourAutoscaledColorCB());

				ms.setIsoContourReferenced(mMapSpec.isIsoContourReferenced());
				ms.setIsoContIsResidualInterp(mMapSpec.isIsoContIsResidualInterp());
				ms.setIsoContourReferenceLevel(mMapSpec.getIsoContRefLevel());
				if (ms.isIsoContIsResidualInterp()) {
					ms.setIsoContMeanCastStnList(null);
					ms.setIsoContMeanCastStnList(new boolean[mMapSpec.getIsoContMeanCastStnList().length]);
					for (int i = 0; i < mMapSpec.getIsoContMeanCastStnList().length; i++) {
						ms.setIsoContByIsoMeanCastStn(i, mMapSpec.isIsoContByIsoMeanCastStn(i));
					}
				}

				ms.setPlotEveryNthContour(mMapSpec.getPlotEveryNthContour());
				ms.setNRng(mMapSpec.getNRng());
				ms.setNX(mMapSpec.getNX());
				ms.setNY(mMapSpec.getNY());
				ms.setCAY(mMapSpec.getCAY());
				ms.setMaskCoast(mMapSpec.isMaskCoast());
				ms.setFilledIsoContours(mMapSpec.isFilledIsoContours());
			}
			else if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
				ms.setStnCalcContourAutoscaledColorCB(mMapSpec.isStnCalcContourAutoscaledColorCB());
				ms.setStnContourStyle(mMapSpec.getStnContourStyle());
				ms.setStnCalcContourVarCode(mMapSpec.getStnCalcContourVarCode());
				ms.setOverlayContoursColorBar(mMapSpec.getOverlayContoursColorBar());
				ms.setStnVarCalcContourColor(mMapSpec.getStnVarCalcContourColor());

				ms.setPlotEveryNthContour(mMapSpec.getPlotEveryNthContour());
				ms.setNRng(mMapSpec.getNRng());
				ms.setNX(mMapSpec.getNX());
				ms.setNY(mMapSpec.getNY());
				ms.setCAY(mMapSpec.getCAY());
				ms.setMaskCoast(mMapSpec.isMaskCoast());
				ms.setFilledStnContours(mMapSpec.isFilledStnContours());
			}

			if (mMapSpec.getBathyColorBar() != null) {
				ms.setBathyColorBar(new NewColorBar(mMapSpec.getBathyColorBar()));
			}

			ms.setNumEtopoFiles(0);
			for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
				if (mMapSpec.getEtopoFile(i) != null) {
					ms.setEtopoFile(new String(mMapSpec.getEtopoFile(i)));
				}
				else {
					ms.setEtopoFile(new String());
				}
			}
			ms.setColorFill(mMapSpec.isColorFill());

			ms.setNumIsobaths(mMapSpec.getNumIsobaths());
			for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) {
				ms.setColor(i, mMapSpec.getColor(i));
				ms.setValue(i, mMapSpec.getValue(i));
				ms.setPath(i, new String(mMapSpec.getPath(i)));
				if (mMapSpec.getDescrip(i) != null) {
					ms.setDescrip(i, new String(mMapSpec.getDescrip(i)));
				}
				else {
					ms.setDescrip(i, null);
				}
			}
			if (mMapSpec.getCustCoastPath() != null) {
				ms.setCustCoastPath(new String(mMapSpec.getCustCoastPath()));
			}
			else {
				ms.setCustCoastPath(null);
			}

			try {
				ms.writeToLog("New Map Plot: (" + mFileViewer.getTitle() + ")");
			}
			catch (Exception ex) {
			}

			JOAMapPlotWindow plotWind = new JOAMapPlotWindow(mFileViewer, ms, 650, 650, mFrame);
			plotWind.pack();
			plotWind.invalidate();
			plotWind.setVisible(true);
			mFileViewer.addOpenWindow(plotWind);
			rbRect.resetRect();
		}
	}

	public void invalidate() {
		try {
			super.invalidate();
			if (mOffScreen != null) {
				mOffScreen = null;
			}
			if (mObsMarker != null) {
				mObsMarker = null;
			}
			if (mStnOffScreen != null) {
				mStnOffScreen = null;
			}

			Dimension nd = this.getSize();
			if (nd.width > 0) {
				mLegend.setWidth(nd.width);
			}
		}
		catch (Exception e) {

		}
	}

	public void makeCompositeOffScreen() {
		mCompositeMap = true;
		// invalidate();
		paintComponent(this.getGraphics());
	}

	public void unMakeCompositeOffScreen() {
		mCompositeMap = false;
	}

	public Image getCompositeMapImage() {
		int topSpacer = 10;
		int bottomSpacer = 10;
		double mWidth;
		double mHeight;
		Dimension od = this.getSize();

		// get the real width of the plot
		UVCoordinate mm = getTransformedExtremes();
		mWidth = mm.u;
		mHeight = mm.v;

		if (mMapSpec.isPlotGratLabels()) {
			mWidth -= 2 * mHOffset;
			mHeight += topSpacer;
		}
		else {
			// remove the map borders from the plot
			mWidth -= 2 * mHOffset;
			mHeight -= 2 * mVOffset;
			mHeight += topSpacer;
		}

		int legendHeight = 0;
		if (mLegend != null && mLegend.getEtopoLegend() != null && mLegend.getEtopoLegend().getColorBar() != null) {
			legendHeight += 50;
			if (mMapSpec.getNumIsobaths() > 0) {
				legendHeight += 50;
				legendHeight += bottomSpacer;
			}
		}

		if (mMapSpec.getStnColorMode() != MapSpecification.COLOR_STNS_BY_JOADEFAULT) {
			legendHeight += bottomSpacer;
			legendHeight += JOAConstants.DEFAULT_PLOT_TITLE_SIZE;
		}

		Image image = createImage((int)od.getWidth(), (int)od.getHeight() + legendHeight + bottomSpacer);
		final Graphics2D og = (Graphics2D)image.getGraphics();

		// render the map on it's own thread
		createImage(image);
		// lbr.setPriority(Thread.NORM_PRIORITY - 1);
		// lbr.startRenderer();

		// set a clip shape if present
		Shape currClip = og.getClip();
		if (this.mCurrClipShape != null) {
			og.setClip(mCurrClipShape);
		}

		// plot the stations
		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_JOADEFAULT && mMapSpec.isPlotStnSymbols()) {
			plotStations(og);
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
			plotStationsWithIsoSurfaceColors(og);
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
			plotStationsWithStnVarColors(og);
		}
		else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
			plotStationsWithMetadataColors(og);
		}

		// draw the overlay contours
		if (getMapSpec().getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
			plotIsoContours(og, true);
		}
		if (getMapSpec().getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
			plotStnValContours(og, true);
		}

		if (mMapSpec.isPlotSectionLabels()) {
			plotSectionLabels(og);
		}

		if (mMapSpec.isPlotStnLabels()) {
			plotStationLabels(og);
		}

		// reset clip
		og.setClip(currClip);

		// add the title
		String sTemp = mFrame.getTitle();
		Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
		map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_PLOT_TITLE_FONT);
		map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_PLOT_TITLE_SIZE));
		if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.BOLD
		    || JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
			map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		}
		if (JOAConstants.DEFAULT_PLOT_TITLE_STYLE == Font.ITALIC
		    || JOAConstants.DEFAULT_PLOT_TITLE_STYLE == (Font.BOLD | Font.ITALIC)) {
			map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}
		map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

		// layout the title
		TextLayout tl = new TextLayout(sTemp, (Map<TextAttribute, Serializable>)map, og.getFontRenderContext());
		Rectangle2D strbounds = tl.getBounds();
		double strWidth = strbounds.getWidth();
		double hh = 60 + mWidth / 2 - strWidth / 2;
		double vv = 40 / 2 + strbounds.getHeight() / 2;

		JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
		    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
		    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

		// draw the legends
		if (mColorBarLegendPanel != null) {
			og.translate(mWidth, 0);
			mColorBarLegendPanel.drawAllColorBars(og, (int)mHeight, (int)mWidth, 0, JOAConstants.VERTICAL_ORIENTATION);
			og.translate(-mWidth, 0);
		}

		if (mLegend != null && mLegend.getEtopoLegend() != null && mLegend.getEtopoLegend().getColorBar() != null) {
			og.translate(0, od.height);
			drawColorBar(og, mLegend.getEtopoLegend().getColorBar(), 50, (int)mWidth, 20, JOAConstants.HORIZONTAL_ORIENTATION);
			og.translate(0, -od.height);
			og.translate(0, od.height + 50);
			if (mMapSpec.getNumIsobaths() > 0) {
				drawIsobathLegend(og, (int)mWidth, 5);
			}
		}

		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
			og.translate(0, JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
			drawTextLegend(og, mMapSpec.getContourIsoSurfaceValue(), 20);
		}

		// plot the overlays
		return image;
	}

	public void createImage(Image image) {
		setBusy(true);
		final Graphics og = image.getGraphics();

		// draw the background filled bathymetry from external netCDF file
		if (getMapSpec().isColorFill()) {
				drawFilledBathy(og);
		}

		// draw the isobath lines from external netCDF files
			drawIsobaths(og);

		// plot the Map
		drawMap(og);

		// draw the map graticule
		if (getMapSpec().isDrawGraticule()) {
				plotBorder(og);
		}

		// if (mMapPlotPanel.getMapSpec().isPlotSectionLabels())
		// mMapPlotPanel.plotSectionLabels(og);

		// if (mMapPlotPanel.getMapSpec().isPlotStnLabels())
		// mMapPlotPanel.plotStationLabels(og);

		// draw the offscreen into the graphics context of the panel
		getGraphics().drawImage(mOffScreen, 2, 2, null);

		// dispose of the offscreen
		og.dispose();

		// set a flag in the map container to say that we have plotted the map for
		// the first time
		getContents().setFirstPlot(false);
		setBusy(false);
		// paintComponent(getGraphics());
	}

	public Image makeOffScreen(Graphics2D g, boolean addTitle, boolean plotOverlays) {
		Image outImage = null;

		// set up the map projection
		this.checkProjection(getSize().width - 4, getSize().height - 4);

		double mWidth;

		// get the real width of the plot
		UVCoordinate mm = getTransformedExtremes();
		mWidth = mm.u;

		if (mWidth <= 1) {
			mWidth = this.getWidth();
		}

		// create the offscreen
		outImage = createImage(getSize().width - 4, getSize().height - 4);
		Graphics2D og = (Graphics2D)outImage.getGraphics();

		// render the map on it's own thread
		LineBathyRenderer lbr = new LineBathyRenderer(outImage, this);

		if (addTitle) {
			// add the title
			String sTemp = mMapSpec.getMapName();

			// layout the title
			Font font = new Font(JOAConstants.DEFAULT_PLOT_TITLE_FONT, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
			    JOAConstants.DEFAULT_PLOT_TITLE_SIZE);
			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			int strWidth = fm.stringWidth(sTemp);
			double hh = 60 + mWidth / 2 - strWidth / 2;
			double vv = 40 / 2 + fm.getHeight() / 2;

			JOAFormulas.drawStyledString(sTemp, (int)hh, (int)vv, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
			    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
			    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);
		}

		return outImage;
	}

	public void paintComponent(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;

		if (mCompositeMap) {
			// create the offscreen
			int height = getSize().height - 4;
			int width = getSize().width - 4;
			int legendOffset = 0;
			if (mLegend.getEtopoLegend() != null) {
				legendOffset = 60;
			}
			if (mLegend.getIsobathLegend() != null) {
				legendOffset += 40;
			}
			mCompositeOffScreen = createImage(width, height + legendOffset);
			// final Graphics og = mCompositeOffScreen.getGraphics();

			// draw the bathymetry
			if (mMapSpec.isColorFill()) {
				drawFilledBathy(g);
				// drawFilledBathyGCS(g);
			}
			drawIsobaths(g);

			// plot the Map
			drawMap(g);

			// set a clip shape if present
			Shape currClip = g.getClip();
			if (this.mCurrClipShape != null) {
				g.setClip(mCurrClipShape);
			}

			// plot the stations
			if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_JOADEFAULT && mMapSpec.isPlotStnSymbols()) {
				plotStations(g);
			}
			else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
				plotStationsWithIsoSurfaceColors(g);
			}
			else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
				plotStationsWithStnVarColors(g);
			}
			else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
				plotStationsWithMetadataColors(g);
			}

			// draw the overlay contours
			if (getMapSpec().getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
				plotIsoContours(g, true);
				drawMaskBathy(g);
			}
			else if (getMapSpec().getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
				plotStnValContours(g, true);
				drawMaskBathy(g);
			}

			if (mMapSpec.isPlotSectionLabels()) {
				plotSectionLabels(g);
			}

			if (mMapSpec.isPlotStnLabels()) {
				plotStationLabels(g);
			}

			g.setClip(currClip);

			if (mMapSpec.isDrawGraticule()) {
				plotBorder(g);
			}
		}
		else {
			if (mOffScreen == null) {
				firstTime = mContents.isFirstPlot();
				mOffScreen = makeOffScreen(g, JOAConstants.DEFAULT_PLOT_TITLES, true);
			}
			else {
				if (!this.isBusy() && mOffScreen != null && g != null) {
					g.drawImage(mOffScreen, 1, 1, null);

					// set a clip shape if present
					Shape currClip = g.getClip();
					if (this.mCurrClipShape != null) {
						g.setClip(mCurrClipShape);
					}

					if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_JOADEFAULT && mMapSpec.isPlotStnSymbols()) {
						plotStations(g);
					}
					else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
						plotStationsWithIsoSurfaceColors(g);
					}
					else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
						plotStationsWithStnVarColors(g);
					}
					else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
						plotStationsWithMetadataColors(g);
					}

					// draw the overlay contours
					boolean selectionInProgress = mSelectionRect != null
					    && (mSelectionRect.getBounds2D().getWidth() > 0 || mSelectionRect.getBounds2D().getHeight() > 0);

					if (getMapSpec().getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
						plotIsoContours(g, !selectionInProgress);
						drawMaskBathy(g);
					}
					else if (getMapSpec().getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
						plotStnValContours(g, !selectionInProgress);
						drawMaskBathy(g);
					}

					if (mMapSpec.isPlotSectionLabels()) {
						plotSectionLabels(g);
					}

					if (mMapSpec.isPlotStnLabels()) {
						plotStationLabels(g);
					}

					g.setClip(currClip);

					if (mObsMarker == null) {
						initDataSpot();
					}

					if (mObsMarker != null && !mWindowIsLocked) {
						g.setColor(Color.black);
						mObsMarker.drawMarker(g, false);
					}
				}

				// draw the selections
				try {
					// draw any previous spline points
					if (mSplinePoints.size() > 0) {
						mSplinePath.reset();
						Point pt = (Point)mSplinePoints.elementAt(0);
						mSplinePath.moveTo((float)pt.x, (float)pt.y);
						g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_OUTLINE_COLOR);
						g.setStroke(lw2);
						for (int i = 1; i < mSplinePoints.size(); i++) {
							pt = (Point)mSplinePoints.elementAt(i);
							mSplinePath.lineTo((float)pt.x, (float)pt.y);
						}
						try {
							g.draw(mSplinePath);
						}
						catch (Exception ex) {
						}
					}
					if (mSelectionRect != null) {
						g.setPaint(JOAConstants.DEFAULT_SELECTION_REGION_BG_COLOR);
						g.fill(mSelectionRect);
						g.setColor(JOAConstants.DEFAULT_SELECTION_REGION_OUTLINE_COLOR);
						g.setStroke(lw2);
						g.draw(mSelectionRect);
						g.setColor(Color.black);
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void drawFancyLabel(Graphics g, int x, int y, String label, Color c) {
		if (label == null || c == null) { return; }
		// get the length of the text
		int red = c.getRed();
		int blue = c.getBlue();
		int green = c.getGreen();
		FontMetrics fm = g.getFontMetrics();
		int strWidth = fm.stringWidth(label);
		int strHeight = fm.getAscent() + fm.getDescent();
		g.setColor(c);
		int width = strWidth + 6;
		int height = strHeight;
		g.fillRect(x - 3, y - strHeight / 2 - 4, width, height);
		if (red + blue + green < 255) {
			g.setColor(Color.white);
		}
		else {
			g.setColor(Color.black);
		}
		g.drawString(label, x, y);

		// draw the drop shadow
		g.setColor(Color.black);
		int sx = x - 3;
		int sy = y + 3;
		g.drawLine(sx, sy, sx + width, sy);
		g.drawLine(sx + width, sy, sx + width, sy - height + 1);
	}

	public void clearSavedETOPOLocs() {
		savedLats = null;
		savedLons = null;
		savedVals = null;
	}
	
	public void drawTheFilledBathy(Graphics gin, String etopoFile) {
		Graphics2D g = (Graphics2D)gin;
		
	}

	public void drawFilledBathy(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;
		ProgressDialog progress = null;
		float delta = 0.5f;

		// get array of etopo filenames
		ptsHaveBeenCached = true;

		// have the etopo selections changed?
		if (oldEtopoFile == null) {
			// first time--haven't changes
			oldEtopoFile = new String[mMapSpec.getNumEtopoFiles()];
			for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
				oldEtopoFile[i] = new String(mMapSpec.getEtopoFile(i));
			}
		}
		else {
			if (oldEtopoFile.length != mMapSpec.getNumEtopoFiles()) {
				// number of etopo files has changed
				ptsHaveBeenCached = false;
			}
			else {
				// number has stayed the same--test whether they are the same
				// etopo files
				for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
					if (!oldEtopoFile[i].equalsIgnoreCase(mMapSpec.getEtopoFile(i))) {
						// etopo file changed--reset to new setting
						oldEtopoFile = null;
						oldEtopoFile = new String[mMapSpec.getNumEtopoFiles()];
						for (int ii = 0; ii < mMapSpec.getNumEtopoFiles(); ii++) {
							oldEtopoFile[ii] = new String(mMapSpec.getEtopoFile(ii));
						}
						ptsHaveBeenCached = false;
						break;
					}
				}
			}
		}

		for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
			try {
				if (savedLats[i] != null) {
					continue;
				}
			}
			catch (Exception ex) {
				savedLats = new float[mMapSpec.getNumEtopoFiles()][];
				savedLons = new float[mMapSpec.getNumEtopoFiles()][];
				savedVals = new float[mMapSpec.getNumEtopoFiles()][][];
				ptsHaveBeenCached = false;
				break;
			}
		}

		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
		for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
			String mFilename = mMapSpec.getEtopoFile(i);
			mETOPODB = null;
			if (firstTime) {
				if (progress != null) {
					progress.dispose();
				}
				progress = null;
				progress = new ProgressDialog(mFileViewer, "drawFilledBathyPlotting Etopo Data in " + mFilename, Color.blue, Color.white);
				progress.setVisible(true);
			}
			else {
				mContents.reset();
			}

			if (mFilename.indexOf("60") >= 0) {
				delta = 0.5f;
			}
			else if (mFilename.indexOf("20") >= 0) {
				delta = 0.1667f;
			}
			else if (mFilename.indexOf("5") >= 0) {
				delta = 0.0417f;
			}
			else if (mFilename.indexOf("2") >= 0) {
				delta = 0.01667f;
			}
			else if (mFilename.indexOf(".5") >= 0) {
				delta = 4.1667e-03f;
			}

			if (mETOPODB == null) {
				// get the etopo datafile
				File etopoFile = null;
				try {
					etopoFile = JOAFormulas.getSupportFile(mFilename);
				}
				catch (IOException ex) {
					// present an error dialog
					return;
				}
				String dir = etopoFile.getParent();

				EpicPtrs ptrDB = new EpicPtrs();

				// create a pointer
				EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "ETOPO Import", "ETOPO", "na", "na", -99, -99,
				    new gov.noaa.pmel.util.GeoDate(), -99, -99, null, mFilename, dir, null);

				// set the data of ptrDB to this one entry
				ptrDB.setFile(etopoFile);
				ptrDB.setData(epPtr);

				// create a database
				PointerDBIterator pdbi = ptrDB.iterator();
				EPSDbase etopoDB = new EPSDbase(pdbi, false);

				// get the database
				EPSDBIterator dbItor = etopoDB.iterator(false);

				try {
					mETOPODB = (Dbase)dbItor.getElement(0);
				}
				catch (Exception ex) {
				}
			}

			// latitude axis
			Axis latAxis = mETOPODB.getAxis("Y");
			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("y");
			}

			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("lat");
			}
			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("latitude");
			}

			MultiArray latma = latAxis.getData();

			// longitude axis
			Axis lonAxis = mETOPODB.getAxis("X");
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("lon");
			}
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("x");
			}
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("longitude");
			}

			MultiArray lonma = lonAxis.getData();

			Vector<?> vars = mETOPODB.getMeasuredVariables(false);
			EPSVariable rose = null;
			int roseVar = 0;
			for (int b = 0; b < vars.size(); b++) {
				EPSVariable var = (EPSVariable)vars.elementAt(b);

				if (var.getName().trim().toUpperCase().indexOf("ROSE") >= 0) {
					roseVar = b;
				}
			}

			rose = (EPSVariable)vars.elementAt(roseVar);
			MultiArray zma = rose.getData();

			UVCoordinate uvUL = null;
			UVCoordinate uvUR = null;
			UVCoordinate uvLL = null;
			UVCoordinate uvLR = null;
			double oldULu = 0.0;
			double oldULv = 0.0;
			double oldURu = 0.0;
			double oldURv = 0.0;
			// double[] ulu = new double[lonAxis.getLen()];
			// double[] ulv = new double[lonAxis.getLen()];
			// double[] oldulu = new double[lonAxis.getLen()];
			// double[] oldulv = new double[lonAxis.getLen()];
			if (!ptsHaveBeenCached) {
				savedLats[i] = new float[latAxis.getLen()];
				savedLons[i] = new float[lonAxis.getLen()];
				savedVals[i] = new float[lonAxis.getLen()][latAxis.getLen()];
			}
			int cnt = 0;
			// loop on latitude and longitude
			for (int ln = 0; ln < lonAxis.getLen(); ln++) {
				if (firstTime) {
					progress.setPercentComplete(100.0 * ((double)ln / (double)lonAxis.getLen()));
				}
				else {
					mContents.setPercentComplete(100.0 * ((double)ln / (double)lonAxis.getLen()));
				}
				boolean first = true;
				for (int lt = 0; lt < latAxis.getLen(); lt++) {
					float latCtr = 0.0f;
					float lonCtr = 0.0f;

					if (ptsHaveBeenCached) {
						latCtr = savedLats[i][lt];
						lonCtr = savedLons[i][ln];
					}
					else {
						try {
							// array optimization needed
							latCtr = (float)latma.getDouble(new int[] { lt });
							savedLats[i][lt] = latCtr;
						}
						catch (Exception ex) {
							// ex.printStackTrace();
							System.out.println("at P3");
							continue;
						}

						try {
							// array optimization needed
							lonCtr = (float)lonma.getDouble(new int[] { ln });
							savedLons[i][ln] = lonCtr;
						}
						catch (Exception ex) {
							System.out.println("at P4");
							continue;
						}
					}

					// plot the points
					if (LLKeep((double)latCtr, (double)lonCtr)) {
						// compute corners of polygon
						float lonLeft = lonCtr - 2 * delta;
						float lonRt = lonCtr;
						float latBott = latCtr;
						float latTop = latCtr + 2 * delta;
						// if (lonLeft > 180)
						// lonLeft = lonLeft - 360;
						// if (lonRt > 180)
						// lonRt = lonRt - 360;

						// get the value
						float zVal = 0.0f;
						if (ptsHaveBeenCached) {
							zVal = savedVals[i][ln][lt];
						}
						else {
							try {
								// array optimization needed
								zVal = (float)zma.getDouble(new int[] { lt, ln });
								savedVals[i][ln][lt] = zVal;
							}
							catch (Exception ex) {
								System.out.println("at P5");
							}
						}

						// choose a color for this z value
						Color currColor = mMapSpec.getBathyColorBar().getEtopoColor(zVal);

						if (first) {
							uvUL = transformLL(latTop, lonLeft);
							uvUR = transformLL(latTop, lonRt);
							first = false;
						}
						else {
							uvUL.u = oldULu;
							uvUL.v = oldULv;
							uvUR.u = oldURu;
							uvUR.v = oldURv;
						}

						// uvCtr = transformLL(latCtr, lonCtr);
						uvLL = transformLL(latBott, lonLeft);
						uvLR = transformLL(latBott, lonRt);

						oldULu = uvLL.u;
						oldULv = uvLL.v;
						oldURu = uvLR.u;
						oldURv = uvLR.v;

						// uvCtr = Scaler(uvCtr.u, uvCtr.v);
						uvLL = mapScaler(uvLL.u, uvLL.v);
						uvLR = mapScaler(uvLR.u, uvLR.v);
						uvUL = mapScaler(uvUL.u, uvUL.v);
						uvUR = mapScaler(uvUR.u, uvUR.v);

						double uDiff = Math.abs(uvUR.u - uvUL.u);
						double vDiff = Math.abs(uvUR.v - uvLR.v);

						if (uDiff > 25 || vDiff > 25) {
							continue;
							// System.out.println("latBott = " + latBott + " lonLeft = " +
							// lonLeft);
							// System.out.println("latTop = " + latTop + " lonRt = " + lonRt);
						}

						g.setColor(currColor);
						// if (uDiff < 1.0f && vDiff < 1.0f && mMapSpec.getProjection() <
						// JOAConstants.NORTHPOLEPROJECTION)
						// g.drawLine((int)uvUL.u, (int)uvUL.v, (int)uvLR.u, (int)uvLR.v);
						// else {
						xpoints[0] = (int)uvLL.u;
						xpoints[1] = (int)uvUL.u;
						xpoints[2] = (int)uvUR.u;
						xpoints[3] = (int)uvLR.u;
						ypoints[0] = (int)uvLL.v;
						ypoints[1] = (int)uvUL.v;
						ypoints[2] = (int)uvUR.v;
						ypoints[3] = (int)uvLR.v;
						cnt++;
						g.fillPolygon(xpoints, ypoints, 4);
						// }
					}
				}
			}
			// System.out.println("num polys = " + cnt);

			if (firstTime) {
				progress.setPercentComplete(100.0);
				progress.dispose();
			}
			else {
				mContents.reset();
			}
		}
	}
	public void drawMaskBathy(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;
		ProgressDialog progress = null;
		float delta = 0.5f;

		// get array of etopo filenames
		ptsHaveBeenCached = true;

		// have the etopo selections changed?
		if (oldEtopoFile == null) {
			// first time--haven't changes
			oldEtopoFile = new String[mMapSpec.getNumEtopoFiles()];
			for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
				oldEtopoFile[i] = new String(mMapSpec.getEtopoFile(i));
			}
		}
		else {
			if (oldEtopoFile.length != mMapSpec.getNumEtopoFiles()) {
				// number of etopo files has changed
				ptsHaveBeenCached = false;
			}
			else {
				// number has stayed the same--test whether they are the same
				// etopo files
				for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
					if (!oldEtopoFile[i].equalsIgnoreCase(mMapSpec.getEtopoFile(i))) {
						// etopo file changed--reset to new setting
						oldEtopoFile = null;
						oldEtopoFile = new String[mMapSpec.getNumEtopoFiles()];
						for (int ii = 0; ii < mMapSpec.getNumEtopoFiles(); ii++) {
							oldEtopoFile[ii] = new String(mMapSpec.getEtopoFile(ii));
						}
						ptsHaveBeenCached = false;
						break;
					}
				}
			}
		}

		for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
			try {
				if (savedLats[i] != null) {
					continue;
				}
			}
			catch (Exception ex) {
				savedLats = new float[mMapSpec.getNumEtopoFiles()][];
				savedLons = new float[mMapSpec.getNumEtopoFiles()][];
				savedVals = new float[mMapSpec.getNumEtopoFiles()][][];
				ptsHaveBeenCached = false;
				break;
			}
		}

		int[] xpoints = new int[4];
		int[] ypoints = new int[4];
		for (int i = 0; i < mMapSpec.getNumEtopoFiles(); i++) {
			String mFilename = mMapSpec.getEtopoFile(i);
			mETOPODB = null;
			if (firstTime) {
				if (progress != null) {
					progress.dispose();
				}
				progress = null;
				progress = new ProgressDialog(mFileViewer, "Plotting Etopo Data in " + mFilename, Color.blue, Color.white);
				progress.setVisible(true);
			}
			else {
				mContents.reset();
			}

			if (mFilename.indexOf("60") >= 0) {
				delta = 0.5f;
			}
			else if (mFilename.indexOf("20") >= 0) {
				delta = 0.1667f;
			}
			else if (mFilename.indexOf("5") >= 0) {
				delta = 0.0417f;
			}
			else if (mFilename.indexOf("2") >= 0) {
				delta = 0.01667f;
			}
			else if (mFilename.indexOf(".5") >= 0) {
				delta = 4.1667e-03f;
			}

			if (mETOPODB == null) {
				// get the etopo datafile
				File etopoFile = null;
				try {
					etopoFile = JOAFormulas.getSupportFile(mFilename);
				}
				catch (IOException ex) {
					// present an error dialog
					return;
				}
				String dir = etopoFile.getParent();

				EpicPtrs ptrDB = new EpicPtrs();

				// create a pointer
				EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "ETOPO Import", "ETOPO", "na", "na", -99, -99,
				    new gov.noaa.pmel.util.GeoDate(), -99, -99, null, mFilename, dir, null);

				// set the data of ptrDB to this one entry
				ptrDB.setFile(etopoFile);
				ptrDB.setData(epPtr);

				// create a database
				PointerDBIterator pdbi = ptrDB.iterator();
				EPSDbase etopoDB = new EPSDbase(pdbi, false);

				// get the database
				EPSDBIterator dbItor = etopoDB.iterator(false);

				try {
					mETOPODB = (Dbase)dbItor.getElement(0);
				}
				catch (Exception ex) {
				}
			}

			// latitude axis
			Axis latAxis = mETOPODB.getAxis("Y");
			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("y");
			}

			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("lat");
			}
			if (latAxis == null) {
				latAxis = mETOPODB.getAxis("latitude");
			}

			MultiArray latma = latAxis.getData();

			// longitude axis
			Axis lonAxis = mETOPODB.getAxis("X");
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("lon");
			}
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("x");
			}
			if (lonAxis == null) {
				lonAxis = mETOPODB.getAxis("longitude");
			}

			MultiArray lonma = lonAxis.getData();

			Vector<?> vars = mETOPODB.getMeasuredVariables(false);
			EPSVariable rose = null;
			int roseVar = 0;
			for (int b = 0; b < vars.size(); b++) {
				EPSVariable var = (EPSVariable)vars.elementAt(b);

				if (var.getName().trim().toUpperCase().indexOf("ROSE") >= 0) {
					roseVar = b;
				}
			}

			rose = (EPSVariable)vars.elementAt(roseVar);
			MultiArray zma = rose.getData();

			UVCoordinate uvUL = null;
			UVCoordinate uvUR = null;
			UVCoordinate uvLL = null;
			UVCoordinate uvLR = null;
			double oldULu = 0.0;
			double oldULv = 0.0;
			double oldURu = 0.0;
			double oldURv = 0.0;
			// double[] ulu = new double[lonAxis.getLen()];
			// double[] ulv = new double[lonAxis.getLen()];
			// double[] oldulu = new double[lonAxis.getLen()];
			// double[] oldulv = new double[lonAxis.getLen()];
			if (!ptsHaveBeenCached) {
				savedLats[i] = new float[latAxis.getLen()];
				savedLons[i] = new float[lonAxis.getLen()];
				savedVals[i] = new float[lonAxis.getLen()][latAxis.getLen()];
			}
			int cnt = 0;
			// loop on latitude and longitude
			for (int ln = 0; ln < lonAxis.getLen(); ln++) {
				if (firstTime) {
					progress.setPercentComplete(100.0 * ((double)ln / (double)lonAxis.getLen()));
				}
				else {
					mContents.setPercentComplete(100.0 * ((double)ln / (double)lonAxis.getLen()));
				}
				boolean first = true;
				for (int lt = 0; lt < latAxis.getLen(); lt++) {
					float latCtr = 0.0f;
					float lonCtr = 0.0f;

					if (ptsHaveBeenCached) {
						latCtr = savedLats[i][lt];
						lonCtr = savedLons[i][ln];
					}
					else {
						try {
							// array optimization needed
							latCtr = (float)latma.getDouble(new int[] { lt });
							savedLats[i][lt] = latCtr;
						}
						catch (Exception ex) {
							// ex.printStackTrace();
							System.out.println("at P3");
							continue;
						}

						try {
							// array optimization needed
							lonCtr = (float)lonma.getDouble(new int[] { ln });
							savedLons[i][ln] = lonCtr;
						}
						catch (Exception ex) {
							System.out.println("at P4");
							continue;
						}
					}

					// plot the points
					if (LLKeep((double)latCtr, (double)lonCtr)) {
						// compute corners of polygon
						float lonLeft = lonCtr - 2 * delta;
						float lonRt = lonCtr;
						float latBott = latCtr;
						float latTop = latCtr + 2 * delta;
						// if (lonLeft > 180)
						// lonLeft = lonLeft - 360;
						// if (lonRt > 180)
						// lonRt = lonRt - 360;

						// get the value
						float zVal = 0.0f;
						if (ptsHaveBeenCached) {
							zVal = savedVals[i][ln][lt];
						}
						else {
							try {
								// array optimization needed
								zVal = (float)zma.getDouble(new int[] { lt, ln });
								savedVals[i][ln][lt] = zVal;
							}
							catch (Exception ex) {
								System.out.println("at P5");
							}
						}

						// choose a color for this z value
						Color currColor = mMapSpec.getMaskColorBar().getEtopoColor(zVal);

						if (first) {
							uvUL = transformLL(latTop, lonLeft);
							uvUR = transformLL(latTop, lonRt);
							first = false;
						}
						else {
							uvUL.u = oldULu;
							uvUL.v = oldULv;
							uvUR.u = oldURu;
							uvUR.v = oldURv;
						}

						// uvCtr = transformLL(latCtr, lonCtr);
						uvLL = transformLL(latBott, lonLeft);
						uvLR = transformLL(latBott, lonRt);

						oldULu = uvLL.u;
						oldULv = uvLL.v;
						oldURu = uvLR.u;
						oldURv = uvLR.v;

						// uvCtr = Scaler(uvCtr.u, uvCtr.v);
						uvLL = mapScaler(uvLL.u, uvLL.v);
						uvLR = mapScaler(uvLR.u, uvLR.v);
						uvUL = mapScaler(uvUL.u, uvUL.v);
						uvUR = mapScaler(uvUR.u, uvUR.v);

						double uDiff = Math.abs(uvUR.u - uvUL.u);
						double vDiff = Math.abs(uvUR.v - uvLR.v);

						if (uDiff > 25 || vDiff > 25) {
							continue;
							// System.out.println("latBott = " + latBott + " lonLeft = " +
							// lonLeft);
							// System.out.println("latTop = " + latTop + " lonRt = " + lonRt);
						}

						g.setColor(currColor);
						// if (uDiff < 1.0f && vDiff < 1.0f && mMapSpec.getProjection() <
						// JOAConstants.NORTHPOLEPROJECTION)
						// g.drawLine((int)uvUL.u, (int)uvUL.v, (int)uvLR.u, (int)uvLR.v);
						// else {
						xpoints[0] = (int)uvLL.u;
						xpoints[1] = (int)uvUL.u;
						xpoints[2] = (int)uvUR.u;
						xpoints[3] = (int)uvLR.u;
						ypoints[0] = (int)uvLL.v;
						ypoints[1] = (int)uvUL.v;
						ypoints[2] = (int)uvUR.v;
						ypoints[3] = (int)uvLR.v;
						cnt++;
						g.fillPolygon(xpoints, ypoints, 4);
						// }
					}
				}
			}
			// System.out.println("num polys = " + cnt);

			if (firstTime) {
				progress.setPercentComplete(100.0);
				progress.dispose();
			}
			else {
				mContents.reset();
			}
		}
	}


	public void drawFilledBathyGCS(Graphics g) {
		String[] files = new String[6];
		files[0] = "etopo5_arc.nc";
		files[1] = "etopo5_ind.nc";
		files[2] = "etopo5_natl.nc";
		files[3] = "etopo5_npac.nc";
		files[4] = "etopo5_satl.nc";
		files[5] = "etopo5_spac.nc";
		double[] lonOffset = { 0, 0, -360, 0, -360, 0 };

		for (int e = 0; e < files.length; e++) {
			ProgressDialog progress = null;
			if (firstTime) {
				progress = new ProgressDialog(mFileViewer, "Plotting Etopo Data..." + files[e], Color.blue, Color.white);
				progress.setVisible(true);
			}

			// if (oldEtopoFile == null)
			// oldEtopoFile = new String(mMapSpec.getEtopoFile());
			// else if (!oldEtopoFile.equalsIgnoreCase(mMapSpec.getEtopoFile())) {
			// etopo file changed
			// oldEtopoFile = new String(mMapSpec.getEtopoFile());
			mETOPODB = null;
			// }
			String mFilename = files[e]; // mMapSpec.getEtopoFile();

			double delta = 0.5;
			if (mFilename.indexOf("60") >= 0) {
				delta = 0.5;
			}
			else if (mFilename.indexOf("20") >= 0) {
				delta = 0.1667;
			}
			else if (mFilename.indexOf("5") >= 0) {
				delta = 0.0417;
			}

			if (mETOPODB == null) {
				// get the etopo datafile
				File etopoFile = null;
				try {
					etopoFile = JOAFormulas.getSupportFile(files[e]);
				}
				catch (IOException ex) {
					// present an error dialog
					return;
				}
				String dir = etopoFile.getParent();

				EpicPtrs ptrDB = new EpicPtrs();

				// create a pointer
				EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "ETOPO Import", "ETOPO", "na", "na", -99, -99,
				    new GeoDate(), -99, -99, null, mFilename, dir, null);

				// set the data of ptrDB to this one entry
				ptrDB.setFile(etopoFile);
				ptrDB.setData(epPtr);

				// create a database
				PointerDBIterator pdbi = ptrDB.iterator();
				EPSDbase etopoDB = new EPSDbase(pdbi, false);

				// get the database
				EPSDBIterator dbItor = etopoDB.iterator(false);

				try {
					mETOPODB = (Dbase)dbItor.getElement(0);
				}
				catch (Exception ex) {
				}
			}

			// latitude axis
			Axis latAxis = mETOPODB.getAxis("Y");
			if (latAxis == null) {
				; // throw something
			}
			MultiArray latma = latAxis.getData();

			// longitude axis
			Axis lonAxis = mETOPODB.getAxis("X");
			if (lonAxis == null) {
				; // throw something
			}
			MultiArray lonma = lonAxis.getData();

			Vector<?> vars = mETOPODB.getMeasuredVariables(false);
			EPSVariable rose = (EPSVariable)vars.elementAt(0);
			MultiArray zma = rose.getData();

			// loop on latitude and longitude
			for (int ln = 0; ln < lonAxis.getLen(); ln++) {
				if (firstTime) {
					progress.setPercentComplete(100.0 * ((double)ln / (double)lonAxis.getLen()));
				}

				for (int lt = 0; lt < latAxis.getLen(); lt++) {
					double latCtr = 0.0;
					try {
						// array optimization
						latCtr = latma.getDouble(new int[] { lt });
					}
					catch (Exception ex) {
						System.out.println("at P3");
						continue;
					}

					double lonCtr = 0.0;
					try {
						// array optimization
						lonCtr = lonma.getDouble(new int[] { ln });
						if (lonCtr >= 0) {
							lonCtr = lonCtr + lonOffset[e];
						}
					}
					catch (Exception ex) {
						System.out.println("at P4");
						continue;
					}

					// plot the points
					// if (LLKeep(latCtr, lonCtr)) {
					// compute corners of polygon
					double lonLeft = lonCtr - delta;
					double lonRt = lonCtr + delta;
					double latBott = latCtr - delta;
					double latTop = latCtr + delta;

					// get the value
					double zVal = 0.0;
					try {
						// array optimization
						zVal = zma.getDouble(new int[] { lt, ln });
					}
					catch (Exception ex) {
					}
					// choose a color for this z value
					Color currColor = mMapSpec.getBathyColorBar().getColor(zVal);

					UVCoordinate uvLL = transformLL(latBott, lonLeft);
					UVCoordinate uvLR = transformLL(latBott, lonRt);
					UVCoordinate uvUL = transformLL(latTop, lonLeft);
					UVCoordinate uvUR = transformLL(latTop, lonRt);

					double uDiff = Math.abs(uvUR.u - uvUL.u);
					double vDiff = Math.abs(uvUR.v - uvUL.v);

					if (uDiff > 100 || vDiff > 100) {
						continue;
					}

					g.setColor(currColor);
					// if (uDiff > 0.5 || vDiff > 0.5) {
					int[] xpoints = { (int)uvLL.u, (int)uvUL.u, (int)uvUR.u, (int)uvLR.u };
					int[] ypoints = { (int)uvLL.v, (int)uvUL.v, (int)uvUR.v, (int)uvLR.v };
					g.fillPolygon(xpoints, ypoints, 4);
					// }
					// else {
					// g.drawLine((int)uvCtr.u, (int)uvCtr.v, (int)uvCtr.u, (int)uvCtr.v);
					// }
					// }

				}
			}
			if (firstTime) {
				progress.setPercentComplete(100.0);
				progress.dispose();
			}
		}
	}

	// draw the overlay contours using zGrid
	public void plotIsoContours(Graphics g, boolean computeContours) {
		if (!computeContours && mCurrContours != null && mCurrContours.getContourLevels().size() > 0) {
			drawContours(g);
			return;
		}

		// need the lat lon range of the stations
		double lonr = mMapSpec.getLonRt();
		double lonl = mMapSpec.getLonLft();
		boolean crosses180 = lonl > 0 && lonr < 0;
		double x1 = mMapSpec.getLonLft();
		double y1 = mMapSpec.getLatMin();
		if (crosses180) {
			lonr += 360;
		}
		double dx = Math.abs((lonr - lonl) / mMapSpec.getNX());
		double dy = (mMapSpec.getLatMax() - mMapSpec.getLatMin()) / mMapSpec.getNY();
		double[] xp = null;
		double[] yp = null;
		double[] zp = null;

		// compute the number of z pts (stns)
		int stnCnt = 0;
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							stnCnt++;
						}
					}
				}
			}
		}

		xp = new double[stnCnt];
		yp = new double[stnCnt];
		zp = new double[stnCnt];

		for (int i = 0; i < stnCnt; i++) {
			xp[i] = Double.NaN;
			yp[i] = Double.NaN;
			zp[i] = Double.NaN;
		}

		stnCnt = 0;
		if (mMapSpec.isIsoContourMinSurfaceValue()) {
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					// get param positions for this section
					int isoVarPos = sech.getVarPos(mFileViewer.mAllProperties[mMapSpec.getIsoContourVarCode()].getVarLabel(),
					    false);
					int surfVarPos = sech.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);

					// collect up the station points
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}

						double lat = sh.mLat;
						double lon = sh.mLon;
						double valOnSurface = Double.NaN;
						double min = 99999999;

						if (LLKeep(lat, lon)) {
							UVCoordinate uv = transformLL(lat, lon);
							uv = mapScaler(uv.u, uv.v);
							if (uv.mInWind) {
								if (crosses180 && lon < 0) {
									lon += 360;
								}
								xp[stnCnt] = lon;
								yp[stnCnt] = lat;
								zp[stnCnt] = Double.NaN;

								// set the value of z
								// loop on the bottles
								if (mMapSpec.isIsoContourBottomUpSearch()) {
									for (int b = sh.mNumBottles; b > 0; b--) {
										Bottle bh = (Bottle)sh.mBottles.elementAt(b);
										boolean keepBottle;
										if (mFileViewer.mObsFilterActive) {
											keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh);
										}
										else {
											keepBottle = true;
										}

										if (keepBottle) {
											double surfVal = bh.mDValues[surfVarPos];
											double isoVal = bh.mDValues[isoVarPos];

											if (isoVal == JOAConstants.MISSINGVALUE) {
												continue;
											}

											if (surfVal < min) {
												min = surfVal;
												valOnSurface = isoVal;
											}
										}
									}
								}
								else {
									for (int b = 0; b < sh.mNumBottles; b++) {
										Bottle bh = (Bottle)sh.mBottles.elementAt(b);
										boolean keepBottle;
										if (mFileViewer.mObsFilterActive) {
											keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh);
										}
										else {
											keepBottle = true;
										}

										if (keepBottle) {
											double surfVal = bh.mDValues[surfVarPos];
											double isoVal = bh.mDValues[isoVarPos];

											if (isoVal == JOAConstants.MISSINGVALUE) {
												continue;
											}

											if (surfVal < min) {
												min = surfVal;
												valOnSurface = isoVal;
											}
										}
									}
								}

								zp[stnCnt] = valOnSurface;
								stnCnt++;
							}
						}
					}
				}
			}
		}
		else if (mMapSpec.isIsoContourMaxSurfaceValue()) {
			// plot at the minimum
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					// get param positions for this section
					int isoVarPos = sech.getVarPos(mFileViewer.mAllProperties[mMapSpec.getIsoContourVarCode()].getVarLabel(),
					    false);
					int surfVarPos = sech.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);

					// draw the station points
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}

						double lat = sh.mLat;
						double lon = sh.mLon;
						double valOnSurface = Double.NaN;
						double max = -99999999;
						if (LLKeep(lat, lon)) {
							UVCoordinate uv = transformLL(lat, lon);
							uv = mapScaler(uv.u, uv.v);
							if (uv.mInWind) {
								if (crosses180 && lon < 0) {
									lon += 360;
								}
								xp[stnCnt] = lon;
								yp[stnCnt] = lat;
								zp[stnCnt] = Double.NaN;
								valOnSurface = Double.NaN;

								if (mMapSpec.isIsoContourBottomUpSearch()) {
									for (int b = sh.mNumBottles; b > 0; b--) {
										Bottle bh = (Bottle)sh.mBottles.elementAt(b);
										double surfVal = bh.mDValues[surfVarPos];
										double isoVal = bh.mDValues[isoVarPos];
										if (isoVal == JOAConstants.MISSINGVALUE) {
											continue;
										}

										if (surfVal > max) {
											max = surfVal;
											valOnSurface = isoVal;
										}
									}

								}
								else {
									for (int b = 0; b < sh.mNumBottles; b++) {
										Bottle bh = (Bottle)sh.mBottles.elementAt(b);
										double surfVal = bh.mDValues[surfVarPos];
										double isoVal = bh.mDValues[isoVarPos];
										if (isoVal == JOAConstants.MISSINGVALUE) {
											continue;
										}

										if (surfVal > max) {
											max = surfVal;
											valOnSurface = isoVal;
										}
									} // for b
								} // else
								zp[stnCnt] = valOnSurface;
								stnCnt++;
							}
						} // if LLKeep
					} // for stc
				} // for sec
			} // for fc
		} // else if
		else {
			// find the closest matching surface level for the current surface value
			double delta = 10000000;
			double valOnSurface;
			NewInterpolationSurface isoSurface = mMapSpec.getIsoContourSurface();
			int numSurfLevels = isoSurface.getNumLevels();
			for (int i = 0; i < numSurfLevels; i++) {
				double valOfSurface = isoSurface.getValue(i);
				double d = Math.abs(mMapSpec.getContourIsoSurfaceValue() - valOfSurface);
				if (d < delta) {
					delta = d;
					mCurrContourIsoSurfaceLevel = i;
				}
			}
			double refValue = 0.0;

			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					// get param positions for this section
					int isoVarPos = sech.getVarPos(mFileViewer.mAllProperties[mMapSpec.getIsoContourVarCode()].getVarLabel(),
					    false);
					int surfVarPos = sech.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);

					if (isoVarPos < 0 || surfVarPos < 0) {
						continue;
					}

					// draw the station points
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}

						if (mMapSpec.isIsoContourReferenced()) {
							double[] sVals = new double[sh.mNumBottles];
							double[] mVals = new double[sh.mNumBottles];

							for (int b = 0; b < sh.mNumBottles; b++) {
								Bottle bh = (Bottle)sh.mBottles.elementAt(b);
								sVals[b] = bh.mDValues[surfVarPos];
								mVals[b] = bh.mDValues[isoVarPos];
							}

							// dereference
							refValue = JOAFormulas.dereferenceStation(sh.mNumBottles, mMapSpec.getIsoContourReferenceLevel(), sVals,
							    mVals);
						}

						double lat = sh.mLat;
						double lon = sh.mLon;
						if (LLKeep(lat, lon)) {
							UVCoordinate uv = transformLL(lat, lon);
							uv = mapScaler(uv.u, uv.v);
							if (uv.mInWind) {
								if (crosses180 && lon < 0) {
									lon += 360;
								}
								xp[stnCnt] = lon;
								yp[stnCnt] = lat;
								valOnSurface = Double.NaN;

								// loop on the bottles
								if (mMapSpec.isIsoContourBottomUpSearch()) {
									for (int b = sh.mNumBottles - 1; b > 0; b--) {
										Bottle bh1 = (Bottle)sh.mBottles.elementAt(b);
										Bottle bh2 = (Bottle)sh.mBottles.elementAt(b - 1);

										// test obs filter
										boolean keepBottle1;
										boolean keepBottle2;
										if (mFileViewer.mObsFilterActive) {
											keepBottle1 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh1);
										}
										else {
											keepBottle1 = true;
										}

										if (mFileViewer.mObsFilterActive) {
											keepBottle2 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh2);
										}
										else {
											keepBottle2 = true;
										}

										if (keepBottle1 && keepBottle2) {
											double surfVal1 = bh1.mDValues[surfVarPos];
											double surfVal2 = bh2.mDValues[surfVarPos];
											if (surfVal1 == JOAConstants.MISSINGVALUE || surfVal2 == JOAConstants.MISSINGVALUE) {
												continue;
											}

											if ((mMapSpec.getContourIsoSurfaceValue() >= surfVal1 && mMapSpec.getContourIsoSurfaceValue() <= surfVal2)
											    || (mMapSpec.getContourIsoSurfaceValue() <= surfVal1 && mMapSpec.getContourIsoSurfaceValue() >= surfVal2)) {
												// found bounding bottles, get the vals of the parameter
												// on the surface
												double isoVal1 = bh1.mDValues[isoVarPos];
												double isoVal2 = bh2.mDValues[isoVarPos];
												if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
													if (!mMapSpec.isIsoContourLocalInterpolation()) {
														valOnSurface = Double.NaN;
													}
													else {
														int maxCnt = mMapSpec.getIsoContourMaxInterpDistance();
														// look for non-missing observation
														if (isoVal2 == JOAConstants.MISSINGVALUE) {
															// look above (reverse this for top down search)
															int cnt = 1;
															while (isoVal2 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
																if (b - cnt < 0) {
																	break;
																}
																Bottle bhAbove = (Bottle)sh.mBottles.elementAt(b - cnt);
																surfVal2 = bhAbove.mDValues[surfVarPos];
																isoVal2 = bhAbove.mDValues[isoVarPos];
																cnt++;
															}
														}
														if (isoVal1 == JOAConstants.MISSINGVALUE) {
															// look below (reverse this for top down search)
															int cnt = 1;
															while (isoVal1 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
																if (b + cnt > sh.mNumBottles) {
																	break;
																}
																Bottle bhBelow = (Bottle)sh.mBottles.elementAt(b + cnt);
																surfVal1 = bhBelow.mDValues[surfVarPos];
																isoVal1 = bhBelow.mDValues[isoVarPos];
																cnt++;
															}
														}
													}
													if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
														valOnSurface = Double.NaN;
													}
													else {
														// interp new value
														double denom = Math.abs(surfVal2 - surfVal1);
														double num1 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal1);
														double num2 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal2);
														double frac = num1 / denom;

														// Triangle inequality.
														if (num1 > denom && num1 > num2) {
															frac = 1.0;
														}
														if (num2 > denom && num2 > num1) {
															frac = 0.0;
														}

														valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));
														break;
													}
												}
												else {
													// interp new value
													double denom = Math.abs(surfVal2 - surfVal1);
													double num1 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal1);
													double num2 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal2);
													double frac = num1 / denom;

													// Triangle inequality.
													if (num1 > denom && num1 > num2) {
														frac = 1.0;
													}
													if (num2 > denom && num2 > num1) {
														frac = 0.0;
													}

													valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));
													break;
												}
											}
										}
										else {
											valOnSurface = Double.NaN;
										}
									}

									// dereference if neccessary
									if (mMapSpec.isIsoContourReferenced()) {
										if (refValue != JOAConstants.MISSINGVALUE) {
											zp[stnCnt] = valOnSurface - refValue;
										}
										else {
											zp[stnCnt] = JOAConstants.MISSINGVALUE;
										}
									}
									else {
										zp[stnCnt] = valOnSurface;
									}

									stnCnt++;
								}
								else {
									for (int b = 0; b < sh.mNumBottles - 1; b++) {
										Bottle bh1 = (Bottle)sh.mBottles.elementAt(b);
										Bottle bh2 = (Bottle)sh.mBottles.elementAt(b + 1);

										// test observation filter
										boolean keepBottle1;
										boolean keepBottle2;
										if (mFileViewer.mObsFilterActive) {
											keepBottle1 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh1);
										}
										else {
											keepBottle1 = true;
										}

										if (mFileViewer.mObsFilterActive) {
											keepBottle2 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh2);
										}
										else {
											keepBottle2 = true;
										}

										if (keepBottle1 && keepBottle2) {
											double surfVal1 = bh1.mDValues[surfVarPos];
											double surfVal2 = bh2.mDValues[surfVarPos];
											if (surfVal1 == JOAConstants.MISSINGVALUE || surfVal2 == JOAConstants.MISSINGVALUE) {
												continue;
											}
											if ((mMapSpec.getContourIsoSurfaceValue() >= surfVal1 && mMapSpec.getContourIsoSurfaceValue() <= surfVal2)
											    || (mMapSpec.getContourIsoSurfaceValue() <= surfVal1 && mMapSpec.getContourIsoSurfaceValue() >= surfVal2)) {
												// found bounding bottles, get the vals of the parameter
												// on the surface
												double isoVal1 = bh1.mDValues[isoVarPos];
												double isoVal2 = bh2.mDValues[isoVarPos];

												if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
													if (!mMapSpec.isIsoContourLocalInterpolation()) {
														valOnSurface = Double.NaN;
													}
													else {
														int maxCnt = mMapSpec.getIsoContourMaxInterpDistance();
														// look for non-missing observation
														if (isoVal1 == JOAConstants.MISSINGVALUE) {
															// look above
															int cnt = 1;
															while (isoVal1 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
																if (b - cnt < 0) {
																	break;
																}
																Bottle bhAbove = (Bottle)sh.mBottles.elementAt(b - cnt);
																surfVal1 = bhAbove.mDValues[surfVarPos];
																isoVal1 = bhAbove.mDValues[isoVarPos];
																cnt++;
															}
														}
														if (isoVal2 == JOAConstants.MISSINGVALUE) {
															// look below
															int cnt = 1;
															while (isoVal2 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
																if (b + cnt > sh.mNumBottles) {
																	break;
																}
																Bottle bhBelow = (Bottle)sh.mBottles.elementAt(b + cnt);
																surfVal2 = bhBelow.mDValues[surfVarPos];
																isoVal2 = bhBelow.mDValues[isoVarPos];
																cnt++;
															}
														}
													}

													if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
														valOnSurface = Double.NaN;
													}
													else {
														// interp new value
														double denom = Math.abs(surfVal2 - surfVal1);
														double num1 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal1);
														double num2 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal2);
														double frac = num1 / denom;

														// Triangle inequality.
														if (num1 > denom && num1 > num2) {
															frac = 1.0;
														}
														if (num2 > denom && num2 > num1) {
															frac = 0.0;
														}

														valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));
														break;
													}
												}
												else {
													// interp new value
													double denom = Math.abs(surfVal2 - surfVal1);
													double num1 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal1);
													double num2 = Math.abs(mMapSpec.getContourIsoSurfaceValue() - surfVal2);
													double frac = num1 / denom;

													// Triangle inequality.
													if (num1 > denom && num1 > num2) {
														frac = 1.0;
													}
													if (num2 > denom && num2 > num1) {
														frac = 0.0;
													}

													valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));
													break;
												}
											}
										}
										else {
											valOnSurface = Double.NaN;
										}
									}

									if (mMapSpec.isIsoContourReferenced()) {
										if (refValue != JOAConstants.MISSINGVALUE && valOnSurface != Double.NaN) {
											zp[stnCnt] = valOnSurface - refValue;
										}
										else {
											zp[stnCnt] = JOAConstants.MISSINGVALUE;
										}
									}
									else {
										zp[stnCnt] = valOnSurface;
									}
									stnCnt++;
								} // else
							} // if in wind
						} // mUseStn
					} // for stc
				} // for sec
			} // for fc
		} // else

		ZGrid grid = new ZGrid(mMapSpec.getNX(), mMapSpec.getNY(), x1, y1, dx, dy, xp, yp, zp, mMapSpec.getCAY(), mMapSpec
		    .getNRng(), mMapSpec.isMaskCoast());
		// System.out.println(mMapSpec.getNX() + "," + mMapSpec.getNY());
		//
		// System.out.println(" x1 = " + x1);
		// System.out.println(" dx = " + dx);
		// System.out.println(" y1 = " + y1);
		// System.out.println(" dy = " + dy);
		// System.out.println(" xp = " + xp.length);
		// System.out.println(" yp = " + yp.length);
		// System.out.println(" zp = " + zp.length);
		// System.out.println(" x = " + grid.getXRange());
		// System.out.println(" y = " + grid.getYRange());
		// System.out.println(" z = " + grid.getZRange());

		// create a contour levels array
		ContourLevels cLevels = new ContourLevels();
		NewColorBar ncb = mMapSpec.getOverlayContoursColorBar();
		int ncl = ncb.getNumLevels();

		Color contColor = Color.black;
		contColor = (Color)mMapSpec.getIsoContourColor();

		// todo: built in colorbars have to have the contour attribute defined
		int mPlotEveryNthOvlContour = 1;
		if (mMapSpec.getPlotEveryNthContour() > 0) {
			mPlotEveryNthOvlContour = mMapSpec.getPlotEveryNthContour();
		}

		for (int k = 0; k < ncl; k += mPlotEveryNthOvlContour) {
			ContourLineAttribute attr = ncb.getContourAttributes(k);
			if (attr == null) {
				if (ncb.getDoubleValue(k) < 0.0) {
					if (JOAConstants.THICKEN_CONTOUR_LINES) {
						ContourLineAttribute tattr = new ContourLineAttribute(ContourLineAttribute.DASHED);
						attr = new ContourLineAttribute(ContourLineAttribute.STROKE);
						attr.setWidth((float)(tattr.getWidth() * 1.5));
						attr.setWidthOverridden(true);
						attr.setDashArray(tattr.getDashArray());
						attr.setDashPhase(tattr.getDashPhase());
						attr.setDashArrayOverridden(true);
						attr.setDashPhaseOverridden(true);
					}
					else {
						attr = new ContourLineAttribute(ContourLineAttribute.DASHED);
					}
				}
				else if (ncb.getDoubleValue(k) > 0.0) {
					if (JOAConstants.THICKEN_CONTOUR_LINES) {
						ContourLineAttribute tattr = new ContourLineAttribute(ContourLineAttribute.SOLID);
						attr = new ContourLineAttribute(ContourLineAttribute.STROKE);
						attr.setWidth((float)(tattr.getWidth() * 1.5));
						attr.setWidthOverridden(true);
						attr.setDashArray(tattr.getDashArray());
						attr.setDashPhase(tattr.getDashPhase());
						attr.setDashArrayOverridden(true);
						attr.setDashPhaseOverridden(true);
					}
					else {
						attr = new ContourLineAttribute(ContourLineAttribute.SOLID);
					}
				}
				else {
					if (JOAConstants.THICKEN_CONTOUR_LINES) {
						ContourLineAttribute tattr = new ContourLineAttribute(ContourLineAttribute.HEAVY);
						attr = new ContourLineAttribute(ContourLineAttribute.STROKE);
						attr.setWidth((float)(tattr.getWidth() * 2.5));
						attr.setWidthOverridden(true);
						attr.setDashArray(tattr.getDashArray());
						attr.setDashPhase(tattr.getDashPhase());
						attr.setDashArrayOverridden(true);
						attr.setDashPhaseOverridden(true);
					}
					else {
						attr = new ContourLineAttribute(ContourLineAttribute.HEAVY);
					}
				}
				attr.setStyleOverridden(true);
				double labelSize = (double)JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE;
				attr.setLabelHeightP(labelSize);
				attr.setLabelHeightPOverridden(true);

				Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
				map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT);
				map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE));
				if (JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == Font.BOLD
				    || JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == (Font.BOLD | Font.ITALIC)) {
					map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
				}
				if (JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == Font.ITALIC
				    || JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == (Font.BOLD | Font.ITALIC)) {
					map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
				}
				map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
				Font lblFont = new Font(map);
				attr.setLabelFont(lblFont);
				attr.setLabelFontOverridden(true);
				if (contColor != null) {
					attr.setColor(contColor);
				}
				else {
					attr.setColor(ncb.getColorValue(k));
				}
				cLevels.addLevel(ncb.getDoubleValue(k), attr);
			}
			else {
				if (ncb.getDoubleValue(k) < 0.0) {
					if (JOAConstants.THICKEN_CONTOUR_LINES) {
						ContourLineAttribute tattr = new ContourLineAttribute(ContourLineAttribute.DASHED);
						attr = new ContourLineAttribute(ContourLineAttribute.STROKE);
						attr.setWidth((float)(tattr.getWidth() * 1.5));
						attr.setWidthOverridden(true);
						attr.setDashArray(tattr.getDashArray());
						attr.setDashPhase(tattr.getDashPhase());
						attr.setDashArrayOverridden(true);
						attr.setDashPhaseOverridden(true);
					}
					else {
						attr = new ContourLineAttribute(ContourLineAttribute.DASHED);
					}
				}
				else if (ncb.getDoubleValue(k) > 0.0) {
					if (JOAConstants.THICKEN_CONTOUR_LINES) {
						ContourLineAttribute tattr = new ContourLineAttribute(ContourLineAttribute.SOLID);
						attr = new ContourLineAttribute(ContourLineAttribute.STROKE);
						attr.setWidth((float)(tattr.getWidth() * 1.5));
						float[] newDashes = { 12.0f, 0.0f };
						attr.setWidthOverridden(true);
						attr.setDashArray(newDashes);
						attr.setDashPhase(0.0f);
						attr.setDashArrayOverridden(true);
						attr.setDashPhaseOverridden(true);
					}
					else {
						attr = new ContourLineAttribute(ContourLineAttribute.SOLID);
					}
				}
				else {
					if (JOAConstants.THICKEN_CONTOUR_LINES) {
						ContourLineAttribute tattr = new ContourLineAttribute(ContourLineAttribute.HEAVY);
						attr = new ContourLineAttribute(ContourLineAttribute.STROKE);
						attr.setWidth((float)(tattr.getWidth() * 2.5));
						float[] newDashes = { 12.0f, 0.0f };
						attr.setWidthOverridden(true);
						attr.setDashArray(newDashes);
						attr.setDashPhase(0.0f);
						attr.setDashArrayOverridden(true);
						attr.setDashPhaseOverridden(true);
					}
					else {
						attr = new ContourLineAttribute(ContourLineAttribute.HEAVY);
					}
				}
				attr.setStyleOverridden(true);
				double labelSize = (double)JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE;
				attr.setLabelHeightP(labelSize);
				attr.setLabelHeightPOverridden(true);
				attr.setLabelColor(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
				attr.setLabelColorOverridden(true);

				Hashtable<TextAttribute, Serializable> map = new Hashtable<TextAttribute, Serializable>();
				map.put(TextAttribute.FAMILY, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT);
				map.put(TextAttribute.SIZE, new Float(JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE));
				if (JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == Font.BOLD
				    || JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == (Font.BOLD | Font.ITALIC)) {
					map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
				}
				if (JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == Font.ITALIC
				    || JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE == (Font.BOLD | Font.ITALIC)) {
					map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
				}
				map.put(TextAttribute.FOREGROUND, JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR);
				Font lblFont = new Font(map);
				attr.setLabelFont(lblFont);
				attr.setLabelFontOverridden(true);
				if (contColor != null) {
					attr.setColor(contColor);
				}
				else {
					attr.setColor(ncb.getColorValue(k));
				}
				cLevels.addLevel(ncb.getDoubleValue(k), attr);
			}
		}

		// have SGT create the contour lines
		mCurrContours = new Contour(this, grid, cLevels, mMapSpec.getContourLabelPrec());
		mCurrContours.generateContourLines();

		// map contour coordinates to display units
		Vector<?> conts = mCurrContours.getContours();
		UVCoordinate uv = null;
		double[] xs = null;
		double[] ys = null;

		// System.out.println("x,y = " + pt.getX() + " " + pt.getY());
		// System.out.println("u,v = " + uv.u + " " + uv.v);
		for (int c = 0; c < conts.size(); c++) {
			Vector<?> cl = (Vector<?>)conts.elementAt(c);
			ContourLine cli = (ContourLine)cl;
			int len = cl.size();
			xs = new double[len];
			ys = new double[len];

			for (int i = 0; i < len; i++) {
				Object obj = cl.elementAt(i);
				gov.noaa.pmel.util.Point2D.Double pt = (gov.noaa.pmel.util.Point2D.Double)obj;
				if (pt.y == Double.NaN || pt.x == Double.NaN) {
					xs[i] = Double.NaN;
					ys[i] = Double.NaN;
				}
				else {
					uv = transformLL(pt.getY(), pt.getX());
					uv = mapScaler(uv.u, uv.v);
					xs[i] = uv.u;
					ys[i] = uv.v;
				}
			}
			cli.removeAllElements();

			for (int i = 0; i < len; i++) {
				cli.addPoint(xs[i], ys[i]);
			}
		}

		if (mMapSpec.isFilledIsoContours() || mMapSpec.isFilledStnContours()) {
			EnumeratedColorMap cmap = createColorMap(mMapSpec.getOverlayContoursColorBar());
			plotZGrid(g, grid, cLevels, cmap);
		}

		mCurrContours.generateContourLabels(g);
		drawContours(g);
	}

	private void plotZGrid(Graphics g, ZGrid inGrid, ContourLevels clevels, EnumeratedColorMap cmap) {
		double[] xs = inGrid.getXArray();
		double[] ys = inGrid.getYArray();
		double[] zs = inGrid.getZArray();
		int nx = inGrid.getXSize();
		int ny = inGrid.getYSize();
		double[] xt = new double[5];
		double[] yt = new double[5];
		double[] zt = new double[5];
		UVCoordinate uv = null;

		for (int i = 0; i < nx - 1; i++) {
			for (int j = 0; j < ny - 1; j++) {
				double ulLat = ys[j];
				double urLat = ys[j];
				double llLat = ys[j + 1];
				double lrLat = ys[j + 1];
				double ulLon = xs[i];
				double urLon = xs[i + 1];
				double llLon = xs[i];
				double lrLon = xs[i + 1];

				double ulZ = zs[j + i * ny];
				double urZ = zs[j + (i + 1) * ny];
				double lrZ = zs[j + 1 + (i + 1) * ny];
				double llZ = zs[j + 1 + i * ny];

				zt[0] = ulZ;
				zt[1] = urZ;
				zt[2] = lrZ;
				zt[3] = llZ;
				zt[4] = ulZ;

				if (ulLat == Double.NaN || ulLon == Double.NaN) {
					xt[0] = Double.NaN;
					yt[0] = Double.NaN;
				}
				else {
					uv = transformLL(ulLat, ulLon);
					uv = mapScaler(uv.u, uv.v);
					xt[0] = uv.u;
					yt[0] = uv.v;
				}

				if (urLat == Double.NaN || urLon == Double.NaN) {
					xt[1] = Double.NaN;
					yt[1] = Double.NaN;
				}
				else {
					uv = transformLL(urLat, urLon);
					uv = mapScaler(uv.u, uv.v);
					xt[1] = uv.u;
					yt[1] = uv.v;
				}

				if (lrLat == Double.NaN || lrLon == Double.NaN) {
					xt[2] = Double.NaN;
					yt[2] = Double.NaN;
				}
				else {
					uv = transformLL(lrLat, lrLon);
					uv = mapScaler(uv.u, uv.v);
					xt[2] = uv.u;
					yt[2] = uv.v;
				}

				if (llLat == Double.NaN || llLon == Double.NaN) {
					xt[3] = Double.NaN;
					yt[3] = Double.NaN;
				}
				else {
					uv = transformLL(llLat, llLon);
					uv = mapScaler(uv.u, uv.v);
					xt[3] = uv.u;
					yt[3] = uv.v;
				}

				xt[4] = xt[0];
				yt[4] = yt[0];

				fillSquare(g, xt, yt, zt, clevels, cmap);

			}
		}
	}

	private void fillSquare(Graphics g, double[] x, double[] y, double[] z, ContourLevels clevels, EnumeratedColorMap cmap) {
		int i, j, cindex, npoly, maxindex;
		double zlev, zlevp1, f;
		Color col;
		double[] xpoly = new double[20];
		double[] ypoly = new double[20];
		double zmin = Math.min(z[0], z[1]);
		double zmax = Math.max(z[0], z[1]);
		for (i = 2; i <= 3; i++) {
			zmin = Math.min(zmin, z[i]);
			zmax = Math.max(zmax, z[i]);
		}
		if (Double.isNaN(zmax))
			return;
		maxindex = clevels.getMaximumIndex();
		for (cindex = -1; cindex < maxindex; cindex++) {
			try {
				if (cindex == -1) {
					zlev = -Double.MAX_VALUE;
				}
				else {
					zlev = clevels.getLevel(cindex);
				}
				if (cindex == maxindex) {
					zlevp1 = Double.MAX_VALUE;
				}
				else {
					zlevp1 = clevels.getLevel(cindex + 1);
				}
			}
			catch (ContourLevelNotFoundException e) {
				System.out.println(e);
				break;
			}
			col = cmap.getColorByIndex(cindex + 1); // +1 for ContourLevels to Color
			// index mapping
			// col = cmap.getColorByIndex(cindex);
			if (zmin > zlevp1 || zmax < zlev)
				continue;
			if (zmin >= zlev && zmax <= zlevp1) {
				fillPolygon(g, col, x, y, 4);
				return;
			}
			npoly = -1;
			for (j = 0; j < 4; j++) { /* sides */
				if (z[j] < zlev) {
					//
					// z[j] is below
					//
					if (z[j + 1] > zlevp1) {
						//
						// z[j+1] is above
						//
						npoly = npoly + 1;
						f = (z[j] - zlev) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
						//
						npoly = npoly + 1;
						f = (z[j] - zlevp1) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
					}
					else if (z[j + 1] >= zlev && z[j + 1] <= zlevp1) {
						//
						// z[j+1] is inside
						//
						npoly = npoly + 1;
						f = (z[j] - zlev) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
						//
						npoly = npoly + 1;
						xpoly[npoly] = x[j + 1];
						ypoly[npoly] = y[j + 1];
					}
				}
				else if (z[j] > zlevp1) {
					//
					// z[j] is above
					//
					if (z[j + 1] < zlev) {
						//
						// z[j+1] is below
						//
						npoly = npoly + 1;
						f = (z[j] - zlevp1) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
						//
						npoly = npoly + 1;
						f = (z[j] - zlev) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
					}
					else if (z[j + 1] >= zlev && z[j + 1] <= zlevp1) {
						//
						// z[j+1] is inside
						//
						npoly = npoly + 1;
						f = (z[j] - zlevp1) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
						//
						npoly = npoly + 1;
						xpoly[npoly] = x[j + 1];
						ypoly[npoly] = y[j + 1];
					}
				}
				else {
					//
					// x[j] is inside
					//
					if (z[j + 1] > zlevp1) {
						//
						// z[j+1] is above
						//
						npoly = npoly + 1;
						f = (z[j] - zlevp1) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
					}
					else if (z[j + 1] < zlev) {
						//
						// z[j+1] is below
						//
						npoly = npoly + 1;
						f = (z[j] - zlev) / (z[j] - z[j + 1]);
						xpoly[npoly] = x[j] - f * (x[j] - x[j + 1]);
						ypoly[npoly] = y[j] - f * (y[j] - y[j + 1]);
					}
					else {
						//
						// z[j+1] is inside
						//
						npoly = npoly + 1;
						xpoly[npoly] = x[j + 1];
						ypoly[npoly] = y[j + 1];
					}
				}
			}
			fillPolygon(g, col, xpoly, ypoly, npoly + 1);
		}
	}

	private void fillPolygon(Graphics g, Color c, double[] x, double[] y, int npoints) {
		int[] xt = new int[20];
		int[] yt = new int[20];
		g.setColor(c);
		for (int i = 0; i < npoints; i++) {
			xt[i] = (int)x[i];// layer.getXPtoD(x[i]);
			yt[i] = (int)y[i];// layer.getYPtoD(y[i]);
		}
		g.fillPolygon(xt, yt, npoints);
	}

	EnumeratedColorMap createColorMap(NewColorBar ncb) {
		Color[] colors = ncb.getColors();
		int numColors = ncb.getNumLevels();
		double[] vals = ncb.getValues();

		int[] red = new int[numColors];
		int[] green = new int[numColors];
		int[] blue = new int[numColors];

		for (int i = 0; i < numColors; i++) {
			red[i] = colors[i].getRed();
			green[i] = colors[i].getGreen();
			blue[i] = colors[i].getBlue();
		}

		EnumeratedColorMap cmap = new EnumeratedColorMap(red, green, blue);
		cmap.setEnumeratedValues(vals);
		return cmap;
	}

	private void drawContours(Graphics g) {
		try {
			Enumeration<?> myenum = mCurrContours.elements();
			while (myenum.hasMoreElements()) {
				ContourLine cl = (ContourLine)myenum.nextElement();
				cl.draw(g);
			}
		}
		catch (Exception ex) {
			// silent
		}
	}

	private int latToScreen(double lt, double ln) {
		UVCoordinate uv = this.transformLL(lt, ln);
		uv = mapScaler(uv.u, uv.v);
		return (int)uv.v;
	}

	private int lonToScreen(double lt, double ln) {
		UVCoordinate uv = this.transformLL(lt, ln);
		uv = mapScaler(uv.u, uv.v);
		return (int)uv.u;
	}

	public void plotFilledContours(double[] x, double[] y, double[][] interpValues, Graphics g) {
		// plot the contours
		double[] zBox = new double[4];
		double z1 = 0.0;
		double z2 = 0.0;
		double z3 = 0.0;
		double z4 = 0.0;
		int nFoul = 0;
		double yDelta = 0.0;
		int lowerX = 0;
		int upperX = 0;
		int numXPixels = 0;
		double xDelta = 0.0;
		double delta = 0.0;
		int newX = 0;
		double val = 0.0;
		int newY = 0;
		int startX = 0;
		int startY = 0;
		int colorIndex = 0;
		int oldColorIndex = 0;
		double t, u;

		int yLen = y.length;
		int xLen = x.length;
		double lowerlat = y[0];
		NewColorBar mColorBar = mMapSpec.getBathyColorBar();
		Color[] colors = mColorBar.getColors();

		for (int j1 = 0; j1 < yLen - 1; j1++) {
			// y is in latitude units
			lowerlat = y[j1];

			int lowerY = latToScreen(y[j1], x[0]);
			int upperY = latToScreen(y[j1 + 1], x[0]);

			int numYPixels = upperY - lowerY;
			if (numYPixels == 0) {
				numYPixels = 1;
			}
			if (numYPixels < 0) {
				numYPixels = -numYPixels;
				int temp = upperY;
				upperY = lowerY;
				lowerY = temp;
			}
			yDelta = upperY - lowerY;
			for (int i1 = 0; i1 < xLen - 1; i1++) {
				z1 = interpValues[j1][i1];
				z2 = interpValues[j1][i1 + 1];
				z3 = interpValues[j1 + 1][i1 + 1];
				z4 = interpValues[j1 + 1][i1];
				zBox[0] = z1;
				zBox[1] = z2;
				zBox[2] = z3;
				zBox[3] = z4;
				nFoul = 0;
				for (int i = 0; i < 4; i++) {
					if (zBox[i] == JOAConstants.MISSINGVALUE) {
						nFoul++;
					}
				}

				if (nFoul > 0) { // first version will only accept complete boxes
					continue;
				}

				lowerX = lonToScreen(lowerlat, x[i1]);
				upperX = lonToScreen(lowerlat, x[i1 + 1]);

				// compute the number of new pixels in each direction
				numXPixels = upperX - lowerX;
				if (numXPixels == 0) {
					numXPixels = 1;
				}
				xDelta = upperX - lowerX;

				// got all four values {
				// interpolate
				if (numYPixels == 1) {
					delta = (z2 - z1) / numXPixels;
					// interpolate along the row
					for (int jj = 0; jj < numXPixels; jj++) {
						newX = lowerX + jj;
						val = z1 + jj * delta;
						// if (!mFileViewer.mAllProperties[surfVarNum].isReverseY())
						// newY = maxY - newY + 2*pTopMargin;
						if (mColorBar.getColorIndex(val) != JOAConstants.MISSINGVALUE) {
							g.setColor(colors[mColorBar.getColorIndex(val)]);
						}
						else {
							g.setColor(Color.white);
						}
						g.drawLine(newX, lowerY, newX, lowerY);
					}
				}
				else if (numXPixels == 1) {
					// interpolate along the column
					delta = (z4 - z1) / numYPixels;
					for (int ii = 0; ii < numYPixels; ii++) {
						newY = lowerY + ii;
						val = z1 + ii * delta;
						// if (!mFileViewer.mAllProperties[surfVarNum].isReverseY())
						// newY = maxY - newY + 2*pTopMargin;
						if (mColorBar.getColorIndex(val) != JOAConstants.MISSINGVALUE) {
							g.setColor(colors[mColorBar.getColorIndex(val)]);
						}
						else {
							g.setColor(Color.white);
						}
						g.drawLine(lowerX, newY, lowerX, newY);
					}
				}
				else {
					for (int jj = 0; jj < numXPixels; jj++) {
						for (int ii = 0; ii < numYPixels; ii++) {
							newX = lowerX + jj;
							newY = lowerY + ii;
							if (newX == 0 && newY == 0) {
								continue;
							}
							t = (newX - lowerX) / xDelta;
							u = 0;
							if (yDelta > 0) {
								u = (newY - lowerY) / yDelta;
							}
							else {
								u = (lowerY - newY) / yDelta;
							}
							// if (!mFileViewer.mAllProperties[surfVarNum].isReverseY())
							// newY = maxY - newY + 2*pTopMargin;

							val = (1 - t) * (1 - u) * z1 + t * (1 - u) * z2 + t * u * z3 + (1 - t) * u * z4;

							// apply an observation filter here

							/*
							 * if (mFileViewer.mObsFilterActive) { double c1Val = 0.0, c2Val =
							 * 0.0, c3Val = 0.0, c4Val = 0.0; if
							 * (mFileViewer.mCurrObsFilter.mCriteria1Active) c1Val =
							 * bh.mDValues[c1Pos]; if
							 * (mFileViewer.mCurrObsFilter.mCriteria2Active) c2Val =
							 * bh.mDValues[c2Pos]; if
							 * (mFileViewer.mCurrObsFilter.mCriteria3Active) c3Val =
							 * bh.mDValues[c3Pos]; if
							 * (mFileViewer.mCurrObsFilter.mCriteria4Active) c4Val =
							 * bh.mDValues[c4Pos];
							 * 
							 * if (!mFileViewer.mCurrObsFilter.testValues(c1Val, c2Val, c3Val,
							 * c4Val, 0, 0, 0, 0)) val = 100;//JOAConstants.MISSINGVALUE; }
							 */

							colorIndex = mColorBar.getColorIndex(val);
							if (mColorBar.getColorIndex(val) != JOAConstants.MISSINGVALUE) {
								g.setColor(colors[mColorBar.getColorIndex(val)]);
							}
							else {
								g.setColor(Color.white);
							}
							if (ii == 0) {
								oldColorIndex = colorIndex;
								startX = newX;
								startY = newY;
							}
							else {
								if (colorIndex != oldColorIndex) {
									if (oldColorIndex != JOAConstants.MISSINGVALUE) {
										g.setColor(colors[oldColorIndex]);
									}
									else {
										g.setColor(Color.white);
									}
									g.drawLine(startX, startY, newX, newY);
									oldColorIndex = colorIndex;
									startX = newX;
									startY = newY;
								}
							}
						}
						// finish the line
						if (oldColorIndex != JOAConstants.MISSINGVALUE) {
							g.setColor(colors[oldColorIndex]);
						}
						else {
							g.setColor(Color.white);
						}
						g.drawLine(startX, startY, newX, newY);
					}
				}
			}
		}
	}

	public void plotStnValContours(Graphics g, boolean computeContours) {
		if (!computeContours && mCurrContours != null && mCurrContours.getContourLevels().size() > 0) {
			drawContours(g);
		}

		int nx = 101;
		int ny = 101;

		// need the lat lon range of the stations
		double x1 = mFileViewer.getMinLon();
		double y1 = mFileViewer.getMinLat();
		double dx = Math.abs((mFileViewer.getMinLon() - mFileViewer.getMaxLon()) / 51.0);
		double dy = (mFileViewer.getMaxLat() - mFileViewer.getMinLat()) / 51.0;
		double[] xp = null;
		double[] yp = null;
		double[] zp = null;

		// compute the number of z pts (stns)
		int stnCnt = 0;
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							stnCnt++;
						}
					}
				}
			}
		}

		xp = new double[stnCnt];
		yp = new double[stnCnt];
		zp = new double[stnCnt];

		for (int i = 0; i < stnCnt; i++) {
			xp[i] = Double.NaN;
			yp[i] = Double.NaN;
			zp[i] = Double.NaN;
		}

		stnCnt = 0;
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// collect up the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					// get the station value
					double valOnSurface = sh.getStnValue(mMapSpec.getStnColorByStnValVarCode());
					if (valOnSurface == JOAConstants.MISSINGVALUE) {
						valOnSurface = Double.NaN;
					}

					double lat = sh.mLat;
					double lon = sh.mLon;

					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							xp[stnCnt] = lon;
							yp[stnCnt] = lat;
							zp[stnCnt] = Double.NaN;

							// set the value of z
							// loop on the bottles
							zp[stnCnt] = valOnSurface;
							stnCnt++;
						}
					}
				}
			}
		}

		ZGrid grid = new ZGrid(nx, ny, x1, y1, dx, dy, xp, yp, zp, 5.0, 5, mMapSpec.isMaskCoast());

		// create a contour levels array
		ContourLevels cLevels = new ContourLevels();
		NewColorBar ncb = mMapSpec.getOverlayContoursColorBar();
		int ncl = ncb.getNumLevels();
		Color contColor = Color.black;

		// todo: built in colorbars have to have the contour attribute defined
		// if (mPlotEveryNthOvlContour <= 0)
		int mPlotEveryNthOvlContour = 1;

		for (int k = 0; k < ncl; k += mPlotEveryNthOvlContour) {
			ContourLineAttribute attr = ncb.getContourAttributes(k);
			if (attr == null) {
				attr = new ContourLineAttribute();
				if (contColor != null) {
					attr.setColor(contColor);
				}
				else {
					attr.setColor(ncb.getColorValue(k));
				}
				cLevels.addLevel(ncb.getDoubleValue(k), attr);
			}
			else {
				if (contColor != null) {
					attr.setColor(contColor);
				}
				else {
					attr.setColor(ncb.getColorValue(k));
				}
				cLevels.addLevel(ncb.getDoubleValue(k), attr);
			}
		}

		// have SGT create the contour lines
		mCurrContours = new Contour(this, grid, cLevels, mMapSpec.getContourLabelPrec());
		mCurrContours.generateContourLines();

		// map contour coordinates to display units
		Vector<?> conts = mCurrContours.getContours();
		UVCoordinate uv = null;
		double[] xs = null;
		double[] ys = null;

		// System.out.println("x,y = " + pt.getX() + " " + pt.getY());
		// System.out.println("u,v = " + uv.u + " " + uv.v);
		for (int c = 0; c < conts.size(); c++) {
			Vector<?> cl = (Vector<?>)conts.elementAt(c);
			ContourLine cli = (ContourLine)cl;
			// System.out.println("transforming = " + cli.getLevel() + "size = " +
			// cl.size());
			int len = cl.size();
			xs = new double[len];
			ys = new double[len];

			for (int i = 0; i < len; i++) {
				Object obj = cl.elementAt(i);
				gov.noaa.pmel.util.Point2D.Double pt = (gov.noaa.pmel.util.Point2D.Double)obj;
				if (pt.getY() == Double.NaN || pt.getX() == Double.NaN) {
					xs[i] = Double.NaN;
					ys[i] = Double.NaN;
				}
				else {
					uv = transformLL(pt.getY(), pt.getX());
					uv = mapScaler(uv.u, uv.v);
					xs[i] = uv.u;
					ys[i] = uv.v;
				}
			}
			cli.removeAllElements();

			for (int i = 0; i < len; i++) {
				cli.addPoint(xs[i], ys[i]);
			}
		}

		if (mMapSpec.isFilledIsoContours() || mMapSpec.isFilledStnContours()) {
			EnumeratedColorMap cmap = createColorMap(mMapSpec.getOverlayContoursColorBar());
			plotZGrid(g, grid, cLevels, cmap);
		}

		mCurrContours.generateContourLabels(g);
		drawContours(g);
	}

	public void drawIsobaths(Graphics g) {
		// check to see if we can get one of the bathy support files
		String bathyDir = "";
		try {
			bathyDir = JOAFormulas.getBathymetryPath();
		}
		catch (Exception exx) {
			return;
		}

		boolean crossesDateLine = false;
		double testLeftLon = mMapSpec.getLonLft();
		double testRightLon = mMapSpec.getLonRt();
		double testMinLat = mMapSpec.getLatMin();
		double testMaxLat = mMapSpec.getLatMax();
		boolean lonCor = false;
		if (testLeftLon > -180 && testRightLon < 180) {
			// 0 degree centered do nothing
			lonCor = false;
		}
		else if (testLeftLon > 0 && testRightLon > 180) {
			// 180 centered--convert the basin ranges
			lonCor = true;
		}
		else if (testLeftLon < 0 && testRightLon > 180) {
			// 180 centered--convert the basin ranges
			lonCor = true;
		}

		int isoCnt = mMapSpec.getNumIsobaths();
		ProgressDialog progress = null;
		if (firstTime) {
			// create a Progress Monitor
			progress = new ProgressDialog(Util.getFrame(this), "Plotting Bathymetry", Color.blue, Color.white);
			progress.setVisible(true);
		}
		else {
			mContents.reset();
		}

		for (int i = 0; i < isoCnt; i++) {
			if (progress != null) {
				progress.setPercentComplete(100.0 * ((double)(i + 1) / (double)isoCnt));
			}
			else {
				mContents.setPercentComplete(100.0 * ((double)(i + 1) / (double)isoCnt));
			}

			// get a value for the specified isobath
			double isoVal = mMapSpec.getValue(i);
			int index = (int)(isoVal / 500.0) - 1;

			// built in isobath
			for (int b = 0; b < 8; b++) {
				// does this isobath intersect the map area?
				if (JOAConstants.ISOINBASIN[b][index]) {
					double leftLon = JOAConstants.BASINLIMITS[b * 12 + index][2];
					double rightLon = JOAConstants.BASINLIMITS[b * 12 + index][3];
					double minLat = JOAConstants.BASINLIMITS[b * 12 + index][0];
					double maxLat = JOAConstants.BASINLIMITS[b * 12 + index][1];

					if (lonCor) {
						// correct the basin longitudes
						leftLon = (leftLon + 360) % 360.0;
						rightLon = (rightLon + 360) % 360.0;
					}

					// do the map region and the basin intersect?
					if (((leftLon <= testRightLon && leftLon >= testLeftLon) || (rightLon <= testRightLon && rightLon >= testLeftLon))
					    || ((minLat <= testMaxLat && minLat >= testMinLat) || (maxLat <= testMaxLat && maxLat >= testMinLat))) {
						String filename = new String(JOAConstants.cArray[index] + JOAConstants.bArray[b]);
						try {
							plotIsobath(g, bathyDir, filename, mMapSpec.getColor(i), crossesDateLine);
						}
						catch (IOException ex) {
							ex.printStackTrace();
							;
						}
					}
					else {
						// try the other way arround
						if (((testLeftLon <= rightLon && testLeftLon >= leftLon) || (testRightLon <= rightLon && testRightLon >= leftLon))
						    || ((testMaxLat <= maxLat && testMinLat >= minLat) || (testMaxLat <= maxLat && testMinLat >= minLat))) {
							String filename = new String(JOAConstants.cArray[index] + JOAConstants.bArray[b]);
							try {
								plotIsobath(g, bathyDir, filename, mMapSpec.getColor(i), crossesDateLine);
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
		if (progress != null) {
			progress.dispose();
		}
		else {
			mContents.reset();
		}
	}

	@SuppressWarnings("unchecked")
	public void plotIsobath(Graphics g, String dir, String filename, Color isoColor, boolean crossesDateLine)
	    throws IOException {
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
		CachedIsobath cachedIsobath = (CachedIsobath)JOAConstants.mIsobathCache.get(filename);
		if (cachedIsobath == null) {
			try {
				bathyFile = new File(dir + filename);
				in1 = new FileInputStream(bathyFile);
			}
			catch (Exception ex) {
			}

			if (in1 == null) {
				if (in2 == null) {
					return;
				}
				else {
					in = in2;
				}
			}
			else {
				in = in1;
			}

			// got the file, now parse it and plot it
			BufferedInputStream bis = new BufferedInputStream(in, 1000000);
			DataInputStream inData = new DataInputStream(bis);
			double testLeftLon = mMapSpec.getLonLft();
			double testRightLon = mMapSpec.getLonRt();
			boolean lonCor = false;

			if (testLeftLon > -180 && testRightLon < 180) {
				// 0 degree centered do nothing
				lonCor = false;
			}
			else if (testLeftLon > 0 && testRightLon > 180) {
				// 180 centered--convert the basin ranges
				lonCor = true;
			}
			else if (testLeftLon < 0 && testRightLon > 180) {
				// 180 centered--convert the basin ranges
				lonCor = true;
			}

			boolean correctC50 = false;
			if (filename.equalsIgnoreCase("c50_foci")) {
				correctC50 = true;
			}

			// store values in array
			while (true) {
				// get the number of entries
				int numEntries = inData.readShort();
				if (numEntries == -1) {
					break;
				}

				// get the lats and lons for this segment
				cpt_save = cpt;
				for (int i = 0; i < numEntries; i++) {
					templats[cpt] = inData.readDouble();
					cpt++;
				}

				cpt = cpt_save;
				for (int i = 0; i < numEntries; i++) {
					// map -180 to 180 to 0 to 360
					double templon = inData.readDouble();

					if (correctC50) {
						templon = -templon;
					}

					if (lonCor) {
						templons[cpt] = (templon + 360) % 360.0;
					}
					else {
						templons[cpt] = templon;
					}

					if (cpt > 0 && Math.abs(templons[cpt] - templons[cpt - 1]) > 50.0) {
						cut_count++;
					}
					cpt++;
				}

				templats[cpt] = Double.NaN;
				templons[cpt] = Double.NaN;
				cpt++;
			}

			int length = cpt;
			bLatVal = new double[cpt + cut_count];
			bLonVal = new double[cpt + cut_count];
			cpt = 0;
			for (int i = 0; i < length; i++) {
				if (i > 0 && Math.abs(templons[i] - templons[i - 1]) > 50.0) {
					bLatVal[cpt] = Double.NaN;
					bLonVal[cpt] = Double.NaN;
					cpt++;
				}
				bLatVal[cpt] = templats[i];
				bLonVal[cpt] = templons[i];
				cpt++;
			}

			// create the cached object and add to hash table
			CachedIsobath cb = new CachedIsobath(bLatVal, bLonVal);
			JOAConstants.mIsobathCache.put(filename, cb);
		}
		else {
			bLatVal = cachedIsobath.getLats();
			bLonVal = cachedIsobath.getLons();
		}

		g.setColor(isoColor);

		double u;
		double v;
		UVCoordinate uv = transformLL(bLatVal[0], bLonVal[0]);
		uv = mapScaler(uv.u, uv.v);
		double oldU = uv.u;
		double oldV = uv.v;
		int c = 1;
		int arrayLen = bLatVal.length;
		while (c < arrayLen - 1) {
			if (!Double.isNaN(bLonVal[c])) {
				if (LLKeep(bLatVal[c], bLonVal[c])) {
					uv = transformLL(bLatVal[c], bLonVal[c]);
					uv = mapScaler(uv.u, uv.v);
					u = uv.u;
					v = uv.v;
					if (Math.abs(oldU - u) < 50 && Math.abs(oldV - v) < 50) {
						g.drawLine((int)oldU, (int)oldV, (int)u, (int)v);
					}
					oldU = u;
					oldV = v;
				}
				c++;
			}
			else {
				// end of a segment: move to begining of next in-window segment
				c++;
				while (c < arrayLen - 1) {
					if (LLKeep(bLatVal[c], bLonVal[c])) {
						uv = transformLL(bLatVal[c], bLonVal[c]);
						uv = mapScaler(uv.u, uv.v);
						oldU = uv.u;
						oldV = uv.v;
						c++;
						break;
					}
					c++;
				}
			}
		}
	}

	public void plotSectionLabels(Graphics g) {
		g.setFont(new Font("sansserif", Font.PLAIN, 12));
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// does section cross dateline
				double lon1, oldLon = 0.0;
				boolean dataCross180 = false;
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					lon1 = sh.mLon;
					if ((lon1 >= -180 && lon1 < -178) && (oldLon >= 178 && oldLon <= 180)) {
						dataCross180 = true;
						break;
					}
					else if ((lon1 >= 178 && lon1 <= 180) && (oldLon >= -180 && oldLon < -178)) {
						dataCross180 = true;
						break;
					}
					oldLon = lon1;
				}

				// compute the range of the stations in this section
				double mLonMin = 360.0;
				double mLatMin = 90.0;
				double mLonMax = -360.0;
				double mLatMax = -90.0;
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					double lat1 = sh.mLat;
					lon1 = sh.mLon;

					mLatMin = lat1 < mLatMin ? lat1 : mLatMin;
					mLatMax = lat1 > mLatMax ? lat1 : mLatMax;

					if (dataCross180) {
						if (lon1 < 0) {
							lon1 += 360.0;
						}
					}

					mLonMin = lon1 < mLonMin ? lon1 : mLonMin;
					mLonMax = lon1 > mLonMax ? lon1 : mLonMax;
				}

				// plot the section label
				double totalLatDegs = mLatMax - mLatMin;
				double latCtr = (mLatMax + mLatMin) / 2.0;
				double lon = mLonMax;
				if (dataCross180) {
					if (lon < 0) {
						lon += 360.0;
					}
				}
				double totalLonDegs = lon - mLonMin;
				double lonCtr = (lon + mLonMin) / 2.0;

				// get the section name
				// g.setColor(sech.mSectionColor);
				String label = JOAFormulas.getShortSectionName(sech.mSectionDescription, 8);

				// convert the
				// place label west of the center point
				if (LLKeep(latCtr, lonCtr)) {
					UVCoordinate uv = transformLL(latCtr, lonCtr);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						if (totalLatDegs > totalLonDegs) {
							drawFancyLabel(g, (int)uv.u + 20, (int)uv.v, label, sech.mSectionColor);
						}
						else if (totalLonDegs > totalLatDegs) {
							drawFancyLabel(g, (int)uv.u, (int)uv.v - 15, label, sech.mSectionColor);
						}
					}
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	public void plotStationLabels(Graphics ing) {
		Graphics2D g = (Graphics2D)ing;
		String stnLbl = "";

		Font font = new Font(JOAConstants.DEFAULT_MAP_STN_LABEL_FONT, JOAConstants.DEFAULT_MAP_STN_LABEL_STYLE,
		    JOAConstants.DEFAULT_MAP_STN_LABEL_SIZE);
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
		int height = fm.getHeight();

		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
						sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
					}
					else {
						// set the color
						if (sech.mSectionColor != null) {
							g.setColor(sech.mSectionColor);
							sh.mCurrColor = sech.mSectionColor;
						}
						else {
							g.setColor(Color.blue);
							sh.mCurrColor = Color.blue;
						}
					}

					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							// get the label
							if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
								if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
									stnLbl = String.valueOf(sh.getDate());
								}
								else if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(
								    rb.getString("kDateTimeMonth"))) {
									stnLbl = String.valueOf(sh.getMonth() - 1);
								}
								else if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(
								    rb.getString("kLatitude"))) {
									stnLbl = JOAFormulas.formatLat(lat, mMapSpec.getContourLabelPrec());
								}
								else if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(
								    rb.getString("kLongitude"))) {
									stnLbl = JOAFormulas.formatLon(lon, mMapSpec.getContourLabelPrec());
								}

							}
							else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
								stnLbl = JOAFormulas.formatDouble(sh.getStnValue(mMapSpec.getStnColorByStnValVarCode()), mMapSpec
								    .getContourLabelPrec(), false);
							}
							else {
								stnLbl = sh.getStn();
							}
							JOAFormulas.drawStyledString(stnLbl, (int)uv.u + mMapSpec.getStnLabelOffset(), (int)uv.v + height / 2, g,
							    mMapSpec.getStnLabelAngle(), JOAConstants.DEFAULT_MAP_STN_LABEL_FONT,
							    JOAConstants.DEFAULT_MAP_STN_LABEL_SIZE, JOAConstants.DEFAULT_MAP_STN_LABEL_STYLE,
							    JOAConstants.DEFAULT_MAP_STN_LABEL_COLOR);
						}
					}
				}
			}
		}

	}

	public void plotStations(Graphics g) {
		double oldU = JOAConstants.MISSINGVALUE, oldV = JOAConstants.MISSINGVALUE;

		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				boolean atSectionBoundary = true;

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
						sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
					}
					else {
						// set the color
						if (sech.mSectionColor != null) {
							g.setColor(sech.mSectionColor);
							sh.mCurrColor = sech.mSectionColor;
						}
						else {
							g.setColor(Color.blue);
							sh.mCurrColor = Color.blue;
						}
					}

					sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							// draw the station point
							if (mMapSpec.getSymbolSize() == 1) {
								g.drawLine((int)uv.u + 2, (int)uv.v + 2, (int)uv.u + 2, (int)uv.v + 2);
							}
							else {
								int width = mMapSpec.getSymbolSize();
								JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, width);
							}

							// connect if necessary
							if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
								if (atSectionBoundary) {
									if (mMapSpec.isConnectStnsAcrossSections()) {
										g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
									}
								}
								else {
									g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
							}
							atSectionBoundary = false;
						}
						oldU = uv.u;
						oldV = uv.v;
					}
				}
			}
		}

		// hilite any symbols that are part of a section selection
		if (mCurrSectionEditor != null) {
			Vector<SectionStation> stns = mCurrSectionEditor.getSectionStations();
			if (stns.size() > 0) {
				for (int s = 0; s < stns.size(); s++) {
					SectionStation ssh = (SectionStation)stns.elementAt(s);
					Station sh = ssh.mFoundStn;
					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							g.setColor(sh.mCurrColor);
							int symbolSize = sh.mCurrSymbolSize + 3;

							// draw the station point
							JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), uv.u + 2, uv.v + 2, symbolSize);

							int x = (int)uv.u - symbolSize / 2 + 2;
							int y = (int)uv.v - symbolSize / 2 + 2;
							int width = symbolSize;
							int height = symbolSize;
							// g.fillOval(x, y, width, height);
							// g.setColor(Color.black);
							// g.drawOval(x, y, width, height);

							// draw any hiliting
							if (sh.mHilitedOnMap) {
								g.drawOval(x - 2, y - 2, width + 4, height + 4);
							}
						}

					}
				}
			}
		}
	}

	public void plotStationsWithMetadataColors(Graphics g) {
		double oldU = JOAConstants.MISSINGVALUE, oldV = JOAConstants.MISSINGVALUE;
		if (mMapSpec.getStnColorColorBar() == null)
			return;

		// plot with station variable color
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				boolean atSectionBoundary = true;

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);

					double stnLat = sh.getLat();
					double stnLon = sh.getLon();
					GeoDate stnDate = sh.getDate();
					int stnMonth = sh.getMonth() - 1;

					if (!sh.mUseStn) {
						g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
					}
					else {
						// get the value of the metadatatype for this station
						if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
							g.setColor(mMapSpec.getStnColorColorBar().getColor(stnDate));
						}
						else if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(
						    rb.getString("kDateTimeMonth"))) {
							g.setColor(mMapSpec.getStnColorColorBar().getColor(stnMonth));
						}
						else if (mMapSpec.getStnColorColorBar().getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"))) {
							g.setColor(mMapSpec.getStnColorColorBar().getColor(stnLat));
						}
						else if (mMapSpec.getStnColorColorBar().getMetadataType()
						    .equalsIgnoreCase(rb.getString("kLongitude"))) {
							g.setColor(mMapSpec.getStnColorColorBar().getColor(stnLon));
						}
					}

					sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
					double lat = sh.mLat;
					double lon = sh.mLon;

					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							// draw the station point
							if (mMapSpec.getSymbolSize() == 1) {
								g.drawLine((int)uv.u + 2, (int)uv.v + 2, (int)uv.u + 2, (int)uv.v + 2);
							}
							else {
								int width = mMapSpec.getSymbolSize();
								JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), uv.u + 2, uv.v + 2, width);
							}

							// connect if necessary
							if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
								if (atSectionBoundary) {
									if (mMapSpec.isConnectStnsAcrossSections()) {
										g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
									}
								}
								else {
									g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
							}
							atSectionBoundary = false;
						}
						oldU = uv.u;
						oldV = uv.v;
					}
				}
			}
		}
	}

	public void plotStationsWithStnVarColors(Graphics g) {
		double oldU = JOAConstants.MISSINGVALUE, oldV = JOAConstants.MISSINGVALUE;

		// plot with station variable color
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				boolean atSectionBoundary = true;

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);

					// get the station value
					double y = sh.getStnValue(mMapSpec.getStnColorByStnValVarCode());

					if (!sh.mUseStn) {
						g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
					}
					else {
						g.setColor(mMapSpec.getStnColorColorBar().getColor(y));
					}

					sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
					double lat = sh.mLat;
					double lon = sh.mLon;

					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							// draw the station point
							if (mMapSpec.getSymbolSize() == 1) {
								g.drawLine((int)uv.u + 2, (int)uv.v + 2, (int)uv.u + 2, (int)uv.v + 2);
							}
							else {
								int width = mMapSpec.getSymbolSize();
								JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, width);
							}

							// connect if necessary
							if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
								if (atSectionBoundary) {
									if (mMapSpec.isConnectStnsAcrossSections()) {
										g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
									}
								}
								else {
									g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
							}
							atSectionBoundary = false;
						}
						oldU = uv.u;
						oldV = uv.v;
					}
				}
			}
		}
		return;
	}

	public void plotStationsWithIsoSurfaceColors(Graphics g) {
		double oldU = JOAConstants.MISSINGVALUE, oldV = JOAConstants.MISSINGVALUE;

		if (mMapSpec.isStnColorByIsoMinIsoSurfaceValue()) {
			// plot at the minimum
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					boolean atSectionBoundary = true;

					// get param positions for this section
					int isoVarPos = sech.getVarPos(mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel(),
					    false);
					int surfVarPos = sech.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);

					// draw the station points
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
							sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
						}
						else {
							// set the color
							// loop on the bottles
							double min = 99999999;
							g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);

							if (mMapSpec.isStnColorByIsoIsReferenced()) {
								// find the reference level for this cast
							}

							if (mMapSpec.isStnColorByIsoBottomUpSearch()) {
								for (int b = sh.mNumBottles - 1; b > 0; b--) {
									Bottle bh = (Bottle)sh.mBottles.elementAt(b);
									boolean keepBottle;
									if (mFileViewer.mObsFilterActive) {
										keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh);
									}
									else {
										keepBottle = true;
									}

									if (keepBottle) {
										double surfVal = bh.mDValues[surfVarPos];
										double colorVal = bh.mDValues[isoVarPos];
										//
										if (colorVal == JOAConstants.MISSINGVALUE) {
											continue;
										}

										if (surfVal < min) {
											min = surfVal;
											g.setColor(mMapSpec.getStnColorColorBar().getColor(colorVal));
											sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(colorVal);
										}
									}
								}
							}
							else {
								min = 9999999;
								g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
								for (int b = 0; b < sh.mNumBottles; b++) {
									sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
									Bottle bh = (Bottle)sh.mBottles.elementAt(b);
									boolean keepBottle;
									if (mFileViewer.mObsFilterActive) {
										keepBottle = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh);
									}
									else {
										keepBottle = true;
									}

									if (keepBottle) {
										double surfVal = bh.mDValues[surfVarPos];
										double colorVal = bh.mDValues[isoVarPos];
										if (colorVal == JOAConstants.MISSINGVALUE) {
											continue;
										}

										if (surfVal < min) {
											min = surfVal;
											g.setColor(mMapSpec.getStnColorColorBar().getColor(colorVal));
											sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(colorVal);
										}
									}
								}
							}
						}

						sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
						double lat = sh.mLat;
						double lon = sh.mLon;
						if (LLKeep(lat, lon)) {
							UVCoordinate uv = transformLL(lat, lon);
							uv = mapScaler(uv.u, uv.v);
							if (uv.mInWind) {
								// draw the station point
								if (mMapSpec.getSymbolSize() == 1) {
									g.drawLine((int)uv.u + 2, (int)uv.v + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
								else {
									int width = mMapSpec.getSymbolSize();
									JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, width);
								}

								// connect if necessary
								if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
									if (atSectionBoundary) {
										if (mMapSpec.isConnectStnsAcrossSections()) {
											g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
										}
									}
									else {
										g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
									}
								}
								atSectionBoundary = false;
							}
							oldU = uv.u;
							oldV = uv.v;
						}
					}
				}
			}
			return;
		}
		else if (mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()) {
			// plot at the maximum
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					boolean atSectionBoundary = true;

					// get param positions for this section
					int isoVarPos = sech.getVarPos(mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel(),
					    false);
					int surfVarPos = sech.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);

					// draw the station points
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
							sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
						}
						else {
							// set the color
							// loop on the bottles
							double max = -999999999999.0;
							g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
							if (mMapSpec.isStnColorByIsoBottomUpSearch()) {
								for (int b = sh.mNumBottles; b > 0; b--) {
									Bottle bh = (Bottle)sh.mBottles.elementAt(b);
									double surfVal = bh.mDValues[surfVarPos];
									double colorVal = bh.mDValues[isoVarPos];
									if (colorVal == JOAConstants.MISSINGVALUE) {
										continue;
									}

									if (surfVal > max) {
										max = surfVal;
										g.setColor(mMapSpec.getStnColorColorBar().getColor(colorVal));
										sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(colorVal);
									}
								}

							}
							else {
								max = -999999999999.0;
								g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
								for (int b = 0; b < sh.mNumBottles; b++) {
									sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
									Bottle bh = (Bottle)sh.mBottles.elementAt(b);
									double surfVal = bh.mDValues[surfVarPos];
									double colorVal = bh.mDValues[isoVarPos];
									if (colorVal == JOAConstants.MISSINGVALUE) {
										continue;
									}

									if (surfVal > max) {
										max = surfVal;
										g.setColor(mMapSpec.getStnColorColorBar().getColor(colorVal));
										sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(colorVal);
									}
								}
							}
						}

						sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
						double lat = sh.mLat;
						double lon = sh.mLon;
						if (LLKeep(lat, lon)) {
							UVCoordinate uv = transformLL(lat, lon);
							uv = mapScaler(uv.u, uv.v);
							if (uv.mInWind) {
								// draw the station point
								if (mMapSpec.getSymbolSize() == 1) {
									g.drawLine((int)uv.u + 2, (int)uv.v + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
								else {
									int width = mMapSpec.getSymbolSize();
									JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, width);
								}

								// connect if necessary
								if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
									if (atSectionBoundary) {
										if (mMapSpec.isConnectStnsAcrossSections()) {
											g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
										}
									}
									else {
										g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
									}
								}
								atSectionBoundary = false;
							}
							oldU = uv.u;
							oldV = uv.v;
						}
					}
				}

			}
			return;
		}

		// find the closest matching surface level for the current surface value
		double delta = 10000000;
		NewInterpolationSurface isoSurface = mMapSpec.getStnColorByIsoSurface();
		int numSurfLevels = isoSurface.getNumLevels();
		for (int i = 0; i < numSurfLevels; i++) {
			double valOfSurface = isoSurface.getValue(i);
			double d = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - valOfSurface);
			if (d < delta) {
				delta = d;
				mCurrIsoSurfaceLevel = i;
			}
		}

		double refValue = 0.0;

		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				boolean atSectionBoundary = true;

				// get param positions for this section
				int isoVarPos = sech.getVarPos(mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel(),
				    false);
				int surfVarPos = sech.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);

				if (isoVarPos < 0 || surfVarPos < 0) {
					continue;
				}

				// draw the station points
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
						sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;
					}
					else {
						if (mMapSpec.isStnColorByIsoIsReferenced()) {
							double[] sVals = new double[sh.mNumBottles];
							double[] mVals = new double[sh.mNumBottles];

							for (int b = 0; b < sh.mNumBottles; b++) {
								Bottle bh = (Bottle)sh.mBottles.elementAt(b);
								sVals[b] = bh.mDValues[surfVarPos];
								mVals[b] = bh.mDValues[isoVarPos];
							}

							// dereference
							refValue = JOAFormulas.dereferenceStation(sh.mNumBottles, mMapSpec.getStnColorByIsoReferenceLevel(),
							    sVals, mVals);
						}

						// set the color
						// loop on the bottles
						if (mMapSpec.isStnColorByIsoBottomUpSearch()) {
							for (int b = sh.mNumBottles - 1; b > 0; b--) {
								g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);

								Bottle bh1 = (Bottle)sh.mBottles.elementAt(b);
								Bottle bh2 = (Bottle)sh.mBottles.elementAt(b - 1);

								// test obs filter
								boolean keepBottle1;
								boolean keepBottle2;
								if (mFileViewer.mObsFilterActive) {
									keepBottle1 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh1);
								}
								else {
									keepBottle1 = true;
								}

								if (mFileViewer.mObsFilterActive) {
									keepBottle2 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh2);
								}
								else {
									keepBottle2 = true;
								}

								if (keepBottle1 && keepBottle2) {
									double surfVal1 = bh1.mDValues[surfVarPos];
									double surfVal2 = bh2.mDValues[surfVarPos];
									if (surfVal1 == JOAConstants.MISSINGVALUE || surfVal2 == JOAConstants.MISSINGVALUE) {
										continue;
									}

									if ((mMapSpec.getStnColorByIsoIsoSurfaceValue() >= surfVal1 && mMapSpec
									    .getStnColorByIsoIsoSurfaceValue() <= surfVal2)
									    || (mMapSpec.getStnColorByIsoIsoSurfaceValue() <= surfVal1 && mMapSpec
									        .getStnColorByIsoIsoSurfaceValue() >= surfVal2)) {
										// found bounding bottles, get the vals of the parameter on
										// the surface
										double isoVal1 = bh1.mDValues[isoVarPos];
										double isoVal2 = bh2.mDValues[isoVarPos];
										if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
											if (!mMapSpec.isStnColorByIsoIsReferenced()) {
												g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
											}
											else {
												int maxCnt = mMapSpec.getStnColorByIsoMaxInterpDistance();
												// look for non-missing observation
												if (isoVal2 == JOAConstants.MISSINGVALUE) {
													// look above (reverse this for top down search)
													int cnt = 1;
													while (isoVal2 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
														if (b - cnt < 0) {
															break;
														}
														Bottle bhAbove = (Bottle)sh.mBottles.elementAt(b - cnt);
														surfVal2 = bhAbove.mDValues[surfVarPos];
														isoVal2 = bhAbove.mDValues[isoVarPos];
														cnt++;
													}
												}
												if (isoVal1 == JOAConstants.MISSINGVALUE) {
													// look below (reverse this for top down search)
													int cnt = 1;
													while (isoVal1 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
														if (b + cnt > sh.mNumBottles) {
															break;
														}
														Bottle bhBelow = (Bottle)sh.mBottles.elementAt(b + cnt);
														surfVal1 = bhBelow.mDValues[surfVarPos];
														isoVal1 = bhBelow.mDValues[isoVarPos];
														cnt++;
													}
												}
											}
											if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
												g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
											}
											else {
												// interp new value
												double denom = Math.abs(surfVal2 - surfVal1);
												double num1 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal1);
												double num2 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal2);
												double frac = num1 / denom;

												// Triangle inequality.
												if (num1 > denom && num1 > num2) {
													frac = 1.0;
												}
												if (num2 > denom && num2 > num1) {
													frac = 0.0;
												}

												double valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));

												// dereference if neccessary
												if (mMapSpec.isStnColorByIsoIsReferenced()) {
													if (refValue != JOAConstants.MISSINGVALUE) {
														g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface - refValue));
														sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface - refValue);
													}
													else {
														g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
													}
												}
												else {
													g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface));
													sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface);
												}
												break;
											}
										}
										else {
											// interp new value
											double denom = Math.abs(surfVal2 - surfVal1);
											double num1 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal1);
											double num2 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal2);
											double frac = num1 / denom;

											// Triangle inequality.
											if (num1 > denom && num1 > num2) {
												frac = 1.0;
											}
											if (num2 > denom && num2 > num1) {
												frac = 0.0;
											}

											double valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));

											// dereference if neccessary
											if (mMapSpec.isStnColorByIsoIsReferenced()) {
												if (refValue != JOAConstants.MISSINGVALUE) {
													g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface - refValue));
													sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface - refValue);
												}
												else {
													g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
												}
											}
											else {
												g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface));
												sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface);
											}
											break;
										}
									}
								}
								else {
									g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
								}
							}
						}
						else {
							for (int b = 0; b < sh.mNumBottles - 1; b++) {
								g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
								sh.mCurrColor = JOAConstants.DEFAULT_MISSINGVAL_COLOR;

								Bottle bh1 = (Bottle)sh.mBottles.elementAt(b);
								Bottle bh2 = (Bottle)sh.mBottles.elementAt(b + 1);

								// test observation filter
								boolean keepBottle1;
								boolean keepBottle2;
								if (mFileViewer.mObsFilterActive) {
									keepBottle1 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh1);
								}
								else {
									keepBottle1 = true;
								}

								if (mFileViewer.mObsFilterActive) {
									keepBottle2 = mFileViewer.mCurrObsFilter.testObservation(mFileViewer, sech, bh2);
								}
								else {
									keepBottle2 = true;
								}

								if (keepBottle1 && keepBottle2) {
									double surfVal1 = bh1.mDValues[surfVarPos];
									double surfVal2 = bh2.mDValues[surfVarPos];
									if (surfVal1 == JOAConstants.MISSINGVALUE || surfVal2 == JOAConstants.MISSINGVALUE) {
										continue;
									}
									if ((mMapSpec.getStnColorByIsoIsoSurfaceValue() >= surfVal1 && mMapSpec
									    .getStnColorByIsoIsoSurfaceValue() <= surfVal2)
									    || (mMapSpec.getStnColorByIsoIsoSurfaceValue() <= surfVal1 && mMapSpec
									        .getStnColorByIsoIsoSurfaceValue() >= surfVal2)) {
										// found bounding bottles, get the vals of the parameter on
										// the surface
										double isoVal1 = bh1.mDValues[isoVarPos];
										double isoVal2 = bh2.mDValues[isoVarPos];
										if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
											if (!mMapSpec.isStnColorByIsoLocalInterpolation()) {
												g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
											}
											else {
												int maxCnt = mMapSpec.getStnColorByIsoMaxInterpDistance();
												// look for non-missing observation
												if (isoVal1 == JOAConstants.MISSINGVALUE) {
													// look above
													int cnt = 1;
													while (isoVal1 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
														if (b - cnt < 0) {
															break;
														}
														Bottle bhAbove = (Bottle)sh.mBottles.elementAt(b - cnt);
														surfVal1 = bhAbove.mDValues[surfVarPos];
														isoVal1 = bhAbove.mDValues[isoVarPos];
														cnt++;
													}
												}
												if (isoVal2 == JOAConstants.MISSINGVALUE) {
													// look below
													int cnt = 1;
													while (isoVal2 == JOAConstants.MISSINGVALUE && cnt <= maxCnt) {
														if (b + cnt > sh.mNumBottles) {
															break;
														}
														Bottle bhBelow = (Bottle)sh.mBottles.elementAt(b + cnt);
														surfVal2 = bhBelow.mDValues[surfVarPos];
														isoVal2 = bhBelow.mDValues[isoVarPos];
														cnt++;
													}
												}
											}
											if (isoVal1 == JOAConstants.MISSINGVALUE || isoVal2 == JOAConstants.MISSINGVALUE) {
												g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
											}
											else {
												// interp new value
												double denom = Math.abs(surfVal2 - surfVal1);
												double num1 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal1);
												double num2 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal2);
												double frac = num1 / denom;

												// Triangle inequality.
												if (num1 > denom && num1 > num2) {
													frac = 1.0;
												}
												if (num2 > denom && num2 > num1) {
													frac = 0.0;
												}

												double valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));

												g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface));
												sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface);
												break;
											}
										}
										else {
											// interp new value
											double denom = Math.abs(surfVal2 - surfVal1);
											double num1 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal1);
											double num2 = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - surfVal2);
											double frac = num1 / denom;

											// Triangle inequality.
											if (num1 > denom && num1 > num2) {
												frac = 1.0;
											}
											if (num2 > denom && num2 > num1) {
												frac = 0.0;
											}

											double valOnSurface = isoVal1 + (frac * (isoVal2 - isoVal1));
											if (mMapSpec.isStnColorByIsoIsReferenced()) {
												if (refValue != JOAConstants.MISSINGVALUE) {
													g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface - refValue));
													sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface - refValue);
												}
												else {
													g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
												}
											}
											else {
												g.setColor(mMapSpec.getStnColorColorBar().getColor(valOnSurface));
												sh.mCurrColor = mMapSpec.getStnColorColorBar().getColor(valOnSurface);
											}
											break;
										}
									}
								}
								else {
									g.setColor(JOAConstants.DEFAULT_MISSINGVAL_COLOR);
								}
							}
						}
					}

					sh.mCurrSymbolSize = mMapSpec.getSymbolSize();
					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							// draw the station point
							if (mMapSpec.getSymbolSize() == 1) {
								g.drawLine((int)uv.u + 2, (int)uv.v + 2, (int)uv.u + 2, (int)uv.v + 2);
							}
							else {
								int width = mMapSpec.getSymbolSize();
								JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, width);
							}

							// connect if necessary
							if (mMapSpec.isConnectStns() && oldU != JOAConstants.MISSINGVALUE) {
								if (atSectionBoundary) {
									if (mMapSpec.isConnectStnsAcrossSections()) {
										g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
									}
								}
								else {
									g.drawLine((int)oldU + 2, (int)oldV + 2, (int)uv.u + 2, (int)uv.v + 2);
								}
							}
							atSectionBoundary = false;
						}
						oldU = uv.u;
						oldV = uv.v;
					}
				}
			}

		}

		// plot any hilited stations
		if (mCurrSectionEditor != null) {
			Vector<SectionStation> stns = mCurrSectionEditor.getSectionStations();
			if (stns.size() > 0) {
				for (int s = 0; s < stns.size(); s++) {
					SectionStation ssh = (SectionStation)stns.elementAt(s);
					Station sh = ssh.mFoundStn;
					double lat = sh.mLat;
					double lon = sh.mLon;
					if (LLKeep(lat, lon)) {
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							g.setColor(sh.mCurrColor);
							int symbolSize = sh.mCurrSymbolSize + 3;

							// draw the station point
							JOAFormulas.plotSymbol((Graphics2D)g, mMapSpec.getSymbol(), (int)uv.u + 2, (int)uv.v + 2, symbolSize);

							int x = (int)uv.u - symbolSize / 2 + 2;
							int y = (int)uv.v - symbolSize / 2 + 2;
							int width = symbolSize;
							int height = symbolSize;

							// draw any hiliting
							if (sh.mHilitedOnMap) {
								g.drawOval(x - 2, y - 2, width + 4, height + 4);
							}
						}
					}
				}
			}
		}
	}

	public void initDataSpot() {
		// init the data spot
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		double lat = sh.mLat;
		double lon = sh.mLon;
		if (LLKeep(lat, lon)) {
			UVCoordinate uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			mObsMarker = new ObsMarker((int)uv.u + 2, (int)uv.v + 2, JOAConstants.DEFAULT_CURSOR_SIZE);
		}
		else {
			mObsMarker = new ObsMarker(2000, 2000, JOAConstants.DEFAULT_CURSOR_SIZE);
		}
	}

	public void drawMap(Graphics g) {
		// draw the coastline
		try {
				if (mMapSpec.getCoastLineRez() == JOAConstants.COARSERESOLUTION) {
					plotMap(g, "finerezcoast.bin");
				}
				else if (mMapSpec.getCoastLineRez() == JOAConstants.FINERESOLUTION) {
					plotMap(g, "finerezcoast.bin");
				}
				else if (mMapSpec.getCoastLineRez() == JOAConstants.CUSTOMCOAST) {
					plotMap(g, mMapSpec.getCustCoastPath());
				}
				else {
					plotMap(g, "");
				}
		}
		catch (IOException ex) {
			// present an error alert
		}
	}

	public void plotGCSMap(Graphics g, String coastFileName) throws IOException {
		double[] lat = new double[1000];
		double[] lon = new double[1000];
		setGCSScaling();
		// get the file
		File coarseFile = null;
		try {
			coarseFile = JOAFormulas.getSupportFile(coastFileName);
		}
		catch (IOException ex) {
			throw ex;
		}

		// got the file, now parse it and plot it
		FileInputStream in = new FileInputStream(coarseFile);
		DataInputStream inData = new DataInputStream(in);
		g.setColor(mMapSpec.getCoastColor());
		try {
			while (true) {
				// get the number of entries
				int numEntries = inData.readShort();
				if (numEntries == -1) {
					break;
				}

				// get the lats and lons for this segment
				for (int i = 0; i < numEntries; i++) {
					lat[i] = inData.readDouble();
				}

				for (int i = 0; i < numEntries; i++) {
					lon[i] = inData.readDouble();
				}

				// plot this segment
				double m = getMagnitude(lat[0]);
				double a = getAngle(lon[0]);
				double x = getX(a, m) + 180;
				double y = 500 - (getY(a, m) + 180);
				double oldX = x;
				double oldY = y;
				for (int i = 1; i < numEntries; i++) {
					m = getMagnitude(lat[i]);
					a = getAngle(lon[i]);
					x = getX(a, m) + 180;
					y = 500 - (getY(a, m) + 180);
					g.drawLine((int)oldX, (int)oldY, (int)x, (int)y);
					oldX = x;
					oldY = y;
				}
			}
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	public void setGCSScaling() {
		double xMax = -999999;
		double xMin = 9999999;
		double yMax = -999999;
		double m = getMagnitude(-90);
		double a = getAngle(0);
		double x = getX(a, m) + 180;
		double y = getY(a, m) + 180;
		xMax = Math.max(x, xMax);
		xMin = Math.min(x, xMin);
		yMax = Math.max(y, yMax);
		m = getMagnitude(-90);
		a = getAngle(90);
		x = getX(a, m) + 180;
		y = getY(a, m) + 180;
		xMax = Math.max(x, xMax);
		xMin = Math.min(x, xMin);
		yMax = Math.max(y, yMax);
		a = getAngle(180);
		x = getX(a, m) + 180;
		y = getY(a, m) + 180;
		xMax = Math.max(x, xMax);
		xMin = Math.min(x, xMin);
		yMax = Math.max(y, yMax);
		a = getAngle(270);
		x = getX(a, m) + 180;
		y = getY(a, m) + 180;
		xMax = Math.max(x, xMax);
		xMin = Math.min(x, xMin);
		yMax = Math.max(y, yMax);
	}

	public double getMagnitude(double inLat) {
		if (inLat >= 0) {
			return 90.0 - inLat;
		}
		else {
			return 90.0 + -inLat;
		}
	}

	public double getAngle(double inLon) {
		double val = 0;
		if (inLon >= 0 && inLon <= 180) {
			val = inLon;
		}
		else if (inLon > 180) {
			val = inLon;
		}
		else if (inLon < 0 && inLon >= -180) {
			val = 360 - (-inLon);
		}
		else if (inLon < -180) {
			val = 360 + inLon;
		}
		return val;
	}

	public double getX(double a, double m) {
		double offset = 200;
		double multiplier = 1.75;
		double val = 0;
		if (a >= 0 && a <= 90) {
			val = -m * Math.cos(JOAConstants.F * a);
		}
		else if (a > 90 && a <= 180) {
			val = -m * Math.cos(JOAConstants.F * a);
		}
		else if (a > 180 && a <= 270) {
			val = -m * Math.cos(JOAConstants.F * a);
		}
		else if (a > 270 && a <= 360) {
			val = -m * Math.cos(JOAConstants.F * a);
		}
		else {
			val = m * Math.cos(JOAConstants.F * a);
		}

		return (multiplier * val) + offset;
	}

	public double getY(double a, double m) {
		double offset = 0;
		double multiplier = 1.75;
		double val = 0;
		if (a >= 0 && a <= 90) {
			val = -m * Math.sin(JOAConstants.F * a);
		}
		else if (a > 90 && a <= 180) {
			val = -m * Math.sin(JOAConstants.F * a);
		}
		else if (a > 180 && a <= 270) {
			val = -m * Math.sin(JOAConstants.F * a);
		}
		else if (a > 270 && a <= 360) {
			val = -m * Math.sin(JOAConstants.F * a);
		}
		else {
			val = -m * Math.sin(JOAConstants.F * a);
		}

		return (multiplier * val) + offset;
	}

	public double computeVectorAngle(int originH, int originV, int currH, int currV) {
		int newH = currH - originH;
		int newV = currV - originV;
		newV = -newV;
		double ratio = 0.01;
		if (newV != 0) {
			ratio = Math.abs((double)newH / (double)newV);
		}
		double angle = Math.atan(ratio);

		if (newH >= 0 && newV >= 0) {
			angle = 2 * Math.PI - angle;
		}
		else if (newH < 0 && newV >= 0) {
			angle = Math.PI + angle;
		}
		else if (newH < 0 && newV < 0) {
			angle = 0.5 * Math.PI + angle;
		}
		else if (newH > 0 && newV < 0) {
			angle = 0.5 * Math.PI - angle;
		}
		return angle;
	}

	public void plotMap(Graphics g, String coastFileName) throws IOException {
		if (coastFileName.length() == 0) { return; }
		double[] lat = new double[1000];
		double[] lon = new double[1000];
		// get the file
		File coarseFile = null;
		try {
			coarseFile = new File(coastFileName);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		if (coarseFile == null || !coarseFile.exists()) {
			try {
				coarseFile = JOAFormulas.getCoastFile(coastFileName);
			}
			catch (IOException ex) {
				try {
					coarseFile = JOAFormulas.getCustomCoastFile(coastFileName);
				}
				catch (Exception exx) {
					ex.printStackTrace();
					throw ex;
				}
			}
		}

		// got the file, now parse it and plot it
		FileInputStream in = new FileInputStream(coarseFile);
		BufferedInputStream bis = new BufferedInputStream(in, 1000000);
		DataInputStream inData = new DataInputStream(bis);
		g.setColor(mMapSpec.getCoastColor());
		try {
			while (true) {
				// get the number of entries
				int numEntries = inData.readShort();
				if (numEntries == -1) {
					break;
				}

				// get the lats and lons for this segment
				for (int i = 0; i < numEntries; i++) {
					lat[i] = inData.readDouble();
				}

				for (int i = 0; i < numEntries; i++) {
					lon[i] = inData.readDouble();
				}

				// plot this segment
				int s = 0;
				while (!LLKeep(lat[s], lon[s]) && s < numEntries) {
					s++;
				}
				UVCoordinate uv = transformLL(lat[s], lon[s]);
				uv = mapScaler(uv.u, uv.v);
				double oldU = uv.u;
				double oldV = uv.v;
				for (int i = 1; i < numEntries; i++) {
					if (LLKeep(lat[i], lon[i])) {
						uv = transformLL(lat[i], lon[i]);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							if (Math.abs(oldU - (int)uv.u) < 50) {
								g.drawLine((int)oldU, (int)oldV, (int)uv.u, (int)uv.v);
							}

						}
						oldU = uv.u;
						oldV = uv.v;
					}
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public void plotBorder(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;

		if (mMapSpec.getProjection() <= JOAConstants.MILLERPROJECTION) {
			mCurrClipShape = rectangularMapBoundary(mMapSpec.getLonLft(), mMapSpec.getLonRt(), mMapSpec.getLatMin(), mMapSpec
			    .getLatMax(), g, mMapSpec.getGratColor());
		}
		else if (mMapSpec.isGlobe() && mMapSpec.getProjection() >= JOAConstants.ORTHOGRAPHICPROJECTION
		    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			// if these are small scale global maps then don't draw a border otherwise
			// attempt a border.
			plotGrid(g);
		}
		else if (!mMapSpec.isGlobe() && mMapSpec.getProjection() >= JOAConstants.ORTHOGRAPHICPROJECTION
		    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
			// if these are small scale global maps then don't draw a border otherwise
			// attempt a border.
			nonRectangularMapBoundary(mMapSpec.getLonLft(), mMapSpec.getLonRt(), mMapSpec.getLatMin(), mMapSpec.getLatMax(),
			    mMapSpec.getCenLon(), g, mMapSpec.getGratColor());
		}
		else if (mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			// draw a border
			plotNorthPolarGrid(g);
		}
		else if (mMapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION) {
			// draw a border
			plotSouthPolarGrid(g);
		}

	}

	public Shape rectangularMapBoundary(double west, double east, double south, double north, Graphics2D g, Color c) {
		double val, v1, v2, dx, dy;
		int nx, ny;
		double clleft, clright, cltop, clbottom;

		g.setStroke(lw1);
		g.setColor(c);

		// select a font for labels
		g.setFont(new Font(JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
		    JOAConstants.DEFAULT_MAP_VALUE_SIZE));
		FontMetrics fm = g.getFontMetrics();
		int charHeight = JOAConstants.DEFAULT_MAP_VALUE_SIZE - 1;

		// Draw western boundary
		UVCoordinate uv = transformLL(south, west);
		uv = mapScaler(uv.u, uv.v);
		UVCoordinate uv2 = transformLL(north, west);
		uv2 = mapScaler(uv2.u, uv2.v);
		g.drawLine((int)uv.u, (int)uv.v, (int)uv2.u, (int)uv2.v);
		g.drawLine((int)uv.u - 5, (int)uv.v + 5, (int)uv2.u - 5, (int)uv2.v - 5);
		clleft = uv.u + 1.0;

		// Draw eastern boundary
		uv = transformLL(south, east);
		uv = mapScaler(uv.u, uv.v);
		uv2 = transformLL(north, east);
		uv2 = mapScaler(uv2.u, uv2.v);
		g.drawLine((int)uv.u, (int)uv.v, (int)uv2.u, (int)uv2.v);
		g.drawLine((int)uv.u + 5, (int)uv.v + 5, (int)uv2.u + 5, (int)uv2.v - 5);
		clright = uv.u - 1.0;

		// Draw southern boundary
		uv = transformLL(south, west);
		uv = mapScaler(uv.u, uv.v);
		uv2 = transformLL(south, east);
		uv2 = mapScaler(uv2.u, uv2.v);
		g.drawLine((int)uv.u, (int)uv.v, (int)uv2.u, (int)uv2.v);
		g.drawLine((int)uv.u - 5, (int)uv.v + 5, (int)uv2.u + 5, (int)uv2.v + 5);
		clbottom = uv.v - 1.0;

		// Draw northern boundary
		uv = transformLL(north, west);
		uv = mapScaler(uv.u, uv.v);
		uv2 = transformLL(north, east);
		uv2 = mapScaler(uv2.u, uv2.v);
		g.drawLine((int)uv.u, (int)uv.v, (int)uv2.u, (int)uv2.v);
		cltop = uv.v + 1.0;
		g.drawLine((int)uv.u - 5, (int)uv.v - 5, (int)uv2.u + 5, (int)uv2.v - 5);

		Rectangle2D returnClip = new Rectangle2D.Double(cltop, clleft, clright - clleft, clbottom - cltop);

		// Draw frame grid for west/east boundaries
		dy = mMapSpec.getLatGratSpacing();
		boolean shade = (((int)Math.floor(south / dy) + 1) % 2) > 0;
		double south1 = Math.floor(south / dy) * dy;
		ny = (south1 > north) ? -1 : (int)((north - south1) / dy + SMALL);
		for (int i = 0; i <= ny; i++) {
			g.setColor(c);
			val = south1 + i * dy;
			g.setStroke(lw1);
			v1 = (val < south) ? south : val;
			uv = transformLL(v1, west);
			uv = mapScaler(uv.u, uv.v);
			uv2 = transformLL(v1, east);
			uv2 = mapScaler(uv2.u, uv2.v);
			g.drawLine((int)uv.u - 7, (int)uv.v, (int)uv2.u + 7, (int)uv2.v);
			g.setStroke(lw1);

			// plotlabels along left boundary
			if (mMapSpec.isPlotGratLabels()) {
				g.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);
				String label = formatLat(v1);
				int w = fm.stringWidth(label);
				g.drawString(label, (int)uv.u - 10 - w, (int)uv.v + charHeight / 2);
				g.setColor(c);
			}
			if (shade) {
				v2 = val + dy;
				if (v2 > north) {
					v2 = north;
				}

				UVCoordinate uv3 = transformLL(v2, west);
				uv3 = mapScaler(uv3.u, uv3.v);
				g.fillRect((int)uv3.u - 5, (int)uv3.v, 5, (int)(uv.v - uv3.v));

				uv3 = transformLL(v2, east);
				uv3 = mapScaler(uv3.u, uv3.v);
				g.fillRect((int)uv3.u, (int)uv3.v, 5, (int)(uv2.v - uv3.v));
				shade = false;
				if (v2 == 0.0) {
					g.setStroke(lw2);
				}
				g.drawLine((int)uv.u - 7, (int)(uv.v - (uv.v - uv3.v)), (int)uv2.u + 7, (int)(uv2.v - (uv2.v - uv3.v)));
				g.setStroke(lw1);
			}
			else {
				if (val == 0.0) {
					g.setStroke(lw2);
				}
				g.drawLine((int)uv.u - 7, (int)uv.v, (int)uv2.u + 7, (int)uv2.v);
				g.setStroke(lw1);
				shade = true;

				// plot labels along left boundary
				if (mMapSpec.isPlotGratLabels()) {
					g.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);
					String label = formatLat(v1);
					int w = fm.stringWidth(label);
					g.drawString(label, (int)uv.u - 10 - w, (int)uv.v + charHeight / 2);
					g.setColor(c);
				}
			}
		}

		// Draw Frame grid for N and S boundaries
		dx = mMapSpec.getLonGratSpacing();
		shade = (((int)Math.floor(west / dx) + 1) % 2) > 0;
		double west1 = Math.floor(west / dx) * dx;
		double lwest = west1;
		if (west1 < west && shade) {
			west1 = west;
		}
		double least = east;
		boolean isGlobal = isGlobalMap(west, east);
		if (isGlobal) {
			nx = getNumLonIntervals(west, east, dx);
		}
		else {
			nx = getNumLonIntervals(west1, east, dx);
		}

		if (nx <= 0) {
			nx = 1;
		}

		// width of map in pixels
		uv = transformLL(south, west);
		uv = mapScaler(uv.u, uv.v);
		uv2 = transformLL(south, east);
		uv2 = mapScaler(uv2.u, uv2.v);

		// find the number of labels that will fit
		int mapWidth = (int)Math.abs(uv.u - uv2.u);
		int pixelsPerLonBand = mapWidth / nx;
		int labelHInc = 1;
		int maxWidth = 0;
		for (int i = 0; i < nx; i++) {
			val = west1 + i * dx;
			if (val > 180) {
				val -= 360;
				// crossed 180
				lwest = -180.0;

			}
			if (val < lwest) {
				v1 = lwest;
			}
			else {
				v1 = val;
			}
			String sTemp = null;
			sTemp = formatLon(v1);
			int len = fm.stringWidth(sTemp);
			maxWidth = len > maxWidth ? len : maxWidth;
		}

		// find the number of labels that will fit
		if (maxWidth + 30 > pixelsPerLonBand) {
			// compute how many labels to skip
			int numFit = (int)Math.round((nx * pixelsPerLonBand) / (maxWidth + 30));
			labelHInc = (int)(Math.round((double)nx / (double)numFit));
		}

		lwest = west1;
		for (int i = 0; i <= nx; i++) {
			g.setColor(c);
			val = west1 + i * dx;
			if (val > 180) {
				val -= 360;
				// crossed 180
				lwest = -180.0;

			}
			if (val < lwest) {
				v1 = lwest;
			}
			else {
				v1 = val;
			}
			uv = transformLL(south, v1);
			uv = mapScaler(uv.u, uv.v);
			uv2 = transformLL(north, v1);
			uv2 = mapScaler(uv2.u, uv2.v);
			int right = (int)uv2.u;
			int width = right - (int)uv.u;
			if (shade) {
				v2 = val + dx;
				if (v2 > least && least > 0 && !isGlobal) {
					v2 = least;
				}
				if (v2 < 0 && least < 0 && v2 > least && !isGlobal) {
					v2 = least;
				}
				g.setStroke(lw1);
				if (val == 0.0 /* || val == 180.0 || val == -180.0 */) {
					g.setStroke(lw2);
				}
				g.drawLine((int)uv.u, (int)uv.v + 7, (int)uv.u, (int)uv2.v - 7);
				g.setStroke(lw1);

				UVCoordinate uv3 = transformLL(south, v2);
				uv3 = mapScaler(uv3.u, uv3.v);
				width = (int)Math.round(uv3.u - uv2.u);
				g.fillRect((int)uv.u, (int)uv.v, width, 5); // (int)(uv3.u - uv.u),
				// 5);
				UVCoordinate uv4 = transformLL(north, v2);
				uv4 = mapScaler(uv4.u, uv4.v);
				g.fillRect((int)uv2.u, (int)uv2.v - 5, width, 5); // (int)(uv3.u -
				// uv.u), 5);
				if (v2 == 0.0 /* || v2 == 180.0 || v2 == -180.0 */) {
					g.setStroke(lw2);
				}
				g.drawLine((int)uv2.u + width, (int)uv3.v + 7, (int)uv2.u + width, (int)uv4.v - 7);
				g.setStroke(lw1);
				shade = false;
			}
			else {
				shade = true;
			}
		}

		lwest = west1;
		for (int i = 0; i <= nx; i++) {
			g.setColor(c);
			val = west1 + i * dx;
			if (val > 180) {
				val -= 360;
				// crossed 180
				lwest = -180.0;

			}
			if (val < lwest) {
				v1 = lwest;
			}
			else {
				v1 = val;
			}
			uv = transformLL(south, v1);
			uv = mapScaler(uv.u, uv.v);

			// plot labels along bottom boundary
			if (mMapSpec.isPlotGratLabels() && i % labelHInc == 0) {
				g.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);
				String label = formatLon(v1);
				int w = fm.stringWidth(label);
				g.drawString(label, (int)uv.u - w / 2, (int)uv.v + charHeight + 10);
				g.setColor(c);
			}
		}
		return returnClip;
	}

	public void nonRectangularMapBoundary(double west, double east, double south, double north, double cenLon,
	    Graphics2D g, Color c) {
		int nx;
		GeneralPath aLine, shadedRgn;
		BasicStroke lw1 = new BasicStroke(1);
		BasicStroke lw2 = new BasicStroke(2);
		UVCoordinate uv, uv2;
		boolean lonLabelBorderIsStraight = false;
		try {
			g.setStroke(lw1);

			// get the bounds of the map
			UVCoordinate latBounds = getBoundingLat(south, west, -0.01, 5.0, 5.0);
			double minticendlatsw = latBounds.v;
			double minouterlatsw = latBounds.u;
			latBounds = getBoundingLat(south, east, -0.01, 5.0, 5.0);
			double minouterlatse = latBounds.u;

			latBounds = getBoundingLat(north, west, +0.01, 5.0, 5.0);
			double maxticendlatnw = latBounds.v;
			double maxouterlatnw = latBounds.u;
			latBounds = getBoundingLat(north, east, +0.01, 5.0, 5.0);
			double maxouterlatne = latBounds.u;

			UVCoordinate lonBounds = getBoundingLon(north, east, +0.01, 5.0, 5.0);
			lonBounds = getBoundingLon(south, east, +0.01, 5.0, 5.0);

			lonBounds = getBoundingLon(north, west, -0.01, 5.0, 5.0);
			lonBounds = getBoundingLon(south, west, -0.01, 5.0, 5.0);

			// check whether the bounding parallels are horizontal lines or curved
			uv = transformLL(south, west);
			uv = mapScaler(uv.u, uv.v);

			uv2 = transformLL(south, cenLon);
			uv2 = mapScaler(uv2.u, uv2.v);

			if (Math.abs(uv.v - uv2.v) < 10) {
				lonLabelBorderIsStraight = true;
			}

			// select a font for labels
			g.setFont(new Font(JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
			    JOAConstants.DEFAULT_MAP_VALUE_SIZE));
			FontMetrics fm = g.getFontMetrics();
			aLine = new GeneralPath();
			shadedRgn = new GeneralPath();

			// determine whether the lon labels are on the north or south of the map
			double lon = west;
			int cnt = 0;
			int maxCnt = (int)(this.getLonRange(west, east) / mMapSpec.getLonGratSpacing());
			int maxLonLabelLen = 0;
			double ndist = 0.0;
			double sdist = 0.0;
			while (cnt <= maxCnt) {
				lon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
				cnt++;
				double nlon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
				if (lon >= 180) {
					lon -= 360.0;
				}
				uv = transformLL(north, lon);
				uv = mapScaler(uv.u, uv.v);
				uv2 = transformLL(north, nlon);
				uv2 = mapScaler(uv2.u, uv2.v);
				ndist += Math.sqrt(((uv.u - uv2.u) * (uv.u - uv2.u)) + ((uv.v - uv2.v) * (uv.v - uv2.v)));

				uv = transformLL(south, lon);
				uv = mapScaler(uv.u, uv.v);
				uv2 = transformLL(south, nlon);
				uv2 = mapScaler(uv2.u, uv2.v);
				sdist += Math.sqrt(((uv.u - uv2.u) * (uv.u - uv2.u)) + ((uv.v - uv2.v) * (uv.v - uv2.v)));
			}

			// find out the longest longitude label
			lon = west;
			cnt = 0;
			maxCnt = (int)(this.getLonRange(west, east) / mMapSpec.getLonGratSpacing());
			maxLonLabelLen = 0;
			while (cnt <= maxCnt) {
				lon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
				if (lon >= 180) {
					lon -= 360.0;
				}
				cnt++;
				String lonLabel = formatLon(lon);
				int w = fm.stringWidth(lonLabel);
				if (w > maxLonLabelLen) {
					maxLonLabelLen = w;
				}
			}

			double pixelsPerLonBand = 15;
			int labelHInc = 1;
			boolean useNorth = false;
			if (sdist > ndist) {
				pixelsPerLonBand = sdist / (double)maxCnt;
				useNorth = false;
			}
			else {
				pixelsPerLonBand = ndist / (double)maxCnt;
				useNorth = true;
			}

			// find the number of labels that will fit
			if (maxLonLabelLen + 30 > pixelsPerLonBand) {
				nx = getNumLonIntervals(west, east, mMapSpec.getLonGratSpacing());
				// compute how many labels to skip
				int numFit = (int)(nx * pixelsPerLonBand) / (maxLonLabelLen + 30);
				labelHInc = (int)(Math.round((double)nx / (double)numFit));
			}

			// Draw parallels of latitude
			double lat = south;
			cnt = 0;
			maxCnt = (int)(Math.abs(south - north) / mMapSpec.getLatGratSpacing());
			boolean shade = (((int)Math.floor(south / mMapSpec.getLatGratSpacing()) + 1) % 2) > 0;
			while (cnt <= maxCnt) {
				lat = mMapSpec.getLatMin() + (cnt * mMapSpec.getLatGratSpacing());
				cnt++;

				// compute the longitude of the endpoints at this latitude
				lonBounds = getBoundingLon(lat, west, -0.01, 5.0, 5.0);
				double ticendLonLeft = lonBounds.v;
				double outerLonLeft = lonBounds.u;
				lonBounds = getBoundingLon(lat, east, +0.01, 5.0, 5.0);
				double ticendLonRight = lonBounds.v;

				// draw a line of latitude as segments
				lon = ticendLonLeft;
				float oldu = -99;
				float oldv = -99;
				int c2 = 0;
				while (isLegalLon(lon, ticendLonLeft, ticendLonRight)) {
					if (c2 > 0) {
						lon += 0.5;
					}
					c2++;
					if (lon > 180) {
						lon -= 360;
					}
					uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (oldu != -99) {
						aLine.moveTo(oldu, oldv);
						aLine.lineTo((float)uv.u, (float)uv.v);
					}
					oldu = (float)uv.u;
					oldv = (float)uv.v;
				}
				try {
					aLine.closePath();
					g.setColor(mMapSpec.getGratColor());
					if (lat != 0.0) {
						g.setStroke(lw1);
					}
					else {
						g.setStroke(lw2);
					}
					g.draw(aLine);
				}
				catch (Exception ex) {
				}

				if (shade) {
					// draw a shaded region
					double nlat = south + (cnt * mMapSpec.getLatGratSpacing());
					if (nlat > north) {
						nlat = north;
					}

					// loop on latitudes in range
					// Polygon poly = new Polygon();
					shadedRgn.reset();
					boolean first = true;
					for (double dl = lat; dl <= nlat; dl += 0.5) {
						lonBounds = getBoundingLon(dl, west, -0.01, 5.0, 5.0);
						double nouterLonLeft = lonBounds.u;
						uv = transformLL(dl, nouterLonLeft);
						uv = mapScaler(uv.u, uv.v);
						if (first) {
							shadedRgn.moveTo((float)uv.u, (float)uv.v);
							first = false;
						}
						else {
							shadedRgn.lineTo((float)uv.u, (float)uv.v);
						}
					}
					uv = transformLL(nlat, west);
					uv = mapScaler(uv.u, uv.v);
					shadedRgn.lineTo((float)uv.u, (float)uv.v);
					;
					for (double dl = nlat; dl >= lat; dl += -0.5) {
						uv = transformLL(dl, west);
						uv = mapScaler(uv.u, uv.v);
						shadedRgn.lineTo((float)uv.u, (float)uv.v);
					}
					shadedRgn.closePath();
					g.setPaint(mMapSpec.getGratColor());
					g.fill(shadedRgn);

					// loop on latitudes in range
					shadedRgn.reset();
					first = true;
					for (double dl = lat; dl <= nlat; dl += 0.5) {
						lonBounds = getBoundingLon(dl, east, +0.01, 5.0, 5.0);
						double nouterLonLeft = lonBounds.u;
						uv = transformLL(dl, nouterLonLeft);
						uv = mapScaler(uv.u, uv.v);
						if (first) {
							shadedRgn.moveTo((float)uv.u, (float)uv.v);
							first = false;
						}
						else {
							shadedRgn.lineTo((float)uv.u, (float)uv.v);
						}
					}
					uv = transformLL(nlat, east);
					uv = mapScaler(uv.u, uv.v);
					shadedRgn.lineTo((float)uv.u, (float)uv.v);
					for (double dl = nlat; dl >= lat; dl += -0.5) {
						uv = transformLL(dl, east);
						uv = mapScaler(uv.u, uv.v);
						shadedRgn.lineTo((float)uv.u, (float)uv.v);
					}
					shadedRgn.closePath();
					g.setPaint(mMapSpec.getGratColor());
					g.fill(shadedRgn);
					shade = false;
				}
				else {
					shade = true;
				}

				if (mMapSpec.isPlotGratLabels()) {
					// form the string to draw
					String latLabel = formatLat(lat);
					double strw = fm.stringWidth(latLabel) / 2.0;

					// get the x,y coordinates of the right side of the label
					uv = transformLL(lat, ticendLonLeft);
					uv = mapScaler(uv.u, uv.v);

					double xr = uv.u;
					double yr = uv.v;

					// get the x,y coordinates of the left side of the label
					lonBounds = getBoundingLon(lat, ticendLonLeft, -0.01, strw, 0.0);
					outerLonLeft = lonBounds.u;
					uv = transformLL(lat, outerLonLeft);
					uv = mapScaler(uv.u, uv.v);

					double xl = uv.u;
					double yl = uv.v;

					// get the angle of rotation
					double angle = JOAFormulas.getAngle(xr, xl, yr, yl);
					angle = 360 + angle;

					double h = uv.u;
					double hoffset = -strw - 5;

					// draw the string
					JOAFormulas.drawStyledString(latLabel, h, uv.v, hoffset, 5, g, angle, JOAConstants.DEFAULT_MAP_VALUE_FONT,
					    JOAConstants.DEFAULT_MAP_VALUE_SIZE, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
					    JOAConstants.DEFAULT_MAP_VALUE_COLOR);
					g.setColor(mMapSpec.getGratColor());
				}
			}

			// Draw meridians
			lon = west;
			cnt = 0;
			maxCnt = (int)(this.getLonRange(west, east) / mMapSpec.getLonGratSpacing());
			shade = (((int)Math.floor(west / mMapSpec.getLonGratSpacing()) + 1) % 2) > 0;
			int l = 0;
			while (cnt <= maxCnt) {
				lon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
				if (lon >= 180) {
					lon -= 360.0;
				}
				cnt++;
				// draw a line of longitude as segments
				lat = minticendlatsw;
				double ulat = lat;
				float oldu = -99;
				float oldv = -99;
				int c2 = 0;
				while (isLegalLat(lat, minticendlatsw, maxticendlatnw)) {
					if (c2 > 0) {
						lat += 0.5;
					}
					if (lat > maxticendlatnw) {
						ulat = maxticendlatnw;
					}
					else {
						ulat = lat;
					}
					c2++;
					uv = transformLL(ulat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (oldu != -99) {
						aLine.moveTo(oldu, oldv);
						aLine.lineTo((float)uv.u, (float)uv.v);
					}
					oldu = (float)uv.u;
					oldv = (float)uv.v;
				}
				try {
					aLine.closePath();
					g.setColor(mMapSpec.getGratColor());
					if (lat != 0.0) {
						g.setStroke(lw1);
					}
					else {
						g.setStroke(lw2);
					}
					g.draw(aLine);
				}
				catch (Exception ex) {
				}

				if (shade) {
					g.setColor(mMapSpec.getGratColor());
					// draw a shaded region
					double nlon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
					if (nlon >= 180) {
						nlon -= 360.0;
					}
					if (!isLegalLon(nlon, west, east)) {
						nlon = east;
					}
					// loop on longitudes in range
					shadedRgn.reset();
					boolean first = true;
					for (double dl = lon; dl <= nlon; dl += 0.5) {
						uv = transformLL(north, dl);
						uv = mapScaler(uv.u, uv.v);
						if (first) {
							shadedRgn.moveTo((float)uv.u, (float)uv.v);
							first = false;
						}
						else {
							shadedRgn.lineTo((float)uv.u, (float)uv.v);
						}
					}
					uv = transformLL(maxouterlatnw, nlon);
					uv = mapScaler(uv.u, uv.v);
					shadedRgn.lineTo((float)uv.u, (float)uv.v);
					for (double dl = nlon; dl >= lon; dl += -0.5) {
						uv = transformLL(maxouterlatnw, dl);
						uv = mapScaler(uv.u, uv.v);
						shadedRgn.lineTo((float)uv.u, (float)uv.v);
					}
					shadedRgn.closePath();
					g.setPaint(mMapSpec.getGratColor());
					g.fill(shadedRgn);

					shadedRgn.reset();
					first = true;
					for (double dl = lon; dl <= nlon; dl += 0.5) {
						uv = transformLL(south, dl);
						uv = mapScaler(uv.u, uv.v);
						if (first) {
							shadedRgn.moveTo((float)uv.u, (float)uv.v);
							first = false;
						}
						else {
							shadedRgn.lineTo((float)uv.u, (float)uv.v);
						}
					}
					uv = transformLL(minouterlatsw, nlon);
					uv = mapScaler(uv.u, uv.v);
					shadedRgn.lineTo((float)uv.u, (float)uv.v);
					for (double dl = nlon; dl >= lon; dl += -0.5) {
						uv = transformLL(minouterlatsw, dl);
						uv = mapScaler(uv.u, uv.v);
						shadedRgn.lineTo((float)uv.u, (float)uv.v);
					}
					shadedRgn.closePath();
					g.setPaint(mMapSpec.getGratColor());
					g.fill(shadedRgn);
					shade = false;
				}
				else {
					shade = true;
				}

				if (mMapSpec.isPlotGratLabels() && l++ % labelHInc == 0) {
					// form the string to draw
					String lonLabel = formatLon(lon);
					double strw = fm.stringWidth(lonLabel) / 2.0;

					double inc = 0.01;
					int offset = 20;
					if (useNorth) {
						lat = north;
						offset = -10;
					}
					else {
						lat = south;
					}

					// get the x,y coordinates of the right side of the label
					uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);

					double xl = uv.u;
					double yl = uv.v;

					// get the x,y coordinates of the left side of the label
					latBounds = getBoundingLat(lat, lon, inc, 20, 0.0);
					double outerLat = latBounds.u;
					uv2 = transformLL(outerLat, lon);
					uv2 = mapScaler(uv2.u, uv2.v);

					double xr = uv2.u;
					double yr = uv2.v;

					// get the angle of rotation
					double angle = JOAFormulas.getAngle(yr, yl, xr, xl);
					if (lonLabelBorderIsStraight) {
						angle = 0.0;
					}

					double h = uv.u;
					double hoffset = -strw - 5;

					// draw the string
					JOAFormulas.drawStyledString(lonLabel, h, uv.v, hoffset, offset, g, angle,
					    JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_SIZE,
					    JOAConstants.DEFAULT_MAP_VALUE_STYLE, JOAConstants.DEFAULT_MAP_VALUE_COLOR);
					g.setColor(mMapSpec.getGratColor());
				}
			}

			// Draw western inner boundary
			lat = south;
			float oldu = -99;
			float oldv = -99;
			int c2 = 0;
			while (lat <= north) {
				if (c2 > 0) {
					lat += 0.5;
				}
				c2++;
				uv = transformLL(lat, west);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("western inner boundary");
			}

			// Draw eastern inner boundary
			aLine = new GeneralPath();
			lat = south;
			oldu = -99;
			oldv = -99;
			c2 = 0;
			while (lat <= north) {
				if (c2 > 0) {
					lat += 0.5;
				}
				c2++;
				uv = transformLL(lat, east);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("eastern inner boundary");
			}

			// Draw northern inner boundary
			aLine = new GeneralPath();
			lon = west;
			oldu = -99;
			oldv = -99;
			c2 = 0;
			while (isLegalLon(lon, west, east)) {
				if (c2 > 0) {
					lon += 0.5;
				}
				c2++;
				if (lon >= 180) {
					lon -= 360.0;
				}
				uv = transformLL(north, lon);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("northern inner boundary");
			}

			// Draw southern inner boundary
			aLine = new GeneralPath();
			lon = west;
			oldu = -99;
			oldv = -99;
			c2 = 0;
			while (isLegalLon(lon, west, east)) {
				if (c2 > 0) {
					lon += 0.5;
				}
				c2++;
				if (lon >= 180) {
					lon -= 360.0;
				}
				uv = transformLL(south, lon);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("southern inner boundary");
			}

			// Draw western outer boundary
			aLine = new GeneralPath();
			lat = minouterlatsw;
			oldu = -99;
			oldv = -99;
			cnt = 0;
			boolean end = false;
			while (lat <= maxouterlatnw && !end) {
				if (cnt > 0) {
					lat += 0.5;
				}
				if (lat > maxouterlatnw) {
					lat = maxouterlatnw;
					end = true;
				}
				cnt++;
				lonBounds = getBoundingLon(lat, west, -0.01, 5.0, 5.0);
				double nouterLonLeft = lonBounds.u;
				uv = transformLL(lat, nouterLonLeft);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("western outer boundary");
			}

			// Draw eastern outer boundary
			aLine = new GeneralPath();
			lat = minouterlatse;
			oldu = -99;
			oldv = -99;
			cnt = 0;
			end = false;
			while (lat <= maxouterlatne && !end) {
				if (cnt > 0) {
					lat += 0.5;
				}
				if (lat > maxouterlatnw) {
					lat = maxouterlatnw;
					end = true;
				}
				cnt++;
				lonBounds = getBoundingLon(lat, east, +0.01, 5.0, 5.0);
				double nouterLonRight = lonBounds.u;
				uv = transformLL(lat, nouterLonRight);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("eastern outer boundary");
			}

			// Draw northern outer boundary
			aLine = new GeneralPath();
			lonBounds = getBoundingLon(maxouterlatnw, west, -0.01, 5.0, 5.0);
			double stlon = lonBounds.u;
			lonBounds = getBoundingLon(maxouterlatnw, east, +0.01, 5.0, 5.0);
			double endlon = lonBounds.u;
			oldu = -99;
			oldv = -99;
			c2 = 0;
			lon = stlon;
			while (isLegalLon(lon, stlon, endlon)) {
				if (c2 > 0) {
					lon += 0.5;
				}
				c2++;
				if (lon > 180) {
					lon -= 360;
				}

				uv = transformLL(lat, lon);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
				System.out.println("northern outer boundary");
			}

			// Draw southern outer boundary
			aLine = new GeneralPath();
			lonBounds = getBoundingLon(minouterlatsw, west, -0.01, 5.0, 5.0);
			stlon = lonBounds.u;
			lonBounds = getBoundingLon(minouterlatsw, east, +0.01, 5.0, 5.0);
			endlon = lonBounds.u;
			oldu = -99;
			oldv = -99;
			c2 = 0;
			lon = stlon;
			while (isLegalLon(lon, stlon, endlon)) {
				if (c2 > 0) {
					lon += 0.5;
				}
				c2++;
				if (lon >= 180) {
					lon -= 360.0;
				}
				uv = transformLL(minouterlatsw, lon);
				uv = mapScaler(uv.u, uv.v);
				if (oldu != -99) {
					aLine.moveTo(oldu, oldv);
					aLine.lineTo((float)uv.u, (float)uv.v);
				}
				oldu = (float)uv.u;
				oldv = (float)uv.v;
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
			}
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public boolean isLegalLon(double lon, double left, double right) {
		if (lon > 180) {
			lon -= 360;
		}
		if (left >= 0 && right > 0) {
			return (lon >= left && lon <= right);
		}
		else if (left > 0 && right < 0) {
			if (lon >= 0) {
				return (lon >= left && lon <= 180);
			}
			else {
				return (lon <= right);
			}
		}
		else if (left < 0 && right < 0) {
			return (lon >= left && lon <= right);
		}
		else if (left < 0 && right >= 0) { return (lon >= left && lon <= right); }
		return false;
	}

	public boolean isLegalLat(double lat, double south, double north) {
		return (lat >= south && lat <= north);
	}

	public int getLonRange(double west, double east) {
		int numDegs;
		if (west > 0 && east < 0) {
			numDegs = (int)((180 - west) + (180 + east));
		}
		else if (Math.abs(east - west) < 1) {
			numDegs = 360;
		}
		else {
			numDegs = (int)(east - west);
		}
		return numDegs;
	}

	public UVCoordinate getBoundingLat(double lat, double lon, double inc, double mdist, double ticoffset) 
			throws Exception{
		double outerlat = lat;
		double ticendlat = lat;
		UVCoordinate uv = transformLL(lat, lon);
		uv = mapScaler(uv.u, uv.v);
		double ui = uv.u;
		double vi = uv.v;
		boolean oLatFnd = false;
		boolean tLatFnd = false;
		int count = 0;
		while (!oLatFnd || !tLatFnd) {
			lat += inc;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			double uc = uv.u;
			double vc = uv.v;
			double dist = Math.sqrt(((ui - uc) * (ui - uc)) + ((vi - vc) * (vi - vc)));
			if (dist >= mdist && !oLatFnd) {
				outerlat = lat;
				oLatFnd = true;
			}
			else if (dist >= mdist + ticoffset && !tLatFnd) {
				ticendlat = lat;
				tLatFnd = true;
			}
			if (++count > 10000) {
				String errStr = "getBoundingLat: " + lat + ", " + lon + ", " + inc + ", " + mdist + ", " + ticoffset + "\n";
				errStr += "oLatFnd " + oLatFnd + "\n";
				errStr += "tLatFnd " + tLatFnd + "\n";
				errStr += "outerlat " + outerlat + "\n";
				errStr += "ticendlat " + ticendlat + "\n";
				throw new Exception(errStr);
			}
		}
		return new UVCoordinate(outerlat, ticendlat);
	}

	public UVCoordinate getBoundingLon(double lat, double lon, double inc, double mdist, double ticoffset) 
		throws Exception {
		double outerlon = lon;
		double ticendlon = lon;
		UVCoordinate uv = transformLL(lat, lon);
		uv = mapScaler(uv.u, uv.v);
		double ui = uv.u;
		double vi = uv.v;
		boolean oLonFnd = false;
		boolean tLonFnd = false;
		int count = 0;
		while (!oLonFnd || !tLonFnd) {
			lon += inc;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			double uc = uv.u;
			double vc = uv.v;
			double dist = Math.sqrt(((ui - uc) * (ui - uc)) + ((vi - vc) * (vi - vc)));
			if (dist >= mdist && !oLonFnd) {
				outerlon = lon;
				oLonFnd = true;
			}
			else if (dist >= mdist + ticoffset && !tLonFnd) {
				ticendlon = lon;
				tLonFnd = true;
			}
			if (++count > 10000) {
				String errStr = "getBoundingLon: " + lat + ", " + lon + ", " + inc + ", " + mdist + ", " + ticoffset + "\n";
				errStr += "oLonFnd " + oLonFnd + "\n";
				errStr += "tLonFnd " + tLonFnd + "\n";
				errStr += "outerlon " + outerlon + "\n";
				errStr += "ticendlon " + ticendlon + "\n";
				throw new Exception(errStr);
			}
		}
		return new UVCoordinate(outerlon, ticendlon);
	}

	public String formatLat(double inLat) {
		if (inLat == 0) {
			return new String("EQ");
		}
		else if (inLat < 0) {
			return new String(JOAFormulas.formatDouble(String.valueOf(-inLat), 0, false) + '\u00B0' + " S");
		}
		else {
			return new String(JOAFormulas.formatDouble(String.valueOf(inLat), 0, false) + '\u00B0' + " N");
		}
	}

	public String formatLon(double inLon) {
		if (inLon < 0) {
			return new String(JOAFormulas.formatDouble(String.valueOf(-inLon), 0, false) + '\u00B0' + " W");
		}
		else {
			return new String(JOAFormulas.formatDouble(String.valueOf(inLon), 0, false) + '\u00B0' + " E");
		}
	}

	public boolean isGlobalMap(double west, double east) {
		return Math.abs(west - east) < 0.5;
	}

	public int getNumLonIntervals(double west, double east, double dx) {
		int nx;
		if (Math.abs(west - east) < 1.0 && east > 0 && west > 0) {
			nx = (int)((west + 360 - east) / dx + SMALL);
		}
		else if (Math.abs(west - east) < 0.5 && east < 0 && west < 0) {
			nx = (int)(360.0 / dx + SMALL);
		}
		else if (west > east && east > 0) {
			nx = (int)((west + Math.abs(east)) / dx + SMALL);
		}
		else if (west > east && east < 0) {
			nx = (int)(((180 - west) + (180 + east)) / dx + SMALL);
		}
		else {
			nx = (int)((east - west) / dx + SMALL);
		}
		return nx;
	}

	public void plotNorthPolarGrid(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;
		// plotGrid DRAWS THE GRID ON THE MAP.
		double lat, lon, dlon, latdeg, londeg, stlat, oldU, oldV;
		UVCoordinate uv = null;
		GeneralPath aLine = null;
		BasicStroke lw1 = new BasicStroke(1);
		BasicStroke lw2 = new BasicStroke(2);

		// select a font for labels
		g.setFont(new Font(JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
		    JOAConstants.DEFAULT_MAP_VALUE_SIZE));
		FontMetrics fm = g.getFontMetrics();
		int charHeight = JOAConstants.DEFAULT_MAP_VALUE_SIZE - 1;

		// set the graticule color
		g.setColor(mMapSpec.getGratColor());

		// compute the longitude for the outer ring
		// compute the longitude for meridians w/tic marks
		UVCoordinate minLatBounds = null;
    try {
	    minLatBounds = getBoundingLat(mMapSpec.getLatMin(), mMapSpec.getCenLon(), -0.01, 5.0, 5.0);
    }
    catch (Exception e) {
    		// silent
    }
    
    if (minLatBounds == null) {
    	System.out.println("Error getting bounding Lat in plotNorthPolarGrid");
    	System.out.println("latMin = " + mMapSpec.getLatMin());
    	System.out.println("cenLon = " + mMapSpec.getCenLon());
    	System.out.println("other args = -0.01, 5.0, 5.0");
    	return;
    }
    
		double ticendlat = minLatBounds.v;
		double outerlat = minLatBounds.u;
		lon = mMapSpec.getCenLon();

		// set the graticule spacing
		if (mMapSpec.isAutoGraticule()) {
			double deltaLat = Math.abs(mMapSpec.getLatMax() - mMapSpec.getLatMin());
			double deltaLon = Math.abs(mMapSpec.getLonRt() - mMapSpec.getLonLft());
			if (deltaLat >= 100 || deltaLon >= 100) {
				londeg = 10.0;
				latdeg = 10.0;
			}
			else if (deltaLat >= 10 || deltaLon >= 10) {
				londeg = 5.0;
				latdeg = 5.0;
			}
			else {
				londeg = 1.0;
				latdeg = 1.0;
			}
			mMapSpec.setLonGratSpacing(londeg);
			mMapSpec.setLatGratSpacing(latdeg);
		}
		else {
			londeg = mMapSpec.getLonGratSpacing();
			latdeg = mMapSpec.getLatGratSpacing();
		}

		// start at the central meridian and draw lines
		dlon = mMapSpec.getLonLft();
		int cnt = 0;
		int maxCnt = (int)(360.0 / mMapSpec.getLonGratSpacing());
		boolean shade = true;
		double iLat = mMapSpec.getLatMin();
		while (cnt < maxCnt) {
			dlon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
			cnt++;
			// draw a line
			uv = transformLL(ticendlat, dlon);
			uv = mapScaler(uv.u, uv.v);
			UVCoordinate uv2 = transformLL(mMapSpec.getLatMax() - mMapSpec.getLatGratSpacing(), dlon);
			uv2 = mapScaler(uv2.u, uv2.v);
			if (dlon != 0.0) {
				g.setStroke(lw1);
			}
			else {
				g.setStroke(lw2);
			}
			g.drawLine((int)uv.u, (int)uv.v, (int)uv2.u, (int)uv2.v);

			if (shade) {
				// draw a shaded region
				// get the next longitude
				double nlon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
				// corners of region are iLat,dlon iLat,nlon oLat,nlon oLat,dlon
				// loop on longitudes in range
				Polygon poly = new Polygon();
				for (double dl = dlon; dl <= nlon; dl += 0.5) {
					uv = transformLL(outerlat, dl);
					uv = mapScaler(uv.u, uv.v);
					poly.addPoint((int)uv.u, (int)uv.v);
				}
				uv = transformLL(iLat, nlon);
				uv = mapScaler(uv.u, uv.v);
				poly.addPoint((int)uv.u, (int)uv.v);
				for (double dl = nlon; dl >= dlon; dl += -0.5) {
					uv = transformLL(iLat, dl);
					uv = mapScaler(uv.u, uv.v);
					poly.addPoint((int)uv.u, (int)uv.v);
				}

				g.fillPolygon(poly);
				shade = false;
			}
			else {
				shade = true;
			}

			if (mMapSpec.isPlotGratLabels()) {
				// form the string to draw
				String lonLabel = formatLon(dlon);

				// get the x,y coordinates
				uv = transformLL(ticendlat, dlon);
				uv = mapScaler(uv.u, uv.v);

				// get the angle of rotation
				double angle = dlon - mMapSpec.getCenLon() + 180.0;

				double h = uv.u;
				double hoffset = -fm.stringWidth(lonLabel) / 2.0;

				// draw the string
				JOAFormulas.drawStyledString(lonLabel, h, uv.v, hoffset, -5, g, angle, JOAConstants.DEFAULT_MAP_VALUE_FONT,
				    JOAConstants.DEFAULT_MAP_VALUE_SIZE, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
				    JOAConstants.DEFAULT_MAP_VALUE_COLOR);
				g.setColor(mMapSpec.getGratColor());
			}
		}

		// -- FIND THE STARTING LAT. THEN DRAW THE PARALLELS FROM THE SOUTH POLE UP
		stlat = 0.0;
		oldU = -99;
		oldV = -99;
		for (double di = 1; di <= 90; di += 1.0) {
			stlat = stlat - latdeg;
			if (stlat < -89.5) {
				break;
			}
		}

		if (Math.abs(stlat + 90.) < 0.1) {
			lat = stlat - latdeg;
		}
		else {
			lat = stlat;
		}

		boolean first = true;
		dlon = 1.0;
		int numDegs = getLonRange(mMapSpec.getLonLft(), mMapSpec.getLonRt());

		double tstLon;
		while (lat <= 90.) {
			lon = mMapSpec.getLonLft() - 1;
			lat = lat + latdeg;
			aLine = new GeneralPath();
			for (int l = 0; l <= numDegs; l++) {
				lon = lon + dlon;

				if (lon > 180) {
					tstLon = -360 + lon;
				}
				else {
					tstLon = lon;
				}

				if (LLKeep(lat, tstLon)) {
					uv = transformLL(lat, tstLon);
					uv = mapScaler(uv.u, uv.v);
					if (oldU != -99 && !first && (Math.abs(oldU - (int)uv.u) < 500)) {
						aLine.moveTo((float)oldU, (float)oldV);
						aLine.lineTo((float)uv.u, (float)uv.v);
					}
					oldU = uv.u;
					oldV = uv.v;
					first = false;
				}
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
			}
			first = true;
			oldU = -99;
			oldV = -99;
		}

		// draw a border at lower latitude
		if (mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			// draw the border at min lat
			double[] lats = { mMapSpec.getLatMin(), outerlat };
			for (int ii = 0; ii < 2; ii++) {
				aLine = new GeneralPath();
				oldU = -99;
				oldV = -99;
				lat = lats[ii];
				for (double di = 1; di <= 360; di += (int)1.0) {
					lon = JOAFormulas.ANINT(lon + londeg);

					uv = transformLL(lat, di);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						if (oldU != -99 && (Math.abs(oldU - (int)uv.u) < 500)) {
							aLine.moveTo((float)oldU, (float)oldV);
							aLine.lineTo((float)uv.u, (float)uv.v);
						}
					}

					oldU = uv.u;
					oldV = uv.v;
				} // for;

				// draw the line
				try {
					aLine.closePath();
					g.setStroke(lw1);
					g.draw(aLine);
				}
				catch (Exception ex) {
				}
			}
		}

		if (!mMapSpec.isPlotGratLabels()) { return; }

		// label the grid along the center of the map
		// select a color for labels
		g.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);

		// label the latitude lines first
		for (double di = 1; di <= 90; di += 1.0) {
			stlat = stlat - latdeg;
			if (stlat < -89.5) {
				break;
			}
		}

		if (Math.abs(stlat + 90.) < 0.1) {
			lat = stlat - latdeg;
		}
		else {
			lat = stlat;
		}

		// find the closest meridian to the center longitude of the map
		lon = mMapSpec.getCenLon();
		double fact = Math.round(Math.abs(mMapSpec.getCenLon()) / londeg);
		if (lon < 0) {
			lon = -fact * londeg;
		}
		else {
			lon = fact * londeg;
		}

		if (lon > 180) {
			lon = -360 + lon;
		}

		while (lat <= 90.) {
			lat = lat + latdeg;
			if (lat <= mMapSpec.getLatMin()) {
				continue;
			}
			if (LLKeep(lat, lon)) {
				uv = transformLL(lat, lon);
				uv = mapScaler(uv.u, uv.v);
				String label = formatLat(lat);
				JOAFormulas.drawStyledString(label, (int)uv.u + 5, (int)uv.v + charHeight / 2, g, 0,
				    JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_SIZE,
				    JOAConstants.DEFAULT_MAP_VALUE_STYLE, JOAConstants.DEFAULT_MAP_VALUE_COLOR);
			}
		}
	}

	public void plotSouthPolarGrid(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;
		// plotGrid DRAWS THE GRID ON THE MAP.
		double lat, lon, dlon, latdeg, londeg, stlat, oldU, oldV;
		UVCoordinate uv = null;
		GeneralPath aLine = null;
		BasicStroke lw1 = new BasicStroke(1);
		BasicStroke lw2 = new BasicStroke(2);

		// select a font for labels
		g.setFont(new Font(JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
		    JOAConstants.DEFAULT_MAP_VALUE_SIZE));
		FontMetrics fm = g.getFontMetrics();
		int charHeight = JOAConstants.DEFAULT_MAP_VALUE_SIZE - 1;

		// set the graticule color
		g.setColor(mMapSpec.getGratColor());

		// compute the longitude for the outer ring
		// compute the longitude for meridians w/tic marks
		UVCoordinate maxLatBounds = null;
    try {
	    maxLatBounds = getBoundingLat(mMapSpec.getLatMax(), mMapSpec.getCenLon(), 0.01, 5.0, 5.0);
    }
    catch (Exception e) {
    }
    
    if (maxLatBounds == null) {
    	System.out.println("Error getting bounding Lat in plotSouthPolarGrid");
    	System.out.println("latMax = " + mMapSpec.getLatMax());
    	System.out.println("cenLon = " + mMapSpec.getCenLon());
    	System.out.println("other args = 0.01, 5.0, 5.0");
    	return;
    }
    
		double ticendlat = maxLatBounds.v;
		double outerlat = maxLatBounds.u;
		lon = mMapSpec.getCenLon();

		// set the graticule spacing
		if (mMapSpec.isAutoGraticule()) {
			double deltaLat = Math.abs(mMapSpec.getLatMax() - mMapSpec.getLatMin());
			double deltaLon = Math.abs(mMapSpec.getLonRt() - mMapSpec.getLonLft());
			if (deltaLat >= 100 || deltaLon >= 100) {
				londeg = 10.0;
				latdeg = 10.0;
			}
			else if (deltaLat >= 10 || deltaLon >= 10) {
				londeg = 5.0;
				latdeg = 5.0;
			}
			else {
				londeg = 1.0;
				latdeg = 1.0;
			}
			mMapSpec.setLonGratSpacing(londeg);
			mMapSpec.setLatGratSpacing(latdeg);
		}
		else {
			londeg = mMapSpec.getLonGratSpacing();
			latdeg = mMapSpec.getLatGratSpacing();
		}

		// start at the central meridian and draw lines
		dlon = mMapSpec.getLonLft();
		int cnt = 0;
		int maxCnt = (int)(360.0 / mMapSpec.getLonGratSpacing());
		boolean shade = true;
		double iLat = mMapSpec.getLatMax();
		while (cnt < maxCnt) {
			dlon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
			cnt++;
			// draw a line
			uv = transformLL(ticendlat, dlon);
			uv = mapScaler(uv.u, uv.v);
			UVCoordinate uv2 = transformLL(mMapSpec.getLatMin() + mMapSpec.getLatGratSpacing(), dlon);
			uv2 = mapScaler(uv2.u, uv2.v);
			if (dlon != 0.0) {
				g.setStroke(lw1);
			}
			else {
				g.setStroke(lw2);
			}
			g.drawLine((int)uv.u, (int)uv.v, (int)uv2.u, (int)uv2.v);

			if (shade) {
				// draw a shaded region
				// get the next longitude
				double nlon = mMapSpec.getLonLft() + (cnt * mMapSpec.getLonGratSpacing());
				// corners of region are iLat,dlon iLat,nlon oLat,nlon oLat,dlon
				// loop on longitudes in range
				Polygon poly = new Polygon();
				for (double dl = dlon; dl <= nlon; dl += 0.5) {
					uv = transformLL(outerlat, dl);
					uv = mapScaler(uv.u, uv.v);
					poly.addPoint((int)uv.u, (int)uv.v);
				}
				uv = transformLL(iLat, nlon);
				uv = mapScaler(uv.u, uv.v);
				poly.addPoint((int)uv.u, (int)uv.v);
				for (double dl = nlon; dl >= dlon; dl += -0.5) {
					uv = transformLL(iLat, dl);
					uv = mapScaler(uv.u, uv.v);
					poly.addPoint((int)uv.u, (int)uv.v);
				}

				g.fillPolygon(poly);
				shade = false;
			}
			else {
				shade = true;
			}

			if (mMapSpec.isPlotGratLabels()) {
				// form the string to draw
				String lonLabel = formatLon(dlon);

				// get the x,y coordinates
				uv = transformLL(ticendlat, dlon);
				uv = mapScaler(uv.u, uv.v);

				// get the angle of rotation
				double angle = 360 - dlon + mMapSpec.getCenLon();

				double h = uv.u;
				double hoffset = -fm.stringWidth(lonLabel) / 2.0;

				// draw the string
				JOAFormulas.drawStyledString(lonLabel, h, uv.v, hoffset, -5.0, g, angle, JOAConstants.DEFAULT_MAP_VALUE_FONT,
				    JOAConstants.DEFAULT_MAP_VALUE_SIZE, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
				    JOAConstants.DEFAULT_MAP_VALUE_COLOR);
				g.setColor(mMapSpec.getGratColor());
			}
		}

		// -- FIND THE STARTING LAT. THEN DRAW THE PARALLELS FROM THE SOUTH POLE UP
		stlat = 0.0;
		oldU = -99;
		oldV = -99;
		for (double di = 1; di <= 90; di += 1.0) {
			stlat = stlat - latdeg;
			if (stlat < -89.5) {
				break;
			}
		}

		if (Math.abs(stlat + 90.) < 0.1) {
			lat = stlat - latdeg;
		}
		else {
			lat = stlat;
		}

		boolean first = true;
		dlon = 1.0;
		int numDegs = this.getLonRange(mMapSpec.getLonLft(), mMapSpec.getLonRt());

		double tstLon;
		while (lat <= 90.) {
			lon = mMapSpec.getLonLft() - 1;
			lat = lat + latdeg;
			aLine = new GeneralPath();
			for (int l = 0; l <= numDegs; l++) {
				lon = lon + dlon;

				if (lon > 180) {
					tstLon = -360 + lon;
				}
				else {
					tstLon = lon;
				}

				if (LLKeep(lat, tstLon)) {
					uv = transformLL(lat, tstLon);
					uv = mapScaler(uv.u, uv.v);
					if (oldU != -99 && !first && (Math.abs(oldU - (int)uv.u) < 500)) {
						aLine.moveTo((float)oldU, (float)oldV);
						aLine.lineTo((float)uv.u, (float)uv.v);
					}
					oldU = uv.u;
					oldV = uv.v;
					first = false;
				}
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
			}
			first = true;
			oldU = -99;
			oldV = -99;
		}

		// draw a border at lower latitude
		// draw the border at min lat
		double[] lats = { mMapSpec.getLatMax(), outerlat };
		for (int ii = 0; ii < 2; ii++) {
			aLine = new GeneralPath();
			oldU = -99;
			oldV = -99;
			lat = lats[ii];
			for (double di = 1; di <= 360; di += (int)1.0) {
				lon = JOAFormulas.ANINT(lon + londeg);

				uv = transformLL(lat, di);
				uv = mapScaler(uv.u, uv.v);
				if (uv.mInWind) {
					if (oldU != -99 && (Math.abs(oldU - (int)uv.u) < 500)) {
						aLine.moveTo((float)oldU, (float)oldV);
						aLine.lineTo((float)uv.u, (float)uv.v);
					}
				}

				oldU = uv.u;
				oldV = uv.v;
			} // for;

			// draw the line
			try {
				aLine.closePath();
				g.setStroke(lw1);
				g.draw(aLine);
			}
			catch (Exception ex) {
			}
		}

		if (!mMapSpec.isPlotGratLabels()) { return; }

		// label the grid along the center of the map

		// select a color for labels
		g.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);

		// label the latitude lines first
		for (double di = 1; di <= 90; di += 1.0) {
			stlat = stlat - latdeg;
			if (stlat < -89.5) {
				break;
			}
		}

		if (Math.abs(stlat + 90.) < 0.1) {
			lat = stlat - latdeg;
		}
		else {
			lat = stlat;
		}

		// find the closest meridian to the center longitude of the map
		lon = mMapSpec.getCenLon();
		double fact = Math.round(Math.abs(mMapSpec.getCenLon()) / londeg);
		if (lon < 0) {
			lon = -fact * londeg;
		}
		else {
			lon = fact * londeg;
		}

		if (lon > 180) {
			lon = -360 + lon;
		}

		while (lat <= 90.) {
			lat = lat + latdeg;
			if (lat >= mMapSpec.getLatMax()) {
				continue;
			}

			if (LLKeep(lat, lon)) {
				uv = transformLL(lat, lon);
				uv = mapScaler(uv.u, uv.v);
				String label = formatLat(lat);
				JOAFormulas.drawStyledString(label, (int)uv.u + 5, (int)uv.v + charHeight / 2, g, 0,
				    JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_SIZE,
				    JOAConstants.DEFAULT_MAP_VALUE_STYLE, JOAConstants.DEFAULT_MAP_VALUE_COLOR);
			}
		}
	}

	public void plotGrid(Graphics gin) {
		Graphics2D g = (Graphics2D)gin;
		// plotGrid DRAWS THE GRID ON THE MAP.
		double lat, lon, dlat, dlon, latdeg, londeg, stlat, oldU, oldV;
		UVCoordinate uv = null;
		GeneralPath aLine;
		BasicStroke lw1 = new BasicStroke(1);
		BasicStroke lw2 = new BasicStroke(2);

		// select a font for labels
		g.setFont(new Font(JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_STYLE,
		    JOAConstants.DEFAULT_MAP_VALUE_SIZE));
		FontMetrics fm = g.getFontMetrics();
		int charHeight = JOAConstants.DEFAULT_MAP_VALUE_SIZE - 1;

		// set the graticule color
		g.setColor(mMapSpec.getGratColor());

		// set the graticule spacing
		if (mMapSpec.isAutoGraticule()) {
			double deltaLat = Math.abs(mMapSpec.getLatMax() - mMapSpec.getLatMin());
			double deltaLon = Math.abs(mMapSpec.getLonRt() - mMapSpec.getLonLft());
			if (deltaLat >= 100 || deltaLon >= 100) {
				londeg = 10.0;
				latdeg = 10.0;
			}
			else if (deltaLat >= 10 || deltaLon >= 10) {
				londeg = 5.0;
				latdeg = 5.0;
			}
			else {
				londeg = 1.0;
				latdeg = 1.0;
			}
			mMapSpec.setLonGratSpacing(londeg);
			mMapSpec.setLatGratSpacing(latdeg);
		}
		else {
			londeg = mMapSpec.getLonGratSpacing();
			latdeg = mMapSpec.getLatGratSpacing();
		}

		// FIRST DRAW THE MERIDIANS.
		lon = -londeg;
		for (double di = 1; di <= 360; di += (int)mMapSpec.getLonGratSpacing()) {
			lon = JOAFormulas.ANINT(lon + londeg);

			lat = -91.0;
			dlat = -1.;
			dlat = -dlat;
			oldU = -99;
			oldV = -99;
			aLine = new GeneralPath();
			while (lat <= 90.) {
				lat = JOAFormulas.ANINT(lat + dlat);
				if (LLKeep(lat, lon)) {
					uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						if (oldU != -99 && (Math.abs(oldU - (int)uv.u) < 500)) {
							aLine.moveTo((float)oldU, (float)oldV);
							aLine.lineTo((float)uv.u, (float)uv.v);
						}
					}

					oldU = uv.u;
					oldV = uv.v;
				} // if
			} // while

			// draw the line
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lon != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
			}
		} // for

		// -- FIND THE STARTING LAT. THEN DRAW THE PARALLELS FROM THE SOUTH POLE UP
		stlat = 0.0;
		oldU = -99;
		oldV = -99;
		for (double di = 1; di <= 90; di += 1.0) {
			stlat = stlat - latdeg;
			if (stlat < -89.5) {
				break;
			}
		}

		if (Math.abs(stlat + 90.) < 0.1) {
			lat = stlat - latdeg;
		}
		else {
			lat = stlat;
		}

		boolean first = true;
		dlon = 1.0;
		int numDegs = this.getLonRange(mMapSpec.getLonLft(), mMapSpec.getLonRt());

		double tstLon;
		while (lat <= 90.) {
			lon = mMapSpec.getLonLft() - 1;
			lat = lat + latdeg;
			aLine = new GeneralPath();
			for (int l = 0; l <= numDegs; l++) {
				lon = lon + dlon;

				if (lon > 180) {
					tstLon = -360 + lon;
				}
				else {
					tstLon = lon;
				}

				if (LLKeep(lat, tstLon)) {
					uv = transformLL(lat, tstLon);
					uv = mapScaler(uv.u, uv.v);
					if (oldU != -99 && !first && (Math.abs(oldU - (int)uv.u) < 500)) {
						aLine.moveTo((float)oldU, (float)oldV);
						aLine.lineTo((float)uv.u, (float)uv.v);
					}
					oldU = uv.u;
					oldV = uv.v;
					first = false;
				}
			}
			try {
				aLine.closePath();
				g.setColor(mMapSpec.getGratColor());
				if (lat != 0.0) {
					g.setStroke(lw1);
				}
				else {
					g.setStroke(lw2);
				}
				g.draw(aLine);
			}
			catch (Exception ex) {
			}
			first = true;
			oldU = -99;
			oldV = -99;
		}

		if (!mMapSpec.isPlotGratLabels()) { return; }

		// label the grid along the center of the map

		// select a color for labels
		g.setColor(JOAConstants.DEFAULT_MAP_VALUE_COLOR);

		// label the latitude lines first
		for (double di = 1; di <= 90; di += 1.0) {
			stlat = stlat - latdeg;
			if (stlat < -89.5) {
				break;
			}
		}

		if (Math.abs(stlat + 90.) < 0.1) {
			lat = stlat - latdeg;
		}
		else {
			lat = stlat;
		}

		// find the closest meridian to the center longitude of the map
		lon = mMapSpec.getCenLon();
		double fact = Math.round(Math.abs(mMapSpec.getCenLon()) / londeg);
		if (lon < 0) {
			lon = -fact * londeg;
		}
		else {
			lon = fact * londeg;
		}

		if (lon > 180) {
			lon = -360 + lon;
		}

		while (lat <= 90.) {
			lat = lat + latdeg;
			if (lat <= mMapSpec.getLatMin() || lat >= mMapSpec.getLatMax()) {
				continue;
			}
			if (LLKeep(lat, lon)) {
				uv = transformLL(lat, lon);
				uv = mapScaler(uv.u, uv.v);
				String label = formatLat(lat);
				JOAFormulas.drawStyledString(label, (int)uv.u + 5, (int)uv.v + charHeight / 2, g, 0,
				    JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_SIZE,
				    JOAConstants.DEFAULT_MAP_VALUE_STYLE, JOAConstants.DEFAULT_MAP_VALUE_COLOR);
			}
		}

		// plot the meridian labels
		double ctrLat = mMapSpec.getCenLat();
		if (mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			ctrLat = mMapSpec.getLatMin() + latdeg;
		}
		else if (mMapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION) {
			ctrLat = mMapSpec.getLatMax() - latdeg;
		}
		fact = Math.round(Math.abs(ctrLat) / latdeg);
		if (ctrLat < 0) {
			lat = -fact * latdeg;
		}
		else {
			lat = fact * latdeg;
		}

		lon = -londeg;
		double l;
		for (double di = 1; di <= 360; di += (int)mMapSpec.getLonGratSpacing()) {
			lon = JOAFormulas.ANINT(lon + londeg);
			l = lon;
			if (lon > 180) {
				l -= 360;
			}

			if (mMapSpec.getLonLft() < mMapSpec.getLonRt()) {
				if ((l <= mMapSpec.getLonLft()) || (l >= mMapSpec.getLonRt())) {
					continue;
				}
			}
			else {
				if ((l <= mMapSpec.getLonLft()) || (l >= mMapSpec.getLonRt())) {
					;
				}
				else {
					continue;
				}
			}

			if (LLKeep(lat, lon)) {
				uv = transformLL(lat, lon);
				uv = mapScaler(uv.u, uv.v);
				if (lon > 180) {
					lon -= 360;
				}
				String label = formatLon(lon);
				int w = fm.stringWidth(label);
				JOAFormulas.drawStyledString(label, (int)uv.u - w / 2, (int)uv.v + charHeight + 10, g, 0,
				    JOAConstants.DEFAULT_MAP_VALUE_FONT, JOAConstants.DEFAULT_MAP_VALUE_SIZE,
				    JOAConstants.DEFAULT_MAP_VALUE_STYLE, JOAConstants.DEFAULT_MAP_VALUE_COLOR);
			} // if
		} // for
	}


	public void obsChanged(ObsChangedEvent evt) {
		if (mWindowIsLocked || evt.getDirection() > JOAConstants.PREVSTN) { return; }
		// display the current station
		Station sh = evt.getFoundStation();
		Section sech = evt.getFoundSection();
		Bottle bh = evt.getFoundBottle();
		setRecord(sech, sh, bh);
	}

	public void levelChanged(LevelChangedEvent evt) {
		if (mWindowIsLocked) { return; }

		for (LevelChange level : evt.getLevel()) {
			// apply level change to either stn and/or contour layers
			if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
				// test for the quality of the surfaces
				if (mMapSpec.getStnColorByIsoSurface().equalGrids(level.getSurface().getValues(),
				    level.getSurface().getNumLevels())) {
					// can safely use the current level of the evt map panel
					mCurrIsoSurfaceLevel = level.getLevel();
					mMapSpec.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoSurface().getValue(mCurrIsoSurfaceLevel));

					// redraw the stations
					mLegend.resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(), IsoBrowsingMode.STN_SYMBOL);
				}
				else {
					// test first whether the grids are in the same parameter
					if (mMapSpec.getStnColorByIsoSurface().getParam().equalsIgnoreCase(level.getSurface().getParam())) {
						// get the value of the surface in the evt
						double inLevelVal = level.getLevelValue();

						// look for the closest matching value in this map's interpolation
						// surface
						double delta = 10000000;
						double[] targetVals = mMapSpec.getStnColorByIsoSurface().getValues();
						int numTargVals = mMapSpec.getStnColorByIsoSurface().getNumLevels();
						int foundLevel = 0;
						for (int b = 0; b < numTargVals; b++) {
							double d = Math.abs(targetVals[b] - inLevelVal);
							if (d < delta) {
								delta = d;
								foundLevel = b;
							}
						}
						mCurrIsoSurfaceLevel = foundLevel;
						mMapSpec.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoSurface().getValue(mCurrIsoSurfaceLevel));

						// redraw the stations
						mLegend.resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(), IsoBrowsingMode.STN_SYMBOL);
					}
					else {
						// use the current bottle to get an approximate value
						OpenDataFile odf = mFileViewer.getCurrDataFile();
						Section sec = odf.getCurrSection();
						Station stn = sec.getCurrStation();
						Bottle bot = stn.getCurrBottle();

						// get the value of the surface parameter at this bottle
						int surfPos = sec.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);
						double surfValAtBottle = bot.mDValues[surfPos];

						// look through the standard levels and find the closest level to to
						// this value

						double delta = 10000000;
						;
						double[] targetVals = mMapSpec.getStnColorByIsoSurface().getValues();
						int numTargVals = mMapSpec.getStnColorByIsoSurface().getNumLevels();
						int foundLevel = 0;
						for (int b = 0; b < numTargVals; b++) {
							double d = Math.abs(targetVals[b] - surfValAtBottle);
							if (d < delta) {
								delta = d;
								foundLevel = b;
							}
						}

						mCurrIsoSurfaceLevel = foundLevel;
						mMapSpec.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoSurface().getValue(mCurrIsoSurfaceLevel));

						// redraw the stations
						mLegend.resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(), IsoBrowsingMode.STN_SYMBOL);
					}
				}
			}

			// test for the quality of the surfaces
			if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
				if (mMapSpec.getIsoContourSurface().equalGrids(level.getSurface().getValues(),
				    level.getSurface().getNumLevels())) {
					// can safely use the current level of the evt map panel

					mCurrContourIsoSurfaceLevel = level.getLevel();
					mMapSpec.setContourIsoSurfaceValue(mMapSpec.getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel));

					// redraw the stations
					mLegend.resetLevel(mMapSpec.getContourIsoSurfaceValue(), IsoBrowsingMode.OVERLAY_CONTOUR);
				}
				else {
					// test first whether the grids are in the same parameter
					if (mMapSpec.getIsoContourSurface().getParam().equalsIgnoreCase(level.getSurface().getParam())) {
						// get the value of the surface in the evt
						double inLevelVal = level.getLevelValue();

						// look for the closest matching value in this map's interpolation
						// surface
						double delta = 10000000;
						double[] targetVals = mMapSpec.getIsoContourSurface().getValues();
						int numTargVals = mMapSpec.getIsoContourSurface().getNumLevels();
						int foundLevel = 0;
						for (int b = 0; b < numTargVals; b++) {
							double d = Math.abs(targetVals[b] - inLevelVal);
							if (d < delta) {
								delta = d;
								foundLevel = b;
							}
						}
						mCurrContourIsoSurfaceLevel = foundLevel;
						mMapSpec.setContourIsoSurfaceValue(mMapSpec.getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel));

						// redraw the stations
						mLegend.resetLevel(mMapSpec.getContourIsoSurfaceValue(), IsoBrowsingMode.OVERLAY_CONTOUR);
					}
					else {
						// use the current bottle to get an approximate value
						OpenDataFile odf = mFileViewer.getCurrDataFile();
						Section sec = odf.getCurrSection();
						Station stn = sec.getCurrStation();
						Bottle bot = stn.getCurrBottle();

						// get the value of the surface parameter at this bottle
						int surfPos = sec.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);
						double surfValAtBottle = bot.mDValues[surfPos];

						// look through the standard levels and find the closest level to to
						// this value

						double delta = 10000000;
						;
						double[] targetVals = mMapSpec.getIsoContourSurface().getValues();
						int numTargVals = mMapSpec.getIsoContourSurface().getNumLevels();
						int foundLevel = 0;
						for (int b = 0; b < numTargVals; b++) {
							double d = Math.abs(targetVals[b] - surfValAtBottle);
							if (d < delta) {
								delta = d;
								foundLevel = b;
							}
						}

						mCurrContourIsoSurfaceLevel = foundLevel;
						mMapSpec.setContourIsoSurfaceValue(mMapSpec.getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel));

						// redraw the stations
						mLegend.resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(), IsoBrowsingMode.OVERLAY_CONTOUR);
					}
				}
			}
		}

		mSelectionRect = null;
		paintComponent(this.getGraphics());
		mLegend.invalidate();
	}

	public void metadataChanged(MetadataChangedEvent evt) {
		if (mColorBarLegendPanel == null) { return; }
		mColorBarLegendPanel.invalidate();
		mColorBarLegendPanel.setSize(this.getSize().width + 1, this.getSize().height);
		mColorBarLegendPanel.setSize(this.getSize().width, this.getSize().height);
		mColorBarLegendPanel.invalidate();
		mColorBarLegendPanel.validate();
	}

	public void setRecord(Section inSec, Station inStn, Bottle inBot) {
		// set the data spot
		try {
			double lat = inStn.mLat;
			double lon = inStn.mLon;
			if (LLKeep(lat, lon)) {
				UVCoordinate uv = transformLL(lat, lon);
				uv = mapScaler(uv.u, uv.v);
				if (uv.mInWind) {
					mObsMarker.setNewPos((int)uv.u + 2, (int)uv.v + 2);
					paintImmediately(new Rectangle(0, 0, 2000, 2000));
				}
			}

			boolean updateMap = false;

			if (!mIgnore && !mMapSpec.isStnColorByIsoMinIsoSurfaceValue() && !mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()
			    && mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
				// get the bottle value for the surface parameter
				int sPos = inSec.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);
				double foundVal = inBot.mDValues[sPos];

				// find the closest matching surface level for the current bottle value
				double delta = 10000000;
				int oldlevel = mCurrIsoSurfaceLevel;
				for (int i = 0; i < mMapSpec.getStnColorByIsoSurface().getNumLevels(); i++) {
					double d = Math.abs(foundVal - mMapSpec.getStnColorByIsoSurface().getValue(i));
					if (d < delta) {
						delta = d;
						mCurrIsoSurfaceLevel = i;
						mMapSpec.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoSurface().getValue(i));
					}
				}
				updateMap = mCurrIsoSurfaceLevel != oldlevel;
			}

			if (!mIgnore && !mMapSpec.isIsoContourMinSurfaceValue() && !mMapSpec.isIsoContourMaxSurfaceValue()
			    && mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
				// get the bottle value for the surface parameter
				int sPos = inSec.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);
				double foundVal = inBot.mDValues[sPos];

				// find the closest matching surface level for the current bottle value
				double delta = 10000000;
				int oldlevel = mCurrContourIsoSurfaceLevel;
				for (int i = 0; i < mMapSpec.getIsoContourSurface().getNumLevels(); i++) {
					double d = Math.abs(foundVal - mMapSpec.getIsoContourSurface().getValue(i));
					if (d < delta) {
						delta = d;
						mCurrContourIsoSurfaceLevel = i;
						mMapSpec.setContourIsoSurfaceValue(mMapSpec.getIsoContourSurface().getValue(i));
					}
				}
				updateMap = mCurrContourIsoSurfaceLevel != oldlevel;
			}

			if (updateMap) {
				// redraw the stations
				paintComponent(this.getGraphics());
				if (mMapSpec.getStnColorMode() == mMapSpec.COLOR_STNS_BY_ISOSURFACE) {
					mLegend.resetLevel(IsoBrowsingMode.STN_SYMBOL);
				}
				if (mMapSpec.getContourOverlayMode() == mMapSpec.CONTOUR_OVERLAY_BY_ISOSURFACE) {
					mLegend.resetLevel(IsoBrowsingMode.OVERLAY_CONTOUR);
				}
				mLegend.invalidate();
			}
			mIgnore = false;
		}
		catch (Exception ex) {
		}
	}

	public void centerMap(int x, int y) {
		// convert to lat/lon
		UVCoordinate uv = invTransformLL(x, y);
		double newCtrLon = uv.u;
		double newCtrLat = uv.v;

		mMapSpec.setCenLat(newCtrLat);
		mMapSpec.setCenLon(newCtrLon);
		mFrame.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
		mFrame.setSize(mFrame.getSize().width - 1, mFrame.getSize().height);
		mFrame.validate();
		this.invalidate();
	}

	public void findByXY(int x, int y) {
		boolean found = false;
		OpenDataFile foundFile = null;
		Section foundSection = null;
		Station foundStation = null;
		Bottle foundBottle = null;
		OpenDataFile oldof = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section oldsech = (Section)oldof.mSections.currElement();
		Station oldsh = (Station)oldsech.mStations.currElement();
		Bottle oldBottle = (Bottle)oldsh.mBottles.currElement();

		// construct the search rectangle
		int searchRectLeft = x - 2;
		int searchRectRight = x + 2;
		int searchRectTop = y - 2;
		int searchRectBottom = y + 2;

		if (searchRectTop > searchRectBottom) {
			int temp = searchRectTop;
			searchRectTop = searchRectBottom;
			searchRectBottom = temp;
		}

		// convert to lat/lon
		UVCoordinate uv = invTransformLL(searchRectLeft, searchRectBottom);
		uv = invTransformLL(searchRectRight, searchRectTop);

		// search for a matching observation
		double minOffset = 10000.0;
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles && !found; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections && !found; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				for (int stc = 0; stc < sech.mStations.size() && !found; stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					double lat = sh.mLat;
					double lon = sh.mLon;
					uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					double off = Math.sqrt(((x - uv.u) * (x - uv.u)) + ((y - uv.v) * (y - uv.v)));

					if (off < minOffset) {
						foundStation = sh;
						foundSection = sech;
						foundFile = of;
						minOffset = off;
					}
				}
			}
		}

		// post event so other components will update
		found = true;
		if (found && foundStation != null) {
			// look for bottle at same depth
			found = false;
			foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
			if (foundBottle != null) {
				found = true;
			}

			if (found && foundBottle != oldBottle) {
				mFileViewer.mOpenFiles.setCurrElement(foundFile);
				foundFile.mSections.setCurrElement(foundSection);
				foundSection.mStations.setCurrElement(foundStation);
				foundStation.mBottles.setCurrElement(foundBottle);
				ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
				oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
				paintImmediately(new Rectangle(0, 0, 2000, 2000));
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
			}
			else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	public boolean findByArrowKey(Integer direction) {
		if (mWindowIsLocked) { return false; }
		OpenDataFile foundFile = null;
		Section foundSection = null;
		Station foundStation = null;
		Bottle foundBottle = null;
		int pPos = mFileViewer.getPRESPropertyPos();

		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		Bottle bh = (Bottle)sh.mBottles.currElement();

		// find new observation
		boolean found = false;
		switch (direction.intValue()) {
			case 1: // JOAConstants.NEXTSTN:
				// go to next station
				foundStation = (Station)sech.mStations.nextElement();
				if (foundStation == null) {
					// go to next section
					foundSection = (Section)of.mSections.nextElement();
					foundFile = of;
					if (foundSection != null) {
						foundSection.mStations.setCurrElementToFirst();
						foundStation = (Station)foundSection.mStations.currElement();
						foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
						found = true;
						if (foundBottle != null) {
							found = true;
						}
					}
					else {
						// look in next file
						foundFile = (OpenDataFile)mFileViewer.mOpenFiles.nextElement();

						if (foundFile != null) {
							foundSection = (Section)foundFile.mSections.currElement();
							foundStation = (Station)foundSection.mStations.currElement();
							foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
							found = true;
							if (foundBottle != null) {
								found = true;
							}
						}
					}
				}
				else {
					foundSection = sech;
					foundFile = of;

					// search for bottle by pressure
					foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
					found = true;
					if (foundBottle != null) {
						found = true;
					}
				}
				break;
			case 2: // JOAConstants.PREVSTN:
				// go to prev station
				foundStation = (Station)sech.mStations.prevElement();
				if (foundStation == null) {
					// go to next section
					foundSection = (Section)of.mSections.prevElement();
					foundFile = of;

					if (foundSection != null) {
						foundSection.mStations.setCurrElementToLast();
						foundStation = (Station)foundSection.mStations.currElement();
						foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
						found = true;
						if (foundBottle != null) {
							found = true;
						}
					}
					else {
						// look in next file
						foundFile = (OpenDataFile)mFileViewer.mOpenFiles.prevElement();

						if (foundFile != null) {
							foundSection = (Section)foundFile.mSections.currElement();
							foundStation = (Station)foundSection.mStations.currElement();
							foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
							found = true;
							if (foundBottle != null) {
								found = true;
							}
						}
					}
				}
				else {
					foundSection = sech;
					foundFile = of;

					// search for bottle by pressure
					foundBottle = JOAFormulas.findBottleByPres(mFileViewer, foundStation);
					found = true;
					if (foundBottle != null) {
						found = true;
					}
				}
				break;
			case 3: // JOAConstants.NEXTOBS:
				// search for a matching bottle
				foundFile = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
				foundSection = (Section)of.mSections.currElement();
				foundStation = (Station)sech.mStations.currElement();
				LevelChangedEvent lce = null;

				if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE
				    || mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {

					if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
						if (!mMapSpec.isStnColorByIsoMinIsoSurfaceValue() && !mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()
						    && mCurrIsoSurfaceLevel + 1 < mMapSpec.getStnColorByIsoSurface().getNumLevels()) {

							// go to the next level down
							mCurrIsoSurfaceLevel++;
							mMapSpec.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoSurface()
							    .getValue(mCurrIsoSurfaceLevel));

							// redraw the stations
							// paintComponent(this.getGraphics());
							// mLegend.resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(),
							// IsoBrowsingMode.STN_SYMBOL);
							// mLegend.invalidate();

							// at the current station find the best bottle match in the
							// surface parameter
							double delta = 10000000;
							int sPos = foundSection.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);
							for (int b = 0; b < foundStation.mNumBottles; b++) {
								bh = (Bottle)foundStation.mBottles.elementAt(b);
								double val = bh.mDValues[sPos];
								double d = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - val);
								if (d < delta) {
									delta = d;
									foundBottle = bh;
									found = true;
									JOAConstants.currTestPres = foundBottle.mDValues[pPos];
								}
							}
							mIgnore = true;

							// post a level-changed event
							if (lce == null) {
								lce = new LevelChangedEvent(mFileViewer);
							}

							lce.addLevel(new LevelChange(IsoBrowsingMode.STN_SYMBOL, mCurrIsoSurfaceLevel, mMapSpec
							    .getStnColorByIsoSurface().getValue(mCurrIsoSurfaceLevel), -99.0, mMapSpec.getStnColorByIsoSurface(),
							    this));
						}
					}

					if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
						if (!mMapSpec.isIsoContourMinSurfaceValue() && !mMapSpec.isIsoContourMaxSurfaceValue()
						    && mCurrContourIsoSurfaceLevel + 1 < mMapSpec.getIsoContourSurface().getNumLevels()) {
							mCurrContourIsoSurfaceLevel++;
							mMapSpec.setContourIsoSurfaceValue(mMapSpec.getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel));

							// redraw the stations
							// paintComponent(this.getGraphics());
							// mLegend.resetLevel(mMapSpec.getContourIsoSurfaceValue(),
							// IsoBrowsingMode.OVERLAY_CONTOUR);
							// mLegend.invalidate();

							// at the current station find the best bottle match in the
							// surface parameter
							double delta = 10000000;
							int sPos = foundSection.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);
							for (int b = 0; b < foundStation.mNumBottles; b++) {
								bh = (Bottle)foundStation.mBottles.elementAt(b);
								double val = bh.mDValues[sPos];
								double d = Math.abs(mMapSpec.getContourIsoSurfaceValue() - val);
								if (d < delta) {
									delta = d;
									foundBottle = bh;
									found = true;
									JOAConstants.currTestPres = foundBottle.mDValues[pPos];
								}
							}
							mIgnore = true;

							// post a level-changed event
							if (lce == null) {
								lce = new LevelChangedEvent(mFileViewer);
							}

							lce.addLevel(new LevelChange(IsoBrowsingMode.OVERLAY_CONTOUR, mCurrContourIsoSurfaceLevel, mMapSpec
							    .getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel), -99.0,
							    mMapSpec.getIsoContourSurface(), this));
						}
					}

					if (lce != null) {
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(lce);
					}
				}
				else {
					// go to next bottle
					foundBottle = (Bottle)sh.mBottles.nextElement();

					if (foundBottle != null) {
						foundStation = sh;
						foundSection = sech;
						foundFile = of;
						// JOAConstants.currTestPres = foundBottle.mValues[pPos] *
						// mFileViewer.mAllProperties[pPos].mActScale +
						// mFileViewer.mAllProperties[pPos].mActOrigin;
						JOAConstants.currTestPres = foundBottle.mDValues[pPos];
						found = true;
					}
				}
				break;
			case 4: // JOAConstants.PREVOBS:
				// search for a matching bottle
				foundFile = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
				foundSection = (Section)of.mSections.currElement();
				foundStation = (Station)sech.mStations.currElement();
				LevelChangedEvent lce2 = null;

				if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE
				    || mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {

					if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
						if (!mMapSpec.isStnColorByIsoMinIsoSurfaceValue() && !mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()
						    && mCurrIsoSurfaceLevel - 1 >= 0) {

							// go to the next level down
							mCurrIsoSurfaceLevel--;
							mMapSpec.setStnColorByIsoIsoSurfaceValue(mMapSpec.getStnColorByIsoSurface()
							    .getValue(mCurrIsoSurfaceLevel));

							// redraw the stations
							// paintComponent(this.getGraphics());
							// mLegend.resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(),
							// IsoBrowsingMode.STN_SYMBOL);
							// mLegend.invalidate();

							// at the current station find the best match in the surface
							// parameter
							double delta = 10000000;
							int sPos = foundSection.getVarPos(mMapSpec.getStnColorByIsoSurface().getParam(), false);
							for (int b = 0; b < foundStation.mNumBottles; b++) {
								bh = (Bottle)foundStation.mBottles.elementAt(b);
								double val = bh.mDValues[sPos];
								double d = Math.abs(mMapSpec.getStnColorByIsoIsoSurfaceValue() - val);
								if (d < delta) {
									delta = d;
									foundBottle = bh;
									found = true;
									JOAConstants.currTestPres = foundBottle.mDValues[pPos];
								}
							}
							mIgnore = true;

							// post a level changed event
							if (lce2 == null) {
								lce2 = new LevelChangedEvent(mFileViewer);
							}

							lce2.addLevel(new LevelChange(IsoBrowsingMode.STN_SYMBOL, mCurrIsoSurfaceLevel, mMapSpec
							    .getStnColorByIsoSurface().getValue(mCurrIsoSurfaceLevel), -99.0, mMapSpec.getStnColorByIsoSurface(),
							    this));
						}
					}

					if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE) {
						if (!mMapSpec.isIsoContourMinSurfaceValue() && !mMapSpec.isIsoContourMaxSurfaceValue()
						    && mCurrContourIsoSurfaceLevel - 1 >= 0) {

							// go to the next level down
							mCurrContourIsoSurfaceLevel--;
							mMapSpec.setContourIsoSurfaceValue(mMapSpec.getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel));

							// redraw the stations
							// paintComponent(this.getGraphics());
							// mLegend.resetLevel(mMapSpec.getContourIsoSurfaceValue(),
							// IsoBrowsingMode.OVERLAY_CONTOUR);
							// mLegend.invalidate();

							// at the current station find the best match in the surface
							// parameter
							double delta = 10000000;
							int sPos = foundSection.getVarPos(mMapSpec.getIsoContourSurface().getParam(), false);
							for (int b = 0; b < foundStation.mNumBottles; b++) {
								bh = (Bottle)foundStation.mBottles.elementAt(b);
								double val = bh.mDValues[sPos];
								double d = Math.abs(mMapSpec.getContourIsoSurfaceValue() - val);
								if (d < delta) {
									delta = d;
									foundBottle = bh;
									found = true;
									JOAConstants.currTestPres = foundBottle.mDValues[pPos];
								}
							}
							mIgnore = true;

							// post a level changed event
							if (lce2 == null) {
								lce2 = new LevelChangedEvent(mFileViewer);
							}

							lce2.addLevel(new LevelChange(IsoBrowsingMode.OVERLAY_CONTOUR, mCurrContourIsoSurfaceLevel, mMapSpec
							    .getIsoContourSurface().getValue(mCurrContourIsoSurfaceLevel), -99.0,
							    mMapSpec.getIsoContourSurface(), this));
						}
					}

					if (lce2 != null) {
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(lce2);
					}
				}
				else {
					// go to previous bottle
					foundBottle = (Bottle)sh.mBottles.prevElement();

					if (foundBottle != null) {
						foundStation = sh;
						foundSection = sech;
						foundFile = of;
						// JOAConstants.JOAConstants.currTestPres =
						// foundBottle.mValues[pPos] *
						// mFileViewer.mAllProperties[pPos].mActScale +
						// mFileViewer.mAllProperties[pPos].mActOrigin;
						JOAConstants.currTestPres = foundBottle.mDValues[pPos];
						found = true;
					}
				}
				break;
		}

		// post event so other components will update
		if (found) {
			mFileViewer.mOpenFiles.setCurrElement(foundFile);
			foundFile.mSections.setCurrElement(foundSection);
			foundSection.mStations.setCurrElement(foundStation);
			foundStation.mBottles.setCurrElement(foundBottle);
			ObsChangedEvent oce = new ObsChangedEvent(mFileViewer);
			oce.setFoundObs(direction.intValue(), foundFile, foundSection, foundStation, foundBottle);
			// paintImmediately(new Rectangle(0, 0, 2000, 2000));
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
			return true;
		}
		else {
			Toolkit.getDefaultToolkit().beep();
			return false;
		}
	}

	public synchronized void checkProjection(int winWidth, int winHeight) {
		double ratio;
		double minScaledU = 999999;
		double minScaledV = 999999;

		// CALCULATE CONSTANTS FOR A MAP.
		if (mMapSpec.getProjection() >= JOAConstants.MILLERPROJECTION && mMapSpec.getProjection() < JOAConstants.ECKERT4PROJECTION) {
			mMapSpec.setH1(Math.sin(mMapSpec.getCenLat() * JOAConstants.F));
			mMapSpec.setH2(Math.cos(mMapSpec.getCenLat() * JOAConstants.F));
			mMapSpec.setH3(mMapSpec.getCenLon() + 90.0);
			if (mMapSpec.getH3() > 180.0) {
				mMapSpec.setH3(mMapSpec.getH3() - 360.0);
			}
			else if (mMapSpec.getH3() < -180.0) {
				mMapSpec.setH3(mMapSpec.getH3() + 360.0);
			}
			mMapSpec.setH3(mMapSpec.getH3() * JOAConstants.F);
		}
		else if (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
			String[] args = {"+proj=eck4", "+lon_0=90w"};
			mEckert4Projection = ProjectionFactory.fromPROJ4Specification( args );
		}

		UVCoordinate uv = null;
		mMapSpec.setUMax(0.0);
		mMapSpec.setUMin(0.0);
		mMapSpec.setVMax(0.0);
		mMapSpec.setVMin(0.0);
		if (mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION ||
				mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
			// find the extremes
			setMapExtremes();
		}
		else {
			for (double lt = mMapSpec.getLatMin(); lt <= mMapSpec.getLatMax(); lt += 1.0) {
				for (double ln = mMapSpec.getLonLft(); ln <= mMapSpec.getLonRt(); ln += 1.0) {
					uv = transformLL(lt, ln);
					mMapSpec.setUMax(Math.max(uv.u, mMapSpec.getUMax()));
					mMapSpec.setUMin(Math.min(uv.u, mMapSpec.getUMin()));
					mMapSpec.setVMax(Math.max(uv.v, mMapSpec.getVMax()));
					mMapSpec.setVMin(Math.min(uv.v, mMapSpec.getVMin()));
				}
			}
		}

		ratio = (mMapSpec.getUMax() - mMapSpec.getUMin()) / (mMapSpec.getVMax() - mMapSpec.getVMin());
		double mapWidth, mapHeight;
		mMapSpec.setWinHeight(winHeight);
		if ((double)winWidth / (double)winHeight >= ratio) {
			// CONSTRAINED BY HEIGHT
			mapWidth = (double)winHeight * ratio;
			mapHeight = winHeight;

			if (!mMapSpec.isRetainProjAspect()) {
				mMapSpec.setMapHScale((double)winWidth / mapWidth);
				mMapSpec.setMapVScale((double)winHeight / mapHeight);
			}
			else {
				mMapSpec.setMapHScale(mapWidth / (double)winWidth);
				mMapSpec.setMapVScale(mapHeight / (double)winHeight);
				mMapSpec.setMapHScale(1.0);
			}
			mMapSpec.setHScale(mapHeight / (mMapSpec.getVMax() - mMapSpec.getVMin()));
			mMapSpec.setVScale(mMapSpec.getHScale());
		}
		else {
			// -- CONSTRAINED BY WIDTH
			mapHeight = (double)winWidth / ratio;
			mapWidth = winWidth;

			if (!mMapSpec.isRetainProjAspect()) {
				mMapSpec.setMapHScale((double)winWidth / mapWidth);
				mMapSpec.setMapVScale(1.0); // (double)winHeight/mapHeight;
			}
			else {
				mMapSpec.setMapHScale(mapWidth / (double)winWidth);
				mMapSpec.setMapVScale(mapHeight / (double)winHeight);
				// mMapSpec.getMapVScale() = 1.0;
			}
			mMapSpec.setVScale((double)winHeight / (mMapSpec.getVMax() - mMapSpec.getVMin()));
			mMapSpec.setHScale(mapHeight / (mMapSpec.getVMax() - mMapSpec.getVMin()));
		}

		// compute the map offsets
		if (!mMapSpec.isRetainProjAspect()) {
			minScaledU = mMapSpec.getUMin();
			minScaledV = mMapSpec.getVMin();
			uv = mapScaler(minScaledU, minScaledV);
			mMapSpec.setHOffset(uv.u - 0);
			mMapSpec.setVOffset(uv.v - winHeight);
		}
		else {
			mMapSpec.setHOffset(0.0);
			mMapSpec.setVOffset(0.0);
		}
		mMapSpec.setHOffset(mHOffset); // minus shifts to right
		mMapSpec.setVOffset(mVOffset); // minus shifts down
		mMapSpec.setMapHScale(mHScale * mMapSpec.getMapHScale()); // < 1 makes map
		// smaller
		mMapSpec.setMapVScale(mVScale * mMapSpec.getMapVScale());
	}

	public UVCoordinate getTransformedExtremes() {
		double xMin = 999999.0, xMax = -999999.0, yMin = 9999999.0, yMax = -999999.0;
		UVCoordinate uv = transformLL(mMapSpec.getLatMax(), mMapSpec.getLonLft());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getLatMax(), mMapSpec.getLonRt());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getLatMin(), mMapSpec.getLonRt());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getLatMin(), mMapSpec.getLonLft());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getLatMin(), mMapSpec.getCenLon());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getLatMax(), mMapSpec.getCenLon());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getCenLat(), mMapSpec.getLonLft());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);
		uv = transformLL(mMapSpec.getCenLat(), mMapSpec.getLonRt());
		uv = mapScaler(uv.u, uv.v);
		xMin = Math.min(xMin, uv.u);
		xMax = Math.max(xMax, uv.u);
		yMin = Math.min(yMin, uv.v);
		yMax = Math.max(yMax, uv.v);

		if (mMapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION
		    || mMapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			uv = transformLL(mMapSpec.getLatMin(), 0.0);
			uv = mapScaler(uv.u, uv.v);
			xMin = Math.min(xMin, uv.u);
			xMax = Math.max(xMax, uv.u);
			uv = transformLL(mMapSpec.getLatMin(), 90.0);
			uv = mapScaler(uv.u, uv.v);
			xMin = Math.min(xMin, uv.u);
			xMax = Math.max(xMax, uv.u);
			uv = transformLL(mMapSpec.getLatMin(), 180.0);
			uv = mapScaler(uv.u, uv.v);
			xMin = Math.min(xMin, uv.u);
			xMax = Math.max(xMax, uv.u);
			uv = transformLL(mMapSpec.getLatMin(), -180.0);
			uv = mapScaler(uv.u, uv.v);
			xMin = Math.min(xMin, uv.u);
			xMax = Math.max(xMax, uv.u);
		}
		if (xMax - xMin < 1) { return new UVCoordinate(xMax, yMax - yMin); }

		return new UVCoordinate(xMax - xMin, yMax - yMin);
	}

	public void setMapExtremes() {
		mMapSpec.setUMax(-32000.0);
		mMapSpec.setUMin(32000.0);
		mMapSpec.setVMax(-32000.0);
		mMapSpec.setVMin(32000.0);
		// double lat, lon, dlat, dlon, latdeg, londeg, u, v, stlat;
		// boolean done;
		UVCoordinate uv = null;

		for (double llon = 0; llon <= 360; llon += 0.5) {
			for (double llat = -90.1; llat <= 90.; llat += 0.5) {
				if (LLKeep(llat, llon)) {
					uv = transformLL(llat, llon);
					mMapSpec.setUMax(Math.max(uv.u, mMapSpec.getUMax()));
					mMapSpec.setUMin(Math.min(uv.u, mMapSpec.getUMin()));
					mMapSpec.setVMax(Math.max(uv.v, mMapSpec.getVMax()));
					mMapSpec.setVMin(Math.min(uv.v, mMapSpec.getVMin()));
				}
			} // while
		} // for
	}

	public synchronized UVCoordinate transformLL(double lt, double ln) {
		UVCoordinate uv = null;
		if (mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION) {
			uv = transformLLMercator(lt, ln);
		}
		else if (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
			uv = transformLLEckert4(lt, ln);
		}
		else if (mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION) {
			uv = transformLLMiller(lt, ln);
		}
		else if (mMapSpec.getProjection() == JOAConstants.ORTHOGRAPHICPROJECTION) {
			uv = transformLLOrthographic(lt, ln);
		}
		else if (mMapSpec.getProjection() == JOAConstants.MOLLWEIDEPROJECTION) {
			uv = transformLLMollweide(lt, ln);
		}
		else if (mMapSpec.getProjection() == JOAConstants.LAMBERTEAPROJECTION) {
			uv = transformLLLambertAEA(lt, ln);
		}
		else if (mMapSpec.getProjection() >= JOAConstants.STEREOPROJECTION
		    && mMapSpec.getProjection() <= JOAConstants.SOUTHPOLEPROJECTION) {
			uv = transformLLPS(lt, ln);
		}
		else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
			;// uv = transformRobinson(lt, ln); to do
		}
		return uv;
	}

	public UVCoordinate transformLLGCS(double lt, double ln) {
		double m = getMagnitude(lt);
		double a = getAngle(ln);
		double x = getX(a, m) + 180;
		double y = 500 - (getY(a, m) + 180);
		return new UVCoordinate(x, y);
	}

	// THESE ROUTINEs DO THE CONVERSIONS FROM LATITUDE-LONGITUDE COORDINATES
	// TO CARTESIAN u-v COORDINATES.

	public UVCoordinate transformLLMollweide(double lt, double ln) {
		double u = 0, v = 0;

		double delta_lon = ln - mMapSpec.getCenLon();
		delta_lon = delta_lon * JOAConstants.F;
		delta_lon = adjust_lon(delta_lon);
		double theta = lt * JOAConstants.F;
		double con = Math.PI * Math.sin(lt * JOAConstants.F);

		for (int i = 0;; i++) {
			double delta_theta = -(theta + Math.sin(theta) - con) / (1.0 + Math.cos(theta));
			theta += delta_theta;
			if (Math.abs(delta_theta) < JOAConstants.EPSLN) {
				break;
			}
			if (i >= 50) { return new UVCoordinate(u, v); }
		}
		theta /= 2.0;

		if (Math.PI / 2 - Math.abs(JOAConstants.F * lt) < JOAConstants.EPSLN) {
			delta_lon = 0;
		}
		u = 0.900316316158 * JOAConstants.RE_M * delta_lon * Math.cos(theta);
		v = 1.4142135623731 * JOAConstants.RE_M * Math.sin(theta);
		return new UVCoordinate(u, v);
	}

	public UVCoordinate transformLLOrthographic(double lt, double ln) {
		double dlon, u = 0, v = 0;
		dlon = ln - mMapSpec.getCenLon();
		dlon = dlon * JOAConstants.F;
//		dlon = adjust_lon(dlon);

		double sinphi = Math.sin(lt * JOAConstants.F);
		double cosphi = Math.cos(lt * JOAConstants.F);

		double coslon = Math.cos(dlon);
		double g = mMapSpec.getH1() * sinphi + mMapSpec.getH2() * cosphi * coslon;
		double ksp = 1.0;
		if ((g > 0) || (Math.abs(g) <= JOAConstants.EPSLN)) {
			u = r_major * ksp * cosphi * Math.sin(dlon);
			v = r_major * ksp * (mMapSpec.getH2() * sinphi - mMapSpec.getH1() * cosphi * coslon);
		}
		else {
			return new UVCoordinate(u, v);
		}
		return new UVCoordinate(u, v);
	}

	public UVCoordinate transformLLMiller(double lt, double ln) {
		double lat, lnmln, u = 0, v = 0;

		lnmln = ln - mMapSpec.getCenLon();
		lnmln = lnmln * JOAConstants.F;
		lnmln = adjust_lon(lnmln);
		lat = lt * JOAConstants.F;
		u = lnmln;
		v = Math.log(Math.tan(Math.PI / 4. + 0.4 * lat)) / 0.8;
		return new UVCoordinate(u, v);
	}

	public UVCoordinate transformLLMercator(double lt, double ln) {
		double lat, lnmln, u = 0, v = 0;
		if (Math.abs(lt) == 90.0) {
			if (lt > 0) {
				lt -= 0.1;
			}
			else if (lt < 0) {
				lt += 0.1;
			}

		}
		lnmln = ln - mMapSpec.getCenLon();
		lnmln = lnmln * JOAConstants.F;
		lnmln = adjust_lon(lnmln);
		lat = lt * JOAConstants.F;
		u = lnmln;
		v = Math.log(Math.tan(Math.PI / 4. + lat / 2.0));
		return new UVCoordinate(u, v);
	}

	private final static double C_x = .42223820031577120149;
	private final static double C_y = 1.32650042817700232218;
	private final static double RC_y = .75386330736002178205;
	private final static double C_p = 3.57079632679489661922;
	private final static double RC_p = .28004957675577868795;
	private final static double EPS = 1e-7;
	private final int NITER = 6;
	
	public UVCoordinate transformLLEckert4(double lt, double ln) {
		Point2D.Double out = new Point2D.Double();
		Point2D outXY = mEckert4Projection.project(ln, lt, out);
		return new UVCoordinate(outXY.getX(), outXY.getY());
	}

	public synchronized UVCoordinate transformLLPS(double lt, double ln) {
		double lat, lnmln, k, coslt, sinlt, coslnm, u = 0, v = 0;

		lnmln = ln - mMapSpec.getCenLon();
		lnmln = lnmln * JOAConstants.F;
		lat = lt * JOAConstants.F;
		sinlt = Math.sin(lat);
		coslt = Math.cos(lat);
		coslnm = Math.cos(lnmln);
		if (Math.abs(mMapSpec.getCenLat()) == 90.) {
			if (lt == -mMapSpec.getCenLat()) {
				// u = JOAConstants.OFFMAP;
				// v = JOAConstants.OFFMAP;
			}
		}
		else if (lt == -mMapSpec.getCenLat()) {
			if (ln == mMapSpec.getCenLon() + 180. || ln == mMapSpec.getCenLon() - 180.) {
				// u = JOAConstants.OFFMAP;
				// v = JOAConstants.OFFMAP;
			}
		}
		k = 2. / (1. + mMapSpec.getH1() * sinlt + mMapSpec.getH2() * coslt * coslnm);
		u = k * coslt * Math.sin(lnmln);
		v = k * (mMapSpec.getH2() * sinlt - mMapSpec.getH1() * coslt * coslnm);
		return new UVCoordinate(u, v);
	}

	public synchronized UVCoordinate transformLLLambertAEA(double lt, double ln) {
		double dlon, u = 0, v = 0;

		dlon = ln - mMapSpec.getCenLon();
		dlon = dlon * JOAConstants.F;
		dlon = adjust_lon(dlon);

		double sin_lat = Math.sin(lt * JOAConstants.F);
		double cos_lat = Math.cos(lt * JOAConstants.F);
		double sin_delta_lon = Math.sin(dlon);
		double cos_delta_lon = Math.cos(dlon);

		double g = mMapSpec.getH1() * sin_lat + mMapSpec.getH2() * cos_lat * cos_delta_lon;
		if (g == -1.0) { return new UVCoordinate(0, 0); }
		double ksp = JOAConstants.RE_M * Math.sqrt(2.0 / (1.0 + g));
		u = ksp * cos_lat * sin_delta_lon;
		v = ksp * (mMapSpec.getH2() * sin_lat - mMapSpec.getH1() * cos_lat * cos_delta_lon);
		return new UVCoordinate(u, v);
	}

	public UVCoordinate mapScaler(double u, double v) {
		// Scaler CHECKS TO SEE IF THE U-V POINT IS IN THE PLOT WINDOW THEN
		// CONVERTS THE U-V POINT TO Pixels.
		double uu, vv;

		// rotate test
		/*
		 * double phi = 180.0 * JOAConstants.F; double cosphi = Math.cos(phi);
		 * double sinphi = Math.sin(phi); double uc = mMapSpec.getUMin() +
		 * (mMapSpec.mUMax - mMapSpec.getUMin())/2.0; double vc = mMapSpec.getVMin()
		 * + (mMapSpec.getVMax() - mMapSpec.getVMin())/2.0; double ru = (u * cosphi)
		 * + (v * -sinphi) + ((-uc * cosphi) + (vc * sinphi) + uc); double rv = (u *
		 * sinphi) + (v * cosphi) + ((-uc * sinphi) + (vc * cosphi) + vc);
		 */

		uu = (u - mMapSpec.getUMin()) * mMapSpec.getHScale();
		vv = (v - mMapSpec.getVMin()) * mMapSpec.getVScale();
		vv = (((mMapSpec.getWinHeight() - vv) * mMapSpec.getMapVScale()) - mMapSpec.getVOffset());
		uu = ((uu * mMapSpec.getMapHScale()) - mMapSpec.getHOffset());
		return new UVCoordinate(uu, vv);
	}

	public boolean LLKeep(double lat, double lon) {
		if ((lat < mMapSpec.getLatMin()) || (lat > mMapSpec.getLatMax())) { return false; }
		if (lon > 180) {
			lon = lon - 360;
		}
		else if (lon < -180) {
			lon = lon + 360;
		}
		if (mMapSpec.getLonLft() < mMapSpec.getLonRt()) {
			if ((lon < mMapSpec.getLonLft()) || (lon > mMapSpec.getLonRt())) { return false; }
		}
		else {
			if ((lon >= mMapSpec.getLonLft()) || (lon <= mMapSpec.getLonRt())) {
				;
			}
			else {
				return false;
			}
		}
		return true;
	}

	// OK Button
	public void dialogDismissed(JDialog f)  {
		f.setVisible(false);
		mMapSpec = ((ConfigMapPlot)f).getMapSpec();
		try {
			mMapSpec.writeToLog("Edited existing plot: " + mFrame.getTitle());
		}
		catch (Exception ex) {
		}

		try {
			// redo legend
			int currWidth = mLegend.getWidth();
			mLegend.reset(mMapSpec);
			mLegend.setWidth(currWidth);
			Dimension d = mLegend.getPreferredSize();
			mLegendHeight = d.height;

			NewColorBar newStnCB = null;
			String newStnCBID = null;
			if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE ||
					mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR || 
					mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
				newStnCB = mMapSpec.getStnColorColorBar();
				newStnCBID = mMapSpec.getStnColorColorBar().getTitle();
			}
			
			NewColorBar newOVLCB = null;
			String newOVLCBID = null;
			if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE ||
					mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
				newOVLCB = mMapSpec.getOverlayContoursColorBar();
				newOVLCBID = newOVLCB.getTitle();
			}
			
			int stnCBState = ((ConfigMapPlot)f).getStnCBState();
			int ovlCBState = ((ConfigMapPlot)f).getOvlCBState();
			
			int sizeDelta = 0;
			
			//test new state of stn color bar		
			if (stnCBState == ConfigMapPlot.STN_CB_ADDED) {
				mContents.addColorBar(newStnCBID, newStnCB, false);
				sizeDelta += 100;
			}
			else if (stnCBState == ConfigMapPlot.STN_CB_CHANGED) {
				mContents.replaceColorBar(newStnCBID, newStnCB);
			}
			else if (stnCBState == ConfigMapPlot.STN_CB_REMOVED) {
				mContents.removeColorBar(newStnCBID);
				sizeDelta -= 100;
			}
			
			//test new state of overlay color bar		
			if (ovlCBState == ConfigMapPlot.OVL_CB_ADDED) {
				mContents.addColorBar(newOVLCBID, newOVLCB, false);
				sizeDelta += 100;
			}
			else if (ovlCBState == ConfigMapPlot.OVL_CB_CHANGED) {
				mContents.replaceColorBar(newOVLCBID, newOVLCB);
			}
			else if (ovlCBState == ConfigMapPlot.OVL_CB_REMOVED) {
				mContents.removeColorBar(newOVLCBID);
				sizeDelta -= 100;
			}
			mFrame.setSize(mFrame.getSize().width + sizeDelta, mFrame.getSize().height);

			
			f.dispose();

			this.setBackground(mMapSpec.getBGColor());
			// if (mapSpecHasChanged)
			mOffScreen = null;
			clearSavedETOPOLocs();
			mStnOffScreen = null;
			mObsMarker = null;
			if (mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION
			    || mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION) {
				this.setSelectionAsRectangle();
			}
			else if (mMapSpec.getProjection() > JOAConstants.MILLERPROJECTION
			    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
				this.setSelectionAsPSRectangle();
			}
			else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
				this.setSelectionAsPSRectangle(); // to do
			}
			else if (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
				this.setSelectionAsRectangle(); // to do
			}
			else {
				this.setSelectionAsPolarCircle();
			}
			mFrame.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
			mFrame.setSize(mFrame.getSize().width - 1, mFrame.getSize().height);
			if (((JOAMapPlotWindow)mFrame).mColorBarLegend != null) {
				((JOAMapPlotWindow)mFrame).mColorBarLegend.forceRedrawAll();
			}
			mLegend.forceRedraw();
			mFrame.validate();
			paintComponent(this.getGraphics());
			mFrame.setTitle(mMapSpec.getMapName());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Cancel button
	public void dialogCancelled(JDialog f) {
//		MapSpecification ms = ((ConfigMapPlot)f).getOrigMapSpec();
//
//		NewColorBar ncb = null;
//		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE || 
//				mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR ||
//				mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
//			ncb = mMapSpec.getStnColorColorBar();
//		}
//
//		NewColorBar ocb = null;
//		if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_ISOSURFACE ||
//				mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
//			ocb = mMapSpec.getOverlayContoursColorBar();
//		}
//
//		try {
//			if (((ConfigMapPlot)f).mLayoutChanged) {
//				if (!((ConfigMapPlot)f).mOriginalIncludeColorBar) {
//					// add the color legend if it has been removed
//					ColorBarPanel mColorBarPanel = new ColorBarPanel(mFrame, mFileViewer, ncb, mMapSpec.getCoastColor(),
//					    JOAConstants.DEFAULT_FRAME_COLOR, false, false);
//					mContents.setColorBar(mColorBarPanel);
//					mContents.getColorBar().setLinked(false);
//					mColorBarPanel.setEnhanceable(false);
//					mContents.addColorBar();
//					mFrame.setSize(mFrame.getSize().width + 100, mFrame.getSize().height);
//					mColorBarLegendPanel = null;
//				}
//				else if (((ConfigMapPlot)f).mOriginalIncludeColorBar) {
//					// remove the color legend if it has been added
//					mContents.removeColorBar();
//					mContents.setColorBar(null);
//					mFrame.setSize(mFrame.getSize().width - 100, mFrame.getSize().height);
//				}
//			}
//
//			// redo legend
//			int currWidth = mLegend.getWidth();
//			mLegend.reset(ms);
//			mLegend.setWidth(currWidth);
//			Dimension d = mLegend.getPreferredSize();
//			mLegendHeight = d.height;
//
//			// explicitly change the color bar
//			if (mContents.getColorBar() != null) {
//				mContents.getColorBar().setNewColorBar(ocb);
//			}
//
//			this.setBackground(ms.getBGColor());
//			mMapSpec = null;
//			mMapSpec = new MapSpecification(ms);
//			((ConfigMapPlot)f).mMapSpec = null;
//			((ConfigMapPlot)f).mMapSpec = new MapSpecification(ms);
//			boolean mapSpecHasChanged = ((ConfigMapPlot)f).mapSpecChanged();
//			if (mapSpecHasChanged) {
//				mOffScreen = null;
//			}
//			clearSavedETOPOLocs();
//			mStnOffScreen = null;
//			mObsMarker = null;
//			paintComponent(this.getGraphics());
//			mFrame.setSize(mFrame.getSize().width, mFrame.getSize().height);
//			mFrame.validate();
//			if (mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION
//			    || mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION) {
//				this.setSelectionAsRectangle();
//			}
//			else if (mMapSpec.getProjection() > JOAConstants.MILLERPROJECTION
//			    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
//				this.setSelectionAsPSRectangle();
//			}
//			else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
//				this.setSelectionAsPSRectangle(); // to do
//			}
//			else {
//				this.setSelectionAsPolarCircle();
//			}
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog f) {
		MapSpecification ms = null;
		try {
			ms = ((ConfigMapPlot)f).getMapSpec();
		}
		catch (ClassCastException ex) {
		}
		mMapSpec = null;
		mMapSpec = new MapSpecification(ms);
		int currWidth = mLegend.getWidth();
		mLegend.reset(ms);
		mLegend.setWidth(currWidth);
		mLegend.invalidate();
		mLegend.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
		mLegend.setSize(mFrame.getSize().width, mFrame.getSize().height);
		paintComponent(this.getGraphics());
		mFrame.setTitle(mMapSpec.getMapName());
	}

	public void dialogApplyTwo(Object d) {
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog f) {
		try {
			mMapSpec.writeToLog("Edited existing plot: " + mFrame.getTitle());
		}
		catch (Exception ex) {
		}

		// get the new map specification
		try {
			mMapSpec = ((ConfigMapPlot)f).getMapSpec();
		}
		catch (ClassCastException ex) {
			ex.printStackTrace();
		}

		NewColorBar ncb = null;
		if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE || 
				mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR ||
				mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
			ncb = mMapSpec.getStnColorColorBar();
		}

		try {
			// redo legend
			int currWidth = mLegend.getWidth();
			mLegend.reset(mMapSpec);
			mLegend.setWidth(currWidth);
			Dimension d = mLegend.getPreferredSize();
			mLegendHeight = d.height;

			try {
		
			}
			catch (ClassCastException ex) {

			}


			this.setBackground(mMapSpec.getBGColor());

			mLegend.setMapSpecification(mMapSpec);
			clearSavedETOPOLocs();
			mStnOffScreen = null;
			mObsMarker = null;

			mFrame.setSize(mFrame.getSize().width + 1, mFrame.getSize().height);
			mFrame.setSize(mFrame.getSize().width - 1, mFrame.getSize().height);
			mFrame.validate();
			this.invalidate();
			mOffScreen = null;
			if (mMapSpec.getProjection() == JOAConstants.MERCATORPROJECTION
			    || mMapSpec.getProjection() == JOAConstants.MILLERPROJECTION) {
				this.setSelectionAsRectangle();
			}
			else if (mMapSpec.getProjection() > JOAConstants.MILLERPROJECTION
			    && mMapSpec.getProjection() <= JOAConstants.STEREOPROJECTION) {
				this.setSelectionAsPSRectangle();
			}
			else if (mMapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
				this.setSelectionAsPSRectangle(); // to do
			}
			else if (mMapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
				this.setSelectionAsRectangle(); // to do
			}
			else {
				this.setSelectionAsPolarCircle();
			}
			mFrame.setTitle(mMapSpec.getMapName());
		}
		catch (Exception ex) {
			System.out.println("got exception OK redoing legend");
			ex.printStackTrace();
		}
	}

	public UVCoordinate invTransformLL(double h, double v) {
		// undo the map scaling
		double u1 = ((h + mMapSpec.getHOffset()) / mMapSpec.getMapHScale());
		double u2 = u1 / mMapSpec.getHScale() + mMapSpec.getUMin();
		double v1 = -(((v + mMapSpec.getVOffset()) / mMapSpec.getMapVScale()) - mMapSpec.getWinHeight());
		double v2 = v1 / mMapSpec.getVScale() + mMapSpec.getVMin();
		double lt = 0, ln = 0;
		double R = 1.0;

		switch (mMapSpec.getProjection()) {
			case 1: // Mercator. (SNYDER)
				ln = u2 * 180.0 / Math.PI;
				ln += mMapSpec.getCenLon();
				ln = ln < -180.0 ? ln += 360.0 : ln;
				lt = 2.0 * (Math.atan(Math.exp(v2 / R)) - Math.PI / 4.0);
				lt *= 180.0 / Math.PI;
				break;
			case 2: // MILLER CYLINDRICAL. (SNYDER)
				ln = u2 * 180.0 / Math.PI;
				ln += mMapSpec.getCenLon();
				ln = ln < -180.0 ? ln += 360.0 : ln;
				lt = 2.5 * (Math.atan(Math.exp(v2 / R / 1.25)) - Math.PI / 4.0);
				lt *= 180.0 / Math.PI;
				break;
			case 3: // JOAConstants.ORTHOGRAPHICPROJECTION
				UVCoordinate uv = orthoinv(u2, v2);
				ln = uv.u;
				lt = uv.v;
				lt *= 180.0 / Math.PI;
				ln *= 180.0 / Math.PI;
				break;
			case 4: // JOAConstants.MOLLWEIDEPROJECTION
				uv = mollinv(u2, v2);
				ln = uv.u;
				lt = uv.v;
				lt *= 180.0 / Math.PI;
				ln *= 180.0 / Math.PI;
				break;
			case 5: // JOAConstants.LAMBERTEAPROJECTION
				uv = lambertinv(u2, v2);
				ln = uv.u;
				lt = uv.v;
				lt *= 180.0 / Math.PI;
				ln *= 180.0 / Math.PI;
				break;
			case 10:
			case 11:
			case 12:
				uv = sterinv(u2, v2);
				ln = uv.u;
				lt = uv.v;
				lt *= 180.0 / Math.PI;
				ln *= 180.0 / Math.PI;
				break;
			case 13:
				// uv = robinsoninv(u2, v2); // to do
				// ln = uv.u;
				// lt = uv.v;
				lt *= 180.0 / Math.PI;
				ln *= 180.0 / Math.PI;
				break;
		}
		return new UVCoordinate(ln, lt);
	}

	public UVCoordinate mollinv(double x, double y) {
		double lt, ln;
		double arg = y / (1.4142135623731 * JOAConstants.RE_M);

		/*
		 * Because of division by zero problems, 'arg' can not be 1.0. Therefore a
		 * number very close to one is used instead.
		 * -------------------------------------------------------------------
		 */
		if (Math.abs(arg) > 0.999999999999) {
			arg = 0.999999999999;
		}
		double theta = Math.asin(arg);
		ln = adjust_lon((mMapSpec.getCenLon() * JOAConstants.F)
		    + (x / (0.900316316158 * JOAConstants.RE_M * Math.cos(theta))));
		if (ln < (-Math.PI)) {
			ln = -Math.PI;
		}
		if (ln > Math.PI) {
			ln = Math.PI;
		}
		arg = (2.0 * theta + Math.sin(2.0 * theta)) / Math.PI;
		if (Math.abs(arg) > 1.0) {
			arg = 1.0;
		}
		lt = Math.asin(arg);
		return new UVCoordinate(ln, lt);
	}

	public UVCoordinate lambertinv(double x, double y) {
		double lon, lat;

		double Rh = Math.sqrt(x * x + y * y);
		double temp = Rh / (2.0 * JOAConstants.RE_M);
		if (temp > 1) { return new UVCoordinate(0, 0); }
		double z = 2.0 * JOAFormulas.asinz(temp);
		double sin_z = Math.sin(z);
		double cos_z = Math.cos(z);

		lon = mMapSpec.getCenLon() * JOAConstants.F;

		if (Math.abs(Rh) > JOAConstants.EPSLN) {
			lat = JOAFormulas.asinz(mMapSpec.getH1() * cos_z + mMapSpec.getH2() * sin_z * y / Rh);
			temp = Math.abs(mMapSpec.getCenLat() * JOAConstants.F) - JOAConstants.HALFPI;
			if (Math.abs(temp) > JOAConstants.EPSLN) {
				temp = cos_z - mMapSpec.getH1() * Math.sin(lat);
				if (temp != 0.0) {
					lon = adjust_lon(mMapSpec.getCenLon() * JOAConstants.F + Math.atan2(x * sin_z * mMapSpec.getH2(), temp * Rh));
				}
			}
			else if (mMapSpec.getCenLat() * JOAConstants.F < 0.0) {
				lon = adjust_lon(mMapSpec.getCenLon() * JOAConstants.F - Math.atan2(-x, y));
			}
			else {
				lon = adjust_lon(mMapSpec.getCenLon() * JOAConstants.F + Math.atan2(x, -y));
			}
		}
		else {
			lat = mMapSpec.getCenLat() * JOAConstants.F;
		}
		return new UVCoordinate(lon, lat);
	}

	public UVCoordinate orthoinv(double x, double y) {
		double lon, lat;

		double lat_origin = mMapSpec.getCenLat() * JOAConstants.F;
		double lon_center = mMapSpec.getCenLon() * JOAConstants.F;

		double rh = Math.sqrt(x * x + y * y);
		// if (rh > r_major + .0000001) {
		// return null;
		// }
		double z = JOAFormulas.asinz(rh / r_major);
		double sinz = Math.sin(z);
		double cosz = Math.cos(z);
		lon = lon_center;
		if (Math.abs(rh) <= JOAConstants.EPSLN) {
			lat = lat_origin;
			return new UVCoordinate(lon, lat);
		}
		lat = JOAFormulas.asinz(cosz * mMapSpec.getH1() + (y * sinz * mMapSpec.getH2()) / rh);
		double con = Math.abs(lat_origin) - JOAConstants.HALFPI;
		if (Math.abs(con) <= JOAConstants.EPSLN) {
			if (lat_origin >= 0) {
				lon = adjust_lon(lon_center + Math.atan2(x, -y));
				return new UVCoordinate(lon, lat);
			}
			else {
				lon = adjust_lon(lon_center - Math.atan2(-x, y));
				return new UVCoordinate(lon, lat);
			}
		}
		con = cosz - mMapSpec.getH1() * Math.sin(lat);
		if ((Math.abs(con) >= JOAConstants.EPSLN) || (Math.abs(x) >= JOAConstants.EPSLN)) {
			lon = adjust_lon(lon_center + Math.atan2((x * sinz * mMapSpec.getH2()), (con * rh)));
		}

		return new UVCoordinate(lon, lat);
	}

	public UVCoordinate sterinv(double x, double y) {
		double rh; // eight above ellipsoid
		double z; // angle
		double sinz, cosz; // sin of z and cos of z
		double con;
		double r_major = 1.0;
		double sin_p10, cos_p10;
		double centerLat = mMapSpec.getCenLat() * JOAConstants.F;
		double centerLon = mMapSpec.getCenLon() * JOAConstants.F;
		double lat = 0, lon = 0;

		// Inverse equations
		sin_p10 = Math.sin(centerLat);
		cos_p10 = Math.cos(centerLat);

		rh = Math.sqrt(x * x + y * y);
		z = 2.0 * Math.atan(rh / (2.0 * r_major));
		sinz = Math.sin(z);
		cosz = Math.cos(z);
		lon = mMapSpec.getCenLon();
		if (Math.abs(rh) <= JOAConstants.EPSLN) {
			lat = JOAConstants.OFFMAP;
			return new UVCoordinate(lon, lat);
		}
		else {
			lat = Math.asin(cosz * sin_p10 + (y * sinz * cos_p10) / rh);
			con = Math.abs(centerLat) - JOAConstants.HALFPI;
			if (Math.abs(con) <= JOAConstants.EPSLN) {
				if (centerLat >= 0.0) {
					lon = adjust_lon(centerLon + Math.atan2(x, -y));
					return new UVCoordinate(lon, lat);
				}
				else {
					lon = adjust_lon(centerLon - Math.atan2(-x, y));
					return new UVCoordinate(lon, lat);
				}
			}
			else {
				con = cosz - sin_p10 * Math.sin(lat);
				if ((Math.abs(con) < JOAConstants.EPSLN) && (Math.abs(x) < JOAConstants.EPSLN)) {
					return new UVCoordinate(lon, lat);
				}
				else {
					lon = adjust_lon(centerLon + Math.atan2((x * sinz * cos_p10), (con * rh)));
				}
			}
		}
		return new UVCoordinate(lon, lat);
	}

	/*
	 * Function to adjust a longitude angle to range from -180 to 180 radians
	 * added if statments
	 * -----------------------------------------------------------------------
	 */
	public double adjust_lon(double x) {
		long count = 0;
		for (;;) {
			if (Math.abs(x) <= Math.PI) {
				break;
			}
			else if (((long)Math.abs(x / Math.PI)) < 2) {
				x = x - (JOAFormulas.sign(x) * JOAConstants.TWOPI);
			}
			else if (((long)Math.abs(x / JOAConstants.TWOPI)) < JOAConstants.MAXLONG) {
				x = x - (((long)(x / JOAConstants.TWOPI)) * JOAConstants.TWOPI);
			}
			else if (((long)Math.abs(x / (JOAConstants.MAXLONG * JOAConstants.TWOPI))) < JOAConstants.MAXLONG) {
				x = x
				    - (((long)(x / (JOAConstants.MAXLONG * JOAConstants.TWOPI))) * (JOAConstants.TWOPI * JOAConstants.MAXLONG));
			}
			else if (((long)Math.abs(x / (JOAConstants.DBLLONG * JOAConstants.TWOPI))) < JOAConstants.MAXLONG) {
				x = x
				    - (((long)(x / (JOAConstants.DBLLONG * JOAConstants.TWOPI))) * (JOAConstants.TWOPI * JOAConstants.DBLLONG));
			}
			else {
				x = x - (JOAFormulas.sign(x) * JOAConstants.TWOPI);
			}
			count++;
			if (count > JOAConstants.MAXVAL) {
				break;
			}
		}

		return (x);
	}

	public boolean processAreaSelection(Rubberband rbRect) {
		if (mWindowIsLocked) { return false; }
		// make a polygon from corners
		int[] xpoints = { rbRect.getUpperLeftX(), rbRect.getUpperRightX(), rbRect.getLowerRightX(), rbRect.getLowerLeftX(),
		    rbRect.getUpperLeftX() };
		int[] ypoints = { rbRect.getUpperLeftY(), rbRect.getUpperRightY(), rbRect.getLowerRightY(), rbRect.getLowerLeftY(),
		    rbRect.getUpperLeftY() };
		if (xpoints[0] == JOAConstants.IOFFMAPN) { return false; }
		Polygon searchPoly = new Polygon(xpoints, ypoints, 5);
		rbRect.resetRect();

		mFoundStns.removeAllElements();

		int ord = 0;
		if (mCurrSectionEditor != null) {
			ord = mCurrSectionEditor.getNumPts();
		}

		// search the data for matching stations
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// loop on the stations
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					double lat = sh.mLat;
					double lon = sh.mLon;
					UVCoordinate uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						// test to see if stn falls into polygon
						if (searchPoly.contains((int)uv.u, (int)uv.v)) {
							// create a section station object
							SectionStation secStn = new SectionStation(sh, sech, ++ord);
							mFoundStns.addElement(secStn);
						}
					}
				}
			}
		}

		// open up a section editor
		if (mFoundStns.size() > 0 && mCurrSectionEditor == null) {
			mCurrSectionEditor = new SectionEditor(this, mFileViewer, mFoundStns, mMapSpec.getLonLft(), mMapSpec.getLonRt());
			mCurrSectionEditor.pack();
			mCurrSectionEditor.setVisible(true);
			paintComponent(this.getGraphics());
		}
		else if (mFoundStns.size() > 0 && mCurrSectionEditor != null) {
			for (int s = 0; s < mFoundStns.size(); s++) {
				SectionStation st = (SectionStation)mFoundStns.elementAt(s);
				mCurrSectionEditor.addStn(st);
			}
			paintComponent(this.getGraphics());
		}
		return mFoundStns.size() > 0;
	}

	public void processSingleSelection(Point p) {
		if (mWindowIsLocked) { return; }
		mFoundStns.removeAllElements();

		int ord = 0;
		if (mCurrSectionEditor != null) {
			ord = mCurrSectionEditor.getNumPts();
		}

		Polygon srcArea = new Polygon();
		srcArea.addPoint((int)p.x - 2, (int)p.y + 2);
		srcArea.addPoint((int)p.x + 2, (int)p.y + 2);
		srcArea.addPoint((int)p.x + 2, (int)p.y - 2);
		srcArea.addPoint((int)p.x - 2, (int)p.y - 2);

		// search the data for matching stations
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// loop on the stations
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					double lat = sh.mLat;
					double lon = sh.mLon;
					UVCoordinate uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						// test to see if stn falls into polygon
						if (srcArea.contains((int)uv.u, (int)uv.v)) {
							// create a section station object
							SectionStation secStn = new SectionStation(sh, sech, ++ord);
							mFoundStns.addElement(secStn);
						}
					}
				}
			}
		}

		// open up a section editor
		if (mFoundStns.size() > 0 && mCurrSectionEditor == null) {
			mCurrSectionEditor = new SectionEditor(this, mFileViewer, mFoundStns, mMapSpec.getLonLft(), mMapSpec.getLonRt());
			mCurrSectionEditor.pack();
			mCurrSectionEditor.setVisible(true);
			paintComponent(this.getGraphics());
		}
		else if (mFoundStns.size() > 0 && mCurrSectionEditor != null) {
			for (int s = 0; s < mFoundStns.size(); s++) {
				SectionStation st = (SectionStation)mFoundStns.elementAt(s);
				mCurrSectionEditor.addStn(st);
			}
			paintComponent(this.getGraphics());
		}
	}

	public void processSectionSpline() {
		if (mWindowIsLocked) { return; }
		int ord = 0;
		mFoundStns.removeAllElements();

		// loop on spline pts
		for (int s = 0; s < mSplinePoints.size() - 2; s++) {
			Point p1 = (Point)mSplinePoints.elementAt(s);
			Point p2 = (Point)mSplinePoints.elementAt(s + 1);

			// construct the search polygon
			double dx = p1.x - p2.x;
			double dy = p1.y - p2.y;
			int width1 = ComputeSectionPixelWidth(p1);
			int width2 = ComputeSectionPixelWidth(p2);

			if (dx == 0 && dy == 0) { return; }
			double dist = Math.pow(dx * dx + dy * dy, (double)0.5);

			double p1x = p1.x + (double)width1 * (-dy / dist);
			double p1y = p1.y + (double)width1 * (dx / dist);

			double p2x = p1.x - (double)width1 * (-dy / dist);
			double p2y = p1.y - (double)width1 * (dx / dist);

			double p3x = p2.x + (double)width2 * (-dy / dist);
			double p3y = p2.y + (double)width2 * (dx / dist);

			double p4x = p2.x - (double)width2 * (-dy / dist);
			double p4y = p2.y - (double)width2 * (dx / dist);

			Polygon srcArea = new Polygon();
			srcArea.addPoint((int)p2x, (int)p2y);
			srcArea.addPoint((int)p1x, (int)p1y);
			srcArea.addPoint((int)p3x, (int)p3y);
			srcArea.addPoint((int)p4x, (int)p4y);

			// search the data for matching stations
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section)of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					// loop on the stations
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station)sech.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}
						double lat = sh.mLat;
						double lon = sh.mLon;
						UVCoordinate uv = transformLL(lat, lon);
						uv = mapScaler(uv.u, uv.v);
						if (uv.mInWind) {
							// test to see if stn falls into polygon
							if (srcArea.contains((int)uv.u, (int)uv.v)) {
								// create a section station object
								SectionStation secStn = new SectionStation(sh, sech, ord++);
								mFoundStns.addElement(secStn);
							}
						}
					}
				}
			}
		} // for splines
		mSplinePath.reset();
		mSplinePoints.removeAllElements();

		// open up a section editor
		if (mFoundStns.size() > 0 && mCurrSectionEditor == null) {
			mCurrSectionEditor = new SectionEditor(this, mFileViewer, mFoundStns, mMapSpec.getLonLft(), mMapSpec.getLonRt());
			mCurrSectionEditor.pack();
			mCurrSectionEditor.setVisible(true);
			repaint();
		}
		else if (mFoundStns.size() > 0 && mCurrSectionEditor != null) {
			for (int s = 0; s < mFoundStns.size(); s++) {
				SectionStation st = (SectionStation)mFoundStns.elementAt(s);
				mCurrSectionEditor.addStn(st);
			}
			repaint();
		}
	}

	public void processPolygonSpline(boolean shiftDown) {
		if (mWindowIsLocked) { return; }

		int ord = 0;
		// if (!shiftDown)
		mFoundStns.removeAllElements();

		// loop on spline pts to construct a Polygon
		Polygon srcArea = new Polygon();
		for (int s = 0; s < mSplinePoints.size(); s++) {
			Point p = (Point)mSplinePoints.elementAt(s);
			int x = p.x;
			int y = p.y;

			srcArea.addPoint(x, y);
		}

		// search the data for matching stations
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// loop on the stations
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					double lat = sh.mLat;
					double lon = sh.mLon;
					UVCoordinate uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						// test to see if stn falls into polygon
						if (srcArea.contains((int)uv.u, (int)uv.v)) {
							// create a section station object
							SectionStation secStn = new SectionStation(sh, sech, ord++);
							mFoundStns.addElement(secStn);
						}
					}
				}
			}
		}

		mSplinePath.reset();
		mSplinePoints.removeAllElements();

		// open up a section editor
		if (mFoundStns.size() > 0 && mCurrSectionEditor == null) {
			mCurrSectionEditor = new SectionEditor(this, mFileViewer, mFoundStns, mMapSpec.getLonLft(), mMapSpec.getLonRt());
			mCurrSectionEditor.pack();
			mCurrSectionEditor.setVisible(true);
			repaint();
		}
		else if (mFoundStns.size() > 0 && mCurrSectionEditor != null) {
			for (int s = 0; s < mFoundStns.size(); s++) {
				SectionStation st = (SectionStation)mFoundStns.elementAt(s);
				mCurrSectionEditor.addStn(st);
			}
			repaint();
		}
	}

	public boolean processCircularSelection(Rubberband rbRect) {
		if (mWindowIsLocked) { return false; }
		double startLat;
		Point thePt = rbRect.getLast();
		Point startPt = rbRect.getAnchor();
		if (thePt.x == startPt.x && thePt.y == startPt.y) { return false; }

		double[] intLons = new double[360];
		double startLon = -180.;
		for (int i = 0; i < 360; i++) {
			intLons[i] = startLon + i;
		}

		UVCoordinate uv = invTransformLL((double)thePt.x, (double)thePt.y);
		startLat = uv.v;

		int[] xpoints = new int[360];
		int[] ypoints = new int[360];

		for (int i = 0; i < 360; i++) {
			uv = transformLL(startLat, intLons[i]);
			uv = mapScaler(uv.u, uv.v);
			xpoints[i] = (int)uv.u;
			ypoints[i] = (int)uv.v;
		}
		Polygon searchPoly = new Polygon(xpoints, ypoints, 360);
		rbRect.resetRect();

		mFoundStns.removeAllElements();

		int ord = 0;
		if (mCurrSectionEditor != null) {
			ord = mCurrSectionEditor.getNumPts();
		}

		// search the data for matching stations
		for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				// loop on the stations
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					double lat = sh.mLat;
					double lon = sh.mLon;
					uv = transformLL(lat, lon);
					uv = mapScaler(uv.u, uv.v);
					if (uv.mInWind) {
						// test to see if stn falls into polygon
						if (searchPoly.contains((int)uv.u, (int)uv.v)) {
							// create a section station object
							SectionStation secStn = new SectionStation(sh, sech, ++ord);
							mFoundStns.addElement(secStn);
						}
					}
				}
			}
		}

		// open up a section editor
		if (mFoundStns.size() > 0 && mCurrSectionEditor == null) {
			mCurrSectionEditor = new SectionEditor(this, mFileViewer, mFoundStns, mMapSpec.getLonLft(), mMapSpec.getLonRt());
			mCurrSectionEditor.pack();
			mCurrSectionEditor.setVisible(true);
			paintComponent(this.getGraphics());
		}
		else if (mFoundStns.size() > 0 && mCurrSectionEditor != null) {
			for (int s = 0; s < mFoundStns.size(); s++) {
				SectionStation st = (SectionStation)mFoundStns.elementAt(s);
				mCurrSectionEditor.addStn(st);
			}
			paintComponent(this.getGraphics());
		}
		return mFoundStns.size() > 0;
	}

	public int ComputeSectionPixelWidth(Point p) {
		if (p == null) { return 0; }
		UVCoordinate uv = this.invTransformLL(p.x, p.y);
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
		uv = this.transformLL(tstLat, stLon);
		uv = this.mapScaler(uv.u, uv.v);
		int pd = ((int)uv.v - p.y) / 2;
		pd = pd < 0 ? -pd : pd;
		return pd == 0 ? 1 : pd;
	}

	public void closeSectionEditor() {
		mCurrSectionEditor = null;

		// update the station display
		paintComponent(this.getGraphics());
	}

	public void forceStnRedraw() {
		// update the station display
		paintComponent(this.getGraphics());
	}

	public boolean isFocusTraversable() {
		return true;
	}

	private class StereographicRectangle extends Rubberband {
		double[] lats = new double[4];
		double[] lons = new double[4];
		GeneralPath aPath = new GeneralPath();

		public StereographicRectangle() {
		}

		public StereographicRectangle(Component component) {
			super(component);
		}

		public void drawLast() {
			drawStereoRect(anchorPt, lastPt);
		}

		public void drawNext() {
			drawStereoRect(anchorPt, stretchedPt);
		}

		public void resetRect() {
			lons[0] = lons[1] = lons[2] = lons[3] = JOAConstants.IOFFMAPN;
			lats[0] = lats[1] = lats[2] = lats[3] = JOAConstants.IOFFMAPN;
		}

		public void drawStereoRect(Point startPt, Point thePt) {
			// assume a delta of 1.0 degrees for now
			double delta = 1.0;
			double[] intLons = new double[360];
			double[] intLats = new double[180];

			// transfor the coordinates of the startpoint and the endpoint
			UVCoordinate uv = invTransformLL((double)startPt.x, (double)startPt.y);
			double spLon = uv.u;
			double spLat = uv.v;

			// transfor the coordinates of the startpoint and the endpoint
			uv = invTransformLL((double)thePt.x, (double)thePt.y);
			double cpLon = uv.u;
			double cpLat = uv.v;

			int numLatPts = 0;
			int numLonPts = 0;
			if (startPt.x < thePt.x && startPt.y < thePt.y) {
				// situation #1
				lats[0] = spLat;
				lons[0] = spLon;
				lats[1] = spLat;
				lons[1] = cpLon;
				lats[2] = cpLat;
				lons[2] = cpLon;
				lats[3] = cpLat;
				lons[3] = spLon;

				// compute intermediate lats from cpLat to spLat
				numLatPts = 0;
				while (true) {
					double tLat = cpLat + (double)numLatPts * delta;
					if (isLegalLat(tLat, cpLat, spLat)) {
						intLats[numLatPts] = tLat;
					}
					else {
						intLats[numLatPts] = spLat;
						break;
					}
					numLatPts++;
				}

				// compute intermediate lons from sp to cp
				numLonPts = 0;
				while (true) {
					double tLon = spLon + (double)numLonPts * delta;
					if (isLegalLon(tLon, spLon, cpLon)) {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = tLon;
					}
					else {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = cpLon;
						break;
					}
					numLonPts++;
				}
			}
			else if (startPt.x > thePt.x && startPt.y < thePt.y) {
				// situation #2
				lats[0] = spLat;
				lons[0] = cpLon;
				lats[1] = spLat;
				lons[1] = spLon;
				lats[2] = cpLat;
				lons[2] = spLon;
				lats[3] = cpLat;
				lons[3] = cpLon;

				// compute intermediate lats from cpLat to spLat
				numLatPts = 0;
				while (true) {
					double tLat = cpLat + (double)numLatPts * delta;
					if (isLegalLat(tLat, cpLat, spLat)) {
						intLats[numLatPts] = tLat;
					}
					else {
						intLats[numLatPts] = spLat;
						break;
					}
					numLatPts++;
				}

				// compute intermediate lons from sp to cp
				numLonPts = 0;
				while (true) {
					double tLon = cpLon + (double)numLonPts * delta;
					if (isLegalLon(tLon, cpLon, spLon)) {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = tLon;
					}
					else {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = spLon;
						break;
					}
					numLonPts++;
				}
			}
			else if (startPt.x > thePt.x && startPt.y > thePt.y) {
				// situation #3
				lats[0] = cpLat;
				lons[0] = cpLon;
				lats[1] = cpLat;
				lons[1] = spLon;
				lats[2] = spLat;
				lons[2] = spLon;
				lats[3] = spLat;
				lons[3] = cpLon;

				// compute intermediate lats from spLat to cpLat
				numLatPts = 0;
				while (true) {
					double tLat = spLat + (double)numLatPts * delta;
					if (isLegalLat(tLat, spLat, cpLat)) {
						intLats[numLatPts] = tLat;
					}
					else {
						intLats[numLatPts] = cpLat;
						break;
					}
					numLatPts++;
				}

				// compute intermediate lons from cp to sp
				numLonPts = 0;
				while (true) {
					double tLon = cpLon + (double)numLonPts * delta;
					if (isLegalLon(tLon, cpLon, spLon)) {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = tLon;
					}
					else {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = spLon;
						break;
					}
					numLonPts++;
				}
			}
			else if (startPt.x < thePt.x && startPt.y > thePt.y) {
				// situation #4
				lats[0] = cpLat;
				lons[0] = spLon;
				lats[1] = cpLat;
				lons[1] = cpLon;
				lats[2] = spLat;
				lons[2] = cpLon;
				lats[3] = spLat;
				lons[3] = spLon;

				// compute intermediate lats from spLat to cpLat
				numLatPts = 0;
				while (true) {
					double tLat = spLat + (double)numLatPts * delta;
					if (isLegalLat(tLat, spLat, cpLat)) {
						intLats[numLatPts] = tLat;
					}
					else {
						intLats[numLatPts] = cpLat;
						break;
					}
					numLatPts++;
				}

				// compute intermediate lons from sp to cp
				numLonPts = 0;
				while (true) {
					double tLon = spLon + (double)numLonPts * delta;
					if (isLegalLon(tLon, spLon, cpLon)) {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = tLon;
					}
					else {
						if (tLon > 180) {
							tLon = tLon - 360;
						}
						intLons[numLonPts] = cpLon;
						break;
					}
					numLonPts++;
				}
			}
			// System.out.println("***********");
			// System.out.println("numLatPts=" + numLatPts);
			// System.out.println("numLonPts=" + numLonPts);

			/*
			 * uv = transformLL(lats[0], lons[0]); uv = Scaler(uv.u, uv.v); if
			 * (uv.mInWind) { aPath.moveTo((float)uv.u, (float)uv.v);
			 * aPath.lineTo((float)uv.u, (float)uv.v); }
			 * 
			 * uv = transformLL(lats[1], lons[1]); uv = Scaler(uv.u, uv.v); if
			 * (uv.mInWind) { aPath.moveTo((float)uv.u, (float)uv.v);
			 * aPath.lineTo((float)uv.u, (float)uv.v); }
			 * 
			 * uv = transformLL(lats[2], lons[2]); uv = Scaler(uv.u, uv.v); if
			 * (uv.mInWind) { aPath.moveTo((float)uv.u, (float)uv.v);
			 * aPath.lineTo((float)uv.u, (float)uv.v); }
			 * 
			 * uv = transformLL(lats[3], lons[3]); uv = Scaler(uv.u, uv.v); if
			 * (uv.mInWind) { aPath.moveTo((float)uv.u, (float)uv.v);
			 * aPath.lineTo((float)uv.u, (float)uv.v); }
			 * 
			 * aPath.reset(); if (startPt.x < thePt.x && startPt.y < thePt.y) { //
			 * compute the corners ulH = startPt.x; ulV = startPt.y; lrH = thePt.x;
			 * lrV = thePt.y;
			 * 
			 * UVCoordinate uv = invTransformLL((double)ulH, (double)ulV); lons[0] =
			 * uv.u; lats[0] = uv.v;
			 * 
			 * uv = invTransformLL((double)lrH, (double)lrV); lons[2] = uv.u; lats[2]
			 * = uv.v;
			 * 
			 * lats[1] = lats[0]; lons[1] = lons[2]; lats[3] = lats[2]; lons[3] =
			 * lons[0]; } else if (startPt.x > thePt.x && startPt.y > thePt.y) { ulH =
			 * thePt.x; ulV = thePt.y; lrH = startPt.x; lrV = startPt.y; // compute
			 * the corners UVCoordinate uv = invTransformLL((double)ulH, (double)ulV);
			 * lons[0] = uv.u; lats[0] = uv.v;
			 * 
			 * uv = invTransformLL((double)lrH, (double)lrV); lons[2] = uv.u; lats[2]
			 * = uv.v;
			 * 
			 * lats[1] = lats[0]; lons[1] = lons[2]; lats[3] = lats[2]; lons[3] =
			 * lons[0]; } else if (startPt.x > thePt.x && startPt.y < thePt.y) { urH =
			 * startPt.x; urV = startPt.y; llH = thePt.x; llV = thePt.y; // compute
			 * the corners UVCoordinate uv = invTransformLL((double)urH, (double)urV);
			 * lons[1] = uv.u; lats[1] = uv.v;
			 * 
			 * uv = invTransformLL((double)llH, (double)llV); lons[3] = uv.u; lats[3]
			 * = uv.v;
			 * 
			 * lats[0] = lats[1]; lons[0] = lons[3]; lats[2] = lats[3]; lons[2] =
			 * lons[1]; } else if (startPt.x < thePt.x && startPt.y > thePt.y) { //
			 * compute the corners urH = thePt.x; urV = thePt.y; llH = startPt.x; llV
			 * = startPt.y;
			 * 
			 * UVCoordinate uv = invTransformLL((double)urH, (double)urV); lons[1] =
			 * uv.u; lats[1] = uv.v;
			 * 
			 * uv = invTransformLL((double)llH, (double)llV); lons[3] = uv.u; lats[3]
			 * = uv.v;
			 * 
			 * lats[0] = lats[1]; lons[0] = lons[3]; lats[2] = lats[3]; lons[2] =
			 * lons[1]; } // compute and draw the intermediate points // number of lon
			 * points boolean crossed180GoingEast = false; boolean crossed180GoingWest
			 * = false; boolean crossed0GoingEast = false; boolean crossed0GoingWest =
			 * false; int numLonPts; if (JOAFormulas.signChanged(lons[0], lons[1]) &&
			 * lons[0] > 0) { // crossed 180 going east numLonPts = (int)(Math.abs(360
			 * + lons[1] - lons[0])/delta); crossed180GoingEast = true; } else if
			 * (JOAFormulas.signChanged(lons[1], lons[0]) && lons[1] > 90) { //
			 * crossed 180 going west numLonPts = (int)(Math.abs(-lons[1] + (180 -
			 * lons[0]))/delta); crossed180GoingWest = true; } else if
			 * (JOAFormulas.signChanged(lons[0], lons[1]) && lons[0] < 0) { // crossed
			 * GM going east numLonPts =(int)(Math.abs(-lons[0] + lons[1])/delta);
			 * crossed0GoingEast = true; } else if (JOAFormulas.signChanged(lons[1],
			 * lons[0]) && lons[1] < 0) { // crossed GM going west numLonPts =
			 * (int)(Math.abs(lons[0] + -lons[1])/delta); crossed0GoingWest = true; }
			 * else numLonPts = (int)(Math.abs(lons[0] - lons[1])/delta); if
			 * (numLonPts <= 0) return; // number of lat points int numLatPts;
			 * numLatPts = (int)(Math.abs(lats[0] - lats[2])/delta);
			 * 
			 * double startLat = 0; if (lats[3] > lats[0]) { double temp; temp =
			 * lats[0]; lats[0] = lats[3]; lats[3] = temp; temp = lats[1]; lats[1] =
			 * lats[2]; lats[2] = temp; } startLat = lats[0];
			 * 
			 * //System.out.println("***********"); double startLon = lons[0];
			 * numLonPts++; for (int i=0; i<numLonPts; i++) { double tLon = startLon +
			 * (double)i * delta; if (crossed180GoingEast) { if (tLon > 180) tLon =
			 * tLon - 360; } else if (crossed180GoingWest) { if (tLon < -180) tLon =
			 * tLon - 360; } intLons[i] = tLon; //System.out.println("intLons[i] = " +
			 * intLons[i] + " i=" + i); } //intLons[numLonPts] = lons[2];
			 * //System.out.println("numLonPts = " + numLonPts); // compute the
			 * intermediate lats for (int i=0; i<numLatPts; i++) { intLats[i] =
			 * startLat - (double)i * delta; } intLats[numLatPts++] = lats[2];
			 */

			// if (numLonPts > 0 && numLatPts > 0) {
			// draw the "rectangle"
			aPath.reset();

			// explicitly move to the beginiing of the shape
			uv = transformLL(lats[0], lons[0]);
			uv = mapScaler(uv.u, uv.v);

			float firstU = (float)uv.u;
			float firstV = (float)uv.v;
			aPath.moveTo(firstU, firstV);

			// first line is drawn from nw corner to ne corner
			for (int i = 0; i < numLonPts; i++) {
				uv = transformLL(lats[0], intLons[i]);
				uv = mapScaler(uv.u, uv.v);
				if (uv.mInWind) {
					aPath.lineTo((float)uv.u, (float)uv.v);
				}
			}
			// second segment is drawn from ne corner down to se corner
			// System.out.println("ne to se*********** fixed lon = " + lons[2]);
			for (int i = numLatPts - 1; i >= 0; i--) {
				uv = transformLL(intLats[i], lons[1]);
				uv = mapScaler(uv.u, uv.v);
				if (uv.mInWind) {
					try {
						aPath.lineTo((float)uv.u, (float)uv.v);
						// System.out.println("computed lat =" + intLats[i] + ", u= " + uv.u
						// + " v=" + uv.v);
					}
					catch (Exception ex) {
						System.out.println("Got exception on second seg");
					}
				}
			}

			// third segment is drawn from se to sw
			// System.out.println("se to sw*********** fixed lat = " + lats[2]);
			for (int i = numLonPts - 1; i >= 0; i--) {
				uv = transformLL(lats[2], intLons[i]);
				uv = mapScaler(uv.u, uv.v);
				if (uv.mInWind) {
					try {
						aPath.lineTo((float)uv.u, (float)uv.v);
						// System.out.println("computed lat =" + intLats[i] + ", u= " + uv.u
						// + " v=" + uv.v);
					}
					catch (Exception ex) {
						System.out.println("Got excpetion on third seg");
					}
				}
			}

			// fourth segment from sw to nw
			// System.out.println("sw to nw*********** fixed lon = " + lons[0]);
			for (int i = 0; i < numLatPts - 1; i++) {
				uv = transformLL(intLats[i], lons[0]);
				uv = mapScaler(uv.u, uv.v);
				if (uv.mInWind) {
					try {
						aPath.lineTo((float)uv.u, (float)uv.v);
					}
					catch (Exception ex) {
						System.out.println("Got exception on fourth seg");
						System.out.println(lons[0] + " computed lat =" + intLats[i] + ", u= " + uv.u + " v=" + uv.v);
						for (int ii = 0; ii < numLatPts; ii++) {
							System.out.println(intLats[ii]);
						}
					}
				}
			}
			try {
				aPath.lineTo(firstU, firstV);
			}
			catch (Exception ex) {
				System.out.println("Got excpetion completing shape");
			}

			((RubberbandPanel)component).setRubberbandDisplayObject(aPath, false);
		}

		public int getUpperLeftX() {
			if (lats[0] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[0], lons[0]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getUpperRightX() {
			if (lats[1] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[1], lons[1]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getLowerRightX() {
			if (lats[2] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[2], lons[2]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getLowerLeftX() {
			if (lats[3] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[3], lons[3]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getUpperLeftY() {
			if (lats[0] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[0], lons[0]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public int getUpperRightY() {
			if (lats[1] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[1], lons[1]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public int getLowerRightY() {
			if (lats[2] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[2], lons[2]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public int getLowerLeftY() {
			if (lats[3] == JOAConstants.IOFFMAPN) { return JOAConstants.IOFFMAPN; }
			UVCoordinate uv = transformLL(lats[3], lons[3]);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public boolean isPoint() {
			if (Math.abs(endPt.x - anchorPt.x) > 5 || Math.abs(endPt.y - anchorPt.y) > 5) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	private class PolarCircle extends Rubberband {
		double[] intLons = new double[360];
		double[] lats = new double[4];
		double[] lons = new double[4];
		GeneralPath aPath = new GeneralPath();

		public PolarCircle() {
		}

		public PolarCircle(Component component) {
			super(component);

			double startLon = -180.;
			for (int i = 0; i < 360; i++) {
				intLons[i] = startLon + i;
			}
		}

		public void drawLast() {
			drawPolarCircle(anchorPt, lastPt);
		}

		public void drawNext() {
			drawPolarCircle(anchorPt, stretchedPt);
		}

		public void resetRect() {
			// lons[0] = lons[1] = lons[2] = lons[3] = JOAConstants.IOFFMAPN;
			// lats[0] = lats[1] = lats[2] = lats[3] = JOAConstants.IOFFMAPN;
		}

		public void drawPolarCircle(Point startPt, Point thePt) {
			if (thePt.x == startPt.x && thePt.y == startPt.y) { return; }

			UVCoordinate uv = invTransformLL((double)thePt.x, (double)thePt.y);
			lons[0] = uv.u;
			lats[0] = uv.v;

			uv = invTransformLL((double)startPt.x, (double)startPt.y);
			lons[1] = uv.u;
			lats[1] = uv.v;

			float[] xpoints = new float[360];
			float[] ypoints = new float[360];

			for (int i = 0; i < 360; i++) {
				uv = transformLL(lats[0], intLons[i]);
				uv = mapScaler(uv.u, uv.v);
				xpoints[i] = (float)uv.u;
				ypoints[i] = (float)uv.v;
			}
			// Polygon searchPoly = new Polygon(xpoints, ypoints, 360);
			aPath.reset();
			aPath.moveTo(xpoints[0], ypoints[0]);
			for (int i = 1; i < 360; i++) {
				aPath.lineTo(xpoints[i], ypoints[i]);
			}
			aPath.lineTo(xpoints[0], ypoints[0]);

			((RubberbandPanel)component).setRubberbandDisplayObject(aPath, false);
		}

		public int getUpperLeftX() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getUpperRightX() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			return (int)uv.u;
		}

		public int getLowerRightX() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)anchorPt.x, (double)anchorPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getLowerLeftX() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.u;
		}

		public int getUpperLeftY() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public int getUpperRightY() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public int getLowerRightY() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public int getLowerLeftY() {
			// if (lats[0] == JOAConstants.IOFFMAPN)
			// return JOAConstants.IOFFMAPN;
			UVCoordinate uv = invTransformLL((double)endPt.x, (double)endPt.y);
			double lon = uv.u;
			double lat = uv.v;
			uv = transformLL(lat, lon);
			uv = mapScaler(uv.u, uv.v);
			return (int)uv.v;
		}

		public boolean isPoint() {
			if (Math.abs(endPt.x - anchorPt.x) > 5 || Math.abs(endPt.y - anchorPt.y) > 5) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	public MapSpecification getMapSpec() {
		return mMapSpec;
	}

	public UVCoordinate getCorrectedXY(int x, int y) {
		return new UVCoordinate(Double.NaN, Double.NaN);
	}

	public double[] getInvTransformedX(double x) {
		double[] xvals = new double[1];
		return xvals;
	}

	public double[] getInvTransformedY(double y) {
		double[] yvals = new double[1];
		return yvals;
	}
	public void drawColorBar(Graphics2D g, NewColorBar colorBar, int height, int width, int leftMargin, int orientation) {
		int numColors = colorBar.getNumLevels();
		int left = leftMargin;
		int right = width;
		int top = 15;
		int bottom = height;
		int pixelsPerBand = (bottom - top - 2) / numColors;
		int bandTop = 0;
		int bandBottom = 0;
		int bandLeft = 0;
		int bandRight = 0;

		g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
		    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
		FontMetrics fm = g.getFontMetrics();
		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			top = 0;
			bottom = height - 30;
			pixelsPerBand = (right - left - 5) / numColors;
		}

		// draw the color ramp and labels
		double base = colorBar.getBaseLevel();
		double end = colorBar.getEndLevel();
		double diff = Math.abs(end - base);
		int numPlaces = 2;
		if (diff < 10) {
			numPlaces = 3;
		}
		else if (diff >= 10 && diff < 100) {
			numPlaces = 2;
		}
		else if (diff >= 100 && diff < 1000) {
			numPlaces = 1;
		}
		else if (diff >= 1000) {
			numPlaces = 1;
		}

		int labelInc = 0;
		if (numColors <= 16) {
			labelInc = 1;
		}
		else if (numColors > 16 && numColors <= 32) {
			labelInc = 2;
		}
		else if (numColors > 32 && numColors <= 48) {
			labelInc = 3;
		}
		else if (numColors > 48 && numColors <= 64) {
			labelInc = 4;
		}
		else if (numColors > 64) {
			labelInc = 5;
		}

		// find the length of the longest label
		int maxWidth = 0;
		int labelHInc = 1;
		boolean offsetLabels = false;
		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			for (int i = 0; i < numColors; i++) {
				String sTemp = null;
				if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
					sTemp = colorBar.getFormattedValue(i) + "m";
				}
				else {
					sTemp = colorBar.getFormattedValue(i);
				}
				int len = fm.stringWidth(sTemp);
				maxWidth = len > maxWidth ? len : maxWidth;
			}

			// find the number of labels that will fit
			if (maxWidth < pixelsPerBand - 10) {
				// all labels will fit w/o offset or dropping labels
				offsetLabels = false;
			}
			else if ((maxWidth < (2 * pixelsPerBand) - 10) && numColors >= 16) {
				// all labels will fit with offset
				offsetLabels = true;
			}
			else {
				// compute how many labels to skip
				int numFit = (numColors * pixelsPerBand) / (maxWidth + 5);
				labelHInc = (int)(Math.round((double)numColors / (double)numFit));
			}
		}

		int maxH = 0;
		int maxLabelH = 0;
		for (int i = 0; i < numColors; i++) {
			// swatch
			if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				bandLeft = (int)(left + i * pixelsPerBand);
				bandRight = (int)(bandLeft + pixelsPerBand);
				maxH = bandRight > maxH ? bandRight : maxH;
				g.setColor(colorBar.getColorValue(i));
				g.fillRect(bandLeft, top, bandRight - bandLeft, bottom);

				// draw a tic mark and labels
				g.setColor(Color.black);
				if (offsetLabels && i > 0 && i % 2 == 0) {
					String sTemp = colorBar.getFormattedValue(i - 1) + "m";
					g.drawLine(bandLeft, bottom, bandLeft, bottom + 15);
					g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 30);
				}
				else if (offsetLabels && i > 0) {
					String sTemp = colorBar.getFormattedValue(i - 1) + "m";
					g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
					g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
				}
				else if (i > 0) {
					String sTemp = colorBar.getFormattedValue(i - 1) + "m";
					if (i % labelHInc == 0) {
						g.drawLine(bandLeft, bottom, bandLeft, bottom + 10);
						g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
						maxLabelH = bandLeft + (fm.stringWidth(sTemp) / 2);
					}
					else {
						g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
					}
				}
			}
			else {
				bandTop = (int)(top + (i) * pixelsPerBand);
				bandBottom = (int)(bandTop + pixelsPerBand);
				g.setColor(colorBar.getColorValue(i));
				g.fillRect(left + 10, bandTop, left + 25, bandBottom - bandTop);

				// label
				g.setColor(Color.black);
				if (i % labelInc == 0) {
					String sTemp = colorBar.getFormattedValue(i, numPlaces, true);
					// String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal),
					// numPlaces, true);
					g.drawString(sTemp, left + 35, bandBottom);
				}
			}
		}

		// plot the last horizontal label
		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION && offsetLabels) {
			if (numColors % 2 == 0) {
				double myVal = colorBar.getDoubleValue(numColors - 1);
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
				g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 30);
			}
			else {
				double myVal = colorBar.getDoubleValue(numColors - 1);
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false);
				g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 20);
			}
		}
		else if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			double myVal = colorBar.getDoubleValue(numColors - 1);
			String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
			int l = bandRight - (fm.stringWidth(sTemp) / 2);
			if (l > maxLabelH + 5) {
				g.drawString(sTemp, l, bottom + 20);
			}
		}

		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			// outline colorbar
			g.drawRect(left, top, maxH - left, bottom);

			// plot the last tic mark
			if (numColors % 2 == 0 && offsetLabels) {
				g.drawLine(maxH, bottom, maxH, bottom + 15);
			}
			else {
				g.drawLine(maxH, bottom, maxH, bottom + 5);
			}
		}

		if (orientation != JOAConstants.HORIZONTAL_ORIENTATION) {
			// put the label here
			String panelLabel = null;
			if (mFileViewer != null) {
				int pPos = mFileViewer.getPropertyPos(colorBar.getParam(), false);
				if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null
				    && mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
					panelLabel = new String(colorBar.getParam() + " (" + mFileViewer.mAllProperties[pPos].getUnits() + ")");
				}
				else {
					panelLabel = new String(colorBar.getParam());
				}
			}
			else {
				panelLabel = new String(colorBar.getParam());
			}
			int strWidth = fm.stringWidth(panelLabel);
			JOAFormulas.drawStyledString(panelLabel, right / 2 - strWidth / 2, bandBottom + 10, fm, (Graphics2D)g);
		}
	}
}

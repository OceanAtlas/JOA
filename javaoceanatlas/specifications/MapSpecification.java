/*
 * $Id: MapSpecification.java,v 1.12 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import java.awt.*;
import org.w3c.dom.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.utility.*;
import java.io.File;
import java.io.IOException;

public class MapSpecification implements PlotSpecification {
	public static int BLACK_CONTOURS = 0;
	public static int WHITE_CONTOURS = 1;
	public static int CUSTOM_CONTOURS = 2;

	public static int COLOR_STNS_BY_JOADEFAULT = 0;
	public static int COLOR_STNS_BY_ISOSURFACE = 1;
	public static int COLOR_STNS_BY_STNVAR = 2;
	public static int COLOR_STNS_BY_STN_METADATA = 3;

	public static int CONTOUR_OVERLAY_BY_NONE = 0;
	public static int CONTOUR_OVERLAY_BY_ISOSURFACE = 1;
	public static int CONTOUR_OVERLAY_BY_STNVAR = 2;

	// General Map Settings
  private int mCoastLineRez = JOAConstants.NOCOAST;
	private int mWinHeight;
	private int mCurrBasin;
	private int mLineWidth;
	private boolean mPlotStnSymbols;
	private int mSymbolSize;
	private int mSymbol;
	private int mProjection;
  private double mCenLat;
	private double mLonRt;
	private double mLonLft;
	private double mLatMin;
	private double mLatMax;
	private double mCenLon;
  private boolean mDrawLegend = true;
	private boolean mAutoGraticule = false;
	private boolean mRetainProjAspect;
	private boolean mDrawGraticule;
	private boolean mConnectStnsAcrossSections;
	private boolean mConnectStns;
  private double h3;
	private double h2;
	private double h1;
	private double mVScale;
	private double mHScale;
	private double mVMin;
	private double mVMax;
	private double mUMax;
	private double mUMin;
	private double mLonGratSpacing;
	private double mLatGratSpacing;
  private double mMapVScale;
	private double mMapHScale;
	private double mVOffset;
	private double mHOffset;
  private Color mSectionColor;
	private Color mCoastColor;
	private Color mGratColor;
	private Color mBGColor;
  private String mMapName = null;
  private boolean mPlotSectionLabels = false;
  private boolean mPlotGratLabels = true;
  private Color mLabelColor = new Color(204, 51, 0);
  private boolean mGlobe = false;
  private boolean mPlotStnLabels = false;
  private double mStnLabelAngle = 45.0;
  private int mStnLabelOffset = 5;
  private int mContourLabelPrec = 2;
  private boolean mCustomMap = false;

  private int mStnColorMode = COLOR_STNS_BY_JOADEFAULT;

  // variables for station symbol coloring By IsoSurface
  private int mStnColorByIsoVarCode;
  private int mStnColorByIsoSurfVarCode;
  private NewInterpolationSurface mStnColorByIsoSurface;
  private double mStnColorByIsoIsoSurfaceValue;
  private double mStnColorByIsoReferenceLevel = -99;
  private boolean mStnColorByIsoIsReferenced = false;
  private boolean mStnColorByIsoMinIsoSurfaceValue = false;
  private boolean mStnColorByIsoMaxIsoSurfaceValue = false;
  private NewColorBar mStnColorColorBar;
  private boolean mStnColorByIsoBottomUpSearch;
  private boolean mStnColorByIsoLocalInterpolation = false;
  private int mStnColorByIsoMaxInterpDistance = 2;
  private boolean[] mStnColorByIsoMeanCastStnList = null;
  private boolean mStnColorByIsoIsResidualInterp = false;
  private boolean mStnColorByIsoAutoscaledColorCB = false;

  // variables for station symbol coloring By station parameter
  private int mStnColorByStnValVarCode;
  private boolean mStnColorByStnValAutoscaledColorBar;
  
  // variables for contours
  private int mContourOverlayMode = CONTOUR_OVERLAY_BY_NONE;
  
  // Isosurface Overlay Contours
  private int mIsoContourStyle = BLACK_CONTOURS;
  private int mIsoContourVarCode;
  private int mIsoContourSurfVarCode;
  private NewInterpolationSurface mIsoContourSurface;
  private double mContourIsoSurfaceValue;
  private double mIsoContourReferenceLevel = -99;
  private boolean mIsoContourReferenced = false;
  private boolean mIsoContourMinSurfaceValue = false;
  private boolean mIsoContourMaxSurfaceValue = false;
  private NewColorBar mOverlayContoursColorBar;
  private boolean mIsoContourBottomUpSearch;
  private boolean mIsoContourLocalInterpolation;
  private int mIsoContourMaxInterpDistance = 2;
  private boolean mIsoContourAutoscaledColorCB = false;
  private Color mIsoContourColor = Color.black;
  private boolean[] mIsoContMeanCastStnList = null;
  private boolean mIsoContIsResidualInterp = false;
  private double mIsoContRefLevel; 

  // Stn Calc Overlay Contours
  private int mStnContourStyle = BLACK_CONTOURS;
  private int mStnCalcContourVarCode;
  private boolean mStnCalcContourAutoscaledColorCB = false;
  private Color mStnVarCalcContourColor = Color.black;
  
  // Common to both overlay contour modes
  private boolean mFilledIsoContours = false;
  private boolean mFilledStnContours = false;
  private int mPlotEveryNthContour = 1; 
  private int mNX = 101;
  private int mNY = 101;
  private double mCAY = 5.0;
  private int mNRng = 10;
  private boolean mMaskCoast = true;

  // attached bathy and coastlines
  private String[] mEtopoFiles = new String[10];
  private int mNumEtopoFiles = 0;
  private boolean mColorFill = true;
  private int mNumIsobaths;
  private double[] mIsobathValues = new double[120];
  private Color[] mIsobathColors = new Color[120];
  private String[] mIsobathPaths = new String[120];
  private String[] mIsobathDescrips = new String[120];
  private String mCustCoastPath = null;
  private String mCustCoastDescrip = null;
  private NewColorBar mBathyColorBar;
  private NewColorBar mMaskColorBar = null;

  public MapSpecification() {
  }

  public MapSpecification(MapSpecification inSpec) {
    mProjection = inSpec.getProjection();
    mSymbol = inSpec.getSymbol();
    mSymbolSize = inSpec.getSymbolSize();
    mPlotStnSymbols = inSpec.isPlotStnSymbols();
    mLineWidth = inSpec.getLineWidth();
    mCurrBasin = inSpec.getCurrBasin();
    mWinHeight = inSpec.getWinHeight();
    mCoastLineRez = inSpec.getCoastLineRez();
    mLatMax = inSpec.getLatMax();
    mLatMin = inSpec.getLatMin();
    mLonRt = inSpec.getLonRt();
    mLonLft = inSpec.getLonLft();
    mCenLat = inSpec.getCenLat();
    mCenLon = inSpec.getCenLon();
    mConnectStns = inSpec.isConnectStns();
    mConnectStnsAcrossSections = inSpec.isConnectStnsAcrossSections();
    mDrawGraticule = inSpec.isDrawGraticule();
    mRetainProjAspect = inSpec.isRetainProjAspect();
    mAutoGraticule = inSpec.isAutoGraticule();
    mDrawLegend = inSpec.isDrawLegend();
    mLatGratSpacing = inSpec.getLatGratSpacing();
    mLonGratSpacing = inSpec.getLonGratSpacing();
    mUMax = inSpec.getUMax();
    mUMin = inSpec.getUMin();
    mVMax = inSpec.getVMax();
    mVMin = inSpec.getVMin();
    mHScale = inSpec.getHScale();
    mVScale = inSpec.getVScale();
    h1 = inSpec.getH1();
    h2 = inSpec.getH2();
    h3 = inSpec.getH3();
    mHOffset = inSpec.getHOffset();
    mVOffset = inSpec.getVOffset();
    mMapHScale = inSpec.getMapHScale();
    mMapVScale = inSpec.getMapVScale();
    mBGColor = inSpec.getBGColor();
    mGratColor = inSpec.getGratColor();
    mCoastColor = inSpec.getCoastColor();
    mSectionColor = inSpec.getSectionColor();
    mMapName = inSpec.getMapName();
    mColorFill = inSpec.isColorFill();

    mNumEtopoFiles = inSpec.getNumEtopoFiles();
    if (mNumEtopoFiles > 0) {
      for (int i = 0; i < mNumEtopoFiles; i++) {
        if (inSpec.getEtopoFile(i) != null) {
          mEtopoFiles[i] = new String(inSpec.getEtopoFile(i));
        }
      }
    }

		if (isStnColorByIsoIsResidualInterp()) {
			setStnColorByIsoMeanCastStnList(null);
			setStnColorByIsoMeanCastStnList(new boolean[inSpec.getStnColorByIsoMeanCastStnList().length]);
			for (int i = 0; i < inSpec.getStnColorByIsoMeanCastStnList().length; i++) {
				setStnColorByIsoMeanCastStn(i, inSpec.isStnColorByIsoMeanCastStn(i));
			}
		}

		if (isIsoContIsResidualInterp()) {
			setIsoContMeanCastStnList(null);
			setIsoContMeanCastStnList(new boolean[inSpec.getIsoContMeanCastStnList().length]);
			for (int i = 0; i < inSpec.getIsoContMeanCastStnList().length; i++) {
				setIsoContByIsoMeanCastStn(i, inSpec.isIsoContByIsoMeanCastStn(i));
			}
		}

    for (int i = 0; i < inSpec.getNumIsobaths(); i++) {
      mIsobathValues[i] = inSpec.getValue(i);
      mIsobathPaths[i] = inSpec.getPath(i);
      mIsobathColors[i] = inSpec.getColor(i);
      mIsobathDescrips[i] = inSpec.getDescrip(i);
    }
    mNumIsobaths = inSpec.getNumIsobaths();
    if (inSpec.getCustCoastPath() != null) {
      mCustCoastPath = new String(inSpec.getCustCoastPath());
    }
    if (inSpec.getCustCoastDescrip() != null) {
      mCustCoastDescrip = new String(inSpec.getCustCoastDescrip());
    }
    mPlotSectionLabels = inSpec.isPlotSectionLabels();

    mPlotGratLabels = inSpec.isPlotGratLabels();
    mLabelColor = inSpec.getLabelColor();
    mGlobe = inSpec.mGlobe;
//    mIsAutoscaledColorCB = inSpec.mIsAutoscaledColorCB;
    mPlotStnLabels = inSpec.isPlotStnLabels();
    mStnLabelAngle = inSpec.getStnLabelAngle();
    mStnLabelOffset = inSpec.getStnLabelOffset();
    mContourLabelPrec = inSpec.getContourLabelPrec();
    mCustomMap = inSpec.isCustomMap();

    mContourOverlayMode = inSpec.getContourOverlayMode();
    mFilledIsoContours = inSpec.isFilledIsoContours();
    mFilledStnContours = inSpec.isFilledStnContours();
    mIsoContourSurfVarCode = inSpec.getIsoContourSurfVarCode();
    if (inSpec.mIsoContourSurface != null) {
      mIsoContourSurface = new NewInterpolationSurface(inSpec.getIsoContourSurface());
    }
    mIsoContourReferenceLevel = inSpec.getIsoContourReferenceLevel();
    mIsoContourReferenced = inSpec.isIsoContourReferenced();
    mContourIsoSurfaceValue = inSpec.getContourIsoSurfaceValue();
    mIsoContourMinSurfaceValue = inSpec.isIsoContourMinSurfaceValue();
    mIsoContourMaxSurfaceValue = inSpec.isIsoContourMaxSurfaceValue();
    mOverlayContoursColorBar = new NewColorBar(inSpec.getOverlayContoursColorBar());
    mIsoContourBottomUpSearch = inSpec.isIsoContourBottomUpSearch();
    mIsoContourLocalInterpolation = inSpec.isIsoContourLocalInterpolation();
    mIsoContourMaxInterpDistance = inSpec.getIsoContourMaxInterpDistance();
    if (inSpec.getBathyColorBar() != null) {
      mBathyColorBar = new NewColorBar(inSpec.getBathyColorBar());
    }
//    mIsContourAutoscaledColorCB = inSpec.mIsContourAutoscaledColorCB;
    mIsoContourVarCode = inSpec.getIsoContourVarCode();
    mPlotEveryNthContour = inSpec.getPlotEveryNthContour();
    mIsoContourColor = inSpec.getIsoContourColor();
    mStnVarCalcContourColor = inSpec.getStnVarCalcContourColor();
    mNRng = inSpec.getNRng();
    mNX = inSpec.getNX();
    mNY = inSpec.getNY();
    mCAY = inSpec.getCAY();
    mMaskCoast = inSpec.isMaskCoast();
  }

  public void dumpSpecification() {
    System.out.println("mProjection=" + mProjection);
    System.out.println("mWinHeight=" + mWinHeight);
    System.out.println("mLatMax=" + mLatMax);
    System.out.println("mLatMin=" + mLatMin);
    System.out.println("mLonRt=" + mLonRt);
    System.out.println("mLonLft=" + getLonLft());
    System.out.println("mCenLat=" + mCenLat);
    System.out.println("mUMax=" + mUMax);
    System.out.println("mUMin=" + mUMin);
    System.out.println("mVMax=" + mVMax);
    System.out.println("mVMin=" + mVMin);
    System.out.println("mHScale=" + mHScale);
    System.out.println("mVScale=" + mVScale);
    System.out.println("h1=" + h1);
    System.out.println("h2=" + h2);
    System.out.println("h3=" + h3);
    System.out.println("mHOffset=" + mHOffset);
    System.out.println("mVOffset=" + mVOffset);
    System.out.println("mMapHScale=" + mMapHScale);
    System.out.println("mMapVScale=" + mMapVScale);
  }

	/* (non-Javadoc)
   * @see javaoceanatlas.specifications.PlotSpecification#saveAsXML(javaoceanatlas.ui.FileViewer, org.w3c.dom.Document, org.w3c.dom.Element)
   */
  public void saveAsXML(FileViewer fv, Document doc, Element root) {
	  // TODO Auto-generated method stub
	  
  }
	
	public String exportJSON(File file) {    
		return null;
	}

	/* (non-Javadoc)
   * @see javaoceanatlas.specifications.PlotSpecification#writeToLog(java.lang.String)
   */
  public void writeToLog(String preamble) throws IOException {
	  // TODO Auto-generated method stub
	  
  }

	/**
   * @param mProjection the mProjection to set
   */
  public void setProjection(int mProjection) {
	  this.mProjection = mProjection;
  }

	/**
   * @return the mProjection
   */
  public int getProjection() {
	  return mProjection;
  }

	/**
   * @param mSymbol the mSymbol to set
   */
  public void setSymbol(int mSymbol) {
	  this.mSymbol = mSymbol;
  }

	/**
   * @return the mSymbol
   */
  public int getSymbol() {
	  return mSymbol;
  }

	/**
   * @param mSymbolSize the mSymbolSize to set
   */
  public void setSymbolSize(int mSymbolSize) {
	  this.mSymbolSize = mSymbolSize;
  }

	/**
   * @return the mSymbolSize
   */
  public int getSymbolSize() {
	  return mSymbolSize;
  }

	/**
   * @param mLineWidth the mLineWidth to set
   */
  public void setLineWidth(int mLineWidth) {
	  this.mLineWidth = mLineWidth;
  }

	/**
   * @return the mLineWidth
   */
  public int getLineWidth() {
	  return mLineWidth;
  }

	/**
   * @param mCurrBasin the mCurrBasin to set
   */
  public void setCurrBasin(int mCurrBasin) {
	  this.mCurrBasin = mCurrBasin;
  }

	/**
   * @return the mCurrBasin
   */
  public int getCurrBasin() {
	  return mCurrBasin;
  }

	/**
   * @param mWinHeight the mWinHeight to set
   */
  public void setWinHeight(int mWinHeight) {
	  this.mWinHeight = mWinHeight;
  }

	/**
   * @return the mWinHeight
   */
  public int getWinHeight() {
	  return mWinHeight;
  }

	/**
   * @param mCoastLineRez the mCoastLineRez to set
   */
  public void setCoastLineRez(int mCoastLineRez) {
	  this.mCoastLineRez = mCoastLineRez;
  }

	/**
   * @return the mCoastLineRez
   */
  public int getCoastLineRez() {
	  return mCoastLineRez;
  }

	/**
   * @param mCenLon the mCenLon to set
   */
  public void setCenLon(double mCenLon) {
	  this.mCenLon = mCenLon;
  }

	/**
   * @return the mCenLon
   */
  public double getCenLon() {
	  return mCenLon;
  }

	/**
   * @param mLatMax the mLatMax to set
   */
  public void setLatMax(double mLatMax) {
	  this.mLatMax = mLatMax;
  }

	/**
   * @return the mLatMax
   */
  public double getLatMax() {
	  return mLatMax;
  }

	/**
   * @param mLatMin the mLatMin to set
   */
  public void setLatMin(double mLatMin) {
	  this.mLatMin = mLatMin;
  }

	/**
   * @return the mLatMin
   */
  public double getLatMin() {
	  return mLatMin;
  }

	/**
   * @param mLonLft the mLonLft to set
   */
  public void setLonLft(double mLonLft) {
	  this.mLonLft = mLonLft;
  }

	/**
   * @return the mLonLft
   */
  public double getLonLft() {
	  return mLonLft;
  }

	/**
   * @param mLonRt the mLonRt to set
   */
  public void setLonRt(double mLonRt) {
	  this.mLonRt = mLonRt;
  }

	/**
   * @return the mLonRt
   */
  public double getLonRt() {
	  return mLonRt;
  }

	/**
   * @param mCenLat the mCenLat to set
   */
  public void setCenLat(double mCenLat) {
	  this.mCenLat = mCenLat;
  }

	/**
   * @return the mCenLat
   */
  public double getCenLat() {
	  return mCenLat;
  }

	/**
   * @param mConnectStns the mConnectStns to set
   */
  public void setConnectStns(boolean mConnectStns) {
	  this.mConnectStns = mConnectStns;
  }

	/**
   * @return the mConnectStns
   */
  public boolean isConnectStns() {
	  return mConnectStns;
  }

	/**
   * @param mConnectStnsAcrossSections the mConnectStnsAcrossSections to set
   */
  public void setConnectStnsAcrossSections(boolean mConnectStnsAcrossSections) {
	  this.mConnectStnsAcrossSections = mConnectStnsAcrossSections;
  }

	/**
   * @return the mConnectStnsAcrossSections
   */
  public boolean isConnectStnsAcrossSections() {
	  return mConnectStnsAcrossSections;
  }

	/**
   * @param mDrawGraticule the mDrawGraticule to set
   */
  public void setDrawGraticule(boolean mDrawGraticule) {
	  this.mDrawGraticule = mDrawGraticule;
  }

	/**
   * @return the mDrawGraticule
   */
  public boolean isDrawGraticule() {
	  return mDrawGraticule;
  }

	/**
   * @param mRetainProjAspect the mRetainProjAspect to set
   */
  public void setRetainProjAspect(boolean mRetainProjAspect) {
	  this.mRetainProjAspect = mRetainProjAspect;
  }

	/**
   * @return the mRetainProjAspect
   */
  public boolean isRetainProjAspect() {
	  return mRetainProjAspect;
  }

	/**
   * @param mAutoGraticule the mAutoGraticule to set
   */
  public void setAutoGraticule(boolean mAutoGraticule) {
	  this.mAutoGraticule = mAutoGraticule;
  }

	/**
   * @return the mAutoGraticule
   */
  public boolean isAutoGraticule() {
	  return mAutoGraticule;
  }

	/**
   * @param mDrawLegend the mDrawLegend to set
   */
  public void setDrawLegend(boolean mDrawLegend) {
	  this.mDrawLegend = mDrawLegend;
  }

	/**
   * @return the mDrawLegend
   */
  public boolean isDrawLegend() {
	  return mDrawLegend;
  }

	/**
   * @param mLatGratSpacing the mLatGratSpacing to set
   */
  public void setLatGratSpacing(double mLatGratSpacing) {
	  this.mLatGratSpacing = mLatGratSpacing;
  }

	/**
   * @return the mLatGratSpacing
   */
  public double getLatGratSpacing() {
	  return mLatGratSpacing;
  }

	/**
   * @param mLonGratSpacing the mLonGratSpacing to set
   */
  public void setLonGratSpacing(double mLonGratSpacing) {
	  this.mLonGratSpacing = mLonGratSpacing;
  }

	/**
   * @return the mLonGratSpacing
   */
  public double getLonGratSpacing() {
	  return mLonGratSpacing;
  }

	/**
   * @param mUMin the mUMin to set
   */
  public void setUMin(double mUMin) {
	  this.mUMin = mUMin;
  }

	/**
   * @return the mUMin
   */
  public double getUMin() {
	  return mUMin;
  }

	/**
   * @param mUMax the mUMax to set
   */
  public void setUMax(double mUMax) {
	  this.mUMax = mUMax;
  }

	/**
   * @return the mUMax
   */
  public double getUMax() {
	  return mUMax;
  }

	/**
   * @param mVMax the mVMax to set
   */
  public void setMax(double mVMax) {
	  this.mVMax = mVMax;
  }

	/**
   * @return the mVMax
   */
  public double getMax() {
	  return mVMax;
  }

	/**
   * @param mVMin the mVMin to set
   */
  public void setVMin(double mVMin) {
	  this.mVMin = mVMin;
  }

	/**
   * @return the mVMin
   */
  public double getVMin() {
	  return mVMin;
  }

	/**
   * @param mHScale the mHScale to set
   */
  public void setHScale(double mHScale) {
	  this.mHScale = mHScale;
  }

	/**
   * @return the mHScale
   */
  public double getHScale() {
	  return mHScale;
  }

	/**
   * @param mVScale the mVScale to set
   */
  public void setVScale(double mVScale) {
	  this.mVScale = mVScale;
  }

	/**
   * @return the mVScale
   */
  public double getVScale() {
	  return mVScale;
  }

	/**
   * @param h1 the h1 to set
   */
  public void setH1(double h1) {
	  this.h1 = h1;
  }

	/**
   * @return the h1
   */
  public double getH1() {
	  return h1;
  }

	/**
   * @param h2 the h2 to set
   */
  public void setH2(double h2) {
	  this.h2 = h2;
  }

	/**
   * @return the h2
   */
  public double getH2() {
	  return h2;
  }

	/**
   * @param h3 the h3 to set
   */
  public void setH3(double h3) {
	  this.h3 = h3;
  }

	/**
   * @return the h3
   */
  public double getH3() {
	  return h3;
  }

	/**
   * @param mHOffset the mHOffset to set
   */
  public void setHOffset(double mHOffset) {
	  this.mHOffset = mHOffset;
  }

	/**
   * @return the mHOffset
   */
  public double getHOffset() {
	  return mHOffset;
  }

	/**
   * @param mVOffset the mVOffset to set
   */
  public void setVOffset(double mVOffset) {
	  this.mVOffset = mVOffset;
  }

	/**
   * @return the mVOffset
   */
  public double getVOffset() {
	  return mVOffset;
  }

	/**
   * @param mMapHScale the mMapHScale to set
   */
  public void setMapHScale(double mMapHScale) {
	  this.mMapHScale = mMapHScale;
  }

	/**
   * @return the mMapHScale
   */
  public double getMapHScale() {
	  return mMapHScale;
  }

	/**
   * @param mMapVScale the mMapVScale to set
   */
  public void setMapVScale(double mMapVScale) {
	  this.mMapVScale = mMapVScale;
  }

	/**
   * @return the mMapVScale
   */
  public double getMapVScale() {
	  return mMapVScale;
  }

	/**
   * @param mBGColor the mBGColor to set
   */
  public void setBGColor(Color mBGColor) {
	  this.mBGColor = mBGColor;
  }

	/**
   * @return the mBGColor
   */
  public Color getBGColor() {
	  return mBGColor;
  }

	/**
   * @param mGratColor the mGratColor to set
   */
  public void setGratColor(Color mGratColor) {
	  this.mGratColor = mGratColor;
  }

	/**
   * @return the mGratColor
   */
  public Color getGratColor() {
	  return mGratColor;
  }

	/**
   * @param mCoastColor the mCoastColor to set
   */
  public void setCoastColor(Color mCoastColor) {
	  this.mCoastColor = mCoastColor;
  }

	/**
   * @return the mCoastColor
   */
  public Color getCoastColor() {
	  return mCoastColor;
  }

	/**
   * @param mSectionColor the mSectionColor to set
   */
  public void setSectionColor(Color mSectionColor) {
	  this.mSectionColor = mSectionColor;
  }

	/**
   * @return the mSectionColor
   */
  public Color getSectionColor() {
	  return mSectionColor;
  }

	/**
   * @param mMapName the mMapName to set
   */
  public void setMapName(String mMapName) {
	  this.mMapName = mMapName;
  }

	/**
   * @return the mMapName
   */
  public String getMapName() {
	  return mMapName;
  }

	/**
   * @param mPlotSectionLabels the mPlotSectionLabels to set
   */
  public void setPlotSectionLabels(boolean mPlotSectionLabels) {
	  this.mPlotSectionLabels = mPlotSectionLabels;
  }

	/**
   * @return the mPlotSectionLabels
   */
  public boolean isPlotSectionLabels() {
	  return mPlotSectionLabels;
  }

	/**
   * @param mPlotLabels the mPlotLabels to set
   */
  public void setPlotGratLabels(boolean b) {
	  this.mPlotGratLabels = b;
  }

	/**
   * @return the mPlotLabels
   */
  public boolean isPlotGratLabels() {
	  return mPlotGratLabels;
  }

	/**
   * @param mLabelColor the mLabelColor to set
   */
  public void setLabelColor(Color c) {
	  this.mLabelColor = c;
  }

	/**
   * @return the mLabelColor
   */
  public Color getLabelColor() {
	  return mLabelColor;
  }

	/**
   * @param mIsGlobe the mIsGlobe to set
   */
  public void setGlobe(boolean b) {
	  this.mGlobe = b;
  }

	/**
   * @return the mIsGlobe
   */
  public boolean isGlobe() {
	  return mGlobe;
  }

	/**
   * @param mPlotStnLabels the mLabelStnLocations to set
   */
  public void setPlotStnLabels(boolean b) {
	  this.mPlotStnLabels = b;
  }

	/**
   * @return the mLabelStnLocations
   */
  public boolean isPlotStnLabels() {
	  return mPlotStnLabels;
  }

	/**
   * @param mStnLabelAngle the mStnLabelAngle to set
   */
  public void setStnLabelAngle(double mStnLabelAngle) {
	  this.mStnLabelAngle = mStnLabelAngle;
  }

	/**
   * @return the mStnLabelAngle
   */
  public double getStnLabelAngle() {
	  return mStnLabelAngle;
  }

	/**
   * @param mStnLabelOffset the mStnLabelOffset to set
   */
  public void setStnLabelOffset(int mStnLabelOffset) {
	  this.mStnLabelOffset = mStnLabelOffset;
  }

	/**
   * @return the mStnLabelOffset
   */
  public int getStnLabelOffset() {
	  return mStnLabelOffset;
  }

	/**
   * @param mStnLabelPrec the mStnLabelPrec to set
   */
  public void setContourLabelPrec(int i) {
	  this.mContourLabelPrec = i;
  }

	/**
   * @return the mStnLabelPrec
   */
  public int getContourLabelPrec() {
	  return mContourLabelPrec;
  }

	/**
   * @param mIsCustomMap the mIsCustomMap to set
   */
  public void setCustomMap(boolean b) {
	  this.mCustomMap = b;
  }

	/**
   * @return the mIsCustomMap
   */
  public boolean isCustomMap() {
	  return mCustomMap;
  }

	/**
   * @param mContourOverlayMode the mContourOverlayMode to set
   */
  public void setContourOverlayMode(int com) {
	  this.mContourOverlayMode = com;
  }

	/**
   * @return the mContourOverlayMode
   */
  public int getContourOverlayMode() {
	  return mContourOverlayMode;
  }

	/**
   * @param mIsoContourStyle the mIsoContourStyle to set
   */
  public void setIsoContourStyle(int mIsoContourStyle) {
	  this.mIsoContourStyle = mIsoContourStyle;
  }

	/**
   * @return the mIsoContourStyle
   */
  public int getIsoContourStyle() {
	  return mIsoContourStyle;
  }

	/**
   * @param mIsoContourVarCode the mIsoContourVarCode to set
   */
  public void setIsoContourVarCode(int mIsoContourVarCode) {
	  this.mIsoContourVarCode = mIsoContourVarCode;
  }

	/**
   * @return the mIsoContourVarCode
   */
  public int getIsoContourVarCode() {
	  return mIsoContourVarCode;
  }

	/**
   * @param mIsoContourSurfVarCode the mIsoContourSurfVarCode to set
   */
  public void setIsoContourSurfVarCode(int mIsoContourSurfVarCode) {
	  this.mIsoContourSurfVarCode = mIsoContourSurfVarCode;
  }

	/**
   * @return the mIsoContourSurfVarCode
   */
  public int getIsoContourSurfVarCode() {
	  return mIsoContourSurfVarCode;
  }

	/**
   * @param mIsoContourSurface the mIsoContourSurface to set
   */
  public void setIsoContourSurface(NewInterpolationSurface mIsoContourSurface) {
	  this.mIsoContourSurface = mIsoContourSurface;
  }

	/**
   * @return the mIsoContourSurface
   */
  public NewInterpolationSurface getIsoContourSurface() {
	  return mIsoContourSurface;
  }

	/**
   * @param mContourIsoSurfaceValue the mContourIsoSurfaceValue to set
   */
  public void setContourIsoSurfaceValue(double mContourIsoSurfaceValue) {
	  this.mContourIsoSurfaceValue = mContourIsoSurfaceValue;
  }

	/**
   * @return the mContourIsoSurfaceValue
   */
  public double getContourIsoSurfaceValue() {
	  return mContourIsoSurfaceValue;
  }

	/**
   * @param mIsoContourReferenceLevel the mIsoContourReferenceLevel to set
   */
  public void setIsoContourReferenceLevel(double mIsoContourReferenceLevel) {
	  this.mIsoContourReferenceLevel = mIsoContourReferenceLevel;
  }

	/**
   * @return the mIsoContourReferenceLevel
   */
  public double getIsoContourReferenceLevel() {
	  return mIsoContourReferenceLevel;
  }

	/**
   * @param mIsoContourIsReferenced the mIsoContourIsReferenced to set
   */
  public void setIsoContourReferenced(boolean b) {
	  this.mIsoContourReferenced = b;
  }

	/**
   * @return the mIsoContourIsReferenced
   */
  public boolean isIsoContourReferenced() {
	  return mIsoContourReferenced;
  }

	/**
   * @param mIsoContourMinSurfaceValue the mIsoContourMinSurfaceValue to set
   */
  public void setIsoContourMinSurfaceValue(boolean mIsoContourMinSurfaceValue) {
	  this.mIsoContourMinSurfaceValue = mIsoContourMinSurfaceValue;
  }

	/**
   * @return the mIsoContourMinSurfaceValue
   */
  public boolean isIsoContourMinSurfaceValue() {
	  return mIsoContourMinSurfaceValue;
  }

	/**
   * @param mIsoContourMaxSurfaceValue the mIsoContourMaxSurfaceValue to set
   */
  public void setIsoContourMaxSurfaceValue(boolean mIsoContourMaxSurfaceValue) {
	  this.mIsoContourMaxSurfaceValue = mIsoContourMaxSurfaceValue;
  }

	/**
   * @return the mIsoContourMaxSurfaceValue
   */
  public boolean isIsoContourMaxSurfaceValue() {
	  return mIsoContourMaxSurfaceValue;
  }

	/**
   * @param mIsoOverlayContoursColorBar the mIsoOverlayContoursColorBar to set
   */
  public void setOverlayContoursColorBar(NewColorBar cb) {
	  this.mOverlayContoursColorBar = cb;
  }

	/**
   * @return the mIsoOverlayContoursColorBar
   */
  public NewColorBar getOverlayContoursColorBar() {
	  return mOverlayContoursColorBar;
  }

	/**
   * @param mIsoContourBottomUpSearch the mIsoContourBottomUpSearch to set
   */
  public void setIsoContourBottomUpSearch(boolean mIsoContourBottomUpSearch) {
	  this.mIsoContourBottomUpSearch = mIsoContourBottomUpSearch;
  }

	/**
   * @return the mIsoContourBottomUpSearch
   */
  public boolean isIsoContourBottomUpSearch() {
	  return mIsoContourBottomUpSearch;
  }

	/**
   * @param mIsoContourLocalInterpolation the mIsoContourLocalInterpolation to set
   */
  public void setIsoContourLocalInterpolation(boolean mIsoContourLocalInterpolation) {
	  this.mIsoContourLocalInterpolation = mIsoContourLocalInterpolation;
  }

	/**
   * @return the mIsoContourLocalInterpolation
   */
  public boolean isIsoContourLocalInterpolation() {
	  return mIsoContourLocalInterpolation;
  }

	/**
   * @param mIsoContourMaxInterpDistance the mIsoContourMaxInterpDistance to set
   */
  public void setIsoContourMaxInterpDistance(int mIsoContourMaxInterpDistance) {
	  this.mIsoContourMaxInterpDistance = mIsoContourMaxInterpDistance;
  }

	/**
   * @return the mIsoContourMaxInterpDistance
   */
  public int getIsoContourMaxInterpDistance() {
	  return mIsoContourMaxInterpDistance;
  }

	/**
   * @param mIsoContourAutoscaledColorCB the mIsIsoContourAutoscaledColorCB to set
   */
  public void setIsoContourAutoscaledColorCB(boolean b) {
	  this.mIsoContourAutoscaledColorCB = b;
  }

	/**
   * @return the mIsIsoContourAutoscaledColorCB
   */
  public boolean isIsoContourAutoscaledColorCB() {
	  return mIsoContourAutoscaledColorCB;
  }

	/**
   * @param mIsoContourColor the mIsoContourColor to set
   */
  public void setIsoContourColor(Color mIsoContourColor) {
	  this.mIsoContourColor = mIsoContourColor;
  }

	/**
   * @return the mIsoContourColor
   */
  public Color getIsoContourColor() {
	  return mIsoContourColor;
  }

	/**
   * @param mIsoContMeanCastStnList the mIsoContMeanCastStnList to set
   */
  public void setIsoContMeanCastStnList(boolean[] mIsoContMeanCastStnList) {
	  this.mIsoContMeanCastStnList = mIsoContMeanCastStnList;
  }

	/**
   * @return the mIsoContMeanCastStnList
   */
  public boolean[] getIsoContMeanCastStnList() {
	  return mIsoContMeanCastStnList;
  }

	/**
   * @param mIsoContIsResidualInterp the mIsoContIsResidualInterp to set
   */
  public void setIsoContIsResidualInterp(boolean mIsoContIsResidualInterp) {
	  this.mIsoContIsResidualInterp = mIsoContIsResidualInterp;
  }

	/**
   * @return the mIsoContIsResidualInterp
   */
  public boolean isIsoContIsResidualInterp() {
	  return mIsoContIsResidualInterp;
  }

	/**
   * @param mIsoContRefLevel the mIsoContRefLevel to set
   */
  public void setIsoContRefLevel(double mIsoContRefLevel) {
	  this.mIsoContRefLevel = mIsoContRefLevel;
  }

	/**
   * @return the mIsoContRefLevel
   */
  public double getIsoContRefLevel() {
	  return mIsoContRefLevel;
  }

	/**
   * @param mStnContourStyle the mStnContourStyle to set
   */
  public void setStnContourStyle(int mStnContourStyle) {
	  this.mStnContourStyle = mStnContourStyle;
  }

	/**
   * @return the mStnContourStyle
   */
  public int getStnContourStyle() {
	  return mStnContourStyle;
  }


	/**
   * @param mIsStnCalcContourAutoscaledColorCB the mIsStnCalcContourAutoscaledColorCB to set
   */
  public void setStnCalcContourAutoscaledColorCB(boolean b) {
	  this.mStnCalcContourAutoscaledColorCB = b;
  }

	/**
   * @return the mIsStnCalcContourAutoscaledColorCB
   */
  public boolean isStnCalcContourAutoscaledColorCB() {
	  return mStnCalcContourAutoscaledColorCB;
  }

	/**
   * @param mStnVarCalcContourColor the mStnVarCalcContourColor to set
   */
  public void setStnVarCalcContourColor(Color mStnVarCalcContourColor) {
	  this.mStnVarCalcContourColor = mStnVarCalcContourColor;
  }

	/**
   * @return the mStnVarCalcContourColor
   */
  public Color getStnVarCalcContourColor() {
	  return mStnVarCalcContourColor;
  }

	/**
   * @param mPlotEveryNthContour the mPlotEveryNthContour to set
   */
  public void setPlotEveryNthContour(int mPlotEveryNthContour) {
	  this.mPlotEveryNthContour = mPlotEveryNthContour;
  }

	/**
   * @return the mPlotEveryNthContour
   */
  public int getPlotEveryNthContour() {
	  return mPlotEveryNthContour;
  }

	/**
   * @param mNX the mNX to set
   */
  public void setNX(int mNX) {
	  this.mNX = mNX;
  }

	/**
   * @return the mNX
   */
  public int getNX() {
	  return mNX;
  }

	/**
   * @param mNY the mNY to set
   */
  public void setNY(int mNY) {
	  this.mNY = mNY;
  }

	/**
   * @return the mNY
   */
  public int getNY() {
	  return mNY;
  }

	/**
   * @param mCAY the mCAY to set
   */
  public void setCAY(double mCAY) {
	  this.mCAY = mCAY;
  }

	/**
   * @return the mCAY
   */
  public double getCAY() {
	  return mCAY;
  }

	/**
   * @param mNRng the mNRng to set
   */
  public void setNRng(int mNRng) {
	  this.mNRng = mNRng;
  }

	/**
   * @return the mNRng
   */
  public int getNRng() {
	  return mNRng;
  }

	/**
   * @param mMaskCoast the mMaskCoast to set
   */
  public void setMaskCoast(boolean mMaskCoast) {
	  this.mMaskCoast = mMaskCoast;
  }

	/**
   * @return the mMaskCoast
   */
  public boolean isMaskCoast() {
	  return mMaskCoast;
  }

	/**
   * @param mEtopoFiles the mEtopoFiles to set
   */
  public void setEtopoFiles(String[] sa) {
	  this.mEtopoFiles = sa;
  }

	/**
   * @return the mEtopoFiles
   */
  public String[] getEtopoFiles() {
	  return mEtopoFiles;
  }

	/**
   * @return the mEtopoFiles
   */
  public String getEtopoFile(int i) {
	  return mEtopoFiles[i];
  }

	/**
   * @return the mEtopoFiles
   */
  public void setEtopoFile(String s) {
	  mEtopoFiles[mNumEtopoFiles++] = s;
  }
  
  public void resetEtopoFiles() {
  	for (int i=0; i<10; i++) {
  		mEtopoFiles[i] = null;
  	}
  	mNumEtopoFiles = 0;
  }

	/**
   * @param mNumEtopoFiles the mNumEtopoFiles to set
   */
  public void setNumEtopoFiles(int i) {
	  this.mNumEtopoFiles = i;
  }

	/**
   * @return the mNumEtopoFiles
   */
  public int getNumEtopoFiles() {
	  return mNumEtopoFiles;
  }

	/**
   * @param mColorFill the mColorFill to set
   */
  public void setColorFill(boolean mColorFill) {
	  this.mColorFill = mColorFill;
  }

	/**
   * @return the mColorFill
   */
  public boolean isColorFill() {
	  return mColorFill;
  }

	/**
   * @param mNumIsobaths the mNumIsobaths to set
   */
  public void setNumIsobaths(int mNumIsobaths) {
	  this.mNumIsobaths = mNumIsobaths;
  }

	/**
   * @return the mNumIsobaths
   */
  public int getNumIsobaths() {
	  return mNumIsobaths;
  }

	/**
   * @param mIsobathValues the mIsobathValues to set
   */
  public void setIsobathValues(double[] mIsobathValues) {
	  this.mIsobathValues = mIsobathValues;
  }

	/**
   * @return the mIsobathValues
   */
  public double[] getIsobathValues() {
	  return mIsobathValues;
  }

	/**
   * @return the mIsobathValues
   */
  public double getValue(int i) {
	  return mIsobathValues[i];
  }

	/**
   * @return the mIsobathValues
   */
  public void setValue(int i, double val) {
	  mIsobathValues[i] = val;
  }

	/**
   * @return the mIsobathValues
   */
  public void setDescrip(int i, String s) {
	  mIsobathDescrips[i] = s;
  }

	/**
   * @return the mIsobathValues
   */
  public void setPath(int i, String s) {
	  mIsobathPaths[i] = s;
  }

	/**
   * @param mIsobathColors the mIsobathColors to set
   */
  public void setIsobathColors(Color[] mIsobathColors) {
	  this.mIsobathColors = mIsobathColors;
  }

	/**
   * @return the mIsobathColors
   */
  public Color[] getIsobathColors() {
	  return mIsobathColors;
  }

	/**
   * @return the mIsobathColors
   */
  public Color getColor(int i) {
	  return mIsobathColors[i];
  }

	/**
   * @return the mIsobathColors
   */
  public void setColor(int i, Color c) {
	  mIsobathColors[i] = c;
  }

	/**
   * @param mCustCoastPath the mCustCoastPath to set
   */
  public void setCustCoastPath(String mCustCoastPath) {
	  this.mCustCoastPath = mCustCoastPath;
  }

	/**
   * @return the mCustCoastPath
   */
  public String getCustCoastPath() {
	  return mCustCoastPath;
  }

	/**
   * @param mCustCoastDescrip the mCustCoastDescrip to set
   */
  public void setCustCoastDescrip(String mCustCoastDescrip) {
	  this.mCustCoastDescrip = mCustCoastDescrip;
  }

	/**
   * @return the mCustCoastDescrip
   */
  public String getCustCoastDescrip() {
	  return mCustCoastDescrip;
  }

	/**
   * @param mBathyColorBar the mBathyColorBar to set
   */
  public void setBathyColorBar(NewColorBar mBathyColorBar) {
	  this.mBathyColorBar = mBathyColorBar;
  }

	/**
   * @return the mBathyColorBar
   */
  public NewColorBar getBathyColorBar() {
	  return mBathyColorBar;
  }
  
  /**
   * @return the mBathyColorBar
   */
  public NewColorBar getMaskColorBar() {
  	if (mMaskColorBar == null) {
  		try {
  			mMaskColorBar = JOAFormulas.getColorBar("ROSE-black_Mask_cbr.xml");
  		}
  		catch (Exception ex) {
  			// silent--no substitute for this cb
  		}
  	}
  	return mMaskColorBar;
  }

	/**
   * @param mVMax the mVMax to set
   */
  public void setVMax(double mVMax) {
	  this.mVMax = mVMax;
  }

	/**
   * @return the mVMax
   */
  public double getVMax() {
	  return mVMax;
  }

	/**
   * @param mIsobathPaths the mIsobathPaths to set
   */
  public void setIsobathPaths(String[] mIsobathPaths) {
	  this.mIsobathPaths = mIsobathPaths;
  }

	/**
   * @return the mIsobathPaths
   */
  public String[] getIsobathPaths() {
	  return mIsobathPaths;
  }

	/**
   * @return the mIsobathPaths
   */
  public String getPath(int i) {
	  return mIsobathPaths[i];
  }

	/**
   * @param mIsobathDescrips the mIsobathDescrips to set
   */
  public void setIsobathDescrips(String[] mIsobathDescrips) {
	  this.mIsobathDescrips = mIsobathDescrips;
  }

	/**
   * @return the mIsobathDescrips
   */
  public String[] getIsobathDescrips() {
	  return mIsobathDescrips;
  }

	/**
   * @return the mIsobathDescrips
   */
  public String getDescrip(int i) {
	  return mIsobathDescrips[i];
  }

	/**
   * @param mStnColorMode the mStnColorMode to set
   */
  public void setStnColorMode(int mStnColorMode) {
	  this.mStnColorMode = mStnColorMode;
  }

	/**
   * @return the mStnColorMode
   */
  public int getStnColorMode() {
	  return mStnColorMode;
  }

	/**
   * @param mStnColorByIsoVarCode the mStnColorByIsoVarCode to set
   */
  public void setStnColorByIsoVarCode(int mStnColorByIsoVarCode) {
	  this.mStnColorByIsoVarCode = mStnColorByIsoVarCode;
  }

	/**
   * @return the mStnColorByIsoVarCode
   */
  public int getStnColorByIsoVarCode() {
	  return mStnColorByIsoVarCode;
  }

	/**
   * @param mStnColorByIsoSurfVarCode the mStnColorByIsoSurfVarCode to set
   */
  public void setStnColorByIsoSurfVarCode(int mStnColorByIsoSurfVarCode) {
	  this.mStnColorByIsoSurfVarCode = mStnColorByIsoSurfVarCode;
  }

	/**
   * @return the mStnColorByIsoSurfVarCode
   */
  public int getStnColorByIsoSurfVarCode() {
	  return mStnColorByIsoSurfVarCode;
  }

	/**
   * @param mStnColorByIsoSurface the mStnColorByIsoSurface to set
   */
  public void setStnColorByIsoSurface(NewInterpolationSurface mStnColorByIsoSurface) {
	  this.mStnColorByIsoSurface = mStnColorByIsoSurface;
  }

	/**
   * @return the mStnColorByIsoSurface
   */
  public NewInterpolationSurface getStnColorByIsoSurface() {
	  return mStnColorByIsoSurface;
  }

	/**
   * @param mStnColorByIsoIsoSurfaceValue the mStnColorByIsoIsoSurfaceValue to set
   */
  public void setStnColorByIsoIsoSurfaceValue(double d) {
	  this.mStnColorByIsoIsoSurfaceValue = d;
  }

	/**
   * @return the mStnColorByIsoIsoSurfaceValue
   */
  public double getStnColorByIsoIsoSurfaceValue() {
	  return mStnColorByIsoIsoSurfaceValue;
  }

	/**
   * @param mStnColorByIsoReferenceLevel the mStnColorByIsoReferenceLevel to set
   */
  public void setStnColorByIsoReferenceLevel(double d) {
	  this.mStnColorByIsoReferenceLevel = d;
  }

	/**
   * @return the mStnColorByIsoReferenceLevel
   */
  public double getStnColorByIsoReferenceLevel() {
	  return mStnColorByIsoReferenceLevel;
  }

	/**
   * @param mStnColorByIsoIsReferenced the mStnColorByIsoIsReferenced to set
   */
  public void setStnColorByIsoIsReferenced(boolean b) {
	  this.mStnColorByIsoIsReferenced = b;
  }

	/**
   * @return the mStnColorByIsoIsReferenced
   */
  public boolean isStnColorByIsoIsReferenced() {
	  return mStnColorByIsoIsReferenced;
  }

	/**
   * @param mStnColorByIsoMinIsoSurfaceValue the mStnColorByIsoMinIsoSurfaceValue to set
   */
  public void setStnColorByIsoMinIsoSurfaceValue(boolean mStnColorByIsoMinIsoSurfaceValue) {
	  this.mStnColorByIsoMinIsoSurfaceValue = mStnColorByIsoMinIsoSurfaceValue;
  }

	/**
   * @return the mStnColorByIsoMinIsoSurfaceValue
   */
  public boolean isStnColorByIsoMinIsoSurfaceValue() {
	  return mStnColorByIsoMinIsoSurfaceValue;
  }

	/**
   * @param mStnColorByIsoMaxIsoSurfaceValue the mStnColorByIsoMaxIsoSurfaceValue to set
   */
  public void setStnColorByIsoMaxIsoSurfaceValue(boolean mStnColorByIsoMaxIsoSurfaceValue) {
	  this.mStnColorByIsoMaxIsoSurfaceValue = mStnColorByIsoMaxIsoSurfaceValue;
  }

	/**
   * @return the mStnColorByIsoMaxIsoSurfaceValue
   */
  public boolean isStnColorByIsoMaxIsoSurfaceValue() {
	  return mStnColorByIsoMaxIsoSurfaceValue;
  }

	/**
   * @param mStnColorByIsoStnColorsColorBar the mStnColorByIsoStnColorsColorBar to set
   */
  public void setStnColorColorBar(NewColorBar ncb) {
	  this.mStnColorColorBar = ncb;
  }

	/**
   * @return the mStnColorByIsoStnColorsColorBar
   */
  public NewColorBar getStnColorColorBar() {
	  return mStnColorColorBar;
  }

	/**
   * @param mStnColorByIsoBottomUpSearch the mStnColorByIsoBottomUpSearch to set
   */
  public void setStnColorByIsoBottomUpSearch(boolean mStnColorByIsoBottomUpSearch) {
	  this.mStnColorByIsoBottomUpSearch = mStnColorByIsoBottomUpSearch;
  }

	/**
   * @return the mStnColorByIsoBottomUpSearch
   */
  public boolean isStnColorByIsoBottomUpSearch() {
	  return mStnColorByIsoBottomUpSearch;
  }

	/**
   * @param mStnColorByIsoLocalInterpolation the mStnColorByIsoLocalInterpolation to set
   */
  public void setStnColorByIsoLocalInterpolation(boolean mStnColorByIsoLocalInterpolation) {
	  this.mStnColorByIsoLocalInterpolation = mStnColorByIsoLocalInterpolation;
  }

	/**
   * @return the mStnColorByIsoLocalInterpolation
   */
  public boolean isStnColorByIsoLocalInterpolation() {
	  return mStnColorByIsoLocalInterpolation;
  }

	/**
   * @param mStnColorByIsoMaxInterpDistance the mStnColorByIsoMaxInterpDistance to set
   */
  public void setStnColorByIsoMaxInterpDistance(int mStnColorByIsoMaxInterpDistance) {
	  this.mStnColorByIsoMaxInterpDistance = mStnColorByIsoMaxInterpDistance;
  }

	/**
   * @return the mStnColorByIsoMaxInterpDistance
   */
  public int getStnColorByIsoMaxInterpDistance() {
	  return mStnColorByIsoMaxInterpDistance;
  }

	/**
   * @param mStnColorByIsoMeanCastStnList the mStnColorByIsoMeanCastStnList to set
   */
  public void setStnColorByIsoMeanCastStnList(boolean[] mStnColorByIsoMeanCastStnList) {
	  this.mStnColorByIsoMeanCastStnList = mStnColorByIsoMeanCastStnList;
  }

	/**
   * @return the mStnColorByIsoMeanCastStnList
   */
  public boolean[] getStnColorByIsoMeanCastStnList() {
	  return mStnColorByIsoMeanCastStnList;
  }
  
  public boolean isStnColorByIsoMeanCastStn(int i) {
  	return mStnColorByIsoMeanCastStnList[i];
  }
  
  public void setStnColorByIsoMeanCastStn(int i, boolean b) {
  	mStnColorByIsoMeanCastStnList[i] = b;
  }
  
  public boolean isIsoContByIsoMeanCastStn(int i) {
  	return mIsoContMeanCastStnList[i];
  }
  
  public void setIsoContByIsoMeanCastStn(int i, boolean b) {
  	mIsoContMeanCastStnList[i] = b;
  }

	/**
   * @param mStnColorByIsoIsResidualInterp the mStnColorByIsoIsResidualInterp to set
   */
  public void setStnColorByIsoIsResidualInterp(boolean mStnColorByIsoIsResidualInterp) {
	  this.mStnColorByIsoIsResidualInterp = mStnColorByIsoIsResidualInterp;
  }

	/**
   * @return the mStnColorByIsoIsResidualInterp
   */
  public boolean isStnColorByIsoIsResidualInterp() {
	  return mStnColorByIsoIsResidualInterp;
  }

	/**
   * @param mStnColorByStnValVarCode the mStnColorByStnValVarCode to set
   */
  public void setStnColorByStnValVarCode(int i) {
	  this.mStnColorByStnValVarCode = i;
  }

	/**
   * @return the mStnColorByStnValVarCode
   */
  public int getStnColorByStnValVarCode() {
	  return mStnColorByStnValVarCode;
  }

	/**
   * @param mStnColorByStnValAutoscaledColorBar the mStnColorByStnValAutoscaledColorBar to set
   */
  public void setStnColorByStnValAutoscaledColorBar(boolean b) {
	  this.mStnColorByStnValAutoscaledColorBar = b;
  }

	/**
   * @return the mStnColorByStnValAutoscaledColorBar
   */
  public boolean isStnColorByStnValAutoscaledColorBar() {
	  return mStnColorByStnValAutoscaledColorBar;
  }

	/**
   * @param mStnCalcContourVarCode the mStnCalcContourVarCode to set
   */
  public void setStnCalcContourVarCode(int mStnCalcContourVarCode) {
	  this.mStnCalcContourVarCode = mStnCalcContourVarCode;
  }

	/**
   * @return the mStnCalcContourVarCode
   */
  public int getStnCalcContourVarCode() {
	  return mStnCalcContourVarCode;
  }

	/**
   * @param mStnColorByIsoAutoscaledColorCB the mStnColorByIsoAutoscaledColorCB to set
   */
  public void setStnColorByIsoAutoscaledColorCB(boolean mStnColorByIsoAutoscaledColorCB) {
	  this.mStnColorByIsoAutoscaledColorCB = mStnColorByIsoAutoscaledColorCB;
  }

	/**
   * @return the mStnColorByIsoAutoscaledColorCB
   */
  public boolean isStnColorByIsoAutoscaledColorCB() {
	  return mStnColorByIsoAutoscaledColorCB;
  }

	/**
   * @param mPlotStnSymbols the mPlotStnSymbols to set
   */
  public void setPlotStnSymbols(boolean mPlotStnSymbols) {
	  this.mPlotStnSymbols = mPlotStnSymbols;
  }

	/**
   * @return the mPlotStnSymbols
   */
  public boolean isPlotStnSymbols() {
	  return mPlotStnSymbols;
  }

	/**
   * @param mIsoFilledContours the mIsoFilledContours to set
   */
  public void setFilledIsoContours(boolean b) {
	  this.mFilledIsoContours = b;
  }

	/**
   * @return the mIsoFilledContours
   */
  public boolean isFilledIsoContours() {
	  return mFilledIsoContours;
  }

	/**
   * @param mIsoFilledContours the mIsoFilledContours to set
   */
  public void setFilledStnContours(boolean b) {
	  this.mFilledStnContours = b;
  }

	/**
   * @return the mIsoFilledContours
   */
  public boolean isFilledStnContours() {
	  return mFilledStnContours;
  }
}

/*
 * $Id: XYPlotSpecification.java,v 1.5 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import java.awt.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.utility.*;
import org.w3c.dom.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javaoceanatlas.resources.*;

public class LinePlotSpecification implements PlotSpecification {
	protected double mWinXPlotMax;
	protected double mWinYPlotMax;
	protected double mWinXPlotMin;
	protected double mWinYPlotMin;
	protected boolean mLineDrawn;
	protected int mSymbolSize = 5;
	protected int mSymbol = 2;
	protected int mXVarCode;
	protected int mYVarCode;
	protected boolean mPlotAxes = true, mXGrid = false, mYGrid = false;
	protected double mYInc;
	protected double mXInc;
	protected int mXTics;
	protected int mYTics;
	protected Vector<FileViewer> mFileViewer = new Vector<FileViewer>();
	protected String mWinTitle = null;
	protected boolean mIncludeObsPanel;
	protected Color mFG, mBG;
	protected int mWidth;
	protected int mHeight;
	protected boolean mReverseY;
	protected boolean mPlotIsopycnals;
	protected double mRefPress;
	protected boolean mCanPlotIsoPycnals;
	protected int mSaltAxis = 0;
	protected boolean mPlotOnlyCurrStn = false;
	protected boolean mAccumulateStns = false;
	private String mOverrideLabel = null;
	private boolean mIgnoreMissingObs = true;
	private String mStnCycleColorPalette = null;
	private int mLineWidth = 2;

	public LinePlotSpecification() {
	}

	public LinePlotSpecification(LinePlotSpecification inSpec) {
		mWinXPlotMax = inSpec.mWinXPlotMax;
		mWinXPlotMin = inSpec.mWinXPlotMin;
		mXVarCode = inSpec.mXVarCode;
		mXInc = inSpec.mXInc;
		mXTics = inSpec.mXTics;
		mSymbolSize = inSpec.mSymbolSize;
		mSymbol = inSpec.mSymbol;

		mWinYPlotMax = inSpec.mWinYPlotMax;
		mWinYPlotMin = inSpec.mWinYPlotMin;
		mYTics = inSpec.mYTics;

		mLineDrawn = inSpec.mLineDrawn;
		mYVarCode = inSpec.mYVarCode;
		mPlotAxes = inSpec.mPlotAxes;
		mXGrid = inSpec.mXGrid;
		mYGrid = inSpec.mYGrid;
		mYInc = inSpec.mYInc;
		mFileViewer = inSpec.mFileViewer;
		mWinTitle = inSpec.mWinTitle;
		mIncludeObsPanel = inSpec.mIncludeObsPanel;
		mFG = inSpec.mFG;
		mBG = inSpec.mBG;
		mWidth = inSpec.mWidth;
		mHeight = inSpec.mHeight;
		mReverseY = inSpec.mReverseY;
		mPlotIsopycnals = inSpec.mPlotIsopycnals;
		mCanPlotIsoPycnals = inSpec.mCanPlotIsoPycnals;
		mPlotOnlyCurrStn = inSpec.mPlotOnlyCurrStn;
		mAccumulateStns = inSpec.mAccumulateStns;
		mRefPress = inSpec.mRefPress;
		mSaltAxis = inSpec.mSaltAxis;

		mOverrideLabel = inSpec.getOverrideLabel();
		mIgnoreMissingObs = inSpec.isIgnoreMissingObs();
		mStnCycleColorPalette = inSpec.getStnCycleColorPalette();
		mLineWidth = inSpec.getLineWidth();
	}
	
	public String exportJSON(File file) {    
		return null;
	}

	public void saveAsXML(FileViewer fv, Document doc, Element root) {
		// in item is the tag for proppropwindow
		// attributes
		Element item = doc.createElement("proppropplot");
		item.setAttribute("title", mWinTitle);
		item.setAttribute("linedrawn", String.valueOf(mLineDrawn));
		item.setAttribute("plotaxes", String.valueOf(mPlotAxes));
		item.setAttribute("xgrid", String.valueOf(mXGrid));
		item.setAttribute("ygrid", String.valueOf(mYGrid));
		item.setAttribute("includeobspanel", String.valueOf(mIncludeObsPanel));
		item.setAttribute("reversey", String.valueOf(mReverseY));
		item.setAttribute("plotisopycnals", String.valueOf(mPlotIsopycnals));
		item.setAttribute("plotonlycurrstn", String.valueOf(mPlotOnlyCurrStn));
		item.setAttribute("accumulatestns", String.valueOf(mAccumulateStns));
		item.setAttribute("refpres", String.valueOf(mRefPress));
		item.setAttribute("saltaxis", String.valueOf(mSaltAxis));
		item.setAttribute("height", String.valueOf(mHeight));
		item.setAttribute("width", String.valueOf(mWidth));

		Element cItem = doc.createElement("forgroundcolor");
		cItem.setAttribute("red", String.valueOf(mFG.getRed()));
		cItem.setAttribute("green", String.valueOf(mFG.getGreen()));
		cItem.setAttribute("blue", String.valueOf(mFG.getBlue()));
		item.appendChild(cItem);
		cItem = doc.createElement("backgroundcolor");
		cItem.setAttribute("red", String.valueOf(mBG.getRed()));
		cItem.setAttribute("green", String.valueOf(mBG.getGreen()));
		cItem.setAttribute("blue", String.valueOf(mBG.getBlue()));
		item.appendChild(cItem);

		// x axis
		Element xaxitem = doc.createElement("xaxis");
		xaxitem.setAttribute("param", String.valueOf(fv.mAllProperties[mXVarCode].getVarLabel()));
		xaxitem.setAttribute("min", String.valueOf(mWinXPlotMin));
		xaxitem.setAttribute("max", String.valueOf(mWinXPlotMax));
		xaxitem.setAttribute("inc", String.valueOf(mXInc));
		xaxitem.setAttribute("tics", String.valueOf(mXTics));
		cItem = doc.createElement("connectstncolor");
		xaxitem.appendChild(cItem);
		xaxitem.setAttribute("symbolsize", String.valueOf(mSymbolSize));
		xaxitem.setAttribute("symbol", String.valueOf(mSymbol));
		item.appendChild(xaxitem);

		// y axis
		Element yaxitem = doc.createElement("yaxis");
		yaxitem.setAttribute("param", String.valueOf(fv.mAllProperties[mYVarCode].getVarLabel()));
		yaxitem.setAttribute("min", String.valueOf(mWinYPlotMin));
		yaxitem.setAttribute("max", String.valueOf(mWinYPlotMax));
		yaxitem.setAttribute("inc", String.valueOf(mYInc));
		yaxitem.setAttribute("tics", String.valueOf(mYTics));
		item.appendChild(yaxitem);
		root.appendChild(item);
	}

	public double getWinXPlotMax() {
		return mWinXPlotMax;
	}

	public void setWinXPlotMax(double d) {
		mWinXPlotMax = d;
	}

	public double getWinYPlotMax() {
		return mWinYPlotMax;
	}

	public void setWinYPlotMax(double d) {
		mWinYPlotMax = d;
	}

	public double getWinXPlotMin() {
		return mWinXPlotMin;
	}

	public void setWinXPlotMin(double d) {
		mWinXPlotMin = d;
	}

	public double getWinYPlotMin() {
		return mWinYPlotMin;
	}

	public void setWinYPlotMin(double d) {
		mWinYPlotMin = d;
	}

	public int getSymbolSize() {
		return mSymbolSize;
	}

	public void setSymbolSize(int i) {
		mSymbolSize = i;
	}

	public int getSymbol() {
		return mSymbol;
	}

	public void setSymbol(int i) {
		mSymbol = i;
	}

	public int getXVarCode() {
		return mXVarCode;
	}

	public void setXVarCode(int i) {
		mXVarCode = i;
	}

	public int getYVarCode() {
		return mYVarCode;
	}

	public void setYVarCode(int i) {
		mYVarCode = i;
	}

	public boolean isYGrid() {
		return mYGrid;
	}

	public void setYGrid(boolean b) {
		mYGrid = b;
	}

	public boolean isXGrid() {
		return mXGrid;
	}

	public void setXGrid(boolean b) {
		mXGrid = b;
	}

	public int getYTics() {
		return mYTics;
	}

	public void setYTics(int i) {
		mYTics = i;
	}

	public int getXTics(int a) {
		return mXTics;
	}

	public void setXTics(int i) {
		mXTics = i;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int i) {
		mWidth = i;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int i) {
		mHeight = i;
	}

	public double getYInc() {
		return mYInc;
	}

	public void setYInc(double d) {
		mYInc = d;
	}

	public double getXInc(int a) {
		return mXInc;
	}

	public void setXInc(double d) {
		mXInc = d;
	}

	public String getWinTitle() {
		return mWinTitle;
	}

	public void setWinTitle(String s) {
		mWinTitle = new String(s);
	}

	public Color getFGColor() {
		return mFG;
	}

	public void setFGColor(Color c) {
		mFG = new Color(c.getRed(), c.getGreen(), c.getBlue());
	}

	public Color getBGColor() {
		return mBG;
	}

	public void setBGColor(Color c) {
		mBG = new Color(c.getRed(), c.getGreen(), c.getBlue());
	}

	public boolean isIncludeObsPanel() {
		return mIncludeObsPanel;
	}

	public void setIncludeObsPanel(boolean b) {
		mIncludeObsPanel = b;
	}

	public boolean isPlotAxes() {
		return mPlotAxes;
	}

	public void setPlotAxes(boolean b) {
		mPlotAxes = b;
	}

	public int isSaltAxis() {
		return mSaltAxis;
	}

	public void setSaltAxis(int i) {
		mSaltAxis = i;
	}

	public boolean isPlotIsopycnals() {
		return mPlotIsopycnals;
	}

	public void setPlotIsopycnals(boolean b) {
		mPlotIsopycnals = b;
	}

	public boolean isCanPlotIsoPycnals() {
		return mCanPlotIsoPycnals;
	}

	public void setCanPlotIsoPycnals(boolean b) {
		mCanPlotIsoPycnals = b;
	}

	public boolean isPlotOnlyCurrStn() {
		return mPlotOnlyCurrStn;
	}

	public void setPlotOnlyCurrStn(boolean b) {
		mPlotOnlyCurrStn = b;
	}

	public boolean isAccumulateStns() {
		return mAccumulateStns;
	}

	public void setAccumulateStns(boolean b) {
		mAccumulateStns = b;
	}

	public double getRefPress() {
		return mRefPress;
	}

	public void setRefPress(double d) {
		mRefPress = d;
	}

	public Vector<FileViewer> getFileViewer() {
		return mFileViewer;
	}

	public void setFileViewer(FileViewer fv) {
		mFileViewer.add(fv);
	}

	public double getXMaxValue() {
		return mWinXPlotMax;
	}

	public double getXMinValue() {
		return mWinXPlotMin;
	}

	public double getXInc() {
		return mXInc;
	}

	public int getXTics() {
		return mXTics;
	}

	public boolean isReverseY() {
		return mReverseY;
	}

	public void setReverseY(boolean b) {
		mReverseY = b;
	}

	public void writeToLog(String preamble) throws IOException {
		for (FileViewer fv : mFileViewer) {
			try {
				JOAConstants.LogFileStream.writeBytes(preamble + "\n");
				JOAConstants.LogFileStream.writeBytes("\t" + "XY Plot: " + " Y = " + fv.mAllProperties[mYVarCode].getVarLabel()
				    + ", Y Min = " + JOAFormulas.formatDouble(mWinYPlotMin, 3, false) + ", Y Max = "
				    + JOAFormulas.formatDouble(mWinYPlotMax, 3, false));

				JOAConstants.LogFileStream.writeBytes(", X Axis " + " = " + fv.mAllProperties[mXVarCode].getVarLabel()
				    + ", X Min = " + JOAFormulas.formatDouble(mWinXPlotMin, 3, false) + ", X Max = "
				    + JOAFormulas.formatDouble(mWinXPlotMax, 3, false));
				JOAConstants.LogFileStream.writeBytes(", Rev. Y = " + String.valueOf(mReverseY));

				if (mPlotIsopycnals) {
					JOAConstants.LogFileStream.writeBytes(", Isopyc. ref. press = "
					    + JOAFormulas.formatDouble(mRefPress, 3, false));
				}
				JOAConstants.LogFileStream.writeBytes("\n");
			}
			catch (IOException ex) {
				throw ex;
			}
		}
	}

	public void setOverrideLabel(String mOverrideLabel) {
		this.mOverrideLabel = mOverrideLabel;
	}

	public String getOverrideLabel() {
		return mOverrideLabel;
	}

	/**
	 * @param mIgnoreMissingObs
	 *          the mIgnoreMissingObs to set
	 */
	public void setIgnoreMissingObs(boolean mIgnoreMissingObs) {
		this.mIgnoreMissingObs = mIgnoreMissingObs;
	}

	/**
	 * @return the mIgnoreMissingObs
	 */
	public boolean isIgnoreMissingObs() {
		return mIgnoreMissingObs;
	}

	/**
	 * @param mStnCycleColorBar
	 *          the mStnCycleColorBar to set
	 */
	public void setStnCycleColorPalette(String cp) {
		this.mStnCycleColorPalette = cp;
	}

	/**
	 * @return the mStnCycleColorBar
	 */
	public String getStnCycleColorPalette() {
		return mStnCycleColorPalette;
	}

	public boolean isCycleColors() {
		return mStnCycleColorPalette == null;
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
}

/*
 * $Id: StnPlotSpecification.java,v 1.5 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import java.awt.*;
import javaoceanatlas.ui.*;
import org.w3c.dom.*;
import java.io.File;
import java.io.IOException;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

public class StnPlotSpecification implements PlotSpecification {
	protected double mWinXPlotMax;
	protected double mWinYPlotMax;
	protected double mWinXPlotMin;
	protected double mWinYPlotMin;
	protected int mSymbolSize;
	protected int mSymbol;
	protected int mXStnVarCode;
	protected int mYStnVarCode;
	protected String mXStnVarName;
	protected String mYStnVarName;
	protected boolean mPlotAxes, mXGrid, mYGrid;
	protected double mYInc, mXInc;
	protected int mXTics, mYTics;
	protected FileViewer mFileViewer;
	protected String mWinTitle = null;
	protected Color mFG, mBG;
	private Color mSymbolColor = Color.black;
	private Color mLineColor = Color.black;
	protected boolean mConnectObs = true;
	protected int mWidth;
	protected int mHeight;
	protected boolean mReverseY = false;
	protected int mSectionType;
	protected double mTraceOffset;

	public StnPlotSpecification() {

	}

	public StnPlotSpecification(StnPlotSpecification inSpec) {
		mWinXPlotMax = inSpec.mWinXPlotMax;
		mWinYPlotMax = inSpec.mWinYPlotMax;
		mWinXPlotMin = inSpec.mWinXPlotMin;
		mWinYPlotMin = inSpec.mWinYPlotMin;
		mSymbolSize = inSpec.mSymbolSize;
		mXStnVarCode = inSpec.mXStnVarCode;
		mYStnVarCode = inSpec.mYStnVarCode;
		if (inSpec.mXStnVarName != null)
			mXStnVarName = new String(inSpec.mXStnVarName);
		if (inSpec.mYStnVarName != null)
			mYStnVarName = new String(inSpec.mYStnVarName);
		mPlotAxes = inSpec.mPlotAxes;
		mXGrid = inSpec.mXGrid;
		mYGrid = inSpec.mYGrid;
		mYInc = inSpec.mYInc;
		mXInc = inSpec.mXInc;
		mXTics = inSpec.mXTics;
		mYTics = inSpec.mYTics;
		mFileViewer = inSpec.mFileViewer;
		mWinTitle = inSpec.mWinTitle;
		mFG = inSpec.mFG;
		mBG = inSpec.mBG;
		setLineColor(inSpec.getLineColor());
		setSymbolColor(inSpec.getSymbolColor());
		mConnectObs = inSpec.mConnectObs;
		mWidth = inSpec.mWidth;
		mHeight = inSpec.mHeight;
		mSymbol = inSpec.mSymbol;
		mReverseY = inSpec.mReverseY;
		mSectionType = inSpec.mSectionType;
		mTraceOffset = inSpec.mTraceOffset;
	}

	public int getXStnVarCode() {
		return mXStnVarCode;
	}

	public int getYStnVarCode() {
		return mYStnVarCode;
	}
	
	public String exportJSON(File file) {    
		return null;
	}

	// protected int mStnVarCode;
	public void saveAsXML(FileViewer fv, Document doc, Element root) {
		// in item is the tag for proppropwindow
		// attributes
		Element item = doc.createElement("stationplot");
		item.setAttribute("title", mWinTitle);
		item.setAttribute("sectiontype", String.valueOf(mSectionType));
		item.setAttribute("traceoffset", String.valueOf(mTraceOffset));
		item.setAttribute("plotaxes", String.valueOf(mPlotAxes));
		item.setAttribute("xgrid", String.valueOf(mXGrid));
		item.setAttribute("ygrid", String.valueOf(mYGrid));
		item.setAttribute("connectobs", String.valueOf(mConnectObs));
		item.setAttribute("height", String.valueOf(mHeight));
		item.setAttribute("width", String.valueOf(mWidth));
		item.setAttribute("symbolsize", String.valueOf(mSymbolSize));
		item.setAttribute("symbol", String.valueOf(mSymbol));
		item.setAttribute("reversey", String.valueOf(mReverseY));

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

		// x axes
		Element xaxitem = doc.createElement("xaxis");
		xaxitem.setAttribute("min", String.valueOf(mWinXPlotMin));
		xaxitem.setAttribute("max", String.valueOf(mWinXPlotMax));
		xaxitem.setAttribute("inc", String.valueOf(mXInc));
		xaxitem.setAttribute("tics", String.valueOf(mXTics));
		item.appendChild(xaxitem);

		// y axis
		Element yaxitem = doc.createElement("yaxis");
		yaxitem.setAttribute("param", mXStnVarName);
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

	public void setXStnVarCode(int i) {
		mXStnVarCode = i;
	}

	public void setYStnVarCode(int i) {
		mYStnVarCode = i;
	}

	public boolean isYGrid() {
		return mYGrid;
	}

	public void setYGrid(boolean b) {
		mYGrid = b;
	}

	public int getYTics() {
		return mYTics;
	}

	public void setYTics(int i) {
		mYTics = i;
	}

	public boolean isXGrid() {
		return mXGrid;
	}

	public void setXGrid(boolean b) {
		mXGrid = b;
	}

	public int getXTics() {
		return mXTics;
	}

	public void setXTics(int i) {
		mXTics = i;
	}

	public int getWidth() {
		return mWidth;
	}

	public void seWidth(int i) {
		mWidth = i;
	}

	public int getHeight() {
		return mHeight;
	}

	public void seHeight(int i) {
		mHeight = i;
	}

	public int getSectionType() {
		return mSectionType;
	}

	public void setSectionType(int i) {
		mSectionType = i;
	}

	public double getYInc() {
		return mYInc;
	}

	public void setXInc(double d) {
		mXInc = d;
	}

	public double getXInc() {
		return mXInc;
	}

	public void setYInc(double d) {
		mYInc = d;
	}

	public double getTraceOffset() {
		return mTraceOffset;
	}

	public void setTraceOffset(double d) {
		mTraceOffset = d;
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

	public boolean isConnectObs() {
		return mConnectObs;
	}

	public void setConnectObs(boolean b) {
		mConnectObs = b;
	}

	public boolean isPlotAxes() {
		return mPlotAxes;
	}

	public void setPlotAxes(boolean b) {
		mPlotAxes = b;
	}

	public void setFileViewer(FileViewer fv) {
		mFileViewer = fv;
	}

	public FileViewer getFileViewer() {
		return mFileViewer;
	}

	public void setXStnVarName(String s) {
		mXStnVarName = s;
	}

	public String getXStnVarName() {
		return mXStnVarName;
	}
	
	public void setYStnVarName(String s) {
		mYStnVarName = s;
	}

	public String getYStnVarName() {
		return mYStnVarName;
	}

	public boolean isReverseY() {
		return mReverseY;
	}

	public void setReverseY(boolean b) {
		mReverseY = b;
	}

	public void writeToLog(String preamble) throws IOException {
		String sectionText = "";
		if (mSectionType == JOAConstants.PROFSEQUENCE)
			sectionText = "Sequence,";
		else if (mSectionType == JOAConstants.PROFDISTANCE)
			sectionText = "Distance,";
		else if (mSectionType == JOAConstants.PROFXY)
			sectionText = "Station XY Plot";
		else if (mSectionType == JOAConstants.PROFLATITUDE)
			sectionText = "Latitude";
		else
			sectionText = "Longitide";

		try {
			JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Station Value Plot: Y = " + mXStnVarName + ", Off. = " + sectionText
			    + ", Y Min = " + JOAFormulas.formatDouble(mWinYPlotMin, 3, false) + ", Y Max = "
			    + JOAFormulas.formatDouble(mWinYPlotMax, 3, false) + ", X Min = "
			    + JOAFormulas.formatDouble(mWinXPlotMin, 3, false) + ", X Max = "
			    + JOAFormulas.formatDouble(mWinXPlotMax, 3, false) + ", Reverse Y = " + String.valueOf(mReverseY) + "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	public void setLineColor(Color mLineColor) {
	  this.mLineColor = mLineColor;
  }

	public Color getLineColor() {
	  return mLineColor;
  }

	public void setSymbolColor(Color mSymbolColor) {
	  this.mSymbolColor = mSymbolColor;
  }

	public Color getSymbolColor() {
	  return mSymbolColor;
  }
}

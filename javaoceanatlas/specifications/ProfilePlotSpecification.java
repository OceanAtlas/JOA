/*
 * $Id: ProfilePlotSpecification.java,v 1.4 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import java.awt.*;
import javaoceanatlas.ui.*;
import org.w3c.dom.*;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.File;
import java.io.IOException;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

public class ProfilePlotSpecification implements PlotSpecification {
	private double mWinXPlotMax;
	private double mWinYPlotMax;
	private double mWinXPlotMin;
	private double mWinYPlotMin;
	private int mSymbolSize;
	private int mSymbol;
	private int mXVarCode, mYVarCode;
	private boolean mYGrid;
	private double mYInc;
	private int mYTics;
	private FileViewer mFileViewer;
	private String mWinTitle = null;
	private boolean mIncludeCBAR;
	private boolean mIncludeObsPanel;
	private Color mFG, mBG;
	private boolean mConnectObs;
	private int mWidth;
	private int mHeight;
	private int mSectionType;
	private double mTraceOffset;
	private double mAmplitude;
	private int mSecOrigin = 0;
	private int mLineWidth;
	private boolean mPlotAxes;
	private boolean mPlotSymbols;
	private boolean mColorByCBParam = true;
	protected boolean mAccumulateStns = false;

	public ProfilePlotSpecification() {

	}

	public ProfilePlotSpecification(ProfilePlotSpecification inSpec) {
		mWinXPlotMax = inSpec.mWinXPlotMax;
		mWinYPlotMax = inSpec.mWinYPlotMax;
		mWinXPlotMin = inSpec.mWinXPlotMin;
		mWinYPlotMin = inSpec.mWinYPlotMin;
		mSymbolSize = inSpec.mSymbolSize;
		mSymbol = inSpec.mSymbol;
		mXVarCode = inSpec.mXVarCode;
		mYVarCode = inSpec.mYVarCode;
		mPlotAxes = inSpec.mPlotAxes;
		mYGrid = inSpec.mYGrid;
		mYInc = inSpec.mYInc;
		mYTics = inSpec.mYTics;
		mFileViewer = inSpec.mFileViewer;
		mWinTitle = inSpec.mWinTitle;
		mIncludeCBAR = inSpec.mIncludeCBAR;
		mIncludeObsPanel = inSpec.mIncludeObsPanel;
		mFG = inSpec.mFG;
		mBG = inSpec.mBG;
		mConnectObs = inSpec.mConnectObs;
		mWidth = inSpec.mWidth;
		mHeight = inSpec.mHeight;
		mTraceOffset = inSpec.mTraceOffset;
		mAmplitude = inSpec.mAmplitude;
		mSecOrigin = inSpec.mSecOrigin;
		mLineWidth = inSpec.mLineWidth;
		mPlotSymbols = inSpec.mPlotSymbols;
		mSectionType = inSpec.mSectionType;
		mColorByCBParam = inSpec.mColorByCBParam;
		mAccumulateStns = inSpec.mAccumulateStns;
	}

	public void saveAsXML(FileViewer fv, Document doc, Element root) {
		// in item is the tag for proppropwindow
		// attributes
		Element item = doc.createElement("profileplot");
		item.setAttribute("title", mWinTitle);
		item.setAttribute("sectiontype", String.valueOf(mSectionType));
		item.setAttribute("amplitude", String.valueOf(mAmplitude));
		item.setAttribute("secorigin", String.valueOf(mSecOrigin));
		item.setAttribute("linewidth", String.valueOf(mLineWidth));
		item.setAttribute("plotaxes", String.valueOf(mPlotAxes));
		item.setAttribute("ygrid", String.valueOf(mYGrid));
		item.setAttribute("includelegend", String.valueOf(mIncludeCBAR));
		item.setAttribute("includeobspanel", String.valueOf(mIncludeObsPanel));
		item.setAttribute("connectobs", String.valueOf(mConnectObs));
		item.setAttribute("colorbycb", String.valueOf(mColorByCBParam));
		item.setAttribute("height", String.valueOf(mHeight));
		item.setAttribute("width", String.valueOf(mWidth));
		item.setAttribute("plotsymbols", String.valueOf(mPlotSymbols));
		item.setAttribute("symbolsize", String.valueOf(mSymbolSize));
		item.setAttribute("symbol", String.valueOf(mSymbol));
		item.setAttribute("accumulatestns", String.valueOf(mAccumulateStns));

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
		xaxitem.setAttribute("param", String.valueOf(fv.mAllProperties[mXVarCode].getVarLabel()));
		xaxitem.setAttribute("min", String.valueOf(mWinXPlotMin));
		xaxitem.setAttribute("max", String.valueOf(mWinXPlotMax));
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

	public int getYTics() {
		return mYTics;
	}

	public void setYTics(int i) {
		mYTics = i;
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

	public int getSectionType() {
		return mSectionType;
	}

	public void setSectionType(int i) {
		mSectionType = i;
	}

	public int getSecOrigin() {
		return mSecOrigin;
	}

	public void setSecOrigin(int i) {
		mSecOrigin = i;
	}

	public int getLineWidth() {
		return mLineWidth;
	}

	public void setLineWidth(int i) {
		mLineWidth = i;
	}

	public double getYInc() {
		return mYInc;
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

	public double getAmplitude() {
		return mAmplitude;
	}

	public void setAmplitude(double d) {
		mAmplitude = d;
	}

	public String getWinTitle() {
		return mWinTitle;
	}

	public void setWinTitle(String s) {
		mWinTitle = new String(s);
	}

	public FileViewer getFileViewer() {
		return mFileViewer;
	}

	public void setFileViewer(FileViewer fv) {
		mFileViewer = fv;
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

	public boolean isIncludeCBAR() {
		return mIncludeCBAR;
	}

	public void setIncludeCBAR(boolean b) {
		mIncludeCBAR = b;
	}

	public boolean isIncludeObsPanel() {
		return mIncludeObsPanel;
	}

	public void setIncludeObsPanel(boolean b) {
		mIncludeObsPanel = b;
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

	public boolean isPlotSymbols() {
		return mPlotSymbols;
	}

	public void setPlotSymbols(boolean b) {
		mPlotSymbols = b;
	}

	public boolean isColorByCBParam() {
		return mColorByCBParam;
	}

	public void setColorByCBParam(boolean b) {
		mColorByCBParam = b;
	}

	public boolean isAccumulateStns() {
		return mAccumulateStns;
	}

	public void setAccumulateStns(boolean b) {
		mAccumulateStns = b;
	}

	public void writeToLog(String preamble) throws IOException {
		String sectionText = "";
		if (mSectionType == JOAConstants.PROFSEQUENCE)
			sectionText = "Sequence,";
		else if (mSectionType == JOAConstants.PROFDISTANCE)
			sectionText = "Distance,";
		else if (mSectionType == JOAConstants.PROFLATITUDE)
			sectionText = "Latitude";
		else
			sectionText = "Longitide";

		try {
			JOAConstants.LogFileStream.writeBytes(preamble + "\n");
			JOAConstants.LogFileStream.writeBytes("\t" + "Profile Plot: X = "
			    + mFileViewer.mAllProperties[mXVarCode].getVarLabel() + " Y = "
			    + mFileViewer.mAllProperties[mYVarCode].getVarLabel() + ", Off. = " + sectionText + ", Y Min = "
			    + JOAFormulas.formatDouble(mWinYPlotMin, 3, false) + ", Y Max = "
			    + JOAFormulas.formatDouble(mWinYPlotMax, 3, false) + ", X Min = "
			    + JOAFormulas.formatDouble(mWinXPlotMin, 3, false) + ", X Max = "
			    + JOAFormulas.formatDouble(mWinXPlotMax, 3, false) + ", Trc. off. = "
			    + JOAFormulas.formatDouble(mTraceOffset, 4, false) + ", Amp. = "
			    + JOAFormulas.formatDouble(mAmplitude, 4, false) + ", Sec. Origin = " + String.valueOf(mSecOrigin)
			    + ", Line width = " + String.valueOf(mLineWidth) + "\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (IOException ex) {
			throw ex;
		}
	}
	public String exportJSON(File file) {    
		JsonFactory f = new JsonFactory();
		JsonGenerator jsonGen;
		try {
			// test to see if it exists
			jsonGen = f.createJsonGenerator(file, JsonEncoding.UTF8);
			jsonGen.setPrettyPrinter(new DefaultPrettyPrinter());

			jsonGen.writeStartObject();
			// key value pairs go here
			jsonGen.writeObjectFieldStart("xyplotspecification");		
			jsonGen.writeNumberField("winxplotmax", mWinXPlotMax);
			jsonGen.writeNumberField("winxplotmin", mWinXPlotMin);
			jsonGen.writeNumberField("winyplotmax",  mWinYPlotMax);
			jsonGen.writeNumberField("winyplotmin", mWinYPlotMin);
//			jsonGen.writeBooleanField("linedrawn", mLineDrawn);
			jsonGen.writeNumberField("symbolsize", mSymbolSize);
			jsonGen.writeNumberField("symbol", mSymbol);
			jsonGen.writeNumberField("xvarcode", mXVarCode);
			jsonGen.writeNumberField("yvarcode", mYVarCode);
			
			jsonGen.writeStringField("xparam", mFileViewer.mAllProperties[mXVarCode].getVarLabel());
			jsonGen.writeStringField("yparam", mFileViewer.mAllProperties[mYVarCode].getVarLabel());
			jsonGen.writeStringField("xparamunits", mFileViewer.mAllProperties[mXVarCode].getUnits());
			jsonGen.writeStringField("yparamunits", mFileViewer.mAllProperties[mYVarCode].getUnits());
			
			jsonGen.writeBooleanField("plotaxes", mPlotAxes);
			jsonGen.writeBooleanField("ygrid", mYGrid);
			jsonGen.writeNumberField("yinc", mYInc);
			jsonGen.writeNumberField("ytics", mYTics);
			jsonGen.writeStringField("title", String.valueOf(this.getWinTitle()));
			jsonGen.writeBooleanField("includecolorbar", mIncludeCBAR);
			jsonGen.writeBooleanField("connectobs", mConnectObs);
			jsonGen.writeBooleanField("reversey", mFileViewer.mAllProperties[mYVarCode].isReverseY());
			jsonGen.writeBooleanField("accumulatestations", mAccumulateStns);
			jsonGen.writeBooleanField("colorbycolorbar", mColorByCBParam);
			String hexStr = Integer.toHexString(mFG.getRGB());
			jsonGen.writeStringField("fgcolor", "#" + hexStr);
			hexStr = Integer.toHexString(mBG.getRGB());
			jsonGen.writeStringField("bgcolor", "#" + hexStr);
			jsonGen.writeStringField("connectstncolor", "#" + hexStr);
			jsonGen.writeEndObject(); // for field 'xyplotspecification'
				 
			jsonGen.close(); // important: will force flushing of output, close underlying
								 // output stream
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    return null;
  }
}

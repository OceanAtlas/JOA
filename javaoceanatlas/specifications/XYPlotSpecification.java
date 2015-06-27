/*
 * $Id: XYPlotSpecification.java,v 1.5 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import gov.noaa.pmel.util.SoTRange;
import java.awt.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.utility.*;
import java.awt.*;
import org.w3c.dom.*;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.resources.*;

public class XYPlotSpecification implements PlotSpecification {
	protected int mNumXAxes = 1;
	protected double[] mWinXPlotMax = new double[10];
	protected double mWinYPlotMax;
	protected double[] mWinXPlotMin = new double[10];
	protected double mWinYPlotMin;
	protected boolean mLineDrawn;
	protected int[] mSymbolSize = { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
	protected int[] mSymbol = { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
	protected int[] mXVarCode = new int[10];
	protected int mYVarCode;
	protected boolean mPlotAxes=true, mXGrid=false, mYGrid=false;
	protected double mYInc;
	protected double[] mXInc = new double[10];
	protected int[] mXTics = new int[10];
	protected int mYTics;
	protected Vector<FileViewer> mFileViewer = new Vector<FileViewer>();
	protected String mWinTitle = null;
	protected boolean mIncludeCBAR;
	protected boolean mIncludeObsPanel;
	protected Color mFG, mBG;
	protected boolean mConnectObs;
	protected int mWidth;
	protected int mHeight;
	protected boolean mReverseY;
	protected boolean mPlotIsopycnals;
	protected double mRefPress;
	protected boolean mCanPlotIsoPycnals;
	protected int mSaltAxis = 0;
	protected boolean mPlotOnlyCurrStn = false;
	protected boolean mAccumulateStns = false;
	protected Color[] mConnectStnColors = { Color.black, Color.green, Color.red, new Color(1.0f, 0.0f, 1.0f), Color.blue,
	    Color.orange, Color.cyan, Color.magenta, Color.pink, Color.white };
	protected boolean mColorByCBParam = true;
	protected boolean mColorByConnectLineColor = false;
	private Color mFilteredOutColor = Color.black;
	private Vector<JOATimeFilter>mTimeFilters;
	private Vector<TSModelTerm> mTSModelTerms = null;
	private NumericFilter mZFilter = null;
	private NumericFilter mTFilter = null;
	private String mOverrideLabel = null;
	private double mModelIntercept;
	private double mTMinOfModel;
	private double mTMaxOfModel;
	private double[] mModelErrTerms;
	private boolean mIgnoreMissingObs = true;
	private NewColorBar mStnCycleColorBar = null;

	public XYPlotSpecification() {

	}

	public XYPlotSpecification(XYPlotSpecification inSpec) {
		mNumXAxes = inSpec.mNumXAxes;
		for (int i = 0; i < inSpec.mNumXAxes; i++) {
			mWinXPlotMax[i] = inSpec.mWinXPlotMax[i];
			mWinXPlotMin[i] = inSpec.mWinXPlotMin[i];
			mXVarCode[i] = inSpec.mXVarCode[i];
			mXInc[i] = inSpec.mXInc[i];
			mXTics[i] = inSpec.mXTics[i];
			mConnectStnColors[i] = new Color(inSpec.mConnectStnColors[i].getRGB());
			mSymbolSize[i] = inSpec.mSymbolSize[i];
			mSymbol[i] = inSpec.mSymbol[i];
		}
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
		mIncludeCBAR = inSpec.mIncludeCBAR;
		mIncludeObsPanel = inSpec.mIncludeObsPanel;
		mFG = inSpec.mFG;
		mBG = inSpec.mBG;
		mConnectObs = inSpec.mConnectObs;
		mWidth = inSpec.mWidth;
		mHeight = inSpec.mHeight;
		mReverseY = inSpec.mReverseY;
		mPlotIsopycnals = inSpec.mPlotIsopycnals;
		mCanPlotIsoPycnals = inSpec.mCanPlotIsoPycnals;
		mPlotOnlyCurrStn = inSpec.mPlotOnlyCurrStn;
		mAccumulateStns = inSpec.mAccumulateStns;
		mRefPress = inSpec.mRefPress;
		mSaltAxis = inSpec.mSaltAxis;
		mColorByCBParam = inSpec.mColorByCBParam;
		mColorByConnectLineColor = inSpec.mColorByConnectLineColor;
		mFilteredOutColor = inSpec.mFilteredOutColor;
		
		mTimeFilters = inSpec.getTimeFilters();
		mTSModelTerms = inSpec.getTSModelTerms();
		mZFilter = inSpec.getZFilter();
		mTFilter = inSpec.getTFilter();
		mOverrideLabel = inSpec.getOverrideLabel();
		mModelIntercept = inSpec.getModelIntercept();
		mTMinOfModel = inSpec.getTMinOfModel();
		mTMaxOfModel = inSpec.getTMaxOfModel();
		mModelErrTerms = inSpec.getModelErrTerms();
		mIgnoreMissingObs = inSpec.isIgnoreMissingObs();
		mStnCycleColorBar = inSpec.getStnCycleColorBar();
	}

	public void saveAsXML(FileViewer fv, Document doc, Element root) {
		// in item is the tag for proppropwindow
		// attributes
		Element item = doc.createElement("proppropplot");
		item.setAttribute("title", mWinTitle);
		item.setAttribute("linedrawn", String.valueOf(mLineDrawn));
		item.setAttribute("numxaxes", String.valueOf(mNumXAxes));
		item.setAttribute("plotaxes", String.valueOf(mPlotAxes));
		item.setAttribute("xgrid", String.valueOf(mXGrid));
		item.setAttribute("ygrid", String.valueOf(mYGrid));
		item.setAttribute("includelegend", String.valueOf(mIncludeCBAR));
		item.setAttribute("includeobspanel", String.valueOf(mIncludeObsPanel));
		item.setAttribute("connectobs", String.valueOf(mConnectObs));
		item.setAttribute("reversey", String.valueOf(mReverseY));
		item.setAttribute("plotisopycnals", String.valueOf(mPlotIsopycnals));
		item.setAttribute("plotonlycurrstn", String.valueOf(mPlotOnlyCurrStn));
		item.setAttribute("accumulatestns", String.valueOf(mAccumulateStns));
		item.setAttribute("refpres", String.valueOf(mRefPress));
		item.setAttribute("saltaxis", String.valueOf(mSaltAxis));
		item.setAttribute("colorbycb", String.valueOf(mColorByCBParam));
		item.setAttribute("colorbyclc", String.valueOf(mColorByConnectLineColor));
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

		// x axes
		for (int i = 0; i < mNumXAxes; i++) {
			Element xaxitem = doc.createElement("xaxis");
			xaxitem.setAttribute("param", String.valueOf(fv.mAllProperties[mXVarCode[i]].getVarLabel()));
			xaxitem.setAttribute("min", String.valueOf(mWinXPlotMin[i]));
			xaxitem.setAttribute("max", String.valueOf(mWinXPlotMax[i]));
			xaxitem.setAttribute("inc", String.valueOf(mXInc[i]));
			xaxitem.setAttribute("tics", String.valueOf(mXTics[i]));
			cItem = doc.createElement("connectstncolor");
			cItem.setAttribute("red", String.valueOf(mConnectStnColors[i].getRed()));
			cItem.setAttribute("green", String.valueOf(mConnectStnColors[i].getGreen()));
			cItem.setAttribute("blue", String.valueOf(mConnectStnColors[i].getBlue()));
			xaxitem.appendChild(cItem);
			xaxitem.setAttribute("symbolsize", String.valueOf(mSymbolSize[i]));
			xaxitem.setAttribute("symbol", String.valueOf(mSymbol[i]));
			item.appendChild(xaxitem);
		}

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

	public double getWinXPlotMax(int a) {
		return mWinXPlotMax[a];
	}

	public void setWinXPlotMax(int a, double d) {
		mWinXPlotMax[a] = d;
	}

	public double getWinYPlotMax() {
		return mWinYPlotMax;
	}

	public void setWinYPlotMax(double d) {
		mWinYPlotMax = d;
	}

	public double getWinXPlotMin(int a) {
		return mWinXPlotMin[a];
	}

	public void setWinXPlotMin(int a, double d) {
		mWinXPlotMin[a] = d;
	}

	public double getWinYPlotMin() {
		return mWinYPlotMin;
	}

	public void setWinYPlotMin(double d) {
		mWinYPlotMin = d;
	}

	public int getSymbolSize(int a) {
		return mSymbolSize[a];
	}

	public int[] getSymbolSizes() {
		return mSymbolSize;
	}

	public void setSymbolSize(int a, int i) {
		mSymbolSize[a] = i;
	}

	public int getSymbol(int a) {
		return mSymbol[a];
	}

	public int[] getSymbols() {
		return mSymbol;
	}

	public void setSymbol(int a, int i) {
		mSymbol[a] = i;
	}

	public int getXVarCode(int a) {
		return mXVarCode[a];
	}

	public void setXVarCode(int a, int i) {
		mXVarCode[a] = i;
	}

	public void setXVarCodes(int[] a) {
		mXVarCode = a;
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
		return mXTics[a];
	}

	public void setXTics(int a, int i) {
		mXTics[a] = i;
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
		return mXInc[a];
	}

	public void setXInc(int a, double d) {
		mXInc[a] = d;
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

	public boolean isColorByCBParam() {
		return mColorByCBParam;
	}

	public boolean isColorByConnectLineColor() {
		return mColorByConnectLineColor;
	}

	public void setColorByCBParam(boolean b) {
		mColorByCBParam = b;
	}

	public void setColorByConnectLineColor(boolean b) {
		mColorByConnectLineColor = b;
	}

	public int getNumXAxes() {
		return mNumXAxes;
	}

	public void setNumXAxes(int i) {
		mNumXAxes = i;
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

	public Color getConnectStnColor(int a) {
		return mConnectStnColors[a];
	}

	public Color[] getConnectStnColors() {
		return mConnectStnColors;
	}

	public void setConnectStnColors(int a, Color c) {
		mConnectStnColors[a] = new Color(c.getRed(), c.getGreen(), c.getBlue());
	}

	public double[] getXMaxValues() {
		return mWinXPlotMax;
	}

	public double[] getXMinValues() {
		return mWinXPlotMin;
	}

	public double[] getXIncs() {
		return mXInc;
	}

	public int[] getXTics() {
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
			JOAConstants.LogFileStream.writeBytes("\t" + "XY Plot: " + " Y = "
			    + fv.mAllProperties[mYVarCode].getVarLabel() + ", Y Min = "
			    + JOAFormulas.formatDouble(mWinYPlotMin, 3, false) + ", Y Max = "
			    + JOAFormulas.formatDouble(mWinYPlotMax, 3, false));

			for (int i = 0; i < mNumXAxes; i++) {
				JOAConstants.LogFileStream.writeBytes(", X Axis #" + (i + 1) + " = "
				    + fv.mAllProperties[mXVarCode[i]].getVarLabel() + ", X Min = "
				    + JOAFormulas.formatDouble(mWinXPlotMin[i], 3, false) + ", X Max = "
				    + JOAFormulas.formatDouble(mWinXPlotMax[i], 3, false));
			}
			JOAConstants.LogFileStream.writeBytes(", Rev. Y = " + String.valueOf(mReverseY));

			if (mPlotIsopycnals) {
				JOAConstants.LogFileStream
				    .writeBytes(", Isopyc. ref. press = " + JOAFormulas.formatDouble(mRefPress, 3, false));
			}
			JOAConstants.LogFileStream.writeBytes("\n");
		}
		catch (IOException ex) {
			throw ex;
		}
		}
	}

	public void setZFilter(NumericFilter filter) {
		mZFilter = filter;
  }
	
	public void setTFilter(NumericFilter filter) {
		mTFilter = filter;
  }

	public NumericFilter getZFilter() {
		return mZFilter;
  }
	
	public NumericFilter getTFilter() {
		return mTFilter;
  }

	public void setFilteredOutColor(Color mFilteredOutColor) {
	  this.mFilteredOutColor = mFilteredOutColor;
  }

	public Color getFilteredOutColor() {
	  return mFilteredOutColor;
  }

	public void setTimeFilters(Vector<JOATimeFilter> mTimeFilters) {
	  this.mTimeFilters = mTimeFilters;
  }

	public Vector<JOATimeFilter> getTimeFilters() {
	  return mTimeFilters;
  }

	public void setTSModelTerms(Vector<TSModelTerm> mTSModelTerms) {
	  this.mTSModelTerms = mTSModelTerms;
  }
	
	public void setModelIntercept(double intercept) {
		mModelIntercept = intercept;
	}
	
	public double getModelIntercept() {
		return mModelIntercept;
	}

	public Vector<TSModelTerm> getTSModelTerms() {
	  return mTSModelTerms;
  }

	public void setOverrideLabel(String mOverrideLabel) {
	  this.mOverrideLabel = mOverrideLabel;
  }

	public String getOverrideLabel() {
	  return mOverrideLabel;
  }

	public void setTMinOfModel(double tmin) {
	  this.mTMinOfModel = tmin;
  }

	public double getTMinOfModel() {
	  return mTMinOfModel;
  }

	public void setTMaxOfModel(double tmax) {
	  this.mTMaxOfModel = tmax;
  }

	public double getTMaxOfModel() {
	  return mTMaxOfModel;
  }

	public void setModelErrTerms(double[] mModelErrTerms) {
	  this.mModelErrTerms = mModelErrTerms;
  }

	public double[] getModelErrTerms() {
	  return mModelErrTerms;
  }

	/**
   * @param mIgnoreMissingObs the mIgnoreMissingObs to set
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
   * @param mStnCycleColorBar the mStnCycleColorBar to set
   */
  public void setStnCycleColorBar(NewColorBar mStnCycleColorBar) {
	  this.mStnCycleColorBar = mStnCycleColorBar;
  }

	/**
   * @return the mStnCycleColorBar
   */
  public NewColorBar getStnCycleColorBar() {
	  return mStnCycleColorBar;
  }
  
  public boolean isCycleColors() {
  	return mStnCycleColorBar == null;
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
			jsonGen.writeNumberField("winxplotmax", mWinXPlotMax[0]);
			jsonGen.writeNumberField("winxplotmin", mWinXPlotMin[0]);
			jsonGen.writeNumberField("winyplotmax",  mWinYPlotMax);
			jsonGen.writeNumberField("winyplotmin", mWinYPlotMin);
			jsonGen.writeBooleanField("linedrawn", mLineDrawn);
			jsonGen.writeNumberField("symbolsize", mSymbolSize[0]);
			jsonGen.writeNumberField("symbol", mSymbol[0]);
			jsonGen.writeNumberField("xvarcode", mXVarCode[0]);
			jsonGen.writeNumberField("yvarcode", mYVarCode);
			
			FileViewer fv = mFileViewer.elementAt(0);
			jsonGen.writeStringField("xparam", fv.mAllProperties[mXVarCode[0]].getVarLabel());
			jsonGen.writeStringField("yparam", fv.mAllProperties[mYVarCode].getVarLabel());
			jsonGen.writeStringField("xparamunits", fv.mAllProperties[mXVarCode[0]].getUnits());
			jsonGen.writeStringField("yparamunits", fv.mAllProperties[mYVarCode].getUnits());
			
			jsonGen.writeBooleanField("plotaxes", mPlotAxes);
			jsonGen.writeBooleanField("xgrid", mXGrid);
			jsonGen.writeBooleanField("ygrid", mYGrid);
			jsonGen.writeNumberField("yinc", mYInc);
			jsonGen.writeNumberField("xinc", mXInc[0]);
			jsonGen.writeNumberField("xtics",mXTics[0]);
			jsonGen.writeNumberField("ytics", mYTics);
			jsonGen.writeStringField("title", String.valueOf(this.getWinTitle()));
			jsonGen.writeBooleanField("includecolorbar", mIncludeCBAR);
			jsonGen.writeBooleanField("connectobs", mConnectObs);
			jsonGen.writeBooleanField("reversey", mReverseY);
			jsonGen.writeBooleanField("plotisopycnals", mPlotIsopycnals);
			jsonGen.writeNumberField("refpres", mRefPress);
			jsonGen.writeBooleanField("canplotisopycnals", mCanPlotIsoPycnals);
			jsonGen.writeBooleanField("plotonlycurrstn", mPlotOnlyCurrStn);
			jsonGen.writeBooleanField("accumulatestations", mAccumulateStns);
			jsonGen.writeBooleanField("colorbycolorbar", mColorByCBParam);
			String hexStr = Integer.toHexString(mFG.getRGB());
			jsonGen.writeStringField("fgcolor", "#" + hexStr);
			hexStr = Integer.toHexString(mBG.getRGB());
			jsonGen.writeStringField("bgcolor", "#" + hexStr);
			jsonGen.writeBooleanField("connectbylinecolor", mColorByConnectLineColor);
			hexStr = Integer.toHexString(mConnectStnColors[0].getRGB());
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

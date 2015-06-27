/*
 * $Id: IntegrationSpecification.java,v 1.6 2005/06/17 18:04:10 oz Exp $
 *
 */

package javaoceanatlas.specifications;

import javaoceanatlas.ui.FileViewer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.utility.JOAFormulas;

public class IntegrationSpecification implements PlotSpecification {
  protected int mIntVarCode, mWRTVarCode;
  protected double mIntRangeMax;
  protected double mIntRangeMin;
  protected boolean mUseDeepest;
  protected boolean mUseShallowest;
  protected boolean mComputeMean;
  protected int mSearchMethod;
  protected boolean mInterpolateMissing;
  protected int mFarBottleLimit = 2;
  String mIntVar, mWRTVar;

  public IntegrationSpecification() {

  }

  public IntegrationSpecification(FileViewer fv, int integrandVar, int wrtVar, double min, double max,
                                  boolean useShallowest, boolean useDeepest, boolean computeWeightedMean, int search,
                                  boolean interpMissing, int numObs) {
    mIntVar = fv.mAllProperties[integrandVar].getVarLabel();
    mWRTVar = fv.mAllProperties[wrtVar].getVarLabel();
    mIntRangeMin = min;
    mIntRangeMax = max;
    mIntVarCode = integrandVar;
    mWRTVarCode = wrtVar;
    mUseDeepest = useDeepest;
    mUseShallowest = useShallowest;
    mComputeMean = computeWeightedMean;
    mSearchMethod = search;
    mInterpolateMissing = interpMissing;
    mFarBottleLimit = numObs;
  }

  public IntegrationSpecification(IntegrationSpecification inSpec) {
    mIntVar = new String(inSpec.mIntVar);
    mWRTVar = new String(inSpec.mWRTVar);
    mIntRangeMin = inSpec.mIntRangeMin;
    mIntRangeMax = inSpec.mIntRangeMax;
    mIntVarCode = inSpec.mIntVarCode;
    mWRTVarCode = inSpec.mWRTVarCode;
    mUseDeepest = inSpec.mUseDeepest;
    mUseShallowest = inSpec.mUseShallowest;
    mComputeMean = inSpec.mComputeMean;
    mSearchMethod = inSpec.mSearchMethod;
    mInterpolateMissing = inSpec.mInterpolateMissing;
    mFarBottleLimit = inSpec.mFarBottleLimit;
  }

  public int getIntVar() {
    return mIntVarCode;
  }

  public int getWRTVar() {
    return mWRTVarCode;
  }

  public int getFarBottleLimit() {
    return mFarBottleLimit;
  }

  public void setFarBottleLimit(int i) {
    mFarBottleLimit = i;
  }

  public int getSearchMethod() {
    return mSearchMethod;
  }

  public double getMinIntVal() {
    return mIntRangeMin;
  }

  public double getMaxIntVal() {
    return mIntRangeMax;
  }

  public boolean isUseShallowest() {
    return mUseShallowest;
  }

  public boolean isUseDeepest() {
    return mUseDeepest;
  }

  public boolean isInterpolateMissing() {
    return mInterpolateMissing;
  }

  public boolean isComputeMean() {
    return mComputeMean;
  }
	
	public String exportJSON(File file) {    
		return null;
	}

  public void saveAsXML(FileViewer fv, Document doc, Element item) {
    item.setAttribute("integrationintvariable", fv.mAllProperties[mIntVarCode].getVarLabel());
    item.setAttribute("integrationwrtvariable", fv.mAllProperties[mWRTVarCode].getVarLabel());
    item.setAttribute("integrationsearchmethod", String.valueOf(this.getSearchMethod()));
    item.setAttribute("integrationminintval", String.valueOf(this.getMinIntVal()));
    item.setAttribute("integrationmaxintval", String.valueOf(this.getMaxIntVal()));
    item.setAttribute("integrationuseshallowest", String.valueOf(this.isUseShallowest()));
    item.setAttribute("integrationusedeepest", String.valueOf(this.isUseDeepest()));
    item.setAttribute("integrationinterpolatemissing", String.valueOf(this.isInterpolateMissing()));
    item.setAttribute("integrationcomputemean", String.valueOf(this.isComputeMean()));
    item.setAttribute("integrationfarbottlelimit", String.valueOf(this.getFarBottleLimit()));
  }

  public void writeToLog(String preamble) throws IOException {
    String dirText = " (top down):";
    if (mSearchMethod == JOAConstants.SEARCH_BOTTOM_UP) {
      dirText = " (bottom up):";
    }

    try {
      JOAConstants.LogFileStream.writeBytes(preamble);
      JOAConstants.LogFileStream.writeBytes("\t" + "Integration " + dirText + " " + mIntVar + " WRT " + mWRTVar +
                                            " Int. Range Min = " + JOAFormulas.formatDouble(mIntRangeMin, 3, false) +
                                            " Int. Range Max = " + JOAFormulas.formatDouble(mIntRangeMax, 3, false) +
                                            " Use deepest = " + Boolean.toString(mUseDeepest) + " Use Shallowest = " +
                                            Boolean.toString(mUseShallowest) + " Compute Mean = " +
                                            Boolean.toString(mComputeMean) + " Interpolate Missing = " +
                                            Boolean.toString(mInterpolateMissing) + " Far Bottle Limit = " +
                                            Integer.toString(mFarBottleLimit) + "\n");
      JOAConstants.LogFileStream.flush();
    }
    catch (IOException ex) {
      throw ex;
    }
  }
}

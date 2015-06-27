/*
 * $Id: IntegrationSpecification.java,v 1.5 2004/09/14 19:11:25 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.specifications;

import gov.noaa.pmel.eps2.ExportVariable;


/**
 * <code>IntegrationSpecification</code> Class for settings for an Integration Calculation
 *
 * @author oz
 * @version 1.0
 */

public class NQIntegrationSpecification {
  protected ExportVariable mIntVarCode, mWRTVarCode;
  protected double mIntRangeMax;
  protected double mIntRangeMin;
  protected boolean mUseDeepest;
  protected boolean mUseShallowest;
  protected boolean mComputeMean;
  protected int mSearchMethod;
  protected boolean mInterpolateMissing;

  public NQIntegrationSpecification() {

  }

  public NQIntegrationSpecification(ExportVariable integrandVar, ExportVariable wrtVar, double min, double max,
                                  boolean useShallowest, boolean useDeepest, boolean computeWeightedMean, int search,
                                  boolean interpMissing) {
    mIntRangeMin = min;
    mIntRangeMax = max;
    mIntVarCode = integrandVar;
    mWRTVarCode = wrtVar;
    mUseDeepest = useDeepest;
    mUseShallowest = useShallowest;
    mComputeMean = computeWeightedMean;
    mSearchMethod = search;
    mInterpolateMissing = interpMissing;
  }

  public NQIntegrationSpecification(NQIntegrationSpecification inSpec) {
    mIntRangeMin = inSpec.mIntRangeMin;
    mIntRangeMax = inSpec.mIntRangeMax;
    mIntVarCode = inSpec.mIntVarCode;
    mWRTVarCode = inSpec.mWRTVarCode;
    mUseDeepest = inSpec.mUseDeepest;
    mUseShallowest = inSpec.mUseShallowest;
    mComputeMean = inSpec.mComputeMean;
    mSearchMethod = inSpec.mSearchMethod;
    mInterpolateMissing = inSpec.mInterpolateMissing;
  }

  public ExportVariable getIntVar() {
    return mIntVarCode;
  }

  public ExportVariable getWRTVar() {
    return mWRTVarCode;
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

  public void dumpSpecification(String title) {
    System.out.println(title);
    System.out.println("mIntRangeMin = " + mIntRangeMin);
    System.out.println("mIntRangeMax = " + mIntRangeMax);
    System.out.println("mIntVarCode = " + mIntVarCode.toString(4));
    System.out.println("mWRTVarCode = " + mWRTVarCode.toString(4));
    System.out.println("mUseDeepest = " + mUseDeepest);
    System.out.println("mUseShallowest = " + mUseShallowest);
    System.out.println("mComputeMean = " + mComputeMean);
    System.out.println("mSearchMethod = " + mSearchMethod);
    System.out.println("mInterpolateMissing = " + mInterpolateMissing);
  }
}

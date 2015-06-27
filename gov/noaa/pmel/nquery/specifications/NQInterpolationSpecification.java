/*
 * $Id: InterpolationSpecification.java,v 1.4 2004/09/14 19:11:25 oz Exp $
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
 * <code>InterpolationSpecification</code> Class for settings for an Interpolation Calculation
 *
 * @author oz
 * @version 1.0
 */

public class NQInterpolationSpecification {
  protected ExportVariable mIntVarCode, mWRTVarCode;
  protected double mIntAtValue;
  protected boolean mUseDeepest;
  protected boolean mAtSurface;
  protected double mSurfaceDepthLimit;
  protected boolean mAtBottom;
  protected int mSearchMethod;
  protected boolean mInterpolateMissing;

  public NQInterpolationSpecification() {

  }

  public NQInterpolationSpecification(ExportVariable integrandVar, ExportVariable wrtVar, double val, boolean atSurface,
                                      double depthLimit, boolean atBottom, boolean useDeepest, int search,
                                      boolean interpMissing) {
    mIntVarCode = integrandVar;
    mWRTVarCode = wrtVar;
    mIntAtValue = val;
    mAtSurface = atSurface;
    mSurfaceDepthLimit = depthLimit;
    mAtBottom = atBottom;
    mUseDeepest = useDeepest;
    mSearchMethod = search;
    mInterpolateMissing = interpMissing;
  }

  public NQInterpolationSpecification(NQInterpolationSpecification inSpec) {
    mIntVarCode = inSpec.mIntVarCode;
    mWRTVarCode = inSpec.mWRTVarCode;
    mIntAtValue = inSpec.mIntAtValue;
    mAtSurface = inSpec.mAtSurface;
    mAtBottom = inSpec.mAtBottom;
    mUseDeepest = inSpec.mUseDeepest;
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

  public double getAtVal() {
    return mIntAtValue;
  }

  public double getDepthLimit() {
    return mSurfaceDepthLimit;
  }

  public boolean isAtSurface() {
    return mAtSurface;
  }

  public boolean isAtBottom() {
    return mAtBottom;
  }

  public boolean isUseDeepest() {
    return mUseDeepest;
  }

  public boolean isInterpolateMissing() {
    return mInterpolateMissing;
  }
}

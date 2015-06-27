/*
 * $Id: MixedLayerCalcSpec.java,v 1.3 2004/09/14 19:11:25 oz Exp $
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
 * <code>MixedLayerCalcSpec</code> Class for settings for a Mixed-Layer Depth Calculation
 *
 * @author oz
 * @version 1.0
 */

public class NQMixedLayerCalcSpec {
  private int mMethod;
  private double mDepth;
  private double mStartDepth;
  private double mTolerance;
  private ExportVariable mParam;

  public NQMixedLayerCalcSpec(int method, ExportVariable param, double startz, double z, double toln) {
    mMethod = method;
    mParam = param;
    mStartDepth = startz;
    mDepth = z;
    mTolerance = toln;
  }

  public int getMethod() {
    return mMethod;
  }

  public void setMethod(int i) {
    mMethod = i;
  }

  public ExportVariable getParam() {
    return mParam;
  }

  public void setParam(ExportVariable param) {
    mParam = param;
  }

  public double getDepth() {
    return mDepth;
  }

  public void setDepth(double d) {
    mDepth = d;
  }

  public double getStartDepth() {
    return mStartDepth;
  }

  public void setStartDepth(double d) {
    mStartDepth = d;
  }

  public double getTolerance() {
    return mTolerance;
  }

  public void setTolerance(double d) {
    mTolerance = d;
  }
}

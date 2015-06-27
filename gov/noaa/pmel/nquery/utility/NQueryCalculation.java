/*
 * $Id: Calculation.java,v 1.7 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.utility;

import gov.noaa.pmel.nquery.resources.*;
import gov.noaa.pmel.nquery.specifications.*;

public class NQueryCalculation {
  Object mArg = null; // will be either Boolean or Double or null
  int mTypeCalc = NQueryConstants.OBS_CALC_TYPE;
  String mCalcType = null;
  String mCanonicalName = null;
  String mUnits = null;
  boolean mCustomDensity = false;
  boolean mBuoyancyFrequency = false;
  boolean mIncludedErrorTerms = false;
  boolean mIsEditable = false;
  boolean mIsStnVar = false;
  String mCustomPrompt = null;

  public NQueryCalculation(String inType, String cName, Object inArg, String units, boolean editable, boolean stnvar) {
    mCalcType = new String(inType);
    mCanonicalName = new String(cName);
    mArg = inArg;
    mTypeCalc = NQueryConstants.OBS_CALC_TYPE;
    if (units != null) {
      mUnits = new String(units);
    }
    mIsEditable = editable;
    mIsStnVar = stnvar;
  }

  public NQueryCalculation(String inType, String cName, Object inArg, int calcType, String units, boolean editable,
                     boolean stnvar) {
    mCalcType = new String(inType);
    mCanonicalName = new String(cName);
    mArg = inArg;
    mTypeCalc = calcType;
    if (units != null) {
      mUnits = new String(units);
    }
    mIsEditable = editable;
    mIsStnVar = stnvar;
  }

  public NQueryCalculation(String inType, String cName, Object inArg, int calcType, boolean editable, boolean stnvar) {
    this(inType, cName, inArg, calcType, null, editable, stnvar);
  }

  public NQueryCalculation(String inType, String cName, int calcType, String units, boolean editable, boolean stnvar) {
    mCalcType = new String(inType);
    mCanonicalName = new String(cName);
    mTypeCalc = calcType;
    if (units != null) {
      mUnits = new String(units);
    }
    mIsEditable = editable;
    mIsStnVar = stnvar;
  }

  public NQueryCalculation(String inType, String cName, int calcType, boolean editable, boolean stnvar) {
    this(inType, cName, calcType, null, editable, stnvar);
  }

  public void setCustomPrompt(String cp) {
    mCustomPrompt = new String(cp);
  }

  public String getCustomPrompt() {
    return mCustomPrompt;
  }

  public void setUnits(String un) {
    mUnits = new String(un);
  }

  public String getUnits() {
    return mUnits;
  }

  public void setIncludeErrorTerms(boolean flag) {
    mIncludedErrorTerms = flag;
  }

  public boolean isIncludeErrorTerms() {
    return mIncludedErrorTerms;
  }

  public int getTypeCalc() {
    return mTypeCalc;
  }

  public String getCalcType() {
    return mCalcType;
  }

  public String getCanonicalName() {
    return mCanonicalName;
  }

  public void setCalcType(String s) {
    mCalcType = new String(s);
  }

  public void setCanonicalName(String s) {
    mCanonicalName = new String(s);
  }

  public boolean isCustomDensity() {
    return mCustomDensity;
  }

  public boolean isStationVar() {
    return mIsStnVar;
  }

  public void setIsCustomDensity() {
    mCustomDensity = true;
  }

  public double getArgAsDouble() {
    return ((Double)mArg).doubleValue();
  }

  public NQMixedLayerCalcSpec getArgAsMixedLayerCalcSpec() {
    return (NQMixedLayerCalcSpec)mArg;
  }

  public Object getArg() {
    return mArg;
  }

  public void setArg(Object arg) {
    mArg = arg;
  }

  public NQIntegrationSpecification getArgAsIntegrationSpecification() {
    return (NQIntegrationSpecification)mArg;
  }

  //public NeutralSurfaceSpecification getArgAsNeutralSurfaceSpecification() {
  //	return (NeutralSurfaceSpecification)mArg;
  //}

  public NQInterpolationSpecification getArgAsInterpolationSpecification() {
    return (NQInterpolationSpecification)mArg;
  }

  public boolean getArgAsBoolean() {
    return ((Boolean)mArg).booleanValue();
  }

  public void setIsBuoyanceFrequency() {
    mBuoyancyFrequency = true;
  }

  public boolean isBuoyanceFrequency() {
    return mBuoyancyFrequency;
  }

  public boolean isEditable() {
    return mIsEditable;
  }

  /*public void saveAsXML(FileViewer fv, Document doc, Element root) {
   Element item = doc.createElement("calculation");
   //attributes: isBuoyanceFrequency, isCustomDensity,,
      item.setAttribute("parameter", String.valueOf(this.getCalcType()));
      item.setAttribute("calctype", String.valueOf(this.getTypeCalc()));
      if (this.getUnits() != null && this.getUnits().length() > 0)
       item.setAttribute("calcunits", String.valueOf(this.getUnits()));
      if (this.isBuoyanceFrequency())
       item.setAttribute("efolding", String.valueOf(this.getArgAsDouble()));
      if (this.isCustomDensity())
       item.setAttribute("customdensity", String.valueOf(this.getArgAsDouble()));

   // arguments
   if (getArg() != null) {
    if (getArg() instanceof IntegrationSpecification)
     getArgAsIntegrationSpecification().saveAsXML(fv, doc, item);
    else if (getArg() instanceof MixedLayerCalcSpec)
     getArgAsMixedLayerCalcSpec().saveAsXML(fv, doc, item);
    else if (getArg() instanceof NeutralSurfaceSpecification) {
     getArgAsNeutralSurfaceSpecification().saveAsXML(item);
    }
   }
      item.setAttribute("includeerrorterms", String.valueOf(this.isIncludeErrorTerms()));

   root.appendChild(item);
    }*/

  public void dumpCalculation(String title) {
    System.out.println(title);
    System.out.println("mTypeCalc = " + mTypeCalc);
    System.out.println("mCalcType = " + mCalcType);
    System.out.println("mUnits = " + mUnits);
    System.out.println("mCustomDensity = " + mCustomDensity);
    System.out.println("mBuoyancyFrequency = " + mBuoyancyFrequency);
    System.out.println("mIncludedErrorTerms = " + mIncludedErrorTerms);
    System.out.println("mIsEditable = " + mIsEditable);
    System.out.println("mCustomPrompt = " + mCustomPrompt);
    System.out.println("mArg = " + mArg);
  }
}

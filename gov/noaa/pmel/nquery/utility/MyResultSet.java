/*
 * $Id: MyResultSet.java,v 1.1 2004/10/01 20:23:19 oz Exp $
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

import gov.noaa.pmel.util.GeoDate;

public class MyResultSet {
  String mItemID;          //platform
  String mType;
  String mVars;
  String mUnits;
  String mLexicon;
  String mZUnits;
  GeoDate mDate;
  GeoDate mStartDate;
  GeoDate mEndDate;
  double mLon;
  double mLat;
  double mZMin;
  double mZMax;
  String mStnID;
  String mPlat;

  public MyResultSet(String id, String plat, String stnid, String type, double lon, double lat, double zmin, double zmax, String zunits,
                     GeoDate stdate, GeoDate endDate, String vars, String units, String lexicon) {
    mItemID = new String(id);
    mStnID = new String(stnid);
    mPlat = new String(plat);
    mType = new String(type);
    mLon = lon;
    mLat = lat;
    mZMin = zmin;
    mZMax = zmax;
    if (zunits != null) {
      mZUnits = new String(zunits);
    }
    mStartDate = new GeoDate(stdate);
    mEndDate = new GeoDate(endDate);
    if (vars != null) {
      mVars = new String(vars);
    }
    if (units != null) {
      mUnits = new String(units);
    }
    if (lexicon != null) {
      mLexicon = new String(lexicon);
    }
  }

  public MyResultSet(String id, String plat, String stnid, String type, double lon, double lat, double zmin, double zmax, String zunits,
                     GeoDate date, String vars, String units, String lexicon) {
    mItemID = new String(id);
    mStnID = new String(stnid);
    mPlat = new String(plat);
    mType = new String(type);
    mLon = lon;
    mLat = lat;
    mZMin = zmin;
    mZMax = zmax;
    if (zunits != null) {
      mZUnits = new String(zunits);
    }
    mDate = new GeoDate(date);
    if (vars != null) {
      mVars = new String(vars);
    }
    if (units != null) {
      mUnits = new String(units);
    }
    if (lexicon != null) {
      mLexicon = new String(lexicon);
    }
  }

  public String getItemID() {
    return mItemID;
  }

  public String getStnID() {
    return mStnID;
  }

  public String getPlatform() {
    return mPlat;
  }

  public String getType() {
    return mType;
  }

  public double getLon() {
    return mLon;
  }

  public double getLat() {
    return mLat;
  }

  public double getZMin() {
    return mZMin;
  }

  public double getZMax() {
    return mZMax;
  }

  public String getZUnits() {
    return mZUnits;
  }

  public GeoDate getDate() {
    return mDate;
  }

  public String getVars() {
    return mVars;
  }

  public String getVarUnits() {
    return mUnits;
  }

  public String getLexicon() {
    return mLexicon;
  }

  public GeoDate getStartDate() {
    return mStartDate;
  }

  public GeoDate getEndDate() {
    return mEndDate;
  }
}

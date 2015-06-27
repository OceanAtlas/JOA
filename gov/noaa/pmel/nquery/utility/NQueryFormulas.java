/*
 * $Id: NQueryFormulas.java,v 1.27 2005/11/01 21:48:22 oz Exp $
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

import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.File;
import java.io.FileNotFoundException;
import javaoceanatlas.utility.UVCoordinate;
import java.awt.Container;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.specifications.NQIntegrationSpecification;
import gov.noaa.pmel.nquery.specifications.NQInterpolationSpecification;

public class NQueryFormulas {

  private static class PrefsNotifyStr extends HandlerBase {
    public void startDocument() throws SAXException {
    }

    public void startElement(String name, AttributeList amap) throws SAXException {
      if (name.equals("builtincalcprefs")) {
        boolean b0 = true, b1 = true, b2 = true;
        for (int i = 0; i < amap.getLength(); i++) {
          try {
            if (amap.getName(i).equals("calcmin")) {
            	NQueryConstants.DEFAULT_CALC_MIN = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("calcmax")) {
            	NQueryConstants.DEFAULT_CALC_MAX = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("calcdepthofmin")) {
            	NQueryConstants.DEFAULT_CALC_DEPTH_OF_MIN = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            if (amap.getName(i).equals("calcdepthofmax")) {
            	NQueryConstants.DEFAULT_CALC_DEPTH_OF_MAX = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("calcavg")) {
            	NQueryConstants.DEFAULT_CALC_AVERAGE = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("calcn")) {
            	NQueryConstants.DEFAULT_CALC_N = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("calcminnonmissingdepth")) {
            	NQueryConstants.DEFAULT_CALC_MIN_DEPTH_OF_NONMISSING = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("calcmaxnonmissingdepth")) {
            	NQueryConstants.DEFAULT_CALC_MAX_DEPTH_OF_NONMISSING = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            else if (amap.getName(i).equals("applycalcstousercalcs")) {
            	NQueryConstants.DEFAULT_APPLY_CALCS_TO_USER_CALCS = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
          }
          catch (Exception ex) {
          }
        }
      }
      else if (name.equals("databaseprefs")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("dburi")) {
            try {
              NQueryConstants.DEFAULT_DB_URI = new String(amap.getValue(i));
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("dbport")) {
            try {
            	NQueryConstants.DEFAULT_DB_PORT = new String(amap.getValue(i));
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("dbusername")) {
            try {
            	NQueryConstants.DEFAULT_DB_USERNAME = new String(amap.getValue(i));
            }
            catch (Exception ex) {
            }
          }
          if (amap.getName(i).equals("dbdefaultdir")) {
            try {
            	NQueryConstants.DEFAULT_DB_SAVE_DIR = new String(amap.getValue(i));
            }
            catch (Exception ex) {
            }
          }
        }
      }
      else if (name.equals("profileprefs")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("translate")) {
            try {
            	NQueryConstants.DEFAULT_TRANSLATE_LEXICON = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("convertdepth")) {
            try {
            	NQueryConstants.DEFAULT_CONVERT_DEPTH = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("converto2units")) {
            try {
            	NQueryConstants.DEFAULT_CONVERT_O2 = Boolean.valueOf(amap.getValue(i)).booleanValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("tolexicon")) {
            try {
              NQueryConstants.DEFAULT_LEXICON = Integer.valueOf(amap.getValue(i)).intValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("salinityvar")) {
            try {
            	NQueryConstants.DEFAULT_SALINITY_VARIABLE = Integer.valueOf(amap.getValue(i)).intValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("salinitysub")) {
            try {
            	NQueryConstants.DEFAULT_SALINITY_SUBSTITUTION = Integer.valueOf(amap.getValue(i)).intValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("o2var")) {
            try {
              NQueryConstants.DEFAULT_O2_VARIABLE = Integer.valueOf(amap.getValue(i)).intValue();
            }
            catch (Exception ex) {
            }
          }
          else if (amap.getName(i).equals("o2sub")) {
            try {
            	NQueryConstants.DEFAULT_O2_SUBSTITUTION = Integer.valueOf(amap.getValue(i)).intValue();
            }
            catch (Exception ex) {
            }
          }
        }
      }
    }

    public void characters(char[] ch, int start, int len) throws SAXException {}

    public void endElement(String name) throws SAXException {}
  }

  public static void readPreferences() throws Exception {
    // get the default preferences
    // xml-based preferences
    try {
      Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
      org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
      PrefsNotifyStr notifyStr = new PrefsNotifyStr();
      parser.setDocumentHandler(notifyStr);
      parser.parse("nqueryprefs.xml");
    }
    catch (Exception ex) {
      throw ex;
    }

    // the Dapper servers are presently hardwired
    NQueryConstants.DEFAULT_DAPPER_SERVERS.addElement("http://www.epic.noaa.gov:10100/dods");
    NQueryConstants.DEFAULT_DAPPER_SERVERS.addElement("http://apdrc2.soest.hawaii.edu:8080/dods/epic");
  }

  public static File getSupportFile(String name) throws FileNotFoundException {
    String dir = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    File nf = new File(dir, name);
    if (nf == null) {
      throw new FileNotFoundException();
    }
    return nf;
  }

  public static double zToPres(double inZ) {
    double zz, zzz;

    zz = inZ * inZ;
    zzz = inZ * inZ * inZ;
    return (1.0076 * inZ + 0.0000023487 * zz - (1.2887e-11 * zzz));
  }

  public static double presToZ(double inPres, double lat) {
    double a, b, c, z;

    a = 2.2e-06 * inPres;
    b = gravity(lat) + 0.05 * a;
    c = 1.0285 + a;
    z = inPres / (b * c);
    return z;
  }

  public static double gravity(double lat) {
    double sLatSQ, grav;

    sLatSQ = Math.sin(lat * 0.0174532925);
    sLatSQ = sLatSQ * sLatSQ;
    grav = 0.9780318 + 5.1859e-03 * sLatSQ;
    return grav;
  }

  public static double log10(double x) {
    return Math.log(x) / Math.log(10.0);
  }

  public static double TAND(double A) {
    return Math.tan(A * NQueryConstants.F);
  }

  public static double mpart(double lat) {
    double x;

    x = Math.sin(lat * NQueryConstants.F);
    return 7915.704468 * log10(TAND(45 + lat / 2)) - 23.268932 * x - 0.0525 * (x * x * x) -
        0.000213 * (x * x * x * x * x);
  }

  public static double ATAN2D(double A, double B) {
    return Math.atan2(A, B) / NQueryConstants.F;
  }

  public static double COSD(double A) {
    return Math.cos(A * NQueryConstants.F);
  }

  public static double SECD(double A) {
    return 1 / Math.cos(A * NQueryConstants.F);
  }

  public static double SIND(double A) {
    return Math.sin(A * NQueryConstants.F);
  }

  public static double CSCD(double A) {
    return 1 / SIND(A);
  }

  public static double HAVD(double A) {
    return (1 - COSD(A)) / 2;
  }

  public static double AHAVD(double A) {
    return Math.acos( -(A * 2 - 1)) / NQueryConstants.F;
  }

  public static double asinz(double con) {
    if (Math.abs(con) > 1.0) {
      if (con > 1.0) {
        con = 1.0;
      }
      else {
        con = -1.0;
      }
    }
    return Math.asin(con);
  }

  public static double calcMax(double[] inVals) {
    double max = -999999.0;
    int numValid = 0;
    for (int i = 0; i < inVals.length; i++) {
      if (NQueryFormulas.isMissing(inVals[i])) {
        continue;
      }
      if (inVals[i] > max) {
        max = inVals[i];
        numValid++;
      }
    }
    if (numValid > 0) {
      return max;
    }
    else {
      return NQueryConstants.MISSINGVALUE;
    }
  }

  public static double calcMin(double[] inVals) {
    double min = 9999999999.0;
    int numValid = 0;
    for (int i = 0; i < inVals.length; i++) {
      if (NQueryFormulas.isMissing(inVals[i])) {
        continue;
      }
      if (inVals[i] < min) {
        min = inVals[i];
        numValid++;
      }
    }
    if (numValid > 0) {
      return min;
    }
    else {
      return NQueryConstants.MISSINGVALUE;
    }
  }

  public static double calcMean(double[] inVals) {
    double sum = 0.0;
    int numValid = 0;
    for (int i = 0; i < inVals.length; i++) {
      if (NQueryFormulas.isMissing(inVals[i])) {
        continue;
      }

      sum += inVals[i];
      numValid++;
    }
    if (numValid > 0) {
      return sum / (double)numValid;
    }
    else {
      return NQueryConstants.MISSINGVALUE;
    }
  }

  public static int calcN(double[] inVals) {
    int numValid = 0;
    for (int i = 0; i < inVals.length; i++) {
      if (NQueryFormulas.isMissing(inVals[i])) {
        continue;
      }

      numValid++;
    }
    return numValid;
  }

  public static double calcDepthOfMin(double[] depthVals, double[] inVals) {
    double min = 9999999999.0;
    double minDepth = NQueryConstants.MISSINGVALUE;
    int numValid = 0;
    for (int i = 0; i < inVals.length; i++) {
      if (NQueryFormulas.isMissing(inVals[i])) {
        continue;
      }
      if (inVals[i] < min) {
        min = inVals[i];
        numValid++;
        minDepth = depthVals[i];
      }
    }
    return minDepth;
  }

  public static double calcDepthOfMax(double[] depthVals, double[] inVals) {
    double max = -9999999999.0;
    double maxDepth = NQueryConstants.MISSINGVALUE;
    int numValid = 0;
    for (int i = 0; i < inVals.length; i++) {
      if (NQueryFormulas.isMissing(inVals[i])) {
        continue;
      }
      if (inVals[i] > max) {
        max = inVals[i];
        numValid++;
        maxDepth = depthVals[i];
      }
    }
    return maxDepth;
  }

  public static double calcMaxDepthOfNonMissingValue(double[] depthVals, double[] inVals) {
    try {
      for (int i = inVals.length - 1; i >= 0; i++) {
        if (!NQueryFormulas.isMissing(inVals[i])) {
          return depthVals[i];
        }
      }
      return NQueryConstants.MISSINGVALUE;
    }
    catch (Exception ex) {
      return NQueryConstants.MISSINGVALUE;
    }
  }

  public static double calcMinDepthOfNonMissingValue(double[] depthVals, double[] inVals) {
    for (int i = 0; i < inVals.length; i++) {
      if (!NQueryFormulas.isMissing(inVals[i])) {
        return depthVals[i];
      }
    }
    return NQueryConstants.MISSINGVALUE;
  }

  /*  This calculates local potential temperature at reference_pressure
      using the Bryden 1973 polynomial for adiabatic lapse rate and
      Runge-Kutta 4th order integration algorithm.

      References:		Bryden, H., 1973, Deep-Sea Res., 20, 401-408.
          Fofonoff, N., 1977, Deep-Sea Res., 24, 489-491.

      Units:
        Pressure			P				decibars
        Temperature			T				degrees Celcius
        Salinity			S				psu (IPSS-78)
        Reference press.	Pref			decibars
        Potential tmp.		Theta			degrees Celcuis

      Check value:	Theta = 36.89073 C for S = 40 (psu), T = 40 deg. C,
          P = 10000 decibars, Pref = 0 decibars.
   */

  public static double theta(double salinity, double temperature, double pressure, double reference_pressure) {
    double S, T, p, h, xk, q;

    S = salinity;
    T = temperature;
    p = pressure;

    h = reference_pressure - p;
    xk = h * atg(S, T, p);
    T += 0.5 * xk;
    q = xk;
    p += 0.5 * h;
    xk = h * atg(S, T, p);
    T += 0.29289322 * (xk - q);
    q = 0.58578644 * xk + 0.121320344 * q;
    xk = h * atg(S, T, p);
    T += 1.707106781 * (xk - q);
    q = 3.414213562 * xk - 4.121320344 * q;
    p += 0.5 * h;
    xk = h * atg(S, T, p);
    T += (xk - 2.0 * q) / 6.0;

    return (T);
  }

  /*  This computes the adiabatic temperature gradient in degrees C per
      decibar.

      Reference:  Bryden, H., 1973, Deep Sea Res., 20, 401-408.

      Units:

       Pressure:			P			decibars
       Temperature:		T			degrees Celcius
       Salinity:			S			psu (IPSS-78)
       Adiabatic:			ATG			deg. C/decibar


      Check value:
        ATG = 3.255796E-4 C/dbar for S = 40 (psu), T = 40 deg. C,
        P = 10000 decibars.
   */

  public static double atg(double salinity, double temperature, double pressure) {
    double S, T, p, latg;

    S = salinity - 35.;
    T = temperature;
    p = pressure;

    latg = ((( -2.1687e-16 * T + 1.8676e-14) * T - 4.6206e-13) * p +
            ((2.7759e-12 * T - 1.1351e-10) * S + (( -5.4481e-14 * T + 8.733e-12) * T - 6.7795e-10) * T + 1.8741e-08)) *
        p + ( -4.2393e-08 * T + 1.8932e-06) * S + ((6.6228e-10 * T - 6.836e-08) * T + 8.5258e-06) * T + 3.5803e-05;

    return (latg);
  }

  /*		This calculates sigma (density - 1000 kg/m^3) at salinity,
     temperature, and pressure.  This will be potential density if
     temperature is potential temperature.  The routine uses the high
     pressure equation of state from Millero et al. 1980 and the one-
     atmosphere equation of state from Millero and Poisson 1981 as
     reported in Gill 1982.  The notation follows Millero et al. 1980
     and Millero and Poisson 1981.

     Note: the routine takes p in decibars and converts to bars for
     the calculations.

     References:	Millero, NQueryConstants.F.J., et al., 1980, Deep-Sea Res., 27A, 255-264.
     Millero, NQueryConstants.F.J. and Alain Poisson, 1981, Deep-Sea Res., 28A,
      625-629.
     Gill, A.E., 1982, Atmosphere-Ocean Dynamics, Academic
      Press, Inc., 662 pp.

     Input Units:
    S: psu	T: deg. C	p: decibars.

     Output Units:
    sigma: kg/m^3

     Check values:
    sigma = -.033249 	for S = 0,  T = 5,  p = 0
    sigma = 27.675465 	for S = 35, T = 5,  p = 0
    sigma = 62.538172 	for S = 35, T = 25, p = 10000.

   */

  public static double sigma(double salinity, double temperature, double pressure) {
    double sigma;
    double S, T, p, rootS;
    double a, b, c, d, e;
    double Aw, Bw, Kw;
    double A, B, C, Kzero, K;
    double rhoW, rhoZero;

    S = salinity;
    T = temperature;
    p = pressure / 10.;
    rootS = Math.sqrt(S);

    // rhoW is the density of pure water at temperature T.
    rhoW = ((((6.536332e-09 * T - 1.120083e-06) * T + 1.001685e-04) * T - 9.095290e-03) * T + 6.793952e-02) * T +
        999.842594;

    A = (((5.3875e-09 * T - 8.2467e-07) * T + 7.6438e-05) * T - 4.0899e-03) * T + 8.24493e-01;

    B = ( -1.6546e-06 * T + 1.0227e-04) * T - 5.72466e-03;

    C = 4.8314e-04;

    // rhoZero is the one-atmosphere density of seawater.
    rhoZero = (C * S + B * rootS + A) * S + rhoW;

    if (pressure == 0.0) {
      sigma = rhoZero - 1000.;
      return (sigma);
    }

    a = (( -6.1670e-05 * T + 1.09987e-02) * T - 6.03459e-01) * T + 54.6746;

    b = ( -5.3009e-04 * T + 1.6483e-02) * T + 7.944e-02;

    c = ( -1.6078e-06 * T - 1.0981e-05) * T + 2.2838e-03;

    d = 1.91075e-04;

    e = (9.1697e-10 * T + 2.0816e-08) * T - 9.9348e-07;

    Aw = (( -5.77905e-07 * T + 1.16092e-04) * T + 1.43713e-03) * T + 3.239908;

    Bw = (5.2787e-08 * T - 6.12293e-06) * T + 8.50935e-05;

    // Kw is the secant bulk modulus of pure water at temperature T.
    Kw = ((( -5.155288e-05 * T + 1.360477e-02) * T - 2.327105) * T + 148.4206) * T + 19652.21;

    // Kzero is the secant bulk modulus of seawater at one atmosphere.
    Kzero = (b * rootS + a) * S + Kw;

    // K is the secant bulk modulus of seawater at (S, T, p).
    K = ((e * p + d * rootS + c) * S + Bw * p + Aw) * p + Kzero;

    sigma = rhoZero / (1.0 - (p / K));

    sigma -= 1000.;

    return (sigma);

  }

  /*Listed below is the AOU routine we have been using.  It is based on the
    work of A-T Chen , Solubility Data Series, vol 7, Oxygen and Ozone.
    This version is in RPL from RS1.  The units for the input variables are:
   Temperature in degrees Kelvin, Salinity in ppt and dissolved oxygen in
    ml/l.  If oxygen is in umol use the factor 22393 ml/mol to convert. The
    ouput units on AOU are umol/l. */

  public static double computeAOU(double inTemp, double inSalt, double inO2, boolean O2Vol2Mass) {
    double Osat, AOU;

    // Sal = 34.309;
    // DO2 = 6.5;

    Osat = o2Saturation(inTemp, inSalt);
    if (O2Vol2Mass) {
      AOU = convertVol2Mass( -(inO2 - Osat));
    }
    else {
      AOU = -(inO2 - Osat);
    }
    return AOU;
  }

  public static double o2Saturation(double inTemp, double inSalt) {
    // units are in ml/l
    double Tkel, Sat, Osat;

    Tkel = inTemp + 273.15;
    Sat = -1268.9782 + 36063.19 / Tkel + 220.1832 * Math.log(Tkel) - 0.351299 * Tkel +
        inSalt * (0.006229 - 3.5912 / Tkel) + 0.00000344 * (inSalt * inSalt);

    Osat = Math.exp(Sat);
    return (Osat);
  }

  public static double convertVol2Mass(double inVol) {
    /* inVol is in ml/l, convert to umol/l
       (ml/l) / (22393 (ml/mol) * 1000000) */
    return inVol / 22393 * 1000000;
  }

  public static double computeNO(double inNO3, double inO2, boolean O2Vol2Mass) {
    double NO, tO2 = inO2;

    if (O2Vol2Mass) {
      tO2 = 43.549 * inO2;
    }
    NO = 8.7864 * inNO3 + tO2;
    return NO;
  }

  public static double computePO(double inPO4, double inO2, boolean O2Vol2Mass) {
    double PO, tO2 = inO2;

    if (O2Vol2Mass) {
      tO2 = 43.549 * inO2;
    }
    PO = 170.8467 * inPO4 + tO2;
    return PO;
  }

  public static double heatCapacity(double s, double t) {

    /*	a polynomial in salinity and temperature to compute the heat capacity of
       sea water in Joules per kilogram per degree Kelvin at atmospheric pressure.
       Equation taken from Millero et al. JGR v.78 #20 4499-4507 (1973).
       Check that heatCapacity( 40, 40 ) == 3981 */
    double t2, t3, t4, c, c32, cpOfT, cpOfST;
    c = s / 1.80655;
    t2 = t * t;
    t3 = t * t2;
    t4 = t * t3;
    c32 = Math.sqrt(c * c * c);

    cpOfT = 4217.4 - 3.720283 * t + 0.1412855 * t2 - 2.654387e-3 * t3 + 2.093236e-5 * t4;

    cpOfST = cpOfT + c * ( -13.81 + 0.1938 * t - 0.0025 * t2) + c32 * (0.43 - 0.0099 * t + 0.00013 * t2);

    return cpOfST;
  }

  public static double specificVolumeAnomoly(double salinity, double theta, double pressure) {
    double R3500 = 1028.1063, R4 = 4.8314e-04, DR350 = 28.106331, V350P;
    double S, T, P, rootS;
    double A, B, C, D, E, B1, A1, DK, K35, DR35P;
    double AW, BW, KW;
    double R2, R3, K0;
    double R1, SIG, SVA, SVAN, GAM, PK, DVAN;

    /* compute sigma */
    S = salinity;
    T = theta;
    P = pressure / 10.;
    rootS = Math.sqrt(S);

    /*	rhoW is the density of pure water at temperature T.	*/
    R1 = ((((6.536332e-09 * T - 1.120083e-06) * T + 1.001685e-04) * T - 9.095290e-03) * T + 6.793952e-02) * T -
        28.263737;

    R2 = (((5.3875e-09 * T - 8.2467e-07) * T + 7.6438e-05) * T - 4.0899e-03) * T + 8.24493e-01;

    R3 = ( -1.6546e-06 * T + 1.0227e-04) * T - 5.72466e-03;

    /*	sig is the one-atmosphere density of seawater.	*/
    SIG = (R4 * S + R3 * rootS + R2) * S + R1;

    /* specific volume at atmospheric pressure */
    V350P = 1.0 / R3500;
    SVA = -SIG * V350P / (R3500 + SIG);
    //*sigma = SIG + DR350;
     SVAN = (SVA * 1.0e+8);
     if (P == 0) {
      return SVAN;
    }

    /* compute a compression term for pressures > 0 */
    E = (9.1697e-10 * T + 2.0816e-08) * T - 9.9348e-07;

    BW = (5.2787e-08 * T - 6.12293e-06) * T + 3.47718e-05;

    B = BW + E * S;

    D = 1.91075e-04;

    C = ( -1.6078e-06 * T - 1.0981e-05) * T + 2.2838e-03;

    AW = (( -5.77905e-07 * T + 1.16092e-04) * T + 1.43713e-03) * T - 0.1194975;

    A = (D * rootS + C) * S + AW;

    B1 = ( -5.3009e-04 * T + 1.6483e-02) * T + 7.944e-02;

    A1 = (( -6.1670e-05 * T + 1.09987e-02) * T - 0.603459) * T + 54.6746;

    /*	Kw is the secant bulk modulus of pure water at temperature T.	*/
    KW = ((( -5.155288e-05 * T + 1.360477e-02) * T - 2.327105) * T + 148.4206) * T - 1930.06;

    /*	Kzero is the secant bulk modulus of seawater at one atmosphere.	*/
    K0 = (B1 * rootS + A1) * S + KW;

    /*	K35 is the secant bulk modulus of seawater at (35, 0, p).	*/
    DK = (B * P + A) * P + K0;
    K35 = (5.03217e-05 * P + 3.359406) * P + 21582.27;
    GAM = P / K35;
    PK = 1.0 - GAM;
    SVA = SVA * PK + (V350P + SVA) * P * DK / (K35 * (K35 + DK));

    /* scale specific volume anomoly to nmally reported units */
    SVAN = SVA * 1.0e08;
    V350P = V350P * PK;

    /* compute density anomoly wrt 1000 kg/m**3 */
    DR35P = GAM / V350P;
    DVAN = SVA / (V350P * (V350P + SVA));
    //*sigma = DR350 + DR35P - DVAN;
     return SVAN;
  }

  public static double soundVelocity(double p0, double t, double s) {
    /* sound velocity:Chen and Millero (1977, JASA,62,1129-1135)
       c(m/s), P(dbar), t(C), s(psu)
       check: 1731.9954 m/s (10,000,40,40)!!
       convert to bars and sqrt(salinity) */

    double p, sr, d, b1, b0, b, a3, a2, a1, a0, a, c3, c2, c1, c0, c, svl;

    p = p0 / 10.0;
    sr = Math.sqrt(Math.abs(s));

    d = 1.727e-3 - 7.9836e-6 * p;

    /* s**3/2 term */

    b1 = 7.3637e-05 + 1.7945e-07 * t;
    b0 = -1.922e-02 - 4.42e-05 * t;
    b = b0 + b1 * p;

    /* s**1 term */

    a3 = ( -3.389e-13 * t + 6.649e-12) * t + 1.100e-10;
    a2 = ((7.988e-12 * t - 1.6002e-10) * t + 9.1041e-9) * t - 3.9064e-7;
    a1 = ((( -2.0122e-10 * t + 1.0507e-8) * t - 6.4885e-8) * t - 1.2580e-5) * t + 9.4742e-5;
    a0 = ((( -3.21e-8 * t + 2.006e-6) * t + 7.164e-5) * t - 1.262e-2) * t + 1.389;
    a = ((a3 * p + a2) * p + a1) * p + a0;

    /* s**0 term */

    c3 = ( -2.3643e-12 * t + 3.8504e-10) * t - 9.7729e-9;
    c2 = (((1.0405e-12 * t - 2.5335e-10) * t + 2.5974e-8) * t - 1.7107e-6) * t + 3.1260e-5;
    c1 = ((( -6.1185e-10 * t + 1.3621e-7) * t - 8.1788e-6) * t + 6.8982e-4) * t + 0.153563;
    c0 = ((((3.1464e-9 * t - 1.47800e-6) * t + 3.342e-4) * t - 5.80852e-2) * t + 5.03711) * t + 1402.388;
    c = ((c3 * p + c2) * p + c1) * p + c0;

    svl = c + (a + b * sr + d * s) * s;

    return svl;
  }

  /* Calculates Spiciness from manuscript by Flimant (unp.) 1987 */

  public static double computeSpiciness(double inTemp, double inSalt) {
    double spic;
    double[] temp = new double[5];
    double[] salt = new double[5];
    double[][] b = { {0.1609705, 0.6542397, 5.222258e-4, -2.586742e-5, 7.565157e-7}, { -8.007345e-2, 5.309506e-3,
        -9.612388e-5, 3.211527e-6, -4.610513e-8}, {1.081912e-2, -1.561608e-4, 3.774240e-6, -1.150394e-7, 1.146084e-9},
        { -1.451748e-4, 3.485063e-6, -1.387056e-7, 3.737360e-9, -2.967108e-11}, {1.219904e-6, -3.591075e-8, 1.953475e-9,
        -5.279546e-11, 4.227375e-13}
    };

    temp[0] = 1;
    salt[0] = 1;
    spic = 0;
    for (int i = 1; i <= 4; i++) {
      temp[i] = inTemp * temp[i - 1];
      salt[i] = inSalt * salt[i - 1];
    }

    for (int i = 0; i <= 4; i++) {
      for (int j = 0; j <= 4; j++) {
        spic += b[i][j] * temp[i] * salt[j];
      }
    }
    return spic;
  }

  public static double gaussSmooth(double zLength, int i, int frst, int last, double[] rho, double[] Z) {
    double depth;
    double weight;
    double sumWeight;
    double zL2;
    double deltaZ;
    double result;

    depth = Z[i];
    if (NQueryFormulas.isMissing(depth)) {
      return NQueryConstants.MISSINGVALUE;
    }
    zL2 = zLength * zLength;
    sumWeight = 0.0;
    result = 0.0;

    for (int k = frst; k <= last; k++) {
      if (NQueryFormulas.isMissing(Z[k])) {
        continue;
      }
      if (NQueryFormulas.isMissing(rho[k])) {
        continue;
      }
      deltaZ = depth - Z[k];
      deltaZ = deltaZ * deltaZ;
      weight = Math.exp( -deltaZ / zL2);
      sumWeight += weight;
      result += weight * rho[k];
    }

    result = (sumWeight > 0.0) ? result / sumWeight : NQueryConstants.MISSINGVALUE;
    return result;
  }

  public static double[] acousticTravelTime(double[] pres, double[] temp, double[] salt, double lat) {
    double sumATT; // sum of trapezoidal panels of delta(travel time)
    double avgSpeed; // average sound speed between bottles (i) & (i+1).
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double soundVel1, soundVel2; // sound velocity at bottles (i) & (i+1).
    double z1, z2; // depth at bottles (i) & (i+1).
    double deltaZ; // depth difference between bottles (i) & (i+1).
    int nBottles; // number of bottles at this station.
    int sVar, tVar, pVar; // indices of salinity, temperature, & pressure in the
    // variable blocks of each bottle.
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    nBottles = pres.length;
    double[] result = new double[nBottles];

    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    salt1 = salt[0];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[0];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[0];
    p1bad = NQueryFormulas.isMissing(pres1);

    if (s1bad || t1bad || p1bad) {
      // Routine quits when it encounters first bad value.
      return result;
    }

    sumATT = 0; // If first observation is at surface,
    if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface water has uniform properties.
      z1 = presToZ(pres1, lat);
      soundVel1 = soundVelocity(pres1, temp1, salt1);
      soundVel2 = soundVel1;
      deltaZ = z1 - 0;
      avgSpeed = 0.5 * (soundVel1 + soundVel2);
      sumATT = sumATT - deltaZ / avgSpeed;
    }
    result[0] = sumATT;

    for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to bottle(nBottles - 1). 					// loop from bottle(0) to bottle(nBottles - 1).

      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[i - 1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[i];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[i - 1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[i];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[i - 1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[i];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        // Routine quits when it encounters first bad value
        break;
      }

      z1 = presToZ(pres1, lat);
      z2 = presToZ(pres2, lat);
      soundVel1 = soundVelocity(pres1, temp1, salt1);
      soundVel2 = soundVelocity(pres2, temp2, salt2);
      deltaZ = z2 - z1;
      avgSpeed = .5 * (soundVel1 + soundVel2); // Evaluate the integrand.
      sumATT = sumATT - deltaZ / avgSpeed;
      result[i] = sumATT;
    } // End integration down the list of bottles.

    return result;
  } //End acousticTravelTime()

  public static double[] heatStorage(double[] pres, double[] temp, double[] salt, double lat) {
    double sumHeat; // sum of trapezoidal panels of delta(heat storage).
    double deltaHeat; // delta(heat storage).
    double avgPoTemp; // mean potential temperature between bottles (i) & (i+1).
    double avgPotDensity; // average potential density between bottles (i) & (i+1).
    double avgPotCp; // mean potential heat capacity between bottles (i) & (i+1).
    double poTemp1, poTemp2; // potential temperature at bottles (i) & (i+1).
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double z1, z2; // depth at bottles (i) & (i+1).
    double deltaZ; // depth difference between bottles (i) & (i+1).
    int nBottles; // number of bottles at this station.
    int sVar, tVar, pVar; // indices of salinity, temperature, & pressure in the
    // variable blocks of each bottle.
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    nBottles = pres.length;
    double[] result = new double[nBottles];
    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    salt1 = salt[0];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[0];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[0];
    p1bad = NQueryFormulas.isMissing(pres1);

    if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first bad value.
      return result;
    }

    sumHeat = 0; // If first observation is at surface,
    if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface water has uniform properties.
      z2 = presToZ(pres1, lat);
      z1 = 0;
      poTemp1 = theta(salt1, temp1, pres1, 0);
      avgPoTemp = poTemp1;
      avgPotDensity = 1000 + sigma(salt1, poTemp1, pres1);
      avgPotCp = heatCapacity(salt1, poTemp1);
      deltaZ = z2 - z1;
      deltaHeat = deltaZ * avgPoTemp * avgPotDensity * avgPotCp; // integrand.
      sumHeat = sumHeat - NQueryConstants.GIGAJOULES * deltaHeat;
    }
    result[0] = sumHeat;

    for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to bottle(nBottles - 1). 					// loop from bottle(0) to bottle(nBottles - 1).

      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[i - 1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[i];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[i - 1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[i];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[i - 1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[i];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        //Routine quits when it encounters first bad value
        break;
      }

      z1 = presToZ(pres1, lat);
      z2 = presToZ(pres2, lat);
      poTemp1 = theta(salt1, temp1, pres1, 0);
      poTemp2 = theta(salt2, temp2, pres2, 0);
      avgPoTemp = .5 * (poTemp1 + poTemp2);
      avgPotDensity = 1000 + .5 * (sigma(salt1, poTemp1, pres1) + sigma(salt2, poTemp2, pres2));
      avgPotCp = .5 * (heatCapacity(salt1, poTemp1) + heatCapacity(salt2, poTemp2));
      deltaZ = z2 - z1;
      deltaHeat = deltaZ * avgPoTemp * avgPotDensity * avgPotCp; // integrand.
      sumHeat = sumHeat - NQueryConstants.GIGAJOULES * deltaHeat;
      result[i] = sumHeat;
    } // End integration down the list of bottles.

    return result;
  }

  public static double[] geopotentialAnomaly(double[] pres, double[] temp, double[] salt, double lat) {
    double sumGPA; // sum of trapezoidal panels of delta(geopotential anomaly).
    double deltaGPA; // delta(geopotential anomaly).
    double deltaPres; // pressure difference between bottles (i) & (i+1).
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double SVA1, SVA2; // specific volume anomaly at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double dummy;
    int nBottles; // number of bottles at this station.
    int sVar, tVar, pVar; // indices of salinity, temperature, & pressure in the
    // variable blocks of each bottle.
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    nBottles = pres.length;
    double[] result = new double[nBottles];

    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if initial data are valid
    salt1 = salt[0];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[0];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[0];
    p1bad = NQueryFormulas.isMissing(pres1);

    if (s1bad || t1bad || p1bad) {
      // Routine quits when it encounters bad first value.
      return result;
    }

    sumGPA = 0; // If first observation is at surface,
    if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface water has uniform properties.
      SVA1 = NQueryConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
      SVA2 = SVA1;
      deltaPres = NQueryConstants.DB2PASCALSI * (pres1 - 0);
      deltaGPA = .5 * (SVA1 + SVA2) * deltaPres;
      sumGPA = sumGPA - deltaGPA;
    }
    result[0] = sumGPA;

    for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to bottle(nBottles - 1). 					// loop from bottle(0) to bottle(nBottles - 1).
      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[i - 1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[i];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[i - 1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[i];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[i - 1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[i];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        //Routine quits when it encounters bad first value
        break;
      }

      SVA1 = NQueryConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
      SVA2 = NQueryConstants.SVA2SI * specificVolumeAnomoly(salt2, temp2, pres2);
      deltaPres = NQueryConstants.DB2PASCALSI * (pres2 - pres1);
      deltaGPA = .5 * (SVA1 + SVA2) * deltaPres; // Evaluate the integrand.
      sumGPA = sumGPA - deltaGPA;
      result[i] = sumGPA;
    } // End integration down the list of bottles.

    return result;
  }

  public static double[] potentialEnergyAnomaly(double[] pres, double[] temp, double[] salt, double lat) {
    double sumPEA; // sum of trapezoidal panels of delta(potentialEnergy anomaly).
    double deltaPEA; // delta(potentialEnergy anomaly).
    double deltaPres; // pressure difference between bottles (i) & (i+1).
    double avgPres; // average pressure of bottles (i) & (i+1).
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double SVA1, SVA2; // specific volume anomaly at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    int nBottles; // number of bottles at this station.
    int sVar, tVar, pVar; // indices of salinity, temperature, & pressure in the
    // variable blocks of each bottle.
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    nBottles = pres.length;
    double[] result = new double[nBottles];

    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    salt1 = salt[0];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[0];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[0];
    p1bad = NQueryFormulas.isMissing(pres1);

    if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first bad value.
      return result;
    }

    sumPEA = 0; // If first observation is at surface,
    if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface water has uniform properties.
      SVA1 = NQueryConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
      SVA2 = SVA1;
      pres2 = pres1;
      pres1 = 0;
      deltaPres = NQueryConstants.DB2PASCALSI * (pres2 - pres1);
      avgPres = NQueryConstants.DB2PASCALSI * (pres2 + pres1) * .5;
      deltaPEA = (.5 * (SVA1 + SVA2) * avgPres * deltaPres) / NQueryConstants.GRAVITY;
      sumPEA = sumPEA - NQueryConstants.MILLION * deltaPEA;
    }
    result[0] = sumPEA;

    for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to bottle(nBottles - 1). 					// loop from bottle(0) to bottle(nBottles - 1).
      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[i - 1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[i];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[i - 1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[i];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[i - 1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[i];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        // Routine quits when it encounters bad first value
        break;
      }

      SVA1 = NQueryConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
      SVA2 = NQueryConstants.SVA2SI * specificVolumeAnomoly(salt2, temp2, pres2);
      deltaPres = NQueryConstants.DB2PASCALSI * (pres2 - pres1);
      avgPres = NQueryConstants.DB2PASCALSI * (pres2 + pres1) * .5;
      deltaPEA = (.5 * (SVA1 + SVA2) * avgPres * deltaPres) / NQueryConstants.GRAVITY; // Evaluate integrand.
      sumPEA = sumPEA - NQueryConstants.MILLION * deltaPEA;
      result[i] = sumPEA;
    } // End integration down the list of bottles.

    return result;
  }

  public static double[] BouyancyFrequency(double[] inpres, double[] intemp, double[] insalt, double inlat,
                                           double zLength, boolean n2Flag, boolean potVort) {
    double salt; // a salt observation
    double temp;
    double pres;
    int frst;
    int last;
    boolean sOK; // logical flags signaling missing values.
    boolean tOK;
    boolean pOK;
    double twiceZ; // twice zLength, the e-folding length
    double N; // bouyancy frequency
    double coriolis; // coriolis factor: 2 ½ sin(phi)
    double phi; // latitude in radians
    double z1, z2; // a pair of depths
    double rho1, rho2; // a pair of densities
    double lat = inlat;

    phi = lat * NQueryConstants.F; // NQueryConstants.F is degrees to radians
    coriolis = 2 * (1.0 / 24.0) * Math.sin(phi); // *cycles* per *hour*

    // Allocate temporary memory.
    int numObs = inpres.length;
    double[] result = new double[numObs];
    double[] rho = new double[numObs];
    double[] rhoSmth = new double[numObs];
    double[] Z = new double[numObs];
    double[] DrhoDz = rho;

    for (int i = 0; i < numObs; i++) {
      // Initialize data arrays to missing
      result[i] = NQueryConstants.MISSINGVALUE;
      rho[i] = NQueryConstants.MISSINGVALUE;
      rhoSmth[i] = NQueryConstants.MISSINGVALUE;
      Z[i] = NQueryConstants.MISSINGVALUE;
    }

    for (int i = 0; i < numObs; i++) {
      // compute densities loop
      // get observations
      salt = insalt[i];
      temp = intemp[i];
      pres = inpres[i];

      // Test to see if data are valid
      sOK = !NQueryFormulas.isMissing(salt);
      tOK = !NQueryFormulas.isMissing(temp);
      pOK = !NQueryFormulas.isMissing(pres);

      if (sOK && tOK && pOK) {
        rho[i] = 1000.0 + NQueryFormulas.sigma(salt, temp, 0); //0 used to be pres
      }

      if (pOK) {
        Z[i] = NQueryFormulas.presToZ(pres, lat);
      }

    } // End compute densities loop

    twiceZ = 2.0 * zLength;
    frst = 0;
    last = 0;
    for (int i = 0; i < numObs; i++) { // smooth the densities loop
      for (int j = i; j >= 0; j--) { // Find index of first observation
        frst = j; // within smoothing length.
        if (NQueryFormulas.isMissing(Z[j])) {
          continue;
        }
        if (twiceZ < Math.abs(Z[i] - Z[frst])) {
          break;
        }
      }

      for (int j = i; j < numObs; j++) { // Find index of last observation
        last = j; // within smoothing length.
        if (NQueryFormulas.isMissing(Z[j])) {
          continue;
        }
        if (twiceZ < Math.abs(Z[i] - Z[last])) {
          break;
        }
      }

      if (zLength > 0) {
        rhoSmth[i] = gaussSmooth(zLength, i, frst, last, rho, Z);
      }
      else {
        rhoSmth[i] = rho[i];
      }

    } // End smooth the densities loop

    for (int i = 0; i < numObs - 1; i++) { // differentiate densities loop
      DrhoDz[i] = NQueryConstants.MISSINGVALUE;
      z1 = Z[i];
      z2 = Z[i + 1];
      rho1 = rhoSmth[i];
      rho2 = rhoSmth[i + 1];
      if (NQueryFormulas.isMissing(z1) || NQueryFormulas.isMissing(z2)) {
        continue;
      }
      if (NQueryFormulas.isMissing(rho1) || NQueryFormulas.isMissing(rho2)) {
        continue; // *rho == *DrhoDz, so
      }
      DrhoDz[i] = (rho2 - rho1) / (z2 - z1); // this overwrites rho[].

    } // End differentiate densities loop

    Z[0] = DrhoDz[0]; // Overwrite Z, no longer needed.
    DrhoDz[numObs - 1] = DrhoDz[numObs - 2];
    for (int i = 0; i < numObs - 1; i++) { // interpolate derivatives loop
      Z[i + 1] = NQueryConstants.MISSINGVALUE;
      z1 = DrhoDz[i];
      z2 = DrhoDz[i + 1];
      if (NQueryFormulas.isMissing(z1) || NQueryFormulas.isMissing(z2)) {
        continue;
      }
      Z[i + 1] = .5 * (z1 + z2);

    } // End interpolate derivatives loop

    DrhoDz = Z;

    for (int i = 0; i < numObs; i++) { // compute N, N^2, or f*N^2/g loop.
      if (NQueryFormulas.isMissing(DrhoDz[i])) {
        continue;
      }
      if (NQueryFormulas.isMissing(rhoSmth[i])) {
        continue;
      }
      N = (NQueryConstants.HOUR / NQueryConstants.TWOPI) *
          Math.sqrt(Math.abs((NQueryConstants.GRAVITY * (DrhoDz[i] / rhoSmth[i]))));
      if (n2Flag) {
        result[i] = N * N;
      }
      else if (potVort) {
        result[i] = Math.abs(coriolis * N * N / NQueryConstants.GRAVITY);
      }
      else {
        result[i] = N;
      }

    } // End compute N, N^2, or f*N^2/g loop

    for (int i = 0; i < numObs; i++) { // Scan for a valid entry
      if (!NQueryFormulas.isMissing(result[i])) {
        break;
      }
    }

    double max = result[0]; // Scan for largest and smallest values.
    double min = max;
    for (int i = 0; i < numObs; i++) {
      if (NQueryFormulas.isMissing(result[i])) {
        continue;
      }
      max = result[i] > max ? result[i] : max;
      min = result[i] < min ? result[i] : min;
    }
    return result;
  } // End BouyancyFrequency().

  public static UVCoordinate acousticTravelTime(double[] insalt, double[] intemp, double[] inpres, int numObs,
                                                double lat, double[] result) {
    double sumATT; // sum of trapezoidal panels of delta(travel time)
    double avgSpeed; // average sound speed between bottles (i) & (i+1).
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double soundVel1, soundVel2; // sound velocity at bottles (i) & (i+1).
    double z1, z2; // depth at bottles (i) & (i+1).
    double deltaZ; // depth difference between bottles (i) & (i+1).
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    for (int i = 0; i < numObs; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    salt1 = insalt[0];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = intemp[0];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = inpres[0];
    p1bad = NQueryFormulas.isMissing(pres1);

    if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first bad value.
      return new UVCoordinate(NQueryConstants.MISSINGVALUE, NQueryConstants.MISSINGVALUE);
    }

    sumATT = 0; // If first observation is at surface,
    if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface water has uniform properties.
      z1 = presToZ(pres1, lat);
      soundVel1 = soundVelocity(pres1, temp1, salt1);
      soundVel2 = soundVel1;
      deltaZ = z1 - 0;
      avgSpeed = .5 * (soundVel1 + soundVel2);
      sumATT = sumATT - deltaZ / avgSpeed;
    }
    result[0] = sumATT;

    for (int i = 1; i < numObs; i++) { // Integrate from bottle(1) to bottle(NQueryConstants - 1). 					// loop from bottle(0) to bottle(NQueryConstants - 1).
      // Test to see if data are valid, rescaling valid data only
      salt1 = insalt[i - 1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = insalt[i];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = intemp[i - 1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = intemp[i];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = inpres[i - 1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = inpres[i];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        /* Routine quits when it encounters first bad value */
        break;
      }

      z1 = presToZ(pres1, lat);
      z2 = presToZ(pres2, lat);
      soundVel1 = soundVelocity(pres1, temp1, salt1);
      soundVel2 = soundVelocity(pres2, temp2, salt2);
      deltaZ = z2 - z1;
      avgSpeed = .5 * (soundVel1 + soundVel2); // Evaluate the integrand.
      sumATT = sumATT - deltaZ / avgSpeed;
      result[i] = sumATT;
    } // End integration down the list of bottles.

    double max = result[0]; // Scan for largest and smallest values.
    double min = max;
    for (int i = 0; i < numObs; i++) {
      if (NQueryFormulas.isMissing(result[i])) {
        break;
      }
      max = result[i] > max ? result[i] : max;
      min = result[i] < min ? result[i] : min;
    }

    return new UVCoordinate(min, max);
  }

  public static double[] alpha(double[] pres, double[] temp, double[] salt) {
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double saltAvg, tempAvg, presAvg;
    double rho1, rho2, rhom, drhodT;
    double alpha;
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    int nBottles = pres.length;
    double[] result = new double[nBottles];
    for (int i = 0; i < nBottles; i++) {
      // Initialize data array with missing values.
      result[i] = NQueryConstants.MISSINGVALUE;
    }

    for (int i = 0; i < nBottles - 1; i++) {
      // loop from bottle(0) to bottle(nBottles - 1).
      int bot_1 = i;
      int bot_2 = i + 1;

      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[bot_1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[bot_2];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[bot_1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[bot_2];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[bot_1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[bot_2];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        continue;
      }

      // compute the averages
      saltAvg = 0.5 * (salt1 + salt2);
      tempAvg = 0.5 * (temp1 + temp2);
      presAvg = 0.5 * (pres1 + pres2);

      // compute the densities
      rho1 = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, temp1, presAvg, presAvg), presAvg);
      rho2 = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, temp2, presAvg, presAvg), presAvg);
      rhom = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

      // compute the derivative
      if (temp2 != temp1 && rho2 != rho1) {
        drhodT = (rho2 - rho1) / (temp2 - temp1);
        alpha = -(1 / rhom) * drhodT;

        // scale up by factor of 100 for better plotting
        alpha *= 100.0;
      }
      else {
        alpha = NQueryConstants.MISSINGVALUE; // missing value
      }

      // store result
      result[i] = alpha;
    }
    return result;
  }

  public static double[] beta(double[] pres, double[] temp, double[] salt) {
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double saltAvg, tempAvg, presAvg;
    double rho1, rho2, rhom, drhodS;
    double beta;
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    int nBottles = pres.length;
    double[] result = new double[nBottles];

    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    int bot_11 = 0;
    salt1 = salt[bot_11];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[bot_11];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[bot_11];
    p1bad = NQueryFormulas.isMissing(pres1);
    if (s1bad || t1bad || p1bad) {
      // Routine quits when it encounters first bad value.
      return result;
    }

    for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to bottle(nBottles - 1).
      int bot_1 = i; // Get the variables of bottle (i).
      int bot_2 = i + 1; // Get the variables of bottle (i+1).

      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[bot_1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[bot_2];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[bot_1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[bot_2];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[bot_1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[bot_2];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        continue;
      }

      // compute the averages
      saltAvg = 0.5 * (salt1 + salt2);
      tempAvg = 0.5 * (temp1 + temp2);
      presAvg = 0.5 * (pres1 + pres2);

      rho1 = NQueryFormulas.sigma(salt1, NQueryFormulas.theta(salt1, tempAvg, presAvg, presAvg), presAvg);
      rho2 = NQueryFormulas.sigma(salt2, NQueryFormulas.theta(salt2, tempAvg, presAvg, presAvg), presAvg);
      rhom = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

      // compute the derivative
      if (salt2 != salt1 && rho2 != rho1) {
        drhodS = (rho2 - rho1) / (salt2 - salt1);
        beta = -(1 / rhom) * drhodS;

        // scale up by factor of 100
        beta *= 100.0;
      }
      else {
        beta = -99;
      }

      // calculate alpha
      result[i] = beta;
    }
    return result;
  }

  public static double[] alphadTdZ(double[] pres, double[] temp, double[] salt) {
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double saltAvg, tempAvg, presAvg;
    double rho1, rho2, rhom, drhodT;
    double alpha;
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    int nBottles = pres.length;
    int bot_11 = 0;

    double[] result = new double[nBottles];
    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    salt1 = salt[bot_11];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[bot_11];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[bot_11];
    p1bad = NQueryFormulas.isMissing(pres1);
    if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first bad value.
      return result;
    }

    for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to bottle(nBottles - 1).
      int bot_1 = i; // Get the variables of bottle (i).
      int bot_2 = i + 1; // Get the variables of bottle (i+1).

      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[bot_1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[bot_2];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[bot_1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[bot_2];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[bot_1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[bot_2];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        continue;
      }

      // compute the averages
      saltAvg = 0.5 * (salt1 + salt2);
      tempAvg = 0.5 * (temp1 + temp2);
      presAvg = 0.5 * (pres1 + pres2);

      // compute the densities
      rho1 = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, temp1, presAvg, presAvg), presAvg);
      rho2 = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, temp2, presAvg, presAvg), presAvg);
      rhom = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

      // compute the derivative
      if (temp2 != temp1 && pres2 != pres1 && rhom != 0) {
        drhodT = (rho2 - rho1) / (temp2 - temp1);
        alpha = -(1 / rhom) * drhodT;

        // multiply by the derivative dT/dZ
        alpha = alpha * ((temp2 - temp1) / (pres2 - pres1));

        // scale up by factor of 1000 for better plotting
        alpha *= 1000.0;
      }
      else {
        alpha = NQueryConstants.MISSINGVALUE; // missing value
      }

      // store result
      result[i] = alpha;
    }
    return result;
  }

  public static double[] betadSdZ(double[] pres, double[] temp, double[] salt) {
    double salt1, salt2; // salinity at bottles (i) & (i+1).
    double temp1, temp2; // temperature at bottles (i) & (i+1).
    double pres1, pres2; // pressure at bottles (i) & (i+1).
    double saltAvg, tempAvg, presAvg;
    double rho1, rho2, rhom, drhodS;
    double beta;
    boolean s1bad, s2bad; // logical flags signaling missing values.
    boolean t1bad, t2bad; // logical flags signaling missing values.
    boolean p1bad, p2bad; // logical flags signaling missing values.

    int nBottles = pres.length;
    int bot_11 = 0;
    double[] result = new double[nBottles];
    for (int i = 0; i < nBottles; i++) {
      result[i] = NQueryConstants.MISSINGVALUE; // Initialize data array with missing values.
    }

    // Test to see if data are valid
    salt1 = salt[bot_11];
    s1bad = NQueryFormulas.isMissing(salt1);
    temp1 = temp[bot_11];
    t1bad = NQueryFormulas.isMissing(temp1);
    pres1 = pres[bot_11];
    p1bad = NQueryFormulas.isMissing(pres1);
    if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first bad value.
      return result;
    }

    for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to bottle(nBottles - 1).
      int bot_1 = i; // Get the variables of bottle (i).
      int bot_2 = i + 1; // Get the variables of bottle (i+1).

      // Test to see if data are valid, rescaling valid data only
      salt1 = salt[bot_1];
      s1bad = NQueryFormulas.isMissing(salt1);
      salt2 = salt[bot_2];
      s2bad = NQueryFormulas.isMissing(salt2);
      temp1 = temp[bot_1];
      t1bad = NQueryFormulas.isMissing(temp1);
      temp2 = temp[bot_2];
      t2bad = NQueryFormulas.isMissing(temp2);
      pres1 = pres[bot_1];
      p1bad = NQueryFormulas.isMissing(pres1);
      pres2 = pres[bot_2];
      p2bad = NQueryFormulas.isMissing(pres2);

      if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
        continue;
      }

      // compute the averages
      saltAvg = 0.5 * (salt1 + salt2);
      tempAvg = 0.5 * (temp1 + temp2);
      presAvg = 0.5 * (pres1 + pres2);

      rho1 = NQueryFormulas.sigma(salt1, NQueryFormulas.theta(salt1, tempAvg, presAvg, presAvg), presAvg);
      rho2 = NQueryFormulas.sigma(salt2, NQueryFormulas.theta(salt2, tempAvg, presAvg, presAvg), presAvg);
      rhom = NQueryFormulas.sigma(saltAvg, NQueryFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

      // compute the derivative
      if (salt2 != salt1 && pres2 != pres1 && rhom != 0) {
        drhodS = (rho2 - rho1) / (salt2 - salt1);
        beta = -(1 / rhom) * drhodS;

        // mutiply by derivative dS/dZ
        beta = beta * ((salt2 - salt1) / (pres2 - pres1));

        // scale up by factor of 1000
        beta *= 1000.0;
      }
      else {
        beta = -99;
      }

      // calculate alpha
      result[i] = beta;

    }
    return result;
  }

  public static double mixedLayerDifference(double[] pres, double[] testparamvals, double testDepth, double delta) {
    int nBottles = pres.length;
    boolean tbad; // logical flags signaling missing values.
    boolean p1bad; // logical flags signaling missing values.

    //find the value of the test variable at the test depth
    int i = 0;
    double testVal = NQueryConstants.MISSINGVALUE;
    double lpres = 0.0;
    while (i < nBottles) {
      int bh = i;
      lpres = pres[bh];
      double tval = testparamvals[bh];
      if (i > 0 && lpres > testDepth && !NQueryFormulas.isMissing(tval)) {
        // found a bottle below test depth
        int pbh = i - 1;
        double pPres = pres[pbh];
        double del1 = Math.abs(lpres - testDepth);
        double del2 = Math.abs(pPres - testDepth);
        if (del1 <= del2) {
          testVal = tval;
        }
        else if (!NQueryFormulas.isMissing(testparamvals[pbh])) {
          testVal = testparamvals[pbh];
          i--;
        }
        break;
      }
      else if (lpres == testDepth && !NQueryFormulas.isMissing(tval)) {
        testVal = testparamvals[bh];
        break;
      }
      i++;
    }

    if (NQueryFormulas.isMissing(testVal)) {
      return NQueryConstants.MISSINGVALUE;
    }

    double diff = 0;
    while (diff < delta && i < nBottles) {
      int bh = i;
      if (!NQueryFormulas.isMissing(testparamvals[bh])) {
        diff = Math.abs(testVal - testparamvals[bh]);
      }
      i++;
    }

    if (diff >= delta && i < nBottles) {
      int bh = i - 1;
      if (!NQueryFormulas.isMissing(pres[bh])) {
        return pres[bh];
      }
    }
    return NQueryConstants.MISSINGVALUE;
  }

  public static double mixedLayerSurface(double[] pres, double[] testparamvals, double startDepth, double maxDepth,
                                         double delta) {
    int nBottles = pres.length;
    int i = 0;

    // find the first good bottle at the start depth
    while (i < nBottles) {
      int bh = i;
      double tval = testparamvals[bh];
      boolean tbad = NQueryFormulas.isMissing(tval);
      double pres1 = pres[bh];
      boolean p1bad = NQueryFormulas.isMissing(pres1);

      if (tbad || p1bad) {
        i++;
      }
      else if (!p1bad) {
        if (pres1 < startDepth) {
          i++;
        }
        else {
          break;
        }
      }
      else {
        break;
      }
    }

    //define a surface salinity as the mean of the top five salinities
    double valSum = 0.0;
    double presSum = 0.0;
    double surfMean = 0.0;
    int count = 0;
    while (i < nBottles) {
      int bh = i;
      double lpres = pres[bh];
      double val = testparamvals[bh];
      if (lpres <= maxDepth && !NQueryFormulas.isMissing(val)) {
        valSum += val;
        presSum += lpres;
        count++;
      }
      else {
        break;
      }
      i++;
    }
    if (count > 0) {
      surfMean = valSum / count;
    }
    else {
      return NQueryConstants.MISSINGVALUE;
    }

    double diff = 0;
    while (diff < delta && i < nBottles) {
      int bh = i;
      if (!NQueryFormulas.isMissing(testparamvals[bh])) {
        diff = Math.abs(surfMean - testparamvals[bh]);
      }
      i++;
    }

    if (diff >= delta && i < nBottles) {
      int bh = i - 1;
      if (!NQueryFormulas.isMissing(pres[bh])) {
        return pres[bh];
      }
    }
    return NQueryConstants.MISSINGVALUE;
  }

  public static double mixedLayerSlope(double[] pres, double[] testparamvals, double testDepth, double delta) {
    int nBottles = pres.length;
    int i = 0;

    // position to start depth
    double lpres = 0.0;
    while (i < nBottles) {
      int bh = i;
      lpres = pres[bh];
      if (i > 0 && lpres > testDepth) {
        // found a bottle below test depth
        int pbh = i - 1;
        double pPres = pres[pbh];
        double del1 = Math.abs(lpres - testDepth);
        double del2 = Math.abs(pPres - testDepth);
        if (del1 <= del2) {
          break;
        }
        else {
          i--;
          break;
        }
      }
      else if (lpres == testDepth) {
        break;
      }
      i++;
    }
    ;

    // find the first good bottle at or after start depth
    while (i < nBottles) {
      int bh = i;
      double tval = testparamvals[bh];
      boolean tbad = NQueryFormulas.isMissing(tval);
      double pres1 = pres[bh];
      boolean p1bad = NQueryFormulas.isMissing(pres1);

      if (tbad || p1bad) {
        i++;
      }
      else {
        break;
      }
    }

    if (i < (nBottles - 1)) {
      int oldbh = i;
      int bh = ++i;

      double diff = 0;
      while ( /*bh != null && */diff < delta && i < nBottles) {
        if (!NQueryFormulas.isMissing(testparamvals[bh]) && !NQueryFormulas.isMissing(testparamvals[oldbh])) {
          double tm1 = testparamvals[oldbh];
          double t = testparamvals[bh];
          diff = Math.abs(tm1 - t);
        }
        oldbh = bh;
        bh = ++i;
      }

      if (diff >= delta && i < nBottles) {
        int prevbh = i - 1;
        if (!NQueryFormulas.isMissing(testparamvals[prevbh])) {
          return pres[prevbh];
        }
      }
    }
    return NQueryConstants.MISSINGVALUE;
  }

  public static double interpolateStation(NQInterpolationSpecification iSpec, double[] pres, double[] intArray,
                                          double[] wrtArray, double lat, double bottom) {
    int nBottles = pres.length;

    if (iSpec.isAtSurface()) {
      // extrapolate to the surface observation
      // test for depth limit
      if (iSpec.getDepthLimit() >= 0) {
        // there is a depth limit--find the two top observations above the depth limit
        if (pres[0] > iSpec.getDepthLimit()) {
          // already failed--first bottle is below depth limit
          return NQueryConstants.MISSINGVALUE;
        }
        // look for second obs above the depth limit
        int bot1 = -99;
        boolean found = false;
        for (int i = 1; i < nBottles; i++) {
          bot1 = i;
          double lpres = pres[bot1];
          if (lpres <= iSpec.getDepthLimit()) {
            found = true;
            break;
          }
        }

        if (found) {
          // found a bottle in the depth range--extrapolate to surface obs
          if (!NQueryFormulas.isMissing(pres[0]) && !NQueryFormulas.isMissing(pres[bot1]) &&
              !NQueryFormulas.isMissing(intArray[0]) && !NQueryFormulas.isMissing(intArray[bot1])) {
            double denom = pres[bot1] - pres[0];
            double num = intArray[bot1] - intArray[0];
            double slope = num / denom;
            double yintercept = intArray[bot1] - (slope * pres[bot1]);
            double valAtSurf = (slope * 0.0) + yintercept;
            return valAtSurf;
          }
          else {
            return NQueryConstants.MISSINGVALUE;
          }
        }
        else {
          return NQueryConstants.MISSINGVALUE;
        }
      }
      else {
        // no depth limit just use the two top observations
        if (nBottles > 2) {
          int bot = 0;
          int bot2 = 1;

          if (!NQueryFormulas.isMissing(pres[0]) && !NQueryFormulas.isMissing(pres[1]) &&
              !NQueryFormulas.isMissing(intArray[0]) && !NQueryFormulas.isMissing(intArray[1])) {
            // compute the gradient in pressure
            double denom = pres[0] - pres[1];
            double num = intArray[0] - intArray[1];
            double slope = num / denom;
            double yintercept = intArray[0] - (slope * pres[0]);
            double valAtSurf = (slope * 0.0) + yintercept;
            return valAtSurf;
          }
          else {
            return NQueryConstants.MISSINGVALUE;
          }
        }
        else {
          // only one observation--just return it as surface obs
          return intArray[0];
        }
      }
    }
    else if (iSpec.isAtBottom()) {
      // interpolate to the bottom observation
      if (bottom != NQueryConstants.MISSINGVALUE) {
        // look for the two observations above bottom and extrapolate to bottom
        int bot1 = nBottles - 1;
        int bot2 = nBottles - 2;

        if (!NQueryFormulas.isMissing(pres[bot1]) && !NQueryFormulas.isMissing(pres[bot2]) &&
            !NQueryFormulas.isMissing(intArray[bot1]) && !NQueryFormulas.isMissing(intArray[bot2])) {
          // compute the gradient in pressure
          double denom = pres[bot1] - pres[bot2];
          double num = intArray[bot1] - intArray[bot2];
          double slope = num / denom;
          double yintercept = intArray[bot2] - (slope * pres[bot2]);
          double valAtSurf = (slope * bottom) + yintercept;
          return valAtSurf;
        }
        else {
          return NQueryConstants.MISSINGVALUE;
        }
      }
      else
      // bottom is missing
      if (iSpec.isUseDeepest()) {
        return intArray[nBottles - 1];
      }
      else {
        return NQueryConstants.MISSINGVALUE;
      }
    }
    else {
      // interpolate to an arbitrary level
      double testVal = iSpec.getAtVal();

      if (iSpec.getSearchMethod() == NQueryConstants.SEARCH_TOP_DOWN) {
        // top down search
        for (int i = 0; i < nBottles - 1; i++) {
          int bot = i;
          int botp1 = i + 1;

          double iVal = intArray[bot];
          double iValp1 = intArray[botp1];
          double wrtVal = wrtArray[bot];
          double wrtValp1 = wrtArray[botp1];
          if (testVal >= wrtVal && testVal <= wrtValp1) {
            // found bounding observations--interpolate
            if (!NQueryFormulas.isMissing(iVal) && !NQueryFormulas.isMissing(iValp1) &&
                !NQueryFormulas.isMissing(wrtVal) && !NQueryFormulas.isMissing(wrtValp1)) {
              // compute the gradient in pressure
              double denom = wrtVal - wrtValp1;
              double num = iVal - iValp1;
              double slope = num / denom;
              double yintercept = iVal - (slope * wrtVal);
              double valAtSurf = (slope * testVal) + yintercept;
              return valAtSurf;
            }
            else {
              return NQueryConstants.MISSINGVALUE;
            }
          }
        }
      }
      else {
        // bottom up search
        if (nBottles < 3) {
          return NQueryConstants.MISSINGVALUE;
        }
        for (int i = nBottles - 1; i > 0; i--) {
          int bot = i;
          int botm1 = i - 1;

          double iVal = intArray[bot];
          double iValm1 = intArray[botm1];
          double wrtVal = wrtArray[bot];
          double wrtValm1 = wrtArray[botm1];
          if (testVal >= wrtValm1 && testVal <= wrtVal) {
            // found bounding observations--interpolate
            if (!NQueryFormulas.isMissing(iVal) && !NQueryFormulas.isMissing(iValm1) &&
                !NQueryFormulas.isMissing(wrtVal) && !NQueryFormulas.isMissing(wrtValm1)) {
              // compute the gradient in pressure
              double denom = wrtValm1 - wrtVal;
              double num = iValm1 - iVal;
              double slope = num / denom;
              double yintercept = iVal - (slope * wrtVal);
              double valAtSurf = (slope * testVal) + yintercept;
              return valAtSurf;
            }
            else {
              return NQueryConstants.MISSINGVALUE;
            }
          }
        }
      }
    }
    return NQueryConstants.MISSINGVALUE;
  }

  public static double integrateStation(NQIntegrationSpecification iSpec, double[] pres, double[] intArray,
                                        double[] wrtArray, double lat) {
    double sumParam; // sum of trapezoidal panels of delta(travel time)
    double avg; // average integrand between bottles (i) & (i+1).
    double paramVal1, paramVal2; // param values at bottles (i) & (i+1).
    double z1, z2; // depth at bottles (i) & (i+1).
    double deltaZ; // depth difference between bottles (i) & (i+1).
    int nBottles; // number of bottles at this station.
    boolean p1bad, p2bad; // logical flags signaling missing values.
    double pres1, pres2; // pressure at bottles (i) & (i+1).

    nBottles = pres.length;
    if (nBottles <= 0) {
      return NQueryConstants.MISSINGVALUE;
    }
    // find these values
    double presAtMin = NQueryConstants.MISSINGVALUE; // pressure of the min value of the wrt variable
    double presAtMax = NQueryConstants.MISSINGVALUE; // pressure of the min value of the wrt variable
    double intValAtMin = NQueryConstants.MISSINGVALUE; // value of the integrand at the interpolated min pressure
    double intValAtMax = NQueryConstants.MISSINGVALUE; // value of the integrand at the interpolated max pressure
    int intIndexAtMin = 0;
    int intIndexAtMax = nBottles;
    int num = 0;
    double[] integrandArray = null;
    double[] presArray = null;
    //sh.setStnValHitShallowest(sech.getNumStnVars()-1, false);
    //sh.setStnValHitDeepest(sech.getNumStnVars()-1, false);
    boolean foundMin = false;
    boolean foundMax = false;

    // test for shallow outcrop first
    if (wrtArray[0] > iSpec.getMinIntVal() && iSpec.isUseShallowest()) {
      presAtMin = pres[0];
      intValAtMin = intArray[0];
      intIndexAtMin = 0;
      //sh.setStnValHitShallowest(sech.getNumStnVars()-1, true);
      foundMin = true;
    }
    else if (wrtArray[0] == iSpec.getMinIntVal()) {
      presAtMin = pres[0];
      intValAtMin = intArray[0];
      intIndexAtMin = 0;
      foundMin = true;
    }
    else if (wrtArray[0] > iSpec.getMinIntVal() && !iSpec.isUseShallowest()) {
      return NQueryConstants.MISSINGVALUE;
    }

    // test for deep outcrop first
    int bh = nBottles - 1;
    if (wrtArray[bh] < iSpec.getMaxIntVal() && iSpec.isUseDeepest()) {
      presAtMax = pres[bh];
      intValAtMax = intArray[bh];
      intIndexAtMax = nBottles - 1;
      //sh.setStnValHitDeepest(sech.getNumStnVars()-1, true);
      foundMax = true;
    }
    else if (wrtArray[bh] == iSpec.getMaxIntVal()) {
      presAtMax = pres[bh];
      intValAtMax = intArray[bh];
      intIndexAtMax = nBottles - 1;
      foundMax = true;
    }
    else if (wrtArray[bh] < iSpec.getMaxIntVal() && !iSpec.isUseDeepest()) {
      return NQueryConstants.MISSINGVALUE;
    }

    if ((!foundMin || !foundMax) && iSpec.getSearchMethod() == NQueryConstants.SEARCH_TOP_DOWN) {
      // top down search
      bh = 0;
      double oldPresVal = pres[bh];
      double oldWRTVal = wrtArray[bh];
      double oldIntVal = intArray[bh];

      for (int i = 1; i < nBottles; i++) {
        bh = i;
        double wrtVal = wrtArray[bh];
        double presVal = pres[bh];
        double intVal = intArray[bh];

        if (!foundMin && wrtVal == iSpec.getMinIntVal()) {
          intIndexAtMin = i;
          intValAtMin = intVal;
          presAtMin = presVal;
          foundMin = true;
        }
        else if (!foundMin && wrtVal > iSpec.getMinIntVal()) {
          //bottle brackets the wrt range
          intIndexAtMin = i - 1;

          if (NQueryFormulas.isMissing(intVal) || NQueryFormulas.isMissing(oldIntVal)) {
            return NQueryConstants.MISSINGVALUE;
          }

          if (iSpec.isInterpolateMissing()) {
            // interpolate the integrand to this
            double denom = wrtVal - oldWRTVal;
            //double num1 = Math.abs(iSpec.getMinIntVal() - wrtVal);
            double num2 = iSpec.getMinIntVal() - oldWRTVal;
            double frac = num2 / denom;

            // Triangle inequality.
            //if (num1 > denom && num1 > num2) frac = 1.0;
            //if (num2 > denom && num2 > num1) frac = 0.0;
            intValAtMin = oldIntVal + (frac * (intVal - oldIntVal));
            presAtMin = oldPresVal + (frac * (presVal - oldPresVal));
          }
          else {
            intValAtMin = intVal;
            presAtMin = presVal;
          }
          foundMin = true;
        }
        else if (!foundMax && wrtVal == iSpec.getMaxIntVal()) {
          intIndexAtMax = i;
          intValAtMax = intVal;
          presAtMax = presVal;
          foundMax = true;
        }
        else if (!foundMax && wrtVal > iSpec.getMaxIntVal()) {
          //bottle brackets the wrt range
          //if (foundMin && intIndexAtMin == 0)
          intIndexAtMax = i;
          //else
          //	intIndexAtMax = i - 1;

          if (NQueryFormulas.isMissing(intVal) || NQueryFormulas.isMissing(oldIntVal)) {
            return NQueryConstants.MISSINGVALUE;
          }

          if (iSpec.isInterpolateMissing()) {
            // interpolate the integrand to this
            double denom = wrtVal - oldWRTVal;
            //double num1 = Math.abs(iSpec.getMinIntVal() - wrtVal);
            double num2 = iSpec.getMaxIntVal() - oldWRTVal;
            double frac = num2 / denom;

            // Triangle inequality.
            //if (num1 > denom && num1 > num2) frac = 1.0;
            //if (num2 > denom && num2 > num1) frac = 0.0;
            intValAtMax = oldIntVal + (frac * (intVal - oldIntVal));
            presAtMax = oldPresVal + (frac * (presVal - oldPresVal));
          }
          else {
            intValAtMax = intVal;
            presAtMax = presVal;
          }
          foundMax = true;
        }

        if (foundMin && foundMax) {
          break;
        }
        oldWRTVal = wrtVal;
        oldPresVal = presVal;
        oldIntVal = intVal;
      }

      if (presAtMin == NQueryConstants.MISSINGVALUE || presAtMax == NQueryConstants.MISSINGVALUE ||
          intValAtMin == NQueryConstants.MISSINGVALUE || intValAtMax == NQueryConstants.MISSINGVALUE) {
        return NQueryConstants.MISSINGVALUE;
      }
    }
    else if ((!foundMin || !foundMax) && iSpec.getSearchMethod() == NQueryConstants.SEARCH_BOTTOM_UP) {
      // bottom up search
      bh = nBottles - 1;
      double oldPresVal = pres[bh];
      double oldWRTVal = wrtArray[bh];
      double oldIntVal = intArray[bh];

      for (int i = nBottles - 1; i >= 0; i--) {
        bh = i;
        double wrtVal = wrtArray[bh];
        double presVal = pres[bh];
        double intVal = intArray[bh];

        if (!foundMin && wrtVal == iSpec.getMinIntVal()) {
          intIndexAtMin = i;
          intValAtMin = intVal;
          presAtMin = presVal - oldPresVal;
          foundMin = true;
        }
        else if (!foundMin && wrtVal < iSpec.getMinIntVal()) {
          //bottle brackets the wrt range
          intIndexAtMin = i;

          if (intVal == NQueryConstants.MISSINGVALUE || oldIntVal == NQueryConstants.MISSINGVALUE) {
            // interpolate a new value
            return NQueryConstants.MISSINGVALUE;
          }

          if (iSpec.isInterpolateMissing()) {
            // interpolate the integrand to this
            double denom = wrtVal - oldWRTVal;
            //double num1 = Math.abs(iSpec.getMinIntVal() - wrtVal);
            double num2 = iSpec.getMinIntVal() - oldWRTVal;
            double frac = num2 / denom;

            // Triangle inequality.
            //if (num1 > denom && num1 > num2) frac = 1.0;
            //if (num2 > denom && num2 > num1) frac = 0.0;
            intValAtMin = oldIntVal + (frac * (intVal - oldIntVal));
            presAtMin = oldPresVal + (frac * (presVal - oldPresVal));
          }
          else {
            intValAtMin = intVal;
            presAtMin = presVal;
          }
          foundMin = true;
        }
        else if (!foundMax && wrtVal == iSpec.getMaxIntVal()) {
          //bottle brackets the wrt range
          intIndexAtMax = i;
          intValAtMax = intVal;
          presAtMax = presVal;
          foundMax = true;
        }
        else if (!foundMax && wrtVal < iSpec.getMaxIntVal()) {
          //bottle brackets the wrt range
          intIndexAtMax = i;
          if (i == 0) {
            intIndexAtMax++;
          }

          if (intVal == NQueryConstants.MISSINGVALUE || oldIntVal == NQueryConstants.MISSINGVALUE) {
            return NQueryConstants.MISSINGVALUE;
          }

          if (iSpec.isInterpolateMissing()) {
            // interpolate the integrand to this
            double denom = wrtVal - oldWRTVal;
            //double num1 = Math.abs(iSpec.getMinIntVal() - wrtVal);
            double num2 = iSpec.getMaxIntVal() - oldWRTVal;
            double frac = num2 / denom;

            // Triangle inequality.
            //if (num1 > denom && num1 > num2) frac = 1.0;
            //if (num2 > denom && num2 > num1) frac = 0.0;
            intValAtMax = oldIntVal + (frac * (intVal - oldIntVal));
            presAtMax = oldPresVal + (frac * (presVal - oldPresVal));
          }
          else {
            intValAtMax = intVal;
            presAtMax = presVal;
          }
          foundMax = true;
        }

        if (foundMin && foundMax) {
          break;
        }
        oldWRTVal = wrtVal;
        oldPresVal = presVal;
        oldIntVal = intVal;
      }

      if (presAtMin == NQueryConstants.MISSINGVALUE || presAtMax == NQueryConstants.MISSINGVALUE ||
          intValAtMin == NQueryConstants.MISSINGVALUE || intValAtMax == NQueryConstants.MISSINGVALUE) {
        return NQueryConstants.MISSINGVALUE;
      }

    }
    else {
      // mixed search
      // top down search for min
      double oldPresVal = pres[0];
      double oldWRTVal = wrtArray[0];
      double oldIntVal = intArray[0];

      if (!foundMin) {
        for (int i = 1; i < nBottles; i++) {
          double wrtVal = wrtArray[i];
          double presVal = pres[i];
          double intVal = intArray[i];

          if (wrtVal == iSpec.getMinIntVal()) {
            intIndexAtMin = i;
            intValAtMin = intVal;
            presAtMin = presVal;
            foundMin = true;
          }
          else if (wrtVal > iSpec.getMinIntVal()) {
            //bottle brackets the wrt range
            intIndexAtMin = i - 1;

            if (intVal == NQueryConstants.MISSINGVALUE || oldIntVal == NQueryConstants.MISSINGVALUE) {
              return NQueryConstants.MISSINGVALUE;
            }

            if (iSpec.isInterpolateMissing()) {
              // interpolate the integrand to this
              double denom = wrtVal - oldWRTVal;
              //double num1 = Math.abs(iSpec.getMinIntVal() - wrtVal);
              double num2 = iSpec.getMinIntVal() - oldWRTVal;
              double frac = num2 / denom;

              // Triangle inequality.
              //if (num1 > denom && num1 > num2) frac = 1.0;
              //if (num2 > denom && num2 > num1) frac = 0.0;
              intValAtMin = oldIntVal + (frac * (intVal - oldIntVal));
              presAtMin = oldPresVal + (frac * (presVal - oldPresVal));
            }
            else {
              intValAtMin = intVal;
              presAtMin = presVal;
            }
            foundMin = true;
            break;
          }
          oldWRTVal = wrtVal;
          oldPresVal = presVal;
          oldIntVal = intVal;
        }
      }

      // bottom up search for max
      double oldPresVal2 = pres[nBottles - 1];
      double oldWRTVal2 = wrtArray[nBottles - 1];
      double oldIntVal2 = intArray[nBottles - 1];

      if (!foundMax) {
        for (int i = nBottles - 1; i >= 0; i--) {
          double wrtVal = wrtArray[i];
          double presVal = pres[i];
          double intVal = intArray[i];

          if (wrtVal == iSpec.getMaxIntVal()) {
            //bottle brackets the wrt range
            intIndexAtMax = i;
            intValAtMax = intVal;
            presAtMax = presVal;
            foundMax = true;
          }
          else if (wrtVal < iSpec.getMaxIntVal()) {
            //bottle brackets the wrt range
            intIndexAtMax = i;
            if (i == 0) {
              intIndexAtMax++;
            }

            // interpolate the integrand to this
            if (intVal == NQueryConstants.MISSINGVALUE || oldIntVal2 == NQueryConstants.MISSINGVALUE) {
              // interpolate a new value
              return NQueryConstants.MISSINGVALUE;
            }

            if (iSpec.isInterpolateMissing()) {
              // interpolate the integrand to this
              double denom = wrtVal - oldWRTVal2;
              //double num1 = Math.abs(iSpec.getMinIntVal() - wrtVal);
              double num2 = iSpec.getMaxIntVal() - oldWRTVal2;
              double frac = num2 / denom;

              // Triangle inequality.
              //if (num1 > denom && num1 > num2) frac = 1.0;
              //if (num2 > denom && num2 > num1) frac = 0.0;
              intValAtMax = oldIntVal2 - (frac * (intVal - oldIntVal2));
              presAtMax = oldPresVal2 - (frac * (presVal - oldPresVal2));
            }
            else {
              intValAtMax = intVal;
              presAtMax = presVal;
            }
            foundMax = true;
          }

          if (foundMin && foundMax) {
            break;
          }
          oldWRTVal2 = wrtVal;
          oldPresVal2 = presVal;
          oldIntVal2 = intVal;
        }
      }
    }

    // now build the integration array
    num = intIndexAtMax - intIndexAtMin + 1;
    integrandArray = new double[num];
    presArray = new double[num];
    integrandArray[0] = intValAtMin;
    integrandArray[num - 1] = intValAtMax;
    presArray[0] = presAtMin;
    presArray[num - 1] = presAtMax;
    int cnt = 1;
    for (int i = intIndexAtMin + 1; i < intIndexAtMax; i++) {
      if (intArray[i] == NQueryConstants.MISSINGVALUE) {
        if (iSpec.isInterpolateMissing()) {
          double midPresVal = pres[i];
          double topIntVal = NQueryConstants.MISSINGVALUE;
          double bottomIntVal = NQueryConstants.MISSINGVALUE;
          double topPresVal = NQueryConstants.MISSINGVALUE;
          double bottomPresVal = NQueryConstants.MISSINGVALUE;
          boolean foundUpper = false;
          boolean foundLower = false;

          // look above first
          int icnt = i - 1;
          if (icnt >= 0) {
            while (icnt >= 0) {
              if (intArray[icnt] != NQueryConstants.MISSINGVALUE) {
                topIntVal = intArray[icnt];
                topPresVal = pres[icnt];
                foundUpper = true;
                break;
              }
              icnt--;
            }
          }
          else {
            // can't find a bottle above so abort the interpolation
            return NQueryConstants.MISSINGVALUE;
          }

          // look below
          icnt = i + 1;
          if (icnt < nBottles - 1) {
            while (icnt < nBottles - 1) {
              if (intArray[icnt] != NQueryConstants.MISSINGVALUE) {
                bottomIntVal = intArray[icnt];
                bottomPresVal = pres[icnt];
                foundLower = true;
                break;
              }
              icnt++;
            }
          }

          if (foundUpper && foundLower) {
            // interpolate
            double denom = Math.abs(topPresVal - bottomPresVal);
            //double num1 = Math.abs(midPresVal - topPresVal);
            double num2 = Math.abs(midPresVal - bottomPresVal);
            double frac = num2 / denom;

            // Triangle inequality.
            //if (num1 > denom && num1 > num2) frac = 1.0;
            //if (num2 > denom && num2 > num1) frac = 0.0;

            // interpolate the integrand to this
            integrandArray[cnt] = topIntVal + (frac * (bottomIntVal - topIntVal));
          }
          else {
            // can't find a bottle below so abort the interpolation
            return NQueryConstants.MISSINGVALUE;
          }
        }
        else {
          // abort the integration
          return NQueryConstants.MISSINGVALUE;
        }
      }
      else {
        integrandArray[cnt] = intArray[i];
      }
      presArray[cnt++] = pres[i];
    }

    // integrate
    sumParam = 0;
    double sumDepth = 0;
    for (int i = 1; i < num; i++) {
      // Test to see if data are valid, rescaling valid data only
      pres1 = presArray[i - 1];
      pres2 = presArray[i];
      p1bad = NQueryFormulas.isMissing(pres1);
      p2bad = NQueryFormulas.isMissing(pres2);

      if (p1bad || p2bad) {
        // Routine quits when it encounters first bad value
        break;
      }

      z1 = presToZ(pres1, lat);
      z2 = presToZ(pres2, lat);
      paramVal1 = integrandArray[i - 1];
      paramVal2 = integrandArray[i];
      deltaZ = z2 - z1;
      avg = .5 * (paramVal1 + paramVal2); // Evaluate the integrand.
      sumParam += deltaZ * avg;
      sumDepth += deltaZ;
    } // End integration down the list of bottles.

    if (iSpec.isComputeMean()) {
      double zRange = presToZ(presArray[num - 1], lat) - presToZ(presArray[0], lat);
      return sumParam / zRange;
    }
    else {
      return sumParam;
    }
  }

  public static void convertDepthAxis(String inUnits, double[] zVals) {
    if (inUnits.equalsIgnoreCase("meters") || inUnits.equalsIgnoreCase("m")) {
      for (int i = 0; i < zVals.length; i++) {
        if (zVals[i] != NQueryConstants.MISSINGVALUE) {
          zVals[i] = zToPres(zVals[i]);
        }
      }
    }
    else if (inUnits.equalsIgnoreCase("cm") || inUnits.equalsIgnoreCase("centimeters")) {
      for (int i = 0; i < zVals.length; i++) {
        if (zVals[i] != NQueryConstants.MISSINGVALUE) {
          zVals[i] = zToPres(100.0 * zVals[i]);
        }
      }
    }
    else if (inUnits.equalsIgnoreCase("ft") || inUnits.equalsIgnoreCase("feet")) {
      for (int i = 0; i < zVals.length; i++) {
        if (zVals[i] != NQueryConstants.MISSINGVALUE) {
          zVals[i] = zToPres(0.30769 * zVals[i]);
        }
      }
    }
    else if (inUnits.equalsIgnoreCase("in") || inUnits.equalsIgnoreCase("inches")) {
      for (int i = 0; i < zVals.length; i++) {
        if (zVals[i] != NQueryConstants.MISSINGVALUE) {
          zVals[i] = zToPres(0.02564 * zVals[i]);
        }
      }
    }
  }

  public static void locateOnParentFrame(Container container, Component compToCenter) {
    locateOnParentFrame(container, compToCenter, 0, 0);
  }

  public static void locateOnParentFrame(Container container, Component compToCenter, int xOffset, int yOffset) {
    // Center over parent
    Rectangle bounds = null;
    Point location = container.getLocation();
    Dimension cd = container.getSize();
    bounds = new Rectangle(location.x, location.y, cd.width, cd.height);
    Rectangle abounds = compToCenter.getBounds();
    int x = bounds.x + (bounds.width - abounds.width) / 2;
    int y = bounds.y + (bounds.height - abounds.height) / 2;

    // Clip to screen. If the dialog extends off the screen,
    // center of screen rather than parent.
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    if (sd.width > 0 && sd.height > 0) {
      Rectangle scr = new Rectangle(0, 0, sd.width - abounds.width, sd.height - abounds.height);

      // Oops. Some portion of the dialog is off the screen. Screw the parent, lets
      // center on the screen.
      if (!scr.contains(new Point(x, y))) {
        x = bounds.x + (sd.width - abounds.width) / 2;
        y = bounds.y + (sd.height - abounds.height) / 2;
      }
    }
    compToCenter.setLocation(x + xOffset, y + yOffset);
  }

  public static void centerFrameOnScreen(Component compToCenter, boolean show) {
    // Center on screen
    Rectangle dBounds = compToCenter.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    compToCenter.setLocation(x, y);
    if (show) {
      compToCenter.show();
    }
  }

  public static String[] parseCSVString(String csvString) {
    int numItems = getNumberOfItems(csvString, ',');
    String[] results = new String[numItems];

    for (int i = 0; i < numItems; i++) {
      results[i] = getItem(csvString, ',', i + 1);
    }
    return results;
  }

  public static int getNumberOfItems(String inString, char delim) {
    int i = 0;
    int delimPos = 0;
    int startPos = 0;
    int lastPos = 0;
    while (true) {
      delimPos = inString.indexOf(delim, startPos);

      if (delimPos == -1) {
        return i + 1;
      }

      lastPos = startPos;
      startPos = delimPos + 1;
      i++;
    }
  }

  public static String getItem(String inString, char delim, int item) {
    int startPos = 0;
    int delimPos = 0;
    int lastPos = 0;
    for (int i = 0; i < item; i++) {
      delimPos = inString.indexOf(delim, startPos);

      if (delimPos == -1) {
        // hit end of string
        lastPos = startPos;
        delimPos = inString.length();
        break;
      }

      lastPos = startPos;
      startPos = delimPos + 1;
    }

    String outStr = inString.substring(lastPos, delimPos);
    return outStr;
  }

  public static String returnMiddleTruncatedString(String inStr, int maxLength) {
    if (maxLength < 0) {
      return inStr;
    }
    int numToKeep = (maxLength - 2) / 2;
    int len = inStr.length();
    if (len < maxLength) {
      return inStr;
    }
    return new String(inStr.substring(0, numToKeep - 1) + ".." + inStr.substring(len - numToKeep - 1, len));
  }

  /*public static String paramNameToJOAName(String inParam) {
   // always translate pressure
   if (inParam.equalsIgnoreCase("CTDPRS") || inParam.startsWith("PRES")) {
    return new String("PRES");
   }

   if (!NQueryConstants.DEFAULT_TRANSLATE_PARAM_NAMES)
    return inParam;

   if (inParam.equalsIgnoreCase("BTLNBR")) {
    return new String("BTLN");
   }
   else if (inParam.startsWith("CTDT") || inParam.startsWith("TEM") ||
    inParam.equalsIgnoreCase("T") || inParam.equalsIgnoreCase("TE")) {
    return new String("TEMP");
   }
   else if (inParam.equalsIgnoreCase("SAMPNO")) {
    return new String("SAMP");
   }
   else if (inParam.equalsIgnoreCase("CTDSAL") || inParam.equalsIgnoreCase("PSAL")) {
    return new String("CTDS");
   }
   else if (inParam.equalsIgnoreCase("CTDOXY") || inParam.equalsIgnoreCase("DOXY")) {
    return new String("CTDO");
   }
   else if (inParam.equalsIgnoreCase("THETA")) {
    return new String("WTHT");
   }
   else if (inParam.equalsIgnoreCase("Bedfort")) {
    return new String("BEDFORT");
   }
   else if (inParam.startsWith("SPPT") || inParam.startsWith("BSAL") || inParam.startsWith("SAL") || inParam.equalsIgnoreCase("S") ||
    inParam.equalsIgnoreCase("SA")|| inParam.equalsIgnoreCase("S0")) {
    return new String("SALT");
   }
   else if (inParam.equalsIgnoreCase("BO") || inParam.equalsIgnoreCase("OXYGEN") || inParam.equalsIgnoreCase("OXY") ||
    inParam.startsWith("O2") || inParam.equalsIgnoreCase("O") || inParam.startsWith("BOTTLE_OXYGEN")) {
    return new String("O2");
   }
   else if (inParam.equalsIgnoreCase("SILICATE") || inParam.equalsIgnoreCase("SILICAT") || inParam.equalsIgnoreCase("SILI") || inParam.startsWith("SIL") || inParam.equalsIgnoreCase("SI") ||
    inParam.equalsIgnoreCase("SIO3") || inParam.equalsIgnoreCase("SIO4") || inParam.equalsIgnoreCase("SLCA")) {
    return new String("SIO3");
   }
   else if (inParam.equalsIgnoreCase("NO2+NO3") || inParam.equalsIgnoreCase("NO2_NO3") || inParam.equalsIgnoreCase("nitrate_plus_nitrite")) {
    return new String("NO2+NO3");
   }
   else if (inParam.equalsIgnoreCase("NITRATE") || inParam.equalsIgnoreCase("NITRAT") || inParam.equalsIgnoreCase("NTRA") ||
    inParam.startsWith("NO3")) {
    return new String("NO3");
   }
   else if (inParam.equalsIgnoreCase("NITRITE") || inParam.equalsIgnoreCase("NITRIT") || inParam.equalsIgnoreCase("NTRI") ||
    inParam.startsWith("NO2")) {
    return new String("NO2");
   }
   else if (inParam.equalsIgnoreCase("PHOSPHATE") || inParam.equalsIgnoreCase("PHOS") || inParam.equalsIgnoreCase("PHSPHT") || inParam.startsWith("PO4")) {
    return new String("PO4 ");
   }
   else if (inParam.equalsIgnoreCase("CFC-11") || inParam.equalsIgnoreCase("Freon_11")) {
    return new String("F11");
   }
   else if (inParam.equalsIgnoreCase("CFC-12") || inParam.equalsIgnoreCase("Freon_12")) {
    return new String("F12");
   }
   else if (inParam.equalsIgnoreCase("CFC-113") || inParam.equalsIgnoreCase("Freon_113")) {
    return new String("F113");
   }
   else if (inParam.equalsIgnoreCase("CFC113")) {
    return new String("F113");
   }
   else if (inParam.equalsIgnoreCase("CF113ER") || inParam.equalsIgnoreCase("freon_113_error") || inParam.equalsIgnoreCase("C113ER")) {
    return new String("F113ER");
   }
   else if (inParam.equalsIgnoreCase("CF11ER") || inParam.equalsIgnoreCase("freon_11_error")) {
    return new String("F11ER");
   }
   else if (inParam.equalsIgnoreCase("CF12ER") || inParam.equalsIgnoreCase("freon_12_error")) {
    return new String("F12ER");
   }
   else if (inParam.equalsIgnoreCase("TRITIUM") || inParam.equalsIgnoreCase("TRITUM")) {
    return new String("TRIT");
   }
   else if (inParam.equalsIgnoreCase("TRITER") || inParam.equalsIgnoreCase("TRITIUM_ERROR")) {
    return new String("TRER");
   }
   else if (inParam.equalsIgnoreCase("HELIUM")) {
    return new String("HELI");
   }
   else if (inParam.equalsIgnoreCase("HELIER") || inParam.equalsIgnoreCase("HELIUM_ERROR")) {
    return new String("HEER");
   }
   else if (inParam.equalsIgnoreCase("DELHE") || inParam.equalsIgnoreCase("DELTA_HELIUM")) {
    return new String("DELH");
   }
   else if (inParam.equalsIgnoreCase("DELHE3") || inParam.equalsIgnoreCase("DELTA_HELIUM_3")) {
    return new String("DELH3");
   }
   else if (inParam.equalsIgnoreCase("DELHER") || inParam.equalsIgnoreCase("DELTA_HELIUM_ERROR")) {
    return new String("DH3E");
   }
   else if (inParam.equalsIgnoreCase("DELC14") || inParam.equalsIgnoreCase("carbon_14")) {
    return new String("C14 ");
   }
   else if (inParam.equalsIgnoreCase("FCO2") || inParam.equalsIgnoreCase("FCO2TMP") || inParam.equalsIgnoreCase("fugacity_co2_temperature")) {
    return new String("FCO2TMP");
   }
   else if (inParam.equalsIgnoreCase("CCL4") || inParam.equalsIgnoreCase("CARBON_TETRACHLORIDE")) {
    return new String("CCL4");
   }
   else if (inParam.equalsIgnoreCase("CCL4ER") || inParam.equalsIgnoreCase("CARBON_TETRACHLORIDE_ERROR")) {
    return new String("CCL4ER");
   }
   else if (inParam.equalsIgnoreCase("C14ERR")) {
    return new String("C14E");
   }
   else if (inParam.equalsIgnoreCase("TCARBN") || inParam.equalsIgnoreCase("total_carbon") || inParam.equalsIgnoreCase("total_co2")) {
    return new String("TCO2");
   }
   else if (inParam.equalsIgnoreCase("TCO2TMP") || inParam.equalsIgnoreCase("total_co2_temperature")) {
    return new String("TCO2TMP");
   }
   else if (inParam.equalsIgnoreCase("ALKALI") || inParam.equalsIgnoreCase("TOTAL_ALKALINITY")) {
    return new String("ALKI");
   }
   else if (inParam.equalsIgnoreCase("PCO2TMP")|| inParam.equalsIgnoreCase("partial_co2_temperature")) {
    return new String("PCOT");
   }
   else if (inParam.equalsIgnoreCase("PCO2") || inParam.equalsIgnoreCase("partial_pressure_of_co2")) {
    return new String("PCO2");
   }
   else if (inParam.equalsIgnoreCase("PH") || inParam.equalsIgnoreCase("PHPH")) {
    return new String("PH  ");
   }
   else if (inParam.equalsIgnoreCase("PHTEMP")) {
    return new String("PHTEMP");
   }
   else if (inParam.equalsIgnoreCase("NH4") || inParam.equalsIgnoreCase("ammonium") || inParam.equalsIgnoreCase("AMON")) {
    return new String("NH4 ");
   }
   else if (inParam.equalsIgnoreCase("BARIUM")) {
    return new String("BARI");
   }
   else if (inParam.equalsIgnoreCase("DELC13") || inParam.equalsIgnoreCase("carbon_13")) {
    return new String("C13 ");
   }
   else if (inParam.equalsIgnoreCase("C133ERR") || inParam.equalsIgnoreCase("FREON_113_ERROR")) {
    return new String("C113E");
   }
   else if (inParam.equalsIgnoreCase("C13ERR") || inParam.equalsIgnoreCase("CARBON_13_ERROR")) {
    return new String("C13E");
   }
   else if (inParam.equalsIgnoreCase("KR-85")|| inParam.equalsIgnoreCase("85_krypton")) {
    return new String("KR85");
   }
   else if (inParam.equalsIgnoreCase("KR85")) {
    return new String("KR85");
   }
   else if (inParam.equalsIgnoreCase("KR85ERR")) {
    return new String("KRER");
   }
   else if (inParam.equalsIgnoreCase("ARGON")) {
    return new String("ARGO");
   }
   else if (inParam.equalsIgnoreCase("ARGERR") || inParam.equalsIgnoreCase("ARGON_ERROR")) {
    return new String("ARGE");
   }
   else if (inParam.equalsIgnoreCase("AR-39") || inParam.equalsIgnoreCase("39_Argon")) {
    return new String("AR39");
   }
   else if (inParam.equalsIgnoreCase("AR39")) {
    return new String("AR39");
   }
   else if (inParam.equalsIgnoreCase("AR39ER")) {
    return new String("ARER");
   }
   else if (inParam.equalsIgnoreCase("NEON")) {
    return new String("NEON");
   }
   else if (inParam.equalsIgnoreCase("NEONER") || inParam.equalsIgnoreCase("NEON_ERROR")) {
    return new String("NEONEER");
   }
   else if (inParam.equalsIgnoreCase("RA-228") || inParam.equalsIgnoreCase("228_radium")) {
    return new String("R228");
   }
   else if (inParam.equalsIgnoreCase("RA228")) {
    return new String("R228");
   }
   else if (inParam.equalsIgnoreCase("R228ER")) {
    return new String("R8ER");
   }
   else if (inParam.equalsIgnoreCase("RA-226") || inParam.equalsIgnoreCase("226_radium")) {
    return new String("R226");
   }
   else if (inParam.equalsIgnoreCase("RA226")) {
    return new String("R226");
   }
   else if (inParam.equalsIgnoreCase("R226ER")) {
    return new String("R6ER");
   }
   else if (inParam.equalsIgnoreCase("O18/16") || inParam.equalsIgnoreCase("O18/O16") || inParam.equalsIgnoreCase("oxy18_oxy16")) {
    return new String("O18/O16");
   }
   else if (inParam.equalsIgnoreCase("O16/O16") || inParam.equalsIgnoreCase("o16_o16")) {
    return new String("O16/O16");
   }
   else if (inParam.equalsIgnoreCase("OXYNIT")) {
    return new String("OXYNIT");
   }
   else if (inParam.equalsIgnoreCase("REVPRS") || inParam.equalsIgnoreCase("REVERSING_THERMOMETER_PRESSURE")) {
    return new String("PREV");
   }
   else if (inParam.equalsIgnoreCase("REVTMP") || inParam.equalsIgnoreCase("REVERSING_THERMOMETER_TEMPERATURE")) {
    return new String("TREV");
   }
   else if (inParam.equalsIgnoreCase("SR-90")) {
    return new String("SR90");
   }
   else if (inParam.equalsIgnoreCase("SR90")) {
    return new String("SR90");
   }
   else if (inParam.equalsIgnoreCase("CS-137") || inParam.equalsIgnoreCase("137_cesium")) {
    return new String("C137");
   }
   else if (inParam.equalsIgnoreCase("CS137")) {
    return new String("C137");
   }
   else if (inParam.equalsIgnoreCase("IODATE")) {
    return new String("IDAT");
   }
   else if (inParam.equalsIgnoreCase("IODIDE")) {
    return new String("IDID");
   }
   else if (inParam.equalsIgnoreCase("IODIDE")) {
    return new String("IDID");
   }
   else if (inParam.equalsIgnoreCase("CH4") || inParam.equalsIgnoreCase("METHANE")|| inParam.equalsIgnoreCase("METHAN")) {
    return new String("CH4 ");
   }
   else if (inParam.equalsIgnoreCase("DON")|| inParam.equalsIgnoreCase("nitrogen_dissolved_organic")) {
    return new String("DON ");
   }
   else if (inParam.equalsIgnoreCase("N20")) {
    return new String("N20 ");
   }
   else if (inParam.equalsIgnoreCase("CHLORA") || inParam.equalsIgnoreCase("CHLA") || inParam.equalsIgnoreCase("chlorophyl_a")  || inParam.equalsIgnoreCase("CHPL")) {
    return new String("CHLA");
   }
   else if (inParam.equalsIgnoreCase("PPHYTN") || inParam.equalsIgnoreCase("phaeophytin") || inParam.equalsIgnoreCase("PHAE")) {
    return new String("PPHYTN");
   }
   else if (inParam.equalsIgnoreCase("POC") || inParam.equalsIgnoreCase("patriculate_organic_carbon") || inParam.equalsIgnoreCase("POCA")) {
    return new String("POC ");
   }
   else if (inParam.equalsIgnoreCase("PON") || inParam.equalsIgnoreCase("patriculate_organic_nitrogen")) {
    return new String("PON ");
   }
   else if (inParam.equalsIgnoreCase("BACT")) {
    return new String("BACT");
   }
   else if (inParam.equalsIgnoreCase("DOC") || inParam.equalsIgnoreCase("dissolved_organic_carbon") || inParam.equalsIgnoreCase("DOCA")) {
    return new String("DOC ");
   }
   else if (inParam.equalsIgnoreCase("COMON") || inParam.equalsIgnoreCase("carbon_monoxide")) {
    return new String("CO  ");
   }
   else if (inParam.equalsIgnoreCase("CH3CCL3")) {
    return new String("CHCL");
   }
   else if (inParam.equalsIgnoreCase("CALCITE_SATURATION")) {
    return new String("CALC_SAT");
   }
   else if (inParam.equalsIgnoreCase("ARAGONITE_SATURATION")) {
    return new String("ARAG_SAT");
   }
   else if (inParam.equalsIgnoreCase("CTDRAW")|| inParam.equalsIgnoreCase("ctd_raw")) {
    return new String("CTDRAW");
   }
   else if (inParam.equalsIgnoreCase("AZOTE")) {
    return new String("AZOTE");
   }
   else if (inParam.equalsIgnoreCase("F113ER")) {
    return new String("Freon 113 Error");
   }
   else if (inParam.equalsIgnoreCase("F12ER")) {
    return new String("Freon 12 Error");
   }
   else if (inParam.equalsIgnoreCase("F11ER")) {
    return new String("Freon 11 Error");
   }
   else if (inParam.equalsIgnoreCase("N2O")|| inParam.equalsIgnoreCase("nitrous_oxide")) {
    return new String("N2O");
   }
   else if (inParam.equalsIgnoreCase("PHAEO")) {
    return new String("PHAEO");
   }
   return null;
    }*/

  public static boolean isMissing(double d) {
    if (d == NQueryConstants.MISSINGVALUE || d == NQueryConstants.WOCEMISSINGVALUE ||
        d == NQueryConstants.EPICMISSINGVALUE) {
      return true;
    }
    else {
      return false;
    }

  }
}

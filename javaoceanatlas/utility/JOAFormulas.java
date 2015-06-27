/*
 * $Id: JOAFormulas.java,v 1.43 2005/11/01 21:47:23 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.io.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import org.w3c.dom.*;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import gov.noaa.pmel.text.*;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.eps2.*;
import ucar.multiarray.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.geom.Rectangle2D;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.specifications.*;
import javaoceanatlas.calculations.*;
import javaoceanatlas.io.CastIDRule;
import javaoceanatlas.io.CastNumberRule;
import javaoceanatlas.io.ConvertParameterNamesRule;
import javaoceanatlas.io.DepthConversionRule;
import javaoceanatlas.io.DestinationQCRule;
import javaoceanatlas.io.PreferPressureParameterRule;
import javaoceanatlas.io.QCConversionRule;
import javaoceanatlas.io.SectionIDRule;
import javaoceanatlas.io.TempConversionRule;

public class JOAFormulas {
	private static int NO_KEY = 0;
	private static int LW_KEY = 1;
	private static int FC_KEY = 2;
	private static int CC_KEY = 3;
	private static int MC_KEY = 4;
	private static int DEC_KEY = 5;
	private static int COD_KEY = 6;
	private static int SLD_KEY = 7;
	private static int CUD_KEY = 8;
	private static int MIN_KEY = 9;
	private static int MAX_KEY = 10;
	private static int INC_KEY = 11;
	private static int WOCE_KEY = 12;
	private static int ASCS_KEY = 14;
	private static int CURSOR_SIZE_KEY = 15;
	private static int CURSOR_SYMBOL_KEY = 16;
	private static int GEOFORMAT_SYMBOL_KEY = 17;
	private static int DATEFORMAT_SYMBOL_KEY = 18;
	private static int ENHANCEMENT_KEY = 19;
	private static int ENHANCED_COLOR_KEY = 20;
	private static int PT_KEY = 21;
	private static int LPCP_KEY = 22;

	private static int KEY_STATE = NO_KEY;
	private static double MIN = JOAConstants.MISSINGVALUE;
	private static double MAX = JOAConstants.MISSINGVALUE;
	private static double INC = JOAConstants.MISSINGVALUE;
	private static LatitudeFormat latDM;
	private static LongitudeFormat lonDM;
	private static GeoDate mGeoDate;
	private static int MAX_SCV = 100;
	private static double R3500 = 1028.1063;
	private static double R4 = 4.8314e-4; // referred to as C in Millero and
	// Poisson 1981
	private static double DR350 = 28.106331;
	private static int NX = 90; // # of longitudes
	private static int NZ = 33; // # of std pressure levels
	private static int NDX = 4; // size of x-grid spacing in gamma.nc
	private static int NDY = 4; // size of y-grid spacing in gamma.nc
	private static int MAX_INTVLS = 50;
	private static float[] mSavedLats = null;
	private static float[] mSavedLons = null;
	private static short[][] mGBottleArray = null;
	private static short[][] mGOceanArray = null;
	private static float[][][] mGSalinityArray = null;
	private static float[][][] mGTemperatureArray = null;
	private static float[][][] mGGammaArray = null;
	private static float[][][] mGAArray = null;
	private static float[] mGPresArray = null;
	private static BasicStroke mLineWidth = new BasicStroke(1);
	private static BasicStroke mLineWidth1 = new BasicStroke(1);
	private static double rCtoO = -(117.0 / 170.0);
	private static double rNtoO = -(16.0 / 170.0);
	private static boolean DEBUG = false;
	private static FeatureGroup currFG;

	private static Color MAPBGCOLOR = null;
	private static int BASIN;
	private static boolean CUSTOMRGN = false;
	private static double MINLAT;
	private static double MAXLAT;
	private static double LONLFT;
	private static double LONRHT;
	private static double CTRLAT;
	private static double CTRLON;
	private static boolean DRAWGRAT = false;
	private static double LATSPC;
	private static double LONSPC;
	private static Color GRATCOLOR = null;
	private static boolean RETAINASPECT = false;
	private static int PROJCODE;
	private static int REZCODE;
	private static Color COASTCOLOR = null;
	private static String CUSTOMCOASTPATH = null;
	private static String CUSTOMCOASTDESCRIP = null;
	private static int SYMBOLCODE;
	private static int SYMBOLSIZE;
	private static boolean PLOTSYMBOLS = true;
	private static int LINEWIDTH;
	private static boolean CONNECTSTNS = false;
	private static boolean CONNECTSTNSACROSS = true;
	private static boolean SECTIONLABELS = true;
	private static boolean ETOPOOVL = false;
	private static String ETOPOCOLORBAR = null;
	private static String[] ETOPOFILENAMES = null;
	private static int ISOBATHCTR = 0;
	private static double[] IOSBATHVALS = new double[120];
	private static Color[] ISOBATHCOLORS = new Color[120];
	private static String[] ISOBATHPATHS = new String[120];
	private static String[] ISOBATHDESCRIPS = new String[120];
	private static boolean PLOTSTNLABELS = false;
	private static boolean PLOTGRATLABELS = true;
	private static boolean ISGLOBE = true;
	private static int eCnt = 0;
	private static int LABELOFFSET = 5;
	private static int LABELANGLE = 45;
	private static int CONTOURPREC = 2;
  /**
   * Parsing state:
   * Initial state before anything read.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int INITIAL = 0;
  /**
   * Parsing state:
   * State in which a possible sign and
   * possible leading zeros have been read.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int LEADZEROS = 1;
  /**
   * Parsing state:
   * State in which a possible sign and
   * at least one non-zero digit
   * has been read followed by some number of
   * zeros.  The decimal place has no
   * been encountered yet.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int MIDZEROS = 2;
  /**
   * Parsing state:
   * State in which a possible sign and
   * at least one non-zero digit
   * has been read.  The decimal place has no
   * been encountered yet.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int DIGITS = 3;
  /**
   * Parsing state:
   * State in which only a possible sign,
   * leading zeros, and a decimal point
   * have been encountered.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int LEADZEROSDOT = 4;
  /**
   * Parsing state:
   * State in which a possible sign,
   * at least one nonzero digit and a
   * decimal point have been encountered.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int DIGITSDOT = 5;
  /**
   * Parsing state:
   * State in which the exponent symbol
   * 'E' has been encountered.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int MANTISSA = 6;
  /**
   * Parsing state:
   * State in which the exponent symbol
   * 'E' has been encountered followed
   * by a possible sign or some number
   * of digits.
   *
   * @since ostermillerutils 1.00.00
   */
  private final static int MANTISSADIGIT = 7;

 /**
  * The exponent of the digits if a
  * decimal place were inserted after
  * the first digit.
  *
  * @since ostermillerutils 1.00.00
  */
 /**
  * positive if true, negative if false.
  *
  * @since ostermillerutils 1.00.00
  */
 /**
  * True if this number has no non-zero digits.
  *
  * @since ostermillerutils 1.00.00

  /**
   * Parse a number from the given string.
   * A valid number has an optional sign, some digits
   * with an optional decimal point, and an optional
   * scientific notation part consisting of an 'E' followed
   * by an optional sign, followed by some digits.
   *
   * @param number String representation of a number.
   * @throws NumberFormatException if the string is not a valid number.
   *
   * @since ostermillerutils 1.00.00
   */
  public static int getSignificantDigits(String number) throws NumberFormatException {
	    boolean isZero = false; 
	    boolean sign = true;
	    int mantissa = -1;

      int length = number.length();
      StringBuffer digits = new StringBuffer(length);
      int state = INITIAL;
      int mantissaStart = -1;
      boolean foundMantissaDigit = false;
      // sometimes we don't know if a zero will be
      // significant or not when it is encountered.
      // keep track of the number of them so that
      // the all can be made significant if we find
      // out that they are.
      int zeroCount = 0;
      int leadZeroCount = 0;

      for (int i=0; i<length; i++){
          char c = number.charAt(i);
          switch (c){
              case '.': {
                  switch (state){
                      case INITIAL:
                      case LEADZEROS: {
                          state = LEADZEROSDOT;
                      } break;
                      case MIDZEROS: {
                          // we now know that these zeros
                          // are more than just trailing place holders.
                          for (int j=0; j<zeroCount; j++){
                              digits.append('0');
                          }
                          zeroCount = 0;
                          state = DIGITSDOT;
                      } break;
                      case DIGITS: {
                          state = DIGITSDOT;
                      } break;
                      default: {
                          throw new NumberFormatException (
                              "Unexpected character '" + c + "' at position " + i
                          );
                      }
                  }
              } break;
              case '+':{
                  switch (state){
                      case INITIAL: {
                          sign = true;
                          state = LEADZEROS;
                      } break;
                      case MANTISSA: {
                          state = MANTISSADIGIT;
                      } break;
                      default: {
                          throw new NumberFormatException (
                              "Unexpected character '" + c + "' at position " + i
                          );
                      }
                  }
              } break;
              case '-': {
                  switch (state){
                      case INITIAL: {
                          sign = false;
                          state = LEADZEROS;
                      } break;
                      case MANTISSA: {
                          state = MANTISSADIGIT;
                      } break;
                      default: {
                          throw new NumberFormatException (
                              "Unexpected character '" + c + "' at position " + i
                          );
                      }
                  }
              } break;
              case '0': {
                  switch (state){
                      case INITIAL:
                      case LEADZEROS: {
                          // only significant if number
                          // is all zeros.
                          zeroCount++;
                          leadZeroCount++;
                          state = LEADZEROS;
                      } break;
                      case MIDZEROS:
                      case DIGITS: {
                          // only significant if followed
                          // by a decimal point or nonzero digit.
                          mantissa++;
                          zeroCount++;
                          state = MIDZEROS;
                      } break;
                      case LEADZEROSDOT:{
                          // only significant if number
                          // is all zeros.
                          mantissa--;
                          zeroCount++;
                          state = LEADZEROSDOT;
                      } break;
                      case DIGITSDOT: {
                          // non-leading zeros after
                          // a decimal point are always
                          // significant.
                          digits.append(c);
                      } break;
                      case MANTISSA:
                      case MANTISSADIGIT: {
                          foundMantissaDigit = true;
                          state = MANTISSADIGIT;
                      } break;
                      default: {
                          throw new NumberFormatException (
                              "Unexpected character '" + c + "' at position " + i
                          );
                      }
                  }
              } break;
              case '1': case '2': case '3':
              case '4': case '5': case '6':
              case '7': case '8': case '9': {
                  switch (state){
                      case INITIAL:
                      case LEADZEROS:
                      case DIGITS: {
                          zeroCount = 0;
                          digits.append(c);
                          mantissa++;
                          state = DIGITS;
                      } break;
                      case MIDZEROS: {
                          // we now know that these zeros
                          // are more than just trailing place holders.
                          for (int j=0; j<zeroCount; j++){
                              digits.append('0');
                          }
                          zeroCount = 0;
                          digits.append(c);
                          mantissa++;
                          state = DIGITS;
                      } break;
                      case LEADZEROSDOT:
                      case DIGITSDOT: {
                          zeroCount = 0;
                          digits.append(c);
                          state = DIGITSDOT;
                      } break;
                      case MANTISSA:
                      case MANTISSADIGIT: {
                          state = MANTISSADIGIT;
                          foundMantissaDigit = true;
                      } break;
                      default: {
                          throw new NumberFormatException (
                              "Unexpected character '" + c + "' at position " + i
                          );
                      }
                  }
              } break;
              case 'E': case 'e': {
                  switch (state){
                      case INITIAL:
                      case LEADZEROS:
                      case DIGITS:
                      case LEADZEROSDOT:
                      case DIGITSDOT: {
                          // record the starting point of the mantissa
                          // so we can do a substring to get it back later
                          mantissaStart = i+1;
                          state = MANTISSA;
                      } break;
                      default: {
                          throw new NumberFormatException (
                              "Unexpected character '" + c + "' at position " + i
                          );
                      }
                  }
              } break;
              default: {
                  throw new NumberFormatException (
                      "Unexpected character '" + c + "' at position " + i
                  );
              }
          }
      }
      if (mantissaStart != -1){
          // if we had found an 'E'
          if (!foundMantissaDigit){
              // we didn't actually find a mantissa to go with.
              throw new NumberFormatException (
                  "No digits in mantissa."
              );
          }
          // parse the mantissa.
          mantissa += Integer.parseInt(number.substring(mantissaStart));
      }
      
      if (digits.length() == 0){
          if (zeroCount > 0){
              // if nothing but zeros all zeros are significant.
              for (int j=0; j<zeroCount; j++){
                  digits.append('0');
              }
              mantissa += leadZeroCount;
              isZero = true;
              sign = true;
          } else {
              // a hack to catch some cases that we could catch
              // by adding a ton of extra states.  Things like:
              // "e2" "+e2" "+." "." "+" etc.
              throw new NumberFormatException (
                  "No digits in number."
              );
          }
      }
      return digits.length();
  }

	@SuppressWarnings("unchecked")
	public static MapSpecification parseMapSpec(File file) throws Exception {
		MapSpecification mMapSpec = new MapSpecification();
		try {
			// parse as xml first
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			MapNotifyStr notifyStr = new MapNotifyStr();
			parser.setDocumentHandler(notifyStr);
			parser.parse(file.getPath());

			// turned parse stuff into a map
			mMapSpec.setProjection(PROJCODE);
			mMapSpec.setSymbol(SYMBOLCODE);
			mMapSpec.setPlotStnSymbols(PLOTSYMBOLS);
			mMapSpec.setSymbolSize(SYMBOLSIZE);
			mMapSpec.setLineWidth(LINEWIDTH);
			mMapSpec.setCurrBasin(BASIN);
			mMapSpec.setCustomMap(CUSTOMRGN);
			mMapSpec.setCoastLineRez(REZCODE);
			mMapSpec.setLatMax(MAXLAT);
			mMapSpec.setLatMin(MINLAT);
			mMapSpec.setLonRt(LONRHT);
			mMapSpec.setLonLft(LONLFT);
			mMapSpec.setCenLat(CTRLAT);
			mMapSpec.setCenLon(CTRLON);
			mMapSpec.setConnectStns(CONNECTSTNS);
			mMapSpec.setConnectStnsAcrossSections(CONNECTSTNSACROSS);
			mMapSpec.setDrawGraticule(DRAWGRAT);
			mMapSpec.setRetainProjAspect(RETAINASPECT);
			mMapSpec.setDrawLegend(true);
			mMapSpec.setLatGratSpacing(LATSPC);
			mMapSpec.setLonGratSpacing(LONSPC);
			mMapSpec.setPlotSectionLabels(SECTIONLABELS);
			mMapSpec.setBGColor(new Color(MAPBGCOLOR.getRed(), MAPBGCOLOR.getGreen(), MAPBGCOLOR.getBlue()));
			mMapSpec.setGratColor(new Color(GRATCOLOR.getRed(), GRATCOLOR.getGreen(), GRATCOLOR.getBlue()));
			mMapSpec.setCoastColor(new Color(COASTCOLOR.getRed(), COASTCOLOR.getGreen(), COASTCOLOR.getBlue()));
			mMapSpec.setGlobe(ISGLOBE);

			mMapSpec.setStnLabelOffset(LABELOFFSET);
			mMapSpec.setStnLabelAngle(LABELANGLE);
			mMapSpec.setContourLabelPrec(CONTOURPREC);

			mMapSpec.setColorFill(ETOPOOVL);
			if (mMapSpec.isColorFill()) {
				mMapSpec.setBathyColorBar(JOAFormulas.getColorBar(ETOPOCOLORBAR));
				mMapSpec.setNumEtopoFiles(0);
				for (int i = 0; i < eCnt; i++) {
					mMapSpec.setEtopoFile(ETOPOFILENAMES[i]);
				}
			}
			if (CUSTOMCOASTPATH != null) {
				mMapSpec.setCustCoastPath(new String(CUSTOMCOASTPATH));
			}
			if (CUSTOMCOASTDESCRIP != null) {
				mMapSpec.setCustCoastDescrip(new String(CUSTOMCOASTDESCRIP));
			}

			mMapSpec.setPlotStnLabels(PLOTSTNLABELS);
			mMapSpec.setPlotGratLabels(PLOTGRATLABELS);
		}
		catch (Exception xmlEx) {
			// xmlEx.printStackTrace();
		}
		finally {
			return mMapSpec;
		}
	}

	@SuppressWarnings("unchecked")
	public static MapSpecification parseMapSpec(String mapSpecAsXML) throws Exception {
		MapSpecification mMapSpec = new MapSpecification();
		try {
			// parse as xml first
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			MapNotifyStr notifyStr = new MapNotifyStr();
			parser.setDocumentHandler(notifyStr);
			StringReader sr = new StringReader(mapSpecAsXML);
			InputSource src = new InputSource(sr);
			try {
				parser.parse(src);
			}
			catch (Exception ex) {
				// silent accept values up exception
				System.out.println("here");
			}

			// turned parse stuff into a map
			mMapSpec.setProjection(PROJCODE);
			mMapSpec.setSymbol(SYMBOLCODE);
			mMapSpec.setPlotStnSymbols(PLOTSYMBOLS);
			mMapSpec.setSymbolSize(SYMBOLSIZE);
			mMapSpec.setLineWidth(LINEWIDTH);
			mMapSpec.setCurrBasin(BASIN);
			mMapSpec.setCustomMap(CUSTOMRGN);
			mMapSpec.setCoastLineRez(REZCODE);
			mMapSpec.setLatMax(MAXLAT);
			mMapSpec.setLatMin(MINLAT);
			mMapSpec.setLonRt(LONRHT);
			mMapSpec.setLonLft(LONLFT);
			mMapSpec.setCenLat(CTRLAT);
			mMapSpec.setCenLon(CTRLON);
			mMapSpec.setConnectStns(CONNECTSTNS);
			mMapSpec.setConnectStnsAcrossSections(CONNECTSTNSACROSS);
			mMapSpec.setDrawGraticule(DRAWGRAT);
			mMapSpec.setRetainProjAspect(RETAINASPECT);
			mMapSpec.setDrawLegend(true);
			mMapSpec.setLatGratSpacing(LATSPC);
			mMapSpec.setLonGratSpacing(LONSPC);
			mMapSpec.setPlotSectionLabels(SECTIONLABELS);
			mMapSpec.setBGColor(new Color(MAPBGCOLOR.getRed(), MAPBGCOLOR.getGreen(), MAPBGCOLOR.getBlue()));
			mMapSpec.setGratColor(new Color(GRATCOLOR.getRed(), GRATCOLOR.getGreen(), GRATCOLOR.getBlue()));
			mMapSpec.setCoastColor(new Color(COASTCOLOR.getRed(), COASTCOLOR.getGreen(), COASTCOLOR.getBlue()));
			mMapSpec.setGlobe(ISGLOBE);

			mMapSpec.setStnLabelOffset(LABELOFFSET);
			mMapSpec.setStnLabelAngle(LABELANGLE);
			mMapSpec.setContourLabelPrec(CONTOURPREC);

			mMapSpec.setColorFill(ETOPOOVL);
			if (mMapSpec.isColorFill()) {
				mMapSpec.setBathyColorBar(JOAFormulas.getColorBar(ETOPOCOLORBAR));
				mMapSpec.setNumEtopoFiles(0);
				for (int i = 0; i < eCnt; i++) {
					mMapSpec.setEtopoFile(ETOPOFILENAMES[i]);
				}
			}
			if (CUSTOMCOASTPATH != null) {
				mMapSpec.setCustCoastPath(new String(CUSTOMCOASTPATH));
			}
			if (CUSTOMCOASTDESCRIP != null) {
				mMapSpec.setCustCoastDescrip(new String(CUSTOMCOASTDESCRIP));
			}

			mMapSpec.setPlotStnLabels(PLOTSTNLABELS);
			mMapSpec.setPlotGratLabels(PLOTGRATLABELS);
		}
		catch (Exception xmlEx) {
			xmlEx.printStackTrace();
		}
		finally {
			return mMapSpec;
		}
	}

	public static NewColorBar parseCB(String cbAsXML) throws Exception {
		NewColorBar resultCB = null;
		try {
			// parse as xml first
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			CBNotifyStr notifyStr = new CBNotifyStr();
			parser.setDocumentHandler(notifyStr);
			StringReader sr = new StringReader(cbAsXML);
			InputSource src = new InputSource(sr);
			parser.parse(src);

			if (METADATATYPE == null) {
				resultCB = new NewColorBar(CVALS, VALS, NLEVELS, PARAM, UNITS, TITLE, DESCRIP);
			}
			else {
				resultCB = new NewColorBar(CVALS, VALS, NLEVELS, PARAM, UNITS, TITLE, DESCRIP, METADATATYPE);
			}
		}
		catch (Exception xmlEx) {
			// xmlEx.printStackTrace();
		}
		finally {
			return resultCB;
		}
	}

	public static boolean stringToBoolean(String sb) {
		if (sb.equalsIgnoreCase("true")) {
			return true;
		}
		else {
			return false;
		}
	}

	private static class PrefsNotifyStr extends HandlerBase {
		public void startDocument() throws SAXException {
		}

		public void startElement(String name, AttributeList amap) throws SAXException {
			if (name.equals("dapperserver")) {
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("URL")) {
							JOAConstants.DEFAULT_DAPPER_SERVERS.addElement(amap.getValue(i));
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			if (name.equals("wodprefs")) {
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						String key = amap.getName(i);
						String val = amap.getValue(i);
						if (key.equals("castidrule")) {
							JOAConstants.DEFAULT_CAST_ID_RULE = CastIDRule.fromValue(val);
						}
						else if (key.equals("castnumberrule")) {
							JOAConstants.DEFAULT_CAST_NUMBER_RULE = CastNumberRule.fromValue(val);
						}
						else if (key.equals("sectionidrule")) {
							JOAConstants.DEFAULT_SECTION_ID_RULE = SectionIDRule.fromValue(val);
						}
						else if (key.equals("convertparameternamesrule")) {
							JOAConstants.DEFAULT_CONVERT_PARAM_NAMES_RULE = ConvertParameterNamesRule.fromValue(val);
						}
						else if (key.equals("destinationqcrule")) {
							JOAConstants.DEFAULT_DEST_QC_RULE = DestinationQCRule.fromValue(val);
						}
						else if (key.equals("preferpressureparameterrule")) {
							JOAConstants.DEFAULT_PRES_PARAM_RULE = PreferPressureParameterRule.fromValue(val);
						}
						else if (key.equals("qcconversionrule")) {
							JOAConstants.DEFAULT_QC_PROCESSING_RULE = QCConversionRule.fromValue(val);
						}
						else if (key.equals("depthconversionrule")) {
							JOAConstants.DEFAULT_DEPTH_CONVERSION_RULE = DepthConversionRule.fromValue(val);
						}
						else if (key.equals("tempconversionrule")) {
							JOAConstants.DEFAULT_TEMP_CONV_RULE = TempConversionRule.fromValue(val);
						}
						else if (key.equals("collectmetadata")) {
							JOAConstants.DEFAULT_COLLECT_METADATA_RULE = stringToBoolean(val);
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			if (name.equals("featuregroup")) {
				// <featuregroup id="kPlots" name="Plots" enabled="true">
				boolean enabled = false;
				String fgname = null;
				String id = null;

				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("name")) {
							fgname = amap.getValue(i);
						}
						else if (amap.getName(i).equals("id")) {
							id = amap.getValue(i);
						}
						else if (amap.getName(i).equals("enabled")) {
							enabled = stringToBoolean(amap.getValue(i));
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				// make a new feature Group
				FeatureGroup fg = new FeatureGroup(id, fgname, enabled);
				currFG = fg;

				// add this to all feature Groups
				JOAConstants.JOA_FEATURESET.put(id, fg);
			}
			else if (name.equals("feature")) {
				// <feature name="Show General Preferences" id="kGeneral" version="1.0"
				// status="rel" enabled="true" visible="true"/>
				boolean enabled = false;
				boolean visible = false;
				String fname = null;
				String id = null;
				String version = null;
				String status = null;

				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("name")) {
							fname = amap.getValue(i);
						}
						else if (amap.getName(i).equals("id")) {
							id = amap.getValue(i);
						}
						else if (amap.getName(i).equals("status")) {
							status = amap.getValue(i);
						}
						else if (amap.getName(i).equals("version")) {
							version = amap.getValue(i);
						}
						else if (amap.getName(i).equals("enabled")) {
							enabled = stringToBoolean(amap.getValue(i));
						}
						else if (amap.getName(i).equals("visible")) {
							visible = stringToBoolean(amap.getValue(i));
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				// make a new feature
				ManagedFeature newMF = new ManagedFeature(null, id, fname, version, status, enabled, visible);

				// add this to the FeatureGroup
				currFG.addFeature(id, newMF);
			}
			else if (name.equals("builtincalcprefs")) {
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("calcmin")) {
							JOAConstants.DEFAULT_CALC_MIN = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("calcmax")) {
							JOAConstants.DEFAULT_CALC_MAX = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("calcdepthofmin")) {
							JOAConstants.DEFAULT_CALC_DEPTH_OF_MIN = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						if (amap.getName(i).equals("calcdepthofmax")) {
							JOAConstants.DEFAULT_CALC_DEPTH_OF_MAX = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("calcavg")) {
							JOAConstants.DEFAULT_CALC_AVERAGE = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("calcn")) {
							JOAConstants.DEFAULT_CALC_N = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("calcminnonmissingdepth")) {
							JOAConstants.DEFAULT_CALC_MIN_DEPTH_OF_NONMISSING = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("calcmaxnonmissingdepth")) {
							JOAConstants.DEFAULT_CALC_MAX_DEPTH_OF_NONMISSING = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("applycalcstousercalcs")) {
							JOAConstants.DEFAULT_APPLY_CALCS_TO_USER_CALCS = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("importprefs")) {
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("convertmass")) {
							JOAConstants.DEFAULT_CONVERT_MASS_TO_VOL = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("convertdepth")) {
							JOAConstants.DEFAULT_CONVERT_DEPTH = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("translate")) {
							JOAConstants.DEFAULT_TRANSLATE_PARAM_NAMES = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						if (amap.getName(i).equals("tolexicon")) {
							JOAConstants.DEFAULT_LEXICON = Integer.valueOf(amap.getValue(i)).intValue();
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("databaseprefs")) {
				for (int i = 0; i < amap.getLength(); i++) {
					// if (amap.getName(i).equals("dburi")) {
					// try {
					// JOAConstants.DEFAULT_DB_URI = new String(amap.getValue(i));
					// }
					// catch (Exception ex) {
					// }
					// }
					// else if (amap.getName(i).equals("dbport")) {
					// try {
					// JOAConstants.DEFAULT_DB_PORT = new String(amap.getValue(i));
					// }
					// catch (Exception ex) {
					// }
					// }
					// else if (amap.getName(i).equals("dbusername")) {
					// try {
					// JOAConstants.DEFAULT_DB_USERNAME = new String(amap.getValue(i));
					// }
					// catch (Exception ex) {
					// }
					// }
					if (amap.getName(i).equals("dbdefaultdir")) {
						try {
							JOAConstants.DEFAULT_DB_SAVE_DIR = new String(amap.getValue(i));
						}
						catch (Exception ex) {
						}
					}
				}
			}
			else if (name.equals("enhancementprefs")) {
				KEY_STATE = ENHANCEMENT_KEY;
				boolean b0 = true, b1 = true, b2 = true;
				double d1 = 0.0, d2 = 0.0;
				int i1 = 5;
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("enlargecurrsymbol")) {
							b0 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						if (amap.getName(i).equals("replacecurrsymbol")) {
							b1 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						if (amap.getName(i).equals("usecontrastingcolor")) {
							b2 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						if (amap.getName(i).equals("enlargecurrsymbolby")) {
							d1 = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						if (amap.getName(i).equals("enlargecontrastingsymbolby")) {
							d2 = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						if (amap.getName(i).equals("contrastingsymbol")) {
							i1 = Integer.valueOf(amap.getValue(i)).intValue();
						}
					}
					catch (Exception ex) {
					}
				}

				JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL = b0;
				JOAConstants.DEFAULT_ENHANCE_REPLACE_CURRENT_SYMBOL = b1;
				JOAConstants.DEFAULT_ENHANCE_USE_CONTRASTING_COLOR = b2;

				JOAConstants.DEFAULT_ENHANCE_ENLARGE_CURRENT_SYMBOL_BY = d1;
				JOAConstants.DEFAULT_ENHANCE_ENLARGE_CONTRASTING_SYMBOL_BY = d2;
				JOAConstants.DEFAULT_ENHANCE_CONTRASTING_SYMBOL = i1 - 1;
			}
			else if (name.equals("contrastingcolor")) {
				KEY_STATE = ENHANCED_COLOR_KEY;
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_ENHANCE_CONTRASTING_COLOR = new Color(red, green, blue);

			}
			else if (name.equals("positionformat")) {
				KEY_STATE = GEOFORMAT_SYMBOL_KEY;
			}
			else if (name.equals("dateformat")) {
				KEY_STATE = DATEFORMAT_SYMBOL_KEY;
			}
			else if (name.equals("browsingcursorsize")) {
				KEY_STATE = CURSOR_SIZE_KEY;
			}
			else if (name.equals("browsingcursorsymbol")) {
				KEY_STATE = CURSOR_SYMBOL_KEY;
			}
			else if (name.equals("connectlinewidth")) {
				KEY_STATE = LW_KEY;
			}
			else if (name.equals("plottitles")) {
				KEY_STATE = PT_KEY;
			}
			else if (name.equals("defaultautoscalecolors")) {
				KEY_STATE = ASCS_KEY;
			}
			else if (name.equals("lineplotcolorpalette")) {
				KEY_STATE = LPCP_KEY;
			}
			else if (name.equals("framecolor")) {
				KEY_STATE = FC_KEY;
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_FRAME_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("contentcolor")) {
				KEY_STATE = CC_KEY;
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_CONTENTS_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("missingvalcolor")) {
				KEY_STATE = MC_KEY;
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_MISSINGVAL_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("decimation")) {
				KEY_STATE = DEC_KEY;
				JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
			}
			else if (name.equals("constantdecimation")) {
				KEY_STATE = COD_KEY;
				JOAConstants.DEFAULT_CONSTANT_CTD_DECIMATION = true;
				JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
			}
			else if (name.equals("stdleveldecimation")) {
				KEY_STATE = SLD_KEY;
				JOAConstants.DEFAULT_STD_LEVEL_CTD_DECIMATION = true;
				JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
			}
			else if (name.equals("customdecimation")) {
				KEY_STATE = CUD_KEY;
				JOAConstants.DEFAULT_CUSTOM_CTD_DECIMATION = true;
				JOAConstants.DEFAULT_NO_CTD_DECIMATION = false;
			}
			else if (name.equals("minvalue")) {
				KEY_STATE = MIN_KEY;
			}
			else if (name.equals("maxvalue")) {
				KEY_STATE = MAX_KEY;
			}
			else if (name.equals("increment")) {
				KEY_STATE = INC_KEY;
			}
			else if (name.equals("woceprefs")) {
				KEY_STATE = WOCE_KEY;
				boolean b0 = true, b1 = true, b3 = false, b4 = false, b5 = false, b6 = false, b7 = false, b8 = false;
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("convqc")) {
							b0 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("convtemp")) {
							b1 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("repleq3")) {
							b3 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						if (amap.getName(i).equals("repleq4")) {
							b4 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("repleq7")) {
							b5 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("repleq8")) {
							b6 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("replalleq4")) {
							b7 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						else if (amap.getName(i).equals("replgaseq34")) {
							b8 = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
					}
					catch (Exception ex) {
					}
				}

				JOAConstants.DEFAULT_CONVERT_QCS = b0;
				JOAConstants.DEFAULT_CONVERT_WOCE_TEMPS = b1;
				JOAConstants.DEFAULT_SET_MSG_QBEQ3 = b3;
				JOAConstants.DEFAULT_SET_MSG_QBEQ4 = b4;
				JOAConstants.DEFAULT_SET_MSG_QBEQ7 = b5;
				JOAConstants.DEFAULT_SET_MSG_QBEQ8 = b6;
				JOAConstants.DEFAULT_SET_ALL_PARAMS_MSG_BQBEQ4 = b7;
				JOAConstants.DEFAULT_SET_GAS_PARAMS_MSG_BQBEQ3_AND_O2QBEQ4 = b8;
			}
			else if (name.equals("stylesheet")) {
				for (int i = 0; i < amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("axisvaluefont")) {
							JOAConstants.DEFAULT_AXIS_VALUE_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("axislabelfont")) {
							JOAConstants.DEFAULT_AXIS_LABEL_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("isopycnalfont")) {
							JOAConstants.DEFAULT_ISOPYCNAL_LABEL_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("plottitlefont")) {
							JOAConstants.DEFAULT_PLOT_TITLE_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("colorbarfont")) {
							JOAConstants.DEFAULT_COLORBAR_LABEL_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("mapvaluefont")) {
							JOAConstants.DEFAULT_MAP_VALUE_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("mapstnlabelfont")) {
							JOAConstants.DEFAULT_MAP_STN_LABEL_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("contourxsecvaluefont")) {
							JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("contourxseclabelfont")) {
							JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_FONT = new String(amap.getValue(i));
						}
						else if (amap.getName(i).equals("regressionlabelfont")) {
							JOAConstants.DEFAULT_REGRESSION_FONT = new String(amap.getValue(i));
						}

						if (amap.getName(i).equals("axislabelsize")) {
							JOAConstants.DEFAULT_AXIS_LABEL_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("axisvaluesize")) {
							JOAConstants.DEFAULT_AXIS_VALUE_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("isopycnalsize")) {
							JOAConstants.DEFAULT_ISOPYCNAL_LABEL_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("plottitlesize")) {
							JOAConstants.DEFAULT_PLOT_TITLE_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("colorbarsize")) {
							JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("mapvaluesize")) {
							JOAConstants.DEFAULT_MAP_VALUE_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("mapstnlabelsize")) {
							JOAConstants.DEFAULT_MAP_STN_LABEL_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("contourxsecvaluesize")) {
							JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("contourxseclabelsize")) {
							JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("regressionlabelsize")) {
							JOAConstants.DEFAULT_REGRESSION_FONT_SIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}

						if (amap.getName(i).equals("axislabelstyle")) {
							JOAConstants.DEFAULT_AXIS_LABEL_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("axisvaluestyle")) {
							JOAConstants.DEFAULT_AXIS_VALUE_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("isopycnalstyle")) {
							JOAConstants.DEFAULT_ISOPYCNAL_LABEL_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("plottitlestyle")) {
							JOAConstants.DEFAULT_PLOT_TITLE_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("colorbarstyle")) {
							JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("mapvaluestyle")) {
							JOAConstants.DEFAULT_MAP_VALUE_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("mapstnlabelstyle")) {
							JOAConstants.DEFAULT_MAP_STN_LABEL_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("contourxsecvaluestyle")) {
							JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("contourxseclabelstyle")) {
							JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						else if (amap.getName(i).equals("regressionlabelstyle")) {
							JOAConstants.DEFAULT_REGRESSION_FONT_STYLE = Integer.valueOf(amap.getValue(i)).intValue();
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("axislabelcolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_AXIS_LABEL_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("axisvaluecolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_AXIS_VALUE_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("isopycnalcolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_ISOPYCNAL_LABEL_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("plottitlecolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_PLOT_TITLE_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("colorbarcolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_COLORBAR_LABEL_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("mapvaluecolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_MAP_VALUE_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("mapstnlabelcolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_MAP_STN_LABEL_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("contourxsecvaluecolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_CONTOUR_XSEC_VALUE_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("contourxseclabelcolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_CONTOUR_XSEC_LABEL_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("regressionlabelcolor")) {
				int red = 0, green = 0, blue = 0;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							red = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							red = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							green = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							green = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							blue = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							blue = 150;
						}
					}
				}
				JOAConstants.DEFAULT_REGRESSION_FONT_COLOR = new Color(red, green, blue);
			}
			else if (name.equals("paramsubprefs")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("defaultsalt")) {
						try {
							JOAConstants.DEFAULT_SALINITY_VARIABLE = Integer.valueOf(amap.getValue(i)).intValue() - 1;
						}
						catch (Exception ex) {
							JOAConstants.DEFAULT_SALINITY_VARIABLE = JOAConstants.BOTTLE_SALINITY;
						}
					}
					else if (amap.getName(i).equals("saltsub")) {
						try {
							JOAConstants.DEFAULT_SALINITY_SUBSTITUTION = Integer.valueOf(amap.getValue(i)).intValue() - 1;
						}
						catch (Exception ex) {
							JOAConstants.DEFAULT_SALINITY_SUBSTITUTION = JOAConstants.CTD_SALINITY;
						}
					}
					else if (amap.getName(i).equals("defaulto2")) {
						try {
							JOAConstants.DEFAULT_O2_VARIABLE = Integer.valueOf(amap.getValue(i)).intValue() - 1;
						}
						catch (Exception ex) {
							JOAConstants.DEFAULT_O2_VARIABLE = JOAConstants.BOTTLE_O2;
						}
					}
					else if (amap.getName(i).equals("o2sub")) {
						try {
							JOAConstants.DEFAULT_O2_SUBSTITUTION = Integer.valueOf(amap.getValue(i)).intValue() - 1;
						}
						catch (Exception ex) {
							JOAConstants.DEFAULT_O2_SUBSTITUTION = JOAConstants.CTD_O2;
						}
					}
				}
			}
			else {
				KEY_STATE = NO_KEY;
			}
		}

		public void characters(char[] ch, int start, int len) throws SAXException {
			String strVal = new String(ch, start, len);
			if (KEY_STATE == CURSOR_SIZE_KEY) {
				try {
					JOAConstants.DEFAULT_CURSOR_SIZE = Integer.valueOf(strVal).intValue();
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_CURSOR_SIZE = 8;
				}
			}
			else if (KEY_STATE == CURSOR_SYMBOL_KEY) {
				try {
					JOAConstants.DEFAULT_CURSOR_SYMBOL = Integer.valueOf(strVal).intValue();
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_CURSOR_SYMBOL = JOAConstants.SYMBOL_CIRCLEFILLED;
				}
			}
			else if (KEY_STATE == GEOFORMAT_SYMBOL_KEY) {
				try {
					JOAConstants.DEFAULT_POSITION_FORMAT = Integer.valueOf(strVal).intValue() - 1;
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_POSITION_FORMAT = JOAConstants.DEC_MINUTES_GEO_DISPLAY;
				}
			}
			else if (KEY_STATE == DATEFORMAT_SYMBOL_KEY) {
				try {
					JOAConstants.DEFAULT_DATE_FORMAT = new String(strVal);
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_DATE_FORMAT = new String("dd-mm-yy");
				}
			}
			else if (KEY_STATE == LW_KEY) {
				try {
					JOAConstants.CONNECT_LINE_WIDTH = Integer.valueOf(strVal).intValue();
				}
				catch (Exception ex) {
					JOAConstants.CONNECT_LINE_WIDTH = 1;
				}
			}
			else if (KEY_STATE == PT_KEY) {
				try {
					JOAConstants.DEFAULT_PLOT_TITLES = Boolean.valueOf(strVal).booleanValue();
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_PLOT_TITLES = true;
				}
			}
			else if (KEY_STATE == ASCS_KEY) {
				try {
					JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME = Integer.valueOf(strVal).intValue();
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_AUTOSCALE_COLOR_SCHEME = 0;
				}
			}
			else if (KEY_STATE == LPCP_KEY) {
				try {
					JOAConstants.DEFAULT_LINE_PLOT_PALETTE = new String(strVal);
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_LINE_PLOT_PALETTE = "";
				}
			}
			else if (KEY_STATE == COD_KEY) {
				try {
					JOAConstants.DEFAULT_DECIMATE_CONSTANT = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					JOAConstants.DEFAULT_DECIMATE_CONSTANT = 5.0;
				}
			}
			else if (KEY_STATE == SLD_KEY) {
				JOAConstants.DEFAULT_DECIMATE_STD_LEVEL = new String(strVal);
			}
			else if (KEY_STATE == MIN_KEY) {
				try {
					MIN = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					MIN = JOAConstants.MISSINGVALUE;
				}
			}
			else if (KEY_STATE == MAX_KEY) {
				try {
					MAX = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					MAX = JOAConstants.MISSINGVALUE;
				}
			}
			else if (KEY_STATE == INC_KEY) {
				try {
					INC = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					INC = JOAConstants.MISSINGVALUE;
				}
			}
		}

		public void endElement(String name) throws SAXException {
			if (name.equals("increment")) {
				// build a new triplet
				JOAConstants.DEFAULT_CUSTOM_DECIMATE_TRIPLETS[JOAConstants.DEFAULT_NUMBER_OF_CUSTOM_TRIPLETS++] = new Triplet(
				    MIN, MAX, INC);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void readPreferences(String prefsAsString) throws PreferencesErrorException {
		// get the default preferences
		// xml-based preferences
		try {
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			PrefsNotifyStr notifyStr = new PrefsNotifyStr();
			parser.setDocumentHandler(notifyStr);
			StringReader sr = new StringReader(prefsAsString);
			InputSource src = new InputSource(sr);
			parser.parse(src);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isMissing(double inVal) {
		if (JOAConstants.USECUSTOMMISSINGVALUE) {
			return inVal == JOAConstants.CUSTOMMISSINGVALUE;
		}
		else {
			if (inVal == JOAConstants.MISSINGVALUE || inVal == JOAConstants.WOCEMISSINGVALUE
			    || inVal == JOAConstants.EPICMISSINGVALUE || inVal == JOAConstants.UOTMISSINGVALUE) { return true; }
		}
		return false;
	}

	public static boolean isGasVar(String inType) {
		String joaName = paramNameToJOAName(inType);
		
//		OXYGEN	-> O2
//		CFC-11	-> F11
//		CFC-12	-> F12
//		CFC-113	-> F113
//		SF6				 not in lexicon
//		CCL4		-> CCL4
//		HE (whatever we call it) Helium is WOCE terminology -> HELI
//		DELHE 	-> DELHE  we also have DELH3 in our lexicon
//		TALK		-> TALK (added TALK to lexicon)
//		TCO2		-> TCO2 (added TCO2 to lexicon)
//		PCO2		-> PCO2
		
		if (inType.indexOf("O2") == 0) {
			return true;
		}
		else if (inType.indexOf("F11") >= 0) {
			return true;
		}
		else if (inType.indexOf("F12") >= 0) {
			return true;
		}
		else if (inType.indexOf("F113") >= 0) {
			return true;
		}
		else if (inType.toLowerCase().indexOf("sf6") >= 0) {
			return true;
		}
		else if (inType.indexOf("CCL4") >= 0) {
			return true;
		}
		else if (inType.indexOf("HELI") >= 0) {
			return true;
		}
		else if (inType.indexOf("DELH") >= 0) {
			return true;
		}
		else if (inType.indexOf("ALKI") >= 0) {
			return true;
		}
		else if (inType.indexOf("TCO2") >= 0) {
			return true;
		}
		else if (inType.indexOf("PCO2") >= 0) {
			return true;
		}
		else if (inType.indexOf("FCO2") >= 0) {
			return true;
		}
		else if (inType.indexOf("F113ER") >= 0) {
			return true;
		}
		else if (inType.indexOf("F11ER") >= 0) {
			return true;
		}
		else if (inType.indexOf("F12ER") >= 0) {
			return true;
		}
		else if (inType.indexOf("CCL4ER") >= 0) {
			return true;
		}
		else if (inType.indexOf("HEER") >= 0) {
			return true;
		}
		else if (inType.indexOf("DELHER") >= 0) {
			return true;
		}
		else if (inType.indexOf("DELH3") >= 0) {
			return true;
		}
		else if (inType.indexOf("DELH3ER") >= 0) {
			return true;
		}
		return false;
	}
	
	public static boolean isNutrient(String inType) {
		if (inType.indexOf("NO3") == 0) {
			return true;
		}
		else if (inType.indexOf("NO2") == 0) {
			return true;
		}
		else if (inType.indexOf("PO4") == 0) {
			return true;
		}
		else if (inType.indexOf("SIO3") == 0) {
			return true;
		}
		else if (inType.indexOf("NH4") == 0) {
			return true;
		}
		else if (inType.indexOf("UREA") >= 0 || inType.indexOf("UR") == 0) { return true; }
		return false;
	}

	public static boolean isBottleVar(String inType) {
		if (inType.indexOf("CTD") >= 0) { return false; }
		return true;
	}

	public static boolean isO2Var(String inType) {
		if (inType.indexOf("OXY") >= 0) {
			return true;
		}
		else if (inType.indexOf("O2") >= 0) { return true; }
		else if (inType.indexOf("CTDO") >= 0) { return true; }
		return false;
	}

	public static boolean isTempVar(String inType) {
		if (inType.indexOf("TEMP") >= 0) {
			return true;
		}
		else if (inType.indexOf("TEM") >= 0) {
			return true;
		}
		else if (inType.indexOf("CTDT") >= 0) {
			return true;
		}
		else if (inType.indexOf("T_") >= 0) { return true; }
		return false;
	}

	/**
	 * Convert a pair of WOCE quality bytes to an IGOSS quality code.
	 * 
	 * @param qb1
	 *          first WOCE QB
	 * @param qb2
	 *          second WOCE QB
	 * 
	 * @return IGOSS quality code
	 * 
	 *         The WMO IGOSS observation quality codes are: 0 No quality control
	 *         yet assigned to this element 1 The element appears to be correct 2
	 *         The element is probably good 3 The element is probably bad 4 The
	 *         element appears erroneous 5 The element has been changed 6 to 8
	 *         Reserved for future use 9 The element is missing
	 * 
	 *         The WOCE Observation QC Codes are: 1 Sample for this measurement
	 *         was drawn from water bottle but analysis not received. Note that if
	 *         water is drawn for any measurement from a water bottle, the quality
	 *         flag for that parameter must be set equal to 1 initially to ensure
	 *         that all water samples are accounted for. 2 Acceptable measurement.
	 *         3 Questionable measurement. 4 Bad measurement. 5 Not reported. 6
	 *         Mean of replicate measurements (Number of replicates should be
	 *         specified in the Ñ.DOC file and replicate data tabulated). 7 Manual
	 *         chromatographic peak measurement. 8 Irregular digital
	 *         chromatographic peak integration. 9 Sample not drawn for this
	 *         measurement from this bottle.
	 * 
	 *         A0000440 QC Codes: 1 - suspect value
	 */

	public static int translateWOCESampleQBToIGOSS(int qb) {
		if (qb == 1) {
			return 0;
		}
		else if (qb == 2) {
			return 1;
		}
		else if (qb == 3) {
			return 3;
		}
		else if (qb == 4) {
			return 4;
		}
		else if (qb == 5) {
			return 0;
		}
		else if (qb == 6) {
			return 2;
		}
		else if (qb == 7) {
			return 2;
		}
		else if (qb == 8) {
			return 2;
		}
		else if (qb == 9) { return 9; }
		return qb;
	}

	/**
	 * Convert a TAO quality bytes to an IGOSS quality code.
	 * 
	 * @param qb
	 *          TAO QB
	 * 
	 * @return IGOSS quality code
	 */
	public static int translateTAOQBToIGOSS(int qb) {
		if (qb == 0) {
			return 9;
		}
		else if (qb == 1) {
			return 1;
		}
		else if (qb == 2) {
			return 1;
		}
		else if (qb == 3) {
			return 5;
		}
		else if (qb == 4) {
			return 2;
		}
		else if (qb == 5) { return 3; }
		return 9;
	}

	public static boolean isTAOQC(EPSVariable taoVar) {
		// TAO variables always have an EPIC code attached
		int epicVarCode = taoVar.getIntegerAttributeValue("epic_code");
		if (epicVarCode != 0) {
			if (epicVarCode > 5000 && epicVarCode < 6000) { return true; }
		}
		return false;
	}

	public static boolean isMatchingTAOQC(EPSVariable taoVar, EPSVariable possibleQCVar) {
		// TAO variables always have an EPIC code attached
		int epicVarCode = taoVar.getIntegerAttributeValue("epic_code");
		int possibleEpicVarCode = possibleQCVar.getIntegerAttributeValue("epic_code");

		if ((possibleEpicVarCode > 5000 && possibleEpicVarCode < 6000) && (epicVarCode == (possibleEpicVarCode - 5000))) { return true; }

		return false;
	}

	/**
	 * Convert a WOCE CTD quality bytes to an IGOSS quality code.
	 * 
	 * @param qb1
	 *          WOCE CTD QB
	 * 
	 * @return IGOSS quality code
	 */
	public static int translateWOCECTDQBToIGOSS(int qb) {
		if (qb == 1) {
			return 0;
		}
		else if (qb == 2) {
			return 1;
		}
		else if (qb == 3) {
			return 2;
		}
		else if (qb == 4) {
			return 4;
		}
		else if (qb == 5) {
			return 0;
		}
		else if (qb == 6) {
			return 2;
		}
		else if (qb == 7) {
			return 2;
		}
		else if (qb == 9) { return 9; }
		return qb;
	}

	/**
	 * Convert a IGOSS quality code to an WOCE quality code.
	 * 
	 * @param qb
	 *          IGOSS QB
	 * 
	 * @return WOCE quality code
	 */
	public static int translateIGOSSQBToWOCE(int qb) {
		if (qb == 0) {
			return 1;
		}
		else if (qb == 1) {
			return 2;
		}
		else if (qb == 2) {
			return 3;
		}
		else if (qb == 4) {
			return 4;
		}
		else if (qb == 9) { return 9; }
		return qb;
	}

	/**
	 * Convert a TAO quality code to an WOCE quality code.
	 * 
	 * @param qb
	 *          TAO QB
	 * 
	 * @return WOCE quality code
	 */
	public static int translateTAOQBToWOCE(int qb) {
		if (qb == 1) {
			return 2;
		}
		else if (qb == 2) {
			return 2;
		}
		else if (qb == 3) {
			return 3;
		}
		else if (qb == 4) { return 3; }
		return qb;
	}

	/**
	 * Convert a pair of WOCE quality bytes to an IGOSS quality code.
	 * 
	 * @param qb1
	 *          WOCE QB
	 * 
	 * @return IGOSS quality code
	 */
	public static int translateWOCEBottleQBToIGOSS(int qb) {
		if (qb == 1) {
			return 0;
		}
		else if (qb == 2) {
			return 1;
		}
		else if (qb == 3) {
			return 3;
		}
		else if (qb == 4) {
			return 4;
		}
		else if (qb == 5) {
			return 0;
		}
		else if (qb == 6) {
			return 4;
		}
		else if (qb == 7) {
			return 4;
		}
		else if (qb == 8) {
			return 4;
		}
		else if (qb == 9) { return 9; }
		return qb;
	}

	// class methods
	public static int getNumberVisStns(FileViewer fv) {
		int s = 0;
		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)fv.mOpenFiles.elementAt(fc);
			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);

				if (sech.mNumCasts == 0) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}
					s++;
				}
			}
		}
		return s - 1;
	}

	public static int getNumberOfItems(String inString) {
		int i = 0;
		int delimPos = 0;
		int startPos = 0;
		while (true) {
			delimPos = inString.indexOf(JOAConstants.DELIMITER, startPos);

			if (delimPos == -1) { return i + 1; }

			startPos = delimPos + 1;
			i++;
		}
	}

	public static int numItems(String inString) {
		int startPos = 0;
		int delimPos = 0;
		int itemCnt = 0;
		for (int i = 0;; i++) {
			delimPos = inString.indexOf(JOAConstants.DELIMITER, startPos);

			if (delimPos == -1) {
				// hit end of string
				break;
			}

			startPos = delimPos + 1;
			itemCnt++;
		}
		return itemCnt;
	}

	public static String getItem(String inString, int item) {
		if (item - 1 > numItems(inString)) { return null; }
		int startPos = 0;
		int delimPos = 0;
		int lastPos = 0;
		for (int i = 0; i < item; i++) {
			delimPos = inString.indexOf(JOAConstants.DELIMITER, startPos);

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

	public static int getItemNumber(String inString, String testStr) {
		testStr = testStr.toUpperCase();

		// test whether testStr is in inString
		if (inString.indexOf(testStr) == -1) { return -1; }

		// loop on the items
		int ic = 1;
		while (true) {
			String itemStr = getItem(inString, ic);
			if (itemStr == null) { return -1; }
			if (itemStr.indexOf(testStr) >= 0) { return ic; }
			ic++;
		}
	}

	public static int getItemNumber(String inString, String[] testStrs, int[] strict) {
		int numTests = testStrs.length;

		for (int i = 0; i < numTests; i++) {
			String testStr = testStrs[i].toUpperCase();

			// loop on the items
			int ic = 1;
			while (true) {
				String itemStr = getItem(inString, ic);
				if (itemStr == null) {
					break;
				}

				if (strict[i] == JOAConstants.MATCHES) {
					if (itemStr.equalsIgnoreCase(testStr)) { return ic; }
				}
				else if (strict[i] == JOAConstants.STARTSWITH) {
					if (itemStr.startsWith(testStr)) { return ic; }
				}
				else if (strict[i] == JOAConstants.CONTAINS) {
					if (itemStr.indexOf(testStr) >= 0) { return ic; }
				}
				ic++;
			}
		}
		return -1;
	}

	public static int getIntItem(String inString, int item) throws NumberFormatException {
		try {
			int retVal = Integer.valueOf(getItem(inString, item)).intValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			return JOAConstants.MISSINGVALUE; // throw ex;
		}
	}

	public static short getShortItem(String inString, int item) throws NumberFormatException {
		try {
			short retVal = Short.valueOf(getItem(inString, item)).shortValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			return (short)JOAConstants.MISSINGVALUE; // throw ex;
		}
	}

	public static double getDoubleItem(String inString, int item) throws NumberFormatException {
		try {
			double retVal = Double.valueOf(getItem(inString, item)).doubleValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			return JOAConstants.MISSINGVALUE; // throw ex;
		}
	}

	public static long getLongItem(String inString, int item) throws NumberFormatException {
		try {
			long retVal = Long.valueOf(getItem(inString, item)).longValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			return JOAConstants.MISSINGVALUE; // throw ex;
		}
	}

	// todo: get the precesion on a param by param basis
	public static short paramNameToEditPrecision(String inParam) {
		return 3;
	}

	public static String paramNameToWOCEName(String inParam) {
		String retVal = inParam;
		String testParam = inParam.toUpperCase();

		// always translate pressure
		if (testParam.equalsIgnoreCase("PRES") || testParam.equalsIgnoreCase("CTDP")
		    || testParam.equalsIgnoreCase("PRESSURE") || testParam.equalsIgnoreCase("REVPRS")) {
			retVal = new String("CTDPRS");
		}

		if (testParam.startsWith("SIG")) {
			retVal = new String(testParam);
		}

		// always translate depth
		if (testParam.equalsIgnoreCase("DEPM") || testParam.startsWith("DEP")) {
			retVal = new String("DEPTH");
		}

		if (testParam.startsWith("SIG")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("PO")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("NO")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("BTLNBR")) {
			retVal = new String("BTLN");
		}
		else if (testParam.equalsIgnoreCase("TEM") || testParam.equalsIgnoreCase("TEMP")
		    || testParam.equalsIgnoreCase("TEMPERATURE") || testParam.equalsIgnoreCase("T")
		    || testParam.equalsIgnoreCase("TE")) {
			retVal = new String("CTDTMP");
		}
		else if (testParam.equalsIgnoreCase("SAMPNO")) {
			retVal = new String("SAMP");
		}
		else if (testParam.equalsIgnoreCase("CTDS")) {
			retVal = new String("CTDSAL");
		}
		else if (testParam.equalsIgnoreCase("CTDO")) {
			retVal = new String("CTDOXY");
		}
		else if (testParam.equalsIgnoreCase("WTHT")) {
			retVal = new String("THETA");
		}
		else if (testParam.equalsIgnoreCase("NO2NO3")) {
			retVal = new String("NO2NO3");
		}
		else if (testParam.equalsIgnoreCase("Bedfort")) {
			retVal = new String("BEDFORT");
		}
		else if (testParam.equalsIgnoreCase("SALT")) {
			retVal = new String("SALNITY");
		}
		else if (testParam.startsWith("SPPT") || testParam.startsWith("BSAL") || testParam.equalsIgnoreCase("SAL")
		    || testParam.equalsIgnoreCase("S") || testParam.equalsIgnoreCase("SA") || testParam.equalsIgnoreCase("S0")
		    || testParam.equalsIgnoreCase("PSAL") || testParam.equalsIgnoreCase("SALINITY")) {
			retVal = new String("SALNITY");
		}
		else if (testParam.equalsIgnoreCase("OXY") || testParam.equalsIgnoreCase("DOXY") || testParam.startsWith("O2")
		    || testParam.equalsIgnoreCase("BO") || testParam.equalsIgnoreCase("O") || testParam.startsWith("BOTTLE_OXYGEN")) {
			retVal = new String("OXYGEN");
		}
		else if (testParam.equalsIgnoreCase("SILICATE") || testParam.equalsIgnoreCase("SILICAT")
		    || testParam.equalsIgnoreCase("SILI") || testParam.startsWith("SIL") || testParam.equalsIgnoreCase("SI")
		    || testParam.equalsIgnoreCase("SIO3") || testParam.equalsIgnoreCase("SIO4")
		    || testParam.equalsIgnoreCase("SLCA")) {
			retVal = new String("SILCAT");
		}
		else if (testParam.equalsIgnoreCase("NO2+NO3") || testParam.equalsIgnoreCase("NO2_NO3")
		    || testParam.equalsIgnoreCase("nitrate_plus_nitrite")) {
			retVal = new String("NO2+NO3");
		}
		else if (testParam.equalsIgnoreCase("NITRATE") || testParam.equalsIgnoreCase("NTRA") || testParam.startsWith("NO3")) {
			retVal = new String("NITRAT");
		}
		else if (testParam.equalsIgnoreCase("NITRITE") || testParam.equalsIgnoreCase("NTRI") || testParam.startsWith("NO2")) {
			retVal = new String("NITRIT");
		}
		else if (testParam.equalsIgnoreCase("PHOSPHATE") || testParam.equalsIgnoreCase("PHOS")
		    || testParam.startsWith("PO4")) {
			retVal = new String("PHSPHT");
		}
		else if (testParam.equalsIgnoreCase("F11") || testParam.equalsIgnoreCase("Freon_11")) {
			retVal = new String("CFC-11");
		}
		else if (testParam.equalsIgnoreCase("F12") || testParam.equalsIgnoreCase("Freon_12")) {
			retVal = new String("CFC-12");
		}
		else if (testParam.equalsIgnoreCase("F113") || testParam.equalsIgnoreCase("Freon_113")) {
			retVal = new String("CFC113");
		}
		else if (testParam.equalsIgnoreCase("CF113ER") || testParam.equalsIgnoreCase("freon_113_error")
		    || testParam.equalsIgnoreCase("C113ER")) {
			retVal = new String("F113ER");
		}
		else if (testParam.equalsIgnoreCase("CF11ER") || testParam.equalsIgnoreCase("freon_11_error")) {
			retVal = new String("F11ER");
		}
		else if (testParam.equalsIgnoreCase("CF12ER") || testParam.equalsIgnoreCase("freon_12_error")) {
			retVal = new String("F12ER");
		}
		else if (testParam.equalsIgnoreCase("TRITIUM") || testParam.equalsIgnoreCase("TRIT")) {
			retVal = new String("TRITUM");
		}
		else if (testParam.equalsIgnoreCase("TRER") || testParam.equalsIgnoreCase("TRITIUM_ERROR")) {
			retVal = new String("TRITER");
		}
		else if (testParam.equalsIgnoreCase("HELI")) {
			retVal = new String("HELIUM");
		}
		else if (testParam.equalsIgnoreCase("HEER") || testParam.equalsIgnoreCase("HELIUM_ERROR")) {
			retVal = new String("HELIER");
		}
		else if (testParam.equalsIgnoreCase("DELHE") || testParam.equalsIgnoreCase("DELTA_HELIUM")) {
			retVal = new String("DELH");
		}
		else if (testParam.equalsIgnoreCase("DELH3") || testParam.equalsIgnoreCase("DELTA_HELIUM_3")) {
			retVal = new String("DELHE3");
		}
		else if (testParam.equalsIgnoreCase("DH3E") || testParam.equalsIgnoreCase("DELTA_HELIUM_ERROR")) {
			retVal = new String("DELHER");
		}
		else if (testParam.equalsIgnoreCase("O18O16")) {
			retVal = new String("O18/O16");
		}
		else if (testParam.equalsIgnoreCase("C14") || testParam.equalsIgnoreCase("carbon_14")) {
			retVal = new String("DELC14");
		}
		else if (testParam.equalsIgnoreCase("FCO2TMP")
		    || testParam.equalsIgnoreCase("fugacity_co2_temperature")) {
			retVal = new String("FCO2TEMP");
		}
		else if (testParam.equalsIgnoreCase("FCO2")
		    || testParam.equalsIgnoreCase("fugacity_co2")) {
			retVal = new String("FCO2");
		}
		else if (testParam.equalsIgnoreCase("CCL4") || testParam.equalsIgnoreCase("CARBON_TETRACHLORIDE")) {
			retVal = new String("CCL4");
		}
		else if (testParam.equalsIgnoreCase("CCL4ER") || testParam.equalsIgnoreCase("CARBON_TETRACHLORIDE_ERROR")) {
			retVal = new String("CCL4ER");
		}
		else if (testParam.equalsIgnoreCase("C14E")) {
			retVal = new String("C14ERR");
		}
		else if (testParam.equalsIgnoreCase("TCO2") || testParam.equalsIgnoreCase("total_carbon")
		    || testParam.equalsIgnoreCase("total_co2")) {
			retVal = new String("TCARBN");
		}
		else if (testParam.equalsIgnoreCase("TCO2TMP") || testParam.equalsIgnoreCase("total_co2_temperature")) {
			retVal = new String("TCO2TMP");
		}
		else if (testParam.equalsIgnoreCase("ALKI") || testParam.equalsIgnoreCase("TOTAL_ALKALINITY")
				 || testParam.equalsIgnoreCase("TALK")) {
			retVal = new String("ALKALI");
		}
		else if (testParam.equalsIgnoreCase("PCOT") || testParam.equalsIgnoreCase("partial_co2_temperature")) {
			retVal = new String("PCO2TMP");
		}
		else if (testParam.equalsIgnoreCase("PCO2") || testParam.equalsIgnoreCase("partial_pressure_of_co2")) {
			retVal = new String("PCO2");
		}
		else if (testParam.equalsIgnoreCase("PH") || testParam.equalsIgnoreCase("PHPH")) {
			retVal = new String("PH");
		}
		else if (testParam.equalsIgnoreCase("PHTEMP")) {
			retVal = new String("PHTEMP");
		}
		else if (testParam.equalsIgnoreCase("NH4") || testParam.equalsIgnoreCase("ammonium")
		    || testParam.equalsIgnoreCase("AMON")) {
			retVal = new String("NH4");
		}
		else if (testParam.equalsIgnoreCase("BARIUM")) {
			retVal = new String("BARI");
		}
		else if (testParam.equalsIgnoreCase("C13") || testParam.equalsIgnoreCase("carbon_13")) {
			retVal = new String("DELC13");
		}
		else if (testParam.equalsIgnoreCase("C113ERR") || testParam.equalsIgnoreCase("FREON_113_ERROR")) {
			retVal = new String("C113E");
		}
		else if (testParam.equalsIgnoreCase("C13ERR") || testParam.equalsIgnoreCase("CARBON_13_ERROR")) {
			retVal = new String("C13E");
		}
		else if (testParam.equalsIgnoreCase("KR85") || testParam.equalsIgnoreCase("85_krypton")) {
			retVal = new String("KR-85");
		}
		else if (testParam.equalsIgnoreCase("KRER")) {
			retVal = new String("KR85ERR");
		}
		else if (testParam.equalsIgnoreCase("ARGO")) {
			retVal = new String("ARGON");
		}
		else if (testParam.equalsIgnoreCase("ARGE") || testParam.equalsIgnoreCase("ARGON_ERROR")) {
			retVal = new String("ARGERR");
		}
		else if (testParam.equalsIgnoreCase("AR39") || testParam.equalsIgnoreCase("39_Argon")) {
			retVal = new String("AR-39");
		}
		else if (testParam.equalsIgnoreCase("ARER")) {
			retVal = new String("AR39ER");
		}
		else if (testParam.equalsIgnoreCase("NEON")) {
			retVal = new String("NEON");
		}
		else if (testParam.equalsIgnoreCase("NEONEER") || testParam.equalsIgnoreCase("NEON_ERROR")) {
			retVal = new String("NEONER");
		}
		else if (testParam.equalsIgnoreCase("R228") || testParam.equalsIgnoreCase("228_radium")) {
			retVal = new String("RA-228");
		}
		else if (testParam.equalsIgnoreCase("RA228")) {
			retVal = new String("R228");
		}
		else if (testParam.equalsIgnoreCase("R8ER")) {
			retVal = new String("R228ER");
		}
		else if (testParam.equalsIgnoreCase("R226") || testParam.equalsIgnoreCase("226_radium")) {
			retVal = new String("RA-226");
		}
		else if (testParam.equalsIgnoreCase("R6ER")) {
			retVal = new String("R226ER");
		}
		else if (testParam.equalsIgnoreCase("O18/16") || testParam.equalsIgnoreCase("O18/O16")
		    || testParam.equalsIgnoreCase("oxy18_oxy16")) {
			retVal = new String("O18/O16");
		}
		else if (testParam.equalsIgnoreCase("O16/O16") || testParam.equalsIgnoreCase("o16_o16")) {
			retVal = new String("O16/O16");
		}
		else if (testParam.equalsIgnoreCase("OXYNIT")) {
			retVal = new String("OXYNIT");
		}
		else if (testParam.equalsIgnoreCase("PREV") || testParam.equalsIgnoreCase("REVERSING_THERMOMETER_PRESSURE")) {
			retVal = new String("REVPRS");
		}
		else if (testParam.equalsIgnoreCase("TREV") || testParam.equalsIgnoreCase("REVERSING_THERMOMETER_TEMPERATURE")) {
			retVal = new String("REVTMP");
		}
		else if (testParam.equalsIgnoreCase("SR90")) {
			retVal = new String("SR-90");
		}
		else if (testParam.equalsIgnoreCase("SR90")) {
			retVal = new String("SR90");
		}
		else if (testParam.equalsIgnoreCase("C137") || testParam.equalsIgnoreCase("137_cesium")) {
			retVal = new String("CS-137");
		}
		else if (testParam.equalsIgnoreCase("CS137")) {
			retVal = new String("C137");
		}
		else if (testParam.equalsIgnoreCase("IODATE")) {
			retVal = new String("IDAT");
		}
		else if (testParam.equalsIgnoreCase("IODIDE")) {
			retVal = new String("IDID");
		}
		else if (testParam.equalsIgnoreCase("CH4") || testParam.equalsIgnoreCase("METHANE")
		    || testParam.equalsIgnoreCase("METHAN")) {
			retVal = new String("CH4");
		}
		else if (testParam.equalsIgnoreCase("DON") || testParam.equalsIgnoreCase("nitrogen_dissolved_organic")) {
			retVal = new String("DON");
		}
		else if (testParam.equalsIgnoreCase("N20")) {
			retVal = new String("N20");
		}
		else if (testParam.equalsIgnoreCase("CHLORA") || testParam.equalsIgnoreCase("CHLA")
		    || testParam.equalsIgnoreCase("chlorophyl_a") || testParam.equalsIgnoreCase("CHPL")) {
			retVal = new String("CHLA");
		}
		else if (testParam.equalsIgnoreCase("CHLOROPHYL") || testParam.equalsIgnoreCase("CHLOROPHYLL")) {
			retVal = new String("CHPL");
		}
		else if (testParam.equalsIgnoreCase("PPHYTN") || testParam.equalsIgnoreCase("phaeophytin")
		    || testParam.equalsIgnoreCase("PHAE")) {
			retVal = new String("PPHYTN");
		}
		else if (testParam.equalsIgnoreCase("POC") || testParam.equalsIgnoreCase("patriculate_organic_carbon")
		    || testParam.equalsIgnoreCase("POCA")) {
			retVal = new String("POC");
		}
		else if (testParam.equalsIgnoreCase("PON") || testParam.equalsIgnoreCase("patriculate_organic_nitrogen")) {
			retVal = new String("PON");
		}
		else if (testParam.equalsIgnoreCase("BACT")) {
			retVal = new String("BACT");
		}
		else if (testParam.equalsIgnoreCase("DOC") || testParam.equalsIgnoreCase("dissolved_organic_carbon")
		    || testParam.equalsIgnoreCase("DOCA")) {
			retVal = new String("DOC");
		}
		else if (testParam.equalsIgnoreCase("COMON") || testParam.equalsIgnoreCase("carbon_monoxide")) {
			retVal = new String("CO");
		}
		else if (testParam.equalsIgnoreCase("CH3CCL3")) {
			retVal = new String("CHCL");
		}
		else if (testParam.equalsIgnoreCase("CALCITE_SATURATION")) {
			retVal = new String("CALC_SAT");
		}
		else if (testParam.equalsIgnoreCase("ARAGONITE_SATURATION")) {
			retVal = new String("ARAG_SAT");
		}
		else if (testParam.equalsIgnoreCase("CTDRAW") || testParam.equalsIgnoreCase("ctd_raw")) {
			retVal = new String("CTDRAW");
		}
		else if (testParam.equalsIgnoreCase("AZOTE")) {
			retVal = new String("AZOTE");
		}
		else if (testParam.equalsIgnoreCase("F113ER")) {
			retVal = new String("Freon 113 Error");
		}
		else if (testParam.equalsIgnoreCase("F12ER")) {
			retVal = new String("Freon 12 Error");
		}
		else if (testParam.equalsIgnoreCase("F11ER")) {
			retVal = new String("Freon 11 Error");
		}
		else if (testParam.equalsIgnoreCase("N2O") || testParam.equalsIgnoreCase("nitrous_oxide")) {
			retVal = new String("N2O");
		}
		else if (testParam.equalsIgnoreCase("PHAEO")) {
			retVal = new String("PHAEO");
		}
		else if (testParam.equalsIgnoreCase("NH4") || testParam.equalsIgnoreCase("amonium")) {
			retVal = new String("NH4");
		}
		else if (testParam.equalsIgnoreCase("UR")) {
			retVal = new String("UREA");
		}
		if (DEBUG) {
			System.out.println("paramNameToWOCEName translated " + testParam + " to " + retVal);
		}

		return retVal;
	}

	public static boolean isParamReversed(String testParam) {
		if (testParam.equalsIgnoreCase("PRES") || testParam.equalsIgnoreCase("CTDPRS")
		    || testParam.equalsIgnoreCase("PRESSURE")) { return true; }

		if (testParam.startsWith("DEPTH") || testParam.equalsIgnoreCase("DEPM") || testParam.startsWith("DEP")) { return true; }
		return false;
	}

	public static String paramNameToJOAName(String inParam) {
		String retVal = inParam.trim();
		String testParam = inParam.toUpperCase().trim();

		// always translate pressure
		if (testParam.equalsIgnoreCase("PRES") || testParam.equalsIgnoreCase("CTDPRS")
		    || testParam.equalsIgnoreCase("PRESSURE")) {
			retVal = new String("PRES");
		}
		if (testParam.startsWith("SIG")) {
			retVal = new String(testParam);
		}

		// always translate depth
		if (testParam.startsWith("DEPTH") || testParam.equalsIgnoreCase("DEPM") || testParam.startsWith("DEP")) {
			retVal = new String("DEPTH");
		}

		if (testParam.startsWith("SIG")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("PO")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("NO")) {
			retVal = new String(testParam);
		}
		else if (testParam.equalsIgnoreCase("BTLNBR")) {
			retVal = new String("BTLN");
		}
		else if (testParam.equalsIgnoreCase("TEM") || testParam.equalsIgnoreCase("TEMP")
		    || testParam.equalsIgnoreCase("TEMPERATURE") || testParam.equalsIgnoreCase("T")
		    || testParam.equalsIgnoreCase("TE") || testParam.equalsIgnoreCase("CTDTMP")
		    || testParam.equalsIgnoreCase("TEMPERATUR") || testParam.toUpperCase().indexOf("ARGO_TEMPERATURE") >= 0) {
			retVal = new String("TEMP");
		}
		else if (testParam.equalsIgnoreCase("SAMPNO")) {
			retVal = new String("SAMP");
		}
		else if (testParam.equalsIgnoreCase("CTDSAL")) {
			retVal = new String("CTDS");
		}
		else if (testParam.equalsIgnoreCase("CTDOXY")) {
			retVal = new String("CTDO");
		}
		else if (testParam.equalsIgnoreCase("THETA")) {
			retVal = new String("WTHT");
		}
		else if (testParam.equalsIgnoreCase("NO2NO3")) {
			retVal = new String("NO2NO3");
		}
		else if (testParam.equalsIgnoreCase("Bedfort")) {
			retVal = new String("BEDFORT");
		}
		else if (testParam.startsWith("SPPT") || testParam.startsWith("BSAL") || testParam.equalsIgnoreCase("SAL")
		    || testParam.equalsIgnoreCase("S") || testParam.equalsIgnoreCase("SA") || testParam.equalsIgnoreCase("SALNTY")
		    || testParam.equalsIgnoreCase("S0") || testParam.equalsIgnoreCase("SALNITY")
		    || testParam.equalsIgnoreCase("PSAL") || testParam.equalsIgnoreCase("SALINITY")
		    || testParam.toUpperCase().indexOf("ARGO_SALINITY") >= 0) {
			retVal = new String("SALT");
		}
		else if (testParam.equalsIgnoreCase("OXYGEN") || testParam.equalsIgnoreCase("OXY")
		    || testParam.equalsIgnoreCase("DOXY") || testParam.toUpperCase().startsWith("O2") || testParam.equalsIgnoreCase("BO")
		    || testParam.equalsIgnoreCase("O") || testParam.toUpperCase().startsWith("BOTTLE_OXYGEN")) {
			retVal = new String("O2");
		}
		else if (testParam.equalsIgnoreCase("SILICATE") || testParam.equalsIgnoreCase("SILICAT")
		    || testParam.equalsIgnoreCase("SILI") || testParam.equalsIgnoreCase("SIL") || testParam.equalsIgnoreCase("SI")
		    || testParam.equalsIgnoreCase("SIO3") || testParam.equalsIgnoreCase("SIO4")
		    || testParam.equalsIgnoreCase("SLCA") || testParam.equalsIgnoreCase("SILCAT")) {
			retVal = new String("SIO3");
		}
		else if (testParam.equalsIgnoreCase("NO2+NO3") || testParam.equalsIgnoreCase("NO2_NO3")
		    || testParam.equalsIgnoreCase("nitrate_plus_nitrite")) {
			retVal = new String("NO2+NO3");
		}
		else if (testParam.equalsIgnoreCase("NITRATE") || testParam.equalsIgnoreCase("NITRAT")
		    || testParam.equalsIgnoreCase("NTRA") || testParam.toUpperCase().startsWith("NO3")) {
			retVal = new String("NO3");
		}
		else if (testParam.equalsIgnoreCase("NITRITE") || testParam.equalsIgnoreCase("NITRIT")
		    || testParam.equalsIgnoreCase("NTRI") || testParam.toUpperCase().startsWith("NO2")) {
			retVal = new String("NO2");
		}
		else if (testParam.equalsIgnoreCase("PHOSPHATE") || testParam.equalsIgnoreCase("PHOS")
		    || testParam.equalsIgnoreCase("PHSPHT") || testParam.toUpperCase().startsWith("PO4")) {
			retVal = new String("PO4");
		}
		else if (testParam.equalsIgnoreCase("CFC-11") || testParam.equalsIgnoreCase("Freon_11")) {
			retVal = new String("F11");
		}
		else if (testParam.equalsIgnoreCase("CFC-12") || testParam.equalsIgnoreCase("Freon_12")) {
			retVal = new String("F12");
		}
		else if (testParam.equalsIgnoreCase("CFC-113") || testParam.equalsIgnoreCase("Freon_113")) {
			retVal = new String("F113");
		}
		else if (testParam.equalsIgnoreCase("CFC113")) {
			retVal = new String("F113");
		}
		else if (testParam.equalsIgnoreCase("CF113ER") || testParam.equalsIgnoreCase("freon_113_error")
		    || testParam.equalsIgnoreCase("C113ER")) {
			retVal = new String("F113ER");
		}
		else if (testParam.equalsIgnoreCase("CF11ER") || testParam.equalsIgnoreCase("freon_11_error")) {
			retVal = new String("F11ER");
		}
		else if (testParam.equalsIgnoreCase("CF12ER") || testParam.equalsIgnoreCase("freon_12_error")) {
			retVal = new String("F12ER");
		}
		else if (testParam.equalsIgnoreCase("TRITIUM") || testParam.equalsIgnoreCase("TRITUM")) {
			retVal = new String("TRIT");
		}
		else if (testParam.equalsIgnoreCase("TRITER") || testParam.equalsIgnoreCase("TRITIUM_ERROR")) {
			retVal = new String("TRER");
		}
		else if (testParam.equalsIgnoreCase("HELIUM") || testParam.equalsIgnoreCase("HE")) {
			retVal = new String("HELI");
		}
		else if (testParam.equalsIgnoreCase("HELIER") || testParam.equalsIgnoreCase("HELIUM_ERROR")) {
			retVal = new String("HEER");
		}
		else if (testParam.equalsIgnoreCase("DELHE") || testParam.equalsIgnoreCase("DELTA_HELIUM")) {
			retVal = new String("DELH");
		}
		else if (testParam.equalsIgnoreCase("DELHE3") || testParam.equalsIgnoreCase("DELTA_HELIUM_3")) {
			retVal = new String("DELH3");
		}
		else if (testParam.equalsIgnoreCase("DELHER") || testParam.equalsIgnoreCase("DELTA_HELIUM_ERROR")) {
			retVal = new String("DELHER");
		}
		else if (testParam.equalsIgnoreCase("O18O16")) {
			retVal = new String("O18O16");
		}
		else if (testParam.equalsIgnoreCase("DELC14") || testParam.equalsIgnoreCase("carbon_14")) {
			retVal = new String("C14");
		}
		else if (testParam.equalsIgnoreCase("FCO2TMP") || testParam.equalsIgnoreCase("fugacity_co2_temperature")) {
			retVal = new String("FCO2TEMP");
		}
		else if (testParam.equalsIgnoreCase("FCO2") || testParam.equalsIgnoreCase("fugacity_co2")) {
			retVal = new String("FCO2");
		}
		else if (testParam.equalsIgnoreCase("CCL4") || testParam.equalsIgnoreCase("CARBON_TETRACHLORIDE")) {
			retVal = new String("CCL4");
		}
		else if (testParam.equalsIgnoreCase("CCL4ER") || testParam.equalsIgnoreCase("CARBON_TETRACHLORIDE_ERROR")) {
			retVal = new String("CCL4ER");
		}
		else if (testParam.equalsIgnoreCase("C14ERR")) {
			retVal = new String("C14E");
		}
		else if (testParam.equalsIgnoreCase("TCARBN") || testParam.equalsIgnoreCase("total_carbon")
		    || testParam.equalsIgnoreCase("total_co2") || testParam.equalsIgnoreCase("tco2")) {
			retVal = new String("TCO2");
		}
		else if (testParam.equalsIgnoreCase("TCO2TMP") || testParam.equalsIgnoreCase("total_co2_temperature")) {
			retVal = new String("TCO2TMP");
		}
		else if (testParam.equalsIgnoreCase("ALKALI") || testParam.equalsIgnoreCase("TOTAL_ALKALINITY")
		    || testParam.equalsIgnoreCase("ALKALINITY") || testParam.equalsIgnoreCase("TALK")) {
			retVal = new String("ALKI");
		}
		else if (testParam.equalsIgnoreCase("PCO2TMP") || testParam.equalsIgnoreCase("partial_co2_temperature")) {
			retVal = new String("PCOT");
		}
		else if (testParam.equalsIgnoreCase("PCO2") || testParam.equalsIgnoreCase("partial_pressure_of_co2")) {
			retVal = new String("PCO2");
		}
		else if (testParam.equalsIgnoreCase("PH") || testParam.equalsIgnoreCase("PHPH")) {
			retVal = new String("PH");
		}
		else if (testParam.equalsIgnoreCase("PHTEMP")) {
			retVal = new String("PHTEMP");
		}
		else if (testParam.equalsIgnoreCase("NH4") || testParam.equalsIgnoreCase("ammonium")
		    || testParam.equalsIgnoreCase("AMON")) {
			retVal = new String("NH4");
		}
		else if (testParam.equalsIgnoreCase("BARIUM")) {
			retVal = new String("BARI");
		}
		else if (testParam.equalsIgnoreCase("DELC13") || testParam.equalsIgnoreCase("carbon_13")) {
			retVal = new String("C13");
		}
		else if (testParam.equalsIgnoreCase("C113ERR") || testParam.equalsIgnoreCase("FREON_113_ERROR")) {
			retVal = new String("C113E");
		}
		else if (testParam.equalsIgnoreCase("C13ERR") || testParam.equalsIgnoreCase("CARBON_13_ERROR")) {
			retVal = new String("C13E");
		}
		else if (testParam.equalsIgnoreCase("KR-85") || testParam.equalsIgnoreCase("85_krypton")) {
			retVal = new String("KR85");
		}
		else if (testParam.equalsIgnoreCase("KR85ERR")) {
			retVal = new String("KRER");
		}
		else if (testParam.equalsIgnoreCase("ARGON")) {
			retVal = new String("ARGO");
		}
		else if (testParam.equalsIgnoreCase("ARGERR") || testParam.equalsIgnoreCase("ARGON_ERROR")) {
			retVal = new String("ARGE");
		}
		else if (testParam.equalsIgnoreCase("AR-39") || testParam.equalsIgnoreCase("39_Argon")) {
			retVal = new String("AR39");
		}
		else if (testParam.equalsIgnoreCase("AR39ER")) {
			retVal = new String("ARER");
		}
		else if (testParam.equalsIgnoreCase("NEON")) {
			retVal = new String("NEON");
		}
		else if (testParam.equalsIgnoreCase("NEONER") || testParam.equalsIgnoreCase("NEON_ERROR")) {
			retVal = new String("NEONEER");
		}
		else if (testParam.equalsIgnoreCase("RA-228") || testParam.equalsIgnoreCase("228_radium")) {
			retVal = new String("R228");
		}
		else if (testParam.equalsIgnoreCase("RA228")) {
			retVal = new String("R228");
		}
		else if (testParam.equalsIgnoreCase("R228ER")) {
			retVal = new String("R8ER");
		}
		else if (testParam.equalsIgnoreCase("RA-226") || testParam.equalsIgnoreCase("226_radium")) {
			retVal = new String("R226");
		}
		else if (testParam.equalsIgnoreCase("RA226")) {
			retVal = new String("R226");
		}
		else if (testParam.equalsIgnoreCase("R226ER")) {
			retVal = new String("R6ER");
		}
		else if (testParam.equalsIgnoreCase("O18/16") || testParam.equalsIgnoreCase("O18/O16")
		    || testParam.equalsIgnoreCase("oxy18_oxy16")) {
			retVal = new String("O18/O16");
		}
		else if (testParam.equalsIgnoreCase("O16/O16") || testParam.equalsIgnoreCase("o16_o16")) {
			retVal = new String("O16/O16");
		}
		else if (testParam.equalsIgnoreCase("OXYNIT")) {
			retVal = new String("OXYNIT");
		}
		else if (testParam.equalsIgnoreCase("REVPRS") || testParam.equalsIgnoreCase("REVERSING_THERMOMETER_PRESSURE")) {
			retVal = new String("PREV");
		}
		else if (testParam.equalsIgnoreCase("REVTMP") || testParam.equalsIgnoreCase("REVERSING_THERMOMETER_TEMPERATURE")) {
			retVal = new String("TREV");
		}
		else if (testParam.equalsIgnoreCase("SR-90") || testParam.equalsIgnoreCase("SR90")) {
			retVal = new String("SR90");
		}
		else if (testParam.equalsIgnoreCase("CS-137") || testParam.equalsIgnoreCase("137_cesium")) {
			retVal = new String("C137");
		}
		else if (testParam.equalsIgnoreCase("CS137")) {
			retVal = new String("C137");
		}
		else if (testParam.equalsIgnoreCase("IODATE")) {
			retVal = new String("IDAT");
		}
		else if (testParam.equalsIgnoreCase("IODIDE")) {
			retVal = new String("IDID");
		}
		else if (testParam.equalsIgnoreCase("CH4") || testParam.equalsIgnoreCase("METHANE")
		    || testParam.equalsIgnoreCase("METHAN")) {
			retVal = new String("CH4");
		}
		else if (testParam.equalsIgnoreCase("DON") || testParam.equalsIgnoreCase("nitrogen_dissolved_organic")) {
			retVal = new String("DON");
		}
		else if (testParam.equalsIgnoreCase("N20")) {
			retVal = new String("N20");
		}
		else if (testParam.equalsIgnoreCase("CHLORA") || testParam.equalsIgnoreCase("CHLA")
		    || testParam.equalsIgnoreCase("chlorophyl_a") || testParam.equalsIgnoreCase("CHPL")) {
			retVal = new String("CHLA");
		}
		else if (testParam.equalsIgnoreCase("CHLOROPHYL") || testParam.equalsIgnoreCase("CHLOROPHYLL")) {
			retVal = new String("CHPL");
		}
		else if (testParam.equalsIgnoreCase("PPHYTN") || testParam.equalsIgnoreCase("phaeophytin")
		    || testParam.equalsIgnoreCase("PHAE")) {
			retVal = new String("PPHYTN");
		}
		else if (testParam.equalsIgnoreCase("POC") || testParam.equalsIgnoreCase("patriculate_organic_carbon")
		    || testParam.equalsIgnoreCase("POCA")) {
			retVal = new String("POC");
		}
		else if (testParam.equalsIgnoreCase("PON") || testParam.equalsIgnoreCase("patriculate_organic_nitrogen")) {
			retVal = new String("PON");
		}
		else if (testParam.equalsIgnoreCase("BACT")) {
			retVal = new String("BACT");
		}
		else if (testParam.equalsIgnoreCase("DOC") || testParam.equalsIgnoreCase("dissolved_organic_carbon")
		    || testParam.equalsIgnoreCase("DOCA")) {
			retVal = new String("DOC");
		}
		else if (testParam.equalsIgnoreCase("COMON") || testParam.equalsIgnoreCase("carbon_monoxide")) {
			retVal = new String("CO");
		}
		else if (testParam.equalsIgnoreCase("CH3CCL3")) {
			retVal = new String("CHCL");
		}
		else if (testParam.equalsIgnoreCase("CALCITE_SATURATION")) {
			retVal = new String("CALC_SAT");
		}
		else if (testParam.equalsIgnoreCase("ARAGONITE_SATURATION")) {
			retVal = new String("ARAG_SAT");
		}
		else if (testParam.equalsIgnoreCase("CTDRAW") || testParam.equalsIgnoreCase("ctd_raw")) {
			retVal = new String("CTDRAW");
		}
		else if (testParam.equalsIgnoreCase("AZOTE")) {
			retVal = new String("AZOTE");
		}
		else if (testParam.equalsIgnoreCase("F113ER")) {
			retVal = new String("Freon 113 Error");
		}
		else if (testParam.equalsIgnoreCase("F12ER")) {
			retVal = new String("Freon 12 Error");
		}
		else if (testParam.equalsIgnoreCase("F11ER")) {
			retVal = new String("Freon 11 Error");
		}
		else if (testParam.equalsIgnoreCase("N2O") || testParam.equalsIgnoreCase("nitrous_oxide")) {
			retVal = new String("N2O");
		}
		else if (testParam.equalsIgnoreCase("PHAEO")) {
			retVal = new String("PHAEO");
		}
		else if (testParam.equalsIgnoreCase("NH4") || testParam.equalsIgnoreCase("amonium")) {
			retVal = new String("NH4");
		}
		else if (testParam.equalsIgnoreCase("UREA")) {
			retVal = new String("UR");
		}
		if (DEBUG) {
			System.out.println("paramNameToJOAName translated " + testParam + " to " + retVal);
		}
		return retVal;
	}

	public static String JOANameToLongName(String inParam) {
		Exception ex = new Exception();
		ex.printStackTrace();
		// always translate pressure
		if (inParam.equalsIgnoreCase("PRES")) { return new String("Pressure"); }

		if (inParam.equalsIgnoreCase("BTLN")) {
			return new String("Bottle Number");
		}
		else if (inParam.startsWith("TEMP")) {
			return new String("Temperature");
		}
		else if (inParam.equalsIgnoreCase("SAMP")) {
			return new String("Sample Number");
		}
		else if (inParam.equalsIgnoreCase("CTDS")) {
			return new String("CDT Salinity");
		}
		else if (inParam.equalsIgnoreCase("CTDO")) {
			return new String("CTD Oxygen");
		}
		else if (inParam.equalsIgnoreCase("THETA")) {
			return new String("Potential Temperature");
		}
		else if (inParam.startsWith("SPPT") || inParam.startsWith("BSAL") || inParam.startsWith("SAL")
		    || inParam.equalsIgnoreCase("S") || inParam.equalsIgnoreCase("SA")) {
			return new String("Salinity");
		}
		else if (inParam.startsWith("O2")) {
			return new String("Oxygen");
		}
		else if (inParam.equalsIgnoreCase("SIO3") || inParam.equalsIgnoreCase("SIO4")) {
			return new String("Silicate");
		}
		else if (inParam.equalsIgnoreCase("NO2+NO3")) {
			return new String("Nitrate + Nitrite");
		}
		else if (inParam.startsWith("NO3")) {
			return new String("Nitrate");
		}
		else if (inParam.startsWith("NO2")) {
			return new String("Nitrite");
		}
		else if (inParam.startsWith("PO4")) {
			return new String("Phosphate");
		}
		else if (inParam.startsWith("F11")) {
			return new String("Freon 11");
		}
		else if (inParam.startsWith("F12")) {
			return new String("Freon 12");
		}
		else if (inParam.equalsIgnoreCase("F113")) {
			return new String("Freon 113");
		}
		else if (inParam.equalsIgnoreCase("TRIT")) {
			return new String("Tritium");
		}
		else if (inParam.equalsIgnoreCase("TRER")) {
			return new String("Tritium Error");
		}
		else if (inParam.equalsIgnoreCase("HELI")) {
			return new String("Helium");
		}
		else if (inParam.equalsIgnoreCase("HEER")) {
			return new String("Helium Error");
		}
		else if (inParam.equalsIgnoreCase("DELH3")) {
			return new String("Delta Helium 3");
		}
		else if (inParam.equalsIgnoreCase("DELH")) {
			return new String("Delta Helium");
		}
		else if (inParam.equalsIgnoreCase("DH3E")) {
			return new String("Delta Helium Error");
		}
		else if (inParam.startsWith("C14")) {
			return new String("Carbon 14");
		}
		else if (inParam.equalsIgnoreCase("CCL4")) {
			return new String("Carbon Tetrachloride");
		}
		else if (inParam.equalsIgnoreCase("CCL4ERR")) {
			return new String("Carbon Tetrachloride Error");
		}
		else if (inParam.equalsIgnoreCase("C14E")) {
			return new String("Carbon 14 Error");
		}
		else if (inParam.equalsIgnoreCase("TCO2")) {
			return new String("Total CO2");
		}
		else if (inParam.equalsIgnoreCase("ALKI")) {
			return new String("Total Alkalinity");
		}
		else if (inParam.equalsIgnoreCase("PCO2TMP")) {
			return new String("partial_pressure_of_co2");
		}
		else if (inParam.equalsIgnoreCase("PCO2")) {
			return new String("partial_pressure_of_co2");
		}
		else if (inParam.startsWith("PH")) {
			return new String("PH");
		}
		else if (inParam.startsWith("NH4")) {
			return new String("Ammonium");
		}
		else if (inParam.equalsIgnoreCase("BARI")) {
			return new String("Barium");
		}
		else if (inParam.startsWith("C13")) {
			return new String("Carbon 13");
		}
		else if (inParam.equalsIgnoreCase("C13E")) {
			return new String("C13 Error");
		}
		else if (inParam.equalsIgnoreCase("KR85")) {
			return new String("Krypton 85");
		}
		else if (inParam.equalsIgnoreCase("KRER")) {
			return new String("Krypton 85 Error");
		}
		else if (inParam.equalsIgnoreCase("MCHFRM")) {
			return new String("MCHFRM");
		}
		else if (inParam.equalsIgnoreCase("ARGO")) {
			return new String("Argon");
		}
		else if (inParam.equalsIgnoreCase("ARGE")) {
			return new String("Argon Error");
		}
		else if (inParam.equalsIgnoreCase("AR39")) {
			return new String("Argon 39");
		}
		else if (inParam.equalsIgnoreCase("ARER")) {
			return new String("Argon 39 Error");
		}
		else if (inParam.equalsIgnoreCase("NEON")) {
			return new String("Neon");
		}
		else if (inParam.equalsIgnoreCase("NEONEER")) {
			return new String("Neon Error");
		}
		else if (inParam.equalsIgnoreCase("R228")) {
			return new String("Radium 228");
		}
		else if (inParam.equalsIgnoreCase("R8ER")) {
			return new String("Radium 228 Error");
		}
		else if (inParam.equalsIgnoreCase("R226")) {
			return new String("Radium 226");
		}
		else if (inParam.equalsIgnoreCase("R6ER")) {
			return new String("Radium 226 Error");
		}
		else if (inParam.equalsIgnoreCase("O18/O16")) {
			return new String("O18/O16");
		}
		else if (inParam.equalsIgnoreCase("REVPRS")) {
			return new String("Reversing Thermometer Pressure");
		}
		else if (inParam.equalsIgnoreCase("TREV")) {
			return new String("Reversing Thermometer Temperature");
		}
		else if (inParam.equalsIgnoreCase("SR90")) {
			return new String("Strontium 90");
		}
		else if (inParam.equalsIgnoreCase("C137")) {
			return new String("Cesium 137");
		}
		else if (inParam.equalsIgnoreCase("IDAT")) {
			return new String("Iodate");
		}
		else if (inParam.equalsIgnoreCase("IDID")) {
			return new String("Iodide");
		}
		else if (inParam.startsWith("CH4")) {
			return new String("Methane");
		}
		else if (inParam.startsWith("DON")) {
			return new String("Dissovled Organic Nitrogen");
		}
		else if (inParam.startsWith("N20")) {
			return new String("N20 ");
		}
		else if (inParam.equalsIgnoreCase("CHLA")) {
			return new String("Chlorophyl A");
		}
		else if (inParam.equalsIgnoreCase("PPHYTN")) {
			return new String("Phaeophytin");
		}
		else if (inParam.startsWith("POC")) {
			return new String("Particulate Organic Carbon");
		}
		else if (inParam.startsWith("PON")) {
			return new String("Particulate Organic Nitrogen");
		}
		else if (inParam.equalsIgnoreCase("BACT")) {
			return new String("Abundance of Bacteria");
		}
		else if (inParam.startsWith("DOC")) {
			return new String("Dissolved Organic Carbon");
		}
		else if (inParam.equalsIgnoreCase("CO")) {
			return new String("Carbon Monoxide");
		}
		else if (inParam.equalsIgnoreCase("CH3CCL3")) {
			return new String("CHCL");
		}
		else if (inParam.equalsIgnoreCase("CALC_SAT")) {
			return new String("Calcite Saturation");
		}
		else if (inParam.equalsIgnoreCase("ARAG_SAT")) {
			return new String("Aragonite Saturation");
		}
		else if (inParam.equalsIgnoreCase("CTDRAW")) {
			return new String("CTD RAW");
		}
		else if (inParam.equalsIgnoreCase("C113E")) {
			return new String("Freon 113 Error");
		}
		else if (inParam.equalsIgnoreCase("FCO2")) {
			return new String("Fugacity CO2");
		}
		else if (inParam.equalsIgnoreCase("FCO2TMP")) {
			return new String("Fugacity CO2 Temperature");
		}
		else if (inParam.equalsIgnoreCase("MCHFRM")) {
			return new String("MCHFRM");
		}
		else if (inParam.equalsIgnoreCase("N2O")) {
			return new String("Nitrous Oxide");
		}
		else if (inParam.equalsIgnoreCase("OXYNIT")) {
			return new String("OXYNIT");
		}
		else if (inParam.equalsIgnoreCase("PHAEO")) {
			return new String("PHAEO");
		}
		else if (inParam.equalsIgnoreCase("PHTEMP")) {
			return new String("ph Temperature");
		}
		else if (inParam.equalsIgnoreCase("TCO2TMP")) {
			return new String("Total CO2 Temperature");
		}
		else if (inParam.equalsIgnoreCase("NH4")) {
			return new String("Amonium");
		}
		else if (inParam.equalsIgnoreCase("UR")) {
			return new String("Urea");
		}
		else if (inParam.equalsIgnoreCase("BEDFORT")) { return new String("BEDFORT"); }
		return inParam;
	}

	public static String paramNameToJOAUnits(boolean isWoce, String inParam) {
		if (inParam.equalsIgnoreCase("PRES")) {
			return new String("db");
		}
		else if (inParam.equalsIgnoreCase("TEMP")) {
			return new String("deg C");
		}
		else if (inParam.equalsIgnoreCase("SALT")) {
			return new String("psu");
		}
		else if (inParam.equalsIgnoreCase("CTDO") && isWoce) {
			return new String("mol/kg");
		}
		else if (inParam.equalsIgnoreCase("O2") && isWoce) {
			return new String("mol/kg");
		}
		else if (inParam.equalsIgnoreCase("O2")) {
			return new String("ml/l");
		}
		else if (inParam.equalsIgnoreCase("THTA")) {
			return new String("deg C");
		}
		else if (inParam.equalsIgnoreCase("SIO3")) {
			return new String("um/l");
		}
		else if (inParam.equalsIgnoreCase("NO3")) {
			return new String("um/l");
		}
		else if (inParam.equalsIgnoreCase("NO2")) {
			return new String("um/l");
		}
		else if (inParam.equalsIgnoreCase("PO4")) {
			return new String("um/l");
		}
		else if (inParam.equalsIgnoreCase("SIG0")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("SIG1")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("SIG2")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("SIG3")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("SIG4")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("GAMMA")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("AOU")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("O2%")) {
			return new String("none");
		}
		else if (inParam.equalsIgnoreCase("NO")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("C*")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("PO")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("SPCY(J&M)")) {
			return new String("none");
		}
		else if (inParam.equalsIgnoreCase("SPCY(F)")) {
			return new String("none");
		}
		else if (inParam.equalsIgnoreCase("SVAN")) {
			return new String("m^3/kg");
		}
		else if (inParam.equalsIgnoreCase("SVEL")) {
			return new String("m/s");
		}
		else if (inParam.equalsIgnoreCase("GPOT")) {
			return new String("m^2/s^2");
		}
		else if (inParam.equalsIgnoreCase("ACTT")) {
			return new String("sec");
		}
		else if (inParam.equalsIgnoreCase("PE")) {
			return new String("10^6 J/m^2");
		}
		else if (inParam.equalsIgnoreCase("HEAT")) {
			return new String("10^9 J/m^2");
		}
		else if (inParam.equalsIgnoreCase("HTST")) {
			return new String("10^6 J/kg");
		}
		else if (inParam.startsWith("BV")) {
			return new String("Hz");
		}
		else if (inParam.startsWith("SB")) {
			return new String("Hz");
		}
		else if (inParam.startsWith("VT")) {
			return new String("Hz");
		}
		else if (inParam.equalsIgnoreCase("ALPH")) {
			return new String("1/degC*10^2");
		}
		else if (inParam.equalsIgnoreCase("ADRV")) {
			return new String("1/db*10^3");
		}
		else if (inParam.equalsIgnoreCase("BETA")) {
			return new String("none");
		}
		else if (inParam.equalsIgnoreCase("BDRV")) {
			return new String("1/db*10^3");
		}
		else if (inParam.equalsIgnoreCase("GAMMA")) {
			return new String("kg/m^3");
		}
		else if (inParam.equalsIgnoreCase("NH4")) {
			return new String("um/l");
		}
		else if (inParam.equalsIgnoreCase("UR")) { return new String("um/l"); }
		return new String("na");
	}

	public static void drawStyledString(String str, int startH, int startV, FontMetrics fm, Graphics2D g) {
		if (str.indexOf('^') >= 0) {
			// draw char by char--loop on chars
			boolean doSuper = false;
			int h = startH;
			int superHeight = 4;
			int startRun = 0;
			int endRun = 0;
			boolean runStarted = false;
			for (int c = 0; c < str.length(); c++) {
				char currChar = str.charAt(c);
				if (currChar == '^') {
					// skip but set flag to superscript next char
					doSuper = true;
				}
				else {
					// draw char runs
					if (doSuper) {
						// first draw any runs
						g.drawString(str.substring(startRun, endRun), h, startV);
						runStarted = false;

						// draw the superscript
						h += fm.stringWidth(str.substring(startRun, endRun));
						char cc = str.charAt(c);
						g.drawString(str.substring(c, c + 1), h, startV - superHeight);
						h += fm.charWidth(cc);
						doSuper = false;
					}
					else {
						if (!runStarted) {
							startRun = c;
						}
						runStarted = true;
						endRun = c + 1;
					}
				}

				if (runStarted) {
					// finish up String
					g.drawString(str.substring(startRun, endRun), h, startV);
				}
			}
		}
		else {
			// draw whole string
			g.drawString(str, startH, startV);
		}
	}

	public static void drawStyledString(String str, double startH, double startV, Graphics2D g, double angle,
	    String family, int size, int style, Color color) {
		// look for a caret in the input string--delete it and record it's position
		int cPos = 0;
		Vector<Integer> expoPos = new Vector<Integer>();
		while ((cPos = str.indexOf('^')) >= 0) {
			expoPos.add(cPos);
			// if (caretPos >= 0) {
			String ss1 = str.substring(0, cPos);
			String ss2 = str.substring(cPos + 1, str.length());
			str = ss1 + ss2;
		}

		// create an attributed string
		AttributedString drawnString = new AttributedString(str);

		// add all the COMMON attributes
		drawnString.addAttribute(TextAttribute.FAMILY, family);
		drawnString.addAttribute(TextAttribute.SIZE, new Float(size));
		if (style == Font.BOLD || style == (Font.BOLD | Font.ITALIC)) {
			drawnString.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		}
		if (style == Font.ITALIC || style == (Font.BOLD | Font.ITALIC)) {
			drawnString.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}
		drawnString.addAttribute(TextAttribute.FOREGROUND, color);

		// Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style, size);
		// FontRenderContext frc = new FontRenderContext(null, true, true);
		// LineMetrics metrics = font.getLineMetrics(str, frc);
		// GlyphVector glphV = font.createGlyphVector(frc, str);
		// g.drawGlyphVector(glphV, 0.0f, 0.0f);
		// System.out.println("adding subscript #1");
		// ADD THE ATTRIBUTE FOR THE SS
		for (Integer caretPos : expoPos) {
			/*
			 * Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style,
			 * size); TextLayout aLayout = new TextLayout(str.substring(caretPos,
			 * caretPos+1), font, g.getFontRenderContext()); Shape aShape =
			 * aLayout.getOutline(null); g.setColor(Color.black); g.fill(aShape);
			 * ShapeGraphicAttribute aReplacement = new ShapeGraphicAttribute(aShape,
			 * GraphicAttribute.TOP_ALIGNMENT, true);
			 * drawnString.addAttribute(TextAttribute.CHAR_REPLACEMENT, aReplacement,
			 * caretPos, caretPos+1);
			 */
			drawnString.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, caretPos, caretPos + 1);
		}

		angle = -(float)angle;

		// save the rendering hints
		RenderingHints oldRenderingHints = null;

		oldRenderingHints = g.getRenderingHints();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// save the current transform
		AffineTransform oldTransform = new AffineTransform(g.getTransform());
		// System.out.println("x translation before = " +
		// g.getTransform().getTranslateX());
		// System.out.println("y translation before = " +
		// g.getTransform().getTranslateY());

		// translate to the drawing point
		g.translate(startH, startV);
		// System.out.println("x translation during = " +
		// g.getTransform().getTranslateX());
		// System.out.println("y translation during = " +
		// g.getTransform().getTranslateY());

		// rotate the coordinate system
		g.rotate(Math.PI * angle / 180.0);

		// draw the string in rotated coordinate space
		g.drawString(drawnString.getIterator(), 0, 0);

		// restore old transform and rendering hints
		g.setTransform(oldTransform);
		g.setRenderingHints(oldRenderingHints);
		// System.out.println("x translation after = " +
		// g.getTransform().getTranslateX());
		// System.out.println("y translation after = " +
		// g.getTransform().getTranslateY());
	}

	public static void drawStyledString(String str, double startH, double startV, double hoffset, double voffset,
	    Graphics2D g, double angle, String family, int size, int style, Color color) {
		// look for a caret in the input string--delete it and record it's position
		int caretPos = str.indexOf('^');
		if (caretPos >= 0) {
			String ss1 = str.substring(0, caretPos);
			String ss2 = str.substring(caretPos + 1, str.length());
			str = ss1 + ss2;
		}

		// create an attributed string
		AttributedString drawnString = new AttributedString(str);

		// add all the COMMON attributes
		drawnString.addAttribute(TextAttribute.FAMILY, family);
		drawnString.addAttribute(TextAttribute.SIZE, new Float(size));
		if (style == Font.BOLD || style == (Font.BOLD | Font.ITALIC)) {
			drawnString.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		}
		if (style == Font.ITALIC || style == (Font.BOLD | Font.ITALIC)) {
			drawnString.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}
		drawnString.addAttribute(TextAttribute.FOREGROUND, color);

		// Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style, size);
		// FontRenderContext frc = new FontRenderContext(null, true, true);
		// LineMetrics metrics = font.getLineMetrics(str, frc);
		// GlyphVector glphV = font.createGlyphVector(frc, str);
		// g.drawGlyphVector(glphV, 0.0f, 0.0f);

		// ADD THE ATTRIBUTE FOR THE SS
		if (caretPos >= 0) {
			/*
			 * Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style,
			 * size); TextLayout aLayout = new TextLayout(str.substring(caretPos,
			 * caretPos+1), font, g.getFontRenderContext()); Shape aShape =
			 * aLayout.getOutline(null); g.setColor(Color.black); g.fill(aShape);
			 * ShapeGraphicAttribute aReplacement = new ShapeGraphicAttribute(aShape,
			 * GraphicAttribute.TOP_ALIGNMENT, true);
			 * drawnString.addAttribute(TextAttribute.CHAR_REPLACEMENT, aReplacement,
			 * caretPos, caretPos+1);
			 */
			drawnString.addAttribute(TextAttribute.SUPERSCRIPT, new Integer(10), caretPos, caretPos + 1);
			drawnString.addAttribute(TextAttribute.SIZE, new Float(0.80 * size), caretPos, caretPos + 1);
		}

		angle = -(float)angle;

		// save the rendering hints
		RenderingHints oldRenderingHints = null;

		oldRenderingHints = g.getRenderingHints();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// save the current transform
		AffineTransform oldTransform = new AffineTransform(g.getTransform());
		// System.out.println("x translation before = " +
		// g.getTransform().getTranslateX());
		// System.out.println("y translation before = " +
		// g.getTransform().getTranslateY());

		// translate to the drawing point
		g.translate(startH, startV);
		// System.out.println("x translation during = " +
		// g.getTransform().getTranslateX());
		// System.out.println("y translation during = " +
		// g.getTransform().getTranslateY());

		// rotate the coordinate system
		g.rotate(Math.PI * angle / 180.0);
		g.translate(hoffset, voffset);

		// draw the string in rotated coordinate space
		g.drawString(drawnString.getIterator(), 0, 0);

		// restore old transform and rendering hints
		g.setTransform(oldTransform);
		g.setRenderingHints(oldRenderingHints);
		// System.out.println("x translation after = " +
		// g.getTransform().getTranslateX());
		// System.out.println("y translation after = " +
		// g.getTransform().getTranslateY());
	}

	public static void drawStyledString(String str, double startH, double startV, Graphics2D g, double angle,
	    Hashtable<TextAttribute, Serializable> map) {
		int caretPos = str.indexOf('^');
		if (caretPos >= 0) {
			String ss1 = str.substring(0, caretPos);
			String ss2 = str.substring(caretPos + 1, str.length());
			str = ss1 + ss2;
		}

		// create an attributed string
		AttributedString drawnString = new AttributedString(str);

		// Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style, size);
		// FontRenderContext frc = new FontRenderContext(null, true, true);
		// LineMetrics metrics = font.getLineMetrics(str, frc);
		// GlyphVector glphV = font.createGlyphVector(frc, str);
		// g.drawGlyphVector(glphV, 0.0f, 0.0f);

		// ADD THE ATTRIBUTE FOR THE SS
		if (caretPos >= 0) {
			/*
			 * Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style,
			 * size); TextLayout aLayout = new TextLayout(str.substring(caretPos,
			 * caretPos+1), font, g.getFontRenderContext()); Shape aShape =
			 * aLayout.getOutline(null); g.setColor(Color.black); g.fill(aShape);
			 * ShapeGraphicAttribute aReplacement = new ShapeGraphicAttribute(aShape,
			 * GraphicAttribute.TOP_ALIGNMENT, true);
			 * drawnString.addAttribute(TextAttribute.CHAR_REPLACEMENT, aReplacement,
			 * caretPos, caretPos+1);
			 */
			drawnString.addAttribute(TextAttribute.SUPERSCRIPT, new Integer(-10), caretPos, caretPos + 1);
			Float size = (Float)map.get(TextAttribute.SIZE);
			drawnString.addAttribute(TextAttribute.SIZE, new Float(0.80 * size.floatValue()), caretPos, caretPos + 1);
		}

		TextAttribute post = (TextAttribute)map.get(TextAttribute.POSTURE);
		if (post != null) {
			drawnString.addAttribute(TextAttribute.POSTURE, post);
		}

		TextAttribute weight = (TextAttribute)map.get(TextAttribute.WEIGHT);
		if (weight != null) {
			drawnString.addAttribute(TextAttribute.WEIGHT, weight);
		}

		angle = -(float)angle;

		// save the rendering hints
		RenderingHints oldRenderingHints = null;

		oldRenderingHints = g.getRenderingHints();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// save the current transform
		AffineTransform oldTransform = g.getTransform();

		// translate to the drawing point
		g.translate(startH, startV);

		// rotate the coordinate system
		g.rotate(Math.PI * angle / 180.0);

		// draw the string in rotated coordinate space
		g.drawString(drawnString.getIterator(), 0, 0);

		// restore old transform and rendering hints
		g.setTransform(oldTransform);
		g.setRenderingHints(oldRenderingHints);
	}

	public static int isDefaultVar(String varLabel) {
		int i = 0;
		for (i = 0; i < JOAConstants.defaultProperties.length; i++) {
			if (varLabel.equalsIgnoreCase(JOAConstants.defaultProperties[i].mVarLabel)) { return (i); }
		}
		return (-1);
	}

	public static short stripAlphas(String inStr) {
		// strip out alpha characters from a string and return and integer
		StringBuffer sb = new StringBuffer();
		String valChars = new String("0123456789");
		for (int i = 0; i < inStr.length(); i++) {
			char c = inStr.charAt(i);
			int indx = valChars.indexOf(c);
			if (indx >= 0) {
				sb.append(inStr.charAt(i));
			}
		}
		if (sb.length() > 0) {
			return Short.valueOf(new String(sb)).shortValue();
		}
		else {
			return (short)JOAConstants.MISSINGVALUE;
		}
	}

	public static String stripExtensions(String inFilename) {
		if (inFilename.indexOf('.') < 0) { return inFilename; }

		int pos = inFilename.lastIndexOf('.');
		int pos2 = inFilename.lastIndexOf("..");
		if (pos - 1 == pos2) { return inFilename; }
		StringBuffer sb = new StringBuffer(inFilename);
		sb.setLength(pos);
		inFilename = new String(sb);
		return inFilename;
	}

	public static String getCustomMapName(MapSpecification mapSpec) {
		// generate a new window name
		String proj = null;
		;
		if (mapSpec.getProjection() == JOAConstants.MERCATORPROJECTION) {
			proj = "Mercator:";
		}
		else if (mapSpec.getProjection() == JOAConstants.MILLERPROJECTION) {
			proj = "Miller:";
		}
		else if (mapSpec.getProjection() == JOAConstants.ORTHOGRAPHICPROJECTION) {
			proj = "Orthographic:";
		}
		else if (mapSpec.getProjection() == JOAConstants.MOLLWEIDEPROJECTION) {
			proj = "Mollweide:";
		}
		else if (mapSpec.getProjection() == JOAConstants.LAMBERTEAPROJECTION) {
			proj = "Lambert Equal Area:";
		}
		else if (mapSpec.getProjection() == JOAConstants.STEREOPROJECTION) {
			proj = "Stereographic:";
		}
		else if (mapSpec.getProjection() == JOAConstants.NORTHPOLEPROJECTION) {
			proj = "North Pole:";
		}
		else if (mapSpec.getProjection() == JOAConstants.SOUTHPOLEPROJECTION) {
			proj = "South Pole:";
		}
		else if (mapSpec.getProjection() == JOAConstants.ROBINSONPROJECTION) {
			proj = "Robinson:";
		}
		else if (mapSpec.getProjection() == JOAConstants.ECKERT4PROJECTION) {
			proj = "Eckert IV:";
		}

		return new String(proj + JOAFormulas.formatDouble(String.valueOf(mapSpec.getLonLft()), 2, false) + "-"
		    + JOAFormulas.formatDouble(String.valueOf(mapSpec.getLonRt()), 2, false) + ","
		    + JOAFormulas.formatDouble(String.valueOf(mapSpec.getLatMin()), 2, false) + "-"
		    + JOAFormulas.formatDouble(String.valueOf(mapSpec.getLatMax()), 2, false));
	}

	public static int GetDisplayPrecision(double inc) {
		int numPlaces = 2;
		if (inc < 0.000001) {
			numPlaces = 7;
		}
		if (inc < 0.00001) {
			numPlaces = 6;
		}
		if (inc < 0.0001) {
			numPlaces = 5;
		}
		else if (inc < 0.001) {
			numPlaces = 4;
		}
		else if (inc < 0.01) {
			numPlaces = 3;
		}
		else if (inc >= 0.01 && inc < 0.1) {
			numPlaces = 2;
		}
		else if (inc >= 0.1 && inc <= 1.0) {
			numPlaces = 1;
		}
		else if (inc > 1.0) {
			double diff = inc - (int)inc;
			if (diff == 0.0) {
				numPlaces = 0;
			}
			else if (diff < 0.0001) {
				numPlaces = 5;
			}
			else if (diff < 0.001) {
				numPlaces = 4;
			}
			else if (diff < 0.01) {
				numPlaces = 3;
			}
			else if (diff >= 0.01 && diff < 0.1) {
				numPlaces = 2;
			}
			else if (diff >= 0.1 && diff < 1.0) {
				numPlaces = 1;
			}
		}
		return numPlaces;
	}

	public static boolean signChanged(double num1, double num2) {
		if (num1 < 0 && num2 > 0) {
			return true;
		}
		else if (num2 < 0 && num1 > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public static double[] test(int numObs, double[] result) {
		// double[] result = new double[numObs];
		for (int i = 0; i < numObs; i++) {
			result[i] = -99;
		}
		return result;
	}

	public static void plotThickLine(Graphics2D g, int h, int v, int h2, int v2, int lineWidth) {
		mLineWidth = new BasicStroke(lineWidth);
		g.setStroke(mLineWidth);
		g.drawLine(h, v, h2, v2);
		g.setStroke(mLineWidth1);
	}

	public static void plotSymbol(Graphics2D g, int symbol, double h, double v, int width) {
		double x = h;
		double y = v;

		if (symbol == JOAConstants.SYMBOL_SQUARE) {
			Rectangle2D.Double rect = new Rectangle2D.Double(h - width / 2.0, v - width / 2.0, width, width);
			g.draw(rect);
		}
		else if (symbol == JOAConstants.SYMBOL_SQUAREFILLED) {
			Rectangle2D.Double rect = new Rectangle2D.Double(h - width / 2.0, v - width / 2.0, width, width);
			g.fill(rect);
		}
		else if (symbol == JOAConstants.SYMBOL_CIRCLE) {
			g.drawOval((int)h - width / 2, (int)v - width / 2, width, width);
		}
		else if (symbol == JOAConstants.SYMBOL_CIRCLEFILLED) {
			g.fillOval((int)h - width / 2, (int)v - width / 2, width, width);
			// g.drawOval(h-width/2, v-width/2, width, width);
		}
		else if (symbol == JOAConstants.SYMBOL_DIAMOND) {
			int[] xpoints = { (int)x, (int)x + width / 2, (int)x, (int)x - width / 2 };
			int[] ypoints = { (int)y - width / 2, (int)y, (int)y + width / 2, (int)y };
			g.drawPolygon(xpoints, ypoints, 4);
		}
		else if (symbol == JOAConstants.SYMBOL_DIAMONDFILLED) {
			int[] xpoints = { (int)x, (int)x + width / 2, (int)x, (int)x - width / 2 };
			int[] ypoints = { (int)y - width / 2, (int)y, (int)y + width / 2, (int)y };
			g.drawPolygon(xpoints, ypoints, 4);
			g.fillPolygon(xpoints, ypoints, 4);
		}
		else if (symbol == JOAConstants.SYMBOL_TRIANGLE) {
			int[] xpoints = { (int)x - width / 2, (int)x + width / 2, (int)x };
			int[] ypoints = { (int)y + width / 2, (int)y + width / 2, (int)y - width / 2 };
			g.drawPolygon(xpoints, ypoints, 3);
		}
		else if (symbol == JOAConstants.SYMBOL_TRIANGLEFILLED) {
			int[] xpoints = { (int)x - width / 2, (int)x + width / 2, (int)x };
			int[] ypoints = { (int)y + width / 2, (int)y + width / 2, (int)y - width / 2 };
			g.drawPolygon(xpoints, ypoints, 3);
			g.fillPolygon(xpoints, ypoints, 3);
		}
		else if (symbol == JOAConstants.SYMBOL_CROSS1) {
			g.drawLine((int)x - width / 2, (int)y, (int)x + width / 2, (int)y);
			g.drawLine((int)x, (int)y - width / 2, (int)x, (int)y + width / 2);
		}
		else if (symbol == JOAConstants.SYMBOL_CROSS2) {
			g.drawLine((int)x - width / 2, (int)y - width / 2, (int)x + width / 2, (int)y + width / 2);
			g.drawLine((int)x - width / 2, (int)y + width / 2, (int)x + width / 2, (int)y - width / 2);
		}
		else if (symbol == JOAConstants.SYMBOL_HORIZONTAL_BAR_SYMBOL) {
			g.drawLine((int)x - 4, (int)y, (int)x + 4, (int)y);
		}
		else if (symbol == JOAConstants.SYMBOL_DOWN_ARROW_SYMBOL) {
			int[] xpoints = { (int)x - 5, (int)x + 5, (int)x, (int)x - 5 };
			int[] ypoints = { (int)y - 5, (int)y - 5, (int)y, (int)y - 5 };
			g.fillPolygon(xpoints, ypoints, 4);
		}
		else if (symbol == JOAConstants.SYMBOL_UP_ARROW_SYMBOL) {
			int[] xpoints3 = { (int)x - 5, (int)x + 5, (int)x, (int)x - 5 };
			int[] ypoints3 = { (int)y + 5, (int)y + 5, (int)y, (int)y + 5 };
			g.fillPolygon(xpoints3, ypoints3, 4);
		}
		else if (symbol == JOAConstants.SYMBOL_RIGHT_ARROW_SYMBOL) {
			int[] xpoints2 = { (int)x - 5, (int)x, (int)x - 5, (int)x - 5 };
			int[] ypoints2 = { (int)y - 5, (int)y, (int)y + 5, (int)y - 5 };
			g.fillPolygon(xpoints2, ypoints2, 4);
		}
		else if (symbol == JOAConstants.SYMBOL_LEFT_ARROW_SYMBOL) {
			int[] xpoints4 = { (int)x, (int)x + 5, (int)x + 5, (int)x };
			int[] ypoints4 = { (int)y, (int)y - 5, (int)y + 5, (int)y };
			g.fillPolygon(xpoints4, ypoints4, 4);
		}
		else if (symbol == JOAConstants.SYMBOL_VERTICAL_BAR_SYMBOL) {
			g.drawLine((int)x, (int)y - 4, (int)x, (int)y + 4);
		}
	}

	public static double sign(double x) {
		if (x < 0.0) {
			return (-1.0);
		}
		else {
			return (1.0);
		}
	}

	public static String formatParamName(String inName) {
		if (inName.length() > 4) {
			return inName.substring(0, 4);
		}
		else if (inName.length() < 4) {
			String temp = new String(inName + "   ");
			return temp.substring(0, 4);
		}
		return inName;
	}

	public static String trimPreceedingWhiteSpace(String inStr) {
		if (inStr == null || inStr.length() == 0) { return inStr; }
		StringBuffer outStr = new StringBuffer(inStr);
		while (true) {
			int len = outStr.length();
			if (outStr.charAt(0) == ' ') {
				for (int i = 0; i < len - 1; i++) {
					outStr.setCharAt(i, outStr.charAt(i + 1));
				}
				outStr.setLength(len - 1);
			}
			else {
				break;
			}
		}
		return new String(outStr);
	}

	public static String trimPreceedingWhiteSpace(String inStr, char charToStrip) {
		if (inStr == null || inStr.length() == 0) { return inStr; }
		StringBuffer outStr = new StringBuffer(inStr);
		while (true) {
			int len = outStr.length();
			if (outStr.charAt(0) == charToStrip) {
				for (int i = 0; i < len - 1; i++) {
					outStr.setCharAt(i, outStr.charAt(i + 1));
				}
				outStr.setLength(len - 1);
			}
			else {
				break;
			}
		}
		return new String(outStr);
	}

	public static String returnZeroPaddedString(int inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(Integer.toString(inNum));

		while (outStr.length() <= finalLength) {
			outStr.insert(0, "0");
		}
		return new String(outStr);
	}

	public static String returnSpacePaddedString(int inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(Integer.toString(inNum));

		while (outStr.length() <= finalLength) {
			outStr.insert(0, " ");
		}
		return new String(outStr);
	}

	public static String returnZeroPaddedString(double inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(Double.toString(inNum));

		while (outStr.length() <= finalLength) {
			outStr.insert(0, "0");
		}
		return new String(outStr);
	}

	public static String returnSpacePaddedString(double inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(Double.toString(inNum));

		while (outStr.length() <= finalLength) {
			outStr.insert(0, " ");
		}
		return new String(outStr);
	}

	public static String returnZeroPaddedString(String inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(inNum);

		while (outStr.length() <= finalLength) {
			outStr.insert(0, "0");
		}
		return new String(outStr);
	}

	public static String returnSpacePaddedString(String inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(inNum);

		while (outStr.length() <= finalLength) {
			outStr.insert(0, " ");
		}
		return new String(outStr);
	}

	public static String returnSpaceEndPaddedString(String inNum, int finalLength) {
		StringBuffer outStr = new StringBuffer(inNum);

		while (outStr.length() <= finalLength) {
			outStr.append(" ");
		}
		return new String(outStr);
	}

	public static String returnMiddleTruncatedString(String inStr, int maxLength) {
		int numToKeep = (maxLength - 2) / 2;
		int len = inStr.length();
		if (len < maxLength) { return inStr; }
		return new String(inStr.substring(0, numToKeep - 1) + ".." + inStr.substring(len - numToKeep - 1, len));
	}

	public static String getShortSectionName(String sn, int maxLen) {
		// get part of string with number in it
		int numPos = 0;
		int endPos = 0;
		for (int i = 0; i < sn.length(); i++) {
			try {
				numPos = i;

				// look for second number
				try {
					endPos = numPos + 1;

					// look for third number
					try {
						endPos = endPos + 1;

						break;
					}
					catch (Exception ex) {
					}
					break;
				}
				catch (Exception ex) {
				}
				break;
			}
			catch (Exception ex) {
			}
		}

		// get the position
		String pos = "";
		if (numPos > 0 && endPos > 0 && endPos > numPos) {
			pos = sn.substring(numPos, endPos);
		}

		// get an Ocean
		String ocean = "";
		String ucsn = sn.toUpperCase();
		if (ucsn.indexOf("ATL") >= 0) {
			ocean = "A";
		}
		else if (ucsn.indexOf("PAC") >= 0) {
			ocean = "P";
		}
		else if (ucsn.indexOf("IND") >= 0) {
			ocean = "I";
		}

		// get a hemisphere
		String hemis = "";
		if (numPos > 0 && endPos > 0 && endPos > numPos) {
			String rest = ucsn.substring(endPos - 1, ucsn.length());
			if (rest.indexOf("W") >= 0) {
				hemis = "W";
			}
			else if (rest.indexOf("E") >= 0) {
				hemis = "E";
			}
			else if (rest.indexOf("N") >= 0) {
				hemis = "N";
			}
			else if (rest.indexOf("S") >= 0) {
				hemis = "S";
			}
		}

		// put together new string if all the pieces are there
		if (pos.length() > 0 && hemis.length() > 0 && ocean.length() > 0) {
			return new String(ocean + pos + hemis);
		}
		else {
			return JOAFormulas.returnMiddleTruncatedString(sn, maxLen);
		}
	}

	public static Color getSectionColor(int secNum) {
		Color outColor = null;
		int testSecNum = secNum;
		if (testSecNum > 14) {
			testSecNum = testSecNum % 14;
		}

		if (testSecNum == 0) {
			outColor = Color.blue.brighter();
		}
		else if (testSecNum == 1) {
			outColor = Color.red;
		}
		else if (testSecNum == 2) {
			outColor = Color.green;
		}
		else if (testSecNum == 3) {
			outColor = Color.yellow;
		}
		else if (testSecNum == 4) {
			outColor = new Color(153, 153, 255);
		}
		else if (testSecNum == 5) {
			outColor = Color.cyan;
		}
		else if (testSecNum == 6) {
			outColor = Color.magenta;
		}
		else if (testSecNum == 7) {
			outColor = Color.orange;
		}
		else if (testSecNum == 8) {
			outColor = Color.red.darker();
		}
		else if (testSecNum == 9) {
			outColor = Color.green.darker();
		}
		else if (testSecNum == 10) {
			outColor = Color.yellow.darker();
		}
		else if (testSecNum == 11) {
			outColor = Color.blue.darker();
		}
		else if (testSecNum == 12) {
			outColor = Color.cyan.darker();
		}
		else if (testSecNum == 13) {
			outColor = Color.magenta.darker();
		}
		else if (testSecNum == 14) {
			outColor = Color.orange.darker();
		}
		else {
			outColor = Color.gray;
		}

		return outColor;
	}

	public static double ANINT(double n1) {
		long l = Math.round(n1);
		return (double)l;
	}

	public static File getSupportFile(String name) throws FileNotFoundException {
		String dir = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		File nf = new File(dir, name);
		if (nf == null) { throw new FileNotFoundException(); }
		return nf;
	}

	public static File getCoastFile(String name) throws FileNotFoundException {
		String dir = getCoastlinePath();
		File nf = new File(dir, name);
		if (nf == null) { throw new FileNotFoundException(); }
		return nf;
	}

	public static File getCustomCoastFile(String name) throws FileNotFoundException {
		String dir = getCustomCoastlinePath();
		File nf = new File(dir, name);
		if (nf == null) { throw new FileNotFoundException(); }
		return nf;
	}

	public static File getBathyFile(String name) throws FileNotFoundException {
		String dir = getBathymetryPath();
		File nf = new File(dir, name);
		if (nf == null) { throw new FileNotFoundException(); }
		return nf;
	}

	public static File getCustomBathyFile(String name) throws FileNotFoundException {
		String dir = getCustomCoastlinePath();
		File nf = new File(dir, name);
		if (nf == null) { throw new FileNotFoundException(); }
		return nf;
	}

	public static File getNewLogFile() throws FileNotFoundException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
		String name = "JOA_Log_" + sdf.format(new Date()) + ".txt";
		String dir = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		File nf = new File(dir, name);
		if (nf == null) { throw new FileNotFoundException(); }
		return nf;
	}

	public static String getSupportPath() {
		String dir = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		return dir;
	}

	public static String getCoastlinePath() {
		String dir = System.getProperty("user.dir") + File.separator + "coastlines" + File.separator;
		return dir;
	}

	public static String getBathymetryPath() {
		String dir = System.getProperty("user.dir") + File.separator + "bathymetry" + File.separator;
		return dir;
	}

	public static String getCustomCoastlinePath() {
		String dir = System.getProperty("user.dir") + File.separator + "coastlines" + File.separator + "Custom_Coastlines"
		    + File.separator;
		return dir;
	}

	public static String getCustomBathymetryPath() {
		String dir = System.getProperty("user.dir") + File.separator + "bathymetry" + File.separator + "Custom_Bathymetry"
		    + File.separator;
		return dir;
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
		return Math.tan(A * JOAConstants.F);
	}

	public static double mpart(double lat) {
		double x;

		x = Math.sin(lat * JOAConstants.F);
		return 7915.704468 * log10(TAND(45 + lat / 2)) - 23.268932 * x - 0.0525 * (x * x * x) - 0.000213
		    * (x * x * x * x * x);
	}

	public static double ATAN2D(double A, double B) {
		return Math.atan2(A, B) / JOAConstants.F;
	}

	public static double COSD(double A) {
		return Math.cos(A * JOAConstants.F);
	}

	public static double SECD(double A) {
		return 1 / Math.cos(A * JOAConstants.F);
	}

	public static double SIND(double A) {
		return Math.sin(A * JOAConstants.F);
	}

	public static double CSCD(double A) {
		return 1 / SIND(A);
	}

	public static double HAVD(double A) {
		return (1 - COSD(A)) / 2;
	}

	public static double AHAVD(double A) {
		return Math.acos(-(A * 2 - 1)) / JOAConstants.F;
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

	public static double Mercator(double lat1, double lon1, double lat2, double lon2) {
		double l, m, dlo, dist, course;
		boolean right;

		/* ROUTINE TO DETERMINE JOAConstants.DISTANCE (NAUT. MILES) AND COURSE GIVEN */
		/* START AND END POINTS. USES MERCATOR SAILING. */

		/* REF: BOWDITCH, NATHANIEL, AMERICAN PRACTICAL NAVIGATOR. */
		/* 1966, P 227-228. VK 555 A48 1966 */

		l = Math.abs(lat2 - lat1);
		dlo = Math.abs(lon2 - lon1);

		if (dlo > 180) {
			dlo = 360.0 - dlo;
			if (lon2 < lon1) {
				right = true;
			}
			else {
				right = false;
			}
		}
		else {
			if (lon2 > lon1) {
				right = true;
			}
			else {
				right = false;
			}
		}

		if (l < 0.001) {
			/* Use parallel sailing */
			dist = Math.abs(dlo * 60 * COSD(lat1));
			if (right) {
				course = 90;
			}
			else {
				course = 270;
			}
			return dist;
		}

		l = l * 60;
		dlo = dlo * 60;
		m = mpart(lat2) - mpart(lat1);
		course = ATAN2D(dlo, m);
		dist = Math.abs(l * SECD(course));
		if (!right) {
			course = 360 - course;
		}
		return dist;
	}

	public static double LonTo360(double inLon) {
		if (inLon < 0) {
			return inLon + 360.0;
		}
		else {
			return inLon;
		}
	}

	public static double L360ToLon(double inLon) {
		if (inLon > 180) {
			return inLon - 360.0;
		}
		else {
			return inLon;
		}
	}

	public static String formatLat(double inLat) {
		if (latDM == null) {
			createLatFormatter();
		}

		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEC_MINUTES_GEO_DISPLAY) {
			if (inLat == 0) {
				return new String("EQ");
			}
			else if (inLat < 0) {
				return new String(JOAFormulas.formatDouble(String.valueOf(-inLat), 3, true) + "S");
			}
			else {
				return new String(JOAFormulas.formatDouble(String.valueOf(inLat), 3, true) + "N");
			}
		}
		else {
			return new String(latDM.format((float)inLat));
		}
	}

	public static String formatLat(double inLat, int prec) {
		if (latDM == null) {
			createLatFormatter();
		}

		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEC_MINUTES_GEO_DISPLAY) {
			if (inLat == 0) {
				return new String("EQ");
			}
			else if (inLat < 0) {
				return new String(JOAFormulas.formatDouble(String.valueOf(-inLat), prec, false) + "S");
			}
			else {
				return new String(JOAFormulas.formatDouble(String.valueOf(inLat), prec, false) + "N");
			}
		}
		else {
			return new String(latDM.format((float)inLat));
		}
	}

	public static String formatLon(double inLon) {
		if (inLon > 180)
			inLon -= 360;
		if (lonDM == null) {
			createLonFormatter();
		}

		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEC_MINUTES_GEO_DISPLAY) {
			if (inLon < 0) {
				return new String(JOAFormulas.formatDouble(String.valueOf(-inLon), 3, true) + "W");
			}
			else {
				return new String(JOAFormulas.formatDouble(String.valueOf(inLon), 3, true) + "E");
			}
		}
		else {
			return new String(lonDM.format((float)inLon));
		}
	}

	public static String formatLon(double inLon, int prec) {
		if (inLon > 180)
			inLon -= 360;
		if (lonDM == null) {
			createLonFormatter();
		}

		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEC_MINUTES_GEO_DISPLAY) {
			if (lonDM == null) {
				createLonFormatter();
			}
			if (inLon < 0) {
				return new String(JOAFormulas.formatDouble(String.valueOf(-inLon), prec, false) + "W");
			}
			else {
				return new String(JOAFormulas.formatDouble(String.valueOf(inLon), prec, false) + "E");
			}
		}
		else {
			return new String(lonDM.format((float)inLon));
		}
	}

	public static String formatLat(double inLat, boolean pad) {
		if (latDM == null) {
			createLatFormatter();
		}

		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEC_MINUTES_GEO_DISPLAY) {
			if (inLat < 0) {
				return new String(JOAFormulas.formatDouble(String.valueOf(-inLat), 3, pad) + "S");
			}
			else {
				return new String(JOAFormulas.formatDouble(String.valueOf(inLat), 3, pad) + "N");
			}
		}
		else {
			return new String(latDM.format((float)inLat));
		}
	}

	public static String formatLon(double inLon, boolean pad) {
		if (lonDM == null) {
			createLonFormatter();
		}

		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEC_MINUTES_GEO_DISPLAY) {
			if (inLon < 0) {
				return new String(JOAFormulas.formatDouble(String.valueOf(-inLon), 3, pad) + "W");
			}
			else {
				return new String(JOAFormulas.formatDouble(String.valueOf(inLon), 3, pad) + "E");
			}
		}
		else {
			return new String(lonDM.format((float)inLon));
		}
	}

	public static String formatDouble(double inVal, int inNumPlaces, boolean pad) {
		if (inVal >= 1.0e35) { return "    ----"; }
		int numPl = inNumPlaces;
		String valStr = new Double(inVal).toString();
		int expPlace = valStr.toUpperCase().indexOf('E');
		String frmt = null;

		if (expPlace > 0) {
			// number in scientific notation--get the exponent
			String exp = valStr.substring(expPlace, valStr.length());
			exp = exp.toLowerCase();
			exp = exp.substring(1, exp.length());
			numPl = Math.abs(Integer.valueOf(exp).intValue());
		}
		if (numPl == 1) {
			frmt = new String("0.0");
		}
		else if (numPl == 2) {
			frmt = new String("0.00");
		}
		else if (numPl == 3) {
			frmt = new String("0.000");
		}
		else if (numPl == 4) {
			frmt = new String("0.0000");
		}
		else if (numPl == 5) {
			frmt = new String("0.00000");
		}
		else if (numPl == 6) {
			frmt = new String("0.000000");
		}
		else if (numPl == 7) {
			frmt = new String("0.0000000");
		}
		else if (numPl == 8) {
			frmt = new String("0.00000000");
		}
		else if (numPl == 9) {
			frmt = new String("0.000000000");
		}

		StringBuffer out = new StringBuffer();
		try {
			DecimalFormat decFormatter = new DecimalFormat(frmt);
			decFormatter.format(inVal, out, new FieldPosition(0));
		}
		catch (Exception ex) {
			try {
				frmt = new String("###E##");
				DecimalFormat decFormatter = new DecimalFormat(frmt);
				decFormatter.format(inVal, out, new FieldPosition(0));
				System.out.println("threw out  = " + out);
			}
			catch (Exception exx) {
				return new Double(inVal).toString();
			}
		}
		if (pad) {
			while (out.length() < 8) {
				out.insert(0, ' ');
			}
		}
		String str = new String(out);
		return str;
	}

	public static String formatDouble(double inVal, int inNumPlaces, boolean pad, int padLength) {
		if (inVal >= 1.0e35) { return "    ----"; }
		int numPl = inNumPlaces;
		String valStr = new Double(inVal).toString();
		int expPlace = valStr.toUpperCase().indexOf('E');
		String frmt = null;

		if (expPlace > 0) {
			// number in scientific notation--get the exponent
			String exp = valStr.substring(expPlace, valStr.length());
			exp = exp.toLowerCase();
			exp = exp.substring(1, exp.length());
			numPl = Math.abs(Integer.valueOf(exp).intValue());
		}
		if (numPl == 1) {
			frmt = new String("0.0");
		}
		else if (numPl == 2) {
			frmt = new String("0.00");
		}
		else if (numPl == 3) {
			frmt = new String("0.000");
		}
		else if (numPl == 4) {
			frmt = new String("0.0000");
		}
		else if (numPl == 5) {
			frmt = new String("0.00000");
		}
		else if (numPl == 6) {
			frmt = new String("0.000000");
		}
		else if (numPl == 7) {
			frmt = new String("0.0000000");
		}
		else if (numPl == 8) {
			frmt = new String("0.00000000");
		}
		else if (numPl == 9) {
			frmt = new String("0.000000000");
		}

		StringBuffer out = new StringBuffer();
		try {
			DecimalFormat decFormatter = new DecimalFormat(frmt);
			decFormatter.format(inVal, out, new FieldPosition(0));
		}
		catch (Exception ex) {
			try {
				frmt = new String("###E##");
				DecimalFormat decFormatter = new DecimalFormat(frmt);
				decFormatter.format(inVal, out, new FieldPosition(0));
				System.out.println("threw out  = " + out);
			}
			catch (Exception exx) {
				return new Double(inVal).toString();
			}
		}
		if (pad) {
			while (out.length() < padLength) {
				out.insert(0, ' ');
			}
		}
		String str = new String(out);
		return str;
	}

	public static String formatDouble(String inValStr, int numPlaces, boolean pad) {
		int numPl = numPlaces;
		String valStr;
		// look for the decimal point
		int expPlace = inValStr.toUpperCase().indexOf('E');
		String exp = "";
		if (expPlace > 0) {
			// number in scientific notation--get the exponent
			String mantissa = inValStr.substring(0, expPlace);
			exp = inValStr.substring(expPlace, inValStr.length());
			exp = exp.toLowerCase();
			exp = exp.substring(1, exp.length());
			int sign = exp.indexOf("-") >= 0 ? -1 : 1;
			numPl = Math.abs(Integer.valueOf(exp).intValue());
			double manVal = Double.valueOf(mantissa).doubleValue();
			manVal *= Math.pow(10.0, (double)(sign * numPl));
			valStr = new Double(manVal).toString();
		}
		else {
			valStr = inValStr;
		}

		int decPlace = valStr.lastIndexOf('.');
		if (decPlace < 0) {
			valStr += ".";
			decPlace = valStr.lastIndexOf('.');
		}
		int len = valStr.length();

		StringBuffer sb = new StringBuffer(valStr);

		if (numPl > 0) {
			if (len - decPlace > numPl + 1) {
				// truncate string
				sb.setLength(decPlace + 1 + numPl);
			}
			else if (len - decPlace < numPl + 1) {
				// pad with 0's
				while (sb.length() < decPlace + 1 + numPl) {
					sb.append('0');
				}
			}
		}
		else {
			sb.setLength(decPlace);
		}

		if (pad) {
			while (sb.length() < 8) {
				sb.insert(0, ' ');
			}
		}
		return new String(sb);
	}

	public static String formatDate(Station sh, boolean includeTime) {
		if (sh.mYear == JOAConstants.MISSINGVALUE) { return JOAConstants.MISSING_VALUE_STRING; }

		if (mGeoDate == null) {
			createTimeFormatter();
		}

		String timeStr = new String("");
		String dateStr = new String("");

		try {
			// create a geodate
			double mins = 0;
			if (sh.mMinute != JOAConstants.MISSINGVALUE) {
				mins = sh.mMinute;
			}

			// make the time axis units
			int imin = (int)mins;
			double fmin = mins - imin;
			int isecs = (int)(fmin * 60.0);
			double fsec = (fmin * 60.0) - isecs;
			int imsec = (int)(fsec * 1000.0);
			mGeoDate.set(sh.mMonth, sh.mDay, sh.mYear, sh.mHour, imin, isecs, imsec);
			dateStr = mGeoDate.toString(JOAConstants.DEFAULT_DATE_FORMAT);
		}
		catch (Exception ex) {
			String yearStr = new String(String.valueOf(sh.mYear));
			String monthStr = new String(String.valueOf(sh.mMonth));
			String dayStr = new String(String.valueOf(sh.mDay));
			dateStr = monthStr + "/" + dayStr + "/" + yearStr;
		}

		if (sh.mHour != JOAConstants.MISSINGVALUE) {
			timeStr = new String(" " + String.valueOf(sh.mHour));
			if (sh.mMinute != JOAConstants.MISSINGVALUE) {
				timeStr = timeStr + ":" + formatDouble(String.valueOf(sh.mMinute), 3, false);
			}
			else {
				timeStr = timeStr + ":00";
			}
		}

		String errStr = null;
		if (sh.mDateError) {
			errStr = new String(" ?");
		}
		else {
			errStr = new String("");
		}

		if (includeTime) {
			return new String(dateStr + " " + timeStr + errStr);
		}
		else {
			return new String(dateStr + errStr);
		}
	}

	public static double getValueOfColorVariable(FileViewer fv, Section sec, Bottle bot) {
		int vPos = sec.getVarPos(fv.mAllProperties[fv.mCurrentColorVariable].mVarLabel, false);
		if (vPos < 0) { return (double)JOAConstants.MISSINGVALUE; }

		return bot.mDValues[vPos];
	}

	public static Bottle findBottleByPres(FileViewer fv, Station sh) {
		int p = fv.getPRESPropertyPos();
		double minOffset = 1000.0;
		Bottle foundBottle = null;
		try {
			for (int b = 0; b < sh.mNumBottles; b++) {
				Bottle bh = (Bottle)sh.mBottles.elementAt(b);
				double val = bh.mDValues[p];
				double off = Math.abs(val - JOAConstants.currTestPres);
				if (off < minOffset) {
					foundBottle = bh;
					minOffset = off;
				}
			}
			if (foundBottle != null) {
				return foundBottle;
			}
			else {
				return (Bottle)sh.mBottles.elementAt(sh.mNumBottles - 1);
			}
		}
		catch (Exception ex) {
			return null;
		}
	}

	public static int getPrecision(double inMin, double inMax) {
		double diff = Math.abs(inMin - inMax);

		if (diff >= 0.0001 && diff < 0.001) {
			return 6;
		}
		else if (diff >= 0.001 && diff < 0.01) {
			return 5;
		}
		else if (diff >= 0.01 && diff < 0.1) {
			return 4;
		}
		else if (diff >= 0.1 && diff < 1.0) {
			return 3;
		}
		else if (diff >= 1.0 && diff < 10.0) {
			return 3;
		}
		else if (diff >= 10.0 && diff < 100.0) {
			return 2;
		}
		else if (diff >= 100.0 && diff < 1000.0) {
			return 1;
		}
		else {
			return 1;
		}
	}

	public static Triplet GetPrettyRange(double inMin, double inMax) {
		double diff, factor = 1.0, outMax, outMin, outInc, mult = 1.0;

		diff = Math.abs(inMin - inMax);

		if (diff == 0.0d) { return new Triplet(-0.1, 0.1, 0.05); }

		if (diff == 0.0) {
			factor = 1.0;
		}
		else if (diff >= 0.000001 && diff < 0.00001) {
			factor = 0.000001;
		}
		else if (diff >= 0.00001 && diff < 0.0001) {
			factor = 0.00001;
		}
		else if (diff >= 0.0001 && diff < 0.001) {
			factor = 0.0001;
		}
		else if (diff >= 0.001 && diff < 0.01) {
			factor = 0.001;
		}
		else if (diff >= 0.01 && diff < 0.1) {
			factor = 0.01;
		}
		else if (diff >= 0.1 && diff < 1.0) {
			factor = 0.1;
		}
		else if (diff >= 1.0 && diff < 10.0) {
			factor = 1.0;
		}
		else if (diff >= 10.0 && diff < 100.0) {
			factor = 10.0;
		}
		else if (diff >= 100.0 && diff < 400.0) {
			factor = 10.0;
		}
		else if (diff >= 400.0 && diff < 1000.0) {
			factor = 100.0;
		}
		else if (diff >= 1000.0 && diff < 10000.0) {
			factor = 1000.0;
		}
		else if (diff >= 10000.0 && diff < 100000.0) {
			factor = 5000.0;
		}
		else if (diff >= 100000.0 && diff < 1000000.0) {
			factor = 50000.0;
		}

		if (inMin > 1000 && inMax >= 1000 && factor == 10.0) {
			mult = 5.0;
		}

		factor *= mult;

		outMin = Math.floor(inMin / factor) * factor;
		outMax = Math.ceil(inMax / factor) * factor;
		int numIncs = (int)((outMax - outMin) / factor);
		outInc = (outMax - outMin) / numIncs;
		return new Triplet(outMin, outMax, outInc);
	}

	/*
	 * This calculates local potential temperature at reference_pressure using the
	 * Bryden 1973 polynomial for adiabatic lapse rate and Runge-Kutta 4th order
	 * integration algorithm.
	 * 
	 * References: Bryden, H., 1973, Deep-Sea Res., 20, 401-408. Fofonoff, N.,
	 * 1977, Deep-Sea Res., 24, 489-491.
	 * 
	 * Units: Pressure P decibars Temperature T degrees Celcius Salinity S psu
	 * (IPSS-78) Reference press. Pref decibars Potential tmp. Theta degrees
	 * Celcuis
	 * 
	 * Check value: Theta = 36.89073 C for S = 40 (psu), T = 40 deg. C, P = 10000
	 * decibars, Pref = 0 decibars.
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

	/*
	 * This computes the adiabatic temperature gradient in degrees C per decibar.
	 * 
	 * Reference: Bryden, H., 1973, Deep Sea Res., 20, 401-408.
	 * 
	 * Units:
	 * 
	 * Pressure: P decibars Temperature: T degrees Celcius Salinity: S psu
	 * (IPSS-78) Adiabatic: ATG deg. C/decibar
	 * 
	 * 
	 * Check value: ATG = 3.255796E-4 C/dbar for S = 40 (psu), T = 40 deg. C, P =
	 * 10000 decibars.
	 */

	public static double atg(double salinity, double temperature, double pressure) {
		double S, T, p, latg;

		S = salinity - 35.;
		T = temperature;
		p = pressure;

		latg = (((-2.1687e-16 * T + 1.8676e-14) * T - 4.6206e-13) * p + ((2.7759e-12 * T - 1.1351e-10) * S
		    + ((-5.4481e-14 * T + 8.733e-12) * T - 6.7795e-10) * T + 1.8741e-08))
		    * p + (-4.2393e-08 * T + 1.8932e-06) * S + ((6.6228e-10 * T - 6.836e-08) * T + 8.5258e-06) * T + 3.5803e-05;

		return (latg);
	}

	/*
	 * This calculates sigma (density - 1000 kg/m^3) at salinity, temperature, and
	 * pressure. This will be potential density if temperature is potential
	 * temperature. The routine uses the high pressure equation of state from
	 * Millero et al. 1980 and the one- atmosphere equation of state from Millero
	 * and Poisson 1981 as reported in Gill 1982. The notation follows Millero et
	 * al. 1980 and Millero and Poisson 1981.
	 * 
	 * Note: the routine takes p in decibars and converts to bars for the
	 * calculations.
	 * 
	 * References: Millero, JOAConstants.F.J., et al., 1980, Deep-Sea Res., 27A,
	 * 255-264. Millero, JOAConstants.F.J. and Alain Poisson, 1981, Deep-Sea Res.,
	 * 28A, 625-629. Gill, A.E., 1982, Atmosphere-Ocean Dynamics, Academic Press,
	 * Inc., 662 pp.
	 * 
	 * Input Units: S: psu T: deg. C p: decibars.
	 * 
	 * Output Units: sigma: kg/m^3
	 * 
	 * Check values: sigma = -.033249 for S = 0, T = 5, p = 0 sigma = 27.675465
	 * for S = 35, T = 5, p = 0 sigma = 62.538172 for S = 35, T = 25, p = 10000.
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
		rhoW = ((((6.536332e-09 * T - 1.120083e-06) * T + 1.001685e-04) * T - 9.095290e-03) * T + 6.793952e-02) * T
		    + 999.842594;

		A = (((5.3875e-09 * T - 8.2467e-07) * T + 7.6438e-05) * T - 4.0899e-03) * T + 8.24493e-01;

		B = (-1.6546e-06 * T + 1.0227e-04) * T - 5.72466e-03;

		C = 4.8314e-04;

		// rhoZero is the one-atmosphere density of seawater.
		rhoZero = (C * S + B * rootS + A) * S + rhoW;

		if (pressure == 0.0) {
			sigma = rhoZero - 1000.;
			return (sigma);
		}

		a = ((-6.1670e-05 * T + 1.09987e-02) * T - 6.03459e-01) * T + 54.6746;

		b = (-5.3009e-04 * T + 1.6483e-02) * T + 7.944e-02;

		c = (-1.6078e-06 * T - 1.0981e-05) * T + 2.2838e-03;

		d = 1.91075e-04;

		e = (9.1697e-10 * T + 2.0816e-08) * T - 9.9348e-07;

		Aw = ((-5.77905e-07 * T + 1.16092e-04) * T + 1.43713e-03) * T + 3.239908;

		Bw = (5.2787e-08 * T - 6.12293e-06) * T + 8.50935e-05;

		// Kw is the secant bulk modulus of pure water at temperature T.
		Kw = (((-5.155288e-05 * T + 1.360477e-02) * T - 2.327105) * T + 148.4206) * T + 19652.21;

		// Kzero is the secant bulk modulus of seawater at one atmosphere.
		Kzero = (b * rootS + a) * S + Kw;

		// K is the secant bulk modulus of seawater at (S, T, p).
		K = ((e * p + d * rootS + c) * S + Bw * p + Aw) * p + Kzero;

		sigma = rhoZero / (1.0 - (p / K));

		sigma -= 1000.;

		return (sigma);

	}

	/*
	 * Listed below is the AOU routine we have been using. It is based on the work
	 * of A-T Chen , Solubility Data Series, vol 7, Oxygen and Ozone. This version
	 * is in RPL from RS1. The units for the input variables are: Temperature in
	 * degrees Kelvin, Salinity in ppt and dissolved oxygen in ml/l. If oxygen is
	 * in umol use the factor 22393 ml/mol to convert. The ouput units on AOU are
	 * umol/l.
	 */

	public static double computeAOU(double inTemp, double inSalt, double inO2, boolean O2Vol2Mass) {
		double Osat, AOU;

		// Sal = 34.309;
		// DO2 = 6.5;

		Osat = o2Saturation(inTemp, inSalt);
		if (O2Vol2Mass) {
			AOU = convertVol2Mass(-(inO2 - Osat));
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
		Sat = -1268.9782 + 36063.19 / Tkel + 220.1832 * Math.log(Tkel) - 0.351299 * Tkel + inSalt
		    * (0.006229 - 3.5912 / Tkel) + 0.00000344 * (inSalt * inSalt);

		Osat = Math.exp(Sat);
		return (Osat);
	}

	public static double convertVol2Mass(double inVol) {
		/*
		 * inVol is in ml/l, convert to umol/l (ml/l) / (22393 (ml/mol) * 1000000)
		 */
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

	public static double computeCstar(double inTCO2, double inTALK, double inO2, boolean O2Vol2Mass) {
		/*
		 * Gruber et la:, Antropogenic CO2 in the ocean, Global Geochemical Cycles,
		 * Vol. 10, NO. 4, Pages 809-837, December, 1996 C* = C - r(c:O2) * O2 -
		 * 1/2(Alk + r(N:O2) * O2)
		 * 
		 * Test Values (all units are um/kg):
		 * 
		 * O2 carbn alkali cstar 275.3 2107.7 2323.3 1148.5
		 */
		double cStar, tO2 = inO2;

		if (O2Vol2Mass) {
			tO2 = 43.549 * inO2;
		}

		cStar = inTCO2 - (rCtoO * tO2) - 0.5 * (inTALK + rNtoO * tO2);
		return cStar;
	}

	public static double heatCapacity(double s, double t) {

		/*
		 * a polynomial in salinity and temperature to compute the heat capacity of
		 * sea water in Joules per kilogram per degree Kelvin at atmospheric
		 * pressure. Equation taken from Millero et al. JGR v.78 #20 4499-4507
		 * (1973). Check that heatCapacity( 40, 40 ) == 3981
		 */
		double t2, t3, t4, c, c32, cpOfT, cpOfST;
		c = s / 1.80655;
		t2 = t * t;
		t3 = t * t2;
		t4 = t * t3;
		c32 = Math.sqrt(c * c * c);

		cpOfT = 4217.4 - 3.720283 * t + 0.1412855 * t2 - 2.654387e-3 * t3 + 2.093236e-5 * t4;

		cpOfST = cpOfT + c * (-13.81 + 0.1938 * t - 0.0025 * t2) + c32 * (0.43 - 0.0099 * t + 0.00013 * t2);

		return cpOfST;
	}

	public static double specificVolumeAnomoly(double salinity, double theta, double pressure) {
		double R3500 = 1028.1063, R4 = 4.8314e-04, V350P;
		double S, T, P, rootS;
		double A, B, C, D, E, B1, A1, DK, K35;
		double AW, BW, KW;
		double R2, R3, K0;
		double R1, SIG, SVA, SVAN, GAM, PK;

		/* compute sigma */
		S = salinity;
		T = theta;
		P = pressure / 10.;
		rootS = Math.sqrt(S);

		/* rhoW is the density of pure water at temperature T. */
		R1 = ((((6.536332e-09 * T - 1.120083e-06) * T + 1.001685e-04) * T - 9.095290e-03) * T + 6.793952e-02) * T
		    - 28.263737;

		R2 = (((5.3875e-09 * T - 8.2467e-07) * T + 7.6438e-05) * T - 4.0899e-03) * T + 8.24493e-01;

		R3 = (-1.6546e-06 * T + 1.0227e-04) * T - 5.72466e-03;

		/* sig is the one-atmosphere density of seawater. */
		SIG = (R4 * S + R3 * rootS + R2) * S + R1;

		/* specific volume at atmospheric pressure */
		V350P = 1.0 / R3500;
		SVA = -SIG * V350P / (R3500 + SIG);
		// *sigma = SIG + DR350;
		SVAN = (SVA * 1.0e+8);
		if (P == 0) { return SVAN; }

		/* compute a compression term for pressures > 0 */
		E = (9.1697e-10 * T + 2.0816e-08) * T - 9.9348e-07;

		BW = (5.2787e-08 * T - 6.12293e-06) * T + 3.47718e-05;

		B = BW + E * S;

		D = 1.91075e-04;

		C = (-1.6078e-06 * T - 1.0981e-05) * T + 2.2838e-03;

		AW = ((-5.77905e-07 * T + 1.16092e-04) * T + 1.43713e-03) * T - 0.1194975;

		A = (D * rootS + C) * S + AW;

		B1 = (-5.3009e-04 * T + 1.6483e-02) * T + 7.944e-02;

		A1 = ((-6.1670e-05 * T + 1.09987e-02) * T - 0.603459) * T + 54.6746;

		/* Kw is the secant bulk modulus of pure water at temperature T. */
		KW = (((-5.155288e-05 * T + 1.360477e-02) * T - 2.327105) * T + 148.4206) * T - 1930.06;

		/* Kzero is the secant bulk modulus of seawater at one atmosphere. */
		K0 = (B1 * rootS + A1) * S + KW;

		/* K35 is the secant bulk modulus of seawater at (35, 0, p). */
		DK = (B * P + A) * P + K0;
		K35 = (5.03217e-05 * P + 3.359406) * P + 21582.27;
		GAM = P / K35;
		PK = 1.0 - GAM;
		SVA = SVA * PK + (V350P + SVA) * P * DK / (K35 * (K35 + DK));

		/* scale specific volume anomoly to nmally reported units */
		SVAN = SVA * 1.0e08;
		V350P = V350P * PK;

		return SVAN;
	}

	public static double soundVelocity(double p0, double t, double s) {
		/*
		 * sound velocity:Chen and Millero (1977, JASA,62,1129-1135) c(m/s),
		 * P(dbar), t(C), s(psu) check: 1731.9954 m/s (10,000,40,40)!! convert to
		 * bars and sqrt(salinity)
		 */

		double p, sr, d, b1, b0, b, a3, a2, a1, a0, a, c3, c2, c1, c0, c, svl;

		p = p0 / 10.0;
		sr = Math.sqrt(Math.abs(s));

		d = 1.727e-3 - 7.9836e-6 * p;

		/* s**3/2 term */

		b1 = 7.3637e-05 + 1.7945e-07 * t;
		b0 = -1.922e-02 - 4.42e-05 * t;
		b = b0 + b1 * p;

		/* s**1 term */

		a3 = (-3.389e-13 * t + 6.649e-12) * t + 1.100e-10;
		a2 = ((7.988e-12 * t - 1.6002e-10) * t + 9.1041e-9) * t - 3.9064e-7;
		a1 = (((-2.0122e-10 * t + 1.0507e-8) * t - 6.4885e-8) * t - 1.2580e-5) * t + 9.4742e-5;
		a0 = (((-3.21e-8 * t + 2.006e-6) * t + 7.164e-5) * t - 1.262e-2) * t + 1.389;
		a = ((a3 * p + a2) * p + a1) * p + a0;

		/* s**0 term */

		c3 = (-2.3643e-12 * t + 3.8504e-10) * t - 9.7729e-9;
		c2 = (((1.0405e-12 * t - 2.5335e-10) * t + 2.5974e-8) * t - 1.7107e-6) * t + 3.1260e-5;
		c1 = (((-6.1185e-10 * t + 1.3621e-7) * t - 8.1788e-6) * t + 6.8982e-4) * t + 0.153563;
		c0 = ((((3.1464e-9 * t - 1.47800e-6) * t + 3.342e-4) * t - 5.80852e-2) * t + 5.03711) * t + 1402.388;
		c = ((c3 * p + c2) * p + c1) * p + c0;

		svl = c + (a + b * sr + d * s) * s;

		return svl;
	}

	// Calculates Spiciness from manuscript by Jackett and McDougall, DSR, 32A,
	// 1195-1208, 1985.

	public static double computeSpiciness(double inTemp, double inSalt) {
		double spic;
		double[] temp = new double[5];
		double[] salt = new double[5];
		double[][] b = { { 0.1609705, 0.6542397, 5.222258e-4, -2.586742e-5, 7.565157e-7 },
		    { -8.007345e-2, 5.309506e-3, -9.612388e-5, 3.211527e-6, -4.610513e-8 },
		    { 1.081912e-2, -1.561608e-4, 3.774240e-6, -1.150394e-7, 1.146084e-9 },
		    { -1.451748e-4, 3.485063e-6, -1.387056e-7, 3.737360e-9, -2.967108e-11 },
		    { 1.219904e-6, -3.591075e-8, 1.953475e-9, -5.279546e-11, 4.227375e-13 } };

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

	/*
	 * double spice(p,t,s) pressure can only be 0 double p,t,s; { static double
	 * b[6][5]; double sp,T,S; int i,j;
	 * 
	 * b[0][0] = 0; b[0][1] = 7.7442e-001; b[0][2] = -5.85e-003; b[0][3] =
	 * -9.84e-004; b[0][4] = -2.06e-004;
	 * 
	 * b[1][0] = 5.1655e-002; b[1][1] = 2.034e-003; b[1][2] = -2.742e-004; b[1][3]
	 * = -8.5e-006; b[1][4] = 1.36e-005;
	 * 
	 * b[2][0] = 6.64783e-003; b[2][1] = -2.4681e-004; b[2][2] = -1.428e-005;
	 * b[2][3] = 3.337e-005; b[2][4] = 7.894e-006;
	 * 
	 * b[3][0] = -5.4023e-005; b[3][1] = 7.326e-006; b[3][2] = 7.0036e-006;
	 * b[3][3] = -3.0412e-006; b[3][4] = -1.0853e-006;
	 * 
	 * b[4][0] = 3.949e-007; b[4][1] = -3.029e-008; b[4][2] = -3.8209e-007;
	 * b[4][3] = 1.0012e-007; b[4][4] = 4.7133e-008;
	 * 
	 * b[5][0] = -6.36e-010; b[5][1] = -1.309e-009; b[5][2] = 6.048e-009; b[5][3]
	 * = -1.1409e-009; b[5][4] = -6.676e-010;
	 * 
	 * s=(s-35.); sp=0.;
	 * 
	 * T=1.; for (i=0;i<6;i++) { S=1.; for(j=0;j<5;j++) { sp+=b[i][j]*T*S; S*=s; }
	 * T*=t; }
	 * 
	 * return(sp);
	 * 
	 * spice(p=0,T=15,S=33)=0.5445864137500002
	 */

	public static double computeSpiciness2(double inTemp, double inSalt) {
		double[][] b = { { 0.0, 7.7442e-001, -5.85e-003, -9.84e-004, -2.06e-004 },
		    { 5.1655e-002, 2.034e-003, -2.742e-004, -8.5e-006, 1.36e-005 },
		    { 6.64783e-003, -2.4681e-004, -1.428e-005, 3.337e-005, 7.894e-006 },
		    { -5.4023e-005, 7.326e-006, 7.0036e-006, -3.0412e-006, -1.0853e-006 },
		    { 3.949e-007, -3.029e-008, -3.8209e-007, 1.0012e-007, 4.7133e-008 },
		    { -6.36e-010, -1.309e-009, 6.048e-009, -1.1409e-009, -6.676e-010 } };

		double s = (inSalt - 35.0);
		double sp = 0.0;

		double T = 1.0;
		for (int i = 0; i < 6; i++) {
			double S = 1.0;
			for (int j = 0; j < 5; j++) {
				sp += b[i][j] * T * S;
				S *= s;
			}
			T *= inTemp;
		}
		return sp;
	}

	public static UVCoordinate alpha(boolean isBottle, Section sech, Station sh, double[] result) {
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		double saltAvg, tempAvg, presAvg;
		double rho1, rho2, rhom, drhodT;
		double alpha;
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		int nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		double max = -9999999; // Initialize min and max with missingVal.
		double min = 9999999;

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to
			// bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i + 1); // Get the variables
			// of bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				continue;
			}

			// compute the averages
			saltAvg = 0.5 * (salt1 + salt2);
			tempAvg = 0.5 * (temp1 + temp2);
			presAvg = 0.5 * (pres1 + pres2);

			// compute the densities
			rho1 = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, temp1, presAvg, presAvg), presAvg);
			rho2 = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, temp2, presAvg, presAvg), presAvg);
			rhom = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

			// compute the derivative
			if (temp2 != temp1 && rho2 != rho1) {
				drhodT = (rho2 - rho1) / (temp2 - temp1);
				alpha = -(1 / rhom) * drhodT;

				// scale up by factor of 100 for better plotting
				alpha *= 100.0;
			}
			else {
				alpha = JOAConstants.MISSINGVALUE; // missing value
			}

			// store result
			result[i] = alpha;
		}

		max = result[0]; // Scan for largest and smallest values.
		min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}
		return new UVCoordinate(min, max);
	}

	public static UVCoordinate beta(boolean isBottle, Section sech, Station sh, double[] result) {
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		double saltAvg, tempAvg, presAvg;
		double rho1, rho2, rhom, drhodS;
		double beta;
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		int nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to
			// bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i + 1); // Get the variables
			// of bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				continue;
			}

			// compute the averages
			saltAvg = 0.5 * (salt1 + salt2);
			tempAvg = 0.5 * (temp1 + temp2);
			presAvg = 0.5 * (pres1 + pres2);

			rho1 = JOAFormulas.sigma(salt1, JOAFormulas.theta(salt1, tempAvg, presAvg, presAvg), presAvg);
			rho2 = JOAFormulas.sigma(salt2, JOAFormulas.theta(salt2, tempAvg, presAvg, presAvg), presAvg);
			rhom = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

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

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}
		return new UVCoordinate(min, max);

	}

	public static UVCoordinate alphadTdZ(boolean isBottle, Section sech, Station sh, double[] result) {
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		double saltAvg, tempAvg, presAvg;
		double rho1, rho2, rhom, drhodT;
		double alpha;
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		int nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to
			// bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i + 1); // Get the variables
			// of bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					salt2 = bot_2.mDValues[subsPos];
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				continue;
			}

			// compute the averages
			saltAvg = 0.5 * (salt1 + salt2);
			tempAvg = 0.5 * (temp1 + temp2);
			presAvg = 0.5 * (pres1 + pres2);

			// compute the densities
			rho1 = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, temp1, presAvg, presAvg), presAvg);
			rho2 = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, temp2, presAvg, presAvg), presAvg);
			rhom = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

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
				alpha = JOAConstants.MISSINGVALUE; // missing value
			}

			// store result
			result[i] = alpha;
		}

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}
		return new UVCoordinate(min, max);
	}

	public static UVCoordinate betadSdZ(boolean isBottle, Section sech, Station sh, double[] result) {
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		double saltAvg, tempAvg, presAvg;
		double rho1, rho2, rhom, drhodS;
		double beta;
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		int nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		for (int i = 0; i < nBottles - 1; i++) { // loop from bottle(0) to
			// bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i + 1); // Get the variables
			// of bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				continue;
			}

			// compute the averages
			saltAvg = 0.5 * (salt1 + salt2);
			tempAvg = 0.5 * (temp1 + temp2);
			presAvg = 0.5 * (pres1 + pres2);

			rho1 = JOAFormulas.sigma(salt1, JOAFormulas.theta(salt1, tempAvg, presAvg, presAvg), presAvg);
			rho2 = JOAFormulas.sigma(salt2, JOAFormulas.theta(salt2, tempAvg, presAvg, presAvg), presAvg);
			rhom = JOAFormulas.sigma(saltAvg, JOAFormulas.theta(saltAvg, tempAvg, presAvg, presAvg), presAvg);

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

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}
		return new UVCoordinate(min, max);
	}

	public static UVCoordinate BouyancyFrequency(boolean isBottle, Section sech, Station sh, double[] result,
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
		double lat = sh.mLat;

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		int nBottles = sh.mNumBottles;
		phi = lat * JOAConstants.F; // JOAConstants.F is degrees to radians
		coriolis = 2 * (1.0 / 24.0) * Math.sin(phi); // *cycles* per *hour*

		// Allocate temporary memory.
		double[] rho = new double[nBottles];
		double[] rhoSmth = new double[nBottles];
		double[] Z = new double[nBottles];
		double[] DrhoDz = rho; // this memory used for *two* purposes

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data arrays to
			// missing.
			rho[i] = JOAConstants.MISSINGVALUE;
			rhoSmth[i] = JOAConstants.MISSINGVALUE;
			Z[i] = JOAConstants.MISSINGVALUE;
		}

		for (int i = 0; i < nBottles; i++) { // compute densities loop
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i);

			salt = bot_1.mDValues[sPos]; // get observations
			temp = bot_1.mDValues[tPos];
			pres = bot_1.mDValues[pPos];

			sOK = (salt != JOAConstants.MISSINGVALUE); // Test to see if data are
			// valid
			tOK = (temp != JOAConstants.MISSINGVALUE);
			pOK = (pres != JOAConstants.MISSINGVALUE);

			if (!sOK && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt = bot_1.mDValues[subsPos];
						sOK = true;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt = bot_1.mDValues[subsPos];
						sOK = true;
					}
				}
				else {
					salt = JOAConstants.MISSINGVALUE;
				}
				sOK = (salt != JOAConstants.MISSINGVALUE);
			}

			if (sOK && tOK && pOK) {
				rho[i] = 1000.0 + JOAFormulas.sigma(salt, temp, 0); // 0 used to be pres
			}

			Z[i] = JOAFormulas.presToZ(pres, lat);

		} // End compute densities loop

		twiceZ = 2.0 * zLength;
		frst = 0;
		last = 0;
		for (int i = 0; i < nBottles; i++) { // smooth the densities loop
			for (int j = i; j >= 0; j--) { // Find index of first observation
				frst = j; // within smoothing length.
				if (Z[j] == JOAConstants.MISSINGVALUE) {
					continue;
				}
				if (twiceZ < Math.abs(Z[i] - Z[frst])) {
					break;
				}
			}

			for (int j = i; j < nBottles; j++) { // Find index of last observation
				last = j; // within smoothing length.
				if (Z[j] == JOAConstants.MISSINGVALUE) {
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

		for (int i = 0; i < nBottles - 1; i++) { // differentiate densities loop
			DrhoDz[i] = JOAConstants.MISSINGVALUE;
			z1 = Z[i];
			z2 = Z[i + 1];
			rho1 = rhoSmth[i];
			rho2 = rhoSmth[i + 1];
			if (z1 == JOAConstants.MISSINGVALUE || z2 == JOAConstants.MISSINGVALUE) {
				continue;
			}
			if (rho1 == JOAConstants.MISSINGVALUE || rho2 == JOAConstants.MISSINGVALUE) {
				continue; // *rho == *DrhoDz, so
			}
			DrhoDz[i] = (rho2 - rho1) / (z2 - z1); // this overwrites rho[].

		} // End differentiate densities loop

		Z[0] = DrhoDz[0]; // Overwrite Z, no longer needed.
		DrhoDz[nBottles - 1] = DrhoDz[nBottles - 2];
		for (int i = 0; i < nBottles - 1; i++) { // interpolate derivatives loop
			Z[i + 1] = JOAConstants.MISSINGVALUE;
			z1 = DrhoDz[i];
			z2 = DrhoDz[i + 1];
			if (z1 == JOAConstants.MISSINGVALUE || z2 == JOAConstants.MISSINGVALUE) {
				continue;
			}
			Z[i + 1] = .5 * (z1 + z2);

		} // End interpolate derivatives loop

		DrhoDz = Z; // Just switching pointers around.

		for (int i = 0; i < nBottles; i++) { // compute N, N^2, or f*N^2/g loop.
			if (DrhoDz[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			if (rhoSmth[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			N = (JOAConstants.HOUR / JOAConstants.TWOPI)
			    * Math.sqrt(Math.abs((JOAConstants.GRAVITY * (DrhoDz[i] / rhoSmth[i]))));
			if (n2Flag) {
				result[i] = N * N;
			}
			else if (potVort) {
				result[i] = Math.abs(coriolis * N * N / JOAConstants.GRAVITY);
			}
			else {
				result[i] = N;
			}

		} // End compute N, N^2, or f*N^2/g loop

		for (int i = 0; i < nBottles; i++) { // Scan for a valid entry
			if (result[i] != JOAConstants.MISSINGVALUE) {
				break;
			}
		}

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}
		return new UVCoordinate(min, max);
	} // End BouyancyFrequency().

	public static double gaussSmooth(double zLength, int i, int frst, int last, double[] rho, double[] Z) {
		double depth;
		double weight;
		double sumWeight;
		double zL2;
		double deltaZ;
		double result;

		depth = Z[i];
		if (depth == JOAConstants.MISSINGVALUE) { return JOAConstants.MISSINGVALUE; }
		zL2 = zLength * zLength;
		sumWeight = 0.0;
		result = 0.0;

		for (int k = frst; k <= last; k++) {
			if (Z[k] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			if (rho[k] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			deltaZ = depth - Z[k];
			deltaZ = deltaZ * deltaZ;
			weight = Math.exp(-deltaZ / zL2);
			sumWeight += weight;
			result += weight * rho[k];
		}

		result = (sumWeight > 0.0) ? result / sumWeight : JOAConstants.MISSINGVALUE;
		return result;
	}

	public static UVCoordinate acousticTravelTime(boolean isBottle, Section sech, Station sh, double[] result) {
		double sumATT; // sum of trapezoidal panels of delta(travel time)
		double avgSpeed; // average sound speed between bottles (i) & (i+1).
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		double soundVel1, soundVel2; // sound velocity at bottles (i) & (i+1).
		double z1, z2; // depth at bottles (i) & (i+1).
		double deltaZ; // depth difference between bottles (i) & (i+1).
		int nBottles; // number of bottles at this station.
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		sumATT = 0; // If first observation is at surface,
		if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface
			// water has uniform properties.
			z1 = presToZ(pres1, sh.mLat);
			soundVel1 = soundVelocity(pres1, temp1, salt1);
			soundVel2 = soundVel1;
			deltaZ = z1 - 0;
			avgSpeed = .5 * (soundVel1 + soundVel2);
			sumATT = sumATT - deltaZ / avgSpeed;
		}
		result[0] = sumATT;

		for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to
			// bottle(nBottles - 1). // loop from
			// bottle(0) to bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i - 1); // Get the variables
			// of bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				/* Routine quits when it encounters first bad value */
				break;
			}

			z1 = presToZ(pres1, sh.mLat);
			z2 = presToZ(pres2, sh.mLat);
			soundVel1 = soundVelocity(pres1, temp1, salt1);
			soundVel2 = soundVelocity(pres2, temp2, salt2);
			deltaZ = z2 - z1;
			avgSpeed = .5 * (soundVel1 + soundVel2); // Evaluate the integrand.
			sumATT = sumATT - deltaZ / avgSpeed;
			result[i] = sumATT;
		} // End integration down the list of bottles.

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}

		return new UVCoordinate(min, max);
	}

	/* End acousticTravelTime(). */

	public static UVCoordinate heatStorage(boolean isBottle, Section sech, Station sh, double[] result) {
		double sumHeat; // sum of trapezoidal panels of delta(heat storage).
		double deltaHeat; // delta(heat storage).
		double avgPoTemp; // mean potential temperature between bottles (i) & (i+1).
		double avgPotDensity; // average potential density between bottles (i) &
		// (i+1).
		double avgPotCp; // mean potential heat capacity between bottles (i) &
		// (i+1).
		double poTemp1, poTemp2; // potential temperature at bottles (i) & (i+1).
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		double z1, z2; // depth at bottles (i) & (i+1).
		double deltaZ; // depth difference between bottles (i) & (i+1).
		int nBottles; // number of bottles at this station.
		// variable blocks of each bottle.
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		sumHeat = 0; // If first observation is at surface,
		if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface
			// water has uniform properties.
			z2 = presToZ(pres1, sh.mLat);
			z1 = 0;
			poTemp1 = theta(salt1, temp1, pres1, 0);
			avgPoTemp = poTemp1;
			avgPotDensity = 1000 + sigma(salt1, poTemp1, pres1);
			avgPotCp = heatCapacity(salt1, poTemp1);
			deltaZ = z2 - z1;
			deltaHeat = deltaZ * avgPoTemp * avgPotDensity * avgPotCp; // integrand.
			sumHeat = sumHeat - JOAConstants.GIGAJOULES * deltaHeat;
		}
		result[0] = sumHeat;

		for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to
			// bottle(nBottles - 1). // loop from
			// bottle(0) to bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i - 1); // Get the variables
			// of bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				/* Routine quits when it encounters first bad value */
				break;
			}

			z1 = presToZ(pres1, sh.mLat);
			z2 = presToZ(pres2, sh.mLat);
			poTemp1 = theta(salt1, temp1, pres1, 0);
			poTemp2 = theta(salt2, temp2, pres2, 0);
			avgPoTemp = .5 * (poTemp1 + poTemp2);
			avgPotDensity = 1000 + .5 * (sigma(salt1, poTemp1, pres1) + sigma(salt2, poTemp2, pres2));
			avgPotCp = .5 * (heatCapacity(salt1, poTemp1) + heatCapacity(salt2, poTemp2));
			deltaZ = z2 - z1;
			deltaHeat = deltaZ * avgPoTemp * avgPotDensity * avgPotCp; // integrand.
			sumHeat = sumHeat - JOAConstants.GIGAJOULES * deltaHeat;
			result[i] = sumHeat;
		} // End integration down the list of bottles.

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}

		return new UVCoordinate(min, max);
	}

	public static UVCoordinate geopotentialAnomaly(boolean isBottle, Section sech, Station sh, double[] result) {
		double sumGPA; // sum of trapezoidal panels of delta(geopotential anomaly).
		double deltaGPA; // delta(geopotential anomaly).
		double deltaPres; // pressure difference between bottles (i) & (i+1).
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double SVA1, SVA2; // specific volume anomaly at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		int nBottles; // number of bottles at this station.
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		// If first observation is at surface,integration begins at zero. Otherwise,
		// assume surface water has uniform properties.
		sumGPA = 0;
		if (pres1 != 0) { //
			SVA1 = JOAConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
			SVA2 = SVA1;
			deltaPres = JOAConstants.DB2PASCALSI * (pres1 - 0);
			deltaGPA = .5 * (SVA1 + SVA2) * deltaPres;
			sumGPA = sumGPA - deltaGPA;
		}
		result[0] = sumGPA;

		for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to
			// bottle(nBottles - 1).
			// loop from bottle(0) to bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i - 1); // Get the variables
			// of bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				/* Routine quits when it encounters first bad value */
				break;
			}

			SVA1 = JOAConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
			SVA2 = JOAConstants.SVA2SI * specificVolumeAnomoly(salt2, temp2, pres2);
			deltaPres = JOAConstants.DB2PASCALSI * (pres2 - pres1);
			deltaGPA = .5 * (SVA1 + SVA2) * deltaPres; // Evaluate the integrand.
			sumGPA = sumGPA - deltaGPA;
			result[i] = sumGPA;
		} // End integration down the list of bottles.

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}

		return new UVCoordinate(min, max);
	}

	public static UVCoordinate potentialEnergyAnomaly(boolean isBottle, Section sech, Station sh, double[] result) {
		double sumPEA; // sum of trapezoidal panels of delta(potentialEnergy
		// anomaly).
		double deltaPEA; // delta(potentialEnergy anomaly).
		double deltaPres; // pressure difference between bottles (i) & (i+1).
		double avgPres; // average pressure of bottles (i) & (i+1).
		double salt1, salt2; // salinity at bottles (i) & (i+1).
		double temp1, temp2; // temperature at bottles (i) & (i+1).
		double SVA1, SVA2; // specific volume anomaly at bottles (i) & (i+1).
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		int nBottles; // number of bottles at this station.
		// variable blocks of each bottle.
		boolean s1bad, s2bad; // logical flags signaling missing values.
		boolean t1bad, t2bad; // logical flags signaling missing values.
		boolean p1bad, p2bad; // logical flags signaling missing values.

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		salt1 = bot_11.mDValues[sPos];
		s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		temp1 = bot_11.mDValues[tPos];
		t1bad = (temp1 == JOAConstants.MISSINGVALUE);
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (s1bad && isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
				int subsPos = sech.getVarPos("CTDS", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
				int subsPos = sech.getVarPos("SALT", true);
				if (subsPos >= 0) {
					salt1 = bot_11.mDValues[subsPos];
					s1bad = false;
				}
			}
			else {
				salt1 = JOAConstants.MISSINGVALUE;
			}
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
		}

		if (s1bad || t1bad || p1bad) { // Routine quits when it encounters first
			// bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		sumPEA = 0; // If first observation is at surface,
		if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface
			// water has uniform properties.
			SVA1 = JOAConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
			SVA2 = SVA1;
			pres2 = pres1;
			pres1 = 0;
			deltaPres = JOAConstants.DB2PASCALSI * (pres2 - pres1);
			avgPres = JOAConstants.DB2PASCALSI * (pres2 + pres1) * .5;
			deltaPEA = (.5 * (SVA1 + SVA2) * avgPres * deltaPres) / JOAConstants.GRAVITY;
			sumPEA = sumPEA - JOAConstants.MILLION * deltaPEA;
		}
		result[0] = sumPEA;

		for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to
			// bottle(nBottles - 1). // loop from
			// bottle(0) to bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i - 1); // Get the variables
			// of bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			salt1 = bot_1.mDValues[sPos];
			s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			salt2 = bot_2.mDValues[sPos];
			s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			temp1 = bot_1.mDValues[tPos];
			t1bad = (temp1 == JOAConstants.MISSINGVALUE);
			temp2 = bot_2.mDValues[tPos];
			t2bad = (temp2 == JOAConstants.MISSINGVALUE);
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (s1bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt1 = bot_1.mDValues[subsPos];
						s1bad = false;
					}
				}
				else {
					salt1 = JOAConstants.MISSINGVALUE;
				}
				s1bad = (salt1 == JOAConstants.MISSINGVALUE);
			}

			if (s2bad && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					int subsPos = sech.getVarPos("CTDS", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					int subsPos = sech.getVarPos("SALT", true);
					if (subsPos >= 0) {
						salt2 = bot_2.mDValues[subsPos];
						s2bad = false;
					}
				}
				else {
					salt2 = JOAConstants.MISSINGVALUE;
				}
				s2bad = (salt2 == JOAConstants.MISSINGVALUE);
			}

			if (s1bad || s2bad || t1bad || t2bad || p1bad || p2bad) {
				// Routine quits when it encounters first bad value
				break;
			}

			SVA1 = JOAConstants.SVA2SI * specificVolumeAnomoly(salt1, temp1, pres1);
			SVA2 = JOAConstants.SVA2SI * specificVolumeAnomoly(salt2, temp2, pres2);
			deltaPres = JOAConstants.DB2PASCALSI * (pres2 - pres1);
			avgPres = JOAConstants.DB2PASCALSI * (pres2 + pres1) * .5;
			deltaPEA = (.5 * (SVA1 + SVA2) * avgPres * deltaPres) / JOAConstants.GRAVITY; // Evaluate
			// integrand.
			sumPEA = sumPEA - JOAConstants.MILLION * deltaPEA;
			result[i] = sumPEA;
		} // End integration down the list of bottles.

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}

		return new UVCoordinate(min, max);
	}

	public static double integrateStation(IntegrationSpecification iSpec, Section sech, Station sh, String intParam,
	    String wrtParam) {
		int intPos = sech.getVarPos(intParam, false);
		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int wrtPos = sech.getVarPos(wrtParam, false); // index into variable block
		double sumParam; // sum of trapezoidal panels of delta(travel time)
		double avg; // average integrand between bottles (i) & (i+1).
		double paramVal1, paramVal2; // param values at bottles (i) & (i+1).
		double deltaZ; // depth difference between bottles (i) & (i+1).
		int nBottles; // number of bottles at this station.
		boolean p1bad, p2bad; // logical flags signaling missing values.
		double pres1, pres2; // pressure at bottles (i) & (i+1).

		nBottles = sh.mNumBottles;
		if (nBottles <= 0) { return JOAConstants.MISSINGVALUE; }
		// find these values
		double presAtMin = JOAConstants.MISSINGVALUE; // pressure of the min value
		// of the wrt variable
		double presAtMax = JOAConstants.MISSINGVALUE; // pressure of the min value
		// of the wrt variable
		double intValAtMin = JOAConstants.MISSINGVALUE; // value of the integrand at
		// the interpolated min
		// pressure
		double intValAtMax = JOAConstants.MISSINGVALUE; // value of the integrand at
		// the interpolated max
		// pressure
		int intIndexAtMin = 0;
		int intIndexAtMax = nBottles;
		int num = 0;
		double[] integrandArray = null;
		double[] presArray = null;
		sh.setStnValHitShallowest(sech.getNumStnVars() - 1, false);
		sh.setStnValHitDeepest(sech.getNumStnVars() - 1, false);
		boolean foundMin = false;
		boolean foundMax = false;

		// create a virtual cast with interpolation as necessary
		double[] presVals = new double[nBottles];
		double[] wrtVals = new double[nBottles];
		double[] intVals = new double[nBottles];
		for (int i = 0; i < nBottles; i++) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			wrtVals[i] = bh.mDValues[wrtPos];
			presVals[i] = bh.mDValues[pPos];
			intVals[i] = bh.mDValues[intPos];
		}

		if (iSpec.isInterpolateMissing()) {
			// interpolate missing values for the integrand and the wrt variable
			for (int i = 0; i < nBottles; i++) {
				if (intVals[i] == JOAConstants.MISSINGVALUE) {
					double midPresVal = presVals[i];
					double topIntVal = JOAConstants.MISSINGVALUE;
					double bottomIntVal = JOAConstants.MISSINGVALUE;
					double topPresVal = JOAConstants.MISSINGVALUE;
					double bottomPresVal = JOAConstants.MISSINGVALUE;
					boolean foundUpper = false;
					boolean foundLower = false;

					// look above first
					int icnt = i - 1;
					int fbl = i - iSpec.getFarBottleLimit();
					if (icnt >= 0) {
						while (icnt >= 0 && icnt >= fbl) {
							if (intVals[icnt] != JOAConstants.MISSINGVALUE) {
								topIntVal = intVals[icnt];
								topPresVal = presVals[icnt];
								foundUpper = true;
								break;
							}
							icnt--;
						}
					}

					// look below
					icnt = i + 1;
					fbl = i + iSpec.getFarBottleLimit();
					if (icnt < nBottles - 1) {
						while (icnt < nBottles - 1 && icnt <= fbl) {
							if (intVals[icnt] != JOAConstants.MISSINGVALUE) {
								bottomIntVal = intVals[icnt];
								bottomPresVal = presVals[icnt];
								foundLower = true;
								break;
							}
							icnt++;
						}
					}

					if (foundUpper && foundLower) {
						// interpolate
						double denom = Math.abs(topPresVal - bottomPresVal);
						double num2 = Math.abs(midPresVal - bottomPresVal);
						double frac = num2 / denom;
						// interpolate the integrand to this
						intVals[i] = topIntVal + (frac * (bottomIntVal - topIntVal));
					}
				}

				if (wrtVals[i] == JOAConstants.MISSINGVALUE) {
					double midPresVal = presVals[i];
					double topWRTVal = JOAConstants.MISSINGVALUE;
					double bottomWRTVal = JOAConstants.MISSINGVALUE;
					double topPresVal = JOAConstants.MISSINGVALUE;
					double bottomPresVal = JOAConstants.MISSINGVALUE;
					boolean foundUpper = false;
					boolean foundLower = false;

					// look above first
					int icnt = i - 1;
					int fbl = i - iSpec.getFarBottleLimit();
					if (icnt >= 0) {
						while (icnt >= 0 && icnt >= fbl) {
							if (wrtVals[icnt] != JOAConstants.MISSINGVALUE) {
								topWRTVal = wrtVals[icnt];
								topPresVal = presVals[icnt];
								foundUpper = true;
								break;
							}
							icnt--;
						}
					}

					// look below
					icnt = i + 1;
					fbl = i + iSpec.getFarBottleLimit();
					if (icnt < nBottles - 1) {
						while (icnt < nBottles - 1 && icnt <= fbl) {
							if (wrtVals[icnt] != JOAConstants.MISSINGVALUE) {
								bottomWRTVal = wrtVals[icnt];
								bottomPresVal = presVals[icnt];
								foundLower = true;
								break;
							}
							icnt++;
						}
					}

					if (foundUpper && foundLower) {
						// interpolate
						double denom = Math.abs(topPresVal - bottomPresVal);
						double num2 = Math.abs(midPresVal - bottomPresVal);
						double frac = num2 / denom;
						// interpolate the integrand to this
						wrtVals[i] = topWRTVal + (frac * (bottomWRTVal - topWRTVal));
					}
				}
			}
		}

		// test for shallow outcrop first
		if (wrtVals[0] > iSpec.getMinIntVal() && iSpec.isUseShallowest()) {
			presAtMin = presVals[0];
			intValAtMin = intVals[0];
			intIndexAtMin = 0;
			if (intValAtMin == JOAConstants.MISSINGVALUE) {
				// min val is missing--try the next bottle
				if (nBottles > 1 && intVals[1] != JOAConstants.MISSINGVALUE) {
					presAtMin = presVals[1];
					intValAtMin = intVals[1];
					intIndexAtMin = 1;
				}
				else {
					return JOAConstants.MISSINGVALUE;
				}
			}
			sh.setStnValHitShallowest(sech.getNumStnVars() - 1, true);
			foundMin = true;
		}
		else if (wrtVals[0] == iSpec.getMinIntVal()) {
			presAtMin = presVals[0];
			intValAtMin = intVals[0];
			intIndexAtMin = 0;
			foundMin = true;
		}
		else if (wrtVals[0] > iSpec.getMinIntVal() && !iSpec.isUseShallowest()) {
			// System.out.println(sh.getStn() + " R1");
			return JOAConstants.MISSINGVALUE;
		}

		// test for deep outcrop next
		if (wrtVals[nBottles - 1] < iSpec.getMaxIntVal() && iSpec.isUseDeepest()) {
			presAtMax = presVals[nBottles - 1];
			intValAtMax = intVals[nBottles - 1];
			intIndexAtMax = nBottles - 1;
			sh.setStnValHitDeepest(sech.getNumStnVars() - 1, true);
			foundMax = true;
		}
		else if (wrtVals[nBottles - 1] == iSpec.getMaxIntVal()) {
			presAtMax = presVals[nBottles - 1];
			intValAtMax = intVals[nBottles - 1];
			intIndexAtMax = nBottles - 1;
			foundMax = true;
		}
		else if (wrtVals[nBottles - 1] < iSpec.getMaxIntVal() && !iSpec.isUseDeepest()) {
			// System.out.println(sh.getStn() + " R2");
			return JOAConstants.MISSINGVALUE;
		}

		if ((!foundMin || !foundMax) && iSpec.getSearchMethod() == JOAConstants.SEARCH_TOP_DOWN) {
			// top down search
			for (int i = 1; i < nBottles; i++) {
				double wrtVal = wrtVals[i];
				double presVal = presVals[i];
				double intVal = intVals[i];

				if (!foundMin && wrtVal == iSpec.getMinIntVal()) {
					// exact match--don't need to interpolate
					intIndexAtMin = i - 1;
					intValAtMin = intVal;
					presAtMin = presVal;
					foundMin = true;
				}
				else if (!foundMin && wrtVal > iSpec.getMinIntVal()) {
					// bottle brackets the wrt range
					intIndexAtMin = i - 1;

					if (intVal == JOAConstants.MISSINGVALUE || intVals[i - 1] == JOAConstants.MISSINGVALUE
					    || wrtVal == JOAConstants.MISSINGVALUE || wrtVals[i - 1] == JOAConstants.MISSINGVALUE) {
						return JOAConstants.MISSINGVALUE;
					}
					else {
						// interpolate values for the int and pres values at the boundary of
						// the integration region
						double delWRT = wrtVals[i] - wrtVals[i - 1];
						double frac = (iSpec.getMinIntVal() - wrtVals[i]) / delWRT;

						intValAtMin = intVals[i] + (frac * (intVals[i] - intVals[i - 1]));
						presAtMin = presVals[i] + (frac * (presVals[i] - presVals[i - 1]));

					}
					foundMin = true;
				}
				else if (!foundMax && wrtVal == iSpec.getMaxIntVal()) {
					// exact match -- no interpolation necessary
					intIndexAtMax = i;
					intValAtMax = intVal;
					presAtMax = presVal;
					foundMax = true;
				}
				else if (!foundMax && wrtVal > iSpec.getMaxIntVal()) {
					intIndexAtMax = i;

					if (intVal == JOAConstants.MISSINGVALUE || intVals[i - 1] == JOAConstants.MISSINGVALUE
					    || wrtVal == JOAConstants.MISSINGVALUE || wrtVals[i - 1] == JOAConstants.MISSINGVALUE) {
						return JOAConstants.MISSINGVALUE;
					}
					else {
						// interpolate values for the int and pres values at the boundary of
						// the integration region
						double delWRT = wrtVals[i] - wrtVals[i - 1];
						double frac = (iSpec.getMaxIntVal() - wrtVals[i]) / delWRT;

						intValAtMax = intVals[i] + (frac * (intVals[i] - intVals[i - 1]));
						presAtMax = presVals[i] + (frac * (presVals[i] - presVals[i - 1]));
					}
					foundMax = true;
				}

				if (foundMin && foundMax) {
					break;
				}
			}

			if (presAtMin == JOAConstants.MISSINGVALUE || presAtMax == JOAConstants.MISSINGVALUE
			    || intValAtMin == JOAConstants.MISSINGVALUE || intValAtMax == JOAConstants.MISSINGVALUE) {
				// System.out.println(sh.getStn() + " R7");
				// can't find a bottle below so abort the interpolation
				return JOAConstants.MISSINGVALUE;
			}
		}
		else if ((!foundMin || !foundMax) && iSpec.getSearchMethod() == JOAConstants.SEARCH_BOTTOM_UP) {
			// bottom up search
			double oldPresVal = presVals[nBottles - 1];

			for (int i = nBottles - 2; i >= 0; i--) {
				double wrtVal = wrtVals[i];
				double presVal = presVals[i];
				double intVal = intVals[i];

				if (!foundMin && wrtVal == iSpec.getMinIntVal()) {
					intIndexAtMin = i;
					intValAtMin = intVal;
					presAtMin = presVal - oldPresVal;
					foundMin = true;
				}
				else if (!foundMin && wrtVal < iSpec.getMinIntVal()) {
					// bottle brackets the wrt range
					intIndexAtMin = i;

					if (intVal == JOAConstants.MISSINGVALUE || intVals[i + 1] == JOAConstants.MISSINGVALUE
					    || wrtVal == JOAConstants.MISSINGVALUE || wrtVals[i + 1] == JOAConstants.MISSINGVALUE) {
						return JOAConstants.MISSINGVALUE;
					}
					else {
						// interpolate values for the int and pres values at the boundary of
						// the integration region
						double delWRT = wrtVals[i + 1] - wrtVals[i];
						double frac = (iSpec.getMinIntVal() - wrtVals[i + 1]) / delWRT;

						intValAtMin = intVals[i + 1] + (frac * (intVals[i + 1] - intVals[i]));
						presAtMin = presVals[i + 1] + (frac * (presVals[i + 1] - presVals[i]));
					}
					foundMin = true;
				}
				else if (!foundMax && wrtVal == iSpec.getMaxIntVal()) {
					// bottle brackets the wrt range
					intIndexAtMax = i;
					intValAtMax = intVal;
					presAtMax = presVal;
					foundMax = true;
				}
				else if (!foundMax && wrtVal < iSpec.getMaxIntVal()) {
					// bottle brackets the wrt range
					intIndexAtMax = i + 1;
					if (i == 0) {
						intIndexAtMax++;
					}

					if (intVal == JOAConstants.MISSINGVALUE || intVals[i + 1] == JOAConstants.MISSINGVALUE
					    || wrtVal == JOAConstants.MISSINGVALUE || wrtVals[i + 1] == JOAConstants.MISSINGVALUE) {
						return JOAConstants.MISSINGVALUE;
					}
					else {
						// interpolate values for the int and pres values at the boundary of
						// the integration region
						double delWRT = wrtVals[i + 1] - wrtVals[i];
						double frac = (iSpec.getMaxIntVal() - wrtVals[i + 1]) / delWRT;

						intValAtMax = intVals[i + 1] + (frac * (intVals[i + 1] - intVals[i]));
						presAtMax = presVals[i + 1] + (frac * (presVals[i + 1] - presVals[i]));
					}
					foundMax = true;
				}

				if (foundMin && foundMax) {
					break;
				}
				oldPresVal = presVal;
			}

			if (presAtMin == JOAConstants.MISSINGVALUE || presAtMax == JOAConstants.MISSINGVALUE
			    || intValAtMin == JOAConstants.MISSINGVALUE || intValAtMax == JOAConstants.MISSINGVALUE) { return JOAConstants.MISSINGVALUE; }

		}
		else {
			// mixed search
			// top down search for min
			if (!foundMin) {
				for (int i = 1; i < nBottles; i++) {
					double wrtVal = wrtVals[i];
					double presVal = presVals[i];
					double intVal = intVals[i];

					if (wrtVal == iSpec.getMinIntVal()) {
						intIndexAtMin = i;
						intValAtMin = intVal;
						presAtMin = presVal;
						foundMin = true;
					}
					else if (wrtVal > iSpec.getMinIntVal()) {
						// bottle matches the wrt range
						intIndexAtMin = i - 1;

						if (intVal == JOAConstants.MISSINGVALUE || intVals[i - 1] == JOAConstants.MISSINGVALUE
						    || wrtVal == JOAConstants.MISSINGVALUE || wrtVals[i - 1] == JOAConstants.MISSINGVALUE) {
							return JOAConstants.MISSINGVALUE;
						}
						else {
							// interpolate values for the int and pres values at the boundary
							// of the integration region
							double delWRT = wrtVals[i] - wrtVals[i - 1];
							double frac = (iSpec.getMinIntVal() - wrtVals[i]) / delWRT;

							intValAtMin = intVals[i] + (frac * (intVals[i] - intVals[i - 1]));
							presAtMin = presVals[i] + (frac * (presVals[i] - presVals[i - 1]));
						}
						foundMin = true;
						break;
					}
				}
			}

			// bottom up search for max
			if (!foundMax) {
				for (int i = nBottles - 2; i >= 0; i--) {
					double wrtVal = wrtVals[i];
					double presVal = presVals[i];
					double intVal = intVals[i];

					if (wrtVal == iSpec.getMaxIntVal()) {
						// bottle brackets the wrt range
						intIndexAtMax = i;
						intValAtMax = intVal;
						presAtMax = presVal;
						foundMax = true;
					}
					else if (wrtVal < iSpec.getMaxIntVal()) {
						// bottle brackets the wrt range
						intIndexAtMax = i + 1;
						if (i == 0) {
							intIndexAtMax++;
						}

						if (intVal == JOAConstants.MISSINGVALUE || intVals[i + 1] == JOAConstants.MISSINGVALUE
						    || wrtVal == JOAConstants.MISSINGVALUE || wrtVals[i + 1] == JOAConstants.MISSINGVALUE) {
							return JOAConstants.MISSINGVALUE;
						}
						else {
							// interpolate values for the int and pres values at the boundary
							// of the integration region
							double delWRT = wrtVals[i + 1] - wrtVals[i];
							double frac = (iSpec.getMaxIntVal() - wrtVals[i + 1]) / delWRT;

							intValAtMax = intVals[i + 1] + (frac * (intVals[i + 1] - intVals[i]));
							presAtMax = presVals[i + 1] + (frac * (presVals[i + 1] - presVals[i]));
						}
						foundMax = true;
					}

					if (foundMin && foundMax) {
						break;
					}
				}
			}
		}

		if (presAtMax == JOAConstants.MISSINGVALUE || presAtMin == JOAConstants.MISSINGVALUE
		    || intValAtMin == JOAConstants.MISSINGVALUE || intValAtMax == JOAConstants.MISSINGVALUE) { return JOAConstants.MISSINGVALUE; }

		// now build the integration array
		// create a vector with non missing values and then convert this to an array
		// missing values will drop out of the integration
		Vector<Double> presValsV = new Vector<Double>();
		Vector<Double> intValsV = new Vector<Double>();
		presValsV.addElement(presAtMin);
		intValsV.addElement(intValAtMin);

		for (int i = intIndexAtMin + 1; i < intIndexAtMax; i++) {
			if (intVals[i] != JOAConstants.MISSINGVALUE && presVals[i] != JOAConstants.MISSINGVALUE) {
				presValsV.addElement(presVals[i]);
				intValsV.addElement(intVals[i]);
			}
		}
		presValsV.addElement(presAtMax);
		intValsV.addElement(intValAtMax);

		num = presValsV.size();
		if (num <= 2) { return JOAConstants.MISSINGVALUE; }
		integrandArray = new double[num];
		presArray = new double[num];

		for (int i = 0; i < num; i++) {
			integrandArray[i] = intValsV.elementAt(i);
			presArray[i] = presValsV.elementAt(i);
		}

		// integrandArray[0] = intValAtMin;
		// integrandArray[num - 1] = intValAtMax;
		// presArray[0] = presAtMin;
		// presArray[num - 1] = presAtMax;

		// integrate
		// for (int i=0; i<num; i++) {
		// System.out.println(sh.getStn() + " " + i + " pres = " + presArray[i] + "
		// intVal = " + integrandArray[i]);
		// }
		sumParam = 0;
		for (int i = 1; i < num; i++) {
			// Test to see if data are valid, rescaling valid data only
			pres1 = presArray[i - 1];
			pres2 = presArray[i];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);
			paramVal1 = integrandArray[i - 1];
			paramVal2 = integrandArray[i];
			boolean pv1bad = (paramVal1 == JOAConstants.MISSINGVALUE);
			boolean pv2bad = (paramVal2 == JOAConstants.MISSINGVALUE);

			if (p1bad || p2bad || pv1bad || pv2bad) {
				// Routine quits when it encounters first bad value
				break;
			}

			deltaZ = pres2 - pres1;
			avg = .5 * (paramVal1 + paramVal2); // Evaluate the integrand.
			sumParam += deltaZ * avg;
		}

		// jim's method
		// double jsumParam = integrandArray[0] * (0.5 * (presArray[1] -
		// presArray[0]));
		// jsumParam += integrandArray[num-1] * (0.5 * (presArray[num-1] -
		// presArray[num-2]));
		// for (int i=1; i<num-1; i++) {
		// double pavg = 0.5 * (presArray[i+1] - presArray[i-1]);
		// jsumParam += pavg * integrandArray[i];
		// }
		double zRange = presArray[num - 1] - presArray[0];
		// double ozVal = sumParam/zRange;
		// double jimVal = jsumParam/zRange;

		// System.out.println(sh.getStn() + " oz val = " + ozVal + " jim val = " +
		// jimVal + " diff = " + (ozVal - jimVal));
		if (iSpec.isComputeMean()) {
			return sumParam / zRange;
		}
		else {
			return sumParam;
		}
	}

	public static double findExtremumAtStation(ExtremumSpecification iSpec, Section sech, Station sh, String intParam,
	    String wrtParam, String[] otherParams, double[] retVals) {
		int intPos = sech.getVarPos(intParam, false);
		int wrtPos = sech.getVarPos(wrtParam, false);
		int nBottles = sh.mNumBottles;
		double min = 999999999e29;
		double max = -999999999e29;
		double valAtMin = JOAConstants.MISSINGVALUE;
		double valAtMax = JOAConstants.MISSINGVALUE;
		double minRangeVal = iSpec.getStartSurfValue();
		double maxRangeVal = iSpec.getEndSurfValue();
		Bottle minValBottle = null;
		Bottle maxValBottle = null;
		for (int i = 0; i < nBottles; i++) {
			Bottle bot = (Bottle)sh.mBottles.elementAt(i);
			double iVal = bot.mDValues[intPos];
			double wrtVal = bot.mDValues[wrtPos];

			if (iVal == JOAConstants.MISSINGVALUE
			    || (wrtVal == JOAConstants.MISSINGVALUE && (minRangeVal != JOAConstants.MISSINGVALUE || maxRangeVal == JOAConstants.MISSINGVALUE))) {
				continue;
			}

			if (minRangeVal == JOAConstants.MISSINGVALUE && maxRangeVal == JOAConstants.MISSINGVALUE) {
				if (iVal < min) {
					valAtMin = iVal;
					min = iVal;
					minValBottle = bot;
				}
				if (iVal > max) {
					valAtMax = iVal;
					max = iVal;
					maxValBottle = bot;
				}
			}
			else if (minRangeVal != JOAConstants.MISSINGVALUE && maxRangeVal == JOAConstants.MISSINGVALUE) {
				if (wrtVal < minRangeVal) {
					continue;
				}
				if (iVal < min) {
					valAtMin = iVal;
					min = iVal;
					minValBottle = bot;
				}
				if (iVal > max) {
					valAtMax = iVal;
					max = iVal;
					maxValBottle = bot;
				}
			}
			else if (minRangeVal == JOAConstants.MISSINGVALUE && maxRangeVal != JOAConstants.MISSINGVALUE) {
				if (wrtVal > maxRangeVal) {
					continue;
				}
				if (iVal < min) {
					valAtMin = iVal;
					min = iVal;
					minValBottle = bot;
				}
				if (iVal > max) {
					valAtMax = iVal;
					max = iVal;
					maxValBottle = bot;
				}
			}
			else if (minRangeVal != JOAConstants.MISSINGVALUE && maxRangeVal != JOAConstants.MISSINGVALUE) {
				if (wrtVal < minRangeVal || wrtVal > maxRangeVal) {
					continue;
				}
				if (iVal < min) {
					valAtMin = iVal;
					min = iVal;
					minValBottle = bot;
				}
				if (iVal > max) {
					valAtMax = iVal;
					max = iVal;
					maxValBottle = bot;
				}
			}
		}

		// get the other parameters to report at this station
		if (retVals != null && retVals.length > 0) {
			for (int i = 0; i < retVals.length; i++) {
				String oVal = otherParams[i];
				int oValPos = sech.getVarPos(oVal, false);
				if (!iSpec.isSearchForMax()) {
					if (minValBottle == null) {
						retVals[i] = JOAConstants.MISSINGVALUE;
					}
					else {
						retVals[i] = minValBottle.mDValues[oValPos];
					}
				}
				else {
					if (maxValBottle == null) {
						retVals[i] = JOAConstants.MISSINGVALUE;
					}
					else {
						retVals[i] = maxValBottle.mDValues[oValPos];
					}
				}
			}
		}
		if (iSpec.isSearchForMax()) {
			return valAtMax;
		}
		else {
			return valAtMin;
		}
	}

	public static double interpolateStation(InterpolationSpecification iSpec, Section sech, Station sh, String intParam,
	    String wrtParam) {
		int intPos = sech.getVarPos(intParam, false);
		int pPos = sech.getPRESVarPos();
		int wrtPos = sech.getVarPos(wrtParam, false);
		int nBottles = sh.mNumBottles;

		if (iSpec.isAtSurface()) {
			// extrapolate to the surface observation
			// test for depth limit
			if (iSpec.getDepthLimit() >= 0) {
				// there is a depth limit--find the two top observations above the depth
				// limit
				Bottle bot0 = (Bottle)sh.mBottles.elementAt(0);
				if (bot0.mDValues[pPos] > iSpec.getDepthLimit()) {
					// already failed--first bottle is below depth limit
					return JOAConstants.MISSINGVALUE;
				}
				// look for second obs above the depth limit
				Bottle bot1 = null;
				boolean found = false;
				for (int i = 1; i < nBottles; i++) {
					bot1 = (Bottle)sh.mBottles.elementAt(i);
					double pres = bot1.mDValues[pPos];
					if (pres <= iSpec.getDepthLimit()) {
						found = true;
						break;
					}
				}

				if (found) {
					// found a bottle in the depth range--extrapolate to surface obs
					if (bot0.mDValues[pPos] != JOAConstants.MISSINGVALUE && bot1.mDValues[pPos] != JOAConstants.MISSINGVALUE
					    && bot0.mDValues[intPos] != JOAConstants.MISSINGVALUE
					    && bot1.mDValues[intPos] != JOAConstants.MISSINGVALUE) {
						double denom = bot1.mDValues[pPos] - bot0.mDValues[pPos];
						double num = bot1.mDValues[intPos] - bot0.mDValues[intPos];
						double slope = num / denom;
						double yintercept = bot1.mDValues[intPos] - (slope * bot1.mDValues[pPos]);
						double valAtSurf = (slope * 0.0) + yintercept;
						return valAtSurf;
					}
					else {
						return JOAConstants.MISSINGVALUE;
					}
				}
				else {
					return JOAConstants.MISSINGVALUE;
				}
			}
			else {
				// no depth limit just use the two top observations
				if (nBottles > 2) {
					Bottle bot = (Bottle)sh.mBottles.elementAt(0);
					Bottle bot2 = (Bottle)sh.mBottles.elementAt(1);

					if (bot.mDValues[pPos] != JOAConstants.MISSINGVALUE && bot2.mDValues[pPos] != JOAConstants.MISSINGVALUE
					    && bot.mDValues[intPos] != JOAConstants.MISSINGVALUE
					    && bot2.mDValues[intPos] != JOAConstants.MISSINGVALUE) {
						// compute the gradient in pressure
						double denom = bot.mDValues[pPos] - bot2.mDValues[pPos];
						double num = bot.mDValues[intPos] - bot2.mDValues[intPos];
						double slope = num / denom;
						double yintercept = bot.mDValues[intPos] - (slope * bot.mDValues[pPos]);
						double valAtSurf = (slope * 0.0) + yintercept;
						return valAtSurf;
					}
					else {
						return JOAConstants.MISSINGVALUE;
					}
				}
				else {
					// only one observation--just return it as surface obs
					Bottle bot = (Bottle)sh.mBottles.elementAt(0);
					return bot.mDValues[intPos];
				}
			}
		}
		else if (iSpec.isAtBottom()) {
			// interpolate to the bottom observation
			double bottom = sh.getBottom();

			if (bottom != JOAConstants.MISSINGVALUE) {
				// look for the two observations above bottom and extrapolate to bottom
				Bottle bot1 = (Bottle)sh.mBottles.elementAt(sh.mNumBottles - 1);
				Bottle bot2 = (Bottle)sh.mBottles.elementAt(sh.mNumBottles - 2);

				if (bot1.mDValues[pPos] != JOAConstants.MISSINGVALUE && bot2.mDValues[pPos] != JOAConstants.MISSINGVALUE
				    && bot2.mDValues[intPos] != JOAConstants.MISSINGVALUE && bot2.mDValues[intPos] != JOAConstants.MISSINGVALUE) {
					// compute the gradient in pressure
					double denom = bot1.mDValues[pPos] - bot2.mDValues[pPos];
					double num = bot1.mDValues[intPos] - bot2.mDValues[intPos];
					double slope = num / denom;
					double yintercept = bot2.mDValues[intPos] - (slope * bot2.mDValues[pPos]);
					double valAtSurf = (slope * bottom) + yintercept;
					return valAtSurf;
				}
				else {
					return JOAConstants.MISSINGVALUE;
				}
			}
			else
			// bottom is missing
			if (iSpec.isUseDeepest()) {
				Bottle bot = (Bottle)sh.mBottles.elementAt(sh.mNumBottles - 1);
				return bot.mDValues[intPos];
			}
			else {
				return JOAConstants.MISSINGVALUE;
			}
		}
		else {
			// interpolate to an arbitrary level
			double testVal = iSpec.getAtVal();

			if (iSpec.getSearchMethod() == JOAConstants.SEARCH_TOP_DOWN) {
				// top down search
				for (int i = 0; i < nBottles - 1; i++) {
					Bottle bot = (Bottle)sh.mBottles.elementAt(i);
					Bottle botp1 = (Bottle)sh.mBottles.elementAt(i + 1);
					double iVal = bot.mDValues[intPos];
					double iValp1 = botp1.mDValues[intPos];
					double wrtVal = bot.mDValues[wrtPos];
					double wrtValp1 = botp1.mDValues[wrtPos];
					if (testVal >= wrtVal && testVal <= wrtValp1) {
						// found bounding observations--interpolate
						if (iVal != JOAConstants.MISSINGVALUE && iValp1 != JOAConstants.MISSINGVALUE
						    && wrtVal != JOAConstants.MISSINGVALUE && wrtValp1 != JOAConstants.MISSINGVALUE) {
							// compute the gradient in pressure
							double denom = wrtVal - wrtValp1;
							double num = iVal - iValp1;
							double slope = num / denom;
							double yintercept = iVal - (slope * wrtVal);
							double valAtSurf = (slope * testVal) + yintercept;
							return valAtSurf;
						}
						else {
							return JOAConstants.MISSINGVALUE;
						}
					}
				}
			}
			else {
				// bottom up search
				if (nBottles < 3) { return JOAConstants.MISSINGVALUE; }
				for (int i = nBottles - 1; i > 0; i--) {
					Bottle bot = (Bottle)sh.mBottles.elementAt(i);
					Bottle botm1 = (Bottle)sh.mBottles.elementAt(i - 1);
					double iVal = bot.mDValues[intPos];
					double iValm1 = botm1.mDValues[intPos];
					double wrtVal = bot.mDValues[wrtPos];
					double wrtValm1 = botm1.mDValues[wrtPos];
					if (testVal >= wrtValm1 && testVal <= wrtVal) {
						// found bounding observations--interpolate
						if (iVal != JOAConstants.MISSINGVALUE && iValm1 != JOAConstants.MISSINGVALUE
						    && wrtVal != JOAConstants.MISSINGVALUE && wrtValm1 != JOAConstants.MISSINGVALUE) {
							// compute the gradient in pressure
							double denom = wrtValm1 - wrtVal;
							double num = iValm1 - iVal;
							double slope = num / denom;
							double yintercept = iVal - (slope * wrtVal);
							double valAtSurf = (slope * testVal) + yintercept;
							return valAtSurf;
						}
						else {
							return JOAConstants.MISSINGVALUE;
						}
					}
				}
			}
		}
		return JOAConstants.MISSINGVALUE;
	}

	public static UVCoordinate IntegrateParameter(Section sech, Station sh, String param, double[] result) {
		double sumParam; // sum of trapezoidal panels of delta(travel time)
		double avg; // average sound speed between bottles (i) & (i+1).
		double paramVal1, paramVal2; // param values at bottles (i) & (i+1).
		double z1, z2; // depth at bottles (i) & (i+1).
		double deltaZ; // depth difference between bottles (i) & (i+1).
		int nBottles; // number of bottles at this station.
		int iVar, pPos; // index of pressure in the variable blocks of each bottle.
		boolean p1bad, p2bad; // logical flags signaling missing values.
		double pres1, pres2; // pressure at bottles (i) & (i+1).
		pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		iVar = sech.getVarPos(param, false); // index into variable block for param
		// to integrate.

		nBottles = sh.mNumBottles;
		Bottle bot_11 = (Bottle)sh.mBottles.elementAt(0);

		for (int i = 0; i < nBottles; i++) {
			result[i] = JOAConstants.MISSINGVALUE; // Initialize data array with
			// missing values.
		}

		// Test to see if data are valid
		pres1 = bot_11.mDValues[pPos];
		p1bad = (pres1 == JOAConstants.MISSINGVALUE);

		if (p1bad) { // Routine quits when it encounters first bad value.
			return new UVCoordinate(JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE);
		}

		sumParam = 0; // If first observation is at surface,
		if (pres1 != 0) { // integration begins at zero. Otherwise, assume surface
			// water has uniform properties.
			z1 = presToZ(pres1, sh.mLat);
			paramVal1 = bot_11.mDValues[iVar];
			paramVal2 = paramVal1;
			deltaZ = z1 - 0;
			avg = .5 * (paramVal1 + paramVal2);
			sumParam = sumParam - deltaZ / avg;
		}
		result[0] = sumParam;

		for (int i = 1; i < nBottles; i++) { // Integrate from bottle(1) to
			// bottle(nBottles - 1). // loop from
			// bottle(0) to bottle(nBottles - 1).
			Bottle bot_1 = (Bottle)sh.mBottles.elementAt(i - 1); // Get the variables
			// of bottle (i).
			Bottle bot_2 = (Bottle)sh.mBottles.elementAt(i); // Get the variables of
			// bottle (i+1).

			// Test to see if data are valid, rescaling valid data only
			pres1 = bot_1.mDValues[pPos];
			p1bad = (pres1 == JOAConstants.MISSINGVALUE);
			pres2 = bot_2.mDValues[pPos];
			p2bad = (pres2 == JOAConstants.MISSINGVALUE);

			if (p1bad || p2bad) {
				// Routine quits when it encounters first bad value
				break;
			}

			z1 = presToZ(pres1, sh.mLat);
			z2 = presToZ(pres2, sh.mLat);
			paramVal1 = bot_1.mDValues[iVar];
			paramVal2 = bot_2.mDValues[iVar];
			deltaZ = z2 - z1;
			avg = .5 * (paramVal1 + paramVal2); // Evaluate the integrand.
			sumParam = sumParam - deltaZ / avg;
			result[i] = sumParam;
		} // End integration down the list of bottles.

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				break;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}

		return new UVCoordinate(min, max);
	}

	public static Vector<String> getColorPalList() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("_pal.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filter);
		for (int i = 0; i < files.length; i++) {
			returnList.addElement(files[i]);
		}
		return returnList;
	}

	public static Vector<String> getSurfaceList() {
		FilenameFilter filters = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("srf") || name.endsWith("_srf.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filters);
		for (int i = 0; i < files.length; i++) {
			if (!files[i].equalsIgnoreCase("default_srf.xml")) {
				returnList.addElement(files[i]);
			}
		}
		return returnList;
	}

	public static Vector<String> getFilteredSurfaceList(String name) {
		String lcname = name.toLowerCase();
		FilenameFilter filters = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("srf") || name.endsWith("_srf.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filters);
		for (int i = 0; i < files.length; i++) {
			String lcfn = files[i].toLowerCase();
			if (lcfn.indexOf(lcname) >= 0) {
				returnList.addElement(files[i]);
			}
		}
		return returnList;
	}

	public static Vector<String> getFilteredSurfaceList(String[] names) {
		FilenameFilter filters = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("srf") || name.endsWith("_srf.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filters);
		for (int i = 0; i < files.length; i++) {
			String lcfn = files[i].toLowerCase();
			for (int n = 0; n < names.length; n++) {
				if (lcfn.indexOf(names[n].toLowerCase()) >= 0) {
					returnList.addElement(files[i]);
				}
			}
		}
		return returnList;
	}

	@SuppressWarnings("unchecked")
	public static NewInterpolationSurface getSurface(String newSurfaceName) throws Exception {
		NewInterpolationSurface mSurface = null;
		try {
			// parse as xml first
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			CBNotifyStr notifyStr = new CBNotifyStr();
			parser.setDocumentHandler(notifyStr);
			File file = JOAFormulas.getSupportFile(newSurfaceName);
			parser.parse(JOAFormulas.getSupportPath() + file.getName());

			// turned parse stuff into an interpolation surface
			mSurface = new NewInterpolationSurface(VALS, NLEVELS, PARAM, TITLE, DESCRIP);
		}
		catch (Exception xmlEx) {
			File file = null;
			try {
				file = JOAFormulas.getSupportFile(newSurfaceName);
			}
			catch (IOException ex) {

			}

			try {
				FileInputStream fis = new FileInputStream(file);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				ObjectInputStream in = new ObjectInputStream(gzis);
				InterpolationSurface oldSurface = (InterpolationSurface)in.readObject();
				// translate to new interpolation surface
				mSurface = new NewInterpolationSurface(oldSurface);
				in.close();
			}
			catch (IOException ex) {
				JFrame f = new JFrame("Java OceanAtlas Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "An IOException occured reading a surface from a file");
				throw ex;
			}
			catch (ClassNotFoundException ex) {
				JFrame f = new JFrame("Java OceanAtlas Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "A ClassNotFoundException occured reading a surface from a file");
				throw ex;
			}
		}
		return mSurface;
	}

	public static void saveSurface(File file, NewInterpolationSurface mSurface) throws Exception {
		try {
			// read the DTD file
			/*
			 * String dtdFile = "JOA_Support/joainterpsurface.dtd"; FileInputStream
			 * fis = new FileInputStream(dtdFile); Parser dtdParser = new
			 * Parser(dtdFile); DTD dtd = dtdParser.readDTDStream(fis);
			 */

			if (mSurface.getParam() == null) {
				JFrame f = new JFrame("Save Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Surface must have a parameter.");
				return;
			}

			if (mSurface.getTitle() == null) {
				JFrame f = new JFrame("Save Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Surface must have a title.");
				return;
			}

			// create a documentobject
			Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joainterpsurface the root element
			Element root = doc.createElement("joainterpsurface");

			// make the param element and add it
			Element item = doc.createElement("param");
			item.appendChild(doc.createTextNode(mSurface.getParam()));
			root.appendChild(item);

			// make the title element and add it
			if (mSurface.getTitle().length() > 0) {
				item = doc.createElement("title");
				item.appendChild(doc.createTextNode(mSurface.getTitle()));
				root.appendChild(item);
			}

			if (mSurface.getDescrip() != null && mSurface.getDescrip().length() > 0) {
				// make the description element and add it
				item = doc.createElement("description");
				item.appendChild(doc.createTextNode(mSurface.getDescrip()));
				root.appendChild(item);
			}

			// make the numlevels element and add it
			item = doc.createElement("numlevels");
			item.appendChild(doc.createTextNode(String.valueOf(mSurface.getNumLevels())));
			root.appendChild(item);

			// make the baselevel element and add it
			item = doc.createElement("baselevel");
			item.appendChild(doc.createTextNode(String.valueOf(mSurface.getBaseLevel())));
			root.appendChild(item);

			// make the endlevel element and add it
			item = doc.createElement("endlevel");
			item.appendChild(doc.createTextNode(String.valueOf(mSurface.getEndLevel())));
			root.appendChild(item);

			// make the surfvalues element and add values to if
			item = doc.createElement("surfvalues");
			double[] vals = mSurface.getValues();
			for (int i = 0; i < mSurface.getNumLevels(); i++) {
				Element valItem = doc.createElement("value");
				valItem.appendChild(doc.createTextNode(String.valueOf(vals[i])));
				item.appendChild(valItem);

			}
			root.appendChild(item);

			doc.appendChild(root);
			((TXDocument)doc).setVersion("1.0");
			FileWriter out = new FileWriter(file);
			((TXDocument)doc).printWithFormat(out);
			out.flush();
			out.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static NewColorBar getColorBar(String newCBARName) throws Exception {
		NewColorBar mColorBar = null;

		try {
			// parse as xml first
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			CBNotifyStr notifyStr = new CBNotifyStr();
			parser.setDocumentHandler(notifyStr);
			File file = JOAFormulas.getSupportFile(newCBARName);
			parser.parse(JOAFormulas.getSupportPath() + file.getName());

			if (METADATATYPE == null) {
				mColorBar = new NewColorBar(CVALS, VALS, NLEVELS, PARAM, null, TITLE, DESCRIP);
			}
			else {
				mColorBar = new NewColorBar(CVALS, VALS, NLEVELS, PARAM, UNITS, TITLE, DESCRIP, METADATATYPE);
			}
		}
		catch (Exception xmlEx) {
			File file = null;
			try {
				file = JOAFormulas.getSupportFile(newCBARName);
			}
			catch (IOException ex) {
				JFrame f = new JFrame("Colorbar Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "An IOException occured reading a colorbar (" + newCBARName + ") from a file");
				throw ex;
			}

			try {
				FileInputStream fis = new FileInputStream(file);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				ObjectInputStream in = new ObjectInputStream(gzis);
				ColorBar oldCB = (ColorBar)in.readObject();
				mColorBar = new NewColorBar(oldCB);
				in.close();
			}
			catch (IOException ex) {
				JFrame f = new JFrame("Java OceanAtlas Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "An IOException occured reading a colorbar (" + newCBARName + ") from a file");
				throw ex;
			}
			catch (ClassNotFoundException ex) {
				JFrame f = new JFrame("Java OceanAtlas Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "A ClassNotFoundException occured reading a colorbar from a file");
				throw ex;
			}
		}
		return mColorBar;
	}

	public static Vector<String> getColorBarList() {
		FilenameFilter filterc = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if ((name.endsWith("cbr") || name.endsWith("_cbr.xml")) && !name.startsWith("default")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filterc);
		for (int i = 0; i < files.length; i++) {
			returnList.addElement(files[i]);
		}
		return returnList;
	}

	public static Vector<String> getEtopoList() {
		FilenameFilter filterc = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lcName = name.toLowerCase();
				if (lcName.endsWith(".nc") && lcName.indexOf("etopo") >= 0) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filterc);
		for (int i = 0; i < files.length; i++) {
			returnList.addElement(files[i]);
		}
		return returnList;
	}

	public static Vector<String> getObsFilterList() {
		FilenameFilter filterc = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lcName = name.toLowerCase();
				if (lcName.endsWith(".xml") && lcName.indexOf("_obsf") >= 0) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		returnList.addElement("Select Filter");
		File dir = new File(directory);

		String[] files = dir.list(filterc);
		for (int i = 0; i < files.length; i++) {
			returnList.addElement(files[i]);
		}
		return returnList;
	}

	public static void saveColorBar(File file, NewColorBar cb) {
		try {
			// read the DTD file
			/*
			 * String dtdFile = "JOA_Support/joacolorbar.dtd"; FileInputStream fis =
			 * new FileInputStream(dtdFile); Parser dtdParser = new Parser(dtdFile);
			 * DTD dtd = dtdParser.readDTDStream(fis);
			 */

			if (cb.getParam() == null) {
				JFrame f = new JFrame("Save Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Colorbar must have a parameter.");
				return;
			}

			if (cb.getTitle() == null) {
				JFrame f = new JFrame("Save Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Colorbar must have a title.");
				return;
			}

			// create a documentobject
			Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joainterpsurface the root element
			Element root = doc.createElement("joacolorbar");

			// make the param element and add it
			Element item = doc.createElement("param");
			item.appendChild(doc.createTextNode(cb.getParam()));
			root.appendChild(item);

			item = doc.createElement("paramunits");
			if (cb.getParamUnits() == null) {
				// units are not assigned yet
				item.appendChild(doc.createTextNode("na"));
				root.appendChild(item);

			}
			else {
				item.appendChild(doc.createTextNode(cb.getParamUnits()));
				root.appendChild(item);
			}

			// make the title element and add it
			if (cb.getTitle().length() > 0) {
				item = doc.createElement("title");
				item.appendChild(doc.createTextNode(cb.getTitle()));
				root.appendChild(item);
			}

			if (cb.getDescription() != null && cb.getDescription().length() > 0) {
				// make the description element and add it
				item = doc.createElement("description");
				item.appendChild(doc.createTextNode(cb.getDescription()));
				root.appendChild(item);
			}

			// metadata tags
			if (cb.isMetadataColorBar()) {
				item = doc.createElement("metadatatype");
				item.appendChild(doc.createTextNode(cb.getMetadataType()));
				root.appendChild(item);
			}

			// make the numlevels element and add it
			item = doc.createElement("numlevels");
			item.appendChild(doc.createTextNode(String.valueOf(cb.getNumLevels())));
			root.appendChild(item);

			// make the baselevel element and add it
			item = doc.createElement("baselevel");
			item.appendChild(doc.createTextNode(String.valueOf(cb.getBaseLevel())));
			root.appendChild(item);

			// make the endlevel element and add it
			item = doc.createElement("endlevel");
			item.appendChild(doc.createTextNode(String.valueOf(cb.getEndLevel())));
			root.appendChild(item);

			// make the contourvalues element and add values to if
			item = doc.createElement("contourvalues");
			double[] vals = cb.getValues();
			for (int i = 0; i < cb.getNumLevels(); i++) {
				Element valItem = doc.createElement("value");
				valItem.appendChild(doc.createTextNode(String.valueOf(vals[i])));
				item.appendChild(valItem);

			}
			root.appendChild(item);

			// make the colorvalues element and add values to if
			item = doc.createElement("colorvalues");
			Color[] colors = cb.getColors();
			for (int i = 0; i < cb.getNumLevels(); i++) {
				Color cc = colors[i];
				Element cItem = doc.createElement("cvalue");
				cItem.setAttribute("red", String.valueOf(cc.getRed()));
				cItem.setAttribute("green", String.valueOf(cc.getGreen()));
				cItem.setAttribute("blue", String.valueOf(cc.getBlue()));
				item.appendChild(cItem);

			}
			root.appendChild(item);

			doc.appendChild(root);
			((TXDocument)doc).setVersion("1.0");
			((TXDocument)doc).printWithFormat(new FileWriter(file));
		}
		catch (Exception ex) {
			ex.printStackTrace();

		}
		/*
		 * try { FileOutputStream fos = new FileOutputStream(file); GZIPOutputStream
		 * gzos = new GZIPOutputStream(fos); ObjectOutputStream out = new
		 * ObjectOutputStream(gzos); out.writeObject(cb); out.flush(); out.close();
		 * } catch (IOException ex) { ex.printStackTrace(); System.out.println("An
		 * error occured writing a color bar to a file" + ex.toString()); }
		 */
	}

	public static void exportColorBar(File outFile, NewColorBar cb) {
		// colorbar
		// param
		// title
		// description
		// numlevels
		// baselevel
		// endlevel
		// [] of contourvalues
		// [] of contourColors
		//		
		//	
		try {
			if (cb.getParam() == null) {
				JFrame f = new JFrame("Save Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Colorbar must have a parameter.");
				return;
			}

			if (cb.getTitle() == null) {
				JFrame f = new JFrame("Export Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "Colorbar must have a title.");
				return;
			}

			JsonFactory f = new JsonFactory();
			JsonGenerator jsonGen;

			// test to see if it exists
			jsonGen = f.createJsonGenerator(outFile, JsonEncoding.UTF8);
			jsonGen.setPrettyPrinter(new DefaultPrettyPrinter());

			jsonGen.writeStartObject();
			jsonGen.writeObjectFieldStart("joacolorbar");
			jsonGen.writeStringField("parameter", cb.getParam());
			jsonGen.writeStringField("parameterunits", cb.getParamUnits());

			String titleStr = cb.getParam();
			if (cb.getTitle().length() > 0) {
				titleStr = cb.getTitle();
			}
			jsonGen.writeStringField("title", titleStr);

			String descripStr = cb.getParam() + " (" + String.valueOf(cb.getNumLevels()) + ")";
			if (cb.getDescription() != null && cb.getDescription().length() > 0) {
				descripStr = cb.getDescription();
			}
			jsonGen.writeStringField("description", descripStr);
			jsonGen.writeNumberField("numlevels", cb.getNumLevels());
			jsonGen.writeNumberField("baselevel", cb.getBaseLevel());
			jsonGen.writeNumberField("endlevel", cb.getEndLevel());

			// array of values
			jsonGen.writeArrayFieldStart("contourvalues");
			double[] vals = cb.getValues();
			for (int i = 0; i < cb.getNumLevels(); i++) {
				jsonGen.writeNumber(vals[i]);
			}
			jsonGen.writeEndArray();

			// array of colors
			jsonGen.writeArrayFieldStart("contourcolors");
			Color[] colors = cb.getColors();
			for (int i = 0; i < cb.getNumLevels(); i++) {
				Color cc = colors[i];
				String rgb = Integer.toHexString(cc.getRGB());
				rgb = rgb.substring(2, rgb.length());
				jsonGen.writeString("#" + rgb);
			}
			jsonGen.writeEndArray();
			jsonGen.writeEndObject(); // for field 'joacolorbar'
			jsonGen.close(); // important: will force flushing of output, close
											 // underlying output stream
		}
		catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	public static double getShape(double baseLevel, double endLevel) {
		double diff;

		diff = Math.abs(baseLevel - endLevel);
		if (diff >= 1000) { return 100.0; }
		if (diff >= 100 && diff < 1000) { return 10.0; }
		if (diff >= 10 && diff < 100) { return 1.0; }
		if (diff >= 1 && diff < 10) { return 0.1; }
		if (diff >= 0 && diff < 1) { return 0.01; }
		return 0.001;
	}

	public static Color getContrastingColor(Color inColor) {
		int red = inColor.getRed();
		int green = inColor.getGreen();
		int blue = inColor.getBlue();

		Color returnColor;
		if (red == 255 && green == 255 && blue == 255) {
			return Color.black;
		}
		else if (red == 0 && green == 0 && blue == 0) {
			return Color.white;
		}
		else if (red + green + blue < 200) {
			returnColor = Color.white;
			// red = red == 0 ? red + 10 : red;
			// green = green == 0 ? green + 10 : green;
			// blue = blue == 0 ? blue + 10 : blue;
			// Color tempColor = new Color(red, green, blue);
			// returnColor =
			// tempColor.brighter().brighter().brighter().brighter().brighter();
		}
		else {
			returnColor = Color.black; // inColor.darker().darker().darker();
		}
		return returnColor;
	}

	public static boolean paramExists(FileViewer fv, String param) {
		if (fv.getPropertyPos(param, false) == -1) {
			return false;
		}
		else {
			return true;
		}
	}

	public static double GreatCircle(double lat1, double lon1, double lat2, double lon2) {
		double l, havth;
		double dlo, d;

		/* THIS ROUTINE FINDS THE GREAT CIRCLE COURSE GIVEN TWO POINTS ON THE GLOBE. */
		/* ALSO THE JOAConstants.DISTANCE IN NAUTICAL MILES. */
		/* REF: BOWDITCH, NATHANIEL, AMERICAN PRACTICAL NAVIGATOR. 1981, P 603-608. */

		l = Math.abs(lat2 - lat1);
		dlo = Math.abs(lon2 - lon1);

		if (dlo < 0.001) {
			d = l * 60;
			return d;
		}

		if (dlo > 180) {
			dlo = 360 - dlo;
		}

		havth = HAVD(dlo) + COSD(lat1) * COSD(lat2);
		d = AHAVD(havth + HAVD(l));
		d = d * 60;

		return d;
	}

	public void listSystemProperties() {
		Properties p = System.getProperties();
		Enumeration<?> e = p.propertyNames();
		while (e.hasMoreElements()) {
			String prop = (String)e.nextElement();
			System.out.println(prop);
		}
	}

	private static int PARAM_KEY = 1;
	private static int TITLE_KEY = 2;
	private static int DESCRIP_KEY = 3;
	private static int NLEVELS_KEY = 4;
	private static int BASELVL_KEY = 5;
	private static int ENDLVL_KEY = 6;
	private static int SVAL_KEY = 7;
	private static int CVAL_KEY = 8;
	private static int VALUE_KEY = 9;
	private static int COLOR_KEY = 10;
	private static int METADATATYPE_KEY = 11;
	private static int PARAM_UNITS_KEY = 12;
	// private static int KEY_STATE = NO_KEY;
	private static double VAL = JOAConstants.MISSINGVALUE;
	private static int NLEVELS;
	private static int RED;
	private static int GREEN;
	private static int BLUE;
	private static int ALPHA = 255;
	private static double[] VALS = null;
	private static Color[] CVALS = null;
	private static int V, C;
	private static String PARAM = null;
	private static String UNITS = null;
	private static String TITLE = null;
	private static String DESCRIP = null;
	private static String METADATATYPE = null;

	private static class CBNotifyStr extends HandlerBase {
		public void startDocument() throws SAXException {
			PARAM = null;
			UNITS = null;
			TITLE = null;
			DESCRIP = null;
			METADATATYPE = null;
		}

		public void startElement(String name, AttributeList amap) throws SAXException {
			KEY_STATE = NO_KEY;
			if (name.equals("param")) {
				KEY_STATE = PARAM_KEY;
			}
			else if (name.equals("paramunits")) {
				KEY_STATE = PARAM_UNITS_KEY;
			}
			else if (name.equals("title")) {
				KEY_STATE = TITLE_KEY;
			}
			else if (name.equals("description")) {
				KEY_STATE = DESCRIP_KEY;
			}
			else if (name.equals("metadatatype")) {
				KEY_STATE = METADATATYPE_KEY;
			}
			else if (name.equals("numlevels")) {
				KEY_STATE = NLEVELS_KEY;
			}
			else if (name.equals("baselevel")) {
				KEY_STATE = BASELVL_KEY;
			}
			else if (name.equals("endlevel")) {
				KEY_STATE = ENDLVL_KEY;
			}
			else if (name.equals("surfvalues")) {
				KEY_STATE = SVAL_KEY;
			}
			else if (name.equals("contourvalues")) {
				KEY_STATE = CVAL_KEY;
			}
			else if (name.equals("value")) {
				KEY_STATE = VALUE_KEY;
			}
			else if (name.equals("cvalue")) {
				KEY_STATE = COLOR_KEY;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							RED = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							RED = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							GREEN = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							GREEN = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							BLUE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							BLUE = 150;
						}
					}
					else if (amap.getName(i).equals("alpha")) {
						try {
							ALPHA = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							ALPHA = 255;
						}
					}
				}
			}
		}

		public void characters(char[] ch, int start, int len) throws SAXException {
			String strVal = new String(ch, start, len);
			if (KEY_STATE == PARAM_KEY) {
				PARAM = new String(strVal);
			}
			else if (KEY_STATE == PARAM_UNITS_KEY) {
				UNITS = new String(strVal);
			}
			else if (KEY_STATE == TITLE_KEY) {
				TITLE = new String(strVal);
			}
			else if (KEY_STATE == DESCRIP_KEY) {
				DESCRIP = new String(strVal);
			}
			else if (KEY_STATE == METADATATYPE_KEY) {
				METADATATYPE = new String(strVal);
			}
			else if (KEY_STATE == NLEVELS_KEY) {
				try {
					NLEVELS = Integer.valueOf(strVal).intValue();
				}
				catch (Exception ex) {
					NLEVELS = 1;
				}
				VALS = new double[NLEVELS];
				CVALS = new Color[NLEVELS];
				V = 0;
				C = 0;
			}
			else if (KEY_STATE == VALUE_KEY) { // SVAL_KEY || KEY_STATE == CVAL_KEY)
				// {
				try {
					VAL = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					VAL = JOAConstants.MISSINGVALUE;
				}
			}
		}

		public void endElement(String name) throws SAXException {
			if (name.equals("value")) {
				// build a new value
				VALS[V++] = VAL;
			}
			else if (name.equals("cvalue")) {
				// build a new color value
				CVALS[C++] = new Color(RED, GREEN, BLUE, ALPHA);
			}
		}
	}

	public static Color getColorFromString(String inColor) {
		// color is a comma separated string R, G, B
		char oldDelim = JOAConstants.DELIMITER;
		JOAConstants.DELIMITER = JOAConstants.COMMA_DELIMITER;
		String r = JOAFormulas.getItem(inColor, 1);
		String g = JOAFormulas.getItem(inColor, 2);
		String b = JOAFormulas.getItem(inColor, 3);
		Color retColor = new Color(Integer.valueOf(r).intValue(), Integer.valueOf(g).intValue(), Integer.valueOf(b)
		    .intValue());
		JOAConstants.DELIMITER = oldDelim;
		return retColor;
	}

	public static String getJOAFilename(String inStr) {
		// delete spaces
		String out = trimPreceedingWhiteSpace(inStr, ' ');
		return out + ".joa";
	}

	public static String getCTDExchangeFilename(String inStr) {
		// delete spaces
		String out = trimPreceedingWhiteSpace(inStr, ' ');
		return out + "_ct1.csv";
	}

	public static UVCoordinate getSecMSec(double secs) {
		int isec = (int)secs;
		double msec = secs - isec;
		msec = msec * 1000.0;
		UVCoordinate secStuff = new UVCoordinate((double)isec, msec);
		return secStuff;
	}

	public static double mixedLayerDifference(Section sech, Station sh, String param, double testDepth, double delta) {
		int nBottles = sh.mNumBottles;

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int tPos = sech.getVarPos(param, false);

		// find the value of the test variable at the test depth
		int i = 0;
		double testVal = JOAConstants.MISSINGVALUE;
		double pres = 0.0;
		while (i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			pres = bh.mDValues[pPos];
			double tval = bh.mDValues[tPos];
			if (i > 0 && pres > testDepth && tval != JOAConstants.MISSINGVALUE) {
				// found a bottle below test depth
				Bottle pbh = (Bottle)sh.mBottles.elementAt(i - 1);
				double pPres = pbh.mDValues[pPos];
				double del1 = Math.abs(pres - testDepth);
				double del2 = Math.abs(pPres - testDepth);
				if (del1 <= del2) {
					testVal = tval;
				}
				else if (pbh.mDValues[tPos] != JOAConstants.MISSINGVALUE) {
					testVal = pbh.mDValues[tPos];
					i--;
				}
				break;
			}
			else if (pres == testDepth && tval != JOAConstants.MISSINGVALUE) {
				testVal = bh.mDValues[tPos];
				break;
			}
			i++;
		}

		if (testVal == JOAConstants.MISSINGVALUE) { return JOAConstants.MISSINGVALUE; }

		double diff = 0;
		while (diff < delta && i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			if (bh.mDValues[tPos] != JOAConstants.MISSINGVALUE) {
				diff = Math.abs(testVal - bh.mDValues[tPos]);
			}
			i++;
		}

		if (diff >= delta && i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i - 1);
			if (bh.mDValues[pPos] != JOAConstants.MISSINGVALUE) { return bh.mDValues[pPos]; }
		}
		return JOAConstants.MISSINGVALUE;
	}

	public static double mixedLayerSurface(Section sech, Station sh, String param, double startDepth, double maxDepth,
	    double delta) {
		int nBottles = sh.mNumBottles;
		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int tPos = sech.getVarPos(param, false);

		nBottles = sh.mNumBottles;
		int i = 0;

		// find the first good bottle at the start depth
		while (i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			double tval = bh.mDValues[tPos];
			boolean tbad = (tval == JOAConstants.MISSINGVALUE);
			double pres1 = bh.mDValues[pPos];
			boolean p1bad = (pres1 == JOAConstants.MISSINGVALUE);

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

		// define a surface salinity as the mean of the top five salinities
		double valSum = 0.0;
		double presSum = 0.0;
		double surfMean = 0.0;
		int count = 0;
		while (i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			double pres = bh.mDValues[pPos];
			double val = bh.mDValues[tPos];
			if (pres <= maxDepth && val != JOAConstants.MISSINGVALUE) {
				valSum += val;
				presSum += pres;
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
			return JOAConstants.MISSINGVALUE;
		}

		double diff = 0;
		while (diff < delta && i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			if (bh.mDValues[tPos] != JOAConstants.MISSINGVALUE) {
				diff = Math.abs(surfMean - bh.mDValues[tPos]);
			}
			i++;
		}

		if (diff >= delta && i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i - 1);
			if (bh.mDValues[pPos] != JOAConstants.MISSINGVALUE) { return bh.mDValues[pPos]; }
		}
		return JOAConstants.MISSINGVALUE;
	}

	public static double mixedLayerSlope(Section sech, Station sh, String param, double testDepth, double delta) {
		int nBottles = sh.mNumBottles;
		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int tPos = sech.getVarPos(param, false);

		int i = 0;

		// position to start depth
		double pres = 0.0;
		while (i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			pres = bh.mDValues[pPos];
			if (i > 0 && pres > testDepth) {
				// found a bottle below test depth
				Bottle pbh = (Bottle)sh.mBottles.elementAt(i - 1);
				double pPres = pbh.mDValues[pPos];
				double del1 = Math.abs(pres - testDepth);
				double del2 = Math.abs(pPres - testDepth);
				if (del1 <= del2) {
					break;
				}
				else {
					i--;
					break;
				}
			}
			else if (pres == testDepth) {
				break;
			}
			i++;
		}
		;

		// find the first good bottle at or after start depth
		while (i < nBottles) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			double tval = bh.mDValues[tPos];
			boolean tbad = (tval == JOAConstants.MISSINGVALUE);
			double pres1 = bh.mDValues[pPos];
			boolean p1bad = (pres1 == JOAConstants.MISSINGVALUE);

			if (tbad || p1bad) {
				i++;
			}
			else {
				break;
			}
		}

		if (i < (nBottles - 1)) {
			Bottle oldbh = (Bottle)sh.mBottles.elementAt(i);
			Bottle bh = (Bottle)sh.mBottles.elementAt(++i);

			double diff = 0;
			while (bh != null && diff < delta && i < nBottles) {
				if (bh.mDValues[tPos] != JOAConstants.MISSINGVALUE && oldbh.mDValues[tPos] != JOAConstants.MISSINGVALUE) {
					double tm1 = oldbh.mDValues[tPos];
					double t = bh.mDValues[tPos];
					diff = Math.abs(tm1 - t);
				}
				oldbh = bh;
				try {
					bh = (Bottle)sh.mBottles.elementAt(++i);
				}
				catch (Exception ex) {
					bh = null;
				}
			}

			if (diff >= delta && i < nBottles) {
				Bottle prevbh = (Bottle)sh.mBottles.elementAt(i - 1);
				if (prevbh.mDValues[tPos] != JOAConstants.MISSINGVALUE) { return prevbh.mDValues[pPos]; }
			}
		}
		return JOAConstants.MISSINGVALUE;
	}

	public static void setPopupFromColorBar(Vector<NewColorBar> cbs, NewColorBar cb, JComboBox popup) {
		for (int i = 0; i < cbs.size(); i++) {
			NewColorBar tcb = (NewColorBar)cbs.elementAt(i);
			if (tcb.getParam().equalsIgnoreCase(cb.getParam()) && tcb.getTitle().equalsIgnoreCase(cb.getTitle())) {
				// match found
				popup.setSelectedIndex(i);
				return;
			}
		}
		popup.setSelectedIndex(0);
	}

	public static int correctListOffset(int c) {
		if (c >= 4) {
			if (c == 4 || c == 5) {
				return 9;
			}
			else if (c == 6 || c == 7) {
				return 21;
			}
			else if (c == 8 || c == 9) {
				return 4 + 3;
			}
			else if (c == 10 || c == 11) {
				return 5 + 3;
			}
			else if (c == 12 || c == 13) {
				return 6 + 3;
			}
			else if (c == 14 || c == 15) {
				return 7 + 3;
			}
			else if (c == 16 || c == 17) {
				return 8 + 3;
			}
			else if (c == 18 || c == 19) {
				return 9 + 3;
			}
			else if (c == 20 || c == 21) {
				return 10 + 3;
			}
			else if (c == 22 || c == 23) {
				return 11 + 3;
			}
			else if (c == 24 || c == 25) {
				return 12 + 3;
			}
			else if (c == 26 || c == 27) {
				return 13 + 3;
			}
			else if (c == 28 || c == 29) {
				return 14 + 3;
			}
			else if (c == 30 || c == 31) {
				return 15 + 3;
			}
			else if (c == 32 || c == 33) {
				return 16 + 3;
			}
			else if (c == 34 || c == 35) {
				return 17 + 3;
			}
			else if (c == 36 || c == 37) {
				return 18 + 3;
			}
			else if (c == 38 || c == 39) {
				return 19 + 3;
			}
			else if (c == 40 || c == 41) {
				return 20 + 3;
			}
			else if (c == 42 || c == 43) {
				return 21 + 3;
			}
			else if (c == 44 || c == 45) {
				return 22 + 3;
			}
			else if (c == 46 || c == 47) {
				return 23 + 3;
			}
			else if (c == 48 || c == 49) {
				return 24 + 3;
			}
			else if (c == 50 || c == 51) { return 25 + 3; }
			return 26 + 3;
		}
		else {
			return c;
		}
	}

	public static void sortBottlesByPres(Section sech, Station sh) {
		int pPos = sech.getVarPos("PRES", true);
		if (pPos < 0) {
			pPos = sech.getVarPos("DEPTH", true);
		}
		if (pPos < 0) { return; }
		boolean sortMe = false;
		for (int i = 0; i < sh.mBottles.size() - 1; i++) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			for (int j = i + 1; j < sh.mBottles.size(); j++) {
				Bottle bhp1 = (Bottle)sh.mBottles.elementAt(j);
				double pres = bh.mDValues[pPos];
				double presp1 = bhp1.mDValues[pPos];

				if (pres > presp1) {
					sortMe = true;
					break;
				}
			}
		}

		if (!sortMe) { return; }

		for (int i = 0; i < sh.mBottles.size() - 1; i++) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			for (int j = i + 1; j < sh.mBottles.size(); j++) {
				Bottle bhp1 = (Bottle)sh.mBottles.elementAt(j);
				double pres = bh.mDValues[pPos];
				double presp1 = bhp1.mDValues[pPos];

				if (pres > presp1) {
					// swap the bottles
					// bottle quality
					short tqual = bh.mQualityFlag;
					bh.mQualityFlag = bhp1.mQualityFlag;
					bhp1.mQualityFlag = tqual;

					// variable and qual code values
					for (int v = 0; v < bh.mNumVars; v++) {
						short tqc = bh.mQualityFlags[v];
						bh.mQualityFlags[v] = bhp1.mQualityFlags[v];
						bhp1.mQualityFlags[v] = tqc;

						float tval = bh.mDValues[v];
						bh.mDValues[v] = bhp1.mDValues[v];
						bhp1.mDValues[v] = tval;
					}
				}
			}
		}
	}

	public static void createLatFormatter() {
		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			latDM = new LatitudeFormat();
		}
		else {
			latDM = new LatitudeFormat(JOAConstants.latFormatString);
		}
	}

	public static void createLonFormatter() {
		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			lonDM = new LongitudeFormat();
		}
		else {
			lonDM = new LongitudeFormat(JOAConstants.lonFormatString);
		}
	}

	public static void createTimeFormatter() {
		mGeoDate = new GeoDate();
	}

	// gamma calculation routines
	public static double[] depth_ns(double[] s, double[] t, double[] p, int n, double s0, double t0, double p0)
	    throws NeutralSurfaceErrorException {

		/*
		 * Finds the position at which the neutral surface through a specified
		 * bottle intersects a neighbouring cast. adapted from code by D.Jackett and
		 * T.McDougall:
		 * 
		 * INPUT : s(n) array of cast salinities t(n) array of cast in situ
		 * temperatures p(n) array of cast pressures n length of cast s0 the bottle
		 * salinity t0 the bottle in situ temperature p0 the bottle pressure
		 * 
		 * returns :sns_addr salinity of the neutral surface intersection with the
		 * cast tns_addr in situ temperature of the intersection pns_addr pressure
		 * of the intersection
		 * 
		 * UNITS : salinities psu (IPSS-78) temperatures degrees C (IPTS-68)
		 * pressures db
		 */
		int iter;
		double pc0, tc0, sc0, ec0, ecz0;
		double pc_0 = 0.0, ec_0 = 0.0, ecz_0;
		double p1, p2, ez1, ez2, pc1;
		double eps, r;
		double sns_addr = JOAConstants.MISSINGVALUE, tns_addr = JOAConstants.MISSINGVALUE, pns_addr = JOAConstants.MISSINGVALUE;
		double[] retVals = new double[3];
		double[] e = new double[n]; // (double *) calloc((size_t) n,
																// sizeof(double));
		double sigl, sigu;
		boolean success = false;

		// compute sigma difference between bottle and each level of cast */

		for (int k = 0; k < n; ++k) {
			double[] d = sig_vals(s0, t0, p0, s[k], t[k], p[k]);
			sigl = d[0];
			sigu = d[1];
			e[k] = sigu - sigl;
		}

		// find the bottle pairs containing a crossing
		int last = n - 1;
		int ncr = 0;
		for (int k = 1; k <= last; ++k) {
			int i = k - 1;
			if (e[i] == 0.0) { // an exact crossing at k-1
				++ncr;
				sns_addr = s[i];
				tns_addr = t[i];
				pns_addr = p[i];
			} // end if
			else {
				if ((e[k] * e[i]) < 0.0) {
					++ncr;
					// some Newton-Raphson iterations to find the crossing
					pc0 = p[i] - e[i] * (p[k] - p[i]) / (e[k] - e[i]);
					iter = 0;
					success = false;

					do {
						++iter;
						double[] d;
						try {
							d = JOAFormulas.stp_interp(s, t, p, n, pc0);
						}
						catch (NeutralSurfaceErrorException ex) {
							throw ex;
						}
						sc0 = d[0];
						tc0 = d[1];
						d = sig_vals(s0, t0, p0, sc0, tc0, pc0);
						sigl = d[0];
						sigu = d[1];
						ec0 = sigu - sigl;

						p1 = (p[i] + pc0) * 0.5;
						ez1 = (e[i] - ec0) / (pc0 - p[i]);
						p2 = (pc0 + p[k]) * 0.5;
						ez2 = (ec0 - e[k]) / (p[k] - pc0);
						r = (pc0 - p1) / (p2 - p1);
						ecz_0 = ez1 + r * (ez2 - ez1);

						ecz0 = ecz_0;
						if (iter > 1) {
							ecz0 = -(ec0 - ec_0) / (pc0 - pc_0);
							if (ecz0 == 0.0) {
								ecz0 = ecz_0;
							}
						}

						pc1 = pc0 + ec0 / ecz0;

						// strategy when the iteration jumps out of the interval
						if (pc1 <= p[i] || pc1 >= p[k]) {
							d = JOAFormulas.e_solve(s, t, p, e, n, k, s0, t0, p0);
							sns_addr = d[0];
							tns_addr = d[1];
							pns_addr = d[2];
							if (pns_addr < p[i] || pns_addr > p[k]) {
								System.out.println("\nFATAL ERROR (1) in depth-ns()\n");
								// exit(1);
							}

							success = true;
						} // end if
						else {
							// otherwise, test the accuracy of the iterate ...
							eps = Math.abs(pc1 - pc0);

							if ((Math.abs(ec0) <= 5.0E-5) && (eps <= 5.0E-3)) {
								sns_addr = sc0;
								tns_addr = tc0;
								pns_addr = pc0;
								success = true;
							}
							else if (iter > 10) {
								d = JOAFormulas.e_solve(s, t, p, e, n, k, s0, t0, p0);
								sns_addr = d[0];
								tns_addr = d[1];
								pns_addr = d[2];
								success = true;
							}
							else {
								pc_0 = pc0;
								ec_0 = ec0;
								pc0 = pc1;
							}

						} // end else
					} while (!success);
				} // end if
			} // end else
		} // end for k

		// check last bottle for exact crossing
		if (e[last] == 0.0) {
			++ncr;
			sns_addr = s[last];
			tns_addr = t[last];
			pns_addr = p[last];
		}

		if (ncr == 0) {
			// no crossings
			sns_addr = JOAConstants.MISSINGVALUE;
			tns_addr = JOAConstants.MISSINGVALUE;
			pns_addr = JOAConstants.MISSINGVALUE;
		}
		if (ncr > 1) {
			// multiple crossings
			sns_addr = JOAConstants.MULTPLECROSSINGS;
			tns_addr = JOAConstants.MULTPLECROSSINGS;
			pns_addr = JOAConstants.MULTPLECROSSINGS;
		}
		retVals[0] = sns_addr;
		retVals[1] = tns_addr;
		retVals[2] = pns_addr;
		return retVals;
	} // end depth_ns()

	public static double[] stp_interp(double[] s, double[] t, double[] p, int n, double p0_addr)
	    throws NeutralSurfaceErrorException {
		/*
		 * DESCRIPTION : Interpolate salinity and in situ temperature on a cast by
		 * linearly interpolating salinity and potential temperature
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : s(n) array of cast salinities t(n) array of cast in situ
		 * temperatures p(n) array of cast pressures n length of cast p0_addr
		 * pressure for which salinity and in situ temperature are required
		 * 
		 * returns:s0_addr interpolated value of salinity t0_addr interpolated value
		 * of situ temperature
		 * 
		 * UNITS : salinities psu (IPSS-78) temperatures degrees C (IPTS-68)
		 * pressures db
		 */
		int k, k1;
		double r, pr0, th0, thk;
		double s0_addr, t0_addr;
		try {
			k = JOAFormulas.indx(p, p0_addr, n);
		}
		catch (NeutralSurfaceErrorException ex) {
			throw ex;
		}

		k1 = k + 1;
		pr0 = 0.0;

		r = (p0_addr - p[k]) / (p[k1] - p[k]);
		s0_addr = s[k] + r * (s[k1] - s[k]);
		thk = JOAFormulas.theta(s[k], t[k], p[k], pr0);
		th0 = thk + r * (JOAFormulas.theta(s[k1], t[k1], p[k1], pr0) - thk);
		t0_addr = JOAFormulas.theta(s0_addr, th0, pr0, p0_addr);
		double[] retVals = new double[2];
		retVals[0] = s0_addr;
		retVals[1] = t0_addr;
		return retVals;
	} // end stp_interp()

	public static double param_interp(double[] d, double[] p, double p0_addr) throws NeutralSurfaceErrorException {
		/*
		 * DESCRIPTION : Interpolate parameter on a cast by linearly interpolating
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : d(n) array of cast parameter values p(n) array of cast pressure
		 * values p0_addr pressure for which salinity and in situ temperature are
		 * required
		 * 
		 * returns:interpolated value of parameter
		 */
		int k, k1;
		double r;

		try {
			k = JOAFormulas.indx(p, p0_addr);
		}
		catch (NeutralSurfaceErrorException ex) {
			throw ex;
		}
		k1 = k + 1;

		r = (p0_addr - p[k]) / (p[k1] - p[k]);
		return d[k] + r * (d[k1] - d[k]);
	}

	public static int indx(double[] x, double z) throws NeutralSurfaceErrorException {
		int k;
		try {
			k = indx(x, z, x.length);
		}
		catch (NeutralSurfaceErrorException ex) {
			throw ex;
		}
		return k;
	}

	public static int indx(double[] x, double z, int n) throws NeutralSurfaceErrorException {
		/*
		 * DESCRIPTION : Find the index of a real number in a monotonically
		 * increasing real array PRECISION : Double INPUT : x array of increasing
		 * values n length of array z real number
		 * 
		 * returns : index k - if x(k) <= z < x(k+1), or n-1 - if z = x(n)
		 */

		int k, kl, ku, km;

		if (x[0] < z && z < x[n - 1]) {
			kl = 0;
			ku = n - 1;

			while ((ku - kl) > 1) {
				km = (ku + kl) / 2;
				if (z > x[km]) {
					kl = km;
				}
				else {
					ku = km;
				}
			}
			k = kl;

			if (z == x[k + 1]) {
				++k;
			}
			return (k);
		}

		if (z == x[0]) { return (0); }

		if (z == x[n - 1]) { return (n - 2); }

		// if we get here something is wrong
		System.out.println("z=" + z);
		for (int i = 0; i < n; i++) {
			System.out.println("x[" + i + "] = " + x[i]);
		}
		throw new NeutralSurfaceErrorException("FATAL ERROR in indx():  out of range");
	} // end indx()

	public static double[] e_solve(double[] s, double[] t, double[] p, double[] e, int n, int k, double s0, double t0,
	    double p0) throws NeutralSurfaceErrorException {

		/*
		 * DESCRIPTION : Find the zero of the e function using a bisection method
		 * PRECISION : Double INPUT : s(n) array of cast salinities t(n) array of
		 * cast in situ temperatures p(n) array of cast pressures e(n) array of cast
		 * e values n length of cast k interval (k-1,k) contains the zero s0 the
		 * bottle salinity t0 the bottle in situ temperature p0 the bottle pressure
		 * Returns :sns_addr salinity of the neutral surface intersection with the
		 * cast tns_addr in situ temperature of the intersection pns_addr pressure
		 * of the intersection UNITS : salinities psu (IPSS-78) temperatures degrees
		 * C (IPTS-68) pressures db AUTHOR : David Jackett (adapted from code by
		 * D.Jackett and T.McDougal CREATED : June 1993 REVISION : 1.1 30/6/93
		 */
		int i;
		double pl, pu, pm;
		double el, eu, em;
		double sm, tm;
		double sigl, sigu;
		double sns_addr, tns_addr, pns_addr;
		double[] retVals = new double[4];

		i = k - 1;
		pl = p[i];
		el = e[i];
		pu = p[k];
		eu = e[k];

		int iter = 0;

		do {
			++iter;
			pm = (pl + pu) * 0.5;

			double[] d;
			try {
				d = JOAFormulas.stp_interp(s, t, p, n, pm);
			}
			catch (NeutralSurfaceErrorException ex) {
				throw ex;
			}
			sm = d[0];
			tm = d[1];
			d = JOAFormulas.sig_vals(s0, t0, p0, sm, tm, pm);
			sigl = d[0];
			sigu = d[1];
			em = sigu - sigl;

			if (el * em < 0.0) {
				pu = pm;
				eu = em;
			}
			else if (em * eu < 0.0) {
				pl = pm;
				el = em;
			}
			else {
				if (em == 0.0) {
					sns_addr = sm;
					tns_addr = tm;
					pns_addr = pm;
					retVals[0] = sns_addr;
					retVals[1] = tns_addr;
					retVals[2] = pns_addr;
					retVals[3] = (double)iter;
					return retVals;
				}
			}

			if ((Math.abs(em) <= 5.0E-5) && (Math.abs(pu - pl) <= 5.0E-3)) {
				sns_addr = sm;
				tns_addr = tm;
				pns_addr = pm;
				retVals[0] = sns_addr;
				retVals[1] = tns_addr;
				retVals[2] = pns_addr;
				retVals[3] = (double)iter;
				return retVals;
			}

		} while (iter < 20);

		System.out.println("\nWARNING from e_solve()");
		System.out.println("\niteration #%d  em: %lf  dp: %lf - %lf = %lf" + iter + (double)Math.abs(em) + pl + pu
		    + (double)Math.abs(pu - pl));
		sns_addr = JOAConstants.MISSINGVALUE;
		tns_addr = JOAConstants.MISSINGVALUE;
		pns_addr = JOAConstants.MISSINGVALUE;
		retVals[0] = sns_addr;
		retVals[1] = tns_addr;
		retVals[2] = pns_addr;
		retVals[3] = (double)iter;
		return retVals;
	} // end e_solve()

	public static double[] sig_vals(double s1, double t1, double p1, double s2, double t2, double p2) {
		/*
		 * DESCRIPTION : Computes the sigma values of two neighbouring bottles
		 * w.r.t. the mid pressure
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : s1,s2 bottle salinities t1,t2 bottle in situ temperatures p1,p2
		 * bottle pressures
		 * 
		 * Returns :*sig1_ptr bottle potential density values sig2_ptr
		 * 
		 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure
		 * db density kg m-3
		 */

		double pmid;
		double[] retVals = new double[2];
		pmid = (p1 + p2) * 0.5;
		retVals[0] = hb_svan(s1, theta(s1, t1, p1, pmid), pmid); // sig1
		retVals[1] = hb_svan(s2, theta(s2, t2, p2, pmid), pmid); // sig2
		return retVals;
	} // end sig_vals()

	public static int depth_scv(double[] s, double[] t, double[] p, int n, double s0, double t0, double p0,
	    double[] sscv, double[] tscv, double[] pscv) throws NeutralSurfaceErrorException {

		/*
		 * Find the position at which the scv surface through a specified bottle
		 * intersects a neighbouring cast adapted from code by D.Jackett and
		 * T.McDougall:
		 * 
		 * INPUT : s[n] array of cast salinities t[n] array of cast in situ
		 * temperatures p[n] array of cast pressures n length of cast s0 the bottle
		 * salinity t0 the bottle in situ temperature p0 the bottle pressure
		 * sscv[MAX_SCV] salinity array already allocated tscv[MAX_SCV] temperature
		 * " " pscv[MAX_SCV] pressure " "
		 * 
		 * returns :sscv[MAX_SCV] salinities of the scv surface intersections with
		 * the cast tscv[MAX_SCV] temperatures of the intersections pscv[MAX_SCV]
		 * pressures of the intersections nscv_addr number of intersections
		 * 
		 * UNITS : salinities psu (IPSS-78) temperatures degrees C (IPTS-68)
		 * pressures db
		 */
		int nscv_addr;
		int i, k, iter, last;
		boolean success;
		double sigl, sigu;
		double pc0, tc0, sc0, ec0, ecz0;
		double pc_0 = 0.0, ec_0 = 0.0, ecz_0 = 0.0;
		double p1, p2, ez1, ez2, pc1;
		double eps, r;
		double tref;
		double[] e = new double[n];

		// compute sigma difference between bottle and each level of cast
		for (k = 0; k < n; ++k) {
			tref = theta(s0, t0, p0, p[k]);
			sigl = hb_svan(s0, tref, p[k]);
			sigu = hb_svan(s[k], t[k], p[k]);
			e[k] = sigu - sigl;
		}

		// find the bottle pairs containing a crossing
		last = n - 1;
		nscv_addr = 0;
		for (k = 1; k <= last; ++k) {
			i = k - 1;
			if (e[i] == 0.0) { // an exact crossing at k-1
				sscv[nscv_addr] = s[i];
				tscv[nscv_addr] = t[i];
				pscv[nscv_addr] = p[i];
				++nscv_addr;
			} // end if
			else {
				if ((e[k] * e[i]) < 0.0) {
					// some Newton-Raphson iterations to find the crossing
					pc0 = p[i] - e[i] * (p[k] - p[i]) / (e[k] - e[i]);
					iter = 0;
					success = false;

					do {
						++iter;
						double[] d;
						try {
							d = JOAFormulas.stp_interp(s, t, p, n, pc0);
						}
						catch (NeutralSurfaceErrorException ex) {
							throw ex;
						}
						sc0 = d[0];
						tc0 = d[1];
						tref = theta(s0, t0, p0, pc0);
						sigl = hb_svan(s0, tref, pc0);
						sigu = hb_svan(sc0, tc0, pc0);
						ec0 = sigu - sigl;

						p1 = (p[i] + pc0) * 0.5;
						ez1 = (e[i] - ec0) / (pc0 - p[i]);
						p2 = (pc0 + p[k]) * 0.5;
						ez2 = (ec0 - e[k]) / (p[k] - pc0);
						r = (pc0 - p1) / (p2 - p1);
						ecz_0 = ez1 + r * (ez2 - ez1);

						ecz0 = ecz_0;
						if (iter > 1) {
							ecz0 = -(ec0 - ec_0) / (pc0 - pc_0);
							if (ecz0 == 0.0) {
								ecz0 = ecz_0;
							}
						}

						pc1 = pc0 + ec0 / ecz0;

						// strategy when the iteration jumps out of the interval
						if (pc1 <= p[i] || pc1 >= p[k]) {
							d = scv_solve(s, t, p, e, n, k, s0, t0, p0);
							sscv[nscv_addr] = d[0];
							tscv[nscv_addr] = d[1];
							pscv[nscv_addr] = d[3];
							if (pscv[nscv_addr] < p[i] || pscv[nscv_addr] > p[k]) {
								System.out.println("\nFATAL ERROR (1) in depth-scv()");
								System.out.println("\nscv_solve() returned bad pressure. \n");
								// exit(1);
							}
							success = true;
							++nscv_addr;
						} // end if
						else {
							// otherwise, test the accuracy of the iterate ... */
							eps = Math.abs(pc1 - pc0);

							if ((Math.abs(ec0) <= 5.0E-5) && (eps <= 5.0E-3)) {
								sscv[nscv_addr] = sc0;
								tscv[nscv_addr] = tc0;
								pscv[nscv_addr] = pc0;
								success = true;
								++nscv_addr;
							}
							else if (iter > 10) {
								d = scv_solve(s, t, p, e, n, k, s0, t0, p0);
								sscv[nscv_addr] = d[0];
								tscv[nscv_addr] = d[1];
								pscv[nscv_addr] = d[3];
								success = true;
								++nscv_addr;
							}
							else {
								pc_0 = pc0;
								ec_0 = ec0;
								pc0 = pc1;
							}
						} // end else
					} while (!success);
				} // end if
			} // end else

			// check array size to prevent overflow
			if (nscv_addr >= MAX_SCV) {
				System.out.println("\n FATAL ERROR::depth_scv():  SCV arays not big enough.");
				System.out.println("\n Increase size of MAX_SCV in hb_gamma.h\n");
				// exit(2);
			}
		} // end for k

		// check last bottle for crossing
		if (e[last] == 0.0) {
			sscv[nscv_addr] = s[last];
			tscv[nscv_addr] = t[last];
			pscv[nscv_addr] = p[last];
			++nscv_addr;
		}

		if (nscv_addr == 0) {
			sscv[0] = JOAConstants.MISSINGVALUE;
			tscv[0] = JOAConstants.MISSINGVALUE;
			pscv[0] = JOAConstants.MISSINGVALUE;
		}
		return nscv_addr;
	} // end depth_scv()

	static double[] scv_solve(double[] s, double[] t, double[] p, double[] e, int n, int k, double s0, double t0,
	    double p0) throws NeutralSurfaceErrorException {
		/*
		 * DESCRIPTION : Find the zero of the v function using a bisection method
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : s(n) array of cast salinities t(n) array of cast in situ
		 * temperatures p(n) array of cast pressures e(n) array of cast e values n
		 * length of cast k interval (k-1,k) contains the zero s0 the bottle
		 * salinity t0 the bottle in situ temperature p0 the bottle pressure
		 * 
		 * returns : sscv_ptr salinity of the scv surface intersection with the cast
		 * tscv_ptr in situ temperature of the intersection pscv_ptr pressure of the
		 * intersection iter number of iterations
		 * 
		 * UNITS : salinities psu (IPSS-78) temperatures degrees C (IPTS-68)
		 * pressures db
		 */

		int i;
		double pl, pu, pm;
		double el, eu, em;
		double sm, tm;
		double sigl, sigu;
		double sscv_ptr, tscv_ptr, pscv_ptr;
		int iter;
		double[] retVals = new double[4];

		i = k - 1;
		pl = p[i];
		el = e[i];
		pu = p[k];
		eu = e[k];

		iter = 0;
		do {
			iter++;
			pm = (pl + pu) * 0.5;
			double[] d;
			try {
				d = JOAFormulas.stp_interp(s, t, p, n, pm);
			}
			catch (NeutralSurfaceErrorException ex) {
				throw ex;
			}
			sm = d[0];
			tm = d[1];
			sigl = hb_svan(s0, theta(s0, t0, p0, pm), pm);
			sigu = hb_svan(sm, tm, pm);
			em = sigu - sigl;

			if (el * em < 0.0) {
				pu = pm;
				eu = em;
			}
			else if (em * eu < 0.0) {
				pl = pm;
				el = em;
			}
			else {
				if (em == 0.0) {
					sscv_ptr = sm;
					tscv_ptr = tm;
					pscv_ptr = pm;
					retVals[0] = sscv_ptr;
					retVals[1] = tscv_ptr;
					retVals[2] = pscv_ptr;
					retVals[3] = (double)iter;
					return retVals;
				}
			}

			if ((Math.abs(em) <= 5.0e-5) && (Math.abs(pu - pl) <= 5.0e-3)) {
				sscv_ptr = sm;
				tscv_ptr = tm;
				pscv_ptr = pm;
				retVals[0] = sscv_ptr;
				retVals[1] = tscv_ptr;
				retVals[2] = pscv_ptr;
				retVals[3] = (double)iter;
				return retVals;
			}
		} while (iter < 20);

		System.out.println("\nWARNING from scv_solve()");
		System.out.println("\niteration #%d  em: %lf  dp: %lf - %lf = %lf" + iter + (double)Math.abs(em) + pl + pu
		    + (double)Math.abs(pu - pl));
		sscv_ptr = JOAConstants.MISSINGVALUE;
		tscv_ptr = JOAConstants.MISSINGVALUE;
		pscv_ptr = JOAConstants.MISSINGVALUE;
		retVals[0] = sscv_ptr;
		retVals[1] = tscv_ptr;
		retVals[2] = pscv_ptr;
		retVals[3] = (double)iter;
		return retVals;
	} // end scv_solve()

	static double[] derthe(double s, double t, double p0) {
		/*
		 * Uses the Bryden (1973) polynomial for potential temperature as a function
		 * of (s,t,p) to obtain the partial derivatives of theta WRT t,s,p. Pressure
		 * is in dbars. Adapted from code by D.Jackett and T.McDougall.
		 */
		double A0 = -0.36504E-4;
		double A1 = -0.83198E-5;
		double A2 = +0.54065E-7;
		double A3 = -0.40274E-9;
		double B0 = -0.17439E-5;
		double B1 = +0.29778E-7;
		double D0 = +0.41057E-10;
		double C0 = -0.89309E-8;
		double C1 = +0.31628E-9;
		double C2 = -0.21987E-11;
		double E0 = +0.16056E-12;
		double E1 = -0.50484E-14;

		double ds, p, pp, ppp;
		double part, tt, ttt;
		double[] retVals = new double[3];
		double dthedt, dtheds, dthedp;

		ds = s - 35.0;
		p = p0;
		pp = p * p;
		ppp = pp * p;
		tt = t * t;
		ttt = tt * t;
		part = 1.0 + p * (A1 + 2.0 * A2 * t + 3.0 * A3 * tt + ds * B1);
		dthedt = part + pp * (C1 + 2.0 * C2 * t) + ppp * E1;
		dtheds = p * (B0 + B1 * t) + pp * D0;
		part = A0 + A1 * t + A2 * tt + A3 * ttt + ds * (B0 + B1 * t);
		dthedp = part + 2.0 * p * (ds * D0 + C0 + C1 * t + C2 * tt) + 3.0 * pp * (E0 + E1 * t);
		retVals[0] = dthedt;
		retVals[1] = dtheds;
		retVals[2] = dthedp;
		return retVals;
	} // end derthe()

	public static double[] eosall(double s, double t, double p0) {
		double dthedt, dtheds, dthedp;
		double pref;
		double alpha_old, beta_old, gamma_old;
		double theta_ptr, sigma_ptr, alpha_ptr, beta_ptr, gamma_ptr, soundv_ptr;
		double[][] drv;

		// drv = (double **)calloc(3, sizeof(double *));
		// for (i=0; i < 3; ++i) {
		// drv[i] = (double *) calloc(8, sizeof(double));
		// }

		// Reference pressure (pref) is kept general but will be equal to
		// zero for all perceived applications of neutral surfaces.

		pref = 0.0;
		theta_ptr = theta(s, t, p0, pref);
		drv = hb_eos80d(s, t, p0);

		alpha_old = -drv[1][2] / (drv[0][2] + 1000.0);
		beta_old = drv[0][7] / (drv[0][2] + 1000.0);
		gamma_old = drv[0][6] / (drv[0][2] + 1000.0);

		// calculate specific volume anomaly and sigma-theta
		sigma_ptr = hb_svan(s, theta_ptr, pref);
		double[] d = derthe(s, t, p0);
		dthedt = d[0];
		dtheds = d[1];
		dthedp = d[2];

		alpha_ptr = alpha_old / dthedt;
		beta_ptr = beta_old + alpha_ptr * dtheds;
		gamma_ptr = gamma_old + alpha_ptr * dthedp;
		soundv_ptr = Math.sqrt((double)(Math.abs(1.0E+4 / (gamma_ptr * (drv[0][2] + 1000.0)))));

		double[] retVals = new double[6];
		retVals[0] = theta_ptr;
		retVals[1] = sigma_ptr;
		retVals[2] = alpha_ptr;
		retVals[3] = beta_ptr;
		retVals[4] = gamma_ptr;
		retVals[5] = soundv_ptr;
		return retVals;
	} // end eosall()

	private static double[][] hb_eos80d(double S, double T, double P0) {
		/**
		 * EOS80 derivatives temperature and salinity by N Fofonoff & R Millard
		 * ____________________________________________________________________________
		 * 
		 * DRV Matrix Format (transposed in converting Fortran to C 0 1 2 -- rows
		 * cols 0 V, VT, VTT spvol wrt temperature 1 V0, VOT, V0TT For S,T,0 wrt
		 * temperature 2 RO, ROT, ROTT For S,T,P Density deriv wrt temp 3 K0, K0T,
		 * K0TT For S,T,0 Sec bulk mod wrt temp 4 A, AT, ATT 5 B, BT, BTT Bulk mod
		 * press coeffs 6 DRDP, K, DVDP derivatives wrt pressure 7 R0S, , VS
		 * derivatives wrt salinity
		 * 
		 * Check value: for S = 40 (IPSS-78) , T = 40 DEG C, P0= 10000 Decibars.
		 * DR/DP DR/DT DR/DS DRV(0,6) DRV(1,2) DRV(0,7)
		 * 
		 * Finite difference with 34d order correction done in double precision
		 * 3.46969238E-3 -.43311722 .705110777
		 * 
		 * Explicit differentiation single precision formulation EOS80 3.4696929E-3
		 * -.4331173 .7051107
		 * 
		 * 
		 * 
		 * S - salinity (IPSS-78) T - temperature deg celcius (IPTS-68) P0 -
		 * pressure (decibars) DRV - DRV matrix (3 rows * 8 cols) SPACE ALREADY
		 * ALLOCATED
		 */

		double P, SIG, SR, R1, R2, R3;
		double A, B, C, D, E, A1, B1, AW, BW, K, K0, KW, K35;
		double SAL, V350P, SVA, SIGMA, V0;
		double RHOT, RHO2, V0T, V0S, RHOS;
		double RHOTT, DBDS, BT, BTT;
		double DKDP, DVDP, VT, VTT, R0TT, VS, V2, V, KTT, K0T, KT;
		double VP, DVAN, DR35P, PK, GAM, DK, K0TT, KS, K0S, ATT, AT, DADS;
		double V0TT, RHO1, R4S;
		double[][] DRV = new double[3][8];

		// Convert pressure to bars and take sqrt of salinity
		P = P0 / 10.0;
		SAL = S;
		SR = Math.sqrt(Math.abs(S));

		// Pure water density at atmospheric pressure
		// Bigg, P.H., 1967, Br. J. Applied Physics 8, pp. 521-537.
		R1 = ((((6.536332E-9 * (T) - 1.120083E-6) * (T) + 1.001685E-4) * (T) - 9.095290E-3) * (T) + 6.793952E-2) * (T)
		    - 28.263737;

		// Seawater density atm pressure - coefficients involving salinity
		// R2=A in notatino of Millero and Poisson, 1981
		R2 = (((5.3875E-9 * (T) - 8.2467E-7) * (T) + 7.6438E-5) * (T) - 4.0899E-3) * (T) + 8.24493E-1;

		// R3=B in notation of Millero and Poisson, 1981
		R3 = (-1.6546E-6 * (T) + 1.0227E-4) * (T) - 5.72466E-3;

		// International one-atmosphere equation of state of seawater
		SIG = (R4 * (S) + R3 * SR + R2) * (S) + R1;

		// Specific volume at atmospheric pressure
		V350P = 1.0 / R3500;
		SVA = -SIG * V350P / (R3500 + SIG);
		SIGMA = SIG + DR350;
		DRV[0][2] = SIGMA;
		V0 = 1.0 / (1000.0 + SIGMA);
		DRV[0][1] = V0;

		// Compute derivative wrt SALT of RHO
		R4S = 9.6628E-4;
		RHOS = R4S * SAL + 1.5 * R3 * SR + R2;

		// Compute derivative w/respect to temperature of RHO
		R1 = (((3.268166E-8 * (T) - 4.480332E-6) * (T) + 3.005055E-4) * (T) - 1.819058E-2) * (T) + 6.793952E-2;
		R2 = ((2.155E-8 * (T) - 2.47401E-6) * (T) + 1.52876E-4) * (T) - 4.0899E-3;
		R3 = -3.3092E-6 * (T) + 1.0227E-4;
		RHOT = (R3 * SR + R2) * SAL + R1;
		DRV[1][2] = RHOT;
		RHO1 = 1000.0 + SIGMA;
		RHO2 = RHO1 * RHO1;
		V0T = -RHOT / (RHO2);

		// Specific volume derivative wrt S
		V0S = -RHOS / RHO2;
		DRV[0][7] = RHOS;
		DRV[1][1] = V0T;

		// Compute second derivative of RHO
		R1 = ((1.3072664E-7 * (T) - 1.3440996E-5) * (T) + 6.01011E-4) * (T) - 1.819058E-2;
		R2 = (6.465E-8 * (T) - 4.94802E-6) * (T) + 1.52876E-4;
		R3 = -3.3092E-6;
		RHOTT = (R3 * SR + R2) * SAL + R1;
		DRV[2][2] = RHOTT;
		V0TT = (2.0 * RHOT * RHOT / RHO1 - RHOTT) / (RHO2);
		DRV[2][1] = V0TT;

		// Compute compression terms
		E = (9.1697E-10 * (T) + 2.0816E-8) * (T) - 9.9348E-7;
		BW = (5.2787E-8 * (T) - 6.12293E-6) * (T) + 3.47718E-5;
		B = BW + E * (S);

		// Derivative of B wrt SALT
		DBDS = E;

		// Correct B for anomaly bias change
		DRV[0][5] = B + 5.03217E-5;

		// Derivative of B
		BW = 1.05574E-7 * (T) - 6.12293E-6;
		E = 1.83394E-9 * (T) + 2.0816E-8;
		BT = BW + E * SAL;
		DRV[1][5] = BT;

		// Coefficients of A second derivative of B
		E = 1.83394E-9;
		BW = 1.05574E-7;
		BTT = BW + E * SAL;
		DRV[2][5] = BTT;
		D = 1.91075E-4;
		C = (-1.6078E-6 * (T) - 1.0981E-5) * (T) + 2.2838E-3;
		AW = ((-5.77905E-7 * (T) + 1.16092E-4) * (T) + 1.43713E-3) * (T) - 0.1194975;
		A = (D * SR + C) * (S) + AW;

		// Correct A for anomaly bias change
		DRV[0][4] = A + 3.3594055;

		// Derivative of A wrt SALT
		DADS = 2.866125E-4 * SR + C;

		// Derivative of A
		C = -3.2156E-6 * (T) - 1.0981E-5;
		AW = (-1.733715E-6 * (T) + 2.32184E-4) * (T) + 1.43713E-3;
		AT = C * SAL + AW;
		DRV[1][4] = AT;

		// Second derivative of A
		C = -3.2156E-6;
		AW = -3.46743E-6 * (T) + 2.32184E-4;
		ATT = C * SAL + AW;
		DRV[2][4] = ATT;

		// Coefficient K0
		B1 = (-5.3009E-4 * (T) + 1.6483E-2) * (T) + 7.944E-2;
		A1 = ((-6.1670E-5 * (T) + 1.09987E-2) * (T) - 0.603459) * (T) + 54.6746;
		KW = (((-5.155288E-5 * (T) + 1.360477E-2) * (T) - 2.327105) * (T) + 148.4206) * (T) - 1930.06;
		K0 = (B1 * SR + A1) * (S) + KW;

		// Add bias to output K0 value
		DRV[0][3] = K0 + 21582.27;

		// Derivative of K0 wrt SALT
		K0S = 1.5 * B1 * SR + A1;

		// Derivative of K wrt SALT
		KS = (DBDS * P + DADS) * P + K0S;

		// Derivative of KO
		B1 = -1.06018E-3 * (T) + 1.6483E-2;
		A1 = (-1.8501E-4 * (T) + 2.19974E-2) * (T) - 0.603459;
		KW = ((-2.0621152E-4 * (T) + 4.081431E-2) * (T) - 4.65421) * (T) + 148.4206;
		K0T = (B1 * SR + A1) * SAL + KW;
		DRV[1][3] = K0T;

		// Second derivative of K0
		B1 = -1.06018E-3;
		A1 = -3.7002E-4 * (T) + 2.19974E-2;
		KW = (-6.1863456E-4 * (T) + 8.162862E-2) * (T) - 4.65421;
		K0TT = (B1 * SR + A1) * SAL + KW;
		DRV[2][3] = K0TT;

		// Evaluate pressure polynomial
		// K equals the secant bulk modulus of seawater
		// DK=K(S,T,P)-K(35,0,P)
		// K35=K(35,0,P)
		DK = (B * P + A) * P + K0;
		K35 = (5.03217E-5 * P + 3.359406) * P + 21582.27;
		GAM = P / K35;
		PK = 1.0 - GAM;
		SVA = SVA * PK + (V350P + SVA) * P * DK / (K35 * (K35 + DK));

		V350P = V350P * PK;

		// Compute density anomaly wrt 1000.0 kg/m**3
		// 1. DR350: density anomaly at 35 (IPSS-78), O deg C and 0 decibars
		// 2. DR35P: density anomaly at 35 (IPSS-78), O deg C, pressure variation
		// 3. DVAN: density anomaly variations involving specific volume anomaly

		// Check value: SIGMA = 59.82037 kg/m**3 FOR S = 40 (IPSS-78),
		// T = 40 deg C, P0= 10000 decibars
		DR35P = GAM / V350P;
		DVAN = SVA / (V350P * (V350P + SVA));
		SIGMA = DR350 + DR35P - DVAN;
		DRV[0][2] = SIGMA;
		K = K35 + DK;
		VP = 1.0 - P / K;
		KT = (BT * P + AT) * P + K0T;
		KTT = (BTT * P + ATT) * P + K0TT;
		V = 1.0 / (SIGMA + 1000.0e0);
		DRV[0][0] = V;
		V2 = V * V;

		// Derivative specific volume wrt SALT
		VS = V0S * VP + V0 * P * KS / (K * K);
		RHOS = -VS / V2;
		DRV[2][7] = VS;
		DRV[0][7] = RHOS;
		VT = V0T * VP + V0 * P * KT / (K * K);
		VTT = V0TT * VP + P * (2.0 * V0T * KT + KTT * V0 - 2.0 * KT * KT * V0 / K) / (K * K);
		R0TT = (2.0 * VT * VT / V - VTT) / V2;
		DRV[2][2] = R0TT;
		DRV[1][0] = VT;
		DRV[2][0] = VTT;
		RHOT = -VT / V2;
		DRV[1][2] = RHOT;

		// Pressure derivative DVDP
		// Set A and B to unbiased values
		A = DRV[0][4];
		B = DRV[0][5];
		DKDP = 2.0 * B * P + A;

		// Correct DVDP to per decibar by multiplying *.1
		DVDP = -.1 * V0 * (1.0 - P * DKDP / K) / K;
		DRV[0][6] = -DVDP / V2;
		DRV[1][6] = K;
		DRV[2][6] = DVDP;
		return DRV;
	}

	public static double[] gamma_errors(double[] s, double[] t, double[] p, double[] gamma, double[] a, int n,
	    double along, double alat, double s0, double t0, double p0, double sns, double tns, double pns, int kns,
	    double gamma_ns) throws NeutralSurfaceErrorException {

		/*
		 * Adapted from fortran code by D.Jackett and T.McDougall: DESCRIPTION :
		 * Find the p-theta and the scv errors associated with the basic neutral
		 * surface calculation
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : s(n) array of Levitus cast salinities t(n) array of cast in situ
		 * temperatures p(n) array of cast pressures gamma(n) array of cast neutral
		 * densities a(n) array of cast quadratic coefficients n length of cast
		 * along longitude of Levitus cast alat latitude of Levitus cast s0 bottle
		 * salinity t0 bottle temperature p0 bottle pressure sns salinity of neutral
		 * surface on cast tns temperature of neutral surface on cast pns pressure
		 * of neutral surface on cast kns index of neutral surface on cast gamma_ns
		 * gamma value of neutral surface on cast
		 * 
		 * returns : *pth_error_ptr p-theta gamma error bar scv_l_error_ptr lower
		 * scv gamma error bar scv_h_error_ptr upper scv gamma error bar
		 * 
		 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure
		 * db gamma kg m-3
		 * 
		 * 
		 * AUTHOR : David Jackett
		 * 
		 * CREATED : March 1995
		 * 
		 * REVISION : 1.1 9/3/95
		 */

		int kns1, kscv, kscv1, nscv;
		double[] sscv_m = new double[MAX_SCV];
		double[] tscv_m = new double[MAX_SCV];
		double[] pscv_m = new double[MAX_SCV];
		double pscv, pscv_mid, gamma_scv = 0.0;
		double pr0, Tb, gamma_limit, test_limit;
		double th0, thns, rho_ns, sig_ns;
		double dp, dth, sig_l, sig_h, b, test, drldp;
		double pth_error_ptr;
		double scv_l_error_ptr;
		double scv_h_error_ptr;

		pr0 = 0.0;
		Tb = 2.7e-8;
		gamma_limit = 26.845;
		test_limit = 0.1;

		// p - theta error
		th0 = theta(s0, t0, p0, pr0);
		thns = theta(sns, tns, pns, pr0);
		sig_ns = hb_svan(sns, tns, pns);
		rho_ns = 1000. + sig_ns;

		kns1 = kns + 1;
		double[] d = sig_vals(s[kns], t[kns], p[kns], s[kns1], t[kns1], p[kns1]);
		sig_l = d[0];
		sig_h = d[1];

		b = (gamma[kns1] - gamma[kns]) / (sig_h - sig_l);

		dp = pns - p0;
		dth = thns - th0;
		pth_error_ptr = rho_ns * b * Tb * Math.abs(dp * dth) / 6.0;

		// scv error
		scv_l_error_ptr = 0.0;
		scv_h_error_ptr = 0.0;

		if (alat <= -60. || gamma[0] >= gamma_limit) {
			drldp = (sig_h - sig_l) / (rho_ns * (p[kns1] - p[kns]));
			test = Tb * dth / drldp;

			// approximation
			if (Math.abs(test) <= test_limit) {
				if (dp * dth >= 0.) {
					scv_h_error_ptr = (3 * pth_error_ptr) / (1.0 - test);
				}
				else {
					scv_l_error_ptr = (3 * pth_error_ptr) / (1.0 - test);
				}

			}
			else {
				// explicit scv solution, when necessary
				try {
					nscv = depth_scv(s, t, p, n, s0, t0, p0, sscv_m, tscv_m, pscv_m);
				}
				catch (NeutralSurfaceErrorException ex) {
					throw ex;
				}
				if (nscv > 0) {
					pscv = pscv_m[0];
					if (nscv > 1) {
						pscv_mid = pscv_m[nscv / 2];
						pscv = pscv_m[0];
						if (p0 > pscv_mid) {
							pscv = pscv_m[nscv - 1];
						}
					} // end if nscv > 1

					kscv = indx(p, pscv);
					kscv1 = kscv + 1;
					gamma_scv = gamma_qdr(p[kscv], gamma[kscv], a[kscv], p[kscv1], gamma[kscv1], pscv);

					if (pscv <= pns) {
						scv_l_error_ptr = gamma_ns - gamma_scv;
					}
					else {
						scv_h_error_ptr = gamma_scv - gamma_ns;
					}
				} // end if nscv > 0
			} // end if ABS(test)
		} // end if alat <= -60. || gamma[0] >= gamma_limit

		// check for negative gamma errors
		if (pth_error_ptr < 0. || scv_l_error_ptr < 0. || scv_h_error_ptr < 0.) {
			System.out.println("\nFATAL ERROR in gamma_errors():  negative scv \n");
			// exit(2);
		}

		double[] retVals = new double[3];
		retVals[0] = pth_error_ptr;
		retVals[1] = scv_l_error_ptr;
		retVals[2] = scv_h_error_ptr;
		return retVals;
	} // end gamma_errors()

	public static double[] goor(double[] s, double[] t, double[] p, double[] gamma, int n, double sb, double tb, double pb) {
		/*
		 * DESCRIPTION : Extend a cast of hydrographic data so that a bottle outside
		 * the gamma range of the cast can be labelled with the neutral density
		 * variable
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : s(n) array of cast salinities t(n) array of cast in situ
		 * temperatures p(n) array of cast pressures gamma(n) array of cast gammas n
		 * length of cast sb bottle salinity tb bottle temperature pb bottle
		 * pressure
		 * 
		 * returns :gammab_ptr bottle gamma value g1_err_ptr bottle Type i error
		 * estimate g2_l_err_ptr bottle Type ii lower error estimate g2_h_err_ptr
		 * bottle Type ii upper error estimate
		 * 
		 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure
		 * db gamma kg m-3
		 */

		int n_sth, i, j;
		double delt_b, delt_t, slope, pr0, Tbp;
		double pmid, bmid = 0.0, sigma, sigb, tref;
		double s_new, t_new, e_new;
		double s_old = 0.0, t_old = 0.0, e_old = 0.0;
		double sns = 0.0, tns = 0.0, pns = 0.0, sigu, sigl;
		double thb, b = 0.0, thns, rho_ns, sig_ns;
		double dth, dp, g2err;
		double gammab_ptr = 0.0;
		double g1_err_ptr;
		double g2_l_err_ptr;
		double g2_h_err_ptr;
		double[] retVals = new double[4];

		delt_b = -0.1;
		delt_t = 0.1;
		slope = -0.14;
		pr0 = 0.0;
		Tbp = 2.7e-8;

		// determine if its bottom data
		i = n - 1;
		pmid = (p[i] + pb) * 0.5;
		tref = theta(s[i], t[i], p[i], pmid);
		sigma = hb_svan(s[i], tref, pmid);
		tref = theta(sb, tb, pb, pmid);
		sigb = hb_svan(sb, tref, pmid);

		if (sigb > sigma) {
			// extend the cast data until it is denser
			n_sth = 0;
			s_new = s[i];
			t_new = t[i];
			e_new = sigma - sigb;

			while (sigma < sigb) {
				s_old = s_new;
				t_old = t_new;
				e_old = e_new;
				++n_sth;
				s_new = s[i] + n_sth * delt_b * slope;
				t_new = t[i] + n_sth * delt_b;

				sigma = hb_svan(s_new, theta(s_new, t_new, p[i], pmid), pmid);
				e_new = sigma - sigb;
			} // end while

			if (sigma == sigb) {
				sns = s_new;
				tns = t_new;
			}
			else {
				double[] d = goor_solve(s_old, t_old, e_old, s_new, t_new, e_new, p[i], sb, tb, pb, sigb);
				sns = d[0];
				tns = d[1];
			}

			// compute the new gamma value
			j = i - 1;
			double[] d = sig_vals(s[j], t[j], p[j], s[i], t[i], p[i]);
			sigl = d[0];
			sigu = d[1];
			bmid = (gamma[i] - gamma[j]) / (sigu - sigl);
			sigl = hb_svan(s[i], t[i], p[i]);
			sigu = hb_svan(sns, tns, p[i]);
			gammab_ptr = gamma[i] + bmid * (sigu - sigl);
			pns = p[i];
		} // end if (sigb > sigma)
		else { //
			// determine if the extension is at the top
			pmid = (p[0] + pb) * 0.5;
			sigma = hb_svan(s[0], theta(s[0], t[0], p[0], pmid), pmid);
			sigb = hb_svan(sb, theta(sb, tb, pb, pmid), pmid);

			if (sigb < sigma) {
				// extend the cast until it is lighter
				n_sth = 0;
				s_new = s[0];
				t_new = t[0];
				e_new = sigma - sigb;

				while (sigma > sigb) {
					s_old = s_new;
					t_old = t_new;
					e_old = e_new;
					++n_sth;
					s_new = s[0];
					t_new = t[0] + n_sth * delt_t;

					sigma = hb_svan(s_new, theta(s_new, t_new, p[0], pmid), pmid);
					e_new = sigma - sigb;
				} // end while

				if (sigma == sigb) {
					sns = s_new;
					tns = t_new;
				}
				else {
					double[] d = goor_solve(s_new, t_new, e_new, s_old, t_old, e_old, p[0], sb, tb, pb, sigb);
					sns = d[0];
					tns = d[1];
				}

				// compute the new gamma values
				double[] d = sig_vals(s[0], t[0], p[0], s[1], t[1], p[1]);
				sigl = d[0];
				sigu = d[1];
				bmid = (gamma[1] - gamma[0]) / (sigu - sigl);
				sigl = hb_svan(sns, tns, p[0]);
				sigu = hb_svan(s[0], t[0], p[0]);
				gammab_ptr = gamma[0] - bmid * (sigu - sigl);
				pns = p[0];
			} // end if sigb < sigma
			else {
				System.out.println("\nFATAL ERROR in goor(): gamma out of range\n");
				// exit(1);
			}
		} // end else

		// compute an error estimate
		thb = theta(sb, tb, pb, pr0);
		thns = theta(sns, tns, pns, pr0);
		sig_ns = hb_svan(sns, tns, pns);
		rho_ns = 1000. + sig_ns;
		b = bmid;
		dp = pns - pb;
		dth = thns - thb;
		g1_err_ptr = rho_ns * b * Tbp * Math.abs(dp * dth) / 6.0;
		g2err = rho_ns * b * Tbp * dp * dth * 0.5;

		if (g2err <= 0.0) {
			g2_l_err_ptr = -g2err;
			g2_h_err_ptr = 0.0;
			retVals[0] = gammab_ptr;
			retVals[1] = g1_err_ptr;
			retVals[2] = g2_l_err_ptr;
			retVals[3] = g2_h_err_ptr;
			return retVals;
		}

		g2_l_err_ptr = 0.0;
		g2_h_err_ptr = g2err;
		retVals[0] = gammab_ptr;
		retVals[1] = g1_err_ptr;
		retVals[2] = g2_l_err_ptr;
		retVals[3] = g2_h_err_ptr;
		return retVals;
	} // end goor()

	public static boolean gamma_n(String stn, double[] s, double[] t, double[] p, int n, double along, double alat,
	    double[] gamma, double[] dg_lo, double[] dg_hi) throws NeutralSurfaceErrorException {
		/*
		 * NOTE: in translating from the fortran code to C, the ordering of the
		 * multi-dimensioned array has been inverted to reflect the way the data are
		 * stored in the gamma.nc file. The function read_nc() returns 4 neighboring
		 * casts of {s, t, gamma, and a} each dimensioned: [lat][lon][pressure]
		 * 
		 * Thus the index variables in this code are reversed [j0][i0][nz] relative
		 * to the original fortran (nz,i0,j0) to reflect this.
		 * 
		 * 
		 * DESCRIPTION :Label a cast of hydrographic data at a specified location
		 * with neutral density
		 * 
		 * 
		 * INPUT : gfile ptr to a struct GAMMA_NC s[n] array of cast salinities t[n]
		 * array of cast in situ temperatures p[n] array of cast pressures n length
		 * of cast (n=1 for single bottle) along longitude of cast (0-360) alat
		 * latitude of cast (-80,64)
		 * 
		 * returns :true for success, false for error gamma array of cast gamma
		 * values dg_lo array of gamma lower error estimates dg_hi array of gamma
		 * upper error estimates
		 * 
		 * NOTE: JOAConstants.MISSINGVALUE denotes algorithm failed -99.1 denotes
		 * input data is outside the valid range of the present equation of state
		 * 
		 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure
		 * db gamma kg m-3
		 */
		int ndx, ndy, nij;
		int k, ij, j0, i0, i_min = 0, j_min = 0;
		int ioce;
		int kns;
		boolean itest;
		// int **n0, **iocean0;
		double[] along0 = new double[2], alat0 = new double[2];
		double dx, dy, rx, ry, rw;
		double dist2_min, dist2, dgw_max;
		double dgamma_0, dgamma_1, dgamma_2_l, dgamma_2_h, dgamma_3;
		double gw, gn, g1_err, g2_l_err, g2_h_err;
		double pns, sns, tns;
		double wsum, wt;
		double[] p0 = new double[NZ];
		double[] gwij = new double[4], wtij = new double[4];
		// double ***s0, ***t0, ***gamma0, ***a0;

		// initialize
		ndx = NDX;
		ndy = NDY;

		// p0 = 0.0;//gfile->p0;
		dgamma_0 = 0.0005;
		dgw_max = 0.3;

		if (along < 0.) {
			along += 360.0;
		}

		if (along == 360.0) {
			along = 0.0;
		}

		if (along < 0. || along > 360. || alat < -80. || alat > 64.) { return false; }

		// allocate memory
		int[][] n0 = new int[2][2];
		int[][] iocean0 = new int[2][2];
		double[][][] s0 = new double[2][2][NZ];
		double[][][] t0 = new double[2][2][NZ];
		double[][][] a0 = new double[2][2][NZ];
		double[][][] gamma0 = new double[2][2][NZ];

		for (k = 0; k < n; ++k) {
			gamma[k] = 0.0;
			dg_lo[k] = 0.0;
			dg_hi[k] = 0.0;

			if (s[k] < 0. || s[k] > 42. || t[k] < -2.5 || t[k] > 40. || p[k] < 0. || p[k] > 10000.) {
				gamma[k] = JOAConstants.MISSINGVALUE;
				dg_lo[k] = JOAConstants.MISSINGVALUE;
				dg_hi[k] = JOAConstants.MISSINGVALUE;
			}
		}

		// read records from the netCDF data file
		read_nc(along, alat, s0, t0, p0, gamma0, a0, n0, along0, alat0, iocean0);

		// find the closest cast
		dist2_min = 1.e10;

		i_min = -1;

		for (j0 = 0; j0 < 2; ++j0) {
			for (i0 = 0; i0 < 2; ++i0) {
				if (n0[j0][i0] != 0) {
					dist2 = (along0[i0] - along) * (along0[i0] - along) + (alat0[j0] - alat) * (alat0[j0] - alat);
					if (dist2 < dist2_min) {
						i_min = i0;
						j_min = j0;
						dist2_min = dist2;
					}
				}
			}
		}

		if (i_min < 0) { // no gamma_n info for this location in gamma.nc
			return (false);
		}

		ioce = iocean0[j_min][i_min];

		// label the cast
		dx = Math.abs(along % (double)ndx);
		dy = Math.abs((alat + 80.) % (double)ndy);
		rx = dx / (double)ndx;
		ry = dy / (double)ndy;

		for (k = 0; k < n; ++k) {
			if (gamma[k] > JOAConstants.MISSINGVALUE) {
				dgamma_1 = 0.0;
				dgamma_2_l = 0.0;
				dgamma_2_h = 0.0;
				wsum = 0.0;
				nij = 0;

				// average the gammas over the box
				int c = 0;
				for (j0 = 0; j0 < 2; ++j0) {
					for (i0 = 0; i0 < 2; ++i0) {
						if (n0[j0][i0] > 0) {
							if (j0 == 0) {
								wt = rx * (1.0 - ry);
								if (i0 == 0) {
									wt = (1.0 - rx) * (1.0 - ry);
								}
							}
							else {
								wt = rx * ry;
								if (i0 == 0) {
									wt = (1.0 - rx) * ry;
								}
							}

							wt += 1.0e-6;

							itest = ocean_test(along, alat, ioce, along0[i0], alat0[j0], iocean0[j0][i0], p[k]);

							if (!itest) {
								wt = 0.0;
							}

							double[] d;
							try {
								d = depth_ns(s0[j0][i0], t0[j0][i0], p0, n0[j0][i0], s[k], t[k], p[k]);
							}
							catch (NeutralSurfaceErrorException ex) {
								throw ex;
							}
							sns = d[0];
							tns = d[1];
							pns = d[2];

							if (tns > -98.9 && sns > -98.9 && pns > -98.9) {
								kns = indx(p0, pns);
								gw = gamma_qdr(p0[kns], gamma0[j0][i0][kns], a0[j0][i0][kns], p0[kns + 1], gamma0[j0][i0][kns + 1], pns);
								d = gamma_errors(s0[j0][i0], t0[j0][i0], p0, gamma0[j0][i0], a0[j0][i0], n0[j0][i0], along0[i0],
								    alat0[j0], s[k], t[k], p[k], sns, tns, pns, kns, gw);
								g1_err = d[0];
								g2_l_err = d[1];
								g2_h_err = d[2];
							}
							else if (pns == JOAConstants.MISSINGVALUE || tns == JOAConstants.MISSINGVALUE
							    || sns == JOAConstants.MISSINGVALUE) {
								gw = 0.0;
								g1_err = 0.0;
								g2_l_err = 0.0;
								g2_h_err = 0.0;
							}
							else {
								d = goor(s0[j0][i0], t0[j0][i0], p0, gamma0[j0][i0], n0[j0][i0], s[k], t[k], p[k]);
								gw = d[0];
								g1_err = d[1];
								g2_l_err = d[2];
								g2_h_err = d[3];

								// adjust weight for gamma extrapolation
								gn = gamma0[j0][i0][n0[j0][i0] - 1]; // deepest gamma0
								if (gw > gn) {
									rw = (dgw_max <= (gw - gn)) ? 1.0 : (gw - gn) / dgw_max;
									wt = (1.0 - rw) * wt;
								}
							}

							if (gw > 0.0) {
								gamma[k] += wt * gw;
								dgamma_1 += wt * g1_err;
								dgamma_2_l = dgamma_2_l >= g2_l_err ? dgamma_2_l : g2_l_err;
								dgamma_2_h = dgamma_2_h >= g2_h_err ? dgamma_2_h : g2_h_err;
								wsum += wt;
								wtij[nij] = wt;
								gwij[nij] = gw;
								++nij;
							}

						} // end if n0
					} // end for i0
				} // end for j0

				// the average
				if (wsum != 0.) {
					gamma[k] /= wsum;
					dgamma_1 /= wsum;

					// the gamma errors
					dgamma_3 = 0.0;
					for (ij = 0; ij < nij; ++ij) {
						dgamma_3 += wtij[ij] * Math.abs(gwij[ij] - gamma[k]);
					}

					dgamma_3 /= wsum;

					dg_lo[k] = dgamma_0;
					if (dg_lo[k] < dgamma_1) {
						dg_lo[k] = dgamma_1;
					}
					if (dg_lo[k] < dgamma_2_l) {
						dg_lo[k] = dgamma_2_l;
					}
					if (dg_lo[k] < dgamma_3) {
						dg_lo[k] = dgamma_3;
					}

					dg_hi[k] = dgamma_0;
					if (dg_hi[k] < dgamma_1) {
						dg_hi[k] = dgamma_1;
					}
					if (dg_hi[k] < dgamma_2_h) {
						dg_hi[k] = dgamma_2_h;
					}
					if (dg_hi[k] < dgamma_3) {
						dg_hi[k] = dgamma_3;
					}
				}
				else {
					gamma[k] = JOAConstants.MISSINGVALUE;
					dg_lo[k] = JOAConstants.MISSINGVALUE;
					dg_hi[k] = JOAConstants.MISSINGVALUE;
				}
			} // end if gamma[k] > -99
			else {
				gamma[k] = JOAConstants.MISSINGVALUE;
				dg_lo[k] = JOAConstants.MISSINGVALUE;
				dg_hi[k] = JOAConstants.MISSINGVALUE;
			}
		} // end for k

		return (true);
	} // end gamma_n()

	public static double gamma_qdr(double pl, double gl, double a, double pu, double gu, double p) {
		/*
		 * DESCRIPTION : Evaluate the quadratic gamma profile at a pressure between
		 * two bottles
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : pl, pu bottle pressures gl, gu bottle gamma values a quadratic
		 * coefficient p pressure for gamma value
		 * 
		 * returns :gamma value at p
		 * 
		 * UNITS : pressure db gamma kg m-3 a kg m-3
		 * 
		 * AUTHOR : David Jackett
		 */
		double p1, p2;
		p1 = (p - pu) / (pu - pl);
		p2 = (p - pl) / (pu - pl);
		return ((a * p1 + (gu - gl)) * p2 + gl);
	}

	public static boolean ocean_test(double x1, double y1, int io1, double x2, double y2, int io2, double z) {
		/*
		 * DESCRIPTION : Test whether two locations are connected by ocean
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : x1 longitude of first location y1 latitude of first location io1
		 * ocean of first location x2 longitude of second location y2 latitude of
		 * second location io2 ocean of second location z depth of connection
		 * 
		 * returns : 0 or 1 success of connection (0 = no connect)
		 */

		int isj1, isj2;
		boolean itest;
		double y;
		double[] x_js = new double[3], y_js = new double[3];
		double em1, em2, c1, c2;

		x_js[0] = 129.87;
		x_js[1] = 140.37;
		x_js[2] = 142.83;
		y_js[0] = 32.75;
		y_js[1] = 37.38;
		y_js[2] = 53.58;
		y = (y1 + y2) * 0.5;

		// same ocean talks
		if (io1 == io2) { return (true); }

		// Atlantic talks
		if (((io1 == 5) || (io1 == 6)) && ((io2 == 5) || (io2 == 6))) { return (true); }

		// exclude Antarctic tip
		if (((io1 * io2) == 12) && (y < -60.)) { return (false); }

		if (y <= -20.) {
			// land of South America doesn't talk
			if (y >= -48. && (io1 * io2) == 12) { return (false); }

			// everything else south of -20 talks
			return (true);
		}

		// test multiple conditions ...
		itest = false;

		// Pacific talks
		if (((io1 == 1) || (io1 == 2)) && ((io2 == 1) || (io2 == 2))) {
			itest = true;
		}

		// Indian talks
		if (((io1 == 3) || (io1 == 4)) && ((io2 == 3) || (io2 == 4))) {
			itest = true;
		}

		// Indonesian throughflow
		if (((io1 * io2) == 8) && (z <= 1200.) && (x1 >= 124.) && (x1 <= 132.) && (x2 >= 124.) && (x2 <= 132)) {
			itest = true;
		}

		// exclude Japan Sea from talking
		if ((x_js[0] <= x1 && x1 <= x_js[2] && y_js[0] <= y1 && y1 <= y_js[2])
		    || (x_js[0] <= x2 && x2 <= x_js[2] && y_js[0] <= y2 && y2 <= y_js[2])) {
			em1 = (y_js[1] - y_js[0]) / (x_js[1] - x_js[0]);
			c1 = y_js[0] - em1 * x_js[0];

			em2 = (y_js[2] - y_js[1]) / (x_js[2] - x_js[1]);
			c2 = y_js[1] - em2 * x_js[1];

			isj1 = 0;
			if (((y1 - em1 * x1 - c1) >= 0) && ((y1 - em2 * x1 - c2) >= 0.)) {
				isj1 = 1;
			}

			isj2 = 0;
			if (((y2 - em1 * x2 - c1) >= 0.) && ((y2 - em2 * x2 - c2) >= 0.)) {
				isj2 = 1;
			}

			if (isj1 == isj2) { return (true); }

			return (false);
		}

		return (itest);
	} // end ocean_test()

	public static double[] goor_solve(double sl, double tl, double el, double su, double tu, double eu, double p,
	    double s0, double t0, double p0, double sigb) {
		/*
		 * DESCRIPTION : Find the intersection of a potential density surface
		 * between two bottles using a bisection method
		 * 
		 * PRECISION : Double
		 * 
		 * INPUT : sl, su bottle salinities tl, tu bottle in situ temperatures el,
		 * eu bottle e values p bottle pressures (the same) s0 emanating bottle
		 * salinity t0 emanating bottle in situ temperature p0 emanating bottle
		 * pressure
		 * 
		 * returns : sns_ptr salinity of the neutral surface intersection with the
		 * bottle pair tns_ptr in situ temperature of the intersection
		 * 
		 * UNITS : salinities psu (IPSS-78) temperatures degrees C (IPTS-68)
		 * pressures db
		 */
		int iter;
		double rm, thm, sm, em;
		double rl, ru, pmid;
		double thl, thu;
		double sigma;
		double sns_ptr;
		double[] retVals = new double[2];

		rl = 0.0;
		ru = 1.0;
		pmid = (p + p0) * 0.5;
		thl = theta(sl, tl, p, pmid);
		thu = theta(su, tu, p, pmid);

		iter = 0;
		do {
			rm = (rl + ru) * 0.5;
			sm = sl + rm * (su - sl);
			thm = thl + rm * (thu - thl);
			sigma = hb_svan(sm, thm, pmid);
			em = sigma - sigb;

			if (em == 0.0) {
				sns_ptr = sm;
				retVals[0] = sns_ptr;
				retVals[1] = sns_ptr;
				return retVals;
			}

			if (el * em < 0.0) {
				ru = rm;
				eu = em;
			}
			else {
				if (em * eu < 0.0) {
					rl = rm;
					el = em;
				}
			}

			if (Math.abs(em) <= 5.0e-5 && Math.abs(ru - rl) <= 5.0e-3) {
				sns_ptr = sm;
				retVals[0] = sns_ptr;
				retVals[1] = sns_ptr;
				return retVals;
			}
		} while (++iter <= 20);

		System.out.println("\n WARNING from goor_solve(): max iterations exceeded\n");
		sns_ptr = sm;
		retVals[0] = sns_ptr;
		retVals[1] = sns_ptr;
		return retVals;
	} // end goor_solve()

	@SuppressWarnings("unchecked")
	public static void readGamma() throws Exception {
		Dbase mGammaDB;
		File gammaFile = null;
		try {
			gammaFile = JOAFormulas.getSupportFile("Gggamma.nc");
		}
		catch (IOException ex) {
			// present an error dialog
			throw ex;
		}
		String dir = gammaFile.getParent();

		EpicPtrs ptrDB = new EpicPtrs();

		// create a pointer
		ArrayList<EpicPtr> params = null;
		EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "Gamma Import", "Gamma", "na", "na", -99, -99,
		    new gov.noaa.pmel.util.GeoDate(), -99, -99, params, "gamma.nc", dir, null);

		// set the data of ptrDB to this one entry
		ptrDB.setFile(gammaFile);
		ptrDB.setData(epPtr);

		// create a database
		PointerDBIterator pdbi = ptrDB.iterator();
		EPSDbase etopoDB = new EPSDbase(pdbi, true);

		// get the database
		EPSDBIterator dbItor = etopoDB.iterator(true);

		try {
			mGammaDB = (Dbase)dbItor.getElement(0);
		}
		catch (Exception ex) {
			throw ex;
		}

		// get the axes and variables
		// latitude axis
		Axis latAxis = mGammaDB.getAxis("lat");
		if (latAxis == null) {
			; // throw something
		}
		MultiArray latma = latAxis.getData();
		mSavedLats = EPS_Util.get1DFloatArray(latma, 0);

		// longitude axis
		Axis lonAxis = mGammaDB.getAxis("lon");
		if (lonAxis == null) {
			; // throw something
		}
		MultiArray lonma = lonAxis.getData();
		mSavedLons = EPS_Util.get1DFloatArray(lonma, 0);

		// depth axis
		Axis zAxis = mGammaDB.getAxis("pressure");
		if (zAxis == null) {
			; // throw something
		}
		MultiArray zma = zAxis.getData();

		Vector<EPSVariable> vars = mGammaDB.getMeasuredVariables(false);
		MultiArray bottlema = null;
		MultiArray oceanma = null;
		MultiArray salinityma = null;
		MultiArray temperaturema = null;
		MultiArray gammama = null;
		MultiArray ama = null;

		for (int i = 0; i < vars.size(); i++) {
			EPSVariable var = (EPSVariable)vars.elementAt(i);
			// System.out.println(var.getName());

			if (var.getName().trim().equalsIgnoreCase("n")) {
				bottlema = var.getData();
			}
			else if (var.getName().trim().equalsIgnoreCase("iocean")) {
				oceanma = var.getData();
			}
			else if (var.getName().trim().equalsIgnoreCase("s")) {
				salinityma = var.getData();
			}
			else if (var.getName().trim().equalsIgnoreCase("t")) {
				temperaturema = var.getData();
			}
			else if (var.getName().trim().equalsIgnoreCase("gamma")) {
				gammama = var.getData();
			}
			else if (var.getName().trim().equalsIgnoreCase("a")) {
				ama = var.getData();
			}
		}

		/*
		 * EPsPosiable bottleVar = (EPsPosiable)vars.elementAt(0); MultiArray
		 * bottlema = bottleVar.getData();
		 * 
		 * EPsPosiable oceanVar = (EPsPosiable)vars.elementAt(1); MultiArray oceanma
		 * = oceanVar.getData();
		 * 
		 * EPsPosiable salinityVar = (EPsPosiable)vars.elementAt(2); MultiArray
		 * salinityma = salinityVar.getData();
		 * 
		 * EPsPosiable temperatureVar = (EPsPosiable)vars.elementAt(3); MultiArray
		 * temperaturema = temperatureVar.getData();
		 * 
		 * EPsPosiable gammaVar = (EPsPosiable)vars.elementAt(4); MultiArray gammama
		 * = gammaVar.getData();
		 * 
		 * EPsPosiable aVar = (EPsPosiable)vars.elementAt(5); MultiArray ama =
		 * aVar.getData();
		 */

		try {
			mGPresArray = EPS_Util.get1DFloatArray(zma, 0);
			mGBottleArray = get2DShortArray(bottlema);
			mGOceanArray = get2DShortArray(oceanma);
			mGSalinityArray = get3DFloatArray(salinityma);
			mGTemperatureArray = get3DFloatArray(temperaturema);
			mGGammaArray = get3DFloatArray(gammama);
			mGAArray = get3DFloatArray(ama);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static float[][][] get3DFloatArray(MultiArray ma) throws IOException {
		int[] lens = ma.getLengths();
		float[][][] xArray = new float[lens[0]][lens[1]][lens[2]];
		Object array = ma.toArray();
		if (array instanceof float[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					for (int k = 0; k < lens[2]; k++) {
						xArray[i][j][k] = ((float[])array)[c++];
					}
				}
			}
		}
		else if (array instanceof double[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					for (int k = 0; k < lens[2]; k++) {
						xArray[i][j][k] = ((float[])array)[c++];
					}
				}
			}
		}
		else if (array instanceof int[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					for (int k = 0; k < lens[2]; k++) {
						xArray[i][j][k] = (float)((int[])array)[c++];
					}
				}
			}
		}
		else if (array instanceof short[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					for (int k = 0; k < lens[2]; k++) {
						xArray[i][j][k] = (float)((short[])array)[c++];
					}
				}
			}
		}
		return xArray;
	}

	public static short[][] get2DShortArray(MultiArray ma) throws IOException {
		int[] lens = ma.getLengths();
		short[][] xArray = new short[lens[0]][lens[1]];
		Object array = ma.toArray();
		if (array instanceof float[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					xArray[i][j] = ((short[])array)[c++];
				}
			}
		}
		else if (array instanceof double[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					xArray[i][j] = ((short[])array)[c++];
				}
			}
		}
		else if (array instanceof int[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					xArray[i][j] = (short)((int[])array)[c++];
				}
			}
		}
		else if (array instanceof short[]) {
			int c = 0;
			for (int i = 0; i < lens[0]; i++) {
				for (int j = 0; j < lens[1]; j++) {
					xArray[i][j] = ((short[])array)[c++];
				}
			}
		}
		return xArray;
	}

	public static void read_nc(double along, double alat, double[][][] s0, double[][][] t0, double[] p0,
	    double[][][] gamma0, double[][][] a0, int[][] n0, double[] along0, double[] alat0, int[][] iocean0) {
		/*
		 * DESCRIPTION : Read variables from the netcdf labelled data file. The file
		 * must be opened by the HydroBase function gamma_nc_init() -- which
		 * initializes struct GAMMA_NC *gfile with various info -- and closed by the
		 * calling routine using nc_close() from the netcdf C library functions
		 * version 3.5 or later.
		 * 
		 * INPUT : along longitude of record alat latitude of record
		 * 
		 * Memory for these arrays is allocated by the calling routine -- not here.
		 * The NZ dimension is defined in hb_gamma.h. The order of dimensions in the
		 * gamma.nc file is lat, lon, pressure; therefore the dimension of each of
		 * these C-arrays is [ny][nx][nz] with nz varying fastest.
		 * 
		 * 
		 * Returns :s0[2][2][NZ] arrays of cast salinities t0[2][2][NZ] arrays of
		 * cast in situ temperatures gamma0[2][2][NZ] array of cast gamma values
		 * a0[2][2][NZ] arrays of cast a values n0[2][2] length of casts along0[2]
		 * array of cast longitudes alat0[2] array of cast latitudes iocean0[2][2]
		 * array of cast oceans
		 * 
		 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure
		 * db gamma kg m-3
		 */

		int k, nx, nz, ndx, ndy;
		// int start[3], count[3], ierr;
		int row0, col0, row1, col1; // row0 corresponds to j0 col0 corresponds to i0
		int i0, i1, j0, j1;
		// float s0_s[NZ], t0_s[NZ]; // netcdf file has float values
		// float a0_s[NZ], gamma0_s[NZ];

		// initialize from definitions in hb_gamma.h
		nx = NX;
		nz = NZ;
		ndx = NDX;
		ndy = NDY;

		// find indices for corners of box containing alat,along
		col0 = (int)(along / ndx); // left
		row0 = (int)((alat + 88.) / ndy); // lower
		row1 = row0 + 1; // upper
		col1 = col0 + 1; // right
		if (col1 == nx) {
			col1 = 0;
		}

		alat0[0] = mSavedLats[row0];
		alat0[1] = mSavedLats[row1];
		along0[0] = mSavedLons[col0];
		along0[1] = mSavedLons[col1];

		i0 = j0 = 0;
		i1 = j1 = 1;

		// get lower/left corner (row0,col0) (j0,i0)
		// start[0] = row0;
		// start[1] = col0;
		// start[2] = 0;

		// count[0] = 1;
		// count[1] = 1;
		// count[2] = nz;

		/*
		 * ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_s, (const size_t *)
		 * start, (const size_t *)count, s0_s); if (ierr != NC_NOERR) {
		 * fprintf(stderr,"ERROR #[%d] reading gamma.nc variable\n", ierr); exit(1);
		 * } ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_t, (const size_t
		 * *)start, (const size_t *)count, t0_s); if (ierr != NC_NOERR) {
		 * fprintf(stderr,"ERROR #[%d] reading gamma.nc variable\n", ierr); exit(1);
		 * } ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_a, (const size_t
		 * *)start, (const size_t *)count, a0_s); if (ierr != NC_NOERR) {
		 * fprintf(stderr,"ERROR #[%d] reading gamma.nc variable\n", ierr); exit(1);
		 * } ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_gamma, (const size_t
		 * *)start, (const size_t *)count, gamma0_s); if (ierr != NC_NOERR) {
		 * fprintf(stderr,"ERROR #[%d] reading gamma.nc variable\n", ierr); exit(1);
		 * }
		 */

		for (k = 0; k < nz; ++k) {
			p0[k] = (double)mGPresArray[k];
		}

		// store as double values in returned arrays
		for (k = 0; k < nz; ++k) {
			s0[j0][i0][k] = (double)mGSalinityArray[row0][col0][k];
			t0[j0][i0][k] = (double)mGTemperatureArray[row0][col0][k];
			a0[j0][i0][k] = (double)mGAArray[row0][col0][k];
			gamma0[j0][i0][k] = (double)mGGammaArray[row0][col0][k];
		}

		// get lower/right corner (row0,col1) (j0,i1)
		// start[0] = row0;
		// start[1] = col1;
		// start[2] = 0;

		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_s, (const size_t
		// *)start, (const size_t *)count, s0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_t, (const size_t
		// *)start, (const size_t *)count, t0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_a, (const size_t
		// *)start, (const size_t *)count, a0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_gamma, (const size_t
		// *)start, (const size_t *)count, gamma0_s);

		// store as double values in returned arrays
		for (k = 0; k < nz; ++k) {
			s0[j0][i1][k] = (double)mGSalinityArray[row0][col1][k];
			t0[j0][i1][k] = (double)mGTemperatureArray[row0][col1][k];
			a0[j0][i1][k] = (double)mGAArray[row0][col1][k];
			gamma0[j0][i1][k] = (double)mGGammaArray[row0][col1][k];
		}

		// get upper/left corner (row1,col0) (j1,i0)
		// start[0] = row1;
		// start[1] = col0;
		// start[2] = 0;

		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_s, (const size_t
		// *)start, (const size_t *)count, s0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_t, (const size_t
		// *)start, (const size_t *)count, t0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_a, (const size_t
		// *)start, (const size_t *)count, a0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_gamma, (const size_t
		// *)start, (const size_t *)count, gamma0_s);

		// store as double values in returned arrays
		for (k = 0; k < nz; ++k) {
			s0[j1][i0][k] = (double)mGSalinityArray[row1][col0][k];
			t0[j1][i0][k] = (double)mGTemperatureArray[row1][col0][k];
			a0[j1][i0][k] = (double)mGAArray[row1][col0][k];
			gamma0[j1][i0][k] = (double)mGGammaArray[row1][col0][k];
		}

		// get upper/right corner (row1,col1) (j1,i1)
		// start[0] = row1;
		// start[1] = col1;
		// start[2] = 0;

		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_s, (const size_t
		// *)start, (const size_t *)count, s0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_t, (const size_t
		// *)start, (const size_t *)count, t0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_a, (const size_t
		// *)start, (const size_t *)count, a0_s);
		// ierr = nc_get_vara_float(gfile->id_gnc, gfile->id_gamma, (const size_t
		// *)start, (const size_t *)count, gamma0_s);

		// store as double values in returned arrays
		for (k = 0; k < nz; ++k) {
			s0[j1][i1][k] = (double)mGSalinityArray[row1][col1][k];
			t0[j1][i1][k] = (double)mGTemperatureArray[row1][col1][k];
			a0[j1][i1][k] = (double)mGAArray[row1][col1][k];
			gamma0[j1][i1][k] = (double)mGGammaArray[row1][col1][k];
		}

		iocean0[j0][i0] = mGOceanArray[row0][col0];
		iocean0[j0][i1] = mGOceanArray[row0][col1];
		iocean0[j1][i0] = mGOceanArray[row1][col0];
		iocean0[j1][i1] = mGOceanArray[row1][col1];

		n0[j0][i0] = mGBottleArray[row0][col0];
		n0[j0][i1] = mGBottleArray[row0][col1];
		n0[j1][i0] = mGBottleArray[row1][col0];
		n0[j1][i1] = mGBottleArray[row1][col1];
		return;
	} // end read_nc()

	public static double[] neutralSurfaces(double[] s, double[] t, double[] p, double[] gamma, int n, double level)
	    throws NeutralSurfaceErrorException {
		/*
		 * DESCRIPTION : For a cast of hydrographic data which has been labelled
		 * with the neutral density variable gamma, find the salinities,
		 * temperatures and pressures on ng specified neutral density surfaces. Cast
		 * must be continuous -- no missing values allowed.
		 * 
		 * 
		 * INPUT : s[n] array of cast salinities t[n] array of cast in situ
		 * temperatures p[n] array of cast pressures gamma[n] array of cast gamma
		 * values n length of cast level neutral density value
		 * 
		 * Returns : sns salinity on the neutral density surfaces tns in situ
		 * temperature on the surfaces pns pressure on the surfaces dsns surface
		 * salinity errors dtns surface temperature errors dpns surface pressure
		 * errors
		 * 
		 * NOTE: sns, tns and pns values of JOAConstants.MISSINGVALUE denotes under
		 * or outcropping
		 * 
		 * non-zero dsns, dtns and dpns values indicates multiply defined surfaces,
		 * and file 'ns-multiples.dat' contains information on the multiple
		 * solutions
		 * 
		 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure
		 * db gamma kg m-3
		 */

		int[] intvl = new int[MAX_INTVLS];
		int i, k, k1, nintvls;
		int nlast;
		int middle;
		int mult;

		double alfa_l, alfa_u, alfa_mid, beta_l, beta_u, beta_mid;
		double thl, thu, delth, dels, pl, pu, delp, delp2;
		double gmin, gmax;
		double a, b, c, q;
		double pr0 = 0.0;
		double ptol = 1.0e-3;
		double rhomid, bmid, sigmid, smid, tmid, pmid;
		double sns_top = 0.0, tns_top = 0.0, pns_top = 0.0;
		double sns_middle = 0.0, tns_middle = 0.0, pns_middle = 0.0;
		double pns1, pns2;
		double bden, rg, plast;
		double[] swork, twork, pwork, gwork;

		// FILE *mfile;
		// mfile = (FILE *) NULL;
		double sns, tns, pns, dsns, dtns, dpns;

		swork = new double[n]; // (double *) calloc((size_t)n, sizeof(double));
		twork = new double[n]; // (double *) calloc((size_t)n, sizeof(double));
		pwork = new double[n]; // (double *) calloc((size_t)n, sizeof(double));
		gwork = new double[n]; // (double *) calloc((size_t)n, sizeof(double));
		double[] retVals = new double[6];

		// check for missing gammas and monotonic pressure series
		plast = -10.0;
		i = 0;
		for (k = 0; k < n; ++k) {
			// if (gamma[k] != JOAConstants.MISSINGVALUE) {
			if (p[k] > plast) {
				plast = p[k];
				swork[i] = s[k];
				twork[i] = t[k];
				pwork[i] = p[k];
				gwork[i] = gamma[k];
				++i;
			}
			// }
		}

		if (i <= 1) {
			sns = JOAConstants.MISSINGVALUE;
			tns = JOAConstants.MISSINGVALUE;
			pns = JOAConstants.MISSINGVALUE;
			dsns = 0.0;
			dtns = 0.0;
			dpns = 0.0;
		}

		if (i == 0) { // no gamma-n information
			sns = JOAConstants.MISSINGVALUE;
			tns = JOAConstants.MISSINGVALUE;
			pns = JOAConstants.MISSINGVALUE;
			dsns = 0.0;
			dtns = 0.0;
			dpns = 0.0;
			retVals[0] = sns;
			retVals[1] = tns;
			retVals[2] = pns;
			retVals[3] = dsns;
			retVals[4] = dtns;
			retVals[5] = dpns;
			return retVals;
		}

		n = i;
		nlast = n - 1;

		// loop for each neutral surface
		nintvls = 0;
		for (k = 0; k < nlast; ++k) {
			gmin = gwork[k];
			gmax = gwork[k];

			if (n > 1) {
				k1 = k + 1;
				gmin = Math.min(gwork[k], gwork[k1]);
				gmax = Math.max(gwork[k], gwork[k1]);
			}

			if (level >= gmin && level <= gmax) {
				if (nintvls == MAX_INTVLS) { throw new NeutralSurfaceErrorException(
				    "FATAL ERROR:  too many crossings in neutral_surfaces()"); }
				intvl[nintvls] = k;
				++nintvls;
			}
		} // end for k

		sns = JOAConstants.MISSINGVALUE;
		tns = JOAConstants.MISSINGVALUE;
		pns = JOAConstants.MISSINGVALUE;
		dsns = 0.0;
		dtns = 0.0;
		dpns = 0.0;

		// unusual case of an exact crossing with castlength of 1
		if ((n == 1) && (nintvls == 1)) {
			sns = swork[0];
			tns = twork[0];
			pns = pwork[0];
		}
		else if (n > 1 && nintvls > 0) {
			// more usual case: neutral surface exists, castlength > 1
			// if more than 1 interval, choose the median
			middle = (int)(nintvls * 0.5);
			// loop over all intersections
			for (i = 0; i < nintvls; ++i) {
				k = intvl[i];
				k1 = k + 1;

				// coefficients of a quadratic for gamma
				double[] d = eosall(swork[k], twork[k], pwork[k]);
				alfa_l = d[2];
				beta_l = d[3];
				d = eosall(swork[k1], twork[k1], pwork[k1]);
				alfa_u = d[2];
				beta_u = d[3];

				alfa_mid = (alfa_l + alfa_u) * 0.5;
				beta_mid = (beta_l + beta_u) * 0.5;
				pmid = (pwork[k] + pwork[k1]) * 0.5;

				try {
					d = stp_interp(swork, twork, pwork, n, pmid);
				}
				catch (NeutralSurfaceErrorException ex) {
					throw ex;
				}
				smid = d[0];
				tmid = d[1];

				sigmid = hb_svan(smid, tmid, pmid);
				rhomid = 1000.0 + sigmid;

				thl = theta(swork[k], twork[k], pwork[k], pr0);
				thu = theta(swork[k1], twork[k1], pwork[k1], pr0);
				delth = thu - thl;

				dels = swork[k1] - swork[k];

				pl = pwork[k];
				pu = pwork[k1];
				delp = pu - pl;
				delp2 = delp * delp;

				bden = rhomid * (beta_mid * dels - alfa_mid * delth);

				if (Math.abs(bden) < 1.0e-6) {
					bden = 1.0e-6;
				}

				bmid = (gwork[k1] - gwork[k]) / bden;

				// coefficients
				a = dels * (beta_u - beta_l) - delth * (alfa_u - alfa_l);
				a = (a * bmid * rhomid) / (2 * delp2);

				b = dels * (pu * beta_l - pl * beta_u) - delth * (pu * alfa_l - pl * alfa_u);
				b = (b * bmid * rhomid) / delp2;

				c = dels * (beta_l * (pl - 2. * pu) + beta_u * pl) - delth * (alfa_l * (pl - 2.0 * pu) + alfa_u * pl);

				c = gwork[k] + (bmid * rhomid * pl * c) / (2 * delp2);
				c -= level;

				/* solve the quadratic */
				if (a != 0.0 && bden > 1.0e-6) {
					mult = (b >= 0.0) ? 1 : -1;
					q = -(b + mult * Math.sqrt(b * b - 4 * a * c)) * 0.5;
					pns1 = q / a;
					pns2 = c / q;
					if (pns1 >= (pwork[k] - ptol) && pns1 <= (pwork[k1] + ptol)) {
						pns = Math.min(pwork[k1], (Math.max(pns1, pwork[k])));
					}
					else if (pns2 >= (pwork[k] - ptol) && pns2 <= (pwork[k1] + ptol)) {
						pns = Math.min(pwork[k1], (Math.max(pns2, pwork[k])));
					}
					else {
						pns = JOAConstants.MISSINGVALUE;
						System.out.println("Error 3 (quadratic sol'n) in neutral_surfaces()\n");
					}
				}
				else {
					rg = (level - gwork[k]) / (gwork[k1] - gwork[k]);
					pns = pwork[k] + rg * (pwork[k1] - pwork[k]);
				}

				if (pns >= 0.0) {
					d = stp_interp(swork, twork, pwork, n, pns);
					sns = d[0];
					tns = d[1];
				}

				// case of multiple intersections
				if (nintvls <= 1) {
					dsns = 0.0;
					dtns = 0.0;
					dpns = 0.0;
				}
				else {
					// write multiples to file
					/*
					 * if (mfile == NULL) mfile = fopen("ns-multiples.dat", "a"); if
					 * (mfile == NULL) { fprintf(stderr,"\nWARNING: unable to open
					 * ns-multiples.dat for writing\n"); } else { if (i == 0)
					 * fprintf(mfile, "\n level: [%8.4lf] # of intersections: [%d]",
					 * glevels, nintvls); fprintf(mfile,"\n%8.4lf %8.4lf %8.1lf ", sns,
					 * tns, pns); }
					 */

					// find median values and errors
					if (i == 0) {
						sns_top = sns;
						tns_top = tns;
						pns_top = pns;

					}
					else if (i == middle) {
						sns_middle = sns;
						tns_middle = tns;
						pns_middle = pns;
						if (nintvls == 2) {
							// this is also nintvls-1 so we must set these ...
							dsns = sns_middle - sns_top;
							dtns = tns_middle - tns_top;
							dpns = pns_middle - pns_top;
							// implicitly sns, tns, pns for this intvl will be returned
						}
					}
					else if (i == (nintvls - 1)) {
						if ((pns_middle - pns_top) > (pns - pns_middle)) {
							dsns = sns_middle - sns_top;
							dtns = tns_middle - tns_top;
							dpns = pns_middle - pns_top;
						}
						else {
							dsns = sns - sns_middle;
							dtns = tns - tns_middle;
							dpns = pns - pns_middle;
						}
						sns = sns_middle;
						tns = tns_middle;
						pns = pns_middle;
					}
				} // end else

			} // end for i

		} // end if - else if

		// if (mfile != NULL) {
		// fclose(mfile);
		// mfile = (FILE *) NULL;
		// }

		retVals[0] = sns;
		retVals[1] = tns;
		retVals[2] = pns;
		retVals[3] = dsns;
		retVals[4] = dtns;
		retVals[5] = dpns;
		return retVals;
	} // end neutral_surfaces()

	public static UVCoordinate[] gamma(boolean isBottle, Section sech, Station sh, double[] result, double[] lerrs,
	    double[] herrs) throws NeutralSurfaceErrorException {
		// variable blocks of each bottle.
		UVCoordinate[] retVals = new UVCoordinate[3];

		int pPos = sech.getPRESVarPos(); // index into variable block for pressure.
		int sPos = -1;
		int tPos = -1;
		int ctdSPos = sech.getVarPos("CTDS", true);
		tPos = sech.getVarPos("TEMP", true);

		if (isBottle) {
			if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.BOTTLE_SALINITY) {
				sPos = sech.getVarPos("SALT", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY) {
					sPos = sech.getVarPos("CTDS", true);
				}
			}
			else if (JOAConstants.DEFAULT_SALINITY_VARIABLE == JOAConstants.CTD_SALINITY) {
				sPos = sech.getVarPos("CTDS", true);

				if (sPos == -1 && JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY) {
					sPos = sech.getVarPos("SALT", true);
				}
			}
		}
		else {
			sPos = sech.getVarPos("SALT", true);
			if (sPos == -1) {
				sPos = sech.getVarPos("CTDS", true);
			}
		}

		int nBottles = sh.mNumBottles;
		double[] s = new double[nBottles];
		double[] t = new double[nBottles];
		double[] p = new double[nBottles];

		// test values and expected results
		double[] pTestVals = { 1.000, 48.000, 97.000, 145.000, 194.000, 291.000, 388.000, 485.000, 581.000, 678.000,
		    775.000, 872.000, 969.000, 1066.000, 1260.000, 1454.000, 1647.000, 1841.000, 2020.000, 2216.000, 2413.000,
		    2611.000, 2878.000, 3000.000 };
		double[] tTestVals = { 12.250, 12.210, 12.090, 11.990, 11.690, 10.540, 9.350, 8.360, 7.860, 7.430, 6.870, 6.040,
		    5.500, 4.900, 4.040, 3.290, 2.780, 2.450, 2.211, 2.011, 1.894, 1.788, 1.554, 1.380 };
		double[] sTestVals = { 35.066, 35.086, 35.089, 35.078, 35.025, 34.851, 34.696, 34.572, 34.531, 34.509, 34.496,
		    34.452, 34.458, 34.456, 34.488, 34.536, 34.579, 34.612, 34.642, 34.657, 34.685, 34.707, 34.720, 34.729 };

		/*
		 * Location: 187.317 -41.667 Labels: 1.0 26.657205 0.001347 0.001347 48.0
		 * 26.682834 0.001053 0.001053 97.0 26.710969 0.000795 0.000795 145.0
		 * 26.723245 0.000633 0.000633 194.0 26.741491 0.000609 0.000609 291.0
		 * 26.825459 0.000500 0.000500 388.0 26.918710 0.000500 0.000500 485.0
		 * 26.989777 0.000500 0.000500 581.0 27.039080 0.000500 0.000500 678.0
		 * 27.089155 0.000514 0.000514 775.0 27.166574 0.000500 0.000500 872.0
		 * 27.260381 0.000500 0.000500 969.0 27.343624 0.000500 0.000500 1066.0
		 * 27.421587 0.000500 0.000500 1260.0 27.557341 0.000500 0.000500 1454.0
		 * 27.698193 0.000500 0.000500 1647.0 27.798448 0.000500 0.000500 1841.0
		 * 27.866286 0.000500 0.000500 2020.0 27.920168 0.000500 0.000500 2216.0
		 * 27.959251 0.000500 0.000500 2413.0 27.997852 0.000500 0.000500 2611.0
		 * 28.031655 0.000500 0.000500 2878.0 28.079949 0.000511 0.000511 3000.0
		 * 28.117360 0.000504 0.000504
		 * 
		 * Surfaces: 26.80 34.906433 10.906537 260.097752 0.0 27.90 34.630799
		 * 2.300250 1953.166408 0.0 28.10 34.724831 1.460609 2943.487234 0.0
		 */

		double[] testResults = new double[24];
		double[] testLerrs = new double[24];
		double[] testHerrs = new double[24];

		// calculate the gammas with errors
		// try {
		// gamma_n("test", sTestVals, tTestVals, pTestVals, 24, 187.317, -41.667,
		// testResults, testLerrs, testHerrs);
		// }
		// catch (NeutralSurfaceErrorException ex) {
		// throw ex;
		// }

		for (int i = 0; i < nBottles; i++) {
			Bottle bh = (Bottle)sh.mBottles.elementAt(i);
			// Initialize results array with missing values.
			result[i] = JOAConstants.MISSINGVALUE;
			lerrs[i] = JOAConstants.MISSINGVALUE;
			herrs[i] = JOAConstants.MISSINGVALUE;
			// Initialize data arrays
			s[i] = bh.mDValues[sPos];
			if (s[i] == JOAConstants.MISSINGVALUE && isBottle) {
				if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.CTD_SALINITY && ctdSPos >= 0) {
					s[i] = bh.mDValues[ctdSPos];
				}
				else if (s[i] == JOAConstants.MISSINGVALUE && !isBottle) {
					if (JOAConstants.DEFAULT_SALINITY_SUBSTITUTION == JOAConstants.BOTTLE_SALINITY && sPos >= 0) {
						s[i] = bh.mDValues[sPos];
					}
				}
			}

			t[i] = bh.mDValues[tPos];
			p[i] = bh.mDValues[pPos];
		}

		// calculate the gammas with errors
		try {
			gamma_n(sh.getStn(), s, t, p, nBottles, sh.getLon(), sh.getLat(), result, lerrs, herrs);
		}
		catch (NeutralSurfaceErrorException ex) {
			throw ex;
		}

		double max = result[0]; // Scan for largest and smallest values.
		double min = max;
		for (int i = 0; i < nBottles; i++) {
			if (result[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			max = result[i] > max ? result[i] : max;
			min = result[i] < min ? result[i] : min;
		}
		retVals[0] = new UVCoordinate(min, max);

		max = lerrs[0];
		min = max;
		for (int i = 0; i < nBottles; i++) {
			if (lerrs[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			max = lerrs[i] > max ? lerrs[i] : max;
			min = lerrs[i] < min ? lerrs[i] : min;
		}
		retVals[1] = new UVCoordinate(min, max);

		max = herrs[0];
		min = max;
		for (int i = 0; i < nBottles; i++) {
			if (herrs[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			max = herrs[i] > max ? herrs[i] : max;
			min = herrs[i] < min ? herrs[i] : min;
		}
		retVals[1] = new UVCoordinate(min, max);

		return retVals;
	}

	public static double hb_svan(double s, double t, double p0) {
		/*
		 * ............................................................... .
		 * Specific Volume Anomaly (steric anomaly) based on 1980 equation . of
		 * state for seawater and 1978 practical salinity scale. . References: .
		 * Millero, et al (1980) deep-sea res.,27a,255-264 . Millero and Poisson
		 * 1981,deep-sea res.,28a pp 625-629. . (both references are also found in
		 * unesco report 38 -- 1981) . . The type of density anomaly (sigma)
		 * computed depends on the type of . temperature and pressure supplied: . .
		 * in situ density: t is in situ temperature . p0 is in situ pressure . .
		 * potential density: p0 is reference pressure . t is temperature referenced
		 * already to p0 . . sigma-t: : p0 = 0 . t is in situ temperature .
		 * ................................................................ . units:
		 * . p0 : pressure [or ref pressure] (decibars) . t : temperature [or pot
		 * temp] (deg C) . s : salinity (ipss-78) . svan : spec. vol. anom. (m**3/kg
		 * *1.0e-8) . sigma : density anomaly (kg/m**3) .
		 * ................................................................ . check
		 * value: svan=981.3021 e-8 m**3/kg. for s = 40 (ipss-78) , . t = 40 deg c,
		 * p0= 10000 decibars. . check value: sigma = 59.82037 kg/m**3 for s = 40
		 * (ipss-78) , . t = 40 deg c, p0= 10000 decibars. .
		 * ...............................................................
		 */
		double p, sig, sr, r1, r2, r3;
		double a, b, c, d, e, a1, b1, aw, bw, k0, kw, k35;
		double sva, v350p, dk, gam, pk, dr35p, dvan;
		double sigma_ptr;

		/*
		 * .............................................................. . convert
		 * pressure to bars and take square root of salinity .
		 * ..............................................................
		 */
		p = p0 / 10.;
		sr = Math.sqrt(Math.abs(s));

		/*
		 * .............................................................. . pure
		 * water density at atmospheric pressure . bigg p.h.,(1967) br. j. applied
		 * physics 8 pp 521-537 .
		 * ..............................................................
		 */
		r1 = ((((6.536332e-9 * t - 1.120083e-6) * t + 1.001685e-4) * t - 9.095290e-3) * t + 6.793952e-2) * t - 28.263737;

		/*
		 * .............................................................. . seawater
		 * density at atmospheric press. . coefficients involving salinity: . r2 = A
		 * in notation of Millero and Poisson 1981 . r3 = B .
		 * ..............................................................
		 */
		r2 = (((5.3875e-9 * t - 8.2467e-7) * t + 7.6438e-5) * t - 4.0899e-3) * t + 8.24493e-1;
		r3 = (-1.6546e-6 * t + 1.0227e-4) * t - 5.72466e-3;

		/*
		 * .............................................................. .
		 * international one-atmosphere equation of state of seawater .
		 * ..............................................................
		 */
		sig = (R4 * s + r3 * sr + r2) * s + r1;

		/*
		 * .............................................................. . specific
		 * volume at atmospheric pressure .
		 * ..............................................................
		 */
		v350p = 1.0 / R3500;
		sva = -sig * v350p / (R3500 + sig);
		sigma_ptr = sig + DR350;

		/*
		 * .............................................................. . scale
		 * specific vol. anomaly to normally reported units .
		 * ..............................................................
		 */
		if (p == 0.) { return sigma_ptr; }

		/*
		 * .............................................................. . high
		 * pressure equation of state for seawater . Millero, et al , 1980 dsr 27a,
		 * pp 255-264 . constant notation follows article . . compute compression
		 * terms ... .
		 * ..............................................................
		 */
		e = (9.1697e-10 * t + 2.0816e-8) * t - 9.9348e-7;
		bw = (5.2787e-8 * t - 6.12293e-6) * t + 3.47718e-5;
		b = bw + e * s;

		d = 1.91075e-4;
		c = (-1.6078e-6 * t - 1.0981e-5) * t + 2.2838e-3;
		aw = ((-5.77905e-7 * t + 1.16092e-4) * t + 1.43713e-3) * t - 0.1194975;
		a = (d * sr + c) * s + aw;

		b1 = (-5.3009e-4 * t + 1.6483e-2) * t + 7.944e-2;
		a1 = ((-6.1670e-5 * t + 1.09987e-2) * t - 0.603459) * t + 54.6746;
		kw = (((-5.155288e-5 * t + 1.360477e-2) * t - 2.327105) * t + 148.4206) * t - 1930.06;
		k0 = (b1 * sr + a1) * s + kw;

		/*
		 * .............................................................. . evaluate
		 * pressure polynomial . . k equals the secant bulk modulus of seawater . dk
		 * = k(s,t,p) - k(35,0,p) . k35 = k(35,0,p) .
		 * ..............................................................
		 */
		dk = (b * p + a) * p + k0;
		k35 = (5.03217e-5 * p + 3.359406) * p + 21582.27;
		gam = p / k35;
		pk = 1.0 - gam;
		sva = sva * pk + (v350p + sva) * p * dk / (k35 * (k35 + dk));

		/*
		 * .............................................................. . scale
		 * specific vol. anamoly to normally reported units... .
		 * ..............................................................
		 */
		v350p = v350p * pk;

		/*
		 * .............................................................. . compute
		 * density anomaly with respect to 1000.0 kg/m**3 . 1) DR350: density
		 * anomaly at 35 (ipss-78), 0 deg. c and 0 decibars . 2) dr35p: density
		 * anomaly 35 (ipss-78), 0 deg. c , pres. variation . 3) dvan : density
		 * anomaly variations involving specfic vol. anamoly .
		 * ..............................................................
		 */

		dr35p = gam / v350p;
		dvan = sva / (v350p * (v350p + sva));
		sigma_ptr = DR350 + dr35p - dvan;

		return sigma_ptr;
	}

	// public static double[] neutral_surfaces(double[]s, double[]t, double[]p,
	// double[] d, double[] gamma, int n, double level) {
	/*
	 * DESCRIPTION : For a cast of hydrographic data which has been labelled with
	 * the neutral density variable gamma, find the salinities, temperatures and
	 * pressures on ng specified neutral density surfaces. Cast must be continuous
	 * -- no missing values allowed.
	 * 
	 * 
	 * INPUT : d[n] array of input parameter values gamma[n] array of cast gamma
	 * values n length of cast level neutral density value
	 * 
	 * Returns : dns parameter on the neutral density surfaces ddns surface
	 * parameter errors
	 * 
	 * NOTE: dns values of JOAConstants.MISSINGVALUE denotes under or outcropping
	 * 
	 * non-zero dsns, dtns and dpns values indicates multiply defined surfaces,
	 * and file 'ns-multiples.dat' contains information on the multiple solutions
	 * 
	 * UNITS : salinity psu (IPSS-78) temperature degrees C (IPTS-68) pressure db
	 * gamma kg m-3
	 */

	/*
	 * int[] intvl = new int[MAX_INTVLS]; int i, ii, ig, k, k1, nintvls; int
	 * nlast, n2 = 2; int halfcastlen, middle; int mult;
	 * 
	 * double alfa_l, alfa_u, alfa_mid, beta_l, beta_u, beta_mid; double thl, thu,
	 * delth, dels, pl, pu, delp, delp2; double gmin, gmax; double a, b, c, q;
	 * double pr0 = 0.0; double ptol = 1.0e-3; double rhomid, bmid, sigmid, smid,
	 * tmid, pmid; double sns_top = 0.0, tns_top = 0.0, pns_top = 0.0; double
	 * sns_middle = 0.0, tns_middle = 0.0, pns_middle = 0.0; double pns1, pns2;
	 * double bden, rg, plast; double[] dwork, gwork; double dns, ddns;
	 * 
	 * swork = new double[n];//(double *) calloc((size_t)n, sizeof(double)); gwork
	 * = new double[n];//(double *) calloc((size_t)n, sizeof(double)); double[]
	 * retVals = new double[2]; // check for missing gammas and monotonic pressure
	 * series plast = -10.0; i = 0; for (k = 0; k < n; ++k) { if (gamma[k] > 0.0)
	 * { if (p[k] > plast) { plast = p[k]; dwork[i] = s[k]; gwork[i] = gamma[k];
	 * ++i; } } }
	 * 
	 * if (i <= 1) { dns = JOAConstants.MISSINGVALUE; ddns = 0.0; }
	 * 
	 * if (i == 0) { // no gamma-n information dns = JOAConstants.MISSINGVALUE;
	 * ddns = 0.0; retVals[0] = dns; retVals[1] = ddns; return retVals; }
	 * 
	 * n = i; nlast = n - 1; halfcastlen = (int)(n * 0.5); // loop for each
	 * neutral surface nintvls = 0; for (k = 0; k < nlast; ++k) { gmin = gwork[k];
	 * gmax = gwork[k];
	 * 
	 * if (n > 1) { k1 = k + 1; gmin = Math.min(gwork[k], gwork[k1]); gmax =
	 * Math.max(gwork[k], gwork[k1]); }
	 * 
	 * if (level >= gmin && level <= gmax) { if (nintvls == MAX_INTVLS) {
	 * System.out.println("\nFATAL ERROR: too many crossings in
	 * neutral_surfaces()\n"); //exit(1); } intvl[nintvls] = k; ++nintvls; } } //
	 * end for k
	 * 
	 * dns = JOAConstants.MISSINGVALUE; ddns = 0.0; // unusual case of an exact
	 * crossing with castlength of 1 if ((n == 1) && (nintvls == 1)) { dns =
	 * dwork[0]; } else if (n > 1 && nintvls > 0) { // more usual case: neutral
	 * surface exists, castlength > 1 // if more than 1 interval, choose the
	 * median middle = (int)(nintvls * 0.5); // loop over all intersections for (i
	 * = 0; i < nintvls; ++i) { k = intvl[i]; k1 = k + 1; // coefficients of a
	 * quadratic for gamma double[] d = eosall(s[k], t[k], p[k]); alfa_l = d[2];
	 * beta_l = d[3]; d = eosall(s[k1], t[k1], p[k1]); alfa_u = d[2]; beta_u =
	 * d[3];
	 * 
	 * alfa_mid = (alfa_l + alfa_u) * 0.5; beta_mid = (beta_l + beta_u) * 0.5;
	 * pmid = (p[k] + p[k1]) * 0.5;
	 * 
	 * d = stp_interp(s, t, p, n2, pmid); smid = d[0]; tmid = d[1];
	 * 
	 * sigmid = hb_svan(smid, tmid, pmid); rhomid = 1000.0 + sigmid;
	 * 
	 * thl = theta(s[k], t[k], p[k], pr0); thu = theta(s[k1], t[k1], p[k1], pr0);
	 * delth = thu - thl;
	 * 
	 * dels = s[k1] - s[k];
	 * 
	 * pl = p[k]; pu = p[k1]; delp = pu - pl; delp2 = delp * delp;
	 * 
	 * bden = rhomid * (beta_mid * dels - alfa_mid * delth);
	 * 
	 * if (Math.abs(bden) < 1.0e-6) bden = 1.0e-6;
	 * 
	 * bmid = (s[k1] - s[k]) / bden; // coefficients a = dels * (beta_u-beta_l) -
	 * delth * (alfa_u-alfa_l); a = (a * bmid * rhomid) / (2 * delp2);
	 * 
	 * b = dels * (pu*beta_l - pl*beta_u) - delth * (pu*alfa_l - pl*alfa_u); b =
	 * (b * bmid * rhomid) / delp2;
	 * 
	 * c = dels * (beta_l * (pl - 2.* pu) + beta_u * pl) - delth * (alfa_l * (pl -
	 * 2.0 * pu) + alfa_u * pl);
	 * 
	 * c = gwork[k] + (bmid * rhomid * pl * c)/ (2*delp2); c -= level; // solve
	 * the quadratic if (a != 0.0 && bden > 1.0e-6) { mult = (b >= 0.0) ? 1 : -1;
	 * q = -(b + mult * Math.sqrt(b*b - 4*a*c)) * 0.5; pns1 = q / a; pns2 = c / q;
	 * if (pns1 >= (p[k]-ptol) && pns1 <= (p[k1]+ptol)) pns = Math.min(p[k1],
	 * (Math.max(pns1, p[k]))); else if (pns2 >= (p[k]-ptol) && pns2 <=
	 * (p[k1]+ptol)) pns = Math.min(p[k1], (Math.max(pns2, p[k]))); else { pns =
	 * JOAConstants.MISSINGVALUE; System.out.println("Error 3 (quadratic sol'n) in
	 * neutral_surfaces()\n"); } } else { rg = (level - gwork[k]) / (gwork[k1] -
	 * gwork[k]); pns = p[k] + rg *(p[k1] - p[k]); }
	 * 
	 * if (pns >= 0.0) { d = stp_interp(s, t, p, n, pns); sns = d[0]; tns = d[1];
	 * } // case of multiple intersections if (nintvls <= 1) { dsns = 0.0; dtns =
	 * 0.0; dpns = 0.0; } else { // find median values and errors if (i == 0) {
	 * sns_top = sns; tns_top = tns; pns_top = pns; } else if (i == middle) {
	 * sns_middle = sns; tns_middle = tns; pns_middle = pns; if (nintvls == 2) {
	 * // this is also nintvls-1 so we must set these ... dsns = sns_middle -
	 * sns_top; dtns = tns_middle - tns_top; dpns = pns_middle - pns_top; //
	 * implicitly sns, tns, pns for this intvl will be returned } } else if (i ==
	 * (nintvls-1)) { if((pns_middle-pns_top) > (pns-pns_middle)) { dsns =
	 * sns_middle - sns_top; dtns = tns_middle - tns_top; dpns = pns_middle -
	 * pns_top; } else { dsns = sns - sns_middle; dtns = tns - tns_middle; dpns =
	 * pns - pns_middle; } sns = sns_middle; tns = tns_middle; pns = pns_middle; }
	 * } // end else } // end for i } // end if - else if
	 * 
	 * retVals[0] = sns; retVals[1] = tns; retVals[2] = pns; retVals[3] = dsns;
	 * retVals[4] = dtns; retVals[5] = dpns; return retVals; } // end
	 * neutral_surfaces()
	 */

	public static Calculation createCalcFromName(FileViewer mFileViewer, String calcName) {
		Double dArg = null;
		// create the calculation objects
		if (calcName.equalsIgnoreCase("THTA")) {
			dArg = new Double(0.0);
			Calculation calc = new Calculation("THTA", dArg, JOAFormulas.paramNameToJOAUnits(false, "THTA"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SIG0")) {
			dArg = new Double(0.0);
			Calculation calc = new Calculation("SIG0", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG0"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SIG1")) {
			dArg = new Double(1000.0);
			Calculation calc = new Calculation("SIG1", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG1"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SIG2")) {
			dArg = new Double(2000.0);
			Calculation calc = new Calculation("SIG2", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG2"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SIG3")) {
			dArg = new Double(3000.0);
			Calculation calc = new Calculation("SIG3", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG3"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SIG4")) {
			dArg = new Double(4000.0);
			Calculation calc = new Calculation("SIG4", dArg, JOAFormulas.paramNameToJOAUnits(false, "SIG4"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SPCY")) {
			Calculation calc = new Calculation("SPCY", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "SPCY"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SVAN")) {
			Calculation calc = new Calculation("SVAN", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "SVAN"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("SVEL")) {
			Calculation calc = new Calculation("SVEL", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "SVEL"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("O2% ")) {
			Calculation calc = new Calculation("O2% ", JOAConstants.OBS_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "SVEL"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		Boolean boolArg = new Boolean(true);
		if (calcName.equalsIgnoreCase("AOU")) {
			Calculation calc = new Calculation("AOU", boolArg, JOAFormulas.paramNameToJOAUnits(false, "AOU"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("NO")) {
			Calculation calc = new Calculation("NO", boolArg, JOAFormulas.paramNameToJOAUnits(false, "NO"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("PO")) {
			Calculation calc = new Calculation("PO", boolArg, JOAFormulas.paramNameToJOAUnits(false, "PO"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("HTST")) {
			Calculation calc = new Calculation("HTST", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "HTST"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("ALPH")) {
			Calculation calc = new Calculation("ALPH", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "ALPH"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("BETA")) {
			Calculation calc = new Calculation("BETA", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "BETA"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("ADRV")) {
			Calculation calc = new Calculation("ADRV", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "ADRV"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("BDRV")) {
			Calculation calc = new Calculation("BDRV", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "BDRV"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("GPOT")) {
			Calculation calc = new Calculation("GPOT", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "GPOT"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("PE")) {
			Calculation calc = new Calculation("PE", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false, "PE"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("HEAT")) {
			Calculation calc = new Calculation("HEAT", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "HEA"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("ACTT")) {
			Calculation calc = new Calculation("ACTT", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "ACTT"));
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		if (calcName.equalsIgnoreCase("GAMMA")) {
			Calculation calc = new Calculation("GAMMA", JOAConstants.INT_CALC_TYPE, JOAFormulas.paramNameToJOAUnits(false,
			    "GAMMA"));
			calc.setIncludeErrorTerms(false);
			try {
				calc.writeToLog("New Calculation (" + mFileViewer.getTitle() + "):");
			}
			catch (Exception ex) {
			}
			return calc;
		}
		return null;
	}

	public static boolean isCalculatable(String calcName) {
		for (int i = 0; i < JOAConstants.paramNames.length; i++) {
			if (calcName.equalsIgnoreCase(JOAConstants.paramNames[i])) { return true; }
		}
		return false;
	}

	private static class PalleteNotifyStr extends HandlerBase {
		public void startDocument() throws SAXException {
			C = 0;
			CVALS = new Color[256];
		}

		public void startElement(String name, AttributeList amap) throws SAXException {
			if (name.equals("color")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							RED = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							RED = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							GREEN = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							GREEN = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							BLUE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							BLUE = 150;
						}
					}
				}
			}
		}

		public void characters(char[] ch, int start, int len) throws SAXException {
		}

		public void endElement(String name) throws SAXException {
			if (name.equals("color")) {
				// build a new color value
				CVALS[C++] = new Color(RED, GREEN, BLUE);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static ColorPalette readPalette(String palletName) throws Exception {
		try {
			Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
			org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
			PalleteNotifyStr notifyStr = new PalleteNotifyStr();
			parser.setDocumentHandler(notifyStr);
			File file = JOAFormulas.getSupportFile(palletName);
			parser.parse(JOAFormulas.getSupportPath() + file.getName());
		}
		catch (Exception ex) {

		}

		// build the color palatte
		ColorPalette cp = new ColorPalette();
		for (int i = 0; i < 256; i++) {
			cp.setColor(i, CVALS[i]);
		}
		return cp;
	}

	public static int rotatePixelX(double x, double angle) {
		return (int)(x * Math.cos(angle) - x * Math.sin(angle));
	}

	public static int rotatePixelY(double y, double angle) {
		return (int)(y * Math.cos(angle) - y * Math.sin(angle));
	}

	public static double getAngle(double xl, double xr, double yl, double yr) {
		// compute hypotenuse
		double hyp = Math.sqrt(((xr - xl) * (xr - xl)) + ((yr - yl) * (yr - yl)));
		double opp = yr - yl;

		double rangle = Math.asin(opp / hyp);
		return (rangle * (180.0 / Math.PI));
	}

	public static int[] getSelIndices(String[] strs, int strLen, JList jlist) {
		int cnt = 0;
		int[] fndLines = new int[100];
		for (int i = 0; i < jlist.getModel().getSize(); i++) {
			String ls = (String)jlist.getModel().getElementAt(i);
			for (int j = 0; j < strLen; j++) {
				String ss = strs[j];
				if (strs[j] != null) {
					if (ss.equalsIgnoreCase(ls)) {
						fndLines[cnt++] = i;
						continue;
					}
				}
			}
		}

		int[] retArray = new int[cnt];
		for (int i = 0; i < cnt; i++) {
			retArray[i] = fndLines[i];
		}
		return retArray;
	}

	public static double calcMax(double[] inVals) {
		double max = -999999.0;
		int numValid = 0;
		for (int i = 0; i < inVals.length; i++) {
			if (inVals[i] == JOAConstants.MISSINGVALUE) {
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
			return JOAConstants.MISSINGVALUE;
		}
	}

	public static double calcMin(double[] inVals) {
		double min = 9999999999.0;
		int numValid = 0;
		for (int i = 0; i < inVals.length; i++) {
			if (inVals[i] == JOAConstants.MISSINGVALUE) {
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
			return JOAConstants.MISSINGVALUE;
		}
	}

	public static double calcMean(double[] inVals) {
		double sum = 0.0;
		int numValid = 0;
		for (int i = 0; i < inVals.length; i++) {
			if (inVals[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}

			sum += inVals[i];
			numValid++;
		}
		if (numValid > 0) {
			return sum / (double)numValid;
		}
		else {
			return JOAConstants.MISSINGVALUE;
		}
	}

	public static int calcN(double[] inVals) {
		int numValid = 0;
		for (int i = 0; i < inVals.length; i++) {
			if (inVals[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}

			numValid++;
		}
		return numValid;
	}

	public static double calcDepthOfMin(double[] depthVals, double[] inVals) {
		double min = 9999999999.0;
		double minDepth = JOAConstants.MISSINGVALUE;
		int numValid = 0;
		for (int i = 0; i < inVals.length; i++) {
			if (inVals[i] == JOAConstants.MISSINGVALUE) {
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
		double maxDepth = JOAConstants.MISSINGVALUE;
		int numValid = 0;
		for (int i = 0; i < inVals.length; i++) {
			if (inVals[i] == JOAConstants.MISSINGVALUE) {
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
		return JOAConstants.MISSINGVALUE;
	}

	public static double calcMinDepthOfNonMissingValue(double[] depthVals, double[] inVals) {
		return JOAConstants.MISSINGVALUE;
	}

	public static boolean isSameColor(Color c1, Color c2) {
		if (c1.getRGB() != c2.getRGB()) { return false; }
		return true;
	}

	public static Color getQCColor(Section sech, Bottle bh) {
		if (sech.getQCStandard() == JOAConstants.IGOSS_QC_STD) {
			if (bh.mQualityFlag == JOAConstants.MISSINGVALUE || bh.mQualityFlag == 0 || bh.mQualityFlag == 9) {
				return Color.gray.darker();
			}
			else if (bh.mQualityFlag == 1) {
				return Color.green.darker();
			}
			else if (bh.mQualityFlag == 3 || bh.mQualityFlag == 4) { return Color.red.darker(); }
			return Color.gray;
		}
		else if (sech.getQCStandard() == JOAConstants.WOCE_QC_STD) {
			if (bh.mQualityFlag == JOAConstants.MISSINGVALUE || bh.mQualityFlag == 1 || bh.mQualityFlag == 5
			    || bh.mQualityFlag == 9) {
				return Color.gray.darker();
			}
			else if (bh.mQualityFlag == 2) {
				return Color.green.darker();
			}
			else if (bh.mQualityFlag == 3 || bh.mQualityFlag == 4 || bh.mQualityFlag == 6 || bh.mQualityFlag == 7
			    || bh.mQualityFlag == 8) { return Color.red.darker(); }
			return Color.gray;
		}
		return Color.gray.darker();
	}

	public static Color getQCColor(short qc, short qcStd) {
		if (qcStd == JOAConstants.IGOSS_QC_STD) {
			if (qc == JOAConstants.MISSINGVALUE || qc == 0 || qc == 9) {
				return Color.gray.darker();
			}
			else if (qc == 1) {
				return Color.green.darker();
			}
			else if (qc == 3 || qc == 4) { return Color.red.darker(); }
			return Color.gray;
		}
		else if (qcStd == JOAConstants.WOCE_QC_STD) {
			if (qc == JOAConstants.MISSINGVALUE || qc == 1 || qc == 5 || qc == 9) {
				return Color.gray.darker();
			}
			else if (qc == 2) {
				return Color.green.darker();
			}
			else if (qc == 3 || qc == 4 || qc == 6 || qc == 7 || qc == 8) { return Color.red.darker(); }
			return Color.gray;
		}
		return Color.gray.darker();
	}

	public static boolean testFeatureGroup(String testID) {
		if (JOAConstants.JOA_FEATURESET.get(testID) != null) { return JOAConstants.JOA_FEATURESET.get(testID).isEnabled(); }
		return true;
	}

	public static boolean testFeature(String featureID) {
		for (FeatureGroup fg : JOAConstants.JOA_FEATURESET.values()) {
			if (fg.hasFeature(featureID)) { return fg.isFeatureEnabled(featureID); }
		}
		return true;
	}

	public static double dereferenceStation(int numLevels, double refLevel, double[] surfaceValues, double[] measuredVals) {
		if (refLevel == JOAConstants.MISSINGVALUE) { return JOAConstants.MISSINGVALUE; }

		// compute dereferenced interpolation
		int row1 = 0, row2 = 0, iSame = 0;
		double datum, delta1 = 0.0, delta2 = 0.0;
		double part1, part2, theRef, theRef1, theRef2;
		boolean flag1, flag2;

		// Test for refLevel matching an interpolation surface.
		flag1 = false;
		for (int i = 0; i < numLevels; i++) {
			if (surfaceValues[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			flag1 = (refLevel - surfaceValues[i]) == 0;
			if (flag1) {
				iSame = i;
				break;
			}
		}
		flag2 = false;
		for (int i = 0; i < numLevels - 1; i++) {
			if (surfaceValues[i] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			if (surfaceValues[i + 1] == JOAConstants.MISSINGVALUE) {
				continue;
			}
			delta1 = refLevel - surfaceValues[i];
			delta2 = refLevel - surfaceValues[i + 1];
			flag2 = ((delta1 > 0) ^ (delta2 > 0));
			if (flag2) {
				row1 = i;
				row2 = i + 1;
				break;
			}
		}

		if (!flag2 && !flag1) {
			for (int i = 0; i < numLevels; i++) {
				measuredVals[i] = JOAConstants.MISSINGVALUE;
			}
			return JOAConstants.MISSINGVALUE;
		}

		if (flag1) {
			flag2 = false;
			theRef = measuredVals[iSame];
			for (int i = 0; i < numLevels; i++) {
				datum = measuredVals[i];
				if (datum == JOAConstants.MISSINGVALUE) {
					continue;
				}
				datum = (theRef == JOAConstants.MISSINGVALUE) ? theRef : datum - theRef;
				measuredVals[i] = datum;
			}
			return theRef;
		}

		if (flag2) {
			delta1 = Math.abs(delta1);
			delta2 = Math.abs(delta2);
			part1 = delta2 / (delta1 + delta2);
			part2 = delta1 / (delta1 + delta2);
			theRef1 = measuredVals[row1];
			theRef2 = measuredVals[row2];
			theRef = part1 * theRef1 + part2 * theRef2;
			theRef = (theRef1 == JOAConstants.MISSINGVALUE) ? JOAConstants.MISSINGVALUE : theRef;
			theRef = (theRef2 == JOAConstants.MISSINGVALUE) ? JOAConstants.MISSINGVALUE : theRef;
			for (int i = 0; i < numLevels; i++) {
				datum = measuredVals[i];
				if (datum == JOAConstants.MISSINGVALUE) {
					continue;
				}
				datum = (theRef == JOAConstants.MISSINGVALUE) ? theRef : datum - theRef;
				measuredVals[i] = datum;
			}
			return theRef;
		}
		return JOAConstants.MISSINGVALUE;
	}

	public static String roundNDecimals(double d, int n) {
		String fmt = "0.";
		for (int i = 0; i < n + 1; i++) {
			fmt += "0";
		}
		DecimalFormat twoDForm = new DecimalFormat(fmt);
		String roundedNum = twoDForm.format(d);
		return formatDouble(roundedNum, n, false);
	}

	private static class MapNotifyStr extends HandlerBase {
		public void startDocument() throws SAXException {
			DRAWGRAT = false;
			PLOTSTNLABELS = false;
			PLOTGRATLABELS = true;
			RETAINASPECT = false;
			CONNECTSTNS = false;
			CONNECTSTNSACROSS = false;
			SECTIONLABELS = true;
			ETOPOOVL = false;
			ISOBATHCTR = 0;
			PROJCODE = -99;
			ISGLOBE = false;
			ETOPOFILENAMES = new String[10];
			eCnt = 0;
			PARAM = null;
			TITLE = null;
			DESCRIP = null;
		}

		public void startElement(String name, AttributeList amap) throws SAXException {
			if (name.equals("joamap")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("mapbgcolor")) {
						MAPBGCOLOR = JOAFormulas.getColorFromString(amap.getValue(i));
					}
				}
			}
			else if (name.equals("metadatatype")) {
				KEY_STATE = METADATATYPE_KEY;
			}
			else if (name.equals("mapregion")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("customregion")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							CUSTOMRGN = true;
						}
					}
					else if (amap.getName(i).equals("basin")) {
						try {
							BASIN = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							BASIN = 0;
						}
					}
					else if (amap.getName(i).equals("minlat")) {
						try {
							MINLAT = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							MINLAT = -90.0;
						}
					}
					else if (amap.getName(i).equals("maxlat")) {
						try {
							MAXLAT = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							MAXLAT = 90.0;
						}
					}
					else if (amap.getName(i).equals("lonleft")) {
						try {
							LONLFT = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							LONLFT = 20.0;
						}
					}
					else if (amap.getName(i).equals("lonright")) {
						try {
							LONRHT = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							LONRHT = 19.99;
						}
					}
					else if (amap.getName(i).equals("centerlat")) {
						try {
							CTRLAT = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							CTRLAT = 0.0;
						}
					}
					else if (amap.getName(i).equals("centerlon")) {
						try {
							CTRLON = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							CTRLON = 0.0;
						}
					}
				}
			}
			else if (name.equals("graticule")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("drawgraticule")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							DRAWGRAT = true;
						}
					}
					else if (amap.getName(i).equals("labelgraticules")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							PLOTGRATLABELS = true;
						}
						else {
							PLOTGRATLABELS = false;
						}
					}
					else if (amap.getName(i).equals("latspc")) {
						try {
							LATSPC = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							LATSPC = 10.0;
						}
					}
					else if (amap.getName(i).equals("lonspc")) {
						try {
							LONSPC = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							LONSPC = 30.0;
						}
					}
					else if (amap.getName(i).equals("gratcolor")) {
						GRATCOLOR = JOAFormulas.getColorFromString(amap.getValue(i));
					}
				}
			}
			else if (name.equals("projection")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("retainprojaspect")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							RETAINASPECT = true;
						}
					}
					else if (amap.getName(i).equals("projcode")) {
						try {
							PROJCODE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							PROJCODE = 0;
						}
					}
					if (amap.getName(i).equals("isglobe")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							ISGLOBE = true;
						}
						else {
							ISGLOBE = false;
						}
					}
				}
			}
			else if (name.equals("coastline")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("resolution")) {
						try {
							REZCODE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							REZCODE = 0;
						}
					}
					else if (amap.getName(i).equals("coastcolor")) {
						COASTCOLOR = JOAFormulas.getColorFromString(amap.getValue(i));
					}
					else if (amap.getName(i).equals("customcoastpath")) {
						CUSTOMCOASTPATH = new String(amap.getValue(i));
					}
					else if (amap.getName(i).equals("customcoastdescrip")) {
						CUSTOMCOASTDESCRIP = new String(amap.getValue(i));
					}
				}
			}
			else if (name.equals("overlaycontour")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("contourlabelprecision")) {
						try {
							CONTOURPREC = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							CONTOURPREC = 0;
						}
					}
				}
			}
			else if (name.equals("sectionline")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("plotsymbols")) {
						try {
							PLOTSYMBOLS = Boolean.valueOf(amap.getValue(i)).booleanValue();
						}
						catch (Exception ex) {
							PLOTSYMBOLS = true;
						}
					}
					if (amap.getName(i).equals("symbolcode")) {
						try {
							SYMBOLCODE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							SYMBOLCODE = 0;
						}
					}
					if (amap.getName(i).equals("symbolsize")) {
						try {
							SYMBOLSIZE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							SYMBOLSIZE = 3;
						}
					}
					if (amap.getName(i).equals("linewidth")) {
						try {
							LINEWIDTH = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							LINEWIDTH = 1;
						}
					}
					if (amap.getName(i).equals("connectstns")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							CONNECTSTNS = true;
						}
					}
					if (amap.getName(i).equals("connectstnsacross")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							CONNECTSTNSACROSS = true;
						}
					}
					if (amap.getName(i).equals("sectionlabels")) {
						if (amap.getValue(i).equalsIgnoreCase("false")) {
							SECTIONLABELS = false;
						}
					}
					else if (amap.getName(i).equals("stationlabels")) {
						if (amap.getValue(i).equalsIgnoreCase("true")) {
							PLOTSTNLABELS = true;
						}
						else {
							PLOTSTNLABELS = false;
						}
					}
					if (amap.getName(i).equals("stnlabeloffset")) {
						try {
							LABELOFFSET = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							LABELOFFSET = 5;
						}
					}
					else if (amap.getName(i).equals("stnlabelangle")) {
						try {
							LABELANGLE = Double.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							LABELANGLE = 45;
						}
					}
				}
			}
			else if (name.equals("etopoovl")) {
				ETOPOOVL = true;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("colorbarname")) {
						ETOPOCOLORBAR = new String(amap.getValue(i));
					}
				}
			}
			else if (name.equals("etopofile")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("etopofilename")) {
						ETOPOFILENAMES[eCnt++] = new String(amap.getValue(i));
					}
				}
			}
			else if (name.equals("isobath")) {
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("value")) {
						try {
							IOSBATHVALS[ISOBATHCTR] = Double.valueOf(amap.getValue(i)).doubleValue();
						}
						catch (Exception ex) {
							IOSBATHVALS[ISOBATHCTR] = -99.0;
						}
					}
					else if (amap.getName(i).equals("color")) {
						ISOBATHCOLORS[ISOBATHCTR] = JOAFormulas.getColorFromString(amap.getValue(i));
					}
					else if (amap.getName(i).equals("path")) {
						ISOBATHPATHS[ISOBATHCTR] = new String(amap.getValue(i));
					}
					else if (amap.getName(i).equals("description")) {
						ISOBATHDESCRIPS[ISOBATHCTR] = new String(amap.getValue(i));
					}
				}
				ISOBATHCTR++;
			}
			else if (name.equals("param")) {
				KEY_STATE = PARAM_KEY;
			}
			else if (name.equals("paramunits")) {
				KEY_STATE = PARAM_UNITS_KEY;
			}
			else if (name.equals("title")) {
				KEY_STATE = TITLE_KEY;
			}
			else if (name.equals("description")) {
				KEY_STATE = DESCRIP_KEY;
			}
			else if (name.equals("numlevels")) {
				KEY_STATE = NLEVELS_KEY;
			}
			else if (name.equals("baselevel")) {
				KEY_STATE = BASELVL_KEY;
			}
			else if (name.equals("endlevel")) {
				KEY_STATE = ENDLVL_KEY;
			}
			else if (name.equals("contourvalues")) {
				KEY_STATE = CVAL_KEY;
			}
			else if (name.equals("value")) {
				KEY_STATE = VALUE_KEY;
			}
			else if (name.equals("cvalue")) {
				KEY_STATE = COLOR_KEY;
				for (int i = 0; i < amap.getLength(); i++) {
					if (amap.getName(i).equals("red")) {
						try {
							RED = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							RED = 150;
						}
					}
					else if (amap.getName(i).equals("green")) {
						try {
							GREEN = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							GREEN = 150;
						}
					}
					else if (amap.getName(i).equals("blue")) {
						try {
							BLUE = Integer.valueOf(amap.getValue(i)).intValue();
						}
						catch (Exception ex) {
							BLUE = 150;
						}
					}
				}
			}
		}

		public void characters(char[] ch, int start, int len) throws SAXException {
			String strVal = new String(ch, start, len);
			if (KEY_STATE == PARAM_KEY) {
				PARAM = new String(strVal);
			}
			else if (KEY_STATE == PARAM_UNITS_KEY) {
				UNITS = new String(strVal);
			}
			else if (KEY_STATE == TITLE_KEY) {
				TITLE = new String(strVal);
			}
			else if (KEY_STATE == DESCRIP_KEY) {
				DESCRIP = new String(strVal);
			}
			else if (KEY_STATE == METADATATYPE_KEY) {
				new String(strVal);
			}
			else if (KEY_STATE == NLEVELS_KEY) {
				try {
					NLEVELS = Integer.valueOf(strVal).intValue();
				}
				catch (Exception ex) {
					NLEVELS = 1;
				}
				VALS = new double[NLEVELS];
				CVALS = new Color[NLEVELS];
				V = 0;
				C = 0;
			}
			else if (KEY_STATE == BASELVL_KEY) {
				try {
					Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
				}
			}
			else if (KEY_STATE == ENDLVL_KEY) {
				try {
					Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
				}
			}
			else if (KEY_STATE == ENDLVL_KEY) {
				try {
					Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
				}
			}
			else if (KEY_STATE == VALUE_KEY) { // SVAL_KEY || KEY_STATE == CVAL_KEY)
				// {
				try {
					VAL = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					VAL = JOAConstants.MISSINGVALUE;
				}
			}
		}

		public void endElement(String name) throws SAXException {
			if (name.equals("value")) {
				// build a new value
				VALS[V++] = VAL;
			}
			else if (name.equals("cvalue")) {
				// build a new color value
				CVALS[C++] = new Color(RED, GREEN, BLUE);
			}
		}
	}
	
	public static TreeMap<String, String> getBasins(double lat, double lon) {
		TreeMap<String, String> outMap = new TreeMap<String, String>();
		
		if (lat > 80) {
			outMap.put("Arctic", "Arctic");
		}
		
		if (lat >= 0 && lat <= 73 && lon >= -100 && lon <= 10) {
			outMap.put("North Atlantic", "North Atlantic");
		}
		
		if (lat <  0 && lat >= -73 && lon >= -70 && lon <= 21) {
			outMap.put("South Atlantic", "South Atlantic");
		}
		
		if (lat >=  -40 && lat <= 50 && lon >= -90 && lon <= 20) {
			outMap.put("Central Atlantic", "Central Atlantic");
		}
		
		if (lat >=  -40 && lat <= 50 && lon >= -90 && lon <= 20) {
			outMap.put("Central Atlantic", "Central Atlantic");
		}
		
		if (lat >= 0 && lat <= 61 && lon >= -80 && lon <= 20) {
			outMap.put("Central Atlantic", "Central Atlantic");
		}
		
		return outMap;
	}
	
	private boolean isLonInAtlantic(double lon) {
		return (lon > -100 && lon <= 20);
	}
	
	private boolean isLonInPacific(double lon) {
		return (lon < 0 && lon < 80) || (lon > 0 && lon > 120);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// test heat storage
		// Heat Storage per volume relative to 0¡K = density x specific heat x
		// temperature
		//
		// for density 1026 kg / m3
		// specific heat 4000 Joules / kg ¡K
		// temperature 283 ¡K (10¡C)
		//
		// Heat Storage = 1,161,432,000 Joules / m3 [equals approx 1 x 10E9 J/m3]
		//
		// (the 10E-6 term, if used, yields Heat Storage in Joules / cm3)
//		double t = 10;
//		double rho = 1026;
//		double hc = 4000;
//
//		double hs = 1e-6 * (rho * (t + 273.15) * hc);
		System.out.println("atg " + (3.255796E-4 - atg(40.0, 40, 10000)));
		System.out.println("theta " + (36.89073 - theta(40.0, 40, 10000, 0.0)));
//		$sigma = -.033249 for $S = 0, $T = 5, p = 0 
//		$sigma = 27.675465 for $S = 35, $T = 5, p = 0 
//		$sigma = 62.538172 for $S = 35, $T = 25, p = 10000
		System.out.println("sigma " + sigma(0.0, 5.0, 0));
		System.out.println("sigma " + sigma(35.0, 5.0, 0));
		System.out.println("sigma " + sigma(34.6944, 0.650056, 0));
		


	}
}

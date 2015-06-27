/*
 * $Id: NdEditFormulas.java,v 1.13 2005/08/22 21:25:16 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.event.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.io.*;
import ucar.nc2.Variable;
import ucar.nc2.NetcdfFile;
import gov.noaa.pmel.util.GeoDate;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import gov.noaa.pmel.util.IllegalTimeValue;
import ndEdit.ncBrowse.NcFile;
import java.awt.font.*; 
import java.awt.geom.*;

public class NdEditFormulas {
 	static String tFormat_ = "yyyy-MM-dd HH:mm:ss";
  	static GeoDate refDate_ = null;
  	static int increment_;

    public static GeoDate convertToGeoDate(NcFile ncFile, Variable var, int index) {
	    String ref;
	    String units;
	    int pos;
	    GeoDate date;
		Object anArray = null;
  		int[] time2_ = null;
		
		try {
			Array arr = var.read();
			anArray = arr.copyTo1DJavaArray();
		} 
		catch (IOException e) {
			System.out.println(e);
			return null;
		}

		boolean is624_ = false;
		Attribute attr = var.findAttribute("units");
		if ((attr != null) && ((pos = attr.getStringValue().indexOf("since")) != -1)) {
			units = attr.getStringValue();
			ref = units.substring(pos + 5).trim();
			int len = Math.min(ref.length(), tFormat_.length());
			try {
				refDate_ = new GeoDate(ref, tFormat_.substring(0, len));
			} 
			catch (IllegalTimeValue e) {
				System.out.println(e);
				try {
					refDate_ = new GeoDate("1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
				} 
				catch (IllegalTimeValue ee) {}
				System.out.println("   Setting default reference date: " + refDate_.toString());
			}

			date = new GeoDate(refDate_);

			if (units.indexOf("second") != -1) {
				increment_ = GeoDate.SECONDS;
			} 
			else if (units.indexOf("min") != -1) {
				increment_ = GeoDate.MINUTES;
			} 
			else if (units.indexOf("hour") != -1) {
				increment_ = GeoDate.HOURS;
			} 
			else if (units.indexOf("day") != -1) {
				increment_ = GeoDate.DAYS;
			} 
			else if (units.indexOf("mon") != -1) {
				increment_ = GeoDate.MONTHS;
			} 
			else if (units.indexOf("year") != -1) {
				increment_ = GeoDate.YEARS;
			} 
			else if (units.indexOf("msec") != -1) {
				increment_ = GeoDate.MSEC;
			} 
			else {
				increment_ = GeoDate.SECONDS;
			}
			if (anArray instanceof int[]) {
				date.increment((double)((int[])anArray)[index], increment_);
			} 
			else if (anArray instanceof short[]) {
				date.increment((double)((short[])anArray)[index], increment_);
			} 
			else if (anArray instanceof float[]) {
				date.increment((double)((float[])anArray)[index], increment_);
			} 
			else if(anArray instanceof double[]) {
				date.increment(((double[])anArray)[index], increment_);
			} 
			else {
				return null;
			}
			return date;
		}
		else {
			attr = var.findAttribute("epic_code");
			if (attr != null) {
				if (attr.getNumericValue().intValue() == 624) {
					is624_ = true;
					//
					// time is a double integer
					//
					Variable time2 = ncFile.findVariable(var.getName() + "2");
					try {
						Array arr = time2.read();
						time2_ = (int[])arr.copyTo1DJavaArray();
					} 
					catch (IOException e) {
						System.out.println(e);
						return null;
					}
					int jday = ((int[])anArray)[index];
					int msec = time2_[index];
					return new GeoDate(jday, msec);
				}
			}
		}
		return null;
	}
  	public static double getNiceLowerValue(double inVal, double range, double constraint) {
  		double tempVal = getNiceLowerValue(inVal, range);
  		if (tempVal < constraint)
  			return constraint;
  		return tempVal;
  	}

	public static double getNiceLowerValue(double inVal, double range) {
		double newVal;
			newVal = inVal - (range * 0.025f);
		return newVal;
	}
	
	public static double getNiceUpperValue(double inVal, double range, double constraint) {
		double tempVal = getNiceUpperValue(inVal, range);
		if (tempVal > constraint)
			return constraint;
		return tempVal;
	}
	
	public static double getNiceUpperValue(double inVal, double range) {
		double newVal;
			newVal = inVal + (range * 0.025f);
		return newVal;
	}
	
	public static Triplet GetPrettyRange(double inMin, double inMax, double floor, double ceiling) {
		double diff, factor = 1.0f, outMax, outMin, outInc;
		
		diff = Math.abs(inMin - inMax);
		
		if (diff == 0.0)
			factor = 1.0f;
		else if (diff >= 0.0001 && diff < 0.001)
			factor = 0.0001f;
		else if (diff >= 0.001 && diff < 0.01)
			factor = 0.001f;
		else if (diff >= 0.01 && diff < 0.1)
			factor = 0.01f;
		else if (diff >= 0.1 && diff < 1.0)
			factor = 0.1f;
		else if (diff >= 1.0 && diff < 10.0)
			factor = 1.0f;
		else if (diff >= 10.0 && diff < 100.0)
			factor = 10.0f;
		else if (diff >= 100.0 && diff < 1000.0)
			factor = 100.0f;
		else if (diff >= 1000.0 && diff < 10000.0)
			factor = 1000.0f;
		else if (diff >= 10000.0 && diff < 100000.0)
			factor = 10000.0f;
		
		outMin = (double)Math.floor(inMin/factor) * factor;
		outMax = (double)Math.ceil(inMax/factor) * factor;
		int numIncs = (int)((outMax - outMin)/factor);
		outInc = (outMax - outMin)/numIncs;
		
		if (outMin < floor) {
			outMin = floor;
			numIncs = (int)((outMax - outMin)/factor);
			outInc = (outMax - outMin)/numIncs;
		}
		
		if (outMax > ceiling) {
			outMax = ceiling;
			numIncs = (int)((outMax - outMin)/factor);
			outInc = (outMax - outMin)/numIncs;
		}
		
		return new Triplet(outMin, outMax, outInc);
	}
	
	public static Triplet GetPrettyRange(double inMin, double inMax) {
		double diff, factor = 1.0f, outMax, outMin, outInc;
		
		diff = Math.abs(inMin - inMax);
		
		if (diff == 0.0)
			factor = 1.0f;
		else if (diff >= 0.0001 && diff < 0.001)
			factor = 0.0001f;
		else if (diff >= 0.001 && diff < 0.01)
			factor = 0.001f;
		else if (diff >= 0.01 && diff < 0.1)
			factor = 0.01f;
		else if (diff >= 0.1 && diff < 1.0)
			factor = 0.1f;
		else if (diff >= 1.0 && diff < 10.0)
			factor = 1.0f;
		else if (diff >= 10.0 && diff < 100.0)
			factor = 10.0f;
		else if (diff >= 100.0 && diff < 1000.0)
			factor = 100.0f;
		else if (diff >= 1000.0 && diff < 10000.0)
			factor = 1000.0f;
		else if (diff >= 10000.0 && diff < 100000.0)
			factor = 10000.0f;
		else if (diff >= 100000.0 && diff < 1000000.0)
			factor = 100000.0f;
		else if (diff >= 1000000.0 && diff < 10000000.0)
			factor = 1000000.0f;
		else if (diff >= 10000000.0 && diff < 100000000.0)
			factor = 10000000.0f;
		else if (diff >= 100000000.0 && diff < 1000000000.0)
			factor = 100000000.0f;
		else if (diff >= 1000000000.0 && diff < 10000000000.0)
			factor = 1000000000.0f;
		else if (diff >= 10000000000.0 && diff < 100000000000.0)
			factor = 10000000000.0f;
		else if (diff >= 100000000000.0 && diff < 1000000000000.0)
			factor = 100000000000.0f;
		else if (diff >= 1000000000000.0 && diff < 10000000000000.0)
			factor = 1000000000000.0f;
		else if (diff >= 10000000000000.0 && diff < 100000000000000.0)
			factor = 10000000000000.0f;
		else if (diff >= 100000000000000.0 && diff < 1000000000000000.0)
			factor = 100000000000000.0f;
		else if (diff >= 1000000000000000.0 && diff < 10000000000000000.0)
			factor = 1000000000000000.0f;
		else if (diff >= 10000000000000000.0 && diff < 100000000000000000.0)
			factor = 10000000000000000.0f;
		else if (diff >= 100000000000000000.0 && diff < 1000000000000000000.0)
			factor = 100000000000000000.0f;
		else if (diff >= 1000000000000000000.0 && diff < 10000000000000000000.0)
			factor = 1000000000000000000.0f;
		else if (diff >= 10000000000000000000.0 && diff < 100000000000000000000.0)
			factor = 10000000000000000000.0f;
		else if (diff >= 100000000000000000000.0 && diff < 1000000000000000000000.0)
			factor = 100000000000000000000.0f;
		else if (diff >= 1000000000000000000000.0 && diff < 10000000000000000000000.0)
			factor = 1000000000000000000000.0f;
		else if (diff >= 10000000000000000000000.0 && diff < 100000000000000000000000.0)
			factor = 10000000000000000000000.0f;
		else if (diff >= 100000000000000000000000.0 && diff < 1000000000000000000000000.0)
			factor = 100000000000000000000000.0f;
		else if (diff >= 1000000000000000000000000.0 && diff < 10000000000000000000000000.0)
			factor = 1000000000000000000000000.0f;
		
		outMin = (double)Math.floor(inMin/factor) * factor;
		outMax = (double)Math.ceil(inMax/factor) * factor;
		int numIncs = (int)((outMax - outMin)/factor);
		outInc = (outMax - outMin)/numIncs;
		return new Triplet(outMin, outMax, outInc);
	}
	
	public static double[] GetPrettyRange(double[] minMax, double minConstraint, double maxConstraint) {		
		double[] tempMinMax = GetPrettyRange(minMax);
		
		double outMin = tempMinMax[0] < minConstraint ? minConstraint : tempMinMax[0];
		double outMax = tempMinMax[1] > maxConstraint ? maxConstraint : tempMinMax[1];

		double[] out = new double[2];
		out[0] = outMin;
		out[1] = outMax;
		
		return out;
	}
	
	public static double[] GetPrettyRange(double[] minMax) {
		double diff, factor = 1.0f, outMax, outMin, outInc;
		
		double inMin = minMax[0];
		double inMax = minMax[1];
		
		diff = Math.abs(inMin - inMax);
		
		if (diff == 0.0)
			factor = 1.0f;
		else if (diff >= 0.0001 && diff < 0.001)
			factor = 0.0001f;
		else if (diff >= 0.001 && diff < 0.01)
			factor = 0.001f;
		else if (diff >= 0.01 && diff < 0.1)
			factor = 0.01f;
		else if (diff >= 0.1 && diff < 1.0)
			factor = 0.1f;
		else if (diff >= 1.0 && diff < 10.0)
			factor = 1.0f;
		else if (diff >= 10.0 && diff < 100.0)
			factor = 10.0f;
		else if (diff >= 100.0 && diff < 1000.0)
			factor = 100.0f;
		else if (diff >= 1000.0 && diff < 10000.0)
			factor = 1000.0f;
		else if (diff >= 10000.0 && diff < 100000.0)
			factor = 10000.0f;
		else if (diff >= 100000.0 && diff < 1000000.0)
			factor = 100000.0f;
		else if (diff >= 1000000.0 && diff < 10000000.0)
			factor = 1000000.0f;
		else if (diff >= 10000000.0 && diff < 100000000.0)
			factor = 10000000.0f;
		else if (diff >= 100000000.0 && diff < 1000000000.0)
			factor = 100000000.0f;
		else if (diff >= 1000000000.0 && diff < 10000000000.0)
			factor = 1000000000.0f;
		else if (diff >= 10000000000.0 && diff < 100000000000.0)
			factor = 10000000000.0f;
		else if (diff >= 100000000000.0 && diff < 1000000000000.0)
			factor = 100000000000.0f;
		else if (diff >= 1000000000000.0 && diff < 10000000000000.0)
			factor = 1000000000000.0f;
		else if (diff >= 10000000000000.0 && diff < 100000000000000.0)
			factor = 10000000000000.0f;
		else if (diff >= 100000000000000.0 && diff < 1000000000000000.0)
			factor = 100000000000000.0f;
		else if (diff >= 1000000000000000.0 && diff < 10000000000000000.0)
			factor = 1000000000000000.0f;
		else if (diff >= 10000000000000000.0 && diff < 100000000000000000.0)
			factor = 10000000000000000.0f;
		else if (diff >= 100000000000000000.0 && diff < 1000000000000000000.0)
			factor = 100000000000000000.0f;
		else if (diff >= 1000000000000000000.0 && diff < 10000000000000000000.0)
			factor = 1000000000000000000.0f;
		else if (diff >= 10000000000000000000.0 && diff < 100000000000000000000.0)
			factor = 10000000000000000000.0f;
		else if (diff >= 100000000000000000000.0 && diff < 1000000000000000000000.0)
			factor = 100000000000000000000.0f;
		else if (diff >= 1000000000000000000000.0 && diff < 10000000000000000000000.0)
			factor = 1000000000000000000000.0f;
		else if (diff >= 10000000000000000000000.0 && diff < 100000000000000000000000.0)
			factor = 10000000000000000000000.0f;
		else if (diff >= 100000000000000000000000.0 && diff < 1000000000000000000000000.0)
			factor = 100000000000000000000000.0f;
		else if (diff >= 1000000000000000000000000.0 && diff < 10000000000000000000000000.0)
			factor = 1000000000000000000000000.0f;
		
		outMin = (double)Math.floor(inMin/factor) * factor;
		outMax = (double)Math.ceil(inMax/factor) * factor;
		int numIncs = (int)((outMax - outMin)/factor);
		outInc = (outMax - outMin)/numIncs;
		
		double[] out = new double[2];
		out[0] = outMin;
		out[1] = outMax;
		
		return out;
	}

	public static int GetDisplayPrecision(double inc) {
		int numPlaces = 2;
		if (inc < 0.01)
			numPlaces = 3;
		else if (inc >= 0.01 && inc < 0.1)
			numPlaces = 2;
		else if (inc >= 0.1 && inc < 1.0)
			numPlaces = 1;
		else if (inc >= 1.0) {
			double diff = inc - (int)inc;
			if (diff == 0.0)
				numPlaces = 0;
			else if (diff < 0.01)
				numPlaces = 3;
			else if (diff >= 0.01 && diff < 0.1)
				numPlaces = 2;
			else if (diff >= 0.1 && diff < 1.0)
				numPlaces = 1;
		}
		return numPlaces;
	}
	
	public static String formatLongDate(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date(date));
	}
	
	public static String formatShortDate(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-DDD");
		return sdf.format(new Date(date));
	}
	
	public static String formatDouble(String inValStr, int numPlaces, boolean pad) {
		// look for the decimal point
		int decPlace = inValStr.lastIndexOf('.');
		int len = inValStr.length();
		
		StringBuffer sb = new StringBuffer(inValStr);
		
		if (numPlaces > 0) {
			if (len - decPlace > numPlaces+1) {
				// truncate string
				sb.setLength(decPlace + 1 + numPlaces);
			}
			else if (len - decPlace < numPlaces+1) {
				// pad with 0's
				while (sb.length() < decPlace + 1 + numPlaces)
					sb.append('0');
			}
		}
		else
			sb.setLength(decPlace);
		
		if (pad) {
			while (sb.length() < 8)
				sb.insert(0, ' ');
		}
		return new String(sb);
	}
	
	public static String formatDouble(double inVal, int inNumPlaces, boolean pad) {
		if (inVal >= 1.0e35)
			return "    ----";
		int numPl = inNumPlaces;
		String valStr = new Double(inVal).toString();
		int expPlace = valStr.indexOf('E');
		if (expPlace > 0) {
			// number in scientific notation--get the exponent
			String exp = valStr.substring(expPlace, valStr.length());
			exp = exp.toLowerCase();
			exp = exp.substring(1, exp.length());
			int sign = exp.indexOf("-") >= 0 ? -1 : 1;
			numPl = Math.abs(Integer.valueOf(exp).intValue());
		}
		
		String frmt = null;
		if (numPl == 1)
			frmt = new String("0.0"); 
		else if (numPl == 2)
			frmt = new String("0.00"); 
		else if (numPl == 3)
			frmt = new String("0.000"); 
		else if (numPl == 4)
			frmt = new String("0.0000");  
		else if (numPl == 5)
			frmt = new String("0.00000");  
		else if (numPl == 6)
			frmt = new String("0.000000"); 
			
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
			}
			catch (Exception exx) {
				return new Double(inVal).toString();
			}
		}
		if (pad) {
			while (out.length() < 8)
				out.insert(0, ' ');
		}
		String str = new String(out);
		return str;
	}
	
	public static void drawStyledString(String str, int startH, int startV, FontMetrics fm, Graphics g) {
		if (str.indexOf('^') >= 0) {
			// draw char by char--loop on chars
			boolean doSuper = false;
			int h = startH;
			int superHeight = 4;
			int startRun = 0;
			int endRun = 0;
			boolean runStarted = false;
			for (int c=0; c<str.length(); c++) {
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
						g.drawString(str.substring(c, c+1), h, startV-superHeight);
						h += fm.charWidth(cc);
						doSuper = false;
					}
					else {
						if (!runStarted)
							startRun = c;
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
		else
			// draw whole string
			g.drawString(str, startH, startV);
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
		
			//Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style, size);
			//FontRenderContext frc = new FontRenderContext(null, true, true);
			//LineMetrics metrics = font.getLineMetrics(str, frc);
			//GlyphVector glphV = font.createGlyphVector(frc, str);
			//g.drawGlyphVector(glphV, 0.0f, 0.0f);
			
		// ADD THE ATTRIBUTE FOR THE SS
		if (caretPos >= 0) {
			/*Font font = new Font(JOAConstants.DEFAULT_AXIS_VALUE_FONT, style, size);
			TextLayout aLayout = new TextLayout(str.substring(caretPos, caretPos+1), font, g.getFontRenderContext());
			Shape aShape = aLayout.getOutline(null);
			g.setColor(Color.black);
			g.fill(aShape);
			ShapeGraphicAttribute aReplacement = new ShapeGraphicAttribute(aShape, GraphicAttribute.TOP_ALIGNMENT, true);
			drawnString.addAttribute(TextAttribute.CHAR_REPLACEMENT, aReplacement, caretPos, caretPos+1);*/
			drawnString.addAttribute(TextAttribute.SUPERSCRIPT, new Integer(10), caretPos, caretPos+1);
			drawnString.addAttribute(TextAttribute.SIZE, new Float(0.80 * size), caretPos, caretPos+1);
		}
			
	    angle = -(double)angle;
	    
	    // save the rendering hints
	    RenderingHints oldRenderingHints = null;
	    
    	oldRenderingHints = g.getRenderingHints();
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	    
	    // save the current transform
	    AffineTransform oldTransform = new AffineTransform(g.getTransform());
	    //System.out.println("x translation before = " + g.getTransform().getTranslateX());
	    //System.out.println("y translation before = " + g.getTransform().getTranslateY());
	    
	    // translate to the drawing point
	    g.translate(startH, startV);
	    //System.out.println("x translation during = " + g.getTransform().getTranslateX());
	   // System.out.println("y translation during = " + g.getTransform().getTranslateY());
	    
	    //  rotate the coordinate system
	    g.rotate(Math.PI * angle/180.0);
	    g.translate(hoffset, voffset);

		// draw the string in rotated coordinate space
	    g.drawString(drawnString.getIterator(), 0, 0);
	    
	    // restore old transform and rendering hints
	    g.setTransform(oldTransform);
	    g.setRenderingHints(oldRenderingHints);
	    //System.out.println("x translation after = " + g.getTransform().getTranslateX());
	    //System.out.println("y translation after = " + g.getTransform().getTranslateY());
	}
	
	public static String formatLat(double inLat, int prec) {
		if (inLat < 0)
			return new String(formatDouble(String.valueOf(-inLat), prec, true) + " S");
		else
			return new String(formatDouble(String.valueOf(inLat), prec, true) + " N");
	}
		
	public static String formatLon(double inLon, int prec) {
		if (inLon < 0)
			return new String(formatDouble(String.valueOf(-inLon), prec, true) + " W");
		else
			return new String(formatDouble(String.valueOf(inLon), prec, true) + " E");
	}
	
	public static File getSupportFile(String name) throws FileNotFoundException {
		String dir = System.getProperty("user.dir") + File.separator;
		File nf = new File(dir, name);
		if (nf == null)
			throw new FileNotFoundException();
		return nf;
	}
	
	public static String getSupportPath() {
		if (Constants.SUPPORT_DIR != null)
			return Constants.SUPPORT_DIR + File.separator;
		else 
			return System.getProperty("user.dir") + File.separator;
	}
	
	public static String getCoastlinePath() {
		if (Constants.SUPPORT_DIR != null)
			return Constants.SUPPORT_DIR + File.separator + "coastlines" + File.separator;
		else 
			return System.getProperty("user.dir") + File.separator + "coastlines" + File.separator;
	}
	
	public static String getBathymetryPath() {
		if (Constants.SUPPORT_DIR != null)
			return Constants.SUPPORT_DIR + File.separator + "bathymetry" + File.separator;
		else 
			return System.getProperty("user.dir") + File.separator + "bathymetry" + File.separator;
	}
	
	public static String getCustomCoastlinePath() {
		if (Constants.SUPPORT_DIR != null)
			return Constants.SUPPORT_DIR + File.separator + "coastlines" + File.separator + "Custom_Coastlines" + File.separator;
		else 
			return System.getProperty("user.dir") + File.separator + "coastlines" + File.separator + "Custom_Coastlines" + File.separator;
	}
	
	public static String getCustomBathymetryPath() {
		if (Constants.SUPPORT_DIR != null)
			return Constants.SUPPORT_DIR + File.separator + "bathymetry" + File.separator + "Custom_Bathymetry" + File.separator;
		else 
			return System.getProperty("user.dir") + File.separator + "bathymetry" + File.separator + "Custom_Bathymetry" + File.separator;
	}

	public static double HAVD(double A) {
		return (1 - COSD(A))/2;
	}

	public static double AHAVD(double A){
		return (double)(Math.acos(-(A*2 - 1))/Constants.F);
	}
	
	public static double COSD(double A) {
		return (double)(Math.cos(A*Constants.F));
	}
		
	public static double GreatCircle(double lat1, double lon1, double lat2, double lon2) {
	    double 		l, havth;
	    double 		dlo, d;


		/*  THIS ROUTINE FINDS THE GREAT CIRCLE COURSE GIVEN TWO POINTS ON THE GLOBE.*/
		/*  ALSO THE JOAConstants.DISTANCE IN NAUTICAL MILES. */
		/*  REF: BOWDITCH, NATHANIEL, AMERICAN PRACTICAL NAVIGATOR. 1981, P 603-608. */

    	l = Math.abs(lat2 - lat1);
    	dlo = Math.abs(lon2 - lon1);

	    if (dlo < 0.001) {
			d = l * 60;
			return d;
	    }
	
	    if (dlo > 180)
			dlo = 360 - dlo;
	
	    havth = HAVD(dlo) + COSD(lat1) * COSD(lat2);
	    d = AHAVD(havth + HAVD(l));
	    d = d * 60;
	
	    return d;
	}
	
	public static String paramNameToJOAName(String inParam) {
		// always translate pressure
		if (inParam.equalsIgnoreCase("CTDPRS") || inParam.startsWith("PRES")) {
			return new String("PRES");
		}
		
		if (inParam.equalsIgnoreCase("BTLNBR")) {
			return new String("BTLN");
		}
		else if (inParam.startsWith("CTDT") || inParam.startsWith("TEM") ||
			inParam.equalsIgnoreCase("T") || inParam.equalsIgnoreCase("TE") || inParam.equalsIgnoreCase("T00AN1")) {
			return new String("TEMP");
		}
		else if (inParam.equalsIgnoreCase("SAMPNO")) {
			return new String("SAMP");
		}
		else if (inParam.indexOf("CTDSAL") >= 0) {
			return new String("CTDS");
		}
		else if (inParam.equalsIgnoreCase("CTDOXY")) {
			return new String("CTDO");
		}
		else if (inParam.equalsIgnoreCase("THETA")) {
			return new String("WTHT");
		}
		else if (inParam.equalsIgnoreCase("Bedfort")) {
			return new String("BEDFORT");
		}
		else if (inParam.startsWith("SPPT") || inParam.startsWith("BSAL") || inParam.startsWith("SAL") || inParam.equalsIgnoreCase("S") || 
				 inParam.equalsIgnoreCase("SA") || inParam.equalsIgnoreCase("S00AN1")) {
			return new String("SALT");
		}
		else if (inParam.equalsIgnoreCase("OXYGEN") || inParam.equalsIgnoreCase("OXY") || inParam.equalsIgnoreCase("DOXY") ||
			inParam.startsWith("O2") || inParam.equalsIgnoreCase("O") || inParam.startsWith("BOTTLE_OXYGEN") || inParam.equalsIgnoreCase("O00AN1")) {
			return new String("O2");
		}
		else if (inParam.equalsIgnoreCase("SILICATE") || inParam.equalsIgnoreCase("SILICAT") || inParam.equalsIgnoreCase("SILI") || inParam.startsWith("SIL") || inParam.equalsIgnoreCase("SI") ||
			inParam.equalsIgnoreCase("SIO3") || inParam.equalsIgnoreCase("SIO4") || inParam.equalsIgnoreCase("SLCA") || inParam.equalsIgnoreCase("I00AN1")) {
			return new String("SIO3");
		}
		else if (inParam.equalsIgnoreCase("NO2+NO3") || inParam.equalsIgnoreCase("NO2_NO3") || inParam.equalsIgnoreCase("nitrate_plus_nitrite")) {
			return new String("NO2+NO3");
		}
		else if (inParam.equalsIgnoreCase("NITRATE") || inParam.equalsIgnoreCase("NITRAT") || inParam.equalsIgnoreCase("NTRA") ||
			inParam.startsWith("NO3") || inParam.equalsIgnoreCase("N00AN1")) {
			return new String("NO3");
		}
		else if (inParam.equalsIgnoreCase("NITRITE") || inParam.equalsIgnoreCase("NITRIT") || inParam.equalsIgnoreCase("NTRI") ||
			inParam.startsWith("NO2")) {
			return new String("NO2");
		}
		else if (inParam.equalsIgnoreCase("PHOSPHATE") || inParam.equalsIgnoreCase("PHOS") || inParam.equalsIgnoreCase("PHSPHT") || 
			     inParam.startsWith("PO4") || inParam.equalsIgnoreCase("P00AN1")) {
			return new String("PO4");
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
		else if (inParam.equalsIgnoreCase("FCO2") || inParam.equalsIgnoreCase("fugacity_co2")) {
			return new String("FCO2");
		}
		else if (inParam.equalsIgnoreCase("FCO2TMP") || inParam.equalsIgnoreCase("fugacity_co2_temperature")) {
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
			return new String("PH");
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
			return new String("C13");
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
			return new String("CH4");
		}
		else if (inParam.equalsIgnoreCase("DON")|| inParam.equalsIgnoreCase("nitrogen_dissolved_organic")) {
			return new String("DON");
		}
		else if (inParam.equalsIgnoreCase("N20")) {
			return new String("N20");
		}
		else if (inParam.equalsIgnoreCase("CHLORA") || inParam.equalsIgnoreCase("CHLA") || inParam.equalsIgnoreCase("chlorophyl_a")  || inParam.equalsIgnoreCase("CHPL")) {
			return new String("CHLA");
		}
		else if (inParam.equalsIgnoreCase("PPHYTN") || inParam.equalsIgnoreCase("phaeophytin") || inParam.equalsIgnoreCase("PHAE")) {
			return new String("PPHYTN");
		}
		else if (inParam.equalsIgnoreCase("POC") || inParam.equalsIgnoreCase("patriculate_organic_carbon") || inParam.equalsIgnoreCase("POCA")) {
			return new String("POC");
		}
		else if (inParam.equalsIgnoreCase("PON") || inParam.equalsIgnoreCase("patriculate_organic_nitrogen")) {
			return new String("PON");
		}
		else if (inParam.equalsIgnoreCase("BACT")) {
			return new String("BACT");
		}
		else if (inParam.equalsIgnoreCase("DOC") || inParam.equalsIgnoreCase("dissolved_organic_carbon") || inParam.equalsIgnoreCase("DOCA")) {
			return new String("DOC");
		}
		else if (inParam.equalsIgnoreCase("COMON") || inParam.equalsIgnoreCase("carbon_monoxide")) {
			return new String("CO");
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
		return inParam;
	}
}
/*
 * $Id: NewColorBar.java,v 1.5 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.util.*;
import java.io.*;
import gov.noaa.pmel.util.*;
import java.text.*;
import java.text.Format;
import gov.noaa.pmel.text.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.events.*;
import gov.noaa.pmel.sgt.*;

@SuppressWarnings("serial")
public class NewColorBar implements Serializable, MetadataChangedListener {
	protected String mParam, mTitle, mDescription;
	private String mParamUnits;
	protected int mNumColorLevels;
	protected double mBaseLevel, mEndLevel;
	protected double[] mContourValues = new double[128];
	protected Color[] mColorValues = new Color[128];
	protected ContourLineAttribute[] mContourLineAttributes = new ContourLineAttribute[128];
	protected boolean mUseMetadata = false;
	protected String mMetadataType;
	protected GeoDate mMinDate;
	protected GeoDate mMaxDate;
	protected double mLatMin;
	protected double mLatMax;
	protected double mLonMin;
	protected double mLonMax;
	private ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	protected LongitudeFormat lonDM;
	protected LatitudeFormat latDM;
	protected String sdfGMT = new String(JOAConstants.DEFAULT_DATE_FORMAT);
	protected GeoDate mGeoDate = new GeoDate();
	protected int mEnhancedRangeMin = -99;
	protected int mEnhancedRangeMax = -99;

	public NewColorBar(Color[] inColors, double[] inVals, int numLevels, String inParam, String units, String title, String description) {
		mContourValues = inVals;
		mColorValues = inColors;
		mNumColorLevels = numLevels;
		mBaseLevel = inVals[0];
		mEndLevel = inVals[numLevels - 1];
		mParam = inParam;
		mTitle = title;
		mDescription = description;
		mUseMetadata = false;
		clearEnhancedColors();
		setDefaultContours();
		mParamUnits = units;
	}

	private void setDefaultContours() {
		for (int i = 0; i < mNumColorLevels; i++) {
			mContourLineAttributes[i] = new ContourLineAttribute();
		}
	}

	public NewColorBar(Color[] inColors, double[] inVals, ContourLineAttribute[] cla, int numLevels, String inParam,
	    String units, String title, String description) {
		this(inColors, inVals, numLevels, inParam, units, title, description);
		mContourLineAttributes = cla;
	}

	public NewColorBar(Color[] inColors, double[] inVals, int numLevels, String inParam, String units, String title,
	    String description, String metaType) {
		this(inColors, inVals, numLevels, inParam, units, title, description);
		mMetadataType = new String(metaType);
		mUseMetadata = true;
		setLabelFormatter();
		clearEnhancedColors();
	}

	public NewColorBar(Color[] inColors, double[] inVals, int numLevels, String inParam, String units, String title,
	    String description, String metaType, GeoDate mindate, GeoDate maxdate, double minlat, double maxlat,
	    double minlon, double maxlon) {
		this(inColors, inVals, numLevels, inParam, units, title, description);
		mMetadataType = new String(metaType);
		mUseMetadata = true;
		this.setMetadata(minlat, maxlat, minlon, maxlon, mindate, maxdate);
		setLabelFormatter();
		clearEnhancedColors();
	}

	public NewColorBar(FileViewer fv, int cbParam) {
		double base = fv.mAllProperties[cbParam].mPlotMin;
		double end = fv.mAllProperties[cbParam].mPlotMax;
		String var = fv.mAllProperties[cbParam].mVarLabel;
		createAutoscaledColorBar(base, end, var);
	}

	public NewColorBar(double base, double end, String inVar) {
		createAutoscaledColorBar(base, end, inVar);
		clearEnhancedColors();
	}

	public NewColorBar(NewColorBar inCB) {
		// copy ctor
		if (inCB == null)
			return;
		if (inCB.mParam == null)
			return;
		mParam = new String(inCB.mParam);
		mTitle = new String(inCB.mTitle);
		if (inCB.mDescription != null)
			mDescription = new String(inCB.mDescription);
		mNumColorLevels = inCB.mNumColorLevels;
		mBaseLevel = inCB.mBaseLevel;
		mEndLevel = inCB.mEndLevel;
		for (int i = 0; i < mNumColorLevels; i++) {
			mContourValues[i] = inCB.mContourValues[i];
			mColorValues[i] = new Color(inCB.mColorValues[i].getRGB());
			mEnhancedRangeMin = inCB.mEnhancedRangeMin;
			mEnhancedRangeMax = inCB.mEnhancedRangeMax;
			mContourLineAttributes[i] = inCB.mContourLineAttributes[i];
		}
		mUseMetadata = inCB.isMetadataColorBar();
		if (mUseMetadata && inCB.getMetadataType() != null) {
			mMetadataType = new String(inCB.getMetadataType());
			if (inCB.mMinDate != null)
				mMinDate = new GeoDate(inCB.mMinDate);
			if (inCB.mMaxDate != null)
				mMaxDate = new GeoDate(inCB.mMaxDate);
			mLatMin = inCB.mLatMin;
			mLatMax = inCB.mLatMax;
			mLonMin = inCB.mLonMin;
			mLonMax = inCB.mLonMax;
		}
	}

	public NewColorBar(ColorBar inCB) {
		// copy ctor
		if (inCB == null)
			return;
		if (inCB.mParam == null)
			return;
		mParam = new String(inCB.mParam);
		if (inCB.mTitle != null)
			mTitle = new String(inCB.mTitle);
		else
			mTitle = "title";
		if (inCB.mDescription != null)
			mDescription = new String(inCB.mDescription);
		mNumColorLevels = inCB.mNumColorLevels;
		mBaseLevel = inCB.mBaseLevel;
		mEndLevel = inCB.mEndLevel;
		for (int i = 0; i < mNumColorLevels; i++) {
			mContourValues[i] = inCB.mContourValues[i];
			mColorValues[i] = new Color(inCB.mColorValues[i].getRGB());
		}
		mUseMetadata = false;
		setLabelFormatter();
	}

	public void clearEnhancedColors() {
		mEnhancedRangeMin = -99;
		mEnhancedRangeMax = -99;
	}

	public void setMinEnhancedColor(int index) {
		mEnhancedRangeMin = index;
		System.out.println("setMinEnhancedColor to " + mEnhancedRangeMin);
	}

	public void setMaxEnhancedColor(int index) {
		mEnhancedRangeMax = index;
		System.out.println("setMaxEnhancedColor to " + mEnhancedRangeMax);
	}

	public int getMinEnhancedRange() {
		System.out.println("mEnhancedRangeMin = " + mEnhancedRangeMin);
		return mEnhancedRangeMin;
	}

	public int getMaxEnhancedRange() {
		System.out.println("mEnhancedRangeMax = " + mEnhancedRangeMax);
		return mEnhancedRangeMax;
	}

	public boolean isColorEnhanced(int index) {
		if (mEnhancedRangeMin != -99 && mEnhancedRangeMax != -99)
			return (index >= mEnhancedRangeMin && index <= mEnhancedRangeMax);
		else if (mEnhancedRangeMin != -99 && mEnhancedRangeMax == -99)
			return index == mEnhancedRangeMin;
		return false;
	}

	public void dumpCB() {
		System.out.println("mParam = " + mParam);
		System.out.println("mContourValues length= " + mContourValues.length);
		System.out.println("mNumColorLevels = " + mNumColorLevels);
		for (int i = 0; i < mNumColorLevels; i++)
			System.out.println("I = " + mContourValues[i] + " " + mColorValues[i]);

	}

	private void createAutoscaledColorBar(double base, double end, String inVar) {
		// Create the default autoscaled color bar: linear from start to end, 15
		// colors
		mNumColorLevels = 15;
		mBaseLevel = base;
		mEndLevel = end;
		// mMethod = JOAConstants.LINEAR;
		mParam = new String(inVar);
		mTitle = new String(inVar + ": Autoscaled");
		double red = 1266. / 65535.;
		double green = 1223. / 65535.;
		double blue = 64768. / 65535.;
		mColorValues[0] = new Color((float) red, (float) green, (float) blue);

		red = 512. / 65535.;
		green = 23296. / 65535.;
		blue = 64768. / 65535.;
		mColorValues[1] = new Color((float) red, (float) green, (float) blue);

		red = 9216. / 65535.;
		green = 42496. / 65535.;
		blue = 64768. / 65535.;
		mColorValues[2] = new Color((float) red, (float) green, (float) blue);

		red = 16896. / 65535.;
		green = 57344. / 65535.;
		blue = 64768. / 65535.;
		mColorValues[3] = new Color((float) red, (float) green, (float) blue);

		red = 25856. / 65535.;
		green = 64768. / 65535.;
		blue = 61440. / 65535.;
		mColorValues[4] = new Color((float) red, (float) green, (float) blue);

		red = 33024. / 65535.;
		green = 64768. / 65535.;
		blue = 54528. / 65535.;
		mColorValues[5] = new Color((float) red, (float) green, (float) blue);

		red = 47872. / 65535.;
		green = 64768. / 65535.;
		blue = 55296. / 65535.;
		mColorValues[6] = new Color((float) red, (float) green, (float) blue);

		red = 58112. / 65535.;
		green = 64768. / 65535.;
		blue = 59392. / 65535.;
		mColorValues[7] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 61696. / 65535.;
		blue = 52992. / 65535.;
		mColorValues[8] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 57600. / 65535.;
		blue = 44800. / 65535.;
		mColorValues[9] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 51713. / 65535.;
		blue = 31863. / 65535.;
		mColorValues[10] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 41984. / 65535.;
		blue = 29952. / 65535.;
		mColorValues[11] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 32512. / 65535.;
		blue = 23040. / 65535.;
		mColorValues[12] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 21504. / 65535.;
		blue = 15872. / 65535.;
		mColorValues[13] = new Color((float) red, (float) green, (float) blue);

		red = 64768. / 65535.;
		green = 1292. / 65535.;
		blue = 1808. / 65535.;
		mColorValues[14] = new Color((float) red, (float) green, (float) blue);

		// compute the levels
		double increment = (end - base) / ((double) mNumColorLevels - 1);
		for (int i = 0; i < mNumColorLevels; i++) {
			mContourValues[i] = base + (i * increment);
		}
		setDefaultContours();
	}

	public int getColorIndex(GeoDate date) {
		return getColorIndex(date.getTime());
	}

	public int getColorIndex(double inVal) {
		// normalize if a metadata colorbar
		double useVal = inVal;
		if (mUseMetadata) {
			// normalize the input value--it's either a time, latitude, or longitude
			if (mMetadataType.equalsIgnoreCase(b.getString("kDateTime"))) {
				double dminval = (double) mMinDate.getTime();
				double dmaxval = (double) mMaxDate.getTime();
				useVal = (inVal - dminval) / (dmaxval - dminval);
			}
			if (mMetadataType.equalsIgnoreCase(b.getString("kDateTimeMonth"))) {
				// inVal is the month of the station date
				double dminval = 1.0;
				double dmaxval = 12.0;
				useVal = (inVal - dminval) / (dmaxval - dminval);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLatitude"))) {
				useVal = (inVal - mLatMin) / (mLatMax - mLatMin);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLongitude"))) {
				useVal = (inVal - mLonMin) / (mLonMax - mLonMin);
			}
		}

		int end = this.mNumColorLevels;
		if (this.mContourValues[1] > this.mContourValues[0]) {
			// increasing color bar:
			if (useVal < this.mContourValues[0]) { return 0; }
			if (useVal >= this.mContourValues[end - 2]) { return end - 1; }
			for (int i = 0; i < end - 2; i++) {
				if (useVal >= this.mContourValues[i] && useVal < this.mContourValues[i + 1]) { return i + 1; }
			}
		}
		else {
			// decreasing color bar:
			if (useVal > this.mContourValues[0]) {
				return 0;
			}
			else if (useVal < this.mContourValues[0]) { return 0; }
			if (useVal <= this.mContourValues[end - 2]) { return end - 1; }
			for (int i = 0; i < end - 2; i++)
				if (useVal <= this.mContourValues[i] && useVal > this.mContourValues[i + 1]) { return i + 1; }
		}
		return 0;
	}

	public Color getColorValue(int i) {
		int ii = i < 128 ? i : 127;
		try {
			return mColorValues[ii];
		}
		catch (Exception ex) {
			// System.out.println("index=" + ii);
			// System.out.println("Value length=" + mColorValues.length);
		}
		return Color.white;
	}

	public void setColorValue(int i, Color c) {
		int ii = i < 128 ? i : 127;
		try {
			mColorValues[ii] = c;
		}
		catch (Exception ex) {
			// System.out.println("index=" + ii);
			// System.out.println("Value length=" + mColorValues.length);
		}
	}

	public ContourLineAttribute getContourAttributes(int i) {
		return mContourLineAttributes[i];
	}

	public void setContourAttributes(int i, ContourLineAttribute ca) {
		mContourLineAttributes[i] = ca;
	}

	public Color getEtopoColor(double inVal) {
		double useVal = inVal;
		if (mUseMetadata) {
			// normalize the input value--it's either a time, latitude, or longitude
			if (mMetadataType.equalsIgnoreCase(b.getString("kDateTime"))) {
				double dminval = (double) mMinDate.getTime();
				double dmaxval = (double) mMaxDate.getTime();
				useVal = (inVal - dminval) / (dmaxval - dminval);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kDateTimeMonth"))) {
				double dminval = 1.0;
				double dmaxval = 12.0;
				useVal = (inVal - dminval) / (dmaxval - dminval);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLatitude"))) {
				useVal = (inVal - mLatMin) / (mLatMax - mLatMin);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLongitude"))) {
				useVal = (inVal - mLonMin) / (mLonMax - mLonMin);
			}
		}

		int end = this.mNumColorLevels;
		if (this.mContourValues[1] > this.mContourValues[0]) { // increasing color
																													 // bar:
			if (useVal < this.mContourValues[0]) { return mColorValues[0]; }
			if (useVal >= this.mContourValues[end - 2]) { return mColorValues[end - 1]; }
			for (int i = 0; i < end - 2; i++)
				if (useVal >= this.mContourValues[i] && useVal < this.mContourValues[i + 1]) { return mColorValues[i + 1]; }
		}
		else { // decreasing color bar:
			if (useVal > this.mContourValues[0]) { return mColorValues[0]; }
			if (useVal <= this.mContourValues[end - 2]) { return mColorValues[end - 1]; }
			for (int i = 0; i < end - 2; i++)
				if (useVal <= this.mContourValues[i] && useVal > this.mContourValues[i + 1]) { return mColorValues[i + 1]; }
		}
		return JOAConstants.DEFAULT_MISSINGVAL_COLOR;
	}

	public Color getColor(double inVal) {
		if (inVal == JOAConstants.MISSINGVALUE || inVal == JOAConstants.WOCEMISSINGVALUE) { return JOAConstants.DEFAULT_MISSINGVAL_COLOR; }

		double useVal = inVal;
		if (mUseMetadata) {
			// normalize the input value--it's either a time, latitude, or longitude
			if (mMetadataType.equalsIgnoreCase(b.getString("kDateTime"))) {
				double dminval = (double) mMinDate.getTime();
				double dmaxval = (double) mMaxDate.getTime();
				useVal = (inVal - dminval) / (dmaxval - dminval);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kDateTimeMonth"))) {
				double dminval = 1.0;
				double dmaxval = 12.0;
				useVal = (inVal - dminval) / (dmaxval - dminval);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLatitude"))) {
				useVal = (inVal - mLatMin) / (mLatMax - mLatMin);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLongitude"))) {
				useVal = (inVal - mLonMin) / (mLonMax - mLonMin);
			}
		}

		int end = this.mNumColorLevels;
		if (this.mContourValues[1] > this.mContourValues[0]) { // increasing color
																													 // bar:
			if (useVal < this.mContourValues[0]) { return mColorValues[0]; }
			if (useVal >= this.mContourValues[end - 2]) { return mColorValues[end - 1]; }
			for (int i = 0; i < end - 2; i++)
				if (useVal >= this.mContourValues[i] && useVal < this.mContourValues[i + 1]) { return mColorValues[i + 1]; }
		}
		else { // decreasing color bar:
			if (useVal > this.mContourValues[0]) { return mColorValues[0]; }
			if (useVal <= this.mContourValues[end - 2]) { return mColorValues[end - 1]; }
			for (int i = 0; i < end - 2; i++)
				if (useVal <= this.mContourValues[i] && useVal > this.mContourValues[i + 1]) { return mColorValues[i + 1]; }
		}
		return JOAConstants.DEFAULT_MISSINGVAL_COLOR;
	}

	public Color getColor(GeoDate date) {
		// turn GeoDate into a value
		if (mMetadataType.equalsIgnoreCase(b.getString("kDateTimeMonth"))) {
			// get the month of this date
			int month = date.getGMTMonth();

			// normalize by 12
			double dmonth = month / 12.0;
			return this.getColor(dmonth);
		}
		else {
			double ddate = (double) date.getTime();
			return this.getColor(ddate);
		}
	}

	public int getNumLevels() {
		return mNumColorLevels;
	}

	public void setNumLevels(int nl) {
		mNumColorLevels = nl;
	}

	public double getDoubleValue(int i) {
		try {
			return mContourValues[i];
		}
		catch (Exception ex) {
			// System.out.println("index=" + i);
			// System.out.println("mContourValues length=" + mContourValues.length);
		}
		return 0.0;
	}

	public double getBaseLevel() {
		return mBaseLevel;
	}

	public double getEndLevel() {
		return mEndLevel;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getParam() {
		return mParam;
	}

	public String getDescription() {
		return mDescription;
	}

	public double[] getValues() {
		return mContourValues;
	}

	public Color[] getColors() {
		return mColorValues;
	}

	public void setBaseLevel(double base) {
		mBaseLevel = base;
	}

	public void setEndLevel(double end) {
		mEndLevel = end;
	}

	public void setTitle(String inTitle) {
		mTitle = inTitle;
	}

	public void setParam(String inParam) {
		mParam = inParam;
	}

	public void setDescription(String inDescrip) {
		mDescription = inDescrip;
	}

	public void setValues(double[] inValues) {
		mContourValues = inValues;
		setNumLevels(inValues.length);
	}

	public void setColors(Color[] inColors) {
		mColorValues = inColors;
	}

	public void setIsMetadataColorBar(boolean flag) {
		mUseMetadata = flag;
	}

	public boolean isMetadataColorBar() {
		return mUseMetadata;
	}

	public void setMetadataType(String type) {
		mMetadataType = null;
		mMetadataType = new String(type);
	}

	public String getMetadataType() {
		return mMetadataType;
	}

	public void metadataChanged(MetadataChangedEvent evt) {
		mMinDate = evt.getMinDate();
		mMaxDate = evt.getMaxDate();
		mLatMin = evt.getMinLat();
		mLatMax = evt.getMaxLat();
		mLonMin = evt.getMinLon();
		mLonMax = evt.getMaxLon();
	}

	public void setMetadata(double minlat, double maxlat, double minlon, double maxlon, GeoDate mindate, GeoDate maxdate) {
		// initialize the metadata
		mMinDate = new GeoDate(mindate);
		mMaxDate = new GeoDate(maxdate);
		mLatMin = minlat;
		mLatMax = maxlat;
		mLonMin = minlon;
		mLonMax = maxlon;
	}

	public void dumpMetadataRanges() {
		System.out.println("min Date = " + mMinDate);
		System.out.println("max Date = " + mMaxDate);
		System.out.println("min lat = " + mLatMin);
		System.out.println("max lat = " + mLatMax);
		System.out.println("min lon = " + mLonMin);
		System.out.println("max lon = " + mLonMax);
	}

	public void createTimeFormatter() {
		sdfGMT = new String(JOAConstants.DEFAULT_DATE_FORMAT);
		/*
		 * if (timeDisplayFormat == JOAConstants.DATE_TIME_DISPLAY) { sdfGMT = new
		 * String("yyyy-MM-dd"); } else if (timeDisplayFormat ==
		 * JOAConstants.YEAR_DAY_TIME_DISPLAY) { sdfGMT = new String("yyyy-DDD"); }
		 * else if (timeDisplayFormat == JOAConstants.MONTH_TIME_DISPLAY) { sdfGMT =
		 * new String("MMMMMMMMMMMMMMM"); }
		 */
		// else if (timeDisplayFormat == JOAConstants.SEASON_TIME_DISPLAY) {
		// sdfGMT = new SeasonFormatter();
		// }
		// sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public void createLatFormatter() {
		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			latDM = new LatitudeFormat();
		}
		else {
			latDM = new LatitudeFormat(JOAConstants.latFormatString);
		}
	}

	public void createLonFormatter() {
		if (JOAConstants.DEFAULT_POSITION_FORMAT == JOAConstants.DEG_MINUTES_SEC_GEO_DISPLAY) {
			lonDM = new LongitudeFormat();
		}
		else {
			lonDM = new LongitudeFormat(JOAConstants.lonFormatString);
		}
	}

	public Format createDecimalFormatter() {
		return new DecimalFormat(JOAConstants.formatString);
	}

	public void setLabelFormatter() {
		if (mUseMetadata) {
			if (mMetadataType.equalsIgnoreCase(b.getString("kLatitude"))) {
				createLatFormatter();
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLongitude"))) {
				createLonFormatter();
			}
			createTimeFormatter();
		}
	}

	public String getFormattedValue(int i) {
		return getFormattedValue(i, 0, false);
	}

	public String getFormattedValue(int i, int np, boolean flag) {
		if (mUseMetadata) {
			if (mMetadataType.equalsIgnoreCase(b.getString("kDateTime"))) {
				// turn a real back into a GeoDate
				double dminval = (double) mMinDate.getTime();
				double dmaxval = (double) mMaxDate.getTime();
				double unnormVal = mContourValues[i] * (dmaxval - dminval) + dminval;
				mGeoDate = new GeoDate((long) unnormVal);
				return "  " + mGeoDate.toString(sdfGMT);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kDateTimeMonth"))) {
				// turn a real back into a GeoDate
				double dminval = 1.0;
				double dmaxval = 12.0;
				double unnormVal = mContourValues[i] * (dmaxval - dminval) + dminval;
				switch ((int) unnormVal) {
					case 1:
						return "  " + b.getString("kJan");
					case 2:
						return "  " + b.getString("kFeb");
					case 3:
						return "  " + b.getString("kMar");
					case 4:
						return "  " + b.getString("kApr");
					case 5:
						return "  " + b.getString("kMay");
					case 6:
						return "  " + b.getString("kJun");
					case 7:
						return "  " + b.getString("kJul");
					case 8:
						return "  " + b.getString("kAug");
					case 9:
						return "  " + b.getString("kSep");
					case 10:
						return "  " + b.getString("kOct");
					case 11:
						return "  " + b.getString("kNov");
					case 12:
						return "  " + b.getString("kDec");
				}
				return "  ";
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLatitude"))) {
				double unnormVal = mContourValues[i] * (mLatMax - mLatMin) + mLatMin;
				if (latDM == null) {
					createLatFormatter();
				}
				return "  " + latDM.format((float) unnormVal);
			}
			else if (mMetadataType.equalsIgnoreCase(b.getString("kLongitude"))) {
				double unnormVal = mContourValues[i] * (mLonMax - mLonMin) + mLonMin;
				if (lonDM == null) {
					createLonFormatter();
				}
				return "  " + lonDM.format((float) unnormVal);
			}
		}
		return JOAFormulas.formatDouble(String.valueOf(mContourValues[i]), np, flag);
	}

	/**
   * @param mParamUnits the mParamUnits to set
   */
  public void setParamUnits(String mParamUnits) {
	  this.mParamUnits = mParamUnits;
  }

	/**
   * @return the mParamUnits
   */
  public String getParamUnits() {
	  return mParamUnits;
  }
}

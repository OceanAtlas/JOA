/**
 * 
 */
package javaoceanatlas.io;

import gov.noaa.pmel.eps2.Dbase;
import gov.noaa.pmel.eps2.EPIC_Key_DB;
import gov.noaa.pmel.eps2.EPSConstants;
import gov.noaa.pmel.eps2.EPSFileReader;
import gov.noaa.pmel.eps2.EPSProgressDialog;
import gov.noaa.pmel.eps2.EPSVarDoesNotExistExcept;
import gov.noaa.pmel.eps2.EpicPtr;
import gov.noaa.pmel.eps2.JOADataFile;
import gov.noaa.pmel.eps2.JOAParameter;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.IllegalTimeValue;
import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import ucar.multiarray.MultiArray;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.utility.JOAFormulas;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author oz
 * 
 */
public class WODCSVReader implements EPSConstants {
	private String mPath;
	private CastIDRule mCastRule;
	private SectionIDRule mSectionRule;
	private CastNumberRule mCastNumRule;
	private List<String[]> mCurrCast = new ArrayList<String[]>();
	private CSVReader mReader;
	private static final String TEST_FILE = "/Users/oz/Desktop/ocldb1334701071.21589.OSD.csv";
	private WODQCStandard mDestQCRule;
	private long mFileSize;
	/**
	* ID count (< 0 if variable not found in EPIC Key database
	*/
	protected int mIDCount = -1;

	/**
	* File to read
	*/
	protected File mFile;
	/**
	* Optional string for progress string
	*/
	protected String mProgressStr = "Reading JOA Binary Data...";
	
	public WODCSVReader(String inCSVFilePath, CastIDRule castRule, SectionIDRule sectionRule, 
			CastNumberRule castNumRule, WODQCStandard destQCRule,
			File inFile) throws FileNotFoundException {
		mPath = inCSVFilePath;
		mCastRule = castRule;
		mSectionRule = sectionRule;
		mCastNumRule = castNumRule;
		mDestQCRule = destQCRule;
		
		// set the file size for progress reporting
		mFileSize = getFileSize();
			
		// create a CSV reader
		mReader = new CSVReader(new FileReader(inCSVFilePath));
	}

  public long getFileSize() {
    File file = new File(mPath);
    
    if (!file.exists() || !file.isFile()) {
      System.out.println("File doesn\'t exist");
      return -1;
    }
    
    //Here we get the actual size
    return file.length();
  }
  
  public long getFileLength() {
  	return mFileSize;
  }
  
  public long getCastLength() {
  	long len = 0;
  	for (int i=0; i<mCurrCast.size(); i++) {
  		String[] sa = mCurrCast.get(i);
  		for (int j=0; j<sa.length; j++) {
  			len += sa[j].length() + 1;
  		}
  		len += 1;
  	}
  	return len;
  }
  
	public void readCast() throws Exception {
		mCurrCast.clear();
		String[] nextLine;
		try {
			while (true) {
				nextLine = mReader.readNext();
//				System.out.println(nextLine);
				if (nextLine == null) {
//					System.out.println("detected end of file");
					throw new Exception();
				}
				if (nextLine[0].indexOf("END OF VARIABLES SECTION") >= 0 ||
						nextLine[0].indexOf("END OF BIOLOGY SECTION") >= 0) {
//					System.out.println("detected end of cast");
					break;
				}

				if (!(nextLine[0].indexOf("#-") >= 0)) {
					mCurrCast.add(nextLine);
//					System.out.println("added line");
				}
			}
		}
		catch (Exception ex) {
			throw ex;
		}
	}
	
	public boolean isValidProfile() {
		return this.getTag("VARIABLES", mCurrCast) != null;
	}

	public void readCast(String lookingFor) throws Exception {
		try {
			while (true) {
				readCast();
				if (this.getTag(lookingFor, mCurrCast) != null) {
					break;
				}
			}
		}
		catch (Exception ex) {
			throw ex;
		}
	}
	
	private void readSpecificCast(String lookingFor) {
		try {
			while (true) {
				readCast();
				String[] cast = this.getTag("CAST", mCurrCast);
				
				if (cast != null && cast[2].trim().equalsIgnoreCase(lookingFor)) {
					break;
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void printCast() {
		for (String[] sa : mCurrCast) {
			for (String tok : sa) {
				System.out.print("[" + tok + "]");
			}
			System.out.println();
		}
	}

	public List<String[]> getVariableMetaData() {
		// Collect everything with VARIABLES and UNITS tags
		List<String[]> outList = new ArrayList<String[]>();

		// skip to the VARIABLES tag
		for (String[] sa : mCurrCast) {
			if (sa[0].trim().toUpperCase().indexOf("VARIABLES") == 0) {
				outList.add(sa);
			}
			else if (sa[0].trim().toUpperCase().indexOf("UNITS") == 0) {
				outList.add(sa);
			}
			else if (sa[0].trim().toUpperCase().indexOf("PROF-FLAG") == 0) {
				outList.add(sa);
				break;
			}
		}
		return outList;
	}
	
	public int getStartOfParamDataSection() {
		int c = 0;
		for (String[] sa : mCurrCast) {
			if (sa[0].trim().toUpperCase().indexOf("PROF-FLAG") == 0) {
				c++;
				break;
			}
			c++;
		}
		return c;
	}
	
	public int getEndOfParamDataSection() {
		int c = 0;
		for (String[] sa : mCurrCast) {
			if (sa[0].trim().toUpperCase().indexOf("END OF VARIABLES SECTION") == 0) {
				break;
			}
			c++;
		}
		return c;
		
	}

	public List<String[]> getBiologySection() {
		// Collect everything with BIOLOGY and END OF BIOLOGY SECTION tags
		List<String[]> outList = new ArrayList<String[]>();

		// skip to the METADATA tag
		boolean foundBioTag = false;
		for (String[] sa : mCurrCast) {
			if (!foundBioTag) {
				if (sa[0].equalsIgnoreCase("BIOLOGY")) {
					foundBioTag = true;
				}
				continue;
			}

			if (sa[0].indexOf("END OF BIOLOGY SECTION") >= 0) {
				break;
			}
			else {
				outList.add(sa);
			}
		}
		return outList;
	}
	
	public List<String[]> getBiologyMetaData() {
		// Collect everything with BIOLOGY METADATA and BIOLOGY tags
		List<String[]> outList = new ArrayList<String[]>();

		// skip to the METADATA tag
		boolean foundBioMDTag = false;
		for (String[] sa : mCurrCast) {
			if (!foundBioMDTag) {
				if (sa[0].equalsIgnoreCase("BIOLOGY METADATA")) {
					foundBioMDTag = true;
				}
				continue;
			}

			if (sa[0].indexOf("BIOLOGY") >= 0 && foundBioMDTag) {
				break;
			}
			else {
				outList.add(sa);
			}
		}
		return outList;
	}

	public List<String[]> getTaggedMetaData() {
		// Collect everything between METADATA tag and either BIOLOGY METADATA or VARIABLES
		List<String[]> outList = new ArrayList<String[]>();

		// skip to the METADATA tag
		boolean foundMetaTag = false;
		for (String[] sa : mCurrCast) {
			if (!foundMetaTag) {
				if (sa[0].equalsIgnoreCase("METADATA")) {
					foundMetaTag = true;
				}
				continue;
			}

			if (sa[0].indexOf("VARIABLES") >= 0 || sa[0].indexOf("BIOLOGY METADATA") >= 0) {
				break;
			}
			else {
				outList.add(sa);
			}
		}
		return outList;
	}
	
	public List<String[]> getCastMetaData() {
		// return the stn/cruise metadata; lon,lat,date, time, etc...
		List<String[]> outList = new ArrayList<String[]>();

		// skip to the METADATA tag
		for (String[] sa : mCurrCast) {
			if (sa[0].indexOf("METADATA") >= 0 ) {
				break;
			}
			else {
				outList.add(sa);
			}
		}
		return outList;
	}

	public String getCastID(List<String[]> stnmeta) {
		String castID = "na";
		String[] ssa = getTag("CAST", stnmeta);
		if (mCastRule == CastIDRule.ORIG_STN_ID) {
			String[] sa = getTag("Originators Station ID", stnmeta);
			if (sa == null) {
				if (ssa != null) {
					castID = ssa[2];
				}
			}
			else {
				String oCastID = sa[2].trim();
				if (oCastID.length() > 0) {
					castID = oCastID;
				}
				else {
					castID = ssa[2];
				}
			}
		}
		else if (mCastRule == CastIDRule.WOD_UNIQUE) {
			if (ssa != null) {
				castID = ssa[2];
			}
		}

		return castID.trim();
	}

	public String getSectionID(List<String[]> stnmeta) {
		//NODV Cruise ID is always defined
		String sectionID = "na";
		String NODCSectionID = "na";
		String[] sa = getTag("Originators Cruise ID", stnmeta);
		String[] ssa = getTag("NODC Cruise ID", stnmeta);
		if (ssa != null) {
			NODCSectionID = ssa[2].trim();
		}

		if (mSectionRule == SectionIDRule.ORIG_CRUISE_ID) {
			if (sa == null) {
				if (ssa != null) {
					sectionID = NODCSectionID;
				}
			}
			else {
				String origSectionID = sa[2].trim();			
				if (origSectionID.length() == 0) {
					sectionID = NODCSectionID;
				}
				else {
					sectionID = origSectionID;
				}
			}
		}
		else if (mSectionRule == SectionIDRule.NODC_CRUISE_ID) {
			if (ssa != null) {
				sectionID = NODCSectionID;
			}
		}

		return sectionID;
	}

	public int getCastNumber(List<String[]> meta) {
		int cast = 1;
		String[] sa = getTag("Cast/Tow Number", meta);
		if (sa != null && sa[2] != null) {
			try {
				cast = Integer.valueOf(sa[2]);
				return cast;
			}
			catch (Exception ex) {
				if (mCastNumRule == CastNumberRule.JOA_SUBSTITUTION) {
					return 1;
				}
				else {
					return -99;
				}
			}
		}
		else {
			if (mCastNumRule == CastNumberRule.JOA_SUBSTITUTION) {
				return 1;
			}
			else {
				return -99;
			}
		}
	}
	
	public String getPlatform(List<String[]> meta) {
		String platform = "na";
		String[] sa = getTag("Platform", meta);
		if (sa != null && sa[4] != null) {
				platform = sa[4];
		}
		return platform;
	}
	
	public String[] getTag(String tag, List<String[]> inList) {
		for (String[] sa : inList) {
			if (sa[0].trim().toLowerCase().indexOf(tag.toLowerCase()) == 0) {
				return sa;
			}
		}
		return null;
	}

	public double getLatitude(List<String[]> stnmeta) {
		try {
			String[] sa = getTag("Latitude", stnmeta);
			return Double.valueOf(sa[2]);
		}
		catch (Exception ex) {
			return JOAConstants.MISSINGVALUE;
		}
	}

	public double getLongitude(List<String[]> stnmeta) {
		try {
			String[] sa = getTag("Longitude", stnmeta);
			return Double.valueOf(sa[2]);
		}
		catch (Exception ex) {
			return JOAConstants.MISSINGVALUE;
		}
	}

	public GeoDate getCastDate(List<String[]> stnmeta) {
		int month = 0, day = 0, year = 0, hour = 0, min = 0, sec = 0;
		
		for (String[] sa : stnmeta) {
			if (sa[0].indexOf("Year") >= 0) {
				year = Integer.valueOf(sa[2].trim());
			}
			if (sa[0].indexOf("Month") >= 0) {
				month = Integer.valueOf(sa[2].trim());
			}
			if (sa[0].indexOf("Day") >= 0) {
				day = Integer.valueOf(sa[2].trim());
			}
			if (sa[0].indexOf("Time") >= 0) {
				// get the units
				String timeUnits = sa[3].trim();
				if (timeUnits != null && timeUnits.length() > 0 && timeUnits.indexOf("decimal hours") >= 0) {
					double decHr = Double.valueOf(sa[2]);
					hour = (int)(decHr);
					double minsRemainder = decHr - (double)hour;
					double decMin = minsRemainder * 60.0;
					min = (int)(decMin);
					double secsRemainder = decMin - (double)min;
					sec = (int)(secsRemainder * 60.0);
				}
				else {
					// todo other time formats
				}
			}	
		}
		try {
	    return new GeoDate(month, day, year, hour, min, sec, 0);
    }
    catch (IllegalTimeValue e) {
    	System.out.println("Illegal value in date");
	    return null;
    }
	}

	public List<String[]> getParametersWithScale(List<String[]> meta) {
		// the scale can be used to determine temperature and salinity scales
		List<String[]> al = new ArrayList<String[]>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("scale") >= 0) {
				al.add(sa);
			}
		}
		return al;
	}
	
	public TemperatureScale getTempScale(List<String[]> paramsWithScale) {
		if (paramsWithScale == null || paramsWithScale.size() == 0) {
			return TemperatureScale.UNKNOWN;
		}
		for (String[] sa : paramsWithScale) {	
			if (sa[1].toLowerCase().indexOf("temperature") >= 0) {				

				// has temperature scale: decode it
				try {
					double code = Double.parseDouble(sa[2]);
					if (code == 103.0) {
						return TemperatureScale.ITS90;
					}
					else if (code == 102.0) {
						return TemperatureScale.T68;
					}
				}
				catch (Exception ex) {
					return TemperatureScale.UNKNOWN;
				}
			}
		}
		return TemperatureScale.UNKNOWN;
	}
	
	public List<String> getSurfaceParameters(List<String[]> meta) {
		return null;
	}
	
	public List<String> getWODParameters(List<String[]> meta, ConvertParameterNamesRule convertNames) {
		List<String> al = new ArrayList<String>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("variables") == 0) {
				// parse this list
				for (int i=1; i<sa.length; i+=3) {
					if (sa[i].length() > 0) {
						if (convertNames == ConvertParameterNamesRule.CONVERT_TO_JOA_LEXICON) {
							al.add(JOAFormulas.paramNameToJOAName(sa[i]));
						}
						else {
							al.add(sa[i]);
						}
					}
				}
			}
		}
		return al;
	}
	
	public List<Integer> getProfileFlags(List<String[]> meta) {
		List<Integer> al = new ArrayList<Integer>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("prof-flag") == 0) {
				// parse this list
				for (int i=2; i<sa.length; i+=3) {
					if (sa[i].length() > 0) {
							al.add(Integer.valueOf(sa[i].trim()));
					}
					else {
						al.add(-99);
					}
				}
			}
		}
			return al;
	}
	
	public List<String> getWODAssignedUnits(List<String[]> meta) {
		List<String> al = new ArrayList<String>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("units") == 0) {
				// parse this list
				for (int i=1; i<sa.length; i+=3) {
					if (sa[i].length() > 0) {
						al.add(sa[i].trim());
					}
				}
			}
		}
		return al;
	}
	
	public int getParamPos(String param, List<String> parameters, boolean translate) {
		int count = 0;
		
		for (int i=0; i<parameters.size(); i++) {
			String p = parameters.get(i);
			if (translate) {
				p = JOAFormulas.paramNameToJOAName(p);
			}
			if (p.toLowerCase().indexOf(param.toLowerCase()) >= 0) {
				return count;
			}
			count++;
		}
		return -99;
	}
		
	public String getDepthUnits(List<String> parameters, List<String> units) {
		int depthPos = getParamPos("depth", parameters, false);
		if (depthPos >= 0) {
			return units.get(depthPos);
		}
		return null;
	}
	
	public int getPressurePos(List<String> parameters, List<String> units) {
		int count = 0;
		for (int i=0; i<parameters.size(); i++) {
			String p = parameters.get(i);
			String u = units.get(i);
			if (p.toLowerCase().indexOf("press") >= 0 && u.toLowerCase().indexOf("db") >= 0) {
				return count;
			}
			count++;
		}
		return -99;
	}
	
	public List<String[]> getDataSection() {
		// everything between "Prof-Flag" and "END OF VARIABLES SECTION"
		List<String[]> al = new ArrayList<String[]>();
		int start = this.getStartOfParamDataSection();
		int end = this.getEndOfParamDataSection();
		
		for (int i=start; i<end; i++) {
			al.add(mCurrCast.get(i));
		}
		return al;
	}
	
	public int[] getObsFlags(String[] ts) {
		int numFlags = ts.length/3;
		int[] flags = new int[numFlags];
		for (int i=0; i<numFlags; i++) {
			String iToken = ts[1+1+(i*3)];
			try {
				int fVal = Integer.valueOf(iToken);
				flags[i] = fVal;
			}
			catch (Exception ex) {
			// silent: this catches blanks used for missing values
				flags[i] = JOAConstants.MISSINGVALUE;
			}
		}
		return flags;
	}
	
	public int[] getOriginatorsFlags(String[] ts) {
		int numFlags = ts.length/3;
		int[] flags = new int[numFlags];
		for (int i=0; i<numFlags; i++) {
			String iToken = ts[1+2+(i*3)];
			try {
				int fVal = Integer.valueOf(iToken);
				flags[i] = fVal;
			}
			catch (Exception ex) {
				// silent: this catches blanks used for missing values
				flags[i] = JOAConstants.MISSINGVALUE;
			}
		}
		return flags;
	}
	
	public double[] getParamValues(String[] ts) {
		int numVals = ts.length/3;
		double[] vals = new double[numVals];
		for (int i=0; i<numVals; i++) {
			String dToken = ts[1+(i*3)];
			try {
				double dVal = Double.valueOf(dToken);
				vals[i] = dVal;
			}
			catch (Exception ex) {
				// silent: this catches dashes used for missing values
				vals[i] = JOAConstants.MISSINGVALUE;
			}
		}
		return vals;	
	}
	
	public Bottle[] getParameterData(int bc, Station sh, Section sech, DepthConversionRule dcr, PreferPressureParameterRule pppr,
			int depthPos, String depthUnits, int pressurePos, WODQCStandard origSrcQCStandard,
			TemperatureScale tempScale, TempConversionRule tempConvRule, int tempPos) {
			
//			WODQCStandard srcQCStandard, 
//			WODQCStandard destQC, Map<String, UnitsConverter> unitsConv, Map<String, ParameterValueScaler> scaler) {
		
	/*	VARIABLES ,Depth     ,F,O,Temperatur,F,O,Salinity  ,F,O,Oxygen    ,F,O,Phosphate ,F,O,Silicate  ,F,O,Nitrate   ,F,O,pH        ,F,O,Alkalinity,F,O,,
		UNITS     ,m         , , ,degrees C , , ,PSS       , , ,ml/l      , , ,umol/l    , , ,umol/l    , , ,umol/l    , , ,(n/a)     , , ,meq/l     , , ,,
		Prof-Flag ,          ,0, ,          ,5, ,          ,0, ,          ,0, ,          ,0, ,          ,0, ,          ,0, ,          ,0, ,          ,1, ,,
		         1,        9.,0, ,     -1.78,0, ,     33.65,0, ,      7.93,0, ,      0.62,0, ,       5.7,0, ,       6.3,0, ,      7.86,0, ,     2.271,0, ,
		         2,       20.,0, ,     -1.78,0, ,     33.66,0, ,      7.89,0, ,      0.63,0, ,       5.7,0, ,       6.5,0, ,      7.86,0, ,     2.271,0, ,
		         3,       30.,0, ,     -1.78,0, ,     33.90,0, ,      7.77,0, ,      0.63,0, ,       5.4,0, ,       7.3,0, ,   ---.---,0, ,   ---.---,0, ,
		         4,       39.,0, ,     -1.78,0, ,       34.,0, ,      7.71,0, ,      0.63,0, ,       5.4,0, ,       7.7,0, ,      7.87,0, ,     2.273,0, ,
		         5,       50.,0, ,     -1.79,0, ,     34.07,0, ,      7.67,0, ,      0.65,0, ,       5.3,0, ,       8.2,0, ,   ---.---,0, ,   ---.---,0, ,
		         6,       59.,0, ,     -1.78,0, ,     34.09,0, ,      7.67,0, ,      0.65,0, ,       5.3,0, ,       8.3,0, ,      7.88,0, ,     2.272,0, ,
		         7,       81.,0, ,     -1.82,0, ,     34.18,0, ,      7.65,0, ,      0.66,0, ,       5.1,0, ,       8.7,0, ,   ---.---,0, ,   ---.---,0, ,
		         8,      100.,0, ,     -1.83,0, ,     34.23,0, ,      7.59,0, ,      0.69,0, ,       5.1,0, ,       9.4,0, ,      7.88,0, ,     2.271,0, ,
		         9,      149.,0, ,     -1.75,0, ,     34.29,0, ,      7.57,0, ,      0.71,0, ,       5.1,0, ,      10.0,0, ,      7.88,0, ,     2.270,0, ,
		        10,      199.,0, ,     -1.69,0, ,     34.34,0, ,      7.55,0, ,      0.73,0, ,       5.2,0, ,      10.3,0, ,      7.88,0, ,     2.271,0, ,
		        11,      249.,0, ,     -0.35,0, ,     34.56,0, ,      7.20,0, ,      0.78,0, ,       5.9,0, ,      11.4,0, ,      7.88,0, ,     2.282,0, ,
		        12,      301.,0, ,      1.04,0, ,     34.84,0, ,      6.91,0, ,      0.84,0, ,       6.0,0, ,      12.4,0, ,      7.88,0, ,     2.295,0, ,
		        13,      401.,0, ,      1.00,0, ,     34.91,0, ,      6.93,0, ,      0.85,0, ,       5.9,0, ,      12.8,0, ,      7.88,0, ,     2.301,0, ,
		        14,      471.,0, ,      0.67,0, ,     34.91,0, ,      6.97,0, ,      0.86,0, ,       6.0,0, ,      12.7,0, ,      7.88,0, ,     2.301,0, ,
		        15,      520.,0, ,      0.43,0, ,     34.91,0, ,      7.01,0, ,      0.86,0, ,       6.1,0, ,      12.8,0, ,      7.88,0, ,     2.301,0, ,*/
		 
		List<String[]> dataSec = getDataSection();
		int numBottles = dataSec.size();
		int numParams = dataSec.get(0).length/3;	
		Bottle[] bots = new Bottle[numBottles];
		int startBottleord = bc;
		WODQCStandard srcQCStandard;
		
		if (origSrcQCStandard == WODQCStandard.NONE) {
			srcQCStandard = WODQCStandard.NODC;
		}
		else {
			srcQCStandard = origSrcQCStandard;
		}
		
		// get the QC Translator
		QCTranslator translator = WODQCStandard.translatorFromString(srcQCStandard.name());
		
		// get the temperature scaler
		ValueScaler scaler = TemperatureScale.scalerFromString(tempScale.name());
		
		for (int i=0; i<dataSec.size(); i++) {
			String[] obsv = dataSec.get(i);
			int[] flags = getObsFlags(obsv);
			int[] oflags = getOriginatorsFlags(obsv);
			double[] vals = getParamValues(obsv);

			Bottle bh = new Bottle(startBottleord++, numParams, sh, sech);
			for (int b=0; b<numParams; b++) {
				if (vals[b] == JOAConstants.MISSINGVALUE || JOAFormulas.isMissing(vals[b])) {
					bh.mDValues[b] = JOAConstants.MISSINGVALUE;
				}
				else {
					float val = (float) vals[b];
					if (b == tempPos && tempConvRule == TempConversionRule.ITS90_TO_IPTS68 &&
							tempScale == TemperatureScale.ITS90) {
						val = scaler.scale(val);
					}
					if (b == depthPos) {
						if (pppr == PreferPressureParameterRule.PREFER_PRESSURE_PARAMETER && pressurePos >= 0) {
							val = (float) vals[pressurePos];
						}
						else if (dcr == DepthConversionRule.CONVERT_DEPTH_TO_PRESSURE ) {
							// convert depth to pressure
							if (depthUnits.equalsIgnoreCase("m")) {
								val = (float)JOAFormulas.zToPres(val);
							}
						}
					}
					
					// QC processing
					int nodcFlag = flags[b];
					int origFlag = oflags[b];
					int flag = -99;
					if (srcQCStandard == WODQCStandard.NODC) {
						flag = nodcFlag;
					}
					else {
						flag = origFlag;
					}
					
					bh.mQualityFlags[b] = (short)translator.translate(flag, mDestQCRule);
					bh.mDValues[b] = val;
				}
				bots[i] = bh;
			}
			
		} 
		return bots;
	}
	
	public Station getStation() {
		return null;
	}
	
	public Section getSection() {
		return null;
	}
	
	public WODQCStandard getOriginatorsQCConvention(List<String[]> meta) {
		try {
			String[] sa = getTag("Originators flag set to use", meta);
			double code =  Double.valueOf(sa[2]);
			return WODQCStandard.fromIntCode((int)code);
		}
		catch (Exception ex) {
			//silent;
		}
		return WODQCStandard.NONE;
	}

	public List<String[]> getParametersWithAdjustments(List<String[]> meta) {
		// these are constants added to a parameters values
		List<String[]> al = new ArrayList<String[]>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("adjustment") > 0) {
				al.add(sa);
			}
		}
		return al;
	}

	public List<String[]> getUncalibratedParameters(List<String[]> meta) {
		// optionally set a QC value for parameters tagged with uncalibrated
		List<String[]> al = new ArrayList<String[]>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("uncalibrated") > 0) {
				al.add(sa);
			}
		}
		return al;
	}

	public List<String[]> getParametersWithOriginalUnits(List<String[]> meta) {
		List<String[]> al = new ArrayList<String[]>();
		for (String[] sa : meta) {
			if (sa[0].toLowerCase().indexOf("original units") > 0) {
				al.add(sa);
			}
		}
		return al;
	}

	public void printArrayList(String tag, List<String[]> list) {
		System.out.println("<" + tag.toUpperCase() + ">");
		for (String[] sa : list) {
			for (String tok : sa) {
				System.out.print("[" + tok + "]");
			}
			System.out.println();
		}
		System.out.println("<//" + tag.toUpperCase() + ">");
	}

	public void printSimpleList(String tag, List<String> list) {
		System.out.println("<" + tag.toUpperCase() + ">");
		for (String str : list) {
				System.out.print("[" + str + "]");
			System.out.println();
		}
		System.out.println("<//" + tag.toUpperCase() + ">");
	}

	public void printIntegerList(String tag, List<Integer> list) {
		System.out.println("<" + tag.toUpperCase() + ">");
		for (Integer ival : list) {
				System.out.print("[" + ival + "]");
			System.out.println();
		}
		System.out.println("<//" + tag.toUpperCase() + ">");
	}
	
	public double getBottomDepth(List<String[]> inList) {
		double retVal = JOAConstants.MISSINGVALUE;
		try {
			String[] sa = getTag("bottom depth", inList);
			if (sa != null) {
				retVal = Double.valueOf(sa[2]);
			}
		}
		catch (Exception ex) {
			//silent--just return a missing value
		}

		return retVal;
	}

	public static void main(String[] args) throws IOException {		
		WODCSVReader reader = new WODCSVReader(TEST_FILE, CastIDRule.ORIG_STN_ID, SectionIDRule.ORIG_CRUISE_ID,
				CastNumberRule.CAST_TOW_ONLY, WODQCStandard.WOCE, null);
		long startTime = System.currentTimeMillis();
		try {
	    reader.readCast();
    }
    catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
		// reader.printCast();

		try {
	    reader.readCast("");
    }
    catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
		// reader.printCast();
		
		// skip to a cast that has this tag
		try {
	    reader.readCast("VARIABLES");
    }
    catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
//		reader.readSpecificCast("9098576");
		
		// read the metadata that identify the cast
		List<String[]> stnmeta = reader.getCastMetaData();
		reader.printArrayList("Stn Metadata", stnmeta);
		
		System.out.println("Cast ID = " + reader.getCastID(stnmeta));
		System.out.println("Section ID = " + reader.getSectionID(stnmeta));	
		System.out.println("Lat = " + reader.getLatitude(stnmeta));
		System.out.println("Lon = " + reader.getLongitude(stnmeta));
		System.out.println("Cast Date = " + reader.getCastDate(stnmeta).toString());

		// 
		List<String[]> meta = reader.getTaggedMetaData();
		reader.printArrayList("Tagged Metadata", meta);
		
		System.out.println("BD =" + reader.getBottomDepth(meta));
		
		List<String[]> varSection = reader.getVariableMetaData();
		reader.printArrayList("Variable Metadata", varSection);
		
		List<String> params = reader.getWODParameters(varSection, ConvertParameterNamesRule.KEEP_WOD_PARAMETER_NAMES);
		reader.printSimpleList("Parameters", params);
		
		List<String> units = reader.getWODAssignedUnits(varSection);
		reader.printSimpleList("Units", units);
		
		List<Integer> flags = reader.getProfileFlags(varSection);
		reader.printIntegerList("Profile Flags", flags);
		
		System.out.println("Pressure Pos = " + reader.getPressurePos(params, units));
		System.out.println("Depth Units = " + reader.getDepthUnits(params, units));

		System.out.println(System.currentTimeMillis() - startTime);
		// System.out.println("\n\nGenerated CSV File:\n\n");
		// System.out.println(sw.toString());
	}
}

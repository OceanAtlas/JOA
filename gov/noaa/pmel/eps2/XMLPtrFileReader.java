package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;
import org.xml.sax.*;
import gov.noaa.pmel.util.GeoDate;
/**
 * <code>XMLPtrFileReader</code> defines the write read for ingesting pointers in XML format.
 *
 * @author oz
 * @version 1.0
 */
 
public class XMLPtrFileReader implements PtrFileReader {
	private static int NO_KEY = 0;
	private static int LAT_KEY = 3;
	private static int LON_KEY = 4;
	private static int VERTICAL_KEY = 5;
	private static int TIME_KEY = 6;
	private static int DATE_KEY = 7;
	private static int DELTA_T_KEY = 8;
	private static int KEY_STATE = NO_KEY;
	private static boolean TRACE = false;
	private static File mFile;
	XMLNotifyStr xmlNotifyStr;
	private static PointerFileAttributes mAttributes;
	private static ArrayList mFileSets = new ArrayList();
	
	public XMLPtrFileReader(File inFile) {
		mFile = inFile;
	}
	
	public ArrayList parse() {
    	try {
	    	Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
	    	org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
	    	xmlNotifyStr = new XMLNotifyStr();
	    	parser.setDocumentHandler(xmlNotifyStr);
	    	parser.parse(mFile.getPath());
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
		return null;
	}
	
	public void dumpParsedFile() {
		xmlNotifyStr.dumpParsedFile();
	}
	
	private static class XMLNotifyStr extends HandlerBase {
		boolean mFoundFirstFS = false;
		String mCurrFSID = null;
		String mCurrFSURI = null;
		String vname = null;
		String vunits = null;
		String vlexicon = null;
		String varRef = null;
		String lexicon = null;
		ArrayList mGlobalVarlist = new ArrayList();
		ArrayList mCurrStns;
		ArrayList mStnCalcsList;
		ArrayList mCurrFSVarList;
		ArrayList mCurrStnVarList;
		ArrayList mDomainLats = new ArrayList();
		ArrayList mDomainLons = new ArrayList();
		ArrayList mDomainVerticals = new ArrayList();
		ArrayList mDomainTimesOrDates = new ArrayList();
		double mStnDeltaT;
		String mStnDeltaTUnits;
		boolean mInStation = false;
		boolean mInFileSet = false;
		boolean mInDomain = false;
		boolean mTopLevelDomainFound = false;
		String mVersion;
		String mType;
		String mGlobalLexicon;
		String mGlobalURI;
		String mTimeLocation;
		String mTimeUnits;
		String mDateLocation;
		String mDateYear;
		String mDateMonth;
		String mDateDay;
		String mDateHour;
		String mDateMinutes;
		String mDateSeconds;
		String mLatLocation;
		String mLatUnits;
		String mLonLocation;
		String mLonUnits;
		String mVerticalLocation;
		String mVerticalUnits;
		String mVerticalPositive;
		ExportLatitude mExpLatitude;
		ExportLongitude mExpLongitude;
		ExportVertical mExpVertical;
		ExportTime mExpTime;
		ExportDate mExpDate;
		ExportFileSet mCurrFS;
		ExportStation mCurrStn;
		ArrayList mStnLats = new ArrayList();
		ArrayList mStnLons = new ArrayList();
		ArrayList mStnVerticals = new ArrayList();
		ArrayList mStnTimesOrDates = new ArrayList();
		int mStnFormat;
		String mStnID = null;
		String mStnCast = null;
		String mStnURI = null;
		double mStnBottom = -99;
		String mStnRef = null;
		String mStnDelatT = null;
		int mNumProfiles = 0;
		int mNumTimeSeries = 0;
		int mNumTracks = 0;
		int mNumGrids = 0;
		
		public void startDocument() throws SAXException {
		}
		
		public void startElement(String name, AttributeList amap) throws SAXException {
			if (TRACE)
				System.out.println("startElement " + name);
			if (name.equals("epicxml")) {
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("version")) {
							mVersion = amap.getValue(i);
						}
						else if (amap.getName(i).equals("type")) {
							mType = amap.getValue(i);
						if (TRACE)
							System.out.println("mType = " + mType);
						}
						else if (amap.getName(i).equals("lexicon")) {
							mGlobalLexicon = amap.getValue(i);
						}
						if (amap.getName(i).equals("uri")) {
							mGlobalURI = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}			
			}
			else if (name.equals("domain")) {
				mInDomain = true;
				if (!mTopLevelDomainFound)
					mTopLevelDomainFound = true;
			}
			else if (name.equals("latitude")) {
				KEY_STATE = LAT_KEY;
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("location")) {
							mLatLocation = amap.getValue(i);
						}
						else if (amap.getName(i).equals("units")) {
							mLatUnits = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("longitude")) {
				KEY_STATE = LON_KEY;
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("location")) {
							mLonLocation = amap.getValue(i);
						}
						else if (amap.getName(i).equals("units")) {
							mLonUnits = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("vertical")) {
				KEY_STATE = VERTICAL_KEY;
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("location")) {
							mVerticalLocation = amap.getValue(i);
						}
						else if (amap.getName(i).equals("units")) {
							mVerticalUnits = amap.getValue(i);
						}
						else if (amap.getName(i).equals("positive")) {
							mVerticalPositive = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("time")) {
				KEY_STATE = TIME_KEY;
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("location")) {
							mTimeLocation = amap.getValue(i);
						}
						else if (amap.getName(i).equals("units")) {
							mTimeUnits = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("date")) {
				KEY_STATE = DATE_KEY;
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("location")) {
							mDateLocation = amap.getValue(i);
						}
						else if (amap.getName(i).equals("year")) {
							mDateYear = amap.getValue(i);
						}
						else if (amap.getName(i).equals("month")) {
							mDateMonth = amap.getValue(i);
						}
						else if (amap.getName(i).equals("day")) {
							mDateDay = amap.getValue(i);
						}
						else if (amap.getName(i).equals("hour")) {
							mDateHour = amap.getValue(i);
						}
						else if (amap.getName(i).equals("min")) {
							mDateMinutes = amap.getValue(i);
						}
						else if (amap.getName(i).equals("secs")) {
							mDateSeconds = amap.getValue(i);
						}
						
						// make an ExportDate
						int yr = 0;
						int mnth = 0;
						int day = 0;
						int hr = 0;
						int min = 0;
						double secs = 0.0;
						
						if (mDateYear != null)
							yr = Integer.valueOf(mDateYear).intValue();
						if (mDateMonth != null)
							mnth = Integer.valueOf(mDateMonth).intValue();
						if (mDateYear != null)
							day = Integer.valueOf(mDateDay).intValue();
						if (mDateHour != null)
							hr = Integer.valueOf(mDateHour).intValue();
						if (mDateMinutes != null)
							min = Integer.valueOf(mDateMinutes).intValue();
						if (mDateSeconds != null)
							secs = Double.valueOf(mDateSeconds).doubleValue();
						mExpDate = new ExportDate(yr, mnth, day, hr, min, secs, mDateLocation);
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("deltat")) {
				KEY_STATE = DELTA_T_KEY;
				try {
					if (amap.getName(0).equals("units")) {
						mStnDeltaTUnits = amap.getValue(0);
					}
				}
				catch (Exception ex) {
					mStnDeltaTUnits = "error";
				}
			}
			else if (name.equals("variableref")) {
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("name")) {
							varRef = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("variable")) {
				vname = null;
				vunits = null;
				vlexicon = null;
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("name")) {
							vname = amap.getValue(i);
						}
						else if (amap.getName(i).equals("units")) {
							vunits = amap.getValue(i);
						}
						else if (amap.getName(i).equals("lexicon")) {
							vlexicon = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
			}
			else if (name.equals("fileset")) {
				mFoundFirstFS = true;
				mCurrFSID = null;
				mCurrFSURI = null;
				mCurrStns = new ArrayList();
				mCurrFSVarList = new ArrayList();
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("id")) {
							mCurrFSID = amap.getValue(i);
						}
						else if (amap.getName(i).equals("uri")) {
							mCurrFSURI = amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
				mInFileSet = true;
			}
			else if (name.equals("station")) {
				mCurrStnVarList = new ArrayList();
				mStnVerticals = new ArrayList();
				mStnID = null;
				mStnCast = null;
				mStnURI = null;
				mStnBottom = -99;
				mStnRef = null;
				mStnDelatT = null;
				mStnDeltaTUnits = null;
				mStnLats = new ArrayList();
				mStnLons = new ArrayList();
				mStnTimesOrDates = new ArrayList();
				for (int i=0; i<amap.getLength(); i++) {
					try {
						if (amap.getName(i).equals("id")) {
							mStnID = amap.getValue(i);
						}
						if (amap.getName(i).equals("cast")) {
							mStnCast = amap.getValue(i);
						}
						else if (amap.getName(i).equals("uri")) {
							mStnURI = amap.getValue(i);
						}
						else if (amap.getName(i).equals("bottom")) {
							mStnBottom =  Double.valueOf(amap.getValue(i)).doubleValue();
						}
						else if (amap.getName(i).equals("reference")) {
							mStnRef =  amap.getValue(i);
						}
					}
					catch (Exception ex) {
					}
				}
				mStnFormat = EPSConstants.PROFILEPTRS;
				if (mType.equalsIgnoreCase("profile")) {
					mStnFormat = EPSConstants.PROFILEPTRS;
					mNumProfiles++;
				}
				if (mType.equalsIgnoreCase("time-series")) {
					mStnFormat = EPSConstants.TSPTRS;
					mNumTimeSeries++;
				}
				mInStation = true;
				if (TRACE)
					System.out.println("mStnFormat = " + mStnFormat);
			}
			else if (name.equals("grid")) {
				mNumGrids++;
			
			}
			else if (name.equals("track")) {
				mNumTracks++;
			}
			else 
				KEY_STATE = NO_KEY;
		}
		
		public void characters(char[] ch, int start, int len) throws SAXException {
			String strVal = new String(ch, start, len);
			if (TRACE)
				System.out.println("characters, keystate = " + KEY_STATE + " strVal = " + strVal);
			if (KEY_STATE == LAT_KEY) {
				try {
					double lat = Double.valueOf(strVal).doubleValue();
					
					// create an ExportLatitude
					mExpLatitude = new ExportLatitude(lat, mLatLocation, mLatUnits);
				}
				catch (Exception ex) {
				}
			}
			else if (KEY_STATE == LON_KEY) {
				try {
					double lon = Double.valueOf(strVal).doubleValue();
					
					// create an ExportLongitude
					mExpLongitude = new ExportLongitude(lon, mLonLocation, mLonUnits);
				}
				catch (Exception ex) {
				}
			}
			else if (KEY_STATE == VERTICAL_KEY) {
				try {
					double vert = Double.valueOf(strVal).doubleValue();
					
					// create an ExportLongitude
					mExpVertical = new ExportVertical(vert, mVerticalLocation, mVerticalUnits, mVerticalPositive);
				}
				catch (Exception ex) {
				}
			}
			else if (KEY_STATE == TIME_KEY) {
				try {
					;//mMinT = new GeoDate(strVal);
				}
				catch (Exception ex) {
					;//mMinT = new GeoDate(strVal);
				}
			}
			else if (KEY_STATE == DELTA_T_KEY) {
				try {
					mStnDeltaT = Double.valueOf(strVal).doubleValue();
				}
				catch (Exception ex) {
					mStnDeltaT = 0.0;
				}
			}
		}
		
		public void endElement(String name) throws SAXException {
			if (TRACE)
			if (name.equals("epicxml")) {
				mAttributes = new PointerFileAttributes(mFile.getName(), mGlobalURI, mType, mDomainLats, mDomainLons, mDomainVerticals, 
								mDomainTimesOrDates, mGlobalVarlist);
			}
			else if (name.equals("fileset")) {
				if (TRACE) {
					
				}
				// add current stations ArrayList and variables to current FileSet
				mFileSets.add(new ExportFileSet(mCurrFSID, mCurrFSURI, mCurrFSVarList, mCurrStns));
				
				mInFileSet = false;
			}
			else if (name.equals("domain")) {
					mInDomain = false;
			}
			else if (name.equals("latitude")) {
				if (mInDomain)
					mDomainLats.add(mExpLatitude);
				else
					mStnLats.add(mExpLatitude);
			}
			else if (name.equals("longitude")) {
				if (mInDomain)
					mDomainLons.add(mExpLongitude);
				else
					mStnLons.add(mExpLongitude);
			}
			else if (name.equals("vertical")) {
				if (mInDomain)
					mDomainVerticals.add(mExpVertical);
				else
					mStnVerticals.add(mExpVertical);
			}
			else if (name.equals("date")) {
				if (mInDomain)
					mDomainTimesOrDates.add(mExpDate);
				else
					mStnTimesOrDates.add(mExpDate);
			}
			else if (name.equals("time")) {
				if (mInDomain)
					mDomainTimesOrDates.add(mExpTime);
				else
					mStnTimesOrDates.add(mExpTime);
			}
			else if (name.equals("variable")) {
				// if no global varlist, add current varlist to station or fileset
				ExportVariable evar = new ExportVariable(vname, vunits, vlexicon);
				if (mInFileSet)
					// add variable to the fileset varlist
					mCurrFSVarList.add(evar);
				else if (mInStation)
					// add variable to the fileset varlist
					mCurrStnVarList.add(evar);
				else
					// add to the global varlist
					mGlobalVarlist.add(evar);
			}
			else if (name.equals("variableref")) {
				// look up variable ref in global vars and set vname, vunits, and vlexicon
				Iterator gvitor = mGlobalVarlist.iterator();
				while (gvitor.hasNext()) {
					ExportVariable var = (ExportVariable)gvitor.next();
					if (varRef.equalsIgnoreCase(var.getVarName())) {
						vname = var.getVarName();
						vunits = var.getVarUnits();
						vlexicon = var.getLexicon();
						break;
					}
				}
				
				// add this expanded variable to either a station or section
				ExportVariable evar = new ExportVariable(vname, vunits, vlexicon);
				if (mInFileSet)
					// add variable to the fileset varlist
					mCurrFSVarList.add(evar);
				else if (mInStation)
					// add variable to the fileset varlist
					mCurrStnVarList.add(evar);
			}
			else if (name.equals("station")) {
				// construct an export station
				if (mStnFormat == EPSConstants.PROFILEPTRS) {
					if (TRACE)
						System.out.println("Stn is Profile");
					mCurrStn = new ExportStation(mStnFormat, "XML Import", "profile", "cruise", mStnID, mStnCast, (ExportLatitude)mStnLats.get(0), 
							(ExportLongitude)mStnLons.get(0), mStnTimesOrDates.get(0), mStnVerticals, mCurrStnVarList, mStnRef, mStnURI, null, mStnBottom);
				}
				else if (mStnFormat == EPSConstants.TSPTRS) {
					if (TRACE) {
						System.out.println("Stn is Time Series");
						System.out.println("Stn Vertical = " + mStnVerticals.get(0));
						System.out.println("Stn Latitude = " + mStnLats.get(0));
						System.out.println("Stn Longitude = " + mStnLons.get(0));
						System.out.println("mStnTimesOrDates = " + mStnTimesOrDates);
						System.out.println("mDeltaT = " + mStnDeltaT);
						System.out.println("mStnDeltaTUnits = " + mStnDeltaTUnits);
						System.out.println("mCurrStnVarList = " + mCurrStnVarList);
						System.out.println("mStnRef = " + mStnRef);
						System.out.println("mStnURI = " + mStnURI);
					}
					mCurrStn = new ExportStation(mStnFormat, "XML Import", "time series", (ExportVertical)mStnVerticals.get(0),(ExportLatitude) mStnLats.get(0), 
							(ExportLongitude)mStnLons.get(0), mStnTimesOrDates, mStnDeltaT, mStnDeltaTUnits, mCurrStnVarList, mStnRef, mStnURI);
				}		
				// add mCurrStn to mCurrStns
		        mCurrStns.add(mCurrStn);
		        
				mInStation = false;
			}
		}
		
		public void dumpParsedFile() {
			// first dump the pointer file attributes
			System.out.println("Domain");
			ArrayList lats = mAttributes.getLats();
			Iterator itor = lats.iterator();
			while (itor.hasNext()) {
				ExportLatitude elat = (ExportLatitude)itor.next();
				System.out.println(elat.toString(1));
			}
			ArrayList lons = mAttributes.getLons();
			itor = lons.iterator();
			while (itor.hasNext()) {
				ExportLongitude elon = (ExportLongitude)itor.next();
				System.out.println(elon.toString(1));
			}
			
			ArrayList verts = mAttributes.getVerticals();
			itor = verts.iterator();
			while (itor.hasNext()) {
				ExportVertical vert = (ExportVertical)itor.next();
				System.out.println(vert.toString(1));
			}
			
			ArrayList timesdates = mAttributes.getTimesOrDates();
			itor = timesdates.iterator();
			while (itor.hasNext()) {
				try {
					ExportTime time = (ExportTime)itor.next();
					System.out.println(time.toString(1));
				}
				catch (ClassCastException ccex) {
					ExportDate date = (ExportDate)itor.next();
					System.out.println(date.toString(1));
				}
			}
			
			// dump any global variables
			if (mAttributes.getVarList().size() > 0) {
				System.out.println("Global Variables");
				ArrayList vars = mAttributes.getVarList();
				itor = vars.iterator();
				while (itor.hasNext()) {
					ExportVariable var = (ExportVariable)itor.next();
					System.out.println(var.toString(1));
				}
			}
			
			// dump any global paths
			if (mAttributes.getPath() != null) {
				System.out.println("Global URI");
				System.out.println("\t" + mAttributes.getPath());
			}
			
			// loop on the filesets
			System.out.println("FileSets");
			itor = mFileSets.iterator();
			while (itor.hasNext()) {
				ExportFileSet fs = (ExportFileSet)itor.next();
				System.out.println("\t" + fs.getID());
				if (fs.getURI() != null)
					System.out.println("\t" + fs.getURI());
				if (fs.getVariables().size() > 0) {
					System.out.println("FileSet Variables");
					ArrayList vars = fs.getVariables();
					itor = vars.iterator();
					while (itor.hasNext()) {
						ExportVariable var = (ExportVariable)itor.next();
						System.out.println(var.toString(1));
					}
				}
				
				// loop on the stations		
				System.out.println("\t" + "Stations");
				ArrayList stns = fs.getStations();	
				Iterator sitor = stns.iterator();
				while (sitor.hasNext()) {
					ExportStation stn = (ExportStation)sitor.next();
					System.out.println("\t\t" + stn.getID());
					
					// location
					ExportLatitude lat = stn.getLat();
					System.out.println(lat.toString(2));
					ExportLongitude lon = stn.getLon();
					System.out.println(lat.toString(2));
					ArrayList sverts = stn.getVerticals();
					Iterator vitor = sverts.iterator();
					while (vitor.hasNext()) {
						ExportVertical vert = (ExportVertical)vitor.next();
						System.out.println(vert.toString(2));
					}
				}
			}
		}
	}
	
	public ArrayList getFileSets() {
		return mFileSets;
	}
	
	public ArrayList getTimeSeriesVariables() {
		return null;
	}
	
	public ArrayList getTrackVariables() {
		return null;
	}
	
	public ArrayList getGridVariables() {
		return null;
	}
	
	public PointerFileAttributes getAttributes() {
		return mAttributes;
	}
	
	public boolean isArgo() {
		return false;
	}
}

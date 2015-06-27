package gov.noaa.pmel.eps2;import java.io.*;import java.lang.reflect.*;import java.text.*;import java.awt.*;import java.util.*;import ucar.netcdf.*;import ucar.multiarray.*;import gov.noaa.pmel.util.*;/** * <code>TSVSectionReader</code>  * Concrete implementation of the EPSFileReader interface to read, parse, and save a  * tab-separated (spreadsheet) format data file. * * @author oz * @version 1.0 * * @see EPSFileReader */public class XYZSectionReader implements EPSFileReader, EPSConstants {	/**	* ID count (< 0 if variable not found in EPIC Key database	*/	protected int mIDCount = -1;	/**	* Dbase that this file reader is initializing	*/	protected Dbase mOwnerDBase;	/**	* File to read	*/	protected File mFile;;	/**	* Optional string for progress string	*/	protected String mProgressStr = "Reading XYZ Data...";	  	/**   	* Construct a new <code>TSVSectionReader</code> with a Dbase amd file.   	*   	* @param dname Dbase that this reader will fill in   	* @param inFile Source section data file   	* 	* @see Dbase   	*/	public XYZSectionReader(gov.noaa.pmel.eps2.Dbase dname, File inFile, EpicPtr ep) {		mOwnerDBase = dname;		mFile = inFile;		mOwnerDBase.setEpicPtr(ep);		if (ep.getProgressStr() != null)			mProgressStr = new String(ep.getProgressStr());	}	  	/**   	* Construct a new <code>TSVSectionReader</code> with a Dbase amd file and a prompt string.   	*   	* @param dname Dbase that this reader will fill in   	* @param inFile Source section data file   	* 	* @see Dbase   	*/	public XYZSectionReader(Dbase dname, File inFile, EpicPtr ep, String progress) {		mOwnerDBase = dname;		mFile = inFile;		mProgressStr = progress;		mOwnerDBase.setEpicPtr(ep);	}		// concrete implementations of the io routines  	/**   	* Parse the section file and fill in the Dbase.   	*   	* @return Success code   	*/	public void parse() throws Exception {		JOAParameter tempProperties[] = new JOAParameter[100];		boolean[] hasQC = new boolean[100];		String inLine = new String();		LineNumberReader in = null;		long bytesInFile = mFile.length();		long bytesRead = 0;		int presPos = 0;				EPSProgressDialog mProgress = new EPSProgressDialog(new Frame(), mProgressStr, Color.blue);		mProgress.setVisible(true);					try {		    in = new LineNumberReader(new FileReader(mFile), 10000);		    		    // read the column header line		    inLine = in.readLine();		    bytesRead += inLine.length();		    		    // convert string to uppercase		    String inLineLC = new String(inLine);		    inLine = inLine.toUpperCase();		    		    // main loop			int bc = 0;						// number of bottles read			int s = 0;						// number of sections read			int mTotalStations = 0;						// create an array of arrays to store the data			double[] xa = new double[1200000];			double[] ya = new double[1200000];			double[] za = new double[1200000];		    			int c = 0;			while (true) {				inLine = in.readLine();				if (inLine == null || inLine.length() == 0) 					break;		    	bytesRead += inLine.length();									mProgress.setPercentComplete(100.0 * ((double)bytesRead/(double)bytesInFile));				String xs = EPS_Util.getItem(inLine, 1);				String ys = EPS_Util.getItem(inLine, 2);				String zs = EPS_Util.getItem(inLine, 3);				xa[c] = Double.valueOf(xs).floatValue();				ya[c] = Double.valueOf(ys).floatValue();				za[c] = Double.valueOf(zs).floatValue();				c++;			}			mProgress.setPercentComplete(100.0);			mProgress.dispose();						// now count the number of distinct longitudes			double prevLon = xa[0];			int lonCount = 0;			int lc = 1;			int maxLC = 0;			int[] lonCouts = new int[10000];			double[] tlons = new double[10000];			double[] tlats = new double[10000];			tlons[0] = prevLon;			tlats[0] = ya[0];						for (int i=0; i<10000; i++)				lonCouts[i] = 0;							for (int i = 0; i<c; i++) {				if (xa[i] != prevLon) {					// store the longitude					prevLon = xa[i];					tlons[lonCount] = prevLon;					if (lc > maxLC) {						maxLC = lc;					}					lonCouts[lonCount] = lc;					//System.out.println(lonCount + " numLats = " + lonCouts[lonCount]);						lc = 0;					lonCount++;				}								tlats[lc] = ya[i];				lc++;			}			    			System.out.println("numLons = " + lonCount);		    			System.out.println("maxLats = " + maxLC);						double[] lons = new double[lonCount];			double[] lats = new double[maxLC];						for (int i = 0; i<lonCount; i++)				lons[i] = tlons[i];						for (int i = 0; i<maxLC; i++) {				lats[i] = tlats[i];	    			System.out.println( i + " lats[i] = " + lats[i]);			}						// create the data array			double[][] zarry = new double[lonCount][maxLC];						// Initialize the array			for (int ln=0; ln< lonCount; ln++) {				for (int lt=0; lt<maxLC; lt++) {					zarry[ln][lt] = -1e34;				}			}						// fill the array			int base = 0;			for (int ln=0; ln<lonCount; ln++) {				if (ln > 0)					base += lonCouts[ln - 1];		    				for (int lt=0; lt<lonCouts[ln]; lt++) {					zarry[ln][lt] = za[base + lt];				}			}			String history = "Created by Java OceanAtlas XYZReader";			mOwnerDBase.addEPSAttribute("History", EPSConstants.EPCHAR, history.length(), history);						// create the axes		    Axis latAxis = new Axis();		    Axis lonAxis = new Axis();		    short[] sarray = new short[1];		    		    lonAxis.setName("longitude");		    lonAxis.setTime(false);		    lonAxis.setUnlimited(false);			lonAxis.setLen(lonCount);		    lonAxis.setAxisType(EPSConstants.EPXAXIS);		    lonAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");		    lonAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees_east");		    sarray[0] = 502;		    lonAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);		    MultiArray lma = new ArrayMultiArray(lons);		    lonAxis.setData(lma);		    mOwnerDBase.setAxis(lonAxis);		    		    // add the x axes variable		    EPSVariable var = new EPSVariable();		    var.setOname("longitude");		    var.setDtype(EPSConstants.EPDOUBLE);		    var.setVclass(Double.TYPE);		    var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");		    var.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees_east");		    var.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");		    sarray[0] = 502;		    var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);		    MultiArray xvma = new ArrayMultiArray(lons);		    try {		    	var.setData(xvma);		    }		    catch (Exception ex) {throw ex;}		    mOwnerDBase.addEPSVariable(var);		    		    latAxis.setName("latitude");		    latAxis.setTime(false);		    latAxis.setUnlimited(false);			latAxis.setLen(maxLC);		    latAxis.setAxisType(EPSConstants.EPYAXIS);		    latAxis.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");		    latAxis.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees_north");		    latAxis.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");		    sarray[0] = 500;		    latAxis.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);		    lma = new ArrayMultiArray(lats);		    latAxis.setData(lma);		    mOwnerDBase.setAxis(latAxis);				    		    // add the y axes variable		    var = new EPSVariable();		    var.setOname("latitude");		    var.setDtype(EPSConstants.EPDOUBLE);		    var.setVclass(Double.TYPE);		    var.addAttribute(0, "FORTRAN_format", EPSConstants.EPCHAR, 5, "f10.4");		    var.addAttribute(1, "units", EPSConstants.EPCHAR, 7, "degrees");		    var.addAttribute(2, "type", EPSConstants.EPCHAR, 1, " ");		    sarray[0] = 500;		    var.addAttribute(3, "epic_code", EPSConstants.EPSHORT, 1, sarray);		    MultiArray yvma = new ArrayMultiArray(lats);		    try {		    	var.setData(yvma);		    }		    catch (Exception ex) {throw ex;}		    mOwnerDBase.addEPSVariable(var);		    		    var = new EPSVariable();		    var.setOname("ROSE");		    var.setDtype(EPSConstants.EPDOUBLE);		    var.setVclass(Double.TYPE);		    int numAttributes = 0;		    String units = "meters";		    var.addAttribute(numAttributes++, "units", EPSConstants.EPCHAR, units.length(), units);		    		    // connect variable to axis			boolean[] dimUsed = {false, false, true, true};			var.setDimorder(0, 3);			var.setDimorder(1, 2);			var.setDimorder(2, -1);			var.setDimorder(3, -1);			var.setY(latAxis);			var.setX(lonAxis);		    		    // store the data		    MultiArray mdma = new ArrayMultiArray(zarry);		    try {		    	var.setData(mdma);		    }		    catch (Exception ex) {throw ex;}			mOwnerDBase.addEPSVariable(var);				    		    // write the output file		    // prompt for the output file		    Frame fr = new Frame();			String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;		    FileDialog f = new FileDialog(fr, "Save xyz netCDF as:", FileDialog.SAVE);		    f.setDirectory(directory);		    f.setFile("etopoxyz.nc");		    f.show();		    directory = f.getDirectory();		    f.dispose();		    		    if (directory != null && f.getFile() != null) {				File nf = new File(directory, f.getFile());		    			    try {			    	mOwnerDBase.writeNetCDF(nf);			    }			    catch (Exception ex) {			    	ex.printStackTrace();			    	System.out.println("an error occurred writing a netCDF file");			    }	        }		}		catch (Exception ex) {			ex.printStackTrace();			mProgress.dispose();			FileImportException fie = new FileImportException();			fie.setErrorLine(in.getLineNumber() - 1);			throw(fie);		}	}	  	/**   	* Get variable data from section file.   	*   	* @param inName Name of variable to get data for   	*   	* @return Multiarray of data   	*   	* @exception EPSVarDoesNotExistExcept Variable not found in the owner database   	* @exception IOException An IO error occurred getting the data    	*/	public MultiArray getvar(String inName) throws EPSVarDoesNotExistExcept, IOException {		return null;	}	public MultiArray getvar(String inName, int[] lci, int[] uci, int[] dims) throws EPSVarDoesNotExistExcept, IOException {		return null;	}	}
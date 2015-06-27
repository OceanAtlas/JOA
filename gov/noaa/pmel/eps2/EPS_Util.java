package gov.noaa.pmel.eps2;

import java.io.*;
import java.util.*;
import java.text.*;
import ucar.netcdf.*;
import ucar.multiarray.*;
import ucar.multiarray.MultiArray;
import ucar.ma2.*;
import java.net.URI;

/**
 * <code>EPS_Util</code> Static utility routines for the EPS Library
 * 
 * @author oz
 * @version 1.0
 */
public class EPS_Util implements EPSConstants {
	/**
	 * Convert an axis name into an Axis EPIC ID.
	 * 
	 * @param nonEPICAxis
	 *          Axis without epic codes
	 * 
	 * @return Axis code
	 */
	public static int getEPICID(Axis nonEPICAxis) {
		if (nonEPICAxis == null)
			return 0;
		int retVal = 0;
		String name = nonEPICAxis.getName().toLowerCase();
		if (name == null)
			return 0;
		if (name.indexOf("lat") >= 0)
			retVal = 500;
		else if (name.indexOf("lon") >= 0)
			retVal = 501;
		return retVal;
	}

	/**
	 * Convert depth in meters to pressure.
	 * 
	 * @param inZ
	 *          Pressure in meters
	 * 
	 * @return pressure in dbars
	 */
	public static double zToPres(double inZ) {
		double zz, zzz;

		zz = inZ * inZ;
		zzz = inZ * inZ * inZ;
		return (1.0076 * inZ + 0.0000023487 * zz - (1.2887e-11 * zzz));
	}

	/**
	 * Determine the file format for the file and path
	 * 
	 * @param name
	 *          Name of file
	 * @param path
	 *          Path to file
	 * 
	 * @return Either "cdf" or "unk"
	 * 
	 * @exception FileNotFoundException
	 *              File not found in specified path.
	 */
	public static int getFileFormat(String directory, String filename) throws FileNotFoundException, IOException {
		// first look at the file extension
		String lcFilename = filename.toLowerCase();
		if (lcFilename.indexOf(".ptr") > 0)
			return EPSConstants.PTRFILEFORMAT;
		else if (lcFilename.indexOf(".xml") > 0)
			return EPSConstants.XMLPTRFILEFORMAT;
		else if (lcFilename.indexOf(".zip") > 0)
			return EPSConstants.ZIPSECTIONFILEFORMAT;
		else if (lcFilename.indexOf(".tgz") > 0)
			return EPSConstants.GZIPTARSECTIONFILEFORMAT;
		else if (lcFilename.indexOf(".gz") > 0)
			return EPSConstants.GZIPFILEFORMAT;
		else if (lcFilename.indexOf(".joa") > 0)
			return EPSConstants.JOAFORMAT;
		else if (lcFilename.indexOf(".poa") > 0)
			return EPSConstants.POAFORMAT;
		else if (lcFilename.indexOf(".jos") > 0)
			return EPSConstants.SSFORMAT;
		else if (lcFilename.indexOf(".sd2") > 0)
			return EPSConstants.SD2FORMAT;
		else if (lcFilename.indexOf(".sd3") > 0)
			return EPSConstants.SD3FORMAT;
		else if (lcFilename.indexOf("hy1.csv") > 0)
			return EPSConstants.WOCEHYDFORMAT;
		else if (lcFilename.indexOf("ct1.csv") > 0)
			return EPSConstants.WOCECTDFORMAT;
		else if (lcFilename.indexOf(".trl") > 0)
			return EPSConstants.NETCDFXBTFORMAT;
		else if (lcFilename.indexOf(".jopi") > 0)
			return EPSConstants.JOPIFORMAT;
		else if (lcFilename.indexOf("_argoinv.txt") > 0)
			return EPSConstants.ARGOINVENTORYFORMAT;
		else if (lcFilename.indexOf("_gtsppinv.txt") > 0)
			return EPSConstants.GTSPPINVENTORYFORMAT;
		else if (lcFilename.indexOf(".txt") > 0)
			return EPSConstants.TEXTFORMAT;
		else if (lcFilename.indexOf(".xyz") > 0)
			return EPSConstants.XYZFORMAT;
		else if (lcFilename.indexOf(".nqdb") > 0)
			return EPSConstants.NQDBFORMAT;
		else if (lcFilename.indexOf("osd.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("xbt.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("mbt.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("ctd.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("uor.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("gld.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("sur.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("pfl.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("mrb.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("drb.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf("apb.csv") > 0)
			return EPSConstants.WODCSVFORMAT;
		else if (lcFilename.indexOf(".csv") > 0)
			return EPSConstants.TEXTFORMAT;
		else if (lcFilename.indexOf(".nc") > 0 || lcFilename.indexOf(".cdf") > 0 || lcFilename.indexOf(".ctd") > 0) {
			// look inside the file to determine whether it's a GDAC Argo multiprofile netCDF file
			File f;
      try {
	      f = EPS_Util.getFile(directory, filename);
      }
      catch (Exception e) {
    		return EPSConstants.UNKNOWNFORMAT;
      }
			EPSNetCDFFile netcdfFile = new EPSNetCDFFile(f, true);
			boolean hasArgoAsTitle = false;
			boolean isArgoConvention = false;
			boolean hasArgoDataType = false;
			
			Variable v = netcdfFile.getVariable("STATION_PARAMETERS");
			if (v!= null) {
				isArgoConvention = true;
			}

			v = netcdfFile.getVariable("DATA_TYPE");
			if (v != null) {
				int[] origin = new int[v.getRank()];
				int[] extent = v.getLengths();
				MultiArray tMa = (MultiArray) v.copyout(origin, extent);
				Object array = tMa.toArray();
				String typeStr = new String();
				for (int c=0; c<16; c++) {
					char pc = ((char[])array)[c];
					typeStr += pc;
				}
				if (typeStr.toLowerCase().indexOf("argo") >= 0) {
					hasArgoDataType = true;
				}
			}
			
			AttributeIterator ai = netcdfFile.getAttributes().iterator();
			while (ai.hasNext()) {
				// get the name
				Attribute at = ai.next();
				String name = at.getName();

				if (name.equalsIgnoreCase("title")) {
					String title = at.getStringValue().toLowerCase().trim();

					if (title.indexOf("argo") >= 0) {
						hasArgoAsTitle = true;
					}
				}

				if (name.equalsIgnoreCase("conventions")) {
					String conv = at.getStringValue().toLowerCase().trim();

					if (conv.indexOf("argo") >= 0 && conv.indexOf("cf") >= 0) {
						isArgoConvention = true;
					}
				}
			}
			
			if (hasArgoAsTitle && isArgoConvention) {
				return EPSConstants.ARGOGDACNETCDFFORMAT;
			}
			else if ((hasArgoAsTitle || hasArgoDataType) && (lcFilename.indexOf("_prof") > 0 || isArgoConvention)) {
				return EPSConstants.ARGOGDACNETCDFFORMAT;
			}
			else if (lcFilename.indexOf("_prof.nc") > 0) {
				return EPSConstants.ARGONODCNETCDFFORMAT;
			}
			else {
				return EPSConstants.NETCDFFORMAT;
			}
		}

		// got to here because we still don't know the format--look at the actual
		// file
		File mFile = new File(directory, filename);
		try {
			// netcdf format
			FileInputStream din = new FileInputStream(mFile);
			DataInputStream inData = new DataInputStream(din);
			int magic = inData.readInt();
			if (magic == 0x43444601 || magic == 0x01464443) {
				din.close();
				return EPSConstants.NETCDFFORMAT;
			}
			else {
				FileImportException fie = new FileImportException();
				throw (fie);
			}
		}
		catch (Exception e) {
			// JOAFORMAT
			FileInputStream in = null;
			// open up the file
			try {
				in = new FileInputStream(mFile);
				BufferedInputStream bis = new BufferedInputStream(in, 1000000);
				DataInputStream inData = new DataInputStream(bis);

				// JOA files have a version number
				short vers = inData.readShort();

				if (vers != 2) {
					FileImportException fie = new FileImportException();
					throw (fie);
				}
				else {
					in.close();
					return EPSConstants.JOAFORMAT;
				}
			}
			catch (Exception ex) {
				in.close();

				try {
					// try POA format
					in = new FileInputStream(mFile);
					BufferedInputStream bis = new BufferedInputStream(in, 1000000);
					DataInputStream inData = new DataInputStream(bis);

					// read number of bytes in file description string
					short inShort = inData.readShort();

					// read the file description String
					byte buf[] = new byte[inShort];
					inData.read(buf, 0, inShort);

					// read the number of sections
					int numSections = inData.readShort();
				}
				catch (Exception exp) {
					in.close();
					try {
						// try spreadsheet file: lines have a tab delimiter
						LineNumberReader in2 = new LineNumberReader(new FileReader(mFile), 10000);
						String inLine = in2.readLine();
						if (inLine.indexOf('\t') > 0) {
							return EPSConstants.SSFORMAT;
						}
						else {
							throw new IOException();
						}
					}
					catch (Exception ex3) {
						// try other formats here

					}
				}
			}
		}
		return EPSConstants.UNKNOWNFORMAT;
	}

	/**
	 * Tests whether character is a digit (0123456789)
	 * 
	 * @param c
	 *          character to test
	 * 
	 * @return true if digit
	 */
	public static boolean isDigit(int c) {
		String digits = new String("0123456789");
		if (digits.indexOf(c) >= 0)
			return true;
		else
			return false;
	}

	/**
	 * Tests whether character is the decimal point (.)
	 * 
	 * @param c
	 *          character to test
	 * 
	 * @return true if decimal point
	 */
	public static boolean isDecimalPoint(int c) {
		if (c == '.')
			return true;
		else
			return false;
	}

	/**
	 * Return a File object for a filename that is in the user directory of EPS
	 * Library
	 * 
	 * @param name
	 *          Name of file
	 * 
	 * @return File object for named file
	 * 
	 * @exception FileNotFoundException
	 *              File not found in user.dir.
	 */
	public static File getSupportFile(String name) throws FileNotFoundException {
		String dir = System.getProperty("user.dir") + File.separator;
		File nf = new File(dir, name);
		if (nf == null)
			throw new FileNotFoundException();
		return nf;
	}

	/**
	 * Return the pathname for the JOA temporary directory that's created when
	 * parsing zip files. Create the directory if necessary.
	 * 
	 * @return Pathname
	 */
	public static String getTempDir() {
		String dir = System.getProperty("user.dir") + File.separator + "temp" + File.separator;

		// test to see if it exists
		File tdir = new File(dir);
		if (!tdir.exists()) {
			tdir.mkdir();
		}
		return dir;
	}

	/**
	 * Delete the temporary directory and it's contents.
	 * 
	 */
	public static void removeTempDir() {
		// test to see if it exists
		File tdir = new File(getTempDir());
		if (!tdir.exists()) {
			tdir.delete();
		}
	}

	/**
	 * Return a File object for a filename with the given path
	 * 
	 * @param name
	 *          Name of file
	 * @param path
	 *          Path to file
	 * 
	 * @return File object for named file
	 * 
	 * @exception FileNotFoundException
	 *              File not found in specified path.
	 */
	public static File getFile(String path, String name) throws Exception {
		if (path.indexOf("file:") >= 0) {
			try {
				File nf = new File(getURI(path, name));
				return nf;
			}
			catch (Exception ex) {
				throw ex;
			}
		}
		else if (path.indexOf("http:") >= 0) {
			try {
				File nf = new File(path);
				return nf;
			}
			catch (Exception ex) {
				throw ex;
			}
		}
		else {
			File nf = new File(path, name);
			if (nf == null)
				throw new FileNotFoundException();
			return nf;
		}
	}

	/**
	 * Return a URI object for a filename with the given path
	 * 
	 * @param name
	 *          Name of file
	 * @param path
	 *          Path to file
	 * 
	 * @return URI object for named file
	 * 
	 * @exception dException
	 *              something went wrong creating the URI.
	 */
	public static URI getURI(String path, String name) throws Exception {
		URI uri;
		// construct a URI from a path and string
		try {
			String URIstr = path + "/" + name;

			// replace any spaces with %20;
			while (URIstr.indexOf(' ') > 0) {
				int spcLoc = URIstr.indexOf(' ');
				StringBuffer sb = new StringBuffer(URIstr);
				sb.replace(spcLoc, spcLoc + 1, "%20");
				URIstr = new String(sb);
			}
			uri = new URI(URIstr);
		}
		catch (Exception ex) {
			System.out.println("malformed URI");
			ex.printStackTrace();
			throw ex;
		}
		return uri;
	}

	public static String expandNullItems(String inStr) {
		if (inStr.indexOf(EPSProperties.DOUBLEDELIM) >= 0) {
			if (inStr.startsWith(EPSProperties.DOUBLEDELIM))
				inStr = new String(" " + inStr);

			// expand
			int pos = -1;
			while ((pos = inStr.indexOf(EPSProperties.DOUBLEDELIM)) >= 0) {
				StringBuffer sb = new StringBuffer(inStr);
				sb = sb.insert(pos + 1, ' ');
				inStr = new String(sb);
			}
			return inStr;
		}
		else
			return inStr;
	}

	/**
	 * Determine the number of delimted items in a string. \n Uses current value
	 * of EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * 
	 * @return Number of items in the string
	 */
	public static int numItems(String inString) {
		StringTokenizer st = new StringTokenizer(inString, EPSProperties.SDELIMITER);
		return st.countTokens();
	}

	/**
	 * Gets the last item from the input string. \n Uses current value of
	 * EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * 
	 * @return Last item in string
	 */
	public static String getLastItem(String inString) {
		return getItem(inString, numItems(inString));
	}

	/**
	 * Gets the Nth item from the input string. \n Uses current value of
	 * EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * @param item
	 *          Item number to return
	 * 
	 * @return Nth item of string
	 */
	public static String getItem(String inString, int item) {
		try {
			StringTokenizer st = new StringTokenizer(inString, EPSProperties.SDELIMITER);
			if (item - 1 > st.countTokens())
				return null;

			int c = 0;
			while (st.hasMoreElements()) {
				String outStr2 = (String) st.nextElement();
				if (c == item - 1) { return outStr2; }
				c++;
			}
			return null;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the item number of the test string in the input string. \n Uses
	 * current value of EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          String to search
	 * @param testStr
	 *          String to look for
	 * 
	 * @return Item number of matching item in inString
	 */
	public static int getItemNumber(String inString, String testStr) {
		testStr = testStr.toUpperCase();

		// test whether testStr is in inString
		if (inString.indexOf(testStr) == -1)
			return -1;

		// loop on the items
		int ic = 1;
		while (true) {
			String itemStr = getItem(inString, ic);
			if (itemStr == null)
				return -1;
			if (itemStr.indexOf(testStr) >= 0)
				return ic;
			ic++;
		}
	}

	/**
	 * Tests whether the nth item is in a list of strings \n Can search for
	 * various spellings as well as apply a "strictness" to the match Uses current
	 * value of EPSProperties.SDELIMITER
	 * 
	 * @param item
	 *          item to search
	 * @param inString
	 *          String to search
	 * @param testStr
	 *          Array of different spellings to search for an item
	 * @param strict
	 *          Array of codes to indicate how strict you want to match a
	 *          particular spelling
	 * 
	 * @return Item number of matching item in inString
	 */
	public static boolean isItem(int item, String inString, String[] testStrs, int[] strict) {
		int numTests = testStrs.length;
		String itemStr = getItem(inString, item);
		if (itemStr == null)
			return false;

		for (int i = 0; i < numTests; i++) {
			String testStr = testStrs[i].toUpperCase();

			if (strict[i] == EPSConstants.CONTAINS) {
				if (itemStr.indexOf(testStr) >= 0)
					return true;
			}
			else if (strict[i] == EPSConstants.MATCHES) {
				if (itemStr.equalsIgnoreCase(testStr))
					return true;
			}
			else if (strict[i] == EPSConstants.STARTSWITH) {
				if (itemStr.startsWith(testStr))
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets the item number of the test strings in the input string. \n Can search
	 * for various spellings as well as apply a "strictness" to the maatch Uses
	 * current value of EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          String to search
	 * @param testStr
	 *          Array of different spellings to search for an item
	 * @param strict
	 *          Array of codes to indicate how strict you want to match a
	 *          particular spelling
	 * 
	 * @return Item number of matching item in inString
	 */
	public static int getItemNumber(String inString, String[] testStrs, int[] strict) {
		int numTests = testStrs.length;

		for (int i = 0; i < numTests; i++) {
			String testStr = testStrs[i].toUpperCase();

			// loop on the items
			int ic = 1;
			while (true) {
				String itemStr = getItem(inString, ic);
				if (itemStr == null)
					break;

				if (strict[i] == EPSConstants.MATCHES) {
					if (itemStr.equalsIgnoreCase(testStr))
						return ic;
				}
				else if (strict[i] == EPSConstants.STARTSWITH) {
					if (itemStr.startsWith(testStr))
						return ic;
				}
				else if (strict[i] == EPSConstants.CONTAINS) {
					if (itemStr.indexOf(testStr) > 0)
						return ic;
				}
				ic++;
			}
		}
		return -1;
	}

	/**
	 * Gets the Nth item from the input string as an integer. \n Uses current
	 * value of EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * @param item
	 *          Item number to return
	 * 
	 * @return Nth item of inString as an integer
	 * 
	 * @exception NumberFormatException
	 *              An error occurred converting a string to an integer.
	 */
	public static int getIntItem(String inString, int item) throws NumberFormatException {
		if (inString == null)
			return -99;
		try {
			int retVal = Integer.valueOf(getItem(inString, item).trim()).intValue();
			return retVal;
		}
		catch (Exception ex) {
			return -99;
			/*
			 * try { String valStr = getItem(inString, item); valStr =
			 * trimPreceedingWhiteSpace(valStr).trim(); int retVal =
			 * Integer.valueOf(valStr).intValue(); return retVal; } catch (Exception
			 * exx) { throw ex; }
			 */
		}
	}

	/**
	 * Gets the Nth item from the input string as an short. \n Uses current value
	 * of EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * @param item
	 *          Item number to return
	 * 
	 * @return Nth item of inString as an short
	 * 
	 * @exception NumberFormatException
	 *              An error occurred converting a string to a short.
	 */
	public static short getShortItem(String inString, int item) throws NumberFormatException {
		try {
			short retVal = Short.valueOf(getItem(inString, item).trim()).shortValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
	}

	/**
	 * Gets the Nth item from the input string as a double. \n Uses current value
	 * of EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * @param item
	 *          Item number to return
	 * 
	 * @return Nth item of inString as a double
	 * 
	 * @exception NumberFormatException
	 *              An error occurred converting a string to a double.
	 */
	public static double getDoubleItem(String inString, int item) throws NumberFormatException {
		try {
			double retVal = Double.valueOf(getItem(inString, item).trim()).doubleValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
	}

	/**
	 * Gets the Nth item from the input string as a long. \n Uses current value of
	 * EPSProperties.SDELIMITER
	 * 
	 * @param inString
	 *          Input string
	 * @param item
	 *          Item number to return
	 * 
	 * @return Nth item of inString as a long
	 * 
	 * @exception NumberFormatException
	 *              An error occurred converting a string to a long.
	 */
	public static long getLongItem(String inString, int item) throws NumberFormatException {
		try {
			long retVal = Long.valueOf(getItem(inString, item).trim()).longValue();
			return retVal;
		}
		catch (NumberFormatException ex) {
			throw ex;
		}
	}

	public static void CollectUniqueItems(String inStr, ArrayList knownItems) {
		for (int i = 0; i < numItems(inStr); i++) {
			String item = getItem(inStr, i + 1);
			boolean found = false;
			for (int j = 0; j < knownItems.size(); j++) {
				String kitem = (String) knownItems.get(j);
				if (kitem.equalsIgnoreCase(item)) {
					found = true;
					break;
				}
			}
			if (!found) {
				knownItems.add(item);
			}
		}
	}

	/**
	 * Trims preceeding spaces from the input string.
	 * 
	 * @param inStr
	 *          String to trim
	 * 
	 * @return Trimmed string
	 */
	public static String trimPreceedingWhiteSpace(String inStr) {
		if (inStr == null || inStr.length() == 0)
			return inStr;
		StringBuffer outStr = new StringBuffer(inStr + "*");
		while (true) {
			int len = outStr.length();
			if (outStr.charAt(0) == ' ') {
				for (int i = 0; i < len - 1; i++) {
					outStr.setCharAt(i, outStr.charAt(i + 1));
				}
				outStr.setLength(len - 1);
			}
			else
				break;
		}

		int len = outStr.length();
		outStr.setLength(len - 1);
		return new String(outStr);
	}

	/**
	 * Checks and returns the axis direction based on input name or units.
	 * 
	 * @param name
	 *          String to parse
	 * @param units
	 *          units
	 * 
	 * @return EPTAXIS for time axis
	 * @return EPXAXIS for x axis
	 * @return EPYAXIS for y axis
	 * @return EPZAXIS for z axis
	 * @return EPNONEAXIS if it can not be determined.
	 */
	public static int getAxisDirection(String name, String units) {
		String lcname = new String(name.toLowerCase());
		String lcunits = new String(units.toLowerCase());

		// Check time
		if (lcname.startsWith("tim"))
			return EPTAXIS;
		else if (isNetcdfTimeUnits(units) > 0)
			return EPTAXIS;

		// Check x
		if (lcname.startsWith("lon"))
			return EPXAXIS;
		else if (lcname.startsWith("x"))
			return EPXAXIS;
		else {
			// check the units
			if (lcunits.startsWith("lon"))
				return EPXAXIS;
			else if (lcunits.startsWith("x"))
				return EPXAXIS;
			else {
				for (int i = 0; xunit[i] != null; i++)
					if (lcunits.startsWith(xunit[i]))
						return EPXAXIS;
			}
		}

		// Check y
		if (lcname.startsWith("lat"))
			return EPYAXIS;
		else if (lcname.startsWith("y"))
			return EPYAXIS;
		else {
			// check the units
			if (lcunits.startsWith("lat"))
				return EPYAXIS;
			else if (lcunits.startsWith("y"))
				return EPYAXIS;
			else {
				for (int i = 0; yunit[i] != null; i++)
					if (lcunits.startsWith(yunit[i]))
						return EPYAXIS;
			}
		}

		// Check z
		if (lcname.startsWith("dep"))
			return EPZAXIS;
		else if (lcname.startsWith("elev"))
			return EPZAXIS;
		else if (lcname.startsWith("z"))
			return EPZAXIS;
		else if (lcname.startsWith("level"))
			return EPZAXIS;
		else {
			// check the units
			if (lcunits.startsWith("dep"))
				return EPZAXIS;
			if (lcunits.startsWith("elev"))
				return EPZAXIS;
			else if (lcunits.startsWith("z"))
				return EPZAXIS;
			else {
				for (int i = 0; zunit[i] != null; i++)
					if (lcunits.startsWith(zunit[i]))
						return EPZAXIS;
			}
		}
		return -1;
	}

	/**
	 * Checks whether time units are netCDF format units.
	 * 
	 * @param units
	 *          units
	 * 
	 * @return true is returned if units are "unit(s) since yyyy-mm-dd hh:mm:ss"
	 */
	public static int isNetcdfTimeUnits(String units) {
		int i, nstr;
		String unit = new String();
		String str2 = new String();
		String plural = new String();

		// space or null is not a udunits time units
		if (units.length() == 0 || units.equalsIgnoreCase(" "))
			return 0;

		// check "unit since yyyy-mm-dd hh:mm:ss" format
		// nstr = sscanf(units,"%s %s",unit,str2);

		Object[] objs = { new String(), new String() };
		MessageFormat msgf = new MessageFormat("{0} {1}");
		try {
			objs = msgf.parse(units);
			unit = (String) objs[0];
			str2 = (String) objs[1];
		}
		catch (ParseException pe) {

		}

		if (unit.length() > 0 && str2.length() > 0 && str2.indexOf("since") < 0)
			return 0;

		// test unit string
		if (unit == null || unit.length() == 0)
			unit = new String(units);
		i = 0;
		while (time_units[i].getScale() != 0) {
			if (unit.equalsIgnoreCase(time_units[i].getName()))
				return i + 1;
			if (!time_units[i].getSingle()) {
				plural = new String(time_units[i].getName() + "s");
				if (unit.equalsIgnoreCase(plural))
					return i + 1;
			}
			i++;
		}
		return 0;
	}

	/**
	 * Convert a MultiArray to a String representation.
	 * 
	 * @param ma
	 *          MultiArray
	 * 
	 * @return String form of input MultiArray
	 */
	public static String MultiArrayToString(ucar.multiarray.MultiArray ma) {
		StringBuffer buf = new StringBuffer((ma).toString() + " (" + ma.getComponentType() + ", " + "[");
		int[] shape = ma.getLengths();
		int rank = ma.getRank();
		for (int i = 0; i < rank - 1; i++) {
			buf.append(shape[i] + ", ");
		}
		if (rank > 0) {
			buf.append(shape[rank - 1]);
		}
		try {
			buf.append("])" + MultiArrayToStringHelper(ma, new IndentLevel()));
		}
		catch (java.io.IOException e) {
			e.printStackTrace();
		}
		return buf.toString();
	}

	/**
	 * Used by MultiArrayToString
	 * 
	 * @param ma
	 *          MultiArray to print
	 * @param ilev
	 *          Indent level
	 */
	private static String MultiArrayToStringHelper(ucar.multiarray.MultiArray ma, IndentLevel ilev)
	    throws java.io.IOException { // no I/O here, so this won't really happen

		final int rank = ma.getRank();
		if (rank == 0) {
			try {
				return ma.get((int[]) null).toString();
			}
			catch (IOException ee) {
			}
		}
		StringBuffer buf = new StringBuffer();
		buf.append("\n" + ilev.getIndent() + "{");
		ilev.incr();
		final int[] dims = ma.getLengths();
		final int last = dims[0];
		for (int ii = 0; ii < last; ii++) {
			final ucar.multiarray.MultiArray inner = new ucar.multiarray.MultiArrayProxy(ma, new SliceMap(0, ii));
			buf.append(MultiArrayToStringHelper(inner, ilev));

			if (ii != last - 1)
				buf.append(", ");
		}
		ilev.decr();
		if (rank > 1) {
			buf.append("\n" + ilev.getIndent());
		}
		buf.append("}");

		return buf.toString();
	}

	/**
	 * Maintains indentation level for printing nested structures.
	 */
	static class IndentLevel {
		private int level = 0;
		private int indentation;
		private StringBuffer indent;
		private StringBuffer blanks;

		public IndentLevel() {
			this(4);
		}

		public IndentLevel(int indentation) {
			if (indentation > 0)
				this.indentation = indentation;
			indent = new StringBuffer();
			blanks = new StringBuffer();
			for (int i = 0; i < indentation; i++)
				blanks.append(" ");
		}

		public void incr() {
			level += indentation;
			indent.append(blanks);
		}

		public void decr() {
			level -= indentation;
			indent.setLength(level);
		}

		public String getIndent() {
			return indent.toString();
		}
	}

	/**
	 * Subtracts two times in EPIC format.
	 * 
	 * @deprecated
	 * 
	 * @param time1
	 *          EPIC time component #1
	 * @param time2
	 *          EPIC time component #2
	 * 
	 * @return Time difference as two longs
	 */
	public static long[] ep_time_sub(long[] time1, long[] time2) {
		// substract time1 from time2
		long[] delta = new long[2];
		delta[0] = time2[0] - time1[0];
		delta[1] = time2[1] - time1[1];

		while ((delta[0] > 0) && (delta[1] < 0)) {
			// subtract one day from delta[0] and add one day to delta[1]
			delta[0]--;
			delta[1] = delta[1] + 86400000;
		}
		while ((delta[0] < 0) && (delta[1] > 0)) {
			// add one day to delta[0] and subract on day from delta[1]
			delta[0]++;
			delta[1] = delta[1] - 86400000;
		}
		return delta;
	}

	public static long[] tArrayToEpicTime(Array t, int t_len, String units, long[] t_orig)
	    throws TimeConversionException, IOException {
		long[] eptime = new long[2 * t_len];
		long[] torig = new long[2];
		String t_units = new String();
		String t_base = new String();
		String t_base2 = new String();
		String str = new String();
		int ind;

		units = units.toLowerCase();
		if ((ind = isNetcdfTimeUnits(units)) == 0) { throw new TimeConversionException(units
		    + " is not a netCDF (udunits standard) time unit"); }

		if (units.indexOf("since") >= 0) {
			if (units.indexOf("(") >= 0 && units.indexOf(")") >= 0) {
				// sscanf(units,"%s since (%s %s)",t_units,t_base,t_base2);
				Object[] objs = { new String(), new String(), new String() };
				MessageFormat msgf = new MessageFormat("{0} since ({1} {2})");
				try {
					objs = msgf.parse(units);
					t_units = (String) objs[0];
					t_base = (String) objs[1];
					t_base2 = (String) objs[2];
				}
				catch (ParseException pe) {
					throw new TimeConversionException("Error parsing units (%s since (%s %s))");
				}
			}
			else {
				// sscanf(units,"%s since %s %s",t_units,t_base,t_base2);
				Object[] objs = { new String(), new String(), new String() };
				MessageFormat msgf = new MessageFormat("{0} since {1} {2}");
				try {
					objs = msgf.parse(units);
					t_units = (String) objs[0];
					t_base = (String) objs[1];
					t_base2 = (String) objs[2];
				}
				catch (ParseException pe) {
					throw new TimeConversionException("Error parsing units (%s since %s %s)");
				}
			}
			t_base = t_base + " " + t_base2;

			if (t_base != null)
				torig = EPS_Util.stringToEpicTime("YYYY-MM-DD hh:mm:ss.fff", t_base);
		}
		else {
			// sscanf(units,"%s",t_units);
			Object[] objs = { new String() };
			MessageFormat msgf = new MessageFormat("{0}");
			try {
				objs = msgf.parse(units);
				t_units = (String) objs[0];
			}
			catch (ParseException pe) {
				throw new TimeConversionException("Error parsing units (%s)");
			}
		}

		double SCALE = time_units[ind - 1].getScale() * 1000;
		// array optimization
		for (int i = 0; i < t_len; i++) {
			eptime[2 * i] = t_orig[0];
			eptime[2 * i + 1] = t_orig[1];
			double tim = 0.0;
			try {
				tim = t.getDouble(new Index1D(new int[] { i }));
			}
			catch (Exception ex) {
				throw new TimeConversionException("Error getting a double from the multiarray");
			}
			double millisec = (double) t_orig[1] + tim * SCALE;
			long d1 = 0;
			while (millisec >= 8.64e+07) {
				millisec -= 8.64e+07;
				d1++;
			}
			eptime[2 * i] += d1;
			eptime[2 * i + 1] = (long) millisec;
		}
		return eptime;
	}

	/**
	 * Convert `t' to EPIC time format based on the input `unit' and time origin
	 * `t_orig'.
	 * 
	 * @param t
	 *          MultiArray of integer or real dates, not GeoDates
	 * @param t_len
	 *          Length of date
	 * @param units
	 *          Time units
	 * @param t_orig
	 *          Time origin
	 * 
	 * @return Array of EPIC times.
	 * 
	 * @exception TimeConversionException
	 *              An error occurred converting integer or floating point time to
	 *              EPIC time.
	 */
	public static long[] tArrayToEpicTime(ucar.multiarray.MultiArray t, int t_len, String units, long[] t_orig)
	    throws TimeConversionException, IOException {
		long[] eptime = new long[2 * t_len];
		long[] torig = new long[2];
		String t_units = new String();
		String t_base = new String();
		String t_base2 = new String();
		String str = new String();
		int ind;

		units = units.toLowerCase();
		if ((ind = isNetcdfTimeUnits(units)) == 0) { throw new TimeConversionException(units
		    + " is not a netCDF (udunits standard) time unit"); }

		if (units.indexOf("since") >= 0) {
			if (units.indexOf("(") >= 0 && units.indexOf(")") >= 0) {
				// sscanf(units,"%s since (%s %s)",t_units,t_base,t_base2);
				Object[] objs = { new String(), new String(), new String() };
				MessageFormat msgf = new MessageFormat("{0} since ({1} {2})");
				try {
					objs = msgf.parse(units);
					t_units = (String) objs[0];
					t_base = (String) objs[1];
					t_base2 = (String) objs[2];
				}
				catch (ParseException pe) {
					throw new TimeConversionException("Error parsing units (%s since (%s %s))");
				}
			}
			else {
				// sscanf(units,"%s since %s %s",t_units,t_base,t_base2);
				Object[] objs = { new String(), new String(), new String() };
				MessageFormat msgf = new MessageFormat("{0} since {1} {2}");
				try {
					objs = msgf.parse(units);
					t_units = (String) objs[0];
					t_base = (String) objs[1];
					t_base2 = (String) objs[2];
				}
				catch (ParseException pe) {
					throw new TimeConversionException("Error parsing units (%s since %s %s)");
				}
			}
			t_base = t_base + " " + t_base2;

			if (t_base != null)
				torig = EPS_Util.stringToEpicTime("YYYY-MM-DD hh:mm:ss.fff", t_base);
		}
		else {
			// sscanf(units,"%s",t_units);
			Object[] objs = { new String() };
			MessageFormat msgf = new MessageFormat("{0}");
			try {
				objs = msgf.parse(units);
				t_units = (String) objs[0];
			}
			catch (ParseException pe) {
				throw new TimeConversionException("Error parsing units (%s)");
			}
		}

		double SCALE = time_units[ind - 1].getScale() * 1000;
		// array optimization
		for (int i = 0; i < t_len; i++) {
			eptime[2 * i] = t_orig[0];
			eptime[2 * i + 1] = t_orig[1];
			double tim = 0.0;
			try {
				tim = t.getDouble(new int[] { i });
			}
			catch (Exception ex) {
				throw new TimeConversionException("Error getting a double from the multiarray");
			}
			double millisec = (double) t_orig[1] + tim * SCALE;
			long d1 = 0;
			while (millisec >= 8.64e+07) {
				millisec -= 8.64e+07;
				d1++;
			}
			eptime[2 * i] += d1;
			eptime[2 * i + 1] = (long) millisec;
		}
		return eptime;
	}

	/**
	 * Convert netCDF time str into YYYY-MM-DD hh:mm:ss.fff. The input netCDF time
	 * format is year-month-day hour:min:second.fff where year, month and day can
	 * be in any number of digit (e.g. 89, 1989, 1, 01, etc).
	 * 
	 * @param t_str
	 *          netCFD time String
	 * 
	 * @return Converted time string.
	 */
	public static String convertNetCDFTimeString(String t_str) {
		String tok;
		String str1;
		int yr, mon, day, hr, min, sec, f;

		// isolate the date from the time
		String oldDelim = EPSProperties.SDELIMITER;
		EPSProperties.SDELIMITER = EPSProperties.SSPACE_DELIMITER;
		String dateStr = EPS_Util.getItem(t_str, 1);
		String timeStr = EPS_Util.getItem(t_str, 2);

		// take apart the date
		EPSProperties.SDELIMITER = EPSProperties.SHYPHEN_DELIMITER;
		if ((tok = EPS_Util.getItem(dateStr, 1)) != null) {
			// get year
			// sscanf(tok,"%d",&yr);
			try {
				yr = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				yr = 0;
			}
		}
		else
			yr = 0;

		if ((tok = EPS_Util.getItem(dateStr, 2)) != null) {
			// get month
			// sscanf(tok,"%d",&yr);
			try {
				mon = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				mon = 1;
			}
		}
		else
			mon = 1;

		if ((tok = EPS_Util.getItem(dateStr, 3)) != null) {
			// get day
			// sscanf(tok,"%d",&yr);
			try {
				day = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				day = 1;
			}
		}
		else
			day = 1;

		// take apart the time
		EPSProperties.SDELIMITER = EPSProperties.SCOLON_DELIMITER;

		if ((tok = EPS_Util.getItem(timeStr, 1)) != null) {
			// get hour
			// sscanf(tok,"%d",&yr);
			try {
				hr = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				hr = 0;
			}
		}
		else
			hr = 0;

		if ((tok = EPS_Util.getItem(timeStr, 2)) != null) {
			// get min
			try {
				min = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				min = 0;
			}
		}
		else
			min = 0;

		if ((tok = EPS_Util.getItem(timeStr, 3)) != null) {
			// get sec
			try {
				sec = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				sec = 0;
			}
		}
		else
			sec = 0;

		EPSProperties.SDELIMITER = EPSProperties.SPERIOD_DELIMITER;
		if ((tok = EPS_Util.getItem(timeStr, 2)) != null) {
			// get 1/000 sec
			try {
				f = Integer.valueOf(tok).intValue();
			}
			catch (Exception ex) {
				f = 0;
			}
		}
		else
			f = 0;

		// sprintf(time_string,"%04d-%02d-%02d
		// %02d:%02d:%02d.%03d",yr,mon,day,hr,min,sec,f);
		String frmt = new String(
		    "{0,number,0000}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}");
		MessageFormat msgf = new MessageFormat(frmt);

		Object[] objs = { new Integer(yr), new Integer(mon), new Integer(day), new Integer(hr), new Integer(min),
		    new Integer(sec), new Integer(f) };
		StringBuffer out = new StringBuffer();
		msgf.format(objs, out, null);
		String time_string = new String(out);
		EPSProperties.SDELIMITER = oldDelim;
		return time_string;
	}

	/**
	 * Converts input string to an integer month. The input must be a in one of
	 * the following format: M (1-12), MM (01-12), MMM (3 character name) and MMMM
	 * (full name).
	 * 
	 * @param mon_str
	 *          month String
	 * 
	 * @return integer code for month.
	 */
	public static int monthToInt(String mon_str) {
		if ((mon_str.charAt(0) >= '0' && mon_str.charAt(0) <= '9')
		    || (mon_str.charAt(0) == ' ' && mon_str.charAt(1) >= '0' && mon_str.charAt(1) <= '9')) {
			try {
				return Integer.valueOf(mon_str).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}

		boolean found = false;
		int i = 0;
		while (!found && i < 12) {
			String mon = mon_str.substring(0, 2);
			String tstMon = LongMonths[i].substring(0, 3);
			if (mon.equalsIgnoreCase(tstMon))
				found = true;
			i++;
		}

		if (found)
			return i;
		else
			return 0;
	}

	/**
	 * Convert mdy hms to eps time.
	 * 
	 * @param mon
	 *          Month
	 * @param day
	 *          Day
	 * @param yr
	 *          Year
	 * @param hour
	 *          Hour
	 * @param min
	 *          Minute
	 * @param sec
	 *          Seconds
	 * 
	 * @return EPIC format time.
	 * 
	 * @exception TimeConversionException
	 *              An error occurred converting to EPIC time.
	 */
	public static long[] mdyhmsToEpicTime(int mon, int day, int yr, int hour, int min, double sec)
	    throws TimeConversionException {
		long[] time = new long[2];
		long jul, ja, jy, jm;

		// check the valid month input */
		if (mon > 12 || mon < 1) { throw new TimeConversionException("mdyhmsToEpicTime: invalid month input: " + mon); }

		// check the valid day input */
		int leap = (yr % 4 != 0 ? 0 : (yr % 400 == 0 ? 1 : (yr % 100 == 0 ? 0 : 1)));
		max_day[1] = 28 + leap;

		if (day > max_day[mon - 1] || day < 1) { throw new TimeConversionException("mdyhmsToEpicTime: invalid day input: "
		    + day); }

		/* check the valid hour minute second input */
		if (hour >= 24 || hour < 0) { throw new TimeConversionException("mdyhmsToEpicTime: invalid hour input: " + hour); }

		if (min >= 60 || min < 0) { throw new TimeConversionException("mdyhmsToEpicTime: invalid minute input: " + min); }

		if (sec >= 60 || sec < 0) { throw new TimeConversionException("mdyhmsToEpicTime: invalid second input: " + sec); }

		if (yr < 0)
			++yr;
		if (mon > 2) {
			jy = yr;
			jm = mon + 1;
		}
		else {
			jy = yr - 1;
			jm = mon + 13;
		}
		jul = (long) (Math.floor(365.25 * jy) + Math.floor(30.6001 * jm) + day + 1720995);
		if (day + 31 * (mon + 12L * yr) >= GREGORIAN) {
			ja = (long) (0.01 * jy);
			jul += 2 - ja + (long) (0.25 * ja);
		}
		time[0] = jul;
		time[1] = (long) ((hour * 3600 + min * 60) * 1000 + (double) (sec * 1000));
		return time;
	}

	/**
	 * Convert string in a given format to eps time format.
	 * 
	 * @param frmt
	 *          Format string
	 * @param str
	 *          String to convert
	 * 
	 * @return EPIC time.
	 * 
	 * @exception TimeConversionException
	 *              An error occurred converting to EPIC time.
	 */
	public static long[] stringToEpicTime(String frmt, String str) throws TimeConversionException {
		char[] c = { 'Y', 'M', 'D', 'h', 'm', 's', 'f', 'N' };
		char[] cc = new char[9];
		int[] first = new int[8];
		int[] last = new int[8];
		int[] ind = new int[8];
		// find the field locations
		int tot = 0;
		for (int i = 0; i < 8; i++) {
			if (frmt.indexOf(c[i]) >= 0) {
				first[tot] = frmt.indexOf(c[i]);
				last[tot] = frmt.lastIndexOf(c[i]);
				cc[tot] = c[i];
				tot++;
			}
		}

		// eps_SortInt(first,tot,ind);
		for (int i = 0; i < tot; i++)
			ind[i] = i;
		for (int i = 0; i < tot - 1; ++i) {
			for (int j = i + 1; j < tot; ++j) {
				if (first[i] > first[j]) {
					int temp = first[i];
					first[i] = first[j];
					first[j] = temp;
					temp = ind[i];
					ind[i] = ind[j];
					ind[j] = temp;
				}
			}
		}

		// /eps_SortInt(last,tot,ind);
		for (int i = 0; i < tot; i++)
			ind[i] = i;
		for (int i = 0; i < tot - 1; ++i) {
			for (int j = i + 1; j < tot; ++j) {
				if (last[i] > last[j]) {
					int temp = last[i];
					last[i] = last[j];
					last[j] = temp;
					temp = ind[i];
					ind[i] = ind[j];
					ind[j] = temp;
				}
			}
		}

		double second = 0.0;
		int sec = 0;
		int msec = 0;
		int min = 0;
		int hour = 0;
		int yr = 0;
		int mon = 0;
		int day = 0;
		int yrday = 0;
		boolean use_yd = false;
		int nyr = 0;
		String newstr = new String(str);
		for (int i = 0; i < tot; i++) {
			try {
				String tmpstr = newstr.substring(first[i], last[i] + 1);
				switch (cc[ind[i]]) {
					case 'D':
						try {
							day = Integer.valueOf(tmpstr).intValue();
						}
						catch (Exception ex) {
							day = 0;
						}
						break;
					case 'M':
						mon = monthToInt(tmpstr);
						break;
					case 'Y':
						nyr = last[i] - first[i] + 1;
						try {
							yr = Integer.valueOf(tmpstr).intValue();
						}
						catch (Exception ex) {
							yr = 0;
						}
						break;
					case 'h':
						try {
							hour = Integer.valueOf(tmpstr).intValue();
						}
						catch (Exception ex) {
							hour = 0;
						}
						break;
					case 'm':
						try {
							min = Integer.valueOf(tmpstr).intValue();
						}
						catch (Exception ex) {
							min = 0;
						}
						break;
					case 's':
						try {
							sec = (int) Double.valueOf(tmpstr).doubleValue();
						}
						catch (Exception ex) {
							sec = 0;
						}
						break;
					case 'f':
						try {
							msec = (int) Double.valueOf(tmpstr).doubleValue();
						}
						catch (Exception ex) {
							msec = 0;
						}
						break;
					case 'N':
						try {
							yrday = (int) Double.valueOf(tmpstr).doubleValue();
							use_yd = true;
						}
						catch (Exception ex) {
							yrday = 0;
						}
						break;
				}
			}
			catch (Exception ex) {
				// usually means that the specified format field was not found in the
				// input string
			}
		}

		second = (double) msec / 1000.0 + sec;
		long[] time = new long[2];
		if (nyr == 2)
			yr += 1900;
		if (use_yd) {
			mon = day = 1;
			try {
				time = mdyhmsToEpicTime(mon, day, yr, hour, min, second);
				time[0] = time[0] + yrday - 1;
			}
			catch (TimeConversionException ex) {
				throw ex;
			}
		}
		else {
			try {
				time = mdyhmsToEpicTime(mon, day, yr, hour, min, second);
			}
			catch (TimeConversionException ex) {
				throw ex;
			}
		}
		return time;
	}

	/**
	 * Convert FERRET time str into DD-MMM-YYYY hh:mm:ss.fff.
	 * 
	 * @param t_str
	 *          String to convert
	 * 
	 * @return EPIC Ferret time string.
	 * 
	 * @exception TimeConversionException
	 *              An error occurred converting Ferret time string.
	 */
	public static String FERRET_time_str(String t_str) throws TimeConversionException {
		/*
		 * convert FERRET time str into DD-MMM-YYYY hh:mm:ss.fff. The input FERRET
		 * time format day-month-year hour:min:second where year, month and day can
		 * be in any number of digit (e.g. 89, 1989, 1, 01, etc).
		 */
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		/*
		 * char *tok; char str1[30], mon[4]; int yr, day, hr, min, sec;
		 * strcpy(str1,t_str); if((tok = (char *) strtok(str1,"-")) != (char*)NULL) //
		 * get day sscanf(tok,"%d",&day); else day=0; if((tok = (char *)strtok((char *)
		 * NULL,"-")) != (char *)NULL) // get mon sscanf(tok,"%s",mon); else
		 * strcpy(mon," "); if((tok = (char *)strtok((char *) NULL," ")) != (char
		 * *)NULL) // get year sscanf(tok,"%d",&yr); else day = 1; if((tok = (char
		 * *)strtok((char *) NULL,":")) != (char *)NULL) // get hour
		 * sscanf(tok,"%d",&hr); else hr = 0; if((tok = (char *)strtok((char *)
		 * NULL,":")) != (char *)NULL) // get min sscanf(tok,"%d",&min); else min =
		 * 0; if((tok = (char *)strtok((char *) NULL,".")) != (char *)NULL) // get
		 * sec sscanf(tok,"%d",&sec); else sec = 0;
		 * sprintf(time_string,"%02d-%3s-%04d
		 * %02d:%02d:%02d",day,mon,yr,hr,min,sec); return(time_string);
		 */
		return null;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public static double[] get1DDoubleArray(ucar.multiarray.MultiArray ma, int index) throws IOException {
		int[] lens = ma.getLengths();
		double[] xArray = new double[lens[index]];
		Object array = ma.toArray();
		if (array instanceof float[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((float[]) array)[i];
		}
		if (array instanceof double[])
			xArray = (double[]) array;
		else if (array instanceof int[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((int[]) array)[i];
		}
		else if (array instanceof short[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((short[]) array)[i];
		}

		return xArray;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public static double[] get1DDoubleArray(ucar.multiarray.MultiArray ma, int offset, int index) throws IOException {
		int[] lens = ma.getLengths();
		double[] xArray = new double[lens[index]];
		Object array = ma.toArray();
		if (array instanceof float[]) {
			int len = lens[index];
			for (int i = 0; i < len; i++)
				xArray[i] = (double) ((float[]) array)[i + (offset * len)];
		}
		if (array instanceof double[])
			xArray = (double[]) array;
		else if (array instanceof int[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((int[]) array)[i];
		}
		else if (array instanceof short[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((short[]) array)[i];
		}

		return xArray;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public static double[] get1DDoubleArray(ucar.multiarray.MultiArray ma, int toffset, int ltoffset, int lnoffset,
	    int index) throws IOException {
		int[] lens = ma.getLengths();
		double[] xArray = new double[lens[index]];
		Object array = ma.toArray();
		if (array instanceof float[]) {
			int len = lens[index];
			for (int i = 0; i < len; i++)
				xArray[i] = (double) ((float[]) array)[i + (toffset * len) + (ltoffset * len) + (lnoffset * len)];
		}
		if (array instanceof double[])
			xArray = (double[]) array;
		else if (array instanceof int[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((int[]) array)[i];
		}
		else if (array instanceof short[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (double) ((short[]) array)[i];
		}

		return xArray;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public static float[] get1DFloatArray(ucar.multiarray.MultiArray ma, int index) throws IOException {
		int[] lens = ma.getLengths();
		float[] xArray = new float[lens[index]];
		Object array = ma.toArray();
		if (array instanceof float[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = ((float[]) array)[i];
		}
		if (array instanceof double[])
			xArray = (float[]) array;
		else if (array instanceof int[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (float) ((int[]) array)[i];
		}
		else if (array instanceof short[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (float) ((short[]) array)[i];
		}

		return xArray;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public static short[] get1DShortArray(ucar.multiarray.MultiArray ma, int index) throws IOException {
		int[] lens = ma.getLengths();
		short[] xArray = new short[lens[index] + 1];
		Object array = ma.toArray();
		if (array instanceof short[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = ((short[]) array)[i];
		}
		else if (array instanceof int[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (short) (((int[]) array)[i]);
		}
		else if (array instanceof double[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (short) (((double[]) array)[i]);
		}
		else if (array instanceof float[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (short) (((float[]) array)[i]);
		}

		return xArray;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public static int[] get1DIntArray(ucar.multiarray.MultiArray ma, int index) throws IOException {
		int[] lens = ma.getLengths();
		int[] xArray = new int[lens[index] + 1];
		Object array = ma.toArray();
		if (array instanceof int[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = ((int[]) array)[i];
		}
		else if (array instanceof long[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = (int) (((long[]) array)[i]);
		}

		return xArray;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return char[] array
	 */
	public static char[] get1DCharArray(ucar.multiarray.MultiArray ma, int index) throws IOException {
		int[] lens = ma.getLengths();
		char[] xArray = new char[lens[index] + 1];
		Object array = ma.toArray();
		if (array instanceof char[]) {
			for (int i = 0; i < lens[index]; i++)
				xArray[i] = ((char[]) array)[i];
		}

		return xArray;
	}

	public static int getMeasuredDim(ucar.multiarray.MultiArray ma) {
		int[] lens = ma.getLengths();
		int max = 0;
		for (int i = 0; i < lens.length; i++) {
			max = lens[i] > max ? i : max;
		}
		return max;
	}

	/**
	 * Convert a MultiArray to a Java array.
	 * 
	 * @param ma
	 *          MultiArray to convert
	 * 
	 * @return double[] array
	 */
	public double[] getDoubleArray(Object array, int index, int[] origin, int[] shape) {
		double[] xArray = new double[shape[index]];
		int out = 0;
		int len = origin[index] + shape[index];
		for (int j = origin[index]; j < len; j++, out++) {
			if (array instanceof int[]) {
				xArray[out] = (double) ((int[]) array)[j];
			}
			else if (array instanceof double[]) {
				xArray[out] = ((double[]) array)[j];
			}
			else {
				xArray[out] = (double) ((float[]) array)[j];
			}
		}
		return xArray;
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
		else if (inParam.equalsIgnoreCase("AOU")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("O2%")) {
			return new String("none");
		}
		else if (inParam.equalsIgnoreCase("NO")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("PO")) {
			return new String("um/kg");
		}
		else if (inParam.equalsIgnoreCase("SPCY")) {
			return new String("none");
		}
		else if (inParam.equalsIgnoreCase("SVAN")) {
			return new String("m^3/kg");
		}
		else if (inParam.equalsIgnoreCase("SVEL")) {
			return new String("m/s");
		}
		else if (inParam.equalsIgnoreCase("GPOT")) {
			return new String("J/m");
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
		else if (inParam.equalsIgnoreCase("BDRV")) { return new String("1/db*10^3"); }
		return new String("na");
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

	public static void setKeySubDir(String subDir) {
		EPSProperties.epicKeySubDir = new String(subDir);
		EPSProperties.epicKeyDB = new EPIC_Key_DB("epic.key");
	}
}
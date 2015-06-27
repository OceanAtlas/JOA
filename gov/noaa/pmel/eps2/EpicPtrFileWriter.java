package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;
import java.text.*;
import gov.noaa.pmel.util.*;

/**
 * <code>EpicPtrFileWriter</code> defines the write method for writing
 * pointers in EPIC format.
 * 
 * @author oz
 * @version 1.0
 */

public class EpicPtrFileWriter implements PtrFileWriter {
	/*
	 * Stores a File object for the destination file.
	 */
	File mFile;

	private boolean mIncludeFileInPath = false;

	/*
	 * Construct a file writer for EPIC standard pointer files.
	 * 
	 * @param inFile Destination file
	 */
	public EpicPtrFileWriter(File inFile) {
		mFile = inFile;
	}

	public void setIncludeFileInPath(boolean b) {
		mIncludeFileInPath = b;
	}

	/*
	 * Write a pointer file.
	 * 
	 * @exception IOException An IO error occurred writing the pointer file
	 */

	public void write(ArrayList thePtrs) throws IOException {
		try {
			EpicPtr ep0 = (EpicPtr) thePtrs.get(0);
			String type = ep0.getDataType();
			if (ep0.isProfile())
				writeProfile(thePtrs);
			else if (ep0.isTimeSeries())
				writeTimeSeries(thePtrs);
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	/*
	 * EPIC (Unix) (Profile) [09:21:27 6-Apr-2000]
	 * 
	 * Longitude Range: 152 2.7W 151 57.6W Latitude Range: 22 40.6N 48 19.5N Depth
	 * Range(m): 0.000 5759.000 Required Depth(m): 0.000000 300.000000 Time Range:
	 * 1991-03-11 10:30 1991-03-27 23:20
	 * 
	 * DAT DATABASE --- -------- xxx unload
	 * 
	 * CRUISE CAST LATITUDE LONGITUDE DATE ZI ZF FILENAME PATH ---------------
	 * -------- --------- ---------- --------------- --------- ---------
	 * ------------ -------------------------------------------- CG2-91-DI - 61 39
	 * 21.0N 151 59.2W 1991-03-22 0933 0.000 5510.000 cg291c061.nc /Program
	 * Files/Metrowerks/CodeWarrior/JOA1.0
	 */

	public void write(ArrayList xPtrs, PointerFileAttributes attribs) throws IOException {
		// convert the "xml" ptrs to epic convention
		ArrayList thePtrs = new ArrayList();

		Iterator itor = xPtrs.iterator();
		while (itor.hasNext()) {
			ExportFileSet efs = (ExportFileSet) itor.next();
			String cruise = efs.getID();
			ArrayList stns = efs.getStations();

			Iterator sitor = stns.iterator();
			while (sitor.hasNext()) {
				ExportStation st = (ExportStation) sitor.next();
				ArrayList tord = st.getTimesOrDates();
				GeoDate date = new GeoDate();
				if (tord.get(0) instanceof ExportDate) {
					date = ((ExportDate) tord.get(0)).getGeoDate();
				}
				else if (tord.get(0) instanceof ExportTime) {
					date = ((ExportTime) tord.get(0)).getGeoDate();
				}

				double zmin = -99;
				double zmax = -99;
				ArrayList verts = st.getVerticals();
				Iterator vitor = verts.iterator();
				while (vitor.hasNext()) {
					ExportVertical v = (ExportVertical) vitor.next();
					if (v.isTop()) {
						zmin = v.getZ();
					}
					else if (v.isBottom()) {
						zmax = v.getZ();
					}
				}

				// the filename contains the URI--isolate path from file name
				String uri = st.getFileName();
				// if (uri.indexOf("file:") >= 0)
				// uri = uri.substring(7, uri.length());

				// isolate the filename
				int lastDelim = uri.lastIndexOf("/");
				String filename = uri.substring(lastDelim + 1, uri.length());

				String path = uri;
				if (!mIncludeFileInPath)
					path = uri.substring(0, lastDelim + 1);

				EpicPtr ep = new EpicPtr(EPSConstants.JOAFORMAT, "NQuery Results", "Profile", cruise, st.getID(), st.getLat()
				    .getLat(), st.getLon().getLon(), date, zmin, zmax, filename, path);
				thePtrs.add(ep);
			}
		}

		try {
			FileOutputStream fos = new FileOutputStream(mFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			DataOutputStream out = new DataOutputStream(bos);

			// write the header information
			EpicPtr ep0 = (EpicPtr) thePtrs.get(0);
			String type = ep0.getDataType();
			String formatStr = "UNKNOWN";
			if (ep0.isProfile())
				formatStr = "Profile";
			int typeLen = type.length();
			out.writeBytes("\n");
			out.writeBytes("    EPIC (" + ep0.getFormat() + ") (" + formatStr + ")\n");
			out.writeBytes("\n");
			out.writeBytes("    ");
			writePaddedString(out, "DATA", typeLen);
			writePrePaddedString(out, "DATABASE\n", 20 - typeLen);
			out.writeBytes("    ");
			writeDashes(out, typeLen);
			writePrePaddedString(out, "--------\n", 20 - typeLen);
			out.writeBytes("    ");
			out.writeBytes(type);
			writePrePaddedString(out, "export\n\n", 20 - typeLen);

			// figure out some of the field lengths
			int crsLen = 6;
			int fnLen = 0;
			int pathLen = 0;
			for (int i = 0; i < thePtrs.size(); i++) {
				EpicPtr ep = (EpicPtr) thePtrs.get(i);
				String crs = ep.getFileSet();
				if (crs != null)
					crsLen = crs.length() > crsLen ? crs.length() : crsLen;
				String fn = ep.getFileName();
				fnLen = fn.length() > fnLen ? fn.length() : fnLen;
				String path = ep.getPath();
				pathLen = path.length() > pathLen ? path.length() : pathLen;
			}

			// column headers names
			writePaddedString(out, "CRUISE", crsLen);
			out.writeBytes("CAST     LATITUDE  LONGITUDE  DATE            ZI        ZF        ");
			writePaddedString(out, "FILENAME", fnLen);
			writePaddedString(out, "PATH", pathLen);
			out.writeBytes("\n");

			// column headers dashes
			writeDashes(out, crsLen);
			out.writeBytes("-------- --------- ---------- --------------- --------- --------- ");
			writeDashes(out, fnLen);
			writeDashes(out, pathLen);
			out.writeBytes("\n");

			// write the data now
			for (int i = 0; i < thePtrs.size(); i++) {
				EpicPtr ep = (EpicPtr) thePtrs.get(i);
				String crs = ep.getFileSet();
				if (crs != null)
					writePaddedString(out, crs, crsLen);
				else
					writePaddedString(out, " ", 1);

				String cast = ep.getID();
				writePrePaddedString(out, cast, 6);
				out.writeBytes("  ");

				String lat = formatLat(ep.getLat());
				String lon = formatLon(ep.getLon());
				out.writeBytes(lat);
				out.writeBytes(" ");
				out.writeBytes(lon);

				GeoDate gd = ep.getStartTime();
				String date = gd.toString("yyyy-MM-dd HHmm").trim();
				out.writeBytes(" ");
				out.writeBytes(date);

				String zmin = formatDepth(ep.getZMin()).trim();
				writePrePaddedString(out, zmin, 8);
				String zmax = formatDepth(ep.getZMax()).trim();
				writePrePaddedString(out, zmax, 8);
				out.writeBytes(" ");

				String fn = ep.getFileName();
				String path = ep.getPath().trim();
				writePaddedString(out, fn, fnLen);
				out.writeBytes(path);
				out.writeBytes("\n");
			}

			out.flush();
			out.close();
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	/*
	 * Write a profile pointer file.
	 * 
	 * @exception IOException An IO error occurred writing the pointer file
	 */

	/*
	 * EPIC (Unix) (Profile) [09:21:27 6-Apr-2000]
	 * 
	 * Longitude Range: 152 2.7W 151 57.6W Latitude Range: 22 40.6N 48 19.5N Depth
	 * Range(m): 0.000 5759.000 Required Depth(m): 0.000000 300.000000 Time Range:
	 * 1991-03-11 10:30 1991-03-27 23:20
	 * 
	 * DAT DATABASE --- -------- xxx unload
	 * 
	 * CRUISE CAST LATITUDE LONGITUDE DATE ZI ZF FILENAME PATH ---------------
	 * -------- --------- ---------- --------------- --------- ---------
	 * ------------ -------------------------------------------- CG2-91-DI - 61 39
	 * 21.0N 151 59.2W 1991-03-22 0933 0.000 5510.000 cg291c061.nc /Program
	 * Files/Metrowerks/CodeWarrior/JOA1.0
	 */
	public void writeProfile(ArrayList thePtrs) throws IOException {
		try {

			FileOutputStream fos = new FileOutputStream(mFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			DataOutputStream out = new DataOutputStream(bos);

			// write the header information
			EpicPtr ep0 = (EpicPtr) thePtrs.get(0);
			String type = ep0.getDataType();
			String formatStr = "UNKNOWN";
			if (ep0.isProfile())
				formatStr = "Profile";
			int typeLen = type.length();
			out.writeBytes("\n");
			out.writeBytes("    EPIC (" + ep0.getFormat() + ") (" + formatStr + ")\n");
			out.writeBytes("\n");
			out.writeBytes("    ");
			writePaddedString(out, "DATA", typeLen);
			writePrePaddedString(out, "DATABASE\n", 20 - typeLen);
			out.writeBytes("    ");
			writeDashes(out, typeLen);
			writePrePaddedString(out, "--------\n", 20 - typeLen);
			out.writeBytes("    ");
			out.writeBytes(type);
			writePrePaddedString(out, "export\n\n", 20 - typeLen);

			// figure out some of the field lengths
			int crsLen = 6;
			int fnLen = 0;
			int pathLen = 0;
			for (int i = 0; i < thePtrs.size(); i++) {
				EpicPtr ep = (EpicPtr) thePtrs.get(i);
				String crs = ep.getFileSet();
				if (crs != null)
					crsLen = crs.length() > crsLen ? crs.length() : crsLen;
				String fn = ep.getFileName();
				fnLen = fn.length() > fnLen ? fn.length() : fnLen;
				String path = ep.getPath();
				pathLen = path.length() > pathLen ? path.length() : pathLen;
			}

			// column headers names
			writePaddedString(out, "CRUISE", crsLen);
			out.writeBytes("CAST     LATITUDE  LONGITUDE  DATE            ZI        ZF        ");
			writePaddedString(out, "FILENAME", fnLen);
			writePaddedString(out, "PATH", pathLen);
			out.writeBytes("\n");

			// column headers dashes
			writeDashes(out, crsLen);
			out.writeBytes("-------- --------- ---------- --------------- --------- --------- ");
			writeDashes(out, fnLen);
			writeDashes(out, pathLen);
			out.writeBytes("\n");

			// write the data now
			for (int i = 0; i < thePtrs.size(); i++) {
				EpicPtr ep = (EpicPtr) thePtrs.get(i);
				String crs = ep.getFileSet();
				if (crs != null)
					writePaddedString(out, crs, crsLen);
				else
					writePaddedString(out, " ", 1);

				String cast = ep.getID();
				writePrePaddedString(out, cast, 6);
				out.writeBytes("  ");

				String lat = formatLat(ep.getLat());
				String lon = formatLon(ep.getLon());
				out.writeBytes(lat);
				out.writeBytes(" ");
				out.writeBytes(lon);

				GeoDate gd = ep.getStartTime();
				String date = gd.toString("yyyy-MM-dd HHmm").trim();
				out.writeBytes(" ");
				out.writeBytes(date);

				String zmin = formatDepth(ep.getZMin()).trim();
				writePrePaddedString(out, zmin, 8);
				String zmax = formatDepth(ep.getZMax()).trim();
				writePrePaddedString(out, zmax, 8);
				out.writeBytes(" ");

				String fn = ep.getFileName();
				String path = ep.getPath().trim();
				writePaddedString(out, fn, fnLen);
				out.writeBytes(path);
				out.writeBytes("\n");
			}

			out.flush();
			out.close();
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	/*
	 * Write a profile time series pointer file.
	 * 
	 * @exception IOException An IO error occurred writing the pointer file
	 */

	/*
	 * EPIC (Unix) (TIME SERIES) [14:58:21 30-Jun-2000]
	 * 
	 * DAT DATABASE --- -------- 30 JUN
	 * 
	 * 
	 * LATITUDE LONGITUDE DEPTH STARTTIME ENDTIME DELTIME FILENAME PATH --------
	 * --------- --------- --------------- --------------- --------
	 * ----------------- ---- 51 49.8N 131 34.8W 0.000 1946-01-01 0600 1982-12-31
	 * 1800 360.000 wr52n132146ax.000 /home/aegir4/epic/ts/alaska/
	 */
	public void writeTimeSeries(ArrayList thePtrs) throws IOException {
		try {

			FileOutputStream fos = new FileOutputStream(mFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			DataOutputStream out = new DataOutputStream(bos);

			// write the header information
			EpicPtr ep0 = (EpicPtr) thePtrs.get(0);
			String type = ep0.getDataType();
			String formatStr = "UNKNOWN";
			if (ep0.isTimeSeries())
				formatStr = "Time Series";
			int typeLen = type.length();
			out.writeBytes("\n");
			out.writeBytes("    EPIC (" + ep0.getFormat() + ") (" + formatStr + ")\n");
			out.writeBytes("\n");
			out.writeBytes("    ");
			writePaddedString(out, "DATA", typeLen);
			writePrePaddedString(out, "DATABASE\n", 20 - typeLen);
			out.writeBytes("    ");
			writeDashes(out, typeLen);
			writePrePaddedString(out, "--------\n", 20 - typeLen);
			out.writeBytes("    ");
			out.writeBytes(type);
			writePrePaddedString(out, "export\n\n", 20 - typeLen);

			// figure out some of the field lengths
			int fnLen = 0;
			int pathLen = 0;
			for (int i = 0; i < thePtrs.size(); i++) {
				EpicPtr ep = (EpicPtr) thePtrs.get(i);
				String fn = ep.getFileName();
				fnLen = fn.length() > fnLen ? fn.length() : fnLen;
				String path = ep.getPath();
				pathLen = path.length() > pathLen ? path.length() : pathLen;
			}

			// column headers names
			out.writeBytes("LATITUDE LONGITUDE  DEPTH     STARTTIME       ENDTIME         DELTIME  ");
			writePaddedString(out, "FILENAME", fnLen);
			writePaddedString(out, "PATH", pathLen);
			out.writeBytes("\n");

			// column headers dashes
			out.writeBytes("-------- ---------- --------- --------------- --------------- -------- ");
			writeDashes(out, fnLen);
			writeDashes(out, pathLen);
			out.writeBytes("\n");

			// write the data now
			for (int i = 0; i < thePtrs.size(); i++) {
				EpicPtr ep = (EpicPtr) thePtrs.get(i);
				String lat = formatLat(ep.getLat());
				String lon = formatLon(ep.getLon());
				out.writeBytes(lat);
				out.writeBytes(" ");
				out.writeBytes(lon);

				String zmin = formatDepth(ep.getZMin()).trim();
				writePrePaddedString(out, zmin, 8);

				GeoDate gd = ep.getStartTime();
				String date = gd.toString("yyyy-MM-dd HHmm").trim();
				out.writeBytes(" ");
				out.writeBytes(date);

				gd = ep.getEndTime();
				date = gd.toString("yyyy-MM-dd HHmm").trim();
				out.writeBytes(" ");
				out.writeBytes(date);

				String delta = formatDepth(ep.getDeltaT()).trim();
				writePrePaddedString(out, delta, 7);
				out.writeBytes(" ");

				String fn = ep.getFileName();
				String path = ep.getPath().trim();
				writePaddedString(out, fn, fnLen);
				out.writeBytes(path);
				out.writeBytes("\n");
			}

			out.flush();
			out.close();
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	/*
	 * Write a end-padded string to the output stream.
	 * 
	 * @param out Output stream @param inStr The String to pad @param finalLength
	 * Final length of string after padding
	 */
	private void writePaddedString(DataOutputStream out, String inStr, int finalLength) throws IOException {
		StringBuffer outStr = new StringBuffer(inStr);

		while (outStr.length() <= finalLength) {
			outStr.append(" ");
		}
		try {
			String sout = new String(outStr);
			// System.out.println("*" + inStr + "* = *" + sout + "*" + finalLength);
			out.writeBytes(sout);
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	/*
	 * Write a pre-padded string to the output stream.
	 * 
	 * @param out Output stream @param inStr The String to pad @param finalLength
	 * Final length of string after padding
	 */
	private void writePrePaddedString(DataOutputStream out, String inStr, int finalLength) throws IOException {
		StringBuffer outStr = new StringBuffer(inStr);

		while (outStr.length() <= finalLength + 1) {
			outStr.insert(0, " ");
		}
		try {
			out.writeBytes(new String(outStr));
		}
		catch (IOException ex) {
			throw ex;
		}

	}

	/*
	 * Write dashes ("-") to the output stream.
	 * 
	 * @param out Output stream @param finalLength Number of dashes to write
	 */
	private void writeDashes(DataOutputStream out, int finalLength) throws IOException {
		try {
			for (int i = 0; i < finalLength; i++)
				out.writeBytes("-");
			out.writeBytes(" ");
		}
		catch (IOException ex) {
			throw ex;
		}
	}

	/*
	 * Format a value as a human-readable latitude.
	 * 
	 * @param lat Latitude value to format
	 */
	private String formatLat(double lat) {
		int deg = (int) lat;
		char hemis = 'N';
		if (lat < 0) {
			hemis = 'S';
			deg = -deg;
			lat = -lat;
		}
		double decDeg = lat - (double) deg;
		double decMin = decDeg * 60.0;

		String frmt = new String("{0,number,00} {1,number,00.0}{2}");
		MessageFormat msgf = new MessageFormat(frmt);
		Object[] objs = { new Integer(deg), new Double(decMin), new Character(hemis) };
		StringBuffer out = new StringBuffer();
		msgf.format(objs, out, null);
		return new String(out);
	}

	/*
	 * Format a value as a human-readable longtitude.
	 * 
	 * @param lon Longitude value to format
	 */
	private String formatLon(double lon) {
		char hemis = 'E';
		int deg = (int) lon;
		if (lon < 0) {
			hemis = 'W';
			deg = -deg;
			lon = -lon;
		}
		double decDeg = lon - (double) deg;
		double decMin = decDeg * 60.0;

		String frmt = new String("{0,number,000} {1,number,000.0}{2}");
		MessageFormat msgf = new MessageFormat(frmt);
		Object[] objs = { new Integer(deg), new Double(decMin), new Character(hemis) };
		StringBuffer out = new StringBuffer();
		msgf.format(objs, out, null);
		return new String(out);
	}

	/*
	 * Format a depth value.
	 * 
	 * @param z Depth value
	 */
	private String formatDepth(double z) {
		String frmt = new String("{0,number,####.###}");
		MessageFormat msgf = new MessageFormat(frmt);
		Object[] objs = { new Double(z) };
		StringBuffer out = new StringBuffer();
		msgf.format(objs, out, null);
		return new String(out);
	}
}

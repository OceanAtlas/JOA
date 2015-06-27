package gov.noaa.pmel.eps2;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import gov.noaa.pmel.util.*;
import ucar.netcdf.*;
import ucar.multiarray.*;
import java.util.zip.*;
import java.util.*;

/**
 * <code>ZipPtrReader</code> a reader class for ptr files is a zip archive.
 *
 * @see EpicPtrFactory
 * @see PtrFileReader
 *
 * @author oz
 * @version 1.0
 */

public class ZipPtrReader implements PtrFileReader, EPSConstants {
    /**
     * Epic ptr file to read.
     */
	protected File mFile;

    /**
     * Keys to use when sorting the file in place.
     */
	protected int[] mSortKeys = new int[4];

    /**
     * Flag to specify whether to open up files and create Epic ptr records.
     */
	protected boolean mCreatePtrs;

    /**
     * Construct a new <code>ZipPtrReader</code> with a file reference and sortkeys.
     *
     * @param inFile Pointer file
     * @param inKeys Sort key array
     */
	public ZipPtrReader(File inFile, int[] inKeys, boolean createPtrs) {
		mFile = inFile;
		if (inKeys != null)
			setSortKeys(inKeys);
		mCreatePtrs = createPtrs;
	}

    /**
     * Set the sort keys.
     *
     * @param inKeys Array of sort keys
     */
	public void setSortKeys(int[] inKeys) {
		for (int i=0; i<4; i++)
			mSortKeys[i] = inKeys[i];
	}

    /**
     * Parse a simple ptr file and create a collection of EpicPtrs.
     *
     * @exception IOException An IO error occurred reading the ptr file
     */
	public ArrayList parse() throws IOException {
		String dataType = new String();
		String database = new String();
		double lat = 0;
		double lon = 0;
		GeoDate stDate = null, endDate = null, secDate = null;
		double zi = 0, zf = 0;
		boolean profile = false;
		String cruise = "na";
		String cast = "na";
		double del = 0.0;
		int numBottles = 1;
		int numTimes = 1;
		ZipInputStream zis = null;

		ArrayList filePtrs = new ArrayList(100);
	    try {
			ZipFile zippo = new ZipFile(mFile);
			int fcount = zippo.size();

			// get or create (if first time) the temporary directory
			String fpath = System.getProperty("user.dir") + File.separator + "temp" + File.separator;

			// test to see if it exists
			File tdir = new File(fpath);
			if (!tdir.exists()) {
				tdir.mkdir();
			}

			Enumeration iter = zippo.entries();
			int entryCount = 0;
			while (iter.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry)iter.nextElement();
				if (zipEntry.isDirectory() || zipEntry.getName().contains("__MACOSX"))
					continue;

				String filename = zipEntry.getName();
				entryCount++;

				// inflate a zip entry to the temporary directory
				File tFile = null;
				try {
					InputStream in = zippo.getInputStream(zipEntry);
					tFile = new File(fpath, filename);
					FileOutputStream out = new FileOutputStream(tFile);
					int len = 102400;
					byte[] buf = new byte[102400];
					int c;
					while ((c = in.read(buf, 0, len)) > -1) {
						out.write(buf, 0, c);
					}
					in.close();
					out.close();
				}
				catch (Exception ex) {
					System.out.println("Failed reading " + tFile + "...skipping");
					continue;
				}

				// get the file format of the component file
				int format = EPS_Util.getFileFormat(fpath, filename);

				// my processing
				if (!mCreatePtrs) {
					// this has to be format specific
					String progressStr = "Reading file #" + String.valueOf(entryCount) + " of " + String.valueOf(fcount);
					EpicPtr eppf = new EpicPtr(format, filename, fpath, progressStr);
					filePtrs.add(eppf);
					continue;
				}

				// create a Dbase object for the file
				Dbase db = new Dbase();
				//CompositeDbase profSection = new CompositeDbase();
				EpicPtr ep = null;

				try {
					// this has to be format specific
					if (format == EPSConstants.NETCDFFORMAT || format == EPSConstants.NETCDFXBTFORMAT) {
						EPSNetCDFFile nc = new EPSNetCDFFile(tFile, NC_NOWRITE);
						db.setFileReader(new netCDFReader(db, nc, ep, "Reading file #" + String.valueOf(entryCount)));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.ARGOGDACNETCDFFORMAT) {
						EPSNetCDFFile nc = new EPSNetCDFFile(tFile, NC_NOWRITE);
						db.setFileReader(new ArgoGDACProfilenetCDFReader(db, nc, ep));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.ARGONODCNETCDFFORMAT) {
						EPSNetCDFFile nc = new EPSNetCDFFile(tFile, NC_NOWRITE);
						db.setFileReader(new ArgoNODCProfilenetCDFReader(db, nc, ep, "Reading file #" + String.valueOf(entryCount)));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.POAFORMAT) {
						db.setFileReader(new POASectionReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.JOAFORMAT) {
						db.setFileReader(new JOASectionReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.SSFORMAT) {
						db.setFileReader(new TSVSectionReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.SD2FORMAT) {
						db.setFileReader(new SD2SectionReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.WOCEHYDFORMAT) {
						db.setFileReader(new WOCEBottleSectionReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.WOCECTDFORMAT) {
						db.setFileReader(new WOCECTDProfileReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
					else if (format == EPSConstants.JOPIFORMAT) {
						db.setFileReader(new JOPISectionReader(db, tFile, ep, "Reading file " + filename + " (#" + String.valueOf(entryCount) + ")"));
						try {
							db.getFileReader().parse();
						}
						catch (Exception ex) {
							throw new IOException();
						}
					}
				}
				catch (IOException ex) {
					throw new IOException("Threw in setFile");
				}

				// get the axes
				// latitude axis
	    		Axis latAxis = db.getAxis("lat");
	    		if (latAxis != null) {
	    			MultiArray lma = latAxis.getData();
	    			try {
	    				lat = lma.getDouble(new int[] {0});
	    			}
	    			catch (Exception ex) {
	    				throw new IOException();
	    			}
	    		}

				// longitude axis
	    		Axis lonAxis = db.getAxis("lon");
	    		if (lonAxis != null) {
	    			MultiArray lma = lonAxis.getData();
	    			try {
	    				lon = lma.getDouble(new int[] {0});
	    			}
	    			catch (Exception ex) {
	    				throw new IOException();
	    			}
	    		}

				// read the date: get a geodate, convert to a string and then parse with a date format
	    		Axis timeAxis = db.getAxis("time");
	    		if (timeAxis != null) {
					numTimes = timeAxis.getLen();
	    			MultiArray tma = timeAxis.getData();
	    			try {
	    				 stDate = (GeoDate)tma.get(new int[] {0});
	    				 if (numTimes > 1) {
	    				 	secDate = (GeoDate)tma.get(new int[] {1});
	    				 	endDate = (GeoDate)tma.get(new int[] {numTimes-1});
	    				 }

	    			}
	    			catch (Exception ex) {
    					throw new IOException();
	    			}
	    		}

	    		Axis depAxis = db.getAxis("depth");
	    		if (depAxis == null)
	    			depAxis = db.getAxis("pres");
	    		else if (depAxis == null)
	    			depAxis = db.getAxis("p");

	    		if (depAxis != null) {
					// get number of "bottles"
					numBottles = depAxis.getLen();
	    			MultiArray zma = depAxis.getData();
	    			try {
	    				 zi = zma.getDouble(new int[] {0});
	    				 if (numBottles > 1)
	    				 	zf = zma.getDouble(new int[] {numBottles-1});
	    			}
	    			catch (Exception ex) {
    					throw new IOException();
	    			}
				}

				// look into the db to recover the metainformation
				String dType = db.getDataType().toUpperCase();

				if (dType.indexOf("CTD") >= 0 || dType.indexOf("BOTTLE") >= 0) {
					// get hydro attributes
					HydroAttributes hydroAtts = db.getHydroAttributes();
					cruise = hydroAtts.getCruise();
					cast = hydroAtts.getCast();
					profile = true;
				}
				else {
					profile = false;
					// possibly a time series
					// get time series attributes
					TimeSeriesAttributes tsAtts = db.getTimeSeriesAttributes();

					// try to get delta
					String delta = tsAtts.getDeltaT();

					if (delta != null) {
						// assume it's a time series plot
						del = Double.valueOf(delta).doubleValue();
					}
					else if (numTimes > 1) {
						// no delta-t attribute--compute delta-t from the time axis
						GeoDate delDate = secDate.subtract(stDate);
						del = delDate.getTime()/1000.0;
					}
				}
				// create a pointer object and add to Vector
				EpicPtr eppf = null;
				if (profile)
					eppf = new EpicPtr(format, "UNKNOWN", dType, cruise, cast, lat, lon,
                  							   stDate, zi, zf, filename, fpath);
                else
					eppf = new EpicPtr(format, "UNKNOWN", dType, zi, lat, lon,
                  								stDate, endDate, del, filename, fpath);
				filePtrs.add(eppf);

			}  //while
		}
		catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		}
		return filePtrs;
	}
	
	public boolean isArgo() {
		return false;
	}
}

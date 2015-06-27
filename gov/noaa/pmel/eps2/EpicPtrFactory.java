package gov.noaa.pmel.eps2;
import java.io.*;
import java.util.*;

/**
 * <code>EpicPtrFactory</code> creates an EpicPtr of the desired concrete class
 *
 * @see EpicPtrFactoryIF
 *
 * @author oz
 * @version 1.0
 */

public class EpicPtrFactory implements EpicPtrFactoryIF, EPSConstants {
	/**
	* Create an EPIC pointers database. Determine format of file by opening it. \n
	* Set the reader class for the EPIC pointers database.
	*
 	* @see EpicPtrs
	*
	* @return new EpicPtrs database
	*
	* @param inFile Input pointer file
	* @param sortFlags Passed to appropriate pointer file reader class used by the pointer file database
	* @param createPtrs Flag to tell SimplePtrReader whether to open files to create full pointer records
  	*
  	* @exception UnkEpicPtrFileException Pointer File is an unknown format.
	*/
	
	private boolean DEBUG = false;
	
	public EpicPtrs createEpicPtrs(File infile, int[] sortFlags, boolean createPtrs, boolean ignoreArgoFlag) throws UnkEpicPtrFileException {
		EpicPtrs mPtrs;
		try {
			mPtrs = new EpicPtrs(infile, sortFlags);
			String inFileName = infile.getName().toLowerCase();

			if (inFileName.indexOf(".zip") > 0) {
				// process a zip file
				mPtrs.setReader(new ZipPtrReader(infile, sortFlags, createPtrs));
				mPtrs.readPtrs();
			}
			else {
				// open pointer file and get the format of the individual entries
				int fileFormat = mPtrs.getFormat();
				
				if (DEBUG)
					System.out.println("file format = " + fileFormat);
				
				// test fileformat and install format specific reader class
				if (fileFormat == PROFILEPTRS)
					mPtrs.setReader(new ProfilePtrReader(infile, sortFlags, ignoreArgoFlag));
				else if (fileFormat == TSPTRS)
					mPtrs.setReader(new TimeSeriesPtrReader(infile, sortFlags));
				else if (fileFormat == SIMPLEPTRS)
					mPtrs.setReader(new SimplePtrReader(infile, sortFlags, createPtrs));
				else if (fileFormat == XMLPTRS)
					mPtrs.setReader(new XMLPtrFileReader(infile));
				else if (fileFormat == ARGOPTRS)
					mPtrs.setReader(new ArgoInvFileReader(infile, sortFlags));
				else if (fileFormat == GTSPPPTRS)
					mPtrs.setReader(new GTSPPInvFileReader(infile, sortFlags));
					
				if (DEBUG)
					System.out.println("file reader = " + mPtrs.getReader());
					
				mPtrs.readPtrs();
				mPtrs.seArgoFlag(mPtrs.getReader().isArgo());
			}
		}
		catch (Exception ex) {
			String err = infile.getName();
			throw new UnkEpicPtrFileException(err);
		}
		return mPtrs;
	}
}

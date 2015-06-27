package gov.noaa.pmel.eps2;

import java.util.*;
import java.io.*;
/**
 * <code>EpicPtrs</code> stores a collection of parsed EPIC pointers
 *
 * @see EpicPtrFactory
 *
 * @author oz
 * @version 1.0
 */
public class EpicPtrs implements EPSConstants {
    /**
     * Storage for the parsed pointer entries.
     */
	protected ArrayList mFilePtrs = null;
    /**
     * Reference to the installed reader for this collection.
     */
	protected PtrFileReader mPtrReader;
    /**
     * Reference to the installed writer for this collection.
     */
	protected PtrFileWriter mPtrWriter;
    /**
     * Source file to parse 
     */
	protected File mFile;
    /**
     * Source URL to parse 
     */
	protected String mURL;
    /**
     * Array of sort keys. Keys are used from left to right order; 
     */
	protected int[] mSortKeys = null;
    /**
     * Counter that shows whether an iterator is active for this collection.
     */
	protected int mIteratorIsActive = 0;
	
	protected boolean mIsXML = false;
	protected PointerFileAttributes mAttributes;
	protected int mFormat = -99;
	protected boolean mIsArgo = false;
	
    /**
     * Zero argument constructor 
     */
	public EpicPtrs() {
	}
	
    /**
     * Construct EpicPtr database  
     *
     * @param inf Source pointer file
     */
	public EpicPtrs(File inf) {
		mFile = inf;
	}
	
    /**
     * Construct EpicPtr database  
     *
     * @param inf Source pointer file
     */
	public EpicPtrs(File inf, boolean isxml) {
		mFile = inf;
		mIsXML = isxml;
	}
	
    /**
     * Construct EpicPtr database  
     *
     * @param inf Source pointer file
     * @param sortKeys Array of sort keys
     */
	public EpicPtrs(File inf, int[] sortKeys) {
		mFile = inf;
		mSortKeys = sortKeys;
	}
	
    /**
     * Opens the source file and reads enough info to try and find out the file format.
     *
     * @return Format Format code (PROFILEPTRS, TSPTRS, or SIMPLEPTRS) of source pointer file
     *
     * @exception UnkEpicPtrFileException Unknown format
     */
	public int getFormat() throws UnkEpicPtrFileException {
		if (mFormat > 0)
			return mFormat;
			
		String lcFilename = mFile.getName().toLowerCase();
		if (lcFilename.indexOf("_argoinv.txt") > 0) {
			mFormat = ARGOPTRS;
 			return mFormat;
		}
		else if (lcFilename.indexOf("_gtsppinv.txt") > 0) {
			mFormat = GTSPPPTRS;
 			return mFormat;
		}
		
		try {
			FileReader fr = new FileReader(mFile);
		    LineNumberReader in = new LineNumberReader(fr, 10000);
			
			// find first non blank line
			String inLine;
			do {
				inLine = in.readLine();
				if (inLine == null) 
					// hit the end of file
					break;
			}
			while (inLine.length() == 0);
			
			if (inLine == null) {
				// hit the end of file
				System.out.println("blank or invalid file names in pointer file");
 				throw new UnkEpicPtrFileException("blank or invalid file names in pointer file");
 			}
 				
 			// look for the string EPIC and either PROFILE or TIME SERIES
 			if (inLine.toUpperCase().indexOf("EPIC") >= 0) {
 				if (inLine.toUpperCase().indexOf("PROFILE") >= 0) {
 					mFormat = PROFILEPTRS;
 					return PROFILEPTRS;
 				}
 				else if (inLine.toUpperCase().indexOf("TIME SERIES") >= 0) {
 					mFormat = TSPTRS;
 					return TSPTRS;
 				}
 			}
			else if (inLine.indexOf("/") >= 0 || inLine.indexOf(":") >= 0 || inLine.indexOf("/") >= 0) {
 				mFormat = SIMPLEPTRS;
				// simple ptr file perhaps
				return SIMPLEPTRS;
			}
			//else if () {
				//TODO: determine whether this is an xml pointer file
			//}
			System.out.println("Threw in EpicPtrs, line 103");
 			throw new UnkEpicPtrFileException();
		}
		catch (IOException ex) {
			System.out.println("Threw in EpicPtrs, line 107");
			throw new UnkEpicPtrFileException();
		}
	}
	
    /**
     * Set the XML attributes object
     *
     * @param attributes object see <code>PointerFileAttributes</code>
     */
	public void setXMLAttributes(PointerFileAttributes attributes) {
		mAttributes = attributes;
	}
	
    /**
     * Set the file reader object
     *
     * @param reader <code>PtrFileReader</code>
     */
	public void setReader(PtrFileReader reader) {
		mPtrReader = reader;
	}
	
    /**
     * Get the file reader object
     *
     * @param reader <code>PtrFileReader</code>
     */
	public PtrFileReader getReader() {
		return mPtrReader;
	}
	
    /**
     * Set the file writer object
     *
     * @param reader <code>PtrFileWriter</code>
     */
	public void setWriter(PtrFileWriter writer) {
		mPtrWriter = writer;
	}
	
    /**
     * Explicitly set the data for this collection. Used to bypass the installed reader.
     *
     * @param an ArrayList of EpicPtr objects
     */
	public void setData(ArrayList inData) {
		mFilePtrs = inData;
	}
	
    /**
     * Explicitly set the data for this collection by adding a single record to the database.
     * Used to bypass the installed reader.
     *
     * @param epPtr Single EpicPtr object
     */
	public void setData(EpicPtr epPtr) {
		if (mFilePtrs == null)
			mFilePtrs = new ArrayList();
		mFilePtrs.add(epPtr);
	}
	
    /**
     * Explicitly set the destination file for this collection.
     *
     * @param f File
     */
	public void setFile(File f) {
		mFile = f;
	}
	
    /**
     * Explicitly set the destination URL for this collection.
     *
     * @param f File
     */
	public void setURL(String s) {
		mURL = s;
	}
	
    /**
     * Writes the pointers to the destination file with the current writer
     *
     * @exception UnkEpicPtrFileException Unknown format
     * @exception PtrWriterNotSetException No pointer writer was installed for this database
     */
	public void writePtrs() throws UnkEpicPtrFileException, PtrWriterNotSetException {
		if (mPtrWriter == null)
			throw new PtrWriterNotSetException();
			
		try {
			if (!mIsXML && mAttributes != null)
				mPtrWriter.write(mFilePtrs, mAttributes);
			else if (mIsXML)
				mPtrWriter.write(mFilePtrs, mAttributes);
			else
				mPtrWriter.write(mFilePtrs);
		}
		catch (Exception ex) {
		ex.printStackTrace();
			throw new UnkEpicPtrFileException();
		}
	}
	
    /**
     * Reads pointer file with the current reader
     *
     * @exception UnkEpicPtrFileException Unknown format
     * @exception PtrReaderNotSetException No pointer reader was installed for this database
     */
	public void readPtrs() throws UnkEpicPtrFileException, PtrReaderNotSetException {
		if (mPtrReader == null)
			throw new PtrReaderNotSetException();
			
		try {
			mFilePtrs = mPtrReader.parse();
		}
		catch (Exception ex) {
		ex.printStackTrace();
			System.out.println(mPtrReader + " Threw in EpicPtrs, in readPtrs");
			throw new UnkEpicPtrFileException();
		}
	}
	
	/**
	 * return an iterator
	 *
	 * @return A reference to a PointerDBIterator
	 */
	public PointerDBIterator iterator() {
		mIteratorIsActive++;
		String name = null;
		// file can be null if source is a URL
		try {
			name = mFile.getName();
		}
		catch (Exception ex) {
			name = mURL;
		}
		PointerDBIterator pdbi = new PointerDBIterator(mFilePtrs, name);
		return pdbi;
	}
	
	/**
	 * sort the pointer file database
	 *
     * @exception CantSortItorActiveExcept Couldn't sort because an iterator is active for this database
	 */
	public void doSort() throws CantSortItorActiveExcept {
		if (mIteratorIsActive > 0)
			throw new CantSortItorActiveExcept();
		
		//
		try {
		if (mSortKeys != null) {
			for (int i=0; i< mSortKeys.length; i++) {
				if (mSortKeys[i] > 0) {
					// sort key defined: set the sort key for individual pointer file entries
					for (int p=0; p<mFilePtrs.size(); p++) {
				    	EpicPtr ep = (EpicPtr)mFilePtrs.get(p);       	
				    	ep.setSortKey(mSortKeys[i]);
				    }
					Collections.sort(mFilePtrs);
				}
			}
		}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * set the sort keys for individual ptr file objects
	 *
	 * @param sortKeys Array of sort keys
	 */
	public void setSort(int[] sortKeys) {
		mSortKeys = sortKeys;
	}
	
	/**
	 * Flush the active iterator
	 *
	 */
	public void flushActiveItor() {
		mIteratorIsActive--;
	}
	
	public void dumpPtrs() {
		Iterator itor = mFilePtrs.iterator();
		while (itor.hasNext()) {
			EpicPtr ep = (EpicPtr)itor.next();
			System.out.println(ep);
		}
	}
	
	public boolean isArgo() {
		return mIsArgo;
	}
	
	public void seArgoFlag(boolean b) {
		mIsArgo = b;
	}
}

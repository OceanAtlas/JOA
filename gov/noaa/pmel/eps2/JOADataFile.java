package gov.noaa.pmel.eps2;

import java.util.*;

/**
 * <code>JOAStation</code> A wrapper class for metadata that defines a data file
 * in Java OceanAtlas.
 *
 * @see JOASectionReader
 *
 * @author oz
 * @version 1.0
 */
public class JOADataFile {
    protected int mOrdinal = 0;
	protected String mName;
	protected Vector mSections = new Vector();
	protected int mNumSections;
	protected String mFileComments = null;
	private String mWOCEHeader;
    
    public JOADataFile(String filename) {
    	mOrdinal++;
    	mName = new String(filename);
	}
	
	public void addWOCEHeader(String s) {
		mWOCEHeader = s;
	}
}
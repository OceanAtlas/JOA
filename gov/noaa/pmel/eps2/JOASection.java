package gov.noaa.pmel.eps2;

import java.util.*;
import java.awt.*;

/**
 * <code>JOAStation</code> A wrapper class for metadata that defines a section 
 * in Java OceanAtlas.
 *
 * @see JOASectionReader
 *
 * @author oz
 * @version 1.0
 */
public class JOASection {
    protected int mOrdinal;
    protected Vector mStations = new Vector();
    protected String mSectionDescription;
    protected String mShipCode;
    protected int mNumCasts=0, mNumVars=0;
    protected int mNumProperties = 0;
    
    public JOASection(int ord, String descrip, String ship, int numCasts, int numVars) {
		mOrdinal = ord;
		mSectionDescription = new String(descrip);
		mShipCode = new String(ship);
		mNumCasts = numCasts;
		mNumVars = numVars;
	}
}
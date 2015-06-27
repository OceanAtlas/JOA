/*
 * $Id: BathyOverlaySpec.java,v 1.6 2005/06/17 17:24:16 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */
package ndEdit;

import javax.swing.*;
import java.util.*;
import java.awt.*;

public class BathyOverlaySpec  { 
    public int mNumIsobaths = 0;   
	public double[] mIsobathValues = new double[120];
    public Color[] mIsobathColors = new Color[120];
    public String[] mIsobathPaths = new String[120];
    public String[] mIsobathDescrips = new String[120];
    public Color mCoastColor = Color.black;
    public int mCoastWeight = 1;

	public BathyOverlaySpec() {
	}
	
	public BathyOverlaySpec(BathyOverlaySpec inSpec) {
		for (int i=0; i<inSpec.mNumIsobaths; i++) {
			mIsobathValues[i] = inSpec.mIsobathValues[i];
			mIsobathPaths[i] = inSpec.mIsobathPaths[i];
			mIsobathColors[i] = inSpec.mIsobathColors[i];
			mIsobathDescrips[i] = inSpec.mIsobathDescrips[i];
		}
		mNumIsobaths = inSpec.mNumIsobaths;
		mCoastColor = inSpec.mCoastColor;
		mCoastWeight = inSpec.mCoastWeight;
    }
    
    public int getNumIsobaths() {
    	return mNumIsobaths;
    }
    
    public void setNumIsobaths(int i) {
    	mNumIsobaths = i;
    }
    
    public double getValue(int i) {
    	return mIsobathValues[i];
    }
    
    public Color getColor(int i) {
    	return mIsobathColors[i];
    }
    
    public String getPath(int i) {
    	return mIsobathPaths[i];
    }
    
    public String getDescrip(int i) {
    	return mIsobathDescrips[i];
    }
    
    public boolean isCustom(int i) {
    	return (mIsobathPaths[i].length() > 0);
    }
    
    public Color getCoastColor() {
    	return mCoastColor;
    }
    
    public int getCoastWeight() {
    	return mCoastWeight;
    }
}
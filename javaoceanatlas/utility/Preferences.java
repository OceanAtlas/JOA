/*
 * $Id: Preferences.java,v 1.2 2005/06/17 18:10:59 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.io.*;

@SuppressWarnings("serial")
public class Preferences implements Serializable {
    Color mPlotFrameColor = null;
    Color mPlotContentsColor = null;
    Color mMissingValColor = null;
    int mLineWidth;
    
    public Preferences(int lineWidth, Color plotFrameColor, Color plotContentsColor, Color missingValColor) {
    	mLineWidth = lineWidth;
    	mPlotFrameColor = plotFrameColor;
    	mPlotContentsColor = plotContentsColor;
    	mMissingValColor = missingValColor;
	}
}
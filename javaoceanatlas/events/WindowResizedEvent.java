/*
 * $Id: WindowResizedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.*;

public class WindowResizedEvent extends AWTEvent {
	Dimension mNewDimension = null;
	Component mSource = null;
	
	public WindowResizedEvent(FileViewer f) {
		super(f, WIND_RESIZED_EVENT);
	}
	
	public static final int WIND_RESIZED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 6666 + 1010;
		
	public void setNewDimension(Dimension nd) {
		mNewDimension = new Dimension(nd.width, nd.height);
	}
		
	public void setNewDimension(Dimension nd, Component c) {
		mNewDimension = new Dimension(nd.width, nd.height);
		mSource = c;
	}
	
	public Component getEvtSource() {
		return mSource;
	}
	
	public Dimension getNewDimensions() {
		return mNewDimension;
	}
	
	public int getWidth() {
		return mNewDimension.width;
	}
	
	public int getHeight() {
		return mNewDimension.height;
	}
	
	public void consume() {
		super.consume();
	}
}
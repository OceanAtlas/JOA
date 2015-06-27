/*
 * $Id: ParameterAddedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
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
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.*;
import javaoceanatlas.ui.*;

public class ParameterAddedEvent extends AWTEvent {
	Section mFoundSection = null;
	Station mFoundStation = null;
	
	public ParameterAddedEvent(FileViewer f) {
		super(f, PARAMETER_ADDED_EVENT);
	}
	
	public static final int PARAMETER_ADDED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 77777;
		
		public void setFoundObs(Section s, Station st) {
		mFoundSection = s;
		mFoundStation = st;
	}
	
	public Section getFoundSection() {
		return mFoundSection;
	}
	
	public Station getFoundStation() {
		return mFoundStation;
	}

	public void consume() {
		super.consume();
	}
}
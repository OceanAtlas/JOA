/*
 * $Id: PrefsChangedEvent.java,v 1.3 2005/06/17 18:02:59 oz Exp $
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
import javaoceanatlas.*;

public class PrefsChangedEvent extends AWTEvent {
	public PrefsChangedEvent(PowerOceanAtlas f) {
		super(f, PREFS_CHANGED_EVENT);
	}
	
	public static final int PREFS_CHANGED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 77177;
	
	public void consume() {
		super.consume();
	}
}
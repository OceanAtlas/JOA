/*
 * $Id: ObsFilterChangedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.*;

public class ObsFilterChangedEvent extends AWTEvent {
	public ObsFilterChangedEvent(FileViewer f) {
		super(f, OBS_FILTER_CHANGED_EVENT);
	}
	
	public static final int OBS_FILTER_CHANGED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 77377;
	
	public void consume() {
		super.consume();
	}
}
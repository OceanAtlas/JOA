/*
 * $Id: DataAddedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.*;

public class DataAddedEvent extends AWTEvent {
	public DataAddedEvent(FileViewer f) {
		super(f, DATA_ADDED_EVENT);
	}
	
	public static final int DATA_ADDED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 77677;
	
	public void consume() {
		super.consume();
	}
}
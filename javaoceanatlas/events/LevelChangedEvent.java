/*
 * $Id: LevelChangedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.utility.*;
	   
public class LevelChangedEvent extends AWTEvent {
	private Vector<LevelChange> mLevelChanges = new Vector<LevelChange>();
	
	public LevelChangedEvent(FileViewer f) {
		super(f, OBS_CHANGED_EVENT);
	}
	
	public static final int OBS_CHANGED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 6631;
		
	public void addLevel(LevelChange level) {
		mLevelChanges.add(level);
	}
	
	public Vector<LevelChange> getLevel() {
		return mLevelChanges;
	}
		
	public void consume() {
		super.consume();
	}

}
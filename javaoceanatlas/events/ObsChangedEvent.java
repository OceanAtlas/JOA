/*
 * $Id: ObsChangedEvent.java,v 1.4 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.*;
import javaoceanatlas.ui.*;

public class ObsChangedEvent extends AWTEvent {
	OpenDataFile mFoundFile = null;
	Section mFoundSection = null;
	Station mFoundStation = null;
	Bottle mFoundBottle = null;
	Component mSource = null;
	int mDirection;
	boolean sendEventToMyself = true;
	
	public ObsChangedEvent(FileViewer f) {
		super(f, OBS_CHANGED_EVENT);
	}
	
	public static final int OBS_CHANGED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 6666;
		
	public void setFoundObs(int dir, OpenDataFile f, Section s, Station st, Bottle b) {
		mDirection = dir;
		mFoundFile = f;
		mFoundSection = s;
		mFoundStation = st;
		mFoundBottle = b;
	}
		
	public void setFoundObs(int dir, OpenDataFile f, Section s, Station st, Bottle b, Component c) {
		mDirection = dir;
		mFoundFile = f;
		mFoundSection = s;
		mFoundStation = st;
		mFoundBottle = b;
		mSource = c;
	}
		
	public void setFoundObs(OpenDataFile f, Section s, Station st, Bottle b) {
		mDirection = -99;
		mFoundFile = f;
		mFoundSection = s;
		mFoundStation = st;
		mFoundBottle = b;
	}
		
	public void setFoundObs(OpenDataFile f, Section s, Station st, Bottle b, Component c) {
		mDirection = -99;
		mFoundFile = f;
		mFoundSection = s;
		mFoundStation = st;
		mFoundBottle = b;
		mSource = c;
	}
	
	public Component getEvtSource() {
		return mSource;
	}
	
	public OpenDataFile getFoundFile() {
		return mFoundFile;
	}
	
	public Section getFoundSection() {
		return mFoundSection;
	}
	
	public Station getFoundStation() {
		return mFoundStation;
	}
	
	public Bottle getFoundBottle() {
		return mFoundBottle;
	}
	
	public int getDirection() {
		return mDirection;
	}
	
	public boolean isSendToMyself() {
		return sendEventToMyself;
	}
	
	public void setSendToMyself(boolean b) {
		sendEventToMyself = b;
	}
	
	public void consume() {
		super.consume();
	}
}
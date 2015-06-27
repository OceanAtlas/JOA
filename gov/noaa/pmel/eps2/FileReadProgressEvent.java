package gov.noaa.pmel.eps2;

import java.awt.*;
import java.awt.event.*;

public class FileReadProgressEvent extends AWTEvent {
	private double mPercent;
	
	public FileReadProgressEvent(Dbase db) {
		super(db, FILE_READ_PROGRESS_EVENT);
	}
	
	public void setPercent(double pct) {
		mPercent = pct;
	}
	
	public double getPercent() {
		return mPercent;
	}
	
	public static final int FILE_READ_PROGRESS_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 8889674;
}
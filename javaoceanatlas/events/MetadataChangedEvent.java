/*
 * $Id: MetadataChangedEvent.java,v 1.2 2005/06/17 18:02:59 oz Exp $
 *
 */

package javaoceanatlas.events;

import java.awt.*;
import java.awt.event.*;
import javaoceanatlas.ui.*;
import gov.noaa.pmel.util.*;

public class MetadataChangedEvent extends AWTEvent {
	GeoDate mMinDate;
	GeoDate mMaxDate;
	double mLatMin;
	double mLatMax;
	double mLonMin;
	double mLonMax;
	
	public MetadataChangedEvent(FileViewer f) {
		super(f, METADATA_CHANGED_EVENT);
	}
	
	public static final int METADATA_CHANGED_EVENT = 
		AWTEvent.RESERVED_ID_MAX + 8678;
		
	public void setMetadata(double minlat, double maxlat, double minlon, double maxlon, GeoDate mindate, GeoDate maxdate) {
		mMinDate = new GeoDate(mindate);
		mMaxDate = new GeoDate(maxdate);
		mLatMin = minlat;
		mLatMax = maxlat;
		mLonMin = minlon;
		mLonMax = maxlon;
	}
	
	public GeoDate getMinDate() {
		return mMinDate;
	}
	
	public GeoDate getMaxDate() {
		return mMaxDate;
	}
	
	public double getMinLat() {
		return mLatMin;
	}
	
	public double getMaxLat() {
		return mLatMax;
	}
	
	public double getMinLon() {
		return mLonMin;
	}
	
	public double getMaxLon() {
		return mLonMax;
	}
}
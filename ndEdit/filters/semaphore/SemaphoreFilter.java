package ndEdit.filters.semaphore;

import ndEdit.*;
import ndEdit.filters.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.AWTEvent;

public class SemaphoreFilter extends FilteredDataManager {

  private Object mParent;
  private PointerCollectionGroup pc;
  private int sz;
  private byte[] results;
  // Individual Filters
  private byte numIndividualFilters = 4;
  private LatFilter 	latFilter;
  private DepthFilter 	depthFilter;
  private LonFilter 	lonFilter;
  private TimeFilter 	timeFilter;

  // constructor
  //
	public SemaphoreFilter(Object parent) {
		super(parent);
		mParent = parent;

     latFilter = new LatFilter(	-95f, 	95f);
     depthFilter = new DepthFilter(	0f, 	10000f);
     timeFilter = new TimeFilter(	0, 	0);
     lonFilter = new LonFilter(	-360f, 	360f);
     
		//latFilter = new LatFilter();
		//lonFilter = new LonFilter();
		////depthFilter = new DepthFilter();
		//timeFilter = new TimeFilter();
		//monthFilter = new MonthFilter();
	}


	public void newPointerCollection(PointerCollectionGroup pcg) {
		if (pcg == null)
			return;
		this.pc = pcg;
		sz = pc.getSize();
		results = null;
		results = new byte[sz];
		// Initialize results so that all individual filter vote yes.
		for (int i = 0; i < sz; i++) {
			//results[i] = numIndividualFilters;
			results[i] = 0;  // have them all vote "no"
		}
		latFilter.newPointerCollection(pc, results);
		depthFilter.newPointerCollection(pc, results);
		timeFilter.newPointerCollection(pc, results);
		lonFilter.newPointerCollection(pc, results);
		//monthFilter.newPointerCollection(pc, results);
		if (Debug.DEBUG_FILTER)
			System.out.println("After new pointer collection: \n" + this.toString(0));
		createFreshlyFilteredData_Event();
	}

	public void createFreshlyFilteredData_Event() {
		DataChangedEvent dce = new DataChangedEvent(mParent);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(dce);
	}

  public byte[] getResults() {
    return results;
  }
  
  public void reset() {
  	latFilter.reset();
	depthFilter.reset();
	timeFilter.reset();
	lonFilter.reset();
  }

  public void latStartChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Lat start changed from: " + oldValue + " to: " +
	newValue);
     latFilter.startChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After new pointer collection: \n" + this.toString(0));
     if (Debug.DEBUG_FILTER) System.out.println("After lat start change: \n" + this.toString(10));
  }

  	public void latStopChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     	if (Debug.DEBUG_FILTER) System.out.println(" Lat stop changed from: " + oldValue + " to: " + newValue);
     	latFilter.stopChanged(oldValue, newValue);
     	if (Debug.DEBUG_FILTER) System.out.println("After new pointer collection: \n" + this.toString(0));
     	if (Debug.DEBUG_FILTER) System.out.println("After lat stop change: \n" + this.toString(10));
  	}

  public void lonStartChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Lon start changed from: " + oldValue + " to: " +
	newValue);
     lonFilter.startChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After lon start change: \n" + this.toString(10));
  }

  public void lonStopChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Lon stop changed from: " + oldValue + " to: " +
	newValue);
     lonFilter.stopChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After lon stop change: \n" + this.toString(10));
  }

  public void depthStartChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Depth start changed from: " + oldValue + " to: " +
	newValue);
     depthFilter.startChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After depth change: \n" + this.toString(10));
  }

  public void depthStopChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Depth stop changed from: " + oldValue + " to: " +
	newValue);
     depthFilter.stopChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After Depth stop change: \n" + this.toString(10));
  }

  public void timeStartChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Time start changed from: " + oldValue + " to: " +
	newValue);
     timeFilter.startChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After time change: \n" + this.toString(10));
  }

  public void timeStopChanged(double oldValue, double newValue) {
if (Debug.DEBUG_FILTER) System.out.println("semaphore--------------------------> ");
     if (Debug.DEBUG_FILTER) System.out.println(" Time stop changed from: " + oldValue + " to: " +
	newValue);
     timeFilter.stopChanged(oldValue, newValue);
     if (Debug.DEBUG_FILTER) System.out.println("After time stop change: \n" + this.toString(10));
  }

	public void monthStartChanged(int oldValue, int newValue) {
		if (Debug.DEBUG_FILTER)
			System.out.println("semaphore--------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Time start changed from: " + oldValue + " to: " + newValue);
		//monthFilter.startChanged(oldValue, newValue);
		if (Debug.DEBUG_FILTER)
			System.out.println("After time change: \n" + this.toString(10));
	}

	public void monthStopChanged(int oldValue, int newValue) {
		if (Debug.DEBUG_FILTER)
			System.out.println("semaphore--------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Time stop changed from: " + oldValue + " to: " + newValue);
		//monthFilter.stopChanged(oldValue, newValue);
		if (Debug.DEBUG_FILTER)
			System.out.println("After time stop change: \n" + this.toString(10));
	}


  public String toString(int typ) {
     StringBuffer str = new StringBuffer();
     double[] lats = pc.getLatArr1();
     double[] late = pc.getLatArr2();
     double[] lons = pc.getLonArr1();
     double[] lone = pc.getLonArr2();
     double[] ds = pc.getDepthArr1();
     double[] de = pc.getDepthArr2();
     double[] ts = pc.getTimeArr1();
     double[] te = pc.getTimeArr2();
     double[] latss = pc.getLatArr1Sorted();
     if (typ == 0) {
/*
        str.append("Unsorted  Sorted\n");
        for (int i = 0; i < sz; i++) {
          str.append(lats[i] + "   " + latss[i] + "   : " + results[i] + "\n");
        }
        str.append("End\n");
*/
     }
     else if (typ == 1) {
     }
     else if (typ == 2) {
	/*
        str.append("DArr1  DArr2  LatArr1 \n");
        for (int i = 0; i < sz; i++) {
          str.append(ds[i] + "   " + de[i] + "   " + lats[i] +
		"   : " + results[i] + "\n");
        }
        str.append("End\n");
	*/
     }
     else if (typ == 10) {
/*
	str.append("Lat beg & end: " + latFilter.getBegValue() + "  " +
		latFilter.getEndValue() + " \n");
	str.append("Lon beg & end: " + lonFilter.getBegValue() + "  " +
		lonFilter.getEndValue() + " \n");
	str.append("Depth beg & end: " + depthFilter.getBegValue() + "  " +
		depthFilter.getEndValue() + " \n");
	str.append("Time beg & end: " + timeFilter.getBegValue() + "  " +
		timeFilter.getEndValue() + " \n");
        str.append("LatS  LatE  LonS  LonE  DStrt DEnd  TimS TimE \n");
        for (int i = 0; i < sz; i++) {
	  str.append(lats[i] + "  ");
	  if (late == null) str.append("      "); else str.append(late[i] + "  ");
	  str.append(lons[i] + "  ");
	  if (lone == null) str.append("      "); else str.append(lone[i] + "  ");
	  str.append(ds[i] + "  ");
	  if (de == null) str.append("      "); else str.append(de[i] + "  ");
	  str.append(ts[i] + "  ");
	  if (te == null) str.append("      "); else str.append(te[i] + "  ");
	  str.append("  " + results[i] + "\n");
        }
        str.append("End\n");
*/
     }

     return(str.toString());
  }

}

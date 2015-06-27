
package ndEdit.filters.simple;

import ndEdit.*;
import ndEdit.filters.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class SimpleFilter extends FilteredDataManager {

  private Object parent;
  private LatFilter latFilter;
  private LonFilter lonFilter;
  private DepthFilter depthFilter;
  private TimeFilter timeFilter;
  private boolean[] latResults;
  private boolean[] lonResults;
  private boolean[] depthResults;
  private boolean[] timeResults;
  private PointerCollection pc;
  private int sz;
  private byte[] results;
  private byte[] resultsCompare;

  // constructor
  //
  public SimpleFilter(Object parent) {
     super(parent);

     latFilter = new LatFilter(	-95f, 	95f);
     depthFilter = new DepthFilter(	0f, 	10000f);
     timeFilter = new TimeFilter(	0, 	0);
     lonFilter = new LonFilter(	-360f, 	360f);
  }
  
  public void reset() {
  }

	public void createFreshlyFilteredData_Event() {
		DataChangedEvent dce = new DataChangedEvent((NdEdit)parent);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(dce);
	}
	public void newPointerCollection(PointerCollection pc) {
		this.pc = pc;
		this.sz = pc.getSize();
		latResults = new boolean[sz];
		lonResults = new boolean[sz];
		depthResults = new boolean[sz];
		timeResults = new boolean[sz];
		//
		// all results are initialized to true
		//
		for (int i = 0; i < sz; i++) {
			latResults[i] = true;
			lonResults[i] = true;
			timeResults[i] = true;
			depthResults[i] = true;
		}
		results = new byte[sz];

		latFilter.newPointerCollection(pc, latResults);
		lonFilter.newPointerCollection(pc, lonResults);
		depthFilter.newPointerCollection(pc, depthResults);
		timeFilter.newPointerCollection(pc, timeResults);
	}

  public byte[] getResults() {
    return results;
  }

  public void mergeResults() {
     boolean comparing = true;
	if (comparing) {
	   resultsCompare = new byte[sz];
	}

     for (int i = 0; i < sz; i++) {
	boolean r = depthResults[i] && latResults[i] && lonResults[i] &&
		timeResults[i];
	if (r)
	   results[i] = 1;
	else
	   results[i] = 0;


	if (comparing) {
	   if (depthResults[i]) resultsCompare[i]++;
	   if (latResults[i]) resultsCompare[i]++;
	   if (lonResults[i]) resultsCompare[i]++;
	   if (timeResults[i]) resultsCompare[i]++;
        }
     }
     if (Debug.DEBUG_FILTER) System.out.println("Simple Filter Results: \n" + this.toString(10));
  }
  
	public void latStartChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER) System.out.println(" Lat start changed from: " + oldValue + " to: " +
		newValue);
		latFilter.startChanged(oldValue, newValue);
		mergeResults();
	}
	
	public void latStopChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Lat stop changed from: " + oldValue + " to: " + newValue);
		latFilter.stopChanged(oldValue, newValue);
		mergeResults();
	}

	public void lonStartChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Lon start changed from: " + oldValue + " to: " + newValue);
		lonFilter.startChanged(oldValue, newValue);
		mergeResults();
	}
	
	public void lonStopChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Lon stop changed from: " + oldValue + " to: " + newValue);
		lonFilter.stopChanged(oldValue, newValue);
		mergeResults();
	}

	public void depthStartChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Depth start changed from: " + oldValue + " to: " + newValue);
		depthFilter.startChanged(oldValue, newValue);
		mergeResults();
	}
	
	public void depthStopChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Depth stop changed from: " + oldValue + " to: " + newValue);
		depthFilter.stopChanged(oldValue, newValue);
		mergeResults();
	}

	public void timeStartChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Time stop changed from: " + oldValue + " to: " + newValue);
		timeFilter.startChanged(oldValue, newValue);
		mergeResults();
	}
	
	public void timeStopChanged(double oldValue, double newValue) {
		if (Debug.DEBUG_FILTER) 
			System.out.println("simple--------------------------------> ");
		if (Debug.DEBUG_FILTER)
			System.out.println(" Time stop changed from: " + oldValue + " to: " + newValue);
		timeFilter.stopChanged(oldValue, newValue);
		mergeResults();
	}

  public String toString(int typ) {
     StringBuffer str = new StringBuffer();
	System.out.println("size is: " + sz);
	System.out.println("pc is: " + pc);
     if (pc != null) {
     double[] lats = pc.getLatArr1();
     double[] late = pc.getLatArr2();
     double[] lons = pc.getLonArr1();
     double[] lone = pc.getLonArr2();
     double[] ds = pc.getDepthArr1();
     double[] de = pc.getDepthArr2();
     double[] ts = pc.getTimeArr1();
     double[] te = pc.getTimeArr2();
        if (typ == 0) {
        }
        else if (typ == 1) {
        }
        else if (typ == 2) {
        }
        else if (typ == 10) {
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
        }

   }
   return(str.toString());
  }

  public void compareToSemaphore(byte[] semResults) {
     double[] lats = pc.getLatArr1();
     double[] late = pc.getLatArr2();
     double[] lons = pc.getLonArr1();
     double[] lone = pc.getLonArr2();
     double[] ds = pc.getDepthArr1();
     double[] de = pc.getDepthArr2();
     double[] ts = pc.getTimeArr1();
     double[] te = pc.getTimeArr2();
System.out.println("************************ Comparing Results **************");
     StringBuffer str = new StringBuffer();
     str.append("LatS  LatE  LonS  LonE  DStrt DEnd  TimS TimE Sem Sim\n");
	int cnt = 0;
     for (int i = 0; i < sz; i++) {
	//
	// Semaphore result is on if not 0
        //
	if (semResults[i]  != resultsCompare[i]) {

	  cnt++;
	  str.append(lats[i] + "  ");
	  if (late == null) str.append("      "); else str.append(late[i] + "  ");
	  str.append(lons[i] + "  ");
	  if (lone == null) str.append("      "); else str.append(lone[i] + "  ");
	  str.append(ds[i] + "  ");
	  if (de == null) str.append("      "); else str.append(de[i] + "  ");
	  str.append(ts[i] + "  ");
	  if (te == null) str.append("      "); else str.append(te[i] + "  ");
	  str.append("  " + semResults[i] + "  " + resultsCompare[i] + "\n");
        }
     }
     if (Debug.DEBUG_FILTER) System.out.println(str.toString());
     System.out.println("Number of inconsistent filtered items: " + cnt);
System.out.println("************************ End Compare **************");
   }

}

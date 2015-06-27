package ndEdit.filters;

import ndEdit.*;
import ndEdit.filters.*;
import ndEdit.filters.simple.*;
import ndEdit.filters.semaphore.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;


/** @note Filtered Data Manager Interface:
  * 
  *   - sends out notifies when new filtered-data action occurs
  *  - provides access to filtered data*/

public abstract class FilteredDataManager implements ChangeableInfoListener {
   public abstract void createFreshlyFilteredData_Event();
//   public abstract int createFilter();
//   public abstract int clearFilter();
/** @directed
  * @clientRole Creates*/
  /*#  NewFilteredData_Info lnkUnnamed*/

   public abstract void latStartChanged(double oldValue, double newValue);
   public abstract void latStopChanged(double oldValue, double newValue);
   public abstract void lonStartChanged(double oldValue, double newValue);
   public abstract void lonStopChanged(double oldValue, double newValue);
   public abstract void depthStartChanged(double oldValue, double newValue);
   public abstract void depthStopChanged(double oldValue, double newValue);
   public abstract void timeStartChanged(double oldValue, double newValue);
   public abstract void timeStopChanged(double oldValue, double newValue);
   public void newPointerCollection(PointerCollectionGroup pc) {}
   public abstract byte[] getResults();
   public abstract void reset();

   private Object parent;
   private CIMFacade anc;
   private byte[] b2;
   private ndEdit.filters.simple.SimpleFilter simFil;
   private ndEdit.filters.semaphore.SemaphoreFilter semFil;
   
   //-----------------------------------------------------------------------
   //
   // constructor
   //
	public FilteredDataManager(Object parent) {
		this.parent = parent;
		if (!(parent instanceof CutPanelView))
			anc = (CIMFacade)getAncestor();
		else
			anc = (CIMFacade)parent;
		if (anc != null) 
			anc.addChangeableInfoListener(this);
	}

   //-----------------------------------------------------------------------
   //
   public Object getAncestor() {
      if (parent != null) {
         return ((Lineage)parent).getAncestor();
      }
      return this;
   }

   // ---------------------------------------------------------------------
   //
	public void popChangeableInfo(ChangeableInfo ci) {
		//if (Debug.DEBUG_FILTER) 
		if (ci.getId().equals("LatitudeStart")) {
			latStartChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("LatitudeStop")) {
			latStopChanged((double)((Double) ci.getOldValue()).doubleValue(),(double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("LongitudeStart")) {
			lonStartChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("LongitudeStop")) {
			lonStopChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("DepthStart")) {
			depthStartChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("DepthStop")) {
			depthStopChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("TimeStart")) {
			timeStartChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
		else if (ci.getId().equals("TimeStop")) {
			timeStopChanged((double)((Double) ci.getOldValue()).doubleValue(), (double)((Double) ci.getNewValue()).doubleValue());
			newFilteredData();
		}
	}

	// ---------------------------------------------------------------------
	//
	public void newFilteredData() {
		boolean undoable = false;
		anc.pushChangeableInfo("NewFilteredData", new FilteredData(this, this.getResults()),
	             				new FilteredData(this, this.getResults()), undoable);
	}

}

 class FilteredData {

   Object source;
   byte[] results;
   public FilteredData(Object source,
			byte[] results) {
      this.source = source;
      this.results = results;
   }
 }

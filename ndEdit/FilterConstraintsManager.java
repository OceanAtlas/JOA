 /*
 * $Id: FilterConstraintsManager.java,v 1.6 2005/03/23 23:52:21 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

 package ndEdit;

  import java.net.URL;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
       
//
// Contains the set of filter constraints that's currently active.
//
public class FilterConstraintsManager implements ChangeableInfoListener {
  private Object parentObject;
  CIMFacade rootObject;
  private FilterConstraints fc;
  private FilterConstraints lastfc;
  private FilterConstraints deffc;
  //
  // Defaults:
  //
  private double[] latRange = {-90.0f, 90.0f};
  private double[] lonRange = {-180.0f, 180.0f};
  private double[] depthRange = {0.0f, 3000.0f};
  private double[] timeRange = {0.0f,0.0f};
  private String[] standardVariables;
  private String[] additionalFields;

  // ---------------------------------------------------------------------
  //
  // The default, no-args constructor initializes the filter constraints
  //  to a set of hard-coded constraints.
  //
  public FilterConstraintsManager(Object parentObject) {
     this.setParent(parentObject);
     if (!(parentObject instanceof CutPanelView))
     	rootObject = (CIMFacade)getAncestor();
     else
     	rootObject = (CIMFacade)parentObject;
     if (rootObject != null) 
     	rootObject.addChangeableInfoListener(this);

     deffc = new FilterConstraints(latRange,
				lonRange,
				depthRange,
				timeRange,
				standardVariables,
				additionalFields);

     fc = new FilterConstraints(latRange,
				lonRange,
				depthRange,
				timeRange,
				standardVariables,
				additionalFields);

     lastfc = new FilterConstraints(latRange,
				lonRange,
				depthRange,
				timeRange,
				standardVariables,
				additionalFields);

     notifyFilterChange(true);

  }
  // ---------------------------------------------------------------------
  //
  // 
  public FilterConstraints getFilterConstraints() {
     return fc;
  }

  // ---------------------------------------------------------------------
  //
  // Notify others that filter constraints have been set.
  //  If there's no net change in the value, set undoable to false, but
  //  still send the message (for the initial case).
  //  
  //
  public void notifyFilterChange(boolean undoit) {
  	rootObject.pushChangeableInfo("StartBatch", 
				null, 
				null, 
				true);
  
    boolean undoable;
    undoable = undoit && (lastfc.latRange[0] != fc.latRange[0]);
    rootObject.pushChangeableInfo("LatitudeStart", 
				new Double(lastfc.latRange[0]), 
				new Double(fc.latRange[0]), 
				undoable);

    undoable = undoit && (lastfc.latRange[1] != fc.latRange[1]);
    rootObject.pushChangeableInfo("LatitudeStop",  
				new Double(lastfc.latRange[1]), 
				new Double(fc.latRange[1]), 
				undoable);

    undoable = undoit && (lastfc.lonRange[0] != fc.lonRange[0]);
    rootObject.pushChangeableInfo("LongitudeStart",
				new Double(lastfc.lonRange[0]), 
				new Double(fc.lonRange[0]), 
				undoable);

    undoable = undoit && (lastfc.lonRange[1] != fc.lonRange[1]);
    rootObject.pushChangeableInfo("LongitudeStop", 
				new Double(lastfc.lonRange[1]), 
				new Double(fc.lonRange[1]), 
				undoable);

    undoable = undoit && (lastfc.depthRange[0] != fc.depthRange[0]);
    rootObject.pushChangeableInfo("DepthStart",  
				new Double(lastfc.depthRange[0]), 
				new Double(fc.depthRange[0]), 
				undoable);

    undoable = undoit && (lastfc.depthRange[1] != fc.depthRange[1]);
    rootObject.pushChangeableInfo("DepthStop",   
				new Double(lastfc.depthRange[1]), 
				new Double(fc.depthRange[1]), 
				undoable);

    undoable = undoit && (lastfc.timeRange[0] != fc.timeRange[0]);
    rootObject.pushChangeableInfo("TimeStart",   
				new Double(lastfc.timeRange[0]), 
				new Double(fc.timeRange[0]), 
				undoable);

    undoable = undoit && (lastfc.timeRange[1] != fc.timeRange[1]);
    rootObject.pushChangeableInfo("TimeStop",    
				new Double(lastfc.timeRange[1]), 
				new Double(fc.timeRange[1]), 
				undoable);
  	rootObject.pushChangeableInfo("EndBatch", 
				null, 
				null, 
				true);

  }


   // ---------------------------------------------------------------------
   //
   // compares filter constraints with defaults.  Thus, can determine if
   // user has set constraints purposefully.  (For case when a new pointer 
   // collection is loaded, we can simply use the extents of the pointer 
   // collection as the filter extents.  If constraints have been purposefully 
   // set, need to constrain the constraints to the bounds of the new
   // pointer collection 
   //
   boolean theseAreDefaults() {
      if (fc.latRange[0] != deffc.latRange[0]) return false;
      if (fc.latRange[1] != deffc.latRange[1]) return false;
      if (fc.lonRange[0] != deffc.lonRange[0]) return false;
      if (fc.lonRange[1] != deffc.lonRange[1]) return false;
      if (fc.depthRange[0] != deffc.depthRange[0]) return false;
      if (fc.depthRange[1] != deffc.depthRange[1]) return false;
      if (fc.timeRange[0] != deffc.timeRange[0]) return false;
      if (fc.timeRange[1] != deffc.timeRange[1]) return false;
      return true;
   }

   // ---------------------------------------------------------------------
   //
   //
   public void resetToDefaults() {
     resetFilterConstraints(deffc);
   }


   // ---------------------------------------------------------------------
   //
   // these constraints actually get set via the pushChangeableInfo -> 
   //  popChangeableInfo mechanism.
   //
	public void resetFilterConstraints(FilterConstraints ifc) {
		boolean undoable = true;
		rootObject.pushChangeableInfo("FilterConstraints", fc, ifc, undoable);
	}
	
   // ---------------------------------------------------------------------
   //
   // Check for any changeable info relating to filter variables
   //
	public void popChangeableInfo(ChangeableInfo ci) {
		if (ci.getId().equals("LatitudeStart")) {
			fc.latRange[0] = ((Double) ci.getNewValue()).floatValue();
		}
		else if (ci.getId().equals("LatitudeStop")) {
			fc.latRange[1] = ((Double) ci.getNewValue()).floatValue();
		}
		else if (ci.getId().equals("LongitudeStart")) {
			fc.lonRange[0] = ((Double) ci.getNewValue()).floatValue();
		}
		else if (ci.getId().equals("LongitudeStop")) {
			fc.lonRange[1] = ((Double) ci.getNewValue()).floatValue();
		}
		else if (ci.getId().equals("DepthStart")) {
			fc.depthRange[0] = ((Double) ci.getNewValue()).floatValue();
		}
		else if (ci.getId().equals("DepthStop")) {
			fc.depthRange[1] = ((Double) ci.getNewValue()).floatValue();
		}
		else if (ci.getId().equals("TimeStart")) {
			fc.timeRange[0] = ((Double) ci.getNewValue()).longValue();
		}
		else if (ci.getId().equals("TimeStop")) {
			fc.timeRange[1] = ((Double) ci.getNewValue()).longValue();
		}
		else if (ci.getId().equals("FilterConstraints")) {
			handleNewFilterConstraints((FilterConstraints) ci.getNewValue());
		}
	}
   // ---------------------------------------------------------------------
   //
   // Check for any changeable info relating to filter variables
   //
   public void handleNewFilterConstraints(FilterConstraints ifc) {
     lastfc = fc;
     fc = new FilterConstraints(ifc.latRange,
				ifc.lonRange,
				ifc.depthRange,
				ifc.timeRange,
				ifc.standardVariables,
				ifc.additionalFields);
     notifyFilterChange(false);

   }

   //--------------------------------------------------------------------------
   // 
   public void setParent(Object parentObject) {
      this.parentObject = parentObject;
   }

   //-----------------------------------------------------------------------
   //
   public Object getAncestor() {
      if (parentObject != null) {
         return ((Lineage)parentObject).getAncestor();
      }
      return this;
   }

   //-----------------------------------------------------------------------
   //
  /* public String toString() {
      StringBuffer str = new StringBuffer();
      str.append("Current Filter Constraints: \n");
      str.append(fc.toString());
      str.append("Last Filter Constraints: \n");
      str.append(lastfc.toString());
      str.append("Default Filter Constraints: \n");
      str.append(deffc.toString());
      return str.toString();
   }*/

}

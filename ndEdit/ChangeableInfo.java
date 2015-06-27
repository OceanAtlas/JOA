/*
 * $Id: ChangeableInfo.java,v 1.2 2005/02/15 18:31:08 oz Exp $
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

// If you need to add a new "id" (changeable), be nice and add the string
// to the list of names below.
//
 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 */
public class ChangeableInfo {

	public String[] names = {"LatitudeStart",
			"LatitudeStop",
			"LongitudeStart",
			"LongitudeStop",
			"DepthStart",
			"DepthStop",
			"TimeStart",
			"TimeStop",
			"Parameter",
			"Season",
			"Cruise",
			"HandPicked",
			"Zoom",
			"FilterConstraints",
			"SemaphoreFilterHappened",
			"SimpleFilterHappened"};


	Object oldValue;
	Object newValue;
	String id;
	boolean undoable;  	// whether this operation should be back/forward sensitive


  //---------------------------------------------
  //
  //
	public ChangeableInfo(String id, Object oldValue, Object newValue, boolean undoable) {
		this.id = id;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.undoable = undoable;
	}
  
	public ChangeableInfo() {
	}
	
	public String getId() {
		return id;
	}
	
	public Object getOldValue() {
		return oldValue;
	}
	
	public Object getNewValue() {
		return newValue;
	}
	public ChangeableInfo getClone() {
		return new ChangeableInfo(this.id, this.oldValue, this.newValue, this.undoable);
	}
	
	public String toString() {
		return (id + "\n     oldValue: " + oldValue 
			       + "\n     newValue: " + newValue 
				   + "\n     undoable: " + undoable);
	}
}



  /*
 * $Id: FilterChangeSpec.java,v 1.1 2005/03/23 23:52:21 oz Exp $
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
//
// Contains the set of filter constraints that's currently active.
//
 /**
 *
 *
 * @author  oz 
 */
public class FilterChangeSpec {
  private String id;
  private Object  oldValue;
  private Object  newValue;
  boolean undoable;

  // ---------------------------------------------------------------------
  //
  public FilterChangeSpec(String type,
			Object oVal,
			Object nVal,
			boolean undo) {
     this.id = type;
     this.oldValue = oVal;
     this.newValue = nVal;
     this.undoable = undo;
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
	
	public FilterChangeSpec getClone() {
		return new FilterChangeSpec(this.id, this.oldValue, this.newValue, this.undoable);
	}
	
	public String toString() {
		return (id + "\n     oldValue: " + oldValue 
			       + "\n     newValue: " + newValue 
				   + "\n     undoable: " + undoable);
	}
}

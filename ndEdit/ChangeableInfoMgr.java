/*
 * $Id: ChangeableInfoMgr.java,v 1.3 2005/02/15 18:31:08 oz Exp $
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

import javax.swing.event.*;

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 *
  * @note Changeable events are events of a coarser granularity.  
  * 
  * The view-controllers, the BackForward Manager, and the Filters 
  * are interested in these events. 
  * 
  * They are of a granularity large enough that they can be 
  * collected by the BackForward Manager to keep a back/forward 
  * record.  In practice, each individual view-controller will 
  * handle its own 
  * fine-grain events and coagulate those fine-grain events into a 
  * coarser event for consumption by the other views, the back-
  * forward manager, and 
  * the filters.
  * 
  * Changeable Events include:
  * - newFilteredData_Event
  * - latitutdeChange
  * - longitudeChange
  * - depthChange
  * - timeChange
  * - cruiseChange
  * - seasonChange
  * - parameterChange
  * - handPickedPointsChange
  * @stereotype Command
  */
public class ChangeableInfoMgr {
    /**@shapeType AggregationLink*/


  protected EventListenerList listenerList = new EventListenerList();


    public ChangeableInfoMgr() {
    }

    //
    //  Handle the event listener bookkeeping
    //
    public void addChangeableInfoListener(ChangeableInfoListener l) {
		listenerList.add(ChangeableInfoListener.class, l);
    }

    public void removeChangeableInfoListener(ChangeableInfoListener l) {
		listenerList.remove(ChangeableInfoListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    protected ChangeEvent changeEvent = null;
	protected void firePopChangeableInfo(ChangeableInfo changeableInfo) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ChangeableInfoListener.class) {
				((ChangeableInfoListener)listeners[i+1]).popChangeableInfo(changeableInfo);
			}	       
		}
	}

	public void pushChangeableInfo(ChangeableInfo ci) {
		ChangeableInfo ci1 = ci.getClone();
		firePopChangeableInfo(ci1);
	}

}

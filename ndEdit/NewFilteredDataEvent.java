/*
 * $Id: NewFilteredDataEvent.java,v 1.2 2005/02/15 18:31:10 oz Exp $
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

 /**
 *
 *
 * @author  Chris Windsor 
 * @version 1.0 01/13/00
 *
 * @note NewFilteredDataEvent differs from a ChangeableEvent in that a 
  * ChangeableEvent causes a NewFilteredDataEvent.  
  * NewFilteredDataEvents don't need to be kept by the 
  * BackForwardManager.
  * 
  * The view part of the view-controller is interested in 
  * NewFilteredDataEvents because it needs to refresh the data 
  * displayed in the view.
  * @stereotype Command
  */
public class NewFilteredDataEvent {
    /**@shapeType AggregationLink*/
    private NewFilteredData_Info lnkUnnamed;
  /**@shapeType DependencyLink*/
}



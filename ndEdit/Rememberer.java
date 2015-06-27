/*
 * $Id: Rememberer.java,v 1.2 2005/02/15 18:31:10 oz Exp $
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
 * @note The Rememberer will remember salient information about the 
  * state of the application on exiting, and it will serialize this 
  * information.   This state information can be used on subsequent 
  * startups if user prefers.
  * 
  * Information to be remembered includes:
  *   - last pointer file opened 
  * @stereotype Singleton
  */
public class Rememberer {
	public int rememberLastPointerFileOpened(){
		return 0;
	}
}

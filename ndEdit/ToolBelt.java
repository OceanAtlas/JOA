/*
 * $Id: ToolBelt.java,v 1.2 2005/02/15 18:31:11 oz Exp $
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
 * @note Each ViewController has a ToolBelt it uses to define what kinds 
  * of tools it likes and to handle the attachment and unattachment 
  * of tools.
  * It also understands how to mediate the resources that each tool 
  * may need.  For example, the ClickZoomer and the ClickGetter 
  * both need mousePress/Release.  So the activation of one implies 
  * the de-activation of the other.
  * Choices are 
  * LIKE_ZOOMERS
  * LIKE_GETTERS 
  * LIKE_CONTOURS
  * DONT_LIKE_ZOOMERS
  * DONT_LIKE_GETTERS
  * DONT_LIKE_CONTOURS
  */
public class ToolBelt {
  public final static int LIKE_ZOOMERS = 1;
  public final static int LIKE_SELECTORS = 2;
  public final static int LIKE_CONTOURS = 3;
  public final static int DONT_LIKE_ZOOMERS = 4;
  public final static int DONT_LIKE_SELECTORS = 5;
  public final static int DONT_LIKE_CONTOURS = 6;
}

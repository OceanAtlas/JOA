/*
 * $Id: UserSettingsManager.java,v 1.6 2005/03/23 23:52:22 oz Exp $
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

  import java.awt.*;
  import java.util.*;
  import java.beans.PropertyChangeSupport;
  import java.beans.PropertyChangeListener;
  import java.beans.PropertyChangeEvent;

 /**
 *
 *
 * @author  Chris Windsor and oz 
 * @version 1.0 01/13/00
 */

/**
* The User Settings manager manages the relationship between the User Settings and
*  all the objects that are interested in user settings.
*  The User Settings manager has a private member object (userSettings) that 
*  keeps the current user settings.  A new set of user settings may be applied 
*  to the application by invoking the "apply" method, passing in the new user
*  settings as a parameter.  Note that the new user settings are _copied_ to the
*  private member of this class.  Likewise, the getUserSettings method passes out a 
*  _copy_ of the userSettings.  (The point being, no other object ever has a 
*  reference to the userSettings instance contained in this class, thus minimizing 
*  bad side-effect behaviour).

*  With regard to the whole arena of NdEdit object construction, note that 
*  while many objects need to know about user settings, this manager is not 
*  consulted by construction-stage NdEdit objects; rather, objects construct 
*  themselves using pre-set hardcoded settings, then after all objects are 
*  constructed, user settings are broadcast to the objects post-construction 
*  using Property Changes via the publishAll method.
*
*
*/

  /**
  * 
  */
public class UserSettingsManager implements java.io.Serializable {
  private Object 	parentObject;
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);


  /**
  * 
  */
  public UserSettingsManager() {
     this.parentObject = parentObject;
  }

  /**
  * 
  */
  public UserSettingsManager(Object parentObject) {
     this.parentObject = parentObject;
  }

  /**
  * 
  */
  public void apply() {
     publishAll();
  }

  public void publishAll() {
     //System.out.println(" Publishing all property changes");
     pcs.firePropertyChange("StartBatch", null, null);
     pcs.firePropertyChange("CutPanelSize", new Dimension(0,0), Constants.USERSETTINGS.getCutPanelSize());
     pcs.firePropertyChange("CutPanelMinSize", new Integer(-1), new Integer(Constants.USERSETTINGS.getCutPanelMinSize()));
     pcs.firePropertyChange("CutPanelMaxSize", new Integer(-1), new Integer(Constants.USERSETTINGS.getCutPanelMaxSize()));
     pcs.firePropertyChange("GeoDisplayFormat", new Integer(-1), new Integer(Constants.USERSETTINGS.getGeoDisplayFormat()));
     pcs.firePropertyChange("TimeDisplayFormat", new Integer(-1), new Integer(Constants.USERSETTINGS.getTimeDisplayFormat()));
     pcs.firePropertyChange("VisibleViews", new int[]{0}, Constants.USERSETTINGS.getVisibleViewIds());
     int dpa = Constants.USERSETTINGS.isDisplayAxes() ? 1 : 0;
     pcs.firePropertyChange("displayPanelAxes", new int[]{0}, new Integer(dpa));
     dpa = Constants.USERSETTINGS.isIndependentHandles() ? 1 : 0;
     pcs.firePropertyChange("independentHandles", new int[]{0}, new Integer(dpa));
     pcs.firePropertyChange("EndBatch", null, null);
  }

  /**
  * 
  */
  public void addDefaultOverlayForView(int viewName, String overlayName){
  }

  /**
  *
  */
  public int getCutPanelMinSize() {
     return Constants.USERSETTINGS.getCutPanelMinSize();
  }

  /**
  * Sets the Cut Panel minimum size of a side, used to regulate stretching,
  * but doesn't fire a property change (see publishCutPanelMinSize).
  *
  * @param minSize smallest allowable size of a side of the cut panel
  */
  public void setCutPanelMinSize(int minSize) {
     Constants.USERSETTINGS.setCutPanelMinSize(minSize);
  }

  /**
  * Sets the Cut Panel minimum size of a side, used to regulate stretching;
  * plus fires a property change to immediately affect the change.
  *
  * @param minSize smallest allowable size of a side of the cut panel
  */

  public void publishCutPanelMinSize(int cutPanelMinSize){
    pcs.firePropertyChange("CutPanelMinSize",
      			 new Integer(-1), 
			 new Integer(cutPanelMinSize));
    Constants.USERSETTINGS.setCutPanelMinSize(cutPanelMinSize);
  }


  /**
  *
  */
  public int getCutPanelMaxSize() {
     return Constants.USERSETTINGS.getCutPanelMaxSize();
  }

  /**
  * Sets the Cut Panel maximum size of a side, used to regulate stretching,
  * but doesn't fire a property change (see publishCutPanelMaxSize).
  *
  * @param maxSize maximum allowable size of a side of the cut panel
  */
  public void setCutPanelMaxSize(int maxSize) {
     Constants.USERSETTINGS.setCutPanelMaxSize(maxSize);
  }

  /**
  * Sets the Cut Panel maximum size of a side, used to regulate stretching;
  * plus fires a property change to immediately affect the change.
  *
  * @param maxSize largest allowable size of a side of the cut panel
  */

  public void publishCutPanelMaxSize(int cutPanelMaxSize){
    pcs.firePropertyChange("CutPanelMaxSize",
      			 new Integer(-1), 
			 new Integer(cutPanelMaxSize));
    Constants.USERSETTINGS.setCutPanelMaxSize(cutPanelMaxSize);
  }

  /**
  * 
  */
  public void setDefaultContouringIntervalForOverlay(String overlayName, double contouringInterval){
  }

  /**
  * 
  */

  public Dimension getCutPanelSize() {
    return Constants.USERSETTINGS.getCutPanelSize();
  }

  /**
  * Sets the cut panel size in the UserSettings object, but doesn't fire a 
  * property change (see publishCutPanelSize).
  *
  * @param cutPanelSize
  */

  public void setCutPanelSize(Dimension cutPanelSize){
    Constants.USERSETTINGS.setCutPanelSize(cutPanelSize);
  }

  /**
  * Sets the cut panel size, plus fires a property change to immediately affect 
  * the change.
  *
  * @param cutPanelSize 
  */

  public void publishCutPanelSize(Dimension cutPanelSize){
    pcs.firePropertyChange("CutPanelSize",
      			 Constants.USERSETTINGS.getCutPanelSize(), cutPanelSize);
    Constants.USERSETTINGS.setCutPanelSize(cutPanelSize);
  }





  /**
  * 
  */
  public int[] getVisibleViewIds(){
    return Constants.USERSETTINGS.getVisibleViewIds();
  }


  /**@tgSet*/
  /**
  * 
  */
  public void setVisibleViewIds(int[] viewEnums){
    Constants.USERSETTINGS.setVisibleViewIds(viewEnums);
  }




  /**@tgGet*/
  /**
  * 
  */
  public double getLonReference(){
    return Constants.USERSETTINGS.getLonReference();
  }


  /**@tgSet*/
  // 
  // LonReference: either 0 or -180       
  //
  /**
  * 
  */
  public void setLonReference(double lonReference){
    Constants.USERSETTINGS.setLonReference(lonReference);
  }




  /**@tgGet*/
  /**
  * 
  */
  public int getGeoDisplayFormat(){
    return Constants.USERSETTINGS.getGeoDisplayFormat();
  }


  /**@tgSet*/
  //
  // GeoDisplayFormat: 0 = deg min; 1 = decimal deg
  //
  /**
  * 
  */
  public void setGeoDisplayFormat(int geoDisplayFormat){
    Constants.USERSETTINGS.setGeoDisplayFormat(geoDisplayFormat);
  }

  //
  /**
  * 
  */
  public void publishGeoDisplayFormat(int geoDisplayFormat){
    pcs.firePropertyChange("GeoDisplayFormat",
      			new Integer(Constants.USERSETTINGS.getGeoDisplayFormat()), 
			new Integer(geoDisplayFormat));
    Constants.USERSETTINGS.setGeoDisplayFormat(geoDisplayFormat);
  }




  /**@tgGet*/
  /**
  * 
  */
  public int getUseCenterWidthOrMinMax(){
    return Constants.USERSETTINGS.getUseCenterWidthOrMinMax();
  }


  /**@tgSet*/
  //
  // HandlebarParadigm: CENTER_WIDTH or MIN_MAX
  //  0 = top and bottom handlebars move dependently
  //  1 = top and bottom handlebars move independently
  //
  /**
  * 
  */
  public void setUseCenterWidthOrMinMax(int useCenterWidthOrMinMax){
    Constants.USERSETTINGS.setUseCenterWidthOrMinMax(useCenterWidthOrMinMax);
  }




  /**@tgGet*/
  /**
  * 
  */
  public int getTimeDisplayFormat(){
    return Constants.USERSETTINGS.getTimeDisplayFormat();
  }

  /**@tgSet*/
  //
  // TimeDisplayFormat: if 0 = YYYY-MM-DD; if 1 = YYYY-NNN
  //
  /**
  * 
  */
  public void setTimeDisplayFormat(int timeDisplayFormat){
    Constants.USERSETTINGS.setTimeDisplayFormat(timeDisplayFormat);
  }

  //
  /**
  * 
  */
  public void publishTimeDisplayFormat(int timeDisplayFormat){
    pcs.firePropertyChange("TimeDisplayFormat", new Integer(Constants.USERSETTINGS.getTimeDisplayFormat()), new Integer(timeDisplayFormat));
    Constants.USERSETTINGS.setTimeDisplayFormat(timeDisplayFormat);
  }




  /**@tgGet*/
  /**
  * 
  */
  public int getTimeAxisMode(){
    return Constants.USERSETTINGS.getTimeAxisMode();
  }


  //
  // TimeAxisMode: if 0 = since reference data; if 1 = date
  //
  /**
  * 
  */
  public void setTimeAxisMode(int timeAxisMode){
    Constants.USERSETTINGS.setTimeAxisMode(timeAxisMode);
  }




  /**
  * 
  */
  public double getTimeAxisReference(){
    return Constants.USERSETTINGS.getTimeAxisReference();
  }


  //
  // TimeAxisReference: ?
  //
  /**
  * 
  */
  public void setTimeAxisReference(double timeAxisReference){
    Constants.USERSETTINGS.setTimeAxisReference(timeAxisReference);
  }




  /**@tgGet*/
  /**
  * 
  */
  public String getTimeSinceUnits(){
    return Constants.USERSETTINGS.getTimeSinceUnits();
  }
  /**
  * 
  */
  public Object getAncestor() {
     if (parentObject != null) {
	return ((Lineage)parentObject).getAncestor();
     }
     return this;
  }


  /**@tgSet*/
  //
  // TimeSinceUnits: Years
  //
  /**
  * 
  */
  public void setTimeSinceUnits(String timeSinceUnits){
    Constants.USERSETTINGS.setTimeSinceUnits(timeSinceUnits);
  }

  /**
  * 
  */
  public String toString() {

    return Constants.USERSETTINGS.toString();
  }

   //--------------------------------------------------------------------------
   // 
   /**
   * 
   */
   public void addPropertyChangeListener(PropertyChangeListener l) {
      if (pcs == null) {
         //super.addPropertyChangeListener(l);
      }
      else {
         pcs.addPropertyChangeListener(l);
      }
   }

   //--------------------------------------------------------------------------
   // 
   /**
   * 
   */
   public void removePropertyChangeListener(PropertyChangeListener l) {
      pcs.removePropertyChangeListener(l);
   }
}

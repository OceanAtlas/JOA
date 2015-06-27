/*
 * $Id: DataModel.java,v 1.7 2004/03/23 23:34:00 dwd Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.sgt.beans;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import gov.noaa.pmel.sgt.dm.SGTData;
import gov.noaa.pmel.sgt.Attribute;

/**
 * A model that supplies the relationship between <code>SGTData</code> objects,
 * its <code>Attribute</code> and the
 * <code>Panel</code> and <code>DataGroup</code> in which it is displayed and the
 * <code>Legend</code>.
 * <p> Some classes have been omitted for display purposes.
 * <P ALIGN="CENTER"><IMG SRC="images/DataModelSimple.png" ALIGN="BOTTOM" BORDER="0">
 *
 * @author Donald Denbo
 * @version $Revision: 1.7 $, $Date: 2004/03/23 23:34:00 $
 * @since 3.0
 * @stereotype bean
 **/
public class DataModel {
  private PropertyChangeSupport support_ = new PropertyChangeSupport(this);
  private List dataList_;

  /**
   * @label page
   */
  private Page page;

  /** @link aggregation
   * @supplierCardinality 1..*
   * @label dataList*/
  /*#DataHolder lnkDataHolder;*/

  transient private boolean batch = false;
  transient private boolean modified = false;
  transient private List events = new Vector();

  /**
   * Default constructor.
   */
  public DataModel() {
    dataList_ = new Vector();
  }

  /**
   * Add data to the <code>DataModel</code>.  Throws a "addData" property change.
   * @param data SGTData
   * @param attr Attribute for data
   * @param pHolder PanelHolder
   * @param dataGroup DataGroup
   * @param legend Legend
   * @return reference to DataHolder object created
   */
  public DataHolder addData(SGTData data, Attribute attr,
                      PanelHolder pHolder, DataGroup dataGroup,
                      Legend legend) {
    DataHolder dh = new DataHolder(this, data, attr, pHolder,
                                   dataGroup, legend);
    dataList_.add(dh);
    firePropertyChange("addData", null, dh);
    return dh;
  }

  /**
   * Remove DataHolder object from list.  Throws a "removeData" property change.
   * @param dh DataHolder
   * @return true if successful
   * @since 3.1
   */
  public boolean removeData(DataHolder dh) {
    boolean result = dataList_.remove(dh);
    firePropertyChange("removeData", null, dh);
    return result;
  }

  /**
   * Remove all DataHolder objects from the DataModel.
   * Throws a "removeData" property change.
   * @since 3.1
   */
  public void removeAllData() {
    List saved = new Vector(dataList_);
    dataList_.clear();
    firePropertyChange("removeAllData", null, saved);
  }

  /**
   * Get <code>Iterator</code> of the <code>DataHolder</code> objects.
   * @return Iteractor for DataHolder objects
   */
  public Iterator dataIterator() {
    return dataList_.iterator();
  }

  /**
   * Add property change listener.
   * @param l property change listener
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    support_.addPropertyChangeListener(l);
  }

  /**
   * Listen for specific property change.
   * @param name property name
   * @param l property change listner
   */
  public void addPropertyChangeListener(String name, PropertyChangeListener l) {
    support_.addPropertyChangeListener(name, l);
  }

  /**
   * Remove property change listener.
   * @param l property change listener
   */
  public void removePropertyChangeListener(PropertyChangeListener l) {
    support_.removePropertyChangeListener(l);
  }

  /**
   * Remove specific property change listener
   * @param name property name
   * @param l property change listener
   */
  public void removePropertyChangeListener(String name, PropertyChangeListener l) {
    support_.removePropertyChangeListener(name, l);
  }

  /**
   * Set <code>Page</code>.
   * @param page Page
   */
  public void setPage(Page page) {
    this.page = page;
  }

  /**
   * Get Page.
   * @return Page
   */
  public Page getPage() {
    return page;
  }

  private void firePropertyChange(String name, Object oldValue, Object newValue) {
    if(batch) {
      modified = true;
      events.add(new PropertyChangeEvent(this, name, oldValue, newValue));
    } else {
      support_.firePropertyChange(name, oldValue, newValue);
    }
  }

  /**
   * Is batching turned on?
   * @return true, if batch is on
   * @since 3.1
   */
  public boolean isBatch() {
    return batch;
  }
  /**
   * Set batching for changes to <code>DataModel</code>. Fires property events
   * when <code>DataModel</code> has been changed and batch is set to false.
   * Fires the events in the order they were originally created.
   * @param batch batch value
   * @since 3.1
   */
  public void setBatch(boolean batch) {
    this.batch = batch;
    if(!this.batch && modified) {
      modified = false;
      for(int i=0; i < events.size(); i++) {
        support_.firePropertyChange((PropertyChangeEvent)events.get(i));
      }
      events.clear();
    }
  }
}

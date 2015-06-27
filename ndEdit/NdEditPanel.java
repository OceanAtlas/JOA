/*
 * $Id: NdEditPanel.java,v 1.8 2004/12/02 00:00:09 dwd Exp $
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import java.awt.*;
import javax.swing.*;

import gov.noaa.pmel.nc2.station.GeoDomain;
import gov.noaa.pmel.nc2.station.StationCollection;
import ndEdit.Constants;
import ndEdit.NdEdit;
import ndEdit.PointerCollectionGroup;
import ndEdit.TuplePointerCollection;
import ndEdit.UserSettings;

public class NdEditPanel extends JComponent implements PropertyChangeListener {
  public NdEditPanel() {
    super();
  }

  private Container parent_ = null;
  private StationCollection collection_ = null;
  private List domainList_ = null;
  private NdEdit ndEdit_ = null;
  private int stationCount_ = 0;
    //private int cpHeight_ = 322;
    //  private int cpWidth_ = 250;
  private int cpHeight_ = 350;
  private int cpWidth_ = 290;

//  private PropertyChangeSupport support_ = new PropertyChangeSupport(this);

  BorderLayout borderLayout1 = new BorderLayout();
  JPanel panel = new JPanel();
  JPanel messagePanel = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel message_ = new JLabel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JLabel countLabel_ = new JLabel();

  public NdEditPanel(Container parentFrame) {
    super();
    parent_ = parentFrame;
    ndEdit_ = new NdEdit(parent_, true, false);
    try {
      jbInit();
      initializeNdEdit();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public void setCollection(StationCollection collection, List domainList) throws Exception {
    collection_ = collection;
    domainList_ = domainList;
    setStationList(domainList_);
    JMenuBar jmb = ((JRootPane)parent_).getJMenuBar();
    JMenu jm = jmb.getMenu(0);
    if(jm.getText().equals("File")) {
      jmb.remove(jm);
      jmb.invalidate();
      validate();
    }
  }

  void jbInit() throws Exception {
    setLayout(borderLayout1);
    setBorder(null);
    setMinimumSize(new Dimension(10, 17));
    setPreferredSize(new Dimension(10, 17));
    setLayout(borderLayout1);
    panel.setLayout(gridBagLayout1);
    messagePanel.setLayout(gridBagLayout2);
    message_.setMinimumSize(new Dimension(0, 15));
    message_.setPreferredSize(new Dimension(0, 15));
    message_.setRequestFocusEnabled(true);
    message_.setText("");
    countLabel_.setMinimumSize(new Dimension(15, 15));
    countLabel_.setToolTipText("");
    countLabel_.setText("");
    add(ndEdit_, BorderLayout.CENTER);
    this.add(panel,  BorderLayout.SOUTH);
    panel.add(messagePanel,   new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    messagePanel.add(message_,      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 15, 5, 15), 0, 0));
    messagePanel.add(countLabel_,      new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 15, 5, 15), 0, 0));
  }

  void initializeNdEdit() {
    /** @todo fix support to be customizable */
    Constants.SUPPORT_DIR = null;
        //"/Users/dwd/JProjects/StationDataset/NdEdit_Support";

    //  int frmheight = cpHeight_ + 189; // for 1 panel vertical
    int frmheight = cpHeight_ + 140; // for 1 panel vertical
    int frmwidth = 3*cpWidth_ + 10;  // for 3 panels horizontal

    this.setPreferredSize(new Dimension(frmwidth, frmheight));

    int[] views = {
        Constants.LAT_LON,
        Constants.LAT_TIME,
        Constants.LAT_DEPTH};

    UserSettings uset = new UserSettings();
    uset.setCutPanelSize(new Dimension(cpWidth_, cpHeight_));
    uset.setCutPanelMaxSize(700);
    uset.setCutPanelMinSize(200);
    uset.setDisplayAxes(true);
    uset.setTimeAxisMode(0);
    uset.setLonReference(0.0f);
    uset.setTimeDisplayFormat(0);
    uset.setUseCenterWidthOrMinMax(0);
    uset.setVisibleViewIds(views);
    ndEdit_.setUserSettings(uset);
    ndEdit_.setDataTitle("Individual Station Selection");
  }

  private void setStationList(List domainList) {
    if(domainList ==  null) return;
    stationCount_ = domainList.size();
    if(stationCount_ <= 0) return;
    countLabel_.setText(stationCount_ + " stations found.");

    // clean up existing pointer collections
    PointerCollectionGroup pc = ndEdit_.getPointerCollection();
    for(int i=0; i < pc.size(); i++) {
      ((TuplePointerCollection)pc.get(i)).removePropertyChangeListener(this);
    }
    pc.clear();

    GeoDomain gd = (GeoDomain)domainList.get(0);
    TuplePointerCollection myPC = null;

    if(gd.hasMinimumZ() && gd.hasMaximumZ()) {
      myPC = buildPCProfile(domainList);
    } else if(gd.hasMinimumT() && gd.hasMinimumT()) {
      myPC = buildPCTimeSeries(domainList);
    } else {
      myPC = buildPC(domainList);
    }
    myPC.addPropertyChangeListener(this);

    ndEdit_.setPointerCollection(myPC, true);
  }

/*  private boolean finalizeNdEdit() {
    stationRefs_ = null;
    PointerCollectionGroup pc = ndEdit_.getPointerCollection();
    TuplePointerCollection fpc = ( (TuplePointerCollection) pc.get(0)).
        getSelectedPointerCollection();

    stationRefs_ = fpc.getReferences();
    if (stationRefs_ == null) {
      JOptionPane.showMessageDialog(this,
                                    "No selected or filtered stations.",
                                    "NdEdit Warning",
                                    JOptionPane.WARNING_MESSAGE);
      return false;
    }
    int selCount = stationRefs_.length;
    if (selCount == stationCount_) {
      String message = "All stations have been selected. (" +
          stationCount_ + " stations)\nContinue?";
      int result = JOptionPane.showConfirmDialog(this,
                                                 message,
                                                 "NdEdit Warning",
                                                 JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.NO_OPTION) {
        stationRefs_ = null;
        return false;
      }
    }
    fireSelectionEvent(collection_.getStationIterator(stationRefs_, ""));
    return true;
  } */

/*  boolean hasSelectionListener() {
    return selectionListeners_.size() > 0;
  }

  public void addSelectionListener(SelectionListener sl) {
    selectionListeners_.add(sl);
  }

  public void removeSelectionListener(SelectionListener sl) {
    selectionListeners_.remove(sl);
  } */

/*  void fireSelectionEvent(Iterator datasetIterator) {
    SelectionEvent se = new SelectionEvent(this, datasetIterator);
    Iterator iter = selectionListeners_.iterator();
    while(iter.hasNext()) {
      ((SelectionListener)iter.next()).selectionPerformed(se);
    }
  } */

/*  public void windowClosing(WindowEvent e) {
    if(finalizeNdEdit())
      setVisible(false);
    else
      setVisible(true);
  } */

/*  public void windowActivated(WindowEvent e) {
    ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
    NdEditActionList allActions = ndEdit_.getActions();
    NdEditAction myAction = allActions.getAction(b.getString("kResetZoom"));
    myAction.doAction();
    // remove File menu
    JMenuBar jmb = ((JRootPane)parent_).getJMenuBar();
    JMenu jm = jmb.getMenu(0);
    if(jm.getText().equals("File")) {
      jmb.remove(jm);
      jmb.invalidate();
      validate();
    }
  } */

  /**
  * build a PointerCollection from arbitrary itemList
  * @param itemList List
  * @return PointerCollection
  */
 private TuplePointerCollection buildPC(List itemList) {
   GeoDomain staDomain = collection_.getDomain();
   double northRef = staDomain.getNorthLat();
   double southRef = staDomain.getSouthLat();
   double westRef = staDomain.getWestLon();
   double eastRef = staDomain.getEastLon();
   double minZRef = staDomain.getMinimumZ();
   double maxZRef = staDomain.getMaximumZ();
   long minTRef = staDomain.getMinimumT();
   long maxTRef = staDomain.getMaximumT();

   Iterator itemIter = itemList.iterator();
   GeoDomain st = (GeoDomain)itemList.get(0);
   double[] latArr1 = new double[itemList.size()];
   double[] latArr2 = null;
   if(st.hasLatRange()) latArr2 = new double[itemList.size()];
   double[] lonArr1 = new double[itemList.size()];
   double[] lonArr2 = null;
   if(st.hasLonRange()) lonArr2 = new double[itemList.size()];
   double[] depthArr1 = new double[itemList.size()];
   double[] depthArr2 = null;
   if(st.hasZRange()) depthArr2 = new double[itemList.size()];
   double[] timeArr1 = new double[itemList.size()];
   double[] timeArr2 = null;
   if(st.hasTRange()) timeArr2 = new double[itemList.size()];
   GeoDomain[] gdArr = new GeoDomain[itemList.size()];

   int index = 0;
   while (itemIter.hasNext()) {
     GeoDomain gd = (GeoDomain) itemIter.next();
     if(gd.hasSouthLat()) {
       latArr1[index] = (float)gd.getSouthLat();
     } else {
       latArr1[index] = (float)southRef;
     }
     if(latArr2 != null) {
       if (gd.hasNorthLat()) {
         latArr2[index] = (float)gd.getNorthLat();
       } else {
         latArr2[index] = (float)northRef;
       }
     }
     if(gd.hasWestLon()) {
       lonArr1[index] = (float)gd.getWestLon();
     } else {
       lonArr1[index] = (float)westRef;
     }
     if(lonArr2 != null) {
       if (gd.hasEastLon()) {
         lonArr2[index] = (float)gd.getEastLon();
       } else {
         lonArr2[index] = (float)eastRef;
       }
     }
     if(gd.hasMinimumT()) {
       timeArr1[index] = (float)gd.getMinimumT();
     } else {
       timeArr1[index] = (float)minTRef;
     }
     if(timeArr2 != null) {
       if (gd.hasMaximumT()) {
         timeArr2[index] = (float)gd.getMaximumT();
       } else {
         timeArr2[index] = (float)maxTRef;
       }
     }
     if(gd.hasMinimumZ()) {
       depthArr1[index] = (float)gd.getMinimumZ();
     } else {
       depthArr1[index] = (float)minZRef;
     }
     if(depthArr2 != null) {
       if (gd.hasMaximumZ()) {
         depthArr2[index] = (float)gd.getMaximumZ();
       } else {
         depthArr2[index] = (float)maxZRef;
       }
     }
     gdArr[index] = gd;
     index++;
   }
   return new TuplePointerCollection(latArr1, latArr2,
                                     lonArr1, lonArr2,
                                     depthArr1, depthArr2,
                                     timeArr1, timeArr2,
                                     gdArr);
 }

 /**
  * build PointerCollection from profile data
  * @param itemList List
  * @return PointerCollection
  */
 private TuplePointerCollection buildPCProfile(List itemList) {
   GeoDomain staDomain = collection_.getConstrainedDomain();
   if(staDomain == null) staDomain = collection_.getDomain();

   double minZRef = staDomain.getMinimumZ();
   double maxZRef = staDomain.getMaximumZ();

   Iterator itemIter = itemList.iterator();
   double[] latArr1 = new double[itemList.size()];
   double[] lonArr1 = new double[itemList.size()];
   double[] depthArr1 = new double[itemList.size()];
   double[] depthArr2 = new double[itemList.size()];
   double[] timeArr1 = new double[itemList.size()];
   GeoDomain[] gdArr = new GeoDomain[itemList.size()];

   int index = 0;
   while (itemIter.hasNext()) {
     GeoDomain gd = (GeoDomain) itemIter.next();
     latArr1[index] = (float)gd.getSouthLat();
     lonArr1[index] = (float)gd.getWestLon();
     timeArr1[index] = (float)gd.getMinimumT();

     depthArr1[index] = (float)gd.getMinimumZ();
     depthArr2[index] = (float)gd.getMaximumZ();

     gdArr[index] = gd;
     index++;
   }
   return new TuplePointerCollection(latArr1, null,
                                     lonArr1, null,
                                     depthArr1, depthArr2,
                                     timeArr1, null,
                                     gdArr);
 }

 /**
  * build pointerCollection from time-series
  * @param itemList List
  * @return PointerCollection
  */
 private TuplePointerCollection buildPCTimeSeries(List itemList) {
   GeoDomain staDomain = collection_.getConstrainedDomain();
   if(staDomain == null) staDomain = collection_.getDomain();

   double minTRef = staDomain.getMinimumT();
   double maxTRef = staDomain.getMaximumT();

   Iterator itemIter = itemList.iterator();
   double[] latArr1 = new double[itemList.size()];
   double[] lonArr1 = new double[itemList.size()];
   double[] depthArr1 = new double[itemList.size()];
   double[] timeArr1 = new double[itemList.size()];
   double[] timeArr2 = new double[itemList.size()];
   GeoDomain[] gdArr = new GeoDomain[itemList.size()];

   int index = 0;
   while (itemIter.hasNext()) {
     GeoDomain gd = (GeoDomain) itemIter.next();
     latArr1[index] = (float)gd.getSouthLat();
     lonArr1[index] = (float)gd.getWestLon();
     timeArr1[index] = (float)minTRef;
     timeArr2[index] = (float)maxTRef;

     depthArr1[index] = (float)gd.getMinimumZ();

     gdArr[index] = gd;
     index++;
   }
   return new TuplePointerCollection(latArr1, null,
                                     lonArr1, null,
                                     depthArr1, null,
                                     timeArr1, timeArr2,
                                     gdArr);
 }

 public PointerCollectionGroup getPointerCollection() {
   return ndEdit_.getPointerCollection();
 }

 public TuplePointerCollection getSelectedPointerCollection() {
   return ((TuplePointerCollection)getPointerCollection().get(0)).getSelectedPointerCollection();
 }

 public GeoDomain[] getReferences() {
   Object[] refs = getSelectedPointerCollection().getReferences();
   GeoDomain[] gdArr = new GeoDomain[refs.length];
   for(int i=0; i < refs.length; i++) {
     gdArr[i] = (GeoDomain)refs[i];
   }
   return gdArr;
 }
  /**
   * propertyChange
   *
   * @param propertyChangeEvent PropertyChangeEvent
   */
  public void propertyChange(PropertyChangeEvent event) {
    PointerCollectionGroup pc = ndEdit_.getPointerCollection();
    TuplePointerCollection fpc =
        ((TuplePointerCollection)pc.get(0)).getSelectedPointerCollection();

    if(fpc.isSomethingSelected()) {
//      openButton.setEnabled(true);
      message_.setText(fpc.getSize() + " stations selected");
    } else {
      message_.setText("No stations selected");
//      openButton.setEnabled(false);
    }
//    PropertyChangeEvent pce = new PropertyChangeEvent(event.getSource(),
//        event.getPropertyName(), null, new Integer(fpc.getSize()));
    this.firePropertyChange(event.getPropertyName(),
                            null, new Integer(fpc.getSize()));
//    support_.firePropertyChange(pce);
  }

//  public void addPropertyChangeListener(PropertyChangeListener listen) {
//    support_.addPropertyChangeListener(listen);
//  }

//  public void removePropertyChangeListener(PropertyChangeListener listen) {
//    support_.removePropertyChangeListener(listen);
//  }
}
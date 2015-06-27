/*
 * $Id: JMapDialog.java,v 1.1 2004/02/19 18:15:07 dwd Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit.ncBrowse;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;

import gov.noaa.pmel.swing.JSlider2Date;
import gov.noaa.pmel.swing.JSelectionMap;

import gov.noaa.pmel.util.TimeRange;
import gov.noaa.pmel.util.GeoDate;

public class JMapDialog extends javax.swing.JDialog {
  JSelectionMap map_ = null;
  public static int OK = 1;
  public static int CANCEL = 0;
  private int result_ = CANCEL;

  public JMapDialog(Frame parent) {
    super(parent);
    try {
      jbInit();
//      pack();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    setTitle("Region Selection - Climate Data Portal");
    setResizable(false);
    setModal(true);
    getContentPane().setLayout(new BorderLayout(0,0));
    setSize(610,486);
    setVisible(false);
    buttonPanel.setBorder(etchedBorder1);
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER,15,5));
    dateSlider.setTwoHandles(true);
    dateSlider.setFormat("yyyy-MM-dd");
    dateSlider.setFont(new java.awt.Font("Lucida Grande", 0, 12));
    getContentPane().add(buttonPanel, "South");
    buttonPanel.setBounds(0,447,610,39);
    OKButton.setText("OK");
    buttonPanel.add(OKButton);
    OKButton.setBounds(235,7,51,25);
    cancelButton.setText("Cancel");
    buttonPanel.add(cancelButton);
    cancelButton.setBounds(301,7,73,25);
    //$$ etchedBorder1.move(0,516);
    mainPanel.setLayout(new BorderLayout(0,0));
    getContentPane().add(mainPanel, "Center");
    mainPanel.setBounds(0,0,610,447);
    datePanel.setBorder(etchedBorder1);
    datePanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,2));
    mainPanel.add(datePanel, "South");
    datePanel.setBounds(0,295,610,152);
    datePanel.add(dateSlider);
    dateSlider.setBounds(117,4,375,144);

    map_ = new JSelectionMap();
    map_.setBorder(new CompoundBorder(etchedBorder1, new EmptyBorder(10,0,0,0)));
    mainPanel.add(map_, "Center");

    SymAction lSymAction = new SymAction();
    OKButton.addActionListener(lSymAction);
    cancelButton.addActionListener(lSymAction);
  }

  public JMapDialog() {
    this((Frame)null);
  }

  public JMapDialog(String sTitle) {
    this();
    setTitle(sTitle);
  }

  public void setVisible(boolean b) {
    if (b)
      setLocation(50, 50);
    super.setVisible(b);
  }

  public void initMap() {
    map_.init();
  }

  static public void main(String args[]) {
    (new JMapDialog()).setVisible(true);
  }

  javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
  javax.swing.JButton OKButton = new javax.swing.JButton();
  javax.swing.JButton cancelButton = new javax.swing.JButton();
  javax.swing.border.EtchedBorder etchedBorder1 = new javax.swing.border.EtchedBorder();
  javax.swing.JPanel mainPanel = new javax.swing.JPanel();
  javax.swing.JPanel datePanel = new javax.swing.JPanel();
  gov.noaa.pmel.swing.JSlider2Date dateSlider = new gov.noaa.pmel.swing.JSlider2Date();

  class SymAction implements java.awt.event.ActionListener  {
    public void actionPerformed(java.awt.event.ActionEvent event) {
      Object object = event.getSource();
      if (object == OKButton)
  OKButton_actionPerformed(event);
      else if (object == cancelButton)
  cancelButton_actionPerformed(event);
    }
  }

  void OKButton_actionPerformed(java.awt.event.ActionEvent event) {
    result_ = OK;
    setVisible(false);
  }

  public void setDefaultSelection(double north, double south,
          double west, double east) {
    map_.setDefaultSelection(north, south, west, east);
  }

  public void setDefaultSelection(String north, String south,
          String west, String east) {
    map_.setDefaultSelection(north, south, west, east);
  }

  public void addDataRegion(double north, double south,
          double west, double east) {
    map_.addDataRegion(north, south, west, east);
  }
  public void addDataRegion(String north, String south,
          String west, String east) {
    map_.addDataRegion(north, south, west, east);
  }

  public void setTimeRange(TimeRange range) {
    dateSlider.setRange(range);
    //	    dateSlider.setStartValue(range.start);
    //	    dateSlider.setEndValue(range.end);
  }
  public void setTimeValue(TimeRange range) {
    dateSlider.setStartValue(range.start);
    dateSlider.setEndValue(range.end);
  }

  public GeoDate getStartValue() {
    return dateSlider.getStartValue();
  }

  public GeoDate getEndValue() {
    return dateSlider.getEndValue();
  }

  public double getSouthLatitude() {
    return map_.getMinLat();
  }

  public double getNorthLatitude() {
    return map_.getMaxLat();
  }

  public double getWestLongitude() {
    return map_.getMinLon();
  }

  public double getEastLongitude() {
    return map_.getMaxLon();
  }

  public int openDialog() {
    result_ = CANCEL;
    //setVisible(true);
    show();
    return result_;
  }

  void cancelButton_actionPerformed(java.awt.event.ActionEvent event)	{
    result_ = CANCEL;
    setVisible(false);
  }
}

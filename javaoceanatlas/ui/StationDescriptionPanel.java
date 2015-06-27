/*
 * $Id: StationDescriptionPanel.java,v 1.9 2005/10/18 23:42:19 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import java.io.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class StationDescriptionPanel extends JPanel implements ObsChangedListener, ParameterAddedListener,
    StnFilterChangedListener, PrefsChangedListener, ActionListener {
  protected Station mCurrStn;
  protected Section mCurrSec;
  protected JPanel line1 = null, line2 = null, line3 = null, line4 = null, line5 = null, line6 = null, line7 = null,
      line7a = null, line8 = null;
  protected FileViewer mFileViewer;
  protected JOAJLabel l1 = null, l2 = null, l3 = null, l4 = null, l5 = null, l6 = null, l7 = null, 
  l7a = null, l8 = null;
  protected JPanel mContents = null;
  protected JPanel mStnVals = null;
  protected JPanel mStnValsCont = null;
  private JScrollPane stnValScroller = null;
  protected JPopupMenu mPopupMenu = null;
  private int mMaxParamLen = 4;
  private int mMaxUnitsLen = 4;
  private Vector<ParamValuePanel> mValuePanels = new Vector<ParamValuePanel>();
  private boolean mStnVarAdded = false;
  private int mWidth = 250;
  private ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  private boolean mIncludeStnCalcs = true;
  private boolean mIsLocked = false;

  public StationDescriptionPanel(FileViewer fv) {
    this(fv, true);
  }

  public StationDescriptionPanel(FileViewer fv, boolean incldStnCalcs) { 
    mFileViewer = fv;
    mIncludeStnCalcs = incldStnCalcs;
    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.elementAt(0);
    mCurrSec = (Section)of.mSections.elementAt(0);
    mCurrStn = (Station)mCurrSec.mStations.elementAt(0);
    this.setLayout(new BorderLayout(0, 0));
    mContents = new JPanel();
    mContents.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));

    mStnVals = new JPanel();
    mStnVals.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));

    mStnValsCont = new JPanel();
    mStnValsCont.setLayout(new BorderLayout(0, 0));

    if (mIncludeStnCalcs) {
      mStnValsCont.add("North", new JOAJLabel(b.getString("kStationCalcResults"), JOAConstants.ISMAC));

      // put stnvals in a scroller
      stnValScroller = new JScrollPane(mStnVals);
      stnValScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      stnValScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      stnValScroller.setOpaque(false);
      mStnValsCont.add("Center", stnValScroller);
      stnValScroller.setBorder(BorderFactory.createEtchedBorder());
    }

    setStn(mCurrSec, mCurrStn);
    mFileViewer.addObsChangedListener(this);
    mFileViewer.addStnFilterChangedListener(this);
    this.addMouseListener(new MyMouseHandler());
    mFileViewer.addParameterAddedListener(this);
    if (PowerOceanAtlas.getInstance() != null) {
      PowerOceanAtlas.getInstance().addPrefsChangedListener(this);
    }
  }

  public Dimension getPreferredSize() {
    return new Dimension(mWidth, 170);
  }

  public void removeMyListeners() {
    mFileViewer.removeObsChangedListener(this);
    mFileViewer.removeStnFilterChangedListener(this);
    mFileViewer.removeParameterAddedListener(this);
    PowerOceanAtlas.getInstance().removePrefsChangedListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("opencontextual")) {
      showConfigDialog();
    }
  }

  public class MyMouseHandler extends MouseAdapter {
    public void mouseClicked(MouseEvent me) {
      if (me.getClickCount() == 2) {
        // show configuration dialog
        showConfigDialog();
      }
    }

    public void mouseReleased(MouseEvent me) {
      super.mouseReleased(me);
      if (me.isPopupTrigger()) {
        createPopup(me.getPoint());
      }
    }

    public void mousePressed(MouseEvent me) {
      super.mousePressed(me);
      if (me.isPopupTrigger()) {
        createPopup(me.getPoint());
      }
    }
  }

  public void createPopup(Point point) {
    mPopupMenu = new JPopupMenu();
    JMenuItem openContextualMenu = new JMenuItem(b.getString("kProperties"));
    openContextualMenu.setActionCommand("opencontextual");
    openContextualMenu.addActionListener(this);
    mPopupMenu.add(openContextualMenu);
    mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    mPopupMenu.show(this, point.x, point.y);
  }

  public void showConfigDialog() {
    // show configuration dialog
    ConfigFileProperties cp = new ConfigFileProperties(mFileViewer);
    cp.pack();

    // show dialog at center of screen
    Rectangle dBounds = cp.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    cp.setLocation(x, y);
    cp.setVisible(true);
  }

  public void setStn(Section sec, Station stn) {
    setMaxParamLength(sec);
    setMaxUnitLength(sec);
    line1 = null;
    line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line2 = null;
    line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line3 = null;
    line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line4 = null;
    line4 = new JPanel();
    line4.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line5 = null;
    line5 = new JPanel();
    line5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line6 = null;
    line6 = new JPanel();
    line6.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line7 = null;
    line7 = new JPanel();
    line7.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line7a = null;
    line7a = new JPanel();
    line7a.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    line8 = null;
    line8 = new JPanel();
    line8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    l1 = new JOAJLabel("Section: " + sec.mSectionDescription, JOAConstants.ISMAC);
    if (sec.getExpoCode() != null && sec.getExpoCode().length() > 0) {
    	l1.setText("Section: " + sec.mSectionDescription + " Expocode:" + sec.getExpoCode());
    }
    l2 = new JOAJLabel("", JOAConstants.ISMAC);
    if (stn.mShipCode != null && stn.mShipCode.length() > 0) {
    	l2.setText("Platform: " + stn.mShipCode);
    }
    l3 = new JOAJLabel("Stn: " + stn.mStnNum, JOAConstants.ISMAC);
    l4 = new JOAJLabel("Cast: " + stn.mCastNum +  " "+ JOAFormulas.formatDouble((stn.mCumDist * 1.852), 2, false) + " (km)");
    l5 = new JOAJLabel("Lon: " + JOAFormulas.formatLon(stn.mLon), JOAConstants.ISMAC);
    l6 = new JOAJLabel("Lat: " + JOAFormulas.formatLat(stn.mLat), JOAConstants.ISMAC);
    l7 = new JOAJLabel("Date: " + JOAFormulas.formatDate(stn, true), JOAConstants.ISMAC);
    if (stn.getBottom() != JOAConstants.MISSINGVALUE) {
      l7a = new JOAJLabel("Bottom: " + stn.getBottom(), JOAConstants.ISMAC);
    }
    else {
      l7a = new JOAJLabel("Bottom: ----", JOAConstants.ISMAC);
    }
    String op = stn.getOriginalPath();
    String on = stn.getOriginalName();
    if (on != null && op != null) {
      l8 = new JOAJLabel("", JOAConstants.ISMAC);
      if (!op.endsWith("\u005c\u005c")) {
        l8.setText("Original path: " + op + File.pathSeparator + on); //"\u005c\u005c" + on);
      }
      else {
        l8.setText("Original path: " + op + on);
      }
    }
    else {
      l8 = new JOAJLabel("", JOAConstants.ISMAC);
      l8.setText("");
    }
    
    l1.setFont(new java.awt.Font("serif", 0, 13));
    l2.setFont(new java.awt.Font("serif", 0, 13));
    l3.setFont(new java.awt.Font("serif", 0, 13));
    l4.setFont(new java.awt.Font("serif", 0, 13));
    l5.setFont(new java.awt.Font("serif", 0, 13));
    l6.setFont(new java.awt.Font("serif", 0, 13));
    l7.setFont(new java.awt.Font("serif", 0, 13));
    l7a.setFont(new java.awt.Font("serif", 0, 13));
    l8.setFont(new java.awt.Font("serif", 0, 13));
    
    line1.add(l1);
    line1.add(l2);
    line2.add(l3);
    line2.add(l4);
    line3.add(l6);
    line3.add(l5);
    line4.add(l7);
    line7a.add(l7a);
    if (l8 != null && stn.getOriginalPath() != null) {
      line5.add(l8);
    }
    mContents.add(line1);
    mContents.add(line2);
    mContents.add(line3);
    mContents.add(line4);
    mContents.add(line7a);
    if (stn.getOriginalPath() != null) {
      mContents.add(line5);
    }
    this.add(mContents, "Center");
    line1.invalidate();
    line2.invalidate();
    line3.invalidate();
    line4.invalidate();
    if (stn.getOriginalPath() != null) {
      line5.invalidate();
    }

    String valStr;
    if (sec.getNumStnVars() > 0 && mIncludeStnCalcs) {
      for (int v = 0; v < sec.getNumStnVars(); v++) {
        String paramStr = sec.getStnVar(v);
        String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen);
        double val = stn.getStnValue(v);
        if (val != JOAConstants.MISSINGVALUE) {
          valStr = new String(JOAFormulas.formatDouble(val, 3, true));
        }
        else {
          valStr = new String("    ----");
        }

        // add units if necessary
        valStr = valStr + JOAFormulas.returnSpacePaddedString(sec.getStnVarUnits(v), mMaxUnitsLen) + "   ";

        ParamValuePanel pvp = new ParamValuePanel();
        pvp.init(paddedParamStr, valStr, Color.black);
        mStnVals.add(pvp);
        mValuePanels.addElement(pvp);
      }
    }
    else if (mIncludeStnCalcs) {
      ParamValuePanel pvp = new ParamValuePanel();
      pvp.init(b.getString("kNone"), "", Color.black);
      mStnVals.add(pvp);
      mValuePanels.addElement(pvp);

    }
    this.add(new TenPixelBorder(mStnValsCont, 0, 0, 5, 5), "East");
  }

  public void setNewStn(Section sec, Station stn) {
    if (mIsLocked) {
      return;
    }
    setMaxParamLength(sec);
    setMaxUnitLength(sec);
    l1.setText("Section: " + sec.mSectionDescription);
    if (sec.getExpoCode() != null && sec.getExpoCode().length() > 0) {
    	l1.setText("Section: " + sec.mSectionDescription + " Expocode:" +sec.getExpoCode());
    }
    if (stn.mShipCode != null && stn.mShipCode.length() > 0) {
    	l2.setText("Platform: " + stn.mShipCode);
    }
    l3.setText("Stn: " + stn.mStnNum);
    l4.setText("Cast: " + stn.mCastNum + "   D: " + JOAFormulas.formatDouble((stn.mCumDist*1.852), 2, false) + " (km)");
    if (stn != null && stn.mLon == JOAConstants.MISSINGVALUE && stn.mLat == JOAConstants.MISSINGVALUE) {
      l5.setText("Lon: Missing");
      l6.setText("Lat: Missing");
    }
    else {
      l5.setText("Lon: " + JOAFormulas.formatLon(stn.mLon));
      l6.setText("Lat: " + JOAFormulas.formatLat(stn.mLat));
    }
    l7.setText("Date: " + JOAFormulas.formatDate(stn, true));
    if (stn.getBottom() != JOAConstants.MISSINGVALUE) {
      l7a.setText("Bottom: " + stn.getBottom());
    }
    else {
      l7a.setText("Bottom: ----");
    }
    String op = stn.getOriginalPath();
    String on = stn.getOriginalName();
    if (l8 != null && on != null && op != null) {
      if (!op.endsWith("\u005c\u005c")) {
        l8.setText("Original path: " + op + File.pathSeparator + on); //"\u005c\u005c" + on);
      }
      else {
        l8.setText("Original path: " + op + on);
      }
    }
    else if (l8 != null) {
      l8.setText("");
    }
    l1.invalidate();
    l2.invalidate();
    l3.invalidate();
    l4.invalidate();
    l5.invalidate();
    l6.invalidate();
    l7.invalidate();
    l7a.invalidate();
    if (l8 != null && stn.getOriginalPath() != null) {
      l8.invalidate();
    }

    // redraw the stn calc vals
    if (mIncludeStnCalcs) {
      String valStr;
      for (int v = 0; v < sec.getNumStnVars(); v++) {
        if (!mStnVarAdded) {
          mStnVarAdded = true;
          mWidth = 420;
          mFileViewer.setSize(mFileViewer.getSize().width + 150, mFileViewer.getSize().height);
          mFileViewer.validate();
        }
        String paramStr = sec.getStnVar(v);
        String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen);
        double val = stn.getStnValue(v);
        if (val != JOAConstants.MISSINGVALUE) {
          valStr = new String(JOAFormulas.formatDouble(val, 3, true));
        }
        else {
          valStr = new String("    ----");
        }

        // add units if necessary
        valStr = valStr + JOAFormulas.returnSpacePaddedString(sec.getStnVarUnits(v), mMaxUnitsLen) + "   ";

        ParamValuePanel pvp = null;
        try {
          pvp = (ParamValuePanel)mValuePanels.elementAt(v);
          pvp.setNewValue(valStr);
          pvp.setNewLabel(paddedParamStr);
          mStnVals.invalidate();
          mStnVals.validate();
        }
        catch (ArrayIndexOutOfBoundsException ex) {
          // need to add a value panel for this parameter
          pvp = new ParamValuePanel();
          pvp.init(paramStr, valStr, Color.black);
          mStnVals.add(pvp);
          mValuePanels.addElement(pvp);
          mStnVals.invalidate();
          mStnVals.validate();
        }
      }
    }
  }

  public void obsChanged(ObsChangedEvent evt) {
    // display the current station
    Station sh = evt.getFoundStation();
    Section sech = evt.getFoundSection();
    setNewStn(sech, sh);
  }

  public void parameterAdded(ParameterAddedEvent evt) {
    // redisplay the current station
    Station sh = evt.getFoundStation();
    Section sech = evt.getFoundSection();
    setNewStn(sech, sh);
    this.invalidate();
    this.validate();
  }

  public void prefsChanged(PrefsChangedEvent evt) {
    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sech = (Section)of.mSections.currElement();
    Station sh = (Station)sech.mStations.currElement();
    setNewStn(sech, sh);
    this.invalidate();
    this.validate();
  }

  public void stnFilterChanged(StnFilterChangedEvent evt) {
    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sech = (Section)of.mSections.currElement();
    Station sh = (Station)sech.mStations.currElement();
    setNewStn(sech, sh);
  }

  private void setMaxParamLength(Section sech) {
    mMaxParamLen = 0;
    for (int v = 0; v < sech.getNumStnVars(); v++) {
      if (sech.getStnVar(v) != null) {
        mMaxParamLen = sech.getStnVar(v).length() > mMaxParamLen ? sech.getStnVar(v).length() : mMaxParamLen;
      }
    }
  }

  private void setMaxUnitLength(Section sech) {
    mMaxUnitsLen = 0;
    for (int v = 0; v < sech.getNumStnVars(); v++) {
      if (sech.getStnVarUnits(v) != null) {
        mMaxUnitsLen = sech.getStnVarUnits(v).length() > mMaxUnitsLen ? sech.getStnVarUnits(v).length() : mMaxUnitsLen;
      }
    }
  }

  public void setLocked(boolean b) {
    mIsLocked = b;
  }
}

/*
 * $Id: MapLegend.java,v 1.6 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;

@SuppressWarnings("serial")
public class MapLegend extends JPanel {
  protected FileViewer mFileViewer;
  protected MapSpecification mMapSpec;
  protected JPanel contents = null;
  protected JPanel bathyLegend = null;
  protected JPanel etopoLegend = null;
  protected JPanel sectionLegend = null;
  protected JPanel[] bathyDetailCont = new JPanel[120];
  //protected JPanel[] sectionDetailCont = null;
  protected JOAJLabel mStnIsoLabel = new JOAJLabel("");
  protected JOAJLabel mContourIsoLabel = new JOAJLabel("");
  protected int mNumEntries = 0;
  protected JOAWindow mParent = null;
  ColorBarPanel mETOPOLegend = null;
  IsobathLegendPanel mIsobathLegendPanel = null;
  protected int mWidth;
  private ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  JFrame mParentFrame = null;
  double mCurrValue;

  public MapLegend(JOAWindow parent, JFrame jparent, FileViewer fv, MapSpecification mapSpec) {
    this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
    mParent = parent;
    mFileViewer = fv;
    mMapSpec = mapSpec;
    mParentFrame = jparent;
    init();
  }

  public double getCurrLabelValue() {
    return mCurrValue;
  }

  public void setMapSpecification(MapSpecification mapSpec) {
    mMapSpec = mapSpec;
  }

  public ColorBarPanel getEtopoLegend() {
    return mETOPOLegend;
  }

  public void paintEtopoLegend(Graphics g) {
    mETOPOLegend.paint(g);
  }

  public IsobathLegendPanel getIsobathLegend() {
    return mIsobathLegendPanel;
  }

  public void paintIsobathLegend(Graphics g) {
    mIsobathLegendPanel.paint(g);
  }

  public int getWidth() {
    return mWidth;
  }

  public void setWidth(int w) {
    mWidth = w;
    this.setSize(w, this.getSize().height);
    if (mETOPOLegend != null) {
      mETOPOLegend.setWidth(w);
    }
    if (mIsobathLegendPanel != null) {
      mIsobathLegendPanel.setWidth(w);
    }
    this.invalidate();
    this.validate();
  }

  public void init() {
    mNumEntries = 0;
    this.setLayout(new BorderLayout(5, 5));
    contents = new JPanel();
    contents.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
    contents.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 5));

    // etopo legend
    if (mMapSpec.isColorFill()) {
      mETOPOLegend = new ColorBarPanel(mParent, mFileViewer, mMapSpec.getBathyColorBar(), Color.white, Color.white, false,
                                          JOAConstants.HORIZONTAL_ORIENTATION, false);
      mETOPOLegend.setEnhanceable(false);
      mETOPOLegend.setLinked(false);
      mETOPOLegend.setLabelPanel(false);
      mETOPOLegend.setBroadcastMode(false);
      contents.add(mETOPOLegend);
    }

    // bathymetry legend
    // check for any map isobaths
    int bathyCount = mMapSpec.getNumIsobaths();
    if (bathyCount > 0) {
      mIsobathLegendPanel = new IsobathLegendPanel(mMapSpec);
      mIsobathLegendPanel.setBackground(Color.white);
      mIsobathLegendPanel.setOpaque(false);
      contents.add(mIsobathLegendPanel);
    }
    
    if (mMapSpec.getStnColorMode() == mMapSpec.COLOR_STNS_BY_ISOSURFACE) {
      resetLevel(mMapSpec.getStnColorByIsoIsoSurfaceValue(), IsoBrowsingMode.STN_SYMBOL);
    }
    else {
      resetLevel(null, IsoBrowsingMode.STN_SYMBOL);
    }
    
    if (mMapSpec.getContourOverlayMode() == mMapSpec.CONTOUR_OVERLAY_BY_ISOSURFACE) {
      resetLevel(mMapSpec.getContourIsoSurfaceValue(), IsoBrowsingMode.OVERLAY_CONTOUR);
    }
    else {
      resetLevel(null, IsoBrowsingMode.STN_SYMBOL);
    }

      JPanel isoSurfLbls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
      isoSurfLbls.add(mStnIsoLabel);
      isoSurfLbls.add(mContourIsoLabel);
      this.add(isoSurfLbls, BorderLayout.SOUTH);

    this.add("Center", contents);
  }

  public void forceRedraw() {
    mParentFrame.setSize(mParentFrame.getSize().width + 1, mParentFrame.getSize().height);
    mParentFrame.setSize(mParentFrame.getSize().width - 1, mParentFrame.getSize().height);
  }

  public void setNewBGColor(Color in) {
    this.setBackground(in);
    contents.setBackground(in);
    if (sectionLegend != null) {
      sectionLegend.setBackground(in);
    }
    for (int i = 0; i < mMapSpec.getNumIsobaths(); i++) {
      bathyDetailCont[i].setBackground(in);
    }
    // if (mNumEntries > 0) {
    //	for (int i=0; i<mNumEntries; i++) {
    //		if (sectionDetailCont[i] != null)
    //			sectionDetailCont[i].setBackground(in);
    //	}
    //}
  }

  public void paintComponent(Graphics g) {
    if (g instanceof PrintGraphics) {
      int textOffset = 12; // + fm.getDescent();
      int leftMargin = 10;
      double rowCnt = 0.5;

      int h1 = -10;
      int h2 = -10;

      if (mETOPOLegend != null) {
        int width = mETOPOLegend.getWidth();
        int height = mETOPOLegend.getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, 2000, 2000);
        g.setClip(0, 0, width, height);
        // plot the color fill legend
        h1 = height;
        // g already translated
        mETOPOLegend.paint(g);
        g.setClip(0, 0, 2000, 2000);
      }

      // bathymetry legend--check for any map isobaths
      if (mIsobathLegendPanel != null) {
        int width = mIsobathLegendPanel.getWidth();
        int height = mIsobathLegendPanel.getHeight();
        h2 = height;
        g.translate(0, h1 + 10);
        g.setColor(Color.white);
        g.fillRect(0, 0, 2000, 2000);
        g.setClip(0, 0, width, height);
        mIsobathLegendPanel.paint(g);
        g.setClip(0, 0, 2000, 2000);
      }

      // draw the isosurface legend
      if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_ISOSURFACE) {
        // isosurface label
        g.translate(0, (h1 + 10) + (h2 + 10));
        g.setColor(Color.white);
        g.fillRect(0, 0, 2000, 2000);
        g.setColor(Color.black);
        String valStr = null;
        String refStr = "";
        if (mMapSpec.isStnColorByIsoIsReferenced()) {
        	refStr += " (ref = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getStnColorByIsoReferenceLevel()), 0, false) + ")";
        }	
        if (mMapSpec.isStnColorByIsoMinIsoSurfaceValue()) {
          valStr = " at minimum";
        }
        else if (mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()) {
          valStr = " at maximum";
        }
        else {
          valStr = " = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getStnColorByIsoIsoSurfaceValue()), 3, false);
        }

        String isoLabel = mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel() + " on " +
            mMapSpec.getStnColorByIsoSurface().getParam() + valStr + " (" +
            mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoSurfVarCode()].getUnits() + ")" + refStr;
        
        g.drawString(isoLabel, leftMargin, (int)(rowCnt * 15) + textOffset);
      }

      /*// draw a rectangle around the whole thing
         g.setColor(Color.black);
           g.drawRect(0, 0, this.getWidth(), (int)(rowCnt+1.5) * 15);

           // draw the label
         g.setColor(Color.white);
           g.fillRect(3, -1, 3 + fm.stringWidth(b.getString("kLegend")), 2);
         g.setColor(Color.black);
         g.drawString(b.getString("kLegend"), 5, 5);*/
    }
    else {
      super.paintComponent(g);
    }
  }

  public void reset(MapSpecification mapSpec) {
    mMapSpec = mapSpec;
    mStnIsoLabel.setText("");
    mContourIsoLabel.setText("");
    this.removeAll();
    this.init();
    this.invalidate();
  }

  public void resetLevel(IsoBrowsingMode mode) {
    if (mode != null && mode == IsoBrowsingMode.STN_SYMBOL) {
      try {
        String valStr = null;
        String refStr = "";

        if (mMapSpec.isStnColorByIsoIsReferenced()) {
        	refStr = " (ref = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getStnColorByIsoReferenceLevel()), 0, false) + ")";
        }
        
        if (mMapSpec.isStnColorByIsoMinIsoSurfaceValue()) {
          valStr = b.getString("kAtMin");
        }
        else if (mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()) {
          valStr = b.getString("kAtMax");
        }
        else {
          valStr = " = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getStnColorByIsoIsoSurfaceValue()), 3, false);
        }

        String filterStr = null;
        if (mFileViewer.mObsFilterActive) {
          filterStr = b.getString("kSubjectTo");
        }
        else {
          filterStr = "";
        }
        String sisoLabel = new String("Stn Symbol: " + mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel() +
                                      b.getString("kOnwspaces") + mMapSpec.getStnColorByIsoSurface().getParam() + valStr + " (" +
                                      mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoSurfVarCode()].getUnits() + ")" + refStr +
                                      filterStr);
        mStnIsoLabel.setText(sisoLabel);
        mStnIsoLabel.invalidate();
        mStnIsoLabel.validate();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    else if (mode != null && mode == IsoBrowsingMode.OVERLAY_CONTOUR) {
      String valStr = null;
      String refStr = "";

      if (mMapSpec.isIsoContourReferenced()) {
      	refStr = " (ref = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getIsoContourReferenceLevel()), 0, false) + ")";
      }
      
      if (mMapSpec.isIsoContourMinSurfaceValue()) {
        valStr = b.getString("kAtMin");
      }
      else if (mMapSpec.isIsoContourMaxSurfaceValue()) {
        valStr = b.getString("kAtMax");
      }
      else {
        valStr = " = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getContourIsoSurfaceValue()), 3, false);
      }

      String filterStr = null;
      if (mFileViewer.mObsFilterActive) {
        filterStr = b.getString("kSubjectTo");
      }
      else {
        filterStr = "";
      }
      String sisoLabel = new String("Contour: " + mFileViewer.mAllProperties[mMapSpec.getIsoContourVarCode()].getVarLabel() +
                                    b.getString("kOnwspaces") + mMapSpec.getIsoContourSurface().getParam() + valStr + " (" +
                                    mFileViewer.mAllProperties[mMapSpec.getIsoContourSurfVarCode()].getUnits() + ")" + refStr +
                                    filterStr);
      mContourIsoLabel.setText(sisoLabel);
      mContourIsoLabel.invalidate();
      mContourIsoLabel.validate();
    }
    
    if (mMapSpec.getStnColorMode() == MapSpecification.CONTOUR_OVERLAY_BY_NONE) {
      mStnIsoLabel.setText("");
      mStnIsoLabel.invalidate();
      mStnIsoLabel.validate();
    }
    else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section sech = (Section)of.mSections.currElement();
      String sisoLabel = b.getString("kStnColorsEqual") + sech.mStnProperties[mMapSpec.getStnColorByStnValVarCode()];
      mStnIsoLabel.setText(sisoLabel);
      mStnIsoLabel.invalidate();
      mStnIsoLabel.validate();
    }
    else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
      String sisoLabel = b.getString("kStnColorsEqual") + mMapSpec.getStnColorColorBar().getTitle();
      mStnIsoLabel.setText(sisoLabel);
      mStnIsoLabel.invalidate();
      mStnIsoLabel.validate();
    }

    if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_NONE) {
      mContourIsoLabel.setText("");
      mContourIsoLabel.invalidate();
      mContourIsoLabel.validate();
    }
    else if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section sech = (Section)of.mSections.currElement();
      String sisoLabel = "Contours: " + sech.mStnProperties[mMapSpec.getStnCalcContourVarCode()];
      mContourIsoLabel.setText(sisoLabel);
      mContourIsoLabel.invalidate();
      mContourIsoLabel.validate();
    }
  }

  public void resetLevel(Double value, IsoBrowsingMode mode) {
    if (value != null && mode != null && mode == IsoBrowsingMode.STN_SYMBOL) {
      try {
        String valStr = null;
        String refStr = "";

        if (mMapSpec.isStnColorByIsoIsReferenced()) {
        	refStr = " (ref = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getStnColorByIsoReferenceLevel()), 0, false) + ")";
        }
        
        if (mMapSpec.isStnColorByIsoMinIsoSurfaceValue()) {
          valStr = b.getString("kAtMin");
        }
        else if (mMapSpec.isStnColorByIsoMaxIsoSurfaceValue()) {
          valStr = b.getString("kAtMax");
        }
        else {
          valStr = " = " + JOAFormulas.formatDouble(String.valueOf(value), 3, false);
        }

        String filterStr = null;
        if (mFileViewer.mObsFilterActive) {
          filterStr = b.getString("kSubjectTo");
        }
        else {
          filterStr = "";
        }

        String sisoLabel = new String("Stn Symbol: " + mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoVarCode()].getVarLabel() +
                                      b.getString("kOnwspaces") + mMapSpec.getStnColorByIsoSurface().getParam() + valStr + " (" +
                                      mFileViewer.mAllProperties[mMapSpec.getStnColorByIsoSurfVarCode()].getUnits() + ")" + refStr +
                                      filterStr);
        mStnIsoLabel.setText(sisoLabel);
        mStnIsoLabel.invalidate();
        mStnIsoLabel.validate();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    else if (value != null && mode != null && mode == IsoBrowsingMode.OVERLAY_CONTOUR) {
      String valStr = null;
      String refStr = "";

      if (mMapSpec.isIsoContourReferenced()) {
      	refStr = " (ref = " + JOAFormulas.formatDouble(String.valueOf(mMapSpec.getIsoContourReferenceLevel()), 0, false) + ")";
      }
      
      if (mMapSpec.isIsoContourMinSurfaceValue()) {
        valStr = b.getString("kAtMin");
      }
      else if (mMapSpec.isIsoContourMaxSurfaceValue()) {
        valStr = b.getString("kAtMax");
      }
      else {
        valStr = " = " + JOAFormulas.formatDouble(String.valueOf(value), 3, false);
      }

      String filterStr = null;
      if (mFileViewer.mObsFilterActive) {
        filterStr = b.getString("kSubjectTo");
      }
      else {
        filterStr = "";
      }
      String sisoLabel = new String("Contour: " + mFileViewer.mAllProperties[mMapSpec.getIsoContourVarCode()].getVarLabel() +
                                    b.getString("kOnwspaces") + mMapSpec.getIsoContourSurface().getParam() + valStr + " (" +
                                    mFileViewer.mAllProperties[mMapSpec.getIsoContourSurfVarCode()].getUnits() + ")" + refStr +
                                    filterStr);
      mContourIsoLabel.setText(sisoLabel);
      mContourIsoLabel.invalidate();
      mContourIsoLabel.validate();	
    }

    
    if (mMapSpec.getStnColorMode() == MapSpecification.CONTOUR_OVERLAY_BY_NONE) {
      mStnIsoLabel.setText("");
      mStnIsoLabel.invalidate();
      mStnIsoLabel.validate();
    }
    else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STNVAR) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section sech = (Section)of.mSections.currElement();
      String sisoLabel = b.getString("kStnColorsEqual") + sech.mStnProperties[mMapSpec.getStnColorByStnValVarCode()];
      mStnIsoLabel.setText(sisoLabel);
      mStnIsoLabel.invalidate();
      mStnIsoLabel.validate();
    }
    else if (mMapSpec.getStnColorMode() == MapSpecification.COLOR_STNS_BY_STN_METADATA) {
      String sisoLabel = b.getString("kStnColorsEqual") + mMapSpec.getStnColorColorBar().getTitle();
      mStnIsoLabel.setText(sisoLabel);
      mStnIsoLabel.invalidate();
      mStnIsoLabel.validate();
    }
    
    if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_NONE) {
      mContourIsoLabel.setText("");
      mContourIsoLabel.invalidate();
      mContourIsoLabel.validate();
    }
    else if (mMapSpec.getContourOverlayMode() == MapSpecification.CONTOUR_OVERLAY_BY_STNVAR) {
      OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
      Section sech = (Section)of.mSections.currElement();
      String sisoLabel = "Contours: " + sech.mStnProperties[mMapSpec.getStnCalcContourVarCode()];
      mContourIsoLabel.setText(sisoLabel);
      mContourIsoLabel.invalidate();
      mContourIsoLabel.validate();
    }
  }

  public void setLocked(boolean b) {
    mETOPOLegend.setLocked(b);
  }
}

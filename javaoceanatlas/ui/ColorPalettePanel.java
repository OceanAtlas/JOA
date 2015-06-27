/*
 * $Id: ColorPalettePanel.java,v 1.2 2005/06/17 18:08:51 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;

@SuppressWarnings("serial")
public class ColorPalettePanel extends JPanel {
  protected ColorPalette mColorPalette;
  protected boolean mRangeHilited = false;
  protected int mFirstHilited = -99;
  protected int mLastHilited = -99;
  protected JPanel mThis;
  protected Component mParent = null;
  protected int mSelectedEntry = 0;
  protected boolean mIsDirty = false;
  protected String mCurrPalName = null;
  protected Point mAnchorPt = new Point(0, 0);
  protected Point mStretchedPt = new Point(0, 0);
  protected Point mLastPt = new Point(0, 0);
  protected Point mEndPt = new Point(0, 0);
  protected boolean mFirstStretch = true;
  protected boolean mActive = true;
  protected int mOldRow = -99;
  protected int mOldCol = -99;
  protected Vector<ColorSelChangedListener> mListeners = new Vector<ColorSelChangedListener>();
  protected int mMaxTop = 0;
  protected int mMaxRight = 0;

  public ColorPalettePanel(ColorPalette inPalette) {
    mColorPalette = inPalette;
    init();
  }

  public ColorPalettePanel() {
    mColorPalette = new ColorPalette();
    init();
  }

  public void setNewColorPalette() {
    mColorPalette = new ColorPalette();
    mCurrPalName = null;

    // redraw
    removeSelection();
    Graphics g = mThis.getGraphics();
    if (g != null) {
      paintComponent(g);
      g.dispose();
    }
    mIsDirty = false;
  }

  public void init() {
    addMouseListener(new MyMouseHandler());
    addMouseMotionListener(new MyMouseMotionHandler());
    mThis = this;
    mParent = this.getParent();
  }

  public class MyMouseHandler extends MouseAdapter {
    public void mouseClicked(MouseEvent me) {
      if (isActive()) {
        end(me.getPoint());
      }
      // turn click into a color cell
      int x = me.getX();
      int y = me.getY();
      int row = y / 12 + 1;
      int col = x / 12 + 1;
      mSelectedEntry = (row - 1) * 16 + col - 1;

      // see if it's a double click--if so display a picker
      int clicks = me.getClickCount();
      if (clicks > 1) {
        // get a new color
        Color oldColor = mColorPalette.getColor(mSelectedEntry);
        Color mColor = JColorChooser.showDialog(mParent, "Choose a new color:", oldColor);
        if (mColor != null) {
          Graphics g = mThis.getGraphics();
          mColorPalette.setColor(mSelectedEntry, mColor);
          if (g != null) {
            paintComponent(g);
            g.dispose();
          }
          mIsDirty = true;
        }
      }
      else {
        // get the shift key
        boolean shiftDown = me.isShiftDown();

        if (shiftDown) {
          if (mSelectedEntry < mFirstHilited) {
            mLastHilited = mFirstHilited;
            mFirstHilited = mSelectedEntry;
          }
          else {
            if (mFirstHilited == -99) {
              // no range exists
              mLastHilited = -99;
              mFirstHilited = mSelectedEntry;
              mRangeHilited = false;
            }
            else {
              mLastHilited = mSelectedEntry;
              mRangeHilited = true;
            }
          }
        }
        else {
          mFirstHilited = mSelectedEntry;
          mRangeHilited = false;
          mLastHilited = -99;
        }
        Graphics g = mThis.getGraphics();
        if (g != null) {
          removeHilite(g);
          paintHilite(g);
          g.dispose();
        }
      }
    }

    public void mousePressed(MouseEvent me) {
      if (isActive()) {
        anchor(me.getPoint());
      }

    }

    public void mouseReleased(MouseEvent me) {
      if (isActive()) {
        end(me.getPoint());
      }

    }
  }

  public class MyMouseMotionHandler extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent me) {
      if (isActive()) {
        stretch(me.getPoint());
      }
    }
  }

  public void anchor(Point p) {
    mFirstStretch = true;
    mAnchorPt.x = p.x;
    mAnchorPt.y = p.y;

    mStretchedPt.x = mLastPt.x = mAnchorPt.x;
    mStretchedPt.y = mLastPt.y = mAnchorPt.y;
  }

  public void end(Point p) {
    mLastPt.x = mEndPt.x = p.x;
    mLastPt.y = mEndPt.y = p.y;
    drawLast();
  }

  public void stretch(Point p) {
    mLastPt.x = mStretchedPt.x;
    mLastPt.y = mStretchedPt.y;

    mStretchedPt.x = p.x;
    mStretchedPt.y = p.y;
    if (mFirstStretch == true) {
      mFirstStretch = false;
    }
    else {
      drawLast();
    }
    drawNext();
  }

  public boolean isActive() {
    return mActive;
  }

  public void setActive(boolean b) {
    mActive = b;
  }

  public void drawLast() {
    /*int x = mStretchedPt.x;
       int y = mStretchedPt.y;
       int row = y/12 + 1;
       int col = x/12 + 1;
       if (row == mOldRow && col == mOldCol)
     return;
       mOldRow = row;
       mOldCol = col;
       System.out.println("in drawLast, row =" + row + " col = " + col);
       mSelectedEntry = (row-1) * 16 + col - 1;
       if (mSelectedEntry < mFirstHilited) {
     mLastHilited = mFirstHilited;
     mFirstHilited = mSelectedEntry;
       }
       else {
     if (mFirstHilited == -99) {
      // no range exists
      mLastHilited = -99;
      mFirstHilited = mSelectedEntry;
      mRangeHilited = false;
     }
     else {
      mLastHilited = mSelectedEntry;
      mRangeHilited = true;
     }
       }
       Graphics g = mThis.getGraphics();
       if (g != null) {
     removeHilite(g);
     paintHilite(g);
     g.dispose();
       }*/
  }

  public void drawNext() {
    int x = mStretchedPt.x;
    int y = mStretchedPt.y;
    if (y > mMaxTop) {
      y = mMaxTop;
    }
    if (x > mMaxRight) {
      x = mMaxRight;
    }
    int row = y / 12 + 1;
    int col = x / 12 + 1;
    if (row == mOldRow && col == mOldCol) {
      return;
    }
    mOldRow = row;
    mOldCol = col;
    mSelectedEntry = (row - 1) * 16 + col - 1;
    if (mSelectedEntry < mFirstHilited) {
      mLastHilited = mFirstHilited;
      mFirstHilited = mSelectedEntry;
    }
    else {
      if (mFirstHilited == -99) {
        // no range exists
        mLastHilited = -99;
        mFirstHilited = mSelectedEntry;
        mRangeHilited = false;
      }
      else {
        mLastHilited = mSelectedEntry;
        mRangeHilited = true;
      }
    }
    Graphics g = mThis.getGraphics();
    if (g != null) {
      removeHilite(g);
      paintHilite(g);
      g.dispose();
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    paintPalette(g);
    //if (mRangeHilited) {
    paintHilite(g);
    //}
  }

  public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }

  protected void paintHilite(Graphics g) {
    if (mFirstHilited == -99) {
      return;
    }
    int i = mFirstHilited;
    do {
      int top = rowFromEntry(i) * 12;
      int left = colFromEntry(i) * 12;
      g.setColor(Color.black);
      g.drawRect(left, top, 12, 12);
      i++;
    }
    while (i < mLastHilited + 1);
  }

  protected int rowFromEntry(int entry) {
    return entry / 16;
  }

  protected int colFromEntry(int entry) {
    return entry % 16;
  }

  protected void paintPalette(Graphics g) {
    int left = 0;
    int top = 0;
    int i = 0;
    mMaxTop = 0;
    mMaxRight = 0;
    for (int r = 1; r <= 16; r++) {
      top = (r - 1) * 12 + 0; // 0 is the top of the panel
      mMaxTop = top + 2 + 9;
      for (int c = 1; c <= 16; c++) {
        left = (c - 1) * 12 + 0; // 0 is left of the panel
        g.setColor(Color.white);
        g.drawRect(left, top, 12, 12);
        g.setColor(mColorPalette.getColor(i));
        g.fillRect(left + 2, top + 2, 9, 9);
        mMaxRight = left + 2 + 9;
        i++;
      }
    }
  }

  protected void removeHilite(Graphics g) {
    int left = 0;
    int top = 0;
    int i = 0;
    for (int r = 1; r <= 16; r++) {
      top = (r - 1) * 12 + 0; // 0 is the top of the panel
      for (int c = 1; c <= 16; c++) {
        left = (c - 1) * 12 + 0; // 0 is left of the panel
        g.setColor(Color.white);
        g.drawRect(left, top, 12, 12);
        i++;
      }
    }
  }

  public int getSelStart() {
    return mFirstHilited;
  }

  public int getSelEnd() {
    return mLastHilited;
  }

  public void blend(int start, int end) {
    mColorPalette.blend(start, end);
    Graphics g = mThis.getGraphics();
    if (g != null) {
      paintComponent(g);
      g.dispose();
    }
    mIsDirty = true;
  }

  public boolean isDirty() {
    return mIsDirty;
  }

  public void pick() {
    // get a new color
    Color oldColor = mColorPalette.getColor(mSelectedEntry);
    Color mColor = JColorChooser.showDialog(mParent, "Choose a new color:", oldColor);
    if (mColor != null) {
      Graphics g = mThis.getGraphics();
      mColorPalette.setColor(mSelectedEntry, mColor);
      if (g != null) {
        paintComponent(g);
        g.dispose();
      }
      mIsDirty = true;
    }
  }

  public void save() {
    // save the color palette object to the support directory
    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("_pal.xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Save color table as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    if (mCurrPalName != null) {
      f.setFile(mCurrPalName);
    }
    else {
      f.setFile("untitled_pal.xml");
    }
    f.setVisible(true);
    directory = f.getDirectory();
    f.dispose();
    if (directory != null && f.getFile() != null) {
      File nf = new File(directory, f.getFile());
      mColorPalette.saveAsXML(nf);
    }
  }

  public void removeSelection() {
    mFirstHilited = -99;
    mLastHilited = -99;
    mSelectedEntry = 0;
  }

  public void setNewPalette(ColorPalette newPal, String newPalName) {
    if (newPal != null) {
      mColorPalette = null;
      mColorPalette = newPal;
      mCurrPalName = newPalName;
      Graphics g = mThis.getGraphics();
      if (g != null) {
        removeSelection();
        paintComponent(g);
        g.dispose();
      }
      mIsDirty = false;
    }
  }

  public void addColorSelChangedListener(ColorSelChangedListener l) {
    if (mListeners.indexOf(l) < 0) {
      mListeners.addElement(l);
    }
  }

  public void removeColorSelChangedListener(ColorSelChangedListener l) {
    mListeners.removeElement(l);
  }

  public void processEvent(AWTEvent evt) {
    if (evt instanceof ColorSelChangedEvent) {
      if (mListeners != null) {
        for (int i = 0; i < mListeners.size(); i++) {
          ((ColorSelChangedListener)mListeners.elementAt(i)).colorChanged((ColorSelChangedEvent)evt);
        }
      }
    }
    else {
      super.processEvent(evt);
    }
  }
}

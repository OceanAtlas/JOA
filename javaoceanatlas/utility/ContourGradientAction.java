/*
 * $Id: ContourPlotAction.java,v 1.1 2005/09/07 18:52:04 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.event.*;
import javax.swing.*;
import javaoceanatlas.ui.*;

@SuppressWarnings("serial")
public class ContourGradientAction extends AbstractAction {
  protected String mText;
  protected FileViewer mFV;
  protected KeyStroke mAccelerator;
  JFrame mParent;

  public ContourGradientAction(JFrame parent, FileViewer fv, String text, KeyStroke ks) {
    super(text, null);
    mParent = parent;
    mText = text;
    mFV = fv;
    mAccelerator = ks;
    putValue(ACCELERATOR_KEY, mAccelerator);
  }

  public void actionPerformed(ActionEvent e) {
    // Open the config contour dialog
    ConfigGradientContourPlot contourDialog = new ConfigGradientContourPlot(mParent, mFV);
    contourDialog.pack();
    contourDialog.setVisible(true);
  }

  public String getText() {
    return mText;
  }

  public boolean isEnabled() {
    return true;
  }

  public KeyStroke getAccelerator() {
    return mAccelerator;
  }
}

package javaoceanatlas.utility;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.resources.JOAConstants;

@SuppressWarnings("serial")
public class MaintainDatabaseAction extends AbstractAction {
  protected String mText;
  protected Component mParent;
  protected KeyStroke mAccelerator;
  final ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

  public MaintainDatabaseAction(Component par, String text, KeyStroke ks) {
    super(text, null);
    mText = text;
    mParent = par;
    mAccelerator = ks;
    if (ks != null) {
      putValue(ACCELERATOR_KEY, mAccelerator);
    }
  }

  public String getText() {
    return b.getString("kMaintainDatabase");
  }

  public void actionPerformed(ActionEvent e) {
    MaintainDatabase config = new MaintainDatabase();
    config.setVisible(true);
  }

  public boolean isEnabled() {
      return JOAConstants.CONNECTED_TO_DB;
  }

  public KeyStroke getAccelerator() {
    return mAccelerator;
  }

  public void setAccelerator(KeyStroke ks) {
    mAccelerator = ks;
    putValue(ACCELERATOR_KEY, mAccelerator);
  }
}

//KeyboardFocusManager.getActiveWindow() or
//KeyboardFocusManager.getGlobalActiveWindow()

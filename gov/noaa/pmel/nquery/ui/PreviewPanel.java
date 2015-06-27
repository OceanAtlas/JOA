/*
 * $Id: PreviewPanel.java,v 1.8 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gov.noaa.pmel.nquery.resources.*;
import javax.swing.border.*;

/**
 * <code>PreviewPanel</code> UI for presenting an SQL query statements as it's constructed.
 *
 * @author John Osborne
 * @version $Revision: 1.8 $, $Date: 2004/09/14 19:11:26 $
 */

public class PreviewPanel extends JPanel {
  protected JTextArea mPreview = null;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  boolean mEnabled = true;

  public PreviewPanel() {
    this.setLayout(new BorderLayout(10, 10));
    mPreview = new JTextArea(0, 0);
    //mPreview.setPreferredSize(new Dimension(400, 50));
    //mPreview.setMaximumSize(new Dimension(400, 50));
    mPreview.setWrapStyleWord(true);
    mPreview.setLineWrap(true);

    this.add("North", new MyScroller(mPreview));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kSelectCmdPreview"));
    this.setBorder(tb);
  }

  public void setEnabled(boolean b) {
    mEnabled = b;
    mPreview.setEnabled(b);
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public String getContents() {
    return mPreview.getText();
  }

  public void setContents(String cmd) {
    mPreview.setText(cmd);
  }

  private class MyScroller extends JScrollPane {
    public MyScroller(Component c) {
      super(c);
      this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public Dimension getPreferredSize() {
      return new Dimension(400, 100);
    }
  }
}

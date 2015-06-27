/*
 * $Id: JOAJCheckBox.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJCheckBox extends JCheckBox {
  public JOAJCheckBox(String s) {
    super(s);
    //if (JOAConstants.ISMAC) {
    //	setFont(new Font("Helvetica", Font.PLAIN, 11));
    //}
  }

  public JOAJCheckBox(String s, boolean state) {
    super(s, state);
    //if (JOAConstants.ISMAC) {
    //	setFont(new Font("Helvetica", Font.PLAIN, 11));
    //}
  }
}

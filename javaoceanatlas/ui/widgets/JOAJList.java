/*
 * $Id: JOAJList.java,v 1.4 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.util.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class JOAJList extends JList {
  public JOAJList(Object[] o) {
    super(o);
    //if (JOAConstants.ISMAC) {
    //	setFont(new Font("Helvetica", Font.PLAIN, 11));
    //}
  }

  public JOAJList(Vector<?> v) {
    super(v);
    //if (JOAConstants.ISMAC) {
    //	setFont(new Font("Helvetica", Font.PLAIN, 11));
    //}
  }

  public JOAJList() {
    super();
    //if (JOAConstants.ISMAC) {
    //	setFont(new Font("Helvetica", Font.PLAIN, 11));
    //}
  }
}

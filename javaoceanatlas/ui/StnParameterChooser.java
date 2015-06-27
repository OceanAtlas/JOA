/*
 * $Id: StnParameterChooser.java,v 1.2 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import java.awt.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class StnParameterChooser extends ParameterChooser {

  public StnParameterChooser(FileViewer fv, String title, Component comp, int numVisible, String prototypeCell) {
    super(fv, title, comp, numVisible, prototypeCell);
  }

  protected void buildList() {
    Vector<String> listData = new Vector<String>();
    OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
    Section sech = (Section)of.mSections.currElement();
    for (int i = 0; i < sech.getNumStnVars(); i++) {
      listData.addElement(sech.getStnVar(i));
    }

    // deal with additions
    for (int i = 0; i < mListAdditions.size(); i++) {
      listData.addElement((String)mListAdditions.elementAt(i));
    }
    if (mList == null) {
      mList = new JOAJList(listData);
    }
    else {
      mList.setListData(listData);
      mList.invalidate();
    }
  }

}

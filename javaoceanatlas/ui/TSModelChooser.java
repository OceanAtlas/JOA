/**
 * 
 */
package javaoceanatlas.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javaoceanatlas.ui.widgets.JOAJLabel;
import javaoceanatlas.ui.widgets.JOAJList;
import net.sourceforge.openforecast.ForecastingModel;

/**
 * @author oz
 *
 */
public class TSModelChooser extends JPanel {
  Vector<ForecastingModel> mAllModels;
  protected String mTitle;
  protected Component mComp;
  protected JOAJList mList = new JOAJList();
  protected String mProtoCell = null;
  protected int mVisibleRows = 6;
  protected JOAJLabel l1;

  public TSModelChooser(Vector<ForecastingModel> models, String title, Component comp, String prototypeCell) {
  	mAllModels = models;
    mTitle = title;
    mComp = comp;
    mProtoCell = prototypeCell;
    init();
  }

  public void init() {
    this.setLayout(new BorderLayout(5, 5));
    l1 = new JOAJLabel(mTitle, JOAJLabel.LEFT);
    this.add(BorderLayout.NORTH, l1);
    
    if (mAllModels != null) {
    	buildList();
    }
    mList.setVisibleRowCount(mVisibleRows - 1);
      mList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
    if (mProtoCell != null) {
      mList.setPrototypeCellValue(mProtoCell);
    }
    else {
      mList.setPrototypeCellValue("SALT         ");
    }
    JScrollPane listScroller = new JScrollPane(mList);
    listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.add(BorderLayout.CENTER, listScroller);
    mList.addListSelectionListener((ListSelectionListener)mComp);
  }

  public JOAJList getJList() {
    return mList;
  }
  
  public void setModels(Vector<ForecastingModel> newModels) {
  	mAllModels = newModels;
  	buildList();
  }

  protected void buildList() {
    Vector<String> listData = new Vector<String>();

    for (int i = 0; i < mAllModels.size(); i++) {
    	ForecastingModel model = mAllModels.elementAt(i);
    	String modelStr = model.getForecastType();
      if (model.isBestModel()) {
      	modelStr += " <<BEST MODEL>>";
      }
      listData.addElement(modelStr);
    }

      mList.setListData(listData);
      mList.invalidate();
  }

  public void setEnabled(boolean state) {
    if (state == false) {
      this.getJList().clearSelection();
    }
    this.getJList().setEnabled(state);
    l1.setEnabled(state);
    l1.invalidate();
  }

  public ForecastingModel getSelectedModel() {
    int selParam = mList.getSelectedIndex();
    if (selParam >= 0) {
      return mAllModels.elementAt(selParam);
    }
    else {
      return null;
    }
  }
}

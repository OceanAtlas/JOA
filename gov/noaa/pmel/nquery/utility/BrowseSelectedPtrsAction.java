/* * $Id: BrowseSelectedPtrsAction.java,v 1.1 2005/09/20 22:06:01 oz Exp $ * */package gov.noaa.pmel.nquery.utility;import java.awt.*;import java.awt.event.*;import javax.swing.*;import ndEdit.*;import gov.noaa.pmel.nquery.*;import gov.noaa.pmel.eps2.*;import gov.noaa.pmel.nquery.ui.*;import java.util.*;public class BrowseSelectedPtrsAction extends AbstractAction implements ndEdit.DialogClient {  protected int[] mSortKeys = null;  protected EpicPtrs ptrDB;  protected NdEdit mParent;  protected String mText;  protected String mTitle;  protected NQuery mNQ;  public BrowseSelectedPtrsAction(String text, Icon icon, NdEdit parent) {    super(text, icon);    mText = text;    mParent = parent;    putValue(ACCELERATOR_KEY,             KeyStroke.getKeyStroke(KeyEvent.VK_O,                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +                                    java.awt.event.InputEvent.ALT_MASK, false));  }  public String getText() {    return mText;  }  /**   * actionPerformed   *   * @param e ActionEvent   * This is how selections from pointer files are handled   */  public void actionPerformed(ActionEvent e) {    try {      NQVariableInspector varUI = new NQVariableInspector();      NQueryFormulas.centerFrameOnScreen(varUI, false);      PointerFileAttributes attr = mParent.getSelAttributes();      varUI.setAttributes(attr);      ArrayList fs = mParent.getSelFileSets(attr);      varUI.setFileSets(fs);      varUI.setVisible(true);    }    catch (Exception ex) {      ex.printStackTrace();    }  }  // OK Button  public void dialogDismissed(JDialog d) {  }  public boolean isEnabled() {    PointerCollectionGroup pc = mParent.getPointerCollection();    return (pc != null && pc.isSomethingSelected());  }  // Cancel button  public void dialogCancelled(JDialog d) {}  // something other than the OK button  //public void dialogDismissedTwo(Frame d);  // Apply button, OK w/o dismissing the dialog  public void dialogApply(JDialog d) {}  // Apply button, OK w/o dismissing the dialog  public void dialogApplyTwo(Object d) {}  // Apply button, OK w/o dismissing the dialog  public void dialogDismissedTwo(JDialog d) {}}
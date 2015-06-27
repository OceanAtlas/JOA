/* * $Id: ExportSSAction.java,v 1.1 2005/09/07 18:43:19 oz Exp $ * */package javaoceanatlas.io;import java.awt.*;import java.awt.event.*;import javax.swing.*;import javaoceanatlas.ui.*;public class ExportWOCEExchangeAction extends AbstractAction {  protected String mText;  protected FileViewer mFV;  protected String mTitle;  public ExportWOCEExchangeAction(FileViewer fv, String text) {    super(text, null);    mFV = fv;    mText = text;  }  public String getText() {    return mText;  }  public void actionPerformed(ActionEvent e) {    ConfigureWOCWExchangeExport expDialog = new ConfigureWOCWExchangeExport(mFV);    // show dialog at expDialog of screen    Rectangle dBounds = expDialog.getBounds();    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();    int x = sd.width / 2 - dBounds.width / 2;    int y = sd.height / 2 - dBounds.height / 2;    expDialog.setLocation(x, y);    expDialog.setVisible(true);  }  public boolean isEnabled() {    return true;  }}
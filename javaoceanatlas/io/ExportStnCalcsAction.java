/* * $Id: ExportStnCalcsAction.java,v 1.2 2005/09/21 05:29:46 oz Exp $ * */package javaoceanatlas.io;import java.awt.*;import java.awt.event.*;import javax.swing.*;import javaoceanatlas.ui.*; @SuppressWarnings("serial")public class ExportStnCalcsAction extends AbstractAction {   protected String mText;   protected FileViewer mFV;   protected String mTitle;    public ExportStnCalcsAction(FileViewer fv, String text) {        super(text, null);        mFV = fv;        mText = text;    }        public String getText() {	    return mText;    } 	public void actionPerformed(ActionEvent e) {		ConfigureStnCalcExport expDialog = new ConfigureStnCalcExport(mFV, mFV);		expDialog.pack();			// show dialog at expDialog of screen		Rectangle dBounds = expDialog.getBounds();		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();		int x = sd.width/2 - dBounds.width/2;		int y = sd.height/2 - dBounds.height/2;		expDialog.setLocation(x, y);		expDialog.setVisible(true);	}    	public boolean isEnabled() {		return mFV.isStnCalcPresent();	}}
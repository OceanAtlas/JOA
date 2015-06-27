/*
 * $Id: CustomCalcAction.java,v 1.1 2005/09/07 18:52:04 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.event.*;
import javax.swing.*;
import javaoceanatlas.ui.*;

 @SuppressWarnings("serial")
public class CustomStnCalcAction extends AbstractAction {
	protected String mText;
	protected FileViewer mFV;
	protected KeyStroke mAccelerator;
	JFrame mParent;

    public CustomStnCalcAction(JFrame parent, FileViewer fv, String text, KeyStroke ks) {
        super(text, null);
        mParent = parent;
        mText = text;
        mFV = fv;
	    mAccelerator = ks;
	    if (mAccelerator != null)
        	putValue(ACCELERATOR_KEY, mAccelerator);
    }

	public void actionPerformed(ActionEvent e) {
		// Open the calculations dialog
		ConfigCustomStnCalc customDialog = new ConfigCustomStnCalc(mParent, mFV);
		customDialog.pack();
		customDialog.setVisible(true);
    }
    
    public String getText() {
	    return mText;
    } 
    
	public boolean isEnabled() {
		return mFV.isStnCalcPresent();
	}
	
	public KeyStroke getAccelerator() {
		return mAccelerator;
	}
	
	public void setAccelerator(KeyStroke ks) {
		mAccelerator = ks;
        putValue(ACCELERATOR_KEY, mAccelerator);
	}
	
}
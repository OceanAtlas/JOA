/**
 * 
 */
package javaoceanatlas.utility;

/**
 * @author oz
 *
 */

import java.awt.event.*;
import javax.swing.*;
import javaoceanatlas.ui.*;

 @SuppressWarnings("serial")
public class ForecastTSAction extends AbstractAction {
	protected String mText;
	protected FileViewer mFV;
	protected KeyStroke mAccelerator;
	JFrame mParent;

    public ForecastTSAction(JFrame parent, FileViewer fv, String text, KeyStroke ks) {
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
		ConfigTSModel customDialog = new ConfigTSModel(mParent, mFV);
		customDialog.pack();
		customDialog.setVisible(true);
    }
    
    public String getText() {
	    return mText;
    } 
    
	public boolean isEnabled() {
		return true;
	}
	
	public KeyStroke getAccelerator() {
		return mAccelerator;
	}
	
	public void setAccelerator(KeyStroke ks) {
		mAccelerator = ks;
        putValue(ACCELERATOR_KEY, mAccelerator);
	}
}

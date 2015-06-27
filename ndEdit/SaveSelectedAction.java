/*
 * $Id: SaveSelectedAction.java,v 1.10 2005/02/15 18:31:10 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.*;
import ndEdit.*;
import gov.noaa.pmel.eps2.*;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.swing.*;
import java.util.*;

 public class SaveSelectedAction extends NdEditAction implements DialogClient {
   protected int[] mSortKeys = null;
   protected EpicPtrs ptrDB;
   protected int mFormat;

    public SaveSelectedAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent, int format) {
        super(menu, text, icon, vm, parent);
        mFormat = format;
    }
    
    public SaveSelectedAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent, int format) {
        super(menu, text, icon, ks, vm, parent);
        mFormat = format;
    }
    
    public void setFormat(int i) {
    	mFormat = i;
    }
    
    public void doAction() {
    	// need to get the type of data--argo or gtspps
    	String title = mParent.getFrame().getTitle();
    	if (title.toLowerCase().indexOf("argo") >= 0)
    		mFormat = Constants.ARGO_FORMAT;
    	else if (title.toLowerCase().indexOf("gtspp") >= 0)
    		mFormat = Constants.GTSPP_FORMAT;
    	
		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		//save the the selected points to a pointer file
		String prompt = b.getString("kNameOfPtrFile");
		String fileExtension = new String("untitled.ptr");
		if (mFormat == Constants.ARGO_FORMAT) {
			prompt = b.getString("kNameOfInvFile");
			fileExtension = new String("untitled_argoinv.txt");
		}
		else if (mFormat == Constants.GTSPP_FORMAT) {
			prompt = b.getString("kNameOfInvFile");
			fileExtension = new String("untitled_gtsppinv.txt");
		}
		// get the output file section
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir");
		FileDialog f = new FileDialog(fr, prompt, FileDialog.SAVE);
		Rectangle dBounds = f.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		f.setLocation(x, y);
		f.setDirectory(directory);
			
		//if (mCurrOutFileName != null)
			f.setFile(fileExtension);
		//else
		//	f.setFile(this.getTitle() + fileExtension);
		f.show();
		directory = f.getDirectory();
		f.dispose();
			
		if (directory != null && f.getFile() != null) {
			// get the user-defined part of the name
			if (mFormat != Constants.ARGO_FORMAT && mFormat != Constants.GTSPP_FORMAT) {
				// get the sort keys
				ConfigSortOptions config = new ConfigSortOptions(mParent.getFrame(), this);
				config.pack();
				config.setVisible(true);
			
				File mCurrOutFile = new File(directory, f.getFile());
				mParent.writePtrFile(mCurrOutFile, mSortKeys);
			}
			else {
				File invOutFile = new File(directory, f.getFile());
				int pos = invOutFile.getName().indexOf("_");
				String userName = null;
				if (pos > 0) {
					userName = invOutFile.getName().substring(0, pos);
				}
				
				prompt = b.getString("kNameOfArgoLocFile");
				if (mFormat == Constants.ARGO_FORMAT)
					if (userName != null)
						fileExtension = new String(userName + "_argoloc.txt");
					else
						fileExtension = new String("untitled_argoloc.txt");
				else if (mFormat == Constants.GTSPP_FORMAT) {
					if (userName != null)
						fileExtension = new String(userName + "_gtspploc.txt");
					else
						fileExtension = new String("untitled_gtspploc.txt");
				} 
				
				fr = new Frame();
				//directory = System.getProperty("user.dir");
				f = new FileDialog(fr, prompt, FileDialog.SAVE);
				dBounds = f.getBounds();
				sd = Toolkit.getDefaultToolkit().getScreenSize();
				x = sd.width/2 - dBounds.width/2;
				y = sd.height/2 - dBounds.height/2;
				f.setLocation(x, y);
				f.setDirectory(directory);
				f.setFile(fileExtension);
				f.show();
				directory = f.getDirectory();
				f.dispose();
				File invLocFile = new File(directory, f.getFile());
			
				mParent.writeInvFile(invOutFile, invLocFile, mSortKeys);
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		doAction();
	}
	
    
	// OK Button
    public void dialogDismissed(JDialog d) {
    	mSortKeys = ((ConfigSortOptions)d).getSortOrder();	
    }
    
    // Cancel button
    public void dialogCancelled(JDialog d) {}
    
    // something other than the OK button 
    //public void dialogDismissedTwo(Frame d);
    
    // Apply button, OK w/o dismissing the dialog
    public void dialogApply(JDialog d) {}
    
    // Apply button, OK w/o dismissing the dialog
    public void dialogApplyTwo(Object d) {}
    
    // Apply button, OK w/o dismissing the dialog
    public void dialogDismissedTwo(JDialog d) {}
	
	public boolean isEnabled() {
		PointerCollectionGroup pc = getPointerCollection();
		return (pc != null && pc.isSomethingSelected());
	}
}

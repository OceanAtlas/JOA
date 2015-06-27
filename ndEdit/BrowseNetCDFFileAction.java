/*
 * $Id: BrowseNetCDFFileAction.java,v 1.4 2005/02/15 18:31:08 oz Exp $
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
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.border.*;
import ndEdit.*;
import ndEdit.ncBrowse.*;
import java.io.*;

 public class BrowseNetCDFFileAction extends NdEditAction {
	private NcFile ncFile = null;
  
    public BrowseNetCDFFileAction(String menu, String text, Icon icon, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, vm, parent);
    }
    
    public BrowseNetCDFFileAction(String menu, String text, Icon icon, KeyStroke ks, ViewManager vm, NdEdit parent) {
        super(menu, text, icon, ks, vm, parent);
    }

	public void actionPerformed(ActionEvent e) {
		doAction();
	}

	public void doAction() {
    	// get a filename
    	FilenameFilter filter = new FilenameFilter() {
    		public boolean accept(File dir, String name) {
    			if (name.endsWith(".nc") || name.endsWith(".cdf"))
    				return true;
    			else
    				return false;
    		}
    	};
    	String directory;
	    Frame fr = new Frame();
	    FileDialog f = new FileDialog(fr, "Browse NetCDF File", FileDialog.LOAD);
	    f.setFilenameFilter(filter);
	    f.setVisible(true);
	    directory = f.getDirectory();
	    String fs = f.getFile();
	    if (directory != null && fs != null) {
	    	File file = new File(directory, fs);
			try{
				ncFile = new LocalNcFile(file);
			} 
			catch (IOException e) {
				e.printStackTrace();
				System.out.println(e + ": new NcFile");
				return;
			}
    
			File nf = new File(directory, fs);
			try {
				NdEditView tblView = new NdEditView(mParent);
				tblView.setNcFile(ncFile);
				tblView.setVisible(true);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
        }
	    f.dispose();
	}
}

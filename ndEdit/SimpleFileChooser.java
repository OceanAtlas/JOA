/*
 * $Id: SimpleFileChooser.java,v 1.3 2005/02/15 18:31:10 oz Exp $
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
// SimpleFileChooser.java
// Just a simple file chooser example to see what it takes to make one
// of these work.
//
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class SimpleFileChooser {
  File fileChosen;
  String directory;
  public SimpleFileChooser() {
	FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (name.endsWith(".ptr"))
				return true;
			else
				return false;
		}
	};
	
    Frame fr = new Frame();
    FileDialog f = new FileDialog(fr, "Open EPIC Pointer File:", FileDialog.LOAD);
    f.setFilenameFilter(filter);
    f.show();
    directory = f.getDirectory();
    f.dispose();
    if (directory != null && f.getFile() != null) {
		fileChosen = new File(directory, f.getFile());
    }
    else { 
   		fileChosen = null;
    }
  }
  
  public File getFileChosen() {
    return fileChosen;
  }
}


class SimpleFileFilter extends javax.swing.filechooser.FileFilter {

  String[] extensions;
  String description;
  private SimpleFileChooser lnkSimpleFileChooser;

  public SimpleFileFilter(String ext) {
    this (new String[] {ext}, null);
  }

  public SimpleFileFilter(String[] exts, String descr) {
  	super();
    // clone and lowercase the extensions
    extensions = new String[exts.length];
    for (int i = exts.length - 1; i >= 0; i--) {
      extensions[i] = exts[i].toLowerCase();
    }
    // make sure we have a valid (if simplistic) description
    description = (descr == null ? exts[0] + " files" : descr);
  }

  public boolean accept(File f) {
    // we always allow directories, regardless of their extension
    if (f.isDirectory()) { return true; }

    // ok, it's a regular file so check the extension
    String name = f.getName().toLowerCase();
    for (int i = extensions.length - 1; i >= 0; i--) {
      if (name.endsWith(extensions[i])) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() { return description; }
}

/*
 * $Id: SaveSelectedPtrsAction.java,v 1.6 2005/06/27 23:28:42 oz Exp $
 *
 */

package javaoceanatlas.io;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import ndEdit.*;
import gov.noaa.pmel.eps2.*;
import javaoceanatlas.*;
import javaoceanatlas.ui.*;
import javaoceanatlas.utility.JOAFormulas;
import javaoceanatlas.resources.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class SaveSelectedPtrsAction extends AbstractAction implements ndEdit.DialogClient {
	protected int[] mSortKeys = null;
	protected EpicPtrs ptrDB;
	protected NdEdit mParent;
	protected String mText;
	protected String mTitle;

	public SaveSelectedPtrsAction(String text, Icon icon, NdEdit parent) {
		super(text, icon);
		mText = text;
		mParent = parent;
	}

	public String getText() {
		return mText;
	}

	public void actionPerformed(ActionEvent e) {
		// save the the selected points to a temporary pointer file
		String prompt = "untitled";
		String title = mParent.getFrame().getTitle();
		String[] tokens = title.split(":");
		if (tokens.length > 0) {
			prompt = tokens[tokens.length - 1];
			String[] tokens2 = title.split(".");
			if (tokens2.length > 0) {
				prompt = tokens2[tokens2.length - 1];
			}
		}
		prompt = JOAFormulas.stripExtensions(prompt);
		prompt += " (subset)";

		ConfigSortOptions config = new ConfigSortOptions(mParent.getFrame(), this, true, prompt);
		config.pack();
		config.setVisible(true);
	}

	// OK Button
	public void dialogDismissed(JDialog d) {
		String directory = EPS_Util.getTempDir();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
		String ptrsName = "temp_" + sdf.format(new Date()) + ".ptr";
		File tFile = new File(directory, ptrsName);

		mSortKeys = ((ConfigSortOptions) d).getSortOrder();
		mTitle = ((ConfigSortOptions) d).getTitle();

		// write the temp ptr file
		mParent.writePtrFile(tFile, mSortKeys);

		// now open this file up in JOA
		int[] sortKeys = { 0, 0, 0, 0 };
		try {
			EpicPtrFactory ptrfact = new EpicPtrFactory();
			EpicPtrs prtDB = ptrfact.createEpicPtrs(tFile, sortKeys, false, true);
			EPSDbase tstDB = new EPSDbase(prtDB.iterator(), true);

			// turn this dbase into a JOA's internal data structures
			try {
				Frame fr = new Frame();
				FileViewer fv = new FileViewer(fr, tstDB, mTitle);
				if (PowerOceanAtlas.getInstance() != null) {
					PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
				}
				fv.setFile(directory, tFile.getParent(), mTitle);
				fv.pack();
				fv.setVisible(true);
				fv.setSavedState(JOAConstants.ISCOLLECTION, null);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		catch (Exception exx) {
		}
	}

	public boolean isEnabled() {
		PointerCollectionGroup pc = mParent.getPointerCollection();
		return (pc != null && pc.isSomethingSelected());
	}

	// Cancel button
	public void dialogCancelled(JDialog d) {
	}

	// something other than the OK button
	// public void dialogDismissedTwo(Frame d);

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog d) {
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApplyTwo(Object d) {
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogDismissedTwo(JDialog d) {
	}
}

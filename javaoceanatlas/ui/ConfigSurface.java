/*
 * $Id: ConfigSurface.java,v 1.3 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;

@SuppressWarnings("serial")
public class ConfigSurface extends JOAJDialog implements ActionListener, ButtonMaintainer, ListSelectionListener,
    DialogClient {
	protected FileViewer mFileViewer = null;
	protected ResourceBundle b = null;
	protected JOAJList mSurfaceList = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mDeleteButton = null;
	protected JOAJButton mEditButton = null;
	protected JOAJButton mNewButton = null;
	protected int mSelectedSurface = -99;
	protected int mOldSelectedSurface = -99;
	protected boolean mEatValueChanged = false;
	protected NewInterpolationSurface mSurface = null;
	protected JPanel mUpperContents = null;
	private Timer timer = new Timer();
	protected DialogClient mFrame = null;
	protected JOAWindow mParent;

	public ConfigSurface(JOAWindow par, FileViewer fv) {
		super(par, "Surface Manager", false);
		mParent = par;
		mFileViewer = fv;
		init();
	}

	public void init() {
		mFrame = this;
		b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		Container contents = getContentPane();
		contents.setLayout(new BorderLayout(5, 5));

		mUpperContents = new JPanel();
		mUpperContents.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		// Controls panels
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout(5, 5));

		// Surface list
		JPanel palPanel = new JPanel();
		palPanel.setLayout(new BorderLayout(5, 5));
		JOAJLabel l1 = new JOAJLabel(b.getString("kInterpolationSurfaces"), JOAJLabel.LEFT);
		palPanel.add(l1, "North");
		Vector<String> listData = JOAFormulas.getSurfaceList();
		mSurfaceList = new JOAJList(listData);
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					SurfaceEditor surfaceDialog = new SurfaceEditor(mParent, mSurface, mFrame);

					// show dialog at center of screen
					surfaceDialog.setSize(600, 600);
					Rectangle dBounds = surfaceDialog.getBounds();
					Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
					int x = sd.width / 2 - dBounds.width / 2;
					int y = sd.height / 2 - dBounds.height / 2;
					surfaceDialog.setLocation(x, y);
					// surfaceDialog.pack();
					// if (JOAConstants.ISMAC)
					// surfaceDialog.setSize(500, 400);
					// else
					surfaceDialog.setVisible(true);
				}
			}
		};
		mSurfaceList.addMouseListener(mouseListener);

		mSurfaceList.setVisibleRowCount(7);
		mSurfaceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScroller = new JScrollPane(mSurfaceList);
		palPanel.add(listScroller, "Center");
		mSurfaceList.addListSelectionListener(this);

		// buttons
		JPanel buttPanel = new JPanel();
		buttPanel.setLayout(new GridLayout(1, 3, 5, 5));
		mEditButton = new JOAJButton(b.getString("kEdit"));
		mNewButton = new JOAJButton(b.getString("kNew"));
		mDeleteButton = new JOAJButton(b.getString("kDelete"));
		mEditButton.setActionCommand("edit");
		mNewButton.setActionCommand("new");
		mDeleteButton.setActionCommand("delete");
		mEditButton.addActionListener(this);
		mNewButton.addActionListener(this);
		mDeleteButton.addActionListener(this);
		buttPanel.add(mEditButton);
		buttPanel.add(mNewButton);
		buttPanel.add(mDeleteButton);
		palPanel.add("South", buttPanel);

		mUpperContents.add(palPanel);
		contents.add("North", mUpperContents);

		// lower Panel
		mOKBtn = new JOAJButton(b.getString("kClose"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		dlgBtnsPanel.add(mOKBtn);
		dlgBtnsInset.add(dlgBtnsPanel);
		contents.add("South", new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));

		mOKBtn.addActionListener(this);

    runTimer();

		// show dialog at center of screen
		this.pack();
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("ok")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("edit")) {
			// edit existing surface
			SurfaceEditor surfaceDialog = new SurfaceEditor(mParent, mSurface, this);

			// surfaceDialog.pack();
			// show dialog at center of screen
			surfaceDialog.setSize(600, 600);
			Rectangle dBounds = surfaceDialog.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			surfaceDialog.setLocation(x, y);
			// surfaceDialog.pack();
			// if (JOAConstants.ISMAC)
			// surfaceDialog.setSize(500, 400);
			// else
			surfaceDialog.setVisible(true);
		}
		else if (cmd.equals("new")) {
			// create a new surface
			SurfaceEditor surfaceDialog = new SurfaceEditor(mParent, this);

			// surfaceDialog.pack();
			// show dialog at center of screen
			surfaceDialog.setSize(600, 600);
			Rectangle dBounds = surfaceDialog.getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			surfaceDialog.setLocation(x, y);
			// surfaceDialog.pack();
			// if (JOAConstants.ISMAC)
			// surfaceDialog.setSize(500, 400);
			// else
			surfaceDialog.setVisible(true);
		}
		else if (cmd.equals("delete")) {
			// get a File object for selected cbar
			String surfName = (String) mSurfaceList.getSelectedValue();
			String dir = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
			File nf = new File(dir, surfName);
			if (nf != null) {
				nf.delete();
				cleanUpAfterDelete();
			}
		}
	}

	public void maintainButtons() {
		if (mSurface != null) {
			mDeleteButton.setEnabled(true);
			mEditButton.setEnabled(true);
		}
		else {
			mDeleteButton.setEnabled(false);
			mEditButton.setEnabled(false);
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
		int tempSelectedSurface = mSurfaceList.getSelectedIndex();
		String newSurfaceName = (String) mSurfaceList.getSelectedValue();
		if (newSurfaceName == null || tempSelectedSurface == mSelectedSurface)
			return;
		mSelectedSurface = tempSelectedSurface;
		// read the surface from disk
		try {
			mSurface = JOAFormulas.getSurface(newSurfaceName);
		}
		catch (Exception ex) {
			mSurface = null;
		}
	}

	protected void cleanUpAfterDelete() {
		// redo the list
		Vector<String> listData = JOAFormulas.getSurfaceList();
		mSurfaceList.setListData(listData);
		mSurface = null;
	}

	// OK Button
	public void dialogDismissed(JDialog d) {
		cleanUpAfterDelete();
	}

	// Cancel button
	public void dialogCancelled(JDialog d) {
		cleanUpAfterDelete();
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog d) {
		cleanUpAfterDelete();
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog d) {
		cleanUpAfterDelete();
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApplyTwo(Object d) {
		cleanUpAfterDelete();
	}

}

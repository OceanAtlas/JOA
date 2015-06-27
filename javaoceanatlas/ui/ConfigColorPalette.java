/*
 * $Id: ConfigColorPalette.java,v 1.3 2005/09/07 18:49:31 oz Exp $
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
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigColorPalette extends JOAJDialog implements ActionListener, ButtonMaintainer, ListSelectionListener {
	protected ColorPalettePanel mColorPalette = null;
	protected ResourceBundle b = null;
	protected JOAJList mPaletteList = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mPickButton = null;
	protected JOAJButton mBlendButton = null;
	protected JOAJButton mSaveButton = null;
	protected JOAJButton mNewButton = null;
	protected int mSelectedPalette = -99;
	protected int mOldSelectedPalette = -99;
	protected boolean mEatValueChanged = false;
	private Timer timer = new Timer();

	public ConfigColorPalette(JOAWindow par, ColorPalettePanel inPalette) {
		super(par, "Color Palette Editor", false);
		mColorPalette = inPalette;
		init();
	}

	public ConfigColorPalette(JOAWindow par) {
		super(par, "Color Palette Editor", false);
		init();
	}

	public void init() {
		b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		Container contents = getContentPane();
		contents.setLayout(new BorderLayout(5, 5));

		JPanel upperContents = new JPanel();
		upperContents.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		if (mColorPalette == null)
			mColorPalette = new ColorPalettePanel();
		upperContents.add(mColorPalette);

		// Controls panels
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout(5, 5));

		// color palette list
		JPanel palPanel = new JPanel();
		palPanel.setLayout(new BorderLayout(5, 5));
		JOAJLabel l1 = new JOAJLabel(b.getString("kColorPalettes"), JOAJLabel.LEFT);
		palPanel.add(l1, "North");
		Vector<String> listData = getPaletteList();
		mPaletteList = new JOAJList(listData);
		mPaletteList.setVisibleRowCount(4);
		mPaletteList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScroller = new JScrollPane(mPaletteList);
		palPanel.add(listScroller, "Center");
		mPaletteList.addListSelectionListener(this);

		// buttons
		JPanel buttPanel = new JPanel();
		buttPanel.setLayout(new GridLayout(2, 2, 5, 5));
		mPickButton = new JOAJButton(b.getString("kPick"));
		mBlendButton = new JOAJButton(b.getString("kBlend"));
		mSaveButton = new JOAJButton(b.getString("kSave"));
		mNewButton = new JOAJButton(b.getString("kNew"));
		mPickButton.setActionCommand("pick");
		mBlendButton.setActionCommand("blend");
		mSaveButton.setActionCommand("save");
		mNewButton.setActionCommand("new");
		mPickButton.addActionListener(this);
		mBlendButton.addActionListener(this);
		mSaveButton.addActionListener(this);
		mNewButton.addActionListener(this);
		buttPanel.add(mPickButton);
		buttPanel.add(mSaveButton);
		buttPanel.add(mBlendButton);
		buttPanel.add(mNewButton);

		controlPanel.add("North", palPanel);
		controlPanel.add("South", buttPanel);
		upperContents.add(controlPanel);
		contents.add("North", upperContents);

		// lower Panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);
		contents.add("South", new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));

		mOKBtn.addActionListener(this);
		mCancelButton.addActionListener(this);

		runTimer();
		this.pack();

		// show dialog at center of screen
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

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			if (mColorPalette.isDirty()) {
				int response = JOptionPane.showConfirmDialog(this, "Save changes to this color palette?");
				switch (response) {
					case JOptionPane.CANCEL_OPTION:
						return;
					case JOptionPane.YES_OPTION:
						mColorPalette.save();
				}
				// fall through on yes and no
			}
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("blend")) {
			mColorPalette.blend(mColorPalette.getSelStart(), mColorPalette.getSelEnd());
		}
		else if (cmd.equals("pick")) {
			mColorPalette.pick();
		}
		else if (cmd.equals("save")) {
			mColorPalette.save();
		}
		else if (cmd.equals("new")) {
			if (mColorPalette.isDirty()) {
				int response = JOptionPane.showConfirmDialog(this, "Save changes to this color palette?");
				switch (response) {
					case JOptionPane.CANCEL_OPTION:
						return;
					case JOptionPane.YES_OPTION:
						mColorPalette.save();
				}
				// fall through on yes and no
			}
			mColorPalette.setNewColorPalette();
			ListSelectionModel lsm = mPaletteList.getSelectionModel();
			lsm.clearSelection();
		}
	}

	public void maintainButtons() {
		if (mColorPalette.isDirty())
			mSaveButton.setEnabled(true);
		else
			mSaveButton.setEnabled(false);

		if (mColorPalette.getSelEnd() > mColorPalette.getSelStart())
			mBlendButton.setEnabled(true);
		else
			mBlendButton.setEnabled(false);

		if (mColorPalette.getSelStart() != -99)
			mPickButton.setEnabled(true);
		else
			mPickButton.setEnabled(false);
	}

	public void valueChanged(ListSelectionEvent evt) {
		int tempSelectedPalette = mPaletteList.getSelectedIndex();
		String newPalName = (String) mPaletteList.getSelectedValue();
		if (mEatValueChanged) {
			mEatValueChanged = false;
			return;
		}
		if (newPalName == null || tempSelectedPalette == mSelectedPalette)
			return;
		mSelectedPalette = tempSelectedPalette;

		if (mColorPalette.isDirty()) {
			int response = JOptionPane.showConfirmDialog(this, "Save changes to this color palette?");
			switch (response) {
				case JOptionPane.CANCEL_OPTION:
					// unselect the current list selection
					if (mOldSelectedPalette >= 0) {
						mEatValueChanged = true;
						mPaletteList.setSelectedIndex(mOldSelectedPalette);
						mSelectedPalette = mOldSelectedPalette;
					}
					else {
						ListSelectionModel lsm = mPaletteList.getSelectionModel();
						lsm.clearSelection();
						mEatValueChanged = true;
					}
					return;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.YES_OPTION:
					mColorPalette.save();
			}
			// fall through on yes and no
		}

		// read the palette from disk
		try {
			ColorPalette newPal = JOAFormulas.readPalette(newPalName);
			mColorPalette.setNewPalette(newPal, newPalName);
		}
		catch (Exception ex) {
			System.out.println("An error occured reading a palette from a file");
		}
		mOldSelectedPalette = mSelectedPalette;
	}

	public Vector<String> getPaletteList() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("_pal.xml"))
					return true;
				else
					return false;
			}
		};
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		Vector<String> returnList = new Vector<String>();
		File dir = new File(directory);

		String[] files = dir.list(filter);
		for (int i = 0; i < files.length; i++) {
			returnList.addElement(files[i]);
		}
		return returnList;
	}
}

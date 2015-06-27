package javaoceanatlas.utility;

/*
 * $Id: CriterionPanel.java,v 1.9 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

import java.awt.event.ActionListener;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import javaoceanatlas.utility.ButtonMaintainer;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javaoceanatlas.ui.widgets.SmallIconButton;
import javax.swing.ImageIcon;
import java.awt.event.ItemEvent;
import javaoceanatlas.ui.widgets.JOAJComboBox;

/**
 * <code>CriterionPanel</code> UI for an individual search criterion.
 * 
 * @author oz
 * @version 1.0
 */

@SuppressWarnings("serial")
public class MergeCriterionPanel extends JPanel implements ButtonMaintainer, ActionListener, ItemListener {
	private JComboBox mParamCombo;
	private Vector<String> mParamList;
	private boolean mIsFirst = false;
	private MergeCriteriaContainer mParent;
	private SmallIconButton mRemoveConditionButton;
	private SmallIconButton mAddConditionButton;
	private Timer timer = new Timer();
	private boolean mChangedFlag = false;
	private boolean mIgnore = false;
	private boolean mEnabled = true;
	private JOAJComboBox mCriterion;
	private int mInitialCriterion = 0;

	public MergeCriterionPanel(Vector<String> params, boolean isfirst, MergeCriteriaContainer parent) {
		mIsFirst = isfirst;
		mParamList = params;
		mParent = parent;
		init();
	}

	public MergeCriterionPanel(Vector<String> params, boolean isfirst, MergeCriteriaContainer parent, int intialMatchCrit) {
		mIsFirst = isfirst;
		mParamList = params;
		mParent = parent;
		mInitialCriterion = intialMatchCrit;
		init();
	}

	public void init() {
		try {
			mRemoveConditionButton = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas")
			    .getResource("images/bigminus.gif")));
			mAddConditionButton = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas")
			    .getResource("images/bigplus.gif")));
		}
		catch (Exception ex) {
			ex.printStackTrace();
    	System.out.println("MergeCriterionPanel:init");
		}

		Vector<String> listData = new Vector<String>();
		listData.add("Section");
		listData.add("Station");
		listData.add("Cast #");
		listData.add("Bottle #");
		listData.add("Sample #");
		listData.add("Bottle Seq #");
		listData.add("Pressure");
		mCriterion = new JOAJComboBox(listData);
		mCriterion.setSelectedIndex(mInitialCriterion);

		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		this.add(mCriterion);
		JLabel l2 = new JLabel("matches");
		this.add(l2);

		if (mParamList == null) {
			mParamList = new Vector<String>();
			mParamList.addElement("not defined");
		}
		
		// filter out units
		Vector<String> strippedParams = new Vector<String>();
		for (String ps : mParamList) {
			String[] tokens = ps.split("[:]");
			String sps = JOAFormulas.returnMiddleTruncatedString(tokens[0], 10);
			strippedParams.add(sps);
		}
		
		mParamCombo = new JComboBox(strippedParams);
		mParamCombo.addItemListener(this);
		this.add(mParamCombo);

		// the add/remove button panel
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1, 2, 5, 5));
		btnPanel.add(mRemoveConditionButton);
		btnPanel.add(mAddConditionButton);
		this.add(btnPanel);
		mRemoveConditionButton.addActionListener(this);
		mAddConditionButton.addActionListener(this);
		mRemoveConditionButton.setActionCommand("removeme");
		mAddConditionButton.setActionCommand("addnew");

		// start a button maintainer
		runTimer();
	}

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

	public void closeMe() {
		timer.cancel();
	}

	public void setEnabled(boolean b) {
		mEnabled = b;
		mCriterion.setEnabled(b);
		mParamCombo.setEnabled(b);
		if (!mIsFirst) {
			mRemoveConditionButton.setEnabled(b);
		}
		mAddConditionButton.setEnabled(b);
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setParams(Vector<String> params) {
		mIgnore = true;
		mParamList = params;
		mParamCombo.removeAllItems();
		
		// filter out units
		Vector<String> strippedParams = new Vector<String>();
		for (String ps : mParamList) {
			String[] tokens = ps.split("[:]");
			String sps = JOAFormulas.returnMiddleTruncatedString(tokens[0], 10);
			strippedParams.add(sps);
		}
		
		for (int i = 0; i < strippedParams.size(); i++) {
			mChangedFlag = false;
			mParamCombo.addItem(strippedParams.elementAt(i));
		}
		mParamCombo.invalidate();
		mParamCombo.validate();
		mIgnore = false;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("removeme")) {
			mRemoveConditionButton.setSelected(false);

			// tell the CriteriaContainer to remove this panel
			mParent.removeCriterion(this);
			mChangedFlag = true;
		}
		else if (cmd.equals("addnew")) {
			mAddConditionButton.setSelected(false);

			// tell the CriteriaContainer to add a new Criterion Panel
			mParent.addCriterion(this);
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) evt.getSource();
			if (cb == mParamCombo && evt.getStateChange() == ItemEvent.SELECTED && !mIgnore) {
				mChangedFlag = true;
			}
		}
	}

	public boolean isCriterionComplete() {
		return true;
	}

	public boolean isChanged() {
		return mChangedFlag;
	}

	public int getParam() {
		return mParamCombo.getSelectedIndex();
	}

	public int getCriterion() {
		return mCriterion.getSelectedIndex();
	}

	public void maintainButtons() {
		if (!mIsFirst) {
			mRemoveConditionButton.setEnabled(true);
		}
		else {
			mRemoveConditionButton.setEnabled(false);
		}
	}
}

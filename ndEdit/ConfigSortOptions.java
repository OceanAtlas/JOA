/*
 * $Id: ConfigSortOptions.java,v 1.6 2005/02/15 18:31:08 oz Exp $
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import gov.noaa.pmel.eps2.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigSortOptions extends JDialog implements ActionListener, ItemListener {
	private DialogClient mClient = null;
	private JCheckBox mLatSort = null;
	private JCheckBox mLonSort = null;
	private JCheckBox mTimeSort = null;
	private JCheckBox mDepthSort = null;
	private JRadioButton mLatAsc = null;
	private JRadioButton mLatDsc = null;
	private JRadioButton mLonAsc = null;
	private JRadioButton mLonDsc = null;
	private JRadioButton mTimeAsc = null;
	private JRadioButton mTimeDsc = null;
	private JRadioButton mDepthAsc = null;
	private JRadioButton mDepthDsc = null;
	private JComboBox mLatSortOrder = null;
	private JComboBox mLonSortOrder = null;
	private JComboBox mTimeSortOrder = null;
	private JComboBox mDepthSortOrder = null;
	private JButton mOKBtn = null;
	private JButton mCancelButton = null;
	private JLabel latLbl;
	private JLabel lonLbl;
	private JLabel depthLbl;
	private JLabel timeLbl;
	private JLabel sortPreview;
	protected static final int LAT = 0;
	protected static final int LON = 1;
	protected static final int Z = 2;
	protected static final int T = 3;
	protected static final int ASC = 4;
	protected static final int DSC = 5;
	private boolean inited = false;
	private boolean mPrompt = false;
	private JLabel label1 = new JLabel();
	private JTextField nameField = new JTextField();
	private JTextField levelField = new JTextField();
	private String mDefStr;

	public ConfigSortOptions(JFrame par, DialogClient client) {
		this(par, client, false, "");
	}

	public ConfigSortOptions(JFrame par, DialogClient client, boolean prompt, String defStr) {
		super(par, "Configure Sort Options", true);
		mClient = client;
		mPrompt = prompt;
		mDefStr = defStr;
		init();
	}

	protected void dialogInit() {
		super.dialogInit();

		JLayeredPane layeredPane = getLayeredPane();
		layeredPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
		    "close-it");
		layeredPane.getActionMap().put("close-it", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
	}

	protected void setUIStyle() {
		latLbl.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		latLbl.setForeground(java.awt.Color.black);

		lonLbl.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		lonLbl.setForeground(java.awt.Color.black);

		depthLbl.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		depthLbl.setForeground(java.awt.Color.black);

		timeLbl.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		timeLbl.setForeground(java.awt.Color.black);

		sortPreview.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		sortPreview.setForeground(java.awt.Color.black);

		mLatSort.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mLonSort.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mTimeSort.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mDepthSort.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mLatAsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mLatDsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mLonAsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mLonDsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mTimeAsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mTimeDsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mDepthAsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		mDepthDsc.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("ndEdit.NdEditResources");
		latLbl = new JLabel(b.getString("kSortOrder"));
		lonLbl = new JLabel(b.getString("kSortOrder"));
		depthLbl = new JLabel(b.getString("kSortOrder"));
		timeLbl = new JLabel(b.getString("kSortOrder"));

		Container contents = this.getContentPane();
		// contents.setBackground(new Color(200, 200, 200));
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanelholder = new JPanel();
		// mainPanelholder.setBackground(new Color(200, 200, 200));
		mainPanelholder.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 5));
		JPanel mainPanel = new JPanel();
		// mainPanel.setBackground(new Color(200, 200, 200));
		mainPanel.setLayout(new GridLayout(1, 4, 5, 1));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kPointerSort"));
		tb.setTitleFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		tb.setTitleColor(java.awt.Color.black);
		mainPanelholder.setBorder(tb);

		// option for getting data just at one level
		JPanel levelOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		levelOptions.add(new JLabel("Filter to depth level:"));
		
		tb = BorderFactory.createTitledBorder("Level Filter");
		tb.setTitleFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		tb.setTitleColor(java.awt.Color.black);
		levelOptions.setBorder(tb);
		levelField.setColumns(5);
		levelOptions.add(nameField);

		Vector<String> sortOrders = new Vector<String>();
		sortOrders.addElement("1");
		sortOrders.addElement("2");
		sortOrders.addElement("3");
		sortOrders.addElement("4");

		// latitude panel
		JPanel latPanel = new JPanel();
		// latPanel.setBackground(new Color(200, 200, 200));
		latPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		mLatSort = new JCheckBox(b.getString("kLatitude"));
		mLatSort.setActionCommand("lat");
		mLatSort.addActionListener(this);
		latPanel.add(mLatSort);
		mLatAsc = new JRadioButton(b.getString("kAscending"), true);
		mLatDsc = new JRadioButton(b.getString("kDescending"));
		mLatAsc.addActionListener(this);
		mLatDsc.addActionListener(this);
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(mLatAsc);
		bg1.add(mLatDsc);

		JPanel latLine1 = new JPanel();
		// latLine1.setBackground(new Color(200, 200, 200));
		latLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		latLine1.add(new JLabel("    "));
		latLine1.add(mLatAsc);
		latPanel.add(latLine1);

		JPanel latLine2 = new JPanel();
		// latLine2.setBackground(new Color(200, 200, 200));
		latLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		latLine2.add(new JLabel("    "));
		latLine2.add(mLatDsc);
		latPanel.add(latLine2);

		JPanel latLine3 = new JPanel();
		// latLine3.setBackground(new Color(200, 200, 200));
		latLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mLatSortOrder = new JComboBox(sortOrders);
		mLatSortOrder.addItemListener(this);
		mLatSortOrder.setSelectedItem("1");
		latLine3.add(new JLabel("    "));
		latLine3.add(latLbl);
		latLine3.add(mLatSortOrder);
		latPanel.add(latLine3);
		mainPanel.add(latPanel);

		// longitude panel
		JPanel lonPanel = new JPanel();
		// lonPanel.setBackground(new Color(200, 200, 200));
		lonPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		mLonSort = new JCheckBox(b.getString("kLongitude"));
		mLonSort.setActionCommand("lon");
		mLonSort.addActionListener(this);
		lonPanel.add(mLonSort);
		mLonAsc = new JRadioButton(b.getString("kAscending"), true);
		mLonDsc = new JRadioButton(b.getString("kDescending"));
		mLonAsc.addActionListener(this);
		mLonDsc.addActionListener(this);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(mLonAsc);
		bg2.add(mLonDsc);

		JPanel lonLine1 = new JPanel();
		// lonLine1.setBackground(new Color(200, 200, 200));
		lonLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		lonLine1.add(new JLabel("    "));
		lonLine1.add(mLonAsc);
		lonPanel.add(lonLine1);

		JPanel lonLine2 = new JPanel();
		// lonLine2.setBackground(new Color(200, 200, 200));
		lonLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		lonLine2.add(new JLabel("    "));
		lonLine2.add(mLonDsc);
		lonPanel.add(lonLine2);

		JPanel lonLine3 = new JPanel();
		// lonLine3.setBackground(new Color(200, 200, 200));
		lonLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mLonSortOrder = new JComboBox(sortOrders);
		mLonSortOrder.addItemListener(this);
		mLonSortOrder.setSelectedItem("2");
		lonLine3.add(new JLabel("    "));
		lonLine3.add(lonLbl);
		lonLine3.add(mLonSortOrder);
		lonPanel.add(lonLine3);
		mainPanel.add(lonPanel);

		// depth panel
		JPanel depthPanel = new JPanel();
		// depthPanel.setBackground(new Color(200, 200, 200));
		depthPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		mDepthSort = new JCheckBox(b.getString("kDepth"));
		mDepthSort.setActionCommand("depth");
		mDepthSort.addActionListener(this);
		depthPanel.add(mDepthSort);
		mDepthAsc = new JRadioButton(b.getString("kAscending"), true);
		mDepthDsc = new JRadioButton(b.getString("kDescending"));
		mDepthAsc.addActionListener(this);
		mDepthDsc.addActionListener(this);
		ButtonGroup bg3 = new ButtonGroup();
		bg3.add(mDepthAsc);
		bg3.add(mDepthDsc);

		JPanel zLine1 = new JPanel();
		// zLine1.setBackground(new Color(200, 200, 200));
		zLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		zLine1.add(new JLabel("    "));
		zLine1.add(mDepthAsc);
		depthPanel.add(zLine1);

		JPanel zLine2 = new JPanel();
		// zLine2.setBackground(new Color(200, 200, 200));
		zLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		zLine2.add(new JLabel("    "));
		zLine2.add(mDepthDsc);
		depthPanel.add(zLine2);

		JPanel zLine3 = new JPanel();
		// zLine3.setBackground(new Color(200, 200, 200));
		zLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mDepthSortOrder = new JComboBox(sortOrders);
		mDepthSortOrder.addItemListener(this);
		mDepthSortOrder.setSelectedItem("3");
		zLine3.add(new JLabel("    "));
		zLine3.add(depthLbl);
		zLine3.add(mDepthSortOrder);
		depthPanel.add(zLine3);
		mainPanel.add(depthPanel);

		// time panel
		JPanel timePanel = new JPanel();
		// timePanel.setBackground(new Color(200, 200, 200));
		timePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		mTimeSort = new JCheckBox(b.getString("kTime"));
		mTimeSort.setActionCommand("time");
		mTimeSort.addActionListener(this);
		timePanel.add(mTimeSort);
		mTimeAsc = new JRadioButton(b.getString("kAscending"), true);
		mTimeDsc = new JRadioButton(b.getString("kDescending"));
		mTimeAsc.addActionListener(this);
		mTimeDsc.addActionListener(this);
		ButtonGroup bg4 = new ButtonGroup();
		bg4.add(mTimeAsc);
		bg4.add(mTimeDsc);

		JPanel tLine1 = new JPanel();
		// tLine1.setBackground(new Color(200, 200, 200));
		tLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tLine1.add(new JLabel("    "));
		tLine1.add(mTimeAsc);
		timePanel.add(tLine1);

		JPanel tLine2 = new JPanel();
		// tLine2.setBackground(new Color(200, 200, 200));
		tLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tLine2.add(new JLabel("    "));
		tLine2.add(mTimeDsc);
		timePanel.add(tLine2);

		JPanel tLine3 = new JPanel();
		// tLine3.setBackground(new Color(200, 200, 200));
		tLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		mTimeSortOrder = new JComboBox(sortOrders);
		mTimeSortOrder.addItemListener(this);
		mTimeSortOrder.setSelectedItem("4");
		tLine3.add(new JLabel("    "));
		tLine3.add(timeLbl);
		tLine3.add(mTimeSortOrder);
		timePanel.add(tLine3);
		mainPanel.add(timePanel);

		// sort preview
		JPanel previewInset = new JPanel();
		// previewInset.setBackground(new Color(200, 200, 200));
		previewInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		sortPreview = new JLabel("No Sorting Specified");
		previewInset.add(sortPreview);
		mainPanelholder.add(previewInset);

		// the name panel
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		label1.setText(b.getString("kDatasetName"));
		labelPanel.add(label1);
		nameField.setColumns(20);
		if (mDefStr != null && mDefStr.length() > 0) {
			nameField.setText(mDefStr);
		}
		else {
			nameField.setText("Untitled");
		}
		labelPanel.add(nameField);

		// set font and color style
		setUIStyle();

		// lower panel
		mOKBtn = new JButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		mCancelButton = new JButton(b.getString("kCancel"));
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		// dlgBtnsInset.setBackground(new Color(200, 200, 200));
		// dlgBtnsPanel.setBackground(new Color(200, 200, 200));
		// mOKBtn.setBackground(new Color(200, 200, 200));
		// mCancelButton.setBackground(new Color(200, 200, 200));
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		if (Constants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mOKBtn.addActionListener(this);
		mCancelButton.addActionListener(this);
		mainPanelholder.add(new NPixelBorder(mainPanel, 0, 0, 0, 0));
		mainPanelholder.add(new NPixelBorder(levelOptions, 0, 0, 0, 0));
		if (mPrompt) {
			JPanel mpHolder = new JPanel();
			mpHolder.setLayout(new BorderLayout(5, 5));
			mpHolder.add(mainPanelholder, "Center");
			mpHolder.add(labelPanel, "South");
			contents.add(new NPixelBorder(mpHolder, 10, 10, 10, 10), "Center");
		}
		else
			contents.add(new NPixelBorder(mainPanelholder, 10, 10, 10, 10), "Center");
		contents.add(new NPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");

		setLatState(false);
		setLonState(false);
		setDepthState(false);
		setTimeState(false);

		this.pack();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
		inited = true;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			this.setVisible(false);
			mClient.dialogDismissed(this);
			this.dispose();
		}
		else if (cmd.equals("lat")) {
			setLatState(mLatSort.isSelected());
		}
		else if (cmd.equals("lon")) {
			setLonState(mLonSort.isSelected());
		}
		else if (cmd.equals("depth")) {
			setDepthState(mDepthSort.isSelected());
		}
		else if (cmd.equals("time")) {
			setTimeState(mTimeSort.isSelected());
		}

		// make a sort preview
		displaySortPreview();
	}

	public void itemStateChanged(ItemEvent evt) {
		displaySortPreview();
	}

	protected void displaySortPreview() {
		int[] sortOrds = { -99, -99, -99, -99 };
		int[] sortDirs = { ASC, ASC, ASC, ASC };

		if (!inited)
			return;
		for (int i = 0; i < 4; i++)
			sortOrds[i] = -99;

		if (mLatSort.isSelected()) {
			int latOrd = mLatSortOrder.getSelectedIndex();
			sortOrds[latOrd] = LAT;
			if (mLatAsc.isSelected())
				sortDirs[latOrd] = ASC;
			else
				sortDirs[latOrd] = DSC;
		}

		if (mLonSort.isSelected()) {
			int lonOrd = mLonSortOrder.getSelectedIndex();
			sortOrds[lonOrd] = LON;
			if (mLonAsc.isSelected())
				sortDirs[lonOrd] = ASC;
			else
				sortDirs[lonOrd] = DSC;
		}

		if (mDepthSort.isSelected()) {
			int zOrd = mDepthSortOrder.getSelectedIndex();
			sortOrds[zOrd] = Z;
			if (mDepthAsc.isSelected())
				sortDirs[zOrd] = ASC;
			else
				sortDirs[zOrd] = DSC;
		}

		if (mTimeSort.isSelected()) {
			int tOrd = mTimeSortOrder.getSelectedIndex();
			sortOrds[tOrd] = T;
			if (mTimeAsc.isSelected())
				sortDirs[tOrd] = ASC;
			else
				sortDirs[tOrd] = DSC;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			if (sortOrds[i] == -99)
				continue;

			if (sortOrds[i] == LAT)
				sb = sb.append(" LAT-");
			else if (sortOrds[i] == LON)
				sb = sb.append(" LON-");
			else if (sortOrds[i] == Z)
				sb = sb.append(" DEPTH-");
			else if (sortOrds[i] == T)
				sb = sb.append(" TIME-");

			if (sortDirs[i] == ASC)
				sb = sb.append("ASC");
			else
				sb = sb.append("DSC");
		}

		if (sb.length() == 0)
			sortPreview.setText("No Sorting Specified");
		else
			sortPreview.setText(new String(sb));
		sortPreview.invalidate();
		sortPreview.validate();
	}

	public String getTitle() {
		return nameField.getText();
	}

	public void setWinTitle(String title) {
		nameField.setText(title);
	}

	public int[] getSortOrder() {
		int[] sortOrds = { -99, -99, -99, -99 };
		int[] sortDirs = { ASC, ASC, ASC, ASC };

		for (int i = 0; i < 4; i++)
			sortOrds[i] = -99;

		if (mLatSort.isSelected()) {
			int latOrd = mLatSortOrder.getSelectedIndex();
			sortOrds[latOrd] = LAT;
			if (mLatAsc.isSelected())
				sortDirs[latOrd] = EPSConstants.Y_ASC;
			else
				sortDirs[latOrd] = EPSConstants.Y_DSC;
		}

		if (mLonSort.isSelected()) {
			int lonOrd = mLonSortOrder.getSelectedIndex();
			sortOrds[lonOrd] = LON;
			if (mLonAsc.isSelected())
				sortDirs[lonOrd] = EPSConstants.X_ASC;
			else
				sortDirs[lonOrd] = EPSConstants.X_DSC;
		}

		if (mDepthSort.isSelected()) {
			int zOrd = mDepthSortOrder.getSelectedIndex();
			sortOrds[zOrd] = Z;
			if (mDepthAsc.isSelected())
				sortDirs[zOrd] = EPSConstants.Z_ASC;
			else
				sortDirs[zOrd] = EPSConstants.Z_DSC;
		}

		if (mTimeSort.isSelected()) {
			int tOrd = mTimeSortOrder.getSelectedIndex();
			sortOrds[tOrd] = T;
			if (mTimeAsc.isSelected())
				sortDirs[tOrd] = EPSConstants.T_ASC;
			else
				sortDirs[tOrd] = EPSConstants.T_DSC;
		}

		// count up number of sort specifications
		int count = 0;
		for (int i = 0; i < 4; i++) {
			if (sortOrds[i] == -99)
				continue;
			else
				count++;
		}

		// create the return array
		int[] ret = new int[count];

		count = 0;
		for (int i = 0; i < 4; i++) {
			if (sortOrds[i] == -99)
				continue;
			else
				ret[count++] = sortDirs[i];
		}

		return ret;
	}

	protected void setLatState(boolean state) {
		if (state) {
			mLatAsc.setEnabled(true);
			mLatDsc.setEnabled(true);
			mLatSortOrder.setEnabled(true);
			latLbl.setEnabled(true);
		}
		else {
			mLatAsc.setEnabled(false);
			mLatDsc.setEnabled(false);
			mLatSortOrder.setEnabled(false);
			latLbl.setEnabled(false);
		}
	}

	protected void setLonState(boolean state) {
		if (state) {
			mLonAsc.setEnabled(true);
			mLonDsc.setEnabled(true);
			mLonSortOrder.setEnabled(true);
			lonLbl.setEnabled(true);
		}
		else {
			mLonAsc.setEnabled(false);
			mLonDsc.setEnabled(false);
			mLonSortOrder.setEnabled(false);
			lonLbl.setEnabled(false);
		}
	}

	protected void setDepthState(boolean state) {
		if (state) {
			mDepthAsc.setEnabled(true);
			mDepthDsc.setEnabled(true);
			mDepthSortOrder.setEnabled(true);
			depthLbl.setEnabled(true);
		}
		else {
			mDepthAsc.setEnabled(false);
			mDepthDsc.setEnabled(false);
			mDepthSortOrder.setEnabled(false);
			depthLbl.setEnabled(false);
		}
	}

	protected void setTimeState(boolean state) {
		if (state) {
			mTimeAsc.setEnabled(true);
			mTimeDsc.setEnabled(true);
			mTimeSortOrder.setEnabled(true);
			timeLbl.setEnabled(true);
		}
		else {
			mTimeAsc.setEnabled(false);
			mTimeDsc.setEnabled(false);
			mTimeSortOrder.setEnabled(false);
			timeLbl.setEnabled(false);
		}
	}

}
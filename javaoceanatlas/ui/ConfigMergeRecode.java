package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigMergeRecode extends JOAJDialog implements ListSelectionListener, ActionListener, ButtonMaintainer,
    DocumentListener {
	protected int mSelParam = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;

	protected JOAJTextField mLowerLimitField = null;
	protected JOAJTextField mUpperLimitField = null;
	protected JOAJTextField mNewParamName = null;
	protected JOAJTextField mResultValue = null;

	protected JOAJRadioButton mGT = null;
	protected JOAJRadioButton mGTE = null;
	protected JOAJRadioButton mLT = null;
	protected JOAJRadioButton mLTE = null;
	protected JOAJRadioButton mEQ = null;

	static int GT = 0;
	static int GTE = 1;
	static int LT = 2;
	static int LTE = 3;
	static int EQ = 4;

	protected JOAJRadioButton mRecodeParameter = null;
	protected JOAJComboBox mValuesPopup;
	protected JOAJComboBox mQCPopup;
	// protected JLabel mRecodeQC = null;

	private JList mParamJList;
	private Object[] mSelParamText;
	private String[] mCols;
	private String mLines;
	private DialogClient mClient;
	private String mRecodedText;

	public ConfigMergeRecode(DialogClient client, JOAWindow par, String[] cols, String lines) {
		super(par, "Recode Merge Editor Values", false);
		mClient = client;
		mCols = cols;
		mLines = lines;
		this.init();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		Vector<String> missingVals = new Vector<String>();
		missingVals.add("JOA Missing Value (-99)");
		missingVals.add("EPIC Missing Value (1e35)");
		missingVals.add("WOCE Missing Value (-999)");
		mValuesPopup = new JOAJComboBox(missingVals);
		mValuesPopup.setEditable(false);
		mValuesPopup.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				int selVar = mValuesPopup.getSelectedIndex();
				if (selVar == 0) {
					mResultValue.setText("-99");
				}
				else if (selVar == 1) {
					mResultValue.setText("1e35");
				}
				else if (selVar == 2) {
					mResultValue.setText("-999");
				}
			}
		});

		// create the two parameter chooser lists
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));

		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));

		Vector<String> listData = new Vector<String>();
		for (int i = 0; i < mCols.length; i++) {
			listData.addElement(mCols[i]);
		}

		mParamJList = new JList(listData);
		mParamJList.addListSelectionListener(this);
    JScrollPane listScroller = new JScrollPane(mParamJList);

		// param panel containers
		JPanel recodeParamCont = new JPanel();
		recodeParamCont.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		// recode parameter
		recodeParamCont.add(listScroller);
		upperPanel.add(recodeParamCont);

		// panel for lower limit radio buttons
		JPanel llRadios = new JPanel();
		llRadios.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		mGT = new JOAJRadioButton(">", true);
		mGTE = new JOAJRadioButton(">=", false);
		mEQ = new JOAJRadioButton("=", false);
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(mGT);
		bg1.add(mGTE);
		bg1.add(mEQ);
		llRadios.add(mGT);
		llRadios.add(mGTE);
		llRadios.add(mEQ);
		upperPanel.add(llRadios);

		// lower limit text field
		mLowerLimitField = new JOAJTextField(4);
		mLowerLimitField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mLowerLimitField.getDocument().addDocumentListener(this);
		upperPanel.add(mLowerLimitField);

		// label for "AND"
		upperPanel.add(new JLabel("AND"));

		// upper limits radio
		JPanel ulRadios = new JPanel();
		ulRadios.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		mLT = new JOAJRadioButton("<", true);
		mLTE = new JOAJRadioButton("<=", false);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(mLT);
		bg2.add(mLTE);
		ulRadios.add(mLT);
		ulRadios.add(mLTE);
		upperPanel.add(ulRadios);

		// lower limit text field
		mUpperLimitField = new JOAJTextField(4);
		mUpperLimitField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mUpperLimitField.getDocument().addDocumentListener(this);
		upperPanel.add(mUpperLimitField);

		// label for "becomes"
		upperPanel.add(new JLabel("becomes"));

		// replace value text field
		mResultValue = new JOAJTextField(4);
		mResultValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mResultValue.getDocument().addDocumentListener(this);
		upperPanel.add(mResultValue);
		upperPanel.add(mValuesPopup);

		// middle panel holds the options
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		// this panel hold radio and text field
		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));

		// construct the dialog
		mainPanel.add("Center", upperPanel);
		mainPanel.add("South", middlePanel);
		contents.add("Center", new TenPixelBorder(mainPanel, 10, 10, 10, 10));

		// lower panel
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

		mOKBtn.addActionListener(this);
		mCancelButton.addActionListener(this);

		contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);

		maintainButtons();
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mParamJList) {
			// get the x param
			mSelParam = mParamJList.getSelectedIndex();
			if (mSelParam < 0) { return; }
			mSelParamText = mParamJList.getSelectedValues();
			maintainButtons();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// Recode
			int op1;
			if (mGT.isSelected()) {
				op1 = GT;
			}
			else if (mGTE.isSelected()) {
				op1 = GTE;
			}
			else {
				op1 = EQ;
			}

			int op2;
			if (mLT.isSelected()) {
				op2 = LT;
			}
			else {
				op2 = LTE;
			}

			float lowVal = -99f;
			float highVal = -99f;
			float replVal = -99f;
			try {
				lowVal = Float.valueOf(mLowerLimitField.getText());
			}
			catch (Exception ex) {
			}

			try {
				highVal = Float.valueOf(mUpperLimitField.getText());
			}
			catch (Exception ex) {
			}

			try {
				replVal = Float.valueOf(mResultValue.getText());
			}
			catch (Exception ex) {
				// some text in the combobox

			}

			for (Object o : mSelParamText) {
				String vtr = (String)o;
				mRecodedText = doRecode(vtr, op1, op2, lowVal, highVal, replVal);
				mLines = mRecodedText;
			}
			mClient.dialogDismissed(this);
			this.dispose();
		}
	}

	public void maintainButtons() {
		float lowVal = -99f;
		float highVal = -99f;
		try {
			lowVal = Float.valueOf(mLowerLimitField.getText());
		}
		catch (Exception ex) {
		}

		try {
			highVal = Float.valueOf(mUpperLimitField.getText());
		}
		catch (Exception ex) {
		}

		if (mSelParamText != null && mSelParamText.length == 0 /*|| (lowVal == -99 && highVal == -99)*/) {
			if (mOKBtn.isEnabled()) {
				mOKBtn.setEnabled(false);
			}
		}
		else if (mSelParamText != null && mSelParamText.length > 0 /*|| (lowVal != -99 || highVal != -99)*/) {
			if (!mOKBtn.isEnabled()) {
				mOKBtn.setEnabled(true);
			}
		}
	}

	public void changedUpdate(DocumentEvent evt) {
		maintainButtons();
	}

	public void insertUpdate(DocumentEvent evt) {
		maintainButtons();
	}

	public void removeUpdate(DocumentEvent evt) {
		maintainButtons();
	}

	public String doRecode(String varToRecode, int op1, int op2, float lowVal, float highVal, float replaceVal) {
		StringBuffer sb = new StringBuffer();

		LineNumberReader in = new LineNumberReader(new StringReader(mLines), 10000);
		// get the header line
		try {
			String inLine;

			int c = 0;
			int foundCol = -99;
			for (String ss : mCols) {
				if (ss.equalsIgnoreCase(varToRecode)) {
					foundCol = c;
					break;
				}
				c++;
			}

			// read the rest of the lines
			while (foundCol >= 0) {
				inLine = in.readLine();

				if (inLine == null || inLine.length() == 0) {
					break;
				}

				String[] tokens2 = inLine.split("[|]");
				c = 0;
				for (String ss : tokens2) {
					if (c == foundCol) {
						String strVal = tokens2[foundCol];
						float pval = -99;
						try {
							pval = Float.valueOf(strVal).floatValue();

							boolean con1 = true;
							boolean con2 = true;
							if (pval != JOAConstants.MISSINGVALUE) {
								if (lowVal != JOAConstants.MISSINGVALUE) {
									// test lower limit
									if (op1 == GT) {
										if (pval <= lowVal) {
											con1 = false;
										}
									}
									else if (op1 == GTE) {
										if (pval < lowVal) {
											con1 = false;
										}
									}
									else if (op1 == EQ) {
										if (pval != lowVal) {
											con1 = false;
										}
									}
								}

								if (highVal != JOAConstants.MISSINGVALUE) {
									// test upper limit
									if (op2 == LT) {
										if (pval >= highVal) {
											con2 = false;
										}
									}
									else if (op2 == LTE) {
										if (pval > highVal) {
											con2 = false;
										}
									}
								}
							}

							if (con1 && con2) {
								sb.append(replaceVal);
								sb.append("|");
							}
							else {
								sb.append(strVal);
								sb.append("|");
							}

						}
						catch (Exception ex) {
							// eat any exceptions and move on
						}
					}
					else {
						sb.append(ss);
						sb.append("|");
					}
					c++;
				}
				sb.append("\r");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		String outStr = new String(sb);
		return outStr;
	}

	public String getRecodedText() {
		return mRecodedText;
	}

}

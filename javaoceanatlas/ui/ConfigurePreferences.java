/**
 * 
 */
package javaoceanatlas.ui;

import gov.noaa.pmel.eps2.EPSConstants;
import gov.noaa.pmel.eps2.EPS_Util;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javaoceanatlas.io.FileImportException;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.ui.widgets.JOAJButton;
import javaoceanatlas.ui.widgets.JOAJRadioButton;
import javaoceanatlas.ui.widgets.JOAJTextField;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import javaoceanatlas.utility.RowLayout;

/**
 * @author oz
 *
 */
public class ConfigurePreferences {
protected JOAJRadioButton mWOCEQCStd;
protected JOAJRadioButton mIGOSSQCStd;
protected JOAJRadioButton mUnknownQCStd;
protected JOAJTextField mCustomMissingFld;
protected JOAJTextField mQCFlagFld;
protected JOAJRadioButton mJOADefaultMissing;
protected JOAJRadioButton mWOCEDefaultMissing;
protected JOAJRadioButton mEPICDefaultMissing;
protected JOAJRadioButton mNAMissing;
protected JOAJRadioButton mNaNMissing;
protected JOAJRadioButton mCustomMissing;
	
	public ConfigurePreferences() {
		//mAppendFileBtn = new JOAJButton(b.getString("kAppendFile"));

//		mAppendFileBtn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				// replace current preview with new file
//				// show merge dialog{
//				boolean keepAsking = true;
//				while (keepAsking) {
//					String mDirectory;
//					FileDialog f = new FileDialog(new JFrame(), "Open File to Append", FileDialog.LOAD);
//					f.setVisible(true);
//					mDirectory = f.getDirectory();
//					f.dispose();
//					if (mDirectory != null && f.getFile() != null) {
//						// get the file format
//						try {
//							int fileFormat = EPS_Util.getFileFormat(mDirectory, f.getFile());
//							if (fileFormat == EPSConstants.TEXTFORMAT || fileFormat == EPSConstants.SSFORMAT
//							    || fileFormat == EPSConstants.WOCEHYDFORMAT || fileFormat == EPSConstants.WOCECTDFORMAT) {
//								// got a text file--pass it to the config dialog
//								// ConfigureMergeImport cm = new ConfigureMergeImport(this,
//								// new File(mDirectory, f.getFile()));
//								File nFile = new File(mDirectory, f.getFile());
//								try {
//									refreshDisplay(nFile);
//								}
//								catch (Exception ex) {
//								}
//								keepAsking = false;
//							}
//						}
//						catch (Exception ex) {
//							JFrame ff = new JFrame("File Open Error");
//							Toolkit.getDefaultToolkit().beep();
//							String msg = "An error occurred trying to import " + f.getFile() + ".\n";
//							if (ex instanceof FileImportException) {
//								msg += ((FileImportException) ex).getErrorType() + " at line: \n";
//								msg += ((FileImportException) ex).getErrorLine();
//							}
//							JOptionPane.showMessageDialog(ff, msg);
//						}
//					}
//					else {
//						keepAsking = false;
//					}
//				}
//			}
//		});
		

		// missing value assignment
//		JPanel missingValuePanel = new JPanel();
//		missingValuePanel.setLayout(new GridLayout(2, 3));// (Orientation.LEFT,
//		// Orientation.TOP, 0));
//		mJOADefaultMissing = new JOAJRadioButton(b.getString("kJOADefaultMissing"), true);
//		mWOCEDefaultMissing = new JOAJRadioButton(b.getString("kWOCEDefaultMissing"));
//		mEPICDefaultMissing = new JOAJRadioButton(b.getString("kEPICDefaultMissing"));
//		mNAMissing = new JOAJRadioButton(b.getString("kNAMissing"));
//		mNaNMissing = new JOAJRadioButton(b.getString("kNaN"));
//		mCustomMissing = new JOAJRadioButton(b.getString("kCustom"));
//		ButtonGroup bg = new ButtonGroup();
//		bg.add(mJOADefaultMissing);
//		bg.add(mWOCEDefaultMissing);
//		bg.add(mEPICDefaultMissing);
//		bg.add(mNaNMissing);
//		bg.add(mNaNMissing);
//		bg.add(mCustomMissing);

		// JPanel mvLine1 = new JPanel(new RowLayout(Orientation.LEFT,
		// Orientation.CENTER, 0));
		// JPanel mvLine2 = new JPanel(new RowLayout(Orientation.LEFT,
		// Orientation.CENTER, 0));
		// JPanel mvLine3 = new JPanel(new RowLayout(Orientation.LEFT,
		// Orientation.CENTER, 0));
		// JPanel mvLine4 = new JPanel(new RowLayout(Orientation.LEFT,
		// Orientation.CENTER, 0));
		// JPanel mvLine5 = new JPanel(new RowLayout(Orientation.LEFT,
		// Orientation.CENTER, 0));
		// mvLine1.add(new JLabel(" "));
		// mvLine2.add(new JLabel(" "));
		// mvLine3.add(new JLabel(" "));
		// mvLine4.add(new JLabel(" "));
		// mvLine5.add(new JLabel(" "));

		// mvLine1.add(mJOADefaultMissing);
		// mvLine2.add(mWOCEDefaultMissing);
		// mvLine3.add(mEPICDefaultMissing);
		// mvLine4.add(mNAMissing);
		// mvLine5.add(mNaNMissing);

//		JPanel customMissingValPanel = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
//		customMissingValPanel.add(mCustomMissing);
//		mCustomMissingFld = new JOAJTextField(4);
//		mCustomMissingFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
//		customMissingValPanel.add(mCustomMissing);
//		customMissingValPanel.add(mCustomMissingFld);
//		missingValuePanel.add(mJOADefaultMissing);
//		missingValuePanel.add(mWOCEDefaultMissing);
//		missingValuePanel.add(mEPICDefaultMissing);
//		missingValuePanel.add(mNAMissing);
//		missingValuePanel.add(mNaNMissing);
//		missingValuePanel.add(customMissingValPanel);
//		fsPanel.add(missingValuePanel);
//
//		JPanel qcPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
//		qcPanel.add(new JLabel(b.getString("kQCIdent")));
//		mWOCEQCStd = new JOAJRadioButton(b.getString("kWOCEQCFLag"), true);
//		mIGOSSQCStd = new JOAJRadioButton(b.getString("kIGOSSQCFLag"));
//		JPanel customHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
//		mUnknownQCStd = new JOAJRadioButton(b.getString("kCustom"));
//		customHolder.add(mUnknownQCStd);
//		mQCFlagFld = new JOAJTextField(5);
//		mQCFlagFld.setEnabled(false);
//		customHolder.add(mQCFlagFld);
//
//		mUnknownQCStd.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent evt) {
//				if (evt.getStateChange() == ItemEvent.SELECTED) {
//					mQCFlagFld.setEnabled(true);
//				}
//			}
//		});
//
//		mWOCEQCStd.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent evt) {
//				if (evt.getStateChange() == ItemEvent.SELECTED) {
//					mQCFlagFld.setEnabled(false);
//				}
//			}
//		});
//
//		mIGOSSQCStd.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent evt) {
//				if (evt.getStateChange() == ItemEvent.SELECTED) {
//					mQCFlagFld.setEnabled(false);
//				}
//			}
//		});
//
//		ButtonGroup bg2 = new ButtonGroup();
//		bg2.add(mWOCEQCStd);
//		bg2.add(mIGOSSQCStd);
//		bg2.add(mUnknownQCStd);
//		JPanel qcStdPanel = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
//		qcStdPanel.add(new JLabel("   "));
//		qcStdPanel.add(mWOCEQCStd);
//		qcStdPanel.add(mIGOSSQCStd);
//		qcStdPanel.add(customHolder);
//		qcPanel.add(qcStdPanel);
//		fsPanel.add(qcPanel);
		

//		// set the missing value value
//		JOAConstants.USECUSTOMMISSINGVALUE = false;
//		if (mWOCEDefaultMissing.isSelected()) {
//			// JOAConstants.CUSTOMMISSINGVALUE = -999.0;
//			// JOAConstants.USECUSTOMMISSINGVALUE = true;
//		}
//		else if (mEPICDefaultMissing.isSelected()) {
//			// JOAConstants.CUSTOMMISSINGVALUE = 1e35;
//			// JOAConstants.USECUSTOMMISSINGVALUE = true;
//		}
//		else if (mNAMissing.isSelected()) {
//			// JOAConstants.CUSTOMMISSINGVALUE = 1e35;
//			// JOAConstants.USECUSTOMMISSINGVALUE = true;
//		}
//		else if (mNaNMissing.isSelected()) {
//			// JOAConstants.CUSTOMMISSINGVALUE = 1e35;
//			// JOAConstants.USECUSTOMMISSINGVALUE = true;
//		}
//
//		if (mCustomMissing.isSelected()) {
//			String valStr = mCustomMissingFld.getText();
//			if (valStr.length() == 0) {
//				JFrame frm = new JFrame("Custom Missing Value Error");
//				Toolkit.getDefaultToolkit().beep();
//				JOptionPane.showMessageDialog(frm, "Custom missing value has not been defined.");
//				return;
//			}
//			else {
//				try {
//					JOAConstants.CUSTOMMISSINGVALUE = Double.valueOf(valStr).doubleValue();
//				}
//				catch (NumberFormatException ex) {
//					JFrame frm = new JFrame("Custom Missing Value Error");
//					Toolkit.getDefaultToolkit().beep();
//					JOptionPane.showMessageDialog(frm, "Illegal custom missing value (must be numeric).");
//					return;
//				}
//
//			}
//			JOAConstants.USECUSTOMMISSINGVALUE = true;
//		}
		


//		JPanel settingsLine4 = new JPanel();
//		settingsLine4.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
//		settingsLine4.add(new JLabel(b.getString("kCommentLines")));
//		mCommentsFld = new JTextField(12);
//		mCommentsFld.setText("# // -- ## BOTTLE END_DATA");
//		mCommentTokens = mCommentsFld.getText().split("[ \t,]");
//		settingsLine4.add(mCommentsFld);
//		settingsPanel.add(settingsLine4);
		


//		mSaveFileBtn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				ConfigureTextExport expDialog = new ConfigureTextExport(new JFrame(), mTextArea.getText(), "|", ":", mFile
//				    .getName());
//
//				// show dialog at expDialog of screen
//				Rectangle dBounds = expDialog.getBounds();
//				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
//				int x = sd.width / 2 - dBounds.width / 2;
//				int y = sd.height / 2 - dBounds.height / 2;
//				expDialog.setLocation(x, y);
//				expDialog.setVisible(true);
//			}
//		});


//		JPanel fsPanel = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 0));
//		fsPanel.add(new JLabel(b.getString("kMissingValueEncoded")));
//
//
//		tb = BorderFactory.createTitledBorder(b.getString("kFileSettings"));
//		fsPanel.setBorder(tb);
		

	}
	public String getMissingVal() {
		// get the missing value value
		String missingValText = "-99";
		if (mJOADefaultMissing.isSelected()) {
			missingValText = "-99";
		}
		else if (mWOCEDefaultMissing.isSelected()) {
			missingValText = "-999.0";
		}
		else if (mEPICDefaultMissing.isSelected()) {
			missingValText = "1e35";
		}
		else if (mCustomMissing.isSelected()) {
			missingValText = mCustomMissingFld.getText();

			if (missingValText == null || missingValText.length() == 0) {
				JFrame frm = new JFrame("Custom Missing Value Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(frm, "Custom missing value has not been defined.");
				return null;
			}
		}
		return missingValText;
	}

	public int getSrcQCStandard() {
		if (mWOCEQCStd.isSelected()) {
			return JOAConstants.WOCE_QC_STD;
		}
		else if (mUnknownQCStd.isSelected()) {
			return JOAConstants.NONE_QC_STD;
		}
		else {
			return JOAConstants.IGOSS_QC_STD;
		}
	}

	public double getSrcMissingValue() {
		if (mJOADefaultMissing.isSelected()) {
			return JOAConstants.MISSINGVALUE;
		}
		else if (mWOCEDefaultMissing.isSelected()) {
			return JOAConstants.WOCEMISSINGVALUE;
		}
		else if (mEPICDefaultMissing.isSelected()) {
			return JOAConstants.EPICMISSINGVALUE;
		}
		else if (mNaNMissing.isSelected()) {
			return Double.NaN;
		}
		else if (mNAMissing.isSelected()) {
			return JOAConstants.MISSINGVALUE;
		}
		else if (mCustomMissing.isSelected()) {
			try {
				double val = Double.valueOf(mCustomMissingFld.getText());
				return val;
			}
			catch (Exception ex) {
				return JOAConstants.MISSINGVALUE;
			}
		}
		return JOAConstants.MISSINGVALUE;
	}
	

}

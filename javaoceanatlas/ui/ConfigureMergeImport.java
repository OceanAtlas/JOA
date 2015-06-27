/*
 * $Id: ConfigureMergeImport.java,v 1.2 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import java.text.DecimalFormat;
import gov.noaa.pmel.eps2.EPSProperties;
import gov.noaa.pmel.eps2.EPS_Util;
import gov.noaa.pmel.eps2.EPSConstants;
import javaoceanatlas.io.FileImportException;

@SuppressWarnings("serial")
public class ConfigureMergeImport extends JOAWindow implements ActionListener {
	protected JOAJButton mNextBtn = null;
	protected JOAJButton mPreviousBtn = null;
	protected JOAJButton mRefreshButton = null;
	protected JOAJComboBox mFieldDelimiters;
	protected File mFile;
	//protected JTextArea mTextArea;
	static int COMMA_DELIM = 0;
	static int TAB_DELIM = 1;
	static int SPACE_DELIM = 2;
	private int mNumSkip = 0;
	private int mHeaderLineNumber = -99;
	private int mUnitsLineNumber = -99;
	private JTextField mNumToSkipFld;
	private JTextField mHeaderLineFld;
	private JTextField mUnitsLineFld;
	ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private String[] mCommentTokens;
	private JPanel mEveryThingPanel;
	private MergeControlPanel mDoMergePanel;
	private MergeEditor mMergeEditor;
	int[] mDispPrec = { 4 };
  private JOAJButton mOpen;
  private JOAJButton mRecode;
  private JOAJButton mExport;
  private JOAJComboBox mActions;
	StringBuffer mPreProcessedText = new StringBuffer();
	JTextArea mPreProcessedTextPreview = new JTextArea(100, 80);

	public ConfigureMergeImport(FileViewer par, File inFile) {
		super(true, true, true, true, true, null);
		mFile = inFile;
		try {
			this.init();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() {
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(0, 0));

		// Everything goes into a JtabbedPane
		mEveryThingPanel = new JPanel();

		// make the preview panel
		try {
			PreviewPanel mPreview = new PreviewPanel();
			mEveryThingPanel.add(mPreview);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		contents.add(new TenPixelBorder(mEveryThingPanel, 5, 5, 0, 0), "Center");
		this.pack();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);

		try {
			refreshDisplay();
		}
		catch (Exception ex) {
		}
	}

	public class PreviewPanel extends JPanel {
		public PreviewPanel() {
			this.setLayout(new BorderLayout(5, 5));
			JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

			// preview pane
			JPanel previewPanel = new JPanel();
			previewPanel.setLayout(new BorderLayout(0, 0));
			
			// make the second step: preview
			String[] cols = { "foo" };
			mMergeEditor = new MergeEditor();
			mMergeEditor.setUp(cols, "0:foo", mFile);
			
			JTabbedPane mTabs = new JTabbedPane();
			mTabs.add("Source Editor", mMergeEditor);
			
			JPanel previewHolder = new JPanel(new BorderLayout(5, 5));
			MyScroller scroller = new MyScroller(mPreProcessedTextPreview);    
	    previewHolder.add(scroller, BorderLayout.CENTER);
			mTabs.add("Source Preview", previewHolder);
			
			previewPanel.add(mTabs, BorderLayout.CENTER);
			TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kFilePreview") + ": " + mFile.getName());
			previewPanel.setBorder(tb);
			split.setLeftComponent(new TenPixelBorder(previewPanel, 5, 5, 0, 0));
			
			Vector<String> delimChoices = new Vector();
			delimChoices.add(b.getString("kTabComma"));
			delimChoices.add(b.getString("kWhiteSpace"));
			mFieldDelimiters = new JOAJComboBox(delimChoices);		

			// panel for settings
			JPanel settingsPanel = new JPanel();
			settingsPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 5));

			JPanel settingsLine1 = new JPanel();
			settingsLine1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			settingsLine1.add(new JLabel(b.getString("kNumToSkip")));
			mNumToSkipFld = new JTextField(4);
			settingsLine1.add(mNumToSkipFld);
			settingsPanel.add(settingsLine1);

			JPanel settingsLine2 = new JPanel();
			settingsLine2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			settingsLine2.add(new JLabel(b.getString("kHeaderLineNumber")));
			mHeaderLineFld = new JTextField(4);
			settingsLine2.add(mHeaderLineFld);
			settingsPanel.add(settingsLine2);

			JPanel settingsLine3 = new JPanel();
			settingsLine3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			settingsLine3.add(new JLabel(b.getString("kUnitsLineNumber")));
			mUnitsLineFld = new JTextField(4);
			settingsLine3.add(mUnitsLineFld);
			settingsPanel.add(settingsLine3);

			//TODO: tokens need to come from preferences
			String commentsStr = "# // -- ## BOTTLE END_DATA";
			mCommentTokens = commentsStr.split("[ \t,]");
			JPanel settingsLine4 = new JPanel();
			settingsLine4.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			settingsLine4.add(new JLabel(b.getString("kFieldDelimiter")));
			settingsLine4.add(mFieldDelimiters);
			settingsPanel.add(settingsLine4);

			JPanel controlPanel = new JPanel();
			controlPanel.setLayout(new GridLayout(3, 1, 5, 5));
			mRefreshButton = new JOAJButton(b.getString("kRefresh"));

			mRefreshButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						String fldText = mNumToSkipFld.getText();
						mNumSkip = Integer.valueOf(fldText);
					}
					catch (Exception ex) {
						mNumSkip = 0;
						mNumToSkipFld.setText("");
					}
					try {
						String fldText = mHeaderLineFld.getText();
						mHeaderLineNumber = Integer.valueOf(fldText);
					}
					catch (Exception ex) {
						mHeaderLineNumber = -99;
						mHeaderLineFld.setText("");
					}
					try {
						String fldText = mUnitsLineFld.getText();
						mUnitsLineNumber = Integer.valueOf(fldText);
					}
					catch (Exception ex) {
						mUnitsLineNumber = -99;
						mUnitsLineFld.setText("");
					}

					try {
						refreshDisplay();
					}
					catch (Exception ex) {

					}
				}
			});
			settingsPanel.add(mRefreshButton);

	    mActions = new JOAJComboBox();
	    mActions.addItem(b.getString("kActions") + ":");
	    mActions.addItem("Add Column...");
	    mActions.addItem("Edit Column Names/Units...");
	    mActions.addItem("Delete Column...");

	    mActions.addItemListener(new ItemListener() {
	      @SuppressWarnings("unchecked")
	      public void itemStateChanged(ItemEvent evt) {
	        int action = mActions.getSelectedIndex();

//	        // need the currently selected column
//	        if (action == 1 && evt.getStateChange() == ItemEvent.SELECTED) {
//	          //Add Variable
//	          ConfigNewColumn customDialog = new ConfigNewColumn(mParent, mThis);
//	          customDialog.pack();
//	          customDialog.setVisible(true);
//	        }
//	        else if (action == 2 && evt.getStateChange() == ItemEvent.SELECTED) {
//	          // get the column header for the selected column
//	          TableColumn tc = mValsTable.getColumnModel().getColumn(mCurrCol);
//	          String header = (String)tc.getHeaderValue();
//	          String param = null;
//	          String units = null;
//	          if (header.indexOf(":") >= 0) {
//	            String[] tokens = header.split("[:]");
//	            param = tokens[0];
//	            if (tokens.length > 1) {
//	            	units = tokens[1];
//	            }
//	          }
//	          else {
//	            param = header;
//	          }
//
//	          EditMergeColumn customDialog = new EditMergeColumn(mParent, mThis, param, units, mDispPrec[mCurrCol]);
//	          customDialog.pack();
//	          customDialog.setVisible(true);
//	        }
//	        else if (action == 3 && evt.getStateChange() == ItemEvent.SELECTED) {
//	          //delete columns
//	          JFrame f = new JFrame("Merge Editor");
//	          Toolkit.getDefaultToolkit().beep();
//	          int response = JOptionPane.YES_OPTION;
//	          JOptionPane.showConfirmDialog(f, "Are you sure you want to delete the selected column?");
//	          if (response == JOptionPane.YES_OPTION) {
//	            // get the current column headers
//	            Vector<String> cols = new Vector<String>(getColHeaders());
//
//	            // remove the selected column header
//	            cols.remove(mCurrCol);
//
//	            String[] newCols = new String[cols.size()];
//
//	            for (int i = 0; i < cols.size(); i++) {
//	              newCols[i] = cols.elementAt(i);
//	            }
//
//	            // now create a vector array
//	            int rc = mValsTable.getRowCount();
//	            Vector<String>[] vals = new Vector[rc];
//
//	            for (int r = 0; r < rc; r++) {
//	              Vector<?> row = getRow(r);
//	              row.remove(mCurrCol);
//	              vals[r] = (Vector<String>)row;
//	            }
//
//	            updateTableModel2(newCols, vals);
//	          }
//
//	        }
	      }
	    });

	    mRecode = new JOAJButton(b.getString("kRecode"));
	    mRecode.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
//	        String src = getSourceText();
//	        ConfigMergeRecode customDialog = new ConfigMergeRecode(mThis, mParent, mCols, src);
//	        customDialog.pack();
//	        customDialog.setVisible(true);
	      }
	    });

	    mOpen = new JOAJButton(b.getString("kOpen"));

	    mOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// replace current preview with new file
					// show merge dialog{
					boolean keepAsking = true;
					while (keepAsking) {
						String mDirectory;
						FileDialog f = new FileDialog(new JFrame(), "Open File to Merge", FileDialog.LOAD);
						f.setVisible(true);
						mDirectory = f.getDirectory();
						f.dispose();
						if (mDirectory != null && f.getFile() != null) {
							// get the file format
							try {
								int fileFormat = EPS_Util.getFileFormat(mDirectory, f.getFile());
								if (fileFormat == EPSConstants.TEXTFORMAT || fileFormat == EPSConstants.SSFORMAT
								    || fileFormat == EPSConstants.WOCEHYDFORMAT || fileFormat == EPSConstants.WOCECTDFORMAT) {
									// got a text file--pass it to the config dialog
									// ConfigureMergeImport cm = new ConfigureMergeImport(this,
									// new File(mDirectory, f.getFile()));
									File nFile = new File(mDirectory, f.getFile());
									try {
										refreshDisplay(nFile);
									}
									catch (Exception ex) {
									}
									keepAsking = false;
								}
							}
							catch (Exception ex) {
								JFrame ff = new JFrame("File Open Error");
								Toolkit.getDefaultToolkit().beep();
								String msg = "An error occurred trying to import " + f.getFile() + ".\n";
								if (ex instanceof FileImportException) {
									msg += ((FileImportException) ex).getErrorType() + " at line: \n";
									msg += ((FileImportException) ex).getErrorLine();
								}
								JOptionPane.showMessageDialog(ff, msg);
							}
						}
						else {
							keepAsking = false;
						}
					}
				}

			});

	    mExport = new JOAJButton(b.getString("kSaveAs"));
	    mExport.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
//	        StringBuffer sb = new StringBuffer();
//
//	        for (int c = 0; c < mValsTable.getColumnCount(); c++) {
//	          String col = mValsTable.getColumnName(c);
//	          sb.append(col);
//	          if (c < mValsTable.getColumnCount() - 1) {
//	            sb.append("|");
//	          }
//	        }
//	        sb.append("\r");
//
//	        // get the number of lines from the editor panel
//	        int numLines = getNumSourceLines();
//
//	        for (int l = 0; l < numLines; l++) {
//	          Vector<String> line = getRow(l);
//
//	          for (int c = 0; c < line.size(); c++) {
//	            String col = line.elementAt(c);
//	            sb.append(col);
//	            if (c < line.size() - 1) {
//	              sb.append("|");
//	            }
//	          }
//	          sb.append("\r");
//	        }
//
//	        String inText = new String(sb);
//	        if (inText == null || inText.length() == 0) {
//	          return;
//	        }
//
//	        ConfigureTextExport expDialog = new ConfigureTextExport(new JFrame(), inText, "|", null, mFile.getName());
//
//	        // show dialog at expDialog of screen
//	        Rectangle dBounds = expDialog.getBounds();
//	        Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
//	        int x = sd.width / 2 - dBounds.width / 2;
//	        int y = sd.height / 2 - dBounds.height / 2;
//	        expDialog.setLocation(x, y);
//	        expDialog.setVisible(true);
	      }
	    });
			split.setRightComponent(settingsPanel);
			this.add(BorderLayout.CENTER, split);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			mDoMergePanel.closeMe();
			mMergeEditor.closeMe();
			this.dispose();
			JOAConstants.CANCELIMPORT = true;
		}
		else if (cmd.equals("ok")) {
		}
	}

	private void refreshDisplay() throws Exception {
		LineNumberReader in = null;
		mPreProcessedText = null;
		mPreProcessedText = new StringBuffer();
		int count = 1;
		DecimalFormat lineNum = new DecimalFormat();
		boolean capturedPrecisions = false;

		try {
			in = new LineNumberReader(new FileReader(mFile), 10000);

			String inLine;

			// skip header lines
			for (int i = 0; i < mNumSkip; i++) {
				inLine = in.readLine();
				count++;
			}

			while (true) {
				inLine = in.readLine();

				if (inLine == null) {
					break;
				}

				if (inLine.length() == 0) {
					count++;
					continue;
				}

				//inLine = inLine.trim();

				while (inLine.indexOf("  ") > 0) {
					inLine = inLine.replace("  ", " ");
				}

				// test for a comment lines
				boolean skip = false;
				for (int i = 0; i < mCommentTokens.length; i++) {
					if (inLine.toUpperCase().startsWith(mCommentTokens[i].toUpperCase())) {
						skip = true;
						break;
					}
				}
				if (skip) {
					count++;
					continue;
				}

				if (mHeaderLineNumber >= 0 && count == mHeaderLineNumber) {
					mPreProcessedText.append("header " + lineNum.format(count) + ": ");
				}
				else if (mUnitsLineNumber >= 0 && count == mUnitsLineNumber) {
					mPreProcessedText.append("units  " + lineNum.format(count) + ": ");
				}
				else {
					mPreProcessedText.append("       " + lineNum.format(count) + ": ");

					//have to deal with duplicated commas and tabs
					//TODO the hard coded values here need be replaced with value from prefs
					// currently defaults to WOCE missing value
					if (inLine.contains(",,")) {
					inLine = expandNullItems(inLine, ",", "-999.0");//String.valueOf(getSrcMissingValue()));
					}
					else if (inLine.contains("\t\t")) {
						inLine = expandNullItems(inLine, "\t", "-999.0");//String.valueOf(getSrcMissingValue()));
					}
				}
				
				String defaultSplitter = "[,\t\\s+]";
				if (((String)mFieldDelimiters.getSelectedItem()).equalsIgnoreCase(b.getString("kTabComma"))) {
					defaultSplitter = "[,\t]";
				}
				else if (((String)mFieldDelimiters.getSelectedItem()).equalsIgnoreCase(b.getString("kWhiteSpace"))) {
					defaultSplitter = "[\\s+]";
				}

				// break the line into tokens
				String[] tokens = inLine.split(defaultSplitter);
				for (int t = 0; t < tokens.length; t++) {
					mPreProcessedText.append(tokens[t] + "|");
				}
				mPreProcessedText.append("\n");
				count++;

				if (!capturedPrecisions) {
					mDispPrec = new int[tokens.length];
					for (int t = 0; t < tokens.length; t++) {
						mDispPrec[t] = 0;
					}
					capturedPrecisions = true;
				}

				// capture precisions to build up maximum precision used
				for (int t = 0; t < tokens.length; t++) {
					String[] ntokens = tokens[t].split("[.]");

					if (ntokens.length > 1) {
						int prec = ntokens[1].length();
						if (prec > mDispPrec[t]) {
							mDispPrec[t] = prec;
						}
					}
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			mPreProcessedText.append("Can't display preview!");
		}
		finally {
			in.close();
			EPSProperties.DOUBLEDELIM = EPSConstants.STAB_DELIMITER + EPSConstants.STAB_DELIMITER;
		}
		mPreProcessedTextPreview.setText(new String(mPreProcessedText));
		updateMergePanel();
	}

	private void refreshDisplay(File inFile) throws Exception {
		LineNumberReader in = null;

		int count = 1;
		DecimalFormat lineNum = new DecimalFormat();

		try {
			in = new LineNumberReader(new FileReader(inFile), 10000);

			String inLine;

			// skip header lines
			for (int i = 0; i < mNumSkip; i++) {
				inLine = in.readLine();
				count++;
			}

			while (true) {
				inLine = in.readLine();

				if (inLine == null) {
					break;
				}

				if (inLine.length() == 0) {
					count++;
					continue;
				}

				inLine = inLine.trim();

				// while (inLine.indexOf(" ") > 0) {
				// inLine = inLine.replace(" ", " ");
				// }

				// test for a comment lines
				boolean skip = false;
				for (int i = 0; i < mCommentTokens.length; i++) {
					if (inLine.toUpperCase().startsWith(mCommentTokens[i].toUpperCase())) {
						skip = true;
						break;
					}
				}
				if (skip) {
					count++;
					continue;
				}

				if (mHeaderLineNumber >= 0 && count == mHeaderLineNumber) {
					mPreProcessedText.append("header " + lineNum.format(count) + ": ");
				}
				else if (mUnitsLineNumber >= 0 && count == mUnitsLineNumber) {
					mPreProcessedText.append("units  " + lineNum.format(count) + ": ");
				}
				else {
					mPreProcessedText.append("       " + lineNum.format(count) + ": ");
				}

				String defaultSplitter = "[,\t\\s+]";
				if (((String)mFieldDelimiters.getSelectedItem()).equalsIgnoreCase(b.getString("kTabComma"))) {
					defaultSplitter = "[,\t]";
				}
				else if (((String)mFieldDelimiters.getSelectedItem()).equalsIgnoreCase(b.getString("kWhiteSpace"))) {
					defaultSplitter = "[\\s+]";
				}

				// break the line into tokens
				String[] tokens = inLine.split(defaultSplitter);
				for (int t = 0; t < tokens.length; t++) {
					mPreProcessedText.append(tokens[t] + "|");
				}
				mPreProcessedText.append("\n");
				count++;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			mPreProcessedText.append("Can't display preview!");
		}
		finally {
			in.close();
		}
		updateMergePanel();
	}

	public void updateMergePanel() {
		String contents = new String(mPreProcessedText);
		LineNumberReader in = new LineNumberReader(new StringReader(contents), 10000);

		// get the header line
		String[] mColHeaders = null;
		try {
			String inHdrLine, inUnitsLine;

			// get first line: by definition, the header line
			inHdrLine = in.readLine();
			inUnitsLine = in.readLine();

			// break by :
			String[] tokens = inHdrLine.split("[:]");
			String headers = tokens[1];

			String units = "";
			if (inUnitsLine.indexOf("units") >= 0) {
				tokens = inUnitsLine.split("[:]");
				units = tokens[1];
			}

			// make the colheaders
			String[] tokens2 = headers.split("[|]");
			mColHeaders = new String[tokens2.length];

			String[] unitTokens = null;
			if (units.length() > 0) {
				unitTokens = units.split("[|]");
			}

			int c = 0;
			for (String ss : tokens2) {
				mColHeaders[c] = ss;
				c++;
			}

			if (unitTokens != null) {
				c = 0;
				for (String un : unitTokens) {
					mColHeaders[c] += ":" + un;
					c++;
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
      JFrame f = new JFrame("Parse Error");
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(f, "JOA encountered an error parsing the column headers." + "\n" +
      		" (" + ex.getCause().toString() + " " + ex.getMessage() + ")" + "\n" +
      		"This may be caused by incorrect leading whitespace.");
		}

		if (mColHeaders != null) {
			// rebuild the table mode;
			mMergeEditor.updateTableModel(mColHeaders, contents);
			mMergeEditor.setDisplayPrecision(mDispPrec);
			mDoMergePanel.setColumns(mColHeaders);
			mDoMergePanel.buildCriteriaCombos();
			mDoMergePanel.buildParamLists();
		}
	}

	public MergeControlPanel getMergeControlPanel() {
		return mDoMergePanel;
	}

	public MergeEditor getMergeEditor() {
		return mMergeEditor;
	}

	public String getTitle() {
		return super.getTitle();
	}

	public RubberbandPanel getPanel() {
		return null;
	}

	public String getMissingVal() {
		// get the missing value value
		String missingValText = "-99";
			missingValText = "-999.0";
		return missingValText;
	}
	
	public String expandNullItems(String inStr, String delim, String replaceVal) {
		String doubleDelim = delim + delim;
		if (inStr.indexOf(doubleDelim) >= 0) {
			if (inStr.startsWith(EPSProperties.DOUBLEDELIM))
				inStr = new String(replaceVal + delim + inStr);
				
			// expand
			int pos = -1;
			while ((pos = inStr.indexOf(doubleDelim)) >= 0) {
				StringBuffer sb = new StringBuffer(inStr);
				sb = sb.insert(pos+1, replaceVal);
				inStr = new String(sb);
			}
			return inStr;
		}
		else
			return inStr;
	}
	
  private class MyScroller extends JScrollPane {
    public MyScroller(Component c) {
      super(c);
    }

    public Dimension getPreferredSize() {
      return new Dimension(210, 150);
    }
  }

  public void runTimer() {
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      public void run() {
        if (mActions.getSelectedIndex() != 0) {
          mActions.setSelectedIndex(0);
        }
      }
    };
    timer.schedule(task, 0, 1000);
  }
}

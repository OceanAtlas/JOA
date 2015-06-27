package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.BorderLayout;
import javaoceanatlas.PowerOceanAtlas;
import javaoceanatlas.ui.widgets.JOAJButton;
import java.awt.GridLayout;
import javax.swing.border.TitledBorder;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import gov.noaa.pmel.eps2.EPSConstants;
import gov.noaa.pmel.swing.NPixelBorder;
import java.awt.FlowLayout;
import javaoceanatlas.resources.JOAConstants;
import java.awt.Cursor;
import javaoceanatlas.ui.widgets.JOAJLabel;
import javaoceanatlas.ui.widgets.JOAJList;
import javaoceanatlas.ui.widgets.JOAJCheckBox;
import javaoceanatlas.ui.widgets.JOAJTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.SectionStation;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.utility.RowLayout;
import javaoceanatlas.ui.widgets.SmallIconButton;
import java.awt.Toolkit;
import javaoceanatlas.events.ParameterAddedEvent;
import javaoceanatlas.utility.Parameter;
import javaoceanatlas.utility.JOAFormulas;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.io.File;
import java.awt.Frame;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import javaoceanatlas.utility.ButtonMaintainer;
import javaoceanatlas.ui.widgets.JOAJRadioButton;
import javaoceanatlas.utility.Searchable;
import javaoceanatlas.ui.widgets.SearchField;
import java.awt.Color;
import javaoceanatlas.utility.MergeCriteriaContainer;
import javaoceanatlas.utility.TenPixelBorder;
import java.awt.Component;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MergeControlPanel extends JPanel implements ListSelectionListener, ButtonMaintainer, Searchable {
	private JOAJButton mMergeButton = null;
	private JOAJButton mClearBtn = null;
	private JOAJButton mSaveButton = null;
	private JOAJCheckBox mMergeMissing = null;
	private JOAJCheckBox mReplaceMatching = null;
	private JOAJCheckBox mCreateNewSection = null;
	private JOAJCheckBox mOptimizedSearch = null;
	private JOAJTextField mSectionNameField = null;
	private SectionChooser mSectionChooser;
	private ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private Vector<String> mColheaders;
	private FileViewer mFileViewer;
	private JTextArea mLogArea;
	private JOAJList mReplaceList = null;
	private JOAJList mMergeList = null;
	private MergeEditor mMergeEditor;
	private Timer timer = new Timer();
	protected JOAJRadioButton mAllLogMessages;
	protected JOAJRadioButton mJustErrors;
	protected JOAJRadioButton mNoOutput;
	private StringBuffer mAllText;
	private StringBuffer mErrorText;
	private SearchField mSearchFld;
	private int srchStart = 0;
	private MergeCriteriaContainer mCriteriaContPanel;
	private MyScroller mCriteriaScroller;
	private boolean mIsInMergeSemaphore = false;
	private double mSrcMissingValue;
	ConfigureMergeImport mParent;
	private int[] mDispPrecisions;

	public MergeControlPanel(FileViewer fv, ConfigureMergeImport parent) {
		mFileViewer = fv;
		mParent = parent;
		mMergeEditor = parent.getMergeEditor();
		mColheaders = mMergeEditor.getColHeaders();
		//TODO get default value from prefs
		mSrcMissingValue = JOAConstants.WOCEMISSINGVALUE;//parent.getSrcMissingValue();

		// make the JLists of column headers
		buildParamLists();

		// holds all the panels in top
		JPanel topPanelHolder = new JPanel();
		topPanelHolder.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));

		// holds criteria and destination
		JPanel topLeftHolder = new JPanel(new GridLayout(2, 1, 5, 5));

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 2, 5, 5));

		JPanel criteriaContainer = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 2));
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kMatchCriteria"));
		criteriaContainer.setBorder(tb);

		Vector<String> listData = new Vector<String>();
		for (int i = 0; i < mColheaders.size(); i++) {
			listData.addElement(mColheaders.elementAt(i));
		}

		mCriteriaContPanel = new MergeCriteriaContainer(listData, this);
		mCriteriaScroller = new MyScroller(new TenPixelBorder(mCriteriaContPanel, 5, 5, 5, 5));
		criteriaContainer.add(mCriteriaScroller);
		mOptimizedSearch = new JOAJCheckBox(b.getString("kOptimizedSearch"), true);
		criteriaContainer.add(mOptimizedSearch);
		topLeftHolder.add(criteriaContainer);

		JPanel mergeToPanel = new JPanel();
		mergeToPanel.setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 2));
		tb = BorderFactory.createTitledBorder(b.getString("kMergeTo"));
		mergeToPanel.setBorder(tb);
		mSectionChooser = new SectionChooser(mFileViewer, b.getString("kSection"), this, "XXXXXXX88888888888888XXXXXXXXXXX");
		mSectionChooser.init();
		mergeToPanel.add(new NPixelBorder(mSectionChooser, 5, 5, 5, 5));
		topLeftHolder.add(mergeToPanel);

		JPanel createNewPanelPanel = new JPanel(new RowLayout(Orientation.CENTER, Orientation.CENTER, 2));
		mCreateNewSection = new JOAJCheckBox(b.getString("kCreateNewSection"), true);
		mergeToPanel.add(mCreateNewSection);
		mSectionNameField = new JOAJTextField(20);
		final JLabel nameLbl = new JOAJLabel(b.getString("kNewName"));
		createNewPanelPanel.add(nameLbl);
		createNewPanelPanel.add(mSectionNameField);

		mCreateNewSection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (mCreateNewSection.isSelected()) {
					mSectionNameField.setEnabled(true);
					nameLbl.setEnabled(true);
				}
				else {
					mSectionNameField.setEnabled(false);
					nameLbl.setEnabled(false);
				}
			}
		});
		mergeToPanel.add(createNewPanelPanel);
		topPanel.add(topLeftHolder);

		JPanel rulesPanel = new JPanel();
		rulesPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.TOP, 2));
		tb = BorderFactory.createTitledBorder(b.getString("kMergeRules"));
		rulesPanel.setBorder(tb);

		JPanel mergeListCont = new JPanel(new RowLayout(Orientation.CENTER, Orientation.TOP, 2));
		JPanel replaceListCont = new JPanel(new RowLayout(Orientation.CENTER, Orientation.TOP, 2));

		mMergeMissing = new JOAJCheckBox(b.getString("kMergeMissingParameters"), true);
		rulesPanel.add(mMergeMissing);

		JScrollPane listScroller = new JScrollPane(mMergeList);
		mergeListCont.add(new JLabel("    "));
		mergeListCont.add(new JLabel("    "));
		mergeListCont.add(listScroller);
		rulesPanel.add(mergeListCont);

		mReplaceMatching = new JOAJCheckBox(b.getString("kReplaceMissingParameters"), true);
		rulesPanel.add(mReplaceMatching);

		JScrollPane listScroller2 = new JScrollPane(mReplaceList);
		replaceListCont.add(new JLabel("    "));
		replaceListCont.add(new JLabel("    "));
		replaceListCont.add(listScroller2);
		rulesPanel.add(replaceListCont);

		topPanel.add(rulesPanel);
		topPanelHolder.add(topPanel);
		mMergeButton = new JOAJButton(b.getString("kMerge"));
		topPanelHolder.add(mMergeButton);

		this.setLayout(new BorderLayout(5, 5));
		this.add(BorderLayout.NORTH, topPanelHolder);

		final SmallIconButton mergeCheckAll = new SmallIconButton(new ImageIcon(getClass().getResource(
		    "images/checkall.gif")));
		final SmallIconButton mergeCheckNone = new SmallIconButton(new ImageIcon(getClass().getResource(
		    "images/checknone.gif")));
		final SmallIconButton replaceCheckAll = new SmallIconButton(new ImageIcon(getClass().getResource(
		    "images/checkall.gif")));
		final SmallIconButton replaceCheckNone = new SmallIconButton(new ImageIcon(getClass().getResource(
		    "images/checknone.gif")));

		JPanel allNoneCont = new JPanel();
		allNoneCont.setLayout(new GridLayout(2, 1, 0, 5));
		allNoneCont.add(mergeCheckAll);
		allNoneCont.add(mergeCheckNone);
		mergeListCont.add(allNoneCont);

		mergeCheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mMergeList.setSelectionInterval(0, mColheaders.size() - 1);
				mergeCheckAll.setSelected(false);
			}
		});

		mergeCheckNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mMergeList.clearSelection();
				mergeCheckNone.setSelected(false);
			}
		});

		JPanel allNoneCont2 = new JPanel();
		allNoneCont2.setLayout(new GridLayout(2, 1, 0, 5));
		allNoneCont2.add(replaceCheckAll);
		allNoneCont2.add(replaceCheckNone);
		replaceListCont.add(allNoneCont2);

		replaceCheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mReplaceList.setSelectionInterval(0, mColheaders.size() - 1);
				replaceCheckAll.setSelected(false);
			}
		});

		replaceCheckNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mReplaceList.clearSelection();
				replaceCheckNone.setSelected(false);
			}
		});

		mMergeList.setEnabled(true);
		mMergeMissing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (mMergeMissing.isSelected()) {
					mMergeList.setEnabled(true);
					mergeCheckAll.setEnabled(true);
					mergeCheckNone.setEnabled(true);
				}
				else {
					mMergeList.setEnabled(false);
					mergeCheckAll.setEnabled(false);
					mergeCheckNone.setEnabled(false);
					mMergeList.clearSelection();
				}
			}
		});

		mReplaceMatching.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (mReplaceMatching.isSelected()) {
					mReplaceList.setEnabled(true);
					replaceCheckAll.setEnabled(true);
					replaceCheckNone.setEnabled(true);
				}
				else {
					mReplaceList.setEnabled(false);
					replaceCheckAll.setEnabled(false);
					replaceCheckNone.setEnabled(false);
					mReplaceList.clearSelection();
				}
			}
		});

		mReplaceList.setEnabled(true);

		// middle panel holds the log as well as related buttons
		JPanel middlePanelHolder = new JPanel();
		middlePanelHolder.setLayout(new BorderLayout(5, 5));
		tb = BorderFactory.createTitledBorder(b.getString("kMergeLog"));
		middlePanelHolder.setBorder(tb);

		mAllLogMessages = new JOAJRadioButton(b.getString("kAllText"), true);
		mJustErrors = new JOAJRadioButton(b.getString("kErrorsOnly"));
		mNoOutput = new JOAJRadioButton(b.getString("kNoOutput"));

		mAllLogMessages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (mAllText != null) {
					mLogArea.setText(new String(mAllText));
				}
			}
		});

		mJustErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (mErrorText != null) {
					mLogArea.setText(new String(mErrorText));
				}
			}
		});

		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(mAllLogMessages);
		bg2.add(mJustErrors);
		bg2.add(mNoOutput);
		JPanel filterPanel = new JPanel(new RowLayout(Orientation.CENTER, Orientation.CENTER, 5));
		filterPanel.add(new JLabel(b.getString("kLogFilter")));
		filterPanel.add(mAllLogMessages);
		filterPanel.add(mJustErrors);
		filterPanel.add(mNoOutput);
		filterPanel.add(new JLabel("   Search:"));
		mSearchFld = new SearchField(this);
		filterPanel.add(mSearchFld);
		middlePanelHolder.add(BorderLayout.NORTH, filterPanel);

		mLogArea = new JTextArea(10, 20);
		mLogArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mLogArea.setLineWrap(true);
		mLogArea.setWrapStyleWord(true);
		mLogArea.setSelectedTextColor(Color.red);
		JScrollPane logScroller = new JScrollPane(mLogArea);
		middlePanelHolder.add(BorderLayout.CENTER, logScroller);

		mClearBtn = new JOAJButton(b.getString("kClearLog"));
		mSaveButton = new JOAJButton(b.getString("kSaveLog"));
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 2, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mClearBtn);
			dlgBtnsPanel.add(mSaveButton);
		}
		else {
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mClearBtn);
		}
		dlgBtnsInset.add(dlgBtnsPanel);
		middlePanelHolder.add(BorderLayout.SOUTH, dlgBtnsInset);
		this.add(BorderLayout.CENTER, middlePanelHolder);

		mClearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mLogArea.setText("");
				mAllText = null;
				mErrorText = null;
			}
		});

		mSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveLog();
			}
		});

		mMergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// do this on a separate thread
				class BasicThread extends Thread {
					String mTitle;

					public BasicThread(String title) {
						mTitle = title;
					}

					public void run() {
						doMerge();
					}
				}

				// Create a thread and run it
				Thread thread = new BasicThread("Merge Thread");
				thread.start();
			}
		});

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

	public void maintainButtons() {
		boolean atLeastOneCriterion = mCriteriaContPanel.isQueryComplete();
		boolean atLeastOneMergeParameter = mMergeMissing.isSelected() && mMergeList.getSelectedIndices().length > 0;
		boolean atLeastOneReplaceParameter = mReplaceMatching.isSelected() && mReplaceList.getSelectedIndices().length > 0;
		boolean destinationSelected = mSectionChooser.getSelectedFileViewer() != null;

		if (atLeastOneCriterion && (atLeastOneMergeParameter || atLeastOneReplaceParameter) && destinationSelected
		    && !mIsInMergeSemaphore) {
			mMergeButton.setEnabled(true);
		}
		else {
			mMergeButton.setEnabled(false);
		}
	}

	public void saveLog() {
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir");
		FileDialog f = new FileDialog(fr, "Export data as:", FileDialog.SAVE);
		f.setDirectory(directory);
		if (mMergeEditor.getFileName() != null) {
			f.setFile(mMergeEditor.getFileName() + ".mergelog");
		}
		else {
			f.setFile("untitled.mergelog");
		}

		Rectangle dBounds = f.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		f.setLocation(x, y);
		f.setVisible(true);

		directory = f.getDirectory();
		f.dispose();
		if (directory != null && f.getFile() != null) {
			File outFile = new File(directory, f.getFile());
			this.exportIt(outFile);

			try {
				JOAConstants.LogFileStream.writeBytes("Exported text of source merge to: " + outFile.getCanonicalPath() + "\n");
				JOAConstants.LogFileStream.flush();
			}
			catch (Exception ex) {
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void exportIt(File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1000000);
			DataOutputStream out = new DataOutputStream(bos);
			out.writeBytes(mLogArea.getText());
			out.flush();
			out.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("An Error occurred exporting the log text");
		}
		finally {
		}
	}

	public void doMerge() {
		FileViewer fv = null;
		mDispPrecisions = mMergeEditor.getDisplayPrecision();

		// get the QC flag convention
		//TODO get QC std from preferences
		int qcStd = JOAConstants.WOCE_QC_STD;//mParent.getSrcQCStandard();

		if (mCreateNewSection.isSelected()) {
			// make a new file viewer
			String newName;
			if (mSectionNameField.getText() != null && mSectionNameField.getText().length() > 0) {
				newName = mSectionNameField.getText();
			}
			else {
				newName = (String) mSectionChooser.getJList().getSelectedValue();
			}
			fv = cloneIt(mFileViewer, newName);
		}
		else {
			// get the destination fileviewer
			fv = mSectionChooser.getSelectedFileViewer();
		}

		mAllText = new StringBuffer();
		mErrorText = new StringBuffer();
		mIsInMergeSemaphore = true;
		mMergeButton.setEnabled(false);
		mClearBtn.setEnabled(false);
		mSaveButton.setEnabled(false);
		JProgressBar progressBar = new JProgressBar(0, 150);
		progressBar.setString("Merging files...");
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);

		JFrame jf = new JFrame();
		jf.getContentPane().setLayout(new FlowLayout());
		jf.getContentPane().add(progressBar);
		Rectangle dBounds = jf.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		jf.setSize(200, 50);
		jf.setLocation(x, y);
		jf.setTitle("Merging files...");
		jf.setVisible(true);

		// get the number of lines from the editor panel
		int numLines = mMergeEditor.getNumSourceLines();

		// get the cols of the matching criteria
		String[] criteria = mCriteriaContPanel.getClauses();
		boolean createdNewParams = false;
		boolean[] critIsSection = new boolean[criteria.length];
		boolean[] critIsStation = new boolean[criteria.length];
		boolean[] critIsCast = new boolean[criteria.length];
		boolean[] critIsSampNo = new boolean[criteria.length];
		boolean[] critIsBottleNbr = new boolean[criteria.length];
		boolean[] critIsBottleSeq = new boolean[criteria.length];
		boolean[] critIsPres = new boolean[criteria.length];
		int[] critCols = new int[criteria.length];

		for (int c = 0; c < criteria.length; c++) {
			String criterion = criteria[c];
			// match criterion is first part of string, matching source column is
			// second
			String[] tokens = criterion.split(":");
			String matchStr = tokens[0];
			int crit = Integer.valueOf(matchStr).intValue();
			String srcCol = tokens[1];
			critCols[c] = Integer.valueOf(srcCol).intValue();
			critIsSection[c] = crit == 0;
			critIsStation[c] = crit == 1;
			critIsCast[c] = crit == 2;
			critIsBottleNbr[c] = crit == 3;
			critIsSampNo[c] = crit == 4;
			critIsBottleSeq[c] = crit == 5;
			critIsPres[c] = crit == 6;
		}

		Object[] params = mMergeList.getSelectedValues();
		Object[] params2 = mReplaceList.getSelectedValues();
		int[] colIndicies = mMergeList.getSelectedIndices();
		int[] colIndicies2 = mReplaceList.getSelectedIndices();
		boolean[] paramAdded = new boolean[params.length];
		int[] displayPrecision = new int[params.length];

		// keep track of whether a parameter has been merged in yet
		for (int i = 0; i < params.length; i++) {
			paramAdded[i] = false;
		}

		boolean optimizedSearch = mOptimizedSearch.isSelected();
		Section foundSection = null;
		Station foundStation = null;
		Bottle foundBottle = null;

		long stTime = System.nanoTime();
		for (int l = 0; l < numLines; l++) {
			Vector<String> line = mMergeEditor.getRow(l);
			if (line == null) {
				logError("Null line encountered at line  " + 1 + " Aborting merge! ", 0);
				break;
			}

			if (line.size() <= 0) {
				logError("Empty line encountered at line  " + 1 + " Line skipped! ", 0);
				continue;
			}

			if (!optimizedSearch) {
				foundSection = null;
				foundStation = null;
			}
			foundBottle = null;

			for (int c = 0; c < criteria.length; c++) {
				// parse the lines for criterion #1: section, stn, or
				// find the value of criteria #1
				String valC1 = ((String) line.elementAt(critCols[c])).trim();
				int cPlusOne = c + 1;

				if (critIsSection[c]) {
					// see if there is a matching section
					logLine("Condition " + cPlusOne + ": Searching using section id " + valC1, 0);
					foundSection = findSection(valC1, fv);
				}

				if (critIsStation[c]) {
					// see if there is a matching station
					logLine("Condition " + cPlusOne + ": Searching using station id " + valC1, 0);
					foundStation = findStation(foundStation, valC1, foundSection, fv);
					if (foundSection == null && foundStation != null) {
						foundSection = findSection(fv, foundStation);
					}
				}

				if (critIsCast[c] && foundStation != null && foundSection != null) {
					// see if there is a matching cast
					logLine("Condition " + cPlusOne + ": Searching using cast number " + valC1, 0);
					int valC1int = new Integer(valC1).intValue();
					foundStation = findCast(valC1int, foundStation, foundSection, fv);
					if (foundSection == null && foundStation != null) {
						foundSection = findSection(fv, foundStation);
					}
				}

				if (critIsSampNo[c] && foundStation != null && foundSection != null) {
					int valC1int = 0;
					try {
						valC1int = new Integer(valC1).intValue();
						logLine("Condition " + cPlusOne + ": Searching using sample #" + valC1int, 0);
						foundBottle = findBottleBySampNo(valC1int, fv, foundSection, foundStation);
						if (foundSection == null && foundStation != null) {
							foundSection = findSection(fv, foundStation);
						}
					}
					catch (Exception ex) {
						logError("Error: Expected numeric data for sample number!" + valC1, 0);
						logError("On Line: " + line, 0);
					}
				}

				if (critIsBottleNbr[c] && foundStation != null && foundSection != null) {
					int valC1int = 0;
					try {
						valC1int = new Integer(valC1).intValue();
						logLine("Condition " + cPlusOne + ": Searching using bottle #" + valC1int, 0);
						foundBottle = findBottle(valC1int, foundStation, fv, foundSection, foundStation);
						if (foundSection == null && foundStation != null) {
							foundSection = findSection(fv, foundStation);
						}
					}
					catch (Exception ex) {
						logError("Error: Expected numeric data for bottle number!" + valC1, 0);
						logError("On Line: " + line, 0);
					}
				}

				if (critIsBottleSeq[c] && foundStation != null && foundSection != null) {
					int valC1int = 0;
					try {
						valC1int = new Integer(valC1).intValue();
						logLine("Condition " + cPlusOne + ": Searching using bottle sequence #" + valC1int, 0);
						foundBottle = findBottleBySeq(valC1int, fv, foundSection, foundStation);
					}
					catch (Exception ex) {
						logError("Error: Expected numeric data for bottle sequence number!" + valC1, 0);
						logError("On Line: " + line, 0);
					}
				}

				if (critIsPres[c] && foundStation != null && foundSection != null) {
					try {
						double valC1dbl = new Double(valC1).doubleValue();
						logLine("Condition " + cPlusOne + ": Searching using pressure = " + valC1dbl, 0);
						foundBottle = findBottleByPres(valC1dbl, fv, foundSection, foundStation);
						if (foundSection == null && foundStation != null) {
							foundSection = findSection(fv, foundStation);
						}
					}
					catch (Exception ex) {
						logError("Error: Expected numeric data for pressure!" + valC1, 0);
						logError("On Line: " + line, 0);
					}
				}
			} // criteria loop

			if (foundSection != null && foundStation != null && foundBottle != null) {
				logLine("Found section =  " + foundSection.getID() + " Found station =  " + foundStation.getStn()
				    + " Found bottle =  " + foundBottle.getBottleNum(), 0);

				// found a match: apply merge rules if needed
				if (mMergeMissing.isSelected()) {
					logLine(">>>>>>Start of Merge Operation", 0);
					// step #1: add any missing parameters to the FileViewer
					if (params.length == 0) {
						logError("Error: No parameters selected!", 0);
						break;
					}

					for (int i = 0; i < params.length; i++) {
						String param = (String) params[i];
						String pUnits = "";

						// isolate units
						if (param.indexOf(":") > 0) {
							// has units
							try {
								String[] tokens = param.split("[:]");
								param = tokens[0];
								pUnits = tokens[1];
							}
							catch (Exception ex) {
								// silent
							}
						}

						String lcName = param.toLowerCase();

						// determine first whether this is a QC Flag
						if (lcName.indexOf("flag") > 0) {
							// test valid convention
							int suffixPos = -99;
							if (qcStd == JOAConstants.WOCE_QC_STD) {
								suffixPos = lcName.indexOf("_flag_w");
							}
							else if (qcStd == JOAConstants.IGOSS_QC_STD) {
								suffixPos = lcName.indexOf("_flag_i");
							}

							if (suffixPos < 0) {
								// malformed qc parameter
								logError("Merge Error: The QC Parameter " + param + " doesn't match WOCE or IGOSS convention.", 1);
								continue;
							}

							// isolate the parameter
							String parentParam = lcName.substring(0, suffixPos);
							int pos = foundSection.getVarPos(parentParam, true);

							if (pos < 0) {
								// no match: try to translate to JOA name
								String joaName = JOAFormulas.paramNameToJOAName(parentParam);
								logLine("Translated parameter name " + parentParam + " to " + joaName, 1);
								pos = foundSection.getVarPos(joaName, false);

								if (pos >= 0) {
									logLine("Parameter exists in destination file", 1);
								}
								else {
									// try WOCE translation
								}
							}

							if (pos < 0) {
								logError("Merge Error: The QC Parameter " + param + " doesn't match an existing parameter.", 1);
								continue;
							}

							// if we get here we can merge the qc value into the parameter at
							// pos
							// really just a replacement
							String val = line.elementAt(colIndicies[i]);
							float dval = JOAConstants.MISSINGVALUE;
							try {
								dval = Float.parseFloat(val);
								//TODO get this from preferences
								if (dval == JOAConstants.WOCEMISSINGVALUE) {//mParent.getSrcMissingValue()) {
									dval = JOAConstants.MISSINGVALUE;
								}
							}
							catch (Exception ex) {
							}
							short oldQCValue = foundBottle.mQualityFlags[pos];
							foundBottle.mQualityFlags[pos] = (short) dval;
							logLine("Changed value of QC for parameter (" + pos + ") " + param + " from " + oldQCValue + " to "
							    + (short) dval, 1);
							foundBottle.setQCValueEdited(pos, true);
						}
						else {
							// not a QC flag
							int pos = foundSection.getVarPos(param, true);

							if (pos < 0) {
								// no match: try to translate to JOA name
								String joaName = JOAFormulas.paramNameToJOAName(param);
								logLine("Translated parameter name " + param + " to " + joaName, 1);
								pos = foundSection.getVarPos(joaName, false);

								if (pos >= 0) {
									logLine("Parameter exists in destination file", 1);
								}
								else {
									// try WOCE translation
								}
							}

							if (pos < 0) {
								// have to add parameter to section and fileviewer
								if (!paramAdded[i]) {
									logLine("Parameter not found in destination, adding parameter to file = " + param, 1);
									logLine("Set units of " + param + " to " + pUnits, 1);

									int newPos = fv.addNewProperty(param, pUnits);
									Parameter tempProp = new Parameter(param, pUnits);
									tempProp.setPlotMin(0);
									tempProp.setPlotMax(10);
									tempProp.setReverseY(false);
									tempProp.setDisplayPrecision(mDispPrecisions[colIndicies[i]]);
									logLine("Setting precision of " + param + " to "  + mDispPrecisions[colIndicies[i]], 1);

									fv.mAllProperties[newPos] = tempProp;
									fv.computePlotBounds();
									paramAdded[i] = true;
									createdNewParams = true;
									logLine("Adding " + param + " to section", 1);
									foundSection.addNewVarToSection(param, "");
								}
							}
						}
					}

					if (mMergeMissing.isSelected() && params.length > 0) {
						for (int i = 0; i < params.length; i++) {
							String param = (String) params[i];
							String pUnits = "";

							// isolate units
							if (param.indexOf(":") > 0) {
								// has units
								try {
									String[] tokens = param.split("[:]");
									param = tokens[0];
									pUnits = tokens[1];
								}
								catch (Exception ex) {
									// silent
								}
							}

							String lcparam = param.toLowerCase();
							boolean isQC = false;
							int pos = foundSection.getVarPos(param, true);

							if (pos < 0) {
								// no match: see if it's a quality flag
								if (lcparam.indexOf("flag") > 0) {
									// try to isolate the variable from a WOCE qc flag variable
									String[] tokens = lcparam.split("[_]");
									if (tokens.length > 0) {
										pos = foundSection.getVarPos(tokens[0], true);
										if (pos >= 0) {
											isQC = true;
										}
									}
								}
							}

							if (pos < 0) {
								// no match: try to translate to JOA name
								String joaName = JOAFormulas.paramNameToJOAName(param);
								pos = foundSection.getVarPos(joaName, false);
							}

							if (pos < 0) {
								logError("Merge Error: Parameter " + param + " did not match a parameter in the destination file.", 1);
								continue;
							}

							if (!paramAdded[i]) {
								continue;
							}

							// get the position of each param in the source
							String val = line.elementAt(colIndicies[i]);
							float dval = JOAConstants.MISSINGVALUE;
							try {
								dval = Float.parseFloat(val);
								//TODO get value form prefs
								if (dval == JOAConstants.WOCEMISSINGVALUE) {//mParent.getSrcMissingValue()) {
									dval = JOAConstants.MISSINGVALUE;
								}
							}
							catch (Exception ex) {
							}

							if (!isQC) {
								foundBottle.mDValues[pos] = dval;
								logLine("Set value of parameter (" + pos + ") " + param + " = " + foundBottle.mDValues[pos], 1);
								foundBottle.setValueEdited(pos, true);
							}
							else {
								// replace a quality flag
								foundBottle.mQualityFlags[pos] = (short) dval;
								logLine("Set value of qc flag (" + pos + ") " + param + " = " + foundBottle.mQualityFlags[pos], 1);
								foundBottle.setQCValueEdited(pos, true);
							}
						}
					}
					logLine(">>>>>>End of Merge Operation", 0);
				}

				// replace rules
				if (mReplaceMatching.isSelected() && params2.length > 0) {
					logLine(">>>>>>Start of Replace Operation", 0);
					// parameter must exist in the destination file
					for (int i = 0; i < params2.length; i++) {
						String param = (String) params2[i];
						String pUnits = "";

						// isolate units
						if (param.indexOf(":") > 0) {
							// has units
							try {
								String[] tokens = param.split("[:]");
								param = tokens[0];
								pUnits = tokens[1];
							}
							catch (Exception ex) {
								// silent
							}
						}

						String lcparam = param.toLowerCase();
						boolean isQC = false;
						int pos = foundSection.getVarPos(param, true);

						if (pos < 0) {
							// no match: see if it's a quality flag
							if (lcparam.indexOf("flag") > 0) {
								// try to isolate the variable from a WOCE qc flag variable
								String[] tokens = lcparam.split("[_]");
								if (tokens.length > 0) {
									pos = foundSection.getVarPos(tokens[0], true);
									if (pos >= 0) {
										isQC = true;
									}
								}
							}
						}

						if (pos < 0) {
							// no match: try to translate to JOA name
							String joaName = JOAFormulas.paramNameToJOAName(param);
							logLine("Translated parameter name " + param + " to " + joaName, 1);
							pos = foundSection.getVarPos(joaName, false);
						}

						if (pos < 0) {
							logError("Replace Error: Parameter " + param + " did not match a parameter in the destination file.", 1);
							continue;
						}
						Parameter masterParam = fv.mAllProperties[pos];
						int masterPrec = 3;

						// compare units
						String masterUnits = masterParam.getUnits();
						if (masterUnits != null && masterUnits.length() > 0) {
							if (pUnits.length() > 0 && !pUnits.equalsIgnoreCase(masterUnits) && !isQC) {
								// units don't match
								int fPos = fv.getPropertyPos(param, false);
								if (fPos >= 0)
									fv.mAllProperties[pos].setUnits(pUnits);
								foundSection.mUnits[pos] = pUnits;
								logLine("Replaced units (" + masterUnits + ") for " + param + " in destination " +  " with new units:" + pUnits, 1);
							}
						}

						if (masterParam != null) {
							masterPrec = masterParam.getDisplayPrecision();
						}

						// get the position of each param in the source
						String val = line.elementAt(colIndicies2[i]);
						float dval = JOAConstants.MISSINGVALUE;
						try {
							dval = Float.parseFloat(val);
							//TODO get values from prefs
							if (dval == JOAConstants.WOCEMISSINGVALUE) {//mParent.getSrcMissingValue()) {
								dval = JOAConstants.MISSINGVALUE;
							}
						}
						catch (Exception ex) {
						}

						if (!isQC) {
							float oldValue = foundBottle.mDValues[pos];
							foundBottle.mDValues[pos] = dval;
							logLine("Changed value of parameter (" + pos + ") " + param + " from " + oldValue + " to " + dval, 1);
							foundBottle.setValueEdited(pos, true);
							if (masterParam != null && mDispPrecisions[colIndicies2[i]] != masterPrec) {
								masterParam.setDisplayPrecision(mDispPrecisions[colIndicies2[i]]);
								logLine("Reset " + param + " precision to = " + mDispPrecisions[colIndicies2[i]], 1);
							}
						}
						else {
							short oldQCValue = foundBottle.mQualityFlags[pos];
							foundBottle.mQualityFlags[pos] = (short) dval;
							logLine("Changed value of QC for parameter (" + pos + ") " + param + " from " + oldQCValue + " to "
							    + (short) dval, 1);
							foundBottle.setQCValueEdited(pos, true);
						}
					}
					logLine(">>>>>>End of Replace Operation", 0);
				}
			}
			else {
				logError("Search Error: Match not found for selected criteria", 0);
				String s1 = foundSection != null ? foundSection.getID() : "Not Found";
				String s2 = foundStation != null ? foundStation.getStn() : "Not Found";
				String s3 = foundBottle != null ? String.valueOf(foundBottle.getBottleNum()) : "Not Found";

				logError("Found section =  " + s1 + " Found station =  " + s2 + " Found bottle =  " + s3, 1);
			}
		}
		long deltaTime = System.nanoTime() - stTime;
		double elapsed = (double) deltaTime / 10e8;
		String text = ("Merge time = " + elapsed + " seconds");
		mLogArea.append(text + "\n");
		mAllText.append(text + "\n");

		if (createdNewParams) {
			fv.getObsPanel().cleanUp();
			// fire a parameter added event
			OpenDataFile oldof = (OpenDataFile) fv.mOpenFiles.currElement();
			Section oldsech = (Section) oldof.mSections.currElement();
			Station oldsh = (Station) oldsech.mStations.currElement();
			ParameterAddedEvent pae = new ParameterAddedEvent(fv);
			pae.setFoundObs(oldsech, oldsh);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pae);
		}

		fv.computePlotBounds();
		fv.forceObsPanelUpdate();

		// done with the progress bar
		jf.setVisible(false);
		jf.dispose();

		mIsInMergeSemaphore = false;
		mMergeButton.setEnabled(true);
		mClearBtn.setEnabled(true);
		mSaveButton.setEnabled(true);
	}

	@SuppressWarnings("unused")
	private int findCol(String crit1, Vector<String> cols) {
		for (int i = 0; i < cols.size(); i++) {
			if (crit1.equalsIgnoreCase(cols.elementAt(i))) { return i; }
		}
		return -99;
	}

	private Section findSection(String inSec, FileViewer fv) {
		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int s = 0; s < of.mNumSections; s++) {
				Section sech = (Section) of.mSections.elementAt(s);

				if (sech.mNumCasts == 0) {
					continue;
				}
				if (sech.getID().equalsIgnoreCase(inSec)) { return sech; }

			}
		}
		return null;
	}

	private Section findSection(FileViewer fv, Station inStn) {
		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int s = 0; s < of.mNumSections; s++) {
				Section sech = (Section) of.mSections.elementAt(s);

				if (sech.mNumCasts == 0) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					if (inStn == sh) { return sech; }
				}
			}
		}
		return null;
	}

	private Station findStationByBottle(FileViewer fv, Bottle inBottle) {
		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int s = 0; s < of.mNumSections; s++) {
				Section sech = (Section) of.mSections.elementAt(s);

				if (sech.mNumCasts == 0) {
					continue;
				}
				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station) sech.mStations.elementAt(stc);
					if (!sh.mUseStn) {
						continue;
					}

					for (int b = 0; b < sh.mNumBottles; b++) {
						Bottle bh = (Bottle) sh.mBottles.elementAt(b);

						boolean keepBottle;
						if (fv.mObsFilterActive) {
							keepBottle = fv.mCurrObsFilter.testObservation(fv, sech, bh);
						}
						else {
							keepBottle = true;
						}

						if (fv.mObsFilterActive && !keepBottle) {
							if (fv.mCurrObsFilter.isShowOnlyMatching()) {
								continue;
							}
						}

						if (bh == inBottle) { return sh; }
					}
				}
			}
		}
		return null;
	}

	private Station findStation(Station startSearchFromStn, String inStn, Section sech, FileViewer fv) {
		String tstStn = inStn.trim();
		if (sech != null) {
			for (int stc = 0; stc < sech.mStations.size(); stc++) {
				Station sh = (Station) sech.mStations.elementAt(stc);
				if (!sh.mUseStn) {
					continue;
				}

				if (sh.getStn().trim().equalsIgnoreCase(tstStn)) { return sh; }
			}
		}
		else if (startSearchFromStn != null && sech != null) {
			sech.mStations.setCurrElement(startSearchFromStn);
			while (startSearchFromStn != null) {
				Station sh = startSearchFromStn;// (Station)
				// sech.mStations.elementAt(stc);
				if (!startSearchFromStn.mUseStn) {
					continue;
				}
				if (startSearchFromStn.getStn().trim().equalsIgnoreCase(tstStn)) { return startSearchFromStn; }
				startSearchFromStn = sech.mStations.nextElement();
			}
		}
		else {
			for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

				for (int s = 0; s < of.mNumSections; s++) {
					Section fondSec = (Section) of.mSections.elementAt(s);

					if (fondSec.mNumCasts == 0) {
						continue;
					}
					for (int stc = 0; stc < fondSec.mStations.size(); stc++) {
						Station sh = (Station) fondSec.mStations.elementAt(stc);
						if (!sh.mUseStn) {
							continue;
						}
						if (sh.getStn().trim().equalsIgnoreCase(tstStn)) { return sh; }
					}
				}
			}
		}
		return null;
	}

	private Station findCast(int inCast, Station startSearchFromStn, Section sech, FileViewer fv) {
		if (startSearchFromStn != null && sech != null) {
			sech.mStations.setCurrElement(startSearchFromStn);
			while (startSearchFromStn != null) {
				Station sh = startSearchFromStn;// (Station)
				// sech.mStations.elementAt(stc);
				if (!startSearchFromStn.mUseStn) {
					continue;
				}
				if (startSearchFromStn.getCast() == inCast) { return startSearchFromStn; }
				startSearchFromStn = sech.mStations.nextElement();
			}
		}
		else {
			for (int stc = 0; stc < sech.mStations.size(); stc++) {
				Station sh = (Station) sech.mStations.elementAt(stc);
				if (!sh.mUseStn) {
					continue;
				}
				if (sh.getStn().equalsIgnoreCase(startSearchFromStn.getStn()) && sh.getCast() == inCast) { return sh; }
			}
		}
		return null;
	}

	private Bottle findBottle(int inBotNum, Station startSearchFromStn, FileViewer fv, Section sech, Station inStn) {
		for (int b = 0; b < inStn.mNumBottles; b++) {
			Bottle bh = (Bottle) inStn.mBottles.elementAt(b);

			boolean keepBottle;
			if (fv.mObsFilterActive) {
				keepBottle = fv.mCurrObsFilter.testObservation(fv, sech, bh);
			}
			else {
				keepBottle = true;
			}

			if (fv.mObsFilterActive && !keepBottle) {
				if (fv.mCurrObsFilter.isShowOnlyMatching()) {
					continue;
				}
			}

			if (bh.isBottleNumUsed() && (bh.getBottleNum() == inBotNum)) { return bh; }
		}
		return null;
	}

	private Bottle findBottleBySampNo(int inSampNum, FileViewer fv, Section sech, Station inStn) {
		for (int b = 0; b < inStn.mNumBottles; b++) {
			Bottle bh = (Bottle) inStn.mBottles.elementAt(b);

			boolean keepBottle;
			if (fv.mObsFilterActive) {
				keepBottle = fv.mCurrObsFilter.testObservation(fv, sech, bh);
			}
			else {
				keepBottle = true;
			}

			if (fv.mObsFilterActive && !keepBottle) {
				if (fv.mCurrObsFilter.isShowOnlyMatching()) {
					continue;
				}
			}

			if (bh.getSampNo() == inSampNum) { return bh; }
		}
		return null;
	}

	private Bottle findBottleBySeq(int inSeqNum, FileViewer fv, Section inSec, Station inStn) {
		for (int b = 0; b < inStn.mNumBottles; b++) {
			Bottle bh = (Bottle) inStn.mBottles.elementAt(b);

			boolean keepBottle;
			if (fv.mObsFilterActive) {
				keepBottle = fv.mCurrObsFilter.testObservation(fv, inSec, bh);
			}
			else {
				keepBottle = true;
			}

			if (fv.mObsFilterActive && !keepBottle) {
				if (fv.mCurrObsFilter.isShowOnlyMatching()) {
					continue;
				}
			}

			// sequence number is from deepest bottle
			if ((inStn.getNumBottles() - bh.mOrdinal) == inSeqNum) {
				int pos = inSec.getPRESVarPos();
				logLine("Matched seqnum " + inSeqNum + " pressure @ match = " + bh.mDValues[pos], 0);
				return bh;
			}
		}
		return null;
	}

	private Bottle findBottleByPres(double inPres, FileViewer fv, Section inSec, Station inStn) {
		// get the position of the PRES variable in this section
		int pPos = inSec.getPRESVarPos();

		for (int b = 0; b < inStn.mNumBottles; b++) {
			Bottle bh = (Bottle) inStn.mBottles.elementAt(b);

			boolean keepBottle;
			if (fv.mObsFilterActive) {
				keepBottle = fv.mCurrObsFilter.testObservation(fv, inSec, bh);
			}
			else {
				keepBottle = true;
			}

			if (fv.mObsFilterActive && !keepBottle) {
				if (fv.mCurrObsFilter.isShowOnlyMatching()) {
					continue;
				}
			}

			if (bh.mDValues[pPos] > inPres - 0.01 && bh.mDValues[pPos] < inPres + 0.01) {
				logLine("Matched pressure " + inPres, 0);
				return bh;
			}
		}

		return null;
	}

	private void logLine(String text, int indent) {
		if (mAllLogMessages.isSelected()) {
			for (int i = 0; i < indent; i++) {
				mLogArea.append("  ");
			}
			mAllText.append("  ");

			mLogArea.append(text + "\n");
			mAllText.append("\n");
		}
	}

	private void logError(String text, int indent) {
		if (mJustErrors.isSelected()) {
			for (int i = 0; i < indent; i++) {
				mLogArea.append("  ");
				mErrorText.append("  ");
			}

			mLogArea.append(text + "\n");
			mErrorText.append(text + "\n");
			mAllText.append(text + "\n");
		}
	}

	public void valueChanged(ListSelectionEvent ev) {
		mSectionNameField.setText((String) (mSectionChooser.getJList().getSelectedValue()) + " (merged)");
	}

	public void setColumns(Vector<String> cols) {
		mColheaders = cols;
	}

	public void setColumns(String[] cols) {
		mColheaders = new Vector<String>();
		for (int i = 0; i < cols.length; i++) {
			mColheaders.addElement(cols[i]);
		}
	}

	public void buildCriteriaCombos() {
		Vector<String> listData = new Vector<String>();
		for (int i = 0; i < mColheaders.size(); i++) {
			listData.addElement(mColheaders.elementAt(i));
		}

		// update the combo boxes in any criterion panels
		mCriteriaContPanel.setParams(listData);
	}

	public void buildParamLists() {
		Vector<String> listData = new Vector<String>();
		for (int i = 0; i < mColheaders.size(); i++) {
			listData.addElement(mColheaders.elementAt(i));
		}

		if (mMergeList == null) {
			mMergeList = new JOAJList(listData);
			mMergeList.setPrototypeCellValue("SALT                                ");
			mMergeList.setVisibleRowCount(10);
		}
		else {
			mMergeList.setListData(listData);
			mMergeList.invalidate();
		}

		if (mReplaceList == null) {
			mReplaceList = new JOAJList(listData);
			mReplaceList.setPrototypeCellValue("SALT                              ");
			mReplaceList.setVisibleRowCount(10);
		}
		else {
			mReplaceList.setListData(listData);
			mReplaceList.invalidate();
		}
	}

	public void startSearch(String srcString) {
		srchStart = 0;
		String txt = mLogArea.getText();
		int st = txt.indexOf(srcString, srchStart);
		if (st >= 0) {
			mLogArea.setCaretPosition(st);
			mLogArea.moveCaretPosition(st + srcString.length());
			srchStart = st;
		}
	}

	public void searchAgain(String srcString) {
		String txt = mLogArea.getText();
		int st = txt.indexOf(srcString, srchStart + 1);
		if (st >= 0) {
			mLogArea.setCaretPosition(st);
			mLogArea.moveCaretPosition(st + srcString.length());
			srchStart = st;
		}
		else {
			srchStart = 0;
		}
	}

	public void endSearch() {
	}

	public void scroll() {
		mCriteriaScroller.scrollRectToVisible(new Rectangle(1000, 1000, 1000, 1000));
	}

	private class MyScroller extends JScrollPane {
		public MyScroller(Component c) {
			super(c);
			this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}

		public Dimension getPreferredSize() {
			return new Dimension(390, 150);
		}
	}

	public FileViewer cloneIt(FileViewer fv, String newName) {
		Vector<SectionStation> mFoundStns = new Vector<SectionStation>();
		FileViewer ff = null;
		try {
			JOAConstants.LogFileStream.writeBytes(fv.getTitle() + ": Cloned");
			JOAConstants.LogFileStream.writeBytes("\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section) of.mSections.elementAt(sec);
				if (sech.mNumCasts == 0) {
					continue;
				}

				Vector<Bottle> combinedBottles = new Vector<Bottle>(100);
				int stc = 0;
				while (stc < sech.mStations.size()) {
					combinedBottles.removeAllElements();
					Station currStn = (Station) sech.mStations.elementAt(stc++);
					// add currStn Bottles
					for (int csb = 0; csb < currStn.mNumBottles; csb++) {
						Bottle bh = (Bottle) currStn.mBottles.elementAt(csb);
						combinedBottles.addElement(new Bottle(bh));
					}

					// assign new ordinals to bottles
					int ord = 0;
					for (int i = 0; i < combinedBottles.size(); i++) {
						Bottle bh = combinedBottles.elementAt(i);
						bh.mOrdinal = ++ord;
					}

					// make a new station for combined casts;
					Station newsh;
					newsh = new Station(currStn);
					if (currStn.getType() == null) {
						newsh.setType("BOTTLE");
					}
					else {
						newsh.setType(currStn.getType());
					}

					// add the sorted bottles to the station
					for (int i = 0; i < combinedBottles.size(); i++) {
						Bottle bh = (Bottle) combinedBottles.elementAt(i);
						newsh.mBottles.addElement(bh);
					}

					SectionStation secStn = new SectionStation(newsh, sech, ++ord);
					mFoundStns.addElement(secStn);
				}
			}

			// make a new file viewer
			Frame fr = new Frame();
			ff = new FileViewer(fr, mFoundStns, newName, fv, false);
			PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
			ff.pack();
			ff.setVisible(true);
			ff.requestFocus();
			ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
		}

		// issue an event to cause all windows to update
		ff.updateAfterSectionManager();
		return ff;
	}
}

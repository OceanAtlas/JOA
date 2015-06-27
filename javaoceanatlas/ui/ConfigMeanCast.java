/*
 * $Id: ConfigMeanCast.java,v 1.7 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class ConfigMeanCast extends JOAJDialog implements ActionListener, ItemListener, ListSelectionListener,
    ButtonMaintainer {
	protected FileViewer mFileViewer = null;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJComboBox mSurfaces = null;
	protected JOAJTextField mSectionNameField = null;
	private JOAJLabel mNameLbl = null;
	protected SectionChooser mSectionChooser;
	protected JOAJCheckBox mIncludeStatistics = null;
	private Timer timer = new Timer();
	protected int mSelFV = -99;

	public ConfigMeanCast(JFrame par, FileViewer fv) {
		super(par, "Configure Mean Cast", false);
		mFileViewer = fv;

		// init the interface
		init();

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

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel(); // everything goes in here
		mainPanel.setLayout(new BorderLayout(5, 5));

		JPanel mUpperContents = new JPanel();
		mUpperContents.setLayout(new BorderLayout(5, 5));

		// upper panel contains the section choosers
		JPanel mUpperRow = new JPanel();
		mUpperRow.setLayout(new RowLayout(javaoceanatlas.utility.Orientation.LEFT,
		    javaoceanatlas.utility.Orientation.CENTER, 5));

		mSectionChooser = new SectionChooser(mFileViewer, b.getString("kSection"), this, "XXXXXXXXXXXXXXXXXX");
		mSectionChooser.init();
		mUpperRow.add(mSectionChooser);

		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new ColumnLayout(javaoceanatlas.utility.Orientation.LEFT,
		    javaoceanatlas.utility.Orientation.TOP, 5));
		mIncludeStatistics = new JOAJCheckBox(b.getString("kIncludeStatistics"), false);
		actionsPanel.add(mIncludeStatistics);

		// choosing the interpolation surface
		JPanel surfPanel = new JPanel();
		surfPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JOAJLabel surfLbl = new JOAJLabel(b.getString("kInterpolationSurface"));
		surfPanel.add(surfLbl);
		String[] params = { "pres", "sig" };
		Vector<String> presetData = JOAFormulas.getFilteredSurfaceList(params);
		mSurfaces = new JOAJComboBox(presetData);
		surfPanel.add(mSurfaces);
		actionsPanel.add(surfPanel);
		mUpperRow.add(actionsPanel);

		mUpperContents.add("North", mUpperRow);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
		mNameLbl = new JOAJLabel(b.getString("kNewSectionName"));
		namePanel.add(mNameLbl);
		mSectionNameField = new JOAJTextField(20);
		mSectionNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		namePanel.add(mSectionNameField);

		// finally the name for new FVs
		mUpperContents.add("South", namePanel);

		// mReverseStations.addItemListener(this);

		mainPanel.add("Center", new TenPixelBorder(mUpperContents, 0, 5, 0, 5));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
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

		// add all the sub panels to main panel
		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		this.setLocation(x, y);
	}

	public void maintainButtons() {
		// maintain the buttons of the subpanel UIs
		if (mSelFV >= 0) {
			mOKBtn.setEnabled(true);
		}
		else {
			mOKBtn.setEnabled(false);
		}
	}

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mSectionChooser.getJList()) {
			// get the param
			mSelFV = mSectionChooser.getJList().getSelectedIndex();
			if (mSectionChooser.getSelectedFileViewer() != null) {
				mSectionNameField.setText(mSectionChooser.getSelectedFileViewer().getTitle() + " (mean)");
			}
			else {
				mSectionNameField.setText("");
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			createMeanCast(mSectionChooser.getSelectedFileViewer());
			timer.cancel();
			this.dispose();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJRadioButton) {
		}
		else if (evt.getSource() instanceof JOAJCheckBox) {
		}
	}

	protected void enableName(boolean flag) {
		mSectionNameField.setEnabled(flag);
		mSectionNameField.invalidate();
		mNameLbl.setEnabled(flag);
		mNameLbl.invalidate();
	}

	public double[][] doInterpDown(FileViewer fv, NewInterpolationSurface interpIn, int mInterpParamNum,
	    int mSurfaceParamNum) {
		// initialize some constants
		double physHigh, physLow, interpLow, interpHigh, delta;
		int newSign, oldSign;
		int c1Pos = 0, c2Pos = 0, c3Pos = 0, c4Pos = 0;
		boolean doIt = false;
		double c1Val = 0.0, c2Val = 0.0, c3Val = 0.0, c4Val = 0.0;

		int stnCnt = -1;

		int mNumLevels = interpIn.getNumLevels();
		double[] mSurfaceValues = interpIn.getValues();
		int MINUS = -1;
		int PLUS = +1;
		int ZERO = 0;
		boolean mFarBottle = false;
		int mNumBottles = 2;

		double[] levelSum = new double[mNumLevels];
		double[] levelSumSqd = new double[mNumLevels];
		double[] levelCount = new double[mNumLevels];

		for (int iz = 0; iz < mNumLevels; iz++) {
			levelSum[iz] = 0;
			levelSumSqd[iz] = 0;
			levelCount[iz] = 0;
		}

		double[][] resultArray = new double[mNumLevels][3];

		// interpolate
		// loop over each level
		for (int iz = 0; iz < mNumLevels; iz++) {
			// count up actual stations used
			stnCnt = -1;

			// loop over the files
			for (int fc = 0; fc < fv.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile) fv.mOpenFiles.elementAt(fc);

				// loop over the sections
				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section) of.mSections.elementAt(sec);
					if (sech.mNumCasts == 0) {
						continue;
					}

					int iPos = sech.getVarPos(fv.mAllProperties[mInterpParamNum].getVarLabel(), false);
					int sPos = sech.getVarPos(fv.mAllProperties[mSurfaceParamNum].getVarLabel(), false);

					if (iPos < 0 || sPos < 0) {
						continue;
					}

					if (fv.mObsFilterActive) {
						if (fv.mCurrObsFilter.isCriteria1Active()) {
							c1Pos = sech.getVarPos(fv.mAllProperties[fv.mCurrObsFilter.getParamIndex(0)].getVarLabel(), false);
						}
						if (fv.mCurrObsFilter.isCriteria2Active()) {
							c2Pos = sech.getVarPos(fv.mAllProperties[fv.mCurrObsFilter.getParamIndex(1)].getVarLabel(), false);
						}
						if (fv.mCurrObsFilter.isCriteria3Active()) {
							c3Pos = sech.getVarPos(fv.mAllProperties[fv.mCurrObsFilter.getParamIndex(2)].getVarLabel(), false);
						}
						if (fv.mCurrObsFilter.isCriteria4Active()) {
							c4Pos = sech.getVarPos(fv.mAllProperties[fv.mCurrObsFilter.getParamIndex(3)].getVarLabel(), false);
						}
					}

					// loop over the stations
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station) sech.mStations.elementAt(stc);
						if (sh.mNumBottles == 0) {
							continue;
						}
						if (sh.mUseStn) {
							stnCnt++;
							// initialize the sign variable from first bottle
							Bottle bh = (Bottle) sh.mBottles.elementAt(0);
							physHigh = bh.mDValues[sPos];
							delta = physHigh - mSurfaceValues[iz];
							oldSign = 0;
							if (delta > 0) {
								oldSign = PLUS;
							}
							else if (delta < 0) {
								oldSign = MINUS;
							}
							else {
								oldSign = ZERO;
							}
							doIt = false;

							// loop over the bottles
							for (int b = 0; b < sh.mNumBottles; b++) {
								bh = (Bottle) sh.mBottles.elementAt(b);
								double val = bh.mDValues[sPos];
								if (val != JOAConstants.MISSINGVALUE) {
									physHigh = val;
									doIt = true;
								}
								else {
								} // else

								if (doIt) {
									delta = physHigh - mSurfaceValues[iz];
									if (delta > 0) {
										newSign = PLUS;
									}
									else if (delta < 0) {
										newSign = MINUS;
									}
									else {
										newSign = ZERO;
									}

									if (newSign == ZERO) {
										// bottle and level are the same
										if (bh.mDValues[iPos] != JOAConstants.MISSINGVALUE) {
											if (fv.mObsFilterActive) {
												if (fv.mCurrObsFilter.isCriteria1Active()) {
													c1Val = bh.mDValues[c1Pos];
												}
												if (fv.mCurrObsFilter.isCriteria2Active()) {
													c2Val = bh.mDValues[c2Pos];
												}
												if (fv.mCurrObsFilter.isCriteria3Active()) {
													c3Val = bh.mDValues[c3Pos];
												}
												if (fv.mCurrObsFilter.isCriteria4Active()) {
													c4Val = bh.mDValues[c4Pos];
												}

												if (fv.mCurrObsFilter.testValues(c1Val, c2Val, c3Val, c4Val, bh, c1Pos, c2Pos, c3Pos, c4Pos)) {
													levelSum[iz] += bh.mDValues[iPos];
													levelSumSqd[iz] += bh.mDValues[iPos] * bh.mDValues[iPos];
													levelCount[iz]++;
												}
											}
											else {
												levelSum[iz] += bh.mDValues[iPos];
												levelSumSqd[iz] += bh.mDValues[iPos] * bh.mDValues[iPos];
												levelCount[iz]++;
											}
											break;
										} // if bh
									} // if newSign
									else if (oldSign != newSign && newSign != ZERO && bh.mOrdinal > 0) {
										// sign changes
										int bb = b - 1;
										Bottle prevBh = null;
										int farBottleMax;
										if (mFarBottle) {
											farBottleMax = mNumBottles;
										}
										else {
											farBottleMax = 1;
										}

										int fbc = 0;
										do {
											// search for a non missing value above current bottle
											prevBh = (Bottle) sh.mBottles.elementAt(bb--);
											fbc++;
										} while (fbc < farBottleMax && prevBh.mDValues[iPos] == JOAConstants.MISSINGVALUE && bb >= 0);

										if (bh.mDValues[iPos] != JOAConstants.MISSINGVALUE
										    && prevBh.mDValues[iPos] != JOAConstants.MISSINGVALUE) {
											physLow = prevBh.mDValues[sPos];
											interpLow = bh.mDValues[iPos];
											interpHigh = prevBh.mDValues[iPos];
											double iVal = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;

											// test whether the interpolated values for the obs filter
											// meet test
											if (fv.mObsFilterActive) {
												if (fv.mCurrObsFilter.isCriteria1Active()) {
													interpLow = bh.mDValues[c1Pos];
													interpHigh = prevBh.mDValues[c1Pos];
													c1Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
												}
												if (fv.mCurrObsFilter.isCriteria2Active()) {
													interpLow = bh.mDValues[c2Pos];
													interpHigh = prevBh.mDValues[c2Pos];
													c2Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
												}
												if (fv.mCurrObsFilter.isCriteria3Active()) {
													interpLow = bh.mDValues[c3Pos];
													interpHigh = prevBh.mDValues[c3Pos];
													c3Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
												}
												if (fv.mCurrObsFilter.isCriteria4Active()) {
													interpLow = bh.mDValues[c4Pos];
													interpHigh = prevBh.mDValues[c4Pos];
													c4Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
												}
												if ((fv.mCurrObsFilter.testValues(c1Val, c2Val, c3Val, c4Val, bh, c1Pos, c2Pos, c3Pos, c4Pos))
												    && (fv.mCurrObsFilter.testValues(c1Val, c2Val, c3Val, c4Val, prevBh, c1Pos, c2Pos, c3Pos,
												        c4Pos))) {
													levelSum[iz] += iVal;
													levelSumSqd[iz] += iVal * iVal;
													levelCount[iz]++;
												}
											}
											else {
												levelSum[iz] += iVal;
												levelSumSqd[iz] += iVal * iVal;
												levelCount[iz]++;
											}
										}
										else {
											// first look for a non missing value at i -2
											if (bh.mOrdinal > 1) {
												Bottle prevPrevBh = (Bottle) sh.mBottles.elementAt(b - 1);
												if (bh.mDValues[iPos] != JOAConstants.MISSINGVALUE
												    && prevPrevBh.mDValues[iPos] != JOAConstants.MISSINGVALUE) {
													physLow = prevPrevBh.mDValues[sPos];
													interpLow = bh.mDValues[iPos];
													interpHigh = prevPrevBh.mDValues[iPos];
													double iVal = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;

													// test whether the interpolated values for the obs
													// filter meet test
													if (fv.mObsFilterActive) {
														if (fv.mCurrObsFilter.isCriteria1Active()) {
															interpLow = bh.mDValues[c1Pos];
															interpHigh = prevPrevBh.mDValues[c1Pos];
															c1Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
														}
														if (fv.mCurrObsFilter.isCriteria2Active()) {
															interpLow = bh.mDValues[c2Pos];
															interpHigh = prevPrevBh.mDValues[c2Pos];
															c2Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
														}
														if (fv.mCurrObsFilter.isCriteria3Active()) {
															interpLow = bh.mDValues[c3Pos];
															interpHigh = prevPrevBh.mDValues[c3Pos];
															c3Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
														}
														if (fv.mCurrObsFilter.isCriteria4Active()) {
															interpLow = bh.mDValues[c4Pos];
															interpHigh = prevPrevBh.mDValues[c4Pos];
															c4Val = (delta / (physHigh - physLow)) * (interpHigh - interpLow) + interpLow;
														}
														if ((fv.mCurrObsFilter.testValues(c1Val, c2Val, c3Val, c4Val, bh, c1Pos, c2Pos, c3Pos,
														    c4Pos))
														    && (fv.mCurrObsFilter.testValues(c1Val, c2Val, c3Val, c4Val, prevBh, c1Pos, c2Pos,
														        c3Pos, c4Pos))) {
															levelSum[iz] += iVal;
															levelSumSqd[iz] += iVal * iVal;
															levelCount[iz]++;
														}
													}
													else {
														levelSum[iz] += iVal;
														levelSumSqd[iz] += iVal * iVal;
														levelCount[iz]++;
													}
												}
											}
										}
										break;
									}
								} // if doIT
							} // for b
						} // if useStn
					} // for stc
				} // for sec
			} // for fc
		} // for iz

		for (int iz = 0; iz < mNumLevels; iz++) {
			resultArray[iz][0] = levelSum[iz];
			resultArray[iz][1] = levelSumSqd[iz];
			resultArray[iz][2] = levelCount[iz];
		}

		return resultArray;
	}

	public void createMeanCast(FileViewer fv) {
		// first create a vector to hold the mean cast
		Vector<SectionStation> mFoundStns = new Vector<SectionStation>();
		boolean includeStats = mIncludeStatistics.isSelected();

		// get the surface
		NewInterpolationSurface surf = null;
		try {
			surf = JOAFormulas.getSurface((String) mSurfaces.getSelectedItem());
		}
		catch (Exception ex) {
			JFrame f = new JFrame("Surface Error");
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(f, "An error occurred attempting to open the specified surface." + "\n"
			    + "Select another surface for this calculation.");
			return;
		}

		int numVars = fv.gNumProperties;
		if (includeStats) {
			numVars = numVars + numVars * 3;
		}

		// Make a new section
		Section sec = new Section(0, "Mean Cast", "none", 1, numVars);

		for (int i = 0; i < fv.gNumProperties; i++) {
			sec.addNewVarToSection(fv.mAllProperties[i].getVarLabel(), fv.mAllProperties[i].getUnits());
		}

		if (includeStats) {
			// add space for the statistics
			for (int i = 1; i < fv.gNumProperties; i++) {
				sec.addNewVarToSection(fv.mAllProperties[i].getVarLabel() + "_n", fv.mAllProperties[i].getUnits());
				sec.addNewVarToSection(fv.mAllProperties[i].getVarLabel() + "_var", fv.mAllProperties[i].getUnits());
			}
		}

		// make a new stn for the mean cast
		Station newsh = new Station(0, "None", "1", 1, JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE, surf
		    .getNumLevels(), JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE,
		    JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE, JOAConstants.MISSINGVALUE,
		    "Computed Mean Cast", "");
		newsh.setType("INTERP");

		// create the blank bottles for the mean cast
		double[] mSurfaceValues = surf.getValues();
		for (int iz = 0; iz < surf.getNumLevels(); iz++) {
			Bottle bh = new Bottle(iz, numVars, newsh, sec);
			bh.mDValues[0] = (float) mSurfaceValues[iz];

			// add the pressure variable
			newsh.mBottles.addElement(bh);
		}

		// add to collector vector
		mFoundStns = new Vector<SectionStation>();
		mFoundStns.addElement(new SectionStation(newsh, sec, 0));

		int mSurfaceParamNum = fv.getPropertyPos(surf.getParam(), false);
		double[][] mResult;

		// interpolate a parameter at a time onto the interp grid for all stations
		for (int i = 1; i < fv.gNumProperties; i++) {
			int mInterpParamNum = i;

			// return an array of sums, sumsqd, and n's for the ith parameter
			mResult = doInterpDown(fv, surf, mInterpParamNum, mSurfaceParamNum);
			for (int iz = 0; iz < surf.getNumLevels(); iz++) {
				Bottle bh = (Bottle) newsh.mBottles.elementAt(iz);

				// compute the mean
				if (mResult[iz][2] > 0) {
					bh.mDValues[i] = (float) mResult[iz][0] / (float) mResult[iz][2];
					if (includeStats) {
						bh.mDValues[((i - 1) * 2) + fv.gNumProperties] = (float) mResult[iz][2];
						if (mResult[iz][2] > 1) {
							double var = (mResult[iz][1] - ((mResult[iz][0] * mResult[iz][0]) / mResult[iz][2]))
							    / (mResult[iz][2] - 1);
							bh.mDValues[((i - 1) * 2) + fv.gNumProperties + 1] = (float) var;
						}
						else {
							bh.mDValues[((i - 1) * 2) + fv.gNumProperties + 1] = (float) JOAConstants.MISSINGVALUE;
						}
					}
				}
				else {
					bh.mDValues[i] = JOAConstants.MISSINGVALUE;
					if (includeStats) {
						bh.mDValues[((i - 1) * 2) + fv.gNumProperties] = (float) JOAConstants.MISSINGVALUE;
						bh.mDValues[((i - 1) * 2) + fv.gNumProperties + 1] = (float) JOAConstants.MISSINGVALUE;
					}
				}
			}
		}

		try {
			JOAConstants.LogFileStream.writeBytes(fv.getTitle() + ": computed mean cast" + ", vertical grid =  "
			    + surf.getDescrip());

			JOAConstants.LogFileStream.writeBytes("\n");
			JOAConstants.LogFileStream.flush();
		}
		catch (Exception ex) {
		}

		// make a new file viewer
		Frame fr = new Frame();
		FileViewer ff = new FileViewer(fr, mFoundStns, mSectionNameField.getText(), fv, true);
		PowerOceanAtlas.getInstance().addOpenFileViewer(ff);
		ff.pack();
		ff.setVisible(true);
		ff.requestFocus();
		ff.setSavedState(JOAConstants.CREATEDONTHEFLY, null);
	}
}

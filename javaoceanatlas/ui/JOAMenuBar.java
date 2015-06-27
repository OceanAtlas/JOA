/*
 * $Id: JOAMenuBar.java,v 1.23 2005/10/18 23:42:19 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.io.*;
import javaoceanatlas.PowerOceanAtlas;

public class JOAMenuBar implements JOAMenuBarHolder {
	private static final Object ACCELERATOR_KEY = null;
	Component mThis;
	JMenuItem open;
	JMenuItem openDapper;
	JMenuItem openDODS;
	JMenuItem concat;
	JMenuItem sectionManager;
	JMenuItem selectAll;
	JMenuItem save;
	JMenuItem saveAs;
	JMenuItem close;
	JMenuItem print;
	JMenuItem saveWorkSpaceAs;
	JMenuItem merge;
	JMenuItem prefs;
	JMenuItem help;
	JMenuItem calcSections;
	JMenuItem colorPalEdit;
	JMenuItem contourManager;
	JMenuItem surfaceEdit;
	JMenuItem colorMapEdit;
	JMenuItem stnfilter;
	JMenuItem obsfilter;
	JMenuItem fileProperties;
	JMenuItem parameterProperties;
	JMenuItem recodeValues;
	JMenuItem maintainDatabase;
	boolean mEnablePrinting;
	boolean mEnableSelectAll;
	JMenu mSwingWindowsMenu;
	FileViewer mFV;
	JMenu file = new JMenu();
	JMenu edit = new JMenu();
	JMenu plots = new JMenu();
	JMenu calcs = new JMenu();
	JMenu color = new JMenu();
//	JMenu helpm = new JMenu();
	JMenu calcStation = new JMenu();
	JMenu filters = new JMenu();
	JMenu calcSectionsm = new JMenu();
	JMenuItem exportStnCalcsAction = null;
	JMenuItem stnPropAction = null;
	JMenuItem stnPropPropAction = null;
	JMenuItem calcWSAction;
	JMenuItem calcCustomStnAction = null;
	public static ImageIcon checked = new ImageIcon(javaoceanatlas.PowerOceanAtlas.class.getResource("images/metal_check.gif"));
	public static ImageIcon diamond = new ImageIcon(javaoceanatlas.PowerOceanAtlas.class.getResource("images/metal_hidden_diamond.png"));
	public static ImageIcon blank = new ImageIcon(javaoceanatlas.PowerOceanAtlas.class.getResource("images/blank_icon_image.png"));

	public JOAMenuBar(Component c, boolean enablePrinting, boolean enableSelectAll, FileViewer fv) {
		mThis = c;
		mEnablePrinting = enablePrinting;
		mEnableSelectAll = enableSelectAll;
		mFV = fv;

		try {
			buildJMenuBar();
			addJListeners();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public JOAMenuBar(Component c, boolean enablePrinting, FileViewer fv) {
		this(c, enablePrinting, false, fv);
	}

	public void buildJMenuBar() {
		final ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		JMenuBar menubar = new JMenuBar();
		menubar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		((JFrame) mThis).setJMenuBar(menubar);

		// create the menus
		file = new JMenu(b.getString("kFile"));
		boolean fileEnabled = JOAFormulas.testFeatureGroup("kFile");
		edit = new JMenu(b.getString("kEditMenu"));
		boolean editEnabled = JOAFormulas.testFeatureGroup("kEditMenu");
		plots = new JMenu(b.getString("kPlots"));
		boolean plotsEnabled = JOAFormulas.testFeatureGroup("kPlots");
		calcs = new JMenu(b.getString("kCalculations"));
		boolean calcsEnabled = JOAFormulas.testFeatureGroup("kCalculations");
		color = new JMenu(b.getString("kResources"));
		boolean resourcesEnabled = JOAFormulas.testFeatureGroup("kResources");
//		helpm = new JMenu(b.getString("kHelp2"));
		filters = new JMenu(b.getString("kFilters"));
		boolean filtersEnabled = JOAFormulas.testFeatureGroup("kFilters");
		
		calcStation = new JMenu(b.getString("kStationCalculation"));
		calcSectionsm = new JMenu(b.getString("kSectionCalculations"));
		mSwingWindowsMenu = new JMenu(b.getString("kWindows"));
		
		// attach a listener
		mSwingWindowsMenu.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				// check front window
				for (JOAViewer viewer: PowerOceanAtlas.getInstance().getOpenFileViewers()) {
					Vector<JOAWindow> windowList = ((FileViewer)viewer).getOpenWindowList();
					for (JOAWindow theWin : windowList) {
						for (int i=0; i<mSwingWindowsMenu.getItemCount(); i++) {
	  				JMenuItem jcb = mSwingWindowsMenu.getItem(i);
	  				if (jcb.getText().indexOf(theWin.getTitle()) >= 0) {
						if (theWin.isVisible() && theWin.isFocused()) {
	  					jcb.setIcon(JOAMenuBar.checked);
						}
					  else if (!theWin.isVisible() || theWin.getState() == JFrame.ICONIFIED) {
	  					jcb.setIcon(JOAMenuBar.diamond);
						}
						else {
	  					jcb.setIcon(JOAMenuBar.blank);
						}
	  				}
					}
					}
				}
			}

      public void menuDeselected(MenuEvent evt) {

      }

      public void menuCanceled(MenuEvent evt) {

      }
    });	

		// add file menu items
		open = new JMenuItem(b.getString("kOpen"));
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
		    false));
		open.setActionCommand("open");
		openDODS = new JMenuItem(b.getString("kOpenDODS"));
		openDODS.setActionCommand("opendods");
		concat = new JMenuItem(b.getString("kAddData"));
		concat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
		    + java.awt.event.InputEvent.ALT_MASK, false));
		concat.setActionCommand("concat");
		save = new JMenuItem(b.getString("kSave"));
		save.setActionCommand("save");
		saveAs = new JMenuItem(b.getString("kSaveAs"));
		saveAs.setActionCommand("saveas");
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
		    false));
		merge = new JMenuItem(b.getString("kMerge"));
		merge.setActionCommand("merge");

		close = new JMenuItem(b.getString("kClose"));
		close.setActionCommand("close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
		    false));

		ExportSSAction exportSSAction = new ExportSSAction(mFV, b.getString("kExportSpreadsheet"));
		ExportNetCDFSectionAction exportNetCDFAction = new ExportNetCDFSectionAction(mFV, b.getString("kExportNetCDF"));
		WriteEpicPtrsAction exportEPICPtrAction = new WriteEpicPtrsAction(mFV, b.getString("kExportEPICPtr"));
		ExportWOCEExchangeAction exportExchangeAction = new ExportWOCEExchangeAction(mFV, b.getString("kWOCEExchange"));
		exportStnCalcsAction = new JMenuItem(b.getString("kExportStnCalcs"));
		exportStnCalcsAction.setActionCommand("exportstncalc");	

		exportStnCalcsAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConfigureStnCalcExport expDialog = new ConfigureStnCalcExport(mFV, mFV);
				expDialog.pack();
			
				// show dialog at expDialog of screen
				Rectangle dBounds = expDialog.getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width/2 - dBounds.width/2;
				int y = sd.height/2 - dBounds.height/2;
				expDialog.setLocation(x, y);
				expDialog.setVisible(true);
			}
		});
		
		
		PageSetupAction pageSetupAction = new PageSetupAction(b.getString("kPageSetup"));

		if (mEnablePrinting) {
			print = new JMenuItem(b.getString("kPrint"));
			print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
			    false));
			print.setActionCommand("print");
		}

		if (mFV != null)
			file.add(open);

		if (mFV != null) {
			OpenDapperAction openDapperAction = new OpenDapperAction(mFV, b.getString("kOpenDapper"));
			if (JOAFormulas.testFeature("kOpenDapper"))
				file.add(openDapperAction);
		}
		// file.add(openDODS);
		if (mFV != null)
			file.add(concat);
		file.add(close);
		file.addSeparator();
		
		
		if (mFV != null) {
			if (JOAFormulas.testFeature("kExportSpreadsheet"))
				file.add(exportSSAction);
			if (JOAFormulas.testFeature("kExportNetCDF"))
				file.add(exportNetCDFAction);
			if (JOAFormulas.testFeature("kExportEPICPtr"))
				file.add(exportEPICPtrAction);
			if (JOAFormulas.testFeature("kWOCEExchange"))
				file.add(exportExchangeAction);
			if (JOAFormulas.testFeature("kExportStnCalcs"))
				file.add(exportStnCalcsAction);
			file.add(saveAs);
			file.addSeparator();
			if (JOAFormulas.testFeature("kMerge")) {
				file.add(merge);
				file.addSeparator();
			}
		}
		file.add(pageSetupAction);
		if (mEnablePrinting) {
			file.add(print);
		}

		if (!JOAConstants.ISMACOSX) {
			QuitJOAAction quitAction = new QuitJOAAction(b.getString("kExit"), KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit
			    .getDefaultToolkit().getMenuShortcutKeyMask(), false));
			file.addSeparator();
			file.add(quitAction);
		}

		file.addMenuListener(new MyMenuListener());

		// edit menu;
		sectionManager = new JMenuItem(b.getString("kSectionManager"));
		sectionManager.setActionCommand("sectionmanager");
		fileProperties = new JMenuItem(b.getString("kFileProperties"));
		fileProperties.setActionCommand("fileproperties");
		parameterProperties = new JMenuItem(b.getString("kParamProperties"));
		parameterProperties.setActionCommand("paramproperties");

		if (mEnableSelectAll) {
			selectAll = new JMenuItem(b.getString("kSelectAll"));
			selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit()
			    .getMenuShortcutKeyMask(), false));
			selectAll.setActionCommand("selectall");
			selectAll.addActionListener((ActionListener) mThis);
		}

		final EditPlotorDataAction editPlotAction = new EditPlotorDataAction(mThis, mFV, b.getString("kEditPlot"),
		    KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		prefs = new JMenuItem(b.getString("kPreferences"));

//		final MaintainDatabaseAction maintainDBAction = new MaintainDatabaseAction(mThis, b.getString("kMaintainDatabase"),
//		    null);

		if (mFV != null) {
			if (JOAFormulas.testFeature("kSectionManager")) {
				edit.add(sectionManager);
				edit.addSeparator();
			}
			
			if (JOAFormulas.testFeature("kFileProperties"))
				edit.add(fileProperties);
			
			if (JOAFormulas.testFeature("kParamProperties"))
			edit.add(parameterProperties);
			
			if (mEnableSelectAll) {
				edit.addSeparator();
				edit.add(selectAll);
			}
			
//			if (JOAFormulas.testFeature("kEditPlot") || JOAFormulas.testFeature("kMaintainDatabase"))
			if (JOAFormulas.testFeature("kEditPlot")) {
				edit.addSeparator();
				edit.add(editPlotAction);
			}
//			if (JOAFormulas.testFeature("kMaintainDatabase"))
//				edit.add(maintainDBAction);
		}

		if (!JOAConstants.ISMAC) {
			if (mFV != null) {
				edit.addSeparator();
			}
			prefs.setActionCommand("prefs");
			edit.add(prefs);
		}

		edit.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				// get the frontmost window type and change the menu text
				if (mFV != null) {
					editPlotAction.putValue(Action.NAME, editPlotAction.getText());
				}
			}

			public void menuDeselected(MenuEvent evt) {

			}

			public void menuCanceled(MenuEvent evt) {

			}
		});

		// add plots menu items
		PvPPlotAction propPropAction = new PvPPlotAction((JFrame) mThis, mFV, b.getString("kPropertyProperty"), KeyStroke
		    .getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kPropertyProperty"))
			plots.add(propPropAction);
		
		LinePlotAction linePlotAction = new LinePlotAction((JFrame) mThis, mFV, b.getString("kLinePlot"), KeyStroke
		    .getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kLinePlot"))
			plots.add(linePlotAction);

		ProfilePlotAction profileAction = new ProfilePlotAction((JFrame) mThis, mFV, b.getString("kProfile"), KeyStroke
		    .getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kProfile"))
			plots.add(profileAction);

		MapPlotAction mapAction = new MapPlotAction((JFrame) mThis, mFV, b.getString("kMap"), KeyStroke.getKeyStroke(
		    KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kMap"))
			plots.add(mapAction);

		ContourPlotAction contourAction = new ContourPlotAction((JFrame) mThis, mFV, b.getString("kContour"), KeyStroke
		    .getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kContour"))
			plots.add(contourAction);

		ContourGradientAction contourGradAction = new ContourGradientAction((JFrame) mThis, mFV, b
		    .getString("kContourGradient"), KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit()
		    .getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kContourGradient"))
			plots.add(contourGradAction);

		stnPropAction = new JMenuItem(b.getString("kStnPlot"), null);
		stnPropPropAction = new JMenuItem(b.getString("kStnXYPlot"), null);
		
		if (mFV != null && JOAFormulas.testFeature("kStnPlot")) {			
			stnPropAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
			    // Open the station value plot dialog
			    ConfigureStationValuePlot stnPDialog = new ConfigureStationValuePlot(mFV, mFV);
			    stnPDialog.pack();
			    stnPDialog.setVisible(true);
				}
			});
			plots.add(stnPropAction);

			if (mFV != null && JOAFormulas.testFeature("kStnXYPlot")) {			
			stnPropPropAction.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
				    // Open the station value plot dialog
						ConfigureStationValueXYPlot stnPDialog = new ConfigureStationValueXYPlot(mFV, mFV);
				    stnPDialog.pack();
				    stnPDialog.setVisible(true);
					}
				});
				plots.add(stnPropPropAction);
			}

			plots.addMenuListener(new MyMenuListener2());
		}

		// filters menu
		stnfilter = new JMenuItem(b.getString("kStationFilter"));
		stnfilter.setActionCommand("stnfilter");
		if (JOAFormulas.testFeature("kStationFilter"))
			filters.add(stnfilter);
		obsfilter = new JMenuItem(b.getString("kObservationFilter"));
		obsfilter.setActionCommand("obsfilter");
		if (JOAFormulas.testFeature("kObservationFilter"))
			filters.add(obsfilter);

		// add calculations menu items
		CalcParamsAction calcParamsAction = new CalcParamsAction((JFrame) mThis, mFV, b.getString("kParameters"), KeyStroke
		    .getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kParameters"))
			calcs.add(calcParamsAction);

		CustomCalcAction calcCustomAction = new CustomCalcAction((JFrame) mThis, mFV, b.getString("kCustomParameter"), null);
		if (JOAFormulas.testFeature("kCustomParameter"))
			calcs.add(calcCustomAction);

		ForecastTSAction forecastAction = new ForecastTSAction((JFrame) mThis, mFV, b.getString("kForecastTS"), null);
		if (JOAFormulas.testFeature("kModelTSRelationship"))
			calcs.add(forecastAction);

		// add the submenus
		boolean b1, b2, b3, b4, b5, b6, b7;
		CalcMLDAction calcMLDAction = new CalcMLDAction((JFrame) mThis, mFV, b.getString("kCalcMLD"), null);
		if (b1 = JOAFormulas.testFeature("kCalcMLD"))
			calcStation.add(calcMLDAction);

		CalcInterpolationAction calcInterpAction = new CalcInterpolationAction((JFrame) mThis, mFV, b
		    .getString("kCalcInterp"), null);
		if (b2 = JOAFormulas.testFeature("kCalcInterp"))
			calcStation.add(calcInterpAction);

		CalcIntegrationAction calcIntegAction = new CalcIntegrationAction((JFrame) mThis, mFV, b.getString("kCalcInteg"),
		    null);
		if (b3 = JOAFormulas.testFeature("kCalcInteg"))
			calcStation.add(calcIntegAction);

		CalcNeutralSurfaceAction calcNSAction = new CalcNeutralSurfaceAction((JFrame) mThis, mFV, b.getString("kCalcNS"),
		    null);
		if (b4 = JOAFormulas.testFeature("kCalcNS"))
			calcStation.add(calcNSAction);

		CalcExtremaAction calcExtremaAction = new CalcExtremaAction((JFrame) mThis, mFV, b.getString("kCalcExtrema"), null);
		if (b5 = JOAFormulas.testFeature("kCalcExtrema"))
			calcStation.add(calcExtremaAction);

		CalcStnStatsAction calcStnStatsAction = new CalcStnStatsAction((JFrame) mThis, mFV, b.getString("kCalcStnStats"),
		    null);
		if (b6 = JOAFormulas.testFeature("kCalcStnStats"))
			calcStation.add(calcStnStatsAction);

		calcWSAction = new JMenuItem(b.getString("kCalcWindSpeed"), null);
		if (b7 = JOAFormulas.testFeature("kCalcWindSpeed")) {
			calcWSAction.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
				    // Open the calculations dialog
				    ConfigUVCalculation customDialog = new ConfigUVCalculation(mFV, mFV);
				    customDialog.pack();
				    customDialog.setVisible(true);
					}
				});
			calcStation.add(calcWSAction);
		}

		if (b1 || b2 || b3 || b4 || b5 || b6 || b7)
			calcs.add(calcStation);

		calcCustomStnAction = new JMenuItem(b.getString("kCustomStnParameter"), null);;
		if (JOAFormulas.testFeature("kCustomStnParameter")) {

			calcCustomStnAction.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
				    // Open the calculations dialog
						ConfigCustomStnCalc customDialog = new ConfigCustomStnCalc(mFV, mFV);
						customDialog.pack();
						customDialog.setVisible(true);
					}
				});
			calcs.add(calcCustomStnAction);
		}

		CalcTransformationAction calcTransformationAction = new CalcTransformationAction((JFrame) mThis, mFV, b
		    .getString("kParameterTransformations"), null);
		if (JOAFormulas.testFeature("kParameterTransformations"))
			calcs.add(calcTransformationAction);

		final CalcSectionDifferenceAction calcSectionDiffAction = new CalcSectionDifferenceAction((JFrame) mThis, mFV, b
		    .getString("kSectionDifference"), null);
		if (b1 = JOAFormulas.testFeature("kSectionDifference"))
			calcSectionsm.add(calcSectionDiffAction);

		CalcMeanCastAction calcSectionMeanAction = new CalcMeanCastAction((JFrame) mThis, mFV, b.getString("kMeanCast3"),
		    null);
		if (b2 = JOAFormulas.testFeature("kMeanCast3"))
			calcSectionsm.add(calcSectionMeanAction);

		if (b1 || b2) {
			calcs.add(calcSectionsm);
		}

		calcs.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				calcSectionDiffAction.setEnabled(PowerOceanAtlas.getInstance().getNumOpenFileViewer() >= 2);
				calcCustomStnAction.setEnabled(mFV.isStnCalcPresent());
			}

			public void menuDeselected(MenuEvent evt) {

			}

			public void menuCanceled(MenuEvent evt) {

			}
		});

		calcStation.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent evt) {
				if (mFV != null) {
					calcWSAction.setEnabled(mFV.isStnCalcPresent());
				}
			}

			public void menuDeselected(MenuEvent evt) {

			}

			public void menuCanceled(MenuEvent evt) {

			}
		});

		RecodeValuesAction recodeAction = new RecodeValuesAction((JFrame) mThis, mFV, b.getString("kRecodeValues"), null);
		if (JOAFormulas.testFeature("kRecodeValues"))
			calcs.add(recodeAction);

		// add resource menu items
		contourManager = new JMenuItem(b.getString("kContourManager"));
		contourManager.setActionCommand("contourManager");
		contourManager.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit()
		    .getMenuShortcutKeyMask(), false));
		colorPalEdit = new JMenuItem(b.getString("kColorPaletteEditor"));
		colorPalEdit.setActionCommand("colorPalEdit");
		surfaceEdit = new JMenuItem(b.getString("kSurfaceManager"));
		surfaceEdit.setActionCommand("surfaceEdit");
		// colorMapEdit = new JMenuItem("Color Map Editor...");
		// colorMapEdit.setActionCommand("colormapEdit");
		// colorPalEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		if (JOAFormulas.testFeature("kContourManager"))
			color.add(contourManager);
		if (JOAFormulas.testFeature("kColorPaletteEditor"))
			color.add(colorPalEdit);
		if (JOAFormulas.testFeature("kSurfaceManager"))
			color.add(surfaceEdit);
		// color.add(colorMapEdit);

		// add help menu items
//		if (!JOAConstants.ISMAC) {
//			AboutJOAAction aboutAction = new AboutJOAAction(mFV, b.getString("kAbout"));
//			helpm.add(aboutAction);
//		}

		//JOANewsAction newsAction = new JOANewsAction(mFV, b.getString("kJOANews"));
		//helpm.add(newsAction);

		// add to the menubar
		if (fileEnabled)
			menubar.add(file);
		if (mFV != null) {
			if (editEnabled)
			menubar.add(edit);
			if (plotsEnabled)
				menubar.add(plots);
			if (calcsEnabled)
				menubar.add(calcs);
			if (filtersEnabled)
				menubar.add(filters);
			if (resourcesEnabled)
				menubar.add(color);
		}
		menubar.add(mSwingWindowsMenu);
//		menubar.add(helpm);

		// toolbar toggle
		if (((JOAWindow) mThis).getToolbar() != null) {
			// JMenu tttb = new JMenu();
			// //Action tba = new ToggleToolbarAction("", new
			// ImageIcon(getClass().getResource("images/arrowcursor.gif")),
			// (JOAWindow)mThis, true);
			// tttb.add(tba);
			// JMenu toggle = new JMenu(tba);
			// //menubar.add(new JButton(tba));//new
			// ImageIcon(getClass().getResource("images/arrowcursor.gif"))));
		}
		// menubar.setHelpMenu(help);
	}

	public JMenu getSwingWindowsMenu() {
		return mSwingWindowsMenu;
	}

	public void addJListeners() {
		// add the listeners
		open.addActionListener((ActionListener) mThis);
		openDODS.addActionListener((ActionListener) mThis);
		concat.addActionListener((ActionListener) mThis);
		sectionManager.addActionListener((ActionListener) mThis);
		save.addActionListener((ActionListener) mThis);
		saveAs.addActionListener((ActionListener) mThis);
		close.addActionListener((ActionListener) mThis);
		merge.addActionListener((ActionListener) mThis);
		// saveWorkSpaceAs.addActionListener((ActionListener)mThis);
		if (mEnablePrinting) {
			print.addActionListener((ActionListener) mThis);
		}
		prefs.addActionListener((ActionListener) mThis);
		// help.addActionListener((ActionListener)mThis);
		colorPalEdit.addActionListener((ActionListener) mThis);
		contourManager.addActionListener((ActionListener) mThis);
		surfaceEdit.addActionListener((ActionListener) mThis);
		// colorMapEdit.addActionListener((ActionListener)mThis);
		stnfilter.addActionListener((ActionListener) mThis);
		obsfilter.addActionListener((ActionListener) mThis);
		fileProperties.addActionListener((ActionListener) mThis);
		parameterProperties.addActionListener((ActionListener) mThis);
		exportStnCalcsAction.addActionListener((ActionListener) mThis);
		stnPropAction.addActionListener((ActionListener) mThis);
	}

	public JMenu getEditMenu() {
		return file;
	}

	private class MyMenuListener implements MenuListener {
		public void menuSelected(MenuEvent evt) {
			if (mFV != null) {
				exportStnCalcsAction.setEnabled(mFV.isStnCalcPresent());
			}
		}

		public void menuDeselected(MenuEvent evt) {

		}

		public void menuCanceled(MenuEvent evt) {

		}
	}

	private class MyMenuListener2 implements MenuListener {
		public void menuSelected(MenuEvent evt) {
			if (mFV != null) {
				stnPropAction.setEnabled(mFV.isStnCalcPresent());
				stnPropPropAction.setEnabled(mFV.isStnCalcPresent());
			}
		}

		public void menuDeselected(MenuEvent evt) {

		}

		public void menuCanceled(MenuEvent evt) {

		}
	}
}

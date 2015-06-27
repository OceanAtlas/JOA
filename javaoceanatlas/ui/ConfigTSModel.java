/**
 * 
 */
package javaoceanatlas.ui;

/*
 * $Id: ConfigCustomCalc.java,v 1.6 2005/09/07 18:49:30 oz Exp $
 *
 */

import gov.noaa.pmel.swing.NPixelBorder;
import gov.noaa.pmel.util.GeoDate;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.ibm.xml.parser.TXDocument;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.DateFilterPanel;
import net.sourceforge.openforecast.Forecaster;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.IntegerFilterPanel;
import net.sourceforge.openforecast.MonthFilterPanel;
import net.sourceforge.openforecast.NumericFilterPanel;
import net.sourceforge.openforecast.Observation;
import net.sourceforge.openforecast.SeasonFilterPanel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.*;
import java.util.Timer;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.XYPlotSpecification;

@SuppressWarnings("serial")
public class ConfigTSModel extends JOAJDialog implements ActionListener, ButtonMaintainer, DocumentListener,
ListSelectionListener {
	protected FileViewer mFileViewer;
	protected JOAJButton mComputeModelBtn = null;
	protected JOAJButton mPlotModelBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJButton mSaveButton = null;
	protected JTextArea mResultsField = null;
	protected JTextArea mCommentField = null;
	protected JOAJCheckBox mTemp = null;
	protected JOAJCheckBox mSqTemp = null;
	protected JOAJCheckBox mLat = null;
	protected JOAJCheckBox mLon = null;
	protected JOAJCheckBox mLonxLat = null;
	protected JOAJCheckBox mRespectJOAFilters = null;
	private Timer timer = new Timer();
	private DateFilterPanel dfp;
	private MonthFilterPanel mfp;
	private SeasonFilterPanel sfp;
	private IntegerFilterPanel ydfp;
	private NumericFilterPanel zfp;
	private NumericFilterPanel tfp;
	private ForecastingModel mFCM;
	private Vector<ForecastingModel> mAllFCMs = new Vector<ForecastingModel>();
	private javax.swing.JButton clearLogButton = new javax.swing.JButton();
	private JPanel mFilteredTSPlotPanel = new JPanel(new BorderLayout(5, 5));
	private TSModelChooser mTSModelChooser;
	private JLabel mCurrModelPreview;
	private JLabel mCurrModelErrTerms;
	double mTMinOfData = 1000.0;
	double mTMaxOfData = -1000.0;

	public ConfigTSModel(JFrame par, FileViewer fv) {
		super(par, "Model TS Relationship", false);
		mFileViewer = fv;
		this.init();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		// create the two parameter chooser lists
		this.getContentPane().setLayout(new BorderLayout(5, 5));

		TitledBorder tb = BorderFactory.createTitledBorder("TS Plot");
		mFilteredTSPlotPanel.setBorder(tb);
		mFilteredTSPlotPanel.add(new JLabel("                                                         "));

		// upper Panels
		JPanel allUpperContents = new JPanel(new BorderLayout(0, 0));
		JPanel leftContents = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 5));
		JPanel rightContents = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 5));
		JPanel varFilterPanel = new JPanel(new BorderLayout(0, 0));

		// Create a split pane with the two scroll panes in it.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContents, rightContents);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(620);

		// Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(600, 325);
		leftContents.setPreferredSize(minimumSize);
		rightContents.setPreferredSize(minimumSize);
		
		allUpperContents.add(BorderLayout.CENTER, splitPane);
		this.getContentPane().add(BorderLayout.CENTER, new NPixelBorder(allUpperContents, 5, 5, 5, 5));

		// Independent Variables
		mTemp = new JOAJCheckBox(b.getString("kTemperature"), true);
		mSqTemp = new JOAJCheckBox(b.getString("kSquaredTemperature"), false);
		mLat = new JOAJCheckBox(b.getString("kLatitude"), false);
		mLon = new JOAJCheckBox(b.getString("kLongitude"), false);
		mLonxLat = new JOAJCheckBox(b.getString("kLonxLat"), false);

		JPanel ivarCont = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
		ivarCont.add(mTemp);
		ivarCont.add(mSqTemp);
		ivarCont.add(mLat);
		ivarCont.add(mLon);
		ivarCont.add(mLonxLat);

		tb = BorderFactory.createTitledBorder(b.getString("kIndependentVariables"));
		ivarCont.setBorder(tb);

		varFilterPanel.add(BorderLayout.NORTH, ivarCont);

		// filters go here
		JPanel timeFilterCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		JPanel ztFilterCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

		tb = BorderFactory.createTitledBorder("Station-Level Filters (Applied First)");
		timeFilterCont.setBorder(tb);

		tb = BorderFactory.createTitledBorder("Observation-Level Filters (Applied Second)");
		ztFilterCont.setBorder(tb);

		zfp = new NumericFilterPanel("Depth range:", "Depth", -999, -999, false);
		tfp = new NumericFilterPanel("Temperature range:", "Temperature", -999, -999, false);
		dfp = new DateFilterPanel("Date", new GeoDate(), new GeoDate(), false);
		mfp = new MonthFilterPanel(1, 12, false);
		sfp = new SeasonFilterPanel(Seasons.WINTER, false);
		ydfp = new IntegerFilterPanel("Year day range:", "Year day", "yeardayfilter", 1, 365, false);

		zfp.setEnabled(false);
		tfp.setEnabled(false);
		dfp.setEnabled(false);
		mfp.setEnabled(false);
		sfp.setEnabled(false);
		ydfp.setEnabled(false);

		ztFilterCont.add(zfp);
		ztFilterCont.add(tfp);
		timeFilterCont.add(dfp);
		timeFilterCont.add(mfp);
		timeFilterCont.add(sfp);
		timeFilterCont.add(ydfp);

		varFilterPanel.add(BorderLayout.CENTER, timeFilterCont);
		varFilterPanel.add(BorderLayout.SOUTH, ztFilterCont);

		leftContents.add(new NPixelBorder(varFilterPanel, 5, 5, 5, 5));
		mComputeModelBtn = new JOAJButton(b.getString("kComputeModel"));
		mComputeModelBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mComputeModelBtn);
		
		JPanel dlgBtnsPanel1 = new JPanel(new BorderLayout(0, 0));
		dlgBtnsPanel1.add(BorderLayout.CENTER, mComputeModelBtn);
		leftContents.add(dlgBtnsPanel1);
		
		// now build the ModelChooser list
		mTSModelChooser = new TSModelChooser(null, "Computed Models", this, "This is a really megalong but a really really really long model String prototype");
		mCurrModelPreview = new JLabel(" ");
		mCurrModelErrTerms= new JLabel(" ");
		mCurrModelPreview.setFont(new java.awt.Font("Dialog", 0, 11));
		mCurrModelErrTerms.setFont(new java.awt.Font("Dialog", 0, 11));
		
		JPanel lowerTopStuffHolder = new JPanel(new GridLayout(1, 2, 10, 0));
		lowerTopStuffHolder.add(mTSModelChooser);

		// comments
		JPanel commentsCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
		commentsCont.add(new JOAJLabel(b.getString("kModelDescrip") + ":"));
		mCommentField = new JTextArea(6, 50);
		mCommentField.setFont(new java.awt.Font("Dialog", 0, 11));

    JScrollPane fldScroller = new JScrollPane(mCommentField);
    fldScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
		commentsCont.add(fldScroller);
		mCommentField.setText("No Description");
		lowerTopStuffHolder.add(commentsCont);
		
		JPanel modelResultsHolder = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.CENTER, 5));
		modelResultsHolder.add(lowerTopStuffHolder);
		modelResultsHolder.add(mCurrModelPreview);
		modelResultsHolder.add(mCurrModelErrTerms);
		
		mPlotModelBtn = new JOAJButton("Plot Model");
		mPlotModelBtn.setActionCommand("plot");
		mSaveButton = new JOAJButton(b.getString("kSaveModel"));

		clearLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mResultsField.setText("");
			}
		});

		mSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				save("untitled_tsmodel.xml");
			}
		});

		JPanel dlgBtnsInset3 = new JPanel();
		JPanel dlgBtnsPanel3 = new JPanel();
		dlgBtnsInset3.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel3.setLayout(new GridLayout(1, 3, 15, 1));
		dlgBtnsPanel3.add(mPlotModelBtn);
		dlgBtnsPanel3.add(mSaveButton);
		dlgBtnsInset3.add(dlgBtnsPanel3);	
		modelResultsHolder.add(dlgBtnsInset3);	
		allUpperContents.add(BorderLayout.SOUTH, new NPixelBorder(modelResultsHolder, 5, 5, 5, 5));
				
		// results
		JPanel resultsCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
		resultsCont.add(new JOAJLabel("Log:"));
		mResultsField = new JTextArea(20, 60);
		mResultsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mResultsField.setLineWrap(true);
		mResultsField.setWrapStyleWord(true);
		mResultsField.setFont(new java.awt.Font("Dialog", 0, 10));
		JScrollPane logScroller = new JScrollPane(mResultsField);
		resultsCont.add(logScroller);

		clearLogButton.setText("Clear Log");
		JPanel dlgBtnsInset2 = new JPanel();
		JPanel dlgBtnsPanel2 = new JPanel();
		dlgBtnsInset2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel2.setLayout(new GridLayout(1, 3, 15, 1));
		dlgBtnsPanel2.add(new JLabel("         "));
		dlgBtnsPanel2.add(clearLogButton);
		dlgBtnsPanel2.add(new JLabel("         "));
		dlgBtnsInset2.add(dlgBtnsPanel2);

		rightContents.add(resultsCont);
		rightContents.add(dlgBtnsPanel2);

		// buttons
		// lower panel
		mCancelButton = new JOAJButton("Done");
		mCancelButton.setActionCommand("cancel");

		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
		dlgBtnsPanel.add(mCancelButton);
		dlgBtnsInset.add(dlgBtnsPanel);

		mComputeModelBtn.addActionListener(this);
		mPlotModelBtn.addActionListener(this);
		mCancelButton.addActionListener(this);

		this.getContentPane().add(BorderLayout.SOUTH, new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5));
		this.pack();

		runTimer();

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
			// collect up the time filters
			Vector<JOATimeFilter> timeFilters = new Vector<JOATimeFilter>();
			NumericFilter zFilter = null;
			NumericFilter tFilter = null;

			if (zfp.isSelected()) {
				zFilter = zfp.getFilter();
			}

			if (tfp.isSelected()) {
				tFilter = tfp.getFilter();
			}

			if (dfp.isSelected()) {
				timeFilters.add(dfp.getFilter());
			}

			if (mfp.isSelected()) {
				timeFilters.add(mfp.getFilter());
			}

			if (sfp.isSelected()) {
				timeFilters.add(sfp.getFilter());
			}

			if (ydfp.isSelected()) {
				timeFilters.add(ydfp.getFilter());
			}

			DataSet newDS = new DataSet();
			newDS.setTimeVariable(null);
			mTMinOfData = 1000.0;
			mTMaxOfData = -1000.0;

			// add the Observations
			for (int fc = 0; fc < mFileViewer.mNumOpenFiles; fc++) {
				OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.elementAt(fc);

				for (int sec = 0; sec < of.mNumSections; sec++) {
					Section sech = (Section) of.mSections.elementAt(sec);

					if (sech.mNumCasts == 0) {
						continue;
					}
					for (int stc = 0; stc < sech.mStations.size(); stc++) {
						Station sh = (Station) sech.mStations.elementAt(stc);
						if (!sh.mUseStn || sh.isSkipStn()) {
							continue;
						}

						int pPos = sech.getPRESVarPos();
						int tPos = sech.getVarPos("TEMP", true);

						if (tPos < 0 || pPos < 0) {
							continue;
						}

						// Apply Time Filters
						boolean exit = false;
						if (timeFilters.size() > 0) {
							for (JOATimeFilter jtFilter : timeFilters) {
								if (!jtFilter.test(sh)) {
									exit = true;
									break;
								}
							}
						}

						if (exit) {
							continue;
						}

						for (int b = 0; b < sh.mNumBottles; b++) {
							Bottle bh = (Bottle) sh.mBottles.elementAt(b);

							// Apply the Observation Filters
							boolean c1 = true;
							if (zFilter != null) {
								c1 = zFilter.test(bh, pPos);
							}

							boolean c2 = true;
							if (c1 && tFilter != null) {
								c2 = tFilter.test(bh, tPos);
							}

							if (!c1 || !c2) {
								continue;
							}

							if (!Double.isNaN(bh.getDependentValue())) {
								mTMinOfData = bh.mDValues[tPos] < mTMinOfData ? bh.mDValues[tPos] : mTMinOfData;
								mTMaxOfData = bh.mDValues[tPos] > mTMaxOfData ? bh.mDValues[tPos] : mTMaxOfData;
								
								Observation obs = new Observation(bh.getDependentValue());

								// add the selected Independent Variables
								if (mTemp.isSelected()) {
									if (Double.isNaN(bh.getIndependentValue(TSModelTermParameter.TEMPERATURE))) {
										break;
									}
									obs.setIndependentValue(TSModelTermParameter.TEMPERATURE.toString(), bh
									    .getIndependentValue(TSModelTermParameter.TEMPERATURE));
								}

								if (mSqTemp.isSelected()) {
									if (Double.isNaN(bh.getIndependentValue(TSModelTermParameter.SQTEMPERATURE))) {
										break;
									}
									obs.setIndependentValue(TSModelTermParameter.SQTEMPERATURE.toString(), bh
									    .getIndependentValue(TSModelTermParameter.SQTEMPERATURE));
								}

								if (mLat.isSelected()) {
									if (Double.isNaN(bh.getIndependentValue(TSModelTermParameter.LATITUDE))) {
										break;
									}
									obs.setIndependentValue(TSModelTermParameter.LATITUDE.toString(), bh
									    .getIndependentValue(TSModelTermParameter.LATITUDE));
								}

								if (mLon.isSelected()) {
									if (Double.isNaN(bh.getIndependentValue(TSModelTermParameter.LONGITUDE))) {
										break;
									}
									obs.setIndependentValue(TSModelTermParameter.LONGITUDE.toString(), bh
									    .getIndependentValue(TSModelTermParameter.LONGITUDE));
								}

								if (mLonxLat.isSelected()) {
									if (Double.isNaN(bh.getIndependentValue(TSModelTermParameter.LONxLAT))) {
										break;
									}
									obs.setIndependentValue(TSModelTermParameter.LONxLAT.toString(), bh
									    .getIndependentValue(TSModelTermParameter.LONxLAT));
								}
								newDS.add(obs);
							}
						}
					}
				}
			}

			try {
				mFCM = null;
				mAllFCMs.clear();
				mFCM = Forecaster.getBestForecast(newDS, mAllFCMs);
				mResultsField.append(Forecaster.getOutput() + System.getProperty("line.separator"));
				mResultsField.append("Final Results:" + System.getProperty("line.separator"));
				mResultsField.append(mFCM.getForecastType() + System.getProperty("line.separator"));
				mResultsField.append(mFCM.toString() + System.getProperty("line.separator"));
				mResultsField.append("Bias =" + mFCM.getBias() + System.getProperty("line.separator"));
				mResultsField.append("Mean Absolute Deviation =" + mFCM.getMAD() + System.getProperty("line.separator"));
				mResultsField.append("Mean Square of Errors =" + mFCM.getMSE() + System.getProperty("line.separator"));
				mResultsField
				    .append("Mean Absolute Precentage Error =" + mFCM.getMAPE() + System.getProperty("line.separator"));
				mResultsField
		    .append("Sum of the Bbsolute Errors =" + mFCM.getSAE() + System.getProperty("line.separator"));
				// set the models list
				mTSModelChooser.setModels(mAllFCMs);
				mCurrModelPreview.setText("");
				mCurrModelErrTerms.setText("");
			}
			catch (Exception ex) {
				ex.printStackTrace();
				mResultsField.append("Forecast Failed--check time filters" + System.getProperty("line.separator"));
				return;
			}
		}
		else if (cmd.equals("plot")) {
	  	ForecastingModel selModel = mTSModelChooser.getSelectedModel();
			Vector<JOATimeFilter> timeFilters = new Vector<JOATimeFilter>();
			NumericFilter zFilter = null;
			NumericFilter tFilter = null;

			if (zfp.isSelected()) {
				zFilter = zfp.getFilter();
			}

			if (tfp.isSelected()) {
				tFilter = tfp.getFilter();
			}

			if (dfp.isSelected()) {
				timeFilters.add(dfp.getFilter());
			}

			if (mfp.isSelected()) {
				timeFilters.add(mfp.getFilter());
			}

			if (sfp.isSelected()) {
				timeFilters.add(sfp.getFilter());
			}

			if (ydfp.isSelected()) {
				timeFilters.add(ydfp.getFilter());
			}

			if (zfp.isSelected()) {
				zFilter = zfp.getFilter();
			}

			if (tfp.isSelected()) {
				tFilter = tfp.getFilter();
			}
			
			// create the model String for plot title
			String mPreviewString = "SALT = " + String.valueOf(selModel.getIntercept());
			for (TSModelTerm tsmt : selModel.getModelTerms()) {
				mPreviewString += tsmt.toHTML();
			}
			
			String winTitle = selModel.getForecastType();
			boolean opened = false;
			
			// make the filter string
			if (zfp.isSelected()) {
				zFilter = zfp.getFilter();
				if (!opened) {
					winTitle += " (";
					opened = true;
				}
				winTitle += zFilter.toLblString("z");
			}
			
			if (tfp.isSelected()) {			
				if (opened) {
					winTitle += ", ";
				}
				tFilter = tfp.getFilter();
				if (!opened) {
					winTitle += " (";
					opened = true;
				}
				winTitle += tFilter.toLblString("T");
			}
			
			if (opened) {
				winTitle += ")";
			}

			// create the plot
			// 1: make an XY Plot Spec
			XYPlotSpecification xyPlotSpec = createPlotSpec(winTitle, mPreviewString);
			xyPlotSpec.setTMinOfModel(mTMinOfData);
			xyPlotSpec.setTMaxOfModel(mTMaxOfData);

			// 2: Add ZT filters
			if (zfp.isSelected()) {
				zFilter = zfp.getFilter();
				xyPlotSpec.setZFilter(zFilter);
			}

			if (tfp.isSelected()) {
				tFilter = tfp.getFilter();
				xyPlotSpec.setTFilter(tFilter);
			}

			// 3: Add any Time Filters
			xyPlotSpec.setTimeFilters(timeFilters);

			// 3: Add the model terms
			xyPlotSpec.setTSModelTerms(selModel.getModelTerms());
			xyPlotSpec.setModelIntercept(selModel.getIntercept());

			// 4: create the plot window
			JOAXYPlotWindow plotWind = new JOAXYPlotWindow(xyPlotSpec, mFileViewer);
			plotWind.pack();
			int w = (int)(1.5 * plotWind.getWidth());
			plotWind.setSize(w, plotWind.getHeight());
			plotWind.setVisible(true);
			mFileViewer.addOpenWindow(plotWind);
		}
	}

	public void maintainButtons() {
  	int selModel = mTSModelChooser.getJList().getSelectedIndex();

  	if (selModel >= 0) {
  		mPlotModelBtn.setEnabled(true);
  		mSaveButton.setEnabled(true);
  	}
  	else {
  		mPlotModelBtn.setEnabled(false);
  		mSaveButton.setEnabled(false);
  	}
	}

	public void changedUpdate(DocumentEvent evt) {
	}

	public void insertUpdate(DocumentEvent evt) {
	}

	public void removeUpdate(DocumentEvent evt) {
	}

	public void save(String suggestedMapName) {
		// get a filename
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("_tsmodel.xml")) {
					return true;
				}
				else {
					return false;
				}
			}
		};
		Frame fr = new Frame();
		String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
		FileDialog f = new FileDialog(fr, "Save TS Model settings with name ending in \"_tsmodel.xml\"", FileDialog.SAVE);
		f.setDirectory(directory);
		f.setFilenameFilter(filter);
		f.setFile(suggestedMapName);
		f.setVisible(true);
		directory = f.getDirectory();
		String fs = f.getFile();
		f.dispose();
		if (directory != null && fs != null) {
			File nf = new File(directory, fs);
			try {
				saveModelSettings(nf);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void saveModelSettings(File file) {
		double intercept = 1.0;
		double min = -999;
		double max = -999;
		double tmin = -999;
		double tmax = -999;

		if (zfp.isSelected()) {
			try {
				min = this.zfp.getMin();
			}
			catch (Exception ex) {
			}

			try {
				max = this.zfp.getMax();
			}
			catch (Exception ex) {
			}
		}

		if (tfp.isSelected()) {
			try {
				tmin = this.tfp.getMin();
			}
			catch (Exception ex) {
			}
			try {
				tmax = this.tfp.getMax();
			}
			catch (Exception ex) {
			}
		}

		// save preferences as XML
		try {
	  	ForecastingModel selModel = mTSModelChooser.getSelectedModel();
			// create a documentobject
			Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("joatsmodel");
			root.setAttribute("intercept", String.valueOf(selModel.getIntercept()));
			if (mCommentField.getText().length() > 0) {
				root.setAttribute("description", mCommentField.getText());
			}

			for (TSModelTerm tsmt : selModel.getModelTerms()) {
				// make the map region element and add it
				double scaler = tsmt.getConstant();
				double exp = tsmt.getExponent();

					Element item = doc.createElement("tsterm");
					item.setAttribute("type", String.valueOf(tsmt.getParam().toString()));
					// filter

					item.setAttribute("scaler", String.valueOf(scaler));
					item.setAttribute("exponent", String.valueOf((int) exp));
					root.appendChild(item);
			}

			if (tfp.isSelected()) {
				Element item = doc.createElement("trangefilter");
				item.setAttribute("min", String.valueOf(tmin));
				item.setAttribute("max", String.valueOf(tmax));
				root.appendChild(item);
			}

			if (zfp.isSelected()) {
				Element item = doc.createElement("zrangefilter");
				item.setAttribute("min", String.valueOf(min));
				item.setAttribute("max", String.valueOf(max));
				root.appendChild(item);
			}

			if (dfp.isSelected()) {
				Element item = dfp.getTag(doc);
				root.appendChild(item);
			}

			if (mfp.isSelected()) {
				Element item = mfp.getTag(doc);
				root.appendChild(item);
			}

			if (sfp.isSelected()) {
				Element item = sfp.getTag(doc);
				root.appendChild(item);
			}

			if (ydfp.isSelected()) {
				Element item = ydfp.getTag(doc);
				root.appendChild(item);
			}

			Element item = doc.createElement("Bias");
			item.setAttribute("value", String.valueOf(selModel.getBias()));
			item.setAttribute("description", "Arithmetic mean of errors");
			root.appendChild(item);

			item = doc.createElement("MAD");
			item.setAttribute("value", String.valueOf(selModel.getMAD()));
			item.setAttribute("description", "Mean Absolute Deviation");
			root.appendChild(item);

			item = doc.createElement("MASE");
			item.setAttribute("value", String.valueOf(selModel.getMSE()));
			item.setAttribute("description", "Mean Square Error");
			root.appendChild(item);

			item = doc.createElement("MAPE");
			item.setAttribute("value", String.valueOf(selModel.getMAPE()));
			item.setAttribute("description", "Mean Absolute % Error");
			root.appendChild(item);

			item = doc.createElement("SAE");
			item.setAttribute("value", String.valueOf(selModel.getSAE()));
			item.setAttribute("description", "Sum Average Errors");
			root.appendChild(item);

			doc.appendChild(root);
			((TXDocument) doc).setVersion("1.0");
			FileWriter fr = new FileWriter(file);
			((TXDocument) doc).printWithFormat(fr);
			fr.flush();
			fr.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public XYPlotSpecification createPlotSpec(String winTitle, String previewString) {
		XYPlotSpecification ps = new XYPlotSpecification();
		// get the colors
		ps.setFGColor(Color.black);
		ps.setBGColor(new Color(192, 192, 192));
		ps.setWidth(550);
		ps.setHeight(450);

		int yPos = mFileViewer.getPropertyPos("temp", true);
		int xPos = mFileViewer.getPropertyPos("salt", true);

		ps.setNumXAxes(1);
		ps.setSaltAxis(0);
		ps.setFileViewer(mFileViewer);
		ps.setXVarCode(0, xPos);
		ps.setYVarCode(yPos);
		ps.setWinTitle(winTitle);
		ps.setOverrideLabel(previewString);
		ps.setIncludeCBAR(false);
		ps.setIncludeObsPanel(false);
		ps.setConnectObs(false);
		ps.setXGrid(false);
		ps.setYGrid(false);
		ps.setPlotAxes(true);
		ps.setPlotIsopycnals(false);
		ps.setCanPlotIsoPycnals(true);
		ps.setSymbolSize(0, 1);
		ps.setSymbol(0, 2);
		ps.setPlotOnlyCurrStn(false);
		ps.setAccumulateStns(false);
		ps.setColorByCBParam(true);
		ps.setColorByConnectLineColor(false);
		ps.setSymbolSize(0, 4);

		double refPress;
		/*
		 * try { refPress = Double.valueOf(mRefPressField.getText()).doubleValue();
		 * } catch (Exception ex) { refPress = 0.0; }
		 */
		refPress = 0.0;
		ps.setRefPress(refPress);

		// create pretty axes ranges really get these from the text fields if needed
		ps.setXTics(0, 1);
		ps.setYTics(1);
		for (int i = 0; i < 1; i++) {
			double tempMin = mFileViewer.mAllProperties[xPos].getPlotMin();
			double tempMax = mFileViewer.mAllProperties[xPos].getPlotMax();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempMin, tempMax);
			ps.setWinXPlotMin(i, newRange.getVal1());
			ps.setWinXPlotMax(i, newRange.getVal2());
			ps.setXInc(i, newRange.getVal3());
		}

		double tempMin = mFileViewer.mAllProperties[yPos].getPlotMin();
		double tempMax = mFileViewer.mAllProperties[yPos].getPlotMax();
		Triplet newRange = JOAFormulas.GetPrettyRange(tempMin, tempMax);
		ps.setWinYPlotMin(newRange.getVal1());
		ps.setWinYPlotMax(newRange.getVal2());
		ps.setYInc(newRange.getVal3());

		return ps;
	}

  public void valueChanged(ListSelectionEvent e) {
  	ForecastingModel selModel = mTSModelChooser.getSelectedModel();
  	
  	try {
    	// fill in the preview
  		updatePreview(selModel);
    	// fill in the error terms
    	updateErrorTerms(selModel);
  	}
  	catch (Exception ex) {
  		// silent
  	}
  }
  
	public void updatePreview(ForecastingModel selModel) {		
		String mPreviewString = "<html><bold>Salinity = " + String.valueOf(selModel.getIntercept()) + "  ";

		// add any additional terms
		for (TSModelTerm tsmt : selModel.getModelTerms()) {
			double scaler = tsmt.getConstant();
			double exp = tsmt.getExponent();

				mPreviewString += tsmt.toHTMLToo();
		}
		mPreviewString += "</bold></html";

		mCurrModelPreview.setText(mPreviewString);
		// mPreview.invalidate();
		// mPreview.validate();
	}
  
	public void updateErrorTerms(ForecastingModel selModel) {
		double bias = selModel.getBias();
		double MAD = selModel.getMAD();
		double MAPE = selModel.getMAPE();
		double MSE = selModel.getMSE();
		double SAE = selModel.getSAE();
		
		// get the precision
		int prec = 3;//(Integer) mPrecisionSpinner.getValue();
		
		String mPreviewString = "<html><bold>Bias=" + JOAFormulas.formatDouble(bias, prec, false) + "  ";
		mPreviewString += "MAD=" + JOAFormulas.formatDouble(MAD, prec, false) + "  ";
		mPreviewString += "MAPE=" + JOAFormulas.formatDouble(MAPE, prec, false) + "  ";
		mPreviewString += "MSE=" + JOAFormulas.formatDouble(MSE, prec, false) + "  ";
		mPreviewString += "SAE=" + JOAFormulas.formatDouble(SAE, prec, false) + "  ";
		mPreviewString += "</bold></html";

		mCurrModelErrTerms.setText(mPreviewString);
		// mPreview.invalidate();
		// mPreview.validate();
	}
}

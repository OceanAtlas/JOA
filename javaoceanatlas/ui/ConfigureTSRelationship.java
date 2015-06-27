/**
 * 
 */
package javaoceanatlas.ui;

import gov.noaa.pmel.swing.NPixelBorder;
import gov.noaa.pmel.util.GeoDate;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.sourceforge.openforecast.DateFilterPanel;
import net.sourceforge.openforecast.IntegerFilterPanel;
import net.sourceforge.openforecast.MonthFilterPanel;
import net.sourceforge.openforecast.NumericFilterPanel;
import net.sourceforge.openforecast.SeasonFilterPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import com.ibm.xml.parser.TXDocument;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.ui.widgets.JOAJButton;
import javaoceanatlas.ui.widgets.JOAJDialog;
import javaoceanatlas.ui.widgets.SmallIconButton;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.DialogClient;
import javaoceanatlas.utility.JOAFormulas;
import javaoceanatlas.utility.Orientation;
import javaoceanatlas.utility.RowLayout;

/**
 * @author oz
 * 
 */
public class ConfigureTSRelationship extends JOAJDialog implements DocumentListener, DialogClient, FilterUIHolder {
  private static final long serialVersionUID = 7177351652471345090L;
	private double mMinVal;
	private double mMaxVal;
	private JTextField mInterceptTF = new JTextField(6);
	private JTextArea mDescription = new JTextArea(3,40);
	private SmallIconButton mRemoveTermButton;
	private SmallIconButton mAddTermButton;
	private Timer timer = new Timer();
	Vector<TSModelTermUI> mAdditionalTerms = new Vector<TSModelTermUI>();
	private JLabel mPreviewDisplay = new JLabel();
	private FileViewer mFV;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mSaveButton = null;
	protected JOAJButton mLoadButton = null;
	protected JOAJButton mCancelButton = null;
	private ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private ConfigureTSRelationship mThis;
	private JPanel mModelBuilderPanel = new JPanel(new RowLayout(Orientation.LEFT, Orientation.CENTER, 5));
	private JPanel mBtnPanel = new JPanel();
	private JPanel mBtnPanel2 = new JPanel();
	// protected ModelPreviewPanel mPreview = null;
	private String mPreviewString = "Salinity = 0.0 + 0.0T";
	private DateFilterPanel dfp;
	private MonthFilterPanel mfp;
	private SeasonFilterPanel sfp;
	private IntegerFilterPanel ydfp;
	private NumericFilterPanel zfp;
	private NumericFilterPanel tfp;

	public ConfigureTSRelationship(FileViewer fv) {
		super(null, "Configure TS Model", false);
		mFV = fv;
		mThis = this;
		try {
			mRemoveTermButton = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas")
			    .getResource("images/bigminus.gif")));
			mAddTermButton = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
			    "images/bigplus.gif")));
			
			mRemoveTermButton.setToolTipText("Remove last term from TS Model");
			mAddTermButton.setToolTipText("Add new term to TS Model");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		this.setLayout(new BorderLayout(5, 5));

		JPanel upperPanels = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		mInterceptTF.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mInterceptTF.getDocument().addDocumentListener(this);
		mModelBuilderPanel.add(new JLabel("Salinity = "));
		mModelBuilderPanel.add(mInterceptTF);

		mBtnPanel.setLayout(new GridLayout(1, 2, 5, 5));
		mBtnPanel.add(mRemoveTermButton);
		mBtnPanel.add(mAddTermButton);
		mModelBuilderPanel.add(mBtnPanel);
		upperPanels.add(mModelBuilderPanel);

		mAddTermButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mAddTermButton.setSelected(false);
				TSModelTermUI tsTerm = new TSModelTermUI(mThis);
				JPanel tsTermEditor = tsTerm.getEditor();
				mAdditionalTerms.add(tsTerm);

				String tempA = mInterceptTF.getText();

				// Redo the UI
				mModelBuilderPanel.removeAll();
				mModelBuilderPanel.add(new JLabel("Salinity ="));
				mModelBuilderPanel.add(mInterceptTF);
				mInterceptTF.setText(tempA);
				mInterceptTF.setCaretPosition(0);

				for (TSModelTermUI tsm : mAdditionalTerms) {
					mModelBuilderPanel.add(tsm.getEditor());
				}
				mModelBuilderPanel.add(mBtnPanel);
				pack();
				Rectangle dBounds = getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width / 2 - dBounds.width / 2;
				int y = sd.height / 2 - dBounds.height / 2;
				setLocation(x, y);
			}
		});

		mRemoveTermButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mRemoveTermButton.setSelected(false);
				TSModelTermUI lastTerm = mAdditionalTerms.elementAt(mAdditionalTerms.size() - 1);
				if (lastTerm != null) {
					mAdditionalTerms.remove(lastTerm);
				}

				// redo the UI
				String tempA = mInterceptTF.getText();
				mModelBuilderPanel.removeAll();
				mModelBuilderPanel.add(new JLabel("Salinity = "));
				mModelBuilderPanel.add(mInterceptTF);
				mInterceptTF.setText(tempA);
				mInterceptTF.setCaretPosition(0);

				for (TSModelTermUI tsm : mAdditionalTerms) {
					mModelBuilderPanel.add(tsm.getEditor());
				}
				mModelBuilderPanel.add(mBtnPanel);

				pack();
				Rectangle dBounds = getBounds();
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				int x = sd.width / 2 - dBounds.width / 2;
				int y = sd.height / 2 - dBounds.height / 2;
				setLocation(x, y);
			}
		});
		
		// filters go here
		JPanel varFilterPanel = new JPanel(new BorderLayout(0, 0));
		JPanel timeFilterCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
		JPanel ztFilterCont = new JPanel(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));

		TitledBorder tb = BorderFactory.createTitledBorder("Station-Level Filters (Applied First)");
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

		JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		previewPanel.add(mPreviewDisplay);
		mPreviewDisplay.setText("<html>Salinity = 0 + 0T<sup>2</sup></html>");
		// mPreview = new ModelPreviewPanel();
		// previewPanel.add(mPreview);
		upperPanels.add(previewPanel);
		
		JPanel upperPanelsCont = new JPanel(new BorderLayout(0, 0));
		
		upperPanelsCont.add(BorderLayout.NORTH, varFilterPanel);
		upperPanelsCont.add(BorderLayout.CENTER, ztFilterCont);
		
		JPanel descripPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		mDescription.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		descripPanel.add(new JLabel(b.getString("kDescription2")));
		descripPanel.add(mDescription);		
		upperPanelsCont.add(BorderLayout.SOUTH, descripPanel);
		upperPanels.add(upperPanelsCont);
    
		// button panel goes here
		mOKBtn = new JOAJButton(b.getString("kOK"));
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kDone"));
		mSaveButton = new JOAJButton(b.getString("kSaveModel"));
		mLoadButton = new JOAJButton(b.getString("kReadModel"));
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
		if (JOAConstants.ISMAC) {
			dlgBtnsPanel.add(mCancelButton);
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mLoadButton);
			dlgBtnsPanel.add(mOKBtn);
		}
		else {
			dlgBtnsPanel.add(mOKBtn);
			dlgBtnsPanel.add(mLoadButton);
			dlgBtnsPanel.add(mSaveButton);
			dlgBtnsPanel.add(mCancelButton);
		}
		dlgBtnsInset.add(dlgBtnsPanel);

		mSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				save("untitled_tsmodel.xml");
			}
		});

		mLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				readSettings();
			}
		});

		mOKBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// have to build the model				
				double intercept = 1.0;
				try {
					intercept = Double.parseDouble(mInterceptTF.getText());
				}
				catch (Exception ex) {
					JFrame f = new JFrame("Value Error");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(f, "The value entered for intercept is not a valid number.");
					mInterceptTF.setText("");
					return;
				}

				TSModel newTSModel = new TSModel();
				newTSModel.setIntercept(intercept);
				newTSModel.setAdditionalTerms(mAdditionalTerms);
				if (zfp.isSelected()) {
					double min = 1.0;
					try {
						min = zfp.getMin();
					}
					catch (Exception ex) {
						JFrame f = new JFrame("Value Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, "The min value entered for the depth constraint is not a valid number.");
						return;
					}
					
					double max = 1.0;
					try {
						max = zfp.getMax();
					}
					catch (Exception ex) {
						JFrame f = new JFrame("Value Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, "The max value entered for the depth constraint is not a valid number.");
						return;
					}
					newTSModel.setZRangeConstraint(min, max);
				}
				
				if (tfp.isSelected()) {
					double min = 1.0;
					try {
						min = tfp.getMin();
					}
					catch (Exception ex) {
						JFrame f = new JFrame("Value Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, "The min value entered for the temperature constraint is not a valid number.");
						return;
					}
					
					double max = 1.0;
					try {
						max = tfp.getMax();
					}
					catch (Exception ex) {
						JFrame f = new JFrame("Value Error");
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(f, "The max value entered for the temperature constraint is not a valid number.");
						return;
					}
					newTSModel.setTRangeConstraint(min, max);
				}
				
				//todo add Time filter filters to newTSModel

				// got to here we have a valid model
				mFV.applyTSModel(newTSModel);

				dispose();
				mFV.requestFocus();
			}
		});

		mCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});

		this.getContentPane().add(BorderLayout.CENTER, new NPixelBorder(upperPanels, 10, 10, 10, 10));
		this.getContentPane().add(BorderLayout.SOUTH, new NPixelBorder(dlgBtnsInset, 10, 10, 10, 10));

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

	public void maintainButtons() {
		if (mAdditionalTerms.size() > 0) {
			mRemoveTermButton.setEnabled(true);
		}
		else {
			mRemoveTermButton.setEnabled(false);
		}
	}

	public void updatePreview() {
		mPreviewString = "<html><bold>Salinity = " + mInterceptTF.getText() + "  ";
		// add any additional terms
		for (TSModelTermUI tsmt : mAdditionalTerms) {
			mPreviewString += tsmt.toString();
		}
		mPreviewString += "</bold></html";

		mPreviewDisplay.setText(mPreviewString);
		// mPreview.invalidate();
		// mPreview.validate();
	}

	public double getMinVal() {
		return mMinVal;
	}

	public double getMaxVal() {
		return mMaxVal;
	}

	public void changedUpdate(DocumentEvent e) {
		updatePreview();
	}

	public void insertUpdate(DocumentEvent e) {
		updatePreview();
	}

	public void removeUpdate(DocumentEvent e) {
		updatePreview();
	}

	public void dialogApply(JDialog d) {
		// TODO Auto-generated method stub
	}

	public void dialogApplyTwo(Object d) {
		// TODO Auto-generated method stub
	}

	public void dialogCancelled(JDialog d) {
		// TODO Auto-generated method stub
	}

	public void dialogDismissed(JDialog d) {
		// TODO Auto-generated method stub
	}

	public void dialogDismissedTwo(JDialog d) {
		// TODO Auto-generated method stub
	}

	public void saveModelSettings(File file) {
		double intercept = 1.0;
		double min = -999;
		double max = -999;
		double tmin = -999;
		double tmax = -999;

		try {
			intercept = Double.parseDouble(mInterceptTF.getText());
		}
		catch (Exception ex) {
			JFrame f = new JFrame("Value Error");
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(f, "The value entered for intercept is not a valid number.");
			mInterceptTF.setText("");
			return;
		}
		
		if (zfp.isSelected()) {
			try {
				min = zfp.getMin();
			}
			catch (Exception ex) {
			}
			
			try {
				max = zfp.getMax();
			}
			catch (Exception ex) {
			}
		}
		
		if (tfp.isSelected()) {
			try {
				tmin = tfp.getMin();
			}
			catch (Exception ex) {
			}
			try {
				tmax = tfp.getMax();
			}
			catch (Exception ex) {
			}
		}
			
		// save preferences as XML
		try {
			// create a documentobject
			Document doc = (Document) Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("joatsmodel");
			root.setAttribute("intercept", String.valueOf(intercept));
			if (mDescription.getText().length() > 0) {
				root.setAttribute("description", mDescription.getText());
			}
			
			for (TSModelTermUI tsmt : mAdditionalTerms) {
				// make the map region element and add it
				Element item = doc.createElement("tsterm");
				item.setAttribute("type", String.valueOf(tsmt.getParam().toString()));
				item.setAttribute("scaler", String.valueOf(tsmt.getConstant()));
				item.setAttribute("exponent", String.valueOf(tsmt.getExponent()));
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
	
  public void readSettings() {
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
    FileDialog f = new FileDialog(fr, "Read TS model settings from:", FileDialog.LOAD);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    f.setVisible(true);
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();
    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
        getTSModel(nf);

  			for (TSModelTermUI tsm : mAdditionalTerms) {
  				tsm.setUI();
  				mModelBuilderPanel.add(tsm.getEditor());
  			}
  			mModelBuilderPanel.add(mBtnPanel);
  			
  			pack();
  			Rectangle dBounds = getBounds();
  			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
  			int x = sd.width / 2 - dBounds.width / 2;
  			int y = sd.height / 2 - dBounds.height / 2;
  			setLocation(x, y);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
	
  private class MapNotifyStr extends HandlerBase {
    public void startDocument() throws SAXException {
    	zfp.setSelected(false);
    	dfp.setSelected(false);
    	tfp.setSelected(false);
    	mfp.setSelected(false);
    	sfp.setSelected(false);
    	ydfp.setSelected(false);
			mModelBuilderPanel.removeAll();
			mModelBuilderPanel.add(new JLabel("Salinity="));
			mModelBuilderPanel.add(mInterceptTF);
			mAdditionalTerms.clear();
    }

    public void startElement(String name, AttributeList amap) throws SAXException {
      if (name.equals("joatsmodel")) {
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("intercept")) {
          	mInterceptTF.setText(amap.getValue(i));
    				mInterceptTF.setCaretPosition(0);
          }
          
          if (amap.getName(i).equals("description")) {
          	mDescription.setText(amap.getValue(i));
          }
        }
      }
      else if (name.equals("zrangefilter")) {
      	zfp.setSelected(true);
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("min")) {
          	zfp.setMinText(amap.getValue(i));
          }
          if (amap.getName(i).equals("max")) {
          	zfp.setMaxText(amap.getValue(i));
          }
        }
      }
      else if (name.equals("trangefilter")) {
      	tfp.setSelected(true);
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("min")) {
          	tfp.setMinText(amap.getValue(i));
          }
          if (amap.getName(i).equals("max")) {
          	tfp.setMaxText(amap.getValue(i));
          }
        }
      }
      else if (name.equals("datefilter")) {
      	dfp.setSelected(true);
      	long lmin = 0;
      	long lmax = 0;
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("min")) {
          	lmin = Long.parseLong(amap.getValue(i));
          }
          if (amap.getName(i).equals("max")) {
          	lmax = Long.parseLong(amap.getValue(i));
          }
        }
        dfp.setMin(new GeoDate(lmin));
        dfp.setMax(new GeoDate(lmax));
      }
      else if (name.equals("monthfilter")) {
      	mfp.setSelected(true);
      	int min = 0;
      	int max = 0;
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("min")) {
          	min = Integer.parseInt(amap.getValue(i));
          }
          if (amap.getName(i).equals("max")) {
          	max = Integer.parseInt(amap.getValue(i));
          }
        }

        mfp.setStartMonth(min);
        mfp.setEndMonth(min);
      }
      else if (name.equals("seasonfilter")) {
      	sfp.setSelected(true);
      	int ord = 0;
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("ord")) {
          	ord = Integer.parseInt(amap.getValue(i));
          }
        }

        sfp.setSeason(Seasons.getSeasonFromOrd(ord));
      }
      else if (name.indexOf("yearday") >= 0) {
      	ydfp.setSelected(true);
      	int min = 0;
      	int max = 0;
        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("min")) {
          	min = Integer.parseInt(amap.getValue(i));
          }
          if (amap.getName(i).equals("max")) {
          	max = Integer.parseInt(amap.getValue(i));
          }
        }

        ydfp.setMin(min);
        ydfp.setMax(max);
      }
      else if (name.equals("tsterm")) {
      	TSModelTermUI newTerm;
      	double c = 1.0;
      	double exp = 1.0;
      	TSModelTermParameter param = null;

        for (int i = 0; i < amap.getLength(); i++) {
          if (amap.getName(i).equals("type")) {
            if (amap.getValue(i).equalsIgnoreCase(TSModelTermParameter.TEMPERATURE.toString())) {
            	param = TSModelTermParameter.TEMPERATURE;
            }
            else if (amap.getValue(i).equalsIgnoreCase(TSModelTermParameter.SQTEMPERATURE.toString())) {
            	param = TSModelTermParameter.SQTEMPERATURE;
            }
            else if (amap.getValue(i).equalsIgnoreCase(TSModelTermParameter.LONGITUDE.toString())) {
            	param = TSModelTermParameter.LONGITUDE;
            }
            else if (amap.getValue(i).equalsIgnoreCase(TSModelTermParameter.LATITUDE.toString())) {
            	param = TSModelTermParameter.LATITUDE;
            }
            else if (amap.getValue(i).equalsIgnoreCase(TSModelTermParameter.LONxLAT.toString())) {
            	param = TSModelTermParameter.LONxLAT;
            }
          }
          else if (amap.getName(i).equals("scaler")) {
            try {
              c = Double.valueOf(amap.getValue(i)).doubleValue();
            }
            catch (Exception ex) {
              c = 0.0;
            }
          }
          else if (amap.getName(i).equals("exponent")) {
            try {
              exp = Double.valueOf(amap.getValue(i)).doubleValue();
            }
            catch (Exception ex) {
              exp = 1.0;
            }
          }
        }
        newTerm = new TSModelTermUI(mThis, c, exp, param);
        mAdditionalTerms.add(newTerm);
      }
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
      String strVal = new String(ch, start, len);
    }

    public void endElement(String name) throws SAXException {
    }
  }
  
  protected void getTSModel(File file) throws Exception {
    try {
      // parse as xml first
      Class c = Class.forName("com.ibm.xml.parser.SAXDriver");
      org.xml.sax.Parser parser = (org.xml.sax.Parser)c.newInstance();
      MapNotifyStr notifyStr = new MapNotifyStr();
      parser.setDocumentHandler(notifyStr);
      parser.parse(file.getPath());
      
			pack();
			Rectangle dBounds = getBounds();
			Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
			int x = sd.width / 2 - dBounds.width / 2;
			int y = sd.height / 2 - dBounds.height / 2;
			setLocation(x, y);
    }
    catch (Exception xmlEx) {
      xmlEx.printStackTrace();
    }
  }

	private class ModelPreviewPanel extends JPanel {
		private Image mOffScreen = null;

		public void invalidate() {
			try {
				super.invalidate();
				if (mOffScreen != null) {
					mOffScreen = null;
					paintImmediately(new Rectangle(0, 0, 1000, 1000));
				}
			}
			catch (Exception e) {

			}
		}

		public Image makeOffScreen(Graphics2D g) {
			Image outImage = null;
			int pWidth = getSize().width;
			outImage = createImage(pWidth, getSize().height);

			Graphics2D og = (Graphics2D) outImage.getGraphics();
			super.paintComponent(og);

			// og.setColor(Color.white);
			// og.fillRect(0, 0, 2000, 2000);

			JOAFormulas.drawStyledString(mPreviewString, 0, 20, og, 0.0, JOAConstants.DEFAULT_PLOT_TITLE_FONT,
			    JOAConstants.DEFAULT_PLOT_TITLE_SIZE, JOAConstants.DEFAULT_PLOT_TITLE_STYLE,
			    JOAConstants.DEFAULT_PLOT_TITLE_COLOR);

			og.dispose();
			return outImage;
		}

		public Dimension getPreferredSize() {
			return new Dimension(400, 30);
		}

		public void paintComponent(Graphics gin) {
			Graphics2D g = (Graphics2D) gin;
			if (mOffScreen == null) {
				mOffScreen = makeOffScreen(g);
				g.drawImage(mOffScreen, 0, 0, null);
			}
			else {
				g.drawImage(mOffScreen, 0, 0, null);
			}
		}
	}

	/* (non-Javadoc)
   * @see javaoceanatlas.ui.FilterUIHolder#updateUIAfterFilterChange()
   */
  public void updateUIAfterFilterChange() {
		pack();
		Rectangle dBounds = getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		setLocation(x, y);
  }
}

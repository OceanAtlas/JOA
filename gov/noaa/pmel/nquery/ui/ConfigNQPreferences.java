/*
 * $Id: ConfigPreferences.java,v 1.19 2005/10/18 23:43:05 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.ui;

import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javaoceanatlas.utility.DialogClient;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javaoceanatlas.utility.TenPixelBorder;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.ibm.xml.parser.TXDocument;
import java.io.FileWriter;
import java.util.Vector;
import gov.noaa.pmel.nquery.database.DatabaseTools;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import java.awt.Cursor;
import javax.swing.JFileChooser;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import java.awt.Font;
import java.io.FilenameFilter;
import javaoceanatlas.ui.widgets.JOAJDialog;

/**
 * <code>ConfigPreferences</code> UI for configuring NQuery's user preferences.
 *
 * @author oz
 * @version 1.0
 */

public class ConfigNQPreferences extends JOAJDialog implements ActionListener {
  protected JTabbedPane mTabPane = null;
  protected DatabasePrefsPanel mDBPrefs = null;
  protected BuiltInCalcsPrefsPanel mCalcsPrefs = null;
  protected ProfileVariablesPrefsPanel mProfilePrefs = null;

  // widgets for built-in calcs prefs
  protected JCheckBox mCalcMin = null;
  protected JCheckBox mCalcMax = null;
  protected JCheckBox mCalcAvg = null;
  protected JCheckBox mCalcDepthOfMax = null;
  protected JCheckBox mCalcDepthOfMin = null;
  protected JCheckBox mCalcMaxDepthOfNonMissingVal = null;
  protected JCheckBox mCalcMinDepthOfNonMissingVal = null;
  protected JCheckBox mCalcN = null;
  protected JCheckBox mApplyToUserCalcs = null;

  // widgets for DB Prefs ;
  protected JTextField dbURI = null;
  ; protected JTextField dbPort = null;
  protected JTextField dbUserName = null;
  protected JPasswordField dbUserPW = null;
  protected JTextField dbDefaultDir = null;

  // widgets for lexicon prefs
  protected JCheckBox mTranslate = null;
  protected JCheckBox mConvertDepth = null;
  protected JCheckBox mConvertO2 = null;
  protected JComboBox mDestinationLexicon = null;
  protected JComboBox mSalinityVar = null;
  protected JComboBox mSalinitySubstitute = null;
  protected JComboBox mO2Var = null;
  protected JComboBox mO2Substitute = null;

  // Debug Preferences
  protected JCheckBox mSetDebugMode = null;

  // whole dialog widgets
  protected JButton mOKBtn = null;
  protected JButton mCancelButton = null;
  protected JButton mSaveBtn = null;
  protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  DialogClient mClient;
  JDialog mThis;

  public ConfigNQPreferences(JFrame par, DialogClient client) {
    super(par, "", false);
    this.setTitle(b.getString("kNQueryPreferences2"));
    mClient = client;
    mThis = this;
    init();
  }

  public void init() {
    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));

    mTabPane = new JTabbedPane();
    mCalcsPrefs = new BuiltInCalcsPrefsPanel();
    mDBPrefs = new DatabasePrefsPanel();
    mProfilePrefs = new ProfileVariablesPrefsPanel();

    mSetDebugMode = new JCheckBox(b.getString("kShowDebugMessagesInConsole"), NQueryConstants.DEFAULT_DEBUG_MODE);
    JPanel debugPanel = new JPanel();
    debugPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    debugPanel.add(mSetDebugMode);

    mTabPane.addTab(b.getString("kDatabase"), mDBPrefs);
    mTabPane.addTab(b.getString("kBuiltInCalcs"), mCalcsPrefs);
    mTabPane.addTab(b.getString("kProfileVarOptions"), mProfilePrefs);
    mTabPane.addTab(b.getString("kDebugOptions"), debugPanel);
    mTabPane.setSelectedIndex(0);

    mainPanel.add("Center", new TenPixelBorder(mTabPane, 5, 5, 5, 5));

    // lower panel
    mOKBtn = new JButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    mSaveBtn = new JButton(b.getString("kSave"));
    mSaveBtn.setActionCommand("save");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
    if (NQueryConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mSaveBtn);
    }
    else {
      dlgBtnsPanel.add(mSaveBtn);
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    mOKBtn.addActionListener(this);
    mCancelButton.addActionListener(this);
    mSaveBtn.addActionListener(this);

    mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    contents.add("Center", mainPanel);
    this.pack();

    // init state of check boxes
    initBtns();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      // fill in the global preferences
      NQueryConstants.DEFAULT_DB_SAVE_DIR = dbDefaultDir.getText();
      NQueryConstants.DEFAULT_CALC_MIN = mCalcMin.isSelected();
      NQueryConstants.DEFAULT_CALC_MAX = mCalcMax.isSelected();
      NQueryConstants.DEFAULT_CALC_DEPTH_OF_MIN = mCalcDepthOfMin.isSelected();
      NQueryConstants.DEFAULT_CALC_DEPTH_OF_MAX = mCalcDepthOfMax.isSelected();
      NQueryConstants.DEFAULT_MAX_DEPTH_OF_NONMISSING_VAL = mCalcMaxDepthOfNonMissingVal.isSelected();
      NQueryConstants.DEFAULT_MIN_DEPTH_OF_NONMISSING_VAL = mCalcMinDepthOfNonMissingVal.isSelected();
      NQueryConstants.DEFAULT_CALC_AVERAGE = mCalcAvg.isSelected();
      NQueryConstants.DEFAULT_CALC_N = mCalcN.isSelected();
      NQueryConstants.DEFAULT_APPLY_CALCS_TO_USER_CALCS = mApplyToUserCalcs.isSelected();
      NQueryConstants.DEFAULT_TRANSLATE_LEXICON = mTranslate.isSelected();
      NQueryConstants.DEFAULT_CONVERT_DEPTH = mConvertDepth.isSelected();
      NQueryConstants.DEFAULT_CONVERT_O2 = mConvertO2.isSelected();
      NQueryConstants.DEFAULT_LEXICON = mDestinationLexicon.getSelectedIndex();
      NQueryConstants.DEFAULT_SALINITY_VARIABLE = mSalinityVar.getSelectedIndex();
      NQueryConstants.DEFAULT_SALINITY_SUBSTITUTION = mSalinitySubstitute.getSelectedIndex();
      NQueryConstants.DEFAULT_O2_VARIABLE = mO2Var.getSelectedIndex();
      NQueryConstants.DEFAULT_O2_SUBSTITUTION = mO2Substitute.getSelectedIndex();
      NQueryConstants.DEFAULT_DEBUG_MODE = mSetDebugMode.isSelected();
      DatabaseTools.setDebugMode(NQueryConstants.DEFAULT_DEBUG_MODE);
      this.dispose();
    }
    else if (cmd.equals("save")) {
      // save prefs to an external xml file
      save();
    }
  }

  public void initBtns() {}

  private class DatabasePrefsPanel extends JPanel implements ItemListener, ActionListener {
    public DatabasePrefsPanel() {
      // init the interface
      init();
    }

    public void init() {
      // layout for entire panel
      JPanel thisPanel = new JPanel();
      thisPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));

      // make a column of labels
      JPanel labelPanel = new JPanel();
      labelPanel.setLayout(new ColumnLayout(Orientation.RIGHT, Orientation.CENTER, 8));
      labelPanel.add(new JLabel(b.getString("kDBURI")));
      labelPanel.add(new JLabel(b.getString("kDBPort")));
      labelPanel.add(new JLabel(b.getString("kDBUserName")));
      labelPanel.add(new JLabel(b.getString("kDBPassword")));
      labelPanel.add(new JLabel(b.getString("kDBDefaultDir")));

      JPanel fieldPanel = new JPanel();
      fieldPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
			dbURI = new JTextField(30);
			dbURI.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			dbURI.setText(NQueryConstants.DEFAULT_DB_URI);
			dbPort = new JTextField(5);
			dbPort.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			dbPort.setText(NQueryConstants.DEFAULT_DB_PORT);
			dbUserName = new JTextField(10);
			dbUserName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			dbUserName.setText(NQueryConstants.DEFAULT_DB_USERNAME);
			dbUserPW = new JPasswordField (10);
			dbUserPW.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			dbUserPW.setEchoChar('#');
			dbUserPW.setText(NQueryConstants.DEFAULT_DB_PASSWORD);
      JPanel defDir = new JPanel();
      defDir.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
      dbDefaultDir = new JTextField(30);
      dbDefaultDir.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      dbDefaultDir.setText(NQueryConstants.DEFAULT_DB_SAVE_DIR);
      defDir.add(dbDefaultDir);
      JButton browseBtn = new JButton();
      browseBtn = new JButton(b.getString("kBrowse2"));
      browseBtn.setActionCommand("browse");
      browseBtn.addActionListener(this);
      defDir.add(browseBtn);

      fieldPanel.add(dbURI);
      fieldPanel.add(dbPort);
      fieldPanel.add(dbUserName);
      fieldPanel.add(dbUserPW);
      fieldPanel.add(defDir);

      thisPanel.add(labelPanel);
      thisPanel.add(fieldPanel);
      this.add(thisPanel);
    }

    public void itemStateChanged(ItemEvent evt) {
    }

    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();

      if (cmd.equals("browse")) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle(b.getString("kSelectDirectory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText(b.getString("kSelect"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
          dbDefaultDir.setText(chooser.getCurrentDirectory().getPath() + File.separator +
                               chooser.getSelectedFile().getName());
        }
      }
    }
  }

  private class BuiltInCalcsPrefsPanel extends JPanel implements ItemListener {
    public BuiltInCalcsPrefsPanel() {
      // init the interface
      init();
    }

    public void init() {
      // panel for selecting built-in calc
      // observation calcs (stored as individual lines in a gridlayout);
      this.setLayout(new BorderLayout(5, 5));

      JPanel content = new JPanel();
      content.setLayout(new GridLayout(8, 2, 0, 0));

      mCalcMin = new JCheckBox(b.getString("kMinimumValue"), NQueryConstants.DEFAULT_CALC_MIN);
      content.add(mCalcMin);

      mCalcMax = new JCheckBox(b.getString("kMaximumValue"), NQueryConstants.DEFAULT_CALC_MAX);
      content.add(mCalcMax);

      mCalcDepthOfMin = new JCheckBox(b.getString("kDepthMinimumValue"), NQueryConstants.DEFAULT_CALC_DEPTH_OF_MIN);
      content.add(mCalcDepthOfMin);

      mCalcDepthOfMax = new JCheckBox(b.getString("kDepthMaximumValue"), NQueryConstants.DEFAULT_CALC_DEPTH_OF_MAX);
      content.add(mCalcDepthOfMax);

      mCalcMaxDepthOfNonMissingVal = new JCheckBox(b.getString("kMaxDepthofNonMissingValue"),
          NQueryConstants.DEFAULT_MAX_DEPTH_OF_NONMISSING_VAL);
      content.add(mCalcMaxDepthOfNonMissingVal);

      mCalcMinDepthOfNonMissingVal = new JCheckBox(b.getString("kMinDepthofNonMissingValue"),
          NQueryConstants.DEFAULT_MIN_DEPTH_OF_NONMISSING_VAL);
      content.add(mCalcMinDepthOfNonMissingVal);

      mCalcAvg = new JCheckBox(b.getString("kCalcAverage"), NQueryConstants.DEFAULT_CALC_AVERAGE);
      content.add(mCalcAvg);

      mCalcN = new JCheckBox(b.getString("kNumberNonMissing"), NQueryConstants.DEFAULT_CALC_N);
      content.add(mCalcN);

      mApplyToUserCalcs = new JCheckBox(b.getString("kApplyToUserCalcs"),
                                        NQueryConstants.DEFAULT_APPLY_CALCS_TO_USER_CALCS);
      this.add("South", mApplyToUserCalcs);

      TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kProfiles"));
      if (NQueryConstants.ISMAC) {
        tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
      }
      content.setBorder(tb);

      this.add("Center", content);
    }

    public void itemStateChanged(ItemEvent evt) {
    }
  }

  public void save() {
    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator;
    FileDialog f = new FileDialog(fr, "Save preferences as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    f.setFile("nqueryprefs.xml");
    f.show();
    directory = f.getDirectory();
    f.dispose();
    if (directory != null && f.getFile() != null) {
      File nf = new File(directory, f.getFile());
      savePrefs(nf);
    }
  }

  public void savePrefs(File file) {
    // save preferences as XML
    try {
      // read the DTD file
      // create a documentobject
      Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

      // make joapreferences the root element
      Element root = doc.createElement("nquerypreferences");

      // Built-in calculation prefs go here;
      Element item = doc.createElement("builtincalcprefs");
      item.setAttribute("calcmin", String.valueOf(mCalcMin.isSelected()));
      item.setAttribute("calcmax", String.valueOf(mCalcMax.isSelected()));
      item.setAttribute("calcdepthofmin", String.valueOf(mCalcDepthOfMin.isSelected()));
      item.setAttribute("calcdepthofmax", String.valueOf(mCalcDepthOfMax.isSelected()));
      item.setAttribute("calcmaxnonmissingdepth", String.valueOf(mCalcMaxDepthOfNonMissingVal.isSelected()));
      item.setAttribute("calcminnonmissingdepth", String.valueOf(mCalcMinDepthOfNonMissingVal.isSelected()));
      item.setAttribute("calcavg", String.valueOf(mCalcAvg.isSelected()));
      item.setAttribute("calcn", String.valueOf(mCalcN.isSelected()));
      item.setAttribute("applycalcstousercalcs", String.valueOf(mApplyToUserCalcs.isSelected()));
      root.appendChild(item);

      // database prefs
      item = doc.createElement("databaseprefs");
      item.setAttribute("dburi", dbURI.getText());
      item.setAttribute("dbport", dbPort.getText());
      item.setAttribute("dbusername", dbUserName.getText());
      item.setAttribute("dbdefaultdir", dbDefaultDir.getText());
      root.appendChild(item);

      // profile prefs
      item = doc.createElement("profileprefs");
      item.setAttribute("translate", String.valueOf(mTranslate.isSelected()));
      item.setAttribute("tolexicon", (String)mDestinationLexicon.getSelectedItem());
      item.setAttribute("convertdepth", String.valueOf(mConvertDepth.isSelected()));
      item.setAttribute("converto2units", String.valueOf(mConvertO2.isSelected()));
      item.setAttribute("salinityvar", (String)mSalinityVar.getSelectedItem());
      item.setAttribute("salinitysub", (String)mSalinitySubstitute.getSelectedItem());
      item.setAttribute("o2var", (String)mO2Var.getSelectedItem());
      item.setAttribute("o2sub", (String)mO2Substitute.getSelectedItem());
      root.appendChild(item);

      doc.appendChild(root);
      ((TXDocument)doc).setVersion("1.0");
      ((TXDocument)doc).printWithFormat(new FileWriter(file));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private class ProfileVariablesPrefsPanel extends JPanel implements ItemListener {
    public ProfileVariablesPrefsPanel() {
      // init the interface
      init();
    }

    public void init() {
      this.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
      JPanel line1 = new JPanel();
      line1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
      JPanel line2 = new JPanel();
      line2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
      JPanel line3 = new JPanel();
      line3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
      JPanel line4 = new JPanel();
      line4.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
      JPanel line5 = new JPanel();
      line5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));

      Vector mLexiconChoices = new Vector();
      mLexiconChoices.add("EPIC");
      mLexiconChoices.add("JOA");
      mLexiconChoices.add("WOCE");

      mTranslate = new JCheckBox(b.getString("kTranslateToLexicon"), NQueryConstants.DEFAULT_TRANSLATE_LEXICON);
      mDestinationLexicon = new JComboBox(mLexiconChoices);
      mDestinationLexicon.setSelectedIndex(NQueryConstants.DEFAULT_LEXICON);
      line1.add(mTranslate);
      line1.add(mDestinationLexicon);
      mTranslate.addItemListener(this);
      if (!mTranslate.isSelected()) {
        mDestinationLexicon.setEnabled(false);
      }
      this.add(line1);

      mConvertDepth = new JCheckBox(b.getString("kConvertDepth"), NQueryConstants.DEFAULT_CONVERT_DEPTH);
      line2.add(mConvertDepth);
      this.add(line2);

      mConvertO2 = new JCheckBox(b.getString("kConvertO2"), NQueryConstants.DEFAULT_CONVERT_O2);
      line3.add(mConvertO2);
      this.add(line3);

      Vector mSalinityChoices = new Vector();
      mSalinityChoices.add(b.getString("kBottleSalinity"));
      mSalinityChoices.add(b.getString("kCTDSalinity"));
      mSalinityVar = new JComboBox(mSalinityChoices);
      mSalinityVar.setSelectedIndex(NQueryConstants.DEFAULT_SALINITY_VARIABLE);

      Vector mSalinitySubChoices = new Vector();
      mSalinitySubChoices.add(b.getString("kBottleSalinity"));
      mSalinitySubChoices.add(b.getString("kCTDSalinity"));
      mSalinitySubChoices.add(b.getString("kDontSubstitute"));
      mSalinitySubstitute = new JComboBox(mSalinitySubChoices);
      mSalinitySubstitute.setSelectedIndex(NQueryConstants.DEFAULT_SALINITY_SUBSTITUTION);

      JLabel label1 = new JLabel(b.getString("kVarToUseForSalt"));
      line4.add(label1);
      line4.add(mSalinityVar);
      JLabel label2 = new JLabel(b.getString("kSubstitution"));
      line4.add(label2);
      line4.add(mSalinitySubstitute);
      this.add(line4);

      Vector mO2Choices = new Vector();
      mO2Choices.add(b.getString("kBottleO2"));
      mO2Choices.add(b.getString("kCTDO2"));
      mO2Var = new JComboBox(mO2Choices);
      mO2Var.setSelectedIndex(NQueryConstants.DEFAULT_O2_VARIABLE);

      Vector mO2SubChoices = new Vector();
      mO2SubChoices.add(b.getString("kBottleO2"));
      mO2SubChoices.add(b.getString("kCTDO2"));
      mO2SubChoices.add(b.getString("kDontSubstitute"));
      mO2Substitute = new JComboBox(mO2SubChoices);
      mO2Substitute.setSelectedIndex(NQueryConstants.DEFAULT_O2_SUBSTITUTION);

      JLabel label3 = new JLabel(b.getString("kVarToUseForO2"));
      line5.add(label3);
      line5.add(mO2Var);
      JLabel label4 = new JLabel(b.getString("kSubstitution"));
      line5.add(label4);
      line5.add(mO2Substitute);
      this.add(line5);
    }

    public void itemStateChanged(ItemEvent evt) {
      if (evt.getSource() instanceof JCheckBox) {
        JCheckBox cb = (JCheckBox)evt.getSource();
        if (cb == mTranslate && evt.getStateChange() == ItemEvent.SELECTED) {
          mDestinationLexicon.setEnabled(true);
        }
        else if (cb == mTranslate && evt.getStateChange() == ItemEvent.DESELECTED) {
          mDestinationLexicon.setEnabled(false);
        }
      }
    }
  }
}

/*
 * $Id: NQuery.java,v 1.20 2005/10/18 23:43:05 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import com.apple.mrj.*;
import gov.noaa.pmel.eps2.*;
import javax.swing.*;
import javax.swing.event.*;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.ui.*;
import gov.noaa.pmel.nquery.utility.*;
import gov.noaa.pmel.swing.dapper.*;
import gov.noaa.pmel.nc2.station.*;
import ndEdit.*;
import gov.noaa.pmel.util.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;

@SuppressWarnings({ "serial", "deprecation" })
public class NQuery extends JFrame implements MRJOpenDocumentHandler, MRJOpenApplicationHandler, ActionListener,
    MRJQuitHandler, MRJAboutHandler, SelectionListener {
  private Vector<JCheckBoxMenuItem> viewCheckBoxes = new Vector<JCheckBoxMenuItem>();
  private Vector<NdEditAction> viewCheckBoxesActions = new Vector<NdEditAction>();
  private Vector<JMenuItem> fileMenuItems = new Vector<JMenuItem>();
  private Vector<AbstractAction> fileMenuActions = new Vector<AbstractAction>();
  private Vector<JMenuItem> zoomMenuItems = new Vector<JMenuItem>();
  private Vector<NdEditAction> zoomMenuActions = new Vector<NdEditAction>();
  private Vector<JMenuItem> editMenuItems = new Vector<JMenuItem>();
  private Vector<NdEditAction> editMenuActions = new Vector<NdEditAction>();
  private Vector<JMenuItem> viewMenuItems = new Vector<JMenuItem>();
  private Vector<NdEditAction> viewMenuActions = new Vector<NdEditAction>();
  private DapperWizard mDapperWiz = null;
  ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  private JToggleButton mDiscloseNdEdit = null;
  private NdEdit nd = null;
  Vector<PowerObs> mAllObs = new Vector<PowerObs>();

  public NQuery() {
    MRJApplicationUtils.registerOpenDocumentHandler(this);
    MRJApplicationUtils.registerOpenApplicationHandler(this);
    String opSys = System.getProperty("os.name");
    NQueryConstants.ISMAC = opSys.startsWith("Mac OS");
    NQueryConstants.ISMACOSX = opSys.startsWith("Mac OS X");
    NQueryConstants.ISSUNOS = opSys.startsWith("Sun");
    NQueryConstants.LEXICON = new Lexicon();

    init();
    this.invalidate();
    this.validate();
    this.setVisible(true);

    // read the preferences
    try {
      NQueryFormulas.readPreferences();
    }
    catch (Exception pex) {
      System.out.println("Couldn't read the preferences!");
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    doCommand(cmd, this);
  }

  public void init() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (UnsupportedLookAndFeelException e) {
      System.out.println("UnsupportedLookAndFeelException");
    }
    catch (ClassNotFoundException e) {
      System.out.println("ClassNotFoundException");
    }
    catch (InstantiationException e) {
      System.out.println("InstantiationException");
    }
    catch (IllegalAccessException e) {
      System.out.println("IllegalAccessException");
    }

    Container contents = getContentPane();
    contents.setLayout(new BorderLayout(0, 0));
    final JFrame fr = this;
    fr.setSize(400, 100);
    fr.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        fr.setVisible(false);
        fr.dispose();
      }
    });

    // MRJ stuff
    MRJApplicationUtils.registerQuitHandler(this);
    MRJApplicationUtils.registerAboutHandler(this);

    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        e.getWindow().dispose();
      }
    };
    fr.addWindowListener(windowListener);

    try {
      mDiscloseNdEdit = new JToggleButton(b.getString("kShowNdedit"),
                                          new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
                                              "images/discloseright.gif")));
      //new ImageIcon(getClass().getResource("images/discloseright.gif")));
      mDiscloseNdEdit.setSelectedIcon(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
                                              "images/disclosedown.gif")));
					      //new ImageIcon(getClass().getResource("images/disclosedown.gif")));
    }
    catch (Exception ex) {
    	System.out.println("NQuery:init");
      ex.printStackTrace();
    }
    mDiscloseNdEdit.setBorderPainted(false);
    mDiscloseNdEdit.setContentAreaFilled(false);
    mDiscloseNdEdit.setActionCommand("options");
    mDiscloseNdEdit.addActionListener(this);
    mDiscloseNdEdit.setHorizontalAlignment(SwingConstants.LEFT);

    nd = new NdEdit((Container)fr, false, false);

    NdEditActionList ndalist = nd.getActions();
    JPanel disclosureCont = new JPanel();
    disclosureCont.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 1));
    disclosureCont.add(mDiscloseNdEdit);
    disclosureCont.add(new JLabel("                                                         "));

    JPanel jp = new JPanel();
    jp.setLayout(new BorderLayout(0, 0));
    jp.add("North", disclosureCont);
    jp.add("Center", nd);
    nd.setVisible(false);
    fr.getContentPane().add(jp);
    fr.setSize(400, 100);
    fr.validate();
    fr.invalidate();

    // build a swing menubar
    JMenuBar menubar = new JMenuBar();
    //JMenuBar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    // create the menus
    JMenu file = new JMenu(b.getString("kFile"));
    JMenu edit = new JMenu(b.getString("kEditMenu"));
    JMenu view = new JMenu(b.getString("kView"));
    JMenu zoom = new JMenu(b.getString("kZoom"));
    new JMenu(b.getString("kWindows"));
    JMenu help = new JMenu(b.getString("kHelp2"));

    // add file menu items

    JMenuItem opendb = new JMenuItem(b.getString("kOpenDB"));
    opendb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    opendb.setActionCommand("opendb");
    opendb.addActionListener(this);
    file.add(opendb);

    JMenuItem open = new JMenuItem(b.getString("kOpenPtr"));
    open.setActionCommand("open");
    file.add(open);

    JMenuItem browseDapper = new JMenuItem(b.getString("kBrowseDapper"));
    browseDapper.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    browseDapper.setActionCommand("browsedapper");
    browseDapper.addActionListener(this);
    file.add(browseDapper);

    // ndedit file commands
    Vector<?> fa = ndalist.getActions("file");
    boolean addedSelFilesMenu = false;
    for (int i = 0; i < fa.size(); i++) {
      NdEditAction nda = (NdEditAction)fa.elementAt(i);
      if (!nda.getText().equalsIgnoreCase("exit") && !nda.getText().equalsIgnoreCase("close") &&
          !nda.getText().equalsIgnoreCase("print...")) {
        if (nda.getText().equals("-------")) {
          file.addSeparator();
        }
        else {
          String txt = nda.getText();
          KeyStroke ks = nda.getAccelerator();

          if (txt.toLowerCase().indexOf("browse...") >= 0) {
            txt = "Browse Pointer File...";
          }

          JMenuItem ndItem = new JMenuItem(txt);
          fileMenuItems.addElement(ndItem);
          fileMenuActions.addElement(nda);

          if (ks != null) {
          }
          if (txt.toLowerCase().indexOf("new window") >= 0) {
            continue;
          }
          if (txt.toLowerCase().indexOf("save selection") >= 0 && !addedSelFilesMenu) {
            // make a new save item action
            BrowseSelectedPtrsAction nnda = new BrowseSelectedPtrsAction("Create New Database From Selection...",
                new ImageIcon("action.gif"), nda.getParent());
            JMenuItem nndItem = new JMenuItem(nnda.getText());
            nndItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + java.awt.event.InputEvent.ALT_MASK, false));
            nndItem.addActionListener(nnda);
            file.add(nndItem);
            fileMenuItems.addElement(nndItem);
            fileMenuActions.addElement(nnda);
            addedSelFilesMenu = true;
          }

          ndItem.addActionListener(nda);
          file.add(ndItem);
        }
      }
    }

    JMenuItem quit = null;
    if (!NQueryConstants.ISMACOSX) {
      quit = new JMenuItem(b.getString("kExit"));
      quit.setActionCommand("quit");
      file.addSeparator();
      file.add(quit);
    }
    //else {
    //	quit = new JMenuItem(b.getString("kQuit"));
    //	quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    //}

    file.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent evt) {
        for (int i = 0; i < fileMenuItems.size(); i++) {

          JMenuItem ndItem = (JMenuItem)fileMenuItems.elementAt(i);
          if (fileMenuActions.elementAt(i) instanceof NdEditAction) {
            NdEditAction itemAction = (NdEditAction)fileMenuActions.elementAt(i);
            ndItem.setEnabled(itemAction.isEnabled());
          }
          else if (fileMenuActions.elementAt(i) instanceof BrowseSelectedPtrsAction) {
            BrowseSelectedPtrsAction itemAction = (BrowseSelectedPtrsAction)fileMenuActions.elementAt(i);
            ndItem.setEnabled(itemAction.isEnabled());
          }
        }
      }

      public void menuDeselected(MenuEvent evt) {

      }

      public void menuCanceled(MenuEvent evt) {

      }
    });
    menubar.add(file);

    // ndedit edit commands
    Vector<?> ea = ndalist.getActions("edit");
    for (int i = 0; i < ea.size(); i++) {
      NdEditAction nda = (NdEditAction)ea.elementAt(i);
      if (nda.getText().equals("-------")) {
        edit.addSeparator();
      }
      else {
        JMenuItem ndItem = new JMenuItem("NdEdit " + nda.getText());
        editMenuItems.addElement(ndItem);
        editMenuActions.addElement(nda);
        KeyStroke ks = nda.getAccelerator();
        if (ks != null) {
          ndItem.setAccelerator(ks);
        }
        ndItem.addActionListener(nda);
        edit.add(ndItem);
      }
    }

    JMenuItem prefs = new JMenuItem(b.getString("kNQueryPreferences"));
    prefs.setActionCommand("prefs");
    edit.add(prefs);
    menubar.add(edit);

    edit.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent evt) {
        for (int i = 0; i < editMenuItems.size(); i++) {
          JMenuItem ndItem = (JMenuItem)editMenuItems.elementAt(i);
          NdEditAction itemAction = (NdEditAction)editMenuActions.elementAt(i);
          ndItem.setEnabled(itemAction.isEnabled());
        }
      }

      public void menuDeselected(MenuEvent evt) {

      }

      public void menuCanceled(MenuEvent evt) {

      }
    });

    // ndedit view commands
    Vector<?> va = ndalist.getActions("view");
    for (int i = 0; i < va.size(); i++) {
      NdEditAction nda = (NdEditAction)va.elementAt(i);
      if (nda.getText().equals("-------")) {
        view.addSeparator();
      }
      else {
        if (nda.isCheckBox()) {
          JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(nda.getText());
          viewCheckBoxes.addElement(cbItem);
          viewCheckBoxesActions.addElement(nda);
          KeyStroke ks = cbItem.getAccelerator();
          if (ks != null) {
            cbItem.setAccelerator(ks);
          }
          cbItem.addActionListener(nda);
          view.add(cbItem);
        }
        else {
          JMenuItem ndItem = new JMenuItem(nda.getText());
          viewMenuItems.addElement(ndItem);
          viewMenuActions.addElement(nda);
          KeyStroke ks = nda.getAccelerator();
          if (ks != null) {
            ndItem.setAccelerator(ks);
          }
          ndItem.addActionListener(nda);
          view.add(ndItem);
        }
      }
    }
    menubar.add(view);

    view.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent evt) {
        // set the state of the menu checkboxes
        for (int i = 0; i < viewCheckBoxesActions.size(); i++) {
          JCheckBoxMenuItem cbItem = (JCheckBoxMenuItem)viewCheckBoxes.elementAt(i);
          NdEditAction cbItemAction = (NdEditAction)viewCheckBoxesActions.elementAt(i);
          cbItem.setEnabled(cbItemAction.isEnabled());
          cbItem.setSelected(cbItemAction.getState());
        }

        for (int i = 0; i < viewMenuItems.size(); i++) {
          JMenuItem ndItem = (JMenuItem)viewMenuItems.elementAt(i);
          NdEditAction itemAction = (NdEditAction)viewMenuActions.elementAt(i);
          ndItem.setEnabled(itemAction.isEnabled());
        }
      }

      public void menuDeselected(MenuEvent evt) {

      }

      public void menuCanceled(MenuEvent evt) {

      }
    });

    // ndedit zoom commands
    Vector<?> za = ndalist.getActions("zoom");
    for (int i = 0; i < za.size(); i++) {
      NdEditAction nda = (NdEditAction)za.elementAt(i);
      if (nda.getText().equals("-------")) {
        zoom.addSeparator();
      }
      else {
        JMenuItem ndItem = new JMenuItem(nda.getText());
        zoomMenuItems.addElement(ndItem);
        zoomMenuActions.addElement(nda);
        KeyStroke ks = nda.getAccelerator();
        if (ks != null) {
          ndItem.setAccelerator(ks);
        }
        ndItem.addActionListener(nda);
        zoom.add(ndItem);
      }
    }
    menubar.add(zoom);

    zoom.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent evt) {
        for (int i = 0; i < zoomMenuItems.size(); i++) {
          JMenuItem ndItem = (JMenuItem)zoomMenuItems.elementAt(i);
          NdEditAction itemAction = (NdEditAction)zoomMenuActions.elementAt(i);
          ndItem.setEnabled(itemAction.isEnabled());
        }
      }

      public void menuDeselected(MenuEvent evt) {

      }

      public void menuCanceled(MenuEvent evt) {

      }
    });

    if (!NQueryConstants.ISMAC) {
      JMenuItem about = null;
      about = new JMenuItem(b.getString("kAbout"));
      about.setActionCommand("about");
      help.add(about);
      about.addActionListener((ActionListener)this);
      menubar.add(help);
    }

    this.setJMenuBar(menubar);

    // add the listeners
    open.addActionListener(this);
    if (quit != null) {
      quit.addActionListener(this);
    }
    prefs.addActionListener(this);

    this.setTitle("NQuery 1.0");
  }

  public static void main(String args[]) {
    if (NQueryConstants.ISMAC) {
      return;
    }

    new NQuery();
    if (args.length >= 0) {
      // attempt to read file(s) from arguments provided
      for (int i = 0; i < args.length; i++) {
        // get directory and file from the argument
        File file = new File(args[i]);

        if (file.isDirectory()) {

        }

        String directory = file.getParent();
        String filename = file.getName();

        if (directory != null && filename != null) {}
      }
    }
  }

  public void handlePrefs() {

  }

  public void handleOpenFile(File file) {
    String directory = file.getParent();
    String filename = file.getName();
    if (directory != null && filename != null) {}
  }

  public void handleOpenApplication() {
  }

  public void doCommand(String cmd, JFrame frame) {
    if (cmd.equals("close")) {
      this.dispose();
    }
    else if (cmd.equals("about")) {
      AboutNQueryDialog ff = new AboutNQueryDialog(this, true);
      //ff.init();
      ff.pack();

      // show dialog at center of screen
      Rectangle dBounds = ff.getBounds();
      Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
      int x = sd.width / 2 - dBounds.width / 2;
      int y = sd.height / 2 - dBounds.height / 2;
      ff.setLocation(x, y);
      ff.setVisible(true);
    }
    else if (cmd.equals("options")) {
      if (mDiscloseNdEdit.isSelected()) {
        nd.setVisible(true);
        this.setSize(990, 500);
        this.invalidate();
        this.validate();
      }
      else {
        nd.setVisible(false);
        this.pack();
      }
    }
    else if (cmd.equals("help")) {
      /*QBSetupHelpFrame ff = new QBSetupHelpFrame(this, "Quality Byte Setup Help");
          ff.init();
          ff.pack();
          ff.show();	*/
    }
    else if (cmd.equals("quit")) {
      System.exit(0);
    }
    else if (cmd.equals("opendb")) {
      FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if (name.endsWith(".nqdb")) {
            return true;
          }
          else {
            return false;
          }
        }
      };
      boolean keepAsking = true;
      while (keepAsking) {
        FileDialog f = new FileDialog(this, "Open Saved Database", FileDialog.LOAD);
        f.setDirectory(NQueryConstants.DEFAULT_DB_SAVE_DIR);
        f.setFilenameFilter(filter);
        f.setVisible(true);
        String mDirectory = f.getDirectory();
        f.dispose();
        if (mDirectory != null && f.getFile() != null) {
          File inFile = new File(mDirectory, f.getFile());
          try {
            DatabaseDocument doc = new DatabaseDocument(inFile);
            NQueryFormulas.centerFrameOnScreen(doc, false);
            doc.setVisible(true);
            keepAsking = false;
          }
          catch (Exception ex) {
            ex.printStackTrace();
            keepAsking = true;
          }
        }
        else {
          keepAsking = false;
        }
      }
    }
    else if (cmd.equals("open")) {
      // XML data
      boolean keepAsking = true;
      while (keepAsking) {
        FileDialog f = new FileDialog(this, "Open File", FileDialog.LOAD);
        f.show();
        String mDirectory = f.getDirectory();
        f.dispose();
        if (mDirectory != null && f.getFile() != null) {
          File inFile = new File(mDirectory, f.getFile());
          try {
            XMLPtrFileReader xmlReader = new XMLPtrFileReader(inFile);
            NQVariableInspector varUI = new NQVariableInspector();
            NQueryFormulas.centerFrameOnScreen(varUI, false);
            varUI.setAttributes(xmlReader.getAttributes());
            varUI.setFileSets(xmlReader.getFileSets());
            varUI.setVisible(true);
            keepAsking = false;
          }
          catch (Exception ex) {
            keepAsking = false;
            try {
              ;//createKMLFile(inFile);
            }
            catch (Exception ex2) {
              keepAsking = false;
            }
          }
        }
        else {
          keepAsking = false;
        }
      }
    }
    else if (cmd.equals("prefs")) {
      //Open the preferences dialog
      ConfigNQPreferences prf = new ConfigNQPreferences(frame, null);
      prf.pack();
      prf.setVisible(true);
    }
    else if (cmd.equals("browsedapper")) {
      // present the dapper interface
      if (mDapperWiz == null) {
        mDapperWiz = new DapperWizard(NQueryConstants.DEFAULT_DAPPER_SERVERS, false);
        mDapperWiz.addSelectionListener(this);
      }
      else {
        mDapperWiz.reset();
      }
      mDapperWiz.setVisible(true);
    }
  }

  private class PowerObs {
    private String currState = "lightson";
    private String mAddr;
    private GeoDate mOffDate;
    private GeoDate mOnDate;
    private String mLoc;
    boolean lightsOff = false;

    public PowerObs(String addr, GeoDate off, GeoDate on, String loc) {
      mAddr = addr;
      mOffDate = off;
      mOnDate = on;
      mLoc = loc;

      if (on == null) {
        currState = "lightsmissing";
      }
    }

    public String getStyle(GeoDate inDate) {
      GeoDate tDate = new GeoDate(inDate);
      tDate.increment(1.0, GeoDate.HOURS);

      if (mOffDate != null && (mOffDate.getTime() >= inDate.getTime() && mOffDate.getTime() < tDate.getTime())) {
        currState = "lightsoff";
        lightsOff = true;
      }
      else if (mOnDate != null && (mOnDate.getTime() >= inDate.getTime() && mOnDate.getTime() < tDate.getTime())) {
        if (lightsOff) {
          currState = "lightsjuston";
          lightsOff = false;
        }
        else {
          currState = "lightson";
        }
      }
      else {
        // no change of state
        if (!lightsOff) {
          currState = "lightson";
        }
      }

      // change
      return currState;
    }

    public String getAddress() {
      return mAddr;
    }

    public String getLocation() {
      return mLoc;
    }
  }

  public void createKMLFile(File inFile) throws Exception {
    // open file

    // read a TSV file with fields address off date, off time, on date, on time, loaction
    // into arrays

    int addrPos = 1;
    int offDatePos = 2;
    int offTimePos = 3;
    int onDatePos = 4;
    int onTimePos = 5;
    int lonPos = 6;
    int latPos = 7;
    long bytesRead = 0;
    EPSProperties.SDELIMITER = EPSProperties.SCOMMA_DELIMITER;

    try {
      LineNumberReader in = new LineNumberReader(new FileReader(inFile), 10000);

      // skip the column header line
      String inLine = in.readLine();
      bytesRead += inLine.length();

      boolean eof = false;
      while (!eof) {
        inLine = in.readLine();
        if (inLine == null) {
          break;
        }
        bytesRead += inLine.length();

        String addr = EPS_Util.getItem(inLine, addrPos);
        String offDate = EPS_Util.getItem(inLine, offDatePos);
        String offTime = EPS_Util.getItem(inLine, offTimePos);
        String onDate = EPS_Util.getItem(inLine, onDatePos);
        String onTime = EPS_Util.getItem(inLine, onTimePos);
        String lon = EPS_Util.getItem(inLine, lonPos);
        String lat = EPS_Util.getItem(inLine, latPos);
        String location = lon + "," + lat + ",0";

        String offStr = offDate + " " + offTime;
        String onStr = onDate + " " + onTime;

        // make GeoDates
        GeoDate on = null;
        GeoDate off = null;

        if (offStr.indexOf("missing") < 0) {
          try {
            off = new GeoDate(offStr, "MM/dd/yyyy HH:mm");
          }
          catch (Exception ex1) {}
        }

        if (onStr.indexOf("missing") < 0) {
          try {
            on = new GeoDate(onStr, "MM/dd/yyyy HH:mm");
          }
          catch (Exception ex2) {}
        }

        // make an observation Object
        PowerObs obs = new PowerObs(addr, off, on, location);
        mAllObs.addElement(obs);
      }
      save("storm2006_final.kml");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void save(String suggestedName) {
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir");
    FileDialog f = new FileDialog(fr, "Save kml file", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFile(suggestedName);
    f.show();
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();
    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
        saveKMLFile(nf);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public void saveKMLFile(File file) {
    // now create the frames kml file
    try {
      // start and end dates of simulation
      GeoDate startDate = new GeoDate(12, 12, 2006, 10, 0, 0, 0);
      startDate.decrement(8.0, GeoDate.HOURS);

      GeoDate endDate = new GeoDate(12, 25, 2006, 0, 0, 0, 0);
      endDate.decrement(8.0, GeoDate.HOURS);
      GeoDate currTime = new GeoDate(startDate);

      // create a documentobject
      Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

      Element root = doc.createElement("Document");
      Element item = doc.createElement("name");
      item.appendChild(doc.createTextNode("Storm2006.kml"));
      root.appendChild(item);

      // symbol styles
      Element styleitem = doc.createElement("Style");
      styleitem.setAttribute("id", "lightson");
      Element isitem = doc.createElement("IconStyle");
      Element colitem = doc.createElement("color");
      colitem.appendChild(doc.createTextNode("b3ffffff"));
      Element scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("0.5"));
      Element iconitem = doc.createElement("Icon");
      Element hrefitem = doc.createElement("href");
      hrefitem.appendChild(doc.createTextNode("/Users/oz/Desktop/ylw-stars.png"));
      iconitem.appendChild(hrefitem);
      isitem.appendChild(colitem);
      isitem.appendChild(scitem);
      isitem.appendChild(iconitem);
      styleitem.appendChild(isitem);
      Element lblstyleitem = doc.createElement("LabelStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("1.1"));
      lblstyleitem.appendChild(scitem);
      styleitem.appendChild(lblstyleitem);
      root.appendChild(styleitem);

      styleitem = doc.createElement("Style");
      styleitem.setAttribute("id", "lightsjuston");
      isitem = doc.createElement("IconStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("0.7"));
      iconitem = doc.createElement("Icon");
      hrefitem = doc.createElement("href");
      hrefitem.appendChild(doc.createTextNode("/Users/oz/Desktop/brtylw-stars.png"));
      iconitem.appendChild(hrefitem);
      isitem.appendChild(scitem);
      isitem.appendChild(iconitem);
      styleitem.appendChild(isitem);
      lblstyleitem = doc.createElement("LabelStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("1.1"));
      lblstyleitem.appendChild(scitem);
      styleitem.appendChild(lblstyleitem);
      root.appendChild(styleitem);

      styleitem = doc.createElement("Style");
      styleitem.setAttribute("id", "lightsoff");
      isitem = doc.createElement("IconStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("0.5"));
      iconitem = doc.createElement("Icon");
      hrefitem = doc.createElement("href");
      hrefitem.appendChild(doc.createTextNode("/Users/oz/Desktop/gry-blank-1.png"));
      iconitem.appendChild(hrefitem);
      isitem.appendChild(scitem);
      isitem.appendChild(iconitem);
      styleitem.appendChild(isitem);
      lblstyleitem = doc.createElement("LabelStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("1.1"));
      lblstyleitem.appendChild(scitem);
      styleitem.appendChild(lblstyleitem);
      root.appendChild(styleitem);

      styleitem = doc.createElement("Style");
      styleitem.setAttribute("id", "lightsmissing");
      isitem = doc.createElement("IconStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("0.5"));
      iconitem = doc.createElement("Icon");
      hrefitem = doc.createElement("href");
      hrefitem.appendChild(doc.createTextNode("/Users/oz/Desktop/blk-blank-1.png"));
      iconitem.appendChild(hrefitem);
      isitem.appendChild(scitem);
      isitem.appendChild(iconitem);
      styleitem.appendChild(isitem);
      lblstyleitem = doc.createElement("LabelStyle");
      scitem = doc.createElement("scale");
      scitem.appendChild(doc.createTextNode("1.1"));
      lblstyleitem.appendChild(scitem);
      styleitem.appendChild(lblstyleitem);
      root.appendChild(styleitem);

      // create the outer folder item
      Element alFramesFolder = doc.createElement("Folder");
      item = doc.createElement("name");
      item.appendChild(doc.createTextNode("frames"));
      alFramesFolder.appendChild(item);
      item = doc.createElement("open");
      item.appendChild(doc.createTextNode("1"));
      alFramesFolder.appendChild(item);

      int t = 0;
      while (true) {
        // get the current time of the animation
        currTime = currTime.increment(1.0, GeoDate.HOURS);
        String seq = String.valueOf(t - 56);
        while (seq.length() < 3) {
          seq = "0" + seq;
        }

        System.out.println(seq + " " + currTime.toString("MMM-dd-EEE hh:mm aa"));
        if (currTime.getTime() > endDate.getTime()) {
          break;
        }

        // make a folder for each frame
        Element frameFolder = doc.createElement("Folder");
        item = doc.createElement("name");
        item.appendChild(doc.createTextNode("frame#" + String.valueOf(t) + " " + currTime.toString()));
        frameFolder.appendChild(item);
        item = doc.createElement("open");
        item.appendChild(doc.createTextNode("0"));
        frameFolder.appendChild(item);

        /*item = doc.createElement("TimeStamp");
                item.setAttribute("id", String.valueOf(t));
         Element witem = doc.createElement("when");
         String ts = currTime.toString("yyyy-MM-dd hh:mm:ss") + "Z";
         String ts2 = ts.substring(0, 10) + "T" +  ts.substring(11, ts.length());
         witem.appendChild(doc.createTextNode(ts2));
                item.appendChild(witem);
                frameFolder.appendChild(item);*/

        for (int i = 0; i < mAllObs.size(); i++) {
          // make a placemark node
          PowerObs obs = (PowerObs)mAllObs.elementAt(i);
          Element placeMark = doc.createElement("Placemark");
          item = doc.createElement("open");
          item.appendChild(doc.createTextNode("0"));
          placeMark.appendChild(item);

          item = doc.createElement("address");
          item.appendChild(doc.createTextNode(obs.getAddress()));
          placeMark.appendChild(item);

          item = doc.createElement("description");
          item.appendChild(doc.createTextNode(obs.getAddress()));
          placeMark.appendChild(item);

          item = doc.createElement("styleUrl");
          item.appendChild(doc.createTextNode(obs.getStyle(currTime)));
          placeMark.appendChild(item);

          Element ptitem = doc.createElement("Point");
          Element coorditem = doc.createElement("coordinates");
          coorditem.appendChild(doc.createTextNode(obs.getLocation()));
          ptitem.appendChild(coorditem);
          placeMark.appendChild(ptitem);

          frameFolder.appendChild(placeMark);
        }
        alFramesFolder.appendChild(frameFolder);
        t++;
      }

      root.appendChild(alFramesFolder);
      doc.appendChild(root);
      ((TXDocument)doc).setVersion("1.0");
      ((TXDocument)doc).printWithFormat(new FileWriter(file));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // a new comment
  public void handleQuit() {
    System.exit(0); // Quit the MRJ runtime
  }

  public void handleAbout() {
    AboutNQueryDialog ff = new AboutNQueryDialog(this, true);
    //ff.init();
    ff.pack();

    // show dialog at center of screen
    Rectangle dBounds = ff.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    ff.setLocation(x, y);
    ff.setVisible(true);
  }

  /**
   * selectionPerformed
   *
   * @param e SelectionEvent
   * This is how dapper data are handled
   */
  public void selectionPerformed(SelectionEvent e) {
    StationCollection coll = e.getCollection();
    GeoDomain[] refs = e.getReferences();
    String dataset = e.getDataSet();

    NQVariableInspector varUI = new NQVariableInspector(coll, refs, dataset);
    NQueryFormulas.centerFrameOnScreen(varUI, false);
    varUI.setVisible(true);
  }
}

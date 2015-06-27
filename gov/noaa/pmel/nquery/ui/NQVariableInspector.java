/*
 * $Id: VariableInspector.java,v 1.63 2005/11/01 21:48:22 oz Exp $
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

import javaoceanatlas.utility.ButtonMaintainer;
import javaoceanatlas.utility.DialogClient;
import javaoceanatlas.utility.TenPixelBorder;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import gov.noaa.pmel.eps2.PointerFileAttributes;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import java.awt.Rectangle;
import gov.noaa.pmel.nc2.station.StationCollection;
import gov.noaa.pmel.nc2.station.GeoDomain;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import gov.noaa.pmel.eps2.EPSProperties;
import gov.noaa.pmel.eps2.Lexicon;
import gov.noaa.pmel.eps2.ExportVariable;
import java.awt.Font;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import java.awt.FileDialog;
import java.io.File;
import gov.noaa.pmel.eps2.XMLPtrFileReader;
import javax.swing.tree.MutableTreeNode;
import java.util.Iterator;
import javax.swing.tree.TreeNode;
import javax.swing.JDialog;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.utility.NQueryCalculation;
import gov.noaa.pmel.eps2.CalculatedVariable;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import javaoceanatlas.ui.JOAViewer;
import gov.noaa.pmel.nquery.utility.NQueryFormulas;
import javaoceanatlas.ui.JOAWindow;
import javaoceanatlas.ui.RubberbandPanel;
import java.awt.event.WindowEvent;
import javaoceanatlas.PowerOceanAtlas;
import javaoceanatlas.events.WindowsMenuChangedListener;
import javaoceanatlas.events.WindowsMenuChangedEvent;
import java.awt.Toolkit;
import javaoceanatlas.ui.widgets.SmallIconButton;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.Orientation;
import java.util.Enumeration;

/**
 * Provides a <code>JTree</code> view of a parsed XML Pointer.
 *
 * @author John Osborne from code by Donald Denbo
 * @version $Revision: 1.63 $, $Date: 2005/11/01 21:48:22 $
 */

public class NQVariableInspector extends JOAWindow implements ButtonMaintainer, ItemListener, DialogClient,
    ActionListener, NQueryMenuBarClient, JOAViewer, WindowsMenuChangedListener {
  private PointerFileAttributes mGlobalAttributes;
  private ArrayList mFileSets;
  private JTree mTreeView;
  private DefaultMutableTreeNode rootNode;
  private DefaultMutableTreeNode dataTypeNode;
  private BorderLayout borderLayout1 = new BorderLayout(0, 0);
  private FlowLayout flowLayout1 = new FlowLayout(FlowLayout.CENTER, 5, 5);
  private DefaultMutableTreeNode profileNode;
  private DefaultMutableTreeNode timeSeriesNode;
  private DefaultMutableTreeNode gridNode;
  private DefaultMutableTreeNode trackNode;
  private javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
  private javax.swing.JButton expandButton = new javax.swing.JButton();
  private javax.swing.JButton collapseButton = new javax.swing.JButton();
  private javax.swing.JButton editButton = new javax.swing.JButton();
  private javax.swing.JScrollPane treeScrollPane = new javax.swing.JScrollPane();
  // Used for addNotify check.
  private boolean fComponentsAdjusted = false;
  private JTree tree;
  private DefaultTreeModel mTreeModel;
  private DefaultMutableTreeNode parentNode = null;
  private TreePath parentPath = null;
  //protected JComboBox mProfileCalcPopup;
  private ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
  private ImageIcon mChecked;
  private ImageIcon mUnchecked;
  private int selRow;
  private TreePath selPath;
  private Rectangle selRect;
  private ArrayList mGlobalVars;
  private boolean ignore = false;
  private boolean mCanCalcProfiles = false;
  private boolean mCanCalcTS = false;
  private StationCollection mStnCollection = null;
  private GeoDomain[] nStnRefs;
  protected SmallIconButton checkAll = null;
  protected SmallIconButton checkNone = null;
  private String mDatasetName;
  private String mSeverName;
  private boolean DEBUG = true;
  private String mDataset;
	private Timer timer = new Timer();

  public boolean isDapper() {
    return!(mStnCollection == null);
  }

  public GeoDomain[] getStnRefs() {
    return nStnRefs;
  }

  public StationCollection getStationCollection() {
    return mStnCollection;
  }

  public void itemStateChanged(ItemEvent evt) {
    if (ignore) {
      ignore = false;
      return;
    }

    if (evt.getSource() instanceof JComboBox) {}
  }

  public NQVariableInspector() {
    super(true, true, true, true, true, null);
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    EPSProperties.SDELIMITER = "\t";
    PowerOceanAtlas.getInstance().addOpenFileViewer(this);
    PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public NQVariableInspector(StationCollection coll, GeoDomain[] refs, String dataset) {
    super(true, true, true, true, true, null);
    mStnCollection = coll;
    nStnRefs = refs;
    ArrayList lats = new ArrayList();
    ArrayList lons = new ArrayList();
    ArrayList verts = new ArrayList();
    ArrayList timesordates = new ArrayList();
    mGlobalVars = new ArrayList();
    mDataset = dataset;

    mDatasetName = mStnCollection.getName();
    mSeverName = mStnCollection.getPathName();
    int pos = mSeverName.indexOf(mDatasetName);

    if (mSeverName.indexOf("dods") >= 0) {
      mSeverName = mSeverName.substring(0, pos);
      mSeverName = mSeverName.replaceFirst("dods:", "http:");
    }

    // get the variables
    String[] varList = mStnCollection.getCollectionVariables();
    String[] varUnits = mStnCollection.getCollectionVariablesUnits();

    for (int i = 0; i < varList.length; i++) {
      String name = varList[i];
      String units = varUnits[i];
      String lex = "unk";

      name = name.toUpperCase();
      String joaName = null;

      if (DEBUG) {
        System.out.println("VI considering = " + name);
      }
      // ignore obvious QC variables
      if (name.indexOf("FLAG") >= 0 || name.indexOf("QC") >= 0) {
        if (DEBUG) {
          System.out.println("Ignoring = " + name);
        }
        continue;
      }

      int epicCode = -99;
      // isolate variables that have embedded epic codes-this will determine the lexicon
      if (name.indexOf("_") > 0) {
        try {
          epicCode = Integer.valueOf(name.substring(name.lastIndexOf("_") + 1, name.length())).intValue();
          if (epicCode > 4200 && epicCode <= 4300) {
            // hack for IPRC dapper server
            lex = "WOCE";
          }
          else {
            lex = "EPIC";
          }
          name = name.substring(0, name.lastIndexOf("_"));
        }
        catch (Exception ex) {
	  // some other variable
        }
      }
      else {
        // attempt to translate a variable to the JOA lexicon
        joaName = Lexicon.paramNameToJOAName(name);

        if (joaName == null) {
          // couldn't find a match--try the Lexicon
          joaName = NQueryConstants.LEXICON.translate(Lexicon.JOA_LEXICON, name);
        }

        if (joaName != null) {
          // store the lexicon in the variable
          lex = "JOA";

          // make sure the units are set
          if (units == null || units.length() == 0) {
            units = NQueryConstants.LEXICON.paramNameToJOAUnits(false, joaName);
          }
        }
      }

      if (units == null || units.length() == 0) {
        units = "na";
      }

      if (DEBUG) {
        System.out.println("VI found lexicon for " + name + " = " + lex);
      }

      mGlobalVars.add(new MyExportVariable(name, joaName, units, lex, epicCode));
    }

    mGlobalAttributes = new PointerFileAttributes("orig ptr file name", mStnCollection.getPathName(), coll.getDataType(),
                                                  lats, lons, verts, timesordates, mGlobalVars);
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    this.setTitle(mDataset + " (Tree View)");
    tree = makeTree(dataset);
    treeScrollPane.setViewportView(tree);

    int row = 0;
    while (row < mTreeView.getRowCount()) {
      if (mTreeView.isCollapsed(row)) {
        mTreeView.expandRow(row);
      }
      row++;
    }
    PowerOceanAtlas.getInstance().addOpenFileViewer(this);
    PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  void jbInit() throws Exception {
    try {
      mChecked = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/checkbox_checked.gif"));
      mUnchecked = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/checkbox_unchecked.gif"));
      checkAll = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/checkall.gif")));
      checkNone = new SmallIconButton(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/checknone.gif")));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    	System.out.println("NQVariableInspector:jbInit");
    }

    JPanel allNoneContCont = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 5));
    JPanel allNoneCont = new JPanel();
    allNoneCont.setLayout(new GridLayout(2, 1, 0, 5));
    allNoneCont.add(checkAll);
    allNoneCont.add(checkNone);
    checkAll.addActionListener(this);
    checkNone.addActionListener(this);
    checkAll.setActionCommand("all");
    checkNone.setActionCommand("none");
    allNoneContCont.add(allNoneCont);

    getContentPane().setLayout(borderLayout1);
    getContentPane().setFont(new Font("Dialog", Font.PLAIN, 12));
    setSize(520, 400);

    setVisible(false);
    buttonPanel.setLayout(flowLayout1);
    getContentPane().add(buttonPanel, "South");
    expandButton.setText(b.getString("kExpandAll"));
    expandButton.setActionCommand("Expand All");
    buttonPanel.add(expandButton);
    collapseButton.setText(b.getString("kCollapseAll"));
    collapseButton.setActionCommand("Collapse All");
    buttonPanel.add(collapseButton);

    editButton.setText(b.getString("kEdit"));
    editButton.setActionCommand("Edit");
    buttonPanel.add(editButton);
    getContentPane().add(new TenPixelBorder(treeScrollPane, 5, 5, 5, 5), "Center");
    getContentPane().add(BorderLayout.EAST, allNoneContCont);

    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    SymAction lSymAction = new SymAction();
    //closeButton.addActionListener(lSymAction);
    expandButton.addActionListener(lSymAction);
    collapseButton.addActionListener(lSymAction);
    editButton.addActionListener(lSymAction);

    mMenuBar = new NQueryMenuBar(this, true, true);

    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        closeMe();
      }

      public void windowActivated(WindowEvent we) {
        if (!NQueryConstants.ISMAC) {
          return;
        }
      }
    };
    this.addWindowListener(windowListener);
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

  public NQVariableInspector(String title) {
    this();
    setTitle(title);
    mDataset = title;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    doCommand(cmd, this);
  }

  public void lookForVarNodes(DefaultMutableTreeNode node, boolean b) {
    Enumeration children = node.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode cnode = (DefaultMutableTreeNode)children.nextElement();
      if (cnode.getUserObject() instanceof VariableNode) {
        VariableNode vn = (VariableNode)cnode.getUserObject();
        vn.setSelected(b);
      }
      else {
        lookForVarNodes(cnode, b);
      }
    }
  }

  public void doCommand(String cmd, JFrame frame) {
    if (cmd.equals("none")) {
      // unselect all selected stations
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)mTreeModel.getRoot();
      lookForVarNodes(root, false);
      checkNone.setSelected(false);
      mTreeModel.nodeChanged(root);
    }
    else if (cmd.equals("all")) {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)mTreeModel.getRoot();
      lookForVarNodes(root, true);
      checkAll.setSelected(false);
      mTreeModel.nodeChanged(root);
    }
    else if (cmd.equals("add")) {
      boolean keepAsking = true;
      while (keepAsking) {
        FileDialog f = new FileDialog(this, "Add File", FileDialog.LOAD);
        f.setVisible(true);
        String mDirectory = f.getDirectory();
        f.dispose();
        if (mDirectory != null && f.getFile() != null) {
          File inFile = new File(mDirectory, f.getFile());
          try {
            XMLPtrFileReader xmlReader = new XMLPtrFileReader(inFile);
            ArrayList al = xmlReader.parse();

            addDataTypeNodes(xmlReader.getAttributes());
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
    else if (cmd.equals("calcmld")) {
      ConfigNQMLDCalc customDialog = new ConfigNQMLDCalc(this, mGlobalVars, this);
      customDialog.pack();
      customDialog.setVisible(true);
    }
    else if (cmd.equals("calcinterp")) {
      ConfigNQInterpolationCalc customDialog = new ConfigNQInterpolationCalc(this, mGlobalVars, this);
      customDialog.pack();
      customDialog.setVisible(true);
    }
    else if (cmd.equals("calcinteg")) {
      ConfigNQIntegrationCalc customDialog = new ConfigNQIntegrationCalc(this, mGlobalVars, this);
      customDialog.pack();
      customDialog.setVisible(true);
    }
    else if (cmd.equals("calcparam")) {
      ConfigNQCalculations customDialog = new ConfigNQCalculations(this, mGlobalVars, this);
      customDialog.pack();
      customDialog.setVisible(true);
    }
    else if (cmd.equals("create")) {
        ConfigSavedDB config = new ConfigSavedDB(this, mDataset, this);
        config.pack();
        config.setVisible(true);
    }
    else if (cmd.equals("about")) {
      /*AboutDialog ff = new AboutDialog(this, true);
             //ff.init();
             ff.pack();

             // show dialog at center of screen
             Rectangle dBounds = ff.getBounds();
             Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
             int x = sd.width / 2 - dBounds.width / 2;
             int y = sd.height / 2 - dBounds.height / 2;
             ff.setLocation(x, y);
             ff.show();*/
    }
    else if (cmd.equals("close")) {
      closeMe();
      timer.cancel();
      this.dispose();
    }
  }

  public JTree makeTree() {
    mTreeModel = getTreeModel();
    mTreeView = new JTree(mTreeModel);
    MouseListener ml = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        selRow = mTreeView.getRowForLocation(e.getX(), e.getY());
        selPath = mTreeView.getPathForLocation(e.getX(), e.getY());
        if (selRow != -1) {
          Rectangle selRect = mTreeView.getRowBounds(selRow);
          if (e.getX() >= selRect.getMinX() && e.getX() <= selRect.getMinX() + 12) {
            // got single click
            singleClick();
          }
          else if ((e.getClickCount() == 2) || ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)) {
            doubleClick();
          }
        }
      }
    };
    mTreeView.addMouseListener(ml);
    try {
      if (mChecked != null && mUnchecked != null) {
        mTreeView.setCellRenderer(new MyRenderer());
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return mTreeView;
  }

  public JTree makeTree(String rootName) {
    mTreeModel = getTreeModel(rootName);
    mTreeView = new JTree(mTreeModel);
    MouseListener ml = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        selRow = mTreeView.getRowForLocation(e.getX(), e.getY());
        selPath = mTreeView.getPathForLocation(e.getX(), e.getY());
        if (selRow != -1) {
          Rectangle selRect = mTreeView.getRowBounds(selRow);
          if (e.getX() >= selRect.getMinX() && e.getX() <= selRect.getMinX() + 12) {
            // got single click
            singleClick();
          }
          else if ((e.getClickCount() == 2) || ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)) {
            doubleClick();
          }
        }
      }
    };
    mTreeView.addMouseListener(ml);
    try {
      if (mChecked != null && mUnchecked != null) {
        mTreeView.setCellRenderer(new MyRenderer());
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return mTreeView;
  }

  void singleClick() {
    Object[] objs = selPath.getPath();
    Object thing = ((DefaultMutableTreeNode)objs[objs.length - 1]).getUserObject();

    if (thing instanceof VariableNode) {
      // get the Variable object behind this leaf
      ((VariableNode)thing).setSelected(!((VariableNode)thing).isSelected());
      double offset = 1;
      this.repaint();
    }
  }

  void doubleClick() {
    getProfileVariables();
    Object[] objs = selPath.getPath();
    Object thing = ((DefaultMutableTreeNode)objs[objs.length - 1]).getUserObject();
    if (thing instanceof CalculatedVariableNode) {
      ((CalculatedVariableNode)thing).configureVariable();
    }
  }

  public DefaultTreeModel getTreeModel() {
    rootNode = new DefaultMutableTreeNode(mGlobalAttributes.getOrigFilename());
    rootNode.add(getDataTypeNodes());
    return new DefaultTreeModel(rootNode);
  }

  public DefaultTreeModel getTreeModel(String rootName) {
    rootNode = new DefaultMutableTreeNode(rootName);
    rootNode.add(getDataTypeNodes());
    return new DefaultTreeModel(rootNode);
  }

  public void addDataTypeNodes(PointerFileAttributes inAttribs) {
    if (inAttribs.getType().equalsIgnoreCase("profile")) {
      Iterator itor = mGlobalVars.iterator();
      while (itor.hasNext()) {
        MyExportVariable var = (MyExportVariable)itor.next();
        profileNode.add(getVariableNode(var));
      }
      mCanCalcProfiles = true;
    }

    if (mGlobalAttributes.getType().equalsIgnoreCase("time-series")) {
      // add in the time series variables
      if (inAttribs.getVarList() != null) {
        // iterate through the global variable list
        mGlobalVars = inAttribs.getVarList();
        Iterator itor = mGlobalVars.iterator();
        while (itor.hasNext()) {
          MyExportVariable var = (MyExportVariable)itor.next();
          timeSeriesNode.add(getVariableNode(var));
        }
        mCanCalcTS = true;
      }
      else {
        // TODO: parse the filesets and compose a variable list
      }
    }
  }

  public MutableTreeNode getDataTypeNodes() {
    dataTypeNode = new DefaultMutableTreeNode("Data Types");
    if (mGlobalAttributes.getType().equalsIgnoreCase("profile")) {
      profileNode = new DefaultMutableTreeNode("Profiles");
      // add in the profile variables
      Iterator itor = mGlobalVars.iterator();
      while (itor.hasNext()) {
        MyExportVariable var = (MyExportVariable)itor.next();
        profileNode.add(getVariableNode(var));
      }
      mCanCalcProfiles = true;
      dataTypeNode.add(profileNode);
    }
    else {
      profileNode = new DefaultMutableTreeNode("Profiles");
      dataTypeNode.add(profileNode);
    }

    if (mGlobalAttributes.getType().equalsIgnoreCase("time-series")) {
      timeSeriesNode = new DefaultMutableTreeNode("Time Series");
      // add in the profile variables
      if (mGlobalAttributes.getVarList() != null) {
        // iterate through the global variable list
        mGlobalVars = mGlobalAttributes.getVarList();
        Iterator itor = mGlobalVars.iterator();
        while (itor.hasNext()) {
          MyExportVariable var = (MyExportVariable)itor.next();
          timeSeriesNode.add(getVariableNode(var));
        }
        mCanCalcTS = true;
      }
      else {
        // TODO: parse the filesets and compose a variable list
      }
      // add in the time series variables
      dataTypeNode.add(timeSeriesNode);
    }
    else {
      timeSeriesNode = new DefaultMutableTreeNode("Time Series");
      dataTypeNode.add(timeSeriesNode);
    }

    trackNode = new DefaultMutableTreeNode("Tracks");
    // add in the track variables
    dataTypeNode.add(trackNode);

    gridNode = new DefaultMutableTreeNode("Grids");
    // add in the grids variables
    dataTypeNode.add(gridNode);
    return dataTypeNode;
  }

  public MutableTreeNode getVariableNode(ExportVariable var) {
    DefaultMutableTreeNode varRoot;
    String name = var.getVarName();
    String lex = var.getLexicon();
    String units = var.getVarUnits();
    int epicCode = var.getEPICCode();

    // do any translations required by the display preference
    boolean italics = true;
    if (NQueryConstants.DEFAULT_TRANSLATE_LEXICON) {
      String tname = NQueryConstants.LEXICON.translate(lex, NQueryConstants.DEFAULT_LEXICON, name, epicCode);
      //System.out.println("tname = " + tname);
      if (tname != null) {
        italics = false;
        name = tname.trim();
      }
    }
    else {
      italics = false;
    }

    var.setPresentationVarName(name);

    if (var instanceof CalculatedVariable) {
      // does this var have an argument
      varRoot = new DefaultMutableTreeNode(new CalculatedVariableNode(this, mGlobalVars, (CalculatedVariable)var));
    }
    else {
      varRoot = new DefaultMutableTreeNode(new VariableNode(this, name, ((MyExportVariable)var).getJOAName(), units, "", false, italics));
    }
    return varRoot;
  }

  public void addNotify() {
    // Record the size of the window prior to calling parents addNotify.
    Dimension d = getSize();

    super.addNotify();

    if (fComponentsAdjusted) {
      return;
    }

    // Adjust components according to the insets
    Insets ins = getInsets();
    setSize(ins.left + ins.right + d.width, ins.top + ins.bottom + d.height);
    Component components[] = getContentPane().getComponents();
    for (int i = 0; i < components.length; i++) {
      Point p = components[i].getLocation();
      p.translate(ins.left, ins.top);
      components[i].setLocation(p);
    }
    fComponentsAdjusted = true;
  }

  class SymWindow extends java.awt.event.WindowAdapter {
    public void windowClosing(java.awt.event.WindowEvent event) {
      Object object = event.getSource();
      if (object == NQVariableInspector.this) {
        TreeView_WindowClosing(event);
      }
    }
  }

  void TreeView_WindowClosing(java.awt.event.WindowEvent event) {
    this.setVisible(false);
    dispose(); // dispose of the Frame.
  }

  public void setAttributes(PointerFileAttributes attribs) {
    mGlobalAttributes = attribs;
    mGlobalVars = new ArrayList();
    String wname = mGlobalAttributes.getOrigFilename();
    this.setTitle(wname + " (Tree View)");
    mDataset = wname;

    Iterator itor = attribs.getVarList().iterator();
    while (itor.hasNext()) {
      // iterate through the global variable list
      ExportVariable var = (ExportVariable)itor.next();
      String name = var.getVarName();
      String units = var.getVarUnits();
      String lex = var.getLexicon();
      int epicCode = var.getEPICCode();

      name = name.toUpperCase();
      String joaName = null;

      // ignore obvious QC variables
      if (name.indexOf("FLAG") >= 0 || name.indexOf("QC") >= 0) {
        continue;
      }

      if (lex == null || lex.length() == 0 || lex.equalsIgnoreCase("unk")) {
        // isolate variables that have embedded epic codes-this will determine the lexicon
        if (name.indexOf("_") > 0) {
          try {
            epicCode = Integer.valueOf(name.substring(name.lastIndexOf("_") + 1, name.length())).intValue();
            if (epicCode > 4200 && epicCode <= 4300) {
              // hack for IPRC dapper server
              lex = "WOCE";
            }
            else {
              lex = "EPIC";
            }
            name = name.substring(0, name.lastIndexOf("_"));
          }
          catch (Exception ex) {
          }
        }
        else {
          // attempt to translate a variable to the JOA lexicon
          joaName = NQueryConstants.LEXICON.paramNameToJOAName(name);

          if (joaName == null) {
            // couldn't find a match--try the Lexicon
            joaName = NQueryConstants.LEXICON.translate(Lexicon.JOA_LEXICON, name);
          }

          if (joaName != null) {
            // store the lexicon in the variable
            lex = "JOA";

            // make sure the units are set
            if (units == null || units.length() == 0) {
              units = NQueryConstants.LEXICON.paramNameToJOAUnits(false, joaName);
            }
          }
        }
      }

      if (units == null || units.length() == 0) {
        units = "na";
      }
      mGlobalVars.add(new MyExportVariable(name, joaName, units, lex, epicCode));
    }

    tree = makeTree();
    treeScrollPane.setViewportView(tree);

    int row = 0;
    while (row < mTreeView.getRowCount()) {
      if (mTreeView.isCollapsed(row)) {
        mTreeView.expandRow(row);
      }
      row++;
    }

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public void setFileSets(ArrayList inFS) {
    mFileSets = inFS;
  }

  class SymAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event) {
      Object object = event.getSource();
      //if (object == closeButton)
      //	closeButton_actionPerformed(event);
      if (object == expandButton) {
        expandButton_actionPerformed(event);
      }
      else if (object == collapseButton) {
        collapseButton_actionPerformed(event);
      }
      else if (object == editButton) {
        editButton_actionPerformed(event);
      }
    }
  }

  void editButton_actionPerformed(java.awt.event.ActionEvent event) {
    Object[] objs = selPath.getPath();
    Object thing = ((DefaultMutableTreeNode)objs[objs.length - 1]).getUserObject();
    if (thing instanceof CalculatedVariableNode) {
      ((CalculatedVariableNode)thing).configureVariable();
    }
  }

  //void closeButton_actionPerformed(java.awt.event.ActionEvent event) {
  // add a variable
  //	addObject((getVariableNode(new CalculatedVariable("foo", "foounits", "foolexicon", false))));
  //}

  public DefaultMutableTreeNode addObject(Object child, DefaultMutableTreeNode parentNode) {
    // aprent node is always either "profile", "TS" or other types
    /*parentNode = null;
       parentPath = tree.getSelectionPath();

       if (parentPath == null) {
     //There's no selection. Default to the root node.
     parentNode = rootNode;
       }
       else {
     parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
       }*/

    return addObject(parentNode, child, true);
  }

  public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
    if (child instanceof DefaultMutableTreeNode) {
      mTreeModel.insertNodeInto((DefaultMutableTreeNode)child, parent, parent.getChildCount());
      if (shouldBeVisible) {
        tree.scrollPathToVisible(new TreePath(((DefaultMutableTreeNode)child).getPath()));
      }
      return (DefaultMutableTreeNode)child;
    }
    else {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
      mTreeModel.insertNodeInto(childNode, parent, parent.getChildCount());
      if (shouldBeVisible) {
        tree.scrollPathToVisible(new TreePath(childNode.getPath()));
      }
      return childNode;
    }
  }

  void expandButton_actionPerformed(java.awt.event.ActionEvent event) {
    int row = 0;
    while (row < mTreeView.getRowCount()) {
      if (mTreeView.isCollapsed(row)) {
        mTreeView.expandRow(row);
      }
      row++;
    }
  }

  void collapseButton_actionPerformed(java.awt.event.ActionEvent event) {
    DefaultTreeModel tm = (DefaultTreeModel)mTreeView.getModel();
    TreePath tp = new TreePath(tm.getPathToRoot((TreeNode)dataTypeNode));
    mTreeView.collapsePath(tp);
  }

  // OK Button
  public void dialogDismissed(JDialog d) {
    // get a calculation object from the config dialog
    try {
      Object thing = ((ConfigNQCalcDialog)d).getCalculation();

      if (thing instanceof ArrayList) {
        // iterate through list of calculations and add to tree
        Iterator itor = ((ArrayList)thing).iterator();
        while (itor.hasNext()) {
          NQueryCalculation calc = (NQueryCalculation)itor.next();
          addObject((getVariableNode(new CalculatedVariable(calc.getCalcType(), calc.getUnits(), calc.isEditable(),
              calc.isStationVar(), "JOA", calc))), profileNode);
        }
      }
      else {
        // make a calculated variable node out of it.
        NQueryCalculation calc = (NQueryCalculation)thing;

        // add it to the tree
        addObject((getVariableNode(new CalculatedVariable(calc.getCalcType(), calc.getUnits(), calc.isEditable(),
            calc.isStationVar(), "JOA", calc))), profileNode);
      }
    }
    catch (ClassCastException ccex) {
      try {
        String mDirectory = ((ConfigSavedDB)d).getDirectory();
        String mDBName = ((ConfigSavedDB)d).getName();
        String mFileName = mDBName + ".nqdb";
        String mComment = ((ConfigSavedDB)d).getComment();
        if (mDirectory != null && mFileName != null) {
          File saveFile = new File(mDirectory, mFileName);
          DatabaseDocument dbd = new DatabaseDocument(this, saveFile, mDBName, mGlobalAttributes, mFileSets,
          		NQueryConstants.DEFAULT_DB_URI, NQueryConstants.DEFAULT_DB_PORT, NQueryConstants.DEFAULT_DB_USERNAME,
          		NQueryConstants.DEFAULT_DB_PASSWORD, mComment, mDatasetName, mSeverName);
          NQueryFormulas.locateOnParentFrame(this, dbd);
          dbd.setVisible(true);
        }
      }
      catch (ClassCastException cccex) {
      	cccex.printStackTrace();
      }
    }
  }

  // Cancel button
  public void dialogCancelled(JDialog d) {
    ;
  }

  // something other than the OK button
  public void dialogDismissedTwo(JDialog d) {
    ;
  }

  // Apply button, OK w/o dismissing the dialog
  public void dialogApply(JDialog d) {
    ;
  }

  // Apply button, OK w/o dismissing the dialog
  public void dialogApplyTwo(Object d) {
    ;
  }

  public void maintainButtons() {
    // need to get the currently selected variable
    parentNode = null;

    if (tree == null) {
      return;
    }

    parentPath = tree.getSelectionPath();

    if (parentPath == null) {
      //There's no selection. Default to the root node.
      parentNode = rootNode;
    }
    else {
      parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
    }

    if (parentNode.isLeaf() && parentNode.getUserObject() instanceof CalculatedVariableNode) {
      // get the object behind this leaf--is it a variable or a calculated variable
      if (((VariableNode)parentNode.getUserObject()).isEditable()) {
        editButton.setEnabled(true);
      }
      else {
        editButton.setEnabled(false);
      }
    }
    else {
      editButton.setEnabled(false);
    }
  }

  class MyRenderer extends DefaultTreeCellRenderer {
    Font baseFont = null;
    Font italicFont = null;

    public MyRenderer() {
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
                                                  int row, boolean hasFocus) {

      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
      if (node.getUserObject() instanceof VariableNode) {
        if (baseFont == null) {
          baseFont = getFont();
          italicFont = baseFont.deriveFont(Font.ITALIC);
        }

        VariableNode vn = (VariableNode)node.getUserObject();
        if (vn.isSelected()) {
          setIcon(mChecked);
        }
        else {
          setIcon(mUnchecked);
        }

        if (vn.isItalic()) {
          setFont(italicFont);
        }
        else {
          setFont(baseFont);
        }
      }
      return this;
    }
  }

  public ArrayList getProfileVariables() {
    ArrayList profileVars = new ArrayList();
    // traverse the tree and get the profile variable nodes
    int varCnt = profileNode.getChildCount();
    for (int i = 0; i < varCnt; i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)profileNode.getChildAt(i);
      if (node.getUserObject() instanceof VariableNode) {
        VariableNode vn = (VariableNode)node.getUserObject();
        // only include the selected variables
        if (vn.isSelected()) {
          profileVars.add(vn);
        }
      }
    }
    return profileVars;
  }

  public ArrayList getTimeSeriesVariables() {
    // TODO: get the variables from the time-series node
    return null;
  }

  public boolean isCanCalcProfiles() {
    return mCanCalcProfiles;
  }

  public boolean isCanCalcTS() {
    return mCanCalcTS;
  }

  public void closeMe() {
    PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener)this);
    PowerOceanAtlas.getInstance().removeOpenFileViewer(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public RubberbandPanel getPanel() {
    return null;
  }

  private class MyExportVariable extends ExportVariable {
    private String mJOAName;

    public MyExportVariable(String name, String JOAName, String units, String lex, int epicCode) {
      super(name, units, lex, epicCode);
      mJOAName = JOAName;
    }

    public String getJOAName() {
      return mJOAName;
    }
  }

}

/*
 * $Id: QueryResults.java,v 1.14 2005/09/20 22:06:01 oz Exp $
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

import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.utility.TenPixelBorder;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javaoceanatlas.utility.ButtonMaintainer;
import java.awt.event.ItemListener;
import gov.noaa.pmel.nc2.station.StationCollection;
import java.util.Vector;
import java.awt.event.ItemEvent;
import java.io.FilenameFilter;
import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import gov.noaa.pmel.nquery.utility.NQueryFormulas;
import gov.noaa.pmel.eps2.ExportVariable;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.eps2.ExportVertical;
import gov.noaa.pmel.eps2.ExportDate;
import gov.noaa.pmel.eps2.ExportStation;
import gov.noaa.pmel.eps2.EPSConstants;
import gov.noaa.pmel.eps2.ExportLatitude;
import gov.noaa.pmel.eps2.ExportLongitude;
import gov.noaa.pmel.eps2.ExportFileSet;
import gov.noaa.pmel.eps2.PointerFileAttributes;
import gov.noaa.pmel.eps2.EpicPtrs;
import gov.noaa.pmel.eps2.XMLPtrFileWriter;
import gov.noaa.pmel.eps2.EpicPtrFileWriter;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.sql.ResultSet;
import gov.noaa.pmel.nquery.utility.MyResultSet;
import gov.noaa.pmel.eps2.EpicPtr;
import gov.noaa.pmel.eps2.EPS_Util;
import gov.noaa.pmel.eps2.EPSDbase;
import gov.noaa.pmel.eps2.EPSDBIterator;
import gov.noaa.pmel.eps2.Dbase;
import javaoceanatlas.ui.FileViewer;
import javaoceanatlas.PowerOceanAtlas;
import javaoceanatlas.ui.NameItDialog;
import javaoceanatlas.ui.JOAWindow;
import javaoceanatlas.ui.JOAViewer;
import javaoceanatlas.events.WindowsMenuChangedListener;
import javaoceanatlas.ui.RubberbandPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javaoceanatlas.events.WindowsMenuChangedEvent;
import javax.swing.JProgressBar;
import java.awt.Rectangle;

public class NQQueryResults extends JOAWindow implements ActionListener, ButtonMaintainer, ItemListener, JOAViewer,
    WindowsMenuChangedListener, NQueryMenuBarClient {
  private JButton mOKBtn = null;
  private JButton mOpenButton = null;
  private ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private Timer timer = new Timer();
  private ResultSet mResults;
  private ResultsPanel mResultsPanel;
  private StationCollection mDapperResults = null;
  private String mDBName;
  private Vector mFoundItems;
  boolean isXML = true;
  private String mShortQuery;

  public NQQueryResults(ResultSet results, String db, String shortQuery) {
    super(true, true, true, true, true, null);
    mDBName = db;
    mShortQuery = shortQuery;
    this.setTitle(b.getString("kQueryResults") + mDBName);
    this.setSize(600, 400);
    mResults = results;
    mDapperResults = mDapperResults;
    this.init();
  }

  public void init() {
    // upper panel: holds the crtieria and the command preview
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new BorderLayout(5, 5));

    // build the results panel
    mResultsPanel = new ResultsPanel(mResults);

    // Scroller hold the results
    MyScroller resultsScroller = new MyScroller(new TenPixelBorder(mResultsPanel, 5, 5, 5, 5));

    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kResults"));
    upperPanel.setBorder(tb);

    // build the upper panel
    upperPanel.add(resultsScroller, "Center");

    // lower panel
    mOpenButton = new JButton(b.getString("kOpen2"));
    mOpenButton.setActionCommand("open");
    mOKBtn = new JButton(b.getString("kSaveAsXMLPtr"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOpenButton);
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new BorderLayout(5, 5));
    dlgBtnsPanel.setLayout(new GridLayout(1, 5, 5, 5));
    JPanel slPanel = new JPanel();
    slPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    if (NQueryConstants.ISMAC) {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mOpenButton);
    }
    else {
      dlgBtnsPanel.add(mOpenButton);
      dlgBtnsPanel.add(mOKBtn);
    }
    JPanel c = new JPanel();
    c.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    c.add(dlgBtnsPanel);
    dlgBtnsInset.add(c, "South");

    mOKBtn.addActionListener(this);
    mOpenButton.addActionListener(this);
    this.getContentPane().add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "Center");
    this.getContentPane().add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");

    mMenuBar = new NQueryMenuBar(this, false, false);

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

    resultsScroller.setAutoscrolls(false);
    PowerOceanAtlas.getInstance().addOpenFileViewer(this);
    PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);

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

  public String getTitle() {
    return (mDBName + mShortQuery);
  }

  public RubberbandPanel getPanel() {
    return null;
  }

  public boolean isCanCalcProfiles() {
    return false;
  }

  public boolean isCanCalcTS() {
    return false;
  }

  public void closeMe() {
    PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener)this);
    PowerOceanAtlas.getInstance().removeOpenFileViewer(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public void doCommand(String cmd, JFrame frame) {
    if (cmd.equals("close")) {
      closeMe();
      this.dispose();
    }
  }

  public void itemStateChanged(ItemEvent evt) {
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      save();
    }
    else if (cmd.equals("open")) {
      open();
    }
    else {
      doCommand(cmd, this);
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
    String directory = System.getProperty("user.dir");
    FileDialog f = new FileDialog(fr, "Save query results as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    f.setFile("untitled_nqueryresults.xml");
    f.show();
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();

    if (fs == null) {
      return;
    }

    if (fs.indexOf(".xml") > 0) {
      isXML = true;
    }
    else if (fs.indexOf(".ptr") > 0) {
      isXML = false;
    }

    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
        saveResults(nf);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        JFrame ff = new JFrame("Query Results Save Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(ff, "An error occurred saving the query results");
      }
    }
  }

  public void saveResults(File file) {
    if (mFoundItems == null || mFoundItems.size() == 0) {
      // TODO: present an alert
      return;
    }

    String type;
    String itemID;
    double lat = -99;
    double lon = -99;
    double zmin = -99;
    double zmax = -99;
    double lonMin = 360.0;
    double latMin = 90.0;
    double lonMax = -360.0;
    double latMax = -90.0;
    double zMinV = 9999.0;
    double zMaxV = 0.0;
    GeoDate date = null;
    GeoDate stDate = null;
    GeoDate endDate = null;
    GeoDate minDate = new GeoDate();
    GeoDate maxDate = new GeoDate(0);
    String vars;
    String units;
    String lexicon;
    ArrayList stns = new ArrayList();
    ArrayList filePtrs = new ArrayList();
    ArrayList globalVars = new ArrayList();
    String zunits = "na";
    int stnCnt = 1;
    boolean foundRealLat = false;
    boolean foundRealLon = false;
    boolean foundRealZMin = false;
    boolean foundRealZMax = false;
    String uriPart = "";

    try {
      for (int i = 0; i < mFoundItems.size(); i++) {
        MyResultSet mrs = (MyResultSet)mFoundItems.elementAt(i);

        //itemid, type, longitude,latitude,zmin,zmax,zunits,date
        itemID = mrs.getItemID();
        int ampPos = itemID.indexOf('&');
        String refPart = itemID.substring(0, itemID.length());
        if (ampPos >= 0) {
          refPart = itemID.substring(ampPos, itemID.length());

          if (i == 0) {
            // get the global URI
            uriPart = itemID.substring(0, ampPos);
          }
        }
        type = mrs.getType();
        lon = mrs.getLon();
        lat = mrs.getLat();
        zmin = mrs.getZMin();
        zmax = mrs.getZMax();
        zunits = mrs.getZUnits();
        date = mrs.getDate();
        vars = mrs.getVars();
        units = mrs.getVarUnits();
        lexicon = mrs.getLexicon();

        // have to compute max/min for the domain
        if (lat != NQueryConstants.MISSINGVALUE) {
          latMin = lat < latMin ? lat : latMin;
          latMax = lat > latMax ? lat : latMax;
          foundRealLat = true;
        }

        if (lon != NQueryConstants.MISSINGVALUE) {
          lonMin = lon < lonMin ? lon : lonMin;
          lonMax = lon > lonMax ? lon : lonMax;
          foundRealLon = true;
        }

        if (zmin != NQueryConstants.MISSINGVALUE) {
          zMinV = zmin < zMinV ? zmin : zMinV;
          foundRealZMin = true;
        }

        if (zmax != NQueryConstants.MISSINGVALUE) {
          zMaxV = zmax > zMaxV ? zmax : zMaxV;
          foundRealZMax = true;
        }

        int comp = date.compareTo(minDate);
        if (comp < 0) {
          minDate = date;
        }

        comp = date.compareTo(maxDate);
        if (comp > 0) {
          maxDate = date;
        }

        // collect up the union of measured variables
        String[] varNames = NQueryFormulas.parseCSVString(vars);
        String[] varUnits = NQueryFormulas.parseCSVString(units);

        for (int fv = 0; fv < varNames.length; fv++) {
          boolean varFnd = false;
          for (int v = 0; v < globalVars.size(); v++) {
            ExportVariable evar = (ExportVariable)globalVars.get(v);
            if (evar.getVarName().equalsIgnoreCase(varNames[fv])) {
              varFnd = true;
              break;
            }
          }
          if (!varFnd) {
            // make a new tempVar
            ExportVariable tvar = new ExportVariable(varNames[fv], varUnits[fv], lexicon);
            globalVars.add(tvar);
          }
        }

        if (type.equalsIgnoreCase("profile")) {
          date = new GeoDate(mrs.getDate());

          // Create an ExportStation
          ArrayList verts = new ArrayList();
          verts.add(new ExportVertical(zmin, "top", zunits));
          verts.add(new ExportVertical(zmax, "bottom", zunits));
          ExportDate outDate = new ExportDate(date, "point");
          ExportStation stn = new ExportStation(EPSConstants.NETCDFFORMAT, "NQuery Export", type, mDBName,
                                                String.valueOf(stnCnt++), "1", new ExportLatitude(lat),
                                                new ExportLongitude(lon), outDate, verts, refPart, null,
                                                NQueryConstants.MISSINGVALUE);

          //epPtr.setIsURL(true);
          stns.add(stn);
        }
        else {
          // TODO: time seris pointer selectCMD = "select item_id,data_type,longitude,latitude,zmin,zmax,start_time,end_time from item where item_id=";
          stDate = new GeoDate(mrs.getStartDate());
          endDate = new GeoDate(mrs.getEndDate());

        }
      }

      ExportFileSet expFS = new ExportFileSet(mDBName, null, globalVars, stns);
      filePtrs.add(expFS);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    ExportLatitude expLat1 = new ExportLatitude(latMin, "south");
    ExportLatitude expLat2 = new ExportLatitude(latMax, "north");
    ArrayList lats = new ArrayList();
    lats.add(expLat1);
    lats.add(expLat2);
    ExportLongitude expLon1 = new ExportLongitude(lonMin, "west");
    ExportLongitude expLon2 = new ExportLongitude(lonMax, "east");
    ArrayList lons = new ArrayList();
    lons.add(expLon1);
    lons.add(expLon2);

    // create depth range
    ArrayList verts = new ArrayList();

    if (foundRealZMin) {
      verts.add(new ExportVertical(zMinV, "top", zunits, "down"));
    }
    else {
      verts.add(new ExportVertical(NQueryConstants.MISSINGVALUE, "top", zunits, "down"));
    }

    if (foundRealZMax) {
      verts.add(new ExportVertical(zMaxV, "bottom", zunits, "down"));
    }
    else {
      verts.add(new ExportVertical(NQueryConstants.MISSINGVALUE, "bottom", zunits, "down"));
    }

    // create the time range
    ArrayList dates = new ArrayList();
    dates.add(new ExportDate(minDate, "start"));
    dates.add(new ExportDate(maxDate, "end"));

    // Currently, NQuery doesn't support a notion of global URI
    PointerFileAttributes meta = new PointerFileAttributes(this.getTitle(), uriPart, "profile", lats, lons, verts,
        dates, globalVars);
    meta.setMissingValue(NQueryConstants.MISSINGVALUE);
    meta.setCreator("NQuery");

    // write the pointer file
    try {
      if (isXML) {
        EpicPtrs ptrDB = new EpicPtrs(file, true);
        XMLPtrFileWriter mXMLWriter = new XMLPtrFileWriter(file);
        ptrDB.setWriter(mXMLWriter);
        ptrDB.setXMLAttributes(meta);
        ptrDB.setData(filePtrs);
        ptrDB.writePtrs();
      }
      else {
        EpicPtrs ptrDB = new EpicPtrs(file, false);
        EpicPtrFileWriter mEpicWriter = new EpicPtrFileWriter(file);
        mEpicWriter.setIncludeFileInPath(true);
        ptrDB.setWriter(mEpicWriter);
        ptrDB.setXMLAttributes(meta);
        ptrDB.setData(filePtrs);
        ptrDB.writePtrs();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void open() {
    if (mFoundItems == null || mFoundItems.size() == 0) {
      JFrame f = new JFrame("Database Error");
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(f, "Selected database contains no data to display.");
      return;
    }
    class BasicThread extends Thread {
      String mTitle;

      public BasicThread(String title) {
        mTitle = title;
      }

      public void run() {
        String type;
        String itemID;
        String plat;
        String stnid;
        double lat = -99;
        double lon = -99;
        double zmin = -99;
        double zmax = -99;
        GeoDate date = null;
        JProgressBar progressBar;

        //Where the GUI is constructed:
        progressBar = new JProgressBar(0, mFoundItems.size());
        progressBar.setString("Contacting Server (getting profile data)...");
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JFrame jf = new JFrame();
        JPanel jp = new JPanel();
        jp.add("South", progressBar);
        jf.getContentPane().add("Center", jp);
        jf.pack();
        Rectangle dBounds = jf.getBounds();
        Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
        int x = sd.width / 2 - dBounds.width / 2;
        int y = sd.height / 2 - dBounds.height / 2;
        jf.setLocation(x, y);
        jf.setVisible(true);

        String directory = EPS_Util.getTempDir();
        String ptrsName = mTitle;
        File tFile = new File(directory, ptrsName);

        // parse this pointer file to read these into DBases
        EpicPtrs ptrDB = new EpicPtrs(tFile);
        String ptrFileName = tFile.getName();
        String lcName = ptrFileName.toLowerCase();
        ptrDB.setWriter(new EpicPtrFileWriter(tFile));

        try {
          for (int i = 0; i < mFoundItems.size(); i++) {
            MyResultSet mrs = (MyResultSet)mFoundItems.elementAt(i);

            itemID = mrs.getItemID();
            plat = mrs.getPlatform();
            stnid = mrs.getStnID();
            type = mrs.getType();
            lon = mrs.getLon();
            lat = mrs.getLat();
            zmin = mrs.getZMin();
            zmax = mrs.getZMax();
            date = mrs.getDate();

            EpicPtr epPtr = new EpicPtr(EPSConstants.NETCDFFORMAT, "NQuery", type, plat, stnid, lat, lon, date, zmin,
                                        zmax, null, "", itemID, null);
            ptrDB.setData(epPtr);
          }

          Vector allDbases = new Vector();
          EPSDbase epsDB = new EPSDbase(ptrDB.iterator(), true);
          EPSDBIterator dbItor = epsDB.iterator(true);
          int c = 0;
          while (dbItor != null && dbItor.hasNext()) {
            progressBar.setValue(c++);
            // get the type of database (ptr or section)
            try {
              Dbase db = (Dbase)dbItor.next();
              allDbases.add(db);
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }

          // done with the progress bar
          jf.hide();
          jf.dispose();

          if (allDbases.size() > 0) {
            // build a new fileviewer
            NameItDialog nameIt = new NameItDialog("Name New Data Window", "Section (dataset) name:",
                (ptrsName + " (from query)"));
            nameIt.setModal(true);
            nameIt.setVisible(true);
            String dataset = nameIt.getName();

            Frame fr = new Frame();
            FileViewer fv = new FileViewer(fr, dataset, allDbases);
            PowerOceanAtlas.getInstance().addOpenFileViewer(fv);
            fv.pack();
            fv.setVisible(true);
            fv.setSavedState(JOAConstants.ISCOLLECTION, null);
          }

        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }

    // Create a thread and run it
    Thread thread = new BasicThread(this.getTitle());
    thread.start();
  }

  public void maintainButtons() {
    // test for any changes in the conditions
  }

  private class ResultsPanel extends JPanel {
    protected MyTextArea mResults = null;
    protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

    public ResultsPanel(java.sql.ResultSet rs) {
      mResults = new MyTextArea(20, 75);
      mResults.setWrapStyleWord(true);
      mResults.setLineWrap(true);
      this.add(mResults);
      mFoundItems = this.setContents(rs);
    }

    public String getContents() {
      return mResults.getText();
    }

    public Vector setContents(java.sql.ResultSet rs) {
      Vector foundItems = new Vector();
      try {
        do {
          String id = rs.getString(1);
	  mResults.append(id + "\n");
          String type = rs.getString(2);
          String platform = rs.getString(3);
          String stn = rs.getString(4);
          double lon = rs.getDouble(5);
          double lat = rs.getDouble(6);
          double zmin = rs.getDouble(7);
          double zmax = rs.getDouble(8);
          String zunits = rs.getString(9);
          GeoDate date = new GeoDate(rs.getDate(10));
          String vars = rs.getString(11);
          String units = rs.getString(12);
          String lexicon = rs.getString(13);

          foundItems.add(new MyResultSet(id, platform, stn, type, lon, lat, zmin, zmax, zunits, date, vars, units,
                                         lexicon));
        }
        while (rs.next());
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }

      return foundItems;
    }

  }

  private class MyScroller extends JScrollPane {
    public MyScroller(Component c) {
      super(c);
      this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public Dimension getPreferredSize() {
      return new Dimension(210, 150);
    }
  }

  private class MyTextArea extends JTextArea {
    public MyTextArea(int w, int h) {
      super(w, h);
    }

    public boolean isFocusTraversable() {
      return false;
    }

  }

}

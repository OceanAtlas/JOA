/*
 * $Id: DisplayColumns.java,v 1.4 2005/10/18 23:43:05 oz Exp $
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javax.swing.border.*;
import gov.noaa.pmel.nquery.database.*;
import gov.noaa.pmel.nquery.resources.NQueryConstants;
import javaoceanatlas.utility.ButtonMaintainer;
import javaoceanatlas.utility.TenPixelBorder;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import javax.swing.table.TableColumn;
import javaoceanatlas.ui.JOAViewer;
import javaoceanatlas.events.WindowsMenuChangedEvent;
import javaoceanatlas.PowerOceanAtlas;
import javaoceanatlas.events.WindowsMenuChangedListener;
import javaoceanatlas.ui.JOAWindow;
import javaoceanatlas.ui.RubberbandPanel;

public class DisplayColumns extends JOAWindow implements ActionListener, ButtonMaintainer, ItemListener, JOAViewer,
    WindowsMenuChangedListener, NQueryMenuBarClient {
  private ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");
	private Timer timer = new Timer();
  private Vector mResults;
  private ResultsPanel mResultsPanel;
  private String mDBName;
  // Vector dbver;
  private int vals;
  private String vers;
  private ItemTableDisplay mItemDisplay;
  private DataTableDisplay mDataDisplay;
  public static boolean DEBUG = false;

  public DisplayColumns(Vector results, String dbname) {
    super(true, true, true, true, true, null);
    this.setTitle("About " + dbname + " Database");
    this.setSize(800, 400);
    mResults = results;
    mDBName = dbname;
    // dbver = ver;
    this.init();
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
    return ("About " + mDBName + " Database");
  }

  public RubberbandPanel getPanel() {
    return null;
  }

  public void init() {
    // upper panel: holds the about info and the table displays
    JPanel upperPanel = new JPanel(new BorderLayout(5, 5));

    // build the about panel
    mResultsPanel = new ResultsPanel(mResults);

    // Scroller hold the results
    MyScroller resultsScroller = new MyScroller(new TenPixelBorder(mResultsPanel, 5, 5, 5, 5));

    TitledBorder tb = BorderFactory.createTitledBorder("About " + mDBName);
    upperPanel.setBorder(tb);

    // build the upper panel
    upperPanel.add(BorderLayout.NORTH, resultsScroller);

    //middle panel holds two panels to show the contents od the two tables, data and item
    JPanel middlePanel = new JPanel(new GridLayout(2, 1));
    mItemDisplay = new ItemTableDisplay(mDBName);
    mDataDisplay = new DataTableDisplay(mDBName);

    mItemDisplay.setPreferredScrollableViewportSize(new Dimension(850, 275));

    JScrollPane itemScrollPane = new JScrollPane(mItemDisplay);
    JScrollPane dataScrollPane = new JScrollPane(mDataDisplay);

    middlePanel.add(itemScrollPane);
    middlePanel.add(dataScrollPane);
    upperPanel.add(BorderLayout.CENTER, middlePanel);

    this.getContentPane().add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "Center");

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

		runTimer();

    resultsScroller.setAutoscrolls(false);
    PowerOceanAtlas.getInstance().addOpenFileViewer(this);
    PowerOceanAtlas.getInstance().addWindowsMenuChangedListener(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public void closeMe() {
  	timer.cancel();
    PowerOceanAtlas.getInstance().removeWindowsMenuChangedListener((WindowsMenuChangedListener)this);
    PowerOceanAtlas.getInstance().removeOpenFileViewer(this);

    WindowsMenuChangedEvent pce = new WindowsMenuChangedEvent(PowerOceanAtlas.getInstance());
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(pce);
  }

  public void itemStateChanged(ItemEvent evt) {
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    doCommand(cmd, this);
  }

  public void maintainButtons() {
    // test for any changes in the conditions
  }

  private class ResultsPanel extends JPanel {
    protected JTextArea mResults = null;
    protected ResourceBundle b = ResourceBundle.getBundle("gov.noaa.pmel.nquery.resources.NQueryResources");

    public ResultsPanel(Vector rs) {
      mResults = new JTextArea(20, 75);
      mResults.setWrapStyleWord(true);
      mResults.setLineWrap(true);
      this.add(mResults);
      this.setBorder(BorderFactory.createLineBorder(Color.black));
      mResults.append("\n" + "About " + "'" + mDBName + "'" + " Database ::" + "\n\n");
      try {
        int recs = DatabaseTools.getNumberValidValues(rs, mDBName);
        String ver = DatabaseTools.getDBMeta();
        vals = recs;
        vers = ver;
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      mResults.append("Running MySQL Version :: " + vers + "\n");
      mResults.append("\n" + "Total Valid Values in '" + mDBName + "' :: " + vals + "\n\n");
      mResults.append("Available Columns in '" + mDBName + "' ::\n");
      this.setContents(rs);
      mResults.setCaretPosition(0);
    }

    public String getContents() {
      return mResults.getText();
    }

    public void setContents(Vector rs) {
      try {
        for (int i = 0; i < rs.size(); i++) {
          String st = rs.elementAt(i) + "\n";
          mResults.append(st);
        }
      }
      catch (Exception ex) {}
    }
  }

  private class MyScroller extends JScrollPane {
    public MyScroller(Component c) {
      super(c);
      this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public Dimension getPreferredSize() {
      return new Dimension(210, 150);
    }
  }

  private class ItemTableDisplay extends JTable {
    String mDBName;

    public ItemTableDisplay(String dbname) {
      super();
      this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      mDBName = dbname;
      ItemTableModel model = new ItemTableModel(dbname);
      this.setModel(model);

      int numCols = this.getModel().getColumnCount();
      for (int i = 0; i < numCols; i++) {
        TableColumn tc = this.getColumnModel().getColumn(i);
        tc.setMinWidth(50);
        tc.setResizable(true);
      }
      this.setGridColor(new Color(100, 100, 100));
      this.setShowVerticalLines(true);
    }
  }

  private class DataTableDisplay extends JTable {
    String mDBName;

    public DataTableDisplay(String dbname) {
      super();
      this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      mDBName = dbname;
      DataTableModel model = new DataTableModel(dbname);
      this.setModel(model);

      int numCols = this.getModel().getColumnCount();
      for (int i = 0; i < numCols; i++) {
        TableColumn tc = this.getColumnModel().getColumn(i);
        tc.setMinWidth(50);
        tc.setResizable(true);
      }
      this.setGridColor(new Color(100, 100, 100));
      this.setShowVerticalLines(true);
    }
  }

  private class ItemTableModel extends AbstractTableModel {
    private String[] columnNames = {"item_id", "data_type", "fileset", "id", "longitude", "latitude", "zmin", "zmax",
        "zunits", "date", "start_time", "end_time", "variables", "units", "lexicon"};
    private Object[][] data;
    private int mNumCols = columnNames.length;

    public ItemTableModel(String dbname) {
      super();

      // now read the table values with a select * command
      try {
        ResultSet results = DatabaseTools.selectData("select * from item;", mDBName);

        // get the number of matches
        int lineCount = 0;
        while (results.next()) {
          lineCount++;
        }
        if (DEBUG) {
          System.out.println("lineCount = " + lineCount);
        }

        results.first();
        data = new Object[lineCount][mNumCols];

        int lcount = 0;
        do {
          for (int c = 1; c <= mNumCols; c++) {
            data[lcount][c - 1] = results.getString(c);
            if (DEBUG) {
              System.out.print(results.getString(c) + "\t");
            }
          }
          if (DEBUG) {
            System.out.println();
          }
          lcount++;
        }
        while (results.next());
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.length;
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
      return columnNames[c].getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
      return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
      if (DEBUG) {
        System.out.println("Setting value at " + row + "," + col + " to " + value + " (an instance of " +
                           value.getClass() + ")");
      }

      data[row][col] = value;
      fireTableCellUpdated(row, col);

      if (DEBUG) {
        System.out.println("New value of data:");
        printDebugData();
      }
    }

    private void printDebugData() {
      int numRows = getRowCount();
      int numCols = getColumnCount();

      for (int i = 0; i < numRows; i++) {
        System.out.print("    row " + i + ":");
        for (int j = 0; j < numCols; j++) {
          System.out.print("  " + data[i][j]);
        }
        System.out.println();
      }
      System.out.println("--------------------------");
    }
  }

  private class DataTableModel extends AbstractTableModel {
    private String[] columnNames;
    private Object[][] data;
    private int mNumCols = 0;

    public DataTableModel(String dbname) {
      super();

      // read the column titles for the item table
      try {
        Vector results = DatabaseTools.getColumnNames(dbname, "data", -99);
        mNumCols = results.size() + 1;
        columnNames = new String[mNumCols];
        columnNames[0] = "item_id";
        for (int i = 0; i < mNumCols; i++) {
          columnNames[i + 1] = new String((String)results.elementAt(i));
        }
      }
      catch (Exception ex) {

      }

      // now read the table values with a select * command
      try {
        ResultSet results = DatabaseTools.selectData("select * from data;", mDBName);

        // get the number of matches
        int lineCount = 0;
        while (results.next()) {
          lineCount++;
        }
        results.first();
        data = new Object[lineCount][mNumCols];

        int lcount = 0;
        do {
          for (int c = 1; c <= mNumCols; c++) {
            data[lcount][c - 1] = results.getString(c);
          }
          lcount++;
        }
        while (results.next());
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return data.length;
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
      return columnNames[c].getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
      return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
      if (DEBUG) {
        System.out.println("Setting value at " + row + "," + col + " to " + value + " (an instance of " +
                           value.getClass() + ")");
      }

      data[row][col] = value;
      fireTableCellUpdated(row, col);

      if (DEBUG) {
        System.out.println("New value of data:");
        printDebugData();
      }
    }

    private void printDebugData() {
      int numRows = getRowCount();
      int numCols = getColumnCount();

      for (int i = 0; i < numRows; i++) {
        System.out.print("    row " + i + ":");
        for (int j = 0; j < numCols; j++) {
          System.out.print("  " + data[i][j]);
        }
        System.out.println();
      }
      System.out.println("--------------------------");
    }
  }

  public boolean isCanCalcProfiles() {
    return false;
  }

  public boolean isCanCalcTS() {
    return false;
  }

  public void doCommand(String cmd, JFrame frame) {
    if (cmd.equals("close")) {
      closeMe();
      this.dispose();
    }
  }
}

package javaoceanatlas.ui;

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

import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javaoceanatlas.utility.TenPixelBorder;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javaoceanatlas.utility.ButtonMaintainer;
import java.awt.event.ItemListener;
import java.util.Vector;
import java.awt.event.ItemEvent;
import gov.noaa.pmel.nquery.database.DatabaseTools;
import javax.swing.JScrollPane;
import java.sql.ResultSet;
import javaoceanatlas.resources.JOAConstants;
import javax.swing.JDialog;
import java.sql.Statement;
import java.sql.Connection;
import javax.swing.JList;
import gov.noaa.pmel.nquery.ui.AskForPassword;
import javaoceanatlas.utility.DialogClient;
import javaoceanatlas.utility.RowLayout;
import javaoceanatlas.utility.Orientation;
import javaoceanatlas.ui.widgets.SmallIconButton;
import javax.swing.ImageIcon;
import javax.swing.ListSelectionModel;
import gov.noaa.pmel.nquery.ui.DisplayColumns;
import javax.swing.JOptionPane;
import java.awt.Toolkit;
import javaoceanatlas.ui.widgets.JOAJDialog;
import java.awt.Rectangle;

@SuppressWarnings("serial")
public class MaintainDatabase extends JOAJDialog implements ActionListener, ButtonMaintainer, ItemListener,
    DialogClient {
  protected JButton mDropBtn = null;
  protected JButton mCancelButton = null;
  protected JButton mMoreInfoButton = null;
  protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	private Timer timer = new Timer();
  ResultSet mResults;
  ResultsPanel mResultsPanel;
  String mDBName;
  boolean isXML = true;
  protected SmallIconButton checkAll = null;
  protected SmallIconButton checkNone = null;

  public MaintainDatabase() {
    this.setTitle(b.getString("kMaintainDatabases2"));

    this.init();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

  public void init() {
    // upper panel: holds the crtieria and the command preview
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));

    // get the results of show databases
    try {
      mResults = DatabaseTools.setSQL(new Integer(1), "", "");
    }
    catch (Exception ex) {
      ex.printStackTrace();
      this.dispose();
      this.setVisible(false);

      /**
       * @todo: present an alert
       */
      return;
    }

    // build the results panel
    mResultsPanel = new ResultsPanel();

    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kCurrentDatabases"));
    upperPanel.setBorder(tb);

    // build the upper panel
    upperPanel.add(mResultsPanel);

    JPanel allNoneCont = new JPanel();
    allNoneCont.setLayout(new GridLayout(2, 1, 0, 5));
    checkAll = new SmallIconButton(new ImageIcon(getClass().getResource("images/checkall.gif")));
    allNoneCont.add(checkAll);
    checkNone = new SmallIconButton(new ImageIcon(getClass().getResource("images/checknone.gif")));
    allNoneCont.add(checkNone);
    checkAll.addActionListener(this);
    checkNone.addActionListener(this);
    checkAll.setActionCommand("all");
    checkNone.setActionCommand("none");

    upperPanel.add(allNoneCont);

    // lower panel
    mMoreInfoButton = new JButton(b.getString("kMoreInfo"));
    mMoreInfoButton.setActionCommand("info");
    mDropBtn = new JButton(b.getString("kDropDatabase"));
    mDropBtn.setActionCommand("drop");
    this.getRootPane().setDefaultButton(mMoreInfoButton);
    mCancelButton = new JButton(b.getString("kClose"));
    mCancelButton.setActionCommand("cancel");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new BorderLayout(5, 5));
    dlgBtnsPanel.setLayout(new GridLayout(1, 5, 5, 5));
    JPanel slPanel = new JPanel();
    slPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mDropBtn);
      dlgBtnsPanel.add(mMoreInfoButton);
    }
    else {
      dlgBtnsPanel.add(mMoreInfoButton);
      dlgBtnsPanel.add(mDropBtn);
      dlgBtnsPanel.add(mCancelButton);
    }
    JPanel c = new JPanel();
    c.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
    c.add(dlgBtnsPanel);
    dlgBtnsInset.add(c, "South");

    mDropBtn.addActionListener(this);
    mCancelButton.addActionListener(this);
    mMoreInfoButton.addActionListener(this);
    this.getContentPane().add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "Center");
    this.getContentPane().add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");

    runTimer();
    this.pack();
  }

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

  public void itemStateChanged(ItemEvent evt) {
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("info")) {
      Object[] sel = mResultsPanel.getSelection();
      Connection c = null;

      for (int i = 0; i < sel.length; i++) {
        String dbName = (String)sel[i];
        // get info on the database
        try {
          c = DatabaseTools.createConnection(dbName);
          Statement s = (Statement)c.createStatement();
          s.execute("USE " + dbName);
          String tableData = new String("data");
          Vector<String> results = DatabaseTools.getColumnNames(dbName, tableData, -99);

          if (results != null) {
            DisplayColumns resultWindow = new DisplayColumns(results, dbName);

            // show dialog at center of screen
            Rectangle dBounds = resultWindow.getBounds();
            Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
            int x = sd.width / 2 - dBounds.width / 2;
            int y = sd.height / 2 - dBounds.height / 2;
            resultWindow.setLocation(x, y);
            resultWindow.setVisible(true);
          }
          else {
            //preset error alert
          }
          c.close();
        }
        catch (Exception ex) {
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(new JFrame(), "Can't display information for the selected database(s).");
        }
      }
    }
    else if (cmd.equals("drop")) {
      Object[] sel = mResultsPanel.getSelection();
      for (int i = 0; i < sel.length; i++) {
        String dbName = (String)sel[i];
        if (dbName.equalsIgnoreCase("mysql")) {
          continue;
        }
        String sql = new String("DROP DATABASE " + dbName);
        try {
          DatabaseTools.setSQL(sql);
        }
        catch (Exception ex) {
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(new JFrame(), "Could not drop the selected database(s).");
        }
      }

      // rebuild the list
      mResultsPanel.reBuildList();
    }
    else if (cmd.equals("none")) {
      checkNone.setSelected(false);
      // unselect all selected stations
      mResultsPanel.getJList().clearSelection();
    }
    else if (cmd.equals("all")) {
      checkAll.setSelected(false);
      // select all stations in list
      mResultsPanel.getJList().setSelectionInterval(0, mResultsPanel.getTotalNumDBs() - 1);
    }
  }

  public void drop() {
    // get the selection

    // iterate through

    // drop database


  }

  public void dialogDismissed(JDialog d) {
    this.init();
  }

  // Cancel button
  public void dialogCancelled(JDialog d) {
    this.dispose();
    this.setVisible(false);
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
    // test for any changes in the conditions
    if (mResultsPanel.getJList() != null && mResultsPanel.getJList().getSelectedIndex() >= 0) {
      mMoreInfoButton.setEnabled(true);
      mDropBtn.setEnabled(true);
    }
    else {
      mMoreInfoButton.setEnabled(false);
      mDropBtn.setEnabled(false);
    }
  }

  private class ResultsPanel extends JPanel {
    protected JList mResults = null;
    private Vector<String> mContents = new Vector<String>();

    public ResultsPanel() {
      mResults = new JList();
      mResults.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      mResults.setPrototypeCellValue("Atlantic 11 S 210 (11.25 S 180.45 W, 4/1/1999");
      mResults.setVisibleRowCount(12);
      JScrollPane listScroller = new JScrollPane(mResults);
      this.add(new TenPixelBorder(listScroller, 0, 5, 0, 0), "Center");
      this.setContents();
    }

    public Object[] getSelection() {
      return mResults.getSelectedValues();
    }

    public JList getJList() {
      return mResults;
    }

    public void reBuildList() {
      mResults.removeAll();
      mContents.clear();
      setContents();
      this.invalidate();
    }

    public void setContents() {
      try {
        Connection c = DatabaseTools.createConnection("mysql");
        Statement s = (Statement)c.createStatement();

        s.execute("show databases;");
        ResultSet rs = (ResultSet)s.getResultSet();
        rs.next();
        do {
          String st = rs.getString(1);
          mContents.add(st);
        }
        while (rs.next()); DatabaseTools.closeConnection(c);
        mResults.setListData(mContents);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    public int getTotalNumDBs() {
      return mContents.size();
    }
  }
}

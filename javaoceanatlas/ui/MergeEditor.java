package javaoceanatlas.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javaoceanatlas.utility.TenPixelBorder;
import javaoceanatlas.utility.ButtonMaintainer;
import javaoceanatlas.ui.widgets.MergeEditorCellRenderer;
import javaoceanatlas.ui.widgets.SearchField;
import javaoceanatlas.utility.Searchable;
import java.io.File;
import javaoceanatlas.ui.widgets.MergeEditorCellEditor;
import javaoceanatlas.utility.DialogClient;

@SuppressWarnings("serial")
public class MergeEditor extends JPanel implements ButtonMaintainer, DocumentListener, Searchable, DialogClient {
  private Timer timer = new Timer();
  private JTable mValsTable;
  private MergeEditorTableModel mMergeModel;
  protected DialogClient mThis = null;
  private String[] mCols;
  private int[] mDispPrec;
  private String mSrcText;
  private SearchField mSearchFld;
  private File mFile;
  private int mCurrCol;

  public MergeEditor() {
  }

  public void setUp(String[] cols, String srcText, File inFile) {
    mCols = cols;
    mSrcText = srcText;
    mFile = inFile;
    init();
    mThis = this;
  }

  public void init() {
    ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
    this.setLayout(new BorderLayout(5, 5));

    JPanel northPanel = new JPanel(new GridLayout(1, 1, 5, 5));
    northPanel.add(new JLabel(""));

    JPanel searchHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    mSearchFld = new SearchField(this);
    searchHolder.add(mSearchFld);
    northPanel.add(searchHolder);
    this.add(BorderLayout.NORTH, new TenPixelBorder(northPanel, 0, 0, 0, 10));

    // create the JTable for the middle panel
    int[] tempDisplPrec = new int[mCols.length];
    for (int i=0; i<mCols.length; i++) {
    	tempDisplPrec[i] = 4;
    }
    mMergeModel = new MergeEditorTableModel(mCols, mSrcText, tempDisplPrec);

    mValsTable = new JTable(mMergeModel);

		JTableHeader header = mValsTable.getTableHeader();

		final TableCellRenderer headerRenderer = header.getDefaultRenderer();
		final Font headerFont  = new Font("Helvetica", Font.BOLD, 10);

    header.setDefaultRenderer( new TableCellRenderer() {
			public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
				Component comp = headerRenderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
					comp.setFont(headerFont);
				return comp;
			}
		});
    
    mValsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mValsTable.setRowHeight(15);
    mValsTable.setColumnSelectionAllowed(true);
    mValsTable.setRowSelectionAllowed(false);
    header.addMouseListener(new ColumnHeaderListener());

    int numCols = mValsTable.getColumnCount();
    TableColumn tc;
    for (int i = 0; i < numCols; i++) {
      tc = mValsTable.getColumnModel().getColumn(i);
      tc.setMinWidth(10);
      tc.setCellRenderer(new MergeEditorCellRenderer(true));
      tc.setResizable(true);
    }
    mValsTable.setGridColor(new Color(100, 100, 100));
    mValsTable.setShowVerticalLines(true);
    mValsTable.setCellSelectionEnabled(true);
    //mValsTable.setSelectionMode(null);//ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    JScrollPane scroller = new JScrollPane();
    mValsTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
    scroller.getViewport().add(mValsTable);
    this.add("Center", new TenPixelBorder(scroller, 15, 15, 15, 15));
  }
  
	private void refreshDisplay(File inFile) throws Exception {
//		LineNumberReader in = null;
//		StringBuffer textPreview = new StringBuffer();
//
//		int count = 1;
//		DecimalFormat lineNum = new DecimalFormat();
//
//		try {
//			in = new LineNumberReader(new FileReader(inFile), 10000);
//
//			String inLine;
//
//			// skip header lines
//			for (int i = 0; i < mNumSkip; i++) {
//				inLine = in.readLine();
//				count++;
//			}
//
//			while (true) {
//				inLine = in.readLine();
//
//				if (inLine == null) {
//					break;
//				}
//
//				if (inLine.length() == 0) {
//					count++;
//					continue;
//				}
//
//				inLine = inLine.trim();
//
//				// while (inLine.indexOf(" ") > 0) {
//				// inLine = inLine.replace(" ", " ");
//				// }
//
//				// test for a comment lines
//				boolean skip = false;
//				for (int i = 0; i < mCommentTokens.length; i++) {
//					if (inLine.toUpperCase().startsWith(mCommentTokens[i].toUpperCase())) {
//						skip = true;
//						break;
//					}
//				}
//				if (skip) {
//					count++;
//					continue;
//				}
//
//				if (mHeaderLineNumber >= 0 && count == mHeaderLineNumber) {
//					textPreview.append("header " + lineNum.format(count) + ": ");
//				}
//				else if (mUnitsLineNumber >= 0 && count == mUnitsLineNumber) {
//					textPreview.append("units  " + lineNum.format(count) + ": ");
//				}
//				else {
//					textPreview.append("       " + lineNum.format(count) + ": ");
//				}
//
//				String defaultSplitter = "[,\t\\s+]";
//				if (mTabCommaDelim.isSelected()) {
//					defaultSplitter = "[,\t]";
//				}
//				else if (mWhiteSpaceDelim.isSelected()) {
//					defaultSplitter = "[\\s+]";
//				}
//				else if (mCustomDelim.isSelected()) {
//					String valStr = mCustomDelimFld.getText();
//					if (valStr.length() == 0) {
//						JFrame frm = new JFrame("Custom Delimiter Error");
//						Toolkit.getDefaultToolkit().beep();
//						JOptionPane.showMessageDialog(frm, "Custom delimiter has not been defined--using automatic.");
//						return;
//					}
//					defaultSplitter = "[" + valStr + "]";
//				}
//				// break the line into tokens
//				String[] tokens = inLine.split(defaultSplitter);
//				for (int t = 0; t < tokens.length; t++) {
//					textPreview.append(tokens[t] + "|");
//				}
//				textPreview.append("\n");
//				count++;
//			}
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//			textPreview.append("Can't display preview!");
//		}
//		finally {
//			in.close();
//		}
//		mTextArea.append(new String(textPreview));
//		mTextArea.setCaretPosition(0);
//		// updateMergePanel();
	}


  public String getFileName() {
    return mFile.getName();
  }
  
  public void closeMe() {
  	timer.cancel();
  }

	public void setDisplayPrecision(int[] dp) {
		mDispPrec = dp;
	} 
  
  public void updateTableModel(String[] cols, String newText) {
    // change the station model to the new stnvals
    mMergeModel = new MergeEditorTableModel(cols, newText, mDispPrec);
    mCols = cols;
    mValsTable.setModel(mMergeModel);

    int numCols = mMergeModel.getColumnCount();
    TableColumn tc;
    for (int i = 0; i < numCols; i++) {
      tc = mValsTable.getColumnModel().getColumn(i);
      tc.setMinWidth(10);
      tc.setCellRenderer(new MergeEditorCellRenderer(true));
      tc.setCellEditor(new MergeEditorCellEditor(new JTextField()));
      tc.setResizable(true);
    }
  }

  public void updateTableModel2(String[] cols, Vector<String>[] vals) {
    // change the station model to the new stnvals
    mMergeModel = new MergeEditorTableModel(cols, vals, mDispPrec);
    mCols = cols;
    mValsTable.setModel(mMergeModel);

    int numCols = mMergeModel.getColumnCount();
    TableColumn tc;
    for (int i = 0; i < numCols; i++) {
      tc = mValsTable.getColumnModel().getColumn(i);
      tc.setMinWidth(10);
      tc.setCellRenderer(new MergeEditorCellRenderer(true));
      tc.setCellEditor(new MergeEditorCellEditor(new JTextField()));
      tc.setResizable(true);
    }
  }

  public void maintainButtons() {
  }

  public RubberbandPanel getPanel() {
    return null;
  }

  @SuppressWarnings("unused")
  private boolean isTableChanged() {
    return false;
  }
  
  public int[] getDisplayPrecision() {
  	return mDispPrec;
  }

  public void changedUpdate(DocumentEvent evt) {
  }

  public void insertUpdate(DocumentEvent evt) {
  }

  public void removeUpdate(DocumentEvent evt) {
  }

  private class ColumnHeaderListener extends MouseAdapter {
    public void mouseClicked(MouseEvent evt) {
      JTable table = ((JTableHeader)evt.getSource()).getTable();
      TableColumnModel colModel = table.getColumnModel();

      // The index of the column whose header was clicked
      int vColIndex = colModel.getColumnIndexAtX(evt.getX());
      mCurrCol = vColIndex;

      // Return if not clicked on any column header
      if (vColIndex == -1) {
        return;
      }

      table.changeSelection(0, vColIndex, false, false);
      table.changeSelection(mMergeModel.getRowCount(), vColIndex, false, true);
    }
  }

  public Vector<String> getColHeaders() {
    // return a list of parameter names from the column headers
    Vector<String> retVector = new Vector<String>();
    for (int i = 0; i < mCols.length; i++) {
      retVector.addElement(mCols[i]);
    }
    return retVector;
  }

  public int getNumSourceLines() {
    return mValsTable.getRowCount();
  }

  public int getNumColumns() {
    return mValsTable.getColumnCount();
  }

  Vector<String> getRow(int r) {
    Vector<String> row = new Vector<String>();
    int cc = mValsTable.getColumnCount();
    for (int c = 0; c < cc; c++) {
      String cell = (mValsTable.getValueAt(r, c)).toString();
      row.addElement(cell);
    }
    return row;
  }

  public String getSourceText() {
    StringBuffer sb = new StringBuffer();

    int cc = mValsTable.getColumnCount();
    int rc = mValsTable.getRowCount();
    for (int r = 0; r < rc; r++) {
      for (int c = 0; c < cc; c++) {
        String cell = (mValsTable.getValueAt(r, c)).toString();
        sb.append(cell);
        sb.append("|");
      }
      sb.append("\r");
    }
    return new String(sb);
  }

  public void startSearch(String srcString) {
    // search the table model and hilight matching cells
    //unselectAll();
    currRow = 0;
    currCol = 0;

    int rc = mValsTable.getRowCount();
    int cc = mValsTable.getColumnCount();
    boolean found = false;
    for (int r = 0; r < rc; r++) {
      for (int c = 0; c < cc && !found; c++) {
        String cell = (mValsTable.getValueAt(r, c)).toString();
        if (cell.indexOf(srcString) == 0) {
          currRow = r;
          currCol = c + 1;
          mValsTable.changeSelection(r, c, false, false);
          found = true;
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void unselectAll() {
    int rc = mValsTable.getRowCount();
    int cc = mValsTable.getColumnCount();
    for (int r = 0; r < rc; r++) {
      for (int c = 0; c < cc; c++) {
        mValsTable.changeSelection(r, c, false, false);
      }
    }
  }

  public void searchAgain(String srcString) {
    int rc = mValsTable.getRowCount();
    int cc = mValsTable.getColumnCount();
    boolean found = false;

    // read to end of current row
    for (int c = currCol; c < cc && !found; c++) {
      String cell = (mValsTable.getValueAt(currRow, c)).toString();
      if (cell.indexOf(srcString) == 0) {
        currCol = c + 1;
        mValsTable.changeSelection(currRow, c, false, false);
        found = true;
      }
    }

    if (!found) {
      for (int r = currRow + 1; r < rc; r++) {
        for (int c = 0; c < cc && !found; c++) {
          String cell = (mValsTable.getValueAt(r, c)).toString();
          if (cell.indexOf(srcString) == 0) {
            currRow = r;
            currCol = c + 1;
            mValsTable.changeSelection(r, c, false, false);
            found = true;
          }
        }
      }
    }

    if (!found) {
      currRow = 0;
      currCol = 0;
    }
  }

  public void endSearch() {

  }

  @SuppressWarnings("unused")
  private void editExistinVariable(int col) {

  }

  private static int currRow = 0;
  private static int currCol = 0;

  /*  private class Searcher implements ConfigNewColumn {
      Searchable mMaintainer;
      long mDelay;

      public Searcher(Searchable maintainer, long delay) {
        mMaintainer = maintainer;
        mDelay = delay;
      }

      public void run() {
        for (; ; ) {
          mMaintainer.startSearch();
          try {
            Thread.sleep(mDelay);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }*/
  // OK Button
  @SuppressWarnings("unchecked")
  public void dialogDismissed(JDialog d) {
    if (d instanceof ConfigNewColumn) {
      String param = ((ConfigNewColumn)d).getParam();
      String units = ((ConfigNewColumn)d).getUnits();
      String fillVal = ((ConfigNewColumn)d).getFill();
      int prec = ((ConfigNewColumn)d).getPrecision();

      // get the current column headers
      Vector<String> cols = new Vector<String>(this.getColHeaders());

      // add the new column header
      if (units != null && units.length() > 0) {
        cols.add(mCurrCol, param + ":" + units);
      }
      else {
        cols.add(mCurrCol, param);
      }
      String[] newCols = new String[cols.size()];

      for (int i = 0; i < cols.size(); i++) {
        newCols[i] = cols.elementAt(i);
      }

      // now create a vector array
      int rc = mValsTable.getRowCount();
      Vector<String>[] vals = new Vector[rc];

      for (int r = 0; r < rc; r++) {
        Vector<String> row = this.getRow(r);
        row.add(mCurrCol, fillVal);
        vals[r] = row;
      }

      this.updateTableModel2(newCols, vals);
    }
    else if (d instanceof ConfigMergeRecode) {
      // get the modified text
      String newText = ((ConfigMergeRecode)d).getRecodedText();
      updateTableModel(mCols, newText);
    }
    else if (d instanceof EditMergeColumn) {
      // get the modified text
      String param = ((EditMergeColumn)d).getParam();
      String units = ((EditMergeColumn)d).getUnits();
      int prec = ((EditMergeColumn)d).getPrecision();
      mDispPrec[mCurrCol] = prec;

      TableColumn tc = mValsTable.getColumnModel().getColumn(mCurrCol);
      String newHeader = param;
      if (units != null && units.length() > 0) {
        newHeader += ":" + units;
      }
      tc.setHeaderValue(newHeader);
      //mMergeModel.fireTableDataChanged();
      updateTableModel(mCols, getSourceText());
      
      //foobar have to update mCols
      this.repaint();
    }
  }

  // Cancel button
  public void dialogCancelled(JDialog d) {}

  // something other than the OK button
  //public void dialogDismissedTwo(Frame d);

  // Apply button, OK w/o dismissing the dialog
  public void dialogApply(JDialog d) {}

  // Apply button, OK w/o dismissing the dialog
  public void dialogApplyTwo(Object d) {}

  // Apply button, OK w/o dismissing the dialog
  public void dialogDismissedTwo(JDialog d) {}
}

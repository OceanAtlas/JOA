
/*
 * $Id: FileSystemInspector,v 1.4 2005/06/21 17:25:52 oz Exp $
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * Provides a <code>JTree</code> view of all JOA Files in a collection of paths.
 *
 * @author John Osborne from code by Donald Denbo
 * @version $Revision: 1.4 $, $Date: 2005/06/21 17:25:52 $
 */

@SuppressWarnings("serial")
public class FileSystemInspector extends javax.swing.JPanel {
  private JTree treeView;
  private DefaultMutableTreeNode rootNode;
  BorderLayout borderLayout1 = new BorderLayout(0, 0);
  FlowLayout flowLayout1 = new FlowLayout(FlowLayout.CENTER, 5, 5);
  DefaultMutableTreeNode variableNode;
  javax.swing.JScrollPane treeScrollPane = new javax.swing.JScrollPane();
  // Used for addNotify check.
  boolean fComponentsAdjusted = false;
  JTree tree;
  DefaultTreeModel tm;
  DefaultMutableTreeNode parentNode = null;
  TreePath parentPath = null;
  ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
  int selRow;
  TreePath selPath;
  Rectangle selRect;
  boolean ignore = false;
  FileViewer mFileViewer;
  Vector<String> mAllPaths = null;
  int mCount;
  protected Component mComp;
  private boolean mRecursive = true;

  @SuppressWarnings("unchecked")
  public FileSystemInspector(Vector<String> paths, String title, int c, Component comp, boolean recursive) {
  	mAllPaths = paths;
    mCount = c;
    mComp = comp;
    mRecursive = recursive;

    tree = makeTree();
    treeScrollPane.setViewportView(tree);
    //tree.setFont(new Font("dialog", Font.PLAIN, 11));

    this.setLayout(new BorderLayout(5, 5));
    JOAJLabel l1 = new JOAJLabel(title, JOAJLabel.LEFT);
    this.add(l1, "North");

    this.add(treeScrollPane, "Center");

    treeView.addTreeSelectionListener((TreeSelectionListener)comp);

    int row = 0;
    while (row < treeView.getRowCount()) {
      if (treeView.isCollapsed(row)) {
        treeView.expandRow(row);
      }
      row++;
    }
  }

  public JTree makeTree() {
    tm = getTreeModel();
    treeView = new JTree(tm);
    treeView.setRootVisible(false);
    treeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    treeView.setVisibleRowCount(mCount);
    treeView.setShowsRootHandles(true);
    MouseListener ml = new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        int selRow = treeView.getRowForLocation(me.getX(), me.getY());
        TreePath selPath = treeView.getPathForLocation(me.getX(), me.getY());
        if (selRow != -1) {
          singleClick(selRow, selPath);
        }

        if (me.isAltDown()) {
          expandTree();
        }
        else if (me.isShiftDown()) {
          collapseTree();
        }
      }
    };
    treeView.addMouseListener(ml);
    return treeView;
  }

  void singleClick(int selRow, TreePath selPath) {
    //if(Debug.DEBUG) System.out.println("row " + selRow + " selected");
    Object[] objs = selPath.getPath();
    Object thing = ((DefaultMutableTreeNode)objs[objs.length - 1]).getUserObject();
    unselectAll();
    if (thing instanceof VariableNode) {
      VariableNode vn = ((VariableNode)thing);
      vn.setSelected(true);
    }
  }

  public DefaultTreeModel getTreeModel() {
    rootNode = new DefaultMutableTreeNode("All Datasets");
   getPathsNodes();
    return new DefaultTreeModel(rootNode);
  }

  public void getPathsNodes() {
    String[] extensions = {"joa", "poa", "csv", "jos", "nc", "cdf", "ctd"};
    // first iterate through the paths
    for (String path : mAllPaths) {
    	File root = new File("/Users/oz/Desktop");
      DefaultMutableTreeNode pathNode = new DefaultMutableTreeNode(path); 

      try {
      	//ct1.csv
      	Collection<File> files = FileUtils.listFiles(root, extensions, mRecursive);

      	for (File file : files) {
      		if (file.getName().indexOf("_hy1.csv") > 0) { 
      			System.out.println("File = " + file.getAbsolutePath());
      		}
      	}
      } catch (Exception e) {
      	e.printStackTrace();
      }
    }

      // second: iterate through the variable nodes
//      for (int j = 0; j < mVars[i].size(); j++) {
//        JOAVariable var = (JOAVariable)mVars[i].elementAt(j);
//        DefaultMutableTreeNode n = getVariableNode(var);
//        if (j == 0) {
//          VariableNode vn = (VariableNode)(n.getUserObject());
//          vn.setSelected(true);
//        }
//        profileNode.add(n);
//      }
//      rootNode.add(profileNode);
//    }
  }

  public DefaultMutableTreeNode getVariableNode(JOAVariable var) {
    DefaultMutableTreeNode varRoot;
    varRoot = new DefaultMutableTreeNode(new VariableNode(var));
    return varRoot;
  }

  void expandTree() {
    int row = 0;
    while (row < treeView.getRowCount()) {
      if (treeView.isCollapsed(row)) {
        treeView.expandRow(row);
      }
      row++;
    }
  }

  void collapseTree() {
    DefaultTreeModel tm = (DefaultTreeModel)treeView.getModel();
    TreePath tp = new TreePath(tm.getPathToRoot((TreeNode)parentNode));
    treeView.collapsePath(tp);
  }

  private void unselectAll() {
    int fvCnt = rootNode.getChildCount();
    for (int i = 0; i < fvCnt; i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(i);
      int varCnt = node.getChildCount();
      for (int v = 0; v < varCnt; v++) {
        DefaultMutableTreeNode vnode = (DefaultMutableTreeNode)node.getChildAt(v);
        if (vnode.getUserObject() instanceof VariableNode) {
          VariableNode vn = (VariableNode)vnode.getUserObject();
          vn.setSelected(false);
        }
      }
    }
  }

  public JOAVariable getSelectedVariable() {
    // traverse the tree and get the profile variable nodes
    int fvCnt = rootNode.getChildCount();
    for (int i = 0; i < fvCnt; i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(i);
      int varCnt = node.getChildCount();
      for (int v = 0; v < varCnt; v++) {
        DefaultMutableTreeNode vnode = (DefaultMutableTreeNode)node.getChildAt(v);
        if (vnode.getUserObject() instanceof VariableNode) {
          VariableNode vn = (VariableNode)vnode.getUserObject();
          // only include the selected variables
          if (vn.isSelected()) {
            return (vn.getVar());
          }
        }
      }
    }
    return null;
  }
 }


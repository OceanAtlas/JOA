/*
 * $Id: VariableInspector.java,v 1.4 2005/06/21 17:25:52 oz Exp $
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

/**
 * Provides a <code>JTree</code> view of all open FileViewers.
 *
 * @author John Osborne from code by Donald Denbo
 * @version $Revision: 1.4 $, $Date: 2005/06/21 17:25:52 $
 */

@SuppressWarnings("serial")
public class VariableInspector extends javax.swing.JPanel {
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
  Vector<JOAViewer> mAllFVs = null;
  Vector<JOAVariable>[] mVars = null;
  int mCount;
  protected Component mComp;

  @SuppressWarnings("unchecked")
  public VariableInspector(FileViewer ifv, String title, int c, Component comp) {
    mFileViewer = ifv;
    mCount = c;
    mComp = comp;

    // get a list of the open FileViewers
    mAllFVs = mFileViewer.getOpenFileViewers();
    mVars = new Vector[mAllFVs.size()];

    for (int i = 0; i < mAllFVs.size(); i++) {
      FileViewer fv = (FileViewer)mAllFVs.elementAt(i);
      mVars[i] = new Vector();
      int numVars = fv.getNumProperties();
      for (int j = 0; j < numVars; j++) {
        JOAVariable joavar = new JOAVariable(fv, fv.mAllProperties[j].getVarLabel(), fv.mAllProperties[j].getUnits(), null);
        mVars[i].add(joavar);
      }
    }

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
    getFVNodes();
    return new DefaultTreeModel(rootNode);
  }

  public void getFVNodes() {
    // first iterate through the fileviewers
    for (int i = 0; i < mAllFVs.size(); i++) {
      FileViewer fv = (FileViewer)mAllFVs.elementAt(i);
      DefaultMutableTreeNode profileNode = new DefaultMutableTreeNode(fv.getTitle());

      // second: iterate through the variable nodes
      for (int j = 0; j < mVars[i].size(); j++) {
        JOAVariable var = (JOAVariable)mVars[i].elementAt(j);
        DefaultMutableTreeNode n = getVariableNode(var);
        if (j == 0) {
          VariableNode vn = (VariableNode)(n.getUserObject());
          vn.setSelected(true);
        }
        profileNode.add(n);
      }
      rootNode.add(profileNode);
    }
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

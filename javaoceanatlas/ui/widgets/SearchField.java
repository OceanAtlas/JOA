package javaoceanatlas.ui.widgets;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.Cursor;
import javax.swing.JLabel;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Dimension;
import java.awt.Color;
import javaoceanatlas.utility.RowLayout;
import javaoceanatlas.utility.Orientation;
import java.awt.Graphics;
import java.awt.geom.RoundRectangle2D;
import javaoceanatlas.utility.Searchable;
import javaoceanatlas.utility.BoxBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;
import java.util.Collections;

/**
 * <p>Title: SearchField</p>
 *
 * <p>Description: Aqua-like search field</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: NOAA/PMEL/NCTR</p>
 *
 * @author oz
 * @version 1.0
 */
@SuppressWarnings("serial")
public class SearchField extends JPanel implements DocumentListener {
  private ImageIcon mMagIcon;
  private ImageIcon mCancelIcon;
  private JTextField mSearchFld;
  private JLabel mCancel;
  private Searchable mSearcher;

  @SuppressWarnings("unchecked")
  public SearchField(Searchable srch) {
    mSearcher = srch;
    try {
      mMagIcon = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource("images/search_mag.gif"));
      mCancelIcon = new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/cancel_search.gif"));
    }
    catch (Exception ex) {}

    this.setBorder(new BoxBorder(false));
    this.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 3));
    this.setBackground(Color.white);
    mSearchFld = new JTextField(20);
    mSearchFld.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    mSearchFld.getDocument().addDocumentListener(this);
    mSearchFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    mSearchFld.setBorder(null);
    this.add(new JLabel(mMagIcon));
    this.add(mSearchFld);
    mCancel = new JLabel(mCancelIcon);
    this.add(mCancel);
    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        mSearchFld.setText("");
        mSearcher.endSearch();
        maintainCancel();
      }
    };
    mCancel.addMouseListener(mouseListener);
    mCancel.setVisible(false);

    mSearchFld.addKeyListener(new KeyListener());
  }

  public void changedUpdate(DocumentEvent evt) {
    maintainCancel();
    search();
  }

  public void insertUpdate(DocumentEvent evt) {
    maintainCancel();
    search();
  }

  public void removeUpdate(DocumentEvent evt) {
    maintainCancel();
    search();
  }

  private void maintainCancel() {
    if (mSearchFld.getText().length() > 0) {
      mCancel.setVisible(true);
    }
    else {
      mCancel.setVisible(false);
    }
  }

  private void search() {
    mSearcher.startSearch(mSearchFld.getText());
  }

  private void searchAgain() {
    mSearcher.searchAgain(mSearchFld.getText());
  }

  public Dimension getPreferredSize() {
    return new Dimension(300, 25);
  }

  public void paintComponent(Graphics g) {
    RoundRectangle2D.Double rect = new RoundRectangle2D.Double(0.0, 0.0, 300, 25, 10.0, 10.0);
    g.setClip(rect);
    super.paintComponent(g);
    g.setClip(0, 0, 10000, 10000);
  }

  public String getSearchString() {
    return mSearchFld.getText();
  }

  private class KeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent ev) {
      int key = ev.getKeyCode();
      if (key == KeyEvent.VK_TAB) {
        searchAgain();
        ev.consume();
      }
      else {
        super.keyPressed(ev);
      }
    }
  }
}

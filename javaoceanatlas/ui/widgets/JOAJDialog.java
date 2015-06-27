package javaoceanatlas.ui.widgets;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFrame;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author oz
 * @version 1.0
 */
@SuppressWarnings("serial")
public class JOAJDialog extends JDialog {
  public JOAJDialog() {
    super();
  }

  public JOAJDialog(JFrame owner, String title, boolean modal) {
    super(owner, title, modal);
  }

  protected void dialogInit() {
    super.dialogInit();
    //getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

    JLayeredPane layeredPane = getLayeredPane();
    layeredPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        "close-it");
    layeredPane.getActionMap().put("close-it", new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
  }
}

/*
 * $Id: ProgressBarDialog.java,v 1.2 2005/06/17 18:08:54 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javaoceanatlas.ui.widgets.*;

public class ProgressBarDialog extends Thread {
  private ProgressClient parent = null;
  private JProgressBar proBar = null;
  int value = 0;
  long mDelay = 100;
  int min;
  int mMax;
  JFrame jf;

  public ProgressBarDialog(Object par, String title, int min, int max) {
    mMax = max;
    this.parent = (ProgressClient)par;
    parent.setProgressValue(0);

    proBar = new JProgressBar(JProgressBar.HORIZONTAL, min, max);
    proBar.setStringPainted(true);

    jf = new JFrame();
    JPanel jp = new JPanel();
    jp.add("South", proBar);
    jf.getContentPane().add("Center", jp);
    JOAJButton jb = new JOAJButton("test");
    jf.getContentPane().add("South", jb);
    jf.pack();
    Rectangle dBounds = jf.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    jf.setLocation(x, y);
    jf.setVisible(true);
  }

  public void dispose() {
    this.dispose();
  }

  @SuppressWarnings("deprecation")
  public void startProgress() {
    if (isAlive()) {
      super.resume();
    }
    else {
      start();
    }
  }

  public void run() {
    for (; ; ) {
      value = parent.getProgressValue();
      proBar.setValue(value);
      System.out.println(value);

      try {
        Thread.sleep(mDelay);
      }
      catch (InterruptedException e) {
        ;
      }
      if (value >= mMax) {
        break;
      }
    }
    endProgress();
  }

  @SuppressWarnings("deprecation")
  public void endProgress() {
    if (isAlive()) {
      stop();
      jf.dispose();
    }
  }
}

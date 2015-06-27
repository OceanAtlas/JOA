/*
 * $Id: ParamValuePanel.java,v 1.3 2005/06/21 17:25:52 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import javaoceanatlas.ui.widgets.*;

@SuppressWarnings("serial")
public class ParamValuePanel extends JPanel {
  JOAJLabel mLabel = null;
  JOAJLabel mLabel2 = null;
  Color mColor = Color.black;
  int mFontSize = -99;
  int mFontStyle = Font.PLAIN;

  public ParamValuePanel() {
    this.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
  }

  public void init(String param, String val, Color fg, int fontSize, int style) {
    mFontSize = fontSize;
    mFontStyle = style;
    init(param, val, fg);
  }

  public void init(String param, String val, Color fg) {
    mLabel = new JOAJLabel(param);
    mLabel2 = new JOAJLabel(val, JOAJLabel.RIGHT);
    if (mFontSize == -99) {
      mFontSize = 12;
    }
    mLabel.setFont(new Font("Courier", mFontStyle, mFontSize));
    mLabel2.setFont(new Font("Courier", mFontStyle, mFontSize));
    mColor = fg;
    mLabel.setForeground(mColor);
    this.add(mLabel);
    this.add(mLabel2);
  }

  public void setFGColor(Color fg) {
    mColor = fg;
  }

  public void setStyle(int style) {
  	if (style != mFontStyle) {
    	mFontStyle = style;
  		mLabel2.setFont(new Font("Courier", mFontStyle, mFontSize));
      mLabel2.invalidate();
      mLabel.setFont(new Font("Courier", mFontStyle, mFontSize));
      mLabel.invalidate();
  	}
  }

  public void setNewValue(String newVal) {
    mLabel2.setText(newVal);
    mLabel2.setForeground(mColor);
    mLabel2.invalidate();
  }

  public void setNewLabel(String newLabel) {
    mLabel.setText(newLabel);
    mLabel.setForeground(mColor);
    mLabel.invalidate();
  }
}

/*
 * $Id: ConfigContourOptions.java,v 1.3 2005/09/07 18:49:30 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import gov.noaa.pmel.swing.*;

@SuppressWarnings("serial")
public class ConfigContourOptions extends JOAJDialog implements ActionListener, ItemListener {
  protected FileViewer mFileViewer = null;
  protected ContourPlotSpecification mPlotSpec = null;
  protected ContourPlotSpecification mOriginalPlotSpec = null;
  protected DialogClient mClient = null;
  protected JOAJCheckBox mColorLines = null;
  protected Swatch mPlotBg = null;
  protected JOAJComboBox mPresetColorSchemes = null;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mApplyButton = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mNumSkipField = null;
  protected JOAJTextField mNumLabelField = null;
	protected JSpinner mLabelPrecSpinner = null;

  public ConfigContourOptions(JFrame par, FileViewer fv, DialogClient client, ContourPlotSpecification plotSpec) {
    super(par, "Configure Contour Options", false);
    mFileViewer = fv;
    mClient = client;
    mPlotSpec = plotSpec;
    mOriginalPlotSpec = new ContourPlotSpecification(mPlotSpec);
    init();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 5));
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

    // color lines
    mColorLines = new JOAJCheckBox(b.getString("kColorLines"), mPlotSpec.isColorLines());
    mainPanel.add(mColorLines);

    // custom contour stuff
    JPanel line3a = new JPanel();
    line3a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line3a.add(new JOAJLabel(b.getString("kPlotEvery")));
    mNumSkipField = new JOAJTextField(2);
    mNumSkipField.setText(String.valueOf(mPlotSpec.getPlotEveryNthContour()));
    mNumSkipField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3a.add(mNumSkipField);
    line3a.add(new JOAJLabel(b.getString("kContours")));
    //mainPanel.add(line3a);
    //JPanel line4a = new JPanel();
    //line4a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    //line3a.add(new JOAJLabel("   " + b.getString("kLabelEvery")));
    //mNumLabelField = new JOAJTextField(String.valueOf(mPlotSpec.mLabelEveryNthContour),2);
    //line3a.add(mNumLabelField);
    //line3a.add(new JOAJLabel(b.getString("kcontours")));
    mainPanel.add(line3a);

    JPanel line7 = new JPanel();
    line7.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
    line7.add(new JOAJLabel(b.getString("kBackgroundColor")));
    mPlotBg = new Swatch(mPlotSpec.getBGColor(), 12, 12);
    line7.add(new JOAJLabel(" "));
    line7.add(mPlotBg);

    //JPanel line9 = new JPanel();
    //line9.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    line7.add(new JOAJLabel("   " + b.getString("kColorSchemes")));
    Vector<String> presetSchemes = new Vector<String>();
    presetSchemes.addElement(b.getString("kDefault"));
    presetSchemes.addElement(b.getString("kWhiteBackground"));
    presetSchemes.addElement(b.getString("kBlackBackground"));
    mPresetColorSchemes = new JOAJComboBox(presetSchemes);
    mPresetColorSchemes.setSelectedItem(b.getString("kDefault"));
    mPresetColorSchemes.addItemListener(this);
    line7.add(mPresetColorSchemes);
    mainPanel.add(line7);
    //mainPanel.add(new TenPixelBorder(line9, 0, 10, 0, 0));
    
		JPanel linem = new JPanel();
		linem.add(new JOAJLabel("Label significant digits:"));
		SpinnerNumberModel model3 = new SpinnerNumberModel(mPlotSpec.getLabelPrecision(), 1, 100, 1);
		mLabelPrecSpinner = new JSpinner(model3);
		linem.add(mLabelPrecSpinner);
    mainPanel.add(linem);

    // lower panel
    mOKBtn = new JOAJButton(b.getString("kOK"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kCancel"));
    mCancelButton.setActionCommand("cancel");
    mApplyButton = new JOAJButton(b.getString("kApply"));
    mApplyButton.setActionCommand("apply");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    mOKBtn.addActionListener(this);
    mApplyButton.addActionListener(this);
    mCancelButton.addActionListener(this);
    contents.add(new TenPixelBorder(mainPanel, 10, 10, 10, 10), "Center");
    contents.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");

    this.pack();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      mClient.dialogCancelled(this);
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      mClient.dialogDismissed(this);
      this.dispose();
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJComboBox) {
      JOAJComboBox cb = (JOAJComboBox)evt.getSource();
      if (cb == mPresetColorSchemes) {
        int colorScheme = cb.getSelectedIndex();
        if (colorScheme == 0) {
          // default bg
          mPlotBg.setColor(JOAConstants.DEFAULT_CONTENTS_COLOR);
        }
        else if (colorScheme == 1) {
          // white bg
          mPlotBg.setColor(Color.white);
        }
        else {
          // color bg
          mPlotBg.setColor(Color.black);
        }
      }
    }
  }

  public ContourPlotSpecification getOrigPlotSpec() {
    return mOriginalPlotSpec;
  }

  public ContourPlotSpecification getPlotSpec() {
  	mPlotSpec.setLabelPrecision((Integer)mLabelPrecSpinner.getValue());
    mPlotSpec.setColorLines(mColorLines.isSelected());
    // get overlay contour stuff
    int plotEvery = 2;
    int labelEvery = 4;
    try {
      String fldText = mNumSkipField.getText();
      plotEvery = Integer.valueOf(fldText).intValue();
    }
    catch (Exception ex) {
    }

    try {
      String fldText = mNumLabelField.getText();
      labelEvery = Integer.valueOf(fldText).intValue();
    }
    catch (Exception ex) {
    }

    mPlotSpec.setPlotEveryNthContour(plotEvery);
    mPlotSpec.setLabelEveryNthContour(labelEvery);

    mPlotSpec.setBGColor(mPlotBg.getColor());
    return mPlotSpec;
  }

}

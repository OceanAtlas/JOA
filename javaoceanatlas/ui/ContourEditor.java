/*
 * $Id: ContourEditor.java,v 1.6 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import javax.swing.border.*;
import gov.noaa.pmel.swing.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ContourEditor extends JOAJDialog implements ActionListener, ButtonMaintainer, ItemListener {
  protected FileViewer mFileViewer = null;
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJButton mMakeDefaultButton = null;
  protected JOAJButton mExportButton = null;
  protected NewColorBar mColorBar = null;
  protected ColorPalette mCurrPalette = null;
  protected SimpleColorPalettePanel mColorPalette = null;
  protected JOAJTextField mStartField = null;
  protected JOAJTextField mEndField = null;
  protected JOAJTextField mNumContField = null;
  protected double[] mColorBarValues = null;
  protected Color[] mColorBarColors = null;
  protected ColorArrayEditor mArrayEditor = null;
  protected LargeIconButton mLinear = null;
  protected LargeIconButton mPowerUp = null;
  protected LargeIconButton mPowerDown = null;
  protected LargeIconButton mLogistic = null;
  protected JOAJRadioButton mAssignFromTable = null;
  protected JOAJRadioButton mBlendColors = null;
  protected JOAJRadioButton mAssignFromRainbow = null;
  protected JOAJRadioButton mAssignFromInvRainbow = null;
  protected JOAFocusableSwatch mBlendStartColor = null;
  protected JOAFocusableSwatch mBlendMiddleColor = null;
  protected JOAFocusableSwatch mBlendEndColor = null;
  protected JOAFocusableSwatch mStartColor = null;
  protected JOAJRadioButton mAcrossTable = null;
  protected JOAJRadioButton mDownTable = null;
  protected ColorBarPreview mColorBarPreview = null;
  protected JOAJTextField mParamField = null;
  protected JOAJTextField mTitleField = null;
  protected JOAJTextField mDescriptionField = null;
  protected JOAJTextField mNumSkipField = null;
  protected JOAJTextField mNumLabelField = null;
  protected Vector<String> mColorPaletteList = null;
	private Timer timer = new Timer();
  protected JOAJButton mAssignColorsOnly = null;
  protected JOAJRadioButton mUseMetadata = null;
  protected JOAJRadioButton mUseParameter = null;
  protected JOAJComboBox mMetadataChoices = null;
  protected JOAJComboBox mColorCombo = null;
  protected JOAJComboBox mParamCombo = null;
  protected JOAJLabel mEndValueLabel = null;
  protected JOAJLabel mStartValueLabel = null;
  protected boolean mUseMetadataFlag = false;
  protected String mMetadataType = null;

  public ContourEditor(JOAWindow par, FileViewer fv) {
    super(par, "Contour Manager", false);
    mFileViewer = fv;
    init();
  }

  public ContourEditor(JOAWindow par, FileViewer fv, NewColorBar cbar) {
    super(par, "Contour Editor", false);
    mFileViewer = fv;
    mColorBar = cbar;
    mColorBarValues = mColorBar.getValues();
    mColorBarColors = mColorBar.getColors();
    mUseMetadataFlag = mColorBar.isMetadataColorBar();
    init();
    fillInDialog();
  }

  public void init() {
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(5, 0));
    JPanel mainPanel = new JPanel(); // everything goes in here
    mainPanel.setLayout(new BorderLayout(5, 0));

    // upper panel
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new BorderLayout(5, 0)); // upperGridPanel

    JPanel upperGridPanel = new JPanel();
    upperGridPanel.setLayout(new GridLayout(2, 1, 5, 0)); // Description, contours, color assignment

    // Description
    JPanel descriptionPanel = new JPanel();
    descriptionPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kDescription"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    descriptionPanel.setBorder(tb);
    JPanel line1 = new JPanel();
    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line1.add(new JOAJLabel(b.getString("kTitle")));
    mTitleField = new JOAJTextField(15);
    mTitleField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mTitleField);
    line1.add(new JOAJLabel(b.getString("kDescription2")));
    mDescriptionField = new JOAJTextField(25);
    mDescriptionField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mDescriptionField);

    mUseParameter = new JOAJRadioButton(b.getString("kParameter3"), true);
    mUseMetadata = new JOAJRadioButton(b.getString("kStationMetadata"));
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mUseParameter);
    bg1.add(mUseMetadata);
    mUseParameter.addItemListener(this);
    mUseMetadata.addItemListener(this);

    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line2.add(new JOAJLabel(b.getString("kBaseColorbar")));
    line2.add(mUseParameter);
    mParamField = new JOAJTextField(4);
    mParamField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line2.add(mParamField);
    //add a pop up that shows the parameters
    if (mFileViewer != null) {
      Vector<String> mParamList = new Vector<String>();
      for (int i = 0; i < mFileViewer.gNumProperties; i++) {
        mParamList.addElement(mFileViewer.mAllProperties[i].getVarLabel());
      }
      mParamCombo = new JOAJComboBox(mParamList);
      mParamCombo.setSelectedItem((String)mParamList.elementAt(0));
      mParamCombo.addItemListener(this);
      line2.add(new JOAJLabel(" "));
      line2.add(mParamCombo);
    }
    line2.add(new JOAJLabel("  "));
    line2.add(mUseMetadata);
    Vector<String> presetMetadata = new Vector<String>();
    presetMetadata.addElement(b.getString("kDateTime"));
    presetMetadata.addElement(b.getString("kDateTimeMonth"));
    presetMetadata.addElement(b.getString("kLatitude"));
    presetMetadata.addElement(b.getString("kLongitude"));
    mMetadataChoices = new JOAJComboBox(presetMetadata);
    mMetadataChoices.addItemListener(this);
    line2.add(mMetadataChoices);
    descriptionPanel.add(line2);
    descriptionPanel.add(line1);

    // Contours
    //JPanel contourPanel = new JPanel();
    //contourPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));
    //tb = BorderFactory.createTitledBorder(b.getString("kContours"));
    //if (JOAConstants.ISMAC) {
    //	//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    //}
    //contourPanel.setBorder(tb);
    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line3.add(new JOAJLabel(b.getString("kNumContours")));
    mNumContField = new JOAJTextField(4);
    mNumContField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3.add(mNumContField);
    line3.add(new JOAJLabel("  "));
    mStartValueLabel = new JOAJLabel(b.getString("kStartValue"));
    line3.add(mStartValueLabel);
    mStartField = new JOAJTextField(6);
    mStartField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3.add(mStartField);
    line3.add(new JOAJLabel("  "));
    mEndValueLabel = new JOAJLabel(b.getString("kEndValue"));
    line3.add(mEndValueLabel);
    mEndField = new JOAJTextField(6);
    mEndField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3.add(mEndField);
    descriptionPanel.add(line3);

    // Color assignment
    JPanel colorAssignmentPanel = new JPanel();
    colorAssignmentPanel.setLayout(new RowLayout(Orientation.CENTER, Orientation.TOP, 0));
    mAssignFromTable = new JOAJRadioButton(b.getString("kAssignFromTable"));
    mBlendColors = new JOAJRadioButton(b.getString("kBlendColors"), true);
    mAssignFromRainbow = new JOAJRadioButton(b.getString("kAssignFromRainbow"));
    mAssignFromInvRainbow = new JOAJRadioButton(b.getString("kAssignFromInvRainbow"));
    ButtonGroup bg = new ButtonGroup();
    bg.add(mBlendColors);
    bg.add(mAssignFromTable);
    bg.add(mAssignFromRainbow);
    bg.add(mAssignFromInvRainbow);
    mBlendColors.addItemListener(this);
    mAssignFromTable.addItemListener(this);
    mAssignFromRainbow.addItemListener(this);
    mAssignFromInvRainbow.addItemListener(this);

    // Color Assignment: Blend
    JPanel blendColorsPanel = new JPanel();
    blendColorsPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    blendColorsPanel.add(mBlendColors);
    SwatchGroup mSwatchGroup = new SwatchGroup();
    JPanel line4 = new JPanel();
    line4.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
    line4.add(new JOAJLabel("     " + b.getString("kStartColor")));
    mBlendStartColor = new JOAFocusableSwatch(Color.blue, 12, 12, mSwatchGroup);
    line4.add(mBlendStartColor);
    line4.add(new JOAJLabel("  " + b.getString("kMiddleColor")));
    mBlendMiddleColor = new JOAFocusableSwatch(Color.white, 12, 12, mSwatchGroup);
    line4.add(mBlendMiddleColor);
    line4.add(new JOAJLabel("  " + b.getString("kEndColor")));
    mBlendEndColor = new JOAFocusableSwatch(Color.red, 12, 12, mSwatchGroup);
    line4.add(mBlendEndColor);
    blendColorsPanel.add(line4);

    // Color Assignment: Assign from table
    JPanel assignColorsPanel = new JPanel();
    assignColorsPanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 0));
    assignColorsPanel.add(mAssignFromTable);
    JPanel line5 = new JPanel();
    line5.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line5.add(new JOAJLabel("     " + b.getString("kStartColor")));
    mStartColor = new JOAFocusableSwatch(Color.white, 12, 12, mSwatchGroup);
    mStartColor.setEditable(false);
    line5.add(mStartColor);
    mAcrossTable = new JOAJRadioButton(b.getString("kAcrossTable"), true);
    mDownTable = new JOAJRadioButton(b.getString("kDownTable"));
    disableTableOptions();
    ButtonGroup bg2 = new ButtonGroup();
    bg2.add(mAcrossTable);
    bg2.add(mDownTable);
    line5.add(new JOAJLabel("  "));
    line5.add(mAcrossTable);
    line5.add(mDownTable);
    assignColorsPanel.add(line5);

    // Color Assignment: Assign from rainbows
    JPanel assignRainbowPanel = new JPanel();
    assignRainbowPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    assignRainbowPanel.add(mAssignFromRainbow);
    assignRainbowPanel.add(mAssignFromInvRainbow);

    // build colorAssignmentPanel
    colorAssignmentPanel.add(blendColorsPanel);
    colorAssignmentPanel.add(assignColorsPanel);

    JPanel colorHolder = new JPanel();
    colorHolder.setLayout(new BorderLayout(5, 0));
    JPanel colorAssignmentPanelCont = new JPanel();
    colorAssignmentPanelCont.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    colorAssignmentPanelCont.add(colorAssignmentPanel);
    colorHolder.add("North", colorAssignmentPanelCont);
    colorHolder.add("South", assignRainbowPanel);
    tb = BorderFactory.createTitledBorder(b.getString("kColorAssignment"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    colorHolder.setBorder(tb);

    // create buttons
    JPanel line6 = new JPanel();
    line6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    line6.add(new JOAJLabel(b.getString("kCreateWithShape")));
    JPanel shapePanel = new JPanel();
    shapePanel.setLayout(new GridLayout(1, 4, 5, 0));
    mLinear = new LargeIconButton(new ImageIcon(getClass().getResource("images/linear.gif")));
    shapePanel.add(mLinear);
    mPowerUp = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerup.gif")));
    shapePanel.add(mPowerUp);
    mPowerDown = new LargeIconButton(new ImageIcon(getClass().getResource("images/powerdown.gif")));
    shapePanel.add(mPowerDown);
    mLogistic = new LargeIconButton(new ImageIcon(getClass().getResource("images/logistic.gif")));
    shapePanel.add(mLogistic);
    mLinear.addActionListener(this);
    mPowerUp.addActionListener(this);
    mPowerDown.addActionListener(this);
    mLogistic.addActionListener(this);
    mLinear.setActionCommand("linear");
    mPowerUp.setActionCommand("powerUp");
    mPowerDown.setActionCommand("powerDown");
    mLogistic.setActionCommand("logistic");
    mAssignColorsOnly = new JOAJButton(b.getString("kAssignColorsOnly"));
    mAssignColorsOnly.setActionCommand("assignColors");
    mAssignColorsOnly.addActionListener(this);
    line6.add(shapePanel);
    line6.add(mAssignColorsOnly);
		
		mLinear.setToolTipText(b.getString("kLinearTip"));
		mPowerUp.setToolTipText(b.getString("kIncreasingExpTip"));
		mPowerDown.setToolTipText(b.getString("kDecreasingExpTip"));
		mLogistic.setToolTipText(b.getString("kReverseSTip"));

    // build the top
    upperGridPanel.add(descriptionPanel);
    //upperGridPanel.add(contourPanel);
    upperGridPanel.add(colorHolder);
    upperPanel.add("North", upperGridPanel);
    upperPanel.add("Center", line6);

    // lower Panel
    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(new RowLayout(Orientation.CENTER, Orientation.TOP, 0));

    // read the color tables and get first palette
    mColorPaletteList = JOAFormulas.getColorPalList();
    mCurrPalette = getColorPalette((String)mColorPaletteList.elementAt(0));

    // build the initial color table
    mColorPalette = new SimpleColorPalettePanel(mCurrPalette);

    // Value Editor and Preview
    JPanel valEdPreviewPanel = new JPanel();
    valEdPreviewPanel.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kValueEditorPreview"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    valEdPreviewPanel.setBorder(tb);
    mArrayEditor = new ColorArrayEditor(this.getRootPane(), false);
    if (mColorBar != null && mFileViewer != null && !mUseMetadataFlag) {
      mArrayEditor.setHistogram(mColorBar.getParam(), mFileViewer, mColorBar);
    }
    if (mColorBar == null) {
      mColorBarPreview = new ColorBarPreview(mSwatchGroup, mColorPalette);
    }
    else {
      mColorBarPreview = new ColorBarPreview(mSwatchGroup, mColorPalette, mColorBar.getColors());
    }
    valEdPreviewPanel.add(mArrayEditor);
    valEdPreviewPanel.add(mColorBarPreview);
    lowerPanel.add(valEdPreviewPanel);

    // color palette
    JPanel colorPalettePanel = new JPanel();
    colorPalettePanel.setLayout(new BorderLayout(5, 0)); // color table
    JPanel colorPalCont = new JPanel();
    colorPalCont.setLayout(new BorderLayout(5, 10));
    tb = BorderFactory.createTitledBorder(b.getString("kColorPalettes"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    colorPalCont.setBorder(tb);

    // build the popup
    mColorCombo = new JOAJComboBox(mColorPaletteList);
    mColorCombo.setSelectedItem((String)mColorPaletteList.elementAt(0));
    mColorCombo.addItemListener(this);
    colorPalCont.add("North", mColorCombo);
    colorPalCont.add("Center", mColorPalette);
    colorPalettePanel.add("North", colorPalCont);

    // init the swatch for table color assignment
    mStartColor.setColor(mCurrPalette.getColor(0));

    // finish building the lower panel
    lowerPanel.add(colorPalCont);

    // button panel
    mOKBtn = new JOAJButton(b.getString("kSave"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    mCancelButton = new JOAJButton(b.getString("kClose"));
    mCancelButton.setActionCommand("cancel");
    mMakeDefaultButton = new JOAJButton(b.getString("kMakeDefault"));
    mMakeDefaultButton.setActionCommand("default");

    mExportButton = new JOAJButton("Export...");
    mExportButton.setActionCommand("export");
     
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kFile");	
  		if (fg.hasFeature("kExportJSON") && fg.isFeatureEnabled("kExportJSON")) {
  			 dlgBtnsPanel.add(mExportButton);
  		}
      dlgBtnsPanel.add(mMakeDefaultButton);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mMakeDefaultButton);
      FeatureGroup fg = JOAConstants.JOA_FEATURESET.get("kFile");	
  		if (fg.hasFeature("kExportJSON") && fg.isFeatureEnabled("kExportJSON")) {
  			 dlgBtnsPanel.add(mExportButton);
  		}
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    mColorPalette.addColorSelChangedListener(mStartColor);
    mColorPalette.addColorSelChangedListener(mBlendStartColor);
    mColorPalette.addColorSelChangedListener(mBlendMiddleColor);
    mColorPalette.addColorSelChangedListener(mBlendEndColor);
    mOKBtn.addActionListener(this);
    mCancelButton.addActionListener(this);
    mMakeDefaultButton.addActionListener(this);
    mExportButton.addActionListener(this);

    // add all the sub panels to main panel
    mainPanel.add(new TenPixelBorder(upperPanel, 5, 5, 5, 5), "North");
    mainPanel.add(new TenPixelBorder(lowerPanel, 5, 5, 5, 5), "Center");
    mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
    contents.add("Center", mainPanel);
    this.pack();
    enableParameterOptions();

    runTimer();

    // show dialog at center of screen
    Rectangle dBounds = this.getBounds();
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    int x = sd.width / 2 - dBounds.width / 2;
    int y = sd.height / 2 - dBounds.height / 2;
    this.setLocation(x, y);
  }

	public void runTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				maintainButtons();
			}
		};
		timer.schedule(task, 0, 1000);
	}

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals("cancel")) {
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      save();
    }
    else if (cmd.equals("default")) {
      makeDefault();
    }
    else if (cmd.equals("export")) {
      export();
    }
    else if (cmd.equals("linear")) {
      createColorBar(JOAConstants.LINEAR);
      mLinear.setSelected(false);
    }
    else if (cmd.equals("powerUp")) {
      createColorBar(JOAConstants.EXPONENTIALUP);
      mPowerUp.setSelected(false);
    }
    else if (cmd.equals("powerDown")) {
      createColorBar(JOAConstants.EXPONENTIALDOWN);
      mPowerDown.setSelected(false);
    }
    else if (cmd.equals("logistic")) {
      createColorBar(JOAConstants.LOGISTIC);
      mLogistic.setSelected(false);
    }
    else if (cmd.equals("assignColors")) {
      createColorBar(JOAConstants.ASSIGNJUSTCOLORS);
    }
  }

  public void createColorBar(int curveShape) {
    double numLevels = 0;
    try {
      // get Number of levels
      String fldText = mNumContField.getText();
      numLevels = Double.valueOf(fldText).doubleValue();
    }
    catch (Exception ex) {
      System.out.println("Invalid Colorbar specification");
      return;
    }

    if (curveShape != JOAConstants.ASSIGNJUSTCOLORS) {
      // get base and end levels
      double baseLevel = 0;
      double endLevel = 0;

      if (!mUseMetadataFlag) {
        try {
          String fldText = mStartField.getText();
          baseLevel = Double.valueOf(fldText).doubleValue();
          fldText = mEndField.getText();
          endLevel = Double.valueOf(fldText).doubleValue();

          // get Number of levels
          fldText = mNumContField.getText();
          numLevels = Double.valueOf(fldText).doubleValue();
        }
        catch (Exception ex) {
          System.out.println("Invalid Colorbar specification");
          return;
        }
      }
      else {
        baseLevel = 0.0;
        endLevel = 1.0;
      }

      // compute new color bar values
      mColorBarValues = null;
      mColorBarValues = new double[(int)numLevels];
      mColorBarColors = new Color[(int)numLevels];

      if (curveShape == JOAConstants.LINEAR) {
        double increment = (endLevel - baseLevel) / (numLevels - 1);
        for (int i = 0; i < (int)numLevels; i++) {
          mColorBarValues[i] = baseLevel + (i * increment);
        }
      }
      else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
        double shape = JOAFormulas.getShape(baseLevel, endLevel);
        double scaledMax = Math.abs(endLevel - baseLevel);
        double lnScaledMin = Math.log(shape);
        double lnScaledMax = Math.log(scaledMax + shape);
        double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

        for (int i = 0; i < (int)numLevels; i++) {
          if (curveShape == JOAConstants.EXPONENTIALUP) {
            // lower
            if (baseLevel < endLevel) {
              mColorBarValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
            }
            else {
              mColorBarValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
            }
          }
          else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
            // upper
            if (baseLevel < endLevel) {
              mColorBarValues[(int)numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
            }
            else {
              mColorBarValues[(int)numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
            }
          }
        }
        mColorBarValues[0] = baseLevel;
      }
      else if (curveShape == JOAConstants.LOGISTIC) {
        // logistic is a pieced together from upper and lower asymptote
        int mid = 0;
        int nl = (int)numLevels;
        if (nl % 2 > 0) {
          mid = (nl / 2) + 1;
        }
        else {
          mid = nl / 2;
        }

        // upper asymptote from base level to midpoint
        double newEndLevel = (baseLevel + endLevel) / 2;
        double shape = JOAFormulas.getShape(baseLevel, newEndLevel);
        double scaledMax = Math.abs(baseLevel - newEndLevel);
        double lnScaledMin = Math.log(shape);
        double lnScaledMax = Math.log(scaledMax + shape);
        double increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

        // lower
        for (int i = 0; i < mid; i++) {
          if (baseLevel < newEndLevel) {
            mColorBarValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
          }
          else {
            mColorBarValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
          }
        }

        // lower asymptote from midpoint to endlevel
        double newBaseLevel = newEndLevel;
        shape = JOAFormulas.getShape(newBaseLevel, endLevel);
        scaledMax = Math.abs(newBaseLevel - endLevel);
        lnScaledMin = Math.log(shape);
        lnScaledMax = Math.log(scaledMax + shape);
        increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

        // upper
        int endl = 0;
        if (nl % 2 > 0) {
          endl = mid - 1;
        }
        else {
          endl = mid;
        }
        for (int i = 0; i < endl; i++) {
          if (newBaseLevel < endLevel) {
            mColorBarValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
          }
          else {
            mColorBarValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
          }
        }
        mColorBarValues[0] = baseLevel;
      }
    }

    // assign colors to color bar
    if (mBlendColors.isSelected()) {
      mColorBarColors = null;
      mColorBarColors = new Color[(int)numLevels];
      // blend colors
      // get current colors
      Color startColor = mBlendStartColor.getColor();
      Color midColor = mBlendMiddleColor.getColor();
      Color endColor = mBlendEndColor.getColor();

      int nl = (int)numLevels;
      int mid = 0;
      double deltaRed = 0;
      double deltaGreen = 0;
      double deltaBlue = 0;
      if (nl % 2 > 0) {
        // odd number of entries--middle color is middle color swatch
        mid = (nl / 2) + 1;
        mColorBarColors[mid - 1] = midColor;

        // blend from start to mid
        deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)mid;
        deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)mid;
        deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)mid;

        int c = 1;
        for (int i = 0; i < mid - 1; i++) {
          double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
          double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
          double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
          c++;
          mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
        }

        // blend from mid to end
        deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)mid;
        deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)mid;
        deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)mid;

        c = 1;
        for (int i = mid; i < (int)numLevels; i++) {
          double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
          double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
          double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
          c++;
          mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
        }
      }
      else {
        // even number of entries--middle color is in between middle values
        mid = nl / 2;

        // blend from start to mid
        deltaRed = (double)(midColor.getRed() - startColor.getRed()) / (double)(mid + 1);
        deltaGreen = (double)(midColor.getGreen() - startColor.getGreen()) / (double)(mid + 1);
        deltaBlue = (double)(midColor.getBlue() - startColor.getBlue()) / (double)(mid + 1);

        int c = 1;
        for (int i = 0; i < mid; i++) {
          double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
          double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
          double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
          c++;
          mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
        }

        // blend from mid to end
        deltaRed = (double)(endColor.getRed() - midColor.getRed()) / (double)(mid + 1);
        deltaGreen = (double)(endColor.getGreen() - midColor.getGreen()) / (double)(mid + 1);
        deltaBlue = (double)(endColor.getBlue() - midColor.getBlue()) / (double)(mid + 1);

        c = 1;
        for (int i = mid; i < (int)numLevels; i++) {
          double newRed = (midColor.getRed() + (c * deltaRed)) / 255.0;
          double newGreen = (midColor.getGreen() + (c * deltaGreen)) / 255.0;
          double newBlue = (midColor.getBlue() + (c * deltaBlue)) / 255.0;
          c++;
          mColorBarColors[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
        }
      }
    }
    else if (mAssignFromRainbow.isSelected()) {
      // assign colors from rainbow
      float hue = 0;
      float sat = 1;
      float light = 1;
      float startHue = 0;
      float hueAngleDelta = (float)270 / (float)(numLevels - 1);
      for (int i = 0; i < numLevels; i++) {
        hue = (startHue + ((float)i * hueAngleDelta)) / 360;
        mColorBarColors[i] = new Color(Color.HSBtoRGB(hue, sat, light));
      }
    }
    else if (mAssignFromInvRainbow.isSelected()) {
      // assign colors from inverse rainbow
      int[] rainbowBathyColorRamp = new int[(int)numLevels];
      float hue = 0;
      float sat = 1;
      float light = 1;
      float startHue = 0;
      float hueAngleDelta = (float)270 / (float)(numLevels - 1);
      for (int i = 0; i < numLevels; i++) {
        hue = (startHue + ((float)i * hueAngleDelta)) / 360;
        rainbowBathyColorRamp[i] = Color.HSBtoRGB(hue, sat, light);
      }

      int nc = 0;
      for (int i = (int)numLevels - 1; i >= 0; i--) {
        hue = ((float)i * hueAngleDelta) / 360;
        mColorBarColors[i] = new Color(rainbowBathyColorRamp[nc++]);
      }
    }
    else {
      // assign colors from table
      // get start table index mStartColor.setColor(mCurrPalette.getColor(0));
      int startIndex = mStartColor.getColorIndex(); //mColorIndex;
      int n1 = startIndex;
      int index = 0;
      for (int i = 0; i < (int)numLevels; i++) {
        if (mAcrossTable.isSelected()) {
          // across table
          index = i + startIndex;
          if (index > 255) {
            index = index - 256;
          }
        }
        else {
          // down table
          index = i * 16 + n1;
          if (index == 271) {
            index = 0;
            n1 = -1 * 16;
          }
          if (index > 255) {
            index = index - 255;
          }
        }
        mColorBarColors[i] = mCurrPalette.getColor(index);
      }
    }

    // create a new NewColorBar
    String paramText = mParamField.getText();
    
		String paramUnits = "NA";
		if (mFileViewer != null) { 
			paramUnits = mFileViewer.mAllProperties[mFileViewer.getPropertyPos(paramText, false)].getUnits();
		}
    String titleText = mTitleField.getText();
    String descripText = mDescriptionField.getText();
    if (mColorBar == null) {
      if (!mUseMetadataFlag) {
        mColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int)numLevels, paramText, paramUnits, titleText, descripText);
      }
      else {
        mColorBar = new NewColorBar(mColorBarColors, mColorBarValues, (int)numLevels, paramText, titleText, descripText,
                                    mMetadataType);
      }
    }
    else {
      // modify existing color bar
      if (curveShape != JOAConstants.ASSIGNJUSTCOLORS) {
        mColorBar.setValues(mColorBarValues);
        mColorBar.setColors(mColorBarColors);
        mColorBar.setBaseLevel(mColorBarValues[0]);
        mColorBar.setEndLevel(mColorBarValues[(int)numLevels - 1]);
        mColorBar.setTitle(titleText);
        mColorBar.setParam(paramText);
        mColorBar.setParamUnits(paramUnits);
        mColorBar.setDescription(descripText);
        if (mUseMetadataFlag) {
          mColorBar.setIsMetadataColorBar(true);
          mColorBar.setMetadataType(mMetadataType);
        }
      }
      else {
        mColorBar.setColors(mColorBarColors);
      }
    }

    // set the colorvalue editor to display the new NewColorBar
    if (mUseMetadataFlag) {
      mArrayEditor.setDisplayYAxis(false);
      mArrayEditor.setAllowValueEditing(false);
    }
    else {
      mArrayEditor.setDisplayYAxis(true);
      mArrayEditor.setAllowValueEditing(true);
    }
    mArrayEditor.setValueArray(mColorBarValues, mColorBar.getNumLevels());
    mArrayEditor.setColorArray(mColorBarColors, mColorBar.getNumLevels());
    mArrayEditor.setNumLevels(mColorBarColors.length);
    mColorBarPreview.setColors(mColorBarColors);

    if (!mArrayEditor.isHistogramSet()) {
      mArrayEditor.setHistogram(mColorBar.getParam(), mFileViewer, mColorBar);
    }
    this.validate();
  }

  public void maintainButtons() {
    if (!mUseMetadataFlag) {
      if (mStartField.getText().length() == 0 || mEndField.getText().length() == 0 ||
          mNumContField.getText().length() == 0) {
        mLinear.setEnabled(false);
        mPowerUp.setEnabled(false);
        mPowerDown.setEnabled(false);
        mLogistic.setEnabled(false);
      }
      else {
        mLinear.setEnabled(true);
        mPowerUp.setEnabled(true);
        mPowerDown.setEnabled(true);
        mLogistic.setEnabled(true);
      }
    }
    else {
      if (mNumContField.getText().length() == 0) {
        mLinear.setEnabled(false);
        mPowerUp.setEnabled(false);
        mPowerDown.setEnabled(false);
        mLogistic.setEnabled(false);
      }
      else {
        mLinear.setEnabled(true);
        mPowerUp.setEnabled(true);
        mPowerDown.setEnabled(true);
        mLogistic.setEnabled(true);
      }
    }

    if (mColorBar == null) {
      mOKBtn.setEnabled(false);
      mAssignColorsOnly.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
      mAssignColorsOnly.setEnabled(true);
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getSource() instanceof JOAJComboBox) {
      JOAJComboBox cb = (JOAJComboBox)evt.getSource();
      if (cb == mColorCombo) {
        mCurrPalette = getColorPalette((String)mColorPaletteList.elementAt(cb.getSelectedIndex()));
        mColorPalette.setNewPalette(mCurrPalette, null);
      }
      else if (cb == mMetadataChoices) {
        mMetadataType = new String((String)mMetadataChoices.getSelectedItem());
      }
      else if (cb == mParamCombo) {
        String param = new String((String)cb.getSelectedItem());
        mParamField.setText(param);
      }
    }
    else if (evt.getSource() instanceof JOAJRadioButton) {
      JOAJRadioButton rb = (JOAJRadioButton)evt.getSource();
      if (rb == mAssignFromTable && evt.getStateChange() == ItemEvent.SELECTED) {
        enableTableOptions();
      }
      else if (rb == mAssignFromTable) {
        disableTableOptions();
      }

      if (rb == mUseParameter && evt.getStateChange() == ItemEvent.SELECTED) {
        enableParameterOptions();
        mUseMetadataFlag = false;
        mColorBar.setIsMetadataColorBar(false);
      }
      else if (rb == mUseMetadata && evt.getStateChange() == ItemEvent.SELECTED) {
        disableParameterOptions();
        mUseMetadataFlag = true;
        mMetadataType = new String((String)mMetadataChoices.getSelectedItem());
        mColorBar.setIsMetadataColorBar(true);
        mColorBar.setMetadataType(mMetadataType);
      }
    }
  }

  public ColorPalette getColorPalette(String newPalName) {
    // read the palette from disk
    try {
      ColorPalette newPal = JOAFormulas.readPalette(newPalName);
      return newPal;
    }
    catch (Exception ex) {
      System.out.println("An error occured reading a palette from a file");
    }

    return null;
  }

  public void makeDefault() {
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    File nf = new File(directory, "default_cbr.xml");
    JOAFormulas.saveColorBar(nf, mColorBar);
  }

  public void save() {
    String paramText = null;
    if (!mUseMetadataFlag) {
      paramText = mParamField.getText();
      if (paramText == null || paramText.length() == 0) {
        // error
        JFrame f = new JFrame("Colorbar Save Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(f,
            "Colorbar is not assigned to a parameter--enter a parameter name in Measured Parameter field.");
        return;
      }
    }
    else {
      paramText = (String)mMetadataChoices.getSelectedItem();
      mColorBar.setMetadataType(paramText);
    }
    String titleText = mTitleField.getText();

    // update the colors and text information to reflect any changes nade to the preview
    mColorBar.setColors(mColorBarPreview.getColors());
    mColorBar.setTitle(titleText);
    mColorBar.setParam(paramText);
    mColorBar.setDescription(mDescriptionField.getText());
    
    // add the parameter units if missing and fileviewer is available
    if (mFileViewer != null && mColorBar.getParamUnits() == null && !mUseMetadataFlag) {
  		if (mFileViewer != null) { 
  			String paramUnits = mFileViewer.mAllProperties[mFileViewer.getPropertyPos(paramText, false)].getUnits();
  	    mColorBar.setParamUnits(paramUnits);
  		}
    }

    // save the colorbar to the support directory in XML
    String suggestedName = null;
    try {
      suggestedName = new String(paramText + "-" + titleText + "_cbr.xml");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("cbr") || name.endsWith("_cbr.xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Save colorbar as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    if (suggestedName != null) {
      f.setFile(suggestedName);
    }
    else {
      f.setFile("untitled_cbr.xml");
    }
    f.setVisible(true);
    directory = f.getDirectory();
    f.dispose();
    if (directory != null && f.getFile() != null) {
      File nf = new File(directory, f.getFile());
      JOAFormulas.saveColorBar(nf, mColorBar);
    }
  }
  
  public void export() {
    String paramText = null;
    if (!mUseMetadataFlag) {
      paramText = mParamField.getText();
      if (paramText == null || paramText.length() == 0) {
        // error
        JFrame f = new JFrame("Colorbar Export Error");
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(f,
            "Colorbar is not assigned to a parameter--enter a parameter name in Measured Parameter field.");
        return;
      }
    }
    else {
      paramText = (String)mMetadataChoices.getSelectedItem();
    }
    String titleText = mTitleField.getText();

    // update the colors and text information to reflect any changes made to the preview
    mColorBar.setColors(mColorBarPreview.getColors());
    mColorBar.setTitle(titleText);
    mColorBar.setParam(paramText);
    mColorBar.setDescription(mDescriptionField.getText());

    // save the colorbar to the support directory in XML
    String suggestedName = null;
    try {
      suggestedName = new String(paramText + "-" + titleText + "_cbr.json");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("json") || name.endsWith("_cbr.json")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Export JSON colorbar as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    if (suggestedName != null) {
      f.setFile(suggestedName);
    }
    else {
      f.setFile("untitled_cbr.json");
    }
    f.setVisible(true);
    directory = f.getDirectory();
    f.dispose();
    if (directory != null && f.getFile() != null) {
      File nf = new File(directory, f.getFile());
      JOAFormulas.exportColorBar(nf, mColorBar);
    }
  }

  public void fillInDialog() {
  	// Watch for side effects of setting user controls!!!!!
    if (mColorBar.isMetadataColorBar()) {
      mStartField.setText("0.0");
      mEndField.setText("1.0");
      mParamField.setText("");
      disableParameterOptions();
      mMetadataType = new String(mColorBar.getMetadataType());
      String temp = mMetadataType;
      mUseMetadataFlag = true;
      mUseMetadata.setSelected(true);
      mMetadataChoices.setSelectedIndex(temp);
      mMetadataType = temp;
      mColorBar.setMetadataType(temp);
    }
    else {
      mStartField.setText(JOAFormulas.formatDouble(String.valueOf(mColorBar.getBaseLevel()), 2, false));
      mEndField.setText(JOAFormulas.formatDouble(String.valueOf(mColorBar.getEndLevel()), 2, false));
      mParamField.setText(mColorBar.getParam());
      mUseMetadataFlag = false;
    }

    if (mUseMetadataFlag) {
      mArrayEditor.setDisplayYAxis(false);
      mArrayEditor.setAllowValueEditing(false);
    }
    else {
      mArrayEditor.setDisplayYAxis(true);
      mArrayEditor.setAllowValueEditing(true);
    }

    mNumContField.setText(String.valueOf(mColorBar.getNumLevels()));
    mTitleField.setText(mColorBar.getTitle());
    mDescriptionField.setText(mColorBar.getDescription());
    mArrayEditor.setValueArray(mColorBarValues, mColorBar.getNumLevels());
    mArrayEditor.setColorArray(mColorBarColors, mColorBar.getNumLevels());
    mColorBarPreview.setColors(mColorBarColors);
    
    if (mUseMetadataFlag) {
      mArrayEditor.setDisplayYAxis(false);
      mArrayEditor.setAllowValueEditing(false);
    }
    else {
      mArrayEditor.setDisplayYAxis(true);
      mArrayEditor.setAllowValueEditing(true);
    }
    this.validate();
  }

  public void enableTableOptions() {
    mStartColor.setEnabled(true);
    mAcrossTable.setEnabled(true);
    mDownTable.setEnabled(true);
  }

  public void disableTableOptions() {
    mStartColor.setEnabled(false);
    mAcrossTable.setEnabled(false);
    mDownTable.setEnabled(false);
  }

  public void enableParameterOptions() {
    mStartField.setEnabled(true);
    mParamField.setEnabled(true);
    mEndField.setEnabled(true);
    mMetadataChoices.setEnabled(false);
    mStartValueLabel.setEnabled(true);
    mEndValueLabel.setEnabled(true);
    if (mParamCombo != null) {
      mParamCombo.setEnabled(true);
    }
  }

  public void disableParameterOptions() {
    mParamField.setEnabled(false);
    mStartField.setEnabled(false);
    mEndField.setEnabled(false);
    mMetadataChoices.setEnabled(true);
    mStartValueLabel.setEnabled(false);
    mEndValueLabel.setEnabled(false);
    if (mParamCombo != null) {
      mParamCombo.setEnabled(false);
    }
  }
}

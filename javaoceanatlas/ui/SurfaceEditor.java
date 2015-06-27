/*
 * $Id: SurfaceEditor.java,v 1.3 2005/06/17 18:08:55 oz Exp $
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
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class SurfaceEditor extends JOAJDialog implements ActionListener, ButtonMaintainer {
  protected JOAJButton mOKBtn = null;
  protected JOAJButton mExportBtn = null;
  protected JOAJButton mCancelButton = null;
  protected JOAJTextField mStartField = null;
  protected JOAJTextField mEndField = null;
  protected JOAJTextField mNumContField = null;
  protected ColorArrayEditor mArrayEditor = null;
  protected LargeIconButton mLinear = null;
  protected LargeIconButton mPowerUp = null;
  protected LargeIconButton mPowerDown = null;
  protected LargeIconButton mLogistic = null;
  protected JOAJTextField mParamField = null;
  protected JOAJTextField mTitleField = null;
  protected JOAJTextField mDescriptionField = null;
  protected NewInterpolationSurface mSurface = null;
  protected double[] mSurfaceValues = null;
	private Timer timer = new Timer();
  protected DialogClient mClient;
  protected JDialog mFrame = null;
  private double numLevels = 0;
  private double oldNumLevels = 0;

  public SurfaceEditor(JOAWindow par, DialogClient client) {
    super(par, "Interpolation Surface Editor", false);
    mClient = client;
    init();
  }

  public SurfaceEditor(JOAWindow par, NewInterpolationSurface inSurf, DialogClient client) {
    super(par, "Interpolation Surface Editor", false);
    mSurface = inSurf;
    mClient = client;
    mSurfaceValues = mSurface.getValues();
    init();
    fillInDialog();
  }

  public void init() {
    mFrame = this;
    ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    Container contents = this.getContentPane();
    this.getContentPane().setLayout(new BorderLayout(0, 0));
    JPanel mainPanel = new JPanel(); // everything goes in here
    mainPanel.setLayout(new BorderLayout(5, 0));

    // upper panel
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new BorderLayout(5, 0));

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
    line1.add(new JOAJLabel(b.getString("kParameter2")));
    mParamField = new JOAJTextField(4);
    mParamField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mParamField);
    line1.add(new JOAJLabel("  "));
    line1.add(new JOAJLabel(b.getString("kTitle")));
    mTitleField = new JOAJTextField(15);
    mTitleField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line1.add(mTitleField);
    JPanel line2 = new JPanel();
    line2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line2.add(new JOAJLabel(b.getString("kDescription2")));
    mDescriptionField = new JOAJTextField(31);
    mDescriptionField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line2.add(mDescriptionField);
    descriptionPanel.add(line1);
    descriptionPanel.add(line2);

    // Levels
    JPanel contourPanel = new JPanel();
    contourPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kLevels"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    contourPanel.setBorder(tb);
    JPanel line3 = new JPanel();
    line3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
    line3.add(new JOAJLabel(b.getString("kNumLevels")));
    mNumContField = new JOAJTextField(4);
    mNumContField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3.add(mNumContField);
    line3.add(new JOAJLabel("  "));
    line3.add(new JOAJLabel(b.getString("kFirstLevel")));
    mStartField = new JOAJTextField(6);
    mStartField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3.add(mStartField);
    line3.add(new JOAJLabel("  "));
    line3.add(new JOAJLabel(b.getString("kLastLevel")));
    mEndField = new JOAJTextField(6);
    mEndField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    line3.add(mEndField);
    contourPanel.add(line3);

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
    line6.add(shapePanel);
		
		mLinear.setToolTipText(b.getString("kSurfLinearTip"));
		mPowerUp.setToolTipText(b.getString("kSurfIncreasingExpTip"));
		mPowerDown.setToolTipText(b.getString("kSurfDecreasingExpTip"));
		mLogistic.setToolTipText(b.getString("kSurfReverseSTip"));

    // build the top
    upperGridPanel.add(descriptionPanel);
    upperGridPanel.add(contourPanel);
    upperPanel.add("North", upperGridPanel);
    upperPanel.add("Center", line6);

    // lower Panel
    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(new RowLayout(Orientation.CENTER, Orientation.TOP, 0));

    // Value Editor
    JPanel valEdPreviewPanel = new JPanel();
    valEdPreviewPanel.setLayout(new RowLayout(Orientation.CENTER, Orientation.TOP, 0));
    tb = BorderFactory.createTitledBorder(b.getString("kLevelEditor"));
    if (JOAConstants.ISMAC) {
      //tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
    }
    valEdPreviewPanel.setBorder(tb);
    mArrayEditor = new ColorArrayEditor(this.getRootPane(), false);
    mArrayEditor.setAllowValueEditing(true);
    //mArrayEditor.setHistogram(mColorBar.getParam(), mFileViewer, mColorBar);
    valEdPreviewPanel.add(mArrayEditor);
    lowerPanel.add(valEdPreviewPanel);

    // button panel
    mExportBtn = new JOAJButton("Export JSON...");
    mExportBtn.setActionCommand("export");
    
    mOKBtn = new JOAJButton(b.getString("kSave"));
    mOKBtn.setActionCommand("ok");
    this.getRootPane().setDefaultButton(mOKBtn);
    
    mCancelButton = new JOAJButton(b.getString("kClose"));
    mCancelButton.setActionCommand("cancel");
    JPanel dlgBtnsInset = new JPanel();
    JPanel dlgBtnsPanel = new JPanel();
    dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
    dlgBtnsPanel.setLayout(new GridLayout(1, 3, 15, 1));
    if (JOAConstants.ISMAC) {
      dlgBtnsPanel.add(mCancelButton);
      dlgBtnsPanel.add(mExportBtn);
      dlgBtnsPanel.add(mOKBtn);
    }
    else {
      dlgBtnsPanel.add(mOKBtn);
      dlgBtnsPanel.add(mExportBtn);
      dlgBtnsPanel.add(mCancelButton);
    }
    dlgBtnsInset.add(dlgBtnsPanel);

    mOKBtn.addActionListener(this);
    mExportBtn.addActionListener(this);
    mCancelButton.addActionListener(this);

    // add all the sub panels to main panel
    mainPanel.add(new TenPixelBorder(upperPanel, 0, 5, 0, 5), "North");
    mainPanel.add(new TenPixelBorder(lowerPanel, 5, 5, 0, 5), "Center");
    mainPanel.add(new TenPixelBorder(dlgBtnsInset, 0, 5, 5, 5), "South");
    contents.add("Center", mainPanel);

    WindowListener windowListener = new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        mClient.dialogCancelled(mFrame);
        timer.cancel();
        e.getWindow().dispose();
      }
    };
    this.addWindowListener(windowListener);

    runTimer();
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
      mClient.dialogCancelled(this);
      timer.cancel();
      this.dispose();
    }
    else if (cmd.equals("ok")) {
      save();
      mClient.dialogDismissed(this);
    }
    else if (cmd.equals("export")) {
      exportAsJSON();
    }
    else if (cmd.equals("linear")) {
      boolean err = createSurface(JOAConstants.LINEAR);
      mLinear.setSelected(false);
      if (!err) {
        resizeWindow();
      }
    }
    else if (cmd.equals("powerUp")) {
      boolean err = createSurface(JOAConstants.EXPONENTIALUP);
      mPowerUp.setSelected(false);
      if (!err) {
        resizeWindow();
      }
    }
    else if (cmd.equals("powerDown")) {
      boolean err = createSurface(JOAConstants.EXPONENTIALDOWN);
      mPowerDown.setSelected(false);
      if (!err) {
        resizeWindow();
      }
    }
    else if (cmd.equals("logistic")) {
      boolean err = createSurface(JOAConstants.LOGISTIC);
      mLogistic.setSelected(false);
      if (!err) {
        resizeWindow();
      }
    }
  }

  public void resizeWindow() {
    if (numLevels > 32 || oldNumLevels > 32) {
      if (oldNumLevels > numLevels) {
        // window should get smaller
      }
      else {
        //window should get bigger
      }
      //this.setSize(width, mFrame.getSize().height);
    }
    // set the colorvalue editor to display the new NewColorBar
    mArrayEditor.setValueArray(mSurfaceValues, mSurface.getNumLevels());
    this.validate();
  }

  public boolean createSurface(int curveShape) {
    // get base and end levels
    double baseLevel = 0;
    double endLevel = 0;
    oldNumLevels = numLevels;
    boolean err1 = false;
    boolean err2 = false;
    boolean err3 = false;
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
      err3 = true;
    }

    if (numLevels <= 0) {
      err1 = true;
    }

    if (endLevel < baseLevel) {
      err2 = true;
    }

    if (err1 || err2 || err3) {
      String errStr = "";
      if (err3) {
        errStr = "An invalid character was found in a field that expected a numeric value " + "\n" +
            "(number of levels, first level, or last level).";
      }
      else {
        if (err1) {
          errStr += "Number of levels must greater than 0." + "\n";
        }
        if (err2) {
          errStr += "The last level value must be greater than the start level value." + "\n";
        }
      }
      JFrame f = new JFrame("Surface Specification Error");
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(f, errStr);
      return true;
    }

    // compute new surface values
    mSurfaceValues = null;
    mSurfaceValues = new double[(int)numLevels];
    if (curveShape == JOAConstants.LINEAR) {
      double increment = (endLevel - baseLevel) / (numLevels - 1);
      for (int i = 0; i < (int)numLevels; i++) {
        mSurfaceValues[i] = baseLevel + (i * increment);
      }
    }
    else if (curveShape == JOAConstants.EXPONENTIALUP || curveShape == JOAConstants.EXPONENTIALDOWN) {
      double shape = getShape(baseLevel, endLevel);
      double scaledMax = Math.abs(endLevel - baseLevel);
      double lnScaledMin = Math.log(shape);
      double lnScaledMax = Math.log(scaledMax + shape);
      double increment = (lnScaledMax - lnScaledMin) / (numLevels - 1);

      for (int i = 0; i < (int)numLevels; i++) {
        if (curveShape == JOAConstants.EXPONENTIALUP) {
          // lower
          if (baseLevel < endLevel) {
            mSurfaceValues[i] = baseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
          }
          else {
            mSurfaceValues[i] = baseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
          }
        }
        else if (curveShape == JOAConstants.EXPONENTIALDOWN) {
          // upper
          if (baseLevel < endLevel) {
            mSurfaceValues[(int)numLevels - i - 1] = endLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
          }
          else {
            mSurfaceValues[(int)numLevels - i - 1] = endLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
          }
        }
      }
      mSurfaceValues[0] = baseLevel;
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
      double shape = getShape(baseLevel, newEndLevel);
      double scaledMax = Math.abs(baseLevel - newEndLevel);
      double lnScaledMin = Math.log(shape);
      double lnScaledMax = Math.log(scaledMax + shape);
      double increment = (lnScaledMax - lnScaledMin) / ((double)mid - 1);

      // lower
      for (int i = 0; i < mid; i++) {
        if (baseLevel < newEndLevel) {
          mSurfaceValues[mid - i - 1] = newEndLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
        }
        else {
          mSurfaceValues[mid - i - 1] = newEndLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
        }
      }

      // lower asymptote from midpoint to endlevel
      double newBaseLevel = newEndLevel;
      shape = getShape(newBaseLevel, endLevel);
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
          mSurfaceValues[i + mid] = newBaseLevel + Math.exp(lnScaledMin + (i * increment)) - shape;
        }
        else {
          mSurfaceValues[i + mid] = newBaseLevel - Math.exp(lnScaledMin + (i * increment)) + shape;
        }
      }
      mSurfaceValues[0] = baseLevel;
    }

    // create a new surface
    String paramText = mParamField.getText();
    String titleText = mTitleField.getText();
    String descripText = mDescriptionField.getText();
    if (mSurface == null) {
      mSurface = new NewInterpolationSurface(mSurfaceValues, (int)numLevels, paramText, titleText, descripText);
    }
    else {
      // modify existing surface
      mSurface.setValues(mSurfaceValues);
      mSurface.setNumLevels((int)numLevels);
      mSurface.setBaseLevel(mSurfaceValues[0]);
      mSurface.setEndLevel(mSurfaceValues[(int)numLevels - 1]);
      mSurface.setTitle(titleText);
      mSurface.setParam(paramText);
      mSurface.setDescription(descripText);
    }

    return false;
  }

  protected double getShape(double baseLevel, double endLevel) {
    double diff;

    diff = Math.abs(baseLevel - endLevel);
    if (diff >= 1000) {
      return 100.0;
    }
    if (diff >= 100 && diff < 1000) {
      return 10.0;
    }
    if (diff >= 10 && diff < 100) {
      return 1.0;
    }
    if (diff >= 1 && diff < 10) {
      return 0.1;
    }
    if (diff >= 0 && diff < 1) {
      return 0.01;
    }
    return 0.001;
  }

  public void maintainButtons() {
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

    if (mSurface == null) {
      mOKBtn.setEnabled(false);
    }
    else {
      mOKBtn.setEnabled(true);
    }
  }

  public void save() {
    String paramText = mParamField.getText();
    String titleText = mTitleField.getText();
    String descripText = mDescriptionField.getText();
    double numLevels = Double.valueOf(mNumContField.getText()).doubleValue();

    // update the surface
    mSurface.setValues(mSurfaceValues);
    mSurface.setNumLevels((int)numLevels);
    mSurface.setBaseLevel(mSurfaceValues[0]);
    mSurface.setEndLevel(mSurfaceValues[(int)numLevels - 1]);
    mSurface.setTitle(titleText);
    mSurface.setParam(paramText);
    mSurface.setDescription(descripText);

    // save the surface object to the support directory
    String suggestedName = null;
    try {
      suggestedName = new String(paramText + "-" + titleText + "_srf.xml");
    }
    catch (Exception ex) {

    }

    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("srf") || name.endsWith("_srf.xml")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Save interpolation surface as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    if (suggestedName != null) {
      f.setFile(suggestedName);
    }
    else {
      f.setFile("untitled_srf.xml");
    }
    f.setVisible(true);
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();
    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
        JOAFormulas.saveSurface(nf, mSurface);
      }
      catch (Exception ex) {}
    }
  }
  
  public void exportAsJSON() {
    String paramText = mParamField.getText();
    String titleText = mTitleField.getText();
    String descripText = mDescriptionField.getText();
    double numLevels = Double.valueOf(mNumContField.getText()).doubleValue();

    // update the surface
    mSurface.setValues(mSurfaceValues);
    mSurface.setNumLevels((int)numLevels);
    mSurface.setBaseLevel(mSurfaceValues[0]);
    mSurface.setEndLevel(mSurfaceValues[(int)numLevels - 1]);
    mSurface.setTitle(titleText);
    mSurface.setParam(paramText);
    mSurface.setDescription(descripText);

    // save the surface object to the support directory
    String suggestedName = null;
    try {
      suggestedName = new String(paramText + "-" + titleText + "_srf.json");
    }
    catch (Exception ex) {

    }

    // get a filename
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith("_srf.json")) {
          return true;
        }
        else {
          return false;
        }
      }
    };
    Frame fr = new Frame();
    String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator;
    FileDialog f = new FileDialog(fr, "Export JSON interpolation surface as:", FileDialog.SAVE);
    f.setDirectory(directory);
    f.setFilenameFilter(filter);
    if (suggestedName != null) {
      f.setFile(suggestedName);
    }
    else {
      f.setFile("untitled_srf.json");
    }
    f.setVisible(true);
    directory = f.getDirectory();
    String fs = f.getFile();
    f.dispose();
    if (directory != null && fs != null) {
      File nf = new File(directory, fs);
      try {
      	// save happens here
      	JsonFactory jf = new JsonFactory();
    		JsonGenerator jsonGen;
				jsonGen = jf.createJsonGenerator(nf, JsonEncoding.UTF8);
				jsonGen.setPrettyPrinter(new DefaultPrettyPrinter());
				
				jsonGen.writeStartObject();
				
	  			jsonGen.writeObjectFieldStart("interpolationsurface");
						jsonGen.writeStringField("title", mSurface.getTitle());
						jsonGen.writeStringField("parameter", mSurface.getParam());
						jsonGen.writeStringField("description", mSurface.getDescrip());
						jsonGen.writeNumberField("numlevels", mSurface.getNumLevels());
						jsonGen.writeNumberField("baselevel", mSurface.getBaseLevel());
						jsonGen.writeNumberField("endlevel", mSurface.getEndLevel());

						double [] vals = mSurface.getValues();
		  	    jsonGen.writeArrayFieldStart("levelvalues");
		  	    for (int i=0; i<vals.length; i++) {
		  	    	jsonGen.writeNumber(vals[i]);
		  	    }
		  	    jsonGen.writeEndArray(); // for field 'levelvalues'	        
		  	    jsonGen.writeEndObject(); // for surface section

        jsonGen.writeEndObject();

  			jsonGen.close(); // important: will force flushing of output, close underlying output stream
      }
      catch (Exception ex) {}
    }
  }

  public void fillInDialog() {
    mStartField.setText(JOAFormulas.formatDouble(String.valueOf(mSurface.getBaseLevel()), 2, false));
    mEndField.setText(JOAFormulas.formatDouble(String.valueOf(mSurface.getEndLevel()), 2, false));
    mNumContField.setText(String.valueOf(mSurface.getNumLevels()));
    mParamField.setText(mSurface.getParam());
    mTitleField.setText(mSurface.getTitle());
    mDescriptionField.setText(mSurface.getDescrip());
    mArrayEditor.setValueArray(mSurfaceValues, mSurface.getNumLevels());
    this.validate();
  }
}

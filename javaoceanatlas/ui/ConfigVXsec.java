/*
 * $Id: ConfigVXsec.java,v 1.4 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigVXsec extends JOAJDialog/*CloseableFrame*/ implements ActionListener {
	protected Component mComp;
    protected JOAJButton mOKBtn = null;
    protected JOAJButton mCancelButton = null;
    protected JOAJButton mApplyButton = null;
    protected JOAJTextField mMinValFld = null;
    protected JOAJTextField mMaxValFld = null;
    protected JOAJTextField mOvlMinValFld = null;
    protected JOAJTextField mOvlMaxValFld = null;
    protected JOAJTextField mIncFld = null;
    protected JOAJTextField mOvlIncFld = null;
	protected DialogClient mClient = null;
	protected double mMin, mMax, mOrigMin, mOrigMax, mInc, mOrigInc;
	protected double mOvlMin, mOvlMax, mOvlOrigMin, mOvlOrigMax, mOvlInc, mOvlOrigInc;
	protected double mYDataMin, mYDataMax;
	protected double mYOvlDataMin, mYOvlDataMax;
	protected JDialog/*CloseableFrame*/ mFrame = null;
	protected boolean mOvl = false;

    public ConfigVXsec(JOAWindow parent, DialogClient client, double min, double max, double inc, double dMin, double dMax) {
    	super(parent, "Configure Vertical Cross Section", false);
		mClient = client;
    	mMin = mOrigMin = min;
    	mMax = mOrigMax = max;
    	mInc = mOrigInc = inc;
    	mYDataMin = dMin;
    	mYDataMax = dMax;
		this.init(false);

    	mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				mClient.dialogCancelled(mFrame);
			}
		};
		this.addWindowListener(windowListener);
	}

    public ConfigVXsec(JOAWindow parent, DialogClient client, double min, double max, double inc, double dMin, double dMax,
    				   double ovlmin, double ovlmax, double ovlinc, double ovldMin, double ovldMax) {
    	super(parent, "Configure Vertical Cross Section", false);
		mClient = client;
    	mMin = mOrigMin = min;
    	mMax = mOrigMax = max;
    	mInc = mOrigInc = inc;
    	mYDataMin = dMin;
    	mYDataMax = dMax;
    	mOvlMin = mOvlOrigMin = ovlmin;
    	mOvlMax = mOvlOrigMax = ovlmax;
    	mOvlInc = mOvlOrigInc = ovlinc;
    	mYOvlDataMin = ovldMin;
    	mYOvlDataMax = ovldMax;
    	mOvl = true;
		this.init(true);

    	mFrame = this;
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				mClient.dialogCancelled(mFrame);
			}
		};
		this.addWindowListener(windowListener);
	}

    public void init(boolean addOvl) {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

    	// create the two parameter chooser lists
    	Container contents = this.getContentPane();
    	this.getContentPane().setLayout(new BorderLayout(0, 0));
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new BorderLayout(0, 0));

    	// Ranges goes in the middle of the middle panel
    	JPanel middlePanel = new JPanel();
    	middlePanel.setLayout(new javaoceanatlas.utility.ColumnLayout(javaoceanatlas.utility.Orientation.CENTER,
        	javaoceanatlas.utility.Orientation.CENTER, 2));
    	TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kXAxisRange"));
		if (JOAConstants.ISMAC) {
			//tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
    	middlePanel.setBorder(tb);

    	// y axis
    	JPanel line0 = new JPanel();
	    line0.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));

    	line0.add(new JOAJLabel(b.getString("kBaseLayer")));
    	line0.add(new JOAJLabel(b.getString("kMinimum")));
    	mMinValFld = new JOAJTextField(6);
		mMinValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	line0.add(mMinValFld);
    	line0.add(new JOAJLabel(b.getString("kMaximum")));
    	mMaxValFld = new JOAJTextField(6);
		mMaxValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	line0.add(mMaxValFld);
    	line0.add(new JOAJLabel(b.getString("kIncrement")));
    	mIncFld = new JOAJTextField(6);
		mIncFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    	line0.add(mIncFld);
	    mMinValFld.setText(JOAFormulas.formatDouble(String.valueOf(mMin), 2, false));
	    mMaxValFld.setText(JOAFormulas.formatDouble(String.valueOf(mMax), 2, false));
	    mIncFld.setText(JOAFormulas.formatDouble(String.valueOf(mInc), 2, false));
		middlePanel.add(line0);

	    if (addOvl) {
	    	JPanel line1 = new JPanel();
		    line1.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));

	    	line1.add(new JOAJLabel(b.getString("kOverlayLayer")));
	    	line1.add(new JOAJLabel(b.getString("kMinimum")));
	    	mOvlMinValFld = new JOAJTextField(6);
			mOvlMinValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	    	line1.add(mOvlMinValFld);
	    	line1.add(new JOAJLabel(b.getString("kMaximum")));
	    	mOvlMaxValFld = new JOAJTextField(6);
			mOvlMaxValFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	    	line1.add(mOvlMaxValFld);
	    	line1.add(new JOAJLabel(b.getString("kIncrement")));
	    	mOvlIncFld = new JOAJTextField(6);
			mOvlIncFld.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	    	line1.add(mOvlIncFld);
		    mOvlMinValFld.setText(JOAFormulas.formatDouble(String.valueOf(mOvlMin), 2, false));
		    mOvlMaxValFld.setText(JOAFormulas.formatDouble(String.valueOf(mOvlMax), 2, false));
		    mOvlIncFld.setText(JOAFormulas.formatDouble(String.valueOf(mOvlInc), 2, false));
		    middlePanel.add(line1);
	    }

    	// build upper part of dialog
    	mainPanel.add("Center", new TenPixelBorder(middlePanel, 5, 5, 5, 5));

		// lower panel
    	mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
    	this.getRootPane().setDefaultButton(mOKBtn);
    	mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
    	mApplyButton = new JOAJButton(b.getString("kApply"));
		mApplyButton.setActionCommand("apply");
    	JOAJButton mUseDataMaxMin = new JOAJButton(b.getString("kDataMaxMin"));
		mUseDataMaxMin.setActionCommand("minmax");
        mUseDataMaxMin.addActionListener(this);
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
        dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
        dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
    	if (JOAConstants.ISMAC) {
	    	dlgBtnsPanel.add(mCancelButton);
    		dlgBtnsPanel.add(mUseDataMaxMin);
	    	dlgBtnsPanel.add(mApplyButton);
	    	dlgBtnsPanel.add(mOKBtn);
		}
		else {
	    	dlgBtnsPanel.add(mOKBtn);
	    	dlgBtnsPanel.add(mApplyButton);
    		dlgBtnsPanel.add(mUseDataMaxMin);
	    	dlgBtnsPanel.add(mCancelButton);
		}
        dlgBtnsInset.add(dlgBtnsPanel);

        mOKBtn.addActionListener(this);
        mApplyButton.addActionListener(this);
        mCancelButton.addActionListener(this);

        mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
        contents.add("Center", mainPanel);
        this.pack();

		// show dialog at center of screen
		Rectangle dBounds = this.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
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
		else if (cmd.equals("apply")) {
			mClient.dialogApply(this);
		}
		if (cmd.equals("minmax")) {
			// set the ranges to the max min for that parameter
			mMinValFld.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMin), 3, false));
			mMaxValFld.setText(JOAFormulas.formatDouble(String.valueOf(mYDataMax), 3, false));
			mIncFld.setText(JOAFormulas.formatDouble(String.valueOf((mYDataMax-mYDataMin)/5.0), 3, false));

			if (mOvl) {
				// set the ranges to the max min for that parameter
				mOvlMinValFld.setText(JOAFormulas.formatDouble(String.valueOf(mYOvlDataMin), 3, false));
				mOvlMaxValFld.setText(JOAFormulas.formatDouble(String.valueOf(mYOvlDataMax), 3, false));
				mOvlIncFld.setText(JOAFormulas.formatDouble(String.valueOf((mYOvlDataMax-mYOvlDataMin)/5.0), 3, false));
			}
		}
	}


    public double getMin() {
		String fldText = mMinValFld.getText();
		double yMin;
		if (fldText.length() == 0)
			yMin = mOrigMin;
		else {
			try {
				yMin = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yMin = mOrigMin;
			}
		}
		return yMin;
    }

    public double getMax() {
		String fldText = mMaxValFld.getText();
		double yMax;

		if (fldText.length() == 0)
			yMax = mOrigMax;
		else {
			try {
				yMax = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yMax = mOrigMax;
			}
		}
    	return yMax;
    }

    public double getInc() {
		String fldText = mIncFld.getText();
		double inc;

		if (fldText.length() == 0)
			inc = mOrigInc;
		else {
			try {
				inc = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				inc = mOrigInc;
			}
			if (inc == 0.0)
				inc = mOrigInc;
		}
    	return inc;
    }

    public double getOrigMin() {
    	return mOrigMin;
    }

    public double getOrigMax() {
    	return mOrigMax;
    }

    public double getOrigInc() {
    	return mOrigInc;
    }

    public double getOvlOrigMin() {
    	return mOvlOrigMin;
    }

    public double getOvlOrigMax() {
    	return mOvlOrigMax;
    }

    public double getOvlOrigInc() {
    	return mOvlOrigInc;
    }

    public double getOvlMin() {
		String fldText = mOvlMinValFld.getText();
		double yMin;
		if (fldText.length() == 0)
			yMin = mOrigMin;
		else {
			try {
				yMin = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yMin = mOvlOrigMin;
			}
		}
		return yMin;
    }

    public double getOvlMax() {
		String fldText = mOvlMaxValFld.getText();
		double yMax;

		if (fldText.length() == 0)
			yMax = mOrigMax;
		else {
			try {
				yMax = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				yMax = mOvlOrigMax;
			}
		}
    	return yMax;
    }

    public double getOvlInc() {
		String fldText = mOvlIncFld.getText();
		double inc;

		if (fldText.length() == 0)
			inc = mOrigInc;
		else {
			try {
				inc = Double.valueOf(fldText).doubleValue();
			}
			catch (NumberFormatException ex) {
				inc = mOrigInc;
			}
			if (inc == 0.0)
				inc = mOvlOrigInc;
		}
    	return inc;
    }
}

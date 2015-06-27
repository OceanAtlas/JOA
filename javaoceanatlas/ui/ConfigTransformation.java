/*
 * $Id: ConfigTransformation.java,v 1.4 2005/09/07 18:49:31 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import javax.swing.border.*;
import javaoceanatlas.calculations.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

@SuppressWarnings("serial")
public class ConfigTransformation extends JOAJDialog implements ListSelectionListener, ActionListener,
    ButtonMaintainer, ItemListener {
	protected FileViewer mFileViewer;
	protected ParameterChooser mParamList;
	protected int mSelParam = -1;
	protected JOAJButton mOKBtn = null;
	protected JOAJButton mCancelButton = null;
	protected JOAJTextField mNewParamName = null;
	protected JOAJTextField mNewParamUnits = null;
	protected JOAJRadioButton lnx = null;
	protected JOAJRadioButton logx = null;
	protected JOAJRadioButton xsqrd = null;
	protected JOAJRadioButton sinx = null;
	protected JOAJRadioButton recipx = null;
	protected JOAJRadioButton ex = null;
	protected JOAJRadioButton tenx = null;
	protected JOAJRadioButton sqrootx = null;
	protected JOAJRadioButton cosx = null;
	protected JOAJRadioButton ptoz = null;
	protected JOAJRadioButton ztop = null;
	protected JOAJRadioButton volToMass = null;
	protected JOAJCheckBox reverseY = null;
	protected JOAJRadioButton create = null;
	protected boolean mIgnoreVC = false;
	protected String mOpText = new String("ln");
	protected int mOperand = JOAConstants.LN_OP;
	private Timer timer = new Timer();
	protected int mNewParamCnt = 1;

	public ConfigTransformation(JFrame par, FileViewer fv) {
		super(par, "Parameter Transformations", false);
		mFileViewer = fv;
		this.init();
	}

	public void init() {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");

		// create the two parameter chooser lists
		Container contents = this.getContentPane();
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));
		JPanel paramPanel = new JPanel();
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout(5, 5));
		paramPanel.setLayout(new GridLayout(1, 2, 5, 5));// RowLayout(Orientation.LEFT,
																											// Orientation.CENTER,
																											// 5));
		mParamList = new ParameterChooser(mFileViewer, new String(b.getString("kParameter")), this, "SALT                ");
		mParamList.init();
		paramPanel.add(mParamList);

		// add the operator panel
		JPanel opPanel = new JPanel();
		opPanel.setLayout(new GridLayout(5, 1, 5, 0));
		JPanel lnPanel = new JPanel();
		lnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		lnx = new JOAJRadioButton("", true);
		JOAJLabel lbl1 = new JOAJLabel(new ImageIcon(getClass().getResource("images/ln.gif")));
		lnPanel.add(lnx);
		lnPanel.add(lbl1);
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		logx = new JOAJRadioButton("");
		JOAJLabel lbl2 = new JOAJLabel(new ImageIcon(getClass().getResource("images/log.gif")));
		logPanel.add(logx);
		logPanel.add(lbl2);
		JPanel sqrdPanel = new JPanel();
		sqrdPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		xsqrd = new JOAJRadioButton("");
		JOAJLabel lbl3 = new JOAJLabel(new ImageIcon(getClass().getResource("images/sqr.gif")));
		sqrdPanel.add(xsqrd);
		sqrdPanel.add(lbl3);
		JPanel sinPanel = new JPanel();
		sinPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		sinx = new JOAJRadioButton("");
		JOAJLabel lbl4 = new JOAJLabel(new ImageIcon(getClass().getResource("images/sin.gif")));
		sinPanel.add(sinx);
		sinPanel.add(lbl4);
		JPanel cosPanel = new JPanel();
		cosPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		cosx = new JOAJRadioButton("");
		JOAJLabel lbl5 = new JOAJLabel(new ImageIcon(getClass().getResource("images/cos.gif")));
		cosPanel.add(cosx);
		cosPanel.add(lbl5);
		JPanel recipPanel = new JPanel();
		recipPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		recipx = new JOAJRadioButton("");
		JOAJLabel lbl6 = new JOAJLabel(new ImageIcon(getClass().getResource("images/recip.gif")));
		recipPanel.add(recipx);
		recipPanel.add(lbl6);
		JPanel expPanel = new JPanel();
		expPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		ex = new JOAJRadioButton("");
		JOAJLabel lbl7 = new JOAJLabel(new ImageIcon(getClass().getResource("images/exp.gif")));
		expPanel.add(ex);
		expPanel.add(lbl7);
		JPanel tenxPanel = new JPanel();
		tenxPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		tenx = new JOAJRadioButton("");
		JOAJLabel lbl8 = new JOAJLabel(new ImageIcon(getClass().getResource("images/tenx.gif")));
		tenxPanel.add(tenx);
		tenxPanel.add(lbl8);
		JPanel sqrtPanel = new JPanel();
		sqrtPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		sqrootx = new JOAJRadioButton("");
		JOAJLabel lbl9 = new JOAJLabel(new ImageIcon(getClass().getResource("images/sqrt.gif")));
		sqrtPanel.add(sqrootx);
		sqrtPanel.add(lbl9);
		ptoz = new JOAJRadioButton("z=f(p)");
		ztop = new JOAJRadioButton("p=f(z)");
		volToMass = new JOAJRadioButton("Volume -> Mass");
		ButtonGroup bg = new ButtonGroup();
		bg.add(lnx);
		bg.add(logx);
		bg.add(xsqrd);
		bg.add(sinx);
		bg.add(cosx);
		bg.add(recipx);
		bg.add(ex);
		bg.add(tenx);
		bg.add(sqrootx);
		bg.add(ptoz);
		bg.add(ztop);
		bg.add(volToMass);
		opPanel.add(lnPanel);
		opPanel.add(logPanel);
		opPanel.add(sqrdPanel);
		opPanel.add(sqrtPanel);
		opPanel.add(sinPanel);
		opPanel.add(cosPanel);
		opPanel.add(recipPanel);
		opPanel.add(expPanel);
		opPanel.add(tenxPanel);
		opPanel.add(ptoz);
		opPanel.add(ztop);
		opPanel.add(volToMass);
		lnx.addItemListener(this);
		logx.addItemListener(this);
		xsqrd.addItemListener(this);
		sinx.addItemListener(this);
		cosx.addItemListener(this);
		recipx.addItemListener(this);
		ex.addItemListener(this);
		tenx.addItemListener(this);
		ptoz.addItemListener(this);
		ztop.addItemListener(this);
		sqrootx.addItemListener(this);
		TitledBorder tb = BorderFactory.createTitledBorder(b.getString("kTransformations"));
		if (JOAConstants.ISMAC) {
			// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		opPanel.setBorder(tb);
		paramPanel.add(opPanel);

		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new ColumnLayout(Orientation.LEFT, Orientation.CENTER, 5));

		JPanel line1 = new JPanel();
		line1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		line1.add(new JOAJLabel(b.getString("kNewParameterName")));
		mNewParamName = new JOAJTextField("NP" + mNewParamCnt, 10);
		mNewParamName.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line1.add(mNewParamName);
		middlePanel.add(line1);

		JPanel line1a = new JPanel();
		line1a.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		line1a.add(new JOAJLabel(b.getString("kUnits")));
		mNewParamUnits = new JOAJTextField(6);
		mNewParamUnits.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		line1a.add(mNewParamUnits);
		middlePanel.add(line1a);

		JPanel line2 = new JPanel();
		line2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		reverseY = new JOAJCheckBox(b.getString("kReverseWhenY"));
		line2.add(reverseY);
		middlePanel.add(line2);

		// construct the dialog
		upperPanel.add("Center", paramPanel);
		upperPanel.add("South", middlePanel);
		mainPanel.add("Center", new TenPixelBorder(upperPanel, 10, 10, 10, 10));

		// lower panel
		mOKBtn = new JOAJButton(b.getString("kOK"));
		mOKBtn.setActionCommand("ok");
		this.getRootPane().setDefaultButton(mOKBtn);
		mCancelButton = new JOAJButton(b.getString("kCancel"));
		mCancelButton.setActionCommand("cancel");
		JPanel dlgBtnsInset = new JPanel();
		JPanel dlgBtnsPanel = new JPanel();
		dlgBtnsInset.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 1));
		dlgBtnsPanel.setLayout(new GridLayout(1, 4, 15, 1));
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
		mCancelButton.addActionListener(this);

		mainPanel.add(new TenPixelBorder(dlgBtnsInset, 5, 5, 5, 5), "South");
		contents.add("Center", mainPanel);
		this.pack();

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

	public void valueChanged(ListSelectionEvent evt) {
		if (evt.getSource() == mParamList.getJList()) {
			if (mIgnoreVC == true) {
				mIgnoreVC = false;
				return;
			}
			// get the x param
			mSelParam = mParamList.getJList().getSelectedIndex();
			if (mSelParam < 0)
				return;
			String selParamText = (String) mParamList.getJList().getSelectedValue();

			// make sure value of the param is not missing
			int yerrLine = -1;
			double tempYMin = mFileViewer.mAllProperties[mSelParam].getPlotMin();
			double tempYMax = mFileViewer.mAllProperties[mSelParam].getPlotMax();
			String units = mFileViewer.mAllProperties[mSelParam].getUnits();
			Triplet newRange = JOAFormulas.GetPrettyRange(tempYMin, tempYMax);
			double yInc = newRange.getVal3();
			if (Double.isNaN(yInc)) {
				yerrLine = mSelParam;
			}

			if (yerrLine >= 0) {
				// disable the y param
				JFrame f = new JFrame("Parameter Values Missing Error");
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(f, "All values for " + selParamText + " are missing. " + "\n"
				    + "Select a new parameter");
				mParamList.clearSelection();
				mSelParam = 0;
				mNewParamUnits.setText("");
			}
			else {
				generateParamName();
				mNewParamUnits.setText(units);
			}
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JOAJRadioButton) {
			JOAJRadioButton rb = (JOAJRadioButton) evt.getSource();
			if (evt.getStateChange() == ItemEvent.SELECTED && rb == lnx) {
				mOpText = "ln";
				mOperand = JOAConstants.LN_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == logx) {
				mOpText = "log";
				mOperand = JOAConstants.LOG_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == xsqrd) {
				mOpText = "sqrd";
				mOperand = JOAConstants.XSQRD_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == sinx) {
				mOpText = "sin";
				mOperand = JOAConstants.SIN_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == recipx) {
				mOpText = "1/";
				mOperand = JOAConstants.RECIP_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == ex) {
				mOpText = "exp";
				mOperand = JOAConstants.EXP_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == tenx) {
				mOpText = "10^";
				mOperand = JOAConstants.TENX_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == sqrootx) {
				mOpText = "sqrt";
				mOperand = JOAConstants.SQRT_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == cosx) {
				mOpText = "cos";
				mOperand = JOAConstants.COS_OP;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == ptoz) {
				mOpText = "toZ";
				mOperand = JOAConstants.PTOZ;
			}
			else if (evt.getStateChange() == ItemEvent.SELECTED && rb == ztop) {
				mOpText = "toP";
				mOperand = JOAConstants.ZTOP;
			}
			generateParamName();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			timer.cancel();
			this.dispose();
		}
		else if (cmd.equals("ok")) {
			// transfer pending calculations to the fileviewer for processing
			String newParam = mNewParamName.getText();
			if (newParam.length() == 0)
				newParam = "UNTL";
			// else
			// newParam = JOAFormulas.formatParamName(newParam);
			mNewParamName.setText(newParam);

			String units = mNewParamUnits.getText();
			if (units.length() == 0)
				units = "none";

			String xVarText = null;
			xVarText = (String) mParamList.getJList().getSelectedValue();

			String newCalcName = null;
			newCalcName = new String(newParam + " = " + mOpText + "(" + xVarText + ")");

			// create a new calculation object
			CustomCalculation cc = new CustomCalculation(newCalcName, newParam, units, (String) mParamList.getJList()
			    .getSelectedValue(), "", mOperand, -99.0, -99.0, reverseY.isSelected());
			cc.setIsTransform();
			mFileViewer.addCustomCalculation(cc);
			mFileViewer.doCustomCalcs();
			timer.cancel();
			this.dispose();
		}
	}

	public void generateParamName() {
		String param = (String) mParamList.getJList().getSelectedValue();
		String nameString = new String(mOpText + param);
		mNewParamName.setText(nameString);
	}

	public void maintainButtons() {
		if (mParamList.getJList().getSelectedIndex() >= 0) {
			mOKBtn.setEnabled(true);
		}
		else {
			mOKBtn.setEnabled(false);
		}
	}
}

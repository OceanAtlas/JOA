/*
 * $Id: CurrentObsPanel.java,v 1.5 2005/08/22 20:44:45 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.classicdatamodel.*;

@SuppressWarnings("serial")
public class CurrentObsPanel extends JPanel implements ObsChangedListener, ParameterAddedListener,
    StnFilterChangedListener, ActionListener, DialogClient {
	protected Station mStn = null;
	protected Section mSec = null;
	protected FileViewer mFileViewer = null;
	protected HashMap<String, ParamValuePanel> mValuePanels = new HashMap<String, ParamValuePanel>();
	private JPanel[] mDisplayColumns = new JPanel[10];
	boolean mDisplayUnits = false;
	boolean mDisplayQC = false;
	private int mMaxUnitsLen = 4;
	private int mMaxParamLen = 4;
	private int mMaxFractionalPartLen = 4;
	private int mMaxWholePartLen = 4;
	protected JPopupMenu mPopupMenu = null;
	protected Color mColor = Color.black;
	int mSampNoPos = -99;
	int mRawCTDPos = -99;
	int mBottleNumPos = -99;

	public CurrentObsPanel(FileViewer fv) {
		mFileViewer = fv;
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		mSec = of.getCurrSection();
		mStn = (Station) mSec.mStations.currElement();
		this.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
		for (int i=0; i<10; i++) {
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			mDisplayColumns[i] = jp;
		}
		
		setMaxUnitLength();
		setMaxParamLength();
		setMaxFractionLength();
		setRecord(mSec, mStn);
		mFileViewer.addObsChangedListener(this);
		mFileViewer.addParameterAddedListener(this);
		mFileViewer.addStnFilterChangedListener(this);
		this.addMouseListener(new MyMouseHandler());
	}
	
	public int getNextOpenDisplayColumn() {
		for (int i=0; i<10; i++) {
			if (mDisplayColumns[i].getComponents().length <= 10) {
				return i;
			}
		}
		return 9;
}

	public void cleanUp() {
		this.removeAll();
		for (int i=0; i<10; i++) {
			mDisplayColumns[i].removeAll();
		}
		mValuePanels = null;
		mValuePanels = new HashMap<String, ParamValuePanel>();
		setMaxUnitLength();
		setMaxParamLength();
		setMaxFractionLength();
	}

	protected Color getQCColor(Section sech, int qcVal) {
		if (sech.getQCStandard() == JOAConstants.IGOSS_QC_STD) {
			if (qcVal == 1) {
				return Color.green.darker();
			}
			else if (qcVal == 2) {
				return Color.green;
			}
			else if (qcVal == 3) {
				return Color.green;
			}
			else if (qcVal == 4) {
				return Color.red;
			}
			else if (qcVal == 8) {
				return Color.yellow;
			}
			else {
				return Color.gray.darker();
			}
		}
		else if (sech.getQCStandard() == JOAConstants.WOCE_QC_STD) {
			if (qcVal == 2) {
				return Color.green.darker();
			}
			else if (qcVal == 3 || qcVal == 6 || qcVal == 7 || qcVal == 8) {
				return Color.green;
			}
			else if (qcVal == 4) {
				return Color.red;
			}
			else {
				return Color.gray.darker();
			}
		}
		return Color.gray.darker();
	}

	protected String getQCString(Section sech, int qcVal) {
		if (sech.getQCStandard() == JOAConstants.IGOSS_QC_STD) {
			return new String("(" + String.valueOf(qcVal) + "I) ");
		}
		else if (sech.getQCStandard() == JOAConstants.WOCE_QC_STD) { return new String("(" + String.valueOf(qcVal) + "W) "); }
		// no qc standard defined in the file
		return "(ns)";
	}

	public void setRecord(Section inSec, Station inStn) {
		// generate the widgets to actually display a record
		int numParams = inSec.mNumVars;
		Bottle bot = (Bottle) inStn.mBottles.currElement();
		String valStr;
		String[] finalValueStr = new String[numParams];
		int[] maxValStrLength = new int[numParams];

		for (int v = 0; v < numParams; v++) {
			maxValStrLength[v] = 0;
		}

		for (int v = 0; v < numParams; v++) {
			String paramStr = inSec.mProperties[v];
			int pPos = inSec.getVarPos(paramStr, false);
			int prec = mFileViewer.getPropertyPrecision(paramStr);

			if (pPos >= 0/* && bot.mDValues[pPos] != JOAConstants.MISSINGVALUE */) {
				double val = bot.mDValues[pPos];
				valStr = new String(JOAFormulas.formatDouble(val, prec, true));
				if (prec < mMaxFractionalPartLen) {
					// append spaces
					for (int i = 0; i < mMaxFractionalPartLen - prec; i++) {
						valStr += " ";
					}
				}
			}
			else {
				valStr = new String("    ----");
			}

			// add qc if necessary
			if (mDisplayQC) {
				String qcStr = null;
				if (pPos >= 0 && bot.mQualityFlags[pPos] != JOAConstants.MISSINGVALUE) {
					qcStr = getQCString(inSec, bot.mQualityFlags[pPos]);
					mColor = getQCColor(inSec, bot.mQualityFlags[pPos]);
				}
				else {
					qcStr = "(--) ";
					mColor = Color.gray.darker();
				}
				valStr = valStr + qcStr;
			}
			else {
				mColor = Color.black;
			}

			// add units if necessary
			if (mDisplayUnits && mFileViewer.mAllProperties[pPos].getUnits() != null) {
				valStr = valStr + JOAFormulas.returnSpacePaddedString(inSec.mUnits[v], mMaxUnitsLen);
			}
			if (valStr.length() > maxValStrLength[v]) {
				maxValStrLength[v] = valStr.length();
			}

			finalValueStr[v] = valStr;
		}

		for (int v = 0; v < numParams; v++) {
			boolean valueIsEdited = bot.isValueEdited(v);
			boolean qcIsEdited = bot.isQCValueEdited(v);
			int style = Font.PLAIN;
			if (valueIsEdited || qcIsEdited) {
				style = Font.ITALIC;
			}
			String paramStr = inSec.mProperties[v];
			int pPos = inSec.getVarPos(paramStr, false);
			int prec = mFileViewer.getPropertyPrecision(paramStr);

			ParamValuePanel pvp = new ParamValuePanel();
			int diff = (mMaxParamLen + maxValStrLength[v]) - paramStr.length();
			if (diff < 0)
				diff = 0;

			// add qc if necessary
			if (mDisplayQC) {
				if (pPos >= 0 && bot.mQualityFlags[pPos] != JOAConstants.MISSINGVALUE) {
					mColor = getQCColor(inSec, bot.mQualityFlags[pPos]);
				}
				else {
					mColor = Color.gray.darker();
				}
			}
			else {
				mColor = Color.black;
			}

			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + prec);
			pvp.init(paddedParamStr, finalValueStr[v], mColor, 12, style);
			mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
			mValuePanels.put(paramStr, pvp);
		}

		int extrasCounter = 0;

		if (bot.isSampNoUsed()) {
			// add the sample number
			String paramStr = "SAMPNO";
			String paddedParamStr = JOAFormulas
			    .returnSpaceEndPaddedString(paramStr, mMaxParamLen + mMaxFractionalPartLen + 1);
			int sampNum = bot.getSampNo();

			valStr = Integer.toString(sampNum);
			// append spaces
			for (int i = 0; i < mMaxFractionalPartLen + 1; i++) {
				valStr += " ";
			}

			ParamValuePanel pvp = new ParamValuePanel();
			pvp.init(paddedParamStr, valStr, mColor);
			mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
			mValuePanels.put(paramStr, pvp);
		}

		if (bot.isBottleNumUsed()) {
			// add the bottle number
			String paramStr = "BOTTLE#";
			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + mMaxFractionalPartLen + 1);
			int botNum = bot.getBottleNum();
			valStr = Integer.toString(botNum);
			// append spaces
			for (int i = 0; i < mMaxFractionalPartLen + 1; i++) {
				valStr += " ";
			}
			
			ParamValuePanel pvp = new ParamValuePanel();
			pvp.init(paddedParamStr, valStr, mColor);
			mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
			mBottleNumPos = numParams + extrasCounter;
			mValuePanels.put(paramStr, pvp);
		}

		if (bot.isRawCTDMeasured()) {
			String paramStr = "CTDRAW";
			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + 1);
			float ctdRaw = bot.getRawCTD();
			int prec = 1;
			valStr = new String(JOAFormulas.formatDouble(ctdRaw, prec, true));
			// append spaces
			for (int i = 0; i < mMaxFractionalPartLen - prec; i++) {
				valStr += " ";
			}

			ParamValuePanel pvp = new ParamValuePanel();
			pvp.init(paddedParamStr, valStr, mColor);
			mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
			mRawCTDPos = numParams + extrasCounter;
			mValuePanels.put(paramStr, pvp);
		}

		for (int i=0; i<10; i++) {
			this.add(mDisplayColumns[i]);
		}
	}

//	private JPanel getDisplayColumn(int v) {
//		if (v <= 11) {
//			return (mCol1);
//		}
//		else if (v > 11 && v <= 23) {
//			return (mCol2);
//		}
//		else if (v > 23 && v <= 35) {
//			return (mCol3);
//		}
//		else if (v > 35 && v <= 47) {
//			return (mCol4);
//		}
//		else if (v > 47 && v <= 59) {
//			return (mCol5);
//		}
//		else if (v > 59 && v <= 71) {
//			return (mCol6);
//		}
//		else if (v > 71 && v <= 83) {
//			return (mCol7);
//		}
//		else if (v > 83 && v <= 95) {
//			return (mCol8);
//		}
//		else if (v > 95 && v <= 107) {
//			return (mCol9);
//		}
//		else if (v > 107 && v <= 119) { return (mCol10); }
//
//		return null;
//	}
	
	public int getPrecision(double val, int numSigDigits, int defaultPrec) {
		if (numSigDigits == 0) {
			return defaultPrec;
		}
		
		double aval = Math.abs(val);
		if (aval < 1) {
			return defaultPrec;
		}	
		else if (aval >= 1 && aval < 10) {
			return numSigDigits - 1 > 0 ? numSigDigits - 1 : defaultPrec;
		}	
		else if (aval >= 10 && aval < 100) {
			return numSigDigits - 2 > 0 ? numSigDigits - 2 : defaultPrec;
		}
		else if (aval >= 100 && aval < 1000) {
			return numSigDigits - 3 > 0 ? numSigDigits - 3 : defaultPrec;
		}
		else if (aval >= 1000 && aval < 10000) {
			return numSigDigits - 4 > 0 ? numSigDigits - 4 : defaultPrec;
		}
		return defaultPrec;
	}

	public void updateRecord(Section inSec, Station inStn) {
		int numParams = inSec.mNumVars;
		Bottle bot = (Bottle) inStn.mBottles.currElement();
		String valStr;
		
		for (ParamValuePanel pvp : mValuePanels.values()) {
			pvp.setNewValue("    ----");
		}
		
		String[] finalValueStr = new String[numParams];

		for (int v = 0; v < numParams; v++) {
			String paramStr = inSec.mProperties[v];
			boolean respectSigDigits = mFileViewer.isRespectSignificantDigits(paramStr);
			int prec = mFileViewer.getPropertyPrecision(paramStr);
			int sigDigs = mFileViewer.getSignificantDigits(paramStr);

			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + prec);
			int pPos = inSec.getVarPos(paramStr, false);
			boolean valueIsEdited = bot.isValueEdited(v);
			boolean qcIsEdited = bot.isQCValueEdited(v);
			int style = Font.PLAIN;
			if (valueIsEdited || qcIsEdited) {
				style = Font.ITALIC;
			}

			if (pPos >= 0/* && bot.mDValues[pPos] != JOAConstants.MISSINGVALUE */) {
				double val = bot.mDValues[pPos];
				if (respectSigDigits) {
					int newPrec = getPrecision(val, sigDigs, prec);
					valStr = new String(JOAFormulas.formatDouble(val, newPrec, true));
					paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + newPrec);
					if (newPrec < mMaxFractionalPartLen) {
						// append spaces
						for (int i = 0; i < mMaxFractionalPartLen - newPrec; i++) {
							valStr += " ";
						}
					}
				}
				else {
					valStr = new String(JOAFormulas.formatDouble(val, prec, true));
					if (prec < mMaxFractionalPartLen) {
						// append spaces
						for (int i = 0; i < mMaxFractionalPartLen - prec; i++) {
							valStr += " ";
						}
					}
				}
				
			}
			else {
				valStr = new String("    ----");
			}

			// add qc if necessary
			if (mDisplayQC) {
				String qcStr = null;
				if (pPos >= 0 && bot.mQualityFlags[pPos] != JOAConstants.MISSINGVALUE) {
					qcStr = getQCString(inSec, bot.mQualityFlags[pPos]);
					mColor = getQCColor(inSec, bot.mQualityFlags[pPos]);
				}
				else {
					qcStr = "(--) ";
					mColor = Color.gray.darker();
				}
				valStr = valStr + qcStr;
			}
			else {
				mColor = Color.black;
			}

			// add units if necessary
			if (mDisplayUnits && mFileViewer.mAllProperties[pPos] != null && mFileViewer.mAllProperties[pPos].getUnits() != null) {
				valStr = valStr + JOAFormulas.returnSpacePaddedString(inSec.mUnits[v], mMaxUnitsLen);
			}
			ParamValuePanel pvp = null;
			try {
				pvp = mValuePanels.get(paramStr);
				pvp.setFGColor(mColor);
				pvp.setNewValue(valStr);
				pvp.setNewLabel(paddedParamStr);
				pvp.setStyle(style);
			}
			catch (Exception ex) {
				// need to add a value panel for this parameter
				pvp = new ParamValuePanel();
				pvp.init(paramStr, valStr, mColor);
				mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
				mValuePanels.put(paramStr, pvp);
			}
			pvp.invalidate();
		}

		int extrasCounter = 0;
		
		if (bot.isSampNoUsed()) {
			// add the sample number
			String paramStr = "SAMPNO";
			String paddedParamStr = JOAFormulas
			    .returnSpaceEndPaddedString(paramStr, mMaxParamLen + mMaxFractionalPartLen + 1);
			int sampNum = bot.getSampNo();

			valStr = Integer.toString(sampNum);
			// append spaces
			for (int i = 0; i < mMaxFractionalPartLen + 1; i++) {
				valStr += " ";
			}

			ParamValuePanel pvp = null;
			try {
				pvp = (ParamValuePanel) mValuePanels.get(paramStr);
				pvp.setFGColor(mColor);
				pvp.setNewValue(valStr);
				pvp.setNewLabel(paddedParamStr);
			}
			catch (Exception ex) {		
				// need to add a value panel for this parameter
				pvp = new ParamValuePanel();
				pvp.init(paramStr, valStr, mColor);
				mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
				mValuePanels.put(paramStr, pvp);
			}
			pvp.invalidate();
		}

		if (bot.isBottleNumUsed()) {
			// add the bottle number
			String paramStr = "BOTTLE#";
			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + mMaxFractionalPartLen + 1);
			int botNum = bot.getBottleNum();
			valStr = Integer.toString(botNum);
			// append spaces
			for (int i = 0; i < mMaxFractionalPartLen + 1; i++) {
				valStr += " ";
			}

			ParamValuePanel pvp = null;
			try {
				pvp = (ParamValuePanel) mValuePanels.get(paramStr);
				pvp.setFGColor(mColor);
				pvp.setNewValue(valStr);
				pvp.setNewLabel(paddedParamStr);
			}
			catch (Exception ex) {
				// need to add a value panel for this parameter
				pvp = new ParamValuePanel();
				pvp.init(paramStr, valStr, mColor);
				mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
				mValuePanels.put(paramStr, pvp);
			}
			pvp.invalidate();
		}

		if (bot.isRawCTDMeasured()) {
			String paramStr = "CTDRAW";
			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen + 1);
			float ctdRaw = bot.getRawCTD();
			int prec = 1;
			valStr = new String(JOAFormulas.formatDouble(ctdRaw, prec, true));
			// append spaces
			for (int i = 0; i < mMaxFractionalPartLen - prec; i++) {
				valStr += " ";
			}

			ParamValuePanel pvp = null;
			try {
				 pvp = (ParamValuePanel) mValuePanels.get(paramStr);
				pvp.setFGColor(mColor);
				pvp.setNewValue(valStr);
				pvp.setNewLabel(paddedParamStr);
			}
			catch (Exception ex) {
				// need to add a value panel for this parameter
				 pvp = new ParamValuePanel();
				pvp.init(paramStr, valStr, mColor);
				mDisplayColumns[getNextOpenDisplayColumn()].add(pvp);
				mValuePanels.put(paramStr, pvp);
			}
			pvp.invalidate();
		}

//		if (numParams + extrasCounter < mValuePanels.size()) {
//			// blank unused panels not used in this section
//			for (int v = numParams; v < mValuePanels.size(); v++) {
//				ParamValuePanel pvp = (ParamValuePanel) mValuePanels.get(v);
//				pvp.setNewValue(" ");
//				pvp.setNewLabel(" ");
//				pvp.invalidate();
//			}
//		}
	}

	/*
	 * public void addNewParam(Section inSec, Station inStn) { String valStr;
	 * Bottle bot = (Bottle)inStn.mBottles.currElement(); double val =
	 * bot.mDValues[inSec.mNumVars-1]; if (val != JOAConstants.MISSINGVALUE) {
	 * valStr = new String(JOAFormulas.formatDouble(String.valueOf(val), 3,
	 * true)); } else { valStr = new String(" ----"); } // add units if necessary
	 * if (mDisplayUnits) { valStr = valStr + " " +
	 * JOAFormulas.returnSpacePaddedString(mFileViewer.mAllProperties[pPos].getUnits(),
	 * mMaxUnitsLen); }
	 * 
	 * String paramStr = inSec.mProperties[inSec.mNumVars-1]; ParamValuePanel pvp =
	 * new ParamValuePanel(); pvp.init(paramStr, valStr); this.add(pvp);
	 * mValuePanels.addElement(pvp); pvp.invalidate(); }
	 */

	public void obsChanged(ObsChangedEvent evt) {
		// display the current station
		Station sh = evt.getFoundStation();
		Section sech = evt.getFoundSection();
		updateRecord(sech, sh);
	}

	public void parameterAdded(ParameterAddedEvent evt) {
		// redisplay the current station
		Station sh = evt.getFoundStation();
		Section sech = evt.getFoundSection();
		cleanUp();
		setRecord(sech, sh);
		updateRecord(sech, sh);
	}
	
	public void forceUpdate() {
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech = of.getCurrSection();
		Station sh = (Station) sech.mStations.currElement();
		cleanUp();
		setRecord(sech, sh);
		updateRecord(sech, sh);
	}

	public void stnFilterChanged(StnFilterChangedEvent evt) {
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech = of.getCurrSection();
		Station sh = (Station) sech.mStations.currElement();
		updateRecord(sech, sh);
	}

	private void setMaxUnitLength() {
		mMaxUnitsLen = 0;
		for (int v = 0; v < mFileViewer.gNumProperties; v++) {
			if (mFileViewer.mAllProperties[v].getUnits() != null) {
				if (mFileViewer.mAllProperties[v].getUnits().length() > mMaxUnitsLen) {
					mMaxUnitsLen = mFileViewer.mAllProperties[v].getUnits().length();
				}
			}
		}
	}

	private void setMaxParamLength() {
		mMaxParamLen = 0;
		for (int v = 0; v < mFileViewer.gNumProperties; v++) {
			if (mFileViewer.mAllProperties[v].getVarLabel() != null) {
				if (mFileViewer.mAllProperties[v].getVarLabel().length() > mMaxParamLen) {
					mMaxParamLen = mFileViewer.mAllProperties[v].getVarLabel().length();
				}
			}
		}
	}

	private void setMaxFractionLength() {
		mMaxFractionalPartLen = 0;
		for (int v = 0; v < mFileViewer.gNumProperties; v++) {
			if (mFileViewer.mAllProperties[v].getVarLabel() != null) {
				if (mFileViewer.mAllProperties[v].getDisplayPrecision() > mMaxFractionalPartLen) {
					mMaxFractionalPartLen = mFileViewer.mAllProperties[v].getDisplayPrecision();
				}
			}
		}
	}

	public class MyMouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent me) {
			if (me.getClickCount() == 2) {
				// show configuration dialog
				showConfigDialog();
			}
		}

		public void mouseReleased(MouseEvent me) {
			super.mouseReleased(me);
			if (me.isPopupTrigger()) {
				createPopup(me.getPoint());
			}
		}

		public void mousePressed(MouseEvent me) {
			super.mousePressed(me);
			if (me.isPopupTrigger()) {
				createPopup(me.getPoint());
			}
		}
	}

	public void showConfigDialog() {
		// show configuration dialog
		ConfigCurrentObsPanel cp = new ConfigCurrentObsPanel(mFileViewer, this, mDisplayUnits, mDisplayQC);
		cp.pack();

		// show dialog at center of screen
		Rectangle dBounds = cp.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width / 2 - dBounds.width / 2;
		int y = sd.height / 2 - dBounds.height / 2;
		cp.setLocation(x, y);
		cp.setVisible(true);
	}

	public void createPopup(Point point) {
		ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
		mPopupMenu = new JPopupMenu();
		JMenuItem openContextualMenu = new JMenuItem(b.getString("kProperties"));
		openContextualMenu.setActionCommand("opencontextual");
		openContextualMenu.addActionListener(this);
		mPopupMenu.add(openContextualMenu);
		mPopupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		mPopupMenu.show(this, point.x, point.y);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("opencontextual")) {
			showConfigDialog();
		}
	}

	// OK Button
	public void dialogDismissed(JDialog d) {
		mDisplayUnits = ((ConfigCurrentObsPanel) d).getDisplayUnits();
		mDisplayQC = ((ConfigCurrentObsPanel) d).getDisplayQC();
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech  = of.getCurrSection();
		Station sh = (Station) sech.mStations.currElement();
		updateRecord(sech, sh);
	}

	// Cancel button
	public void dialogCancelled(JDialog d) {
		mDisplayUnits = ((ConfigCurrentObsPanel) d).getDisplayUnits();
		mDisplayQC = ((ConfigCurrentObsPanel) d).getDisplayQC();
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech = of.getCurrSection();
		Station sh = (Station) sech.mStations.currElement();
		updateRecord(sech, sh);
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog d) {
		mDisplayUnits = ((ConfigCurrentObsPanel) d).getDisplayUnits();
		mDisplayQC = ((ConfigCurrentObsPanel) d).getDisplayQC();
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech  = of.getCurrSection();
		Station sh = (Station) sech.mStations.currElement();
		updateRecord(sech, sh);
	}

	// Applxy button, OK w/o dismissing the dialog
	public void dialogApply(JDialog d) {
		mDisplayUnits = ((ConfigCurrentObsPanel) d).getDisplayUnits();
		mDisplayQC = ((ConfigCurrentObsPanel) d).getDisplayQC();
		OpenDataFile of = (OpenDataFile) mFileViewer.mOpenFiles.currElement();
		Section sech = of.getCurrSection();
		Station sh = (Station) sech.mStations.currElement();
		updateRecord(sech, sh);
	}

	public void dialogApplyTwo(Object d) {
	}
}

/*
 * $Id: CurrentObsPanel2.java,v 1.4 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.events.*;

@SuppressWarnings("serial")
public class CurrentObsPanel2 extends JPanel implements ObsChangedListener, ParameterAddedListener, 
	StnFilterChangedListener, ActionListener, DialogClient {
	protected Station mStn = null;
	protected Section mSec = null;
	protected FileViewer mFileViewer = null;
	protected Vector<ParamValuePanel> mValuePanels = new Vector<ParamValuePanel>();
	protected JPanel[] mColumns = new JPanel[40];
	boolean mDisplayUnits = false;
	boolean mDisplayQC = false;
	private int mMaxUnitsLen = 4;
	private int mMaxParamLen = 4;
	protected JPopupMenu mPopupMenu = null;
	protected Color mColor = Color.black;
	protected JOAWindow mWind;
	protected boolean mIsLocked = false;
	
	public CurrentObsPanel2(FileViewer fv, JOAWindow wind) {
		mFileViewer = fv;
		mWind = wind;
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		mSec = (Section)of.getCurrSection();
		mStn = (Station)mSec.mStations.currElement();
		this.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP, 0));
		for (int i=0; i<40; i++) {
			mColumns[i] = new JPanel();
			mColumns[i].setLayout(new BoxLayout(mColumns[i], BoxLayout.Y_AXIS));
		}
		
		setMaxUnitLength();
		setMaxParamLength();
		setRecord(mSec, mStn);
		mFileViewer.addObsChangedListener(this);
		mFileViewer.addParameterAddedListener(this);
		mFileViewer.addStnFilterChangedListener(this);
		this.addMouseListener(new MyMouseHandler());
		this.setBackground(Color.white);
		/*mCol1.setBackground(Color.white);
		mCol2.setBackground(Color.white);
		mCol3.setBackground(Color.white);
		mCol4.setBackground(Color.white);
		mCol5.setBackground(Color.white);*/
	}
		
	public boolean isFocusTraversable() {
		return false;
	} 
	
	public void cleanUp() {
		this.removeAll();
		for (int i=0; i<40; i++)
			mColumns[i].removeAll();
		mValuePanels = null;
		mValuePanels = new Vector<ParamValuePanel>();
		setMaxUnitLength();
		setMaxParamLength();
	}
	
	public void setLocked(boolean b) {
		mIsLocked = b;
	}
	
	public boolean isLocked() {
		return mIsLocked;
	}
	
	protected Color getQCColor(Section sech, int qcVal) {
		if (sech.getQCStandard() == JOAConstants.IGOSS_QC_STD) {
			if (qcVal == 1)
				return Color.green.darker();
			else if (qcVal == 2)
				return Color.green;
			else if (qcVal == 3)
				return Color.green;
			else if (qcVal == 4)
				return Color.red;
			else if (qcVal == 8)
				return Color.yellow;
			else 
				return Color.gray.darker();
		}
		else if (sech.getQCStandard() == JOAConstants.WOCE_QC_STD) {
			if (qcVal == 2)
				return Color.green.darker();
			else if (qcVal == 3 || qcVal == 6 || qcVal == 7 || qcVal == 8)
				return Color.green;
			else if (qcVal == 4)
				return Color.red;
			else 
				return Color.gray.darker();
		}
		return Color.gray.darker();
	}
	
	protected String getQCString(Section sech, int qcVal) {
		if (sech.getQCStandard() == JOAConstants.IGOSS_QC_STD) {
			return new String("(" + String.valueOf(qcVal) + "I) ");
		}
		else if (sech.getQCStandard() == JOAConstants.WOCE_QC_STD) {
			return new String("(" + String.valueOf(qcVal) + "W) ");
		}
		return "(ns)";
	}

	public void setRecord(Section inSec, Station inStn) {
		// generate the widgets to actually display of record
		int numParams = inSec.mNumVars;
		Bottle bot = (Bottle)inStn.mBottles.currElement();
		String valStr;
		
		int col = 0;
		for (int v=0; v<numParams; v++) {
			String paramStr = inSec.mProperties[v];
			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen);
			int pPos = mFileViewer.getPropertyPos(paramStr, false);
			if (bot.mDValues[v] != JOAConstants.MISSINGVALUE) {
				double val = bot.mDValues[v];
				valStr = new String(JOAFormulas.formatDouble(val, 3, true));
			}
			else {
				valStr = new String("    ----");
			}

			// add qc if necessary
			if (mDisplayQC) {
				String qcStr = null;
				if (bot.mQualityFlags[v] != JOAConstants.MISSINGVALUE) {
					qcStr = getQCString(inSec, bot.mQualityFlags[v]);
					mColor = getQCColor(inSec, bot.mQualityFlags[v]);
				}
				else {
					qcStr = "(--) ";
					mColor = Color.gray.darker();
				}
				valStr = valStr + qcStr;
			}
			else
				mColor = Color.black;
			
			// add units if necessary
			if (mDisplayUnits && mFileViewer.mAllProperties[pPos].getUnits() != null) {
				valStr = valStr + JOAFormulas.returnSpacePaddedString(mFileViewer.mAllProperties[pPos].getUnits(), mMaxUnitsLen);
			}
			
			ParamValuePanel pvp = new ParamValuePanel();
			pvp.setBackground(Color.white);
			pvp.init(paddedParamStr, valStr, mColor, 10, Font.PLAIN);
			if (v % 3 == 0) 
				col++;
			mColumns[col].add(pvp);
			mValuePanels.addElement(pvp);
		}
		for (int i=0; i<40; i++) 
			this.add(mColumns[i]);
	}
	
	public void updateRecord(Section inSec, Station inStn) {
		int numParams = inSec.mNumVars;
		Bottle bot = (Bottle)inStn.mBottles.currElement();
		String valStr;
		int col = 0;
		for (int v=0; v<numParams; v++) {
			String paramStr = inSec.mProperties[v];
			String paddedParamStr = JOAFormulas.returnSpaceEndPaddedString(paramStr, mMaxParamLen);
			int pPos = mFileViewer.getPropertyPos(paramStr, false);
			if (bot.mDValues[v] != JOAConstants.MISSINGVALUE) {
				double val = bot.mDValues[v];
				valStr = new String(JOAFormulas.formatDouble(val, 3, true));
			}
			else {
				valStr = new String("    ----");
			}							

			// add qc if necessary
			if (mDisplayQC) {
				String qcStr = null;
				if (bot.mQualityFlags[v] != JOAConstants.MISSINGVALUE) {
					qcStr = getQCString(inSec, bot.mQualityFlags[v]);
					mColor = getQCColor(inSec, bot.mQualityFlags[v]);
				}
				else {
					qcStr = "(--) ";
					mColor = Color.gray.darker();
				}
				valStr = valStr + qcStr;
			}
			else
				mColor = Color.black;
			
			// add units if necessary
			if (mDisplayUnits && mFileViewer.mAllProperties[pPos].getUnits() != null) {
				valStr = valStr + JOAFormulas.returnSpacePaddedString(mFileViewer.mAllProperties[pPos].getUnits(), mMaxUnitsLen);
			}
			
			ParamValuePanel pvp = null;
			try {
				pvp = (ParamValuePanel)mValuePanels.elementAt(v);
				pvp.setFGColor(mColor);
				pvp.setNewValue(valStr);
				pvp.setNewLabel(paddedParamStr);
			}
			catch (ArrayIndexOutOfBoundsException ex) {
				// need to add a value panel for this parameter
				pvp = new ParamValuePanel();
				pvp.init(paramStr, valStr, mColor, 10, Font.PLAIN);
				if (v % 3 == 0)
					col++;
				mColumns[col].add(pvp);
				mValuePanels.addElement(pvp);
			}
			pvp.invalidate();
		}
			
		if (numParams < mValuePanels.size()) {
			// blank unused panels not used in this section
			for (int v=numParams; v<mValuePanels.size(); v++) {
				ParamValuePanel pvp = (ParamValuePanel)mValuePanels.elementAt(v);
				pvp.setNewValue(" ");
				pvp.setNewLabel(" ");
				pvp.invalidate();
			}
		}
	}
	
	/*public void addNewParam(Section inSec, Station inStn) {
		String valStr;
		Bottle bot = (Bottle)inStn.mBottles.currElement();
		double val = bot.mDValues[inSec.mNumVars-1];
		if (val != JOAConstants.MISSINGVALUE) {
			valStr = new String(JOAFormulas.formatDouble(String.valueOf(val), 3, true));
		}
		else {
			valStr = new String("    ----");
		}
			
		// add units if necessary
		if (mDisplayUnits) {
			valStr = valStr + " " + JOAFormulas.returnSpacePaddedString(mFileViewer.mAllProperties[pPos].getUnits(), mMaxUnitsLen);
		}
			
		String paramStr = inSec.mProperties[inSec.mNumVars-1];
		ParamValuePanel pvp = new ParamValuePanel();
		pvp.init(paramStr, valStr);
		this.add(pvp);
		mValuePanels.addElement(pvp);
		pvp.invalidate();
	}*/
	
	public void obsChanged(ObsChangedEvent evt) {
		if (mIsLocked)
			return;
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
		this.invalidate();
		this.validate();
    }	
	
	public void stnFilterChanged(StnFilterChangedEvent evt) {
		if (mIsLocked)
			return;
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		updateRecord(sech, sh);
	}
	
	private void setMaxUnitLength() {
		mMaxUnitsLen = 0;
		for (int v=0; v<mFileViewer.gNumProperties; v++) {
			if (mFileViewer.mAllProperties[v].getUnits() != null) {
				if (mFileViewer.mAllProperties[v].getUnits().length() > mMaxUnitsLen)
					mMaxUnitsLen = mFileViewer.mAllProperties[v].getUnits().length();
			}
		}
	}
	
	private void setMaxParamLength() {
		mMaxParamLen = 0;
		for (int v=0; v<mFileViewer.gNumProperties; v++) {
			if (mFileViewer.mAllProperties[v].getVarLabel() != null) {
				if (mFileViewer.mAllProperties[v].getVarLabel().length() > mMaxParamLen)
					mMaxParamLen = mFileViewer.mAllProperties[v].getVarLabel().length();
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
		ConfigCurrentObsPanel cp = new ConfigCurrentObsPanel(mWind, this, mDisplayUnits, mDisplayQC);
		cp.pack();
		
		// show dialog at center of screen
		Rectangle dBounds = cp.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
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
    	mDisplayUnits = ((ConfigCurrentObsPanel)d).getDisplayUnits();
    	mDisplayQC = ((ConfigCurrentObsPanel)d).getDisplayQC();
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		updateRecord(sech, sh);
    }
    
    // Cancel button
    public void dialogCancelled(JDialog d) {
    	mDisplayUnits = ((ConfigCurrentObsPanel)d).getDisplayUnits();
    	mDisplayQC = ((ConfigCurrentObsPanel)d).getDisplayQC();
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		updateRecord(sech, sh);
    }
    
    // something other than the OK button 
    public void dialogDismissedTwo(JDialog d) {
    	mDisplayUnits = ((ConfigCurrentObsPanel)d).getDisplayUnits();
    	mDisplayQC = ((ConfigCurrentObsPanel)d).getDisplayQC();
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		updateRecord(sech, sh);
    }
    
    // Applxy button, OK w/o dismissing the dialog
    public void dialogApply(JDialog d) {
    	mDisplayUnits = ((ConfigCurrentObsPanel)d).getDisplayUnits();
    	mDisplayQC = ((ConfigCurrentObsPanel)d).getDisplayQC();
		OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		updateRecord(sech, sh);
    }
    
    public void dialogApplyTwo(Object d) {
    }
    
    public int getHeight() {
    	return 100;
    }
}
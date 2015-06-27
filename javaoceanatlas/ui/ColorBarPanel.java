/*
 * $Id: ColorBarPanel.java,v 1.14 2005/09/23 14:51:23 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javaoceanatlas.classicdatamodel.*;
import javaoceanatlas.events.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.PowerOceanAtlas;

@SuppressWarnings("serial")
public class ColorBarPanel extends JPanel implements DialogClient, ActionListener, PrefsChangedListener {
	protected FileViewer mFileViewer;
	protected NewColorBar mColorBar = null;
	protected ObsMarker mObsMarker = null;
	protected boolean mLinked = true;
	public SwatchPanel mSwatchPanel = null;
	protected JPanel mCBCont = null;
	protected DialogClient mDialogClient = null;
	protected boolean mBroadcastChanges = true;
	protected Color mBG;
	protected boolean mLabelPanel = true;
	protected boolean mFancyLabelPanel = false;
	private JPopupMenu mPopupMenu = null;
	protected JFrame mParent;
	protected int mOrientation = JOAConstants.VERTICAL_ORIENTATION;
	protected int mWidth;
	protected int mHeight;
	protected int correctY = 0;
	protected int correctBottom = 0;
	protected boolean mIsEnhanceable = false;
	protected boolean mLocked = false;
	protected boolean mColorBarChanged = false;
	protected Color mLineColor = null;
	protected boolean mDisplaySwatchesAsColorLines = false;

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb) {
		this(parent, fv, cb, null, null, true, JOAConstants.VERTICAL_ORIENTATION, false, null);
	}

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb, Color lineColor) {
		this(parent, fv, cb, null, null, true, JOAConstants.VERTICAL_ORIENTATION, false, lineColor);
	}

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb, int orientation) {
		this(parent, fv, cb, null, null, true, orientation, false, null);
	}

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb, Color fgColor, Color bgColor, boolean inLabelPanel) {
		this(parent, fv, cb, fgColor, bgColor, true, JOAConstants.VERTICAL_ORIENTATION, false, null);
	}

	public ColorBarPanel(JOAWindow parent, FileViewer fv, NewColorBar cb, Color fgColor, Color bgColor,
	    boolean inLabelPanel, boolean fancyLabel) {
		this(parent, fv, cb, fgColor, bgColor, true, JOAConstants.VERTICAL_ORIENTATION, fancyLabel, null);
	}

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb, Color fgColor, Color bgColor,
	    boolean inLabelPanel, boolean fancyLabel, Color lineColor) {
		this(parent, fv, cb, fgColor, bgColor, inLabelPanel, JOAConstants.VERTICAL_ORIENTATION, fancyLabel, lineColor);
	}

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb, Color fgColor, Color bgColor,
	    boolean inLabelPanel, int orientation, boolean fancyLabel) {
		this(parent, fv, cb, fgColor, bgColor, inLabelPanel, orientation, fancyLabel, null);
	}

	public ColorBarPanel(JFrame parent, FileViewer fv, NewColorBar cb, Color fgColor, Color bgColor,
	    boolean inLabelPanel, int orientation, boolean fancyLabel, Color lineColor) {
		mParent = parent;
		mLabelPanel = inLabelPanel;
		mFancyLabelPanel = fancyLabel;
		if (bgColor != null) {
			this.setBackground(bgColor);
			mBG = bgColor;
		}
		if (cb == null) { return; }
		mFileViewer = fv;
		mColorBar = cb;
		if (mFileViewer != null && PowerOceanAtlas.getInstance() != null) {
			PowerOceanAtlas.getInstance().addPrefsChangedListener(this);
		}
		mOrientation = orientation;
		this.setLayout(new BorderLayout(0, 0));

		mCBCont = new JPanel();
		if (mBG != null) {
			mCBCont.setBackground(mBG);
		}
		// mCBCont.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER,
		// 0));
		mSwatchPanel = new SwatchPanel();
		// mCBCont.add(mSwatchPanel);
		// this.add(mCBCont, "Center");
		this.add(mSwatchPanel, "Center");
		if (mLabelPanel && mFancyLabelPanel) {
			String panelLabel = null;
			if (mFileViewer != null) {
				int pPos = mFileViewer.getPropertyPos(mColorBar.getParam(), false);
				if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null
				    && mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
					panelLabel = new String(mColorBar.getParam() + " (" + mFileViewer.mAllProperties[pPos].getUnits() + ")");
				}
				else {
					panelLabel = new String(mColorBar.getParam());
				}
			}
			else {
				panelLabel = new String(mColorBar.getParam());
			}
			TitledBorder tb = BorderFactory.createTitledBorder(panelLabel);
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			this.setBorder(tb);
		}
		addMouseListener(new XYMouseHandler());
		mDialogClient = this;

		if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			mWidth = 550;
			mHeight = 50;
		}
		else {
			mWidth = 135;
			mHeight = 250;
		}
		if (mLabelPanel && mFancyLabelPanel) {
			correctY = 20;
		}
		else {
			correctBottom -= 15;
		}
		mLineColor = lineColor;
	}

	public ColorBarPanel(JOAWindow par, FileViewer fv, NewColorBar cb, Color fgColor, Color bgColor) {
		this(par, fv, cb, fgColor, bgColor, true);
	}

	public void setLocked(boolean b) {
		mLocked = b;
	}

	public boolean isLocked(boolean b) {
		return mLocked;
	}

	public void setHeight(int h) {
		mHeight = h;
	}

	public void setWidth(int w) {
		mWidth = w;
		this.setSize(mWidth, mHeight);
		if (mSwatchPanel != null) {
			mSwatchPanel.setSize(mWidth, mHeight);
			invalidate();
		}
	}

	public void invalidateSwatch() {
		if (mSwatchPanel != null) {
			mSwatchPanel.invalidate();
		}
	}

	public void setEnhanceable(boolean flag) {
		mIsEnhanceable = flag;
	}

	public void setNewColorBar(NewColorBar newCB) {
		mColorBar = newCB;
		if (mLabelPanel && mFancyLabelPanel) {
			String newParam = mColorBar.getParam();
			int pPos = mFileViewer.getPropertyPos(newParam, false);
			String panelLabel = null;
			if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null
			    && mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
				panelLabel = new String(mColorBar.getParam() + " (" + mFileViewer.mAllProperties[pPos].getUnits() + ")");
			}
			else {
				panelLabel = new String(mColorBar.getParam());
			}

			TitledBorder tb = BorderFactory.createTitledBorder(panelLabel);
			if (JOAConstants.ISMAC) {
				// tb.setTitleFont(new Font("Helvetica", Font.PLAIN, 11));
			}
			this.setBorder(tb);
		}
		this.invalidate();
		forceRedraw();

		this.setSize(this.getSize().width, this.getSize().height + 1);
		this.setSize(this.getSize().width, this.getSize().height);
	}

	public void setNewBGColor(Color bg) {
		mBG = bg;
		this.setBackground(mBG);
		mCBCont.setBackground(mBG);
	}

	public void setLineColor(Color c) {
		mLineColor = c;
	}

	public Color getLineColor() {
		return mLineColor;
	}

	public void setLabelPanel(boolean mode) {
		mLabelPanel = mode;
	}

	public void setBroadcastMode(boolean inMode) {
		mBroadcastChanges = inMode;
	}

	public Dimension getPreferredSize() {
		return new Dimension(mWidth, mHeight);
	}

	public void setLinked(boolean in) {
		mLinked = in;
	}

	public boolean isLinked() {
		return mLinked;
	}

	// OK Button
	public void dialogDismissed(JDialog f) {
		mColorBarChanged = true;
		mColorBar = ((EditColorbar)f).getColorBar();
		mColorBar.setNumLevels(((EditColorbar)f).getNumLevels());
		mObsMarker = null;
		if (((JOAWindow)mParent).getPanel() != null) {
			((JOAWindow)mParent).getPanel().paintImmediately(new Rectangle(0, 0, 1000, 1000));
		}
		else {
			((JOAWindow)mParent).forceRedraw();
		}

		try {
			mSwatchPanel.setSize(mSwatchPanel.getSize().width, mSwatchPanel.getSize().height + 1);
			mSwatchPanel.setSize(mSwatchPanel.getSize().width, mSwatchPanel.getSize().height - 1);
		}
		catch (Exception ex) {
			mSwatchPanel.setSize(mParent.getSize().width, mParent.getSize().height + 1);
			mSwatchPanel.setSize(mParent.getSize().width, mParent.getSize().height - 1);
		}

		// broadcast an event
		if (mBroadcastChanges) {
			ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
			cbce.setColorBar(mColorBar);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
		}
	}

	// Cancel button
	public void dialogCancelled(JDialog f) {
		// mColorBar = ((EditColorbar)f).getColorBar();
		// mColorBar.mNumColorLevels = ((EditColorbar)f).getNumLevels();
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog f) {
		;
	}

	public boolean isChanged() {
		return mColorBarChanged;
	}

	public void setChanged(boolean b) {
		mColorBarChanged = b;
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog f) {
		mColorBarChanged = true;
		mColorBar = ((EditColorbar)f).getColorBar();
		mColorBar.setNumLevels(((EditColorbar)f).getNumLevels());
		mObsMarker = null;
		if (((JOAWindow)mParent).getPanel() != null) {
			((JOAWindow)mParent).getPanel().paintImmediately(new Rectangle(0, 0, 1000, 1000));
		}
		else {
			((JOAWindow)mParent).forceRedraw();
		}

		try {
			// mSwatchPanel.paintComponent(myG);
			mSwatchPanel.setSize(mSwatchPanel.getSize().width, mSwatchPanel.getSize().height + 1);
			mSwatchPanel.setSize(mSwatchPanel.getSize().width, mSwatchPanel.getSize().height - 1);
		}
		catch (Exception ex) {
			mSwatchPanel.setSize(mParent.getSize().width, mParent.getSize().height + 1);
			mSwatchPanel.setSize(mParent.getSize().width, mParent.getSize().height - 1);
		}

		// broadcast an event
		if (mBroadcastChanges) {
			ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
			cbce.setColorBar(mColorBar);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
		}
	}

	public void dialogApplyTwo(Object d) {
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

	public void showConfigDialog() {
		// show configuration dialog
		EditColorbar ecb = new EditColorbar((JOAWindow)mParent, mFileViewer, mDialogClient, mColorBar, !mColorBar
		    .isMetadataColorBar());
		ecb.pack();
		ecb.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("opencontextual")) {
			showConfigDialog();
		}
	}

	public void forceRedraw() {
		if (mSwatchPanel != null) {
			mSwatchPanel.redraw();
		}
	}

	public class XYMouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent me) {
			if (mLocked) { return; }
			boolean update = false;
			if (me.getClickCount() == 2) {
				showConfigDialog();
			}
			else {
				if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
				else if (mIsEnhanceable) {
					// potentially got a click in a swatch
					// find which swatch the click occurred
					int swatch = mSwatchPanel.getSwatchIndex(me.getPoint());

					if (swatch != -99) {
						// got click in swatch
						boolean shiftDown = me.isShiftDown();
						if (!shiftDown) {
							mColorBar.clearEnhancedColors();
							// just highlight it
							mColorBar.setMinEnhancedColor(swatch);
						}
						else {
							// shift key is down
							int currMin = mColorBar.getMinEnhancedRange();
							int currMax = mColorBar.getMaxEnhancedRange();
							if (currMin == -99) {
								// this is actually the first color--just set the range like a
								// normal click w/o shift
								mColorBar.clearEnhancedColors();
								mColorBar.setMinEnhancedColor(swatch);
							}
							else {
								if (JOAConstants.COLORBARDIRECTION == Direction.UP) {
									// range has already been started
									if (swatch < currMin) {
										if (currMax == -99) {
											// have to reset the top to the previous min
											mColorBar.setMaxEnhancedColor(currMin);
										}
										mColorBar.setMinEnhancedColor(swatch);
									}
									else if (currMax == -99 && swatch > currMin) {
										// initialize the top of the range
										mColorBar.setMaxEnhancedColor(swatch);
									}
									else if (currMax != -99 && swatch > currMax) {
										// reset the top of the range
										mColorBar.setMaxEnhancedColor(swatch);
									}
									else if (currMax != -99 && swatch < currMax) {
										// reset the top of the range
										mColorBar.setMaxEnhancedColor(swatch);
									}
								}
								else {
									if (swatch < currMin) {
										if (currMax == -99) {
											// have to reset the top to the previous min
											mColorBar.setMaxEnhancedColor(currMin);
										}
										mColorBar.setMinEnhancedColor(swatch);
									}
									else if (currMax == -99 && swatch > currMin) {
										// initialize the top of the range
										mColorBar.setMaxEnhancedColor(swatch);
									}
									else if (currMax != -99 && swatch > currMax) {
										// reset the top of the range
										mColorBar.setMaxEnhancedColor(swatch);
									}
									else if (currMax != -99 && swatch < currMax) {
										// reset the top of the range
										mColorBar.setMaxEnhancedColor(swatch);
									}
								}
							}
						}
						update = true;
					}
					else {
						// clear the highlight selection
						update = true;
						mColorBar.clearEnhancedColors();
					}

					if (update) {
						// redraw the colorbar
						ColorBarChangedEvent cbce = new ColorBarChangedEvent(mFileViewer);
						cbce.setColorBar(mColorBar);
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(cbce);
						mSwatchPanel.redraw();
					}
				}
			}
		}

		public void mouseReleased(MouseEvent me) {
			super.mouseReleased(me);
			if (mLocked) { return; }
			if (me.isPopupTrigger()) {
				createPopup(me.getPoint());
			}
		}

		public void mousePressed(MouseEvent me) {
			super.mousePressed(me);
			if (mLocked) { return; }
			if (me.isPopupTrigger()) {
				createPopup(me.getPoint());
			}
		}
	}

	public class SwatchPanel extends JPanel implements ObsChangedListener {
		private Image mOffScreen = null;

		public SwatchPanel() {
			if (mFileViewer != null) {
				mFileViewer.addObsChangedListener(this);
			}
			if (mBG != null) {
				this.setBackground(mBG);
			}
		}

		public void invalidate() {
			try {
				super.invalidate();
				if (mOffScreen != null) {
					mOffScreen = null;
				}
				if (mObsMarker != null) {
					mObsMarker = null;
				}
			}
			catch (Exception e) {

			}
		}

		public void paintComponent(Graphics gin) {
			Graphics2D g = (Graphics2D)gin;
			if (mOffScreen == null) {
				mOffScreen = createImage(getSize().width, getSize().height);
				Graphics2D og = (Graphics2D)mOffScreen.getGraphics();
				super.paintComponent(og);
				if (mLineColor != null) {
					drawColorLines(og);
				}
				else {
					drawColorBar(og);
				}

				g.drawImage(mOffScreen, 0, 0, null);
				og.dispose();
			}
			else {
				g.drawImage(mOffScreen, 0, 0, null);
			}

			if (mLinked && mObsMarker != null) {
				mObsMarker.drawMarker(g, false);
			}
		}

		public void redraw() {
			invalidate();
			// Graphics g = this.getGraphics();
			// paintComponent(g);
		}

		public int getSwatchIndex(Point p) {
			int numColors = mColorBar.getNumLevels();
			Dimension d = this.getSize();
			int left = 0;
			int right = d.width;
			int top = 15;
			int bottom = d.height;
			bottom += correctBottom;

			int pixelsPerBand = (bottom - top - 2) / numColors;
			int bandTop = 0;
			int bandBottom = 0;
			int bandLeft = 0;
			int bandRight = 0;
			Point pp = new Point();
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				left = 20;
				top = 0;
				bottom = d.height - 30;
				right -= 20;
				pixelsPerBand = (right - left - 5) / numColors;
			}

			int maxH = 0;
			Rectangle rect = new Rectangle();
			for (int i = 0; i < numColors; i++) {
				// swatch
				if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
					bandLeft = (int)(left + i * pixelsPerBand);
					bandRight = (int)(bandLeft + pixelsPerBand);
					maxH = bandRight > maxH ? bandRight : maxH;
					rect.setBounds(bandLeft, top, bandRight - bandLeft, bottom);
				}
				else {
					if (JOAConstants.COLORBARDIRECTION == Direction.DOWN) {
						bandTop = (int)(top + (i) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
						rect.setBounds(left + 10, bandTop, left + 25, bandBottom - bandTop);
					}
					else {
						bandTop = (int)(top + (mColorBar.getNumLevels() - i) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
						rect.setBounds(left + 10, bandTop, left + 25, bandBottom - bandTop);
					}
				}
				pp.setLocation(p.x, p.y - correctY);
				if (rect.contains(pp)) { return i; }
			}

			return -99;
		}

		@SuppressWarnings("deprecation")
		public void obsChanged(ObsChangedEvent evt) {
			if (!mLinked) { return; }
			// display the current observation
			int numColors = mColorBar.getNumLevels();
			Dimension d = this.getSize();
			int left = 0;
			int top = 15;
			int bottom = d.height;
			if (!(mLabelPanel && mFancyLabelPanel)) {
				bottom -= 15;
			}
			int pixelsPerBand = (bottom - top - 2) / numColors;
			int bandTop = 0;
			int bandBottom = 0;

			// do the same for horizontal orientation

			OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
			Section sech = of.getCurrSection();
			Station sh = (Station)sech.mStations.currElement();
			Bottle bh = (Bottle)sh.mBottles.currElement();
			double val = 0.0;
			if (!mFileViewer.mDefaultCB.isMetadataColorBar()) {
				val = bh.mDValues[mFileViewer.mCurrentColorVariable];
			}
			else {
				// get the value of the metadatatype for this station
				ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
				if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
					val = sh.getDate().getTime();
				}
				else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTimeMonth"))) {
					val = sh.getDate().getMonth() - 1;
				}
				else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"))) {
					val = sh.getLat();
				}
				else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"))) {
					val = sh.getLon();
				}
			}
			int index = mColorBar.getColorIndex(val);
			if (mObsMarker != null && val != JOAConstants.MISSINGVALUE && index != JOAConstants.MISSINGVALUE) {
				int xx = left + 5;
				if (JOAConstants.COLORBARDIRECTION == Direction.DOWN) {
					bandTop = (int)(top + (index) * pixelsPerBand);
					bandBottom = (int)(bandTop + pixelsPerBand);
				}
				else {
					bandTop = (int)(top + (mColorBar.getNumLevels() - index) * pixelsPerBand);
					bandBottom = (int)(bandTop + pixelsPerBand);
				}
				int yy = bandTop + (bandBottom - bandTop) / 2;
				mObsMarker.setNewPos(xx, yy - correctY);
				paintImmediately(0, 0, 1000, 1000);
			}
		}

		@SuppressWarnings("deprecation")
		public void drawColorLines(Graphics2D g) {
			int numColors = mColorBar.getNumLevels();
			Dimension d = this.getSize();
			int left = 0;
			int right = d.width;
			int top = 15;
			int bottom = d.height;
			if (!(mLabelPanel && mFancyLabelPanel)) {
				bottom -= 15;
			}
			int pixelsPerBand = (bottom - top - 2) / numColors;
			int bandTop = 0;
			int bandBottom = 0;
			int bandLeft = 0;
			int bandRight = 0;

			g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
			    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
			FontMetrics fm = g.getFontMetrics();
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				left = 20;
				top = 0;
				bottom = d.height - 30;
				right -= 20;
				pixelsPerBand = (right - left - 5) / numColors;
			}

			// draw the color ramp and labels
			double base = mColorBar.getBaseLevel();
			double end = mColorBar.getEndLevel();
			double diff = Math.abs(end - base);
			int numPlaces = 2;
			if (diff < 10) {
				numPlaces = 3;
			}
			else if (diff >= 10 && diff < 100) {
				numPlaces = 2;
			}
			else if (diff >= 100 && diff < 1000) {
				numPlaces = 1;
			}
			else if (diff >= 1000) {
				numPlaces = 1;
			}

			int labelInc = 0;
			if (numColors <= 16) {
				labelInc = 1;
			}
			else if (numColors > 16 && numColors <= 32) {
				labelInc = 2;
			}
			else if (numColors > 32 && numColors <= 48) {
				labelInc = 3;
			}
			else if (numColors > 48 && numColors <= 64) {
				labelInc = 4;
			}
			else if (numColors > 64) {
				labelInc = 5;
			}

			// find the length of the longest label
			int maxWidth = 0;
			int labelHInc = 1;
			boolean offsetLabels = false;
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				for (int i = 0; i < numColors; i++) {
					String sTemp = null;
					if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
						sTemp = mColorBar.getFormattedValue(i) + "m";
					}
					else {
						sTemp = mColorBar.getFormattedValue(i);
					}
					int len = fm.stringWidth(sTemp);
					maxWidth = len > maxWidth ? len : maxWidth;
				}

				// find the number of labels that will fit
				if (maxWidth < pixelsPerBand - 10) {
					// all labels will fit w/o offset or dropping labels
					offsetLabels = false;
				}
				else if ((maxWidth < (2 * pixelsPerBand) - 10) && numColors >= 16) {
					// all labels will fit with offset
					offsetLabels = true;
				}
				else {
					// compute how many labels to skip
					int numFit = (numColors * pixelsPerBand) / (maxWidth + 5);
					labelHInc = (int)(Math.round((double)numColors / (double)numFit));
				}
			}

			int maxH = 0;
			int maxLabelH = 0;
			boolean drawSelection = false;
			int selTop = 0;
			int selBottom = 0;
			for (int i = 0; i < numColors; i++) {
				if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
					bandLeft = (int)(left + i * pixelsPerBand);
					bandRight = (int)(bandLeft + pixelsPerBand);
					maxH = bandRight > maxH ? bandRight : maxH;

					if (mLineColor instanceof Color) {
						g.setColor((Color)mLineColor);
					}
					else {
						g.setColor(mColorBar.getColorValue(i));
					}

					int l = left + 10;
					int r = left + 25;
					int h = l + (r - l) / 2;
					g.drawLine(l, bandBottom, h, bandTop);
					g.drawLine(h, bandTop, r, bandBottom);

					// draw a tic mark and labels
					g.setColor(Color.black);
					if (offsetLabels && i > 0 && i % 2 == 0) {
						String sTemp = mColorBar.getFormattedValue(i - 1) + "m";
						// g.drawLine(bandLeft, bottom, bandLeft, bottom + 15);
						g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 30);
					}
					else if (offsetLabels && i > 0) {
						String sTemp = mColorBar.getFormattedValue(i - 1) + "m";
						// g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
						g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
					}
					else if (i > 0) {
						String sTemp = mColorBar.getFormattedValue(i - 1) + "m";
						if (i % labelHInc == 0) {
							// g.drawLine(bandLeft, bottom, bandLeft, bottom + 10);
							g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
							maxLabelH = bandLeft + (fm.stringWidth(sTemp) / 2);
						}
						else {
							; // g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
						}
					}
				}
				else {
					if (JOAConstants.COLORBARDIRECTION == Direction.DOWN) {
						bandTop = (int)(top + (i) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
					}
					else {
						bandTop = (int)(top + (mColorBar.getNumLevels() - i) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
					}

					if (mLineColor instanceof Color) {
						g.setColor((Color)mLineColor);
					}
					else {
						g.setColor(mColorBar.getColorValue(i));
					}

					int l = left + 10;
					int r = left + 25;
					int h = l + (r - l) / 2;
					g.drawLine(l, bandBottom, h, bandTop);
					g.drawLine(h, bandTop, r, bandBottom);

					if (mColorBar.isColorEnhanced(i)) {
						if (i == mColorBar.getMinEnhancedRange()) {
							selTop = bandTop;
							selBottom = bandBottom;
						}
						if (i == mColorBar.getMaxEnhancedRange()) {
							selBottom = bandBottom;
						}
						drawSelection = true;
					}

					// label
					g.setColor(Color.black);
					if (i % labelInc == 0) {
						String sTemp = mColorBar.getFormattedValue(i, numPlaces, true);
						// String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal),
						// numPlaces, true);
						g.drawString(sTemp, left + 35, bandBottom);
					}
				}
			}

			if (drawSelection && mIsEnhanceable) {
				g.setColor(Color.black);
				g.drawRect(left + 10, selTop, left + 24, selBottom - selTop - 1);
			}

			// plot the last horizontal label
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION && offsetLabels) {
				if (numColors % 2 == 0) {
					double myVal = mColorBar.getDoubleValue(numColors - 1);
					String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
					g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 30);
				}
				else {
					double myVal = mColorBar.getDoubleValue(numColors - 1);
					String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false);
					g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 20);
				}
			}
			else if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				double myVal = mColorBar.getDoubleValue(numColors - 1);
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
				int l = bandRight - (fm.stringWidth(sTemp) / 2);
				if (l > maxLabelH + 5) {
					g.drawString(sTemp, l, bottom + 20);
				}
			}

			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				// outline colorbar
				g.drawRect(left, top, maxH - left, bottom);

				// plot the last tic mark
				if (numColors % 2 == 0 && offsetLabels) {
					g.drawLine(maxH, bottom, maxH, bottom + 15);
				}
				else {
					g.drawLine(maxH, bottom, maxH, bottom + 5);
				}
			}

			if (mLabelPanel && mOrientation != JOAConstants.HORIZONTAL_ORIENTATION && !mFancyLabelPanel) {
				// put the label here
				String panelLabel = null;
				if (mFileViewer != null) {
					int pPos = mFileViewer.getPropertyPos(mColorBar.getParam(), false);
					if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null
					    && mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
						panelLabel = new String(mColorBar.getParam() + " (" + mFileViewer.mAllProperties[pPos].getUnits() + ")");
					}
					else {
						panelLabel = new String(mColorBar.getParam());
					}
				}
				else {
					panelLabel = new String(mColorBar.getParam());
				}
				int strWidth = fm.stringWidth(panelLabel);
				JOAFormulas.drawStyledString(panelLabel, right / 2 - strWidth / 2, bandBottom + 10, fm, (Graphics2D)g);
			}

			// initialize the spot
			if (mObsMarker == null && mFileViewer != null && isLinked()) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
				Section sech = of.getCurrSection();
				Station sh = (Station)sech.mStations.currElement();
				Bottle bh = (Bottle)sh.mBottles.currElement();
				double val = 0.0;
				if (!mFileViewer.mDefaultCB.isMetadataColorBar()) {
					val = bh.mDValues[mFileViewer.mCurrentColorVariable];
				}
				else {
					// get the value of the metadatatype for this station
					ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
					if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
						val = sh.getDate().getTime();
					}
					if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTimeMonth"))) {
						val = sh.getDate().getMonth() - 1;
					}
					else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"))) {
						val = sh.getLat();
					}
					else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"))) {
						val = sh.getLon();
					}
				}
				int index = mColorBar.getColorIndex(val);
				if (index != JOAConstants.MISSINGVALUE) {
					int xx = left + 5;
					if (JOAConstants.COLORBARDIRECTION == Direction.DOWN) {
						bandTop = (int)(top + (index) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
					}
					else {
						bandTop = (int)(top + (mColorBar.getNumLevels() - index) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
					}
					int yy = bandTop + (bandBottom - bandTop) / 2;
					mObsMarker = new ObsMarker(xx, yy - correctY, 10);
					mObsMarker.setSymbol(JOAConstants.SYMBOL_RIGHT_ARROW_SYMBOL);
				}
			}
		}

		@SuppressWarnings("deprecation")
		public void drawColorBar(Graphics2D g) {
			int numColors = mColorBar.getNumLevels();
			if (numColors <= 0)
				numColors = 16;
			Dimension d = this.getSize();
			int left = 0;
			int right = d.width;
			int top = 15;
			int bottom = d.height;
			if (!(mLabelPanel && mFancyLabelPanel)) {
				bottom -= 15;
			}

			int pixelsPerBand = (bottom - top - 2) / numColors;
			int bandTop = 0;
			int bandBottom = 0;
			int bandLeft = 0;
			int bandRight = 0;

			g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
			    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
			FontMetrics fm = g.getFontMetrics();
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				left = 20;
				top = 0;
				bottom = d.height - 30;
				right -= 20;
				pixelsPerBand = (right - left - 5) / numColors;
			}

			// draw the color ramp and labels
			double base = mColorBar.getBaseLevel();
			double end = mColorBar.getEndLevel();
			double diff = Math.abs(end - base);
			int numPlaces = 2;
			if (diff < 10) {
				numPlaces = 3;
			}
			else if (diff >= 10 && diff < 100) {
				numPlaces = 2;
			}
			else if (diff >= 100 && diff < 1000) {
				numPlaces = 1;
			}
			else if (diff >= 1000) {
				numPlaces = 1;
			}

			int labelInc = 0;
			if (numColors <= 16) {
				labelInc = 1;
			}
			else if (numColors > 16 && numColors <= 32) {
				labelInc = 2;
			}
			else if (numColors > 32 && numColors <= 48) {
				labelInc = 3;
			}
			else if (numColors > 48 && numColors <= 64) {
				labelInc = 4;
			}
			else if (numColors > 64) {
				labelInc = 5;
			}

			// find the length of the longest label
			int maxWidth = 0;
			int labelHInc = 1;
			boolean offsetLabels = false;
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				for (int i = 0; i < numColors; i++) {
					String sTemp = null;
					if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
						sTemp = mColorBar.getFormattedValue(i) + "m";
					}
					else {
						sTemp = mColorBar.getFormattedValue(i);
					}
					int len = fm.stringWidth(sTemp);
					maxWidth = len > maxWidth ? len : maxWidth;
				}

				// find the number of labels that will fit
				if (maxWidth < pixelsPerBand - 10) {
					// all labels will fit w/o offset or dropping labels
					offsetLabels = false;
				}
				else if ((maxWidth < (2 * pixelsPerBand) - 10) && numColors >= 16) {
					// all labels will fit with offset
					offsetLabels = true;
				}
				else {
					// compute how many labels to skip
					int numFit = (numColors * pixelsPerBand) / (maxWidth + 5);
					labelHInc = (int)(Math.round((double)numColors / (double)numFit));
				}
			}

			int maxH = 0;
			int maxLabelH = 0;
			boolean drawSelection = false;
			int selTop = 0;
			int selBottom = 0;
			// swatch
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				for (int i = 0; i < numColors; i++) {
					bandLeft = (int)(left + i * pixelsPerBand);
					bandRight = (int)(bandLeft + pixelsPerBand);
					maxH = bandRight > maxH ? bandRight : maxH;
					g.setColor(mColorBar.getColorValue(i));
					g.fillRect(bandLeft, top, bandRight - bandLeft, bottom);

					// draw a tic mark and labels
					g.setColor(Color.black);
					if (offsetLabels && i > 0 && i % 2 == 0) {
						String sTemp = mColorBar.getFormattedValue(i - 1) + "m";
						g.drawLine(bandLeft, bottom, bandLeft, bottom + 15);
						g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 30);
					}
					else if (offsetLabels && i > 0) {
						String sTemp = mColorBar.getFormattedValue(i - 1) + "m";
						g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
						g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
					}
					else if (i > 0) {
						String sTemp = mColorBar.getFormattedValue(i - 1) + "m";
						if (i % labelHInc == 0) {
							g.drawLine(bandLeft, bottom, bandLeft, bottom + 10);
							g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
							maxLabelH = bandLeft + (fm.stringWidth(sTemp) / 2);
						}
						else {
							g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
						}
					}
				}
			}
			else {
				if (JOAConstants.COLORBARDIRECTION == Direction.DOWN) {
					for (int i = 0; i < numColors; i++) {
						bandTop = (int)(top + (i) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
						g.setColor(mColorBar.getColorValue(i));
						g.fillRect(left + 10, bandTop, left + 25, bandBottom - bandTop);
						
//						if (i == 0) {
//							int[] xn = new int[4];
//							int[] yn = new int[4];
//							
//							xn[0] = left + 10;
//							xn[1] = left + 25;
//							xn[2] = ((left + 25) - (left + 10))/2;
//							xn[3] = left + 10;
//							
//							yn[0] = bandTop;
//							yn[1] = bandTop;
//							yn[2] = bandTop - pixelsPerBand/2;
//							yn[3] = bandTop;
//							
//
//					    Polygon dpolygon_ = new Polygon(xn, yn, 4);
//							g.fillPolygon(dpolygon_);
//						}
//						
//						if (i == numColors - 1) {
//							int[] xn = new int[4];
//							int[] yn = new int[4];
//							
//							xn[0] = left + 10;
//							xn[1] = left + 25;
//							xn[2] = ((left + 25) - (left + 10))/2;
//							xn[3] = left + 10;
//							
//							yn[0] = bandBottom;
//							yn[1] = bandBottom;
//							yn[2] = bandBottom + pixelsPerBand/2;
//							yn[3] = bandBottom;
//							
//
//					    Polygon dpolygon_ = new Polygon(xn, yn, 4);
//							g.fillPolygon(dpolygon_);
//						}
						
						if (mColorBar.isColorEnhanced(i)) {
							if (i == mColorBar.getMinEnhancedRange()) {
								selTop = bandTop;
								selBottom = bandBottom;
							}
							if (i == mColorBar.getMaxEnhancedRange()) {
								selBottom = bandBottom;
							}
							drawSelection = true;
						}

						// label
						g.setColor(Color.black);
						if (i % labelInc == 0) {
							String sTemp = mColorBar.getFormattedValue(i, numPlaces, true);
							// String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal),
							// numPlaces, true);
							g.drawString(sTemp, left + 35, bandBottom);
						}
					}
				}
				else {
					int c = 0;
					selTop = -99;
					selBottom = -99;
					for (int i = numColors - 1; i >= 0; i--) {
						bandTop = (int)(top + (c) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
						g.setColor(mColorBar.getColorValue(i));
						g.fillRect(left + 10, bandTop, left + 25, bandBottom - bandTop);

//						if (i == numColors - 1) {
//							int[] xn = new int[4];
//							int[] yn = new int[4];
//							
//							xn[0] = left + 10;
//							xn[1] = left + 35;
//							xn[2] = left + 23;
//							xn[3] = left + 10;
//							
//							yn[0] = bandTop;
//							yn[1] = bandTop;
//							yn[2] = bandTop - 13;
//							yn[3] = bandTop;
//
//					    Polygon dpolygon_ = new Polygon(xn, yn, 4);
//							g.fillPolygon(dpolygon_);
//						}
//						
//						if (i == 0) {
//							int[] xn = new int[4];
//							int[] yn = new int[4];
//							
//							xn[0] = left + 10;
//							xn[1] = left + 35;
//							xn[2] = left + 22;
//							xn[3] = left + 10;
//							
//							yn[0] = bandBottom;
//							yn[1] = bandBottom;
//							yn[2] = bandBottom + 13;
//							yn[3] = bandBottom;
//							
//					    Polygon dpolygon_ = new Polygon(xn, yn, 4);
//							g.fillPolygon(dpolygon_);
//						}
						
						if (mColorBar.isColorEnhanced(i)) {					
							if (i == mColorBar.getMinEnhancedRange()) {
								if (selTop == -99)
									selTop = bandTop+pixelsPerBand;
									selBottom = bandBottom+pixelsPerBand;
							}
							else if (i == mColorBar.getMaxEnhancedRange()) {
								selTop = bandTop+pixelsPerBand;
								if (selBottom == -99)
									selBottom = bandBottom+pixelsPerBand;
							}
							drawSelection = true;
						}

						// label
						g.setColor(Color.black);
						if (c % labelInc == 0) {
							String sTemp = mColorBar.getFormattedValue(i, numPlaces, true);
							g.drawString(sTemp, left + 35, bandBottom);
						}
						c++;
					}
				}
			}

			if (drawSelection && mIsEnhanceable) {
				g.setColor(Color.black);
				g.drawRect(left + 10, selTop, left + 24, selBottom - selTop - 1);
			}

			// plot the last horizontal label
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION && offsetLabels) {
				if (numColors % 2 == 0) {
					double myVal = mColorBar.getDoubleValue(numColors - 1);
					String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
					g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 30);
				}
				else {
					double myVal = mColorBar.getDoubleValue(numColors - 1);
					String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false);
					g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 20);
				}
			}
			else if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				double myVal = mColorBar.getDoubleValue(numColors - 1);
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
				int l = bandRight - (fm.stringWidth(sTemp) / 2);
				if (l > maxLabelH + 5) {
					g.drawString(sTemp, l, bottom + 20);
				}
			}

			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				// outline colorbar
				g.drawRect(left, top, maxH - left, bottom);

				// plot the last tic mark
				if (numColors % 2 == 0 && offsetLabels) {
					g.drawLine(maxH, bottom, maxH, bottom + 15);
				}
				else {
					g.drawLine(maxH, bottom, maxH, bottom + 5);
				}
			}

			if (mLabelPanel && mOrientation != JOAConstants.HORIZONTAL_ORIENTATION && !mFancyLabelPanel) {
				// put the label here
				String panelLabel = null;
				if (mFileViewer != null) {
					int pPos = mFileViewer.getPropertyPos(mColorBar.getParam(), false);
					if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null
					    && mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
						panelLabel = new String(mColorBar.getParam() + " (" + mFileViewer.mAllProperties[pPos].getUnits() + ")");
					}
					else {
						panelLabel = new String(mColorBar.getParam());
					}
				}
				else {
					panelLabel = new String(mColorBar.getParam());
				}
				int strWidth = fm.stringWidth(panelLabel);
				JOAFormulas.drawStyledString(panelLabel, right / 2 - strWidth / 2, bandBottom + 10, fm, (Graphics2D)g);
			}

			// initialize the spot
			if (mObsMarker == null && mFileViewer != null && isLinked()) {
				OpenDataFile of = (OpenDataFile)mFileViewer.mOpenFiles.currElement();
				Section sech = of.getCurrSection();
				Station sh = (Station)sech.mStations.currElement();
				Bottle bh = (Bottle)sh.mBottles.currElement();
				double val = 0.0;
				if (!mFileViewer.mDefaultCB.isMetadataColorBar()) {
					val = bh.mDValues[mFileViewer.mCurrentColorVariable];
				}
				else {
					// get the value of the metadatatype for this station
					ResourceBundle rb = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
					if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTime"))) {
						val = sh.getDate().getTime();
					}
					if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kDateTimeMonth"))) {
						val = sh.getDate().getMonth() - 1;
					}
					else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLatitude"))) {
						val = sh.getLat();
					}
					else if (mFileViewer.mDefaultCB.getMetadataType().equalsIgnoreCase(rb.getString("kLongitude"))) {
						val = sh.getLon();
					}
				}
				int index = mColorBar.getColorIndex(val);
				if (index != JOAConstants.MISSINGVALUE) {
					int xx = left + 5;
					if (JOAConstants.COLORBARDIRECTION == Direction.DOWN) {
						bandTop = (int)(top + (index) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
					}
					else {
						bandTop = (int)(top + (mColorBar.getNumLevels() - index) * pixelsPerBand);
						bandBottom = (int)(bandTop + pixelsPerBand);
					}

					int yy = bandTop + (bandBottom - bandTop) / 2;
					mObsMarker = new ObsMarker(xx, yy - correctY, 10);
					mObsMarker.setSymbol(JOAConstants.SYMBOL_RIGHT_ARROW_SYMBOL);
				}
			}
		}

		public Dimension getPreferredSize() {
			if (mOrientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				return new Dimension(mWidth, mHeight);
			}
			else {
				return new Dimension(mWidth, mHeight + 25);
			}
		}

		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	}

	public NewColorBar getColorBar() {
		return mColorBar;
	}

	public void prefsChanged(PrefsChangedEvent evt) {
		mColorBar.setLabelFormatter();
		mSwatchPanel.setSize(mSwatchPanel.getSize().width, mSwatchPanel.getSize().height + 1);
		mSwatchPanel.setSize(mSwatchPanel.getSize().width, mSwatchPanel.getSize().height - 1);
	}

	/*
	 * private class labelPanel extends JPanel { private labelPanel() { init(); }
	 * 
	 * public void init() { double base = mColorBar.getBaseLevel(); double end =
	 * mColorBar.getEndLevel(); double diff = Math.abs(end - base); int numPlaces
	 * = 2; if (diff < 10) numPlaces = 3; else if (diff >= 10 && diff < 100)
	 * numPlaces = 2; else if (diff >= 100 && diff < 1000) numPlaces = 1; else if
	 * (diff >= 1000) numPlaces = 1; int labelInc = 0; int numColors =
	 * mColorBar.mNumColorLevels; if (numColors <= 16) labelInc = 1; else if
	 * (numColors > 16 && numColors <= 32) labelInc = 2; else if (numColors > 32
	 * && numColors <= 48) labelInc = 3; else if (numColors > 48 && numColors <=
	 * 64) labelInc = 4; this.setLayout(new GridLayout(16, 1, 0, 0)); for (int
	 * i=0; i<mColorBar.mNumColorLevels; i+=labelInc) { double myVal =
	 * mColorBar.getDoubleValue(i); JOAJLabel lb = new
	 * JOAJLabel(JOAFormulas.formatDouble(String.valueOf(myVal), numPlaces,
	 * true)); lb.setFont(new Font("Courier", Font.PLAIN, 11)); this.add(lb); } }
	 * 
	 * public void paintComponent(Graphics g) { super.paintComponent(g); }
	 * 
	 * public Dimension getPreferredSize() { return new Dimension(100, 220); }
	 * 
	 * public Insets getInsets() { return new Insets(0, 0, 0, 0); } }
	 */

}

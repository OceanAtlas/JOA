/**
 * 
 */
package javaoceanatlas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import javaoceanatlas.classicdatamodel.Bottle;
import javaoceanatlas.classicdatamodel.OpenDataFile;
import javaoceanatlas.classicdatamodel.Section;
import javaoceanatlas.classicdatamodel.Station;
import javaoceanatlas.events.ObsChangedEvent;
import javaoceanatlas.events.ObsChangedListener;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.ui.widgets.ObsMarker;
import javaoceanatlas.utility.ColumnLayout;
import javaoceanatlas.utility.JOAFormulas;
import javaoceanatlas.utility.Orientation;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author oz
 * 
 */

public class StnLegendPanel extends JPanel implements ObsChangedListener {
	private FileViewer mFV;
	private HashMap<String, Color> mStnColorHash;
	private HashMap<String, StnDetailPanel> mStnHash = new HashMap<String, StnDetailPanel>();
	private HashMap<String, Boolean> mStnPaintedCache = new HashMap<String, Boolean>();
	private ObsMarker mObsMarker;
	private JPanel detailPanel;
	private int mHeight;
	private int mWidth;
	private StnDetailPanel mCurrDetail;
	private boolean mCurrStnOnly = false;

	public StnLegendPanel(FileViewer fv, HashMap<String, Color> stnHash) {
		this(fv, stnHash, 50, 10);
	}

	public StnLegendPanel(FileViewer fv, HashMap<String, Color> stnHash, int width, int height) {
		mFV = fv;
		mStnColorHash = stnHash;
		mHeight = height + 3;
		mWidth = width + 3;
		this.setBackground(JOAConstants.DEFAULT_FRAME_COLOR);
		mFV.addObsChangedListener(this);
		mObsMarker = new ObsMarker(0, 0, JOAConstants.DEFAULT_CURSOR_SIZE);

		// set layout
		this.setLayout(new BorderLayout(0, 0));

		// make the contents panel
		detailPanel = new JPanel(new ColumnLayout(Orientation.CENTER, Orientation.TOP, 2));

		// populate the detail panels
		buildDetailPanels();

		// make a scroller
		MyScroller swatchScroller = new MyScroller(detailPanel);

		this.add(BorderLayout.CENTER, swatchScroller);

		updateCurrStn();
	}

	public void buildDetailPanels() {
		detailPanel.removeAll();
		mStnHash.clear();

		for (int fc = 0; fc < mFV.mNumOpenFiles; fc++) {
			OpenDataFile of = (OpenDataFile)mFV.mOpenFiles.elementAt(fc);

			for (int sec = 0; sec < of.mNumSections; sec++) {
				Section sech = (Section)of.mSections.elementAt(sec);

				for (int stc = 0; stc < sech.mStations.size(); stc++) {
					Station sh = (Station)sech.mStations.elementAt(stc);

					if (!sh.mUseStn) {
						continue;
					}

					Color c = getStnColorHash().get(sh.mStnNum);
					if (c != null) {
						StnDetailPanel detail = new StnDetailPanel(of, sech, sh, c);
						if (mCurrDetail == null) {
							detail.setCurrObs(true);
							mCurrDetail = detail;
						}
						else {
							detail.setCurrObs(false);
						}
						detailPanel.add(detail);
						mStnHash.put(sh.mStnNum, detail);
					}
				}
			}
		}

		// set currObs
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javaoceanatlas.events.ObsChangedListener#obsChanged(javaoceanatlas.events
	 * .ObsChangedEvent)
	 */
	public void obsChanged(ObsChangedEvent evt) {
		// find matching station detail
		// display the current station
		Station sh = evt.getFoundStation();

		StnDetailPanel detail = this.mStnHash.get(sh.mStnNum);
		if (detail != null) {
			if (mCurrStnOnly) {
				setAllUnpainted();
			}
			this.mCurrDetail.setCurrObs(false);
			mCurrDetail.repaint();
			mCurrDetail = detail;
			this.mCurrDetail.setCurrObs(true);
			mCurrDetail.setPainted(true);
			mCurrDetail.repaint();
		}
	}

	public void updateCurrStn() {
		OpenDataFile of = (OpenDataFile)mFV.mOpenFiles.currElement();
		Section sech = (Section)of.mSections.currElement();
		Station sh = (Station)sech.mStations.currElement();
		StnDetailPanel detail = this.mStnHash.get(sh.mStnNum);

		if (detail != null) {
			this.mCurrDetail.setCurrObs(false);
			mCurrDetail.repaint();
			mCurrDetail = detail;
			this.mCurrDetail.setCurrObs(true);
			mCurrDetail.setPainted(true);
			mCurrDetail.repaint();
		}
	}

	/**
	 * @param mStnColorHash
	 *          the mStnColorHash to set
	 */
	public void setStnColorHash(HashMap<String, Color> sch) {
		this.mStnColorHash = sch;
		buildDetailPanels();
	}

	/**
	 * @return the mStnColorHash
	 */
	private HashMap<String, Color> getStnColorHash() {
		return mStnColorHash;
	}

	private class StnDetailPanel extends JPanel {
		private boolean mCurrObs = false;
		private String mLabel;
		private Color mColor;
		private boolean mPainted = true;
		OpenDataFile foundFile;
		Section foundSection;
		Station foundStation;

		public StnDetailPanel(OpenDataFile of, Section sech, Station sh, Color stnColor) {
			foundFile = of;
			foundSection = sech;
			foundStation = sh;
			mLabel = new String(sh.mStnNum);
			mColor = new Color(stnColor.getRGB());
			this.setToolTipText("Station = " + mLabel);
			this.addMouseListener(new XYMouseHandler());
		}

		/**
		 * @param mCurrObs
		 *          the mCurrObs to set
		 */
		public void setCurrObs(boolean b) {
			this.mCurrObs = b;
			this.invalidate();
		}

		/**
		 * @return the mCurrObs
		 */
		public boolean isCurrObs() {
			return mCurrObs;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Color swatchColor = mColor;
			Color textColor = Color.black;

			if (!this.isPainted()) {
				swatchColor = Color.LIGHT_GRAY;
				textColor = Color.LIGHT_GRAY;
			}

			g.setColor(swatchColor);
			g.fillRect(2, 2, 10, mHeight - 3);
			g.translate(14, mHeight - 1);
			g.setFont(new java.awt.Font("serif", 0, 13));
			g.setColor(textColor);
			g.drawString(mLabel, 0, 0);

			FontMetrics fm = g.getFontMetrics();
			if (this.isCurrObs()) {
				int newX = (int)fm.getStringBounds(mLabel, g).getBounds().getWidth() + 9;
				int newY = -(int)(mHeight / 2) + 1;
				mObsMarker.setNewPos(newX, newY);
				mObsMarker.drawMarker((Graphics2D)g, false);
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(mWidth, mHeight);
		}

		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}

		/**
		 * @param mPainted
		 *          the mPainted to set
		 */
		public void setPainted(boolean mPainted) {
			this.mPainted = mPainted;
		}

		/**
		 * @return the mPainted
		 */
		public boolean isPainted() {
			return mPainted;
		}

		/**
		 * @param mLabel
		 *          the mLabel to set
		 */
		public void setLabel(String mLabel) {
			this.mLabel = mLabel;
		}

		/**
		 * @return the mLabel
		 */
		public String getLabel() {
			return mLabel;
		}

		public void findBottle() {
			Bottle foundBottle = null;
			OpenDataFile oldof = (OpenDataFile)mFV.mOpenFiles.currElement();
			Section oldsech = (Section)oldof.mSections.currElement();
			Station oldsh = (Station)oldsech.mStations.currElement();
			Bottle oldBottle = (Bottle)oldsh.mBottles.currElement();

			int xPos = foundSection.getPRESVarPos();
			double testPres = oldBottle.mDValues[xPos];
    	JOAConstants.currTestPres = testPres;
      foundBottle = JOAFormulas.findBottleByPres(mFV, foundStation);

			// post event so other components will update
			if (foundBottle != null) {
					mFV.mOpenFiles.setCurrElement(foundFile);
					foundFile.mSections.setCurrElement(foundSection);
					foundSection.mStations.setCurrElement(foundStation);
					foundStation.mBottles.setCurrElement(foundBottle);
					ObsChangedEvent oce = new ObsChangedEvent(mFV);
					oce.setFoundObs(foundFile, foundSection, foundStation, foundBottle);
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(oce);
			}
			else {
				Toolkit.getDefaultToolkit().beep();
			}
		}

		private class XYMouseHandler extends MouseAdapter {
			public void mouseClicked(MouseEvent me) {
				// find a new observation
				findBottle();
			}

			public void mouseReleased(MouseEvent me) {
			}

			public void mousePressed(MouseEvent me) {
			}
		}
	}

	public void setAllUnpainted() {
		for (StnDetailPanel sdp : mStnHash.values()) {
			sdp.setPainted(false);
			sdp.repaint();
		}
	}

	public void setAllPainted() {
		for (StnDetailPanel sdp : mStnHash.values()) {
			sdp.setPainted(true);
			sdp.repaint();
		}
	}

	public void setPlotOnlyCurrStn(boolean b) {
		mCurrStnOnly = b;
	}

	public void cachePaintedDetails() {
		mStnPaintedCache.clear();
		for (StnDetailPanel sdp : mStnHash.values()) {
			boolean selState = sdp.isPainted();
			String sh = sdp.getLabel();
			mStnPaintedCache.put(sh, selState);
		}
	}

	public void restoreCachedPaintedDetails() {
		for (StnDetailPanel sdp : mStnHash.values()) {
			String sh = sdp.getLabel();
			Boolean cacheState = mStnPaintedCache.get(sh);
			if (cacheState != null) {
				sdp.setPainted(cacheState);
			}
		}
	}

	private class MyScroller extends JScrollPane {
		public MyScroller(Component c) {
			super(c);
			this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}

		public Dimension getPreferredSize() {
			return new Dimension(100, 200);
		}
	}
}

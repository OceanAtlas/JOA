/*
 * $Id: SimpleMapPlotPanel.java,v 1.7 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import javaoceanatlas.specifications.*;

@SuppressWarnings("serial")
public class SimpleMapPlotPanel extends MapPlotPanel implements DialogClient {
	double mFilterLatMin = -10;
	double mFilterLatMax = 30;
	double mFilterLonMin = -100;
	double mFilterLonMax = 0;
	boolean mFilterRegionActive = true;
	JOAJTextField mLatMin = null;
	JOAJTextField mLatMax = null;
	JOAJTextField mLonMin = null;
	JOAJTextField mLonMax = null;
	private JPopupMenu mPopupMenu = null;

	public SimpleMapPlotPanel(FileViewer fv, MapSpecification mapspec, int width, int height, int legendHeight,
							ObsMarker obsMarker, JOAMapContainer contents, JOAWindow frame, MapLegend legend,
							MapColorBarPanel cbarLegend, boolean browsable, int hoffset, int voffset, double hscale,
							double vscale) {
		super(fv, mapspec, obsMarker, contents, frame, legend, cbarLegend, hoffset, voffset, hscale, vscale);
	}

	public void showConfigDialog() {
		// show configuration dialog
		ConfigSimpleMapPlot cp = new ConfigSimpleMapPlot(mFrame, this, mMapSpec);
		cp.pack();

		// show dialog at center of screen
		Rectangle dBounds = cp.getBounds();
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int x = sd.width/2 - dBounds.width/2;
		int y = sd.height/2 - dBounds.height/2;
		cp.setLocation(x, y);

		cp.setVisible(true);
	}

	public void init() {
		setSectionMode();
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 2) {
					showConfigDialog();
				}
				else {
					if (me.isPopupTrigger()) {
						createPopup(me.getPoint());
					}
					else {
						if (me.isShiftDown()) {
							zoomOut();
						}
					}
				}
				//me.consume();
			}
			public void mouseReleased(MouseEvent me) {
				super.mouseReleased(me);
				if (rbRect != null &&  me.getID() == MouseEvent.MOUSE_RELEASED) {
					zoomPlot(rbRect.getBounds(), me.isAltDown(), me.isShiftDown());
				}
				else if (me.isPopupTrigger()) {
					createPopup(me.getPoint());
				}
			}
		});
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

	public void zoomOut() {
		mMapSpec.setLatMax(mMapSpec.getLatMax() <= 90 ? mMapSpec.getLatMax() + 5 : 90);
		mMapSpec.setLatMin(mMapSpec.getLatMin() >= -90 ? mMapSpec.getLatMin() - 5 : -90);
		if (mMapSpec.getLatMax() >= 90 && mMapSpec.getLatMin() <= -90) {
			mMapSpec.setLatMax(90);
			mMapSpec.setLatMin(-90);
			mMapSpec.setLonRt(19.99);
			mMapSpec.setLonLft(20);
			mMapSpec.setCenLon(-180 + mMapSpec.getLonRt());
		}
		else {
			mMapSpec.setLonRt(mMapSpec.getLonRt() <= 180 ? mMapSpec.getLonRt() + 5 : 180);
			mMapSpec.setLonLft(mMapSpec.getLonLft() >= -180 ? mMapSpec.getLonLft() - 5 : -180);
	    	double diff = 0;
	    	if (mMapSpec.getLonLft() > 0 && mMapSpec.getLonRt() < 0)
	    		diff = Math.abs(((180 + mMapSpec.getLonRt()) + (180 - mMapSpec.getLonLft()))/2);
	    	else
	    		diff = Math.abs((mMapSpec.getLonRt() - mMapSpec.getLonLft())/2);
		    mMapSpec.setCenLon(diff + mMapSpec.getLonLft() > 180 ? mMapSpec.getLonRt() - diff : mMapSpec.getLonLft() + diff);
		}
    	mMapSpec.setCenLat(mMapSpec.getLatMin() + (mMapSpec.getLatMax() - mMapSpec.getLatMin())/2.0);

		// invalidate and replot
		invalidate();
		paintComponent(this.getGraphics());
		rbRect.end(rbRect.getStretched());
	}

	public void zoomPlot(Rectangle newRect, boolean mode, boolean mode2) {
		// convert corners of rectangle to new plot range
		Rectangle newBounds = rbRect.lastBounds();
		int x1 = newBounds.x;
		int x2 = x1 + newBounds.width;
		int y1 = newBounds.y;
		int y2 = y1 + newBounds.height;

		// convert to lat/lon
		UVCoordinate uv = invTransformLL(x1, y2);
		double minLon = uv.u;
		double minLat = uv.v;
		uv = invTransformLL(x2, y1);
		double maxLon = uv.u;
		double maxLat = uv.v;

		if (newBounds.width < 10 || newBounds.height < 10)
			return;

		if (mode) {
			// zoom in
			mMapSpec.setLatMax(maxLat);
			mMapSpec.setLatMin(minLat);
			mMapSpec.setLonRt(maxLon);
			mMapSpec.setLonLft(minLon);
	    	mMapSpec.setCenLat(mMapSpec.getLatMin() + (mMapSpec.getLatMax() - mMapSpec.getLatMin())/2.0);
	    	double diff = 0;
	    	if (mMapSpec.getLonLft() > 0 && mMapSpec.getLonRt() < 0)
	    		diff = Math.abs(((180 + mMapSpec.getLonRt()) + (180 - mMapSpec.getLonLft()))/2);
	    	else
	    		diff = Math.abs((mMapSpec.getLonRt() - mMapSpec.getLonLft())/2);
		    mMapSpec.setCenLon(diff + mMapSpec.getLonLft() > 180 ? mMapSpec.getLonRt() - diff : mMapSpec.getLonLft() + diff);

			// invalidate and replot
			invalidate();
			paintComponent(this.getGraphics());
		}
		else if (mode2) {
			// zoom out
		}
		else {
			// zoom using current window
			mFilterLatMax = maxLat;
			mFilterLatMin = minLat;
			mFilterLonMax = maxLon;
			mFilterLonMin = minLon;
			updateTextFlds();
		}
	}

	public void paintComponent(Graphics gin) {
		if (gin == null)
			return;
		Graphics2D g = (Graphics2D)gin;
		if (mOffScreen == null) {
			this.checkProjection(getSize().width, getSize().height);
			mOffScreen = createImage(getSize().width, getSize().height);

			final Graphics og = mOffScreen.getGraphics();
			super.paintComponent(og);

			if (mMapSpec.isColorFill())
				drawFilledBathy(og);

			if (mMapSpec.isDrawGraticule())
				plotBorder(og);

			g.drawImage(mOffScreen, 0, 0, null);
			og.dispose();

			// recompute the selection rectangle
			if (mSelectionRect != null)
				setFilterRegion(mFilterLatMin, mFilterLatMax, mFilterLonMin, mFilterLonMax);
		}
		else
			g.drawImage(mOffScreen, 0, 0, null);

		// plot the stations
		plotStations(g);

		if (mSelectionRect != null) {
			g.setColor(Color.white);
			g.setStroke(lw2);
			g.draw(mSelectionRect);
			g.setColor(Color.black);
		}
	}

	public void resetMap() {
		resetSelection();
		setFilterRegion(-90, 90, -180, 180);
		forceRedraw(false);
	}

	public void resetSelection() {
		mSelectionRect = null;
	}

	public void setFilterRegion(double minLat, double maxLat, double minLon, double maxLon) {
		mFilterLatMin = minLat;
		mFilterLatMax = maxLat;
		mFilterLonMin = minLon;
		mFilterLonMax = maxLon;

		if (mSelectionRect == null)
			return;

		// have to recompute the bounds of the selection rect
		UVCoordinate uv1 = transformLL(mFilterLatMax, mFilterLonMin);
		uv1 = mapScaler(uv1.u, uv1.v);
		UVCoordinate uv2 = transformLL(mFilterLatMin, mFilterLonMax);
		uv2 = mapScaler(uv2.u, uv2.v);
		mSelectionRect.reset();
		mSelectionRect.moveTo((float)uv1.u, (float)uv1.v);
		mSelectionRect.lineTo((float)uv2.u, (float)uv1.v);
		mSelectionRect.lineTo((float)uv2.u, (float)uv2.v);
		mSelectionRect.lineTo((float)uv1.u, (float)uv2.v);
		mSelectionRect.lineTo((float)uv1.u, (float)uv1.v);
        this.setRubberbandDisplayObject(mSelectionRect, false);
	}

	public void setTextFlds(JOAJTextField latMin, JOAJTextField latMax, JOAJTextField lonMin, JOAJTextField lonMax) {
		mLatMin = latMin;
		mLatMax = latMax;
		mLonMin = lonMin;
		mLonMax = lonMax;
	}

	public void forceRedraw(boolean redrawMap) {
		if (redrawMap) {
			invalidate();
			paintComponent(this.getGraphics());
		}
		else
			repaint();
	}

	public void updateTextFlds() {
		if (mLatMin != null) {
			mLatMin.setText(JOAFormulas.formatDouble(String.valueOf(mFilterLatMin), 3, true));
		}
		if (mLatMax != null) {
			mLatMax.setText(JOAFormulas.formatDouble(String.valueOf(mFilterLatMax), 3, true));
		}
		if (mLonMin != null) {
			mLonMin.setText(JOAFormulas.formatDouble(String.valueOf(mFilterLonMin), 3, true));
		}
		if (mLonMax != null) {
			mLonMax.setText(JOAFormulas.formatDouble(String.valueOf(mFilterLonMax), 3, true));
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("opencontextual")) {
			showConfigDialog();
		}
	}

	// OK Button
    @SuppressWarnings("deprecation")
    public void dialogDismissed(JDialog f) {
		f.hide();

    	f.dispose();
		forceRedraw(true);
    }

	// Cancel button
    public void dialogCancelled(JDialog f) {
    }

	// something other than the OK button
    public void dialogDismissedTwo(JDialog f) {
    }

    public void dialogApplyTwo(Object d) {
    }

	// Apply button, OK w/o dismissing the dialog
    public void dialogApply(JDialog f) {
    	try {
    		((ConfigSimpleMapPlot)f).getMapSpec();
    	}
    	catch (ClassCastException ex) {
    	}
		forceRedraw(true);
    }
}

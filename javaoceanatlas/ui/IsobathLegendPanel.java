/*
 * $Id: IsobathLegendPanel.java,v 1.4 2005/06/17 18:08:53 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.specifications.*;
import java.awt.geom.*;

@SuppressWarnings("serial")
public class IsobathLegendPanel extends JPanel implements DialogClient, ActionListener {
	protected MapSpecification mMapSpec = null;
	public swatchPanel mSwatchPanel = null;
	protected JPanel mCBCont = null;
	protected DialogClient mDialogClient = null;
	private JPopupMenu mPopupMenu = null;
	protected int mWidth;
	protected int mHeight;
	protected Color mBG;
	int numCols;
	int numRows;

	public IsobathLegendPanel(MapSpecification mapSpec) {
		// if (bgColor != null) {
		// this.setBackground(bgColor);
		// mBG = bgColor;
		// }
		if (mapSpec == null)
			return;
		mMapSpec = mapSpec;
		this.setLayout(new BorderLayout(0, 0));
		mCBCont = new JPanel();
		mCBCont.setBackground(Color.white);
		mCBCont.setOpaque(true);
		// if (mBG != null) {
		// mCBCont.setBackground(mBG);
		// }
		mCBCont.setLayout(new RowLayout(Orientation.LEFT, Orientation.CENTER, 0));
		mSwatchPanel = new swatchPanel();
		mSwatchPanel.setBackground(Color.white);
		mSwatchPanel.setOpaque(true);
		mCBCont.add(mSwatchPanel);
		this.add(mCBCont, "Center");
		addMouseListener(new XYMouseHandler());
		mDialogClient = this;

		mWidth = 550;
		int numColors = mMapSpec.getNumIsobaths();
		numCols = mWidth / 55;
		numRows = numColors / numCols + 1;
		mHeight = numRows * 22;
	}

	public void drawColorBar(Graphics g) {
		mSwatchPanel.drawColorBar(g);
	}

	public void setHeight(int h) {
		mHeight = h;
	}

	public void setWidth(int w) {
		mWidth = w;

		// compute how many squiggles will fit on legend
		int numColors = mMapSpec.getNumIsobaths();
		numCols = mWidth / 55;
		numRows = numColors / numCols + 1;
		mHeight = numRows * 22;
		if (mSwatchPanel != null) {
			mSwatchPanel.setSize(mWidth, mHeight);
		}
		this.setSize(mWidth, mHeight);
	}

	public void setNewMapSpecfication(MapSpecification newspec) {
		mMapSpec = newspec;
		this.setSize(this.getSize().width, this.getSize().height + 1);
		this.setSize(this.getSize().width, this.getSize().height - 1);
	}

	public void setNewBGColor(Color bg) {
		mBG = bg;
		this.setBackground(mBG);
		mCBCont.setBackground(mBG);
	}

	public Dimension getPreferredSize() {
		return new Dimension(mWidth, mHeight);
	}

	// OK Button
	public void dialogDismissed(JDialog f) {
	}

	// Cancel button
	public void dialogCancelled(JDialog f) {
	}

	// something other than the OK button
	public void dialogDismissedTwo(JDialog f) {
	}

	// Apply button, OK w/o dismissing the dialog
	public void dialogApply(JDialog f) {
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
		// EditColorbar ecb = new EditColorbar(mParent, mFileViewer, mDialogClient,
		// mColorBar);
		// ecb.pack();
		// ecb.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("opencontextual")) {
			showConfigDialog();
		}
	}

	public class XYMouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent me) {
			if (me.getClickCount() == 2) {
				;// showConfigDialog();
			}
			else {
				if (me.isPopupTrigger()) {
					;// createPopup(me.getPoint());
				}
			}
		}

		public void mouseReleased(MouseEvent me) {
			super.mouseReleased(me);
			if (me.isPopupTrigger()) {
				;// createPopup(me.getPoint());
			}
		}

		public void mousePressed(MouseEvent me) {
			super.mousePressed(me);
			if (me.isPopupTrigger()) {
				;// createPopup(me.getPoint());
			}
		}
	}

	private class swatchPanel extends JPanel {
		public swatchPanel() {
		}

		public void paintComponent(Graphics g) {
			if (g instanceof PrintGraphics) {
				drawColorBar(g);
			}
			else {
				super.paintComponent(g);
				// if (mBG != null) {
				// this.setBackground(mBG);
				// }
				drawColorBar(g);
			}
		}

		public void drawColorBar(Graphics gin) {
			Graphics2D g = (Graphics2D) gin;
			float v = 20;
			float h = 5;
			int numColors = mMapSpec.getNumIsobaths();
			GeneralPath aLine;
			BasicStroke lw1 = new BasicStroke(2);

			// compute how many squiggles will fit on legend
			// int numCols = mWidth/55 - 1;

			// set the font size
			g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
			    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
			g.setClip(0, 0, 1000, 1000);

			// reverse the values
			Color[] tempColors = new Color[numColors];
			double[] tempValues = new double[numColors];
			for (int i = 0; i < numColors; i++) {
				tempColors[i] = mMapSpec.getIsobathColors()[numColors - 1 - i];
				tempValues[i] = -mMapSpec.getIsobathValues()[numColors - 1 - i];
			}

			for (int i = 0; i < numColors; i++) {
				aLine = new GeneralPath();
				double myVal = tempValues[i];
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
				aLine.moveTo(h - 4, v + 2);
				aLine.lineTo(h + 2, v - 10);
				aLine.lineTo(h + 7, v - 2);
				aLine.lineTo(h + 13, v - 15);

				try {
					// aLine.closePath();
					g.setStroke(lw1);
					g.setColor(tempColors[i]);
					g.draw(aLine);
				}
				catch (Exception ex) {
				}

				g.setColor(Color.black);
				g.drawString(sTemp, h + 11, v);

				if ((i + 1) % numCols == 0) {
					h = 5;
					v += 20;
				}
				else
					h += 55;
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(mWidth, mHeight);
		}

		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	}
}
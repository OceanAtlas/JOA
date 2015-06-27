/**
 * 
 */
package javaoceanatlas.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import javaoceanatlas.resources.JOAConstants;
import javaoceanatlas.utility.JOAFormulas;
import javaoceanatlas.utility.NewColorBar;
import javaoceanatlas.utility.Orientation;
import javaoceanatlas.utility.RowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author oz
 *
 */
public class MapColorBarPanel extends JPanel {
	private HashMap<String, ColorBarPanel> mPanels = new HashMap<String, ColorBarPanel>();
	private FileViewer mFileViewer;
	private JFrame mParentFrame;

	public MapColorBarPanel(JFrame parentFrame, FileViewer fv) {
		super();
		mFileViewer = fv;
		mParentFrame = parentFrame;
		this.setLayout(new RowLayout(Orientation.LEFT, Orientation.TOP));
	}
	
  public void addColorBar(String id, NewColorBar cb) {
  	ColorBarPanel cbp = new ColorBarPanel(mParentFrame, mFileViewer, cb);
  	if (cbp != null) {
  		mPanels.put(id, cbp);
  		this.add(cbp);
  	}
  }
  
  public void replaceColorBar(String id, NewColorBar cb) {
  	ColorBarPanel cbp = mPanels.get(id);
  	if (cbp != null) {
  		this.remove(cbp);
    	ColorBarPanel ncbp = new ColorBarPanel(mParentFrame, mFileViewer, cb);
  		this.add(ncbp);
  		mPanels.remove(cbp);
  		mPanels.put(id, ncbp);
  	}
  }
  
  public void setNewBGColor(Color c) {
  	for (ColorBarPanel cbp : mPanels.values()) {
  		cbp.setBackground(c);
  	}
  }
  
  public void setLocked(boolean b) {
  	for (ColorBarPanel cbp : mPanels.values()) {
  		cbp.setLocked(b);
  	}
  }

  public void removeColorBarPanel(String id) {
  	ColorBarPanel cbp = mPanels.get(id);
  	if (cbp != null) {
  		mPanels.remove(id);
  		this.remove(cbp);
  	}
  }

  public ColorBarPanel getColorBarPanel(String id) {
  	ColorBarPanel cbp = mPanels.get(id);
  	if (cbp != null) {
  		return cbp;
  	}
  	return null;
  }
  
  public void clear() {
  	this.removeAll();
  	mPanels.clear();
  }
  
  public int getColorBarWidth(Graphics2D g) {
  	int totalWidth = 0;
  	for (ColorBarPanel cbp : mPanels.values()) {
  		totalWidth += getColorBarWidth(cbp, g);
  	}
  	return totalWidth;
  }

  public int getColorBarWidth(ColorBarPanel cbp, Graphics2D g) {
		g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
		    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
		FontMetrics fm = g.getFontMetrics();
		int numColors = cbp.getColorBar().getNumLevels();

		// draw the color ramp and labels
		double base = cbp.getColorBar().getBaseLevel();
		double end = cbp.getColorBar().getEndLevel();
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
		int maxLabelH = 0;
		for (int i = 0; i < numColors; i++) {
			String sTemp = cbp.getColorBar().getFormattedValue(i, numPlaces, true);
			int strWidth = fm.stringWidth(sTemp);
			maxLabelH = strWidth > maxLabelH ? strWidth : maxLabelH;
		}
		return maxLabelH + 45;
	}
  
  public void drawAllColorBars(Graphics2D g, int height, int width, int leftMargin, int orientation) {
  	int i = 0;
  	for (ColorBarPanel cbp : mPanels.values()) {
  		drawColorBar(g, cbp.getName(),  height,  width,  leftMargin,  i, orientation);
  		i++;
  	}
  }
  
  public void forceRedrawAll() {
  	for (ColorBarPanel cbp : mPanels.values()) {
  		cbp.forceRedraw();
  	}
  }
  
  public void drawColorBar(Graphics2D g, String colorBarID, int height, int width, int leftMargin, int ord, int orientation) {
  	NewColorBar colorBar = getColorBarPanel(colorBarID).getColorBar();
		int numColors = colorBar.getNumLevels();
		int left = leftMargin;
		int right = width;
		int top = 15;
		int bottom = height;
		int pixelsPerBand = (bottom - top - 2) / numColors;
		int bandTop = 0;
		int bandBottom = 0;
		int bandLeft = 0;
		int bandRight = 0;
		
		g.translate(ord * 50, 0);

		g.setFont(new Font(JOAConstants.DEFAULT_COLORBAR_LABEL_FONT, JOAConstants.DEFAULT_COLORBAR_LABEL_STYLE,
		    JOAConstants.DEFAULT_COLORBAR_LABEL_SIZE));
		FontMetrics fm = g.getFontMetrics();
		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			top = 0;
			bottom = height - 30;
			pixelsPerBand = (right - left - 5) / numColors;
		}

		// draw the color ramp and labels
		double base = colorBar.getBaseLevel();
		double end = colorBar.getEndLevel();
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
		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			for (int i = 0; i < numColors; i++) {
				String sTemp = null;
				if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
					sTemp = colorBar.getFormattedValue(i) + "m";
				}
				else {
					sTemp = colorBar.getFormattedValue(i);
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
		for (int i = 0; i < numColors; i++) {
			// swatch
			if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
				bandLeft = (int)(left + i * pixelsPerBand);
				bandRight = (int)(bandLeft + pixelsPerBand);
				maxH = bandRight > maxH ? bandRight : maxH;
				g.setColor(colorBar.getColorValue(i));
				g.fillRect(bandLeft, top, bandRight - bandLeft, bottom);

				// draw a tic mark and labels
				g.setColor(Color.black);
				if (offsetLabels && i > 0 && i % 2 == 0) {
					String sTemp = colorBar.getFormattedValue(i - 1) + "m";
					g.drawLine(bandLeft, bottom, bandLeft, bottom + 15);
					g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 30);
				}
				else if (offsetLabels && i > 0) {
					String sTemp = colorBar.getFormattedValue(i - 1) + "m";
					g.drawLine(bandLeft, bottom, bandLeft, bottom + 5);
					g.drawString(sTemp, bandLeft - (fm.stringWidth(sTemp) / 2), bottom + 20);
				}
				else if (i > 0) {
					String sTemp = colorBar.getFormattedValue(i - 1) + "m";
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
			else {
				bandTop = (int)(top + (i) * pixelsPerBand);
				bandBottom = (int)(bandTop + pixelsPerBand);
				g.setColor(colorBar.getColorValue(i));
				g.fillRect(left + 10, bandTop, left + 25, bandBottom - bandTop);

				// label
				g.setColor(Color.black);
				if (i % labelInc == 0) {
					String sTemp = colorBar.getFormattedValue(i, numPlaces, true);
					// String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal),
					// numPlaces, true);
					g.drawString(sTemp, left + 35, bandBottom);
				}
			}
		}

		// plot the last horizontal label
		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION && offsetLabels) {
			if (numColors % 2 == 0) {
				double myVal = colorBar.getDoubleValue(numColors - 1);
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
				g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 30);
			}
			else {
				double myVal = colorBar.getDoubleValue(numColors - 1);
				String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false);
				g.drawString(sTemp, bandRight - (fm.stringWidth(sTemp) / 2), bottom + 20);
			}
		}
		else if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
			double myVal = colorBar.getDoubleValue(numColors - 1);
			String sTemp = JOAFormulas.formatDouble(String.valueOf(myVal), 0, false) + "m";
			int l = bandRight - (fm.stringWidth(sTemp) / 2);
			if (l > maxLabelH + 5) {
				g.drawString(sTemp, l, bottom + 20);
			}
		}

		if (orientation == JOAConstants.HORIZONTAL_ORIENTATION) {
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

		if (orientation != JOAConstants.HORIZONTAL_ORIENTATION) {
			// put the label here
			String panelLabel = null;
			if (mFileViewer != null) {
				int pPos = mFileViewer.getPropertyPos(colorBar.getParam(), false);
				if (pPos >= 0 && mFileViewer.mAllProperties[pPos].getUnits() != null
				    && mFileViewer.mAllProperties[pPos].getUnits().length() > 0) {
					panelLabel = new String(colorBar.getParam() + " (" + mFileViewer.mAllProperties[pPos].getUnits() + ")");
				}
				else {
					panelLabel = new String(colorBar.getParam());
				}
			}
			else {
				panelLabel = new String(colorBar.getParam());
			}
			int strWidth = fm.stringWidth(panelLabel);
			JOAFormulas.drawStyledString(panelLabel, right / 2 - strWidth / 2, bandBottom + 10, fm, (Graphics2D)g);
		}
		g.translate(-(ord * 50), 0);
	}
}

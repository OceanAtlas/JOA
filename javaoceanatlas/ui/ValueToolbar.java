/*
 * $Id: ValueToolbar.java,v 1.4 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javaoceanatlas.utility.*;
import javaoceanatlas.ui.widgets.*;
import java.awt.event.*;

 /**
 * @author  oz 
 * @version 1.0 01/13/00
 */
@SuppressWarnings("serial")
public class ValueToolbar extends JPanel {
	private JOAJTextField xTF = null;
	private JOAJTextField yTF = null;
	protected DecimalFormat depthDM = new DecimalFormat();
	protected double x = Double.NaN, prevx = Double.NaN;
	protected double y = Double.NaN, prevy = Double.NaN;
	protected String[] mXLabels;
	protected String[] mYLabels;
	protected int mNumYlabels, mNumXLabels;
	protected ResourceBundle b = ResourceBundle.getBundle("javaoceanatlas.resources.JOAResources");
	protected int[] mNumXPlaces;
	protected int[] mNumYPlaces;
	protected JOAJLabel mXLabel;
	protected JOAJLabel mYLabel;
	protected RubberbandPanel mComp;
	protected boolean mUseX = true;
	protected JPanel xfldPanel;
	protected JPanel yfldPanel;
	
	public ValueToolbar(RubberbandPanel comp, String[] yLabels, int[] numYPlaces) { 
		this(comp, null, null, yLabels, numYPlaces);
	}
		
	// constructor
	public ValueToolbar(RubberbandPanel comp, String[] xLabels, int[] numXPlaces, String[] yLabels, int[] numYPlaces) {
		mComp = comp;
		if (xLabels == null)
			mUseX = false;
			
		Font tff = new Font("SansSerif", Font.PLAIN, 10);
		if (mUseX) {
			mNumXLabels = xLabels.length;
		
			mXLabels = new String[mNumXLabels];
			mNumXPlaces = new int[mNumXLabels];
			for (int i=0; i<mNumXLabels; i++) {
				mXLabels[i] = new String(xLabels[i]);
				mNumXPlaces[i] = numXPlaces[i];
			}
			xfldPanel = new JPanel();
			xfldPanel.setLayout(new FlowLayout(3));
			
			mXLabel = new JOAJLabel("");
			mXLabel.setFont(tff);
	      	mXLabel.setForeground(java.awt.Color.black);
			xfldPanel.add(mXLabel);
		}
		mNumYlabels = yLabels.length;
		
		mYLabels = new String[mNumYlabels];
		mNumYPlaces = new int[mNumYlabels];
		for (int i=0; i<mNumYlabels; i++) {
			mYLabels[i] = new String(yLabels[i]);
			mNumYPlaces[i] = numYPlaces[i];
		}

		this.setLayout(new FlowLayout(3));
		
		yfldPanel = new JPanel();
		yfldPanel.setLayout(new FlowLayout(3));

		mYLabel = new JOAJLabel("");
		mYLabel.setFont(tff);
      	mYLabel.setForeground(java.awt.Color.black);
		yfldPanel.add(mYLabel);
		this.add(yfldPanel);
		if (mUseX)
			this.add(xfldPanel);
		this.invalidate();
		this.validate();
					
		mComp.addMouseMotionListener(new MouseMotionAdapter() {
			UVCoordinate uv;
			public void mouseDragged(MouseEvent me) {
				uv = mComp.getCorrectedXY(me.getX(), me.getY());
				setLocation(mComp.getInvTransformedX(uv.u), mComp.getInvTransformedY(uv.v));
			}
			
			public void mouseMoved(MouseEvent me) {
				uv = mComp.getCorrectedXY(me.getX(), me.getY());
				setLocation(mComp.getInvTransformedX(uv.u), mComp.getInvTransformedY(uv.v));
			}
		});
	}
	
	public void setXLabels(String[] xLabels, int[] numXPlaces) {
		mNumXLabels = xLabels.length;
		mXLabels = null;
		mNumXPlaces = null;
		mNumXPlaces = new int[mNumXLabels];
		mXLabels = new String[mNumXLabels];
		for (int i=0; i<mNumXLabels; i++) {
			mXLabels[i] = new String(xLabels[i]);
			mNumXPlaces[i] = numXPlaces[i];
		}
	}
	
	public void setYLabels(String[] yLabels, int[] numYPlaces) {
		mNumYlabels = yLabels.length;
		mYLabels = null;
		mYLabels = new String[mNumYlabels];
		mNumYPlaces = null;
		mNumYPlaces = new int[mNumYlabels];
		for (int i=0; i<mNumYlabels; i++) {
			mYLabels[i] = new String(yLabels[i]);
			mNumYPlaces[i] = numYPlaces[i];
		}
	}
   
   public void setX(double xx) {
		if (xx != prevx) {
			if (!Double.isNaN(xx)) 
				xTF.setText(JOAFormulas.formatDouble(xx, 3, false));
			this.prevx = x;
			this.x = xx;
		}
   }
	
	public void setY(double yy) {
		if (yy != prevy) {
			if (!Double.isNaN(yy)) 
				yTF.setText(JOAFormulas.formatDouble(yy, 3, false));
			this.prevy = y;
			this.y = yy;
		}
	}
	
	public void setLocation(double[] xx, double[] yy) {
		String xText = new String("");
		if (mUseX) {
			xText = new String("");
			for (int i=0; i<mNumXLabels; i++) {
				if (!Double.isNaN(xx[i]))
					xText += mXLabels[i] + "=" + JOAFormulas.formatDouble(xx[i], mNumXPlaces[i], false);
				else
					xText += mXLabels[i] + "= ----";
				if (i < mNumXLabels - 1)
					xText += ", ";		
			}
		}
		
		String yText = new String("");
		for (int i=0; i<mNumYlabels; i++) {
			if (!Double.isNaN(yy[i]))
				yText += mYLabels[i] + "=" + JOAFormulas.formatDouble(yy[i], mNumYPlaces[i], false);
			else 
				yText += mYLabels[i] + "= -----";
			if (i < mNumYlabels - 1)
				yText += ", ";		
		}
		
		// now reset the labels
		if (mUseX) {
			mXLabel.setText(xText);
			mXLabel.invalidate();
		}
			
		mYLabel.setText(yText);
		mYLabel.invalidate();
	}
}

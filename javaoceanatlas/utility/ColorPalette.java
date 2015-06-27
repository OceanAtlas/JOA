/*
 * $Id: ColorPalette.java,v 1.2 2005/06/17 18:10:58 oz Exp $
 *
 */

package javaoceanatlas.utility;

import java.awt.*;
import java.io.*;
import org.w3c.dom.*;
import com.ibm.xml.parser.*;

public class ColorPalette {
	String mTitle;
	Color[] mColorEntries = new Color[256];

	public ColorPalette() {
		mColorEntries = new Color[256];
		initBlankPalette();
	}

	public Color getColor(int i) {
		return mColorEntries[i];
	}
	
	public int getNumNonBlackColors() {
		int c = 0;
		for (int i = 0; i < 256; i++) {
			if (isBlack(mColorEntries[i])) {
				break;
			}
			c++;
		}
		return c;
	}
	
	private boolean isBlack(Color c) {
		if (c.getRGB() == -16777216) {
			return true;
		}
		else return false;
	}

	public void setColor(int i, Color inColor) {
		mColorEntries[i] = inColor;
	}

	public void initBlankPalette() {
		for (int i = 0; i < 256; i++) {
			mColorEntries[i] = Color.black;
		}
	}

	public void blend(int start, int end) {
		int numSteps = end - start;

		Color endColor = mColorEntries[end];
		Color startColor = mColorEntries[start];

		double deltaRed = (double)(endColor.getRed() - startColor.getRed()) / (double)numSteps;
		double deltaGreen = (double)(endColor.getGreen() - startColor.getGreen()) / (double)numSteps;
		double deltaBlue = (double)(endColor.getBlue() - startColor.getBlue()) / (double)numSteps;

		int c = 1;
		for (int i = start + 1; i < end; i++) {
			double newRed = (startColor.getRed() + (c * deltaRed)) / 255.0;
			double newGreen = (startColor.getGreen() + (c * deltaGreen)) / 255.0;
			double newBlue = (startColor.getBlue() + (c * deltaBlue)) / 255.0;
			c++;
			mColorEntries[i] = new Color((float)newRed, (float)newGreen, (float)newBlue);
		}
	}

	public void saveAsXML(File file) {
		// save preferences as XML
		try {
			// create a documentobject
			Document doc = (Document)Class.forName("com.ibm.xml.parser.TXDocument").newInstance();

			// make joapreferences the root element
			Element root = doc.createElement("colorpalette");

			// make the framecolor element and add it
			for (int i = 0; i < 256; i++) {
				Element item = doc.createElement("color");
				item.setAttribute("red", String.valueOf(mColorEntries[i].getRed()));
				item.setAttribute("green", String.valueOf(mColorEntries[i].getGreen()));
				item.setAttribute("blue", String.valueOf(mColorEntries[i].getBlue()));
				root.appendChild(item);
			}

			doc.appendChild(root);
			((TXDocument)doc).setVersion("1.0");
			((TXDocument)doc).printWithFormat(new FileWriter(file));
		}
		catch (Exception ex) {

		}
	}
}
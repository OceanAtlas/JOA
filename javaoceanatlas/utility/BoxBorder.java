package javaoceanatlas.utility;

//
//  BoxBorder.java
//  BoxBorderTest
//
//  Created by Florijan Stamenkovic on 23/11/06.
//  Copyright 2006 CNG Havaso Ltd. All rights reserved.
//

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import javax.swing.border.Border;

/**
 *	A <tt>Border</tt> that makes the using <tt>Component</tt> appear like a Mac OS X style box.
 *	This implementation is entirely cross-platform. It is possible to make the border shaded, or
 *	not shaded, the attribute determening if the whole of the component for which the border is
 *	set should be slightly darker then it is normally (the default value  is <tt>true</tt>, since
 *	it is so in a OS X style box.
 *
 @author	Florijan Stamenkovic (flor385@mac.com)
 @version	1.0, 11/24/06
 */
public class BoxBorder implements Border {

  //	determines if the border is shaded or not
  private boolean shaded = true;
  //	insets determined by the images used to render the border
  private Insets insets;

  //	images used in the shaded version
  private BufferedImage shadedTopLeft, shadedTopMiddle, shadedTopRight, shadedMiddleRight, shadedMiddleLeft,
      shadedBottomLeft, shadedBottomMiddle, shadedBottomRight;
  //	images used in the non shaded version
  private BufferedImage emptyTopLeft, emptyTopMiddle, emptyTopRight, emptyMiddleRight, emptyMiddleLeft, emptyBottomLeft,
      emptyBottomMiddle, emptyBottomRight;

  /**
   *	Constructs a shaded <tt>BoxBorder</tt>.
   */
  public BoxBorder() {
    this(true);
  }

  /**
   *	Constructs a <tt>BoxBorder</tt> where the parameter detrmines if
   *	it is shaded or not.
   *
   @param	shaded	Determines if the border is shaded or not.
   */
  public BoxBorder(boolean shaded) {

    try {
      shadedTopLeft = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedTopLeft.png")).getImage());
      shadedTopMiddle = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedTopMiddle.png")).getImage());
      shadedTopRight = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedTopRight.png")).getImage());
      shadedMiddleRight = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedMiddleRight.png")).getImage());
      shadedMiddleLeft = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedMiddleLeft.png")).getImage());
      shadedBottomLeft = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedBottomLeft.png")).getImage());
      shadedBottomMiddle = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedBottomMiddle.png")).getImage());

      shadedBottomRight = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/shadedBottomRight.png")).getImage());

      // images used in the non shaded version
      emptyTopLeft = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyTopLeft.png")).getImage());
      emptyTopMiddle = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyTopMiddle.png")).getImage());
      emptyTopRight = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyTopRight.png")).getImage());
      emptyMiddleRight = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyMiddleRight.png")).getImage());
      emptyMiddleLeft = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyMiddleLeft.png")).getImage());
      emptyBottomLeft = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyBottomLeft.png")).getImage());
      emptyBottomMiddle = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyBottomMiddle.png")).getImage());
      emptyBottomRight = getBufferedImage(new ImageIcon(Class.forName("javaoceanatlas.PowerOceanAtlas").getResource(
          "images/emptyBottomRight.png")).getImage());
    }
    catch (Exception ex) {
    	ex.printStackTrace();
    	System.out.println("BoxBorder:ctor");
    }

    this.shaded = shaded;

    //	calculate the insets
    if (shaded) {
      insets = new Insets(shadedTopMiddle.getHeight(), shadedMiddleLeft.getWidth(), shadedBottomMiddle.getHeight(),
                          shadedMiddleRight.getHeight());
    }
    else {
      insets = new Insets(emptyTopMiddle.getHeight(), emptyMiddleLeft.getWidth(), emptyBottomMiddle.getHeight(),
                          emptyMiddleRight.getHeight());
    }
  }

  /**
   *	Returns the insets of this border.
   *
   @param	c	The component which is using the border,
    a disregarded argument
   */
  public Insets getBorderInsets(Component c) {
    return insets;
  }

  /**
   *	Always returns false, since this border works on the principle of
   *	overlaying the border area with predetermined images and colors
   *	which are not fully opaque.
   *
   @return	Always <tt>false</tt>
   */
  public boolean isBorderOpaque() {
    return false;
  }

  /**
   *	Helper method that fills up the border sides using the loaded
   *	texture.
   *
   @param	g2	The graphics context to paint on
   @param	img	The texture to paint
   @param	x	X coordinate where painting should start in the graphics context
   @param	y	Y coordinate where painting should start in the graphics context
   @param	w	The width of the area that should be painted
   @param	h	The height of the area that should be painted
   */
  private void fillTexture(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
    Rectangle anchor = new Rectangle(x, y, img.getWidth(), img.getHeight());
    TexturePaint paint = new TexturePaint(img, anchor);
    g2.setPaint(paint);

    g2.fillRect(x, y, w, h);
  }

  /**
   *	Paints the border.
   *
   @param	c	The component that is using the border
   @param	g	The graphic context to paint on
   @param	x	The X coordinate where the border should start
   @param	y	The Y coordinate where the border should start
   @param	w	The width of the border (outer edge)
   @param	h	The height of the border (outer edge)
   */
  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

    if (shaded) {
      //	draw the corners
      g.drawImage(shadedTopLeft, x, y, null);
      g.drawImage(shadedTopRight, x + w - insets.right, y, null);
      g.drawImage(shadedBottomLeft, x, y + h - insets.bottom, null);
      g.drawImage(shadedBottomRight, x + w - insets.right, y + h - insets.bottom, null);

      //	draw the sides
      Graphics2D g2 = (Graphics2D)g;
      fillTexture(g2, shadedTopMiddle, x + insets.left, y, w - insets.left - insets.right, insets.top);
      fillTexture(g2, shadedMiddleLeft, x, y + insets.top, insets.left, h - insets.top - insets.bottom);
      fillTexture(g2, shadedMiddleRight, x + w - insets.right, y + insets.top, insets.right,
                  h - insets.top - insets.bottom);
      fillTexture(g2, shadedBottomMiddle, x + insets.left, y + h - insets.bottom, w - insets.left - insets.right,
                  insets.bottom);

      //	paint the shade
      g2.setColor(new Color(shadedBottomMiddle.getRGB(0, 0), true));
      g2.fillRect(x + insets.left, y + insets.top, w - insets.left - insets.right, h - insets.top - insets.bottom);
    }
    else {
      //	draw the corners
      g.drawImage(emptyTopLeft, x, y, null);
      g.drawImage(emptyTopRight, x + w - insets.right, y, null);
      g.drawImage(emptyBottomLeft, x, y + h - insets.bottom, null);
      g.drawImage(emptyBottomRight, x + w - insets.right, y + h - insets.bottom, null);

      //	draw the sides
      Graphics2D g2 = (Graphics2D)g;
      fillTexture(g2, emptyTopMiddle, x + insets.left, y, w - insets.left - insets.right, insets.top);
      fillTexture(g2, emptyMiddleLeft, x, y + insets.top, insets.left, h - insets.top - insets.bottom);
      fillTexture(g2, emptyMiddleRight, x + w - insets.right, y + insets.top, insets.right,
                  h - insets.top - insets.bottom);
      fillTexture(g2, emptyBottomMiddle, x + insets.left, y + h - insets.bottom, w - insets.left - insets.right,
                  insets.bottom);
    }
  }

  public static BufferedImage getBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage)image;
    }

    int oldHeight = image.getHeight(null);
    while (oldHeight == -1) {
      oldHeight = image.getHeight(null);
    }
    int oldWidth = image.getWidth(null);
    while (oldWidth == -1) {
      oldWidth = image.getWidth(null);
    }

    BufferedImage outImage = new BufferedImage(oldWidth, oldHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D)outImage.createGraphics();

    g2.drawImage(image, 0, 0, null);

    g2.dispose();

    return outImage;
  }
}

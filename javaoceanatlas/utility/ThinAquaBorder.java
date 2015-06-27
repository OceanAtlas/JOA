package javaoceanatlas.utility;

//
//  ThinAquaBorder.java
//  HavasoUtilities
//
//  Created by Florijan Stamenkovic on 22/01/07.
//  Copyright 2007 CNG Havaso Ltd. All rights reserved.
//
import java.awt.Color;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

/**
 *	A one-pixel <tt>Border</tt> in Aqua LAF colors, used primarily as
 *	a more elegant choice for a scroll-pane border.
 *
 @author	Florijan Stamenkovic (flor385@mac.com)
 @version	1.0, 01/22/07
 */
@SuppressWarnings("serial")
public class ThinAquaBorder extends CompoundBorder {

  public ThinAquaBorder() {
    super(new MatteBorder(1, 0, 0, 0, new Color(142, 142, 142)), new MatteBorder(0, 1, 1, 1, new Color(190, 190, 190)));
  }
}

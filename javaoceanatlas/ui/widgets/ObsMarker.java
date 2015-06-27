/*
 * $Id: ObsMarker.java,v 1.7 2005/06/17 18:08:55 oz Exp $
 *
 */

package javaoceanatlas.ui.widgets;

import java.awt.*;
import javaoceanatlas.resources.*;
import javaoceanatlas.utility.*;

public class ObsMarker {
  int mCurrX, mCurrY, mOldX, mOldY;
  boolean mFirstDrawn = false;
  int mSymbol = JOAConstants.DEFAULT_CURSOR_SYMBOL;
  int mSize = 4;
  Color mColor = null;

  public ObsMarker(int x, int y, int size, Color c) {
    this(x, y, size);
    mColor = c;
  }

  public ObsMarker(int x, int y, int size) {
    mCurrX = x;
    mCurrY = y;
    mOldX = x;
    mOldY = y;
    mSize = size;
    mSymbol = JOAConstants.DEFAULT_CURSOR_SYMBOL;
  }

  public int getX() {
    return mCurrX;
  }

  public int getY() {
    return mCurrY;
  }

  public void drawMarker(Graphics2D g, boolean flag) {
    if (g == null) {
      return;
    }
    plotObsSymbol(g, mSymbol, mCurrX, mCurrY, mSize);
  }

  public void setNewPos(int x, int y) {
    mCurrX = x;
    mCurrY = y;
  }

  public Point getPos() {
    return new Point(mCurrX, mCurrY);
  }

  public void setSymbol(int symbol) {
    mSymbol = symbol;
  }

  public void setSymbolSize(int size) {
    mSize = size;
  }

  public void plotObsSymbol(Graphics2D g, int symbol, int h, int v, int width) {
    if (mColor != null) {
      g.setColor(mColor);
    }
    else {
      g.setColor(Color.black);
    }

    int x = h;
    int y = v;
    if (symbol == JOAConstants.SYMBOL_SQUARE) {
      g.drawRect(h - width / 2, v - width / 2, width, width);
    }
    else if (symbol == JOAConstants.SYMBOL_SQUAREFILLED) {
      g.fillRect(h - width / 2, v - width / 2, width, width);
    }
    else if (symbol == JOAConstants.SYMBOL_CIRCLE) {
      g.drawOval(h - width / 2, v - width / 2, width, width);
    }
    else if (symbol == JOAConstants.SYMBOL_CIRCLEFILLED) {
      g.fillOval(h - width / 2, v - width / 2, width, width);
      //g.drawOval(h-width/2, v-width/2, width, width);
    }
    else if (symbol == JOAConstants.SYMBOL_DIAMOND) {
      int[] xpoints = {x, x + width / 2, x, x - width / 2};
      int[] ypoints = {y - width / 2, y, y + width / 2, y};
      g.drawPolygon(xpoints, ypoints, 4);
    }
    else if (symbol == JOAConstants.SYMBOL_DIAMONDFILLED) {
      int[] xpoints = {x, x + width / 2, x, x - width / 2};
      int[] ypoints = {y - width / 2, y, y + width / 2, y};
      g.drawPolygon(xpoints, ypoints, 4);
      g.fillPolygon(xpoints, ypoints, 4);
    }
    else if (symbol == JOAConstants.SYMBOL_TRIANGLE) {
      int[] xpoints = {x - width / 2, x + width / 2, x};
      int[] ypoints = {y + width / 2, y + width / 2, y - width / 2};
      g.drawPolygon(xpoints, ypoints, 3);
    }
    else if (symbol == JOAConstants.SYMBOL_TRIANGLEFILLED) {
      int[] xpoints = {x - width / 2, x + width / 2, x};
      int[] ypoints = {y + width / 2, y + width / 2, y - width / 2};
      g.drawPolygon(xpoints, ypoints, 3);
      g.fillPolygon(xpoints, ypoints, 3);
    }
    else if (symbol == JOAConstants.SYMBOL_CROSS1) {
      JOAFormulas.plotThickLine(g, x - width / 2 + 1, y, x + width / 2, y, 2);
      JOAFormulas.plotThickLine(g, x, y - width / 2 + 1, x, y + width / 2, 2);
    }
    else if (symbol == JOAConstants.SYMBOL_CROSS2) {
      g.drawLine(x - width / 2, y - width / 2, x + width / 2, y + width / 2);
      g.drawLine(x - width / 2, y + width / 2, x + width / 2, y - width / 2);
      /*g.translate(1, 0);
          g.drawLine(x-width/2, y-width/2, x+width/2, y+width/2);
          g.drawLine(x-width/2, y+width/2, x+width/2, y-width/2);
          g.translate(-1, 0); */
    }
    else if (symbol == JOAConstants.SYMBOL_HORIZONTAL_BAR_SYMBOL) {
      g.drawLine(x - 4, y, x + 4, y);
    }
    else if (symbol == JOAConstants.SYMBOL_DOWN_ARROW_SYMBOL) {
      int[] xpoints = {x - 5, x + 5, x, x - 5};
      int[] ypoints = {y - 5, y - 5, y, y - 5};
      g.fillPolygon(xpoints, ypoints, 4);
    }
    else if (symbol == JOAConstants.SYMBOL_UP_ARROW_SYMBOL) {
      int[] xpoints3 = {x - 5, x + 5, x, x - 5};
      int[] ypoints3 = {y + 5, y + 5, y, y + 5};
      g.fillPolygon(xpoints3, ypoints3, 4);
    }
    else if (symbol == JOAConstants.SYMBOL_RIGHT_ARROW_SYMBOL) {
      int[] xpoints2 = {x - 5, x, x - 5, x - 5};
      int[] ypoints2 = {y - 5, y, y + 5, y - 5};
      g.fillPolygon(xpoints2, ypoints2, 4);
    }
    else if (symbol == JOAConstants.SYMBOL_LEFT_ARROW_SYMBOL) {
      int[] xpoints4 = {x, x + 5, x + 5, x};
      int[] ypoints4 = {y, y - 5, y + 5, y};
      g.fillPolygon(xpoints4, ypoints4, 4);
    }
    else if (symbol == JOAConstants.SYMBOL_VERTICAL_BAR_SYMBOL) {
      g.drawLine(x, y - 4, x, y + 4);
    }
    g.setColor(Color.black);
  }
}

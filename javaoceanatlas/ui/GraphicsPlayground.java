package javaoceanatlas.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
 

class GraphicsPlayground extends Canvas{
  public GraphicsPlayground(){
      setSize(200, 200);
      setBackground(Color.white);
  }

  public static void main(String[] argS){
       //GraphicsPlayground class is now a type of canvas
        //since it extends the Canvas class
        //lets instantiate it
  	GraphicsPlayground gPlay = new GraphicsPlayground();  

      //create a new frame to which we will add a canvas
      Frame aFrame = new Frame();
      aFrame.setSize(300, 300);
      
      //add the canvas
      aFrame.add(gPlay);
      
      aFrame.setVisible(true);
  }

  public void paint(Graphics g){
  	Graphics2D g2 = (Graphics2D)g;
  	g2.rotate(.1);//3.14159/2.0);
  	
  	g2.setColor(Color.blue);
//      drawTri2(g, 100, 100, 100);
//      drawTriPoly2(g2, 100, 100, 100);
  	g2.drawLine(30, 30, 80, 80);
  	g2.drawRect(20, 150, 100, 100);
  	g2.fillRect(20, 150, 100, 100);
//      
//      g.fillOval(150, 20, 100, 100); 
  }
  
  public void drawTri(Graphics g, int x, int y, int width) {
  	g.drawLine(x, y, x + width/2, y + width/2);
  	g.drawLine(x + width/2, y + width/2, x - width/2, y + width/2);
  	g.drawLine(x - width/2, y + width/2, x, y);
  }
  
	public void drawTri3(Graphics g, int x, int y, int width) {
		int yOffset = -width / 4;
		int p1X = x + width / 2;
		int p1Y = y + yOffset + width / 2;
		int p2X = x - width / 2;
		int p2Y = y + yOffset + width / 2;

		g.drawLine(x-1, y-1, x+1, y+1);
		g.drawLine(x-1, y+1, x+1, y-1);

		g.drawLine(x, y + yOffset, p1X, p1Y);
		g.drawLine(p1X, p1Y, p2X, p2Y);
		g.drawLine(p2X, p2Y, x, y + yOffset);
	}
  
	public void drawTriPoly2(Graphics2D g, int x, int y, int width) {
		Polygon poly = new Polygon();
		int p1X = x + width / 2;
		int p1Y = y + width / 2;
		int p2X = x - width / 2;
		int p2Y = y + width / 2;

		poly.addPoint(x, y);
		poly.addPoint(p1X, p1Y);
		poly.addPoint(p2X, p2Y);
		poly.addPoint(x, y);
		
		g.fillPolygon(poly);
	}
  
  public void drawTri2(Graphics g, int apexX, int aPexY, int width) {
  	int p1X = apexX + width/2;
  	int p1Y	= aPexY + width/2;
  	int p2X = apexX - width/2;
  	int p2Y	= aPexY + width/2;

    	g.drawLine(apexX, aPexY, p1X, p1Y);
    	g.drawLine(p1X, p1Y, p2X, p2Y);
    	g.drawLine(p2X, p2Y, apexX, aPexY);
    }

  
  public void drawTriPoly(Graphics g, int x, int y, int width) {
		int[] xpoints = { (int)x - width / 2, (int)x + width / 2, (int)x };
		int[] ypoints = { (int)y + width / 2, (int)y + width / 2, (int)y - width / 2 };
		g.drawPolygon(xpoints, ypoints, 3);
  }
}
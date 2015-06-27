/*
 * $Id: TimeAxis.java,v 1.14 2001/04/03 18:41:03 dwd Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package  gov.noaa.pmel.sgt;

import gov.noaa.pmel.util.TimeRange;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.Debug;
import gov.noaa.pmel.util.SoTRange;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import javaoceanatlas.utility.*;
import javaoceanatlas.resources.*;

// jdk1.2
//import java.awt.geom.Point2D;

/**
 * Base class for time axes.  A time axis is an axis whose user units
 * are GeoDate objects.
 *
 * @author Donald Denbo
 * @version $Revision: 1.14 $, $Date: 2001/04/03 18:41:03 $
 * @since 1.0
 * @see Axis
 */
public class TimeAxis implements Cloneable {
  /**
   * Place the label and/or tic on the positive side of the axis.
   * The right side of <code>VERTICAL</code> axes and the top of
   * <code>HORIZONTAL</code> axes.
   */
  public static final int POSITIVE_SIDE = 0;
  /**
   * Place the label and/or tic on the negative side of the axis.
   * The left side of <code>VERTICAL</code> axes and the bottom of
   * <code>HORIZONTAL</code> axes.
   */
  public static final int NEGATIVE_SIDE = 1;
  /**
   * Do not draw a label and/or tic.
   */
  public static final int NO_LABEL = 2;
  /**
   * Draw the tics on both sides of the axes.
   */
  public static final int BOTH_SIDES = 2;
  /**
   * Draw a horizontal axis.
   */
  public static final int HORIZONTAL = 0;
  /**
   * Draw a vertical axis.
   */
  public static final int VERTICAL = 1;
  /**
   * Align top of label
   **/
  public static final int TOP = 0;
  /**
   * Align middle of label
   **/
  public static final int MIDDLE = 1;
  /**
   * Align bottom of label
   **/
  public static final int BOTTOM = 2;
  /**
   * Align left of label
   **/
  public static final int LEFT = 0;
  /**
   * Align center of label
   **/
  public static final int CENTER = 1;
  /**
   * Align right of label
   **/
  public static final int RIGHT = 2;
  /**
   * Orient label horizontal
   */
  public static final int ANGLE = 2;
 
  protected TimeRange tRange_;
  protected String minorLabelFormat_;
  protected int minorLabelInterval_;
  protected String majorLabelFormat_;
  protected int majorLabelInterval_;
  protected double yminor_;
  protected double ymajor_;
  protected double xminor_;
  protected double xmajor_;
  protected int vertalign_;
  protected int numSmallTics_;
  protected double largeTicHeight_;
  protected double smallTicHeight_;
  protected double thickTicWidth_;
  protected int ticPosition_;
  protected int labelPosition_;
  protected int labelInterval_;
  protected double labelHeight_;
  protected String labelFormat_;
  protected int orientation_ = HORIZONTAL;
  //
  /**@shapeType AggregationLink
     @associates <b>TimeAxisStyle</b>
     * @supplierCardinality 1
     * @byValue */
  protected TimeAxisStyle txt_;
  protected int axisStyle_;
  static final double TIC_RATIO__ = 1.5;
  static final double TIC_GAP__ = 4.0; //0.05;
  static final double LABEL_RATIO__ = 1.1;
  static final double MAJOR_LABEL_RATIO__ = 1.25;

  static final double defaultLargeTicHeight__ = 8.0;//0.1;
  static final double defaultSmallTicHeight__ = 4.0;//0.05;
  static final int defaultTicPosition__ = POSITIVE_SIDE;
  static final int defaultLabelPosition__ = POSITIVE_SIDE;
  
	protected Font valFont;
    int xloc, yloc, xend, yend;
    Color labelColor;
 
  /**
   * Automatically select the time axis style
   */
  public static final int AUTO = 0;
  /**
   * Use the YearDecadeAxis style.
   * <pre>
   *   |..........|..........|..........|..........|
   *        84         85         86         87
   *                       1980
   * </pre>
   */
  public static final int YEAR_DECADE = 1;
  /**
   * Use the MonthYearAxis style.
   * <pre>
   *   |..........|..........|..........|..........|
   *        Mar        Apr        May       Jun
   *                       1980
   * </pre>
   */
  public static final int MONTH_YEAR = 2;
  /**
   * Use the DayMonthAxis style.
   * <pre>
   *   |..........|..........|..........|..........|
   *        3          4          5           6
   *                        1993-04
   * </pre>
   */
  public static final int DAY_MONTH = 3;
  /**
   * Use the HourDayAxis style.
   * <pre>
   *   |..........|..........|..........|..........|
   *   03         04         05         06         07
   *                     1987-06-07
   * </pre>
   */
  public static final int HOUR_DAY = 4;
  /**
   * Use the MinuteHourAxis style.
   * <pre>
   *   |..........|..........|..........|..........|
   *   15         30         45         00         15
   *                   1987-06-07 13
   * </pre>
   */
  public static final int MINUTE_HOUR = 5;

  private void setAuto() {
    TimeAxisStyle newStyle = null;

    GeoDate delta = tRange_.end.subtract(tRange_.start);
    double days = ((double)Math.abs(delta.getTime()))/((double)GeoDate.MSECS_IN_DAY);
    if(Debug.TAXIS) {
      System.out.println("setAuto: days = " + days);
    }
    if(days > 1000.0) {
      if(!(txt_ instanceof YearDecadeAxis)) {
        newStyle = new YearDecadeAxis();
      }
    } 
    else if(days > 91.0) {
      if(!(txt_ instanceof MonthYearAxis)) {
        newStyle = new MonthYearAxis();
      }
    } 
    else if(days > 5.0) {
      if(!(txt_ instanceof DayMonthAxis)) {
        newStyle = new DayMonthAxis();
      }
    } 
    else if((days > 0.1666667)) {                      // 6 hours
      if(!(txt_ instanceof HourDayAxis)) {
        newStyle = new HourDayAxis();
      }
    } 
    else {
      if(!(txt_ instanceof MinuteHourAxis)) {
        newStyle = new MinuteHourAxis();
      }
    }
    
    if(newStyle != null) {
      txt_ = newStyle;
      //    } else {
      //      return;
    }
    txt_.computeDefaults(delta);

    minorLabelFormat_ = txt_.getDefaultMinorLabelFormat();
    majorLabelFormat_ = txt_.getDefaultMajorLabelFormat();
    minorLabelInterval_ = txt_.getDefaultMinorLabelInterval();
    majorLabelInterval_ = txt_.getDefaultMajorLabelInterval();
    numSmallTics_ = txt_.getDefaultNumSmallTics();
    thickTicWidth_ = 2.0;
	
    if(Debug.TAXIS) {
      System.out.println("    style, ticPosition, labelPostiion = " +
                         txt_.toString() + ", " + ticPosition_ + ", " + labelPosition_);
      System.out.println("    minorFormat, majorFormat, minorInterval, majorInterval = " +
                         minorLabelFormat_ + ", " + majorLabelFormat_ + ", " +
                         minorLabelInterval_ + ", " + majorLabelInterval_);
      System.out.println("    smallTics, largeHgt, smallHgt, labelHgt = " +
                         numSmallTics_ + ", " + largeTicHeight_ + ", " + smallTicHeight_ + ", " + labelHeight_);
    }
  }
  
  protected void setupDraw(double val) {
    if(orientation_ == HORIZONTAL) {
      if(labelPosition_ == POSITIVE_SIDE) {
        vertalign_ = BOTTOM;
        if(minorLabelInterval_ == 0) {
          yminor_ = val;
        } 
        else if(ticPosition_ == BOTH_SIDES || ticPosition_ == POSITIVE_SIDE) {
          yminor_ = val + TIC_RATIO__*largeTicHeight_;
        } 
        else {
          yminor_ = val + TIC_GAP__;
        }
        ymajor_ = yminor_ + LABEL_RATIO__*labelHeight_;
      } 
      else {
        vertalign_ = TOP;
        if(minorLabelInterval_ == 0) {
          yminor_ = val;
        } 
        else if(ticPosition_ == BOTH_SIDES || ticPosition_ == NEGATIVE_SIDE) {
          yminor_ = val - TIC_RATIO__*largeTicHeight_;
        } 
        else {
          yminor_ = val - TIC_GAP__;
        }
        ymajor_ = yminor_ - LABEL_RATIO__*labelHeight_;
      }
    } 
    else {
      if(labelPosition_ == NEGATIVE_SIDE) {
        vertalign_ = BOTTOM;
        if(minorLabelInterval_ == 0) {
          xminor_ = val;
        } 
        else if(ticPosition_ == BOTH_SIDES || ticPosition_ == NEGATIVE_SIDE) {
          xminor_ = val - TIC_RATIO__*largeTicHeight_;
        } 
        else {
          xminor_ = val - TIC_GAP__;
        }
        xmajor_ = xminor_ - LABEL_RATIO__*labelHeight_;
      } 
      else {
        vertalign_ = TOP;
        if(minorLabelInterval_ == 0) {
          xminor_ = val;
        } 
        else if(ticPosition_ == BOTH_SIDES || ticPosition_ == POSITIVE_SIDE) {
          xminor_ = val + TIC_RATIO__*largeTicHeight_;
        } 
        else {
          xminor_ = val + TIC_GAP__;
        }
        xmajor_ = xminor_ + LABEL_RATIO__*labelHeight_;
      }
    }
  }
  
  protected void drawMinorLabel(Graphics g, double val, GeoDate time) {
  	// set the font
    String label;
    if (orientation_ == HORIZONTAL) {
      label = time.toString(minorLabelFormat_);
    } 
    else {
      label = time.toString(minorLabelFormat_); //new Point2D.Double(xminor_, val));
    }
    
    // draw label centered at val
   // g.setColor(labelColor);
    //g.setFont(valFont);
	FontMetrics fm = g.getFontMetrics();
	int strWidth = fm.stringWidth(label);
   // g.drawString(label, (int)(val - strWidth/2), (int)yminor_);
    
	JOAFormulas.drawStyledString(label,  (int)(val - strWidth/2), (int)yminor_, (Graphics2D)g, 0.0, JOAConstants.DEFAULT_AXIS_VALUE_FONT, JOAConstants.DEFAULT_AXIS_VALUE_SIZE, 
	JOAConstants.DEFAULT_AXIS_VALUE_STYLE, JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
	g.setColor(Color.black);
  }
  
  protected void drawMajorLabel(Graphics g,double val,GeoDate time) {
  	//set the font
    g.setColor(labelColor);
    String label;
    if (orientation_ == HORIZONTAL) {
      label = time.toString(majorLabelFormat_);
    } 
    else {
      label = time.toString(majorLabelFormat_);// new Point2D.Double(xmajor_, val));
    }
    
    // draw label centered at val
    //g.setFont(valFont);
	FontMetrics fm = g.getFontMetrics();
	int strWidth = fm.stringWidth(label);
    //g.drawString(label, (int)(val - strWidth/2), (int)ymajor_);
    
	JOAFormulas.drawStyledString(label,  val - strWidth/2, ymajor_, (Graphics2D)g, 0.0, JOAConstants.DEFAULT_AXIS_VALUE_FONT, (int)(JOAConstants.DEFAULT_AXIS_VALUE_SIZE * MAJOR_LABEL_RATIO__), 
	JOAConstants.DEFAULT_AXIS_VALUE_STYLE, JOAConstants.DEFAULT_AXIS_VALUE_COLOR);
	g.setColor(Color.black);
  }
  //
  /**
   * Default contructor.
   **/
  public TimeAxis(int style) {
    this("", style);
  }
  /**
   * TimeAxis constructor.
   *
   * @param id axis identifier
   **/
  public TimeAxis(String id,int style) {
    minorLabelInterval_ = 2;
    majorLabelInterval_ = 1;
    numSmallTics_ = 0;
    axisStyle_ = style;
    //
    if(axisStyle_ == AUTO || axisStyle_ == MONTH_YEAR) {
      txt_ = new MonthYearAxis();
    } 
    else if(axisStyle_ == YEAR_DECADE) {
      txt_ = new YearDecadeAxis();
    } 
    else if(axisStyle_ == DAY_MONTH) {
      txt_ = new DayMonthAxis();
    } 
    else if(axisStyle_ == HOUR_DAY) {
      txt_ = new HourDayAxis();
    } 
    else {
      txt_ = new MinuteHourAxis();
    }
    minorLabelFormat_ = txt_.getDefaultMinorLabelFormat();
    majorLabelFormat_ = txt_.getDefaultMajorLabelFormat();
    minorLabelInterval_ = txt_.getDefaultMinorLabelInterval();
    majorLabelInterval_ = txt_.getDefaultMajorLabelInterval();
    numSmallTics_ = txt_.getDefaultNumSmallTics();

    largeTicHeight_ = defaultLargeTicHeight__;
    smallTicHeight_ = defaultSmallTicHeight__;
    ticPosition_ = defaultTicPosition__;
    labelPosition_ = defaultLabelPosition__;
    tRange_ = null;
  }
  
  /**
   * Set the minor and major label formats.
   *
   * @param minor minor label format
   * @param major major label format
   * !! zero refs!!
   **/
  public void setLabelFormat(String minor,String major) {
    if(minorLabelFormat_ == null ||
       majorLabelFormat_ == null ||
      !minorLabelFormat_.equals(minor) ||
      !majorLabelFormat_.equals(major)) {

      minorLabelFormat_ = minor;
      majorLabelFormat_ = major;
    }
  }
  
  /**
   * Set the minor label format.
   *
   * @param minor minor label format
   * !! zero refs!!
   **/
  public void setMinorLabelFormat(String minor) {
    if(minorLabelFormat_ == null || !minorLabelFormat_.equals(minor)) {
      minorLabelFormat_ = minor;
    }
  }
  
  /**
   * Set the major label format.
   *
   * @param major major label format
   * !! zero refs!!
   **/
  public void setMajorLabelFormat(String major) {
    if(majorLabelFormat_ == null || !majorLabelFormat_.equals(major)) {
      majorLabelFormat_ = major;
    }
  }
  
  /**
   * Get the minor label format.
   *
   * @return minor label format
   * !! zero refs!!
   **/
  public String getMinorLabelFormat() {
    return minorLabelFormat_;
  }
  
  /**
   * Get the major label format.
   *
   * @return major label format
   * !! zero refs!!
   **/
  public String getMajorLabelFormat() {
    return majorLabelFormat_;
  }
  
  /**
   * Set the minor and major label intervals.
   *
   * @param minor minor label interval
   * @param major major label interval
   * !! zero refs!!
   **/
  public void setLabelInterval(int minor,int major) {
    if(minorLabelInterval_ != minor || majorLabelInterval_ != major) {
      minorLabelInterval_ = minor;
      majorLabelInterval_ = major;
    }
  }
  
  /**
   * Set the minor label interval.
   *
   * @param minor minor label interval
   * !! zero refs!!
   **/
  public void setMinorLabelInterval(int minor) {
    if(minorLabelInterval_ != minor) {
      minorLabelInterval_ = minor;
    }
  }
  
  /**
   * Set the major label interval.
   *
   * @param major major label interval
   * !! zero refs!!
   **/
  public void setMajorLabelInterval(int major) {
    if(majorLabelInterval_ != major) {
      majorLabelInterval_ = major;
    }
  }
  
  /**
   * Get the minor label interval.
   *
   * @return minor label interval
   * !! zero refs!!
   **/
  public int getMinorLabelInterval() {
    return minorLabelInterval_;
  }
  
  /**
   * Get the major label interval.
   *
   * @return major label interval
   * !! zero refs!!
   **/
  public int getMajorLabelInterval() {
    return majorLabelInterval_;
  }
  
  /**
   * Set the time axis style.
   *
   * @param style new time axis style
   * !! zero refs!!
   */
  public void setStyle(int style) {
    if(axisStyle_ != style) {
      axisStyle_ = style;
      if(axisStyle_ == AUTO && tRange_ != null) {
        setAuto();
      }
    }
  }
  
  /**
   * Get the time axis style.
   *
   * @return time axis style
   * !! zero refs!!
   */
  public int getStyle() {
    return axisStyle_;
  }
  
  /**
   * Set the user range to draw the axis.  Registered Axes and Transforms
   * will be updated.
   *
   * @param tr TimeRange of axis.
   **/
  public void setRangeU(TimeRange tr) {
    if(tRange_ == null || !tRange_.equals(tr)) {
      tRange_ = tr;
      if(axisStyle_ == AUTO) {
        setAuto();
      }
    }
  }
  
  public void setFont(String font, int style, int size, Color c) {
  	valFont = new Font(font, size, style);
  	labelColor = c;
  	labelHeight_ = size;
  }
  
  public void setAxisOrigin(int x, int y) {
  	xloc = x;
  	yloc = y;
  	yend = y;
  }
  
  public void setAxisLength(int len) {
  	xend = xloc + len;
  }
  
  /**
   * Get the time range of the axis.
   *
   * @return TimeRange of axis
   * !! zero refs!!
   **/
  public TimeRange getTimeRangeU() {
    return tRange_;
  }
  
  public void setRangeU(SoTRange tr) {
    GeoDate start = ((SoTRange.GeoDate)tr).start;
    GeoDate end = ((SoTRange.GeoDate)tr).end;
    GeoDate delta = ((SoTRange.GeoDate)tr).delta;
    setRangeU(new TimeRange(start, end, delta));
  }

  // !! zero refs!!
  public SoTRange getSoTRangeU() {
    return new SoTRange.GeoDate(tRange_);
  }
  
  public void draw(Graphics2D g) {
    int minor_val, minor_val_old;
    int major_val, major_val_old;
    double xp, yp;
    double xp_minor_old, yp_minor_old;
    double x, y;
    double xp_major_old, yp_major_old;
    boolean draw_minor, draw_major;
    boolean time_increasing;
    GeoDate time = new GeoDate();
    GeoDate minor_time_old;
    GeoDate major_time_old;
    GeoDate time_end = new GeoDate();

    g.setColor(Color.black);

    draw_minor = minorLabelInterval_ != 0 && labelPosition_ != NO_LABEL;
    draw_major = majorLabelInterval_ != 0 && labelPosition_ != NO_LABEL;

    time_increasing = tRange_.end.after(tRange_.start);
    
    if(time_increasing) {
    	time_end = new GeoDate(tRange_.end);
    } 
    else {
    	time_end = new GeoDate(tRange_.start);
    }

    time = txt_.getStartTime(tRange_);
    int width = xend - xloc;

	if (orientation_ == HORIZONTAL) {
    	// draw the axis
		g.drawLine(xloc, yloc, xend, yloc);
      
      	// first tic can fall on the origin 
      	yp = yloc;
      	xp = xloc;
		
		// this call sets up the offsets from the axis for the tic marks
      	setupDraw(yp + 10);
      	
      	// scale the x axis
		double xScale = (double)width/(double)(tRange_.end.getTime() - tRange_.start.getTime());

        xp = xloc + xScale * (time.getTime() - tRange_.start.getTime());
        
		if (txt_.isStartOfMinor(time)) {
			drawThickXTic(g, xp, yp, 1.3f*largeTicHeight_);
		} 
		else {
			drawXTic(g, xp, yp, largeTicHeight_);
		}
      
      major_val = txt_.getMajorValue(time);
      major_val_old = major_val;
      minor_val_old = txt_.getMinorValue(time);
      xp_major_old = xp;
      xp_minor_old = xp;
      minor_time_old = new GeoDate(time);
      major_time_old = new GeoDate(time);
      time.increment(txt_.getIncrementValue(), txt_.getIncrementUnits());

	  // now plot the rest of the tic marks
      while (time.before(time_end)) {
      	// convert time to an xcoordinate
        xp = xloc + xScale * (time.getTime() - tRange_.start.getTime());
        
        minor_val = txt_.getMinorValue(time);
        if(txt_.isStartOfMinor(time)) {
        	drawThickXTic(g, xp, yp, 1.3f*largeTicHeight_);
        } 
        else {
        	drawXTic(g, xp, yp, largeTicHeight_);
        }
        
        if(draw_minor && minor_val_old % minorLabelInterval_ == 0) {
          x = txt_.computeLocation(xp_minor_old, xp);
          drawMinorLabel(g, x, minor_time_old);
        }
        
        major_val = txt_.getMajorValue(time);
        if(major_val != major_val_old) {
          if(draw_major && major_val_old%majorLabelInterval_ == 0) {
            x = (xp_major_old + xp)*0.5;
            drawMajorLabel(g, x, major_time_old);
          }
          xp_major_old = xp;
          major_val_old = major_val;
          major_time_old = new GeoDate(time);
        }
        xp_minor_old = xp;
        minor_val_old = minor_val;
        minor_time_old = new GeoDate(time);
        time.increment(txt_.getIncrementValue(), txt_.getIncrementUnits());
      } // end of while
      
      if(draw_major && major_val_old%majorLabelInterval_ == 0) {
        GeoDate delta = time_end.subtract(major_time_old);
        if(txt_.isRoomForMajorLabel(delta)) {
          // convert at end time
          xp = xScale * time_end.getTime();
          x = (xp_major_old + xp)*0.5;
          drawMajorLabel(g, x, major_time_old);
        }
      }
    } 
    else {          // vertical axis
      xloc = 10;//graph_.getXUtoD(tLocation_.x);
      yloc = 10;//graph_.getYUtoD(tRange_.start);
      yend = 10;//graph_.getYUtoD(tRange_.end);
      g.drawLine(xloc, yloc, xloc, yend);
      //
      xp = 10;//graph_.getXUtoP(tLocation_.x);
      yp = 10;//graph_.getYUtoP(time);

      setupDraw(xp);

      if(txt_.isStartOfMinor(time)) {
        drawThickYTic(g, xp, yp, 1.3f*largeTicHeight_);
      } else {
        drawYTic(g, xp, yp, largeTicHeight_);
      }
      major_val = txt_.getMajorValue(time);
      major_val_old = major_val;
      minor_val_old = txt_.getMinorValue(time);
      yp_major_old = yp;
      yp_minor_old = yp;
      minor_time_old = new GeoDate(time);
      major_time_old = new GeoDate(time);
      time.increment(txt_.getIncrementValue(), txt_.getIncrementUnits());
      //
      while (time.before(time_end)) {
        yp = 10;//graph_.getYUtoP(time);
        minor_val = txt_.getMinorValue(time);
        if(txt_.isStartOfMinor(time)) {
          drawThickYTic(g, xp, yp, 1.3f*largeTicHeight_);
        } else {
          drawYTic(g, xp, yp, largeTicHeight_);
        }
        if(draw_minor && minor_val_old%minorLabelInterval_ == 0) {
          y = txt_.computeLocation(yp_minor_old, yp);
          drawMinorLabel(g, y, minor_time_old);
        }
        major_val = txt_.getMajorValue(time);
        if(major_val != major_val_old) {
          if(draw_major && major_val_old%majorLabelInterval_ == 0) {
            y = (yp_major_old + yp)*0.5;
            drawMajorLabel(g, y, major_time_old);
          }
          yp_major_old = yp;
          major_val_old = major_val;
          major_time_old = new GeoDate(time);
        }
        yp_minor_old = yp;
        minor_val_old = minor_val;
        minor_time_old = new GeoDate(time);
        time.increment(txt_.getIncrementValue(), txt_.getIncrementUnits());
      } // end of while
      if(draw_major && major_val_old%majorLabelInterval_ == 0) {
        GeoDate delta = time_end.subtract(major_time_old);
        if(txt_.isRoomForMajorLabel(delta)) {
          yp = 10;//graph_.getYUtoP(time_end);
          y = (yp_major_old + yp)*0.5;
          drawMajorLabel(g, y, major_time_old);
        }
      }
    }
  }
  
  protected void drawXTic(Graphics ig,double xp,double yp,double ticHeight) {
  	Graphics2D g = (Graphics2D)ig;
    double yp0, yp1;
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == POSITIVE_SIDE) {
      yp0 = yp + ticHeight;
    } 
    else {
      yp0 = yp;
    }
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == NEGATIVE_SIDE) {
      yp1 = yp - ticHeight;
    } 
    else {
      yp1 = yp;
    }
    
    g.drawLine((int)xp, (int)yp0, (int)xp, (int)yp1);
  }

  protected void drawThickXTic(Graphics2D g,double xp,double yp,double ticHeight) {
    double x0, x1, y0, y1, xc;
    double ticW, ticH;
    double yp0, yp1;
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == POSITIVE_SIDE) {
      yp0 = yp + ticHeight;
    } 
    else {
      yp0 = yp;
    }
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == NEGATIVE_SIDE) {
      yp1 = yp - ticHeight;
    }
    else {
      yp1 = yp;
    }

    xc = xp;
    y0 = yp0;
    y1 = yp1;
    
    x0 = xp - thickTicWidth_/2.0;
    x1 = xp + thickTicWidth_/2.0;
    if((x1-x0) < 3) {
      x0 = xc - 1;
      x1 = xc + 1;
    }
    ticW = x1 - x0;
    ticH = y1 - y0;
	g.drawLine((int)x0-1, (int)y0, (int)x0-1, (int)y1);
	g.drawLine((int)x0, (int)y0, (int)x0, (int)y1);
	g.drawLine((int)x0+1, (int)y0, (int)x0+1, (int)y1); 
  }

  protected void drawYTic(Graphics2D g,double xp,double yp,double ticHeight) {
    double xp0, xp1;
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == POSITIVE_SIDE) {
      xp0 = xp + ticHeight;
    } 
    else {
      xp0 = xp;
    }
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == NEGATIVE_SIDE) {
      xp1 = xp - ticHeight;
    } 
    else {
      xp1 = xp;
    }
    
    g.drawLine((int)xp0, (int)yp, (int)xp1, (int)yp);
  }

  protected void drawThickYTic(Graphics g,double xp,double yp,double ticHeight) {
    double xp0, xp1;
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == POSITIVE_SIDE) {
      xp0 = xp + ticHeight;
    } 
    else {
      xp0 = xp;
    }
    
    if(ticPosition_ == BOTH_SIDES || ticPosition_ == NEGATIVE_SIDE) {
      xp1 = xp - ticHeight;
    } 
    else {
      xp1 = xp;
    }
    
    g.drawLine((int)xp0, (int)yp-1, (int)xp1, (int)yp-1);
    g.drawLine((int)xp0, (int)yp, (int)xp1, (int)yp);
    g.drawLine((int)xp0, (int)yp+1, (int)xp1, (int)yp+1);
  }
}


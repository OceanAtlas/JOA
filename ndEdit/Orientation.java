/*
 * $Id: Orientation.java,v 1.2 2005/02/15 18:31:10 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

/**
 * Constants for orientations (and alignments).<p>
 *
 * This class may not be instantiated.
 *
 * @version 1.0, Apr 11 1996
 * @author  David Geary
 */
public class Orientation {
    public static final Orientation BAD    = new Orientation();
    public static final Orientation NORTH  = new Orientation();
    public static final Orientation SOUTH  = new Orientation();
    public static final Orientation EAST   = new Orientation();
    public static final Orientation WEST   = new Orientation();
    public static final Orientation CENTER = new Orientation();
    public static final Orientation TOP    = new Orientation();
    public static final Orientation LEFT   = new Orientation();
    public static final Orientation RIGHT  = new Orientation();
    public static final Orientation BOTTOM = new Orientation();

    public static final Orientation HORIZONTAL = 
        new Orientation();
    public static final Orientation VERTICAL   = 
        new Orientation();

    static public Orientation fromString(String s) {
        Orientation o = BAD;

        if(s.equals("NORTH") || s.equals("north"))    o = NORTH;
        else if(s.equals("SOUTH") || s.equals("south"))    
            o = SOUTH;
        else if(s.equals("EAST")  || s.equals("east"))     
            o = EAST;
        else if(s.equals("WEST")  || s.equals("west"))     
            o = WEST;
        else if(s.equals("CENTER") || s.equals("center"))   
            o = CENTER;
        else if(s.equals("TOP")   || s.equals("top"))      
            o = TOP;
        else if(s.equals("LEFT")  || s.equals("left"))     
            o = LEFT;
        else if(s.equals("RIGHT")  || s.equals("right"))    
            o = RIGHT;
        else if(s.equals("BOTTOM") || s.equals("bottom"))   
            o = BOTTOM;
        else if(s.equals("VERTICAL") || s.equals("vertical")) 
            o = VERTICAL;
        else if(s.equals("HORIZONTAL") || 
                s.equals("horizontal"))
          o = HORIZONTAL;

        return o;
    }
    public String toString() {
        String s = new String();

        if(this == Orientation.NORTH) 
            s = getClass().getName() + "=NORTH";
        else if(this == Orientation.SOUTH)
            s = getClass().getName() + "=SOUTH";
        else if(this == Orientation.EAST)
            s = getClass().getName() + "=EAST";
        else if(this == Orientation.WEST)
            s = getClass().getName() + "=WEST";
        else if(this == Orientation.CENTER)
            s = getClass().getName() + "=CENTER";
        else if(this == Orientation.TOP)
            s = getClass().getName() + "=TOP";
        else if(this == Orientation.LEFT)
            s = getClass().getName() + "=LEFT";
        else if(this == Orientation.RIGHT)
            s = getClass().getName() + "=RIGHT";
        else if(this == Orientation.BOTTOM)
            s = getClass().getName() + "=BOTTOM";
        else if(this == Orientation.HORIZONTAL)
            s = getClass().getName() + "=HORIZONTAL";
        else if(this == Orientation.VERTICAL)
            s = getClass().getName() + "=VERTICAL";
        else if(this == Orientation.BAD)
            s = getClass().getName() + "=BAD";

        return s;
    }
    private Orientation() { }  // Defeat instantiation
}


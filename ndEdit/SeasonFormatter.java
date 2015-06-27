/*
 * $Id: SeasonFormatter.java,v 1.2 2005/02/15 18:31:10 oz Exp $
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

import java.awt.*;
import java.text.*;
import java.util.*;

public class SeasonFormatter extends SimpleDateFormat {
	public SeasonFormatter() {
		super();
	}
	
	public StringBuffer format(Date date, StringBuffer sb, FieldPosition pos) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int day = cal.get(Calendar.DAY_OF_YEAR);
		if (day < 79 || day >= 355) 
			return new StringBuffer("Winter");
		if (day >= 79 && day < 170)
			return new StringBuffer("Spring");
		if (day >= 170 && day < 264)
			return new StringBuffer("Summer");
		if (day >= 264 && day < 355)
			return new StringBuffer("Autumn");
		return null;
	}

}

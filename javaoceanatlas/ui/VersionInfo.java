/**
 * 
 */
package javaoceanatlas.ui;
/*
 * $Id: VersionInfo.java 3716 2008-09-17 21:04:32Z dwd $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

/**
 *
 * @author Donald Denbo
 * @version 2.0
 * @since 1.0
 */
public final class VersionInfo {
  private final static String CLIENT_VERSION = "5.2.2";
  private final static String VERSION_DATE = "November 22, 2014 10:42 AM PST";
  private final static String VERSION_SHORT_DATE = "November 22, 2014";
  private final static String REVISION = "$Revision: 3747 $";
  private static String revision_;
  private static String version_;
 
  static {
    revision_ = REVISION.substring(REVISION.indexOf(":")+1, REVISION.length()-1).trim();
    version_ = CLIENT_VERSION;
  }
  
  static public String getVersion( ) {
    return version_;
  }

  static public String getVersionDate( ) {
    return VERSION_DATE;
  }

  static public String getVersionShortDate( ) {
    return VERSION_SHORT_DATE;
  }

  static public String getRevision() {
    return revision_;
  }

  static public String getDescription() {
    return "SIFT version " + version_ + " " + VERSION_DATE;
  }
}

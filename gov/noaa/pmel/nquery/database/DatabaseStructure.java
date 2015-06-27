/**
 *  $Id: DatabaseStructure.java,v 1.7 2004/09/14 19:11:49 oz Exp $
 */

package gov.noaa.pmel.nquery.database;

import java.util.Hashtable;

/*
 * $Id: DatabaseStructure.java,v 1.7 2004/09/14 19:11:49 oz Exp $
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
 * <code>DatabaseStructure</code>
 *
 * @author oz & Kevin McHugh
 * @version 1.0
 */

public class DatabaseStructure {
  Hashtable<String, String> mAddedVariables;
  String databaseName = new String();

  public DatabaseStructure(String dbName, Hashtable<String, String> addedVars) {
    databaseName = new String(dbName);
    mAddedVariables = addedVars;
  }

  public DatabaseStructure(String dbName) {
    databaseName = new String(dbName);
    mAddedVariables = new Hashtable<String, String>();
  }

  public String getDBName() {
    return databaseName;
  }

  public Hashtable<String, String> getAddedVars() {
    return mAddedVariables;
  }

  public void addEntry(String varName, String dbColName) {
    mAddedVariables.put(varName, dbColName);
  }

  public String getEntry(String varName) {
    return (String)mAddedVariables.get(varName);
  }
}

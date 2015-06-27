/*
 * $Id: DatabaseTools.java,v 1.40 2005/10/18 23:43:20 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package gov.noaa.pmel.nquery.database;

import gov.noaa.pmel.nquery.utility.Logger;
import java.util.Vector;
import java.util.Iterator;
import gov.noaa.pmel.nquery.utility.ProfileResults;
import gov.noaa.pmel.nquery.utility.SingleProfileResult;
import java.io.File;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.awt.Toolkit;

/**
 * <code>DatabaseTools</code> Routines for creation and accessing an SQL database
 *
 * @author Kevin McHugh
 * @version 1.0
 */

public class DatabaseTools {
  private static boolean DEBUG = false;
  private static Logger mLogger;
  private static String mDBURI;
  private static String mDBUserName;
  private static String mDBPassword;
  private static String mDBPort;

  public DatabaseTools() {
    try {
      String driver = "com.mysql.jdbc.Driver";
      // load the database driver
      Class.forName(driver);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setUpAuthentication(String uri, String port, String user, String pass) {
    mDBURI = new String(uri);
    mDBPort = new String(port);
    mDBUserName = new String(user);
    mDBPassword = new String(pass);
  }

  public static void main(String args[]) {
    try {
      if (isDatabase("nquerytrial")) {
        dropDatabase("nquerytrial");
        createDatabase("nquerytrial");
      }
      else {
        createDatabase("nquerytrial");
      }
    }

    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setDebugMode(boolean b) {
    DEBUG = b;
  }

  public static void setLogger(Logger l) {
    mLogger = l;
  }

  /**
   * Execute the sql command
   *
   * @param sql String
   */
  public static boolean setSQL(String sql) throws Exception {
    try {
      Connection c = createConnection();
      Statement s = c.createStatement();
      s.execute(sql);
      closeConnection(c);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Connection " + c + " closed", true);
      }
      else if (DEBUG) {
        System.out.println("Connection " + c + " closed");
      }
      return true;
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   * Execute the sql command and return the number of valid values in the DB
   *
   * @param cols Vector
   * @param dbname String
   */
  public static int getNumberValidValues(Vector<String> cols, String dbname) throws Exception {
    int stt = 0;
    String sql = new String();
    try {
      String st = new String("SELECT ");
      Connection c = createConnection();
      Statement s = (Statement)c.createStatement();
      for (int i = 0; i < cols.size(); i++) {
        if (i == 0) {
          sql = "COUNT(" + cols.elementAt(i) + ")";
        }
        else {
          sql = sql + " + COUNT(" + cols.elementAt(i) + ")";
        }
      }
      //System.out.println(st + sql + " FROM data");
      s.execute("USE " + dbname);
      s.execute(st + sql + " FROM data");
      ResultSet rs = s.getResultSet();
      rs.next();
      stt = rs.getInt(1);
      //System.out.println(stt);
      closeConnection(c);
      //}
      return stt;
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   * Execute the sql command and return a ResultSet.  Designed specifically for isDatabase()
   * and isTable()
   *
   * @param dtc Integer value of what sql command to pass
   * @param sql String database name to be utilized when finding tables and columns
   * @param tableName String table name to be used when describing a table
   */
  public static ResultSet setSQL(Integer dtc, String newDB, String tableName) throws Exception {
    try {
      Integer db = new Integer(1);
      Integer tb = new Integer(2);
      Integer co = new Integer(3);
      Connection c = createConnection();
      Statement s = (Statement)c.createStatement();
      if (db.equals(dtc)) {
        s.execute("SHOW DATABASES;");
        ResultSet rs = (ResultSet)s.getResultSet();
        return rs;
      }
      else if (tb.equals(dtc)) {
        s.execute("USE " + newDB);
        s.execute("SHOW TABLES");
        ResultSet rs = (ResultSet)s.getResultSet();
        return rs;
      }
      else if (co.equals(dtc)) {
        s.execute("USE " + newDB);
        s.execute("DESCRIBE " + tableName);
        ResultSet rs = (ResultSet)s.getResultSet();
        return rs;
      }
      closeConnection(c);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Connection " + c + " closed", true);
      }
      else if (DEBUG) {
        System.out.println("Connection " + c + " closed");
      }
      return null;
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   *Execute the sql command and return a ResultSet.  Designed specifically for isColumn()
   *
   * @param sql MySQL command to send
   * @param newDB Database name to be used
   */
  public static ResultSet setSQL(String sql, String newDB) throws Exception {
    Connection c = createConnection(newDB);
    Statement s = (Statement)c.createStatement();
    ResultSet rs = null;
    try {
      s.execute("USE " + newDB);
      s.execute(sql);
      rs = (ResultSet)s.getResultSet();
      closeConnection(c);
    }
    catch (Exception e) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Threw exception in setSQL. Refer to console output.", true);
      }
      else if (DEBUG) {
        e.printStackTrace();
      }

      if (e.toString().indexOf("MysqlDataTruncation") >= 0) {
	;// eat the exception: work around for floating point bug in MySQL
      }
      else
	throw e;
    }
    return rs;
  }

  /**
   *Execute the sql SELECT command and return a ResultSet.
   *
   * @param sql MySQL command to send
   * @param newDB Database name to be used
   */
  public static ResultSet selectData(String selectCmd, String newDB) throws Exception {
    Connection c = createConnection(newDB);
    Statement s = (Statement)c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    try {
      if (mLogger != null) {
        mLogger.logMessage("Executing Query: " + selectCmd, true);
      }
      s.execute(selectCmd);
      ResultSet rs = (ResultSet)s.getResultSet();
      //closeConnection(c);
      return rs;
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   *Execute a user defined command and return a ResultSet.
   *
   * @param sql MySQL command to send
   * @param newDB Database name to be used
   */
  public static void customCommand(String theCmd, String newDB) throws Exception {
    Connection c = createConnection(newDB);
    Statement s = (Statement)c.createStatement();

      if (mLogger != null) {
	mLogger.logMessage("Executing Command: " + theCmd, true);
      }

    if (theCmd.indexOf("drop") >= 0 && theCmd.indexOf("database") >= 0 && theCmd.indexOf(newDB) >=  0) {
      Toolkit.getDefaultToolkit().beep();
      if (mLogger != null) {
	mLogger.logMessage("Sorry, you aren't allowed to drop the current database!", true);
      }
      return;
    }

    try {
      s.execute(theCmd);
      ResultSet rs = (ResultSet)s.getResultSet();
      rs.next();
      if (mLogger != null) {
        do {
          String st = rs.getString(1);
          if (mLogger != null) {
            mLogger.logMessage(st, true);
          }
          else if (DEBUG) {
            System.out.println(st);
          }
        }
        while (rs.next());
      }
      closeConnection(c);
    }
    catch (Exception e) {
      // present the error in the console
      if (mLogger != null) {
        mLogger.logMessage(e.getMessage(), true);
      }
      else {
        throw e;
      }
    }
  }

  /**
   * Create a named database.
   *
   * @param newDB Name of database to create
   */
  public static void createDatabase(String newDB) throws Exception {
    try {
      String sql = new String("CREATE DATABASE " + newDB);
      setSQL(sql);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Creating " + newDB, true);
      }
      else if (DEBUG) {
        System.out.println("Creating " + newDB);
      }
    }
    catch (Exception ex) {
      throw ex;
    }
  }

  /**
   * method to use the chosen database.
   *
   * @param newDB Name of database to use
   */
  public static void selectDatabase(String newDB) throws Exception {
    try {
      String sql = new String("USE " + newDB);
      setSQL(sql);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Using " + newDB, true);
      }
      else if (DEBUG) {
        System.out.println("Using " + newDB);
      }
    }
    catch (Exception ex) {
      throw ex;
    }
  }

  /**
   * Create a table using a string value
   *
   * @param newDB Name of database which a table will be created in
   * @param tableName Name of table to create in current database
   * @param columnName Name of column to be created
   * @param columnType SQL type the column is to be created as
   */
  public static void createTable(String newDB, String tableName, String columnName, String columnType, boolean index) throws
      Exception {
    // table stuff
    try {
      String sql;
      if (index) {
        sql = new String("CREATE TABLE " + tableName + " (" + columnName + " " + columnType + ", PRIMARY KEY (" +
                         columnName + "))");
      }
      else {
        sql = new String("CREATE TABLE " + tableName + " (" + columnName + " " + columnType + ")");
      }

      setSQL(sql, newDB);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Create Table sql = " + sql, true);
        mLogger.logMessage("Table " + tableName + " created", true);
        mLogger.logMessage("Column " + columnName + " (" + columnType + ") created", true);
      }
      else if (DEBUG) {
        System.out.println("sql = " + sql);
        System.out.println("Table " + tableName + " created");
        System.out.println("Column " + columnName + " (" + columnType + ") created");
      }
    }
    catch (Exception ex) {
      throw ex;
    }
  }

  /**
   * Create a column using a string value
   *
   * @param newDB Name of database column will be created in
   * @param tableName Name of table to create column in
   * @param columnName Name of column to create in current table
   * @param sqlType data type of created column
   */
  public static void createColumn(String newDB, String tableName, String columnName, String columnType) throws
      Exception {
    try {
      String sql = new String("ALTER TABLE " + tableName + " ADD COLUMN (" + columnName + " " + columnType + ")");
      setSQL(sql, newDB);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("createColumn sql = " + sql, true);
        mLogger.logMessage("Column " + columnName + " (" + columnType + ") created", true);
      }
      else if (DEBUG) {
        System.out.println("Column " + columnName + " (" + columnType + ") created");
      }
    }
    catch (Exception ex) {
      throw ex;
    }
  }

  /**
   * Create a column using a vector list of field names, and sql types associated
   * with those field names.
   *
   * @param newDB Name of database column will be created in
   * @param tableName Name of table to create column in
   * @param variableVector Vector of variables [column names] to be created
   * @param sqltypeVector Vector of associated sql column types with variableVector
   */
  public static int createColumn(String newDB, String tableName, Vector<String> variableVector, Vector<String> sqltypeVector) throws
      Exception {
    //if(variableVector.size() == null) return false;
    String qry = new String("ALTER TABLE " + tableName);
    try {
      for (int i = 0; i < variableVector.size(); i++) {
        String v = ((String)variableVector.get(i)).trim();
        String t = ((String)sqltypeVector.get(i)).trim();
        qry = qry + " , ADD COLUMN " + v + " " + t;
      }
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("createColumn with vector, sql = " + qry, true);
      }
      setSQL(qry, newDB);
      return variableVector.size();
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   * Create a column using a vector list of field names, and sql types associated
   * with those field names for the table "item"
   *
   * @param newDB Name of database column will be created in
   * @param tableName Name of table to create column in
   * @param variableVector Vector of variables [column names] to be created
   * @param sqltypeVector Vector of associated sql column types with variableVector
   */
  public static int createColumnitem(String newDB, String tableName, Vector<String> variableVector, Vector<String> sqltypeVector) throws
      Exception {
    if (variableVector.size() == 0) {
      return 0;
    }
    tableName = "item";
    String qry = new String("ALTER TABLE " + tableName);
    try {
      for (int i = 0; i < variableVector.size(); i++) {
        String v = ((String)variableVector.get(i)).trim();
        String t = ((String)sqltypeVector.get(i)).trim();
        qry = qry + " , ADD COLUMN " + v + " " + t;
      }
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("createColumnitem with vector, sql = " + qry, true);
      }
      setSQL(qry, newDB);
      return variableVector.size();
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   * Create a column using a vector list of field names, and sql types associated
   * with those field names, for the table "data"
   *
   * @param newDB Name of database column will be created in
   * @param tableName Name of table to create column in
   * @param variableVector Vector of variables [column names] to be created
   * @param sqltypeVector Vector of associated sql column types with variableVector
   */
  public static int createColumndata(String newDB, String tableName, Vector<String> variableVector, Vector<String> sqltypeVector) throws
      Exception {
    //if(variableVector.size() == null) return false;
    tableName = "data";
    String qry = new String("ALTER TABLE " + tableName);
    try {
      for (int i = 0; i < variableVector.size(); i++) {
        Object v = variableVector.get(i);
        Object t = sqltypeVector.get(i);
        if (i > 0)
        	qry = qry + " , ADD COLUMN " + v + " " + t;
        else {
        	qry = qry + " ADD COLUMN " + v + " " + t;
        }
      }
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("createColumndata with vector, sql = " + qry, true);
      }
      else if (DEBUG) {
        System.out.println(qry);
      }
      setSQL(qry, newDB);
      return variableVector.size();
    }
    catch (Exception e) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in createColumnData--see console", true);
      }
      else if (DEBUG) {
        e.printStackTrace();
      }
      throw e;
    }
  }

  /**
   * Create a column using an arraylist of column titles. Note:
   * Columns created in this way are all assinged the SQL "DOUBLE" type
   *
   * @param newDB Name of database column will be created in
   * @param tableName Name of table to create column in
   * @param columns ArrayList of column namess to create in current table
   */
  public static void createColumn(String newDB, String tableName, Vector columns) throws Exception {
    Iterator itor = columns.iterator();
    while (itor.hasNext()) {
      String thecol = (String)itor.next();
      try {
        createColumn(newDB, tableName, thecol, "DOUBLE"); // "type" is set to double right now for test purposes
      }
      catch (Exception ex) {
        throw ex;
      }
    }
  }

  /**
   * a series of calls to the DB for info about the DB structure/contents and
   * just writes the results to the console using System.out.println()
   *
   */
  public static void dumpDB() {
    //after consulting with oz re: which db meta info is to be passed back
    //i will fill this section in with the necessary code to access the info
  }

  /**
   * Method to write data to the database
   *
   * @param inResults Vector of column names and values
   */
  public static void insertData(String newDB, ProfileResults inResults) throws Exception {
    if (DEBUG && mLogger != null) {
      mLogger.logMessage("insertData =", true);
    }
    else if (DEBUG) {
      System.out.println("insertData =");
    }
    try {
      String key = inResults.getDBKey();
      String colStr = new String("(item_id, ");
      String valStr = new String("(\'" + key + "\', ");
      String sql = new String();
      for (int i = 0; i < 8; i++) {
        if (inResults.isResultValueUsed(i)) {
          String col = inResults.getVarName(i);
          double val = inResults.getResultValue(i);
          if (i != 7) {
            colStr += col + ", ";
            valStr += "\'" + Double.toString(val) + "\', ";
          }
          else {
            colStr += col + ")";
            valStr += "\'" + Double.toString(val) + "\')";
          }
        }
      }
      sql = "INSERT INTO data " + colStr + " VALUES " + valStr;

      if (DEBUG && mLogger != null) {
        mLogger.logMessage("insertData sql =" + sql, true);
      }
      else if (DEBUG) {
        System.out.println("insertData sql =" + sql);
      }
      setSQL(sql, newDB);

      if (DEBUG && mLogger != null) {
        mLogger.logMessage(key, true);
      }
      else if (DEBUG) {
        System.out.println(key);
      }
    }
    catch (Exception ex) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in insertData--see console", true);
      }
      else if (DEBUG) {
        ex.printStackTrace();
      }

      if (ex.toString().indexOf("MysqlDataTruncation") >= 0) {
	// eat the exception: work around for floating point bug in MySQL
      }
      else
	throw ex;
    }
  }

  /**
   * Method to write data to the database
   *
   * @param inResults Vector of column names and values
   */
  public static void insertData(String newDB, SingleProfileResult inResult) throws Exception {
    if (DEBUG && mLogger != null) {
      mLogger.logMessage("insertData =", true);
    }
    else if (DEBUG) {
      System.out.println("insertData =");
    }

    try {
      String key = inResult.getDBKey();
      String colStr = new String("(item_id, ");
      String valStr = new String("(\'" + key + "\', ");
      String sql = new String();

      String col = inResult.getVarName();
      double val = inResult.getResultValue();
      colStr += col + ")";
      valStr += "\'" + Double.toString(val) + "\')";
      sql = "INSERT INTO data " + colStr + " VALUES " + valStr;

      if (DEBUG && mLogger != null) {
        mLogger.logMessage("insertData sql =" + sql, true);
      }
      else if (DEBUG) {
        System.out.println("insertData sql =" + sql);
      }
      setSQL(sql, newDB);

      if (DEBUG && mLogger != null) {
        mLogger.logMessage(key, true);
      }
      else if (DEBUG) {
        System.out.println(key);
      }
    }
    catch (Exception ex) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in insertData--see console", true);
      }
      else if (DEBUG) {
        ex.printStackTrace();
      }
      throw ex;
    }
  }

  /**
   * Method to write data to the database
   *
   * @param inResults Vector of column names and values
   */
  public static void updateData(String newDB, ProfileResults inResults) throws Exception {
    if (DEBUG && mLogger != null) {
      mLogger.logMessage("updateData =", true);
    }
    else if (DEBUG) {
      System.out.println("updateData =");
    }
    try {
      String key = inResults.getDBKey();
      String colStr = new String("");
      String valStr = new String("\'" + key + "\', ");
      String sql = new String();
      for (int i = 0; i < 8; i++) {
        if (inResults.isResultValueUsed(i)) {
          String col = inResults.getVarName(i);
          double val = inResults.getResultValue(i);
          if (i != 7) {
            colStr += col + "=\'" + Double.toString(val) + "\', ";
          }
          else {
            colStr += col + "=\'" + Double.toString(val) + "\'";
          }
        }
      }
      sql = "UPDATE data SET " + colStr + " WHERE item_id=\'" + key + "\'";

      if (DEBUG && mLogger != null) {
        mLogger.logMessage("updateData sql =" + sql, true);
      }
      else if (DEBUG) {
        System.out.println("updateData sql =" + sql);
      }
      setSQL(sql, newDB);

      if (DEBUG && mLogger != null) {
        mLogger.logMessage(key, true);
      }
      else if (DEBUG) {
        System.out.println(key);
      }
    }
    catch (Exception ex) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in insertData--see console", true);
      }
      else if (DEBUG) {
        ex.printStackTrace();
      }
      throw ex;
    }
  }

  /**
   * Method to write a single data to the database
   *
   * @param inResults Vector of column names and values
   */
  public static void updateData(String newDB, SingleProfileResult inResults) throws Exception {
    if (DEBUG && mLogger != null) {
      mLogger.logMessage("updateData =", true);
    }
    else if (DEBUG) {
      System.out.println("updateData =");
    }
    try {
      String key = inResults.getDBKey();
      String colStr = new String("");
      String valStr = new String("\'" + key + "\', ");
      String sql = new String();
      String col = inResults.getVarName();
      double val = inResults.getResultValue();
      colStr += col + "=\'" + Double.toString(val) + "\'";
      sql = "UPDATE data SET " + colStr + " WHERE item_id=\'" + key + "\'";

      if (DEBUG && mLogger != null) {
        mLogger.logMessage("updateData sql =" + sql, true);
      }
      else if (DEBUG) {
        System.out.println("updateData sql =" + sql);
      }
      setSQL(sql, newDB);

      if (DEBUG && mLogger != null) {
        mLogger.logMessage(key, true);
      }
      else if (DEBUG) {
        System.out.println(key);
      }
    }
    catch (Exception ex) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in insertData--see console", true);
      }
      else if (DEBUG) {
        ex.printStackTrace();
      }
      throw ex;
    }
  }

  /**
   * Method to write data to the database of table "item"
   *
   * @param inResults Vector of column names and values
   */
  public static void updateItem(String newDB, String key, String type, String fs, String id, double lati, double loni,
                                double zmin, double zmax, String zunits, String date, String dates, String datee,
                                String variables, String units, String lexicon) throws Exception {
    if (DEBUG && mLogger != null) {
      mLogger.logMessage("updateItem =", true);
    }
    else if (DEBUG) {
      System.out.println("updateItem =");
    }
    try {
      String colStr = new String("");
      String valStr = new String("\'" + key + "\', ");
      String sql = new String();
      //sql = "UPDATE item SET latitude=\'" + lati + "\',longitude=\'" + loni + "\'" + " WHERE item_id=\'" + key + "\'";
      sql = "INSERT INTO item (item_id, data_type, fileset, id, longitude, latitude, zmin, zmax, zunits, date, start_time, end_time, variables, units, lexicon) VALUES (\'" +
          key + "\',\'" + type + "\',\'" + fs + "\',\'" + id + "\',\'" + loni + "\',\'" + lati + "\',\'" + zmin +
          "\',\'" + zmax + "\',\'" + zunits + "\',\'" + date + "\',\'" + dates + "\',\'" + datee + "\',\'" + variables +
          "\',\'" + units + "\',\'" + lexicon + "\')";

      if (DEBUG && mLogger != null) {
        mLogger.logMessage("updateItem sql =" + sql, true);
      }
      else if (DEBUG) {
        System.out.println("updateItem sql =" + sql);
      }

      setSQL(sql, newDB);

      if (DEBUG && mLogger != null) {
        mLogger.logMessage(key, true);
      }
      else if (DEBUG) {
        System.out.println(key);
      }
    }
    catch (Exception ex) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in insertItem--see console", true);
      }
      else if (DEBUG) {
        ex.printStackTrace();
      }
      throw ex;
    }
  }

  /**
   * Method to write data to a given column/table/db
   *
   * @param newDB Name of database data is to be inserted into
   * @param columnName Column Name
   * @param tableName Table Name
   * @param variableValue Variable Value
   */
  public static void insertData(String newDB, String tableName, String columnName, double variableValue) throws
      Exception {
    try {
      String sql = new String("UPDATE " + tableName + " SET " + columnName + "=\'" + variableValue + "\'");
      setSQL(sql, newDB);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("insertData sql =" + sql, true);
        mLogger.logMessage(columnName + " " + variableValue + " has been inserted into " + tableName, true);
      }
      else if (DEBUG) {
        System.out.println(columnName + " " + variableValue + " has been inserted into " + tableName);
      }
    }
    catch (Exception ex) {
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Exception thrown in insertData--see console", true);
      }
      else if (DEBUG) {
        ex.printStackTrace();
      }
      throw ex;
    }
  }

  /**
   * Method to return a vector of column names from a selected table
   *
   * @param tableName table name
   * @return vector of listed column names
   */
  public static Vector<String> getColumnNames(String newDB, String tableName, int maxChars) throws Exception {
    Integer dtc = new Integer(3);
    ResultSet rs = setSQL(dtc, newDB, tableName);
    Vector<String> cols = new Vector<String>();
    rs.next();
    while (rs.next()) {
      String st = rs.getString(1);
      cols.addElement(st);
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("Column " + st + " exists", true);
      }
      else if (DEBUG) {
        System.out.println("Column " + st + " exists");
      }
    }
    return cols;
  }

  /**
   * Method to return the version of database being used
   *
   *@return string of the version
   *
   */
  public static String getDBMeta() throws Exception {
    try {
      String st = new String();
      Connection c = createConnection();
      Statement s = (Statement)c.createStatement();
      DatabaseMetaData md = c.getMetaData();
      st = md.getDatabaseProductVersion();
      return st;
    }
    catch (Exception e) {
      throw e;
    }
  }

  /**
   * Method to find if a database exists, returned is a boolean value of true
   * [meaning a database name already exists] or false [a database name does not
   * exist}
   *
   * @param newDB Database name
   * @return true if named database exists
   */
  public static boolean isDatabase(String newDB) throws Exception {
    Integer db = new Integer(1);
    String dn = new String("dummy");
    String tn = new String("rangy");
    ResultSet rs = setSQL(db, newDB, tn);
    if (rs == null) {
      return false;
    }
    while (rs.next()) {
      String st = rs.getString(1);
      if (st.equalsIgnoreCase(newDB)) {
        if (DEBUG && mLogger != null) {
          mLogger.logMessage("DBname " + newDB + " exists", true);
        }
        else if (DEBUG) {
          System.out.println("DBname " + newDB + " exists");
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Method to determine if a table exists
   *
   * @param tableName table name
   * @return true if named table exists
   */
  public static boolean isTable(String tableName) throws Exception {
    Integer ta = new Integer(2);
    String dn = new String("dummy");
    ResultSet rs = setSQL(ta, dn, tableName);
    if (rs == null) {
      return false;
    }
    while (rs.next()) {
      String st = rs.getString(1);
      if (st.equalsIgnoreCase(tableName)) {
        if (DEBUG && mLogger != null) {
          mLogger.logMessage("Table " + tableName + " exists", true);
        }
        else if (DEBUG) {
          System.out.println("Table " + tableName + " exists");
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Method to determine if a column exists in the current database and table
   *
   * @param newDB Name of database column is being queried upon
   * @param tableName table name
   * @param columnName column name
   * @return true if named column exists in named table
   */
  public static boolean isColumn(String newDB, String tableName, String columnName) throws Exception {
    Integer co = new Integer(3);
    ResultSet rs = setSQL(co, newDB, tableName);
    if (rs == null) {
      return false;
    }
    while (rs.next()) {
      String st = rs.getString(1);
      if (st.equalsIgnoreCase(columnName)) {
        if (DEBUG && mLogger != null) {
          mLogger.logMessage("Column " + columnName + " exists", true);
        }
        else if (DEBUG) {
          System.out.println("Column " + columnName + " exists");
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Method to drop a database if the named database already exists.  it does this
   * by sending an sql query for already named db's, parsing the returned objects
   *and if it exists, drops the dbname and sends an method call to createDatabase.
   *
   * @param newDB database name
   */
  public static void dropDatabase(String newDB) throws Exception {
    String sql = new String("DROP DATABASE " + newDB);
    setSQL(sql);
    if (DEBUG && mLogger != null) {
      mLogger.logMessage("DB dropped = ", true);
    }
    else if (DEBUG) {
      System.out.println("DB dropped = " + newDB);
    }
  }

  /**
   * Method to create connection to the database.
   *
   * @return Connection Returns connection to the database manager
   */
  public static Connection createConnection() throws Exception {
    try {
      String driver = "com.mysql.jdbc.Driver";
//      if (DEBUG) {
      		System.out.println("mDBURI= " + mDBURI);
      		System.out.println("mDBPort= " + mDBPort);
      		System.out.println("mDBUserName= " + mDBUserName);
      		System.out.println("mDBPassword= " + mDBPassword);
//      }
      // load the database driver
      Class.forName(driver);
      Connection c = (Connection)DriverManager.getConnection(mDBURI + ":" + mDBPort + "/", mDBUserName, "drowssap");
      if (DEBUG && mLogger != null) {
        mLogger.logMessage("connection= ", true);
      }
      else if (DEBUG) {
        System.out.println("connection= " + c);
      }
      return c;
    }
    catch (Exception e) {
      throw e;
      //System.err.println(e);
      //if (e instanceof SQLException)
      //    System.err.println("SQLState: " + ((SQLException)e).getSQLState());
    }
  }

  /**
   * Method to create connection to the database.
   *
   * @param newDB Database name to be used during connection
   * @return Connection Returns connection to the database manager
   */
  public static Connection createConnection(String newDB) throws Exception {
    try {
      String driver = "com.mysql.jdbc.Driver";
      // load the database driver
      Class.forName(driver);
      Connection c = (Connection)DriverManager.getConnection(mDBURI + ":" + mDBPort + "/" + newDB, mDBUserName,
          "drowssap");

      if (DEBUG && mLogger != null) {
        mLogger.logMessage("connection= " + c, true);
      }
      else if (DEBUG) {
        System.out.println("connection= " + c);
      }
      return c;
    }
    catch (Exception e) {
      throw e;
      //System.err.println(e);
      //if (e instanceof SQLException)
      //    System.err.println("SQLState: " + ((SQLException)e).getSQLState());
    }
  }

  /**
   * Method to close connection to the database.
   *
   * @param c Connection
   */
  public static void closeConnection(Connection c) throws Exception {
    try {
      c.close();
    }
    catch (Exception e) {
      throw e;
    }
  }
}

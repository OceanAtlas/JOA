package gov.noaa.pmel.nquery.database;

import gov.noaa.pmel.nquery.resources.NQueryConstants;
import java.sql.Connection;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class MakeDB {
  public static void main(String args[]) {
    Connection c = null;
    try {
      // load the database driver
      try {
        // load the database driver
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      
      // and set up a connection to the specified database
      String directory = System.getProperty("user.dir") + File.separator + "JOA_Support" + File.separator + "JOA_DB";
      String dbConnectionURL = NQueryConstants.DEFAULT_DB_URI + directory + ";create=true";
      c = (Connection)DriverManager.getConnection(dbConnectionURL, "joa", "drowssap");

      // create three new tables for our data
      // the package table contains a package id and a package name
      // the class table contains a class id, a package id and a class name
      // the member table contians a class id, a member name and an int
      // that indicated whether the class memeber is a field or a method.
      Statement s = c.createStatement();
      s.executeUpdate("DROP DATABASE nquerytrial ");
      s.executeUpdate("CREATE DATABASE nquerytrial ");
      s.executeUpdate("USE nquerytrial ");
      s.executeUpdate("CREATE TABLE item " + "(item_id VARCHAR(40), start DATETIME, end DATETIME," +
                      " max_z DOUBLE, min_z DOUBLE, uri VARCHAR(60), water_depth DOUBLE )");
      s.executeUpdate("CREATE TABLE data " + "(item_id VARCHAR(40), data_id VARCHAR(40), max_temp DOUBLE," +
                      " min_temp DOUBLE, max_salt DOUBLE, min_salt DOUBLE)");
      //s.executeUpdate("CREATE TABLE member " + "(classid SMALLINT, name VARCHAR(48), isfield SMALLINT)");

      // prepare some statements that will be used to insert records into
      // these tables
      //insertpackage = c.prepareStatement("INSERT INTO package VALUES(?,?)");
      //insertclass = c.prepareStatement("INSERT INTO class VALUES(?,?,?)");
      //insertmember = c.prepareStatement("INSERT INTO member VALUES(?,?,?)");

      // now loop through the list of classes and store them all in the tables
      //for(int i = 0; i < classnames.size(); i++)
      //    storeClass((String)classnames.elementAt(i));
    }
    catch (Exception e) {
      System.err.println(e);
      if (e instanceof SQLException) {
        System.err.println("SQLState: " + ((SQLException)e).getSQLState());
      }
      System.err.println("Usage: java MakeDB <classlistfile> <propfile>");
    }
    // when we'return done, close the connection to the database
    finally {
      try {
        c.close();
      }
      catch (Exception e) {}
    }
  }

  public static void storeClass(String name) throws SQLException, ClassNotFoundException {
    String packagename, classname;

    // dynamically load the class;
    Class c = Class.forName(name);

    // display output so the user knows that the program is responding
    System.out.println("Storing data for: " + name);
  }
}

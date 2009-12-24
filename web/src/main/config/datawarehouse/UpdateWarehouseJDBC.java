import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2006 Akaza Research
 */

public class UpdateWarehouseJDBC {
  private static Properties params = new Properties();
  public static void main(String[] args) {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      System.err.println("Driver not found: " + e + "\n" + e.getMessage());
    }    
    
    try {
      
        params.load(new FileInputStream(args[0]));
   
  } catch (IOException ioe) {
    System.out
    .println("Please make sure you have the file 'update_warehouse_jdbc.properties' under C:\\OpenClinica\\OpenClinica-1.1\\conf");

    return;
  }
    // Use the driver to connect to the database
    
    try {
      Connection conn = null;        
      String sqlFile =  params.getProperty("sqlPath");
      String database = params.getProperty("database");
      String username = params.getProperty("connUsername");
      String password = params.getProperty("connPassword");
      
      /*
      System.out.println(sqlFile);
      System.out.println(database);
      System.out.println(username);
      System.out.println(password);
      */
      conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/" + database,
          username, password);

      Statement stmt = conn.createStatement();      

      // Now execute our query
     String sql = getContents(new File(sqlFile));
     stmt.execute(sql);    
     conn.close();
      
    } catch (Exception e) {
      System.err.println("Exception: " + e + "\n" + e.getMessage());
      e.printStackTrace();
    } 
  }

  /**
   * Fetch the entire contents of a text file, and return it in a String. This
   * style of implementation does not throw Exceptions to the caller.
   * 
   * @param aFile
   *          is a file which already exists and can be read.
   */
  static public String getContents(File aFile) {
    // ...checks on aFile are elided
    StringBuffer contents = new StringBuffer();

    // declared here only to make visible to finally clause
    BufferedReader input = null;
    try {
      // use buffering
      // this implementation reads one line at a time
      input = new BufferedReader(new FileReader(aFile));
      String line = null; // not declared within while loop
      while ((line = input.readLine()) != null) {
        contents.append(line);
        contents.append(System.getProperty("line.separator"));
      }
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        if (input != null) {
          // flush and close both "input" and its underlying FileReader
          input.close();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return contents.toString();
  }
  
  


}

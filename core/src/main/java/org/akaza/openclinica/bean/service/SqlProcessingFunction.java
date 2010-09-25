package org.akaza.openclinica.bean.service;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;

/**
 * Class to implement the datamart in SQL.
 * by Tom Hickerson, 09/2010
 * @author thickerson
 *
 */
public class SqlProcessingFunction extends ProcessingFunction {

	private ExtractPropertyBean extractPropertyBean;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
	private String databaseType;
	
    public SqlProcessingFunction(ExtractPropertyBean extractPropertyBean) {
    	this.extractPropertyBean = extractPropertyBean;
    	fileType = "sql";
    }
    
    /**
     * the run function will find the file name, run the SQL on the assigned
     * db, and make sure the datamart is assembled correctly.
     * 
     * 
     * 
     */
    public String run() {
    	try {
    		// load the proper database class below
    		if ("postgres".equals(databaseType)) {
    			Class.forName("org.postgresql.Driver");
    		} else {
    			Class.forName("oracle.jdbc.driver.OracleDriver");
    		}
    		Connection conn = 
    			DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
    		conn.setAutoCommit(false);
    		File sqlFile = new File(getTransformFileName());
    		String[] statements = getFileContents(sqlFile); // ???
    		for (String statement : statements) {
    		    Statement stmt = conn.createStatement();
    		    // and then execute the statement here
    		    // convert the translated file to a string and then tries an execute

    		    System.out.println("-- > about to run " + statement);

    		    stmt.executeUpdate(statement);
    		    
    		    stmt.close();
    		    
    		}
    		conn.commit();
    		conn.setAutoCommit(false);
    		conn.close();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	    System.out.println(" -- > found an exception : " + e.getMessage());
    	}
    	// set up the reply object
    	return null;
    }
    

	public ExtractPropertyBean getExtractPropertyBean() {
		return extractPropertyBean;
	}

	public void setExtractPropertyBean(ExtractPropertyBean extractPropertyBean) {
		this.extractPropertyBean = extractPropertyBean;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	
	private String[] getFileContents(File sqlFile) throws Exception {
	    String value = "";
	    StringBuffer sb = new StringBuffer();
	    int bufSize = 1024;
	    BufferedReader br = new BufferedReader(new FileReader(sqlFile));
	    char[] buffer = new char[bufSize];
	    int amt = 0;
	    while ((amt = br.read(buffer)) >= 0) {
	        // value = value.concat(buffer);
	        sb.append(buffer, 0, amt);
	    }
	    br.close();
	    // sending sql statement by sql statement for error checking, tbh
	    // since we have plpsql functions we need to ignore semis that are included
	    // in quotes
	    
	    // return sb.toString().split(";[^as \'.*\']");
	    String[] ret = new String[1];
	    ret[0] = sb.toString();
	    return ret;
	}
}

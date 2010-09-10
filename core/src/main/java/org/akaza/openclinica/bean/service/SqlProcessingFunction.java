package org.akaza.openclinica.bean.service;

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
    }
    
    /**
     * the run function will find the file name, run the SQL on the assigned
     * db, and make sure the datamart is assembled correctly.
     * 
     * now theres the thing, do we send the datasource into the object?
     * probably not since it could be a separate DB
     */
    public void run() {
    	try {
    		// load the proper database class below
    		if ("postgres".equals(databaseType)) {
    			Class.forName("");
    		} else {
    			Class.forName("");
    		}
    		Connection conn = 
    			DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
    		Statement stmt = conn.createStatement();
    		// and then execute the statement here
    	} catch (Exception e) {

    	}
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
}

package org.akaza.openclinica.bean.service;

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
    
    public void run() {
        
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

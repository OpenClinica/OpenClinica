package org.akaza.openclinica.bean.service;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.core.util.ScriptRunner;
import org.akaza.openclinica.dao.core.CoreResources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Class to implement the datamart in SQL. by Tom Hickerson, 09/2010
 * 
 * @author thickerson
 * 
 */
public class SqlProcessingFunction extends ProcessingFunction implements Serializable {

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
     * The run() method will find the file name, runs the SQL on the assigned
     * db, and returns a success/fail message.
     * 
     * This method assumes all variables are set in the XsltTransformJob, and
     * that the database variables are correctly set in either
     * extract.properties or datainfo.properties NOTE that if variables are set
     * in extract, we do not try datainfo (it has to be correct somewhere)
     * 
     */
    public ProcessingResultType run() {
        Connection conn = null;
        ProcessingResultType resultError = null;
        Statement stmt = null;
        try {
            // load the proper database class below
            Properties props = new Properties();
            props.setProperty("user", databaseUsername);
            props.setProperty("password", databasePassword);
            // props.setProperty("ssl","true");
            conn = DriverManager.getConnection(databaseUrl, props);
            CoreResources.setSchema(conn);


            conn.setAutoCommit(false);
            File sqlFile = new File(getTransformFileName());
            // String[] statements = getFileContents(sqlFile);

            ScriptRunner runner = new ScriptRunner(conn, true, false);
            runner.runScript(new BufferedReader(new FileReader(sqlFile)));

            /*
             * stmt = conn.createStatement(); for (String statement :
             * statements) {
             * 
             * // and then execute the statement here // convert the translated
             * file to a string and then tries an execute
             * 
               * stmt.executeUpdate(statement); //stmt.close();
             * 
             * } // stmt.executeBatch();
             */
            if (conn != null) {
                conn.commit();
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultError = ProcessingResultType.FAIL;
            resultError.setUrl(""); // no url required
            resultError.setArchiveMessage("Failure thrown: " + e.getMessage());
            resultError.setDescription("Your job failed with the message of: " + e.getMessage());

            return resultError;

        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null) {
                    conn.commit();
                    conn.setAutoCommit(true);

                    conn.close();
                }
                if (resultError != null)
                    return resultError;
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // set up the reply object
        ProcessingResultType result = ProcessingResultType.SUCCESS;
        result.setUrl(""); // no url required
        result.setArchiveMessage("Successfully run");
        result.setDescription("Your job ran successfully.");
        // replace with something from extract prop bean?
        return result;

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

    /*
     * getFileContents(sqlFile): pulls out all the contents and assembles a
     * string with all the SQL statements to be executed on the datamart.
     */
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
        // since we have plpsql functions we need to ignore semis that are
        // included
        // in quotes

        // return sb.toString().split(";[^as \'.*\']");
        // JN: Changing this to use tokenizer
        // String[] ret = new String[1];
        String[] ret = sb.toString().split(";");
        return ret;
    }
}

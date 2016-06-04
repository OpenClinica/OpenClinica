/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

;

/**
 * Utility which handles connection and login, as prompted by OpenClinica
 * control servlets. Updated August 2004 to better handle connection pooling.
 * Updated again in August 2004 to support SQL Statements in XML. Will require a
 * change to all control servlets; new SessionManagers will have to be stored in
 * session, since we don't want to take in all this information every time a
 * user clicks on a new servlet.
 *
 * @author Tom Hickerson
 * @author Jun Xu
 */
public class SessionManager {
    private Connection con;

    private UserAccountBean ub;

    private String logFileName;

    private OracleDataSource ods;

    private Level logLevel;

    private DataSource ds;

    final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private String dbName;
    private UserAccountDAO uDAO = null;

    //TODO: this is a hack needs to be refactord
    private static DataSource staticDataSource;

    /**
     * Constructor of SessionManager
     *
     * @param userFromSession
     * @param userName
     * @throws SQLException
     */
    public SessionManager(UserAccountBean userFromSession, String userName) throws SQLException {
        setupDataSource();
        setupUser(userFromSession, userName);
    }

    /**
     * Constructor of SessionManager
     *
     * @param userFromSession
     * @param userName
     * @throws SQLException
     */
    public SessionManager(UserAccountBean userFromSession, String userName, ApplicationContext applicationContext) throws SQLException {
        this.ds = (DataSource) applicationContext.getBean("dataSource");
        staticDataSource = ds;
        setupUser(userFromSession, userName);
    }

    public SessionManager( ApplicationContext applicationContext) {
        this.ds = (DataSource) applicationContext.getBean("dataSource");
        staticDataSource = ds;

    }
    public void setupUser(UserAccountBean userFromSession, String userName) {
        if (userFromSession == null || StringUtil.isBlank(userFromSession.getName())) {
            // create a new user account bean form database
            SQLFactory factory = SQLFactory.getInstance();

            uDAO = new UserAccountDAO(ds);
            if (userName == null) {
                userName = "";
            }
            ub = (UserAccountBean) uDAO.findByUserName(userName);
            logger.debug("User  : {} , email address : {} Logged In ", ub.getName(), ub.getEmail());

        } else {
            ub = userFromSession;
        }
    }



    public void setupDataSource() {
        // begin remove later
        // logger.info("***** BEGIN LISTING PROPERTIES *****");
        // System.getProperties().list(System.out);
        // logger.info("***** END LISTING PROPERTIES *****");
        // end remove later
        try {
            Context ctx = new InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            dbName = CoreResources.getField("dataBase");
            if ("oracle".equals(dbName)) {
                logger.debug("looking up oracle...");
                ds = (DataSource) env.lookup("SQLOracle");
            } else if ("postgres".equals(dbName)) {
                // logger.info("looking up postgres...");
                ds = (DataSource) env.lookup("SQLPostgres");
            }

        } catch (NamingException ne) {
            ne.printStackTrace();
            logger.warn("This is :" + ne.getMessage() + " when we tried to get the connection");
        }
    }

    public SessionManager() {
        setupDataSource();
    }

    public Connection getConnection() throws SQLException {
        Connection conn =ds.getConnection();
        CoreResources.setSchema(conn);
        return conn;
    }

    public UserAccountBean getUserBean() {
        return ub;
    }

    public void setConnection(Connection con) {
        this.con = con;
    }

    public void setUserBean(UserAccountBean user) {
        this.ub = user;
    }

    public DataSource getDataSource() {
        return ds;
    }

    /** added 08-04-2004 by tbh, supporting Oracle 10g */
    public OracleDataSource getOracleDataSource() {
        return ods;
    }

    public static DataSource getStaticDataSource() {
        return staticDataSource;
    }

}
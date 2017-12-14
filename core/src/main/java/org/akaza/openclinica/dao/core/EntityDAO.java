/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.core;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.ApplicationConstants;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.cache.EhCacheWrapper;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;

/**
 * <p/>
 * EntityDAO.java, the generic data access object class for the database layer, by Tom Hickerson, 09/24/2004
 * <p/>
 * A signalling system was added on 7 Dec 04 to indicate the success or failure of a query. A query is considered
 * successful iff a SQLException was not thrown
 * in the process of executing the query.
 * <p/>
 * The system can be used by outside classes / subclasses as follows: - Immediately after calling select or execute,
 * isQuerySuccessful() is <code>true</code> if
 * the query was successful, <code>false</code> otherwise. - If isQuerySuccessful returns <code>false</code>
 * getFailureDetails() returns the SQLException which
 * was thrown.
 * <p/>
 * In order to maintain the system, the following invariants must be maintained by developers: 1. Every method executing
 * a query must call clearSignals() as the
 * first statement. 2. Every method executing a query must call either signalSuccess or signalFailure before returning.
 * <p/>
 * At the time of writing, the only methods which execute queries are select and execute.
 *
 * @author thickerson
 * @param <V>
 * @param <K>
 */
public abstract class EntityDAO<K extends String, V extends ArrayList> implements DAOInterface {
    protected DataSource ds;

    protected String digesterName;

    protected DAODigester digester;

    private HashMap setTypes = new HashMap();

    /* Here is the cache reference */
    protected EhCacheWrapper cache;
    // protected EhCacheWrapper cache = new EhCacheWrapper();
    protected EhCacheManagerFactoryBean cacheManager;

    // set the types we expect from the database

    // private ArrayList results = new ArrayList();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private boolean querySuccessful;

    private SQLException failureDetails;

    /**
     * Should the name of a query which refers to a SQL command of the following form:
     * <code>SELECT currval('sequence') AS key</code> The column name "key" is required, as
     * getCurrentPK() relies on it.
     */
    protected String getCurrentPKName;

    /**
     * Should the name of a query which refers to a SQL command of the following form:
     * <code>SELECT nextval('sequence') AS key</code> The column name "key" is required, as getNextPK()
     * relies on it.
     */
    protected String getNextPKName;

    protected abstract void setDigesterName();

    // YW 11-26-2007, at this time, it is set only by the method
    // "executeWithPK".
    private int latestPK;

    protected Locale locale = ResourceBundleProvider.getLocale();
    // BWP>> these Strings are initialized from the constructor: the
    // initializeI18nStrings() method; for JUnit tests
    protected String oc_df_string = "";
    protected String local_df_string = "";

    protected EhCacheWrapper ehCacheWrapper;

    public EntityDAO(DataSource ds) {
        this.ds = ds;
        setDigesterName();
        digester = SQLFactory.getInstance().getDigester(digesterName);
        initializeI18nStrings();
        setCache(SQLFactory.getInstance().getEhCacheWrapper());
    }

    /**
     * This is the method added to cache the queries
     * 
     * @param cache
     */
    public void setCache(final EhCacheWrapper cache) {
        this.cache = cache;
    }

    public EhCacheWrapper getCache() {
        return cache;
    }

    /**
     * setTypeExpected, expects to enter the type of object to retrieve from the database
     *
     * @param num
     *            the order the column should be extracted from the database
     * @param type
     *            the number that is equal to TypeNames
     */
    public void setTypeExpected(int num, int type) {
        setTypes.put(Integer.valueOf(num), Integer.valueOf(type));
    }

    public void unsetTypeExpected() {
        setTypes = new HashMap();
    }

    /**
     * select, a static query interface to the database, returning an array of hashmaps that contain key->object pairs.
     * <P>
     * This is the first operation created for the database, so therefore it is the simplest; cull information from the
     * database but not specify any parameters.
     *
     * @param query
     *            a static query of the database.
     * @return ArrayList of HashMaps carrying the database values.
     */
    public ArrayList select(String query) {
        clearSignals();

        ArrayList results = new ArrayList();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;

        logger.debug("query???" + query);
        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            // if (logger.isInfoEnabled()) {
            logger.debug("Executing static query, GenericDAO.select: " + query);
            // logger.info("fond information about result set: was null: "+
            // rs.wasNull());
            // }
            // ps.close();
            signalSuccess();
            results = this.processResultRows(rs);
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        // return rs;
        return results;

    }

    public ArrayList<V> select(String query, HashMap variables) {
        clearSignals();

        ArrayList results = new ArrayList();

        ResultSet rs = null;
        Connection con = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables);
        PreparedStatement ps = null;

        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

            ps = con.prepareStatement(query);

            ps = psf.generate(ps);// enter variables here!

            {
                rs = ps.executeQuery();
                results = this.processResultRows(rs);

            }

            // if (logger.isInfoEnabled()) {

            logger.debug("Executing dynamic query, EntityDAO.select:query " + query);
            // }
            signalSuccess();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing dynamic query, GenericDAO.select: " + query + ":message: " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return results;

    }

    // Added by YW, 11-26-2007
    public ArrayList select(String query, Connection con) {
        clearSignals();

        ArrayList results = new ArrayList();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            // if (logger.isInfoEnabled()) {
            logger.debug("Executing dynamic query, EntityDAO.select:query " + query);
            // }
            signalSuccess();
            results = this.processResultRows(rs);

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing dynamic query, GenericDAO.select: " + query + ":message: " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(rs, ps);
        }
        return results;

    }

    // JN: The following method is added for when certain queries needed caching...

    public ArrayList<V> selectByCache(String query, HashMap variables) {
        clearSignals();

        ArrayList results = new ArrayList();
        V value;
        K key;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables);
        PreparedStatement ps = null;

        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

            ps = con.prepareStatement(query);

            ps = psf.generate(ps);// enter variables here!
            key = (K) ps.toString();
            if ((results = (V) cache.get(key)) == null) {
                rs = ps.executeQuery();
                results = this.processResultRows(rs);
                if (results != null) {
                    cache.put(key, results);
                }
            }

            // if (logger.isInfoEnabled()) {
            logger.debug("Executing dynamic query, EntityDAO.select:query " + query);
            // }
            signalSuccess();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing dynamic query, GenericDAO.select: " + query + ":message: " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return results;

    }

    /**
     * execute, the static version of executing an update or insert on a table in the database.
     *
     * @param query
     *            a static SQL statement which updates or inserts.
     * 
     * 
     */

    public void execute(String query) {
        Connection con = null;
        execute(query, con);
    }

    /*
     * this function is used for transactional updates to allow all updates in
     * one actions to run as one transaction
     */
    public void execute(String query, Connection con) {
        clearSignals();

        boolean isTrasactional = false;
        if (con != null) {
            isTrasactional = true;
        }
        PreparedStatement ps = null;
        try {
            if (!isTrasactional) {
                con = ds.getConnection();
            }
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: EntityDAO.execute!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);

            if (ps.executeUpdate() != 1) {
                logger.warn("Problem with executing static query, EntityDAO: " + query);
                throw new SQLException();
            } else {
                signalSuccess();
                logger.debug("Executing static query, EntityDAO: " + query);
            }
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static statement, GenericDAO.execute: " + query + ":message: " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            if (!isTrasactional) {
                this.closeIfNecessary(con, ps);
            } else {
                closePreparedStatement(ps);
            }
        }
    }

    public void execute(String query, HashMap variables) {
        Connection con = null;
        execute(query, variables, con);
    }

    public void execute(String query, HashMap variables, Connection con) {
        clearSignals();

        boolean isTrasactional = false;
        if (con != null) {
            isTrasactional = true;
        }

        PreparedStatement ps = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables);
        try {
            if (!isTrasactional) {
                con = ds.getConnection();
            }
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: EntityDAO.execute!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            ps = psf.generate(ps);// enter variables here!
            if (ps.executeUpdate() < 0) {// change by jxu, delete can affect
                // more than one row
                logger.warn("Problem with executing dynamic query, EntityDAO: " + query);
                throw new SQLException();

            } else {
                signalSuccess();
                logger.debug("Executing dynamic query, EntityDAO: " + query);
            }
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing dynamic statement, EntityDAO.execute: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            if (!isTrasactional) {
                this.closeIfNecessary(con, ps);
            } else {
                closePreparedStatement(ps);
            }
        }
    }

    public void execute(String query, HashMap variables, HashMap nullVars) {
        Connection con = null;
        execute(query, variables, nullVars, con);
    }

    public void execute(String query, HashMap variables, HashMap nullVars, Connection con) {
        clearSignals();

        boolean isTrasactional = false;
        if (con != null) {
            isTrasactional = true;
        }

        PreparedStatement ps = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables, nullVars);
        try {
            if (!isTrasactional) {
                con = ds.getConnection();
            }
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: EntityDAO.execute!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            ps = psf.generate(ps);// enter variables here!
            if (ps.executeUpdate() != 1) {
                logger.warn("Problem with executing dynamic query, EntityDAO: " + query);
                throw new SQLException();

            } else {
                signalSuccess();
                logger.debug("Executing dynamic query, EntityDAO: " + query);
            }
        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing dynamic statement, EntityDAO.execute: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            if (!isTrasactional) {
                this.closeIfNecessary(con, ps);
            } else {
                closePreparedStatement(ps);
            }
        }
    }

    /**
     * This method inserts one row for an entity table and gets latestPK of this row.
     *
     * @param query
     * @param variables
     * @param nullVars
     *
     * @author ywang 11-26-2007
     */
    public void executeWithPK(String query, HashMap variables, HashMap nullVars) {
        clearSignals();

        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables, nullVars);
        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: EntityDAO.execute!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            ps = psf.generate(ps);// enter variables here!
            if (ps.executeUpdate() != 1) {
                logger.warn("Problem with executing dynamic query, EntityDAO: " + query);
                throw new SQLException();

            } else {
                logger.debug("Executing dynamic query, EntityDAO: " + query);

                if (getCurrentPKName == null) {
                    this.latestPK = 0;
                }

                this.unsetTypeExpected();
                this.setTypeExpected(1, TypeNames.INT);

                ArrayList al = select(digester.getQuery(getCurrentPKName), con);

                if (al.size() > 0) {
                    HashMap h = (HashMap) al.get(0);
                    this.latestPK = ((Integer) h.get("key")).intValue();
                }

            }

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing dynamic statement, EntityDAO.execute: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, ps);
        }
    }

    /*
     * Currently, latestPK is set only in executeWithPK() after inserting has been executed successfully. So, this
     * method should be called only immediately
     * after executeWithPK()
     * 
     * @return ywang 11-26-2007
     */
    protected int getLatestPK() {
        return latestPK;
    }

    private void logMe(String message) {
        // System.out.println(message);
        logger.debug(message);
    }

    public ArrayList processResultRows(ResultSet rs) {// throws SQLException
        ArrayList al = new ArrayList();
        HashMap hm;

        try {
            // rs.beforeFirst();
            while (rs.next()) {
                hm = new HashMap();
                ResultSetMetaData rsmd = rs.getMetaData();

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String column = rsmd.getColumnName(i).toLowerCase();
                    Integer type = (Integer) setTypes.get(Integer.valueOf(i));
                    // @pgawade 18-May-2011 Fix for issue #9703 - temporarily
                    // commented out the following log statement
                    // as in case of viewing SDV page, type value for one of
                    // columns was null causing NullPointerException
                    // logMe("column name: "+column+" type # "+type.intValue()+" row # "+i);
                    if (null != type) {
                        switch (type.intValue()) {
                        // just putting the top five in here for now, tbh
                        // put in statements to catch nulls in the db, tbh
                        // 10-15-2004
                        case TypeNames.DATE:
                            // logger.warn("date: "+column);
                            hm.put(column, rs.getDate(i));
                            // do we want to put in a fake date if it's null?
                            /*
                             * if (rs.wasNull()) { hm.put(column,new
                             * Date(System.currentTimeMillis())); }
                             */
                            break;
                        case TypeNames.TIMESTAMP:
                            // logger.warn("timestamp: "+column);
                            hm.put(column, rs.getTimestamp(i));
                            break;
                        case TypeNames.DOUBLE:
                            // logger.warn("double: "+column);
                            hm.put(column, new Double(rs.getDouble(i)));
                            if (rs.wasNull()) {
                                hm.put(column, new Double(0));
                            }
                            break;
                        case TypeNames.BOOL:
                            // BADS FLAG
                            if (CoreResources.getDBName().equals("oracle")) {
                                hm.put(column, new Boolean(rs.getString(i).equals("1") ? true : false));
                                if (rs.wasNull()) {
                                    if (column.equalsIgnoreCase("start_time_flag") || column.equalsIgnoreCase("end_time_flag")) {
                                        hm.put(column, new Boolean(false));
                                    } else {
                                        hm.put(column, new Boolean(true));
                                    }
                                }
                            } else {
                                hm.put(column, new Boolean(rs.getBoolean(i)));
                                if (rs.wasNull()) {
                                    // YW 08-17-2007 << Since I didn't
                                    // investigate
                                    // what's the impact if changing true to
                                    // false,
                                    // I only do change for the columns of
                                    // "start_time_flag" and "end_time_flag" in
                                    // the
                                    // table study_event
                                    if (column.equalsIgnoreCase("start_time_flag") || column.equalsIgnoreCase("end_time_flag")) {
                                        hm.put(column, new Boolean(false));
                                    } else {
                                        hm.put(column, new Boolean(true));
                                    }
                                    // bad idea? what to put, then?
                                }
                            }
                            break;
                        case TypeNames.FLOAT:
                            hm.put(column, new Float(rs.getFloat(i)));
                            if (rs.wasNull()) {
                                hm.put(column, new Float(0.0));
                            }
                            break;
                        case TypeNames.INT:
                            hm.put(column, Integer.valueOf(rs.getInt(i)));
                            if (rs.wasNull()) {
                                hm.put(column, Integer.valueOf(0));
                            }
                            break;
                        case TypeNames.STRING:
                            hm.put(column, rs.getString(i));
                            if (rs.wasNull()) {
                                hm.put(column, "");
                            }
                            break;
                        case TypeNames.CHAR:
                            hm.put(column, rs.getString(i));
                            if (rs.wasNull()) {
                                char x = 'x';
                                hm.put(column, new Character(x));
                            }
                            break;
                        default:
                            // do nothing?
                        }// end switch
                    }
                } // end for loop
                al.add(hm);
                // adding a row gotten from the database
            }
        } catch (SQLException sqle) {
            // System.out.println("exception at column ");
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.select: " + ": " + sqle.getMessage() + ": array length: " + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }
        return al;
    }

    /*
     * @return the current value of the primary key sequence, if <code> getNextPKName </code> is non-null, or null if
     * <code> getNextPKName </code> is null.
     */
    public int getNextPK() {
        int answer = 0;

        if (getNextPKName == null) {
            return answer;
        }

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        ArrayList<HashMap<String, ?>> al = select(digester.getQuery(getNextPKName));

        if (al.size() > 0) {
            HashMap<String, ?> h = al.get(0);
            answer = ((Integer) h.get("key")).intValue();
        }

        return answer;
    }

    /*
     * @return the current value of the primary key sequence, if <code> getCurrentPKName </code> is non-null, or null if
     * <code> getCurrentPKName </code> is
     * null.
     */
    public int getCurrentPK() {
        int answer = 0;

        if (getCurrentPKName == null) {
            return answer;
        }

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        ArrayList al = select(digester.getQuery(getCurrentPKName));

        if (al.size() > 0) {
            HashMap h = (HashMap) al.get(0);
            answer = ((Integer) h.get("key")).intValue();
        }

        return answer;
    }

    /**
     * This method executes a "findByPK-style" query. Such a query has two characteristics:
     * <ol>
     * <li>The columns SELECTed by the SQL are all of the columns in the table relevant to the DAO, and only those
     * columns. (e.g., in StudyDAO, the columns
     * SELECTed are all of the columns in the study table, and only those columns.)
     * <li>It returns at most one EntityBean.
     * <ul>
     * <li>Typically this means that the WHERE clause includes the columns in a candidate key with "=" criteria.
     * <li>e.g., "WHERE item_id = ?" when selecting from item
     * <li>e.g., "WHERE item_id = ? AND event_crf_id=?" when selecting from item_data
     * </ol>
     *
     * Note that queries which join two tables may be included in the definition of "findByPK-style" query, as long as
     * the first criterion is met.
     *
     * @param queryName
     *            The name of the query which should be executed.
     * @param variables
     *            The set of variables used to populate the PreparedStatement; should be empty if none are needed.
     * @return The EntityBean selected by the query.
     */
    public EntityBean executeFindByPKQuery(String queryName, HashMap variables) {
        EntityBean answer = new EntityBean();

        String sql = digester.getQuery(queryName);
        logMe("query:" + queryName + "variables:" + variables);

        ArrayList rows;
        if (variables == null || variables.isEmpty()) {
            rows = this.select(sql);
        } else {
            rows = this.select(sql, variables);
        }

        Iterator it = rows.iterator();

        if (it.hasNext()) {
            answer = (EntityBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }

    /**
     * Exactly equivalent to calling <code>executeFindByPKQuery(queryName, new HashMap())</code>.
     *
     * @param queryName
     *            The name of the query which should be executed.
     * @return The EntityBean selected by the query.
     */
    public EntityBean executeFindByPKQuery(String queryName) {
        return executeFindByPKQuery(queryName, new HashMap());
    }

    public void closeIfNecessary(Connection con) {
        try {
            // close the connection for right now
            if (con != null)
                con.close();
        } catch (SQLException sqle) {// eventually throw a custom
            // exception,tbh
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown in GenericDAO.closeIfNecessary");
                logger.error(sqle.getMessage(), sqle);
            }
        } // end of catch
    }

    public void closeIfNecessary(Connection con, ResultSet rs) {
        try {
            // close the connection for right now
            if (rs != null)
                rs.close();
            if (con != null)
                con.close();
        } catch (SQLException sqle) {// eventually throw a custom
            // exception,tbh
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown in GenericDAO.closeIfNecessary");
                logger.error(sqle.getMessage(), sqle);
            }
        } // end of catch
    }

    public void closeIfNecessary(Connection con, ResultSet rs, PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
            if (con != null)
                con.close();
        } catch (SQLException sqle) {// eventually throw a custom
            // exception,tbh
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown in GenericDAO.closeIfNecessary");
                logger.error(sqle.getMessage(), sqle);
            }
        } // end of catch
    }

    public void closeIfNecessary(ResultSet rs, PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException sqle) {// eventually throw a custom
            // exception,tbh
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown in GenericDAO.closeIfNecessary(rs,ps)");
                logger.error(sqle.getMessage(), sqle);
            }
        } // end of catch
    }

    public void closeIfNecessary(Connection con, PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();
            if (con != null)
                con.close();
        } catch (SQLException sqle) {// eventually throw a custom
            // exception,tbh
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown in GenericDAO.closeIfNecessary");
                logger.error(sqle.getMessage(), sqle);
            }
        } // end of catch
    }

    /**
     * getDS, had to add it to allow queries of other daos within the daos
     *
     * @return Returns the ds.
     */
    public DataSource getDs() {
        return ds;
    }

    /**
     * @param ds
     *            The ds to set.
     */
    // public void setDs(DataSource ds) {
    // this.ds = ds;
    // }
    /**
     * Clear the signals which indicate the success or failure of the query. This method should be called at the
     * beginning of every select or execute method.
     */
    protected void clearSignals() {
        querySuccessful = false;
    }

    /**
     * Signal that the query was successful. Either this method or signalFailure should be called by the time a select
     * or execute method returns.
     */
    protected void signalSuccess() {
        querySuccessful = true;
    }

    /**
     * Signal that the query was unsuccessful. Either this method or signalSuccess should be called by the time a select
     * or execute method returns.
     *
     * @param sqle
     *            The SQLException which was thrown by PreparedStatement.execute/executeUpdate.
     */
    protected void signalFailure(SQLException sqle) {
        querySuccessful = false;
        failureDetails = sqle;
    }

    /**
     * @return Returns the failureDetails.
     */
    public SQLException getFailureDetails() {
        return failureDetails;
    }

    /**
     * @return Returns the querySuccessful.
     */
    public boolean isQuerySuccessful() {
        return querySuccessful;
    }

    protected String selectString(HashMap hm, String column) {
        if (hm.containsKey(column)) {
            try {
                String s = (String) hm.get(column);
                if (s != null) {
                    return s;
                }
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    protected int selectInt(HashMap hm, String column) {
        if (hm.containsKey(column)) {
            try {
                Integer i = (Integer) hm.get(column);
                if (i != null) {
                    return i.intValue();
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    protected boolean selectBoolean(HashMap hm, String column) {
        if (hm.containsKey(column)) {
            try {
                Boolean b = (Boolean) hm.get(column);
                if (b != null) {
                    return b.booleanValue();
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public void initializeI18nStrings() {
        if (locale != null) {
            // oc_df_string = ResourceBundleProvider.getFormatBundle(locale).getString("oc_date_format_string");
            oc_df_string = ApplicationConstants.getDateFormatInItemData();
            local_df_string = ResourceBundleProvider.getFormatBundle(locale).getString("date_format_string");
        }
    }

    /**
     * ********************************************************************************
     * ********************************************************************************
     *
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION - a new section that uses a different way to access the data -
     *      this is to improve performance and fix
     *      some bugs with the data extraction
     *      *******************************************************************************
     *
     *
     *      @ywang, 09-09-2008, modified syntax of some sql scripts for oracle database.
     *
     */
    /**
     * select, a static query interface to the database, returning an array of hashmaps that contain key->object pairs.
     * <P>
     * This is the first operation created for the database, so therefore it is the simplest; cull information from the
     * database but not specify any parameters.
     *
     * @param query
     *            a static query of the database.
     * @return ArrayList of HashMaps carrying the database values.
     */
    public ArrayList selectStudySubjects(int studyid, int parentid, String sedin, String it_in, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {
        clearSignals();
        String query = getSQLSubjectStudySubjectDataset(studyid, parentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint,
                CoreResources.getDBName());
        logger.debug("sqlSubjectStudySubjectDataset=" + query);
        ArrayList results = new ArrayList();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            con.setAutoCommit(false);
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

            ps = con.prepareStatement(query);
            ps.setFetchSize(50);
            rs = ps.executeQuery();
            if (logger.isInfoEnabled()) {
                logger.debug("Executing static query, GenericDAO.select: " + query);
                // logger.info("fond information about result set: was null: "+
                // rs.wasNull());
            }
            // ps.close();
            signalSuccess();
            results = this.processStudySubjects(rs);
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        // return rs;
        return results;

    }//

    /**
     *
     * @param rs
     * @return
     */
    public ArrayList processStudySubjects(ResultSet rs) {// throws
        // SQLException
        ArrayList al = new ArrayList();

        try {
            while (rs.next()) {
                StudySubjectBean obj = new StudySubjectBean();
                // first column
                obj.setId(rs.getInt("study_subject_id"));
                if (rs.wasNull()) {
                    obj.setId(0);
                }

                // second column
                obj.setSubjectId(Integer.valueOf(rs.getInt("subject_id")));
                if (rs.wasNull()) {
                    obj.setSubjectId(Integer.valueOf(0));
                }

                // old subject_identifier
                obj.setLabel(rs.getString("label"));
                if (rs.wasNull()) {
                    obj.setLabel("");
                }

                obj.setDateOfBirth(rs.getDate("date_of_birth"));
                // what default?
                /*
                 * if (rs.wasNull()) { obj.setDateOfBirth(""); }
                 */
                String gender = rs.getString("gender");
                if (gender != null && gender.length() > 0) {
                    obj.setGender(gender.charAt(0));
                } else {
                    obj.setGender(' ');
                }

                obj.setUniqueIdentifier(rs.getString("unique_identifier"));
                if (rs.wasNull()) {
                    obj.setUniqueIdentifier("");
                }

                // Date of birth
                if (CoreResources.getDBName().equals("oracle")) {
                    obj.setDobCollected(new Boolean(rs.getString("dob_collected").equals("1") ? true : false));
                } else {
                    obj.setDobCollected(rs.getBoolean("dob_collected"));
                }
                if (rs.wasNull()) {
                    obj.setDobCollected(false);
                }

                Integer subjectStatusId = Integer.valueOf(rs.getInt("status_id"));
                if (rs.wasNull()) {
                    subjectStatusId = Integer.valueOf(0);
                }
                obj.setStatus(Status.get(subjectStatusId.intValue()));

                obj.setSecondaryLabel(rs.getString("secondary_label"));
                if (rs.wasNull()) {
                    obj.setSecondaryLabel("");
                }

                // add
                al.add(obj);

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Exception while processing result rows, EntityDAO.processStudySubjects: " + ": " + sqle.getMessage() + ": array length: " + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        return al;
    }

    protected String getSQLSubjectStudySubjectDataset(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint,
            String ecStatusConstraint, String itStatusConstraint, String databaseName) {
        /**
         *
         * SELECT
         *
         * DISTINCT ON (study_subject.study_subject_id ) study_subject.study_subject_id , study_subject.subject_id,
         * study_subject.label, subject.date_of_birth,
         * subject.gender, subject.unique_identifier,subject.dob_collected, subject.status_id,
         * study_subject.secondary_label, study_event.start_time_flag,
         * study_event.end_time_flag FROM study_subject
         *
         *
         * JOIN subject ON (study_subject.subject_id = subject.subject_id::numeric) JOIN study_event ON
         * (study_subject.study_subject_id =
         * study_event.study_subject_id)
         *
         *
         * WHERE
         *
         * study_subject.study_subject_id IN (
         *
         * SELECT DISTINCT studysubjectid FROM
         *
         * (SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 )
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id) )
         * AS SBQTWO )
         *
         *
         *
         */
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            // The original sql for postgresql fetched only one record for each
            // study_subject.study_subject_id.
            // It is possible that there exists multiple records for one
            // study_subject_id.
            // But it is hard to trace which one has been fetched out.
            // Or study_event in the script has never be used.
            // So, I take off the study_event. If something goes wrong, we can
            // come back to recover it.
            return "SELECT distinct study_subject.study_subject_id , study_subject.label,  study_subject.subject_id, "
                    + "  subject.date_of_birth, subject.gender, subject.unique_identifier, subject.dob_collected,  "
                    + "  subject.status_id, study_subject.secondary_label" + "  FROM  " + "     study_subject "
                    + "  JOIN subject ON (study_subject.subject_id = subject.subject_id)  " + "  WHERE  " + "     study_subject.study_subject_id IN  " + "  ( "
                    + "SELECT DISTINCT studysubjectid FROM " + "( "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint) + " ) SBQTWO "
                    + "  ) order by study_subject.study_subject_id";
            /*
             * // Here, for oracle, we go for min(study_event_id) for a // study_subject_id return "SELECT
             * study_subject.study_subject_id , study_subject.label,
             * study_subject.subject_id, " + " subject.date_of_birth, subject.gender, subject.unique_identifier,
             * subject.dob_collected, " + " subject.status_id,
             * study_subject.secondary_label, study_event.start_time_flag, study_event.end_time_flag " + " FROM " + "
             * study_subject " + " JOIN subject ON
             * (study_subject.subject_id = subject.subject_id) " + " JOIN study_event ON (study_subject.study_subject_id
             * = study_event.study_subject_id) " + "
             * WHERE " + " study_subject.study_subject_id IN " + " ( " + "SELECT DISTINCT studysubjectid FROM " + "( " +
             * getSQLDatasetBASE_EVENTSIDE(studyid,
             * studyparentid, sedin, it_in, dateConstraint) + " ) SBQTWO " + " ) and study_event.study_event_id =
             * (select min(se.study_event_id) from
             * study_event se" + " where se.study_subject_id = study_event.study_subject_id) order by
             * study_subject.study_subject_id";
             */
        } else {
            return " SELECT   " + " DISTINCT ON (study_subject.study_subject_id ) "
                    + " study_subject.study_subject_id , study_subject.label,  study_subject.subject_id, "
                    + "  subject.date_of_birth, subject.gender, subject.unique_identifier, subject.dob_collected,  "
                    + "  subject.status_id, study_subject.secondary_label, study_event.start_time_flag, study_event.end_time_flag  " + "  FROM  "
                    + "   study_subject " + "  JOIN subject ON (study_subject.subject_id = subject.subject_id::numeric)  "
                    + "  JOIN study_event ON (study_subject.study_subject_id = study_event.study_subject_id) " + "  WHERE  "
                    + "   study_subject.study_subject_id IN  " + "  ( " + "SELECT DISTINCT studysubjectid FROM " + "( "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint)
                    + " ) AS SBQTWO " + "  ) ";

        }
    }// getSQLSubjectStudySubjectDataset

    /**
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @param it_in
     * @param eb
     * @return
     */
    public boolean loadBASE_ITEMGROUPSIDEHashMap(int studyid, int parentid, String sedin, String it_in, ExtractBean eb) {
        clearSignals();
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        // YW, 09-2008, << modified syntax of sql for oracle database
        String query = getSQLDatasetBASE_ITEMGROUPSIDE(studyid, parentid, sedin, it_in, genDatabaseDateConstraint(eb), ecStatusConstraint, itStatusConstraint);
        // YW, 09-2008 >>
        logger.error("sqlDatasetBase_itemGroupside=" + query);
        boolean bret = false;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            con.setAutoCommit(false);
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            ps.setFetchSize(50);

            rs = ps.executeQuery();
            if (logger.isInfoEnabled()) {
                logger.debug("Executing static query, GenericDAO.select: " + query);
                // logger.info("fond information about result set: was null: "+
                // rs.wasNull());
            }
            // ps.close();
            signalSuccess();
            processBASE_ITEMGROUPSIDERecords(rs, eb);
            bret = true;
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        // return rs;
        return bret;

    }//

    /**
     * Main function call
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @param it_in
     * @param eb
     * @return
     */
    public boolean loadBASE_EVENTINSIDEHashMap(int studyid, int parentid, String sedin, String it_in, ExtractBean eb) {
        clearSignals();
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        // YW, 09-2008, << modified syntax of sql for oracle database
        String query = getSQLDatasetBASE_EVENTSIDE(studyid, parentid, sedin, it_in, this.genDatabaseDateConstraint(eb), ecStatusConstraint, itStatusConstraint);
        // YW, 09-2008>>
        logger.error("sqlDatasetBase_eventside=" + query);
        boolean bret = false;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            con.setAutoCommit(false);
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

            ps = con.prepareStatement(query);
            ps.setFetchSize(50);

            rs = ps.executeQuery();
            logger.debug("Executing static query, GenericDAO.select: " + query);
            // logger.info("fond information about result set: was null: "+
            // rs.wasNull());
            // ps.close();
            signalSuccess();
            bret = processBASE_EVENTSIDERecords(rs, eb);
            // rs.close();
            bret = true;

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        // return rs;
        return bret;

    }//

    /**
     * For each item_data_id stores a getSQLDatasetBASE_ITEMGROUPSIDE object
     *
     * @param rs
     * @return
     */
    public boolean processBASE_ITEMGROUPSIDERecords(ResultSet rs, ExtractBean eb) {// throws
        // SQLException

        int cnt = 0;

        /**
         * fields are: SELECT itemdataid, itemdataordinal, item_group_metadata.item_group_id , item_group.name,
         * itemdesc, itemname, itemvalue, itemunits,
         * crfversioname, crfversionstatusid, dateinterviewed, interviewername, eventcrfdatecompleted,
         * eventcrfdatevalidatecompleted,
         * eventcrfcompletionstatusid, repeat_number, crfid,
         *
         *
         * //and ids studysubjectid, eventcrfid, itemid, crfversionid
         *
         */
        try {
            while (rs.next()) {

                // itemdataid
                Integer vitemdataid = Integer.valueOf(rs.getInt("itemdataid"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // itemdataordinal
                Integer vitemdataordinal = Integer.valueOf(rs.getInt("itemdataordinal"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // item_group_id
                Integer vitem_group_id = Integer.valueOf(rs.getInt("item_group_id"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // itemgroupname
                String vitemgroupname = rs.getString("name");
                if (rs.wasNull()) {
                    vitemgroupname = new String("");
                }

                if ("ungrouped".equalsIgnoreCase(vitemgroupname) && vitemdataordinal <= 0) {
                    vitemdataordinal = 1;
                }

                // itemdesc
                String vitemdesc = rs.getString("itemdesc");
                if (rs.wasNull()) {
                    vitemdesc = new String("");
                }

                // itemname
                String vitemname = rs.getString("itemname");
                if (rs.wasNull()) {
                    vitemname = new String("");
                }

                String vitemvalue = rs.getString("itemvalue");
                // store the
                // vitemvalue = Utils.convertedItemDateValue(vitemvalue, oc_df_string, local_df_string);
                // << should not need the above since we convert upon input, tbh 08/2010 #5312
                if (rs.wasNull()) {
                    vitemvalue = Utils.convertedItemDateValue("", oc_df_string, local_df_string);
                }

                // itemunits
                String vitemunits = rs.getString("itemunits");
                if (rs.wasNull()) {
                    vitemunits = new String("");
                }

                // crfversioname
                String vcrfversioname = rs.getString("crfversioname");
                if (rs.wasNull()) {
                    vcrfversioname = new String("");
                }

                // crfversionstatusid
                Integer vcrfversionstatusid = Integer.valueOf(rs.getInt("crfversionstatusid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                    // vcrfversionstatusid = Integer.valueOf(?);
                }

                // dateinterviewed
                Date vdateinterviewed = rs.getDate("dateinterviewed");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // interviewername
                String vinterviewername = rs.getString("interviewername");
                if (rs.wasNull()) {
                    vinterviewername = new String("");
                }

                // eventcrfdatecompleted
                Timestamp veventcrfdatecompleted = rs.getTimestamp("eventcrfdatecompleted");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfdatevalidatecompleted
                Timestamp veventcrfdatevalidatecompleted = rs.getTimestamp("eventcrfdatevalidatecompleted");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfcompletionstatusid
                Integer veventcrfcompletionstatusid = Integer.valueOf(rs.getInt("eventcrfcompletionstatusid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // repeat_number
                Integer vrepeat_number = Integer.valueOf(rs.getInt("repeat_number"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // crfid
                Integer vcrfid = Integer.valueOf(rs.getInt("crfid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // studysubjectid
                Integer vstudysubjectid = Integer.valueOf(rs.getInt("studysubjectid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfid
                Integer veventcrfid = Integer.valueOf(rs.getInt("eventcrfid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // itemid
                Integer vitemid = Integer.valueOf(rs.getInt("itemid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // crfversionid
                Integer vcrfversionid = Integer.valueOf(rs.getInt("crfversionid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                Integer eventcrfstatusid = Integer.valueOf(rs.getInt("eventcrfstatusid"));
                Integer itemdatatypeid = Integer.valueOf(rs.getInt("itemDataTypeId"));

                // add it to the HashMap
                eb.addEntryBASE_ITEMGROUPSIDE(/* Integer pitemDataId */vitemdataid, /* Integer vitemdataordinal */vitemdataordinal,
                        /* Integer pitemGroupId */vitem_group_id, /* String pitemGroupName */vitemgroupname, itemdatatypeid,
                        /* String pitemDescription */vitemdesc, /* String pitemName */vitemname, /* String pitemValue */vitemvalue,
                        /* String pitemUnits */vitemunits, /* String pcrfVersionName */vcrfversioname,
                        /* Integer pcrfVersionStatusId */vcrfversionstatusid, /* Date pdateInterviewed */vdateinterviewed,
                        /* String pinterviewerName, */vinterviewername, /* Timestamp peventCrfDateCompleted */veventcrfdatecompleted,
                        /* Timestamp peventCrfDateValidateCompleted */veventcrfdatevalidatecompleted,
                        /* Integer peventCrfCompletionStatusId */veventcrfcompletionstatusid,
                        /* Integer repeat_number */vrepeat_number, /* Integer crfId */vcrfid,
                        /* Integer pstudySubjectId */vstudysubjectid, /* Integer peventCrfId */veventcrfid,
                        /* Integer pitemId */vitemid, /* Integer pcrfVersionId */vcrfversionid, eventcrfstatusid

                );

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.addHashMapEntryBASE_ITEMGROUPSIDE: " + ": " + sqle.getMessage()
                        + ": array length: " + eb.getHBASE_ITEMGROUPSIDE().size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        // if (logger.isInfoEnabled()) {
        logger.debug("Loaded addHashMapEntryBASE_ITEMGROUPSIDE: " + eb.getHBASE_EVENTSIDE().size());
        // logger.info("fond information about result set: was null: "+
        // rs.wasNull());
        // }

        return true;
    }

    /**
     * For each item_data_id stores a getSQLDatasetBASE_ITEMGROUPSIDE object
     *
     * @param rs
     * @return
     */
    public boolean processBASE_EVENTSIDERecords(ResultSet rs, ExtractBean eb) {// throws
        // SQLException

        /**
         * fields are: SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * study_event.start_time_flag study_event.end_time_flag study_event.status_id
         * study_event.subject_event_status_id
         *
         *
         * //ids itemid, crfversionid, eventcrfid, studyeventid
         *
         */

        int cnt = 0;
        try {
            while (rs.next()) {

                // itemdataid
                Integer vitemdataid = Integer.valueOf(rs.getInt("itemdataid"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // studysubjectid
                Integer vstudysubjectid = Integer.valueOf(rs.getInt("studysubjectid"));
                if (rs.wasNull()) {
                    // ERROR - should always be different than NULL
                }

                // sample_ordinal
                Integer vsample_ordinal = rs.getInt("sample_ordinal");
                if (rs.wasNull()) {
                    // TODO
                }

                // study_event_definition_id
                Integer vstudy_event_definition_id = Integer.valueOf(rs.getInt("study_event_definition_id"));
                if (rs.wasNull()) {
                    //
                }

                // name
                String vname = rs.getString("name");
                if (rs.wasNull()) {
                    vname = new String("");
                }

                String vlocation = rs.getString("location");
                // store the
                if (rs.wasNull()) {
                    vlocation = new String("");
                }

                // date_start
                Timestamp vdate_start = rs.getTimestamp("date_start");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // date_end
                Timestamp vdate_end = rs.getTimestamp("date_end");
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // BADS FLAG

                // start_time_flag
                Boolean vstart_time_flag;
                if (CoreResources.getDBName().equals("oracle")) {
                    vstart_time_flag = new Boolean(rs.getString("start_time_flag").equals("1") ? true : false);
                    if (rs.wasNull()) {
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vstart_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                    }
                } else {
                    vstart_time_flag = new Boolean(rs.getBoolean("start_time_flag"));
                    if (rs.wasNull()) {
                        // YW 08-17-2007 << Since I didn't investigate
                        // what's the impact if changing true to false,
                        // I only do change for the columns of
                        // "start_time_flag" and "end_time_flag" in the
                        // table study_event
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vstart_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                        // bad idea? what to put, then?
                    }
                } // if

                // end_time_flag
                Boolean vend_time_flag;
                if (CoreResources.getDBName().equals("oracle")) {
                    vend_time_flag = new Boolean(rs.getString("end_time_flag").equals("1") ? true : false);
                    if (rs.wasNull()) {
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vend_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                    }
                } else {
                    vend_time_flag = new Boolean(rs.getBoolean("end_time_flag"));
                    if (rs.wasNull()) {
                        // YW 08-17-2007 << Since I didn't investigate
                        // what's the impact if changing true to false,
                        // I only do change for the columns of
                        // "start_time_flag" and "end_time_flag" in the
                        // table study_event
                        // if (column.equalsIgnoreCase("start_time_flag") ||
                        // column.equalsIgnoreCase("end_time_flag")) {
                        vend_time_flag = new Boolean(false);
                        // } else {
                        // hm.put(column, new Boolean(true));
                        // }
                        // bad idea? what to put, then?
                    }
                } // if

                // status_id
                Integer vstatus_id = Integer.valueOf(rs.getInt("status_id"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // subject_event_status_id
                Integer vsubject_event_status_id = Integer.valueOf(rs.getInt("subject_event_status_id"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // studyeventid
                Integer vstudyeventid = Integer.valueOf(rs.getInt("studyeventid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // eventcrfid
                Integer veventcrfid = Integer.valueOf(rs.getInt("eventcrfid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // itemid
                Integer vitemid = Integer.valueOf(rs.getInt("itemid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // crfversionid
                Integer vcrfversionid = Integer.valueOf(rs.getInt("crfversionid"));
                if (rs.wasNull()) {
                    // TODO - what value default
                }

                // add it to the HashMap
                eb.addEntryBASE_EVENTSIDE(/* Integer pitemDataId */vitemdataid, /* Integer pstudySubjectId */vstudysubjectid,
                        /* Integer psampleOrdinal */vsample_ordinal, /* Integer pstudyEvenetDefinitionId */vstudy_event_definition_id,
                        /* String pstudyEventDefinitionName */vname, /* String pstudyEventLoacation */vlocation,
                        /* Timestamp pstudyEventDateStart */vdate_start, /* Timestamp pstudyEventDateEnd */vdate_end,
                        /* Boolean pstudyEventStartTimeFlag */vstart_time_flag, /* Boolean pstudyEventEndTimeFlag */vend_time_flag,
                        /* Integer pstudyEventStatusId */vstatus_id, /* Integer pstudyEventSubjectEventStatusId */vsubject_event_status_id,
                        /* Integer pitemId */vitemid, /* Integer pcrfVersionId */vcrfversionid,
                        /* Integer peventCrfId */veventcrfid, /* Integer pstudyEventId */vstudyeventid

                );

                // add the item_data_id
                eb.addItemDataIdEntry(vitemdataid);

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.processBASE_EVENTSIDERecords: " + ": " + sqle.getMessage() + ": array length: "
                        + eb.getHBASE_EVENTSIDE().size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        // if (logger.isInfoEnabled()) {
        logger.debug("Loaded addHashMapEntryBASE_EVENTSIDE: " + eb.getHBASE_EVENTSIDE().size());
        // logger.info("fond information about result set: was null: "+
        // rs.wasNull());
        // }

        return true;
    }

    /**
     * There are two base querries for dataset
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @param it_in
     * @return
     */
    protected String getSQLDatasetBASE_EVENTSIDE(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {

        /**
         * NEEEDS to replace four elements: - item_id IN (...) from dataset sql - study_event_definition_id IN (...)
         * from sql dataset - study_id and
         * parent_study_id from current study
         *
         * SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 )
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id)
         */

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return " SELECT  " + " itemdataid,  " + " studysubjectid, study_event.sample_ordinal,  " + " study_event.study_event_definition_id,   "
                    + " study_event_definition.name, study_event.location, study_event.date_start, study_event.date_end, "
                    + " study_event.start_time_flag , study_event.end_time_flag , study_event.status_id, study_event.subject_event_status_id, "
                    + " itemid,  crfversionid,  eventcrfid, studyeventid " + " FROM " + " ( "
                    + " 	SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item.name AS itemname, item.description AS itemdesc,  "
                    + " 	item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + " 	event_crf.study_subject_id as studysubjectid, event_crf.study_event_id AS studyeventid " + " 	FROM item_data, item, event_crf "
                    + " 	JOIN crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + " 	WHERE  " + " 	item_data.item_id = item.item_id " + " 	AND " + " 	item_data.event_crf_id = event_crf.event_crf_id " + " 	AND "
                    + " 	item_data.item_id IN " + it_in + " 	AND item_data.event_crf_id IN  " + " 	( " + " 		SELECT event_crf_id FROM event_crf "
                    + " 		WHERE  " + " 			event_crf.study_event_id IN  " + " 			( "
                    + " 				SELECT study_event_id FROM study_event  " + " 				WHERE "
                    + " 					study_event.study_event_definition_id IN " + sedin + " 				   AND  "
                    + " 					(	study_event.sample_ordinal IS NOT NULL AND "
                    // + " study_event.location IS NOT NULL AND " //JN:Starting 3.1 study event location is no longer
                    // null
                    + " 						study_event.date_start IS NOT NULL  " + " 					) " + " 				   AND "
                    + " 					study_event.study_subject_id IN " + " 				   ( "
                    + " 					SELECT DISTINCT study_subject.study_subject_id " + " 					 FROM  	study_subject   "
                    + " 					 JOIN	study  			ON ( " + " 										study.study_id = study_subject.study_id  "
                    + " 									   AND " + " 										(study.study_id= " + studyid
                    + "OR study.parent_study_id= " + studyparentid + ") " + " 									   ) "
                    + " 					 JOIN	subject  		ON study_subject.subject_id = subject.subject_id "
                    + " 					 JOIN	study_event_definition  ON ( "
                    + " 										study.study_id = study_event_definition.study_id " + " 									    OR "
                    + " 										study.parent_study_id = study_event_definition.study_id "
                    + " 									   ) " + " 					 JOIN	study_event  		ON ( "
                    + " 										study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 									   AND "
                    + " 										study_event_definition.study_event_definition_id = study_event.study_event_definition_id  "
                    + " 									   ) " + " 					 JOIN	event_crf  		ON ( "
                    + " 										study_event.study_event_id = event_crf.study_event_id  "
                    + " 									   AND  "
                    + " 										study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 									   AND " + " 										(event_crf.status_id " + ecStatusConstraint
                    + ") " + " 									   ) " + " 					WHERE " + dateConstraint + " 					    AND "
                    + " 						study_event_definition.study_event_definition_id IN " + sedin + " 				   )  " + " 			) "
                    + " 			AND study_subject_id IN ( " + " 				SELECT DISTINCT study_subject.study_subject_id "
                    + " 				 FROM  	study_subject   " + " 				 JOIN	study  			ON ( "
                    + " 									study.study_id  = study_subject.study_id  " + " 								   AND "
                    + " 									(study.study_id= " + studyid + " OR study.parent_study_id= " + studyparentid + ") "
                    + " 								   ) " + " 				 JOIN	subject  		ON study_subject.subject_id = subject.subject_id  "
                    + " 				 JOIN	study_event_definition  ON ( "
                    + " 									study.study_id  = study_event_definition.study_id  " + " 								    OR  "
                    + " 									study.parent_study_id = study_event_definition.study_id " + " 								   ) "
                    + " 				 JOIN	study_event  		ON ( "
                    + " 									study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 								   AND "
                    + " 									study_event_definition.study_event_definition_id  = study_event.study_event_definition_id  "
                    + " 								   ) " + " 				 JOIN	event_crf  		ON ( "
                    + " 									study_event.study_event_id = event_crf.study_event_id  "
                    + " 								   AND  "
                    + " 									study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 								   AND " + " 									(event_crf.status_id " + ecStatusConstraint + ") "
                    + " 								   ) " + " 				WHERE " + dateConstraint + " 				    AND "
                    + " 					study_event_definition.study_event_definition_id IN " + sedin + " 			) " + " 			AND "
                    + " 			(event_crf.status_id " + ecStatusConstraint + ") " + " 	)  " + " 	AND  " + " 	(item_data.status_id " + itStatusConstraint
                    + ")  " + " ) SBQONE, study_event, study_event_definition " + " WHERE  " + " (study_event.study_event_id = SBQONE.studyeventid) " + " AND "
                    + " (study_event.study_event_definition_id = study_event_definition.study_event_definition_id) " + " ORDER BY itemdataid asc ";
        } else {
            /*
             * TODO: why date constraint has been hard-coded ???
             */
            return " SELECT  " + " itemdataid,  " + " studysubjectid, study_event.sample_ordinal,  " + " study_event.study_event_definition_id,   "
                    + " study_event_definition.name, study_event.location, study_event.date_start, study_event.date_end, "
                    + " study_event.start_time_flag , study_event.end_time_flag , study_event.status_id, study_event.subject_event_status_id, "
                    + " itemid,  crfversionid,  eventcrfid, studyeventid " + " FROM " + " ( "
                    + "   SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item.name AS itemname, item.description AS itemdesc,  "
                    + "   item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + "   event_crf.study_subject_id as studysubjectid, event_crf.study_event_id AS studyeventid " + "   FROM item_data, item, event_crf "
                    + "   JOIN crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + "   WHERE  " + "   item_data.item_id = item.item_id " + "   AND " + "   item_data.event_crf_id = event_crf.event_crf_id " + "   AND "
                    + "   item_data.item_id IN " + it_in + "   AND item_data.event_crf_id IN  " + "   ( " + "       SELECT event_crf_id FROM event_crf "
                    + "       WHERE  " + "           event_crf.study_event_id IN  " + "           ( "
                    + "               SELECT study_event_id FROM study_event  " + "               WHERE "
                    + "                   study_event.study_event_definition_id IN " + sedin + "                  AND  "
                    + "                   (   study_event.sample_ordinal IS NOT NULL AND "
                    // + " study_event.location IS NOT NULL AND " JN: starting 3.1 study_event.location can be null
                    + "                       study_event.date_start IS NOT NULL  " + "                   ) " + "                  AND "
                    + "                   study_event.study_subject_id IN " + "                  ( "
                    + "                   SELECT DISTINCT study_subject.study_subject_id " + "                    FROM   study_subject   "
                    + "                    JOIN   study           ON ( "
                    + "                                       study.study_id::numeric = study_subject.study_id  " + "                                      AND "
                    + "                                       (study.study_id= " + studyid + "OR study.parent_study_id= " + studyparentid + ") "
                    + "                                      ) "
                    + "                    JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                    JOIN   study_event_definition  ON ( "
                    + "                                       study.study_id::numeric = study_event_definition.study_id "
                    + "                                       OR "
                    + "                                       study.parent_study_id = study_event_definition.study_id "
                    + "                                      ) " + "                    JOIN   study_event         ON ( "
                    + "                                       study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                      AND "
                    + "                                       study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                      ) " + "                    JOIN   event_crf       ON ( "
                    + "                                       study_event.study_event_id = event_crf.study_event_id  "
                    + "                                      AND  "
                    + "                                       study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                      AND " + "                                       (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                      ) " + "                   WHERE " + dateConstraint + "                       AND "
                    + "                       study_event_definition.study_event_definition_id IN " + sedin + "                  )  " + "           ) "
                    + "           AND study_subject_id IN ( " + "               SELECT DISTINCT study_subject.study_subject_id "
                    + "                FROM   study_subject   " + "                JOIN   study           ON ( "
                    + "                                   study.study_id::numeric = study_subject.study_id  " + "                                  AND "
                    + "                                   (study.study_id= " + studyid + " OR study.parent_study_id= " + studyparentid + ") "
                    + "                                  ) "
                    + "                JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                JOIN   study_event_definition  ON ( "
                    + "                                   study.study_id::numeric = study_event_definition.study_id  "
                    + "                                   OR  " + "                                   study.parent_study_id = study_event_definition.study_id "
                    + "                                  ) " + "                JOIN   study_event         ON ( "
                    + "                                   study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                  AND "
                    + "                                   study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                  ) " + "                JOIN   event_crf       ON ( "
                    + "                                   study_event.study_event_id = event_crf.study_event_id  " + "                                  AND  "
                    + "                                   study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                  AND " + "                                   (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                  ) " + "               WHERE " + dateConstraint + "                   AND "
                    + "                   study_event_definition.study_event_definition_id IN " + sedin + "           ) " + "           AND "
                    + "           (event_crf.status_id " + ecStatusConstraint + ") " + "   )  " + "   AND  " + "   (item_data.status_id " + itStatusConstraint
                    + ")  " + " ) AS SBQONE, study_event, study_event_definition " + " WHERE  " + " (study_event.study_event_id = SBQONE.studyeventid) "
                    + " AND " + " (study_event.study_event_definition_id = study_event_definition.study_event_definition_id) " + " ORDER BY itemdataid asc ";
        }
    }// getSQLDatasetBASE_EVENTSIDE

    /**
     * This is the second base sql
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @param it_in
     * @return
     */
    protected String getSQLDatasetBASE_ITEMGROUPSIDE(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint,
            String ecStatusConstraint, String itStatusConstraint) {
        /**
         * NEEEDS to replace four elements: - item_id IN (...) from dataset sql - study_event_definition_id IN (...)
         * from sql dataset - study_id and
         * parent_study_id from current study
         *
         *
         * SELECT itemdataid, itemdataordinal, item_group_metadata.item_group_id , item_group.name, itemdesc, itemname,
         * itemvalue, itemunits, crfversioname,
         * crfversionstatusid, crfid, item_group_metadata.repeat_number, dateinterviewed, interviewername,
         * eventcrfdatevalidatecompleted,eventcrfcompletionstatusid,
         *
         *
         * studysubjectid, eventcrfid, itemid, crfversionid FROM ( SELECT item_data.item_data_id AS itemdataid,
         * item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item_data.ordinal AS itemdataordinal, item.name AS itemname, item.description AS itemdesc,
         * item.units AS itemunits, event_crf.event_crf_id
         * AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,
         * crf_version.crf_id AS crfid, event_crf.study_subject_id
         * as studysubjectid, crf_version.status_id AS crfversionstatusid, event_crf.date_interviewed AS
         * dateinterviewed, event_crf.interviewer_name as
         * interviewername, event_crf.date_validate_completed AS eventcrfdatevalidatecompleted,
         * event_crf.completion_status_id AS eventcrfcompletionstatusid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 )
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, item_group_metadata, item_group
         *
         *
         *
         * WHERE
         *
         * (item_group_metadata.item_id = SBQONE.itemid AND item_group_metadata.crf_version_id = SBQONE.crfversionid)
         *
         * AND
         *
         * (item_group.item_group_id = item_group_metadata.item_group_id)
         */

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return " SELECT  " + " itemdataid,  itemdataordinal,"
                    + " item_group_metadata.item_group_id , item_group.name, itemdatatypeid, itemdesc, itemname, itemvalue, itemunits, "
                    + " crfversioname, crfversionstatusid, crfid, item_group_metadata.repeat_number, "
                    + " dateinterviewed, interviewername, eventcrfdatevalidatecompleted, eventcrfdatecompleted, eventcrfcompletionstatusid, "
                    + " studysubjectid, eventcrfid, itemid, crfversionid, eventcrfstatusid " + " FROM " + " ( "
                    + " 	SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item_data.ordinal AS itemdataordinal, item.item_data_type_id As itemdatatypeid, item.name AS itemname, item.description AS itemdesc,  "
                    + " 	item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + " 	event_crf.study_subject_id as studysubjectid, crf_version.status_id AS crfversionstatusid, crf_version.crf_id AS crfid, "
                    + "   event_crf.date_interviewed AS dateinterviewed, event_crf.interviewer_name AS interviewername, event_crf.date_completed AS eventcrfdatecompleted, "
                    + " 	event_crf.date_validate_completed AS eventcrfdatevalidatecompleted, event_crf.completion_status_id AS eventcrfcompletionstatusid, event_crf.status_id AS eventcrfstatusid "
                    + " 	FROM item_data, item, event_crf "
                    + " 	join crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + " 	WHERE  " + " 	item_data.item_id = item.item_id " + " 	AND " + " 	item_data.event_crf_id = event_crf.event_crf_id " + " 	AND "
                    + " 	item_data.item_id IN " + it_in + " 	AND item_data.event_crf_id IN " + " 	( " + " 		SELECT event_crf_id FROM event_crf "
                    + " 		WHERE  " + " 			event_crf.study_event_id IN  " + " 			( "
                    + " 				SELECT study_event_id FROM study_event  " + " 				WHERE "
                    + " 					study_event.study_event_definition_id IN " + sedin + " 				   AND  "
                    + " 					(	study_event.sample_ordinal IS NOT NULL AND " + " 						study_event.location IS NOT NULL AND "
                    + " 						study_event.date_start IS NOT NULL  " + " 					) " + " 				   AND "
                    + " 					study_event.study_subject_id IN " + " 				   ( "
                    + " 					SELECT DISTINCT study_subject.study_subject_id " + " 					 FROM  	study_subject   "
                    + " 					 JOIN	study  			ON ( " + " 										study.study_id = study_subject.study_id  "
                    + " 									   AND " + " 										(study.study_id=" + studyid
                    + " OR study.parent_study_id= " + studyparentid + ") " + " 									   ) "
                    + " 					 JOIN	subject  		ON study_subject.subject_id = subject.subject_id "
                    + " 					 JOIN	study_event_definition  ON ( "
                    + " 										study.study_id = study_event_definition.study_id  "
                    + " 									    OR  "
                    + " 										study.parent_study_id = study_event_definition.study_id "
                    + " 									   ) " + " 					 JOIN	study_event  		ON ( "
                    + " 										study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 									   AND "
                    + " 										study_event_definition.study_event_definition_id = study_event.study_event_definition_id  "
                    + " 									   ) " + " 					 JOIN	event_crf  		ON ( "
                    + " 										study_event.study_event_id = event_crf.study_event_id  "
                    + " 									   AND  "
                    + " 										study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 									   AND " + " 										(event_crf.status_id " + ecStatusConstraint
                    + ") " + " 									   ) " + " 					WHERE " + dateConstraint + " 					    AND "
                    + " 						study_event_definition.study_event_definition_id IN " + sedin + " 				   )  " + " 			) "
                    + " 			AND study_subject_id IN ( " + " 				SELECT DISTINCT study_subject.study_subject_id "
                    + " 				 FROM  	study_subject   " + " 				 JOIN	study  			ON ( "
                    + " 									study.study_id = study_subject.study_id  " + " 								   AND "
                    + " 									(study.study_id=" + studyid + " OR study.parent_study_id= " + studyparentid + " )"
                    + " 								   ) " + " 				 JOIN	subject  		ON study_subject.subject_id = subject.subject_id "
                    + " 				 JOIN	study_event_definition  ON ( "
                    + " 									study.study_id = study_event_definition.study_id  " + " 								    OR  "
                    + " 									study.parent_study_id = study_event_definition.study_id " + " 								   ) "
                    + " 				 JOIN	study_event  		ON ( "
                    + " 									study_subject.study_subject_id = study_event.study_subject_id  "
                    + " 								   AND "
                    + " 									study_event_definition.study_event_definition_id = study_event.study_event_definition_id  "
                    + " 								   ) " + " 				 JOIN	event_crf  		ON ( "
                    + " 									study_event.study_event_id = event_crf.study_event_id  "
                    + " 								   AND  "
                    + " 									study_event.study_subject_id = event_crf.study_subject_id  "
                    + " 								   AND " + " 									(event_crf.status_id " + ecStatusConstraint + ") "
                    + " 								   ) " + " 				WHERE " + dateConstraint + " 				    AND "
                    + " 					study_event_definition.study_event_definition_id IN " + sedin + " 			) " + " 			AND "
                    + " 			(event_crf.status_id " + ecStatusConstraint + ") " + " 	)  " + " 	AND  " + " 	(item_data.status_id " + itStatusConstraint
                    + ")  " + " ) SBQONE, item_group_metadata, item_group " + " WHERE  "
                    + " (item_group_metadata.item_id = SBQONE.itemid AND item_group_metadata.crf_version_id = SBQONE.crfversionid) " + " AND "
                    + " (item_group.item_group_id = item_group_metadata.item_group_id) " + "  ORDER BY itemdataid asc ";
        } else {
            /*
             * TODO: why date constraint has been hard-coded ???
             */

            return " SELECT  " + " itemdataid,  itemdataordinal,"
                    + " item_group_metadata.item_group_id , item_group.name, itemdatatypeid, itemdesc, itemname, itemvalue, itemunits, "
                    + " crfversioname, crfversionstatusid, crfid, item_group_metadata.repeat_number, "
                    + " dateinterviewed, interviewername, eventcrfdatevalidatecompleted, eventcrfdatecompleted, eventcrfcompletionstatusid, "
                    + " studysubjectid, eventcrfid, itemid, crfversionid, eventcrfstatusid " + " FROM " + " ( "
                    + "   SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS itemvalue, item_data.ordinal AS itemdataordinal, item.item_data_type_id AS itemdatatypeid, item.name AS itemname, item.description AS itemdesc,  "
                    + "   item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid,  "
                    + "   event_crf.study_subject_id as studysubjectid, crf_version.status_id AS crfversionstatusid, crf_version.crf_id AS crfid, "
                    + "   event_crf.date_interviewed AS dateinterviewed, event_crf.interviewer_name AS interviewername, event_crf.date_completed AS eventcrfdatecompleted, "
                    + "   event_crf.date_validate_completed AS eventcrfdatevalidatecompleted, event_crf.completion_status_id AS eventcrfcompletionstatusid, event_crf.status_id AS eventcrfstatusid "
                    + "   FROM item_data, item, event_crf "
                    + "   join crf_version  ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id " + ecStatusConstraint + ") "
                    + "   WHERE  " + "   item_data.item_id = item.item_id " + "   AND " + "   item_data.event_crf_id = event_crf.event_crf_id " + "   AND "
                    + "   item_data.item_id IN " + it_in + "   AND item_data.event_crf_id IN " + "   ( " + "       SELECT event_crf_id FROM event_crf "
                    + "       WHERE  " + "           event_crf.study_event_id IN  " + "           ( "
                    + "               SELECT study_event_id FROM study_event  " + "               WHERE "
                    + "                   study_event.study_event_definition_id IN " + sedin + "                  AND  "
                    + "                   (   study_event.sample_ordinal IS NOT NULL AND " + "                       study_event.location IS NOT NULL AND "
                    + "                       study_event.date_start IS NOT NULL  " + "                   ) " + "                  AND "
                    + "                   study_event.study_subject_id IN " + "                  ( "
                    + "                   SELECT DISTINCT study_subject.study_subject_id " + "                    FROM   study_subject   "
                    + "                    JOIN   study           ON ( "
                    + "                                       study.study_id::numeric = study_subject.study_id  " + "                                      AND "
                    + "                                       (study.study_id=" + studyid + " OR study.parent_study_id= " + studyparentid + ") "
                    + "                                      ) "
                    + "                    JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                    JOIN   study_event_definition  ON ( "
                    + "                                       study.study_id::numeric = study_event_definition.study_id  "
                    + "                                       OR  "
                    + "                                       study.parent_study_id = study_event_definition.study_id "
                    + "                                      ) " + "                    JOIN   study_event         ON ( "
                    + "                                       study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                      AND "
                    + "                                       study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                      ) " + "                    JOIN   event_crf       ON ( "
                    + "                                       study_event.study_event_id = event_crf.study_event_id  "
                    + "                                      AND  "
                    + "                                       study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                      AND " + "                                       (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                      ) " + "                   WHERE " + dateConstraint + "                       AND "
                    + "                       study_event_definition.study_event_definition_id IN " + sedin + "                  )  " + "           ) "
                    + "           AND study_subject_id IN ( " + "               SELECT DISTINCT study_subject.study_subject_id "
                    + "                FROM   study_subject   " + "                JOIN   study           ON ( "
                    + "                                   study.study_id::numeric = study_subject.study_id  " + "                                  AND "
                    + "                                   (study.study_id=" + studyid + " OR study.parent_study_id= " + studyparentid + " )"
                    + "                                  ) "
                    + "                JOIN   subject         ON study_subject.subject_id = subject.subject_id::numeric "
                    + "                JOIN   study_event_definition  ON ( "
                    + "                                   study.study_id::numeric = study_event_definition.study_id  "
                    + "                                   OR  " + "                                   study.parent_study_id = study_event_definition.study_id "
                    + "                                  ) " + "                JOIN   study_event         ON ( "
                    + "                                   study_subject.study_subject_id = study_event.study_subject_id  "
                    + "                                  AND "
                    + "                                   study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id  "
                    + "                                  ) " + "                JOIN   event_crf       ON ( "
                    + "                                   study_event.study_event_id = event_crf.study_event_id  " + "                                  AND  "
                    + "                                   study_event.study_subject_id = event_crf.study_subject_id  "
                    + "                                  AND " + "                                   (event_crf.status_id " + ecStatusConstraint + ") "
                    + "                                  ) " + "               WHERE " + dateConstraint + "                   AND "
                    + "                   study_event_definition.study_event_definition_id IN " + sedin + "           ) " + "           AND "
                    + "           (event_crf.status_id " + ecStatusConstraint + ") " + "   )  " + "   AND  " + "   (item_data.status_id " + itStatusConstraint
                    + ")  " + " ) AS SBQONE, item_group_metadata, item_group " + " WHERE  "
                    + " (item_group_metadata.item_id = SBQONE.itemid AND item_group_metadata.crf_version_id = SBQONE.crfversionid) " + " AND "
                    + " (item_group.item_group_id = item_group_metadata.item_group_id) " + "  ORDER BY itemdataid asc ";
        }
    }// getSQLDatasetBASE_ITEMGROUPSIDE

    /**
     *
     * @param sedin
     * @param itin
     * @param currentstudyid
     * @param parentstudyid
     * @return
     */
    protected String getSQLInKeyDatasetHelper(int studyid, int studyparentid, String sedin, String it_in, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {
        /**
         * SELECT DISTINCT study_event.study_event_definition_id, study_event.sample_ordinal, crfv.crf_id, it.item_id,
         * ig.name AS item_group_name FROM event_crf
         * ec
         *
         * JOIN crf_version crfv ON ec.crf_version_id = crfv.crf_version_id AND (ec.status_id = 2::numeric OR
         * ec.status_id = 6::numeric) JOIN item_form_metadata
         * ifm ON crfv.crf_version_id = ifm.crf_version_id LEFT JOIN item_group_metadata igm ON ifm.item_id =
         * igm.item_id AND crfv.crf_version_id::numeric =
         * igm.crf_version_id LEFT JOIN item_group ig ON igm.item_group_id = ig.item_group_id::numeric JOIN item it ON
         * ifm.item_id = it.item_id::numeric JOIN
         * study_event ON study_event.study_event_id = ec.study_event_id AND study_event.study_subject_id =
         * ec.study_subject_id
         *
         * WHERE ec.event_crf_id IN (
         *
         * SELECT DISTINCT eventcrfid FROM ( SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN ( 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018,
         * 1133, 1134, 1198, 1135, 1136, 1137, 1138,
         * 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156,
         * 1157, 1158, 1159, 1160, 1161, 1162, 1163,
         * 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181,
         * 1182, 1183, 1184, 1185, 1186, 1187, 1188,
         * 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197 ) AND item_data.event_crf_id IN ( SELECT event_crf_id
         * FROM event_crf WHERE
         * event_crf.study_event_id IN ( SELECT study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (9) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) )
         * AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (9) ) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id = 6::numeric) )
         * AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id) )
         * AS SBQTWO )
         */

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            return " 	SELECT DISTINCT  " + "	study_event.study_event_definition_id,  " + "	study_event.sample_ordinal,  " + "	crfv.crf_id,  "
                    + "	it.item_id,  " + "	ig.name AS item_group_name  " + "	 FROM  " + " 	event_crf ec  "
                    + " JOIN crf_version crfv ON ec.crf_version_id = crfv.crf_version_id AND (ec.status_id " + ecStatusConstraint + ") "
                    + " JOIN item_form_metadata ifm ON crfv.crf_version_id = ifm.crf_version_id  "
                    + " LEFT JOIN item_group_metadata igm ON ifm.item_id = igm.item_id AND crfv.crf_version_id = igm.crf_version_id  "
                    + " LEFT JOIN item_group ig ON igm.item_group_id = ig.item_group_id  " + " JOIN item it ON ifm.item_id = it.item_id  "
                    + " JOIN study_event ON study_event.study_event_id = ec.study_event_id AND study_event.study_subject_id = ec.study_subject_id   "
                    + " WHERE ec.event_crf_id IN  " + " (  " + "	SELECT DISTINCT eventcrfid FROM  " + "	(     "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint) + "	) SBQTWO "
                    + " ) ";

        } else {
            return "   SELECT DISTINCT  " + "   study_event.study_event_definition_id,  " + "   study_event.sample_ordinal,  " + "   crfv.crf_id,  "
                    + "   it.item_id,  " + "   ig.name AS item_group_name  " + "    FROM  " + "   event_crf ec  "
                    + " JOIN crf_version crfv ON ec.crf_version_id = crfv.crf_version_id AND (ec.status_id " + ecStatusConstraint + ") "
                    + " JOIN item_form_metadata ifm ON crfv.crf_version_id = ifm.crf_version_id  "
                    + " LEFT JOIN item_group_metadata igm ON ifm.item_id = igm.item_id AND crfv.crf_version_id::numeric = igm.crf_version_id  "
                    + " LEFT JOIN item_group ig ON igm.item_group_id = ig.item_group_id::numeric  " + " JOIN item it ON ifm.item_id = it.item_id::numeric  "
                    + " JOIN study_event ON study_event.study_event_id = ec.study_event_id AND study_event.study_subject_id = ec.study_subject_id   "
                    + " WHERE ec.event_crf_id IN  " + " (  " + "   SELECT DISTINCT eventcrfid FROM  " + "   (     "
                    + getSQLDatasetBASE_EVENTSIDE(studyid, studyparentid, sedin, it_in, dateConstraint, ecStatusConstraint, itStatusConstraint)
                    + "   ) AS SBQTWO " + " ) ";
        }
        /**
         * TODO - replace with sql
         */
    }

    /**
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @return
     */
    public HashMap setHashMapInKeysHelper(int studyid, int parentid, String sedin, String itin, String dateConstraint, String ecStatusConstraint,
            String itStatusConstraint) {
        clearSignals();
        // YW, 09-2008, << modified syntax of sql for oracle database
        String query = getSQLInKeyDatasetHelper(studyid, parentid, sedin, itin, dateConstraint, ecStatusConstraint, itStatusConstraint);
        // YW, 09-2008 >>

        HashMap results = new HashMap();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: setHashMapInKeysHelper.select!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            // if (logger.isInfoEnabled()) {
            logger.debug("Executing static query, setHashMapInKeysHelper.select: " + query);
            // logger.info("fond information about result set: was null: "+
            // rs.wasNull());
            // }
            // ps.close();
            signalSuccess();
            results = this.processInKeyDataset(rs);
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        // return rs;
        return results;

    }//

    /**
     * Return directly the HashMap with the key It shouldn't be NULL !! TODO - throw an error if any of the fields is
     * null!
     *
     * @param rs
     * @return
     */
    public HashMap processInKeyDataset(ResultSet rs) {// throws SQLException
        HashMap al = new HashMap();

        try {
            while (rs.next()) {
                String stsed = new String("");
                stsed = ((Integer) rs.getInt("study_event_definition_id")).toString();
                if (rs.wasNull()) {
                    stsed = new String("");
                }

                // second column
                String stso = new String("");
                stso = ((Integer) rs.getInt("sample_ordinal")).toString();
                if (rs.wasNull()) {
                    stso = new String("");
                }

                String stcrf = new String("");
                stcrf = ((Integer) rs.getInt("crf_id")).toString();
                if (rs.wasNull()) {
                    stcrf = new String("");
                }

                String stitem = new String("");
                stitem = ((Integer) rs.getInt("item_id")).toString();
                if (rs.wasNull()) {
                    stitem = new String("");
                }

                String stgn = new String("");
                stgn = rs.getString("item_group_name");
                if (rs.wasNull()) {
                    stgn = new String("");
                }

                /**
                 * build the key as [study_event_definition_id]_[sample_ordinal]_[crf_id]_[item_id]_[item_group_name]
                 */
                String key = stsed + "_" + stso + "_" + stcrf + "_" + stitem + "_" + stgn;

                // add
                al.put(key, new Boolean(true));

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.loadExtractStudySubject: " + ": " + sqle.getMessage() + ": array length: "
                        + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        return al;
    }

    /**
     * ======================================================================================
     * ====================================================================================== Extra helper function to
     * retrieve various report data from
     * database
     */

    /**
     * ******************************************************************************* This returns the final array of
     * strings of event_crf_id
     *
     * @param studyid
     * @param parentid
     * @param sedin
     * @param studysubj_in
     * @return
     */
    public ArrayList getEventCRFIDs(int studyid, int parentid, String sedin, String studysubj_in) {
        clearSignals();
        String query = getSQLEventCRFIDs(studyid, parentid, sedin, studysubj_in);

        ArrayList results = new ArrayList();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: selectStudySubjectIDs!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            // if (logger.isInfoEnabled()) {
            logger.debug("Executing static query, selectStudySubjectIDs: " + query);
            // logger.info("fond information about result set: was null: "+
            // rs.wasNull());
            // }
            // ps.close();
            signalSuccess();
            results = this.processEventCRFIDs(rs);
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, GenericDAO.select: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }

        return results;

    }//

    /**
     * This returns an ArrayList of Strings
     *
     * @param rs
     * @return
     */
    public ArrayList processEventCRFIDs(ResultSet rs) {// throws SQLException
        ArrayList al = new ArrayList();

        try {
            while (rs.next()) {
                String obj = new String("");
                // first column
                obj = ((Integer) rs.getInt("event_crf_id")).toString();
                if (rs.wasNull()) {
                    // NOTE: It shoudln't be NULL!
                    obj = new String("");
                }

                // add
                al.add(obj);

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, processEventCRFIDs: " + ": " + sqle.getMessage() + ": array length: " + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        return al;
    }

    /**
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @param it_in
     * @return
     */
    protected String getSQLEventCRFIDs(int studyid, int studyparentid, String sedin, String it_in) {

        /**
         * This is the SQL that will extract the event_crf_id list
         *
         * SELECT DISTINCT eventcrfid FROM
         *
         * (SELECT
         *
         * itemdataid, studysubjectid, study_event.sample_ordinal, study_event.study_event_definition_id,
         * study_event_definition.name, study_event.location,
         * study_event.date_start, study_event.date_end,
         *
         * itemid, crfversionid, eventcrfid, studyeventid
         *
         * FROM ( SELECT item_data.item_data_id AS itemdataid, item_data.item_id AS itemid, item_data.value AS
         * itemvalue, item.name AS itemname,
         * item.description AS itemdesc, item.units AS itemunits, event_crf.event_crf_id AS eventcrfid, crf_version.name
         * AS crfversioname,
         * crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as studysubjectid,
         * event_crf.study_event_id AS studyeventid
         *
         * FROM item_data, item, event_crf
         *
         * join crf_version ON event_crf.crf_version_id = crf_version.crf_version_id and (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric)
         *
         * WHERE
         *
         * item_data.item_id = item.item_id AND item_data.event_crf_id = event_crf.event_crf_id AND
         *
         * item_data.item_id IN
         *
         * (98, 99, 100, 102, 103, 104, 105, 106, 107, 108, 109, 110, 37, 38, 39, 41, 42, 43, 44, 45, 46, 47, 48, 49,
         * 37, 38, 39, 40, 41, 42, 43, 44, 45, 46,
         * 47, 48, 49, 1632, 1633, 1634, 1635, 1636, 1637, 1638, 1639, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
         * 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
         * 25, 26, 27, 431, 28, 432, 433, 29, 434, 30, 435, 31, 32, 436, 437, 438, 439, 440, 441, 442, 443, 444, 445,
         * 446, 447, 448, 449, 450, 451, 452, 453,
         * 454, 455, 456, 457, 458, 459, 460, 461, 462)
         *
         * AND item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event
         *
         * WHERE study_event.study_event_definition_id IN (2, 7, 3) AND ( study_event.sample_ordinal IS NOT NULL AND
         * study_event.location IS NOT NULL AND
         * study_event.date_start IS NOT NULL ) AND study_event.study_subject_id IN (
         *
         * SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON ( study.study_id::numeric =
         * study_subject.study_id AND
         * (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON study_subject.subject_id =
         * subject.subject_id::numeric JOIN study_event_definition ON
         * ( study.study_id::numeric = study_event_definition.study_id OR study.parent_study_id =
         * study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (2, 7, 3)
         * ) ) AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON
         * ( study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id
         * ) JOIN study_event ON ( study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE
         * (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * study_event_definition.study_event_definition_id IN (2, 7, 3)
         * ) AND (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) AND (item_data.status_id =
         * 2::numeric OR item_data.status_id =
         * 6::numeric) ) AS SBQONE, study_event, study_event_definition
         *
         *
         *
         * WHERE
         *
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id) )
         * AS SBQTWO
         *
         */

        String ret = "";
        /**
         * TODO - implement
         */

        return ret;
    }

    /**
     * ******************************************************************************* Returns a list with
     * study_subject_id
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @return
     */
    public ArrayList getStudySubjectIDs(int studyid, int parentid, String sedin) {
        clearSignals();
        String query = getSQLStudySubjectIDs(studyid, parentid, sedin);

        ArrayList results = new ArrayList();
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ds.getConnection();
            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: getStudySubjectIDs!");
                throw new SQLException();
            }
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            // if (logger.isInfoEnabled()) {
            logger.debug("Executing static query, getStudySubjectIDs: " + query);
            // logger.info("fond information about result set: was null: "+
            // rs.wasNull());
            // }
            // ps.close();
            signalSuccess();
            results = this.processStudySubjectIDs(rs);
            // rs.close();

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exeception while executing static query, getStudySubjectIDs: " + query + ": " + sqle.getMessage());
                logger.error(sqle.getMessage(), sqle);
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return results;

    }//

    /**
     * This returns an ArrayList of Strings
     *
     * @param rs
     * @return
     */
    public ArrayList processStudySubjectIDs(ResultSet rs) {// throws
        // SQLException
        ArrayList al = new ArrayList();

        try {
            while (rs.next()) {
                String obj = new String("");
                // first column
                obj = ((Integer) rs.getInt("study_subject_id")).toString();
                if (rs.wasNull()) {
                    // NOTE: It shoudln't be NULL!
                    obj = new String("");
                }

                // add
                al.add(obj);

            } // while
        } catch (SQLException sqle) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while processing result rows, EntityDAO.loadExtractStudySubject: " + ": " + sqle.getMessage() + ": array length: "
                        + al.size());
                logger.error(sqle.getMessage(), sqle);
            }
        }

        return al;
    }

    /**
     * This returns the SQL with all active study_subject_id.
     *
     * @param studyid
     * @param studyparentid
     * @param sedin
     * @return
     */
    protected String getSQLStudySubjectIDs(int studyid, int studyparentid, String sedin) {
        /*
         * SELECT * FROM study_subject WHERE study_subject_id IN ( SELECT DISTINCT studysubjectid FROM ( SELECT
         * itemdataid, studysubjectid,
         * study_event.sample_ordinal, study_event.study_event_definition_id, study_event_definition.name,
         * study_event.location, study_event.date_start,
         * study_event.date_end, itemid, crfversionid, eventcrfid, studyeventid FROM ( SELECT item_data.item_data_id AS
         * itemdataid, item_data.item_id AS itemid,
         * item_data.value AS itemvalue, item.name AS itemname, item.description AS itemdesc, item.units AS itemunits,
         * event_crf.event_crf_id AS eventcrfid,
         * crf_version.name AS crfversioname, crf_version.crf_version_id AS crfversionid, event_crf.study_subject_id as
         * studysubjectid, event_crf.study_event_id
         * AS studyeventid FROM item_data, item, event_crf JOIN crf_version ON ( (event_crf.crf_version_id =
         * crf_version.crf_version_id) AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE item_data.item_id =
         * item.item_id AND item_data.event_crf_id =
         * event_crf.event_crf_id AND item_data.item_id IN //this is the item_id group from SQL dataset ( 1005, 1006,
         * 1007, 1008, 1009, 1010, 1011, 1012, 1013,
         * 1014, 1015, 1016, 1017, 1018, 1133, 1134, 1198, 1135, 1136, 1137, 1138, 1139, 1140, 1141, 1142, 1143, 1144,
         * 1145, 1146, 1147, 1148, 1149, 1150, 1151,
         * 1152, 1153, 1154, 1155, 1156, 1157, 1158, 1159, 1160, 1161, 1162, 1163, 1164, 1165, 1166, 1167, 1168, 1169,
         * 1170, 1171, 1172, 1173, 1174, 1175, 1176,
         * 1177, 1178, 1179, 1180, 1181, 1182, 1183, 1184, 1185, 1186, 1187, 1188, 1189, 1190, 1191, 1192, 1193, 1194,
         * 1195, 1196, 1197) AND
         * item_data.event_crf_id IN ( SELECT event_crf_id FROM event_crf WHERE event_crf.study_event_id IN ( SELECT
         * study_event_id FROM study_event WHERE
         * //here is the first (from three )replacement - 9 is the event_definition_id from SQL dataset
         * study_event.study_event_definition_id IN (9) AND (
         * study_event.sample_ordinal IS NOT NULL AND study_event.location IS NOT NULL AND study_event.date_start IS NOT
         * NULL ) AND study_event.study_subject_id
         * IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON (
         * study.study_id::numeric = study_subject.study_id AND //here is
         * the )replacement - 2 is the study_id and parent_study_id from SQL dataset (study.study_id=2 OR
         * study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric JOIN study_event_definition ON (
         * study.study_id::numeric = study_event_definition.study_id OR
         * study.parent_study_id = study_event_definition.study_id ) JOIN study_event ON (
         * study_subject.study_subject_id = study_event.study_subject_id AND
         * study_event_definition.study_event_definition_id::numeric = study_event.study_event_definition_id ) JOIN
         * event_crf ON ( study_event.study_event_id =
         * event_crf.study_event_id AND study_event.study_subject_id = event_crf.study_subject_id AND
         * (event_crf.status_id = 2::numeric OR event_crf.status_id =
         * 6::numeric) ) WHERE (date(study_subject.enrollment_date) >= date('1900-01-01')) and
         * (date(study_subject.enrollment_date) <= date('2100-12-31')) AND
         * //here is the second (from three )replacement - 9 is the event_definition_id from SQL dataset
         * study_event_definition.study_event_definition_id IN (9)
         * ) ) AND study_subject_id IN ( SELECT DISTINCT study_subject.study_subject_id FROM study_subject JOIN study ON
         * ( study.study_id::numeric =
         * study_subject.study_id AND (study.study_id=2 OR study.parent_study_id=2) ) JOIN subject ON
         * study_subject.subject_id = subject.subject_id::numeric
         * JOIN study_event_definition ON ( study.study_id::numeric = study_event_definition.study_id OR //here is the
         * )replacement - 2 is the study_id and
         * parent_study_id from SQL dataset study.parent_study_id = study_event_definition.study_id ) JOIN study_event
         * ON ( study_subject.study_subject_id =
         * study_event.study_subject_id AND study_event_definition.study_event_definition_id::numeric =
         * study_event.study_event_definition_id ) JOIN event_crf
         * ON ( study_event.study_event_id = event_crf.study_event_id AND study_event.study_subject_id =
         * event_crf.study_subject_id AND (event_crf.status_id =
         * 2::numeric OR event_crf.status_id = 6::numeric) ) WHERE (date(study_subject.enrollment_date) >=
         * date('1900-01-01')) and
         * (date(study_subject.enrollment_date) <= date('2100-12-31')) AND ////here is the third (from three
         * )replacement - 9 is the event_definition_id from
         * SQL dataset study_event_definition.study_event_definition_id IN (9) ) AND (event_crf.status_id = 2::numeric
         * OR event_crf.status_id = 6::numeric) )
         * AND (item_data.status_id = 2::numeric OR item_data.status_id = 6::numeric) ) AS SBQONE, study_event,
         * study_event_definition WHERE
         * (study_event.study_event_id = SBQONE.studyeventid) AND (study_event.study_event_definition_id =
         * study_event_definition.study_event_definition_id) )
         * AS SBQTWO )
         */

        String ret = "";

        // TODO - to implement

        return ret;
    }// getSQLStudySubjectIDs

    /**
     * @return the oc_df_string
     */
    public String getOc_df_string() {
        return oc_df_string;
    }

    /**
     * @return the local_df_string
     */
    public String getLocal_df_string() {
        return local_df_string;
    }

    public String genDatabaseDateConstraint(ExtractBean eb) {
        String dateConstraint = "";
        String dbName = CoreResources.getDBName();
        String sql = eb.getDataset().getSQLStatement();
        String[] os = sql.split("'");
        if ("postgres".equalsIgnoreCase(dbName)) {
            dateConstraint = " (date(study_subject.enrollment_date) >= date('" + os[1] + "')) and (date(study_subject.enrollment_date) <= date('" + os[3]
                    + "'))";
        } else if ("oracle".equalsIgnoreCase(dbName)) {
            dateConstraint = " trunc(study_subject.enrollment_date) >= to_date('" + os[1] + "') and trunc(study_subject.enrollment_date) <= to_date('" + os[3]
                    + "')";
        }
        return dateConstraint;
    }

    public String getECStatusConstraint(int datasetItemStatusId) {
        String statusConstraint = "";
        switch (datasetItemStatusId) {
        default:
        case 0:
        case 1:
            statusConstraint = "in (2,6)";
            break;
        case 2:
            statusConstraint = "not in (2,6,5,7)";
            break;
        case 3:
            statusConstraint = "not in (5,7)";
            break;
        }
        return statusConstraint;
    }

    public String getItemDataStatusConstraint(int datasetItemStatusId) {
        String statusConstraint = "";
        switch (datasetItemStatusId) {
        default:
        case 0:
        case 1:
            statusConstraint = "in (2,6)";
            break;
        case 2:
            statusConstraint = "not in (6,5,7)"; // 6 is locked.
            break;
        case 3:
            statusConstraint = "not in (5,7)";
            break;
        }
        return statusConstraint;
    }

    public void closePreparedStatement(PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();

        } catch (SQLException sqle) {// eventually throw a custom
            // exception,tbh
            if (logger.isWarnEnabled()) {
                logger.warn("Exception thrown in GenericDAO.closeIfNecessary");
                logger.error(sqle.getMessage(), sqle);
            }
        } // end of catch
    }

}
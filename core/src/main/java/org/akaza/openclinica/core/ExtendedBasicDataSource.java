package org.akaza.openclinica.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.SQLNestedException;
import org.apache.commons.pool.KeyedObjectPoolFactory;

public class ExtendedBasicDataSource extends BasicDataSource {

    int dbServerMajorVersion = 0;
    int dbServerMinorVersion = 0;

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }

    /**
     * Overridden to allow conditional this.connectionInitSqls.
     *
     * @param driverConnectionFactory JDBC connection factory
     * @param statementPoolFactory statement pool factory (null if statement pooling is turned off)
     * @param configuration abandoned connection tracking configuration (null if no tracking)
     * @throws SQLException if an error occurs creating the PoolableConnectionFactory
     */
    @Override
    protected void createPoolableConnectionFactory(ConnectionFactory driverConnectionFactory,
            KeyedObjectPoolFactory statementPoolFactory, AbandonedConfig configuration) throws SQLException {
        PoolableConnectionFactory connectionFactory = null;
        try {
            connectionFactory =
                new PoolableConnectionFactory(driverConnectionFactory,
                                              connectionPool,
                                              statementPoolFactory,
                                              validationQuery,
                                              validationQueryTimeout,
                                              connectionInitSqls,
                                              defaultReadOnly,
                                              defaultAutoCommit,
                                              defaultTransactionIsolation,
                                              defaultCatalog,
                                              configuration);
            validateConnectionFactory(connectionFactory);
            setupConditionalInitSql(connectionFactory);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLNestedException("Cannot create PoolableConnectionFactory (" + e.getMessage() + ")", e);
        }
    }

    /**
     * If postgresql, during database initialisation, get the database server version, later we could set postgres session
     * parameters to provide compatibility with the current recommended server version postgresql-8.4.
     */
    public void setupConditionalInitSql(PoolableConnectionFactory connectionFactory) throws Exception {
        if (!url.contains("postgresql")) {
            return;
        }
        Connection conn = null;
        try {
            conn = (Connection) connectionFactory.makeObject();
            if (conn.isClosed()) {
                throw new SQLException("setupConditionalInitSql: connection closed");
            }
            Statement stmt = null;
            ResultSet rset = null;
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery("SHOW SERVER_VERSION");
                if(!rset.next()) {
                    throw new SQLException("setupConditionalInitSql: didn't return a row");
                }

                String serverVersion = rset.getString(1);
                Pattern pattern = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+).*");
                Matcher matcher = pattern.matcher(serverVersion);
                if (matcher.find()) {
                    this.dbServerMajorVersion = Integer.parseInt(matcher.group("major"));
                    this.dbServerMinorVersion = Integer.parseInt(matcher.group("minor"));
                } else {
                    throw new SQLException("setupConditionalInitSql: cannot fetch server version");
                }

                ArrayList initSqls = new ArrayList();
                if (connectionInitSqls != null) {
                    initSqls.addAll(connectionInitSqls);
                }

                postgresByteAOutputCompatibility(initSqls);

                if (!initSqls.isEmpty()) {
                    connectionFactory.setConnectionInitSql(initSqls);
                }
            } finally {
                if (rset != null) {
                    try {
                        rset.close();
                    } catch (Exception t) {
                        // ignored
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception t) {
                        // ignored
                    }
                }
            }
        } finally {
            if (conn != null) {
                connectionFactory.destroyObject(conn);
            }
        }
    }

    private void postgresByteAOutputCompatibility(ArrayList initSqls) {
        if (dbServerMajorVersion > 8) {
            initSqls.add("SET bytea_output TO 'escape'");
        }
    }
}

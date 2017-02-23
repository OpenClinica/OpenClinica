package org.akaza.openclinica.core;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

public class ExtendedBasicDataSource extends BasicDataSource implements Serializable {

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getGlobal();
    }

    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();

        /*String schema = CoreResources.tenantSchema.get();
        if (StringUtils.isNotEmpty(schema)) {
            if (schema.equalsIgnoreCase(conn.getSchema()))
                return conn;
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.execute("set search_path to '" + schema + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            CoreResources.tenantSchema.set(conn.getSchema());
        }
*/
        return conn;
    }
}
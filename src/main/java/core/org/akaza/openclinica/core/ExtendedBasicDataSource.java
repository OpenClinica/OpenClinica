package core.org.akaza.openclinica.core;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.dbcp2.BasicDataSource;

public class ExtendedBasicDataSource extends BasicDataSource implements Serializable {

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }

    /**
     * Sets the schema on the connection by calling {@link CoreResources#setSchema(Connection)}.
     */
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        CoreResources.setSchema(connection);
        return connection;
    }
}
package org.akaza.openclinica.core;

import java.io.Serializable;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;

public class ExtendedBasicDataSource extends BasicDataSource implements Serializable {

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getGlobal();
    }
}
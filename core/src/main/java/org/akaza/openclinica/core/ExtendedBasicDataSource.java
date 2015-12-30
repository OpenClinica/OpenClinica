package org.akaza.openclinica.core;

import java.util.ArrayList;
import org.apache.commons.dbcp.BasicDataSource;

public class ExtendedBasicDataSource extends BasicDataSource {

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }

    public void init() {
        this.connectionInitSqls = new ArrayList();
        if (url.contains("postgresql")) {
            // TODO: remove when postgresql-8.x support were dropped.
            connectionInitSqls.add("SET bytea_output TO 'escape'");
        }
    }
}
package org.akaza.openclinica.core;

import java.io.Serializable;

import org.apache.commons.dbcp2.BasicDataSource;

public class ExtendedBasicDataSource extends BasicDataSource implements Serializable {

    public void setBigStringTryClob(String value) {
        addConnectionProperty("SetBigStringTryClob", value);
    }
}
package org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This Service class should be used to get core instance properties.
 *
 */

@Service
public class CoreUtilServiceImpl {

    protected final Logger log = LoggerFactory.getLogger(getClass().getName());

    private String customerUuid;

    public void setCustomerUuid(String customerUuid){
        this.customerUuid = customerUuid;
    }

    public String getCustomerUuid() {
        return customerUuid;
    }
}

package org.akaza.openclinica.core;

import liquibase.Liquibase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yogi on 3/16/17.
 */
public class OCCreatePostgresAppServer extends SpringLiquibase {
    @Autowired DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        System.setProperty("dbUser", CoreResources.getField("dbUser"));
        System.setProperty("dbPass", CoreResources.getField("dbPass"));
        System.setProperty("db", CoreResources.getField("db"));
        System.setProperty("dbHost", CoreResources.getField("dbHost"));
        System.setProperty("mappingServer", "mapping_server_" + CoreResources.getField("db"));
        super.afterPropertiesSet();
    }
}

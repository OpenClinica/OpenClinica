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
import java.util.HashMap;
import java.util.List;

/**
 * Created by yogi on 3/16/17.
 */
public class OCCreatePostgresAppServer extends SpringLiquibase {
    @Autowired DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        HashMap<String,String> parameters = new HashMap<>();
        parameters.put("dbUser", CoreResources.getField("dbUser"));
        parameters.put("dbPass", CoreResources.getField("dbPass"));
        parameters.put("db", CoreResources.getField("db"));
        parameters.put("dbHost", CoreResources.getField("dbHost"));
        parameters.put("mappingServer", "mapping_server_" + CoreResources.getField("db"));
        super.setChangeLogParameters(parameters);
        super.afterPropertiesSet();
    }
}

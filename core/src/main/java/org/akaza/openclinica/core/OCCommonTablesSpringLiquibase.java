package org.akaza.openclinica.core;

import liquibase.Liquibase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogi on 3/16/17.
 * This class is not Thread safe multiple threads accessing this class
 * specially when creating new schemas via SBS will cause issues if singleton.
 * @Bean initialized with session scope.
 */
public class OCCommonTablesSpringLiquibase extends SpringLiquibase {
    @Autowired DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        processSchemaLiquibase(null);
    }

    public void processSchemaLiquibase(List<String>schemas) throws LiquibaseException {
        Liquibase liquibase = null;
        if (schemas == null) {
            schemas = new ArrayList<>();
            schemas.add("public");
        }
        for (String schema : schemas) {
            Connection c = null;
            try {
                c = dataSource.getConnection();
                setDefaultSchema(schema);
                c.setSchema(schema);
                liquibase = createLiquibase(c);
                performUpdate(liquibase);
            } catch (Exception e) {
                throw new LiquibaseException(e);
            } finally {
                if (c != null) {
                    try {
                        c.rollback();
                        c.close();
                    } catch (SQLException e) {
                        //nothing to do
                    }
                }
            }
        }
    }
}

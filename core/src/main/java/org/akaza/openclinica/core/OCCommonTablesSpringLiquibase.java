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
 */
public class OCCommonTablesSpringLiquibase extends SpringLiquibase {
    @Autowired DataSource dataSource;
    List<String>schemas = null;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        processSchemaLiquibase();
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public void processSchemaLiquibase() throws LiquibaseException {
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

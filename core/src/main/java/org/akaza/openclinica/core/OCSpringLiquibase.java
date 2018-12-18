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
public class OCSpringLiquibase extends SpringLiquibase {
    @Autowired DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        HashMap<String,String> parameters = new HashMap<>();
        String dbName = CoreResources.getField("db").replaceAll("-", "_");
        parameters.put("mappingServer", "mapping_server_" + dbName);
        super.setChangeLogParameters(parameters);

    }

    public void processSchemaLiquibase(List<String> schemas) throws Exception {
        Liquibase liquibase = null;
        for (String schema : schemas) {
            Connection c = dataSource.getConnection();
            try {
                setDefaultSchema(schema);
                c.setSchema(schema);
                liquibase = createLiquibase(c);
                performUpdate(liquibase);
            } catch (SQLException e) {
                throw new DatabaseException(e);
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

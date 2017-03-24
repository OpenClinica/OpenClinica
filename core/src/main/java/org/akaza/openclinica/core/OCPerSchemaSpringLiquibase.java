package org.akaza.openclinica.core;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.MultiTenantSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.FileSystemResourceAccessor;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogi on 2/17/17.
 */
public class OCPerSchemaSpringLiquibase extends MultiTenantSpringLiquibase {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired StudyDao studyDao;
    @Autowired DataSource dataSource;

    @Override public void afterPropertiesSet() throws Exception {
        List<String> schemas = new ArrayList<>();
        ArrayList<Study> studies = null;
        try {
            studies = studyDao.findAll();
            for (Study study : studies) {
                if (StringUtils.isNotEmpty(study.getSchemaName())) {
                    logger.info("Adding a schema:" + study.getSchemaName() + " to Liquibase");
                    schemas.add(study.getSchemaName());
                }
            }
        } catch (Exception e) {
            logger.info("There are no tables created as of yet.", e.getMessage(), e);
        }
        // there is no schemas to begin with
        if (schemas.size() == 0)
            return;

        super.setSchemas(schemas);
        super.afterPropertiesSet();
    }

    public void dynamicAfterPropertiesSet(List<String> schemas) throws Exception {
        super.setSchemas(schemas);
        super.afterPropertiesSet();
    }
}

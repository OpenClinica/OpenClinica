package org.akaza.openclinica.core;

import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * This class is a copy of SpringLiquibase class
 * Jira ticket has been filed for a bug with connection schema
 * https://liquibase.jira.com/projects/CORE/issues/CORE-3137
 * Once that is fixed and a new SpringLiquibase library is available, this class should be replaced with the library class
 *
 */
public class CustomMultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {
    private Logger log = LogFactory.getLogger(liquibase.integration.spring.MultiTenantSpringLiquibase.class.getName());
    private String jndiBase;
    private final List<DataSource> dataSources = new ArrayList();
    private DataSource dataSource;
    private List<String> schemas;
    private ResourceLoader resourceLoader;
    private String changeLog;
    private String contexts;
    private String labels;
    private Map<String, String> parameters;
    private String defaultSchema;
    private boolean dropFirst = false;
    private boolean shouldRun = true;
    private File rollbackFile;


    public void afterPropertiesSet() throws Exception {
        if (this.dataSource == null && this.schemas == null) {
            this.log.info("DataSources based multitenancy enabled");
            this.resolveDataSources();
            this.runOnAllDataSources();
        } else {
            if (this.dataSource == null && this.schemas != null) {
                throw new LiquibaseException("When schemas are defined you should also define a base dataSource");
            }

            if (this.dataSource != null) {
                this.log.info("Schema based multitenancy enabled");
                if (this.schemas == null || this.schemas.isEmpty()) {
                    this.log.warning("Schemas not defined, using defaultSchema only");
                    this.schemas = new ArrayList();
                    this.schemas.add(this.defaultSchema);
                }

                this.runOnAllSchemas();
            }
        }

    }

    private void resolveDataSources() throws NamingException {
        Context context = new InitialContext();
        int lastIndexOf = this.jndiBase.lastIndexOf("/");
        String jndiRoot = this.jndiBase.substring(0, lastIndexOf);
        String jndiParent = this.jndiBase.substring(lastIndexOf + 1);
        Context base = (Context)context.lookup(jndiRoot);
        NamingEnumeration list = base.list(jndiParent);

        while(list.hasMoreElements()) {
            NameClassPair entry = (NameClassPair)list.nextElement();
            String name = entry.getName();
            String jndiUrl;
            if (entry.isRelative()) {
                jndiUrl = this.jndiBase + "/" + name;
            } else {
                jndiUrl = name;
            }

            Object lookup = context.lookup(jndiUrl);
            if (lookup instanceof DataSource) {
                this.dataSources.add((DataSource)lookup);
                this.log.debug("Added a data source at " + jndiUrl);
            } else {
                this.log.info("Skipping a resource " + jndiUrl + " not compatible with DataSource.");
            }
        }

    }

    private void runOnAllDataSources() throws LiquibaseException {
        Iterator i$ = this.dataSources.iterator();

        while(i$.hasNext()) {
            DataSource aDataSource = (DataSource)i$.next();
            this.log.info("Initializing Liquibase for data source " + aDataSource);
            CustomSpringLiquibase liquibase = this.getSpringLiquibase(aDataSource);
            liquibase.afterPropertiesSet();
            this.log.info("Liquibase ran for data source " + aDataSource);
        }

    }

    private void runOnAllSchemas() throws LiquibaseException {
        Iterator i$ = this.schemas.iterator();

        while(i$.hasNext()) {
            String schema = (String)i$.next();
            if (StringUtils.isEmpty(schema))
                continue;
            if (schema.equals("default")) {
                schema = null;
            }

            this.log.info("Initializing Liquibase for schema " + schema);
            CustomSpringLiquibase liquibase = this.getSpringLiquibase(this.dataSource);
            liquibase.setDefaultSchema(schema);
            liquibase.afterPropertiesSet();
            this.log.info("Liquibase ran for schema " + schema);
        }

    }

    private CustomSpringLiquibase getSpringLiquibase(DataSource dataSource) {
        CustomSpringLiquibase liquibase = new CustomSpringLiquibase();
        liquibase.setChangeLog(this.changeLog);
        liquibase.setChangeLogParameters(this.parameters);
        liquibase.setContexts(this.contexts);
        liquibase.setLabels(this.labels);
        liquibase.setDropFirst(this.dropFirst);
        liquibase.setShouldRun(this.shouldRun);
        liquibase.setRollbackFile(this.rollbackFile);
        liquibase.setResourceLoader(this.resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema(this.defaultSchema);
        return liquibase;
    }

    public String getJndiBase() {
        return this.jndiBase;
    }

    public void setJndiBase(String jndiBase) {
        this.jndiBase = jndiBase;
    }

    public String getChangeLog() {
        return this.changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getContexts() {
        return this.contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public String getLabels() {
        return this.labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getDefaultSchema() {
        return this.defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public boolean isDropFirst() {
        return this.dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public boolean isShouldRun() {
        return this.shouldRun;
    }

    public void setShouldRun(boolean shouldRun) {
        this.shouldRun = shouldRun;
    }

    public File getRollbackFile() {
        return this.rollbackFile;
    }

    public void setRollbackFile(File rollbackFile) {
        this.rollbackFile = rollbackFile;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<String> getSchemas() {
        return this.schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}


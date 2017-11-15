package org.akaza.openclinica.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;
import liquibase.util.file.FilenameUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.sql.DataSource;
import java.io.*;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/***
 * This class is a copy of SpringLiquibase class
 * Jira ticket has been filed for a bug with connection schema
 * https://liquibase.jira.com/projects/CORE/issues/CORE-3137
 * Once that is fixed and a new SpringLiquibase library is available, this class should be replaced with the library class
 *
 */
public class CustomSpringLiquibase implements InitializingBean, BeanNameAware, ResourceLoaderAware {
    protected String beanName;
    protected ResourceLoader resourceLoader;
    protected DataSource dataSource;
    protected final Logger log = LogFactory.getLogger(CustomSpringLiquibase.class.getName());
    protected String changeLog;
    protected String contexts;
    protected String labels;
    protected String tag;
    protected Map<String, String> parameters;
    protected String defaultSchema;
    protected boolean dropFirst = false;
    protected boolean shouldRun = true;
    protected File rollbackFile;
    private boolean ignoreClasspathPrefix = true;

    public boolean isDropFirst() {
        return this.dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public void setShouldRun(boolean shouldRun) {
        this.shouldRun = shouldRun;
    }

    public String getDatabaseProductName() throws DatabaseException {
        Connection connection = null;
        Database database = null;
        String name = "unknown";

        try {
            connection = this.getDataSource().getConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            name = database.getDatabaseProductName();
        } catch (SQLException var12) {
            throw new DatabaseException(var12);
        } finally {
            if (database != null) {
                database.close();
            } else if (connection != null) {
                try {
                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                    }

                    connection.close();
                } catch (Exception var11) {
                    this.log.warning("problem closing connection", var11);
                }
            }

        }

        return name;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getChangeLog() {
        return this.changeLog;
    }

    public void setChangeLog(String dataModel) {
        this.changeLog = dataModel;
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

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDefaultSchema() {
        return this.defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public void afterPropertiesSet() throws LiquibaseException {
        ConfigurationProperty shouldRunProperty = LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, "shouldRun");
        if (!((Boolean)shouldRunProperty.getValue(Boolean.class)).booleanValue()) {
            LogFactory.getLogger().info("Liquibase did not run because " + LiquibaseConfiguration.getInstance().describeValueLookupLogic(shouldRunProperty) + " was set to false");
        } else if (!this.shouldRun) {
            LogFactory.getLogger().info("Liquibase did not run because 'shouldRun' property was set to false on " + this.getBeanName() + " Liquibase Spring bean.");
        } else {
            Connection c = null;
            Liquibase liquibase = null;
            boolean var9 = false;

            try {
                var9 = true;
                c = this.getDataSource().getConnection();
                c.setSchema(this.getDefaultSchema());
                liquibase = this.createLiquibase(c);
                this.generateRollbackFile(liquibase);
                this.performUpdate(liquibase);
                var9 = false;
            } catch (SQLException var10) {
                throw new DatabaseException(var10);
            } finally {
                if (var9) {
                    Database database = null;
                    if (liquibase != null) {
                        database = liquibase.getDatabase();
                    }

                    if (database != null) {
                        database.close();
                    }

                }
            }

            Database database = null;
            if (liquibase != null) {
                database = liquibase.getDatabase();
            }

            if (database != null) {
                database.close();
            }

        }
    }

    private void generateRollbackFile(Liquibase liquibase) throws LiquibaseException {
        if (this.rollbackFile != null) {
            OutputStreamWriter output = null;

            try {
                output = new OutputStreamWriter(new FileOutputStream(this.rollbackFile), ((GlobalConfiguration)LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)).getOutputEncoding());
                if (this.tag != null) {
                    liquibase.futureRollbackSQL(this.tag, new Contexts(this.getContexts()), new LabelExpression(this.getLabels()), output);
                } else {
                    liquibase.futureRollbackSQL(new Contexts(this.getContexts()), new LabelExpression(this.getLabels()), output);
                }
            } catch (IOException var11) {
                throw new LiquibaseException("Unable to generate rollback file.", var11);
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException var10) {
                    this.log.severe("Error closing output", var10);
                }

            }
        }

    }

    protected void performUpdate(Liquibase liquibase) throws LiquibaseException {
        if (this.tag != null) {
            liquibase.update(this.tag, new Contexts(this.getContexts()), new LabelExpression(this.getLabels()));
        } else {
            liquibase.update(new Contexts(this.getContexts()), new LabelExpression(this.getLabels()));
        }

    }

    protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
        CustomSpringLiquibase.SpringResourceOpener resourceAccessor = this.createResourceOpener();
        Liquibase liquibase = new Liquibase(this.getChangeLog(), resourceAccessor, this.createDatabase(c, resourceAccessor));
        liquibase.setIgnoreClasspathPrefix(this.isIgnoreClasspathPrefix());
        if (this.parameters != null) {
            Iterator i$ = this.parameters.entrySet().iterator();

            while(i$.hasNext()) {
                Entry<String, String> entry = (Entry)i$.next();
                liquibase.setChangeLogParameter((String)entry.getKey(), entry.getValue());
            }
        }

        if (this.isDropFirst()) {
            liquibase.dropAll();
        }

        return liquibase;
    }

    protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {
        Object liquibaseConnection;
        if (c == null) {
            this.log.warning("Null connection returned by liquibase datasource. Using offline unknown database");
            liquibaseConnection = new OfflineConnection("offline:unknown", resourceAccessor);
        } else {
            liquibaseConnection = new JdbcConnection(c);
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation((DatabaseConnection)liquibaseConnection);
        if (StringUtils.trimToNull(this.defaultSchema) != null) {
            database.setDefaultSchemaName(this.defaultSchema);
        }

        return database;
    }

    public void setChangeLogParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    protected CustomSpringLiquibase.SpringResourceOpener createResourceOpener() {
        return new CustomSpringLiquibase.SpringResourceOpener(this.getChangeLog());
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    public void setRollbackFile(File rollbackFile) {
        this.rollbackFile = rollbackFile;
    }

    public boolean isIgnoreClasspathPrefix() {
        return this.ignoreClasspathPrefix;
    }

    public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
    }

    public String toString() {
        return this.getClass().getName() + "(" + this.getResourceLoader().toString() + ")";
    }

    public class SpringResourceOpener extends ClassLoaderResourceAccessor {
        private String parentFile;

        public SpringResourceOpener(String parentFile) {
            this.parentFile = parentFile;
        }

        protected void init() {
            super.init();

            try {
                Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(CustomSpringLiquibase.this.getResourceLoader()).getResources("");
                int i$x;
                if (resources.length == 0 || resources.length == 1 && !resources[0].exists()) {
                    Set<String> liquibasePackages = new HashSet();
                    Resource[] arr$ = ResourcePatternUtils.getResourcePatternResolver(CustomSpringLiquibase.this.getResourceLoader()).getResources("META-INF/MANIFEST.MF");
                    i$x = arr$.length;

                    for(int i$xx = 0; i$xx < i$x; ++i$xx) {
                        Resource manifest = arr$[i$xx];
                        if (manifest.exists()) {
                            InputStream inputStream = null;

                            try {
                                inputStream = manifest.getInputStream();
                                Manifest manifestObj = new Manifest(inputStream);
                                Attributes attributes = manifestObj.getAttributes("Liquibase-Package");
                                if (attributes != null) {
                                    Iterator i$xxxx = attributes.values().iterator();

                                    while(i$xxxx.hasNext()) {
                                        Object attr = i$xxxx.next();
                                        String packages = "\\s*,\\s*";
                                        String[] arr$xxx = attr.toString().split(packages);
                                        int len$xx = arr$xxx.length;

                                        for(int i$xxxxx = 0; i$xxxxx < len$xx; ++i$xxxxx) {
                                            String fullPackage = arr$xxx[i$xxxxx];
                                            liquibasePackages.add(fullPackage.split("\\.")[0]);
                                        }
                                    }
                                }
                            } finally {
                                if (inputStream != null) {
                                    inputStream.close();
                                }

                            }
                        }
                    }

                    if (liquibasePackages.size() == 0) {
                        LogFactory.getInstance().getLog().warning("No Liquibase-Packages entry found in MANIFEST.MF. Using fallback of entire 'liquibase' package");
                        liquibasePackages.add("liquibase");
                    }

                    Iterator i$ = liquibasePackages.iterator();

                    while(i$.hasNext()) {
                        String foundPackage = (String)i$.next();
                        resources = ResourcePatternUtils.getResourcePatternResolver(CustomSpringLiquibase.this.getResourceLoader()).getResources(foundPackage);
                        Resource[] arr$xx = resources;
                        int len$x = resources.length;

                        for(int i$xxx = 0; i$xxx < len$x; ++i$xxx) {
                            Resource resx = arr$xx[i$xxx];
                            this.addRootPath(resx.getURL());
                        }
                    }
                } else {
                    Resource[] arr$x = resources;
                    int len$ = resources.length;

                    for(i$x = 0; i$x < len$; ++i$x) {
                        Resource res = arr$x[i$x];
                        this.addRootPath(res.getURL());
                    }
                }
            } catch (IOException var21) {
                LogFactory.getInstance().getLog().warning("Error initializing SpringLiquibase", var21);
            }

        }

        public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
            if (path == null) {
                return null;
            } else {
                Set<String> returnSet = new HashSet();
                String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeTo), path);
                Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(CustomSpringLiquibase.this.getResourceLoader()).getResources(this.adjustClasspath(tempFile));
                Resource[] arr$ = resources;
                int len$ = resources.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    Resource res = arr$[i$];
                    Set<String> list = super.list((String)null, res.getURL().toExternalForm(), includeFiles, includeDirectories, recursive);
                    if (list != null) {
                        returnSet.addAll(list);
                    }
                }

                return returnSet;
            }
        }

        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            if (path == null) {
                return null;
            } else {
                Set<InputStream> returnSet = new HashSet();
                Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(CustomSpringLiquibase.this.getResourceLoader()).getResources(this.adjustClasspath(path));
                if (resources != null && resources.length != 0) {
                    Resource[] arr$ = resources;
                    int len$ = resources.length;

                    for(int i$ = 0; i$ < len$; ++i$) {
                        Resource resource = arr$[i$];
                        LogFactory.getInstance().getLog().debug("Opening " + resource.getURL().toExternalForm() + " as " + path);
                        URLConnection connection = resource.getURL().openConnection();
                        connection.setUseCaches(false);
                        returnSet.add(connection.getInputStream());
                    }

                    return returnSet;
                } else {
                    return null;
                }
            }
        }

        public Resource getResource(String file) {
            return CustomSpringLiquibase.this.getResourceLoader().getResource(this.adjustClasspath(file));
        }

        private String adjustClasspath(String file) {
            if (file == null) {
                return null;
            } else {
                return this.isPrefixPresent(this.parentFile) && !this.isPrefixPresent(file) ? "classpath:" + file : file;
            }
        }

        public boolean isPrefixPresent(String file) {
            if (file == null) {
                return false;
            } else {
                return file.startsWith("classpath") || file.startsWith("file:") || file.startsWith("url:");
            }
        }

        public ClassLoader toClassLoader() {
            return CustomSpringLiquibase.this.getResourceLoader().getClassLoader();
        }
    }
}


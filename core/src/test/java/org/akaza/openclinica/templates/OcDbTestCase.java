package org.akaza.openclinica.templates;

import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Locale;
import java.util.Properties;

public abstract class OcDbTestCase extends DataSourceBasedDBTestCase {

    // @pgawade 10272010 - Added the ApplicationContext attribute
    private ApplicationContext context;

    Properties properties = new Properties();
    private final String dbName;
    private final String dbUrl;
    private final String dbUserName;
    private final String dbPassword;
    private final String dbDriverClassName;
    private final String locale;

    public OcDbTestCase() {
        super();
        loadProperties();
        dbName = properties.getProperty("dbName");
        dbUrl = properties.getProperty("url");
        dbUserName = properties.getProperty("username");
        dbPassword = properties.getProperty("password");
        dbDriverClassName = properties.getProperty("driver");
        locale = properties.getProperty("locale");
        initializeLocale();
        initializeQueriesInXml();

    }

    private void setUpContext() {
        // Loading the applicationContext under test/resources first allows
        // test.properties to be loaded first.Hence we can
        // use different settings.
        context =
            new ClassPathXmlApplicationContext(
                    new String[] { "classpath*:applicationContext*.xml", "classpath*:org/akaza/openclinica/applicationContext*.xml", });
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSet(OcDbTestCase.class.getResourceAsStream(getTestDataFilePath()));
    }

    @Override
    public DataSource getDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setDriverClassName(dbDriverClassName);
        ds.setUsername(dbUserName);
        ds.setPassword(dbPassword);
        ds.setUrl(dbUrl);
        return ds;
    }

    private void loadProperties() {
        try {
            properties.load(OcDbTestCase.class.getResourceAsStream(getPropertiesFilePath()));
        } catch (Exception ioExc) {
            ioExc.printStackTrace();
        }
    }

    private void initializeLocale() {
        ResourceBundleProvider.updateLocale(new Locale(locale));
    }

    /**
     * Instantiates SQLFactory and all the xml files that contain the queries
     * that are used in our dao class
     */
    private void initializeQueriesInXml() {
        String baseDir = System.getProperty("basedir");
        if (baseDir == null || "".equalsIgnoreCase(baseDir)) {
            throw new IllegalStateException(
                    "The system properties basedir were not made available to the application. Therefore we cannot locate the test properties file.");
        }
        // @pgawade 05-Nov-2010 Updated the path of directory storing xml files
        // containing sql queries
        // SQLFactory.JUNIT_XML_DIR =
        // baseDir + File.separator + "src" + File.separator + "main" +
        // File.separator + "webapp" + File.separator + "properties" +
        // File.separator;
     /*   SQLFactory.JUNIT_XML_DIR =
            baseDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "properties" + File.separator;
*/
        // @pgawade 05-Nov-2010 Updated the path of directory storing xml files
        // containing sql queries
        // SQLFactory.getInstance().run(dbName);
        SQLFactory.getInstance().run(dbName, context);
    }

    private String getPropertiesFilePath() {
        return "/datainfo.properties";
    }

    /**
     * Gets the path and the name of the xml file holding the data. Example if
     * your Class Name is called
     * org.akaza.openclinica.service.rule.expression.TestExample.java you need
     * an xml data file in resources folder under same path + testdata + same
     * Class Name .xml
     * org/akaza/openclinica/service/rule/expression/testdata/TestExample.xml
     * 
     * @return path to data file
     */
    private String getTestDataFilePath() {
        StringBuffer path = new StringBuffer("/");
        path.append(getClass().getPackage().getName().replace(".", "/"));
        path.append("/testdata/");
        path.append(getClass().getSimpleName() + ".xml");
        return path.toString();
    }
}
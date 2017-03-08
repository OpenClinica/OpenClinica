package org.akaza.openclinica.templates;

import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.util.Locale;
import java.util.Properties;

public abstract class HibernateOcDbTestCase extends DataSourceBasedDBTestCase {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
   // public static PlatformTransactionManager transactionManager;
    protected static ApplicationContext context;

    protected static Properties properties = new Properties();
    public static String dbName;
    public static String dbUrl;
    public static String dbUserName;
    public static String dbPassword;
    public static String dbDriverClassName;
    public static String locale;
    public  BasicDataSource ds ;
    
   protected static  PlatformTransactionManager transactionManager;
   static
   {
       
       loadProperties();
       dbName = properties.getProperty("dbName");
       dbUrl = properties.getProperty("url");
       dbUserName = properties.getProperty("username");
       dbPassword = properties.getProperty("password");
       dbDriverClassName = properties.getProperty("driver");
       locale = properties.getProperty("locale");
       initializeLocale();
       initializeQueriesInXml();
      
    
       
       context =
           new ClassPathXmlApplicationContext(
                   new String[] { "classpath*:applicationContext-core-s*.xml", "classpath*:org/akaza/openclinica/applicationContext-core-db.xml",
                       "classpath*:org/akaza/openclinica/applicationContext-core-email.xml",
                       "classpath*:org/akaza/openclinica/applicationContext-core-hibernate.xml",
                       "classpath*:org/akaza/openclinica/applicationContext-core-service.xml",
                      " classpath*:org/akaza/openclinica/applicationContext-core-timer.xml",
                       "classpath*:org/akaza/openclinica/applicationContext-security.xml" });
     transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
     transactionManager.getTransaction(new DefaultTransactionDefinition());
       

   }
  

    public HibernateOcDbTestCase() {
   
      
    }

    @Override
    protected void setUp() throws Exception {
     
    /*    loadProperties();
        dbName = properties.getProperty("dbName");
        dbUrl = properties.getProperty("url");
        dbUserName = properties.getProperty("username");
        dbPassword = properties.getProperty("password");
        dbDriverClassName = properties.getProperty("driver");
        locale = properties.getProperty("locale");
        initializeLocale();
        initializeQueriesInXml();*/
       // setUpContext();
        // TODO Auto-generated method stub
        super.setUp();

    }
    @Override
    protected IDatabaseTester newDatabaseTester() {
        logger.debug("newDatabaseTester() - start");
        return new DataSourceDatabaseTester(this.getDataSource(), "public");
    }

    private void setUpContext() {
        // Loading the applicationContext under test/resources first allows
        // test.properties to be loaded first.Hence we can
        // use different settings.
       /* context =
            new ClassPathXmlApplicationContext(
                    new String[] { "classpath*:applicationContext-core-s*.xml", "classpath*:org/akaza/openclinica/applicationContext-core-db.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-email.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-hibernate.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-scheduler.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-service.xml",
                       " classpath*:org/akaza/openclinica/applicationContext-core-timer.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-security.xml" });
        transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
        transactionManager.getTransaction(new DefaultTransactionDefinition());*/
        
    }

    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, "true");
    }


    @Override
    protected IDataSet getDataSet() throws Exception {
        FlatXmlDataSetBuilder setBuilder = new FlatXmlDataSetBuilder();
        return setBuilder.build(HibernateOcDbTestCase.class.getResourceAsStream(getTestDataFilePath()));
    }

    @Override
    public  DataSource getDataSource() {
       ds = new BasicDataSource();
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setDriverClassName(dbDriverClassName);
        ds.setUsername(dbUserName);
        ds.setPassword(dbPassword);
        ds.setUrl(dbUrl);
        return ds;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public static void loadProperties() {
        try {
            properties.load(HibernateOcDbTestCase.class.getResourceAsStream(getPropertiesFilePath()));
        } catch (Exception ioExc) {
            ioExc.printStackTrace();
        }
    }

    protected static void initializeLocale() {
        ResourceBundleProvider.updateLocale(new Locale(locale));
    }

    /**
     * Instantiates SQLFactory and all the xml files that contain the queries
     * that are used in our dao class
     */
    protected static void initializeQueriesInXml() {
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
  
        //Revisit this later
        /*    SQLFactory.JUNIT_XML_DIR =
            baseDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "properties" + File.separator;
*/
        // @pgawade 10272010 - Added the ResourceLoader instance as a parameter
        // to run method of SQLFactory
        // SQLFactory.getnstance().run(dbName);
        SQLFactory.getInstance().run(dbName, context);
    }

    
    private static String getPropertiesFilePath() {
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

    public String getDbName() {
        return dbName;
    }
  @Override
  public void tearDown(){
    
      try {
          transactionManager.commit( transactionManager.getTransaction(new DefaultTransactionDefinition()));
        super.tearDown();
      //  transactionManager = null;
       if(ds!=null)
        ds.getConnection().close();
       // getDataSource().getConnection().close();
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
  }
}
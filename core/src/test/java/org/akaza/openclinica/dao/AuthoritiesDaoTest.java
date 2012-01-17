package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.hibernate.HibernateException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;
import java.util.List;

public class AuthoritiesDaoTest extends HibernateOcDbTestCase {
    private static AuthoritiesDao authoritiesDao;
   /* 
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
     // transactionManager.getTransaction(new DefaultTransactionDefinition());
        

    }
*/
    public void setUp() throws Exception{
        super.setUp();
        authoritiesDao = (AuthoritiesDao) getContext().getBean("authoritiesDao");
        
    }
  
    public void testSaveOrUpdate() {
    	//AuthoritiesDao authoritiesDao = (AuthoritiesDao) getContext().getBean("authoritiesDao");
        AuthoritiesBean authorities = new AuthoritiesBean();
        authorities.setUsername("root");
        authorities.setAuthority("ROLE_USER");
        authorities.setId(-1);
        authorities = authoritiesDao.saveOrUpdate(authorities);

        assertNotNull("Persistant id is null", authorities.getId());
    }

    public void testFindById() {
    //	AuthoritiesDao authoritiesDao = (AuthoritiesDao) getContext().getBean("authoritiesDao");
        
    	AuthoritiesBean authorities = null;
    	authorities = authoritiesDao.findById(-1);

        // Test Authorities
        assertNotNull("RuleSet is null", authorities);
        assertEquals("The id of the retrieved Domain Object should be -1", new Integer(-1), authorities.getId());
   }
    public void testFindByUsername() {

        
        AuthoritiesBean authorities = null;
        authorities = authoritiesDao.findByUsername("root");
        
      
        // Test Authorities
        assertNotNull("RuleSet is null", authorities);
        assertEquals("The id of the retrieved Domain Object should be -1", new Integer(-1), authorities.getId());
    }
    
    
    
    
    public void tearDown(){
        try {
           
            authoritiesDao.getSessionFactory().getCurrentSession().close();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.tearDown();
    }

}
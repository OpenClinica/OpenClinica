package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.hibernate.HibernateException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;
import java.util.List;

public class RuleSetAuditDaoTest extends HibernateOcDbTestCase {
    private static RuleSetAuditDao ruleSetAuditDao;
    private static RuleSetDao ruleSetDao;
   
  /*  static
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
                        "classpath*:org/akaza/openclinica/applicationContext-core-scheduler.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-service.xml",
                       " classpath*:org/akaza/openclinica/applicationContext-core-timer.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-security.xml" });
      transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
      transactionManager.getTransaction(new DefaultTransactionDefinition());
        

    }*/
    
    
    public RuleSetAuditDaoTest() {
        super();
         ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");
         ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
    }

    public void testFindAllByRuleSet() {
      //RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
       // RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");

        RuleSetBean ruleSet = ruleSetDao.findById(-1);
        List<RuleSetAuditBean> ruleSetAudits = ruleSetAuditDao.findAllByRuleSet(ruleSet);

        assertNotNull("ruleSetAudits is null", ruleSetAudits);
        assertEquals("The size of the ruleSetAudits is not 2", new Integer(2), Integer.valueOf(ruleSetAudits.size()));

    }

    public void testFindById() {
       // RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");

        RuleSetAuditBean ruleSetAuditBean = ruleSetAuditDao.findById(-1);

        assertNotNull("ruleSetRuleAuditBean is null", ruleSetAuditBean);
        assertEquals("The ruleSetRuleAuditBean.getRuleSetRule.getId should be 3", new Integer(-1), Integer.valueOf(ruleSetAuditBean.getRuleSetBean().getId()));

    }

    public void testSaveOrUpdate() {
   //     RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");
       // RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetBean ruleSetBean = ruleSetDao.findById(-1);

        RuleSetAuditBean ruleSetAuditBean = new RuleSetAuditBean();
        ruleSetAuditBean.setRuleSetBean(ruleSetBean);
        ruleSetAuditBean = ruleSetAuditDao.saveOrUpdate(ruleSetAuditBean);

        assertNotNull("Persistant id is null", ruleSetAuditBean.getId());
    }
    public void tearDown(){
        try {
            ruleSetAuditDao.getSessionFactory().getCurrentSession().close();
            ruleSetAuditDao.getSessionFactory().getCurrentSession().close();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.tearDown();
    }
}
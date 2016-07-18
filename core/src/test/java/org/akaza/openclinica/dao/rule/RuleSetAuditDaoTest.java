package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.hibernate.HibernateException;

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

        RuleSetAuditBean ruleSetAuditBean = null;
        try {
            ruleSetAuditBean = ruleSetAuditDao.findById(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertNotNull("ruleSetRuleAuditBean is null", ruleSetAuditBean);
        assertEquals("The ruleSetRuleAuditBean.getRuleSetRule.getId should be -1", new Integer(-1), Integer.valueOf(ruleSetAuditBean.getRuleSetBean().getId()));
    }

    public void testSaveOrUpdate() {
        //     RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");
       // RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetBean ruleSetBean = ruleSetDao.findById(-1);

        RuleSetAuditBean ruleSetAuditBean = new RuleSetAuditBean();
        ruleSetAuditBean.setRuleSetBean(ruleSetBean);
        try {
        ruleSetAuditBean = ruleSetAuditDao.saveOrUpdate(ruleSetAuditBean);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertNotNull("Persistant id is null", ruleSetAuditBean.getId());
    }

    public void tearDown(){
        try {
            // if there are any uncommitted transactions, commit them now
            if (ruleSetAuditDao.getCurrentSession().getTransaction().isActive())
                ruleSetAuditDao.getCurrentSession().getTransaction().commit();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.tearDown();
    }
}
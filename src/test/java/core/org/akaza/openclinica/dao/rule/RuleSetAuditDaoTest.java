package core.org.akaza.openclinica.dao.rule;

import core.org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import core.org.akaza.openclinica.dao.hibernate.RuleSetDao;
import core.org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.hibernate.HibernateException;
import org.junit.Ignore;

import java.util.List;

@Ignore
public class RuleSetAuditDaoTest extends HibernateOcDbTestCase {
    private static RuleSetAuditDao ruleSetAuditDao;
    private static RuleSetDao ruleSetDao;
    
    
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
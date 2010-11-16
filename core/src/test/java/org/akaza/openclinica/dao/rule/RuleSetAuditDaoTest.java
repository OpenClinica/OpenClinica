package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

import java.util.List;

public class RuleSetAuditDaoTest extends HibernateOcDbTestCase {

    public RuleSetAuditDaoTest() {
        super();
    }

    public void testFindAllByRuleSet() {
        RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");

        RuleSetBean ruleSet = ruleSetDao.findById(-1);
        List<RuleSetAuditBean> ruleSetAudits = ruleSetAuditDao.findAllByRuleSet(ruleSet);

        assertNotNull("ruleSetAudits is null", ruleSetAudits);
        assertEquals("The size of the ruleSetAudits is not 2", new Integer(2), Integer.valueOf(ruleSetAudits.size()));

    }

    public void testFindById() {
        RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");

        RuleSetAuditBean ruleSetAuditBean = ruleSetAuditDao.findById(-1);

        assertNotNull("ruleSetRuleAuditBean is null", ruleSetAuditBean);
        assertEquals("The ruleSetRuleAuditBean.getRuleSetRule.getId should be 3", new Integer(-1), Integer.valueOf(ruleSetAuditBean.getRuleSetBean().getId()));

    }

    public void testSaveOrUpdate() {
        RuleSetAuditDao ruleSetAuditDao = (RuleSetAuditDao) getContext().getBean("ruleSetAuditDao");
        RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetBean ruleSetBean = ruleSetDao.findById(-1);

        RuleSetAuditBean ruleSetAuditBean = new RuleSetAuditBean();
        ruleSetAuditBean.setRuleSetBean(ruleSetBean);
        ruleSetAuditBean = ruleSetAuditDao.saveOrUpdate(ruleSetAuditBean);

        assertNotNull("Persistant id is null", ruleSetAuditBean.getId());
    }

}
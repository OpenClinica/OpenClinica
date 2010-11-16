package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

import java.util.List;

public class RuleSetRuleAuditDaoTest extends HibernateOcDbTestCase {

    public RuleSetRuleAuditDaoTest() {
        super();
    }

    public void testFindAllByRuleSet() {
        RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetRuleAuditDao ruleSetRuleAuditDao = (RuleSetRuleAuditDao) getContext().getBean("ruleSetRuleAuditDao");

        RuleSetBean ruleSet = ruleSetDao.findById(-1);
        List<RuleSetRuleAuditBean> ruleSetRuleAudits = ruleSetRuleAuditDao.findAllByRuleSet(ruleSet);

        assertNotNull("ruleSetAudits is null", ruleSetRuleAudits);
        assertEquals("The size of the ruleSetRuleAudits is not 2", new Integer(2), Integer.valueOf(ruleSetRuleAudits.size()));

    }

    public void testFindById() {
        RuleSetRuleAuditDao ruleSetRuleAuditDao = (RuleSetRuleAuditDao) getContext().getBean("ruleSetRuleAuditDao");

        RuleSetRuleAuditBean ruleSetRuleAuditBean = ruleSetRuleAuditDao.findById(-1);

        assertNotNull("ruleSetRuleAuditBean is null", ruleSetRuleAuditBean);
        assertEquals("The ruleSetRuleAuditBean.getRuleSetRule.getId should be 3", new Integer(3), Integer.valueOf(ruleSetRuleAuditBean.getRuleSetRuleBean()
                .getId()));

    }

    public void testSaveOrUpdate() {
        RuleSetRuleAuditDao ruleSetRuleAuditDao = (RuleSetRuleAuditDao) getContext().getBean("ruleSetRuleAuditDao");
        RuleSetRuleDao ruleSetRuleDao = (RuleSetRuleDao) getContext().getBean("ruleSetRuleDao");
        RuleSetRuleBean ruleSetRuleBean = ruleSetRuleDao.findById(3);

        RuleSetRuleAuditBean ruleSetRuleAuditBean = new RuleSetRuleAuditBean();
        ruleSetRuleAuditBean.setRuleSetRuleBean(ruleSetRuleBean);
        ruleSetRuleAuditBean = ruleSetRuleAuditDao.saveOrUpdate(ruleSetRuleAuditBean);

        assertNotNull("Persistant id is null", ruleSetRuleAuditBean.getId());
    }
}
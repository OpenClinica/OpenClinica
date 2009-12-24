package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleAuditBean;

import java.util.ArrayList;

public class RuleSetRuleAuditDao extends AbstractDomainDao<RuleSetRuleAuditBean> {

    @Override
    public Class<RuleSetRuleAuditBean> domainClass() {
        return RuleSetRuleAuditBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetRuleAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetRuleAudit  where ruleSetRuleAudit.ruleSetRuleBean.ruleSetBean = :ruleSet  ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setParameter("ruleSet", ruleSet);
        return (ArrayList<RuleSetRuleAuditBean>) q.list();
    }
}

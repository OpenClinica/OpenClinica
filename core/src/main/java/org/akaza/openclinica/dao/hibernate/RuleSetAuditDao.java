package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;

import java.util.ArrayList;

public class RuleSetAuditDao extends AbstractDomainDao<RuleSetAuditBean> {

    @Override
    public Class<RuleSetAuditBean> domainClass() {
        return RuleSetAuditBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetAudit  where ruleSetAudit.ruleSetBean = :ruleSet  ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setParameter("ruleSet", ruleSet);
        return (ArrayList<RuleSetAuditBean>) q.list();
    }
}

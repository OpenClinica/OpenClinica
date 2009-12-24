package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;

import java.util.ArrayList;

public class RuleSetRuleDao extends AbstractDomainDao<RuleSetRuleBean> {

    @Override
    public Class<RuleSetRuleBean> domainClass() {
        return RuleSetRuleBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetRuleBean> findByRuleSetBeanAndRuleBean(RuleSetBean ruleSetBean, RuleBean ruleBean) {
        String query = "from " + getDomainClassName() + " ruleSetRule  where ruleSetRule.ruleSetBean = :ruleSetBean" + " AND ruleSetRule.ruleBean = :ruleBean ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setParameter("ruleSetBean", ruleSetBean);
        q.setParameter("ruleBean", ruleBean);
        return (ArrayList<RuleSetRuleBean>) q.list();
    }
}

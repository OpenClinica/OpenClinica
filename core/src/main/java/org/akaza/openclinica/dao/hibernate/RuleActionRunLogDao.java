package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.hibernate.criterion.Example;

public class RuleActionRunLogDao extends AbstractDomainDao<RuleActionRunLogBean> {

    @Override
    public Class<RuleActionRunLogBean> domainClass() {
        return RuleActionRunLogBean.class;
    }

    public RuleActionRunLogBean findByRuleActionRunLogBean(RuleActionRunLogBean ruleActionRunLog) {
        return (RuleActionRunLogBean) getCurrentSession().createCriteria(domainClass()).add(Example.create(ruleActionRunLog)).uniqueResult();
    }

}

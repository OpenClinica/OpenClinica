package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.hibernate.Query;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.springframework.transaction.annotation.Transactional;

public class RuleActionRunLogDao extends AbstractDomainDao<RuleActionRunLogBean> {

    @Override
    public Class<RuleActionRunLogBean> domainClass() {
        return RuleActionRunLogBean.class;
    }

    @Transactional
    public Integer findCountByRuleActionRunLogBean(RuleActionRunLogBean ruleActionRunLog) {
        Long k = (Long) getCurrentSession().createCriteria(domainClass()).add(Example.create(ruleActionRunLog)).setProjection(Projections.rowCount()).list()
                .get(0);
        return k.intValue();
    }

    public void delete(int itemDataId) {
        String query = " delete from " + getDomainClassName() + "  where itemDataId =:itemDataId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("itemDataId", itemDataId);
        q.executeUpdate();
    }

    public List<RuleActionRunLogBean> findAllItemData(int itemDataId) {
        String query = "from RuleActionRunLogBean logbean where " + "logbean.itemDataId =:itemDataId ";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("itemDataId", itemDataId);
        List<RuleActionRunLogBean> list = (List<RuleActionRunLogBean>) q.list();
        return list;
    }

}

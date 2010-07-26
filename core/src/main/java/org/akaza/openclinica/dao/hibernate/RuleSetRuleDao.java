package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;

import java.math.BigInteger;
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

    public int getCountWithFilter(final ViewRuleAssignmentFilter filter) {

        // Using a sql query because we are referencing objects not managed by hibernate
        String query =
            "select COUNT(DISTINCT(rsr.id)) from rule_set_rule rsr " + " join rule_set rs on rs.id = rsr.rule_set_id "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule r on r.id = rsr.rule_id "
                + " join rule_expression rer on r.rule_expression_id = rer.id " + " join rule_action ra on ra.rule_set_rule_id = rsr.id " + " where ";

        query += filter.execute("");
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);

        return ((BigInteger) q.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetRuleBean> getWithFilterAndSort(final ViewRuleAssignmentFilter filter, final ViewRuleAssignmentSort sort, final int rowStart,
            final int rowEnd) {

        String query =
            "select DISTINCT(rsr.*),i.name,re.value,sed.name,c.name,cv.name,ig.name,rer.value,r.oc_oid,r.description,r.name from rule_set_rule rsr "
                + " join rule_set rs on rs.id = rsr.rule_set_id "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule r on r.id = rsr.rule_id "
                + " join rule_expression rer on r.rule_expression_id = rer.id " + " join rule_action ra on ra.rule_set_rule_id = rsr.id " + " where ";

        query += filter.execute("");
        query += sort.execute("");
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setFirstResult(rowStart);
        q.setMaxResults(rowEnd - rowStart);
        return (ArrayList<RuleSetRuleBean>) q.list();
    }
}

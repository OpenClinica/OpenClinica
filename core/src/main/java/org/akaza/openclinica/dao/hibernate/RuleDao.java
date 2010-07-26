package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleBean;

public class RuleDao extends AbstractDomainDao<RuleBean> {

    @Override
    public Class<RuleBean> domainClass() {
        return RuleBean.class;
    }

    public RuleBean findByOid(RuleBean ruleBean) {
        String query = "from " + getDomainClassName() + " rule  where rule.oid = :oid and  rule.studyId = :studyId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("oid", ruleBean.getOid());
        q.setInteger("studyId", ruleBean.getStudyId());
        return (RuleBean) q.uniqueResult();
    }

    public RuleBean findByOid(String oid, Integer studyId) {
        String query = "from " + getDomainClassName() + " rule  where rule.oid = :oid and  rule.studyId = :studyId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("oid", oid);
        q.setInteger("studyId", studyId);
        return (RuleBean) q.uniqueResult();
    }

}

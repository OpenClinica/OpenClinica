/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
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

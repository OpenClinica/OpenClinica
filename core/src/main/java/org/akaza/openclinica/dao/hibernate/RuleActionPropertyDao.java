package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.rule.action.PropertyBean;

public class RuleActionPropertyDao extends AbstractDomainDao<PropertyBean> {

    @Override
    public Class<PropertyBean> domainClass() {
        return PropertyBean.class;
    }

    public ArrayList <PropertyBean> findByOid(String itemOid , String groupOid) {
        String query = "from " + getDomainClassName() +  "  where oc_oid = :itemOid OR oc_oid=:groupOid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("itemOid", itemOid);
        q.setString("groupOid", groupOid);
        return (ArrayList <PropertyBean>) q.list();
    }

}

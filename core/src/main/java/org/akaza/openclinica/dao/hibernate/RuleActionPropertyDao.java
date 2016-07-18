package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.action.PropertyBean;

import java.util.ArrayList;

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

    public ArrayList <PropertyBean> findByOid(String Oid) {
        String query = "from " + getDomainClassName() +  "  where oc_oid=:Oid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("Oid", Oid);
        return (ArrayList <PropertyBean>) q.list();
    }


}

package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.ItemGroup;

public class ItemGroupDao extends AbstractDomainDao<ItemGroup> {

    @Override
    Class<ItemGroup> domainClass() {
        // TODO Auto-generated method stub
        return ItemGroup.class;
    }

    public ItemGroup findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("OCOID", OCOID);
        return (ItemGroup) q.uniqueResult();
    }

    public ItemGroup findByNameCrfId(String groupName, CrfBean crf) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.name = :groupName and do.crf = :crf";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("groupName", groupName);
        q.setEntity("crf", crf);
        return (ItemGroup) q.uniqueResult();
    }
}

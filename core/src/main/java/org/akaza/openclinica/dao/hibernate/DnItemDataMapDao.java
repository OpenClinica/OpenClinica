package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.DnItemDataMap;

import java.util.List;

public class DnItemDataMapDao extends AbstractDomainDao<DnItemDataMap> {

    @Override
    Class<DnItemDataMap> domainClass() {
        return DnItemDataMap.class;
    }

    public List<DnItemDataMap> findByItemData(Integer itemDataId) {
        String query = "from " + getDomainClassName() + " do where do.itemData.itemDataId = :itemdataid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("itemdataid", itemDataId);
        return (List<DnItemDataMap>) q.list();
    }
}

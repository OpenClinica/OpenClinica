package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.hibernate.query.Query;

public class VersioningMapDao extends AbstractDomainDao<VersioningMap> {

    @Override
    Class<VersioningMap> domainClass() {
        // TODO Auto-generated method stub
        return VersioningMap.class;
    }

    public ArrayList<VersioningMap> findByVersionIdAndItemId(int versionId, int itemId) {
        String query = "from " + getDomainClassName() + " vm  where vm.crfVersion.crfVersionId = :versionId and  vm.item.itemId = :itemId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("versionId", versionId);
        q.setParameter("itemId", itemId);
        return (ArrayList<VersioningMap>) q.list();
    }

}

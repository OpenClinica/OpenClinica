package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

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

    public List<VersioningMap> findByFormLayoutId(int formLayoutId) {
        String query = "from " + getDomainClassName() + " vm  where vm.formLayout.formLayoutId = :formLayoutId ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("formLayoutId", formLayoutId);
        return (List<VersioningMap>) q.list();
    }

    public ArrayList<VersioningMap> findByVersionIdFormLayoutIdAndItemId(int versionId, int formLayoutId, int itemId, int itemOrdinal) {
        String query = "from " + getDomainClassName()
                + " vm  where vm.crfVersion.crfVersionId = :versionId and vm.formLayout.formLayoutId = :formLayoutId and vm.item.itemId = :itemId and vm.itemInFormLayout = :itemOrdinal";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("versionId", versionId);
        q.setParameter("formLayoutId", formLayoutId);
        q.setParameter("itemId", itemId);
        q.setParameter("itemOrdinal", itemOrdinal);
        return (ArrayList<VersioningMap>) q.list();
    }

}

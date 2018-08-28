package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ArchivedDatasetFilePermissionTag;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfPermissionTag;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
public class ArchivedDatasetFilePermissionTagDao extends AbstractDomainDao<ArchivedDatasetFilePermissionTag> {

    @Override
    Class<ArchivedDatasetFilePermissionTag> domainClass() {
        // TODO Auto-generated method stub
        return ArchivedDatasetFilePermissionTag.class;
    }

    @Transactional
    public  ArchivedDatasetFilePermissionTag saveOrUpdate(ArchivedDatasetFilePermissionTag adf) {
        return super.saveOrUpdate(adf);
        }

    public List<ArchivedDatasetFilePermissionTag> findAllByArchivedDatasetFileId(int archivedDatasetFileId) {
        String query = "from " + getDomainClassName()
                + " do where do.archivedDatasetFileId = :archivedDatasetFileId";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        ((Query) q).setParameter("archivedDatasetFileId", archivedDatasetFileId);
        return (List<ArchivedDatasetFilePermissionTag>) q.list();
    }


    @Transactional
    public void delete(int adfId)
    {
        String query = " delete from " + getDomainClassName() + " do  where do.archivedDatasetFileId =:id ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", adfId);
        q.executeUpdate();
    }


}

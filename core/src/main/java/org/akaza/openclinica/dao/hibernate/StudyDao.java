package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
@Transactional
public class StudyDao extends AbstractDomainDao<Study> {
	
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    public boolean doesStudyExist(String uniqueId, String schemaName) {
        Query q = getCurrentSession().createNativeQuery("select count(*) from " + schemaName + ".study where unique_identifier='" + uniqueId + "'");
        BigInteger count = (BigInteger) q.getSingleResult();
        return (count.intValue() == 1) ? true:false;
    }
    public Study findByUniqueId(String uniqueId) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.uniqueIdentifier = :uniqueId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uniqueId", uniqueId);
        return  (Study) q.uniqueResult();
    }

    public Study findByOidEnvType(String oid, StudyEnvEnum envType) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.oc_oid = :oid and do.envType = :envType";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("oid", oid);
        q.setParameter("envType", envType);
        return  (Study) q.uniqueResult();
    }

    public Study findByStudyEnvUuid(String studyEnvUuid) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.studyEnvUuid = :studyEnvUuid "
                + "or do.studyEnvSiteUuid = :studyEnvUuid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyEnvUuid", studyEnvUuid);
        return  (Study) q.uniqueResult();
    }

    public Study findPublicStudy(String ocId) {
        String schema = CoreResources.getRequestSchema();
        if (StringUtils.isEmpty(schema))
            return null;
        CoreResources.setRequestSchema("public");
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.oc_oid = :ocId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("ocId", ocId);
        Study study =   (Study) q.uniqueResult();
        CoreResources.setRequestSchema(schema);
        return study;
    }
    public Study findPublicStudyById(int studyId) {
        String schema = CoreResources.getRequestSchema();
        if (StringUtils.isEmpty(schema))
            return null;
        CoreResources.setRequestSchema("public");
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.studyId = :studyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", studyId);
        Study study =   (Study) q.uniqueResult();
        CoreResources.setRequestSchema(schema);
        return study;
    }
    public Study updatePublicStudy(Study study) {
        String schema = CoreResources.getRequestSchema();
        if (StringUtils.isEmpty(schema))
            return null;
        CoreResources.setRequestSchema("public");
        getSessionFactory().getStatistics().logSummary();
        this.saveOrUpdate(study);
        CoreResources.setRequestSchema(schema);
        return study;
    }
}

package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Transactional
public class StudyDao extends AbstractDomainDao<Study> {

    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    public boolean doesStudyExist(String uniqueId, String schemaName) {
        Query q = getCurrentSession().createNativeQuery("select count(*) from " + schemaName + ".study where unique_identifier='" + uniqueId + "'");
        BigInteger count = (BigInteger) q.getSingleResult();
        return (count.intValue() == 1) ? true : false;
    }

    public Study findByUniqueId(String uniqueId) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.uniqueIdentifier = :uniqueId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uniqueId", uniqueId);
        return (Study) q.uniqueResult();
    }

    public Study findByNameAndParent(String name, Integer parentStudyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.name = :name and parent_study_id=:parentStudyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("name", name);
        q.setParameter("parentStudyId", parentStudyId);
        return (Study) q.uniqueResult();
    }

    public List<Study> findAllActiveSites(int parentStudyId) {
        String query = " from Study do  where parent_study_id=:parentStudyId and status_id=1";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("parentStudyId", parentStudyId);
        return q.list();
    }
    public List<ChangeStudyDTO> findByUser(String username) {
        getSessionFactory().getStatistics().logSummary();
        String query = " select s.study_id as \"studyId\", concat(s.study_env_uuid, ps.study_env_uuid) as \"studyEnvUuid\", s.study_env_site_uuid as \"siteEnvUuid\" \n" +
                "\t      from study s\n" +
                "\t      left join study ps on (s.parent_study_id = ps.study_id) where s.study_id in (select study_id from study_user_role where user_name=:username)\n" +
                "\t      or s.parent_study_id in (select study_id from study_user_role where user_name=:username);";
        Query q = getCurrentSession().createNativeQuery(query).setResultTransformer(Transformers.aliasToBean(ChangeStudyDTO.class));
        q.setParameter("username", username);
        return q.list();
    }

    public List<ChangeStudyDTO> findAllUsersByStudy(int studyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = " select u.user_name as \"username\", concat(s.study_env_uuid, ps.study_env_uuid) as \"studyEnvUuid\", s.study_env_site_uuid as \"siteEnvUuid\" \n" +
                "from user_account u, study s left join " +
                "(select study_id from study_user_role tmp where tmp.status_id = 1) sur on (s.study_id=sur.study_id) \n" +
                "left join study ps on (s.parent_study_id = ps.study_id) where s.study_id=:studyId and sur.user_name=u.user_name and u.status_id=1";
        Query q = getCurrentSession().createNativeQuery(query).setResultTransformer(Transformers.aliasToBean(ChangeStudyDTO.class));
        q.setParameter("studyId", studyId);
        return q.list();
    }


    public Study findByOidEnvType(String oid, StudyEnvEnum envType) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.oc_oid = :oid and do.envType = :envType";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("oid", oid);
        q.setParameter("envType", envType);
        return (Study) q.uniqueResult();
    }

    public Study findByStudyEnvUuid(String studyEnvUuid) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.studyEnvUuid = :studyEnvUuid " + "or do.studyEnvSiteUuid = :studyEnvUuid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyEnvUuid", studyEnvUuid);
        return (Study) q.uniqueResult();
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
        Study study = (Study) q.uniqueResult();
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
        Study study = (Study) q.uniqueResult();
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
    public List<String> findAllSchemas() {
        Query query = getCurrentSession().createNativeQuery("SELECT DISTINCT schema_name FROM public.study");
        List<String> result = (List<String>) query.getResultList();
        return result;
    }
}

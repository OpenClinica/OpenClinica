package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.bean.oid.OidGenerator;
import core.org.akaza.openclinica.bean.oid.StudyOidGenerator;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.core.DAODigester;
import core.org.akaza.openclinica.dao.core.SQLFactory;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Transactional
public class StudyDao extends AbstractDomainDao<Study> {

    private DAODigester digester;

    public StudyDao(){
        String digesterName=SQLFactory.getInstance().DAO_STUDY;
        digester= SQLFactory.getInstance().getDigester(digesterName);
    }
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    public Study update(Study study){
        study.setOc_oid(getValidOid(study));
        return saveOrUpdate(study);
    }

    public Study create(Study study){
        study.setOc_oid(getValidOid(study));
        save(study);
        return study;
    }
    private String getOid(Study study) {
        OidGenerator oidGenerator = new StudyOidGenerator();
        String oid;
        try {
            oid = study.getOc_oid() != null ? study.getOc_oid() : oidGenerator.generateOid(study.getUniqueIdentifier());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(Study study) {
        OidGenerator oidGenerator = new StudyOidGenerator();
        String oid = getOid(study);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
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

    public Study findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        String query=digester.getQuery("findSiteByUniqueIdentifier");
        Query q=getCurrentSession().createNativeQuery(query);
        q.setParameter("parentUniqueIdentifier",parentUniqueIdentifier);
        q.setParameter("unique_identifier",siteUniqueIdentifier);
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

    public Study findSiteByOid(String parentOid, String siteOid) {
        String query="Select site.* from "+ getDomainClassName() +" site, "+getDomainClassName()+" study where site.study.studyId = study.studyId and" +
                " study.oc_oid = :parentOid and site.oc_oid = :siteOid";
        Query q=getCurrentSession().createQuery(query);
        q.setParameter("parentOid",parentOid);
        q.setParameter("siteOid",siteOid);
        return (Study) q.uniqueResult();
    }

    public Study findStudyByOid(String studyOid) {
        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study =  cq.from(Study.class);
        Predicate predicate = cb.and(study.get("study.studyId").isNull(),
                                cb.equal(study.get("oc_oid"),studyOid));
        cq.select(study).where(predicate);
        return (Study) getCurrentSession().createQuery(cq).uniqueResult();
    }

    public Collection findAllByUser(String username) {

        Query q=getCurrentSession().createNativeQuery(digester.getQuery("findAllByUser")).setResultTransformer(Transformers.aliasToBean(Study.class));
        q.setParameter("userName",username);
        return q.list();
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

package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.oid.OidGenerator;
import core.org.akaza.openclinica.bean.oid.StudyOidGenerator;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.core.DAODigester;
import core.org.akaza.openclinica.dao.core.SQLFactory;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.StudyBuildServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
        if(study.getOc_oid() == null || StringUtils.isEmpty(study.getOc_oid()))
            study.setOc_oid(getValidOid(study));
        getCurrentSession().update(study);
        return study;
    }

    public Study create(Study study){
        study.setOc_oid(getValidOid(study));
        if(study.getDateCreated() == null)
            study.setDateCreated(new java.util.Date());
        getCurrentSession().save(study);
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

    @Transactional
    public boolean doesStudyExist(String uniqueId, String schemaName) {
        Query q = getCurrentSession().createNativeQuery("select count(*) from " + schemaName + ".study where unique_identifier='" + uniqueId + "'");
        BigInteger count = (BigInteger) q.getSingleResult();
        return (count.intValue() == 1) ? true : false;
    }
    @Transactional
    public Study findByUniqueId(String uniqueId) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.uniqueIdentifier = :uniqueId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uniqueId", uniqueId);
        return (Study) q.uniqueResult();
    }
    @Transactional
    public Study findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        String query=digester.getQuery("findSiteByUniqueIdentifier");
        Query q=getCurrentSession().createNativeQuery(query);
        q.setParameter("parentUniqueIdentifier",parentUniqueIdentifier);
        q.setParameter("unique_identifier",siteUniqueIdentifier);
        return (Study) q.uniqueResult();
    }
    @Transactional
    public Study findByNameAndParent(String name, Integer parentStudyId) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.name = :name and parent_study_id=:parentStudyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("name", name);
        q.setParameter("parentStudyId", parentStudyId);
        return (Study) q.uniqueResult();
    }
    @Transactional
    public List<Study> findAllActiveSites(int parentStudyId) {
        String query = " from Study do  where parent_study_id=:parentStudyId and status_id=1";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("parentStudyId", parentStudyId);
        return q.list();
    }
    @Transactional
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
    @Transactional
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

    @Transactional
    public Study findByOidEnvType(String oid, StudyEnvEnum envType) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.oc_oid = :oid and do.envType = :envType";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("oid", oid);
        q.setParameter("envType", envType);
        return (Study) q.uniqueResult();
    }
    @Transactional
    public Study findByStudyEnvUuid(String studyEnvUuid) {
        getSessionFactory().getStatistics().logSummary();
        String query = " from Study do  where do.studyEnvUuid = :studyEnvUuid " + "or do.studyEnvSiteUuid = :studyEnvUuid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyEnvUuid", studyEnvUuid);
        return (Study) q.uniqueResult();
    }
    @Transactional
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
    @Transactional
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
    @Transactional
    public Study findSiteByOid(String parentOid, String siteOid) {
        String query = "Select site.* from "+ getDomainClassName() +" site, "+getDomainClassName()+" study where site.study.studyId = study.studyId and" +
                " study.oc_oid = :parentOid and site.oc_oid = :siteOid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("parentOid", parentOid);
        q.setParameter("siteOid", siteOid);
        return (Study) q.uniqueResult();
    }
    @Transactional
    public Study findStudyByOid(String studyOid) {
        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study =  cq.from(Study.class);
        ParameterExpression<String> pStudyOid = cb.parameter(String.class);
        Predicate predicate = cb.and(study.get("study").get("studyId").isNull(),
                                cb.equal(study.get("oc_oid"),pStudyOid));
        cq.select(study).where(predicate);
        TypedQuery<Study> q = getCurrentSession().createQuery(cq);
        q.setParameter(pStudyOid, studyOid);
        return (Study) q.getSingleResult();
    }

    @Transactional
    public Study findByOid(String studyOid) {
        String query = "from Study do where do.oc_oid = :studyOid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyOid", studyOid);
        Study study = (Study) q.uniqueResult();
        return study;
    }

    @Transactional
    public Collection findAllByUser(String username) {
        Query q = getCurrentSession().createNativeQuery(digester.getQuery("findAllByUser"));
        q.setParameter("userName", username);
        return (List<Study>) q.getResultList();
    }

    @Transactional
    public List<Integer> getStudyIdsByCRF(int crfId) {
        String query = "select distinct eventDefCrf.study.studyId from EventDefinitionCrf eventDefCrf where eventDefCrf.crf.crfId=:crfId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("crfId", crfId);
        return (List<Integer>) q.getResultList();
    }
    @Transactional
    public Collection findAllByUserNotRemoved(String username) {
        String query = "SELECT s.* FROM "+ getDomainClassName() +" s, StudyUserRole sur WHERE sur.id.userName = :userName"+
        " AND s.studyId=sur.id.studyId AND sur.statusId != 5 AND sur.statusId != 7";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("userName", username);
        return (List<Study>) q.getResultList();
    }
    @Transactional
    public List findAllByStatus(Status status) {
        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study =  cq.from(Study.class);
        ParameterExpression<Status> pStatus = cb.parameter(Status.class);
        cq.select(study).where(cb.equal(study.get("status"), pStatus));
         TypedQuery<Study> query = getCurrentSession().createQuery(cq);
         query.setParameter(pStatus,status);
         return (List<Study>) query.getResultList();
    }
    @Transactional
    public Collection findAllByLimit(boolean isLimited) {
        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study = cq.from(Study.class);
        cq.orderBy(cb.asc(study.get("name")));
        List<Study> studyList = null;
        if(isLimited)
             studyList = (List<Study>) getCurrentSession().createQuery(cq).setMaxResults(5).getResultList();
        else
            studyList = (List<Study>) getCurrentSession().createQuery(cq).getResultList();
        return studyList;
    }
    @Transactional
    public ArrayList<Study> findAll() {
        return (ArrayList<Study>) findAllByLimit(false);
    }

    @Transactional
    public Collection findAllParents() {
        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study = cq.from(Study.class);
        cq.where(study.get("study").isNull()).orderBy(cb.asc(study.get("name")));
        return (List<Study>) getCurrentSession().createQuery(cq).getResultList();
    }

    /**
     * isAParent(), finds out whether or not a study is a parent.
     *
     * @return a boolean
     */
    @Transactional
    public boolean isAParent(int studyId) {
        boolean ret = false;
        Collection col = findAllByParent(studyId);
        if (col != null && col.size() > 0) {
            ret = true;
        }
        return ret;
    }
    @Transactional
    public Collection findAllByParent(int parentStudyId) {
        return findAllByParentAndLimit(parentStudyId, false);
    }
    @Transactional
    public Collection findAllByParentAndLimit(int parentStudyId, boolean isLimited) {

        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study = cq.from(Study.class);
        ParameterExpression<Integer> pParentStudyId = cb.parameter(Integer.class);
        cq.where(cb.equal(study.get("study").get("studyId"), pParentStudyId));
        TypedQuery<Study> query = getCurrentSession().createQuery(cq);
        query.setParameter(pParentStudyId, parentStudyId);
        if(isLimited)
            query.setMaxResults(5);
        return (List<Study>) query.getResultList();
    }
    @Transactional
    public Collection findAll(int studyId) {
       CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
       CriteriaQuery<Study> cq = cb.createQuery(Study.class);
       Root<Study> study = cq.from(Study.class);
       ParameterExpression<Integer> pStudyId = cb.parameter(Integer.class);
       Predicate predicate = cb.and(cb.or(cb.equal(study.get("studyId"), pStudyId),cb.equal(study.get("study").get("studyId"), pStudyId)),
               study.get("study").isNotNull());
       cq.where(predicate).orderBy(cb.asc(study.get("name")));
       TypedQuery<Study> query = getCurrentSession().createQuery(cq);
       query.setParameter(pStudyId, studyId);
       return (List<Study>) query.getResultList();
    }
    @Transactional
    public Study findByPK(int ID) {
        String query = "select do from Study do where do.studyId = :studyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", ID);
        Study study = (Study) q.uniqueResult();
        return study;
    }

    @Transactional
    public Study findByName(String name){
        CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Study> cq = cb.createQuery(Study.class);
        Root<Study> study = cq.from(Study.class);
        ParameterExpression<String> pName = cb.parameter(String.class);
        cq.where(cb.equal(study.get("name"), pName));
        TypedQuery query = getCurrentSession().createQuery(cq);
        query.setParameter(pName, name);
        List<Study> list=query.getResultList();
        if(list != null && list.size() > 0)
            return list.get(0);
        return null;
    }
    @Transactional
    public void deleteTestOnly(String name) {
        String query = "delete from "+ getDomainClassName() +" s where s.name=:name";
        Query q = getCurrentSession().createQuery(query);
        q.executeUpdate();
    }

    /**
     * Only for use by getChildrenByParentIds
     *
     * @param answer
     * @param parentId
     * @param child
     * @return
     */
    @Transactional
    private HashMap addChildToParent(HashMap answer, int parentId, Study child) {
        Integer key = new Integer(parentId);
        ArrayList children = (ArrayList) answer.get(key);

        if (children == null) {
            children = new ArrayList();
        }

        children.add(child);
        answer.put(key, children);

        return answer;
    }

    /**
     * @param allStudies
     *            The result of findAll().
     * @return A HashMap where the keys are Integers whose intValue are studyIds
     *         and the values are ArrayLists; each element of the ArrayList is a
     *         Study representing a child of the study whose id is the key
     *         <p>
     *         e.g., if A has children B and C, then this will return a HashMap
     *         h where h.get(A.getId()) returns an ArrayList whose elements are
     *         B and C
     */
    @Transactional
    public HashMap getChildrenByParentIds(ArrayList allStudies) {
        HashMap answer = new HashMap();

        if (allStudies == null) {
            return answer;
        }

        for (int i = 0; i < allStudies.size(); i++) {
            Study study = (Study) allStudies.get(i);

            int parentStudyId = study.checkAndGetParentStudyId();
            if (parentStudyId > 0) { // study is a child
                answer = addChildToParent(answer, parentStudyId, study);
            }
        }

        return answer;
    }
    @Transactional
    public Collection<Integer> findAllSiteIdsByStudy(Study study) {
        String query = "Select s.studyId from "+ getDomainClassName() +" s WHERE s.studyId=:studyId or s.study.studyId = :parentStudyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("parentStudyId", study.getStudyId());
        q.setParameter("studyId", study.getStudyId());
        List<Integer> list = q.getResultList();
        if(list != null && list.size() > 0)
            return list;
        return null;
    }
    @Transactional
    public Collection<Integer> findOlnySiteIdsByStudy(Study s) {
        String query = "SELECT s.studyId from "+ getDomainClassName() +" s where s.study.studyId = :parentStudyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("parentStudyId", s.getStudyId());
        List<Integer> list = q.getResultList();
        if(list != null && list.size() > 0)
            return list;
        return null;
    }
    @Transactional
    public void updateSitesStatus(Study parent) {
        String query = "from "+ getDomainClassName() +" s WHERE s.study.studyId = :parentStudyId";
        Query q=getCurrentSession().createQuery(query);
        q.setParameter("parentStudyId", parent.getStudyId());
        List<Study> studyList=(List<Study>) q.getResultList();
        for(Study s: studyList){
            s.setStatus(parent.getStatus());
            s.setOldStatusId(parent.getOldStatusId());
            getCurrentSession().update(s);
        }
    }

    @Transactional
    public Study updateStudyStatus(Study s) {
        String query = "from "+ getDomainClassName() +" s WHERE s.studyId = :studyId";
        Query q=getCurrentSession().createQuery(query);
        q.setParameter("studyId", s.getStudyId());
        Study s1=(Study) q.uniqueResult();
        s1.setStatus(s.getStatus());
        s1.setOldStatusId(s.getOldStatusId());
        getCurrentSession().update(s1);
        return s1;
    }
    @Transactional
    public Study findByStudySubjectId(int studySubjectId) {
        String query="select s from StudySubject ss, "+ getDomainClassName() +" s where ss.studySubjectId = :studySubjectId and ss.study.studyId = s.studyId";
        Query q=getCurrentSession().createQuery(query);
        q.setParameter("studySubjectId", studySubjectId);
        return (Study) q.uniqueResult();
    }

    @Transactional
    public Collection findAllByParentStudyIdOrderedByIdAsc(int parentStudyId) {
        /* This function will load the studyParamValues without lazy initialization*/
        String query = "select distinct s from Study s left join fetch s.studyParameterValues where ( s.studyId=:studyId ) or ( s.study.studyId=:parentStudyId ) order by s.studyId asc";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", parentStudyId);
        q.setParameter("parentStudyId", parentStudyId);
        List<Study> studyList = q.getResultList();
        for(Study study : studyList){
            if(study != null && study.isSite())
            {
                Study parentStudy = this.findStudyWithSPVByStudyId(study.getStudy().getStudyId());
                study.setStudy(parentStudy);
            }
        }
        return studyList;
    }
    @Transactional
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
    @Transactional
    public List<String> findAllSchemas() {
        Query query = getCurrentSession().createNativeQuery("SELECT DISTINCT schema_name FROM public.study");
        List<String> result = (List<String>) query.getResultList();
        return result;
    }
    public Study getPublicStudy(int id) {
        HttpServletRequest request = StudyBuildServiceImpl.getRequest();
        String schema = null;
        if (request == null) {
            schema = CoreResources.getRequestSchema();
            CoreResources.setRequestSchema("public");
        } else {
                schema = (String) request.getAttribute("requestSchema");
                request.setAttribute("requestSchema", "public");
        }
        Study study = (Study) findByPK(id);
        if (org.apache.commons.lang.StringUtils.isNotEmpty(schema) && request != null)
            request.setAttribute("requestSchema", schema);
        else if(org.apache.commons.lang.StringUtils.isNotEmpty(schema))
            CoreResources.setRequestSchema(schema);
        return study;
    }
    public Study findStudyWithSPVByStudyId(int studyId)
    {
        /* This function will load the studyParamValues without lazy initialization*/
        String query = "select distinct s from Study s left join fetch s.studyParameterValues where ( s.studyId=:studyId ) ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", studyId);
        Study study = (Study) q.uniqueResult();
        if(study != null && study.isSite()) {
            Study parentStudy = this.findStudyWithSPVByStudyId(study.getStudy().getStudyId());
            study.setStudy(parentStudy);
        }
        return study;
    }

    @Transactional
    public Study findStudyWithSPVByUniqueId(String uniqueId) {
        /* This function will load the studyParamValues without lazy initialization*/
        getSessionFactory().getStatistics().logSummary();
        String query = " select distinct s from Study s left join fetch s.studyParameterValues where s.uniqueIdentifier = :uniqueId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uniqueId", uniqueId);
        Study study = (Study) q.uniqueResult();
        if(study != null && study.isSite())
        {
            Study parentStudy = this.findStudyWithSPVByStudyId(study.getStudy().getStudyId());
            study.setStudy(parentStudy);
        }
        return study;
    }

    @Transactional
    public Study findStudyWithSPVByStudyEnvUuid(String studyEnvUuid) {
        /* This function will load the studyParamValues without lazy initialization*/
        getSessionFactory().getStatistics().logSummary();
        String query = " select distinct s from Study s left join fetch s.studyParameterValues where s.studyEnvUuid = :studyEnvUuid " + "or s.studyEnvSiteUuid = :studyEnvUuid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyEnvUuid", studyEnvUuid);
        Study study = (Study) q.uniqueResult();
        if(study != null && study.isSite())
        {
            Study parentStudy = this.findStudyWithSPVByStudyId(study.getStudy().getStudyId());
            study.setStudy(parentStudy);
        }
        return study;
    }

    public Study findStudyWithSPVByOcOID(String OCOID) {
        /* This function will load the studyParamValues without lazy initialization*/
        getSessionFactory().getStatistics().logSummary();
        String query = "select distinct s from " + getDomainClassName() + " s left join fetch s.studyParameterValues where s.oc_oid = :oc_oid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("oc_oid", OCOID);
        Study study = (Study) q.uniqueResult();
        if(study != null && study.isSite())
        {
            Study parentStudy = this.findStudyWithSPVByStudyId(study.getStudy().getStudyId());
            study.setStudy(parentStudy);
        }
        return study;
    }
}

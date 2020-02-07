package core.org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import core.org.akaza.openclinica.bean.oid.OidGenerator;
import core.org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

    @Override
    Class<StudySubject> domainClass() {
        // TODO Auto-generated method stub
        return StudySubject.class;
    }

    /**
     * findAllByStudyWithAvailableAndSignedStatusOnly(Integer studyId,int pageNumber, int pageSize), finds all the studySubjects which are available or signed.
     *
     * @return a list of studySubjects
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public List<StudySubject> findAllByStudyWithAvailableAndSignedStatusOnly(Integer studyId,int pageNumber, int pageSize) {
        String query = "from " + getDomainClassName() + " do where do.study.studyId = :studyid and (status_id = :statusId or status_id = :statusId1) order by do.dateCreated ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("statusId",1);
        q.setParameter("statusId1", 8);
        q.setParameter("studyid", studyId);
        q.setFirstResult  ((pageNumber-1)*pageSize);
        q.setMaxResults(pageSize);
        return (List<StudySubject>) q.list();

    }

    public List<StudySubject> findAllByStudy(Study study){
        String query = "from " + getDomainClassName() + " do where do.study.studyId = :studyid or do.study.study.studyId=:parentStudyId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("parentStudyId", study.getStudyId());
        return (List<StudySubject>) q.list();
    }

    public StudySubject findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (StudySubject) q.uniqueResult();
    }

    public StudySubject findByLabelAndStudy(String embeddedStudySubjectId, Study study) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.studyId = :studyid and do.label = :label";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyid", study.getStudyId());
        q.setString("label", embeddedStudySubjectId);
        return (StudySubject) q.uniqueResult();
    }

    public ArrayList<StudySubject> findByParticipantIdFirstNameLastNameIdentifier(Study study, String participantId, String firstNameForSearchUse, String lastNameForSearchUse, String identifierForSearchUse) {

        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do where  do.study.studyId = :studyid ";

        if (participantId != null)
            query = query + " and lower(do.label) like :participantId ";
        if (firstNameForSearchUse != null)
            query = query + " and do.studySubjectDetail.firstNameForSearchUse = :firstNameForSearchUse ";

        if (lastNameForSearchUse != null)
            query = query + " and do.studySubjectDetail.lastNameForSearchUse = :lastNameForSearchUse ";

        if (identifierForSearchUse != null)
            query = query + " and do.studySubjectDetail.identifierForSearchUse = :identifierForSearchUse";

        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyid", study.getStudyId());

        if (participantId != null)
            q.setString("participantId","%" + participantId.toLowerCase()+ "%");

        if (firstNameForSearchUse != null)
            q.setString("firstNameForSearchUse", firstNameForSearchUse);

        if (lastNameForSearchUse != null)
            q.setString("lastNameForSearchUse", lastNameForSearchUse);

        if (identifierForSearchUse != null)
            q.setString("identifierForSearchUse", identifierForSearchUse);

        return (ArrayList<StudySubject>) q.list();
    }

    public  ArrayList<StudySubject> findByIdentifier(String studyOid, String identifier) {
        //TODO: looks like auto-encryption is not happening need to look into it.
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " " +
                "do  where do.study.oc_oid = :studyOid and do.studySubjectDetail.identifier = :identifier";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("studyOid", studyOid);
        q.setString("identifier" , identifier);
        return (ArrayList<StudySubject>) q.list();
    }


    public  ArrayList<StudySubject> findAllParticipateParticipants() {
        //TODO: looks like auto-encryption is not happening need to look into it.
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " " +
                "do  where do.userId != null" ;
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (ArrayList<StudySubject>) q.list();
    }

    @Transactional
    public StudySubject findByLabelAndStudyOrParentStudy(String embeddedStudySubjectId, Study study) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where (do.study.studyId = :studyid or do.study.study.studyId = :studyid) and do.label = :label";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyid", study.getStudyId());
        q.setString("label", embeddedStudySubjectId);
        return (StudySubject) q.uniqueResult();
    }

    @Transactional
    public ArrayList<StudySubject> findByLabelAndParentStudy(String embeddedStudySubjectId, Study parentStudy) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.study.studyId = :studyid and do.label = :label";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyid", parentStudy.getStudyId());
        q.setString("label", embeddedStudySubjectId);
        return (ArrayList<StudySubject>) q.list();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("id", id.toString());

        return (ArrayList<StudyEvent>) q.list();

    }
    public String getValidOid(StudySubject studySubject, ArrayList<String> oidList) {
        OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid = getOid(studySubject);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(StudySubject studySubject) {
        OidGenerator oidGenerator = new StudySubjectOidGenerator();
        String oid;
        try {
            oid = studySubject.getOcOid() != null ? studySubject.getOcOid() : oidGenerator.generateOid(studySubject.getLabel());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }
    public int findTheGreatestLabel() {
        List<StudySubject> allStudySubjects = super.findAll();

        int greatestLabel = 0;
        for (StudySubject subject:allStudySubjects) {
            int labelInt = 0;
            try {
                labelInt = Integer.parseInt(subject.getLabel());
            } catch (NumberFormatException ne) {
                labelInt = 0;
            }
            if (labelInt > greatestLabel) {
                greatestLabel = labelInt;
            }
        }
        return greatestLabel;
    }

}

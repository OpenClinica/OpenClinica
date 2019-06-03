package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

    @Override
    Class<StudySubject> domainClass() {
        // TODO Auto-generated method stub
        return StudySubject.class;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<StudySubject> findAllByStudy(Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.study.studyId = :studyid";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", studyId);
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
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
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

        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", study.getStudyId());

        if (participantId != null)
            q.setParameter("participantId","%" + participantId.toLowerCase()+ "%");

        if (firstNameForSearchUse != null)
            q.setParameter("firstNameForSearchUse", firstNameForSearchUse);

        if (lastNameForSearchUse != null)
            q.setParameter("lastNameForSearchUse", lastNameForSearchUse);

        if (identifierForSearchUse != null)
            q.setParameter("identifierForSearchUse", identifierForSearchUse);

        return (ArrayList<StudySubject>) q.list();
    }

    public  ArrayList<StudySubject> findByIdentifier(String studyOid, String identifier) {
        //TODO: looks like auto-encryption is not happening need to look into it.
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " " +
                "do  where do.study.oc_oid = :studyOid and do.studySubjectDetail.identifier = :identifier";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyOid", studyOid);
        q.setParameter("identifier" , identifier);
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
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", study.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (StudySubject) q.uniqueResult();
    }

    public ArrayList<StudySubject> findByLabelAndParentStudy(String embeddedStudySubjectId, Study parentStudy) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.study.study.studyId = :studyid and do.label = :label";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyid", parentStudy.getStudyId());
        q.setParameter("label", embeddedStudySubjectId);
        return (ArrayList<StudySubject>) q.list();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", id.toString());

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

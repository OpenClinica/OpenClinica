package core.org.akaza.openclinica.dao.hibernate;

import java.util.Date;
import java.util.List;

import core.org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.springframework.transaction.annotation.Transactional;

public class EventCrfDao extends AbstractDomainDao<EventCrf> {

    @Override
    Class<EventCrf> domainClass() {
        return EventCrf.class;
    }

    public List<EventCrf> findNonArchivedByStudyEventId(int study_event_id) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid " +
                "and (event_crf.removed = null or event_crf.removed !=:removed) and (event_crf.archived = null or event_crf.archived !=:archived) ";

        org.hibernate.query.Query hibernateQuery = getCurrentSession().createQuery(query);
        hibernateQuery.setParameter("studyeventid", study_event_id);
        hibernateQuery.setParameter("removed", true);
        hibernateQuery.setParameter("archived", true);

        return hibernateQuery.list();
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfVersionId(int study_event_id, int study_subject_id, int crf_version_id) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crfVersionId = :crfversionid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("crfversionid", crf_version_id);
        return (EventCrf) q.uniqueResult();
    }
    public EventCrf findByStudyEventOIdStudySubjectOIdCrfOId(String studyEventOID, String studySubjectLabel, String formOID, int ordinal) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crf.ocOid = :formOID and event_crf.studyEvent.studyEventDefinition.oc_oid = :studyEventOID and event_crf.studySubject.label = :studySubjectLabel and event_crf.studyEvent.sampleOrdinal = :ordinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyEventOID", studyEventOID);
        q.setParameter("studySubjectLabel", studySubjectLabel);
        q.setParameter("formOID", formOID);
        q.setParameter("ordinal", ordinal);
        return (EventCrf) q.uniqueResult();
    }

    public EventCrf findByStudyEventIdStudySubjectIdFormLayoutId(int study_event_id, int study_subject_id, int formLayoutId) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.formLayout.formLayoutId = :formLayoutid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("formLayoutid", formLayoutId);
        return (EventCrf) q.uniqueResult();
    }

    public List<EventCrf> findByStudyEventIdStudySubjectIdCrfId(int study_event_id, int study_subject_id, int crf_id) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.formLayout.crf.crfId = :crfid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("crfid", crf_id);
        return (List<EventCrf>) q.list();
    }

    @SuppressWarnings("unchecked")
    public List<EventCrf> findByStudyEventIdStudySubjectId(Integer studyEventId, String studySubjectOid) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.ocOid= :studysubjectoid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", studyEventId);
        q.setString("studysubjectoid", studySubjectOid);
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<EventCrf> findByStudyEventStatus(Integer studyEventId, Integer statusCode) {
        String query = "from " + getDomainClassName() + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.statusId = :statusid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", studyEventId);
        q.setInteger("statusid", statusCode);
        return q.list();
    }

    @Transactional
    public void updateSdvStatus(SdvStatus sdvStatus, int userId, int eventCRFId){
        EventCrf eventCrf = findByPK(eventCRFId);
        eventCrf.setSdvStatus(sdvStatus);
        eventCrf.setUpdateId(userId);
        if(sdvStatus.equals(SdvStatus.VERIFIED))
            eventCrf.setLastSdvVerifiedDate(new Date());
        getCurrentSession().update(eventCrf);
    }
}

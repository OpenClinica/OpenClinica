package core.org.akaza.openclinica.dao.hibernate;

import java.util.List;

import core.org.akaza.openclinica.domain.datamap.EventCrf;

public class EventCrfDao extends AbstractDomainDao<EventCrf> {

    @Override
    Class<EventCrf> domainClass() {
        return EventCrf.class;
    }

    public List<EventCrf> findNonArchivedByStudyEventId(int study_event_id) {
        String query = "from " + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.statusId not in (5,7)";
        org.hibernate.query.Query hibernateQuery = getCurrentSession().createQuery(query);
        hibernateQuery.setParameter("studyeventid", study_event_id);

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

}

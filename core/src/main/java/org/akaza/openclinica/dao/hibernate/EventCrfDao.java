package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.EventCrf;

public class EventCrfDao extends AbstractDomainDao<EventCrf> {

    @Override
    Class<EventCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventCrf.class;
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfVersionId(int study_event_id, int study_subject_id, int crf_version_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crfVersionId = :crfversionid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("crfversionid", crf_version_id);
        return (EventCrf) q.uniqueResult();
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfId(int study_event_id, int study_subject_id, int crf_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crf.crfId = :crfid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", study_event_id);
        q.setInteger("studysubjectid", study_subject_id);
        q.setInteger("crfid", crf_id);
        return (EventCrf) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
	public List<EventCrf> findByStudyEventIdStudySubjectId(Integer studyEventId, String studySubjectOid) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.ocOid= :studysubjectoid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", studyEventId);
        q.setString("studysubjectoid", studySubjectOid);
        return (List<EventCrf>) q.list();
	}
    
    @SuppressWarnings("unchecked")
    public List<EventCrf> findByStudyEventStatus(Integer studyEventId, Integer statusCode) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.statusId = :statusid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventid", studyEventId);
        q.setInteger("statusid", statusCode);
        return (List<EventCrf>) q.list();
    }
}

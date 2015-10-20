package org.akaza.openclinica.dao.hibernate;

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

}

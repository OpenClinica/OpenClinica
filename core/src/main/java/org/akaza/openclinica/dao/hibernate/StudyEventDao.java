package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.hibernate.query.Query;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;

public class StudyEventDao extends AbstractDomainDao<StudyEvent> implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;
    private StudyEventChangeDetails changeDetails;
    private static String findByStudyEventIdQuery = "select se from StudyEvent se " + "join fetch se.studyEventDefinition as sed "
            + "join fetch se.studySubject as ss " + "where se.studyEventId = :studyEventId ";

    public Class<StudyEvent> domainClass() {
        return StudyEvent.class;
    }

    @Transactional
    public StudyEvent findByStudyEventId(int studyEventId) {
        Query q = getCurrentSession().createQuery(findByStudyEventIdQuery);
        q.setParameter("studyEventId", studyEventId);
        return (StudyEvent) q.uniqueResult();
    }

    public StudyEvent fetchByStudyEventDefOID(String oid, Integer studySubjectId) {
        String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);

        StudyEvent se = (StudyEvent) q.uniqueResult();
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
        return se;

    }

    @Transactional
    public StudyEvent fetchByStudyEventDefOIDAndOrdinal(String oid, Integer ordinal, Integer studySubjectId) {
        String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid and se.sampleOrdinal = :ordinal order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);
        q.setInteger("ordinal", ordinal);
        StudyEvent se = (StudyEvent) q.uniqueResult();
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
        return se;
    }

    @Transactional
    public StudyEvent fetchByStudyEventDefOIDAndOrdinalTransactional(String oid, Integer ordinal, Integer studySubjectId) {
        String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid and se.sampleOrdinal = :ordinal order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);
        q.setInteger("ordinal", ordinal);
        StudyEvent se = (StudyEvent) q.uniqueResult();
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
        return se;
    }

    public Integer findMaxOrdinalByStudySubjectStudyEventDefinition(int studySubjectId, int studyEventDefinitionId) {
        String query = "select max(sample_ordinal) from study_event where study_subject_id = " + studySubjectId + " and study_event_definition_id = "
                + studyEventDefinitionId;
        Query q = getCurrentSession().createSQLQuery(query);
        Number result = (Number) q.uniqueResult();
        if (result == null)
            return 0;
        else
            return result.intValue();
    }

    public List<StudyEvent> fetchListByStudyEventDefOID(String oid, Integer studySubjectId) {
        List<StudyEvent> eventList = null;

        String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);

        eventList = (List<StudyEvent>) q.list();
        return eventList;

    }

    @Transactional
    public StudyEvent saveOrUpdate(StudyEventContainer container) {
        StudyEvent event = saveOrUpdate(container.getEvent());
        this.eventPublisher.publishEvent(new OnStudyEventUpdated(container));
        return event;
    }

    public StudyEvent saveOrUpdateTransactional(StudyEventContainer container) {
        StudyEvent event = saveOrUpdate(container.getEvent());
        this.eventPublisher.publishEvent(new OnStudyEventUpdated(container));
        return event;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setChangeDetails(StudyEventChangeDetails changeDetails) {
        this.changeDetails = changeDetails;
    }

}

package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.ocobserver.OnStudyEventUpdated;
import core.org.akaza.openclinica.ocobserver.StudyEventChangeDetails;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import org.hibernate.query.Query;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

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

    @Transactional
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

    public List<StudyEvent> fetchStudyEvents(String oid, String subjectOID) {
        List<StudyEvent> eventList = null;

        String query = " from StudyEvent se where se.studySubject.ocOid = :subjectOID and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("subjectOID", subjectOID);
        q.setString("oid", oid);

        eventList = (List<StudyEvent>) q.list();
        return eventList;

    }

    public List<StudyEvent> fetchStudyEvents(int studyEventOrdinal, String oid, String subjectOID) {
        List<StudyEvent> eventList = null;

        String query = " from StudyEvent se where se.studySubject.ocOid = :subjectOID and se.studyEventDefinition.oc_oid = :oid and se.sampleOrdinal = :seOrdinal order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("subjectOID", subjectOID);
        q.setString("oid", oid);
        q.setInteger("seOrdinal", studyEventOrdinal);

        eventList = (List<StudyEvent>) q.list();
        return eventList;

    }

    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("id", id.toString());

        return (ArrayList<StudyEvent>) q.list();

    }

    public ArrayList<StudyEvent> fetchNonArchivedListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id " +
                "and (se.removed = null or se.removed !=:removed) and (se.archived = null or se.archived !=:archived) " +
        "order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("id", id.toString());
        q.setParameter("removed", true);
        q.setParameter("archived", true);




        return (ArrayList<StudyEvent>) q.list();

    }

    @Transactional
    public List<StudyEvent> fetchListByStudyEventDefOIDTransactional(String oid, Integer studySubjectId) {
        List<StudyEvent> eventList = null;

        String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);

        eventList = (List<StudyEvent>) q.list();
        return eventList;

    }


    public List<RandomizeQueryResult> fetchItemData(List<String> eventOids, String studySubjectOid, List<String> formOids,
                                                    List<String>itemGroups, List<String> itemOids) {

        Query query = getCurrentSession().createQuery("select new core.org.akaza.openclinica.dao.hibernate.RandomizeQueryResult(s, c, ig, i) " +
                "from StudyEvent s join s.eventCrfs c join c.formLayout.crf.itemGroups ig join c.itemDatas i " +
                "join ig.itemGroupMetadatas igm " +
                "where s.studyEventDefinition.oc_oid in :eventOids " +
                "and c.studySubject.ocOid = :studySubjectOid " +
                "and c.formLayout.crf.ocOid in :formOids " +
                "and ig.ocOid in :itemGroups " +
                "and i.item.ocOid in :itemOids " +
                "and igm.item.itemId = i.item.itemId " +
                "and i.ordinal=1 " +
                "and i.value is not null " +
                "and i.value <> ''");

        query.setParameter("eventOids", eventOids);
        query.setParameter("studySubjectOid", studySubjectOid);
        query.setParameter("itemOids", itemOids);
        query.setParameter("formOids", formOids);
        query.setParameter("itemGroups", itemGroups);

        List<RandomizeQueryResult> resultList = query.getResultList();
        logger.debug("Item data result size: {}", resultList.size());
        return resultList;
    }

    @Transactional
    public int fetchCountOfInitiatedSEs(String oid, String subjectOID) {
        List<StudyEvent> eventList = null;

        String queryString = "SELECT count(*) from StudyEvent se where se.studySubject.ocOid = :subjectOID and se.studyEventDefinition.oc_oid = :oid";
        Query query = getCurrentSession().createQuery(queryString);
        query.setParameter("subjectOID", subjectOID);
        query.setParameter("oid", oid);

        int result = toIntExact((long) query.getSingleResult());

        return result;
    }

    @Transactional
    public StudyEvent saveOrUpdate(StudyEventContainer container) {
        StudyEvent event = super.saveOrUpdate(container.getEvent());
        this.eventPublisher.publishEvent(new OnStudyEventUpdated(container));
        return event;
    }

    public StudyEvent saveOrUpdateTransactional(StudyEventContainer container) {
        StudyEvent event = super.saveOrUpdate(container.getEvent());
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

    @Transactional
    public StudyEvent saveOrUpdate(StudyEvent studyEvent){
        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails();
        changeDetails.setStartDateChanged(true);
        changeDetails.setStatusChanged(true);
        StudyEventContainer studyEventContainer = new StudyEventContainer(studyEvent,changeDetails);
        return saveOrUpdate(studyEventContainer);
    }
}

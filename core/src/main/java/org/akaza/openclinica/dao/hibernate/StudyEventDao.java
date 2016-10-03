package org.akaza.openclinica.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.hibernate.Query;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class StudyEventDao extends AbstractDomainDao<StudyEvent> implements ApplicationEventPublisherAware{

	private ApplicationEventPublisher eventPublisher;
	private StudyEventChangeDetails changeDetails;

	public Class<StudyEvent> domainClass(){
		return StudyEvent.class;
	}
	public StudyEvent fetchByStudyEventDefOID(String oid,Integer studySubjectId){
		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 org.hibernate.Query q = getCurrentSession().createQuery(query);
         q.setInteger("studySubjectId", studySubjectId);
         q.setString("oid", oid);

         StudyEvent se = (StudyEvent) q.uniqueResult();
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
         return se;


	}
	public StudyEvent fetchByStudyEventDefOIDAndOrdinal(String oid,Integer ordinal,Integer studySubjectId){
		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid and se.sampleOrdinal = :ordinal order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 org.hibernate.Query q = getCurrentSession().createQuery(query);
         q.setInteger("studySubjectId", studySubjectId);
         q.setString("oid", oid);
         q.setInteger("ordinal", ordinal);
         StudyEvent se = (StudyEvent) q.uniqueResult();
        // this.eventPublisher.publishEvent(new OnStudyEventUpdated(se));
         return se;
	}

    @Transactional(propagation = Propagation.NEVER)
    public StudyEvent fetchByStudyEventDefOIDAndOrdinalTransactional(String oid,Integer ordinal,Integer studySubjectId){
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
        String query = "select max(sample_ordinal) from study_event where study_subject_id = " + studySubjectId + " and study_event_definition_id = " + studyEventDefinitionId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);
        Number result = (Number) q.uniqueResult();
        if (result == null) return 0;
        else return result.intValue();
    }



	public List<StudyEvent> fetchListByStudyEventDefOID(String oid,Integer studySubjectId){
		List<StudyEvent> eventList = null;

		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);

        eventList = (List<StudyEvent>) q.list();
        return eventList;

	}

	public List<StudyEvent> fetchListOpenEventsByStudySubjectAndDate(
		int studySubjectId,
		Date dateStart){

		String hql = 
			"from StudyEvent se "
				+ "where se.studySubject.studySubjectId = :studySubjectId "
				+ "and se.dateStart <= :dateStart";
		Query query =
			getCurrentSession().
			createQuery(
				hql);
		query.setInteger(
			"studySubjectId",
			studySubjectId);
		query.setDate(
			"dateStart",
			dateStart);

        	return (List<StudyEvent>) query.list();
	}


	@Transactional(propagation = Propagation.NEVER)
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
	 public StudyEvent saveOrUpdate(StudyEvent domainObject) {
	 super.saveOrUpdate(domainObject);
	        getCurrentSession().flush();
	        return domainObject;
	    }

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
 this.eventPublisher = applicationEventPublisher;
	}

	public void setChangeDetails(StudyEventChangeDetails changeDetails) {
		this.changeDetails = changeDetails;
	}

}

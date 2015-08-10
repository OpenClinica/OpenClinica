package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Transactional(propagation = Propagation.NEVER)
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
	
	public List<StudyEvent> fetchListByStudyEventDefOID(String oid,Integer studySubjectId){
		List<StudyEvent> eventList = null;
		
		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studySubjectId", studySubjectId);
        q.setString("oid", oid);
        
        eventList = (List<StudyEvent>) q.list();
        return eventList;
      
	}
	
	public StudyEvent saveOrUpdate(StudyEventContainer container) {
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

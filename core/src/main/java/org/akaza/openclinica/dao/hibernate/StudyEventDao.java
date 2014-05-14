package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;

@Transactional    
public class StudyEventDao extends AbstractDomainDao<StudyEvent> implements ApplicationEventPublisherAware{

	private ApplicationEventPublisher eventPublisher;

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
@Override
	 public StudyEvent saveOrUpdate(StudyEvent domainObject) {
	 super.saveOrUpdate(domainObject);
	        getCurrentSession().flush();
	        
	        this.eventPublisher.publishEvent(new OnStudyEventUpdated(domainObject));

	        		
	        return domainObject;
	    }
	 
	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
 this.eventPublisher = applicationEventPublisher;		
	}
}

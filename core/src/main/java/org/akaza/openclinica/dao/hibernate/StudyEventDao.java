package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyEvent;

public class StudyEventDao extends AbstractDomainDao<StudyEvent> {

	public Class<StudyEvent> domainClass(){
		return StudyEvent.class;
	}
	public StudyEvent fetchByStudyEventDefOID(String oid,Integer studySubjectId){
		String query = " from StudyEvent se where se.studySubject.studySubjectId = :studySubjectId and se.studyEventDefinition.oc_oid = :oid order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
		 org.hibernate.Query q = getCurrentSession().createQuery(query);
         q.setInteger("studySubjectId", studySubjectId);
         q.setString("oid", oid);
         return (StudyEvent) q.uniqueResult();
       
		
	}
}

package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;

import java.util.List;

public class EventDefinitionCrfDao extends AbstractDomainDao<EventDefinitionCrf> {

    @Override
    Class<EventDefinitionCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrf.class;
    }
    
    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from "
                + getDomainClassName()
                + " event_definition_crf where event_definition_crf.studyEventDefinition.studyEventDefinitionId = :studyeventdefinitionid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefinitionid", studyEventDefinitionId);
        return (List<EventDefinitionCrf>) q.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findAvailableByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid " + 
                " and do.study.studyId = :studyid and do.statusId = 1";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        return (List<EventDefinitionCrf>) q.list();
        
    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findSiteHiddenByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid " + 
                " and do.study.studyId = :studyid and do.statusId = 1 and do.hideCrf = true";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        return (List<EventDefinitionCrf>) q.list();
        
    }
}

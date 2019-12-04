package core.org.akaza.openclinica.dao.hibernate;

import java.util.List;

import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.hibernate.Query;

public class EventDefinitionCrfDao extends AbstractDomainDao<EventDefinitionCrf> {

    @Override
    Class<EventDefinitionCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrf.class;
    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from " + getDomainClassName()
                + " event_definition_crf where event_definition_crf.studyEventDefinition.studyEventDefinitionId = :studyeventdefinitionid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefinitionid", studyEventDefinitionId);
        return (List<EventDefinitionCrf>) q.list();
    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findAvailableByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid "
                + " and do.study.studyId = :studyid and do.statusId = 1";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        return (List<EventDefinitionCrf>) q.list();

    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findSiteHiddenByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid "
                + " and do.study.studyId = :studyid and do.statusId = 1 and do.hideCrf = true";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        return (List<EventDefinitionCrf>) q.list();

    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findAllSiteDefinitionsByParentDefinition(Integer eventDefinitionCrfId) {
        String query = "from " + getDomainClassName() + " do where do.parentId = :eventDefinitionCrfId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventDefinitionCrfId", eventDefinitionCrfId);
        return (List<EventDefinitionCrf>) q.list();

    }

    @SuppressWarnings("unchecked")
    public EventDefinitionCrf findByStudyEventDefinitionIdAndCRFIdAndStudyId(Integer studyEventDefinitionId, Integer crfId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid "
                + " and do.study.studyId = :studyid and do.crf.crfId = :crfid";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        q.setInteger("crfid", crfId);
        return (EventDefinitionCrf) q.uniqueResult();

    }

    @SuppressWarnings("unchecked")
    public EventDefinitionCrf findByStudyEventDefinitionIdAndCRFIdAndStudyIdorSiteId(Integer studyEventDefinitionId, Integer crfId, Integer studyId) {
        String query = "select do from " + getDomainClassName() + " do,Study s where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid "
                + " and (do.study.studyId = :studyid or (s.studyId = :studyid and s.study.studyId=do.study.studyId)) and do.crf.crfId = :crfid";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        q.setInteger("crfid", crfId);
        return (EventDefinitionCrf) q.uniqueResult();

    }

}

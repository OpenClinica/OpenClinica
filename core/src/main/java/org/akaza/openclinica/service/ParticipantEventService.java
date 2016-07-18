package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class ParticipantEventService {

    private DataSource dataSource = null;
    private StudyDAO studyDAO = null;
    private StudyEventDAO studyEventDAO = null;
    private EventCRFDAO eventCRFDAO = null;
    private EventDefinitionCRFDAO eventDefCRFDAO = null;
    private CRFVersionDAO crfVersionDAO = null;
    
    public ParticipantEventService(DataSource dataSource) { 
        this.dataSource = dataSource;
    }
    
    public StudyEventBean getNextParticipantEvent(StudySubjectBean studySubject) {
        List<StudyEventBean> studyEvents = (ArrayList<StudyEventBean>)getStudyEventDAO().findAllBySubjectIdOrdered(studySubject.getId());
        
        for (StudyEventBean studyEvent:studyEvents) {
            // Skip to next event if study event is not in the right status
            if (studyEvent.getStatus() != Status.AVAILABLE || 
                    (studyEvent.getSubjectEventStatus() != SubjectEventStatus.DATA_ENTRY_STARTED
                    && studyEvent.getSubjectEventStatus() != SubjectEventStatus.SCHEDULED)) continue;
            
            List<EventDefinitionCRFBean> eventDefCrfs = getEventDefCrfsForStudyEvent(studySubject, studyEvent);
            
            for (EventDefinitionCRFBean eventDefCrf:eventDefCrfs) {
                boolean participantForm = eventDefCrf.isParticipantForm();
                
                if (participantForm) {
                    List<CRFVersionBean> crfVersions = getAllCrfVersions(eventDefCrf);
                    
                    boolean eventCrfExists = false;
                    for (CRFVersionBean crfVersion:crfVersions) {
                        EventCRFBean eventCRF = getEventCRFDAO().findByEventCrfVersion(studyEvent, crfVersion);
                        if (eventCRF != null && eventCRF.getStatus() == Status.AVAILABLE) return studyEvent;
                        else if (eventCRF != null) eventCrfExists = true;
                    }
                    if (!eventCrfExists) return studyEvent;
                    
                }
            }
        }
        
        // Did not find a next participant event
        return null;
    }
    
    public EventCRFBean getExistingEventCRF(StudySubjectBean studySubject, StudyEventBean nextEvent,
            EventDefinitionCRFBean eventDefCrf) {

        List<CRFVersionBean> crfVersions = getAllCrfVersions(eventDefCrf);
        for (CRFVersionBean crfVersion:crfVersions) {
            EventCRFBean eventCRF = (EventCRFBean) getEventCRFDAO().findByEventCrfVersion(nextEvent, crfVersion);
            if (eventCRF != null) return eventCRF;
        }
        return null;
    }

    public List<CRFVersionBean> getAllCrfVersions(EventDefinitionCRFBean eventDefCrf) {

        List<CRFVersionBean> versions = new ArrayList<CRFVersionBean>();
        
        EventDefinitionCRFBean selectedEventDefCrf = null;
        if (eventDefCrf.getParentId() > 0) selectedEventDefCrf = (EventDefinitionCRFBean)getEventDefCRFDAO().findByPK(eventDefCrf.getParentId());
        else selectedEventDefCrf = eventDefCrf;
        versions = (ArrayList) getCRFVersionDAO().findAllByCRF(selectedEventDefCrf.getCrfId());
        
        return versions;
       }

    public List<EventDefinitionCRFBean> getEventDefCrfsForStudyEvent(StudySubjectBean studySubject, StudyEventBean studyEvent) {
        Integer studyId = studySubject.getStudyId();
        StudyBean studyBean = (StudyBean) getStudyDAO().findByPK(studyId);
        ArrayList<EventDefinitionCRFBean> eventDefCrfs = null;
        ArrayList<EventDefinitionCRFBean> parentEventDefCrfs = new ArrayList<EventDefinitionCRFBean>();
        ArrayList<EventDefinitionCRFBean> netEventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();

        eventDefCrfs = (ArrayList<EventDefinitionCRFBean>) getEventDefCRFDAO().findAllDefIdandStudyId(studyEvent.getStudyEventDefinitionId(), studyId);

        StudyBean parentStudy = null;
        if (studyBean.getParentStudyId() == 0) parentStudy = studyBean;
        else parentStudy = (StudyBean) getStudyDAO().findByPK(studyBean.getParentStudyId());
        parentEventDefCrfs = (ArrayList<EventDefinitionCRFBean>) getEventDefCRFDAO().findAllDefIdandStudyId(studyEvent.getStudyEventDefinitionId(), parentStudy.getId());

        boolean found;
        for (EventDefinitionCRFBean parentEventDefinitionCrf : parentEventDefCrfs) {
            found = false;
            for (EventDefinitionCRFBean eventDefinitionCrf : eventDefCrfs) {
                if (parentEventDefinitionCrf.getId() == eventDefinitionCrf.getParentId()) { //
                    found = true;
                    netEventDefinitionCrfs.add(eventDefinitionCrf);
                    break;
                }
            }
            if (!found) netEventDefinitionCrfs.add(parentEventDefinitionCrf);
        }

        return netEventDefinitionCrfs;
    }

    /**
     * @return the StudyDAO
     */
    private StudyDAO getStudyDAO() {
        studyDAO = studyDAO != null ? studyDAO : new StudyDAO(dataSource);
        return studyDAO;
    }

    /**
     * @return the StudyEventDAO
     */
    private StudyEventDAO getStudyEventDAO() {
        studyEventDAO = studyEventDAO != null ? studyEventDAO : new StudyEventDAO(dataSource);
        return studyEventDAO;
    }

    /**
     * @return the EventCRFDAO
     */
    private EventCRFDAO getEventCRFDAO() {
        eventCRFDAO = eventCRFDAO != null ? eventCRFDAO : new EventCRFDAO(dataSource);
        return eventCRFDAO;
    }

    /**
     * @return the EventDefinitionCRFDAO
     */
    private EventDefinitionCRFDAO getEventDefCRFDAO() {
        eventDefCRFDAO = eventDefCRFDAO != null ? eventDefCRFDAO : new EventDefinitionCRFDAO(dataSource);
        return eventDefCRFDAO;
    }

    /**
     * @return the CRFVersionDAO
     */
    private CRFVersionDAO getCRFVersionDAO() {
        crfVersionDAO = crfVersionDAO != null ? crfVersionDAO : new CRFVersionDAO(dataSource);
        return crfVersionDAO;
    }

}

package core.org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;

public class ParticipantEventService {

    private DataSource dataSource = null;
    private StudyDao studyDao;
    private StudyEventDAO studyEventDAO = null;
    private EventCRFDAO eventCRFDAO = null;
    private EventDefinitionCRFDAO eventDefCRFDAO = null;
    private FormLayoutDAO formLayoutDAO = null;

    public ParticipantEventService(DataSource dataSource, StudyDao studyDao) {
        this.dataSource = dataSource;
        this.studyDao = studyDao;
    }
    
    public StudyEventBean getNextParticipantEvent(StudySubjectBean studySubject) {
        List<StudyEventBean> studyEvents = (ArrayList<StudyEventBean>)getStudyEventDAO().findAllBySubjectIdOrdered(studySubject.getId());
        
        for (StudyEventBean studyEvent:studyEvents) {
            // Skip to next event if study event is not in the right status
            if (studyEvent.getStatus() != Status.AVAILABLE || 
                    (!studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED)
                    && !studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED))) continue;
            
            List<EventDefinitionCRFBean> eventDefCrfs = getEventDefCrfsForStudyEvent(studySubject, studyEvent);
            
            for (EventDefinitionCRFBean eventDefCrf:eventDefCrfs) {
                boolean participantForm = eventDefCrf.isParticipantForm();
                
                if (participantForm) {
                    List<FormLayoutBean> formLayouts = getAllFormLayouts(eventDefCrf);
                    
                    boolean eventCrfExists = false;
                    for (FormLayoutBean formLayout:formLayouts) {
                        EventCRFBean eventCRF = getEventCRFDAO().findByEventFormLayout(studyEvent, formLayout);
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

        List<FormLayoutBean> formLayouts = getAllFormLayouts(eventDefCrf);
        for (FormLayoutBean formLayout:formLayouts) {
            EventCRFBean eventCRF = (EventCRFBean) getEventCRFDAO().findByEventFormLayout(nextEvent, formLayout);
            if (eventCRF != null) return eventCRF;
        }
        return null;
    }


    public List<FormLayoutBean> getAllFormLayouts(EventDefinitionCRFBean eventDefCrf) {

        List<FormLayoutBean> versions = new ArrayList<FormLayoutBean>();

        EventDefinitionCRFBean selectedEventDefCrf = null;
        if (eventDefCrf.getParentId() > 0) selectedEventDefCrf = (EventDefinitionCRFBean)getEventDefCRFDAO().findByPK(eventDefCrf.getParentId());
        else selectedEventDefCrf = eventDefCrf;
        versions = (ArrayList) getFormLayoutDAO().findAllByCRF(selectedEventDefCrf.getCrfId());

        return versions;
    }
    public List<EventDefinitionCRFBean> getEventDefCrfsForStudyEvent(StudySubjectBean studySubject, StudyEventBean studyEvent) {
        Integer studyId = studySubject.getStudyId();
        Study studyBean = (Study) studyDao.findByPK(studyId);
        ArrayList<EventDefinitionCRFBean> eventDefCrfs = null;
        ArrayList<EventDefinitionCRFBean> parentEventDefCrfs = new ArrayList<EventDefinitionCRFBean>();
        ArrayList<EventDefinitionCRFBean> netEventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();

        eventDefCrfs = (ArrayList<EventDefinitionCRFBean>) getEventDefCRFDAO().findAllDefIdandStudyId(studyEvent.getStudyEventDefinitionId(), studyId);

        Study parentStudy = null;
        if (!studyBean.isSite()) parentStudy = studyBean;
        else parentStudy = (Study) studyDao.findByPK(studyBean.getStudy().getStudyId());
        parentEventDefCrfs = (ArrayList<EventDefinitionCRFBean>) getEventDefCRFDAO().findAllDefIdandStudyId(studyEvent.getStudyEventDefinitionId(), parentStudy.getStudyId());

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
     * @return the FormLayoutDAO
     */

    private FormLayoutDAO getFormLayoutDAO() {
        formLayoutDAO = formLayoutDAO != null ? formLayoutDAO : new FormLayoutDAO(dataSource);
        return formLayoutDAO;
    }

}

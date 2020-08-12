package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.controller.dto.FormRequestDTO;
import org.akaza.openclinica.controller.dto.FormResponseDTO;

import java.util.ArrayList;

/*
    Service class for actions that primarily effect the event_crf.
 */

public interface EventCRFService {
    FormResponseDTO createEventCrf(FormRequestDTO formRequestDTO, String studyOid, String siteOid, UserAccountBean userAccountBean);

    FormResponseDTO updateEventCrf(FormRequestDTO formRequestDTO, String studyOid, String siteOid, UserAccountBean userAccountBean);

    void restoreEventCrf(StudySubjectBean studySubject, StudyEventBean studyEvent, EventCRFBean eventCRFBean, UserAccountBean userAccountBean);

    void removeEventCrf(StudySubjectBean studySubject, StudyEventBean studyEvent, EventCRFBean eventCRFBean, ArrayList<ItemDataBean> itemData, UserAccountBean userAccountBean);

    // Deletes/clears data from a form, previously "delete event CRF".
    void clearEventCrf(StudySubjectBean studySub, StudyEventBean event, EventCRFBean eventCRF, ArrayList<ItemDataBean> itemData, UserAccountBean userAccountBean);
}
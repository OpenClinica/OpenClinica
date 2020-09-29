package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.RestReponseDTO;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


public interface StudyEventService {

    RestReponseDTO scheduleStudyEvent(HttpServletRequest request, String studyOID, String siteOID, String studyEventOID, String participantId, String startDate, String endDate);

    RestReponseDTO scheduleStudyEvent(UserAccountBean ub, String studyOID, String siteOID, String studyEventOID,
                                      String participantId, String startDate, String endDate);

    Object studyEventProcess(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String methodType);

    void populateOdmContainerForEventUpdate(ODMContainer odmContainer, StudyEventUpdateRequestDTO studyEventUpdateRequestDTO, String siteOid);

    void populateOdmContainerForEventSchedule(ODMContainer odmContainer, StudyEventScheduleRequestDTO studyEventScheduleRequestDTO, String siteOid);

    void scheduleOrUpdateBulkEvent(MultipartFile file, Study study, String siteOid, UserAccountBean userAccountBean, JobDetail jobDetail, String schema);

    void convertStudyEventStatus(String value, StudyEvent studyEvent);

    void convertStudyEventBeanStatus(String value, StudyEventBean studyEventBean);

    boolean isEventSignable(StudyEvent studyEvent);

    StudyEvent validateStudyEventExists(StudySubject studySubject, String studyEventOID, String studyEventRepeatKey, CustomRuntimeException validationErrors);

    void removeStudyEvent(StudySubjectBean studySubject, StudyEventBean studyEvent, UserAccountBean userAccountBean);

    void restoreStudyEvent(StudySubjectBean studySubject, StudyEventBean studyEvent, UserAccountBean userAccountBean);

    ArrayList<DisplayEventCRFBean> getDisplayEventCRFs(ArrayList eventCRFs, ArrayList eventDefinitionCRFs, StudyUserRoleBean currentRole, UserAccountBean userAccountBean);
}
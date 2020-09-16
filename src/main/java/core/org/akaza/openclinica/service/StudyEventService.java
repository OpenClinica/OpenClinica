package core.org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

import core.org.akaza.openclinica.bean.login.RestReponseDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.springframework.web.multipart.MultipartFile;


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

}

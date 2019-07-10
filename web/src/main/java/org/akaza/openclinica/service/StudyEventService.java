package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.RestReponseDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.controller.dto.DataImportReport;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface StudyEventService {

    RestReponseDTO scheduleStudyEvent(HttpServletRequest request, String studyOID, String siteOID, String studyEventOID, String participantId, String startDate, String endDate);

    RestReponseDTO scheduleStudyEvent(UserAccountBean ub, String studyOID, String siteOID, String studyEventOID,
                                      String participantId, String startDate, String endDate);

    Object studyEventProcess(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String methodType);

    void populateOdmContainerForEventUpdate(ODMContainer odmContainer, StudyEventUpdateRequestDTO studyEventUpdateRequestDTO, String siteOid);

    void populateOdmContainerForEventSchedule(ODMContainer odmContainer, StudyEventScheduleRequestDTO studyEventScheduleRequestDTO, String siteOid);

    void scheduleOrUpdateBulkEvent(MultipartFile file, Study study, String siteOid, UserAccountBean userAccountBean, JobDetail jobDetail, String schema);

}

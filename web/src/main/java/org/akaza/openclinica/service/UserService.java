/**
 *
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.cdisc.ns.odm.v130.ODM;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author joekeremian
 *
 */

public interface UserService {

    public static final String BULK_JOBS = "bulk_jobs";

    OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, String accessToken,
                                 UserAccountBean ownerUserAccountBean, String customerUuid, ResourceBundle restext);

    OCUserDTO getParticipantAccount(String studyOid, String ssid, String accessToken);

    List<OCUserDTO> getAllParticipantAccountsFromUserService(String accessToken);

    ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String customerUuid, UserAccountBean userAccountBean,boolean auditAccessCodeViewing);
    
    ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String customerUuid, UserAccountBean userAccountBean,boolean auditAccessCodeViewing,boolean includeAccessCode);

    List<OCUserDTO> searchParticipantsByFields(String studyOid, String accessToken, String participantId, String firstName, String lastName, String identifier, UserAccountBean userAccountBean);

    void extractParticipantsInfo(String studyOid, String siteOid, String accessToken, String customerUuid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail,boolean incRelatedInfo);

    JobDetail persistJobCreated(Study study, Study site, UserAccount createdBy, JobType jobType, String sourceFileName);

    void persistJobCompleted(JobDetail jobDetail, String fileName);

    void persistJobFailed(JobDetail jobDetail,String fileName);

    String getFilePath(JobType jobType);

}
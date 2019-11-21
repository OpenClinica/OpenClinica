/**
 *
 */
package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.OCParticipantDTO;
import core.org.akaza.openclinica.service.OCUserDTO;
import core.org.akaza.openclinica.service.ParticipantAccessDTO;

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

    ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String customerUuid, UserAccountBean userAccountBean, boolean auditAccessCodeViewing);
    
    ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String customerUuid, UserAccountBean userAccountBean,boolean auditAccessCodeViewing,boolean includeAccessCode);

    List<OCUserDTO> searchParticipantsByFields(String studyOid, String accessToken, String participantId, String firstName, String lastName, String identifier, UserAccountBean userAccountBean);

    void extractParticipantsInfo(String studyOid, String siteOid, String accessToken, String customerUuid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail,boolean incRelatedInfo,int pageNumber,int pageSize);
    
    StudyParticipantDetailDTO extractParticipantInfo(String studyOid, String siteOid, String accessToken, String customerUuid, UserAccountBean userAccountBean, String participantID,boolean incRelatedInfo) throws OpenClinicaSystemException;

    JobDetail persistJobCreated(Study study, Study site, UserAccount createdBy, JobType jobType, String sourceFileName);

    void persistJobCompleted(JobDetail jobDetail, String fileName);

    void persistJobFailed(JobDetail jobDetail,String fileName);

    String getFilePath(JobType jobType);

}
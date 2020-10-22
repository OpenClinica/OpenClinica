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
import core.org.akaza.openclinica.service.OCUserRoleDTO;
import core.org.akaza.openclinica.service.ParticipantAccessDTO;
import core.org.akaza.openclinica.web.pform.StudyAndSiteEnvUuid;

import java.util.List;
import java.util.ResourceBundle;

/**
 * @author joekeremian
 *
 */

public interface UserService {

    public static final String BULK_JOBS = "bulk_jobs";

    OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, String accessToken,
                                 UserAccountBean ownerUserAccountBean, String realm, ResourceBundle restext);

    OCUserDTO getParticipantAccount(String studyOid, String ssid, String accessToken);

    List<OCUserDTO> getAllParticipantAccountsFromUserService(String accessToken);

    ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String realm, UserAccountBean userAccountBean, boolean auditAccessCodeViewing);
    
    ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String realm, UserAccountBean userAccountBean,boolean auditAccessCodeViewing,boolean includeAccessCode);

    List<OCUserDTO> searchParticipantsByFields(String studyOid, String accessToken, String participantId, String firstName, String lastName, String identifier, UserAccountBean userAccountBean);

    void extractParticipantsInfo(String studyOid, String siteOid, String accessToken, String realm, UserAccountBean userAccountBean, String schema, JobDetail jobDetail,boolean incRelatedInfo,int pageNumber,int pageSize, boolean isStudyLevelUser);

    StudyParticipantDetailDTO extractParticipantInfo(String studyOid, String siteOid, String accessToken, String realm, UserAccountBean userAccountBean, String participantID,boolean incRelatedInfo, boolean isStudyLevelUser) throws OpenClinicaSystemException;

    JobDetail persistJobCreated(Study study, Study site, UserAccount createdBy, JobType jobType, String sourceFileName);

    void persistJobCompleted(JobDetail jobDetail, String fileName);

    void persistJobFailed(JobDetail jobDetail,String fileName);

    String getFilePath(JobType jobType);

    List<OCUserDTO> getfilteredOCUsersDTOFromUserService( StudyAndSiteEnvUuid studyAndSiteEnvUuid, String accessToken);

    List<OCUserRoleDTO> getOcUserRoleDTOsFromUserService(String studyEnvUuid, String accessToken1);

    List<OCUserRoleDTO> addOCUserFromUserService(String studyEnvUUId, String accessToken);

    List<OCUserDTO> filterUserBasedOnStudyEventUuid(List<OCUserRoleDTO> userServiceList, StudyAndSiteEnvUuid studyAndSiteEnvUuid);

}
package org.akaza.openclinica.web.pform;

import org.akaza.openclinica.service.OCUserDTO;
import org.akaza.openclinica.service.OCUserRoleDTO;

import java.util.List;

public interface OpenRosaService {

    String getUserListFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid) throws Exception;

    OCUserDTO fetchUserInfoFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid, String username) throws Exception;

    List<OCUserRoleDTO> getOcUserRoleDTOs(String studyEnvUuid);

}

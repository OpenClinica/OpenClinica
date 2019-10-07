package core.org.akaza.openclinica.web.pform;

import core.org.akaza.openclinica.service.OCUserDTO;
import core.org.akaza.openclinica.service.OCUserRoleDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OpenRosaService {

    String getUserListFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid) throws Exception;

    OCUserDTO fetchUserInfoFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid, String username) throws Exception;

    List<OCUserRoleDTO> getOcUserRoleDTOs(String studyEnvUuid);

}

package org.akaza.openclinica.web.pform;

import org.akaza.openclinica.service.OCUserDTO;

import javax.xml.parsers.ParserConfigurationException;

public interface OpenRosaService {
    public String getUserListFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid) throws Exception;
    public OCUserDTO fetchUserInfoFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid, String username) throws Exception;
}

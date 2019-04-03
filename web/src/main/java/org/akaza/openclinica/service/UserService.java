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
import org.akaza.openclinica.domain.user.UserAccount;
import org.cdisc.ns.odm.v130.ODM;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface UserService {


	OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO,String accessToken,UserAccountBean ownerUserAccountBean,String customerUuid);

	OCUserDTO getParticipantAccount(String studyOid, String ssid,String accessToken);

	List<OCUserDTO> getAllParticipantAccountsFromUserService(String accessToken);

    ParticipantAccessDTO getAccessInfo(String accessToken,String studyOid, String ssid,String customerUuid);

	List<OCUserDTO> searchParticipantsByFields(String studyOid, String accessToken,String participantId,String firstName,String lastName,String identifier,UserAccountBean userAccountBean);

}
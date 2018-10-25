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

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface UserService {


	Object connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, HttpServletRequest request);

	Object getParticipantAccount(String studyOid, String ssid, OCParticipantDTO participantDTO, HttpServletRequest request);

	List<OCUserRoleDTO> getParticipantsByStudyFromUserService(HttpServletRequest request, String studyOid);
}
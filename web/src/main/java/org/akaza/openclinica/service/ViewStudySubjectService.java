/**
 * 
 */
package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.controller.dto.CommonEventContainerDTO;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;

/**
 * @author joekeremian
 *
 */
public interface ViewStudySubjectService {

	/**
	 * This method will add or schedule new event/form
	 * 
	 * @param request
	 * @param studyOid
	 * @param studyEventDefinitionOid
	 * @param crfOid
	 * @param studySubjectOid
	 * @return
	 */
	ViewStudySubjectDTO addNewForm(HttpServletRequest request, String studyOid, String studyEventDefinitionOid, String crfOid, String studySubjectOid);

	/**
	 * This method will retrieve Page object for UI filtering.
	 * 
	 * @param request
	 * @param studyOid
	 * @param name
	 * @return
	 */
	Page getPage(HttpServletRequest request, String studyOid, String name);


	/**
	 *
	 * @param studyEventDefinitionOid
	 * @param crfOid
	 * @param studySubjectOid
	 * @param userAccount
	 * @return
	 */
	public CommonEventContainerDTO addCommonForm(String studyEventDefinitionOid, String crfOid, String studySubjectOid,
												 UserAccount userAccount,String studyOid);

}

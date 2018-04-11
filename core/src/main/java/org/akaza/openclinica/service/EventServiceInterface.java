package org.akaza.openclinica.service;

import java.util.Date;
import java.util.HashMap;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;

public interface EventServiceInterface {

	public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
			String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException;

	/**
	 * This method will remove Study Event Definition
	 * 
	 * @param defId
	 * @param userId
	 */
	public void removeStudyEventDefn(int defId, int userId);

	/**
	 * This method will restore Study Event Definition
	 * 
	 * @param defId
	 * @param userId
	 */
	public void restoreStudyEventDefn(int defId, int userId);

	/**
	 * This method will remove Event Crf definition that will cascade down to events and items
	 * 
	 * @param eventDefnCrfId
	 * @param defId
	 * @param userId
	 * @param studyId
	 */
	public void removeCrfFromEventDefinition(int eventDefnCrfId, int defId, int userId, int studyId);

	/**
	 * This method will restore Event Crf definition that will cascade down to events and items
	 * 
	 * @param eventDefnCrfId
	 * @param defId
	 * @param userId
	 */
	public void restoreCrfFromEventDefinition(int eventDefnCrfId, int defId, int userId);

	/**
	 * This method will make all items available in each event passed in.
	 * 
	 * @param edc
	 * @param sed
	 * @param ub
	 */
	public void restoreAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed, UserAccountBean ub);

	/**
	 * This method will make all items unavailable in each event passed in.
	 * 
	 * @param edc
	 * @param sed
	 * @param ub
	 * @param study
	 */
	public void removeAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed, UserAccountBean ub, StudyBean study);

}
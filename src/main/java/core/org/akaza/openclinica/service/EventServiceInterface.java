package core.org.akaza.openclinica.service;

import java.util.Date;
import java.util.HashMap;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;

public interface EventServiceInterface {

	/**
	 * This method will remove Study Event Definition
	 * 
	 * @param defId
	 * @param userId
	 */
	public void archiveEventDefinition(StudyEventDefinition sed, UserAccount userAccount, Study study);

	/**
	 * This method will restore Study Event Definition
	 * 
	 * @param defId
	 * @param userId
	 */
	public void unArchiveEventDefinition(StudyEventDefinition sed, UserAccount userAccount);

	/**
	 * This method will remove Event Crf definition that will cascade down to events and items
	 * 
	 * @param eventDefnCrfId
	 * @param defId
	 * @param userId
	 * @param studyId
	 */
	public void archiveEventForm(EventDefinitionCrf edc, UserAccount userAccount, Study study);

	/**
	 * This method will restore Event Crf definition that will cascade down to events and items
	 * 
	 * @param eventDefnCrfId
	 * @param defId
	 * @param userId
	 */
	public void unArchiveEventForm(EventDefinitionCrf edc, UserAccount userAccount);

	/**
	 * This method will make all items available in each event passed in.
	 * 
	 * @param edc
	 * @param sed
	 * @param ub
	 */

	public void archiveFormLayout( FormLayout formLayout, UserAccount userAccount);

	}
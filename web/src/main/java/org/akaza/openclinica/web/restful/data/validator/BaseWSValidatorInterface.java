package org.akaza.openclinica.web.restful.data.validator;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.springframework.validation.Errors;
import org.akaza.openclinica.bean.core.Status;

public interface BaseWSValidatorInterface {
	public abstract boolean verifyRole(UserAccountBean user,int study_id, int site_id, Role excluded_role, Errors errors);
	public abstract boolean verifyRole(UserAccountBean user, int study_id, int site_id,  Errors errors);
	
	public abstract StudyBean verifyStudy( StudyDAO dao, String study_id, Status[] included_status,  Errors errors);
	public abstract StudyBean verifyStudyByOID( StudyDAO dao, String study_id, Status[] included_status,	 Errors errors);
	public abstract StudyBean verifySite( StudyDAO dao, String study_id,  String site_id,Status[] included_status,  Errors errors);
	
	public abstract StudyBean verifyStudySubject(String study_id, String subjectId, int max_length, Errors errors);
	
}

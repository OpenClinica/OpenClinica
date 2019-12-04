package core.org.akaza.openclinica.web.restful.data.validator;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.springframework.validation.Errors;

public interface BaseWSValidatorInterface {
	public abstract boolean verifyRole(UserAccountBean user,int study_id, int site_id, Role excluded_role, Errors errors);
	public abstract boolean verifyRole(UserAccountBean user, int study_id, int site_id,  Errors errors);
	
	public abstract Study verifyStudy(StudyDao dao, String study_id, Status[] included_status, Errors errors);
	public abstract Study verifyStudyByOID( StudyDao dao, String study_id, Status[] included_status,	 Errors errors);
	public abstract Study verifySite( StudyDao dao, String study_id,  String site_id,Status[] included_status,  Errors errors);
	
	public abstract Study verifyStudySubject(String study_id, String subjectId, int max_length, Errors errors);
	
}

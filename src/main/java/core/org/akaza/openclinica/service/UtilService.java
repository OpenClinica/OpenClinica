/**
 *
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author joekeremian
 *
 */
public interface UtilService {

	String getAccessTokenFromRequest(HttpServletRequest request);

	void setSchemaFromStudyOid(String studyOid);

	String getCustomerUuidFromRequest(HttpServletRequest request);

	UserAccountBean getUserAccountFromRequest(HttpServletRequest request);

    boolean isParticipantIDSystemGenerated(StudyBean tenantStudy);

    boolean isParticipantUniqueToSite(String siteOID , String studySubjectId);

    void checkFileFormat(MultipartFile file, String fileHeaderMappring);

	}
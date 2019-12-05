/**
 *
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.Study;
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

    boolean isParticipantIDSystemGenerated(Study tenantStudy);

    boolean isParticipantUniqueToSite(String siteOID , String studySubjectId);

    void checkFileFormat(MultipartFile file, String fileHeaderMappring);

	}
/**
 *
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.AddParticipantResponseDTO;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author joekeremian
 *
 */

public interface StudyParticipantService {

    AddParticipantResponseDTO addParticipant(AddParticipantRequestDTO addParticipantRequestDTO, UserAccountBean userAccountBean, Study tenantStudy, Study tenantSite,String realm, String customerUuid, ResourceBundle textsBundle, String accessToken, String register );

    void startBulkAddParticipantJob(MultipartFile file, Study study, Study site,UserAccountBean userAccountBean,  JobDetail jobDetail, String schema,String realm,String customerUuid, ResourceBundle textsBundle,String accessToken, String register);
  
    void startCaseBookPDFJob(JobDetail jobDetail,
                             String schema,
                             Study study,
					    	 Study site,
                             StudySubject ss,
                             ServletContext servletContext,
                             String userAccountID,
                             String fullFinalFilePathName,
                             String format,
                             String margin,
                             String landscape,
                             List   <String> permissionTagsString) throws Exception;

}
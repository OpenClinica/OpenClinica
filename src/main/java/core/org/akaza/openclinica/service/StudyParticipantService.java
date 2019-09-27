/**
 *
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.AddParticipantResponseDTO;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * @author joekeremian
 *
 */

public interface StudyParticipantService {

    AddParticipantResponseDTO addParticipant(AddParticipantRequestDTO addParticipantRequestDTO, UserAccountBean userAccountBean, String studyOid, String siteOid , String customerUuid, ResourceBundle textsBundle, String accessToken, String register );

    void startBulkAddParticipantJob(MultipartFile file, Study study, Study site,UserAccountBean userAccountBean,  JobDetail jobDetail, String schema,String customerUuid, ResourceBundle textsBundle,String accessToken, String register);
  
    File getCaseBookPDF(String studyOID,  
            String studySubjectIdentifier,
            String includeDns, String includeAudits,
            HttpServletRequest request, String userAccountID,
            String clinicaldata,
            String includeMetadata,
            String clinicalData,
            String showArchived ,
            String crossFormLogic,
            String links);
}
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
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author joekeremian
 *
 */

public interface StudyParticipantService {

    AddParticipantResponseDTO addParticipant(AddParticipantRequestDTO addParticipantRequestDTO, UserAccountBean userAccountBean, String studyOid, String siteOid , String customerUuid, ResourceBundle textsBundle, String accessToken, String register );

    void startBulkAddParticipantJob(MultipartFile file, Study study, Study site,UserAccountBean userAccountBean,  JobDetail jobDetail, String schema,String customerUuid, ResourceBundle textsBundle,String accessToken, String register);
  
    File startCaseBookPDFJob(JobDetail jobDetail,
    		String studyOID,  
            String studySubjectIdentifier,            
            ServletContext servletContext,
            String userAccountID,                    
            String fullFinalFilePathName,
            String format, 
            String margin, 
            String landscape,
            List<String> permissionTagsString) throws Exception;
    
    String getMergedPDFcasebookFileName(String studyOID, String studySubjectIdentifier);
}
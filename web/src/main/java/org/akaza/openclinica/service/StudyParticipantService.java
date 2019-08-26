/**
 *
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.ParticipantRestfulRequestDTO;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
import org.springframework.web.multipart.MultipartFile;

import java.util.ResourceBundle;

/**
 * @author joekeremian
 *
 */

public interface StudyParticipantService {

    Object addParticipant(AddParticipantRequestDTO addParticipantRequestDTO, UserAccountBean userAccountBean, String studyOid, String siteOid , String customerUuid, ResourceBundle textsBundle, String accessToken, String register );

    void startBulkAddParticipantJob(MultipartFile file, Study study, Study site,UserAccountBean userAccountBean,  JobDetail jobDetail, String schema,String customerUuid, ResourceBundle textsBundle,String accessToken, String register);


}
/**
 *
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.JobDetailDTO;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;

import java.util.List;

/**
 * @author joekeremian
 *
 */

public interface JobService {


    List<JobDetailDTO> findAllNonDeletedJobsBySiteExceptPublishedStudies(Study tenantSite, UserAccountBean userAccountBean);

    List<JobDetailDTO> findAllNonDeletedJobsByStudyExceptPublishedStudies(Study tenantStudy, UserAccountBean userAccountBean);

    JobDetail saveOrUpdateJob(JobDetail jobDetail);

    void deleteJob(JobDetail jobDetail, UserAccountBean userAccountBean);

}
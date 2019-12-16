package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.JobDetailDTO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.JobDetailDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.UserService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This Service class is used with Bulk Jobs
 * @author joekeremian
 */

@Service("jobService")
@Transactional
@EnableAsync
public class JobServiceImpl implements JobService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;


    @Autowired
    JobDetailDao jobDetailDao;

    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    UserService userService;

    public List<JobDetailDTO> findAllNonDeletedJobsBySiteExceptPublishedStudies(Study tenantSite, UserAccountBean userAccountBean) {
        List<JobDetailDTO> jobDetailDTOS = new ArrayList<>();
        List<JobDetail> jobDetails = jobDetailDao.findAllNonDeletedJobsBySiteExceptPublishedStudies(tenantSite.getStudyId(), userAccountBean.getId());
        for (JobDetail jobDetail : jobDetails) {
            jobDetailDTOS.add(convertEntityToDTO(jobDetail));
        }
        return jobDetailDTOS;
    }

    public List<JobDetailDTO> findAllNonDeletedJobsByStudyExceptPublishedStudies(Study tenantStudy, UserAccountBean userAccountBean) {
        List<JobDetailDTO> jobDetailDTOS = new ArrayList<>();
        List<JobDetail> jobDetails = jobDetailDao.findAllNonDeletedJobsByStudyExceptPublishedStudies(tenantStudy.getStudyId(), userAccountBean.getId());
        for (JobDetail jobDetail : jobDetails) {
            jobDetailDTOS.add(convertEntityToDTO(jobDetail));
        }
        return jobDetailDTOS;
    }


    public JobDetail saveOrUpdateJob(JobDetail jobDetail) {
        return jobDetailDao.saveOrUpdate(jobDetail);
    }

    private JobDetailDTO convertEntityToDTO(JobDetail jobDetail) {
        JobDetailDTO jobDetailDTO = new JobDetailDTO();
        jobDetailDTO.setUuid(jobDetail.getUuid());
        jobDetailDTO.setSiteOid(jobDetail.getSite() != null ? jobDetail.getSite().getOc_oid() : "");
        jobDetailDTO.setStudyOid(jobDetail.getStudy().getOc_oid());
        jobDetailDTO.setCreatedByUsername(jobDetail.getCreatedBy() != null ? jobDetail.getCreatedBy().getUserName() : null);
        jobDetailDTO.setUpdatedByUsername(jobDetail.getUpdatedBy() != null ? jobDetail.getUpdatedBy().getUserName() : null);
        jobDetailDTO.setDateCompleted(jobDetail.getDateCompleted());
        jobDetailDTO.setDateCreated(jobDetail.getDateCreated());
        jobDetailDTO.setDateUpdated(jobDetail.getDateUpdated());
        jobDetailDTO.setStatus(jobDetail.getStatus());
        jobDetailDTO.setType(jobDetail.getType());
        jobDetailDTO.setSourceFileName(jobDetail.getSourceFileName());
        return jobDetailDTO;
    }

    /**
     * Delete the  Job by id.
     * @param id the id of the entity
     */
    public void deleteJob(JobDetail jobDetail, UserAccountBean userAccountBean) {
        UserAccount updaterUserAccount = userAccountDao.findByUserId(userAccountBean.getId());

        jobDetail.setStatus(JobStatus.DELETED);
        jobDetail.setUpdatedBy(updaterUserAccount);
        jobDetail.setDateUpdated(new Date());
        logger.debug("Request to delete Job : {}", jobDetail.getUuid());
        jobDetailDao.saveOrUpdate(jobDetail);
        String fileLocationPath = getFileLocationPath(jobDetail.getLogPath(), jobDetail.getType());
        deleteFile(fileLocationPath);
    }

    private String getFileLocationPath(String fileName, JobType jobType) {
        String fileLocation = getFilePath() + File.separator + jobType.toString().toLowerCase() + File.separator + fileName;
        return fileLocation;
    }

    private String getFilePath() {
        String path = CoreResources.getField("filePath") + userService.BULK_JOBS;
        return path;
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

}
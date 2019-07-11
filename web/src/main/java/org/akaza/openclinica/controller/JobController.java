package org.akaza.openclinica.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.JobDetailDTO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.JobDetailDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.JobStatus;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.JobService;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.web.util.ErrorConstants;
import org.akaza.openclinica.web.util.HeaderUtil;
import org.apache.batik.bridge.UserAgent;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.Timed;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping( value = "/auth/api" )
@Api( value = "Job", tags = {"Job"}, description = "REST API for Job" )
public class JobController {

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    private JobService jobService;

    @Autowired
    private ValidateService validateService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private JobDetailDao jobDetailDao;

    @Autowired
    private UserService userService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String ENTITY_NAME = "JobController";

    public JobController() {
    }


    @ApiOperation( value = "To get all jobs by site", hidden = true )
    @RequestMapping( value = "/studies/{studyOID}/sites/{siteOID}/jobs", method = RequestMethod.GET )
    public ResponseEntity<List<JobDetailDTO>> getAllJobsBySite(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid, @PathVariable( "siteOID" ) String siteOid) throws InterruptedException {
        utilService.setSchemaFromStudyOid(studyOid);
        Study tenantStudy = getTenantStudy(studyOid);
        Study tenantSite = getTenantStudy(siteOid);


        if (!validateService.isStudyOidValid(studyOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INVALID_STUDY_OID, "InValid StudyOID. The StudyOID is invalid or does not exist")).body(null);
        }
        if (!validateService.isStudyOidValidStudyLevelOid(studyOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INVALID_STUDY_OID_AS_STUDY, "InValid StudyOID. The StudyOID should have a Study Level Oid")).body(null);
        }
        if (!validateService.isSiteOidValid(siteOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INVALID_SITE_OID, "InValid SiteOID. The SiteOID is invalid or does not exist")).body(null);
        }
        if (!validateService.isSiteOidValidSiteLevelOid(siteOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INVALID_SITE_OID_AS_SITE, "InValid SiteOID. The SiteOID should have a Site Level Oid")).body(null);
        }
        if (!validateService.isStudyToSiteRelationValid(studyOid, siteOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_MISMATCH_STUDY_OID_AND_SITE_OID, "Mismatch StudyOID and SiteOID. The StudyOID and SiteOID relation is invalid")).body(null);
        }

        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();

        if (!validateService.isUserHasAccessToStudy(userRoles,studyOid) && !validateService.isUserHasAccessToSite(userRoles,siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_ROLE_SETUP);
        }else if (!validateService.isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(userRoles,siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
        }

        String accessToken = utilService.getAccessTokenFromRequest(request);
        String customerUuid = utilService.getCustomerUuidFromRequest(request);

        List<JobDetailDTO> jobDetailDTOS=jobService.findAllNonDeletedJobsBySite(tenantSite,userAccountBean);
        logger.debug("REST request to get all JobDetails by site");

        return new ResponseEntity<List<JobDetailDTO>>(jobDetailDTOS,HttpStatus.OK);
    }


    @ApiOperation( value = "To get all jobs by study", hidden = true )
    @RequestMapping( value = "/studies/{studyOID}/jobs", method = RequestMethod.GET )
    public ResponseEntity<List<JobDetailDTO>> getAllJobsByStudy(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid) throws InterruptedException {
        utilService.setSchemaFromStudyOid(studyOid);
        Study tenantStudy = getTenantStudy(studyOid);


        if (!validateService.isStudyOidValid(studyOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INVALID_STUDY_OID, "InValid StudyOID. The StudyOID is invalid or does not exist")).body(null);
        }
        if (!validateService.isStudyOidValidStudyLevelOid(studyOid)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INVALID_STUDY_OID_AS_STUDY, "InValid StudyOID. The StudyOID should have a Study Level Oid")).body(null);
        }

        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();

        if (!validateService.isUserHasAccessToStudy(userRoles,studyOid) ) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_ROLE_SETUP);
        }else if (!validateService.isUserHas_DM_DEP_DS_RoleInStudy(userRoles,studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
        }

        String accessToken = utilService.getAccessTokenFromRequest(request);
        String customerUuid = utilService.getCustomerUuidFromRequest(request);

        List<JobDetailDTO> jobDetailDTOS=  jobService.findAllNonDeletedJobsByStudy(tenantStudy,userAccountBean);
        logger.debug("REST request to get all JobDetails By study");

        return new ResponseEntity<List<JobDetailDTO>>(jobDetailDTOS,HttpStatus.OK);
    }


    private Study getTenantStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }


    @ApiOperation( value = "To download job files ", notes = "Will download job file" )
    @RequestMapping( value = "/jobs/{uuid}/downloadFile", method = RequestMethod.GET )
    public ResponseEntity<Object> downloadLogFile(HttpServletRequest request, @PathVariable( "uuid" ) String uuid, @RequestParam(required = false) String open, HttpServletResponse response) throws Exception {
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        Study publicStudy = studyDao.findPublicStudyById(userAccountBean.getActiveStudyId());
        String studyOid;
        if (publicStudy.getStudy() == null) {
            studyOid = publicStudy.getOc_oid();
        } else{
            studyOid = publicStudy.getStudy().getOc_oid();
         }
         utilService.setSchemaFromStudyOid(studyOid);

        JobDetail jobDetail=jobDetailDao.findByUuid(uuid);
        if (jobDetail==null) {
            return new ResponseEntity(ErrorConstants.ERR_INVALID_UUID, org.springframework.http.HttpStatus.NOT_FOUND);
        }else if (jobDetail.getCreatedBy().getUserId() != userAccountBean.getId()) {
            return new ResponseEntity(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, org.springframework.http.HttpStatus.OK);
        }else if(jobDetail.getStatus().equals(JobStatus.DELETED)){
            return new ResponseEntity(ErrorConstants.ERR_INVALID_UUID, HttpStatus.NOT_FOUND);
        }else if(jobDetail.getStatus().equals(JobStatus.IN_PROGRESS)){
            return new ResponseEntity(ErrorConstants.ERR_JOB_IN_PROGRESS, org.springframework.http.HttpStatus.OK);
        }else {

        InputStream inputStream = null;
        try {
            String logFileName = getFilePath(jobDetail.getType()) + File.separator + jobDetail.getLogPath();
            File fileToDownload = new File(logFileName);
            inputStream = new FileInputStream(fileToDownload);
            if (!"true".equals(open)) {
                response.setContentType("application/force-download");
                String fileName = URLEncoder.encode(jobDetail.getLogPath(), "UTF-8").replace("+", "%20");
                String userAgent = request.getHeader("user-agent");

                if (userAgent.contains("Firefox") || userAgent.contains("Safari")) {
                    response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
                } else {
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                }
                }
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            logger.debug("Request could not be completed at this moment. Please try again.");
            logger.debug(e.getStackTrace().toString());
            return new ResponseEntity(ErrorConstants.ERR_NO_LOG_FILE_FOUND, org.springframework.http.HttpStatus.OK);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.debug(e.getStackTrace().toString());
                    throw e;
                }
            }
           }
        }
        return new ResponseEntity(org.springframework.http.HttpStatus.OK);

    }

    private String getFilePath(JobType jobType) {
        return CoreResources.getField("filePath") + userService.BULK_JOBS + File.separator+jobType.toString().toLowerCase();
    }


    /**
     * DELETE  /jobs /:uuid : delete the "uuid" job.
     *
     * @param uuid the uuid of the jobDetail to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @ApiOperation( value = "Delete Job ")
    @DeleteMapping( "/jobs/{uuid}" )
    public ResponseEntity<Void> deleteJob(HttpServletRequest request, @PathVariable String uuid) {
        logger.debug("REST request to delete Job : {}", uuid);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        Study publicStudy = studyDao.findPublicStudyById(userAccountBean.getActiveStudyId());
        utilService.setSchemaFromStudyOid(publicStudy.getOc_oid());

        JobDetail jobDetail = jobDetailDao.findByUuid(uuid);
        if (jobDetail.getCreatedBy().getUserId() != userAccountBean.getId()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_INCORRECT_USER, "Incorrect User. The user is not the owner of this log file")).body(null);
        }
        jobService.deleteJob(jobDetail,userAccountBean);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, String.valueOf(uuid))).build();
    }


}
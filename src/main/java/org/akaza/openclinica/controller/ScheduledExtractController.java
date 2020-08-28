package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.web.util.ErrorConstants;
import core.org.akaza.openclinica.web.util.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.akaza.openclinica.controller.dto.JobDetailDTO;
import org.akaza.openclinica.controller.dto.ScheduledExtractJobDetailDTO;
import org.apache.http.entity.ContentType;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping(value = "/auth/api")
@Api(value = "ScheduledExtractJob", tags = {"Scheduled Extract Job"}, description = "REST API for Scheduled Extract Job")
public class ScheduledExtractController {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobController.class);

    @Autowired
    private UtilService utilService;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private ArchivedDatasetFileDAO archivedDatasetFileDAO;

    private static final String ENTITY_NAME = "ScheduledExtractController";


    @ApiOperation(value = "To get list of latest scheduled extract job execution UUIDs and creation time",
            notes = "Requires authentication and permission to access the dataset extracted by the job. " +
                    "Returns a list of job execution UUIDs and creation times for the job specified by the job UUID. " +
                    "A job execution UUID can be used to retrieve the file resulting from that job execution. Job UUIDs " +
                    "can be found on the “View Job” page in your OpenClinica. The job can be configured to keep up to 10 execution files. ")
    @RequestMapping(value = "/extractJobs/{jobUuid}/jobExecutions", method = RequestMethod.GET)
    public ResponseEntity<List<ScheduledExtractJobDetailDTO>> getScheduledExtractJobDatasetIdsAndCreationTime(@PathVariable("jobUuid") String jobUuid,
                                                                                                              HttpServletRequest request,
                                                                                                              HttpServletResponse response) throws SchedulerException {
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        if (!userAccountBean.isSysAdmin() && !userAccountBean.isTechAdmin()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES,
                    "Insufficient privileges.")).body(null);
        }

        ArrayList<ArchivedDatasetFileBean> archivedDatasetFileBeans = archivedDatasetFileDAO.findByJobUuid(jobUuid.trim());

        if (archivedDatasetFileBeans.size() == 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ERR_MISSING_FILE,
                    "No content found for job Uuid: " + jobUuid + ".")).body(null);
        }

        List<ScheduledExtractJobDetailDTO> scheduledExtractJobDetailDTOList = new ArrayList<>();
        for (ArchivedDatasetFileBean adfb : archivedDatasetFileBeans) {
            if (adfb.getStatus().equals(JobStatus.COMPLETED.name()) && !adfb.getFileReference().isEmpty())
                scheduledExtractJobDetailDTOList.add(convertEntityToDTO(adfb));
        }

        return new ResponseEntity<>(scheduledExtractJobDetailDTOList, HttpStatus.OK);
    }


    @ApiOperation(value = "To get extract file for a given job execution UUID", notes = "Requires authentication and permission to access the dataset " +
            "extracted by the job. Retrieves the dataset file produced by the job execution determined by UUID.")
    @RequestMapping(value = "/extractJobs/jobExecutions/{jobExecutionUuid}/dataset", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<Object> getScheduledExtract(@PathVariable("jobExecutionUuid") String jobExecutionUuid,
                                               HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        if (!userAccountBean.isSysAdmin() && !userAccountBean.isTechAdmin()) {
            String errorMessage = errorHelper("User must be type admin.", response);
            return new ResponseEntity<>(errorMessage, org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        ArchivedDatasetFileBean extract = (ArchivedDatasetFileBean) archivedDatasetFileDAO.findByJobExecutionUuid(jobExecutionUuid);
        if (extract.getId() == 0) {
            String errorMessage = errorHelper("Job execution id " + jobExecutionUuid + " is invalid.", response);
            return new ResponseEntity(errorMessage, HttpStatus.NOT_FOUND);
        }

        String filePath = extract.getFileReference();
        if (StringUtil.isBlank(filePath)) {
            String errorMessage = errorHelper("The file reference for job_execution_uuid " + jobExecutionUuid + " has been deleted.", response);
            return new ResponseEntity(errorMessage, HttpStatus.NOT_FOUND);
        }
        logger.debug("Found location of file: " + filePath);

        File file = new File(filePath);
        byte[] contents = java.nio.file.Files.readAllBytes(file.toPath());

        response.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());
        response.setContentLength((int) file.length());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"" + file.getName() + "\""));
        response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + file.length());

        return new ResponseEntity(contents, HttpStatus.OK);
    }

    private String errorHelper(String errorMessage, HttpServletResponse response) {
        logger.debug(errorMessage);
        response.setContentLength(errorMessage.length());
        return errorMessage;
    }

    private ScheduledExtractJobDetailDTO convertEntityToDTO(ArchivedDatasetFileBean archivedDatasetFileBean) {
        ScheduledExtractJobDetailDTO scheduledExtractJobDetailDTO = new ScheduledExtractJobDetailDTO();
        scheduledExtractJobDetailDTO.setDateCreated(archivedDatasetFileBean.getDateCreated());
        scheduledExtractJobDetailDTO.setJobExecutionUuid(archivedDatasetFileBean.getJobExecutionUuid());
        return scheduledExtractJobDetailDTO;
    }

}

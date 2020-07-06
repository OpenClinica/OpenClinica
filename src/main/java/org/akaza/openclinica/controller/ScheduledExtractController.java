package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.service.UtilService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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


    @ApiOperation(value = "To get latest scheduled extract dataset ids and creation time for the job name at study level", notes = "only work for authorized users with the right access permission")
    @RequestMapping(value = "/extractJobs/{jobUuid}/jobExecutions", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<Object> getScheduledExtractJobDatasetIdsAndCreationTime(@PathVariable("jobUuid") String jobUuid,
                                                                           HttpServletRequest request,
                                                                           HttpServletResponse response) throws SchedulerException {
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        if (!userAccountBean.isSysAdmin() && !userAccountBean.isTechAdmin()) {
            String errorMessage = errorHelper("User must be type admin.", response);
            return new ResponseEntity<>(errorMessage, org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        ArrayList<ArchivedDatasetFileBean> archivedDatasetFileBeans = archivedDatasetFileDAO.findByJobUuid(jobUuid);

        if (archivedDatasetFileBeans.size() == 0) {
            String errorMessage = errorHelper("No content found for job Uuid: " + jobUuid + ".", response);
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        String output = "";
        for (ArchivedDatasetFileBean adfb : archivedDatasetFileBeans) {
            if (adfb.getStatus().equals(JobStatus.COMPLETED.name()) && !adfb.getFileReference().isEmpty())
                output += " Dataset Id: " + adfb.getJobExecutionUuid() + "  Date Created: " + adfb.getDateCreated() + "\n";
        }

        return new ResponseEntity<>("Extract files for job name " + jobUuid + ": \n" + output, HttpStatus.OK);
    }


    @ApiOperation(value = "To get latest scheduled extract dataset ids and creation time for the job name at study level", notes = "only work for authorized users with the right access permission")
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

}

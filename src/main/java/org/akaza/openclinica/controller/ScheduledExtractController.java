package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/auth/api")
@Api(value = "ScheduledExtractJob", tags = {"Scheduled Extract Job"}, description = "REST API for Scheduled Extract Job")
public class ScheduledExtractController {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobController.class);

    @Autowired
    private UtilService utilService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private StudyDao studyDao;




    @ApiOperation(value = "To get scheduled extract jobs for specific job name at study level", notes = "only work for authorized users with the right access permission ")
    @RequestMapping(value = "/studies/{studyOID}/extractJobs/{jobName}/result", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<Object> getScheduledExtractJob(@PathVariable("studyOID") String studyOid,
                                                  @PathVariable("jobName") String jobName,
                                                  HttpServletRequest request) throws SchedulerException {

        Study study = studyDao.findByOcOID(studyOid.trim());
        if (study == null) {
            return new ResponseEntity<>("Invalid studyOid.", HttpStatus.NOT_FOUND);
        }

        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        if (!userAccountBean.isSysAdmin() && !userAccountBean.isTechAdmin()) {
            return new ResponseEntity<>("User must be type admin.", HttpStatus.UNAUTHORIZED);
        }

        utilService.setSchemaFromStudyOid(studyOid);
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(jobName, XsltTriggerService.TRIGGER_GROUP_NAME));
        if (jobDetail == null) {
            return new ResponseEntity<>("Invalid job name.", HttpStatus.NOT_FOUND);
        }

        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        int fileId = jobDataMap.getInt("archived_dataset_file_bean_id");
        if (fileId == 0) {
            return new ResponseEntity<>("Could not find extract job.", HttpStatus.NO_CONTENT);
        }

        logger.debug("Found archived_dataset_file_id: " + fileId);

        String link = "<a href=\"" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fileId
                + "\">" + CoreResources.getField("sysURL.base") + "AccessFile?fileId=" + fileId
                + " </a>";

        return new ResponseEntity<>("Download file: " + link, HttpStatus.OK);

    }
}

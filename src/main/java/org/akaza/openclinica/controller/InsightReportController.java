package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.service.InsightReportService;
import org.akaza.openclinica.service.UserService;
import core.org.akaza.openclinica.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * OC-10732 Ability to run a report configured in Insight from within OpenClinica
 */

@Controller
@RequestMapping( value = "/api" )
public class InsightReportController {

    @Autowired
    private UtilService utilService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    private InsightReportService insightReportService;

    private String[] reportNames;

    private String[] reportIds;

    private String replicaSubstring;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping( value = "/insight/report/studies/{studyOID}/participantID/{participantLabel}/create", method = RequestMethod.POST )
    public ResponseEntity<Object> createReport(HttpServletRequest request,
                                             @PathVariable( "studyOID" ) String studyOid,
                                             @PathVariable( "participantLabel" ) String participantLabel) throws Exception {

        logger.info("REST request to POST Insight Report");
        String insightURL = CoreResources.getField("insight.URL");
        String reports = CoreResources.getField("insight.reports");
        splitReports(reports);
        replicaSubstring = CoreResources.getField("insight.report.replica.substring");
        String[] participantLabels = getLabels(participantLabel);
        String username = CoreResources.getField("insight.account.username");
        String password = CoreResources.getField("insight.account.password");

        JobDetail jobDetail = null;
        Study site = studyDao.findByOcOID(studyOid);
        if (site != null) {
            Study parentStd = site.getStudy();
            if (parentStd != null) {
                utilService.setSchemaFromStudyOid(studyOid);
                UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
                UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
                int i;
                for(i = 0; i < reportNames.length; i++) {
                    String fullPath = insightReportService.getFilePath(participantLabel, reportNames[i],
                            userService.getFilePath(JobType.INSIGHT_REPORT));
                    String fileName = insightReportService.getFileName();

                    jobDetail= userService.persistJobCreated(parentStd, site, userAccount, JobType.INSIGHT_REPORT, fileName);

                    doJobInFuture(username, password, insightURL, participantLabels, fullPath, reportIds[i], jobDetail, fileName);
                }
            }
        }
        return new ResponseEntity<Object> ("job UUID : " + jobDetail.getUuid(), HttpStatus.OK);
    }

    public void splitReports(String reports) {
        String[] idNames = reports.split(",");
        reportIds = new String[idNames.length];
        reportNames = new String[idNames.length];
        int i;
        for(i = 0; i < idNames.length; i++) {
            String[] idNname = idNames[i].split(":");
            reportIds[i] = idNname[0].trim();
            reportNames[i] = idNname[1].trim();
        }
    }

    public String[] getLabels(String label) {
        // original label/id not contain replicaSubString
        // replica label/id contain replicaSubString
        String[] labels = new String[2];
        if (label.indexOf(replicaSubstring) > -1) {
            labels[0] = label.split(replicaSubstring)[0];
            labels[1] = label;
        } else {
            labels[0] = label;
            labels[1] = label + replicaSubstring;
        }

        return labels;
    }

    public void doJobInFuture(String username, String password, String insightURL, String[] participantLabels,
                              String fullPath, String reportId, JobDetail jobDetail, String fileName) {

        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                String response = insightReportService.runReport(username, password, insightURL, participantLabels, fullPath, reportId);
                insightReportService.saveToFile(response.toString(), fullPath);
                userService.persistJobCompleted(jobDetail, fileName);
            }catch(Exception e) {
                logger.error("Exception is thrown while creating file : " + e);
                userService.persistJobFailed(jobDetail, fileName);
            }
            return null;
        });
    }
}

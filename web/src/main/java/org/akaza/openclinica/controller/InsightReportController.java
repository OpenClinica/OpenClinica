package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.UtilService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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

    private String reportName;

    private String replicaSubstring;

    public static final String UNDERSCORE = "_";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping( value = "/insight/report/studies/{studyOID}/participantID/{participantLabel}/create", method = RequestMethod.POST )
    public ResponseEntity createReport(HttpServletRequest request,
                                             @PathVariable( "studyOID" ) String studyOid,
                                             @PathVariable( "participantLabel" ) String participantLabel) throws Exception {

        logger.info("REST request to POST Insight Report");
        String insightURL = CoreResources.getField("insight.URL");
        reportName = CoreResources.getField("insight.report.name");
        replicaSubstring = CoreResources.getField("insight.report.replica.substring");
        String reportID = CoreResources.getField("insight.report.ID");
        String[] participantLabels = getLabels(participantLabel);
        String username = CoreResources.getField("insight.account.username");
        String password = CoreResources.getField("insight.account.password");

        // get Session ID
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        String jsonCredential = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String strSession = doPost(insightURL + "/api/session", headers,
                jsonCredential, null);

        if (strSession == null) {
            return new ResponseEntity(HttpStatus.BAD_GATEWAY);
        }

        JSONObject session = new JSONObject(strSession);

        // get content from insight
        headers = new HashMap<String, String>();
        headers.put("X-Metabase-Session", session.get("id").toString());

        String origin = "{\"type\":\"category\"," +
                "\"target\":[\"dimension\",[\"template-tag\",\"original_id\"]]," +
                "\"value\":[\"" + participantLabels[0] + "\"]}";
        String replica = "{\"type\":\"category\"," +
                "\"target\":[\"dimension\",[\"template-tag\",\"replicate_id\"]]," +
                "\"value\":[\"" + participantLabels[1] + "\"]}";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("origin", origin);
        params.put("replica", replica);

        String url = insightURL + "/api/card/" + reportID + "/query/csv?parameters=[{origin},{replica}]";
        String response = doPost(url, headers, null, params);

        if (response != null) {
            Study site = studyDao.findByOcOID(studyOid);
            if (site != null) {
                Study parentStd = site.getStudy();
                if (parentStd != null) {
                    utilService.setSchemaFromStudyOid(studyOid);
                    UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
                    UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());

                    String fileName = saveToFile(response.toString(), participantLabel);
                    if (fileName != null) {
                        JobDetail jobDetail= userService.persistJobCreated(parentStd, site, userAccount, JobType.INSIGHT_REPORT, fileName);
                        userService.persistJobCompleted(jobDetail, fileName);
                    }
                }
            }
        }

        return new ResponseEntity(HttpStatus.OK);
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

    public String doPost(String api, HashMap<String, String> mapHeaders,
                         String body, HashMap<String, String> queries) {
        try {
            HttpHeaders headers = new HttpHeaders();
            for (String i : mapHeaders.keySet()) {
                headers.add(i, mapHeaders.get(i).toString());
            }

            HttpEntity<String> request;
            if (body != null) {
                request = new HttpEntity<>(body, headers);
            } else {
                request = new HttpEntity<String>(headers);
            }
            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> response;
            if (queries != null) {
                response = rest.postForEntity(api, request, String.class, queries);
            } else {
                response = rest.postForEntity(api, request, String.class);
            }
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error sending request : ", e);
            return null;
        }

    }

    public String getFilePath(String label) {
        return userService.getFilePath(JobType.INSIGHT_REPORT) + File.separator + label + UNDERSCORE + reportName +
                new SimpleDateFormat("_yyyy-MM-dd-hhmmssS'.txt'").format(new Date());
    }

    public String saveToFile(String content, String label) {
        FileOutputStream fop = null;
        File file;
        String fileName = null;
        try {

            file = new File(getFilePath(label));
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            fileName = file.getName();

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            logger.error("Error creating file : ", e);
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                logger.error("Error closing output stream : ", e);
            }
            return fileName;
        }
    }
}

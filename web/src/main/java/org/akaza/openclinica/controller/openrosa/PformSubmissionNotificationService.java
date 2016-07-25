package org.akaza.openclinica.controller.openrosa;

import java.util.HashMap;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.service.pmanage.Study;
import org.akaza.openclinica.service.pmanage.Submission;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PformSubmissionNotificationService {

    @Autowired
    private CrfVersionDao crfVersionDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public void notify(String studyOid, HashMap<String, String> subjectContext) {
        try {
            Integer studyEventDefnId = Integer.valueOf(subjectContext.get("studyEventDefinitionID"));
            Integer studyEventOrdinal = Integer.valueOf(subjectContext.get("studyEventOrdinal"));
            String crfVersionOid = subjectContext.get("crfVersionOID");

            String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/submission";
            Submission submission = new Submission();
            Study pManageStudy = new Study();
            pManageStudy.setInstanceUrl(CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid);
            pManageStudy.setStudyOid(studyOid);
            submission.setStudy(pManageStudy);
            submission.setStudy_event_def_id(studyEventDefnId);
            submission.setStudy_event_def_ordinal(studyEventOrdinal);
            submission.setCrf_version_id(crfVersionDao.findByOcOID(crfVersionOid).getCrfVersionId());

            RestTemplate rest = new RestTemplate();
            String result = rest.postForObject(pManageUrl, submission, String.class);
            logger.debug("Notified Participate of CRF submission with a result of: " + result);
        } catch (Exception e) {
            logger.error("Unable to notify Participate of successful CRF submission.");
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }

    }
}

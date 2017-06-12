package org.akaza.openclinica.service.crfdata.xform;

import java.io.Serializable;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class EnketoCredentials implements Serializable {
    private String serverUrl = null;
    private String apiKey = null;
    private String ocInstanceUrl = null;
    protected static final Logger logger = LoggerFactory.getLogger(EnketoCredentials.class);

    @Autowired
    private static StudyDao studyDao;

    private EnketoCredentials() {

    }

    public static EnketoCredentials getInstance(String studyOid) {
        Study study = getParentStudy(studyOid);
        studyOid = study.getOc_oid();
        EnketoCredentials credentials = new EnketoCredentials();
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        credentials.setServerUrl(CoreResources.getField("form.engine.url"));
        credentials.setApiKey(study.getStudyEnvUuid());
        credentials.setOcInstanceUrl(ocUrl);
        return credentials;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getOcInstanceUrl() {
        return ocInstanceUrl;
    }

    public void setOcInstanceUrl(String ocInstanceUrl) {
        this.ocInstanceUrl = ocInstanceUrl;
    }

    public static Study getParentStudy(String studyOid) {
        Study study = studyDao.findPublicStudy(studyOid);
        if (study.getStudy() == null) {
            logger.debug("The Study Oid: " + studyOid + " is a Study level Oid");
            return study;
        } else {
            logger.debug("The Study Oid: " + studyOid + " is a Site level Oid");
            int parentStudyId = study.getStudy().getStudyId();
            Study parentStudy = studyDao.findPublicStudyById(parentStudyId);
            return parentStudy;
        }
    }

    public static StudyDao getStudyDao() {
        return studyDao;
    }

    public static void setStudyDao(StudyDao studyDao) {
        EnketoCredentials.studyDao = studyDao;
    }

}

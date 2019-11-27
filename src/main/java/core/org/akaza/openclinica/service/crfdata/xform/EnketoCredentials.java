package core.org.akaza.openclinica.service.crfdata.xform;

import java.io.Serializable;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EnketoCredentials implements Serializable {
    private String serverUrl = null;
    private String apiKey = null;
    private String ocInstanceUrl = null;
    protected static final Logger logger = LoggerFactory.getLogger(EnketoCredentials.class);

    private static StudyDao studyDao;

    private EnketoCredentials() {

    }
    @Autowired
    public EnketoCredentials(StudyDao studyDao)
    {
        EnketoCredentials.studyDao = studyDao;
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
        Study study = studyDao.findByOcOID(studyOid);
        if (study.getStudy() == null) {
            logger.debug("The Study Oid: " + studyOid + " is a Study level Oid");
            return study;
        } else {
            logger.debug("The Study Oid: " + studyOid + " is a Site level Oid");
            Study parentStudy = study.getStudy();
            return parentStudy;
        }
    }

    public static Study getSiteStudy(String studyOid) {
            Study study = studyDao.findByOcOID(studyOid);
        if (study.getStudy() == null) {
            logger.debug("The Study Oid: " + studyOid + " is a Study level Oid");
            return study;
        } else {
            logger.debug("The Study Oid: " + studyOid + " is a Site level Oid");
            return null;
        }
    }
}

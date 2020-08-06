package core.org.akaza.openclinica.service.pmanage;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSession;

import core.org.akaza.openclinica.bean.login.ParticipantDTO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class ParticipantPortalRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int PARTICIPATE_READ_TIMEOUT = 5000;

    //NOTE: This method is called in SystemController which seems to be tied to all of the OC3 modules. This can likely
    // be removed if we clean up the SystemController class and make sure it is no longer active.
    @Deprecated
    public Authorization getAuthorization(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setReadTimeout(PARTICIPATE_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            Authorization[] response = rest.getForObject(pManageUrl, Authorization[].class);
            if (response.length > 0 && response[0].getAuthorizationStatus() != null)
                return response[0];
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    @Deprecated
    public String getRegistrationStatus(String studyOid) throws Exception {
        //NOTE: This method previously returned the status of the OC3 participate module as derived from the OCUI admin panel.
        // This is different than the module status as stored in the database table "StudyParameterValue".
        // Given all active functionality in OC4 no longer references this I am having this method return INACTIVE.
        // The methods controllers and services still referencing this method are likely deprecated however removing them entirely
        // would require a further look as there may be some fringe parts of the system that still reference this code.
        return "INACTIVE";
    }

}

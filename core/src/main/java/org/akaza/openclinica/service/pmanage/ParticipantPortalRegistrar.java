package org.akaza.openclinica.service.pmanage;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class ParticipantPortalRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int PARTICIPATE_READ_TIMEOUT = 5000;

    public Authorization getAuthorization(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
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

    public String getCachedRegistrationStatus(String studyOid, HttpSession session) throws Exception {
        String regStatus = (String) session.getAttribute("pManageRegistrationStatus");
        if (regStatus == null) {
            regStatus = getRegistrationStatus(studyOid);
            session.setAttribute("pManageRegistrationStatus", regStatus);
        }
        return regStatus;
    }

    public String getRegistrationStatus(String studyOid) throws Exception {
        return loadRegistrationStatus(studyOid);
    }

    private String loadRegistrationStatus(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
        requestFactory.setReadTimeout(PARTICIPATE_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);
        try {
            Authorization[] response = rest.getForObject(pManageUrl, Authorization[].class);
            if (response.length > 0 && response[0].getAuthorizationStatus() != null)
                return response[0].getAuthorizationStatus().getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String getHostNameAvailability(String hostName) {
        String pManageUrl = CoreResources.getField("portalURL") + "/app/permit/studys/name?hostName=" + hostName;
        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
        requestFactory.setReadTimeout(PARTICIPATE_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);
        String response = null;
        try {
            if (!validHostNameCheck(hostName))
                return INVALID;
            response = rest.getForObject(pManageUrl, String.class);
            if (response.equals("UNAVAILABLE"))
                return UNAVAILABLE;
            else if (response.equals("INVALID"))
                return INVALID;
            else if (response.equals("AVAILABLE"))
                return AVAILABLE;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return UNKNOWN;
    }

    public boolean validHostNameCheck(String hostName) {
        String pManageBaseUrl = CoreResources.getField("portalURL");
        if (hostName.contains("."))
            return false;
        try {
            URL baseUrl = new URL(pManageBaseUrl);
            String port = "";
            if (baseUrl.getPort() > 0)
                port = ":" + String.valueOf(baseUrl.getPort());
            // Check that hostname makes a valid URL
            URL customerUrl = new URL(baseUrl.getProtocol() + "://" + hostName + "." + baseUrl.getHost() + port);
            // Check that hostname only contains alphanumeric characters and/or hyphens
            if (hostName.matches("^[A-Za-z0-9-]+$"))
                return true;
        } catch (MalformedURLException mue) {
            logger.error("Error validating customer selected Participate subdomain.");
            logger.error(mue.getMessage());
            logger.error(ExceptionUtils.getStackTrace(mue));
        }
        return false;
    }

    public String registerStudy(String studyOid) {
        return registerStudy(studyOid, null, null);
    }

    public String sendEmailThruMandrillViaOcui(ParticipantDTO participantDTO, String hostname) {
    	String host = hostname.substring(0,hostname.indexOf("/app/oauth2"));
       	String pManageUrl =host + "/app/rest/oc/email";

        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
        requestFactory.setReadTimeout(PARTICIPATE_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            ParticipantDTO response = rest.postForObject(pManageUrl, participantDTO, ParticipantDTO.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String registerStudy(String studyOid, String hostName, String studyName) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        Authorization authRequest = new Authorization();
        Study authStudy = new Study();
        authStudy.setStudyOid(studyOid);
        authStudy.setInstanceUrl(ocUrl);
        authStudy.setHost(hostName);
        authStudy.setStudyName(studyName);
        authStudy.setOpenClinicaVersion(CoreResources.getField("OpenClinica.version"));
        authRequest.setStudy(authStudy);

        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
        requestFactory.setReadTimeout(PARTICIPATE_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            Authorization response = rest.postForObject(pManageUrl, authRequest, Authorization.class);
            if (response != null && response.getAuthorizationStatus() != null)
                return response.getAuthorizationStatus().getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String getStudyHost(String studyOid) throws Exception {

        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL");
        String pManageUrlFull = pManageUrl + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;

        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
        requestFactory.setReadTimeout(PARTICIPATE_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);
        try {
            Authorization[] response = rest.getForObject(pManageUrlFull, Authorization[].class);
            if (response.length > 0 && response[0].getStudy() != null && response[0].getStudy().getHost() != null
                    && !response[0].getStudy().getHost().equals("")) {
                URL url = new URL(pManageUrl);
                String port = "";
                if (url.getPort() > 0)
                    port = ":" + String.valueOf(url.getPort());
                return url.getProtocol() + "://" + response[0].getStudy().getHost() + "." + url.getHost() + port + "/app/oauth2";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

}

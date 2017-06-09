package org.akaza.openclinica.service.crfdata.xform;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.client.RestTemplate;

public class EnketoAPI {

    private String enketoURL = null;
    private String token = null;
    private String ocURL = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String userPasswdCombo;

    public EnketoAPI(EnketoCredentials credentials) {
        this.enketoURL = credentials.getServerUrl();
        this.token = credentials.getApiKey();
        this.ocURL = credentials.getOcInstanceUrl();
        this.userPasswdCombo =  new String(Base64.encodeBase64((CoreResources.getField("ocform.adminapikey") + ":").getBytes()));
    }

    public String getOfflineFormURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v2/survey/offline");
        EnketoURLResponse response = registerAndGetURL(eURL, crfOID);
        if (response != null) {
            String myUrl = response.getOffline_url();
            if (enketoURL.toLowerCase().startsWith("https") && !myUrl.toLowerCase().startsWith("https")) {
                myUrl = myUrl.replaceFirst("http", "https");
            }
            return myUrl;
        } else
            return "";
    }

    public String getFormURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v2/survey/single/fieldsubmission/iframe");
        // URL eURL = new URL(enketoURL + "/api/v2/survey/iframe");

        EnketoURLResponse response = registerAndGetURL(eURL, crfOID);
        if (response != null) {
            String myUrl = response.getSingle_fieldsubmission_iframe_url();
            if (enketoURL.toLowerCase().startsWith("https") && !myUrl.toLowerCase().startsWith("https")) {
                myUrl = myUrl.replaceFirst("http", "https");
            }
            return myUrl;
        } else
            return "";
    }

    public String getFormPreviewURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v2/survey/preview");
        EnketoURLResponse response = registerAndGetURL(eURL, crfOID);
        if (response != null)
            return response.getPreview_url();
        else
            return "";
    }

    private EnketoURLResponse registerAndGetURL(URL url, String crfOID) {
        EnketoURLResponse urlResponse = null;
        try {
            urlResponse = getURL(url, crfOID);
        } catch (Exception e) {
            if (StringUtils.equalsIgnoreCase(e.getMessage(), "401 Unauthorized")
                    || StringUtils.equalsIgnoreCase(e.getMessage(), "403 Forbidden")) {
                savePformRegistration();
                try {
                    urlResponse = getURL(url, crfOID);
                } catch (Exception e1) {
                    logger.error(e.getMessage());
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            } else {
                logger.error(e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        } finally {
            return urlResponse;
        }
    }


    public EnketoURLResponse registerAndGetEditURL(FormLayout formLayout, String flavor, String instance, String ecid, String redirect, boolean markComplete,
            String studyOid, List<FormLayoutMedia> mediaList, String goTo) {
        EnketoURLResponse urlResponse = null;
        try {
            urlResponse = getEditURL(formLayout, flavor, instance, ecid, redirect, markComplete, studyOid, mediaList, goTo);
        } catch (Exception e) {
            if (StringUtils.equalsIgnoreCase(e.getMessage(), "401 Unauthorized")
                    || StringUtils.equalsIgnoreCase(e.getMessage(), "403 Forbidden")) {
                savePformRegistration();
                try {
                    urlResponse = getEditURL(formLayout, flavor, instance, ecid, redirect, markComplete, studyOid, mediaList, goTo);
                } catch (Exception e1) {
                    logger.error(e.getMessage());
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            } else {
                logger.error(e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return urlResponse;
    }

    private EnketoURLResponse getURL(URL url, String crfOID) throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Basic " +
                    new String(Base64.encodeBase64((token + ":").getBytes())));
            headers.add("Accept-Charset", "UTF-8");
            EnketoURLRequest body = new EnketoURLRequest(ocURL, crfOID);
            HttpEntity<EnketoURLRequest> request = new HttpEntity<EnketoURLRequest>(body, headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoURLResponse> response = rest.postForEntity(url.toString(), request, EnketoURLResponse.class);
            if (response != null)
                return response.getBody();
            else
                return null;

    }

    public EnketoAccountResponse savePformRegistration() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + userPasswdCombo);
        headers.add("Accept-Charset", "UTF-8");
        EnketoAccountRequest body = new EnketoAccountRequest(ocURL, token);
        HttpEntity<EnketoAccountRequest> request = new HttpEntity<EnketoAccountRequest>(body, headers);

        RestTemplate rest = new RestTemplate();
        ResponseEntity<EnketoAccountResponse> response = null;
        if (!checkExistingEnketoAccount())
        {
            response = rest.postForEntity(enketoURL + "/accounts/api/v1/account", request,
                    EnketoAccountResponse.class);
        } else {
            try {
                response = rest
                        .exchange(enketoURL + "/accounts/api/v1/account", HttpMethod.PUT, request, EnketoAccountResponse.class, new HashMap<String, String>());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return response.getBody();
    }

    public boolean checkExistingEnketoAccount()
    {
        boolean accountExists = false;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + userPasswdCombo);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<EnketoAccountRequest> entity = new HttpEntity<EnketoAccountRequest>(headers);

        try
        {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoAccountResponse> response = rest.exchange(CoreResources.getField("form.engine.url") +
                            "/accounts/api/v1/account" + "?server_url=" + ocURL +
                            "&api_key=" + token,
                    HttpMethod.GET, entity, EnketoAccountResponse.class);
            if (response.getBody().getCode() == 200) accountExists = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return accountExists;
    }

    public EnketoURLResponse getEditURL(FormLayout formLayout, String flavor, String instance, String ecid, String redirect, boolean markComplete,
            String studyOid, List<FormLayoutMedia> mediaList, String goTo) throws Exception {
        EnketoURLResponse urlResponse = null;
        String crfOid = formLayout.getOcOid() + flavor;
        if (enketoURL == null)
            return null;

        try {
            // Build instanceId to cache populated instance at Enketo with
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String hashString = ecid + "." + String.valueOf(cal.getTimeInMillis());
            ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
            String instanceId = encoder.encodePassword(hashString, null);

            URL eURL = new URL(enketoURL + "/api/v2/instance/fieldsubmission/iframe");
            // URL eURL = new URL(enketoURL + "/api/v2/instance/iframe");

            String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));

            InstanceAttachment attachment = new InstanceAttachment();

            for (FormLayoutMedia media : mediaList) {
                String fileName = media.getName();
                String baseUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
                String downLoadUrl = baseUrl + "/downloadMedia?formLayoutMediaId=" + media.getFormLayoutMediaId();
                attachment.setAdditionalProperty(fileName, downLoadUrl);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Basic " + userPasswdCombo);
            headers.add("Accept-Charset", "UTF-8");
            EnketoEditURLRequest body = new EnketoEditURLRequest(ocURL, crfOid, instanceId, redirect, instance, String.valueOf(markComplete), attachment, goTo);
            HttpEntity<EnketoEditURLRequest> request = new HttpEntity<EnketoEditURLRequest>(body, headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoURLResponse> response = rest.postForEntity(eURL.toString(), request, EnketoURLResponse.class);
            if (response != null)
                urlResponse = response.getBody();

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
        return urlResponse;
    }

}
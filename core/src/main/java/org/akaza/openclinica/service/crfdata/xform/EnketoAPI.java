package org.akaza.openclinica.service.crfdata.xform;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.client.RestTemplate;

public class EnketoAPI {

    private String enketoURL = null;
    private String token = null;
    private String ocURL = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EnketoAPI(EnketoCredentials credentials) {
        this.enketoURL = credentials.getServerUrl();
        this.token = credentials.getApiKey();
        this.ocURL = credentials.getOcInstanceUrl();
    }

    public String getOfflineFormURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + "/api/v2/survey/offline");
        EnketoURLResponse response = getURL(eURL, crfOID);
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

        EnketoURLResponse response = getURL(eURL, crfOID);
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
        EnketoURLResponse response = getURL(eURL, crfOID);
        if (response != null)
            return response.getPreview_url();
        else
            return "";
    }

    private EnketoURLResponse getURL(URL url, String crfOID) {
        try {
            String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Basic " + userPasswdCombo);
            headers.add("Accept-Charset", "UTF-8");
            EnketoURLRequest body = new EnketoURLRequest(ocURL, crfOID);
            HttpEntity<EnketoURLRequest> request = new HttpEntity<EnketoURLRequest>(body, headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoURLResponse> response = rest.postForEntity(url.toString(), request, EnketoURLResponse.class);
            if (response != null)
                return response.getBody();
            else
                return null;

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public HashMap<String, String> getEditURL(FormLayout formLayout, String flavor, String instance, String ecid, String redirect, boolean markComplete,
            List<FormLayoutMedia> mediaList) {
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
            // URL eURL = new URL(enketoURL + "/api/v1/instance/iframe");

            String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));
            List<String> attachementList = null;
            if (mediaList != null) {
                attachementList = new ArrayList<>();
            }
            for (FormLayoutMedia media : mediaList) {
                String fileName = media.getName();
                String downLoadUrl = "http://oc.local:8081/OpenClinica/rest2/openrosa/S_STUDY1/downloadMedia?formLayoutMediaId%3d"
                        + media.getFormLayoutMediaId();
                String instanceAttachement = fileName + "-+-+-" + downLoadUrl;
                attachementList.add(instanceAttachement);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + userPasswdCombo);
            headers.add("Accept-Charset", "UTF-8");
            EnketoEditURLRequest enketoEditUrl = new EnketoEditURLRequest(ocURL, crfOid, instanceId, redirect, instance, String.valueOf(markComplete));
            HashMap<String, String> body = enketoEditUrl.getEnketoEditUrlObject(enketoEditUrl, attachementList);
            HttpEntity<HashMap> request = new HttpEntity<HashMap>(body, headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<HashMap> response = rest.postForEntity(eURL.toString(), request, HashMap.class);
            if (response != null)
                return response.getBody();
            else
                return null;

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

}
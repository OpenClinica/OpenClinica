package org.akaza.openclinica.service.crfdata.xform;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
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
    public static final String QUERY_FLAVOR = "-query";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";

    /*
     * SURVEY : Initial Data Entry
     * INSTANCE : Edit mode (passing in instance)
     * 100% READONLY: applies for both field and query and dn close button should not appear
     */
    public static final String SURVEY_PREVIEW_MODE = "/api/v2/survey/preview";
    public static final String SURVEY_OFFLINE_MODE = "/api/v2/survey/offline";

    public static final String SURVEY_100_PERCENT_READONLY = "/api/v2/survey/view/iframe"; // implemented
    public static final String INSTANCE_100_PERCENT_READONLY = "/api/v2/instance/view/iframe"; // implemented

    public static final String SURVEY_100_PERCENT_WRITABLE = "/api/v2/survey/single/fieldsubmission/iframe"; // implemented
    public static final String INSTANCE_100_PERCENT_WRITABLE = "/api/v2/instance/fieldsubmission/iframe"; // implemented

    public static final String SURVEY_READONLY_WITH_DN_CLOSE_BUTTON = "/api/v2/survey/single/fieldsubmission/view/dnc/iframe";// need
    public static final String INSTANCE_READONLY_WITH_DN_CLOSE_BUTTON = "/api/v2/instance/fieldsubmission/view/dnc/iframe"; // implemented

    public static final String SURVEY_WRITABLE_WITHOUT_DN_CLOSE_BUTTON = "/api/v2/survey/single/fieldsubmission/edit/dn/iframe"; // need
    public static final String INSTANCE_WRITABLE_WITHOUT_DN_CLOSE_BUTTON = "/api/v2/instance/fieldsubmission/edit/dn/iframe"; // need

    public static final String SURVEY_READONLY_WITHOUT_DN_CLOSE_BUTTON = "/api/v2/survey/single/fieldsubmission/view/dn/iframe"; // need
    public static final String INSTANCE_READONLY_WITHOUT_DN_CLOSE_BUTTON = "/api/v2/instance/fieldsubmission/view/dn/iframe"; // implemented

    public EnketoAPI(EnketoCredentials credentials) {
        this.enketoURL = credentials.getServerUrl();
        this.token = credentials.getApiKey();
        this.ocURL = credentials.getOcInstanceUrl();
    }

    public String getOfflineFormURL(String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + SURVEY_OFFLINE_MODE);
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

    public String getFormURL(String crfOID, String studyOid, Role role, Study parentStudy, StudyEvent studyEvent, String mode) throws Exception {
        if (enketoURL == null)
            return "";

        URL eURL = null;
        if (parentStudy.getStatus().equals(Status.LOCKED)
                || (studyEvent != null && studyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.LOCKED.getId()))) {
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
        } else if (parentStudy.getStatus().equals(Status.FROZEN)
                && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
            // eURL = new URL(enketoURL + SURVEY_READONLY_WITHOUT_DN_CLOSE_BUTTON);
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
        } else if (!parentStudy.getStatus().equals(Status.FROZEN) && mode.equals(EDIT_MODE)
                && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
            // eURL = new URL(enketoURL + SURVEY_WRITABLE_WITHOUT_DN_CLOSE_BUTTON);
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_WRITABLE);
        } else if (!parentStudy.getStatus().equals(Status.FROZEN) && mode.equals(VIEW_MODE)
                && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
            // eURL = new URL(enketoURL + SURVEY_READONLY_WITHOUT_DN_CLOSE_BUTTON);
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
        } else if (role == Role.STUDYDIRECTOR || role == Role.COORDINATOR && mode.equals(EDIT_MODE)) {
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_WRITABLE);
        } else if (role == Role.STUDYDIRECTOR || role == Role.COORDINATOR && mode.equals(VIEW_MODE)) {
            // eURL = new URL(enketoURL + SURVEY_READONLY_WITH_DN_CLOSE_BUTTON);
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
        }
        String myUrl = null;
        EnketoURLResponse response = getURL(eURL, crfOID);
        if (response != null) {
            if (response.getSingle_fieldsubmission_iframe_url() != null) {
                myUrl = response.getSingle_fieldsubmission_iframe_url();
            } else if (response.getView_iframe_url() != null) {
                myUrl = response.getView_iframe_url();
            }

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
        URL eURL = new URL(enketoURL + SURVEY_PREVIEW_MODE);
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

    public EnketoURLResponse getEditURL(FormLayout formLayout, String crfFlavor, String instance, String ecid, String redirect, boolean markComplete,
            String studyOid, List<FormLayoutMedia> mediaList, String goTo, String flavor, Role role, Study parentStudy, StudyEvent studyEvent, String mode) {
        String crfOid = formLayout.getOcOid() + crfFlavor;
        if (enketoURL == null)
            return null;

        try {
            // Build instanceId to cache populated instance at Enketo with
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String hashString = ecid + "." + String.valueOf(cal.getTimeInMillis());
            ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
            String instanceId = encoder.encodePassword(hashString, null);
            URL eURL = null;

            if (parentStudy.getStatus().equals(Status.LOCKED) || studyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.LOCKED.getId())) {
                eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                markComplete = false;
            } else if (flavor.equals(QUERY_FLAVOR) && parentStudy.getStatus().equals(Status.FROZEN)
                    && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
                eURL = new URL(enketoURL + INSTANCE_READONLY_WITHOUT_DN_CLOSE_BUTTON);
                markComplete = false;
            } else if (flavor.equals(QUERY_FLAVOR) && !parentStudy.getStatus().equals(Status.FROZEN) && mode.equals(EDIT_MODE)
                    && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
                // eURL = new URL(enketoURL + INSTANCE_WRITABLE_WITHOUT_DN_CLOSE_BUTTON);
                eURL = new URL(enketoURL + INSTANCE_100_PERCENT_WRITABLE);
            } else if (flavor.equals(QUERY_FLAVOR) && !parentStudy.getStatus().equals(Status.FROZEN) && mode.equals(VIEW_MODE)
                    && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
                eURL = new URL(enketoURL + INSTANCE_READONLY_WITHOUT_DN_CLOSE_BUTTON);
                markComplete = false;
            } else if (flavor.equals(QUERY_FLAVOR) && role == Role.MONITOR) {
                eURL = new URL(enketoURL + INSTANCE_READONLY_WITH_DN_CLOSE_BUTTON);
                markComplete = false;
            } else if (flavor.equals(QUERY_FLAVOR) && (role == Role.STUDYDIRECTOR || role == Role.COORDINATOR) && mode.equals(VIEW_MODE)) {
                eURL = new URL(enketoURL + INSTANCE_READONLY_WITH_DN_CLOSE_BUTTON);
                markComplete = false;
            } else if (flavor.equals(QUERY_FLAVOR) && (role == Role.STUDYDIRECTOR || role == Role.COORDINATOR) && mode.equals(EDIT_MODE)) {
                eURL = new URL(enketoURL + INSTANCE_100_PERCENT_WRITABLE);

            } else if (flavor.equals(SINGLE_ITEM_FLAVOR) && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
                eURL = new URL(enketoURL + INSTANCE_READONLY_WITHOUT_DN_CLOSE_BUTTON);
                markComplete = false;
            } else if (flavor.equals(SINGLE_ITEM_FLAVOR) && (role == Role.MONITOR || role == Role.STUDYDIRECTOR || role == Role.COORDINATOR)) {
                eURL = new URL(enketoURL + INSTANCE_READONLY_WITH_DN_CLOSE_BUTTON);
                markComplete = false;
            }

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
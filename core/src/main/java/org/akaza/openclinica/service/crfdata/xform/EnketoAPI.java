package org.akaza.openclinica.service.crfdata.xform;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.service.crfdata.FormUrlObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class EnketoAPI {

    private String enketoURL = null;
    private String token = null;
    private String ocURL = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String QUERY_FLAVOR = "-query";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";
    public static final String PREVIEW_MODE = "preview";

    /*
     * SURVEY : Initial Data Entry
     * INSTANCE : Edit mode (passing in instance)
     * 100% READONLY: applies for both field and query and dn close button should not appear
     */

    // public static final String SURVEY_PREVIEW_MODE = "/api/v2/survey/preview";
    public static final String SURVEY_PREVIEW_MODE = "/oc/api/v1/survey/preview";

    public static final String SURVEY_OFFLINE_MODE = "/api/v2/survey/offline";

    // public static final String SURVEY_100_PERCENT_READONLY = "/api/v2/survey/view/iframe";
    public static final String SURVEY_100_PERCENT_READONLY = "/oc/api/v1/survey/view";

    // public static final String SURVEY_WRITABLE_DN = "/api/v2/survey/single/fieldsubmission/iframe";
    public static final String SURVEY_WRITABLE_DN = "/oc/api/v1/survey/collect";
    // public static final String SURVEY_WRITABLE_DN_CLOSE_BUTTON = "/api/v2/survey/single/fieldsubmission/c/iframe";
    public static final String SURVEY_WRITABLE_DN_CLOSE_BUTTON = "/oc/api/v1/survey/collect/c";

    // public static final String INSTANCE_100_PERCENT_READONLY = "/api/v2/instance/view/iframe";
    public static final String INSTANCE_100_PERCENT_READONLY = "/oc/api/v1/instance/view";

    // public static final String INSTANCE_READONLY_DN = "/api/v2/instance/fieldsubmission/note/iframe";
    public static final String INSTANCE_READONLY_DN = "/oc/api/v1/instance/note";
    // public static final String INSTANCE_READONLY_DN_CLOSE_BUTTON = "/api/v2/instance/fieldsubmission/note/c/iframe";
    public static final String INSTANCE_READONLY_DN_CLOSE_BUTTON = "/oc/api/v1/instance/note/c";

    // public static final String INSTANCE_WRITABLE_DN = "/api/v2/instance/fieldsubmission/iframe";
    public static final String INSTANCE_WRITABLE_DN_RFC = "/oc/api/v1/instance/edit/rfc";
    // public static final String INSTANCE_WRITABLE_DN_CLOSE_BUTTON = "/api/v2/instance/fieldsubmission/c/iframe";
    public static final String INSTANCE_WRITABLE_DN_CLOSE_BUTTON_RFC = "/oc/api/v1/instance/edit/rfc/c";

    // public static final String INSTANCE_WRITABLE_DN = "/api/v2/instance/fieldsubmission/iframe";
    public static final String INSTANCE_WRITABLE_DN = "/oc/api/v1/instance/edit";
    // public static final String INSTANCE_WRITABLE_DN_CLOSE_BUTTON = "/api/v2/instance/fieldsubmission/c/iframe";
    public static final String INSTANCE_WRITABLE_DN_CLOSE_BUTTON = "/oc/api/v1/instance/edit/c";

    private String userPasswdCombo;

    public EnketoAPI(EnketoCredentials credentials) {
        this.enketoURL = credentials.getServerUrl();
        this.token = credentials.getApiKey();
        this.ocURL = credentials.getOcInstanceUrl();
        this.userPasswdCombo = new String(Base64.encodeBase64((CoreResources.getField("ocform.adminapikey") + ":").getBytes()));
    }

    public FormUrlObject getOfflineFormURL(String ecId, String crfOID) throws Exception {
        if (enketoURL == null)
            return null;
        URL eURL = new URL(enketoURL + SURVEY_OFFLINE_MODE);
        EnketoFormResponse response = registerAndGetURL(eURL, ecId, crfOID, null);
        if (response != null) {
            String myUrl = response.getEnketoUrlResponse().getUrl();
            if (enketoURL.toLowerCase().startsWith("https") && !myUrl.toLowerCase().startsWith("https")) {
                myUrl = myUrl.replaceFirst("http", "https");
            }
            return new FormUrlObject(myUrl, false);
        } else
            return null;
    }

    public FormUrlObject getFormURL(String subjectContextKey, String crfOID, String studyOid, Role role, Study parentStudy, StudyEvent studyEvent,
                                    String mode, String loadWarning, boolean isFormLocked) throws Exception {
        boolean lockOn = false;
        boolean shouldLock = false;

        if (enketoURL == null)
            return null;

        URL eURL = null;

        // https://jira.openclinica.com/browse/OC-8269 Open Form when study is locked
        // https://jira.openclinica.com/browse/OC-8270 Open Form when event is locked
        // https://jira.openclinica.com/browse/OC-8276 Open Form when study is frozen
        // https://jira.openclinica.com/browse/OC-8275 Data Specialist views XForms.
        // https://jira.openclinica.com/browse/OC-8274 Data Entry Person views XForms.
        // https://jira.openclinica.com/browse/OC-8272 Investigator views XForms.
        // https://jira.openclinica.com/browse/OC-8273 CRC views XForms.
        // https://jira.openclinica.com/browse/OC-7573 Data Manager views XForms.
        // https://jira.openclinica.com/browse/OC-7574 Study Director views XForms.
        // https://jira.openclinica.com/browse/OC-7575 Monitor views XForms.
        if (parentStudy.getStatus().equals(Status.LOCKED)
                || (studyEvent != null && studyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.LOCKED.getId()))
                || parentStudy.getStatus().equals(Status.FROZEN) || mode.equals(VIEW_MODE)) {
            eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
            lockOn = false;
            // https://jira.openclinica.com/browse/OC-8267 Data Specialist edits XForms.
            // https://jira.openclinica.com/browse/OC-8266 Data Entry Person edits XForms.
            // https://jira.openclinica.com/browse/OC-7572 Investigator edits XForms.
            // https://jira.openclinica.com/browse/OC-7571 CRC edits XForms.
        } else if (mode.equals(EDIT_MODE) && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2 || role == Role.INVESTIGATOR)) {
            if (isFormLocked) {
                eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
                lockOn = false;
            } else {
                eURL = new URL(enketoURL + SURVEY_WRITABLE_DN);
                lockOn = true;
            }
            shouldLock = true;

            // https://jira.openclinica.com/browse/OC-8278 Data Manager edits XForms.
            // https://jira.openclinica.com/browse/OC-8279 Study Director edits XForms.
        } else if (mode.equals(EDIT_MODE) && (role == Role.STUDYDIRECTOR || role == Role.COORDINATOR)) {
            if (isFormLocked) {
                eURL = new URL(enketoURL + SURVEY_100_PERCENT_READONLY);
                lockOn = false;
            } else {
                eURL = new URL(enketoURL + SURVEY_WRITABLE_DN_CLOSE_BUTTON);
                lockOn = true;
            }
            shouldLock = true;
        } else if (mode.equals(PREVIEW_MODE)) {
            eURL = new URL(enketoURL + SURVEY_PREVIEW_MODE);
        }

        String myUrl = null;
        String finalLoadWarning = loadWarning;
        if (!isFormLocked || !shouldLock)
            finalLoadWarning = "";

        EnketoFormResponse response = registerAndGetURL(eURL, subjectContextKey, crfOID, finalLoadWarning);
        if (response != null) {
            if (response.getEnketoUrlResponse().getUrl() != null) {
                myUrl = response.getEnketoUrlResponse().getUrl();
            }

            if (enketoURL.toLowerCase().startsWith("https") && !myUrl.toLowerCase().startsWith("https")) {
                myUrl = myUrl.replaceFirst("http", "https");
            }
            return new FormUrlObject(myUrl, lockOn);
        } else
            return null;
    }

    public String getFormPreviewURL(String ecId, String crfOID) throws Exception {
        if (enketoURL == null)
            return "";
        URL eURL = new URL(enketoURL + SURVEY_PREVIEW_MODE);
        EnketoFormResponse response = registerAndGetURL(eURL, ecId, crfOID, "");
        if (response != null)
            return response.getEnketoUrlResponse().getUrl();
        else
            return "";
    }

    public EnketoURLResponse registerAndDeleteCache(URL url, String ecId,String crfOID) {
        EnketoURLResponse urlResponse = null;
        try {
            deleteCache(url, ecId, crfOID);
        } catch (Exception e) {
            if (StringUtils.equalsIgnoreCase(e.getMessage(), "401 Unauthorized") || StringUtils.equalsIgnoreCase(e.getMessage(), "403 Forbidden")) {
                savePformRegistration();
                try {
                    deleteCache(url, ecId, crfOID);
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

    private EnketoFormResponse registerAndGetURL(URL url, String ecId, String crfOID, String loadWarning) {
        EnketoFormResponse enketoFormResponse = null;
        try {
            enketoFormResponse = getURL(url, ecId, crfOID, loadWarning);
        } catch (Exception e) {
            if (StringUtils.equalsIgnoreCase(e.getMessage(), "401 Unauthorized") || StringUtils.equalsIgnoreCase(e.getMessage(), "403 Forbidden")) {
                savePformRegistration();
                try {
                    enketoFormResponse = getURL(url, ecId, crfOID, loadWarning);
                } catch (Exception e1) {
                    logger.error(e.getMessage());
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            } else {
                logger.error(e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        } finally {
            return enketoFormResponse;
        }
    }

    public EnketoFormResponse registerAndGetActionURL(ActionUrlObject actionUrlObject) {
        EnketoFormResponse formResponse = null;
        try {
            // Role role, Study parentStudy, StudyEvent studyEvent, String mode
            formResponse = getActionURL(actionUrlObject);
        } catch (Exception e) {
            if (StringUtils.equalsIgnoreCase(e.getMessage(), "401 Unauthorized") || StringUtils.equalsIgnoreCase(e.getMessage(), "403 Forbidden")) {
                savePformRegistration();
                try {
                    formResponse = getActionURL(actionUrlObject);
                } catch (Exception e1) {
                    logger.error(e.getMessage());
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            } else {
                logger.error(e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return formResponse;
    }

    private void deleteCache(URL url, String ecId, String crfOID) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((token + ":").getBytes())));
        headers.add("Accept-Charset", "UTF-8");
        EnketoURLRequest body = new EnketoURLRequest(ocURL, ecId, crfOID, null, isJiniEnabled(), getParticipantId(), getParentWindowOrigin());
        HttpEntity<EnketoURLRequest> request = new HttpEntity<EnketoURLRequest>(body, headers);
        RestTemplate rest = new RestTemplate();
        ResponseEntity<String> result = rest.exchange(url.toString(), HttpMethod.DELETE, request, String.class);
    }

    private String isJiniEnabled() {
        String jini ="false";
        String jiniEnabled =CoreResources.getField("jini.enabled");
        if (!jiniEnabled.equals("") && jiniEnabled.equalsIgnoreCase("true")) {
            jini = "true";
        }
        return jini;
    }

    private String getParentWindowOrigin() {
        String origin = null;
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            origin = request.getScheme() + "://" + request.getServerName();
        }
        return origin;
    }

    private String getParticipantId() {
        String pid = null;
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            pid = (String) request.getAttribute("studySubjectId");
        }
        return pid;
    }

    private EnketoFormResponse getURL(URL url, String ecId, String crfOID, String loadWarning) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((token + ":").getBytes())));
        headers.add("Accept-Charset", "UTF-8");
        EnketoURLRequest body = new EnketoURLRequest(ocURL, ecId, crfOID, loadWarning, isJiniEnabled(), getParticipantId(), getParentWindowOrigin());
        HttpEntity<EnketoURLRequest> request = new HttpEntity<EnketoURLRequest>(body, headers);
        RestTemplate rest = new RestTemplate();
        ResponseEntity<EnketoURLResponse> response = rest.postForEntity(url.toString(), request, EnketoURLResponse.class);
        if (response != null) {
            EnketoFormResponse formResponse = new EnketoFormResponse(response.getBody(), false);
            return formResponse;
        } else
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
        if (!checkExistingEnketoAccount()) {
            response = rest.postForEntity(enketoURL + "/accounts/api/v1/account", request, EnketoAccountResponse.class);
        } else {
            try {
                response = rest.exchange(enketoURL + "/accounts/api/v1/account", HttpMethod.PUT, request, EnketoAccountResponse.class,
                        new HashMap<String, String>());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return response.getBody();
    }

    public boolean checkExistingEnketoAccount() {
        boolean accountExists = false;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + userPasswdCombo);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<EnketoAccountRequest> entity = new HttpEntity<EnketoAccountRequest>(headers);

        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<EnketoAccountResponse> response = rest.exchange(
                    CoreResources.getField("form.engine.url") + "/accounts/api/v1/account" + "?server_url=" + ocURL + "&api_key=" + token, HttpMethod.GET,
                    entity, EnketoAccountResponse.class);
            if (response.getBody().getCode() == 200)
                accountExists = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return accountExists;
    }

    public EnketoFormResponse getActionURL(ActionUrlObject actionUrlObject) throws Exception {
        String ecid = actionUrlObject.ecid;
        String crfOid = actionUrlObject.crfOid;
        Study parentStudy = actionUrlObject.parentStudy;
        Study site = actionUrlObject.site;
        StudyEvent studyEvent = actionUrlObject.studyEvent;
        boolean markComplete = actionUrlObject.markComplete;
        EventDefinitionCrf edc = actionUrlObject.edc;
        Role role = actionUrlObject.role;
        String mode = actionUrlObject.mode;
        String flavor = actionUrlObject.flavor;
        List<FormLayoutMedia> mediaList = actionUrlObject.mediaList;
        String instance = actionUrlObject.instance;
        String redirect = actionUrlObject.redirect;
        String goTo = actionUrlObject.goTo;
        String studyOid = actionUrlObject.studyOid;
        String loadWarning = actionUrlObject.loadWarning;
        EventCrf eventCrf = actionUrlObject.eventCrf;
        EnketoURLResponse urlResponse = null;
        boolean lockOn = false;
        boolean shouldLock = false;
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
            // https://jira.openclinica.com/browse/OC-8270 Open Form when event is locked
            // https://jira.openclinica.com/browse/OC-8269 Open Form when study is locked

            if (((parentStudy.getStatus().equals(Status.LOCKED))
                    || (site != null && site.getStatus().equals(Status.LOCKED)))
                 || studyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.LOCKED.getId())
                 || studyEvent.getStatusId().equals(Status.DELETED.getCode())
                 || studyEvent.getStatusId().equals(Status.AUTO_DELETED.getCode())
                 || edc.getStatusId().equals(Status.DELETED.getCode())
                 || edc.getStatusId().equals(Status.AUTO_DELETED.getCode())
                 || eventCrf.getStatusId().equals(Status.DELETED.getCode())
                 || eventCrf.getStatusId().equals(Status.AUTO_DELETED.getCode())) {

                eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                markComplete = false;
                lockOn = false;
                // https://jira.openclinica.com/browse/OC-8275 Data Specialist views XForms.
                // https://jira.openclinica.com/browse/OC-8274 Data Entry Person views XForms.
                // https://jira.openclinica.com/browse/OC-8272 Investigator views XForms.
                // https://jira.openclinica.com/browse/OC-8273 CRC views XForms.
            } else if (flavor.equals(QUERY_FLAVOR) && mode.equals(VIEW_MODE)
                        && (role == Role.RESEARCHASSISTANT
                            || role == Role.RESEARCHASSISTANT2
                            || role == Role.INVESTIGATOR)) {
                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN);
                    lockOn = true;
                }
                shouldLock = true;
                markComplete = false;
                // https://jira.openclinica.com/browse/OC-7575 Monitor views XForms.
                // https://jira.openclinica.com/browse/OC-7574 Study Director views XForms.
                // https://jira.openclinica.com/browse/OC-7573 Data Manager views XForms.
            } else if (flavor.equals(QUERY_FLAVOR) && mode.equals(VIEW_MODE)
                        && (role == Role.STUDYDIRECTOR
                            || role == Role.COORDINATOR
                            || role == Role.MONITOR)) {
                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN_CLOSE_BUTTON);
                    lockOn = true;
                }
                shouldLock = true;
                markComplete = false;

                // https://jira.openclinica.com/browse/OC-8276 Open Form when study is frozen
                // https://jira.openclinica.com/browse/OC-8267 Data Specialist edits XForms.
                // https://jira.openclinica.com/browse/OC-8266 Data Entry Person edits XForms.
                // https://jira.openclinica.com/browse/OC-7572 Investigator edits XForms.
                // https://jira.openclinica.com/browse/OC-7571 CRC edits XForms.
            } else if (flavor.equals(QUERY_FLAVOR) && mode.equals(EDIT_MODE)
                        && ((!parentStudy.getStatus().equals(Status.FROZEN)
                            || (site != null && !site.getStatus().equals(Status.FROZEN))))
                        && (role == Role.RESEARCHASSISTANT
                            || role == Role.RESEARCHASSISTANT2
                            || role == Role.INVESTIGATOR)) {
                if (markComplete) {
                    if(actionUrlObject.formLocked) {
                        eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                        lockOn = false;
                    } else {
                        eURL = new URL(enketoURL + INSTANCE_WRITABLE_DN);
                        lockOn = true;
                    }
                } else {
                    if(actionUrlObject.formLocked) {
                        eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                        lockOn = false;
                    } else {
                        eURL = new URL(enketoURL + INSTANCE_WRITABLE_DN_RFC);
                        lockOn = true;
                    }
                }
                shouldLock = true;
                // https://jira.openclinica.com/browse/OC-8276 Open Form when study is frozen
                // https://jira.openclinica.com/browse/OC-8279 Study Director edits XForms.
                // https://jira.openclinica.com/browse/OC-8278 Data Manager edits XForms.
            } else if (flavor.equals(QUERY_FLAVOR) && mode.equals(EDIT_MODE)
                        && ((!parentStudy.getStatus().equals(Status.FROZEN))
                            || (site != null && !site.getStatus().equals(Status.FROZEN)))
                        && (role == Role.STUDYDIRECTOR
                                || role == Role.COORDINATOR)) {
                if (markComplete) {
                    if(actionUrlObject.formLocked) {
                        eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                        lockOn = false;
                    } else {
                        eURL = new URL(enketoURL + INSTANCE_WRITABLE_DN_CLOSE_BUTTON);
                        lockOn = true;
                    }
                } else {
                    if(actionUrlObject.formLocked) {
                        eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                        lockOn = false;
                    } else {
                        eURL = new URL(enketoURL + INSTANCE_WRITABLE_DN_CLOSE_BUTTON_RFC);
                        lockOn = true;
                    }
                }
                shouldLock = true;
                // https://jira.openclinica.com/browse/OC-8276 Open Form when study is frozen
            } else if (flavor.equals(QUERY_FLAVOR)
                        && mode.equals(EDIT_MODE)
                        && ((parentStudy.getStatus().equals(Status.FROZEN))
                                || (site != null && site.getStatus().equals(Status.FROZEN)))
                        && (role == Role.RESEARCHASSISTANT
                                || role == Role.RESEARCHASSISTANT2
                                || role == Role.INVESTIGATOR)) {

                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN);
                    lockOn = true;
                }

                shouldLock = true;
                markComplete = false;
                // https://jira.openclinica.com/browse/OC-8276 Open Form when study is frozen
            } else if (flavor.equals(QUERY_FLAVOR) && mode.equals(EDIT_MODE)
                    && ((parentStudy.getStatus().equals(Status.FROZEN))
                            || (site != null && site.getStatus().equals(Status.FROZEN)))
                    && (role == Role.STUDYDIRECTOR || role == Role.COORDINATOR)) {
                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN_CLOSE_BUTTON);
                    lockOn = true;
                }

                shouldLock = true;
                markComplete = false;
                // https://jira.openclinica.com/browse/OC-7575 Monitor views XForms.
            } else if (flavor.equals(QUERY_FLAVOR)
                        && mode.equals(EDIT_MODE)
                        && role == Role.MONITOR) {
                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN_CLOSE_BUTTON);
                    lockOn = true;
                }

                shouldLock = true;
                markComplete = false;
                // View Queries for Individual fields
            } else if (flavor.equals(SINGLE_ITEM_FLAVOR)
                    && (role == Role.RESEARCHASSISTANT || role == Role.RESEARCHASSISTANT2
                    || role == Role.INVESTIGATOR)) {

                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN);
                    lockOn = true;
                }
                shouldLock = true;
                markComplete = false;
                // View Queries for Individual fields
            } else if (flavor.equals(SINGLE_ITEM_FLAVOR)
                    && (role == Role.MONITOR || role == Role.STUDYDIRECTOR || role == Role.COORDINATOR)) {
                if(actionUrlObject.formLocked) {
                    eURL = new URL(enketoURL + INSTANCE_100_PERCENT_READONLY);
                    lockOn = false;
                } else {
                    eURL = new URL(enketoURL + INSTANCE_READONLY_DN_CLOSE_BUTTON);
                    lockOn = true;
                }
                markComplete = false;
                shouldLock = true;
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

            if (!actionUrlObject.formLocked || !shouldLock)
                loadWarning = "";
            EnketoEditURLRequest body = new EnketoEditURLRequest(ocURL, actionUrlObject.ecid, crfOid, instanceId, redirect, instance, String.valueOf(markComplete),
                    attachment, goTo, loadWarning, isJiniEnabled(), actionUrlObject.studyEvent.getStudySubject().getLabel(), getParentWindowOrigin());
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
        EnketoFormResponse enketoFormResponse = new EnketoFormResponse(urlResponse, lockOn);
        return enketoFormResponse;
    }

}

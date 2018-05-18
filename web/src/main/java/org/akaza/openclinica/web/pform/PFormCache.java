package org.akaza.openclinica.web.pform;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;

import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.service.crfdata.FormUrlObject;
import org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class PFormCache {
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";

    // HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String, HashMap<String, String>> urlCache = null;
    // HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String, HashMap<String, String>> offlineUrlCache = null;
    // HashMap of context hash, HashMap of properties such as ssoid, crf version oid, etc...
    LinkedHashMap<String, HashMap<String, String>> subjectContextCache = null;

    @Autowired
    private EnketoCredentials enketoCredentials;

    private PFormCache() {

    }

    private PFormCache(ServletContext context) {
        urlCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("pformURLCache");
        offlineUrlCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("pformOfflineURLCache");
        subjectContextCache = (LinkedHashMap<String, HashMap<String, String>>) context.getAttribute("subjectContextCache");

        if (urlCache == null) {
            urlCache = new HashMap<String, HashMap<String, String>>();
            context.setAttribute("pformURLCache", urlCache);
        }
        if (offlineUrlCache == null) {
            offlineUrlCache = new HashMap<String, HashMap<String, String>>();
            context.setAttribute("pformOfflineURLCache", offlineUrlCache);
        }
        if (subjectContextCache == null) {
            subjectContextCache = new LinkedHashMap<String, HashMap<String, String>>();
            context.setAttribute("subjectContextCache", subjectContextCache);
        }

    }

    public static PFormCache getInstance(ServletContext context) throws Exception {
        return new PFormCache(context);
    }

    public String getPFormURL(String studyOID, String formLayoutOID, StudyEvent studyEvent) throws Exception {
        return getPFormURL(studyOID, formLayoutOID, false, studyEvent);
    }

    public String getPFormURL(String studyOID, String formLayoutOID, boolean isOffline, StudyEvent studyEvent) throws Exception {
        Study parentStudy = enketoCredentials.getParentStudy(studyOID);
        studyOID = parentStudy.getOc_oid();
        FormUrlObject formUrlObject = null;

        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));
        HashMap<String, String> studyURLs = null;
        if (isOffline)
            studyURLs = offlineUrlCache.get(studyOID);
        else
            studyURLs = urlCache.get(studyOID);
        if (studyURLs == null) {
            studyURLs = new HashMap<String, String>();
            formUrlObject = null;
            if (isOffline)
                formUrlObject = enketo.getOfflineFormURL(formLayoutOID);
            else
                formUrlObject = enketo.getFormURL(formLayoutOID, studyOID, null, parentStudy, studyEvent, EDIT_MODE, null, false);

            if (formUrlObject.getFormUrl().equals("")) {
                throw new Exception("Unable to get enketo form url.");
            }
            studyURLs.put(formLayoutOID, formUrlObject.getFormUrl());
            if (isOffline)
                offlineUrlCache.put(studyOID, studyURLs);
            else
                urlCache.put(studyOID, studyURLs);
            return formUrlObject.getFormUrl();
        } else if (studyURLs.get(formLayoutOID) == null) {
            if (isOffline)
                formUrlObject = enketo.getOfflineFormURL(formLayoutOID);
            else
                formUrlObject = enketo.getFormURL(formLayoutOID, studyOID,
                        null, parentStudy, studyEvent, EDIT_MODE, null, false);
            studyURLs.put(formLayoutOID, formUrlObject.getFormUrl());
            return formUrlObject.getFormUrl();
        } else
            return studyURLs.get(formLayoutOID);
    }

    public HashMap<String, String> getSubjectContext(String key) throws Exception {
        return subjectContextCache.get(key);
    }

    public String putSubjectContext(PFormCacheSubjectContextEntry entry) {
        return putSubjectContext(entry.getStudySubjectOid(), entry.getStudyEventDefinitionId(), entry.getOrdinal(), entry.getFormLayoutOid(),
                entry.getUserAccountId(), entry.getStudyEventId(), entry.getStudyOid(), entry.getFormLoadMode());
    }

    public String putSubjectContext(String studySubjectOID, String studyEventDefinitionID, String studyEventOrdinal, String formLayoutOID, String studyEventID,
            String studyOid, String formLoadMode) {
        return putSubjectContext(studySubjectOID, studyEventDefinitionID, studyEventOrdinal, formLayoutOID, null, studyEventID, studyOid, formLoadMode);
    }

    public String putSubjectContext(String studySubjectOID, String studyEventDefinitionID, String studyEventOrdinal, String formLayoutOID, String userAccountID,
            String studyEventID, String studyOid, String formLoadMode) {
        HashMap<String, String> contextMap = new HashMap<String, String>();
        contextMap.put("studySubjectOID", studySubjectOID);
        contextMap.put("studyEventDefinitionID", studyEventDefinitionID);
        contextMap.put("studyEventOrdinal", studyEventOrdinal);
        contextMap.put("formLayoutOID", formLayoutOID);
        contextMap.put("userAccountID", userAccountID);
        contextMap.put("studyEventID", studyEventID);
        contextMap.put("formLoadMode", formLoadMode);

        contextMap.put("studyOid", studyOid);
        String hashString = userAccountID + "." + studySubjectOID + "." + studyEventDefinitionID + "." + studyEventOrdinal + "." + formLayoutOID;
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String hashOutput = encoder.encodePassword(hashString, null);
        subjectContextCache.remove(hashOutput);
        subjectContextCache.put(hashOutput, contextMap);
        return hashOutput;
    }

    public String putAnonymousFormContext(String studyOID, String formLayoutOID, int studyEventDefinitionId) {
        HashMap<String, String> contextMap = new HashMap<String, String>();
        contextMap.put("studySubjectOID", null);
        contextMap.put("studyOID", studyOID);
        contextMap.put("formLayoutOID", formLayoutOID);
        contextMap.put("studyEventDefinitionID", String.valueOf(studyEventDefinitionId));
        contextMap.put("studyEventOrdinal", "1");

        String hashString = studyOID + "." + formLayoutOID;
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String hashOutput = encoder.encodePassword(hashString, null);
        subjectContextCache.put(hashOutput, contextMap);
        return hashOutput;
    }

}

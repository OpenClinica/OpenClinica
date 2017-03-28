package org.akaza.openclinica.web.pform;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class PFormCache {

    // HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String, HashMap<String, String>> urlCache = null;
    // HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String, HashMap<String, String>> offlineUrlCache = null;
    // HashMap of context hash, HashMap of properties such as ssoid, crf version oid, etc...
    HashMap<String, HashMap<String, String>> subjectContextCache = null;
    @Autowired
    private EnketoCredentials enketoCredentials;

    private PFormCache() {

    }

    private PFormCache(ServletContext context) {
        urlCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("pformURLCache");
        offlineUrlCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("pformOfflineURLCache");
        subjectContextCache = (HashMap<String, HashMap<String, String>>) context.getAttribute("subjectContextCache");

        if (urlCache == null) {
            urlCache = new HashMap<String, HashMap<String, String>>();
            context.setAttribute("pformURLCache", urlCache);
        }
        if (offlineUrlCache == null) {
            offlineUrlCache = new HashMap<String, HashMap<String, String>>();
            context.setAttribute("pformOfflineURLCache", offlineUrlCache);
        }
        if (subjectContextCache == null) {
            subjectContextCache = new HashMap<String, HashMap<String, String>>();
            context.setAttribute("subjectContextCache", subjectContextCache);
        }

    }

    public static PFormCache getInstance(ServletContext context) throws Exception {
        return new PFormCache(context);
    }

    public String getPFormURL(String studyOID, String formLayoutOID) throws Exception {
        return getPFormURL(studyOID, formLayoutOID, false);
    }

    public String getPFormURL(String studyOID, String formLayoutOID, boolean isOffline) throws Exception {
        Study study = enketoCredentials.getParentStudy(studyOID);
        studyOID = study.getOc_oid();
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));
        HashMap<String, String> studyURLs = null;
        if (isOffline)
            studyURLs = offlineUrlCache.get(studyOID);
        else
            studyURLs = urlCache.get(studyOID);
        if (studyURLs == null) {
            studyURLs = new HashMap<String, String>();
            String url = null;
            if (isOffline)
                url = enketo.getOfflineFormURL(formLayoutOID);
            else
                url = enketo.getFormURL(formLayoutOID);

            if (url.equals("")) {
                throw new Exception("Unable to get enketo form url.");
            }
            studyURLs.put(formLayoutOID, url);
            if (isOffline)
                offlineUrlCache.put(studyOID, studyURLs);
            else
                urlCache.put(studyOID, studyURLs);
            return url;
        } else if (studyURLs.get(formLayoutOID) == null) {
            String url = null;
            if (isOffline)
                url = enketo.getOfflineFormURL(formLayoutOID);
            else
                url = enketo.getFormURL(formLayoutOID);
            studyURLs.put(formLayoutOID, url);
            return url;
        } else
            return studyURLs.get(formLayoutOID);
    }

    public HashMap<String, String> getSubjectContext(String key) throws Exception {
        return subjectContextCache.get(key);
    }

    public String putSubjectContext(PFormCacheSubjectContextEntry entry) {
        return putSubjectContext(entry.getStudySubjectOid(), entry.getStudyEventDefinitionId().toString(), entry.getOrdinal().toString(),
                entry.getFormLayoutOid(), entry.getUserAccountId().toString());
    }

    public String putSubjectContext(String studySubjectOID, String studyEventDefinitionID, String studyEventOrdinal, String formLayoutOID) {
        return putSubjectContext(studySubjectOID, studyEventDefinitionID, studyEventOrdinal, formLayoutOID, null);
    }

    public String putSubjectContext(String studySubjectOID, String studyEventDefinitionID, String studyEventOrdinal, String formLayoutOID,
            String userAccountID) {
        HashMap<String, String> contextMap = new HashMap<String, String>();
        contextMap.put("studySubjectOID", studySubjectOID);
        contextMap.put("studyEventDefinitionID", studyEventDefinitionID);
        contextMap.put("studyEventOrdinal", studyEventOrdinal);
        contextMap.put("formLayoutOID", formLayoutOID);
        contextMap.put("userAccountID", userAccountID);

        String hashString = studySubjectOID + "." + studyEventDefinitionID + "." + studyEventOrdinal + "." + formLayoutOID;
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String hashOutput = encoder.encodePassword(hashString, null);
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

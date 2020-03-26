package core.org.akaza.openclinica.web.pform;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.service.crfdata.FormUrlObject;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import core.org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class PFormCache {
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

    public String getPFormURL(String studyOID, String crfOID, StudyEvent studyEvent ,boolean isOffline,String contextHash) throws Exception {
        return getPFormURL(studyOID, crfOID, studyEvent,contextHash);
    }

    public String getPFormURL(String studyOID, String crfOID, StudyEvent studyEvent,String contextHash) throws Exception {
        Study parentStudy = enketoCredentials.getParentStudy(studyOID);
        Study site = enketoCredentials.getSiteStudy(studyOID);

        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(parentStudy.getOc_oid()));
        HashMap<String, String> studyURLs = null;

        FormUrlObject formUrlObject = enketo.getFormURL(contextHash, crfOID, site,
                Role.RESEARCHASSISTANT, parentStudy, studyEvent, EnketoAPI.PARTICIPATE_MODE, null, false);
        return formUrlObject.getFormUrl();
    }


    public HashMap<String, String> getSubjectContext(String key) throws Exception {
        return subjectContextCache.get(key);
    }

    public String putSubjectContext(PFormCacheSubjectContextEntry entry) {
        return putSubjectContext(entry.getStudySubjectOid(), entry.getStudyEventDefinitionId(), entry.getOrdinal(), entry.getFormLayoutOid(),
                entry.getUserAccountId(), entry.getStudyEventId(), entry.getStudyOid(), entry.getFormLoadMode());
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

        HttpServletRequest request= CoreResources.getRequest();
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        contextMap.put("accessToken", accessToken);


        contextMap.put("studyOID", studyOid);
        String hashString = userAccountID + "." + studySubjectOID + "." + studyEventDefinitionID + "." + studyEventOrdinal + "." + formLayoutOID;
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String hashOutput = encoder.encodePassword(hashString, null);
        subjectContextCache.remove(hashOutput);
        subjectContextCache.put(hashOutput, contextMap);
        return hashOutput;
    }

    public String putSubjectContext(String studySubjectOID, String studyEventDefinitionID, String studyEventOrdinal, String formLayoutOID, String userAccountID,
                                    String studyEventID, String studyOid, String formLoadMode,String accessToken) {
        HashMap<String, String> contextMap = new HashMap<String, String>();
        contextMap.put("studySubjectOID", studySubjectOID);
        contextMap.put("studyEventDefinitionID", studyEventDefinitionID);
        contextMap.put("studyEventOrdinal", studyEventOrdinal);
        contextMap.put("formLayoutOID", formLayoutOID);
        contextMap.put("userAccountID", userAccountID);
        contextMap.put("studyEventID", studyEventID);
        contextMap.put("formLoadMode", formLoadMode);
        contextMap.put("accessToken", accessToken);


        contextMap.put("studyOID", studyOid);
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

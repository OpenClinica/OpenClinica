package org.akaza.openclinica.web.pform;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class PFormCache {
    
    //HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String,HashMap<String,String>> urlCache = null;
    //HashMap of study, HashMap of crfVersionOID, pFormURL
    HashMap<String,HashMap<String,String>> offlineUrlCache = null;
    //HashMap of context hash, HashMap of properties such as ssoid, crf version oid, etc...
    HashMap<String,HashMap<String,String>> subjectContextCache = null;

    private PFormCache()
    {
        
    }
    
    private PFormCache(ServletContext context)
    {
        urlCache = (HashMap<String,HashMap<String,String>>) context.getAttribute("pformURLCache");
        offlineUrlCache = (HashMap<String,HashMap<String,String>>) context.getAttribute("pformOfflineURLCache");
        subjectContextCache = (HashMap<String,HashMap<String,String>>) context.getAttribute("subjectContextCache");
        
        if (urlCache == null) 
        {
            urlCache = new HashMap<String,HashMap<String,String>>();
            context.setAttribute("pformURLCache",urlCache);
        }
        if (offlineUrlCache == null) 
        {
            offlineUrlCache = new HashMap<String,HashMap<String,String>>();
            context.setAttribute("pformOfflineURLCache",offlineUrlCache);
        }
        if (subjectContextCache == null)
        {
            subjectContextCache = new HashMap<String,HashMap<String,String>>();
            context.setAttribute("subjectContextCache", subjectContextCache);
        }

    }

    public static PFormCache getInstance(ServletContext context) throws Exception
    {
        return new PFormCache(context);        
    }

    public String getPFormURL(String studyOID, String crfVersionOID) throws Exception
    {
        return getPFormURL(studyOID, crfVersionOID, false);
    }
    public String getPFormURL(String studyOID, String crfVersionOID, boolean isOffline) throws Exception
    {
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));
        HashMap<String,String> studyURLs = null;
        if (isOffline) studyURLs = offlineUrlCache.get(studyOID);
        else studyURLs = urlCache.get(studyOID);
        if (studyURLs == null)
        {
            studyURLs = new HashMap<String,String>();
            String url = null;
            if (isOffline) url = enketo.getOfflineFormURL(crfVersionOID);
            else url = enketo.getFormURL(crfVersionOID);
            
            if (url.equals("")) {
                throw new Exception("Unable to get enketo form url.");
            }
            studyURLs.put(crfVersionOID,url);
            if (isOffline) offlineUrlCache.put(studyOID, studyURLs);
            else urlCache.put(studyOID, studyURLs);
            return url;
        }
        else if (studyURLs.get(crfVersionOID) == null)
        {
            String url = null;
            if (isOffline) url = enketo.getOfflineFormURL(crfVersionOID);
            else url = enketo.getFormURL(crfVersionOID);
            studyURLs.put(crfVersionOID,url);
            return url;
        }
        else return studyURLs.get(crfVersionOID);
    }

    public HashMap<String,String> getSubjectContext(String key) throws Exception
    {
        return subjectContextCache.get(key);
    }

    public String putSubjectContext(PFormCacheSubjectContextEntry entry) {
        return putSubjectContext(entry.getStudySubjectOid(),entry.getStudyEventDefinitionId().toString(),
                entry.getOrdinal().toString(),entry.getCrfVersionOid());
    }
    
    public String putSubjectContext(String studySubjectOID, String studyEventDefinitionID, 
            String studyEventOrdinal, String crfVersionOID)
    {
        HashMap<String,String> contextMap = new HashMap<String,String>();
        contextMap.put("studySubjectOID",studySubjectOID);
        contextMap.put("studyEventDefinitionID",studyEventDefinitionID);
        contextMap.put("studyEventOrdinal", studyEventOrdinal);
        contextMap.put("crfVersionOID", crfVersionOID);
        
        String hashString = studySubjectOID + "." + studyEventDefinitionID + "." + studyEventOrdinal + "." + crfVersionOID;
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String hashOutput = encoder.encodePassword(hashString,null);
        subjectContextCache.put(hashOutput, contextMap);
        return hashOutput;
    }
    public String putAnonymousFormContext(String studyOID,  String crfVersionOID , int studyEventDefinitionId)
    {
        HashMap<String,String> contextMap = new HashMap<String,String>();
        contextMap.put("studySubjectOID",null);
        contextMap.put("studyOID",studyOID);
        contextMap.put("crfVersionOID", crfVersionOID);
        contextMap.put("studyEventDefinitionID", String.valueOf(studyEventDefinitionId));
        contextMap.put("studyEventOrdinal", "1");
        
        String hashString = studyOID + "." + crfVersionOID;
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String hashOutput = encoder.encodePassword(hashString,null);
        subjectContextCache.put(hashOutput, contextMap);
        return hashOutput;
    }

}

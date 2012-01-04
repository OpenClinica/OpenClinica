/**
 * 
 */
package org.akaza.openclinica.control.urlRewrite;

import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.control.core.CoreSecureController;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pgawade Servlet to call appropriate application pages correspond to
 *         supported RESTful URLs
 * 
 */
public class UrlRewriteServlet extends CoreSecureController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {

    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {

            String requestURI = request.getRequestURI();
            String requestQueryStr = request.getQueryString();

            OpenClinicaResource ocResource = null;
            String requestOIDStr = null;
            String RESTUrlStart = "/ClinicalData/html/view/";
            if ((null != requestURI) && (requestURI.contains("/ClinicalData/html/view/"))) {
                requestOIDStr = requestURI.substring(requestURI.indexOf(RESTUrlStart) + RESTUrlStart.length(), requestURI.length());
            }

            ocResource = getOpenClinicaResourceFromURL(requestOIDStr);

            // If the form OID in the request uri is not null, it will be
            // interpretted as a request to
            // view form data and hence will be forwarded to servlet path
            // "/ViewSectionDataEntry"
            if ((null != ocResource) && (ocResource.getFormVersionOID() != null)) {
                HashMap<String, String> mapQueryParams = getQueryStringParameters(requestQueryStr);

            // set the required parameters into request
                if (null != ocResource.getEventDefinitionCrfId()) {
                    request.setAttribute("eventDefinitionCRFId", ocResource.getEventDefinitionCrfId().toString());
                }
                if (null != ocResource.getEventCrfId()) {
                    request.setAttribute("ecId", ocResource.getEventCrfId().toString());
                }
                if (null != ocResource.getStudyEventId()) {
                    request.setAttribute("eventId", ocResource.getStudyEventId().toString());
                }
                if (null != ocResource.getStudySubjectID()) {
                    request.setAttribute("studySubjectId", ocResource.getStudySubjectID().toString());
                    // request.setAttribute("exitTo", "ViewStudySubject?id=" +
                    // ocResource.getStudySubjectID());
                }
                // request.setAttribute("crfVersionId","");
                if ((null != mapQueryParams) && (mapQueryParams.size() != 0)) {
                    if (mapQueryParams.containsKey("tabId")) {
                        request.setAttribute("tabId", mapQueryParams.get("tabId"));
                    }
                    if ((null != ocResource.getStudySubjectID()) && (mapQueryParams.containsKey("exitTo"))) {
                        request.setAttribute("exitTo", "ViewStudySubject?id=" + ocResource.getStudySubjectID());
                    }
                }

                forwardPage(Page.VIEW_SECTION_DATA_ENTRY_SERVLET, request, response);
                // response.sendRedirect(Page.VIEW_SECTION_DATA_ENTRY_SERVLET.getFileName());
            }

            // implement other RESTful URLs here forwarding to corresponding
            // pages of application
            // (and associated combinations of mode and format)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private HashMap<String, String> getQueryStringParameters(String queryString) {
        HashMap<String, String> mapQueryParams = new HashMap<String, String>();

        if ((null != queryString) && (!queryString.equalsIgnoreCase(""))) {
            if (queryString.contains("&")) {
                String[] tokens = queryString.split("&");
                if (tokens.length != 0) {
                    String token = null;
                    String tokenBeforeEq = null;
                    String tokenAfterEq = null;
                    for (int i = 0; i < tokens.length; i++) {
                        token = tokens[i];
                        if ((null != token) && (!token.equalsIgnoreCase("")) && (token.contains("="))) {
                            tokenBeforeEq = token.substring(0, token.indexOf("="));
                            tokenAfterEq = token.substring(token.indexOf("=") + 1);
                            mapQueryParams.put(tokenBeforeEq, tokenAfterEq);
                        }
                    }
                }
            }
        }
        return mapQueryParams;
    }

    /**
     * Method to parse the request URL parameters and get the respective
     * database identifiers
     * 
     * @param URLPath
     *            - example "S_CPCS/320999/SE_CPCS%5B1%5D/F_CPCS_1"
     * @param queryString
     *            - example
     *            "format=html&mode=view&tabId=1&exitTo=ViewStudySubject"
     * @return
     */
    public OpenClinicaResource getOpenClinicaResourceFromURL(String URLPath/*
                                                                            * ,
                                                                            * String
                                                                            * queryString
                                                                            */) {
        OpenClinicaResource openClinicaResource = new OpenClinicaResource();

        if ((null != URLPath) && (!URLPath.equals(""))) {
            if (URLPath.contains("/")) {
                String[] tokens = URLPath.split("/");
                if (tokens.length != 0) {
                    String URLParamValue = "";
                    StudyDAO stdao = new StudyDAO(getDataSource());
                    StudySubjectDAO ssubdao = new StudySubjectDAO(getDataSource());
                    StudyEventDefinitionDAO sedefdao = new StudyEventDefinitionDAO(getDataSource());
                    CRFDAO crfdao = new CRFDAO(getDataSource());
                    CRFVersionDAO crfvdao = new CRFVersionDAO(getDataSource());
                    ItemDAO idao = new ItemDAO(getDataSource());
                    ItemGroupDAO igdao = new ItemGroupDAO(getDataSource());
                    StudyEventDAO sedao = new StudyEventDAO(getDataSource());

                    StudyBean study = null;
                    StudySubjectBean subject = null;
                    StudyEventDefinitionBean sed = null;
                    CRFBean c = null;
                    CRFVersionBean cv = null;
                    ItemBean item = null;
                    ItemGroupBean ig = null;

                    Integer studySubjectId = 0;
                    Integer eventDefId = 0;
                    Integer eventRepeatKey = 0;

                    for (int i = 0; i < tokens.length; i++) {
                        // when interpreting these request URL parameters, the
                        // assumption is that the position of
                        // each type of parameters will be fixed. Meaning, study
                        // OID is always going to be at the start
                        // followed by StudySubjectKey followed by study event
                        // definition OID followed by
                        // study event repeat key followed by form OID followed
                        // by item group OID followed by
                        // item group repeat key followed by item OID
                        // It can also be done based on the start of OID value
                        // (example study OID presently
                        // starts with 'S_' but we will have to change it if we
                        // change the method of generating
                        // oID values in future.

                        URLParamValue = tokens[i].trim();
                        if ((null != URLParamValue) && (!URLParamValue.equals(""))) {
                            switch (i) {
                            case 0: {// study OID
                                study = stdao.findByOid(URLParamValue);
                                openClinicaResource.setStudyOID(URLParamValue);
                                if (null != study) {
                                    openClinicaResource.setStudyID(study.getId());
                                }
                                break;
                            }

                            case 1: {// StudySubjectKey
                                subject = ssubdao.findByOidAndStudy(URLParamValue, study.getId());
                                openClinicaResource.setStudySubjectOID(URLParamValue);
                                if (null != subject) {
                                    studySubjectId = subject.getId();
                                    openClinicaResource.setStudySubjectID(studySubjectId);
                                }
                                break;
                            }

                            case 2: {// study event definition OID
                                // separate study event OID and study event
                                // repeat key
                                String seoid = "";
                                String eventOrdinal = "";
                                if (URLParamValue.contains("%5B")) {
                                    seoid = URLParamValue.substring(0, URLParamValue.indexOf("%5B"));
                                    openClinicaResource.setStudyEventDefOID(seoid);
                                    eventOrdinal = URLParamValue.substring(URLParamValue.indexOf("%5B") + 3, URLParamValue.indexOf("%5D"));
                                }
                                if ((null != seoid) && (null != study)) {
                                    sed = sedefdao.findByOidAndStudy(seoid, study.getId(), study.getParentStudyId());

                                    if (null != sed) {
                                        eventDefId = sed.getId();
                                        openClinicaResource.setStudyEventDefID(eventDefId);
                                    }
                                }
                                if (null != eventRepeatKey) {
                                    eventRepeatKey = Integer.parseInt(eventOrdinal.trim());
                                    openClinicaResource.setStudyEventRepeatKey(eventRepeatKey);
                                }
                                break;
                            }

                            case 3: {// form OID
                                openClinicaResource.setFormVersionOID(URLParamValue);
                                if (null != study) {
                                    // cv =
                                    // crfvdao.findByCrfVersionOidAndStudy(URLParamValue,
                                    // study.getId());
                                    // if (null != cv) {
                                    // openClinicaResource.setFormVersionID(cv.getId());
                                    // openClinicaResource.setFormID(cv.getCrfId());
                                    // }

                                    HashMap studySubjectCRFDataDetails =
                                        sedao.getStudySubjectCRFData(study, studySubjectId, eventDefId, URLParamValue, eventRepeatKey);
                                    if ((null != studySubjectCRFDataDetails) && (studySubjectCRFDataDetails.size() != 0)) {
                                        if (studySubjectCRFDataDetails.containsKey("event_crf_id")) {
                                            openClinicaResource.setEventCrfId((Integer) studySubjectCRFDataDetails.get("event_crf_id"));
                                        }

                                        if (studySubjectCRFDataDetails.containsKey("event_definition_crf_id")) {
                                            openClinicaResource.setEventDefinitionCrfId((Integer) studySubjectCRFDataDetails.get("event_crf_id"));
                                        }

                                        if (studySubjectCRFDataDetails.containsKey("study_event_id")) {
                                            openClinicaResource.setStudyEventId((Integer) studySubjectCRFDataDetails.get("event_crf_id"));
                                        }
                                    }
                                }
                                break;
                            }

                            case 4: {// item group OID
                                // separate item group OID and item group
                                // repeat key
                                String igoid = "";
                                String igRepeatKey = "";
                                if (URLParamValue.contains("[")) {
                                    igoid = URLParamValue.substring(1, URLParamValue.indexOf("["));
                                    igRepeatKey = URLParamValue.substring(URLParamValue.indexOf("["), URLParamValue.indexOf("}]"));
                                }
                                if ((null != igoid) && (null != cv)) {
                                    ig = igdao.findByOidAndCrf(URLParamValue, cv.getCrfId());

                                    if (null != ig) {
                                        openClinicaResource.setItemGroupID(ig.getId());
                                    }
                                }
                                if (null != igRepeatKey) {
                                    openClinicaResource.setItemGroupRepeatKey(Integer.parseInt(igRepeatKey));
                                }
                                break;
                            }

                            case 5: {// item OID
                                // item = idao.find
                                break;
                            }

                            }// switch end
                        }
                    }
                }

            }

        }

        return openClinicaResource;
    }

}

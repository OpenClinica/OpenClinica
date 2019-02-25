package org.akaza.openclinica.web.restful;

import com.sun.jersey.api.view.Viewable;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfPermissionTag;
import org.akaza.openclinica.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/***
 * * Rest service for ODM clinical data usage
 * ROOT_CONTEXT/rest/clinicaldata/{format}/{mode}/{STUDYOID} format:xml/ json
 * mode:view
 *
 * @author jnyayapathi
 *
 */

@Path("/clinicaldata")
@Component
@Scope("prototype")
public class ODMClinicaDataResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ODMClinicaDataResource.class);
    private static final int INDENT_LEVEL = 2;

    @Autowired
    private EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao;
    @Autowired
    private ClinicalDataCollectorResource clinicalDataCollectorResource;
    @Autowired
    private MetadataCollectorResource metadataCollectorResource;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PermissionService permissionService;

    public MetadataCollectorResource getMetadataCollectorResource() {
        return metadataCollectorResource;
    }

    public void setMetadataCollectorResource(MetadataCollectorResource metadataCollectorResource) {
        this.metadataCollectorResource = metadataCollectorResource;
    }

    public ClinicalDataCollectorResource getClinicalDataCollectorResource() {
        return clinicalDataCollectorResource;
    }

    public void setClinicalDataCollectorResource(ClinicalDataCollectorResource clinicalDataCollectorResource) {
        this.clinicalDataCollectorResource = clinicalDataCollectorResource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @api {get} /rest/clinicaldata/json/view/:study/:subject/:event/:form Retrieve case report forms - JSON
     * @apiVersion 3.8.0
     * @apiName getODMClinicaldataJSON
     * @apiGroup Subject
     * @apiPermission user
     * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
     * @apiParam {String} study Study or Site OID.
     * @apiParam {String} subject Subject Key or ID.
     * @apiParam {String} event Study Event Definition OID. Use '*' for all.
     * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
     * @apiError NoAccessRight Only authenticated users can access the data.
     * @apiError NotFound The resource was not found.
     */
    @GET
    @Path("/json/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getODMClinicaldata(@PathParam("studyOID") String studyOID, @PathParam("formVersionOID") String formVersionOID,
                                     @PathParam("studyEventOID") String studyEventOID, @PathParam("studySubjectIdentifier") String studySubjectIdentifier,
                                     @DefaultValue("n") @QueryParam("includeDNs") String includeDns, @DefaultValue("n") @QueryParam("includeAudits") String includeAudits,
                                     @Context HttpServletRequest request,
                                     @DefaultValue("y") @QueryParam("clinicaldata") String clinicaldata,
                                     @DefaultValue("y") @QueryParam("metadata") String metadata,
                                     @QueryParam("showArchived") String showArchived ,
                                     @DefaultValue("n") @QueryParam("crossFormLogic") String crossFormLogic ) {
        LOGGER.debug("Requesting clinical data resource");
        boolean includeDN = false;
        boolean includeAudit = false;
        boolean crossForm = false;
        boolean archived = false;
        boolean meta = true;
        boolean clinical = true;

        if (showArchived != null && (showArchived.equalsIgnoreCase("no") || showArchived.equalsIgnoreCase("n")))
            archived = false;
        if (showArchived != null && (showArchived.equalsIgnoreCase("yes") || showArchived.equalsIgnoreCase("y")))
            archived = true;

        if (crossFormLogic.equalsIgnoreCase("no") || crossFormLogic.equalsIgnoreCase("n"))
            crossForm = false;
        if (crossFormLogic.equalsIgnoreCase("yes") || crossFormLogic.equalsIgnoreCase("y"))
            crossForm = true;
        if (metadata.equalsIgnoreCase("no") || metadata.equalsIgnoreCase("n"))
            meta = false;
        if (metadata.equalsIgnoreCase("yes") || metadata.equalsIgnoreCase("y"))
            meta = true;
        if (clinicaldata.equalsIgnoreCase("no") || clinicaldata.equalsIgnoreCase("n"))
            clinical = false;
        if (clinicaldata.equalsIgnoreCase("yes") || clinicaldata.equalsIgnoreCase("y"))
            clinical = true;
        if (includeDns.equalsIgnoreCase("no") || includeDns.equalsIgnoreCase("n"))
            includeDN = false;
        if (includeAudits.equalsIgnoreCase("no") || includeAudits.equalsIgnoreCase("n"))
            includeAudit = false;
        if (includeDns.equalsIgnoreCase("yes") || includeDns.equalsIgnoreCase("y"))
            includeDN = true;
        if (includeAudits.equalsIgnoreCase("yes") || includeAudits.equalsIgnoreCase("y"))
            includeAudit = true;
        UserAccountBean userAccountBean = ((UserAccountBean) request.getSession().getAttribute("userBean"));
        StudyDAO sdao = new StudyDAO(getDataSource());
        StudyBean studyBean = sdao.findByOid(studyOID);
        String permissionTagsString = "";
        String[] permissionTagsStringArray;

        if (crossForm) {
            permissionTagsString = loadPermissionTagsString();
            permissionTagsStringArray = loadPermissionTagsStringArray();
        } else {
            permissionTagsString = permissionService.getPermissionTagsString(studyBean, request);
            permissionTagsStringArray = permissionService.getPermissionTagsStringArray(studyBean, request);
        }
        FullReportBean report=null;

        LinkedHashMap<String,OdmClinicalDataBean> clinicalDataBeans;

        if (clinical) {
            clinicalDataBeans = getClinicalDataCollectorResource().generateClinicalData(studyOID, getStudySubjectOID(studySubjectIdentifier, studyOID), studyEventOID,
                    formVersionOID, includeDN, includeAudit, request.getLocale(), userAccountBean.getId(), crossForm);
        } else {
            clinicalDataBeans = null;
        }

        XMLSerializer xmlSerializer = new XMLSerializer();
             report = getMetadataCollectorResource().collectODMMetadataForClinicalData(studyOID, formVersionOID,clinicalDataBeans, archived, permissionTagsString,meta);


        report.createOdmXml(true, getDataSource(), userAccountBean, permissionTagsStringArray,meta,clinical,crossForm);
        xmlSerializer.setTypeHintsEnabled(true);
        JSON json = xmlSerializer.read(report.getXmlOutput().toString().trim());

        JSONClinicalDataPostProcessor processor = new JSONClinicalDataPostProcessor(request.getLocale());
        processor.process((JSONObject) json);

        return json.toString(INDENT_LEVEL);
    }

    /**
     * @api {get} /rest/clinicaldata/html/print/:study/:subject/:event/:form Retrieve case report forms - HTML
     * @apiVersion 3.8.0
     * @apiName getPrintCRFController
     * @apiGroup Subject
     * @apiPermission user
     * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
     * @apiParam {String} study Study or Site OID.
     * @apiParam {String} subject Subject Key or ID.
     * @apiParam {String} event Study Event Definition OID. Use '*' for all.
     * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
     * @apiError NoAccessRight Only authenticated users can access the data.
     * @apiError NotFound The resource was not found.
     * @apiErrorExample Response (example):
     * HTTP/1.1 401 Not Authenticated
     * {
     * "error": "NoAccessRight"
     * }
     */
    @GET
    @Path("/html/print/{studyOID}/{studySubjectIdentifier}/{eventOID}/{formVersionOID}")
    public Viewable getPrintCRFController(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
                                          @PathParam("studySubjectIdentifier") String studySubjectIdentifier, @PathParam("eventOID") String eventOID,
                                          @PathParam("formVersionOID") String formVersionOID, @DefaultValue("n") @QueryParam("includeDNs") String includeDns,
                                          @DefaultValue("n") @QueryParam("includeAudits") String includeAudits) throws Exception {
        request.setCharacterEncoding("UTF-8");
        request.setAttribute("studyOID", studyOID);
        request.setAttribute("studySubjectOID", getStudySubjectOID(studySubjectIdentifier, studyOID));
        request.setAttribute("eventOID", eventOID);
        request.setAttribute("formVersionOID", formVersionOID);
        request.setAttribute("includeAudits", includeAudits);
        request.setAttribute("includeDNs", includeDns);
        return new Viewable("/WEB-INF/jsp/printcrf.jsp", null);
    }

    /**
     * @api {get} /rest/clinicaldata/xml/view/:study/:subject/:event/:form Retrieve case report forms - XML
     * @apiVersion 3.8.0
     * @apiName getODMMetadata
     * @apiGroup Subject
     * @apiPermission user
     * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
     * @apiParam {String} study Study or Site OID.
     * @apiParam {String} subject Subject Key or ID.
     * @apiParam {String} event Study Event Definition OID. Use '*' for all.
     * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
     * @apiError NoAccessRight Only authenticated users can access the data.
     * @apiError NotFound The resource was not found.
     * @apiErrorExample Response (example):
     * HTTP/1.1 401 Not Authenticated
     * {
     * "error": "NoAccessRight"
     * }
     */
    @GET
    @Path("/xml/view/{studyOID}/{studySubjectIdentifier}/{studyEventOID}/{formVersionOID}")
    @Produces(MediaType.TEXT_XML)
    public String getODMMetadata(@PathParam("studyOID") String studyOID, @PathParam("formVersionOID") String formVersionOID,
                                 @PathParam("studySubjectIdentifier") String studySubjectIdentifier, @PathParam("studyEventOID") String studyEventOID,
                                 @DefaultValue("n") @QueryParam("includeDNs") String includeDns, @DefaultValue("n") @QueryParam("includeAudits") String includeAudits,
                                 @Context HttpServletRequest request, String userAccountID,
                                 @DefaultValue("y") @QueryParam("clinicaldata") String clinicaldata,
                                 @DefaultValue("y") @QueryParam("metadata") String metadata,
                                 @QueryParam("showArchived") String showArchived ,
                                 @DefaultValue("n")@QueryParam("crossFormLogic") String crossFormLogic ) {
        LOGGER.debug("Requesting clinical data resource");
        boolean includeDN = false;
        boolean includeAudit = false;
        boolean crossForm = false;
        boolean archived = false;
        boolean meta = true;
        boolean clinical = true;


        if (showArchived != null && (showArchived.equalsIgnoreCase("no") || showArchived.equalsIgnoreCase("n")))
            archived = false;
        if (showArchived != null && (showArchived.equalsIgnoreCase("yes") || showArchived.equalsIgnoreCase("y")))
            archived = true;

        int userId = 0;
        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (userBean != null) {
            userId = userBean.getId();
        } else {
            userId = Integer.valueOf(userAccountID);
            userBean = (UserAccountBean) new UserAccountDAO(dataSource).findByPK(userId);
        }

        if (crossFormLogic.equalsIgnoreCase("no") || crossFormLogic.equalsIgnoreCase("n"))
            crossForm = false;
        if (crossFormLogic.equalsIgnoreCase("yes") || crossFormLogic.equalsIgnoreCase("y"))
            crossForm = true;
        if (metadata.equalsIgnoreCase("no") || metadata.equalsIgnoreCase("n"))
            meta = false;
        if (metadata.equalsIgnoreCase("yes") || metadata.equalsIgnoreCase("y"))
            meta = true;
        if (clinicaldata.equalsIgnoreCase("no") || clinicaldata.equalsIgnoreCase("n"))
            clinical = false;
        if (clinicaldata.equalsIgnoreCase("yes") || clinicaldata.equalsIgnoreCase("y"))
            clinical = true;
        if (includeDns.equalsIgnoreCase("no") || includeDns.equalsIgnoreCase("n"))
            includeDN = false;
        if (includeAudits.equalsIgnoreCase("no") || includeAudits.equalsIgnoreCase("n"))
            includeAudit = false;
        if (includeDns.equalsIgnoreCase("yes") || includeDns.equalsIgnoreCase("y"))
            includeDN = true;
        if (includeAudits.equalsIgnoreCase("yes") || includeAudits.equalsIgnoreCase("y"))
            includeAudit = true;
        StudyDAO sdao = new StudyDAO(getDataSource());
        StudyBean studyBean = sdao.findByOid(studyOID);
        String permissionTagsString = "";
        String[] permissionTagsStringArray;
        if (crossForm) {
            permissionTagsString = loadPermissionTagsString();
            permissionTagsStringArray = loadPermissionTagsStringArray();
        } else {
            permissionTagsString = permissionService.getPermissionTagsString(studyBean, request);
            permissionTagsStringArray = permissionService.getPermissionTagsStringArray(studyBean, request);
        }
        FullReportBean report=null;


        LinkedHashMap<String,OdmClinicalDataBean> clinicalDataBeans;

        if (clinical) {
            clinicalDataBeans = getClinicalDataCollectorResource().generateClinicalData(studyOID, getStudySubjectOID(studySubjectIdentifier, studyOID), studyEventOID,
                    formVersionOID, includeDN, includeAudit, request.getLocale(), userId, crossForm);
        } else {
            clinicalDataBeans = null;
        }

        report = getMetadataCollectorResource().collectODMMetadataForClinicalData(studyOID, formVersionOID, clinicalDataBeans, archived, permissionTagsString, meta);

        report.createOdmXml(true, getDataSource(), userBean, permissionTagsStringArray,meta,clinical,crossForm);
        LOGGER.debug(report.getXmlOutput().toString().trim());

        return report.getXmlOutput().toString().trim();
    }

    public String getODMXMLData(@PathParam("studyOID") String studyOID, @PathParam("formVersionOID") String formVersionOID,
                                   @PathParam("studyEventOID") String studyEventOID, @PathParam("studySubjectIdentifier") String studySubjectIdentifier,
                                   @DefaultValue("n") @QueryParam("includeDNs") String includeDns, @DefaultValue("n") @QueryParam("includeAudits") String includeAudits,
                                   @Context HttpServletRequest request,
                                   @DefaultValue("y") @QueryParam("clinicaldata") String clinicaldata,
                                   @DefaultValue("y") @QueryParam("metadata") String metadata,
                                   @QueryParam("showArchived") String showArchived ,
                                   @DefaultValue("n")@QueryParam("crossFormLogic") String crossFormLogic ) {

        LOGGER.debug("Requesting clinical data resource");
        boolean includeDN = false;
        boolean includeAudit = false;
        boolean crossForm = false;
        boolean archived = false;
        boolean meta = true;
        boolean clinical = true;

        if (showArchived != null && (showArchived.equalsIgnoreCase("no") || showArchived.equalsIgnoreCase("n")))
            archived = false;
        if (showArchived != null && (showArchived.equalsIgnoreCase("yes") || showArchived.equalsIgnoreCase("y")))
            archived = true;

        if (crossFormLogic.equalsIgnoreCase("no") || crossFormLogic.equalsIgnoreCase("n"))
            crossForm = false;
        if (crossFormLogic.equalsIgnoreCase("yes") || crossFormLogic.equalsIgnoreCase("y"))
            crossForm = true;
        if (metadata.equalsIgnoreCase("no") || metadata.equalsIgnoreCase("n"))
            meta = false;
        if (metadata.equalsIgnoreCase("yes") || metadata.equalsIgnoreCase("y"))
            meta = true;
        if (clinicaldata.equalsIgnoreCase("no") || clinicaldata.equalsIgnoreCase("n"))
            clinical = false;
        if (clinicaldata.equalsIgnoreCase("yes") || clinicaldata.equalsIgnoreCase("y"))
            clinical = true;
        if (includeDns.equalsIgnoreCase("no") || includeDns.equalsIgnoreCase("n"))
            includeDN = false;
        if (includeAudits.equalsIgnoreCase("no") || includeAudits.equalsIgnoreCase("n"))
            includeAudit = false;
        if (includeDns.equalsIgnoreCase("yes") || includeDns.equalsIgnoreCase("y"))
            includeDN = true;
        if (includeAudits.equalsIgnoreCase("yes") || includeAudits.equalsIgnoreCase("y"))
            includeAudit = true;

        int userId = 0;
        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (userBean != null) {
            userId = userBean.getId();
        } else {
            userBean = (UserAccountBean) new UserAccountDAO(dataSource).findByPK(userId);
            userId = userBean.getId();
        }
        StudyDAO sdao = new StudyDAO(getDataSource());
        StudyBean studyBean = sdao.findByOid(studyOID);
        String permissionTagsString = "";
        String[] permissionTagsStringArray;

        if (crossForm) {
            permissionTagsString = loadPermissionTagsString();
            permissionTagsStringArray = loadPermissionTagsStringArray();
        } else {
            permissionTagsString = permissionService.getPermissionTagsString(studyBean, request);
            permissionTagsStringArray = permissionService.getPermissionTagsStringArray(studyBean, request);
        }
        FullReportBean report=null;

        LinkedHashMap<String,OdmClinicalDataBean> clinicalDataBeans;

        if (clinical) {
            clinicalDataBeans = getClinicalDataCollectorResource().generateClinicalData(studyOID, getStudySubjectOID(studySubjectIdentifier, studyOID), studyEventOID,
                    formVersionOID, includeDN, includeAudit, request.getLocale(), userId, crossForm);
        } else {
            clinicalDataBeans = null;
        }
        report = getMetadataCollectorResource().collectODMMetadataForClinicalData(studyOID, formVersionOID, clinicalDataBeans, archived, permissionTagsString,meta);

        if (report.getClinicalDataMap() == null)
            return null;

        report.createOdmXml(true, getDataSource(), userBean, permissionTagsStringArray,meta,clinical,crossForm);
        LOGGER.debug(report.getXmlOutput().toString().trim());

        return report.getXmlOutput().toString().trim();
    }

    /**
     * This function checks to see whether the supplied subject identifier is a Study Subject OID or a Study Subject ID.
     * If a valid Study Subject OID or wildcard (*) is supplied, just return it.
     * If a Study Subject ID is supplied, a lookup is done to get the Study Subject OID and return that.
     */
    private String getStudySubjectOID(String subjectIdentifier, String studyOID) {
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(getDataSource());
        StudySubjectBean studySubject = studySubjectDAO.findByOid(subjectIdentifier);
        if (subjectIdentifier.equals("*") || (studySubject != null && studySubject.getOid() != null))
            return subjectIdentifier;
        else {
            StudyDAO studyDAO = new StudyDAO(getDataSource());
            StudyBean study = studyDAO.findByOid(studyOID);
            studySubject = studySubjectDAO.findByLabelAndStudy(subjectIdentifier, study);
            if (studySubject != null && studySubject.getOid() != null)
                return studySubject.getOid();
            else
                return subjectIdentifier;
        }
    }

    private String loadPermissionTagsString() {
        List<EventDefinitionCrfPermissionTag> tags = eventDefinitionCrfPermissionTagDao.findAll();

        List<String> tagsList = new ArrayList<>();
        for (EventDefinitionCrfPermissionTag tag : tags) {
            if (!tagsList.contains(tag.getPermissionTagId())) {
                tagsList.add(tag.getPermissionTagId());
            }
        }
        return tagsList.stream().collect(Collectors.joining("','", "'", "'"));

    }

    private String[] loadPermissionTagsStringArray() {
        List<EventDefinitionCrfPermissionTag> tags = eventDefinitionCrfPermissionTagDao.findAll();
        Set<String> tagsSet = new HashSet<>();
        for (EventDefinitionCrfPermissionTag tag : tags) {
            tagsSet.add(tag.getPermissionTagId());
        }
        return tagsSet.toArray(new String[0]);
    }

}

package org.akaza.openclinica.web.pform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.controller.openrosa.OpenRosaSubmissionController;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfVersionMediaDao;
import org.akaza.openclinica.dao.hibernate.RuleActionPropertyDao;
import org.akaza.openclinica.dao.hibernate.SCDItemMetadataDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.datamap.CrfVersionMedia;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.formlist.XForm;
import org.akaza.openclinica.web.pform.formlist.XFormList;
import org.akaza.openclinica.web.pform.manifest.Manifest;
import org.akaza.openclinica.web.pform.manifest.MediaFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

@Path("/openrosa")
@Component
public class OpenRosaServices {

    public static final String INPUT_USER_SOURCE = "userSource";
    public static final String INPUT_FIRST_NAME = "Participant";
    public static final String INPUT_LAST_NAME = "User";
    public static final String INPUT_EMAIL = "email";
    public static final String INPUT_INSTITUTION = "PFORM";
    public static final String INPUT_STUDY = "activeStudy";
    public static final String INPUT_ROLE = "role";
    public static final String INPUT_TYPE = "type";
    public static final String INPUT_DISPLAY_PWD = "displayPwd";
    public static final String INPUT_RUN_WEBSERVICES = "runWebServices";
    public static final String USER_ACCOUNT_NOTIFICATION = "notifyPassword";

    public static final String FORM_CONTEXT = "ecid";
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRosaServices.class);
    private DataSource dataSource;
    private CoreResources coreResources;
    private OpenRosaSubmissionController openRosaSubmissionController;
    private RuleActionPropertyDao ruleActionPropertyDao;
    private SCDItemMetadataDao scdItemMetadataDao;
    ParticipantPortalRegistrar participantPortalRegistrar;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    StudyDAO sdao;
    StudySubjectDAO studySubjectDao;

    /**
     * @api {get} /rest2/openrosa/:studyOID/formList Get Form List
     * @apiName getFormList
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOID Study Oid.
     * @apiGroup Form
     * @apiDescription Retrieves a listing of the available OpenClinica forms.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_SAMPLTE",
     *                  }
     * @apiSuccessExample {xml} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    <xforms xmlns="http://openrosa.org/xforms/xformsList">
     *                    <xform>
     *                    <formID>F_FIRSTFORM_1</formID>
     *                    <name>First Form</name>
     *                    <majorMinorVersion>1</majorMinorVersion>
     *                    <version>1</version>
     *                    <hash>8678370cd92814d4e3216d58d821403f</hash>
     *                    <downloadUrl>http://oc1.openclinica.com/OpenClinica-web/rest2/openrosa/S_SAMPLTE/formXml?
     *                    formId=F_FIRSTFORM_1</downloadUrl>
     *                    </xform>
     *                    <xform>
     *                    <formID>F_SECONDFORM_1</formID>
     *                    <name>Second Form</name>
     *                    <majorMinorVersion>1</majorMinorVersion>
     *                    <version>1</version>
     *                    <hash>7ee60d1c6516b730bbe9bdbd7cad942f</hash>
     *                    <downloadUrl>http://oc1.openclinica.com/OpenClinica-web/rest2/openrosa/S_SAMPLTE/formXml?
     *                    formId=F_SECONDFORM_1</downloadUrl>
     *                    </xform>
     *                    </xforms>
     */

    @GET
    @Path("/{studyOID}/formList")
    @Produces(MediaType.TEXT_XML)
    public String getFormList(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formID") String crfOID, @RequestHeader("Authorization") String authorization, @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(studyOID))
            return null;

        StudyDAO sdao = new StudyDAO(getDataSource());
        StudyBean study = sdao.findByOid(studyOID);

        CRFDAO cdao = new CRFDAO(getDataSource());
        Collection<CRFBean> crfs = cdao.findAll();

        CRFVersionDAO cVersionDao = new CRFVersionDAO(getDataSource());
        Collection<CRFVersionBean> crfVersions = cVersionDao.findAll();

        CrfVersionMediaDao mediaDao = (CrfVersionMediaDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionMediaDao");

        try {
            XFormList formList = new XFormList();
            for (CRFBean crf : crfs) {
                for (CRFVersionBean version : crfVersions) {
                    if (version.getCrfId() == crf.getId()) {
                        XForm form = new XForm(crf, version);
                        // TODO: Need to generate hash based on contents of
                        // XForm. Will be done in a later story.
                        // TODO: For now all XForms get a date based hash to
                        // trick Enketo into always downloading
                        // TODO: them.
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        form.setHash(DigestUtils.md5Hex(String.valueOf(cal.getTimeInMillis())));

                        String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
                        form.setDownloadURL(urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formId=" + version.getOid());

                        List<CrfVersionMedia> mediaList = mediaDao.findByCrfVersionId(version.getId());
                        if (mediaList != null && mediaList.size() > 0) {
                            form.setManifestURL(urlBase + "/rest2/openrosa/" + studyOID + "/manifest?formId=" + version.getOid());
                        }
                        formList.add(form);
                    }
                }
            }

            // Create the XML formList using a Castor mapping file.
            XMLContext xmlContext = new XMLContext();
            Mapping mapping = xmlContext.createMapping();
            mapping.loadMapping(getCoreResources().getURL("openRosaFormListMapping.xml"));
            xmlContext.addMapping(mapping);

            Marshaller marshaller = xmlContext.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.setWriter(writer);
            marshaller.marshal(formList);

            // Set response headers
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            Date currentDate = new Date();
            cal.setTime(currentDate);
            SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
            format.setCalendar(cal);
            response.setHeader("Content-Type", "text/xml; charset=UTF-8");
            response.setHeader("Date", format.format(currentDate));
            response.setHeader("X-OpenRosa-Version", "1.0");
            return writer.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return "<Error>" + e.getMessage() + "</Error>";
        }
    }

    /**
     * @api {get} /rest2/openrosa/:studyOID/manifest Get Form Manifest
     * @apiName getManifest
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOID Study Oid.
     * @apiGroup Form
     * @apiDescription Gets additional information on a particular Form, including links to associated media.
     */

    @GET
    @Path("/{studyOID}/manifest")
    @Produces(MediaType.TEXT_XML)
    public String getManifest(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formId") String crfOID, @RequestHeader("Authorization") String authorization, @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(studyOID))
            return null;

        CRFVersionDAO cVersionDao = new CRFVersionDAO(getDataSource());
        CrfVersionMediaDao mediaDao = (CrfVersionMediaDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionMediaDao");

        CRFVersionBean crfVersion = cVersionDao.findByOid(crfOID);
        List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
        Manifest manifest = new Manifest();

        List<CrfVersionMedia> mediaList = mediaDao.findByCrfVersionId(crfVersion.getId());
        if (mediaList != null && mediaList.size() > 0) {
            for (CrfVersionMedia media : mediaList) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];

                MediaFile mediaFile = new MediaFile();
                mediaFile.setFilename(media.getName());
                mediaFile.setHash(DigestUtils.md5Hex(String.valueOf(cal.getTimeInMillis())));
                mediaFile.setDownloadUrl(urlBase + "/rest2/openrosa/" + studyOID + "/downloadMedia?crfVersionMediaId=" + media.getCrfVersionMediaId());
                manifest.add(mediaFile);
            }
        }
        try {
            // Create the XML manifest using a Castor mapping file.
            XMLContext xmlContext = new XMLContext();
            Mapping mapping = xmlContext.createMapping();
            mapping.loadMapping(getCoreResources().getURL("openRosaManifestMapping.xml"));
            xmlContext.addMapping(mapping);

            Marshaller marshaller = xmlContext.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.setWriter(writer);
            marshaller.marshal(manifest);

            // Set response headers
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            Date currentDate = new Date();
            cal.setTime(currentDate);
            SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
            format.setCalendar(cal);
            response.setHeader("Content-Type", "text/xml; charset=UTF-8");
            response.setHeader("Date", format.format(currentDate));
            response.setHeader("X-OpenRosa-Version", "1.0");
            return writer.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return "<Error>" + e.getMessage() + "</Error>";
        }
    }

    /**
     * @api {get} /rest2/openrosa/:studyOID/formXml Get Form XML
     * @apiName getFormXml
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOID Study Oid.
     * @apiGroup Form
     * @apiDescription Downloads the contents of a form
     */

    @GET
    @Path("/{studyOID}/formXml")
    @Produces(MediaType.APPLICATION_XML)
    public String getFormXml(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formID") String crfOID, @RequestHeader("Authorization") String authorization) throws Exception {
        if (!mayProceedPreview(studyOID))
            return null;

        String xform = null;

        // get parameters
        String formId = request.getParameter("formId");
        if (formId == null) {
            return "<error>formID is null :(</error>";
        }

        try {
            CRFVersionDAO versionDAO = new CRFVersionDAO(dataSource);
            CRFVersionBean crfVersion = versionDAO.findByOid(formId);

            if (crfVersion.getXform() != null && !crfVersion.getXform().equals("")){
                xform = updateRepeatGroupsWithOrdinal(crfVersion.getXform());
            } else {

                OpenRosaXmlGenerator generator = new OpenRosaXmlGenerator(coreResources, dataSource, ruleActionPropertyDao);
                xform = generator.buildForm(formId);
            }
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        	System.out.println(ExceptionUtils.getStackTrace(e));
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return "<error>" + e.getMessage() + "</error>";
        }
        response.setHeader("Content-Type", "text/xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + crfOID + ".xml" + "\";");
        response.setContentType("text/xml; charset=utf-8");
        return xform;

    }

	/**
     * @api {get} /rest2/openrosa/:studyOID/downloadMedia Download media
     * @apiName getMediaFile
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOID Study Oid.
     * @apiGroup Form
     * @apiDescription Downloads media associated with a form, including images and video.
     */

    @GET
    @Path("/{studyOID}/downloadMedia")
    public Response getMediaFile(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("crfVersionMediaId") String crfVersionMediaId, @RequestHeader("Authorization") String authorization, @Context ServletContext context)
            throws Exception {
        if (!mayProceedPreview(studyOID))
            return null;

        CrfVersionMediaDao mediaDao = (CrfVersionMediaDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionMediaDao");
        CrfVersionMedia media = mediaDao.findById(Integer.valueOf(crfVersionMediaId));

        File image = new File(media.getPath() + media.getName());
        FileInputStream fis = new FileInputStream(image);
        StreamingOutput stream = new MediaStreamingOutput(fis);
        return Response.ok(stream).build();
    }

    /**
     * @api {post} /pages/api/v1/editform/:studyOid/submission Submit form data
     * @apiName doSubmission
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used to look up subject context information while processing submission.
     * @apiGroup Form
     * @apiDescription Submits the data from a completed form.
     */

    @POST
    @Path("/{studyOID}/submission")
    @Produces(MediaType.APPLICATION_XML)
    public Response doSubmission(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext,
            @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String context) {

        Response.ResponseBuilder builder = Response.noContent();

        ResponseEntity<String> responseEntity = openRosaSubmissionController.doSubmission(request, response, studyOID, context);
        if (responseEntity == null) {
            LOGGER.debug("Null response from OpenRosaSubmissionController.");
            return builder.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.CREATED)) {
            LOGGER.debug("Successful OpenRosa submission");
            builder.entity("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>");
            return builder.status(javax.ws.rs.core.Response.Status.CREATED).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.NOT_ACCEPTABLE)) {
            LOGGER.debug("Failed OpenRosa submission");
            return builder.status(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE).build();
        } else {
            LOGGER.debug("Failed OpenRosa submission with unhandled error");
            return builder.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{studyOID}/getSchedule")
    @Produces(MediaType.APPLICATION_XML)
    public String getSchedule(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext context,
            @PathParam("studyOID") String studyOID, @RequestHeader("Authorization") String authorization) throws Exception {

        String ssoid = request.getParameter("studySubjectOID");
        StudySubjectDAO ssdao = new StudySubjectDAO<String, ArrayList>(dataSource);
        StudySubjectBean ssBean = ssdao.findByOid(ssoid);
        if (!mayProceedSubmission(studyOID, ssBean))
            return null;

        HashMap<String, String> urlCache = (HashMap<String, String>) context.getAttribute("pformURLCache");
        context.getAttribute("subjectContextCache");
        if (ssoid == null) {
            return "<error>studySubjectOID is null :(</error>";
        }

        try {
            // Need to retrieve crf's for next event
            StudyEventDAO eventDAO = new StudyEventDAO(getDataSource());
            StudyEventBean nextEvent = (StudyEventBean) eventDAO.getNextScheduledEvent(ssoid);
            CRFVersionDAO versionDAO = new CRFVersionDAO(getDataSource());
            ArrayList<CRFVersionBean> crfs = versionDAO.findDefCRFVersionsByStudyEvent(nextEvent.getStudyEventDefinitionId());
            PFormCache cache = PFormCache.getInstance(context);
            for (CRFVersionBean crfVersion : crfs) {
                String enketoURL = cache.getPFormURL(studyOID, crfVersion.getOid());
                String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()),
                        String.valueOf(nextEvent.getSampleOrdinal()), crfVersion.getOid());
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
            LOGGER.debug(ExceptionUtils.getStackTrace(e));
            return "<error>" + e.getMessage() + "</error>";
        }

        response.setHeader("Content-Type", "text/xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"schedule.xml\";");
        response.setContentType("text/xml; charset=utf-8");
        return "<result>success</result>";

    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    public OpenRosaSubmissionController getOpenRosaSubmissionController() {
        return openRosaSubmissionController;
    }

    public void setOpenRosaSubmissionController(OpenRosaSubmissionController openRosaSubmissionController) {
        this.openRosaSubmissionController = openRosaSubmissionController;
    }

    public RuleActionPropertyDao getRuleActionPropertyDao() {
        return ruleActionPropertyDao;
    }

    public void setRuleActionPropertyDao(RuleActionPropertyDao ruleActionPropertyDao) {
        this.ruleActionPropertyDao = ruleActionPropertyDao;
    }

    public SCDItemMetadataDao getScdItemMetadataDao() {
        return scdItemMetadataDao;
    }

    public void setScdItemMetadataDao(SCDItemMetadataDao scdItemMetadataDao) {
        this.scdItemMetadataDao = scdItemMetadataDao;
    }

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private String updateRepeatGroupsWithOrdinal(String xform) throws Exception {
    	
    	NamedNodeMap attribs = fetchXformAttributes(xform);
    	InputStream is = new ByteArrayInputStream(xform.getBytes());
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setNamespaceAware(false);
    	Document doc = factory.newDocumentBuilder().parse(is);
        
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath(); 
        XPathExpression expr = null;
        expr = xpath.compile("/html/body/group/repeat");
        NodeList repeatNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (int k = 0; k < repeatNodes.getLength(); k++) {
        	Element groupElement = ((Element) repeatNodes.item(k).getParentNode());
        	String groupRef = groupElement.getAttribute("ref");
        	
            expr = xpath.compile("/html/head/model/instance[1]" + groupRef);
            Element group = (Element) expr.evaluate(doc, XPathConstants.NODE);
            Element ordinal = doc.createElement("OC.REPEAT_ORDINAL");
        	group.appendChild(ordinal);
            }

        TransformerFactory transformFactory = TransformerFactory.newInstance();
        Transformer transformer = transformFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String modifiedXform = writer.toString();
        modifiedXform = applyXformAttributes(modifiedXform, attribs);
        System.out.println("Finalized xform source: " + modifiedXform);
    	return modifiedXform;
	}

    private String applyXformAttributes(String xform, NamedNodeMap attribs) throws Exception {
    	String defaultNamespace = null;
        for (int i=0;i<attribs.getLength();i++) {
        	Attr attrib = (Attr) attribs.item(i);
        	if (attrib.getName().equals("xmlns")) defaultNamespace = attrib.getValue(); 
        }        
        String xformArray[] = xform.split("html",2);
        String modifiedXform = xformArray[0] + "html xmlns=\"" + defaultNamespace + "\" " + xformArray[1];
        return modifiedXform;
	}

	private NamedNodeMap fetchXformAttributes(String xform) throws Exception {
    	InputStream is = new ByteArrayInputStream(xform.getBytes());
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setNamespaceAware(true);
    	Document doc = factory.newDocumentBuilder().parse(is);
        Element html = doc.getDocumentElement();
        NamedNodeMap attribs = html.getAttributes();
		return attribs;
	}

    private boolean mayProceedSubmission(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString(); // ACTIVE ,
                                                                                                      // PENDING ,
                                                                                                      // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus
                + "  studySubjectStatus: " + ssBean.getStatus().getName());
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")
                && ssBean.getStatus() == Status.AVAILABLE) {
            accessPermission = true;
        }
        return accessPermission;
    }

    private boolean mayProceedPreview(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);

        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString(); // ACTIVE ,
                                                                                                      // PENDING ,
                                                                                                      // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
        if (participateStatus.equalsIgnoreCase("enabled")
                && (studyStatus.equalsIgnoreCase("available") || studyStatus.equalsIgnoreCase("pending") || studyStatus.equalsIgnoreCase("frozen") || studyStatus
                        .equalsIgnoreCase("locked"))
                && (pManageStatus.equalsIgnoreCase("ACTIVE") || pManageStatus.equalsIgnoreCase("PENDING") || pManageStatus.equalsIgnoreCase("INACTIVE"))) {
            accessPermission = true;
        }
        return accessPermission;
    }

    private class MediaStreamingOutput implements StreamingOutput {

        private InputStream in = null;

        public MediaStreamingOutput(InputStream in) {
            this.in = in;
        }

        @Override
        public void write(OutputStream out) throws IOException, WebApplicationException {
            // TODO Auto-generated method stub
            IOUtils.copy(in, out);
            in.close();
            out.close();
        }
    }
}

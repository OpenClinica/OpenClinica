package org.akaza.openclinica.web.pform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.controller.openrosa.OpenRosaSubmissionController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutMediaDao;
import org.akaza.openclinica.dao.hibernate.RuleActionPropertyDao;
import org.akaza.openclinica.dao.hibernate.SCDItemMetadataDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.formlist.XForm;
import org.akaza.openclinica.web.pform.formlist.XFormList;
import org.akaza.openclinica.web.pform.manifest.Manifest;
import org.akaza.openclinica.web.pform.manifest.MediaFile;
import org.akaza.openclinica.web.restful.ODMClinicaDataResource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

@Path("/openrosa")
@Component
public class OpenRosaServices {

    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    StudyDao studyDao;

    @Autowired
    StudyEventDao studyEventDao;

    @Autowired
    StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    StudySubjectDao ssDao;

    @Autowired
    CrfDao crfDao;

    @Autowired
    CrfVersionDao crfVersionDao;

    @Autowired
    FormLayoutDao formLayoutDao;

    @Autowired
    FormLayoutMediaDao formLayoutMediaDao;

    @Autowired
    XformParserHelper xformParserHelper;

    @Autowired
    ODMClinicaDataResource odmClinicalDataRestResource;

    @Autowired
    StudySubjectDao studySubjectDao;

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
    public static final String QUERY_SUFFIX = "form-queries.xml";
    public static final String NO_SUFFIX = "form.xml";
    public static final String QUERY_FLAVOR = "-query";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    public static final String NO_FLAVOR = "";
    public static final String SVG = ".svg";
    public static final String DASH = "-";

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
     *                    formID=F_FIRSTFORM_1</downloadUrl>
     *                    </xform>
     *                    <xform>
     *                    <formID>F_SECONDFORM_1</formID>
     *                    <name>Second Form</name>
     *                    <majorMinorVersion>1</majorMinorVersion>
     *                    <version>1</version>
     *                    <hash>7ee60d1c6516b730bbe9bdbd7cad942f</hash>
     *                    <downloadUrl>http://oc1.openclinica.com/OpenClinica-web/rest2/openrosa/S_SAMPLTE/formXml?
     *                    formID=F_SECONDFORM_1</downloadUrl>
     *                    </xform>
     *                    </xforms>
     */
    @GET
    @Path("/{studyOID}/formList")
    @Produces(MediaType.TEXT_XML)
    public String getFormList(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formID") String formID, @QueryParam(FORM_CONTEXT) String ecid, @RequestHeader("Authorization") String authorization,
            @Context ServletContext context) throws Exception {
        if (request.getMethod().equals("HEAD")) {
            response.setStatus(200);
            return null;
        }
        if (!mayProceedPreview(request, studyOID))
            return null;
        XFormList formList = null;

        try {
            if (StringUtils.isEmpty(formID)) {
                List<CrfBean> crfs = crfDao.findAll();
                List<FormLayout> formLayouts = formLayoutDao.findAll();
                formList = new XFormList();
                for (CrfBean crf : crfs) {
                    for (FormLayout formLayout : formLayouts) {
                        if (formLayout.getCrf().getCrfId() == crf.getCrfId()) {
                            XForm form = new XForm(crf, formLayout);
                            // TODO: Need to generate hash based on contents of
                            // XForm. Will be done in a later story.
                            // TODO: For now all XForms get a date based hash to
                            // trick Enketo into always downloading
                            // TODO: them.

                            String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
                            String downloadURL = urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formID=" + formLayout.getOcOid() + DASH
                                    + formLayout.getXform();
                            form.setDownloadURL(downloadURL);

                            List<FormLayoutMedia> mediaList = formLayoutMediaDao.findByFormLayoutIdForNoteTypeMedia(formLayout.getFormLayoutId());
                            if (mediaList != null && mediaList.size() > 0) {
                                String manifestURL = urlBase + "/rest2/openrosa/" + studyOID + "/manifest?ecid=" + ecid + "&formID=" + formLayout.getOcOid()
                                        + DASH + formLayout.getXform();
                                form.setManifestURL(manifestURL);
                            }

                            formList.add(form);
                        }
                    }
                }
            } else {
                request.setAttribute("requestSchema", "public");
                formList = getForm(request, response, studyOID, formID, authorization, context, ecid);
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
            String result = writer.toString();
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return "<Error>" + e.getMessage() + "</Error>";
        }

    }

    @GET
    @Path("/{studyOID}/form")
    @Produces(MediaType.TEXT_XML)
    public XFormList getForm(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formID") String formID, @RequestHeader("Authorization") String authorization, @Context ServletContext context,
            @QueryParam(FORM_CONTEXT) String ecid) throws Exception {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // print logback's internal status
        StatusPrinter.print(lc);
        if (!mayProceedPreview(request, studyOID))
            return null;
        if (formID == null) {
            LOGGER.error("<error> formID is null </error>");
        }
        String flavor = getQuerySet(formID);

        String formLayoutOid = getFormLayoutOid(formID);

        FormLayout formLayout = formLayoutDao.findByOcOID(formLayoutOid);
        if (formLayout == null) {
            LOGGER.error("<error> formID is incorrect </error>");
        }
        CrfBean crf = crfDao.findByCrfId(formLayout.getCrf().getCrfId());
        LOGGER.info("Schema name before setting it to: " + CoreResources.getRequestSchema());
        LOGGER.info("Check if study a site Oid - StudyOid is :" + studyOID);

        if (ecid != null) {
            HashMap<String, String> subjectContext = null;
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);
            String studySubjectOID = subjectContext.get("studySubjectOID");
            String formLayoutOID = subjectContext.get("formLayoutOID");
            String formLoadMode = subjectContext.get("formLoadMode");
            LOGGER.info("studySubjectOID from ecid: " + studySubjectOID);
            LOGGER.info("formLayoutOID from ecid: " + formLayoutOID);
            LOGGER.info("formLoadMode from ecid: " + formLoadMode);

        }

        StudyBean publicStudy = getPublicStudy(studyOID);
        CoreResources.setRequestSchema(publicStudy.getSchemaName());
        LOGGER.info("Schema name after setting it to : " + CoreResources.getRequestSchema());
        LOGGER.info("StudyOid is :" + studyOID);

        StudyBean study = getParentStudy(studyOID);

        String xformOutput = "";
        String attribute = "";
        int studyFilePath = 0;
        if (flavor.equals(SINGLE_ITEM_FLAVOR)) {
            attribute = formID.substring(formID.indexOf(SINGLE_ITEM_FLAVOR));
            xformOutput = (String) context.getAttribute(attribute);
        } else {
            studyFilePath = study.getFilePath();
            LOGGER.info("From Database original studyFilePath is" + studyFilePath);
            do {
                xformOutput = getXformOutput(studyOID, studyFilePath, crf.getOcOid(), formLayout.getOcOid(), flavor);
                studyFilePath--;
            } while (xformOutput.equals("") && studyFilePath > 0);
            LOGGER.info(" Final studyFilePath is" + studyFilePath);
        }
        XFormList formList = null;

        try {
            formList = new XFormList();
            XForm form = new XForm(crf, formLayout);

            LOGGER.info("FormID: " + formID);
            LOGGER.info("formLayoutOid: " + formLayoutOid);
            LOGGER.info("formLayout database Id: " + formLayout.getFormLayoutId());
            LOGGER.info("Crf  database Id: " + crf.getCrfId());
            // TODO: Need to generate hash based on contents of
            // XForm. Will be done in a later story.
            // TODO: For now all XForms get a date based hash to
            // trick Enketo into always downloading
            // TODO: them.
            // TODO Uncomment this before checking in
            if (StringUtils.isNotEmpty(xformOutput)) {
                if (formLayout.getExternalInstance() == null) {
                    checkForCllinicalDataInstanceInXform(formLayout, xformOutput);
                }
                form.setHash(DigestUtils.md5Hex(xformOutput));
            } else {
                LOGGER.error("<error> xform is null or xform file with name " + formLayoutOid + " not found in data directory. StudyPath is : " + studyFilePath
                        + "</error> ");
            }

            String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
            List<FormLayoutMedia> mediaList = formLayoutMediaDao.findByFormLayoutIdForNoteTypeMedia(formLayout.getFormLayoutId());
            String manifestURL = null;
            String downloadURL = null;
            if (flavor.equals(QUERY_FLAVOR)) {
                downloadURL = urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formID=" + formLayout.getOcOid() + DASH + formLayout.getXform()
                        + QUERY_FLAVOR;
                form.setDownloadURL(downloadURL);

                manifestURL = urlBase + "/rest2/openrosa/" + studyOID + "/manifest?ecid=" + ecid + "&formID=" + formLayout.getOcOid() + DASH
                        + formLayout.getXform() + QUERY_FLAVOR;
                form.setManifestURL(manifestURL);

                form.setFormID(formLayout.getOcOid() + DASH + formLayout.getXform() + QUERY_FLAVOR);
            } else if (flavor.equals(SINGLE_ITEM_FLAVOR)) {
                downloadURL = urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formID=" + formLayout.getOcOid() + DASH + formLayout.getXform() + attribute;
                form.setDownloadURL(downloadURL);
                manifestURL = urlBase + "/rest2/openrosa/" + studyOID + "/manifest?ecid=" + ecid + "&formID=" + formLayout.getOcOid() + DASH
                        + formLayout.getXform() + attribute;
                form.setManifestURL(manifestURL);
                form.setFormID(formLayout.getOcOid() + DASH + formLayout.getXform() + attribute);
            } else {
                downloadURL = urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formID=" + formLayout.getOcOid() + DASH + formLayout.getXform();
                form.setDownloadURL(downloadURL);
                manifestURL = urlBase + "/rest2/openrosa/" + studyOID + "/manifest?ecid=" + ecid + "&formID=" + formLayout.getOcOid() + DASH
                        + formLayout.getXform();
                form.setManifestURL(manifestURL);

            }
            formList.add(form);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            // return "<Error>" + e.getMessage() + "</Error>";
        }
        return formList;
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
            @QueryParam("formID") String formID, @QueryParam(FORM_CONTEXT) String ecid, @RequestHeader("Authorization") String authorization,
            @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        String formLayoutOid = getFormLayoutOid(formID);
        FormLayout formLayout = formLayoutDao.findByOcOID(formLayoutOid);

        Manifest manifest = new Manifest();

        List<FormLayoutMedia> mediaList = formLayoutMediaDao.findByFormLayoutIdForNoteTypeMedia(formLayout.getFormLayoutId());

        String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
        if (mediaList != null && mediaList.size() > 0) {
            for (FormLayoutMedia media : mediaList) {

                MediaFile mediaFile = new MediaFile();
                mediaFile.setFilename(media.getName());
                File image = new File(Utils.getFilePath() + media.getPath() + media.getName());
                mediaFile.setHash(DigestUtils.md5Hex(media.getName()) + Double.toString(image.length()));
                mediaFile.setDownloadUrl(
                        urlBase + "/rest2/openrosa/" + studyOID + "/downloadMedia?ecid=" + ecid + "&formLayoutMediaId=" + media.getFormLayoutMediaId());
                manifest.add(mediaFile);
            }
        }

        // Add user list
        MediaFile userList = new MediaFile();
        String userXml = getUserXml(context, studyOID, ecid);
        userList.setHash((DigestUtils.md5Hex(userXml)));
        userList.setFilename("users.xml");
        userList.setDownloadUrl(urlBase + "/rest2/openrosa/" + studyOID + "/downloadUsers?ecid=" + ecid);
        manifest.add(userList);

        MediaFile odmPayload = new MediaFile();
        String odm = getODMMetadata(request, studyOID, ecid, context, formID);
        odmPayload.setHash((DigestUtils.md5Hex(odm)));
        odmPayload.setFilename("clinicaldata.xml");
        odmPayload.setDownloadUrl(urlBase + "/rest2/openrosa/" + studyOID + "/" + ecid + "/" + formID);
        manifest.add(odmPayload);
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
            String result = writer.toString();
            System.out.println(result);
            return result;
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
    public Response getFormXml(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formID") String formID, @QueryParam(FORM_CONTEXT) String ecid, @RequestHeader("Authorization") String authorization,
            @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;

        String xform = null;
        ResponseBuilder builder = null;

        // get parameters
        if (formID == null) {
            LOGGER.error("<error>formID is null </error>");
            builder = Response.ok("<error>formID is null </error>");
            builder = builder.header("Content-Type", "text/xml");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        String flavor = getQuerySet(formID);
        String formLayoutOid = getFormLayoutOid(formID);
        FormLayout formLayout = formLayoutDao.findByOcOID(formLayoutOid);
        if (formLayout == null) {
            LOGGER.error("<error>formID is incorrect </error>");
            builder = Response.ok("<error>formID is incorrect or does not exist in database </error>");
            builder = builder.header("Content-Type", "text/xml");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        LOGGER.info("Schema name before setting it to: " + CoreResources.getRequestSchema());
        LOGGER.info("Check if study a site Oid - StudyOid is :" + studyOID);

        if (ecid != null) {
            HashMap<String, String> subjectContext = null;
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);
            String studySubjectOID = subjectContext.get("studySubjectOID");
            String formLayoutOID = subjectContext.get("formLayoutOID");
            String formLoadMode = subjectContext.get("formLoadMode");
            LOGGER.info("studySubjectOID from ecid: " + studySubjectOID);
            LOGGER.info("formLayoutOID from ecid: " + formLayoutOID);
            LOGGER.info("formLoadMode from ecid: " + formLoadMode);
        }
        StudyBean publicStudy = getPublicStudy(studyOID);
        CoreResources.setRequestSchema(publicStudy.getSchemaName());
        StudyBean study = getParentStudy(studyOID);
        LOGGER.info("Schema name after setting it to: " + CoreResources.getRequestSchema());

        CrfBean crf = formLayout.getCrf();
        int studyFilePath = 0;
        String xformOutput = "";
        if (flavor.equals(SINGLE_ITEM_FLAVOR)) {
            String attribute = formID.substring(formID.indexOf(SINGLE_ITEM_FLAVOR));
            xformOutput = (String) context.getAttribute(attribute);
        } else {
            studyFilePath = study.getFilePath();
            LOGGER.info("From Database original studyFilePath is" + studyFilePath);
            do {
                xformOutput = getXformOutput(studyOID, studyFilePath, crf.getOcOid(), formLayout.getOcOid(), flavor);
                studyFilePath--;
            } while (xformOutput.equals("") && studyFilePath > 0);
            LOGGER.info(" Final studyFilePath is" + studyFilePath);
        }
        try {
            if (StringUtils.isNotEmpty(xformOutput)) {
                builder = Response.ok(xformOutput);
                builder = builder.header("Content-Type", "text/xml");
                if (formLayout.getExternalInstance() == null) {
                    checkForCllinicalDataInstanceInXform(formLayout, xformOutput);
                }
                xform = xformOutput;
            } else {
                builder = Response.ok("<error> xform is null or xform file with name " + formLayoutOid + " not found in data directory. StudyPath is : "
                        + studyFilePath + "</error> ");
                builder = builder.header("Content-Type", "text/xml");
                LOGGER.error("<error> xform is null or xform file with name " + formLayoutOid + " not found in data directory. StudyPath is : " + studyFilePath
                        + "</error> ");
                return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            builder = Response.ok(e.getMessage());
            builder = builder.header("Content-Type", "text/xml");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        }
        response.setHeader("Content-Type", "text/xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + formID + ".xml" + "\";");
        response.setContentType("text/xml; charset=utf-8");
        return builder.status(Response.Status.OK).build();

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
            @QueryParam("formLayoutMediaId") String formLayoutMediaId, @QueryParam(FORM_CONTEXT) String ecid,
            @RequestHeader("Authorization") String authorization, @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        StudyBean publicStudy = getPublicStudy(studyOID);
        CoreResources.setRequestSchema(publicStudy.getSchemaName());

        FormLayoutMedia media = formLayoutMediaDao.findByFormLayoutMediaId(Integer.valueOf(formLayoutMediaId));

        File image = new File(Utils.getFilePath() + media.getPath() + media.getName());
        FileInputStream fis = new FileInputStream(image);
        StreamingOutput stream = new MediaStreamingOutput(fis);
        ResponseBuilder builder = Response.ok(stream);

        // Set content type, if known
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(media.getPath() + media.getName());
        if (type != null && !type.isEmpty()) {
            builder = builder.header("Content-Type", type);
        } else if (media.getName().endsWith(SVG)) {
            builder = builder.header("Content-Type", "image/svg+xml");
        }
        return builder.build();
    }

    /**
     * @api {get} /rest2/openrosa/:studyOID/downloadUsers Download users
     * @apiName getUserList
     * @apiPermission admin
     * @apiVersion 3.12.0
     * @apiParam {String} studyOID Study Oid.
     * @apiGroup Form
     * @apiDescription Downloads list of users for use with queries.
     */

    @GET
    @Path("/{studyOID}/downloadUsers")
    public Response getUserList(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam(FORM_CONTEXT) String ecid, @RequestHeader("Authorization") String authorization, @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        String userXml = getUserXml(context, studyOID, ecid);

        ResponseBuilder builder = Response.ok(userXml);
        builder = builder.header("Content-Type", "text/xml");
        return builder.build();
    }

    /**
     * @api {post} /rest2/openrosa/:studyOid/submission Submit form data
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
            @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String ecid) {

        ResponseBuilder builder = Response.noContent();

        ResponseEntity<String> responseEntity = openRosaSubmissionController.doSubmission(request, response, studyOID, ecid);
        if (responseEntity == null) {
            LOGGER.debug("Null response from OpenRosaSubmissionController.");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.CREATED)) {
            LOGGER.debug("Successful OpenRosa submission");
            builder.entity("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>");
            return builder.status(Response.Status.CREATED).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.NOT_ACCEPTABLE)) {
            LOGGER.debug("Failed OpenRosa submission");
            return builder.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {
            LOGGER.debug("Failed OpenRosa submission with unhandled error");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{studyOID}/fieldsubmission/complete")
    @Produces(MediaType.APPLICATION_XML)
    public Response doFieldSubmissionCompletePut(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @Context ServletContext servletContext, @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String ecid) throws Exception {
        return doFieldSubmissionComplete(request, response, servletContext, studyOID, ecid);
    }

    @POST
    @Path("/{studyOID}/fieldsubmission/complete")
    @Produces(MediaType.APPLICATION_XML)
    public Response doFieldSubmissionComplete(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @Context ServletContext servletContext, @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String ecid) throws Exception {

        ResponseBuilder builder = Response.noContent();
        ResponseEntity<String> responseEntity = openRosaSubmissionController.markComplete(request, response, studyOID, ecid);
        if (responseEntity == null) {
            LOGGER.debug("Null response from OpenRosaSubmissionController.");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.CREATED)) {
            LOGGER.debug("Successful OpenRosa submission");
            builder.entity("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>");
            return builder.status(Response.Status.CREATED).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.NOT_ACCEPTABLE)) {
            LOGGER.debug("Failed OpenRosa submission");
            return builder.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {
            LOGGER.debug("Failed OpenRosa submission with unhandled error");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{studyOID}/fieldsubmission")
    @Produces(MediaType.APPLICATION_XML)
    public Response doFieldSubmissionPut(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext,
            @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String context) {
        return doFieldSubmission(request, response, servletContext, studyOID, context);
    }

    @POST
    @Path("/{studyOID}/fieldsubmission")
    @Produces(MediaType.APPLICATION_XML)
    public Response doFieldSubmission(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext,
            @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String context) {

        ResponseBuilder builder = Response.noContent();

        ResponseEntity<String> responseEntity = openRosaSubmissionController.doFieldSubmission(request, response, studyOID, context);
        if (responseEntity == null) {
            LOGGER.debug("Null response from OpenRosaSubmissionController.");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.CREATED)) {
            LOGGER.debug("Successful OpenRosa submission");
            builder.entity("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>");
            return builder.status(Response.Status.CREATED).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.NOT_ACCEPTABLE)) {
            LOGGER.debug("Failed OpenRosa submission");
            return builder.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {
            LOGGER.debug("Failed OpenRosa submission with unhandled error");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{studyOID}/fieldsubmission")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response doFieldDeletion(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext,
            @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String context) {

        ResponseBuilder builder = Response.noContent();

        ResponseEntity<String> responseEntity = openRosaSubmissionController.doFieldDeletion(request, response, studyOID, context);
        if (responseEntity == null) {
            LOGGER.debug("Null response from OpenRosaSubmissionController.");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.CREATED)) {
            LOGGER.debug("Successful OpenRosa submission");
            builder.entity("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>");
            return builder.status(Response.Status.CREATED).build();
        } else if (responseEntity.getStatusCode().equals(org.springframework.http.HttpStatus.NOT_ACCEPTABLE)) {
            LOGGER.debug("Failed OpenRosa submission");
            return builder.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {
            LOGGER.debug("Failed OpenRosa submission with unhandled error");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @api {head} /rest2/openrosa/:studyOid/submission Submit form data
     * @apiName doSubmissionHead
     * @apiPermission admin
     * @apiVersion 3.11.0
     * @apiParam {String} studyOid Study Oid.
     * @apiGroup Form
     * @apiDescription Returns the HTTP headers for a form submission request.
     */

    @HEAD
    @Path("/{studyOID}/submission")
    @Produces(MediaType.APPLICATION_XML)
    public Response doSubmissionHead(@PathParam("studyOID") String studyOID) {

        ResponseBuilder builder = Response.noContent();
        String maxSubmissionSize = CoreResources.getField("pformMaxSubmissionSize");
        int maxSubmissionSizeInt = -1;

        try {
            maxSubmissionSizeInt = Integer.valueOf(maxSubmissionSize);
        } catch (Exception e) {
            logger.error("Unable to parse pformMaxSubmissionSize as an integer.");
        }

        // Build response headers
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Date currentDate = new Date();
        cal.setTime(currentDate);
        SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
        format.setCalendar(cal);
        builder = builder.header("Date", format.format(currentDate));
        builder = builder.header("X-OpenRosa-Version", "1.0");

        if (maxSubmissionSizeInt < 1) {
            logger.error("pformMaxSubmissionSize does not contain an integer value greater than 0.");
            return builder.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else {
            builder = builder.header("X-OpenRosa-Accept-Content-Length", maxSubmissionSizeInt);
            return builder.status(Response.Status.ACCEPTED).build();
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
            StudyEvent studyEvent = studyEventDao.findById(nextEvent.getId());
            for (CRFVersionBean crfVersion : crfs) {
                String enketoURL = cache.getPFormURL(studyOID, crfVersion.getOid(), studyEvent);
                String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()),
                        String.valueOf(nextEvent.getSampleOrdinal()), crfVersion.getOid(), null, studyOID, null);
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

    private StudyBean getStudyById(int id) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
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

    public String applyXformAttributes(String xform, NamedNodeMap attribs) throws Exception {
        String defaultNamespace = null;
        for (int i = 0; i < attribs.getLength(); i++) {
            Attr attrib = (Attr) attribs.item(i);
            if (attrib.getName().equals("xmlns"))
                defaultNamespace = attrib.getValue();
        }
        String xformArray[] = xform.split("html", 2);
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

    private String getUserXml(ServletContext context, String studyOID, String ecid) throws Exception {
        HashMap<String, String> subjectContext = null;
        PFormCache cache = PFormCache.getInstance(context);
        subjectContext = cache.getSubjectContext(ecid);
        int userAccountID = Integer.valueOf(subjectContext.get("userAccountID"));
        String studySubjectOID = subjectContext.get("studySubjectOID");
        Document doc = buildDocument();
        Element root = appendRootElement(doc);

        List<UserAccount> users = null;
        StudySubject ssBean = ssDao.findByOcOID(studySubjectOID);

        if (ssBean != null) {
            StudyBean publicStudy = getPublicStudy(ssBean.getStudy().getOc_oid());
            StudyBean parentPublicStudy = getParentPublicStudy(ssBean.getStudy().getOc_oid());
            CoreResources.setRequestSchema("public");
            UserAccount currentUser = userAccountDao.findByUserId(userAccountID);
            users = userAccountDao.findNonRootNonParticipateUsersByStudyId(publicStudy.getId(), parentPublicStudy.getId());
            CoreResources.setRequestSchema(publicStudy.getSchemaName());
            for (UserAccount userAccount : users) {
                Element item = doc.createElement("item");
                Element userName = doc.createElement("user_name");
                userName.appendChild(doc.createTextNode(userAccount.getUserName()));
                Element firstName = doc.createElement("first_name");
                firstName.appendChild(doc.createTextNode(userAccount.getFirstName()));
                Element lastName = doc.createElement("last_name");
                lastName.appendChild(doc.createTextNode(userAccount.getLastName()));
                item.appendChild(userName);
                item.appendChild(firstName);
                item.appendChild(lastName);
                if (userAccount.getUserName().equals(currentUser.getUserName())) {
                    item.setAttribute("current", "true");
                }
                root.appendChild(item);
            }
        }
        String writer = getWriter(doc);
        return writer;
    }

    private boolean mayProceedSubmission(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getParentPublicStudy(studyOid);
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

    private boolean mayProceedPreview(HttpServletRequest request, String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getParentPublicStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        request.setAttribute("requestSchema", study.getSchemaName());
        // PENDING ,
        // INACTIVE
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.debug("   studyStatus: " + studyStatus);
        if ((studyStatus.equalsIgnoreCase("available") || studyStatus.equalsIgnoreCase("pending") || studyStatus.equalsIgnoreCase("frozen")
                || studyStatus.equalsIgnoreCase("locked"))) {
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

    private StudyBean getPublicStudy(String studyOid) {
        String schema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(studyOid);
        CoreResources.setRequestSchema(schema);
        return studyBean;
    }

    private StudyBean getParentPublicStudy(String studyOid) {
        StudyBean resultBean = null;
        String schema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            resultBean = study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            resultBean = parentStudy;
        }
        CoreResources.setRequestSchema(schema);
        return resultBean;
    }

    private String getFormLayoutOid(String formID) {
        if (formID.endsWith(QUERY_FLAVOR)) {
            formID = formID.substring(0, formID.length() - QUERY_FLAVOR.length());
        } else if (formID.contains(SINGLE_ITEM_FLAVOR)) {
            formID = formID.substring(0, formID.indexOf(SINGLE_ITEM_FLAVOR));
        }
        formID = formID.substring(0, formID.lastIndexOf(DASH));
        return formID;
    }

    private String getQuerySet(String formID) {
        if (formID.endsWith(QUERY_FLAVOR)) {
            return QUERY_FLAVOR;
        } else if (formID.contains(SINGLE_ITEM_FLAVOR)) {
            return SINGLE_ITEM_FLAVOR;
        } else {
            return NO_FLAVOR;
        }
    }

    public XformParserHelper getXformParserHelper() {
        return xformParserHelper;
    }

    public void setXformParserHelper(XformParserHelper xformParserHelper) {
        this.xformParserHelper = xformParserHelper;
    }

    @GET
    @Path("/{studyOID}/{ecid}/{formID}")
    @Produces(MediaType.TEXT_XML)
    public String getODMMetadata(@Context HttpServletRequest request, @PathParam("studyOID") String studyOID, @PathParam("ecid") String ecid,
            @Context ServletContext context, @PathParam("formID") String formID) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        HashMap<String, String> subjectContext = null;
        PFormCache cache = PFormCache.getInstance(context);
        subjectContext = cache.getSubjectContext(ecid);
        String studySubjectOID = subjectContext.get("studySubjectOID");
        String formLayoutOID = subjectContext.get("formLayoutOID");
        String formLoadMode = subjectContext.get("formLoadMode");
        String externalInstance = getFormLayout(formLayoutOID).getExternalInstance();
        String flavor = getQuerySet(formID);

        if (studySubjectOID == null || externalInstance.equals("FALSE") || formLoadMode.equals("view") || flavor.equals(SINGLE_ITEM_FLAVOR)) {
            Document doc = buildDocument();
            appendRootElement(doc);
            String writer = getWriter(doc);
            return writer;
        }
        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOID);
        studyOID = studySubject.getStudy().getOc_oid();

        String studyEventDefinitionID = subjectContext.get("studyEventDefinitionID");
        String studyEventRepeat = subjectContext.get("studyEventOrdinal");
        StudyEventDefinition sed = studyEventDefinitionDao.findById(Integer.valueOf(studyEventDefinitionID));
        String phraseToLookForInOdm = "<StudyEventData StudyEventOID=\"" + sed.getOc_oid() + "\" StudyEventRepeatKey=\"" + studyEventRepeat + "\"";
        String clinicalData = "yes";
        String userAccountID = subjectContext.get("userAccountID");
        String result = odmClinicalDataRestResource.getODMMetadata(studyOID, "*", studySubjectOID, "*", "no", "no", request, userAccountID, clinicalData, "no");
        result = result.replaceAll("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"", "");
        result = result.replaceAll("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"", "xmlns:OpenClinica=\"http://openclinica.com/odm\"");
        int index = result.indexOf(phraseToLookForInOdm);
        if (index == -1) {
            logger.error(" Current Study Event location can't be found in ODM ");
            return null;
        }

        String part1 = result.substring(0, index + phraseToLookForInOdm.length());
        String part2 = result.substring(index + phraseToLookForInOdm.length() + 1);
        String output = part1 + " OpenClinica:Current=\"Yes\" " + part2;

        return output;
    }

    private Document buildDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        return doc;
    }

    private Element appendRootElement(Document doc) {
        Element root = doc.createElement("root");
        doc.appendChild(root);
        return root;
    }

    private String getWriter(Document doc) throws TransformerException {
        DOMSource dom = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(dom, result);
        return writer.toString();
    }

    private FormLayout getFormLayout(String formLayoutOID) {
        return formLayoutDao.findByOcOID(formLayoutOID);

    }

    private void checkForCllinicalDataInstanceInXform(FormLayout formLayout, String xformOutput) {
        if (xformOutput.contains("<instance id=\"clinicaldata\" src=\"jr://file/clinicaldata.xml\"/>")) {
            formLayout.setExternalInstance("TRUE");
        } else {
            formLayout.setExternalInstance("FALSE");
        }
        formLayoutDao.saveOrUpdate(formLayout);
    }

    private String getXformOutput(String studyOID, int studyFilePath, String crfOID, String formLayoutOID, String flavor) throws IOException {
        String xformOutput = "";
        String directoryPath = Utils.getFilePath() + Utils.getCrfMediaPath(studyOID, studyFilePath, crfOID, formLayoutOID);
        File dir = new File(directoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if ((flavor.equals(QUERY_FLAVOR) && child.getName().endsWith(QUERY_SUFFIX))
                        || (flavor.equals(NO_FLAVOR) && child.getName().endsWith(NO_SUFFIX))) {
                    xformOutput = new String(Files.readAllBytes(Paths.get(child.getPath())));
                    break;
                }
            }
        }
        return xformOutput;
    }
}

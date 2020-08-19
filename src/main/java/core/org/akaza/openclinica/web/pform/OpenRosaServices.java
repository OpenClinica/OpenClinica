package core.org.akaza.openclinica.web.pform;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.Utils;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.core.util.EncryptionUtil;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.domain.xform.XformParser;
import core.org.akaza.openclinica.domain.xform.XformParserHelper;
import core.org.akaza.openclinica.domain.xform.dto.*;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import core.org.akaza.openclinica.web.pform.formlist.XForm;
import core.org.akaza.openclinica.web.pform.formlist.XFormList;
import core.org.akaza.openclinica.web.pform.manifest.Manifest;
import core.org.akaza.openclinica.web.pform.manifest.MediaFile;
import core.org.akaza.openclinica.web.restful.ODMClinicaDataResource;
import org.akaza.openclinica.controller.openrosa.OpenRosaSubmissionController;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tika.Tika;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static core.org.akaza.openclinica.service.crfdata.xform.EnketoAPI.DATABASE_ID_ENCRYPTION_KEY_PROPERTY;

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

    @Autowired
    OpenRosaService openRosaService;

    @Autowired
    OpenRosaXMLUtil openRosaXMLUtil;

    @Autowired
    XformParser xformParser;

    public static final String QUERY_SUFFIX = "form-queries.xml";
    public static final String PARTICIPATE_SUFFIX = "form-participate.xml";
    public static final String NO_SUFFIX = "form.xml";
    public static final String QUERY_FLAVOR = "-query";
    public static final String PARTICIPATE_FLAVOR = "-participate";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    public static final String NO_FLAVOR = "";
    public static final String SVG = ".svg";
    public static final String DASH = "-";
    public static final String CONTACTDATA = "contactdata";
    public static final String UNSCHECDULED = "unscheduled";
    public static final String CURRENT_EVENT = "CURRENT_EVENT";
    public static final String OPEN_BRACKET = "[";
    public static final String CLOSE_BRACKET = "]";

    public static final String FORM_CONTEXT = "ecid";
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRosaServices.class);
    private DataSource dataSource;
    private CoreResources coreResources;
    private OpenRosaSubmissionController openRosaSubmissionController;
    private RuleActionPropertyDao ruleActionPropertyDao;
    private SCDItemMetadataDao scdItemMetadataDao;
    ParticipantPortalRegistrar participantPortalRegistrar;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


    static ConcurrentMap<String, String> studyUserMap = new ConcurrentHashMap<>();
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

        Study publicStudy = getPublicStudy(studyOID);
        CoreResources.setRequestSchema(publicStudy.getSchemaName());
        LOGGER.info("Schema name after setting it to : " + CoreResources.getRequestSchema());
        LOGGER.info("StudyOid is :" + studyOID);

        Study study = getParentStudy(studyOID);

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
                xformOutput = xformParserHelper.getXformOutput(studyOID, studyFilePath, crf.getOcOid(), formLayout.getOcOid(), flavor);
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
                if (formLayout.getExternalInstance() == null || formLayout.getExternalInstance().equals("")) {
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
            }else if (flavor.equals(PARTICIPATE_FLAVOR)) {
                    downloadURL = urlBase + "/rest2/openrosa/" + studyOID + "/formXml?formID=" + formLayout.getOcOid() + DASH + formLayout.getXform()
                            + PARTICIPATE_FLAVOR;
                    form.setDownloadURL(downloadURL);
                    manifestURL = urlBase + "/rest2/openrosa/" + studyOID + "/manifest?ecid=" + ecid + "&formID=" + formLayout.getOcOid() + DASH
                            + formLayout.getXform() + PARTICIPATE_FLAVOR;
                    form.setManifestURL(manifestURL);
                    form.setFormID(formLayout.getOcOid() + DASH + formLayout.getXform() + PARTICIPATE_FLAVOR);
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
                              @QueryParam("formID") String formID, @DefaultValue("") @QueryParam(FORM_CONTEXT) String ecid, @RequestHeader("Authorization") String authorization,
                              @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        String formLayoutOid = getFormLayoutOid(formID);
        FormLayout formLayout = formLayoutDao.findByOcOID(formLayoutOid);
        String flavor = getQuerySet(formID);

        Manifest manifest = new Manifest();

        List<FormLayoutMedia> mediaList = formLayoutMediaDao.findByFormLayoutIdForNoteTypeMedia(formLayout.getFormLayoutId());

        String urlBase = getCoreResources().getDataInfo().getProperty("sysURL").split("/MainMenu")[0];
        String databaseIdEncryptionKey = getCoreResources().getDataInfo().getProperty(DATABASE_ID_ENCRYPTION_KEY_PROPERTY);
        if (mediaList != null && mediaList.size() > 0) {
            for (FormLayoutMedia media : mediaList) {

                MediaFile mediaFile = new MediaFile();
                mediaFile.setFilename(media.getName());
                File image = new File(Utils.getFilePath() + media.getPath() + media.getName());
                mediaFile.setHash(DigestUtils.md5Hex(media.getName()) + Double.toString(image.length()));
                String formLayoutMediaId = String.valueOf(media.getFormLayoutMediaId());
                // Encrypt the form layout media id so we don't expose database ids to users
                String encryptedFormLayoutMediaId = EncryptionUtil.encryptValue(formLayoutMediaId, databaseIdEncryptionKey);
                mediaFile.setDownloadUrl(
                        urlBase + "/rest2/openrosa/" + studyOID + "/downloadMediaEncrypted?ecid=" + ecid + "&formLayoutMediaId=" + encryptedFormLayoutMediaId);
                manifest.add(mediaFile);
            }
        }
        if (!flavor.equals(PARTICIPATE_FLAVOR)) {
            // Add user list
            MediaFile userList = new MediaFile();
            // String userXml = getUserXml(context, studyOID, ecid);
            StudyAndSiteEnvUuid studyAndSiteUuids = getStudyAndSiteUuids(context, studyOID, ecid);
            String userXml = openRosaService.getUserListFromUserService(studyAndSiteUuids);
            if (userXml != null)
                studyUserMap.put(studyAndSiteUuids.studyEnvUuid + studyAndSiteUuids.siteEnvUuid, userXml);
            userList.setHash((DigestUtils.md5Hex(userXml == null ? "" : userXml)));
            userList.setFilename("users.xml");
            userList.setDownloadUrl(urlBase + "/rest2/openrosa/" + studyOID + "/downloadUsers?ecid=" + ecid);
            manifest.add(userList);
        }
        MediaFile odmPayload = new MediaFile();

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
            LOGGER.info(result);
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
        Study publicStudy = getPublicStudy(studyOID);
        CoreResources.setRequestSchema(publicStudy.getSchemaName());
        Study study = getParentStudy(studyOID);
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
                xformOutput = xformParserHelper.getXformOutput(studyOID, studyFilePath, crf.getOcOid(), formLayout.getOcOid(), flavor);
                studyFilePath--;
            } while (xformOutput.equals("") && studyFilePath > 0);
            LOGGER.info(" Final studyFilePath is" + studyFilePath);
        }
        try {
            if (StringUtils.isNotEmpty(xformOutput)) {
                builder = Response.ok(xformOutput);
                builder = builder.header("Content-Type", "text/xml");
                if (formLayout.getExternalInstance() == null || formLayout.getExternalInstance().equals("")) {
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
     * @api {get} /rest2/openrosa/:studyOID/downloadMediaEncrypted Download media
     * @apiName getMediaFile
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOID Study Oid.
     * @apiGroup Form
     * @apiDescription Downloads media associated with a form, including images and video.
     */

    @GET
    @Path("/{studyOID}/downloadMediaEncrypted")
    public Response getMediaFile(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formLayoutMediaId") String formLayoutMediaId, @QueryParam(FORM_CONTEXT) String ecid,
            @RequestHeader("Authorization") String authorization, @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        Study publicStudy = getPublicStudy(studyOID);
        CoreResources.setRequestSchema(publicStudy.getSchemaName());
        // Decrypt the encrypted form layout media id
        String encryptionKey = getCoreResources().getDataInfo().getProperty(DATABASE_ID_ENCRYPTION_KEY_PROPERTY);
        String decryptedFormLayoutMediaId = EncryptionUtil.decryptValue(formLayoutMediaId, encryptionKey);
        FormLayoutMedia media = formLayoutMediaDao.findByFormLayoutMediaId(Integer.valueOf(decryptedFormLayoutMediaId));

        File mediaFile = new File(Utils.getFilePath() + media.getPath() + media.getName());
        FileInputStream fis = new FileInputStream(mediaFile);
        StreamingOutput stream = new MediaStreamingOutput(fis);
        ResponseBuilder builder = Response.ok(stream);

        // Set content type, if known
        Tika tika = new Tika();
        String mimeType = tika.detect(mediaFile);
        if (mimeType != null && !mimeType.isEmpty()) {
            builder = builder.header("Content-Type", mimeType);
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
            LOGGER.error("Failed OpenRosa submission");
            return builder.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {
            LOGGER.error("Failed OpenRosa submission with unhandled error");
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

    private Study getStudy(String oid) {
        Study studyBean = (Study) studyDao.findByOcOID(oid);
        return studyBean;
    }

    private Study getStudyById(int id) {
        Study study = (Study) studyDao.findByPK(id);
        return study;
    }

    private Study getParentStudy(String studyOid) {
        Study study = getStudy(studyOid);
        if (!study.isSite()) {
            return study;
        } else {
            Study parentStudy = study.getStudy();
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

    private StudyAndSiteEnvUuid getStudyAndSiteUuids(ServletContext context, String studyOID, String ecid) throws Exception {
        String studyEnvUuid = null;
        StudySubject ssBean = null;
        StudyAndSiteEnvUuid studyAndSiteEnvUuid = new StudyAndSiteEnvUuid();

        HashMap<String, String> subjectContext = null;
        PFormCache cache = PFormCache.getInstance(context);
        if (StringUtils.isNotEmpty(ecid) && !ecid.equalsIgnoreCase("null")) {
            subjectContext = cache.getSubjectContext(ecid);
            int userAccountID = Integer.valueOf(subjectContext.get("userAccountID"));
            String studySubjectOID = subjectContext.get("studySubjectOID");
            ssBean = ssDao.findByOcOID(studySubjectOID);
            studyAndSiteEnvUuid.currentUser = userAccountDao.findByUserId(userAccountID);
        }
        Study publicStudy = null;
        Study parentPublicStudy = null;
        if (ssBean != null) {
            publicStudy = getPublicStudy(ssBean.getStudy().getOc_oid());
            parentPublicStudy = getParentPublicStudy(ssBean.getStudy().getOc_oid());
        } else {
            publicStudy = getPublicStudy(studyOID);
            parentPublicStudy = getParentPublicStudy(studyOID);
        }
        if (!publicStudy.isSite()) {
            studyAndSiteEnvUuid.studyEnvUuid = publicStudy.getStudyEnvUuid();
        } else {
            studyAndSiteEnvUuid.studyEnvUuid = parentPublicStudy.getStudyEnvUuid();
            studyAndSiteEnvUuid.siteEnvUuid = publicStudy.getStudyEnvSiteUuid();
        }
         return studyAndSiteEnvUuid;
    }
    private String getUserXml(ServletContext context, String studyOID, String ecid) throws Exception {
        HashMap<String, String> subjectContext = null;
        PFormCache cache = PFormCache.getInstance(context);
        subjectContext = cache.getSubjectContext(ecid);
        int userAccountID = Integer.valueOf(subjectContext.get("userAccountID"));
        String studySubjectOID = subjectContext.get("studySubjectOID");
        Document doc = openRosaXMLUtil.buildDocument();
        Element root = openRosaXMLUtil.appendRootElement(doc);

        List<UserAccount> users = null;
        StudySubject ssBean = ssDao.findByOcOID(studySubjectOID);

        if (ssBean != null) {
            Study publicStudy = getPublicStudy(ssBean.getStudy().getOc_oid());
            Study parentPublicStudy = getParentPublicStudy(ssBean.getStudy().getOc_oid());
            CoreResources.setRequestSchemaToPublic();
            UserAccount currentUser = userAccountDao.findByUserId(userAccountID);
            users = userAccountDao.findNonRootNonParticipateUsersByStudyId(publicStudy.getStudyId(), parentPublicStudy.getStudyId());
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
        String writer = openRosaXMLUtil.getWriter(doc);
        return writer;
    }

    private boolean mayProceedSubmission(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        Study study = getParentPublicStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
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
        Study study = getParentPublicStudy(studyOid);
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

    private Study getPublicStudy(String studyOid) {
        String schema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchemaToPublic();
        Study studyBean = (Study) studyDao.findByOcOID(studyOid);
        CoreResources.setRequestSchema(schema);
        return studyBean;
    }

    private Study getParentPublicStudy(String studyOid) {
        Study resultBean = null;
        String schema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchemaToPublic();
        Study study = getStudy(studyOid);
        if (!study.isSite()) {
            resultBean = study;
        } else {
            Study parentStudy = (Study) studyDao.findByPK(study.getStudy().getStudyId());
            resultBean = parentStudy;
        }
        CoreResources.setRequestSchema(schema);
        return resultBean;
    }

    private String getFormLayoutOid(String formID) {
        if (formID.endsWith(QUERY_FLAVOR)) {
            formID = formID.substring(0, formID.length() - QUERY_FLAVOR.length());
        } else if (formID.endsWith(PARTICIPATE_FLAVOR)) {
            formID = formID.substring(0, formID.length() - PARTICIPATE_FLAVOR.length());
        } else if (formID.contains(SINGLE_ITEM_FLAVOR)) {
            formID = formID.substring(0, formID.indexOf(SINGLE_ITEM_FLAVOR));
        }
        int indexOfDash=formID.lastIndexOf(DASH);

        if(indexOfDash!=-1){
            formID = formID.substring(0, indexOfDash);
        }
        return formID;
    }

    private String getQuerySet(String formID) {
        if (formID.endsWith(QUERY_FLAVOR)) {
            return QUERY_FLAVOR;
        } else if (formID.endsWith(PARTICIPATE_FLAVOR)) {
            return PARTICIPATE_FLAVOR;
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
                                 @Context ServletContext context, @PathParam("formID") String formID,@DefaultValue("true") @QueryParam(value="includeClinicalData") boolean includeClinicalData) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        HashMap<String, String> subjectContext = null;
        PFormCache cache = PFormCache.getInstance(context);
        subjectContext = cache.getSubjectContext(ecid);
        if (subjectContext == null) {
            Document doc = openRosaXMLUtil.buildDocument();
            openRosaXMLUtil.appendRootElement(doc);
            String writer = openRosaXMLUtil.getWriter(doc);
            return writer;
        }
        String studySubjectOID = subjectContext.get("studySubjectOID");
        String formLayoutOID = subjectContext.get("formLayoutOID");
        String formLoadMode = subjectContext.get("formLoadMode");
        String externalInstance = getFormLayout(formLayoutOID).getExternalInstance();
        String flavor = getQuerySet(formID);

        if (studySubjectOID == null || externalInstance.equals("FALSE") || flavor.equals(SINGLE_ITEM_FLAVOR)) {
            Document doc = openRosaXMLUtil.buildDocument();
            openRosaXMLUtil.appendRootElement(doc);
            String writer = openRosaXMLUtil.getWriter(doc);
            return writer;
        }
        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOID);
        studyOID = studySubject.getStudy().getOc_oid();

        String studyEventDefinitionID = subjectContext.get("studyEventDefinitionID");
        String studyEventRepeat = subjectContext.get("studyEventOrdinal");
        StudyEventDefinition sed = studyEventDefinitionDao.findById(Integer.valueOf(studyEventDefinitionID));
        String phraseToLookForInOdm ="";
        if(sed.getType().equals(UNSCHECDULED) && !sed.isRepeating()) {
            phraseToLookForInOdm = "<StudyEventData StudyEventOID=\"" + sed.getOc_oid() + "\"";
        }else{
            phraseToLookForInOdm = "<StudyEventData StudyEventOID=\"" + sed.getOc_oid() + "\" StudyEventRepeatKey=\"" + studyEventRepeat + "\"";
        }
        String userAccountID = subjectContext.get("userAccountID");

        String crossformReferenceEvents=getCrossFormReferenceEvents(formLayoutOID,studyOID,flavor,sed,studyEventRepeat);

        String result = null;
        // first time call
        if(!includeClinicalData) {
            result = odmClinicalDataRestResource.getODMMetadata(studyOID, "*", studySubjectOID, !StringUtils.isEmpty(crossformReferenceEvents) ?crossformReferenceEvents:"*", "no", "no", request, userAccountID, "no","no", "no","no","yes", "yes");
        }else {
            // 2nd time call
            result = odmClinicalDataRestResource.getODMMetadata(studyOID, "*", studySubjectOID, !StringUtils.isEmpty(crossformReferenceEvents)?crossformReferenceEvents:"*", "no", "no", request, userAccountID, "yes","no", "yes","no","yes", "n");

        }

        result = result.replaceAll("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"", "");
        result = result.replaceAll("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"", "xmlns:OpenClinica=\"http://openclinica.com/odm\"");
        int index = result.indexOf(phraseToLookForInOdm);
        if (index == -1) {
            logger.error(" Current Study Event location can't be found in ODM ");
            return result;
        }

        String part1 = result.substring(0, index + phraseToLookForInOdm.length());
        String part2 = result.substring(index + phraseToLookForInOdm.length() + 1);
        String output = part1 + " OpenClinica:Current=\"Yes\" " + part2;

        return output;
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

    @GET
    @Path("/{studyOID}/downloadUsers")
    
    public Response getUserList(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
                                @QueryParam(FORM_CONTEXT) String ecid, @RequestHeader("Authorization") String authorization, @Context ServletContext context) throws Exception {
        if (!mayProceedPreview(request, studyOID))
            return null;
        StudyAndSiteEnvUuid studyAndSiteEnvUuids = getStudyAndSiteUuids(context, studyOID, ecid);

        // if there is no entry in the hashmap due to User service not available or any other reasons, get the list from RT database
        String userXml = studyUserMap.computeIfAbsent(studyAndSiteEnvUuids.studyEnvUuid + studyAndSiteEnvUuids.siteEnvUuid, key -> {
            try {
                return getUserXml(context, studyOID, ecid);
            } catch (Exception e) {
                logger.error("Fetching user list failed from Runtime query", e);
            }
            return "";
        });
        studyUserMap.remove(studyAndSiteEnvUuids.studyEnvUuid + studyAndSiteEnvUuids.siteEnvUuid);
        ResponseBuilder builder = Response.ok(userXml);
        builder = builder.header("Content-Type", "text/xml");
        return builder.build();
    }

    public List<Bind> getBinds(FormLayout formLayout, String flavor, String studyOid) throws Exception {
        return xformParserHelper.getBinds(formLayout, flavor, studyOid);
    }
    public boolean isFormContainsContactData(List<Bind> binds) {
        boolean formContainsContactData = false;
        for (Bind bind : binds) {
            if (bind.getOcExternal() != null && bind.getOcExternal().startsWith(CONTACTDATA)) {
                formContainsContactData = true;
                break;
            }
        }
        return formContainsContactData;
    }

    private String getCrossFormReferenceEvents(String formLayoutOID, String studyOID, String flavor, StudyEventDefinition sed, String studyEventRepeat) throws Exception {
        Html html = xformParserHelper.getHtml(getFormLayout(formLayoutOID), flavor, studyOID);
        String crossformReferenceEvents = html.getHead().getCrossform_references();

        if (crossformReferenceEvents == null || crossformReferenceEvents.equals("")) {
            return null;
        }

        crossformReferenceEvents = crossformReferenceEvents.toUpperCase();
        List<String> eventOids = Arrays.asList(crossformReferenceEvents.split("\\s*,\\s*"));

        for (String eventOid : eventOids) {
            if (!eventOid.equals(CURRENT_EVENT)) {
                StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByColumnName(eventOid.toUpperCase(), "oc_oid");
                if (studyEventDefinition == null) {
                    LOGGER.error("Invalid event oid in crossform_references: {}",crossformReferenceEvents);
                    return null;
                }
            }
        }

        if (crossformReferenceEvents.contains(CURRENT_EVENT)) {
            boolean currentEventIsInList = false;
            for (String eventOid : eventOids) {
                if (eventOid.equals(sed.getOc_oid())) {
                    currentEventIsInList = true;
                    break;
                }
            }

            if (currentEventIsInList) {
                crossformReferenceEvents = crossformReferenceEvents.replaceAll("(?i)"+CURRENT_EVENT, "");
            } else {
                crossformReferenceEvents = crossformReferenceEvents.replaceAll("(?i)"+CURRENT_EVENT, sed.getOc_oid() + OPEN_BRACKET + studyEventRepeat + CLOSE_BRACKET);
            }
        }

        return crossformReferenceEvents;
    }

}

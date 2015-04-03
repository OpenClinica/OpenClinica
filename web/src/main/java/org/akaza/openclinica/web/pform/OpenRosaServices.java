package org.akaza.openclinica.web.pform;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleActionPropertyDao;
import org.akaza.openclinica.dao.hibernate.SCDItemMetadataDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.web.pform.formlist.XFormList;
import org.akaza.openclinica.web.pform.formlist.XForm;
import org.akaza.openclinica.web.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pmanage.Study;
import org.akaza.openclinica.web.pmanage.Submission;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;
import org.akaza.openclinica.service.PformSubmissionService;

@Path("/openrosa")
@Component
public class OpenRosaServices {

    public static final String FORM_CONTEXT = "ecid";
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRosaServices.class);
    private DataSource dataSource;
    private CoreResources coreResources;
    private PformSubmissionService PformSubmissionService;
    private RuleActionPropertyDao ruleActionPropertyDao;
    private SCDItemMetadataDao scdItemMetadataDao;
    ParticipantPortalRegistrar participantPortalRegistrar;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    StudyDAO sdao;

    @GET
    @Path("/{studyOID}/formList")
    @Produces(MediaType.TEXT_XML)
    public String getFormList(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("studyOID") String studyOID,
            @QueryParam("formID") String crfOID, @RequestHeader("Authorization") String authorization) throws Exception {
        if (!mayProceedPreview(studyOID))
            return null;

        StudyDAO sdao = new StudyDAO(getDataSource());
        StudyBean study = sdao.findByOid(studyOID);

        CRFDAO cdao = new CRFDAO(getDataSource());
        Collection<CRFBean> crfs = cdao.findAll();

        CRFVersionDAO cVersionDao = new CRFVersionDAO(getDataSource());
        Collection<CRFVersionBean> crfVersions = cVersionDao.findAll();

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
            OpenRosaXmlGenerator generator = new OpenRosaXmlGenerator(coreResources, dataSource, ruleActionPropertyDao);
            xform = generator.buildForm(formId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return "<error>" + e.getMessage() + "</error>";
        }

        response.setHeader("Content-Type", "text/xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + crfOID + ".xml" + "\";");
        response.setContentType("text/xml; charset=utf-8");
        return xform;

    }

    @POST
    @Path("/{studyOID}/submission")
    @Produces(MediaType.APPLICATION_XML)
    public String doSubmission(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext,
            @PathParam("studyOID") String studyOID, @QueryParam(FORM_CONTEXT) String context) {

        String output = null;
        String studySubjectOid = null;
        Integer studyEventDefnId = null;
        Integer studyEventOrdinal = null;
        String crfVersionOID = null;
        CRFVersionDAO crfvdao = new CRFVersionDAO(dataSource);

        try {

            if (ServletFileUpload.isMultipartContent(request)) {
                LOGGER.warn("WARNING: This prototype doesn't support multipart content.");
            }

            PFormCache cache = PFormCache.getInstance(servletContext);
            HashMap<String, String> userContext = cache.getSubjectContext(context);
            StudySubjectDAO ssdao = new StudySubjectDAO<String, ArrayList>(dataSource);
            StudySubjectBean ssBean = ssdao.findByOid(userContext.get("studySubjectOID"));

            if (!mayProceedSubmission(studyOID, ssBean))
                return null;

            studySubjectOid = userContext.get("studySubjectOID");
            studyEventDefnId = Integer.valueOf(userContext.get("studyEventDefinitionID"));
            studyEventOrdinal = Integer.valueOf(userContext.get("studyEventOrdinal"));
            crfVersionOID = userContext.get("crfVersionOID");

            StringWriter writer = new StringWriter();
            String body = IOUtils.toString(request.getInputStream(), "UTF-8");

            body = body.substring(body.indexOf("<F_"));
            int length = body.indexOf(" ");
            body = body.replace(body.substring(body.indexOf("<meta>"), body.indexOf("</meta>") + 7), "");
            body = body.substring(0, body.indexOf("</F_") + length + 2);
            body = "<instance>" + body + "</instance>";

            Errors errors = getPformSubmissionService().saveProcess(body, studySubjectOid, studyEventDefnId, studyEventOrdinal);

            // Set response headers
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            Date currentDate = new Date();
            cal.setTime(currentDate);
            SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
            format.setCalendar(cal);
            response.setHeader("Date", format.format(currentDate));
            response.setHeader("X-OpenRosa-Version", "1.0");
            response.setContentType("text/xml; charset=utf-8");
            response.setStatus(201);

            if (!errors.hasErrors()) {

                output = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
                LOGGER.debug("Successful OpenRosa submission");

            } else {
                LOGGER.error("Failed OpenRosa submission");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.flushBuffer();
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return "<Error>" + e.getMessage() + "</Error>";
        }

        try {
            // Notify Participate of successful form submission.
            String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/submission";
            Submission submission = new Submission();
            Study pManageStudy = new Study();
            pManageStudy.setInstanceUrl(CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOID);
            pManageStudy.setStudyOid(studyOID);
            submission.setStudy(pManageStudy);
            submission.setStudy_event_def_id(studyEventDefnId);
            submission.setStudy_event_def_ordinal(studyEventOrdinal);
            submission.setCrf_version_id(crfvdao.findByOid(crfVersionOID).getId());

            RestTemplate rest = new RestTemplate();
            String result = rest.postForObject(pManageUrl, submission, String.class);
            LOGGER.debug("Notified Participate of CRF submission with a result of: " + result);
        } catch (Exception e) {
            LOGGER.error("Unable to notify Participate of successful CRF submission.");
            LOGGER.error(e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return output;
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

    public PformSubmissionService getPformSubmissionService() {
        return PformSubmissionService;
    }

    public void setPformSubmissionService(PformSubmissionService pformSubmissionService) {
        PformSubmissionService = pformSubmissionService;
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

}

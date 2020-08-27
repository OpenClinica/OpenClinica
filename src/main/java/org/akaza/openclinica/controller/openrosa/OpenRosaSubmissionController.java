package org.akaza.openclinica.controller.openrosa;

import com.openclinica.kafka.dto.FormChangeDTO;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.rule.FileProperties;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.ocobserver.StudyEventChangeDetails;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.service.auth.TokenService;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
import core.org.akaza.openclinica.web.pform.PFormCache;
import com.openclinica.kafka.KafkaService;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.service.FormCacheServiceImpl;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping(value = "/openrosa")
public class OpenRosaSubmissionController {

    @Autowired
    private ServletContext context;

    @Autowired
    private OpenRosaSubmissionService openRosaSubmissionService;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private StudyParameterValueDao studyParameterValueDao;

    @Autowired
    private UserAccountDao userAccountDao;

    @Autowired
    private StudyEventDao studyEventDao;

    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Autowired
    private FormLayoutDao formLayoutDao;

    @Autowired
    private CrfDao crfDao;

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    private CompletionStatusDao completionStatusDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemDataDao itemDataDao;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RandomizationService randomizationService;

    @Autowired
    private StudyBuildService studyBuildService;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    FormCacheServiceImpl formCacheService;

    @Autowired
    private UtilService utilService;
    @Autowired
    TokenService tokenService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String FORM_CONTEXT = "ecid";
    private final String COMMON = "common";

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

    @RequestMapping(value = "/{studyOID}/submission", method = RequestMethod.POST)
    public ResponseEntity<String> doSubmission(HttpServletRequest request, HttpServletResponse response, @PathVariable("studyOID") String studyOID,
            @RequestParam(FORM_CONTEXT) String ecid) {

        logger.info("Processing xform submission.");
        HashMap<String, String> subjectContext = null;
        Locale locale = LocaleResolver.getLocale(request);

        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        Study parentStudy = studyDao.findByOcOID(studyOID);
        request.setAttribute("requestSchema", parentStudy.getSchemaName());
        Study study = studyDao.findByOcOID(studyOID);
        String requestBody = null;

        HashMap<String, String> map = new HashMap();
        ArrayList<HashMap> listOfUploadFilePaths = new ArrayList();

        try {
            // Verify Study is allowed to submit
            if (!mayProceed(study)) {
                logger.info("Submissions to the study not allowed.  Aborting submission.");
                return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
            }
            if (ServletFileUpload.isMultipartContent(request)) {
                String dir = getAttachedFilePath(studyOID);
                FileProperties fileProperties = new FileProperties();
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setFileSizeMax(fileProperties.getFileSizeMax());
                List<FileItem> items = upload.parseRequest(request);
                for (FileItem item : items) {
                    if (item.getContentType() != null && !item.getFieldName().equals("xml_submission_file")) {
                        if (!new File(dir).exists())
                            new File(dir).mkdirs();

                        File file = processUploadedFile(item, dir);
                        map.put(item.getFieldName(), file.getPath());

                    } else if (item.getFieldName().equals("xml_submission_file")) {
                        requestBody = item.getString("UTF-8");
                    }
                }
                listOfUploadFilePaths.add(map);
            } else {
                requestBody = IOUtils.toString(request.getInputStream(), "UTF-8");
            }

            // Load user context from ecid
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);
            UserAccount userAccount = getUserAccount(subjectContext);

            // Execute save as Hibernate transaction to avoid partial imports
            openRosaSubmissionService.processRequest(study, subjectContext, requestBody, errors, locale, listOfUploadFilePaths,
                    SubmissionContainer.FieldRequestTypeEnum.FORM_FIELD,userAccount);
        } catch (Exception e) {
            logger.error("Exception while processing xform submission.");
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            if (errors.hasErrors()) {
                // Send a failure response
                logger.info("Submission caused internal error.  Sending error response.");
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (!errors.hasErrors()) {
            // JsonLog submission with Participate
            logger.info("Completed xform submission. Sending successful response");
            String responseMessage = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
            return new ResponseEntity<String>(responseMessage, HttpStatus.CREATED);
        } else {
            logger.info("Submission contained errors. Sending error response");
            return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    // @RequestMapping(value = "/{studyOID}/fieldsubmission/complete", method = RequestMethod.POST)
    public ResponseEntity<String> markComplete(HttpServletRequest request, HttpServletResponse response, @PathVariable("studyOID") String studyOID,
            @RequestParam(FORM_CONTEXT) String ecid) throws Exception {

        HashMap<String, String> subjectContext = null;
        PFormCache cache = PFormCache.getInstance(context);
        subjectContext = cache.getSubjectContext(ecid);
        int studyEventDefinitionID = Integer.valueOf(subjectContext.get("studyEventDefinitionID"));
        int userAccountID = Integer.valueOf(subjectContext.get("userAccountID"));
        String studySubjectOID = subjectContext.get("studySubjectOID");
        String formLayoutOID = subjectContext.get("formLayoutOID");
        int studyEventOrdinal = Integer.valueOf(subjectContext.get("studyEventOrdinal"));
        String accessToken = subjectContext.get("accessToken");
        request.getSession().setAttribute("accessToken",accessToken);

        UserAccount userAccount = userAccountDao.findById(userAccountID);
        Study parentStudy = studyDao.findByOcOID(studyOID);
        request.setAttribute("requestSchema", parentStudy.getSchemaName());

        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOID);
        Study study = studyDao.findByOcOID(studyOID);
        StudyEventDefinition sed = studyEventDefinitionDao.findById(studyEventDefinitionID);
        FormLayout formLayout = formLayoutDao.findByOcOID(formLayoutOID);
        CrfVersion crfVersion = crfVersionDao.findAllByCrfId(formLayout.getCrf().getCrfId()).get(0);
        StudyEvent studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinalTransactional(sed.getOc_oid(), studyEventOrdinal,
                studySubject.getStudySubjectId());
        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(),
                formLayout.getFormLayoutId());

        if (eventCrf == null) {
            eventCrf = createEventCrf(formLayout, studyEvent, studySubject, userAccount);
            List<Item> items = itemDao.findAllByCrfVersion(crfVersion.getCrfVersionId());
            createItemData(items.get(0), "", eventCrf, userAccount);
        }

        if (!eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
            eventCrf.setWorkflowStatus(EventCrfWorkflowStatusEnum.COMPLETED);
            eventCrf.setUserAccount(userAccount);
            eventCrf.setUpdateId(userAccount.getUserId());
            eventCrf.setDateCompleted(new Date());
            eventCrf.setDateUpdated(new Date());
            eventCrfDao.saveOrUpdate(eventCrf);

            if (!formCacheService.expireAndRemoveForm(ecid)){
                FormChangeDTO formChangeDTO = kafkaService.constructFormChangeDTO(eventCrf);
                formChangeDTO.setFormWorkflowStatus(EventCrfWorkflowStatusEnum.COMPLETED.getDisplayValue());
                kafkaService.sendFormChangeMessage(formChangeDTO);
            }
            //TODO Re-enable randomize.
          checkRandomization(subjectContext, studyOID, studySubjectOID);
        }

        updateStudyEventStatus(study,studySubject,sed,studyEvent,userAccount);

        String responseMessage = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
        return new ResponseEntity<String>(responseMessage, HttpStatus.CREATED);
    }


    private void checkRandomization(Map<String, String> subjectContext, String studyOid, String subjectOid) throws Exception {
        Study parentPublicStudy = studyBuildService.getParentPublicStudy(studyOid);
        String accessToken = subjectContext.get("accessToken");
        randomizationService.processRandomization(parentPublicStudy, accessToken, subjectOid);
    }


    /**
     * @api {post} /pages/api/v2/editform/:studyOid/fieldsubmission Submit form data
     * @apiName doSubmission
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used to look up subject context information while processing submission.
     * @apiGroup Form
     * @apiDescription Submits the data from a completed form.
     */

    @RequestMapping(value = "/{studyOID}/fieldsubmission", method = RequestMethod.POST)
    public ResponseEntity<String> doFieldSubmission(HttpServletRequest request, HttpServletResponse response, @PathVariable("studyOID") String studyOID,
            @RequestParam(FORM_CONTEXT) String ecid) {

        long millis = System.currentTimeMillis();

        logger.info("Processing xform field submission.");
        HashMap<String, String> subjectContext = null;
        Locale locale = LocaleResolver.getLocale(request);

        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        Study publicStudy = studyDao.findByOcOID(studyOID);
        request.setAttribute("requestSchema", publicStudy.getSchemaName());
        Study study = studyDao.findByOcOID(studyOID);
        String requestBody = null;
        String instanceId = null;
        HashMap<String, String> map = new HashMap();
        ArrayList<HashMap> listOfUploadFilePaths = new ArrayList();

        try {
            // Verify Study is allowed to submit
            if (!mayProceed(study)) {
                logger.info("Field Submissions to the study not allowed.  Aborting field submission.");
                return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
            }
            String dir = getAttachedFilePath(studyOID);
            FileProperties fileProperties = new FileProperties();
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(fileProperties.getFileSizeMax());
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.getFieldName().equals("instance_id")) {
                    instanceId = item.getString();
                } else if (item.getFieldName().equals("xml_submission_fragment_file")) {
                    requestBody = item.getString("UTF-8");
                } else if (item.getContentType() != null) {
                    if (!new File(dir).exists())
                        new File(dir).mkdirs();

                    File file = processUploadedFile(item, dir);
                    map.put(item.getFieldName(), file.getPath());

                }
            }
            listOfUploadFilePaths.add(map);
            if (instanceId == null) {
                logger.info("Field Submissions to the study not allowed without a valid instanceId.  Aborting field submission.");
                return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
            }

            // Load user context from ecid
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);
            UserAccount userAccount = getUserAccount(subjectContext);

            if (!formCacheService.resetExpiration(ecid)){
                logger.info("Updating expiration failed, re-adding entry in expiration map for: " + ecid);
                int studyEventId = Integer.parseInt(subjectContext.get("studyEventID"));
                StudyEvent studyEvent = studyEventDao.findById(studyEventId);
                String formLayoutOid = subjectContext.get("formLayoutOID");
                FormLayout formLayout = formLayoutDao.findByOcOID(formLayoutOid);
                //TODO Public study is not "current study" so we're not getting the site if the user is at the site level. Do we actually need the site OID for this DTO? It is getting set as null for now.
                formCacheService.addNewFormToFormCache(ecid, publicStudy, studyEvent, formLayout);
            }

            // Execute save as Hibernate transaction to avoid partial imports
            openRosaSubmissionService.processFieldSubmissionRequest(study, subjectContext, instanceId, requestBody, errors, locale, listOfUploadFilePaths,
                    SubmissionContainer.FieldRequestTypeEnum.FORM_FIELD,userAccount);
        } catch (Exception e) {
            logger.error("Exception while processing xform submission.");
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            if (!errors.hasErrors()) {
                // Send a failure response
                logger.info("Submission caused internal error.  Sending error response.");
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (!errors.hasErrors()) {
            // JsonLog submission with Participate
            logger.info("Completed xform field submission. Sending successful response");
            String responseMessage = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
            long endMillis = System.currentTimeMillis();
            logger.info("Total time *********** " + (endMillis - millis));
            return new ResponseEntity<String>(responseMessage, HttpStatus.CREATED);
        } else {
            logger.info("Field Submission contained errors. Sending error response");
            return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * @api {post} /pages/api/v2/editform/:studyOid/fieldsubmission Submit form data
     * @apiName doSubmission
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used to look up subject context information while processing submission.
     * @apiGroup Form
     * @apiDescription Submits the data from a completed form.
     */

    @RequestMapping(value = "/{studyOID}/fieldsubmission", method = RequestMethod.DELETE)
    public ResponseEntity<String> doFieldDeletion(HttpServletRequest request, HttpServletResponse response, @PathVariable("studyOID") String studyOID,
            @RequestParam(FORM_CONTEXT) String ecid) {

        logger.info("Processing xform field deletion.");
        HashMap<String, String> subjectContext = null;
        Locale locale = LocaleResolver.getLocale(request);

        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        Study publicStudy = studyDao.findByOcOID(studyOID);
        request.setAttribute("requestSchema", publicStudy.getSchemaName());
        Study study = studyDao.findByOcOID(studyOID);
        String requestBody = null;
        String instanceId = null;
        HashMap<String, String> map = new HashMap();
        ArrayList<HashMap> listOfUploadFilePaths = new ArrayList();

        try {
            // Verify Study is allowed to submit
            if (!mayProceed(study)) {
                logger.info("Field Deletions to the study not allowed.  Aborting field submission.");
                return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
            }

            String dir = getAttachedFilePath(studyOID);
            FileProperties fileProperties = new FileProperties();
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(fileProperties.getFileSizeMax());
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.getFieldName().equals("instance_id")) {
                    instanceId = item.getString();
                } else if (item.getFieldName().equals("xml_submission_fragment_file")) {
                    requestBody = item.getString("UTF-8");
                } else if (item.getContentType() != null) {
                    if (!new File(dir).exists())
                        new File(dir).mkdirs();

                    File file = processUploadedFile(item, dir);
                    map.put(item.getFieldName(), file.getPath());

                }
            }
            listOfUploadFilePaths.add(map);
            if (instanceId == null) {
                logger.info("Field Submissions to the study not allowed without a valid instanceId.  Aborting field submission.");
                return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
            }

            // Load user context from ecid
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);
            UserAccount userAccount = getUserAccount(subjectContext);

            // Execute save as Hibernate transaction to avoid partial imports
            openRosaSubmissionService.processFieldSubmissionRequest(study, subjectContext, instanceId, requestBody, errors, locale, listOfUploadFilePaths,
                    SubmissionContainer.FieldRequestTypeEnum.DELETE_FIELD,userAccount);

        } catch (Exception e) {
            logger.error("Exception while processing xform submission.");
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            if (!errors.hasErrors()) {
                // Send a failure response
                logger.info("Submission caused internal error.  Sending error response.");
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (!errors.hasErrors()) {
            // JsonLog submission with Participate
            logger.info("Completed xform field submission. Sending successful response");
            String responseMessage = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
            return new ResponseEntity<String>(responseMessage, HttpStatus.CREATED);
        } else {
            logger.info("Field Submission contained errors. Sending error response");
            return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private boolean isParticipantSubmission(HashMap<String, String> subjectContext) {
        boolean isParticipant = true;
        String userAccountId = subjectContext.get("userAccountID");
        if (StringUtils.isNotEmpty(userAccountId)) {
            UserAccount user = userAccountDao.findByUserId(Integer.valueOf(userAccountId));
            // All Participants have a '.' in the user name. Non-participant user creation does not allow a '.' in the
            // user name.
            if (user != null && !user.getUserName().contains("."))
                return false;
        }
        return isParticipant;
    }

    private Study getParentStudy(Study childStudy) {
        Study parentStudy = childStudy.getStudy();
        if (parentStudy != null && parentStudy.getStudyId() > 0)
            return parentStudy;
        else
            return childStudy;
    }

    private boolean mayProceed(Study study) throws Exception {
        return mayProceed(study, null);
    }

    private boolean mayProceed(Study childStudy, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        Study study = getParentStudy(childStudy);

        // available, pending, frozen, or locked
        String studyStatus = study.getStatus().getName().toString();

        if (ssBean == null) {
            logger.debug("studyStatus: " + studyStatus);
            if (study.getStatus() == Status.AVAILABLE || study.getStatus() == Status.FROZEN)
                accessPermission = true;
        } else {
            logger.info("studyStatus: " + studyStatus + "  studySubjectStatus: " + ssBean.getStatus().getName());
            if (study.getStatus() == Status.AVAILABLE && ssBean.getStatus() == core.org.akaza.openclinica.bean.core.Status.AVAILABLE)
                accessPermission = true;
        }
        return accessPermission;
    }

    public static String getAttachedFilePath(String studyOid) {
        String attachedFilePath = CoreResources.getField("attached_file_location");
        if (attachedFilePath == null || attachedFilePath.length() <= 0) {
            attachedFilePath = CoreResources.getField("filePath") + "attached_files" + File.separator + studyOid + File.separator;
        } else {
            attachedFilePath += studyOid + File.separator;
        }
        return attachedFilePath;
    }

    private File processUploadedFile(FileItem item, String dirToSaveUploadedFileIn) {
        dirToSaveUploadedFileIn = dirToSaveUploadedFileIn == null ? System.getProperty("java.io.tmpdir") : dirToSaveUploadedFileIn;
        String fileName = item.getName();
        // Some browsers IE 6,7 getName returns the whole path
        int startIndex = fileName.lastIndexOf('\\');
        if (startIndex != -1) {
            fileName = fileName.substring(startIndex + 1, fileName.length());
        }

        File uploadedFile = new File(dirToSaveUploadedFileIn + File.separator + fileName);
        /*
         * try {
         * uploadedFile = new UploadFileServlet().new OCFileRename().rename(uploadedFile, item.getInputStream());
         * } catch (IOException e) {
         * throw new OpenClinicaSystemException(e.getMessage());
         * }
         */
        try {
            item.write(uploadedFile);
        } catch (Exception e) {
            throw new OpenClinicaSystemException(e.getMessage());
        }
        return uploadedFile;
    }

    private EventCrf createEventCrf(FormLayout formLayout, StudyEvent studyEvent, StudySubject studySubject, UserAccount user) {
        EventCrf eventCrf = new EventCrf();
        CrfVersion crfVersion = crfVersionDao.findAllByCrfId(formLayout.getCrf().getCrfId()).get(0);
        Date currentDate = new Date();
        eventCrf.setAnnotations("");
        eventCrf.setDateCreated(currentDate);
        eventCrf.setCrfVersion(crfVersion);
        eventCrf.setFormLayout(formLayout);
        eventCrf.setInterviewerName("");
        eventCrf.setDateInterviewed(null);
        eventCrf.setUserAccount(user);
        eventCrf.setWorkflowStatus(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY);
        eventCrf.setCompletionStatus(completionStatusDao.findByCompletionStatusId(1));// setCompletionStatusId(1);
        eventCrf.setStudySubject(studySubject);
        eventCrf.setStudyEvent(studyEvent);
        eventCrf.setValidateString("");
        eventCrf.setValidatorAnnotations("");
        eventCrf.setUpdateId(user.getUserId());
        eventCrf.setDateUpdated(new Date());
        eventCrf.setValidatorId(0);
        eventCrf.setSdvUpdateId(0);
        eventCrf.setSdvStatus(null);
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        logger.debug("*********CREATED EVENT CRF");
        return eventCrf;
    }

    protected void createItemData(Item item, String itemValue, EventCrf eventCrf, UserAccount userAccount) {
        ItemData itemData = new ItemData();
        itemData.setItem(item);
        itemData.setEventCrf(eventCrf);
        itemData.setValue(itemValue);
        itemData.setDateCreated(new Date());
        itemData.setOrdinal(1);
        itemData.setUserAccount(userAccount);
        itemData.setDeleted(false);
        itemDataDao.saveOrUpdate(itemData);
    }

    private void persistStudyEvent(StudyEvent studyEvent,boolean statusChanged) {
        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(statusChanged, false);
        StudyEventContainer container = new StudyEventContainer(studyEvent, changeDetails);
        studyEventDao.saveOrUpdateTransactional(container);
    }

    private UserAccount getUserAccount(HashMap<String, String> subjectContext) {
        String userAccountId = subjectContext.get("userAccountID");
        if (StringUtils.isNotEmpty(userAccountId)) {
            UserAccount user = userAccountDao.findByUserId(Integer.valueOf(userAccountId));
            return user;
        }
        return null;

    }

    public void updateStudyEventStatus(Study study, StudySubject studySubject, StudyEventDefinition sed, StudyEvent studyEvent, UserAccount userAccount) {
        List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubject.getOcOid());
        List<EventDefinitionCrf> eventDefinitionCrfs = eventDefinitionCrfDao.findAvailableByStudyEventDefStudy(sed.getStudyEventDefinitionId(),
                study.getStudyId());
        studyEvent.setUpdateId(userAccount.getUserId());
        studyEvent.setDateUpdated(new Date());
        int countOfEventCrfsInEDC = getCountOfEventCrfsInEDC(eventCrfs, eventDefinitionCrfs);

        boolean allFormsComplete = true;
        for (EventCrf evCrf : eventCrfs) {
            if (!evCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                allFormsComplete = false;
                break;
            }
        }
        Boolean isEventWorkflowStatusUpDated = false;
        if ((allFormsComplete && countOfEventCrfsInEDC == eventDefinitionCrfs.size()) || sed.getType().equals(COMMON)) {
            if (!studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.COMPLETED);
                isEventWorkflowStatusUpDated = true;
                if(studyEvent.isCurrentlySigned())
                    studyEvent.setSigned(false);
                persistStudyEvent(studyEvent, true);
            }
        } else {
            if (!studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED)) {
                studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED);
                isEventWorkflowStatusUpDated = true;
                if(studyEvent.isCurrentlySigned())
                    studyEvent.setSigned(false);
                persistStudyEvent(studyEvent, true);
            }
        }
        if(isEventWorkflowStatusUpDated && studySubject.getStatus().isSigned()){
            studySubject.setStatus(Status.AVAILABLE);
            studySubject.setDateUpdated(new Date());
            studySubject.setUpdateId(userAccount.getUserId());
            studySubjectDao.saveOrUpdate(studySubject);
        }
    }

    private int getCountOfEventCrfsInEDC(List<EventCrf> eventCrfs ,List <EventDefinitionCrf> eventDefinitionCrfs){
        int count=0;
        for (EventCrf evCrf : eventCrfs) {
            if (evCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)
                    || evCrf.isCurrentlyRemoved()
                    || evCrf.isCurrentlyArchived()
                    ){
                for (EventDefinitionCrf eventDefinitionCrf : eventDefinitionCrfs) {
                    if (eventDefinitionCrf.getCrf().getCrfId() == evCrf.getFormLayout().getCrf().getCrfId()) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

}

package core.org.akaza.openclinica.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.login.RestReponseDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.controller.dto.*;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.akaza.openclinica.service.ImportService;
import org.akaza.openclinica.service.UserService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service( "StudyEventService" )
public class StudyEventServiceImpl implements StudyEventService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;

    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    ImportService importService;

    @Autowired
    StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    StudyEventDao studyEventDao;
    @Autowired
    EventCrfDao eventCrfDao;
    @Autowired
    StudySubjectDao studySubjectDao;
    @Autowired
    EventDefinitionCrfDao eventDefinitionCrfDao;

    @Autowired
    UserService userService;
    @Autowired
    EventDefinitionCRFDAO eventDefinitionCRFDAO;

    @Autowired
    private CSVService csvService;

    @Autowired
    private StudyBuildService studyBuildService;
    @Autowired
    @Qualifier("studyEventJDBCDao")
    private StudyEventDAO studyEventDAO;

    private RestfulServiceHelper restfulServiceHelper;


    public static final String DASH = "-";
    public static final String UNDERSCORE = "_";
    public static final String FAILED = "Failed";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String CREATED = "Created";
    public static final String UPDATED = "Updated";

    /**
     * DAOs
     */
    private StudySubjectDAO msStudySubjectDAO = null;
    private StudyEventDefinitionDAO sedDao = null;
    private final String COMMON = "common";
    public static final String UNSCHEDULED = "unscheduled";
    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    public static final String SCHEDULE_EVENT = "_Schedule Event";


    public RestReponseDTO scheduleStudyEvent(HttpServletRequest request, String studyOID, String siteOID, String studyEventOID, String participantId, String startDate, String endDate) {

        RestReponseDTO responseDTO = new RestReponseDTO();
        ArrayList<String> errors = new ArrayList<String>();
        responseDTO.setErrors(errors);

        String message = "";

        String studySubjectKey = participantId;
        String errMsg = null;
        Study currentStudy = null;
        Study currentSiteStudy = null;
        StudyEventDefinitionBean definition = null;
        StudySubjectBean studySubject = null;
        String startDateStr;
        String endDateStr;

        try {
            /**
             *  basic check 1: startDate and end Date
             */
            Date startDt = null;
            Date endDt = null;

            if (startDate == null) {
                errMsg = "start date is missing";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NO_START_DATE);
            } else {
                startDt = this.getRestfulServiceHelper().getDateTime(startDate);
            }

            // endDate is optional
            if (endDate != null && endDate.trim().length() > 7) {
                endDt = this.getRestfulServiceHelper().getDateTime(endDate);
            }

            if (startDt == null) {
                errMsg = "start date can't be parsed as a valid date,please enter in correct date format";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_START_DATE);
            } else if (endDt != null) {

                if (endDt.before(startDt)) {
                    errMsg = "The endDate can not before startDate";
                    logger.info(errMsg);
                    throw new OpenClinicaException(errMsg, ErrorConstants.ERR_END_DATE_BEFORE_START_DATE);
                }
            }

            /**
             * Step 2: check study
             */

            // check study first
            currentStudy = studyDao.findStudyByOid(studyOID);

            if (currentStudy == null) {
                errMsg = "The study {" + studyOID + "} is not existing in the system.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_STUDY_NOT_EXIST);
            } else if (currentStudy.getStatus().equals(Status.LOCKED)) {
                errMsg = "The study {" + studyOID + "} has been LOCKED.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_STUDY_LOCKED);
            }
            // continue check site
            if (siteOID != null) {
                currentSiteStudy = studyDao.findSiteByOid(studyOID, siteOID);

                if (currentSiteStudy == null) {
                    errMsg = "The study site {" + siteOID + "} is not existing in the system.";
                    logger.info(errMsg);
                    throw new OpenClinicaException(errMsg, ErrorConstants.ERR_SITE_NOT_EXIST);
                }
            }

            if (currentSiteStudy != null) {
                currentStudy = currentSiteStudy;
            }

            /**
             *  Step 3: check Subject/Participant
             */
            StudySubjectDAO sdao = this.getMsStudySubjectDAO();
            //OC-10575 check participant level-- study or site
            studySubject = (StudySubjectBean) sdao.findByLabelAndOnlyByStudy(participantId, currentStudy);
            if (studySubject == null || (studySubject.getId() == 0 && studySubject.getLabel().trim().length() == 0)) {
                errMsg = "The study subject {" + studySubjectKey + "} can not be found in the system.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NO_SUBJECT_FOUND);
            }
            Status subjectStatus = studySubject.getStatus();
            if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
                errMsg = "The study subject {" + studySubjectKey + "} has been removed.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_SUBJECT_REMOVED);
            }

            /**
             *  Step 4: check study event
             */
            StudyEventDefinitionDAO seddao = this.getSedDao();
            definition = seddao.findByOidAndStudy(studyEventOID,
                    currentStudy.getStudyId(), currentStudy.checkAndGetParentStudyId());

            Study studyWithEventDefinitions = null;
            if (currentStudy.isSite())
                studyWithEventDefinitions = currentStudy.getStudy();
            else
                studyWithEventDefinitions = currentStudy;
            // find all active definitions with CRFs
            if (definition == null) {
                errMsg = "The definition of event(" + studyEventOID + ") can not be found in the study(" + studyOID + ").";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_EVENT_NOT_EXIST);
            }

            /**
             *  step 5 basic check: sampleOrdinal
             */
            int sampleOrdinal = studyEventDAO.getMaxSampleOrdinal(definition, studySubject) + 1;

            if (definition.getType().equals(COMMON)) {
                errMsg = "The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is not a visit based event.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_WRONG_EVENT_TYPE);
            } else if (!(definition.isRepeating())) {
                if (sampleOrdinal != 1) {
                    errMsg = "The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is a visit based NON repeating event,so ordinal must be 1.";
                    logger.info(errMsg);
                    throw new OpenClinicaException(errMsg, ErrorConstants.ERR_ORDINAL_NOT_ONE_FOR_NONREPEATING);
                }
            } else {
                // repeating visited based event
            }

            /**
             *  step 6: permission check
             */
            UserAccountBean ub = this.getRestfulServiceHelper().getUserAccount(request);
            String userName = ub.getName();
            if (studyOID != null && siteOID != null) {
                errMsg = this.getRestfulServiceHelper().verifyRole(userName, studyOID, siteOID);
            } else {
                errMsg = this.getRestfulServiceHelper().verifyRole(userName, studyOID, null);

            }

            if (errMsg != null) {
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
            }


            if (!subjectMayReceiveStudyEvent(dataSource, definition, studySubject, sampleOrdinal)) {
                errMsg = "The event is NON repeating, and an event of this type already exists for the specified participant.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NON_REPEATING_ALREADY_EXISIT);

            }

            /**
             * At this stage, it has passed all validation check
             */
            StudyEventBean studyEvent = new StudyEventBean();
            Date today = new Date();
            studyEvent.setCreatedDate(today);
            studyEvent.setUpdatedDate(today);
            studyEvent.setStudyEventDefinitionId(definition.getId());
            studyEvent.setStudySubjectId(studySubject.getId());

            studyEvent.setDateStarted(startDt);
            studyEvent.setDateEnded(endDt);
            studyEvent.setOwner(ub);
            studyEvent.setStatus(Status.AVAILABLE);
            studyEvent.setStudySubjectId(studySubject.getId());
            studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);
            studyEvent.setSampleOrdinal(sampleOrdinal);
            studyEvent = (StudyEventBean) studyEventDAO.create(studyEvent);

            if (!studyEvent.isActive()) {
                logger.info("Event is not scheduled -- because it's not active");
                throw new OpenClinicaException("Event is not scheduled", ErrorConstants.ERR_EVENT_NOT_ACTIVE);
            }

        } catch (OpenClinicaException e) {
            message = "Scheduled event " + studyEventOID + " for participant " + participantId + " in study " + studyOID + " Failed.";
            responseDTO.setMessage(message);
            responseDTO.getErrors().add(e.errorID + ":" + e.getOpenClinicaMessage());

            return responseDTO;
        }

        /**
         *  no any error, reply successful response
         */
        message = "Scheduled event " + studyEventOID + " for participant " + participantId + " in study " + studyOID + " successfully.";
        responseDTO.setMessage(message);

        return responseDTO;


    }

    public RestReponseDTO scheduleStudyEvent(UserAccountBean ub, String studyOID, String siteOID, String studyEventOID, String participantId, String startDate, String endDate) {

        RestReponseDTO responseDTO = new RestReponseDTO();
        ArrayList<String> errors = new ArrayList<String>();
        responseDTO.setErrors(errors);

        String message = "";

        String studySubjectKey = participantId;
        String errMsg = null;
        Study currentStudy = null;
        Study currentSiteStudy = null;
        StudyEventDefinitionBean definition = null;
        StudySubjectBean studySubject = null;
        String startDateStr;
        String endDateStr;

        try {
            /**
             *  basic check 1: startDate and end Date
             */
            Date startDt = null;
            Date endDt = null;

            if (startDate == null) {
                errMsg = "start date is missing";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NO_START_DATE);
            } else {
                startDt = this.getRestfulServiceHelper().getDateTime(startDate);
            }

            // endDate is optional
            if (endDate != null && endDate.trim().length() > 7) {
                endDt = this.getRestfulServiceHelper().getDateTime(endDate);
            }

            if (startDt == null) {
                errMsg = "start date can't be parsed as a valid date,please enter in correct date format";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_START_DATE);
            } else if (endDt != null) {

                if (endDt.before(startDt)) {
                    errMsg = "The endDate can not before startDate";
                    logger.info(errMsg);
                    throw new OpenClinicaException(errMsg, ErrorConstants.ERR_END_DATE_BEFORE_START_DATE);
                }
            }

            /**
             * Step 2: check study
             */

            // check study first
            currentStudy = studyDao.findStudyByOid(studyOID);

            if (currentStudy == null) {
                errMsg = "The study {" + studyOID + "} is not existing in the system.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_STUDY_NOT_EXIST);
            } else if (currentStudy.getStatus().equals(Status.LOCKED)) {
                errMsg = "The study {" + studyOID + "} has been LOCKED.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_STUDY_LOCKED);
            }
            // continue check site
            if (siteOID != null) {
                currentSiteStudy = studyDao.findSiteByOid(studyOID, siteOID);

                if (currentSiteStudy == null) {
                    errMsg = "The study site {" + siteOID + "} is not existing in the system.";
                    logger.info(errMsg);
                    throw new OpenClinicaException(errMsg, ErrorConstants.ERR_SITE_NOT_EXIST);
                }
            }

            if (currentSiteStudy != null) {
                currentStudy = currentSiteStudy;
            }

            /**
             *  Step 3: check Subject/Participant
             */
            StudySubjectDAO sdao = this.getMsStudySubjectDAO();

            studySubject = (StudySubjectBean) sdao.findByLabelAndStudy(participantId, currentStudy);
            if (studySubject == null || (studySubject.getId() == 0 && studySubject.getLabel().trim().length() == 0)) {
                errMsg = "The study subject {" + studySubjectKey + "} can not be found in the system.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NO_SUBJECT_FOUND);
            }
            Status subjectStatus = studySubject.getStatus();
            if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
                errMsg = "The study subject {" + studySubjectKey + "} has been removed.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_SUBJECT_REMOVED);
            }

            /**
             *  Step 4: check study event
             */
            StudyEventDefinitionDAO seddao = this.getSedDao();
            definition = seddao.findByOidAndStudy(studyEventOID,
                    currentStudy.getStudyId(), currentStudy.checkAndGetParentStudyId());

            Study studyWithEventDefinitions = null;
            if (currentStudy.isSite())
                studyWithEventDefinitions = currentStudy.getStudy();
            else
                studyWithEventDefinitions = currentStudy;
            // find all active definitions with CRFs
            if (definition == null) {
                errMsg = "The definition of event(" + studyEventOID + ") can not be found in the study(" + studyOID + ").";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_EVENT_NOT_EXIST);
            }

            /**
             *  Step 5 basic check : get sampleOrdinal
             */
            int sampleOrdinal = studyEventDAO.getMaxSampleOrdinal(definition, studySubject) + 1;

            if (definition.getType().equals(COMMON)) {
                errMsg = "The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is not a visit based event.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_WRONG_EVENT_TYPE);
            } else if (!(definition.isRepeating())) {
                if (sampleOrdinal != 1) {
                    errMsg = "The type of event(" + studyEventOID + ") in the study(" + studyOID + ") is a visit based NON repeating event,so ordinal must be 1.";
                    logger.info(errMsg);
                    throw new OpenClinicaException(errMsg, ErrorConstants.ERR_ORDINAL_NOT_ONE_FOR_NONREPEATING);
                }
            } else {
                // repeating visited based event
            }


            /**
             *  step 6: permission check
             */
            String userName = ub.getName();
            if (studyOID != null && siteOID != null) {
                errMsg = this.getRestfulServiceHelper().verifyRole(userName, studyOID, siteOID);
            } else {
                errMsg = this.getRestfulServiceHelper().verifyRole(userName, studyOID, null);
            }

            if (errMsg != null) {
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
            }


            if (!subjectMayReceiveStudyEvent(dataSource, definition, studySubject, sampleOrdinal)) {
                errMsg = "The event is NON repeating, and an event of this type already exists for the specified participant.";
                logger.info(errMsg);
                throw new OpenClinicaException(errMsg, ErrorConstants.ERR_NON_REPEATING_ALREADY_EXISIT);

            }

            /**
             * At this stage, it has passed all validation check
             */
            StudyEventBean studyEvent = new StudyEventBean();
            Date today = new Date();
            studyEvent.setCreatedDate(today);
            studyEvent.setUpdatedDate(today);
            studyEvent.setStudyEventDefinitionId(definition.getId());
            studyEvent.setStudySubjectId(studySubject.getId());

            studyEvent.setDateStarted(startDt);
            studyEvent.setDateEnded(endDt);
            studyEvent.setOwner(ub);
            studyEvent.setStatus(Status.AVAILABLE);
            studyEvent.setStudySubjectId(studySubject.getId());
            studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);
            studyEvent.setSampleOrdinal(sampleOrdinal);
            studyEvent = (StudyEventBean) studyEventDAO.create(studyEvent);

            if (!studyEvent.isActive()) {
                logger.info("Event is not scheduled -- because it's not active");
                throw new OpenClinicaException("Event is not scheduled", ErrorConstants.ERR_EVENT_NOT_ACTIVE);
            }

        } catch (OpenClinicaException e) {
            message = "Scheduled event " + studyEventOID + " for participant " + participantId + " in study " + studyOID + " Failed.";
            responseDTO.setMessage(message);
            responseDTO.getErrors().add(e.errorID + ":" + e.getOpenClinicaMessage());

            return responseDTO;
        }

        /**
         *  no any error, reply successful response
         */
        message = "Scheduled event " + studyEventOID + " for participant " + participantId + " in study " + studyOID + " successfully.";
        responseDTO.setMessage(message);

        return responseDTO;


    }

    /**
     * Determines whether a subject may receive an additional study event. This
     * is true if:
     * <ul>
     * <li>The study event definition is repeating; or
     * <li>The subject does not yet have a study event for the given study event
     * definition
     * </ul>
     *
     * @param studyEventDefinition The definition of the study event which is to be added for the
     *                             subject.
     * @param studySubject         The subject for which the study event is to be added.
     * @return <code>true</code> if the subject may receive an additional study
     * event, <code>false</code> otherwise.
     */
    public boolean subjectMayReceiveStudyEvent(DataSource ds, StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject, int ordinal) throws OpenClinicaException {

        ArrayList<StudyEventBean> allEvents = studyEventDAO.findAllByDefinitionAndSubject(studyEventDefinition, studySubject);

        if (studyEventDefinition.isRepeating()) {
            for (StudyEventBean studyEvent : allEvents) {
                if (studyEvent.getSampleOrdinal() == ordinal) {
                    throw new OpenClinicaException("found repeating event with same ordinal " + ordinal, ErrorConstants.ERR_ALREADY_EXISIT);
                }
            }

        } else {
            if (allEvents.size() > 0) {
                return false;
            }
        }


        return true;
    }


    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource, studyBuildService, studyDao);
        }
        return restfulServiceHelper;
    }

    public StudySubjectDAO getMsStudySubjectDAO() {
        if (msStudySubjectDAO == null) {
            msStudySubjectDAO = new StudySubjectDAO(dataSource);
        }

        return msStudySubjectDAO;
    }

    public void setMsStudySubjectDAO(StudySubjectDAO msStudySubjectDAO) {
        this.msStudySubjectDAO = msStudySubjectDAO;
    }

    public StudyEventDefinitionDAO getSedDao() {
        if (sedDao == null) {
            sedDao = new StudyEventDefinitionDAO(dataSource);
        }

        return sedDao;
    }

    public void setSedDao(StudyEventDefinitionDAO sedDao) {
        this.sedDao = sedDao;
    }

    public Object studyEventProcess(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String methodType) {
        Object subjectObject = null;
        Object eventObject = null;

        Study tenantStudy = null;
        if (siteOid != null) {
            tenantStudy = studyDao.findByOcOID(siteOid);
        } else {
            tenantStudy = studyDao.findByOcOID(studyOid);
        }
        if (tenantStudy == null) {
            logger.error("Study {} Not Valid", tenantStudy.getOc_oid());
        }

        StudyEventResponseDTO studyEventResponseDTO = null;

        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        StudySubject studySubject = null;
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        if (subjectDataBeans != null) {
            for (SubjectDataBean subjectDataBean : subjectDataBeans) {

                if (subjectDataBean.getSubjectOID() != null)
                    subjectDataBean.setSubjectOID(subjectDataBean.getSubjectOID().toUpperCase());


                subjectObject = importService.validateStudySubject(subjectDataBean, tenantStudy);
                if (subjectObject instanceof ErrorObj) {
                    return subjectObject;
                } else if (subjectObject instanceof StudySubject) {
                    studySubject = (StudySubject) subjectObject;
                }


                ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
                for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {

                    if (studyEventDataBean.getStudyEventOID() != null)
                        studyEventDataBean.setStudyEventOID(studyEventDataBean.getStudyEventOID().toUpperCase());
                    StudyEvent studyEvent = null;
                    if (methodType.equals(CREATE)) {
                        eventObject = validateStudyEventToSchedule(studyEventDataBean, studySubject, userAccount);
                    } else {
                        eventObject = validateStudyEventToUpdate(studyEventDataBean, studySubject, userAccount);
                    }
                    if (eventObject instanceof ErrorObj) {
                        return eventObject;
                    } else if (eventObject instanceof StudyEvent) {

                        studyEventResponseDTO = new StudyEventResponseDTO();
                        studyEventResponseDTO.setSubjectKey(subjectDataBean.getStudySubjectID());
                        studyEventResponseDTO.setStudyEventOID(studyEventDataBean.getStudyEventOID());
                        studyEventResponseDTO.setStartDate(studyEventDataBean.getStartDate());
                        studyEventResponseDTO.setEndDate(studyEventDataBean.getEndDate());
                        studyEventResponseDTO.setStudyEventRepeatKey(studyEventDataBean.getStudyEventRepeatKey());
                        studyEventResponseDTO.setEventStatus(((StudyEvent) eventObject).getWorkflowStatus().toString());
                    }
                }
            }
        }

        return studyEventResponseDTO;
    }

    private Object validateStudyEventToSchedule(StudyEventDataBean studyEventDataBean, StudySubject studySubject, UserAccount userAccount) {
        Object eventObject = null;
        StudyEvent studyEvent = null;
        // OID is missing
        if (studyEventDataBean.getStudyEventOID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_STUDYEVENTOID);
        }

        // StudyEventDefinition invalid OID and Archived
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByOcOID(studyEventDataBean.getStudyEventOID());
        if (studyEventDefinition == null || (studyEventDefinition != null && !studyEventDefinition.getStatus().equals(core.org.akaza.openclinica.domain.Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_STUDYEVENTOID);
        }

        int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());
        int eventOrdinal = maxSeOrdinal + 1;

        if (studyEventDefinition.getType().equals(COMMON)) {   // Common Event
            return new ErrorObj(FAILED, ErrorConstants.ERR_COMMON_EVENTS_CANNOT_BE_SCHEDULED);
        } else {   // Visit Event
            if (studyEventDataBean.getStartDate() == null) {
                return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_START_DATE);
            }

            if (studyEventDefinition.isRepeating()) {   // Repeating Visit Event
                studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));

                eventObject = importService.validateStartAndEndDateAndOrder(studyEventDataBean);
                if (eventObject instanceof ErrorObj) return eventObject;

                eventObject = importService.scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);

            } else {   // Non Repeat Event
                studyEventDataBean.setStudyEventRepeatKey(String.valueOf('1'));
                studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());
                if (studyEvent == null) {
                    eventObject = importService.validateStartAndEndDateAndOrder(studyEventDataBean);
                    if (eventObject instanceof ErrorObj) return eventObject;
                    eventObject = importService.scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                } else {
                    return new ErrorObj(FAILED, ErrorConstants.ERR_EVENT_ALREADY_EXISTS);
                }
            }
        }
        return eventObject;
    }


    private Object validateStudyEventToUpdate(StudyEventDataBean studyEventDataBean, StudySubject studySubject, UserAccount userAccount) {
        Object eventObject = null;
        //    StudyEvent studyEvent = null;
        // OID is missing
        if (studyEventDataBean.getStudyEventOID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_STUDYEVENTOID);
        }

        // StudyEventDefinition invalid OID and Archived
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByOcOID(studyEventDataBean.getStudyEventOID());
        if (studyEventDefinition == null || (studyEventDefinition != null && !studyEventDefinition.getStatus().equals(core.org.akaza.openclinica.domain.Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_STUDYEVENTOID);
        }


        int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());
        int eventOrdinal = maxSeOrdinal + 1;

        if (studyEventDefinition.getType().equals(UNSCHEDULED) && studyEventDefinition.isRepeating()) {   // Repeating Visit Event
            if (studyEventDataBean.getStudyEventRepeatKey() != null && !studyEventDataBean.getStudyEventRepeatKey().equals("")) {   // Repeat Key present
                eventObject = importService.validateEventRepeatKeyIntNumber(studyEventDataBean.getStudyEventRepeatKey());
                if (eventObject instanceof ErrorObj) return eventObject;
                eventObject = processEventUpdateForUnscheduled(studyEventDataBean, userAccount, studySubject);
                if (eventObject instanceof ErrorObj) return eventObject;
            } else {
                return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_STUDY_EVENT_REPEAT_KEY);
            }

        } else if (studyEventDefinition.getType().equals(COMMON)) {   // Repeating Visit Event
            if (studyEventDataBean.getStudyEventRepeatKey() != null && !studyEventDataBean.getStudyEventRepeatKey().equals("")) {   // Repeat Key present
                eventObject = importService.validateEventRepeatKeyIntNumber(studyEventDataBean.getStudyEventRepeatKey());
                if (eventObject instanceof ErrorObj) return eventObject;
                studyEventDataBean.setStartDate(null);
                studyEventDataBean.setEndDate(null);
                eventObject = processEventUpdateForCommon(studyEventDataBean, userAccount, studySubject);
                if (eventObject instanceof ErrorObj) return eventObject;
            } else {
                return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_STUDY_EVENT_REPEAT_KEY);
            }

        } else if (studyEventDefinition.getType().equals(UNSCHEDULED) && !studyEventDefinition.isRepeating()) {   // Non Repeat Event
            studyEventDataBean.setStudyEventRepeatKey(String.valueOf('1'));
            eventObject = processEventUpdateForUnscheduled(studyEventDataBean, userAccount, studySubject);
            if (eventObject instanceof ErrorObj) return eventObject;
        }
        return eventObject;
    }


    public Object processEventUpdateForUnscheduled(StudyEventDataBean studyEventDataBean, UserAccount userAccount, StudySubject studySubject) {
        Object eventObject = null;
        StudyEvent studyEvent = null;
        studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());
        if (studyEvent == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_STUDY_EVENT_REPEAT_NOT_FOUND);
        } else {

            if (studyEventDataBean.getStartDate() == null && studyEvent.getDateStart() != null)
                studyEventDataBean.setStartDate(studyEvent.getDateStart().toString().substring(0, 10));
            if (studyEventDataBean.getEndDate() == null && studyEvent.getDateEnd() != null)
                studyEventDataBean.setEndDate(studyEvent.getDateEnd().toString().substring(0, 10));

            eventObject = importService.validateStartAndEndDateAndOrder(studyEventDataBean);
            if (eventObject instanceof ErrorObj) return eventObject;

            if (studyEventDataBean.getEventStatus() == null) {
                eventObject = importService.updateStudyEventDates(studyEvent, userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
            } else {
                eventObject = importService.validateEventStatus(studyEventDataBean.getEventStatus());
                if (eventObject instanceof ErrorObj) return eventObject;
                eventObject = importService.validateEventTransition(studyEvent, userAccount, studyEventDataBean.getEventStatus());
                if (eventObject instanceof ErrorObj) return eventObject;
                eventObject = importService.updateStudyEventDatesAndStatus(studyEvent, userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate(), studyEventDataBean.getEventStatus());
            }
        }
        return eventObject;
    }

    public Object processEventUpdateForCommon(StudyEventDataBean studyEventDataBean, UserAccount userAccount, StudySubject studySubject) {
        Object eventObject = null;
        StudyEvent studyEvent = null;
        studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());
        if (studyEvent == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_STUDY_EVENT_REPEAT_NOT_FOUND);
        } else {
            if (studyEventDataBean.getEventStatus() == null) {
                eventObject = importService.updateStudyEventDates(studyEvent, userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
            } else {
                eventObject = importService.validateEventStatus(studyEventDataBean.getEventStatus());
                if (eventObject instanceof ErrorObj) return eventObject;
                eventObject = importService.validateEventTransition(studyEvent, userAccount, studyEventDataBean.getEventStatus());
                if (eventObject instanceof ErrorObj) return eventObject;
                eventObject = importService.updateStudyEvntStatus(studyEvent, userAccount, studyEventDataBean.getEventStatus());
            }
        }
        return eventObject;
    }

    public void populateOdmContainerForEventUpdate(ODMContainer odmContainer, StudyEventUpdateRequestDTO studyEventUpdateRequestDTO, String siteOid) {
        ArrayList<StudyEventDataBean> studyEventDataBeans = new ArrayList<>();
        StudyEventDataBean studyEventDataBean = new StudyEventDataBean();
        studyEventDataBean.setStudyEventOID(studyEventUpdateRequestDTO.getStudyEventOID());
        studyEventDataBean.setStartDate(studyEventUpdateRequestDTO.getStartDate());
        studyEventDataBean.setEndDate(studyEventUpdateRequestDTO.getEndDate());
        studyEventDataBean.setStudyEventRepeatKey(studyEventUpdateRequestDTO.getStudyEventRepeatKey());
        studyEventDataBean.setEventStatus(studyEventUpdateRequestDTO.getEventStatus());
        studyEventDataBeans.add(studyEventDataBean);

        ArrayList<SubjectDataBean> subjectDataBeans = new ArrayList<>();
        SubjectDataBean subjectDataBean = new SubjectDataBean();
        subjectDataBean.setStudySubjectID(studyEventUpdateRequestDTO.getSubjectKey());
        subjectDataBean.setStudyEventData(studyEventDataBeans);
        subjectDataBeans.add(subjectDataBean);

        CRFDataPostImportContainer importContainer = new CRFDataPostImportContainer();
        importContainer.setStudyOID(siteOid);
        importContainer.setSubjectData(subjectDataBeans);

        odmContainer.setCrfDataPostImportContainer(importContainer);
    }


    public void populateOdmContainerForEventSchedule(ODMContainer odmContainer, StudyEventScheduleRequestDTO studyEventScheduleRequestDTO, String siteOid) {
        ArrayList<StudyEventDataBean> studyEventDataBeans = new ArrayList<>();
        StudyEventDataBean studyEventDataBean = new StudyEventDataBean();
        studyEventDataBean.setStudyEventOID(studyEventScheduleRequestDTO.getStudyEventOID());
        studyEventDataBean.setStartDate(studyEventScheduleRequestDTO.getStartDate());
        studyEventDataBean.setEndDate(studyEventScheduleRequestDTO.getEndDate());
        studyEventDataBeans.add(studyEventDataBean);

        ArrayList<SubjectDataBean> subjectDataBeans = new ArrayList<>();
        SubjectDataBean subjectDataBean = new SubjectDataBean();
        subjectDataBean.setStudySubjectID(studyEventScheduleRequestDTO.getSubjectKey());
        subjectDataBean.setStudyEventData(studyEventDataBeans);
        subjectDataBeans.add(subjectDataBean);

        CRFDataPostImportContainer importContainer = new CRFDataPostImportContainer();
        importContainer.setStudyOID(siteOid);
        importContainer.setSubjectData(subjectDataBeans);

        odmContainer.setCrfDataPostImportContainer(importContainer);
    }


    public void scheduleOrUpdateBulkEvent(MultipartFile file, Study study, String siteOid, UserAccountBean userAccountBean, JobDetail jobDetail, String schema) {

        ResponseEntity response = null;
        String logFileName = null;
        CoreResources.setRequestSchema(schema);

        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        String fileName = study.getUniqueIdentifier() + DASH + study.getEnvType() + SCHEDULE_EVENT + "_" + sdf_fileName.format(new Date()) + ".csv";

        String filePath = userService.getFilePath(JobType.SCHEDULE_EVENT) + File.separator + fileName;
        jobDetail.setLogPath(filePath);
        List<DataImportReport> dataImportReports = new ArrayList<>();
        try {

            // read csv file
            List<StudyEventScheduleDTO> studyEventScheduleDTOList = csvService.readStudyEventScheduleBulkCSVFile(file, study.getOc_oid(), siteOid);
            for (StudyEventScheduleDTO studyEventScheduleDTO : studyEventScheduleDTOList) {
                String studyEventOID = studyEventScheduleDTO.getStudyEventOID();
                String participantId = studyEventScheduleDTO.getSubjectKey();
                String eventRepeatKey = studyEventScheduleDTO.getOrdinal();
                String startDate = studyEventScheduleDTO.getStartDate();
                String endDate = studyEventScheduleDTO.getEndDate();
                String studyEventStatus = studyEventScheduleDTO.getStudyEventStatus();
                Integer rowNumber=studyEventScheduleDTO.getRowNum();

                ODMContainer odmContainer = new ODMContainer();
                Object result = null;
                DataImportReport dataImportReport = null;
                if (eventRepeatKey == null) {
                    //schedule events
                    StudyEventScheduleRequestDTO studyEventScheduleRequestDTO = new StudyEventScheduleRequestDTO();
                    studyEventScheduleRequestDTO.setStudyEventOID(studyEventOID);
                    studyEventScheduleRequestDTO.setSubjectKey(participantId);
                    studyEventScheduleRequestDTO.setStartDate(startDate);
                    studyEventScheduleRequestDTO.setEndDate(endDate);
                    populateOdmContainerForEventSchedule(odmContainer, studyEventScheduleRequestDTO, siteOid);
                    result = studyEventProcess(odmContainer, study.getOc_oid(), siteOid, userAccountBean, CREATE);
                    if(result instanceof StudyEventResponseDTO) {
                        dataImportReport = new DataImportReport(rowNumber,participantId, studyEventOID,((StudyEventResponseDTO) result).getStudyEventRepeatKey(), CREATED, null);
                    }

                } else {
                    //Update events
                    StudyEventUpdateRequestDTO studyEventUpdateRequestDTO = new StudyEventUpdateRequestDTO();
                    studyEventUpdateRequestDTO.setStudyEventOID(studyEventOID);
                    studyEventUpdateRequestDTO.setSubjectKey(participantId);
                    studyEventUpdateRequestDTO.setStudyEventRepeatKey(eventRepeatKey);
                    studyEventUpdateRequestDTO.setStartDate(startDate);
                    studyEventUpdateRequestDTO.setEndDate(endDate);
                    studyEventUpdateRequestDTO.setEventStatus(studyEventStatus);
                    populateOdmContainerForEventUpdate(odmContainer, studyEventUpdateRequestDTO, siteOid);
                    result = studyEventProcess(odmContainer, study.getOc_oid(), siteOid, userAccountBean, UPDATE);
                    if(result instanceof StudyEventResponseDTO) {
                        dataImportReport = new DataImportReport(rowNumber,participantId, studyEventOID,((StudyEventResponseDTO) result).getStudyEventRepeatKey(), UPDATED, null);
                    }
                }
                if (result instanceof ErrorObj) {
                    dataImportReport = new DataImportReport(rowNumber,participantId, studyEventOID, eventRepeatKey, ((ErrorObj) result).getCode(), ((ErrorObj) result).getMessage());
                }
                dataImportReports.add(dataImportReport);


            }
            importService.writeToFile(dataImportReports, fileName, JobType.SCHEDULE_EVENT);
            userService.persistJobCompleted(jobDetail, fileName);

        } catch (Exception e) {
            userService.persistJobFailed(jobDetail, fileName);
            logger.error("Error " + e.getMessage());
        }


    }

public void convertStudyEventStatus(String value, StudyEvent studyEvent){
        StudyEventWorkflowStatusEnum workflowStatus=convertOriginalStudyEventStatusToWorkflowSatus(value);
        if (workflowStatus!=null) {
            studyEvent.setWorkflowStatus(workflowStatus);
        }else{
            if (value.equals("7")) {
                studyEvent.setLocked(Boolean.TRUE);
            }else if (value.equals("8")){
                studyEvent.setSigned(Boolean.TRUE);
            }
        }
}

    public void convertStudyEventBeanStatus(String value, StudyEventBean studyEventBean){
        StudyEventWorkflowStatusEnum workflowStatus=convertOriginalStudyEventStatusToWorkflowSatus(value);
        if (workflowStatus!=null) {
            if( !studyEventBean.getWorkflowStatus().equals(workflowStatus) && studyEventBean.isSigned())
                studyEventBean.setSigned(Boolean.FALSE);
            studyEventBean.setWorkflowStatus(workflowStatus);
        }else{
            if (value.equals("7")) {
                studyEventBean.setLocked(Boolean.TRUE);
            }else if (value.equals("8")){
                studyEventBean.setSigned(Boolean.TRUE);
            }
        }
    }

    public StudyEventWorkflowStatusEnum convertOriginalStudyEventStatusToWorkflowSatus(String value) {
        switch (value) {
            case "1":
                return StudyEventWorkflowStatusEnum.NOT_SCHEDULED;
            case "2":
                return StudyEventWorkflowStatusEnum.SCHEDULED;
            case "3":
                return StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED;
            case "4":
                return StudyEventWorkflowStatusEnum.COMPLETED;
            case "5":
                return StudyEventWorkflowStatusEnum.STOPPED;
            case "6":
                return StudyEventWorkflowStatusEnum.SKIPPED;
            default:
                return null;
        }
    }

    public boolean isEventSignable(StudyEvent studyEvent){
        if (!areAllEventCrfsValid(studyEvent) || !areAllRequiredEventCrfsComplete(studyEvent)) {
            return false;
        }
        return true;
    }

    private boolean areAllEventCrfsValid(StudyEvent studyEvent) {
        List<EventCrf> eventCrfs = studyEvent.getEventCrfs();
        for (EventCrf eventCrf: eventCrfs) {
            EventDefinitionCRFBean eventDefinitionCrf = eventDefinitionCRFDAO.findForStudyByStudyEventIdAndCRFVersionId(eventCrf.getStudyEvent().getStudyEventId(), eventCrf.getCrfVersion().getCrfVersionId());
            // AC1 :An event will be eligible to sign only if all non-Archived required forms in the event
            // have Complete workflow status and are not Removed.
            // AC2: An event will be eligible to sign only if all non-Archived forms in the event
            // have Not Started or Complete workflow status or are Removed
            if (!eventCrf.isCurrentlyArchived()) {
                if (eventDefinitionCrf.isRequiredCRF()) {
                    if (eventCrf.isCurrentlyRemoved() || !eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                        return false;
                    }
                } else {
                    if (!eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)
                            && !eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.NOT_STARTED)
                            && !eventCrf.isCurrentlyRemoved()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean areAllRequiredEventCrfsComplete(StudyEvent studyEvent) {

        List<EventCrf> existingEventCrfs = studyEvent.getEventCrfs();
        List<EventDefinitionCrf> requiredEventDefinitionCrfs = studyEvent.getStudyEventDefinition().getEventDefinitionCrfs().stream().
                filter(eventDefinitionCrf -> eventDefinitionCrf.getRequiredCrf()).
                collect(Collectors.toList());

        for (EventDefinitionCrf requiredEventDefinitionCrf : requiredEventDefinitionCrfs){
            boolean requiredFound = false;
            for (EventCrf existingEventCrf : existingEventCrfs){
                if (existingEventCrf.getCrfVersion().getCrf().getCrfId() == requiredEventDefinitionCrf.getCrf().getCrfId()){
                    requiredFound = true;
                }
            }
            if (!requiredFound){
                return false;
            }
        }
        return true;
    }
}





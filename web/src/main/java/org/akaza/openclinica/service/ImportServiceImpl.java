package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jxl.demo.CSV;
import net.sf.saxon.type.ItemType;
import org.akaza.openclinica.ParticipateInviteEnum;
import org.akaza.openclinica.ParticipateInviteStatusEnum;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.bean.submit.crfdata.*;
import org.akaza.openclinica.controller.dto.*;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.controller.openrosa.OpenRosaSubmissionController;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.domain.EventCRFStatus;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.enumsupport.JobStatus;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.akaza.openclinica.web.util.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.akaza.openclinica.domain.rule.action.NotificationActionProcessor.messageServiceUri;
import static org.akaza.openclinica.domain.rule.action.NotificationActionProcessor.sbsUrl;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "importService" )
public class ImportServiceImpl implements ImportService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    StudyDao studyDao;

    @Autowired
    StudySubjectDao studySubjectDao;

    @Autowired
    StudyEventDao studyEventDao;

    @Autowired
    EventCrfDao eventCrfDao;

    @Autowired
    ItemDataDao itemDataDao;

    @Autowired
    StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    CrfDao crfDao;

    @Autowired
    CrfVersionDao crfVersionDao;

    @Autowired
    CompletionStatusDao completionStatusDao;

    @Autowired
    EventDefinitionCrfDao eventDefinitionCrfDao;

    @Autowired
    FormLayoutDao formLayoutDao;

    @Autowired
    ItemGroupDao itemGroupDao;

    @Autowired
    ItemGroupMetadataDao itemGroupMetadataDao;

    @Autowired
    ItemDao itemDao;

    @Autowired
    ValidateService validateService;

    @Autowired
    UtilService utilService;

    @Autowired
    UserService userService;

    @Autowired
    JobService jobService;

    @Autowired
    OpenRosaSubmissionController openRosaSubmissionController;

    public static final String COMMON = "common";
    public static final String UNSCHEDULED = "unscheduled";
    public static final String SEPERATOR = "|";
    public static final String BULK_JOBS = "bulk_jobs";
    public static final String DASH = "-";
    public static final String UNDERSCORE = "_";
    public static final String INITIAL_DATA_ENTRY = "initial data entry";
    public static final String DATA_ENTRY_COMPLETE = "data entry complete";
    public static final String COMPLETE = "complete";
    public static final String FAILED = "Failed";
    public static final String INSERTED = "Inserted";
    public static final String UPDATED = "Updated";
    public static final String SKIPPED = "Skipped";


    @Transactional
    public void validateAndProcessDataImport(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail) {
        CoreResources.setRequestSchema(schema);
        Study tenantStudy = null;
        if (siteOid != null) {
            tenantStudy = studyDao.findByOcOID(siteOid);
        } else {
            tenantStudy = studyDao.findByOcOID(studyOid);
        }
        if (tenantStudy == null) {
            logger.debug("Study {} Not Valid", tenantStudy.getOc_oid());

        }

        List<DataImportReport> dataImportReports = new ArrayList<>();
        DataImportReport dataImportReport = null;

        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        String uniqueIdentifier = tenantStudy.getStudy() == null ? tenantStudy.getUniqueIdentifier() : tenantStudy.getStudy().getUniqueIdentifier();
        String envType = tenantStudy.getStudy() == null ? tenantStudy.getEnvType().toString() : tenantStudy.getStudy().getEnvType().toString();

        String fileName = uniqueIdentifier + DASH + envType + UNDERSCORE + JobType.XML_IMPORT + new SimpleDateFormat("_yyyy-MM-dd-hhmmssS'.txt'").format(new Date());

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            subjectDataBean.setSubjectOID(subjectDataBean.getSubjectOID().toUpperCase());
            StudySubject studySubject = null;
            StudySubject studySubject02 = null;


            if (subjectDataBean.getSubjectOID() == null && subjectDataBean.getStudySubjectID() == null) {
                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, null, null, FAILED, "errorCode.participantNotFound ");
                dataImportReports.add(dataImportReport);
                continue;
            } else if (subjectDataBean.getSubjectOID() != null && subjectDataBean.getStudySubjectID() == null) {
                studySubject = studySubjectDao.findByOcOID(subjectDataBean.getSubjectOID());
                if (studySubject == null || (studySubject != null && studySubject.getStudy().getStudyId() != tenantStudy.getStudyId())) {
                    logger.debug("SubjectKey {} Not Found", subjectDataBean.getSubjectOID());
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, null, null, FAILED, "errorCode.participantNotFound ");
                    dataImportReports.add(dataImportReport);
                    continue;
                }
            } else if (subjectDataBean.getSubjectOID() == null && subjectDataBean.getStudySubjectID() != null) {
                try {
                    studySubject = studySubjectDao.findByLabelAndStudyOrParentStudy(subjectDataBean.getStudySubjectID(), tenantStudy);
                    if (studySubject == null || (studySubject != null && studySubject.getStudy().getStudyId() != tenantStudy.getStudyId())) {
                        logger.debug("SubjectKey {} Not Found", subjectDataBean.getSubjectOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, null, null, FAILED, "errorCode.participantNotFound ");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.debug(e.getMessage());
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, null, null, FAILED, "errorCode.multipleParticipantsFound ");
                    dataImportReports.add(dataImportReport);
                    continue;
                }

            } else if (subjectDataBean.getSubjectOID() != null && subjectDataBean.getStudySubjectID() != null) {
                studySubject = studySubjectDao.findByOcOID(subjectDataBean.getSubjectOID());
                studySubject02 = studySubjectDao.findByLabelAndStudyOrParentStudy(subjectDataBean.getStudySubjectID(), tenantStudy);

                if (studySubject == null || studySubject02 == null || (studySubject != null && studySubject02 != null && studySubject.getStudySubjectId() != studySubject02.getStudySubjectId())) {
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, null, null, FAILED, "errorCode.participantIdentiersMismatch ");
                    dataImportReports.add(dataImportReport);
                    continue;
                }
            }
            subjectDataBean.setSubjectOID(studySubject.getOcOid());
            subjectDataBean.setStudySubjectID(studySubject.getLabel());

            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                studyEventDataBean.setStudyEventOID(studyEventDataBean.getStudyEventOID().toUpperCase());
                // OID is missing
                if (studyEventDataBean.getStudyEventOID() == null) {
                    logger.debug("StudEventOID {} for SubjectKey {} is not valid", studyEventDataBean.getStudyEventOID(), subjectDataBean.getSubjectOID());
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, null, null, FAILED, "errorCode.invalidStudyEventOID ");
                    dataImportReports.add(dataImportReport);
                    continue;
                }

                // StudyEventDefinition invalid OID and Archived
                StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByOcOID(studyEventDataBean.getStudyEventOID());
                if (studyEventDefinition == null || (studyEventDefinition != null && (studyEventDefinition.getStatus().equals(Status.DELETED) || studyEventDefinition.getStatus().equals(Status.AUTO_DELETED)))) {
                    logger.debug("StudEventOID {} for SubjectKey {} is not valid", studyEventDataBean.getStudyEventOID(), subjectDataBean.getSubjectOID());
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, null, null, FAILED, "errorCode.invalidStudyEventOID ");
                    dataImportReports.add(dataImportReport);
                    continue;
                }


                // Study Event Repeat key is not an int number
                try {
                    Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey());
                } catch (NumberFormatException nfe) {
                    nfe.getStackTrace();
                    logger.debug("StudyEventRepeatKey {} for SubjectKey {} and StudyEventOID {} is not Valid", studyEventDataBean.getStudyEventRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.repeatKeyNotValid ");
                    dataImportReports.add(dataImportReport);
                    continue;
                }

                int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());

                StudyEvent studyEvent = null;
                if (studyEventDataBean.getStudyEventRepeatKey() != null) {  // Repeat Key present
                    studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());
                    // repeat key present,not scheduled, common event , verify key too Large , schedule
                    if (studyEvent == null && studyEventDefinition.getType().equals(COMMON)) {
                        if (Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()) != (maxSeOrdinal + 1)) {
                            logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is not next available repeat number", studyEventDataBean.getStudyEventRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.studyEventRepeatKeyTooLarge ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }

                        studyEvent = createStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, null);
                        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                        logger.debug("Scheduling new Common Event");
                        // repeat key present,not scheduled, visit event Repeating ,start date present , verify key too Large ,verify startdate valid ,schedule
                    } else if (studyEvent == null && studyEventDefinition.getType().equals(UNSCHEDULED) && studyEventDefinition.getRepeating() && studyEventDataBean.getStartDate() != null) {
                        if (Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()) != (maxSeOrdinal + 1)) {
                            logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is not next available repeat number", studyEventDataBean.getStudyEventRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.studyEventRepeatKeyTooLarge ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }
                        ErrorObj startDateErrorObj = validateForDate(studyEventDataBean.getStartDate());
                        if (startDateErrorObj != null) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.invalidStartDate");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }

                        studyEvent = createStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, studyEventDataBean.getStartDate());
                        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                        logger.debug("Scheduling new Visit Base Repeating Event");

                        // repeat key present,not scheduled, visit event Repeating ,start date null , Can't schedule
                    } else if (studyEvent == null && studyEventDefinition.getType().equals(UNSCHEDULED) && studyEventDefinition.getRepeating() && studyEventDataBean.getStartDate() == null) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.startDateMissing");
                        dataImportReports.add(dataImportReport);
                        continue;

                        // repeat key present, not scheduled,visit event Non Repeating ,start date present ,verify repeat key=1 ,verify startdate valid ,schedule
                    } else if (studyEvent == null && studyEventDefinition.getType().equals(UNSCHEDULED) && !studyEventDefinition.getRepeating() && studyEventDataBean.getStartDate() != null) {

                        if (Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()) != 1) {
                            logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Less Than 1", studyEventDataBean.getStudyEventRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.repeatKeyNot1 ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }

                        ErrorObj startDateErrorObj = validateForDate(studyEventDataBean.getStartDate());
                        if (startDateErrorObj != null) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.invalidStartDate");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }
                        studyEvent = createStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, studyEventDataBean.getStartDate());
                        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                        logger.debug("Scheduling new Visit Base Non Repeating Event");

                        // repeat key present,not scheduled, visit event Non Repeating ,start date null , Can't schedule
                    } else if (studyEvent == null && studyEventDefinition.getType().equals(UNSCHEDULED) && !studyEventDefinition.getRepeating() && studyEventDataBean.getStartDate() == null) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.startDateMissing");
                        dataImportReports.add(dataImportReport);
                        continue;


                    }


                } else { // Repeat Key is missing
                    // Repeat Key missing,not scheduled, common event , assign repeat, schedule
                    if (studyEvent == null && studyEventDefinition.getType().equals(COMMON)) {
                        // assign new repeat Key
                        studyEvent = createStudyEvent(studySubject, studyEventDefinition, maxSeOrdinal + 1, userAccount, null);
                        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                        logger.debug("Scheduling new Common Event");
                        // Repeat Key missing, not scheduled,visit event Repeating, start date not null,validate start date , assign repeat, schedule
                    } else if (studyEvent == null && studyEventDefinition.getType().equals(UNSCHEDULED) && studyEventDefinition.getRepeating() && studyEventDataBean.getStartDate() != null) {
                        ErrorObj startDateErrorObj = validateForDate(studyEventDataBean.getStartDate());
                        if (startDateErrorObj != null) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.invalidStartDate");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }

                        studyEvent = createStudyEvent(studySubject, studyEventDefinition, maxSeOrdinal + 1, userAccount, studyEventDataBean.getStartDate());
                        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                        logger.debug("Scheduling new Visit Base Repeating Event");

                        // Repeat Key missing, not scheduled,visit event Repeating, start date  null, reject
                    } else if (studyEvent == null && studyEventDefinition.getType().equals(UNSCHEDULED) && studyEventDefinition.getRepeating() && studyEventDataBean.getStartDate() == null) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.startDateMissing");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }


                    // Repeat Key missing,visit event Non Repeating,

                    if (studyEventDefinition.getType().equals(UNSCHEDULED) && !studyEventDefinition.getRepeating()) {
                        studyEventDataBean.setStudyEventRepeatKey("1");
                        studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());

                        // Repeat Key missing, not scheduled,visit event Non Repeating,start date not null,validate start date , assign repeat 1 , schedule
                        if (studyEvent == null && studyEventDataBean.getStartDate() != null) {
                            ErrorObj startDateErrorObj = validateForDate(studyEventDataBean.getStartDate());
                            if (startDateErrorObj != null) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.invalidStartDate");
                                dataImportReports.add(dataImportReport);
                                continue;
                            }

                            studyEvent = createStudyEvent(studySubject, studyEventDefinition, 1, userAccount, studyEventDataBean.getStartDate());
                            studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                            logger.debug("Scheduling new Visit Base Repeating Event");

                            // Repeat Key missing,not scheduled, visit event Non Repeating, start date  null, reject
                        } else if (studyEvent == null && studyEventDataBean.getStartDate() == null) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, null, null, FAILED, "errorCode.eventNotScheduled.startDateMissing");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }
                    }

                }


                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                for (FormDataBean formDataBean : formDataBeans) {
                    formDataBean.setFormOID(formDataBean.getFormOID().toUpperCase());
                    formDataBean.setEventCRFStatus(formDataBean.getEventCRFStatus().toLowerCase());
                    if (formDataBean.getFormOID() == null) {
                        logger.debug("FormOid {} for SubjectKey {} and StudyEventOID {} is not Valid", formDataBean.getFormOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, null, null, FAILED, "errorCode.formOIDNotFound ");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }


                    // Form Invalid OID
                    CrfBean crf = crfDao.findByOcOID(formDataBean.getFormOID());
                    if (crf == null) {
                        logger.debug("FormOid {} for SubjectKey {} and StudyEventOID {} is not Valid", formDataBean.getFormOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, null, null, FAILED, "errorCode.formOIDNotFound ");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }
                    // Form Invalid OID
                    EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                            tenantStudy.getStudy() == null ? tenantStudy.getStudyId() : tenantStudy.getStudy().getStudyId());
                    if (edc == null || (edc != null && (edc.getStatusId().equals(Status.DELETED.getCode()) || edc.getStatusId().equals(Status.AUTO_DELETED.getCode())))) {
                        logger.debug("FormOid {} for SubjectKey {} and StudyEventOID {} is not Valid", formDataBean.getFormOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, null, null, FAILED, "errorCode.formOIDNotFound ");
                        dataImportReports.add(dataImportReport);
                        continue;

                    }

                    if (formDataBean.getFormLayoutName() == null) {
                        logger.debug("FormLayoutOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", formDataBean.getFormLayoutName(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), null, null, null, null, FAILED, "errorCode.formLayoutOIDNotFound ");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }

                    // FormLayout Invalid OID
                    FormLayout formLayout = formLayoutDao.findByNameCrfId(formDataBean.getFormLayoutName(), crf.getCrfId());
                    if (formLayout == null || (formLayout != null && formLayout.getStatus().equals(Status.LOCKED))) {
                        logger.debug("FormLayoutOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", formDataBean.getFormLayoutName(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), null, null, null, null, FAILED, "errorCode.formLayoutOIDNotFound ");
                        dataImportReports.add(dataImportReport);
                        continue;

                    }

                    // Form Status is null , then Form has Initial Data Entry Status
                    if (formDataBean.getEventCRFStatus() == null) {
                        formDataBean.setEventCRFStatus(INITIAL_DATA_ENTRY);
                    }

                    // Form Status is not acceptable
                    if (!formDataBean.getEventCRFStatus().equals(INITIAL_DATA_ENTRY) &&
                            !formDataBean.getEventCRFStatus().equals(DATA_ENTRY_COMPLETE) &&
                            !formDataBean.getEventCRFStatus().equals(COMPLETE)) {
                        logger.debug("Form Status {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", formDataBean.getEventCRFStatus(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), null, null, null, null, FAILED, "errorCode.formStatusNotValid ");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }

                    // Event Crf has status complete
                    EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());
                    if (eventCrf != null && eventCrf.getStatusId() == Status.UNAVAILABLE.getCode()) {
                        logger.debug("Form {} for SubjectKey {} and StudyEventOID {} and FormOID {} already complete", formDataBean.getFormOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), null, null, null, null, FAILED, "errorCode.formAlreadyComplete ");
                        dataImportReports.add(dataImportReport);
                        continue;
                    }

                    if (eventCrf == null) {
                        eventCrf = createEventCrf(studySubject, studyEvent, formLayout, userAccount);
                        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
                        logger.debug("*********CREATED EVENT CRF");

                        studyEvent = updateStudyEvent(studyEvent, userAccount);
                        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                        logger.debug("*********Update Study EVENT Status");
                    }

                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();

                    int itemCountInFormData = 0;
                    int itemInsertedUpdatedCountInFrom = 0;
                    int itemInsertedUpdatedSkippedCountInFrom = 0;
                    for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                        itemCountInFormData = itemCountInFormData + itemGroupDataBean.getItemData().size();
                    }

                    for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                        itemGroupDataBean.setItemGroupOID(itemGroupDataBean.getItemGroupOID().toUpperCase());
                        if (itemGroupDataBean.getItemGroupOID() == null) {
                            logger.debug("ItemGroupOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", itemGroupDataBean.getItemGroupOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), itemGroupDataBean.getItemGroupOID(), null, null, null, FAILED, "errorCode.itemGroupOIDNotFound ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }

                        //Item Group invalid Oid
                        ItemGroup itemGroup = itemGroupDao.findByOcOID(itemGroupDataBean.getItemGroupOID());
                        if (itemGroup == null) {
                            logger.debug("ItemGroupOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", itemGroupDataBean.getItemGroupOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), itemGroupDataBean.getItemGroupOID(), null, null, null, FAILED, "errorCode.itemGroupOIDNotFound ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }
                        //Item Group invalid Oid
                        ItemGroup itmGroup = itemGroupDao.findByNameCrfId(itemGroup.getName(), crf);
                        if (itmGroup == null) {
                            logger.debug("ItemGroupOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", itemGroupDataBean.getItemGroupOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), itemGroupDataBean.getItemGroupOID(), null, null, null, FAILED, "errorCode.itemGroupOIDNotFound ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }

                        ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();

                        int highestGroupOrdinal = 0;

                        List<ItemGroupMetadata> igms = itemGroup.getItemGroupMetadatas();
                        for (ItemGroupMetadata igm : igms) {
                            int maxRepeatGroup = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), igm.getItem().getItemId());
                            if (maxRepeatGroup > highestGroupOrdinal)
                                highestGroupOrdinal = maxRepeatGroup;
                        }


                        //Missing Item Group Repeat Key
                        if (itemGroupDataBean.getItemGroupRepeatKey() == null && !itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup()) {
                            itemGroupDataBean.setItemGroupRepeatKey("1");
                        } else if (itemGroupDataBean.getItemGroupRepeatKey() == null && itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup()) {
                            itemGroupDataBean.setItemGroupRepeatKey(String.valueOf(highestGroupOrdinal + 1));
                        }

                        if (Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()) > (highestGroupOrdinal + 1)) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, null, FAILED, "errorCode.itemGroupRepeatKeyTooLarge");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }


                        // Item Group Repeat key is not an int number
                        try {
                            int groupRepeatKey = Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey());
                        } catch (NumberFormatException nfe) {
                            nfe.getStackTrace();
                            logger.debug("ItemGroupRepeatKey {} for SubjectKey {} and StudyEventOID {} and FormOid {} is not Valid", itemGroupDataBean.getItemGroupRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(), formDataBean.getFormOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, null, FAILED, "errorCode.groupRepeatKeyNotValid ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }


                        // Item Group Repeat key is Less than 1
                        if (Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()) < 1) {
                            logger.debug("Group RepeatKey {} for SubjectKey {} and StudyEventOID {} is Less Than 1", itemGroupDataBean.getItemGroupRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, null, FAILED, "errorCode.groupRepeatKeyLessThanOne ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }


                        //Item Group is Non Repeating and Repeat key is >1
                        if (!itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup() && Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()) > 1) {
                            logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Larger Than 1", studyEventDataBean.getStudyEventRepeatKey(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formDataBean.getFormLayoutName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, null, FAILED, "errorCode.repeatKeyLargerThanOne ");
                            dataImportReports.add(dataImportReport);
                            continue;
                        }


                        for (ImportItemDataBean itemDataBean : itemDataBeans) {
                            itemDataBean.setItemOID(itemDataBean.getItemOID().toUpperCase());
                            if (itemDataBean.getItemOID() == null) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), null, FAILED, "errorCode.itemNotFound ");
                                dataImportReports.add(dataImportReport);
                                continue;
                            }

                            Item item = itemDao.findByOcOID(itemDataBean.getItemOID());

                            // ItemOID is not valid
                            if (item == null) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), FAILED, "errorCode.itemNotFound ");
                                dataImportReports.add(dataImportReport);
                                continue;
                            }
                            Item itm = itemDao.findByNameCrfId(item.getName(), crf.getCrfId());
                            // ItemOID is not valid
                            if (itm == null) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), FAILED, "errorCode.itemNotFound ");
                                dataImportReports.add(dataImportReport);
                                continue;
                            }


                            if (itemDataBean.getValue().length() > 3999) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), FAILED, "errorCode.valueTooLong ");
                                dataImportReports.add(dataImportReport);
                                continue;
                            }

                            if(StringUtils.isNotEmpty(itemDataBean.getValue())) {
                                ErrorObj itemDataTypeErrorObj = validateItemDataType(item, itemDataBean.getValue());
                                if (itemDataTypeErrorObj != null) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), FAILED, itemDataTypeErrorObj.getMessage());
                                    dataImportReports.add(dataImportReport);
                                    continue;
                                }

                                Set<ItemFormMetadata> ifms = item.getItemFormMetadatas();
                                ResponseSet responseSet = ifms.iterator().next().getResponseSet();
                                ErrorObj responseSetErrorObj = validateResponseSets(responseSet, itemDataBean.getValue());
                                if (responseSetErrorObj != null) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), FAILED, responseSetErrorObj.getMessage());
                                    dataImportReports.add(dataImportReport);
                                    continue;
                                }
                            }

                            ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));

                            if (itemData != null) {
                                if (itemData.getValue().equals(itemDataBean.getValue())) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), SKIPPED, "");
                                    dataImportReports.add(dataImportReport);
                                    itemInsertedUpdatedSkippedCountInFrom++;
                                } else {
                                    itemData = updateItemData(itemData, userAccount, itemDataBean.getValue());
                                    itemData = itemDataDao.saveOrUpdate(itemData);
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), UPDATED, "");
                                    dataImportReports.add(dataImportReport);
                                    itemInsertedUpdatedCountInFrom++;
                                    itemInsertedUpdatedSkippedCountInFrom++;
                                }
                            } else {
                                itemData = createItemData(eventCrf, itemDataBean, userAccount, item, Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
                                itemData = itemDataDao.saveOrUpdate(itemData);
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), formLayout.getName(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), itemDataBean.getValue(), INSERTED, "");
                                dataImportReports.add(dataImportReport);
                                itemInsertedUpdatedCountInFrom++;
                                itemInsertedUpdatedSkippedCountInFrom++;
                            }
                        }//itemDataBean for loop
                    } //itemGroupDataBean for loop

                    if ((formDataBean.getEventCRFStatus().equals(COMPLETE) || formDataBean.getEventCRFStatus().equals(DATA_ENTRY_COMPLETE)) && itemInsertedUpdatedSkippedCountInFrom == itemCountInFormData) {                         // update eventcrf status into Complete
                        // Update Event Crf Status into Complete
                        eventCrf = updateEventCrf(eventCrf, userAccount, Status.UNAVAILABLE);
                        // check if all Forms within this Event is Complete
                        openRosaSubmissionController.updateStudyEventStatus(tenantStudy, studySubject, studyEventDefinition, studyEvent, userAccount);

                    } else if (itemInsertedUpdatedCountInFrom > 0) {                         // update eventcrf status into data entry status
                        // Update Event Crf Status into Initial Data Entry
                        eventCrf = updateEventCrf(eventCrf, userAccount, Status.AVAILABLE);
                    }


                } // formDataBean for loop
            } // StudyEventDataBean for loop
        } // StudySubjectDataBean for loop

        writeToFile(dataImportReports, studyOid, fileName);
        userService.persistJobCompleted(jobDetail, fileName);

    }

    private void writeToFile(List<DataImportReport> dataImportReports, String studyOid, String fileName) {
        String filePath = getFilePath(JobType.XML_IMPORT) + File.separator + fileName;

        File file = new File(filePath);

        PrintWriter writer = null;
        try {
            writer = openFile(file);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.print(writeToTextFile(dataImportReports));
            closeFile(writer);
        }
        StringBuilder body = new StringBuilder();


        logger.info(body.toString());


    }


    public String getFilePath(JobType jobType) {
        String dirPath = CoreResources.getField("filePath") + BULK_JOBS + File.separator + jobType.toString().toLowerCase();
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return dirPath;
    }

    private PrintWriter openFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
        return writer;
    }


    private void closeFile(PrintWriter writer) {
        writer.close();
    }


    private String writeToTextFile(List<DataImportReport> dataImportReports) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SubjectKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ParticipantID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("StudyEventOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("StudyEventRepeatKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("FormOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("FormLayoutOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemGroupOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemGroupRepeatKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Value");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Message");
        stringBuffer.append('\n');
        for (DataImportReport dataImportReport : dataImportReports) {
            stringBuffer.append(dataImportReport.getSubjectKey() != null ? dataImportReport.getSubjectKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudySubjectID() != null ? dataImportReport.getStudySubjectID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudyEventOID() != null ? dataImportReport.getStudyEventOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudyEventRepeatKey() != null ? dataImportReport.getStudyEventRepeatKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getFormOID() != null ? dataImportReport.getFormOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getFormLayoutOID() != null ? dataImportReport.getFormLayoutOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemGroupOID() != null ? dataImportReport.getItemGroupOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemGroupRepeatKey() != null ? dataImportReport.getItemGroupRepeatKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemOID() != null ? dataImportReport.getItemOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getValue() != null ? dataImportReport.getValue() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStatus() != null ? dataImportReport.getStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getMessage() != null ? dataImportReport.getMessage() : "");
            stringBuffer.append('\n');
        }

        StringBuilder sb = new StringBuilder();
        sb.append(stringBuffer.toString() + "\n");

        return sb.toString();
    }

    private ItemData createItemData(EventCrf eventCrf, ImportItemDataBean itemDataBean, UserAccount userAccount, Item item, int groupRepeatKey) {
        ItemData itemData = new ItemData();
        itemData.setEventCrf(eventCrf);
        itemData.setItem(item);
        itemData.setDeleted(false);
        itemData.setValue(itemDataBean.getValue());
        itemData.setUserAccount(userAccount);
        itemData.setDateCreated(new Date());
        itemData.setStatus(Status.AVAILABLE);
        itemData.setOrdinal(groupRepeatKey);
        return itemData;
    }

    private ItemData updateItemData(ItemData itemData, UserAccount userAccount, String value) {
        itemData.setValue(value);
        itemData.setOldStatus(itemData.getStatus());
        itemData.setDateUpdated(new Date());
        itemData.setUpdateId(userAccount.getUserId());
        return itemData;
    }


    private EventCrf createEventCrf(StudySubject studySubject, StudyEvent studyEvent, FormLayout formLayout, UserAccount userAccount) {
        EventCrf eventCrf = new EventCrf();
        CrfVersion crfVersion = crfVersionDao.findAllByCrfId(formLayout.getCrf().getCrfId()).get(0);
        Date currentDate = new Date();
        eventCrf.setAnnotations("");
        eventCrf.setDateCreated(currentDate);
        eventCrf.setCrfVersion(crfVersion);
        eventCrf.setFormLayout(formLayout);
        eventCrf.setInterviewerName("");
        eventCrf.setDateInterviewed(null);
        eventCrf.setUserAccount(userAccount);
        eventCrf.setStatusId(Status.AVAILABLE.getCode());
        eventCrf.setCompletionStatus(completionStatusDao.findByCompletionStatusId(1));// setCompletionStatusId(1);
        eventCrf.setStudySubject(studySubject);
        eventCrf.setStudyEvent(studyEvent);
        eventCrf.setValidateString("");
        eventCrf.setValidatorAnnotations("");
        eventCrf.setDateUpdated(new Date());
        eventCrf.setValidatorId(0);
        eventCrf.setOldStatusId(0);
        eventCrf.setSdvUpdateId(0);
        return eventCrf;
    }

    private EventCrf updateEventCrf(EventCrf eventCrf, UserAccount userAccount, Status formStatus) {
        eventCrf.setDateUpdated(new Date());
        eventCrf.setUpdateId(userAccount.getUserId());
        eventCrf.setOldStatusId(eventCrf.getStatusId());
        eventCrf.setStatusId(formStatus.getCode());

        return eventCrf;
    }


    private StudyEvent createStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, int ordinal,
                                        UserAccount userAccount, String startDate) {

        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(ordinal);
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.SCHEDULED.getCode());
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setStudySubject(studySubject);
        studyEvent.setDateCreated(new Date());
        studyEvent.setUserAccount(userAccount);
        studyEvent.setDateStart(null);

        if (startDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate localDate = LocalDate.parse(startDate, formatter);
            Date date = java.sql.Date.valueOf(localDate);
            studyEvent.setDateStart(date);
        }
        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        return studyEvent;
    }

    private StudyEvent updateStudyEvent(StudyEvent studyEvent, UserAccount userAccount) {
        studyEvent.setDateUpdated(new Date());
        studyEvent.setUpdateId(userAccount.getUserId());
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.DATA_ENTRY_STARTED.getCode());
        return studyEvent;
    }

    private ErrorObj createErrorObj(String code, String message) {
        ErrorObj errorObj = new ErrorObj();
        errorObj.setCode(code);
        errorObj.setMessage(message);
        return errorObj;
    }

    private ErrorObj validateItemDataType(Item item, String value) {
        ItemDataType itemDataType = item.getItemDataType();
        switch (itemDataType.getCode()) {
            case "BL":
                return validateForBoolean(value);
            case "ST":
                return null;
            case "INT":
                return validateForInteger(value);
            case "REAL":
                return validateForReal(value);
            case "DATE":
                return validateForDate(value);
            case "PDATE":
                return new ErrorObj(FAILED, "errorCode.itemTypeNotSupportedInImport");
            case "FILE":
                return new ErrorObj(FAILED, "errorCode.itemTypeNotSupportedInImport");
            default:
                return new ErrorObj(FAILED, "errorCode.itemTypeNotSupportedInImport");
        }

    }


    private ErrorObj validateForBoolean(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            return new ErrorObj(FAILED, "errorCode.valueTypeMismatch");
        }
        return null;

    }

    private ErrorObj validateForInteger(String value) {
        try {
            Integer int1 = Integer.parseInt(value);
            return null;
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return new ErrorObj(FAILED, "errorCode.valueTypeMismatch");
        }
    }

    private ErrorObj validateForReal(String value) {
        if (isNumeric(value))
            return null;
        else
            return new ErrorObj(FAILED, "errorCode.valueTypeMismatch");
    }

    private boolean isNumeric(String str) {
        return str.matches("^\\d+(\\.\\d+)?");
    }

    private ErrorObj validateForDate(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(value, formatter);
            return null;
        } catch (Exception pe) {
            pe.getStackTrace();
            return new ErrorObj(FAILED, "errorCode.invalidDateFormat");
        }
    }


    private ErrorObj validateResponseSets(ResponseSet responseSet, String value) {
        ResponseType responseType = responseSet.getResponseType();
        switch (responseType.getName()) {
            case ("checkbox"):
                return validateCheckBoxOrMultiSelect(responseSet, value);
            case ("multi-select"):
                return validateCheckBoxOrMultiSelect(responseSet, value);
            case ("radio"):
                return validateRadioOrSingleSelect(responseSet, value);
            case ("single-select"):
                return validateRadioOrSingleSelect(responseSet, value);
            case ("text"):
                return null;
            case ("textarea"):
                return null;
            case ("file"):
                return new ErrorObj(FAILED, "errorCode.itemTypeNotSupportedInImport");
            case ("calculation"):
                return new ErrorObj(FAILED, "errorCode.itemTypeNotSupportedInImport");
            default:
                return new ErrorObj(FAILED, "errorCode.itemTypeNotSupportedInImport");

        }
    }


    private ErrorObj validateRadioOrSingleSelect(ResponseSet responseSet, String value) {
        if (!responseSet.getOptionsValues().contains(value)) {
            return new ErrorObj(FAILED, "errorCode.valueChoiceCodeNotFound");
        }
        return null;
    }

    private ErrorObj validateCheckBoxOrMultiSelect(ResponseSet responseSet, String value) {
        String[] values = value.split(",");
        ArrayList list = new ArrayList(Arrays.asList(values));

        for (String v : values) {
            if (!responseSet.getOptionsValues().contains(v)) {
                return new ErrorObj(FAILED, "errorCode.valueChoiceCodeNotFound");
            }
        }
        return null;
    }


}
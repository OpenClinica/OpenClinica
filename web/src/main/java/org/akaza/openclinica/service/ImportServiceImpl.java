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
import org.akaza.openclinica.core.EmailEngine;
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
    public static final String SUCCESS = "Success";


    @Transactional
    public ErrorObj validateAndProcessDataImport(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail, boolean process) {
        CoreResources.setRequestSchema(schema);
        Study tenantStudy = null;
        if (siteOid != null) {
            tenantStudy = studyDao.findByOcOID(siteOid);
        } else {
            tenantStudy = studyDao.findByOcOID(studyOid);
        }
        if (tenantStudy == null) {
            logger.debug("Study {} Not Valid", tenantStudy.getOc_oid());
            return createErrorObj("errorCode.studyNotExist", "Study " + tenantStudy.getOc_oid() + " does not exist");
        }

        DataImportHelper dataImportHelper = new DataImportHelper();
        List<DataImportReport> dataImportReports = new ArrayList<>();

        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        String uniqueIdentifier = tenantStudy.getStudy() == null ? tenantStudy.getUniqueIdentifier() : tenantStudy.getStudy().getUniqueIdentifier();
        String envType = tenantStudy.getStudy() == null ? tenantStudy.getEnvType().toString() : tenantStudy.getStudy().getEnvType().toString();

        String fileName = uniqueIdentifier + DASH + envType + UNDERSCORE + JobType.XML_IMPORT + new SimpleDateFormat("_yyyy-MM-dd-hhmmssS'.txt'").format(new Date());

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            String subjectOid = subjectDataBean.getSubjectOID();
            dataImportHelper.setSubjectKey(subjectOid);

            // Study Subject invalid OID
            StudySubject studySubject = studySubjectDao.findByOcOID(subjectOid);
            if (studySubject == null || (studySubject != null && studySubject.getStudy().getStudyId() != tenantStudy.getStudyId())) {
                logger.debug("SubjectKey {} Not Found", subjectOid);
                return createErrorObj("errorCode.participantNotFound", "SubjectKey " + subjectOid + " Not Found");
            }

            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                String sedOid = studyEventDataBean.getStudyEventOID();
                dataImportHelper.setStudyEventOID(sedOid);

                // StudyEventDefinition invalid OID
                StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByOcOID(sedOid);
                if (studyEventDefinition == null) {
                    logger.debug("StudEventOID {} for SubjectKey {} is not valid", sedOid, subjectOid);
                    return createErrorObj("errorCode.invalidStudyEventOID", "StudEventOID " + sedOid + " for SubjectKey " + subjectOid + " is not valid");
                }

                // Study Event Repeat key is not an int number
                int seRepeatKey = 0;
                try {
                    seRepeatKey = Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey());
                    dataImportHelper.setStudyEventRepeatKey(String.valueOf(seRepeatKey));
                } catch (NumberFormatException nfe) {
                    nfe.getStackTrace();
                    logger.debug("StudyEventRepeatKey {} for SubjectKey {} and StudyEventOID {} is not Valid", studyEventDataBean.getStudyEventRepeatKey(), subjectOid, sedOid);
                    return createErrorObj("errorCode.repeatKeyNotValid", "StudyEventRepeatKey " + studyEventDataBean.getStudyEventRepeatKey() + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Not Valid");
                }

                // Study Event Repeat key is Less than 1
                if (seRepeatKey < 1) {
                    logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Less Than 1", seRepeatKey, subjectOid, sedOid);
                    return createErrorObj("errorCode.repeatKeyLessThanOne", "RepeatKey " + seRepeatKey + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Less Than 1");
                }

                // Study Event Type is Visit Base and Not Scheduled
                StudyEvent studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(sedOid, seRepeatKey, studySubject.getStudySubjectId());
                if (studyEventDefinition.getType().equals(UNSCHEDULED) && studyEvent == null) {
                    logger.debug("StudEventOID {} for SubjectKey {} is not Scheduled", sedOid, subjectOid);
                    return createErrorObj("errorCode.visitNotScheduled", "StudEventOID " + sedOid + " for SubjectKey " + subjectOid + " is not scheduled");
                }

                //Study Event Type is Visit Base , Non Repeating and Repeat key is >1
                if (studyEventDefinition.getType().equals(UNSCHEDULED) && !studyEventDefinition.getRepeating() && seRepeatKey > 1) {
                    logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Larger Than 1", seRepeatKey, subjectOid, sedOid);
                    return createErrorObj("errorCode.repeatKeyLargerThanOne", "Study Event RepeatKey " + seRepeatKey + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Larger Than 1");

                }
                // Study Event is Repeating and Repeat Key is not next Ordinal
                int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());
                if (studyEventDefinition.getType().equals(COMMON) && seRepeatKey != maxSeOrdinal + 1) {
                    logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Too Large Or not Valid", seRepeatKey, subjectOid, sedOid);
                    return createErrorObj("errorCode.repeatKeyTooLarge", "Study Event RepeatKey " + seRepeatKey + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " too Large");
                }


                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                for (FormDataBean formDataBean : formDataBeans) {
                    String formOid = formDataBean.getFormOID();
                    dataImportHelper.setFormOID(formOid);
                    String formLayoutName = formDataBean.getFormLayoutName();
                    dataImportHelper.setFormLayoutOID(formLayoutName);

                    // Form Invalid OID
                    CrfBean crf = crfDao.findByOcOID(formOid);
                    if (crf == null) {
                        logger.debug("FormOid {} for SubjectKey {} and StudyEventOID {} is not Valid", formOid, subjectOid, sedOid);
                        return createErrorObj("errorCode.formOIDNotFound", "FormOid " + formOid + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Not Valid");
                    }
                    // Form Invalid OID
                    EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                            tenantStudy.getStudy() == null ? tenantStudy.getStudyId() : tenantStudy.getStudy().getStudyId());
                    if (edc == null) {
                        logger.debug("FormOid {} for SubjectKey {} and StudyEventOID {} is not Valid", formOid, subjectOid, sedOid);
                        return createErrorObj("errorCode.formOIDNotFound", "FormOid " + formOid + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Not Valid");
                    }

                    // FormLayout Invalid OID
                    FormLayout formLayout = formLayoutDao.findByNameCrfId(formLayoutName, crf.getCrfId());
                    if (formLayout == null) {
                        logger.debug("FormLayoutOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", formLayoutName, subjectOid, sedOid, formOid);
                        return createErrorObj("errorCode.formLayoutOIDNotFound", "FormLayoutOid " + formLayoutName + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " and FormOID " + formOid + " is Not Valid");
                    }

                    // Form Status is null , then Form has Initial Data Entry Status
                    if (formDataBean.getEventCRFStatus() == null) {
                        formDataBean.setEventCRFStatus(INITIAL_DATA_ENTRY);
                    }

                    // Form Status is not acceptable
                    if (!formDataBean.getEventCRFStatus().equals(INITIAL_DATA_ENTRY) &&
                            !formDataBean.getEventCRFStatus().equals(INITIAL_DATA_ENTRY) &&
                            !formDataBean.getEventCRFStatus().equals(INITIAL_DATA_ENTRY)) {
                        logger.debug("Form Status {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", formDataBean.getEventCRFStatus(), subjectOid, sedOid, formOid);
                        return createErrorObj("errorCode.formStatusNotValid", "Form Status " + formDataBean.getEventCRFStatus() + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + "and FormOID " + formOid + " is Not Valid\n Acceptable Options 'initial data entry','data entry complete' and 'complete' ");
                    }
                    dataImportHelper.setFormStatus(formDataBean.getEventCRFStatus());

                    // Event Crf has status complete
                    EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());
                    if (eventCrf != null && eventCrf.getStatusId() == Status.UNAVAILABLE.getCode()) {
                        logger.debug("Form {} for SubjectKey {} and StudyEventOID {} and FormOID {} already complete", formOid, subjectOid, sedOid, formOid);
                        return createErrorObj("errorCode.errorCode.formAlreadyComplete", "FormOid " + formOid + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + "and FormOID " + formOid + " is already complete");
                    }


                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                    for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                        String itemGroupOID = itemGroupDataBean.getItemGroupOID();
                        dataImportHelper.setItemGroupOID(itemGroupOID);

                        //Item Group invalid Oid
                        ItemGroup itemGroup = itemGroupDao.findByOcOID(itemGroupOID);
                        if (itemGroup == null) {
                            logger.debug("ItemGroupOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", itemGroupOID, subjectOid, sedOid, formOid);
                            return createErrorObj("errorCode.itemGroupOIDNotFound", "ItemGroupOid " + itemGroupOID + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + "and FormOID " + formOid + " is Not Valid");
                        }
                        //Item Group invalid Oid
                        ItemGroup itmGroup = itemGroupDao.findByNameCrfId(itemGroup.getName(), crf);
                        if (itmGroup == null) {
                            logger.debug("ItemGroupOid {} for SubjectKey {} and StudyEventOID {} and FormOID {} is not Valid", itemGroupOID, subjectOid, sedOid, formOid);
                            return createErrorObj("errorCode.itemGroupOIDNotFound", "ItemGroupOid " + itemGroupOID + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + "and FormOID " + formOid + " is Not Valid");
                        }

                        // Item Group Repeat key is not an int number
                        int groupRepeatKey = 0;
                        try {
                            groupRepeatKey = Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey());
                            dataImportHelper.setItemGroupRepeatKey(String.valueOf(groupRepeatKey));

                        } catch (NumberFormatException nfe) {
                            nfe.getStackTrace();
                            logger.debug("ItemGroupRepeatKey {} for SubjectKey {} and StudyEventOID {} and FormOid {} is not Valid", itemGroupDataBean.getItemGroupRepeatKey(), subjectOid, sedOid, formOid);
                            return createErrorObj("errorCode.groupRepeatKeyNotValid", "GroupRepeatKey " + itemGroupDataBean.getItemGroupRepeatKey() + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " and FormOID " + formOid + " is NotValid ");
                        }


                        // Item Group Repeat key is Less than 1
                        if (groupRepeatKey < 1) {
                            logger.debug("Group RepeatKey {} for SubjectKey {} and StudyEventOID {} is Less Than 1", groupRepeatKey, subjectOid, sedOid);
                            return createErrorObj("errorCode.groupRepeatKeyLessThanOne", "ItemGroupRepeatKey " + groupRepeatKey + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Less Than 1");
                        }


                        //Item Group is Non Repeating and Repeat key is >1
                        if (!itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup() && groupRepeatKey > 1) {
                            logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Larger Than 1", seRepeatKey, subjectOid, sedOid);
                            return createErrorObj("errorCode.repeatKeyLargerThanOne", "Study Event RepeatKey " + seRepeatKey + " for SubjectKey " + subjectOid + " and StudyEventOID " + sedOid + " is Larger Than 1");

                        }


                        /// From here down, Error message will be part of LogFile
                        dataImportHelper.setStudyOid(studyOid);
                        dataImportHelper.setJobDetail(jobDetail);
                        dataImportHelper.setFileName(fileName);
                        dataImportHelper.setStudyEventDefinition(studyEventDefinition);
                        dataImportHelper.setUserAccount(userAccount);
                        dataImportHelper.setItemGroupDataBean(itemGroupDataBean);
                        dataImportHelper.setStudySubject(studySubject);
                        dataImportHelper.setStudyEvent(studyEvent);
                        dataImportHelper.setFormLayout(formLayout);
                        dataImportHelper.setEventCrf(eventCrf);
                        dataImportHelper.setCrf(crf);
                        dataImportHelper.setItemGroup(itemGroup);
                        dataImportHelper.setDataImportReports(dataImportReports);
                        if (process) {
                            dataImportReports = process(dataImportHelper);
                        }

                    } //itemGroupDataBean for loop
                } // formDataBean for loop
            } // StudyEventDataBean for loop
        } // StudySubjectDataBean for loop
        if (process) {
            writeToFile(dataImportReports, studyOid, fileName);
            userService.persistJobCompleted(jobDetail, fileName);
        }
        return null;
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
        stringBuffer.append("StudyEventOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("StudyEventRepeatKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("FormOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("FormLayoutOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("FormStatus");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemGroupOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemGroupRepeatKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Message");
        stringBuffer.append('\n');
        for (DataImportReport dataImportReport : dataImportReports) {
            stringBuffer.append(dataImportReport.getSubjectKey() != null ? dataImportReport.getSubjectKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudyEventOID() != null ? dataImportReport.getStudyEventOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudyEventRepeatKey() != null ? dataImportReport.getStudyEventRepeatKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getFormOID() != null ? dataImportReport.getFormOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getFormLayoutOID() != null ? dataImportReport.getFormLayoutOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getFormStatus() != null ? dataImportReport.getFormStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemGroupOID() != null ? dataImportReport.getItemGroupOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemGroupRepeatKey() != null ? dataImportReport.getItemGroupRepeatKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemOID() != null ? dataImportReport.getItemOID() : "");
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

    private void updateItemData(ItemData itemData) {
        itemData.setOldStatus(itemData.getStatus());
        itemData.setDateUpdated(new Date());
        //   itemData.setUpdateId();
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
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        logger.debug("*********CREATED EVENT CRF");
        return eventCrf;
    }

    private void updateEventCrf() {

    }


    private StudyEvent createStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, int maxOrdinal,
                                        UserAccount userAccount) {

        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(maxOrdinal + 1);
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.NOT_SCHEDULED.getCode());
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setStudySubject(studySubject);
        studyEvent.setDateCreated(new Date());
        studyEvent.setUserAccount(userAccount);
        studyEvent.setDateStart(null);
        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        return studyEvent;
    }


    private void updateStudyEvent() {

    }

    private List<DataImportReport> process(DataImportHelper dataImportHelper) {
        String studyOid = dataImportHelper.getStudyOid();
        String subjectOid = dataImportHelper.getSubjectKey();
        String sedOid = dataImportHelper.getStudyEventOID();
        String formOid = dataImportHelper.getFormOID();
        String itemGroupOid = dataImportHelper.getItemGroupOID();
        JobDetail jobDetail = dataImportHelper.getJobDetail();
        String fileName = dataImportHelper.getFileName();
        String formStatus = dataImportHelper.getFormStatus();
        StudyEventDefinition studyEventDefinition = dataImportHelper.getStudyEventDefinition();
        int seRepeatKey = Integer.parseInt(dataImportHelper.getStudyEventRepeatKey());
        int groupRepeatKey = Integer.parseInt(dataImportHelper.getItemGroupRepeatKey());
        UserAccount userAccount = dataImportHelper.getUserAccount();
        ImportItemGroupDataBean itemGroupDataBean = dataImportHelper.getItemGroupDataBean();
        StudySubject studySubject = dataImportHelper.getStudySubject();
        StudyEvent studyEvent = dataImportHelper.getStudyEvent();
        FormLayout formLayout = dataImportHelper.getFormLayout();
        EventCrf eventCrf = dataImportHelper.getEventCrf();
        CrfBean crf = dataImportHelper.getCrf();
        ItemGroup itemGroup = dataImportHelper.getItemGroup();
        List<DataImportReport> dataImportReports = dataImportHelper.getDataImportReports();

        DataImportReport dataImportReport = null;

        ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();

        // Study Event is Repeating and Repeat Key is not next Ordinal
/*        int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());
        if (studyEventDefinition.getRepeating() && seRepeatKey != maxSeOrdinal + 1) {
            logger.debug("RepeatKey {} for SubjectKey {} and StudyEventOID {} is Too Large Or not Valid", seRepeatKey, subjectOid, sedOid);
            dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, null, formStatus, FAILED, "errorCode.invalidItemGroupRepeatKey ");
            dataImportReports.add(dataImportReport);
        }*/

        if (studyEventDefinition.getType().equals(COMMON) && studyEvent == null) {
            studyEvent = createStudyEvent(studySubject, studyEventDefinition, seRepeatKey, userAccount);
            studyEvent = studyEventDao.saveOrUpdate(studyEvent);
            logger.debug("Scheduling new Common Event");
        }

        if (eventCrf == null) {
            eventCrf = createEventCrf(studySubject, studyEvent, formLayout, userAccount);
            eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        }

        for (ImportItemDataBean itemDataBean : itemDataBeans) {
            String itemOID = itemDataBean.getItemOID();
            dataImportHelper.setItemOID(itemOID);
            Item item = itemDao.findByOcOID(itemOID);

            // ItemOID is not valid
            if (item == null) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, "errorCode.itemNotFound ");
                dataImportReports.add(dataImportReport);
                continue;

            }
            Item itm = itemDao.findByNameCrfId(item.getName(), crf.getCrfId());
            // ItemOID is not valid
            if (itm == null) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, "errorCode.itemNotFound ");
                dataImportReports.add(dataImportReport);
                continue;
            }

            // ItemGroup is Repeating and Repeat Key is not next Ordinal
            int maxRepeatGroup = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), item.getItemId());
            if (itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup() && groupRepeatKey != maxRepeatGroup + 1) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, "errorCode.invalidItemGroupRepeatKey ");
                dataImportReports.add(dataImportReport);
                continue;
            }

            if (itemDataBean.getValue().length() > 3999) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, "errorCode.valueTooLong ");
                dataImportReports.add(dataImportReport);
                continue;
            }

            ErrorObj itemDataTypeErrorObj = validateItemDataType(item, itemDataBean.getValue());
            if (itemDataTypeErrorObj != null) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, itemDataTypeErrorObj.getMessage());
                dataImportReports.add(dataImportReport);
                continue;
            }

            Set<ItemFormMetadata> ifms = item.getItemFormMetadatas();
            ResponseSet responseSet = ifms.iterator().next().getResponseSet();
            ErrorObj responseSetErrorObj = validateResponseSets(responseSet, itemDataBean.getValue());
            if (responseSetErrorObj != null) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, responseSetErrorObj.getMessage());
                dataImportReports.add(dataImportReport);
                continue;
            }

            ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), groupRepeatKey);
            if (item != null) {
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, FAILED, "errorCode:itemExists");
                dataImportReports.add(dataImportReport);
            } else {
                itemData = createItemData(eventCrf, itemDataBean, userAccount, item, groupRepeatKey);
                itemData = itemDataDao.saveOrUpdate(itemData);
                dataImportReport = new DataImportReport(subjectOid, sedOid, seRepeatKey, formOid, formLayout.getName(), itemGroupOid, groupRepeatKey, itemOID, formStatus, SUCCESS, "");
                dataImportReports.add(dataImportReport);
            }
        }
        return dataImportReports;
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
        try {
            Float flaot1 = Float.parseFloat(value);
            return null;
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return new ErrorObj(FAILED, "errorCode.valueTypeMismatch");
        }
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
                return new ErrorObj(FAILED, "errorCode.readOnlyItem");
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
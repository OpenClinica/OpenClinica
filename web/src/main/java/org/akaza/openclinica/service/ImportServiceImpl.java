package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.crfdata.*;
import org.akaza.openclinica.controller.dto.*;
import org.akaza.openclinica.controller.openrosa.OpenRosaSubmissionController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    VersioningMapDao versioningMapDao;

    @Autowired
    OpenRosaSubmissionController openRosaSubmissionController;

    public static final String COMMON = "common";
    public static final String UNSCHEDULED = "unscheduled";
    public static final String SEPERATOR = ",";
    public static final String BULK_JOBS = "bulk_jobs";
    public static final String DASH = "-";
    public static final String UNDERSCORE = "_";
    public static final String INITIAL_DATA_ENTRY = "initial data entry";
    public static final String DATA_ENTRY_COMPLETE = "data entry complete";
    public static final String COMPLETE = "complete";

    public static final String DATA_ENTRY_STARTED = "data entry started";
    public static final String COMPLETED = "completed";
    public static final String STOPPED = "stopped";
    public static final String SKIPPED = "skipped";
    public static final String LOCKED = "locked";

    public static final String FAILED = "Failed";
    public static final String INSERTED = "Inserted";
    public static final String UPDATED = "Updated";
    public static final String NO_CHANGE = "No Change";
    List<DataImportReport> dataImportReports = null;
    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    SimpleDateFormat sdf_logFile = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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
            logger.error("Study {} Not Valid", tenantStudy.getOc_oid());
        }

        dataImportReports = new ArrayList<>();
        DataImportReport dataImportReport = null;
        sdf_logFile.setTimeZone(TimeZone.getTimeZone("GMT"));

        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        String uniqueIdentifier = tenantStudy.getStudy() == null ? tenantStudy.getUniqueIdentifier() : tenantStudy.getStudy().getUniqueIdentifier();
        String envType = tenantStudy.getStudy() == null ? tenantStudy.getEnvType().toString() : tenantStudy.getStudy().getEnvType().toString();

        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        String fileName = uniqueIdentifier + DASH + envType + UNDERSCORE + JobType.XML_IMPORT + "_" + sdf_fileName.format(new Date()) + ".csv";

        logger.debug("Job Filename is : {}", fileName);


        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        if (subjectDataBeans != null) {
            for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                if (subjectDataBean.getSubjectOID() != null)
                    subjectDataBean.setSubjectOID(subjectDataBean.getSubjectOID().toUpperCase());
                StudySubject studySubject = null;
                StudySubject studySubject02 = null;


                if (subjectDataBean.getSubjectOID() == null && subjectDataBean.getStudySubjectID() == null) {
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_MISSING_PARTICIPANT_ID);
                    dataImportReports.add(dataImportReport);
                    logger.info("Participant SubjectKey and StudySubjectID are null ");
                    continue;
                } else if (subjectDataBean.getSubjectOID() != null && subjectDataBean.getStudySubjectID() == null) {
                    studySubject = studySubjectDao.findByOcOID(subjectDataBean.getSubjectOID());
                    if (studySubject == null
                            || (studySubject != null && tenantStudy.getStudy() != null && studySubject.getStudy().getStudyId() != tenantStudy.getStudyId())
                            || (studySubject != null && tenantStudy.getStudy() == null && studySubject.getStudy().getStudy() != null && studySubject.getStudy().getStudy().getStudyId() != tenantStudy.getStudyId())) {

                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
                        dataImportReports.add(dataImportReport);
                        logger.error("Participant SubjectKey {} Not Found", subjectDataBean.getSubjectOID());
                        continue;
                    }
                } else if (subjectDataBean.getSubjectOID() == null && subjectDataBean.getStudySubjectID() != null) {
                    try {
                        studySubject = studySubjectDao.findByLabelAndStudyOrParentStudy(subjectDataBean.getStudySubjectID(), tenantStudy);
                        if (studySubject == null) {
                            List<StudySubject> studySubjects = (studySubjectDao.findByLabelAndParentStudy(subjectDataBean.getStudySubjectID(), tenantStudy));
                            if (studySubjects != null && studySubjects.size() > 0)
                                studySubject = studySubjects.get(0);
                        }
                        if (studySubject == null) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
                            dataImportReports.add(dataImportReport);
                            logger.error("Participant StudySubjectID {} Not Found", subjectDataBean.getStudySubjectID());
                            continue;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_MULTIPLE_PARTICIPANTS_FOUND);
                        dataImportReports.add(dataImportReport);
                        logger.error("multipleParticipantsFound {}", e.getMessage());
                        continue;
                    }

                } else if (subjectDataBean.getSubjectOID() != null && subjectDataBean.getStudySubjectID() != null) {
                    studySubject = studySubjectDao.findByOcOID(subjectDataBean.getSubjectOID());
                    studySubject02 = studySubjectDao.findByLabelAndStudyOrParentStudy(subjectDataBean.getStudySubjectID(), tenantStudy);
                    if (studySubject02 == null) {
                        List<StudySubject> studySubjects = (studySubjectDao.findByLabelAndParentStudy(subjectDataBean.getStudySubjectID(), tenantStudy));
                        if (studySubjects != null && studySubjects.size() > 0)
                            studySubject02 = studySubjects.get(0);
                    }

                    if (studySubject == null || studySubject02 == null) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
                        dataImportReports.add(dataImportReport);
                        logger.error("Participant Identifiers {} {} mismatch", subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID());
                        continue;
                    }

                    if (studySubject != null && studySubject02 != null && studySubject.getStudySubjectId() != studySubject02.getStudySubjectId()) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_PARTICIPANT_IDENTIFIERS_MISMATCH);
                        dataImportReports.add(dataImportReport);
                        logger.error("Participant Identifiers {} {} mismatch", subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID());
                        continue;
                    }
                }
                subjectDataBean.setSubjectOID(studySubject.getOcOid());
                subjectDataBean.setStudySubjectID(studySubject.getLabel());

                ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
                for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                    if (studyEventDataBean.getStudyEventOID() != null)
                        studyEventDataBean.setStudyEventOID(studyEventDataBean.getStudyEventOID().toUpperCase());
                    // OID is missing
                    if (studyEventDataBean.getStudyEventOID() == null) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, FAILED, null, ErrorConstants.ERR_MISSING_STUDYEVENTOID);
                        dataImportReports.add(dataImportReport);
                        logger.error("StudEventOID {} is not valid", studyEventDataBean.getStudyEventOID());
                        continue;
                    }

                    // StudyEventDefinition invalid OID and Archived
                    StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByOcOID(studyEventDataBean.getStudyEventOID());
                    if (studyEventDefinition == null || (studyEventDefinition != null && !studyEventDefinition.getStatus().equals(Status.AVAILABLE))) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, FAILED, null, ErrorConstants.ERR_INVALID_STUDYEVENTOID);
                        dataImportReports.add(dataImportReport);
                        logger.error("StudEventOID {} is not valid or Archived", studyEventDataBean.getStudyEventOID());
                        continue;
                    }


                    StudyEvent studyEvent = validateStudyEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    if (studyEvent == null) {
                        continue;
                    }

                    ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                    for (FormDataBean formDataBean : formDataBeans) {
                        if (formDataBean.getFormOID() != null)
                            formDataBean.setFormOID(formDataBean.getFormOID().toUpperCase());
                        if (formDataBean.getEventCRFStatus() != null)
                            formDataBean.setEventCRFStatus(formDataBean.getEventCRFStatus().toLowerCase());

                        if (formDataBean.getFormOID() == null) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_MISSING_FORMOID);
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOid {} for SubjectKey {} and StudyEventOID {} is not Valid", formDataBean.getFormOID(), subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID());
                            continue;
                        }


                        // Form Invalid OID and form not Archived
                        CrfBean crf = crfDao.findByOcOID(formDataBean.getFormOID());
                        if (crf == null || (crf != null && !crf.getStatus().equals(Status.AVAILABLE))) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_FORMOID_NOT_FOUND);
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOid {} is not Valid or not Found", formDataBean.getFormOID());
                            continue;
                        }
                        // Form Invalid OID
                        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                                tenantStudy.getStudy() == null ? tenantStudy.getStudyId() : tenantStudy.getStudy().getStudyId());
                        if (edc == null || (edc != null && !edc.getStatusId().equals(Status.AVAILABLE.getCode()))) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_FORMOID_NOT_FOUND);
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOid {} is not Valid or not Found", formDataBean.getFormOID());
                            continue;

                        }

                        if (formDataBean.getFormLayoutName() == null) {
                            formDataBean.setFormLayoutName(edc.getFormLayout().getName());
                        }

                        // FormLayout Invalid OID
                        FormLayout formLayout = formLayoutDao.findByNameCrfId(formDataBean.getFormLayoutName(), crf.getCrfId());
                        if (formLayout == null || (formLayout != null && !formLayout.getStatus().equals(Status.AVAILABLE))) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_FORMLAYOUTOID_NOT_FOUND);
                            dataImportReports.add(dataImportReport);
                            logger.error("FormLayoutOid {} is not Valid or not Found", formDataBean.getFormLayoutName());
                            continue;

                        }

                        // Form Status is null , then Form has Initial Data Entry Status
                        if (formDataBean.getEventCRFStatus() == null) {
                            formDataBean.setEventCRFStatus(INITIAL_DATA_ENTRY);
                        }

                        // Form Status is not acceptable
                        if (!formDataBean.getEventCRFStatus().equalsIgnoreCase(INITIAL_DATA_ENTRY) &&
                                !formDataBean.getEventCRFStatus().equalsIgnoreCase(DATA_ENTRY_COMPLETE) &&
                                !formDataBean.getEventCRFStatus().equalsIgnoreCase(COMPLETE)) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_FORM_STATUS_NOT_VALID);
                            dataImportReports.add(dataImportReport);
                            logger.error("Form Status {}  is not Valid", formDataBean.getEventCRFStatus());
                            continue;
                        }

                        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());

                        // Event Crf has status complete
                        if (eventCrf != null && eventCrf.getStatusId().equals(Status.UNAVAILABLE.getCode())) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_FORM_ALREADY_COMPLETE);
                            dataImportReports.add(dataImportReport);
                            logger.debug("Form {}  already complete", formDataBean.getFormOID());
                            continue;
                        }

                        if (eventCrf == null && studyEventDefinition.getType().equals(UNSCHEDULED)) {
                            List<EventCrf> eventCrfs= eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(studyEvent.getStudyEventId(),studySubject.getStudySubjectId(),crf.getCrfId());
                            if (eventCrfs.size() > 0) {
                                eventCrf = eventCrfs.get(0);
                                formDataBean.setFormLayoutName(eventCrf.getFormLayout().getXformName());
                            }
                        }

                        if (eventCrf == null && studyEventDefinition.getType().equals(COMMON) && studyEventDefinition.getRepeating()) {
                            List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubject.getOcOid());
                            if (eventCrfs.size() > 0) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_REPEAT_KEY_AND_FORM_MISMATCH);
                                dataImportReports.add(dataImportReport);
                                logger.debug("Form {}  already complete", formDataBean.getFormOID());
                                continue;
                            }
                        }


                        if (eventCrf == null) {
                            eventCrf = createEventCrf(studySubject, studyEvent, formLayout, userAccount);
                            eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
                            logger.debug("new EventCrf Id {} is created  ", eventCrf.getEventCrfId());

                            studyEvent = updateStudyEvent(studyEvent, userAccount, SubjectEventStatus.DATA_ENTRY_STARTED, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
                            studyEvent = studyEventDao.saveOrUpdate(studyEvent);
                            logger.debug("Study Event Id {} is updated", studyEvent.getStudyEventId());
                        }

                        ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();

                        int itemCountInFormData = 0;
                        int itemInsertedUpdatedCountInFrom = 0;
                        int itemInsertedUpdatedSkippedCountInFrom = 0;

                        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                            itemCountInFormData = itemCountInFormData + itemGroupDataBean.getItemData().size();
                        }

                        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                            if (itemGroupDataBean.getItemGroupOID() != null)
                                itemGroupDataBean.setItemGroupOID(itemGroupDataBean.getItemGroupOID().toUpperCase());
                            if (itemGroupDataBean.getItemGroupOID() == null) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), null, null, FAILED, null, ErrorConstants.ERR_ITEMGROUPOID_NOT_FOUND);
                                dataImportReports.add(dataImportReport);
                                logger.error("ItemGroupOid {} is not Valid or not found", itemGroupDataBean.getItemGroupOID());
                                continue;
                            }

                            //Item Group invalid Oid
                            ItemGroup itemGroup = itemGroupDao.findByOcOID(itemGroupDataBean.getItemGroupOID());
                            if (itemGroup == null || (itemGroup != null && !itemGroup.getStatus().equals(Status.AVAILABLE))) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), null, null, FAILED, null, ErrorConstants.ERR_ITEMGROUPOID_NOT_FOUND);
                                dataImportReports.add(dataImportReport);
                                logger.error("ItemGroupOid {} is not Valid or not found", itemGroupDataBean.getItemGroupOID());
                                continue;
                            }
                            //Item Group invalid Oid in Form
                            ItemGroup itmGroup = itemGroupDao.findByNameCrfId(itemGroup.getName(), crf);
                            if (itmGroup == null) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), null, null, FAILED, null, ErrorConstants.ERR_ITEMGROUPOID_NOT_FOUND);
                                dataImportReports.add(dataImportReport);
                                logger.error("ItemGroupOid {} is not Valid or not found", itemGroupDataBean.getItemGroupOID());
                                continue;
                            }


                            itemGroup = validateItemGroupRepeat(studySubject, studyEventDataBean, formDataBean, eventCrf, itemGroupDataBean, itemGroup, userAccount);
                            if (itemGroup == null) {
                                continue;
                            }


                            ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                            for (ImportItemDataBean itemDataBean : itemDataBeans) {
                                if (itemDataBean.getItemOID() != null)
                                    itemDataBean.setItemOID(itemDataBean.getItemOID().toUpperCase());
                                if (itemDataBean.getItemOID() == null) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, ErrorConstants.ERR_ITEM_NOT_FOUND);
                                    dataImportReports.add(dataImportReport);
                                    logger.error("Item {} is not found or invalid", itemDataBean.getItemOID());
                                    continue;
                                }

                                Item item = itemDao.findByOcOID(itemDataBean.getItemOID());

                                // ItemOID is not valid
                                if (item == null || (item != null && !item.getStatus().equals(Status.AVAILABLE))) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, ErrorConstants.ERR_ITEM_NOT_FOUND);
                                    dataImportReports.add(dataImportReport);
                                    logger.error("Item {} is not found or invalid", itemDataBean.getItemOID());
                                    continue;
                                }
                                Item itm = itemDao.findByNameCrfId(item.getName(), crf.getCrfId());
                                // ItemOID is not valid
                                if (itm == null) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, ErrorConstants.ERR_ITEM_NOT_FOUND);
                                    dataImportReports.add(dataImportReport);
                                    logger.error("Item {} is not found or invalid", itemDataBean.getItemOID());
                                    continue;
                                }
                                 if(itm!=null){
                                     // what version this item belongs to and if that version has status available or it is allowed to import?


                                 }


                                if (itemDataBean.getValue() == null) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, ErrorConstants.ERR_MISSING_VALUE);
                                    dataImportReports.add(dataImportReport);
                                    logger.error("Item {} value is missing", itemDataBean.getItemOID());
                                    continue;
                                }

                                if (itemDataBean.getValue().length() > 3999) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, ErrorConstants.ERR_VALUE_TOO_LONG);
                                    dataImportReports.add(dataImportReport);
                                    logger.error("Item {} value too long over 3999", itemDataBean.getItemOID());
                                    continue;
                                }

                                if (StringUtils.isNotEmpty(itemDataBean.getValue())) {
                                    ErrorObj itemDataTypeErrorObj = validateItemDataType(item, itemDataBean.getValue());
                                    if (itemDataTypeErrorObj != null) {
                                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, itemDataTypeErrorObj.getMessage());
                                        dataImportReports.add(dataImportReport);
                                        logger.error("Item {} data type error. {}", itemDataBean.getItemOID(), itemDataTypeErrorObj.getMessage());
                                        continue;
                                    }

                                    Set<ItemFormMetadata> ifms = item.getItemFormMetadatas();
                                    ResponseSet responseSet = ifms.iterator().next().getResponseSet();
                                    ErrorObj responseSetErrorObj = validateResponseSets(responseSet, itemDataBean.getValue());
                                    if (responseSetErrorObj != null) {
                                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), FAILED, null, responseSetErrorObj.getMessage());
                                        dataImportReports.add(dataImportReport);
                                        logger.error("Item {} response option text error. {}", itemDataBean.getItemOID(), responseSetErrorObj.getMessage());
                                        continue;
                                    }
                                }

                                ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));

                                if (itemData != null) {
                                    if (itemData.getValue().equals(itemDataBean.getValue())) {
                                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), NO_CHANGE, null, "");
                                        dataImportReports.add(dataImportReport);
                                        itemInsertedUpdatedSkippedCountInFrom++;
                                        logger.debug("Item {} value skipped ", itemDataBean.getItemOID());
                                    } else {
                                        itemData = updateItemData(itemData, userAccount, itemDataBean.getValue());
                                        itemData = itemDataDao.saveOrUpdate(itemData);
                                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), UPDATED, sdf_logFile.format(new Date()), "");
                                        dataImportReports.add(dataImportReport);
                                        itemInsertedUpdatedCountInFrom++;
                                        itemInsertedUpdatedSkippedCountInFrom++;
                                        logger.debug("Item {} value updated ", itemDataBean.getItemOID());
                                    }
                                } else {
                                    itemData = createItemData(eventCrf, itemDataBean, userAccount, item, Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
                                    itemData = itemDataDao.saveOrUpdate(itemData);
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), INSERTED, sdf_logFile.format(new Date()), "");
                                    dataImportReports.add(dataImportReport);
                                    itemInsertedUpdatedCountInFrom++;
                                    itemInsertedUpdatedSkippedCountInFrom++;
                                    logger.debug("Item {} value inserted ", itemDataBean.getItemOID());
                                }
                            }//itemDataBean for loop

                        } //itemGroupDataBean for loop


                        if ((formDataBean.getEventCRFStatus().equals(COMPLETE) || formDataBean.getEventCRFStatus().equals(DATA_ENTRY_COMPLETE)) && itemInsertedUpdatedSkippedCountInFrom == itemCountInFormData) {                         // update eventcrf status into Complete
                            // Update Event Crf Status into Complete
                            eventCrf = updateEventCrf(eventCrf, userAccount, Status.UNAVAILABLE);
                            // check if all Forms within this Event is Complete
                            openRosaSubmissionController.updateStudyEventStatus(tenantStudy.getStudy() != null ? tenantStudy.getStudy() : tenantStudy, studySubject, studyEventDefinition, studyEvent, userAccount);
                            logger.debug("Form {} status updated to Complete ", formDataBean.getFormOID());

                        } else if (itemInsertedUpdatedCountInFrom > 0) {                         // update eventcrf status into data entry status
                            // Update Event Crf Status into Initial Data Entry
                            eventCrf = updateEventCrf(eventCrf, userAccount, Status.AVAILABLE);
                        }
                    } // formDataBean for loop
                } // StudyEventDataBean for loop
            } // StudySubjectDataBean for loop
        } else { // subjectDataBean ==null
            dataImportReport = new DataImportReport(null, null, null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_SUBJECT_DATA_MISSING);
            dataImportReports.add(dataImportReport);
            logger.info("SubjectData is missing ");
        }

        writeToFile(dataImportReports, studyOid, fileName);
        userService.persistJobCompleted(jobDetail, fileName);

    }

    private void writeToFile(List<DataImportReport> dataImportReports, String studyOid, String fileName) {
        logger.debug("writing report to File");

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
        stringBuffer.append("ItemGroupOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemGroupRepeatKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ItemOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Timestamp");
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
            stringBuffer.append(dataImportReport.getItemGroupOID() != null ? dataImportReport.getItemGroupOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemGroupRepeatKey() != null ? dataImportReport.getItemGroupRepeatKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getItemOID() != null ? dataImportReport.getItemOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStatus() != null ? dataImportReport.getStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getTimeStamp() != null ? dataImportReport.getTimeStamp() : "");
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
        logger.debug("Creating new Item Data");
        return itemData;
    }

    private ItemData updateItemData(ItemData itemData, UserAccount userAccount, String value) {
        itemData.setValue(value);
        itemData.setOldStatus(itemData.getStatus());
        itemData.setDateUpdated(new Date());
        itemData.setUpdateId(userAccount.getUserId());
        logger.debug("Updating Item Data Id {}", itemData.getItemDataId());
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
        logger.debug("Creating new Event Crf");

        return eventCrf;
    }

    private EventCrf updateEventCrf(EventCrf eventCrf, UserAccount userAccount, Status formStatus) {
        eventCrf.setDateUpdated(new Date());
        eventCrf.setUpdateId(userAccount.getUserId());
        eventCrf.setOldStatusId(eventCrf.getStatusId());
        eventCrf.setStatusId(formStatus.getCode());
        logger.debug("Updating Event Crf Id {}", eventCrf.getEventCrfId());
        return eventCrf;
    }


    private StudyEvent createStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, int ordinal,
                                        UserAccount userAccount, String startDate, String endDate) {

        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(ordinal);
        studyEvent.setSubjectEventStatusId(SubjectEventStatus.SCHEDULED.getCode());
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setStudySubject(studySubject);
        studyEvent.setDateCreated(new Date());
        studyEvent.setUserAccount(userAccount);

        setEventStartAndEndDate(studyEvent, startDate, endDate);

        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        logger.debug("Creating new Study Event");
        return studyEvent;
    }

    private StudyEvent updateStudyEvent(StudyEvent studyEvent, UserAccount userAccount, SubjectEventStatus subjectEventStatus, String startDate, String endDate) {
        if (studyEvent.getSubjectEventStatusId() != subjectEventStatus.getCode()) {
            if ((subjectEventStatus.equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                    && (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SCHEDULED.getCode()
                    || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode()))
                    ||
                    (subjectEventStatus.equals(SubjectEventStatus.COMPLETED)
                            && studyEvent.getSubjectEventStatusId() == SubjectEventStatus.DATA_ENTRY_STARTED.getCode())
                    ||
                    (subjectEventStatus.equals(SubjectEventStatus.STOPPED)
                            && studyEvent.getSubjectEventStatusId() == SubjectEventStatus.DATA_ENTRY_STARTED.getCode())
                    ||
                    (subjectEventStatus.equals(SubjectEventStatus.SKIPPED)
                            && studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SCHEDULED.getCode())
                    ||
                    (subjectEventStatus.equals(SubjectEventStatus.LOCKED)
                            && (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.COMPLETED.getCode()
                            || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode()))
            ) {
                studyEvent.setSubjectEventStatusId(subjectEventStatus.getCode());
            } else {
                // Do not schedule or update

            }


        }


        setEventStartAndEndDate(studyEvent, startDate, endDate);


        studyEvent.setDateUpdated(new Date());
        studyEvent.setUpdateId(userAccount.getUserId());
        logger.debug("Updating Study Event Id {}", studyEvent.getStudyEventId());
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
                return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
            case "FILE":
                return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
            default:
                return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
        }

    }


    private ErrorObj validateForBoolean(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
        }
        return null;

    }

    private ErrorObj validateForInteger(String value) {
        try {
            Integer int1 = Integer.parseInt(value);
            return null;
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
        }
    }

    private ErrorObj validateForReal(String value) {
        if (isNumeric(value))
            return null;
        else
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
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
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_DATE_FORMAT);
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
                return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
            case ("calculation"):
                return null;
            default:
                return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);

        }
    }


    private ErrorObj validateRadioOrSingleSelect(ResponseSet responseSet, String value) {
        if (!responseSet.getOptionsValues().contains(value)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND);
        }
        return null;
    }

    private ErrorObj validateCheckBoxOrMultiSelect(ResponseSet responseSet, String value) {
        String[] values = value.split(",");
        ArrayList list = new ArrayList(Arrays.asList(values));

        for (String v : values) {
            if (!responseSet.getOptionsValues().contains(v)) {
                return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND);
            }
        }
        return null;
    }


    private EventCrf nonRepeatingCommonEventCrf(StudyEventDataBean studyEventDataBean, StudyEventDefinition studyEventDefinition, StudySubject studySubject) {
        String formOid = studyEventDataBean.getFormData().get(0).getFormOID();
        String formLayoutName = studyEventDataBean.getFormData().get(0).getFormLayoutName();
        CrfBean crf = crfDao.findByOcOID(formOid);
        FormLayout formLayout = formLayoutDao.findByNameCrfId(formLayoutName, crf.getCrfId());

        List<StudyEvent> studyEvents = studyEventDao.fetchListByStudyEventDefOID(studyEventDefinition.getOc_oid(), studySubject.getStudySubjectId());
        EventCrf eventCrf = null;
        for (StudyEvent stEvent : studyEvents) {
            eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(stEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());
            if (eventCrf != null) {
                logger.debug("EventCrf with StudyEventDefinition Oid {},Crf Oid {} and StudySubjectOid {} already exist in the System",
                        studyEventDefinition.getOc_oid(), crf.getOcOid(), studySubject.getOcOid());
                break;
            }
        }

        return eventCrf;
    }


    private StudyEvent validateStudyEvent(StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount) {
        int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());
        int eventOrdinal = maxSeOrdinal + 1;
        StudyEvent studyEvent = null;
        if (studyEventDefinition.getType().equals(COMMON)) {   // Common Event

            if (studyEventDefinition.getRepeating()) {   // Repeating Common Event
                if (studyEventDataBean.getStudyEventRepeatKey() != null && !studyEventDataBean.getStudyEventRepeatKey().equals("")) {   // Repeat Key present
                    studyEvent = eventRepeatKeyPresent(studyEventDataBean, studySubject, studyEventDefinition, userAccount, eventOrdinal);
                } else {   //  Repeat Key missing
                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));
                    studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                }
            } else {   // non Repeating Common Event
                EventCrf eventCrf = nonRepeatingCommonEventCrf(studyEventDataBean, studyEventDefinition, studySubject);
                if (eventCrf != null) {     // form exist
                    studyEvent = eventCrf.getStudyEvent();
                } else {      // form does not exist
                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));
                    studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                }
            }
        } else {   // Visit Event

            if (studyEventDefinition.getRepeating()) {   // Repeating Visit Event
                if (studyEventDataBean.getStudyEventRepeatKey() != null) {   // Repeat Key present
                    studyEvent = eventRepeatKeyPresent(studyEventDataBean, studySubject, studyEventDefinition, userAccount, eventOrdinal);
                } else {  // Repeat Key missing
                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));
                    studyEvent = validateStartDate(studyEvent, studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                }

            } else {   // Non Repeat Event
                studyEventDataBean.setStudyEventRepeatKey(String.valueOf('1'));
                studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());
                if (studyEvent == null) {
                    studyEvent = validateStartDate(studyEvent, studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                }
            }
        }
        return studyEvent;

    }

    private StudyEvent validateStartDate(StudyEvent studyEvent, StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount) {
        if (studyEventDataBean.getStartDate() != null) {
            //validate start date
            ErrorObj startDateErrorObj = validateForDate(studyEventDataBean.getStartDate());
            if (startDateErrorObj != null) {
                DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, FAILED, null, ErrorConstants.ERR_INVALID_START_DATE);
                dataImportReports.add(dataImportReport);
                logger.error("StudEventOID {} eventNotScheduled.invalidStartDate", studyEventDataBean.getStudyEventOID());
                return null;
            } else {
                if (studyEventDataBean.getEndDate() != null) {
                    // Validate End Date
                    ErrorObj endDateErrorObj = validateForDate(studyEventDataBean.getEndDate());
                    if (endDateErrorObj != null) {
                        DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, FAILED, null, ErrorConstants.ERR_INVALID_END_DATE);
                        dataImportReports.add(dataImportReport);
                        logger.error("StudEventOID {} eventNotScheduled.invalidEndDate", studyEventDataBean.getStudyEventOID());
                        return null;
                    } else {
                        //schedule
                        studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                        return studyEvent;
                    }
                } else {
                    // schedule
                    studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    return studyEvent;
                }
            }
        } else {
            DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, FAILED, null, ErrorConstants.ERR_MISSING_START_DATE);
            dataImportReports.add(dataImportReport);
            logger.error("StudEventOID {} eventNotScheduled.startDateMissing", studyEventDataBean.getStudyEventOID());
            return null;
        }
    }


    private StudyEvent eventRepeatKeyPresent(StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount, int eventOrdinal) {
        // Validate Repeat Key is an Integer Value
        try {
            Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey());
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, FAILED, null, ErrorConstants.ERR_INVALID_EVENT_REPEAT_KEY);
            dataImportReports.add(dataImportReport);
            logger.error("StudyEventRepeatKey {} is not Valid Integer", studyEventDataBean.getStudyEventRepeatKey());
            return null;
        }
        //  Look for existing event
        StudyEvent studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());
        if (studyEvent == null) {
            // verify repeat key too Large
            if (Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()) != (eventOrdinal)) {
                DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), null, null, null, null, FAILED, null, ErrorConstants.ERR_EVENT_REPEAT_KEY_TOO_LARGE);
                dataImportReports.add(dataImportReport);
                logger.error("RepeatKey {} too large,  is not next available repeat number", studyEventDataBean.getStudyEventRepeatKey());
                return null;
            }

            if (studyEventDefinition.getType().equals(UNSCHEDULED)) {
                studyEvent = validateStartDate(studyEvent, studyEventDataBean, studySubject, studyEventDefinition, userAccount);
            } else if (studyEventDefinition.getType().equals(COMMON)) {
                studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
            }
        }
        return studyEvent;
    }

    private StudyEvent scheduleEvent(StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount) {
        StudyEvent studyEvent = createStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        logger.debug("Scheduling new Visit Base  Event ID {}", studyEvent.getStudyEventId());
        return studyEvent;
    }


    private ItemGroup groupRepeatKeyPresent(StudySubject studySubject, StudyEventDataBean studyEventDataBean, FormDataBean formDataBean, ImportItemGroupDataBean importItemGroupDataBean, ItemGroup itemGroup, UserAccount userAccount, int highestGroupOrdinal) {
        // Validate Repeat Key is an Integer Value
        try {
            Integer.parseInt(importItemGroupDataBean.getItemGroupRepeatKey());
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), importItemGroupDataBean.getItemGroupOID(), importItemGroupDataBean.getItemGroupRepeatKey(), null, FAILED, null, ErrorConstants.ERR_INVALID_GROUP_REPEAT_KEY);
            dataImportReports.add(dataImportReport);
            logger.error("Item Group RepeatKey {} is not Valid Integer", importItemGroupDataBean.getItemGroupRepeatKey());
            return null;
        }
        // verify repeat key too Large
        if (Integer.parseInt(importItemGroupDataBean.getItemGroupRepeatKey()) > (highestGroupOrdinal + 1)) {
            DataImportReport dataImportReport = new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), importItemGroupDataBean.getItemGroupOID(), importItemGroupDataBean.getItemGroupRepeatKey(), null, FAILED, null, ErrorConstants.ERR_ITEMGROUP_REPEAT_KEY_TOO_LARGE);
            dataImportReports.add(dataImportReport);
            logger.error("RepeatKey {} too large,  is not next available repeat number", importItemGroupDataBean.getItemGroupRepeatKey());
            return null;
        }

        return itemGroup;
    }

    private ItemGroup validateItemGroupRepeat(StudySubject studySubject, StudyEventDataBean studyEventDataBean, FormDataBean formDataBean, EventCrf eventCrf, ImportItemGroupDataBean importItemGroupDataBean, ItemGroup itemGroup, UserAccount userAccount) {
        // find Highest Group Ordinal
        int highestGroupOrdinal = 0;
        List<ItemGroupMetadata> igms = itemGroup.getItemGroupMetadatas();
        for (ItemGroupMetadata igm : igms) {
            int maxRepeatGroup = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), igm.getItem().getItemId());
            if (maxRepeatGroup > highestGroupOrdinal)
                highestGroupOrdinal = maxRepeatGroup;
        }

        if (itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup()) {   // Repeating Item Group
            if (importItemGroupDataBean.getItemGroupRepeatKey() != null && !importItemGroupDataBean.getItemGroupRepeatKey().equals("")) {   // Repeat Key present
                itemGroup = groupRepeatKeyPresent(studySubject, studyEventDataBean, formDataBean, importItemGroupDataBean, itemGroup, userAccount, highestGroupOrdinal);
            } else {  // Repeat Key missing
                importItemGroupDataBean.setItemGroupRepeatKey(String.valueOf(highestGroupOrdinal + 1));
            }

        } else {   // Non Repeat Item Group
            importItemGroupDataBean.setItemGroupRepeatKey(String.valueOf('1'));
        }

        return itemGroup;

    }

    private void setEventStartAndEndDate(StudyEvent studyEvent, String startDate, String endDate) {
        if (startDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate localDate = LocalDate.parse(startDate, formatter);
            Date date = java.sql.Date.valueOf(localDate);
            studyEvent.setDateStart(date);
        } else {
            studyEvent.setDateStart(null);
        }

        if (endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate localDate = LocalDate.parse(endDate, formatter);
            Date date = java.sql.Date.valueOf(localDate);
            studyEvent.setDateEnd(date);
        } else {
            studyEvent.setDateEnd(null);
        }
    }


}
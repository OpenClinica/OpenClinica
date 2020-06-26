package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.submit.crfdata.*;
import core.org.akaza.openclinica.service.JobService;
import core.org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.controller.dto.DataImportReport;
import org.akaza.openclinica.controller.helper.table.ItemCountInForm;
import org.akaza.openclinica.controller.openrosa.OpenRosaSubmissionController;
import org.akaza.openclinica.controller.openrosa.QueryService;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.processor.QueryServiceHelperBean;
import core.org.akaza.openclinica.core.form.xform.QueryBean;
import core.org.akaza.openclinica.core.form.xform.QueryType;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
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

@Service("importService")
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

    @Autowired
    QueryService queryService;

    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;

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

    public static final String FAILED = "Failed";
    public static final String INSERTED = "Inserted";
    public static final String UPDATED = "Updated";
    public static final String NO_CHANGE = "No Change";
    public static final String DiscrepancyNoteMessage = "import XML";
    public static final String DetailedNotes = "Update via Import";

    List<DataImportReport> dataImportReports = null;
    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    SimpleDateFormat sdf_logFile = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Transactional
    public boolean validateAndProcessDataImport(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail, boolean isSystemUserImport) {
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

        Object subjectObject = null;
        StudySubject studySubject = null;

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        if (subjectDataBeans != null) {
            for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                if (subjectDataBean.getSubjectOID() != null)
                    subjectDataBean.setSubjectOID(subjectDataBean.getSubjectOID().toUpperCase());

                subjectObject = validateStudySubject(subjectDataBean, tenantStudy);

                if (subjectObject instanceof ErrorObj) {
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, ((ErrorObj) subjectObject).getCode(), null, ((ErrorObj) subjectObject).getMessage());
                    dataImportReports.add(dataImportReport);
                    logger.error("StudSubjectOID {} related issue", subjectDataBean.getSubjectOID());
                    continue;
                } else if (subjectObject instanceof StudySubject) {
                    studySubject = (StudySubject) subjectObject;
                }

                tenantStudy = studySubject.getStudy();

                ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
                for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                    if (studyEventDataBean.getStudyEventOID() != null)
                        studyEventDataBean.setStudyEventOID(studyEventDataBean.getStudyEventOID().toUpperCase());

                    Object eventObject = null;
                    StudyEvent studyEvent = null;

                    eventObject = validateStudyEvent(studyEventDataBean, studySubject, userAccount);
                    if (eventObject instanceof ErrorObj) {
                        dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, ((ErrorObj) eventObject).getCode(), null, ((ErrorObj) eventObject).getMessage());
                        dataImportReports.add(dataImportReport);
                        logger.error("StudEventOID {} related issue", studyEventDataBean.getStudyEventOID());
                        continue;
                    } else if (eventObject instanceof StudyEvent) {
                        studyEvent = (StudyEvent) eventObject;
                    }

                    StudyEventDefinition studyEventDefinition = studyEvent.getStudyEventDefinition();
                    ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                    int formDataBeanCount = 0;
                    for (FormDataBean formDataBean : formDataBeans) {
                        String reasonForChange = formDataBean.getReasonForChangeForCompleteForms();
                        formDataBeanCount++;
                        if (formDataBean.getFormOID() != null)
                            formDataBean.setFormOID(formDataBean.getFormOID().toUpperCase());
                        if (formDataBean.getEventCRFStatus() != null)
                            formDataBean.setEventCRFStatus(formDataBean.getEventCRFStatus().toLowerCase());

                        if (studyEventDefinition.getType().equals(COMMON) && formDataBeanCount > 1) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FAILED, null, ErrorConstants.ERR_FORM_MISSING_STUDY_EVENT_CONSTRUCT);
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOID {} related issue", formDataBean.getFormOID());
                            continue;
                        }

                        Object crfObject = null;
                        CrfBean crf = null;

                        crfObject = validateForm(formDataBean, tenantStudy, studyEventDefinition);
                        if (crfObject instanceof ErrorObj) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, ((ErrorObj) crfObject).getCode(), null, ((ErrorObj) crfObject).getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOID {} related issue", formDataBean.getFormOID());
                            continue;
                        } else if (crfObject instanceof CrfBean) {
                            crf = (CrfBean) crfObject;
                        }

                        Object edcObject = null;
                        EventDefinitionCrf edc = null;

                        edcObject = validateEventDefnCrf(tenantStudy, studyEventDefinition, crf);
                        if (edcObject instanceof ErrorObj) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, ((ErrorObj) edcObject).getCode(), null, ((ErrorObj) edcObject).getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOID {} related issue", formDataBean.getFormOID());
                            continue;
                        } else if (edcObject instanceof EventDefinitionCrf) {
                            edc = (EventDefinitionCrf) edcObject;
                        }

                        Object formLayoutObject = null;
                        FormLayout formLayout = null;

                        formLayoutObject = validateFormLayout(formDataBean, edc, crf);
                        if (formLayoutObject instanceof ErrorObj) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, ((ErrorObj) formLayoutObject).getCode(), null, ((ErrorObj) formLayoutObject).getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("FormLayoutOID {} related issue", formDataBean.getFormOID());
                            continue;
                        } else if (formLayoutObject instanceof FormLayout) {
                            formLayout = (FormLayout) formLayoutObject;
                        }

                        Object eventCrfObject = null;
                        EventCrf eventCrf = null;

                        eventCrfObject = validateEventCrf(formDataBean, studySubject, studyEvent, studyEventDefinition, userAccount, crf, formLayout, edc);
                        if (eventCrfObject instanceof ErrorObj) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, ((ErrorObj) eventCrfObject).getCode(), null, ((ErrorObj) eventCrfObject).getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("EventCrf {} related issue", formDataBean.getFormOID());
                            continue;
                        } else if (eventCrfObject instanceof EventCrf) {
                            eventCrf = (EventCrf) eventCrfObject;
                        }


                        ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();

                        ItemCountInForm itemCountInForm = new ItemCountInForm(0, 0, 0);


                        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                            itemCountInForm.setItemCountInFormData(itemCountInForm.getItemCountInFormData() + itemGroupDataBean.getItemData().size());
                        }

                        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                            if (itemGroupDataBean.getItemGroupOID() != null)
                                itemGroupDataBean.setItemGroupOID(itemGroupDataBean.getItemGroupOID().toUpperCase());


                            ItemGroup itemGroup = null;
                            Object itemGroupObject = null;


                            itemGroupObject = validateItemGroup(itemGroupDataBean, eventCrf, crf);
                            if (itemGroupObject instanceof ErrorObj) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, ((ErrorObj) itemGroupObject).getCode(), null, ((ErrorObj) itemGroupObject).getMessage());
                                dataImportReports.add(dataImportReport);
                                logger.error("ItemGroupOID {} related issue", itemGroupDataBean.getItemGroupOID());
                                continue;
                            } else if (itemGroupObject instanceof ItemGroup) {
                                itemGroup = (ItemGroup) itemGroupObject;
                            }


                            ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                            for (ImportItemDataBean itemDataBean : itemDataBeans) {
                                if (itemDataBean.getItemOID() != null)
                                    itemDataBean.setItemOID(itemDataBean.getItemOID().toUpperCase());

                                Item item = null;
                                Object itemObject = null;

                                itemObject = validateItem(itemDataBean, crf, eventCrf, itemGroupDataBean, userAccount, itemCountInForm, tenantStudy, studySubject, reasonForChange);

                                if (itemObject instanceof ErrorObj) {
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), ((ErrorObj) itemObject).getCode(), null, ((ErrorObj) itemObject).getMessage());
                                    dataImportReports.add(dataImportReport);
                                    logger.error("ItemOID {} related issue", itemDataBean.getItemOID());
                                    continue;
                                } else if (itemObject instanceof DataImportReport) {
                                    dataImportReport = (DataImportReport) itemObject;
                                    dataImportReport.setSubjectKey(subjectDataBean.getSubjectOID());
                                    dataImportReport.setStudySubjectID(subjectDataBean.getStudySubjectID());
                                    dataImportReport.setStudyEventOID(studyEventDataBean.getStudyEventOID());
                                    dataImportReport.setStudyEventRepeatKey(studyEventDataBean.getStudyEventRepeatKey());
                                    dataImportReport.setFormOID(formDataBean.getFormOID());
                                    dataImportReport.setItemGroupOID(itemGroupDataBean.getItemGroupOID());
                                    dataImportReport.setItemGroupRepeatKey(itemGroupDataBean.getItemGroupRepeatKey());
                                    dataImportReport.setItemOID(itemDataBean.getItemOID());
                                    dataImportReports.add(dataImportReport);
                                    continue;
                                } else if (itemObject instanceof Item) {
                                    item = (Item) itemObject;
                                }


                            }//itemDataBean for loop

                        } //itemGroupDataBean for loop

                        /* OC-11606 for signed event---  has specific logic to handle event status
                         *  After data is successfully imported, and at lease one item/field is updated/imported, then
                         *  the form will still be Complete,
                         *  and the event will be changed back to data entry started.
                         *
                         *  if no data get updated/imported, even have successful process, still need to keep the event as signed,
                         *  so this need to skip the existing set event status logic
                         */
                        if (this.isStudyEventSigned(studyEvent)) {
                            if (itemCountInForm.getInsertedUpdatedItemCountInForm() > 0) {
                                studyEvent.setStatusId(Status.AVAILABLE.getCode());
                                //OC-11632
                                studyEvent.setSubjectEventStatusId(SubjectEventStatus.COMPLETED.getCode());
                                studyEventDao.saveOrUpdate(studyEvent);
                            }

                        } else {
                            if ((formDataBean.getEventCRFStatus().equals(COMPLETE) || formDataBean.getEventCRFStatus().equals(DATA_ENTRY_COMPLETE))) {

                                if (itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() == itemCountInForm.getItemCountInFormData()) {
                                    // update eventcrf status into Complete\
                                    eventCrf = updateEventCrf(eventCrf, userAccount, Status.UNAVAILABLE, new Date());
                                    openRosaSubmissionController.updateStudyEventStatus(tenantStudy.getStudy() != null ? tenantStudy.getStudy() : tenantStudy, studySubject, studyEventDefinition, studyEvent, userAccount);

                                    logger.debug("Form {} status updated to Complete ", formDataBean.getFormOID());
                                } else {
                                    // event in COMPLETE, but during import process may still get some item updated
                                    ;
                                }


                            } else if (itemCountInForm.getInsertedUpdatedItemCountInForm() > 0) {                         // update eventcrf status into data entry status

                                //AC3: Complete forms with data imported into them must stay in Complete status at the conclusion of the import.
                                if (this.isEventCrfCompleted(eventCrf)) {
                                    ;
                                } else {
                                    // Update Event Crf Status into Initial Data Entry
                                    eventCrf = updateEventCrf(eventCrf, userAccount, Status.AVAILABLE, null);
                                }

                            }
                        }

                        // check if all Forms within this Event is Complete
                    } // formDataBean for loop


                } // StudyEventDataBean for loop
            } // StudySubjectDataBean for loop
        } else { // subjectDataBean ==null
            dataImportReport = new DataImportReport(null, null, null, null, null, null, null, null, FAILED, null, ErrorConstants.ERR_SUBJECT_DATA_MISSING);
            dataImportReports.add(dataImportReport);
            logger.info("SubjectData is missing ");
        }


        writeToFile(dataImportReports, fileName, JobType.XML_IMPORT);
        if (isSystemUserImport) {
            // For system level import, check if the import failed and return the status
            boolean hasImportFailed = dataImportReports.stream()
                    .filter(dataImportReport1 -> dataImportReport1.getStatus().equals(FAILED))
                    .findAny()
                    .isPresent();
            return !hasImportFailed;
        } else {
            // For all other imports, mark the job as completed and always return true
            userService.persistJobCompleted(jobDetail, fileName);
            return true;
        }

    }

    public void writeToFile(List<DataImportReport> dataImportReports, String fileName, JobType jobType) {
        logger.debug("writing report to File");

        String filePath = getFilePath(jobType) + File.separator + fileName;

        File file = new File(filePath);

        PrintWriter writer = null;
        try {
            writer = openFile(file);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.error("Error while accessing file to start writing: ", e);
        } finally {
            if (jobType.equals(JobType.XML_IMPORT))
                writer.print(writeImportToTextFile(dataImportReports));
            else if (jobType.equals(JobType.SCHEDULE_EVENT))
                writer.print(writeBulkEventScheduleOrUpdateToTextFile(dataImportReports));
            else if (jobType.equals(JobType.BULK_ADD_PARTICIPANTS))
                writer.print(writeBulkAddParticipantToTextFile(dataImportReports));
            closeFile(writer);
        }

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


    private String writeImportToTextFile(List<DataImportReport> dataImportReports) {

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

    private String writeBulkAddParticipantToTextFile(List<DataImportReport> dataImportReports) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Row");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ParticipantID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Participant OID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Participate Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Message");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append('\n');


        dataImportReports.forEach(p -> {
            stringBuffer.append(p.getRowNumber());
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getStudySubjectID() != null ? p.getStudySubjectID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getSubjectKey() != null ? p.getSubjectKey() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getParticipateStatus() != null ? p.getParticipateStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getStatus() != null ? p.getStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getMessage() != null ? p.getMessage() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append('\n');
        });


        StringBuilder sb = new StringBuilder();
        sb.append(stringBuffer.toString() + "\n");

        return sb.toString();
    }


    private String writeBulkEventScheduleOrUpdateToTextFile(List<DataImportReport> dataImportReports) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Row #");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ParticipantID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("StudyEventOID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("StudyEventRepeatKey");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Message");
        stringBuffer.append('\n');
        for (DataImportReport dataImportReport : dataImportReports) {
            stringBuffer.append(dataImportReport.getRowNumber() != null ? dataImportReport.getRowNumber() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudySubjectID() != null ? dataImportReport.getStudySubjectID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudyEventOID() != null ? dataImportReport.getStudyEventOID() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(dataImportReport.getStudyEventRepeatKey() != null ? dataImportReport.getStudyEventRepeatKey() : "");
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
        logger.debug("Creating new Item Data");
        itemData = itemDataDao.saveOrUpdate(itemData);
        return itemData;
    }

    private ItemData updateItemData(ItemData itemData, UserAccount userAccount, String value) {
        itemData.setValue(value);
        itemData.setOldStatus(itemData.getStatus());
        itemData.setDateUpdated(new Date());
        itemData.setUpdateId(userAccount.getUserId());
        itemData = itemDataDao.saveOrUpdate(itemData);
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
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        logger.debug("Creating new Event Crf");

        return eventCrf;
    }

    private EventCrf updateEventCrf(EventCrf eventCrf, UserAccount userAccount, Status formStatus, Date dateCompleted) {
        eventCrf.setDateUpdated(new Date());
        eventCrf.setUpdateId(userAccount.getUserId());
        eventCrf.setOldStatusId(eventCrf.getStatusId());
        eventCrf.setStatusId(formStatus.getCode());
        if (dateCompleted != null)
            eventCrf.setDateCompleted(dateCompleted);
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
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
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        logger.debug("Creating new Study Event");
        return studyEvent;
    }

    public StudyEvent updateStudyEventDatesAndStatus(StudyEvent studyEvent, UserAccount userAccount, String startDate, String endDate, String eventStatus) {
        SubjectEventStatus subjectEventStatus = getSubjectEventStatus(eventStatus);
        studyEvent.setSubjectEventStatusId(subjectEventStatus.getCode());
        setEventStartAndEndDate(studyEvent, startDate, endDate);
        studyEvent.setDateUpdated(new Date());
        studyEvent.setUpdateId(userAccount.getUserId());
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        logger.debug("Updating Study Event Id {}", studyEvent.getStudyEventId());
        return studyEvent;
    }

    public StudyEvent updateStudyEventDates(StudyEvent studyEvent, UserAccount userAccount, String startDate, String endDate) {

        setEventStartAndEndDate(studyEvent, startDate, endDate);
        studyEvent.setDateUpdated(new Date());
        studyEvent.setUpdateId(userAccount.getUserId());
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        logger.debug("Updating Study Event Id {}", studyEvent.getStudyEventId());
        return studyEvent;
    }

    public StudyEvent updateStudyEvntStatus(StudyEvent studyEvent, UserAccount userAccount, String eventStatus) {
        SubjectEventStatus subjectEventStatus = getSubjectEventStatus(eventStatus);
        studyEvent.setSubjectEventStatusId(subjectEventStatus.getCode());
        studyEvent.setDateUpdated(new Date());
        studyEvent.setUpdateId(userAccount.getUserId());
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        logger.debug("Updating Study Event Id {}", studyEvent.getStudyEventId());
        return studyEvent;
    }


    private ErrorObj validateItemDataType(Item item, String value, ItemCountInForm itemCountInForm) {
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
            default:
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
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
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
        }
        return null;
    }

    private ErrorObj validateForReal(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            logger.error("Unable to parse real value", nfe);
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
        }
        return null;
    }

    private ErrorObj validateForDate(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(value, formatter);
        } catch (Exception pe) {
            pe.getStackTrace();
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_DATE_FORMAT);
        }
        return null;
    }


    private ErrorObj validateResponseSets(ResponseSet responseSet, String value, ItemCountInForm itemCountInForm) {
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
            case ("calculation"):
                return null;
            default:
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
        }
    }


    private ErrorObj validateRadioOrSingleSelect(ResponseSet responseSet, String value) {
        if(responseSet.getOptionsText().equals("_") && responseSet.getOptionsValues().equals("_"))
            return null;
        if (!responseSet.getOptionsValues().contains(value)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND);
        }
        return null;
    }

    private ErrorObj validateCheckBoxOrMultiSelect(ResponseSet responseSet, String value) {
        String[] values = value.split(",");

        for (String v : values) {
            if (!responseSet.getOptionsValues().contains(v)) {
                return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND);
            }
        }
        return null;
    }


    private Object getFormLayout(StudyEventDataBean studyEventDataBean) {
        String formOid = studyEventDataBean.getFormData().get(0).getFormOID();
        if (formOid != null) formOid = formOid.toUpperCase();
        String formLayoutName = studyEventDataBean.getFormData().get(0).getFormLayoutName();
        CrfBean crf = crfDao.findByOcOID(formOid);
        if (crf == null || (crf != null && !crf.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }

        FormLayout formLayout = formLayoutDao.findByNameCrfId(formLayoutName, crf.getCrfId());
        return formLayout;
    }


    private Object commonNonRepeatingEventCrfLookUp(StudyEventDataBean studyEventDataBean, StudyEventDefinition studyEventDefinition, StudySubject studySubject) {
        Object formLayoutObject = getFormLayout(studyEventDataBean);
        if (formLayoutObject instanceof ErrorObj) return formLayoutObject;
        FormLayout formLayout = (FormLayout) formLayoutObject;

        List<StudyEvent> studyEvents = studyEventDao.fetchListByStudyEventDefOID(studyEventDefinition.getOc_oid(), studySubject.getStudySubjectId());
        EventCrf eventCrf = null;
        List<EventCrf> eventCrfs = null;
        for (StudyEvent stEvent : studyEvents) {
            eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(stEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getCrf().getCrfId());
            if (eventCrfs.size() > 0) eventCrf = eventCrfs.get(0);
            break;
        }

        return eventCrf;
    }


    private Object validateStudyEvent(StudyEventDataBean studyEventDataBean, StudySubject studySubject, UserAccount userAccount) {
        if (studyEventDataBean.getFormData().size() == 0)
            return new ErrorObj(FAILED, ErrorConstants.ERR_EVENT_DOES_NOT_CONTAIN_FORMDATA);

        StudyEvent studyEvent = null;
        Object eventObject = null;
        Object formLayoutObject = null;
        Object eventCrfObject = null;

        // OID is missing
        if (studyEventDataBean.getStudyEventOID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_STUDYEVENTOID);
        }

        // StudyEventDefinition invalid OID and Archived
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByOcOID(studyEventDataBean.getStudyEventOID());
        if (studyEventDefinition == null || (studyEventDefinition != null && !studyEventDefinition.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_STUDYEVENTOID);
        }

        int maxSeOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(), studyEventDefinition.getStudyEventDefinitionId());
        int eventOrdinal = maxSeOrdinal + 1;
        if (studyEventDefinition.getType().equals(COMMON)) {   // Common Event

            if (studyEventDefinition.getRepeating()) {   // Repeating Common Event
                if (studyEventDataBean.getStudyEventRepeatKey() != null && !studyEventDataBean.getStudyEventRepeatKey().equals("")) {   // Repeat Key present
                    eventObject = validateEventRepeatKeyIntNumber(studyEventDataBean.getStudyEventRepeatKey());
                    if (eventObject instanceof ErrorObj) return eventObject;
                    studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());

                    ErrorObj errorObj = checkEventAvailable(studyEvent);
                    if (errorObj != null) {
                        return errorObj;
                    }

                    if (studyEvent == null) {
                        eventObject = validateEventRepeatKeyTooLarge(studyEventDataBean.getStudyEventRepeatKey(), eventOrdinal);
                        if (eventObject instanceof ErrorObj) return eventObject;
                        studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    } else {
                        formLayoutObject = getFormLayout(studyEventDataBean);
                        if (formLayoutObject instanceof ErrorObj) return formLayoutObject;
                        FormLayout formLayout = (FormLayout) formLayoutObject;
                        EventCrf eventCrf = null;
                        List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getCrf().getCrfId());
                        if (eventCrfs.size() > 0) eventCrf = eventCrfs.get(0);
                        // Event Crf has status complete or unavailable
                        // in complete status will not throw out error any more at this stage
                        if (eventCrf != null && eventCrf.getStatusId() != (Status.AVAILABLE.getCode()) && !isEventCrfCompleted(eventCrf))
                            return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_NOT_AVAILABLE);

                        if (eventCrf == null) {
                            return new ErrorObj(FAILED, ErrorConstants.ERR_REPEAT_KEY_AND_FORM_MISMATCH);
                        }
                    }
                    return studyEvent;

                } else {   //  Repeat Key missing
                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));
                    studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    return studyEvent;
                }
            } else {   // non Repeating Common Event

                // Discard Repeat Key
                eventCrfObject = commonNonRepeatingEventCrfLookUp(studyEventDataBean, studyEventDefinition, studySubject);
                if (eventCrfObject instanceof ErrorObj) return eventCrfObject;
                EventCrf eventCrf = (EventCrf) eventCrfObject;

                // Event Crf has status complete or invalid
                // in complete status will not throw out error any more at this stage
                if (eventCrf != null && eventCrf.getStatusId() != (Status.AVAILABLE.getCode()) && !isEventCrfCompleted(eventCrf))
                    return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_NOT_AVAILABLE);

                if (eventCrf != null) {     // form exist
                    studyEvent = eventCrf.getStudyEvent();
                    ErrorObj errorObj = checkEventAvailable(studyEvent);
                    if (errorObj != null) {
                        return errorObj;
                    }

                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(studyEvent.getSampleOrdinal()));
                    return studyEvent;
                } else {
                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));
                    studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    return studyEvent;
                }
            }
        } else {   // Visit Event

            if (studyEventDefinition.getRepeating()) {   // Repeating Visit Event
                if (studyEventDataBean.getStudyEventRepeatKey() != null && !studyEventDataBean.getStudyEventRepeatKey().equals("")) {   // Repeat Key present
                    //validate repeat key for integer
                    eventObject = validateEventRepeatKeyIntNumber(studyEventDataBean.getStudyEventRepeatKey());
                    if (eventObject instanceof ErrorObj) return eventObject;
                    // Lookup for event if exists
                    studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());

                    //event not available                
                    ErrorObj errorObj = checkEventAvailable(studyEvent);
                    if (errorObj != null) {
                        return errorObj;
                    }

                    if (studyEvent == null) {
                        //validate repeat key too large
                        eventObject = validateEventRepeatKeyTooLarge(studyEventDataBean.getStudyEventRepeatKey(), eventOrdinal);
                        if (eventObject instanceof ErrorObj) return eventObject;
                        //validate start, end date
                        eventObject = validateStartAndEndDateAndOrder(studyEventDataBean);
                        if (eventObject instanceof ErrorObj) return eventObject;
                        // schedule event
                        studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    }
                    return studyEvent;

                } else {// Repeat Key missing
                    // set repeat key
                    studyEventDataBean.setStudyEventRepeatKey(String.valueOf(eventOrdinal));
                    //lookup for event if exits
                    studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());

                    //event not available
                    ErrorObj errorObj = checkEventAvailable(studyEvent);
                    if (errorObj != null) {
                        return errorObj;
                    }

                    if (studyEvent == null) {
                        // validate start , end date
                        eventObject = validateStartAndEndDateAndOrder(studyEventDataBean);
                        if (eventObject instanceof ErrorObj) return eventObject;
                        // schedule event
                        studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    }
                    return studyEvent;
                }

            } else {   // Non Repeat Event
                //set repeat key to 1
                studyEventDataBean.setStudyEventRepeatKey(String.valueOf('1'));
                //lookup for event if exists
                studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDataBean.getStudyEventOID(), Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), studySubject.getStudySubjectId());

                if (studyEvent == null) {
                    // validate start , end date
                    eventObject = validateStartAndEndDateAndOrder(studyEventDataBean);
                    if (eventObject instanceof ErrorObj) return eventObject;
                    // schedule event
                    studyEvent = scheduleEvent(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                } else {

                    ErrorObj errorObj = checkEventAvailable(studyEvent);
                    if (errorObj != null) {
                        return errorObj;
                    }
                }
                return studyEvent;
            }
        }
    }

    /**
     * @param studyEvent
     */
    private ErrorObj checkEventAvailable(StudyEvent studyEvent) {
        ErrorObj errorObj = null;

        if (studyEvent != null && (
        		// OC-11780, for visit and just scheduled event(before enter any data),UI side will only update status of StudyEvent,because no CRF yet
        		studyEvent.getStatusId()==Status.DELETED.getCode() ||
                studyEvent.getSubjectEventStatusId() == SubjectEventStatus.LOCKED.getCode() ||
                        studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode() ||
                        studyEvent.getSubjectEventStatusId() == SubjectEventStatus.STOPPED.getCode())) {

            errorObj = new ErrorObj(FAILED, ErrorConstants.ERR_EVENT_NOT_AVAILABLE);
        }


        return errorObj;
    }


    private boolean isEventCrfCompleted(EventCrf eventCrf) {
        return eventCrf.getStatusId() == Status.UNAVAILABLE.getCode();
    }

    /**
     * @param studyEvent
     * @return
     */
    private boolean isStudyEventSigned(StudyEvent studyEvent) {
        return studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SIGNED.getCode();
    }

    public ErrorObj validateStartAndEndDateAndOrder(StudyEventDataBean studyEventDataBean) {
        if (studyEventDataBean.getStartDate() == null)
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_START_DATE);
        ErrorObj errorObj = null;
        if (studyEventDataBean.getStartDate() != null) {
            //validate start date
            errorObj = validateForDate(studyEventDataBean.getStartDate());
            if (errorObj != null) {
                errorObj.setMessage(ErrorConstants.ERR_INVALID_START_DATE);
                return errorObj;
            }
        }
        if (studyEventDataBean.getEndDate() != null) {
            // Validate End Date
            errorObj = validateForDate(studyEventDataBean.getEndDate());
            if (errorObj != null) {
                errorObj.setMessage(ErrorConstants.ERR_INVALID_END_DATE);
                return errorObj;
            }
        }

        if (studyEventDataBean.getStartDate() != null && studyEventDataBean.getEndDate() != null) {
            //Validate Date Order
            return validateDateOrder(studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
        }
        return null;
    }


    public StudyEvent scheduleEvent(StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount) {
        StudyEvent studyEvent = createStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
        logger.debug("Scheduling new Visit Base  Event ID {}", studyEvent.getStudyEventId());
        return studyEvent;
    }


    private ErrorObj validateItemGroupRepeat(EventCrf eventCrf, ImportItemGroupDataBean itemGroupDataBean, ItemGroup itemGroup) {
        ErrorObj errorObj = null;
        // find Highest Group Ordinal
        int highestGroupOrdinal = 0;
        List<ItemGroupMetadata> igms = itemGroup.getItemGroupMetadatas();
        for (ItemGroupMetadata igm : igms) {
            int maxRepeatGroup = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), igm.getItem().getItemId());
            if (maxRepeatGroup > highestGroupOrdinal)
                highestGroupOrdinal = maxRepeatGroup;
        }
        int groupOrdinal = highestGroupOrdinal + 1;

        if (itemGroup.getItemGroupMetadatas().get(0).isRepeatingGroup()) {   // Repeating Item Group
            if (itemGroupDataBean.getItemGroupRepeatKey() != null && !itemGroupDataBean.getItemGroupRepeatKey().equals("")) {   // Repeat Key present
                errorObj = validateGroupRepeatKeyIntNumber(itemGroupDataBean.getItemGroupRepeatKey());
                if (errorObj != null) return errorObj;
                errorObj = validateGroupRepeatKeyTooLarge(itemGroupDataBean.getItemGroupRepeatKey(), groupOrdinal);
                if (errorObj != null) return errorObj;
            } else {  // Repeat Key missing
                itemGroupDataBean.setItemGroupRepeatKey(String.valueOf(groupOrdinal));
            }

        } else {   // Non Repeat Item Group
            itemGroupDataBean.setItemGroupRepeatKey(String.valueOf('1'));
        }

        return null;
    }

    private void setEventStartAndEndDate(StudyEvent studyEvent, String startDate, String endDate) {
        if (startDate != null)
            studyEvent.setDateStart(convertStringIntoDate(startDate));

        if (endDate != null)
            studyEvent.setDateEnd(convertStringIntoDate(endDate));

    }


    public ErrorObj validateEventTransition(StudyEvent studyEvent, UserAccount userAccount, String eventStatus) {
        SubjectEventStatus subjectEventStatus = getSubjectEventStatus(eventStatus);
        if (subjectEventStatus == null)
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_EVENT_TRANSITION_STATUS);

        if (studyEvent.getSubjectEventStatusId() != subjectEventStatus.getCode()) {
            if (!(subjectEventStatus.equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                    && (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SCHEDULED.getCode()
                    || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode()))
                    &&
                    !(subjectEventStatus.equals(SubjectEventStatus.COMPLETED)
                            && studyEvent.getSubjectEventStatusId() == SubjectEventStatus.DATA_ENTRY_STARTED.getCode())
                    &&
                    !(subjectEventStatus.equals(SubjectEventStatus.STOPPED)
                            && studyEvent.getSubjectEventStatusId() == SubjectEventStatus.DATA_ENTRY_STARTED.getCode())
                    &&
                    !(subjectEventStatus.equals(SubjectEventStatus.SKIPPED)
                            && studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SCHEDULED.getCode())
                    &&
                    !(subjectEventStatus.equals(SubjectEventStatus.LOCKED)
                            && (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.COMPLETED.getCode()
                            || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode()))
                    ) {
                return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_EVENT_TRANSITION_STATUS);
            }
        }
        return null;
    }

    private Date convertStringIntoDate(String dateInSring) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(dateInSring, formatter);
        Date date = java.sql.Date.valueOf(localDate);
        return date;
    }

    private SubjectEventStatus getSubjectEventStatus(String subjectEventStatus) {
        if (SubjectEventStatus.SCHEDULED.getDescription().equalsIgnoreCase(subjectEventStatus)) {
            return SubjectEventStatus.SCHEDULED;
        } else if (SubjectEventStatus.DATA_ENTRY_STARTED.getDescription().replace("_", " ").equalsIgnoreCase(subjectEventStatus)) {
            return SubjectEventStatus.DATA_ENTRY_STARTED;
        } else if (SubjectEventStatus.COMPLETED.getDescription().equalsIgnoreCase(subjectEventStatus)) {
            return SubjectEventStatus.COMPLETED;
        } else if (SubjectEventStatus.SKIPPED.getDescription().equalsIgnoreCase(subjectEventStatus)) {
            return SubjectEventStatus.SKIPPED;
        } else if (SubjectEventStatus.STOPPED.getDescription().equalsIgnoreCase(subjectEventStatus)) {
            return SubjectEventStatus.STOPPED;
        } else if (SubjectEventStatus.LOCKED.getDescription().equalsIgnoreCase(subjectEventStatus)) {
            return SubjectEventStatus.LOCKED;
        }
        return null;
    }


    public ErrorObj validateEventStatus(String eventStatus) {
        if (!SubjectEventStatus.SCHEDULED.getDescription().equalsIgnoreCase(eventStatus)
                && !SubjectEventStatus.DATA_ENTRY_STARTED.getDescription().replace("_", " ").equalsIgnoreCase(eventStatus)
                && !SubjectEventStatus.COMPLETED.getDescription().equalsIgnoreCase(eventStatus)
                && !SubjectEventStatus.SKIPPED.getDescription().equalsIgnoreCase(eventStatus)
                && !SubjectEventStatus.STOPPED.getDescription().equalsIgnoreCase(eventStatus)
                && !SubjectEventStatus.LOCKED.getDescription().equalsIgnoreCase(eventStatus)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_EVENT_STATUS);
        }
        return null;
    }

    public ErrorObj validateGroupRepeatKeyIntNumber(String repeatKey) {
        try {
            Integer.parseInt(repeatKey);
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_GROUP_REPEAT_KEY);
        }
        return null;
    }

    public ErrorObj validateEventRepeatKeyIntNumber(String repeatKey) {
        try {
            Integer.parseInt(repeatKey);
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_EVENT_REPEAT_KEY);
        }
        return null;
    }

    private ErrorObj validateEventRepeatKeyTooLarge(String repeatKey, int eventOrdinal) {
        if (Integer.parseInt(repeatKey) != (eventOrdinal)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_EVENT_REPEAT_KEY_TOO_LARGE);
        }
        return null;
    }

    private ErrorObj validateGroupRepeatKeyTooLarge(String repeatKey, int groupOrdinal) {
        if (Integer.parseInt(repeatKey) > (groupOrdinal)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_GROUP_REPEAT_KEY_TOO_LARGE);
        }
        return null;
    }

    private ErrorObj validateDateOrder(String startDate, String endDate) {
        Date sDate = convertStringIntoDate(startDate);
        Date eDate = convertStringIntoDate(endDate);
        if (eDate.before(sDate)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_END_DATE_BEFORE_START_DATE);
        }
        return null;
    }

    public Object validateStudySubject(SubjectDataBean subjectDataBean, Study tenantStudy) {
        if (subjectDataBean.getStudyEventData().size() == 0)
            return new ErrorObj(FAILED, ErrorConstants.ERR_SUBJECT_DOES_NOT_CONTAIN_EVENTDATA);
        StudySubject studySubject = null;
        StudySubject studySubject02 = null;

        if (subjectDataBean.getSubjectOID() == null && subjectDataBean.getStudySubjectID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_PARTICIPANT_ID);

        } else if (subjectDataBean.getSubjectOID() != null && subjectDataBean.getStudySubjectID() == null) {
            studySubject = studySubjectDao.findByOcOID(subjectDataBean.getSubjectOID());
            if (studySubject == null
                    || (studySubject != null && tenantStudy.getStudy() != null && studySubject.getStudy().getStudyId() != tenantStudy.getStudyId())
                    || (studySubject != null && tenantStudy.getStudy() == null && studySubject.getStudy().getStudy() != null && studySubject.getStudy().getStudy().getStudyId() != tenantStudy.getStudyId())) {

                return new ErrorObj(FAILED, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);

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
                    return new ErrorObj(FAILED, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
                }
            } catch (Exception e) {
                logger.error("Error while validating study subject: ", e);
                return new ErrorObj(FAILED, ErrorConstants.ERR_MULTIPLE_PARTICIPANTS_FOUND);
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
                return new ErrorObj(FAILED, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
            }

            if (studySubject != null && studySubject02 != null && studySubject.getStudySubjectId() != studySubject02.getStudySubjectId()) {
                return new ErrorObj(FAILED, ErrorConstants.ERR_PARTICIPANT_IDENTIFIERS_MISMATCH);
            }
        }
        if (studySubject != null && !(studySubject.getStatus().equals(Status.AVAILABLE)) && !(studySubject.getStatus().equals(Status.SIGNED))) {

                return new ErrorObj(FAILED, ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
        }
        subjectDataBean.setSubjectOID(studySubject.getOcOid());
        subjectDataBean.setStudySubjectID(studySubject.getLabel());
        return studySubject;
    }


    private Object validateEventCrf(FormDataBean formDataBean, StudySubject studySubject, StudyEvent studyEvent, StudyEventDefinition studyEventDefinition, UserAccount userAccount, CrfBean crf, FormLayout formLayout, EventDefinitionCrf edc) {

        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());

        // Event Crf has status complete or invalid
        // in complete status will not throw out error any more at this stage
        if (eventCrf != null && eventCrf.getStatusId() != (Status.AVAILABLE.getCode()) && !isEventCrfCompleted(eventCrf))
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_NOT_AVAILABLE);


        if (eventCrf == null) {
            List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), crf.getCrfId());
            if (eventCrfs.size() > 0) {
                eventCrf = eventCrfs.get(0);
                formDataBean.setFormLayoutName(eventCrf.getFormLayout().getXformName());
            }
        }


        if (eventCrf == null) {

            String selectedVersionIds = edc.getSelectedVersionIds();
            if (!StringUtils.isEmpty(selectedVersionIds)) {
                String[] ids = selectedVersionIds.split(",");
                ArrayList<Integer> idList = new ArrayList<Integer>();
                for (String id : ids) {
                    idList.add(Integer.valueOf(id));
                }
                if (!idList.contains(formLayout.getFormLayoutId()))
                    return new ErrorObj(FAILED, ErrorConstants.ERR_FORMLAYOUTOID_NOT_AVAILABLE);
            }

            eventCrf = createEventCrf(studySubject, studyEvent, formLayout, userAccount);

            logger.debug("new EventCrf Id {} is created  ", eventCrf.getEventCrfId());
            updateStudyEvntStatus(studyEvent, userAccount, DATA_ENTRY_STARTED);


            logger.debug("Study Event Id {} is updated", studyEvent.getStudyEventId());
        }
        return eventCrf;
    }

    private Object validateItemGroup(ImportItemGroupDataBean itemGroupDataBean, EventCrf eventCrf, CrfBean crf) {
        if (itemGroupDataBean.getItemData() == null)
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEMGROUP_DOES_NOT_CONTAIN_ITEMDATA);

        ErrorObj errorObj = null;
        if (itemGroupDataBean.getItemGroupOID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEMGROUPOID_NOT_FOUND);
        }

        ItemGroup itemGroup = itemGroupDao.findByOcOIDCrfId(itemGroupDataBean.getItemGroupOID(), crf);
        if (itemGroup == null || (itemGroup != null && !itemGroup.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEMGROUPOID_NOT_FOUND);
        }
        //Item Group invalid Oid in Form
        ItemGroup itmGroup = itemGroupDao.findByNameCrfId(itemGroup.getName(), crf);
        if (itmGroup == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEMGROUPOID_NOT_FOUND);
        }

        errorObj = validateItemGroupRepeat(eventCrf, itemGroupDataBean, itemGroup);
        if (errorObj != null) return errorObj;

        return itemGroup;
    }


    private Object validateItem(ImportItemDataBean itemDataBean, CrfBean crf, EventCrf eventCrf, ImportItemGroupDataBean itemGroupDataBean, UserAccount userAccount, ItemCountInForm itemCountInForm, Study study, StudySubject studySubject, String reasonForChange) {
        ErrorObj errorObj = null;
        if (itemDataBean.getItemOID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_NOT_FOUND);
        }

        Item item = itemDao.findByOcOID(itemDataBean.getItemOID());

        // ItemOID is not valid
        if (item == null || (item != null && !item.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_NOT_FOUND);
        }

        if (!validateItemInGroup(item,itemGroupDataBean,crf)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_ITEMGROUP_DOES_NOT_CONTAIN_ITEMDATA);
        }

        if (itemDataBean.getValue() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_VALUE);
        }

        if (itemDataBean.getValue().length() > 3999) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TOO_LONG);
        }

        if (StringUtils.isNotEmpty(itemDataBean.getValue())) {
            errorObj = validateItemDataType(item, itemDataBean.getValue(), itemCountInForm);
            if (errorObj != null) return errorObj;


            Set<ItemFormMetadata> ifms = item.getItemFormMetadatas();
            ResponseSet responseSet = ifms.iterator().next().getResponseSet();
            errorObj = validateResponseSets(responseSet, itemDataBean.getValue(), itemCountInForm);
            if (errorObj != null) return errorObj;

        }
        ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));

        if (itemData != null) {
            if (itemData.getValue().equals(itemDataBean.getValue())) {
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                return new ErrorObj(NO_CHANGE, null);

            } else {
                if (isEventCrfCompleted(eventCrf)) {
                    ErrorObj eb = createQuery(userAccount, study, studySubject, itemData, reasonForChange);
                    if (eb != null) {
                        return eb;
                    }
                }
                itemData = updateItemData(itemData, userAccount, itemDataBean.getValue());
                itemCountInForm.setInsertedUpdatedItemCountInForm(itemCountInForm.getInsertedUpdatedItemCountInForm() + 1);
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                return new DataImportReport(null, null, null, null, null, null, null, null, UPDATED, sdf_logFile.format(new Date()), null);
            }
        } else {
            itemData = createItemData(eventCrf, itemDataBean, userAccount, item, Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
            if (isEventCrfCompleted(eventCrf)) {
                ErrorObj eb = createQuery(userAccount, study, studySubject, itemData, reasonForChange);
                if (eb != null) {
                    return eb;
                }
            }
            itemCountInForm.setInsertedUpdatedItemCountInForm(itemCountInForm.getInsertedUpdatedItemCountInForm() + 1);
            itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
            return new DataImportReport(null, null, null, null, null, null, null, null, INSERTED, sdf_logFile.format(new Date()), null);
        }
    }

    /**
     * @param userAccount
     * @param study
     * @param studySubject
     * @param itemData
     */
    private ErrorObj createQuery(UserAccount userAccount, Study study, StudySubject studySubject, ItemData itemData, String reasonForChange) {

        ErrorObj eb = null;

        try {
            QueryBean queryBean = new QueryBean();
            queryBean.setType(QueryType.REASON.getName());
            if (reasonForChange != null && !(reasonForChange.isEmpty())) {
                queryBean.setComment(reasonForChange);
            } else {
                queryBean.setComment(this.DetailedNotes);
            }


            QueryServiceHelperBean helperBean = new QueryServiceHelperBean();
            helperBean.setItemData(itemData);
            SubmissionContainer container = new SubmissionContainer();
            container.setStudy(study);
            container.setUser(userAccount);
            container.setSubject(studySubject);
            helperBean.setContainer(container);

            DiscrepancyNote parentDn = (DiscrepancyNote) queryService.createQuery(helperBean, queryBean, true);
            parentDn.setDescription(this.DiscrepancyNoteMessage);

            parentDn = discrepancyNoteDao.saveOrUpdate(parentDn);
            helperBean.setDn(parentDn);
            queryService.saveQueryItemDatamap(helperBean);

            DiscrepancyNote childDN = queryService.createQuery(helperBean, queryBean, false);
            childDN.setParentDiscrepancyNote(parentDn);
            childDN = discrepancyNoteDao.saveOrUpdate(childDN);

            // update Item data map           
            helperBean.setDn(childDN);
            helperBean.setParentDn(parentDn);
            queryService.saveQueryItemDatamap(helperBean);
        } catch (Exception e) {
            eb = new ErrorObj(FAILED, ErrorConstants.ERR_IMPORT_XML_QUERY_CREAT_FAILED);
        }

        return eb;
    }

    private Object validateForm(FormDataBean formDataBean, Study tenantStudy, StudyEventDefinition studyEventDefinition) {
        if (formDataBean.getItemGroupData() == null)
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_DOES_NOT_CONTAIN_ITEMGROUPDATA);
        CrfBean crf = null;
        if (formDataBean.getFormOID() == null) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_FORMOID);
        }
        // Form Invalid OID and form not Archived
        crf = crfDao.findByOcOID(formDataBean.getFormOID());
        if (crf == null || (crf != null && !crf.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }
        // Form Invalid OID
        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                tenantStudy.getStudy() == null ? tenantStudy.getStudyId() : tenantStudy.getStudy().getStudyId());
        if (edc == null || (edc != null && !edc.getStatusId().equals(Status.AVAILABLE.getCode()))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }

        return crf;

    }

    private Object validateEventDefnCrf(Study tenantStudy, StudyEventDefinition studyEventDefinition, CrfBean crf) {

        // Form Invalid OID

        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(),
                crf.getCrfId(), tenantStudy.getStudyId());
        if (edc == null) {
            edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                    tenantStudy.getStudy().getStudyId());
        }
        if (edc == null || (edc != null && !edc.getStatusId().equals(Status.AVAILABLE.getCode()))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }

        return edc;

    }

    private Object validateFormLayout(FormDataBean formDataBean, EventDefinitionCrf edc, CrfBean crf) {


        if (formDataBean.getFormLayoutName() == null) {
            formDataBean.setFormLayoutName(edc.getFormLayout().getName());
        }

        // FormLayout Invalid OID
        FormLayout formLayout = formLayoutDao.findByNameCrfId(formDataBean.getFormLayoutName(), crf.getCrfId());
        if (formLayout == null || (formLayout != null && !formLayout.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMLAYOUTOID_NOT_FOUND);
        }

        // Form Status is null , then Form has Initial Data Entry Status
        if (formDataBean.getEventCRFStatus() == null) {
            formDataBean.setEventCRFStatus(INITIAL_DATA_ENTRY);
        }

        // Form Status is not acceptable
        if (!formDataBean.getEventCRFStatus().equalsIgnoreCase(INITIAL_DATA_ENTRY) &&
                !formDataBean.getEventCRFStatus().equalsIgnoreCase(DATA_ENTRY_COMPLETE) &&
                !formDataBean.getEventCRFStatus().equalsIgnoreCase(COMPLETE)) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_STATUS_NOT_VALID);
        }

        return formLayout;
    }

    private boolean validateItemInGroup(Item item, ImportItemGroupDataBean itemGroupDataBean, CrfBean crf) {
        ItemGroup itemGroup = itemGroupDao.findByOcOIDCrfId(itemGroupDataBean.getItemGroupOID(), crf);
        for (ItemGroupMetadata itemGroupMetadata : itemGroup.getItemGroupMetadatas()) {
            if (itemGroupMetadata.getItem().getOcOid().equals(item.getOcOid())) {
                return true;
            }
        }
        return false;
    }
}
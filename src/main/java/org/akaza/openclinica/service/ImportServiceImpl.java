package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.submit.crfdata.*;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.JobService;
import core.org.akaza.openclinica.service.StudyEventService;
import core.org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

    @Autowired
    private ImportValidationService importValidationService;

    @Autowired
    private AuditLogEventDao auditLogEventDao;

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
    public static final String SKIPPED = "Skipped";
    public static final String ITEMDATA_SKIPPED_MSG = "No item value present";
    public static final String DiscrepancyNoteMessage = "import XML";

    public static final String PARTICIPANT_TYPE_KEYWORD = "bulk_action_log_participanct_type";
    public static final String EVENT_TYPE_KEYWORD = "bulk_action_log_event_type";
    public static final String SIGNATURE_TYPE_KEYWORD = "bulk_action_log_signature_type";
    public static final String FORM_TYPE_KEYWORD = "bulk_action_log_form_type";
    public static final String SDV_TYPE_KEYWORD = "bulk_action_log_sdv_status_type";
    public static final String ITEM_GROUP_TYPE_KEYWORD = "bulk_action_log_item_group_type";
    public static final String ITEM_TYPE_KEYWORD = "bulk_action_log_item_data_type";
    public static final String QUERY_TYPE_KEYWORD = "bulk_action_log_query_type";
    public static final String ANNOTATION_TYPE_KEYWORD = "bulk_action_log_annotation_type";
    public static final String DetailedNotes = "Update via Import";
    public static final String SDV_STATUS_UPDATED = "Sdv status imported";
    public static final String ITEMGROUP_REMOVED = "itemGroup removed";
    private static final String IMPORT_SIGNATURE_POSTFIX_KEYWORD = "import_signature_postfix";
    private static final String ATTESTATIONS_IMPORTED = "Attestations Imported";
    private static final String EVENT_IS_SIGNED = "Event is Signed";



    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    SimpleDateFormat sdf_logFile = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Transactional
    public boolean validateAndProcessDataImport(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail, boolean isSystemUserImport) {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        CoreResources.setRequestSchema(schema);
        Study tenantStudy;
        if (siteOid != null) {
            tenantStudy = studyDao.findByOcOID(siteOid);
        } else {
            tenantStudy = studyDao.findByOcOID(studyOid);
        }
        if (tenantStudy == null) {
            logger.error("Study {} Not Valid", tenantStudy.getOc_oid());
        }

        List<DataImportReport> dataImportReports = new ArrayList<>();
        DataImportReport dataImportReport;
        sdf_logFile.setTimeZone(TimeZone.getTimeZone("GMT"));

        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        String uniqueIdentifier = tenantStudy.getStudy() == null ? tenantStudy.getUniqueIdentifier() : tenantStudy.getStudy().getUniqueIdentifier();
        String envType = tenantStudy.getStudy() == null ? tenantStudy.getEnvType().toString() : tenantStudy.getStudy().getEnvType().toString();

        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        String fileName = uniqueIdentifier + DASH + envType + UNDERSCORE + JobType.XML_IMPORT + "_" + sdf_fileName.format(new Date()) + ".csv";

        logger.debug("Job Filename is : {}", fileName);

        Object subjectObject;
        StudySubject studySubject = null;

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        if (subjectDataBeans != null) {
            for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                if (subjectDataBean.getSubjectOID() != null)
                    subjectDataBean.setSubjectOID(subjectDataBean.getSubjectOID().toUpperCase());

                subjectObject = validateStudySubject(subjectDataBean, tenantStudy);

                if (subjectObject instanceof ErrorObj) {
                    logger.error("StudSubjectOID {} related issue", subjectDataBean.getSubjectOID());
                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), null, null, null, null, null, null, PARTICIPANT_TYPE_KEYWORD, ((ErrorObj) subjectObject).getCode(), null, ((ErrorObj) subjectObject).getMessage());
                    dataImportReports.add(dataImportReport);
                    continue;
                } else if (subjectObject instanceof StudySubject) {
                    studySubject = (StudySubject) subjectObject;
                }

                tenantStudy = studySubject.getStudy();

                ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
                for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                    if(studyEventDataBean.getWorkflowStatus() == null){
                        studyEventDataBean.setWorkflowStatusAsString(studyEventDataBean.getEventStatus());
                    }
                    if (studyEventDataBean.getStudyEventOID() != null)
                        studyEventDataBean.setStudyEventOID(studyEventDataBean.getStudyEventOID().toUpperCase());

                    Object eventObject = null;
                    StudyEvent studyEvent = null;

                    eventObject = validateStudyEvent(studyEventDataBean, studySubject, userAccount);
                    if (eventObject instanceof ErrorObj) {
                        ErrorObj errorObj = (ErrorObj) eventObject;
                        if(errorObj.getMessage().equals(ErrorConstants.ERR_FORMLAYOUTOID_NOT_FOUND)){
                            List<FormDataBean> tempformData = studyEventDataBean.getFormData();
                            String formOid = null;
                            if(tempformData.size() > 0 && tempformData.get(0).getFormOID() != null)
                                formOid = tempformData.get(0).getFormOID();
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formOid, null, null, null, FORM_TYPE_KEYWORD, errorObj.getCode(), null, errorObj.getMessage());
                        } else
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, EVENT_TYPE_KEYWORD, errorObj.getCode(), null, errorObj.getMessage());
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
                        Boolean proceedToSdv = true;
                        String reasonForChange = formDataBean.getReasonForChangeForCompleteForms();
                        formDataBeanCount++;
                        if (formDataBean.getFormOID() != null)
                            formDataBean.setFormOID(formDataBean.getFormOID().toUpperCase());

                        if (studyEventDefinition.getType().equals(COMMON) && formDataBeanCount > 1) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FORM_TYPE_KEYWORD, FAILED, null, ErrorConstants.ERR_FORM_MISSING_STUDY_EVENT_CONSTRUCT);
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOID {} related issue", formDataBean.getFormOID());
                            continue;
                        }

                        Object crfObject = null;
                        CrfBean crf = null;
                        try {
                            importValidationService.validateForm(formDataBean, tenantStudy, studyEventDefinition);
                            crf = crfDao.findByOcOID(formDataBean.getFormOID());
                        }catch (OpenClinicaSystemException e){
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FORM_TYPE_KEYWORD, e.getErrorCode(), null, e.getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOID {} related issue", formDataBean.getFormOID());
                            continue;
                        }

                        EventDefinitionCrf edc = null;

                        try {
                            importValidationService.validateEventDefnCrf(tenantStudy, studyEventDefinition, crf);
                            edc = createEventDefnCrf(tenantStudy, studyEventDefinition, crf);
                        }catch (OpenClinicaSystemException e){
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FORM_TYPE_KEYWORD, e.getErrorCode(), null, e.getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("FormOID {} related issue", formDataBean.getFormOID());
                            continue;
                        }

                        Object formLayoutObject = null;
                        FormLayout formLayout = null;

                        formLayoutObject = validateFormLayout(formDataBean, edc, crf);
                        if (formLayoutObject instanceof ErrorObj) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FORM_TYPE_KEYWORD, ((ErrorObj) formLayoutObject).getCode(), null, ((ErrorObj) formLayoutObject).getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("FormLayoutOID {} related issue", formDataBean.getFormOID());
                            continue;
                        } else if (formLayoutObject instanceof FormLayout) {
                            formLayout = (FormLayout) formLayoutObject;
                        }

                        EventCrf eventCrf = null;
                        try {
                            importValidationService.validateEventCrf(studySubject, studyEvent, formLayout, edc);
                            eventCrf = getEventCrf(studySubject, studyEvent, userAccount, crf, formLayout, formDataBean);
                        }catch (OpenClinicaSystemException e){
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), null, null, null, FORM_TYPE_KEYWORD, e.getErrorCode(), null, e.getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("EventCrf {} related issue", formDataBean.getFormOID());
                            continue;
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
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, ITEM_GROUP_TYPE_KEYWORD, ((ErrorObj) itemGroupObject).getCode(), null, ((ErrorObj) itemGroupObject).getMessage());
                                dataImportReports.add(dataImportReport);
                                proceedToSdv = false;
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
                                try {
                                    importValidationService.validateItem(itemDataBean, crf, itemGroupDataBean, itemCountInForm);
                                    itemObject = createOrUpdateItem(itemDataBean, crf, eventCrf, itemGroupDataBean, userAccount, itemCountInForm, tenantStudy, studySubject, reasonForChange);
                                }
                                catch (OpenClinicaSystemException e){
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), ITEM_TYPE_KEYWORD, e.getErrorCode(), null, e.getMessage());
                                    dataImportReports.add(dataImportReport);
                                    logger.error("ItemOID {} related issue: ", itemDataBean.getItemOID());
                                    continue;
                                }
                                if (itemObject instanceof DataImportReport) {
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
                                } else if (itemObject instanceof Item) {
                                    item = (Item) itemObject;
                                }
                                item = itemDao.findByOcOID(itemDataBean.getItemOID());
                                ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
                                for(DiscrepancyNoteBean discrepancyNoteBean : itemDataBean.getDiscrepancyNotes().getDiscrepancyNotes()){
                                    try {
                                        importValidationService.validateQuery(discrepancyNoteBean, itemData);
                                        createQuery(discrepancyNoteBean, tenantStudy, studySubject, eventCrf, itemDataBean.getItemOID(), itemGroupDataBean , itemData, null, null, true, dataImportReports, userAccount);
                                    }catch (OpenClinicaSystemException e){
                                        String insertionType = QUERY_TYPE_KEYWORD;
                                        if(discrepancyNoteBean.getNoteType() != null && discrepancyNoteBean.getNoteType().equalsIgnoreCase(QueryType.ANNOTATION.getName()))
                                            insertionType = ANNOTATION_TYPE_KEYWORD;
                                        for(ErrorObj err : e.getMultiErrors()){
                                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemDataBean.getItemOID(), insertionType, err.getCode(), null, err.getMessage());
                                            dataImportReports.add(dataImportReport);
                                        }
                                        logger.error("Query Import {} related issue: ", itemDataBean.getItemOID());
                                    }
                                }

                            }//itemDataBean for loop
                            try {
                                importValidationService.validateItemGroupRemoved(itemGroupDataBean, (ItemGroup) itemGroupObject);
                                if(BooleanUtils.isTrue(itemGroupDataBean.isRemoved())){
                                    removeItemGroup(tenantStudy, studySubject, eventCrf, (ItemGroup) itemGroupObject, itemGroupDataBean, userAccount);
                                    dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(),
                                            studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(),
                                            itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), null, ITEM_GROUP_TYPE_KEYWORD,
                                            ITEMGROUP_REMOVED, sdf_logFile.format(new Date()), null);
                                    dataImportReports.add(dataImportReport);
                                }
                            }catch (OpenClinicaSystemException e){
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(),
                                        studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(),
                                        null, ITEM_GROUP_TYPE_KEYWORD,  e.getErrorCode(), null, e.getMessage());
                                dataImportReports.add(dataImportReport);
                                logger.error("ItemGroupOID {} related issue", itemGroupDataBean.getItemGroupOID());
                            }


                        } //itemGroupDataBean for loop

                        /* OC-11606 for signed event---  has specific logic to handle event status
                         *  After data is successfully imported, and at lease one item/field is updated/imported, then
                         *  the form will still be Complete,
                         *  and the event will be changed back to data entry started.
                         *
                         *  if no data get updated/imported, even have successful process, still need to keep the event as signed,
                         *  so this need to skip the existing set event status logic
                         */
                        if (itemCountInForm.getInsertedUpdatedItemCountInForm() > 0) {
                            updateSdvStatusIfAlreadyVerified(eventCrf, userAccount);
                            updateEventAndSubjectStatusIfSigned(studyEvent, studySubject, userAccount);
                        }

                        if (formDataBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                            if (itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() == itemCountInForm.getItemCountInFormData()) {
                                // update eventcrf status into Complete\
                                eventCrf = updateEventCrf(eventCrf, userAccount, EventCrfWorkflowStatusEnum.COMPLETED, new Date());
                                openRosaSubmissionController.updateStudyEventStatus(tenantStudy.getStudy() != null ? tenantStudy.getStudy() : tenantStudy, studySubject, studyEventDefinition, studyEvent, userAccount);
                                logger.debug("Form {} status updated to Complete ", formDataBean.getFormOID());
                            }

                        } else if (itemCountInForm.getInsertedUpdatedItemCountInForm() > 0) {                         // update eventcrf status into data entry status
                            //AC3: Complete forms with data imported into them must stay in Complete status at the conclusion of the import.
                            if (this.isEventCrfCompleted(eventCrf)) {
                                ;
                            } else {
                                // Update Event Crf Status into Initial Data Entry
                                eventCrf = updateEventCrf(eventCrf, userAccount, EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY, null);
                            }
                        }

                        // check if all Forms within this Event is Complete
                        try {
                            if(!proceedToSdv)
                                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_SDV_STATUS_CANNOT_BE_UPDATED_BECAUSE_OF_ITEM_IMPORT_FAILURE);
                            importValidationService.validateSdvStatus(studySubject, formDataBean, eventCrf);
                            Boolean sdvImported = setSdvStatusOnEventCrf(formDataBean, eventCrf, userAccount);
                            if(sdvImported) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(),
                                        studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(),
                                        null, null, null, SDV_TYPE_KEYWORD, SDV_STATUS_UPDATED, sdf_logFile.format(new Date()), null);
                                dataImportReports.add(dataImportReport);
                            }
                        }catch (OpenClinicaSystemException e) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(),
                                    studyEventDataBean.getStudyEventOID(), studyEventDataBean.getStudyEventRepeatKey(), formDataBean.getFormOID(),
                                    null, null, null, SDV_TYPE_KEYWORD, e.getErrorCode(), null, e.getMessage());
                            dataImportReports.add(dataImportReport);
                            logger.error("Setting sdvStatus {} related issue", formDataBean.getFormOID());
                        }
                    } // formDataBean for loop
                    try{
                        if(studyEventDataBean.getSignatures() != null) {
                            importValidationService.validateSignatureForStudyEvent(studyEventDataBean, studyEvent, studySubject);
                            importSignatures(studyEventDataBean, studyEvent, userAccount);
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, SIGNATURE_TYPE_KEYWORD,  ATTESTATIONS_IMPORTED, sdf_logFile.format(new Date()), null);
                            dataImportReports.add(dataImportReport);
                            if (studyEventDataBean.getSigned() != null && studyEventDataBean.getSigned()) {
                                dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, SIGNATURE_TYPE_KEYWORD, EVENT_IS_SIGNED, sdf_logFile.format(new Date()), null);
                                dataImportReports.add(dataImportReport);
                            }
                        }
                    }catch (OpenClinicaSystemException e){
                        for (ErrorObj err : e.getMultiErrors()) {
                            dataImportReport = new DataImportReport(subjectDataBean.getSubjectOID(), subjectDataBean.getStudySubjectID(), studyEventDataBean.getStudyEventOID(), null, null, null, null, null, SIGNATURE_TYPE_KEYWORD, err.getCode(), null, err.getMessage());
                            dataImportReports.add(dataImportReport);
                        }
                        logger.error("Signature {} related issue", studyEventDataBean.getStudyEventOID());
                    }

                } // StudyEventDataBean for loop
            } // StudySubjectDataBean for loop
        } else { // subjectDataBean ==null
            dataImportReport = new DataImportReport(null, null, null, null, null, null, null, null,  SIGNATURE_TYPE_KEYWORD, FAILED, null, ErrorConstants.ERR_SUBJECT_DATA_MISSING);
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
        stringBuffer.append("Type");
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
            stringBuffer.append(dataImportReport.getType() != null ? dataImportReport.getType() : "");
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

    private ItemData createItemData(EventCrf eventCrf, String itemDataValue, UserAccount userAccount, Item item, int groupRepeatKey) {
        saveEventCrf(eventCrf, userAccount);
        ItemData itemData = new ItemData();
        itemData.setEventCrf(eventCrf);
        itemData.setItem(item);
        itemData.setDeleted(false);
        itemData.setValue(itemDataValue);
        itemData.setUserAccount(userAccount);
        itemData.setDateCreated(new Date());
        itemData.setOrdinal(groupRepeatKey);
        logger.debug("Creating new Item Data");
        itemData = itemDataDao.saveOrUpdate(itemData);
        return itemData;
    }

    private ItemData updateItemData(ItemData itemData, UserAccount userAccount, String value) {
        itemData.setValue(value);
        itemData.setDateUpdated(new Date());
        itemData.setUpdateId(userAccount.getUserId());
        itemData = itemDataDao.saveOrUpdate(itemData);
        logger.debug("Updating Item Data Id {}", itemData.getItemDataId());
        return itemData;
    }

    private EventCrf saveSdvStatus(EventCrf eventCrf, SdvStatus sdvStatus, UserAccount userAccount){
        eventCrf.setSdvStatus(sdvStatus);
        if(sdvStatus.equals(SdvStatus.VERIFIED))
            eventCrf.setLastSdvVerifiedDate(new Date());
        eventCrf.setSdvUpdateId(userAccount.getUserId());
        return saveEventCrf(eventCrf, userAccount);
    }

    public EventCrf saveEventCrf(EventCrf eventCrf, UserAccount userAccount){
        // only created new event crf once
        if (eventCrf.getEventCrfId() == 0) {
            if(eventCrf.getStudyEvent().getStudyEventId() == 0)
            {
                eventCrf.setStudyEvent(studyEventDao.saveOrUpdate(eventCrf.getStudyEvent()));
            }
            updateStudyEvntStatus(eventCrf.getStudyEvent(), userAccount, DATA_ENTRY_STARTED);
        }
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        return eventCrf;
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
        eventCrf.setWorkflowStatus(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY);
        eventCrf.setCompletionStatus(completionStatusDao.findByCompletionStatusId(1));// setCompletionStatusId(1);
        eventCrf.setStudySubject(studySubject);
        eventCrf.setStudyEvent(studyEvent);
        eventCrf.setValidateString("");
        eventCrf.setValidatorAnnotations("");
        eventCrf.setDateUpdated(new Date());
        eventCrf.setValidatorId(0);
        eventCrf.setSdvUpdateId(0);
        eventCrf.setSdvStatus(null);
        logger.debug("Creating new Event Crf");

        return eventCrf;
    }

    private EventCrf updateEventCrf(EventCrf eventCrf, UserAccount userAccount, EventCrfWorkflowStatusEnum workflow, Date dateCompleted) {
        eventCrf.setDateUpdated(new Date());
        eventCrf.setUpdateId(userAccount.getUserId());
        eventCrf.setWorkflowStatus(workflow);
        if (dateCompleted != null)
            eventCrf.setDateCompleted(dateCompleted);
        eventCrf = eventCrfDao.saveOrUpdate(eventCrf);
        logger.debug("Updating Event Crf Id {}", eventCrf.getEventCrfId());
        return eventCrf;
    }

    public StudyEvent buildStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, int ordinal,
                                       UserAccount userAccount, String startDate, String endDate) {

        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(ordinal);
        studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);
        studyEvent.setStudySubject(studySubject);
        studyEvent.setDateCreated(new Date());
        studyEvent.setUserAccount(userAccount);

        setEventStartAndEndDate(studyEvent, startDate, endDate);

        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        logger.debug("Creating new Study Event");
        return studyEvent;
    }


    public StudyEvent updateStudyEventDatesAndStatus(StudyEvent studyEvent, UserAccount userAccount, String startDate, String endDate, String eventStatus) {
        StudyEventWorkflowStatusEnum newEventStatus = getWorkflowStatus(eventStatus);
        if( !studyEvent.getWorkflowStatus().equals(newEventStatus) && studyEvent.isCurrentlySigned())
            studyEvent.setSigned(Boolean.FALSE);
        studyEvent.setWorkflowStatus(getWorkflowStatus(eventStatus));
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
        StudyEventWorkflowStatusEnum newEventStatus = getWorkflowStatus(eventStatus);
        if( !studyEvent.getWorkflowStatus().equals(newEventStatus) && studyEvent.isCurrentlySigned())
            studyEvent.setSigned(Boolean.FALSE);
        studyEvent.setWorkflowStatus(newEventStatus);
        studyEvent.setDateUpdated(new Date());
        studyEvent.setUpdateId(userAccount.getUserId());
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        logger.debug("Updating Study Event Id {}", studyEvent.getStudyEventId());
        return studyEvent;
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

    private Object getFormLayout(StudyEventDataBean studyEventDataBean) {
        String formOid = studyEventDataBean.getFormData().get(0).getFormOID();
        if (formOid != null) formOid = formOid.toUpperCase();
        String formLayoutName = studyEventDataBean.getFormData().get(0).getFormLayoutName();
        CrfBean crf = crfDao.findByOcOID(formOid);
        if (crf == null || (crf != null && !crf.getStatus().equals(Status.AVAILABLE))) {
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }

        FormLayout formLayout = formLayoutDao.findByNameCrfId(formLayoutName, crf.getCrfId());
        if(formLayout == null)
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORMLAYOUTOID_NOT_FOUND);
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
            if (eventCrfs.size() > 0) {
            	eventCrf = eventCrfs.get(0);
            	break;
            }
            
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

            if (studyEventDefinition.isRepeating()) {   // Repeating Common Event
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

                        // If the event is not imported correctly, StudyEvent will be in Scheduled state,
                        // So checking the errorCode.repeatKeyAndFormMismatch only on other event status
                        if(!studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED) && !studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)){
                            List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getCrf().getCrfId());
                            if (eventCrfs.size() == 0 || eventCrfs.get(0) == null)
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

                    studyEvent = scheduleEventForImport(studyEventDataBean, studySubject, studyEventDefinition, userAccount);
                    return studyEvent;
                }
            }
        } else {   // Visit Event

            if (studyEventDefinition.isRepeating()) {   // Repeating Visit Event
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
                                studyEvent.isCurrentlyLocked() ||
                                studyEvent.isCurrentlyRemoved() ||
                                studyEvent.isCurrentlyArchived() ||
                        studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SKIPPED)  ||
                        studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.STOPPED) )) {

            errorObj = new ErrorObj(FAILED, ErrorConstants.ERR_EVENT_NOT_AVAILABLE);
        }


        return errorObj;
    }


    private boolean isEventCrfCompleted(EventCrf eventCrf) {
        return eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED);

    }

    /**
     * @param studyEventDataBean
     * @return
     */

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
        StudyEvent studyEvent = buildStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
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
        StudyEventWorkflowStatusEnum workflowStatus = getWorkflowStatus(eventStatus);
        if (workflowStatus == null)
            return new ErrorObj(FAILED, ErrorConstants.ERR_INVALID_EVENT_TRANSITION_STATUS);

        if (!studyEvent.getWorkflowStatus().equals(workflowStatus) ) {
            if (!(workflowStatus.equals(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED)
                    && (studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)
                    || studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SKIPPED) ))
                    &&
                    !(workflowStatus.equals(StudyEventWorkflowStatusEnum.COMPLETED)
                            && studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED))
                    &&
                    !(workflowStatus.equals(StudyEventWorkflowStatusEnum.STOPPED)
                            && studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED) )
                    &&
                    !(workflowStatus.equals(StudyEventWorkflowStatusEnum.SKIPPED)
                            && studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED) )
                    /****************  &&
                    !(workflowStatus.equals(StudyEventWorkflowEnum.LOCKED)
                            && (studyEvent.getWorkflowStatus().equals(StudyEventWorkflowEnum.COMPLETED)
                            || studyEvent.getWorkflowStatus().equals(StudyEventWorkflowEnum.SKIPPED)))*/
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

    private StudyEventWorkflowStatusEnum getWorkflowStatus(String workflowStatus) {
        if (StudyEventWorkflowStatusEnum.SCHEDULED.toString().equalsIgnoreCase(workflowStatus)) {
            return StudyEventWorkflowStatusEnum.SCHEDULED;
        } else if (StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED.toString().replace("_", " ").equalsIgnoreCase(workflowStatus)) {
            return StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED;
        } else if (StudyEventWorkflowStatusEnum.COMPLETED.toString().equalsIgnoreCase(workflowStatus)) {
            return StudyEventWorkflowStatusEnum.COMPLETED;
        } else if (StudyEventWorkflowStatusEnum.SKIPPED.toString().equalsIgnoreCase(workflowStatus)) {
            return StudyEventWorkflowStatusEnum.SKIPPED;
        } else if (StudyEventWorkflowStatusEnum.STOPPED.toString().equalsIgnoreCase(workflowStatus)) {
            return StudyEventWorkflowStatusEnum.STOPPED;
       /************** } else if (StudyEventWorkflowEnum.LOCKED.getDisplayValue().equalsIgnoreCase(workflowStatus)) {
            return StudyEventWorkflowEnum.LOCKED;*/
        }
        return null;
    }


    public ErrorObj validateEventStatus(String eventStatus) {
        if (!StudyEventWorkflowStatusEnum.SCHEDULED.toString().equalsIgnoreCase(eventStatus)
                && !StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED.toString().replace("_", " ").equalsIgnoreCase(eventStatus)
                && !StudyEventWorkflowStatusEnum.COMPLETED.toString().equalsIgnoreCase(eventStatus)
                && !StudyEventWorkflowStatusEnum.SKIPPED.toString().equalsIgnoreCase(eventStatus)
                && !StudyEventWorkflowStatusEnum.STOPPED.toString().equalsIgnoreCase(eventStatus)
/********************
                && !StudyEventWorkflowEnum.LOCKED.getDisplayValue().equalsIgnoreCase(eventStatus)
*/  )

        {
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


    private EventCrf getEventCrf(StudySubject studySubject, StudyEvent studyEvent, UserAccount userAccount,
                                 CrfBean crf, FormLayout formLayout, FormDataBean formDataBean) {

        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());

        // Event Crf has status complete or invalid
        // in complete status will not throw out error any more at this stage


        if (eventCrf == null) {
            List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), crf.getCrfId());
            if (eventCrfs.size() > 0) {
                eventCrf = eventCrfs.get(0);
                formDataBean.setFormLayoutName(eventCrf.getFormLayout().getXformName());
            }
        }

        if (eventCrf == null) {
            eventCrf = createEventCrf(studySubject, studyEvent, formLayout, userAccount);

            logger.debug("new EventCrf Id {} is created  ", eventCrf.getEventCrfId());

            logger.debug("Study Event Id {} is updated", studyEvent.getStudyEventId());
        }

        return eventCrf;
    }

    public boolean setSdvStatusOnEventCrf(FormDataBean formDataBean, EventCrf eventCrf, UserAccount userAccount){
        if(StringUtils.isNotBlank(formDataBean.getSdvStatusString())) {
            SdvStatus newSdvStatus = formDataBean.getSdvStatus();
            if (newSdvStatus != null && (eventCrf.getSdvStatus() == null || !eventCrf.getSdvStatus().equals(newSdvStatus))) {
                if (newSdvStatus.equals(SdvStatus.VERIFIED)) {
                    if (eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED) && !eventCrf.isCurrentlyRemoved()
                            && !eventCrf.isCurrentlyArchived()) {
                        saveSdvStatus(eventCrf, newSdvStatus, userAccount);
                        return true;
                    }
                } else {
                    saveSdvStatus(eventCrf, newSdvStatus, userAccount);
                    return true;
                }
            }
        }
        return false;
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


    private DataImportReport createOrUpdateItem(ImportItemDataBean itemDataBean, CrfBean crf, EventCrf eventCrf, ImportItemGroupDataBean itemGroupDataBean, UserAccount userAccount, ItemCountInForm itemCountInForm, Study study, StudySubject studySubject, String reasonForChange) {
        ErrorObj errorObj = null;
        if(itemDataBean.getValue() == null)
            return new DataImportReport(null, null, null, null, null, null, null, null, ITEM_TYPE_KEYWORD, SKIPPED, null, ITEMDATA_SKIPPED_MSG);
        Item item = itemDao.findByOcOID(itemDataBean.getItemOID());
        ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));

        if (itemData != null) {
            if (itemData.getValue().equals(itemDataBean.getValue())) {
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                return new DataImportReport(null, null, null, null, null, null, null, null, ITEM_TYPE_KEYWORD, NO_CHANGE, null, null);

            } else {
                if (isEventCrfCompleted(eventCrf)) {
                    createReasonForChangeQuery(userAccount, study, studySubject, itemGroupDataBean, itemData, reasonForChange);
                }
                itemData = updateItemData(itemData, userAccount, itemDataBean.getValue());
                itemCountInForm.setInsertedUpdatedItemCountInForm(itemCountInForm.getInsertedUpdatedItemCountInForm() + 1);
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                return new DataImportReport(null, null, null, null, null, null, null, null, ITEM_TYPE_KEYWORD, UPDATED, sdf_logFile.format(new Date()), null);
            }
        } else {
            itemData = createItemData(eventCrf, itemDataBean.getValue(), userAccount, item, Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
            if (isEventCrfCompleted(eventCrf)) {
                createReasonForChangeQuery(userAccount, study, studySubject, itemGroupDataBean, itemData, reasonForChange);
            }
            itemCountInForm.setInsertedUpdatedItemCountInForm(itemCountInForm.getInsertedUpdatedItemCountInForm() + 1);
            itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
            return new DataImportReport(null, null, null, null, null, null, null, null, ITEM_TYPE_KEYWORD, INSERTED, sdf_logFile.format(new Date()), null);
        }
    }

    /**
     * @param userAccount
     * @param study
     * @param studySubject
     * @param itemData
     */
    private ErrorObj createReasonForChangeQuery(UserAccount userAccount, Study study, StudySubject studySubject, ImportItemGroupDataBean itemGroupDataBean, ItemData itemData, String reasonForChange) {

        ErrorObj eb = null;
        DiscrepancyNoteBean discrepancyNoteBean = new DiscrepancyNoteBean();
        discrepancyNoteBean.setNoteType(QueryType.REASON.getName());
        ChildNoteBean childNoteBean = new ChildNoteBean();

        if (reasonForChange != null && !(reasonForChange.isEmpty())) {
            childNoteBean.setDetailedNote(reasonForChange);
        } else {
            childNoteBean.setDetailedNote(this.DetailedNotes);
        }
        childNoteBean.setOwnerUserName(userAccount.getUserName());
        discrepancyNoteBean.getChildNotes().add(childNoteBean);
        createQuery(discrepancyNoteBean, study, studySubject, itemData.getEventCrf(), itemData.getItem().getOcOid(), itemGroupDataBean,  itemData, null, null, true, new ArrayList<DataImportReport>(), userAccount);
        return eb;
    }

    private DiscrepancyNote createQuery(DiscrepancyNoteBean discrepancyNoteBean, Study study, StudySubject studySubject,
             EventCrf eventCrf, String itemOid, ImportItemGroupDataBean itemGroupDataBean,ItemData itemData, ChildNoteBean childNoteBean,
             DiscrepancyNote parentDn, Boolean isParentDn, List<DataImportReport> dataImportReports, UserAccount importerUserAccount) {

        SubmissionContainer container = new SubmissionContainer();
        container.setStudy(study);
        container.setSubject(studySubject);
        QueryServiceHelperBean helperBean = new QueryServiceHelperBean();
        if(itemData == null) {
            Item item = itemDao.findByOcOID(itemOid);
            itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
            if(itemData == null){
                itemData = createItemData(eventCrf, "", importerUserAccount, item, Integer.parseInt(itemGroupDataBean.getItemGroupRepeatKey()));
            }
        }
        helperBean.setItemData(itemData);
        helperBean.setContainer(container);

        DiscrepancyNote discNote = null;
        QueryBean queryBean = new QueryBean();
        String displayId = isParentDn ? discrepancyNoteBean.getDisplayId() : childNoteBean.getDisplayId();
        Boolean isNoteCreated = false;
        if(displayId != null)
            discNote = discrepancyNoteDao.findByDisplayIdWithoutNotePrefix(displayId);
        if(discNote == null) {
            isNoteCreated = true;
            queryBean.setType(discrepancyNoteBean.getNoteType());
            queryBean.setDisplayId(displayId);
            if (!isParentDn){
                queryBean.setComment(childNoteBean.getDetailedNote());
                queryBean.setStatus(childNoteBean.getStatus());
                queryBean.setUser(childNoteBean.getOwnerUserName());
                UserAccount userAccount = userAccountDao.findByUserName(childNoteBean.getOwnerUserName());
                helperBean.getContainer().setUser(userAccount);
                if(childNoteBean.getUserRef() != null)
                    queryBean.setAssigned_to(childNoteBean.getUserRef().getUserName());
            }
            else
                queryBean.setComment("");
            discNote = (DiscrepancyNote) queryService.createQuery(helperBean, queryBean, isParentDn);
            if (isParentDn)
                discNote.setThreadUuid(UUID.randomUUID().toString());
            else {
                discNote.setThreadUuid(parentDn.getThreadUuid());
                discNote.setParentDiscrepancyNote(parentDn);
                helperBean.setParentDn(parentDn);
            }
            helperBean.setDn(discNote);
            discNote = discrepancyNoteDao.saveOrUpdate(discNote);
            queryService.saveQueryItemDatamap(helperBean);
            String insertionType = QUERY_TYPE_KEYWORD;
            if(discrepancyNoteBean.getNoteType().equalsIgnoreCase(QueryType.ANNOTATION.getName()))
                insertionType = ANNOTATION_TYPE_KEYWORD;
            DataImportReport dataImportReport =  new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(),
                    eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid(), eventCrf.getStudyEvent().getSampleOrdinal().toString(),
                    eventCrf.getFormLayout().getCrf().getOcOid(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemOid, insertionType ,discNote.getDisplayId()+" "+INSERTED, sdf_logFile.format(new Date()),  null);
            dataImportReports.add(dataImportReport);
        }
        if (isParentDn) {
            for(int i = 0 ; i < discrepancyNoteBean.getChildNotes().size();i++){
                DiscrepancyNote childNote = createQuery(discrepancyNoteBean, study, studySubject, eventCrf, itemOid, itemGroupDataBean, itemData, discrepancyNoteBean.getChildNotes().get(i), discNote, false, dataImportReports, importerUserAccount);
                if(i == discrepancyNoteBean.getChildNotes().size()-1){
                    discNote.setResolutionStatus(childNote.getResolutionStatus());
                    discNote.setUserAccount(childNote.getUserAccount());
                    discNote.setDetailedNotes(childNote.getDetailedNotes());
                    discNote.setUserAccountByOwnerId(childNote.getUserAccountByOwnerId());
                    discNote = discrepancyNoteDao.saveOrUpdate(discNote);
                    if(!isNoteCreated){
                        String insertionType = QUERY_TYPE_KEYWORD;
                        if(discrepancyNoteBean.getNoteType().equalsIgnoreCase(QueryType.ANNOTATION.getName()))
                            insertionType = ANNOTATION_TYPE_KEYWORD;
                        DataImportReport dataImportReport =  new DataImportReport(studySubject.getOcOid(), studySubject.getLabel(),
                                eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid(), eventCrf.getStudyEvent().getSampleOrdinal().toString(),
                                eventCrf.getFormLayout().getCrf().getOcOid(), itemGroupDataBean.getItemGroupOID(), itemGroupDataBean.getItemGroupRepeatKey(), itemOid, insertionType,discNote.getDisplayId()+" "+UPDATED, sdf_logFile.format(new Date()),  null);
                        dataImportReports.add(dataImportReport);
                    }
                }
            }
        }
        return  discNote;
    }

    private EventDefinitionCrf createEventDefnCrf(Study tenantStudy, StudyEventDefinition studyEventDefinition, CrfBean crf) {
        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(),
                crf.getCrfId(), tenantStudy.getStudyId());
        if (edc == null) {
            edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                    tenantStudy.getStudy().getStudyId());
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

        if(formDataBean.getWorkflowStatus() == null && formDataBean.getEventCRFStatus() != null){
            formDataBean.setWorkflowStatusAsString(formDataBean.getEventCRFStatus());
            if(formDataBean.getWorkflowStatus() == null){
                return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_STATUS_NOT_VALID);
            }
        }
        // Form Status is null , then Form has Initial Data Entry Status
        if (formDataBean.getWorkflowStatus() == null) {
            formDataBean.setWorkflowStatus(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY);
        }
        if(!formDataBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY)
                && !formDataBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)){
            return new ErrorObj(FAILED, ErrorConstants.ERR_FORM_STATUS_NOT_VALID);
        }

        return formLayout;
    }



    private void removeItemGroup(Study study, StudySubject studySubject, EventCrf eventCrf, ItemGroup itemGroup, ImportItemGroupDataBean igDataBean, UserAccount userAccount) {
        List<ItemData> itemDataList = (ArrayList<ItemData>)itemDataDao.findByEventCrfGroup(eventCrf.getEventCrfId(), itemGroup.getItemGroupId());
        CrfVersion crfVersion = crfVersionDao.findAllByCrfId(eventCrf.getFormLayout().getCrf().getCrfId()).get(0);
        for(ItemData itemData : itemDataList){
            ItemGroupMetadata igm = itemGroupMetadataDao.findByItemCrfVersion(itemData.getItem().getItemId(), crfVersion.getCrfVersionId());
            if(igm.isRepeatingGroup() && itemData.getOrdinal() == Integer.parseInt(igDataBean.getItemGroupRepeatKey()) && !itemData.isDeleted()){
                itemData.setDeleted(true);
                updateItemData(itemData, userAccount, itemData.getValue());
                queryService.closeItemDiscrepancyNotesForItemData(study, userAccount, studySubject, itemData);
                logger.debug("Updating Item Data Id {}", itemData.getItemDataId());
            }
        }
    }
    public void updateEventAndSubjectStatusIfSigned(StudyEvent studyEvent, StudySubject studySubject, UserAccount userAccount) {
        if (studyEvent.isCurrentlySigned()) {
            studyEvent.setSigned(Boolean.FALSE);
            studyEvent.setUpdateId(userAccount.getUserId());
            studyEvent.setDateUpdated(new Date());
            studyEventDao.saveOrUpdate(studyEvent);
        }

        if (studySubject.getStatus().equals(Status.SIGNED)) {
            studySubject.setStatus(Status.AVAILABLE);
            studySubject.setUpdateId(userAccount.getUserId());
            studySubject.setDateUpdated(new Date());
            studySubjectDao.saveOrUpdate(studySubject);
        }
    }

    public StudyEvent scheduleEventForImport(StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount) {
        //StudyEvent is actually inserted in db inside createItemData() function
        StudyEvent studyEvent = buildStudyEvent(studySubject, studyEventDefinition, Integer.parseInt(studyEventDataBean.getStudyEventRepeatKey()), userAccount, studyEventDataBean.getStartDate(), studyEventDataBean.getEndDate());
        logger.debug("Scheduling new Visit Base  Event ID {}", studyEvent.getStudyEventId());
        return studyEvent;
    }

    public void importSignatures(StudyEventDataBean studyEventDataBean, StudyEvent studyEvent, UserAccount userAccount){
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(Locale.ENGLISH);
        AuditLogEventType auditLogEventType = new AuditLogEventType();
        auditLogEventType.setAuditLogEventTypeId(65);
        List<SignatureBean> signatureBeans = studyEventDataBean.getSignatures();
        boolean manuallyImportToAuditLog = true;
        for(SignatureBean signatureBean: signatureBeans){

            String attestationMsg = signatureBean.getAttestation().concat(resword.getString(IMPORT_SIGNATURE_POSTFIX_KEYWORD));
            AuditLogEvent auditLogEvent = new AuditLogEvent();
            auditLogEvent.setNewValue("false");

            //condition to check if this is the last signature bean
            if(signatureBean.equals(signatureBeans.get(signatureBeans.size()-1)) && studyEventDataBean.getSigned() != null && studyEventDataBean.getSigned()){
                    //Auto-insert to audit_log_event won't get triggered if the signed status and attestation is same
                    if(!studyEvent.isCurrentlySigned() || studyEvent.getAttestation() == null || !studyEvent.getAttestation().equals(attestationMsg))
                        manuallyImportToAuditLog = false;
                    else {
                        auditLogEvent.setNewValue("true");
                        if(studyEvent.getSigned() != null)
                            auditLogEvent.setOldValue(studyEvent.getSigned().toString());
                    }
                    studyEvent.setAttestation(attestationMsg);
                    studyEvent.setSigned(true);
                    studyEvent.setUpdateId(userAccount.getUserId());
                    studyEvent.setDateUpdated(new Date());
                    studyEventDao.saveOrUpdate(studyEvent);
            }
            if(manuallyImportToAuditLog) {
                auditLogEvent.setAuditDate(new Date());
                auditLogEvent.setAuditTable("study_event");
                auditLogEvent.setUserAccount(userAccount);
                auditLogEvent.setEntityId(studyEvent.getStudyEventId());
                auditLogEvent.setEntityName("Signed");
                auditLogEvent.setAuditLogEventType(auditLogEventType);
                auditLogEvent.setDetails(attestationMsg);
                auditLogEventDao.saveOrUpdate(auditLogEvent);
            }
        }
    }

    private void updateSdvStatusIfAlreadyVerified(EventCrf eventCrf, UserAccount userAccount) {
        if(eventCrf.getSdvStatus() != null && eventCrf.getSdvStatus().equals(SdvStatus.VERIFIED)){
            eventCrf.setSdvStatus(SdvStatus.CHANGED_SINCE_VERIFIED);
            eventCrf.setUpdateId(userAccount.getUserId());
            eventCrfDao.saveOrUpdate(eventCrf);
        }
    }
}
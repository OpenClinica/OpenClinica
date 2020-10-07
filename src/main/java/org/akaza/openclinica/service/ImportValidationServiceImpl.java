package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.submit.crfdata.*;
import core.org.akaza.openclinica.core.form.xform.QueryType;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.StudyEventService;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.controller.dto.DataImportReport;
import org.akaza.openclinica.controller.helper.table.ItemCountInForm;
import org.akaza.openclinica.controller.openrosa.QueryService;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("importValidationService")
public class ImportValidationServiceImpl implements ImportValidationService{
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;
    @Autowired
    private ItemGroupDao itemGroupDao;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    @Autowired
    private CrfDao crfDao;
    @Autowired
    private StudyEventService studyEventService;
    @Autowired
    private AuditLogEventDao auditLogEventDao;
    @Autowired
    private QueryService queryService;

    public static final String FAILED = "Failed";
    public static final String NO_CHANGE_IN_QUERIES="No change in queries";
    public static final String QUERY_KEYWORD = "Query";
    private static final String STATUS_ATTRIBUTE_TRUE = "Yes";
    private static final String STATUS_ATTRIBUTE_FALSE = "No";
    private static final String IMPORT_SIGNATURE_POSTFIX_KEYWORD = "import_signature_postfix";
    private boolean isQueryNewStatusValid;
    private boolean isQueryUpdatedStatusValid;
    private boolean isQueryClosedStatusValid;
    private boolean isQueryClosedModifiedStatusValid;

    public void validateQuery(DiscrepancyNoteBean discrepancyNoteBean, ItemData itemData){
        ArrayList<ErrorObj> errors = new ArrayList<>();
        isQueryNewStatusValid = true;
        isQueryUpdatedStatusValid = false;
        isQueryClosedStatusValid = false;
        isQueryClosedModifiedStatusValid = false;
        Set<String> newDisplayNodeIds = new HashSet<>();

        boolean newQueriesStarted = false;
        boolean isResolutionTypeAnnotation = false;
        if(discrepancyNoteBean.getNoteType() == null)
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_DISCREPANCY_NOTE_TYPE));
        else if(!checkDiscrepancyNoteTypeValid(discrepancyNoteBean.getNoteType()))
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_TYPE_NOT_VALID));
        isResolutionTypeAnnotation = QueryType.ANNOTATION.getName().equalsIgnoreCase(discrepancyNoteBean.getNoteType());
        if(isResolutionTypeAnnotation && discrepancyNoteBean.getChildNotes().size() > 1)
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ANNOTATION_MUST_HAVE_ONE_CHILD_NOTE));
        DiscrepancyNote parentDN =null;
        if(discrepancyNoteBean.getDisplayId() != null) {
            newDisplayNodeIds.add(discrepancyNoteBean.getDisplayId());
            parentDN = discrepancyNoteDao.findByDisplayIdWithoutNotePrefix(discrepancyNoteBean.getDisplayId());
            if (parentDN != null) {
                if(itemData != null && (parentDN.getDnItemDataMaps() == null || parentDN.getDnItemDataMaps().size() == 0 ||
                        parentDN.getDnItemDataMaps().get(0).getItemData().getItemDataId() != itemData.getItemDataId()))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_EXISTING_NOTE_ID_IN_OTHER_ITEM));
                if(parentDN.getParentDiscrepancyNote() != null)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_NOTE_ID_ALREADY_IN_USE));
                setResolutionStatusForCheckingChildNotesValidity(parentDN.getResolutionStatus().getName());
            }
            if(discrepancyNoteBean.getDisplayId().length() > 32)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_ID_TOO_LONG));
        }
        if(discrepancyNoteBean.getChildNotes() == null || discrepancyNoteBean.getChildNotes().size() == 0)
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_CHILD_NOTES_NOT_AVAILABLE_IN_IMPORT_FILE));
        for(ChildNoteBean childNoteBean : discrepancyNoteBean.getChildNotes()){
            boolean discrepancyNoteStatusValid = true;
            if(!isResolutionTypeAnnotation) {
                if (childNoteBean.getStatus() == null) {
                    discrepancyNoteStatusValid = false;
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_DISCREPANCY_NOTE_STATUS));
                }
                else if(!checkDiscrepancyNoteStatusValid(childNoteBean.getStatus())) {
                    discrepancyNoteStatusValid = false;
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_STATUS_NOT_VALID));
                }
            }
            if(childNoteBean.getOwnerUserName() == null)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_USER_NAME));
            else if(!isUserExist(childNoteBean.getOwnerUserName()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_USER_NOT_VALID));
            if(!isResolutionTypeAnnotation && childNoteBean.getUserRef() != null && !isUserExist(childNoteBean.getUserRef().getUserName()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ASSIGNED_USER_NOT_VALID));
            if(StringUtils.isBlank(childNoteBean.getDetailedNote()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DETAILED_NOTE_MISSING));
            else if(childNoteBean.getDetailedNote().length() > 1000)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DETAILED_NOTE_TOO_LONG));
            DiscrepancyNote childDN = null;
            if(childNoteBean.getDisplayId() != null) {
                if(newDisplayNodeIds.contains(childNoteBean.getDisplayId()))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_ID_IS_REDUNDANT));
                newDisplayNodeIds.add(childNoteBean.getDisplayId());
                if(childNoteBean.getDisplayId().length() > 32)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_ID_TOO_LONG));
                childDN = discrepancyNoteDao.findByDisplayIdWithoutNotePrefix(childNoteBean.getDisplayId());
            }
            if (childDN == null) {
                    if (!isResolutionTypeAnnotation && discrepancyNoteStatusValid && !isChildStatusApplicable(childNoteBean.getStatus()))
                        errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_QUERY_STATUS_NOT_APPLICABLE));
                    setResolutionStatusForCheckingChildNotesValidity(childNoteBean.getStatus());
                    newQueriesStarted = true;
            }else{
                if(itemData != null && (childDN.getDnItemDataMaps() == null || childDN.getDnItemDataMaps().size() == 0 ||
                        childDN.getDnItemDataMaps().get(0).getItemData().getItemDataId() != itemData.getItemDataId()))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_EXISTING_NOTE_ID_IN_OTHER_ITEM));
                if(childDN.getParentDiscrepancyNote() == null)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_NOTE_ID_ALREADY_IN_USE));
                if(parentDN == null)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_NOTE_ID_ALREADY_IN_USE));
                else if(!childDN.getParentDiscrepancyNote().getDisplayId().equalsIgnoreCase(parentDN.getDisplayId()))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_NOTE_ID_ALREADY_IN_USE));
                if(!isResolutionTypeAnnotation && newQueriesStarted)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_NEW_QUERIES_IN_BETWEEN_OLD_QUERIES));
            }

            if(parentDN != null && isResolutionTypeAnnotation
                    && (childNoteBean.getDisplayId() == null || childDN == null)){
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ANNOTATION_MUST_HAVE_ONE_CHILD_NOTE));
            }
            childNoteBean.setDisplayId(validateAndGenerateNewDisplayId(childNoteBean.getDisplayId(), false, errors));
        }

        discrepancyNoteBean.setDisplayId(validateAndGenerateNewDisplayId(discrepancyNoteBean.getDisplayId(), true, errors));
        if(errors.size() ==0 && !newQueriesStarted){
            errors.add(new ErrorObj( NO_CHANGE_IN_QUERIES, ""));
        }
        if(errors.size() > 0){
            throw new OpenClinicaSystemException(FAILED, errors);
        }
    }

    public void validateItem(ImportItemDataBean itemDataBean, CrfBean crf, ImportItemGroupDataBean itemGroupDataBean, ItemCountInForm itemCountInForm) {
        if (itemDataBean.getItemOID() == null) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEM_NOT_FOUND);
        }

        Item item = itemDao.findByOcOID(itemDataBean.getItemOID());
        // ItemOID is not valid
        if (item == null || (item != null && !item.getStatus().equals(Status.AVAILABLE))) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEM_NOT_FOUND);
        }else {
            validateItemInGroup(item, itemGroupDataBean, crf);
            if (itemDataBean.getValue() == null) {
            } else if (itemDataBean.getValue().length() > 3999) {
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_VALUE_TOO_LONG);
            } else if (StringUtils.isNotEmpty(itemDataBean.getValue())) {
                validateItemDataType(item, itemDataBean.getValue(), itemCountInForm);
                Set<ItemFormMetadata> ifms = item.getItemFormMetadatas();
                ResponseSet responseSet = ifms.iterator().next().getResponseSet();
                validateResponseSets(responseSet, itemDataBean.getValue(), itemCountInForm);
            }
        }
    }

    public void validateEventCrf(StudySubject studySubject, StudyEvent studyEvent, FormLayout formLayout, EventDefinitionCrf edc) {

        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());

        // Event Crf has status complete or invalid
        // in complete status will not throw out error any more at this stage


        if (eventCrf != null) {
            if(eventCrf.isCurrentlyArchived() || eventCrf.isCurrentlyRemoved()){
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORM_NOT_AVAILABLE);
            }
        }else {
            String selectedVersionIds = edc.getSelectedVersionIds();
            if (!StringUtils.isEmpty(selectedVersionIds)) {
                String[] ids = selectedVersionIds.split(",");
                ArrayList<Integer> idList = new ArrayList<Integer>();
                for (String id : ids) {
                    idList.add(Integer.valueOf(id));
                }
                if (!idList.contains(formLayout.getFormLayoutId()))
                    throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORMLAYOUTOID_NOT_AVAILABLE);
            }
        }

    }

    public void validateSdvStatus(StudySubject studySubject, FormDataBean formDataBean, EventCrf eventCrf) {
        if(StringUtils.isNotBlank(formDataBean.getSdvStatusString())) {
            SdvStatus newSdvStatus = formDataBean.getSdvStatus();
            if(newSdvStatus == null) {
                if(eventCrf.getSdvStatus() != null) {
                    throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_NEVER_VERIFIED_SDV_STATUS_NOT_AVAILABLE);
                }
            } else if((eventCrf.getSdvStatus() == null || !eventCrf.getSdvStatus().equals(newSdvStatus)) && newSdvStatus.equals(SdvStatus.VERIFIED)) {
                if (eventCrf == null || !eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                    throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORM_STATUS_SHOULD_BE_COMPLETE_FOR_SDV_VERIFICATION);
                } else if (eventCrf.isCurrentlyRemoved() || eventCrf.isCurrentlyArchived()) {
                    throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORM_WITH_REMOVED_OR_ARCHIVED_ATTRIBUTE_CANNOT_BE_SDV_VERIFIED);
                }
            }
        }
    }

    public void validateEventDefnCrf(Study tenantStudy, StudyEventDefinition studyEventDefinition, CrfBean crf) {

        // Form Invalid OID

        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(),
                crf.getCrfId(), tenantStudy.getStudyId());
        if (edc == null) {
            edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                    tenantStudy.getStudy().getStudyId());
        }
        if (edc == null || (edc != null && !edc.getStatusId().equals(Status.AVAILABLE.getCode()))) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }

    }

    public void validateForm(FormDataBean formDataBean, Study tenantStudy, StudyEventDefinition studyEventDefinition) {

        if (formDataBean.getItemGroupData() == null)
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORM_DOES_NOT_CONTAIN_ITEMGROUPDATA);
        CrfBean crf = null;
        if (formDataBean.getFormOID() == null) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_MISSING_FORMOID);
        }
        // Form Invalid OID and form not Archived
        crf = crfDao.findByOcOID(formDataBean.getFormOID());
        if (crf == null || (crf != null && !crf.getStatus().equals(Status.AVAILABLE))) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }
        // Form Invalid OID
        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                tenantStudy.getStudy() == null ? tenantStudy.getStudyId() : tenantStudy.getStudy().getStudyId());
        if (edc == null || (edc != null && !edc.getStatusId().equals(Status.AVAILABLE.getCode()))) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_FORMOID_NOT_FOUND);
        }
    }

    public void validateSignatureForStudyEvent(StudyEventDataBean studyEventDataBean, StudyEvent studyEvent, StudySubject studySubject){
        ArrayList<ErrorObj> errors = new ArrayList<>();
        ResourceBundle resword = ResourceBundleProvider.getWordsBundle(Locale.ENGLISH);
        Boolean eventNeedsToBeSigned = null;
        Boolean signedStatus = null;
        try {
             signedStatus = studyEventDataBean.getSigned();
        }catch (OpenClinicaSystemException e){
            errors.add(new ErrorObj(e.getErrorCode(), e.getMessage()));
        }
        if(BooleanUtils.isNotTrue(signedStatus))
            eventNeedsToBeSigned = false;
        else if(BooleanUtils.isTrue(signedStatus))
            eventNeedsToBeSigned = true;
        List<SignatureBean> signatureBeans = studyEventDataBean.getSignatures();
        for (SignatureBean signatureBean: signatureBeans) {
            if (signatureBean.getAttestation() == null)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ATTESTATION_IS_MISSING));
            else if (signatureBean.getAttestation().length() > 1000)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ATTESTATION_TEXT_TOO_LONG));
            else if (!signatureBean.equals(signatureBeans.get(signatureBeans.size()-1)) || !eventNeedsToBeSigned) {
                String attestationMsg = signatureBean.getAttestation().concat(resword.getString(IMPORT_SIGNATURE_POSTFIX_KEYWORD));
                if (!auditLogEventDao.findSignedEventAuditLogByAttestation(attestationMsg).isEmpty())
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ATTESTATION_ALREADY_EXIST_IN_SYSTEM));
            }
        }
        if (eventNeedsToBeSigned && !studyEventService.isEventSignable(studyEvent, studySubject))
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_EVENT_IS_NOT_ELLIGIBLE_TO_BE_SIGNED));

        if(errors.size() > 0){
            throw new OpenClinicaSystemException(FAILED, errors);
        }
    }

    private String validateAndGenerateNewDisplayId(String displayId,boolean parentDn, List errors){
        try {
            if(displayId == null){
                return queryService.generateDisplayId(parentDn);
            }
        }
        catch (OpenClinicaSystemException e){
            errors.add(new ErrorObj(e.getErrorCode(), e.getMessage()));
        }
        return displayId;
    }
    private boolean isChildStatusApplicable(String status) {
        ResolutionStatus currResolutionStatus = ResolutionStatus.getByEnglishDescription(status);
        if(currResolutionStatus != null) {
            if (currResolutionStatus.equals(ResolutionStatus.OPEN) && isQueryNewStatusValid)
                return true;
            if (currResolutionStatus.equals(ResolutionStatus.UPDATED) && isQueryUpdatedStatusValid)
                return true;
            if (currResolutionStatus.equals(ResolutionStatus.CLOSED) && isQueryClosedStatusValid)
                return true;
            if (currResolutionStatus.equals(ResolutionStatus.CLOSED_MODIFIED) && isQueryClosedStatusValid)
                return true;
        }
        return false;
    }

    private boolean checkDiscrepancyNoteTypeValid(String noteType){
        if(noteType != null && QUERY_KEYWORD.equalsIgnoreCase(noteType) || QueryType.ANNOTATION.getName().equalsIgnoreCase(noteType))
            return true;
        return false;
    }

    private boolean checkDiscrepancyNoteStatusValid(String status){
        if(status != null && ResolutionStatus.getByEnglishDescription(status) != null)
            return true;
        return false;
    }

    private boolean isUserExist(String username){
        if(userAccountDao.findByUserName(username)!= null)
            return true;
        return false;
    }
    public void setResolutionStatusForCheckingChildNotesValidity(String resolutionStatus){
        if(resolutionStatus != null) {
            if (resolutionStatus.equalsIgnoreCase(ResolutionStatus.OPEN.getName())) {
                isQueryNewStatusValid = false;
                isQueryUpdatedStatusValid = true;
                isQueryClosedStatusValid = true;
                isQueryClosedModifiedStatusValid = false;
            } else if (resolutionStatus.equalsIgnoreCase(ResolutionStatus.CLOSED.getName())) {
                isQueryNewStatusValid = false;
                isQueryClosedStatusValid = false;
                isQueryUpdatedStatusValid = true;
                isQueryClosedModifiedStatusValid = true;
            } else if (resolutionStatus.equalsIgnoreCase("Closed-Modified")) {
                isQueryNewStatusValid = false;
                isQueryClosedModifiedStatusValid = false;
                isQueryClosedStatusValid = true;
                isQueryUpdatedStatusValid = true;
            } else if (resolutionStatus.equalsIgnoreCase(ResolutionStatus.UPDATED.getName())) {
                isQueryNewStatusValid = false;
                isQueryUpdatedStatusValid = true;
                isQueryClosedStatusValid = true;
                isQueryClosedModifiedStatusValid = false;
            }
        }
    }

    private void validateItemInGroup(Item item, ImportItemGroupDataBean itemGroupDataBean, CrfBean crf) {
        ItemGroup itemGroup = itemGroupDao.findByOcOIDCrfId(itemGroupDataBean.getItemGroupOID(), crf);
        for (ItemGroupMetadata itemGroupMetadata : itemGroup.getItemGroupMetadatas()) {
            if (itemGroupMetadata.getItem().getOcOid().equals(item.getOcOid())) {
                return;
            }
        }
        throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEMGROUP_DOES_NOT_CONTAIN_ITEMDATA);
    }
    private void validateResponseSets(ResponseSet responseSet, String value, ItemCountInForm itemCountInForm) {
        ResponseType responseType = responseSet.getResponseType();
        switch (responseType.getName()) {
            case ("checkbox"):
                validateCheckBoxOrMultiSelect(responseSet, value);
                return;
            case ("multi-select"):
                validateCheckBoxOrMultiSelect(responseSet, value);
                return;
            case ("radio"):
                validateRadioOrSingleSelect(responseSet, value);
                return;
            case ("single-select"):
                validateRadioOrSingleSelect(responseSet, value);
                return;
            case ("text"):
                return;
            case ("textarea"):
                return;
            case ("calculation"):
                return;
            default:
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
        }
    }

    private void validateCheckBoxOrMultiSelect(ResponseSet responseSet, String value) {
        String[] values = value.split(",");

        for (String v : values) {
            if (!responseSet.getOptionsValues().contains(v)) {
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND);
            }
        }
        return;
    }

    private void validateRadioOrSingleSelect(ResponseSet responseSet, String value) {
        if(responseSet.getOptionsText().equals("_") && responseSet.getOptionsValues().equals("_"))
            return;
        if (!responseSet.getOptionsValues().contains(value)) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND);
        }
    }

    private void validateItemDataType(Item item, String value, ItemCountInForm itemCountInForm) {
        ItemDataType itemDataType = item.getItemDataType();
        switch (itemDataType.getCode()) {
            case "BL":
                validateForBoolean(value);
                return;
            case "ST":
                return;
            case "INT":
                validateForInteger(value);
                return;
            case "REAL":
                validateForReal(value);
                return;
            case "DATE":
                validateForDate(value);
                return;
            default:
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
        }
    }

    private void validateForBoolean(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
        }

    }
    private void validateForInteger(String value) {
        try {
            Integer int1 = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
        }
    }

    private void validateForReal(String value) {
        if(NumberUtils.isParsable(value))
            return;
        throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH);
    }

    private void validateForDate(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(value, formatter);
        } catch (Exception pe) {
            pe.getStackTrace();
            throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_INVALID_DATE_FORMAT);
        }
    }
}

package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import core.org.akaza.openclinica.core.form.xform.QueryType;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.controller.dto.DataImportReport;
import org.akaza.openclinica.controller.helper.table.ItemCountInForm;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
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

    public static final String FAILED = "Failed";
    public static final String NO_CHANGE_IN_QUERIES="No change in queries";
    public static final String QUERY_KEYWORD = "Query";
    private boolean isQueryNewStatusValid;
    private boolean isQueryUpdatedStatusValid;
    private boolean isQueryClosedStatusValid;
    private boolean isQueryClosedModifiedStatusValid;

    public void validateQuery(DiscrepancyNoteBean discrepancyNoteBean){
        ArrayList<ErrorObj> errors = new ArrayList<>();
        isQueryNewStatusValid = true;
        isQueryUpdatedStatusValid = false;
        isQueryClosedStatusValid = false;
        isQueryClosedModifiedStatusValid = false;

        boolean newQueriesStarted = false;
        boolean isResolutionTypeAnnotation = false;
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
        if(!checkDiscrepancyNoteTypeValid(discrepancyNoteBean.getNoteType()))
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_TYPE_NOT_VALID));
        isResolutionTypeAnnotation = QueryType.ANNOTATION.getName().equalsIgnoreCase(discrepancyNoteBean.getNoteType());
        if(isResolutionTypeAnnotation && discrepancyNoteBean.getChildNotes().size() > 1)
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ANNOTATION_IMPORT_CANNOT_HAVE_MANY_CHILD_NOTES));
        DiscrepancyNote parentDN =null;
        if(discrepancyNoteBean.getDisplayId() != null) {
            parentDN = discrepancyNoteDao.findByDisplayId(discrepancyNoteBean.getDisplayId());
            if (parentDN != null) {
                if(parentDN.getParentDiscrepancyNote() != null)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_PARENT_DISPLAYID_IS_ALREADY_USED_AS_CHILD_DISPLAYID));
                setResolutionStatusForCheckingChildNotesValidity(parentDN.getResolutionStatus().getName());
            }
            if(discrepancyNoteBean.getDisplayId().length() > 32)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_ID_TOO_LONG));
        }

        for(ChildNoteBean childNoteBean : discrepancyNoteBean.getChildNotes()){
            if(!isResolutionTypeAnnotation && !checkDiscrepancyNoteStatusValid(childNoteBean.getStatus()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_STATUS_NOT_VALID));
            if(!isUserExist(childNoteBean.getOwnerUserName()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_USER_NOT_VALID));
            if(!isResolutionTypeAnnotation && childNoteBean.getUserRef() != null && !isUserExist(childNoteBean.getUserRef().getUserName()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ASSIGNED_USER_NOT_VALID));
            if(StringUtils.isBlank(childNoteBean.getDetailedNote()))
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DETAILED_NOTE_NOT_AVAILABLE));
            if(childNoteBean.getDetailedNote().length() > 1000)
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DETAILED_NOTE_TOO_LONG));
            DiscrepancyNote childDN = null;
            if(childNoteBean.getDisplayId() != null) {
                if(childNoteBean.getDisplayId().length() > 32)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_DISCREPANCY_NOTE_ID_TOO_LONG));
                childDN = discrepancyNoteDao.findByDisplayId(childNoteBean.getDisplayId());
            }
            if (childDN == null) {
                    if (!isResolutionTypeAnnotation && !isChildStatusApplicable(childNoteBean.getStatus()))
                        errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_STATUS_NOT_APPLICABLE));
                    setResolutionStatusForCheckingChildNotesValidity(childNoteBean.getStatus());
                    newQueriesStarted = true;
            }else{
                if(childDN.getParentDiscrepancyNote() == null)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_CHILD_DISPLAYID_IS_ALREADY_USED_AS_PARENT_DISPLAYID));
                if(parentDN == null)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_CHILD_DISPLAYID_IS_ALREADY_USED));
                else if(!childDN.getParentDiscrepancyNote().getDisplayId().equalsIgnoreCase(parentDN.getDisplayId()))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_CHILD_DISPLAYID_IS_NOT_CORRESPONDING_TO_PARENT_DISPLAYID));
                if(newQueriesStarted)
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_NEW_QUERIES_IN_BETWEEN_OLD_QUERIES));
            }

            if(parentDN != null && isResolutionTypeAnnotation
                    && (childNoteBean.getDisplayId() == null || childDN == null)){
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ANNOTATION_IMPORT_CANNOT_HAVE_MANY_CHILD_NOTES));
            }
        }
        if(errors.size() ==0 && !newQueriesStarted){
            errors.add(new ErrorObj( NO_CHANGE_IN_QUERIES, ""));
        }
        if(errors.size() > 0){
            throw new OpenClinicaSystemException(FAILED, errors);
        }
    }

    public void validateItem(ImportItemDataBean itemDataBean, CrfBean crf, ImportItemGroupDataBean itemGroupDataBean, ItemCountInForm itemCountInForm) {
        ErrorObj errorObj = null;
        List<ErrorObj> errors = new ArrayList<>();
        if (itemDataBean.getItemOID() == null) {
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_NOT_FOUND));
            throw new OpenClinicaSystemException(FAILED, errors);
        }

        Item item = itemDao.findByOcOID(itemDataBean.getItemOID());

        // ItemOID is not valid
        if (item == null || (item != null && !item.getStatus().equals(Status.AVAILABLE))) {
            errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ITEM_NOT_FOUND));
        }else {

            if (!validateItemInGroup(item, itemGroupDataBean, crf)) {
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_ITEMGROUP_DOES_NOT_CONTAIN_ITEMDATA));
            }

            if (itemDataBean.getValue() == null) {
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_MISSING_VALUE));
            } else if (itemDataBean.getValue().length() > 3999) {
                errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TOO_LONG));
            } else if (StringUtils.isNotEmpty(itemDataBean.getValue())) {
                if (!validateItemDataType(item, itemDataBean.getValue(), itemCountInForm))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_TYPE_MISMATCH));


                Set<ItemFormMetadata> ifms = item.getItemFormMetadatas();
                ResponseSet responseSet = ifms.iterator().next().getResponseSet();
                if (!validateResponseSets(responseSet, itemDataBean.getValue(), itemCountInForm))
                    errors.add(new ErrorObj(FAILED, ErrorConstants.ERR_VALUE_CHOICE_NOT_FOUND));

            }
        }
        if(errors.size() > 0){
            throw new OpenClinicaSystemException(FAILED, errors);
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

    private boolean validateItemInGroup(Item item, ImportItemGroupDataBean itemGroupDataBean, CrfBean crf) {
        ItemGroup itemGroup = itemGroupDao.findByOcOIDCrfId(itemGroupDataBean.getItemGroupOID(), crf);
        for (ItemGroupMetadata itemGroupMetadata : itemGroup.getItemGroupMetadatas()) {
            if (itemGroupMetadata.getItem().getOcOid().equals(item.getOcOid())) {
                return true;
            }
        }
        return false;
    }
    private Boolean validateResponseSets(ResponseSet responseSet, String value, ItemCountInForm itemCountInForm) {
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
                return true;
            case ("textarea"):
                return true;
            case ("calculation"):
                return true;
            default:
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
        }
    }

    private Boolean validateCheckBoxOrMultiSelect(ResponseSet responseSet, String value) {
        String[] values = value.split(",");

        for (String v : values) {
            if (!responseSet.getOptionsValues().contains(v)) {
                return false;
            }
        }
        return true;
    }

    private Boolean validateRadioOrSingleSelect(ResponseSet responseSet, String value) {
        if(responseSet.getOptionsText().equals("_") && responseSet.getOptionsValues().equals("_"))
            return true;
        if (!responseSet.getOptionsValues().contains(value)) {
            return false;
        }
        return true;
    }

    private Boolean validateItemDataType(Item item, String value, ItemCountInForm itemCountInForm) {
        ItemDataType itemDataType = item.getItemDataType();
        switch (itemDataType.getCode()) {
            case "BL":
                return validateForBoolean(value);
            case "ST":
                return true;
            case "INT":
                return validateForInteger(value);
            case "REAL":
                return validateForReal(value);
            case "DATE":
                return validateForDate(value);
            default:
                itemCountInForm.setInsertedUpdatedSkippedItemCountInForm(itemCountInForm.getInsertedUpdatedSkippedItemCountInForm() + 1);
                throw new OpenClinicaSystemException(FAILED, ErrorConstants.ERR_ITEM_TYPE_NOT_SUPPORTED);
        }
    }

    private Boolean validateForBoolean(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            return false;
        }
        return true;

    }
    private Boolean validateForInteger(String value) {
        try {
            Integer int1 = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            return false;
        }
        return true;
    }

    private Boolean validateForReal(String value) {
        if(NumberUtils.isParsable(value))
            return true;
        return false;
    }

    private Boolean validateForDate(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(value, formatter);
        } catch (Exception pe) {
            pe.getStackTrace();
            return false;
        }
        return true;
    }
}

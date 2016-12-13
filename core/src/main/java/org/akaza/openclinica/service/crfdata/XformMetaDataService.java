package org.akaza.openclinica.service.crfdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionMediaDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataTypeDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemReferenceTypeDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.SectionDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.hibernate.VersioningMapDao;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.CrfVersionMedia;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemDataType;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ResponseSet;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.datamap.Section;
import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.akaza.openclinica.domain.datamap.VersioningMapId;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.domain.xform.XformGroup;
import org.akaza.openclinica.domain.xform.XformItem;
import org.akaza.openclinica.domain.xform.XformUtils;
import org.akaza.openclinica.domain.xform.dto.Bind;
import org.akaza.openclinica.domain.xform.dto.Group;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.domain.xform.dto.UserControl;
import org.akaza.openclinica.validator.xform.ItemValidator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

@Service
public class XformMetaDataService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private CrfDao crfDao;

    @Autowired
    private SectionDao sectionDao;

    @Autowired
    private UserAccountDao userDao;

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Autowired
    private CrfVersionMediaDao crfVersionMediaDao;

    @Autowired
    private ItemGroupDao itemGroupDao;

    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;

    @Autowired
    private VersioningMapDao versioningMapDao;

    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemDataTypeDao itemDataTypeDao;

    @Autowired
    private ItemReferenceTypeDao itemRefTypeDao;

    @Autowired
    private ResponseTypeDao responseTypeDao;

    @Autowired
    private ResponseSetService responseSetService;

    public Errors runService(CRFVersionBean version, XformContainer container, StudyBean currentStudy, UserAccountBean ub, Html html, String submittedCrfName,
            String submittedCrfVersionName, String submittedCrfVersionDescription, String submittedRevisionNotes, String submittedXformText,
            List<FileItem> formItems) {

        // Create container for holding validation errors
        DataBinder dataBinder = new DataBinder(new CrfVersion());
        Errors errors = dataBinder.getBindingResult();

        try {
            createCRFMetaData(version, container, currentStudy, ub, html, submittedCrfName, submittedCrfVersionName, submittedCrfVersionDescription,
                    submittedRevisionNotes, submittedXformText, formItems, errors);
        } catch (Exception e) {
            // Transaction has been rolled back due to an exception.
            // TODO: Should we add an error message here?
            logger.error("Error encountered while saving CRF: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return errors;
    }

    @Transactional
    public CrfVersion createCRFMetaData(CRFVersionBean version, XformContainer container, StudyBean currentStudy, UserAccountBean ub, Html html,
            String submittedCrfName, String submittedCrfVersionName, String submittedCrfVersionDescription, String submittedRevisionNotes,
            String submittedXformText, List<FileItem> formItems, Errors errors) throws Exception {

        CrfVersion crfVersion;

        // Retrieve CrfBean. Create one if it doesn't exist yet.
        CrfBean crf = null;
        if (version.getCrfId() > 0) {
            crf = (CrfBean) crfDao.findById(version.getCrfId());
            crf.setUpdateId(ub.getId());
            crf.setDateUpdated(new Date());
            crfDao.saveOrUpdate(crf);

            crfVersion = crfVersionDao.findByOcOID(version.getOid());
            // crfVersion.setXform(submittedXformText);
            crfVersion.setXformName(container.getInstanceName());
            crfVersion = crfVersionDao.saveOrUpdate(crfVersion);

        } else {
            crf = new CrfBean();
            crf.setName(submittedCrfName);
            crf.setDescription(submittedCrfVersionDescription);
            crf.setUserAccount(userDao.findById(ub.getId()));
            crf.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            crf.setStudy(studyDao.findById(currentStudy.getId()));
            crf.setOcOid(crfDao.getValidOid(new CrfBean(), submittedCrfName));
            crf.setUpdateId(ub.getId());
            crf.setDateUpdated(new Date());
            Integer crfId = (Integer) crfDao.save(crf);
            crf.setCrfId(crfId);

            // Create new CRF Version
            crfVersion = new CrfVersion();
            crfVersion.setName(submittedCrfVersionName);
            crfVersion.setDescription(submittedCrfVersionDescription);
            crfVersion.setCrf(crf);
            crfVersion.setUserAccount(userDao.findById(ub.getId()));
            crfVersion.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            crfVersion.setRevisionNotes(submittedRevisionNotes);
            crfVersion.setOcOid(crfVersionDao.getValidOid(new CrfVersion(), crf.getOcOid(), crfVersion.getName()));
            crfVersion.setXform(submittedXformText);
            crfVersion.setXformName(container.getInstanceName());
            Integer crfVersionId = (Integer) crfVersionDao.save(crfVersion);
            crfVersion.setCrfVersionId(crfVersionId);
        }
        // Create Section
        Section section = sectionDao.findByCrfVersionOrdinal(crfVersion.getCrfVersionId(), 1);
        if (section == null) {
            section = new Section();
            section.setCrfVersion(crfVersion);
            section.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            section.setLabel("");
            section.setTitle("");
            section.setSubtitle("");
            section.setPageNumberLabel("");
            section.setOrdinal(1);
            section.setUserAccount(userDao.findById(ub.getId())); // not null
            section.setBorders(0);
            sectionDao.saveOrUpdate(section);
            section = sectionDao.findByCrfVersionOrdinal(crfVersion.getCrfVersionId(), 1);
        }
        createGroups(container, html, submittedXformText, crf, crfVersion, section, ub, errors);

        saveMedia(formItems, crf, crfVersion);

        if (errors.hasErrors()) {
            logger.error("Encounter validation errors while saving CRF.  Rolling back transaction.");
            throw new RuntimeException("Encountered validation errors while saving CRF.");
        }
        return crfVersion;
    }

    private void saveMedia(List<FileItem> items, CrfBean crf, CrfVersion version) {
        boolean hasFiles = false;
        for (FileItem item : items) {
            if (!item.isFormField() && item.getName() != null && !item.getName().isEmpty())
                hasFiles = true;
        }

        if (hasFiles) {
            String dir = Utils.getCrfMediaFilePath(crf, version);
            // Save any media files
            for (FileItem item : items) {
                if (!item.isFormField()) {

                    String fileName = item.getName();
                    // Some browsers IE 6,7 getName returns the whole path
                    int startIndex = fileName.lastIndexOf('\\');
                    if (startIndex != -1) {
                        fileName = fileName.substring(startIndex + 1, fileName.length());
                    }

                    CrfVersionMedia media = crfVersionMediaDao.findByCrfVersionIdAndFileName(version.getCrfVersionId(), fileName);
                    if (media == null) {
                        media = new CrfVersionMedia();
                        media.setCrfVersion(version);
                        media.setName(fileName);
                        media.setPath(dir);
                        crfVersionMediaDao.saveOrUpdate(media);
                    }

                }
            }
        }
    }

    private void createGroups(XformContainer container, Html html, String submittedXformText, CrfBean crf, CrfVersion version, Section section,
            UserAccountBean ub, Errors errors) throws Exception {
        Integer itemOrdinal = 1;
        ArrayList<String> usedGroupOids = new ArrayList<String>();
        ArrayList<String> usedItemOids = new ArrayList<String>();
        List<Group> htmlGroups = html.getBody().getGroup();

        // for (Group htmlGroup : htmlGroups) {
        for (XformGroup xformGroup : container.getGroups()) {

            // XformGroup xformGroup = container.findGroupByRef(htmlGroup.getRef());
            ItemGroup itemGroup = itemGroupDao.findByNameCrfId(xformGroup.getGroupName(), crf);

            if (itemGroup == null) {
                itemGroup = new ItemGroup();
                itemGroup.setName(xformGroup.getGroupName());
                itemGroup.setCrf(crf);
                itemGroup.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
                itemGroup.setUserAccount(userDao.findById(ub.getId()));
                itemGroup.setOcOid(itemGroupDao.getValidOid(new ItemGroup(), crf.getName(), xformGroup.getGroupName(), usedGroupOids));
                usedGroupOids.add(itemGroup.getOcOid());
                Integer itemGroupId = (Integer) itemGroupDao.save(itemGroup);
                itemGroup.setItemGroupId(itemGroupId);
            }
            List<UserControl> widgets = null;
            boolean isRepeating = xformGroup.isRepeating();
            // Create Item specific DB entries: item,
            // response_set,item_form_metadata,versioning_map,item_group_metadata
            // for (UserControl widget : widgets) {

            for (XformItem xformItem : xformGroup.getItems()) {
                // Skip reserved name and read-only items here
                // XformItem xformItem = container.findItemByGroupAndRef(xformGroup, widget.getRef());
                String readonly = xformItem.getReadonly();

                if (!xformItem.getItemName().equals("OC.STUDY_SUBJECT_ID") && !xformItem.getItemName().equals("OC.STUDY_SUBJECT_ID_CONFIRM")
                        && (readonly == null || !readonly.trim().equals("true()"))) {
                    Item item = createItem(html, xformGroup, xformItem, crf, ub, usedItemOids, errors);
                    if (item != null) {
                        ResponseType responseType = getResponseType(html, xformItem);
                        ResponseSet responseSet = responseSetService.getResponseSet(html, submittedXformText, xformItem, version, responseType, item, errors);
                        // add if statement
                        ItemFormMetadata ifmd = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), version.getCrfVersionId());
                        if (ifmd == null) {
                            ifmd = createItemFormMetadata(html, xformItem, item, responseSet, section, version, itemOrdinal);
                        }
                        createVersioningMap(version, item);
                        //
                        ItemGroupMetadata igmd = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), version.getCrfVersionId());
                        if (igmd == null) {
                            igmd = createItemGroupMetadata(html, item, version, itemGroup, isRepeating, itemOrdinal);
                        }
                        itemOrdinal++;
                    }
                }
            }
        }

    }

    private ItemGroupMetadata createItemGroupMetadata(Html html, Item item, CrfVersion version, ItemGroup itemGroup, boolean isRepeating, Integer itemOrdinal) {
        ItemGroupMetadata itemGroupMetadata = new ItemGroupMetadata();
        itemGroupMetadata.setItemGroup(itemGroup);
        itemGroupMetadata.setHeader("");
        itemGroupMetadata.setSubheader("");
        itemGroupMetadata.setLayout("");
        if (isRepeating) {
            itemGroupMetadata.setRepeatingGroup(true);
            itemGroupMetadata.setRepeatNumber(1);
            itemGroupMetadata.setRepeatMax(40);
        } else {
            itemGroupMetadata.setRepeatingGroup(false);
            itemGroupMetadata.setRepeatNumber(1);
            itemGroupMetadata.setRepeatMax(1);
        }
        itemGroupMetadata.setRepeatArray("");
        itemGroupMetadata.setRowStartNumber(0);
        itemGroupMetadata.setCrfVersion(version);
        itemGroupMetadata.setItem(item);
        itemGroupMetadata.setOrdinal(itemOrdinal);
        itemGroupMetadata.setShowGroup(true);
        itemGroupMetadata = itemGroupMetadataDao.saveOrUpdate(itemGroupMetadata);
        return itemGroupMetadata;
    }

    private void createVersioningMap(CrfVersion version, Item item) {

        VersioningMapId versioningMapId = new VersioningMapId();
        versioningMapId.setCrfVersionId(version.getCrfVersionId());
        versioningMapId.setItemId(item.getItemId());

        VersioningMap versioningMap = new VersioningMap();
        versioningMap.setCrfVersion(version);
        versioningMap.setItem(item);
        versioningMap.setVersionMapId(versioningMapId);
        versioningMapDao.saveOrUpdate(versioningMap);
    }

    private ItemFormMetadata createItemFormMetadata(Html html, XformItem xformItem, Item item, ResponseSet responseSet, Section section, CrfVersion version,
            Integer itemOrdinal) {
        ItemFormMetadata itemFormMetadata = new ItemFormMetadata();
        itemFormMetadata.setCrfVersionId(version.getCrfVersionId());
        itemFormMetadata.setResponseSet(responseSet);
        itemFormMetadata.setItem(item);
        itemFormMetadata.setSubheader("");
        itemFormMetadata.setHeader("");
        itemFormMetadata.setLeftItemText(getLeftItemText(html, xformItem));
        itemFormMetadata.setRightItemText("");
        itemFormMetadata.setParentId(0);
        itemFormMetadata.setSection(section);
        itemFormMetadata.setOrdinal(itemOrdinal);
        itemFormMetadata.setParentLabel("");
        itemFormMetadata.setColumnNumber(0);
        itemFormMetadata.setPageNumberLabel("");
        itemFormMetadata.setQuestionNumberLabel("");
        itemFormMetadata.setRegexp("");
        itemFormMetadata.setRegexpErrorMsg("");
        if (getItemFormMetadataRequired(html, xformItem))
            itemFormMetadata.setRequired(true);
        else
            itemFormMetadata.setRequired(false);
        itemFormMetadata.setDefaultValue("");
        itemFormMetadata.setResponseLayout("Vertical");
        itemFormMetadata.setWidthDecimal("");
        itemFormMetadata.setShowItem(true);
        itemFormMetadata = itemFormMetadataDao.saveOrUpdate(itemFormMetadata);
        return itemFormMetadata;
    }

    private Item createItem(Html html, XformGroup xformGroup, XformItem xformItem, CrfBean crf, UserAccountBean ub, ArrayList<String> usedItemOids,
            Errors errors) throws Exception {
        ItemDataType newDataType = getItemDataType(html, xformItem);

        if (newDataType == null) {
            logger.error("Found unsupported item type for item: " + xformItem.getItemName());
            return null;
        }

        Item item = itemDao.findByNameCrfId(xformItem.getItemName(), crf.getCrfId());
        ItemDataType oldDataType = null;
        if (item != null) {
            oldDataType = itemDataTypeDao.findByItemDataTypeId(itemDao.getItemDataTypeId(item));
        } else {
            item = new Item();
            item.setName(xformItem.getItemName());
            item.setDescription("");
            item.setUnits("");
            item.setPhiStatus(false);
            item.setItemDataType(newDataType);
            item.setItemReferenceType(itemRefTypeDao.findByItemReferenceTypeId(1));
            item.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            item.setUserAccount(userDao.findById(ub.getId()));
            item.setOcOid(itemDao.getValidOid(new Item(), crf.getName(), xformItem.getItemName(), usedItemOids));
            usedItemOids.add(item.getOcOid());
            Integer itemId = (Integer) itemDao.save(item);
            item.setItemId(itemId);
        }
        ItemValidator validator = new ItemValidator(itemDao, oldDataType, newDataType);
        DataBinder dataBinder = new DataBinder(item);
        Errors itemErrors = dataBinder.getBindingResult();
        validator.validate(item, itemErrors);
        errors.addAllErrors(itemErrors);

        return itemDao.findByOcOID(item.getOcOid());
    }

    private String getLeftItemText(Html html, XformItem xformItem) {
        List<UserControl> controls = responseSetService.getUserControl(html);

        for (UserControl control : controls) {
            if (control.getRef().equals(xformItem.getItemPath())) {
                if (control.getLabel() != null && control.getLabel().getLabel() != null)
                    return control.getLabel().getLabel();
                else if (control.getLabel() != null && control.getLabel().getRef() != null && !control.getLabel().getRef().equals("")) {
                    String ref = control.getLabel().getRef();
                    String itextKey = ref.substring(ref.indexOf("'") + 1, ref.lastIndexOf("'"));
                    return XformUtils.getDefaultTranslation(html, itextKey);
                } else
                    return "";
            }
        }
        return "";
    }

    private ItemDataType getItemDataType(Html html, XformItem xformItem) {
        String dataType = "";

        for (Bind bind : html.getHead().getModel().getBind()) {
            if (bind.getNodeSet().equals(xformItem.getItemPath()) && bind.getType() != null && !bind.getType().equals("")) {
                dataType = bind.getType();

                if (dataType.equals("string"))
                    return itemDataTypeDao.findByItemDataTypeCode("ST");
                else if (dataType.equals("int"))
                    return itemDataTypeDao.findByItemDataTypeCode("INT");
                else if (dataType.equals("decimal"))
                    return itemDataTypeDao.findByItemDataTypeCode("REAL");
                else if (dataType.equals("select") || dataType.equals("select1"))
                    return itemDataTypeDao.findByItemDataTypeCode("ST");
                else if (dataType.equals("binary"))
                    return itemDataTypeDao.findByItemDataTypeCode("FILE");
                else if (dataType.equals("date"))
                    return itemDataTypeDao.findByItemDataTypeCode("DATE");
            }
        }
        return null;
    }

    private boolean getItemFormMetadataRequired(Html html, XformItem xformItem) {
        boolean required = false;

        for (Bind bind : html.getHead().getModel().getBind()) {
            if (bind.getNodeSet().equals(xformItem.getItemPath()) && bind.getRequired() != null && !bind.getRequired().equals("")) {
                if (bind.getRequired().equals("true()"))
                    required = true;
                else if (bind.getRequired().equals("false()"))
                    required = false;
            }
        }
        return required;
    }

    private ResponseType getResponseType(Html html, XformItem xformItem) {
        String responseType = "";

        for (Bind bind : html.getHead().getModel().getBind()) {
            if (bind.getNodeSet().equals(xformItem.getItemPath()) && bind.getType() != null && !bind.getType().equals("")) {
                responseType = bind.getType();

                if (responseType.equals("string"))
                    return responseTypeDao.findByResponseTypeName("text");
                else if (responseType.equals("int"))
                    return responseTypeDao.findByResponseTypeName("text");
                else if (responseType.equals("decimal"))
                    return responseTypeDao.findByResponseTypeName("text");
                else if (responseType.equals("date"))
                    return responseTypeDao.findByResponseTypeName("text");
                else if (responseType.equals("select"))
                    return responseTypeDao.findByResponseTypeName("checkbox");
                else if (responseType.equals("select1"))
                    return responseTypeDao.findByResponseTypeName("radio");
                else if (responseType.equals("binary"))
                    return responseTypeDao.findByResponseTypeName("file");
                else
                    return null;
            }
        }
        return null;
    }

}

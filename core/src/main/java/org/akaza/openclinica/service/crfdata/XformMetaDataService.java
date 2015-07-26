package org.akaza.openclinica.service.crfdata;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataTypeDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemReferenceTypeDao;
import org.akaza.openclinica.dao.hibernate.ResponseSetDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.SectionDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.hibernate.VersioningMapDao;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
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
import org.akaza.openclinica.domain.xform.dto.Bind;
import org.akaza.openclinica.domain.xform.dto.Group;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.domain.xform.dto.UserControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ItemGroupDao itemGroupDao;

    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;

    @Autowired
    private VersioningMapDao versioningMapDao;

    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;

    @Autowired
    private ResponseSetDao responseSetDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemDataTypeDao itemDataTypeDao;

    @Autowired
    private ItemReferenceTypeDao itemRefTypeDao;

    @Autowired
    private ResponseTypeDao responseTypeDao;

    @Autowired
    private DataSource datasource;

    @Transactional
    public CrfVersion createCRFMetaData(CRFVersionBean version, XformContainer container, StudyBean currentStudy, UserAccountBean ub, Html html,
            String submittedCrfName, String submittedCrfVersionName, String submittedCrfVersionDescription, String submittedRevisionNotes,
            String submittedXformText) {

        // Retrieve CrfBean. Create one if it doesn't exist yet.
        CrfBean crf = null;
        if (version.getCrfId() > 0) {
            crf = (CrfBean) crfDao.findById(version.getCrfId());
        } else {
            CRFDAO oldCRFDAO = new CRFDAO(datasource);
            crf = new CrfBean();
            crf.setName(submittedCrfName);
            crf.setDescription(submittedCrfVersionDescription);
            crf.setUserAccount(userDao.findById(ub.getId()));
            crf.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            crf.setStudy(studyDao.findById(currentStudy.getId()));
            crf.setOcOid(oldCRFDAO.getValidOid(new CRFBean(), submittedCrfName));
            crfDao.saveOrUpdate(crf);
            crf = (CrfBean) crfDao.findByName(crf.getName());
        }

        // Create new CRF Version
        CRFVersionDAO oldCRFVersionDAO = new CRFVersionDAO(datasource);
        CrfVersion crfVersion = new CrfVersion();
        crfVersion.setName(submittedCrfVersionName);
        crfVersion.setDescription(submittedCrfVersionDescription);
        crfVersion.setCrf(crf);
        crfVersion.setUserAccount(userDao.findById(ub.getId()));
        crfVersion.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        crfVersion.setRevisionNotes(submittedRevisionNotes);
        crfVersion.setOcOid(oldCRFVersionDAO.getValidOid(new CRFVersionBean(), crf.getOcOid(), crfVersion.getName()));
        crfVersion.setXform(submittedXformText);
        crfVersion.setXformName(container.getInstanceName());
        crfVersionDao.saveOrUpdate(crfVersion);
        crfVersion = crfVersionDao.findByOcOID(crfVersion.getOcOid());

        // Create Section
        Section section = new Section();
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

        createGroups(container, html, crf, crfVersion, section, ub);

        return crfVersion;
    }

    private void createGroups(XformContainer container, Html html, CrfBean crf, CrfVersion version, Section section, UserAccountBean ub) {
        ItemGroupDAO itemGroupDAO = new ItemGroupDAO(datasource);

        List<Group> htmlGroups = html.getBody().getGroup();

        for (Group htmlGroup : htmlGroups) {
            XformGroup xformGroup = container.findGroupByRef(htmlGroup.getRef());
            ItemGroup itemGroup = new ItemGroup();
            itemGroup.setName(xformGroup.getGroupName());
            itemGroup.setCrf(crf);
            itemGroup.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            itemGroup.setUserAccount(userDao.findById(ub.getId()));
            itemGroup.setOcOid(itemGroupDAO.getValidOid(new ItemGroupBean(), crf.getName(), xformGroup.getGroupName(), new ArrayList()));
            // dbgroup.setDateCreated(dateCreated)
            itemGroupDao.saveOrUpdate(itemGroup);
            itemGroup = itemGroupDao.findByOcOID(itemGroup.getOcOid());

            // Create Item specific DB entries: item, response_set,item_form_metadata,versioning_map,item_group_metadata
            for (UserControl widget : htmlGroup.getUsercontrol()) {
                XformItem xformItem = container.findItemByGroupAndRef(xformGroup, widget.getRef());
                Item item = createItem(html, widget, xformGroup, xformItem, crf, ub);
                if (item != null) {
                    ResponseSet responseSet = createResponseSet(html, xformItem, widget, version);
                    createItemFormMetadata(html, xformItem, item, responseSet, section, version);
                    createVersioningMap(version, item);
                    createItemGroupMetadata(html, item, version, itemGroup);
                }
            }
        }

    }

    private void createItemGroupMetadata(Html html, Item item, CrfVersion version, ItemGroup itemGroup) {
        ItemGroupMetadata itemGroupMetadata = new ItemGroupMetadata();
        itemGroupMetadata.setItemGroup(itemGroup);// item_group_id,
        itemGroupMetadata.setHeader("");// header,
        itemGroupMetadata.setSubheader("");// subheader,
        itemGroupMetadata.setLayout("");// layout,
        // TODO: Add repeating group info here.
        itemGroupMetadata.setRepeatNumber(1);// repeat_number,
        itemGroupMetadata.setRepeatMax(1);// repeat_max,
        itemGroupMetadata.setRepeatArray("");// repeat_array,
        itemGroupMetadata.setRowStartNumber(0);// row_start_number,
        itemGroupMetadata.setCrfVersion(version);// crf_version_id,
        itemGroupMetadata.setItem(item);// item_id ,
        // TODO: Figure out ordinals here.
        itemGroupMetadata.setOrdinal(1);// ordinal,
        itemGroupMetadata.setShowGroup(true);// show_group,
        // TODO: More repeating group info here.
        itemGroupMetadata.setRepeatingGroup(false);// repeating_group
        itemGroupMetadataDao.saveOrUpdate(itemGroupMetadata);
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

    private void createItemFormMetadata(Html html, XformItem xformItem, Item item, ResponseSet responseSet, Section section, CrfVersion version) {
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
        // TODO: Will need to pull the ordinal from the XML.
        itemFormMetadata.setOrdinal(1);
        itemFormMetadata.setParentLabel("");
        itemFormMetadata.setColumnNumber(0);
        itemFormMetadata.setPageNumberLabel("");
        itemFormMetadata.setQuestionNumberLabel("");
        itemFormMetadata.setRegexp("");
        itemFormMetadata.setRegexpErrorMsg("");
        // TODO: Will need to pull required info from bindings
        itemFormMetadata.setRequired(false);
        itemFormMetadata.setDefaultValue("");
        itemFormMetadata.setResponseLayout("Horizontal");
        itemFormMetadata.setWidthDecimal("");
        itemFormMetadata.setShowItem(true);
        itemFormMetadataDao.saveOrUpdate(itemFormMetadata);
    }

    private ResponseSet createResponseSet(Html html, XformItem xformItem, UserControl widget, CrfVersion version) {
        ResponseType responseType = getResponseType(html, xformItem);

        // TODO: Eventually will need to build support for ItemSets defined in XML.
        // TODO: And for non text types
        ResponseSet existingSet = responseSetDao.findByLabelVersion(responseType.getName(), version.getCrfVersionId());
        if (existingSet == null) {
            ResponseSet responseSet = new ResponseSet();
            responseSet.setLabel(responseType.getName());
            responseSet.setOptionsText(responseType.getName());
            responseSet.setOptionsValues(responseType.getName());
            responseSet.setResponseType(responseType);
            responseSet.setVersionId(version.getCrfVersionId());
            return responseSetDao.saveOrUpdate(responseSet);
        } else
            return existingSet;
    }

    private Item createItem(Html html, UserControl widget, XformGroup xformGroup, XformItem xformItem, CrfBean crf, UserAccountBean ub) {
        ItemDAO itemDAO = new ItemDAO(datasource);

        ItemDataType dataType = getItemDataType(html, xformItem);
        if (dataType != null) {
            Item item = new Item();
            item.setName(xformItem.getItemName());
            item.setDescription("");
            item.setUnits("");
            item.setPhiStatus(false);
            item.setItemDataType(dataType);
            item.setItemReferenceType(itemRefTypeDao.findByItemReferenceTypeId(1));
            item.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            item.setUserAccount(userDao.findById(ub.getId()));
            // TODO: DATE_CREATED,
            item.setOcOid(itemDAO.getValidOid(new ItemBean(), crf.getName(), xformItem.getItemName(), new ArrayList()));// OC_OID
            itemDao.saveOrUpdate(item);
            return itemDao.findByOcOID(item.getOcOid());
        } else {
            System.out.println("Found unsupported item type for item: " + xformItem.getItemName());
            return null;
        }
    }

    private String getLeftItemText(Html html, XformItem xformItem) {
        // TODO: Need to handle repeating groups here.
        for (Group group : html.getBody().getGroup()) {
            for (UserControl control : group.getUsercontrol()) {
                if (control.getRef().equals(xformItem.getItemPath())) {
                    if (control.getLabel() != null && control.getLabel().getLabel() != null)
                        return control.getLabel().getLabel();
                    else
                        return "";
                }
            }
        }
        return "";
    }

    private ItemDataType getItemDataType(Html html, XformItem xformItem) {
        String dataType = "";

        for (Bind bind : html.getHead().getModel().getBind()) {
            if (bind.getNodeSet().equals(xformItem.getItemPath()) && bind.getType() != null && !bind.getType().equals("")) {
                dataType = bind.getType();
                // TODO: Only String data type supported for this story.
                // TODO: Will add support for other types in future stories.

                if (dataType.equals("string"))
                    return itemDataTypeDao.findByItemDataTypeCode("ST");
            }
        }
        return null;
    }

    private ResponseType getResponseType(Html html, XformItem xformItem) {
        String responseType = "";

        for (Bind bind : html.getHead().getModel().getBind()) {
            if (bind.getNodeSet().equals(xformItem.getItemPath()) && bind.getType() != null && !bind.getType().equals("")) {
                responseType = bind.getType();
                // TODO: Only Text response type supported for this story.
                // TODO: Will add support for other types in future stories.

                if (responseType.equals("string"))
                    return responseTypeDao.findByResponseTypeName("text");
            }
        }
        return null;
    }

}

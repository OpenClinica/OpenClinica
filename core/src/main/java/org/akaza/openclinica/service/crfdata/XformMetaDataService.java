package org.akaza.openclinica.service.crfdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutMediaDao;
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
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
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
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.domain.xform.XformUtils;
import org.akaza.openclinica.domain.xform.dto.Bind;
import org.akaza.openclinica.domain.xform.dto.Group;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.domain.xform.dto.UserControl;
import org.akaza.openclinica.service.dto.Version;
import org.akaza.openclinica.validator.xform.ItemValidator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class XformMetaDataService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String FORM_SUFFIX = "form.xml";
    public static final String INSTANCE_SUFFIX = "instance.tpl";
    public static final String INSTANCEQUERIES_SUFFIX = "instance-queries.tpl";
    public static final String FORMQUERIES_SUFFIX = "form-queries.xml";
    public static final String XLS_SUFFIX = ".xls";

    public static final String GEOPOINT_DATATYPE = "geopoint";
    public static final String GEOTRACE_DATATYPE = "geotrace";
    public static final String GEOSHAPE_DATATYPE = "geoshape";
    public static final String BARCODE_DATATYPE = "barcode";
    public static final String TIME_DATATYPE = "time";
    public static final String DATETIME_DATATYPE = "dateTime";

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
    private FormLayoutDao formLayoutDao;

    @Autowired
    private FormLayoutMediaDao formLayoutMediaDao;

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

    @Autowired
    private XformParserHelper xformParserHelper;

    @Autowired
    private XformParser xformParser;

    @Transactional
    public FormLayout createCRFMetaData(CrfMetaDataObject cmdObject) throws Exception {

        CrfVersion crfVersion = null;
        FormLayout formLayout = null;
        CrfBean crfBean = null;
        Section section = null;

        crfBean = (CrfBean) crfDao.findByOcOID(cmdObject.crf.getOcoid());
        if (crfBean != null) {
            crfBean.setUpdateId(cmdObject.ub.getId());
            crfBean.setDateUpdated(new Date());
            crfBean = crfDao.saveOrUpdate(crfBean);

            formLayout = formLayoutDao.findByOcOID(cmdObject.version.getOcoid());
            if (formLayout == null) {
                formLayout = new FormLayout();
                formLayout = populateFormLayout(formLayout, crfBean, cmdObject);
                formLayout = formLayoutDao.saveOrUpdate(formLayout);
            }

            crfVersion = crfVersionDao.findAllByCrfId(crfBean.getCrfId()).get(0);
            section = sectionDao.findByCrfVersionOrdinal(crfVersion.getCrfVersionId(), 1);

        } else {
            crfBean = new CrfBean();
            crfBean = populateCrf(crfBean, cmdObject);
            Integer crfId = (Integer) crfDao.save(crfBean);
            crfBean.setCrfId(crfId);

            // Create new Form Layout
            formLayout = new FormLayout();
            formLayout = populateFormLayout(formLayout, crfBean, cmdObject);
            formLayout = formLayoutDao.saveOrUpdate(formLayout);

            // Create new CRF Version
            crfVersion = new CrfVersion();
            crfVersion = populateCrfVersion(crfBean, crfVersion, cmdObject);
            crfVersion = crfVersionDao.saveOrUpdate(crfVersion);

            // Create Section
            section = sectionDao.findByCrfVersionOrdinal(crfVersion.getCrfVersionId(), 1);
            if (section == null) {
                section = new Section();
                section = populateCrfVersion(section, crfVersion, cmdObject);
                sectionDao.saveOrUpdate(section);
                section = sectionDao.findByCrfVersionOrdinal(crfVersion.getCrfVersionId(), 1);
            }
        }
        createGroups(cmdObject.container, cmdObject.html, cmdObject.xform, crfBean, crfVersion, formLayout, section, cmdObject.ub, cmdObject.errors);

        if (cmdObject.errors.hasErrors()) {
            logger.error("Encounter validation errors while saving CRF.  Rolling back transaction.");
            throw new RuntimeException("Encountered validation errors while saving CRF.");
        }
        return formLayout;
    }

    private void createGroups(XformContainer container, Html html, String submittedXformText, CrfBean crf, CrfVersion crfVersion, FormLayout formLayout,
            Section section, UserAccountBean ub, Errors errors) throws Exception {
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
                itemGroup.setLayoutGroupPath(xformGroup.getGroupPath());
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
                boolean calculate = xformItem.isCalculate();

                if (!xformItem.getItemName().equals("OC.STUDY_SUBJECT_ID") && !xformItem.getItemName().equals("OC.STUDY_SUBJECT_ID_CONFIRM")
                        && (readonly == null || !readonly.trim().equals("true()") || (readonly.trim().equals("true()") && calculate))) {
                    Item item = createItem(html, xformGroup, xformItem, crf, ub, usedItemOids, errors);
                    if (item != null) {
                        ResponseType responseType = null;
                        if (calculate) {
                            responseType = responseTypeDao.findByResponseTypeName("calculation");
                        } else {
                            responseType = getResponseType(html, xformItem);
                        }
                        ResponseSet responseSet = responseSetService.getResponseSet(html, submittedXformText, xformItem, crfVersion, responseType, item,
                                errors);
                        // add if statement
                        ItemFormMetadata ifmd = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                        if (ifmd == null) {
                            ifmd = createItemFormMetadata(html, xformItem, item, responseSet, section, crfVersion, itemOrdinal);
                        }
                        ArrayList<VersioningMap> vm = versioningMapDao.findByVersionIdFormLayoutIdAndItemId(crfVersion.getCrfVersionId(),
                                formLayout.getFormLayoutId(), item.getItemId(), itemOrdinal);
                        if (vm.size() == 0) {
                            createVersioningMap(crfVersion, item, formLayout, xformItem.getItemOrderInForm());
                        }
                        //
                        ItemGroupMetadata igmd = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                        if (igmd == null) {
                            igmd = createItemGroupMetadata(html, item, crfVersion, itemGroup, isRepeating, itemOrdinal);
                        }
                        itemOrdinal++;

                    }
                }
            }
        }

    }

    private ItemGroupMetadata createItemGroupMetadata(Html html, Item item, CrfVersion crfVersion, ItemGroup itemGroup, boolean isRepeating,
            Integer itemOrdinal) {
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
        itemGroupMetadata.setCrfVersion(crfVersion);
        itemGroupMetadata.setItem(item);
        itemGroupMetadata.setOrdinal(itemOrdinal);
        itemGroupMetadata.setShowGroup(true);
        itemGroupMetadata = itemGroupMetadataDao.saveOrUpdate(itemGroupMetadata);
        return itemGroupMetadata;
    }

    private void createVersioningMap(CrfVersion crfVersion, Item item, FormLayout formLayout, int itemOrderInForm) {
        VersioningMapId versioningMapId = new VersioningMapId();
        versioningMapId.setCrfVersionId(crfVersion.getCrfVersionId());
        versioningMapId.setItemId(item.getItemId());
        versioningMapId.setFormLayoutId(formLayout.getFormLayoutId());
        versioningMapId.setItemOrderInForm(itemOrderInForm);

        VersioningMap versioningMap = new VersioningMap();
        versioningMap.setVersionMapId(versioningMapId);
        versioningMapDao.saveOrUpdate(versioningMap);
    }

    private ItemFormMetadata createItemFormMetadata(Html html, XformItem xformItem, Item item, ResponseSet responseSet, Section section, CrfVersion crfVersion,
            Integer itemOrdinal) {
        ItemFormMetadata itemFormMetadata = new ItemFormMetadata();
        itemFormMetadata.setCrfVersionId(crfVersion.getCrfVersionId());
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
            item = itemDao.saveOrUpdate(item);
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

    public XformContainer parseInstance(String xform, Errors errors, Html html, String crfName) throws Exception {

        // Could use the following xpath to get all leaf nodes in the case
        // of multiple levels of groups: //*[count(./*) = 0]
        // For now will assume a structure of /form/item or /form/group/item
        Document doc = null;
        try {
            InputStream stream = new ByteArrayInputStream(xform.getBytes(StandardCharsets.UTF_8));
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);

            NodeList instances = doc.getElementsByTagName("instance");

            // All whitespace outside tags gets parsed as Text objects and returned
            // by the various Node methods. We need to ignore these and
            // focus on actual Elements

            Element instance = null;
            // List<XformItem> items = new ArrayList<XformItem>();
            List<XformGroup> groups = new ArrayList<XformGroup>();

            // Get the primary instance
            for (int i = 0; i < instances.getLength(); i++) {
                Element curInstance = (Element) instances.item(i);
                if (curInstance instanceof Element) {
                    instance = curInstance;
                    break;
                }
            }

            // Get the form element
            Node form = null;
            String path = "";
            List<String> instanceItemsPath = new ArrayList<>();
            for (int i = 0; i < instance.getChildNodes().getLength(); i++) {
                Node curNode = instance.getChildNodes().item(i);
                if (curNode instanceof Element) {
                    form = curNode;
                    path = "/" + form.getNodeName();
                    for (int j = 0; j < form.getChildNodes().getLength(); j++) {
                        Node node = form.getChildNodes().item(j);
                        if (node instanceof Element && !node.getNodeName().equals("meta") && !node.getNodeName().equals("formhub")) {
                            instanceItemsPath = xformParserHelper.instanceItemPaths(node, instanceItemsPath, path + "/" + node.getNodeName(), errors);
                        }
                    }
                    logger.info("list size: " + instanceItemsPath.size());
                }
            }

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/html/body");

            Node bodyNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            List<String> bodyGroupPaths = new ArrayList<>();
            bodyGroupPaths = xformParserHelper.bodyGroupNodePaths(bodyNode, bodyGroupPaths);

            List<XformItem> xformItems = new ArrayList<>();
            int itemOrderInForm = 0;
            for (Bind bd : html.getHead().getModel().getBind()) {
                // OC-7690 AC1: CRF elements can be defined as read-only = yes and calculation attribute = non-null to
                // make them read-only calculated elements.
                if (!bd.getNodeSet().endsWith("/meta/instanceID") && !bodyGroupPaths.contains(bd.getNodeSet()) && (bd.getReadOnly() == null
                        || bd.getReadOnly().equals("false()") || (bd.getReadOnly().equals("true()") && bd.getCalculate() != null))) {
                    itemOrderInForm++;
                    XformItem xformItem = new XformItem();
                    xformItem.setItemDataType(bd.getType());
                    xformItem.setItemGroup(bd.getItemGroup());
                    String itemPath = bd.getNodeSet();
                    xformItem.setItemPath(itemPath);
                    int index = itemPath.lastIndexOf("/");
                    String itemName = itemPath.substring(index + 1);
                    xformItem.setItemName(itemName);
                    xformItem.setItemOrderInForm(itemOrderInForm);
                    if (bd.getReadOnly() != null) {
                        xformItem.setReadonly(bd.getReadOnly());
                    }
                    if (bd.getCalculate() != null) {
                        xformItem.setCalculate(true);
                    } else {
                    }
                    xformItems.add(xformItem);
                } else if (!bd.getNodeSet().endsWith("/meta/instanceID") && !bodyGroupPaths.contains(bd.getNodeSet())
                        && (bd.getReadOnly().equals("true()") && bd.getCalculate() == null && bd.getItemGroup() != null)) {
                    validateReadOnlyElements(bd, errors);
                }

            }

            List<String> repeatGroupPathList = new ArrayList<>();
            repeatGroupPathList = xformParserHelper.bodyRepeatNodePaths(bodyNode, repeatGroupPathList);

            validateNestedRepeats(repeatGroupPathList, errors);

            validateDataTypes(xformItems, errors);

            validateOcGroupNotNull(xformItems, errors);

            List<XformGroup> repeatingXformGroups = new ArrayList();

            for (String repeatGroupPath : repeatGroupPathList) {
                XformGroup repeatingXformGroup = null;
                for (XformItem xformItem : xformItems) {
                    String ipath = xformItem.getItemPath();
                    int index = -1;
                    while (index != 0) {
                        index = ipath.lastIndexOf("/");
                        ipath = ipath.substring(0, index);
                        if (repeatGroupPath.equals(ipath)) {
                            if (repeatingXformGroup == null) {
                                repeatingXformGroup = new XformGroup();
                                repeatingXformGroup.setGroupName(xformItem.getItemGroup());
                                repeatingXformGroup.setGroupPath(repeatGroupPath);
                                repeatingXformGroup.setRepeating(true);
                                repeatingXformGroup.getItems().add(xformItem);
                            } else {
                                if (repeatingXformGroup.getGroupName().equals(xformItem.getItemGroup())
                                        && repeatingXformGroup.getGroupPath().equals(repeatGroupPath)) {
                                } else {
                                    validateItemsInRepeatingGroup(xformItem, repeatGroupPath, errors);
                                }
                                repeatingXformGroup.getItems().add(xformItem);
                            }
                            index = 0;
                            if (!repeatingXformGroups.contains(repeatingXformGroup))
                                repeatingXformGroups.add(repeatingXformGroup);
                        }
                    }
                }
            }

            boolean itemExistInRepeat;
            List<XformItem> nonRepeatXformItems = new ArrayList<>();
            for (XformItem xformItem : xformItems) {
                itemExistInRepeat = false;
                for (XformGroup repeatingXformGroup : repeatingXformGroups) {
                    for (XformItem xItem : repeatingXformGroup.getItems()) {
                        if (xItem.getItemName().equals(xformItem.getItemName())) {
                            itemExistInRepeat = true;
                        }
                    }
                }
                if (!itemExistInRepeat) {
                    nonRepeatXformItems.add(xformItem);
                }

            }

            List<XformGroup> nonRepeatingXformGroups = new ArrayList<>();
            Set<String> groupSet = new HashSet<>();
            for (XformItem xformItem : nonRepeatXformItems) {
                if (xformItem.getItemGroup() != null) {
                    groupSet.add(xformItem.getItemGroup());
                }
            }
            for (String group : groupSet) {
                XformGroup xformGroup = new XformGroup();
                xformGroup.setGroupName(group);
                xformGroup.setRepeating(false);
                nonRepeatingXformGroups.add(xformGroup);
            }

            for (XformItem nonRepeatXformItem : nonRepeatXformItems) {
                for (XformGroup nonRepeatingXformGroup : nonRepeatingXformGroups) {
                    if (nonRepeatXformItem.getItemGroup() != null && nonRepeatingXformGroup.getGroupName() != null
                            && nonRepeatXformItem.getItemGroup().equals(nonRepeatingXformGroup.getGroupName())) {
                        nonRepeatingXformGroup.getItems().add(nonRepeatXformItem);
                    }
                }
            }
            List<XformGroup> allGroups = new ArrayList<>();
            validateItemsInRepeating(repeatingXformGroups, nonRepeatingXformGroups, allGroups, errors);
            validateElementAndLayoutGroup(xformItems, errors);
            validateItemUniquenessInCRF(instanceItemsPath, errors);

            XformContainer container = new XformContainer();
            container.setGroups(allGroups);
            container.setInstanceName(crfName);
            return container;
        } catch (

        Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
    }

    // nested repeating groups not allowed more than once
    // Repeating layout groups can be included at most one time in a nested layout groups structure
    // OC-7671 AC7: Repeating group must not be nested inside another repeating group (directly or
    // indirectly).
    private void validateNestedRepeats(List<String> repeatGroupList, Errors errors) {
        for (String repeatGroup : repeatGroupList) {
            int index = -1;
            String parentPath = repeatGroup;
            while (index != 0) {
                index = parentPath.lastIndexOf("/");
                parentPath = parentPath.substring(0, index);
                if (repeatGroupList.contains(parentPath)) {
                    int innerGroupLastIndex = repeatGroup.lastIndexOf("/");
                    String innerGroupLayout = repeatGroup.substring(innerGroupLastIndex + 1);
                    int outerGroupLastIndex = parentPath.lastIndexOf("/");
                    String outerGroupLayout = parentPath.substring(outerGroupLastIndex + 1);

                    errors.rejectValue("name", "nested_repeat_group_not_allowed",
                            "Repeating Group <" + innerGroupLayout + "> cannot be nested within Repeating Group <" + outerGroupLayout + ">");
                }
            }

        }
    }

    // itemName is unique within crf
    private void validateItemUniquenessInCRF(List<String> instanceItemsPath, Errors errors) {
        List<String> itemNames = new ArrayList<>();
        for (String itemPath : instanceItemsPath) {
            int index = itemPath.lastIndexOf("/");
            String item = itemPath.substring(index + 1);
            if (itemNames.contains(item)) {
                errors.rejectValue("name", "duplicate_item_name", "ItemName:  " + item);
            } else {
                itemNames.add(item);
            }
        }

    }

    // CRFs must have a data group defined for every Item.
    // OC-7671 AC5: All elements with "readonly" != "yes" or "calculation" = non-null must have Item Group
    // defined.
    private void validateOcGroupNotNull(List<XformItem> xformItems, Errors errors) {
        for (XformItem xformItem : xformItems) {
            if (xformItem.getItemGroup() == null) {
                errors.rejectValue("name", "group_name_missing_for_this_item", "Element <" + xformItem.getItemName() + "> must be assigned to an Item Group");
            }
        }
    }

    // OC-7671 AC3: Only Items with supported data types can be included. Supported types are - integer,
    // decimal, text, select_one, select_multiple, date, image, calculate, acknowledge, audio, video
    private void validateDataTypes(List<XformItem> xformItems, Errors errors) {
        for (XformItem xformItem : xformItems) {
            if (xformItem.getItemDataType().equals(GEOPOINT_DATATYPE) || xformItem.getItemDataType().equals(GEOTRACE_DATATYPE)
                    || xformItem.getItemDataType().equals(GEOSHAPE_DATATYPE) || xformItem.getItemDataType().equals(BARCODE_DATATYPE)
                    || xformItem.getItemDataType().equals(TIME_DATATYPE) || xformItem.getItemDataType().equals(DATETIME_DATATYPE)) {
                errors.rejectValue("name", "data_type_not_supported",
                        "Element <" + xformItem.getItemName() + "> has an unsupported data type: <" + xformItem.getItemDataType() + ">");
            }
        }
    }

    // AC14: The data group assigned to an Item in a repeating layout group must not be
    // assigned to any Item that is not directly or indirectly in the same repeating layout group.
    // OC-7671 AC9: Items in repeating group must not have the same item group as items not in the repeating
    // group.
    private void validateItemsInRepeating(List<XformGroup> repeatingXformGroups, List<XformGroup> nonRepeatingXformGroups, List<XformGroup> allGroups,
            Errors errors) {
        List<String> groupNames = new ArrayList<>();
        for (XformGroup repeatingXformGroup : repeatingXformGroups) {
            allGroups.add(repeatingXformGroup);
            if (groupNames.contains(repeatingXformGroup.getGroupName())) {
                errors.rejectValue("name", "repeating_layout_group", "Elements in Repeating Group <" + repeatingXformGroup.getGroupName()
                        + ">  must not have the same value in column 'bind::oc:itemgroup' as other elements");
            } else {
                groupNames.add(repeatingXformGroup.getGroupName());
            }
        }
        for (XformGroup nonRepeatingXformGroup : nonRepeatingXformGroups) {
            allGroups.add(nonRepeatingXformGroup);
            if (groupNames.contains(nonRepeatingXformGroup.getGroupName())) {
                errors.rejectValue("name", "repeating_layout_group", "Elements in Repeating Group <" + nonRepeatingXformGroup.getGroupName()
                        + ">  must not have the same value in column 'bind::oc:itemgroup' as other elements");
            } else {
                groupNames.add(nonRepeatingXformGroup.getGroupName());
            }
        }
    }

    // OC-7671 AC2: Elements and Layout Groups may not have the same names as each other
    private void validateElementAndLayoutGroup(List<XformItem> xformItems, Errors errors) {
        Set<String> groupLayoutList = new HashSet<>();
        List<String> itemList = new ArrayList<>();
        for (XformItem xformItem : xformItems) {
            int itemLastIndex = xformItem.getItemPath().lastIndexOf("/");
            String itemName = xformItem.getItemPath().substring(itemLastIndex + 1);
            String itemPath = xformItem.getItemPath().substring(0, itemLastIndex);
            int groupLastIndex = itemPath.lastIndexOf("/");
            String groupName = itemPath.substring(groupLastIndex + 1);
            groupLayoutList.add(groupName);
            itemList.add(itemName);
        }
        for (String groupLayout : groupLayoutList) {
            if (itemList.contains(groupLayout)) {
                errors.rejectValue("name", "element_and_layout_group", "Elements and/or Groups cannot have the same name: <" + groupLayout + ">");
            }
        }
    }

    // AC13: All items located directly or indirectly in a repeating layout group must
    // be assigned to the same data group.
    // OC-7671 AC8: All items in a repeating group must have the same item group.
    private void validateItemsInRepeatingGroup(XformItem xformItem, String repeatGroupPath, Errors errors) {
        int itemLastIndex = xformItem.getItemPath().lastIndexOf("/");
        String itemName = xformItem.getItemPath().substring(itemLastIndex + 1);
        int groupLastIndex = repeatGroupPath.lastIndexOf("/");
        String repeatGroupLayout = repeatGroupPath.substring(groupLastIndex + 1);
        errors.rejectValue("name", "repeating_layout_group_item_assigned_to_wrong_group", "    Element <" + itemName
                + "> must have the same value in column 'bind::oc:itemgroup' as all other elements in Repeating Group <" + repeatGroupLayout + ">");

    }

    // OC-7671 AC4: All elements with "readonly" = "yes" and "calculation" = null must not have Item
    // Group defined.
    private void validateReadOnlyElements(Bind bd, Errors errors) {
        int lastIndex = bd.getNodeSet().lastIndexOf("/");
        String nodeName = bd.getNodeSet().substring(lastIndex + 1);
        errors.rejectValue("name", "note_type_with_ocgroup_value",
                "Read-only note element <" + nodeName + "> cannot have a value in column 'bind::oc:itemgroup'");
    }

    public FileItem getMediaFileItemFromFormManager(String fileLink, String crfOid, String formLayoutOid) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(fileLink, HttpMethod.GET, entity, byte[].class, "1");
        FileItem fileItem = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            String fileName = "";
            FileOutputStream output = null;
            try {
                String disposition = response.getHeaders().get("Content-Disposition").get(0);
                fileName = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
                String dir = Utils.getCrfMediaFilePath(crfOid, formLayoutOid);
                if (!new File(dir).exists()) {
                    new File(dir).mkdirs();
                    logger.debug("Made the directory " + dir);
                }
                File file = new File(dir + fileName);
                output = new FileOutputStream(file);
                IOUtils.write(response.getBody(), output);
                fileItem = new DiskFileItem("media_file", response.getHeaders().get("Content-Type").get(0), false, fileName, 100000000, file);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileItem;
    }

    public void executeIndividualCrf(ExecuteIndividualCrfObject eicObject) {
        for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : eicObject.formLayoutDefs) {
            FormLayout formLayout;
            List<FileItem> items = eicObject.items;
            if (items == null) {
                items = new ArrayList();
            }

            List<String> fileLinks = null;
            String xform = "";
            RestTemplate rest = new RestTemplate();
            if (eicObject.crf != null) {
                List<Version> versions = eicObject.crf.getVersions();
                for (Version version : versions) {
                    if (version.getName().equals(formLayoutDef.getOID())) {
                        fileLinks = version.getFileLinks();
                        for (String fileLink : fileLinks) {
                            if (fileLink.endsWith(FORM_SUFFIX)) {
                                xform = rest.getForObject(fileLink, String.class);
                            }
                        }

                        if (!eicObject.errors.hasErrors()) {
                            // Parse instance and xform
                            Html html = null;
                            try {
                                html = xformParser.unMarshall(xform);
                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            XformContainer container = null;
                            try {
                                container = parseInstance(xform, eicObject.errors, html, eicObject.crf.getName());
                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            if (eicObject.errors.hasErrors()) {
                                return;
                            }

                            // Save meta-data in database
                            try {
                                try {
                                    formLayout = createCRFMetaData(
                                            new CrfMetaDataObject(eicObject.crf, version, container, eicObject.currentStudy, eicObject.ub, html, xform, items,
                                                    eicObject.errors, formLayoutDef.getURL(), eicObject.crfName, eicObject.crfDescription));
                                    saveFormArtifactsInOCDataDirectory(fileLinks, eicObject.crf.getOcoid(), version.getOcoid());
                                    saveMediaFiles(fileLinks, eicObject.crf.getOcoid(), formLayout);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } catch (RuntimeException e) {
                                logger.error("Error encountered while saving CRF: " + e.getMessage());
                                logger.error(ExceptionUtils.getStackTrace(e));
                                if (!eicObject.errors.hasErrors())
                                    throw e;
                            }
                        }
                    }
                }
            }
        }

    }

    private void saveFormArtifactsInOCDataDirectory(List<String> fileLinks, String crfOid, String formLayoutOid) throws IOException {
        // Create the directory structure for saving the media
        String dir = Utils.getCrfMediaFilePath(crfOid, formLayoutOid);
        if (!new File(dir).exists()) {
            new File(dir).mkdirs();
            logger.debug("Made the directory " + dir);
        }
        // Save any media files
        for (String fileLink : fileLinks) {
            String fileName = "";
            int startIndex = fileLink.lastIndexOf('/');
            if (startIndex != -1) {
                fileName = fileLink.substring(startIndex + 1);
            }
            saveAttachedFiles(fileLink, dir, fileName);
        }
    }

    public void saveAttachedFiles(String uri, String dir, String fileName) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class, "1");

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            FileOutputStream output = new FileOutputStream(new File(dir + File.separator + fileName));
            IOUtils.write(response.getBody(), output);
        }
    }

    private FormLayout populateFormLayout(FormLayout formLayout, CrfBean crfBean, CrfMetaDataObject cmdObject) {
        formLayout.setName(cmdObject.version.getName());
        formLayout.setDescription(cmdObject.version.getDescription());
        formLayout.setCrf(crfBean);
        formLayout.setUserAccount(userDao.findById(cmdObject.ub.getId()));
        formLayout.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        formLayout.setRevisionNotes(cmdObject.version.getDescription());
        formLayout.setOcOid(crfVersionDao.getValidOid(new CrfVersion(), crfBean.getOcOid(), formLayout.getName()));
        formLayout.setXform(cmdObject.xform);
        formLayout.setXformName(cmdObject.container.getInstanceName());
        formLayout.setUrl(cmdObject.formLayoutUrl);
        return formLayout;

    }

    private CrfBean populateCrf(CrfBean crfBean, CrfMetaDataObject cmdObject) {
        crfBean.setName(cmdObject.crfName);
        crfBean.setDescription(cmdObject.crfDescription);
        crfBean.setUserAccount(userDao.findById(cmdObject.ub.getId()));
        crfBean.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        crfBean.setStudy(studyDao.findById(cmdObject.currentStudy.getId()));
        crfBean.setOcOid(cmdObject.crf.getOcoid());
        crfBean.setUpdateId(cmdObject.ub.getId());
        crfBean.setDateUpdated(new Date());
        return crfBean;
    }

    private CrfVersion populateCrfVersion(CrfBean crfBean, CrfVersion crfVersion, CrfMetaDataObject cmdObject) {
        crfVersion.setName(cmdObject.version.getName());
        crfVersion.setDescription(cmdObject.version.getDescription());
        crfVersion.setCrf(crfBean);
        crfVersion.setUserAccount(userDao.findById(cmdObject.ub.getId()));
        crfVersion.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        crfVersion.setRevisionNotes(cmdObject.version.getDescription());
        crfVersion.setOcOid(crfVersionDao.getValidOid(new CrfVersion(), crfBean.getOcOid(), crfVersion.getName()));
        return crfVersion;
    }

    private Section populateCrfVersion(Section section, CrfVersion crfVersion, CrfMetaDataObject cmdObject) {
        section.setCrfVersion(crfVersion);
        section.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        section.setLabel("");
        section.setTitle("");
        section.setSubtitle("");
        section.setPageNumberLabel("");
        section.setOrdinal(1);
        section.setUserAccount(userDao.findById(cmdObject.ub.getId())); // not null
        section.setBorders(0);
        return section;
    }

    private void saveMediaFiles(List<String> fileLinks, String crfOid, FormLayout formLayout) throws IOException {
        // Create the directory structure for saving the media
        String dir = Utils.getCrfMediaFilePathWithoutSysPath(crfOid, formLayout.getOcOid());
        for (String fileLink : fileLinks) {
            String fileName = "";
            int startIndex = fileLink.lastIndexOf('/');
            if (startIndex != -1) {
                fileName = fileLink.substring(startIndex + 1);
            }
            if (!fileLink.endsWith(FORM_SUFFIX) && !fileLink.endsWith(INSTANCEQUERIES_SUFFIX) && !fileLink.endsWith(FORMQUERIES_SUFFIX)
                    && !fileLink.endsWith(XLS_SUFFIX) && !fileLink.endsWith(INSTANCE_SUFFIX)) {

                FormLayoutMedia media = formLayoutMediaDao.findByFormLayoutIdAndFilePath(formLayout.getFormLayoutId(), dir);
                if (media == null) {
                    media = new FormLayoutMedia();
                    media.setFormLayout(formLayout);
                    media.setName(fileName);
                    media.setPath(dir);
                    media.setEventCrfId(0);
                    formLayoutMediaDao.saveOrUpdate(media);
                }
            }
        }
    }

}

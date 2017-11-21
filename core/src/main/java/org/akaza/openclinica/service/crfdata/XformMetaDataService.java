package org.akaza.openclinica.service.crfdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.dao.core.CoreResources;
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
import org.akaza.openclinica.domain.Status;
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
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.akaza.openclinica.domain.datamap.VersioningMapId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.domain.xform.XformGroup;
import org.akaza.openclinica.domain.xform.XformItem;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.service.dto.FormVersion;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class XformMetaDataService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String FORM_SUFFIX = "form.xml";
    public static final String INSTANCE_SUFFIX = "instance.tpl";
    public static final String INSTANCEQUERIES_SUFFIX = "instance-queries.tpl";
    public static final String FORMQUERIES_SUFFIX = "form-queries.xml";
    public static final String FORMPREVIEW_SUFFIX = "form-preview.xml";
    public static final String XLS_SUFFIX = ".xls";

    public static final String GEOPOINT_DATATYPE = "geopoint";
    public static final String GEOTRACE_DATATYPE = "geotrace";
    public static final String GEOSHAPE_DATATYPE = "geoshape";
    public static final String BARCODE_DATATYPE = "barcode";
    public static final String TIME_DATATYPE = "time";
    public static final String DATETIME_DATATYPE = "dateTime";
    public static String VERSION = "data_group.json";

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

    private CoreResources coreResources;

    @Transactional
    public FormLayout createCRFMetaData(CrfMetaDataObject cmdObject) throws Exception {

        CrfVersion crfVersion = null;
        FormLayout formLayout = null;
        CrfBean crfBean = null;
        Section section = null;

        crfBean = (CrfBean) crfDao.findByOcOID(cmdObject.crf.getOcoid());
        if (crfBean != null) {
            crfBean.setUpdateId(cmdObject.ub.getUserId());
            crfBean.setName(cmdObject.crf.getName());
            crfBean.setDateUpdated(new Date());
            crfBean = crfDao.saveOrUpdate(crfBean);

            formLayout = formLayoutDao.findByOcOID(cmdObject.version.getOcoid());
            if (formLayout == null) {
                formLayout = new FormLayout();
                formLayout = populateFormLayout(formLayout, crfBean, cmdObject);
                formLayout = formLayoutDao.saveOrUpdate(formLayout);
            } else if (!formLayout.getStatus().equals(Status.AVAILABLE)) {
                UserAccount userAccount = userDao.findById(cmdObject.ub.getUserId());
                formLayout.setStatus(Status.AVAILABLE);
                formLayout.setUserAccount(userAccount);
                formLayout.setDateCreated(new Date());
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
        createGroups(cmdObject.container, crfBean, crfVersion, formLayout, section, cmdObject.ub, cmdObject.errors);
        return formLayout;
    }

    private void createGroups(XformContainer container, CrfBean crf, CrfVersion crfVersion, FormLayout formLayout, Section section, UserAccount ub,
            Errors errors) throws Exception {
        Integer itemOrdinal = 1;
        ArrayList<String> usedGroupOids = new ArrayList<String>();
        ArrayList<String> usedItemOids = new ArrayList<String>();

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
                itemGroup.setUserAccount(userDao.findById(ub.getUserId()));
                itemGroup.setOcOid(xformGroup.getGroupOid());
                usedGroupOids.add(itemGroup.getOcOid());
                itemGroup = itemGroupDao.saveOrUpdate(itemGroup);
            } else {
                itemGroup.setName(xformGroup.getGroupName());
                itemGroup = itemGroupDao.saveOrUpdate(itemGroup);
            }
            boolean isRepeating = xformGroup.isRepeating();
            // Create Item specific DB entries: item,
            // response_set,item_form_metadata,versioning_map,item_group_metadata
            // for (UserControl widget : widgets) {

            for (XformItem xformItem : xformGroup.getItems()) {

                Item item = createItem(xformGroup, xformItem, crf, ub, usedItemOids, errors);
                if (item != null) {
                    ResponseType responseType = getResponseType(xformItem);
                    ResponseSet responseSet = responseSetService.getResponseSet(xformItem, crfVersion, responseType, item, errors);
                    // add if statement
                    ItemFormMetadata ifmd = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                    if (ifmd == null) {
                        ifmd = createItemFormMetadata(xformItem, item, responseSet, section, crfVersion, itemOrdinal);
                    } else {
                        ifmd.setRequired(xformItem.isRequired());
                        ifmd.setLeftItemText(xformItem.getLeftItemText());
                        ifmd.setItem(item);
                        ifmd.setResponseSet(responseSet);
                        ifmd = itemFormMetadataDao.saveOrUpdate(ifmd);
                    }
                    ArrayList<VersioningMap> vm = versioningMapDao.findByVersionIdFormLayoutIdAndItemId(crfVersion.getCrfVersionId(),
                            formLayout.getFormLayoutId(), item.getItemId(), itemOrdinal);
                    if (vm.size() == 0) {
                        createVersioningMap(crfVersion, item, formLayout, xformItem.getItemOrderInForm());
                    }
                    //
                    ItemGroupMetadata igmd = itemGroupMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                    if (igmd == null) {
                        igmd = createItemGroupMetadata(item, crfVersion, itemGroup, isRepeating, itemOrdinal);
                    }
                    itemOrdinal++;

                }

            }
        }

    }

    private ItemGroupMetadata createItemGroupMetadata(Item item, CrfVersion crfVersion, ItemGroup itemGroup, boolean isRepeating, Integer itemOrdinal) {
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

    private ItemFormMetadata createItemFormMetadata(XformItem xformItem, Item item, ResponseSet responseSet, Section section, CrfVersion crfVersion,
            Integer itemOrdinal) {
        ItemFormMetadata itemFormMetadata = new ItemFormMetadata();
        itemFormMetadata.setCrfVersionId(crfVersion.getCrfVersionId());
        itemFormMetadata.setResponseSet(responseSet);
        itemFormMetadata.setItem(item);
        itemFormMetadata.setSubheader("");
        itemFormMetadata.setHeader("");
        itemFormMetadata.setLeftItemText(xformItem.getLeftItemText());
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
        itemFormMetadata.setRequired(xformItem.isRequired());
        itemFormMetadata.setDefaultValue("");
        itemFormMetadata.setResponseLayout("Vertical");
        itemFormMetadata.setWidthDecimal("");
        itemFormMetadata.setShowItem(true);
        itemFormMetadata = itemFormMetadataDao.saveOrUpdate(itemFormMetadata);
        return itemFormMetadata;
    }

    private Item createItem(XformGroup xformGroup, XformItem xformItem, CrfBean crf, UserAccount ub, ArrayList<String> usedItemOids, Errors errors)
            throws Exception {
        ItemDataType newDataType = getItemDataType(xformItem);

        Item item = itemDao.findByNameCrfId(xformItem.getItemName(), crf.getCrfId());
        if (item != null) {
            item.setDescription(xformItem.getItemDescription());
            item = itemDao.saveOrUpdate(item);
        } else {
            item = new Item();
            item.setName(xformItem.getItemName());
            item.setDescription(xformItem.getItemDescription());
            item.setUnits("");
            item.setPhiStatus(false);
            item.setItemDataType(newDataType);
            item.setItemReferenceType(itemRefTypeDao.findByItemReferenceTypeId(1));
            item.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            item.setUserAccount(userDao.findById(ub.getUserId()));
            item.setOcOid(xformItem.getItemOid());
            usedItemOids.add(item.getOcOid());
            item = itemDao.saveOrUpdate(item);
        }

        return item;
    }

    private ItemDataType getItemDataType(XformItem xformItem) {
        String dataType = xformItem.getItemDataType();

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
        else
            return null;

    }

    private ResponseType getResponseType(XformItem xformItem) {
        String responseType = xformItem.getItemDataType();
        String readOnly = "";
        String relevant = "";
        if (xformItem.getReadonly() != null)
            readOnly = xformItem.getReadonly();
        if (xformItem.getRelevant() != null)
            relevant = xformItem.getRelevant();

        if ((xformItem.isCalculate() && !readOnly.equals("true()") && !relevant.equals("false()")) || (xformItem.isCalculate() && relevant.equals("false()")))
            return responseTypeDao.findByResponseTypeName("calculation");
        else if (responseType.equals("string"))
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

    public FileItem getMediaFileItemFromFormManager(String fileLink, String crfOid, String formLayoutOid) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(replaceUrlWithServiceGatewayURL(fileLink), HttpMethod.GET, entity, byte[].class, "1");
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

    public Set<Long> executeIndividualCrf(ExecuteIndividualCrfObject eicObject, Set<Long> publishedVersions) {
        for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : eicObject.formLayoutDefs) {

            List<String> fileLinks = null;
            String vForm = "";
            RestTemplate rest = new RestTemplate();
            if (eicObject.form != null) {
                List<FormVersion> versions = eicObject.form.getVersions();
                for (FormVersion version : versions) {
                    if (version.getName().equals(formLayoutDef.getOID())) {
                        fileLinks = version.getFileLinks();
                        for (String fileLink : fileLinks) {
                            if (fileLink.endsWith(VERSION)) {
                                vForm = rest.getForObject(replaceUrlWithServiceGatewayURL(fileLink), String.class);
                                break;
                            }
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        TypeReference<List<XformGroup>> mapType = new TypeReference<List<XformGroup>>() {
                        };
                        List<XformGroup> jsonList = null;
                        try {
                            jsonList = mapper.readValue(vForm, mapType);
                        } catch (JsonParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JsonMappingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        XformContainer xformContainer = new XformContainer();
                        xformContainer.setGroups(jsonList);
                        eicObject.setContainer(xformContainer);

                        // Save meta-data in database
                        saveFormMetadata(eicObject, version, eicObject.container, formLayoutDef, fileLinks);
                        StudyEnvEnum existingEnv = version.getPublishedEnvType();
                        StudyEnvEnum publishingEnv = eicObject.study.getEnvType();
                        if ((publishingEnv.equals(StudyEnvEnum.TEST) && existingEnv.equals(StudyEnvEnum.NOT_PUBLISHED))
                                || (publishingEnv.equals(StudyEnvEnum.PROD) && !existingEnv.equals(StudyEnvEnum.PROD))) {
                            publishedVersions.add(version.getId());
                        }

                    }
                }
            }
        }
        return publishedVersions;
    }

    public void saveFormMetadata(ExecuteIndividualCrfObject eicObj, FormVersion version, XformContainer container,
            OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef, List<String> fileLinks) {

        try {
            try {
                FormLayout formLayout = createCRFMetaData(
                        new CrfMetaDataObject(eicObj.form, version, container, eicObj.getStudy(), eicObj.ub, eicObj.errors, formLayoutDef.getURL()));
                saveFormArtifactsInOCDataDirectory(fileLinks, eicObj.getStudy(), eicObj.form.getOcoid(), version.getOcoid(), formLayout);
                saveMediaFiles(fileLinks, eicObj.getStudy(), eicObj.form.getOcoid(), formLayout);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            logger.error("Error encountered while saving CRF: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void saveFormArtifactsInOCDataDirectory(List<String> fileLinks, Study study, String crfOid, String formLayoutOid, FormLayout formLayout)
            throws IOException {
        // Create the directory structure for saving the media

        String dir = Utils.getFilePath() + Utils.getCrfMediaPath(study.getOc_oid(), study.getFilePath(), crfOid, formLayoutOid);
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
            saveAttachedFiles(fileLink, dir, fileName, formLayout);
        }
    }

    public void saveAttachedFiles(String uri, String dir, String fileName, FormLayout formLayout) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(replaceUrlWithServiceGatewayURL(uri), HttpMethod.GET, entity, byte[].class, "1");

        File file = new File(dir + File.separator + fileName);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            FileOutputStream output = new FileOutputStream(file);
            IOUtils.write(response.getBody(), output);
        }

        if (fileName.equals(FORM_SUFFIX)) {
            String xformOutput = new String(Files.readAllBytes(Paths.get(file.getPath())));
            String hash = DigestUtils.md5Hex(xformOutput);
            if (formLayout.getXform() == null || !formLayout.getXform().equals(hash)) {
                formLayout.setXform(DigestUtils.md5Hex(xformOutput));
                formLayoutDao.saveOrUpdate(formLayout);
            }
        }
    }

    private FormLayout populateFormLayout(FormLayout formLayout, CrfBean crfBean, CrfMetaDataObject cmdObject) {
        formLayout.setName(cmdObject.version.getName());
        formLayout.setDescription(cmdObject.version.getDescription());
        formLayout.setCrf(crfBean);
        formLayout.setUserAccount(userDao.findById(cmdObject.ub.getUserId()));
        formLayout.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        formLayout.setRevisionNotes(cmdObject.version.getDescription());
        formLayout.setOcOid(cmdObject.version.getOcoid());
        // formLayout.setXform(null);
        formLayout.setXformName(cmdObject.container.getInstanceName());
        formLayout.setUrl(cmdObject.formLayoutUrl);
        return formLayout;

    }

    private CrfBean populateCrf(CrfBean crfBean, CrfMetaDataObject cmdObject) {
        crfBean.setName(cmdObject.crf.getName());
        crfBean.setDescription(cmdObject.crf.getDescription());
        crfBean.setUserAccount(userDao.findById(cmdObject.ub.getUserId()));
        crfBean.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        crfBean.setStudy(studyDao.findById(cmdObject.study.getStudyId()));
        crfBean.setOcOid(cmdObject.crf.getOcoid());
        crfBean.setUpdateId(cmdObject.ub.getUserId());
        crfBean.setDateUpdated(new Date());
        return crfBean;
    }

    private CrfVersion populateCrfVersion(CrfBean crfBean, CrfVersion crfVersion, CrfMetaDataObject cmdObject) {
        crfVersion.setName(cmdObject.version.getName());
        crfVersion.setDescription(cmdObject.version.getDescription());
        crfVersion.setCrf(crfBean);
        crfVersion.setUserAccount(userDao.findById(cmdObject.ub.getUserId()));
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
        section.setUserAccount(userDao.findById(cmdObject.ub.getUserId())); // not null
        section.setBorders(0);
        return section;
    }

    private void saveMediaFiles(List<String> fileLinks, Study study, String crfOid, FormLayout formLayout) throws IOException {
        // Create the directory structure for saving the media
        String dir = Utils.getCrfMediaPath(study.getOc_oid(), study.getFilePath(), crfOid, formLayout.getOcOid());
        for (String fileLink : fileLinks) {
            String fileName = "";
            int startIndex = fileLink.lastIndexOf('/');
            if (startIndex != -1) {
                fileName = fileLink.substring(startIndex + 1);
            }
            if (!fileLink.endsWith(FORM_SUFFIX) && !fileLink.endsWith(INSTANCEQUERIES_SUFFIX) && !fileLink.endsWith(FORMQUERIES_SUFFIX)
                    && !fileLink.endsWith(XLS_SUFFIX) && !fileLink.endsWith(INSTANCE_SUFFIX) && !fileLink.endsWith(VERSION)
                    && !fileLink.endsWith(FORMPREVIEW_SUFFIX)) {

                FormLayoutMedia media = formLayoutMediaDao.findByFormLayoutIdFileNameForNoteTypeMedia(formLayout.getFormLayoutId(), fileName, dir);
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

    private String replaceUrlWithServiceGatewayURL(String url) {
        return CoreResources.getSBSFieldFormservice() + url.split("api")[1];

    }
}

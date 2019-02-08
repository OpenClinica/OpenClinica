package org.akaza.openclinica.service.crfdata;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.*;
import org.akaza.openclinica.service.dto.FormVersion;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Service
public class XformMetaDataService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String FORM_SUFFIX = "form.xml";
    public static final String INSTANCE_SUFFIX = "instance.tpl";
    public static final String INSTANCEQUERIES_SUFFIX = "instance-queries.tpl";
    public static final String FORMQUERIES_SUFFIX = "form-queries.xml";
    public static final String FORMPARTICIPATE_SUFFIX = "form-participate.xml";
    public static final String FORMPREVIEW_SUFFIX = "form-preview.xml";
    public static final String XLS_SUFFIX = ".xls";

    public static final String GEOPOINT_DATATYPE = "geopoint";
    public static final String GEOTRACE_DATATYPE = "geotrace";
    public static final String GEOSHAPE_DATATYPE = "geoshape";
    public static final String BARCODE_DATATYPE = "barcode";
    public static final String TIME_DATATYPE = "time";
    public static final String DATETIME_DATATYPE = "dateTime";
    public static String VERSION = "data_group.json";

    private StudyDao studyDao;

    private CrfDao crfDao;
    private SectionDao sectionDao;
    private UserAccountDao userDao;
    private CrfVersionDao crfVersionDao;
    private FormLayoutDao formLayoutDao;
    private FormLayoutMediaDao formLayoutMediaDao;
    private ItemGroupDao itemGroupDao;
    private ItemGroupMetadataDao itemGroupMetadataDao;
    private VersioningMapDao versioningMapDao;
    private ItemFormMetadataDao itemFormMetadataDao;
    private ItemDao itemDao;
    private ItemDataTypeDao itemDataTypeDao;
    private ItemReferenceTypeDao itemRefTypeDao;
    private ResponseTypeDao responseTypeDao;
    private ResponseSetService responseSetService;
    private XformParserHelper xformParserHelper;
    private XformParser xformParser;
    private CoreResources coreResources;
    private ResponseSetDao responseSetDao;


    public XformMetaDataService(StudyDao studyDao, CrfDao crfDao, SectionDao sectionDao, UserAccountDao userDao, CrfVersionDao crfVersionDao, FormLayoutDao formLayoutDao, FormLayoutMediaDao formLayoutMediaDao, ItemGroupDao itemGroupDao, ItemGroupMetadataDao itemGroupMetadataDao, VersioningMapDao versioningMapDao, ItemFormMetadataDao itemFormMetadataDao, ItemDao itemDao, ItemDataTypeDao itemDataTypeDao, ItemReferenceTypeDao itemRefTypeDao, ResponseTypeDao responseTypeDao, ResponseSetService responseSetService, XformParserHelper xformParserHelper, XformParser xformParser, CoreResources coreResources, ResponseSetDao responseSetDao) {
        this.studyDao = studyDao;
        this.crfDao = crfDao;
        this.sectionDao = sectionDao;
        this.userDao = userDao;
        this.crfVersionDao = crfVersionDao;
        this.formLayoutDao = formLayoutDao;
        this.formLayoutMediaDao = formLayoutMediaDao;
        this.itemGroupDao = itemGroupDao;
        this.itemGroupMetadataDao = itemGroupMetadataDao;
        this.versioningMapDao = versioningMapDao;
        this.itemFormMetadataDao = itemFormMetadataDao;
        this.itemDao = itemDao;
        this.itemDataTypeDao = itemDataTypeDao;
        this.itemRefTypeDao = itemRefTypeDao;
        this.responseTypeDao = responseTypeDao;
        this.responseSetService = responseSetService;
        this.xformParserHelper = xformParserHelper;
        this.xformParser = xformParser;
        this.coreResources = coreResources;
        this.responseSetDao = responseSetDao;
    }

    public FormLayout createCRFMetaData(CrfMetaDataObject cmdObject) throws Exception {

        CrfVersion crfVersion = null;
        FormLayout formLayout = null;
        CrfBean crfBean = null;
        Section section = null;
        boolean formExist = false;

        List<Item> items = null;
        List<ResponseSet> responseSets = null;
        List<ItemFormMetadata> ifms = null;
        List<ItemGroupMetadata> igms = null;
        List<VersioningMap> vms = null;
        List<ItemGroup> itemGroups = null;

        crfBean = (CrfBean) crfDao.findByOcOID(cmdObject.crf.getOcoid());
        if (crfBean != null) {
            formExist = true;
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

            items = itemDao.findAllByCrfVersion(crfVersion.getCrfVersionId());
            responseSets = responseSetDao.findAllByVersion(crfVersion.getCrfVersionId());
            ifms = itemFormMetadataDao.findAllByCrfVersion(crfVersion.getCrfVersionId());
            igms = itemGroupMetadataDao.findAllByCrfVersion(crfVersion.getCrfVersionId());
            vms = versioningMapDao.findAllByVersionId(crfVersion.getCrfVersionId());
            itemGroups = itemGroupDao.findByCrfVersionId(crfVersion.getCrfVersionId());
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
        createGroups(cmdObject.container, crfBean, crfVersion, formLayout, section, cmdObject.ub, cmdObject.errors, formExist, items, responseSets, ifms, igms, vms, itemGroups);

        return formLayout;
    }

    private void createGroups(XformContainer container, CrfBean crf, CrfVersion crfVersion, FormLayout formLayout, Section section, UserAccount ub,
                              Errors errors, boolean formExist, List<Item> items, List<ResponseSet> responseSets, List<ItemFormMetadata> ifms, List<ItemGroupMetadata> igms, List<VersioningMap> vms, List<ItemGroup> itemGroups) throws Exception {

        Integer itemOrdinal = 1;
        ArrayList<String> usedGroupOids = new ArrayList<String>();
        ArrayList<String> usedItemOids = new ArrayList<String>();

        // for (Group htmlGroup : htmlGroups) {
        for (XformGroup xformGroup : container.getGroups()) {
            ItemGroup itemGroup = null;

            if (formExist) {
                for (ItemGroup ig : emptyIfNull(itemGroups)) {
                    if (ig.getOcOid().equals(xformGroup.getGroupOid())) {
                        itemGroup = ig;
                        break;
                    }
                }
            }

            if (itemGroup == null) {
                itemGroup = new ItemGroup();
                itemGroup.setName(xformGroup.getGroupName());
                itemGroup.setLayoutGroupPath(xformGroup.getGroupPath());
                itemGroup.setCrf(crf);
                itemGroup.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
                itemGroup.setUserAccount(userDao.findById(ub.getUserId()));
                itemGroup.setOcOid(xformGroup.getGroupOid());
                usedGroupOids.add(itemGroup.getOcOid());
            } else {
                itemGroup.setName(xformGroup.getGroupName());
            }
            itemGroup = itemGroupDao.saveOrUpdate(itemGroup);
            boolean isRepeating = xformGroup.isRepeating();
            // Create Item specific DB entries: item,
            // response_set,item_form_metadata,versioning_map,item_group_metadata
            // for (UserControl widget : widgets) {

            for (XformItem xformItem : xformGroup.getItems()) {

                Item item = createItem(xformGroup, xformItem, crf, ub, usedItemOids, errors, formExist, items);
                if (item != null) {
                    ResponseType responseType = getResponseType(xformItem);
                    ResponseSet responseSet = responseSetService.getResponseSet(xformItem, crfVersion, responseType, item, errors, responseSets);
                    // add if statement

                    ItemFormMetadata ifmd = null;
                    for (ItemFormMetadata ifm : emptyIfNull(ifms)) {
                        if (ifm.getItem().getItemId() == item.getItemId()) {
                            ifmd = ifm;
                            break;
                        }
                    }
                    if (ifmd == null) {
                        createItemFormMetadata(xformItem, item, responseSet, section, crfVersion, itemOrdinal);
                    } else {
                        ifmd.setRequired(xformItem.isRequired());
                        ifmd.setLeftItemText(xformItem.getLeftItemText());
                        ifmd.setItem(item);
                        ifmd.setResponseSet(responseSet);
                        itemFormMetadataDao.saveOrUpdate(ifmd);
                    }
                    VersioningMap vm = null;

                    for (VersioningMap v : emptyIfNull(vms)) {
                        if (v.getVersionMapId().getFormLayoutId() == formLayout.getFormLayoutId() && v.getVersionMapId().getItemId() == item.getItemId()) {
                            vm = v;
                            break;
                        }
                    }

                    if (vm == null) {
                        createVersioningMap(crfVersion, item, formLayout, xformItem.getItemOrderInForm());
                    }
                    //
                    ItemGroupMetadata igmd = null;

                    for (ItemGroupMetadata igm : emptyIfNull(igms)) {
                        if (igm.getItem().getItemId() == item.getItemId()) {
                            igmd = igm;
                            break;
                        }
                    }
                    if (igmd == null) {
                        createItemGroupMetadata(item, crfVersion, itemGroup, isRepeating, itemOrdinal);
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

    private Item createItem(XformGroup xformGroup, XformItem xformItem, CrfBean crf, UserAccount ub, ArrayList<String> usedItemOids, Errors errors, boolean formExist, List<Item> items)
            throws Exception {
        Item item = null;
        ItemDataType newDataType = getItemDataType(xformItem);

        if (formExist) {
            for (Item i : emptyIfNull(items)) {
                if (i.getOcOid().equals(xformItem.getItemOid())) {
                    item = i;
                    break;
                }
            }
        }
        if (item == null) {
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
        } else {
            item.setDescription(xformItem.getItemDescription());
        }
        item = itemDao.saveOrUpdate(item);


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
            rest.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
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
                logger.info("After Saving Form Artifacts");
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
                formLayout.setExternalInstance("");
                formLayoutDao.saveOrUpdate(formLayout);
            }
        }


        logger.info("Form name:{} version:{} and Filename: {} wrote in OC directory", formLayout.getCrf().getName(), formLayout.getName(), fileName);
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
        logger.info("Saving media file {}", fileLink);
            String fileName = "";
            int startIndex = fileLink.lastIndexOf('/');
            if (startIndex != -1) {
                fileName = fileLink.substring(startIndex + 1);
            }
            if (!fileLink.endsWith(FORM_SUFFIX) && !fileLink.endsWith(INSTANCEQUERIES_SUFFIX) && !fileLink.endsWith(FORMQUERIES_SUFFIX) && !fileLink.endsWith(FORMPARTICIPATE_SUFFIX)
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
        String updatedURL = CoreResources.getSBSFieldFormservice() + url.split("api")[1];
        return updatedURL;

    }


}

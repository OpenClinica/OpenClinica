package org.akaza.openclinica.control.admin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.domain.xform.XformGroup;
import org.akaza.openclinica.domain.xform.XformItem;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.domain.xform.dto.Bind;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.crfdata.Crf;
import org.akaza.openclinica.service.crfdata.Version;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateXformCRFVersionServlet extends SecureController {
    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();
    public static final String FORM_SUFFIX = "form.xml";

    @Override
    protected void processRequest() throws Exception {
        CrfDao crfDao = (CrfDao) SpringServletAccess.getApplicationContext(context).getBean("crfDao");
        CrfVersionDao crfVersionDao = (CrfVersionDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionDao");

        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundleProvider.updateLocale(locale);
        resword = ResourceBundleProvider.getWordsBundle(locale);

        // Retrieve submission data from multipart request
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        String submittedCrfName = retrieveFormFieldValue(items, "crfName");
        String submittedCrfVersionName = retrieveFormFieldValue(items, "versionName");
        String submittedCrfVersionDescription = retrieveFormFieldValue(items, "versionDescription");
        String submittedRevisionNotes = retrieveFormFieldValue(items, "revisionNotes");
        String submittedXformText = retrieveFormFieldValue(items, "xformText");

        CRFVersionBean version = (CRFVersionBean) session.getAttribute("version");
        logger.debug("Found original CRF ID for new CRF Version:" + version.getCrfId());

        // Create container for holding validation errors
        DataBinder dataBinder = new DataBinder(new CrfVersion());
        Errors errors = dataBinder.getBindingResult();

        Crf response = filesTofm(items, currentStudy.getOid(), submittedCrfName);
        List<String> fileLinks = null;

        RestTemplate rest = new RestTemplate();
        if (response != null) {
            List<Version> versions = response.getVersions();
            Version vs = versions.get(0);
            fileLinks = vs.getFileLinks();
            submittedCrfVersionName = vs.getName();

            for (String fileLink : fileLinks) {
                if (fileLink.endsWith(FORM_SUFFIX)) {
                    submittedXformText = rest.getForObject(fileLink, String.class);
                    break;
                }
            }

        }

        // Validate all upload form fields were populated
        validateFormFields(errors, version, submittedCrfName, submittedCrfVersionName, submittedCrfVersionDescription, submittedRevisionNotes,
                submittedXformText);

        if (!errors.hasErrors()) {

            // Parse instance and xform
            XformParser parser = (XformParser) SpringServletAccess.getApplicationContext(context).getBean("xformParser");
            Html html = parser.unMarshall(submittedXformText);
            XformContainer container = parseInstance(submittedXformText, errors, html, submittedCrfName);

            // Save meta-data in database
            XformMetaDataService xformService = (XformMetaDataService) SpringServletAccess.getApplicationContext(context).getBean("xformMetaDataService");
            try {
                xformService.createCRFMetaData(version, container, currentStudy, ub, html, submittedCrfName, submittedCrfVersionName,
                        submittedCrfVersionDescription, submittedRevisionNotes, submittedXformText, items, errors);
            } catch (RuntimeException e) {
                logger.error("Error encountered while saving CRF: " + e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
                // If there are no logged validation errors, this was an unanticipated exception
                // and should be allow to crash the page for now
                if (!errors.hasErrors())
                    throw e;
            }
        }
        // Save errors to request so they can be displayed to the user
        if (errors.hasErrors()) {
            request.setAttribute("errorList", errors.getAllErrors());
            logger.debug("Found at least one error.  CRF data not saved.");
        } else {
            logger.debug("Didn't find any errors.  CRF data saved.");

            // Save any media files uploaded with xform
            CrfBean crf = (submittedCrfName == null || submittedCrfName.equals("")) ? crfDao.findByCrfId(version.getCrfId())
                    : crfDao.findByName(submittedCrfName);
            CrfVersion newVersion = crfVersionDao.findByNameCrfId(submittedCrfVersionName, crf.getCrfId());
            // saveAttachedMedia(items, crf, newVersion);

            saveArtifactsInFM(fileLinks, crf, newVersion);

        }

        forwardPage(Page.CREATE_XFORM_CRF_VERSION_SERVLET);
    }

    private void validateFormFields(Errors errors, CRFVersionBean version, String submittedCrfName, String submittedCrfVersionName,
            String submittedCrfVersionDescription, String submittedRevisionNotes, String submittedXformText) {

        // Verify CRF Name is populated
        if (version.getCrfId() == 0 && (submittedCrfName == null || submittedCrfName.equals(""))) {
            DataBinder crfDataBinder = new DataBinder(new CrfBean());
            Errors crfErrors = crfDataBinder.getBindingResult();
            crfErrors.rejectValue("name", "crf_val_crf_name_blank", resword.getString("CRF_name"));
            errors.addAllErrors(crfErrors);
        }

        DataBinder crfVersionDataBinder = new DataBinder(new CrfVersion());
        Errors crfVersionErrors = crfVersionDataBinder.getBindingResult();

        // Verify CRF Version Name is populated
        if (submittedCrfVersionName == null || submittedCrfVersionName.equals("")) {
            crfVersionErrors.rejectValue("name", "crf_ver_val_name_blank", resword.getString("version_name"));
        }

        // Verify CRF Version Description is populated
        if (submittedCrfVersionDescription == null || submittedCrfVersionDescription.equals("")) {
            crfVersionErrors.rejectValue("description", "crf_ver_val_desc_blank", resword.getString("crf_version_description"));
        }

        // Verify CRF Version Revision Notes is populated
        if (submittedRevisionNotes == null || submittedRevisionNotes.equals("")) {
            crfVersionErrors.rejectValue("revisionNotes", "crf_ver_val_rev_notes_blank", resword.getString("revision_notes"));
        }

        // Verify Xform text is populated
        if (submittedXformText == null || submittedXformText.equals("")) {
            crfVersionErrors.rejectValue("xform", "crf_ver_val_xform_blank", resword.getString("xform"));
        }

        errors.addAllErrors(crfVersionErrors);
    }

    private XformContainer parseInstance(String xform, Errors errors, Html html, String submittedCrfName) throws Exception {
        XformParserHelper xformParserHelper = (XformParserHelper) SpringServletAccess.getApplicationContext(context).getBean("xformParserHelper");

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
                            instanceItemsPath = xformParserHelper.instanceItemPaths(node, instanceItemsPath, path + "/" + node.getNodeName());
                        }
                    }
                    System.out.println("list size: " + instanceItemsPath.size());
                }
            }

            List<XformItem> xformItems = new ArrayList<>();
            for (Bind bd : html.getHead().getModel().getBind()) {
                if (bd.getItemGroup() != null) {
                    XformItem xformItem = new XformItem();
                    xformItem.setItemGroup(bd.getItemGroup());
                    String itemPath = bd.getNodeSet();
                    xformItem.setItemPath(itemPath);
                    int index = itemPath.lastIndexOf("/");
                    String itemName = itemPath.substring(index + 1);
                    xformItem.setItemName(itemName);
                    if (bd.getReadOnly() != null) {
                        xformItem.setReadonly(bd.getReadOnly());
                    }
                    xformItems.add(xformItem);
                }
            }

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/html/body");

            // List<Body> body = (List<Body>) html.getBody();
            // Body b = html.getBody();

            List<String> repeatGroupPathList = new ArrayList<>();

            Node bodyNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            repeatGroupPathList = xformParserHelper.bodyRepeatNodePaths(bodyNode, repeatGroupPathList);

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
                                if (repeatingXformGroup.getGroupName().equals(xformItem.getItemGroup())) {
                                    repeatingXformGroup.getItems().add(xformItem);
                                } else {
                                    // AC13: All items located directly or indirectly in a repeating layout group must
                                    // be assigned
                                    // to the same
                                    // data group.
                                    // AC14: The data group assigned to an Item in a repeating layout group must not be
                                    // assigned to
                                    // any Item
                                    // that is not directly or indirectly in the same repeating layout group.
                                    errors.rejectValue("", "repeating_layout_group_item_assigned_to_wrong_group",
                                            "Group Name:  " + xformItem.getItemGroup() + "  --- ItemPath:  " + xformItem.getItemPath());
                                }
                            }
                            index = 0;
                            if (!repeatingXformGroups.contains(repeatingXformGroup))
                                repeatingXformGroups.add(repeatingXformGroup);
                        }
                    }

                }
            }

            List<XformGroup> xformGroups = new ArrayList<>();
            Set<String> groupSet = new HashSet<>();
            for (XformItem xformItem : xformItems) {
                groupSet.add(xformItem.getItemGroup());
            }
            for (String group : groupSet) {
                XformGroup xformGroup = new XformGroup();
                xformGroup.setGroupName(group);
                xformGroups.add(xformGroup);
            }

            for (XformItem xformItem : xformItems) {
                for (XformGroup xformGroup : xformGroups) {
                    if (xformItem.getItemGroup().equals(xformGroup.getGroupName())) {
                        xformGroup.getItems().add(xformItem);
                    }
                }
            }

            List<XformGroup> nonRepeatingXformGroups = new ArrayList<>();
            for (XformGroup xformGroup : xformGroups) {
                if (!repeatingXformGroups.contains(xformGroup)) {
                    nonRepeatingXformGroups.add(xformGroup);
                }
            }

            List<XformGroup> allGroups = new ArrayList<>();
            for (XformGroup repeatingXformGroup : repeatingXformGroups) {
                allGroups.add(repeatingXformGroup);
            }
            for (XformGroup nonRepeatingXformGroup : nonRepeatingXformGroups) {
                allGroups.add(nonRepeatingXformGroup);
            }

            // Repeating layout groups can be included at most one time in a nested layout groups structure
            validateNestedRepeats(repeatGroupPathList, errors);

            // itemName is unique within crf
            validateItemUniquenessInCRF(instanceItemsPath, errors);

            // AC11: CRFs must have a data group defined for every Item (i.e., no "ungrouped" Items allowed).
            validateOcGroupNotNull(xformItems, errors);

            // verify group names compatible with group naming convention

            // AC15: Items that are not directly or indirectly in a repeating layout group can be assigned to the same
            // data group as each other.

            XformContainer container = new XformContainer();
            container.setGroups(allGroups);
            container.setInstanceName(submittedCrfName);
            return container;
        } catch (

        Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
    }

    private void itemBelongsToRepeatGroup(List<String> repeatGroupList, String itemPath) {
        for (String repeatGroup : repeatGroupList) {
            int index = -1;
            String parentPath = itemPath;
            while (index != 0) {
                index = parentPath.lastIndexOf("/");
                parentPath = parentPath.substring(0, index);
                if (repeatGroupList.contains(parentPath)) {

                }
            }

        }
    }

    // nested repeating groups not allowed more than once
    private void validateNestedRepeats(List<String> repeatGroupList, Errors errors) {
        for (String repeatGroup : repeatGroupList) {
            int index = -1;
            String parentPath = repeatGroup;
            while (index != 0) {
                index = parentPath.lastIndexOf("/");
                parentPath = parentPath.substring(0, index);
                if (repeatGroupList.contains(parentPath)) {
                    errors.rejectValue("name", "nested_repeat_group_not_allowed", "Repeat GroupPath:  " + repeatGroup);
                    // errors.rejectValue("name", repeatGroup, resword.getString("nested_repeat_group_not_allowed"));
                }
            }

        }
    }

    public void validateItemUniquenessInCRF(List<String> instanceItemsPath, Errors errors) {
        List<String> itemNames = new ArrayList<>();
        for (String itemPath : instanceItemsPath) {
            int index = itemPath.lastIndexOf("/");
            String item = itemPath.substring(index + 1);
            if (itemNames.contains(item)) {
                errors.rejectValue("name", "duplicate_item_name", "ItemName:  " + item);
                // errors.rejectValue("name", item, resword.getString("duplicate_item_name"));
            } else {
                itemNames.add(item);
            }
        }

    }

    public void validateOcGroupNotNull(List<XformItem> xformItems, Errors errors) {
        for (XformItem xformItem : xformItems) {
            if (xformItem.getItemGroup() == null) {
                errors.rejectValue("name", "group_name_missing_for_this_item", "ItemName:  " + xformItem.getItemName());
                // errors.rejectValue("name", xformItem.getItemName(),
                // resword.getString("group_name_missing_for_this_item"));
            }
        }

    }

    private String retrieveFormFieldValue(List<FileItem> items, String fieldName) throws Exception {
        for (FileItem item : items) {
            if (fieldName.equals(item.getFieldName()))
                return item.getString("UTF-8");
        }
        logger.warn("Form field '" + fieldName + "' missing from xform submission.");
        return "";
    }

    private void saveAttachedMedia(List<FileItem> items, CrfBean crf, CrfVersion version) {
        boolean hasFiles = false;
        for (FileItem item : items) {
            if (!item.isFormField() && item.getName() != null && !item.getName().isEmpty())
                hasFiles = true;
        }

        if (hasFiles) {
            // Create the directory structure for saving the media
            String dir = Utils.getCrfMediaFilePath(crf, version);
            if (!new File(dir).exists()) {
                new File(dir).mkdirs();
                logger.debug("Made the directory " + dir);
            }
            // Save any media files
            for (FileItem item : items) {
                if (!item.isFormField()) {

                    String fileName = item.getName();
                    // Some browsers IE 6,7 getName returns the whole path
                    int startIndex = fileName.lastIndexOf('\\');
                    if (startIndex != -1) {
                        fileName = fileName.substring(startIndex + 1, fileName.length());
                    }

                    File uploadedFile = new File(dir + File.separator + fileName);
                    try {
                        item.write(uploadedFile);
                    } catch (Exception e) {
                        throw new OpenClinicaSystemException(e.getMessage());
                    }
                }
            }
        }
    }

    private void saveArtifactsInFM(List<String> fileLinks, CrfBean crf, CrfVersion version) throws IOException {
        // Create the directory structure for saving the media
        String dir = Utils.getCrfMediaFilePath(crf, version);
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

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);

        // Make sure xforms are enabled
        String xformEnabled = CoreResources.getField("xform.enabled");
        if (xformEnabled == null || !xformEnabled.equals("true")) {
            addPageMessage(respage.getString("may_not_create_xforms"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_create_xforms"), "1");
        }

        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    private Crf filesTofm(List<FileItem> files, String studyOid, String formName) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        List<String> tempFileNames = new ArrayList<>();
        ArrayList<ByteArrayResource> byteArrayResources = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        String uploadFilesUrl = "http://fm.openclinica.info:8080/api/protocol/" + studyOid + "/forms/" + formName + "/artifacts";
        map.add("file", byteArrayResources);

        for (FileItem file : files) {
            String filename = file.getName();
            if (!file.isFormField()) {
                ByteArrayResource contentsAsResource = new ByteArrayResource(file.get()) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };
                map.get("file").add(contentsAsResource);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        // TODO: replace with Crf object instead of String object
        Crf response = restTemplate.postForObject(uploadFilesUrl, requestEntity, Crf.class);

        return response;
    }
}
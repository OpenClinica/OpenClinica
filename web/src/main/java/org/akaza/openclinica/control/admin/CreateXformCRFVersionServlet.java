package org.akaza.openclinica.control.admin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

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
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateXformCRFVersionServlet extends SecureController {
    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();

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
        
        // Validate all upload form fields were populated
        validateFormFields(errors, version, submittedCrfName,submittedCrfVersionName,submittedCrfVersionDescription,
        		submittedRevisionNotes,submittedXformText);

        
        if (!errors.hasErrors()){
        	
            // Parse instance and xform
            XformParser parser = (XformParser) SpringServletAccess.getApplicationContext(context).getBean("xformParser");
            XformContainer container = parseInstance(submittedXformText);
            Html html = parser.unMarshall(submittedXformText);


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
	        CrfBean crf = (submittedCrfName == null || submittedCrfName.equals("")) ? crfDao.findByCrfId(version.getCrfId()) : crfDao.findByName(submittedCrfName);
	        CrfVersion newVersion = crfVersionDao.findByNameCrfId(submittedCrfVersionName, crf.getCrfId());
	        saveAttachedMedia(items, crf, newVersion);
        }

        forwardPage(Page.CREATE_XFORM_CRF_VERSION_SERVLET);
    }

    private void validateFormFields(Errors errors, CRFVersionBean version, String submittedCrfName, String submittedCrfVersionName,
			String submittedCrfVersionDescription, String submittedRevisionNotes, String submittedXformText) {

    	// Verify CRF Name is populated
        if (version.getCrfId() == 0 && (submittedCrfName == null || submittedCrfName.equals(""))) {
            DataBinder crfDataBinder = new DataBinder(new CrfBean());
            Errors crfErrors = crfDataBinder.getBindingResult();
            crfErrors.rejectValue("name","crf_val_crf_name_blank",resword.getString("CRF_name"));
            errors.addAllErrors(crfErrors);
        }

        DataBinder crfVersionDataBinder = new DataBinder(new CrfVersion());
        Errors crfVersionErrors = crfVersionDataBinder.getBindingResult();

    	// Verify CRF Version Name is populated
        if (submittedCrfVersionName == null || submittedCrfVersionName.equals("")) {
        	crfVersionErrors.rejectValue("name","crf_ver_val_name_blank",resword.getString("version_name"));
        }

    	// Verify CRF Version Description is populated
        if (submittedCrfVersionDescription == null || submittedCrfVersionDescription.equals("")) {
        	crfVersionErrors.rejectValue("description","crf_ver_val_desc_blank",resword.getString("crf_version_description"));
        }

    	// Verify CRF Version Revision Notes is populated
        if (submittedRevisionNotes == null || submittedRevisionNotes.equals("")) {
        	crfVersionErrors.rejectValue("revisionNotes","crf_ver_val_rev_notes_blank",resword.getString("revision_notes"));
        }

    	// Verify Xform text is populated
        if (submittedXformText == null || submittedXformText.equals("")) {
        	crfVersionErrors.rejectValue("xform","crf_ver_val_xform_blank",resword.getString("xform"));
        }
        errors.addAllErrors(crfVersionErrors);
    }

	private XformContainer parseInstance(String xform) throws Exception {

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
            Element form = null;
            for (int i = 0; i < instance.getChildNodes().getLength(); i++) {
                Node curNode = instance.getChildNodes().item(i);
                if (curNode instanceof Element) {
                    form = (Element) curNode;
                    break;
                }
            }

            // Get the groups and grouped items
            for (int i = 0; i < form.getChildNodes().getLength(); i++) {
                if (form.getChildNodes().item(i) instanceof Element && ((Element) form.getChildNodes().item(i)).hasChildNodes()
                        && !((Element) form.getChildNodes().item(i)).getTagName().equals("meta")) {
                    Element group = (Element) form.getChildNodes().item(i);
                    XformGroup newGroup = new XformGroup();
                    newGroup.setGroupName(group.getTagName());
                    newGroup.setGroupPath("/" + form.getTagName() + "/" + group.getTagName());
                    groups.add(newGroup);
                    for (int j = 0; j < group.getChildNodes().getLength(); j++) {
                        if (group.getChildNodes().item(j) instanceof Element) {
                            Element item = (Element) group.getChildNodes().item(j);
                            XformItem newItem = new XformItem();
                            newItem.setItemPath("/" + form.getTagName() + "/" + group.getTagName() + "/" + item.getTagName());
                            newItem.setItemName(item.getTagName());
                            // group is null;
                            newGroup.getItems().add(newItem);
                        }
                    }
                }
            }
            XformContainer container = new XformContainer();
            container.setGroups(groups);
            container.setInstanceName(form.getTagName());
            return container;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
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

}
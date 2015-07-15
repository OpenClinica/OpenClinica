package org.akaza.openclinica.control.admin;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class CreateXformCRFVersionServlet extends SecureController {
    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();

    @Override
    protected void processRequest() throws Exception {
        // Retrieve submission data from multipart request
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);

        CRFVersionBean version = (CRFVersionBean) session.getAttribute("version");
        logger.debug("Found original CRF ID for new CRF Version:" + version.getCrfId());

        CRFDAO crfDAO = new CRFDAO(sm.getDataSource());
        CRFBean crf = null;

        // Retrieve CRFBean. Create one if it doesn't exist yet.
        if (version.getCrfId() > 0) {
            crf = (CRFBean) crfDAO.findByPK(version.getCrfId());
        } else {
            CRFBean newCRF = new CRFBean();
            newCRF.setName(retrieveFormFieldValue(items, "crfName"));
            newCRF.setDescription(retrieveFormFieldValue(items, "versionDescription"));
            newCRF.setOwner(ub);
            newCRF.setStatus(Status.AVAILABLE);
            newCRF.setStudyId(currentStudy.getId());
            crfDAO.create(newCRF);
            crf = (CRFBean) crfDAO.findByName(newCRF.getName());
        }

        // Create new CRF Version
        CRFVersionDAO versionDAO = new CRFVersionDAO(sm.getDataSource());
        CRFVersionBean newCRFVersion = new CRFVersionBean();
        newCRFVersion.setName(retrieveFormFieldValue(items, "versionName"));
        newCRFVersion.setDescription(retrieveFormFieldValue(items, "versionDescription"));
        newCRFVersion.setCrfId(crf.getId());
        newCRFVersion.setOwner(ub);
        newCRFVersion.setStatus(Status.AVAILABLE);
        newCRFVersion.setRevisionNotes(retrieveFormFieldValue(items, "revisionNotes"));
        newCRFVersion.setOid(versionDAO.getValidOid(new CRFVersionBean(), crf.getOid(), newCRFVersion.getName()));
        newCRFVersion.setXform(retrieveFormFieldValue(items, "xformText"));
        versionDAO.create(newCRFVersion);
        // Save any media files uploaded with xform
        saveAttachedMedia(items, crf, newCRFVersion);

        forwardPage(Page.CREATE_XFORM_CRF_VERSION_SERVLET);
    }

    private String retrieveFormFieldValue(List<FileItem> items, String fieldName) {
        for (FileItem item : items) {
            if (fieldName.equals(item.getFieldName()))
                return item.getString();
        }
        logger.warn("Form field '" + fieldName + "' missing from xform submission.");
        return "";
    }

    private void saveAttachedMedia(List<FileItem> items, CRFBean crf, CRFVersionBean newCRFVersion) {
        boolean hasFiles = false;
        for (FileItem item : items) {
            if (!item.isFormField() && item.getName() != null && !item.getName().isEmpty())
                hasFiles = true;
        }

        if (hasFiles) {
            // Create the directory structure for saving the media
            String dir = Utils.getCrfMediaFilePath(crf, newCRFVersion);
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

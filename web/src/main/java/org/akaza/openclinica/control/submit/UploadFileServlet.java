/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.rule.FileProperties;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;

public class UploadFileServlet extends SecureController {
    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper(new FileProperties(CoreResources.getField("crf.file.extensions"),
            CoreResources.getField("crf.file.extensionSettings")));

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
        if ("false".equals(session.getAttribute("mayProcessUploading"))) {
            addPageMessage(respage.getString("you_not_have_permission_upload_file"));
            request.setAttribute("uploadFileStauts", "noPermission");
        }
        return;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        HashMap<String, String> newUploadedFiles = (HashMap<String, String>) session.getAttribute("newUploadedFiles");
        if (newUploadedFiles == null) {
            newUploadedFiles = new HashMap<String, String>();
        }
        String submitted = fp.getString("submitted") != null ? fp.getString("submitted") : "";
        if ("no".equalsIgnoreCase(submitted)) {
            request.setAttribute("fileItemId", fp.getString("itemId"));
            request.setAttribute("inputName", fp.getString("inputName"));
            forwardPage(Page.FILE_UPLOAD);
        } else {
            String dir = Utils.getAttachedFilePath(currentStudy);
            if (dir == null || dir.length() <= 0) {
                request.setAttribute("uploadFileStauts", "failed");
                this.forwardPage(Page.FILE_UPLOAD);
            } else {
                if (!new File(dir).isDirectory()) {
                    new File(dir).mkdirs();
                    logger.info("Made the directory " + dir);
                }
                request.setAttribute("attachedFilePath", dir);
                try {
                    List<File> files = uploadHelper.returnFiles(request, context, dir, new OCFileRename());
                    String fileName = "";
                    for (File temp : files) {
                        //MultipartRequest multi = new MultipartRequest(request, dir, 20 * 1024 * 1024, new OCFileRename());
                        //Enumeration files = multi.getFileNames();

                        //if (files.hasMoreElements()) {
                        //File temp = multi.getFile((String) files.nextElement());
                        if (temp == null || temp.getName() == null) {
                            fileName = "";
                        } else {
                            fileName = temp.getName();
                            logger.info("fileName="+fileName);
                        }
                    }
                    logger.info("===== fileName="+fileName);
                    request.setAttribute("fileName", fileName);
                    request.setAttribute("uploadFileStatus", "successed");
                    String key = "";
                    String inputName = (String) request.getAttribute("inputName");
                    String itemId = (String) request.getAttribute("itemId");
                    request.setAttribute("fileItemId", itemId + "");
                    if (inputName != null && inputName.length() > 0) {
                        // for group file items
                        key = fileName;
                    } else {
                        key = itemId;
                    }
                    if (fileName.length() > 0) {
                        newUploadedFiles.put(key, dir + File.separator + fileName);
                        addPageMessage(fileName + " " + respage.getString("uploaded_successfully_go_to_data_entry_page_to_save_into_database"));
                    } else {
                        request.setAttribute("uploadFileStatus", "empty");
                        addPageMessage(respage.getString("no_file_uploaded_please_specify_file"));
                    }
                    if (inputName != null && inputName.length() > 0) {
                        request.setAttribute("inputName", inputName);
                    }
                    session.setAttribute("newUploadedFiles", newUploadedFiles);
                } catch (OpenClinicaSystemException e) {
                	request.setAttribute("uploadFileStauts", "failed");
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString(e.getErrorCode()));
                    Object[] arguments = e.getErrorParams();
                    addPageMessage(respage.getString("file_uploading_failed_please_check_logs_and_upload_again") + mf.format(arguments));
                    e.printStackTrace();
                }
                this.forwardPage(Page.FILE_UPLOAD);
            }
        }
    }

    /*
    class OCFileRename implements FileRenamePolicy {
        public File rename(File f) {
            // here, File f has been validated as a valid File.
            String pathAndName = f.getPath();
            int p = pathAndName.lastIndexOf('.');
            String newName = pathAndName.substring(0, p) + (new SimpleDateFormat("yyyyMMddHHmmssZ")).format(new Date()) + pathAndName.substring(p);
            return new File(newName);
        }
    }
    */

   public class OCFileRename implements org.akaza.openclinica.bean.rule.FileRenamePolicy {
    	private String checksum(File f, InputStream content) throws Exception {
    		byte[] buffer = new byte[8192];
    		MessageDigest md = MessageDigest.getInstance("SHA-1");

    		DigestInputStream dis = new DigestInputStream(content, md);
    		try {
    			while (dis.read(buffer) != -1);
    		} finally {
    			dis.close();
    		}

    		return DatatypeConverter.printHexBinary(md.digest());
    	}

        public File rename(File f, InputStream content) {
            // here, File f has been validated as a valid File.
            String pathAndName = f.getPath();
            String newName;
            int p = pathAndName.lastIndexOf('.');
            int n = pathAndName.lastIndexOf(File.separatorChar);
            logger.debug("found n: " + n);
            logger.debug("found p: " + p);
            String fileName = pathAndName.substring(n, p);
            logger.debug("found file name: " + fileName);
            if (Utils.isWithinRegexp(fileName, "\\W+")) {
                logger.debug("found non word characters");
                fileName = fileName.replaceAll("\\W+", "_");
            }
            try {
            	newName = pathAndName.substring(0, n) + File.separator + fileName +
            			checksum(f, content) + pathAndName.substring(p);
            } catch (Throwable e) {
            	e.printStackTrace();
            	return null;
            }
            // >> tbh 5545 remove all html-symbol characters here
            //            if (!Utils.isMatchingRegexp(newName, "[a-zA-Z_0-9/\\\\:.+-\\s]")) {
            //                logger.debug("found non word characters");
            //                newName = newName.replaceAll("[^a-zA-Z_0-9/\\\\:.+-\\s]", "_");
            //            }

            // << tbh 5545 replace all non-words with the underscore
            logger.debug("-- > returning: " + newName);
            return new File(newName);
        }
    }
}

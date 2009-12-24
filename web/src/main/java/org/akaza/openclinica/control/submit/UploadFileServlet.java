/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.FileRenamePolicy;

public class UploadFileServlet extends SecureController {
    Locale locale;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        locale = request.getLocale();
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
                if (!(new File(dir)).isDirectory()) {
                    (new File(dir)).mkdirs();
                    logger.info("Made the directory " + dir);
                }
                request.setAttribute("attachedFilePath", dir);
                try {
                    MultipartRequest multi = new MultipartRequest(request, dir, 20 * 1024 * 1024, new OCFileRename());
                    Enumeration files = multi.getFileNames();
                    String fileName = "";
                    if (files.hasMoreElements()) {
                        File temp = multi.getFile((String) files.nextElement());
                        if (temp == null || temp.getName() == null) {
                            fileName = "";
                        } else {
                            fileName = temp.getName();
                        }
                    }
                    request.setAttribute("fileName", fileName);
                    request.setAttribute("uploadFileStatus", "successed");
                    String key = "";
                    String inputName = multi.getParameter("inputName");
                    String itemId = multi.getParameter("itemId");
                    request.setAttribute("fileItemId", itemId + "");
                    if (inputName != null && inputName.length() > 0) {
                        // for group file items
                        key = fileName;
                    } else {
                        key = itemId;
                    }
                    if (fileName.length() > 0) {
                        newUploadedFiles.put(key, dir + File.separator + fileName);
                        addPageMessage(fileName
                            + " " + respage.getString("uploaded_successfully_go_to_data_entry_page_to_save_into_database"));
                    } else {
                        request.setAttribute("uploadFileStatus", "empty");
                        addPageMessage(respage.getString("no_file_uploaded_please_specify_file"));
                    }
                    if (inputName != null && inputName.length() > 0) {
                        request.setAttribute("inputName", inputName);
                    }
                    session.setAttribute("newUploadedFiles", newUploadedFiles);
                } catch (IOException e) {
                    request.setAttribute("uploadFileStauts", "failed");
                    addPageMessage(respage.getString("file_uploading_failed_please_check_logs_and_upload_again"));
                    e.printStackTrace();
                }
                this.forwardPage(Page.FILE_UPLOAD);
            }
        }
    }

    class OCFileRename implements FileRenamePolicy {
        public File rename(File f) {
            // here, File f has been validated as a valid File.
            String pathAndName = f.getPath();
            int p = pathAndName.lastIndexOf('.');
            String newName = pathAndName.substring(0, p) + (new SimpleDateFormat("yyyyMMddHHmmssZ")).format(new Date()) + pathAndName.substring(p);
            return new File(newName);
        }
    }
}
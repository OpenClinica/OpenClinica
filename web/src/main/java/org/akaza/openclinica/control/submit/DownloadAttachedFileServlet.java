/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.io.File;
import java.util.Locale;

/**
 * @author ywang (Dec., 2008)
 *
 */
public class DownloadAttachedFileServlet extends SecureController {

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        Locale locale = request.getLocale();
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("eventCRFId");
        EventCRFDAO edao = new EventCRFDAO(sm.getDataSource());

        if (eventCRFId > 0) {
            if (!entityIncluded(eventCRFId, ub.getName(), edao, sm.getDataSource())) {
                request.setAttribute("downloadStatus", "false");
                addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
                throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
            }
        } else {
            request.setAttribute("downloadStatus", "false");
            addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
            throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
        }

        if (ub.isSysAdmin()) {
            return;
        }
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        request.setAttribute("downloadStatus", "false");
        addPageMessage(respage.getString("you_not_have_permission_download_attached_file"));
        throw new InsufficientPermissionException(Page.DOWNLOAD_ATTACHED_FILE, resexception.getString("no_permission"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String filePathName = "";
        String attachedFilePath = "";
        String fileName = fp.getString("fileName");
        File f = new File(fileName);
        if (fileName != null && fileName.length() > 0) {
            attachedFilePath = Utils.getAttachedFilePath(currentStudy);
            filePathName = attachedFilePath + f.getName();
        }
        File file = new File(filePathName);
        if (!file.exists() || file.length() <= 0) {
            addPageMessage("File " + filePathName + " " + respage.getString("not_exist."));
        } else {
            request.setAttribute("downloadStatus", "true");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\";");

            response.setHeader("Pragma", "public");
            request.setAttribute("generate", filePathName);
            response.setHeader("Pragma", "public");
        }
        forwardPage(Page.DOWNLOAD_ATTACHED_FILE);
    }
}
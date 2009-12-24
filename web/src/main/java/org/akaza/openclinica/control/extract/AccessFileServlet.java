/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * @author thickerson
 *
 *
 */
public class AccessFileServlet extends SecureController {

    Locale locale;

    // < ResourceBundlerestext,respage,resexception;

    public static String getLink(int fId) {
        return "AccessFile?fileId=" + fId;
    }

    private static String WEB_DIR = "/WEB-INF/datasets/";

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int fileId = fp.getInt("fileId");
        ArchivedDatasetFileDAO asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());
        ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fileId);
        // asdfBean.setWebPath(WEB_DIR+
        // asdfBean.getDatasetId()+
        // "/"+
        // asdfBean.getName());
        Page finalTarget = Page.EXPORT_DATA_CUSTOM;
        /*
         * if (asdfBean.getExportFormatId() ==
         * ExportFormatBean.EXCELFILE.getId()) { //
         * response.setContentType("application/octet-stream");
         * response.setHeader("Content-Disposition", "attachment; filename=" +
         * asdfBean.getName()); logger.info("found file name: "+
         * finalTarget.getFileName()); //
         * finalTarget.setFileName(asdfBean.getWebPath()); finalTarget =
         * Page.GENERATE_EXCEL_DATASET; } else {
         */
        if (asdfBean.getFileReference().endsWith(".zip")) {
            response.setContentType("application/zip");
            response.setHeader("Content-disposition", "attachment; filename=\"" + asdfBean.getName() + "\";");

            response.setHeader("Pragma", "public");
        } else {
            response.setContentType("text/plain");
            // to ensure backwards compatability to text files shown on server
        }
        finalTarget.setFileName("/WEB-INF/jsp/extract/generatedFileDataset.jsp");
        // }
        // finalTarget.setFileName(asdfBean.getWebPath());
        request.setAttribute("generate", asdfBean.getFileReference());
        response.setHeader("Pragma", "public");
        forwardPage(finalTarget);
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = request.getLocale();
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);

        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO

    }

}

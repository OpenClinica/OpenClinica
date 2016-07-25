/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.job.ExportLogger;
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
        DatasetDAO dsDao = new DatasetDAO(sm.getDataSource());
        ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fileId);
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        DatasetBean dsBean = (DatasetBean) dsDao.findByPK(asdfBean.getDatasetId());
        int parentId = currentStudy.getParentStudyId();
        if(parentId==0)//Logged in at study level
        {
            StudyBean studyBean = (StudyBean )studyDao.findByPK(dsBean.getStudyId());
            parentId =  studyBean.getParentStudyId();//parent id of dataset created

        }
        //logic: is parentId of the dataset created not equal to currentstudy? or is current study
        if (parentId!=currentStudy.getId() )
if( dsBean.getStudyId() != currentStudy.getId())		{
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
        }
        ExportLogger.logAccessExportFile(ub, asdfBean );

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
        logger.debug("found file reference: " + asdfBean.getFileReference() + " and file name: " + asdfBean.getName());
        if (asdfBean.getFileReference().endsWith(".zip")) {
            response.setHeader("Content-disposition", "attachment; filename=\"" + asdfBean.getName() + "\";");
            response.setContentType("application/zip");
            // response.setContentType("application/download");
        } else if (asdfBean.getFileReference().endsWith(".pdf")) {
            response.setHeader("Content-disposition", "attachment; filename=\"" + asdfBean.getName() + "\";");
            response.setContentType("application/pdf");
            // response.setContentType("application/download; application/pdf");
        } else if (asdfBean.getFileReference().endsWith(".csv")) {
            response.setHeader("Content-disposition", "attachment; filename=\"" + asdfBean.getName() + "\";");
            response.setContentType("text/csv");
            // response.setContentType("application/download; text/csv");
        } else if (asdfBean.getFileReference().endsWith(".xml")) {
            response.setHeader("Content-disposition", "attachment; filename=\"" + asdfBean.getName() + "\";");
            response.setContentType("text/xml");
            // response.setContentType("application/download; text/xml");
        } else if (asdfBean.getFileReference().endsWith(".html")) {
            response.setHeader("Content-disposition", "filename=\"" + asdfBean.getName() + "\";");
            response.setContentType("text/html; charset=utf-8");
        } else {

            // response.setContentType("text/plain");
            // to ensure backwards compatability to text files shown on server
            // not needed anymore? tbh 10/2010
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

        locale = LocaleResolver.getLocale(request);
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

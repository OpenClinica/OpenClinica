/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.hibernate.ArchivedDatasetFilePermissionTagDao;
import core.org.akaza.openclinica.domain.datamap.ArchivedDatasetFilePermissionTag;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
    private static String ORIGINATING_PAGE = "/OpenClinica/MainMenu";

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        ArchivedDatasetFileDAO  asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());
        DatasetDAO dsDao = new DatasetDAO(sm.getDataSource());
        int fileId = fp.getInt("fileId");

        String study_oid = fp.getString("study_oid");
        Study study = null;
        List<String> permissionTagsList = null;
        DatasetBean dsBean = null;
        ArchivedDatasetFileBean asdfBean = null;

        if (!StringUtils.isEmpty(study_oid)) {
            study = getStudyDao().findPublicStudy(study_oid.toUpperCase());
            CoreResources.setRequestSchema(request, study.getSchemaName());
            study=getStudyDao().findByOid(study.getOc_oid());
            if (checkRolesByUserAndStudy(ub, study)) {
                permissionTagsList = getPermissionService().getPermissionTagsList(study, request);
                asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fileId);
                dsBean = (DatasetBean) dsDao.findByPK(asdfBean.getDatasetId());
                if(!hasAccessPermission(asdfBean,  permissionTagsList, ORIGINATING_PAGE)){
                    return;
                }
            } else {
                request.setAttribute("originatingPage", ORIGINATING_PAGE);
                forwardPage(Page.NO_ACCESS);
                return;
            }
        } else {
            permissionTagsList = getPermissionTagsList();
            asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fileId);
            dsBean = (DatasetBean) dsDao.findByPK(asdfBean.getDatasetId());
            String originatingPage = "ExportDataset?datasetId=" + dsBean.getId();
            if(!hasAccessPermission(asdfBean,  permissionTagsList, originatingPage)){
                return;
            }
        }


        if (study_oid == null) {
            int parentId = currentStudy.checkAndGetParentStudyId();
            if (parentId == 0)//Logged in at study level
            {
                Study studyBean = (Study) getStudyDao().findByPK(dsBean.getStudyId());
                parentId = studyBean.checkAndGetParentStudyId();//parent id of dataset created
            }

            //logic: is parentId of the dataset created not equal to currentstudy? or is current study
            if (parentId != currentStudy.getStudyId())
                if (dsBean.getStudyId() != currentStudy.getStudyId()) {
                    addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
                    throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
                }
        }

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
        Page finalTarget = Page.EXPORT_DATA_CUSTOM;
        finalTarget.setFileName("/WEB-INF/jsp/extract/generatedFileDataset.jsp");
        // finalTarget.setFileName(asdfBean.getWebPath());
        request.setAttribute("generate", asdfBean.getFileReference());
        response.setHeader("Pragma", "public");
        forwardPage(finalTarget);

    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.exceptions",locale);

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

    public List<ArchivedDatasetFilePermissionTag>  getArchivedDatasetFileTags(int adfId) {
        ArchivedDatasetFilePermissionTagDao adfDao = (ArchivedDatasetFilePermissionTagDao) SpringServletAccess.getApplicationContext(context).getBean("archivedDatasetFilePermissionTagDao");
        List<ArchivedDatasetFilePermissionTag> adfTags = adfDao.findAllByArchivedDatasetFileId(adfId);
        return adfTags;
    }

    private boolean hasAccessPermission(ArchivedDatasetFileBean asdfBean,  List<String> permissionTagsList, String originatingPage) {
        List<ArchivedDatasetFilePermissionTag> adfTags = getArchivedDatasetFileTags(asdfBean.getId());

        for (ArchivedDatasetFilePermissionTag adfTag : adfTags) {
            if (!permissionTagsList.contains(adfTag.getPermissionTagId())) {
                request.setAttribute("originatingPage", originatingPage);
                forwardPage(Page.NO_ACCESS);
                return false;
            }
        }
        return true;
    }

}

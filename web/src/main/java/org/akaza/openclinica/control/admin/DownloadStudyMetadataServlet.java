/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.extract.odm.MetaDataReportBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.logic.odmExport.MetadataUnit;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ywang
 * 
 */
public class DownloadStudyMetadataServlet extends SecureController {
    public static String STUDY_ID = "studyId";

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studyId = fp.getInt(STUDY_ID);
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) sdao.findByPK(studyId);
        String studyName = "";
        if (study.getParentStudyId() > 0) {
            studyName += ((StudyBean) sdao.findByPK(study.getParentStudyId())).getName() + "_";
        }
        studyName += study.getName();
        MetadataUnit mdc = new MetadataUnit(sm.getDataSource(), study, 0);
        mdc.collectOdmStudy();
        MetaDataReportBean meta = new MetaDataReportBean(mdc.getOdmStudy());
        meta.addNodeStudy(Boolean.FALSE);
        String creationTime = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
        String fileName = studyName + "_" + creationTime + ".txt";
        String filePath = SQLInitServlet.getField("filePath") + "studyMeta" + File.separator + study.getId() + File.separator + creationTime + File.separator;
        if (Utils.createZipFile(fileName, filePath, meta.getXmlOutput())) {
            response.setContentType("application/zip");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + ".zip\";");
            response.setHeader("Pragma", "public");
            request.setAttribute("generate", filePath + fileName + ".zip");
            response.setHeader("Pragma", "public");
            Page finalTarget = Page.EXPORT_DATA_CUSTOM;
            finalTarget.setFileName("/WEB-INF/jsp/extract/generatedFileDataset.jsp");
            forwardPage(finalTarget);
        } else {
            addPageMessage(respage.getString("metadata_unavailable_see_log"));
            forwardPage(Page.VIEW_FULL_STUDY);
        }
    }
}
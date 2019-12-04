/*
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.util.ArrayList;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jxu
 *
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("serial")
public class BatchCRFMigrationServlet extends SecureController {

    private static String CRF_ID = "crfId";
    private static String CRF = "crf";

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);

        ArrayList<CRFVersionBean> crfVersionList = null;
        ArrayList<FormLayoutBean> formLayoutList = null;
        ArrayList<StudyEventDefinitionBean> eventList = null;
        ArrayList<Study> siteList = null;

        // checks which module the requests are from, manage or admin
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfId = fp.getInt(CRF_ID);
        if (crfId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"));
            forwardPage(Page.CRF_LIST);
        } else {
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            // CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
            FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) cdao.findByPK(crfId);
            request.setAttribute("crfName", crf.getName());
            // ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) vdao.findAllByCRF(crfId);
            ArrayList<FormLayoutBean> formLayouts = (ArrayList<FormLayoutBean>) fldao.findAllByCRF(crfId);

            crfVersionList = new ArrayList<CRFVersionBean>();
            formLayoutList = new ArrayList<FormLayoutBean>();

            for (FormLayoutBean version : formLayouts) {
                if (version.getStatus().isAvailable())
                    formLayoutList.add(version);
            }
            // for (CRFVersionBean version : versions) {
            // if (version.getStatus().isAvailable())
            // crfVersionList.add(version);
            // }
            // crf.setVersions(crfVersionList);
            crf.setFormLayouts(formLayoutList);

            ArrayList<Study> listOfSites = (ArrayList<Study>) getStudyDao().findAllByParent(currentStudy.getStudyId());
            siteList = new ArrayList<Study>();
            Study studyBean = new Study();
            studyBean.setOc_oid(currentStudy.getOc_oid());
            studyBean.setName(resterm.getString("Study_Level_Subjects_Only"));
            siteList.add(studyBean);
            for (Study s : listOfSites) {
                if (s.getStatus().isAvailable()) {
                    siteList.add(s);
                }
            }

            ArrayList<StudyEventDefinitionBean> listOfDefn = seddao().findAllByStudy(currentStudy);
            eventList = new ArrayList<StudyEventDefinitionBean>();
            for (StudyEventDefinitionBean d : listOfDefn) {
                if (d.getStatus().isAvailable()) {
                    eventList.add(d);
                }
            }

            // if coming from change crf version -> display message
            String crfVersionChangeMsg = fp.getString("isFromCRFVersionBatchChange");
            if (crfVersionChangeMsg != null && !crfVersionChangeMsg.equals("")) {
                addPageMessage(crfVersionChangeMsg);
            }

            request.setAttribute("study", currentStudy);
            request.setAttribute("siteList", siteList);
            request.setAttribute("eventList", eventList);
            request.setAttribute(CRF, crf);
            forwardPage(Page.BATCH_CRF_MIGRATION);

        }
    }

    @SuppressWarnings("rawtypes")
    private StudyEventDefinitionDAO seddao() {
        return new StudyEventDefinitionDAO(sm.getDataSource());
    }

}

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class InitUpdateCRFServlet extends SecureController {

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

        boolean isStudyDirectorInParent = false;
        if (currentStudy.getParentStudyId() > 0) {
            logger.info("2222");
            Role r = ub.getRoleByStudy(currentStudy.getParentStudyId()).getRole();
            if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.ADMIN)) {
                isStudyDirectorInParent = true;
            }
        }

        // get current studyid
        int studyId = currentStudy.getId();

        if (ub.hasRoleInStudy(studyId)) {
            Role r = ub.getRoleByStudy(studyId).getRole();
            if (isStudyDirectorInParent || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.ADMIN)) {
                return;
            }
        }

        addPageMessage(respage.getString("you_not_have_permission_to_update_a_CRF") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.CRF_LIST_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);

        setToPanel(resword.getString("create_CRF"), respage.getString("br_create_new_CRF_entering"));

        setToPanel(resword.getString("create_CRF_version"), respage.getString("br_create_new_CRF_uploading"));
        setToPanel(resword.getString("revise_CRF_version"), respage.getString("br_if_you_owner_CRF_version"));
        setToPanel(resword.getString("CRF_spreadsheet_template"), respage.getString("br_download_blank_CRF_spreadsheet_from"));
        setToPanel(resword.getString("example_CRF_br_spreadsheets"), respage.getString("br_download_example_CRF_instructions_from"));

        FormProcessor fp = new FormProcessor(request);

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfId = fp.getInt(CRF_ID);
        if (crfId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_version_to_update"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) cdao.findByPK(crfId);
            if(!ub.isSysAdmin() && (crf.getOwnerId() != ub.getId())){
                addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                        + " " + respage.getString("change_active_study_or_contact"));
                forwardPage(Page.MENU_SERVLET);
                return;
            } else {
                session.setAttribute(CRF, crf);
                forwardPage(Page.UPDATE_CRF);
            }

        }
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}

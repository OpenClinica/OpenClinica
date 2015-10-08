/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Prepares to creat a new CRF Version
 * 
 * @author jxu
 */
public class InitCreateCRFVersionServlet extends SecureController {
    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (!request.getParameter(MODULE).equals("admin")
                && (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR))) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");
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

        String idString = request.getParameter("crfId");
        /*
         * now that we have automated the choice of crf id, we need to get it from someplace else besides the
         * request...this is throwing off the generation of filenames and other processes downstream, tbh 06/2008
         */
        String name = request.getParameter("name");
        logger.info("*** ^^^ *** crf id:" + idString);

        // checks which module the requests are from
        String module = request.getParameter(MODULE);
        request.setAttribute(MODULE, module);
        session.setAttribute("xformEnabled", CoreResources.getField("xform.enabled"));

        if (StringUtil.isBlank(idString) || StringUtil.isBlank(name)) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_add_new_version_for"));
            forwardPage(Page.CRF_LIST);
        } else {
            // crf id
            int crfId = Integer.valueOf(idString.trim()).intValue();
            CRFVersionBean version = new CRFVersionBean();
            version.setCrfId(crfId);
            session.setAttribute("version", version);
            request.setAttribute("crfName", name);
            request.setAttribute("CrfId", new Integer(crfId));
            forwardPage(Page.CREATE_CRF_VERSION);
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

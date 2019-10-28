/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 *
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ViewCRFVersionServlet extends SecureController {
    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        // CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        // int crfVersionId = fp.getInt("id");
        int formLayoutId = fp.getInt("id");

        if (formLayoutId == 0) {
            addPageMessage(respage.getString("please_choose_a_crf_to_view_details"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            // CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(formLayoutId);
            // tbh
            CRFDAO crfdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) crfdao.findByPK(formLayout.getCrfId());
            CRFVersionMetadataUtil metadataUtil = new CRFVersionMetadataUtil(sm.getDataSource());
            ArrayList<SectionBean> sections = metadataUtil.retrieveFormMetadata(formLayout);
            request.setAttribute("sections", sections);
            request.setAttribute("version", formLayout);
            // tbh
            request.setAttribute("crfname", crf.getName());
            // tbh
            forwardPage(Page.VIEW_CRF_VERSION);

        }
    }

}

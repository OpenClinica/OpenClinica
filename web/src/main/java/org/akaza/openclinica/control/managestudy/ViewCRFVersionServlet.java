/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
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

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfVersionId = fp.getInt("id");

        if (crfVersionId == 0) {
            addPageMessage(respage.getString("please_choose_a_crf_to_view_details"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            // tbh
            CRFDAO crfdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) crfdao.findByPK(version.getCrfId());
            // tbh, 102007
            SectionDAO sdao = new SectionDAO(sm.getDataSource());
            ItemGroupDAO igdao = new ItemGroupDAO(sm.getDataSource());
            ItemGroupMetadataDAO igmdao = new ItemGroupMetadataDAO(sm.getDataSource());
            ArrayList sections = (ArrayList) sdao.findByVersionId(version.getId());
            HashMap versionMap = new HashMap();
            for (int i = 0; i < sections.size(); i++) {
                SectionBean section = (SectionBean) sections.get(i);
                versionMap.put(new Integer(section.getId()), section.getItems());
                // YW 08-21-2007, add group metadata
                ArrayList<ItemGroupBean> igs = (ArrayList<ItemGroupBean>) igdao.findGroupBySectionId(section.getId());
                for (int j = 0; j < igs.size(); ++j) {
                    ArrayList<ItemGroupMetadataBean> igms =
                        (ArrayList<ItemGroupMetadataBean>) igmdao.findMetaByGroupAndSection(igs.get(j).getId(), section.getCRFVersionId(), section.getId());
                    if (!igms.isEmpty()) {
                        // Note, the following logic has been adapted here -
                        // "for a given crf version,
                        // all the items in the same group have the same group
                        // metadata
                        // so we can get one of them and set metadata for the
                        // group"
                        igs.get(j).setMeta(igms.get(0));
                        igs.get(j).setItemGroupMetaBeans(igms);
                    }
                }
                ((SectionBean) sections.get(i)).setGroups(igs);
                // YW >>
            }
            ArrayList items = idao.findAllItemsByVersionId(version.getId());
            // YW 08-22-2007, if this crf_version_id doesn't exist in
            // item_group_metadata table,
            // items in this crf_version will not exist in item_group_metadata,
            // then different query will be used
            if (igmdao.versionIncluded(crfVersionId)) {
                for (int i = 0; i < items.size(); i++) {
                    ItemBean item = (ItemBean) items.get(i);
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionId(item.getId(), version.getId());

                    item.setItemMeta(ifm);
                    // logger.info("option******" +
                    // ifm.getResponseSet().getOptions().size());
                    ArrayList its = (ArrayList) versionMap.get(new Integer(ifm.getSectionId()));
                    its.add(item);
                }
            } else {
                for (int i = 0; i < items.size(); i++) {
                    ItemBean item = (ItemBean) items.get(i);
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionIdNotInIGM(item.getId(), version.getId());

                    item.setItemMeta(ifm);
                    // logger.info("option******" +
                    // ifm.getResponseSet().getOptions().size());
                    ArrayList its = (ArrayList) versionMap.get(new Integer(ifm.getSectionId()));
                    its.add(item);
                }
            }

            for (int i = 0; i < sections.size(); i++) {
                SectionBean section = (SectionBean) sections.get(i);
                section.setItems((ArrayList) versionMap.get(new Integer(section.getId())));
            }
            request.setAttribute("sections", sections);
            request.setAttribute("version", version);
            // tbh
            request.setAttribute("crfname", crf.getName());
            // tbh
            forwardPage(Page.VIEW_CRF_VERSION);

        }
    }

}

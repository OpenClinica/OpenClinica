/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author jxu
 *
 * Views selected items for creating dataset, aslo allow user to de-select or
 * select all items in a study
 */
public class ViewSelectedServlet extends SecureController {

    Locale locale;

    // < ResourceBundlerestext,resexception,respage;
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
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    /*
     * setup study groups, tbh, added july 2007 FIXME in general a repeated set
     * of code -- need to create a superclass which will contain this class, tbh
     */
    public void setUpStudyGroups() {
        ArrayList sgclasses = (ArrayList) session.getAttribute("allSelectedGroups");
        if (sgclasses == null || sgclasses.size() == 0) {
            StudyGroupClassDAO sgclassdao = new StudyGroupClassDAO(sm.getDataSource());
            Study theStudy = (Study) getStudyBuildService().getPublicStudy(sm.getUserBean().getActiveStudyId());
            sgclasses = sgclassdao.findAllActiveByStudy(theStudy);
        }
        session.setAttribute("allSelectedGroups", sgclasses);
        session.setAttribute("numberOfStudyGroups", sgclasses.size());
        request.setAttribute("allSelectedGroups", sgclasses);
    }

    @Override
    public void processRequest() throws Exception {

        DatasetBean db = (DatasetBean) session.getAttribute("newDataset");
        HashMap events = (HashMap) session.getAttribute(CreateDatasetServlet.EVENTS_FOR_CREATE_DATASET);
        if (events == null) {
            events = new HashMap();
        }
        request.setAttribute("eventlist", events);

        CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO imfdao = new ItemFormMetadataDAO(sm.getDataSource());
        ArrayList ids = CreateDatasetServlet.allSedItemIdsInStudy(events, crfdao, idao);// new
                                                                                        // ArrayList();
        // ArrayList allItemsInStudy = EditSelectedServlet.selectAll(events,
        // crfdao, idao);
        // for (int j = 0; j < allItemsInStudy.size(); j++) {
        // ItemBean item = (ItemBean) allItemsInStudy.get(j);
        // Integer itemId = new Integer(item.getId());
        // if (!ids.contains(itemId)) {
        // ids.add(itemId);
        // }
        // }
        session.setAttribute("numberOfStudyItems", new Integer(ids.size()).toString());

        ArrayList items = new ArrayList();
        if (db == null || db.getItemIds().size() == 0) {
            session.setAttribute("allSelectedItems", items);
            setUpStudyGroups();// FIXME can it be that we have no selected
            // items and
            // some selected groups? tbh
            forwardPage(Page.CREATE_DATASET_VIEW_SELECTED);
            return;
        }

        items = getAllSelected(db, idao, imfdao);

        session.setAttribute("allSelectedItems", items);

        FormProcessor fp = new FormProcessor(request);
        String status = fp.getString("status");
        if (!StringUtil.isBlank(status) && "html".equalsIgnoreCase(status)) {
            forwardPage(Page.CREATE_DATASET_VIEW_SELECTED_HTML);
        } else {
            setUpStudyGroups();
            forwardPage(Page.CREATE_DATASET_VIEW_SELECTED);
        }

    }

    public static ArrayList getAllSelected(DatasetBean db, ItemDAO idao, ItemFormMetadataDAO imfdao) throws Exception {
        ArrayList items = new ArrayList();
        // ArrayList itemIds = db.getItemIds();
        ArrayList itemDefCrfs = db.getItemDefCrf();

        for (int i = 0; i < itemDefCrfs.size(); i++) {
            ItemBean item = (ItemBean) itemDefCrfs.get(i);
            item.setSelected(true);
            ArrayList metas = imfdao.findAllByItemId(item.getId());
            for (int h = 0; h < metas.size(); h++) {
                ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(h);
                // logger.info("group name found:
                // "+ifmb.getGroupLabel());
            }
            item.setItemMetas(metas);
            items.add(item);
        }

        return items;

    }

}

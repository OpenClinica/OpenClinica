/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * @author jxu
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class EditSelectedServlet extends SecureController {

    Locale locale;

    // < ResourceBundlerespage,resexception;

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
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
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    /*
     * TODO this function exists in four different places... needs to be added to an additional superclass for Submit Data Control Servlets, tbh July 2007
     */
    public void setUpStudyGroups() {
        ArrayList sgclasses = (ArrayList) session.getAttribute("allSelectedGroups");
        if (sgclasses == null || sgclasses.size() == 0) {
            StudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyGroupClassDAO sgclassdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyBean theStudy = (StudyBean) studydao.findByPK(sm.getUserBean().getActiveStudyId());
            sgclasses = sgclassdao.findAllActiveByStudy(theStudy);
        }
        session.setAttribute("allSelectedGroups", sgclasses);
        request.setAttribute("allSelectedGroups", sgclasses);
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        boolean selectAll = fp.getBoolean("all");
        boolean selectAllItemsGroupsAttrs = fp.getBoolean("allAttrsAndItems");
        // BWP 3095: Only show a "select all items" like on a side info panel if
        // it
        // is not part of the EditSelected-related JSP>>
        request.setAttribute("EditSelectedSubmitted", true);
        // <<
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        // CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        ItemFormMetadataDAO imfdao = new ItemFormMetadataDAO(sm.getDataSource());
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());

        DatasetBean db = (DatasetBean) session.getAttribute("newDataset");
        if (db == null) {
            db = new DatasetBean();
            session.setAttribute("newDataset", db);
        }
        // << tbh
        // HashMap eventlist = (HashMap) request.getAttribute("eventlist");
        // if (eventlist == null) {
        // System.out.println("TTTTT found the second hashmap!");
        HashMap eventlist = (LinkedHashMap) session.getAttribute("eventsForCreateDataset");
        // }
        ArrayList<String> ids = CreateDatasetServlet.allSedItemIdsInStudy(eventlist, crfdao, idao);
        // >> tbh 11/09, need to fill in a session variable
        if (selectAll) {
            logger.info("select all..........");
            db = selectAll(db);

            MessageFormat msg = new MessageFormat("");
            msg.setLocale(locale);
            msg.applyPattern(respage.getString("choose_include_all_items_dataset"));
            Object[] arguments = { ids.size() };
            addPageMessage(msg.format(arguments));

            // addPageMessage("You choose to include all items in current study
            // for the dataset, " +db.getItemIds().size() + " items total.");
        }// end of if selectAll

        if (selectAllItemsGroupsAttrs) {
            logger.info("select everything....");
            db = selectAll(db);
            db.setShowCRFcompletionDate(true);
            db.setShowCRFinterviewerDate(true);
            db.setShowCRFinterviewerName(true);
            db.setShowCRFstatus(true);
            db.setShowCRFversion(true);

            db.setShowEventEnd(true);
            db.setShowEventEndTime(true);
            db.setShowEventLocation(true);
            db.setShowEventStart(true);
            db.setShowEventStartTime(true);
            db.setShowEventStatus(true);

            db.setShowSubjectAgeAtEvent(true);
            db.setShowSubjectDob(true);
            db.setShowSubjectGender(true);
            db.setShowSubjectGroupInformation(true);
            db.setShowSubjectStatus(true);
            db.setShowSubjectUniqueIdentifier(true);

            // select all groups
            ArrayList sgclasses = (ArrayList) session.getAttribute("allSelectedGroups");
            //
            ArrayList newsgclasses = new ArrayList();
            StudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyGroupClassDAO sgclassdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyBean theStudy = (StudyBean) studydao.findByPK(sm.getUserBean().getActiveStudyId());
            sgclasses = sgclassdao.findAllActiveByStudy(theStudy);
            for (int i = 0; i < sgclasses.size(); i++) {
                StudyGroupClassBean sgclass = (StudyGroupClassBean) sgclasses.get(i);
                sgclass.setSelected(true);
                newsgclasses.add(sgclass);
            }
            session.setAttribute("allSelectedGroups", newsgclasses);
            request.setAttribute("allSelectedGroups", newsgclasses);
        }

        session.setAttribute("newDataset", db);

        HashMap events = (HashMap) session.getAttribute(CreateDatasetServlet.EVENTS_FOR_CREATE_DATASET);
        if (events == null) {
            events = new HashMap();
        }
        ArrayList allSelectItems = selectAll ? selectAll(events, crfdao, idao) : ViewSelectedServlet.getAllSelected(db, idao, imfdao);
        // >> tbh
        session.setAttribute("numberOfStudyItems", new Integer(ids.size()).toString());
        // << tbh 11/2009
        session.setAttribute("allSelectedItems", allSelectItems);
        setUpStudyGroups();
        forwardPage(Page.CREATE_DATASET_VIEW_SELECTED);

    }

    public DatasetBean selectAll(DatasetBean db) {
        HashMap events = (HashMap) session.getAttribute(CreateDatasetServlet.EVENTS_FOR_CREATE_DATASET);
        if (events == null) {
            events = new HashMap();
        }
        request.setAttribute("eventlist", events);

        ItemDAO idao = new ItemDAO(sm.getDataSource());
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        ArrayList allItems = selectAll(events, crfdao, idao);
        Iterator it = events.keySet().iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) it.next();
            if (!db.getEventIds().contains(new Integer(sed.getId()))) {
                db.getEventIds().add(new Integer(sed.getId()));
            }
        }

        // for (int j = 0; j < allItems.size(); j++) {
        // ItemBean item = (ItemBean) allItems.get(j);
        // ArrayList ids = db.getItemIds();
        // ArrayList itemDefCrfs = db.getItemDefCrf();
        // Integer itemId = new Integer(item.getId());
        // if (!ids.contains(itemId)) {
        // ids.add(itemId);
        // itemDefCrfs.add(item);
        // }
        // }
        db.getItemDefCrf().clear();
        db.setItemDefCrf(allItems);
        return db;

    }

    /**
     * Finds all the items in a study giving all events in the study
     *
     * @param events
     * @return
     */
    public static ArrayList selectAll(HashMap events, CRFDAO crfdao, ItemDAO idao) {

        ArrayList allItems = new ArrayList();

        Iterator it = events.keySet().iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) it.next();
            ArrayList crfs = (ArrayList) crfdao.findAllActiveByDefinition(sed);
            for (int i = 0; i < crfs.size(); i++) {
                CRFBean crf = (CRFBean) crfs.get(i);
                ArrayList items = idao.findAllActiveByCRF(crf);
                for (int j = 0; j < items.size(); j++) {
                    ItemBean item = (ItemBean) items.get(j);
                    item.setCrfName(crf.getName());
                    item.setDefName(sed.getName());
                    item.setDefId(sed.getId());
                    item.setSelected(true);
                }
                allItems.addAll(items);
            }
        }
        return allItems;

    }
}

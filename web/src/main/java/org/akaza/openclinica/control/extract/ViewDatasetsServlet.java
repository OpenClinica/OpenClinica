/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DatasetRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

import java.util.*;

/**
 * ViewDatasetsServlet.java, the view datasets function accessed from the
 * extract datasets main page.
 *
 * @author thickerson
 *
 *
 *
 */
public class ViewDatasetsServlet extends SecureController {

    Locale locale;

    // < ResourceBundleresword,restext,respage,resexception;

    public static String getLink(int dsId) {
        return "ViewDatasets?action=details&datasetId=" + dsId;
    }

    @Override
    public void processRequest() throws Exception {
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        String action = request.getParameter("action");
        resetPanel();
        request.setAttribute(STUDY_INFO_PANEL, panel);
        // YW, 2-15-2008 <<
        session.removeAttribute("allSelectedItems");
        session.removeAttribute("allSelectedGroups");
        session.removeAttribute("allItems");
        session.removeAttribute("newDataset");
        // YW >>
        if (StringUtil.isBlank(action)) {
            // YW 08-2008 << 2529 fix
            StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudyBean studyWithEventDefinitions = currentStudy;
            if (currentStudy.getParentStudyId() > 0) {
                studyWithEventDefinitions = new StudyBean();
                studyWithEventDefinitions.setId(currentStudy.getParentStudyId());

            }
            ArrayList seds = seddao.findAllActiveByStudy(studyWithEventDefinitions);
            CRFDAO crfdao = new CRFDAO(sm.getDataSource());
            HashMap events = new LinkedHashMap();
            for (int i = 0; i < seds.size(); i++) {
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seds.get(i);
                ArrayList crfs = (ArrayList) crfdao.findAllActiveByDefinition(sed);
                if (!crfs.isEmpty()) {
                    events.put(sed, crfs);
                }
            }
            session.setAttribute("eventsForCreateDataset", events);
            // YW >>

            FormProcessor fp = new FormProcessor(request);

            EntityBeanTable table = fp.getEntityBeanTable();
            ArrayList datasets = new ArrayList();
//            if (ub.isSysAdmin()) {
//                datasets = dsdao.findAllByStudyIdAdmin(currentStudy.getId());
//            } else {
            datasets = dsdao.findAllByStudyId(currentStudy.getId());
//            }

            ArrayList datasetRows = DatasetRow.generateRowsFromBeans(datasets);

            String[] columns =
                { resword.getString("dataset_name"), resword.getString("description"), resword.getString("created_by"), resword.getString("created_date"),
                    resword.getString("status"), resword.getString("actions") };
            table.setColumns(new ArrayList(Arrays.asList(columns)));
            table.hideColumnLink(5);
            table.addLink(resword.getString("show_only_my_datasets"), "ViewDatasets?action=owner&ownerId=" + ub.getId());
            table.addLink(resword.getString("create_dataset"), "CreateDataset");
            table.setQuery("ViewDatasets", new HashMap());
            table.setRows(datasetRows);
            table.computeDisplay();

            request.setAttribute("table", table);
            // this is the old code that the tabling code replaced:
            // ArrayList datasets = (ArrayList)dsdao.findAll();
            // request.setAttribute("datasets", datasets);
            forwardPage(Page.VIEW_DATASETS);
        } else {
            if ("owner".equalsIgnoreCase(action)) {
                FormProcessor fp = new FormProcessor(request);
                int ownerId = fp.getInt("ownerId");
                EntityBeanTable table = fp.getEntityBeanTable();

                ArrayList datasets = (ArrayList) dsdao.findByOwnerId(ownerId, currentStudy.getId());

                /*
                 * if (datasets.isEmpty()) {
                 * forwardPage(Page.VIEW_EMPTY_DATASETS); } else {
                 */

                ArrayList datasetRows = DatasetRow.generateRowsFromBeans(datasets);
                String[] columns =
                    { resword.getString("dataset_name"), resword.getString("description"), resword.getString("created_by"), resword.getString("created_date"),
                        resword.getString("status"), resword.getString("actions") };
                table.setColumns(new ArrayList(Arrays.asList(columns)));
                table.hideColumnLink(5);
                table.addLink(resword.getString("show_all_datasets"), "ViewDatasets");
                table.addLink(resword.getString("create_dataset"), "CreateDataset");
                table.setQuery("ViewDatasets?action=owner&ownerId=" + ub.getId(), new HashMap());
                table.setRows(datasetRows);
                table.computeDisplay();
                request.setAttribute("table", table);
                // this is the old code:

                // ArrayList datasets = (ArrayList)dsdao.findByOwnerId(ownerId);
                // request.setAttribute("datasets", datasets);
                forwardPage(Page.VIEW_DATASETS);
                // }
            } else if ("details".equalsIgnoreCase(action)) {
                FormProcessor fp = new FormProcessor(request);
                int datasetId = fp.getInt("datasetId");

                DatasetBean db = initializeAttributes(datasetId);
                StudyDAO sdao = new StudyDAO(sm.getDataSource());
                StudyBean study = (StudyBean)sdao.findByPK(db.getStudyId());

                if (study.getId() != currentStudy.getId() && study.getParentStudyId() != currentStudy.getId()) {
                    addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                            + " " + respage.getString("change_active_study_or_contact"));
                    forwardPage(Page.MENU_SERVLET);
                    return;
                }

                /*
                 * EntityBeanTable table = fp.getEntityBeanTable(); ArrayList
                 * datasetRows = DatasetRow.generateRowFromBean(db); String[]
                 * columns = { "Dataset Name", "Description", "Created By",
                 * "Created Date", "Status", "Actions" }; table.setColumns(new
                 * ArrayList(Arrays.asList(columns))); table.hideColumnLink(5);
                 * table.setQuery("ViewDatasets", new HashMap());
                 * table.setRows(datasetRows); table.computeDisplay();
                 * request.setAttribute("table", table);
                 */
                request.setAttribute("dataset", db);

                forwardPage(Page.VIEW_DATASET_DETAILS);
            }
        }

    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        if  (!( currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.RESEARCHASSISTANT2) ) ) {
            return;
        }
    
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    /**
     * Initialize data of a DatasetBean and set session attributes for
     * displaying selected data of this DatasetBean
     *
     * @param db
     * @return
     *
     */
    // @author ywang (Feb, 2008)
    public DatasetBean initializeAttributes(int datasetId) {
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        DatasetBean db = dsdao.initialDatasetData(datasetId);
        session.setAttribute("newDataset", db);
        session.setAttribute("allItems", db.getItemDefCrf().clone());
        session.setAttribute("allSelectedItems", db.getItemDefCrf().clone());
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
        StudyDAO studydao = new StudyDAO(sm.getDataSource());
        StudyBean theStudy = (StudyBean) studydao.findByPK(sm.getUserBean().getActiveStudyId());
        ArrayList<StudyGroupClassBean> allSelectedGroups = sgcdao.findAllActiveByStudy(theStudy);
        ArrayList<Integer> selectedSubjectGroupIds = db.getSubjectGroupIds();
        if (selectedSubjectGroupIds != null && allSelectedGroups != null) {
            for (Integer id : selectedSubjectGroupIds) {
                for (int i = 0; i < allSelectedGroups.size(); ++i) {
                    if (allSelectedGroups.get(i).getId() == id) {
                        allSelectedGroups.get(i).setSelected(true);
                        break;
                    }
                }
            }
        }
        session.setAttribute("allSelectedGroups", allSelectedGroups);

        return db;
    }
}

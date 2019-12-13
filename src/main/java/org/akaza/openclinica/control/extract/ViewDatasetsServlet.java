/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.bean.DatasetRow;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

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
            Study studyWithEventDefinitions = currentStudy;
            if (currentStudy.isSite()) {
                studyWithEventDefinitions = currentStudy.getStudy();
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
            datasets = dsdao.findAllByStudyId(currentStudy.getStudyId());
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

                ArrayList datasets = (ArrayList) dsdao.findByOwnerId(ownerId, currentStudy.getStudyId());

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
                Study study = (Study)getStudyDao().findByPK(db.getStudyId());

                if (study != null && study.getStudyId() != currentStudy.getStudyId() && study.checkAndGetParentStudyId() != currentStudy.getStudyId()) {
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
        String prevSchema = (String) request.getAttribute("requestSchema");
        request.setAttribute("requestSchema", "public");
        Study theStudy = (Study) getStudyBuildService().getPublicStudy(sm.getUserBean().getActiveStudyId());
        request.setAttribute("requestSchema", prevSchema);
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

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author thickerson
 *
 *
 */
public class EditDatasetServlet extends SecureController {

    public static String getLink(int dsId) {
        return "EditDataset?dsId=" + dsId;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        int dsId = fp.getInt("dsId");
        DatasetBean dataset = initializeAttributes(dsId);

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean)sdao.findByPK(dataset.getStudyId());
        // Checking if user has permission to access the current study/site
        checkRoleByUserAndStudy(ub, study, sdao);

        // Checking the dataset belongs to current study or a site of current study
        if (study.getId() != currentStudy.getId() && study.getParentStudyId() != currentStudy.getId()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }

        if((currentRole.isMonitor() || currentRole.isInvestigator()) && dataset.getOwnerId() != ub.getId()){
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }


        HashMap events = (LinkedHashMap) session.getAttribute("eventsForCreateDataset");
        // << tbh
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());

        // >> tbh 11/2009
        if (events == null || events.isEmpty()) {
            events = new LinkedHashMap();
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());

            StudyBean studyWithEventDefinitions = currentStudy;
            if (currentStudy.getParentStudyId() > 0) {
                studyWithEventDefinitions = new StudyBean();
                studyWithEventDefinitions.setId(currentStudy.getParentStudyId());

            }
            ArrayList seds = seddao.findAllActiveByStudy(studyWithEventDefinitions);
            for (int i = 0; i < seds.size(); i++) {
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seds.get(i);
                ArrayList crfs = (ArrayList) crfdao.findAllActiveByDefinition(sed);
                if (!crfs.isEmpty()) {
                    events.put(sed, crfs);
                }
            }
            if (events.isEmpty()) {
                addPageMessage(respage.getString("not_have_study_definitions_assigned"));
                forwardPage(Page.VIEW_DATASETS);
            } else {
                request.setAttribute("eventlist", events);
                session.setAttribute("eventsForCreateDataset", events);
            }
        }
        /*
         * if ("validate".equalsIgnoreCase(action)) { //check name, description, status for right now Validator v = new Validator(request);
         * v.addValidation("dsName", Validator.NO_BLANKS); v.addValidation("dsDesc", Validator.NO_BLANKS); v.addValidation("dsStatus", Validator.IS_VALID_TERM,
         * TermType.STATUS); v.addValidation("dsName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
         * v.addValidation("dsDesc", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000); //what else to validate?
         * HashMap errors = v.validate(); if (!StringUtil.isBlank(fp.getString("dsName"))) { //logger.info("dsName" + fp.getString("dsName")); DatasetDAO dsdao
         * = new DatasetDAO(sm.getDataSource()); DatasetBean dsBean = (DatasetBean) dsdao.findByNameAndStudy(fp.getString("dsName").trim(), currentStudy); if
         * (dsBean.getId() > 0 && (dsBean.getId() != fp.getInt("dsId"))) { Validator.addError(errors, "dsName",
         * restext.getString("dataset_name_used_by_another_choose_unique")); } } if (!errors.isEmpty()) { String fieldNames[] = { "dsName", "dsDesc" };
         * fp.setCurrentStringValuesAsPreset(fieldNames); fp.addPresetValue("dsStatus", fp.getInt("dsStatus"));
         * addPageMessage(restext.getString("errors_in_update_see_below")); setInputMessages(errors); setPresetValues(fp.getPresetValues()); //TODO determine if
         * this is necessary //int dsId = fp.getInt("dsId"); //DatasetDAO dsDAO = new DatasetDAO(sm.getDataSource()); //DatasetBean showDataset = (DatasetBean)
         * dsDAO.findByPK(dsId); request.setAttribute("dataset", dataset); //maybe just set the above to the session? request.setAttribute("statuses",
         * getStatuses()); forwardPage(Page.EDIT_DATASET); } else { dataset.setName(fp.getString("dsName")); dataset.setDescription(fp.getString("dsDesc"));
         * dataset.setStatus(Status.get(fp.getInt("dsStatus"))); dataset.setUpdater(ub); //dataset.setUpdaterId(ub.getId()); dsDAO.update(dataset);
         * addPageMessage(restext.getString("dataset_properties_updated")); //forward to view all datasets ArrayList datasets = (ArrayList)
         * dsDAO.findAllByStudyId(currentStudy.getId()); //changed from findAll() EntityBeanTable table = fp.getEntityBeanTable(); ArrayList datasetRows =
         * DatasetRow.generateRowsFromBeans(datasets); String[] columns = {resword.getString("dataset_name"), resword.getString("description"),
         * resword.getString("created_by"), resword.getString("created_date"), resword.getString("status"),resword.getString("actions")}; table.setColumns(new
         * ArrayList(Arrays.asList(columns))); table.hideColumnLink(5); table.setQuery("ViewDatasets", new HashMap()); table.setRows(datasetRows);
         * table.computeDisplay(); request.setAttribute("table", table); //request.setAttribute("datasets", datasets); forwardPage(Page.VIEW_DATASETS); } } else
         * {
         */
        request.setAttribute("dataset", dataset);
        request.setAttribute("statuses", getStatuses());
        forwardPage(Page.EDIT_DATASET);
        // }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }
        // TODO add a limit so that the owner can edit, no one else?
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    private ArrayList getStatuses() {
        Status statusesArray[] = { Status.AVAILABLE, Status.PENDING, Status.PRIVATE, Status.UNAVAILABLE };
        List statuses = Arrays.asList(statusesArray);
        return new ArrayList(statuses);
    }

    /**
     * Initialize data of a DatasetBean and set session attributes for displaying selected data of this DatasetBean
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

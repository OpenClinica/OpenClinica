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
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DatasetRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author thickerson
 *
 */
public class RestoreDatasetServlet extends SecureController {

    Locale locale;

    // < ResourceBundlerespage,resexception,resword;

    public static String getLink(int dsId) {
        return "RestoreDataset?dsId=" + dsId;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int dsId = fp.getInt("dsId");
        DatasetDAO dsDAO = new DatasetDAO(sm.getDataSource());
        DatasetBean dataset = (DatasetBean) dsDAO.findByPK(dsId);

        String action = request.getParameter("action");
        if (resword.getString("restore_this_dataset").equalsIgnoreCase(action)) {
            dataset.setStatus(Status.AVAILABLE);
            dsDAO.update(dataset);
            addPageMessage(respage.getString("dataset_has_been_succesfully_reinstated"));
            request.setAttribute("table", getDatasetTable());
            forwardPage(Page.VIEW_DATASETS);
        } else if (resword.getString("cancel").equalsIgnoreCase(action)) {

            request.setAttribute("table", getDatasetTable());
            forwardPage(Page.VIEW_DATASETS);
        } else {
            request.setAttribute("dataset", dataset);
            forwardPage(Page.RESTORE_DATASET);
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;// TODO limit to owner only?
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_restore_dataset"), "1");

    }

    private EntityBeanTable getDatasetTable() {
        FormProcessor fp = new FormProcessor(request);

        EntityBeanTable table = fp.getEntityBeanTable();
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        ArrayList datasets = new ArrayList();
        // if (ub.isSysAdmin()) {
        // datasets =
        // (ArrayList)dsdao.findAllByStudyIdAdmin(currentStudy.getId());
        // } else {
        datasets = dsdao.findAllByStudyId(currentStudy.getId());
        // }

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
        return table;
    }

}

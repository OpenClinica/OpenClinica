/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DatasetRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * <P>
 * The main page for the extract datasets use case. Show five last datasets and
 * offers links for viewing all and viewing users' datasets, together with a
 * link for extracting datasets.
 * </P>
 *
 * @author thickerson
 *
 */
public class ExtractDatasetsMainServlet extends SecureController {

    public static final String PATH = "ExtractDatasetsMain";
    public static final String ARG_USER_ID = "userId";

    public static String getLink(int userId) {
        return PATH + '?' + ARG_USER_ID + '=' + userId;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        EntityBeanTable table = fp.getEntityBeanTable();

        ArrayList datasets = (ArrayList) dsdao.findTopFive(currentStudy);
        ArrayList datasetRows = DatasetRow.generateRowsFromBeans(datasets);

        String[] columns =
            { resword.getString("dataset_name"), resword.getString("description"), resword.getString("created_by"), resword.getString("created_date"),
                resword.getString("status"), resword.getString("actions") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(5);

        table.addLink(resword.getString("view_all"), "ViewDatasets");
        table.addLink(resword.getString("view_my_datasets"), "ViewDatasets?action=owner&ownerId=" + ub.getId());
        table.addLink(resword.getString("create_dataset"), "CreateDataset");
        table.setQuery("ExtractDatasetsMain", new HashMap());
        table.setRows(datasetRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        // the code above replaces the following lines:
        // DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        // ArrayList datasets = (ArrayList)dsdao.findTopFive();
        // request.setAttribute("datasets", datasets);
        resetPanel();

        request.setAttribute(STUDY_INFO_PANEL, panel);

        forwardPage(Page.EXTRACT_DATASETS_MAIN);
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) 
            || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }
}

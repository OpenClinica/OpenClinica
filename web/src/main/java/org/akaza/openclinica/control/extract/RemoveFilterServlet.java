/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.extract.FilterDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.FilterRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * <P>
 * The goal here is to provide a small servlet which will change a status from
 * 'available' to 'unavailable' so that it cannot be accessed.
 *
 * <P>
 * TODO define who can or can't remove a filter; creator only? anyone in the
 * project?
 *
 * @author thickerson
 *
 */
public class RemoveFilterServlet extends SecureController {

    Locale locale;
    // < ResourceBundleresmessage,restext,resword,resexception;

    public static final String PATH = "RemoveFilter";
    public static final String ARG_FILTER_ID = "filterId";

    public static String getLink(int filterId) {
        return PATH + '?' + ARG_FILTER_ID + '=' + filterId;
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int filterId = fp.getInt("filterId");
        FilterDAO fDAO = new FilterDAO(sm.getDataSource());
        FilterBean filter = (FilterBean) fDAO.findByPK(filterId);

        String action = request.getParameter("action");
        if (resword.getString("remove_this_filter").equalsIgnoreCase(action)) {
            filter.setStatus(Status.DELETED);
            fDAO.update(filter);
            addPageMessage(respage.getString("filter_removed_admin_can_access_and_reverse"));
            EntityBeanTable table = getFilterTable();
            request.setAttribute("table", table);

            forwardPage(Page.CREATE_FILTER_SCREEN_1);
        } else if (resword.getString("cancel").equalsIgnoreCase(action)) {
            EntityBeanTable table = getFilterTable();
            request.setAttribute("table", table);

            forwardPage(Page.CREATE_FILTER_SCREEN_1);
        } else {
            request.setAttribute("filter", filter);
            forwardPage(Page.REMOVE_FILTER);
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resmessage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);

        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    private EntityBeanTable getFilterTable() {
        FormProcessor fp = new FormProcessor(request);
        FilterDAO fdao = new FilterDAO(sm.getDataSource());
        EntityBeanTable table = fp.getEntityBeanTable();

        ArrayList filters = (ArrayList) fdao.findAll();
        // TODO make findAllByProject
        ArrayList filterRows = FilterRow.generateRowsFromBeans(filters);

        String[] columns =
            { resword.getString("filter_name"), resword.getString("description"), resword.getString("created_by"), resword.getString("created_date"),
                resword.getString("status"), resword.getString("actions") };

        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(5);
        table.setQuery("CreateFiltersOne", new HashMap());
        table.setRows(filterRows);
        table.computeDisplay();
        return table;
    }

}

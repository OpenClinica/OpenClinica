/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.UserAccountRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ListUserAccountsServlet extends SecureController {
    public static final String PATH = "ListUserAccounts";
    public static final String ARG_MESSAGE = "message";

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("you_may_not_perform_administrative_functions"));
            throw new InsufficientPermissionException(Page.ADMIN_SYSTEM_SERVLET, respage.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        EntityBeanTable table = fp.getEntityBeanTable();
        // table.setSortingIfNotExplicitlySet(1, false);

        ArrayList allUsers = getAllUsers(udao);
        setStudyNamesInStudyUserRoles(allUsers);
        ArrayList allUserRows = UserAccountRow.generateRowsFromBeans(allUsers);

        String[] columns =
            { resword.getString("user_name"), resword.getString("first_name"), resword.getString("last_name"), resword.getString("status"),
                resword.getString("actions") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(4);
        table.setQuery("ListUserAccounts", new HashMap());
        table.addLink(resword.getString("create_a_new_user"), "CreateUserAccount");

        table.setRows(allUserRows);
        table.computeDisplay();

        request.setAttribute("table", table);

        String message = fp.getString(ARG_MESSAGE, true);
        request.setAttribute(ARG_MESSAGE, message);
        request.setAttribute("siteRoleMap", Role.siteRoleMap);
        request.setAttribute("studyRoleMap", Role.studyRoleMap);

        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        if (allUsers.size() > 0) {
            setToPanel(resword.getString("users"), new Integer(allUsers.size()).toString());
        }

        forwardPage(Page.LIST_USER_ACCOUNTS);
    }

    private ArrayList getAllUsers(UserAccountDAO udao) {
        ArrayList result = (ArrayList) udao.findAll();
        return result;
    }

    /**
     * For each user, for each study user role, set the study user role's
     * studyName property.
     *
     * @param users
     *            The users to display in the table of users. Each element is a
     *            UserAccountBean.
     */
    private void setStudyNamesInStudyUserRoles(ArrayList users) {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        ArrayList allStudies = (ArrayList) sdao.findAll();
        HashMap studiesById = new HashMap();

        int i;
        for (i = 0; i < allStudies.size(); i++) {
            StudyBean sb = (StudyBean) allStudies.get(i);
            studiesById.put(new Integer(sb.getId()), sb);
        }

        for (i = 0; i < users.size(); i++) {
            UserAccountBean u = (UserAccountBean) users.get(i);
            ArrayList roles = u.getRoles();

            for (int j = 0; j < roles.size(); j++) {
                StudyUserRoleBean surb = (StudyUserRoleBean) roles.get(j);
                StudyBean sb = (StudyBean) studiesById.get(new Integer(surb.getStudyId()));
                if (sb != null) {
                    surb.setStudyName(sb.getName());
                    surb.setParentStudyId(sb.getParentStudyId());
                    // FR 2018-09-20: set parentStudyName
                    surb.setParentStudyName(sb.getParentStudyName());
                }
                roles.set(j, surb);
            }
            u.setRoles(roles);
            users.set(i, u);
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}

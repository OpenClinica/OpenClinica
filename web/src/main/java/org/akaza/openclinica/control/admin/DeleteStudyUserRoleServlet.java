/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.EntityAction;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

// allows both deletion and restoration of a study user role

public class DeleteStudyUserRoleServlet extends SecureController {
    public static final String PATH = "DeleteStudyUserRole";
    public static final String ARG_USERNAME = "userName";
    public static final String ARG_STUDYID = "studyId";
    public static final String ARG_ACTION = "action";

    public static String getLink(String userName, int studyId, EntityAction action) {
        return PATH + "?" + ARG_USERNAME + "=" + userName + "&" + ARG_STUDYID + "=" + studyId + "&" + ARG_ACTION + "=" + action.getId();
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());

        FormProcessor fp = new FormProcessor(request);
        int studyId = fp.getInt(ARG_STUDYID);
        String uName = fp.getString(ARG_USERNAME);
        UserAccountBean user = (UserAccountBean) udao.findByUserName(uName);
        techAdminProtect(user);
        
        int action = fp.getInt(ARG_ACTION);

        StudyUserRoleBean s = udao.findRoleByUserNameAndStudyId(uName, studyId);

        String message;
        if (!s.isActive()) {
            message = respage.getString("the_specified_user_role_not_exits_for_study");
        } else if (!EntityAction.contains(action)) {
            message = respage.getString("the_specified_action_is_invalid");
        } else if (!EntityAction.get(action).equals(EntityAction.DELETE) && !EntityAction.get(action).equals(EntityAction.RESTORE)) {
            message = respage.getString("the_specified_action_is_not_allowed");
        } else if (EntityAction.get(action).equals(EntityAction.RESTORE) && user.getStatus().equals(Status.DELETED)) {
            message = respage.getString("the_role_cannot_be_restored_since_user_deleted");
        } else {
            EntityAction desiredAction = EntityAction.get(action);

            if (desiredAction.equals(EntityAction.DELETE)) {
                s.setStatus(Status.DELETED);
                message = respage.getString("the_study_user_role_deleted");
            } else {
                s.setStatus(Status.AVAILABLE);
                message = respage.getString("the_study_user_role_restored");
            }

            s.setUpdater(ub);
            udao.updateStudyUserRole(s, uName);
        }

        addPageMessage(message);
        forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
    }

    // public void processRequest(HttpServletRequest request,
    // HttpServletResponse response)
    // throws OpenClinicaException {
    // session = request.getSession();
    // session.setMaxInactiveInterval(60 * 60 * 3);
    // logger.setLevel(Level.ALL);
    // UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
    // try {
    // String userName = request.getRemoteUser();
    //
    // sm = new SessionManager(ub, userName);
    // ub = sm.getUserBean();
    // if (logger.isLoggable(Level.INFO)) {
    // logger.info("user bean from DB" + ub.getName());
    // }
    //
    // SQLFactory factory = SQLFactory.getInstance();
    // UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
    //
    // FormProcessor fp = new FormProcessor(request);
    // int studyId = fp.getInt(ARG_STUDYID);
    // String uName = fp.getString(ARG_USERNAME);
    // int action = fp.getInt(ARG_ACTION);
    //
    // StudyUserRoleBean s = udao.findRoleByUserNameAndStudyId(uName, studyId);
    //
    // String message;
    // if (!s.isActive()) {
    // message = "The specified user role does not exist for the specified
    // study.";
    // }
    // else if (!EntityAction.contains(action)) {
    // message = "The specified action on the study user role is invalid.";
    // }
    // else if (!EntityAction.get(action).equals(EntityAction.DELETE)
    // && !EntityAction.get(action).equals(EntityAction.RESTORE)) {
    // message = "The specified action is not allowed.";
    // }
    // else {
    // EntityAction desiredAction = EntityAction.get(action);
    //
    // if (desiredAction.equals(EntityAction.DELETE)) {
    // s.setStatus(Status.DELETED);
    // message = "The study user role has been deleted.";
    // }
    // else {
    // s.setStatus(Status.AVAILABLE);
    // message = "The study user role has been restored.";
    // }
    //
    // udao.updateStudyUserRole(s, uName);
    // }
    //
    // request.setAttribute("message", message);
    // forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET, request, response);
    // } catch (Exception e) {
    // e.printStackTrace();
    // logger.warn("OpenClinicaException:: control.deleteStudyUserRole: " +
    // e.getMessage());
    //
    // forwardPage(Page.ERROR, request, response);
    // }
    // }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}

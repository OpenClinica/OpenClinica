/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.TermType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.*;

/**
 * @author ssachs
 *
 * Servlet for creating a user account.
 */
public class EditStudyUserRoleServlet extends SecureController {
    public static final String INPUT_ROLE = "role";

    public static final String PATH = "EditStudyUserRole";
    public static final String ARG_STUDY_ID = "studyId";
    public static final String ARG_USER_NAME = "userName";

    public static String getLink(StudyUserRoleBean s, UserAccountBean user) {
        int studyId = s.getStudyId();
        return PATH + "?" + ARG_STUDY_ID + "=" + studyId + "&" + ARG_USER_NAME + "=" + user.getName();
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

        int studyId = fp.getInt(ARG_STUDY_ID);
        String uName = fp.getString(ARG_USER_NAME);
        StudyUserRoleBean studyUserRole = udao.findRoleByUserNameAndStudyId(uName, studyId);

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean sb = (StudyBean) sdao.findByPK(studyUserRole.getStudyId());
        if (sb != null) {
            studyUserRole.setStudyName(sb.getName());
        }

        if (!studyUserRole.isActive()) {
            String message = respage.getString("the_user_has_no_role_in_study");
            addPageMessage(message);
            forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
        } else {
            Map roleMap = new LinkedHashMap();
            for (Iterator it = getRoles().iterator(); it.hasNext();) {
                Role role = (Role) it.next();
                roleMap.put(role.getId(), role.getDescription());
            }

            roleMap = new LinkedHashMap();
            ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();
            StudyBean study = (StudyBean) sdao.findByPK(studyUserRole.getStudyId());
            if (study.getParentStudyId() == 0) {
                for (Iterator it = getRoles().iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                        case 2: roleMap.put(role.getId(), resterm.getString("Study_Coordinator").trim());
                            break;
                        case 3: roleMap.put(role.getId(), resterm.getString("Study_Director").trim());
                            break;
                        case 4: roleMap.put(role.getId(), resterm.getString("Investigator").trim());
                            break;
                        case 5: roleMap.put(role.getId(), resterm.getString("Data_Entry_Person").trim());
                            break;
                        case 6: roleMap.put(role.getId(), resterm.getString("Monitor").trim());
                            break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            } else {
                for (Iterator it = getRoles().iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
//                        case 2: roleMap.put(role.getId(), resterm.getString("site_Study_Coordinator").trim());
//                            break;
//                        case 3: roleMap.put(role.getId(), resterm.getString("site_Study_Director").trim());
//                            break;
                        case 4: roleMap.put(role.getId(), resterm.getString("site_investigator").trim());
                            break;
                        case 5: roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Person").trim());
                            break;
                        case 6: roleMap.put(role.getId(), resterm.getString("site_monitor").trim());
                            break;
                        case 7: roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Person2").trim());
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            }

            if (study.getParentStudyId() > 0) {
                roleMap.remove(Role.COORDINATOR.getId());
                roleMap.remove(Role.STUDYDIRECTOR.getId());
            }

            // send the user to the right place..
            if (!fp.isSubmitted()) {
                request.setAttribute("userName", uName);
                request.setAttribute("studyUserRole", studyUserRole);
                request.setAttribute("roles", roleMap);
                request.setAttribute("chosenRoleId", new Integer(studyUserRole.getRole().getId()));
                forwardPage(Page.EDIT_STUDY_USER_ROLE);
            }

            // process the form
            else {
                Validator v = new Validator(request);
                v.addValidation(INPUT_ROLE, Validator.IS_VALID_TERM, TermType.ROLE);
                HashMap errors = v.validate();

                if (errors.isEmpty()) {
                    int roleId = fp.getInt(INPUT_ROLE);
                    Role r = Role.get(roleId);
                    studyUserRole.setRoleName(r.getName());
                    studyUserRole.setUpdater(ub);
                    udao.updateStudyUserRole(studyUserRole, uName);

                    String message = respage.getString("the_user_in_study_has_been_updated");
                    addPageMessage(message);

                    forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
                } else {
                    String message = respage.getString("the_role_choosen_was_invalid_choose_another");
                    addPageMessage(message);

                    request.setAttribute("userName", uName);
                    request.setAttribute("studyUserRole", studyUserRole);
                    request.setAttribute("chosenRoleId", new Integer(fp.getInt(INPUT_ROLE)));
                    request.setAttribute("roles", roleMap);
                    forwardPage(Page.EDIT_STUDY_USER_ROLE);
                }
            }
        }
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
    //
    // int studyId = fp.getInt(ARG_STUDY_ID);
    // String uName = fp.getString(ARG_USER_NAME);
    // StudyUserRoleBean studyUserRole =
    // udao.findRoleByUserNameAndStudyId(uName, studyId);
    //
    // StudyDAO sdao = new StudyDAO(sm.getDataSource());
    // StudyBean sb = (StudyBean) sdao.findByPK(studyUserRole.getStudyId());
    // if (sb != null) {
    // studyUserRole.setStudyName(sb.getName());
    // }
    //
    // if (!studyUserRole.isActive()) {
    // String message = "The user has no role in the specified study. You may
    // not use this feature to add the user to a new study.";
    // request.setAttribute("message", message);
    // forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET, request, response);
    // }
    // else {
    // // send the user to the right place..
    // if (!fp.isSubmitted()) {
    // request.setAttribute("userName", uName);
    // request.setAttribute("studyUserRole", studyUserRole);
    // request.setAttribute("roles", getRoles());
    // request.setAttribute("chosenRoleId", new
    // Integer(studyUserRole.getRole().getId()));
    // forwardPage(Page.EDIT_STUDY_USER_ROLE, request, response);
    // }
    //
    // // process the form
    // else {
    // Validator v = new Validator(request);
    // v.addValidation(INPUT_ROLE, Validator.IS_VALID_TERM, TermType.ROLE);
    // HashMap errors = v.validate();
    //
    // if (errors.isEmpty()) {
    // int roleId = fp.getInt(INPUT_ROLE);
    // Role r = Role.get(roleId);
    // studyUserRole.setRoleName(r.getName());
    // udao.updateStudyUserRole(studyUserRole, uName);
    //
    // String message = "The user's role in the study has been updated.";
    // request.setAttribute("message", message);
    // forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET, request, response);
    // }
    // else {
    // String message = "The role chosen was invalid. Please choose another
    // one.";
    // request.setAttribute("message", message);
    //
    // request.setAttribute("userName", uName);
    // request.setAttribute("studyUserRole", studyUserRole);
    // request.setAttribute("chosenRoleId", new Integer(fp.getInt(INPUT_ROLE)));
    // request.setAttribute("roles", getRoles());
    // forwardPage(Page.EDIT_STUDY_USER_ROLE, request, response);
    // }
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // logger.warn("OpenClinicaException::
    // OpenClinica.control.createUserAccount: " + e.getMessage());
    //
    // forwardPage(Page.ERROR, request, response);
    // }
    // }

    private ArrayList getRoles() {
        ArrayList roles = Role.toArrayList();
        roles.remove(Role.ADMIN);
        return roles;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}

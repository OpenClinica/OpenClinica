/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SetUserRoleServlet extends SecureController {

    @Autowired
    private StudyDao studyDao;
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_USER_ACCOUNTS_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        int userId = fp.getInt("userId");
        if (userId == 0) {
            addPageMessage(respage.getString("please_choose_a_user_to_set_role_for"));
            forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
        } else {
            String action = request.getParameter("action");
            UserAccountBean user = (UserAccountBean) udao.findByPK(userId);
            ArrayList studies = (ArrayList) getStudyDao().findAll();
            ArrayList studiesHaveRole = (ArrayList) getStudyDao().findAllByUser(user.getName());
            studies.removeAll(studiesHaveRole);
            HashSet<Study> studiesNotHaveRole = new HashSet<Study>();
            HashSet<Study> sitesNotHaveRole = new HashSet<Study>();
            for (int i = 0; i < studies.size(); i++) {
                Study study1 = (Study) studies.get(i);

                // TODO: implement equal() according to id
                boolean hasStudy = false;
                for (int j = 0; j < studiesHaveRole.size(); j++) {
                    Study study2 = (Study) studiesHaveRole.get(j);
                    if (study2.getStudyId() == study1.getStudyId()) {
                        hasStudy = true;
                        break;
                    }
                }
                if (!hasStudy) {
                    // YW 11-19-2007 <<
                    if (study1.isSite()) {
                        sitesNotHaveRole.add(study1);
                    } else {
                        studiesNotHaveRole.add(study1);
                    }
                    // YW >>
                }
            }

            Map roleMap = new LinkedHashMap();
            for (Iterator it = getRoles().iterator(); it.hasNext();) {
                Role role = (Role) it.next();
                // I added the below if statement , to exclude displaying on study level the newly added 'ReseachAssisstant2' role by default.
                if (role.getId() != 7)        
                    roleMap.put(role.getId(), role.getDescription());
            }

            Boolean changeRoles = request.getParameter("changeRoles") == null ? false : Boolean.parseBoolean(request.getParameter("changeRoles"));
            int studyId = fp.getInt("studyId");
            if (changeRoles) {
                Study study = (Study) getStudyDao().findByPK(studyId);
                roleMap = new LinkedHashMap();
                ResourceBundle resterm = core.org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();

                if (study.isSite()) {
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
                            case 8: roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Participant").trim());
                                break;
                        default:
                            // logger.info("No role matched when setting role description");
                        }
                    }
                } else {
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
                }
            } else {
                if (currentStudy.isSite()) {
                    roleMap.remove(Role.COORDINATOR.getId());
                    roleMap.remove(Role.STUDYDIRECTOR.getId());
                }
            }
            request.setAttribute("roles", roleMap);
            request.setAttribute("studyId", studyId);
            if ("confirm".equalsIgnoreCase(action) || changeRoles) {
                // YW 11-19-2007 << re-order studiesNotHaveRole so that sites
                // under their studies;
                ArrayList finalStudiesNotHaveRole = new ArrayList();
                Iterator iter_study = studiesNotHaveRole.iterator();
                while (iter_study.hasNext()) {
                    Study s = (Study) iter_study.next();
                    finalStudiesNotHaveRole.add(s);
                    Iterator iter_site = sitesNotHaveRole.iterator();
                    while (iter_site.hasNext()) {
                        Study site = (Study) iter_site.next();
                        if (site.checkAndGetParentStudyId() == s.getStudyId()) {
                            finalStudiesNotHaveRole.add(site);
                        }
                    }
                }
                // YW >>
                request.setAttribute("user", user);
                request.setAttribute("studies", finalStudiesNotHaveRole);
                StudyUserRoleBean uRole = new StudyUserRoleBean();
                uRole.setFirstName(user.getFirstName());
                uRole.setLastName(user.getLastName());
                uRole.setUserName(user.getName());
                request.setAttribute("uRole", uRole);

//                ArrayList roles = Role.toArrayList();
//                roles.remove(Role.ADMIN); // admin is not a user role, only used for tomcat
//                if (currentStudy.getParentStudyId() > 0) {
//                    roles.remove(Role.COORDINATOR);
//                    roles.remove(Role.STUDYDIRECTOR);
//                }
//                request.setAttribute("roles", roles);

                forwardPage(Page.SET_USER_ROLE);
            } else {
                // set role
                String userName = fp.getString("name");
                studyId = fp.getInt("studyId");
                Study userStudy = (Study) getStudyDao().findByPK(studyId);
                int roleId = fp.getInt("roleId");
                // new user role
                StudyUserRoleBean sur = new StudyUserRoleBean();
                sur.setName(userName);
                sur.setRole(Role.get(roleId));
                sur.setStudyId(studyId);
                sur.setStudyName(userStudy.getName());
                sur.setStatus(Status.AVAILABLE);
                sur.setOwner(ub);
                sur.setCreatedDate(new Date());

                if (studyId > 0) {
                    udao.createStudyUserRole(user, sur);

                    addPageMessage(user.getFirstName() + " " + user.getLastName() + " (" + resword.getString("username") + ": " + user.getName() + ") "
                        + respage.getString("has_been_granted_the_role") + " \"" + sur.getRole().getDescription() + "\" " + respage.getString("in_the_study_site") + " "
                        + userStudy.getName() + ".");
                }
                ArrayList <String> pMessage =  (ArrayList<String>) request.getAttribute(SecureController.PAGE_MESSAGE);
                String actionUrl = "ListUserAccounts";
                if (pMessage != null) {
                    actionUrl += "?alertmessage="+  URLEncoder.encode(pMessage.get(0), "UTF-8");
                }
                String url=response.encodeRedirectURL(actionUrl);
                response.sendRedirect(url);

            }

        }
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    private ArrayList getRoles() {
        ArrayList roles = Role.toArrayList();
        roles.remove(Role.ADMIN);

        return roles;
    }


}

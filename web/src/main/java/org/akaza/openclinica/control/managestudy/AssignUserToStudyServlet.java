/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.UserAccountRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Processes request to assign a user to a study
 * 
 * @author jxu
 * 
 */
public class AssignUserToStudyServlet extends SecureController {

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");
        ArrayList users = findUsers();
        String nextListPage = request.getParameter("next_list_page");
        if (StringUtil.isBlank(action) || (nextListPage != null && nextListPage.equalsIgnoreCase("true"))) {
            FormProcessor fp = new FormProcessor(request);
            EntityBeanTable table = fp.getEntityBeanTable();
            ArrayList allRows = UserAccountRow.generateRowsFromBeans(users);

            if (nextListPage == null) {
                session.removeAttribute("tmpSelectedUsersMap");
            }

            /*
             * The tmpSelectedUsersMap will hold all the selected users in the
             * session when the user is navigating through the list. This has
             * been done so that when the user moves to the next page of Users
             * list, the selection made in the previous page doesn't get lost.
             */
            Map tmpSelectedUsersMap = (HashMap) session.getAttribute("tmpSelectedUsersMap");
            if (tmpSelectedUsersMap == null) {
                tmpSelectedUsersMap = new HashMap();
            }
            if (nextListPage != null && nextListPage.equalsIgnoreCase("true")) {
                for (int i = 0; i < users.size(); i++) {
                    int id = fp.getInt("id" + i);
                    int roleId = fp.getInt("activeStudyRoleId" + i);
                    String checked = fp.getString("selected" + i);
                    // logger.info("selected:" + checked);
                    if (!StringUtil.isBlank(checked) && "yes".equalsIgnoreCase(checked.trim())) {
                        tmpSelectedUsersMap.put(id, roleId);
                    } else {
                        // Removing the elements from session which has been
                        // deselected.
                        if (tmpSelectedUsersMap.containsKey(id)) {
                            tmpSelectedUsersMap.remove(id);
                        }
                    }
                }
                session.setAttribute("tmpSelectedUsersMap", tmpSelectedUsersMap);
            }

            String[] columns =
                { resword.getString("user_name"), resword.getString("first_name"), resword.getString("last_name"), resword.getString("role"),
                    resword.getString("selected"), resword.getString("notes") };
            table.setColumns(new ArrayList(Arrays.asList(columns)));
            table.hideColumnLink(3);
            table.hideColumnLink(4);
            table.hideColumnLink(5);
            table.setQuery("AssignUserToStudy", new HashMap());
            table.setRows(allRows);
            table.computeDisplay();

            request.setAttribute("table", table);
            // request.setAttribute("studyUsers", users);
            ArrayList roles = Role.toArrayList();
            if (currentStudy.getParentStudyId() > 0) {
                roles.remove(Role.COORDINATOR);
                roles.remove(Role.STUDYDIRECTOR);
            }

            roles.remove(Role.ADMIN); // admin is not a user role, only used
            // for
            // tomcat
            request.setAttribute("roles", roles);
            forwardPage(Page.STUDY_USER_LIST);
        } else {
            if ("submit".equalsIgnoreCase(action)) {
                addUser(users);
            }
        }
    }

    private void addUser(ArrayList users) throws Exception {
        String pageMass = "";
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        Map tmpSelectedUsersMap = (HashMap) session.getAttribute("tmpSelectedUsersMap");
        Set addedUsers = new HashSet();
        boolean continueLoop = true;
        for (int i = 0; i < users.size()&& continueLoop; i++) {
            int id = fp.getInt("id" + i);
            String firstName = fp.getString("firstName" + i);
            String lastName = fp.getString("lastName" + i);
            String name = fp.getString("name" + i);
            String email = fp.getString("email" + i);
            int roleId = fp.getInt("activeStudyRoleId" + i);
            String checked = fp.getString("selected" + i);
            // logger.info("selected:" + checked);
            
            if (!StringUtil.isBlank(checked) && "yes".equalsIgnoreCase(checked.trim())) {
                logger.info("one user selected");
                UserAccountBean u = new UserAccountBean();
                u.setId(id);
                u.setLastName(lastName);
                u.setFirstName(firstName);
                u.setName(name);
                u.setEmail(email);
                u.setActiveStudyId(ub.getActiveStudyId());
                u.setOwnerId(id);
                addedUsers.add(id);

                StudyUserRoleBean sub = new StudyUserRoleBean();
                sub.setRoleName(Role.get(roleId).getName());
                sub.setStudyId(currentStudy.getId());
                sub.setStatus(Status.AVAILABLE);
                sub.setOwner(ub);
               if(udao.findStudyUserRole(u,sub).getName()!=null && udao.findStudyUserRole(u,sub).getName().isEmpty())//create only when it doesn't exist in database
            	   udao.createStudyUserRole(u, sub);
               else
               {
            	   continueLoop = false;
            	   break;
               }
                logger.info("one user added");
                pageMass = pageMass + sendEmail(u, sub);

            } else {
                if (tmpSelectedUsersMap != null && tmpSelectedUsersMap.containsKey(id)) {
                    tmpSelectedUsersMap.remove(id);
                }
            }
        }
      //  if(!continueLoop) forwardPage(Page.STUDY_USER_LIST);
        
        /* Assigning users which might have been selected during list navigation */
        if (tmpSelectedUsersMap != null) {// try to fix the null pointer
            // exception
            for (Iterator iterator = tmpSelectedUsersMap.keySet().iterator(); iterator.hasNext();) {
                int id = (Integer) iterator.next();
                int roleId = (Integer) tmpSelectedUsersMap.get(id);
                boolean alreadyAdded = false;
                for (Iterator it = addedUsers.iterator(); it.hasNext();) {
                    if (id == (Integer) it.next()) {
                        alreadyAdded = true;
                    }
                }
                if (!alreadyAdded) {
                    UserAccountBean u = new UserAccountBean();
                    u.setId(id);
                    u.setName(udao.findByPK(id).getName());
                    u.setActiveStudyId(ub.getActiveStudyId());
                    u.setOwnerId(id);

                    StudyUserRoleBean sub = new StudyUserRoleBean();
                    sub.setRoleName(Role.get(roleId).getName());
                    sub.setStudyId(currentStudy.getId());
                    sub.setStatus(Status.AVAILABLE);
                    sub.setOwner(ub);
                    udao.createStudyUserRole(u, sub);
                    logger.info("one user added");
                    pageMass = pageMass + sendEmail(u, sub);
                }
            }
        }
        session.removeAttribute("tmpSelectedUsersMap");

        if ("".equals(pageMass)) {
            addPageMessage(respage.getString("no_new_user_assigned_to_study"));
        } else {
            addPageMessage(pageMass);
        }

        ArrayList pageMessages = (ArrayList) request.getAttribute(PAGE_MESSAGE);
        session.setAttribute("pageMessages", pageMessages);
        // tbh #3936 07/2009
        if (currentStudy.getParentStudyId() == 0) {
            response.sendRedirect(request.getContextPath() + Page.MANAGE_STUDY_MODULE.getFileName());
        } else {
            // you are in a site which means you should NOT access build study
            // module
            forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
        }
        // << tbh

    }

    /**
     * Find all users in the system
     * 
     * @return
     */
    private ArrayList findUsers() {
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList userList = (ArrayList) udao.findAll();
        ArrayList userAvailable = new ArrayList();
        for (int i = 0; i < userList.size(); i++) {
            UserAccountBean u = (UserAccountBean) userList.get(i);
            int activeStudyId = currentStudy.getId();
            StudyUserRoleBean sub = udao.findRoleByUserNameAndStudyId(u.getName(), activeStudyId);
            if (!sub.isActive()) {// doesn't have a role in the current study
                sub.setRole(Role.RESEARCHASSISTANT);
                sub.setStudyId(activeStudyId);
                u.setActiveStudyId(activeStudyId);
                u.addRole(sub);
                u.setStatus(Status.AVAILABLE);
                // logger.info("\n activeStudyRole:" +
                // u.getActiveStudyRole().getName());

                // try to find whether this user has role in site or parent
                if (currentStudy.getParentStudyId() > 0) {// this is a site
                    StudyUserRoleBean subParent = udao.findRoleByUserNameAndStudyId(u.getName(), currentStudy.getParentStudyId());
                    if (subParent.isActive()) {
                        u.setNotes(subParent.getRole().getDescription() + " " + respage.getString("in_parent_study"));
                    }

                } else {
                    // find all the sites for this top study
                    StudyDAO sdao = new StudyDAO(sm.getDataSource());
                    ArrayList sites = (ArrayList) sdao.findAllByParent(currentStudy.getId());
                    String notes = "";
                    for (int j = 0; j < sites.size(); j++) {
                        StudyBean site = (StudyBean) sites.get(j);
                        StudyUserRoleBean subSite = udao.findRoleByUserNameAndStudyId(u.getName(), site.getId());
                        if (subSite.isActive()) {
                            notes = notes + subSite.getRole().getDescription() + respage.getString("in_site") + ":" + site.getName() + "; ";
                        }
                    }
                    u.setNotes(notes);
                }

            } else {
                // already have a role in the current study
                sub.setStudyId(activeStudyId);
                u.setActiveStudyId(activeStudyId);
                u.addRole(sub);
                u.setStatus(Status.UNAVAILABLE);
            }
            userAvailable.add(u);
        }
        return userAvailable;

    }

    private String sendEmail(UserAccountBean u, StudyUserRoleBean sub) throws Exception {
        logger.info("Sending email...");
        String body =
            u.getFirstName() + " " + u.getLastName() + "(" + resword.getString("username") + ": " + u.getName() + ") "
                + respage.getString("has_been_assigned_to_the_study") + currentStudy.getName() + " "
                + resword.getString("as") + " \"" + sub.getRole().getDescription() + "\". ";

        if (currentStudy.getParentStudyId() > 0){
            body =
                    u.getFirstName() + " " + u.getLastName() + "(" + resword.getString("username") + ": " + u.getName() + ") "
                        + respage.getString("has_been_assigned_to_the_site")
                        + currentStudy.getName()
                        + " under the Study " + currentStudy.getParentStudyName() +" "
                        + resword.getString("as") + " \"" + sub.getRole().getDescription() + "\". ";
        }
//        boolean emailSent = sendEmail(u.getEmail().trim(), respage.getString("new_user_added_to_study"), body, false);
//        if (emailSent) {
//            sendEmail(ub.getEmail().trim(), EmailEngine.getAdminEmail(), respage.getString("new_user_added_to_study"), body, false,"","", false);
//            sendEmail(EmailEngine.getAdminEmail(), EmailEngine.getAdminEmail(), respage.getString("new_user_added_to_study"), body, false,"","", false);
//        }

        return body;

    }
    



}

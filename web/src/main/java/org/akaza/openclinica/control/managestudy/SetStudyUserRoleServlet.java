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

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SetStudyUserRoleServlet extends SecureController {
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

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_USER_IN_STUDY_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        String name = request.getParameter("name");
        String studyIdString = request.getParameter("studyId");
        if (StringUtil.isBlank(name) || StringUtil.isBlank(studyIdString)) {
            addPageMessage(respage.getString("please_choose_a_user_to_set_role_for"));
            forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
        } else {
            String action = request.getParameter("action");
            FormProcessor fp = new FormProcessor(request);
            UserAccountBean user = (UserAccountBean) udao.findByUserName(name);
            StudyBean userStudy = (StudyBean) sdao.findByPK(fp.getInt("studyId"));
            if ("confirm".equalsIgnoreCase(action)) {
                int studyId = Integer.valueOf(studyIdString.trim()).intValue();

                request.setAttribute("user", user);

                StudyUserRoleBean uRole = udao.findRoleByUserNameAndStudyId(name, studyId);
                uRole.setStudyName(userStudy.getName());
                request.setAttribute("uRole", uRole);

                ArrayList roles = Role.toArrayList();
                roles.remove(Role.ADMIN); // admin is not a user role, only used for tomcat
                roles.remove(Role.RESEARCHASSISTANT2);

                StudyBean studyBean = (StudyBean) sdao.findByPK(uRole.getStudyId());

                if (currentStudy.getParentStudyId() > 0) {
                    roles.remove(Role.COORDINATOR);
                    roles.remove(Role.STUDYDIRECTOR);

                } else if (studyBean.getParentStudyId() > 0) {
                    roles.remove(Role.COORDINATOR);
                    roles.remove(Role.STUDYDIRECTOR);
                    // TODO: redo this fix
                    Role r = Role.RESEARCHASSISTANT;
                    r.setDescription("site_Data_Entry_Person");
                    roles.remove(Role.RESEARCHASSISTANT);
                    roles.add(r);

                    Role ri = Role.INVESTIGATOR;
                    ri.setDescription("site_investigator");
                    roles.remove(Role.INVESTIGATOR);
                    roles.add(ri);

                    Role r2 = Role.RESEARCHASSISTANT2;
                    r2.setDescription("site_Data_Entry_Person2");
                    roles.remove(Role.RESEARCHASSISTANT2);
                    roles.add(r2);

                    Role r3 = Role.PARTICIPATE;
                    r3.setDescription("site_Data_Entry_Participant");
                    roles.remove(Role.PARTICIPATE);
                    roles.add(r3);
                }
                request.setAttribute("roles", roles);

                forwardPage(Page.SET_USER_ROLE_IN_STUDY);
            } else {
                // set role

                String userName = fp.getString("name");
                int studyId = fp.getInt("studyId");
                int roleId = fp.getInt("roleId");
                StudyUserRoleBean sur = new StudyUserRoleBean();
                sur.setName(userName);
                sur.setRole(Role.get(roleId));
                sur.setStudyId(studyId);
                sur.setStudyName(userStudy.getName());
                sur.setStatus(Status.AVAILABLE);
                sur.setUpdater(ub);
                sur.setUpdatedDate(new Date());
                udao.updateStudyUserRole(sur, userName);
                addPageMessage(sendEmail(user, sur));
                forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);

            }

        }
    }

    /**
     * Send email to the user, director and administrator
     *
     * @param request
     * @param response
     */
    private String sendEmail(UserAccountBean u, StudyUserRoleBean sub) throws Exception {

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) sdao.findByPK(sub.getStudyId());
        logger.info("Sending email...");
        String body =
            u.getFirstName() + " " + u.getLastName() + " (" + resword.getString("username") + ": " + u.getName() + ") "
                + respage.getString("has_been_granted_the_role") + " " + sub.getRole().getDescription() + " " + respage.getString("in_the_study_site") + " "
                + study.getName() + ".";

//        boolean emailSent = sendEmail(u.getEmail().trim(), respage.getString("set_user_role"), body, false);
//        if (emailSent) {
//            sendEmail(ub.getEmail().trim(), respage.getString("set_user_role"), body, false);
//            sendEmail(EmailEngine.getAdminEmail(), respage.getString("set_user_role"), body, false);
//        }
        return body;

    }

}

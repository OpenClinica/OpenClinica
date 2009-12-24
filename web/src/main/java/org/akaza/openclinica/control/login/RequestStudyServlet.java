/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.TermType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RequestStudyServlet extends SecureController {
    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        ArrayList studies = sdao.findAllByStatus(Status.AVAILABLE);
        ArrayList roles = Role.toArrayList();
        roles.remove(Role.ADMIN); // admin is not a user role, only used for
        // tomcat

        request.setAttribute("roles", roles);
        request.setAttribute("studies", studies);

        if (StringUtil.isBlank(action)) {
            request.setAttribute("newRole", new StudyUserRoleBean());
            forwardPage(Page.REQUEST_STUDY);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                confirm();

            } else if ("submit".equalsIgnoreCase(action)) {
                submit();
            } else {
                logger.info("here...");
                forwardPage(Page.REQUEST_STUDY);
            }
        }
    }

    /**
     *
     * @param request
     * @param response
     */
    private void confirm() throws Exception {
        Validator v = new Validator(request);
        v.addValidation("studyId", Validator.IS_AN_INTEGER);
        v.addValidation("studyRoleId", Validator.IS_VALID_TERM, TermType.ROLE);

        HashMap errors = v.validate();
        FormProcessor fp = new FormProcessor(request);
        StudyUserRoleBean newRole = new StudyUserRoleBean();
        if (fp.getInt("studyRoleId") > 0) {
            newRole.setRole(Role.get(fp.getInt("studyRoleId")));
        }
        newRole.setStudyId(fp.getInt("studyId"));
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean studyRequested = (StudyBean) sdao.findByPK(newRole.getStudyId());
        newRole.setStudyName(studyRequested.getName());
        session.setAttribute("newRole", newRole);
        if (!errors.isEmpty()) {
            logger.info("after processing form,error is not empty");
            request.setAttribute("formMessages", errors);

            forwardPage(Page.REQUEST_STUDY);

        } else {
            logger.info("after processing form,no errors");

            forwardPage(Page.REQUEST_STUDY_CONFIRM);
        }

    }

    /**
     * Gets user basic info and set email to the administrator
     *
     * @param request
     * @param response
     */
    private void submit() throws Exception {
        StudyUserRoleBean newRole = (StudyUserRoleBean) session.getAttribute("newRole");

        logger.info("Sending email...");
        StringBuffer email = new StringBuffer(restext.getString("dear_openclinica_administrator") + ", <br>");
        email.append(ub.getFirstName() + restext.getString("request_to_acces_the_following_study") + ": <br>");
        email.append(resword.getString("user_full_name") + ": " + ub.getFirstName() + " " + ub.getLastName());
        email.append("<br>" + resword.getString("username2") + ": " + ub.getName());
        email.append("<br>" + resword.getString("email") + ": " + ub.getEmail());
        email.append("<br>" + resword.getString("study_requested") + ":" + newRole.getStudyName() + ", id:" + newRole.getStudyId());
        email.append("<br>" + resword.getString("user_role_requested") + ": " + newRole.getRole().getDescription());
        String emailBody = email.toString();
        logger.info("Sending email...begin" + emailBody);

        sendEmail(EmailEngine.getAdminEmail(), ub.getEmail().trim(), "request study access", emailBody, false);

        session.removeAttribute("newRole");
        forwardPage(Page.MENU);
    }

}

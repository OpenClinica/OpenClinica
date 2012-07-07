/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.login;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.hibernate.PasswordRequirementsDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 * @version CVS: $Id: UpdateProfileServlet.java,v 1.9 2005/02/23 18:58:11 jxu
 *          Exp $
 * 
 * Servlet for processing 'update profile' request from user
 */
public class UpdateProfileServlet extends SecureController {

    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");// action sent by user
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean userBean1 = (UserAccountBean) udao.findByUserName(ub.getName());

        ArrayList studies = (ArrayList) sdao.findAllByUser(ub.getName());

        if (StringUtil.isBlank(action)) {
            request.setAttribute("studies", studies);
            session.setAttribute("userBean1", userBean1);
            forwardPage(Page.UPDATE_PROFILE);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                logger.info("confirm");
                request.setAttribute("studies", studies);
                confirmProfile(userBean1);

            } else if ("submit".equalsIgnoreCase(action)) {
                logger.info("submit");
                submitProfile(udao);

                addPageMessage(respage.getString("profile_updated_succesfully"));
                ub.incNumVisitsToMainMenu();
                forwardPage(Page.MENU_SERVLET);
            }
        }

    }
    
    private boolean hasLowerCaseChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (Character.isLowerCase(str.charAt(i))) return true;
    	}
    	return false;
    }
    private boolean hasUpperCaseChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (Character.isUpperCase(str.charAt(i))) return true;
    	}
    	return false;
    }
    private boolean hasDigits(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (Character.isDigit(str.charAt(i))) return true;
    	}
    	return false;
    }
    private boolean hasSpecialChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (PasswordRequirementsDao.SPECIALS.indexOf(str.charAt(i)) >= 0)
    			return true;
    	}
    	return false;
    }

    private ArrayList<String> validatePassword(
    		UserAccountBean userBean, String newPassword) {
    	ConfigurationDao configurationDao = SpringServletAccess
    			.getApplicationContext(context)
    			.getBean(ConfigurationDao.class);
    	PasswordRequirementsDao passwordRequirementsDao = new PasswordRequirementsDao(configurationDao);
    	
    	ArrayList<String> errors = new ArrayList<String>();
    	// TODO - check for 'allow reuse' option
    	// TODO - check min length
    	// TODO - check max length

    	if (passwordRequirementsDao.hasLower() && !hasLowerCaseChars(newPassword)) {
    		errors.add("pwd_needs_lower_case");
    	}
    	if (passwordRequirementsDao.hasUpper() && !hasUpperCaseChars(newPassword)) {
    		errors.add("pwd_needs_upper_case");
    	}
    	if (passwordRequirementsDao.hasDigits() && !hasDigits(newPassword)) {
    		errors.add("pwd_needs_digits");
    	}
    	if (passwordRequirementsDao.hasSpecials() && !hasSpecialChars(newPassword)) {
    		errors.add("pwd_needs_special_chars");
    	}
    	return errors;
    }

    @SuppressWarnings("unchecked")
	private void confirmProfile(UserAccountBean userBean1) throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation("firstName", Validator.NO_BLANKS);
        v.addValidation("lastName", Validator.NO_BLANKS);
        v.addValidation("email", Validator.IS_A_EMAIL);
        v.addValidation("passwdChallengeQuestion", Validator.NO_BLANKS);
        v.addValidation("passwdChallengeAnswer", Validator.NO_BLANKS);
        // v.addValidation("activeStudyId", Validator.IS_AN_INTEGER);
        v.addValidation("oldPasswd", Validator.NO_BLANKS);// old password
        if (!StringUtil.isBlank(fp.getString("passwd"))) {
            v.addValidation("passwd", Validator.IS_A_PASSWORD);// new password
            v.addValidation("passwd1", Validator.CHECK_SAME, "passwd");// confirm
            // password
        }
        v.addValidation("phone", Validator.NO_BLANKS);
        errors = v.validate();

        ArrayList<String> pwdErrors = validatePassword(userBean1, fp.getString("passwd"));
        for (String err: pwdErrors) {
        	v.addError(errors, "passwd", v.messageFor(err));
        }
        
        userBean1.setFirstName(fp.getString("firstName"));
        userBean1.setLastName(fp.getString("lastName"));
        userBean1.setEmail(fp.getString("email"));
        userBean1.setInstitutionalAffiliation(fp.getString("institutionalAffiliation"));
        userBean1.setPasswdChallengeQuestion(fp.getString("passwdChallengeQuestion"));
        userBean1.setPasswdChallengeAnswer(fp.getString("passwdChallengeAnswer"));
        userBean1.setPhone(fp.getString("phone"));
        userBean1.setActiveStudyId(fp.getInt("activeStudyId"));
        StudyDAO sdao = new StudyDAO(sm.getDataSource());

        StudyBean newActiveStudy = (StudyBean) sdao.findByPK(userBean1.getActiveStudyId());
        request.setAttribute("newActiveStudy", newActiveStudy);

        if (errors.isEmpty()) {
            logger.info("no errors");

            session.setAttribute("userBean1", userBean1);

            // logger.info("old password:" + "in Form:" +
            // fp.getString("oldPasswd") +
            // ", in Session:"
            // + ub.getPasswd());

            String oldPass = fp.getString("oldPasswd").trim();
            SecurityManager sm = (SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager");
            String oldDigestPass = sm.encrytPassword(oldPass, getUserDetails());
            if (!sm.isPasswordValid(ub.getPasswd(), oldPass, getUserDetails())) {
                Validator.addError(errors, "oldPasswd", resexception.getString("wrong_old_password"));
                request.setAttribute("formMessages", errors);
                // addPageMessage("Wrong old password. Please try again.");
                forwardPage(Page.UPDATE_PROFILE);
            } else {
                if (!StringUtil.isBlank(fp.getString("passwd"))) {
                    String newDigestPass = sm.encrytPassword(fp.getString("passwd"), getUserDetails());
                    userBean1.setPasswd(newDigestPass);
                    userBean1.setPasswdTimestamp(new Date());
                }
                session.setAttribute("userBean1", userBean1);
                forwardPage(Page.UPDATE_PROFILE_CONFIRM);
            }

        } else {
            logger.info("has validation errors");
            session.setAttribute("userBean1", userBean1);
            request.setAttribute("formMessages", errors);
            forwardPage(Page.UPDATE_PROFILE);
        }

    }

    /**
     * Updates user new profile
     * 
     */
    private void submitProfile(UserAccountDAO udao) {

        logger.info("user bean to be updated:" + ub.getId() + ub.getFirstName());
        UserAccountBean userBean1 = (UserAccountBean) session.getAttribute("userBean1");
        if (userBean1 != null) {
            userBean1.setLastVisitDate(new Date());
            userBean1.setUpdater(ub);
            udao.update(userBean1);
            session.setAttribute("userBean", userBean1);
            ub = userBean1;
            session.removeAttribute("userBean1");
        }

    }

}

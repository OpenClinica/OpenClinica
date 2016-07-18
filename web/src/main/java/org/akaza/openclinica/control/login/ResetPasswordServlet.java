/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.hibernate.PasswordRequirementsDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringUtils;

import java.util.*;
/**
 * Reset expired password
 *
 * @author ywang
 */
public class ResetPasswordServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5259201015824317949L;

	/**
	 * 
	 */
	

	@Override
    public void mayProceed() throws InsufficientPermissionException {
    }

    /**
     * Tasks include:
     * <ol>
     * <li>Validation:
     * <ol>
     * <li>1. old password match database record
     * <li>2. new password is follows requirements
     * <li>4. two times entered passwords are same
     * <li>5. all required fields are filled
     * </ol>
     * <li>Update ub - UserAccountBean - in session and database
     * </ol>
     */
    @Override
    public void processRequest() throws Exception {
        logger.info("Change expired password");

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        Validator v = new Validator(request);
        errors.clear();
        FormProcessor fp = new FormProcessor(request);
        String mustChangePwd = request.getParameter("mustChangePwd");
        String newPwd = fp.getString("passwd").trim();
        String passwdChallengeQ = fp.getString("passwdChallengeQ");
        String passwdChallengeA = fp.getString("passwdChallengeA");


        if ("yes".equalsIgnoreCase(mustChangePwd)) {
            addPageMessage(respage.getString("your_password_has_expired_must_change"));
        } else {
            addPageMessage(respage.getString("password_expired") + " " + respage.getString("if_you_do_not_want_change_leave_blank"));
        }
        request.setAttribute("mustChangePass", mustChangePwd);

        String oldPwd = fp.getString("oldPasswd").trim();
        UserAccountBean ubForm = new UserAccountBean(); // user bean from web
        // form
        ubForm.setPasswd(oldPwd);
        ubForm.setPasswdChallengeQuestion(passwdChallengeQ);
        ubForm.setPasswdChallengeAnswer(passwdChallengeA);
        request.setAttribute("userBean1", ubForm);
        
        SecurityManager sm = ((SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager"));
 if (!sm.isPasswordValid(ub.getPasswd(), oldPwd, getUserDetails())) {         
		 Validator.addError(errors, "oldPasswd", resexception.getString("wrong_old_password"));
            request.setAttribute("formMessages", errors);
           
            
            forwardPage(Page.RESET_PASSWORD);
        } else {
            if (mustChangePwd.equalsIgnoreCase("yes")) {
                v.addValidation("passwd", Validator.NO_BLANKS);
                v.addValidation("passwd1", Validator.NO_BLANKS);
                v.addValidation("passwdChallengeQ", Validator.NO_BLANKS);
                v.addValidation("passwdChallengeA", Validator.NO_BLANKS);
                v.addValidation("passwd", Validator.CHECK_DIFFERENT, "oldPasswd");
            }

            String newDigestPass = sm.encrytPassword(newPwd, getUserDetails());

            List<String> pwdErrors = new ArrayList<String>();

            if (!StringUtils.isEmpty(newPwd)) {
                v.addValidation("passwd", Validator.IS_A_PASSWORD);
                v.addValidation("passwd1", Validator.CHECK_SAME, "passwd");

                ConfigurationDao configurationDao = SpringServletAccess
                        .getApplicationContext(context)
                        .getBean(ConfigurationDao.class);

                PasswordRequirementsDao passwordRequirementsDao = new PasswordRequirementsDao(configurationDao);

                Locale locale = LocaleResolver.getLocale(request);
                ResourceBundle resexception = ResourceBundleProvider.getExceptionsBundle(locale);

                pwdErrors = PasswordValidator.validatePassword(
                                passwordRequirementsDao,
                                udao,
                                ub.getId(),
                                newPwd,
                                newDigestPass,
                                resexception);

            }
            errors = v.validate();
            for (String err: pwdErrors) {
                v.addError(errors, "passwd", err);
            }

            if (!errors.isEmpty()) {
                logger.info("ResetPassword page has validation errors");
                request.setAttribute("formMessages", errors);
                forwardPage(Page.RESET_PASSWORD);
            } else {
                logger.info("ResetPassword page has no errors");

                if (!StringUtils.isBlank(newPwd)) {
                    ub.setPasswd(newDigestPass);
                    ub.setPasswdTimestamp(new Date());
                } else if ("no".equalsIgnoreCase(mustChangePwd)) {
                    ub.setPasswdTimestamp(new Date());
                }
                ub.setOwner(ub);
                ub.setUpdater(ub);// when update ub, updator id is required
                ub.setPasswdChallengeQuestion(passwdChallengeQ);
                ub.setPasswdChallengeAnswer(passwdChallengeA);
                udao.update(ub);

                ArrayList<String> pageMessages = new ArrayList<String>();
                request.setAttribute(PAGE_MESSAGE, pageMessages);
                addPageMessage(respage.getString("your_expired_password_reset_successfully"));
                ub.incNumVisitsToMainMenu();
                forwardPage(Page.MENU_SERVLET);
            }
        }

    }

}

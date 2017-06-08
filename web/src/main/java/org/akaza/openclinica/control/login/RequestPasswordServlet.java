/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.bean.login.PwdChallengeQuestion;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.filter.OpenClinicaJdbcService;

import java.util.Calendar;
import java.util.Date;

/**
 * @author jxu
 * @version CVS: $Id: RequestPasswordServlet.java 9771 2007-08-28 15:26:26Z
 *          thickerson $
 * 
 *          Servlet of requesting password
 */
public class RequestPasswordServlet extends SecureController {

    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");
        session.setAttribute("challengeQuestions", PwdChallengeQuestion.toArrayList());

        if (StringUtil.isBlank(action)) {
            request.setAttribute("userBean1", new UserAccountBean());
            forwardPage(Page.REQUEST_PWD);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                confirmPassword();

            } else {
                request.setAttribute("userBean1", new UserAccountBean());
                forwardPage(Page.REQUEST_PWD);
            }
        }

    }

    /**
     * 
     * @param request
     * @param response
     */
    private void confirmPassword() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("email", Validator.IS_A_EMAIL);
        v.addValidation("email", Validator.NO_LEADING_OR_TRAILING_SPACES);
        v.addValidation("passwdChallengeQuestion", Validator.NO_BLANKS);
        v.addValidation("passwdChallengeAnswer", Validator.NO_BLANKS);

        errors = v.validate();

        UserAccountBean ubForm = new UserAccountBean(); // user bean from web
        // form
        ubForm.setName(fp.getString("name"));
        ubForm.setEmail(fp.getString("email"));
        ubForm.setPasswdChallengeQuestion(fp.getString("passwdChallengeQuestion"));
        ubForm.setPasswdChallengeAnswer(fp.getString("passwdChallengeAnswer"));

        sm = new SessionManager(null, ubForm.getName(), SpringServletAccess.getApplicationContext(context));

        UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
        // see whether this user in the DB
        UserAccountBean ubDB = (UserAccountBean) uDAO.findByUserName(ubForm.getName());

        UserAccountBean updater = ubDB;

        request.setAttribute("userBean1", ubForm);
        if (!errors.isEmpty()) {
            logger.info("after processing form,has errors");
            request.setAttribute("formMessages", errors);
            forwardPage(Page.REQUEST_PWD);
        } else {
            logger.info("after processing form,no errors");
            // whether this user's email is in the DB
            if (ubDB.getEmail() != null && ubDB.getEmail().equalsIgnoreCase(ubForm.getEmail())) {
                logger.info("ubDB.getPasswdChallengeQuestion()" + ubDB.getPasswdChallengeQuestion());
                logger.info("ubForm.getPasswdChallengeQuestion()" + ubForm.getPasswdChallengeQuestion());
                logger.info("ubDB.getPasswdChallengeAnswer()" + ubDB.getPasswdChallengeAnswer());
                logger.info("ubForm.getPasswdChallengeAnswer()" + ubForm.getPasswdChallengeAnswer());

                // if this user's password challenge can be verified
                if (ubDB.getPasswdChallengeQuestion().equals(ubForm.getPasswdChallengeQuestion())
                    && ubDB.getPasswdChallengeAnswer().equalsIgnoreCase(ubForm.getPasswdChallengeAnswer())) {

                    SecurityManager sm = ((SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager"));
                    String newPass = sm.genPassword();
                    OpenClinicaJdbcService ocService =
                        ((OpenClinicaJdbcService) SpringServletAccess.getApplicationContext(context).getBean("ocUserDetailsService"));
                    String newDigestPass = sm.encrytPassword(newPass, ocService.loadUserByUsername(ubForm.getName()));
                    ubDB.setPasswd(newDigestPass);

                    // passwdtimestamp should be null ,fix
                    // PrepareStatementFactory
                    Calendar cal = Calendar.getInstance();

                    //Date date = local_df.parse("01/01/1900");
                    //cal.setTime(date);
                    //ubDB.setPasswdTimestamp(cal.getTime());
                    ubDB.setPasswdTimestamp(null);
                    ubDB.setUpdater(updater);
                    ubDB.setLastVisitDate(new Date());

                    logger.info("user bean to be updated:" + ubDB.getId() + ubDB.getName() + ubDB.getActiveStudyId());

                    uDAO.update(ubDB);
                    sendPassword(newPass, ubDB);
                } else {
                    addPageMessage(respage.getString("your_password_not_verified_try_again"));
                    forwardPage(Page.REQUEST_PWD);
                }

            } else {
                addPageMessage(respage.getString("your_email_address_not_found_try_again"));
                forwardPage(Page.REQUEST_PWD);
            }

        }

    }

    /**
     * Gets user basic info and set email to the administrator
     * 
     * @param request
     * @param response
     */
    private void sendPassword(String passwd, UserAccountBean ubDB) throws Exception {

        logger.info("Sending email...");

        StringBuffer email = new StringBuffer("Hello, " + ubDB.getFirstName() + ", <br>");
        email.append(restext.getString("this_email_is_from_openclinica_admin") + "<br>");
        email.append( restext.getString("your_password_has_been_reset_as") + ": " + passwd);
        email.append("<br> " + restext.getString("you_will_be_required_to_change")+" ");
        email.append(restext.getString("time_you_login_to_the_system")+" ");
        email.append(restext.getString("use_the_following_link_to_log") + ":<br> ");
        email.append(SQLInitServlet.getField("sysURL"));

        String emailBody = email.toString();
        sendEmail(ubDB.getEmail().trim(), EmailEngine.getAdminEmail(), restext.getString("your_openclinica_password"), emailBody, true, respage
                .getString("your_password_reset_new_password_emailed"), respage.getString("your_password_not_send_due_mail_server_problem"), true);

        session.removeAttribute("challengeQuestions");
        forwardPage(Page.LOGIN);

    }
}

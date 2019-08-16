/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.util.Locale;

import org.akaza.openclinica.bean.core.EntityAction;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

// allows both deletion and restoration of a study user role

public class DeleteUserServlet extends SecureController {

    private static final long serialVersionUID = 298106781476442393L;

    // < ResourceBundle restext;
    Locale locale;

    public static final String PATH = "DeleteUser";
    public static final String ARG_USERID = "userId";
    public static final String ARG_ACTION = "action";

    public static String getLink(UserAccountBean u, EntityAction action) {
        return PATH + "?" + ARG_USERID + "=" + u.getId() + "&" + "&" + ARG_ACTION + "=" + action.getId();
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);

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
        int userId = fp.getInt(ARG_USERID);
        int action = fp.getInt(ARG_ACTION);

        UserAccountBean u = (UserAccountBean) udao.findByPK(userId);
 
        techAdminProtect(u);
        
        String message;
        if (!u.isActive()) {
            message = respage.getString("the_specified_user_not_exits");
        } else if (!EntityAction.contains(action)) {
            message = respage.getString("the_specified_action_on_the_user_is_invalid");
        } else if (!EntityAction.get(action).equals(EntityAction.DELETE) && !EntityAction.get(action).equals(EntityAction.RESTORE)) {
            message = respage.getString("the_specified_action_is_not_allowed");
        } else {
            EntityAction desiredAction = EntityAction.get(action);
            u.setUpdater(ub);

            if (desiredAction.equals(EntityAction.DELETE)) {
                udao.delete(u);

                if (udao.isQuerySuccessful()) {
                    message = respage.getString("the_user_has_been_removed_successfully");
                    // YW 07-31-2007 << for feature that deletion doesn't need
                    // email the deleted user.
                    /*
                     * //YW 07-26-2007 << catch exception (eg. timeout) and
                     * inform users. try { sendDeleteEmail(u); } catch
                     * (Exception e) { message += " However, there has been an
                     * error sending the user an email regarding this
                     * deletion."; }
                     */
                    // YW >>
                } else {
                    message = respage.getString("the_user_could_not_be_deleted_due_database_error");
                }
            } else {
                SecurityManager sm = (SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager");
                String password = sm.genPassword();
                String passwordHash = sm.encrytPassword(password, getUserDetails());

                if (!u.isLdapUser()) {
                    u.setPasswd(passwordHash);
                    u.setPasswdTimestamp(null);
                }

                udao.restore(u);

                if (udao.isQuerySuccessful()) {
                    message = respage.getString("the_user_has_been_restored");

                    try {
                        if (!u.isLdapUser()) {
                            sendRestoreEmail(u, password);
                        }
                    } catch (Exception e) {
                        message += respage.getString("however_was_error_sending_user_email_regarding");
                    }
                } else {
                    message = respage.getString("the_user_could_not_be_deleted_due_database_error");
                }
            }
        }

        addPageMessage(message);
        forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
    }

    private void sendRestoreEmail(UserAccountBean u, String password) throws Exception {
        logger.info("Sending restore and password reset notification to " + u.getName());

        String body = resword.getString("dear")+ " " + u.getFirstName() + " " + u.getLastName() + ",\n";
        body += restext.getString("your_account_has_been_restored_and_password_reset") + ":\n\n";
        body += resword.getString("user_name")+ " "  + u.getName() + "\n";
        body += resword.getString("password")+ " "  + password + "\n\n";
        body += restext.getString("please_test_your_login_information_and_let") + "\n";
        body += SQLInitServlet.getField("sysURL");
        body += " . ";
        body += restext.getString("openclinica_system_administrator");

        logger.info("Sending email...begin");
        sendEmail(u.getEmail().trim(), restext.getString("your_new_openclinica_account_has_been_restored"), body, false);
        logger.info("Sending email...done");
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}

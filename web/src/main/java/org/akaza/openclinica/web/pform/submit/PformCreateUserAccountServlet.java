/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.pform.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.TermType;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.domain.user.LdapUser;
import org.akaza.openclinica.service.user.LdapUserService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

/**
 * Servlet for creating a user account.
 *
 * @author ssachs
 */
public class PformCreateUserAccountServlet extends SecureController {

	// < ResourceBundle restext;
	Locale locale;

	public static String STUDY_OID = "S_BL101A";
	public static Integer SUBJECTID = 110;
	public int studyId = 4;

	public static final String INPUT_USER_SOURCE = "userSource";
	public static final String INPUT_USERNAME = STUDY_OID.trim() + SUBJECTID;
	public static final String INPUT_FIRST_NAME = "particiapant";
	public static final String INPUT_LAST_NAME = "User";
	public static final String INPUT_EMAIL = "email";
	public static final String INPUT_INSTITUTION = "PFORM";
	public static final String INPUT_STUDY = "activeStudy";
	public static final String INPUT_ROLE = "role";
	public static final String INPUT_TYPE = "type";
	public static final String INPUT_DISPLAY_PWD = "displayPwd";
	public static final String INPUT_RUN_WEBSERVICES = "runWebServices";
	public static final String USER_ACCOUNT_NOTIFICATION = "notifyPassword";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
	 */
	@Override
	protected void mayProceed() throws InsufficientPermissionException {

		locale = LocaleResolver.getLocale(request);
		// < restext =
		// ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);

		if (!ub.isSysAdmin()) {
			throw new InsufficientPermissionException(Page.MENU, resexception.getString("you_may_not_perform_administrative_functions"),
					"1");
		}

		return;
	}

	@Override
	public void processRequest() throws Exception {
		FormProcessor fp = new FormProcessor(request);

		UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
		Validator v = new Validator(request);

		// username must be unique
		v.addValidation(INPUT_USERNAME, Validator.USERNAME_UNIQUE_PFORM, udao);

		HashMap errors = v.validate();

		if (errors.isEmpty()) {
			UserAccountBean createdUserAccountBean = new UserAccountBean();
			createdUserAccountBean.setName(INPUT_USERNAME);
			createdUserAccountBean.setFirstName(INPUT_FIRST_NAME);
			createdUserAccountBean.setLastName(INPUT_LAST_NAME);
			createdUserAccountBean.setEmail(INPUT_EMAIL);
			createdUserAccountBean.setInstitutionalAffiliation(INPUT_INSTITUTION);

			String password = null;
			String passwordHash = UserAccountBean.LDAP_PASSWORD;

			SecurityManager secm = (SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager");
			password = secm.genPassword();
			passwordHash = secm.encrytPassword(password, getUserDetails());

			createdUserAccountBean.setPasswd(passwordHash);

			createdUserAccountBean.setPasswdTimestamp(null);
			createdUserAccountBean.setLastVisitDate(null);

			createdUserAccountBean.setStatus(Status.DELETED);
			createdUserAccountBean.setPasswdChallengeQuestion("");
			createdUserAccountBean.setPasswdChallengeAnswer("");
			createdUserAccountBean.setPhone("");
			createdUserAccountBean.setOwner(ub);
			createdUserAccountBean.setRunWebservices(false);

			Role r = Role.RESEARCHASSISTANT2;

			createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, studyId, r);
			UserType type = UserType.get(2);
			logger.debug("*** found type: " + fp.getInt("type"));
			logger.debug("*** setting type: " + type.getDescription());
			createdUserAccountBean.addUserType(type);
			createdUserAccountBean = (UserAccountBean) udao.create(createdUserAccountBean);
			AuthoritiesDao authoritiesDao = (AuthoritiesDao) SpringServletAccess.getApplicationContext(context).getBean("authoritiesDao");
			authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));
			String displayPwd = fp.getString(INPUT_DISPLAY_PWD);

			if (createdUserAccountBean.isActive()) {
				addPageMessage(respage.getString("the_user_account") + "\"" + createdUserAccountBean.getName() + "\""
						+ respage.getString("was_created_succesfully"));
				if ("no".equalsIgnoreCase(displayPwd)) {
					try {
						// sendNewAccountEmail(createdUserAccountBean,
						// password);
					} catch (Exception e) {
						addPageMessage(respage.getString("the_user_already_exists_in_the_system") + " :  " + INPUT_USERNAME);
					}
				}
				forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
			}

		} else {
			setInputMessages(errors);
			addPageMessage(respage.getString("the_user_already_exists_in_the_system") + " :  " + INPUT_USERNAME);
			forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);

		}

	}

	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r) {
		createdUserAccountBean.setActiveStudyId(studyId);

		StudyUserRoleBean activeStudyRole = new StudyUserRoleBean();

		activeStudyRole.setStudyId(studyId);
		activeStudyRole.setRoleName(r.getName());
		activeStudyRole.setStatus(Status.AVAILABLE);
		activeStudyRole.setOwner(ub);

		createdUserAccountBean.addRole(activeStudyRole);

		return createdUserAccountBean;
	}

	@Override
	protected String getAdminServlet() {
		return SecureController.ADMIN_SERVLET_CODE;
	}
}

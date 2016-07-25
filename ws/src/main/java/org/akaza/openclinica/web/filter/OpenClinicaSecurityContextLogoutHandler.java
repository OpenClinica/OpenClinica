package org.akaza.openclinica.web.filter;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Date;
import java.util.Locale;

/**
 * Call Super Class SecurityContextLogoutHandler that Performs a logout by modifying the {@link org.springframework.security.context.SecurityContextHolder}.
 * <p>
 * Will log this event to an OpenClinica user logging table
 * 
 * @author Krikor Krumlian
 */
public class OpenClinicaSecurityContextLogoutHandler extends SecurityContextLogoutHandler {

    AuditUserLoginDao auditUserLoginDao;
    UserAccountDAO userAccountDao;
    DataSource dataSource;

    // ~ Methods ========================================================================================================

    /**
     * Requires the request to be passed in.
     * 
     * @param request
     *            from which to obtain a HTTP session (cannot be null)
     * @param response
     *            not used (can be <code>null</code>)
     * @param authentication
     *            not used (can be <code>null</code>)
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            auditLogout(authentication.getName());
        }
        super.logout(request, response, authentication);
    }

    void auditLogout(String username) {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        UserAccountBean userAccount = (UserAccountBean) getUserAccountDao().findByUserName(username);
        AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
        auditUserLogin.setUserName(username);
        auditUserLogin.setLoginStatus(LoginStatus.SUCCESSFUL_LOGOUT);
        auditUserLogin.setLoginAttemptDate(new Date());
        auditUserLogin.setUserAccountId(userAccount != null ? userAccount.getId() : null);
        getAuditUserLoginDao().saveOrUpdate(auditUserLogin);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserAccountDAO getUserAccountDao() {
        return userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

}

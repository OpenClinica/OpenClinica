package org.akaza.openclinica.web.filter;

import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public class OpenClinicaSessionRegistryImpl extends SessionRegistryImpl {

    AuditUserLoginDao auditUserLoginDao;
    UserAccountDAO userAccountDao;
    DataSource dataSource;
    CRFLocker crfLocker;

    @Override
    public void removeSessionInformation(String sessionId) {
        SessionInformation info = getSessionInformation(sessionId);

        if (info != null) {
            String username = null;
            Object p = info.getPrincipal();
            if (p instanceof User) {
                username = ((User) p).getUsername();
            } else if (p instanceof LdapUserDetails) {
                username = ((LdapUserDetails) p).getUsername();
            }

            auditLogout(username);
        }
        super.removeSessionInformation(sessionId);
    }

    void auditLogout(String username) {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        UserAccountBean userAccount = (UserAccountBean) getUserAccountDao().findByUserName(username);
    	System.out.println("Unlocking all crfs for user " + userAccount.getId() + " from OpenClinicaSessionRegistryImpl.auditLogout()");
        crfLocker.unlockAllForUser(userAccount.getId());

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

    public void setCrfLocker(CRFLocker crfLocker) {
        this.crfLocker = crfLocker;
    }
}

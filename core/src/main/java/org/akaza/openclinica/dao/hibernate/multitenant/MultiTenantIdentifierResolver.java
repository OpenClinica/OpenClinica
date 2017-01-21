package org.akaza.openclinica.dao.hibernate.multitenant;

/**
 * Created by yogi on 1/19/17.
 */
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class MultiTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static void setTenantIdentifier(String tenantIdentifier) {
        threadLocal.set(tenantIdentifier);
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        return threadLocal.get();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        String tenantIdentifier = threadLocal.get();
        if (tenantIdentifier != null) {
            return true;
        } else {
            return false;
        }
    }
}
package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by yogi on 1/16/17.
 */
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    public static final String DEFAULT_TENANT_ID = "public";
    public static final String CURRENT_TENANT_ID = "current_tenant_id";
    private ThreadLocal currentTenantId = new ThreadLocal();

    @Override
    public String resolveCurrentTenantIdentifier() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String path = attr.getRequest().getPathInfo();
            UserAccountBean ub = (UserAccountBean) attr.getRequest().getSession().getAttribute("userBean");

            if (ub != null) {
                HttpSession session = attr.getRequest().getSession();
                if (StringUtils.isNotEmpty(path) && path.contains("/schema/tenant")) {
                    String identifier = path.substring(path.lastIndexOf("/") + 1);
                    if (StringUtils.isNotEmpty(identifier)) {
                        System.out.println("Returning tenant:" + identifier);
                        session.setAttribute(CURRENT_TENANT_ID, identifier);
                        currentTenantId.set(identifier);
                        session.setAttribute("study", null);
                        return identifier;
                    }
                } else if (session.getAttribute(CURRENT_TENANT_ID) != null) {
                    return (String) session.getAttribute(CURRENT_TENANT_ID);
                }
            }
        }
        if (currentTenantId.get() != null)
            return (String) currentTenantId.get();
        else {
            System.out.println("Returning default tenant:" + DEFAULT_TENANT_ID);
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}

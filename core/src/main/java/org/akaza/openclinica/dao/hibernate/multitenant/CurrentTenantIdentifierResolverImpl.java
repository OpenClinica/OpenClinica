package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.bean.login.UserAccountBean;
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

    private static final String DEFAULT_TENANT_ID = "tenant1";

    @Override
    public String resolveCurrentTenantIdentifier() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String path = attr.getRequest().getPathInfo();
            UserAccountBean ub = (UserAccountBean) attr.getRequest().getSession().getAttribute("userBean");

            if (path != null && ub != null) {
               if (path.contains("/schema/tenant")) {

                   String identifier = path.substring(path.lastIndexOf("/") + 1);
                   if (StringUtils.isNotEmpty(identifier)) {
                       System.out.println("Returning tenant:" + identifier);
                       return identifier;
                   } else
                       return DEFAULT_TENANT_ID;
               }
            }
        }
        System.out.println("Returning default tenant");
        return DEFAULT_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}

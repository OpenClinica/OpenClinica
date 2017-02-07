package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by yogi on 1/16/17.
 */
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    public static final String DEFAULT_TENANT_ID = "public";
    public static final String CURRENT_TENANT_ID = "current_tenant_id";

    @Override
    public String resolveCurrentTenantIdentifier() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            String tenant = null;
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession();

            if (request.getAttribute(CURRENT_TENANT_ID) != null)
                tenant = (String) request.getAttribute(CURRENT_TENANT_ID);
            else if (session != null && session.getAttribute(CURRENT_TENANT_ID) != null)
                tenant = (String) session.getAttribute(CURRENT_TENANT_ID);

            if (StringUtils.isNotEmpty(tenant))
                return tenant;
        }
        System.out.println("Returning default tenant:" + DEFAULT_TENANT_ID);
        return DEFAULT_TENANT_ID;

    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}

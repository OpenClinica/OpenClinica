package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by yogi on 1/16/17.
 */
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    public static final String DEFAULT_TENANT_ID = "public";
    public static final String CURRENT_TENANT_ID = "current_tenant_id";
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public String resolveCurrentTenantIdentifier() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            String tenant = null;
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession();
            if (request.getParameter("changeStudySchema") != null) {
                tenant = (String) request.getParameter("changeStudySchema");
                if (session != null) {
                    session.setAttribute(CURRENT_TENANT_ID, tenant);
                }
            } else if (request.getAttribute("requestSchema") != null) {
                tenant = (String) request.getAttribute("requestSchema");
            } else if (request.getAttribute(CURRENT_TENANT_ID) != null) {
                tenant = (String) request.getAttribute(CURRENT_TENANT_ID);
            } else if (session != null) {
                if (session.getAttribute("requestSchema") != null) {
                    tenant = (String) request.getAttribute("requestSchema");;
                } else if (session.getAttribute(CURRENT_TENANT_ID) != null) {
                    tenant = (String) session.getAttribute(CURRENT_TENANT_ID);
                }
            }
            if (StringUtils.isNotEmpty(tenant)) {
                CoreResources.tenantSchema.set(tenant);
                return tenant;
            }
        }

        String tenant = CoreResources.tenantSchema.get();
        if (StringUtils.isEmpty(tenant)) {
            tenant = DEFAULT_TENANT_ID;
        }
        logger.debug("Returning default tenant:" + tenant);
        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}

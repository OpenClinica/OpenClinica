package org.akaza.openclinica.dao.hibernate.multitenant;

/**
 * Created by yogi on 2/1/17.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;
import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT_ID;

@Component
public class MultiTenantFilter implements Filter {

    //@Value("${multitenant.tenantKey}")
    public static String tenantKey = CURRENT_TENANT_ID;

    //@Value("${multitenant.defaultTenant}")
    public static String defaultTenant = DEFAULT_TENANT_ID;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String tenant = null;
        /*HttpServletRequest req = (HttpServletRequest) request;
        if (request.getParameter("studyOID") != null)) {
            tenant = request.getParameter("studyOID");
        }
                || (request.getParameter("studyOid") != null)) {

        }
        String tenant = req.getHeader(tenantKey);

        if (tenant != null) {
            req.setAttribute(tenantKey, tenant);
        } else {
            req.setAttribute(tenantKey, defaultTenant);
        }*/
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}

package org.akaza.openclinica.dao.hibernate.multitenant;

/**
 * Created by yogi on 2/1/17.
 */
import org.akaza.openclinica.core.form.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

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
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        // first check the header elements
        tenant = req.getHeader("studyOid");
        if (StringUtils.isEmpty(tenant)) {
            tenant = req.getHeader("studyOID");
        }
        if (StringUtils.isEmpty(tenant)) {
            if (req.getParameter("studyOID") != null) {
                tenant = req.getParameter("studyOID");
            } else if (req.getParameter("studyOid") != null) {
                tenant = req.getParameter("studyOid");
            } else {
                String path = req.getRequestURI();
                if (StringUtils.isNotEmpty(path)) {
                    if (path.endsWith("/schema/tenant") || path.contains("/schema/public")) {
                        tenant = path.substring(path.lastIndexOf("/") + 1);
                    } else if (path.endsWith("/protocol/build")) {
                        req.setAttribute("requestSchema", "public");
                        System.out.println("Comes here");
                    } else if (path.endsWith("/ListStudy")) {
                        req.setAttribute("requestSchema", "public");
                        System.out.println("Comes here");
                    }
                    if (StringUtils.isNotEmpty(tenant)) {
                        System.out.println("Returning tenant:" + tenant);

                        if (session != null) {
                            session.setAttribute("study", null);
                        }
                    }
                }
                if (StringUtils.isEmpty(tenant)) {
                    if (req.getAttribute(CURRENT_TENANT_ID) != null) {
                        tenant = (String) req.getAttribute(CURRENT_TENANT_ID);
                    } else if (session != null && session.getAttribute(CURRENT_TENANT_ID) != null) {
                        tenant = (String) session.getAttribute(CURRENT_TENANT_ID);
                    }
                }
            }
        }
        if (StringUtils.isEmpty(tenant)) {
            tenant = defaultTenant;
        }
        req.setAttribute(tenantKey, tenant);

        if (session != null ) {
            session.setAttribute(tenantKey, tenant);
        }
        chain.doFilter(req, response);
    }

    @Override
    public void destroy() {

    }
}

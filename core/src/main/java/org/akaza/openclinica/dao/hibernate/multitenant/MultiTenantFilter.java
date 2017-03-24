package org.akaza.openclinica.dao.hibernate.multitenant;

/**
 * Created by yogi on 2/1/17.
 */

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;
import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT_ID;

@Component
public class MultiTenantFilter implements Filter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static String tenantKey = CURRENT_TENANT_ID;
    public static String defaultTenant = DEFAULT_TENANT_ID;
    public static HashSet<String> excludedFileTypes = new HashSet<String>(
            Arrays.asList(
                    "js",
                    "gif",
                    "jpg",
                    "jpeg",
                    "png",
                    "css",
                    "xml"
            ));
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
                tenant = getRequestSchema(req, session);
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

    private String getRequestSchema(HttpServletRequest req, HttpSession session) {
        String tenant = null;
        String path = StringUtils.substringAfterLast(req.getRequestURI(), "/");
        if (StringUtils.isEmpty(path))
            return tenant;
        String ext = StringUtils.substringAfterLast(path, ".");
        if (StringUtils.isNotEmpty(ext) && excludedFileTypes.contains(ext)) {
                return tenant;
        }
        switch(path) {
        case "ListStudy":
        case "ChangeStudy":
            req.setAttribute("requestSchema", "public");
            tenant = "public";
            logger.debug("Request schema is set to 'public'");
            break;
        default:
            if (session == null)
                return tenant;
            StudyBean publicStudy = (StudyBean) session.getAttribute("publicStudy");
            if (publicStudy != null) {
                req.setAttribute("requestSchema", publicStudy.getSchemaName());
                tenant = publicStudy.getSchemaName();
            }
            break;
        }
        return tenant;
    }
    @Override
    public void destroy() {

    }
}

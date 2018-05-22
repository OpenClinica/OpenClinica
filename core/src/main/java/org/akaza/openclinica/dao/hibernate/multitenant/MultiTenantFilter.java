package org.akaza.openclinica.dao.hibernate.multitenant;

/**
 * Created by yogi on 2/1/17.
 */

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.ExtendedBasicDataSource;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;
import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT_ID;

@Component public class MultiTenantFilter implements Filter {

    public static String tenantKey = CURRENT_TENANT_ID;
    public static String defaultTenant = DEFAULT_TENANT_ID;
    public static HashSet<String> excludedFileTypes = new HashSet<String>(Arrays.asList("js", "gif", "jpg", "jpeg", "png", "css", "xml"));
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override public void init(FilterConfig filterConfig) throws ServletException {

    }

    public DataSource getDataSource() {
        BasicDataSource ds = new ExtendedBasicDataSource();
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setDriverClassName(CoreResources.getField("dataBase"));
        ds.setUsername(CoreResources.getField("username"));
        ds.setPassword(CoreResources.getField("password"));
        ds.setUrl(CoreResources.getField("url"));
        ds.setDriverClassName(CoreResources.getField("driver"));
        return ds;
    }

    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String tenant = null;
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        // first check the header elements
        String studyOid = req.getHeader("studyOid");
        if (StringUtils.isEmpty(studyOid)) {
            tenant = getRequestSchema(req, session);
            if (StringUtils.isEmpty(tenant)) {
                if (req.getAttribute(CURRENT_TENANT_ID) != null) {
                    tenant = (String) req.getAttribute(CURRENT_TENANT_ID);
                } else if (session != null && session.getAttribute(CURRENT_TENANT_ID) != null) {
                    tenant = (String) session.getAttribute(CURRENT_TENANT_ID);
                }
            }

        } else {
            request.setAttribute("studyOid", studyOid);
        }
        if (StringUtils.isEmpty(tenant)) {
            tenant = defaultTenant;
        }
        req.setAttribute(tenantKey, tenant);

        if (session != null) {
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
        switch (path) {
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

    @Override public void destroy() {

    }
}

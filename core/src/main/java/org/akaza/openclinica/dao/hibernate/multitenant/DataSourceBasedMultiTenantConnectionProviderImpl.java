package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl
		implements ServiceRegistryAwareService, Stoppable {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	private BasicDataSource dataSource;


	@Override protected DataSource selectAnyDataSource() {
		return dataSource;
	}

	@Override protected DataSource selectDataSource(String tenantIdentifier) {
        return dataSource;
    }
	@Override public Connection getConnection(String tenantIdentifier) throws SQLException {

		String tenant = getTenant();
		if (tenant == null)
		    tenant = tenantIdentifier;
		Connection conn = super.getConnection(tenant);
		logger.debug("Tenant schema:" + tenant);
		conn.setSchema(tenant);
		return conn;
	}

	private String getTenant() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String tenant = null;
        if (requestAttributes != null) {
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
                    tenant = (String) request.getAttribute("requestSchema");
                    ;
                } else if (session.getAttribute(CURRENT_TENANT_ID) != null) {
                    tenant = (String) session.getAttribute(CURRENT_TENANT_ID);
                }
            }
            if (StringUtils.isNotEmpty(tenant)) {
                CoreResources.tenantSchema.set(tenant);
            }
        }
        return tenant;
    }
    @Override public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        final Object dataSourceConfigValue = serviceRegistry.getService( ConfigurationService.class )
                .getSettings()
                .get( AvailableSettings.DATASOURCE );
	    logger.debug("using datasource class" + dataSourceConfigValue.getClass().toString());
        dataSource = (BasicDataSource) dataSourceConfigValue;
    }

    @Override public void stop() {

    }
}
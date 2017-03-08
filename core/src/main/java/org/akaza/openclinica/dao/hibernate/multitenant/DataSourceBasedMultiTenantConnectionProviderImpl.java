package org.akaza.openclinica.dao.hibernate.multitenant;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
		Connection conn = super.getConnection(tenantIdentifier);
		logger.debug("Tenant schema:" + tenantIdentifier);
		conn.setSchema(tenantIdentifier);
		return conn;
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
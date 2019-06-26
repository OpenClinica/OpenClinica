package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.core.ExtendedBasicDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractMultiTenantConnectionProvider
        implements ServiceRegistryAwareService {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	@Autowired
	private ExtendedBasicDataSource dataSource;

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return new ConnectionProviderImpl();
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return new ConnectionProviderImpl(tenantIdentifier);
    }

	private class ConnectionProviderImpl extends UserSuppliedConnectionProviderImpl {
        private String tenantId;

        public ConnectionProviderImpl(String tenantId) {
            this.tenantId = tenantId;
        }

        public ConnectionProviderImpl() {

        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = dataSource.getAnyConnection();
            if (tenantId != null) {
                connection.setSchema(tenantId);
            }
            return connection;
        }

        public void closeConnection(Connection conn) throws SQLException {
            conn.close();
        }
    }

    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        final Object dataSourceConfigValue = serviceRegistry.getService( ConfigurationService.class )
                .getSettings()
                .get( AvailableSettings.DATASOURCE );
        logger.debug("using datasource class" + dataSourceConfigValue.getClass().toString());
        dataSource = (ExtendedBasicDataSource) dataSourceConfigValue;
    }
}
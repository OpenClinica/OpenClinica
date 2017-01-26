package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.core.ExtendedBasicDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.DEFAULT_TENANT_ID;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {


	@Autowired private BasicDataSource dataSource;

	private static Map<String, DataSource> map;

	@PostConstruct public void load() {
		map = new HashMap<>();
		map.put(DEFAULT_TENANT_ID, dataSource);
        map.put("tenant1", dataSource);
        map.put("tenant2", dataSource);
		map.put("tenant3", dataSource);
	}

	@Override protected DataSource selectAnyDataSource() {
		return map.get(DEFAULT_TENANT_ID);
	}

	@Override protected DataSource selectDataSource(String tenantIdentifier) {
		return map.get(tenantIdentifier);
	}

	@Override public Connection getConnection(String tenantIdentifier) throws SQLException {
		Connection conn = super.getConnection(tenantIdentifier);
		System.out.println("*************************Tenant schema:" + tenantIdentifier);
		conn.setSchema(tenantIdentifier);
		return conn;
	}
}
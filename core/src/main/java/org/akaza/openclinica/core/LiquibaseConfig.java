package org.akaza.openclinica.core;

import liquibase.integration.spring.SpringLiquibase;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogi on 3/22/17.
 */
@Configuration
public class LiquibaseConfig {
    @Autowired CoreResources coreResources;
    @Autowired DataSource dataSource;

    @Bean
    @DependsOn ("coreResources")
    public OCCreatePostgresAppServer createPostgresAppServer() {
        OCCreatePostgresAppServer appServer = new OCCreatePostgresAppServer();
        appServer.setDataSource(dataSource);
        appServer.setChangeLog("classpath:migration/appServer/release.xml");
        return appServer;
    }

    @Bean
    @DependsOn ("coreResources")
    public OCCommonTablesSpringLiquibase liquibaseSchemaCommonTables() {
        OCCommonTablesSpringLiquibase liquibase = new OCCommonTablesSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("public");
        List<String> schemas = new ArrayList<>();
        schemas.add("public");
        liquibase.setSchemas(schemas);
        liquibase.setChangeLog("classpath:migration/schemaCommonTables/release.xml");
        return liquibase;
    }

    @Bean
    @DependsOn ("coreResources")
    public SpringLiquibase liquibasePublicSchema() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("public");
        liquibase.setChangeLog("classpath:migration/publicSchemaOnly/release.xml");
        return liquibase;
    }

    @Bean
    @DependsOn ("coreResources")
    public OCSpringLiquibase liquibaseForeignTables() {
        OCSpringLiquibase liquibase = new OCSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:migration/schema/release.xml");
        return liquibase;
    }

    @Bean
    @DependsOn ("coreResources")
    public OCMultiTenantSpringLiquibase liquibase() {
        OCMultiTenantSpringLiquibase liquibase = new OCMultiTenantSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:migration/master.xml");
        return liquibase;
    }

}

package core.org.akaza.openclinica.core;

import liquibase.integration.spring.SpringLiquibase;
import core.org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;

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
        appServer.setChangeLog("classpath:core/migration/initDB/release.xml");
        return appServer;
    }

    @Bean
    @DependsOn ("createPostgresAppServer")
    @Scope(value = "prototype")
    public OCCommonTablesSpringLiquibase liquibaseSchemaCommonTables() {
        OCCommonTablesSpringLiquibase liquibase = new OCCommonTablesSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("public");
        liquibase.setChangeLog("classpath:core/migration/dualPurposeStudyTable/release.xml");
        return liquibase;
    }
    
    @Bean
    @DependsOn ("liquibaseSchemaCommonTables")
    public SpringLiquibase liquibasePublicSchema() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("public");
        liquibase.setChangeLog("classpath:core/migration/publicSchemaOnly/release.xml");
        return liquibase;
    }



    @Bean
    @DependsOn ("liquibasePublicSchema")
    public OCSpringLiquibase liquibaseForeignTables() {
        OCSpringLiquibase liquibase = new OCSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:core/migration/tenantForeignTables/release.xml");
        return liquibase;
    }

    @Bean
    @DependsOn ("liquibaseForeignTables")
    public OCMultiTenantSpringLiquibase liquibase() {
        OCMultiTenantSpringLiquibase liquibase = new OCMultiTenantSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:core/migration/master.xml");
        return liquibase;
    }
}

package org.akaza.openclinica.core;

import liquibase.integration.spring.MultiTenantSpringLiquibase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogi on 2/17/17.
 */
public class OCMultiTenantSpringLiquibase extends MultiTenantSpringLiquibase {
    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> schemas = new ArrayList<>();
        schemas.add("public");
        //schemas.add("tenant1");
        super.setSchemas(schemas);
        super.afterPropertiesSet();
    }
    public void dynamicAfterPropertiesSet(List<String>schemas) throws Exception {
        super.setSchemas(schemas);
        super.afterPropertiesSet();
    }
}

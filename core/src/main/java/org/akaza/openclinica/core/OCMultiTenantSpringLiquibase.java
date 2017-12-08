package org.akaza.openclinica.core;

import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogi on 2/17/17.
 */
public class OCMultiTenantSpringLiquibase extends CustomMultiTenantSpringLiquibase {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired StudyDao studyDao;
    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> schemas = null;
        try {
            schemas = studyDao.findAllSchemas();
        } catch (Exception e) {
            logger.info("There is no study created as of yet.", e.getMessage());
        }
        if (schemas == null) {
            schemas = new ArrayList<>();
        }
        schemas.add("public");
        super.setSchemas(schemas);
        super.afterPropertiesSet();
    }
    public void dynamicAfterPropertiesSet(List<String>schemas) throws Exception {
        super.setSchemas(schemas);
        super.afterPropertiesSet();
    }
}

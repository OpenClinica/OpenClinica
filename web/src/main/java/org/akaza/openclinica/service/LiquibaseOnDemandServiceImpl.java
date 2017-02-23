package org.akaza.openclinica.service;

import org.akaza.openclinica.core.OCMultiTenantSpringLiquibase;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yogi on 2/23/17.
 */
@Service("liquibaseOnDemandService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class LiquibaseOnDemandServiceImpl implements LiquibaseOnDemandService {
    @Autowired
    ApplicationContext context;
    @Autowired
    private StudyDao studyDao;
    public void process(String schemaName, String name, String uniqueId, String ocId, HttpServletRequest request,
            HttpServletResponse response) {
        Session session = studyDao.getSessionFactory().getCurrentSession();

        try {
            OCMultiTenantSpringLiquibase liquibase = (OCMultiTenantSpringLiquibase) context.getBean("liquibase");
            List schemas = new ArrayList();
            schemas.add(schemaName);
            liquibase.setSchemas(schemas);
            liquibase.dynamicAfterPropertiesSet(schemas);
        } catch (Exception e) {
            System.out.println("Error while creating a schema error 3");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        try {
            request.setAttribute("requestSchema", schemaName);
            Study schemaStudy = new Study();
            schemaStudy.setName(name);
            schemaStudy.setUniqueIdentifier(uniqueId);
            schemaStudy.setOc_oid(ocId);
            ((SessionImpl) session).connection().setSchema(schemaName);
            studyDao.save(schemaStudy);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}


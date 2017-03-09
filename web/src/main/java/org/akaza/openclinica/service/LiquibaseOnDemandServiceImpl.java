package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.AsyncStudyHelper;
import org.akaza.openclinica.core.OCMultiTenantSpringLiquibase;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yogi on 2/23/17.
 */
@Service("liquibaseOnDemandService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class LiquibaseOnDemandServiceImpl implements LiquibaseOnDemandService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private ApplicationContext context;
    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Autowired
    private SchemaServiceDao schemaServiceDao;
    public Study process(String schemaName, String name, String uniqueId, String ocId, UserAccountBean ub) {
        Study schemaStudy = null;

        try {
            OCMultiTenantSpringLiquibase liquibase = (OCMultiTenantSpringLiquibase) context.getBean("liquibase");
            List schemas = new ArrayList();
            schemas.add(schemaName);
            liquibase.setSchemas(schemas);
            liquibase.dynamicAfterPropertiesSet(schemas);
            AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Created schema for this protocol", "PENDING");
            AsyncStudyHelper.asyncStudyMap.put(uniqueId, asyncStudyHelper);
        } catch (Exception e) {
            logger.error("Error while creating a liquibase schema:" + schemaName);
            logger.error(e.getMessage(), e);
            return schemaStudy;
        }

        try {
            schemaStudy = new Study();
            schemaStudy.setName(name);
            schemaStudy.setUniqueIdentifier(uniqueId);
            schemaStudy.setOc_oid(ocId);
            schemaStudy.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            schemaServiceDao.setConnectionSchemaName(schemaName);
            Integer studyId = (Integer) studyDao.save(schemaStudy);
            AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Protocol created in the new schema", "PENDING");
            AsyncStudyHelper.asyncStudyMap.put(uniqueId, asyncStudyHelper);
            StudyUserRole studyUserRole = new StudyUserRole();
            StudyUserRoleId userRoleId = new StudyUserRoleId();
            studyUserRole.setId(userRoleId);
            userRoleId.setUserName(ub.getName());
            userRoleId.setOwnerId(ub.getOwnerId());
            userRoleId.setRoleName(Role.COORDINATOR.getName());
            userRoleId.setStudyId(studyId);
            userRoleId.setDateCreated(new Date());
            userRoleId.setStatusId(org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
            studyUserRoleDao.save(studyUserRole);
            logger.info("liquibase: studyId" + studyId);
        } catch (Exception e) {
            logger.error("Error while creating Study and StudyUserRole:" + schemaName);
            logger.error(e.getMessage(), e);
        }
        return schemaStudy;
    }
}


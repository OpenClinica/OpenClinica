package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.AsyncStudyHelper;
import org.akaza.openclinica.controller.helper.ProtocolInfo;
import org.akaza.openclinica.core.OCMultiTenantSpringLiquibase;
import org.akaza.openclinica.core.OCSpringLiquibase;
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
    public Study process(String name, ProtocolInfo protocolInfo, UserAccountBean ub) throws Exception {
        Study schemaStudy = null;

        try {
            List schemas = new ArrayList();
            schemas.add(protocolInfo.getSchema());
            OCMultiTenantSpringLiquibase liquibase = (OCMultiTenantSpringLiquibase) context.getBean("liquibase");
            liquibase.setSchemas(schemas);
            liquibase.dynamicAfterPropertiesSet(schemas);
            AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Created schema for this protocol", "PENDING");
            AsyncStudyHelper.put(protocolInfo.getUniqueStudyId(), asyncStudyHelper);

        } catch (Exception e) {
            logger.error("Error while creating a liquibase schema:" + protocolInfo.getSchema());
            logger.error(e.getMessage(), e);
            throw e;
        }

        try {
            schemaStudy = new Study();
            schemaStudy.setName(name);
            schemaStudy.setUniqueIdentifier(protocolInfo.getUniqueStudyId());
            schemaStudy.setOc_oid(protocolInfo.getOcId());
            schemaStudy.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            schemaServiceDao.setConnectionSchemaName(protocolInfo.getSchema());
            Integer studyId = (Integer) studyDao.save(schemaStudy);
            AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Protocol created in the new schema", "PENDING");
            AsyncStudyHelper.put(protocolInfo.getUniqueStudyId(), asyncStudyHelper);
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
            logger.error("Error while creating Study and StudyUserRole:" + protocolInfo.getSchema());
            logger.error(e.getMessage(), e);
            throw e;
        }
        return schemaStudy;
    }
    public void createPublicTables(ProtocolInfo protocolInfo) throws Exception {
        try {
            List schemas = new ArrayList();
            schemas.add(protocolInfo.getSchema());
            OCSpringLiquibase liquibasePerSchema = (OCSpringLiquibase) context.getBean("liquibaseForeignTables", schemas);
            liquibasePerSchema.processSchemaLiquibase(schemas);
            AsyncStudyHelper asyncStudySchemaHelper = new AsyncStudyHelper("Created foreign tables for this protocol", "PENDING");
            AsyncStudyHelper.put(protocolInfo.getUniqueStudyId(), asyncStudySchemaHelper);
        }  catch (Exception e) {
            logger.error("Error while creating Study and StudyUserRole:" + protocolInfo.getSchema());
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}


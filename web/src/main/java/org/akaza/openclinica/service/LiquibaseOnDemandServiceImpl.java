package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.AsyncStudyHelper;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.core.OCCommonTablesSpringLiquibase;
import org.akaza.openclinica.core.OCMultiTenantSpringLiquibase;
import org.akaza.openclinica.core.OCSpringLiquibase;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
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
    private SchemaServiceDao schemaServiceDao;
    @Autowired
    private UserAccountDao userAccountDao;

    public Study process(StudyInfoObject studyInfoObject, UserAccountBean ub) throws Exception {
        Study schemaStudy = null;

        try {
            List schemas = new ArrayList();
            schemas.add(studyInfoObject.getSchema());
            OCCommonTablesSpringLiquibase commonLiquibase = (OCCommonTablesSpringLiquibase)
                    context.getBean("liquibaseSchemaCommonTables");
            commonLiquibase.processSchemaLiquibase(schemas);
            AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Created common tables for all public and study schemas for this protocol", "PENDING");
            AsyncStudyHelper.put(studyInfoObject.getStudy().getUniqueIdentifier(), asyncStudyHelper);
            OCMultiTenantSpringLiquibase liquibase = (OCMultiTenantSpringLiquibase) context.getBean("liquibase");
            liquibase.setSchemas(schemas);
            liquibase.dynamicAfterPropertiesSet(schemas);
            AsyncStudyHelper asyncStudyHelper2 = new AsyncStudyHelper("Created schema for this protocol", "PENDING");
            AsyncStudyHelper.put(studyInfoObject.getStudy().getUniqueIdentifier(), asyncStudyHelper2);

        } catch (Exception e) {
            logger.error("Error while creating a liquibase schema:" + studyInfoObject.getSchema());
            logger.error(e.getMessage(), e);
            throw e;
        }

        try {
            UserAccount userAccount = userAccountDao.findByUserName(ub.getName());
            userAccount.setActiveStudy(studyInfoObject.getStudy());
            schemaStudy = new Study();
            schemaStudy.setName(studyInfoObject.getStudy().getName());
            schemaStudy.setUniqueIdentifier(studyInfoObject.getStudy().getUniqueIdentifier());
            schemaStudy.setOc_oid(studyInfoObject.getStudy().getOc_oid());
            schemaStudy.setStatus(studyInfoObject.getStudy().getStatus());
            schemaStudy.setDateCreated(new Date());
            schemaStudy.setEnvType(studyInfoObject.getStudy().getEnvType());
            schemaStudy.setStudyEnvSiteUuid(studyInfoObject.getStudy().getStudyEnvSiteUuid());
            schemaStudy.setStudyEnvUuid(studyInfoObject.getStudy().getStudyEnvUuid());
            schemaStudy.setDatePlannedStart(studyInfoObject.getStudy().getDatePlannedStart());
            schemaStudy.setDatePlannedEnd(studyInfoObject.getStudy().getDatePlannedEnd());
            schemaStudy.setExpectedTotalEnrollment((studyInfoObject.getStudy().getExpectedTotalEnrollment()));
            schemaStudy.setProtocolType(studyInfoObject.getStudy().getProtocolType());

            schemaServiceDao.setConnectionSchemaName(studyInfoObject.getSchema());
            studyDao.getCurrentSession().clear();
            int studyId = (Integer) studyDao.save(schemaStudy);
            userAccountDao.saveOrUpdate(userAccount);
            AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Protocol created in the new schema", "PENDING");
            AsyncStudyHelper.put(studyInfoObject.getStudy().getUniqueIdentifier(), asyncStudyHelper);
            logger.info("liquibase: studyId" + studyId);
        } catch (Exception e) {
            logger.error("Error while creating Study and StudyUserRole:" + studyInfoObject.getSchema());
            logger.error(e.getMessage(), e);
            throw e;
        }
        return schemaStudy;
    }
    public void createForeignTables(StudyInfoObject studyInfoObject) throws Exception {
        try {
            List schemas = new ArrayList();
            schemas.add(studyInfoObject.getSchema());
            OCSpringLiquibase liquibasePerSchema = (OCSpringLiquibase) context.getBean("liquibaseForeignTables");
            liquibasePerSchema.processSchemaLiquibase(schemas);
            AsyncStudyHelper asyncStudySchemaHelper = new AsyncStudyHelper("Created foreign tables for this protocol", "PENDING");
            AsyncStudyHelper.put(studyInfoObject.getStudy().getUniqueIdentifier(), asyncStudySchemaHelper);
        }  catch (Exception e) {
            logger.error("Error while creating Study and StudyUserRole:" + studyInfoObject.getSchema());
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}


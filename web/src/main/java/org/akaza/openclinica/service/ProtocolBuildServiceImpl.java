package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.oid.StudyOidGenerator;
import org.akaza.openclinica.controller.helper.ProtocolInfo;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by yogi on 11/10/16.
 */
@Service("protocolBuildService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class ProtocolBuildServiceImpl implements ProtocolBuildService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Autowired
    private SchemaServiceDao schemaServiceDao;

    public ProtocolInfo process(String name, String uniqueId, UserAccountBean ub) throws Exception  {
        String schemaName = null;
        Study study = new Study();

        try {
            int schemaId = schemaServiceDao.getProtocolSchemaSeq();
            study.setName(name);
            study.setUniqueIdentifier(uniqueId);
            // generate OC id
            StudyOidGenerator studyOidGenerator = new StudyOidGenerator();
            study.setOc_oid(studyOidGenerator.generateOid(uniqueId));
            study.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            schemaName = CoreResources.getField("schemaPrefix")+ schemaId;
            study.setSchemaName(schemaName);
            Integer studyId = (Integer) studyDao.save(study);
            StudyUserRole studyUserRole = new StudyUserRole();
            StudyUserRoleId userRoleId = new StudyUserRoleId();
            studyUserRole.setId(userRoleId);
            userRoleId.setUserName(ub.getName());
            userRoleId.setOwnerId(ub.getOwnerId());
            userRoleId.setRoleName(Role.COORDINATOR.getName());
            userRoleId.setStudyId(studyId);
            userRoleId.setStatusId(org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
            userRoleId.setDateCreated(new Date());
            studyUserRoleDao.save(studyUserRole);
        } catch (Exception e) {
            logger.error("Error while creating a study entry in public schema:" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
        createSchema(schemaName);
        return new ProtocolInfo(uniqueId, study.getOc_oid(), schemaName);
    }

    private boolean createSchema(String schemaName) throws Exception {
        try {
           schemaServiceDao.createProtocolSchema(schemaName);
        } catch (Exception e) {
            logger.error("Error while creating a liquibase schema:" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
        return true;
    }
}
package org.akaza.openclinica.service;

import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by yogi on 5/4/17.
 */
@Service("schemaCleanupService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class SchemaCleanupServiceImpl implements SchemaCleanupService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Override
    public void dropSchema(StudyInfoObject studyInfoObject) {
        if (studyInfoObject == null)
            return;
        if (StringUtils.isEmpty(studyInfoObject.getSchema()))
            return;
        logger.info("deleting the schema entry from the mapping table");
        Query query = studyUserRoleDao.getCurrentSession().createQuery("delete from StudyUserRole where id.studyId = :studyId");
        query.setParameter("studyId", studyInfoObject.getStudy().getStudyId());
        int result = query.executeUpdate();

        if (result > 0) {
            logger.info("deleted " + result + " rows from study_user_role table");
        }
        Query queryStudy = studyDao.getCurrentSession().createQuery("delete from Study where studyId = :studyId");
        queryStudy.setParameter("studyId", studyInfoObject.getStudy().getStudyId());
        result = queryStudy.executeUpdate();

        if (result > 0) {
            logger.info("deleted " + result + " rows from study_user_role table");
        }

        Query querySchema = studyDao.getCurrentSession().createNativeQuery("drop schema " + studyInfoObject.getSchema() + " cascade");
        querySchema.executeUpdate();
    }
}

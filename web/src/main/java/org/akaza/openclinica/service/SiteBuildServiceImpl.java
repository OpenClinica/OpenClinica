package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserRole;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.controller.StudyController;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by yogi on 11/10/16.
 */
@Service("siteBuildService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class SiteBuildServiceImpl implements SiteBuildService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private DataSource dataSource;

    public void process(StudyBean parentStudy, StudyBean siteBean, UserAccountBean ownerUserAccount, List<UserRole> userList) throws Exception  {
        String schemaName = null;
        StudyBean site = new StudyBean();

        try {
            StudyController studyController = new StudyController();
            site.setName(siteBean.getName());
            site.setIdentifier(siteBean.getIdentifier());
            // generate OC id
            site.setOid(siteBean.getOid());
            site.setStatus(Status.AVAILABLE);
            site.setOwner(ownerUserAccount);
            site.setParentStudyId(parentStudy.getId());
            site.setUuid(siteBean.getUuid());
            StudyBean createdSite = studyController.createStudyWithDatasource(site, dataSource);
            StudyUserRoleBean sub = null;
        } catch (Exception e) {
            logger.error("Error while creating a site entry" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.controller.StudyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * Created by yogi on 11/10/16.
 */
@Service("siteBuildService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class SiteBuildServiceImpl implements SiteBuildService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private DataSource dataSource;

    public void process(Study parentStudy, Study siteBean, UserAccountBean ownerUserAccount, StudyDao studyDao) throws Exception  {
        String schemaName = null;
        Study site = new Study();

        try {
            StudyController studyController = new StudyController(studyDao);
            site.setName(siteBean.getName());
            site.setUniqueIdentifier(siteBean.getUniqueIdentifier());
            // generate OC id
            site.setOc_oid(siteBean.getOc_oid());
            site.setStatus(siteBean.getStatus());
            site.setProtocolDateVerification(siteBean.getProtocolDateVerification());
            site.setDatePlannedStart(siteBean.getDatePlannedStart());
            site.setUserAccount(ownerUserAccount.toUserAccount(studyDao));
            site.setStudy(parentStudy);
            site.setPublished(parentStudy.isPublished());
            site.setStudyEnvSiteUuid(siteBean.getStudyEnvSiteUuid());
            site.setEnvType(siteBean.getEnvType());
            site.setExpectedTotalEnrollment(siteBean.getExpectedTotalEnrollment());
            site.setPrincipalInvestigator(siteBean.getPrincipalInvestigator());
            site.setFacilityCity(siteBean.getFacilityCity());
            site.setFacilityState(siteBean.getFacilityState());
            site.setFacilityZip(siteBean.getFacilityZip());
            site.setFacilityCountry(siteBean.getFacilityCountry());
            site.setFacilityContactName(siteBean.getFacilityContactName());
            site.setFacilityContactPhone(siteBean.getFacilityContactPhone());
            site.setFacilityContactEmail(siteBean.getFacilityContactEmail());
            Study createdSite = studyController.createStudy(site);
            StudyUserRoleBean sub = null;
        } catch (Exception e) {
            logger.error("Error while creating a site entry" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
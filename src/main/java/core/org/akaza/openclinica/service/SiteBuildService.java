package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yogi on 11/10/16.
 */
public interface SiteBuildService {
    Logger logger = LoggerFactory.getLogger(SiteBuildService.class);
    public void process(Study parentStudy, Study siteBean, UserAccountBean ownerUserAccount, StudyDao studyDao) throws Exception;
}

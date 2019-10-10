package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yogi on 11/10/16.
 */
public interface SiteBuildService {
    Logger logger = LoggerFactory.getLogger(SiteBuildService.class);
    public void process(StudyBean parentStudy, StudyBean siteBean, UserAccountBean ownerUserAccount) throws Exception;
}

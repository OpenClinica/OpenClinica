package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserRole;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yogi on 11/10/16.
 */
public interface SiteBuildService {
    Logger logger = LoggerFactory.getLogger(SiteBuildService.class);
    public void process(StudyBean parentStudy, StudyBean siteBean, UserAccountBean ownerUserAccount, List<UserRole> userList) throws Exception;
}

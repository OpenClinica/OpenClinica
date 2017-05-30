package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yogi on 11/10/16.
 */
public interface StudyBuildService {
    Logger logger = LoggerFactory.getLogger(StudyBuildService.class);
    public StudyInfoObject process(HttpServletRequest request, Study study, UserAccountBean ub) throws Exception ;
}

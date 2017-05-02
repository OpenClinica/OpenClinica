package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.ProtocolInfo;
import org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yogi on 11/10/16.
 */
public interface ProtocolBuildService {
    Logger logger = LoggerFactory.getLogger(ProtocolBuildService.class);
    public ProtocolInfo process(Study study, UserAccountBean ub, String role) throws Exception ;
}

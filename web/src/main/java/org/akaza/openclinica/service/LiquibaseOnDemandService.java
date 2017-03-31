package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.ProtocolInfo;
import org.akaza.openclinica.domain.datamap.Study;

/**
 * Created by yogi on 2/23/17.
 */
public interface LiquibaseOnDemandService {
    public Study process(String name, ProtocolInfo protocolInfo, UserAccountBean ub) throws Exception;
    public void createForeignTables(ProtocolInfo protocolInfo) throws Exception ;

}

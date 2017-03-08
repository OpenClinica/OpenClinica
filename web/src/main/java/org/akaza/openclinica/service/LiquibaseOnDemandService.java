package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.datamap.Study;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yogi on 2/23/17.
 */
public interface LiquibaseOnDemandService {
    public Study process(String schemaName, String name, String uniqueId, String ocId, UserAccountBean ub);
}

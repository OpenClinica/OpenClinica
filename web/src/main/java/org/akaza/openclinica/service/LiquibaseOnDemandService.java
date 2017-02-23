package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yogi on 2/23/17.
 */
public interface LiquibaseOnDemandService {
    public void process(String schemaName, String name, String uniqueId, String ocId, HttpServletRequest request,
            HttpServletResponse response);
}

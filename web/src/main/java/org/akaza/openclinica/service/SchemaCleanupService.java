package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.ProtocolInfo;
import org.akaza.openclinica.domain.datamap.Study;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yogi on 5/4/17.
 */
public interface SchemaCleanupService {
    void dropSchema(ProtocolInfo protocolInfo);
}


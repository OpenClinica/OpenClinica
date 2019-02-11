package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.UserAccountHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

/**
 * Created by yogi on 5/3/17.
 */
public interface CallbackService {
    UserAccountHelper isCallbackSuccessful(HttpServletRequest request, KeycloakUser user) throws Exception;
    UserAccountBean getUpdatedUser(UserAccountBean ub);
    void updateParticipateModuleStatus(String accessToken, String studyOid);
    }


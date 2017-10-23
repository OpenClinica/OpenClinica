package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.UserAccountHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yogi on 5/3/17.
 */
public interface CallbackService {
    UserAccountHelper isCallbackSuccessful(HttpServletRequest request, Auth0User user) throws Exception;
    UserAccountBean getUpdatedUser(UserAccountBean ub);
}


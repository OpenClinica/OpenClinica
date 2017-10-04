package org.akaza.openclinica.config;

import com.auth0.SessionUtils;
import org.akaza.openclinica.controller.Auth0Controller;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by yogi on 7/20/17.
 */
public class OCLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
    @Autowired Auth0Controller controller;

    public OCLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        String returnTo = (String) SessionUtils.get(request, Auth0Controller.RETURN_TO);
        if (returnTo == null) {
            returnTo = request.getRequestURI();
            SessionUtils.set(request, Auth0Controller.RETURN_TO, returnTo);
        }
        return this.getLoginFormUrl();
    }
}
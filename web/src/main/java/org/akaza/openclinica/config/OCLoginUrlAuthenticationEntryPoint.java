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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.regex.Pattern;

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
        Enumeration<String> parameterNames = request.getParameterNames();
        String queryStr = "";
        if (isUrlSuitableToCreateCookie(request) == false)
            return this.getLoginFormUrl();

        while (parameterNames.hasMoreElements()) {
            String element = parameterNames.nextElement();
            if (StringUtils.isEmpty(queryStr))
                queryStr += "?" + element + "=" + request.getParameter(element);
            else
                queryStr += "&" + element + "=" + request.getParameter(element);
        }
        Cookie cookie = null;
        try {
            cookie = new Cookie("queryStr", URLEncoder.encode(request.getRequestURL() + queryStr, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        cookie.setPath("/");
        cookie.setMaxAge(60);
        response.addCookie(cookie);

        return this.getLoginFormUrl();
    }

    protected boolean isUrlSuitableToCreateCookie(HttpServletRequest request) {
        if (Pattern.matches(".*(css|jpg|png|gif|js|htm|html)$", request.getRequestURL()))
            return false;
        return true;
    }
}
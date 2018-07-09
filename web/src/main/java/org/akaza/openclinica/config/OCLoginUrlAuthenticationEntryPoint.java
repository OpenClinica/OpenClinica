package org.akaza.openclinica.config;

import org.akaza.openclinica.bean.core.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * Created by yogi on 7/20/17.
 */
public class OCLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public OCLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        return getLoginFormUrl(request);
    }

    public String getLoginFormUrl(HttpServletRequest request) {
        String paramsString = "";
        try {
            paramsString = Utils.getParamsString(request.getParameterMap());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String loginUrl = getLoginFormUrl()  + "?" + paramsString;
        return loginUrl;
    }
}
package org.akaza.openclinica.config;

import org.akaza.openclinica.bean.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger= LoggerFactory.getLogger(OCLoginUrlAuthenticationEntryPoint.class);
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
            logger.error("Encoding is not UTF-8: ",e);
        }
        String loginUrl = getLoginFormUrl()  + "?" + paramsString;
        return loginUrl;
    }
}
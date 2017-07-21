package org.akaza.openclinica.config;

import org.apache.commons.lang.StringUtils;
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
    public OCLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException,
            ServletException {
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        Enumeration<String> parameterNames = request.getParameterNames();
        String queryStr = "";
        while (parameterNames.hasMoreElements()) {
            String element = parameterNames.nextElement();
            if (StringUtils.isEmpty(queryStr))
                queryStr += "?" + element + "=" + request.getParameter(element);
            else
                queryStr += "&" + element + "=" + request.getParameter(element);
        }
        redirectStrategy.sendRedirect(request, response, getLoginFormUrl() + queryStr);
    }
}

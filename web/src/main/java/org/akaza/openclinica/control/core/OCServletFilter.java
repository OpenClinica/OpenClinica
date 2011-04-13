/**
 * 
 */
package org.akaza.openclinica.control.core;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.log.LoggingConstants;
import org.slf4j.MDC;

/**
 * @author pgawade
 *
 */
public class OCServletFilter implements javax.servlet.Filter {

    public static final String USER_BEAN_NAME = "userBean";

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        UserAccountBean ub = (UserAccountBean) req.getSession().getAttribute(USER_BEAN_NAME);
        boolean successfulRegistration = false;
        String username = "";

        Principal principal = req.getUserPrincipal();

        if ((ub != null) && (null != ub.getName()) && (!ub.getName().equals(""))) {
            username = ub.getName();
            successfulRegistration = registerUsernameWithLogContext(username);
        } else if (principal != null) {
            username = principal.getName();
            successfulRegistration = registerUsernameWithLogContext(username);
        }

        try {
            chain.doFilter(request, response);
          } finally {
            if (successfulRegistration) {
                MDC.remove(LoggingConstants.USERNAME);
            }
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

    public void destroy() {
    }

    /**
     * Register the user in the MDC under USERNAME.
     * 
     * @param username
     * @return true id the user can be successfully registered
     */
    private boolean registerUsernameWithLogContext(String username) {
        if (username != null && username.trim().length() > 0) {
            MDC.put(LoggingConstants.USERNAME, username);
            return true;
        }
        return false;
    }

}

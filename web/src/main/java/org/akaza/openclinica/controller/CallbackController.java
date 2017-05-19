package org.akaza.openclinica.controller;

import com.auth0.Auth0User;
import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import com.auth0.spring.security.mvc.Auth0CallbackHandler;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.service.CallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by krikorkrumlian on 3/9/17.
 */

@Controller
public class CallbackController extends Auth0CallbackHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected HttpSession session;
    public static final String USER_BEAN_NAME = "userBean";
    private String realm = "Protected";
    @Autowired CallbackService callbackService;

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        SessionUtils.setState(req, "nonce=123456");
        NonceUtils.addNonceToStorage(req);
        super.handle(req, res);
        Auth0User user = SessionUtils.getAuth0User(req);
        UserAccountBean ub = callbackService.isCallbackSuccessful(req, user);
        if (ub != null) {
            req.getSession().setAttribute(USER_BEAN_NAME, ub);
        } else {
            unauthorized(res, "Bad credentials");
            return;
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

}
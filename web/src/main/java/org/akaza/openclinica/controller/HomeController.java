package org.akaza.openclinica.controller;

import com.auth0.Auth0User;
import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import com.auth0.spring.security.mvc.Auth0Config;
import org.akaza.openclinica.config.SsoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by krikorkrumlian on 3/30/17.
 */
@Controller
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Auth0Config auth0Config;
    private SsoConfig ssoConfig;

    @Autowired
    public HomeController(final Auth0Config auth0Config, final SsoConfig ssoConfig) {
        this.auth0Config = auth0Config;
        this.ssoConfig = ssoConfig;
    }

    @RequestMapping(value="/home", method = RequestMethod.GET)
    protected String home(final Map<String, Object> model, final HttpServletRequest req) {
        logger.info("Home page");
        // add Nonce to storage
        SessionUtils.setState(req, "nonce=123456");
        NonceUtils.addNonceToStorage(req);
        setupModel(model, req);
        return "home";
    }

    /**
     * required attributes needed in request for view presentation
     */
    private void setupModel(final Map<String, Object> model, final HttpServletRequest req) {
        // null if no user exists..
        final Auth0User user = SessionUtils.getAuth0User(req);
        model.put("user", user);
        model.put("domain", auth0Config.getDomain());
        model.put("clientId", auth0Config.getClientId());
        model.put("loginCallback", auth0Config.getLoginCallback());
        model.put("loginRedirectOnFail", auth0Config.getLoginRedirectOnFail());
        model.put("state", SessionUtils.getState(req));
        // sso config specific
        model.put("logoutEndpoint", ssoConfig.getLogoutEndpoint());
        model.put("connection", ssoConfig.getConnection());
        model.put("partnerLoginUrl", ssoConfig.getPartnerLoginUrl());
    }
}

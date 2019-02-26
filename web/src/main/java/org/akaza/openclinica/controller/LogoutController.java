package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.config.AppConfig;
import org.akaza.openclinica.core.EventCRFLocker;
import org.akaza.openclinica.service.LogoutService;
import org.akaza.openclinica.view.Page;
import org.keycloak.authorization.client.AuthzClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by krikorkrumlian on 3/30/17.
 */
@Controller(value = "logoutController")
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired LogoutService logoutService;
    @Autowired AppConfig config;
    @Autowired
    EventCRFLocker eventCRFLocker;

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    protected void home(final Map<String, Object> model, final HttpServletRequest req, final HttpServletResponse response) {
        String authUrl = getLogoutUri(req, false);
        HttpSession session = req.getSession();
        logger.debug("Logout page");
        resetSession(session);
        String redirectUri = getRedirectUri(req, false);
        try {
            req.logout();
            response.sendRedirect(authUrl);
        } catch (Exception e) {
            logger.error("Error logging out:", e);
        }
    }

    private String getRedirectUri(HttpServletRequest req, boolean callback) {
        int port = req.getServerPort();
        String portStr ="";
        if (port != 80 && port != 443) {
            portStr = ":" + port;
        }
        String redirectUri = req.getScheme() + "://" + req.getServerName() + portStr + req.getContextPath();
        if (callback) {
            redirectUri += "/pages/login";
        } else {
            redirectUri += "/MainMenu";
        }
        return redirectUri;
    }

    private String getLogoutUri(HttpServletRequest req, boolean callback) {
        AuthzClient authzClient = AuthzClient.create();
        String coreAuthUrl = authzClient.getConfiguration().getAuthServerUrl();
        String redirectUri = getRedirectUri(req, callback);
        String authUrl = null;
        String action = null;
        if (callback)
            action = "auth";
        else
            action = "logout";
        try {
            authUrl = coreAuthUrl + "/realms/" + authzClient.getConfiguration().getRealm()
                    + "/protocol/openid-connect/" + action + "?redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
            if (callback) {
                authUrl += "&client_id=bridge&response_type=code";
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Encoding redirect URI:" + redirectUri, e);
        }
        return authUrl;
    }

    @RequestMapping(value="/logoutSuccess", method = RequestMethod.GET)
    protected String logout(final HttpServletRequest request, final HttpServletResponse response) {
        int index = request.getRequestURL().indexOf(request.getContextPath());
        String returnURL = request.getRequestURL().substring(0, index)
                + request.getContextPath() + Page.MENU_SERVLET.getFileName();
        String param = "";
        if (request.getParameter("studyEnvUuid") != null) {
            param = "?studyEnvUuid=" + request.getParameter("studyEnvUuid");
        }
        logger.info("/logoutSuccess:" + returnURL + param);
        return "redirect:" + returnURL + param;
    }



    @RequestMapping(value="/invalidateKeycloakToken", method = RequestMethod.GET)
    public void invalidateAccessToken(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        String authUrl = getLogoutUri(request, true);
        response.sendRedirect(authUrl);
    }

    @RequestMapping(value="/resetFirstLogin", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void resetFirstLogin(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        final HttpSession session = request.getSession();
        logger.error("**********Resetting first time to false**********");
        session.setAttribute("firstLoginCheck", "false");
    }

    private void resetSession(HttpSession session) {
        // first release all locks
        UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
        if (ub != null) {
            eventCRFLocker.unlockAllForUser(ub.getId());
        }
        session.invalidate();
        SecurityContextHolder.clearContext();
    }
}
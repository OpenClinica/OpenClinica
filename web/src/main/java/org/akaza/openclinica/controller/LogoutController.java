package org.akaza.openclinica.controller;

import org.akaza.openclinica.config.AppConfig;
import org.akaza.openclinica.service.LogoutService;
import org.akaza.openclinica.view.Page;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
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
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by krikorkrumlian on 3/30/17.
 */
@Controller(value = "logoutController")
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Auth0Controller controller;
    @Autowired LogoutService logoutService;
    @Autowired AppConfig config;
    @RequestMapping(value="/logout", method = RequestMethod.GET)
    protected String home(final Map<String, Object> model, final HttpServletRequest req) {
        HttpSession session = req.getSession();
        logger.info("Logout page");
        resetSessionAttributes(session);
        session.invalidate();
        String urlPrefix = req.getRequestURL().substring(0, req.getRequestURL().lastIndexOf("/"));
        int index = urlPrefix.indexOf(req.getContextPath());
        String returnURL = urlPrefix.substring(0, index).concat(req.getContextPath()).concat("/pages/logoutSuccess");
        String logoutURL = controller.buildLogoutURL(req, returnURL);
        return String.format("redirect:%s", logoutURL);
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
        logger.info("/logoutSuccess" + returnURL + param);
        return "redirect:" + returnURL + param;
    }

    @RequestMapping(value="/invalidateAuth0Token", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void invalidateAccessToken(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("Invalidating token");
            auth.setAuthenticated(false);
            final HttpSession session = request.getSession();
            resetSessionAttributes(session);
            response.sendRedirect(controller.buildAuthorizeUrl(request, true));
        }
    }

    @RequestMapping(value="/resetFirstLogin", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void resetFirstLogin(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        final HttpSession session = request.getSession();
        logger.error("**********Resetting first time to false**********");
        //String redirectURL = request.getParameter("redirectURL");
        session.setAttribute("firstLoginCheck", false);
        //response.sendRedirect(URLDecoder.decode(redirectURL, "UTF-8"));
    }

    private void resetSessionAttributes(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.removeAttribute("userBean");
        session.removeAttribute("study");
        session.removeAttribute("publicStudy");
        session.removeAttribute("userRole");
        session.removeAttribute("passwordExpired");
    }
}
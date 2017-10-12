package org.akaza.openclinica.controller;

import com.auth0.SessionUtils;
import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import org.akaza.openclinica.config.AppConfig;
import org.akaza.openclinica.config.TokenAuthentication;
import org.akaza.openclinica.service.LogoutService;
import org.akaza.openclinica.view.Page;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by krikorkrumlian on 3/30/17.
 */
@Controller
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
        session.removeAttribute("userBean");
        session.removeAttribute("study");
        session.removeAttribute("userRole");
        session.removeAttribute("passwordExpired");
        SecurityContextHolder.clearContext();
        session.invalidate();

        String urlPrefix = req.getRequestURL().substring(0, req.getRequestURL().lastIndexOf("/"));
        int index = urlPrefix.indexOf(req.getContextPath());
        String returnURL = urlPrefix.substring(0, index).concat(req.getContextPath()).concat("/pages/logoutSuccess");
        return String.format("redirect:%s", controller.buildLogoutURL(returnURL));
    }

    @RequestMapping(value="/logoutSuccess", method = RequestMethod.GET)
    protected String logout(final HttpServletRequest request, HttpServletResponse response) {
        String returnToCookie = null;
        try {
            returnToCookie = URLDecoder.decode(logoutService.getReturnToCookie(request, response), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error parsing returnToCookie:" + e.getMessage());
        }
        int index = request.getRequestURL().indexOf(request.getContextPath());
        String returnURL = request.getRequestURL().substring(0, index);
        if (StringUtils.isEmpty(returnToCookie))
            returnToCookie = request.getContextPath() + Page.MENU_SERVLET.getFileName();
        return "redirect:" + returnURL + returnToCookie;
    }

    @RequestMapping(value="/invalidateSession", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    protected void invalidateAccessToken(final Map<String, Object> model, final HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null)
            auth.setAuthenticated(false);
    }
}

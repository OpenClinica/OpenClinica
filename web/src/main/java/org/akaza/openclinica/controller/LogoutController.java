package org.akaza.openclinica.controller;

import com.auth0.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by krikorkrumlian on 3/30/17.
 */
@Controller
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Auth0Controller controller;

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
        String returnTo = (String) SessionUtils.get(req, Auth0Controller.RETURN_TO);

        String urlPrefix = req.getRequestURL().substring(0, req.getRequestURL().lastIndexOf("/"));
        String returnURL = String.format("%s/%s", urlPrefix, "logoutSuccess");
        return String.format("redirect:%s", controller.buildLogoutURL(returnURL));
    }

    @RequestMapping(value="/logoutSuccess", method = RequestMethod.GET)
    protected String logout(final Map<String, Object> model, final HttpServletRequest req) {
        return "login/logout";
    }

    @RequestMapping(value="/invalidateSession", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    protected void invalidateSession(final Map<String, Object> model, final HttpServletRequest req) {
        HttpSession session = req.getSession();
        logger.info("Logout page");
        session.removeAttribute("userBean");
        session.removeAttribute("study");
        session.removeAttribute("userRole");
        session.removeAttribute("passwordExpired");
        SecurityContextHolder.clearContext();
        session.invalidate();
    }
}

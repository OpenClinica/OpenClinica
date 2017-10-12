package org.akaza.openclinica.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Controller public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired private Auth0Controller controller;

    @PermitAll @RequestMapping(value = "/login", method = RequestMethod.GET) protected void login(final HttpServletRequest req, HttpServletResponse res) {
        logger.debug("Performing login");
        String authorizeUrl = controller.buildAuthorizeUrl(req, true);
        try {
            res.sendRedirect(authorizeUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

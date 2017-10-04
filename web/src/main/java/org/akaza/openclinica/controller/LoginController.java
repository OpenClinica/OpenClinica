package org.akaza.openclinica.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class LoginController {

    @Autowired
    private Auth0Controller controller;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PermitAll
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    protected void login(final HttpServletRequest req, HttpServletResponse res) {
        logger.debug("Performing login");
        String authorizeUrl = controller.buildAuthorizeUrl(req, true);
        try {
            res.sendRedirect(authorizeUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    KeycloakController keycloakController;
    @PermitAll @RequestMapping(value = "/ocLogin", method = RequestMethod.GET) protected void login(final HttpServletRequest req, HttpServletResponse res) {
        logger.debug("Performing login");
        String authorizeUrl = "/OpenClinica";
        int port = req.getServerPort();
        String portStr ="";
        if (port != 80 && port != 443) {
            portStr = ":" + port;
        }
        String redirectUri = keycloakController.buildAuthorizeUrl(req);
        try {
            res.sendRedirect(redirectUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

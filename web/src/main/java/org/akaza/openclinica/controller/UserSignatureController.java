package org.akaza.openclinica.controller;

import com.auth0.IdentityVerificationException;
import com.auth0.InvalidRequestException;
import com.auth0.Tokens;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.akaza.openclinica.config.AppConfig;
import org.akaza.openclinica.service.LogoutService;
import org.akaza.openclinica.view.Page;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Controller(value = "signController")
public class UserSignatureController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Auth0Controller controller;
    @Autowired LogoutService logoutService;
    @Autowired AppConfig config;
    @RequestMapping(value="/userSignature", method = RequestMethod.GET)
    protected void signIn(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
        String authorizeUrl = controller.buildAuthorizeSignatureUrl(req, false);
        res.sendRedirect(authorizeUrl);
    }
    @RequestMapping(value="/signatureCallback", method = RequestMethod.GET)
    protected void signatureCallback(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
        try {
            String error = req.getParameter("error");
            if (error != null && error.equals("login_required")) {
                System.out.println("Login failed");
            } else {
                Tokens tokens = controller.handle(req);
                DecodedJWT decodedJWT = JWT.decode(tokens.getAccessToken());
            }
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (IdentityVerificationException e) {
            e.printStackTrace();
        }
    }
}
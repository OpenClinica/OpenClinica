package org.akaza.openclinica.controller;

import com.auth0.IdentityVerificationException;
import com.auth0.InvalidRequestException;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.sf.json.util.JSONUtils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.config.TokenAuthentication;
import org.akaza.openclinica.controller.helper.UserAccountHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.service.Auth0User;
import org.akaza.openclinica.service.CallbackService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.akaza.openclinica.control.core.SecureController.USER_BEAN_NAME;

@SuppressWarnings("unused")
@Controller
public class CallbackController {

    @Autowired
    private Auth0Controller controller;
    @Autowired
    CallbackService callbackService;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final String redirectOnFail;
    private final String redirectOnSuccess;
    private String realm = "Protected";

    public CallbackController() {
        this.redirectOnFail = "/error";
        this.redirectOnSuccess = "/portal/home";
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void getCallback(final HttpServletRequest req, final HttpServletResponse res) throws Exception {
        handle(req, res);
    }

    @RequestMapping(value = "/callback", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    protected void postCallback(final HttpServletRequest req, final HttpServletResponse res) throws Exception {
        handle(req, res);
    }

    private void handle(HttpServletRequest req, HttpServletResponse res) throws Exception {
        try {
            String error = req.getParameter("error");
            if (error != null && (error.equals("login_required") || error.equals("unauthorized"))) {
                res.sendRedirect(controller.buildAuthorizeUrl(req, false /* don't do SSO, SSO already failed */));
            } else {
                Tokens tokens = controller.handle(req);
                DecodedJWT decodedJWT = JWT.decode(tokens.getAccessToken());

                TokenAuthentication tokenAuth = new TokenAuthentication(decodedJWT);
                SecurityContextHolder.getContext().setAuthentication(tokenAuth);
                CoreResources.setRequestSchema(req, "public");
                req.getSession().setAttribute("accessToken", tokens.getAccessToken());
                Auth0User user = new Auth0User(decodedJWT);
                UserAccountHelper userAccountHelper = null;
                try {
                    userAccountHelper = callbackService.isCallbackSuccessful(req, user);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    throw e;
                }
                UserAccountBean ub = userAccountHelper.getUb();
                if (ub != null) {
                    if (userAccountHelper.isUpdated()) {
                        ub = callbackService.getUpdatedUser(ub);
                    }
                    req.getSession().removeAttribute("userRole");
                    req.getSession().setAttribute(USER_BEAN_NAME, ub);
                    logger.debug("Setting firstLoginCheck to true");
                    req.getSession().setAttribute("firstLoginCheck", "true");
                } else {
                    unauthorized(res, "Bad credentials");
                    return;
                }
                String returnTo = controller.getReturnTo(req);
                String state = req.getParameter("state");
                String param = "";
                JSONObject jsonObject;
                if (JSONUtils.mayBeJSON(state)) {
                    jsonObject = new JSONObject(state);

                    Object newJSON;
                    try {
                        newJSON = jsonObject.get("studyEnvUuid");
                        logger.debug(newJSON.toString());
                        param = "?studyEnvUuid=" + newJSON.toString();
                    } catch (JSONException e) {
                        logger.debug("studyEnvUuid is not in the state");
                    }

                    try {
                        newJSON = jsonObject.get("forceRenewAuth");
                        logger.debug(newJSON.toString());
                        if (StringUtils.isEmpty(param))
                            param += "?";
                        else
                            param += "&";
                        param += "forceRenewAuth=" + newJSON.toString();
                    } catch (JSONException e) {
                        logger.debug("forceRenewAuth is not in the state");
                    }

                }
                if (returnTo == null) returnTo = this.redirectOnSuccess;
                res.sendRedirect(returnTo + param);
            }
        } catch (InvalidRequestException e) {
            e.printStackTrace();
            SecurityContextHolder.clearContext();
            res.sendRedirect(req.getContextPath());
        } catch (IdentityVerificationException e) {
            e.printStackTrace();
            SecurityContextHolder.clearContext();
            res.sendRedirect(redirectOnFail);
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

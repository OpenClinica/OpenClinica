package org.akaza.openclinica.controller;

import com.auth0.IdentityVerificationException;
import com.auth0.InvalidRequestException;
import com.auth0.Tokens;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.sf.json.util.JSONUtils;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.config.TokenAuthentication;
import org.akaza.openclinica.controller.helper.UserAccountHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.service.Auth0User;
import org.akaza.openclinica.service.CallbackService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

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
        this.redirectOnSuccess = "/MainMenu";
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
            if (error != null) {
                if (error.equals("login_required")) {
                    String authorizeUrl = controller.buildAuthorizeUrl(req, false/* don't do SSO, SSO already failed */);
                    logger.info("CallbackController In login_required:%%%%%%%%" + authorizeUrl);
                    res.sendRedirect(authorizeUrl);
                } else if (error.equals("unauthorized")) {
                    logger.info("CallbackController In unauthorized:%%%%%%%%");
                    String smURL = CoreResources.getField("smURL");
                    int lastIndex = smURL.lastIndexOf('/');
                    String unauthURl = smURL.substring(0, lastIndex) + "/error-user-deactivated";
                    res.sendRedirect(unauthURl);
                }
            } else {
                logger.info("CallbackController In not login_required:%%%%%%%%");
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
                    logger.error("UserAccountHelper:", e);
                    throw e;
                }
                UserAccountBean ub = userAccountHelper.getUb();
                if (ub != null) {
                    if (userAccountHelper.isUpdated()) {
                        ub = callbackService.getUpdatedUser(ub);
                    }
                    req.getSession().setAttribute(USER_BEAN_NAME, ub);
                    refreshUserRole(req, ub);
                    logger.info("Setting firstLoginCheck to true");
                    req.getSession().setAttribute("firstLoginCheck", "true");
                    logger.info("CallbackController set firstLoginCheck to true:%%%%%%%%");
                } else {
                    logger.error("UserAccountBean ub ");
                    unauthorized(res, "Bad credentials");
                    return;
                }
                String returnTo = controller.getReturnTo(req);
                String state = req.getParameter("state");
                String param = "";
                JSONObject jsonObject;
                if (JSONUtils.mayBeJSON(state)) {
                    jsonObject = new JSONObject(state);

                    Set<String> keySet = jsonObject.keySet();
                    Object newJSON;
                    for (String key : keySet) {
                        logger.debug(key);
                        newJSON = jsonObject.get(key);

                        if (StringUtils.isEmpty(param))
                            param += "?";
                        else
                            param += "&";
                        param += key + "=" + newJSON.toString();

                    }
                }
                if (StringUtils.isEmpty(returnTo) || StringUtils.equals(returnTo, "/OpenClinica"))
                    returnTo = this.redirectOnSuccess;
                logger.info("CallbackController returnTo URL:%%%%%%%%" + returnTo);
                logger.info("param:" + param);

                res.sendRedirect(returnTo + param);
            }
        } catch (InvalidRequestException e) {
            logger.error("CallbackController:" + e);
            SecurityContextHolder.clearContext();
            res.sendRedirect(req.getContextPath());
        } catch (IdentityVerificationException e) {
            logger.error("CallbackController:" + e);
            SecurityContextHolder.clearContext();
            res.sendRedirect(redirectOnFail);
        }
    }

    private void refreshUserRole(HttpServletRequest req, UserAccountBean ub) {
        StudyUserRoleBean roleByStudy = ub.getRoleByStudy(ub.getActiveStudyId());
        req.getSession().setAttribute("userRole", roleByStudy);

    }
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

}

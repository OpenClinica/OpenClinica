package org.akaza.openclinica.controller;

import net.sf.json.util.JSONUtils;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.service.CallbackService;
import core.org.akaza.openclinica.service.CustomRuntimeException;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.security.Principal;
import java.util.Set;

@SuppressWarnings("unused")
@Controller
public class CallbackController {


    @Autowired
    CallbackService callbackService;
    @Autowired
    KeycloakController keycloakController;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final String redirectOnFail;
    private final String redirectOnSuccess;
    @Autowired private DataSource dataSource;
    @Autowired private UserAccountController userAccountController;
    @Autowired private StudyBuildService studyBuildService;
    @Autowired private UserAccountDAO userAccountDAO;

    public CallbackController() {
        this.redirectOnFail = "/error";
        this.redirectOnSuccess = "/MainMenu";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    protected void getCallback(final HttpServletRequest req, final HttpServletResponse res) throws Exception {
        handle(req, res);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    protected void postCallback(final HttpServletRequest req, final HttpServletResponse res) throws Exception {
        handle(req, res);
    }

    private void handle(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String error = req.getParameter("error");
        if (error != null && error.equals("unauthorized")) {
            logger.info("CallbackController In unauthorized:%%%%%%%%");
            String sbsUrl = CoreResources.getField("SBSBaseUrl");
            String unauthURl = sbsUrl + "/#/error-user-deactivated";
            res.sendRedirect(unauthURl);
            return;
        }
        final Principal userPrincipal = req.getUserPrincipal();

        if (userPrincipal == null) {
            String authorizeUrl = keycloakController.buildAuthorizeUrl(req);
            logger.info("CallbackController In login_required:%%%%%%%%" + authorizeUrl);
            res.sendRedirect(authorizeUrl);
        } else if (userPrincipal instanceof KeycloakAuthenticationToken) {
            String ocUserUuid = null;
            try {
                ocUserUuid = keycloakController.getOcUserUuid(req);
            } catch (CustomRuntimeException e) {
                res.sendError(401, e.getMessage());
                return;
            }
            String returnTo = keycloakController.getReturnTo(req);
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

    }

    public RefreshableKeycloakSecurityContext getKeycloakSecurityContext() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes()).getRequest();
        if (request.getAttribute(KeycloakSecurityContext.class.getName()) != null) {
            RefreshableKeycloakSecurityContext context = (RefreshableKeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
            return context;
        }
        return null;
    }
}

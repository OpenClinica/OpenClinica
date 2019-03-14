package org.akaza.openclinica.controller;

import net.sf.json.util.JSONUtils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.controller.helper.UserAccountHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.CallbackService;
import org.akaza.openclinica.service.KeycloakUser;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Map;

import static org.akaza.openclinica.control.core.SecureController.USER_BEAN_NAME;

@Component
public class KeycloakController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    CallbackService callbackService;
    @Autowired
    StudyDao studyDao;
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    private StudyDAO studyDAO;

    public String buildAuthorizeUrl(HttpServletRequest request) {
        AuthzClient authzClient = AuthzClient.create();
        String coreAuthUrl = authzClient.getConfiguration().getAuthServerUrl();
        int port = request.getServerPort();
        String portStr ="";
        if (port != 80 && port != 443) {
            portStr = ":" + port;
        }
        String redirectUri = request.getScheme() + "://" + request.getServerName() + portStr + request.getContextPath() + "/MainMenu";
        String authUrl = coreAuthUrl + "/realms/" + authzClient.getConfiguration().getRealm()
                + "/protocol/openid-connect/auth?scope=openid&client_id=" + authzClient.getConfiguration().getResource() +
                "&response_type=code&login=true&redirect_uri=" + redirectUri;
        JSONObject json = null;
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            switch ((entry.getKey())) {

                case "forceRenewAuth":
                    if (json == null)
                        json = new JSONObject();
                    json.put(entry.getKey(), entry.getValue()[0]);
                    UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
                    if (ub != null && StringUtils.isNotEmpty(ub.getName()))
                        json.put("loggedUser", ub.getName());
                    break;
                case "state":
                    if (JSONUtils.mayBeJSON(entry.getValue()[0])) {
                        json = new JSONObject(entry.getValue()[0]);
                        try {
                            json.remove("forceRenewAuth");
                        } catch (Exception e) {
                            logger.error("State parameter:", e);
                        }
                    }
                    break;
                default:
                    if (json == null)
                        json = new JSONObject();
                    json.put(entry.getKey(), entry.getValue()[0]);
                    break;
            }
        };
        if (json != null)
        {
            try {
                authUrl += "&state=" + URLEncoder.encode(json.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("Url encoding:", e);
            }
        }
        return authUrl;
    }
    public String getReturnTo(HttpServletRequest req) {
        return "/OpenClinica/MainMenu";
    }

    public String getOcUserUuid(HttpServletRequest req) throws Exception {
        String ocUserUuid = null;
        final Principal userPrincipal = req.getUserPrincipal();
        if (userPrincipal == null)
            return ocUserUuid;
        KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) ((KeycloakAuthenticationToken) userPrincipal).getPrincipal();
        AccessToken token = kp.getKeycloakSecurityContext().getToken();
        req.getSession().setAttribute("accessToken", kp.getKeycloakSecurityContext().getTokenString());
        KeycloakUser user = new KeycloakUser(token);

        String userType = (String) user.getUserContext().get("userType");
        if (userType.equals(org.akaza.openclinica.service.UserType.PARTICIPATE.getName())) {
            ocUserUuid = (String) user.getUserContext().get("username");
        } else {
            ocUserUuid = (String) user.getUserContext().get("userUuid");
        }

        logger.debug("%%%%%%%%%%%In KeycloakController :getOcUserUuid: " + ocUserUuid);
        String requestSchema = CoreResources.getRequestSchema(req);
        CoreResources.setRequestSchema(req, "public");

        UserAccountHelper userAccountHelper;
        UserAccountBean prevUser = (UserAccountBean) req.getSession().getAttribute(USER_BEAN_NAME);
        if (prevUser == null || StringUtils.isEmpty(prevUser.getName())) {
            logger.info("Setting firstLoginCheck to true");
            req.getSession().setAttribute("firstLoginCheck", "true");
            logger.info("CallbackController set firstLoginCheck to true:%%%%%%%%");
        }
        try {
            userAccountHelper = callbackService.isCallbackSuccessful(req, user);
        } catch (Exception e) {
            logger.error("UserAccountHelper:", e);
            throw e;
        } finally {
            CoreResources.setRequestSchema(req, requestSchema);
        }
        UserAccountBean ub = userAccountHelper.getUb();
        if (ub != null && StringUtils.isNotEmpty(ub.getName())) {
            if (userAccountHelper.isUpdated()) {
                ub = callbackService.getUpdatedUser(ub);
            }
            req.getSession().setAttribute(USER_BEAN_NAME, ub);

            // Public study will be null if there is no active study for this user
            if (ub.getActiveStudyId() != 0) {

                StudyBean publicStudy = null;
                publicStudy = (StudyBean) req.getSession().getAttribute("publicStudy");

                if (publicStudy == null) {
                    studyDAO = new StudyDAO(dataSource);
                    publicStudy = studyDAO.findByPublicPK(ub.getActiveStudyId());
                }

                String accessToken = (String) req.getSession().getAttribute("accessToken");
                callbackService.updateParticipateModuleStatus(accessToken, publicStudy.getOid());

                SecureController.refreshUserRole(req, ub, CoreResources.getPublicStudy(publicStudy.getOid(),dataSource));
            }

        } else {
            logger.error("UserAccountBean ub is null");

        }
        return ocUserUuid;

    }
}
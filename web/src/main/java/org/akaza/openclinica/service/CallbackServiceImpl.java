package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.LinkedTreeMap;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.UserAccountController;
import org.akaza.openclinica.controller.helper.UserAccountHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yogi on 5/3/17.
 */
@Service("callbackService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class CallbackServiceImpl implements CallbackService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Autowired private DataSource dataSource;
    @Autowired private UserAccountController userAccountController;
    @Autowired private StudyBuildService studyBuildService;
    @Autowired private UserAccountDAO userAccountDAO;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public UserAccountHelper isCallbackSuccessful(HttpServletRequest request, Auth0User user) throws Exception {
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        String _username = user.getNickname();
        logger.info("Callback for user:" + _username);
        if (StringUtils.isEmpty(_username))
            return null;
        Map<String, Object> userContextMap = getUserContextMap(user);
        String userUuid = (String) userContextMap.get("userUuid");
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserUuid(userUuid);
        if (StringUtils.isEmpty(ub.getName())) {
            ub = (UserAccountBean) userAccountDAO.findByUserName(_username);
        } else {
            ub.setName(_username);
            userAccountDAO.update(ub);
            updateStudyUsername(ub, user);
        }
        if (ub.getId() == 0) {
            ub = createUserAccount(request, user, userContextMap);
        }
        boolean isUserUpdated = updateStudyUserRoles(request, ub, user, userContextMap);
        return new UserAccountHelper(ub, isUserUpdated);
    }

    public UserAccountBean getUpdatedUser(UserAccountBean ub) {
        return (UserAccountBean) userAccountDAO.findByUserName(ub.getName());
    }

    @Modifying
    private void updateStudyUsername(UserAccountBean ub, Auth0User user) {
        Map<String, Object> appMetadata = user.getAppMetadata();
        Query query=studyUserRoleDao.getCurrentSession().createQuery("update StudyUserRole set id.userName=:userName where id.userName=:prevUser");
        logger.info("update StudyUserRole set id.userName=" + user.getNickname() + " where id.userName=" + user.getUserId());
        query.setParameter("userName", user.getNickname());
        query.setParameter("prevUser", user.getUserId());
        int modifications=query.executeUpdate();
        logger.info(modifications + " studyUserRole rows have been updated from user:" + ub.getName() + " to user:" + user.getNickname());
    }

    public Map<String, Object> getUserContextMap(Auth0User user)  throws Exception {
        Map<String, Object> appMetadata = user.getAppMetadata();
        Object userContext = appMetadata.get("userContext");
        String toJson = objectMapper.writeValueAsString(userContext);
        Map<String, Object> userContextMap = objectMapper.readValue(toJson, Map.class);
        return userContextMap;
    }
    @Modifying
    private boolean updateStudyUserRoles(HttpServletRequest request, UserAccountBean ub, Auth0User user, Map<String, Object> userContextMap) throws Exception {
        request.getSession().setAttribute("userContextMap", userContextMap);
        return studyBuildService.saveStudyEnvRoles(request, ub);
    }

    private UserAccountBean createUserAccount(HttpServletRequest request, Auth0User user, Map<String, Object> userContextMap ) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", user.getNickname());
        if (StringUtils.isNotEmpty(user.getGivenName()))
            map.put("fName", user.getGivenName());
        else
            map.put("fName", "first");
        if (StringUtils.isNotEmpty(user.getFamilyName()))
            map.put("lName", user.getFamilyName());
        else
            map.put("lName", "last");

        map.put("role_name", "Data Manager");
        map.put("user_uuid", (String) userContextMap.get("userUuid"));
        Map<String, Object> appMetadata = user.getAppMetadata();
        Object userType = appMetadata.get("userContext");
        LinkedTreeMap<String, String> userTypeMap = (LinkedTreeMap<String, String>) userType;
        String convertedUserType = null;
        switch ((String) userTypeMap.get("userType")) {
        case "Business Admin":
            convertedUserType = UserType.SYSADMIN.getName();
            break;
        case "Tech Admin":
            convertedUserType = UserType.TECHADMIN.getName();
            break;
        case "User":
            convertedUserType = UserType.USER.getName();
            break;
        default:
            String error = "Invalid userType:" + (String) userTypeMap.get("userType");
            logger.error(error);
            throw new Exception(error);
        }
        map.put("user_type", convertedUserType);
        map.put("authorize_soap", "true");
        map.put("email", user.getEmail());
        map.put("institution", "OC");
        CoreResources.setRootUserAccountBean(request, dataSource);
        userAccountController.createOrUpdateAccount(request, map);
        return (UserAccountBean) request.getAttribute("createdUaBean");
    }
}


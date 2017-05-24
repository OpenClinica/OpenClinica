package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import com.google.gson.internal.LinkedTreeMap;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.UserAccountController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
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
    @Autowired DataSource dataSource;
    @Autowired UserAccountController userAccountController;

    @Override
    public UserAccountBean isCallbackSuccessful(HttpServletRequest request, Auth0User user) throws Exception {
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        String _username = user.getNickname();

        if (StringUtils.isEmpty(_username))
            return null;
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(user.getUserId());
        if (StringUtils.isEmpty(ub.getName())) {
            ub = (UserAccountBean) userAccountDAO.findByUserName(_username);
        } else {
            ub.setName(_username);
            userAccountDAO.update(ub);
            updateStudyUsedRoles(ub, user);
        }
        if (ub.getId() != 0)
            return ub;
        else {
            return createUserAccount(request, user);
        }
    }

    @Modifying
    private void updateStudyUsedRoles(UserAccountBean ub, Auth0User user) {
        Query query=studyUserRoleDao.getCurrentSession().createQuery("update StudyUserRole set id.userName=:userName where id.userName=:prevUser");
        query.setParameter("userName", user.getNickname());
        query.setParameter("prevUser", user.getUserId());
        int modifications=query.executeUpdate();
        logger.info(modifications + " studyUserRole rows have been updated from user:" + ub.getName() + " to user:" + user.getNickname());
    }

    private UserAccountBean createUserAccount(HttpServletRequest request, Auth0User user) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", user.getNickname());
        map.put("fName", "first");
        map.put("lName", "last");
        map.put("role_name", "Data Manager");
        Map<String, Object> appMetadata = user.getAppMetadata();
        Object userType = appMetadata.get("userContext");
        LinkedTreeMap<String, String> userTypeMap = (LinkedTreeMap<String, String>) userType;
        switch ((String) userTypeMap.get("userType")) {
        case "Business Admin":
            map.put("user_type", UserType.SYSADMIN.getName());
            break;
        }
        map.put("authorize_soap", "false");
        map.put("email", user.getEmail());
        map.put("institution", "OC");
        CoreResources.setRootUserAccountBean(request, dataSource);
        userAccountController.createOrUpdateAccount(request, map);
        return (UserAccountBean) request.getAttribute("createdUaBean");
    }
}


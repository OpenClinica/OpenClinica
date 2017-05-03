package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import org.akaza.openclinica.bean.login.UserAccountBean;
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

    @Override
    public UserAccountBean isCallbackSuccessful(HttpServletRequest request, Auth0User user) {
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        String _username = user.getNickname();
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserName(user.getUserId());
        if (StringUtils.isEmpty(ub.getName())) {
            ub = (UserAccountBean) userAccountDAO.findByUserName(_username);
        } else {
            ub.setName(user.getNickname());
            userAccountDAO.update(ub);
            updateStudyUsedRoles(ub, user);
        }
        if (StringUtils.isNotEmpty(_username) && ub.getId() != 0)
            return ub;

        return null;

    }

    @Modifying
    private void updateStudyUsedRoles(UserAccountBean ub, Auth0User user) {
        Query query=studyUserRoleDao.getCurrentSession().createQuery("update StudyUserRole set id.userName=:userName where id.userName=:prevUser");
        query.setParameter("userName", user.getNickname());
        query.setParameter("prevUser", user.getUserId());
        int modifications=query.executeUpdate();
        logger.info(modifications + " studyUserRole rows have been updated from user:" + ub.getName() + " to user:" + user.getNickname());
    }
}


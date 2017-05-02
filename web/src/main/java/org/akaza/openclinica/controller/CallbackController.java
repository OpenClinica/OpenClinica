package org.akaza.openclinica.controller;

import com.auth0.Auth0User;
import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import com.auth0.spring.security.mvc.Auth0CallbackHandler;
import liquibase.util.StringUtils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * Created by krikorkrumlian on 3/9/17.
 */

@Controller
public class CallbackController extends Auth0CallbackHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected HttpSession session;
    public static final String USER_BEAN_NAME = "userBean";
    private String realm = "Protected";
    @Autowired
    private DataSource dataSource;
    @Autowired StudyUserRoleDao studyUserRoleDao;

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);

        SessionUtils.setState(req, "nonce=123456");
        NonceUtils.addNonceToStorage(req);
        super.handle(req, res);
        Auth0User user = SessionUtils.getAuth0User(req);
        String _username = user.getNickname();
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserName(user.getUserId());
        if (StringUtils.isEmpty(ub.getName())) {
            ub = (UserAccountBean) userAccountDAO.findByUserName(_username);
        } else {
            ub.setName(user.getNickname());
            userAccountDAO.update(ub);
            updateStudyUsedRoles(ub, user);
        }
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(user.getUserId());
        ub.setName(user.getNickname());
        userAccountDAO.update(ub);
        if (!_username.equals("") && ub.getId() != 0) {
            req.getSession().setAttribute(USER_BEAN_NAME, ub);
        } else {
            unauthorized(res, "Bad credentials");
            return;
        }
    }

    @Modifying
    private void updateStudyUsedRoles(UserAccountBean ub, Auth0User user) {
        Transaction transaction = studyUserRoleDao.getCurrentSession().beginTransaction();
        Query query=studyUserRoleDao.getCurrentSession().createQuery("update StudyUserRole set id.userName=:userName where id.userName=:prevUser");
        query.setParameter("userName", user.getNickname());
        query.setParameter("prevUser", user.getUserId());
        int modifications=query.executeUpdate();
        logger.info(modifications + " studyUserRole rows have been updated from user:" + ub.getName() + " to user:" + user.getNickname());
        transaction.commit();
    }
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

}
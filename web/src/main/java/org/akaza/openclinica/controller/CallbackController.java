package org.akaza.openclinica.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.auth0.Auth0User;
/**
 * Created by krikorkrumlian on 3/9/17.
 */
import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import com.auth0.spring.security.mvc.Auth0CallbackHandler;

@Controller
public class CallbackController extends Auth0CallbackHandler {
    protected HttpSession session;
    public static final String USER_BEAN_NAME = "userBean";
    private String realm = "Protected";
    @Autowired
    private DataSource dataSource;

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);

        SessionUtils.setState(req, "nonce=123456");
        NonceUtils.addNonceToStorage(req);
        super.handle(req, res);
        Auth0User user = SessionUtils.getAuth0User(req);
        String _username = user.getNickname();
        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserName(user.getUserId());
        ub.setName(user.getNickname());
        userAccountDAO.update(ub);
        if (!_username.equals("") && ub.getId() != 0) {
            req.getSession().setAttribute(USER_BEAN_NAME, ub);
        } else {
            unauthorized(res, "Bad credentials");
            return;
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
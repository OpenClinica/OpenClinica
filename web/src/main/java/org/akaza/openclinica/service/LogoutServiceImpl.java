package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by yogi on 10/7/17.
 */
@Service("logoutService")
public class LogoutServiceImpl implements LogoutService {

    public String getReturnToCookie(HttpServletRequest request, HttpServletResponse response) {
        String returnTo = "";
        HttpSession session = request.getSession();
        UserAccountBean userBean = (UserAccountBean) session.getAttribute(SecureController.USER_BEAN_NAME);
        if (userBean == null)
            return returnTo;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("returnTo-" + userBean.getName())) {
                returnTo = cookie.getValue();
                cookie.setValue(null);
                cookie.setMaxAge(30);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }
        return returnTo;
    }
}

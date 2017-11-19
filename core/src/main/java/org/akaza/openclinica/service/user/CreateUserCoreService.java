package org.akaza.openclinica.service.user;

import org.akaza.openclinica.bean.login.UserAccountBean;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface CreateUserCoreService {
    UserAccountBean createUser(HttpServletRequest request, HashMap<String, String> map) throws Exception;
}

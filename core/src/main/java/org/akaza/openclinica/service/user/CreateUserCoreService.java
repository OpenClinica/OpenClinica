package org.akaza.openclinica.service.user;

import org.akaza.openclinica.bean.login.UserAccountBean;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public interface CreateUserCoreService {
    UserAccountBean createUser(HttpServletRequest request, Map<String, String> map) throws Exception;
}

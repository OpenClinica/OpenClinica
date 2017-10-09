package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yogi on 10/7/17.
 */
public interface LogoutService {
    String getReturnToCookie(HttpServletRequest request, HttpServletResponse response);
}

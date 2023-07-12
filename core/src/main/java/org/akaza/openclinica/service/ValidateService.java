package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

public interface ValidateService {
    /**
    * This function checks whether the user is study-level or site-level
    * @param request;
    * @return true or false;
    **/
    boolean isStudyLevelUser(HttpServletRequest request);
}

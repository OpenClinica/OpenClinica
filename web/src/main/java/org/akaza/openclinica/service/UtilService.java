/**
 *
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.datamap.Study;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author joekeremian
 *
 */
public interface UtilService {

    String getAccessTokenFromRequest(HttpServletRequest request);

    void setSchemaFromStudyOid(String studyOid);

    String getCustomerUuidFromRequest(HttpServletRequest request);

    UserAccountBean getUserAccountFromRequest(HttpServletRequest request);

    }
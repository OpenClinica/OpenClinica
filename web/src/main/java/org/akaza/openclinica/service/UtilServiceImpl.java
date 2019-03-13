package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "utilService" )
public class UtilServiceImpl implements UtilService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;

    @Autowired
    RestfulServiceHelper restfulServiceHelper;


    public String getAccessTokenFromRequest(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("accessToken");
    }

    public void setSchemaFromStudyOid(String studyOid) {
        CoreResources.setRequestSchemaByStudy(studyOid,dataSource);
    }

    public String getCustomerUuidFromRequest(HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        return (String) userContextMap.get("customerUuid");
    }

  public UserAccountBean getUserAccountFromRequest(HttpServletRequest request){
      return getRestfulServiceHelper().getUserAccount(request);
  }


    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource);
        }
        return restfulServiceHelper;
    }

}
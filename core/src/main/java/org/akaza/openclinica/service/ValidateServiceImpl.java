package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

public class ValidateServiceImpl implements ValidateService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String USER_BEAN_NAME = "userBean";
    public static final String STUDY_NAME = "study";

    public boolean isStudyLevelUser(HttpServletRequest request){
        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute(STUDY_NAME);
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
        ArrayList studyUserRoles = ub.getRoles();
        boolean isStudyLevelUser = true;
        for (int i = 0; i < studyUserRoles.size(); i++) {
            StudyUserRoleBean studyUserRole = (StudyUserRoleBean) studyUserRoles.get(i);
            if (studyUserRole.getStudyId() == currentStudy.getId() && currentStudy.getParentStudyId() > 0) {
                isStudyLevelUser = false;
                break;
            }
        }
        return isStudyLevelUser;
    }

}

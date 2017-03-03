package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.OCMultiTenantSpringLiquibase;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.akaza.openclinica.control.core.SecureController.USER_BEAN_NAME;

/**
 * Created by yogi on 2/23/17.
 */
@Service("liquibaseOnDemandService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class LiquibaseOnDemandServiceImpl implements LiquibaseOnDemandService {
    @Autowired
    ApplicationContext context;
    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    public Study process(String schemaName, String name, String uniqueId, String ocId, HttpServletRequest request) {
        Study schemaStudy = null;
        Session session = studyDao.getSessionFactory().getCurrentSession();

        try {
            OCMultiTenantSpringLiquibase liquibase = (OCMultiTenantSpringLiquibase) context.getBean("liquibase");
            List schemas = new ArrayList();
            schemas.add(schemaName);
            liquibase.setSchemas(schemas);
            liquibase.dynamicAfterPropertiesSet(schemas);
        } catch (Exception e) {
            System.out.println("Error while creating a schema error 3");
            e.printStackTrace();
            return schemaStudy;
        }
        try {
            request.setAttribute("requestSchema", schemaName);
            schemaStudy = new Study();
            schemaStudy.setName(name);
            schemaStudy.setUniqueIdentifier(uniqueId);
            schemaStudy.setOc_oid(ocId);
            schemaStudy.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            ((SessionImpl) session).connection().setSchema(schemaName);
            Integer studyId = (Integer) studyDao.save(schemaStudy);
            HttpSession httpSession = request.getSession();
            if (httpSession == null) {
                System.out.println("Session cannot be null");
            }
            UserAccountBean ub = (UserAccountBean) httpSession.getAttribute(USER_BEAN_NAME);
            StudyUserRole studyUserRole = new StudyUserRole();
            StudyUserRoleId userRoleId = new StudyUserRoleId();
            studyUserRole.setId(userRoleId);
            userRoleId.setUserName(ub.getName());
            userRoleId.setOwnerId(ub.getOwnerId());
            userRoleId.setRoleName(Role.COORDINATOR.getName());
            userRoleId.setStudyId(studyId);
            userRoleId.setDateCreated(new Date());
            userRoleId.setStatusId(org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
            studyUserRoleDao.save(studyUserRole);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schemaStudy;
    }
}


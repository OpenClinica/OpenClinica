package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.oid.StudyOidGenerator;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by yogi on 11/10/16.
 */
@Service("studyBuildService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class StudyBuildServiceImpl implements StudyBuildService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Autowired
    private SchemaServiceDao schemaServiceDao;

    public StudyInfoObject process(HttpServletRequest request, Study study, UserAccountBean ub) throws Exception  {
        String schemaName = null;

        try {
            int schemaId = schemaServiceDao.getProtocolSchemaSeq();
            study.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            study.setDateCreated(new Date());
            schemaName = CoreResources.getField("schemaPrefix")+ schemaId;
            study.setSchemaName(schemaName);
            Integer studyId = (Integer) studyDao.save(study);
            saveStudyEnvRoles(request, ub);
        } catch (Exception e) {
            logger.error("Error while creating a study entry in public schema:" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
        createSchema(schemaName);
        return new StudyInfoObject(schemaName, study);
    }


    private ArrayList getRoles() {
        ArrayList roles = Role.toArrayList();
        roles.remove(Role.ADMIN);

        return roles;
    }

    private String getOCRole(String givenRole, boolean siteFlag) {
        ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();
        String key = null;
        for (Iterator it = getRoles().iterator(); it.hasNext(); ) {
            Role role = (Role) it.next();
            switch (role.getId()) {
            case 2:
                key = "Study_Coordinator";
                break;
            case 3:
                key = "Study_Director";
                break;
            case 4:
                key = "Investigator";
                break;
            case 5:
                key = "Data_Entry_Person";
                break;
            case 6:
                key = "Monitor";
                break;
            default:
                break;
            // logger.info("No role matched when setting role description");
            }
            String value = resterm.getString(key).trim();
            if (StringUtils.equals(givenRole, value))
                return Role.getByDesc(value).getName();
        }
        return null;
    }
    public void saveStudyEnvRoles(HttpServletRequest request, UserAccountBean ub) throws Exception {
        LinkedHashMap<String, Object> userContextMap = (LinkedHashMap<String, Object>)request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return;
        ArrayList<LinkedHashMap<String, String>> roles =  (ArrayList<LinkedHashMap<String, String>>) userContextMap.get("roles");
        if (roles == null)
            return;
        for (LinkedHashMap<String, String> roleByStudy : roles) {
            Study study = studyDao.findByStudyEnvUuid(roleByStudy.get("studyEnvUuid"));
            if (study == null)
                continue;
            Study parentStudy = study.getStudy();
            int parentStudyId = parentStudy == null ? study.getStudyId() : study.getStudy().getStudyId();
            UserAccount userAccount = new UserAccount();
            userAccount.setUserName(ub.getName());
            ArrayList<StudyUserRole> byUserAccount = studyUserRoleDao.findAllUserRolesByUserAccount(userAccount, study.getStudyId(), parentStudyId);
            String role = roleByStudy.get("roleName");
            String ocRole = getOCRole(role, false);
            if (byUserAccount.isEmpty()) {
                StudyUserRole studyUserRole = new StudyUserRole();
                StudyUserRoleId userRoleId = new StudyUserRoleId();
                studyUserRole.setId(userRoleId);
                userRoleId.setUserName(ub.getName());
                studyUserRole.setOwnerId(ub.getOwnerId());

                studyUserRole.setRoleName(ocRole);

                userRoleId.setStudyId(study.getStudyId());
                studyUserRole.setStatusId(org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
                studyUserRole.setDateCreated(new Date());
                studyUserRoleDao.saveOrUpdate(studyUserRole);
            } else {
                for (StudyUserRole sur : byUserAccount) {
                    if (sur.getRoleName().equals(ocRole))
                        continue;
                    else {
                        sur.setRoleName(ocRole);
                        sur.setDateUpdated(new Date());
                        StudyUserRole studyUserRole = studyUserRoleDao.saveOrUpdate(sur);
                    }
                }
            }
        }
    }

    private boolean createSchema(String schemaName) throws Exception {
        try {
           schemaServiceDao.createProtocolSchema(schemaName);
        } catch (Exception e) {
            logger.error("Error while creating a liquibase schema:" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
        return true;
    }
}
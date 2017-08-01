package org.akaza.openclinica.service;

import com.auth0.Auth0User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.oid.StudyOidGenerator;
import org.akaza.openclinica.controller.helper.OCUserDTO;
import org.akaza.openclinica.controller.helper.StudyEnvironmentRoleDTO;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Autowired
    private SchemaServiceDao schemaServiceDao;
    @Autowired
    private UserAccountDao userAccountDao;

    public StudyInfoObject process(HttpServletRequest request, Study study, UserAccountBean ub) throws Exception  {
        String schemaName = null;
        boolean isUserUpdated = false;
        try {
            int schemaId = schemaServiceDao.getProtocolSchemaSeq();
            study.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
            study.setDateCreated(new Date());
            schemaName = CoreResources.getField("schemaPrefix")+ schemaId;
            study.setSchemaName(schemaName);
            Integer studyId = (Integer) studyDao.save(study);
            isUserUpdated = saveStudyEnvRoles(request, ub);
        } catch (Exception e) {
            logger.error("Error while creating a study entry in public schema:" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
        createSchema(schemaName);
        return new StudyInfoObject(schemaName, study, ub, isUserUpdated);
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

    public boolean processSpecificStudyEnvUuid(HttpServletRequest request, int userActiveStudyId, UserAccount ub) {
        boolean allUserRolesUpdated =false;
        HttpSession session = request.getSession();
        String studyEnvUuid = (String) request.getParameter("studyEnvUuid");
        if (StringUtils.isEmpty(studyEnvUuid))
            return allUserRolesUpdated;

        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyBean currentPublicStudy = studyDAO.findByStudyEnvUuid(studyEnvUuid);
        Study userStudy = studyDao.findByStudyEnvUuid(studyEnvUuid);
        if (currentPublicStudy == null) {
            return allUserRolesUpdated;
        }

        int parentStudyId = currentPublicStudy.getParentStudyId() > 0 ? currentPublicStudy.getParentStudyId() : currentPublicStudy.getId();
        if (ub.getActiveStudy() != null  && ub.getActiveStudy().getStudyId() == parentStudyId)
            return allUserRolesUpdated;

        // check to see if the user has a role for this study
        ArrayList<StudyUserRole> userRoles = studyUserRoleDao.findAllUserRolesByUserAccount(ub, currentPublicStudy.getId(), parentStudyId);
        if (userRoles.isEmpty()) {
            updateStudyUserRoles(request, ub, userActiveStudyId);
            allUserRolesUpdated= true;
            userRoles = studyUserRoleDao.findAllUserRolesByUserAccount(ub, currentPublicStudy.getId(), parentStudyId);
            if (userRoles.isEmpty()) {
                logger.error("Sorry you do not have a user role for this study:" + currentPublicStudy.getStudyEnvUuid());
                return allUserRolesUpdated;
            }
        }
        StudyUserRoleBean studyUserRoleBean = new StudyUserRoleBean();
        ub.setActiveStudy(userStudy);
        userAccountDao.saveOrUpdate(ub);

        session.setAttribute("study", null);
        session.setAttribute("publicStudy", null);
        return allUserRolesUpdated;
    }


    public boolean saveStudyEnvRoles(HttpServletRequest request, UserAccountBean ubIn) throws Exception {
        UserAccount ub = userAccountDao.findByUserId(ubIn.getId());
        boolean studyUserRoleUpdated = false;
        int userActiveStudyId;

        if (ub.getActiveStudy() == null)
            userActiveStudyId = 0;
        else
            userActiveStudyId = ub.getActiveStudy().getStudyId();
        boolean allUserRolesUpdated = processSpecificStudyEnvUuid(request, userActiveStudyId, ub);

        if(allUserRolesUpdated && ub.getActiveStudy().getStudyId() != 0)
            return true;
        studyUserRoleUpdated = updateStudyUserRoles(request, ub, userActiveStudyId);
        if (ub.getActiveStudy().getStudyId() == 0) {
            throw new Exception("Your study has not been published yet.");
        }
        if (studyUserRoleUpdated) {
            return true;
        } else
            return false;
    }
    private boolean updateStudyUserRoles(HttpServletRequest request, UserAccount ub, int userActiveStudyId) {
        boolean studyUserRoleUpdated = false;
        ResponseEntity<StudyEnvironmentRoleDTO[]> userRoles = getUserRoles(request);
        for (StudyEnvironmentRoleDTO role: userRoles.getBody()) {
            Study study = studyDao.findByStudyEnvUuid(role.getStudyEnvironmentUuid());
            if (study == null)
                continue;
            Study parentStudy = study.getStudy();
            Study toUpdate = parentStudy == null ? study : study.getStudy();
            // set this as the active study
            if (ub.getActiveStudy() == null) {
                ub.setActiveStudy(toUpdate);
                userAccountDao.saveOrUpdate(ub);
            }
            UserAccount userAccount = new UserAccount();
            userAccount.setUserName(ub.getUserName());
            ArrayList<StudyUserRole> byUserAccount = studyUserRoleDao.findAllUserRolesByUserAccount(userAccount, study.getStudyId(), toUpdate.getStudyId());
            String rolename = role.getRoleName();
            String ocRole = getOCRole(rolename, false);
            if (byUserAccount.isEmpty()) {
                StudyUserRole studyUserRole = new StudyUserRole();
                StudyUserRoleId userRoleId = new StudyUserRoleId();
                studyUserRole.setId(userRoleId);
                userRoleId.setUserName(ub.getUserName());
                studyUserRole.setOwnerId(ub.getUserId());

                studyUserRole.setRoleName(ocRole);

                userRoleId.setStudyId(study.getStudyId());
                studyUserRole.setStatusId(org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
                studyUserRole.setDateCreated(new Date());
                studyUserRoleDao.saveOrUpdate(studyUserRole);
                if (userActiveStudyId == toUpdate.getStudyId())
                    studyUserRoleUpdated = true;
            } else {
                for (StudyUserRole sur : byUserAccount) {
                    if (sur.getRoleName().equals(ocRole))
                        continue;
                    else {
                        sur.setRoleName(ocRole);
                        sur.setDateUpdated(new Date());
                        StudyUserRole studyUserRole = studyUserRoleDao.saveOrUpdate(sur);
                        if (userActiveStudyId == toUpdate.getStudyId())
                            studyUserRoleUpdated = true;
                    }
                }
            }
        }
        return studyUserRoleUpdated;
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

    public ResponseEntity getUserRoles(HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = CoreResources.getField("SBSUrl") + userUuid + "/roles";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<StudyEnvironmentRoleDTO[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, StudyEnvironmentRoleDTO[].class);
        return response;
    }

    public ResponseEntity getUserDetails (HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = CoreResources.getField("SBSUrl") + userUuid;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<OCUserDTO> response = restTemplate.exchange(uri, HttpMethod.GET, entity, OCUserDTO.class);
        return response;
    }
    public void updateStudyUsername(UserAccountBean ub, Auth0User user) {
        int numUpdated = studyUserRoleDao.updateUsername(user.getNickname(), user.getUserId());
        logger.debug(numUpdated + " studyUserRoles updated for user:" + user.getNickname() + " and prevUser:" + user.getUserId());
    }
}
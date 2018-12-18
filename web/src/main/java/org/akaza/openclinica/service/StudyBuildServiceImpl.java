package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.sf.json.util.JSONUtils;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.dto.StudyEnvironmentDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.enumsupport.ModuleStatus;
import org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    public static final String ACTIVE = "active";
    public static final String PARTICIPATE = "participate";


    PermissionService permissionService;
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

    @Autowired
    private RestfulServiceHelper serviceHelper;
    public StudyBuildServiceImpl(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public StudyInfoObject process(HttpServletRequest request, Study study, UserAccountBean ub) throws Exception  {
        boolean isUserUpdated;

        /***************************************** BEWARE***************************************************************
         Postgres always creates schema in lowercase. If you try to setSchema to a connectxion with an uppercase schema name, it won't work.
         Always change the schemaName to lowercase
         *
         */
        String schemaName = study.getOc_oid().replaceAll("\\(", "").replaceAll("\\)", "").toLowerCase();
        try {
            study.setStatus(study.getStatus());
            study.setDateCreated(new Date());
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
        ArrayList roles = Role.toStudyArrayList();

        return roles;
    }

    private String getOCRole(String givenRole, boolean siteFlag) {
        ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();
        String key = null;
        if (siteFlag) {
            String value = (String) Role.sbsSiteRoleMap.get(givenRole);
            if (StringUtils.isNotEmpty(value))
                return value;
            return "invalid";
        } else {
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
                    return Role.getByRoleDesc(value).getName();
            }
        }
        return null;
    }

    private String processAuth0State(HttpServletRequest req) {
        String state = req.getParameter("state");
        String param = null;
        JSONObject jsonObject;
        if (JSONUtils.mayBeJSON(state)) {
            jsonObject = new JSONObject(state);

            Set<String> keySet = jsonObject.keySet();
            Object newJSON;
            for (String key : keySet) {
                logger.debug(key);
                newJSON = jsonObject.get(key);

                if (StringUtils.equals(key, "studyEnvUuid"))
                   return newJSON.toString();
            }
        }
        return null;
    }
    public boolean processSpecificStudyEnvUuid(HttpServletRequest request, int userActiveStudyId, UserAccount ub) {
        boolean studyEnvUuidProcessed =false;
        HttpSession session = request.getSession();
        String studyEnvUuid = (String) request.getParameter("studyEnvUuid");
        if (StringUtils.isEmpty(studyEnvUuid)) {
            studyEnvUuid = processAuth0State(request);
            if (StringUtils.isEmpty(studyEnvUuid))
                return studyEnvUuidProcessed;
        }
        updateStudyUserRoles(request, ub, userActiveStudyId, studyEnvUuid);

        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyBean currentPublicStudy = studyDAO.findByStudyEnvUuid(studyEnvUuid);
        Study userStudy = studyDao.findByStudyEnvUuid(studyEnvUuid);
        if (currentPublicStudy == null) {
            return studyEnvUuidProcessed;
        }

        int parentStudyId = currentPublicStudy.getParentStudyId() > 0 ? currentPublicStudy.getParentStudyId() : currentPublicStudy.getId();
        if (ub.getActiveStudy() != null  && ub.getActiveStudy().getStudyId() == parentStudyId)
            return studyEnvUuidProcessed;

        // check to see if the user has a role for this study
        ArrayList<StudyUserRole> userRoles = studyUserRoleDao.findAllUserRolesByUserAccountAndStudy(ub, currentPublicStudy.getId());
        if (userRoles.isEmpty()) {
            logger.error("Sorry you do not have a user role for this study:" + currentPublicStudy.getStudyEnvUuid());
            studyEnvUuidProcessed = true;
            return studyEnvUuidProcessed;
        }
        StudyUserRoleBean studyUserRoleBean = new StudyUserRoleBean();
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        UserAccountBean jdbcUb = (UserAccountBean) userAccountDAO.findByUserName(ub.getUserName());
        ArrayList userRoleBeans = (ArrayList) userAccountDAO.findAllRolesByUserName(ub.getUserName());
        jdbcUb.setRoles(userRoleBeans);
        session.setAttribute(SecureController.USER_BEAN_NAME, jdbcUb);

        ub.setActiveStudy(userStudy);
        userAccountDao.saveOrUpdate(ub);

        session.setAttribute("study", null);
        session.setAttribute("publicStudy", null);
        studyEnvUuidProcessed = true;
        return studyEnvUuidProcessed;
    }
    @Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
    public UserAccount getUserAccountObject(UserAccountBean ubIn) {
        UserAccount byUserId = userAccountDao.findByUserId(ubIn.getId());
        return byUserId;
    }
    public boolean saveStudyEnvRoles(HttpServletRequest request, UserAccountBean ubIn) throws Exception {
        UserAccount ub = userAccountDao.findByUserId(ubIn.getId());
        boolean studyUserRoleUpdated = false;
        int userActiveStudyId;

        if (ub.getActiveStudy() == null)
            userActiveStudyId = 0;
        else
            userActiveStudyId = ub.getActiveStudy().getStudyId();
        boolean studyEnvUuidProcessed = processSpecificStudyEnvUuid(request, userActiveStudyId, ub);

        if(studyEnvUuidProcessed)
            return true;
        studyUserRoleUpdated = updateStudyUserRoles(request, ub, userActiveStudyId, null);
        if (ub.getActiveStudy() == null) {
            logger.error("There are no studies or this user has no studies avaiable");
            throw new CustomRuntimeException("There are no studies or this user has no studies avaiable", null);
        } else if (ub.getActiveStudy().getStudyId() == 0) {
            throw new Exception("You have no roles for this study.");
        }
        if (studyUserRoleUpdated) {
            return true;
        } else
            return false;
    }
    private void removeDeletedUserRoles(ArrayList<StudyUserRole> modifiedStudyUserRoles, Collection<StudyUserRole> existingStudyUserRoles) {
        existingStudyUserRoles.removeIf(existingStudyUserRole -> modifiedStudyUserRoles.stream().anyMatch(
                modifiedStudyUserRole -> existingStudyUserRole.getId().getStudyId().equals(modifiedStudyUserRole.getId().getStudyId())));
        existingStudyUserRoles.forEach(studyToDelete -> studyUserRoleDao.getCurrentSession().delete(studyToDelete));
    }

    private boolean checkIfParentExists(HttpServletRequest request, Study study, List<StudyEnvironmentRoleDTO> roles) {
        StudyEnvironmentRoleDTO role = roles.stream().filter(s -> s.getStudyEnvironmentUuid().equals(study.getStudy().getStudyEnvUuid())).findAny()
                .orElse(null);
        if (role != null) {
            request.getSession().setAttribute("customUserRole", role.getDynamicRoleName());
            return true;
        }
        return false;
    }

    @Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
    public boolean updateStudyUserRoles(HttpServletRequest request, UserAccount ub, int userActiveStudyId, String altStudyEnvUuid) {
        boolean studyUserRoleUpdated = false;
        HttpSession session = request.getSession();
        String currentSchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema(request, "public");

        ResponseEntity<List<StudyEnvironmentRoleDTO>> userRoles = getUserRoles(request);

        if (userRoles == null) {
            CoreResources.setRequestSchema(currentSchema);
            return studyUserRoleUpdated;
        }
        Collection<StudyUserRole> existingStudyUserRoles = studyUserRoleDao.findAllUserRolesByUserAccount(ub);
        ArrayList<StudyUserRole> modifiedSURArray = new ArrayList<>();
        boolean currentActiveStudyValid = false;
        Study placeHolderStudy = null;
        Study activeStudy = null;
        if (userActiveStudyId > 0) {
            activeStudy = studyDao.findById(userActiveStudyId);
        }
        for (StudyEnvironmentRoleDTO role: userRoles.getBody()) {
            String uuidToFind = null;
            boolean siteFlag = false;
            if (StringUtils.isNotEmpty(role.getSiteUuid())) {
                uuidToFind = role.getSiteUuid();
                siteFlag = true;
            } else
                uuidToFind = role.getStudyEnvironmentUuid();

            Study study = studyDao.findByStudyEnvUuid(uuidToFind);

            if (study == null)
                continue;
            boolean parentExists = false;
            if (siteFlag) {
                // see if the parent is in this list. If found, assign the custom role of the parent
                parentExists = checkIfParentExists(request, study, userRoles.getBody());
            } else if (activeStudy != null && activeStudy.getStudy() != null && activeStudy.getStudy().getStudyId() > 0) {
                parentExists = activeStudy.getStudy().getStudyId() == study.getStudyId();
            }
            if (StringUtils.isNotEmpty(altStudyEnvUuid)) {
                if (uuidToFind.equals(altStudyEnvUuid)) {
                    request.getSession().setAttribute("altCustomUserRole", role.getDynamicRoleName());
                }
            }
            if ((study.getStudyId() == userActiveStudyId) || parentExists) {
                currentActiveStudyValid = true;
                session.setAttribute("customUserRole", role.getDynamicRoleName());
                session.setAttribute("baseUserRole", role.getRoleName());

            }

            Study parentStudy = study.getStudy();
            Study toUpdate = parentStudy == null ? study : study.getStudy();
            // set this as the active study
            if (ub.getActiveStudy() == null) {
                ub.setActiveStudy(toUpdate);
                userAccountDao.saveOrUpdate(ub);
                currentActiveStudyValid = true;
                if (!parentExists)
                    request.getSession().setAttribute("customUserRole", role.getDynamicRoleName());
            }
            placeHolderStudy = study;
            UserAccount userAccount = new UserAccount();
            userAccount.setUserName(ub.getUserName());
            ArrayList<StudyUserRole> byUserAccount = studyUserRoleDao.findAllUserRolesByUserAccountAndStudy(userAccount, study.getStudyId());
            String rolename = role.getRoleName();
            String ocRole = getOCRole(rolename, parentStudy != null ? true: false);
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
                    if (sur.getRoleName() != null
                            && sur.getRoleName().equals(ocRole)) {
                        modifiedSURArray.add(sur);
                        continue;
                    } else {
                        sur.setRoleName(ocRole);
                        sur.setDateUpdated(new Date());
                        StudyUserRole studyUserRole = studyUserRoleDao.saveOrUpdate(sur);
                        modifiedSURArray.add(studyUserRole);
                        if (userActiveStudyId == toUpdate.getStudyId())
                            studyUserRoleUpdated = true;
                    }
                }
            }
        }
        // remove all the roles that are not there for this user
        removeDeletedUserRoles(modifiedSURArray, existingStudyUserRoles);
        if (currentActiveStudyValid == false) {
            ub.setActiveStudy(placeHolderStudy);
            userAccountDao.saveOrUpdate(ub);
        }
        CoreResources.setRequestSchema(currentSchema);
        return studyUserRoleUpdated;
    }

    private boolean createSchema(String schemaName) throws Exception {
        try {
            schemaServiceDao.createStudySchema(schemaName);
        } catch (Exception e) {
            logger.error("Error while creating a liquibase schema:" + schemaName);
            logger.error(e.getMessage(), e);
            throw e;
        }
        return true;
    }

    public ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request) {
        return permissionService.getUserRoles(request);
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
    public void updateStudyUsername(UserAccountBean ub, KeycloakUser user) {
        int numUpdated = studyUserRoleDao.updateUsername(user.getNickname(), user.getUserId());
        logger.debug(numUpdated + " studyUserRoles updated for user:" + user.getNickname() + " and prevUser:" + user.getUserId());
    }

    public void updateParticipateModuleStatusInOC(HttpServletRequest request, String studyOid) {
        getRestfulServiceHelper().setSchema(studyOid, request);
        Study study = studyDao.findByOcOID(studyOid);
        if (study.getStudy()!=null)
            study = study.getStudy();
        persistparticipateModuleStatus(request,study);
   }



   public void persistparticipateModuleStatus(HttpServletRequest request, Study study){
       List<ModuleConfigDTO> moduleConfigDTOs = getParticipateModuleStatusFromStudyService(request, study);
           persistparticipateModuleStatus( moduleConfigDTOs,study);
   }


    public void persistparticipateModuleStatus(List<ModuleConfigDTO> moduleConfigDTOs,Study study){
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        String statusValue= DISABLED;
        if(moduleConfigDTOs.size()!=0) {
             statusValue = getModuleStatus(moduleConfigDTOs,study);
        }
        if (!spv.isActive()) {
            spv = new StudyParameterValueBean();
            spv.setStudyId(study.getStudyId());
            spv.setParameter("participantPortal");
            spv.setValue(statusValue);
            spvdao.create(spv);
        } else if (spv.isActive() && !spv.getValue().equals(statusValue)) {
            spv.setValue(statusValue);
            spvdao.update(spv);
        }
    }

    public String getModuleStatus(List<ModuleConfigDTO> moduleConfigDTOs, Study study) {
        for (ModuleConfigDTO moduleConfigDTO : moduleConfigDTOs) {
            if (moduleConfigDTO.getStudyUuid().equals(study.getStudyUuid())&& moduleConfigDTO.getModuleName().equalsIgnoreCase(PARTICIPATE)) {
                ModuleStatus moduleStatus = moduleConfigDTO.getStatus();
                if (moduleStatus.name().equalsIgnoreCase(ACTIVE)) {
                    logger.info("Module Status is Enabled");
                    return ENABLED;
                }
            }
        }
        logger.info("Module Status is Disabled");
        return DISABLED;
    }

    public List<ModuleConfigDTO> getParticipateModuleStatusFromStudyService (HttpServletRequest request , Study study) {
        if(StringUtils.isEmpty(study.getStudyUuid())) {
            // make call to study service to get study servie
            StudyEnvironmentDTO studyEnvironmentDTO = getStudyUuidFromStudyService(request,study);
            study.setStudyUuid(studyEnvironmentDTO.getStudyUuid());
            // save in study table in public and tenant
            studyDao.saveOrUpdate(study);
        }

        String SBSUrl = CoreResources.getField("SBSUrl");
        int index = SBSUrl.indexOf("//");
        String protocol = SBSUrl.substring(0, index) + "//";
        String appendUrl= "/study-service/api/studies/"+study.getStudyUuid()+"/module-configs";
        String uri = protocol + SBSUrl.substring(index + 2, SBSUrl.indexOf("/", index + 2)) + appendUrl;

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
        ResponseEntity<List<ModuleConfigDTO>> response = restTemplate.exchange(uri, HttpMethod.GET,entity,new ParameterizedTypeReference<List<ModuleConfigDTO>>(){});
        if(response==null)
            return null;

        return response.getBody();
    }

    public StudyEnvironmentDTO getStudyUuidFromStudyService (HttpServletRequest request , Study study) {

        String SBSUrl = CoreResources.getField("SBSUrl");
        int index = SBSUrl.indexOf("//");
        String protocol = SBSUrl.substring(0, index) + "//";
        String appendUrl= "/study-service/api/study-environments/"+study.getStudyEnvUuid();

        String uri = protocol + SBSUrl.substring(index + 2, SBSUrl.indexOf("/", index + 2)) + appendUrl;

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
           ResponseEntity<StudyEnvironmentDTO> response = restTemplate.exchange(uri, HttpMethod.GET,entity,StudyEnvironmentDTO.class);
        return response.getBody();
    }

    public RestfulServiceHelper getRestfulServiceHelper() {
        if (serviceHelper == null) {
            serviceHelper = new RestfulServiceHelper(this.dataSource);
        }
        return serviceHelper;
    }
}

package core.org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.sf.json.util.JSONUtils;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.dto.StudyEnvironmentDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.SchemaServiceDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyUserRole;
import core.org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.service.randomize.ModuleProcessor;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by yogi on 11/10/16.
 */
@Service("studyBuildService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class StudyBuildServiceImpl implements StudyBuildService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String sbsUrl = CoreResources.getField("SBSBaseUrl");
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
    private UtilService utilService;

    @Autowired
    private RandomizationService randomizationService;

    @Autowired
    private ParticipateService participateService;

    @Autowired
    private RestfulServiceHelper serviceHelper;

    public StudyBuildServiceImpl(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public StudyInfoObject process(HttpServletRequest request, Study study, UserAccountBean ub) throws Exception {
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
            isUserUpdated = saveStudyEnvRoles(request, ub, false);
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
        ResourceBundle resterm = core.org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();
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
        boolean studyEnvUuidProcessed = false;
        HttpSession session = request.getSession();
        String studyEnvUuid = (String) request.getParameter("studyEnvUuid");
        if (StringUtils.isEmpty(studyEnvUuid)) {
            studyEnvUuid = processAuth0State(request);
            if (StringUtils.isEmpty(studyEnvUuid))
                return studyEnvUuidProcessed;
        }
        updateStudyUserRoles(request, ub, userActiveStudyId, studyEnvUuid, false);

        Study currentPublicStudy = studyDao.findByStudyEnvUuid(studyEnvUuid);
        Study userStudy = studyDao.findByStudyEnvUuid(studyEnvUuid);
        if (currentPublicStudy == null) {
            return studyEnvUuidProcessed;
        }

        int parentStudyId = currentPublicStudy.isSite() ? currentPublicStudy.getStudy().getStudyId() : currentPublicStudy.getStudyId();
        if (ub.getActiveStudy() != null && ub.getActiveStudy().getStudyId() == parentStudyId)
            return studyEnvUuidProcessed;

        // check to see if the user has a role for this study
        ArrayList<StudyUserRole> userRoles = studyUserRoleDao.findAllUserRolesByUserAccountAndStudy(ub, currentPublicStudy.getStudyId());
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

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public UserAccount getUserAccountObject(UserAccountBean ubIn) {
        UserAccount byUserId = userAccountDao.findByUserId(ubIn.getId());
        return byUserId;
    }

    public boolean saveStudyEnvRoles(HttpServletRequest request, UserAccountBean ubIn, boolean isLogin) throws Exception {
        UserAccount ub = userAccountDao.findByUserId(ubIn.getId());
        boolean studyUserRoleUpdated;
        int userActiveStudyId;

        // because JDB transaction is not seen right away by Hibernate, active Study in UserAccountBean may not be the same as UserAccount
        if (ubIn.getActiveStudyId() != 0) {
            if ((ub.getActiveStudy() != null && ub.getActiveStudy().getStudyId() != ubIn.getActiveStudyId())
                    || ub.getActiveStudy() == null)
                ub.setActiveStudy(studyDao.findPublicStudyById(ubIn.getActiveStudyId()));
        }
        if (ub.getActiveStudy() == null)
            userActiveStudyId = 0;
        else
            userActiveStudyId = ub.getActiveStudy().getStudyId();
        boolean studyEnvUuidProcessed = processSpecificStudyEnvUuid(request, userActiveStudyId, ub);

        if (studyEnvUuidProcessed)
            return true;
        studyUserRoleUpdated = updateStudyUserRoles(request, ub, userActiveStudyId, null, isLogin);
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


    private boolean checkIfParentExists(HttpServletRequest request, Study study, List<StudyEnvironmentRoleDTO> roles) {
        StudyEnvironmentRoleDTO role = roles.stream().filter(s -> s.getStudyEnvironmentUuid().equals(study.getStudy().getStudyEnvUuid())).findAny()
                .orElse(null);
        if (role != null) {
            //request.getSession().setAttribute("customUserRole", role.getDynamicRoleName());
            return true;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public boolean updateStudyUserRoles(HttpServletRequest request, UserAccount ub, int userActiveStudyId, String altStudyEnvUuid, boolean isLogin) {
        boolean studyUserRoleUpdated = false;
        HttpSession session = request.getSession();
        String currentSchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema(request, "public");

        ResponseEntity<List<StudyEnvironmentRoleDTO>> userRoles = getUserRoles(request, isLogin);

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
            getRoleAssociatedWithActiveStudy(activeStudy,userRoles.getBody(),request);
        }

        // TODO: refactor this loop seems complex and error-prone & seems to break SRP.
        for (StudyEnvironmentRoleDTO role : userRoles.getBody()) {
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
            } else if (activeStudy != null && activeStudy.isSite()) {
                parentExists = activeStudy.getStudy().getStudyId() == study.getStudyId();
            }
            if (StringUtils.isNotEmpty(altStudyEnvUuid)) {
                if (uuidToFind.equals(altStudyEnvUuid)) {
                    logger.debug("Commented out setting altCustomUserRole - does it have any adverse effects ??");
                    //request.getSession().setAttribute("altCustomUserRole", role.getDynamicRoleName());
                }
            }
            // if current active study is still valid, then need to keep,because the active study is not always 
            // the first one in the study list come back from SBS call, so need to "refresh"
            if (study.getStudyId() == userActiveStudyId) {
            	ub.setActiveStudy(study);        	           
                userAccountDao.saveOrUpdate(ub);
            
                currentActiveStudyValid = true;
            }

            Study parentStudy = study.getStudy();
            Study toUpdate = parentStudy == null ? study : study.getStudy();
            // set this as the active study
            if (ub.getActiveStudy() == null || !currentActiveStudyValid) {
            	if(siteFlag) {
            		 ub.setActiveStudy(study);
            	}else {
            		 ub.setActiveStudy(toUpdate);
            	}
               
                userAccountDao.saveOrUpdate(ub);
                currentActiveStudyValid = true;
                //if (!parentExists)
                    //request.getSession().setAttribute("customUserRole", role.getDynamicRoleName());
            }
            placeHolderStudy = study;
            UserAccount userAccount = new UserAccount();
            userAccount.setUserName(ub.getUserName());
            ArrayList<StudyUserRole> byUserAccount = studyUserRoleDao.findAllUserRolesByUserAccountAndStudy(userAccount, study.getStudyId());
            String rolename = role.getRoleName();
            String ocRole = getOCRole(rolename, parentStudy != null ? true : false);
            if (byUserAccount.isEmpty()) {
                StudyUserRole studyUserRole = new StudyUserRole();
                StudyUserRoleId userRoleId = new StudyUserRoleId();
                studyUserRole.setId(userRoleId);
                userRoleId.setUserName(ub.getUserName());
                studyUserRole.setOwnerId(ub.getUserId());

                studyUserRole.setRoleName(ocRole);

                userRoleId.setStudyId(study.getStudyId());
                studyUserRole.setStatusId(core.org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
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
        
       
        
        // If role sizes are different update the flag
        if (modifiedSURArray.size() != existingStudyUserRoles.size())
            studyUserRoleUpdated = true;
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

    public ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request, boolean isLogin) {
        ResponseEntity<List<StudyEnvironmentRoleDTO>> responseEntity = permissionService.getUserRoles(request);
        if (isLogin)
            request.getSession().setAttribute("allUserRoles", responseEntity.getBody());
        return responseEntity;
    }

    public ResponseEntity getUserDetails(HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = sbsUrl + "/user-service/api/users/" + userUuid;
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

    private void processSingleModule(Study study, List<ModuleConfigDTO> moduleConfigDTOs, ModuleProcessor.Modules module, String accessToken) {
        String moduleEnabled = isModuleEnabled(moduleConfigDTOs, study, module);
        ModuleProcessor moduleProcessor = null;
        switch(module) {
            case PARTICIPATE:
                moduleProcessor = participateService;
                break;
            case RANDOMIZE:
                moduleProcessor = randomizationService;
                break;
            default:
                break;
        }
        if (moduleProcessor != null)
            moduleProcessor.processModule(study, moduleEnabled, accessToken);
    }

    public void processModule(String accessToken, Study study, ModuleProcessor.Modules module) {
        study =getModuleStudy(study.getOc_oid());
        List<ModuleConfigDTO> moduleConfigDTOs = getModuleConfigsFromStudyService(accessToken, study);
        processSingleModule(study, moduleConfigDTOs, module, accessToken);
    }

    private Study getModuleStudy(String studyOid) {
        utilService.setSchemaFromStudyOid(studyOid);
        Study study = studyDao.findByOcOID(studyOid);
        if (study.getStudy() != null)
            study = study.getStudy();
        return study;
    }

    public String isModuleEnabled(List<ModuleConfigDTO> moduleConfigDTOs, Study study, ModuleProcessor.Modules module) {
        if(moduleConfigDTOs!= null) {
            for (ModuleConfigDTO moduleConfigDTO : moduleConfigDTOs) {
                if (moduleConfigDTO.getStudyUuid().equals(study.getStudyUuid()) && moduleConfigDTO.getModuleName().equalsIgnoreCase(module.name())) {
                    core.org.akaza.openclinica.domain.enumsupport.ModuleStatus moduleStatus = moduleConfigDTO.getStatus();
                    if (moduleStatus.name().equalsIgnoreCase(ModuleProcessor.ModuleStatus.ACTIVE.name())) {
                        logger.info("Module Status is Enabled");
                        return ModuleProcessor.ModuleStatus.ENABLED.getValue();
                    }
                }
            }
        }
        logger.info("Module Status is Disabled");
        return ModuleProcessor.ModuleStatus.DISABLED.getValue();
    }


    public ModuleConfigDTO getModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study study, ModuleProcessor.Modules module) {
        if(moduleConfigDTOs != null) {
            Optional<ModuleConfigDTO> moduleConfigDTO =
                    moduleConfigDTOs.stream().filter(config -> config.getStudyUuid().equals(study.getStudyUuid()) && config.getModuleName().equalsIgnoreCase(module.name())).findAny();
            if (moduleConfigDTO.isPresent()) {
                logger.info("ModuleConfigDTO  is :" + moduleConfigDTO.get());
                return moduleConfigDTO.get();
            }
        }
        logger.info("ModuleConfigDTO  is null");
        return null;
    }

    public ModuleConfigAttributeDTO getModuleConfigAttribute(Set<ModuleConfigAttributeDTO> moduleConfigAttributeDTOs, Study study) {
        for (ModuleConfigAttributeDTO moduleConfigAttributeDTO : moduleConfigAttributeDTOs) {
            if (moduleConfigAttributeDTO.getStudyEnvironmentUuid().equals(study.getStudyEnvUuid())) {
                logger.info("ModuleConfigAttributeDTO  is :" + moduleConfigAttributeDTO);
                return moduleConfigAttributeDTO;
            }
        }
        logger.info("ModuleConfigAttributeDTO  is null");
        return null;
    }


    public List<ModuleConfigDTO> getModuleConfigsFromStudyService(String accessToken, Study study) {
        if (StringUtils.isEmpty(study.getStudyUuid())) {
            // make call to study service to get study servie
            StudyEnvironmentDTO studyEnvironmentDTO = getStudyUuidFromStudyService(accessToken, study);
            study.setStudyUuid(studyEnvironmentDTO.getStudyUuid());
            // save in study table in public and tenant
            studyDao.saveOrUpdate(study);
        }

        String appendUrl = "/study-service/api/studies/" + study.getStudyUuid() + "/module-configs";
        String uri = sbsUrl + appendUrl;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Accept-Charset", "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            List<HttpMessageConverter<?>> converters = new ArrayList<>();
            MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            jsonConverter.setObjectMapper(objectMapper);
            converters.add(jsonConverter);
            restTemplate.setMessageConverters(converters);
            ResponseEntity<List<ModuleConfigDTO>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ModuleConfigDTO>>() {
            });
            if (response == null)
                return null;
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e ) {
            logger.error("Client error: HttpStatusCode: {} HttpResponse: {} Error Trace: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e){
            logger.error("Error in fetching Module config from SBS: {}", e);
        }
        return null;
    }

    public StudyEnvironmentDTO getStudyUuidFromStudyService(String accessToken, Study study) {

        String appendUrl = "/study-service/api/study-environments/" + study.getStudyEnvUuid();
        String uri =sbsUrl + appendUrl;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<StudyEnvironmentDTO> response = restTemplate.exchange(uri, HttpMethod.GET, entity, StudyEnvironmentDTO.class);
        return response.getBody();
    }

    public RestfulServiceHelper getRestfulServiceHelper() {
        if (serviceHelper == null) {
            serviceHelper = new RestfulServiceHelper(this.dataSource, this, studyDao);
        }
        return serviceHelper;
    }

    private void getRoleAssociatedWithActiveStudy(Study study,
                                                  List<StudyEnvironmentRoleDTO> roles,
                                                  HttpServletRequest request) {

        StudyEnvironmentRoleDTO role;

        if (study.getStudyEnvSiteUuid() != null) {
            // Active study is a site level study
            // Active study is a site level hence it's parent is the study
            String studyEnvUuid = study.getStudy().getStudyEnvUuid();
            // Look for a site based role
            role = roles.stream()
                    .filter(s -> s.getStudyEnvironmentUuid() != null && s.getStudyEnvironmentUuid().equals(studyEnvUuid))
                    .filter(s -> s.getSiteUuid() != null && s.getSiteUuid().equals(study.getStudyEnvSiteUuid()))
                    .findAny()
                    .orElse(null);
            if (role == null) {
                // The user does not have a site based role so checking to see if they inherit a study level role
                role = roles.stream()
                        .filter(s -> s.getStudyEnvironmentUuid() != null && s.getStudyEnvironmentUuid().equals(studyEnvUuid))
                        .findAny()
                        .orElse(null);
            }
        } else {
            // Active study is a study not a site
            role = roles.stream()
                    .filter(s -> s.getStudyEnvironmentUuid() != null && s.getStudyEnvironmentUuid().equals(study.getStudyEnvUuid()))
                    .findAny()
                    .orElse(null);
        }
        if (role != null) {
            request.getSession().setAttribute("customUserRole", role.getDynamicRoleName());
            request.getSession().setAttribute("baseUserRole", role.getRoleName());
        }
    }
    
    private void removeDeletedUserRoles(ArrayList<StudyUserRole> modifiedStudyUserRoles, Collection<StudyUserRole> existingStudyUserRoles) {
        existingStudyUserRoles.removeIf(existingStudyUserRole -> modifiedStudyUserRoles.stream().anyMatch(
                modifiedStudyUserRole -> existingStudyUserRole.getId().getStudyId().equals(modifiedStudyUserRole.getId().getStudyId())));
        existingStudyUserRoles.forEach(studyToDelete -> studyUserRoleDao.getCurrentSession().delete(studyToDelete));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Study getPublicStudy(String ocId) {
        HttpServletRequest request = getRequest();
        String schema = null;
        if (request == null) {
            schema = CoreResources.getRequestSchema();
        } else {
            if (request != null)
                schema = (String) request.getAttribute("requestSchema");
        }
        if (request != null)
            request.setAttribute("requestSchema", "public");

        Study study = studyDao.findByOid(ocId);
        if (org.apache.commons.lang.StringUtils.isNotEmpty(schema) && request != null)
            request.setAttribute("requestSchema", schema);
        return study;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Study getParentPublicStudy(String ocId) {
        Study resultBean;
        HttpServletRequest request = getRequest();
        String schema = null;
        if (request == null) {
            schema = CoreResources.getRequestSchema();
        } else {
            if (request != null)
                schema = (String) request.getAttribute("requestSchema");
        }
        if (request != null)
            request.setAttribute("requestSchema", "public");

        Study study = getPublicStudy(ocId);
        if (study.isSite()) {
            resultBean = study.getStudy();
        } else {
            Study parentStudy = study;
            resultBean = parentStudy;
        }
        CoreResources.setRequestSchema(schema);
        return resultBean;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Study getPublicStudy(int id) {
        HttpServletRequest request = getRequest();
        String schema = null;
        if (request == null) {
            schema = CoreResources.getRequestSchema();
        } else {
            if (request != null)
                schema = (String) request.getAttribute("requestSchema");
        }
        if (request != null)
            request.setAttribute("requestSchema", "public");

        Study study = (Study) studyDao.findByPK(id);
        if (org.apache.commons.lang.StringUtils.isNotEmpty(schema) && request != null)
            request.setAttribute("requestSchema", schema);
        return study;
    }
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            return request;
        }
        return null;
    }

    public Boolean isPublicStudySameAsTenantStudy(Study tenantStudy, String publicStudyOID) {
        Study publicStudy = getPublicStudy(tenantStudy.getOc_oid());
        return publicStudy.getOc_oid().equals(publicStudyOID);
    }

    public void setRequestSchemaByStudy(String ocId) {
        Study studyBean = getPublicStudy(ocId);
        if (studyBean != null)
            CoreResources.setRequestSchema(studyBean.getSchemaName());
    }
}

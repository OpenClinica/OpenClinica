package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * A simple example of an annotated Spring Controller. Notice that it is a POJO; it
 * does not implement any Spring interfaces or extend Spring classes.
 */
@Controller
@RequestMapping(value = "/auth/api")
@Api(value = "Participant", tags = { "Participant" }, description = "REST API for Study Participant")
public class UserController {
    //Autowire the class that handles the sidebar structure with a configured
    //bean named "sidebarInit"
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;
    private RestfulServiceHelper restfulServiceHelper;

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    private UserService userService;

    @Autowired
    private ValidateService validateService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private StudyDao studyDao;
    @Autowired
    UserAccountDao userAccountDao;
    @Autowired
    KeycloakClientImpl keycloakClient;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String ENTITY_NAME = "UserController";

    public UserController() {
    }

    /**
     * The method is mapped to the URL /user.htm
     *
     * @param request The HttpServletRequest for storing attributes.
     * @param userId  The id of the user.
     * @return The return value is a ModelMap (instead of ModelAndView object),
     * because the view name automatically resolves to "user"
     */
    @RequestMapping("/user")
    public ModelMap userHandler(HttpServletRequest request,
                                @RequestParam("id") int userId) {
        ModelMap map = new ModelMap();
        List<String> userList = new ArrayList<String>();

        //set up request attributes for sidebar
        setUpSidebar(request);

        userList.add("Bruce");
        userList.add("Yufang");
        userList.add("Krikor");
        userList.add("Tom");


        //TODO: Get user from Hibernate DAO class
        //userList.add(userDao.loadUser(userId).getName())
        map.addAllAttributes(userList);
        return map;
    }

    @RequestMapping(value = "/clinicaldata/studies/{studyOID}/participants/{SSID}/connect", method = RequestMethod.POST)
    public ResponseEntity<OCUserDTO> connectParticipant(HttpServletRequest request, @PathVariable("studyOID") String studyOid, @PathVariable("SSID") String ssid, @RequestBody OCParticipantDTO participantDTO) {
        utilService.setSchemaFromStudyOid(studyOid);
        String accessToken = utilService.getAccessTokenFromRequest(request);
        UserAccountBean ownerUserAccountBean = utilService.getUserAccountFromRequest(request);
        String customerUuid = utilService.getCustomerUuidFromRequest(request);
        ResourceBundle textsBundle = ResourceBundleProvider.getTextsBundle(LocaleResolver.getLocale(request));
        String realm = keycloakClient.getRealmName(accessToken, customerUuid);

        OCUserDTO ocUserDTO = userService.connectParticipant(studyOid, ssid, participantDTO, accessToken, ownerUserAccountBean, realm, customerUuid,textsBundle);
        logger.info("REST request to POST OCUserDTO : {}", ocUserDTO);
        return new ResponseEntity<OCUserDTO>(ocUserDTO, HttpStatus.OK);
    }


    @RequestMapping(value = "/clinicaldata/studies/{studyOID}/participants/{SSID}", method = RequestMethod.GET)
    public ResponseEntity<OCUserDTO> getParticipant(HttpServletRequest request, @PathVariable("studyOID") String studyOid, @PathVariable("SSID") String ssid) {
        utilService.setSchemaFromStudyOid(studyOid);
        String accessToken = utilService.getAccessTokenFromRequest(request);

        OCUserDTO ocUserDTO = userService.getParticipantAccount(studyOid, ssid, accessToken);
        logger.info("REST request to GET OCUserDTO : {}", ocUserDTO);
        if (ocUserDTO == null) {
            return new ResponseEntity<OCUserDTO>(ocUserDTO, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<OCUserDTO>(ocUserDTO, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/clinicaldata/studies/{studyOID}/participantUsers", method = RequestMethod.GET)
    public ResponseEntity<List<OCUserDTO>> getAllParticipantFromUserService(HttpServletRequest request, @PathVariable("studyOID") String studyOid) {
        utilService.setSchemaFromStudyOid(studyOid);
        String accessToken = utilService.getAccessTokenFromRequest(request);

        List<OCUserDTO> ocUserDTOs = userService.getAllParticipantAccountsFromUserService(accessToken);
        logger.info("REST request to GET List of OCUserDTO : {}", ocUserDTOs);

        return new ResponseEntity<List<OCUserDTO>>(ocUserDTOs, HttpStatus.OK);

    }

    @RequestMapping(value = "/clinicaldata/studies/{studyOID}/participants/{SSID}/accessLink", method = RequestMethod.GET)
    public ResponseEntity<ParticipantAccessDTO> getAccessLink(HttpServletRequest request, @PathVariable("studyOID") String studyOid, @PathVariable("SSID") String ssid,
                                                              @RequestParam(value = "includeAccessCode", defaultValue = "n", required = false) String includeAccessCode) {

        boolean incldAccessCode = false;
        if (includeAccessCode != null && includeAccessCode.trim().toUpperCase().equals("Y")) {
            incldAccessCode = true;
        }

        utilService.setSchemaFromStudyOid(studyOid);
        String accessToken = utilService.getAccessTokenFromRequest(request);
        String customerUuid = utilService.getCustomerUuidFromRequest(request);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        String realm = keycloakClient.getRealmName(accessToken, customerUuid);

        ParticipantAccessDTO participantAccessDTO = userService.getAccessInfo(accessToken, studyOid, ssid, realm, userAccountBean,incldAccessCode,incldAccessCode);
        if (participantAccessDTO == null) {
            logger.error("REST request to GET AccessLink Object for Participant not found ");
            return new ResponseEntity<ParticipantAccessDTO>(participantAccessDTO, HttpStatus.NOT_FOUND);
        }

        logger.info("REST request to GET AccessLink Object : {}", participantAccessDTO);
        return new ResponseEntity<ParticipantAccessDTO>(participantAccessDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/clinicaldata/studies/{studyOID}/participants/searchByFields", method = RequestMethod.GET)
    public ResponseEntity<List<OCUserDTO>> searchByIdentifier(HttpServletRequest request, @PathVariable("studyOID") String studyOid, @PathParam("participantId") String participantId, @PathParam("firstName") String firstName, @PathParam("lastName") String lastName, @PathParam("identifier") String identifier) {
        utilService.setSchemaFromStudyOid(studyOid);
        String accessToken = utilService.getAccessTokenFromRequest(request);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        List<OCUserDTO> userDTOs = null;
        // validate accessToken against studyOid ,
        // also validate accessToken's user role crc/investigator
        userDTOs = userService.searchParticipantsByFields(studyOid, accessToken, participantId, firstName, lastName, identifier, userAccountBean);

        logger.info("REST request to POST OCUserDTO : {}", userDTOs);
        return new ResponseEntity<List<OCUserDTO>>(userDTOs, HttpStatus.OK);
    }

    @ApiOperation( value = "Retrieve all participants contact information with or without their OpenClinica participate access code.", notes = "Will extract the data in a text file" )
    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/sites/{siteOID}/participants/extractParticipantsInfo", method = RequestMethod.POST )
    public ResponseEntity<Object> extractParticipantsInfo(HttpServletRequest request,
                                                          @ApiParam( value = "Study OID", required = true ) @PathVariable( "studyOID" ) String studyOid,
                                                          @ApiParam( value = "Site OID", required = true ) @PathVariable( "siteOID" ) String siteOid,
                                                          @ApiParam( value = "Use this parameter to retrieve participant's access code for OpenClinica Participant module. Possible values - y or n.", required = false )
                                                          @RequestParam( value = "includeParticipateInfo", defaultValue = "n", required = false ) String includeParticipateInfo,
                                                          @RequestParam( value = "page_number", defaultValue = "1", required = false ) String pNumber,
                                                          @RequestParam( value = "page_size", defaultValue = "100", required = false ) String pSize) throws InterruptedException {
        utilService.setSchemaFromStudyOid(studyOid);
        Study tenantStudy = getTenantStudy(studyOid);
        Study tenantSite = getTenantStudy(siteOid);
        ResponseEntity<Object> response = null;
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();
        int pageNumber = 1;
        int pageSize = 100;
        boolean incRelatedInfo = false;
        if (includeParticipateInfo != null && includeParticipateInfo.trim().toUpperCase().equals("Y")) {
            incRelatedInfo = true;
        }
        boolean isStudyLevelUser;
        try {
            pageNumber = validatePageNumber(pNumber);
            pageSize = validatePageSize(pSize);
             validateService.validateStudyAndRolesForExtractParticipantInfo(studyOid, siteOid, userAccountBean, incRelatedInfo);

        } catch (OpenClinicaSystemException e) {
            String errorMsg = e.getErrorCode();
            HashMap<String, String> map = new HashMap<>();
            map.put("studyOid", studyOid);
            map.put("siteOid", siteOid);
            core.org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM responseDTO = new ParameterizedErrorVM(errorMsg, map);
            response = new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            return response;
        }

        isStudyLevelUser = utilService.checkStudyLevelUser(userAccountBean.getRoles(), siteOid);
        String accessToken = utilService.getAccessTokenFromRequest(request);
        String customerUuid = utilService.getCustomerUuidFromRequest(request);
        String realm = keycloakClient.getRealmName(accessToken, customerUuid);

        String uuid = startExtractJob(studyOid, siteOid, accessToken, realm, userAccountBean, incRelatedInfo, pageNumber, pageSize, isStudyLevelUser);

        logger.info("REST request to Extract Participants info ");
        return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);
    }


    private void setUpSidebar(HttpServletRequest request) {
        if (sidebarInit.getAlertsBoxSetup() ==
                SidebarEnumConstants.OPENALERTS) {
            request.setAttribute("alertsBoxSetup", true);
        }

        if (sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO) {
            request.setAttribute("infoBoxSetup", true);
        }
        if (sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS) {
            request.setAttribute("instructionsBoxSetup", true);
        }

        if (!(sidebarInit.getEnableIconsBoxSetup() ==
                SidebarEnumConstants.DISABLEICONS)) {
            request.setAttribute("enableIconsBoxSetup", true);
        }


    }

    public SidebarInit getSidebarInit() {
        return sidebarInit;
    }

    public void setSidebarInit(SidebarInit sidebarInit) {
        this.sidebarInit = sidebarInit;
    }

    private Study getTenantStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }

    public String startExtractJob(String studyOid, String siteOid, String accessToken, String realm, UserAccountBean userAccountBean, boolean incRelatedInfo,int pageNumber,int pageSize, boolean isStudyLevelUser) {
        utilService.setSchemaFromStudyOid(studyOid);
        String schema = CoreResources.getRequestSchema();

        Study site = studyDao.findByOcOID(siteOid);
        Study study = studyDao.findByOcOID(studyOid);
        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.ACCESS_CODE, null);
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                userService.extractParticipantsInfo(studyOid, siteOid, accessToken, realm, userAccountBean, schema, jobDetail, incRelatedInfo,pageNumber,pageSize, isStudyLevelUser);
            } catch (Exception e) {
                logger.error("Exeception is thrown while extracting job : " + e);
            }
            return null;
        });
        return jobDetail.getUuid();
    }

    private int validatePageNumber(String pNumber) {
        int pageNumber = 1;
        try {
            pageNumber = Integer.parseInt(pNumber.trim());
        } catch (NumberFormatException nfe) {
            logger.info("invalid Page Number Format Exception: " + nfe.getMessage());
            throw new OpenClinicaSystemException(ErrorConstants.ERR_INVALID_PAGE_NUMBER_PARAMETER);
        }
        if (pageNumber < 1)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_INVALID_PAGE_NUMBER_PARAMETER);

        return pageNumber;
    }

    private int validatePageSize(String pSize) {
        int pageSize = 1;
        try {
            pageSize = Integer.parseInt(pSize.trim());
        } catch (NumberFormatException nfe) {
            logger.info("invalid Page Number Format Exception: " + nfe.getMessage());
            throw new OpenClinicaSystemException(ErrorConstants.ERR_INVALID_PAGE_SIZE_PARAMETER);
        }
        if (pageSize < 1)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_INVALID_PAGE_SIZE_PARAMETER);

        return pageSize;
    }
}

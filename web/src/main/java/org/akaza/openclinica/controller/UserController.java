package org.akaza.openclinica.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.PathParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.*;
import org.akaza.openclinica.service.crfdata.xform.EnketoURLRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

/**
 * A simple example of an annotated Spring Controller. Notice that it is a POJO; it
 * does not implement any Spring interfaces or extend Spring classes.
 */
@Controller
@RequestMapping( value = "/auth/api" )
public class UserController {
    //Autowire the class that handles the sidebar structure with a configured
    //bean named "sidebarInit"
    @Autowired
    @Qualifier( "sidebarInit" )
    private SidebarInit sidebarInit;
    private RestfulServiceHelper restfulServiceHelper;

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    private UserService userService;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

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
    @RequestMapping( "/user" )
    public ModelMap userHandler(HttpServletRequest request,
                                @RequestParam( "id" ) int userId) {
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

    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/participants/{SSID}/connect", method = RequestMethod.POST )
    public ResponseEntity<OCUserDTO> connectParticipant(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid, @PathVariable( "SSID" ) String ssid, @RequestBody OCParticipantDTO participantDTO) {
        setSchemaFromRequest(studyOid, request);
        String accessToken = getAccessTokenFromRequest(request);
        UserAccountBean ownerUserAccountBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        String customerUuid = getCustomerUuidFromRequest(request);

        OCUserDTO ocUserDTO= userService.connectParticipant(studyOid, ssid, participantDTO ,accessToken,ownerUserAccountBean,customerUuid);
        logger.info("REST request to POST OCUserDTO : {}", ocUserDTO);
        return new ResponseEntity<OCUserDTO>(ocUserDTO, HttpStatus.OK);
    }


    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/participants/{SSID}", method = RequestMethod.GET )
    public ResponseEntity<OCUserDTO> getParticipant(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid, @PathVariable( "SSID" ) String ssid) {
        setSchemaFromRequest(studyOid, request);
        String accessToken = getAccessTokenFromRequest(request);

        OCUserDTO ocUserDTO = userService.getParticipantAccount(studyOid, ssid, accessToken);
        logger.info("REST request to GET OCUserDTO : {}", ocUserDTO);
        if (ocUserDTO == null) {
            return new ResponseEntity<OCUserDTO>(ocUserDTO, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<OCUserDTO>(ocUserDTO, HttpStatus.OK);
        }
    }


    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/participantUsers", method = RequestMethod.GET )
    public ResponseEntity<List <OCUserDTO>> getAllParticipantFromUserService(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid) {
        setSchemaFromRequest(studyOid, request);
        String accessToken = getAccessTokenFromRequest(request);

        List <OCUserDTO> ocUserDTOs = userService.getAllParticipantAccountsFromUserService(accessToken);
        logger.info("REST request to GET List of OCUserDTO : {}", ocUserDTOs);

        return new ResponseEntity<List <OCUserDTO>>(ocUserDTOs, HttpStatus.OK);

    }

    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/participants/{SSID}/accessLink", method = RequestMethod.GET )
    public ResponseEntity<ParticipantAccessDTO> getAccessLink(HttpServletRequest request,@PathVariable( "studyOID" ) String studyOid, @PathVariable( "SSID" ) String ssid) {
        setSchemaFromRequest(studyOid, request);
        String accessToken = getAccessTokenFromRequest(request);
        String customerUuid = getCustomerUuidFromRequest(request);

        ParticipantAccessDTO participantAccessDTO= userService.getAccessInfo(accessToken,studyOid, ssid,customerUuid);
        if(participantAccessDTO==null){
            logger.error("REST request to GET AccessLink Object for Participant not found ");
            return new ResponseEntity<ParticipantAccessDTO>(participantAccessDTO, HttpStatus.NOT_FOUND);
        }

        logger.info("REST request to GET AccessLink Object : {}", participantAccessDTO);
        return new ResponseEntity<ParticipantAccessDTO>(participantAccessDTO, HttpStatus.OK);
    }

    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/participants/searchByFields", method = RequestMethod.GET )
    public ResponseEntity<List<OCUserDTO>> searchByIdentifier(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid,@PathParam( "participantId" ) String participantId,@PathParam( "firstName" ) String firstName,@PathParam( "lastName" ) String lastName, @PathParam( "identifier" ) String identifier) {
        setSchemaFromRequest(studyOid, request);
        String accessToken = getAccessTokenFromRequest(request);
        UserAccountBean userAccountBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        List<OCUserDTO> userDTOs=null;
        // validate accessToken against studyOid ,
        // also validate accessToken's user role crc/investigator
            userDTOs = userService.searchParticipantsByFields(studyOid, accessToken, participantId, firstName, lastName, identifier, userAccountBean);

        logger.info("REST request to POST OCUserDTO : {}", userDTOs);
        return new ResponseEntity<List<OCUserDTO>>(userDTOs, HttpStatus.OK);
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

    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource);
        }
        return restfulServiceHelper;
    }

    private String getAccessTokenFromRequest(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("accessToken");
    }

    private void setSchemaFromRequest(String studyOid,HttpServletRequest request){
        getRestfulServiceHelper().setSchema(studyOid, request);
    }

private String getCustomerUuidFromRequest(HttpServletRequest request){
    Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
    return  (String) userContextMap.get("customerUuid");
}

}

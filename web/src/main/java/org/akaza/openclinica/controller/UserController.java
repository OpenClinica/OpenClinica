package org.akaza.openclinica.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.service.*;
import org.akaza.openclinica.service.crfdata.xform.EnketoURLRequest;
import org.apache.commons.codec.binary.Base64;
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

    @Autowired
    private UserService userService;

    @Autowired
    private ParticipateService participateService;

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
    public ResponseEntity<Object> connectParticipant(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid, @PathVariable( "SSID" ) String ssid, @RequestBody OCParticipantDTO participantDTO) {
        participateService.getRestfulServiceHelper().setSchema(studyOid, request);

        Object object = userService.connectParticipant(studyOid, ssid, participantDTO, request);
        if (object instanceof HttpClientErrorException) {
            return new ResponseEntity<Object>(((HttpClientErrorException) object).getResponseBodyAsString(), ((HttpClientErrorException) object).getStatusCode());
        } else if (object instanceof OCUserDTO) {
            return new ResponseEntity<Object>(object, HttpStatus.OK);
        } else {
            return null;
        }
    }

    @RequestMapping( value = "/clinicaldata/studies/{studyOID}/sites/{sitesOID}/participants/{SSID}/connect", method = RequestMethod.POST )
    public ResponseEntity<Object> connectSiteParticipant(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid,@PathVariable( "studyOID" ) String siteOid, @PathVariable( "SSID" ) String ssid, @RequestBody OCParticipantDTO participantDTO) {
        participateService.getRestfulServiceHelper().setSchema(studyOid, request);

        Object object = userService.connectParticipant(studyOid, ssid, participantDTO, request);
        if (object instanceof HttpClientErrorException) {
            return new ResponseEntity<Object>(((HttpClientErrorException) object).getResponseBodyAsString(), ((HttpClientErrorException) object).getStatusCode());
        } else if (object instanceof OCUserDTO) {
            return new ResponseEntity<Object>(object, HttpStatus.OK);
        } else {
            return null;
        }
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


}

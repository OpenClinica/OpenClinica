package org.akaza.openclinica.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
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
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    @Autowired
    private UserService userService;


    public UserController(){}

    /**
     * The method is mapped to the URL /user.htm
     * @param request  The HttpServletRequest for storing attributes.
     * @param userId   The id of the user.
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

    @RequestMapping( value ="/createOCUser", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> createOCUser(HttpServletRequest request, @RequestBody OCUserDTO userDTO) {
        String createUserUri = CoreResources.getField("SBSUrl");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        StudyBean studyBean = null;
        HttpEntity<OCUserDTO> entity = new HttpEntity<OCUserDTO>(userDTO, headers);

        ResponseEntity<OCUserDTO> userResponse = restTemplate.exchange(createUserUri, HttpMethod.POST, entity, OCUserDTO.class);
        if (userResponse == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }else{
            userDTO=userResponse.getBody();
            if(userDTO!=null) {
                HttpSession session = request.getSession();
                 studyBean = (StudyBean) session.getAttribute("study");

                userService.updateParticipantUserInfo(userDTO,studyBean);
            }
        }
        StudyEnvironmentRoleDTO roleDTO=new StudyEnvironmentRoleDTO();
         roleDTO.setRoleUuid("1234");
         roleDTO.setStudyUuid(studyBean.getStudyEnvUuid());






        String assignRoleUri = createUserUri+userDTO.getUuid()+ "/study-environments/"+studyBean.getStudyEnvUuid() +"/roles";
        ResponseEntity<StudyEnvironmentRoleDTO> roleResponse = restTemplate.exchange(assignRoleUri, HttpMethod.POST, entity, StudyEnvironmentRoleDTO.class);
        if (roleResponse == null) {
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }else{
            roleDTO=roleResponse.getBody();
        }

        return new ResponseEntity(userDTO, org.springframework.http.HttpStatus.OK);

    }






    private void setUpSidebar(HttpServletRequest request){
        if(sidebarInit.getAlertsBoxSetup() ==
          SidebarEnumConstants.OPENALERTS){
            request.setAttribute("alertsBoxSetup",true);
        }

        if(sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO){
            request.setAttribute("infoBoxSetup",true);
        }
        if(sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS){
            request.setAttribute("instructionsBoxSetup",true);
        }

        if(! (sidebarInit.getEnableIconsBoxSetup() ==
          SidebarEnumConstants.DISABLEICONS)){
            request.setAttribute("enableIconsBoxSetup",true);
        }


    }

    public SidebarInit getSidebarInit() {
        return sidebarInit;
    }

    public void setSidebarInit(SidebarInit sidebarInit) {
        this.sidebarInit = sidebarInit;
    }





}

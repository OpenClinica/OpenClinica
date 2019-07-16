package org.akaza.openclinica.controller;

import io.swagger.annotations.ApiOperation;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.ContactsModuleDTO;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * A simple example of an annotated Spring Controller. Notice that it is a POJO; it
 * does not implement any Spring interfaces or extend Spring classes.
 */
@Controller
@RequestMapping( value = "/auth/api/system/studies" )

public class ContactsModuleController {

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    private ContactsModuleService contactsModuleService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private ValidateService validateService;

    @Autowired
    private UserAccountDao userAccountDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ContactsModuleController() {
    }

    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";

    /*
    @ApiOperation( value = "To update contacts Module Status ", hidden = true )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Successful operation" ),
            @ApiResponse( code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error list: <br /> "
                    + "<br />Error Code                                            Descriptions"
                    + "<br />Status    : Status should be either 'enabled' or 'disabled'."
            )} )
    */
    @RequestMapping( value = "/{studyOID}/modules/contacts", method = RequestMethod.POST )
    public ResponseEntity updateContactModuleStatus(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid,
                                                    @RequestBody ContactsModuleDTO contactsModuleDTO) {
        String accessToken = utilService.getAccessTokenFromRequest(request);
        utilService.setSchemaFromStudyOid(studyOid);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

        if (!validateService.isStudyOidValid(studyOid)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid studyOID");
        }

        if (!validateService.isStudyOidValidStudyLevelOid(studyOid)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("studyOID is not a Study Level Oid");
        }

        if (!contactsModuleDTO.getStatus().equals(ENABLED) && !contactsModuleDTO.getStatus().equals(DISABLED)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status should be either 'enabled' or 'disabled'");
        }

        UserAccount userAccount = userAccountDao.findByUserId(userAccountBean.getId());
        if (!validateService.isUserHasTechAdminRole(userAccount)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not have a tech admin Role");
        }

        contactsModuleService.updateContactModuleStatus(studyOid, contactsModuleDTO);

        return ResponseEntity.status(HttpStatus.OK).body(contactsModuleDTO);
    }

    /*@ApiOperation( value = "To get contacts Module Status ", hidden = true )*/
    @RequestMapping( value = "/{studyOID}/modules/contacts", method = RequestMethod.GET )
    public ResponseEntity getContactModuleStatus(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid
    ) {
        String accessToken = utilService.getAccessTokenFromRequest(request);
        utilService.setSchemaFromStudyOid(studyOid);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);

        if (!validateService.isStudyOidValid(studyOid)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid studyOID");
        }

        if (!validateService.isStudyOidValidStudyLevelOid(studyOid)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("studyOID is not a Study Level Oid");
        }

        UserAccount userAccount = userAccountDao.findByUserId(userAccountBean.getId());
        if (!validateService.isUserHasTechAdminRole(userAccount)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not have a tech admin Role");
        }

        ContactsModuleDTO contactsModuleDTO = contactsModuleService.getContactModuleStatus(studyOid);
        return ResponseEntity.status(HttpStatus.OK).body(contactsModuleDTO);
    }

}

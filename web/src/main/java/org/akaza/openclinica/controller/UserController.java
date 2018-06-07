package org.akaza.openclinica.controller;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.service.OCUserRoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * A simple example of an annotated Spring Controller. Notice that it is a POJO; it
 * does not implement any Spring interfaces or extend Spring classes.
 */
@RestController("userController")
@RequestMapping(value = "/auth/api/users")
public class UserController {
    @RequestMapping(value = "/{userUuid}/update", method = RequestMethod.PUT)
    public void updateUser(
            @RequestBody OCUserRoleDTO ocUserRoleDTO,
            @PathVariable("userUuid") String studyEnvUuid,
            HttpServletRequest request) {

    }
}

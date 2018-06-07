package org.akaza.openclinica.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

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

package org.akaza.openclinica.controller;

import org.akaza.openclinica.service.UserType;
import org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.service.auth.TokenService;
import org.akaza.openclinica.service.randomize.RandomizationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/auth/api/v1/randomize")
public class RandomizeController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private UtilService utilService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RandomizationService randomizationService;

    /**
     * refresh randomize configurations for all studies for which this module is enabled.
     * @param request
     * @return
     */
    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    public ResponseEntity<String> refreshConfiguration(HttpServletRequest request) {
        String accessToken = utilService.getAccessTokenFromRequest(request);
        if (StringUtils.isEmpty(accessToken))
            return new ResponseEntity<>("Access token not specified.", HttpStatus.BAD_REQUEST);

        String role = tokenService.getRole(accessToken);

        if (!StringUtils.equals(role, UserType.TECH_ADMIN.getName()))
            return new ResponseEntity<>("Tech Admin role is needed to call this service.", HttpStatus.BAD_REQUEST);

        randomizationService.refreshConfigurations(accessToken);
        return new ResponseEntity("Success", HttpStatus.NO_CONTENT);
    }
}

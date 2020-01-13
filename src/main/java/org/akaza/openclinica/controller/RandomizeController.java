package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.service.UserType;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.service.auth.TokenService;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
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
import java.util.HashMap;
import java.util.Map;

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
     * If the request returns successful, but returns 400 errors for the subsequent individual studies, this usually means
     * that the randomize module is enabled for that study but it does not have a corresponding configuration in randomize service.
     * @param request
     * @return
     */
    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    public ResponseEntity<String> refreshConfiguration(HttpServletRequest request) {
        String accessToken = utilService.getAccessTokenFromRequest(request);
        if (StringUtils.isEmpty(accessToken))
            return new ResponseEntity<>("Access token not specified.", HttpStatus.BAD_REQUEST);

        String userType = tokenService.getUserType(accessToken);

        if (!StringUtils.equals(userType, UserType.TECH_ADMIN.getName()))
            return new ResponseEntity<>("Tech Admin user type is needed to call this service.", HttpStatus.FORBIDDEN);

        Map<String, String> configMap = new HashMap<>();
        boolean isSuccess = randomizationService.refreshConfigurations(accessToken, configMap);
        if (isSuccess)
            return new ResponseEntity(configMap, HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity(configMap, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

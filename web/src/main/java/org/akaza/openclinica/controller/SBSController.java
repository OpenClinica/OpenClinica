package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class SBSController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/customer-service/api/allowed-connections", method = RequestMethod.GET)
    protected ResponseEntity<String[]> redirectToSBS(final HttpServletRequest req, HttpServletResponse res,
            @RequestParam("subdomain") String subDomain) {
        logger.debug("Performing login");
        String SBSUrl = CoreResources.getField("SBSUrl");
        int index = SBSUrl.indexOf("//");
        String protocol = SBSUrl.substring(0, index) + "//";
        String SBSDomainURl = protocol + SBSUrl.substring(index + 2, SBSUrl.indexOf("/", index + 2)) + "/customer-service/api/allowed-connections?subdomain=" + subDomain;
        RestTemplate restTemplate = new RestTemplate();
        String[] responseStr = restTemplate.getForObject(SBSDomainURl, String[].class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        ResponseEntity<String[]> responseEntity = new ResponseEntity<String[]>(responseStr, headers, HttpStatus.OK);
        return responseEntity;
    }
}

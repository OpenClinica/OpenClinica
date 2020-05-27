package org.akaza.openclinica.controller;

import org.akaza.openclinica.service.FormCacheServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// A custom controller for additional Enketo related calls that are not
// made directly from Enketo itself. See OpenRosaSubmissionController
// for endpoints that are directly called by Enketo.
@Controller(value = "enketoController")
public class EnketoController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FormCacheServiceImpl formCacheService;

    @RequestMapping(value="/closeForm", method = RequestMethod.GET)
    protected void signatureCallback2(final HttpServletRequest req, final HttpServletResponse res, @RequestParam("ecid") String ecId) throws IOException {
        logger.info("hit controller: " + ecId);
        formCacheService.expireAndRemoveForm(ecId);
    }
}
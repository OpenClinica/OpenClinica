package org.akaza.openclinica.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller(value = "keepSessionAliveController")
public class KeepSessionAliveController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/keepAlive", method = RequestMethod.GET)
    protected void signatureCallback(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
       logger.debug("keeping the session alive");
        System.out.println("keeping the session alive");
    }
}
package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller public class ResetOCAppTimeoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PermitAll @RequestMapping(value = "/resetOCAppTimeout", method = RequestMethod.GET)
    protected String login(final HttpServletRequest req, HttpServletResponse res) {
        String debugCode = req.getParameter("automation");
        if (StringUtils.isEmpty(debugCode))
            return "error";
        if (!StringUtils.equals(debugCode, "0610443160820171"))
            return "error";
        String SBSUrl = CoreResources.getField("SBSBaseUrl");
        if (StringUtils.isNotEmpty(SBSUrl)) {
            String crossStorageURL = SBSUrl + "/hub/hub.html";
            req.setAttribute("crossStorageURL", crossStorageURL);
            return "resetOCAppTimeout";
        } else
            return "error";

    }
}

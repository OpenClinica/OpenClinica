package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Controller
public class PermissionController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private EventCrfDao eventCrfDao;

    @RequestMapping(value = "/checkAccess", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Boolean> isPermiited(final HttpServletRequest request,
                                            @RequestParam("eventCrfId") Integer eventCrfId,
                                            @RequestParam("formLayoutId") Integer formLayoutId,
                                            @RequestParam("studyEventId") Integer studyEventId) {
        final EventCrf ec = eventCrfDao.findById(eventCrfId);
        return Collections.singletonMap("status", permissionService.hasFormAccess(ec, formLayoutId, studyEventId, request));
    }
}

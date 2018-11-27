package org.akaza.openclinica.controller;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.AuditLogEventService;
import org.akaza.openclinica.service.ParticipateService;
import org.akaza.openclinica.service.SiteBuildService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.cdisc.ns.odm.v130.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;

@Controller

public class AuditLogEventController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private AuditLogEventService auditLogEventService;


    @RequestMapping(value = "/auth/api/studies/{studyOID}/auditEvents", method = RequestMethod.POST)
    public ResponseEntity<?> createAuditLogEvent(HttpServletRequest request, @PathVariable( "studyOID" ) String studyOid, @RequestBody AuditLogEventDTO auditLogEventDTO) {
        logger.info("Creating AuditLogEvent : {}", auditLogEventDTO);

        auditLogEventService.getRestfulServiceHelper().setSchema(studyOid, request);
        UserAccountBean ub = auditLogEventService.getRestfulServiceHelper().getUserAccount(request);

        auditLogEventService.saveAuditLogEvent(auditLogEventDTO,ub);

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }






}

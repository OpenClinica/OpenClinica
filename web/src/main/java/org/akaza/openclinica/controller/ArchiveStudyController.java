package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.service.LiquibaseOnDemandService;
import org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.service.SiteBuildService;
import org.akaza.openclinica.service.archival.ArchivedStudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.*;

@Controller
@RequestMapping(value = "/auth/api/v1/studies/archive")
public class ArchiveStudyController {

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;
	@Autowired
	private StudyDao studyDao;
	@Autowired
	private StudyBuildService studyBuildService;
	@Autowired
	private LiquibaseOnDemandService liquibaseOnDemandService;
	@Autowired
	private SiteBuildService siteBuildService;
	@Autowired
	private ArchivedStudyService archivedStudyService;

	public static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword, resworkflow;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	UserAccountDAO udao;
	StudyDAO sdao;
	StudyEventDefinitionDAO seddao;


	@RequestMapping(value = "/{uniqueProtocolID}/restore", method = RequestMethod.GET)
	public ResponseEntity<Object> restoreStudy(HttpServletRequest request, @PathVariable("uniqueProtocolID") String uniqueProtocolID) throws Exception {
		logger.debug("Archiving for protocol:" + uniqueProtocolID);

		archivedStudyService.restoreStudy(uniqueProtocolID);
		ResponseEntity response = new ResponseEntity(null, HttpStatus.OK);

		return response;

	}
	@RequestMapping(value = "/{uniqueProtocolID}/perform", method = RequestMethod.GET)
	public ResponseEntity<Object> archiveStudy(HttpServletRequest request, @PathVariable("uniqueProtocolID") String uniqueProtocolID) throws Exception {
		logger.debug("Archiving for protocol:" + uniqueProtocolID);
        request.setAttribute("requestSchema", "public");
        archivedStudyService.archiveStudy(uniqueProtocolID);
		ResponseEntity response = new ResponseEntity(null, HttpStatus.OK);

		return response;

	}
}



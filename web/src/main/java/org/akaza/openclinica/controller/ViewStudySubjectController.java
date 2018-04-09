package org.akaza.openclinica.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.service.Page;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewStudySubjectController {

	@Autowired
	private ViewStudySubjectService viewStudySubjectService;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Schedule new event Overhaul and add a form to event
	 * 
	 * @param request
	 * @param studyOid
	 * @param studyEventDefinitionOid
	 * @param crfOid
	 * @param studySubjectOid
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(value = "/api/addAnotherForm", method = RequestMethod.POST)
	public ResponseEntity<ViewStudySubjectDTO> addNewForm(HttpServletRequest request, @RequestParam("studyoid") String studyOid,
			@RequestParam("studyeventdefinitionoid") String studyEventDefinitionOid, @RequestParam("crfoid") String crfOid,
			@RequestParam("studysubjectoid") String studySubjectOid) throws IOException, URISyntaxException {

		ViewStudySubjectDTO viewStudySubjectDTO = viewStudySubjectService.addNewForm(request, studyOid, studyEventDefinitionOid, crfOid, studySubjectOid);

		if (viewStudySubjectDTO == null) {
			return new ResponseEntity<ViewStudySubjectDTO>(viewStudySubjectDTO, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<ViewStudySubjectDTO>(viewStudySubjectDTO, org.springframework.http.HttpStatus.OK);

	}

	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(value = "/api/studies/{studyoid}/pages/{name}", method = RequestMethod.GET)
	public ResponseEntity<Page> getPageLayout(HttpServletRequest request, @PathVariable("studyoid") String studyOid, @PathVariable("name") String name) {

		Page page = viewStudySubjectService.getPage(request, studyOid, name);
		if (page == null) {
			return new ResponseEntity<Page>(page, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Page>(page, org.springframework.http.HttpStatus.OK);
	}

}

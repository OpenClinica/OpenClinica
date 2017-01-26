package org.akaza.openclinica.controller;

import org.akaza.openclinica.service.multischema.SchemaChangingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/schema")
public class StudySubjectController {

	@Autowired
	private SchemaChangingService schemaChangingService;

	@RequestMapping(value = "/{tenantid}", method = RequestMethod.GET)
	public String studySubjects(@PathVariable String tenantid, Model model) {
		model.addAttribute("tenantid", tenantid);
		return "redirect:/MainMenu";
	}


}

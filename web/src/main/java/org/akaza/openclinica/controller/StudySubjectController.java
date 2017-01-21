package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.StudySubject;
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
	private StudySubjectDao studySubjectDao;

	@RequestMapping(value = "/{tenantid}", method = RequestMethod.GET)
	public String studySubjects(HttpSession session, @PathVariable String tenantid, Model model) {
		model.addAttribute("tenantid", tenantid);
		String studySubject = studySubjectDao.findById(1).getLabel();
		System.out.println("Study Subject:" + studySubject);
		model.addAttribute("studySubject", studySubject);

		return "multiSchemaResult";
	}

}

package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/schema")
public class StudySubjectController {

	@Autowired
	private StudySubjectDao studySubjectDao;

	@Autowired
	private ItemDataDao itemDataDao;

	@RequestMapping(value = "/{tenantId}", method = RequestMethod.GET)
	public String studySubjects(@PathVariable String tenantId, Model model) {
		model.addAttribute("tenantId", tenantId);
		return "redirect:/MainMenu";
	}

	@RequestMapping(value = "/tenant/{tenantId}", method = RequestMethod.GET)
	public String studySubjects(HttpSession session, @PathVariable String tenantId, Model model) {
		model.addAttribute("tenantId", tenantId);
		String studySubject = studySubjectDao.findById(1).getLabel();
		System.out.println("Study Subject:" + studySubject);
		model.addAttribute("studySubject", studySubject);

		return "multiSchemaResult";
	}
	@RequestMapping(value = "/post", method = RequestMethod.POST)
	public String postTenant(@RequestParam("studyOID") String tenantId, Model model) {
		model.addAttribute("tenantId", tenantId);
		String studySubject = studySubjectDao.findById(1).getLabel();
		System.out.println("Study Subject:" + studySubject);
		model.addAttribute("studySubject", studySubject);

		return "multiSchemaResult";
	}

	@RequestMapping(value = "/tenant/item_data/{tenantId}", method = RequestMethod.GET)
	public String getItemData(HttpSession session, @PathVariable String tenantId, Model model) {
		model.addAttribute("tenantId", tenantId);
		ItemData data = itemDataDao.findByItemEventCrfOrdinal(1, 1, 1);
		System.out.println("Item Data Value:" + data.getValue());
		model.addAttribute("item_data_value", data.getValue());

		return "itemDataResult";
	}
}

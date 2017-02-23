package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.service.LiquibaseOnDemandService;
import org.akaza.openclinica.service.ProtocolBuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/protocol")
public class ProtocolBuildController {
	@Autowired
	ProtocolBuildService protocolBuildService;
	@Autowired LiquibaseOnDemandService liquibaseOnDemandService;

	@RequestMapping(value = "/build", method = RequestMethod.GET)
	public @ResponseBody void createProtocol(
			@RequestParam String name,
			@RequestParam String id,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		System.out.println("name:" + name + " id: " + id);
		String schemaneName = protocolBuildService.process(name, id, id, request, response);
		liquibaseOnDemandService.process(schemaneName, name, id, id, request, response);
	}
}

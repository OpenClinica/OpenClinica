package org.akaza.openclinica.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.service.CustomRuntimeException;
import org.akaza.openclinica.service.OdmImportService;
import org.akaza.openclinica.service.Page;
import org.akaza.openclinica.service.PublishDTO;
import org.cdisc.ns.odm.v130.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class OdmImportController {

	@Autowired
	OdmImportService odmImportService;

	@Autowired
	private StudyDao studyDao;
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/auth/api/v1/studyversion/boardId/{boardId}", method = RequestMethod.POST)
	public ResponseEntity<Object> importOdmToOC(@RequestBody PublishDTO publishDTO, String boardId, HttpServletRequest request) throws Exception {

		ODM odm = publishDTO.getOdm();
		Page page = publishDTO.getPage();
		Instant start = Instant.now();

		try {
			Map<String, Object> map = (Map<String, Object>) odmImportService.importOdm(odm, page, boardId, request);
			Study study = (Study) map.get("study");
			Study publicStudy = studyDao.findPublicStudy(study.getOc_oid());
			odmImportService.updatePublicStudyPublishedFlag(publicStudy);
			odmImportService.setPublishedVersionsInFM(map, request);
			Instant end = Instant.now();
			logger.info("***** Time execustion for {} method : {}   *****", new Object() {
			}.getClass().getEnclosingMethod().getName(), Duration.between(start, end));

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (CustomRuntimeException e) {
			return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
		}

	}

}

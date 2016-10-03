package org.akaza.openclinica.web.restful_ssib;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.CycleDetectionStrategy;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.web.restful_ssib.dto.StudyEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
/**
 * Rest service for ODM clinical data usage
 * ROOT_CONTEXT/rest/personSubject/{format}/{personUid} format:xml/ json
 * mode:view
 * 
 * @author SJM
 */

//@Path("/personSubject")
@Controller
@RequestMapping("/studyEvent")
@Scope("prototype")
public class StudySubjectEventResource {
	private static final Logger LOGGER =
		LoggerFactory
			.getLogger(
				StudySubjectEventResource.class);

	private static final SimpleDateFormat INPUT_FORMAT =
		new SimpleDateFormat(
			"yyyyMMddHHmmss");
                                          
	private static final int INDENT_LEVEL = 2;

	@Autowired
	private StudyEventDao studyEventDao;


	public void setStudyEventDao(
		StudyEventDao studyEventDao) {
		this.studyEventDao =
			studyEventDao;
	}

	public StudyEventDao getStudyEventDao() {
		return
			this.studyEventDao;
	}

	@RequestMapping(
		value = "/json/view/{studySubjectId}/{date}",
		method = RequestMethod.GET)
	public ResponseEntity<Collection<StudyEventDto>> getOpenEventsBySubjectAndDate(
		@PathVariable("studySubjectId") int studySubjectID,
		@PathVariable("date") String eventOpenBefore)
			throws Exception {

		try {
			LOGGER.warn(
				"SJM: Entrando getOpenEventsBySubjectAndDate("
					+ studySubjectID
					+ ", "
					+ eventOpenBefore
					+ ")");

			Date date;
			synchronized (INPUT_FORMAT) {
				date =
					INPUT_FORMAT.
						parse(
							eventOpenBefore);
			}

			Collection<StudyEvent> studyEvents =
				this.
					getStudyEventDao().
					fetchListOpenEventsByStudySubjectAndDate(
						studySubjectID,
						date);

			ArrayList<StudyEventDto> results =
				new ArrayList<>();
			for (StudyEvent studyEvent : studyEvents) {
				results.
					add(
						new StudyEventDto(
							studyEvent));
			}

			LOGGER.warn(
				"SJM: Saliendo getOpenEventsByStudySubjectAndDate");

			return
				new ResponseEntity<Collection<StudyEventDto>>(
					results,
					HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.warn(
				"SJM: Excepcion: "
					+ e.getMessage(),
				e);
			throw e;
		}
	}
}


package es.ssib.otic.epona.openclinica.restful;

import org.akaza.openclinica.dao.hibernate.StudyDaoSsib;
import org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 *  Created by SJM.
 */
@Controller
@RequestMapping("/ssib/study")
@Scope("prototype")
public class StudySsibResource {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(
//			StudySsibResource.class);
			"org.akaza.openclinica.ssib.restful.StudySsibResource");
	@Autowired
	private StudyDaoSsib studyDaoSsib;

	public StudyDaoSsib getStudyDaoSsib() {

		return
			this.studyDaoSsib;
	}

	public void setStudyDaoSsib(
		StudyDaoSsib studyDaoSsib) {

		this.studyDaoSsib =
			studyDaoSsib;
	}


	public StudySsibResource()
		throws BeanInitializationException {

		LOGGER.info(
			"Inicializando StudySsibResource");
	}

	@RequestMapping(
		value = "/json/idFromOid/{studyOid}",
		method = RequestMethod.GET)
	public ResponseEntity<Integer> getStudyIdByOid(
		@PathVariable ("studyOid") String studyOid) {

		try {
			Study study =
				this.getStudyDaoSsib().
					findByOid(
						studyOid);

			ResponseEntity<Integer> response;
			if (study != null) {
				LOGGER.info(
					"Devolviendo id "
						+ study.getId()
						+ " para OID "
						+ studyOid);
				response =
					new ResponseEntity<Integer>(
						study.getStudyId(),
						HttpStatus.OK);
			} else {
				LOGGER.info(
					"No se ha encontrado estudio con OID "
						+ studyOid);
				response =
					new ResponseEntity<Integer>(
						HttpStatus.NOT_FOUND);
			}

			return
				response;
		} catch (Exception e) {
			LOGGER.warn(
				"Error buscando id de estudio con OID "
					+ studyOid,
				e);
			return
				new ResponseEntity<Integer>(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

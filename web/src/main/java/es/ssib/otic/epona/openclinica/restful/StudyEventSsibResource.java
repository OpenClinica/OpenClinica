package es.ssib.otic.epona.openclinica.restful;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import es.ssib.otic.epona.openclinica.restful.dto.IdResultDto;
/**
 *  Created by SJM.
 */
@Controller
@RequestMapping("/ssib/studyEvent")
@Scope("prototype")
public class StudyEventSsibResource {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(
//			StudySsibResource.class);
			"org.akaza.openclinica.ssib.restful.StudyEventSsibResource");

        @Autowired(
                required = true)
        private ApplicationContext applicationContext;

        public ApplicationContext getApplicationContext() {
                return
                        this.applicationContext;
        }

        public void setApplicationContext(
                ApplicationContext applicationContext) {

                this.applicationContext =
                        applicationContext;
        }

	public StudyEventSsibResource()
		throws BeanInitializationException {

		LOGGER.info(
			"Inicializando StudyEventSsibResource");
	}

	@RequestMapping(
		value = "/json/idFromOid/{studyEventDefinitionOid}",
		method = RequestMethod.GET)
	public ResponseEntity<IdResultDto> getStudyEventDefinitionIdByOid(
		@PathVariable ("studyEventDefinitionOid") String studyEventDefinitionOid) {

		try {

                        LOGGER.info(
                                "Entrando en createStudySubject");

                        DataSource ds =
                                (DataSource)
                                        this.applicationContext.getBean(
                                                "dataSource");

			StudyEventDefinitionDAO studyEventDefinitionDAO =
				new StudyEventDefinitionDAO(
					ds);

			StudyEventDefinitionBean studyEventDefinitionBean =
				studyEventDefinitionDAO.findByOid(
					studyEventDefinitionOid);

			LOGGER.info(
				"Devolviendo id "
					+ studyEventDefinitionBean.getId()
					+ " de evento con OID "
					+ studyEventDefinitionOid);

			IdResultDto resultado =
				new IdResultDto(
					studyEventDefinitionBean.getId());
			return
				new ResponseEntity<>(
					(IdResultDto) resultado,
					HttpStatus.OK);
		} catch (Exception e) {
			String message =
				"Error buscando id de definición de evento con OID "
					+ studyEventDefinitionOid;
			LOGGER.warn(
				message,
				e);
			return
				new ResponseEntity<>(
					new IdResultDto(
						message),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

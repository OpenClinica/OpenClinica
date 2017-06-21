package es.ssib.otic.epona.openclinica.restful;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.hibernate.StudySubjectDaoSsib;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.StudySubject;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import es.ssib.otic.epona.openclinica.restful.dto.CreateStudySubjectDto;
import es.ssib.otic.epona.openclinica.restful.dto.CreateStudySubjectEventDto;
import es.ssib.otic.epona.openclinica.restful.dto.CreateStudySubjectResultDto;
import es.ssib.otic.epona.openclinica.restful.dto.IdResultDto;
import es.ssib.otic.epona.openclinica.restful.dto.StudySubjectDto;

/**
 * Created by S004256 on 27/01/2017.
 */
@Controller
@RequestMapping("/ssib/studySubject")
@Scope("prototype")
public class StudySubjectSsibResource {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(
			"org.akaza.openclinica.ssib.restful.studySubjectSsibResource");
//			StudySubjectSsibResource.class);

	@Autowired(
		required = true)
	private StudySubjectDaoSsib studySubjectDaoSsib;

	public StudySubjectDaoSsib getStudySubjectDaoSsib() {
		return
			this.studySubjectDaoSsib;
	}

	public void setStudySubjectDaoSsib(
		StudySubjectDaoSsib studySubjectDaoSsib) {
		
		this.studySubjectDaoSsib =
			studySubjectDaoSsib;
	}

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

	public StudySubjectSsibResource()
		throws BeanInitializationException{

		LOGGER.info(
			"Inicializando StudySubjectSsibResource");
	}

	@RequestMapping(
		value = "/json/view/oid/{studyOid}",
		method = RequestMethod.GET)
	public ResponseEntity<Collection<StudySubjectDto>> getStudySubjectsByOid(
		@PathVariable ("studyOid") String studyOid) {

		try {
			Collection<StudySubjectDto> results =
				this.getStudySubjects(
					studyOid);
			return
				new ResponseEntity<Collection<StudySubjectDto>>(
					results,
					HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.warn(
				"Error obteniendo datos de sujetos enrolados en estudio con OID "
					+ studyOid,
				e);
			return
				new ResponseEntity<Collection<StudySubjectDto>>(
					new ArrayList<StudySubjectDto>(),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	public Collection<StudySubjectDto> getStudySubjects(
		String studyOid)
			throws Exception {

		try {
			Collection<StudySubject> studySubjects =
				this.getStudySubjectDaoSsib().
					findByStudyOid(
						studyOid);
			Collection<StudySubjectDto> results =
				new ArrayList<>();
			for (StudySubject studySubject : studySubjects) {
				results.add(
					new StudySubjectDto(
						studySubject));
			}

			return results;
		} catch (Exception e) {
			LOGGER.warn(
				"Excepción obteniendo pacientes enrolados en estudio con OID "
					+ studyOid,
				e);
			throw e;
		}
	}

	@RequestMapping(
		value = "/json/create",
		method = RequestMethod.POST)
	public ResponseEntity<CreateStudySubjectResultDto> createStudySubject(
		@RequestBody CreateStudySubjectDto createStudySubject) {

		try {

			LOGGER.info(
				"Entrando en createStudySubject");

			DataSource ds =
				(DataSource) 
					this.applicationContext.getBean(
						"dataSource");

			UserAccountBean ub =
				new UserAccountBean();
			ub.setId(1);
	
			SubjectBean subjectBean =
				null;	
			SubjectDAO subjectDao =
				new SubjectDAO(
					ds);
			if ((createStudySubject.getSubjectId() != null) && (createStudySubject.getSubjectId() != 0)) {
				LOGGER.info(
					"Recuperando subject por PK");
				subjectBean =
					(SubjectBean) subjectDao.findByPK(
						createStudySubject.getSubjectId().intValue());
			} else {
				String uniqueIdentifier =
					createStudySubject.getSubjectUniqueIdentifier();
				if ((uniqueIdentifier != null) && (!uniqueIdentifier.isEmpty())) {
					LOGGER.info("Recuperando Subject por UniqueIdentifier");
					subjectBean =
						subjectDao.
							findByUniqueIdentifier(
								createStudySubject.getSubjectUniqueIdentifier());
				} else {
					uniqueIdentifier =
						createStudySubject.getStudySubjectLabel();
				}
				if ((subjectBean == null) || (subjectBean.getId() == 0)) {
					LOGGER.info("Creando Subject");
					subjectBean =
						new SubjectBean();
					if (createStudySubject.getDateOfBirth() != null) {
						subjectBean.setDateOfBirth(
							createStudySubject.getDateOfBirth());
						subjectBean.setDobCollected(
							true);
					}
	
					subjectBean.setStatus(
						Status.AVAILABLE);
					subjectBean.setOwner(ub);
				
					subjectBean.setGender(
						createStudySubject.getGender());
					subjectBean.setUniqueIdentifier(
						uniqueIdentifier);

					subjectBean =
						subjectDao.create(
							subjectBean);
		
					if ((subjectBean == null) || (subjectBean.getId() == 0)) {
						LOGGER.warn("iNo se ha podido crear el subject");
						throw
							new Exception(
								"No se ha podido crear subject con uniqueIdentifier "
									+ createStudySubject.getSubjectUniqueIdentifier()
									+ ". Revise el log para ver los detalles.");
					}
				}
			}
			
			LOGGER.info("Subject encontratdo");
								
			StudyDAO studyDao =
				new StudyDAO(
					ds);
			StudyBean studyBean =
				studyDao.findByOid(
					createStudySubject.getStudyOid());
			if ((studyBean == null) || (studyBean.getId() == 0)) {
				LOGGER.warn(
					"No se ha encontrado estudio con OID "
						+ createStudySubject.getStudyOid());
				throw
					new Exception(
						"No se ha encontrado estudio con OID "
							+ createStudySubject.getStudyOid());
			}

			StudySubjectDAO studySubjectDao =
				new StudySubjectDAO(
					ds);
			StudySubjectBean studySubjectBean =
				studySubjectDao.findByLabelAndStudy(
					createStudySubject.getStudySubjectLabel(),
					studyBean);
			
			if ((studySubjectBean != null) && (studySubjectBean.getId() != 0)) {
				LOGGER.info(
					"Se ha encontrado StudySubject con label "
						+ createStudySubject.getStudySubjectLabel()
						+ " para el estudio con OID "
						+ createStudySubject.getStudyOid());
			} else {
				studySubjectBean =
					new StudySubjectBean();
				studySubjectBean.setLabel(
					createStudySubject.getStudySubjectLabel());
				studySubjectBean.setSecondaryLabel(
					createStudySubject.getStudySubjectSecondaryLabel());
				studySubjectBean.setStatus(
					Status.AVAILABLE);
				studySubjectBean.setStudyId(
					studyBean.getId());
				studySubjectBean.setSubjectId(
					subjectBean.getId());
				studySubjectBean.setOwner(
					ub);
	
				studySubjectBean =
					studySubjectDao.create(
						studySubjectBean,
						false);
				if ((studySubjectBean == null) || (studySubjectBean.getId() == 0)) {
					throw
						new Exception(
							"Error creando studySubject; revise logs para detalles");
				}
				// El método create sólo actualiza el id, no el OID.
				studySubjectBean =
					(StudySubjectBean) studySubjectDao.findByPK(
						studySubjectBean.getId());
			}
			CreateStudySubjectResultDto resultBean =
				new CreateStudySubjectResultDto();
			resultBean.setStudySubjectOid(
				studySubjectBean.getOid());
			resultBean.setStudySubjectId(
				studySubjectBean.getId());
			return
				new ResponseEntity<CreateStudySubjectResultDto>(
					resultBean,
					HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.warn(
				"Error creando StudySubject : "
					+ e.getMessage(),
				e);
			CreateStudySubjectResultDto errorBean =
				new CreateStudySubjectResultDto();
			errorBean.setErrorMessage(
				"Error creando Study Subject: "
					+ e.getMessage());
			return
				new ResponseEntity<CreateStudySubjectResultDto>(
					errorBean,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(
		value = "/json/createEvent",
		method = RequestMethod.POST)
	public ResponseEntity<IdResultDto> createStudySubject(
		@RequestBody CreateStudySubjectEventDto createStudySubjectEvent) {

                try {

                        LOGGER.info(
                                "Entrando en createStudySubject: eventId "
					+ createStudySubjectEvent.getStudyEventDefinitionId()
					+ ", studySubjectId "
					+ createStudySubjectEvent.getStudySubjectId());

                        DataSource ds =
                                (DataSource)
                                        this.applicationContext.getBean(
                                                "dataSource");

                        UserAccountBean ub =
                                new UserAccountBean();
                        ub.setId(1);

                        StudyEventDAO studyEventDao =
                                new StudyEventDAO(
                                        ds);

                        StudyEventBean studyEventBean =
				new StudyEventBean();
			studyEventBean.setStudyEventDefinitionId(
				createStudySubjectEvent.getStudyEventDefinitionId());
			studyEventBean.setStudySubjectId(
				createStudySubjectEvent.getStudySubjectId());
			int maxSampleOrdinal =
				studyEventDao.getMaxSampleOrdinal(
					createStudySubjectEvent.getStudyEventDefinitionId(),
					createStudySubjectEvent.getStudySubjectId());
			if (!createStudySubjectEvent.isCrearNuevo() && (maxSampleOrdinal != 0)) {
				LOGGER.info(
					"Ya hay un evento creado para la definición "
						+ createStudySubjectEvent.getStudyEventDefinitionId()
						+ " y studySubject "
						+ createStudySubjectEvent.getStudySubjectId());
				IdResultDto response =
					new IdResultDto(
						-1);
				return
					new ResponseEntity(
						response,
						HttpStatus.OK);
			}

			studyEventBean.setSampleOrdinal(
				maxSampleOrdinal + 1);
			LOGGER.info(
				"sampleOrdinal es " 
					+ maxSampleOrdinal);
			studyEventBean.setDateStarted(
				createStudySubjectEvent.getStartDate());
			studyEventBean.setDateEnded(
				createStudySubjectEvent.getEndDate());
			studyEventBean.setOwner(
				ub);
			studyEventBean.setStatus(
				Status.AVAILABLE);
			studyEventBean.setSubjectEventStatus(
				SubjectEventStatus.SCHEDULED);
			studyEventBean.setStartTimeFlag(
				false);
			studyEventBean.setEndTimeFlag(
				false);
			StudyEventBean result =
				(StudyEventBean) studyEventDao.create(
					studyEventBean);
			if ((result == null) || (result.getId() == 0)) {
				String message =
					"No se ha podido crear studyEvent para studySubject "
						+ createStudySubjectEvent.getStudySubjectId()
						+ "y eventDefinition "
						+ createStudySubjectEvent.getStudyEventDefinitionId();
				LOGGER.warn(
					message);
				IdResultDto response =
					new IdResultDto(
						message);
				return
					new ResponseEntity<>(
						response,
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			IdResultDto response =
				new IdResultDto(
					result.getId());
			return
				new ResponseEntity<>(
					response,
					HttpStatus.OK);
		} catch (Exception e) {
			String message =
				"Excepción creando studyEvent para studySubject "
					+ createStudySubjectEvent.getStudySubjectId()
					+ "y eventDefinitioon "
					+ createStudySubjectEvent.getStudyEventDefinitionId()
					+ ": "
					+ e.getMessage();
			LOGGER.warn(
				message,
				e);

			IdResultDto response =
				new IdResultDto(
					message);
			return
				new ResponseEntity<>(
					response,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

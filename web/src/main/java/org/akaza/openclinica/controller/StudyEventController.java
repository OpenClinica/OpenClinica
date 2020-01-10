/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/auth/api/v1/studyevent")
public class StudyEventController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;
	
	@Autowired
    private EventCrfDao eventCrfDao;
	
	@Autowired
    private StudyEventDao studyEventDao;

	@Autowired
    private StudySubjectDao studySubjectDao;

	@Autowired
	private StudyEventDefinitionDao studyEventDefinitionDao;
	
	@Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
	
	@Autowired
	private StudyParameterValueDao studyParameterValueDao;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


	/**
	 * @api {put} /pages/auth/api/v1/studyevent/studysubject/{studySubjectOid}/studyevent/{studyEventDefOid}/ordinal/{ordinal}/complete Complete a Participant Event
	 * @apiName completeParticipantEvent
	 * @apiPermission Authenticate using api-key. admin
	 * @apiVersion 1.0.0
	 * @apiParam {String} studySubjectOid Study Subject OID.
	 * @apiParam {String} studyEventDefOid Study Event Definition OID.
	 * @apiParam {Integer} ordinal Ordinal of Study Event Repetition.
	 * @apiGroup Form
	 * @apiHeader {String} api_key Users unique access-key.
	 * @apiDescription Completes a participant study event.
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 403 Forbidden
	 *                  {
	 *                  "code": "403",
	 *                  "message": "Request Denied.  Operation not allowed."
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "code": "200",
	 *                    "message": "Success."
	 *                    }
	 */
	@RequestMapping(value = "/studysubject/{studySubjectOid}/studyevent/{studyEventDefOid}/ordinal/{ordinal}/complete", method = RequestMethod.PUT)
	public @ResponseBody Map<String,String> completeParticipantEvent(HttpServletRequest request, @PathVariable("studySubjectOid") String studySubjectOid, 
			@PathVariable("studyEventDefOid") String studyEventDefOid,
			@PathVariable("ordinal") Integer ordinal)
			throws Exception {
		
		StudySubject subject = studySubjectDao.findByOcOID(studySubjectOid);
		StudyEvent studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(studyEventDefOid, ordinal, subject.getStudySubjectId());
		StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId());
		Study study = studyEventDefinition.getStudy();
		Map<String,String> response = new HashMap<String,String>();
		
		// Verify this request is allowed.
		if (!mayProceed(study)) {
			response.put("code", String.valueOf(HttpStatus.FORBIDDEN.value()));
			response.put("message", "Request Denied.  Operation not allowed.");
			return response;
		}
				
		// Get list of eventCRFs
		// By this point we can assume all Participant forms have been submitted at least once and have an event_crf entry.
		// Non-Participant forms may not have an entry.
		List<EventDefinitionCrf> eventDefCrfs = eventDefinitionCrfDao.findByStudyEventDefinitionId(studyEventDefinition.getStudyEventDefinitionId());
		List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubjectOid);
		
		
        try {
            completeData(studyEvent, eventDefCrfs, eventCrfs);
        } catch (Exception e) {
            // Transaction has been rolled back due to an exception.
            logger.error("Error encountered while completing Study Event: " + e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            response.put("code", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			response.put("message", "Error encountered while completing participant event.");
			return response;

        }

		
		response.put("code",  String.valueOf(HttpStatus.OK.value()));
		response.put("message", "Success.");
		return response;
		//return new ResponseEntity<String>("<message>Success</message>", org.springframework.http.HttpStatus.OK);

	}

	@Transactional
    private void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs) throws Exception{
		boolean completeStudyEvent = true;
		
		// Loop thru event CRFs and complete all that are participant events.
		for (EventDefinitionCrf eventDefCrf:eventDefCrfs) {
			boolean foundEventCrfMatch = false;
			for (EventCrf eventCrf:eventCrfs) {
				if (eventDefCrf.getCrf().getCrfId() == eventCrf.getCrfVersion().getCrf().getCrfId()) {
					foundEventCrfMatch = true;
					if (eventDefCrf.getParicipantForm()) {
						eventCrf.setStatusId(Status.UNAVAILABLE.getCode());
						eventCrfDao.saveOrUpdate(eventCrf);					
					} else if (eventCrf.getStatusId() != Status.UNAVAILABLE.getCode()) completeStudyEvent = false;
				}
			}
			if (!foundEventCrfMatch && !eventDefCrf.getParicipantForm()) completeStudyEvent = false;
		}
		
		// Complete study event only if there are no uncompleted, non-participant forms.
		if (completeStudyEvent) {
			studyEvent.setSubjectEventStatusId(4);
            StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,false);
            StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
			studyEventDao.saveOrUpdateTransactional(container);
		}
		
		
	}

	private boolean mayProceed(Study study) throws Exception {
        boolean accessPermission = false;

        StudyParameterValue pStatus = studyParameterValueDao.findByStudyIdParameter(study.getStudyId(), "participantPortal");
        ParticipantPortalRegistrar participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOc_oid()).toString(); // ACTIVE,PENDING,INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
        System.out.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }
}



package org.akaza.openclinica.web.pform;

import java.util.HashMap;
import javax.sql.DataSource;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class StudyEventResource {

    private final Logger log = LoggerFactory.getLogger(StudyEventResource.class);

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    /**
     * @api {post} /rest2/study/:studyOid/subject/:studySubjectOid/markEventAsCompleted Mark study event as completed
     * @apiName markEventAsCompleted
     * @apiPermission Authenticate using access code
     * @apiVersion 1.0.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectOid Study Subject Oid
     * @apiParam {String} accessCode OpenClinica participant's access code.
     * @apiParam {String} eventName Event name (not unique, used for validation).
     * @apiGroup Study Event
     * @apiDescription Let participant marks a survey as completed and begin filing in other surveys.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "accessCode": "01292525",
     *                  "eventName": "Event Name"
     *                  }
     * @apiErrorExample {json} Error-Response:
     *                  HTTP/1.1 400 Bad Request
     *                  {
     *                  "name": "Event Name",
     *                  "message": "VALIDATION FAILED",
     *                  "type": "",
     *                  "errors": [
     *                  {"field": "Type", "resource": "Event Object", "code": "Type Field cannot be blank."}
     *                  ],
     *                  "description": "Mark study event as completed",
     *                  "eventDefOid": null
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     */
    @RequestMapping(value = "/{studyOid}/studysubject/{studySubjectOid}/continueNextEvent", method = RequestMethod.POST)
    public ResponseEntity markEventAsCompleted(@RequestBody HashMap<String, Object> body, @PathVariable("studyOid") String studyOid,
            @PathVariable("studySubjectOid") String studySubjectOid) {

        log.debug("I'm in Mark Event as Completed");
        Errors errors = instanciateErrors();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", CoreResources.getField("portalURL"));

        // Required submitted fields.
        String accessCode = (String) body.get("accessCode");
        if (accessCode == null) {
            errors.rejectValue("accessCode", "RequiredField", "This field is required");
        }
        String eventName = (String) body.get("eventName");
        if (eventName == null) {
            errors.rejectValue("eventName", "RequiredField", "This field is required");
        }
        // Get study.
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyBean study = (StudyBean) studyDAO.findByOid(studyOid);
        // Get next event.
        StudyEventDAO eventDAO = new StudyEventDAO(dataSource);
        StudyEventBean currentEvent;
        // Get study subject.
        StudySubjectBean studySubject;
        try {
            currentEvent = (StudyEventBean) eventDAO.getNextScheduledEvent(studySubjectOid);
            studySubject = currentEvent.getStudySubject();
        } catch (NullPointerException e) {
            currentEvent = null;
            studySubject = null;
        }

        if (study == null || currentEvent == null || studySubject == null) {
            return new ResponseEntity(headers, HttpStatus.NOT_FOUND);
        } else if (!currentEvent.getName().equals(eventName)) {
            errors.rejectValue("eventName", "InvalidValue", "StudyEvent is not available for update");
        } else if (!(currentEvent.isActive() && (currentEvent.getSubjectEventStatus() == SubjectEventStatus.SCHEDULED
                || currentEvent.getSubjectEventStatus() == SubjectEventStatus.DATA_ENTRY_STARTED))) {
            errors.rejectValue("eventName", "InvalidValue", "StudyEvent has a Status other than Scheduled or Started");
        } else {
            // Check if participant has completed the survey or has filled all of its forms.
            for (int ii = 0; ii < currentEvent.getEventCRFs().size(); ii = ii + 1) {
                EventCRFBean crf = (EventCRFBean) currentEvent.getEventCRFs().get(ii);
                if (crf.getStatus() == Status.AVAILABLE) {
                    errors.rejectValue("eventName", "InvalidValue", "StudyEvent still has available forms");
                    break;
                }
            }
        }

        // User with accessCode exists.
        String userName = studyOid + "." + studySubjectOid;
		UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        UserAccountBean userAccount = (UserAccountBean) userAccountDAO.findByUserName(userName);
        if (userAccount == null || !userAccount.getAccessCode().equals(accessCode)) {
            errors.rejectValue("accessCode", "InvalidValue", "User not registered");
        } else if (!userAccount.isActive() || !studySubject.isActive()) {
            // Inactive user.
            errors.rejectValue("accessCode", "InvalidValue", "User account does not exist");
        }

        if (errors.hasErrors()) {
            return new ResponseEntity(errors.getAllErrors(), headers, HttpStatus.BAD_REQUEST);
        } else {
            // Update status to Completed
            currentEvent.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
            currentEvent = (StudyEventBean) eventDAO.update(currentEvent);
            return new ResponseEntity(currentEvent, headers, HttpStatus.OK);
        }
    }

    /**
     * Instantiate an Error object
     *
     * @return
     */
    public Errors instanciateErrors() {
        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        return errors;
    }
}

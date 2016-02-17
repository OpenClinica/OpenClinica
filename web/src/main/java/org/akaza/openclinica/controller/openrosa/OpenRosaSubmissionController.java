package org.akaza.openclinica.controller.openrosa;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/openrosa")
public class OpenRosaSubmissionController {

    @Autowired
    ServletContext context;

    @Autowired
    private OpenRosaSubmissionService openRosaSubmissionService;
    
    @Autowired
    private StudyDao studyDao;
    
    @Autowired
    private StudyParameterValueDao studyParameterValueDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String FORM_CONTEXT = "ecid";

    /**
     * @api {post} /pages/api/v1/editform/:studyOid/submission Submit form data
     * @apiName doSubmission
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used to look up subject context information while processing submission.
     * @apiGroup Form
     * @apiDescription Submits the data from a completed form.
     */

    @RequestMapping(value = "/{studyOID}/submission", method = RequestMethod.POST)
    public ResponseEntity<String> doSubmission(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("studyOID") String studyOID, @RequestParam(FORM_CONTEXT) String ecid) {

        HashMap<String, String> subjectContext = null;
        Locale locale = LocaleResolver.getLocale(request);

        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();

        try {
            // Verify Study is allowed to submit
            if (!mayProceed(studyOID))
                return new ResponseEntity<String>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

            Study study = studyDao.findByOcOID(studyOID);
            
            // Parse submission to extract payload
            String requestBody = IOUtils.toString(request.getInputStream(), "UTF-8");

            // Load user context from ecid
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);

            // Execute save as Hibernate transaction to avoid partial imports
            //OpenRosaSubmissionService service = new OpenRosaSubmissionService(locale, errors);
            openRosaSubmissionService.processRequest(study, subjectContext, requestBody, errors, locale);
        
        } catch (Exception e) {
            logger.error("Unsuccessful xform submission.");
            System.out.println(e.getMessage());
            System.out.println(ExceptionUtils.getStackTrace(e));
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            
            // Send a failure response
            return new ResponseEntity<String>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        if (!errors.hasErrors()) {
            // Log submission with Participate
            PformSubmissionNotificationService notifier = new PformSubmissionNotificationService();
            notifier.notify(studyOID, subjectContext);

            String responseMessage = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
            return new ResponseEntity<String>(responseMessage, org.springframework.http.HttpStatus.CREATED);
        } else {
            return new ResponseEntity<String>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private Study getParentStudy(String studyOid) {
        Study study = studyDao.findByOcOID(studyOid);
        Study parentStudy = study.getStudy();
        if (parentStudy != null && parentStudy.getStudyId() > 0)
            return parentStudy;
        else 
            return study;
    }


    private boolean mayProceed(String studyOid) throws Exception {
        return mayProceed(studyOid, null);
    }

    private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        ParticipantPortalRegistrar participantPortalRegistrar= new ParticipantPortalRegistrar();
        Study study = getParentStudy(studyOid);
        StudyParameterValue pStatus = studyParameterValueDao.findByStudyIdParameter(study.getStudyId(), "participantPortal");

        // ACTIVE, PENDING, or INACTIVE
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString();

        // enabled or disabled
        String participateStatus = pStatus.getValue().toString();

        // available, pending, frozen, or locked
        String studyStatus = study.getStatus().getName().toString(); 

        if (ssBean == null) {
            logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
            if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE"))
                accessPermission = true;
        } else {
            logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus
                    + "  studySubjectStatus: " + ssBean.getStatus().getName());
            if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")
                    && ssBean.getStatus() == Status.AVAILABLE)
                accessPermission = true;
        }
        return accessPermission;
    }
}

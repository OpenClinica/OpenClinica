package org.akaza.openclinica.controller;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/api/v1/editform")
public class EditFormController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    EnketoUrlService urlService;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    /**
     * @api {get} /pages/api/v1/editform/:studyOid/url Get Form Edit URL
     * @apiName getEditUrl
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used by enketo to cache form information.
     * @apiGroup Form
     * @apiDescription This API is used to retrieve a URL for a form with data pre-loaded into it
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "ecid":"a9f8f3aadea4b67e1f214140ccfdf70bad0b9e9b622a9776a3c85bbf6bb532cd"
     *                  }
     * @apiSuccessExample Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    http://ocform.oc.com:8005/edit/::YYYM?instance_id=
     *                    d16bba9200177fad34594e75d8b9565ff92b0bce4297e3b6c27275e531044a59
     *                    &returnUrl=http%3A%2F%2Fstudy1.mystudy.me%3A8080%2F%23%2Fevent%2FSS_SUB001%2Fdashboard&ecid=
     *                    d16bba9200177fad34594e75d8b9565ff92b0bce4297e3b6c27275e531044a59
     *                    }
     */

    @RequestMapping(value = "/{studyOid}/url", method = RequestMethod.GET)
    public ResponseEntity<String> getEditUrl(@RequestParam(FORM_CONTEXT) String formContext, @PathVariable("studyOid") String studyOID) throws Exception {

        String editURL = null;
        if (!mayProceed(studyOID))
            return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Load context
        PFormCache cache = PFormCache.getInstance(context);
        HashMap<String, String> subjectContextMap = cache.getSubjectContext(formContext);
        PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
        subjectContext.setStudyEventDefinitionId(Integer.valueOf(subjectContextMap.get("studyEventDefinitionID")));
        subjectContext.setCrfVersionOid(subjectContextMap.get("crfVersionOID"));
        subjectContext.setStudySubjectOid(subjectContextMap.get("studySubjectOID"));
        subjectContext.setOrdinal(Integer.valueOf(subjectContextMap.get("studyEventOrdinal")));

        editURL = urlService.getEditUrl(formContext, subjectContext, studyOID, null, null);
        logger.debug("Generating Enketo edit url for form: " + editURL);

        return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.ACCEPTED);

    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE ,
                                                                                                            // PENDING ,
                                                                                                            // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: "
                + siteStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available")
                && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }

}
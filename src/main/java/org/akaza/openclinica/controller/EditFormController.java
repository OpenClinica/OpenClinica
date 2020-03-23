package org.akaza.openclinica.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.domain.xform.XformParser;
import core.org.akaza.openclinica.domain.xform.dto.*;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.service.crfdata.EnketoUrlService;
import core.org.akaza.openclinica.service.crfdata.FormUrlObject;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import core.org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import core.org.akaza.openclinica.web.pform.OpenRosaServices;
import core.org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp2.BasicDataSource;
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
@RequestMapping(value = "/auth/api/editform")
public class EditFormController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    FormLayoutDao formLayoutDao;

    @Autowired
    EnketoUrlService urlService;

    @Autowired
    XformParser xformParser;

    @Autowired
    EnketoCredentials enketoCredentials;

    @Autowired
    OpenRosaServices openRosaServices;

    @Autowired
    private UtilService utilService;

    @Autowired
    private StudyBuildService studyBuildService;

    @Autowired
    private StudyDao sdao;
    private RestfulServiceHelper restfulServiceHelper;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    public static final String QUERY_FLAVOR = "-query";
    public static final String PARTICIPATE_FLAVOR = "-participate";
    public static final String NO_FLAVOR = "";
    public static final String CONTACTDATA = "contactdata";



    /**
     * @api {get} /pages/api/v1/editform/:studyOid/url Get Form Edit URL
     * @apiName getActionUrl
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
    public ResponseEntity<String> getEditUrl(@RequestParam(FORM_CONTEXT) String formContext, @PathVariable("studyOid") String studyOID , HttpServletRequest request) throws Exception {
        getRestfulServiceHelper().setSchema(studyOID, request);

        FormUrlObject editURL = null;
    //    if (!mayProceed(studyOID))
    //        return new ResponseEntity<String>(editURL.getFormUrl(), org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Load context
        PFormCache cache = PFormCache.getInstance(context);
        HashMap<String, String> subjectContextMap = cache.getSubjectContext(formContext);
        PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
        subjectContext.setStudyEventDefinitionId(subjectContextMap.get("studyEventDefinitionID"));
        subjectContext.setFormLayoutOid(subjectContextMap.get("formLayoutOID"));
        subjectContext.setStudyEventId(subjectContextMap.get("studyEventID"));

        subjectContext.setStudySubjectOid(subjectContextMap.get("studySubjectOID"));
        subjectContext.setOrdinal(subjectContextMap.get("studyEventOrdinal"));
        subjectContext.setUserAccountId(subjectContextMap.get("userAccountID"));
        UserAccountDAO udao = new UserAccountDAO(dataSource);
        UserAccountBean ub = (UserAccountBean) udao.findByPK(Integer.parseInt(subjectContextMap.get("userAccountID")));
        logger.info("Subject Context info *** {} *** ",subjectContext.toString());

        FormLayout formLayout = formLayoutDao.findByOcOID(subjectContext.getFormLayoutOid());
        Role role = Role.RESEARCHASSISTANT;
        String mode = EnketoAPI.PARTICIPATE_MODE;

        List <Bind> binds = openRosaServices.getBinds(formLayout,PARTICIPATE_FLAVOR,studyOID);
        boolean formContainsContactData=false;
        if(openRosaServices.isFormContainsContactData(binds))
            formContainsContactData=true;

        editURL = urlService.getActionUrl(formContext, subjectContext, studyOID, formLayout, PARTICIPATE_FLAVOR, null, role, mode, null, false,formContainsContactData,binds,ub);
        logger.debug("Generating Enketo edit url for form: " + editURL);

        return new ResponseEntity<String>(editURL.getFormUrl(), org.springframework.http.HttpStatus.ACCEPTED);

    }

    private Study getParentStudy(String studyOid) {
        Study study = getStudy(studyOid);
        if (study.isSite()) {
            return study;
        } else {
            Study parentStudy = study.getStudy();
            return parentStudy;
        }

    }

    private Study getStudy(String oid) {
        Study studyBean = (Study) sdao.findByOcOID(oid);
        return studyBean;
    }

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        Study siteStudy = getStudy(studyOid);
        Study study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOc_oid()).toString(); // ACTIVE ,
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
    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource, studyBuildService, sdao);
        }
        return restfulServiceHelper;
    }
}
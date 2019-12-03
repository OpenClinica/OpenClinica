package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.*;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import springfox.documentation.annotations.ApiIgnore;

import core.org.akaza.openclinica.bean.core.UserType;
import core.org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.dbcp2.BasicDataSource;
import org.cdisc.ns.odm.v130.ODM;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;

import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;

@Controller
public class OdmController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private ParticipateService participateService;

    @Autowired
    private RuleController ruleController;

    @Autowired
    private UtilService utilService;

    @Autowired
    private ParticipantService participantService;
    @Autowired
    StudySubjectDao studySubjectDao;
    
    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    private StudyBuildService studyBuildService;

    @Autowired
    private StudyDao studyDao;

    private RestfulServiceHelper restfulServiceHelper;

    private static final int INDENT_LEVEL = 2;

    /**
     * @api {get} /pages/odmk/studies/:studyOid/metadata Retrieve metadata
     * @apiName getStudyMetadata
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiGroup Study
     * @apiDescription Retrieve the metadata of the specified study
     * @apiParamExample {json} Request-Example:
     * {
     * "studyOid": "S_BL101",
     * }
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * {
     * The whole Study Metadata
     * }
     */

    @RequestMapping( value = "/odmk/studies/{study}/metadata", method = RequestMethod.GET )
    public ModelAndView getStudyMetadata(Model model, HttpSession session, @PathVariable( "study" ) String studyOid, HttpServletResponse response)
            throws Exception {

        if (!participateService.mayProceed(studyOid))
            return null;
        return ruleController.studyMetadata(model, session, studyOid, response);
    }

    /**
     * This URL needs to change ... Right now security disabled on this ... You can call this with
     * http://localhost:8080/OpenClinica-web-MAINLINE-SNAPSHOT /pages/odmk/studies/S_DEFAULTS1/events
     *
     * @param studyOid
     * @return
     * @throws Exception
     */
    /**
     * @api {get} /pages/odmk/study/:studyOid/studysubject/:studySubjectOid/events Retrieve an event - participant
     * @apiName getEvent
     * @apiPermission Module participate - enabled
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectOid Study Subject Oid
     * @apiGroup Study Event
     * @apiDescription Retrieve an event with earliest start date and ordinal.
     * @apiParamExample {json} Request-Example:
     * {
     * "studyOid": "S_BL101",
     * "studySubjectOid": "SS_DYN101"
     * }
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id": null,
     * "signature": [],
     * "clinicalData": [{
     * "annotations": [],
     * "subjectData": [{
     * "annotation": [],
     * "signature": null,
     * "status": "available",
     * "dateOfBirth": null,
     * "uniqueIdentifier": null,
     * "studyEventData": [{
     * "annotation": [],
     * "signature": null,
     * "status": null,
     * "eventName": "Scoring Visit",
     * "studyEventRepeatKey": null,
     * "endDate": null,
     * "formData": [{
     * "annotation": [],
     * "signature": null,
     * "status": "Not Started",
     * "interviewerName": null,
     * "formOID": "F_SCORING2_CRF_V10",
     * "itemGroupData": [],
     * "url":
     * "http://localhost:8006/::YYYF?iframe=true&ecid=a480dc4479409f6fe99a03d472f5cf77f4f12fb2b5ac471b9d35d737d934b042"
     * ,
     * "version": null,
     * "transactionType": null,
     * "auditRecord": null,
     * "archiveLayoutRef": null,
     * "formDataElementExtension": [],
     * "formRepeatKey": null,
     * "interviewDate": null,
     * "formName": "Scoring2_CRF",
     * "versionDescription": "Scoring2",
     * "statusChangeTimeStamp": null
     * }],
     * "studyEventOID": null,
     * "transactionType": null,
     * "auditRecord": null,
     * "studyEventDataElementExtension": [],
     * "studyEventLocation": null,
     * "startDate": "2015-08-27 12:00:00.0",
     * "subjectAgeAtEvent": null
     * }],
     * "studySubjectID": "DYN101",
     * "transactionType": null,
     * "yearOfBirth": null,
     * "auditRecord": null,
     * "investigatorRef": null,
     * "siteRef": null,
     * "subjectDataElementExtension": [],
     * "subjectKey": "SS_DYN101",
     * "secondaryID": null,
     * "sex": null
     * }],
     * "studyName": "Baseline Study 101",
     * "studyOID": "S_BL101",
     * "metaDataVersionOID": null,
     * "auditRecords": [],
     * "signatures": []
     * }],
     * "fileType": null,
     * "fileOID": null,
     * "description": null,
     * "study": [],
     * "association": [],
     * "odmversion": null,
     * "creationDateTime": null,
     * "adminData": [],
     * "referenceData": [],
     * "granularity": null,
     * "archival": null,
     * "priorFileOID": null,
     * "asOfDateTime": null,
     * "originator": null,
     * "sourceSystem": null,
     * "sourceSystemVersion": null
     * }
     */

    @RequestMapping( value = "/auth/api/events", method = RequestMethod.GET )
    public @ResponseBody
    ResponseEntity getEvent(HttpServletRequest request) throws Exception {
        ODM odm = null;
        String studyOid=(String)request.getSession().getAttribute("studyOid");
        UserAccountBean ub = utilService.getUserAccountFromRequest(request);
        getRestfulServiceHelper().setSchema(studyOid, request);
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        if(ub==null || !ub.hasUserType(UserType.PARTICIPATE)){
            logger.info("Responding with HttpStatus.FORBIDDEN because the user is either null or not of type participate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        logger.info("UserAccount username: " +ub.getName());
        Study currentStudy = participateService.getStudy(studyOid);
        logger.info("Study OId: " +currentStudy.getOc_oid());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        String userName=ub.getName();
        int lastIndexOfDot= userName.lastIndexOf(".");
        String subjectOid=userName.substring(lastIndexOfDot+1);

        StudySubjectBean studySubject= studySubjectDAO.findByOid(subjectOid);


        logger.info("StudySubject Id: " +studySubject.getLabel());

        Study siteBean = participateService.getStudyById(studySubject.getStudyId());

        if (participateService.mayProceed(siteBean.getOc_oid()) && studySubject != null && studySubject.isActive() && studySubject.getStatus().isAvailable()) {
            odm = participateService.getODM(studyOid, studySubject.getOid(), ub);
        }

        XMLSerializer xmlSerializer = new XMLSerializer();
        StringWriter sw = new StringWriter();
        if (odm == null) {
            odm = participateService.getOdmHeader(odm , currentStudy,studySubject);
        }

        JAXB.marshal(odm, sw);
        String xmlString = sw.toString();
        JSON json = xmlSerializer.read(xmlString);

        if(studySubject.getUserStatus().equals(UserStatus.CREATED) ||studySubject.getUserStatus().equals(UserStatus.INVITED)){
            studySubject.setUpdater(ub);
            studySubject.setUpdatedDate(new Date());
            studySubject.setUserStatus(UserStatus.ACTIVE);
            studySubjectDAO.update(studySubject);
        }

        return ResponseEntity.ok(json);

    }
    
    @ApiIgnore
    @RequestMapping( value = "/auth/api/participant-info", method = RequestMethod.GET )
    public @ResponseBody
    ResponseEntity getParticipant(HttpServletRequest request) throws Exception {       
        String studyOid=(String)request.getSession().getAttribute("studyOid");
        UserAccountBean ub = utilService.getUserAccountFromRequest(request);
        getRestfulServiceHelper().setSchema(studyOid, request);
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        if(ub==null || !ub.hasUserType(UserType.PARTICIPATE)){
            logger.info("Responding with HttpStatus.FORBIDDEN because the user is either null or not of type participate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        logger.info("UserAccount username: " +ub.getName());
        Study currentStudy = participateService.getStudy(studyOid);
        logger.info("Study OId: " +currentStudy.getOc_oid());
      
        String userName=ub.getName();
        int lastIndexOfDot= userName.lastIndexOf(".");
        String subjectOid=userName.substring(lastIndexOfDot+1);

        StudySubject studySubject= studySubjectDao.findByOcOID(subjectOid);
        String jsonStr = null; 
        if(studySubject != null) {
        	logger.info("StudySubject Id: " +studySubject.getLabel());
            
        	StudyParticipantDetailDTO spDTO= new StudyParticipantDetailDTO();
        	
            spDTO = participantService.buildStudyParticipantDetailDTO(studySubject);
            
           // convert to Json
            ObjectMapper Obj = new ObjectMapper();                                       
            jsonStr = Obj.writerWithDefaultPrettyPrinter().writeValueAsString(spDTO);                   
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity(jsonStr, headers, HttpStatus.OK);
    }
    
    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource, studyBuildService, studyDao);
        }
        return restfulServiceHelper;
    }
}

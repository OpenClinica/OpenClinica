package org.akaza.openclinica.controller;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.ParticipantEventService;
import org.akaza.openclinica.service.ParticipateService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.akaza.openclinica.web.restful.JSONClinicalDataPostProcessor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130.ODM;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionClinicalData;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionFormData;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionStudyEventData;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionSubjectData;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionLink;

import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionLinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OdmController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private ParticipateService participateService;

    @Autowired
    private RuleController ruleController;

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

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

    @RequestMapping( value = "/auth/api/studies/{studyOid}/events", method = RequestMethod.GET )
    public @ResponseBody
    String getEvent(@PathVariable( "studyOid" ) String studyOid, HttpServletRequest request) throws Exception {
        ODM odm = null;
        participateService.getRestfulServiceHelper().setSchema(studyOid, request);
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        UserAccountBean ub = participateService.getRestfulServiceHelper().getUserAccount(request);
        logger.info("UserAccount username: " +ub.getName());
        StudyBean currentStudy = participateService.getStudy(studyOid);
        logger.info("Study OId: " +currentStudy.getOid());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        String userName=ub.getName();
        int lastIndexOfDot= userName.lastIndexOf(".");
        String subjectOid=userName.substring(lastIndexOfDot+1);

        StudySubjectBean studySubject= studySubjectDAO.findByOid(subjectOid);


        logger.info("StudySubject Id: " +studySubject.getLabel());

        StudyBean siteBean = participateService.getStudyById(studySubject.getStudyId());

        if (participateService.mayProceed(siteBean.getOid()) && studySubject != null && studySubject.isActive() && studySubject.getStatus().isAvailable()) {
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
        return json.toString(INDENT_LEVEL);

    }

}

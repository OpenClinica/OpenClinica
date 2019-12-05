package org.akaza.openclinica.controller;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130.ODM;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionClinicalData;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionSubjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/odmss")
public class OdmStudySubjectController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    CoreResources coreResources;

    @Autowired
    ServletContext context;

    @Autowired
    AccountController accountController;
    @Autowired
    StudyDao sdao;
    ParticipantPortalRegistrar participantPortalRegistrar;
    public static final String FORM_CONTEXT = "ecid";

    private MessageSource messageSource;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * This URL needs to change ... Right now security disabled on this ... You can call this with
     * http://localhost:8080/OpenClinica-web-MAINLINE-SNAPSHOT /pages/odmk/studies/S_DEFAULTS1/events
     *
     * @param studyOid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/study/{studyOid}/crc/{crcUserName}/studysubject/{studySubjectId}", method = RequestMethod.GET)
    public @ResponseBody ODM getSubjectODM(@PathVariable("studyOid") String studyOid, @PathVariable("crcUserName") String crcUserName,
            @PathVariable("studySubjectId") String studySubjectLabel) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));

        return getODM(studyOid, studySubjectLabel, crcUserName);
    }

    private ODM getODM(String studyOID, String studySubjectLabel, String crcUserName) {

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        Study studyBean = null;
        StudySubjectBean studySubjectBean = null;
        try {
            // Retrieve crfs for next event
            studyBean = sdao.findByOcOID(studyOID);
            if (studyBean != null) {
                studySubjectBean = (StudySubjectBean) studySubjectDAO.findByLabelAndStudy(studySubjectLabel, studyBean);
                if (!mayProceed(studyOID, studySubjectBean))
                    return null;

                if (studySubjectBean.getId() != 0 && !accountController.isCRCHasAccessToStudySubject(studyOID, crcUserName, studySubjectLabel)) {
                    return createOdm(studyBean, studySubjectBean);

                } else if (studySubjectBean.getId() != 0 && accountController.isCRCHasAccessToStudySubject(studyOID, crcUserName, studySubjectLabel)) {
                    return null;

                } else {
                    return createOdm(studyBean, null);
                }
            } else {
                return createOdm(null, null);
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            logger.debug(ExceptionUtils.getStackTrace(e));
        }

        return null;

    }

    private ODM createOdm(Study studyBean, StudySubjectBean studySubjectBean) {
        ODM odm = new ODM();
        ODMcomplexTypeDefinitionClinicalData clinicalData = null;
        if (studyBean != null) {
            clinicalData = generateClinicalData(studyBean);

            if (studySubjectBean != null) {
                ODMcomplexTypeDefinitionSubjectData subjectData = generateSubjectData(studySubjectBean);
                clinicalData.getSubjectData().add(subjectData);
            }
            odm.getClinicalData().add(clinicalData);
        }
        return odm;
    }

    private ODMcomplexTypeDefinitionClinicalData generateClinicalData(Study study) {
        ODMcomplexTypeDefinitionClinicalData clinicalData = new ODMcomplexTypeDefinitionClinicalData();
        clinicalData.setStudyName(study.getName());
        clinicalData.setStudyOID(study.getOc_oid());
        return clinicalData;
    }

    private ODMcomplexTypeDefinitionSubjectData generateSubjectData(StudySubjectBean studySubject) {
        ODMcomplexTypeDefinitionSubjectData subjectData = new ODMcomplexTypeDefinitionSubjectData();
        subjectData.setSubjectKey(studySubject.getOid());
        subjectData.setStudySubjectID(studySubject.getLabel());
        subjectData.setStatus(studySubject.getStatus().getName());
        return subjectData;
    }

    public static boolean isAjaxRequest(String requestedWith) {
        return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
    }

    public static boolean isAjaxUploadRequest(HttpServletRequest request) {
        return request.getParameter("ajaxUpload") != null;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private Study getStudy(String oid) {
        Study studyBean = (Study) sdao.findByOcOID(oid);
        return studyBean;
    }

    private Study getParentStudy(String studyOid) {
        Study study = getStudy(studyOid);
        if (!study.isSite()) {
            return study;
        } else {
            Study parentStudy = study.getStudy();
            return parentStudy;
        }

    }

    private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        Study study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString(); // ACTIVE ,
                                                                                                      // PENDING ,
                                                                                                      // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus
                + "  studySubjectStatus: " + ssBean.getStatus().getName());
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")
                && ssBean.getStatus() == Status.AVAILABLE) {
            accessPermission = true;
        }
        return accessPermission;
    }

}

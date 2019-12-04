package org.akaza.openclinica.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletContext;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import core.org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/api/v2/anonymousform")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AnonymousFormControllerV2 {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    @Autowired
    private StudyDao sdao;

    /**
     * @api {post} /pages/api/v2/anonymousform/form Retrieve anonymous form URL
     * @apiName getEnketoForm
     * @apiPermission Module participate - enabled
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid
     * @apiParam {String} submissionUri Submission Url
     * @apiGroup Form
     * @apiDescription Retrieve anonymous form url.
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "submissionUri": "abcde"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "url":
     *                    "http://localhost:8006/::YYYi?iframe=true&ecid=abb764d026830e98b895ece6d9dcaf3c5e817983cc00a4ebfaabcb6c3700b4d5",
     *                    "offline": "false"
     *                    }
     */

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public ResponseEntity<AnonymousUrlResponse> getEnketoForm(@RequestBody HashMap<String, String> map) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        EventDefinitionCrfTagService tagService = (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context)
                .getBean("eventDefinitionCrfTagService");
        String formUrl = null;
        String studyOid = map.get("studyOid");

        if (!mayProceed(studyOid))
            return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        String submissionUri = map.get("submissionUri");
        if (submissionUri != "" && submissionUri != null) {

            Study study = getStudy(studyOid);

            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(dataSource);
            ArrayList<EventDefinitionCRFBean> edcBeans = edcdao.findAllSubmissionUriAndStudyId(submissionUri, study.getStudyId());
            if (edcBeans.size() != 0) {
                EventDefinitionCRFBean edcBean = edcBeans.get(0);
                CRFDAO crfdao = new CRFDAO(dataSource);
                FormLayoutDAO fldao = new FormLayoutDAO(dataSource);
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);

                FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(edcBean.getDefaultVersionId());
                CRFBean crf = (CRFBean) crfdao.findByPK(formLayout.getCrfId());
                Study sBean = (Study) sdao.findByPK(edcBean.getStudyId());
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(edcBean.getStudyEventDefinitionId());

                String tagPath = sedBean.getOid() + "." + crf.getOid();

                boolean isOffline = tagService.getEventDefnCrfOfflineStatus(2, tagPath, true);
                String offline = null;
                if (isOffline)
                    offline = "true";
                else
                    offline = "false";

                formUrl = createAnonymousEnketoUrl(sBean.getOc_oid(), formLayout, edcBean, isOffline);
                AnonymousUrlResponse anonResponse = new AnonymousUrlResponse(formUrl, offline, crf.getName(), formLayout.getDescription());

                return new ResponseEntity<AnonymousUrlResponse>(anonResponse, org.springframework.http.HttpStatus.OK);
            } else {
                return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            return new ResponseEntity<AnonymousUrlResponse>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
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

    private Study getStudy(String oid) {
        Study studyBean = (Study) sdao.findByOcOID(oid);
        return studyBean;
    }

    private String createAnonymousEnketoUrl(String studyOID, FormLayoutBean formLayout, EventDefinitionCRFBean edcBean, boolean isOffline) throws Exception {
        Study parentStudyBean = getParentStudy(studyOID);
        PFormCache cache = PFormCache.getInstance(context);
        String contextHash = cache.putAnonymousFormContext(studyOID, formLayout.getOid(), edcBean.getStudyEventDefinitionId());
        String enketoURL = cache.getPFormURL(parentStudyBean.getOc_oid(), formLayout.getOid(),null, isOffline, contextHash);
        String url = null;
        if (isOffline)
            url = enketoURL.split("#", 2)[0] + "?" + FORM_CONTEXT + "=" + contextHash + "#" + enketoURL.split("#", 2)[1];
        else
            url = enketoURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + formLayout.getName() + "= " + url);
        return url;

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

    private class AnonymousUrlResponse {
        private String url = null;
        private String offline = null;
        private String name = null;
        private String description = null;

        public AnonymousUrlResponse(String url, String offline, String name, String description) {
            this.url = url;
            this.offline = offline;
            this.name = name;
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getOffline() {
            return offline;
        }

        public void setOffline(String offline) {
            this.offline = offline;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

}

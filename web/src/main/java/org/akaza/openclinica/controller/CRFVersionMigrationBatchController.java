package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.rule.action.NotificationActionProcessor;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.akaza.openclinica.view.StudyInfoPanel;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.cdisc.ns.odm.v130_api.ODM;

/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller
@RequestMapping(value = "auth/api/v1/batchmigration")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class CRFVersionMigrationBatchController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    CoreResources coreResources;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    ResourceBundle resword, resformat, respage;
    HashMap<String, Object> hashMap = null;
    HttpServletRequest request;

    public CRFVersionMigrationBatchController() {
        super();
    }

    public CRFVersionMigrationBatchController(HashMap<String, Object> hashMap, HttpServletRequest request) {
        super();
        this.hashMap = hashMap;
        this.request = request;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<ArrayList<EventCRFBean>> runBatchCrfVersionMigration(@RequestBody HashMap<String, Object> hashMap, HttpServletRequest request)
            throws Exception {
        CRFVersionMigrationBatchController cmbController = new CRFVersionMigrationBatchController(hashMap, request);
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        System.out.println("I'm in run Batch CrfVersion Migration");
        int eventCrfCount = 0;

        boolean dryrun = Boolean.valueOf((String) hashMap.get("dryrun"));
        String studyOid = (String) hashMap.get("studyOid");
        String sourceCrfVersion = (String) hashMap.get("sourceCrfVersion");
        String targetCrfVersion = (String) hashMap.get("targetCrfVersion");
        ArrayList<String> studyEventDefnlist = (ArrayList<String>) hashMap.get("studyEventDefnlist");
        ArrayList<String> sitelist = (ArrayList<String>) hashMap.get("sitelist");

        ArrayList<EventCRFBean> migratableEventCrfs = new ArrayList<EventCRFBean>();
        ArrayList<EventCRFBean> nonMigratableEventCrfs = new ArrayList<EventCRFBean>();

        // May Proceed

        EventCRFDAO<String, ArrayList> ecdao = new EventCRFDAO<String, ArrayList>(dataSource);
        CRFVersionDAO<String, ArrayList> cvdao = new CRFVersionDAO<String, ArrayList>(dataSource);
        StudyEventDAO sedao = new StudyEventDAO(dataSource);
        StudyEventDefinitionDAO<String, ArrayList> seddao = new StudyEventDefinitionDAO<String, ArrayList>(dataSource);
        StudySubjectDAO<String, ArrayList> ssdao = new StudySubjectDAO<String, ArrayList>(dataSource);
        StudyDAO<String, ArrayList> sdao = new StudyDAO<String, ArrayList>(dataSource);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(dataSource);
        UserAccountDAO uadao = new UserAccountDAO(dataSource);

        CRFVersionBean sourceCrfVersionBean = cvdao.findByOid(sourceCrfVersion);
        CRFVersionBean targetCrfVersionBean = cvdao.findByOid(targetCrfVersion);

        StudyBean stBean = sdao.findByOid(studyOid);
        if (stBean == null)
            return new ResponseEntity<ArrayList<EventCRFBean>>(migratableEventCrfs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        StudyUserRoleBean suRole = uadao.findRoleByUserNameAndStudyId(getCurrentUser(request).getName(), stBean.getId());
        Role r = suRole.getRole();
        if (suRole == null || !(r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)))
            return new ResponseEntity<ArrayList<EventCRFBean>>(migratableEventCrfs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (sourceCrfVersionBean == null || targetCrfVersionBean == null || sourceCrfVersionBean.getCrfId() != targetCrfVersionBean.getCrfId()
                || sourceCrfVersionBean.getId() == targetCrfVersionBean.getId() || sourceCrfVersionBean.getStatus().isUnavailable()
                || targetCrfVersionBean.getStatus().isUnavailable())
            return new ResponseEntity<ArrayList<EventCRFBean>>(migratableEventCrfs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        for (String site : sitelist) {
            StudyBean siteBean = (StudyBean) sdao.findByOid(site);
            if (siteBean == null || siteBean.getStatus().isUnavailable())
                return new ResponseEntity<ArrayList<EventCRFBean>>(migratableEventCrfs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }

        for (String studyEventDefn : studyEventDefnlist) {
            StudyEventDefinitionBean sedefnBean = (StudyEventDefinitionBean) seddao.findByOid(studyEventDefn);
            if (sedefnBean == null || sedefnBean.getStatus().isUnavailable())
                return new ResponseEntity<ArrayList<EventCRFBean>>(migratableEventCrfs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }

        ArrayList<EventCRFBean> eventCRFBeans = ecdao.findByCrfVersion(sourceCrfVersionBean);
        for (EventCRFBean eventCRFBean : eventCRFBeans) {
            StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eventCRFBean.getStudySubjectId());
            StudyBean sBean = (StudyBean) sdao.findByPK(ssBean.getStudyId());
            StudyEventBean seBean = (StudyEventBean) sedao.findByPK(eventCRFBean.getStudyEventId());
            StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(seBean.getStudyEventDefinitionId());
            EventDefinitionCRFBean edcBean = edcdao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(sedBean.getId(), sourceCrfVersionBean.getCrfId(),
                    sBean.getId());
            boolean sourceVersionPass = true;
            boolean targetVersionPass = true;
            if (edcBean != null) {
                if (!edcBean.getSelectedVersionIds().equals("")) {
                    List<String> selectedVersions = new ArrayList<String>(Arrays.asList(edcBean.getSelectedVersionIds().split(",")));
                    sourceVersionPass = false;
                    targetVersionPass = false;
                    for (String selectedVersion : selectedVersions) {
                        if (selectedVersion.equals(String.valueOf(sourceCrfVersionBean.getId()))) {
                            sourceVersionPass = true;
                        }
                        if (selectedVersion.equals(String.valueOf(targetCrfVersionBean.getId()))) {
                            targetVersionPass = true;
                        }
                    }
                    if (!sourceVersionPass) {
                        System.out.println(sedBean.getOid() + " in " + sBean.getOid() + " source crf version not available for study_subject "
                                + ssBean.getOid());
                    }
                    if (!targetVersionPass) {
                        System.out.println(sedBean.getOid() + " in " + sBean.getOid() + " target crf version not available for study_subject "
                                + ssBean.getOid());
                    }
                }
            }
            if (sourceVersionPass && targetVersionPass) {
                if ((studyEventDefnlist.contains(sedBean.getOid()) || studyEventDefnlist.size() == 0)
                        && (sitelist.contains(sBean.getOid()) || sitelist.size() == 0)) {
                    System.out.println("In List:  " + ssBean.getOid() + " - " + sedBean.getOid() + " - " + sBean.getOid() + " - " + eventCRFBean.getId());
                    eventCrfCount++;
                    migratableEventCrfs.add(eventCRFBean);
                } else {
                    System.out.println("Not In List:  " + ssBean.getOid() + " - " + sedBean.getOid() + " - " + sBean.getOid() + " - " + eventCRFBean.getId());
                    nonMigratableEventCrfs.add(eventCRFBean);
                }
            }

        }
        if (!dryrun) {
            for (EventCRFBean migratableEventCrf : migratableEventCrfs) {
                executeMigrationAction(migratableEventCrf, targetCrfVersionBean, request);
            }
        }
        return new ResponseEntity<ArrayList<EventCRFBean>>(migratableEventCrfs, org.springframework.http.HttpStatus.OK);

    }

    public void executeMigrationAction(EventCRFBean eventCRFBEan, CRFVersionBean targetCrfVersionBean, HttpServletRequest request) {
        try {
            EventCRFDAO event_crf_dao = new EventCRFDAO(dataSource);
            StudyEventDAO sedao = new StudyEventDAO(dataSource);

            EventCRFBean ev_bean = (EventCRFBean) event_crf_dao.findByPK(eventCRFBEan.getId());
            StudyEventBean st_event_bean = (StudyEventBean) sedao.findByPK(ev_bean.getStudyEventId());

            Connection con = dataSource.getConnection();
            con.setAutoCommit(false);
            event_crf_dao.updateCRFVersionID(eventCRFBEan.getId(), targetCrfVersionBean.getId(), getCurrentUser(request).getId(), null);

            String status_before_update = null;
            SubjectEventStatus eventStatus = null;
            Status subjectStatus = null;
            AuditDAO auditDao = new AuditDAO(dataSource);

            // event signed, check if subject is signed as well
            StudySubjectDAO studySubDao = new StudySubjectDAO(dataSource);
            StudySubjectBean studySubBean = (StudySubjectBean) studySubDao.findByPK(st_event_bean.getStudySubjectId());
            if (studySubBean.getStatus().isSigned()) {
                status_before_update = auditDao.findLastStatus("study_subject", studySubBean.getId(), "8");
                if (status_before_update != null && status_before_update.length() == 1) {
                    int subject_status = Integer.parseInt(status_before_update);
                    subjectStatus = Status.get(subject_status);
                    studySubBean.setStatus(subjectStatus);
                }
                studySubBean.setUpdater(getCurrentUser(request));
                studySubDao.update(studySubBean, null);
            }
            st_event_bean.setUpdater(getCurrentUser(request));
            st_event_bean.setUpdatedDate(new Date());

            status_before_update = auditDao.findLastStatus("study_event", st_event_bean.getId(), "8");
            if (status_before_update != null && status_before_update.length() == 1) {
                int status = Integer.parseInt(status_before_update);
                eventStatus = SubjectEventStatus.get(status);
                st_event_bean.setSubjectEventStatus(eventStatus);
            }
            sedao.update(st_event_bean, null);

            con.commit();
            con.setAutoCommit(true);
            con.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private UserAccountBean getCurrentUser(HttpServletRequest request) {
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        return ub;
    }

}

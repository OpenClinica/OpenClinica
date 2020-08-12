/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.admin.AuditEventBean;
import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.admin.StudyEventAuditBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateNewStudyEventServlet;
import org.akaza.openclinica.control.submit.SubmitDataUtil;
import core.org.akaza.openclinica.dao.admin.AuditEventDAO;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.service.crfdata.HideCRFManager;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.bean.DisplayStudyEventRow;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author jxu
 *
 *         Processes 'view subject' request
 */
public class ViewStudySubjectServlet extends SecureController {
    // The study subject has an existing discrepancy note related to their
    // person id; this
    // value will be saved as a request attribute
    public final static String HAS_UNIQUE_ID_NOTE = "hasUniqueIDNote";
    // The study subject has an existing discrepancy note related to their date
    // of birth; this
    // value will be saved as a request attribute
    public final static String HAS_DOB_NOTE = "hasDOBNote";
    // The study subject has an existing discrepancy note related to their
    // Gender; this
    // value will be saved as a request attribute
    public final static String HAS_GENDER_NOTE = "hasGenderNote";
    // The study subject has an existing discrepancy note related to their
    // Enrollment Date; this
    // value will be saved as a request attribute
    public final static String HAS_ENROLLMENT_NOTE = "hasEnrollmentNote";
    // request attribute for a discrepancy note
    public final static String UNIQUE_ID_NOTE = "uniqueIDNote";
    // request attribute for a discrepancy note
    public final static String DOB_NOTE = "dOBNote";
    // request attribute for a discrepancy note
    public final static String GENDER_NOTE = "genderNote";
    // request attribute for a discrepancy note
    public final static String ENROLLMENT_NOTE = "enrollmentNote";
    public final static String COMMON = "common";
    public final static String OPEN_BRACKET = "[";
    public final static String CLOSE_BRACKET = "]";
    public final static String DOT_ESCAPED = "\\.";

    public final static String visitBasedEventItempath=CoreResources.getField("visitBasedEventItem");

    @Autowired
    private StudyEventDAO studyEventDAO;
    @Autowired
    private EventCRFDAO eventCRFDAO;
    @Autowired
    private StudySubjectDAO studySubjectDAO;
    @Autowired
    private SubjectDAO subjectDAO;
    @Autowired
    FormLayoutDAO formLayoutDAO;
    @Autowired
    private CRFDAO crfDAO;
    @Autowired
    private ItemDataDAO itemDataDAO;
    @Autowired
    private ItemDAO itemDAO;
    @Autowired
    private SubjectGroupMapDAO subjectGroupMapDAO;
    @Autowired
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    @Autowired
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    @Autowired
    private AuditEventDAO auditEventDAO;
    @Autowired
    UserAccountDAO userAccountDAO;

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        // YW 10-18-2007, if a study subject with passing parameter does not
        // belong to user's studies, it can not be viewed
        // mayAccess();
        getEventCrfLocker().unlockAllForUser(ub.getId());
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataUtil.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_study_director"), "1");
    }


    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studySubId = fp.getInt("id", true);// studySubjectId
        String from = fp.getString("from");
        int parentStudyId = currentStudy.isSite() ? currentStudy.getStudy().getStudyId() : currentStudy.getStudyId();
        if(currentStudy.isSite()){
            currentStudy.setSubjectIdGeneration(currentStudy.getStudy().getSubjectIdGeneration());
        }

        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        // if coming from change crf version -> display message
        String crfVersionChangeMsg = fp.getString("isFromCRFVersionChange");
        if (crfVersionChangeMsg != null && !crfVersionChangeMsg.equals("")) {
            addPageMessage(crfVersionChangeMsg);

        }
        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            if (!StringUtils.isBlank(from)) {
                request.setAttribute("from", from); // form ListSubject or
                // ListStudySubject
            } else {
                request.setAttribute("from", "");
            }
            StudySubjectBean studySub = (StudySubjectBean) studySubjectDAO.findByPK(studySubId);

            request.setAttribute("studySub", studySub);
            Study studyRelatedTostudySub = (Study) getStudyDao().findById(studySub.getStudyId());
            request.setAttribute("studyRelatedTostudySub",studyRelatedTostudySub);
            request.setAttribute("originatingPage", URLEncoder.encode("ViewStudySubject?id=" + studySub.getId(), "UTF-8"));

            int studyId = studySub.getStudyId();
            int subjectId = studySub.getSubjectId();

            Study study = (Study) getStudyDao().findByPK(studyId);
            // Check if this StudySubject would be accessed from the Current Study
            if (studySub.getStudyId() != currentStudy.getStudyId()) {
                if (currentStudy.isSite()) {
                    addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                    forwardPage(Page.MENU_SERVLET);
                    return;
                } else {
                    // The SubjectStudy is not belong to currentstudy and current study is not a site.
                    Collection sites = getStudyDao().findOlnySiteIdsByStudy(currentStudy);
                    if (!sites.contains(study.getStudyId())) {
                        addPageMessage(
                                respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                        forwardPage(Page.MENU_SERVLET);
                        return;
                    }
                }
            }
            // If the study subject derives from a site, and is being viewed
            // from a parent study,
            // then the study IDs will be different. However, since each note is
            // saved with the specific
            // study ID, then its study ID may be different than the study
            // subject's ID.
            boolean subjectStudyIsCurrentStudy = studyId == currentStudy.getStudyId();
            boolean isParentStudy = !study.isSite();

            // Get any disc notes for this subject : studySubId
            List<DiscrepancyNoteBean> allNotesforSubject = new ArrayList<DiscrepancyNoteBean>();

            // These methods return only parent disc notes
            if (subjectStudyIsCurrentStudy && isParentStudy) {
                allNotesforSubject = discrepancyNoteDAO.findAllSubjectByStudyAndId(study, subjectId);
                allNotesforSubject.addAll(discrepancyNoteDAO.findAllStudySubjectByStudyAndId(study, studySubId));
            } else {
                if (!isParentStudy) {
                    Study stParent = study.getStudy();
                    allNotesforSubject = discrepancyNoteDAO.findAllSubjectByStudiesAndSubjectId(stParent, study, subjectId);
                    allNotesforSubject.addAll(discrepancyNoteDAO.findAllStudySubjectByStudiesAndStudySubjectId(stParent, study, studySubId));
                } else {
                    allNotesforSubject = discrepancyNoteDAO.findAllSubjectByStudiesAndSubjectId(currentStudy, study, subjectId);
                    allNotesforSubject.addAll(discrepancyNoteDAO.findAllStudySubjectByStudiesAndStudySubjectId(currentStudy, study, studySubId));
                }
            }

            if (!allNotesforSubject.isEmpty()) {
                setRequestAttributesForNotes(allNotesforSubject);
            }

            SubjectBean subject = (SubjectBean) subjectDAO.findByPK(subjectId);
            if (currentStudy.getCollectDob().equals("2")) {
                Date dob = subject.getDateOfBirth();
                if (dob != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dob);
                    int year = cal.get(Calendar.YEAR);
                    request.setAttribute("yearOfBirth", new Integer(year));
                } else {
                    request.setAttribute("yearOfBirth", "");
                }
            }

            request.setAttribute("subject", subject);

            if (isParentStudy) {
                study.setCollectDob(currentStudy.getCollectDob());
            }

            // YW >>
            request.setAttribute("subjectStudy", study);

            if (study.isSite()) {// this is a site,find parent
                Study parentStudy2 = study.getStudy();
                request.setAttribute("parentStudy", parentStudy2);
            } else {
                request.setAttribute("parentStudy", new Study());
            }

            ArrayList children = (ArrayList) subjectDAO.findAllChildrenByPK(subjectId);
            request.setAttribute("children", children);

            // find study events
            StudySubjectService studySubjectService = (StudySubjectService) WebApplicationContextUtils.getWebApplicationContext(getServletContext())
                    .getBean("studySubjectService");
            List<DisplayStudyEventBean> displayEvents = studySubjectService.getDisplayStudyEventsForStudySubject(studySub, ub, currentRole, study);
            List<DisplayStudyEventBean> tempList = new ArrayList<>();
            for (DisplayStudyEventBean displayEvent : displayEvents) {
                if (!displayEvent.getStudyEvent().getStudyEventDefinition().getType().equals(COMMON)) {
                    tempList.add(displayEvent);
                }
            }
            displayEvents = new ArrayList(tempList);
            List<String> itemPathList =null;
            String givenStudyOid=null ;
            String givenEventOid=null ;
            String givenFormOid=null ;
            String givenGroupRepeat=null ;
            String givenItemOid=null ;
            if(!StringUtils.isEmpty(visitBasedEventItempath)) {
                 itemPathList = Arrays.asList(visitBasedEventItempath.split("\\s*,\\s*"));
            }
            Study parentStudyBean = null;
            if(currentStudy.isSite())
                parentStudyBean = currentStudy.getStudy();
            else
                parentStudyBean = currentStudy;

                for (int i = 0; i < displayEvents.size(); i++) {
                    DisplayStudyEventBean decb = displayEvents.get(i);
                    StudyEventBean seBean = decb.getStudyEvent();
                    StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(seBean.getStudyEventDefinitionId());

                   if(itemPathList!=null) {
                       for (String itemPath : itemPathList) {
                           givenStudyOid = itemPath.split(DOT_ESCAPED)[0].trim();
                           givenEventOid = itemPath.split(DOT_ESCAPED)[1].trim();
                           givenFormOid = itemPath.split(DOT_ESCAPED)[2].trim();
                           givenGroupRepeat = StringUtils.substringBetween(itemPath.split(DOT_ESCAPED)[3], OPEN_BRACKET, CLOSE_BRACKET).trim();
                           givenItemOid = itemPath.split(DOT_ESCAPED)[4].trim();
                           if (
                                   parentStudyBean.getOc_oid().equals(givenStudyOid)
                                           && sedBean.getOid().equals(givenEventOid)
                           ) {
                               List<EventCRFBean> eventCRFBeans = eventCRFDAO.findAllByStudyEvent(seBean);
                               for (EventCRFBean eventCRFBean : eventCRFBeans) {
                                   FormLayoutBean formLayoutBean = (FormLayoutBean) formLayoutDAO.findByPK(eventCRFBean.getFormLayoutId());
                                   CRFBean crfBean = (CRFBean) crfDAO.findByPK(formLayoutBean.getCrfId());

                                   if (crfBean.getOid().equals(givenFormOid)) {
                                       List<ItemBean> itemBeans = itemDAO.findByOid(givenItemOid);
                                       if (itemBeans != null) {
                                           ItemDataBean itemDataBean = itemDataDAO.findByItemIdAndEventCRFIdAndOrdinal(itemBeans.get(0).getId(), eventCRFBean.getId(), Integer.valueOf(givenGroupRepeat));
                                           if (itemDataBean != null && itemDataBean.getId() != 0)
                                               decb.getStudyEvent().setAdditionalNotes(itemDataBean.getValue());
                                       }
                                       break;
                                   }
                               }

                           }
                       }
                   }


            }
            if (currentStudy.isSite()) {
                HideCRFManager hideCRFManager = HideCRFManager.createHideCRFManager();
                for (DisplayStudyEventBean displayStudyEventBean : displayEvents) {
                    hideCRFManager.removeHiddenEventCRF(displayStudyEventBean);
                }
            }

            EntityBeanTable table = fp.getEntityBeanTable();
            table.setSortingIfNotExplicitlySet(1, false);// sort by start
            // date, desc
            ArrayList allEventRows = DisplayStudyEventRow.generateRowsFromBeans(displayEvents);

            String[] columns = { resword.getString("event") + " (" + resword.getString("occurrence_number") + ")", resword.getString("start_date1"),
                    resword.getString("status"), resword.getString("event_actions"), resword.getString("CRFs") };
            table.setColumns(new ArrayList(Arrays.asList(columns)));
            table.hideColumnLink(4);
            table.hideColumnLink(5);
            if (!"removed".equalsIgnoreCase(studySub.getStatus().getName()) && !"auto-removed".equalsIgnoreCase(studySub.getStatus().getName())) {
                if (currentStudy.getStatus().isAvailable() && !currentRole.getRole().equals(Role.MONITOR)) {
                    table.addLink(resword.getString("add_new"),
                            "CreateNewStudyEvent?" + CreateNewStudyEventServlet.INPUT_STUDY_SUBJECT_ID_FROM_VIEWSUBJECT + "=" + studySub.getId());
                }
            }
            HashMap args = new HashMap();
            args.put("id", new Integer(studySubId).toString());
            table.setQuery("ViewStudySubject", args);
            table.setRows(allEventRows);
            table.setFilterPlaceHolder("enter_event_name");
            table.computeDisplayWithFilteringUsingContains();

            request.setAttribute("table", table);
            ArrayList groupMaps = (ArrayList) subjectGroupMapDAO.findAllByStudySubject(studySubId);
            request.setAttribute("groups", groupMaps);

            // find audit log for events
            ArrayList logs = auditEventDAO.findEventStatusLogByStudySubject(studySubId);
            // logger.warning("^^^ retrieved logs");
            ArrayList eventLogs = new ArrayList();
            // logger.warning("^^^ starting to iterate");
            for (int i = 0; i < logs.size(); i++) {
                AuditEventBean avb = (AuditEventBean) logs.get(i);
                StudyEventAuditBean sea = new StudyEventAuditBean();
                sea.setAuditEvent(avb);
                StudyEventBean se = (StudyEventBean) studyEventDAO.findByPK(avb.getEntityId());
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(se.getStudyEventDefinitionId());
                sea.setDefinition(sed);
                String old = avb.getOldValue().trim();
                try {
                    if (!StringUtils.isBlank(old)) {
                        SubjectEventStatus oldStatus = SubjectEventStatus.get(new Integer(old).intValue());
                        sea.setOldSubjectEventStatus(oldStatus);
                    }
                    String newValue = avb.getNewValue().trim();
                    if (!StringUtils.isBlank(newValue)) {
                        SubjectEventStatus newStatus = SubjectEventStatus.get(new Integer(newValue).intValue());
                        sea.setNewSubjectEventStatus(newStatus);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Subject event status is not able to be fetched: ",e);
                }
                UserAccountBean updater = (UserAccountBean) userAccountDAO.findByPK(avb.getUserId());
                sea.setUpdater(updater);
                eventLogs.add(sea);
            }
            request.setAttribute("eventLogs", eventLogs);
            String errorData = request.getParameter("errorData");
            if (StringUtils.isNotEmpty(errorData))
                request.setAttribute("errorData", errorData);
            Study tempParentStudy = currentStudy.isSite() ? currentStudy.getStudy() : currentStudy;
            request.setAttribute("participateStatus", getParticipateStatus(tempParentStudy).toLowerCase());
            Study subjectStudy= getStudyDao().findByPK(studySub.getStudyId());
            request.setAttribute("subjectStudy" ,subjectStudy);
            forwardPage(Page.VIEW_STUDY_SUBJECT);
        }
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    private void setRequestAttributesForNotes(List<DiscrepancyNoteBean> discBeans) {
        for (DiscrepancyNoteBean discrepancyNoteBean : discBeans) {
            if ("unique_identifier".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_UNIQUE_ID_NOTE, "yes");
                request.setAttribute(UNIQUE_ID_NOTE, discrepancyNoteBean);
            } else if ("date_of_birth".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_DOB_NOTE, "yes");
                request.setAttribute(DOB_NOTE, discrepancyNoteBean);
            } else if ("enrollment_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_ENROLLMENT_NOTE, "yes");
                request.setAttribute(ENROLLMENT_NOTE, discrepancyNoteBean);
            } else if ("gender".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_GENDER_NOTE, "yes");
                request.setAttribute(GENDER_NOTE, discrepancyNoteBean);
            }

        }

    }

}

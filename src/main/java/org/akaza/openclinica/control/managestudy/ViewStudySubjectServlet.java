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
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.*;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateNewStudyEventServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import core.org.akaza.openclinica.dao.admin.AuditEventDAO;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.service.crfdata.HideCRFManager;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
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

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_study_director"), "1");
    }

    public static ArrayList<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, DataSource ds, UserAccountBean ub,
            StudyUserRoleBean currentRole, StudyDao studyDao) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);

        ArrayList events = sedao.findAllByStudySubject(studySub);

        ArrayList displayEvents = new ArrayList();
        for (int i = 0; i < events.size(); i++) {
            StudyEventBean event = (StudyEventBean) events.get(i);
            StudySubjectBean studySubject = (StudySubjectBean) ssdao.findByPK(event.getStudySubjectId());

            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            // find all active crfs in the definition
            Study study = (Study) studyDao.findByPK(studySubject.getStudyId());
            ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, sed.getId());
            ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

            // construct info needed on view study event page
            DisplayStudyEventBean de = new DisplayStudyEventBean();
            de.setStudyEvent(event);
            de.setDisplayEventCRFs(getDisplayEventCRFs(ds, eventCRFs, eventDefinitionCRFs, ub, currentRole, event.getSubjectEventStatus(), study));
            ArrayList al = getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getSubjectEventStatus());
            populateUncompletedCRFsWithCRFAndVersions(ds, al);
            de.setUncompletedCRFs(al);

            de.setMaximumSampleOrdinal(sedao.getMaxSampleOrdinal(sed, studySubject));

            Status status = de.getStudyEvent().getStatus();
            if (status == Status.AVAILABLE || status == Status.AUTO_DELETED)
                displayEvents.add(de);
            // event.setEventCRFs(createAllEventCRFs(eventCRFs,
            // eventDefinitionCRFs));

        }

        return displayEvents;
    }

    @Override
    public void processRequest() throws Exception {
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(sm.getDataSource());
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        EventCRFDAO eventCRFDAO = new EventCRFDAO(sm.getDataSource());
        ItemDataDAO itemDataDAO = new ItemDataDAO(sm.getDataSource());
        ItemDAO itemDAO = new ItemDAO(sm.getDataSource());
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
            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);

            request.setAttribute("studySub", studySub);
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
            DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(sm.getDataSource());
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

            SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);
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

            /*
             * StudyDAO studydao = new StudyDAO(sm.getDataSource()); Study
             * study = (Study) studydao.findByPK(studyId);
             */
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

            ArrayList children = (ArrayList) sdao.findAllChildrenByPK(subjectId);
            request.setAttribute("children", children);

            // find study events
            StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());

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
            Study parentStudyBean = currentStudy.getStudy();

                for (int i = 0; i < displayEvents.size(); i++) {
                    DisplayStudyEventBean decb = displayEvents.get(i);
                    StudyEventBean seBean = decb.getStudyEvent();
                    StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(seBean.getStudyEventDefinitionId());

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
                                   CRFBean crfBean = (CRFBean) crfdao.findByPK(formLayoutBean.getCrfId());

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
                    if (!(currentRole.isDirector() || currentRole.isCoordinator()) && decb.getStudyEvent().getSubjectEventStatus().isLocked()) {
                        decb.getStudyEvent().setEditable(false);
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
            table.computeDisplay();

            request.setAttribute("table", table);
            SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
            ArrayList groupMaps = (ArrayList) sgmdao.findAllByStudySubject(studySubId);
            request.setAttribute("groups", groupMaps);

            // find audit log for events
            AuditEventDAO aedao = new AuditEventDAO(sm.getDataSource(), getStudyDao());
            ArrayList logs = aedao.findEventStatusLogByStudySubject(studySubId);
            // logger.warning("^^^ retrieved logs");
            UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
            ArrayList eventLogs = new ArrayList();
            // logger.warning("^^^ starting to iterate");
            for (int i = 0; i < logs.size(); i++) {
                AuditEventBean avb = (AuditEventBean) logs.get(i);
                StudyEventAuditBean sea = new StudyEventAuditBean();
                sea.setAuditEvent(avb);
                StudyEventBean se = (StudyEventBean) sedao.findByPK(avb.getEntityId());
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
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
                UserAccountBean updater = (UserAccountBean) udao.findByPK(avb.getUserId());
                sea.setUpdater(updater);
                eventLogs.add(sea);
            }
            request.setAttribute("eventLogs", eventLogs);
            String errorData = request.getParameter("errorData");
            if (StringUtils.isNotEmpty(errorData))
                request.setAttribute("errorData", errorData);
            Study tempParentStudy = currentStudy.isSite() ? currentStudy.getStudy() : currentStudy;
            request.setAttribute("participateStatus", getParticipateStatus(tempParentStudy).toLowerCase());
            forwardPage(Page.VIEW_STUDY_SUBJECT);
        }
    }

    /**
     * Each of the event CRFs with its corresponding CRFBean. Then generates a
     * list of DisplayEventCRFBeans, one for each event CRF.
     *
     * @param eventCRFs
     *            The list of event CRFs for this study event.
     * @param eventDefinitionCRFs
     *            The list of event definition CRFs for this study event.
     * @return The list of DisplayEventCRFBeans for this study event.
     */
    public static ArrayList getDisplayEventCRFs(DataSource ds, ArrayList eventCRFs, ArrayList eventDefinitionCRFs, UserAccountBean ub,
            StudyUserRoleBean currentRole, SubjectEventStatus status, Study study) {
        ArrayList answer = new ArrayList();

        // HashMap definitionsById = new HashMap();
        int i;
        /*
         * for (i = 0; i < eventDefinitionCRFs.size(); i++) {
         * EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
         * eventDefinitionCRFs.get(i); definitionsById.put(new
         * Integer(edc.getStudyEventDefinitionId()), edc); }
         */

        StudyEventDAO sedao = new StudyEventDAO(ds);
        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        FormLayoutDAO fldao = new FormLayoutDAO(ds);
        ItemDataDAO iddao = new ItemDataDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);

        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            // populate the event CRF with its crf bean
            int crfVersionId = ecb.getCRFVersionId();
            int formLayoutId = ecb.getFormLayoutId();
            CRFBean cb = cdao.findByLayoutId(formLayoutId);
            ecb.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            ecb.setCrfVersion(cvb);
            FormLayoutBean flb = (FormLayoutBean) fldao.findByPK(formLayoutId);
            ecb.setFormLayout(flb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = ecb.getStudyEventId();
            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);

            // EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
            // definitionsById.get(new Integer(
            // studyEventDefinitionId));
            // fix problem of the above code(commented out), find the correct
            // edc, note that on definitionId can be related to multiple
            // eventdefinitioncrfBeans
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());
            // below added 092007 tbh
            // rules updated 112007 tbh
            if (status.equals(SubjectEventStatus.LOCKED) || status.equals(SubjectEventStatus.SKIPPED ) || status.equals(SubjectEventStatus.STOPPED )) {
                ecb.setStage(DataEntryStage.LOCKED);

                // we need to set a SED-wide flag here, because other edcs
                // in this event can be filled in and change the status, tbh
            } else if (status.equals(SubjectEventStatus.INVALID)) {
                ecb.setStage(DataEntryStage.LOCKED);
            } else if (!cb.getStatus().equals(Status.AVAILABLE)) {
                ecb.setStage(DataEntryStage.LOCKED);
            } else if (!cvb.getStatus().equals(Status.AVAILABLE)) {
                ecb.setStage(DataEntryStage.LOCKED);
            }
            // above added 092007-102007 tbh
            // TODO need to refactor since this is similar to other code, tbh
            if (edc != null) {
                // System.out.println("edc is not null, need to set flags");
                DisplayEventCRFBean dec = new DisplayEventCRFBean();
                dec.setEventDefinitionCRF(edc);
                // System.out.println("edc.isDoubleEntry()" +
                // edc.isDoubleEntry() + ecb.getId());
                dec.setFlags(ecb, ub, currentRole, edc.isDoubleEntry());

                if (dec.isLocked()) {
                    // System.out.println("*** found a locked DEC:
                    // "+edc.getCrfName());
                }
                ArrayList idata = iddao.findAllByEventCRFId(ecb.getId());
                if (!idata.isEmpty()) {
                    // consider an event crf started only if item data get
                    // created
                    answer.add(dec);
                }
            }
        }

        return answer;
    }

    /**
     * Finds all the event definitions for which no event CRF exists - which is
     * the list of event definitions with uncompleted event CRFs.
     *
     * @param eventDefinitionCRFs
     *            All of the event definition CRFs for this study event.
     * @param eventCRFs
     *            All of the event CRFs for this study event.
     * @return The list of event definitions for which no event CRF exists.
     */
    public static ArrayList getUncompletedCRFs(DataSource ds, ArrayList eventDefinitionCRFs, ArrayList eventCRFs, SubjectEventStatus status) {
        int i;
        HashMap completed = new HashMap();
        HashMap startedButIncompleted = new HashMap();
        ArrayList answer = new ArrayList();

        /**
         * A somewhat non-standard algorithm is used here: let answer = empty;
         * foreach event definition ED, set isCompleted(ED) = false foreach
         * event crf EC, set isCompleted(EC.getEventDefinition()) = true foreach
         * event definition ED, if (!isCompleted(ED)) { answer += ED; } return
         * answer; This algorithm is guaranteed to find all the event
         * definitions for which no event CRF exists.
         *
         * The motivation for using this algorithm is reducing the number of
         * database hits.
         *
         * -jun-we have to add more CRFs here: the event CRF which dones't have
         * item data yet
         */

        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            completed.put(new Integer(edcrf.getCrfId()), Boolean.FALSE);
            startedButIncompleted.put(new Integer(edcrf.getCrfId()), new EventCRFBean());
        }

        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        ItemDataDAO iddao = new ItemDataDAO(ds);
        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecrf = (EventCRFBean) eventCRFs.get(i);
            // System.out.println("########event crf id:" + ecrf.getId());
            int crfId = cvdao.getCRFIdFromCRFVersionId(ecrf.getCRFVersionId());
            ArrayList idata = iddao.findAllByEventCRFId(ecrf.getId());
            if (!idata.isEmpty()) {// this crf has data already
                completed.put(new Integer(crfId), Boolean.TRUE);
            } else {// event crf got created, but no data entered
                // System.out.println("added one into startedButIncompleted" + ecrf.getId());
                startedButIncompleted.put(new Integer(crfId), ecrf);
            }
        }

        // TODO possible relation to 1689 here, tbh
        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);

            dedc.setEdc(edcrf);
            // below added tbh, 112007 to fix bug 1943
            if (status.equals(SubjectEventStatus.LOCKED)) {
                dedc.setStatus(Status.LOCKED);
            }
            Boolean b = (Boolean) completed.get(new Integer(edcrf.getCrfId()));
            EventCRFBean ev = (EventCRFBean) startedButIncompleted.get(new Integer(edcrf.getCrfId()));
            if (b == null || !b.booleanValue()) {

                dedc.setEventCRF(ev);
                answer.add(dedc);

            }
        }
        return answer;
    }

    public static void populateUncompletedCRFsWithCRFAndVersions(DataSource ds, ArrayList uncompletedEventDefinitionCRFs) {
        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);

        int size = uncompletedEventDefinitionCRFs.size();
        for (int i = 0; i < size; i++) {
            DisplayEventDefinitionCRFBean dedcrf = (DisplayEventDefinitionCRFBean) uncompletedEventDefinitionCRFs.get(i);
            CRFBean cb = (CRFBean) cdao.findByPK(dedcrf.getEdc().getCrfId());
            dedcrf.getEdc().setCrf(cb);

            ArrayList theVersions = (ArrayList) cvdao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
            ArrayList versions = new ArrayList();
            HashMap<String, CRFVersionBean> crfVersionIds = new HashMap<String, CRFVersionBean>();

            for (int j = 0; j < theVersions.size(); j++) {
                CRFVersionBean crfVersion = (CRFVersionBean) theVersions.get(j);
                crfVersionIds.put(String.valueOf(crfVersion.getId()), crfVersion);
            }

            if (!dedcrf.getEdc().getSelectedVersionIds().equals("")) {
                String[] kk = dedcrf.getEdc().getSelectedVersionIds().split(",");
                for (String string : kk) {
                    if (crfVersionIds.get(string) != null) {
                        versions.add(crfVersionIds.get(string));
                    }
                }
            } else {
                versions = theVersions;
            }
            dedcrf.getEdc().setVersions(versions);
            uncompletedEventDefinitionCRFs.set(i, dedcrf);
        }
    }

    /*
     * //Returns an array list which contain all completed eventcrf and
     * uncompleted
     *
     * private ArrayList createAllEventCRFs(ArrayList eventCRFs, ArrayList
     * eventDefinitionCRFs) { CRFDAO cdao = new CRFDAO(sm.getDataSource());
     * CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource()); HashMap
     * crfIdMap = new HashMap(); ArrayList evs = new ArrayList(); for (int i =
     * 0; i < eventDefinitionCRFs.size(); i++) { EventDefinitionCRFBean edc =
     * (EventDefinitionCRFBean) eventDefinitionCRFs.get(i); crfIdMap.put(new
     * Integer(edc.getCrfId()), Boolean.FALSE); }
     *
     * for (int i = 0; i < eventCRFs.size(); i++) { EventCRFBean eventCRF =
     * (EventCRFBean) eventCRFs.get(i); logger.info("\nstage:" +
     * eventCRF.getStage().getName()); CRFBean crf =
     * cdao.findByVersionId(eventCRF.getCRFVersionId()); CRFVersionBean cVersion =
     * (CRFVersionBean) vdao.findByPK(eventCRF.getCRFVersionId());
     * eventCRF.setCrf(crf); eventCRF.setCrfVersion(cVersion);
     * evs.add(eventCRF);
     *
     * if (crfIdMap.containsKey(new Integer(crf.getId()))) { crfIdMap.put(new
     * Integer(crf.getId()), Boolean.TRUE);//already has // entry for this //
     * crf } }//for
     *
     * //find those crfs which are not started yet(stage=uncompleted) Set keys =
     * crfIdMap.keySet(); Iterator it = keys.iterator(); while (it.hasNext()) {
     * Integer crfId = (Integer) it.next(); if (crfIdMap.containsKey(crfId) &&
     * crfIdMap.get(crfId).equals(Boolean.FALSE)) { EventCRFBean ec = new
     * EventCRFBean(); CRFBean crf1 = (CRFBean) cdao.findByPK(crfId.intValue());
     * ArrayList versions = vdao.findAllByCRFId(crfId.intValue());
     * crf1.setVersions(versions); ec.setCrf(crf1);
     *
     * logger.info("\nstage:" + ec.getStage().getName()); evs.add(ec); }
     * }//while
     *
     * return evs; }
     *
     */
    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    /**
     * Current User may access a requested study subject in the current user's
     * studies
     *
     * @author ywang 10-18-2007
     */
    public void mayAccess() throws InsufficientPermissionException {
        FormProcessor fp = new FormProcessor(request);
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        int studySubId = fp.getInt("id", true);

        if (studySubId > 0) {
            if (!entityIncluded(studySubId, ub.getName(), subdao, sm.getDataSource())) {
                addPageMessage(respage.getString("required_study_subject_not_belong"));
                throw new InsufficientPermissionException(Page.MENU, resexception.getString("entity_not_belong_studies"), "1");
            }
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

    private void addDiscrepancyNotesFromChildStudies(List<DiscrepancyNoteBean> discBeans, int parentStudyId, int subjectId, int studySubId,
            DiscrepancyNoteDAO discrepancyNoteDAO) {

        if (discBeans == null || discBeans.isEmpty() || discrepancyNoteDAO == null) {
            return;
        }
        ArrayList<Study> childStudies = (ArrayList) getStudyDao().findAllByParent(parentStudyId);

        for (Study studyBean : childStudies) {
            discBeans.addAll(discrepancyNoteDAO.findAllSubjectByStudyAndId(studyBean, subjectId));
            discBeans.addAll(discrepancyNoteDAO.findAllStudySubjectByStudyAndId(studyBean, studySubId));
        }

    }








}

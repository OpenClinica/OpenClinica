package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.AuditEventBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.StudyEventAuditBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DisplayEventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateNewStudyEventServlet;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.service.Auth0UserService;
import org.akaza.openclinica.service.Auth0UserServiceImpl;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DisplayStudyEventRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Created by IntelliJ IDEA. User: bads Date: Jun 10, 2008 Time: 5:28:46 PM To
 * change this template use File | Settings | File Templates.
 */
public class SignStudySubjectServlet extends SecureController {
    private WebApplicationContext ctx = null;
    public static final String ORIGINATING_PAGE = "originatingPage";

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");
    }

    public static ArrayList getDisplayStudyEventsForStudySubject(StudyBean study, StudySubjectBean studySub, DataSource ds, UserAccountBean ub,
            StudyUserRoleBean currentRole) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);

        ArrayList events = sedao.findAllByStudySubject(studySub);

        ArrayList displayEvents = new ArrayList();
        for (int i = 0; i < events.size(); i++) {
            StudyEventBean event = (StudyEventBean) events.get(i);

            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            // find all active crfs in the definition
            ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, sed.getId());

            ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

            // construct info needed on view study event page
            DisplayStudyEventBean de = new DisplayStudyEventBean();
            de.setStudyEvent(event);
            de.setDisplayEventCRFs(getDisplayEventCRFs(study, ds, eventCRFs, ub, currentRole, event.getSubjectEventStatus()));
            ArrayList al = getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getSubjectEventStatus());
            populateUncompletedCRFsWithCRFAndVersions(ds, al);
            de.setUncompletedCRFs(al);

            StudySubjectBean studySubject = (StudySubjectBean) ssdao.findByPK(event.getStudySubjectId());
            de.setMaximumSampleOrdinal(sedao.getMaxSampleOrdinal(sed, studySubject));

            displayEvents.add(de);
            // event.setEventCRFs(createAllEventCRFs(eventCRFs,
            // eventDefinitionCRFs));

        }

        return displayEvents;
    }

    public static boolean permitSign(StudySubjectBean studySub, DataSource ds) {
        boolean sign = true;
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        StudyDAO sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByPK(studySub.getStudyId());
        // DiscrepancyNoteDAO discDao = new DiscrepancyNoteDAO(ds);
        ArrayList studyEvents = sedao.findAllByStudySubject(studySub);
        for (int l = 0; l < studyEvents.size(); l++) {
            StudyEventBean studyEvent = (StudyEventBean) studyEvents.get(l);
            ArrayList eventCrfs = ecdao.findAllByStudyEvent(studyEvent);
            for (int i = 0; i < eventCrfs.size(); i++) {
                EventCRFBean ecrf = (EventCRFBean) eventCrfs.get(i);
                // ArrayList discList =
                // discDao.findAllItemNotesByEventCRF(ecrf.getId());
                // for (int j = 0; j < discList.size(); j++) {
                // DiscrepancyNoteBean discBean = (DiscrepancyNoteBean)
                // discList.get(j);
                // if
                // (discBean.getResStatus().equals(org.akaza.openclinica.bean.core
                // .ResolutionStatus.OPEN)
                // ||
                // discBean.getResStatus().equals(org.akaza.openclinica.bean.core
                // .ResolutionStatus.UPDATED))
                // {
                // sign = false;
                // break;
                // }
                // }
                EventDefinitionCRFBean edcBean = edcdao.findByStudyEventIdAndCRFVersionId(studyBean, studyEvent.getId(), ecrf.getCRFVersionId());
                if (ecrf.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY)
                        || ecrf.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) && edcBean.isDoubleEntry() == true) {
                    sign = false;
                    break;
                }

            }
        }
        return sign;
    }

    public static boolean signSubjectEvents(StudySubjectBean studySub, DataSource ds, UserAccountBean ub) {
        boolean updated = true;
        // StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        // StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        DiscrepancyNoteDAO discDao = new DiscrepancyNoteDAO(ds);
        ArrayList studyEvents = sedao.findAllByStudySubject(studySub);
        for (int l = 0; l < studyEvents.size(); l++) {
            try {
                StudyEventBean studyEvent = (StudyEventBean) studyEvents.get(l);
                studyEvent.setUpdater(ub);
                Date date = new Date();
                studyEvent.setUpdatedDate(date);
                studyEvent.setSubjectEventStatus(SubjectEventStatus.SIGNED);
                studyEvent.setAttestation("The eCRFs that are part of this event were signed by " + ub.getFirstName() + " " + ub.getLastName() + " (" + ub.getName()
                        + ") " + "on Date Time " + date + " under the following attestation:\n\n" + resword.getString("sure_to_sign_subject3"));
                sedao.update(studyEvent);
            } catch (Exception ex) {
                updated = false;
            }
        }
        return updated;
    }

    @Override
    public void processRequest() throws Exception {
        ctx = WebApplicationContextUtils.getWebApplicationContext(context);
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        String action = fp.getString("action");
        int studySubId = fp.getInt("id", true);// studySubjectId

        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);
        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
            return;
        }
        CoreResources.setRequestSchema(request, currentPublicStudy.getSchemaName());
        StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
        request.setAttribute("studySub", studySub);

        if (!permitSign(studySub, sm.getDataSource())) {
            addPageMessage(respage.getString("subject_event_cannot_signed"));
            // forwardPage(Page.SUBMIT_DATA_SERVLET);
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            // >> changed tbh, 06/2009
            return;
        }

        if (action.equalsIgnoreCase("confirm")) {
            String username = request.getParameter("j_user");
            String password = request.getParameter("j_pass");
            // String encodedUserPass =
            // org.akaza.openclinica.core.SecurityManager
            // .getInstance().encrytPassword(password);
            UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
            Auth0UserService auth0UserService = ctx.getBean("auth0UserService", Auth0UserServiceImpl.class);
            boolean isAuthenticated = auth0UserService.authenticateAuth0User(username, password);
            if (isAuthenticated && ub.getName().equals(username)) {
                if (signSubjectEvents(studySub, sm.getDataSource(), ub)) {
                    // Making the StudySubject signed as all the events have
                    // become signed.
                    studySub.setStatus(Status.SIGNED);
                    studySub.setUpdater(ub);
                    subdao.update(studySub);
                    addPageMessage(respage.getString("subject_event_signed"));
                    // forwardPage(Page.SUBMIT_DATA_SERVLET);
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                    // >> changed tbh, 06/2009
                    return;
                } else {
                    addPageMessage(respage.getString("errors_in_submission_see_below"));
                    forwardPage(Page.LIST_STUDY_SUBJECTS);
                    return;
                }
            } else {

                int studyId = studySub.getStudyId();
                StudyDAO studydao = new StudyDAO(sm.getDataSource());
                StudyBean study = (StudyBean) studydao.findByPK(studyId);

                StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
                EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());

                // find all eventcrfs for each event
                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
                ArrayList<DisplayStudyEventBean> displayEvents = getDisplayStudyEventsForStudySubject(study, studySub, sm.getDataSource(), ub, currentRole);

                for (DisplayStudyEventBean displayEvent : displayEvents) {
                    StudyEventBean studyEvent = displayEvent.getStudyEvent();
                    ArrayList eventCRFs = ecdao.findAllByStudyEvent(displayEvent.getStudyEvent());
                    ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, studyEvent.getStudyEventDefinitionId());
                    ArrayList displayEventCRFs = ViewStudySubjectServlet.getDisplayEventCRFs(sm.getDataSource(), eventCRFs, eventDefinitionCRFs, ub,
                            currentRole, studyEvent.getSubjectEventStatus(), study);
                    displayEvent.setDisplayEventCRFs(displayEventCRFs);
                }

                DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();
                discNoteUtil.injectParentDiscNotesIntoDisplayStudyEvents(displayEvents, new HashSet(), sm.getDataSource(), 0);

                Map discNoteByEventCRFid = discNoteUtil.createDiscNoteMapByEventCRF(displayEvents);
                String originationUrl = "SignStudySubject?id=" + studySub.getId();

                request.setAttribute("discNoteByEventCRFid", discNoteByEventCRFid);
                request.setAttribute("displayStudyEvents", displayEvents);
                request.setAttribute(ORIGINATING_PAGE, originationUrl);
                request.setAttribute("id", new Integer(studySubId).toString());
                addPageMessage(restext.getString("password_match"));
                forwardPage(Page.SIGN_STUDY_SUBJECT);
                return;
            }
        }

        int studyId = studySub.getStudyId();
        int subjectId = studySub.getSubjectId();

        SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);
        if (currentStudy.getStudyParameterConfig().getCollectDob().equals("2")) {
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

        StudyDAO studydao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) studydao.findByPK(studyId);

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        study.getStudyParameterConfig().setCollectDob(spvdao.findByHandleAndStudy(studyId, "collectDob").getValue());
        // request.setAttribute("study", study);

        if (study.getParentStudyId() > 0) {// this is a site,find parent
            StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
            request.setAttribute("parentStudy", parentStudy);
        } else {
            request.setAttribute("parentStudy", new StudyBean());
        }

        ArrayList children = (ArrayList) sdao.findAllChildrenByPK(subjectId);

        request.setAttribute("children", children);

        // find study events
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());

        // find all eventcrfs for each event
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

        ArrayList<DisplayStudyEventBean> displayEvents = getDisplayStudyEventsForStudySubject(study, studySub, sm.getDataSource(), ub, currentRole);

        for (DisplayStudyEventBean displayEvent : displayEvents) {
            StudyEventBean studyEvent = displayEvent.getStudyEvent();
            ArrayList eventCRFs = ecdao.findAllByStudyEvent(displayEvent.getStudyEvent());
            ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, studyEvent.getStudyEventDefinitionId());
            ArrayList displayEventCRFs = ViewStudySubjectServlet.getDisplayEventCRFs(sm.getDataSource(), eventCRFs, eventDefinitionCRFs, ub, currentRole,
                    studyEvent.getSubjectEventStatus(), study);
            displayEvent.setDisplayEventCRFs(displayEventCRFs);
        }

        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();
        // Don't filter for now; disc note beans are returned with eventCRFId
        // set
        discNoteUtil.injectParentDiscNotesIntoDisplayStudyEvents(displayEvents, new HashSet(), sm.getDataSource(), 0);
        // All the displaystudyevents for one subject

        // Set up a Map for the JSP view, mapping the eventCRFId to another Map:
        // the
        // inner Map maps the resolution status name to the number of notes for
        // that
        // eventCRF id, as in New --> 2

        Map discNoteByEventCRFid = discNoteUtil.createDiscNoteMapByEventCRF(displayEvents);
        String originationUrl = "SignStudySubject?id=" + studySub.getId();

        request.setAttribute("discNoteByEventCRFid", discNoteByEventCRFid);
        request.setAttribute("displayStudyEvents", displayEvents);
        request.setAttribute(ORIGINATING_PAGE, originationUrl);

        EntityBeanTable table = fp.getEntityBeanTable();
        table.setSortingIfNotExplicitlySet(1, false);// sort by start date,
        // desc
        ArrayList allEventRows = DisplayStudyEventRow.generateRowsFromBeans(displayEvents);

        String[] columns = { resword.getString("event") + " (" + resword.getString("occurrence_number") + ")", resword.getString("start_date1"),
                resword.getString("location"), resword.getString("status"), resword.getString("actions"), resword.getString("CRFs_atrib") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(4);
        table.hideColumnLink(5);

        if (!"removed".equalsIgnoreCase(studySub.getStatus().getName()) && !"auto-removed".equalsIgnoreCase(studySub.getStatus().getName())) {
            table.addLink(resword.getString("add_new"),
                    "CreateNewStudyEvent?" + CreateNewStudyEventServlet.INPUT_STUDY_SUBJECT_ID_FROM_VIEWSUBJECT + "=" + studySub.getId());
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

        AuditEventDAO aedao = new AuditEventDAO(sm.getDataSource());
        ArrayList logs = aedao.findEventStatusLogByStudySubject(studySubId);

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList eventLogs = new ArrayList();

        for (int i = 0; i < logs.size(); i++) {
            AuditEventBean avb = (AuditEventBean) logs.get(i);
            StudyEventAuditBean sea = new StudyEventAuditBean();
            sea.setAuditEvent(avb);
            StudyEventBean se = (StudyEventBean) sedao.findByPK(avb.getEntityId());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
            sea.setDefinition(sed);
            String old = avb.getOldValue().trim();
            try {
                if (!StringUtil.isBlank(old)) {
                    SubjectEventStatus oldStatus = SubjectEventStatus.get(new Integer(old).intValue());
                    sea.setOldSubjectEventStatus(oldStatus);
                }
                String newValue = avb.getNewValue().trim();
                if (!StringUtil.isBlank(newValue)) {
                    SubjectEventStatus newStatus = SubjectEventStatus.get(new Integer(newValue).intValue());
                    sea.setNewSubjectEventStatus(newStatus);
                }
            } catch (NumberFormatException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
                // logger.warning("^^^ caught NFE");
            }
            UserAccountBean updater = (UserAccountBean) udao.findByPK(avb.getUserId());
            sea.setUpdater(updater);
            eventLogs.add(sea);

        }
        // logger.warning("^^^ finished iteration");
        request.setAttribute("eventLogs", eventLogs);

        forwardPage(Page.SIGN_STUDY_SUBJECT);

    }

    /**
     * Each of the event CRFs with its corresponding CRFBean. Then generates a
     * list of DisplayEventCRFBeans, one for each event CRF.
     *
     * @param eventCRFs
     *            The list of event CRFs for this study event.
     * @return The list of DisplayEventCRFBeans for this study event.
     */
    public static ArrayList getDisplayEventCRFs(StudyBean study, DataSource ds, ArrayList eventCRFs, UserAccountBean ub, StudyUserRoleBean currentRole,
            SubjectEventStatus status) {
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
        ItemDataDAO iddao = new ItemDataDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);

        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            // populate the event CRF with its crf bean
            int crfVersionId = ecb.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            ecb.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            ecb.setCrfVersion(cvb);

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
            if (status.equals(SubjectEventStatus.LOCKED) || status.equals(SubjectEventStatus.SKIPPED) || status.equals(SubjectEventStatus.STOPPED)) {
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
                // System.out.println("edc.isDoubleEntry()" +
                // edc.isDoubleEntry() + ecb.getId());
                dec.setFlags(ecb, ub, currentRole, edc.isDoubleEntry());
                // if (dec.isLocked()) {
                // System.out.println("*** found a locked DEC: " + edc.getCrfName());
                // }
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
            int crfId = cvdao.getCRFIdFromCRFVersionId(ecrf.getCRFVersionId());
            ArrayList idata = iddao.findAllByEventCRFId(ecrf.getId());
            if (!idata.isEmpty()) {// this crf has data already
                completed.put(new Integer(crfId), Boolean.TRUE);
            } else {// event crf got created, but no data entered
                startedButIncompleted.put(new Integer(crfId), ecrf);
            }
        }

        // TODO possible relation to 1689 here, tbh
        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);

            // System.out.println("created dedc with edcrf
            // "+edcrf.getCrfName()+" default version "+
            // edcrf.getDefaultVersionName()+", id
            // "+edcrf.getDefaultVersionId());

            dedc.setEdc(edcrf);
            // below added tbh, 112007 to fix bug 1943
            if (status.equals(SubjectEventStatus.LOCKED)) {
                dedc.setStatus(Status.LOCKED);
            }
            Boolean b = (Boolean) completed.get(new Integer(edcrf.getCrfId()));
            EventCRFBean ev = (EventCRFBean) startedButIncompleted.get(new Integer(edcrf.getCrfId()));
            if (b == null || !b.booleanValue()) {

                // System.out.println("entered boolean loop with ev
                // "+ev.getId()+" crf version id "+
                // ev.getCRFVersionId());

                dedc.setEventCRF(ev);
                answer.add(dedc);

                // System.out.println("just added dedc to answer");
                // removed, tbh, since this is proving nothing, 11-2007

                /*
                 * if (dedc.getEdc().getDefaultVersionId() !=
                 * dedc.getEventCRF().getId()) { System.out.println("ID
                 * MISMATCH: edc name "+dedc.getEdc().getName()+ ", default
                 * version id "+dedc.getEdc().getDefaultVersionId()+ " event crf
                 * id "+dedc.getEventCRF().getId()); }
                 */
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

            ArrayList versions = (ArrayList) cvdao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
            dedcrf.getEdc().setVersions(versions);
            uncompletedEventDefinitionCRFs.set(i, dedcrf);
        }
    }

    /*
     * //Returns an array list which contain all completed eventcrf and
     * uncompleted private ArrayList createAllEventCRFs(ArrayList eventCRFs,
     * ArrayList eventDefinitionCRFs) { CRFDAO cdao = new
     * CRFDAO(sm.getDataSource()); CRFVersionDAO vdao = new
     * CRFVersionDAO(sm.getDataSource()); HashMap crfIdMap = new HashMap();
     * ArrayList evs = new ArrayList(); for (int i = 0; i <
     * eventDefinitionCRFs.size(); i++) { EventDefinitionCRFBean edc =
     * (EventDefinitionCRFBean) eventDefinitionCRFs.get(i); crfIdMap.put(new
     * Integer(edc.getCrfId()), Boolean.FALSE); } for (int i = 0; i <
     * eventCRFs.size(); i++) { EventCRFBean eventCRF = (EventCRFBean)
     * eventCRFs.get(i); logger.info("\nstage:" +
     * eventCRF.getStage().getName()); CRFBean crf =
     * cdao.findByVersionId(eventCRF.getCRFVersionId()); CRFVersionBean cVersion
     * = (CRFVersionBean) vdao.findByPK(eventCRF.getCRFVersionId());
     * eventCRF.setCrf(crf); eventCRF.setCrfVersion(cVersion);
     * evs.add(eventCRF); if (crfIdMap.containsKey(new Integer(crf.getId()))) {
     * crfIdMap.put(new Integer(crf.getId()), Boolean.TRUE);//already has //
     * entry for this // crf } }//for //find those crfs which are not started
     * yet(stage=uncompleted) Set keys = crfIdMap.keySet(); Iterator it =
     * keys.iterator(); while (it.hasNext()) { Integer crfId = (Integer)
     * it.next(); if (crfIdMap.containsKey(crfId) &&
     * crfIdMap.get(crfId).equals(Boolean.FALSE)) { EventCRFBean ec = new
     * EventCRFBean(); CRFBean crf1 = (CRFBean) cdao.findByPK(crfId.intValue());
     * ArrayList versions = vdao.findAllByCRFId(crfId.intValue());
     * crf1.setVersions(versions); ec.setCrf(crf1); logger.info("\nstage:" +
     * ec.getStage().getName()); evs.add(ec); } }//while return evs; }
     */
    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

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

}

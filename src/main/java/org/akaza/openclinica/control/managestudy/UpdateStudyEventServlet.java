/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.hibernate.RuleSetDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.rule.RuleSetDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.AuditLogEventService;
import core.org.akaza.openclinica.service.DiscrepancyNoteUtil;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
import core.org.akaza.openclinica.service.rule.RuleSetService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jxu
 *
 *         Performs updating study event action
 */
public class UpdateStudyEventServlet extends SecureController {

    public static final String EVENT_ID = "event_id";
    public static final String STUDY_SUBJECT_ID = "ss_id";
    public static final String EVENT_BEAN = "studyEvent";
    public static final String EVENT_DEFINITION_BEAN = "eventDefinition";
    public static final String FIRST_SIGN = "first_sign";

    public static final String EVENT_WORKFLOW_STATUS = "statusId";
    public static final String LOCKED = "Locked";
    public static final String UNLOCKED = "UnLocked";

    public static final String INPUT_STARTDATE_PREFIX = "start";
    public static final String INPUT_ENDDATE_PREFIX = "end";
    public static final String INPUT_LOCATION = "location";
    public final static String HAS_LOCATION_NOTE = "hasLocationNote";
    public final static String LOCATION_NOTE = "locationNote";
    public final static String HAS_START_DATE_NOTE = "hasStartDateNote";
    public final static String START_DATE_NOTE = "startDateNote";
    public final static String HAS_END_DATE_NOTE = "hasEndDateNote";
    public final static String END_DATE_NOTE = "endDateNote";
    private WebApplicationContext ctx = null;
    public static final String ORIGINATING_PAGE = "originatingPage";
    public static final String STUDY_EVENT = "study_event";
    public static final String PREV_STUDY_EVENT_SIGNED_STATUS = "prev_study_event_workflow_status";
    public static final String NEW_STATUS = "newStatus";

    private StudyEventDAO studyEventDAO;
    private StudySubjectDao studySubjectDao;
    private EventCRFDAO eventCRFDAO;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        if (SubmitDataServlet.maySubmitData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        ctx = WebApplicationContextUtils.getWebApplicationContext(context);

        studyEventDAO = (StudyEventDAO) SpringServletAccess.getApplicationContext(context).getBean("studyEventJDBCDao");
        studySubjectDao = (StudySubjectDao) SpringServletAccess.getApplicationContext(context).getBean("studySubjectDaoDomain");
        eventCRFDAO = (EventCRFDAO) SpringServletAccess.getApplicationContext(context).getBean("eventCRFJDBCDao");
        studyEventDefinitionDAO = (StudyEventDefinitionDAO) SpringServletAccess.getApplicationContext(context).getBean("studyEventDefinitionJDBCDao");

        FormDiscrepancyNotes discNotes = null;
        FormProcessor fp = new FormProcessor(request);
        int studyEventId = fp.getInt(EVENT_ID, true);
        int studySubjectId = fp.getInt(STUDY_SUBJECT_ID, true);

        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        String fromResolvingNotes = fp.getString("fromResolvingNotes", true);
        if (StringUtil.isBlank(fromResolvingNotes)) {
            session.removeAttribute(ViewNotesServlet.WIN_LOCATION);
            session.removeAttribute(ViewNotesServlet.NOTES_TABLE);
            checkStudyLocked(Page.MANAGE_STUDY, respage.getString("current_study_locked"));
            checkStudyFrozen(Page.MANAGE_STUDY, respage.getString("current_study_frozen"));
        }

        if (studyEventId == 0 || studySubjectId == 0) {
            addPageMessage(respage.getString("choose_a_study_event_to_edit"));
            request.setAttribute("id", new Integer(studySubjectId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            return;
        }

        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean ssub = null;
        if (studySubjectId > 0) {
            ssub = (StudySubjectBean) ssdao.findByPK(studySubjectId);
            request.setAttribute("studySubject", ssub);
            request.setAttribute("id", studySubjectId + "");// for the workflow
            // box, so it can
            // link back to view
            // study subject
        }
        // YW 11-07-2007, a study event could not be updated if its study
        // subject has been removed
        // Status s = ((StudySubjectBean)new
        // StudySubjectDAO(sm.getDataSource()).findByPK(studySubjectId)).getStatus();
        Status s = ssub.getStatus();
        if ("removed".equalsIgnoreCase(s.getName()) || "auto-removed".equalsIgnoreCase(s.getName())) {
            addPageMessage(resword.getString("study_event") + resterm.getString("could_not_be") + resterm.getString("updated") + "."
                    + respage.getString("study_subject_has_been_deleted"));
            request.setAttribute("id", new Integer(studySubjectId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        }
        // YW

        request.setAttribute(STUDY_SUBJECT_ID, new Integer(studySubjectId).toString());

        StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByPK(studyEventId);

        studyEvent.setStudyEventDefinition((StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(studyEvent.getStudyEventDefinitionId()));

        studyEvent.setEventCRFs(eventCRFDAO.findAllByStudyEvent(studyEvent));

        List<StudyEventWorkflowStatusEnum> eventWorkflowStatuses = new ArrayList<>();

        Study studyBean = (Study) getStudyDao().findByPK(ssub.getStudyId());
        checkRoleByUserAndStudy(ub, studyBean);
        // To remove signed status from the list
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        // DiscrepancyNoteDAO discDao = new
        // DiscrepancyNoteDAO(sm.getDataSource());
        ArrayList eventCrfs = studyEvent.getEventCRFs();

        // ///End of remove signed status from the list

        // BWP: 2735>>keep the DATA_ENTRY_STARTED status
        if(studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED)){
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.NOT_SCHEDULED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.SCHEDULED);
        }
        else if(studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)){
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.SCHEDULED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.SKIPPED);
        }
        else if(studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SKIPPED)){
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.SKIPPED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.SCHEDULED);
        }
        else if(studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED)){
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.COMPLETED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.STOPPED);
        }
        else if(studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.STOPPED)){
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.STOPPED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED);
        }
        else if(studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)){
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.COMPLETED);
            eventWorkflowStatuses.add(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED);
        }



        ArrayList getECRFs = studyEvent.getEventCRFs();
        // above removed tbh 102007, require to get all definitions, no matter
        // if they are filled in or now
        EventDefinitionCRFDAO edefcrfdao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList getAllECRFs = (ArrayList) edefcrfdao.findAllByDefinition(studyBean, studyEvent.getStudyEventDefinitionId());
        // does the study event have all complete CRFs which are required?
        logger.debug("found number of ecrfs: " + getAllECRFs.size());
        // may not be populated, only entered crfs seem to ping the list
        for (int u = 0; u < getAllECRFs.size(); u++) {
            EventDefinitionCRFBean ecrfBean = (EventDefinitionCRFBean) getAllECRFs.get(u);

            //
            logger.debug("found number of existing ecrfs: " + getECRFs.size());
            if (getECRFs.size() == 0) {
                eventWorkflowStatuses.remove(StudyEventWorkflowStatusEnum.COMPLETED);

            } // otherwise...
            for (int uv = 0; uv < getECRFs.size(); uv++) {
                EventCRFBean existingBean = (EventCRFBean) getECRFs.get(uv);
                logger.debug("***** found: " + existingBean.getCRFVersionId() + " " + existingBean.getCrf().getId() + " "
                        + existingBean.getCrfVersion().getName() + " " + existingBean.getWorkflowStatus() + " " + existingBean.getStage().getName());

                logger.debug("***** comparing above to ecrfBean.DefaultVersionID: " + ecrfBean.getDefaultVersionId());

                // if (existingBean.getCRFVersionId() ==
                // ecrfBean.getDefaultVersionId()) {
                // OK. this only works if we go ahead and remove the drop down
                // will this match up? Do we need to pull it out of
                // studyEvent.getEventCRFs()?
                // only case that this will screw up is if there are no crfs
                // whatsoever
                // this is addressed in the if-clause above
                if (!existingBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED) && edefcrfdao.isRequiredInDefinition(existingBean.getCRFVersionId(), studyEvent, getStudyDao())) {

                    logger.debug("found that " + existingBean.getCrfVersion().getName() + " is required...");
                    // that is, it's not completed but required to complete
                    eventWorkflowStatuses.remove(StudyEventWorkflowStatusEnum.COMPLETED);
                    // per new rule above 11-16-2007
                }
                // }
            }
        }

        // ### tbh, above modified 102007
        request.setAttribute("eventWorkflowStatuses", eventWorkflowStatuses);

        String action = fp.getString("action");
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId());
        request.setAttribute(EVENT_DEFINITION_BEAN, sed);

        String start_date = fp.getDateTimeInputString(INPUT_STARTDATE_PREFIX);
        String end_date = fp.getDateTimeInputString(INPUT_ENDDATE_PREFIX);
        SimpleDateFormat dteFormat = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_format_string"));
        Date start = null;
        Date end = null;
        if (!StringUtils.isEmpty(start_date)) {
            start = fp.getDateTime(INPUT_STARTDATE_PREFIX);
            start_date = dteFormat.format(start);
        }
        if (!StringUtils.isEmpty(end_date)) {
            end = fp.getDateTime(INPUT_ENDDATE_PREFIX);
            end_date = dteFormat.format(end);
        }

        ssdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(studyEvent.getStudySubjectId());

        Study study = (Study) getStudyDao().findByPK(ssb.getStudyId());
        StudySubjectService studySubjectService = (StudySubjectService) WebApplicationContextUtils.getWebApplicationContext(getServletContext())
                .getBean("studySubjectService");

        if (action.equalsIgnoreCase("submit")) {
            discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
            DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);
            StudySubject studySubject = studySubjectDao.findByPK(studyEvent.getStudySubjectId());
            boolean isStudySubjectUpdated = false;
            StudyEventWorkflowStatusEnum ses = StudyEventWorkflowStatusEnum.valueOf( fp.getString(EVENT_WORKFLOW_STATUS));
            if(ses != null && !studyEvent.getWorkflowStatus().equals(ses)){
                if(studyEvent.isSigned())
                    studyEvent.setSigned(false);
                if(studySubject.getStatus().isSigned()) {
                    studySubject.setStatus(core.org.akaza.openclinica.domain.Status.AVAILABLE);
                    isStudySubjectUpdated = true;
                }
            }
            studyEvent.setWorkflowStatus(ses);
            session.setAttribute(PREV_STUDY_EVENT_SIGNED_STATUS, studyEvent.getSigned());

            ArrayList<EventCRFBean> eventCRFs = eventCRFDAO.findAllByStudyEvent(studyEvent);
            String newStatus = fp.getString(NEW_STATUS);

            if (newStatus != null && newStatus.equals(LOCKED)) {
                studyEvent.setLocked(true);
            }else if (newStatus != null && newStatus.equals(UNLOCKED)) {
                studyEvent.setLocked(false);
                }

            // YW 3-12-2008, 2220 fix
            String strEnd = fp.getDateTimeInputString(INPUT_ENDDATE_PREFIX);
            String strEndScheduled = fp.getDateTimeInputString(INPUT_ENDDATE_PREFIX);
            String strStartScheduled = fp.getDateTimeInputString(INPUT_STARTDATE_PREFIX);

            if (!strStartScheduled.equals("")) {
                v.addValidation(INPUT_STARTDATE_PREFIX, Validator.IS_DATE_TIME);
                v.alwaysExecuteLastValidation(INPUT_STARTDATE_PREFIX);
            }
            if (!strEndScheduled.equals("")) {
                v.addValidation(INPUT_ENDDATE_PREFIX, Validator.IS_DATE_TIME);
                v.alwaysExecuteLastValidation(INPUT_ENDDATE_PREFIX);
            }
            // v.addValidation(INPUT_LOCATION, Validator.NO_BLANKS); Disable validation on location, location can be
            // empty when updating a study event
            HashMap errors = v.validate();
            // YW, 3-12-2008, 2220 fix <<
            if (!strEnd.equals("") && !errors.containsKey(INPUT_STARTDATE_PREFIX) && !errors.containsKey(INPUT_ENDDATE_PREFIX)) {
                end = fp.getDateTime(INPUT_ENDDATE_PREFIX);
                if (!fp.getString(INPUT_STARTDATE_PREFIX + "Date").equals(fp.getString(INPUT_ENDDATE_PREFIX + "Date"))) {
                    if (end.before(start)) {
                        v.addError(errors, INPUT_ENDDATE_PREFIX, resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                    }
                } else {
                    // if in same date, only check when both had time entered
                    if (fp.timeEntered(INPUT_STARTDATE_PREFIX) && fp.timeEntered(INPUT_ENDDATE_PREFIX)) {
                        if (end.before(start) || end.equals(start)) {
                            v.addError(errors, INPUT_ENDDATE_PREFIX, resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                        }
                    }
                }
            }
            // YW >>

            if (!errors.isEmpty()) {
                setInputMessages(errors);
                String prefixes[] = { INPUT_STARTDATE_PREFIX, INPUT_ENDDATE_PREFIX };
                fp.setCurrentDateTimeValuesAsPreset(prefixes);
                setPresetValues(fp.getPresetValues());

                studyEvent.setLocation(fp.getString(INPUT_LOCATION));

                request.setAttribute("changeDate", fp.getString("changeDate"));
                request.setAttribute(EVENT_BEAN, studyEvent);
                forwardPage(Page.UPDATE_STUDY_EVENT);


            } else {
                logger.debug("no validation error");
                // YW 08-17-2007 << update start_time_flag column
                if (fp.getString(INPUT_STARTDATE_PREFIX + "Hour").equals("-1") && fp.getString(INPUT_STARTDATE_PREFIX + "Minute").equals("-1")
                        && fp.getString(INPUT_STARTDATE_PREFIX + "Half").equals("")) {
                    studyEvent.setStartTimeFlag(false);
                } else {
                    studyEvent.setStartTimeFlag(true);
                }
                // YW >>
                if(start != null)
                    studyEvent.setDateStarted(start);
                // YW, 3-12-2008, 2220 fix which adding End datetime <<
                if (!strEnd.equals("")) {
                    studyEvent.setDateEnded(end);
                    if (fp.getString(INPUT_ENDDATE_PREFIX + "Hour").equals("-1") && fp.getString(INPUT_ENDDATE_PREFIX + "Minute").equals("-1")
                            && fp.getString(INPUT_ENDDATE_PREFIX + "Half").equals("")) {
                        studyEvent.setEndTimeFlag(false);
                    } else {
                        studyEvent.setEndTimeFlag(true);
                    }
                }
                // YW >>
                studyEvent.setLocation(fp.getString(INPUT_LOCATION));

                logger.debug("update study event and discrepancy notes...");
                studyEvent.setUpdater(ub);
                studyEvent.setUpdatedDate(new Date());
                updateClosedQueriesForUpdatedStudySubjectFields(studyEvent);
                StudyEventBean updatedStudyEvent = (StudyEventBean) studyEventDAO.update(studyEvent);

                if(isStudySubjectUpdated) {
                    studySubject.setDateUpdated(new Date());
                    studySubject.setUpdateId(ub.getId());
                    studySubject = studySubjectDao.saveOrUpdate(studySubject);
                }
                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());

                AddNewSubjectServlet.saveFieldNotes(INPUT_LOCATION, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(INPUT_STARTDATE_PREFIX, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(INPUT_ENDDATE_PREFIX, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);

                // getRuleSetService().runRulesInBeanProperty(createRuleSet(ssub,sed),currentStudy,ub,request,ssub);

                addPageMessage(respage.getString("study_event_updated"));
                request.setAttribute("id", new Integer(studySubjectId).toString());
                session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                // FORWARD SHOULD BE TO THE NEW PAGE
            }
        } else if (action.equalsIgnoreCase("confirm")) {// confirming the signed
            // status
            String username = request.getParameter("j_user");
            String password = request.getParameter("j_pass");
            if (username == null)
                username = "";
            if (password == null)
                password = "";

            // tring encodedUserPass =
            // core.org.akaza.openclinica.core.SecurityManager.getInstance().encrytPassword(password);
            UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
            boolean isAuthenticated = false;
            KeycloakClientImpl keycloakClient = ctx.getBean("keycloakClientImpl", KeycloakClientImpl.class);
            try {
                keycloakClient.getAccessToken(username, password);
                isAuthenticated = true;
            } catch (Exception e) {
                logger.error("Failed to fetch access token", e);
            }

            if (isAuthenticated && ub.getName().equalsIgnoreCase(username)) {
                Date date = new Date();
                String detail = "The eCRFs that are part of this event were signed by " + ub.getFirstName() + " " + ub.getLastName() + " (" + ub.getName()
                        + ") " + "on Date Time " + date + " under the following attestation:\n\n" + resword.getString("sure_to_sign_subject3");
                studyEvent.setUpdater(ub);
                studyEvent.setUpdatedDate(date);
                studyEvent.setAttestation(detail);
                studyEvent.setSigned(true);
                studyEventDAO.update(studyEvent);

                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());

                AddNewSubjectServlet.saveFieldNotes(INPUT_LOCATION, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(INPUT_STARTDATE_PREFIX, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(INPUT_ENDDATE_PREFIX, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);

                request.setAttribute("id", new Integer(studySubjectId).toString());
                addPageMessage(respage.getString("study_event_updated"));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            } else {
                request.setAttribute(STUDY_SUBJECT_ID, new Integer(studySubjectId).toString());

                request.setAttribute("studyEvent", studyEvent);
                // -------------------

                // prepare to figure out what the display should look like
                ArrayList<EventCRFBean> eventCRFs = eventCRFDAO.findAllByStudyEvent(studyEvent);
                ArrayList<Boolean> doRuleSetsExist = new ArrayList<Boolean>();
                RuleSetDAO ruleSetDao = new RuleSetDAO(sm.getDataSource());
                ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, studyEvent.getStudyEventDefinitionId());

                DisplayStudyEventBean dse = null;
                for (DisplayStudyEventBean dsevent : studySubjectService.getDisplayStudyEventsForStudySubject(ssb, ub, currentRole, study)) {
                    if (dsevent.getStudyEvent().getId() == studyEventId) {
                        dse = dsevent;
                    }
                }
                ArrayList uncompletedEventDefinitionCRFs = dse.getUncompletedCRFs();

                ArrayList<DisplayEventCRFBean> displayEventCRFs = getDisplayEventCRFs(sm.getDataSource(), eventCRFs,
                        eventDefinitionCRFs, ub, currentRole, studyEvent.getWorkflowStatus(), study);

                DiscrepancyNoteUtil discNoteUtil = (DiscrepancyNoteUtil) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("discrepancyNoteUtil");
                DisplayStudyEventBean displayEvBean = new DisplayStudyEventBean();
                List<DisplayStudyEventBean> displayEvents = new ArrayList<DisplayStudyEventBean>();
                displayEvBean.setDisplayEventCRFs(displayEventCRFs);
                displayEvBean.setStudyEvent(studyEvent);
                displayEvents.add(displayEvBean);
                discNoteUtil.injectParentDiscNotesIntoDisplayStudyEvents(displayEvents, new HashSet(), sm.getDataSource(), 0);
                Map discNoteByEventCRFid = discNoteUtil.createDiscNoteMapByEventCRF(displayEvents);
                request.setAttribute("discNoteByEventCRFid", discNoteByEventCRFid);
                request.setAttribute("studySubject", ssb);
                request.setAttribute("uncompletedEventDefinitionCRFs", uncompletedEventDefinitionCRFs);
                request.setAttribute("displayEventCRFs", displayEventCRFs);

                String isFirstSign = fp.getString(FIRST_SIGN);
                // ------------------
                if (!isFirstSign.equals("true"))
                    addPageMessage(restext.getString("password_match"));

                String originationUrl = "UpdateStudyEvent?action=" + action + "%26event_id=" + studyEventId + "%26ss_id=" + studySubjectId + "%26startDate="
                        + start_date + "%26startHour=" + fp.getString(INPUT_STARTDATE_PREFIX + "Hour") + "%26startMinute="
                        + fp.getString(INPUT_STARTDATE_PREFIX + "Minute") + "%26startHalf=" + fp.getString(INPUT_STARTDATE_PREFIX + "Half") + "%26endDate="
                        + end_date + "%26endHour=" + fp.getString(INPUT_ENDDATE_PREFIX + "Hour") + "%26endMinute="
                        + fp.getString(INPUT_ENDDATE_PREFIX + "Minute") + "%26endHalf=" + fp.getString(INPUT_ENDDATE_PREFIX + "Half") + "%26statusId="
                        + studyEvent.getWorkflowStatus();

                request.setAttribute(ORIGINATING_PAGE, originationUrl);
                forwardPage(Page.UPDATE_STUDY_EVENT_SIGNED);
            }
        } else {
            logger.debug("no action, go to update page");

            DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(sm.getDataSource());
            StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByPK(studyEvent.getStudySubjectId());
            int studyId = studySubjectBean.getStudyId();
            boolean subjectStudyIsCurrentStudy = studyId == currentStudy.getStudyId();
            boolean isParentStudy = !studyBean.isSite();

            ArrayList<DiscrepancyNoteBean> allNotesforSubjectAndEvent = new ArrayList<DiscrepancyNoteBean>();
            allNotesforSubjectAndEvent = discrepancyNoteDAO.findExistingNoteForStudyEvent(studyEvent);

            if (!allNotesforSubjectAndEvent.isEmpty()) {
                setRequestAttributesForNotes(allNotesforSubjectAndEvent);
            }

            HashMap presetValues = new HashMap();
            // YW 08-17-2007 <<
            if (studyEvent.getDateStarted() != null) {
                if (studyEvent.getStartTimeFlag() == true) {
                    Calendar c = new GregorianCalendar();
                    c.setTime(studyEvent.getDateStarted());
                    presetValues.put(INPUT_STARTDATE_PREFIX + "Hour", new Integer(c.get(Calendar.HOUR_OF_DAY)));
                    presetValues.put(INPUT_STARTDATE_PREFIX + "Minute", new Integer(c.get(Calendar.MINUTE)));
                    // Later it could be put to somewhere as a static method if
                    // necessary.
                    switch (c.get(Calendar.AM_PM)) {
                    case 0:
                        presetValues.put(INPUT_STARTDATE_PREFIX + "Half", "am");
                        break;
                    case 1:
                        presetValues.put(INPUT_STARTDATE_PREFIX + "Half", "pm");
                        break;
                    default:
                        presetValues.put(INPUT_STARTDATE_PREFIX + "Half", "");
                        break;
                    }
                } else {
                    presetValues.put(INPUT_STARTDATE_PREFIX + "Hour", new Integer(-1));
                    presetValues.put(INPUT_STARTDATE_PREFIX + "Minute", new Integer(-1));
                    presetValues.put(INPUT_STARTDATE_PREFIX + "Half", "");
                }

                // YW >>

                String dateValue = local_df.format(studyEvent.getDateStarted());
                presetValues.put(INPUT_STARTDATE_PREFIX + "Date", dateValue);

                // YW 3-12-2008, add end datetime for 2220 fix<<
                presetValues.put(INPUT_ENDDATE_PREFIX + "Hour", new Integer(-1));
                presetValues.put(INPUT_ENDDATE_PREFIX + "Minute", new Integer(-1));
                presetValues.put(INPUT_ENDDATE_PREFIX + "Half", "");
            }
            if (studyEvent.getDateEnded() != null) {
                if (studyEvent.getEndTimeFlag() == true) {
                    Calendar c = new GregorianCalendar();
                    c.setTime(studyEvent.getDateEnded());
                    presetValues.put(INPUT_ENDDATE_PREFIX + "Hour", new Integer(c.get(Calendar.HOUR_OF_DAY)));
                    presetValues.put(INPUT_ENDDATE_PREFIX + "Minute", new Integer(c.get(Calendar.MINUTE)));
                    // Later it could be put to somewhere as a static method if
                    // necessary.
                    switch (c.get(Calendar.AM_PM)) {
                    case 0:
                        presetValues.put(INPUT_ENDDATE_PREFIX + "Half", "am");
                        break;
                    case 1:
                        presetValues.put(INPUT_ENDDATE_PREFIX + "Half", "pm");
                        break;
                    default:
                        presetValues.put(INPUT_ENDDATE_PREFIX + "Half", "");
                        break;
                    }
                }
                presetValues.put(INPUT_ENDDATE_PREFIX + "Date", local_df.format(studyEvent.getDateEnded()));
            }
            // YW >>

            setPresetValues(presetValues);

            DisplayStudyEventBean dse = null;
            for (DisplayStudyEventBean dsevent : studySubjectService.getDisplayStudyEventsForStudySubject(ssb, ub, currentRole, study)) {
                if (dsevent.getStudyEvent().getId() == studyEventId) {
                    dse = dsevent;
                }
            }

            request.setAttribute("studyEvent", studyEvent);
            request.setAttribute("studySubject", studySubjectBean);
            request.setAttribute("dse", dse);

            discNotes = new FormDiscrepancyNotes();
            session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

            forwardPage(Page.UPDATE_STUDY_EVENT);
        } // else

    }

    public ArrayList getDisplayEventCRFs(DataSource ds, ArrayList eventCRFs, ArrayList eventDefinitionCRFs, UserAccountBean ub,
                                         StudyUserRoleBean currentRole, StudyEventWorkflowStatusEnum workflowStatus, Study study) {
        ArrayList answer = new ArrayList();

        // HashMap definitionsById = new HashMap();
        int i;
        /*
         * for (i = 0; i < eventDefinitionCRFs.size(); i++) {
         * EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
         * eventDefinitionCRFs.get(i); definitionsById.put(new
         * Integer(edc.getStudyEventDefinitionId()), edc); }
         */

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
            int studyEventDefinitionId = studyEventDAO.getDefinitionIdFromStudyEventId(studyEventId);

            // EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
            // definitionsById.get(new Integer(
            // studyEventDefinitionId));
            // fix problem of the above code(commented out), find the correct
            // edc, note that on definitionId can be related to multiple
            // eventdefinitioncrfBeans
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());
            // below added 092007 tbh

            // above added 092007-102007 tbh
            // TODO need to refactor since this is similar to other code, tbh
            if (edc != null) {
                ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) fldao.findAllActiveByCRF(edc.getCrfId());
                edc.setVersions(versions);

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

    private void updateClosedQueriesForUpdatedStudySubjectFields(StudyEventBean updatedStudyEvent) {
        StudyEventBean existingStudyEvent = (StudyEventBean) studyEventDAO.findByPK(updatedStudyEvent.getId());
        DiscrepancyNoteDAO dnDAO = new DiscrepancyNoteDAO(sm.getDataSource());
        List<DiscrepancyNoteBean> existingNotes = dnDAO.findExistingNoteForStudyEvent(existingStudyEvent);

        for (DiscrepancyNoteBean existingNote : existingNotes) {
            if (existingNote.getColumn().equals("start_date") && existingNote.getResStatus().equals(ResolutionStatus.CLOSED)
                    && existingStudyEvent.getDateStarted().getTime() != updatedStudyEvent.getDateStarted().getTime()) {
                existingNote.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());
                DiscrepancyNoteBean childNote = createChildNote(existingNote);
                dnDAO.create(childNote);
                dnDAO.createMapping(childNote);
                dnDAO.update(existingNote);
            }

            if (existingNote.getColumn().equals("end_date") && existingNote.getResStatus().equals(ResolutionStatus.CLOSED)
                    && existingStudyEvent.getDateEnded().getTime() != updatedStudyEvent.getDateEnded().getTime()) {
                existingNote.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());
                DiscrepancyNoteBean childNote = createChildNote(existingNote);
                dnDAO.create(childNote);
                dnDAO.createMapping(childNote);
                dnDAO.update(existingNote);
            }
        }
    }

    private DiscrepancyNoteBean createChildNote(DiscrepancyNoteBean parent) {
        DiscrepancyNoteBean child = new DiscrepancyNoteBean();
        child.setParentDnId(parent.getId());
        child.setDiscrepancyNoteTypeId(parent.getDiscrepancyNoteTypeId());
        child.setDetailedNotes(resword.getString("closed_modified_message"));
        child.setResolutionStatusId(parent.getResolutionStatusId());
        child.setAssignedUserId(parent.getAssignedUserId());
        child.setResStatus(parent.getResStatus());
        child.setOwner(ub);
        child.setStudyId(currentStudy.getStudyId());
        child.setEntityId(parent.getEntityId());
        child.setEntityType(parent.getEntityType());
        child.setColumn(parent.getColumn());
        child.setField(parent.getField());

        return child;
    }

    private List<RuleSetBean> createRuleSet(StudySubjectBean ssub, StudyEventDefinitionBean sed) {

        return getRuleSetDao().findAllByStudyEventDef(sed);

    }

    private RuleSetDao getRuleSetDao() {
        return (RuleSetDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetDao");

    }

    private ArrayList getUncompletedCRFs(ArrayList eventDefinitionCRFs, ArrayList eventCRFs) {
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

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
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

        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            dedc.setEdc(edcrf);
            Boolean b = (Boolean) completed.get(new Integer(edcrf.getCrfId()));
            EventCRFBean ev = (EventCRFBean) startedButIncompleted.get(new Integer(edcrf.getCrfId()));
            if (b == null || !b.booleanValue()) {
                dedc.setEventCRF(ev);
                answer.add(dedc);
            }
        }

        return answer;
    }

    private void populateUncompletedCRFsWithCRFAndVersions(ArrayList uncompletedEventDefinitionCRFs) {
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());

        int size = uncompletedEventDefinitionCRFs.size();
        for (int i = 0; i < size; i++) {
            DisplayEventDefinitionCRFBean dedcrf = (DisplayEventDefinitionCRFBean) uncompletedEventDefinitionCRFs.get(i);
            CRFBean cb = (CRFBean) cdao.findByPK(dedcrf.getEdc().getCrfId());
            // note that we do not check status in the above query, so let's
            // check it here, tbh 102007
            if (cb.getStatus().equals(Status.AVAILABLE)) {
                // the above does not allow us to show the CRF as a thing with
                // status of 'invalid' so we have to
                // go to the JSP for this one, I think
                dedcrf.getEdc().setCrf(cb);

                ArrayList versions = (ArrayList) cvdao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
                dedcrf.getEdc().setVersions(versions);
                // added tbh 092007, fix for 1461
                if (versions != null && versions.size() != 0) {
                    boolean isLocked = false;
                    for (int ii = 0; ii < versions.size(); ii++) {
                        CRFVersionBean crfvb = (CRFVersionBean) versions.get(ii);
                        logger.debug("...checking versions..." + crfvb.getName());
                        if (!crfvb.getStatus().equals(Status.AVAILABLE)) {
                            logger.debug("found a non active crf version");
                            isLocked = true;
                        }
                    }
                    logger.debug("re-set event def, line 240: " + isLocked);
                    if (isLocked) {
                        dedcrf.setStatus(Status.LOCKED);
                        dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                    }
                    uncompletedEventDefinitionCRFs.set(i, dedcrf);
                } else {// above added 092007, tbh
                    dedcrf.setStatus(Status.LOCKED);
                    dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                    uncompletedEventDefinitionCRFs.set(i, dedcrf);
                } // added 102007, tbh
            } else {
                dedcrf.getEdc().setCrf(cb);
                logger.debug("_found a non active crf _");
                dedcrf.setStatus(Status.LOCKED);
                dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                dedcrf.getEdc().getCrf().setStatus(Status.LOCKED);
                uncompletedEventDefinitionCRFs.set(i, dedcrf);
            } // enclosing if statement added 102007, tbh
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
            if ("location".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_LOCATION_NOTE, "yes");
                request.setAttribute(LOCATION_NOTE, discrepancyNoteBean);
            } else if ("start_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_START_DATE_NOTE, "yes");
                request.setAttribute(START_DATE_NOTE, discrepancyNoteBean);
            } else if ("end_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_END_DATE_NOTE, "yes");
                request.setAttribute(END_DATE_NOTE, discrepancyNoteBean);
            }

        }

    }

    private RuleSetService getRuleSetService() {
        return (RuleSetService) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");
    }

    private AuditLogEventService getAuditLogEventService() {
        return  (AuditLogEventService) SpringServletAccess.getApplicationContext(context).getBean("auditLogEventService");
    }
}

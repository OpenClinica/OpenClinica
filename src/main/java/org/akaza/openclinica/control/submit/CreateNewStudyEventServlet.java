/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import com.openclinica.kafka.KafkaService;
import com.openclinica.kafka.dto.EventAttributeChangeDTO;
import core.org.akaza.openclinica.bean.core.NumericComparisonOperator;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.service.auth.TokenService;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.hibernate.RuleSetDao;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: support YYYY-MM-DD HH:MM time formats

@Component
public class CreateNewStudyEventServlet extends SecureController {

    Locale locale;
    // < ResourceBundlerestext,respage,resexception;

    public static final String INPUT_STUDY_EVENT_DEFINITION = "studyEventDefinition";
    public static final String INPUT_STUDY_SUBJECT = "studySubject";
    public static final String INPUT_STUDY_SUBJECT_LABEL = "studySubjectLabel";
    public static final String INPUT_STUDY_SUBJECT_ID_FROM_VIEWSUBJECT = "studySubjectId";
    public static final String INPUT_EVENT_DEF_ID_FROM_VIEWSUBJECT = "eventDefId";
    public static final String INPUT_STARTDATE_PREFIX = "start";
    public static final String INPUT_ENDDATE_PREFIX = "end";
    public static final String INPUT_REQUEST_STUDY_SUBJECT = "requestStudySubject";

    public static final String INPUT_LOCATION = "location";
    private final String COMMON = "common";

    private FormProcessor fp;

    public final static String[] INPUT_STUDY_EVENT_DEFINITION_SCHEDULED = { "studyEventDefinitionScheduled0", "studyEventDefinitionScheduled1",
            "studyEventDefinitionScheduled2", "studyEventDefinitionScheduled3" };
    public final static String[] INPUT_SCHEDULED_LOCATION = { "locationScheduled0", "locationScheduled1", "locationScheduled2", "locationScheduled3" };
    public final static String[] INPUT_STARTDATE_PREFIX_SCHEDULED = { "startScheduled0", "startScheduled1", "startScheduled2", "startScheduled3" };
    public final static String[] INPUT_ENDDATE_PREFIX_SCHEDULED = { "endScheduled0", "endScheduled1", "endScheduled2", "endScheduled3" };
    public final static String[] DISPLAY_SCHEDULED = { "display0", "display1", "display2", "display3" };
    public final static int ADDITIONAL_SCHEDULED_NUM = 4;

    private StudyEventDAO studyEventDAO;
    private StudySubjectDao studySubjectDao;

    @Override
    protected void processRequest() throws Exception {
        studyEventDAO = (StudyEventDAO) SpringServletAccess.getApplicationContext(context).getBean("studyEventJDBCDao");
        studySubjectDao = (StudySubjectDao) SpringServletAccess.getApplicationContext(context).getBean("studySubjectDaoDomain");

        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        panel.setStudyInfoShown(false);
        fp = new FormProcessor(request);
        FormDiscrepancyNotes discNotes = null;
        int studySubjectId = fp.getInt(INPUT_STUDY_SUBJECT_ID_FROM_VIEWSUBJECT);
        // input from manage subject matrix, user has specified definition id
        int studyEventDefinitionId = fp.getInt(INPUT_STUDY_EVENT_DEFINITION);

        // TODO: make this sensitive to permissions
        StudySubjectDAO sdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean studySubjectBean;
        if (studySubjectId <= 0) {
            studySubjectBean = (StudySubjectBean) request.getAttribute(INPUT_STUDY_SUBJECT);
        } else {
            // YW 11-08-2007, << a new study event could not be added if its
            // study subject has been removed
            studySubjectBean = (StudySubjectBean) sdao.findByPK(studySubjectId);
            Status subjectStatus = studySubjectBean.getStatus();
            if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
                addPageMessage(resword.getString("study_event") + resterm.getString("could_not_be") + resterm.getString("added") + "."
                        + respage.getString("study_subject_has_been_deleted"));
                request.setAttribute("id", new Integer(studySubjectId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
            // YW >>
            request.setAttribute(INPUT_REQUEST_STUDY_SUBJECT, "no");
        }

        // running this crashes the server, or so we think...instead, let's grab a count
        // or remove it altogether tbh 10/2009
        // ArrayList subjects = sdao.findAllActiveByStudyOrderByLabel(currentStudy);

        // TODO: make this sensitive to permissions
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());

        Study studyWithEventDefinitions = currentStudy;
        if (currentStudy.isSite()) {
            studyWithEventDefinitions = currentStudy.getStudy();
        }
        // find all active definitions with CRFs
        ArrayList<StudyEventDefinitionBean> eventDefinitions = seddao.findAllActiveByStudy(studyWithEventDefinitions);
        ArrayList<StudyEventDefinitionBean> tempList = new ArrayList<>();
        for (StudyEventDefinitionBean eventDefinition : eventDefinitions) {
            if (!eventDefinition.getType().equals(COMMON)) {
            	tempList.add(eventDefinition);
            }
        }
        eventDefinitions = new ArrayList(tempList);
        // EventDefinitionCRFDAO edcdao = new
        // EventDefinitionCRFDAO(sm.getDataSource());
        // ArrayList definitionsWithCRF = new ArrayList();
        // for (int i=0; i<eventDefinitions.size(); i++) {
        // StudyEventDefinitionBean sed =
        // (StudyEventDefinitionBean)eventDefinitions.get(i);
        // logger.info("StudyEventDefinition name[" + sed.getName() + "]");
        // ArrayList edcs = edcdao.findAllByEventDefinitionId(sed.getId());
        // if (!edcs.isEmpty()) {
        // definitionsWithCRF.add(sed);
        // }
        // }

        // Collections.sort(definitionsWithCRF);
        Collections.sort(eventDefinitions);

        /*
         * ArrayList eventDefinitionsScheduled = new ArrayList(); for (int i =
         * 0; i < eventDefinitions.size(); i++) { StudyEventDefinitionBean sed =
         * (StudyEventDefinitionBean) eventDefinitions.get(i); if
         * (sed.getType().equalsIgnoreCase("scheduled")) {
         * eventDefinitionsScheduled.add(sed); } }
         */
        // all definitions will appear in scheduled event creation box-11/26/05
        ArrayList eventDefinitionsScheduled = new ArrayList(eventDefinitions);

        if (!fp.isSubmitted()) {
            // sed.updateSampleOrdinals_v092();

            HashMap presetValues = new HashMap();

            // YW 08-16-2007 << set default as blank for time
            presetValues.put(INPUT_STARTDATE_PREFIX + "Hour", new Integer(-1));
            presetValues.put(INPUT_STARTDATE_PREFIX + "Minute", new Integer(-1));
            presetValues.put(INPUT_STARTDATE_PREFIX + "Half", new String(""));
            presetValues.put(INPUT_ENDDATE_PREFIX + "Hour", new Integer(-1));
            presetValues.put(INPUT_ENDDATE_PREFIX + "Minute", new Integer(-1));
            presetValues.put(INPUT_ENDDATE_PREFIX + "Half", new String(""));
            for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                presetValues.put(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Hour", new Integer(-1));
                presetValues.put(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Minute", new Integer(-1));
                presetValues.put(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Half", new String(""));
                presetValues.put(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Hour", new Integer(-1));
                presetValues.put(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Minute", new Integer(-1));
                presetValues.put(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Half", new String(""));
            }

            // SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            // example of taking the above line and transferring to i18n on the
            // below line, tbh
            String dateValue = local_df.format(new Date(System.currentTimeMillis()));
            presetValues.put(INPUT_STARTDATE_PREFIX + "Date", dateValue);
            for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                presetValues.put(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Date", dateValue);
                // location
                presetValues.put(INPUT_SCHEDULED_LOCATION[i], currentStudy.getFacilityCity());
                presetValues.put(this.DISPLAY_SCHEDULED[i], "none");
            }
            presetValues.put(INPUT_LOCATION, currentStudy.getFacilityCity());// defualt

            if (studySubjectBean != null && studySubjectBean.isActive()) {
                presetValues.put(INPUT_STUDY_SUBJECT, studySubjectBean);

                String requestStudySubject = (String) request.getAttribute(INPUT_REQUEST_STUDY_SUBJECT);
                if (requestStudySubject != null) {
                    presetValues.put(INPUT_REQUEST_STUDY_SUBJECT, requestStudySubject);

                    dateValue = local_df.format(new Date());
                    presetValues.put(INPUT_STARTDATE_PREFIX + "Date", dateValue);
                }
            }

            if (studyEventDefinitionId > 0) {
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);
                presetValues.put(INPUT_STUDY_EVENT_DEFINITION, sed);
            }

            // tbh
            logger.debug("set preset values: " + presetValues.toString());
            logger.debug("found def.w.CRF list, size " + eventDefinitions.size());
            // tbh
            setPresetValues(presetValues);

            ArrayList subjects = new ArrayList();
            setupBeans(subjects, eventDefinitions);

            discNotes = new FormDiscrepancyNotes();
            session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

            request.setAttribute("eventDefinitionsScheduled", eventDefinitionsScheduled);
            setInputMessages(new HashMap());
            forwardPage(Page.CREATE_NEW_STUDY_EVENT);
        } else {

            String dateCheck2 = request.getParameter("startDate");
            String endCheck2 = request.getParameter("endDate");
            logger.debug(dateCheck2 + "; " + endCheck2);

            // YW, 3-12-2008, 2220 fix <<
            String strEnd = fp.getDateTimeInputString(INPUT_ENDDATE_PREFIX);
            String strEndScheduled[] = new String[ADDITIONAL_SCHEDULED_NUM];
            for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                strEndScheduled[i] = fp.getDateTimeInputString(INPUT_ENDDATE_PREFIX_SCHEDULED[i]);
            }
            Date start = getInputStartDate();
            Date end = null;
            Date[] startScheduled = new Date[ADDITIONAL_SCHEDULED_NUM];
            for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                startScheduled[i] = getInputStartDateScheduled(i);
            }
            Date[] endScheduled = new Date[ADDITIONAL_SCHEDULED_NUM];

            // YW >>

            // for (java.util.Enumeration enu = request.getAttributeNames();
            // enu.hasMoreElements() ;) {
            // logger.debug(">>> found "+enu.nextElement().toString());
            // }
            // tbh
            discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
            if (discNotes == null) {
                discNotes = new FormDiscrepancyNotes();
                session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
            }
            DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);

            v.addValidation(INPUT_STARTDATE_PREFIX, Validator.IS_DATE_TIME);
            v.alwaysExecuteLastValidation(INPUT_STARTDATE_PREFIX);
            if (!strEnd.equals("")) {
                v.addValidation(INPUT_ENDDATE_PREFIX, Validator.IS_DATE_TIME);
                v.alwaysExecuteLastValidation(INPUT_ENDDATE_PREFIX);
            }

            v.addValidation(INPUT_STUDY_EVENT_DEFINITION, Validator.ENTITY_EXISTS_IN_STUDY, seddao, studyWithEventDefinitions);
            // v.addValidation(INPUT_STUDY_SUBJECT, Validator.ENTITY_EXISTS_IN_STUDY, sdao, currentStudy);
            // removed tbh 11/2009
            // Made optional field-issue-4904.
            // v.addValidation(INPUT_LOCATION, Validator.NO_BLANKS);
            v.addValidation(INPUT_STUDY_SUBJECT_LABEL, Validator.NO_BLANKS);
            v.addValidation(INPUT_LOCATION, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            if (currentStudy.getEventLocationRequired().equalsIgnoreCase("required")) {
                v.addValidation(INPUT_LOCATION, Validator.NO_BLANKS);
            }

            v.alwaysExecuteLastValidation(INPUT_LOCATION);

            boolean hasScheduledEvent = false;
            for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                if (!StringUtil.isBlank(fp.getString(this.INPUT_STUDY_EVENT_DEFINITION_SCHEDULED[i]))) {
                    // logger.debug("has scheduled definition******");
                    v.addValidation(this.INPUT_STUDY_EVENT_DEFINITION_SCHEDULED[i], Validator.ENTITY_EXISTS_IN_STUDY, seddao, studyWithEventDefinitions);
                    if (currentStudy.getEventLocationRequired().equalsIgnoreCase("required")) {
                        v.addValidation(INPUT_SCHEDULED_LOCATION[i], Validator.NO_BLANKS);
                        v.addValidation(INPUT_SCHEDULED_LOCATION[i], Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO,
                                2000);
                        v.alwaysExecuteLastValidation(INPUT_SCHEDULED_LOCATION[i]);
                    }
                    v.addValidation(INPUT_STARTDATE_PREFIX_SCHEDULED[i], Validator.IS_DATE_TIME);
                    v.alwaysExecuteLastValidation(INPUT_STARTDATE_PREFIX_SCHEDULED[i]);
                    if (!strEndScheduled[i].equals("")) {
                        v.addValidation(INPUT_ENDDATE_PREFIX_SCHEDULED[i], Validator.IS_DATE_TIME);
                        v.alwaysExecuteLastValidation(INPUT_ENDDATE_PREFIX_SCHEDULED[i]);
                    }
                    hasScheduledEvent = true;
                    fp.addPresetValue(DISPLAY_SCHEDULED[i], "all");
                } else {
                    fp.addPresetValue(DISPLAY_SCHEDULED[i], "none");
                }
            }

            HashMap errors = v.validate();
            // logger.debug("v is not null *****");
            String location = resword.getString("location");
            // don't allow user to use the default value 'Location' since
            // location
            // is a required field
            if (!StringUtil.isBlank(fp.getString(INPUT_LOCATION)) && fp.getString(INPUT_LOCATION).equalsIgnoreCase(location)) {
                Validator.addError(errors, INPUT_LOCATION, restext.getString("not_a_valid_location"));
            }

            StudyEventDefinitionBean definition = (StudyEventDefinitionBean) seddao.findByPK(fp.getInt(INPUT_STUDY_EVENT_DEFINITION));

            // StudySubjectBean studySubject = (StudySubjectBean) sdao.findByPK(fp.getInt(INPUT_STUDY_SUBJECT));
            // sdao.findByLabelAndStudy(label, study)
            StudySubjectBean studySubject = sdao.findByLabelAndStudy(fp.getString(INPUT_STUDY_SUBJECT_LABEL), currentStudy);
            // >> 4358 tbh, 11/2009
            // what if we are sent here from AddNewSubjectServlet.java??? we need to get that study subject bean
            if (request.getAttribute(INPUT_STUDY_SUBJECT) != null) {
                studySubject = (StudySubjectBean) request.getAttribute(INPUT_STUDY_SUBJECT);
            }
            // << tbh
            if (studySubject.getLabel() == "") {
                // add an error here, tbh
                Validator.addError(errors, INPUT_STUDY_SUBJECT, respage.getString("must_enter_subject_ID_for_identifying"));
            }

            if (!subjectMayReceiveStudyEvent(sm.getDataSource(), definition, studySubject)) {
                Validator.addError(errors, INPUT_STUDY_EVENT_DEFINITION, restext.getString("not_added_since_event_not_repeating"));
            }

            ArrayList<StudyEventDefinitionBean> definitionScheduleds = new ArrayList<StudyEventDefinitionBean>();
            int[] scheduledDefinitionIds = new int[ADDITIONAL_SCHEDULED_NUM];
            if (hasScheduledEvent) {
                for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                    int pk = fp.getInt(INPUT_STUDY_EVENT_DEFINITION_SCHEDULED[i]);
                    if (pk > 0) {
                        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(pk);
                        logger.debug("scheduled def:" + pk + " " + INPUT_STUDY_EVENT_DEFINITION_SCHEDULED[i] + " " + sedb.getName());
                        definitionScheduleds.add(sedb);
                        scheduledDefinitionIds[i] = pk;
                        if (!subjectMayReceiveStudyEvent(sm.getDataSource(), sedb, studySubject)) {
                            Validator.addError(errors, INPUT_STUDY_EVENT_DEFINITION_SCHEDULED[i], restext.getString("not_added_since_event_not_repeating"));
                        }
                    } else {
                        definitionScheduleds.add(new StudyEventDefinitionBean());
                    }
                }
            }

            // YW, 3-12-2008, 2220 fix >>
            if (!"".equals(strEnd) && !errors.containsKey(INPUT_STARTDATE_PREFIX) && !errors.containsKey(INPUT_ENDDATE_PREFIX)) {
                end = getInputEndDate();
                if (!fp.getString(INPUT_STARTDATE_PREFIX + "Date").equals(fp.getString(INPUT_ENDDATE_PREFIX + "Date"))) {
                    if (end.before(start)) {
                        Validator.addError(errors, INPUT_ENDDATE_PREFIX, resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                    }
                } else {
                    // if in same date, only check when both had time entered
                    if (fp.timeEntered(INPUT_STARTDATE_PREFIX) && fp.timeEntered(INPUT_ENDDATE_PREFIX)) {
                        if (end.before(start) || end.equals(start)) {
                            Validator.addError(errors, INPUT_ENDDATE_PREFIX,
                                    resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                        }
                    }
                }
            }

            String prevStartPrefix = INPUT_STARTDATE_PREFIX;
            Set<Integer> pickedSeds = new TreeSet<Integer>();
            pickedSeds.add(studyEventDefinitionId);
            HashMap<Integer, Integer> scheduledSeds = new HashMap<Integer, Integer>();
            scheduledSeds.put(studyEventDefinitionId, -1);
            for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                if (scheduledDefinitionIds[i] > 0 && !errors.containsKey(INPUT_STARTDATE_PREFIX_SCHEDULED[i])
                        && !errors.containsKey(INPUT_ENDDATE_PREFIX_SCHEDULED[i])) {
                    if (scheduledSeds.containsKey(scheduledDefinitionIds[i])) {
                        int prevStart = scheduledSeds.get(scheduledDefinitionIds[i]);
                        prevStartPrefix = prevStart == -1 ? INPUT_STARTDATE_PREFIX : INPUT_STARTDATE_PREFIX_SCHEDULED[prevStart];
                        Date prevStartDate = prevStart == -1 ? this.getInputStartDate()
                                : this.getInputStartDateScheduled(Integer.parseInt(prevStartPrefix.charAt(prevStartPrefix.length() - 1) + ""));
                        if (fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Date").equals(fp.getString(prevStartPrefix + "Date"))) {
                            // if in same day, only check when both have time
                            // inputs.
                            boolean schStartTime = fp.timeEntered(INPUT_STARTDATE_PREFIX_SCHEDULED[i]);
                            boolean startTime = fp.timeEntered(prevStartPrefix);
                            if (schStartTime && startTime) {
                                if (startScheduled[i].before(prevStartDate)) {
                                    Validator.addError(errors, INPUT_STARTDATE_PREFIX_SCHEDULED[i],
                                            resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                                }
                            }
                        } else {
                            if (startScheduled[i].before(prevStartDate)) {
                                Validator.addError(errors, INPUT_STARTDATE_PREFIX_SCHEDULED[i],
                                        resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                            }
                        }
                    }
                    scheduledSeds.put(scheduledDefinitionIds[i], i);
                    if (!strEndScheduled[i].equals("")) {
                        endScheduled[i] = fp.getDateTime(INPUT_ENDDATE_PREFIX_SCHEDULED[i]);
                        String prevEndPrefix = i > 0 ? INPUT_ENDDATE_PREFIX_SCHEDULED[i - 1] : INPUT_ENDDATE_PREFIX;
                        if (!fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Date").equals(fp.getString(prevEndPrefix + "Date"))) {
                            if (endScheduled[i].before(startScheduled[i])) {
                                Validator.addError(errors, INPUT_ENDDATE_PREFIX_SCHEDULED[i],
                                        resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                            }
                        } else {
                            // if in same date, only check when both had time
                            // entered
                            if (fp.timeEntered(INPUT_STARTDATE_PREFIX_SCHEDULED[i]) && fp.timeEntered(INPUT_ENDDATE_PREFIX_SCHEDULED[i])) {
                                if (endScheduled[i].before(startScheduled[i]) || endScheduled[i].equals(startScheduled[i])) {
                                    Validator.addError(errors, INPUT_ENDDATE_PREFIX_SCHEDULED[i],
                                            resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
                                }
                            }
                        }
                    }
                }
            }
            // YW >>
            logger.error("we have errors; number of this; " + errors.size());
            if (!errors.isEmpty()) {
                logger.debug("we have errors; number of this; " + errors.size());
                logger.error("found request study subject: " + fp.getString(INPUT_REQUEST_STUDY_SUBJECT));
                addPageMessage(respage.getString("errors_in_submission_see_below"));
                setInputMessages(errors);

                fp.addPresetValue(INPUT_STUDY_EVENT_DEFINITION, definition);
                fp.addPresetValue(INPUT_STUDY_SUBJECT, studySubject);
                fp.addPresetValue(INPUT_STUDY_SUBJECT_LABEL, fp.getString(INPUT_STUDY_SUBJECT_LABEL));
                fp.addPresetValue(INPUT_REQUEST_STUDY_SUBJECT, fp.getString(INPUT_REQUEST_STUDY_SUBJECT));
                fp.addPresetValue(INPUT_LOCATION, fp.getString(INPUT_LOCATION));

                for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                    fp.addPresetValue(INPUT_SCHEDULED_LOCATION[i], fp.getString(INPUT_SCHEDULED_LOCATION[i]));
                }
                String prefixes[] = new String[2 + 2 * ADDITIONAL_SCHEDULED_NUM];
                prefixes[0] = INPUT_STARTDATE_PREFIX;
                prefixes[1] = INPUT_ENDDATE_PREFIX;
                int b = ADDITIONAL_SCHEDULED_NUM + 2;
                for (int i = 2; i < b; ++i) {
                    prefixes[i] = INPUT_STARTDATE_PREFIX_SCHEDULED[i - 2];
                }
                for (int i = b; i < ADDITIONAL_SCHEDULED_NUM + b; ++i) {
                    prefixes[i] = INPUT_ENDDATE_PREFIX_SCHEDULED[i - b];
                }
                fp.setCurrentDateTimeValuesAsPreset(prefixes);

                if (hasScheduledEvent) {
                    for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                        fp.addPresetValue(INPUT_STUDY_EVENT_DEFINITION_SCHEDULED[i], definitionScheduleds.get(i));
                    }
                }

                setPresetValues(fp.getPresetValues());
                ArrayList subjects = new ArrayList();
                setupBeans(subjects, eventDefinitions);
                request.setAttribute("eventDefinitionsScheduled", eventDefinitionsScheduled);
                forwardPage(Page.CREATE_NEW_STUDY_EVENT);
            } else {
                logger.debug("error is empty");

                StudyEventBean studyEvent = new StudyEventBean();
                studyEvent.setStudyEventDefinitionId(definition.getId());
                studyEvent.setStudySubjectId(studySubject.getId());

                // YW 08-17-2007 <<
                if ("-1".equals(getInputStartHour()) && "-1".equals(getInputStartMinute()) && "".equals(getInputStartHalf())) {
                    studyEvent.setStartTimeFlag(false);
                } else {
                    studyEvent.setStartTimeFlag(true);
                }
                // YW >>
                studyEvent.setDateStarted(start);
                // comment to find bug 1389, tbh
                logger.debug("found start date: " + local_df.format(start));
                Date startScheduled2[] = new Date[ADDITIONAL_SCHEDULED_NUM];
                for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {
                    startScheduled2[i] = getInputStartDateScheduled(i);
                }
                // tbh
                // YW, 3-12-2008, 2220 fix <<
                if (!"".equals(strEnd)) {
                    // YW >>
                    if ("-1".equals(getInputEndHour()) && "-1".equals(getInputEndMinute()) && "".equals(getInputEndHalf())) {
                        studyEvent.setEndTimeFlag(false);
                    } else {
                        studyEvent.setEndTimeFlag(true);
                    }
                    studyEvent.setDateEnded(end);
                }
                studyEvent.setOwner(ub);
                studyEvent.setStatus(Status.AVAILABLE);
                studyEvent.setLocation(fp.getString(INPUT_LOCATION));
                studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);
                studyEvent.setSampleOrdinal(studyEventDAO.getMaxSampleOrdinal(definition, studySubject) + 1);
                studyEvent = (StudyEventBean) studyEventDAO.create(studyEvent);
                // getRuleSetService().runRulesInBeanProperty(createRuleSet(studySubject,definition),currentStudy,ub,request,studySubject);

                if (!studyEvent.isActive()) {
                    throw new OpenClinicaException(restext.getString("event_not_created_in_database"), "2");
                }

                unsignStudySubjectIfSigned(studyEvent.getStudySubjectId(), ub);
                addPageMessage(restext.getString("X_event_wiht_definition") + definition.getName() + restext.getString("X_and_subject") + studySubject.getName()
                        + respage.getString("X_was_created_succesfully"));

                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
                String[] eventFields = { INPUT_LOCATION, INPUT_STARTDATE_PREFIX, INPUT_ENDDATE_PREFIX };
                for (String element : eventFields) {
                    AddNewSubjectServlet.saveFieldNotes(element, fdn, dndao, studyEvent.getId(), "studyEvent", currentStudy);
                }
                if (hasScheduledEvent) {
                    for (int i = 0; i < ADDITIONAL_SCHEDULED_NUM; ++i) {

                        // should only do the following process if user inputs a
                        // scheduled event,
                        // which is scheduledDefinitionIds[i] > 0
                        if (scheduledDefinitionIds[i] > 0) {
                            if (subjectMayReceiveStudyEvent(sm.getDataSource(), definitionScheduleds.get(i), studySubject)) {

                                StudyEventBean studyEventScheduled = new StudyEventBean();
                                studyEventScheduled.setStudyEventDefinitionId(scheduledDefinitionIds[i]);
                                studyEventScheduled.setStudySubjectId(studySubject.getId());

                                // YW 11-14-2007
                                if ("-1".equals(fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Hour"))
                                        && "-1".equals(fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Minute"))
                                        && "".equals(fp.getString(INPUT_STARTDATE_PREFIX_SCHEDULED[i] + "Half"))) {
                                    studyEventScheduled.setStartTimeFlag(false);
                                } else {
                                    studyEventScheduled.setStartTimeFlag(true);
                                }
                                // YW >>

                                studyEventScheduled.setDateStarted(startScheduled[i]);
                                // YW, 3-12-2008, 2220 fix<<
                                if (!"".equals(strEndScheduled[i])) {
                                    endScheduled[i] = fp.getDateTime(INPUT_ENDDATE_PREFIX_SCHEDULED[i]);
                                    if ("-1".equals(fp.getString(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Hour"))
                                            && "-1".equals(fp.getString(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Minute"))
                                            && "".equals(fp.getString(INPUT_ENDDATE_PREFIX_SCHEDULED[i] + "Half"))) {
                                        studyEventScheduled.setEndTimeFlag(false);
                                    } else {
                                        studyEventScheduled.setEndTimeFlag(true);
                                    }
                                }
                                studyEventScheduled.setDateEnded(endScheduled[i]);
                                // YW >>
                                studyEventScheduled.setOwner(ub);
                                studyEventScheduled.setStatus(Status.AVAILABLE);
                                studyEventScheduled.setLocation(fp.getString(INPUT_SCHEDULED_LOCATION[i]));
                                studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);

                                // subjectsExistingEvents =
                                // sed.findAllByStudyAndStudySubjectId(
                                // currentStudy,
                                // studySubject.getId());
                                studyEventScheduled.setSampleOrdinal(studyEventDAO.getMaxSampleOrdinal(definitionScheduleds.get(i), studySubject) + 1);
                                // System.out.println("create scheduled events");
                                studyEventScheduled = (StudyEventBean) studyEventDAO.create(studyEventScheduled);
                                if (!studyEventScheduled.isActive()) {
                                    throw new OpenClinicaException(restext.getString("scheduled_event_not_created_in_database"), "2");
                                }
                                unsignStudySubjectIfSigned(studyEvent.getStudySubjectId(), ub);
                                AddNewSubjectServlet.saveFieldNotes(INPUT_SCHEDULED_LOCATION[i], fdn, dndao, studyEventScheduled.getId(), "studyEvent",
                                        currentStudy);
                                // YW 3-12-2008, 2220 fix <<
                                AddNewSubjectServlet.saveFieldNotes(INPUT_STARTDATE_PREFIX_SCHEDULED[i], fdn, dndao, studyEventScheduled.getId(), "studyEvent",
                                        currentStudy);
                                AddNewSubjectServlet.saveFieldNotes(INPUT_ENDDATE_PREFIX_SCHEDULED[i], fdn, dndao, studyEventScheduled.getId(), "studyEvent",
                                        currentStudy);
                                // YW >>
                            } else {
                                addPageMessage(restext.getString("scheduled_event_definition") + definitionScheduleds.get(i).getName()
                                        + restext.getString("X_and_subject") + studySubject.getName()
                                        + restext.getString("not_created_since_event_not_repeating") + restext.getString("event_type_already_exists"));
                            }
                        }
                    }

                } // if

                session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                request.setAttribute(EnterDataForStudyEventServlet.INPUT_EVENT_ID, String.valueOf(studyEvent.getId()));
                ArrayList<String> pMessage = (ArrayList<String>) request.getAttribute(SecureController.PAGE_MESSAGE);

                String url = response.encodeRedirectURL("ViewStudySubject?id=" + studySubject.getId());
                response.sendRedirect(url);
                // forwardPage(Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET);
                // we want to actually have url of entering data in browser, so
                // redirecting
                // response.sendRedirect(response.encodeRedirectURL(
                // "EnterDataForStudyEvent?eventId="
                // + studyEvent.getId()));
                return;
            }
        }
    }

    private void unsignStudySubjectIfSigned(int studySubjectId, UserAccountBean ub) {
        StudySubject studySubHib = studySubjectDao.findById(studySubjectId);
        if(studySubHib.getStatus().isSigned()){
            studySubHib.setStatus(core.org.akaza.openclinica.domain.Status.AVAILABLE);
            studySubHib.setUpdateId(ub.getId());
            studySubHib.setDateUpdated(new Date());
            studySubjectDao.saveOrUpdate(studySubHib);
        }
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.page_messages",
        // locale);
        // <
        // resexception=ResourceBundle.getBundle(
        // "core.org.akaza.openclinica.i18n.exceptions",locale);

        String exceptionName = resexception.getString("no_permission_to_add_new_study_event");
        String noAccessMessage = respage.getString("not_create_new_event") + " " + respage.getString("change_study_contact_sysadmin");

        if (SubmitDataServlet.maySubmitData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.MENU_SERVLET, exceptionName, "1");
    }

    /**
     * Determines whether a subject may receive an additional study event. This
     * is true if:
     * <ul>
     * <li>The study event definition is repeating; or
     * <li>The subject does not yet have a study event for the given study event
     * definition
     * </ul>
     *
     * @param studyEventDefinition
     *            The definition of the study event which is to be added for the
     *            subject.
     * @param studySubject
     *            The subject for which the study event is to be added.
     * @return <code>true</code> if the subject may receive an additional study
     *         event, <code>false</code> otherwise.
     */
    public boolean subjectMayReceiveStudyEvent(DataSource ds, StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {

        if (studyEventDefinition.isRepeating()) {
            // System.out.println("this def is repeating" +
            // studyEventDefinition.getName());
            return true;
        }

        ArrayList allEvents = studyEventDAO.findAllByDefinitionAndSubject(studyEventDefinition, studySubject);

        if (allEvents.size() > 0) {
            // System.out.println("this non-repeating def has event already" +
            // studyEventDefinition.getName());
            return false;
        }

        return true;
    }

    private void setupBeans(ArrayList subjects, ArrayList eventDefinitions) throws Exception {
        // addEntityList("subjects", subjects, restext.getString("cannot_create_event_because_no_subjects"),
        // Page.LIST_STUDY_SUBJECTS_SERVLET);
        // removed by tbh, 10/2009
        addEntityList("eventDefinitions", eventDefinitions, restext.getString("cannot_create_event_because_no_event_definitions"),
                Page.LIST_STUDY_SUBJECTS_SERVLET);

    }

    private Date getInputStartDate() {
        return fp.getDateTime(INPUT_STARTDATE_PREFIX);
    }

    private Date getInputStartDateScheduled(int i) {
        return fp.getDateTime(INPUT_STARTDATE_PREFIX_SCHEDULED[i]);
    }

    private Date getInputEndDate() {
        /*
         * if (fp.getString(INPUT_ENDDATE_PREFIX + "Date").equals("")) { return
         * fp.getDateTime(INPUT_STARTDATE_PREFIX); } else { return
         * fp.getDateTime(INPUT_ENDDATE_PREFIX); }
         */
        return fp.getDateTime(INPUT_ENDDATE_PREFIX);
    }

    // YW 08-17-2007
    private String getInputStartHour() {
        return fp.getString(INPUT_STARTDATE_PREFIX + "Hour");
    }

    private String getInputStartMinute() {
        return fp.getString(INPUT_STARTDATE_PREFIX + "Minute");
    }

    private String getInputStartHalf() {
        return fp.getString(INPUT_STARTDATE_PREFIX + "Half");
    }

    private String getInputEndHour() {
        return fp.getString(INPUT_ENDDATE_PREFIX + "Hour");
    }

    private String getInputEndMinute() {
        return fp.getString(INPUT_ENDDATE_PREFIX + "Minute");
    }

    private String getInputEndHalf() {
        return fp.getString(INPUT_ENDDATE_PREFIX + "Half");
    }

    // YW >>
    private List<RuleSetBean> createRuleSet(StudySubjectBean ssub, StudyEventDefinitionBean sed) {

        return getRuleSetDao().findAllByStudyEventDef(sed);

    }

    private RuleSetService getRuleSetService() {
        return (RuleSetService) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");
    }

    private RuleSetDao getRuleSetDao() {
        return (RuleSetDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetDao");

    }

}

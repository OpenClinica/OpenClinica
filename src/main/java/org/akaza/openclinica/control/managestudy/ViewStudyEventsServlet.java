/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import core.org.akaza.openclinica.web.bean.StudyEventRow;

/**
 * @author jxu
 *
 *         Handles user request of "view study events"
 */
public class ViewStudyEventsServlet extends SecureController {

    Locale locale;
    // < ResourceBundlerestext;

    public static final String INPUT_STARTDATE = "startDate";

    public static final String INPUT_ENDDATE = "endDate";

    public static final String INPUT_DEF_ID = "definitionId";

    public static final String INPUT_STATUS_ID = "statusDisplayValue";

    public static final String STATUS_MAP = "statuses";

    public static final String DEFINITION_MAP = "definitions";

    public static final String PRINT = "print";
    private final String COMMON = "common";
    private StudyEventDAO studyEventDAO;
    private EventCRFDAO eventCRFDAO;

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.notes",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, restext.getString("not_correct_role"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        studyEventDAO = (StudyEventDAO) SpringServletAccess.getApplicationContext(context).getBean("studyEventJDBCDao");
        eventCRFDAO = (EventCRFDAO) SpringServletAccess.getApplicationContext(context).getBean("eventCRFJDBCDao");
        // checks which module requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int sedId = fp.getInt("sedId");
        String statusDisplayValue = fp.getString(INPUT_STATUS_ID);
        int definitionId = fp.getInt(INPUT_DEF_ID);
        Date startDate = fp.getDate(INPUT_STARTDATE);
        Date endDate = fp.getDate(INPUT_ENDDATE);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        String defaultStartDateString = month + "/01/" + year;
        Date defaultStartDate = new Date();

        defaultStartDate = new SimpleDateFormat("MM/dd/yy").parse(defaultStartDateString);
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        defaultStartDateString = dateFormatter.format(defaultStartDate);

        cal.setTime(defaultStartDate);
        cal.add(Calendar.DATE, 30);
        Date defaultEndDate = cal.getTime();

        if (!fp.isSubmitted()) {
            logger.info("not submitted");
            HashMap presetValues = new HashMap();

            presetValues.put(INPUT_STARTDATE, local_df.format(defaultStartDate));
            presetValues.put(INPUT_ENDDATE, local_df.format(defaultEndDate));
            startDate = defaultStartDate;
            endDate = defaultEndDate;
            setPresetValues(presetValues);
        } else {
            Validator v = new Validator(request);
            v.addValidation(INPUT_STARTDATE, Validator.IS_A_DATE);
            v.addValidation(INPUT_ENDDATE, Validator.IS_A_DATE);
            errors = v.validate();
            if (!errors.isEmpty()) {
                setInputMessages(errors);
                startDate = defaultStartDate;
                endDate = defaultEndDate;
            }
            fp.addPresetValue(INPUT_STARTDATE, fp.getString(INPUT_STARTDATE));
            fp.addPresetValue(INPUT_ENDDATE, fp.getString(INPUT_ENDDATE));
            fp.addPresetValue(INPUT_DEF_ID, fp.getInt(INPUT_DEF_ID));
            fp.addPresetValue(INPUT_STATUS_ID, fp.getString(INPUT_STATUS_ID));
            setPresetValues(fp.getPresetValues());
        }

        ArrayList statuses = new ArrayList();

        for(StudyEventWorkflowStatusEnum statusEnum :StudyEventWorkflowStatusEnum.values()) {
            if (!statusEnum.equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED)) {
                statuses.add(statusEnum.getDisplayValue());
            }
        }
        statuses.add(resterm.getString(SIGNED.toLowerCase()).toLowerCase());
        statuses.add(resterm.getString(LOCKED.toLowerCase()).toLowerCase());
        statuses.add(resterm.getString(NOT_SIGNED.toLowerCase()).toLowerCase());
        statuses.add(resterm.getString(NOT_LOCKED.toLowerCase()).toLowerCase());

        request.setAttribute(STATUS_MAP, statuses);

        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource(), getStudyDao());
        ArrayList<StudyEventDefinitionBean> definitions = seddao.findAllByStudy(currentStudy);
        ArrayList tempList = new ArrayList<>();
        for (StudyEventDefinitionBean defn : definitions) {
            if (!defn.getType().equals(COMMON)) {
                tempList.add(defn);
            }
        }
        definitions = new ArrayList(tempList);

        request.setAttribute(DEFINITION_MAP, definitions);

        ArrayList allEvents = new ArrayList();
        allEvents = genTables(fp, definitions, startDate, endDate, sedId, definitionId, statusDisplayValue);

        request.setAttribute("allEvents", allEvents);

        // for print version
        String queryUrl = INPUT_STARTDATE + "=" + local_df.format(startDate) + "&" + INPUT_ENDDATE + "=" + local_df.format(endDate) + "&" + INPUT_DEF_ID + "="
                + definitionId + "&" + INPUT_STATUS_ID + "=" + statusDisplayValue + "&" + "sedId=" + sedId + "&submitted=" + fp.getInt("submitted");
        request.setAttribute("queryUrl", queryUrl);
        if ("yes".equalsIgnoreCase(fp.getString(PRINT))) {
            allEvents = genEventsForPrint(fp, definitions, startDate, endDate, sedId, definitionId, statusDisplayValue);
            request.setAttribute("allEvents", allEvents);
            forwardPage(Page.VIEW_STUDY_EVENTS_PRINT);
        } else {
            forwardPage(Page.VIEW_STUDY_EVENTS);
        }

    }

    /**
     *
     * @param fp
     * @param definitions
     * @param startDate
     * @param endDate
     * @param sedId
     * @param definitionId
     * @param statusDisplayValue
     * @return
     */
    private ArrayList genTables(FormProcessor fp, ArrayList definitions, Date startDate, Date endDate, int sedId, int definitionId, String statusDisplayValue ) {

        ArrayList allEvents = new ArrayList();
        definitions = findDefinitionById(definitions, definitionId);
        // YW <<
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        ArrayList studySubjects = ssdao.findAllByStudyId(currentStudy.getStudyId());
        // YW >>
        for (int i = 0; i < definitions.size(); i++) {
            ViewEventDefinitionBean ved = new ViewEventDefinitionBean();
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) definitions.get(i);
            ved.setDefinition(sed);

            // YW <<
            ArrayList events = new ArrayList();
            for (int s = 0; s < studySubjects.size(); ++s) {
                StudySubjectBean ssb = (StudySubjectBean) studySubjects.get(s);
                ArrayList evts = studyEventDAO.findAllWithSubjectLabelByStudySubjectAndDefinition(ssb, sed.getId());

                for (int v = 0; v < evts.size(); ++v) {
                    StudyEventBean seb = (StudyEventBean) evts.get(v);
                    if (!(currentRole.isDirector() || currentRole.isCoordinator()) &&  seb.isLocked()) {
                        seb.setEditable(false);
                    }
                    events.add(seb);
                }
            }

            int subjectScheduled = 0;
            int subjectCompleted = 0;
            int subjectDiscontinued = 0;
            events = findEventByStatusAndDate(events, statusDisplayValue, startDate, endDate);

            Date firstStartDateForScheduled = null;
            Date lastCompletionDate = null;
            // find the first firstStartDateForScheduled
            for (int k = 0; k < events.size(); k++) {
                StudyEventBean se = (StudyEventBean) events.get(k);
                if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)) {
                    firstStartDateForScheduled = se.getDateStarted();
                    break;
                }

            }
            // find the first lastCompletionDate
            for (int k = 0; k < events.size(); k++) {
                StudyEventBean se = (StudyEventBean) events.get(k);
                if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED) && se.getDateEnded() != null) {
                    lastCompletionDate = se.getDateEnded();
                    break;
                }
            }

            for (int j = 0; j < events.size(); j++) {
                StudyEventBean se = (StudyEventBean) events.get(j);
                if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)) {
                    subjectScheduled++;
                    if (se.getDateStarted().before(new Date())) {
                        ArrayList eventCRFs = eventCRFDAO.findAllByStudyEvent(se);
                        if (eventCRFs.isEmpty()) {
                            se.setScheduledDatePast(true);
                        }
                    }
                    if (firstStartDateForScheduled == null) {
                        firstStartDateForScheduled = se.getDateStarted();
                    } else if (se.getDateStarted().before(firstStartDateForScheduled)) {
                        firstStartDateForScheduled = se.getDateStarted();
                    }
                } else if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                    subjectCompleted++;
                    if (lastCompletionDate == null) {
                        lastCompletionDate = se.getDateEnded();
                    } else if (se.getDateEnded() != null && se.getDateEnded().after(lastCompletionDate)) {
                        lastCompletionDate = se.getDateEnded();
                    }
            //  ****************    } else if (se.getWorkflowStatus().getId() > 4) {
                    // dropped out/stopped/skipped/relapse
            //        subjectDiscontinued++;
                }
            }

            ved.setSubjectCompleted(subjectCompleted);
            ved.setSubjectScheduled(subjectScheduled);
            ved.setSubjectDiscontinued(subjectDiscontinued);
            ved.setFirstScheduledStartDate(firstStartDateForScheduled);
            ved.setLastCompletionDate(lastCompletionDate);

            EntityBeanTable table;
            if (sedId == sed.getId()) {// apply finding function or ordering
                // function
                // to a specific table
                table = fp.getEntityBeanTable();
            } else {
                table = new EntityBeanTable();
            }
            table.setSortingIfNotExplicitlySet(1, false);// sort by event
            // start date,
            // desc
            ArrayList allEventRows = StudyEventRow.generateRowsFromBeans(events);

            String[] columns = { resword.getString("study_subject_ID"), resword.getString("event_date_started"), resword.getString("subject_event_status"),
                    resword.getString("actions") };
            table.setColumns(new ArrayList(Arrays.asList(columns)));
            table.hideColumnLink(3);
            HashMap args = new HashMap();
            args.put("sedId", new Integer(sed.getId()).toString());
            args.put("definitionId", new Integer(definitionId).toString());
            args.put("statusDisplayValue", statusDisplayValue);
            args.put("startDate", local_df.format(startDate));
            args.put("endDate", local_df.format(endDate));
            table.setQuery("ViewStudyEvents", args);
            table.setRows(allEventRows);
            table.computeDisplay();

            ved.setStudyEventTable(table);

            if (!events.isEmpty()) {
                allEvents.add(ved);
            }
        }

        // A. Hamid.
        return allEvents;
    }

    /**
     * Generates an arraylist of study events for printing
     *
     * @param fp
     * @param definitions
     * @param startDate
     * @param endDate
     * @param sedId
     * @param definitionId
     * @param statusDisplayValue
     * @return
     */
    private ArrayList genEventsForPrint(FormProcessor fp, ArrayList definitions, Date startDate, Date endDate, int sedId, int definitionId, String statusDisplayValue) {
        ArrayList allEvents = new ArrayList();
        definitions = findDefinitionById(definitions, definitionId);
        // YW <<
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        ArrayList studySubjects = ssdao.findAllByStudyId(currentStudy.getStudyId());
        // YW >>
        for (int i = 0; i < definitions.size(); i++) {
            ViewEventDefinitionBean ved = new ViewEventDefinitionBean();
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) definitions.get(i);

            ved.setDefinition(sed);

            // YW <<
            ArrayList events = new ArrayList();
            for (int s = 0; s < studySubjects.size(); ++s) {
                StudySubjectBean ssb = (StudySubjectBean) studySubjects.get(s);
                ArrayList evts = studyEventDAO.findAllWithSubjectLabelByStudySubjectAndDefinition(ssb, sed.getId());

                for (int v = 0; v < evts.size(); ++v) {
                    events.add(evts.get(v));
                }
            }
            // YW >>

            int subjectScheduled = 0;
            int subjectCompleted = 0;
            int subjectDiscontinued = 0;
            events = findEventByStatusAndDate(events, statusDisplayValue, startDate, endDate);

            Date firstStartDateForScheduled = null;
            Date lastCompletionDate = null;
            // find the first firstStartDateForScheduled
            for (int k = 0; k < events.size(); k++) {
                StudyEventBean se = (StudyEventBean) events.get(k);
                if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)) {
                    firstStartDateForScheduled = se.getDateStarted();
                    break;
                }

            }
            // find the first lastCompletionDate
            for (int k = 0; k < events.size(); k++) {
                StudyEventBean se = (StudyEventBean) events.get(k);
                if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                    lastCompletionDate = se.getDateEnded();
                    break;
                }
            }

            for (int j = 0; j < events.size(); j++) {
                StudyEventBean se = (StudyEventBean) events.get(j);
                if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SCHEDULED)) {
                    subjectScheduled++;
                    if (se.getDateStarted().before(new Date())) {
                        ArrayList eventCRFs = eventCRFDAO.findAllByStudyEvent(se);
                        if (eventCRFs.isEmpty()) {
                            se.setScheduledDatePast(true);
                        }
                    }
                    if (firstStartDateForScheduled == null) {
                        firstStartDateForScheduled = se.getDateStarted();
                    } else if (se.getDateStarted().before(firstStartDateForScheduled)) {
                        firstStartDateForScheduled = se.getDateStarted();
                    }
                } else if (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                    subjectCompleted++;
                    if (lastCompletionDate == null) {
                        lastCompletionDate = se.getDateEnded();
                    } else if (se.getDateEnded().after(lastCompletionDate)) {
                        lastCompletionDate = se.getDateEnded();
                    }
         //////**************       } else if (se.getSubjectEventStatus().getId() > 4) {
                    // dropped out/stopped/skipped/relapse
   //                 subjectDiscontinued++;
                }

            }
            ved.setSubjectCompleted(subjectCompleted);
            ved.setSubjectScheduled(subjectScheduled);
            ved.setSubjectDiscontinued(subjectDiscontinued);
            ved.setFirstScheduledStartDate(firstStartDateForScheduled);
            ved.setLastCompletionDate(lastCompletionDate);

            ved.setStudyEvents(events);

            if (!events.isEmpty()) {
                allEvents.add(ved);
            }
        }

        return allEvents;
    }

    /**
     *
     * @param definitions
     * @param definitionId
     * @return
     */
    private ArrayList findDefinitionById(ArrayList definitions, int definitionId) {
        if (definitionId > 0) {
            for (int i = 0; i < definitions.size(); i++) {
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) definitions.get(i);
                if (sed.getId() == definitionId) {
                    ArrayList a = new ArrayList();
                    a.add(sed);
                    return a;
                }
            }
        }
        return definitions;
    }

    /**
     *
     * @param events
     * @param statusDisplayValue
     * @param startDate
     * @param endDate
     * @return
     */
    private ArrayList findEventByStatusAndDate(ArrayList events, String statusDisplayValue, Date startDate, Date endDate) {
        ArrayList a = new ArrayList();
        for (int i = 0; i < events.size(); i++) {
            StudyEventBean se = (StudyEventBean) events.get(i);
            if (!se.getDateStarted().before(startDate) && !se.getDateStarted().after(endDate)) {
                a.add(se);
            }
        }
        ArrayList b = new ArrayList();
        if (!statusDisplayValue.isEmpty()) {
            for (int i = 0; i < a.size(); i++) {
                StudyEventBean se = (StudyEventBean) a.get(i);

                if ((statusDisplayValue.equalsIgnoreCase(resterm.getString(LOCKED.toLowerCase())) && se.isLocked())
                        || (statusDisplayValue.equalsIgnoreCase(resterm.getString(NOT_LOCKED.toLowerCase())) && !se.isLocked())
                        || (statusDisplayValue.equalsIgnoreCase(resterm.getString(SIGNED.toLowerCase())) && se.isSigned())
                        || (statusDisplayValue.equalsIgnoreCase(resterm.getString(NOT_SIGNED.toLowerCase())) && !se.isSigned())
                        || (se.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.getByI18nDescription(statusDisplayValue)))) {
                    b.add(se);
                }
            }
            return b;
        }
        return a;
    }
}

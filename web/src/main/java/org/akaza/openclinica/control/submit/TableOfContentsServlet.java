/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.managestudy.ViewStudySubjectServlet;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ssachs
 */

// TODO: make it possible to input an event crf bean to this servlet rather than
// an int
public class TableOfContentsServlet extends SecureController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String BEAN_DISPLAY = "toc";

    // these inputs are used when you get here from a jsp page
    // e.g. TableOfContents?action=ide_c&id=123
    public static final String INPUT_ACTION = "action";

    public static final String INPUT_ID = "ecid";

    // these inputs are used when another servlet sends you here
    // such as mark crf complete, initial data entry, etc
    public static final String INPUT_EVENT_CRF_BEAN = "eventCRF";

    // these are only for use with ACTION_START_INITIAL_DATA_ENTRY
    public static final String INPUT_EVENT_DEFINITION_CRF_ID = "eventDefinitionCRFId";

    public static final String INPUT_CRF_VERSION_ID = "crfVersionId";

    public static final String INPUT_STUDY_EVENT_ID = "studyEventId";

    public static final String INPUT_SUBJECT_ID = "subjectId";

    public static final String INPUT_EVENT_CRF_ID = "eventCRFId";

    // these inputs are displayed on the table of contents and
    // are used to edit Event CRF properties
    public static final String INPUT_INTERVIEWER = "interviewer";

    public static final String INPUT_INTERVIEW_DATE = "interviewDate";

    public static final String ACTION_START_INITIAL_DATA_ENTRY = "ide_s";

    public static final String ACTION_CONTINUE_INITIAL_DATA_ENTRY = "ide_c";

    public static final String ACTION_START_DOUBLE_DATA_ENTRY = "dde_s";

    public static final String ACTION_CONTINUE_DOUBLE_DATA_ENTRY = "dde_c";

    public static final String ACTION_ADMINISTRATIVE_EDITING = "ae";

    public static final String[] ACTIONS = { ACTION_START_INITIAL_DATA_ENTRY, ACTION_CONTINUE_INITIAL_DATA_ENTRY, ACTION_START_DOUBLE_DATA_ENTRY,
            ACTION_CONTINUE_DOUBLE_DATA_ENTRY, ACTION_ADMINISTRATIVE_EDITING };

    private FormProcessor fp;

    private EventCRFDAO ecdao;

    private EventCRFBean ecb;

    private String action;

    private void getEventCRFAndAction() {
        ecdao = new EventCRFDAO(sm.getDataSource());

        ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF_BEAN);

        if (ecb == null) {
            int ecid = fp.getInt(INPUT_ID, true);
            AuditableEntityBean aeb = ecdao.findByPKAndStudy(ecid, currentStudy);

            if (!aeb.isActive()) {
                ecb = new EventCRFBean();
            } else {
                ecb = (EventCRFBean) aeb;
            }

            action = fp.getString(INPUT_ACTION, true);
        } else {
            action = getActionForStage(ecb.getStage());
        }
    }

    /**
     * Determines if the action requested is a valid action.
     * 
     * @param action
     *            The action requested.
     * @return <code>true</code> if the action is valid, <code>false</code>
     *         otherwise.
     */
    private boolean invalidAction(String action) {
        ArrayList validActions = new ArrayList(Arrays.asList(ACTIONS));
        return !validActions.contains(action);
    }

    /**
     * Determines if the action requested is consistent with the specified Event
     * CRF's data entry stage.
     * 
     * @param action
     *            The action requested.
     * @param ecb
     *            The Event CRF whose data entry stage is being checked for
     *            consistency with the action.
     * @return <code>true</code> if the action is consistent with the Event
     *         CRF's stage, <code>false</code> otherwise.
     */
    private boolean isConsistentAction(String action, EventCRFBean ecb) {
        DataEntryStage stage = ecb.getStage();

        boolean isConsistent = true;
        if (action.equals(ACTION_START_INITIAL_DATA_ENTRY) && !stage.equals(DataEntryStage.UNCOMPLETED)) {
            isConsistent = false;
        } else if (action.equals(ACTION_CONTINUE_INITIAL_DATA_ENTRY) && !stage.equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
            isConsistent = false;
        } else if (action.equals(ACTION_START_DOUBLE_DATA_ENTRY) && !stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)) {
            isConsistent = false;
        } else if (action.equals(ACTION_CONTINUE_DOUBLE_DATA_ENTRY) && !stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
            isConsistent = false;
        } else if (action.equals(ACTION_ADMINISTRATIVE_EDITING) && !stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)) {
            isConsistent = false;
        }

        return isConsistent;
    }

    /**
     * Creates a new Event CRF or update the exsiting one, that is, an event CRF
     * can be created but not item data yet, in this case, still consider it is
     * not started(called uncompleted before)
     * 
     * @return
     * @throws Exception
     */
    private EventCRFBean createEventCRF() throws Exception {
        EventCRFBean ecb;
        ecdao = new EventCRFDAO(sm.getDataSource());

        int crfVersionId = fp.getInt(INPUT_CRF_VERSION_ID);
        int studyEventId = fp.getInt(INPUT_STUDY_EVENT_ID);
        int eventDefinitionCRFId = fp.getInt(INPUT_EVENT_DEFINITION_CRF_ID);
        int subjectId = fp.getInt(INPUT_SUBJECT_ID);
        int eventCRFId = fp.getInt(INPUT_EVENT_CRF_ID);

        logger.info("Creating event CRF within Table of Contents.  Study id: " + currentStudy.getId() + "; CRF Version id: " + crfVersionId
                + "; Study Event id: " + studyEventId + "; Event Definition CRF id: " + eventDefinitionCRFId + "; Subject: " + subjectId);

        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean ssb = ssdao.findBySubjectIdAndStudy(subjectId, currentStudy);

        if (!ssb.isActive()) {
            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("trying_to_begin_DE1"));
        }

        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sedb = seddao.findByEventDefinitionCRFId(eventDefinitionCRFId);

        if (!ssb.isActive() || !sedb.isActive()) {
            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("trying_to_begin_DE2"));
        }

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(crfVersionId);
        List<CRFVersionBean> crfVersions = cvdao.findAllByCRFId(formLayout.getCrfId());
        CRFVersionBean crfVersion = crfVersions.get(0);
        EntityBean eb = cvdao.findByPK(crfVersionId);

        if (!eb.isActive()) {
            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("trying_to_begin_DE3"));
        }

        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventBean sEvent = (StudyEventBean) sedao.findByPK(studyEventId);

        StudyBean studyWithSED = currentStudy;
        if (currentStudy.getParentStudyId() > 0) {
            studyWithSED = new StudyBean();
            studyWithSED.setId(currentStudy.getParentStudyId());
        }

        AuditableEntityBean aeb = sedao.findByPKAndStudy(studyEventId, studyWithSED);

        if (!aeb.isActive()) {
            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("trying_to_begin_DE4"));
        }

        ecb = new EventCRFBean();
        if (eventCRFId == 0) {// no event CRF created yet
            ecb.setAnnotations("");
            ecb.setCreatedDate(new Date());
            ecb.setCRFVersionId(crfVersion.getId());
            ecb.setInterviewerName("");
            if (sEvent.getDateStarted() != null) {
                ecb.setDateInterviewed(sEvent.getDateStarted());// default date
            } else {
                ecb.setDateInterviewed(null);
            }
            ecb.setOwnerId(ub.getId());
            ecb.setStatus(Status.AVAILABLE);
            ecb.setCompletionStatusId(1);
            ecb.setStudySubjectId(ssb.getId());
            ecb.setStudyEventId(studyEventId);
            ecb.setValidateString("");
            ecb.setValidatorAnnotations("");
            ecb.setFormLayout(formLayout);

            ecb = (EventCRFBean) ecdao.create(ecb);
            logger.info("CREATED EVENT CRF");
        } else {
            // there is an event CRF already, only need to update
            ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
            ecb.setCRFVersionId(crfVersionId);
            ecb.setUpdatedDate(new Date());
            ecb.setUpdater(ub);
            ecb = (EventCRFBean) ecdao.update(ecb);

        }

        if (!ecb.isActive()) {
            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("new_event_CRF_not_created_database_error"));
        } else {
            sEvent.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
            sEvent.setUpdater(ub);
            sEvent.setUpdatedDate(new Date());
            sedao.update(sEvent);

        }

        return ecb;
    }

    private void validateEventCRFAndAction() throws Exception {
        if (invalidAction(action)) {
            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("no_action_specified_or_invalid"));
        }

        if (!isConsistentAction(action, ecb)) {
            HashMap verbs = new HashMap();
            verbs.put(ACTION_START_INITIAL_DATA_ENTRY, resword.getString("start_initial_data_entry"));
            verbs.put(ACTION_CONTINUE_INITIAL_DATA_ENTRY, resword.getString("continue_initial_data_entry"));
            verbs.put(ACTION_START_DOUBLE_DATA_ENTRY, resword.getString("start_double_data_entry"));
            verbs.put(ACTION_CONTINUE_DOUBLE_DATA_ENTRY, resword.getString("continue_double_data_entry"));
            verbs.put(ACTION_ADMINISTRATIVE_EDITING, resword.getString("perform_administrative_editing"));
            String verb = (String) verbs.get(action);

            if (verb == null) {
                verb = "start initial data entry";
            }

            throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
                    resexception.getString("you_are_trying_to") + verb + " " + resexception.getString("on_event_CRF_inappropiate_action"));
        }

        if (action.equals(ACTION_START_DOUBLE_DATA_ENTRY)) {
            ecb.setValidatorId(ub.getId());
            ecb.setDateValidate(new Date());

            ecb = (EventCRFBean) ecdao.update(ecb);
        }
    }

    private void updatePresetValues(EventCRFBean ecb) {
        fp.addPresetValue(INPUT_INTERVIEWER, ecb.getInterviewerName());
        if (ecb.getDateInterviewed() != null) {
            // SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            String idateFormatted = local_df.format(ecb.getDateInterviewed());
            fp.addPresetValue(INPUT_INTERVIEW_DATE, idateFormatted);
        } else {
            fp.addPresetValue(INPUT_INTERVIEW_DATE, "");
        }
        setPresetValues(fp.getPresetValues());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        FormDiscrepancyNotes discNotes;
        if (action.equals(ACTION_START_INITIAL_DATA_ENTRY)) {
            ecb = createEventCRF();
        } else {
            validateEventCRFAndAction();
        }

        updatePresetValues(ecb);

        Boolean b = (Boolean) request.getAttribute(DataEntryServlet.INPUT_IGNORE_PARAMETERS);

        if (fp.isSubmitted() && b == null) {
            discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
            if (discNotes == null) {
                discNotes = new FormDiscrepancyNotes();
                session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

            }
            DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);

            v.addValidation(INPUT_INTERVIEWER, Validator.NO_BLANKS);
            v.addValidation(INPUT_INTERVIEW_DATE, Validator.IS_A_DATE);
            v.alwaysExecuteLastValidation(INPUT_INTERVIEW_DATE);

            errors = v.validate();

            if (errors.isEmpty()) {

                ecb.setInterviewerName(fp.getString(INPUT_INTERVIEWER));
                ecb.setDateInterviewed(fp.getDate(INPUT_INTERVIEW_DATE));

                if (ecdao == null) {
                    ecdao = new EventCRFDAO(sm.getDataSource());
                }

                ecb = (EventCRFBean) ecdao.update(ecb);

                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());

                AddNewSubjectServlet.saveFieldNotes(INPUT_INTERVIEWER, fdn, dndao, ecb.getId(), "EventCRF", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(INPUT_INTERVIEW_DATE, fdn, dndao, ecb.getId(), "EventCRF", currentStudy);

                if (ecdao.isQuerySuccessful()) {
                    updatePresetValues(ecb);
                    if (!fp.getBoolean("editInterview", true)) {
                        // editing completed
                        addPageMessage(respage.getString("interviewer_name_date_updated"));
                    }
                } else {
                    addPageMessage(respage.getString("database_error_interviewer_name_date_not_updated"));
                }

            } else {
                String[] textFields = { INPUT_INTERVIEWER, INPUT_INTERVIEW_DATE };
                fp.setCurrentStringValuesAsPreset(textFields);

                setInputMessages(errors);
                setPresetValues(fp.getPresetValues());
            }
        } else {
            discNotes = new FormDiscrepancyNotes();
            session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

        }

        DisplayTableOfContentsBean displayBean = getDisplayBean(ecb, sm.getDataSource(), currentStudy);

        // this is for generating side info panel
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
        ArrayList beans = ViewStudySubjectServlet.getDisplayStudyEventsForStudySubject(ssb, sm.getDataSource(), ub, currentRole);
        request.setAttribute("studySubject", ssb);
        request.setAttribute("beans", beans);
        request.setAttribute("eventCRF", ecb);

        request.setAttribute(BEAN_DISPLAY, displayBean);

        boolean allowEnterData = true;
        if (StringUtil.isBlank(ecb.getInterviewerName())) {
            if (discNotes == null || discNotes.getNotes(TableOfContentsServlet.INPUT_INTERVIEWER).isEmpty()) {
                allowEnterData = false;
            }
        }

        if (ecb.getDateInterviewed() == null) {
            if (discNotes == null || discNotes.getNotes(TableOfContentsServlet.INPUT_INTERVIEW_DATE).isEmpty()) {
                allowEnterData = false;
            }
        }

        if (!allowEnterData) {
            request.setAttribute("allowEnterData", "no");
            // forwardPage(Page.INTERVIEWER);
            /*
             * BWP 2966 >> the original Page.INTERVIEWER jsp is not a complete
             * web page and did not provide a body tag for producing a popup
             * window for discrepancy notes. So I changed it to create a
             * complete web page.
             */
            forwardPage(Page.INTERVIEWER_ENTIRE_PAGE);
        } else {

            if (fp.getBoolean("editInterview", true)) {
                // user wants to edit interview info
                request.setAttribute("allowEnterData", "yes");
                forwardPage(Page.INTERVIEWER);
            } else {
                if (fp.isSubmitted() && !errors.isEmpty()) {
                    // interview form submitted, but has blank field or
                    // validation error
                    request.setAttribute("allowEnterData", "no");
                    forwardPage(Page.INTERVIEWER);
                } else {
                    request.setAttribute("allowEnterData", "yes");
                    forwardPage(Page.TABLE_OF_CONTENTS);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        fp = new FormProcessor(request);
        getEventCRFAndAction();

        Role r = currentRole.getRole();
        boolean isSuper = DisplayEventCRFBean.isSuper(ub, r);

        if (!SubmitDataServlet.maySubmitData(ub, currentRole)) {
            String exceptionName = resexception.getString("no_permission_to_perform_data_entry");
            String noAccessMessage = respage.getString("you_may_not_perform_data_entry_on_a_CRF") + " "
                    + respage.getString("change_study_contact_study_coordinator");

            addPageMessage(noAccessMessage);
            throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
        }

        // we're creating an event crf
        if (action.equals(ACTION_START_INITIAL_DATA_ENTRY)) {
            return;
        }
        // we're editing an existing event crf
        else {
            if (!ecb.isActive()) {
                addPageMessage(respage.getString("event_CRF_not_exist_contact_study_coordinator"));
                throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("event_CRF_not_belong_current_study"), "1");
            }

            if (action.equals(ACTION_CONTINUE_INITIAL_DATA_ENTRY)) {
                if (ecb.getOwnerId() == ub.getId() || isSuper) {
                    return;
                } else {
                    addPageMessage(respage.getString("not_begin_DE_on_CRF_not_resume_DE"));
                    throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("event_CRF_not_belong_current_user"),
                            "1");
                }
            } else if (action.equals(ACTION_START_DOUBLE_DATA_ENTRY)) {
                if (ecb.getOwnerId() != ub.getId()) {
                    return;
                } else {
                    if (!DisplayEventCRFBean.initialDataEntryCompletedMoreThanTwelveHoursAgo(ecb) && !isSuper) {
                        addPageMessage(respage.getString("began_DE_on_CRF_marked_complete_less_12_not_begin_DE"));
                        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("owner_attempting_DDE_12_hours"),
                                "1");
                    } else {
                        return;
                    }
                }
            } else if (action.equals(ACTION_CONTINUE_INITIAL_DATA_ENTRY)) {
                if (ecb.getValidatorId() == ub.getId() || isSuper) {
                    return;
                } else {
                    addPageMessage(respage.getString("not_begin_DDE_on_CRF_not_resume_DE"));
                    throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("validation_event_CRF_not_begun_user"),
                            "1");
                }
            } else if (action.equals(ACTION_ADMINISTRATIVE_EDITING)) {
                if (isSuper) {
                    return;
                } else {
                    addPageMessage(respage.getString("you_may_not_perform_administrative_editing") + " "
                            + respage.getString("change_study_contact_study_coordinator"));
                    throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET,
                            resexception.getString("no_permission_to_perform_administrative_editing"), "1");
                }
            } // end else if (action.equals(ACTION_ADMINISTRATIVE_EDITING))
        } // end else (for actions other than ACTION_START_INITIAL_DATA_ENTRY
    } // end mayProceed

    public static int getIntById(HashMap h, Integer key) {
        Integer value = (Integer) h.get(key);
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    /**
     * Assumes the Event CRF's data entry stage is not Uncompleted.
     * 
     * @param ecb
     *            An Event CRF which should be displayed in the table of
     *            contents.
     * @return A text link to the Table of Contents servlet for the bean.
     */
    public static String getLink(EventCRFBean ecb) {
        DataEntryStage stage = ecb.getStage();
        String answer = Page.TABLE_OF_CONTENTS_SERVLET.getFileName();

        answer = Page.TABLE_OF_CONTENTS_SERVLET.getFileName();
        answer += "?action=" + getActionForStage(ecb.getStage());
        answer += "&" + INPUT_ID + "=" + ecb.getId();

        return answer;
    }

    public static String getActionForStage(DataEntryStage stage) {
        if (stage.equals(DataEntryStage.UNCOMPLETED)) {
        }

        else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
            return ACTION_CONTINUE_INITIAL_DATA_ENTRY;
        }

        else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)) {
            return ACTION_START_DOUBLE_DATA_ENTRY;
        }

        else if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
            return ACTION_CONTINUE_DOUBLE_DATA_ENTRY;
        }

        else if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)) {
            return ACTION_ADMINISTRATIVE_EDITING;
        }

        return "";
    }

    public static ArrayList getSections(EventCRFBean ecb, DataSource ds) {
        SectionDAO sdao = new SectionDAO(ds);
        ItemGroupDAO igdao = new ItemGroupDAO(ds);

        HashMap numItemsBySectionId = sdao.getNumItemsBySectionId();
        HashMap numItemsPlusRepeatBySectionId = sdao.getNumItemsPlusRepeatBySectionId(ecb);
        HashMap numItemsCompletedBySectionId = sdao.getNumItemsCompletedBySectionId(ecb);
        HashMap numItemsPendingBySectionId = sdao.getNumItemsPendingBySectionId(ecb);

        ArrayList sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());

        for (int i = 0; i < sections.size(); i++) {
            SectionBean sb = (SectionBean) sections.get(i);

            int sectionId = sb.getId();
            Integer key = new Integer(sectionId);
            // YW 10-11-2007 << handle number of item completion on tab.
            int numItems = getIntById(numItemsBySectionId, key);
            List<ItemGroupBean> itemGroups = igdao.findLegitGroupBySectionId(sectionId);
            if (!itemGroups.isEmpty()) {
                // this section has repeating rows-jxu
                int numItemsPlusRepeat = getIntById(numItemsPlusRepeatBySectionId, key);
                if (numItemsPlusRepeat > numItems) {
                    sb.setNumItems(numItemsPlusRepeat);
                } else {
                    sb.setNumItems(numItems);
                }
            } else {
                sb.setNumItems(numItems);
            }

            // According to logic that I searched from code of this package by
            // this time,
            // for double data entry and stage.initial_data_entry,
            // pending should be the status in query.
            int numItemsCompleted = getIntById(numItemsCompletedBySectionId, key);
            // the following is removed to fix issue 2091-jxu
            // if(numItemsCompleted == 0) {
            // numItemsCompleted = getIntById(numItemsPendingBySectionId, key) ;
            // }
            sb.setNumItemsCompleted(numItemsCompleted);
            // YW >>
            sb.setNumItemsNeedingValidation(getIntById(numItemsPendingBySectionId, key));
            sections.set(i, sb);
        }

        return sections;
    }

    public static DisplayTableOfContentsBean getDisplayBean(EventCRFBean ecb, DataSource ds, StudyBean currentStudy) {
        DisplayTableOfContentsBean answer = new DisplayTableOfContentsBean();

        answer.setEventCRF(ecb);

        // get data
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
        answer.setStudySubject(ssb);

        StudyEventDAO sedao = new StudyEventDAO(ds);
        StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
        answer.setStudyEvent(seb);

        SectionDAO sdao = new SectionDAO(ds);
        ArrayList sections = getSections(ecb, ds);
        answer.setSections(sections);

        // get metadata
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(seb.getStudyEventDefinitionId());
        answer.setStudyEventDefinition(sedb);

        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
        answer.setCrfVersion(cvb);

        CRFDAO cdao = new CRFDAO(ds);
        CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
        answer.setCrf(cb);

        StudyBean studyForStudySubject = new StudyDAO(ds).findByStudySubjectId(ssb.getId());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        EventDefinitionCRFBean edcb = edcdao.findByStudyEventDefinitionIdAndCRFId(studyForStudySubject, sedb.getId(), cb.getId());
        answer.setEventDefinitionCRF(edcb);

        answer.setAction(getActionForStage(ecb.getStage()));

        return answer;
    }

    /**
     * A section contains all hidden dynamics will be removed from data entry tab and jump box.
     *
     * @param ds
     * @param displayTableOfContentsBean
     * @param dynamicsMetadataService
     * @return
     */
    public static DisplayTableOfContentsBean getDisplayBeanWithShownSections(DataSource ds, DisplayTableOfContentsBean displayTableOfContentsBean,
            DynamicsMetadataService dynamicsMetadataService) {
        if (displayTableOfContentsBean == null) {
            return displayTableOfContentsBean;
        }
        EventCRFBean ecb = displayTableOfContentsBean.getEventCRF();
        SectionDAO sectionDAO = new SectionDAO(ds);
        ArrayList<SectionBean> sectionBeans = getSections(ecb, ds);
        ArrayList<SectionBean> showSections = new ArrayList<SectionBean>();
        if (sectionBeans != null && sectionBeans.size() > 0) {
            for (SectionBean s : sectionBeans) {
                if (sectionDAO.containNormalItem(s.getCRFVersionId(), s.getId())) {
                    showSections.add(s);
                } else {
                    // for section contains dynamics, does it contain showing item_group/item?
                    if (dynamicsMetadataService.hasShowingDynGroupInSection(s.getId(), s.getCRFVersionId(), ecb.getId())) {
                        showSections.add(s);
                    } else {
                        if (dynamicsMetadataService.hasShowingDynItemInSection(s.getId(), s.getCRFVersionId(), ecb.getId())) {
                            showSections.add(s);
                        }
                    }
                }
            }
            displayTableOfContentsBean.setSections(showSections);
        }
        return displayTableOfContentsBean;
    }

    public static LinkedList<Integer> sectionIdsInToc(DisplayTableOfContentsBean toc) {
        LinkedList<Integer> ids = new LinkedList<Integer>();
        if (toc != null) {
            ArrayList<SectionBean> sectionBeans = toc.getSections();
            if (sectionBeans != null && sectionBeans.size() > 0) {
                for (int i = 0; i < sectionBeans.size(); ++i) {
                    SectionBean s = sectionBeans.get(i);
                    ids.add(s.getId());
                }
            }
        }
        return ids;
    }

    /**
     * Index starts from 0. If not in, return -1.
     * 
     * @param sb
     * @param toc
     * @param sectionIdsInToc
     * @return
     */
    public static int sectionIndexInToc(SectionBean sb, DisplayTableOfContentsBean toc, LinkedList<Integer> sectionIdsInToc) {
        ArrayList<SectionBean> sectionBeans = new ArrayList<SectionBean>();
        int index = -1;
        if (toc != null) {
            sectionBeans = toc.getSections();
        }
        if (sectionBeans != null && sectionBeans.size() > 0) {
            for (int i = 0; i < sectionIdsInToc.size(); ++i) {
                if (sb.getId() == sectionIdsInToc.get(i)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
}

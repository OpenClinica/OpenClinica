/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

// import core.org.akaza.openclinica.bean.core.Role;

import core.org.akaza.openclinica.domain.datamap.Study;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import core.org.akaza.openclinica.bean.core.*;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import core.org.akaza.openclinica.dao.hibernate.RuleSetDao;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.controller.helper.TemplateHelper;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

// import javax.servlet.http.*;

/**
 * Enroll a new subject into system
 *
 * @author ssachs
 * @version CVS: $Id: AddNewSubjectServlet.java,v 1.15 2005/07/05 17:20:43 jxu
 * Exp $
 */
@Configurable
public class AddNewSubjectServlet extends SecureController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    // Shaoyu Su
    private final Object simpleLockObj = new Object();
    public static final String INPUT_UNIQUE_IDENTIFIER = "uniqueIdentifier";// global
    // Id

    public static final String INPUT_DOB = "dob";

    public static final String INPUT_YOB = "yob"; // year of birth

    public static final String INPUT_GENDER = "gender";

    public static final String INPUT_LABEL = "label";

    public static final String INPUT_SECONDARY_LABEL = "secondaryLabel";

    public static final String INPUT_ENROLLMENT_DATE = "enrollmentDate";

    public static final String INPUT_EVENT_START_DATE = "startDate";

    public static final String INPUT_GROUP = "group";

    public static final String FORM_DISCREPANCY_NOTES_NAME = "fdnotes";


    public static final String BEAN_GROUPS = "groups";


    public static final String SUBMIT_EVENT_BUTTON = "submitEvent";

    public static final String SUBMIT_ENROLL_BUTTON = "submitEnroll";

    public static final String SUBMIT_DONE_BUTTON = "submitDone";

    public static final String EDIT_DOB = "editDob";

    public static final String EXISTING_SUB_SHOWN = "existingSubShown";


    public static final String STUDY_EVENT_DEFINITION = "studyEventDefinition";
    public static final String LOCATION = "location";

    // YW <<
    String DOB = "";
    String YOB = "";
    String GENDER = "";
    public static final String G_WARNING = "gWarning";
    public static final String D_WARNING = "dWarning";
    public static final String Y_WARNING = "yWarning";
    boolean needUpdate;
    SubjectBean updateSubject = new SubjectBean();
    @Autowired
    private Configuration freemarkerConfiguration;

    // YW >>

    @Override
    public void init() throws ServletException {
        super.init();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {

        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));

        StudySubjectDAO ssd = new StudySubjectDAO(sm.getDataSource());
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
        ArrayList classes = new ArrayList();
        panel.setStudyInfoShown(false);
        FormProcessor fp = new FormProcessor(request);
        FormDiscrepancyNotes discNotes;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        // TODO l10n for dates? Note that in some places we hard-code the YOB by
        // using "01/01/"+yob,
        // not exactly supporting i18n...tbh

        // YW << update study parameters of current study.
        // "collectDob" and "genderRequired" are set as the same as the parent
        // study
        Study tempParentStudy = null;
        if (!currentStudy.isSite()) {
//            parentStudyId = currentStudy.getStudyId();
            classes = sgcdao.findAllActiveByStudy(currentStudy);
        } else {
            tempParentStudy = currentStudy.getStudy();
            classes = sgcdao.findAllActiveByStudy(tempParentStudy);
        }
        if(currentStudy.isSite()) {
            currentStudy.setCollectDob(tempParentStudy.getCollectDob());
            currentStudy.setGenderRequired(tempParentStudy.getGenderRequired());
            // YW >>
            // tbh
            currentStudy.setSubjectPersonIdRequired(tempParentStudy.getSubjectPersonIdRequired());
            // end fix for 1750, tbh 10 2007
        }
        if (!fp.isSubmitted()) {
            if (fp.getBoolean("instr")) {
                session.removeAttribute(FORM_DISCREPANCY_NOTES_NAME);
                forwardPage(Page.INSTRUCTIONS_ENROLL_SUBJECT);
            } else {

                setUpBeans(classes);
                Date today = new Date(System.currentTimeMillis());

                // YW 10-07-2007 <<
                String idSetting = "";
                if (currentStudy.isSite()) {
                    currentStudy.setSubjectIdGeneration(tempParentStudy.getSubjectIdGeneration());
                }
                idSetting = currentStudy.getSubjectIdGeneration();
                // YW >>
                logger.info("subject id setting :" + idSetting);
                // set up auto study subject id
                // Shaoyu Su: if idSetting is auto, do not calculate the next
                // available ID (label) for now
                if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
                    //Shaoyu Su
                    // int nextLabel = ssd.findTheGreatestLabel() + 1;
                    // fp.addPresetValue(INPUT_LABEL, new Integer(nextLabel).toString());
                    fp.addPresetValue(INPUT_LABEL, resword.getString("id_generated_Save_Add"));
                }

                setPresetValues(fp.getPresetValues());
                discNotes = new FormDiscrepancyNotes();
                session.setAttribute(FORM_DISCREPANCY_NOTES_NAME, discNotes);
                forwardPage(Page.ADD_NEW_SUBJECT);

            }
            if(tempParentStudy != null)
                tempParentStudy = null;
        } else {// submitted
            if
            (tempParentStudy != null)
                tempParentStudy = null;
            discNotes = (FormDiscrepancyNotes) session.getAttribute(FORM_DISCREPANCY_NOTES_NAME);
            if (discNotes == null) {
                discNotes = new FormDiscrepancyNotes();
            }

            DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);
            String label = fp.getString(INPUT_LABEL).trim();

            if (label.equalsIgnoreCase(resword.getString("id_generated_Save_Add"))) {
                boolean labelExists;
                int iteration =0;
                do {
                    labelExists = false;
                    iteration++;
                    label = generateParticipantIdUsingTemplate();
                    StudySubjectBean subjectWithSameLabel = ssd.findByLabelAndStudyForCreatingParticipant(label, currentStudy.getStudyId());
                    StudySubjectBean subjectWithSameLabelInParent = null;
                    if (currentStudy.isSite()) {
                        subjectWithSameLabelInParent = ssd.findByLabelAndStudyForCreatingParticipant(label, currentStudy.getStudy().getStudyId());
                    }
                    if (subjectWithSameLabel.isActive() || (subjectWithSameLabelInParent != null && subjectWithSameLabelInParent.isActive())) {
                        labelExists = true;
                    }
                } while (labelExists && iteration < 11);

                request.setAttribute(INPUT_LABEL, label);
            }
            v.addValidation(INPUT_LABEL, Validator.NO_BLANKS);

            String subIdSetting = currentStudy.getSubjectIdGeneration();
            if (!subIdSetting.equalsIgnoreCase("auto editable")) {
                v.addValidation(INPUT_LABEL, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 30);
            }

            HashMap errors = v.validate();

            if (label.contains("<") || label.contains(">")) {
                Validator.addError(errors, INPUT_LABEL, resexception
                        .getString("study_subject_id_can_not_contain_html_lessthan_or_greaterthan_elements"));
            }
            StudySubjectBean subjectWithSameLabel = ssd.findByLabelAndStudyForCreatingParticipant(label, currentStudy.getStudyId());

            StudySubjectBean subjectWithSameLabelInParent = new StudySubjectBean();
            if (currentStudy.isSite()) {
                subjectWithSameLabelInParent = ssd.findByLabelAndStudyForCreatingParticipant(label, currentStudy.getStudy().getStudyId());

            }
            if (subjectWithSameLabel.isActive() || subjectWithSameLabelInParent.isActive()) {
                Validator.addError(errors, INPUT_LABEL, resexception.getString("another_assigned_this_ID_choose_unique"));
            }

            if (isEnrollmentCapped()) {
                Validator.addError(errors, INPUT_LABEL, resexception.getString("current_study_full"));
            }
            //checkIfStudyEnrollmentCapped(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_full"));

            if (!errors.isEmpty() && subIdSetting.equalsIgnoreCase("auto non-editable") && !isEnrollmentCapped()) {
                // generate default template

                StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());

                StudySubjectBean studySubjectBean = new StudySubjectBean();
                while (studySubjectBean != null) {
                    Random rnd = new Random();
                    int n = 100000 + rnd.nextInt(900000);
                    label = currentStudy.getOc_oid() + "-" + n;
                    studySubjectBean = ssdao.findByLabel(label);
                    if (studySubjectBean != null && !studySubjectBean.isActive())
                        studySubjectBean = null;
                }
                errors.clear();
            }

            if (!errors.isEmpty()) {

                addPageMessage(respage.getString("there_were_some_errors_submission"));
                setInputMessages(errors);
                fp.addPresetValue(INPUT_LABEL, label);
                setPresetValues(fp.getPresetValues());

                Object isSubjectOverlay = fp.getRequest().getParameter("subjectOverlay");
                if (isSubjectOverlay != null) {
                    request.setAttribute("showOverlay", true);
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                } else {
                    forwardPage(Page.ADD_NEW_SUBJECT);
                }

            } else {

                int subjectCount = getSubjectCount(currentStudy);

                currentStudy.setSubjectCount(subjectCount + 1);
                getStudyDao().update(currentStudy);

                // no errors
                SubjectBean subject = new SubjectBean();
                subject.setStatus(Status.AVAILABLE);
                subject.setOwner(ub);
                SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
                subject = sdao.create(subject);
                if (!subject.isActive()) {
                    throw new OpenClinicaException(resexception.getString("could_not_create_subject"), "3");
                }

                StudySubjectBean studySubject = new StudySubjectBean();
                studySubject.setSubjectId(subject.getId());
                studySubject.setStudyId(currentStudy.getStudyId());
                studySubject.setLabel(label);
                studySubject.setStatus(Status.AVAILABLE);
                studySubject.setOwner(ub);
                studySubject = ssd.createWithoutGroup(studySubject);
                if (!studySubject.isActive()) {
                    throw new OpenClinicaException(resexception.getString("could_not_create_study_subject"), "4");
                }


                request.removeAttribute(FormProcessor.FIELD_SUBMITTED);
                request.setAttribute(CreateNewStudyEventServlet.INPUT_STUDY_SUBJECT, studySubject);
                request.setAttribute(CreateNewStudyEventServlet.INPUT_REQUEST_STUDY_SUBJECT, "no");
                request.setAttribute(FormProcessor.FIELD_SUBMITTED, "0");
                addPageMessage(respage.getString("subject_with_unique_identifier") + studySubject.getLabel() + respage.getString("X_was_created_succesfully"));
                request.setAttribute("id", studySubject.getId() + "");
                //forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                response.sendRedirect(request.getContextPath() + "/ViewStudySubject?id=" + new Integer(studySubject.getId()).toString());
                return;
            }
        }// end of no error (errors.isEmpty())
    }// end of fp.isSubmitted()

    private List<RuleSetBean> createRuleSet(StudySubjectBean ssub,
                                            StudyEventDefinitionBean sed) {

        return getRuleSetDao().findAllByStudyEventDef(sed);


    }

    private RuleSetService getRuleSetService() {
        return (RuleSetService) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");
    }


    private RuleSetDao getRuleSetDao() {
        return (RuleSetDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetDao");

    }


    protected void createStudyEvent(FormProcessor fp, StudySubjectBean s) {
        int studyEventDefinitionId = fp.getInt("studyEventDefinition");
        String location = fp.getString("location");
        Date startDate = s.getEventStartDate();
        if (studyEventDefinitionId > 0) {
            String locationTerm = resword.getString("location");
            // don't allow user to use the default value 'Location' since
            // location
            // is a required field
            if (location.equalsIgnoreCase(locationTerm)) {
                addPageMessage(restext.getString("not_a_valid_location"));
            } else {
                logger.info("will create event with new subject");
                StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
                StudyEventBean se = new StudyEventBean();
                se.setLocation(location);
                se.setDateStarted(startDate);
                se.setDateEnded(startDate);
                se.setOwner(ub);
                se.setStudyEventDefinitionId(studyEventDefinitionId);
                se.setStatus(Status.AVAILABLE);
                se.setStudySubjectId(s.getId());
                se.setWorkflowStatus(StudyEventWorkflowStatusEnum.SCHEDULED);


                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);


                se.setSampleOrdinal(sedao.getMaxSampleOrdinal(sed, s) + 1);
                sedao.create(se);
                //    getRuleSetService().runRulesInBeanProperty(createRuleSet(s,sed),currentStudy,ub,request,s);


            }

        } else {
            addPageMessage(respage.getString("no_event_sheduled_for_subject"));

        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        String exceptionName = resexception.getString("no_permission_to_add_new_subject");
        String noAccessMessage = respage.getString("may_not_add_new_subject") + " " + respage.getString("change_study_contact_sysadmin");

        if (SubmitDataServlet.maySubmitData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.MENU_SERVLET, exceptionName, "1");
    }

    protected void setUpBeans(ArrayList classes) throws Exception {
        StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
        // addEntityList(BEAN_GROUPS, sgdao.findAllByStudy(currentStudy),
        // "A group must be available in order to add new subjects to this
        // study;
        // however, there are no groups in this Study. Please contact your Study
        // Director.",
        // Page.SUBMIT_DATA);

        for (int i = 0; i < classes.size(); i++) {
            StudyGroupClassBean group = (StudyGroupClassBean) classes.get(i);
            ArrayList studyGroups = sgdao.findAllByGroupClass(group);
            group.setStudyGroups(studyGroups);
        }

        request.setAttribute(BEAN_GROUPS, classes);
    }

    /**
     * Save the discrepancy notes of each field into session in the form
     *
     * @param field
     * @param notes
     * @param dndao
     * @param entityId
     * @param entityType
     * @param sb
     */
    public static void saveFieldNotes(String field, FormDiscrepancyNotes notes, DiscrepancyNoteDAO dndao, int entityId, String entityType, Study sb) {

        saveFieldNotes(field, notes, dndao, entityId, entityType, sb, -1);

    }

    public static void saveFieldNotes(String field, FormDiscrepancyNotes notes,
                                      DiscrepancyNoteDAO dndao, int entityId, String entityType, Study sb,
                                      int event_crf_id) {

        if (notes == null || dndao == null || sb == null) {
            // logger.info("AddNewSubjectServlet,saveFieldNotes:parameter is
            // null, cannot proceed:");
            return;
        }
        ArrayList fieldNotes = notes.getNotes(field);
        if ((fieldNotes == null || fieldNotes.size() < 1) && event_crf_id > 0) {
            fieldNotes = notes.getNotes(field);
        }
        // System.out.println("+++ notes size:" + fieldNotes.size() + " for field " + field);
        for (int i = 0; i < fieldNotes.size(); i++) {
            DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) fieldNotes.get(i);
            dnb.setEntityId(entityId);
            dnb.setStudyId(sb.getStudyId());
            dnb.setEntityType(entityType);

            // updating exsiting note if necessary
            /*
             * if ("itemData".equalsIgnoreCase(entityType)) {
             * System.out.println(" **** find parent note for item data:" +
             * dnb.getEntityId()); ArrayList parentNotes =
             * dndao.findExistingNotesForItemData(dnb.getEntityId()); for (int j
             * = 0; j < parentNotes.size(); j++) { DiscrepancyNoteBean parent =
             * (DiscrepancyNoteBean) parentNotes.get(j); if
             * (parent.getParentDnId() == 0) { if
             * (dnb.getDiscrepancyNoteTypeId() ==
             * parent.getDiscrepancyNoteTypeId()) { if
             * (dnb.getResolutionStatusId() != parent.getResolutionStatusId()) {
             * parent.setResolutionStatusId(dnb.getResolutionStatusId());
             * dndao.update(parent); } } } } }
             */
            if (dnb.getResolutionStatusId() == 0) {
                dnb.setResStatus(ResolutionStatus.NOT_APPLICABLE);
                dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
                if (!dnb.getDisType().equals(DiscrepancyNoteType.REASON_FOR_CHANGE)) {
                    dnb.setResStatus(ResolutionStatus.OPEN);
                    dnb.setResolutionStatusId(ResolutionStatus.OPEN.getId());
                }

            }
            // << tbh 05/2010 second fix to try out queries
            dnb = (DiscrepancyNoteBean) dndao.create(dnb);
            dndao.createMapping(dnb);

            if (dnb.getParentDnId() == 0) {
                // see issue 2659 this is a new thread, we will create two notes
                // in this case,
                // This way one can be the parent that updates as the status
                // changes, but one also stays as New.
                dnb.setParentDnId(dnb.getId());
                dnb = (DiscrepancyNoteBean) dndao.create(dnb);
                dndao.createMapping(dnb);
            } else if (dnb.getParentDnId() > 0) {
                DiscrepancyNoteBean parentNote = (DiscrepancyNoteBean) dndao.findByPK(dnb.getParentDnId());
                if (dnb.getDiscrepancyNoteTypeId() == parentNote.getDiscrepancyNoteTypeId() && dnb.getResolutionStatusId() != parentNote.getResolutionStatusId()) {
                    parentNote.setResolutionStatusId(dnb.getResolutionStatusId());
                    dndao.update(parentNote);
                }
            }
        }
    }

    public String generateParticipantIdUsingTemplate() {
        Map<String, Object> data = new HashMap<String, Object>();
        String templateID = "";
        if(currentStudy.isSite())
            templateID = currentStudy.getStudy().getParticipantIdTemplate();
        else
            templateID = currentStudy.getParticipantIdTemplate();

        int subjectCount = getSubjectCount(currentStudy);

        String siteId = currentStudy.getUniqueIdentifier();

        // Adding Sample data to validate templateID
        data.put("siteId", siteId);
        data.put("siteParticipantCount", subjectCount);
        data.put("helper", new TemplateHelper());
        StringWriter wtr = new StringWriter();
        Template template = null;

        try {
            template = new Template("template name", new StringReader(templateID), freemarkerConfiguration);
            template.process(data, wtr);
            logger.info("Template ID  :" + wtr.toString());

        } catch (TemplateException te) {
            logger.error("Template is not able to be parsed properly: ",te);


        } catch (IOException ioe) {
            logger.error("Template is not working properly: ",ioe);


        }
        return wtr.toString();
    }


}

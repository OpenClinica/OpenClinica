/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.NumericComparisonOperator;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import core.org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jxu Processes request to update a study subject
 */
public class UpdateStudySubjectServlet extends SecureController {

    SimpleDateFormat yformat = new SimpleDateFormat("yyyy");

    StudySubjectBean studySub;
    SubjectBean subject;
    FormDiscrepancyNotes formDiscNotes;

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
                || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        StudyDAO stdao = new StudyDAO(sm.getDataSource());
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO studySubdao = new StudySubjectDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        boolean updateIsValid = false;

        String fromResolvingNotes = fp.getString("fromResolvingNotes", true);
        if (StringUtil.isBlank(fromResolvingNotes)) {
            session.removeAttribute(ViewNotesServlet.WIN_LOCATION);
            session.removeAttribute(ViewNotesServlet.NOTES_TABLE);
            checkStudyLocked(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_locked"));
            checkStudyFrozen(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_frozen"));
        }

        int studySubId = Integer.valueOf(fp.getString("id"));

        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_study_subject_to_edit"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            String action = fp.getString("action", true);
            if (StringUtil.isBlank(action)) {
                addPageMessage(respage.getString("no_action_specified"));
                forwardPage(Page.LIST_STUDY_SUBJECTS);
                return;
            }

            studySub = (StudySubjectBean) studySubdao.findByPK(studySubId);
            subject = (SubjectBean) sdao.findByPK(studySub.getSubjectId());
            StudyBean study = (StudyBean) stdao.findByPK(studySub.getStudyId());


            StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
            SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
            ArrayList groupMaps = (ArrayList) sgmdao.findAllByStudySubject(studySubId);

            HashMap gMaps = new HashMap();
            for (int i = 0; i < groupMaps.size(); i++) {
                SubjectGroupMapBean groupMap = (SubjectGroupMapBean) groupMaps.get(i);
                gMaps.put(new Integer(groupMap.getStudyGroupClassId()), groupMap);

            }

            ArrayList classes = new ArrayList();
            if (!"submit".equalsIgnoreCase(action)) {
                // YW <<
                int parentStudyId = currentStudy.getParentStudyId();
                if (parentStudyId > 0) {
                    StudyBean parentStudy = (StudyBean) stdao.findByPK(parentStudyId);
                    classes = sgcdao.findAllActiveByStudy(parentStudy);
                } else {
                    classes = sgcdao.findAllActiveByStudy(currentStudy);
                }
                // YW >>
                for (int i = 0; i < classes.size(); i++) {
                    StudyGroupClassBean group = (StudyGroupClassBean) classes.get(i);
                    ArrayList studyGroups = sgdao.findAllByGroupClass(group);
                    group.setStudyGroups(studyGroups);
                    SubjectGroupMapBean gMap = (SubjectGroupMapBean) gMaps.get(new Integer(group.getId()));
                    if (gMap != null) {
                        group.setStudyGroupId(gMap.getStudyGroupId());
                        group.setGroupNotes(gMap.getNotes());
                    }
                }

                session.setAttribute("groups", classes);
            }

            if ("show".equalsIgnoreCase(action)) {

                session.setAttribute("studySub", studySub);
                session.setAttribute("subject", subject);

                List<DiscrepancyNoteBean> discNotes = getDiscNotesForSubjectStudySubject(study, subject.getId(), studySub.getId());
                setRequestAttributesForNotes(discNotes);


                formDiscNotes = new FormDiscrepancyNotes();
                session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, formDiscNotes);


                forwardPage(Page.UPDATE_STUDY_SUBJECT);
            } else if ("confirm".equalsIgnoreCase(action)) {
                session.setAttribute("subject", subject);
                List<DiscrepancyNoteBean> discNotes = getDiscNotesForSubjectStudySubject(study, subject.getId(), studySub.getId());
                setRequestAttributesForNotes(discNotes);
                updateIsValid = validateEditSubject(sgdao);

            }

            // This block commits the update to the DB, should be based on the if above.
            if (updateIsValid) {

                studySub.setUpdater(ub);

                // TODO remove these once we clear the study parameter config
                /*
                if (! currentStudy.getStudyParameterConfig().getCollectDob().equals("3")){
                    if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("2"))
                    {
                        String d_date = fp.getString(UpdateSubjectServlet.DATE_DOB_TO_SAVE);
                        if ( !(d_date == null || d_date.trim().length()==0)){
                            Date date_new = yformat.parse(fp.getString(UpdateSubjectServlet.DATE_DOB_TO_SAVE));
                            sub.setDateOfBirth(date_new);
                        }
                    }
                    if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("1"))
                    {

                        Date date_new = local_df.parse(fp.getString(UpdateSubjectServlet.DATE_DOB_TO_SAVE));
                        sub.setDateOfBirth(date_new);
                    }
                }
                */
                subject.setUpdater(ub);

                updateClosedQueriesForUpdatedStudySubjectFields(study, subject, studySub);
                studySubdao.update(studySub);
                sdao.update(subject);

                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
                AddNewSubjectServlet.saveFieldNotes("enrollmentDate", fdn, dndao, studySub.getId(), "studySub", currentStudy);

                ArrayList groups = (ArrayList) session.getAttribute("groups");
                if (!groups.isEmpty()) {
                    for (int i = 0; i < groups.size(); i++) {
                        StudyGroupClassBean sgc = (StudyGroupClassBean) groups.get(i);
                        /*We will be allowing users to remove a subject from all groups. Issue-4524*/
                        if (sgc.getStudyGroupId() == 0) {
                            Collection subjectGroups = sgmdao.findAllByStudySubject(studySub.getId());
                            for (Iterator it = subjectGroups.iterator(); it.hasNext(); ) {
                                sgmdao.deleteTestGroupMap(((SubjectGroupMapBean) it.next()).getId());
                            }
                        } else {
                            SubjectGroupMapBean sgm = new SubjectGroupMapBean();
                            SubjectGroupMapBean gMap = (SubjectGroupMapBean) gMaps.get(new Integer(sgc.getId()));
                            sgm.setStudyGroupId(sgc.getStudyGroupId());
                            sgm.setNotes(sgc.getGroupNotes());
                            sgm.setStudyGroupClassId(sgc.getId());
                            sgm.setStudySubjectId(studySub.getId());
                            sgm.setStatus(Status.AVAILABLE);
                            if (sgm.getStudyGroupId() > 0) {
                                if (gMap != null && gMap.getId() > 0) {
                                    sgm.setUpdater(ub);
                                    sgm.setId(gMap.getId());
                                    sgmdao.update(sgm);
                                } else {
                                    sgm.setOwner(ub);
                                    sgmdao.create(sgm);
                                }
                            }
                        }
                    }
                }

                addPageMessage(respage.getString("study_subject_updated_succesfully"));
                session.removeAttribute("studySub");
                session.removeAttribute("subject");
                session.removeAttribute("groups");
                session.removeAttribute("enrollDateStr");
                session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                request.setAttribute("id", new Integer(studySubId).toString());

                response.sendRedirect(request.getContextPath() + "/ViewStudySubject?id=" + new Integer(studySubId).toString());
            }

        }
    }

    private void updateClosedQueriesForUpdatedStudySubjectFields(StudyBean study, SubjectBean updatedSubject, StudySubjectBean updatedStudySubject) {
        DiscrepancyNoteDAO dnDAO = new DiscrepancyNoteDAO(sm.getDataSource());
        StudySubjectDAO studySubdao = new StudySubjectDAO(sm.getDataSource());
        SubjectDAO subdao = new SubjectDAO(sm.getDataSource());
        StudySubjectBean existingStudySubject = (StudySubjectBean) studySubdao.findByPK(updatedStudySubject.getId());
        SubjectBean existingSubject = (SubjectBean) subdao.findByPK(updatedSubject.getId());

        List<DiscrepancyNoteBean> discNotes = getDiscNotesForSubjectStudySubject(study, updatedStudySubject.getSubjectId(), updatedStudySubject.getId());

        for (DiscrepancyNoteBean note : discNotes) {
            if (note.getColumn().equals("enrollment_date") && note.getResStatus().equals(ResolutionStatus.CLOSED) &&
                    !existingStudySubject.getEnrollmentDate().equals(updatedStudySubject.getEnrollmentDate())) {
                note.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());
                DiscrepancyNoteBean childNote = createChildNote(note);
                dnDAO.create(childNote);
                dnDAO.createMapping(childNote);
                dnDAO.update(note);
            }

            if (note.getColumn().equals("unique_identifier") && note.getResStatus().equals(ResolutionStatus.CLOSED) &&
                    !existingSubject.getUniqueIdentifier().equals(updatedSubject.getUniqueIdentifier())) {
                note.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());
                DiscrepancyNoteBean childNote = createChildNote(note);
                dnDAO.create(childNote);
                dnDAO.createMapping(childNote);
                dnDAO.update(note);
            }

            if (note.getColumn().equals("gender") && note.getResStatus().equals(ResolutionStatus.CLOSED) &&
                    existingSubject.getGender() != updatedSubject.getGender()) {
                note.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());
                DiscrepancyNoteBean childNote = createChildNote(note);
                dnDAO.create(childNote);
                dnDAO.createMapping(childNote);
                dnDAO.update(note);
            }

            if (note.getColumn().equals("date_of_birth") && note.getResStatus().equals(ResolutionStatus.CLOSED) &&
                    !existingSubject.getDateOfBirth().equals(updatedSubject.getDateOfBirth())) {
                note.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());
                DiscrepancyNoteBean childNote = createChildNote(note);
                dnDAO.create(childNote);
                dnDAO.createMapping(childNote);
                dnDAO.update(note);
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
        child.setStudyId(currentStudy.getId());
        child.setEntityId(parent.getEntityId());
        child.setEntityType(parent.getEntityType());
        child.setColumn(parent.getColumn());
        child.setField(parent.getField());

        return child;
    }

    // Due to a change in page flow, this method mostly handles validation checks.
    private boolean validateEditSubject(StudyGroupDAO sgdao) throws Exception {
        HashMap manualErrors = new HashMap();
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();

        DiscrepancyValidator validator = new DiscrepancyValidator(request, discNotes);
        FormProcessor fp = new FormProcessor(request);

        int studySubId = Integer.valueOf(fp.getString("id"));

        // Update: allow data entry person role to edit subject on study level (https://jira.openclinica.com/browse/OC-8620)
        if (ub.isSysAdmin() || currentRole.isManageStudy() || currentRole.isInvestigator() || currentRole.isResearchAssistant() || currentStudy.getParentStudyId() > 0 && currentRole.isResearchAssistant2()) {

            validator.addValidation("label", Validator.NO_BLANKS);
            validator.addValidation("label", Validator.DOES_NOT_CONTAIN_HTML_LESSTHAN_GREATERTHAN_ELEMENTS);
            validator.addValidation("label", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 30);

            if (!StringUtil.isBlank(fp.getString("label"))) {
                StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());

                StudySubjectBean sub1 = (StudySubjectBean) ssdao.findAnotherBySameLabel(fp.getString("label").trim(), currentStudy.getId(), studySub.getId());
                if (sub1.getId() == 0) {
                    sub1 = (StudySubjectBean) ssdao.findAnotherBySameLabelInSites(fp.getString("label").trim(), currentStudy.getId(), studySub.getId());
                }
                if (sub1.getId() > 0) {
                    Validator.addError(manualErrors, "label", resexception.getString("subject_ID_used_by_another_choose_unique"));
                }
            }

            studySub.setLabel(fp.getString("label").trim());
            subject.setGender(' ');
            errors = validator.validate();
            addMultipleErrors(manualErrors);

            session.setAttribute("studySub", studySub);
            session.setAttribute("subject", subject);

            if (!errors.isEmpty()) {
                logger.info("has errors");
                if (StringUtil.isBlank(studySub.getLabel())) {
                    addPageMessage(respage.getString("must_enter_subject_ID_for_identifying") + respage.getString("this_may_be_external_ID_number")
                            + respage.getString("you_may_enter_study_subject_ID_listed")
                            + respage.getString("study_subject_ID_should_not_contain_protected_information"));
                } else {
                    StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
                    StudySubjectBean sub1 = (StudySubjectBean) subdao.findAnotherBySameLabel(studySub.getLabel(), studySub.getStudyId(), studySub.getId());
                    if (sub1.getId() > 0) {
                        addPageMessage(resexception.getString("subject_ID_used_by_another_choose_unique"));
                    }
                }

                addPageMessage(respage.getString("there_were_some_errors_submission"));

                setInputMessages(errors);
                fp.addPresetValue("label", fp.getString("label"));
                setPresetValues(fp.getPresetValues());

                Object isSubjectOverlay = fp.getRequest().getParameter("subjectOverlay");
                request.setAttribute("showOverlay", true);
                request.setAttribute("subjectId", fp.getString("label"));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);

                return false;

            } else {
                return true;
            }
        }
        addPageMessage(resexception.getString("no_permission"));
        return false;
    }


    private void setLocalDOB(SubjectBean subject) {
        Date birthDate = subject.getDateOfBirth();
        try {
            if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
                String localBirthDate = local_df.format(birthDate);
                request.setAttribute("localBirthDate", localBirthDate);
            } else if (currentStudy.getStudyParameterConfig().getCollectDob().equals("2")) {
                String localBirthDate = yformat.format(birthDate);
                request.setAttribute("localBirthDate", localBirthDate);
            }
        } catch (NullPointerException e) {
            logger.debug("update subject: cannot convert date " + birthDate);
        }
    }

    private List<DiscrepancyNoteBean> getDiscNotesForSubjectStudySubject(StudyBean study, Integer subjectId, Integer studySubId) {
        // If the study subject derives from a site, and is being viewed from a parent study,
        // then the study IDs will be different. However, since each note is
        // saved with the specific study ID, then its study ID may be different than the study
        // subject's ID.
        boolean subjectStudyIsCurrentStudy = study.getId() == currentStudy.getId();
        boolean isParentStudy = study.getParentStudyId() < 1;

        // Get any disc notes for this subject : studySubId
        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(sm.getDataSource());
        List<DiscrepancyNoteBean> allNotesforSubject = new ArrayList<DiscrepancyNoteBean>();

        // These methods return only parent disc notes
        if (subjectStudyIsCurrentStudy && isParentStudy) {
            allNotesforSubject = discrepancyNoteDAO.findAllSubjectByStudyAndId(study, subjectId);
            allNotesforSubject.addAll(discrepancyNoteDAO.findAllStudySubjectByStudyAndId(study, studySubId));
        } else {
            if (!isParentStudy) {
                StudyDAO studydao = new StudyDAO(sm.getDataSource());
                StudyBean stParent = (StudyBean) studydao.findByPK(study.getParentStudyId());
                allNotesforSubject = discrepancyNoteDAO.findAllSubjectByStudiesAndSubjectId(stParent, study, subjectId);
                allNotesforSubject.addAll(discrepancyNoteDAO.findAllStudySubjectByStudiesAndStudySubjectId(stParent, study, studySubId));
            } else {
                allNotesforSubject = discrepancyNoteDAO.findAllSubjectByStudiesAndSubjectId(currentStudy, study, subjectId);
                allNotesforSubject.addAll(discrepancyNoteDAO.findAllStudySubjectByStudiesAndStudySubjectId(currentStudy, study, studySubId));
            }
        }
        return allNotesforSubject;
    }

    private void setRequestAttributesForNotes(List<DiscrepancyNoteBean> discBeans) {
        for (DiscrepancyNoteBean discrepancyNoteBean : discBeans) {
            if ("unique_identifier".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(ViewStudySubjectServlet.HAS_UNIQUE_ID_NOTE, "yes");
                request.setAttribute(ViewStudySubjectServlet.UNIQUE_ID_NOTE, discrepancyNoteBean);
            } else if ("date_of_birth".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(ViewStudySubjectServlet.HAS_DOB_NOTE, "yes");
                request.setAttribute(ViewStudySubjectServlet.DOB_NOTE, discrepancyNoteBean);
            } else if ("enrollment_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(ViewStudySubjectServlet.HAS_ENROLLMENT_NOTE, "yes");
                request.setAttribute(ViewStudySubjectServlet.ENROLLMENT_NOTE, discrepancyNoteBean);
            } else if ("gender".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(ViewStudySubjectServlet.HAS_GENDER_NOTE, "yes");
                request.setAttribute(ViewStudySubjectServlet.GENDER_NOTE, discrepancyNoteBean);
            }

        }

    }

    private void addMultipleErrors(Map<String, List<String>> newErrors) {
        for (String newField : newErrors.keySet()) {
            List<String> newFieldErrors = (List<String>) newErrors.get(newField);
            List<String> fieldErrors;
            if (errors.containsKey(newField)) {
                fieldErrors = (List<String>) errors.get(newField);
            } else {
                fieldErrors = new ArrayList<String>();
            }
            for (String newFieldError : newFieldErrors) {
                fieldErrors.add(newFieldError);
            }
            errors.put(newField, fieldErrors);
        }
    }


}

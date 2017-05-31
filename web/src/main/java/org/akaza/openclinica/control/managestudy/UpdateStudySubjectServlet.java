/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu Processes request to update a study subject
 */
public class UpdateStudySubjectServlet extends SecureController {
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
        FormDiscrepancyNotes discNotes = null;
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);

        String fromResolvingNotes = fp.getString("fromResolvingNotes",true);
        if (StringUtil.isBlank(fromResolvingNotes)) {
            session.removeAttribute(ViewNotesServlet.WIN_LOCATION);
            session.removeAttribute(ViewNotesServlet.NOTES_TABLE);
            checkStudyLocked(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_locked"));
            checkStudyFrozen(Page.LIST_STUDY_SUBJECTS_SERVLET, respage.getString("current_study_frozen"));
        }

        int studySubId = fp.getInt("id", true);// studySubjectId

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

            StudySubjectBean sub = (StudySubjectBean) subdao.findByPK(studySubId);

            StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
            SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
            ArrayList groupMaps = (ArrayList) sgmdao.findAllByStudySubject(studySubId);

            HashMap gMaps = new HashMap();
            for (int i = 0; i < groupMaps.size(); i++) {
                SubjectGroupMapBean groupMap = (SubjectGroupMapBean) groupMaps.get(i);
                gMaps.put(new Integer(groupMap.getStudyGroupClassId()), groupMap);

            }

            StudyDAO stdao = new StudyDAO(sm.getDataSource());
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

                session.setAttribute("studySub", sub);
                // below added tbh 092007
                String enrollDateStr = sub.getEnrollmentDate()!=null ?
                    local_df.format(sub.getEnrollmentDate()) : "";
                session.setAttribute("enrollDateStr", enrollDateStr);
                // above added tbh 092007
                discNotes = new FormDiscrepancyNotes();
                session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

                forwardPage(Page.UPDATE_STUDY_SUBJECT);
            } else if ("confirm".equalsIgnoreCase(action)) {
                confirm(sgdao);

            } else if ("submit".equalsIgnoreCase(action)) {// submit to DB
                StudySubjectBean subject = (StudySubjectBean) session.getAttribute("studySub");
                subject.setUpdater(ub);
                subdao.update(subject);

                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
                AddNewSubjectServlet.saveFieldNotes("enrollmentDate", fdn, dndao, subject.getId(), "studySub", currentStudy);

                ArrayList groups = (ArrayList) session.getAttribute("groups");
                if (!groups.isEmpty()) {
                    for (int i = 0; i < groups.size(); i++) {
                        StudyGroupClassBean sgc = (StudyGroupClassBean) groups.get(i);
                        /*We will be allowing users to remove a subject from all groups. Issue-4524*/
                        if (sgc.getStudyGroupId() == 0) {
                            Collection subjectGroups = sgmdao.findAllByStudySubject(subject.getId());
                            for (Iterator it = subjectGroups.iterator(); it.hasNext();) {
                                sgmdao.deleteTestGroupMap(((SubjectGroupMapBean)it.next()).getId());
                            }
                        } else {
                            SubjectGroupMapBean sgm = new SubjectGroupMapBean();
                            SubjectGroupMapBean gMap = (SubjectGroupMapBean) gMaps.get(new Integer(sgc.getId()));
                            sgm.setStudyGroupId(sgc.getStudyGroupId());
                            sgm.setNotes(sgc.getGroupNotes());
                            sgm.setStudyGroupClassId(sgc.getId());
                            sgm.setStudySubjectId(subject.getId());
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
                session.removeAttribute("groups");
                session.removeAttribute("enrollDateStr");
                session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                request.setAttribute("id", new Integer(studySubId).toString());

                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            } else {
                addPageMessage(respage.getString("no_action_specified"));
                forwardPage(Page.LIST_STUDY_SUBJECTS);
                return;
            }

        }
    }

    /**
     * Processes 'confirm' request, validate the study subject object
     *
     * @param sub
     * @throws Exception
     */
    private void confirm(StudyGroupDAO sgdao) throws Exception {
        ArrayList classes = (ArrayList) session.getAttribute("groups");
        StudySubjectBean sub = (StudySubjectBean) session.getAttribute("studySub");
        FormDiscrepancyNotes discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
        DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);
        FormProcessor fp = new FormProcessor(request);
        java.util.Date enrollDate = sub.getEnrollmentDate();

        if (ub.isSysAdmin() || currentRole.isManageStudy() || currentRole.isInvestigator() || currentStudy.getParentStudyId() > 0 && currentRole.isResearchAssistant() || currentStudy.getParentStudyId() > 0 && currentRole.isResearchAssistant2()){
            //currentRole.getRoleName().equals(Role.STUDYDIRECTOR) || currentRole.getRoleName().equals(Role.COORDINATOR)) {

            v.addValidation("label", Validator.NO_BLANKS);
            v.addValidation("label", Validator.DOES_NOT_CONTAIN_HTML_LESSTHAN_GREATERTHAN_ELEMENTS);
            v.addValidation("secondaryLabel", Validator.DOES_NOT_CONTAIN_HTML_LESSTHAN_GREATERTHAN_ELEMENTS);

            String eDateString = fp.getString("enrollmentDate");
            if (!StringUtil.isBlank(eDateString)) {
                v.addValidation("enrollmentDate", Validator.IS_A_DATE);
                v.alwaysExecuteLastValidation("enrollmentDate");
            }

            errors = v.validate();

            if (!StringUtil.isBlank(fp.getString("label"))) {
                StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());

                StudySubjectBean sub1 = (StudySubjectBean) ssdao.findAnotherBySameLabel(fp.getString("label").trim(), currentStudy.getId(), sub.getId());

                // JRWS>> Also look for labels in the child studies
                if (sub1.getId() == 0) {
                    sub1 = (StudySubjectBean) ssdao.findAnotherBySameLabelInSites(fp.getString("label").trim(), currentStudy.getId(), sub.getId());
                }

                if (sub1.getId() > 0) {
                    Validator.addError(errors, "label", resexception.getString("subject_ID_used_by_another_choose_unique"));
                }
            }

            sub.setLabel(fp.getString("label"));
            sub.setSecondaryLabel(fp.getString("secondaryLabel"));

            try {
                local_df.setLenient(false);
                if (!StringUtil.isBlank(eDateString)) {
                    enrollDate = local_df.parse(eDateString);
                } else {
                    enrollDate = null;
                }
            } catch (ParseException fe) {
                logger.warn("Enrollment Date cannot be parsed.");
            }
            sub.setEnrollmentDate(enrollDate);

        }

        // below added tbh 092007, fix for YY vs YYYY formatting
        String enrollDateStr = enrollDate != null ? local_df.format(enrollDate) : "";

        session.setAttribute("enrollDateStr", enrollDateStr);
        // above added tbh 092007
        session.setAttribute("studySub", sub);

        if (!classes.isEmpty()) {
            for (int i = 0; i < classes.size(); i++) {
                StudyGroupClassBean sgc = (StudyGroupClassBean) classes.get(i);
                int groupId = fp.getInt("studyGroupId" + i);
                String notes = fp.getString("notes" + i);
                v.addValidation("notes" + i, Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
                sgc.setStudyGroupId(groupId);
                sgc.setGroupNotes(notes);
                if (groupId > 0) {
                    StudyGroupBean sgb = (StudyGroupBean) sgdao.findByPK(groupId);
                    sgc.setStudyGroupName(sgb.getName());
                }
            }
        }
        session.setAttribute("groups", classes);
        if (!errors.isEmpty()) {
            logger.info("has errors");
            if (StringUtil.isBlank(sub.getLabel())) {
                addPageMessage(respage.getString("must_enter_subject_ID_for_identifying") + respage.getString("this_may_be_external_ID_number")
                    + respage.getString("you_may_enter_study_subject_ID_listed")
                    + respage.getString("study_subject_ID_should_not_contain_protected_information"));
            } else {
                StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
                StudySubjectBean sub1 = (StudySubjectBean) subdao.findAnotherBySameLabel(sub.getLabel(), sub.getStudyId(), sub.getId());
                if (sub1.getId() > 0) {
                    addPageMessage(resexception.getString("subject_ID_used_by_another_choose_unique"));
                }
            }

            request.setAttribute("formMessages", errors);
            forwardPage(Page.UPDATE_STUDY_SUBJECT);

        } else {
            forwardPage(Page.UPDATE_STUDY_SUBJECT_CONFIRM);
        }

    }

}

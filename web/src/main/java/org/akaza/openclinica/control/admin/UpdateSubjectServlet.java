/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplaySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.managestudy.ViewNotesServlet;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author jxu
 *
 */
public class UpdateSubjectServlet extends SecureController {
   
    public static final String YEAR_DOB = "yearOfBirth";

    public static final String DATE_DOB = "dateOfBirth";

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SUBJECT_LIST_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        FormDiscrepancyNotes discNotes = null;

        String fromResolvingNotes = fp.getString("fromResolvingNotes",true);
        if (StringUtil.isBlank(fromResolvingNotes)) {
            session.removeAttribute(ViewNotesServlet.WIN_LOCATION);
            session.removeAttribute(ViewNotesServlet.NOTES_TABLE);
            checkStudyLocked(Page.LIST_SUBJECT_SERVLET, respage.getString("current_study_locked"));
            checkStudyFrozen(Page.LIST_SUBJECT_SERVLET, respage.getString("current_study_frozen"));
        }


        int subjectId = fp.getInt("id", true);
        int studySubId = fp.getInt("studySubId", true);

        if (subjectId == 0) {
            addPageMessage(respage.getString("please_choose_subject_to_edit"));
            forwardPage(Page.LIST_SUBJECT_SERVLET);
        } else {

            String action = fp.getString("action", true);

            if (StringUtil.isBlank("action")) {
                addPageMessage(respage.getString("no_action_specified"));
                forwardPage(Page.LIST_SUBJECT_SERVLET);
                return;
            }
            SubjectBean sub = (SubjectBean) sdao.findByPK(subjectId);

            request.setAttribute("studySubId", new Integer(studySubId));
           
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            StudyDAO stdao = new StudyDAO(sm.getDataSource());

      
            if (!sub.isDobCollected()) {
                Date dob = sub.getDateOfBirth();
                Calendar cal = Calendar.getInstance();
                int year = 0;
                if (dob != null) {
                    cal.setTime(dob);
                    year = cal.get(Calendar.YEAR);
                   // request.setAttribute(YEAR_DOB, new Integer(year));
                } else {
                    //request.setAttribute(DATE_DOB, "");
                }
            }
            if ("show".equalsIgnoreCase(action)) {
                session.setAttribute("subjectToUpdate", sub);
                // tbh
                Date birthDate = sub.getDateOfBirth();
                try {
                    String localBirthDate = local_df.format(birthDate);
                    session.setAttribute("localBirthDate", localBirthDate);
                } catch (NullPointerException e) {
                    // TODO Auto-generated catch block
                    logger.info("***** found a NPE on birthday " + sub.getName());
                    e.printStackTrace();
                }
                // added 102007, tbh
          
                discNotes = new FormDiscrepancyNotes();
                session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

              
                int flagRStatusId = dndao.getResolutionStatusIdForSubjectDNFlag(subjectId, "gender");
                if(flagRStatusId > 0) {
                    session.setAttribute("genderDNFlag",AbstractTableFactory.getDNFlagIconName(flagRStatusId));
                }else {
                    session.setAttribute("genderDNFlag","icon_noNote");
                }
                flagRStatusId = dndao.getResolutionStatusIdForSubjectDNFlag(subjectId, "date_of_birth");
                if(flagRStatusId > 0) {
                    session.setAttribute("birthDNFlag",AbstractTableFactory.getDNFlagIconName(flagRStatusId));
                }else {
                    session.setAttribute("birthDNFlag","icon_noNote");
                }

                forwardPage(Page.UPDATE_SUBJECT);
            } else if ("confirm".equalsIgnoreCase(action)) {
                    confirm();
            } else {
                SubjectBean subject = (SubjectBean) session.getAttribute("subjectToUpdate");
                subject.setUpdater(ub);
                sdao.update(subject);

                // save discrepancy notes into DB
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                AddNewSubjectServlet.saveFieldNotes("gender", fdn, dndao, subject.getId(), "subject", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(YEAR_DOB, fdn, dndao, subject.getId(), "subject", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(DATE_DOB, fdn, dndao, subject.getId(), "subject", currentStudy);

                session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                addPageMessage(respage.getString("subject_updated_succcesfully"));
                session.removeAttribute("subjectToUpdate");
                session.removeAttribute("genderDNFlag");
                session.removeAttribute("birthDNFlag");
                if (studySubId > 0) {
                    request.setAttribute("id", new Integer(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                } else {
                    forwardPage(Page.LIST_SUBJECT_SERVLET);
                }
            }

        }
    }

    /**
     * Processes 'confirm' request, validate the subject object
     *
     * @throws Exception
     */
    private void confirm() throws Exception {
        SubjectBean sub = (SubjectBean) session.getAttribute("subjectToUpdate");
        FormDiscrepancyNotes discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
        DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation("uniqueIdentifier", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.alwaysExecuteLastValidation("uniqueIdentifier");

        // tbh

        // tbh
        if (!sub.isDobCollected()) {
            if (!StringUtil.isBlank(fp.getString(YEAR_DOB))) {
                v.addValidation(YEAR_DOB, Validator.IS_AN_INTEGER);
                v.alwaysExecuteLastValidation(YEAR_DOB);
            }
            // if the original DOB is null, but user entered a new DOB
            if (!StringUtil.isBlank(fp.getString(DATE_DOB))) {
                v.addValidation(DATE_DOB, Validator.IS_A_DATE);
                v.alwaysExecuteLastValidation(DATE_DOB);
            }
        } else {
            if (!StringUtil.isBlank(fp.getString(DATE_DOB))) {
                v.addValidation(DATE_DOB, Validator.IS_A_DATE);
                v.alwaysExecuteLastValidation(DATE_DOB);
            }

        }

        errors = v.validate();

        // uniqueIdentifier must be unique in the system
        if (!StringUtil.isBlank(fp.getString("uniqueIdentifier"))) {
            SubjectDAO sdao = new SubjectDAO(sm.getDataSource());

            SubjectBean sub1 = (SubjectBean) sdao.findAnotherByIdentifier(fp.getString("uniqueIdentifier").trim(), sub.getId());
            // tbh
            logger.info("checking unique identifier: " + sub.getUniqueIdentifier() + " and " + fp.getString("uniqueIdentifier").trim());
            // tbh
            if (sub1.getId() > 0) {
                Validator.addError(errors, "uniqueIdentifier", resexception.getString("person_ID_used_by_another_choose_unique"));
            }
        }

        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        StudyDAO stdao = new StudyDAO(sm.getDataSource());
       
        String uniqueIdentifier = fp.getString("uniqueIdentifier");
        if (!StringUtil.isBlank(uniqueIdentifier)) {
            SubjectBean subjectWithSameId = sdao.findByUniqueIdentifier(uniqueIdentifier);
            if (subjectWithSameId.isActive() && subjectWithSameId.getId() != sub.getId()) {
                Validator.addError(errors, "uniqueIdentifier", resexception.getString("another_assigned_this_ID_choose_unique"));
            }
        }

       

        boolean newDobInput = false;
        if (!sub.isDobCollected()) {
            if (!StringUtil.isBlank(fp.getString(YEAR_DOB))) {
                int yob = fp.getInt(YEAR_DOB);

                v.addValidation(YEAR_DOB, Validator.IS_AN_INTEGER);
                v.alwaysExecuteLastValidation(YEAR_DOB);

                v.addValidation(YEAR_DOB, Validator.COMPARES_TO_STATIC_VALUE, NumericComparisonOperator.GREATER_THAN_OR_EQUAL_TO, 1000);
                v.addValidation(YEAR_DOB, Validator.COMPARES_TO_STATIC_VALUE, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 9999);

                String dobString = "01/01/" + yob;
                try {
                    Date fakeDOB = local_df.parse(dobString);
                    sub.setDateOfBirth(fakeDOB);
                } catch (ParseException pe) {
                    logger.info("Parse exception happened.");
                    Validator.addError(errors, YEAR_DOB, resexception.getString("please_enter_a_valid_year_birth"));
                }
                request.setAttribute(YEAR_DOB, fp.getString(YEAR_DOB));
            } else if (!StringUtil.isBlank(fp.getString(DATE_DOB))) {
                // DOB is null orginally, and user entered a new DOB
                v.addValidation(DATE_DOB, Validator.IS_A_DATE);
                v.alwaysExecuteLastValidation(DATE_DOB);
                request.setAttribute(DATE_DOB, fp.getString(DATE_DOB));
                newDobInput = true;
                sub.setDateOfBirth(fp.getDate(DATE_DOB));

            } else {
                sub.setDateOfBirth(null);

            }
        } else {
            if(StringUtil.isBlank(fp.getString(DATE_DOB))) {
                sub.setDateOfBirth(null);
            } else {
                sub.setDateOfBirth(fp.getDate(DATE_DOB));
            }
        }

        if (!StringUtil.isBlank(fp.getString("gender"))) {
            sub.setGender(fp.getString("gender").charAt(0));
        } else {
            sub.setGender(' ');
        }
        sub.setUniqueIdentifier(uniqueIdentifier);
        session.setAttribute("subjectToUpdate", sub);

        if (errors.isEmpty()) {
            logger.info("no errors");

           
            if (newDobInput) {
                sub.setDobCollected(true);
            }
            forwardPage(Page.UPDATE_SUBJECT_CONFIRM);
        } else {
            logger.info("validation errors");
            request.setAttribute(YEAR_DOB, fp.getString(YEAR_DOB));
            setInputMessages(errors);
            forwardPage(Page.UPDATE_SUBJECT);
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

}

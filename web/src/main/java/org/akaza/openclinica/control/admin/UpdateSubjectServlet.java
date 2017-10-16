/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
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
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringUtils;

/**
 * @author jxu
 *
 */
public class UpdateSubjectServlet extends SecureController {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Changes
    SimpleDateFormat yformat = new SimpleDateFormat("yyyy");
    
    public static final String INPUT_UNIQUE_IDENTIFIER = "uniqueIdentifier";// global
    public static final String DATE_DOB = "localBirthDate";
    public static final String DATE_DOB_TO_SAVE = "localBirthDateToSave";

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
        FormProcessor fp = new FormProcessor(request);
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();

        
        String fromResolvingNotes = fp.getString("fromResolvingNotes",true);
        if (StringUtils.isBlank(fromResolvingNotes)) {
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

            if (StringUtils.isBlank("action")) {
                addPageMessage(respage.getString("no_action_specified"));
                forwardPage(Page.LIST_SUBJECT_SERVLET);
                return;
            }
            SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);
             
            if (action.equals("show") || action.equals("confirm") ){
                request.setAttribute("studySubId", new Integer(studySubId));
                request.setAttribute("id", new Integer(subjectId));
                 request.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
            }
            if ("show".equalsIgnoreCase(action)) {
                
                 request.setAttribute("localBirthDate", "");//no DOB collected
                if (!currentStudy.getStudyParameterConfig().getCollectDob().equals("3") &&
                        subject.getDateOfBirth() != null ){
                    setLocalDOB( subject);
                }
               
                discNotes = new FormDiscrepancyNotes();
               
                request.setAttribute("genderDNFlag","icon_noNote");
                request.setAttribute("birthDNFlag","icon_noNote");
                request.setAttribute("subjectToUpdate",subject);
                setDNFlag( subjectId);
                
                forwardPage(Page.UPDATE_SUBJECT);
            } else if ("confirm".equalsIgnoreCase(action)) {
               
                confirm(subject,subjectId);
                
            } else {
                String gender = fp.getString("gender");
                subject.setGender(gender.charAt(0));
                if (currentStudy.getStudyParameterConfig().getSubjectPersonIdRequired().equals("required")
                        || currentStudy.getStudyParameterConfig().getSubjectPersonIdRequired().equals("optional")){

                    subject.setUniqueIdentifier(fp.getString("uniqueIdentifier"));
                }
                subject.setUpdater(ub);
                if (! currentStudy.getStudyParameterConfig().getCollectDob().equals("3")){
                    if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("2"))
                    {
                        String d_date = fp.getString(DATE_DOB_TO_SAVE);
                        if ( !(d_date == null || d_date.trim().length()==0)){
                            Date date_new = yformat.parse(fp.getString(DATE_DOB_TO_SAVE));
                            subject.setDateOfBirth(date_new);
                        }
                    }
                    if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("1"))
                    {
                        Date date_new = local_df.parse(fp.getString(DATE_DOB_TO_SAVE));
                        subject.setDateOfBirth(date_new);
                    }
                }
                
                
                sdao.update(subject);

                // save discrepancy notes into DB
                DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
                
                FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
                AddNewSubjectServlet.saveFieldNotes("gender", fdn, dndao, subject.getId(), "subject", currentStudy);
                AddNewSubjectServlet.saveFieldNotes(DATE_DOB, fdn, dndao, subject.getId(), "subject", currentStudy);

                addPageMessage(respage.getString("subject_updated_succcesfully"));
                
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
    private void confirm(SubjectBean subject, int subjectId) throws Exception {
        FormDiscrepancyNotes discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
        if (discNotes == null) {
            discNotes = new FormDiscrepancyNotes();
        }
        DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);
        FormProcessor fp = new FormProcessor(request);

//        v.addValidation("uniqueIdentifier", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
//        v.alwaysExecuteLastValidation("uniqueIdentifier");

        if (currentStudy.getStudyParameterConfig().getPersonIdShownOnCRF().equals("true")){
            v.addValidation("uniqueIdentifier", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
            v.alwaysExecuteLastValidation("uniqueIdentifier");
         }
        
        String personId = fp.getString(INPUT_UNIQUE_IDENTIFIER);
        if (personId.contains("<") || personId.contains(">")) {
            v.addValidation("uniqueIdentifier", Validator.DOES_NOT_CONTAIN_HTML_LESSTHAN_GREATERTHAN_ELEMENTS);
        }

        
        if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("1")){
            if (!StringUtil.isBlank(fp.getString(DATE_DOB))) {
                v.addValidation(DATE_DOB, Validator.IS_A_DATE);
                v.alwaysExecuteLastValidation(DATE_DOB);
            }
            else if (StringUtil.isBlank(fp.getString(DATE_DOB)) && subject.getDateOfBirth()!= null){
                Validator.addError(errors, DATE_DOB, resexception.getString("field_not_blank"));
            }
            if ( fp.getDate(DATE_DOB) != null){
                subject.setDateOfBirth(fp.getDate(DATE_DOB));
                String  converted_date = local_df.format(subject.getDateOfBirth());
                request.setAttribute(DATE_DOB_TO_SAVE, converted_date);    
            }
            
        }
        
        else if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("2")){
            if (!StringUtils.isBlank(fp.getString(DATE_DOB))) {
               
                // if DOB was not updated (and originally entered as a full day, post it as is
                String submitted_date = fp.getString(DATE_DOB);
                
                
                boolean isTheSameDate = false;
                try{
                    Date fakeDOB = yformat.parse(submitted_date);
                    if(subject.getDateOfBirth() != null)
                    {
                    if (subject.getDateOfBirth().getYear() == (fakeDOB.getYear())){
                        isTheSameDate=true;
                        String  converted_date = yformat.format(subject.getDateOfBirth());
                        request.setAttribute(DATE_DOB_TO_SAVE, converted_date);
                     }
                    }
                }catch(ParseException pe){
                    logger.debug("update subject: cannot convert date " + submitted_date);
                    //I am putting on Pradnya's request the link to code review with a long discussion
                    //about what type of logging should be here: enjoy
                    //https://dev.openclinica.com/crucible/cru/OC-117
                }
                
                if ( !isTheSameDate){
                      
                      v.addValidation(DATE_DOB, Validator.IS_AN_INTEGER);
                      v.alwaysExecuteLastValidation(DATE_DOB);
                      v.addValidation(DATE_DOB, Validator.COMPARES_TO_STATIC_VALUE, NumericComparisonOperator.GREATER_THAN_OR_EQUAL_TO, 1000);

                    // get today's year
                    Date today = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(today);
                    int currentYear = c.get(Calendar.YEAR);
                    v.addValidation(DATE_DOB, Validator.COMPARES_TO_STATIC_VALUE, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, currentYear);
                    int yob = fp.getInt(DATE_DOB);
                    Date fakeDate = new Date(yob);
                    String dobString = yformat.format(fakeDate);
                    try {
                        
                        Date fakeDOB = yformat.parse(dobString);
                        if (yob != 0){subject.setDateOfBirth(fakeDOB);}
                        request.setAttribute(DATE_DOB_TO_SAVE, yob);    
                    } catch (ParseException pe) {
                        logger.debug("Parse exception happened.");
                      //I am putting on Pradnya's request the link to code review with a long discussion
                        //about what type of logging should be here: enjoy
                        //https://dev.openclinica.com/crucible/cru/OC-117
                        Validator.addError(errors, DATE_DOB, resexception.getString("please_enter_a_valid_year_birth"));
                    }
                }
                request.setAttribute(DATE_DOB, fp.getString(DATE_DOB));    
                
            }
            else{
                Validator.addError(errors, DATE_DOB, resexception.getString("field_not_blank"));
            }
        }
       
         

        errors = v.validate();

        // uniqueIdentifier must be unique in the system
        if (currentStudy.getStudyParameterConfig().getSubjectPersonIdRequired().equals("required")
                || currentStudy.getStudyParameterConfig().getSubjectPersonIdRequired().equals("optional")){

            String uniqueIdentifier = fp.getString("uniqueIdentifier");
            if (currentStudy.getStudyParameterConfig().getSubjectPersonIdRequired().equals("required") &&
                    !(subject.getUniqueIdentifier() == null || subject.getUniqueIdentifier().isEmpty() ) &&
                    (uniqueIdentifier == null || uniqueIdentifier.isEmpty())) {
                Validator.addError(errors, "uniqueIdentifier", resexception.getString("field_not_blank"));
                
            }
             if (uniqueIdentifier != null && !uniqueIdentifier.isEmpty()) {
                
                 
                if ( uniqueIdentifier.length() > 255){
                      String descr =  resexception.getString("input_provided_is_not") +  NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO.getDescription() + " 255 " 
                                 + resword.getString("characters_long") + ".";
                    Validator.addError(errors, "uniqueIdentifier", descr);
                    
                }
                SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
                SubjectBean sub1 = (SubjectBean) sdao.findAnotherByIdentifier(uniqueIdentifier, subject.getId());
                if (sub1.getId() > 0) {
                      Validator.addError(errors, "uniqueIdentifier", resexception.getString("person_ID_used_by_another_choose_unique"));
                }
                SubjectBean subjectWithSameId = sdao.findByUniqueIdentifier(uniqueIdentifier);
                if (subjectWithSameId.isActive() && subjectWithSameId.getId() != subject.getId()) {
                     Validator.addError(errors, "uniqueIdentifier", resexception.getString("another_assigned_this_ID_choose_unique"));
                    
                }
            }
             subject.setUniqueIdentifier(uniqueIdentifier);
        }
       

        if (!StringUtil.isBlank(fp.getString("gender"))) {
            subject.setGender(fp.getString("gender").charAt(0));
        } else {
            if (currentStudy.getStudyParameterConfig().getGenderRequired().equals("true") && subject.getGender() !=  ' '){
                Validator.addError(errors, "gender", resexception.getString("field_not_blank"));
            }
            subject.setGender(' ');
        }
        
        
        request.setAttribute("subjectToUpdate",subject);
        if (errors.isEmpty()) {
            forwardPage(Page.UPDATE_SUBJECT_CONFIRM);
        } else {
            //I am putting on Pradnya's request the link to code review with a long discussion
            //about what type of logging should be here: enjoy
            //https://dev.openclinica.com/crucible/cru/OC-117
            logger.error("update subject validation errors");
            
            setInputMessages(errors);
            setDNFlag( subjectId);
            setLocalDOB( subject);
            if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("2"))
            request.setAttribute("localBirthDate", "");
            
            forwardPage(Page.UPDATE_SUBJECT);
        }
    }
    
    private void setDNFlag(int subjectId){
        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        
         request.setAttribute("genderDNFlag","icon_noNote");
         request.setAttribute("birthDNFlag","icon_noNote");
        
        int flagRStatusId = dndao.getResolutionStatusIdForSubjectDNFlag(subjectId, "gender");
        if(flagRStatusId > 0) {
            request.setAttribute("genderDNFlag",AbstractTableFactory.getDNFlagIconName(flagRStatusId));
        }
        flagRStatusId = dndao.getResolutionStatusIdForSubjectDNFlag(subjectId, "date_of_birth");
        if(flagRStatusId > 0) {
            request.setAttribute("birthDNFlag",AbstractTableFactory.getDNFlagIconName(flagRStatusId));
        }
    }
    
//    private void setLocalDOB(SubjectBean subject){
//        Date birthDate = subject.getDateOfBirth();
//        try {
//            String localBirthDate = yformat.format(birthDate);
//            request.setAttribute("localBirthDate", localBirthDate);
//        } catch (NullPointerException e) {
//            logger.debug("update subject: cannot convert date " + birthDate);
//        }
//    }    

    private void setLocalDOB(SubjectBean subject){
               
        Date birthDate = subject.getDateOfBirth();
        
        try 
        {
            if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("1"))
            {
                String localBirthDate = local_df.format(birthDate);
                request.setAttribute("localBirthDate", localBirthDate);
            }
            else if ( currentStudy.getStudyParameterConfig().getCollectDob().equals("2"))
            {
                String localBirthDate = yformat.format(birthDate);
                request.setAttribute("localBirthDate", localBirthDate);
            }
        }
        catch (NullPointerException e) 
        {
            logger.debug("update subject: cannot convert date " + birthDate);
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

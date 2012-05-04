/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.InterventionBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Updates a top-level study
 * 
 * @author jxu
 * @version CVS: $Id: UpdateStudyServlet.java 10902 2008-04-10 18:53:11Z
 *          kkrumlian $
 * 
 */
public class UpdateStudyServlet extends SecureController {
    public static final String INPUT_START_DATE = "startDate";
    public static final String INPUT_END_DATE = "endDate";
    public static final String INPUT_VER_DATE = "protocolDateVerification";

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.STUDY_LIST_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        panel.setExtractData(false);
        panel.setSubmitDataModule(false);
        panel.setCreateDataset(false);
        panel.setIconInfoShown(true);
        panel.setManageSubject(false);

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) session.getAttribute("newStudy");

        if (study == null) {
            addPageMessage(respage.getString("please_choose_a_study_to_edit"));
            forwardPage(Page.STUDY_LIST_SERVLET);
            return;
        }

        // whether the study is interventional
        String interventional = resadmin.getString("interventional");
        boolean isInterventional = interventional.equalsIgnoreCase(study.getProtocolType());

        String action = request.getParameter("action");

        if (StringUtil.isBlank(action)) {
            // request.setAttribute("facRecruitStatusMap",
            // CreateStudyServlet.facRecruitStatusMap);
            request.setAttribute("statuses", Status.toActiveArrayList());
            forwardPage(Page.UPDATE_STUDY1);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                confirmWholeStudy();

            } else if ("submit".equalsIgnoreCase(action)) {
                submitStudy();
                addPageMessage(respage.getString("the_study_has_been_updated_succesfully"));
                forwardPage(Page.STUDY_LIST_SERVLET);

            } else if ("next".equalsIgnoreCase(action)) {
                Integer pageNumber = Integer.valueOf(request.getParameter("pageNum"));
                if (pageNumber != null) {
                    if (pageNumber.intValue() == 6) {
                        confirmStudy6();
                    } else if (pageNumber.intValue() == 5) {
                        confirmStudy5();
                    } else if (pageNumber.intValue() == 4) {
                        confirmStudy4();
                    } else if (pageNumber.intValue() == 3) {
                        confirmStudy3(isInterventional);
                    } else if (pageNumber.intValue() == 2) {
                        confirmStudy2();
                    } else {
                        logger.info("confirm study 1" + pageNumber.intValue());
                        confirmStudy1();
                    }
                } else {
                    session.setAttribute("newStudy", study);
                    // request.setAttribute("facRecruitStatusMap",
                    // CreateStudyServlet.facRecruitStatusMap);
                    // request.setAttribute("statuses",
                    // Status.toActiveArrayList());
                    forwardPage(Page.UPDATE_STUDY1);
                }
            }
        }
    }

    /**
     * Validates the first section of study and save it into study bean
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    private void confirmStudy1() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("uniqueProId", Validator.NO_BLANKS);
        v.addValidation("description", Validator.NO_BLANKS);
        v.addValidation("prinInvestigator", Validator.NO_BLANKS);
        v.addValidation("sponsor", Validator.NO_BLANKS);

        v.addValidation("secondProId", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("collaborators", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);
        v.addValidation("protocolDescription", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);

        errors = v.validate();
        if (fp.getString("name").trim().length() > 100) {
            Validator.addError(errors, "name", resexception.getString("maximum_lenght_name_100"));
        }
        if (fp.getString("uniqueProId").trim().length() > 30) {
            Validator.addError(errors, "uniqueProId", resexception.getString("maximum_lenght_unique_protocol_30"));
        }
        if (fp.getString("description").trim().length() > 255) {
            Validator.addError(errors, "description", resexception.getString("maximum_lenght_brief_summary_255"));
        }
        if (fp.getString("prinInvestigator").trim().length() > 255) {
            Validator.addError(errors, "prinInvestigator", resexception.getString("maximum_lenght_principal_investigator_255"));
        }
        if (fp.getString("sponsor").trim().length() > 255) {
            Validator.addError(errors, "sponsor", resexception.getString("maximum_lenght_sponsor_255"));
        }
        if (fp.getString("officialTitle").trim().length() > 255) {
            Validator.addError(errors, "officialTitle", resexception.getString("maximum_lenght_official_title_255"));
        }
        session.setAttribute("newStudy", createStudyBean());

        if (errors.isEmpty()) {
            logger.info("no errors in the first section");
            request.setAttribute("studyPhaseMap", CreateStudyServlet.studyPhaseMap);
            request.setAttribute("statuses", Status.toActiveArrayList());
            StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
            fp.addPresetValue(INPUT_START_DATE, local_df.format(newStudy.getDatePlannedStart()));
            if (newStudy.getDatePlannedEnd() != null) {
                fp.addPresetValue(INPUT_END_DATE, local_df.format(newStudy.getDatePlannedEnd()));
            }
            fp.addPresetValue(INPUT_VER_DATE, local_df.format(newStudy.getProtocolDateVerification()));
            setPresetValues(fp.getPresetValues());
            forwardPage(Page.UPDATE_STUDY2);

        } else {
            logger.info("has validation errors in the first section");
            request.setAttribute("formMessages", errors);

            forwardPage(Page.UPDATE_STUDY1);
        }

    }

    private void confirmStudy2() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation(INPUT_START_DATE, Validator.IS_A_DATE);
        if (!StringUtil.isBlank(fp.getString(INPUT_END_DATE))) {
            v.addValidation(INPUT_END_DATE, Validator.IS_A_DATE);
        }
        v.addValidation(INPUT_VER_DATE, Validator.IS_A_DATE);

        errors = v.validate();
        boolean isInterventional = updateStudy2();

        if (errors.isEmpty()) {
            logger.info("no errors");
            ArrayList interventionArray = new ArrayList();
            if (isInterventional) {
                interventionArray = parseInterventions((StudyBean) session.getAttribute("newStudy"));
                setMaps(isInterventional, interventionArray);
                forwardPage(Page.UPDATE_STUDY3);
            } else {
                setMaps(isInterventional, interventionArray);
                forwardPage(Page.UPDATE_STUDY4);
            }

        } else {
            logger.info("has validation errors");
            try {
                local_df.parse(fp.getString(INPUT_START_DATE));
                fp.addPresetValue(INPUT_START_DATE, local_df.format(fp.getDate(INPUT_START_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
            }
            try {
                local_df.parse(fp.getString(INPUT_VER_DATE));
                fp.addPresetValue(INPUT_VER_DATE, local_df.format(fp.getDate(INPUT_VER_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
            }
            try {
                local_df.parse(fp.getString(INPUT_END_DATE));
                fp.addPresetValue(INPUT_END_DATE, local_df.format(fp.getDate(INPUT_END_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
            }
            setPresetValues(fp.getPresetValues());
            request.setAttribute("formMessages", errors);
            request.setAttribute("studyPhaseMap", CreateStudyServlet.studyPhaseMap);
            request.setAttribute("statuses", Status.toActiveArrayList());
            forwardPage(Page.UPDATE_STUDY2);
        }

    }

    /**
     * Confirms the third input block of study info
     * 
     * @throws Exception
     */
    private void confirmStudy3(boolean isInterventional) throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation("purpose", Validator.NO_BLANKS);
        for (int i = 0; i < 10; i++) {
            String type = fp.getString("interType" + i);
            String name = fp.getString("interName" + i);
            if (!StringUtil.isBlank(type) && StringUtil.isBlank(name)) {
                v.addValidation("interName", Validator.NO_BLANKS);
                request.setAttribute("interventionError", respage.getString("name_cannot_be_blank_if_type"));
                break;
            }
            if (!StringUtil.isBlank(name) && StringUtil.isBlank(type)) {
                v.addValidation("interType", Validator.NO_BLANKS);
                request.setAttribute("interventionError", respage.getString("name_cannot_be_blank_if_name"));
                break;
            }
        }

        errors = v.validate();
        updateStudy3(isInterventional);

        if (errors.isEmpty()) {
            logger.info("no errors");
            request.setAttribute("interventions", session.getAttribute("interventions"));
            forwardPage(Page.UPDATE_STUDY5);

        } else {
            logger.info("has validation errors");
            request.setAttribute("formMessages", errors);
            setMaps(isInterventional, (ArrayList) session.getAttribute("interventions"));
            if (isInterventional) {
                forwardPage(Page.UPDATE_STUDY3);
            } else {
                forwardPage(Page.UPDATE_STUDY4);
            }
        }
    }

    /**
     * Validates the forth section of study and save it into study bean
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    private void confirmStudy4() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        Validator v = new Validator(request);
        v.addValidation("conditions", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 500);
        v.addValidation("keywords", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("eligibility", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 500);
        errors = v.validate();
        if (fp.getInt("expectedTotalEnrollment") <= 0) {
            Validator.addError(errors, "expectedTotalEnrollment", respage.getString("expected_total_enrollment_must_be_a_positive_number"));
        }

        StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
        newStudy.setConditions(fp.getString("conditions"));
        newStudy.setKeywords(fp.getString("keywords"));
        newStudy.setEligibility(fp.getString("eligibility"));
        newStudy.setGender(fp.getString("gender"));

        newStudy.setAgeMax(fp.getString("ageMax"));
        newStudy.setAgeMin(fp.getString("ageMin"));
        newStudy.setHealthyVolunteerAccepted(fp.getBoolean("healthyVolunteerAccepted"));
        newStudy.setExpectedTotalEnrollment(fp.getInt("expectedTotalEnrollment"));
        session.setAttribute("newStudy", newStudy);
        request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
        if (errors.isEmpty()) {
            forwardPage(Page.UPDATE_STUDY6);
        } else {
            request.setAttribute("formMessages", errors);
            forwardPage(Page.UPDATE_STUDY5);
        }

    }

    /**
     * Validates the forth section of study and save it into study bean
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    private void confirmStudy5() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        Validator v = new Validator(request);
        if (!StringUtil.isBlank(fp.getString("facConEmail"))) {
            v.addValidation("facConEmail", Validator.IS_A_EMAIL);
        }
        v.addValidation("facName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facCity", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facState", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 20);
        v.addValidation("facZip", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
        v.addValidation("facCountry", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
        v.addValidation("facConName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facConDegree", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facConPhone", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facConEmail", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        errors = v.validate();

        StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");

        newStudy.setFacilityCity(fp.getString("facCity"));
        newStudy.setFacilityContactDegree(fp.getString("facConDrgree"));
        newStudy.setFacilityName(fp.getString("facName"));
        newStudy.setFacilityContactEmail(fp.getString("facConEmail"));
        newStudy.setFacilityContactPhone(fp.getString("facConPhone"));
        newStudy.setFacilityContactName(fp.getString("facConName"));
        newStudy.setFacilityCountry(fp.getString("facCountry"));
        newStudy.setFacilityContactDegree(fp.getString("facConDegree"));
        // newStudy.setFacilityRecruitmentStatus(fp.getString("facRecStatus"));
        newStudy.setFacilityState(fp.getString("facState"));
        newStudy.setFacilityZip(fp.getString("facZip"));

        session.setAttribute("newStudy", newStudy);
        if (errors.isEmpty()) {
            forwardPage(Page.UPDATE_STUDY7);
        } else {
            request.setAttribute("formMessages", errors);
            request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
            forwardPage(Page.UPDATE_STUDY6);
        }

    }

    private void confirmStudy6() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        Validator v = new Validator(request);
        v.addValidation("medlineIdentifier", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("url", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("urlDescription", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

        errors = v.validate();

        StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
        newStudy.setMedlineIdentifier(fp.getString("medlineIdentifier"));
        newStudy.setResultsReference(fp.getBoolean("resultsReference"));
        newStudy.setUrl(fp.getString("url"));
        newStudy.setUrlDescription(fp.getString("urlDescription"));
        session.setAttribute("newStudy", newStudy);
        // request.setAttribute("interventions",session.getAttribute("interventions"));
        if (errors.isEmpty()) {
            forwardPage(Page.UPDATE_STUDY8);
        } else {
            request.setAttribute("formMessages", errors);
            forwardPage(Page.UPDATE_STUDY7);
        }
    }

    private void confirmWholeStudy() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        Validator v = new Validator(request);
        errors = v.validate();

        StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
        newStudy.getStudyParameterConfig().setCollectDob(fp.getString("collectDob"));
        newStudy.getStudyParameterConfig().setDiscrepancyManagement(fp.getString("discrepancyManagement"));
        newStudy.getStudyParameterConfig().setGenderRequired(fp.getString("genderRequired"));

        newStudy.getStudyParameterConfig().setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
        newStudy.getStudyParameterConfig().setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
        newStudy.getStudyParameterConfig().setInterviewDateEditable(fp.getString("interviewDateEditable"));
        newStudy.getStudyParameterConfig().setInterviewDateRequired(fp.getString("interviewDateRequired"));
        newStudy.getStudyParameterConfig().setInterviewerNameEditable(fp.getString("interviewerNameEditable"));
        newStudy.getStudyParameterConfig().setInterviewDateDefault(fp.getString("interviewDateDefault"));

        newStudy.getStudyParameterConfig().setSubjectIdGeneration(fp.getString("subjectIdGeneration"));
        newStudy.getStudyParameterConfig().setSubjectPersonIdRequired(fp.getString("subjectPersonIdRequired"));
        newStudy.getStudyParameterConfig().setSubjectIdPrefixSuffix(fp.getString("subjectIdPrefixSuffix"));

        newStudy.getStudyParameterConfig().setPersonIdShownOnCRF(fp.getString("personIdShownOnCRF"));

        session.setAttribute("newStudy", newStudy);

        if (errors.isEmpty()) {
            forwardPage(Page.STUDY_UPDATE_CONFIRM);

        } else {
            request.setAttribute("formMessages", errors);
            forwardPage(Page.UPDATE_STUDY8);
        }

    }

    private void submitStudy() {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());

        StudyBean study1 = (StudyBean) session.getAttribute("newStudy");
        logger.info("study bean to be updated:" + study1.getName());
        study1.setUpdatedDate(new Date());
        study1.setUpdater((UserAccountBean) session.getAttribute("userBean"));
        logger.debug("study's parentId=" + study1.getParentStudyId());
        sdao.update(study1);

        StudyParameterValueBean spv = new StudyParameterValueBean();

        spv.setStudyId(study1.getId());
        spv.setParameter("collectDob");
        spv.setValue(new Integer(study1.getStudyParameterConfig().getCollectDob()).toString());
        updateParameter(spvdao, spv);

        spv.setParameter("discrepancyManagement");
        spv.setValue(study1.getStudyParameterConfig().getDiscrepancyManagement());
        updateParameter(spvdao, spv);

        spv.setParameter("genderRequired");
        spv.setValue(study1.getStudyParameterConfig().getGenderRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectPersonIdRequired");
        spv.setValue(study1.getStudyParameterConfig().getSubjectPersonIdRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewerNameRequired");
        spv.setValue(study1.getStudyParameterConfig().getInterviewerNameRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewerNameDefault");
        spv.setValue(study1.getStudyParameterConfig().getInterviewerNameDefault());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewerNameEditable");
        spv.setValue(study1.getStudyParameterConfig().getInterviewerNameEditable());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateRequired");
        spv.setValue(study1.getStudyParameterConfig().getInterviewDateRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateDefault");
        spv.setValue(study1.getStudyParameterConfig().getInterviewDateDefault());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateEditable");
        spv.setValue(study1.getStudyParameterConfig().getInterviewDateEditable());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectIdGeneration");
        spv.setValue(study1.getStudyParameterConfig().getSubjectIdGeneration());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectIdPrefixSuffix");
        spv.setValue(study1.getStudyParameterConfig().getSubjectIdPrefixSuffix());
        updateParameter(spvdao, spv);

        spv.setParameter("personIdShownOnCRF");
        spv.setValue(study1.getStudyParameterConfig().getPersonIdShownOnCRF());
        updateParameter(spvdao, spv);

        StudyBean curStudy = (StudyBean) session.getAttribute("study");
        if (curStudy != null && study1.getId() == curStudy.getId()) {
            super.currentStudy = study1;
            session.setAttribute("study", study1);
        }
        // update manage_pedigrees for all sites
        ArrayList children = (ArrayList) sdao.findAllByParent(study1.getId());
        for (int i = 0; i < children.size(); i++) {
            StudyBean child = (StudyBean) children.get(i);
            child.setType(study1.getType());// same as parent's type
            child.setUpdatedDate(new Date());
            child.setUpdater(ub);
            sdao.update(child);
            // YW << update "collectDob" and "genderRequired" for sites
            StudyParameterValueBean childspv = new StudyParameterValueBean();
            childspv.setStudyId(child.getId());
            childspv.setParameter("collectDob");
            childspv.setValue(new Integer(study1.getStudyParameterConfig().getCollectDob()).toString());
            updateParameter(spvdao, childspv);
            childspv.setParameter("genderRequired");
            childspv.setValue(study1.getStudyParameterConfig().getGenderRequired());
            updateParameter(spvdao, childspv);
            // YW >>
        }

        session.removeAttribute("newStudy");
        session.removeAttribute("interventions");
    }

    /**
     * Constructs study bean from request-first section
     * 
     * @param request
     * @return
     */
    private StudyBean createStudyBean() {
        FormProcessor fp = new FormProcessor(request);
        StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
        newStudy.setName(fp.getString("name"));
        newStudy.setOfficialTitle(fp.getString("officialTitle"));
        newStudy.setIdentifier(fp.getString("uniqueProId"));
        newStudy.setSecondaryIdentifier(fp.getString("secondProId"));
        newStudy.setPrincipalInvestigator(fp.getString("prinInvestigator"));

        newStudy.setSummary(fp.getString("description"));
        newStudy.setProtocolDescription(fp.getString("protocolDescription"));

        newStudy.setSponsor(fp.getString("sponsor"));
        newStudy.setCollaborators(fp.getString("collaborators"));

        return newStudy;

    }

    /**
     * Updates the study bean with inputs from second section
     * 
     * @param request
     * @return true if study type is Interventional, otherwise false
     */
    private boolean updateStudy2() {
        FormProcessor fp = new FormProcessor(request);
        StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
        // this is not fully supported yet, because the system will not handle
        // studies which are pending
        // or private...
        newStudy.setStatus(Status.get(fp.getInt("statusId")));

        newStudy.setProtocolDateVerification(fp.getDate(INPUT_VER_DATE));

        newStudy.setDatePlannedStart(fp.getDate(INPUT_START_DATE));

        if (StringUtil.isBlank(fp.getString(INPUT_END_DATE))) {
            newStudy.setDatePlannedEnd(null);
        } else {
            newStudy.setDatePlannedEnd(fp.getDate(INPUT_END_DATE));
        }

        newStudy.setPhase(fp.getString("phase"));

        if (fp.getInt("genetic") == 1) {
            newStudy.setGenetic(true);
        } else {
            newStudy.setGenetic(false);
        }

        session.setAttribute("newStudy", newStudy);

        String interventional = resadmin.getString("interventional");
        return interventional.equalsIgnoreCase(newStudy.getProtocolType());

    }

    private void updateStudy3(boolean isInterventional) {
        FormProcessor fp = new FormProcessor(request);
        StudyBean study = (StudyBean) session.getAttribute("newStudy");
        study.setPurpose(fp.getString("purpose"));
        ArrayList interventionArray = new ArrayList();
        if (isInterventional) {
            study.setAllocation(fp.getString("allocation"));
            study.setMasking(fp.getString("masking"));
            study.setControl(fp.getString("control"));
            study.setAssignment(fp.getString("assignment"));
            study.setEndpoint(fp.getString("endpoint"));

            // Handle Interventions-type and name
            // repeat 10 times for each pair on the web page
            StringBuffer interventions = new StringBuffer();

            for (int i = 0; i < 10; i++) {
                String type = fp.getString("interType" + i);
                String name = fp.getString("interName" + i);
                if (!StringUtil.isBlank(type) && !StringUtil.isBlank(name)) {
                    InterventionBean ib = new InterventionBean(fp.getString("interType" + i), fp.getString("interName" + i));
                    interventionArray.add(ib);
                    interventions.append(ib.toString()).append(",");
                }
            }
            study.setInterventions(interventions.toString());

        } else {// type = observational
            study.setDuration(fp.getString("duration"));
            study.setSelection(fp.getString("selection"));
            study.setTiming(fp.getString("timing"));
        }
        session.setAttribute("newStudy", study);
        session.setAttribute("interventions", interventionArray);
    }

    /**
     * Parses the intetventions of a study and divides it into different type
     * and name pairs type and name are separated by '/', and interventions are
     * separated by ',' examples: type1/name1,type2/name2,type3/name3,
     * 
     * @param sb
     * @return
     */
    private ArrayList parseInterventions(StudyBean sb) {
        ArrayList inters = new ArrayList();
        String interventions = sb.getInterventions();
        try {
            if (!StringUtil.isBlank(interventions)) {
                StringTokenizer st = new StringTokenizer(interventions, ",");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    StringTokenizer st1 = new StringTokenizer(s, "/");
                    String type = st1.nextToken();
                    String name = st1.nextToken();
                    InterventionBean ib = new InterventionBean(type, name);
                    inters.add(ib);

                }
            }
        } catch (NoSuchElementException nse) {
            return new ArrayList();
        }
        return inters;

    }

    /**
     * Sets map in request for different JSP pages
     * 
     * @param request
     * @param isInterventional
     */
    private void setMaps(boolean isInterventional, ArrayList interventionArray) {
        if (isInterventional) {
            request.setAttribute("interPurposeMap", CreateStudyServlet.interPurposeMap);
            request.setAttribute("allocationMap", CreateStudyServlet.allocationMap);
            request.setAttribute("maskingMap", CreateStudyServlet.maskingMap);
            request.setAttribute("controlMap", CreateStudyServlet.controlMap);
            request.setAttribute("assignmentMap", CreateStudyServlet.assignmentMap);
            request.setAttribute("endpointMap", CreateStudyServlet.endpointMap);
            request.setAttribute("interTypeMap", CreateStudyServlet.interTypeMap);
            session.setAttribute("interventions", interventionArray);
        } else {
            request.setAttribute("obserPurposeMap", CreateStudyServlet.obserPurposeMap);
            request.setAttribute("selectionMap", CreateStudyServlet.selectionMap);
            request.setAttribute("timingMap", CreateStudyServlet.timingMap);
        }

    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    private void updateParameter(StudyParameterValueDAO spvdao, StudyParameterValueBean spv) {
        StudyParameterValueBean spv1 = spvdao.findByHandleAndStudy(spv.getStudyId(), spv.getParameter());
        if (spv1.getId() > 0) {
            spvdao.update(spv);
        } else {
            spvdao.create(spv);
        }
    }
}

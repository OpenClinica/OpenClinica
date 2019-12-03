/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.NumericComparisonOperator;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.InterventionBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.service.StudyConfigService;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.config.StudyParamNames;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

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
        throw new InsufficientPermissionException(Page.ERROR, resexception.getString("not_admin"), "1");

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

        Study study = (Study) session.getAttribute("newStudy");

        if (study == null) {
            addPageMessage(respage.getString("please_choose_a_study_to_edit"));
            forwardPage(Page.ERROR);
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
                forwardPage(Page.ERROR);

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
            Study newStudy = (Study) session.getAttribute("newStudy");
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
                interventionArray = parseInterventions((Study) session.getAttribute("newStudy"));
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

        Study newStudy = (Study) session.getAttribute("newStudy");
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

        Study newStudy = (Study) session.getAttribute("newStudy");

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

        Study newStudy = (Study) session.getAttribute("newStudy");
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

        Study newStudy = (Study) session.getAttribute("newStudy");
        newStudy.setCollectDob(fp.getString("collectDob"));
        newStudy.setDiscrepancyManagement(fp.getString("discrepancyManagement"));
        newStudy.setGenderRequired(fp.getString("genderRequired"));

        newStudy.setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
        newStudy.setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
        newStudy.setInterviewDateEditable(fp.getString("interviewDateEditable"));
        newStudy.setInterviewDateRequired(fp.getString("interviewDateRequired"));
        newStudy.setInterviewerNameEditable(fp.getString("interviewerNameEditable"));
        newStudy.setInterviewDateDefault(fp.getString("interviewDateDefault"));

        newStudy.setSubjectIdGeneration(fp.getString("subjectIdGeneration"));
        newStudy.setSubjectPersonIdRequired(fp.getString("subjectPersonIdRequired"));
        newStudy.setSubjectIdPrefixSuffix(fp.getString("subjectIdPrefixSuffix"));

        newStudy.setPersonIdShownOnCRF(fp.getString("personIdShownOnCRF"));

        session.setAttribute("newStudy", newStudy);

        if (errors.isEmpty()) {
            forwardPage(Page.STUDY_UPDATE_CONFIRM);

        } else {
            request.setAttribute("formMessages", errors);
            forwardPage(Page.UPDATE_STUDY8);
        }

    }

    private void submitStudy() {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());

        Study study1 = (Study) session.getAttribute("newStudy");
        logger.info("study bean to be updated:" + study1.getName());
        study1.setDateUpdated(new Date());
        study1.setUpdater((UserAccountBean) session.getAttribute("userBean"));
        logger.debug("study's parentId=" + study1.checkAndGetParentStudyId());
        getStudyDao().update(study1);


        Study curStudy = (Study) session.getAttribute("study");
        if (curStudy != null && study1 != null && study1.getStudyId() == curStudy.getStudyId()) {
            super.currentStudy = study1;
            session.setAttribute("study", study1);
        }
        // update manage_pedigrees for all sites
        ArrayList children = (ArrayList) getStudyDao().findAllByParent(study1.getStudyId());
        for (int i = 0; i < children.size(); i++) {
            Study child = (Study) children.get(i);
            child.setDateUpdated(new Date());
            child.setUpdater(ub);
            StudyConfigService scs = new StudyConfigService(sm.getDataSource());
            scs.updateOrCreateSpv(child, StudyParamNames.COLLECT_DOB, study1.getCollectDob());
            scs.updateOrCreateSpv(child, StudyParamNames.GENDER_REQUIRED, study1.getGenderRequired());
            getStudyDao().update(child);
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
    private Study createStudyBean() {
        FormProcessor fp = new FormProcessor(request);
        Study newStudy = (Study) session.getAttribute("newStudy");
        newStudy.setName(fp.getString("name"));
        newStudy.setOfficialTitle(fp.getString("officialTitle"));
        newStudy.setUniqueIdentifier(fp.getString("uniqueProId"));
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
        Study newStudy = (Study) session.getAttribute("newStudy");
        // this is not fully supported yet, because the system will not handle
        // studies which are pending
        // or private...
        newStudy.setStatus(core.org.akaza.openclinica.domain.Status.getByCode(fp.getInt("statusId")));

        newStudy.setProtocolDateVerification(fp.getDate(INPUT_VER_DATE));

        newStudy.setDatePlannedStart(fp.getDate(INPUT_START_DATE));

        if (StringUtil.isBlank(fp.getString(INPUT_END_DATE))) {
            newStudy.setDatePlannedEnd(null);
        } else {
            newStudy.setDatePlannedEnd(fp.getDate(INPUT_END_DATE));
        }

        newStudy.setPhase(fp.getString("phase"));

        session.setAttribute("newStudy", newStudy);

        String interventional = resadmin.getString("interventional");
        return interventional.equalsIgnoreCase(newStudy.getProtocolType());

    }

    private void updateStudy3(boolean isInterventional) {
        FormProcessor fp = new FormProcessor(request);
        Study study = (Study) session.getAttribute("newStudy");
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
    private ArrayList parseInterventions(Study sb) {
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

}

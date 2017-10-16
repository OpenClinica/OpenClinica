/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
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
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringUtils;
import org.akaza.openclinica.dao.core.CoreResources;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class UpdateStudyServletNew extends SecureController {
    public static final String INPUT_START_DATE = "startDate";
    public static final String INPUT_END_DATE = "endDate";
    public static final String INPUT_VER_DATE = "protocolDateVerification";
    public static StudyBean study;
    private CoreResources core;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        FormProcessor fp = new FormProcessor(request);
        Validator v = new Validator(request);
        int studyId = fp.getInt("id");
        studyId = studyId == 0 ? fp.getInt("studyId") : studyId;
        String action = fp.getString("action");
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        boolean isInterventional = false;

        study = (StudyBean) sdao.findByPK(studyId);
        if (study.getId() != currentStudy.getId()) {
            addPageMessage(respage.getString("not_current_study") + respage.getString("change_study_contact_sysadmin"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }

        study.setId(studyId);
        StudyConfigService scs = new StudyConfigService(sm.getDataSource());
        study = scs.setParametersForStudy(study);
        request.setAttribute("studyToView", study);
        request.setAttribute("studyId", studyId + "");
        request.setAttribute("studyPhaseMap", CreateStudyServlet.studyPhaseMap);
        ArrayList statuses = Status.toStudyUpdateMembersList();
        statuses.add(Status.PENDING);
        request.setAttribute("statuses", statuses);

        String interventional = resadmin.getString("interventional");
        isInterventional = interventional.equalsIgnoreCase(study.getProtocolType());

        request.setAttribute("isInterventional", isInterventional ? "1" : "0");
        String protocolType = study.getProtocolTypeKey();

        // A. Hamid. 5001
        if (study.getParentStudyId() > 0) {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            request.setAttribute("parentStudy", parentStudy);
        }

        ArrayList interventionArray = new ArrayList();
        if (isInterventional) {
            interventionArray = parseInterventions(study);
            setMaps(isInterventional, interventionArray);
        } else {
            setMaps(isInterventional, interventionArray);
        }

        if (!action.equals("submit")) {

            // First Load First Form
            if (study.getDatePlannedStart() != null) {
                fp.addPresetValue(INPUT_START_DATE, local_df.format(study.getDatePlannedStart()));
            }
            if (study.getDatePlannedEnd() != null) {
                fp.addPresetValue(INPUT_END_DATE, local_df.format(study.getDatePlannedEnd()));
            }
            if (study.getProtocolDateVerification() != null) {
                fp.addPresetValue(INPUT_VER_DATE, local_df.format(study.getProtocolDateVerification()));
            }
            setPresetValues(fp.getPresetValues());
            // first load 2nd form
        }
        if (study == null) {
            addPageMessage(respage.getString("please_choose_a_study_to_edit"));
            forwardPage(Page.STUDY_LIST_SERVLET);
            return;
        }
        if (action.equals("submit")) {

            validateStudy1(fp, v);
            validateStudy2(fp, new Validator(request));
            validateStudy3(isInterventional, new Validator(request), fp);
            validateStudy4(fp, new Validator(request));
            validateStudy5(fp, new Validator(request));
            validateStudy6(fp, new Validator(request));
            confirmWholeStudy(fp);

            request.setAttribute("studyToView", study);
            if (!errors.isEmpty()) {
                logger.error("found errors : " + errors.toString());
                request.setAttribute("formMessages", errors);

                forwardPage(Page.UPDATE_STUDY_NEW);
            } else {
                study.setProtocolType(protocolType);
                submitStudy(study);
                addPageMessage(respage.getString("the_study_has_been_updated_succesfully"));
                ArrayList pageMessages = (ArrayList) request.getAttribute(PAGE_MESSAGE);
                session.setAttribute("pageMessages", pageMessages);
                response.sendRedirect(request.getContextPath() + "/pages/studymodule");
                // forwardPage(Page.MANAGE_STUDY_MODULE);
            }
        } else {
            forwardPage(Page.UPDATE_STUDY_NEW);
        }
    }

    private void validateStudy1(FormProcessor fp, Validator v) {

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
        study = createStudyBean(fp);
    }

    private void validateStudy2(FormProcessor fp, Validator v) {

        v.addValidation(INPUT_START_DATE, Validator.IS_A_DATE);
        if (!StringUtils.isBlank(fp.getString(INPUT_END_DATE))) {
            v.addValidation(INPUT_END_DATE, Validator.IS_A_DATE);
            // validation for end >= start should be provided here, but outside the scope
            // it would be great to write all validation as one function, the solution provided now is a bad patch
            // Date end = fp.getDateTime(INPUT_END_DATE);
            // Date start = fp.getDateTime(INPUT_START_DATE);
            // if (end.before(start)) {
            // Validator.addError(errors, INPUT_END_DATE,
            // resexception.getString("input_provided_not_occure_after_previous_start_date_time"));
            // }
        }
        if (!StringUtils.isBlank(fp.getString(INPUT_VER_DATE))) {
            v.addValidation(INPUT_VER_DATE, Validator.IS_A_DATE);
        }

        HashMap vStudy2 = v.validate();
        if (vStudy2 != null && vStudy2.size() > 0) {
            errors.putAll(vStudy2);
        }
        vStudy2 = null;

        logger.info("has validation errors");

        if (!StringUtils.isBlank(fp.getString(INPUT_START_DATE))) {
            fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
        }
        if (!StringUtils.isBlank(fp.getString(INPUT_VER_DATE))) {
            fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
        }
        if (!StringUtils.isBlank(fp.getString(INPUT_END_DATE))) {
            fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
        }
        updateStudy2(fp);
        setPresetValues(fp.getPresetValues());

    }

    private void validateStudy3(boolean isInterventional, Validator v, FormProcessor fp) {

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
        updateStudy3(isInterventional, fp);

    }

    private void validateStudy4(FormProcessor fp, Validator v) {

        v.addValidation("conditions", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 500);
        v.addValidation("keywords", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("eligibility", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 500);

        HashMap vStudy4 = v.validate();
        if (vStudy4 != null && vStudy4.size() > 0) {
            errors.putAll(vStudy4);
        }
        vStudy4 = null;

        if (fp.getInt("expectedTotalEnrollment") <= 0) {
            Validator.addError(errors, "expectedTotalEnrollment", respage.getString("expected_total_enrollment_must_be_a_positive_number"));
        }

        study.setConditions(fp.getString("conditions"));
        study.setKeywords(fp.getString("keywords"));
        study.setEligibility(fp.getString("eligibility"));
        study.setGender(fp.getString("gender"));
        if (fp.getString("ageMax").length() > 3) {
            Validator.addError(errors, "ageMax", respage.getString("condition_eligibility_3"));
        }
        study.setAgeMax(fp.getString("ageMax"));

        study.setAgeMin(fp.getString("ageMin"));
        study.setHealthyVolunteerAccepted(fp.getBoolean("healthyVolunteerAccepted"));
        study.setExpectedTotalEnrollment(fp.getInt("expectedTotalEnrollment"));
        request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
    }

    private void validateStudy5(FormProcessor fp, Validator v) {

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

        HashMap vStudy5 = v.validate();
        if (vStudy5 != null && vStudy5.size() > 0) {
            errors.putAll(vStudy5);
        }
        vStudy5 = null;

        study.setFacilityCity(fp.getString("facCity"));
        study.setFacilityContactDegree(fp.getString("facConDrgree"));
        study.setFacilityName(fp.getString("facName"));
        study.setFacilityContactEmail(fp.getString("facConEmail"));
        study.setFacilityContactPhone(fp.getString("facConPhone"));
        study.setFacilityContactName(fp.getString("facConName"));
        study.setFacilityCountry(fp.getString("facCountry"));
        study.setFacilityContactDegree(fp.getString("facConDegree"));
        // newStudy.setFacilityRecruitmentStatus(fp.getString("facRecStatus"));
        study.setFacilityState(fp.getString("facState"));
        study.setFacilityZip(fp.getString("facZip"));

        if (!errors.isEmpty()) {
            request.setAttribute("formMessages", errors);
            request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
        }
    }

    private void validateStudy6(FormProcessor fp, Validator v) {
        v.addValidation("medlineIdentifier", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("url", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("urlDescription", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

        HashMap vStudy6 = v.validate();
        if (vStudy6 != null && vStudy6.size() > 0) {
            errors.putAll(vStudy6);
        }
        vStudy6 = null;

        study.setMedlineIdentifier(fp.getString("medlineIdentifier"));
        study.setResultsReference(fp.getBoolean("resultsReference"));
        study.setUrl(fp.getString("url"));
        study.setUrlDescription(fp.getString("urlDescription"));
        // request.setAttribute("interventions",session.getAttribute(
        // "interventions"));
        if (!errors.isEmpty()) {
            request.setAttribute("formMessages", errors);
        }
    }

    private void confirmWholeStudy(FormProcessor fp) {
        if (study.getStatus().isLocked()) {
            study.getStudyParameterConfig().setDiscrepancyManagement("false");
        } else {
            study.getStudyParameterConfig().setDiscrepancyManagement(fp.getString("discrepancyManagement"));
        }
        study.getStudyParameterConfig().setCollectDob(fp.getString("collectDob"));
        study.getStudyParameterConfig().setGenderRequired(fp.getString("genderRequired"));
        study.getStudyParameterConfig().setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
        study.getStudyParameterConfig().setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
        study.getStudyParameterConfig().setInterviewDateEditable(fp.getString("interviewDateEditable"));
        study.getStudyParameterConfig().setInterviewDateRequired(fp.getString("interviewDateRequired"));
        study.getStudyParameterConfig().setInterviewerNameEditable(fp.getString("interviewerNameEditable"));
        study.getStudyParameterConfig().setInterviewDateDefault(fp.getString("interviewDateDefault"));
        study.getStudyParameterConfig().setSubjectIdGeneration(fp.getString("subjectIdGeneration"));
        study.getStudyParameterConfig().setSubjectPersonIdRequired(fp.getString("subjectPersonIdRequired"));
        study.getStudyParameterConfig().setSubjectIdPrefixSuffix(fp.getString("subjectIdPrefixSuffix"));
        study.getStudyParameterConfig().setPersonIdShownOnCRF(fp.getString("personIdShownOnCRF"));
        study.getStudyParameterConfig().setSecondaryLabelViewable(fp.getString("secondaryLabelViewable"));
        study.getStudyParameterConfig().setAdminForcedReasonForChange(fp.getString("adminForcedReasonForChange"));
        study.getStudyParameterConfig().setEventLocationRequired(fp.getString("eventLocationRequired"));
        if (!errors.isEmpty()) {
            request.setAttribute("formMessages", errors);
        }
    }

    private StudyBean createStudyBean(FormProcessor fp) {
        StudyBean newStudy = study;
        newStudy.setId(fp.getInt("studyId"));
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

    private boolean updateStudy2(FormProcessor fp) {

        study.setOldStatus(study.getStatus());
        study.setStatus(Status.get(fp.getInt("status")));

        if (StringUtil.isBlank(fp.getString(INPUT_VER_DATE))) {
            study.setProtocolDateVerification(null);
        } else {
            study.setProtocolDateVerification(fp.getDate(INPUT_VER_DATE));
        }

        study.setDatePlannedStart(fp.getDate(INPUT_START_DATE));

        if (StringUtil.isBlank(fp.getString(INPUT_END_DATE))) {
            study.setDatePlannedEnd(null);
        } else {
            study.setDatePlannedEnd(fp.getDate(INPUT_END_DATE));
        }

        study.setPhase(fp.getString("phase"));

        if (fp.getInt("genetic") == 1) {
            study.setGenetic(true);
        } else {
            study.setGenetic(false);
        }

        String interventional = resadmin.getString("interventional");
        return interventional.equalsIgnoreCase(study.getProtocolType());
    }

    private void updateStudy3(boolean isInterventional, FormProcessor fp) {

        study.setPurpose(fp.getString("purpose"));
        ArrayList interventionArray = new ArrayList();
        if (isInterventional) {
            study.setAllocation(fp.getString("allocation"));
            study.setMasking(fp.getString("masking"));
            study.setControl(fp.getString("control"));
            study.setAssignment(fp.getString("assignment"));
            study.setEndpoint(fp.getString("endpoint"));

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
        request.setAttribute("interventions", interventionArray);
    }

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

    private void submitStudy(StudyBean newStudy) {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());

        StudyBean study1 = newStudy;
        logger.info("study bean to be updated:" + study1.getName());
        study1.setUpdatedDate(new Date());
        study1.setUpdater((UserAccountBean) session.getAttribute("userBean"));
        sdao.update(study1);

        ArrayList siteList = (ArrayList) sdao.findAllByParent(newStudy.getId());
        if (siteList.size() > 0) {
            sdao.updateSitesStatus(study1);
        }

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

        // BWP 1/12/2009 3169 Update interviewerNameEditable and
        // interviewDateEditable parameters for all sites>>
        List<StudyBean> sites = new ArrayList<StudyBean>();
        sites = (ArrayList) sdao.findAllByParent(newStudy.getId());
        if (sites != null && !sites.isEmpty()) {
            updateInterviewerForSites(newStudy, sites, spvdao, "interviewerNameEditable");
        }
        // >>

        spv.setParameter("interviewDateRequired");
        spv.setValue(study1.getStudyParameterConfig().getInterviewDateRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateDefault");
        spv.setValue(study1.getStudyParameterConfig().getInterviewDateDefault());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateEditable");
        spv.setValue(study1.getStudyParameterConfig().getInterviewDateEditable());
        updateParameter(spvdao, spv);
        // BWP 1/12/2009 3169>>
        if (sites != null && !sites.isEmpty()) {
            updateInterviewerForSites(newStudy, sites, spvdao, "interviewDateEditable");
        }
        // >>
        spv.setParameter("subjectIdGeneration");
        spv.setValue(study1.getStudyParameterConfig().getSubjectIdGeneration());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectIdPrefixSuffix");
        spv.setValue(study1.getStudyParameterConfig().getSubjectIdPrefixSuffix());
        updateParameter(spvdao, spv);

        spv.setParameter("personIdShownOnCRF");
        spv.setValue(study1.getStudyParameterConfig().getPersonIdShownOnCRF());
        updateParameter(spvdao, spv);
        spv.setParameter("secondaryLabelViewable");
        spv.setValue(study1.getStudyParameterConfig().getSecondaryLabelViewable());
        updateParameter(spvdao, spv);

        // tbh, 06/04/2009 3684>>
        spv.setParameter("adminForcedReasonForChange");
        spv.setValue(study1.getStudyParameterConfig().getAdminForcedReasonForChange());
        updateParameter(spvdao, spv);
        // >>

        // AH 08/26/2010 5732
        spv.setParameter("eventLocationRequired");
        spv.setValue(study1.getStudyParameterConfig().getEventLocationRequired());
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
        }
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    private void updateParameter(StudyParameterValueDAO spvdao, StudyParameterValueBean spv) {
        StudyParameterValueBean spv1 = spvdao.findByHandleAndStudy(spv.getStudyId(), spv.getParameter());
        logger.debug("found parameter: " + spv.getParameter());
        if (spv1.getId() > 0) {
            spvdao.update(spv);
            logger.debug("updating");
        } else {
            spvdao.create(spv);
            logger.debug("creating");
        }
    }

    private void updateInterviewerForSites(StudyBean studyBean, List<StudyBean> sites, StudyParameterValueDAO studyParameterValueDAO, String parameterType) {

        StudyParameterValueBean studyParameterValueBean = new StudyParameterValueBean();

        if ("interviewerNameEditable".equalsIgnoreCase(parameterType)) {
            studyParameterValueBean.setParameter("interviewerNameEditable");
            studyParameterValueBean.setValue(studyBean.getStudyParameterConfig().getInterviewerNameEditable());
        } else {
            studyParameterValueBean.setParameter("interviewDateEditable");
            studyParameterValueBean.setValue(studyBean.getStudyParameterConfig().getInterviewDateEditable());
        }
        for (StudyBean siteBean : sites) {
            studyParameterValueBean.setStudyId(siteBean.getId());
            updateParameter(studyParameterValueDAO, studyParameterValueBean);
        }

    }
}
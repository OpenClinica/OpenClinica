/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.dao.service;

import core.org.akaza.openclinica.bean.service.StudyParameterConfig;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.bean.service.StudyParamsConfig;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class StudyConfigService {

    private DataSource ds;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private StudyDao studyDao;

    public StudyConfigService(DataSource ds) {
        this.ds = ds;
    }

    /**
     * @return Returns the ds.
     */
    public DataSource getDs() {
        return ds;
    }

    /**
     * @param ds
     *            The ds to set.
     */
    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    /**
     *  true if the study has a value defined for this parameter o if studyId
     * is a parent study, then this is true iff there is a row for this
     * study/parameter pair in the study_parameter_value table o if studyId is a
     * site, then this is true if: ? * the parameter is inheritable and the
     * studys parent has a defined parameter value; OR ? * the parameter is not
     * inheritable and there is a row for this studyId/parameter pair in the
     * study_parameter_value table
     *
     * @param studyId
     * @param parameterHandle
     * @return
     */
    public String hasDefinedParameterValue(int studyId, String parameterHandle) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);

        if (studyId <= 0 || StringUtil.isBlank(parameterHandle)) {
            return null;
        }

        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(studyId, parameterHandle);
        StudyParameter sp = spvdao.findParameterByHandle(parameterHandle);
        Study study = (Study) studyDao.findByPK(studyId);
        if (spv.getId() > 0) {// there is a row for that study, no matter it
            // is a
            // top study or not
            return spv.getValue();
        }
        int parentId = study.getStudy() != null ? study.getStudy().getStudyId() : 0;
        if (parentId > 0) {
            StudyParameterValueBean spvParent = spvdao.findByHandleAndStudy(parentId, parameterHandle);
            if (spvParent.getId() > 0 && sp.isInheritable()) {
                return spvParent.getValue();
            }

        }
        return null;

    }

    /**
     * This method construct an object which has all the study parameter values
     *
     * @param study
     * @return
     */
    public Study setParametersForStudy(Study study) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        ArrayList parameters = spvdao.findAllParameters();
        List resultparamsList = new ArrayList();
        for (int i = 0; i < parameters.size(); i++) {
            core.org.akaza.openclinica.domain.datamap.StudyParameter sp = (core.org.akaza.openclinica.domain.datamap.StudyParameter) parameters.get(i);
            String handle = sp.getHandle();
            // logger.info("handle:" + handle);
            StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), handle);
            StudyParamsConfig spc = new StudyParamsConfig();
            spc.setParameter(sp);
            spc.setValue(spv);
            resultparamsList.add(spc);
        }
        study.setStudyParameters(resultparamsList);
        return study;

    }

    public Study setParameterValuesForStudy(Study study) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        ArrayList theParameters = spvdao.findParamConfigByStudy(study);
        study.setStudyParameters(theParameters);

        ArrayList parameters = spvdao.findAllParameterValuesByStudy(study);

        for (int i = 0; i < parameters.size(); i++) {
            StudyParameterValueBean spvb = (StudyParameterValueBean) parameters.get(i);
            String parameter = spvb.getParameter();
            if (parameter.equalsIgnoreCase("collectDob")) {
                study.getStudyParameterConfig().setCollectDob(spvb.getValue());
            } else if (parameter.equalsIgnoreCase("genderRequired")) {
                study.getStudyParameterConfig().setGenderRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectPersonIdRequired")) {
                study.getStudyParameterConfig().setSubjectPersonIdRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("discrepancyManagement")) {
                study.getStudyParameterConfig().setDiscrepancyManagement(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdGeneration")) {
                study.getStudyParameterConfig().setSubjectIdGeneration(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                study.getStudyParameterConfig().setSubjectIdPrefixSuffix(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameRequired")) {
                study.getStudyParameterConfig().setInterviewerNameRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameDefault")) {
                study.getStudyParameterConfig().setInterviewerNameDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameEditable")) {
                study.getStudyParameterConfig().setInterviewerNameEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateRequired")) {
                study.getStudyParameterConfig().setInterviewDateRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateDefault")) {
                study.getStudyParameterConfig().setInterviewDateDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateEditable")) {
                study.getStudyParameterConfig().setInterviewDateEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("personIdShownOnCRF")) {
                study.getStudyParameterConfig().setPersonIdShownOnCRF(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("adminForcedReasonForChange")) {
                study.getStudyParameterConfig().setAdminForcedReasonForChange(spvb.getValue());
            }
        }
        return study;

    }

    public Study setParametersForSite(Study site) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        Study parent = (Study) studyDao.findByPK(site.getStudy().getStudyId());
        parent = this.setParameterValuesForStudy(parent);
        site.setStudyParameterConfig(parent.getStudyParameterConfig());
        ArrayList siteParameters = spvdao.findAllParameterValuesByStudy(site);

        for (int i = 0; i < siteParameters.size(); i++) {
            StudyParameterValueBean spvb = (StudyParameterValueBean) siteParameters.get(i);
            String parameter = spvb.getParameter();
            if (parameter.equalsIgnoreCase("collectDob")) {
                site.getStudyParameterConfig().setCollectDob(spvb.getValue());
            } else if (parameter.equalsIgnoreCase("genderRequired")) {
                site.getStudyParameterConfig().setGenderRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectPersonIdRequired")) {
                site.getStudyParameterConfig().setSubjectPersonIdRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("discrepancyManagement")) {
                site.getStudyParameterConfig().setDiscrepancyManagement(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdGeneration")) {
                site.getStudyParameterConfig().setSubjectIdGeneration(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                site.getStudyParameterConfig().setSubjectIdPrefixSuffix(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameRequired")) {
                site.getStudyParameterConfig().setInterviewerNameRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameDefault")) {
                site.getStudyParameterConfig().setInterviewerNameDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameEditable")) {
                site.getStudyParameterConfig().setInterviewerNameEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateRequired")) {
                site.getStudyParameterConfig().setInterviewDateRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateDefault")) {
                site.getStudyParameterConfig().setInterviewDateDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateEditable")) {
                site.getStudyParameterConfig().setInterviewDateEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("personIdShownOnCRF")) {
                site.getStudyParameterConfig().setPersonIdShownOnCRF(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("adminForcedReasonForChange")) {
                site.getStudyParameterConfig().setAdminForcedReasonForChange(spvb.getValue());
            }

            // will add interview name/date features later
        }
        return site;

    }

}

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.dao.service;

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
        int parentId = study.checkAndGetParentStudyId();
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
        if(study.getStudyParameterValues() == null || study.getStudyParameterValues().size() == 0) {
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
            ArrayList theParameters = spvdao.findParamConfigByStudy(study);
            study.setStudyParameters(theParameters);

            ArrayList parameters = spvdao.findAllParameterValuesByStudy(study);

            for (int i = 0; i < parameters.size(); i++) {
                StudyParameterValueBean spvb = (StudyParameterValueBean) parameters.get(i);
                String parameter = spvb.getParameter();
                if (parameter.equalsIgnoreCase("collectDob")) {
                    study.setCollectDob(spvb.getValue());
                } else if (parameter.equalsIgnoreCase("genderRequired")) {
                    study.setGenderRequired(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("subjectPersonIdRequired")) {
                    study.setSubjectPersonIdRequired(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("discrepancyManagement")) {
                    study.setDiscrepancyManagement(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("subjectIdGeneration")) {
                    study.setSubjectIdGeneration(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                    study.setSubjectIdPrefixSuffix(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("interviewerNameRequired")) {
                    study.setInterviewerNameRequired(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("interviewerNameDefault")) {
                    study.setInterviewerNameDefault(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("interviewerNameEditable")) {
                    study.setInterviewerNameEditable(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("interviewDateRequired")) {
                    study.setInterviewDateRequired(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("interviewDateDefault")) {
                    study.setInterviewDateDefault(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("interviewDateEditable")) {
                    study.setInterviewDateEditable(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("personIdShownOnCRF")) {
                    study.setPersonIdShownOnCRF(spvb.getValue());

                } else if (parameter.equalsIgnoreCase("adminForcedReasonForChange")) {
                    study.setAdminForcedReasonForChange(spvb.getValue());
                }
            }
        }
        return study;

    }

    public Study setParametersForSite(Study site) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        Study parent = site.getStudy();
        parent = this.setParameterValuesForStudy(parent);
        site.setStudyParameterValues(parent.getStudyParameterValues());
        ArrayList siteParameters = spvdao.findAllParameterValuesByStudy(site);

        for (int i = 0; i < siteParameters.size(); i++) {
            StudyParameterValueBean spvb = (StudyParameterValueBean) siteParameters.get(i);
            site.setIndividualStudyParameterValue(spvb.getParameter(),spvb.getValue());
            String parameter = spvb.getParameter();
            if (parameter.equalsIgnoreCase("collectDob")) {
                site.setCollectDob(spvb.getValue());
            } else if (parameter.equalsIgnoreCase("genderRequired")) {
                site.setGenderRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectPersonIdRequired")) {
                site.setSubjectPersonIdRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("discrepancyManagement")) {
                site.setDiscrepancyManagement(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdGeneration")) {
                site.setSubjectIdGeneration(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("subjectIdPrefixSuffix")) {
                site.setSubjectIdPrefixSuffix(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameRequired")) {
                site.setInterviewerNameRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameDefault")) {
                site.setInterviewerNameDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewerNameEditable")) {
                site.setInterviewerNameEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateRequired")) {
                site.setInterviewDateRequired(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateDefault")) {
                site.setInterviewDateDefault(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("interviewDateEditable")) {
                site.setInterviewDateEditable(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("personIdShownOnCRF")) {
                site.setPersonIdShownOnCRF(spvb.getValue());

            } else if (parameter.equalsIgnoreCase("adminForcedReasonForChange")) {
                site.setAdminForcedReasonForChange(spvb.getValue());
            }

            // will add interview name/date features later
        }
        return site;

    }

}

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
import core.org.akaza.openclinica.dao.hibernate.StudyParameterDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyParameter;
import core.org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class StudyConfigService {

    private DataSource ds;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

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
    public String hasDefinedParameterValue(int studyId, String parameterHandle, StudyDao studyDao) {
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

    public void updateOrCreateSpv(Study study, String handle, String value){
        boolean paramIsPresent = false;
        if(study.getStudyParameterValues() != null && study.getStudyParameterValues().size() != 0){
            for(StudyParameterValue spv : study.getStudyParameterValues()) {
                if (spv.getStudyParameter().getHandle().equals(handle)) {
                    if(spv.getValue() == null || !spv.getValue().equalsIgnoreCase(value))
                        spv.setValue(value);
                    paramIsPresent = true;
                    break;
                }
            }
        }
        if(!paramIsPresent){
            StudyParameterValue newSpv = new StudyParameterValue();
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
            StudyParameter parameter = spvdao.findParameterByHandle(handle);
            newSpv.setStudyParameter(parameter);
            newSpv.setValue(value);
            newSpv.setStudy(study);
            if(study.getStudyParameterValues() == null)
                study.setStudyParameterValues(new ArrayList<StudyParameterValue>());
            study.getStudyParameterValues().add(newSpv);
        }
    }
}



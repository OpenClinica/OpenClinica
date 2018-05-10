/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.logic.odmExport;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.sql.DataSource;

/**
 * Gather information about an odm study.
 * 
 * @author ywang (May, 2009)
 */

public class OdmStudyBase {
    private StudyBean study;
    private List<StudyEventDefinitionBean> sedBeansInStudy;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmStudyBase() {
    }

    /**
     * In this constructor, study, sedBeansInStudy has been initialized.
     * 
     * @param ds
     * @param study
     */
    public OdmStudyBase(DataSource ds, StudyBean study) {
        if (study == null) {
            logger.info("Study is null!");
            return;
        }
        this.study = study;
        int parentStudyId = this.study.getParentStudyId() > 0 ? this.study.getParentStudyId() : this.study.getId();
        this.sedBeansInStudy = new StudyEventDefinitionDAO(ds).findAllActiveByParentStudyId(parentStudyId);
    }

    public OdmStudyBase setOdmStudyBean(DataSource ds, StudyBean study) {
        OdmStudyBase studyBase = new OdmStudyBase();
        if (study == null) {
            logger.info("Study is null!");
        } else {
            this.study = study;
            int parentStudyId = this.study.getParentStudyId() > 0 ? this.study.getParentStudyId() : this.study.getId();
            this.sedBeansInStudy = new StudyEventDefinitionDAO(ds).findAllActiveByParentStudyId(parentStudyId);
        }
        return studyBase;
    }

    /**
     * 
     * @param ds
     * @param study
     * @param seds
     */
    
    public OdmStudyBase(DataSource ds, StudyBean study,List<StudyEventDefinitionBean> seds) {
        if (study == null) {
            logger.info("Study is null!");
            return;
        }
        this.study = study;
        int parentStudyId = this.study.getParentStudyId() > 0 ? this.study.getParentStudyId() : this.study.getId();
        this.sedBeansInStudy = seds;
    }
    
    
    public void setStudy(StudyBean study) {
        this.study = study;
    }

    public StudyBean getStudy() {
        return this.study;
    }

    public void setSedBeansInStudy(List<StudyEventDefinitionBean> seds) {
        this.sedBeansInStudy = seds;
    }

    public List<StudyEventDefinitionBean> getSedBeansInStudy() {
        return this.sedBeansInStudy;
    }
}

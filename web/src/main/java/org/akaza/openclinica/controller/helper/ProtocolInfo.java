package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.Study;

/**
 * Created by yogi on 3/16/17.
 */

public class ProtocolInfo {

    public ProtocolInfo(String schema, Study study) {
        this.schema = schema;
        this.study = study;
    }

    public ProtocolInfo(String schema, StudyBean study) {
        this.schema = schema;
        this.studyBean = study;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public StudyBean getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(StudyBean studyBean) {
        this.studyBean = studyBean;
    }

    private String schema;
    private Study study;
    private StudyBean studyBean;
}

package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.Study;

/**
 * Created by yogi on 3/16/17.
 */

public class StudyInfoObject {

    public StudyInfoObject(String schema, Study study, UserAccountBean ub, boolean isUserUpdated) {
        this.schema = schema;
        this.study = study;
        this.ub = ub;
        this.isUserUpdated = isUserUpdated;
    }

    public StudyInfoObject(String schema, StudyBean study) {
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

    public boolean isUserUpdated() {
        return isUserUpdated;
    }

    public void setUserUpdated(boolean userUpdated) {
        isUserUpdated = userUpdated;
    }

    private boolean isUserUpdated;

    public UserAccountBean getUb() {
        return ub;
    }

    public void setUb(UserAccountBean ub) {
        this.ub = ub;
    }

    private UserAccountBean ub;
}

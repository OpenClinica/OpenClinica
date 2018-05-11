package org.akaza.openclinica.service.pmanage;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Submission {
    private Study study;

    private Integer study_event_def_id;

    private Integer study_event_def_ordinal;

    private Integer crf_version_id;
    private Integer form_layout_id;

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Integer getStudy_event_def_id() {
        return study_event_def_id;
    }

    public void setStudy_event_def_id(Integer study_event_def_id) {
        this.study_event_def_id = study_event_def_id;
    }

    public Integer getStudy_event_def_ordinal() {
        return study_event_def_ordinal;
    }

    public void setStudy_event_def_ordinal(Integer study_event_def_ordinal) {
        this.study_event_def_ordinal = study_event_def_ordinal;
    }

    public Integer getCrf_version_id() {
        return crf_version_id;
    }

    public void setCrf_version_id(Integer crf_version_id) {
        this.crf_version_id = crf_version_id;
    }

    public Integer getForm_layout_id() {
        return form_layout_id;
    }

    public void setForm_layout_id(Integer form_layout_id) {
        this.form_layout_id = form_layout_id;
    }

}

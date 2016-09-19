package org.akaza.openclinica.service.crfdata.xform;

public class PFormCacheSubjectContextEntry {

    private String studySubjectOid = null;
    private Integer studyEventDefinitionId = null;
    private Integer ordinal = null;
    private String crfVersionOid = null;
    private Integer userAccountId = null;

    public String getStudySubjectOid() {
        return studySubjectOid;
    }
    public void setStudySubjectOid(String studySubjectOid) {
        this.studySubjectOid = studySubjectOid;
    }
    public Integer getStudyEventDefinitionId() {
        return studyEventDefinitionId;
    }
    public void setStudyEventDefinitionId(Integer studyEventDefinitionId) {
        this.studyEventDefinitionId = studyEventDefinitionId;
    }
    public Integer getOrdinal() {
        return ordinal;
    }
    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }
    public String getCrfVersionOid() {
        return crfVersionOid;
    }
    public void setCrfVersionOid(String crfVersionOid) {
        this.crfVersionOid = crfVersionOid;
    }
    public Integer getUserAccountId() {
        return userAccountId;
    }
    public void setUserAccountId(Integer userAccountId) {
        this.userAccountId = userAccountId;
    }
}

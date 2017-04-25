package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.domain.datamap.Study;

/**
 * Created by yogi on 3/16/17.
 */

public class ProtocolInfo {
    public ProtocolInfo(String uniqueStudyId, String schema) {
        this.uniqueStudyId = uniqueStudyId;
        this.schema = schema;
    }
    public ProtocolInfo(String uniqueStudyId, String ocId, String schema) {
        this.uniqueStudyId = uniqueStudyId;
        this.schema = schema;
        this.ocId = ocId;
    }

    public ProtocolInfo(String uniqueStudyId, String ocId, String schema, Study study) {
        this.uniqueStudyId = uniqueStudyId;
        this.schema = schema;
        this.ocId = ocId;
        this.study = study;
    }

    public String getUniqueStudyId() {
        return uniqueStudyId;
    }

    public void setUniqueStudyId(String uniqueStudyId) {
        this.uniqueStudyId = uniqueStudyId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getOcId() {
        return ocId;
    }

    public void setOcId(String ocId) {
        this.ocId = ocId;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    private String uniqueStudyId;
    private String schema;
    private String ocId;
    private Study study;
}

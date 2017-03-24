package org.akaza.openclinica.controller.helper;

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

    String uniqueStudyId;
    String schema;
    String ocId;
}
